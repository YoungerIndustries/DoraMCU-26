package com.yisy.modules.doramcu.hex;

import com.yisy.modules.doramcu.hex.model.BuildContract;
import com.yisy.modules.doramcu.hex.model.BuildContract.BuildContractStatus;
import com.yisy.modules.doramcu.hex.model.InfrastructureItem;
import com.yisy.modules.doramcu.hex.model.ProductionChainTracking.ProductionChainStatus;
import com.yisy.modules.doramcu.hex.model.MaterialCost;
import com.yisy.modules.doramcu.hex.model.ProductionChainTracking;
import com.yisy.modules.doramcu.hex.repository.ProductionChainTrackingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Plans and manages full production chains from raw materials to final products.
 * <p>
 * A production chain traces the entire process of building an infrastructure item,
 * from mining raw resources through processing, assembly, and final construction.
 * This service generates all necessary build contracts with proper dependency tracking
 * so that contracts in earlier phases complete before later phases begin.
 * </p>
 * <p>
 * Chain lifecycle: PLANNED → IN_PROGRESS → COMPLETED (or FAILED)
 * </p>
 * <p>
 * Phase ordering:
 * 1. MINE — extract raw resources (no dependencies)
 * 2. PROCESS — convert raw to refined resources (depends on MINE)
 * 3. ASSEMBLE — build construction modules from refined resources (depends on PROCESS)
 * 4. CONSTRUCT — build the final infrastructure item (depends on ASSEMBLE)
 * </p>
 */
@Service
public class ProductionChainService {

    private static final Logger log = LoggerFactory.getLogger(ProductionChainService.class);

    private final ProductionChainTrackingRepository chainRepository;
    private final BuildContractService buildContractService;
    private final MaterialCostService materialCostService;

    public ProductionChainService(ProductionChainTrackingRepository chainRepository,
                                   BuildContractService buildContractService,
                                   MaterialCostService materialCostService) {
        this.chainRepository = chainRepository;
        this.buildContractService = buildContractService;
        this.materialCostService = materialCostService;
    }

    // ------------------------------------------------------------------
    // Chain creation
    // ------------------------------------------------------------------

    /**
     * Create a full production chain for an infrastructure item.
     * This plans the chain but does not yet generate contracts.
     *
     * @param infrastructureItem the item to build
     * @param galaxyId the galaxy where the chain takes place
     * @return the created production chain
     */
    @Transactional
    public ProductionChainTracking createProductionChain(InfrastructureItem infrastructureItem, Long galaxyId) {
        ProductionChainTracking chain = new ProductionChainTracking();
        chain.setChainName(infrastructureItem.getItemName() + " - Full Production Chain");
        chain.setTargetItemId(infrastructureItem.getId());
        chain.setTotalPhases(4);
        chain.setTotalMaterialCostCredits(infrastructureItem.getLaborCostCredits());
        chain.setTotalLaborCostCredits(infrastructureItem.getLaborCostCredits());
        chain.setTotalDurationTurns(infrastructureItem.getBuildTimeTurns());
        chain.setMinCrewsRequired(infrastructureItem.getRequiredCrewGroups() != null ? infrastructureItem.getRequiredCrewGroups() : 1);

        // Build phase breakdown JSON
        Map<String, Object> breakdown = new HashMap<>();
        List<Map<String, Object>> phases = new ArrayList<>();

        Map<String, Object> minePhase = new HashMap<>();
        minePhase.put("name", "MINE");
        minePhase.put("description", "Extract raw resources");
        minePhase.put("contracts", new ArrayList<>());
        phases.add(minePhase);

        Map<String, Object> processPhase = new HashMap<>();
        processPhase.put("name", "PROCESS");
        processPhase.put("description", "Convert raw to refined resources");
        processPhase.put("contracts", new ArrayList<>());
        phases.add(processPhase);

        Map<String, Object> assemblePhase = new HashMap<>();
        assemblePhase.put("name", "ASSEMBLE");
        assemblePhase.put("description", "Build construction modules");
        assemblePhase.put("contracts", new ArrayList<>());
        phases.add(assemblePhase);

        Map<String, Object> constructPhase = new HashMap<>();
        constructPhase.put("name", "CONSTRUCT");
        constructPhase.put("description", "Build final infrastructure item");
        constructPhase.put("contracts", new ArrayList<>());
        phases.add(constructPhase);

        breakdown.put("phases", phases);
        breakdown.put("target_item", infrastructureItem.getItemName());
        breakdown.put("galaxy_id", galaxyId);

        chain.setPhaseBreakdownJson(mapToJson(breakdown));

        ProductionChainTracking saved = chainRepository.save(chain);
        log.info("Created production chain {} for {} in galaxy {}", saved.getId(), infrastructureItem.getItemName(), galaxyId);
        return saved;
    }

    // ------------------------------------------------------------------
    // Contract generation
    // ------------------------------------------------------------------

    /**
     * Auto-generate all build contracts from a production chain.
     * This decomposes the infrastructure item into phases and creates
     * contracts with proper dependency ordering.
     *
     * @param chain the production chain to generate contracts for
     * @return the updated chain with total contracts count
     */
    @Transactional
    public ProductionChainTracking generateContracts(ProductionChainTracking chain) {
        Optional<InfrastructureItem> itemOpt = materialCostService.getInfrastructureItem(chain.getTargetItemId());
        if (itemOpt.isEmpty()) {
            throw new IllegalArgumentException("Infrastructure item not found: " + chain.getTargetItemId());
        }
        InfrastructureItem item = itemOpt.get();
        Long galaxyId = chain.getTargetItemId(); // use target item ID as galaxy reference

        List<BuildContract> mineContracts = new ArrayList<>();
        List<BuildContract> processContracts = new ArrayList<>();
        List<BuildContract> assembleContracts = new ArrayList<>();
        List<BuildContract> constructContracts = new ArrayList<>();

        // Phase 1: MINE — create contracts for each raw resource
        Map<String, Integer> rawResources = materialCostService.parseJsonQuantityMap(item.getRawResourcesJson());
        for (Map.Entry<String, Integer> entry : rawResources.entrySet()) {
            BuildContract contract = buildContractService.createContractWithDependencies(
                    BuildContract.ContractType.MINE,
                    "RAW_RESOURCE",
                    entry.getKey(),
                    entry.getKey() + ": " + entry.getValue(),
                    1,
                    galaxyId,
                    chain.getId(),
                    null
            );
            mineContracts.add(contract);
        }

        // Phase 2: PROCESS — create contracts for each refined resource
        Map<String, Integer> refinedResources = materialCostService.parseJsonQuantityMap(item.getRefinedResourcesJson());
        for (Map.Entry<String, Integer> entry : refinedResources.entrySet()) {
            Optional<MaterialCost> materialOpt = materialCostService.getMaterialCost(entry.getKey());
            int processingTime = materialOpt.map(MaterialCost::getProcessingTimeTurns).orElse(2);

            BuildContract contract = buildContractService.createContractWithDependencies(
                    BuildContract.ContractType.PROCESS,
                    "REFINED_RESOURCE",
                    entry.getKey(),
                    entry.getKey() + ": " + entry.getValue(),
                    processingTime,
                    galaxyId,
                    chain.getId(),
                    getContractIds(mineContracts)
            );
            processContracts.add(contract);
        }

        // Phase 3: ASSEMBLE — create contracts for each construction module
        Map<String, Integer> constructionModules = materialCostService.parseJsonQuantityMap(item.getConstructionModulesJson());
        for (Map.Entry<String, Integer> entry : constructionModules.entrySet()) {
            Optional<MaterialCost> materialOpt = materialCostService.getMaterialCost(entry.getKey());
            int processingTime = materialOpt.map(MaterialCost::getProcessingTimeTurns).orElse(3);

            BuildContract contract = buildContractService.createContractWithDependencies(
                    BuildContract.ContractType.ASSEMBLE,
                    "CONSTRUCTION_MODULE",
                    entry.getKey(),
                    entry.getKey() + ": " + entry.getValue(),
                    processingTime,
                    galaxyId,
                    chain.getId(),
                    getContractIds(processContracts)
            );
            assembleContracts.add(contract);
        }

        // Phase 4: CONSTRUCT — create the final construction contract
        BuildContract constructContract = buildContractService.createContractWithDependencies(
                BuildContract.ContractType.CONSTRUCT,
                item.getItemType().name(),
                item.getItemName(),
                "{}",
                item.getBuildTimeTurns() != null ? item.getBuildTimeTurns() : 4,
                galaxyId,
                chain.getId(),
                getContractIds(assembleContracts)
        );
        constructContracts.add(constructContract);

        // Update chain with total contracts
        int totalContracts = mineContracts.size() + processContracts.size() +
                assembleContracts.size() + constructContracts.size();
        chain.setTotalContracts(totalContracts);

        ProductionChainTracking saved = chainRepository.save(chain);
        log.info("Generated {} contracts for chain {}: {} MINE, {} PROCESS, {} ASSEMBLE, {} CONSTRUCT",
                totalContracts, chain.getId(),
                mineContracts.size(), processContracts.size(),
                assembleContracts.size(), constructContracts.size());
        return saved;
    }

    // ------------------------------------------------------------------
    // Dependency tracking
    // ------------------------------------------------------------------

    /**
     * Get contracts whose dependencies are all met (all prerequisites COMPLETED).
     * These contracts are ready to be assigned to crew groups.
     *
     * @param chainId the production chain ID
     * @return list of ready contracts
     */
    public List<BuildContract> getReadyContracts(Long chainId) {
        List<BuildContract> pendingContracts = buildContractService.getContractsByStatus(BuildContractStatus.PENDING);
        List<BuildContract> ready = new ArrayList<>();

        for (BuildContract contract : pendingContracts) {
            if (!chainId.equals(contract.getProductionChainId())) {
                continue;
            }
            if (areDependenciesMet(contract)) {
                ready.add(contract);
            }
        }

        return ready;
    }

    /**
     * Check if all dependencies for a contract are met (all prerequisites COMPLETED).
     *
     * @param contract the contract to check
     * @return true if all dependencies are completed or there are no dependencies
     */
    private boolean areDependenciesMet(BuildContract contract) {
        String depsJson = contract.getDependenciesJson();
        if (depsJson == null || depsJson.isEmpty()) {
            return true;
        }

        List<Long> depIds = parseDependencyIds(depsJson);
        for (Long depId : depIds) {
            Optional<BuildContract> dep = buildContractService.getContract(depId);
            if (dep.isEmpty() || dep.get().getStatus() != BuildContractStatus.COMPLETED) {
                return false;
            }
        }
        return true;
    }

    // ------------------------------------------------------------------
    // Phase management
    // ------------------------------------------------------------------

    /**
     * Mark a phase as complete by checking if all contracts in that phase are done.
     *
     * @param chainId the production chain ID
     * @param phaseName the phase name (MINE, PROCESS, ASSEMBLE, CONSTRUCT)
     * @return true if the phase is complete
     */
    public boolean isPhaseComplete(Long chainId, String phaseName) {
        List<BuildContract> chainContracts = buildContractService.getContractsByStatus(BuildContractStatus.COMPLETED);
        Set<String> completedContractIds = new HashSet<>();
        for (BuildContract c : chainContracts) {
            if (chainId.equals(c.getProductionChainId())) {
                completedContractIds.add(c.getId().toString());
            }
        }

        // Parse phase breakdown to get contract IDs for this phase
        // For simplicity, check by contract type
        BuildContract.ContractType contractType;
        switch (phaseName.toUpperCase()) {
            case "MINE": contractType = BuildContract.ContractType.MINE; break;
            case "PROCESS": contractType = BuildContract.ContractType.PROCESS; break;
            case "ASSEMBLE": contractType = BuildContract.ContractType.ASSEMBLE; break;
            case "CONSTRUCT": contractType = BuildContract.ContractType.CONSTRUCT; break;
            default: return false;
        }

        List<BuildContract> typeContracts = buildContractService.getContractsByTypeAndStatus(contractType, BuildContractStatus.COMPLETED);
        for (BuildContract c : typeContracts) {
            if (chainId.equals(c.getProductionChainId())) {
                completedContractIds.add(c.getId().toString());
            }
        }

        // Check if all contracts of this type in the chain are completed
        List<BuildContract> allOfType = buildContractService.getContractsByType(contractType);
        for (BuildContract c : allOfType) {
            if (chainId.equals(c.getProductionChainId())) {
                if (!completedContractIds.contains(c.getId().toString())) {
                    return false;
                }
            }
        }
        return true;
    }

    // ------------------------------------------------------------------
    // Progress tracking
    // ------------------------------------------------------------------

    /**
     * Recalculate aggregate progress from all child contracts.
     * Updates the chain status based on contract completion.
     *
     * @param chainId the production chain ID
     * @return the updated chain
     */
    @Transactional
    public ProductionChainTracking updateChainProgress(Long chainId) {
        Optional<ProductionChainTracking> opt = chainRepository.findById(chainId);
        if (opt.isEmpty()) {
            throw new IllegalArgumentException("Production chain not found: " + chainId);
        }
        ProductionChainTracking chain = opt.get();

        // Get all contracts for this chain
        List<BuildContract> allContracts = new ArrayList<>();
        for (BuildContractStatus status : BuildContractStatus.values()) {
            allContracts.addAll(buildContractService.getContractsByStatus(status));
        }

        List<BuildContract> chainContracts = new ArrayList<>();
        for (BuildContract c : allContracts) {
            if (chainId.equals(c.getProductionChainId())) {
                chainContracts.add(c);
            }
        }

        if (chainContracts.isEmpty()) {
            return chain;
        }

        // Calculate average progress
        int totalProgress = 0;
        int completedCount = 0;
        int failedCount = 0;
        for (BuildContract c : chainContracts) {
            totalProgress += c.getProgressPercent() != null ? c.getProgressPercent() : 0;
            if (c.getStatus() == BuildContractStatus.COMPLETED) {
                completedCount++;
            } else if (c.getStatus() == BuildContractStatus.FAILED) {
                failedCount++;
            }
        }

        int avgProgress = totalProgress / chainContracts.size();

        // Update chain status
        if (failedCount > 0 && completedCount + failedCount == chainContracts.size()) {
            chain.setStatus(ProductionChainStatus.FAILED);
            chain.setCompletedAt(LocalDateTime.now());
        } else if (completedCount == chainContracts.size()) {
            chain.setStatus(ProductionChainStatus.COMPLETED);
            chain.setCompletedAt(LocalDateTime.now());
        } else if (completedCount > 0 || failedCount > 0) {
            chain.setStatus(ProductionChainStatus.IN_PROGRESS);
        }

        chainRepository.save(chain);
        log.info("Updated chain {} progress: {}% ({} completed, {} failed, {} total)",
                chainId, avgProgress, completedCount, failedCount, chainContracts.size());
        return chain;
    }

    // ------------------------------------------------------------------
    // Chain completion and failure
    // ------------------------------------------------------------------

    /**
     * Mark the entire chain as completed.
     *
     * @param chainId the chain ID
     * @return the updated chain
     */
    @Transactional
    public ProductionChainTracking completeChain(Long chainId) {
        Optional<ProductionChainTracking> opt = chainRepository.findById(chainId);
        if (opt.isEmpty()) {
            throw new IllegalArgumentException("Production chain not found: " + chainId);
        }
        ProductionChainTracking chain = opt.get();
        chain.setStatus(ProductionChainStatus.COMPLETED);
        chain.setCompletedAt(LocalDateTime.now());

        ProductionChainTracking saved = chainRepository.save(chain);
        log.info("Completed production chain {} ({})", chainId, chain.getChainName());
        return saved;
    }

    /**
     * Mark the chain as failed with a reason.
     *
     * @param chainId the chain ID
     * @param reason the failure reason
     * @return the updated chain
     */
    @Transactional
    public ProductionChainTracking failChain(Long chainId, String reason) {
        Optional<ProductionChainTracking> opt = chainRepository.findById(chainId);
        if (opt.isEmpty()) {
            throw new IllegalArgumentException("Production chain not found: " + chainId);
        }
        ProductionChainTracking chain = opt.get();
        chain.setStatus(ProductionChainStatus.FAILED);
        chain.setCompletedAt(LocalDateTime.now());

        ProductionChainTracking saved = chainRepository.save(chain);
        log.info("Failed production chain {}: {}", chainId, reason);
        return saved;
    }

    // ------------------------------------------------------------------
    // Lookup
    // ------------------------------------------------------------------

    /**
     * Get a production chain by ID.
     *
     * @param chainId the chain ID
     * @return optional production chain
     */
    public Optional<ProductionChainTracking> getChain(Long chainId) {
        return chainRepository.findById(chainId);
    }

    /**
     * Get all production chains for a target item.
     *
     * @param targetItemId the infrastructure item ID
     * @return list of production chains
     */
    public List<ProductionChainTracking> getChainsByTargetItem(Long targetItemId) {
        return chainRepository.findByTargetItemIdAndStatus(targetItemId, null);
    }

    /**
     * Look up an infrastructure item by ID.
     * Delegates to MaterialCostService.
     *
     * @param itemId the infrastructure item ID
     * @return optional infrastructure item
     */
    public Optional<InfrastructureItem> getInfrastructureItem(Long itemId) {
        return materialCostService.getInfrastructureItem(itemId);
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    private List<Long> getContractIds(List<BuildContract> contracts) {
        List<Long> ids = new ArrayList<>();
        for (BuildContract c : contracts) {
            ids.add(c.getId());
        }
        return ids;
    }

    private List<Long> parseDependencyIds(String depsJson) {
        List<Long> ids = new ArrayList<>();
        if (depsJson == null || depsJson.isEmpty()) {
            return ids;
        }
        String content = depsJson.trim();
        if (content.startsWith("[")) content = content.substring(1);
        if (content.endsWith("]")) content = content.substring(0, content.length() - 1);
        String[] parts = content.split(",");
        for (String part : parts) {
            part = part.trim();
            try {
                ids.add(Long.parseLong(part));
            } catch (NumberFormatException e) {
                log.warn("Failed to parse dependency ID: {}", part);
            }
        }
        return ids;
    }

    private String mapToJson(Map<String, Object> map) {
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (!first) sb.append(",");
            sb.append("\"").append(entry.getKey()).append("\":");
            Object value = entry.getValue();
            if (value instanceof String) {
                sb.append("\"").append(value).append("\"");
            } else if (value instanceof Number) {
                sb.append(value);
            } else if (value instanceof List) {
                sb.append(listToJson((List<?>) value));
            } else if (value instanceof Map) {
                sb.append(mapToJson((Map<String, Object>) value));
            }
            first = false;
        }
        sb.append("}");
        return sb.toString();
    }

    private String listToJson(List<?> list) {
        StringBuilder sb = new StringBuilder("[");
        boolean first = true;
        for (Object item : list) {
            if (!first) sb.append(",");
            if (item instanceof String) {
                sb.append("\"").append(item).append("\"");
            } else if (item instanceof Number) {
                sb.append(item);
            } else if (item instanceof Map) {
                sb.append(mapToJson((Map<String, Object>) item));
            } else if (item instanceof List) {
                sb.append(listToJson((List<?>) item));
            }
            first = false;
        }
        sb.append("]");
        return sb.toString();
    }
}
