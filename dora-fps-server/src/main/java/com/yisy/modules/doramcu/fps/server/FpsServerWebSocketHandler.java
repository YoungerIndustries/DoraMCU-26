package com.yisy.modules.doramcu.fps.server;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.yisy.modules.doramcu.fps.core.FpsGameServer;
import com.yisy.modules.doramcu.fps.core.FpsPlayerState;
import com.yisy.modules.doramcu.fps.core.FpsSession;
import com.yisy.modules.doramcu.fps.core.model.FpsMatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * WebSocket handler for the dedicated FPS server.
 * Adapts WebSocket messages to the framework-agnostic FpsGameServer.
 */
@Component
public class FpsServerWebSocketHandler extends TextWebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(FpsServerWebSocketHandler.class);

    private final FpsGameServer gameServer;
    private final FpsBackendClient backendClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /** Session ID → list of connected WebSocket sessions. */
    private final ConcurrentHashMap<String, List<WebSocketSession>> sessionSessions = new ConcurrentHashMap<>();

    /** WebSocket session → player ID mapping. */
    private final ConcurrentHashMap<WebSocketSession, String> sessionPlayerIds = new ConcurrentHashMap<>();

    /** WebSocket session → player name mapping. */
    private final ConcurrentHashMap<WebSocketSession, String> sessionPlayerNames = new ConcurrentHashMap<>();

    public FpsServerWebSocketHandler(FpsServerRegistration registration) {
        this.gameServer = registration.getGameServer();
        this.backendClient = new FpsBackendClient(new FpsServerConfig());
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String sessionId = getQueryParam(session, "sessionId");
        String playerId = getQueryParam(session, "playerId");
        String playerName = getQueryParam(session, "playerName");
        String teamParam = getQueryParam(session, "team");
        String token = getQueryParam(session, "token");

        if (sessionId == null || playerId == null) {
            log.warn("WebSocket connection without sessionId or playerId — closing");
            session.close(CloseStatus.BAD_DATA);
            return;
        }

        // Validate token if provided
        if (token != null && !token.isEmpty()) {
            // Token validation is async — allow connection but log
            log.info("Player {} connecting with token to session {}", playerId, sessionId);
        }

        int team = 0;
        if (teamParam != null) {
            try {
                team = Integer.parseInt(teamParam);
            } catch (NumberFormatException e) {
                log.warn("Invalid team parameter: {}", teamParam);
            }
        }

        if (playerName == null || playerName.isEmpty()) {
            playerName = "Player_" + playerId.substring(0, Math.min(4, playerId.length()));
        }

        // Get or create the game session
        FpsSession fpsSession = gameServer.getSession(sessionId);
        if (fpsSession == null) {
            log.warn("Session {} not found — closing", sessionId);
            session.close(CloseStatus.POLICY_VIOLATION);
            return;
        }

        // Add player to the session
        if (!fpsSession.addPlayer(playerId, playerName, team)) {
            log.warn("Failed to add player {} to session {}", playerId, sessionId);
            session.close(CloseStatus.POLICY_VIOLATION);
            return;
        }

        // Register WebSocket session
        sessionSessions.computeIfAbsent(sessionId, k -> new CopyOnWriteArrayList<>()).add(session);
        sessionPlayerIds.put(session, playerId);
        sessionPlayerNames.put(session, playerName);

        log.info("Player {} connected to FPS session {} — total players: {}",
                playerName, sessionId, fpsSession.getPlayerCount());

        // Send connection acknowledgment
        ObjectNode ack = objectMapper.createObjectNode();
        ack.put("type", "CONNECT_ACK");
        ack.put("sessionId", sessionId);
        ack.put("playerId", playerId);
        ack.put("team", team);
        ack.put("message", "Connected to FPS session");

        // Include available weapons
        ArrayNode weapons = ack.putArray("weapons");
        for (var weapon : fpsSession.getAvailableWeapons()) {
            ObjectNode w = objectMapper.createObjectNode();
            w.put("id", weapon.getId());
            w.put("name", weapon.getName());
            w.put("type", weapon.getType().name());
            w.put("damage", weapon.getDamage());
            w.put("fireRate", weapon.getFireRate());
            w.put("reloadTime", weapon.getReloadTime());
            w.put("magSize", weapon.getMagSize());
            weapons.add(w);
        }

        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(ack)));

        // Broadcast player joined
        broadcastToSession(sessionId, createPlayerJoinedEvent(playerId, playerName));

        // If match is in progress, send current state
        if (fpsSession.getState() == FpsSession.SessionState.IN_PROGRESS) {
            broadcastGameState(sessionId, fpsSession);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        String playerId = sessionPlayerIds.remove(session);
        String playerName = sessionPlayerNames.remove(session);
        if (playerId != null) {
            for (Map.Entry<String, List<WebSocketSession>> entry : sessionSessions.entrySet()) {
                if (entry.getValue().remove(session)) {
                    String sessionId = entry.getKey();
                    log.info("Player {} disconnected from FPS session {} — status: {}",
                            playerName, sessionId, status.getCode());

                    FpsSession fpsSession = gameServer.getSession(sessionId);
                    if (fpsSession != null) {
                        fpsSession.removePlayer(playerId);
                    }

                    if (entry.getValue().isEmpty()) {
                        sessionSessions.remove(sessionId);
                        if (fpsSession != null && fpsSession.getState() == FpsSession.SessionState.IN_PROGRESS) {
                            fpsSession.endMatch();
                        }
                    } else {
                        broadcastToSession(sessionId, createPlayerLeftEvent(playerId, playerName));
                    }
                    break;
                }
            }
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        try {
            JsonNode root = objectMapper.readTree(message.getPayload());
            String type = root.path("type").asText("");
            String sessionId = root.path("sessionId").asText("");

            switch (type) {
                case "INPUT":
                    handleInput(session, sessionId, root);
                    break;
                case "READY":
                    handleReady(session, sessionId);
                    break;
                case "START_MATCH":
                    handleStartMatch(session, sessionId);
                    break;
                case "GET_STATE":
                    handleGetState(session, sessionId);
                    break;
                default:
                    log.warn("Unknown WS message type: {} from {}", type, session.getId());
            }
        } catch (JsonProcessingException e) {
            log.error("Failed to parse WebSocket message: {}", e.getMessage());
        } catch (Exception e) {
            log.error("Error handling WebSocket message: {}", e.getMessage(), e);
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        log.error("WebSocket transport error: {}", exception.getMessage());
    }

    private void handleInput(WebSocketSession session, String sessionId, JsonNode root) {
        String playerId = sessionPlayerIds.get(session);
        if (playerId == null) {
            sendError(session, "Player not registered");
            return;
        }

        long timestamp = root.path("timestamp").asLong(System.currentTimeMillis());
        JsonNode move = root.path("move");
        double moveX = move.path("x").asDouble(0);
        double moveZ = move.path("z").asDouble(0);
        JsonNode aim = root.path("aim");
        double aimPitch = aim.path("pitch").asDouble(0);
        double aimYaw = aim.path("yaw").asDouble(0);

        List<String> actions = new ArrayList<>();
        if (root.has("actions") && root.get("actions").isArray()) {
            for (JsonNode action : root.get("actions")) {
                actions.add(action.asText());
            }
        }

        FpsGameServer.FpsInputMessage input = new FpsGameServer.FpsInputMessage(
                playerId, timestamp, moveX, moveZ, aimPitch, aimYaw, actions
        );
        gameServer.submitInput(sessionId, input);
    }

    private void handleReady(WebSocketSession session, String sessionId) {
        FpsSession fpsSession = gameServer.getSession(sessionId);
        if (fpsSession == null) {
            sendError(session, "Session not found");
            return;
        }

        fpsSession.setState(FpsSession.SessionState.READY);
        ObjectNode msg = objectMapper.createObjectNode();
        msg.put("type", "PLAYER_READY");
        String playerId = sessionPlayerIds.get(session);
        msg.put("playerId", playerId);
        broadcastToSession(sessionId, msg);

        if (fpsSession.getPlayerCount() >= 2) {
            fpsSession.startMatch();
            broadcastToSession(sessionId, createMatchStartedEvent());
            startGameStateBroadcast(sessionId);
        }
    }

    private void handleStartMatch(WebSocketSession session, String sessionId) {
        FpsSession fpsSession = gameServer.getSession(sessionId);
        if (fpsSession == null) {
            sendError(session, "Session not found");
            return;
        }

        fpsSession.startMatch();
        broadcastToSession(sessionId, createMatchStartedEvent());
        startGameStateBroadcast(sessionId);
    }

    private void handleGetState(WebSocketSession session, String sessionId) {
        FpsSession fpsSession = gameServer.getSession(sessionId);
        if (fpsSession == null) {
            sendError(session, "Session not found");
            return;
        }

        ObjectNode state = createGameState(fpsSession);
        try {
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(state)));
        } catch (IOException e) {
            log.error("Failed to send game state: {}", e.getMessage());
        }
    }

    private void startGameStateBroadcast(String sessionId) {
        java.util.concurrent.ScheduledExecutorService executor = java.util.concurrent.Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "fps-broadcast-" + sessionId);
            t.setDaemon(true);
            return t;
        });

        executor.scheduleAtFixedRate(() -> {
            FpsSession fpsSession = gameServer.getSession(sessionId);
            if (fpsSession != null && fpsSession.getState() == FpsSession.SessionState.IN_PROGRESS) {
                broadcastGameState(sessionId, fpsSession);
            }
        }, 0, 50, java.util.concurrent.TimeUnit.MILLISECONDS);
    }

    private void broadcastGameState(String sessionId, FpsSession fpsSession) {
        ObjectNode state = createGameState(fpsSession);
        broadcastToSession(sessionId, state);
    }

    private ObjectNode createGameState(FpsSession fpsSession) {
        ObjectNode state = objectMapper.createObjectNode();
        state.put("type", "STATE");
        state.put("tick", fpsSession.getCurrentTick());
        state.put("sessionId", fpsSession.getSessionId());

        ArrayNode players = state.putArray("players");
        for (FpsPlayerState player : fpsSession.getPlayers().values()) {
            ObjectNode p = objectMapper.createObjectNode();
            p.put("id", player.getPlayerId());
            p.put("name", player.getPlayerName());
            p.put("team", player.getTeam());
            p.put("health", player.getHealth());
            p.put("maxHealth", player.getMaxHealth());
            p.put("ammo", player.getAmmo());
            p.put("reserveAmmo", player.getReserveAmmo());
            p.put("alive", player.isAlive());
            p.put("kills", player.getKills());
            p.put("deaths", player.getDeaths());
            p.put("reloading", player.isReloading());

            ObjectNode pos = objectMapper.createObjectNode();
            pos.put("x", player.getPosX());
            pos.put("y", player.getPosY());
            pos.put("z", player.getPosZ());
            p.set("position", pos);

            ObjectNode rot = objectMapper.createObjectNode();
            rot.put("pitch", player.getPitch());
            rot.put("yaw", player.getYaw());
            p.set("rotation", rot);

            players.add(p);
        }

        ArrayNode events = state.putArray("events");
        List<FpsSession.SessionEvent> sessionEvents = fpsSession.getEvents();
        int start = Math.max(0, sessionEvents.size() - 20);
        for (int i = start; i < sessionEvents.size(); i++) {
            FpsSession.SessionEvent event = sessionEvents.get(i);
            ObjectNode e = objectMapper.createObjectNode();
            e.put("type", event.type().name());
            if (event.playerId() != null) e.put("playerId", event.playerId());
            if (event.playerName() != null) e.put("playerName", event.playerName());
            if (event.data() != null) e.put("data", event.data());
            events.add(e);
        }

        // Capture point states for CTP mode
        if (fpsSession.getGameMode() == FpsMatch.GameMode.CAPTURE_THE_POINT) {
            Map<String, FpsSession.CapturePointState> cps = fpsSession.getCapturePointStates();
            if (cps != null && !cps.isEmpty()) {
                ArrayNode capturePoints = state.putArray("capturePoints");
                for (FpsSession.CapturePointState c : cps.values()) {
                    ObjectNode cp = objectMapper.createObjectNode();
                    cp.put("id", c.pointId());
                    cp.put("ownerTeam", c.ownerTeam());
                    cp.put("progress", c.captureProgress());
                    capturePoints.add(cp);
                }
            }

            Map<Integer, Integer> scores = fpsSession.getTeamScores();
            if (scores != null && !scores.isEmpty()) {
                ObjectNode teamScores = state.putObject("teamScores");
                for (Map.Entry<Integer, Integer> entry : scores.entrySet()) {
                    teamScores.put(String.valueOf(entry.getKey()), entry.getValue());
                }
            }
        }

        return state;
    }

    private void broadcastToSession(String sessionId, JsonNode message) {
        List<WebSocketSession> sessions = sessionSessions.get(sessionId);
        if (sessions == null || sessions.isEmpty()) return;

        TextMessage textMsg;
        try {
            textMsg = new TextMessage(objectMapper.writeValueAsString(message));
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize message: {}", e.getMessage());
            return;
        }

        for (WebSocketSession wsSession : sessions) {
            try {
                if (wsSession.isOpen()) {
                    wsSession.sendMessage(textMsg);
                }
            } catch (IOException e) {
                log.warn("Failed to send to {}: {}", wsSession.getId(), e.getMessage());
            }
        }
    }

    private ObjectNode createPlayerJoinedEvent(String playerId, String playerName) {
        ObjectNode event = objectMapper.createObjectNode();
        event.put("type", "EVENT");
        event.put("eventType", "PLAYER_JOINED");
        event.put("playerId", playerId);
        event.put("playerName", playerName);
        return event;
    }

    private ObjectNode createPlayerLeftEvent(String playerId, String playerName) {
        ObjectNode event = objectMapper.createObjectNode();
        event.put("type", "EVENT");
        event.put("eventType", "PLAYER_LEFT");
        event.put("playerId", playerId);
        event.put("playerName", playerName);
        return event;
    }

    private ObjectNode createMatchStartedEvent() {
        ObjectNode event = objectMapper.createObjectNode();
        event.put("type", "MATCH_STARTED");
        event.put("message", "Match started");
        return event;
    }

    private void sendError(WebSocketSession session, String message) {
        ObjectNode error = objectMapper.createObjectNode();
        error.put("type", "ERROR");
        error.put("message", message);
        try {
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(error)));
        } catch (IOException e) {
            log.error("Failed to send error: {}", e.getMessage());
        }
    }

    private String getQueryParam(WebSocketSession session, String name) {
        String query = session.getUri().getQuery();
        if (query == null || query.isEmpty()) return null;
        for (String param : query.split("&")) {
            String[] parts = param.split("=", 2);
            if (parts.length == 2 && parts[0].equals(name)) {
                try {
                    return java.net.URLDecoder.decode(parts[1], "UTF-8");
                } catch (Exception e) {
                    return parts[1];
                }
            }
        }
        return null;
    }
}
