package com.yisy.modules.doramcu.corporation.service;

import com.yisy.modules.doramcu.ai.PlayerCloneService;
import com.yisy.modules.doramcu.eve.model.AiCharacter;
import com.yisy.modules.doramcu.model.*;
import com.yisy.modules.doramcu.corporation.model.*;
import com.yisy.modules.doramcu.repository.*;
import com.yisy.modules.doramcu.corporation.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AiAssistantServiceTest {

    @Mock
    private PlayerCloneService playerCloneService;
    @Mock
    private CorporateRoleService corporateRoleService;
    @Mock
    private ContractExecutionService contractExecutionService;
    @Mock
    private CorporateOfficeService corporateOfficeService;
    @Mock
    private CorporationEconomyService economyService;
    @Mock
    private AiAssistantActionRepository actionRepository;
    @Mock
    private PlayerRepository playerRepository;
    @Mock
    private AiCharacterRepository aiCharacterRepository;
    @Mock
    private CorporateContractRepository contractRepository;
    @Mock
    private CorporateHiringRepository hiringRepository;

    @InjectMocks
    private AiAssistantService aiAssistantService;

    // ---- VP Mode Gate ----

    @Test
    void runManagementCycle_skipsWhenPlayerIsCEO() {
        when(corporateRoleService.getPlayerRole(1L)).thenReturn(Optional.of(CorporateRole.CEO));

        Map<String, Object> result = aiAssistantService.runManagementCycle(1L, 1L);

        assertTrue((Boolean) result.get("skipped"));
        assertEquals("Player is in Chief Executive Officer mode", result.get("reason"));
        verify(corporateOfficeService, never()).getActiveContracts(any());
        verify(actionRepository).save(any());
    }

    @Test
    void runManagementCycle_skipsWhenPlayerNotFound() {
        when(corporateRoleService.getPlayerRole(999L)).thenReturn(Optional.empty());

        Map<String, Object> result = aiAssistantService.runManagementCycle(999L, 1L);

        assertTrue((Boolean) result.get("skipped"));
        assertEquals("Player not found", result.get("reason"));
    }

    @Test
    void runManagementCycle_runsWhenPlayerIsVP() {
        when(corporateRoleService.getPlayerRole(1L)).thenReturn(Optional.of(CorporateRole.VP));
        when(corporateOfficeService.getActiveContracts(1L)).thenReturn(Collections.emptyList());
        when(corporateOfficeService.browseContracts(1L, null)).thenReturn(Collections.emptyList());
        when(corporateOfficeService.getActiveHires(1L)).thenReturn(Collections.emptyList());

        Map<String, Object> result = aiAssistantService.runManagementCycle(1L, 1L);

        assertFalse((Boolean) result.get("skipped"));
        assertEquals(0, result.get("contractsProcessed"));
        assertEquals(0, result.get("contractsAccepted"));
        assertEquals(0, result.get("hiresMade"));
        assertEquals(0, result.get("firesMade"));
    }

    // ---- Contract Processing ----

    @Test
    void runManagementCycle_processesActiveContracts() {
        when(corporateRoleService.getPlayerRole(1L)).thenReturn(Optional.of(CorporateRole.VP));

        CorporateContract contract = new CorporateContract();
        contract.setId(100L);
        contract.setTitle("Test Contract");
        contract.setTurnsRemaining(2);
        contract.setDurationTurns(5);
        contract.setPaymentCredits(10000);
        List<CorporateContract> activeContracts = Collections.singletonList(contract);
        when(corporateOfficeService.getActiveContracts(1L)).thenReturn(activeContracts);
        when(corporateOfficeService.browseContracts(1L, null)).thenReturn(Collections.emptyList());
        when(corporateOfficeService.getActiveHires(1L)).thenReturn(Collections.emptyList());

        Map<String, Object> processResult = new HashMap<>();
        processResult.put("finalized", false);
        processResult.put("turnsRemaining", 1);
        when(contractExecutionService.processTurn(100L)).thenReturn(processResult);

        Map<String, Object> result = aiAssistantService.runManagementCycle(1L, 1L);

        assertFalse((Boolean) result.get("skipped"));
        assertEquals(1, result.get("contractsProcessed"));
        verify(contractExecutionService).processTurn(100L);
        verify(actionRepository).save(argThat(action ->
                action.getActionType() == AiAssistantAction.ActionType.PROCESS_CONTRACT));
    }

    @Test
    void runManagementCycle_handlesFinalizedContract() {
        when(corporateRoleService.getPlayerRole(1L)).thenReturn(Optional.of(CorporateRole.VP));

        CorporateContract contract = new CorporateContract();
        contract.setId(100L);
        contract.setTitle("Finalizing Contract");
        contract.setTurnsRemaining(0);
        contract.setDurationTurns(5);
        contract.setPaymentCredits(10000);
        when(corporateOfficeService.getActiveContracts(1L)).thenReturn(Collections.singletonList(contract));
        when(corporateOfficeService.browseContracts(1L, null)).thenReturn(Collections.emptyList());
        when(corporateOfficeService.getActiveHires(1L)).thenReturn(Collections.emptyList());

        Map<String, Object> processResult = new HashMap<>();
        processResult.put("finalized", true);
        processResult.put("success", true);
        when(contractExecutionService.processTurn(100L)).thenReturn(processResult);

        Map<String, Object> result = aiAssistantService.runManagementCycle(1L, 1L);

        assertEquals(1, result.get("contractsProcessed"));
        verify(actionRepository).save(argThat(action ->
                action.getActionType() == AiAssistantAction.ActionType.PROCESS_CONTRACT &&
                action.getResult() == AiAssistantAction.Result.SUCCESS));
    }

    // ---- Contract Acceptance ----

    @Test
    void runManagementCycle_acceptsContractsBasedOnPersonality() {
        when(corporateRoleService.getPlayerRole(1L)).thenReturn(Optional.of(CorporateRole.VP));
        when(corporateOfficeService.getActiveContracts(1L)).thenReturn(Collections.emptyList());

        // Provide one hire so the AI will consider accepting contracts
        CorporateHiring hire = new CorporateHiring();
        hire.setId(1L);
        hire.setPosition(CorporatePosition.CFO);
        hire.setPerformanceRating(5.0);
        when(corporateOfficeService.getActiveHires(1L)).thenReturn(Collections.singletonList(hire));

        CorporateContract available = new CorporateContract();
        available.setId(200L);
        available.setTitle("Trade Deal");
        available.setType(CorporateContract.ContractType.TRADE);
        available.setDifficulty(3);
        available.setPaymentCredits(5000);
        when(corporateOfficeService.browseContracts(1L, null)).thenReturn(Collections.singletonList(available));

        CorporateContract accepted = new CorporateContract();
        accepted.setId(200L);
        accepted.setTitle("Trade Deal");
        accepted.setStatus(CorporateContract.Status.ACTIVE);
        when(corporateOfficeService.acceptContract(1L, 200L)).thenReturn(accepted);

        Map<String, Object> result = aiAssistantService.runManagementCycle(1L, 1L);

        assertFalse((Boolean) result.get("skipped"));
        // With default personality (riskTolerance=0.5), difficulty 3/10=0.3 <= 0.5, so should accept
        assertEquals(1, result.get("contractsAccepted"));
    }

    @Test
    void runManagementCycle_respectsMaxAcceptances() {
        when(corporateRoleService.getPlayerRole(1L)).thenReturn(Optional.of(CorporateRole.VP));
        when(corporateOfficeService.getActiveContracts(1L)).thenReturn(Collections.emptyList());

        // Provide one hire so the AI will consider accepting contracts
        CorporateHiring hire = new CorporateHiring();
        hire.setId(1L);
        hire.setPosition(CorporatePosition.CFO);
        hire.setPerformanceRating(5.0);
        when(corporateOfficeService.getActiveHires(1L)).thenReturn(Collections.singletonList(hire));

        // Create 15 available contracts
        List<CorporateContract> available = new ArrayList<>();
        for (int i = 0; i < 15; i++) {
            CorporateContract c = new CorporateContract();
            c.setId((long) (300 + i));
            c.setTitle("Contract " + i);
            c.setType(CorporateContract.ContractType.TRADE);
            c.setDifficulty(1);
            c.setPaymentCredits(10000);
            available.add(c);
        }
        when(corporateOfficeService.browseContracts(1L, null)).thenReturn(available);

        CorporateContract accepted = new CorporateContract();
        accepted.setId(300L);
        accepted.setStatus(CorporateContract.Status.ACTIVE);
        when(corporateOfficeService.acceptContract(anyLong(), anyLong())).thenReturn(accepted);

        Map<String, Object> result = aiAssistantService.runManagementCycle(1L, 1L);

        // Should not exceed MAX_CONTRACT_ACCEPTANCES (10)
        int acceptedCount = (int) result.get("contractsAccepted");
        assertTrue(acceptedCount <= AiAssistantService.MAX_CONTRACT_ACCEPTANCES);
    }

    // ---- Crew Hiring ----

    @Test
    void runManagementCycle_noHireWhenEnoughCrew() {
        when(corporateRoleService.getPlayerRole(1L)).thenReturn(Optional.of(CorporateRole.VP));
        when(corporateOfficeService.getActiveContracts(1L)).thenReturn(Collections.emptyList());
        when(corporateOfficeService.browseContracts(1L, null)).thenReturn(Collections.emptyList());

        CorporateHiring hire = new CorporateHiring();
        hire.setId(1L);
        hire.setPosition(CorporatePosition.CFO);
        hire.setPerformanceRating(5.0);
        when(corporateOfficeService.getActiveHires(1L)).thenReturn(Collections.singletonList(hire));

        Map<String, Object> result = aiAssistantService.runManagementCycle(1L, 1L);

        assertEquals(0, result.get("hiresMade"));
    }

    // ---- Crew Firing ----

    @Test
    void runManagementCycle_noFireWhenPerformanceGood() {
        when(corporateRoleService.getPlayerRole(1L)).thenReturn(Optional.of(CorporateRole.VP));
        when(corporateOfficeService.getActiveContracts(1L)).thenReturn(Collections.emptyList());
        when(corporateOfficeService.browseContracts(1L, null)).thenReturn(Collections.emptyList());

        CorporateHiring hire = new CorporateHiring();
        hire.setId(1L);
        hire.setPosition(CorporatePosition.CFO);
        hire.setPerformanceRating(7.0);
        when(corporateOfficeService.getActiveHires(1L)).thenReturn(Collections.singletonList(hire));

        Map<String, Object> result = aiAssistantService.runManagementCycle(1L, 1L);

        assertEquals(0, result.get("firesMade"));
        verify(corporateOfficeService, never()).fireAiCharacter(anyLong());
    }

    // ---- isAssistantActive ----

    @Test
    void isAssistantActive_returnsTrueForVP() {
        when(corporateRoleService.getPlayerRole(1L)).thenReturn(Optional.of(CorporateRole.VP));

        assertTrue(aiAssistantService.isAssistantActive(1L));
    }

    @Test
    void isAssistantActive_returnsFalseForCEO() {
        when(corporateRoleService.getPlayerRole(1L)).thenReturn(Optional.of(CorporateRole.CEO));

        assertFalse(aiAssistantService.isAssistantActive(1L));
    }

    @Test
    void isAssistantActive_returnsFalseForMissingPlayer() {
        when(corporateRoleService.getPlayerRole(999L)).thenReturn(Optional.empty());

        assertFalse(aiAssistantService.isAssistantActive(999L));
    }

    // ---- getRecentActions ----

    @Test
    void getRecentActions_returnsActionsLimitedByLimit() {
        List<AiAssistantAction> actions = new ArrayList<>();
        for (int i = 0; i < 30; i++) {
            AiAssistantAction action = new AiAssistantAction(
                    1L, AiAssistantAction.ActionType.PROCESS_CONTRACT, (long) i,
                    AiAssistantAction.Result.SUCCESS, "Action " + i, null);
            actions.add(action);
        }
        when(actionRepository.findByPlayerIdOrderByTimestampDesc(1L)).thenReturn(actions);

        List<AiAssistantAction> result = aiAssistantService.getRecentActions(1L, 10);

        assertEquals(10, result.size());
    }

    @Test
    void getRecentActions_returnsAllWhenNoLimit() {
        List<AiAssistantAction> actions = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            AiAssistantAction action = new AiAssistantAction(
                    1L, AiAssistantAction.ActionType.PROCESS_CONTRACT, (long) i,
                    AiAssistantAction.Result.SUCCESS, "Action " + i, null);
            actions.add(action);
        }
        when(actionRepository.findByPlayerIdOrderByTimestampDesc(1L)).thenReturn(actions);

        List<AiAssistantAction> result = aiAssistantService.getRecentActions(1L, 0);

        assertEquals(5, result.size());
    }

    // ---- Constants ----

    @Test
    void maxContractAcceptances_isTen() {
        assertEquals(10, AiAssistantService.MAX_CONTRACT_ACCEPTANCES);
    }

    @Test
    void maxHiresPerTurn_isTwo() {
        assertEquals(2, AiAssistantService.MAX_HIRES_PER_TURN);
    }

    @Test
    void defaultFireThreshold_isThree() {
        assertEquals(3.0, AiAssistantService.DEFAULT_FIRE_THRESHOLD);
    }
}
