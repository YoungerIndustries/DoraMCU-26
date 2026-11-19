package com.yisy.modules.doramcu.corporation.service;
import org.springframework.stereotype.Service;

import com.yisy.modules.doramcu.dto.GameEvent;
import com.yisy.modules.doramcu.corporation.model.CorporateContract;
import com.yisy.modules.doramcu.corporation.model.CorporateHiring;
import com.yisy.modules.doramcu.service.session.GameSessionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * Service that monitors corporation state and broadcasts events via SSE.
 * <p>
 * After each AI management cycle, this service checks for notable corporation events
 * (contract completions, financial emergencies, crew issues) and broadcasts them
 * as GameEvents to players in the game session so they receive notifications
 * while adventuring.
 * </p>
 */
@Service
public class CorporationEventService {

    private static final Logger log = LoggerFactory.getLogger(CorporationEventService.class);

    /** Threshold for near-insolvency warning (balance < this amount triggers a warning) */
    static final long NEAR_INSOLVENCY_THRESHOLD = 1000;

    private final CorporateOfficeService corporateOfficeService;
    private final CorporationEconomyService economyService;
    private final GameSessionService gameSessionService;

    public CorporationEventService(
            CorporateOfficeService corporateOfficeService,
            CorporationEconomyService economyService,
            GameSessionService gameSessionService) {
        this.corporateOfficeService = corporateOfficeService;
        this.economyService = economyService;
        this.gameSessionService = gameSessionService;
    }

    /**
     * Run all corporation event checks and broadcast any detected events.
     * <p>
     * This is the main entry point called after each AI management cycle.
     * It batches all checks into one call to avoid event spam.
     * </p>
     *
     * @param corporationId the corporation to check
     * @param sessionId     the game session to broadcast events to
     */
    public void checkAndBroadcastAll(Long corporationId, String sessionId) {
        try {
            checkContractCompletions(corporationId, sessionId);
            checkFinancialEmergency(corporationId, sessionId);
            checkCrewIssues(corporationId, sessionId);
        } catch (Exception e) {
            log.warn("Failed to check corporation events for corp {}: {}", corporationId, e.getMessage());
        }
    }

    /**
     * Detect recently completed contracts and broadcast events.
     */
    public void checkContractCompletions(Long corporationId, String sessionId) {
        List<CorporateContract> activeContracts = corporateOfficeService.getActiveContracts(corporationId);
        int completedCount = 0;
        int failedCount = 0;

        for (CorporateContract contract : activeContracts) {
            if (contract.getStatus() == CorporateContract.Status.COMPLETED) {
                completedCount++;
            } else if (contract.getStatus() == CorporateContract.Status.FAILED) {
                failedCount++;
            }
        }

        if (completedCount > 0) {
            broadcastEvent(sessionId, GameEvent.EventType.CORPORATION_CONTRACT_COMPLETED,
                    Map.of(
                            "corporationId", corporationId,
                            "message", completedCount + " contract(s) completed successfully",
                            "severity", "INFO",
                            "completedCount", completedCount,
                            "failedCount", failedCount
                    ));
            log.info("Broadcasted contract completion event for corp {}: {} completed, {} failed",
                    corporationId, completedCount, failedCount);
        } else if (failedCount > 0) {
            broadcastEvent(sessionId, GameEvent.EventType.CORPORATION_EMERGENCY,
                    Map.of(
                            "corporationId", corporationId,
                            "message", failedCount + " contract(s) failed",
                            "severity", "WARNING",
                            "completedCount", completedCount,
                            "failedCount", failedCount
                    ));
            log.warn("Broadcasted contract failure event for corp {}: {} failed", corporationId, failedCount);
        }
    }

    /**
     * Detect insolvency or near-insolvency and broadcast warnings.
     */
    public void checkFinancialEmergency(Long corporationId, String sessionId) {
        boolean insolvent = economyService.isInsolvent(corporationId);
        if (insolvent) {
            broadcastEvent(sessionId, GameEvent.EventType.CORPORATION_INSOLVENCY_WARNING,
                    Map.of(
                            "corporationId", corporationId,
                            "message", "CORPORATION INSOLVENT: Balance is negative. Complete contracts to recover.",
                            "severity", "CRITICAL",
                            "isInsolvent", true
                    ));
            log.warn("Broadcasted insolvency warning for corp {}", corporationId);
        } else {
            // Check for near-insolvency
            try {
                Map<String, Object> report = economyService.getFinancialReport(corporationId);
                Long balance = (Long) report.getOrDefault("creditBalance", 0L);
                if (balance != null && balance > 0 && balance < NEAR_INSOLVENCY_THRESHOLD) {
                    broadcastEvent(sessionId, GameEvent.EventType.CORPORATION_INSOLVENCY_WARNING,
                            Map.of(
                                    "corporationId", corporationId,
                                    "message", "LOW BALANCE WARNING: Corporation balance is " + balance + " credits.",
                                    "severity", "WARNING",
                                    "balance", balance,
                                    "isInsolvent", false
                            ));
                    log.info("Broadcasted low balance warning for corp {}: balance={}", corporationId, balance);
                }
            } catch (Exception e) {
                log.debug("Could not check financial report for corp {}: {}", corporationId, e.getMessage());
            }
        }
    }

    /**
     * Detect crew issues (underperforming hires) and broadcast events.
     */
    public void checkCrewIssues(Long corporationId, String sessionId) {
        List<CorporateHiring> activeHires = corporateOfficeService.getActiveHires(corporationId);
        int underperformingCount = 0;

        for (CorporateHiring hire : activeHires) {
            if (hire.getPerformanceRating() != null && hire.getPerformanceRating() < 3.0) {
                underperformingCount++;
            }
        }

        if (underperformingCount > 0) {
            broadcastEvent(sessionId, GameEvent.EventType.CORPORATION_CREW_ISSUE,
                    Map.of(
                            "corporationId", corporationId,
                            "message", underperformingCount + " crew member(s) underperforming. Consider retraining or replacement.",
                            "severity", "WARNING",
                            "underperformingCount", underperformingCount,
                            "totalActiveHires", activeHires.size()
                    ));
            log.info("Broadcasted crew issue event for corp {}: {} underperforming",
                    corporationId, underperformingCount);
        }
    }

    /**
     * Broadcast a corporation event to the game session via SSE.
     */
    private void broadcastEvent(String sessionId, GameEvent.EventType eventType, Map<String, Object> payload) {
        if (sessionId == null || sessionId.isBlank()) {
            log.warn("Cannot broadcast event: sessionId is null or blank");
            return;
        }
        GameEvent event = new GameEvent(eventType, sessionId, payload);
        gameSessionService.broadcastEvent(sessionId, event);
        log.debug("Broadcasted {} event to session {}", eventType, sessionId);
    }
}
