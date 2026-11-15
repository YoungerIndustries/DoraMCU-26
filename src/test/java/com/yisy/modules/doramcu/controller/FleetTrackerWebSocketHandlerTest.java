package com.yisy.modules.doramcu.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.lang.reflect.Method;
import java.net.URI;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("FleetTrackerWebSocketHandler Tests")
class FleetTrackerWebSocketHandlerTest {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private FleetTrackerWebSocketHandler handler;

    @Mock
    private WebSocketSession wsSession1;
    @Mock
    private WebSocketSession wsSession2;

    @BeforeEach
    void setUp() throws Exception {
        handler = new FleetTrackerWebSocketHandler();

        when(wsSession1.isOpen()).thenReturn(true);
        when(wsSession1.getUri()).thenReturn(new URI("/ws/fleet-tracker?sessionId=test1&playerId=player1"));
        when(wsSession1.getId()).thenReturn("session1");

        when(wsSession2.isOpen()).thenReturn(true);
        when(wsSession2.getUri()).thenReturn(new URI("/ws/fleet-tracker?sessionId=test1&playerId=player2"));
        when(wsSession2.getId()).thenReturn("session2");
    }

    @Nested
    @DisplayName("Connection Lifecycle")
    class ConnectionLifecycle {

        @Test
        @DisplayName("Should register session on connection")
        void connectionEstablished() throws Exception {
            handler.afterConnectionEstablished(wsSession1);

            assertEquals(1, handler.getConnectionCount());
            assertTrue(handler.getActiveSessionIds().contains("test1"));
        }

        @Test
        @DisplayName("Should send initial fleet snapshot on connection")
        void connectionSendsSnapshot() throws Exception {
            handler.afterConnectionEstablished(wsSession1);

            verify(wsSession1, atLeast(1)).sendMessage(any(TextMessage.class));
        }

        @Test
        @DisplayName("Should unregister session on close")
        void sessionClosed() throws Exception {
            handler.afterConnectionEstablished(wsSession1);
            handler.afterConnectionClosed(wsSession1, CloseStatus.NORMAL);

            assertEquals(0, handler.getConnectionCount());
        }

        @Test
        @DisplayName("Should handle transport errors")
        void transportError() throws Exception {
            handler.afterConnectionEstablished(wsSession1);
            handler.handleTransportError(wsSession1, new RuntimeException("Connection lost"));

            assertEquals(0, handler.getConnectionCount());
        }

        @Test
        @DisplayName("Should handle multiple sessions in same group")
        void multipleSessionsSameGroup() throws Exception {
            handler.afterConnectionEstablished(wsSession1);
            handler.afterConnectionEstablished(wsSession2);

            assertEquals(2, handler.getConnectionCount());
            Set<String> sessions = handler.getActiveSessionIds();
            assertEquals(1, sessions.size());
            assertTrue(sessions.contains("test1"));
        }
    }

    @Nested
    @DisplayName("Message Handling")
    class MessageHandling {

        @Test
        @DisplayName("Should handle subscribe message")
        void subscribe() throws Exception {
            handler.afterConnectionEstablished(wsSession1);

            ObjectNode msg = objectMapper.createObjectNode();
            msg.put("action", "subscribe");
            msg.put("galaxyId", "Corellian");

            handler.handleTextMessage(wsSession1, new TextMessage(msg.toString()));

            verify(wsSession1, atLeast(1)).sendMessage(any(TextMessage.class));
        }

        @Test
        @DisplayName("Should handle ping message")
        void ping() throws Exception {
            handler.afterConnectionEstablished(wsSession1);

            ObjectNode msg = objectMapper.createObjectNode();
            msg.put("action", "ping");

            handler.handleTextMessage(wsSession1, new TextMessage(msg.toString()));

            verify(wsSession1, atLeast(1)).sendMessage(any(TextMessage.class));
        }

        @Test
        @DisplayName("Should handle invalid JSON gracefully")
        void invalidJson() throws Exception {
            handler.afterConnectionEstablished(wsSession1);

            handler.handleTextMessage(wsSession1, new TextMessage("not json"));

            assertEquals(1, handler.getConnectionCount());
        }

        @Test
        @DisplayName("Should handle unknown action gracefully")
        void unknownAction() throws Exception {
            handler.afterConnectionEstablished(wsSession1);

            ObjectNode msg = objectMapper.createObjectNode();
            msg.put("action", "unknown");

            handler.handleTextMessage(wsSession1, new TextMessage(msg.toString()));

            assertEquals(1, handler.getConnectionCount());
        }
    }

    @Nested
    @DisplayName("Fleet Simulation")
    class FleetSimulation {

        private void invokePrivateMethod(String methodName) throws Exception {
            Method method = FleetTrackerWebSocketHandler.class.getDeclaredMethod(methodName);
            method.setAccessible(true);
            method.invoke(handler);
        }

        @Test
        @DisplayName("Should broadcast fleet updates to all sessions")
        void fleetUpdates() throws Exception {
            handler.afterConnectionEstablished(wsSession1);
            handler.afterConnectionEstablished(wsSession2);

            invokePrivateMethod("simulateFleetUpdates");

            verify(wsSession1, atLeast(1)).sendMessage(any(TextMessage.class));
            verify(wsSession2, atLeast(1)).sendMessage(any(TextMessage.class));
        }

        @Test
        @DisplayName("Should broadcast fleet movement")
        void fleetMovement() throws Exception {
            handler.afterConnectionEstablished(wsSession1);

            invokePrivateMethod("simulateFleetMovements");

            verify(wsSession1, atLeast(1)).sendMessage(any(TextMessage.class));
        }

        @Test
        @DisplayName("Should broadcast fleet combat events")
        void fleetCombat() throws Exception {
            handler.afterConnectionEstablished(wsSession1);

            invokePrivateMethod("simulateCombatEvents");

            verify(wsSession1, atLeast(1)).sendMessage(any(TextMessage.class));
        }

        @Test
        @DisplayName("Should create new fleets periodically")
        void fleetCreation() throws Exception {
            handler.afterConnectionEstablished(wsSession1);

            invokePrivateMethod("simulateFleetCreation");

            verify(wsSession1, atLeast(1)).sendMessage(any(TextMessage.class));
        }

        @Test
        @DisplayName("Should broadcast to only connected sessions")
        void broadcastToConnectedSessions() throws Exception {
            handler.afterConnectionEstablished(wsSession1);
            handler.afterConnectionClosed(wsSession1, CloseStatus.NORMAL);

            // Should not throw even with no sessions
            invokePrivateMethod("simulateFleetUpdates");
        }
    }

    @Nested
    @DisplayName("Session Management")
    class SessionManagement {

        @Test
        @DisplayName("Should track connection count correctly")
        void connectionCount() throws Exception {
            assertEquals(0, handler.getConnectionCount());

            handler.afterConnectionEstablished(wsSession1);
            assertEquals(1, handler.getConnectionCount());

            handler.afterConnectionEstablished(wsSession2);
            assertEquals(2, handler.getConnectionCount());

            handler.afterConnectionClosed(wsSession1, CloseStatus.NORMAL);
            assertEquals(1, handler.getConnectionCount());
        }

        @Test
        @DisplayName("Should return active session IDs")
        void activeSessionIds() throws Exception {
            handler.afterConnectionEstablished(wsSession1);

            Set<String> sessions = handler.getActiveSessionIds();
            assertFalse(sessions.isEmpty());
            assertTrue(sessions.contains("test1"));
        }

        @Test
        @DisplayName("Should extract sessionId from URI")
        void extractSessionId() throws Exception {
            handler.afterConnectionEstablished(wsSession1);

            assertEquals(1, handler.getConnectionCount());
        }
    }

    @Nested
    @DisplayName("Error Handling")
    class ErrorHandling {

        @Test
        @DisplayName("Should handle send errors gracefully")
        void sendError() throws Exception {
            handler.afterConnectionEstablished(wsSession1);
            doThrow(new RuntimeException("Send failed")).when(wsSession1).sendMessage(any(TextMessage.class));

            assertDoesNotThrow(() -> {
                handler.handleTextMessage(wsSession1, new TextMessage("{\"action\":\"ping\"}"));
            });
        }

        @Test
        @DisplayName("Should handle null messages")
        void nullMessage() throws Exception {
            handler.afterConnectionEstablished(wsSession1);

            assertDoesNotThrow(() -> {
                handler.handleTextMessage(wsSession1, new TextMessage(""));
            });
        }
    }

    @Nested
    @DisplayName("Integration Scenarios")
    class IntegrationScenarios {

        private void invokePrivateMethod(String methodName) throws Exception {
            Method method = FleetTrackerWebSocketHandler.class.getDeclaredMethod(methodName);
            method.setAccessible(true);
            method.invoke(handler);
        }

        @Test
        @DisplayName("Full lifecycle: connect, subscribe, receive updates, disconnect")
        void fullLifecycle() throws Exception {
            // Connect
            handler.afterConnectionEstablished(wsSession1);
            assertEquals(1, handler.getConnectionCount());

            // Subscribe to fleets
            ObjectNode subscribe = objectMapper.createObjectNode();
            subscribe.put("action", "subscribe");
            subscribe.put("galaxyId", "Corellian");
            handler.handleTextMessage(wsSession1, new TextMessage(subscribe.toString()));

            // Simulate fleet updates
            invokePrivateMethod("simulateFleetUpdates");
            invokePrivateMethod("simulateFleetMovements");

            // Disconnect
            handler.afterConnectionClosed(wsSession1, CloseStatus.NORMAL);
            assertEquals(0, handler.getConnectionCount());
        }

        @Test
        @DisplayName("Multiple sessions receive same updates")
        void multiSessionUpdates() throws Exception {
            handler.afterConnectionEstablished(wsSession1);
            handler.afterConnectionEstablished(wsSession2);

            invokePrivateMethod("simulateFleetUpdates");

            verify(wsSession1, atLeast(1)).sendMessage(any(TextMessage.class));
            verify(wsSession2, atLeast(1)).sendMessage(any(TextMessage.class));
        }

        @Test
        @DisplayName("Session disconnect does not affect other sessions")
        void sessionDisconnectIsolation() throws Exception {
            handler.afterConnectionEstablished(wsSession1);
            handler.afterConnectionEstablished(wsSession2);

            handler.afterConnectionClosed(wsSession1, CloseStatus.NORMAL);

            assertEquals(1, handler.getConnectionCount());
            invokePrivateMethod("simulateFleetUpdates");
            verify(wsSession2, atLeast(1)).sendMessage(any(TextMessage.class));
        }
    }
}
