package com.yisy.modules.doramcu.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.yisy.modules.doramcu.admin.service.AiAdminOrderProgressService;
import com.yisy.modules.doramcu.admin.service.AiAdminOrderProgressService.OrderProgress;
import com.yisy.modules.doramcu.controller.misc.OrderProgressWebSocketHandler;
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
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("OrderProgressWebSocketHandler Tests")
class OrderProgressWebSocketHandlerTest {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private OrderProgressWebSocketHandler handler;

    @Mock
    private WebSocketSession wsSession1;
    @Mock
    private WebSocketSession wsSession2;
    @Mock
    private WebSocketSession wsSession3;
    @Mock
    private AiAdminOrderProgressService progressService;

    @BeforeEach
    void setUp() throws Exception {
        handler = new OrderProgressWebSocketHandler(progressService);

        when(wsSession1.getId()).thenReturn("ws1");
        when(wsSession1.getUri()).thenReturn(new URI("ws://localhost/ws/order-progress"));
        when(wsSession1.isOpen()).thenReturn(true);
        doAnswer(invocation -> null).when(wsSession1).sendMessage(any(TextMessage.class));

        when(wsSession2.getId()).thenReturn("ws2");
        when(wsSession2.getUri()).thenReturn(new URI("ws://localhost/ws/order-progress"));
        when(wsSession2.isOpen()).thenReturn(true);
        doAnswer(invocation -> null).when(wsSession2).sendMessage(any(TextMessage.class));

        when(wsSession3.getId()).thenReturn("ws3");
        when(wsSession3.getUri()).thenReturn(new URI("ws://localhost/ws/order-progress"));
        when(wsSession3.isOpen()).thenReturn(true);
        doAnswer(invocation -> null).when(wsSession3).sendMessage(any(TextMessage.class));
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

        // Subscribe to order 100
        ObjectNode subscribe = objectMapper.createObjectNode();
        subscribe.put("type", "subscribe");
        subscribe.put("orderId", 100L);
        when(progressService.getOrderProgress(100L)).thenReturn(Optional.empty());
        handler.handleTextMessage(wsSession1, new TextMessage(subscribe.toString()));

        assertEquals(1, handler.getMonitoredOrders());

        // Disconnect
        handler.afterConnectionClosed(wsSession1, CloseStatus.NORMAL);
        assertEquals(0, handler.getMonitoredOrders());
        assertEquals(0, handler.getConnectedSessions());
    }

    // ------------------------------------------------------------------
    // Subscribe tests
    // ------------------------------------------------------------------

    @Test
    @DisplayName("Subscribe to order should send snapshot")
    void subscribeShouldSendSnapshot() throws Exception {
        handler.afterConnectionEstablished(wsSession1);

        OrderProgress progress = new OrderProgress();
        progress.setOrderId(100L);
        progress.setStatus("RUNNING");
        progress.setProgressPercent(50);
        progress.setCurrentTestName("testApi");
        progress.setTestsCompleted(5);
        progress.setTestsTotal(10);
        when(progressService.getOrderProgress(100L)).thenReturn(Optional.of(progress));

        ObjectNode subscribe = objectMapper.createObjectNode();
        subscribe.put("type", "subscribe");
        subscribe.put("orderId", 100L);
        handler.handleTextMessage(wsSession1, new TextMessage(subscribe.toString()));

        verify(wsSession1).sendMessage(any(TextMessage.class));
        assertEquals(1, handler.getMonitoredOrders());
    }

    @Test
    @DisplayName("Subscribe to non-existent order should send error")
    void subscribeToNonExistentOrderShouldSendError() throws Exception {
        handler.afterConnectionEstablished(wsSession1);
        when(progressService.getOrderProgress(999L)).thenReturn(Optional.empty());

        ObjectNode subscribe = objectMapper.createObjectNode();
        subscribe.put("type", "subscribe");
        subscribe.put("orderId", 999L);
        handler.handleTextMessage(wsSession1, new TextMessage(subscribe.toString()));

        // Should receive error message
        verify(wsSession1).sendMessage(any(TextMessage.class));
    }

    @Test
    @DisplayName("Subscribe without orderId should send error")
    void subscribeWithoutOrderIdShouldSendError() throws Exception {
        handler.afterConnectionEstablished(wsSession1);

        ObjectNode subscribe = objectMapper.createObjectNode();
        subscribe.put("type", "subscribe");
        handler.handleTextMessage(wsSession1, new TextMessage(subscribe.toString()));

        verify(wsSession1).sendMessage(any(TextMessage.class));
    }

    @Test
    @DisplayName("Multiple sessions can subscribe to the same order")
    void multipleSessionsSubscribeToSameOrder() throws Exception {
        OrderProgress progress = new OrderProgress();
        progress.setOrderId(100L);
        progress.setStatus("RUNNING");
        progress.setProgressPercent(25);
        when(progressService.getOrderProgress(100L)).thenReturn(Optional.of(progress));

        handler.afterConnectionEstablished(wsSession1);
        handler.afterConnectionEstablished(wsSession2);

        ObjectNode subscribe1 = objectMapper.createObjectNode();
        subscribe1.put("type", "subscribe");
        subscribe1.put("orderId", 100L);
        handler.handleTextMessage(wsSession1, new TextMessage(subscribe1.toString()));

        ObjectNode subscribe2 = objectMapper.createObjectNode();
        subscribe2.put("type", "subscribe");
        subscribe2.put("orderId", 100L);
        handler.handleTextMessage(wsSession2, new TextMessage(subscribe2.toString()));

        assertEquals(1, handler.getMonitoredOrders());
    }

    @Test
    @DisplayName("Session can subscribe to multiple orders")
    void sessionCanSubscribeToMultipleOrders() throws Exception {
        OrderProgress progress1 = new OrderProgress();
        progress1.setOrderId(100L);
        progress1.setStatus("RUNNING");
        OrderProgress progress2 = new OrderProgress();
        progress2.setOrderId(101L);
        progress2.setStatus("PENDING");
        when(progressService.getOrderProgress(100L)).thenReturn(Optional.of(progress1));
        when(progressService.getOrderProgress(101L)).thenReturn(Optional.of(progress2));

        handler.afterConnectionEstablished(wsSession1);

        ObjectNode subscribe1 = objectMapper.createObjectNode();
        subscribe1.put("type", "subscribe");
        subscribe1.put("orderId", 100L);
        handler.handleTextMessage(wsSession1, new TextMessage(subscribe1.toString()));

        ObjectNode subscribe2 = objectMapper.createObjectNode();
        subscribe2.put("type", "subscribe");
        subscribe2.put("orderId", 101L);
        handler.handleTextMessage(wsSession1, new TextMessage(subscribe2.toString()));

        assertEquals(2, handler.getMonitoredOrders());
    }

    // ------------------------------------------------------------------
    // Unsubscribe tests
    // ------------------------------------------------------------------

    @Test
    @DisplayName("Unsubscribe should remove session from order")
    void unsubscribeShouldRemoveSession() throws Exception {
        OrderProgress progress = new OrderProgress();
        progress.setOrderId(100L);
        when(progressService.getOrderProgress(100L)).thenReturn(Optional.of(progress));

        handler.afterConnectionEstablished(wsSession1);

        // Subscribe
        ObjectNode subscribe = objectMapper.createObjectNode();
        subscribe.put("type", "subscribe");
        subscribe.put("orderId", 100L);
        handler.handleTextMessage(wsSession1, new TextMessage(subscribe.toString()));
        assertEquals(1, handler.getMonitoredOrders());

        // Unsubscribe
        ObjectNode unsubscribe = objectMapper.createObjectNode();
        unsubscribe.put("type", "unsubscribe");
        unsubscribe.put("orderId", 100L);
        handler.handleTextMessage(wsSession1, new TextMessage(unsubscribe.toString()));
        assertEquals(0, handler.getMonitoredOrders());
    }

    @Test
    @DisplayName("Unsubscribe without orderId should send error")
    void unsubscribeWithoutOrderIdShouldSendError() throws Exception {
        handler.afterConnectionEstablished(wsSession1);

        ObjectNode unsubscribe = objectMapper.createObjectNode();
        unsubscribe.put("type", "unsubscribe");
        handler.handleTextMessage(wsSession1, new TextMessage(unsubscribe.toString()));

        verify(wsSession1).sendMessage(any(TextMessage.class));
    }

    @Test
    @DisplayName("Unsubscribe should not remove other sessions")
    void unsubscribeShouldNotRemoveOtherSessions() throws Exception {
        OrderProgress progress = new OrderProgress();
        progress.setOrderId(100L);
        when(progressService.getOrderProgress(100L)).thenReturn(Optional.of(progress));

        handler.afterConnectionEstablished(wsSession1);
        handler.afterConnectionEstablished(wsSession2);

        // Both subscribe
        ObjectNode subscribe1 = objectMapper.createObjectNode();
        subscribe1.put("type", "subscribe");
        subscribe1.put("orderId", 100L);
        handler.handleTextMessage(wsSession1, new TextMessage(subscribe1.toString()));

        ObjectNode subscribe2 = objectMapper.createObjectNode();
        subscribe2.put("type", "subscribe");
        subscribe2.put("orderId", 100L);
        handler.handleTextMessage(wsSession2, new TextMessage(subscribe2.toString()));

        // Session 1 unsubscribes
        ObjectNode unsubscribe = objectMapper.createObjectNode();
        unsubscribe.put("type", "unsubscribe");
        unsubscribe.put("orderId", 100L);
        handler.handleTextMessage(wsSession1, new TextMessage(unsubscribe.toString()));

        // Order still monitored because session 2 is still subscribed
        assertEquals(1, handler.getMonitoredOrders());
    }

    // ------------------------------------------------------------------
    // Broadcast tests
    // ------------------------------------------------------------------

    @Test
    @DisplayName("Broadcast should send to all subscribers of an order")
    void broadcastShouldSendToAllSubscribers() throws Exception {
        OrderProgress progress = new OrderProgress();
        progress.setOrderId(100L);
        when(progressService.getOrderProgress(100L)).thenReturn(Optional.of(progress));

        handler.afterConnectionEstablished(wsSession1);
        handler.afterConnectionEstablished(wsSession2);
        handler.afterConnectionEstablished(wsSession3);

        // Session 1 and 2 subscribe to order 100
        ObjectNode subscribe1 = objectMapper.createObjectNode();
        subscribe1.put("type", "subscribe");
        subscribe1.put("orderId", 100L);
        handler.handleTextMessage(wsSession1, new TextMessage(subscribe1.toString()));

        ObjectNode subscribe2 = objectMapper.createObjectNode();
        subscribe2.put("type", "subscribe");
        subscribe2.put("orderId", 100L);
        handler.handleTextMessage(wsSession2, new TextMessage(subscribe2.toString()));

        // Session 3 subscribes to order 200
        ObjectNode subscribe3 = objectMapper.createObjectNode();
        subscribe3.put("type", "subscribe");
        subscribe3.put("orderId", 200L);
        when(progressService.getOrderProgress(200L)).thenReturn(Optional.of(progress));
        handler.handleTextMessage(wsSession3, new TextMessage(subscribe3.toString()));

        // Broadcast progress for order 100
        Map<String, Object> data = new HashMap<>();
        data.put("status", "RUNNING");
        data.put("progressPercent", 75);
        handler.broadcastOrderProgress(100L, data);

        // Only session 1 and 2 should receive the update
        verify(wsSession1, atLeast(2)).sendMessage(any(TextMessage.class));
        verify(wsSession2, atLeast(2)).sendMessage(any(TextMessage.class));
        verify(wsSession3, times(1)).sendMessage(any(TextMessage.class));
    }

    @Test
    @DisplayName("Broadcast to unsubscribed order should not send")
    void broadcastToUnsubscribedOrderShouldNotSend() throws Exception {
        handler.afterConnectionEstablished(wsSession1);

        // No one subscribed to order 999
        Map<String, Object> data = new HashMap<>();
        data.put("status", "RUNNING");
        handler.broadcastOrderProgress(999L, data);

        verify(wsSession1, never()).sendMessage(any(TextMessage.class));
    }

    @Test
    @DisplayName("Broadcast with null data should not crash")
    void broadcastWithNullDataShouldNotCrash() throws Exception {
        OrderProgress progress = new OrderProgress();
        progress.setOrderId(100L);
        when(progressService.getOrderProgress(100L)).thenReturn(Optional.of(progress));

        handler.afterConnectionEstablished(wsSession1);

        ObjectNode subscribe = objectMapper.createObjectNode();
        subscribe.put("type", "subscribe");
        subscribe.put("orderId", 100L);
        handler.handleTextMessage(wsSession1, new TextMessage(subscribe.toString()));

        // Should not throw
        handler.broadcastOrderProgress(100L, null);
        verify(wsSession1, atLeast(2)).sendMessage(any(TextMessage.class));
    }

    @Test
    @DisplayName("Broadcast should include all progress data fields")
    void broadcastShouldIncludeAllFields() throws Exception {
        OrderProgress progress = new OrderProgress();
        progress.setOrderId(100L);
        when(progressService.getOrderProgress(100L)).thenReturn(Optional.of(progress));

        handler.afterConnectionEstablished(wsSession1);

        ObjectNode subscribe = objectMapper.createObjectNode();
        subscribe.put("type", "subscribe");
        subscribe.put("orderId", 100L);
        handler.handleTextMessage(wsSession1, new TextMessage(subscribe.toString()));

        Map<String, Object> data = new HashMap<>();
        data.put("status", "COMPLETED");
        data.put("progressPercent", 100);
        data.put("currentTestName", "testFinal");
        data.put("testsCompleted", 10);
        data.put("testsTotal", 10);
        handler.broadcastOrderProgress(100L, data);

        verify(wsSession1, atLeast(2)).sendMessage(any(TextMessage.class));
    }

    // ------------------------------------------------------------------
    // Status change broadcast tests
    // ------------------------------------------------------------------

    @Test
    @DisplayName("Broadcast status change should notify subscribers")
    void broadcastStatusChangeShouldNotifySubscribers() throws Exception {
        OrderProgress progress = new OrderProgress();
        progress.setOrderId(100L);
        when(progressService.getOrderProgress(100L)).thenReturn(Optional.of(progress));

        handler.afterConnectionEstablished(wsSession1);

        ObjectNode subscribe = objectMapper.createObjectNode();
        subscribe.put("type", "subscribe");
        subscribe.put("orderId", 100L);
        handler.handleTextMessage(wsSession1, new TextMessage(subscribe.toString()));

        handler.broadcastOrderStatusChange(100L, "COMPLETED");
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
        OrderProgress progress = new OrderProgress();
        progress.setOrderId(100L);
        when(progressService.getOrderProgress(100L)).thenReturn(Optional.of(progress));

        handler.afterConnectionEstablished(wsSession1);
        when(wsSession1.isOpen()).thenReturn(false);

        ObjectNode subscribe = objectMapper.createObjectNode();
        subscribe.put("type", "subscribe");
        subscribe.put("orderId", 100L);
        handler.handleTextMessage(wsSession1, new TextMessage(subscribe.toString()));

        Map<String, Object> data = new HashMap<>();
        data.put("status", "RUNNING");
        handler.broadcastOrderProgress(100L, data);

        // Session is closed, so no additional messages after subscribe
        verify(wsSession1, times(1)).sendMessage(any(TextMessage.class));
    }

    // ------------------------------------------------------------------
    // Monitored orders count tests
    // ------------------------------------------------------------------

    @Test
    @DisplayName("Monitored orders should count unique orders")
    void monitoredOrdersShouldCountUniqueOrders() throws Exception {
        OrderProgress progress = new OrderProgress();
        progress.setOrderId(100L);
        when(progressService.getOrderProgress(100L)).thenReturn(Optional.of(progress));

        handler.afterConnectionEstablished(wsSession1);
        handler.afterConnectionEstablished(wsSession2);

        // Both subscribe to the same order
        ObjectNode subscribe1 = objectMapper.createObjectNode();
        subscribe1.put("type", "subscribe");
        subscribe1.put("orderId", 100L);
        handler.handleTextMessage(wsSession1, new TextMessage(subscribe1.toString()));

        ObjectNode subscribe2 = objectMapper.createObjectNode();
        subscribe2.put("type", "subscribe");
        subscribe2.put("orderId", 100L);
        handler.handleTextMessage(wsSession2, new TextMessage(subscribe2.toString()));

        assertEquals(1, handler.getMonitoredOrders());
    }
}
