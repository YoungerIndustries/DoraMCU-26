package com.yisy.modules.doramcu.admin.service;

import com.yisy.modules.doramcu.admin.model.AiAdminIncident;
import com.yisy.modules.doramcu.admin.model.AiAdminOrder;
import com.yisy.modules.doramcu.admin.repository.AiAdminIncidentRepository;
import com.yisy.modules.doramcu.eve.model.AiCharacter;
import com.yisy.modules.doramcu.repository.ai.AiCharacterRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * AiAdminIncidentAutomationService — Autonomous SRE-style incident handling.
 *
 * This service runs as a scheduled task to autonomously process open incidents:
 * 1. Pick up OPEN incidents
 * 2. Assign to available AI admin
 * 3. Use LLM to investigate and recommend action
 * 4. Execute the recommended action (INVESTIGATE, RESOLVE, ESCALATE, MONITOR, DEPLOY, REPAIR)
 * 5. Record audit trail via collaboration notes
 *
 * Features:
 * - Circuit breaker: Pauses after repeated LLM failures per incident
 * - Rate limiting: Each incident is checked at configurable intervals
 * - Configuration: Enable/disable, check interval, max failures, cooldown period
 */
@Service
public class AiAdminIncidentAutomationService {

    private static final Logger log = LoggerFactory.getLogger(AiAdminIncidentAutomationService.class);

    private final AiAdminIncidentRepository incidentRepository;
    private final AiCharacterRepository characterRepository;
    private final AiAdminLlmService llmService;
    private final AiAdminIncidentService incidentService;
    private final AiAdminCollaborationService collaborationService;
    private final AiAdminService aiAdminService;

    // Circuit breaker: tracks LLM failures per incident
    private final Map<Long, Integer> llmFailureCounts = new ConcurrentHashMap<>();

    // Rate limiting: tracks last check time per incident
    private final Map<Long, LocalDateTime> lastCheckTimes = new ConcurrentHashMap<>();

    @Value("${dora.admin.incident.automation.enabled:true}")
    private boolean automationEnabled;

    @Value("${dora.admin.incident.automation.check-interval-ms:300000}")
    private long checkIntervalMs;

    @Value("${dora.admin.incident.automation.max-llm-failures:3}")
    private int maxLlmFailures;

    @Value("${dora.admin.incident.automation.circuit-breaker-cooldown-ms:3600000}")
    private long circuitBreakerCooldownMs;

    public AiAdminIncidentAutomationService(AiAdminIncidentRepository incidentRepository,
                                            AiCharacterRepository characterRepository,
                                            AiAdminLlmService llmService,
                                            AiAdminIncidentService incidentService,
                                            AiAdminCollaborationService collaborationService,
                                            AiAdminService aiAdminService) {
        this.incidentRepository = incidentRepository;
        this.characterRepository = characterRepository;
        this.llmService = llmService;
        this.incidentService = incidentService;
        this.collaborationService = collaborationService;
        this.aiAdminService = aiAdminService;
    }

    /**
     * Scheduled task that processes all OPEN incidents.
     * Runs at configurable intervals (default: every 5 minutes).
     */
    @Scheduled(fixedDelayString = "${dora.admin.incident.automation.check-interval-ms:300000}", initialDelayString = "${dora.admin.incident.automation.initial-delay-ms:60000}")
    public void processOpenIncidents() {
        if (!automationEnabled) {
            return;
        }

        log.info("Starting automated incident processing cycle");

        List<AiAdminIncident> openIncidents = incidentRepository.findOpenIncidents(AiAdminIncident.Status.OPEN);
        log.info("Found {} open incidents to process", openIncidents.size());

        for (AiAdminIncident incident : openIncidents) {
            try {
                processIncident(incident);
            } catch (Exception e) {
                log.error("Error processing incident {}: {}", incident.getId(), e.getMessage(), e);
            }
        }

        log.info("Completed automated incident processing cycle");
    }

    /**
     * Process a single incident through the automation loop.
     *
     * @param incident the incident to process
     */
    public void processIncident(AiAdminIncident incident) {
        Long incidentId = incident.getId();

        // Check circuit breaker
        if (isCircuitBreakerOpen(incidentId)) {
            log.debug("Circuit breaker open for incident {}, skipping", incidentId);
            return;
        }

        // Check rate limiting
        if (isRateLimited(incidentId)) {
            log.debug("Rate limited for incident {}, skipping", incidentId);
            return;
        }

        // Update last check time
        lastCheckTimes.put(incidentId, LocalDateTime.now());

        // Find available AI admin for this incident
        AiCharacter admin = findAvailableAdmin(incident);
        if (admin == null) {
            log.warn("No available AI admin for incident {} in galaxy {}", incidentId, incident.getGalaxyName());
            return;
        }

        // Assign admin to incident if not already assigned
        if (incident.getAssignedAiAdminId() == null) {
            try {
                incidentService.assignIncident(incidentId, admin.getId());
                log.info("Assigned admin '{}' to incident {}", admin.getName(), incidentId);
            } catch (Exception e) {
                log.error("Failed to assign admin to incident {}: {}", incidentId, e.getMessage());
                return;
            }
        }

        // LLM analysis
        LlmAnalysisResult llmResult;
        try {
            llmResult = llmService.analyzeIncident(admin, incident);
            llmFailureCounts.remove(incidentId); // Reset failure count on success
        } catch (Exception e) {
            log.error("LLM analysis failed for incident {}: {}", incidentId, e.getMessage());
            recordLlmFailure(incidentId);
            return;
        }

        if (!llmResult.isSuccess()) {
            log.warn("LLM analysis unsuccessful for incident {}: {}", incidentId, llmResult.getError());
            recordLlmFailure(incidentId);
            return;
        }

        // Execute the LLM's recommended action
        try {
            executeLlmAction(incident, admin, llmResult);
        } catch (Exception e) {
            log.error("Failed to execute LLM action for incident {}: {}", incidentId, e.getMessage(), e);
        }
    }

    /**
     * Execute the LLM's recommended action.
     *
     * @param incident the incident
     * @param admin the AI admin executing the action
     * @param llmResult the LLM analysis result
     */
    public void executeLlmAction(AiAdminIncident incident, AiCharacter admin, LlmAnalysisResult llmResult) {
        String action = llmResult.getAction();
        Long incidentId = incident.getId();

        log.info("Executing LLM action '{}' for incident {}: {}", action, incidentId, llmResult.getReasoning());

        switch (action) {
            case "INVESTIGATE":
                handleInvestigate(incident, admin, llmResult);
                break;
            case "RESOLVE":
                handleResolve(incident, admin, llmResult);
                break;
            case "ESCALATE":
                handleEscalate(incident, llmResult);
                break;
            case "MONITOR":
                handleMonitor(incident, admin, llmResult);
                break;
            case "DEPLOY":
            case "REPAIR":
                handleDeployOrRepair(incident, admin, llmResult);
                break;
            default:
                log.warn("Unknown LLM action '{}' for incident {}", action, incidentId);
                addAuditNote(incident, admin, "Unknown LLM action: " + action);
        }
    }

    /**
     * Handle INVESTIGATE action: add investigation notes and continue monitoring.
     */
    private void handleInvestigate(AiAdminIncident incident, AiCharacter admin, LlmAnalysisResult llmResult) {
        String notes = "LLM Investigation: " + llmResult.getReasoning();
        
        try {
            incidentService.investigate(incident.getId(), admin.getId(), notes, false);
            addAuditNote(incident, admin, "Investigated: " + llmResult.getReasoning());
            log.info("Incident {} investigated by '{}'", incident.getId(), admin.getName());
        } catch (Exception e) {
            log.error("Failed to investigate incident {}: {}", incident.getId(), e.getMessage());
        }
    }

    /**
     * Handle RESOLVE action: resolve the incident with resolution action and root cause.
     */
    private void handleResolve(AiAdminIncident incident, AiCharacter admin, LlmAnalysisResult llmResult) {
        Map<String, Object> params = llmResult.getParameters();
        String resolutionAction = params.containsKey("resolutionAction") 
                ? params.get("resolutionAction").toString() 
                : llmResult.getReasoning();
        String rootCause = params.containsKey("rootCause") 
                ? params.get("rootCause").toString() 
                : "LLM-determined root cause";

        try {
            incidentService.resolve(incident.getId(), admin.getId(), resolutionAction, rootCause);
            addAuditNote(incident, admin, "Resolved: " + resolutionAction + " (Root cause: " + rootCause + ")");
            log.info("Incident {} resolved by '{}': {}", incident.getId(), admin.getName(), resolutionAction);
        } catch (Exception e) {
            log.error("Failed to resolve incident {}: {}", incident.getId(), e.getMessage());
        }
    }

    /**
     * Handle ESCALATE action: escalate the incident for human review.
     */
    private void handleEscalate(AiAdminIncident incident, LlmAnalysisResult llmResult) {
        try {
            incidentService.escalate(incident.getId());
            addAuditNote(incident, null, "Escalated: " + llmResult.getReasoning());
            log.info("Incident {} escalated: {}", incident.getId(), llmResult.getReasoning());
        } catch (Exception e) {
            log.error("Failed to escalate incident {}: {}", incident.getId(), e.getMessage());
        }
    }

    /**
     * Handle MONITOR action: add a note and continue monitoring.
     */
    private void handleMonitor(AiAdminIncident incident, AiCharacter admin, LlmAnalysisResult llmResult) {
        try {
            addAuditNote(incident, admin, "Monitoring: " + llmResult.getReasoning());
            log.info("Incident {} is being monitored by '{}': {}", incident.getId(), admin.getName(), llmResult.getReasoning());
        } catch (Exception e) {
            log.error("Failed to add monitoring note for incident {}: {}", incident.getId(), e.getMessage());
        }
    }

    /**
     * Handle DEPLOY/REPAIR action: create a work order for the dispatch service.
     */
    private void handleDeployOrRepair(AiAdminIncident incident, AiCharacter admin, LlmAnalysisResult llmResult) {
        Map<String, Object> params = llmResult.getParameters();
        String workOrderText = params.containsKey("workOrderText") 
                ? params.get("workOrderText").toString() 
                : "Automated work order for incident #" + incident.getId() + ": " + llmResult.getReasoning();

        try {
            String submittedBy = "AIAdmin:" + admin.getName();
            AiAdminOrder order = aiAdminService.submitOrder(submittedBy, workOrderText);
            
            addAuditNote(incident, admin, "Created work order #" + order.getId() + ": " + workOrderText);
            log.info("Created work order {} for incident {} by '{}'", order.getId(), incident.getId(), admin.getName());
        } catch (Exception e) {
            log.error("Failed to create work order for incident {}: {}", incident.getId(), e.getMessage());
        }
    }

    /**
     * Add an audit note via collaboration service.
     */
    private void addAuditNote(AiAdminIncident incident, AiCharacter admin, String note) {
        try {
            String prefix = admin != null ? "[Automation - " + admin.getName() + "] " : "[Automation] ";
            collaborationService.addNote(incident.getId(), admin != null ? admin.getId() : 0L, prefix + note);
        } catch (Exception e) {
            log.error("Failed to add audit note for incident {}: {}", incident.getId(), e.getMessage());
        }
    }

    /**
     * Find an available AI admin for the incident.
     * Prefers admins in the same galaxy/empire, then falls back to any available admin.
     */
    private AiCharacter findAvailableAdmin(AiAdminIncident incident) {
        // Try to find an admin in the same galaxy/empire first
        List<AiCharacter> adminsInEmpire = characterRepository.findByAiAdminTrueAndActiveTrueAndEmpireName(incident.getGalaxyName());
        for (AiCharacter admin : adminsInEmpire) {
            if (admin.getActiveOrderCount() < 3) {
                return admin;
            }
        }

        // Fall back to any available admin
        List<AiCharacter> allAdmins = characterRepository.findByAiAdminTrueAndActiveTrue();
        for (AiCharacter admin : allAdmins) {
            if (admin.getActiveOrderCount() < 3) {
                return admin;
            }
        }

        return null;
    }

    /**
     * Check if the circuit breaker is open for an incident.
     */
    private boolean isCircuitBreakerOpen(Long incidentId) {
        Integer failures = llmFailureCounts.get(incidentId);
        if (failures != null && failures >= maxLlmFailures) {
            // Check if cooldown has passed
            LocalDateTime lastCheck = lastCheckTimes.get(incidentId);
            if (lastCheck != null) {
                long elapsedMs = java.time.Duration.between(lastCheck, LocalDateTime.now()).toMillis();
                if (elapsedMs < circuitBreakerCooldownMs) {
                    return true;
                }
                // Cooldown passed, reset
                llmFailureCounts.remove(incidentId);
            }
        }
        return false;
    }

    /**
     * Check if the incident is rate limited.
     */
    private boolean isRateLimited(Long incidentId) {
        LocalDateTime lastCheck = lastCheckTimes.get(incidentId);
        if (lastCheck != null) {
            long elapsedMs = java.time.Duration.between(lastCheck, LocalDateTime.now()).toMillis();
            return elapsedMs < checkIntervalMs;
        }
        return false;
    }

    /**
     * Record an LLM failure for circuit breaker tracking.
     */
    private void recordLlmFailure(Long incidentId) {
        int failures = llmFailureCounts.merge(incidentId, 1, Integer::sum);
        log.warn("LLM failure {} for incident {} (circuit breaker threshold: {})", failures, incidentId, maxLlmFailures);
    }

    // Getters for configuration
    public boolean isAutomationEnabled() {
        return automationEnabled;
    }

    public long getCheckIntervalMs() {
        return checkIntervalMs;
    }

    public int getMaxLlmFailures() {
        return maxLlmFailures;
    }

    public long getCircuitBreakerCooldownMs() {
        return circuitBreakerCooldownMs;
    }
}
