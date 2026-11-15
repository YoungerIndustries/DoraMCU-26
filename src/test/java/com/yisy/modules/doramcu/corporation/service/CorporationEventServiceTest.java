package com.yisy.modules.doramcu.corporation.service;

import com.yisy.modules.doramcu.dto.GameEvent;
import com.yisy.modules.doramcu.corporation.model.CorporateContract;
import com.yisy.modules.doramcu.corporation.model.CorporateHiring;
import com.yisy.modules.doramcu.corporation.model.CorporatePosition;
import com.yisy.modules.doramcu.service.GameSessionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CorporationEventServiceTest {

    @Mock
    private CorporateOfficeService corporateOfficeService;

    @Mock
    private CorporationEconomyService economyService;

    @Mock
    private GameSessionService gameSessionService;

    private CorporationEventService service;

    @BeforeEach
    void setUp() {
        service = new CorporationEventService(corporateOfficeService, economyService, gameSessionService);
    }

    @Test
    void checkAndBroadcastAll_callsAllChecks() {
        when(corporateOfficeService.getActiveContracts(1L)).thenReturn(List.of());
        when(economyService.isInsolvent(1L)).thenReturn(false);
        when(corporateOfficeService.getActiveHires(1L)).thenReturn(List.of());

        service.checkAndBroadcastAll(1L, "session-1");

        verify(corporateOfficeService).getActiveContracts(1L);
        verify(economyService).isInsolvent(1L);
        verify(corporateOfficeService).getActiveHires(1L);
    }

    @Test
    void checkAndBroadcastAll_handlesException() {
        when(corporateOfficeService.getActiveContracts(1L)).thenThrow(new RuntimeException("DB error"));

        assertDoesNotThrow(() -> service.checkAndBroadcastAll(1L, "session-1"));
    }

    @Test
    void checkContractCompletions_broadcastsOnSuccess() {
        CorporateContract contract = new CorporateContract("Test Contract", CorporateContract.ContractType.TRADE,
                10000, 1L);
        contract.setStatus(CorporateContract.Status.COMPLETED);

        when(corporateOfficeService.getActiveContracts(1L)).thenReturn(List.of(contract));

        service.checkContractCompletions(1L, "session-1");

        ArgumentCaptor<GameEvent> captor = ArgumentCaptor.forClass(GameEvent.class);
        verify(gameSessionService).broadcastEvent(eq("session-1"), captor.capture());
        assertEquals(GameEvent.EventType.CORPORATION_CONTRACT_COMPLETED, captor.getValue().getType());
    }

    @Test
    void checkContractCompletions_broadcastsOnFailure() {
        CorporateContract contract = new CorporateContract("Failed Contract", CorporateContract.ContractType.SECURITY,
                5000, 1L);
        contract.setStatus(CorporateContract.Status.FAILED);

        when(corporateOfficeService.getActiveContracts(1L)).thenReturn(List.of(contract));

        service.checkContractCompletions(1L, "session-1");

        ArgumentCaptor<GameEvent> captor = ArgumentCaptor.forClass(GameEvent.class);
        verify(gameSessionService).broadcastEvent(eq("session-1"), captor.capture());
        assertEquals(GameEvent.EventType.CORPORATION_EMERGENCY, captor.getValue().getType());
    }

    @Test
    void checkContractCompletions_noBroadcastWhenNoContracts() {
        when(corporateOfficeService.getActiveContracts(1L)).thenReturn(List.of());

        service.checkContractCompletions(1L, "session-1");

        verify(gameSessionService, never()).broadcastEvent(anyString(), any());
    }

    @Test
    void checkFinancialEmergency_broadcastsInsolvency() {
        when(economyService.isInsolvent(1L)).thenReturn(true);

        service.checkFinancialEmergency(1L, "session-1");

        ArgumentCaptor<GameEvent> captor = ArgumentCaptor.forClass(GameEvent.class);
        verify(gameSessionService).broadcastEvent(eq("session-1"), captor.capture());
        assertEquals(GameEvent.EventType.CORPORATION_INSOLVENCY_WARNING, captor.getValue().getType());
    }

    @Test
    void checkFinancialEmergency_broadcastsLowBalance() {
        when(economyService.isInsolvent(1L)).thenReturn(false);
        Map<String, Object> report = Map.of("creditBalance", 500L);
        when(economyService.getFinancialReport(1L)).thenReturn(report);

        service.checkFinancialEmergency(1L, "session-1");

        ArgumentCaptor<GameEvent> captor = ArgumentCaptor.forClass(GameEvent.class);
        verify(gameSessionService).broadcastEvent(eq("session-1"), captor.capture());
        assertEquals(GameEvent.EventType.CORPORATION_INSOLVENCY_WARNING, captor.getValue().getType());
    }

    @Test
    void checkFinancialEmergency_noBroadcastWhenHealthy() {
        when(economyService.isInsolvent(1L)).thenReturn(false);
        Map<String, Object> report = Map.of("creditBalance", 50000L);
        when(economyService.getFinancialReport(1L)).thenReturn(report);

        service.checkFinancialEmergency(1L, "session-1");

        verify(gameSessionService, never()).broadcastEvent(anyString(), any());
    }

    @Test
    void checkCrewIssues_broadcastsWhenUnderperforming() {
        CorporateHiring hire = new CorporateHiring(1L, 1L, CorporatePosition.CFO,
                5000L);
        hire.setStatus(CorporateHiring.Status.ACTIVE);
        hire.setPerformanceRating(2.0);

        when(corporateOfficeService.getActiveHires(1L)).thenReturn(List.of(hire));

        service.checkCrewIssues(1L, "session-1");

        ArgumentCaptor<GameEvent> captor = ArgumentCaptor.forClass(GameEvent.class);
        verify(gameSessionService).broadcastEvent(eq("session-1"), captor.capture());
        assertEquals(GameEvent.EventType.CORPORATION_CREW_ISSUE, captor.getValue().getType());
    }

    @Test
    void checkCrewIssues_noBroadcastWhenAllGood() {
        CorporateHiring hire = new CorporateHiring(1L, 1L, CorporatePosition.CFO,
                5000L);
        hire.setStatus(CorporateHiring.Status.ACTIVE);
        hire.setPerformanceRating(7.5);

        when(corporateOfficeService.getActiveHires(1L)).thenReturn(List.of(hire));

        service.checkCrewIssues(1L, "session-1");

        verify(gameSessionService, never()).broadcastEvent(anyString(), any());
    }

    @Test
    void checkCrewIssues_noBroadcastWhenNoHires() {
        when(corporateOfficeService.getActiveHires(1L)).thenReturn(List.of());

        service.checkCrewIssues(1L, "session-1");

        verify(gameSessionService, never()).broadcastEvent(anyString(), any());
    }

    @Test
    void broadcastEvent_skipsNullSessionId() {
        service.checkContractCompletions(1L, null);
        service.checkFinancialEmergency(1L, "");
        service.checkCrewIssues(1L, "   ");

        verify(gameSessionService, never()).broadcastEvent(anyString(), any());
    }

    @Test
    void nearInsolvencyThreshold_isConfigurable() {
        assertEquals(1000L, CorporationEventService.NEAR_INSOLVENCY_THRESHOLD);
    }
}
