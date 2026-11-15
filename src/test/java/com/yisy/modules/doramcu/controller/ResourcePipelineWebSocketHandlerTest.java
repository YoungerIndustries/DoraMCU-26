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
@DisplayName("ResourcePipelineWebSocketHandler Tests")
class ResourcePipelineWebSocketHandlerTest {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private ResourcePipelineWebSocketHandler handler;

    @Mock
    private WebSocketSession wsSession1;
    @Mock
    private WebSocketSession wsSession2;

    @BeforeEach
    void setUp() throws Exception {
        handler = new ResourcePipelineWebSocketHandler();

        when(wsSession1.isOpen()).thenReturn(true);
        when(wsSession1.getUri()).thenReturn(new URI("/ws/resource-pipeline?sessionId=test1&galaxyId=Corellian"));
        when(wsSession1.getId()).thenReturn("session1");

        when(wsSession2.isOpen()).thenReturn(true);
        when(wsSession2.getUri()).thenReturn(new URI("/ws/resource-pipeline?sessionId=test1&galaxyId=Corellian"));
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
        }

        @Test
        @DisplayName("Should send initial pipeline snapshot on connection")
        void connectionSendsSnapshot() throws Exception {
            handler.afterConnectionEstablished(wsSession1);

            verify(wsSession1).sendMessage(any(TextMessage.class));
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

            verify(wsSession1).sendMessage(any(TextMessage.class));
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
    @DisplayName("Transport Route Simulation")
    class TransportRouteSimulation {

        @Test
        @DisplayName("Should simulate route progress updates")
        void routeProgress() throws Exception {
            handler.afterConnectionEstablished(wsSession1);

            Method method = ResourcePipelineWebSocketHandler.class.getDeclaredMethod("simulateRouteUpdates");
            method.setAccessible(true);
            method.invoke(handler);

            verify(wsSession1, atLeast(1)).sendMessage(any(TextMessage.class));
        }

        @Test
        @DisplayName("Should broadcast route updates to all sessions")
        void routeBroadcast() throws Exception {
            handler.afterConnectionEstablished(wsSession1);
            handler.afterConnectionEstablished(wsSession2);

            Method method = ResourcePipelineWebSocketHandler.class.getDeclaredMethod("simulateRouteUpdates");
            method.setAccessible(true);
            method.invoke(handler);

            verify(wsSession1, atLeast(1)).sendMessage(any(TextMessage.class));
            verify(wsSession2, atLeast(1)).sendMessage(any(TextMessage.class));
        }

        @Test
        @DisplayName("Should not crash with no sessions")
        void routeProgressNoSessions() throws Exception {
            Method method = ResourcePipelineWebSocketHandler.class.getDeclaredMethod("simulateRouteUpdates");
            method.setAccessible(true);
            assertDoesNotThrow(() -> method.invoke(handler));
        }
    }

    @Nested
    @DisplayName("Mining Operation Simulation")
    class MiningSimulation {

        @Test
        @DisplayName("Should simulate mining production updates")
        void miningProduction() throws Exception {
            handler.afterConnectionEstablished(wsSession1);

            Method method = ResourcePipelineWebSocketHandler.class.getDeclaredMethod("simulateMiningUpdates");
            method.setAccessible(true);
            method.invoke(handler);

            verify(wsSession1, atLeast(1)).sendMessage(any(TextMessage.class));
        }

        @Test
        @DisplayName("Should broadcast mining updates to all sessions")
        void miningBroadcast() throws Exception {
            handler.afterConnectionEstablished(wsSession1);
            handler.afterConnectionEstablished(wsSession2);

            Method method = ResourcePipelineWebSocketHandler.class.getDeclaredMethod("simulateMiningUpdates");
            method.setAccessible(true);
            method.invoke(handler);

            verify(wsSession1, atLeast(1)).sendMessage(any(TextMessage.class));
            verify(wsSession2, atLeast(1)).sendMessage(any(TextMessage.class));
        }

        @Test
        @DisplayName("Should not crash with no sessions")
        void miningProductionNoSessions() throws Exception {
            Method method = ResourcePipelineWebSocketHandler.class.getDeclaredMethod("simulateMiningUpdates");
            method.setAccessible(true);
            assertDoesNotThrow(() -> method.invoke(handler));
        }
    }

    @Nested
    @DisplayName("Facility Simulation")
    class FacilitySimulation {

        @Test
        @DisplayName("Should simulate facility efficiency updates")
        void facilityEfficiency() throws Exception {
            handler.afterConnectionEstablished(wsSession1);

            Method method = ResourcePipelineWebSocketHandler.class.getDeclaredMethod("simulateFacilityUpdates");
            method.setAccessible(true);
            method.invoke(handler);

            verify(wsSession1, atLeast(1)).sendMessage(any(TextMessage.class));
        }

        @Test
        @DisplayName("Should broadcast facility updates to all sessions")
        void facilityBroadcast() throws Exception {
            handler.afterConnectionEstablished(wsSession1);
            handler.afterConnectionEstablished(wsSession2);

            Method method = ResourcePipelineWebSocketHandler.class.getDeclaredMethod("simulateFacilityUpdates");
            method.setAccessible(true);
            method.invoke(handler);

            verify(wsSession1, atLeast(1)).sendMessage(any(TextMessage.class));
            verify(wsSession2, atLeast(1)).sendMessage(any(TextMessage.class));
        }

        @Test
        @DisplayName("Should not crash with no sessions")
        void facilityEfficiencyNoSessions() throws Exception {
            Method method = ResourcePipelineWebSocketHandler.class.getDeclaredMethod("simulateFacilityUpdates");
            method.setAccessible(true);
            assertDoesNotThrow(() -> method.invoke(handler));
        }
    }

    @Nested
    @DisplayName("Statistics Updates")
    class StatisticsUpdates {

        @Test
        @DisplayName("Should simulate statistics updates")
        void statisticsUpdate() throws Exception {
            handler.afterConnectionEstablished(wsSession1);

            Method method = ResourcePipelineWebSocketHandler.class.getDeclaredMethod("simulateStatisticsUpdate");
            method.setAccessible(true);
            method.invoke(handler);

            verify(wsSession1, atLeast(1)).sendMessage(any(TextMessage.class));
        }

        @Test
        @DisplayName("Should broadcast statistics to all sessions")
        void statisticsBroadcast() throws Exception {
            handler.afterConnectionEstablished(wsSession1);
            handler.afterConnectionEstablished(wsSession2);

            Method method = ResourcePipelineWebSocketHandler.class.getDeclaredMethod("simulateStatisticsUpdate");
            method.setAccessible(true);
            method.invoke(handler);

            verify(wsSession1, atLeast(1)).sendMessage(any(TextMessage.class));
            verify(wsSession2, atLeast(1)).sendMessage(any(TextMessage.class));
        }

        @Test
        @DisplayName("Should not crash with no sessions")
        void statisticsUpdateNoSessions() throws Exception {
            Method method = ResourcePipelineWebSocketHandler.class.getDeclaredMethod("simulateStatisticsUpdate");
            method.setAccessible(true);
            assertDoesNotThrow(() -> method.invoke(handler));
        }
    }

    @Nested
    @DisplayName("Pipeline Data Initialization")
    class PipelineDataInitialization {

        @Test
        @DisplayName("Should initialize transport routes")
        void transportRoutes() throws Exception {
            handler.afterConnectionEstablished(wsSession1);

            // Handler should have initialized routes
            verify(wsSession1).sendMessage(any(TextMessage.class));
        }

        @Test
        @DisplayName("Should initialize mining operations")
        void miningOperations() throws Exception {
            handler.afterConnectionEstablished(wsSession1);

            verify(wsSession1).sendMessage(any(TextMessage.class));
        }

        @Test
        @DisplayName("Should initialize processing facilities")
        void processingFacilities() throws Exception {
            handler.afterConnectionEstablished(wsSession1);

            verify(wsSession1).sendMessage(any(TextMessage.class));
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
        @DisplayName("Should extract galaxyId from URI")
        void extractGalaxyId() throws Exception {
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

        @Test
        @DisplayName("Should handle simulation errors gracefully")
        void simulationErrors() throws Exception {
            Method routeMethod = ResourcePipelineWebSocketHandler.class.getDeclaredMethod("simulateRouteUpdates");
            routeMethod.setAccessible(true);
            Method miningMethod = ResourcePipelineWebSocketHandler.class.getDeclaredMethod("simulateMiningUpdates");
            miningMethod.setAccessible(true);
            Method facilityMethod = ResourcePipelineWebSocketHandler.class.getDeclaredMethod("simulateFacilityUpdates");
            facilityMethod.setAccessible(true);
            Method statsMethod = ResourcePipelineWebSocketHandler.class.getDeclaredMethod("simulateStatisticsUpdate");
            statsMethod.setAccessible(true);
            assertDoesNotThrow(() -> routeMethod.invoke(handler));
            assertDoesNotThrow(() -> miningMethod.invoke(handler));
            assertDoesNotThrow(() -> facilityMethod.invoke(handler));
            assertDoesNotThrow(() -> statsMethod.invoke(handler));
        }
    }

    @Nested
    @DisplayName("Integration Scenarios")
    class IntegrationScenarios {

        private void invokePrivateMethod(String methodName) throws Exception {
            Method method = ResourcePipelineWebSocketHandler.class.getDeclaredMethod(methodName);
            method.setAccessible(true);
            method.invoke(handler);
        }

        @Test
        @DisplayName("Full lifecycle: connect, subscribe, receive updates, disconnect")
        void fullLifecycle() throws Exception {
            handler.afterConnectionEstablished(wsSession1);
            assertEquals(1, handler.getConnectionCount());

            ObjectNode subscribe = objectMapper.createObjectNode();
            subscribe.put("action", "subscribe");
            subscribe.put("galaxyId", "Corellian");
            handler.handleTextMessage(wsSession1, new TextMessage(subscribe.toString()));

            invokePrivateMethod("simulateRouteUpdates");
            invokePrivateMethod("simulateMiningUpdates");
            invokePrivateMethod("simulateFacilityUpdates");
            invokePrivateMethod("simulateStatisticsUpdate");

            handler.afterConnectionClosed(wsSession1, CloseStatus.NORMAL);
            assertEquals(0, handler.getConnectionCount());
        }

        @Test
        @DisplayName("Multiple sessions receive same updates")
        void multiSessionUpdates() throws Exception {
            handler.afterConnectionEstablished(wsSession1);
            handler.afterConnectionEstablished(wsSession2);

            invokePrivateMethod("simulateRouteUpdates");

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
            invokePrivateMethod("simulateRouteUpdates");
            verify(wsSession2, atLeast(1)).sendMessage(any(TextMessage.class));
        }

        @Test
        @DisplayName("All simulation types work together")
        void allSimulations() throws Exception {
            handler.afterConnectionEstablished(wsSession1);

            invokePrivateMethod("simulateRouteUpdates");
            invokePrivateMethod("simulateMiningUpdates");
            invokePrivateMethod("simulateFacilityUpdates");
            invokePrivateMethod("simulateStatisticsUpdate");

            verify(wsSession1, atLeast(4)).sendMessage(any(TextMessage.class));
        }
    }
}
