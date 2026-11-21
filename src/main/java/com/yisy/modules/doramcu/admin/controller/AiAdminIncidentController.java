package com.yisy.modules.doramcu.admin.controller;

import com.yisy.modules.doramcu.admin.model.AiAdminCollaborationNote;
import com.yisy.modules.doramcu.admin.model.AiAdminIncident;
import com.yisy.modules.doramcu.admin.model.AiAdminIncidentCollaborator;
import com.yisy.modules.doramcu.admin.service.AiAdminCollaborationService;
import com.yisy.modules.doramcu.admin.service.AiAdminIncidentService;
import com.yisy.modules.doramcu.eve.model.AiCharacter;
import com.yisy.modules.doramcu.repository.ai.AiCharacterRepository;
import com.yisy.modules.doramcu.model.players.Player;
import com.yisy.modules.doramcu.model.user.User;
import com.yisy.modules.doramcu.repository.players.PlayerRepository;
import com.yisy.modules.doramcu.repository.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * REST controller for AI admin incident management, collaboration, and specialization endpoints.
 * Extracted from AiAdminController to reduce god class size.
 * <p>
 * Provides incident CRUD, investigation workflow, collaboration management,
 * and AI admin specialization assignment.
 * </p>
 */
@RestController
@RequestMapping("/api/v1/admin/ai-orders")
public class AiAdminIncidentController {

    private static final Logger log = LoggerFactory.getLogger(AiAdminIncidentController.class);

    private final AiAdminIncidentService incidentService;
    private final AiAdminCollaborationService collaborationService;
    private final AiCharacterRepository aiCharacterRepository;
    private final PlayerRepository playerRepository;
    private final UserRepository userRepository;

    public AiAdminIncidentController(AiAdminIncidentService incidentService,
                                      AiAdminCollaborationService collaborationService,
                                      AiCharacterRepository aiCharacterRepository,
                                      PlayerRepository playerRepository,
                                      UserRepository userRepository) {
        this.incidentService = incidentService;
        this.collaborationService = collaborationService;
        this.aiCharacterRepository = aiCharacterRepository;
        this.playerRepository = playerRepository;
        this.userRepository = userRepository;
    }

    // ==================== INCIDENT MANAGEMENT ENDPOINTS ====================

    /**
     * Create a new incident manually.
     */
    @PostMapping("/incidents")
    public ResponseEntity<?> createIncident(@RequestBody Map<String, Object> body, Authentication authentication) {
        if (!isAdmin(authentication)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Admin access required"));
        }
        String type = (String) body.get("type");
        String severity = (String) body.get("severity");
        String title = (String) body.get("title");
        String description = (String) body.get("description");
        String galaxyName = (String) body.get("galaxyName");
        if (title == null || title.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Title is required"));
        }
        if (galaxyName == null || galaxyName.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Galaxy name is required"));
        }
        AiAdminIncident.IncidentType incidentType = parseIncidentType(type);
        AiAdminIncident.Severity incidentSeverity = parseSeverity(severity);
        AiAdminIncident incident = incidentService.createIncident(
                incidentType, incidentSeverity, title, description, galaxyName);
        return ResponseEntity.status(HttpStatus.CREATED).body(incidentToMap(incident));
    }

    /**
     * List incidents with optional filters.
     */
    @GetMapping("/incidents")
    public ResponseEntity<?> listIncidents(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String severity,
            @RequestParam(required = false) String galaxyName,
            Authentication authentication) {
        if (!isAdmin(authentication)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Admin access required"));
        }
        List<AiAdminIncident> incidents;
        if (galaxyName != null && !galaxyName.isBlank()) {
            incidents = incidentService.getIncidentsForGalaxy(galaxyName);
        } else if (status != null && !status.isBlank()) {
            AiAdminIncident.Status s = parseIncidentStatus(status);
            incidents = incidentService.getIncidentsByStatus(s);
        } else {
            incidents = incidentService.getOpenIncidents();
        }
        List<Map<String, Object>> result = incidents.stream().map(this::incidentToMap).toList();
        return ResponseEntity.ok(result);
    }

    /**
     * Get incident details by ID.
     */
    @GetMapping("/incidents/{id}")
    public ResponseEntity<?> getIncident(@PathVariable Long id, Authentication authentication) {
        if (!isAdmin(authentication)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Admin access required"));
        }
        AiAdminIncident incident = incidentService.getIncidentsByStatus(AiAdminIncident.Status.OPEN).stream()
                .filter(i -> i.getId().equals(id)).findFirst().orElse(null);
        if (incident == null) {
            // Try all statuses
            for (AiAdminIncident.Status s : AiAdminIncident.Status.values()) {
                incident = incidentService.getIncidentsByStatus(s).stream()
                        .filter(i -> i.getId().equals(id)).findFirst().orElse(null);
                if (incident != null) break;
            }
        }
        if (incident == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Incident not found"));
        }
        return ResponseEntity.ok(incidentToMap(incident));
    }

    /**
     * Assign an incident to an AI admin.
     */
    @PostMapping("/incidents/{id}/assign")
    public ResponseEntity<?> assignIncident(@PathVariable Long id, @RequestBody Map<String, Object> body, Authentication authentication) {
        if (!isAdmin(authentication)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Admin access required"));
        }
        Long adminId = ((Number) body.get("adminId")).longValue();
        try {
            AiAdminIncident incident = incidentService.assignIncident(id, adminId);
            return ResponseEntity.ok(incidentToMap(incident));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Investigate an incident with optional LLM analysis.
     */
    @PostMapping("/incidents/{id}/investigate")
    public ResponseEntity<?> investigateIncident(@PathVariable Long id, @RequestBody Map<String, Object> body, Authentication authentication) {
        if (!isAdmin(authentication)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Admin access required"));
        }
        Long adminId = ((Number) body.get("adminId")).longValue();
        String notes = (String) body.get("notes");
        boolean useLlm = body.getOrDefault("useLlm", true).equals(true);
        try {
            AiAdminIncident incident = incidentService.investigate(id, adminId, notes, useLlm);
            return ResponseEntity.ok(incidentToMap(incident));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Resolve an incident.
     */
    @PostMapping("/incidents/{id}/resolve")
    public ResponseEntity<?> resolveIncident(@PathVariable Long id, @RequestBody Map<String, Object> body, Authentication authentication) {
        if (!isAdmin(authentication)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Admin access required"));
        }
        Long adminId = ((Number) body.get("adminId")).longValue();
        String action = (String) body.get("action");
        String rootCause = (String) body.get("rootCause");
        try {
            AiAdminIncident incident = incidentService.resolve(id, adminId, action, rootCause);
            return ResponseEntity.ok(incidentToMap(incident));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Escalate an incident.
     */
    @PostMapping("/incidents/{id}/escalate")
    public ResponseEntity<?> escalateIncident(@PathVariable Long id, Authentication authentication) {
        if (!isAdmin(authentication)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Admin access required"));
        }
        try {
            AiAdminIncident incident = incidentService.escalate(id);
            return ResponseEntity.ok(incidentToMap(incident));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Close an incident.
     */
    @PostMapping("/incidents/{id}/close")
    public ResponseEntity<?> closeIncident(@PathVariable Long id, Authentication authentication) {
        if (!isAdmin(authentication)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Admin access required"));
        }
        try {
            AiAdminIncident incident = incidentService.close(id);
            return ResponseEntity.ok(incidentToMap(incident));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get incidents for a galaxy.
     */
    @GetMapping("/incidents/galaxy/{galaxyName}")
    public ResponseEntity<?> getIncidentsForGalaxy(@PathVariable String galaxyName, Authentication authentication) {
        if (!isAdmin(authentication)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Admin access required"));
        }
        List<AiAdminIncident> incidents = incidentService.getIncidentsForGalaxy(galaxyName);
        List<Map<String, Object>> result = incidents.stream().map(this::incidentToMap).toList();
        return ResponseEntity.ok(result);
    }

    /**
     * Get incident dashboard summary.
     */
    @GetMapping("/incidents/dashboard")
    public ResponseEntity<?> getIncidentDashboard(Authentication authentication) {
        if (!isAdmin(authentication)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Admin access required"));
        }
        Map<String, Object> summary = incidentService.getDashboardSummary();
        return ResponseEntity.ok(summary);
    }

    // ==================== COLLABORATION ENDPOINTS ====================

    /**
     * Add a collaborator to an incident.
     */
    @PostMapping("/incidents/{id}/collaborators")
    public ResponseEntity<?> addCollaborator(@PathVariable Long id, @RequestBody Map<String, Object> body, Authentication authentication) {
        if (!isAdmin(authentication)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Admin access required"));
        }
        Long adminId = ((Number) body.get("adminId")).longValue();
        String role = (String) body.get("role");
        AiAdminIncidentCollaborator.Role collaboratorRole;
        if (role == null || role.isBlank()) {
            collaboratorRole = AiAdminIncidentCollaborator.Role.INVESTIGATOR;
        } else {
            try {
                collaboratorRole = AiAdminIncidentCollaborator.Role.valueOf(role.toUpperCase());
            } catch (IllegalArgumentException e) {
                collaboratorRole = AiAdminIncidentCollaborator.Role.INVESTIGATOR;
            }
        }
        try {
            AiAdminIncidentCollaborator collab = collaborationService.addCollaborator(id, adminId, collaboratorRole);
            return ResponseEntity.ok(collaboratorToMap(collab));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Remove a collaborator from an incident.
     */
    @DeleteMapping("/incidents/{id}/collaborators/{adminId}")
    public ResponseEntity<?> removeCollaborator(@PathVariable Long id, @PathVariable Long adminId, Authentication authentication) {
        if (!isAdmin(authentication)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Admin access required"));
        }
        try {
            collaborationService.removeCollaborator(id, adminId);
            return ResponseEntity.ok(Map.of("message", "Collaborator removed"));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * List all collaborators for an incident.
     */
    @GetMapping("/incidents/{id}/collaborators")
    public ResponseEntity<?> listCollaborators(@PathVariable Long id, Authentication authentication) {
        if (!isAdmin(authentication)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Admin access required"));
        }
        List<AiAdminIncidentCollaborator> collaborators = collaborationService.getCollaborators(id);
        List<Map<String, Object>> result = collaborators.stream().map(this::collaboratorToMap).toList();
        return ResponseEntity.ok(result);
    }

    /**
     * Add a collaboration note to an incident.
     */
    @PostMapping("/incidents/{id}/notes")
    public ResponseEntity<?> addNote(@PathVariable Long id, @RequestBody Map<String, Object> body, Authentication authentication) {
        if (!isAdmin(authentication)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Admin access required"));
        }
        Long adminId = ((Number) body.get("adminId")).longValue();
        String note = (String) body.get("note");
        try {
            AiAdminCollaborationNote collaborationNote = collaborationService.addNote(id, adminId, note);
            return ResponseEntity.ok(noteToMap(collaborationNote));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get all notes for an incident.
     */
    @GetMapping("/incidents/{id}/notes")
    public ResponseEntity<?> getNotes(@PathVariable Long id, Authentication authentication) {
        if (!isAdmin(authentication)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Admin access required"));
        }
        List<AiAdminCollaborationNote> notes = collaborationService.getNotes(id);
        List<Map<String, Object>> result = notes.stream().map(this::noteToMap).toList();
        return ResponseEntity.ok(result);
    }

    /**
     * Auto-invite collaborators based on expertise.
     */
    @PostMapping("/incidents/{id}/auto-invite")
    public ResponseEntity<?> autoInvite(@PathVariable Long id, Authentication authentication) {
        if (!isAdmin(authentication)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Admin access required"));
        }
        try {
            List<AiAdminIncidentCollaborator> invited = collaborationService.autoInviteCollaborators(id);
            List<Map<String, Object>> result = invited.stream().map(this::collaboratorToMap).toList();
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get collaboration summary for an incident.
     */
    @GetMapping("/incidents/{id}/collaboration-summary")
    public ResponseEntity<?> getCollaborationSummary(@PathVariable Long id, Authentication authentication) {
        if (!isAdmin(authentication)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Admin access required"));
        }
        Map<String, Object> summary = collaborationService.getCollaborationSummary(id);
        return ResponseEntity.ok(summary);
    }

    /**
     * Set the specialization for an AI admin character.
     */
    @PutMapping("/admins/{id}/specialization")
    public ResponseEntity<?> setSpecialization(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, Object> body,
            Authentication authentication) {
        if (!isAdmin(authentication)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Admin access required"));
        }
        try {
            AiCharacter admin = aiCharacterRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("AI admin not found"));
            if (!admin.isAiAdmin()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Not an AI admin character"));
            }
            if (body == null || body.get("specialization") == null) {
                admin.setSpecialization(null);
            } else {
                String spec = ((String) body.get("specialization")).toUpperCase();
                try {
                    admin.setSpecialization(AiAdminIncident.IncidentType.valueOf(spec));
                } catch (IllegalArgumentException e) {
                    return ResponseEntity.badRequest().body(Map.of("error", "Invalid specialization: " + spec));
                }
            }
            admin = aiCharacterRepository.save(admin);
            Map<String, Object> result = new HashMap<>();
            result.put("id", admin.getId());
            result.put("name", admin.getName());
            result.put("specialization", admin.getSpecialization() != null ? admin.getSpecialization().name() : null);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
            }
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get a summary of all AI admins grouped by specialization.
     */
    @GetMapping("/admins/specialization-summary")
    public ResponseEntity<?> getSpecializationSummary(Authentication authentication) {
        if (!isAdmin(authentication)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Admin access required"));
        }
        List<AiCharacter> admins = aiCharacterRepository.findByAiAdminTrueAndActiveTrue();
        Map<String, List<Map<String, Object>>> grouped = new HashMap<>();
        for (AiAdminIncident.IncidentType type : AiAdminIncident.IncidentType.values()) {
            grouped.put(type.name(), new ArrayList<>());
        }
        grouped.put("NONE", new ArrayList<>());
        for (AiCharacter admin : admins) {
            String key = admin.getSpecialization() != null ? admin.getSpecialization().name() : "NONE";
            Map<String, Object> info = new HashMap<>();
            info.put("id", admin.getId());
            info.put("name", admin.getName());
            info.put("activeOrderCount", admin.getActiveOrderCount());
            info.put("totalExperience", admin.getTotalExperience());
            grouped.computeIfAbsent(key, k -> new ArrayList<>()).add(info);
        }
        return ResponseEntity.ok(grouped);
    }

    // ---- Helpers ----

    private boolean isAdmin(Authentication authentication) {
        String username = authentication.getName();
        Optional<Player> playerOpt = playerRepository.findByEmail(username);
        if (playerOpt.isEmpty()) {
            playerOpt = playerRepository.findByEmail(username + "@doramcu.local");
        }
        if (playerOpt.isEmpty()) {
            Optional<User> userOpt = userRepository.findByUsername(username);
            if (userOpt.isPresent()) {
                playerOpt = playerRepository.findByUser(userOpt.get());
            }
        }
        return playerOpt.map(Player::isAdmin).orElse(false);
    }

    private Map<String, Object> incidentToMap(AiAdminIncident incident) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", incident.getId());
        map.put("type", incident.getIncidentType() != null ? incident.getIncidentType().name() : null);
        map.put("severity", incident.getSeverity() != null ? incident.getSeverity().name() : null);
        map.put("priority", incident.getPriority());
        map.put("status", incident.getStatus() != null ? incident.getStatus().name() : null);
        map.put("title", incident.getTitle());
        map.put("description", incident.getDescription());
        map.put("galaxyName", incident.getGalaxyName());
        map.put("assignedAiAdminId", incident.getAssignedAiAdminId());
        map.put("investigatorNotes", incident.getInvestigatorNotes());
        map.put("resolutionAction", incident.getResolutionAction());
        map.put("rootCause", incident.getRootCause());
        map.put("relatedOrderId", incident.getRelatedOrderId());
        map.put("createdAt", incident.getCreatedAt());
        map.put("updatedAt", incident.getUpdatedAt());
        map.put("resolvedAt", incident.getResolvedAt());
        map.put("escalatedAt", incident.getEscalatedAt());
        return map;
    }

    private AiAdminIncident.IncidentType parseIncidentType(String type) {
        if (type == null || type.isBlank()) return AiAdminIncident.IncidentType.HEALTH;
        try {
            return AiAdminIncident.IncidentType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            return AiAdminIncident.IncidentType.HEALTH;
        }
    }

    private AiAdminIncident.Severity parseSeverity(String severity) {
        if (severity == null || severity.isBlank()) return AiAdminIncident.Severity.MEDIUM;
        try {
            return AiAdminIncident.Severity.valueOf(severity.toUpperCase());
        } catch (IllegalArgumentException e) {
            return AiAdminIncident.Severity.MEDIUM;
        }
    }

    private AiAdminIncident.Status parseIncidentStatus(String status) {
        if (status == null || status.isBlank()) return AiAdminIncident.Status.OPEN;
        try {
            return AiAdminIncident.Status.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            return AiAdminIncident.Status.OPEN;
        }
    }

    private Map<String, Object> collaboratorToMap(AiAdminIncidentCollaborator c) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", c.getId());
        map.put("incidentId", c.getIncidentId());
        map.put("aiAdminId", c.getAiAdminId());
        map.put("role", c.getRole() != null ? c.getRole().name() : null);
        map.put("status", c.getStatus() != null ? c.getStatus().name() : null);
        map.put("notes", c.getNotes());
        map.put("joinedAt", c.getJoinedAt());
        map.put("completedAt", c.getCompletedAt());
        return map;
    }

    private Map<String, Object> noteToMap(AiAdminCollaborationNote n) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", n.getId());
        map.put("incidentId", n.getIncidentId());
        map.put("aiAdminId", n.getAiAdminId());
        map.put("note", n.getNote());
        map.put("createdAt", n.getCreatedAt());
        return map;
    }
}
