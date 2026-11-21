package com.yisy.modules.doramcu.corporation.service;

import com.yisy.modules.doramcu.eve.model.AiCharacter;
import com.yisy.modules.doramcu.corporation.model.CorporateContract;
import com.yisy.modules.doramcu.corporation.model.CorporateContractAssignment;
import com.yisy.modules.doramcu.corporation.model.CorporateHiring;
import com.yisy.modules.doramcu.corporation.model.CorporatePosition;
import com.yisy.modules.doramcu.repository.ai.AiCharacterRepository;
import com.yisy.modules.doramcu.corporation.repository.CorporateContractAssignmentRepository;
import com.yisy.modules.doramcu.corporation.repository.CorporateContractRepository;
import com.yisy.modules.doramcu.corporation.repository.CorporateHiringRepository;
import com.yisy.modules.doramcu.service.MultiUniverseContractService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ContractExecutionService Tests")
class ContractExecutionServiceTest {

    @Mock
    private CorporateContractRepository contractRepository;

    @Mock
    private CorporateHiringRepository hiringRepository;

    @Mock
    private CorporateContractAssignmentRepository assignmentRepository;

    @Mock
    private AiCharacterRepository aiCharacterRepository;

    @Mock
    private CorporationEconomyService economyService;

    @Mock
    private CareerProgressionService careerProgressionService;

    @Mock
    private TravellerCorporationService travellerCorporationService;

    @Mock
    private MultiUniverseContractService multiUniverseContractService;

    @InjectMocks
    private ContractExecutionService contractExecutionService;

    private CorporateContract testContract;
    private CorporateHiring testHiring;
    private AiCharacter testAiCharacter;

    @BeforeEach
    void setUp() {
        testContract = new CorporateContract();
        testContract.setId(1L);
        testContract.setTitle("Test Contract");
        testContract.setType(CorporateContract.ContractType.SECURITY);
        testContract.setPaymentCredits(5000);
        testContract.setXpReward(100);
        testContract.setDifficulty(5);
        testContract.setDurationTurns(3);
        testContract.setStatus(CorporateContract.Status.ACTIVE);
        testContract.setCorporationId(1L);
        testContract.setTurnsRemaining(3);
        testContract.setProgressPercent(0.0);
        testContract.setAssignedCrewIds(new ArrayList<>());

        testAiCharacter = new AiCharacter();
        testAiCharacter.setId(100L);
        testAiCharacter.setName("Test AI");
        testAiCharacter.setLeadershipRating(7);
        testAiCharacter.setPrimarySkill("Security");

        testHiring = new CorporateHiring(1L, 100L, CorporatePosition.SECURITY_CHIEF, 500L);
        testHiring.setId(10L);
        testHiring.setPerformanceRating(7.0);
    }

    // ---- Position Mapping Tests ----

    @Test
    @DisplayName("position mapping should include all positions")
    void positionMapping_allPositions() {
        Map<CorporatePosition, List<CorporateContract.ContractType>> mapping = contractExecutionService.getPositionToContractMapping();

        assertEquals(9, mapping.size());
        for (CorporatePosition pos : CorporatePosition.values()) {
            assertTrue(mapping.containsKey(pos), "Missing mapping for " + pos);
        }
    }

    @Test
    @DisplayName("SECURITY_CHIEF should map to SECURITY contracts")
    void positionMapping_securityChief() {
        Map<CorporatePosition, List<CorporateContract.ContractType>> mapping = contractExecutionService.getPositionToContractMapping();

        List<CorporateContract.ContractType> types = mapping.get(CorporatePosition.SECURITY_CHIEF);
        assertTrue(types.contains(CorporateContract.ContractType.SECURITY));
    }

    @Test
    @DisplayName("CFO should map to TRADE contracts")
    void positionMapping_cfo() {
        Map<CorporatePosition, List<CorporateContract.ContractType>> mapping = contractExecutionService.getPositionToContractMapping();

        List<CorporateContract.ContractType> types = mapping.get(CorporatePosition.CFO);
        assertTrue(types.contains(CorporateContract.ContractType.TRADE));
    }

    @Test
    @DisplayName("ENGINEER should map to LOGISTICS contracts")
    void positionMapping_engineer() {
        Map<CorporatePosition, List<CorporateContract.ContractType>> mapping = contractExecutionService.getPositionToContractMapping();

        List<CorporateContract.ContractType> types = mapping.get(CorporatePosition.ENGINEER);
        assertTrue(types.contains(CorporateContract.ContractType.LOGISTICS));
    }

    // ---- Auto-Assignment Tests ----

    @Test
    @DisplayName("autoAssignCrew should throw when contract not found")
    void autoAssignCrew_notFound() {
        when(contractRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
                contractExecutionService.autoAssignCrew(999L));
    }

    @Test
    @DisplayName("autoAssignCrew should throw when contract is not active")
    void autoAssignCrew_notActive() {
        testContract.setStatus(CorporateContract.Status.AVAILABLE);
        when(contractRepository.findById(1L)).thenReturn(Optional.of(testContract));

        assertThrows(IllegalArgumentException.class, () ->
                contractExecutionService.autoAssignCrew(1L));
    }

    @Test
    @DisplayName("autoAssignCrew should return empty list when no hires available")
    void autoAssignCrew_noHires() {
        when(contractRepository.findById(1L)).thenReturn(Optional.of(testContract));
        when(hiringRepository.findByCorporationIdAndStatus(1L, CorporateHiring.Status.ACTIVE))
                .thenReturn(Collections.emptyList());
        when(contractRepository.save(any(CorporateContract.class))).thenAnswer(inv -> inv.getArgument(0));

        List<CorporateContractAssignment> result = contractExecutionService.autoAssignCrew(1L);

        assertTrue(result.isEmpty());
        assertEquals(3, testContract.getTurnsRemaining());
    }

    @Test
    @DisplayName("autoAssignCrew should assign matching crew based on position")
    void autoAssignCrew_matchingPosition() {
        when(contractRepository.findById(1L)).thenReturn(Optional.of(testContract));
        when(hiringRepository.findByCorporationIdAndStatus(1L, CorporateHiring.Status.ACTIVE))
                .thenReturn(List.of(testHiring));
        when(assignmentRepository.save(any(CorporateContractAssignment.class))).thenAnswer(inv -> {
            CorporateContractAssignment a = inv.getArgument(0);
            a.setId(1L);
            return a;
        });
        when(contractRepository.save(any(CorporateContract.class))).thenAnswer(inv -> inv.getArgument(0));

        List<CorporateContractAssignment> result = contractExecutionService.autoAssignCrew(1L);

        assertEquals(1, result.size());
        assertEquals(10L, result.get(0).getHiringId());
        assertEquals(100L, result.get(0).getAiCharacterId());
        assertEquals(1, testContract.getAssignedCrewIds().size());
    }

    @Test
    @DisplayName("autoAssignCrew should assign non-matching hires when no exact match")
    void autoAssignCrew_fallbackToAllHires() {
        // CFO doesn't match SECURITY, but should still be assigned as fallback
        CorporateHiring cfoHiring = new CorporateHiring(1L, 200L, CorporatePosition.CFO, 600L);
        cfoHiring.setId(20L);
        cfoHiring.setPerformanceRating(6.0);

        when(contractRepository.findById(1L)).thenReturn(Optional.of(testContract));
        when(hiringRepository.findByCorporationIdAndStatus(1L, CorporateHiring.Status.ACTIVE))
                .thenReturn(List.of(cfoHiring));
        when(assignmentRepository.save(any(CorporateContractAssignment.class))).thenAnswer(inv -> {
            CorporateContractAssignment a = inv.getArgument(0);
            a.setId(2L);
            return a;
        });
        when(contractRepository.save(any(CorporateContract.class))).thenAnswer(inv -> inv.getArgument(0));

        List<CorporateContractAssignment> result = contractExecutionService.autoAssignCrew(1L);

        assertEquals(1, result.size());
        assertEquals(20L, result.get(0).getHiringId());
    }

    // ---- Process Turn Tests ----

    @Test
    @DisplayName("processTurn should throw when contract not found")
    void processTurn_notFound() {
        when(contractRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
                contractExecutionService.processTurn(999L));
    }

    @Test
    @DisplayName("processTurn should throw when contract is not active")
    void processTurn_notActive() {
        testContract.setStatus(CorporateContract.Status.COMPLETED);
        when(contractRepository.findById(1L)).thenReturn(Optional.of(testContract));

        assertThrows(IllegalArgumentException.class, () ->
                contractExecutionService.processTurn(1L));
    }

    @Test
    @DisplayName("processTurn should decrement turns and return progress")
    void processTurn_decrementsTurns() {
        when(contractRepository.findById(1L)).thenReturn(Optional.of(testContract));
        when(contractRepository.save(any(CorporateContract.class))).thenAnswer(inv -> inv.getArgument(0));

        Map<String, Object> result = contractExecutionService.processTurn(1L);

        assertEquals(2, testContract.getTurnsRemaining());
        assertEquals(1L, result.get("contractId"));
        assertEquals("ACTIVE", result.get("status"));
        assertFalse((Boolean) result.get("finalized"));
    }

    // ---- Calculate Success Tests ----

    @Test
    @DisplayName("calculateSuccess should return false for no crew and high difficulty")
    void calculateSuccess_noCrewHighDifficulty() {
        testContract.setAssignedCrewIds(Collections.emptyList());
        testContract.setDifficulty(8);

        boolean success = contractExecutionService.calculateSuccess(testContract);

        assertFalse(success);
    }

    @Test
    @DisplayName("calculateSuccess should return true for no crew and very low difficulty")
    void calculateSuccess_noCrewLowDifficulty() {
        testContract.setAssignedCrewIds(Collections.emptyList());
        testContract.setDifficulty(2);

        boolean success = contractExecutionService.calculateSuccess(testContract);

        assertTrue(success);
    }

    @Test
    @DisplayName("calculateSuccess should compare crew performance vs difficulty")
    void calculateSuccess_withCrew() {
        testContract.setAssignedCrewIds(List.of(100L));
        testContract.setDifficulty(5);
        when(aiCharacterRepository.findById(100L)).thenReturn(Optional.of(testAiCharacter));
        when(multiUniverseContractService.getPerformanceModifier(any(), any())).thenReturn(1.0);

        // Leadership rating 7 should succeed against difficulty 5 with variance
        boolean success = contractExecutionService.calculateSuccess(testContract);

        // With rating 7 and variance 0.8-1.2, score is 5.6-8.4, so should succeed against difficulty 5
        assertTrue(success);
    }

    // ---- Distribute Rewards Tests ----

    @Test
    @DisplayName("distributeRewards should award XP to crew members")
    void distributeRewards_awardsXp() {
        testContract.setId(1L);
        testContract.setXpReward(100);
        testContract.setAssignedCrewIds(List.of(100L));
        testAiCharacter.setTotalExperience(0);

        when(aiCharacterRepository.findById(100L)).thenReturn(Optional.of(testAiCharacter));
        when(aiCharacterRepository.save(any(AiCharacter.class))).thenAnswer(inv -> inv.getArgument(0));
        when(assignmentRepository.findByContractId(1L)).thenReturn(Collections.emptyList());

        List<Map<String, Object>> result = contractExecutionService.distributeRewards(testContract);

        assertEquals(1, result.size());
        assertEquals(100L, result.get(0).get("aiCharacterId"));
        assertEquals(100, result.get(0).get("xpEarned"));
        assertEquals(100, testAiCharacter.getTotalExperience());
    }

    @Test
    @DisplayName("distributeRewards should split XP among multiple crew")
    void distributeRewards_splitXp() {
        testContract.setId(1L);
        testContract.setXpReward(100);
        testContract.setAssignedCrewIds(List.of(100L, 200L));

        AiCharacter ai2 = new AiCharacter();
        ai2.setId(200L);
        ai2.setName("AI Two");
        ai2.setLeadershipRating(6);

        when(aiCharacterRepository.findById(100L)).thenReturn(Optional.of(testAiCharacter));
        when(aiCharacterRepository.findById(200L)).thenReturn(Optional.of(ai2));
        when(aiCharacterRepository.save(any(AiCharacter.class))).thenAnswer(inv -> inv.getArgument(0));
        when(assignmentRepository.findByContractId(1L)).thenReturn(Collections.emptyList());

        List<Map<String, Object>> result = contractExecutionService.distributeRewards(testContract);

        assertEquals(2, result.size());
        assertEquals(50, result.get(0).get("xpEarned"));
        assertEquals(50, result.get(1).get("xpEarned"));
    }

    // ---- Estimate Success Chance Tests ----

    @Test
    @DisplayName("estimateSuccessChance should return low chance for no crew")
    void estimateSuccessChance_noCrew() {
        testContract.setAssignedCrewIds(Collections.emptyList());
        testContract.setDifficulty(5);

        double chance = contractExecutionService.estimateSuccessChance(testContract);

        assertEquals(10.0, chance);
    }

    @Test
    @DisplayName("estimateSuccessChance should calculate based on crew performance")
    void estimateSuccessChance_withCrew() {
        testContract.setAssignedCrewIds(List.of(100L));
        testContract.setDifficulty(5);
        when(aiCharacterRepository.findById(100L)).thenReturn(Optional.of(testAiCharacter));

        double chance = contractExecutionService.estimateSuccessChance(testContract);

        // Leadership 7 / difficulty 5 * 100 = 140%, capped at 100
        assertEquals(100.0, chance);
    }

    // ---- Finalize Contract Tests ----

    @Test
    @DisplayName("finalizeContract should throw when contract not found")
    void finalizeContract_notFound() {
        when(contractRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
                contractExecutionService.finalizeContract(999L));
    }

    @Test
    @DisplayName("finalizeContract should throw when contract is not active")
    void finalizeContract_notActive() {
        testContract.setStatus(CorporateContract.Status.COMPLETED);
        when(contractRepository.findById(1L)).thenReturn(Optional.of(testContract));

        assertThrows(IllegalArgumentException.class, () ->
                contractExecutionService.finalizeContract(1L));
    }

    @Test
    @DisplayName("finalizeContract should mark contract as completed on success")
    void finalizeContract_success() {
        testContract.setAssignedCrewIds(List.of(100L));
        when(contractRepository.findById(1L)).thenReturn(Optional.of(testContract));
        when(contractRepository.save(any(CorporateContract.class))).thenAnswer(inv -> inv.getArgument(0));
        when(aiCharacterRepository.findById(100L)).thenReturn(Optional.of(testAiCharacter));
        when(aiCharacterRepository.save(any(AiCharacter.class))).thenAnswer(inv -> inv.getArgument(0));
        when(assignmentRepository.findByContractId(1L)).thenReturn(Collections.emptyList());
        when(multiUniverseContractService.getPerformanceModifier(any(), any())).thenReturn(1.0);

        Map<String, Object> result = contractExecutionService.finalizeContract(1L);

        assertEquals("COMPLETED", result.get("status"));
        assertTrue((Boolean) result.get("success"));
        assertNotNull(result.get("crewXpAwarded"));
    }

    @Test
    @DisplayName("finalizeContract should mark contract as failed on failure")
    void finalizeContract_failure() {
        testContract.setAssignedCrewIds(List.of(100L));
        testContract.setDifficulty(10);
        when(contractRepository.findById(1L)).thenReturn(Optional.of(testContract));
        when(contractRepository.save(any(CorporateContract.class))).thenAnswer(inv -> inv.getArgument(0));
        when(aiCharacterRepository.findById(100L)).thenReturn(Optional.of(testAiCharacter));

        Map<String, Object> result = contractExecutionService.finalizeContract(1L);

        assertEquals("FAILED", result.get("status"));
        assertFalse((Boolean) result.get("success"));
        assertEquals(0L, result.get("paymentCredits"));
    }
}
