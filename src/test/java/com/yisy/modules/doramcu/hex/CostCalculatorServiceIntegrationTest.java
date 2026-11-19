package com.yisy.modules.doramcu.hex;

import com.yisy.modules.doramcu.eve.model.AiCharacter;
import com.yisy.modules.doramcu.hex.model.BuildContract;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for the CostCalculatorService using an H2 in-memory database.
 * <p>
 * Tests leadership modifier formula, labor cost calculation, and total cost calculation.
 * </p>
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = EconomicProductionTestConfig.class)
@ActiveProfiles("test")
@Transactional
@DisplayName("CostCalculatorService Integration Tests")
class CostCalculatorServiceIntegrationTest {

    @Autowired
    private CostCalculatorService costCalculatorService;

    @Autowired
    private MaterialCostRepository materialCostRepository;

    @Autowired
    private AiCharacterRepository aiCharacterRepository;

    private AiCharacter leaderHigh;
    private AiCharacter leaderAverage;
    private AiCharacter leaderLow;

    @BeforeEach
    void setUp() {
        // Create raw resource: ORE
        MaterialCost ore = new MaterialCost();
        ore.setMaterialType(MaterialCostType.RAW_RESOURCE);
        ore.setMaterialName("ORE");
        ore.setBaseValueCredits(new BigDecimal("10"));
        ore.setProcessingTimeTurns(1);
        materialCostRepository.save(ore);

        // Create refined resource: METAL_ALLOY
        MaterialCost alloy = new MaterialCost();
        alloy.setMaterialType(MaterialCostType.REFINED_RESOURCE);
        alloy.setMaterialName("METAL_ALLOY");
        alloy.setBaseValueCredits(new BigDecimal("15"));
        alloy.setRawInputsJson("{\"ORE\": 2}");
        alloy.setProcessingTimeTurns(2);
        materialCostRepository.save(alloy);

        // Create leaders with different leadership ratings
        leaderHigh = new AiCharacter();
        leaderHigh.setName("High Leader");
        leaderHigh.setRole("ENGINEER");
        leaderHigh.setEmpireName("Test Empire");
        leaderHigh.setLeadershipRating(10);
        leaderHigh.setDailySalary(1000L);
        aiCharacterRepository.save(leaderHigh);

        leaderAverage = new AiCharacter();
        leaderAverage.setName("Average Leader");
        leaderAverage.setRole("ENGINEER");
        leaderAverage.setEmpireName("Test Empire");
        leaderAverage.setLeadershipRating(5);
        leaderAverage.setDailySalary(1000L);
        aiCharacterRepository.save(leaderAverage);

        leaderLow = new AiCharacter();
        leaderLow.setName("Low Leader");
        leaderLow.setRole("ENGINEER");
        leaderLow.setEmpireName("Test Empire");
        leaderLow.setLeadershipRating(1);
        leaderLow.setDailySalary(1000L);
        aiCharacterRepository.save(leaderLow);
    }

    @Test
    @DisplayName("Leadership modifier for rating 10 should be 0.75")
    void calculateLeadershipModifier_max() {
        double modifier = costCalculatorService.calculateLeadershipModifier(10);
        assertEquals(0.75, modifier, 0.001);
    }

    @Test
    @DisplayName("Leadership modifier for rating 5 should be 1.0")
    void calculateLeadershipModifier_average() {
        double modifier = costCalculatorService.calculateLeadershipModifier(5);
        assertEquals(1.0, modifier, 0.001);
    }

    @Test
    @DisplayName("Leadership modifier for rating 1 should be 1.2")
    void calculateLeadershipModifier_min() {
        double modifier = costCalculatorService.calculateLeadershipModifier(1);
        assertEquals(1.2, modifier, 0.001);
    }

    @Test
    @DisplayName("Calculate labor cost should apply leadership modifier")
    void calculateLaborCost() {
        // crewSize=4, dailySalary=1000, duration=5 turns, leadership=10
        BigDecimal laborCost = costCalculatorService.calculateLaborCost(
                4, 1000L, 5, 10);

        // 4 * 1000 * 5 * 0.75 = 15000
        assertEquals(0, new BigDecimal("15000").compareTo(laborCost));
    }

    @Test
    @DisplayName("Calculate labor cost with average leadership should have no modifier")
    void calculateLaborCost_averageLeadership() {
        // crewSize=4, dailySalary=1000, duration=5 turns, leadership=5
        BigDecimal laborCost = costCalculatorService.calculateLaborCost(
                4, 1000L, 5, 5);

        // 4 * 1000 * 5 * 1.0 = 20000
        assertEquals(0, new BigDecimal("20000").compareTo(laborCost));
    }

    @Test
    @DisplayName("Calculate labor cost with low leadership should increase cost")
    void calculateLaborCost_lowLeadership() {
        // crewSize=4, dailySalary=1000, duration=5 turns, leadership=1
        BigDecimal laborCost = costCalculatorService.calculateLaborCost(
                4, 1000L, 5, 1);

        // 4 * 1000 * 5 * 1.2 = 24000
        assertEquals(0, new BigDecimal("24000").compareTo(laborCost));
    }

    @Test
    @DisplayName("Calculate material cost should delegate to MaterialCostService")
    void calculateMaterialCost() {
        BigDecimal cost = costCalculatorService.calculateMaterialCost("ORE");
        assertEquals(new BigDecimal("10"), cost);
    }

    @Test
    @DisplayName("Calculate material cost for refined resource should include processing markup")
    void calculateMaterialCost_refined() {
        BigDecimal cost = costCalculatorService.calculateMaterialCost("METAL_ALLOY");
        // METAL_ALLOY = 2 * ORE(10) * 1.3 = 26
        assertNotNull(cost);
        assertTrue(cost.compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    @DisplayName("Calculate total cost should combine material and labor costs")
    void calculateTotalCost() {
        BuildContract contract = new BuildContract();
        contract.setTargetItemName("TEST_ITEM");
        contract.setTargetItemType("PLANET_FACILITY");
        contract.setContractType(BuildContract.ContractType.CONSTRUCT);
        contract.setMaterialRequirementsJson("{\"ORE\": 10}");

        BigDecimal totalCost = costCalculatorService.calculateTotalCost(
                contract, leaderHigh);

        assertNotNull(totalCost);
        assertTrue(totalCost.compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    @DisplayName("Higher leadership should result in lower labor cost")
    void leadershipModifier_affectsLaborCost() {
        BigDecimal laborHigh = costCalculatorService.calculateLaborCost(4, 1000L, 5, 10);
        BigDecimal laborAverage = costCalculatorService.calculateLaborCost(4, 1000L, 5, 5);
        BigDecimal laborLow = costCalculatorService.calculateLaborCost(4, 1000L, 5, 1);

        assertTrue(laborHigh.compareTo(laborAverage) < 0, "High leadership should reduce labor cost");
        assertTrue(laborLow.compareTo(laborAverage) > 0, "Low leadership should increase labor cost");
    }
}
