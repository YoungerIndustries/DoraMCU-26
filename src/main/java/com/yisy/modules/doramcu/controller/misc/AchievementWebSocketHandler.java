package com.yisy.modules.doramcu.controller.misc;
import com.yisy.modules.doramcu.model.players.Player;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.yisy.modules.doramcu.service.misc.AchievementService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * AchievementWebSocketHandler — Real-time achievement notifications.
 *
 * WebSocket endpoint: /ws/achievements
 *
 * Message types:
 * - achievement_unlocked: New achievement unlocked
 * - leaderboard_update: Leaderboard position changed
 * - notification: Achievement notification
 * - error: Error message
 *
 * Client messages:
 * - subscribe: {"type":"subscribe","playerId":"..."}
 * - unlock: {"type":"unlock","playerId":"...","achievementId":"..."}
 * - ping/pong: Health check
 */
public class AchievementWebSocketHandler extends TextWebSocketHandler {
    private static final Logger log = LoggerFactory.getLogger(AchievementWebSocketHandler.class);

    private final AchievementService achievementService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Set<WebSocketSession> sessions = ConcurrentHashMap.newKeySet();
    private final Map<String, WebSocketSession> playerSessions = new ConcurrentHashMap<>();

    public AchievementWebSocketHandler(AchievementService achievementService) {
        this.achievementService = achievementService;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessions.add(session);
        log.info("Achievement WebSocket connected: {}", session.getId());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.remove(session);
        playerSessions.entrySet().removeIf((entry) -> entry.getValue().equals(session));
        log.info("Achievement WebSocket disconnected: {}", session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        try {
            JsonNode json = objectMapper.readTree(message.getPayload());
            String type = json.get("type").asText();
            String playerId = json.has("playerId") ? json.get("playerId").asText() : "anonymous";

            switch (type) {
                case "subscribe":
                    playerSessions.put(playerId, session);
                    sendAchievements(session, playerId);
                    sendNotifications(session, playerId);
                    break;
                case "unlock":
                    String achievementId = json.get("achievementId").asText();
                    boolean unlocked = achievementService.unlockAchievement(playerId, achievementId);
                    if (unlocked) {
                        broadcastAchievementUnlock(playerId, achievementId);
                    }
                    sendUnlockResponse(session, playerId, achievementId, unlocked);
                    break;
                case "ping":
                    sendPong(session);
                    break;
                default:
                    sendError(session, "Unknown message type: " + type);
            }
        } catch (Exception e) {
            log.error("Error handling achievement message: {}", e.getMessage());
            try {
                sendError(session, "Error: " + e.getMessage());
            } catch (IOException ioException) {
                log.error("Error sending error message: {}", ioException.getMessage());
            }
        }
    }

    /**
     * Send all achievements for a player.
     */
    private void sendAchievements(WebSocketSession session, String playerId) throws IOException {
        List<AchievementService.AchievementDto> achievements = achievementService.getPlayerAchievements(playerId);
        ObjectNode response = objectMapper.createObjectNode();
        response.put("type", "achievements");
        response.put("playerId", playerId);
        response.set("achievements", objectMapper.valueToTree(achievements));
        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));
    }

    /**
     * Send notifications for a player.
     */
    private void sendNotifications(WebSocketSession session, String playerId) throws IOException {
        List<AchievementService.AchievementNotification> notifications = achievementService.getNotifications(playerId);
        ObjectNode response = objectMapper.createObjectNode();
        response.put("type", "notifications");
        response.put("playerId", playerId);
        response.set("notifications", objectMapper.valueToTree(notifications));
        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));
    }

    /**
     * Broadcast achievement unlock to all sessions.
     */
    private void broadcastAchievementUnlock(String playerId, String achievementId) {
        AchievementService.AchievementDefinition def = achievementService.getAchievementDefinitions().get(achievementId);
        if (def == null) return;

        ObjectNode message = objectMapper.createObjectNode();
        message.put("type", "achievement_unlocked");
        message.put("playerId", playerId);
        message.put("achievementId", achievementId);
        message.put("name", def.name);
        message.put("description", def.description);
        message.put("icon", def.icon);
        message.put("points", def.points);
        message.put("timestamp", System.currentTimeMillis());

        String json;
        try {
            json = objectMapper.writeValueAsString(message);
        } catch (Exception e) {
            log.error("Error serializing achievement unlock: {}", e.getMessage());
            return;
        }

        TextMessage textMessage = new TextMessage(json);
        sessions.forEach(s -> {
            try {
                if (s.isOpen()) {
                    s.sendMessage(textMessage);
                }
            } catch (IOException e) {
                log.error("Error broadcasting achievement unlock: {}", e.getMessage());
            }
        });
    }

    /**
     * Send unlock response.
     */
    private void sendUnlockResponse(WebSocketSession session, String playerId, String achievementId, boolean unlocked) throws IOException {
        ObjectNode response = objectMapper.createObjectNode();
        response.put("type", "unlock_response");
        response.put("playerId", playerId);
        response.put("achievementId", achievementId);
        response.put("unlocked", unlocked);
        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));
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
     * Notify a specific player of an achievement unlock.
     */
    public void notifyPlayer(String playerId, String achievementId) {
        WebSocketSession session = playerSessions.get(playerId);
        if (session != null && session.isOpen()) {
            boolean unlocked = achievementService.unlockAchievement(playerId, achievementId);
            if (unlocked) {
                broadcastAchievementUnlock(playerId, achievementId);
            }
        }
    }

    /**
     * Get the number of connected sessions.
     */
    public int getConnectedSessions() {
        return sessions.size();
    }
}
