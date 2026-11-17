package com.yisy.modules.doramcu.controller.misc;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.yisy.modules.doramcu.admin.service.AiAdminOrderProgressService;
import com.yisy.modules.doramcu.admin.service.AiAdminOrderProgressService.OrderProgress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * OrderProgressWebSocketHandler — Real-time order progress updates.
 *
 * WebSocket endpoint: /ws/order-progress
 *
 * Message types (server → client):
 * - progress_update: Progress changed for a subscribed order
 * - order_status_change: Order status changed (e.g., RUNNING → COMPLETED)
 * - snapshot: Initial progress state when subscribing to an order
 * - pong: Response to ping
 * - error: Error message
 *
 * Client messages:
 * - subscribe: {"type":"subscribe","orderId":123}
 * - unsubscribe: {"type":"unsubscribe","orderId":123}
 * - ping: {"type":"ping"}
 */
public class OrderProgressWebSocketHandler extends TextWebSocketHandler {
    private static final Logger log = LoggerFactory.getLogger(OrderProgressWebSocketHandler.class);

    private final AiAdminOrderProgressService progressService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // All connected sessions
    private final Set<WebSocketSession> sessions = ConcurrentHashMap.newKeySet();

    // Maps orderId to sessions subscribed to that order
    private final Map<Long, Set<WebSocketSession>> orderSubscriptions = new ConcurrentHashMap<>();

    // Maps session to set of orderIds it is subscribed to
    private final Map<WebSocketSession, Set<Long>> sessionSubscriptions = new ConcurrentHashMap<>();

    public OrderProgressWebSocketHandler(AiAdminOrderProgressService progressService) {
        this.progressService = progressService;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessions.add(session);
        sessionSubscriptions.put(session, ConcurrentHashMap.newKeySet());
        log.info("Order progress WebSocket connected: {}", session.getId());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.remove(session);
        // Unsubscribe from all orders
        Set<Long> subscribedOrders = sessionSubscriptions.remove(session);
        if (subscribedOrders != null) {
            for (Long orderId : subscribedOrders) {
                Set<WebSocketSession> orderSessions = orderSubscriptions.get(orderId);
                if (orderSessions != null) {
                    orderSessions.remove(session);
                    if (orderSessions.isEmpty()) {
                        orderSubscriptions.remove(orderId);
                    }
                }
            }
        }
        log.info("Order progress WebSocket disconnected: {}", session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        try {
            JsonNode json = objectMapper.readTree(message.getPayload());
            String type = json.get("type").asText();

            switch (type) {
                case "subscribe":
                    handleSubscribe(session, json);
                    break;
                case "unsubscribe":
                    handleUnsubscribe(session, json);
                    break;
                case "ping":
                    sendPong(session);
                    break;
                default:
                    sendError(session, "Unknown message type: " + type);
            }
        } catch (Exception e) {
            log.error("Error handling order progress message: {}", e.getMessage());
            try {
                sendError(session, "Error: " + e.getMessage());
            } catch (IOException ioException) {
                log.error("Error sending error message: {}", ioException.getMessage());
            }
        }
    }

    /**
     * Handle subscribe message: client wants to receive updates for a specific order.
     */
    private void handleSubscribe(WebSocketSession session, JsonNode json) throws IOException {
        if (!json.has("orderId")) {
            sendError(session, "Missing orderId in subscribe message");
            return;
        }

        Long orderId = json.get("orderId").asLong();
        Set<WebSocketSession> orderSessions = orderSubscriptions.computeIfAbsent(orderId, k -> ConcurrentHashMap.newKeySet());
        orderSessions.add(session);

        Set<Long> subscribedOrders = sessionSubscriptions.get(session);
        if (subscribedOrders != null) {
            subscribedOrders.add(orderId);
        }

        // Send initial snapshot of current progress
        sendSnapshot(session, orderId);
        log.info("Session {} subscribed to order {}", session.getId(), orderId);
    }

    /**
     * Handle unsubscribe message: client no longer wants updates for a specific order.
     */
    private void handleUnsubscribe(WebSocketSession session, JsonNode json) throws IOException {
        if (!json.has("orderId")) {
            sendError(session, "Missing orderId in unsubscribe message");
            return;
        }

        Long orderId = json.get("orderId").asLong();
        Set<WebSocketSession> orderSessions = orderSubscriptions.get(orderId);
        if (orderSessions != null) {
            orderSessions.remove(session);
            if (orderSessions.isEmpty()) {
                orderSubscriptions.remove(orderId);
            }
        }

        Set<Long> subscribedOrders = sessionSubscriptions.get(session);
        if (subscribedOrders != null) {
            subscribedOrders.remove(orderId);
        }

        log.info("Session {} unsubscribed from order {}", session.getId(), orderId);
    }

    /**
     * Send initial progress snapshot when a client subscribes to an order.
     */
    private void sendSnapshot(WebSocketSession session, Long orderId) throws IOException {
        Optional<OrderProgress> progress = progressService.getOrderProgress(orderId);
        if (progress.isEmpty()) {
            sendError(session, "Order " + orderId + " not found");
            return;
        }

        ObjectNode response = objectMapper.createObjectNode();
        response.put("type", "snapshot");
        response.put("orderId", orderId);
        response.put("status", progress.get().getStatus());
        response.put("progressPercent", progress.get().getProgressPercent() != null ? progress.get().getProgressPercent() : 0);
        response.put("currentTestName", progress.get().getCurrentTestName() != null ? progress.get().getCurrentTestName() : "");
        response.put("testsCompleted", progress.get().getTestsCompleted() != null ? progress.get().getTestsCompleted() : 0);
        response.put("testsTotal", progress.get().getTestsTotal() != null ? progress.get().getTestsTotal() : 0);
        response.put("submittedBy", progress.get().getSubmittedBy() != null ? progress.get().getSubmittedBy() : "");
        response.put("orderText", progress.get().getOrderText() != null ? progress.get().getOrderText() : "");
        response.put("timestamp", System.currentTimeMillis());

        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));
    }

    /**
     * Broadcast progress update to all sessions subscribed to a specific order.
     * Called by AiAdminOrderProgressService when progress changes.
     */
    public void broadcastOrderProgress(Long orderId, Map<String, Object> progressData) {
        Set<WebSocketSession> orderSessions = orderSubscriptions.get(orderId);
        if (orderSessions == null || orderSessions.isEmpty()) {
            log.debug("No subscribers for order {}", orderId);
            return;
        }

        ObjectNode message = objectMapper.createObjectNode();
        message.put("type", "progress_update");
        message.put("orderId", orderId != null ? orderId : 0);
        if (progressData != null) {
            progressData.forEach((key, value) -> {
                if (value instanceof String) {
                    message.put(key, (String) value);
                } else if (value instanceof Number) {
                    message.put(key, ((Number) value).intValue());
                } else if (value instanceof Boolean) {
                    message.put(key, (Boolean) value);
                } else if (value != null) {
                    message.put(key, value.toString());
                }
            });
        }
        message.put("timestamp", System.currentTimeMillis());

        String json;
        try {
            json = objectMapper.writeValueAsString(message);
        } catch (Exception e) {
            log.error("Error serializing progress update: {}", e.getMessage());
            return;
        }

        TextMessage textMessage = new TextMessage(json);
        orderSessions.forEach(s -> {
            try {
                if (s.isOpen()) {
                    s.sendMessage(textMessage);
                }
            } catch (IOException e) {
                log.error("Error broadcasting progress update to session {}: {}", s.getId(), e.getMessage());
            }
        });
        log.debug("Broadcast progress update for order {} to {} sessions", orderId, orderSessions.size());
    }

    /**
     * Broadcast order status change to all sessions subscribed to a specific order.
     */
    public void broadcastOrderStatusChange(Long orderId, String newStatus) {
        Set<WebSocketSession> orderSessions = orderSubscriptions.get(orderId);
        if (orderSessions == null || orderSessions.isEmpty()) {
            return;
        }

        ObjectNode message = objectMapper.createObjectNode();
        message.put("type", "order_status_change");
        message.put("orderId", orderId);
        message.put("status", newStatus);
        message.put("timestamp", System.currentTimeMillis());

        String json;
        try {
            json = objectMapper.writeValueAsString(message);
        } catch (Exception e) {
            log.error("Error serializing status change: {}", e.getMessage());
            return;
        }

        TextMessage textMessage = new TextMessage(json);
        orderSessions.forEach(s -> {
            try {
                if (s.isOpen()) {
                    s.sendMessage(textMessage);
                }
            } catch (IOException e) {
                log.error("Error broadcasting status change to session {}: {}", s.getId(), e.getMessage());
            }
        });
    }

    /**
     * Send pong response.
     */
    private void sendPong(WebSocketSession session) throws IOException {
        ObjectNode response = objectMapper.createObjectNode();
        response.put("type", "pong");
        response.put("timestamp", System.currentTimeMillis());
        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));
    }

    /**
     * Send error message.
     */
    private void sendError(WebSocketSession session, String error) throws IOException {
        ObjectNode response = objectMapper.createObjectNode();
        response.put("type", "error");
        response.put("message", error);
        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));
    }

    /**
     * Get the number of connected sessions.
     */
    public int getConnectedSessions() {
        return sessions.size();
    }

    /**
     * Get the number of unique orders being monitored.
     */
    public int getMonitoredOrders() {
        return orderSubscriptions.size();
    }
}
