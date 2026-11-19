package com.yisy.modules.doramcu.controller.misc;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.yisy.modules.doramcu.admin.service.AiAdminNotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * NotificationWebSocketHandler — Real-time notification push updates.
 *
 * WebSocket endpoint: /ws/notifications
 *
 * Message types (server → client):
 * - new_notification: New notification for a subscribed admin
 * - notification_read: Notification marked as read
 * - snapshot: Initial unread notifications when subscribing to an admin
 * - pong: Response to ping
 * - error: Error message
 *
 * Client messages:
 * - subscribe: {"type":"subscribe","adminId":123}
 * - unsubscribe: {"type":"unsubscribe","adminId":123}
 * - ping: {"type":"ping"}
 */
public class NotificationWebSocketHandler extends TextWebSocketHandler {
    private static final Logger log = LoggerFactory.getLogger(NotificationWebSocketHandler.class);

    private final AiAdminNotificationService notificationService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // All connected sessions
    private final Set<WebSocketSession> sessions = ConcurrentHashMap.newKeySet();

    // Maps adminId to sessions subscribed to that admin's notifications
    private final Map<Long, Set<WebSocketSession>> adminSubscriptions = new ConcurrentHashMap<>();

    // Maps session to set of adminIds it is subscribed to
    private final Map<WebSocketSession, Set<Long>> sessionSubscriptions = new ConcurrentHashMap<>();

    public NotificationWebSocketHandler(AiAdminNotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessions.add(session);
        sessionSubscriptions.put(session, ConcurrentHashMap.newKeySet());
        log.info("Notification WebSocket connected: {}", session.getId());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.remove(session);
        // Unsubscribe from all admins
        Set<Long> subscribedAdmins = sessionSubscriptions.remove(session);
        if (subscribedAdmins != null) {
            for (Long adminId : subscribedAdmins) {
                Set<WebSocketSession> adminSessions = adminSubscriptions.get(adminId);
                if (adminSessions != null) {
                    adminSessions.remove(session);
                    if (adminSessions.isEmpty()) {
                        adminSubscriptions.remove(adminId);
                    }
                }
            }
        }
        log.info("Notification WebSocket disconnected: {}", session.getId());
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) {
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
            log.error("Error handling notification message: {}", e.getMessage());
            try {
                sendError(session, "Error: " + e.getMessage());
            } catch (IOException ioException) {
                log.error("Error sending error message: {}", ioException.getMessage());
            }
        }
    }

    /**
     * Handle subscribe message: client wants to receive notifications for a specific admin.
     */
    private void handleSubscribe(WebSocketSession session, JsonNode json) throws IOException {
        if (!json.has("adminId")) {
            sendError(session, "Missing adminId in subscribe message");
            return;
        }

        Long adminId = json.get("adminId").asLong();
        Set<WebSocketSession> adminSessions = adminSubscriptions.computeIfAbsent(adminId, k -> ConcurrentHashMap.newKeySet());
        adminSessions.add(session);

        Set<Long> subscribedAdmins = sessionSubscriptions.get(session);
        if (subscribedAdmins != null) {
            subscribedAdmins.add(adminId);
        }

        // Send initial snapshot of unread notifications
        sendSnapshot(session, adminId);
        log.info("Session {} subscribed to admin {} notifications", session.getId(), adminId);
    }

    /**
     * Handle unsubscribe message: client no longer wants notifications for a specific admin.
     */
    private void handleUnsubscribe(WebSocketSession session, JsonNode json) throws IOException {
        if (!json.has("adminId")) {
            sendError(session, "Missing adminId in unsubscribe message");
            return;
        }

        Long adminId = json.get("adminId").asLong();
        Set<WebSocketSession> adminSessions = adminSubscriptions.get(adminId);
        if (adminSessions != null) {
            adminSessions.remove(session);
            if (adminSessions.isEmpty()) {
                adminSubscriptions.remove(adminId);
            }
        }

        Set<Long> subscribedAdmins = sessionSubscriptions.get(session);
        if (subscribedAdmins != null) {
            subscribedAdmins.remove(adminId);
        }

        log.info("Session {} unsubscribed from admin {} notifications", session.getId(), adminId);
    }

    /**
     * Send initial snapshot of unread notifications when a client subscribes to an admin.
     */
    private void sendSnapshot(WebSocketSession session, Long adminId) throws IOException {
        ObjectNode response = objectMapper.createObjectNode();
        response.put("type", "snapshot");
        response.put("adminId", adminId);

        // Add unread count
        long unreadCount = notificationService.getUnreadCountForAdmin(adminId);
        response.put("unreadCount", unreadCount);
        response.put("timestamp", System.currentTimeMillis());

        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));
    }

    /**
     * Broadcast new notification to all sessions subscribed to a specific admin.
     * Called by AiAdminNotificationService when a new notification is created.
     */
    public void broadcastNewNotification(Long adminId, Map<String, Object> notificationData) {
        Set<WebSocketSession> adminSessions = adminSubscriptions.get(adminId);
        if (adminSessions == null || adminSessions.isEmpty()) {
            log.debug("No subscribers for admin {} notifications", adminId);
            return;
        }

        ObjectNode message = objectMapper.createObjectNode();
        message.put("type", "new_notification");
        message.put("adminId", adminId != null ? adminId : 0);
        if (notificationData != null) {
            notificationData.forEach((key, value) -> {
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
            log.error("Error serializing notification: {}", e.getMessage());
            return;
        }

        TextMessage textMessage = new TextMessage(json);
        adminSessions.forEach(s -> {
            try {
                if (s.isOpen()) {
                    s.sendMessage(textMessage);
                }
            } catch (IOException e) {
                log.error("Error broadcasting notification to session {}: {}", s.getId(), e.getMessage());
            }
        });
        log.debug("Broadcast new notification for admin {} to {} sessions", adminId, adminSessions.size());
    }

    /**
     * Broadcast notification read status change to all sessions subscribed to a specific admin.
     */
    public void broadcastNotificationRead(Long adminId, Long notificationId) {
        Set<WebSocketSession> adminSessions = adminSubscriptions.get(adminId);
        if (adminSessions == null || adminSessions.isEmpty()) {
            return;
        }

        ObjectNode message = objectMapper.createObjectNode();
        message.put("type", "notification_read");
        message.put("adminId", adminId);
        message.put("notificationId", notificationId != null ? notificationId : 0);
        message.put("timestamp", System.currentTimeMillis());

        String json;
        try {
            json = objectMapper.writeValueAsString(message);
        } catch (Exception e) {
            log.error("Error serializing read notification: {}", e.getMessage());
            return;
        }

        TextMessage textMessage = new TextMessage(json);
        adminSessions.forEach(s -> {
            try {
                if (s.isOpen()) {
                    s.sendMessage(textMessage);
                }
            } catch (IOException e) {
                log.error("Error broadcasting read notification to session {}: {}", s.getId(), e.getMessage());
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
     * Get the number of unique admins being monitored.
     */
    public int getMonitoredAdmins() {
        return adminSubscriptions.size();
    }
}
