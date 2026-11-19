package com.yisy.modules.doramcu.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.yisy.modules.doramcu.admin.model.AiAdminCollaborationNote;
import com.yisy.modules.doramcu.admin.model.AiAdminIncidentCollaborator;
import com.yisy.modules.doramcu.admin.service.AiAdminCollaborationService;
import com.yisy.modules.doramcu.controller.misc.CollaborationWebSocketHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("CollaborationWebSocketHandler Tests")
class CollaborationWebSocketHandlerTest {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private CollaborationWebSocketHandler handler;

    @Mock
    private WebSocketSession wsSession1;
    @Mock
    private WebSocketSession wsSession2;
    @Mock
    private WebSocketSession wsSession3;
    @Mock
    private AiAdminCollaborationService collaborationService;

    @BeforeEach
    void setUp() throws Exception {
        handler = new CollaborationWebSocketHandler(collaborationService);

        when(wsSession1.getId()).thenReturn("ws1");
        when(wsSession1.getUri()).thenReturn(new URI("ws://localhost/ws/collaboration"));
        when(wsSession1.isOpen()).thenReturn(true);
        doAnswer(invocation -> null).when(wsSession1).sendMessage(any(TextMessage.class));

        when(wsSession2.getId()).thenReturn("ws2");
        when(wsSession2.getUri()).thenReturn(new URI("ws://localhost/ws/collaboration"));
        when(wsSession2.isOpen()).thenReturn(true);
        doAnswer(invocation -> null).when(wsSession2).sendMessage(any(TextMessage.class));

        when(wsSession3.getId()).thenReturn("ws3");
        when(wsSession3.getUri()).thenReturn(new URI("ws://localhost/ws/collaboration"));
        when(wsSession3.isOpen()).thenReturn(true);
        doAnswer(invocation -> null).when(wsSession3).sendMessage(any(TextMessage.class));

        // Default: empty collaborators and notes
        when(collaborationService.getCollaborators(anyLong())).thenReturn(new ArrayList<>());
        when(collaborationService.getNotes(anyLong())).thenReturn(new ArrayList<>());
    }

    // ------------------------------------------------------------------
    // Connection lifecycle tests
    // ------------------------------------------------------------------

    @Test
    @DisplayName("Connection established should increment session count")
    void connectionEstablished() throws Exception {
        handler.afterConnectionEstablished(wsSession1);
        assertEquals(1, handler.getConnectedSessions());
    }

    @Test
    @DisplayName("Multiple connections should increment session count")
    void multipleConnectionsEstablished() throws Exception {
        handler.afterConnectionEstablished(wsSession1);
        handler.afterConnectionEstablished(wsSession2);
        handler.afterConnectionEstablished(wsSession3);
        assertEquals(3, handler.getConnectedSessions());
    }

    @Test
    @DisplayName("Connection closed should decrement session count")
    void connectionClosed() throws Exception {
        handler.afterConnectionEstablished(wsSession1);
        handler.afterConnectionClosed(wsSession1, CloseStatus.NORMAL);
        assertEquals(0, handler.getConnectedSessions());
    }

    @Test
    @DisplayName("Disconnect should clean up all subscriptions")
    void disconnectCleansUpSubscriptions() throws Exception {
        handler.afterConnectionEstablished(wsSession1);

        // Subscribe to incident 100
        ObjectNode subscribe = objectMapper.createObjectNode();
        subscribe.put("type", "subscribe");
        subscribe.put("incidentId", 100L);
        handler.handleTextMessage(wsSession1, new TextMessage(subscribe.toString()));

        assertEquals(1, handler.getMonitoredIncidents());

        // Disconnect
        handler.afterConnectionClosed(wsSession1, CloseStatus.NORMAL);
        assertEquals(0, handler.getMonitoredIncidents());
        assertEquals(0, handler.getConnectedSessions());
    }

    // ------------------------------------------------------------------
    // Subscribe tests
    // ------------------------------------------------------------------

    @Test
    @DisplayName("Subscribe to incident should send snapshot")
    void subscribeShouldSendSnapshot() throws Exception {
        handler.afterConnectionEstablished(wsSession1);

        ObjectNode subscribe = objectMapper.createObjectNode();
        subscribe.put("type", "subscribe");
        subscribe.put("incidentId", 100L);
        handler.handleTextMessage(wsSession1, new TextMessage(subscribe.toString()));

        verify(wsSession1).sendMessage(any(TextMessage.class));
        assertEquals(1, handler.getMonitoredIncidents());
    }

    @Test
    @DisplayName("Subscribe without incidentId should send error")
    void subscribeWithoutIncidentIdShouldSendError() throws Exception {
        handler.afterConnectionEstablished(wsSession1);

        ObjectNode subscribe = objectMapper.createObjectNode();
        subscribe.put("type", "subscribe");
        handler.handleTextMessage(wsSession1, new TextMessage(subscribe.toString()));

        verify(wsSession1).sendMessage(any(TextMessage.class));
    }

    @Test
    @DisplayName("Multiple sessions can subscribe to the same incident")
    void multipleSessionsSubscribeToSameIncident() throws Exception {
        handler.afterConnectionEstablished(wsSession1);
        handler.afterConnectionEstablished(wsSession2);

        ObjectNode subscribe1 = objectMapper.createObjectNode();
        subscribe1.put("type", "subscribe");
        subscribe1.put("incidentId", 100L);
        handler.handleTextMessage(wsSession1, new TextMessage(subscribe1.toString()));

        ObjectNode subscribe2 = objectMapper.createObjectNode();
        subscribe2.put("type", "subscribe");
        subscribe2.put("incidentId", 100L);
        handler.handleTextMessage(wsSession2, new TextMessage(subscribe2.toString()));

        assertEquals(1, handler.getMonitoredIncidents());
    }

    @Test
    @DisplayName("Session can subscribe to multiple incidents")
    void sessionCanSubscribeToMultipleIncidents() throws Exception {
        handler.afterConnectionEstablished(wsSession1);

        ObjectNode subscribe1 = objectMapper.createObjectNode();
        subscribe1.put("type", "subscribe");
        subscribe1.put("incidentId", 100L);
        handler.handleTextMessage(wsSession1, new TextMessage(subscribe1.toString()));

        ObjectNode subscribe2 = objectMapper.createObjectNode();
        subscribe2.put("type", "subscribe");
        subscribe2.put("incidentId", 101L);
        handler.handleTextMessage(wsSession1, new TextMessage(subscribe2.toString()));

        assertEquals(2, handler.getMonitoredIncidents());
    }

    @Test
    @DisplayName("Snapshot should include collaborators and notes")
    void snapshotShouldIncludeCollaboratorsAndNotes() throws Exception {
        AiAdminIncidentCollaborator collab = new AiAdminIncidentCollaborator();
        collab.setId(1L);
        collab.setAiAdminId(10L);
        collab.setRole(AiAdminIncidentCollaborator.Role.LEAD);
        collab.setStatus(AiAdminIncidentCollaborator.Status.ACTIVE);
        collab.setJoinedAt(LocalDateTime.now());

        AiAdminCollaborationNote note = new AiAdminCollaborationNote();
        note.setId(1L);
        note.setAiAdminId(10L);
        note.setNote("Investigation started");
        note.setCreatedAt(LocalDateTime.now());

        when(collaborationService.getCollaborators(100L)).thenReturn(List.of(collab));
        when(collaborationService.getNotes(100L)).thenReturn(List.of(note));

        handler.afterConnectionEstablished(wsSession1);

        ObjectNode subscribe = objectMapper.createObjectNode();
        subscribe.put("type", "subscribe");
        subscribe.put("incidentId", 100L);
        handler.handleTextMessage(wsSession1, new TextMessage(subscribe.toString()));

        verify(wsSession1).sendMessage(any(TextMessage.class));
    }

    // ------------------------------------------------------------------
    // Unsubscribe tests
    // ------------------------------------------------------------------

    @Test
    @DisplayName("Unsubscribe should remove session from incident")
    void unsubscribeShouldRemoveSession() throws Exception {
        handler.afterConnectionEstablished(wsSession1);

        // Subscribe
        ObjectNode subscribe = objectMapper.createObjectNode();
        subscribe.put("type", "subscribe");
        subscribe.put("incidentId", 100L);
        handler.handleTextMessage(wsSession1, new TextMessage(subscribe.toString()));
        assertEquals(1, handler.getMonitoredIncidents());

        // Unsubscribe
        ObjectNode unsubscribe = objectMapper.createObjectNode();
        unsubscribe.put("type", "unsubscribe");
        unsubscribe.put("incidentId", 100L);
        handler.handleTextMessage(wsSession1, new TextMessage(unsubscribe.toString()));
        assertEquals(0, handler.getMonitoredIncidents());
    }

    @Test
    @DisplayName("Unsubscribe without incidentId should send error")
    void unsubscribeWithoutIncidentIdShouldSendError() throws Exception {
        handler.afterConnectionEstablished(wsSession1);

        ObjectNode unsubscribe = objectMapper.createObjectNode();
        unsubscribe.put("type", "unsubscribe");
        handler.handleTextMessage(wsSession1, new TextMessage(unsubscribe.toString()));

        verify(wsSession1).sendMessage(any(TextMessage.class));
    }

    @Test
    @DisplayName("Unsubscribe should not remove other sessions")
    void unsubscribeShouldNotRemoveOtherSessions() throws Exception {
        handler.afterConnectionEstablished(wsSession1);
        handler.afterConnectionEstablished(wsSession2);

        // Both subscribe
        ObjectNode subscribe1 = objectMapper.createObjectNode();
        subscribe1.put("type", "subscribe");
        subscribe1.put("incidentId", 100L);
        handler.handleTextMessage(wsSession1, new TextMessage(subscribe1.toString()));

        ObjectNode subscribe2 = objectMapper.createObjectNode();
        subscribe2.put("type", "subscribe");
        subscribe2.put("incidentId", 100L);
        handler.handleTextMessage(wsSession2, new TextMessage(subscribe2.toString()));

        // Session 1 unsubscribes
        ObjectNode unsubscribe = objectMapper.createObjectNode();
        unsubscribe.put("type", "unsubscribe");
        unsubscribe.put("incidentId", 100L);
        handler.handleTextMessage(wsSession1, new TextMessage(unsubscribe.toString()));

        // Incident still monitored because session 2 is still subscribed
        assertEquals(1, handler.getMonitoredIncidents());
    }

    // ------------------------------------------------------------------
    // Broadcast tests
    // ------------------------------------------------------------------

    @Test
    @DisplayName("Broadcast should send to all subscribers of an incident")
    void broadcastShouldSendToAllSubscribers() throws Exception {
        handler.afterConnectionEstablished(wsSession1);
        handler.afterConnectionEstablished(wsSession2);
        handler.afterConnectionEstablished(wsSession3);

        // Session 1 and 2 subscribe to incident 100
        ObjectNode subscribe1 = objectMapper.createObjectNode();
        subscribe1.put("type", "subscribe");
        subscribe1.put("incidentId", 100L);
        handler.handleTextMessage(wsSession1, new TextMessage(subscribe1.toString()));

        ObjectNode subscribe2 = objectMapper.createObjectNode();
        subscribe2.put("type", "subscribe");
        subscribe2.put("incidentId", 100L);
        handler.handleTextMessage(wsSession2, new TextMessage(subscribe2.toString()));

        // Session 3 subscribes to incident 200
        ObjectNode subscribe3 = objectMapper.createObjectNode();
        subscribe3.put("type", "subscribe");
        subscribe3.put("incidentId", 200L);
        handler.handleTextMessage(wsSession3, new TextMessage(subscribe3.toString()));

        // Broadcast event for incident 100
        Map<String, Object> data = new HashMap<>();
        data.put("aiAdminId", 10L);
        data.put("role", "INVESTIGATOR");
        handler.broadcastCollaborationEvent(100L, "collaborator_joined", data);

        // Only session 1 and 2 should receive the update
        verify(wsSession1, atLeast(2)).sendMessage(any(TextMessage.class));
        verify(wsSession2, atLeast(2)).sendMessage(any(TextMessage.class));
        verify(wsSession3, times(1)).sendMessage(any(TextMessage.class));
    }

    @Test
    @DisplayName("Broadcast to unsubscribed incident should not send")
    void broadcastToUnsubscribedIncidentShouldNotSend() throws Exception {
        handler.afterConnectionEstablished(wsSession1);

        // No one subscribed to incident 999
        Map<String, Object> data = new HashMap<>();
        data.put("aiAdminId", 10L);
        handler.broadcastCollaborationEvent(999L, "collaborator_joined", data);

        verify(wsSession1, never()).sendMessage(any(TextMessage.class));
    }

    @Test
    @DisplayName("Broadcast with null data should not crash")
    void broadcastWithNullDataShouldNotCrash() throws Exception {
        handler.afterConnectionEstablished(wsSession1);

        ObjectNode subscribe = objectMapper.createObjectNode();
        subscribe.put("type", "subscribe");
        subscribe.put("incidentId", 100L);
        handler.handleTextMessage(wsSession1, new TextMessage(subscribe.toString()));

        // Should not throw
        handler.broadcastCollaborationEvent(100L, "collaborator_joined", null);
        verify(wsSession1, atLeast(2)).sendMessage(any(TextMessage.class));
    }

    @Test
    @DisplayName("Broadcast collaborator_joined event")
    void broadcastCollaboratorJoined() throws Exception {
        handler.afterConnectionEstablished(wsSession1);

        ObjectNode subscribe = objectMapper.createObjectNode();
        subscribe.put("type", "subscribe");
        subscribe.put("incidentId", 100L);
        handler.handleTextMessage(wsSession1, new TextMessage(subscribe.toString()));

        Map<String, Object> data = new HashMap<>();
        data.put("aiAdminId", 10L);
        data.put("role", "INVESTIGATOR");
        handler.broadcastCollaborationEvent(100L, "collaborator_joined", data);

        verify(wsSession1, atLeast(2)).sendMessage(any(TextMessage.class));
    }

    @Test
    @DisplayName("Broadcast note_added event")
    void broadcastNoteAdded() throws Exception {
        handler.afterConnectionEstablished(wsSession1);

        ObjectNode subscribe = objectMapper.createObjectNode();
        subscribe.put("type", "subscribe");
        subscribe.put("incidentId", 100L);
        handler.handleTextMessage(wsSession1, new TextMessage(subscribe.toString()));

        Map<String, Object> data = new HashMap<>();
        data.put("aiAdminId", 10L);
        data.put("note", "Found the root cause");
        handler.broadcastCollaborationEvent(100L, "note_added", data);

        verify(wsSession1, atLeast(2)).sendMessage(any(TextMessage.class));
    }

    @Test
    @DisplayName("Broadcast status_changed event")
    void broadcastStatusChanged() throws Exception {
        handler.afterConnectionEstablished(wsSession1);

        ObjectNode subscribe = objectMapper.createObjectNode();
        subscribe.put("type", "subscribe");
        subscribe.put("incidentId", 100L);
        handler.handleTextMessage(wsSession1, new TextMessage(subscribe.toString()));

        Map<String, Object> data = new HashMap<>();
        data.put("aiAdminId", 10L);
        data.put("newStatus", "COMPLETED");
        handler.broadcastCollaborationEvent(100L, "status_changed", data);

        verify(wsSession1, atLeast(2)).sendMessage(any(TextMessage.class));
    }

    @Test
    @DisplayName("Broadcast auto_invited event")
    void broadcastAutoInvited() throws Exception {
        handler.afterConnectionEstablished(wsSession1);

        ObjectNode subscribe = objectMapper.createObjectNode();
        subscribe.put("type", "subscribe");
        subscribe.put("incidentId", 100L);
        handler.handleTextMessage(wsSession1, new TextMessage(subscribe.toString()));

        Map<String, Object> data = new HashMap<>();
        data.put("invitedCount", 2);
        handler.broadcastCollaborationEvent(100L, "auto_invited", data);

        verify(wsSession1, atLeast(2)).sendMessage(any(TextMessage.class));
    }

    // ------------------------------------------------------------------
    // Ping/pong tests
    // ------------------------------------------------------------------

    @Test
    @DisplayName("Ping should return pong")
    void pingShouldReturnPong() throws Exception {
        handler.afterConnectionEstablished(wsSession1);

        ObjectNode ping = objectMapper.createObjectNode();
        ping.put("type", "ping");
        handler.handleTextMessage(wsSession1, new TextMessage(ping.toString()));

        verify(wsSession1).sendMessage(any(TextMessage.class));
    }

    // ------------------------------------------------------------------
    // Error handling tests
    // ------------------------------------------------------------------

    @Test
    @DisplayName("Unknown message type should send error")
    void unknownMessageTypeShouldSendError() throws Exception {
        handler.afterConnectionEstablished(wsSession1);

        ObjectNode invalid = objectMapper.createObjectNode();
        invalid.put("type", "unknown_type");
        handler.handleTextMessage(wsSession1, new TextMessage(invalid.toString()));

        verify(wsSession1).sendMessage(any(TextMessage.class));
    }

    @Test
    @DisplayName("Malformed JSON should not crash")
    void malformedJsonShouldNotCrash() throws Exception {
        handler.afterConnectionEstablished(wsSession1);

        handler.handleTextMessage(wsSession1, new TextMessage("{invalid json}"));
        assertEquals(1, handler.getConnectedSessions());
    }

    @Test
    @DisplayName("Closed session should not receive broadcast")
    void closedSessionShouldNotReceiveBroadcast() throws Exception {
        handler.afterConnectionEstablished(wsSession1);
        when(wsSession1.isOpen()).thenReturn(false);

        ObjectNode subscribe = objectMapper.createObjectNode();
        subscribe.put("type", "subscribe");
        subscribe.put("incidentId", 100L);
        handler.handleTextMessage(wsSession1, new TextMessage(subscribe.toString()));

        Map<String, Object> data = new HashMap<>();
        data.put("aiAdminId", 10L);
        handler.broadcastCollaborationEvent(100L, "collaborator_joined", data);

        // Session is closed, so no additional messages after subscribe
        verify(wsSession1, times(1)).sendMessage(any(TextMessage.class));
    }

    // ------------------------------------------------------------------
    // Monitored incidents count tests
    // ------------------------------------------------------------------

    @Test
    @DisplayName("Monitored incidents should count unique incidents")
    void monitoredIncidentsShouldCountUniqueIncidents() throws Exception {
        handler.afterConnectionEstablished(wsSession1);
        handler.afterConnectionEstablished(wsSession2);

        // Both subscribe to the same incident
        ObjectNode subscribe1 = objectMapper.createObjectNode();
        subscribe1.put("type", "subscribe");
        subscribe1.put("incidentId", 100L);
        handler.handleTextMessage(wsSession1, new TextMessage(subscribe1.toString()));

        ObjectNode subscribe2 = objectMapper.createObjectNode();
        subscribe2.put("type", "subscribe");
        subscribe2.put("incidentId", 100L);
        handler.handleTextMessage(wsSession2, new TextMessage(subscribe2.toString()));

        assertEquals(1, handler.getMonitoredIncidents());
    }

    @Test
    @DisplayName("Snapshot on non-existent incident should handle gracefully")
    void snapshotOnNonExistentIncident() throws Exception {
        when(collaborationService.getCollaborators(999L)).thenThrow(new IllegalArgumentException("Incident not found"));

        handler.afterConnectionEstablished(wsSession1);

        ObjectNode subscribe = objectMapper.createObjectNode();
        subscribe.put("type", "subscribe");
        subscribe.put("incidentId", 999L);
        handler.handleTextMessage(wsSession1, new TextMessage(subscribe.toString()));

        // Should still send a snapshot with empty arrays, not crash
        verify(wsSession1).sendMessage(any(TextMessage.class));
    }
}
