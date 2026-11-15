package com.yisy.modules.doramcu.fps;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.yisy.modules.doraai.dto.VoiceCommandResponse;
import com.yisy.modules.doraai.service.VoiceCommandService;
import com.yisy.modules.doramcu.fps.core.FpsSession;
import com.yisy.modules.doramcu.fps.core.FpsPlayerState;
import com.yisy.modules.doramcu.fps.core.model.FpsMatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * WebSocket handler for FPS real-time multiplayer.
 * <p>
 * Each connected player has one WebSocket session. The handler:
 * <ul>
 *   <li>Joins a player to a session on connection</li>
 *   <li>Dispatches incoming INPUT messages to the game server</li>
 *   <li>Broadcasts game state changes to all connected players</li>
   *   <li>Handles player disconnections and respawns</li>
 * </ul>
 * </p>
 */
@SuppressWarnings("AutoCloseableResource")
@Component
public class FpsWebSocketHandler extends TextWebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(FpsWebSocketHandler.class);

    private final FpsGameServerWrapper gameServer;
    private final VoiceCommandService voiceCommandService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /** Session ID â†’ list of connected player sessions. */
    private final ConcurrentHashMap<String, List<WebSocketSession>> sessionSessions = new ConcurrentHashMap<>();

    /** WebSocket session â†’ player ID mapping. */
    private final ConcurrentHashMap<WebSocketSession, String> sessionPlayerIds = new ConcurrentHashMap<>();

    /** WebSocket session â†’ player name mapping. */
    private final ConcurrentHashMap<WebSocketSession, String> sessionPlayerNames = new ConcurrentHashMap<>();

    public FpsWebSocketHandler(FpsGameServerWrapper gameServer, VoiceCommandService voiceCommandService) {
        this.gameServer = gameServer;
        this.voiceCommandService = voiceCommandService;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String sessionId = getQueryParam(session, "sessionId");
        String playerId = getQueryParam(session, "playerId");
        String playerName = getQueryParam(session, "playerName");
        String teamParam = getQueryParam(session, "team");

        if (sessionId == null || playerId == null) {
            log.warn("WebSocket connection without sessionId or playerId â€” closing");
            session.close(CloseStatus.BAD_DATA);
            return;
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
            log.warn("Session {} not found â€” closing", sessionId);
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

        log.info("Player {} connected to FPS session {} â€” total players: {}",
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

        // Broadcast player joined to all clients
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
            // Find the session this player belonged to
            for (Map.Entry<String, List<WebSocketSession>> entry : sessionSessions.entrySet()) {
                if (entry.getValue().remove(session)) {
                    String sessionId = entry.getKey();
                    log.info("Player {} disconnected from FPS session {} â€” status: {}",
                            playerName, sessionId, status.getCode());

                    // Remove player from game session
                    FpsSession fpsSession = gameServer.getSession(sessionId);
                    if (fpsSession != null) {
                        fpsSession.removePlayer(playerId);
                    }

                    // If no players left, clean up
                    if (entry.getValue().isEmpty()) {
                        sessionSessions.remove(sessionId);
                        log.info("No players left in FPS session {} â€” session ended", sessionId);

                        // End the match if in progress
                        if (fpsSession != null && fpsSession.getState() == FpsSession.SessionState.IN_PROGRESS) {
                            fpsSession.endMatch();
                        }
                    } else {
                        // Notify remaining players
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

            log.debug("Received WS message type={} for session={}", type, sessionId);

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
                case "VOICE_COMMAND":
                    handleVoiceCommand(session, sessionId, root);
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

    // â”€â”€ Message Handlers â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    private void handleInput(WebSocketSession session, String sessionId, JsonNode root) {
        String playerId = sessionPlayerIds.get(session);
        if (playerId == null) {
            sendError(session, "Player not registered");
            return;
        }

        long timestamp = root.path("timestamp").asLong(System.currentTimeMillis());

        // Parse movement
        JsonNode move = root.path("move");
        double moveX = move.path("x").asDouble(0);
        double moveZ = move.path("z").asDouble(0);

        // Parse aim
        JsonNode aim = root.path("aim");
        double aimPitch = aim.path("pitch").asDouble(0);
        double aimYaw = aim.path("yaw").asDouble(0);

        // Parse actions
        List<String> actions = new ArrayList<>();
        if (root.has("actions") && root.get("actions").isArray()) {
            for (JsonNode action : root.get("actions")) {
                actions.add(action.asText());
            }
        }

        com.yisy.modules.doramcu.fps.core.FpsGameServer.FpsInputMessage input = new com.yisy.modules.doramcu.fps.core.FpsGameServer.FpsInputMessage(
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

        // If all players are ready, start the match
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

    private void handleVoiceCommand(WebSocketSession session, String sessionId, JsonNode root) {
        String playerId = sessionPlayerIds.get(session);
        if (playerId == null) {
            sendError(session, "Player not registered");
            return;
        }

        String text = root.path("text").asText(null);
        if (text == null || text.isBlank()) {
            sendError(session, "Voice command text is required");
            return;
        }

        log.info("Voice command from player {} in session {}: '{}'", playerId, sessionId, text);

        try {
            // Interpret the command using VoiceCommandService
            VoiceCommandResponse commandResponse = voiceCommandService.interpretCommand(text, sessionId);

            // Build the response message
            ObjectNode response = objectMapper.createObjectNode();
            response.put("type", "VOICE_COMMAND_RESULT");
            response.put("commandType", commandResponse.getCommandType());
            response.put("executed", commandResponse.isExecuted());
            response.put("message", commandResponse.getMessage());
            response.put("transcribedText", text);

            // Add parameters as a nested object
            if (commandResponse.getParameters() != null && !commandResponse.getParameters().isEmpty()) {
                ObjectNode params = response.objectNode();
                commandResponse.getParameters().forEach((key, value) -> {
                    if (value instanceof String) {
                        params.put(key, (String) value);
                    } else if (value instanceof Number) {
                        params.put(key, ((Number) value).doubleValue());
                    } else if (value instanceof Boolean) {
                        params.put(key, (Boolean) value);
                    } else {
                        params.put(key, String.valueOf(value));
                    }
                });
                response.set("parameters", params);
            }

            // Send result back to the player
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));

            // If the command was executed, also broadcast an event to all players in the session
            if (commandResponse.isExecuted() && !"QUERY".equals(commandResponse.getCommandType())) {
                ObjectNode event = objectMapper.createObjectNode();
                event.put("type", "EVENT");
                event.put("eventType", "VOICE_COMMAND");
                event.put("playerId", playerId);
                event.put("playerName", sessionPlayerNames.get(session));
                event.put("commandType", commandResponse.getCommandType());
                event.put("data", commandResponse.getMessage());
                broadcastToSession(sessionId, event);
            }

            log.debug("Voice command processed: {} -> {}", text, commandResponse.getCommandType());
        } catch (Exception e) {
            log.error("Failed to process voice command: {}", e.getMessage(), e);
            sendError(session, "Failed to process voice command: " + e.getMessage());
        }
    }

    // â”€â”€ Game State Broadcast â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    /**
     * Start periodic game state broadcasting for a session.
     */
    private void startGameStateBroadcast(String sessionId) {
        // Broadcast state at the same tick rate as the game server
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

    /**
     * Broadcast game state to all players in a session.
     */
    private void broadcastGameState(String sessionId, FpsSession fpsSession) {
        ObjectNode state = createGameState(fpsSession);
        broadcastToSession(sessionId, state);
    }

    /**
     * Create a game state JSON object from the session.
     */
    private ObjectNode createGameState(FpsSession fpsSession) {
        ObjectNode state = objectMapper.createObjectNode();
        state.put("type", "STATE");
        state.put("tick", fpsSession.getCurrentTick());
        state.put("sessionId", fpsSession.getSessionId());

        // Add player states
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

            // Position
            ObjectNode pos = objectMapper.createObjectNode();
            pos.put("x", player.getPosX());
            pos.put("y", player.getPosY());
            pos.put("z", player.getPosZ());
            p.set("position", pos);

            // Rotation
            ObjectNode rot = objectMapper.createObjectNode();
            rot.put("pitch", player.getPitch());
            rot.put("yaw", player.getYaw());
            p.set("rotation", rot);

            players.add(p);
        }

        // Add recent events
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

        // Add capture point states for CTP mode
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

            // Add team scores
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

    /**
     * Broadcast a message to all players in a session.
     */
    private void broadcastToSession(String sessionId, JsonNode message) {
        List<WebSocketSession> sessions = sessionSessions.get(sessionId);
        if (sessions == null || sessions.isEmpty()) {
            return;
        }

        TextMessage textMsg;
        try {
            textMsg = new TextMessage(objectMapper.writeValueAsString(message));
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize message: {}", e.getMessage());
            return;
        }

        int sent = 0;
        for (WebSocketSession wsSession : sessions) {
            try {
                if (wsSession.isOpen()) {
                    wsSession.sendMessage(textMsg);
                    sent++;
                }
            } catch (IOException e) {
                log.warn("Failed to send to {}: {}", wsSession.getId(), e.getMessage());
            }
        }
        if (sent > 0) {
            log.debug("Broadcast to {} players in session {}", sent, sessionId);
        }
    }

    // â”€â”€ Event Creators â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

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

    // â”€â”€ Utilities â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

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
        if (query == null || query.isEmpty()) {
            return null;
        }
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
