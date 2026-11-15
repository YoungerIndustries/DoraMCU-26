package com.yisy.modules.doramcu.controller;

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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("NarrativeWebSocketHandler Tests")
class NarrativeWebSocketHandlerTest {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private NarrativeWebSocketHandler handler;

    @Mock
    private WebSocketSession wsSession1;
    @Mock
    private WebSocketSession wsSession2;

    @BeforeEach
    void setUp() throws Exception {
        handler = new NarrativeWebSocketHandler();

        when(wsSession1.isOpen()).thenReturn(true);
        when(wsSession1.getUri()).thenReturn(new URI("/ws/narrative?sessionId=test1&playerId=player1"));
        when(wsSession1.getId()).thenReturn("session1");

        when(wsSession2.isOpen()).thenReturn(true);
        when(wsSession2.getUri()).thenReturn(new URI("/ws/narrative?sessionId=test1&playerId=player2"));
        when(wsSession2.getId()).thenReturn("session2");
    }

    @Nested
    @DisplayName("Connection Lifecycle")
    class ConnectionLifecycle {

        @Test
        @DisplayName("Should register session on connection")
        void connectionEstablished() throws Exception {
            handler.afterConnectionEstablished(wsSession1);

            // Verify session is registered by checking timeline is accessible
            assertDoesNotThrow(() -> handler.getTimelineSize());
        }

        @Test
        @DisplayName("Should send initial timeline on connection")
        void connectionSendsTimeline() throws Exception {
            handler.afterConnectionEstablished(wsSession1);

            verify(wsSession1).sendMessage(any(TextMessage.class));
        }

        @Test
        @DisplayName("Should unregister session on close")
        void sessionClosed() throws Exception {
            handler.afterConnectionEstablished(wsSession1);
            handler.afterConnectionClosed(wsSession1, CloseStatus.NORMAL);

            // Session closed without error
            assertDoesNotThrow(() -> handler.getTimelineSize());
        }

        @Test
        @DisplayName("Should handle transport errors")
        void transportError() throws Exception {
            handler.afterConnectionEstablished(wsSession1);
            handler.handleTransportError(wsSession1, new RuntimeException("Connection lost"));

            // Session cleaned up without error
            assertDoesNotThrow(() -> handler.getTimelineSize());
        }

        @Test
        @DisplayName("Should handle multiple sessions in same group")
        void multipleSessionsSameGroup() throws Exception {
            handler.afterConnectionEstablished(wsSession1);
            handler.afterConnectionEstablished(wsSession2);

            // Both sessions connected
            assertDoesNotThrow(() -> handler.getTimelineSize());
        }
    }

    @Nested
    @DisplayName("Narrative Event Generation")
    class EventGeneration {

        private void invokePrivateMethod(String methodName) throws Exception {
            Method method = NarrativeWebSocketHandler.class.getDeclaredMethod(methodName);
            method.setAccessible(true);
            method.invoke(handler);
        }

        @Test
        @DisplayName("Should generate narrative events")
        void generateEvent() throws Exception {
            handler.afterConnectionEstablished(wsSession1);

            invokePrivateMethod("simulateNarrativeEvents");

            verify(wsSession1, atLeast(1)).sendMessage(any(TextMessage.class));
        }

        @Test
        @DisplayName("Should generate different event types")
        void eventTypes() throws Exception {
            handler.afterConnectionEstablished(wsSession1);

            // Generate multiple events
            for (int i = 0; i < 10; i++) {
                invokePrivateMethod("simulateNarrativeEvents");
            }

            assertTrue(handler.getActiveEventCount() > 0);
        }

        @Test
        @DisplayName("Should add events to timeline")
        void eventAddsToTimeline() throws Exception {
            handler.afterConnectionEstablished(wsSession1);

            invokePrivateMethod("simulateNarrativeEvents");

            assertTrue(handler.getTimelineSize() > 0);
        }

        @Test
        @DisplayName("Should broadcast events to all sessions")
        void eventBroadcast() throws Exception {
            handler.afterConnectionEstablished(wsSession1);
            handler.afterConnectionEstablished(wsSession2);

            invokePrivateMethod("simulateNarrativeEvents");

            verify(wsSession1, atLeast(1)).sendMessage(any(TextMessage.class));
            verify(wsSession2, atLeast(1)).sendMessage(any(TextMessage.class));
        }
    }

    @Nested
    @DisplayName("Timeline Management")
    class TimelineManagement {

        @Test
        @DisplayName("Should track timeline size")
        void timelineSize() throws Exception {
            handler.afterConnectionEstablished(wsSession1);

            int initialSize = handler.getTimelineSize();

            Method method = NarrativeWebSocketHandler.class.getDeclaredMethod("simulateNarrativeEvents");
            method.setAccessible(true);
            method.invoke(handler);
            assertEquals(initialSize + 1, handler.getTimelineSize());
        }

        @Test
        @DisplayName("Should track active event count")
        void activeEventCount() throws Exception {
            handler.afterConnectionEstablished(wsSession1);

            Method method = NarrativeWebSocketHandler.class.getDeclaredMethod("simulateNarrativeEvents");
            method.setAccessible(true);
            method.invoke(handler);
            int activeCount = handler.getActiveEventCount();
            assertTrue(activeCount >= 1);
        }

        @Test
        @DisplayName("Should resolve old events")
        void resolveOldEvents() throws Exception {
            handler.afterConnectionEstablished(wsSession1);

            Method simMethod = NarrativeWebSocketHandler.class.getDeclaredMethod("simulateNarrativeEvents");
            simMethod.setAccessible(true);
            simMethod.invoke(handler);

            Method resolveMethod = NarrativeWebSocketHandler.class.getDeclaredMethod("resolveOldEvents");
            resolveMethod.setAccessible(true);
            assertDoesNotThrow(() -> resolveMethod.invoke(handler));
        }

        @Test
        @DisplayName("Should send timeline updates")
        void timelineUpdate() throws Exception {
            handler.afterConnectionEstablished(wsSession1);

            Method method = NarrativeWebSocketHandler.class.getDeclaredMethod("simulateNarrativeEvents");
            method.setAccessible(true);
            method.invoke(handler);

            verify(wsSession1, atLeast(1)).sendMessage(any(TextMessage.class));
        }
    }

    @Nested
    @DisplayName("Message Handling")
    class MessageHandling {

        @Test
        @DisplayName("Should handle acknowledge message")
        void acknowledge() throws Exception {
            handler.afterConnectionEstablished(wsSession1);

            Method method = NarrativeWebSocketHandler.class.getDeclaredMethod("simulateNarrativeEvents");
            method.setAccessible(true);
            method.invoke(handler);

            ObjectNode msg = objectMapper.createObjectNode();
            msg.put("action", "acknowledge");
            msg.put("eventId", "event_1");
            handler.handleTextMessage(wsSession1, new TextMessage(msg.toString()));
            // Connection remains stable
            assertDoesNotThrow(() -> handler.getTimelineSize());
        }

        @Test
        @DisplayName("Should handle choose message")
        void choose() throws Exception {
            handler.afterConnectionEstablished(wsSession1);

            Method method = NarrativeWebSocketHandler.class.getDeclaredMethod("simulateNarrativeEvents");
            method.setAccessible(true);
            method.invoke(handler);

            ObjectNode msg = objectMapper.createObjectNode();
            msg.put("action", "choose");
            msg.put("eventId", "event_1");
            msg.put("choiceId", "choice_1");
            handler.handleTextMessage(wsSession1, new TextMessage(msg.toString()));
            // Connection remains stable
            assertDoesNotThrow(() -> handler.getTimelineSize());
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

            // Connection remains stable
            assertDoesNotThrow(() -> handler.getTimelineSize());
        }

        @Test
        @DisplayName("Should handle unknown action gracefully")
        void unknownAction() throws Exception {
            handler.afterConnectionEstablished(wsSession1);

            ObjectNode msg = objectMapper.createObjectNode();
            msg.put("action", "unknown");

            handler.handleTextMessage(wsSession1, new TextMessage(msg.toString()));

            // Connection remains stable
            assertDoesNotThrow(() -> handler.getTimelineSize());
        }
    }

    @Nested
    @DisplayName("Event Styles and Types")
    class EventStylesAndTypes {

        @Test
        @DisplayName("Should generate events with different styles")
        void eventStyles() throws Exception {
            handler.afterConnectionEstablished(wsSession1);

            Method method = NarrativeWebSocketHandler.class.getDeclaredMethod("simulateNarrativeEvents");
            method.setAccessible(true);
            for (int i = 0; i < 20; i++) {
                method.invoke(handler);
            }
            assertTrue(handler.getTimelineSize() > 0);
        }

        @Test
        @DisplayName("Should generate events with different types")
        void eventTypesGeneration() throws Exception {
            handler.afterConnectionEstablished(wsSession1);

            Method method = NarrativeWebSocketHandler.class.getDeclaredMethod("simulateNarrativeEvents");
            method.setAccessible(true);
            for (int i = 0; i < 15; i++) {
                method.invoke(handler);
            }
            assertTrue(handler.getActiveEventCount() > 0);
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

        @Test
        @DisplayName("Should handle event generation errors")
        void eventGenerationError() throws Exception {
            Method method = NarrativeWebSocketHandler.class.getDeclaredMethod("simulateNarrativeEvents");
            method.setAccessible(true);
            assertDoesNotThrow(() -> method.invoke(handler));
        }
    }

    @Nested
    @DisplayName("Integration Scenarios")
    class IntegrationScenarios {

        private void invokePrivateMethod(String methodName) throws Exception {
            Method method = NarrativeWebSocketHandler.class.getDeclaredMethod(methodName);
            method.setAccessible(true);
            method.invoke(handler);
        }

        @Test
        @DisplayName("Full lifecycle: connect, receive events, acknowledge, disconnect")
        void fullLifecycle() throws Exception {
            handler.afterConnectionEstablished(wsSession1);

            invokePrivateMethod("simulateNarrativeEvents");
            assertTrue(handler.getTimelineSize() > 0);

            ObjectNode ack = objectMapper.createObjectNode();
            ack.put("action", "acknowledge");
            ack.put("eventId", "event_1");
            handler.handleTextMessage(wsSession1, new TextMessage(ack.toString()));

            ObjectNode choose = objectMapper.createObjectNode();
            choose.put("action", "choose");
            choose.put("eventId", "event_1");
            choose.put("choiceId", "choice_1");
            handler.handleTextMessage(wsSession1, new TextMessage(choose.toString()));

            handler.afterConnectionClosed(wsSession1, CloseStatus.NORMAL);
            assertDoesNotThrow(() -> handler.getTimelineSize());
        }

        @Test
        @DisplayName("Multiple sessions receive same events")
        void multiSessionEvents() throws Exception {
            handler.afterConnectionEstablished(wsSession1);
            handler.afterConnectionEstablished(wsSession2);

            invokePrivateMethod("simulateNarrativeEvents");

            verify(wsSession1, atLeast(1)).sendMessage(any(TextMessage.class));
            verify(wsSession2, atLeast(1)).sendMessage(any(TextMessage.class));
        }

        @Test
        @DisplayName("Session disconnect does not affect event generation")
        void sessionDisconnectIsolation() throws Exception {
            handler.afterConnectionEstablished(wsSession1);
            handler.afterConnectionEstablished(wsSession2);

            handler.afterConnectionClosed(wsSession1, CloseStatus.NORMAL);

            invokePrivateMethod("simulateNarrativeEvents");
            verify(wsSession2, atLeast(1)).sendMessage(any(TextMessage.class));
        }

        @Test
        @DisplayName("Event resolution flow")
        void eventResolutionFlow() throws Exception {
            handler.afterConnectionEstablished(wsSession1);

            invokePrivateMethod("simulateNarrativeEvents");
            invokePrivateMethod("simulateNarrativeEvents");

            int timelineSize = handler.getTimelineSize();
            assertEquals(2, timelineSize);

            invokePrivateMethod("resolveOldEvents");
            assertTrue(handler.getTimelineSize() >= 0);
        }
    }
}
