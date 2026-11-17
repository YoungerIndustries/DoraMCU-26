package com.yisy.modules.doramcu.admin.service;

import com.yisy.modules.doramcu.admin.model.AiAdminCollaborationNote;
import com.yisy.modules.doramcu.admin.model.AiAdminIncident;
import com.yisy.modules.doramcu.admin.model.AiAdminIncidentCollaborator;
import com.yisy.modules.doramcu.admin.model.AiAdminNotification;
import com.yisy.modules.doramcu.admin.model.AiAdminNotificationType;
import com.yisy.modules.doramcu.admin.repository.AiAdminCollaborationNoteRepository;
import com.yisy.modules.doramcu.admin.repository.AiAdminIncidentCollaboratorRepository;
import com.yisy.modules.doramcu.admin.repository.AiAdminIncidentRepository;
import com.yisy.modules.doramcu.admin.repository.AiAdminNotificationRepository;
import com.yisy.modules.doramcu.eve.model.AiCharacter;
import com.yisy.modules.doramcu.repository.ai.AiCharacterRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for managing AI admin collaboration on incidents.
 * Enables multiple AI admins to work together with roles and an audit trail.
 */
@Service
public class AiAdminCollaborationService {

    private static final Logger log = LoggerFactory.getLogger(AiAdminCollaborationService.class);

    private final AiAdminIncidentCollaboratorRepository collaboratorRepository;
    private final AiAdminCollaborationNoteRepository noteRepository;
    private final AiAdminIncidentRepository incidentRepository;
    private final AiCharacterRepository characterRepository;
    private final AiAdminNotificationRepository notificationRepository;

    public AiAdminCollaborationService(AiAdminIncidentCollaboratorRepository collaboratorRepository,
                                       AiAdminCollaborationNoteRepository noteRepository,
                                       AiAdminIncidentRepository incidentRepository,
                                       AiCharacterRepository characterRepository,
                                       AiAdminNotificationRepository notificationRepository) {
        this.collaboratorRepository = collaboratorRepository;
        this.noteRepository = noteRepository;
        this.incidentRepository = incidentRepository;
        this.characterRepository = characterRepository;
        this.notificationRepository = notificationRepository;
    }

    /**
     * Add a collaborator to an incident.
     */
    @Transactional
    public AiAdminIncidentCollaborator addCollaborator(Long incidentId, Long aiAdminId, AiAdminIncidentCollaborator.Role role) {
        AiAdminIncident incident = incidentRepository.findById(incidentId)
                .orElseThrow(() -> new IllegalArgumentException("Incident not found: " + incidentId));

        // Check if already a collaborator
        Optional<AiAdminIncidentCollaborator> existing = collaboratorRepository.findByIncidentIdAndAiAdminId(incidentId, aiAdminId);
        if (existing.isPresent()) {
            AiAdminIncidentCollaborator collab = existing.get();
            if (collab.getStatus() != AiAdminIncidentCollaborator.Status.REMOVED) {
                throw new IllegalStateException("Admin is already a collaborator on this incident");
            }
            // Re-activate removed collaborator
            collab.setStatus(AiAdminIncidentCollaborator.Status.ACTIVE);
            collab.setRole(role);
            collab.setJoinedAt(LocalDateTime.now());
            collab.setCompletedAt(null);
            log.info("Re-activated collaborator #{} for incident #{}", aiAdminId, incidentId);
            return collaboratorRepository.save(collab);
        }

        // Check LEAD uniqueness
        if (role == AiAdminIncidentCollaborator.Role.LEAD) {
            Optional<AiAdminIncidentCollaborator> existingLead = collaboratorRepository.findLeadCollaborator(incidentId);
            if (existingLead.isPresent()) {
                throw new IllegalStateException("Incident already has a lead collaborator");
            }
        }

        AiAdminIncidentCollaborator collaborator = new AiAdminIncidentCollaborator();
        collaborator.setIncidentId(incidentId);
        collaborator.setAiAdminId(aiAdminId);
        collaborator.setRole(role);
        collaborator.setStatus(AiAdminIncidentCollaborator.Status.ACTIVE);
        collaborator.setJoinedAt(LocalDateTime.now());
        collaboratorRepository.save(collaborator);

        // Notify the collaborator
        try {
            sendCollaborationNotification(aiAdminId, incidentId, incident.getTitle(), role);
        } catch (Exception e) {
            log.warn("Failed to send collaboration notification: {}", e.getMessage());
        }

        log.info("Added collaborator #{} with role {} to incident #{}", aiAdminId, role, incidentId);
        return collaborator;
    }

    /**
     * Remove a collaborator from an incident.
     */
    @Transactional
    public void removeCollaborator(Long incidentId, Long aiAdminId) {
        AiAdminIncidentCollaborator collaborator = collaboratorRepository.findByIncidentIdAndAiAdminId(incidentId, aiAdminId)
                .orElseThrow(() -> new IllegalArgumentException("Collaborator not found"));

        if (collaborator.getRole() == AiAdminIncidentCollaborator.Role.LEAD) {
            long activeCount = collaboratorRepository.countByIncidentIdAndStatus(incidentId, AiAdminIncidentCollaborator.Status.ACTIVE);
            if (activeCount <= 1) {
                throw new IllegalStateException("Cannot remove the only lead collaborator");
            }
        }

        collaborator.setStatus(AiAdminIncidentCollaborator.Status.REMOVED);
        collaboratorRepository.save(collaborator);
        log.info("Removed collaborator #{} from incident #{}", aiAdminId, incidentId);
    }

    /**
     * Get all collaborators for an incident.
     */
    @Transactional(readOnly = true)
    public List<AiAdminIncidentCollaborator> getCollaborators(Long incidentId) {
        return collaboratorRepository.findByIncidentIdOrderByJoinedAtAsc(incidentId);
    }

    /**
     * Get active collaborators for an incident.
     */
    @Transactional(readOnly = true)
    public List<AiAdminIncidentCollaborator> getActiveCollaborators(Long incidentId) {
        return collaboratorRepository.findActiveCollaborators(incidentId);
    }

    /**
     * Add a collaboration note to an incident.
     */
    @Transactional
    public AiAdminCollaborationNote addNote(Long incidentId, Long aiAdminId, String note) {
        if (note == null || note.isBlank()) {
            throw new IllegalArgumentException("Note cannot be empty");
        }

        // Verify the admin is a collaborator
        Optional<AiAdminIncidentCollaborator> collaborator = collaboratorRepository.findByIncidentIdAndAiAdminId(incidentId, aiAdminId);
        if (collaborator.isEmpty() || collaborator.get().getStatus() == AiAdminIncidentCollaborator.Status.REMOVED) {
            throw new IllegalStateException("Admin is not an active collaborator on this incident");
        }

        AiAdminCollaborationNote collaborationNote = new AiAdminCollaborationNote();
        collaborationNote.setIncidentId(incidentId);
        collaborationNote.setAiAdminId(aiAdminId);
        collaborationNote.setNote(note);
        collaborationNote.setCreatedAt(LocalDateTime.now());
        noteRepository.save(collaborationNote);

        log.info("Added note by admin #{} to incident #{}", aiAdminId, incidentId);
        return collaborationNote;
    }

    /**
     * Get all notes for an incident.
     */
    @Transactional(readOnly = true)
    public List<AiAdminCollaborationNote> getNotes(Long incidentId) {
        return noteRepository.findByIncidentIdOrderByCreatedAtAsc(incidentId);
    }

    /**
     * Auto-invite collaborators based on incident severity and expertise.
     */
    @Transactional
    public List<AiAdminIncidentCollaborator> autoInviteCollaborators(Long incidentId) {
        AiAdminIncident incident = incidentRepository.findById(incidentId)
                .orElseThrow(() -> new IllegalArgumentException("Incident not found: " + incidentId));

        // Only auto-invite for CRITICAL and HIGH severity
        if (incident.getSeverity() != AiAdminIncident.Severity.CRITICAL &&
                incident.getSeverity() != AiAdminIncident.Severity.HIGH) {
            log.info("Skipping auto-invite for {} severity incident #{}", incident.getSeverity(), incidentId);
            return Collections.emptyList();
        }

        // Get existing collaborators
        List<AiAdminIncidentCollaborator> existing = collaboratorRepository.findByIncidentIdOrderByJoinedAtAsc(incidentId);
        Set<Long> existingAdminIds = existing.stream()
                .map(AiAdminIncidentCollaborator::getAiAdminId)
                .collect(Collectors.toSet());

        // Find available AI admins
        List<AiCharacter> allAdmins = characterRepository.findByAiAdminTrueAndActiveTrueOrderByTotalExperienceDesc();
        List<AiCharacter> candidates = allAdmins.stream()
                .filter(admin -> !existingAdminIds.contains(admin.getId()))
                .filter(admin -> admin.getActiveOrderCount() < 3)
                .collect(Collectors.toList());

        // Determine how many to invite based on severity
        int inviteCount = incident.getSeverity() == AiAdminIncident.Severity.CRITICAL ? 2 : 1;
        List<AiAdminIncidentCollaborator> invited = new ArrayList<>();

        for (int i = 0; i < Math.min(inviteCount, candidates.size()); i++) {
            AiCharacter admin = candidates.get(i);
            try {
                AiAdminIncidentCollaborator.Role role = assignRoleBasedOnExpertise(admin, incident);
                AiAdminIncidentCollaborator collab = addCollaborator(incidentId, admin.getId(), role);
                invited.add(collab);
            } catch (Exception e) {
                log.warn("Failed to invite admin #{} to incident #{}: {}", admin.getId(), incidentId, e.getMessage());
            }
        }

        log.info("Auto-invited {} collaborators to incident #{}", invited.size(), incidentId);
        return invited;
    }

    /**
     * Get a collaboration summary for an incident.
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getCollaborationSummary(Long incidentId) {
        List<AiAdminIncidentCollaborator> collaborators = collaboratorRepository.findByIncidentIdOrderByJoinedAtAsc(incidentId);
        List<AiAdminCollaborationNote> notes = noteRepository.findByIncidentIdOrderByCreatedAtAsc(incidentId);

        Map<String, Object> summary = new HashMap<>();
        summary.put("incidentId", incidentId);
        summary.put("totalCollaborators", collaborators.size());
        summary.put("activeCollaborators", collaborators.stream()
                .filter(c -> c.getStatus() == AiAdminIncidentCollaborator.Status.ACTIVE).count());
        summary.put("completedCollaborators", collaborators.stream()
                .filter(c -> c.getStatus() == AiAdminIncidentCollaborator.Status.COMPLETED).count());
        summary.put("totalNotes", notes.size());

        List<Map<String, Object>> collaboratorDetails = new ArrayList<>();
        for (AiAdminIncidentCollaborator c : collaborators) {
            Map<String, Object> detail = new HashMap<>();
            detail.put("aiAdminId", c.getAiAdminId());
            detail.put("role", c.getRole() != null ? c.getRole().name() : null);
            detail.put("status", c.getStatus() != null ? c.getStatus().name() : null);
            detail.put("joinedAt", c.getJoinedAt());
            detail.put("completedAt", c.getCompletedAt());

            // Get admin name
            characterRepository.findById(c.getAiAdminId()).ifPresent(admin -> {
                detail.put("adminName", admin.getName());
                detail.put("adminRole", admin.getRole());
                detail.put("empireName", admin.getEmpireName());
            });

            collaboratorDetails.add(detail);
        }
        summary.put("collaborators", collaboratorDetails);

        List<Map<String, Object>> noteDetails = new ArrayList<>();
        for (AiAdminCollaborationNote n : notes) {
            Map<String, Object> noteDetail = new HashMap<>();
            noteDetail.put("id", n.getId());
            noteDetail.put("aiAdminId", n.getAiAdminId());
            noteDetail.put("note", n.getNote());
            noteDetail.put("createdAt", n.getCreatedAt());

            characterRepository.findById(n.getAiAdminId()).ifPresent(admin -> {
                noteDetail.put("adminName", admin.getName());
            });

            noteDetails.add(noteDetail);
        }
        summary.put("notes", noteDetails);

        return summary;
    }

    /**
     * Mark a collaborator as completed.
     */
    @Transactional
    public void markCollaboratorCompleted(Long incidentId, Long aiAdminId) {
        AiAdminIncidentCollaborator collaborator = collaboratorRepository.findByIncidentIdAndAiAdminId(incidentId, aiAdminId)
                .orElseThrow(() -> new IllegalArgumentException("Collaborator not found"));

        collaborator.setStatus(AiAdminIncidentCollaborator.Status.COMPLETED);
        collaborator.setCompletedAt(LocalDateTime.now());
        collaboratorRepository.save(collaborator);
        log.info("Marked collaborator #{} as completed for incident #{}", aiAdminId, incidentId);
    }

    /**
     * Assign a role based on the admin's expertise.
     */
    private AiAdminIncidentCollaborator.Role assignRoleBasedOnExpertise(AiCharacter admin, AiAdminIncident incident) {
        String role = admin.getRole() != null ? admin.getRole().toLowerCase() : "";
        if (role.contains("lead") || role.contains("overseer") || role.contains("general")) {
            return AiAdminIncidentCollaborator.Role.LEAD;
        } else if (role.contains("analyst") || role.contains("advisor")) {
            return AiAdminIncidentCollaborator.Role.ANALYST;
        } else if (role.contains("review") || role.contains("audit")) {
            return AiAdminIncidentCollaborator.Role.REVIEWER;
        }
        return AiAdminIncidentCollaborator.Role.INVESTIGATOR;
    }

    /**
     * Send a notification to a collaborator.
     */
    private void sendCollaborationNotification(Long aiAdminId, Long incidentId, String incidentTitle, AiAdminIncidentCollaborator.Role role) {
        AiAdminNotification notification = new AiAdminNotification();
        notification.setAiAdminId(aiAdminId);
        notification.setNotificationType(AiAdminNotificationType.SYSTEM_ALERT);
        notification.setTitle("Incident Collaboration Request");
        notification.setMessage("You have been added as " + role.name() + " to incident: " + incidentTitle);
        notification.setOrderId(incidentId);
        notification.setRead(false);
        notification.setCreatedAt(LocalDateTime.now());
        notification.setExpiresAt(LocalDateTime.now().plusDays(7));
        notificationRepository.save(notification);
    }
}
