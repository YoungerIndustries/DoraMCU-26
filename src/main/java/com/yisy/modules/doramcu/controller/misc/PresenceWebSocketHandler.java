package com.yisy.modules.doramcu.controller.misc;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.yisy.modules.doramcu.service.game.GameStateBridge;
import com.yisy.modules.doramcu.service.ai.AiAdminVisibilityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * WebSocket handler for multiplayer presence tracking in 3D galaxy visualization.
 * <p>
 * Connection URL: {@code ws://host:port/ws/presence?sessionId=ABC&playerId=1&playerName=John}
 * </p>
 * <p>
 * Server-to-client message types:
 * <ul>
 *   <li>{@code player_joined} — New player entered the session</li>
 *   <li>{@code player_left} — Player left the session</li>
 *   <li>{@code player_moved} — Player position update</li>
 *   <li>{@code player_activity} — Player activity change (mining, building, etc.)</li>
 *   <li>{@code player_chat} — Chat message from a player</li>
 *   <li>{@code player_list} — Full list of online players</li>
 * </ul>
 * </p>
 * <p>
 * Client-to-server message types:
 * <ul>
 *   <li>{@code chat} — Send a chat message</li>
 *   <li>{@code move} — Update player position</li>
 *   <li>{@code activity} — Update player activity</li>
 *   <li>{@code ping} — Keep-alive ping</li>
 * </ul>
 * </p>
 */
@Component
public class PresenceWebSocketHandler extends TextWebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(PresenceWebSocketHandler.class);

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final GameStateBridge gameStateBridge;
    private final AiAdminVisibilityService aiAdminVisibilityService;
    private final boolean realDataMode;

    // Player data
    private final Map<String, PlayerData> players = new ConcurrentHashMap<>();
    private final Map<WebSocketSession, String> sessionPlayerIds = new ConcurrentHashMap<>();
    private final Map<WebSocketSession, String> sessionSessionIds = new ConcurrentHashMap<>();
    private final Map<String, List<WebSocketSession>> sessionGroups = new ConcurrentHashMap<>();

    // Simulation
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    /**
     * Player data structure.
     */
    public static class PlayerData {
        String playerId;
        String playerName;
        String sessionId;
        String team;
        String activity; // idle, mining, building, exploring, combat, trading
        double x, y, z;
        long lastSeen;
        boolean online;
    }

    public PresenceWebSocketHandler() {
        this(null, null, false);
    }

    public PresenceWebSocketHandler(GameStateBridge gameStateBridge, AiAdminVisibilityService aiAdminVisibilityService, boolean realDataMode) {
        this.gameStateBridge = gameStateBridge;
        this.aiAdminVisibilityService = aiAdminVisibilityService;
        this.realDataMode = realDataMode;
        if (!realDataMode) {
            scheduler.scheduleAtFixedRate(this::simulateBotActivity, 10, 8, TimeUnit.SECONDS);
        } else {
            log.info("PresenceManager starting in real-data mode with periodic refresh");
            scheduler.scheduleAtFixedRate(this::refreshRealData, 10, 10, TimeUnit.SECONDS);
        }
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        String playerId = extractParameter(session.getUri().getQuery(), "playerId");
        String playerName = extractParameter(session.getUri().getQuery(), "playerName");
        String gameSessionId = extractParameter(session.getUri().getQuery(), "sessionId");

        if (playerId == null) playerId = "player_" + System.currentTimeMillis();
        if (playerName == null) playerName = "Player";
        if (gameSessionId == null) gameSessionId = "default";

        sessionPlayerIds.put(session, playerId);
        sessionSessionIds.put(session, gameSessionId);

        // Register player
        PlayerData player = new PlayerData();
        player.playerId = playerId;
        player.playerName = playerName;
        player.sessionId = gameSessionId;
        player.team = "blue";
        player.activity = "idle";
        player.x = 0;
        player.y = 0;
        player.z = 0;
        player.lastSeen = System.currentTimeMillis();
        player.online = true;
        players.put(playerId, player);

        // Group by game session
        sessionGroups.computeIfAbsent(gameSessionId, k -> Collections.synchronizedList(new ArrayList<>())).add(session);

        log.info("Presence connected: sessionId={}, playerId={}, playerName={}", gameSessionId, playerId, playerName);

        // Broadcast player joined
        broadcastPlayerJoined(player);

        // Send player list to the new player
        sendPlayerList(session);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        try {
            ObjectNode payload = (ObjectNode) objectMapper.readTree(message.getPayload());
            String type = payload.has("type") ? payload.get("type").asText() : "";
            String playerId = sessionPlayerIds.get(session);

            switch (type) {
                case "chat":
                    handleChat(session, payload, playerId);
                    break;
                case "move":
                    handleMove(session, payload, playerId);
                    break;
                case "activity":
                    handleActivity(session, payload, playerId);
                    break;
                case "ping":
                    sendPingResponse(session);
                    break;
                default:
                    log.warn("Unknown presence message type: {}", type);
            }
        } catch (Exception e) {
            log.error("Error handling presence message", e);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        String playerId = sessionPlayerIds.remove(session);
        String sessionId = sessionSessionIds.remove(session);

        if (playerId != null) {
            PlayerData player = players.get(playerId);
            if (player != null) {
                player.online = false;
                player.lastSeen = System.currentTimeMillis();

                // Broadcast player left
                broadcastPlayerLeft(playerId);

                log.info("Presence disconnected: sessionId={}, playerId={}", sessionId, playerId);
            }
        }

        // Remove from session group
        if (sessionId != null) {
            List<WebSocketSession> group = sessionGroups.get(sessionId);
            if (group != null) {
                group.remove(session);
                if (group.isEmpty()) {
                    sessionGroups.remove(sessionId);
                }
            }
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        log.error("Presence transport error", exception);
        afterConnectionClosed(session, CloseStatus.SERVER_ERROR);
    }

    /**
     * Handle chat message.
     */
    private void handleChat(WebSocketSession session, ObjectNode payload, String playerId) {
        String text = payload.has("text") ? payload.get("text").asText() : "";
        if (text.isEmpty()) return;

        PlayerData player = players.get(playerId);
        if (player == null) return;

        try {
            ObjectNode data = objectMapper.createObjectNode();
            data.put("playerId", playerId);
            data.put("playerName", player.playerName);
            data.put("text", text);
            data.put("timestamp", System.currentTimeMillis());

            ObjectNode position = objectMapper.createObjectNode();
            position.put("x", player.x);
            position.put("y", player.y);
            position.put("z", player.z);
            data.set("position", position);

            ObjectNode message = objectMapper.createObjectNode();
            message.put("type", "player_chat");
            message.set("data", data);

            broadcast(new TextMessage(objectMapper.writeValueAsString(message)));
        } catch (IOException e) {
            log.error("Failed to broadcast chat message", e);
        }
    }

    /**
     * Handle player movement.
     */
    private void handleMove(WebSocketSession session, ObjectNode payload, String playerId) {
        PlayerData player = players.get(playerId);
        if (player == null) return;

        if (payload.has("position")) {
            ObjectNode pos = (ObjectNode) payload.get("position");
            player.x = pos.has("x") ? pos.get("x").asDouble() : player.x;
            player.y = pos.has("y") ? pos.get("y").asDouble() : player.y;
            player.z = pos.has("z") ? pos.get("z").asDouble() : player.z;
        }

        player.lastSeen = System.currentTimeMillis();

        try {
            ObjectNode data = objectMapper.createObjectNode();
            data.put("playerId", playerId);
            data.put("playerName", player.playerName);

            ObjectNode position = objectMapper.createObjectNode();
            position.put("x", player.x);
            position.put("y", player.y);
            position.put("z", player.z);
            data.set("position", position);

            ObjectNode message = objectMapper.createObjectNode();
            message.put("type", "player_moved");
            message.set("data", data);

            broadcast(new TextMessage(objectMapper.writeValueAsString(message)));
        } catch (IOException e) {
            log.error("Failed to broadcast player movement", e);
        }

        // Broadcast nearby AI admins to the moving player
        broadcastNearbyAiAdmins(session, player);
    }

    /**
     * Broadcast nearby AI admins to a player after movement.
     */
    private void broadcastNearbyAiAdmins(WebSocketSession session, PlayerData player) {
        if (aiAdminVisibilityService == null) return;
        
        // Extract galaxy name from session ID or use a default
        String galaxyName = extractParameter(session.getUri().getQuery(), "galaxyName");
        if (galaxyName == null || galaxyName.isEmpty()) {
            galaxyName = player.sessionId; // fallback to session ID as galaxy identifier
        }

        try {
            List<com.yisy.modules.doramcu.admin.dto.AiAdminVisibilityDTO> nearbyAdmins = aiAdminVisibilityService
                    .getAiAdminsNearPlayer(galaxyName, player.x, player.y, player.z,
                            com.yisy.modules.doramcu.service.ai.AiAdminVisibilityService.DEFAULT_PROXIMITY_RADIUS);

            if (nearbyAdmins.isEmpty()) return;

            ObjectNode message = objectMapper.createObjectNode();
            message.put("type", "ai_admins_near");
            
            ObjectNode data = objectMapper.createObjectNode();
            data.put("playerId", player.playerId);
            data.put("galaxyName", galaxyName);
            
            ArrayNode adminsArray = objectMapper.createArrayNode();
            for (com.yisy.modules.doramcu.admin.dto.AiAdminVisibilityDTO admin : nearbyAdmins) {
                ObjectNode adminNode = objectMapper.createObjectNode();
                adminNode.put("id", String.valueOf(admin.getId()));
                adminNode.put("name", admin.getName());
                adminNode.put("role", admin.getRole());
                adminNode.put("posX", admin.getPosX());
                adminNode.put("posY", admin.getPosY());
                adminNode.put("posZ", admin.getPosZ());
                adminNode.put("distance", admin.getDistance());
                adminNode.put("currentOrderId", admin.getCurrentOrderId() != null ? 
                        String.valueOf(admin.getCurrentOrderId()) : "null");
                adminsArray.add(adminNode);
            }
            data.set("admins", adminsArray);
            message.set("data", data);

            // Send only to the requesting player's session
            if (session.isOpen()) {
                session.sendMessage(new TextMessage(objectMapper.writeValueAsString(message)));
            }
        } catch (Exception e) {
            log.warn("Failed to broadcast nearby AI admins for player {}: {}", player.playerId, e.getMessage());
        }
    }

    /**
     * Handle activity change.
     */
    private void handleActivity(WebSocketSession session, ObjectNode payload, String playerId) {
        PlayerData player = players.get(playerId);
        if (player == null) return;

        String activity = payload.has("activity") ? payload.get("activity").asText() : "idle";
        player.activity = activity;

        try {
            ObjectNode data = objectMapper.createObjectNode();
            data.put("playerId", playerId);
            data.put("playerName", player.playerName);
            data.put("activity", activity);

            ObjectNode position = objectMapper.createObjectNode();
            position.put("x", player.x);
            position.put("y", player.y);
            position.put("z", player.z);
            data.set("position", position);

            ObjectNode message = objectMapper.createObjectNode();
            message.put("type", "player_activity");
            message.set("data", data);

            broadcast(new TextMessage(objectMapper.writeValueAsString(message)));
        } catch (IOException e) {
            log.error("Failed to broadcast activity change", e);
        }
    }

    /**
     * Broadcast player joined event.
     */
    private void broadcastPlayerJoined(PlayerData player) {
        try {
            ObjectNode data = objectMapper.createObjectNode();
            data.put("playerId", player.playerId);
            data.put("playerName", player.playerName);
            data.put("team", player.team);

            ObjectNode position = objectMapper.createObjectNode();
            position.put("x", player.x);
            position.put("y", player.y);
            position.put("z", player.z);
            data.set("position", position);

            ObjectNode message = objectMapper.createObjectNode();
            message.put("type", "player_joined");
            message.set("data", data);

            broadcast(new TextMessage(objectMapper.writeValueAsString(message)));
        } catch (IOException e) {
            log.error("Failed to broadcast player joined", e);
        }
    }

    /**
     * Broadcast player left event.
     */
    private void broadcastPlayerLeft(String playerId) {
        try {
            ObjectNode data = objectMapper.createObjectNode();
            data.put("playerId", playerId);

            ObjectNode message = objectMapper.createObjectNode();
            message.put("type", "player_left");
            message.set("data", data);

            broadcast(new TextMessage(objectMapper.writeValueAsString(message)));
        } catch (IOException e) {
            log.error("Failed to broadcast player left", e);
        }
    }

    /**
     * Send player list to a session.
     */
    private void sendPlayerList(WebSocketSession session) {
        try {
            ObjectNode message = objectMapper.createObjectNode();
            message.put("type", "player_list");

            ObjectNode data = objectMapper.createObjectNode();
            data.putPOJO("players", players.values().stream()
                    .filter(p -> p.online)
                    .map(p -> {
                        ObjectNode node = objectMapper.createObjectNode();
                        node.put("playerId", p.playerId);
                        node.put("playerName", p.playerName);
                        node.put("team", p.team);
                        node.put("activity", p.activity);
                        ObjectNode pos = objectMapper.createObjectNode();
                        pos.put("x", p.x);
                        pos.put("y", p.y);
                        pos.put("z", p.z);
                        node.set("position", pos);
                        return node;
                    })
                    .toList());
            message.set("data", data);

            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(message)));
        } catch (IOException e) {
            log.error("Failed to send player list", e);
        }
    }

    /**
     * Send ping response.
     */
    private void sendPingResponse(WebSocketSession session) {
        try {
            ObjectNode message = objectMapper.createObjectNode();
            message.put("type", "pong");
            message.put("timestamp", System.currentTimeMillis());
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(message)));
        } catch (IOException e) {
            log.error("Failed to send pong", e);
        }
    }

    /**
     * Simulate bot player activity.
     */
    private void simulateBotActivity() {
        Random random = new Random();

        // Ensure at least some bot players exist
        List<String> botIds = new ArrayList<>();
        for (Map.Entry<String, PlayerData> entry : players.entrySet()) {
            if (entry.getValue().playerId.startsWith("bot_")) {
                botIds.add(entry.getKey());
            }
        }

        // Create bots if needed
        if (botIds.isEmpty() && !sessionGroups.isEmpty()) {
            String[] botNames = {"Commander Riker", "Lt. Data", "Dr. Crusher", "Counselor Troi", "Worf"};
            String[] activities = {"mining", "building", "exploring", "trading", "idle"};
            String[] teams = {"blue", "red", "green", "yellow"};

            for (int i = 0; i < 3; i++) {
                String botId = "bot_" + (i + 1);
                PlayerData bot = new PlayerData();
                bot.playerId = botId;
                bot.playerName = botNames[i];
                bot.sessionId = "default";
                bot.team = teams[i % teams.length];
                bot.activity = activities[i % activities.length];
                bot.x = random.nextDouble() * 200 - 100;
                bot.y = random.nextDouble() * 50 - 25;
                bot.z = random.nextDouble() * 200 - 100;
                bot.lastSeen = System.currentTimeMillis();
                bot.online = true;
                players.put(botId, bot);
                botIds.add(botId);

                // Broadcast bot joined
                broadcastPlayerJoined(bot);
            }
        }

        // Update bot positions and activities
        for (String botId : botIds) {
            PlayerData bot = players.get(botId);
            if (bot == null) continue;

            double oldX = bot.x;
            double oldY = bot.y;
            double oldZ = bot.z;

            bot.x += (random.nextDouble() - 0.5) * 8;
            bot.y += (random.nextDouble() - 0.5) * 2;
            bot.z += (random.nextDouble() - 0.5) * 8;
            bot.lastSeen = System.currentTimeMillis();

            // Randomly change activity
            if (random.nextDouble() < 0.2) {
                String[] activities = {"idle", "mining", "building", "exploring", "trading"};
                bot.activity = activities[random.nextInt(activities.length)];

                try {
                    ObjectNode data = objectMapper.createObjectNode();
                    data.put("playerId", bot.playerId);
                    data.put("playerName", bot.playerName);
                    data.put("activity", bot.activity);

                    ObjectNode position = objectMapper.createObjectNode();
                    position.put("x", bot.x);
                    position.put("y", bot.y);
                    position.put("z", bot.z);
                    data.set("position", position);

                    ObjectNode message = objectMapper.createObjectNode();
                    message.put("type", "player_activity");
                    message.set("data", data);

                    broadcast(new TextMessage(objectMapper.writeValueAsString(message)));
                } catch (IOException e) {
                    log.error("Failed to broadcast bot activity", e);
                }
            }

            // Broadcast position update
            try {
                ObjectNode data = objectMapper.createObjectNode();
                data.put("playerId", bot.playerId);
                data.put("playerName", bot.playerName);

                ObjectNode position = objectMapper.createObjectNode();
                position.put("x", bot.x);
                position.put("y", bot.y);
                position.put("z", bot.z);
                data.set("position", position);

                ObjectNode message = objectMapper.createObjectNode();
                message.put("type", "player_moved");
                message.set("data", data);

                broadcast(new TextMessage(objectMapper.writeValueAsString(message)));
            } catch (IOException e) {
                log.error("Failed to broadcast bot movement", e);
            }
        }
    }

    /**
     * Broadcast message to all connected sessions.
     */
    private void broadcast(TextMessage message) {
        for (List<WebSocketSession> group : sessionGroups.values()) {
            for (WebSocketSession session : group) {
                try {
                    if (session.isOpen()) {
                        session.sendMessage(message);
                    }
                } catch (IOException e) {
                    log.error("Failed to broadcast to session {}", session.getId(), e);
                }
            }
        }
    }

    /**
     * Extract query parameter value.
     */
    private String extractParameter(String query, String paramName) {
        if (query == null) return null;
        for (String param : query.split("&")) {
            String[] pair = param.split("=");
            if (pair.length == 2 && pair[0].equals(paramName)) {
                try {
                    return java.net.URLDecoder.decode(pair[1], "UTF-8");
                } catch (Exception e) {
                    return pair[1];
                }
            }
        }
        return null;
    }

    /**
     * Get online player count.
     */
    public int getOnlinePlayerCount() {
        return (int) players.values().stream().filter(p -> p.online).count();
    }

    /**
     * Get all players.
     */
    public Collection<PlayerData> getPlayers() {
        return players.values();
    }

    /**
     * Refresh real player data from GameStateBridge.
     */
    private void refreshRealData() {
        if (gameStateBridge == null) return;
        for (String sessionId : sessionGroups.keySet()) {
            ObjectNode snapshot = gameStateBridge.getPlayerSnapshot(sessionId);
            if (snapshot != null) {
                try {
                    broadcast(new TextMessage(objectMapper.writeValueAsString(snapshot)));
                } catch (Exception e) {
                    log.warn("Failed to broadcast real player data for session {}: {}", sessionId, e.getMessage());
                }
            }
        }
    }

    public boolean isRealDataMode() {
        return realDataMode;
    }

    /**
     * Broadcast a dashboard update event to all connected WebSocket sessions.
     * Used by the dispatch service to notify the admin dashboard of state changes.
     *
     * @param eventType type of change: admin_workload_changed, admin_position_changed, order_completed
     * @param adminId the AI admin character ID that changed
     */
    public void broadcastDashboardUpdate(String eventType, Long adminId) {
        try {
            ObjectNode event = objectMapper.createObjectNode();
            event.put("type", "dashboard_update");
            event.put("eventType", eventType);
            event.put("adminId", adminId);
            event.put("timestamp", System.currentTimeMillis());
            broadcast(new TextMessage(objectMapper.writeValueAsString(event)));
            log.debug("Broadcast dashboard update: {} for admin {}", eventType, adminId);
        } catch (Exception e) {
            log.warn("Failed to broadcast dashboard update: {}", e.getMessage());
        }
    }
}
