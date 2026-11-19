package com.yisy.modules.doramcu.admin.controller;

import com.yisy.modules.doramcu.admin.model.AiAdminIncident;
import com.yisy.modules.doramcu.admin.service.*;
import com.yisy.modules.doramcu.model.players.Player;
import com.yisy.modules.doramcu.model.user.User;
import com.yisy.modules.doramcu.repository.ai.AiCharacterRepository;
import com.yisy.modules.doramcu.repository.players.PlayerRepository;
import com.yisy.modules.doramcu.repository.user.UserRepository;
import com.yisy.modules.doramcu.service.ai.AiAdminVisibilityService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AiAdminControllerIncidentTest {

    @Mock
    private AiAdminService aiAdminService;

    @Mock
    private AiAdminDispatchService aiAdminDispatchService;

    @Mock
    private AiAdminNotificationService notificationService;

    @Mock
    private PlayerRepository playerRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private com.yisy.modules.doramcu.admin.repository.AiAdminTestCaseRepository testCaseRepository;

    @Mock
    private TestRegistry testRegistry;

    @Mock
    private AiCharacterRepository aiCharacterRepository;

    @Mock
    private AiAdminLlmService llmService;

    @Mock
    private AiAdminVisibilityService visibilityService;

    @Mock
    private AiAdminOrderProgressService progressService;

    @Mock
    private AiAdminInteractionService interactionService;

    @Mock
    private AiAdminAnalyticsService analyticsService;

    @Mock
    private AiAdminDashboardService dashboardService;

    @Mock
    private AiAdminIncidentService incidentService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AiAdminController controller;

    private AiAdminIncident incident;

    @BeforeEach
    void setUp() {
        incident = new AiAdminIncident();
        incident.setId(1L);
        incident.setIncidentType(AiAdminIncident.IncidentType.HEALTH);
        incident.setSeverity(AiAdminIncident.Severity.CRITICAL);
        incident.setPriority(10);
        incident.setStatus(AiAdminIncident.Status.OPEN);
        incident.setTitle("Low fleet count");
        incident.setDescription("Fleet count below threshold");
        incident.setGalaxyName("Alpha Centauri");
        incident.setCreatedAt(LocalDateTime.now());
        incident.setUpdatedAt(LocalDateTime.now());

        when(authentication.getName()).thenReturn("admin@test.com");
        Player adminPlayer = new Player();
        adminPlayer.setEmail("admin@test.com");
        adminPlayer.setAdmin(true);
        when(playerRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(adminPlayer));
    }

    @Test
    void createIncident_success() {
        when(incidentService.createIncident(any(), any(), anyString(), anyString(), anyString()))
                .thenReturn(incident);

        Map<String, Object> body = new HashMap<>();
        body.put("type", "HEALTH");
        body.put("severity", "CRITICAL");
        body.put("title", "Low fleet count");
        body.put("description", "Fleet count below threshold");
        body.put("galaxyName", "Alpha Centauri");

        ResponseEntity<?> response = controller.createIncident(body, authentication);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        @SuppressWarnings("unchecked")
        Map<String, Object> result = (Map<String, Object>) response.getBody();
        assertEquals("HEALTH", result.get("type"));
        assertEquals("CRITICAL", result.get("severity"));
    }

    @Test
    void createIncident_forbiddenForNonAdmin() {
        when(playerRepository.findByEmail("admin@test.com")).thenReturn(Optional.empty());

        Map<String, Object> body = new HashMap<>();
        body.put("title", "Test");
        body.put("galaxyName", "Galaxy");

        ResponseEntity<?> response = controller.createIncident(body, authentication);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void createIncident_badRequestForMissingTitle() {
        Map<String, Object> body = new HashMap<>();
        body.put("galaxyName", "Alpha Centauri");

        ResponseEntity<?> response = controller.createIncident(body, authentication);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void createIncident_badRequestForMissingGalaxy() {
        Map<String, Object> body = new HashMap<>();
        body.put("title", "Test");

        ResponseEntity<?> response = controller.createIncident(body, authentication);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void listIncidents_returnsOpenIncidents() {
        when(incidentService.getOpenIncidents()).thenReturn(List.of(incident));

        ResponseEntity<?> response = controller.listIncidents(null, null, null, authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> result = (List<Map<String, Object>>) response.getBody();
        assertEquals(1, result.size());
    }

    @Test
    void listIncidents_filteredByGalaxy() {
        when(incidentService.getIncidentsForGalaxy("Alpha Centauri")).thenReturn(List.of(incident));

        ResponseEntity<?> response = controller.listIncidents(null, null, "Alpha Centauri", authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(incidentService).getIncidentsForGalaxy("Alpha Centauri");
    }

    @Test
    void listIncidents_filteredByStatus() {
        when(incidentService.getIncidentsByStatus(AiAdminIncident.Status.RESOLVED))
                .thenReturn(List.of(incident));

        ResponseEntity<?> response = controller.listIncidents("RESOLVED", null, null, authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(incidentService).getIncidentsByStatus(AiAdminIncident.Status.RESOLVED);
    }

    @Test
    void getIncident_notFound() {
        for (AiAdminIncident.Status s : AiAdminIncident.Status.values()) {
            when(incidentService.getIncidentsByStatus(s)).thenReturn(Collections.emptyList());
        }

        ResponseEntity<?> response = controller.getIncident(999L, authentication);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void assignIncident_success() {
        incident.setStatus(AiAdminIncident.Status.INVESTIGATING);
        when(incidentService.assignIncident(1L, 100L)).thenReturn(incident);

        Map<String, Object> body = new HashMap<>();
        body.put("adminId", 100L);

        ResponseEntity<?> response = controller.assignIncident(1L, body, authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        @SuppressWarnings("unchecked")
        Map<String, Object> result = (Map<String, Object>) response.getBody();
        assertEquals("INVESTIGATING", result.get("status"));
    }

    @Test
    void assignIncident_notFound() {
        when(incidentService.assignIncident(999L, 100L))
                .thenThrow(new IllegalArgumentException("Incident not found: 999"));

        Map<String, Object> body = new HashMap<>();
        body.put("adminId", 100L);

        ResponseEntity<?> response = controller.assignIncident(999L, body, authentication);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void resolveIncident_success() {
        incident.setStatus(AiAdminIncident.Status.RESOLVED);
        when(incidentService.resolve(1L, 100L, "Deployed ships", "Insufficient fleet"))
                .thenReturn(incident);

        Map<String, Object> body = new HashMap<>();
        body.put("adminId", 100L);
        body.put("action", "Deployed ships");
        body.put("rootCause", "Insufficient fleet");

        ResponseEntity<?> response = controller.resolveIncident(1L, body, authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        @SuppressWarnings("unchecked")
        Map<String, Object> result = (Map<String, Object>) response.getBody();
        assertEquals("RESOLVED", result.get("status"));
    }

    @Test
    void escalateIncident_success() {
        incident.setStatus(AiAdminIncident.Status.ESCALATED);
        when(incidentService.escalate(1L)).thenReturn(incident);

        ResponseEntity<?> response = controller.escalateIncident(1L, authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        @SuppressWarnings("unchecked")
        Map<String, Object> result = (Map<String, Object>) response.getBody();
        assertEquals("ESCALATED", result.get("status"));
    }

    @Test
    void closeIncident_success() {
        incident.setStatus(AiAdminIncident.Status.CLOSED);
        when(incidentService.close(1L)).thenReturn(incident);

        ResponseEntity<?> response = controller.closeIncident(1L, authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        @SuppressWarnings("unchecked")
        Map<String, Object> result = (Map<String, Object>) response.getBody();
        assertEquals("CLOSED", result.get("status"));
    }

    @Test
    void getIncidentsForGalaxy_returnsList() {
        when(incidentService.getIncidentsForGalaxy("Alpha Centauri")).thenReturn(List.of(incident));

        ResponseEntity<?> response = controller.getIncidentsForGalaxy("Alpha Centauri", authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> result = (List<Map<String, Object>>) response.getBody();
        assertEquals(1, result.size());
    }

    @Test
    void getIncidentDashboard_returnsSummary() {
        Map<String, Object> summary = new HashMap<>();
        summary.put("open", 5L);
        summary.put("critical", 2L);
        when(incidentService.getDashboardSummary()).thenReturn(summary);

        ResponseEntity<?> response = controller.getIncidentDashboard(authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        @SuppressWarnings("unchecked")
        Map<String, Object> result = (Map<String, Object>) response.getBody();
        assertEquals(5L, result.get("open"));
        assertEquals(2L, result.get("critical"));
    }

    @Test
    void investigateIncident_success() {
        incident.setStatus(AiAdminIncident.Status.INVESTIGATING);
        when(incidentService.investigate(1L, 100L, "Notes", true)).thenReturn(incident);

        Map<String, Object> body = new HashMap<>();
        body.put("adminId", 100L);
        body.put("notes", "Notes");
        body.put("useLlm", true);

        ResponseEntity<?> response = controller.investigateIncident(1L, body, authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(incidentService).investigate(1L, 100L, "Notes", true);
    }
}
