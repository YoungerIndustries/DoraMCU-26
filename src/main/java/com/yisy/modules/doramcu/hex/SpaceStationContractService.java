package com.yisy.modules.doramcu.hex;

import com.yisy.modules.doramcu.eve.model.AiCharacter;
import com.yisy.modules.doramcu.hex.dungeon.StationModuleTemplate;
import com.yisy.modules.doramcu.hex.model.BuildContract;
import com.yisy.modules.doramcu.hex.model.BuildContract.BuildContractStatus;
import com.yisy.modules.doramcu.hex.model.ProductionChainTracking;
import com.yisy.modules.doramcu.repository.ai.AiCharacterRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

/**
 * Service for modularizing space station construction into build contracts.
 * <p>
 * Bridges the economic production system with the existing space station builder.
 * Each space station is broken into modular contracts that can be executed by
 * 1 leader + 3 crew members. The service maps station types to module templates
 * and generates the corresponding build contracts with material requirements.
 * </p>
 * <p>
 * Station Module Mapping (from design document §5.2):
 * <pre>
 * MILITARY (5 contracts):
 *   1. Foundation Frame — 400 minerals + 200 energy
 *   2. Command Module — 3 HULL + 2 FACILITY
 *   3. Weapon Platform — 3 WEAPON + 1 HULL
 *   4. Shield Array — 3 SHIELD + 1 POWER
 *   5. Docking Ring — 2 HULL + 1 FACILITY
 *
 * SHIPYARD (4 contracts):
 *   1. Foundation Frame — 600 minerals + 300 energy
 *   2. Command Module — 3 HULL + 2 FACILITY
 *   3. Engine Bay — 3 ENGINE + 1 HULL
 *   4. Docking Ring — 2 HULL + 1 FACILITY
 *
 * MINING (3 contracts):
 *   1. Foundation Frame — 320 minerals + 160 energy
 *   2. Command Module — 2 HULL + 1 FACILITY
 *   3. Cargo Module — 2 HULL + 1 FACILITY
 *
 * RESEARCH (3 contracts):
 *   1. Foundation Frame — 360 minerals + 180 energy
 *   2. Command Module — 2 HULL + 1 FACILITY
 *   3. Research Lab — 2 FACILITY + 1 SHIELD
 *
 * TRADE (3 contracts):
 *   1. Foundation Frame — 240 minerals + 120 energy
 *   2. Command Module — 2 HULL + 1 FACILITY
 *   3. Cargo Module — 2 HULL + 1 FACILITY
 *
 * COLONY (4 contracts):
 *   1. Foundation Frame — 280 minerals + 140 energy
 *   2. Command Module — 2 HULL + 1 FACILITY
 *   3. Crew Quarters — 2 FACILITY
 *   4. Life Support — 1 FACILITY + 1 POWER
 * </pre>
 * </p>
 */
@Service
public class SpaceStationContractService {

    private static final Logger log = LoggerFactory.getLogger(SpaceStationContractService.class);

    private final BuildContractService buildContractService;
    private final ProductionChainService productionChainService;
    private final CostCalculatorService costCalculatorService;
    private final MaterialCostService materialCostService;
    private final SpaceStationBuilder spaceStationBuilder;
    private final AiCharacterRepository aiCharacterRepository;
    private final com.yisy.modules.doramcu.hex.repository.BuildContractRepository contractRepository;

    public SpaceStationContractService(BuildContractService buildContractService,
                                        ProductionChainService productionChainService,
                                        CostCalculatorService costCalculatorService,
                                        MaterialCostService materialCostService,
                                        SpaceStationBuilder spaceStationBuilder,
                                        AiCharacterRepository aiCharacterRepository,
                                        com.yisy.modules.doramcu.hex.repository.BuildContractRepository contractRepository) {
        this.buildContractService = buildContractService;
        this.productionChainService = productionChainService;
        this.costCalculatorService = costCalculatorService;
        this.materialCostService = materialCostService;
        this.spaceStationBuilder = spaceStationBuilder;
        this.aiCharacterRepository = aiCharacterRepository;
        this.contractRepository = contractRepository;
    }

    // ------------------------------------------------------------------
    // Station module mapping
    // ------------------------------------------------------------------

    /**
     * Represents a single module contract definition for a space station.
     */
    public static class StationModuleContract {
        private final String contractName;
        private final String materialRequirementsJson;
        private final int estimatedDurationTurns;
        private final int priority;
        private final StationModuleTemplate.StationModuleType moduleType;
        private final String leaderRoleRequired;

        StationModuleContract(String contractName, String materialRequirementsJson,
                              int estimatedDurationTurns, int priority,
                              StationModuleTemplate.StationModuleType moduleType,
                              String leaderRoleRequired) {
            this.contractName = contractName;
            this.materialRequirementsJson = materialRequirementsJson;
            this.estimatedDurationTurns = estimatedDurationTurns;
            this.priority = priority;
            this.moduleType = moduleType;
            this.leaderRoleRequired = leaderRoleRequired;
        }

        public String getContractName() {
            return contractName;
        }

        public String getMaterialRequirementsJson() {
            return materialRequirementsJson;
        }

        public int getEstimatedDurationTurns() {
            return estimatedDurationTurns;
        }

        public int getPriority() {
            return priority;
        }

        public StationModuleTemplate.StationModuleType getModuleType() {
            return moduleType;
        }

        public String getLeaderRoleRequired() {
            return leaderRoleRequired;
        }
    }

    /**
     * Gets the module contract mapping for a space station type.
     * Each station type maps to a list of module contracts with material requirements.
     *
     * @param stationType the space station type
     * @return list of module contracts in construction order
     */
    public List<StationModuleContract> getStationModuleMapping(SpaceStationType stationType) {
        return switch (stationType) {
            case MILITARY -> getMilitaryStationModules();
            case SHIPYARD -> getShipyardStationModules();
            case MINING -> getMiningStationModules();
            case RESEARCH -> getResearchStationModules();
            case TRADE -> getTradeStationModules();
            case COLONY -> getColonyStationModules();
        };
    }

    private List<StationModuleContract> getMilitaryStationModules() {
        return List.of(
                new StationModuleContract(
                        "Foundation Frame",
                        "{\"HULL_MODULE\":4,\"POWER_CELL\":2}",
                        5, 10,
                        StationModuleTemplate.StationModuleType.CORRIDOR,
                        "Engineer"
                ),
                new StationModuleContract(
                        "Command Module",
                        "{\"HULL_MODULE\":3,\"FACILITY_MODULE\":2}",
                        4, 9,
                        StationModuleTemplate.StationModuleType.COMMAND_MODULE,
                        "Engineer"
                ),
                new StationModuleContract(
                        "Weapon Platform",
                        "{\"WEAPON_MODULE\":3,\"HULL_MODULE\":1}",
                        5, 8,
                        StationModuleTemplate.StationModuleType.WEAPON_PLATFORM,
                        "General"
                ),
                new StationModuleContract(
                        "Shield Array",
                        "{\"SHIELD_MODULE\":3,\"POWER_CELL\":1}",
                        4, 7,
                        StationModuleTemplate.StationModuleType.SHIELD_ARRAY,
                        "Scientist"
                ),
                new StationModuleContract(
                        "Docking Ring",
                        "{\"HULL_MODULE\":2,\"FACILITY_MODULE\":1}",
                        3, 6,
                        StationModuleTemplate.StationModuleType.DOCKING_RING,
                        "Engineer"
                )
        );
    }

    private List<StationModuleContract> getShipyardStationModules() {
        return List.of(
                new StationModuleContract(
                        "Foundation Frame",
                        "{\"HULL_MODULE\":6,\"POWER_CELL\":3}",
                        6, 10,
                        StationModuleTemplate.StationModuleType.CORRIDOR,
                        "Engineer"
                ),
                new StationModuleContract(
                        "Command Module",
                        "{\"HULL_MODULE\":3,\"FACILITY_MODULE\":2}",
                        4, 9,
                        StationModuleTemplate.StationModuleType.COMMAND_MODULE,
                        "Engineer"
                ),
                new StationModuleContract(
                        "Engine Bay",
                        "{\"ENGINE_MODULE\":3,\"HULL_MODULE\":1}",
                        5, 8,
                        StationModuleTemplate.StationModuleType.POWER_MODULE,
                        "Engineer"
                ),
                new StationModuleContract(
                        "Docking Ring",
                        "{\"HULL_MODULE\":2,\"FACILITY_MODULE\":1}",
                        3, 6,
                        StationModuleTemplate.StationModuleType.DOCKING_RING,
                        "Engineer"
                )
        );
    }

    private List<StationModuleContract> getMiningStationModules() {
        return List.of(
                new StationModuleContract(
                        "Foundation Frame",
                        "{\"HULL_MODULE\":3,\"POWER_CELL\":1}",
                        4, 10,
                        StationModuleTemplate.StationModuleType.CORRIDOR,
                        "Engineer"
                ),
                new StationModuleContract(
                        "Command Module",
                        "{\"HULL_MODULE\":2,\"FACILITY_MODULE\":1}",
                        3, 9,
                        StationModuleTemplate.StationModuleType.COMMAND_MODULE,
                        "Engineer"
                ),
                new StationModuleContract(
                        "Cargo Module",
                        "{\"HULL_MODULE\":2,\"FACILITY_MODULE\":1}",
                        3, 8,
                        StationModuleTemplate.StationModuleType.CARGO_MODULE,
                        "Engineer"
                )
        );
    }

    private List<StationModuleContract> getResearchStationModules() {
        return List.of(
                new StationModuleContract(
                        "Foundation Frame",
                        "{\"HULL_MODULE\":4,\"POWER_CELL\":2}",
                        5, 10,
                        StationModuleTemplate.StationModuleType.CORRIDOR,
                        "Engineer"
                ),
                new StationModuleContract(
                        "Command Module",
                        "{\"HULL_MODULE\":2,\"FACILITY_MODULE\":1}",
                        3, 9,
                        StationModuleTemplate.StationModuleType.COMMAND_MODULE,
                        "Engineer"
                ),
                new StationModuleContract(
                        "Research Lab",
                        "{\"FACILITY_MODULE\":2,\"SHIELD_MODULE\":1}",
                        4, 8,
                        StationModuleTemplate.StationModuleType.RESEARCH_LAB,
                        "Scientist"
                )
        );
    }

    private List<StationModuleContract> getTradeStationModules() {
        return List.of(
                new StationModuleContract(
                        "Foundation Frame",
                        "{\"HULL_MODULE\":2,\"POWER_CELL\":1}",
                        3, 10,
                        StationModuleTemplate.StationModuleType.CORRIDOR,
                        "Engineer"
                ),
                new StationModuleContract(
                        "Command Module",
                        "{\"HULL_MODULE\":2,\"FACILITY_MODULE\":1}",
                        3, 9,
                        StationModuleTemplate.StationModuleType.COMMAND_MODULE,
                        "Engineer"
                ),
                new StationModuleContract(
                        "Cargo Module",
                        "{\"HULL_MODULE\":2,\"FACILITY_MODULE\":1}",
                        3, 8,
                        StationModuleTemplate.StationModuleType.CARGO_MODULE,
                        "Trader"
                )
        );
    }

    private List<StationModuleContract> getColonyStationModules() {
        return List.of(
                new StationModuleContract(
                        "Foundation Frame",
                        "{\"HULL_MODULE\":3,\"POWER_CELL\":2}",
                        4, 10,
                        StationModuleTemplate.StationModuleType.CORRIDOR,
                        "Engineer"
                ),
                new StationModuleContract(
                        "Command Module",
                        "{\"HULL_MODULE\":2,\"FACILITY_MODULE\":1}",
                        3, 9,
                        StationModuleTemplate.StationModuleType.COMMAND_MODULE,
                        "Engineer"
                ),
                new StationModuleContract(
                        "Crew Quarters",
                        "{\"FACILITY_MODULE\":2}",
                        3, 8,
                        StationModuleTemplate.StationModuleType.CREW_QUARTERS,
                        "Engineer"
                ),
                new StationModuleContract(
                        "Life Support",
                        "{\"FACILITY_MODULE\":1,\"POWER_CELL\":1}",
                        3, 7,
                        StationModuleTemplate.StationModuleType.LIFE_SUPPORT,
                        "Engineer"
                )
        );
    }

    // ------------------------------------------------------------------
    // Contract generation
    // ------------------------------------------------------------------

    /**
     * Gets the module contracts for a station type without creating them.
     * Useful for previewing what contracts will be generated.
     *
     * @param stationType the space station type
     * @return list of module contracts
     */
    public List<StationModuleContract> getModuleContracts(SpaceStationType stationType) {
        return getStationModuleMapping(stationType);
    }

    /**
     * Creates build contracts for all modules of a space station.
     * Each module becomes a separate CONSTRUCT contract that can be
     * assigned to a crew group (1 leader + 3 members).
     *
     * @param stationType the space station type
     * @param name the station name
     * @param galaxyId the galaxy ID
     * @return list of created build contracts
     */
    @Transactional
    public List<BuildContract> createStationContracts(SpaceStationType stationType, String name, Long galaxyId) {
        List<StationModuleContract> moduleContracts = getStationModuleMapping(stationType);
        List<BuildContract> contracts = new ArrayList<>();

        for (int i = 0; i < moduleContracts.size(); i++) {
            StationModuleContract module = moduleContracts.get(i);

            // Dependencies: each module depends on the previous one
            List<Long> depIds = null;
            if (i > 0 && !contracts.isEmpty()) {
                BuildContract previous = contracts.get(i - 1);
                depIds = List.of(previous.getId());
            }

            BuildContract saved = buildContractService.createContractWithDependencies(
                    BuildContract.ContractType.CONSTRUCT,
                    stationType.name(),
                    name + " - " + module.getContractName(),
                    module.getMaterialRequirementsJson(),
                    module.getEstimatedDurationTurns(),
                    galaxyId,
                    null,
                    depIds
            );
            saved.setPriority(module.getPriority());
            contractRepository.save(saved);
            contracts.add(saved);

            log.info("Created station contract: {} (ID: {})", saved.getTargetItemName(), saved.getId());
        }

        log.info("Created {} contracts for {} station: {}", contracts.size(), stationType, name);
        return contracts;
    }

    // ------------------------------------------------------------------
    // Production chain integration
    // ------------------------------------------------------------------

    /**
     * Creates a full production chain for a space station, including all phases
     * from raw material mining through module assembly to station construction.
     *
     * @param stationType the space station type
     * @param name the station name
     * @param galaxyId the galaxy ID
     * @return the created production chain
     */
    @Transactional
    public ProductionChainTracking createProductionChainForStation(SpaceStationType stationType,
                                                                    String name, Long galaxyId) {
        // Create infrastructure item for the station
        com.yisy.modules.doramcu.hex.model.InfrastructureItem item = new com.yisy.modules.doramcu.hex.model.InfrastructureItem();
        item.setItemType(com.yisy.modules.doramcu.hex.model.InfrastructureItem.InfrastructureItemType.SPACE_STATION);
        item.setItemName(name);
        item.setDescription(stationType.name() + " space station: " + name);
        item.setBuildTimeTurns(calculateTotalBuildTime(stationType));
        item.setRequiredCrewGroups(getStationModuleMapping(stationType).size());

        // Calculate total material requirements
        Map<String, Integer> totalMaterials = new HashMap<>();
        for (StationModuleContract module : getStationModuleMapping(stationType)) {
            Map<String, Integer> materials = materialCostService.parseJsonQuantityMap(module.getMaterialRequirementsJson());
            for (Map.Entry<String, Integer> entry : materials.entrySet()) {
                totalMaterials.merge(entry.getKey(), entry.getValue(), Integer::sum);
            }
        }
        item.setConstructionModulesJson(serializeJsonQuantityMap(totalMaterials));

        // Create the production chain
        ProductionChainTracking chain = productionChainService.createProductionChain(item, galaxyId);

        // Generate contracts for the chain
        productionChainService.generateContracts(chain);

        log.info("Created production chain for {} station: {} (chain ID: {})", stationType, name, chain.getId());
        return chain;
    }

    // ------------------------------------------------------------------
    // Cost calculation
    // ------------------------------------------------------------------

    /**
     * Calculates the total cost for building a space station, including
     * all module materials and labor costs with leadership modifier.
     *
     * @param stationType the space station type
     * @param leader the leader AI character
     * @return map containing the cost breakdown
     */
    public Map<String, Object> calculateStationCost(SpaceStationType stationType, AiCharacter leader) {
        Map<String, Object> report = new HashMap<>();
        report.put("stationType", stationType);

        int leadershipRating = 5;
        String leaderName = "N/A";
        long dailySalary = 100;

        if (leader != null) {
            leaderName = leader.getName();
            leadershipRating = leader.getLeadershipRating();
            dailySalary = leader.getDailySalary();
        }
        report.put("leaderName", leaderName);
        report.put("leadershipRating", leadershipRating);
        report.put("leadershipModifier", costCalculatorService.calculateLeadershipModifier(leadershipRating));

        List<StationModuleContract> modules = getStationModuleMapping(stationType);
        BigDecimal totalMaterialCost = BigDecimal.ZERO;
        BigDecimal totalLaborCost = BigDecimal.ZERO;

        Map<String, Object> moduleCosts = new LinkedHashMap<>();

        for (StationModuleContract module : modules) {
            Map<String, Object> moduleReport = new HashMap<>();

            // Material cost
            BigDecimal moduleMaterialCost = BigDecimal.ZERO;
            Map<String, Integer> materials = materialCostService.parseJsonQuantityMap(module.getMaterialRequirementsJson());
            for (Map.Entry<String, Integer> entry : materials.entrySet()) {
                BigDecimal unitCost = materialCostService.calculateTotalMaterialCost(entry.getKey());
                moduleMaterialCost = moduleMaterialCost.add(
                        unitCost.multiply(BigDecimal.valueOf(entry.getValue()))
                );
            }
            moduleReport.put("materialCostCredits", moduleMaterialCost);

            // Labor cost
            BigDecimal moduleLaborCost = costCalculatorService.calculateLaborCost(
                    4, dailySalary, module.getEstimatedDurationTurns(), leadershipRating
            );
            moduleReport.put("laborCostCredits", moduleLaborCost);
            moduleReport.put("totalCostCredits", moduleMaterialCost.add(moduleLaborCost));
            moduleReport.put("durationTurns", module.getEstimatedDurationTurns());

            moduleCosts.put(module.getContractName(), moduleReport);

            totalMaterialCost = totalMaterialCost.add(moduleMaterialCost);
            totalLaborCost = totalLaborCost.add(moduleLaborCost);
        }

        report.put("moduleCosts", moduleCosts);
        report.put("totalMaterialCostCredits", totalMaterialCost);
        report.put("totalLaborCostCredits", totalLaborCost);
        report.put("totalCostCredits", totalMaterialCost.add(totalLaborCost));
        report.put("totalBuildTimeTurns", calculateTotalBuildTime(stationType));
        report.put("totalModuleCount", modules.size());

        log.info("Calculated cost for {} station: material={}, labor={}, total={}",
                stationType, totalMaterialCost, totalLaborCost, totalMaterialCost.add(totalLaborCost));

        return report;
    }

    // ------------------------------------------------------------------
    // Helper methods
    // ------------------------------------------------------------------

    /**
     * Calculates the total build time for a station type.
     * This is the sum of all module build times (sequential construction).
     *
     * @param stationType the space station type
     * @return total build time in turns
     */
    private int calculateTotalBuildTime(SpaceStationType stationType) {
        int total = 0;
        for (StationModuleContract module : getStationModuleMapping(stationType)) {
            total += module.getEstimatedDurationTurns();
        }
        return total;
    }

    /**
     * Serializes a map of material names to quantities as a JSON string.
     * Simple manual serialization to avoid external JSON library dependency.
     */
    private String serializeJsonQuantityMap(Map<String, Integer> map) {
        if (map == null || map.isEmpty()) return "{}";
        StringBuilder sb = new StringBuilder();
        sb.append('{');
        int i = 0;
        for (Map.Entry<String, Integer> entry : map.entrySet()) {
            if (i > 0) sb.append(',');
            sb.append('"').append(entry.getKey()).append("\":").append(entry.getValue());
            i++;
        }
        sb.append('}');
        return sb.toString();
    }
}
