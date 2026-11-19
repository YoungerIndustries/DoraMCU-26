package com.yisy.modules.doramcu.admin.service;

import com.yisy.modules.doramcu.admin.model.AiAdminIncident;
import com.yisy.modules.doramcu.admin.model.AiAdminNotification;
import com.yisy.modules.doramcu.admin.model.AiAdminNotificationType;
import com.yisy.modules.doramcu.admin.model.AiAdminOrder;
import com.yisy.modules.doramcu.admin.repository.AiAdminIncidentRepository;
import com.yisy.modules.doramcu.admin.repository.AiAdminNotificationRepository;
import com.yisy.modules.doramcu.eve.model.AiCharacter;
import com.yisy.modules.doramcu.repository.ai.AiCharacterRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AiAdminIncidentServiceTest {

    @Mock
    private AiAdminIncidentRepository incidentRepository;

    @Mock
    private AiAdminNotificationRepository notificationRepository;

    @Mock
    private AiCharacterRepository characterRepository;

    @Mock
    private AiAdminLlmService llmService;

    @Mock
    private AiAdminNotificationService notificationService;

    @InjectMocks
    private AiAdminIncidentService incidentService;

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
        incident.setCreatedAt(LocalDateTime.now().minusHours(2));
        incident.setUpdatedAt(LocalDateTime.now().minusHours(2));
    }

    @Test
    void createIncident_createsWithCorrectFields() {
        when(incidentRepository.save(any(AiAdminIncident.class))).thenAnswer(inv -> inv.getArgument(0));

        AiAdminIncident result = incidentService.createIncident(
                AiAdminIncident.IncidentType.HEALTH,
                AiAdminIncident.Severity.CRITICAL,
                "Low fleet count",
                "Fleet count below threshold",
                "Alpha Centauri");

        assertNotNull(result.getId());
        assertEquals(AiAdminIncident.IncidentType.HEALTH, result.getIncidentType());
        assertEquals(AiAdminIncident.Severity.CRITICAL, result.getSeverity());
        assertEquals(10, result.getPriority());
        assertEquals(AiAdminIncident.Status.OPEN, result.getStatus());
        assertEquals("Low fleet count", result.getTitle());
        assertEquals("Alpha Centauri", result.getGalaxyName());
        verify(incidentRepository).save(any(AiAdminIncident.class));
    }

    @Test
    void createIncident_withRelatedOrderId() {
        when(incidentRepository.save(any(AiAdminIncident.class))).thenAnswer(inv -> inv.getArgument(0));

        AiAdminIncident result = incidentService.createIncident(
                AiAdminIncident.IncidentType.SYSTEM,
                AiAdminIncident.Severity.HIGH,
                "System overload",
                "CPU usage critical",
                "Beta Pictoris",
                42L);

        assertEquals(42L, result.getRelatedOrderId());
    }

    @Test
    void createIncident_priorityBySeverity() {
        when(incidentRepository.save(any(AiAdminIncident.class))).thenAnswer(inv -> inv.getArgument(0));

        AiAdminIncident critical = incidentService.createIncident(
                AiAdminIncident.IncidentType.HEALTH, AiAdminIncident.Severity.CRITICAL,
                "Title", "Desc", "Galaxy");
        assertEquals(10, critical.getPriority());

        AiAdminIncident high = incidentService.createIncident(
                AiAdminIncident.IncidentType.HEALTH, AiAdminIncident.Severity.HIGH,
                "Title", "Desc", "Galaxy");
        assertEquals(7, high.getPriority());

        AiAdminIncident medium = incidentService.createIncident(
                AiAdminIncident.IncidentType.HEALTH, AiAdminIncident.Severity.MEDIUM,
                "Title", "Desc", "Galaxy");
        assertEquals(5, medium.getPriority());

        AiAdminIncident low = incidentService.createIncident(
                AiAdminIncident.IncidentType.HEALTH, AiAdminIncident.Severity.LOW,
                "Title", "Desc", "Galaxy");
        assertEquals(3, low.getPriority());
    }

    @Test
    void assignIncident_assignsAndNotifies() {
        when(incidentRepository.findById(1L)).thenReturn(Optional.of(incident));
        when(incidentRepository.save(any(AiAdminIncident.class))).thenAnswer(inv -> inv.getArgument(0));
        when(notificationRepository.save(any(AiAdminNotification.class))).thenAnswer(inv -> inv.getArgument(0));

        AiAdminIncident result = incidentService.assignIncident(1L, 100L);

        assertEquals(100L, result.getAssignedAiAdminId());
        assertEquals(AiAdminIncident.Status.INVESTIGATING, result.getStatus());
        verify(notificationRepository).save(any(AiAdminNotification.class));
    }

    @Test
    void assignIncident_throwsWhenClosed() {
        incident.setStatus(AiAdminIncident.Status.CLOSED);
        when(incidentRepository.findById(1L)).thenReturn(Optional.of(incident));

        assertThrows(IllegalStateException.class, () -> incidentService.assignIncident(1L, 100L));
    }

    @Test
    void assignIncident_throwsWhenNotFound() {
        when(incidentRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> incidentService.assignIncident(999L, 100L));
    }

    @Test
    void investigate_withoutLlm_setsNotes() {
        when(incidentRepository.findById(1L)).thenReturn(Optional.of(incident));
        when(incidentRepository.save(any(AiAdminIncident.class))).thenAnswer(inv -> inv.getArgument(0));

        AiAdminIncident result = incidentService.investigate(1L, 100L, "Manual investigation notes", false);

        assertEquals("Manual investigation notes", result.getInvestigatorNotes());
        assertEquals(AiAdminIncident.Status.INVESTIGATING, result.getStatus());
    }

    @Test
    void investigate_withLlm_appendsLlmNotes() {
        when(incidentRepository.findById(1L)).thenReturn(Optional.of(incident));
        when(incidentRepository.save(any(AiAdminIncident.class))).thenAnswer(inv -> inv.getArgument(0));
        when(characterRepository.findById(100L)).thenReturn(Optional.of(new AiCharacter()));
        when(llmService.analyzeOrder(any(AiCharacter.class), any(AiAdminOrder.class)))
                .thenReturn(new AiAdminLlmService.LlmAnalysisResult(
                        "DEPLOY", "Fleet is low, deploy ships", new HashMap<>(), "", true, null));

        AiAdminIncident result = incidentService.investigate(1L, 100L, "Manual notes", true);

        assertNotNull(result.getInvestigatorNotes());
        assertTrue(result.getInvestigatorNotes().contains("Manual notes"));
        assertTrue(result.getInvestigatorNotes().contains("[LLM Analysis]"));
    }

    @Test
    void investigate_throwsWhenClosed() {
        incident.setStatus(AiAdminIncident.Status.CLOSED);
        when(incidentRepository.findById(1L)).thenReturn(Optional.of(incident));

        assertThrows(IllegalStateException.class, () ->
                incidentService.investigate(1L, 100L, "Notes", false));
    }

    @Test
    void resolve_setsResolutionAndTimestamp() {
        when(incidentRepository.findById(1L)).thenReturn(Optional.of(incident));
        when(incidentRepository.save(any(AiAdminIncident.class))).thenAnswer(inv -> inv.getArgument(0));
        when(notificationRepository.save(any(AiAdminNotification.class))).thenAnswer(inv -> inv.getArgument(0));

        AiAdminIncident result = incidentService.resolve(1L, 100L, "Deployed ships", "Insufficient fleet");

        assertEquals(AiAdminIncident.Status.RESOLVED, result.getStatus());
        assertEquals("Deployed ships", result.getResolutionAction());
        assertEquals("Insufficient fleet", result.getRootCause());
        assertNotNull(result.getResolvedAt());
        verify(notificationRepository).save(any(AiAdminNotification.class));
    }

    @Test
    void resolve_throwsWhenClosed() {
        incident.setStatus(AiAdminIncident.Status.CLOSED);
        when(incidentRepository.findById(1L)).thenReturn(Optional.of(incident));

        assertThrows(IllegalStateException.class, () ->
                incidentService.resolve(1L, 100L, "Action", "Cause"));
    }

    @Test
    void escalate_setsEscalatedStatus() {
        when(incidentRepository.findById(1L)).thenReturn(Optional.of(incident));
        when(incidentRepository.save(any(AiAdminIncident.class))).thenAnswer(inv -> inv.getArgument(0));

        AiAdminIncident result = incidentService.escalate(1L);

        assertEquals(AiAdminIncident.Status.ESCALATED, result.getStatus());
        assertNotNull(result.getEscalatedAt());
    }

    @Test
    void escalate_throwsWhenClosed() {
        incident.setStatus(AiAdminIncident.Status.CLOSED);
        when(incidentRepository.findById(1L)).thenReturn(Optional.of(incident));

        assertThrows(IllegalStateException.class, () -> incidentService.escalate(1L));
    }

    @Test
    void close_setsClosedStatus() {
        when(incidentRepository.findById(1L)).thenReturn(Optional.of(incident));
        when(incidentRepository.save(any(AiAdminIncident.class))).thenAnswer(inv -> inv.getArgument(0));

        AiAdminIncident result = incidentService.close(1L);

        assertEquals(AiAdminIncident.Status.CLOSED, result.getStatus());
    }

    @Test
    void getIncidentsForGalaxy_returnsList() {
        List<AiAdminIncident> incidents = List.of(incident);
        when(incidentRepository.findByGalaxyNameOrderByCreatedAtDesc("Alpha Centauri")).thenReturn(incidents);

        List<AiAdminIncident> result = incidentService.getIncidentsForGalaxy("Alpha Centauri");

        assertEquals(1, result.size());
        assertEquals("Alpha Centauri", result.get(0).getGalaxyName());
    }

    @Test
    void getIncidentsForGalaxy_emptyForNull() {
        List<AiAdminIncident> result = incidentService.getIncidentsForGalaxy(null);
        assertTrue(result.isEmpty());
    }

    @Test
    void getIncidentsForGalaxy_emptyForBlank() {
        List<AiAdminIncident> result = incidentService.getIncidentsForGalaxy("  ");
        assertTrue(result.isEmpty());
    }

    @Test
    void getOpenIncidents_returnsOpenList() {
        List<AiAdminIncident> openIncidents = List.of(incident);
        when(incidentRepository.findOpenIncidents(AiAdminIncident.Status.OPEN)).thenReturn(openIncidents);

        List<AiAdminIncident> result = incidentService.getOpenIncidents();

        assertEquals(1, result.size());
    }

    @Test
    void getIncidentsByStatus_returnsFiltered() {
        List<AiAdminIncident> resolved = List.of(incident);
        when(incidentRepository.findByStatusIn(List.of(AiAdminIncident.Status.RESOLVED))).thenReturn(resolved);

        List<AiAdminIncident> result = incidentService.getIncidentsByStatus(AiAdminIncident.Status.RESOLVED);

        assertEquals(1, result.size());
    }

    @Test
    void getIncidentsForAdmin_returnsAssigned() {
        incident.setAssignedAiAdminId(100L);
        List<AiAdminIncident> assigned = List.of(incident);
        when(incidentRepository.findByAssignedAiAdminIdOrderByCreatedAtDesc(100L)).thenReturn(assigned);

        List<AiAdminIncident> result = incidentService.getIncidentsForAdmin(100L);

        assertEquals(1, result.size());
        assertEquals(100L, result.get(0).getAssignedAiAdminId());
    }

    @Test
    void getDashboardSummary_returnsCounts() {
        when(incidentRepository.countByStatus(AiAdminIncident.Status.OPEN)).thenReturn(5L);
        when(incidentRepository.countByStatus(AiAdminIncident.Status.INVESTIGATING)).thenReturn(2L);
        when(incidentRepository.countByStatus(AiAdminIncident.Status.RESOLVED)).thenReturn(10L);
        when(incidentRepository.countByStatus(AiAdminIncident.Status.ESCALATED)).thenReturn(1L);
        when(incidentRepository.countByStatus(AiAdminIncident.Status.CLOSED)).thenReturn(3L);
        when(incidentRepository.countBySeverity(AiAdminIncident.Severity.CRITICAL)).thenReturn(2L);
        when(incidentRepository.countBySeverity(AiAdminIncident.Severity.HIGH)).thenReturn(4L);
        when(incidentRepository.countBySeverity(AiAdminIncident.Severity.MEDIUM)).thenReturn(6L);
        when(incidentRepository.countBySeverity(AiAdminIncident.Severity.LOW)).thenReturn(9L);

        Map<String, Object> summary = incidentService.getDashboardSummary();

        assertEquals(5L, summary.get("open"));
        assertEquals(2L, summary.get("investigating"));
        assertEquals(10L, summary.get("resolved"));
        assertEquals(1L, summary.get("escalated"));
        assertEquals(3L, summary.get("closed"));
        assertEquals(2L, summary.get("critical"));
        assertEquals(4L, summary.get("high"));
    }

    @Test
    void escalateStaleIncidents_escalatesOldOpen() {
        incident.setCreatedAt(LocalDateTime.now().minusHours(2));
        incident.setStatus(AiAdminIncident.Status.OPEN);
        List<AiAdminIncident> staleOpen = List.of(incident);

        when(incidentRepository.findByStatusAndCreatedAtBefore(
                eq(AiAdminIncident.Status.OPEN), any(LocalDateTime.class)))
                .thenReturn(staleOpen);
        when(incidentRepository.findByStatusAndCreatedAtBeforeAndEscalatedAtIsNull(
                eq(AiAdminIncident.Status.INVESTIGATING), any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());
        when(incidentRepository.save(any(AiAdminIncident.class))).thenAnswer(inv -> inv.getArgument(0));

        incidentService.escalateStaleIncidents();

        verify(incidentRepository).save(incident);
        assertEquals(AiAdminIncident.Status.ESCALATED, incident.getStatus());
    }

    @Test
    void escalateStaleIncidents_escalatesOldInvestigating() {
        incident.setCreatedAt(LocalDateTime.now().minusHours(3));
        incident.setStatus(AiAdminIncident.Status.INVESTIGATING);
        List<AiAdminIncident> staleInvestigating = List.of(incident);

        when(incidentRepository.findByStatusAndCreatedAtBefore(
                eq(AiAdminIncident.Status.OPEN), any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());
        when(incidentRepository.findByStatusAndCreatedAtBeforeAndEscalatedAtIsNull(
                eq(AiAdminIncident.Status.INVESTIGATING), any(LocalDateTime.class)))
                .thenReturn(staleInvestigating);
        when(incidentRepository.save(any(AiAdminIncident.class))).thenAnswer(inv -> inv.getArgument(0));

        incidentService.escalateStaleIncidents();

        verify(incidentRepository).save(incident);
    }

    @Test
    void escalateStaleIncidents_noStaleIncidents() {
        when(incidentRepository.findByStatusAndCreatedAtBefore(
                eq(AiAdminIncident.Status.OPEN), any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());
        when(incidentRepository.findByStatusAndCreatedAtBeforeAndEscalatedAtIsNull(
                eq(AiAdminIncident.Status.INVESTIGATING), any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());

        incidentService.escalateStaleIncidents();

        verify(incidentRepository, never()).save(any(AiAdminIncident.class));
    }

    @Test
    void investigate_llmFailure_gracefulFallback() {
        when(incidentRepository.findById(1L)).thenReturn(Optional.of(incident));
        when(incidentRepository.save(any(AiAdminIncident.class))).thenAnswer(inv -> inv.getArgument(0));
        when(characterRepository.findById(100L)).thenReturn(Optional.of(new AiCharacter()));
        when(llmService.analyzeOrder(any(AiCharacter.class), any(AiAdminOrder.class)))
                .thenThrow(new RuntimeException("LLM timeout"));

        AiAdminIncident result = incidentService.investigate(1L, 100L, "Fallback test", true);

        assertNotNull(result.getInvestigatorNotes());
        assertTrue(result.getInvestigatorNotes().contains("Fallback test"));
        assertTrue(result.getInvestigatorNotes().contains("LLM unavailable"));
    }
}
