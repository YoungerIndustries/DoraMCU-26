package com.yisy.modules.doramcu.admin.service;

import com.yisy.modules.doramcu.admin.model.AiAdminIncident;
import com.yisy.modules.doramcu.admin.model.AiAdminIncidentCollaborator;
import com.yisy.modules.doramcu.admin.model.AiAdminNotification;
import com.yisy.modules.doramcu.admin.model.AiAdminNotificationType;
import com.yisy.modules.doramcu.admin.model.AiAdminOrder;
import com.yisy.modules.doramcu.admin.repository.AiAdminIncidentRepository;
import com.yisy.modules.doramcu.admin.repository.AiAdminNotificationRepository;
import com.yisy.modules.doramcu.eve.model.AiCharacter;
import com.yisy.modules.doramcu.repository.ai.AiCharacterRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service for managing AI admin incident lifecycle.
 * Incidents are created for severe health findings and tracked through:
 * OPEN → INVESTIGATING → RESOLVED/ESCALATED/CLOSED.
 */
@Service
public class AiAdminIncidentService {

    private static final Logger log = LoggerFactory.getLogger(AiAdminIncidentService.class);

    /** Hours before an OPEN incident is auto-escalated */
    private static final int ESCALATION_OPEN_HOURS = 1;

    /** Hours before an INVESTIGATING incident is auto-escalated */
    private static final int ESCALATION_INVESTIGATING_HOURS = 2;

    private final AiAdminIncidentRepository incidentRepository;
    private final AiAdminNotificationRepository notificationRepository;
    private final AiCharacterRepository characterRepository;
    private final AiAdminLlmService llmService;
    private final AiAdminNotificationService notificationService;
    private final AiAdminCollaborationService collaborationService;

    public AiAdminIncidentService(AiAdminIncidentRepository incidentRepository,
                                   AiAdminNotificationRepository notificationRepository,
                                   AiCharacterRepository characterRepository,
                                   AiAdminLlmService llmService,
                                   AiAdminNotificationService notificationService,
                                   AiAdminCollaborationService collaborationService) {
        this.incidentRepository = incidentRepository;
        this.notificationRepository = notificationRepository;
        this.characterRepository = characterRepository;
        this.llmService = llmService;
        this.notificationService = notificationService;
        this.collaborationService = collaborationService;
    }

    /**
     * Create a new incident.
     */
    @Transactional
    public AiAdminIncident createIncident(AiAdminIncident.IncidentType type,
                                           AiAdminIncident.Severity severity,
                                           String title,
                                           String description,
                                           String galaxyName) {
        return createIncident(type, severity, title, description, galaxyName, null);
    }

    /**
     * Create a new incident with an optional related order ID.
     */
    @Transactional
    public AiAdminIncident createIncident(AiAdminIncident.IncidentType type,
                                           AiAdminIncident.Severity severity,
                                           String title,
                                           String description,
                                           String galaxyName,
                                           Long relatedOrderId) {
        AiAdminIncident incident = new AiAdminIncident();
        incident.setIncidentType(type);
        incident.setSeverity(severity);
        incident.setPriority(calculatePriority(severity));
        incident.setStatus(AiAdminIncident.Status.OPEN);
        incident.setTitle(title);
        incident.setDescription(description);
        incident.setGalaxyName(galaxyName);
        incident.setCreatedAt(LocalDateTime.now());
        incident.setUpdatedAt(LocalDateTime.now());
        if (relatedOrderId != null) {
            incident.setRelatedOrderId(relatedOrderId);
        }
        AiAdminIncident saved = incidentRepository.save(incident);
        log.info("Created incident #{}: {} [{}] in galaxy {}", saved.getId(), title, severity, galaxyName);

        // Auto-add assigned admin as LEAD collaborator if assigned
        if (saved.getAssignedAiAdminId() != null) {
            try {
                collaborationService.addCollaborator(saved.getId(), saved.getAssignedAiAdminId(),
                        AiAdminIncidentCollaborator.Role.LEAD);
            } catch (Exception e) {
                log.warn("Failed to add lead collaborator for incident #{}: {}", saved.getId(), e.getMessage());
            }
        }

        return saved;
    }

    /**
     * Assign an incident to an AI admin for investigation.
     */
    @Transactional
    public AiAdminIncident assignIncident(Long incidentId, Long aiAdminId) {
        AiAdminIncident incident = incidentRepository.findById(incidentId)
                .orElseThrow(() -> new IllegalArgumentException("Incident not found: " + incidentId));

        if (incident.getStatus() == AiAdminIncident.Status.CLOSED) {
            throw new IllegalStateException("Cannot assign a closed incident");
        }

        incident.setAssignedAiAdminId(aiAdminId);
        incident.setStatus(AiAdminIncident.Status.INVESTIGATING);
        incident.setUpdatedAt(LocalDateTime.now());

        // Notify the assigned admin
        try {
            AiAdminNotification notification = new AiAdminNotification();
            notification.setAiAdminId(aiAdminId);
            notification.setNotificationType(AiAdminNotificationType.SYSTEM_ALERT);
            notification.setTitle("Incident Assigned");
            notification.setMessage("Incident #" + incidentId + ": " + incident.getTitle());
            notification.setOrderId(incidentId);
            notification.setRead(false);
            notification.setCreatedAt(LocalDateTime.now());
            notification.setExpiresAt(LocalDateTime.now().plusDays(7));
            notificationRepository.save(notification);
        } catch (Exception e) {
            log.warn("Failed to send assignment notification for incident #{}: {}", incidentId, e.getMessage());
        }

        return incidentRepository.save(incident);
    }

    /**
     * Investigate an incident, optionally using LLM analysis.
     */
    @Transactional
    public AiAdminIncident investigate(Long incidentId, Long aiAdminId, String notes, boolean useLlm) {
        AiAdminIncident incident = incidentRepository.findById(incidentId)
                .orElseThrow(() -> new IllegalArgumentException("Incident not found: " + incidentId));

        if (incident.getStatus() == AiAdminIncident.Status.CLOSED) {
            throw new IllegalStateException("Cannot investigate a closed incident");
        }

        if (incident.getStatus() != AiAdminIncident.Status.INVESTIGATING) {
            incident.setStatus(AiAdminIncident.Status.INVESTIGATING);
        }

        // Build investigation notes
        StringBuilder fullNotes = new StringBuilder();
        if (notes != null && !notes.isBlank()) {
            fullNotes.append(notes);
        }

        // Optionally use LLM for investigation analysis
        if (useLlm) {
            try {
                AiCharacter admin = characterRepository.findById(aiAdminId).orElse(null);
                String context = buildInvestigationContext(incident);
                // Create a temporary order to pass to the LLM service
                AiAdminOrder tempOrder = new AiAdminOrder();
                tempOrder.setOrderText(context);
                tempOrder.setSubmittedBy("Incident:" + incident.getId());
                AiAdminLlmService.LlmAnalysisResult result = llmService.analyzeOrder(admin, tempOrder);
                if (result != null) {
                    if (fullNotes.length() > 0) {
                        fullNotes.append("\n\n");
                    }
                    fullNotes.append("[LLM Analysis] Action: ").append(result.getAction())
                            .append("\nReasoning: ").append(result.getReasoning());
                }
            } catch (Exception e) {
                log.warn("LLM investigation failed for incident #{}: {}", incidentId, e.getMessage());
                if (fullNotes.length() > 0) {
                    fullNotes.append("\n\n");
                }
                fullNotes.append("[LLM unavailable: ").append(e.getMessage()).append("]");
            }
        }

        incident.setInvestigatorNotes(fullNotes.toString());
        incident.setUpdatedAt(LocalDateTime.now());
        return incidentRepository.save(incident);
    }

    /**
     * Resolve an incident with action taken and root cause.
     */
    @Transactional
    public AiAdminIncident resolve(Long incidentId, Long aiAdminId, String action, String rootCause) {
        AiAdminIncident incident = incidentRepository.findById(incidentId)
                .orElseThrow(() -> new IllegalArgumentException("Incident not found: " + incidentId));

        if (incident.getStatus() == AiAdminIncident.Status.CLOSED) {
            throw new IllegalStateException("Cannot resolve a closed incident");
        }

        incident.setStatus(AiAdminIncident.Status.RESOLVED);
        incident.setResolutionAction(action);
        incident.setRootCause(rootCause);
        incident.setResolvedAt(LocalDateTime.now());
        incident.setUpdatedAt(LocalDateTime.now());

        // Notify the admin
        if (aiAdminId != null) {
            try {
                AiAdminNotification notification = new AiAdminNotification();
                notification.setAiAdminId(aiAdminId);
                notification.setNotificationType(AiAdminNotificationType.SYSTEM_ALERT);
                notification.setTitle("Incident Resolved");
                notification.setMessage("Incident #" + incidentId + ": " + incident.getTitle() + " resolved.");
                notification.setOrderId(incidentId);
                notification.setRead(false);
                notification.setCreatedAt(java.time.LocalDateTime.now());
                notification.setExpiresAt(java.time.LocalDateTime.now().plusDays(7));
                notificationRepository.save(notification);
            } catch (Exception e) {
                log.warn("Failed to send resolution notification for incident #{}: {}", incidentId, e.getMessage());
            }
        }

        return incidentRepository.save(incident);
    }

    /**
     * Escalate an incident.
     */
    @Transactional
    public AiAdminIncident escalate(Long incidentId) {
        AiAdminIncident incident = incidentRepository.findById(incidentId)
                .orElseThrow(() -> new IllegalArgumentException("Incident not found: " + incidentId));

        if (incident.getStatus() == AiAdminIncident.Status.CLOSED) {
            throw new IllegalStateException("Cannot escalate a closed incident");
        }

        incident.setStatus(AiAdminIncident.Status.ESCALATED);
        incident.setEscalatedAt(LocalDateTime.now());
        incident.setUpdatedAt(LocalDateTime.now());
        AiAdminIncident saved = incidentRepository.save(incident);

        log.warn("Incident #{} escalated: {} [{}]", incidentId, incident.getTitle(), incident.getSeverity());

        // Auto-invite collaborators for CRITICAL and HIGH severity incidents
        try {
            List<AiAdminIncidentCollaborator> invited = collaborationService.autoInviteCollaborators(incidentId);
            if (!invited.isEmpty()) {
                log.info("Auto-invited {} collaborators to escalated incident #{}", invited.size(), incidentId);
            }
        } catch (Exception e) {
            log.warn("Failed to auto-invite collaborators for incident #{}: {}", incidentId, e.getMessage());
        }

        return saved;
    }

    /**
     * Close an incident.
     */
    @Transactional
    public AiAdminIncident close(Long incidentId) {
        AiAdminIncident incident = incidentRepository.findById(incidentId)
                .orElseThrow(() -> new IllegalArgumentException("Incident not found: " + incidentId));

        incident.setStatus(AiAdminIncident.Status.CLOSED);
        incident.setUpdatedAt(LocalDateTime.now());
        return incidentRepository.save(incident);
    }

    /**
     * Get all incidents for a galaxy.
     */
    @Transactional(readOnly = true)
    public List<AiAdminIncident> getIncidentsForGalaxy(String galaxyName) {
        if (galaxyName == null || galaxyName.isBlank()) {
            return List.of();
        }
        return incidentRepository.findByGalaxyNameOrderByCreatedAtDesc(galaxyName);
    }

    /**
     * Get all open incidents across all galaxies.
     */
    @Transactional(readOnly = true)
    public List<AiAdminIncident> getOpenIncidents() {
        return incidentRepository.findOpenIncidents(AiAdminIncident.Status.OPEN);
    }

    /**
     * Get incidents by status.
     */
    @Transactional(readOnly = true)
    public List<AiAdminIncident> getIncidentsByStatus(AiAdminIncident.Status status) {
        return incidentRepository.findByStatusIn(List.of(status));
    }

    /**
     * Get incidents assigned to an AI admin.
     */
    @Transactional(readOnly = true)
    public List<AiAdminIncident> getIncidentsForAdmin(Long aiAdminId) {
        if (aiAdminId == null) {
            return List.of();
        }
        return incidentRepository.findByAssignedAiAdminIdOrderByCreatedAtDesc(aiAdminId);
    }

    /**
     * Get incident dashboard summary.
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getDashboardSummary() {
        Map<String, Object> summary = new HashMap<>();
        summary.put("open", incidentRepository.countByStatus(AiAdminIncident.Status.OPEN));
        summary.put("investigating", incidentRepository.countByStatus(AiAdminIncident.Status.INVESTIGATING));
        summary.put("resolved", incidentRepository.countByStatus(AiAdminIncident.Status.RESOLVED));
        summary.put("escalated", incidentRepository.countByStatus(AiAdminIncident.Status.ESCALATED));
        summary.put("closed", incidentRepository.countByStatus(AiAdminIncident.Status.CLOSED));
        summary.put("critical", incidentRepository.countBySeverity(AiAdminIncident.Severity.CRITICAL));
        summary.put("high", incidentRepository.countBySeverity(AiAdminIncident.Severity.HIGH));
        summary.put("medium", incidentRepository.countBySeverity(AiAdminIncident.Severity.MEDIUM));
        summary.put("low", incidentRepository.countBySeverity(AiAdminIncident.Severity.LOW));
        return summary;
    }

    /**
     * Scheduled task to escalate stale incidents.
     * Runs every 10 minutes.
     */
    @Scheduled(fixedRate = 600000)
    @Transactional
    public void escalateStaleIncidents() {
        LocalDateTime openThreshold = LocalDateTime.now().minusHours(ESCALATION_OPEN_HOURS);
        LocalDateTime investigatingThreshold = LocalDateTime.now().minusHours(ESCALATION_INVESTIGATING_HOURS);

        // Escalate OPEN incidents older than threshold
        List<AiAdminIncident> staleOpen = incidentRepository
                .findByStatusAndCreatedAtBefore(AiAdminIncident.Status.OPEN, openThreshold);
        for (AiAdminIncident incident : staleOpen) {
            escalate(incident.getId());
            log.info("Auto-escalated stale OPEN incident #{}: {}", incident.getId(), incident.getTitle());
        }

        // Escalate INVESTIGATING incidents older than threshold
        List<AiAdminIncident> staleInvestigating = incidentRepository
                .findByStatusAndCreatedAtBeforeAndEscalatedAtIsNull(
                        AiAdminIncident.Status.INVESTIGATING, investigatingThreshold);
        for (AiAdminIncident incident : staleInvestigating) {
            escalate(incident.getId());
            log.info("Auto-escalated stale INVESTIGATING incident #{}: {}", incident.getId(), incident.getTitle());
        }
    }

    /**
     * Calculate priority based on severity.
     */
    private int calculatePriority(AiAdminIncident.Severity severity) {
        return switch (severity) {
            case CRITICAL -> 10;
            case HIGH -> 7;
            case MEDIUM -> 5;
            case LOW -> 3;
        };
    }

    /**
     * Build context string for LLM investigation.
     */
    private String buildInvestigationContext(AiAdminIncident incident) {
        return "Incident #" + incident.getId() +
                "\nType: " + incident.getIncidentType() +
                "\nSeverity: " + incident.getSeverity() +
                "\nGalaxy: " + incident.getGalaxyName() +
                "\nTitle: " + incident.getTitle() +
                "\nDescription: " + incident.getDescription();
    }
}
