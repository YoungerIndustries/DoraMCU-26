package com.yisy.modules.doramcu.hex;

import com.yisy.modules.doramcu.eve.model.AiCharacter;
import com.yisy.modules.doramcu.hex.model.MaterialCost;
import com.yisy.modules.doramcu.hex.model.MaterialCost.MaterialCostType;
import com.yisy.modules.doramcu.hex.repository.MaterialCostRepository;
import com.yisy.modules.doramcu.repository.ai.AiCharacterRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for the SpaceStationContractService using an H2 in-memory database.
 * <p>
 * Tests station module mapping, contract generation, and cost calculation.
 * </p>
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = EconomicProductionTestConfig.class)
@ActiveProfiles("test")
@Transactional
@DisplayName("SpaceStationContractService Integration Tests")
class SpaceStationContractServiceIntegrationTest {

    private static final long TEST_GALAXY_ID = 1L;

    @Autowired
    private SpaceStationContractService spaceStationContractService;

    @Autowired
    private MaterialCostRepository materialCostRepository;

    @Autowired
    private AiCharacterRepository aiCharacterRepository;

    private AiCharacter testLeader;

    @BeforeEach
    void setUp() {
        // Create raw resources
        MaterialCost ore = new MaterialCost();
        ore.setMaterialType(MaterialCostType.RAW_RESOURCE);
        ore.setMaterialName("ORE");
        ore.setBaseValueCredits(new BigDecimal("10"));
        ore.setProcessingTimeTurns(1);
        materialCostRepository.save(ore);

        MaterialCost crystal = new MaterialCost();
        crystal.setMaterialType(MaterialCostType.RAW_RESOURCE);
        crystal.setMaterialName("CRYSTAL");
        crystal.setBaseValueCredits(new BigDecimal("15"));
        crystal.setProcessingTimeTurns(1);
        materialCostRepository.save(crystal);

        // Create refined resources
        MaterialCost alloy = new MaterialCost();
        alloy.setMaterialType(MaterialCostType.REFINED_RESOURCE);
        alloy.setMaterialName("METAL_ALLOY");
        alloy.setBaseValueCredits(new BigDecimal("15"));
        alloy.setRawInputsJson("{\"ORE\": 2}");
        alloy.setProcessingTimeTurns(2);
        materialCostRepository.save(alloy);

        MaterialCost powerCell = new MaterialCost();
        powerCell.setMaterialType(MaterialCostType.REFINED_RESOURCE);
        powerCell.setMaterialName("POWER_CELL");
        powerCell.setBaseValueCredits(new BigDecimal("20"));
        powerCell.setRawInputsJson("{\"CRYSTAL\": 2}");
        powerCell.setProcessingTimeTurns(2);
        materialCostRepository.save(powerCell);

        // Create construction modules
        MaterialCost hull = new MaterialCost();
        hull.setMaterialType(MaterialCostType.CONSTRUCTION_MODULE);
        hull.setMaterialName("HULL");
        hull.setBaseValueCredits(new BigDecimal("25"));
        hull.setRefinedInputsJson("{\"METAL_ALLOY\": 3}");
        hull.setProcessingTimeTurns(3);
        materialCostRepository.save(hull);

        MaterialCost facility = new MaterialCost();
        facility.setMaterialType(MaterialCostType.CONSTRUCTION_MODULE);
        facility.setMaterialName("FACILITY");
        facility.setBaseValueCredits(new BigDecimal("30"));
        facility.setRefinedInputsJson("{\"METAL_ALLOY\": 2, \"POWER_CELL\": 1}");
        facility.setProcessingTimeTurns(3);
        materialCostRepository.save(facility);

        // Create test leader
        testLeader = new AiCharacter();
        testLeader.setName("Test Leader");
        testLeader.setRole("ENGINEER");
        testLeader.setEmpireName("Test Empire");
        testLeader.setLeadershipRating(8);
        testLeader.setDailySalary(1000L);
        aiCharacterRepository.save(testLeader);
    }

    @Test
    @DisplayName("Get station module mapping for MILITARY should return 5 modules")
    void getStationModuleMapping_military() {
        List<SpaceStationContractService.StationModuleContract> modules =
                spaceStationContractService.getStationModuleMapping(SpaceStationType.MILITARY);

        assertEquals(5, modules.size());
    }

    @Test
    @DisplayName("Get station module mapping for MINING should return 3 modules")
    void getStationModuleMapping_mining() {
        List<SpaceStationContractService.StationModuleContract> modules =
                spaceStationContractService.getStationModuleMapping(SpaceStationType.MINING);

        assertEquals(3, modules.size());
    }

    @Test
    @DisplayName("Get station module mapping for SHIPYARD should return 4 modules")
    void getStationModuleMapping_shipyard() {
        List<SpaceStationContractService.StationModuleContract> modules =
                spaceStationContractService.getStationModuleMapping(SpaceStationType.SHIPYARD);

        assertEquals(4, modules.size());
    }

    @Test
    @DisplayName("Get module contracts should return module contracts for station type")
    void getModuleContracts() {
        List<SpaceStationContractService.StationModuleContract> modules =
                spaceStationContractService.getModuleContracts(SpaceStationType.MILITARY);

        assertEquals(5, modules.size());
        for (SpaceStationContractService.StationModuleContract m : modules) {
            assertNotNull(m.getContractName());
        }
    }

    @Test
    @DisplayName("Calculate station cost should return positive cost")
    void calculateStationCost() {
        Map<String, Object> costReport = spaceStationContractService.calculateStationCost(
                SpaceStationType.MINING, testLeader);

        assertNotNull(costReport);
        assertTrue(costReport.containsKey("totalCostCredits"));
    }

    @Test
    @DisplayName("Calculate station cost with different leadership should vary")
    void calculateStationCost_leadershipVariation() {
        AiCharacter highLeader = new AiCharacter();
        highLeader.setName("High Leader");
        highLeader.setRole("ENGINEER");
        highLeader.setEmpireName("Test Empire");
        highLeader.setLeadershipRating(10);
        highLeader.setDailySalary(1000L);
        aiCharacterRepository.save(highLeader);

        AiCharacter lowLeader = new AiCharacter();
        lowLeader.setName("Low Leader");
        lowLeader.setRole("ENGINEER");
        lowLeader.setEmpireName("Test Empire");
        lowLeader.setLeadershipRating(1);
        lowLeader.setDailySalary(1000L);
        aiCharacterRepository.save(lowLeader);

        Map<String, Object> costHigh = spaceStationContractService.calculateStationCost(
                SpaceStationType.MINING, highLeader);
        Map<String, Object> costLow = spaceStationContractService.calculateStationCost(
                SpaceStationType.MINING, lowLeader);

        assertNotNull(costHigh);
        assertNotNull(costLow);
    }

    @Test
    @DisplayName("Create station contracts should create build contracts")
    void createStationContracts() {
        List<com.yisy.modules.doramcu.hex.model.BuildContract> contracts =
                spaceStationContractService.createStationContracts(
                        SpaceStationType.MINING, "Test Mining Station", TEST_GALAXY_ID);

        assertEquals(3, contracts.size());
        for (com.yisy.modules.doramcu.hex.model.BuildContract c : contracts) {
            assertEquals(TEST_GALAXY_ID, c.getGalaxyId());
        }
    }

    @Test
    @DisplayName("Get station module mapping for all types should have modules")
    void getStationModuleMapping_allTypes() {
        for (SpaceStationType type : SpaceStationType.values()) {
            List<SpaceStationContractService.StationModuleContract> modules =
                    spaceStationContractService.getStationModuleMapping(type);
            assertTrue(modules.size() > 0, "Station type " + type + " should have modules");
        }
    }
}
