package com.yisy.modules.doramcu.hex;

import com.yisy.modules.doramcu.eve.model.AiCharacter;
import com.yisy.modules.doramcu.hex.model.BuildContract;
import com.yisy.modules.doramcu.hex.model.BuildContract.BuildContractStatus;
import com.yisy.modules.doramcu.hex.model.InfrastructureItem;
import com.yisy.modules.doramcu.hex.model.MaterialCost;
import com.yisy.modules.doramcu.hex.model.ProductionChainTracking;
import com.yisy.modules.doramcu.hex.repository.BuildContractRepository;
import com.yisy.modules.doramcu.hex.repository.ProductionChainTrackingRepository;
import com.yisy.modules.doramcu.repository.ai.AiCharacterRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Complete cost calculation engine for the economic production system.
 * <p>
 * Implements the cost formulas from the design document (§7.2), including
 * leadership modifiers for labor cost efficiency. This service calculates
 * material costs, labor costs, and total costs for individual contracts
 * and entire production chains.
 * </p>
 * <p>
 * Cost Formulas (§7.2):
 * <pre>
 * Total Material Cost = Σ(raw_resource_qty × raw_resource_value)
 *                     + Σ(refined_resource_qty × refined_resource_value)
 *                     + Σ(module_qty × module_value)
 * Module Value = Σ(refined_inputs × refined_value) × 1.5  (assembly markup)
 * Refined Value = Σ(raw_inputs × raw_value) × 1.3  (processing markup)
 * Total Labor Cost = crew_size × daily_salary × duration_turns × leadership_modifier
 * Leadership Modifier = 1.0 - ((leader_leadership - 5) × 0.05)
 * Total Cost = Total Material Cost + Total Labor Cost
 * </pre>
 * </p>
 * <p>
 * Leadership Modifier Range:
 * - Leadership 1 → 1.25 modifier (25% more labor cost)
 * - Leadership 5 → 1.0 modifier (baseline)
 * - Leadership 10 → 0.75 modifier (25% less labor cost)
 * </p>
 */
@Service
public class CostCalculatorService {

    private static final Logger log = LoggerFactory.getLogger(CostCalculatorService.class);

    private final MaterialCostService materialCostService;
    private final AiCharacterRepository aiCharacterRepository;
    private final BuildContractRepository buildContractRepository;
    private final ProductionChainTrackingRepository chainRepository;

    public CostCalculatorService(MaterialCostService materialCostService,
                                  AiCharacterRepository aiCharacterRepository,
                                  BuildContractRepository buildContractRepository,
                                  ProductionChainTrackingRepository chainRepository) {
        this.materialCostService = materialCostService;
        this.aiCharacterRepository = aiCharacterRepository;
        this.buildContractRepository = buildContractRepository;
        this.chainRepository = chainRepository;
    }

    // ------------------------------------------------------------------
    // Material cost calculation
    // ------------------------------------------------------------------

    /**
     * Calculate the material cost for a single material by name.
     * Delegates to MaterialCostService for recursive cost calculation.
     *
     * @param materialName the name of the material
     * @return the total material cost in credits
     */
    public BigDecimal calculateMaterialCost(String materialName) {
        return materialCostService.calculateTotalMaterialCost(materialName);
    }

    /**
     * Calculate the total material cost for a build contract by parsing
     * its material requirements JSON and summing the costs.
     *
     * @param contract the build contract
     * @return the total material cost in credits
     */
    public BigDecimal calculateMaterialCostForContract(BuildContract contract) {
        String materialJson = contract.getMaterialRequirementsJson();
        if (materialJson == null || materialJson.isEmpty()) {
            return BigDecimal.ZERO;
        }

        Map<String, Integer> materials = materialCostService.parseJsonQuantityMap(materialJson);
        BigDecimal total = BigDecimal.ZERO;

        for (Map.Entry<String, Integer> entry : materials.entrySet()) {
            BigDecimal unitCost = calculateMaterialCost(entry.getKey());
            BigDecimal lineCost = unitCost.multiply(BigDecimal.valueOf(entry.getValue()));
            total = total.add(lineCost);
        }

        return total;
    }

    // ------------------------------------------------------------------
    // Labor cost calculation
    // ------------------------------------------------------------------

    /**
     * Calculate labor cost with leadership modifier.
     * <p>
     * Formula: crew_size × daily_salary × duration_turns × leadership_modifier
     * Leadership Modifier = 1.0 - ((leader_leadership - 5) × 0.05)
     * </p>
     *
     * @param crewSize number of crew members
     * @param dailySalary daily salary per crew member in credits
     * @param durationTurns number of turns to complete the work
     * @param leadershipRating leader's leadership rating (1-10)
     * @return the total labor cost in credits
     */
    public BigDecimal calculateLaborCost(int crewSize, long dailySalary, int durationTurns, int leadershipRating) {
        double leadershipModifier = calculateLeadershipModifier(leadershipRating);

        BigDecimal baseCost = BigDecimal.valueOf(crewSize)
                .multiply(BigDecimal.valueOf(dailySalary))
                .multiply(BigDecimal.valueOf(durationTurns));

        BigDecimal laborCost = baseCost
                .multiply(BigDecimal.valueOf(leadershipModifier))
                .setScale(2, RoundingMode.HALF_UP);

        return laborCost;
    }

    /**
     * Calculate labor cost for a contract using the leader's AI character data.
     *
     * @param contract the build contract
     * @param leader the leader AI character
     * @return the total labor cost in credits
     */
    public BigDecimal calculateLaborCost(BuildContract contract, AiCharacter leader) {
        int crewSize = contract.getCrewSize() != null ? contract.getCrewSize() : 4;
        long dailySalary = leader.getDailySalary();
        int durationTurns = contract.getEstimatedDurationTurns() != null ? contract.getEstimatedDurationTurns() : 1;
        int leadershipRating = leader.getLeadershipRating();

        return calculateLaborCost(crewSize, dailySalary, durationTurns, leadershipRating);
    }

    /**
     * Calculate the leadership modifier for a given leadership rating.
     * <p>
     * Formula: 1.0 - ((leader_leadership - 5) × 0.05)
     * - Leadership 1 → 1.25 (25% more labor cost)
     * - Leadership 5 → 1.0 (baseline)
     * - Leadership 10 → 0.75 (25% less labor cost)
     * </p>
     *
     * @param leadershipRating the leader's leadership rating (1-10)
     * @return the leadership modifier
     */
    public double calculateLeadershipModifier(int leadershipRating) {
        return 1.0 - ((leadershipRating - 5) * 0.05);
    }

    // ------------------------------------------------------------------
    // Total cost calculation
    // ------------------------------------------------------------------

    /**
     * Calculate the total cost (material + labor) for a contract.
     *
     * @param contract the build contract
     * @param leader the leader AI character
     * @return the total cost in credits
     */
    public BigDecimal calculateTotalCost(BuildContract contract, AiCharacter leader) {
        BigDecimal materialCost = calculateMaterialCostForContract(contract);
        BigDecimal laborCost = calculateLaborCost(contract, leader);
        return materialCost.add(laborCost);
    }

    // ------------------------------------------------------------------
    // Cost reporting
    // ------------------------------------------------------------------

    /**
     * Generate a cost breakdown report for a single build contract.
     *
     * @param contractId the contract ID
     * @return a map containing the cost breakdown, or empty if contract not found
     */
    public Optional<Map<String, Object>> calculateContractCostReport(Long contractId) {
        Optional<BuildContract> contractOpt = buildContractRepository.findById(contractId);
        if (contractOpt.isEmpty()) {
            return Optional.empty();
        }

        BuildContract contract = contractOpt.get();
        Map<String, Object> report = new HashMap<>();
        report.put("contractId", contractId);
        report.put("contractType", contract.getContractType());
        report.put("targetItemName", contract.getTargetItemName());
        report.put("status", contract.getStatus());

        BigDecimal materialCost = calculateMaterialCostForContract(contract);
        report.put("materialCostCredits", materialCost);

        BigDecimal laborCost = BigDecimal.ZERO;
        double leadershipModifier = 1.0;
        int leadershipRating = 5;

        if (contract.getAssignedLeaderId() != null) {
            Optional<AiCharacter> leaderOpt = aiCharacterRepository.findById(contract.getAssignedLeaderId());
            if (leaderOpt.isPresent()) {
                AiCharacter leader = leaderOpt.get();
                leadershipRating = leader.getLeadershipRating();
                leadershipModifier = calculateLeadershipModifier(leadershipRating);
                laborCost = calculateLaborCost(contract, leader);
                report.put("leaderName", leader.getName());
                report.put("leadershipRating", leadershipRating);
            }
        }

        report.put("leadershipModifier", leadershipModifier);
        report.put("laborCostCredits", laborCost);
        report.put("totalCostCredits", materialCost.add(laborCost));

        return Optional.of(report);
    }

    /**
     * Generate an aggregate cost report for all contracts in a production chain.
     *
     * @param chainId the production chain ID
     * @return a map containing the aggregate cost report, or empty if chain not found
     */
    public Optional<Map<String, Object>> calculateChainCostReport(Long chainId) {
        Optional<ProductionChainTracking> chainOpt = chainRepository.findById(chainId);
        if (chainOpt.isEmpty()) {
            return Optional.empty();
        }

        ProductionChainTracking chain = chainOpt.get();
        Map<String, Object> report = new HashMap<>();
        report.put("chainId", chainId);
        report.put("chainName", chain.getChainName());
        report.put("status", chain.getStatus());

        List<BuildContract> allContracts = buildContractRepository.findAll();
        List<BuildContract> chainContracts = new java.util.ArrayList<>();
        for (BuildContract c : allContracts) {
            if (chainId.equals(c.getProductionChainId())) {
                chainContracts.add(c);
            }
        }

        BigDecimal totalMaterialCost = BigDecimal.ZERO;
        BigDecimal totalLaborCost = BigDecimal.ZERO;

        Map<String, BigDecimal> phaseMaterialCosts = new HashMap<>();
        Map<String, BigDecimal> phaseLaborCosts = new HashMap<>();

        for (BuildContract contract : chainContracts) {
            BigDecimal materialCost = calculateMaterialCostForContract(contract);
            totalMaterialCost = totalMaterialCost.add(materialCost);

            BigDecimal laborCost = BigDecimal.ZERO;
            if (contract.getAssignedLeaderId() != null) {
                Optional<AiCharacter> leaderOpt = aiCharacterRepository.findById(contract.getAssignedLeaderId());
                if (leaderOpt.isPresent()) {
                    laborCost = calculateLaborCost(contract, leaderOpt.get());
                }
            }
            totalLaborCost = totalLaborCost.add(laborCost);

            String phase = contract.getContractType() != null ? contract.getContractType().name() : "UNKNOWN";
            phaseMaterialCosts.merge(phase, materialCost, BigDecimal::add);
            phaseLaborCosts.merge(phase, laborCost, BigDecimal::add);
        }

        report.put("totalContracts", chainContracts.size());
        report.put("totalMaterialCostCredits", totalMaterialCost);
        report.put("totalLaborCostCredits", totalLaborCost);
        report.put("totalCostCredits", totalMaterialCost.add(totalLaborCost));
        report.put("phaseMaterialCosts", phaseMaterialCosts);
        report.put("phaseLaborCosts", phaseLaborCosts);

        return Optional.of(report);
    }

    /**
     * Calculate the full cost for building an infrastructure item,
     * including both material and labor costs.
     *
     * @param item the infrastructure item to build
     * @param leader the leader AI character
     * @return a map containing the cost breakdown
     */
    public Map<String, Object> calculateInfrastructureItemCost(InfrastructureItem item, AiCharacter leader) {
        Map<String, Object> report = new HashMap<>();
        report.put("itemName", item.getItemName());
        report.put("itemType", item.getItemType());

        // Calculate material costs from all three sources
        BigDecimal rawResourceCost = BigDecimal.ZERO;
        BigDecimal refinedResourceCost = BigDecimal.ZERO;
        BigDecimal moduleCost = BigDecimal.ZERO;

        Map<String, Integer> rawResources = materialCostService.parseJsonQuantityMap(item.getRawResourcesJson());
        for (Map.Entry<String, Integer> entry : rawResources.entrySet()) {
            BigDecimal unitCost = calculateMaterialCost(entry.getKey());
            rawResourceCost = rawResourceCost.add(unitCost.multiply(BigDecimal.valueOf(entry.getValue())));
        }

        Map<String, Integer> refinedResources = materialCostService.parseJsonQuantityMap(item.getRefinedResourcesJson());
        for (Map.Entry<String, Integer> entry : refinedResources.entrySet()) {
            BigDecimal unitCost = calculateMaterialCost(entry.getKey());
            refinedResourceCost = refinedResourceCost.add(unitCost.multiply(BigDecimal.valueOf(entry.getValue())));
        }

        Map<String, Integer> modules = materialCostService.parseJsonQuantityMap(item.getConstructionModulesJson());
        for (Map.Entry<String, Integer> entry : modules.entrySet()) {
            BigDecimal unitCost = calculateMaterialCost(entry.getKey());
            moduleCost = moduleCost.add(unitCost.multiply(BigDecimal.valueOf(entry.getValue())));
        }

        BigDecimal totalMaterialCost = rawResourceCost.add(refinedResourceCost).add(moduleCost);

        // Calculate labor cost
        int buildTime = item.getBuildTimeTurns() != null ? item.getBuildTimeTurns() : 4;
        int crewGroups = item.getRequiredCrewGroups() != null ? item.getRequiredCrewGroups() : 1;
        int crewSize = 4; // 1 leader + 3 members per group
        BigDecimal laborCost = BigDecimal.ZERO;
        int leadershipRating = 5;
        String leaderName = "N/A";

        if (leader != null) {
            laborCost = calculateLaborCost(crewSize * crewGroups, leader.getDailySalary(), buildTime, leader.getLeadershipRating());
            leadershipRating = leader.getLeadershipRating();
            leaderName = leader.getName();
        }

        report.put("rawResourceCostCredits", rawResourceCost);
        report.put("refinedResourceCostCredits", refinedResourceCost);
        report.put("moduleCostCredits", moduleCost);
        report.put("totalMaterialCostCredits", totalMaterialCost);
        report.put("laborCostCredits", laborCost);
        report.put("totalCostCredits", totalMaterialCost.add(laborCost));
        report.put("buildTimeTurns", buildTime);
        report.put("crewGroupsRequired", crewGroups);
        report.put("leaderName", leaderName);
        report.put("leadershipRating", leadershipRating);
        report.put("leadershipModifier", calculateLeadershipModifier(leadershipRating));

        log.info("Calculated cost for {}: material={}, labor={}, total={}",
                item.getItemName(), totalMaterialCost, laborCost, totalMaterialCost.add(laborCost));

        return report;
    }
}
