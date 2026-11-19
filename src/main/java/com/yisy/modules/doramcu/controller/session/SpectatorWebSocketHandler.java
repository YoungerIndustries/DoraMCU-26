package com.yisy.modules.doramcu.controller.session;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.yisy.modules.doramcu.dto.session.GameEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;

/**
 * WebSocket handler for 3D spectator mode.
 * <p>
 * Provides read-only observation of game sessions in real-time.
 * Spectators receive redacted game state that hides sensitive player information.
 * </p>
 * <p>
 * Message types sent to clients:
 * <ul>
 *   <li>{@code spectator_joined} - Confirmation of joining a session</li>
 *   <li>{@code game_state} - Redacted game state snapshot</li>
 *   <li>{@code game_event} - Real-time game event</li>
 *   <li>{@code spectator_list} - List of current spectators</li>
 *   <li>{@code event_history} - Historical events for catch-up</li>
 * </ul>
 * </p>
 * <p>
 * Message types received from clients:
 * <ul>
 *   <li>{@code join} - Join a session: {"type":"join","sessionId":"..."}</li>
 *   <li>{@code leave} - Leave current session: {"type":"leave"}</li>
 *   <li>{@code camera_mode} - Change camera: {"type":"camera_mode","mode":"overview"}</li>
 *   <li>{@code follow_fleet} - Follow a fleet: {"type":"follow_fleet","fleetId":"..."}</li>
 *   <li>{@code list_sessions} - List active sessions: {"type":"list_sessions"}</li>
 * </ul>
 * </p>
 */
@Component
public class SpectatorWebSocketHandler extends TextWebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(SpectatorWebSocketHandler.class);

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final SpectatorService spectatorService;

    // Spectator state per WebSocket connection
    private final Map<WebSocketSession, SpectatorState> spectatorStates = new ConcurrentHashMap<>();

    // Game state refresh scheduler
    private final ScheduledExecutorService refreshScheduler = Executors.newScheduledThreadPool(2);

    public SpectatorWebSocketHandler(SpectatorService spectatorService) {
        this.spectatorService = spectatorService;
    }

    /**
     * State tracking for each spectator connection.
     */
    private static class SpectatorState {
        String sessionId;
        String spectatorId;
        String cameraMode = "overview";
        String followFleetId;
        long joinTime;
        ScheduledFuture<?> refreshTask;

        SpectatorState(String sessionId, String spectatorId) {
            this.sessionId = sessionId;
            this.spectatorId = spectatorId;
            this.joinTime = System.currentTimeMillis();
        }
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        log.info("Spectator client connected: {}", session.getId());
        sendAvailableSessions(session);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        try {
            ObjectNode json = (ObjectNode) objectMapper.readTree(message.getPayload());
            String type = json.has("type") ? json.get("type").asText() : null;

            switch (type) {
                case "join":
                    handleJoin(session, json);
                    break;
                case "leave":
                    handleLeave(session);
                    break;
                case "camera_mode":
                    handleCameraMode(session, json);
                    break;
                case "follow_fleet":
                    handleFollowFleet(session, json);
                    break;
                case "list_sessions":
                    sendAvailableSessions(session);
                    break;
                default:
                    log.warn("Unknown spectator message type: {}", type);
            }
        } catch (Exception e) {
            log.error("Error handling spectator message: {}", e.getMessage(), e);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        SpectatorState state = spectatorStates.remove(session);
        if (state != null) {
            stopRefresh(state);
            spectatorService.leaveSession(state.spectatorId, state.sessionId);
            log.info("Spectator {} left session {}", state.spectatorId, state.sessionId);
        }
        log.info("Spectator client disconnected: {}", session.getId());
    }

    // ==================== Message Handlers ====================

    private void handleJoin(WebSocketSession session, ObjectNode json) {
        String sessionId = json.has("sessionId") ? json.get("sessionId").asText() : null;
        if (sessionId == null || sessionId.isBlank()) {
            sendError(session, "sessionId is required");
            return;
        }

        // Leave any existing session
        SpectatorState existing = spectatorStates.get(session);
        if (existing != null) {
            spectatorService.leaveSession(existing.spectatorId, existing.sessionId);
            stopRefresh(existing);
        }

        String spectatorId = "spectator_" + session.getId().substring(0, 8);
        Optional<SpectatorGameState> joinResult = spectatorService.joinSession(spectatorId, sessionId);

        if (!joinResult.isPresent()) {
            // Allow joining simulated sessions
            if (!sessionId.startsWith("demo-") && !sessionId.startsWith("spectator-demo")) {
                sendError(session, "Failed to join session: " + sessionId);
                return;
            }
        }

        SpectatorState state = new SpectatorState(sessionId, spectatorId);
        spectatorStates.put(session, state);

        // Send join confirmation
        ObjectNode confirm = objectMapper.createObjectNode();
        confirm.put("type", "spectator_joined");
        confirm.put("sessionId", sessionId);
        confirm.put("spectatorId", spectatorId);
        confirm.put("cameraMode", "overview");
        sendMessage(session, confirm);

        // Send initial game state
        sendGameState(session, state);

        // Send event history for catch-up
        sendEventHistory(session, state);

        // Send spectator list
        sendSpectatorList(session, state);

        // Start periodic game state refresh
        startRefresh(state, session);

        log.info("Spectator {} joined session {}", spectatorId, sessionId);
    }

    private void handleLeave(WebSocketSession session) {
        SpectatorState state = spectatorStates.get(session);
        if (state == null) return;

        spectatorService.leaveSession(state.spectatorId, state.sessionId);
        stopRefresh(state);
        spectatorStates.remove(session);

        ObjectNode message = objectMapper.createObjectNode();
        message.put("type", "spectator_left");
        message.put("sessionId", state.sessionId);
        sendMessage(session, message);

        log.info("Spectator {} left session {}", state.spectatorId, state.sessionId);
    }

    private void handleCameraMode(WebSocketSession session, ObjectNode json) {
        SpectatorState state = spectatorStates.get(session);
        if (state == null) return;

        String mode = json.has("mode") ? json.get("mode").asText() : "overview";
        state.cameraMode = mode;

        ObjectNode message = objectMapper.createObjectNode();
        message.put("type", "camera_mode_changed");
        message.put("mode", mode);
        sendMessage(session, message);
    }

    private void handleFollowFleet(WebSocketSession session, ObjectNode json) {
        SpectatorState state = spectatorStates.get(session);
        if (state == null) return;

        String fleetId = json.has("fleetId") ? json.get("fleetId").asText() : null;
        state.followFleetId = fleetId;
        state.cameraMode = fleetId != null ? "follow_fleet" : "overview";

        ObjectNode message = objectMapper.createObjectNode();
        message.put("type", "follow_fleet");
        message.put("fleetId", fleetId);
        message.put("cameraMode", state.cameraMode);
        sendMessage(session, message);
    }

    // ==================== Game State Refresh ====================

    private void startRefresh(SpectatorState state, WebSocketSession session) {
        stopRefresh(state);

        state.refreshTask = refreshScheduler.scheduleAtFixedRate(() -> {
            if (!spectatorStates.containsKey(session)) return;
            sendGameState(session, state);
        }, 0, 5, TimeUnit.SECONDS);
    }

    private void stopRefresh(SpectatorState state) {
        if (state.refreshTask != null) {
            state.refreshTask.cancel(false);
            state.refreshTask = null;
        }
    }

    private void sendGameState(WebSocketSession session, SpectatorState state) {
        try {
            ObjectNode message = objectMapper.createObjectNode();
            message.put("type", "game_state");
            message.put("cameraMode", state.cameraMode);
            if (state.followFleetId != null) {
                message.put("followFleetId", state.followFleetId);
            }
            message.put("timestamp", System.currentTimeMillis());

            // Try to get real game state
            Optional<SpectatorGameState> gameStateOpt = spectatorService.getSpectatorGameState(state.sessionId);
            if (gameStateOpt.isPresent()) {
                SpectatorGameState gameState = gameStateOpt.get();
                ObjectNode stateNode = message.putObject("gameState");
                stateNode.put("sessionId", gameState.getSessionId() != null ? gameState.getSessionId() : "");
                stateNode.put("turnNumber", gameState.getCurrentTurn());
                stateNode.put("fleetCount", gameState.getFleetCount());
                stateNode.put("playerCount", gameState.getPlayerCount());

                ArrayNode players = stateNode.putArray("players");
                if (gameState.getPlayers() != null) {
                    for (SpectatorPlayerSummary p : gameState.getPlayers()) {
                        ObjectNode player = players.addObject();
                        player.put("name", p.getName() != null ? p.getName() : "Unknown");
                        player.put("fleetCount", p.getFleetCount());
                        player.put("planetCount", p.getPlanetCount());
                    }
                }

                ArrayNode fleets = stateNode.putArray("fleets");
                if (gameState.getFleets() != null) {
                    for (SpectatorFleetSummary f : gameState.getFleets()) {
                        ObjectNode fleet = fleets.addObject();
                        fleet.put("id", f.getId() != null ? f.getId() : "unknown");
                        fleet.put("playerName", f.getPlayerName() != null ? f.getPlayerName() : "Unknown");
                        fleet.put("shipCount", f.getShipCount());
                        fleet.put("sector", f.getSector() != null ? f.getSector() : "unknown");
                    }
                }
            } else {
                // Generate simulated game state
                ObjectNode stateNode = message.putObject("gameState");
                stateNode.putAll(generateSimulatedGameState(state.sessionId));
            }

            sendMessage(session, message);
        } catch (Exception e) {
            log.warn("Failed to send game state: {}", e.getMessage());
        }
    }

    private void sendEventHistory(WebSocketSession session, SpectatorState state) {
        try {
            List<GameEvent> events = spectatorService.getEventHistory(state.sessionId, 50);

            ObjectNode message = objectMapper.createObjectNode();
            message.put("type", "event_history");
            message.put("sessionId", state.sessionId);

            ArrayNode eventsArray = message.putArray("events");
            for (GameEvent event : events) {
                ObjectNode eventNode = eventsArray.addObject();
                if (event.getType() != null) {
                    eventNode.put("eventType", event.getType().name());
                }
                if (event.getSessionId() != null) {
                    eventNode.put("sessionId", event.getSessionId());
                }
                if (event.getPayload() != null) {
                    eventNode.put("payload", event.getPayload().toString());
                }
            }

            sendMessage(session, message);
        } catch (Exception e) {
            log.warn("Failed to send event history: {}", e.getMessage());
        }
    }

    private void sendSpectatorList(WebSocketSession session, SpectatorState state) {
        try {
            ObjectNode message = objectMapper.createObjectNode();
            message.put("type", "spectator_list");
            message.put("sessionId", state.sessionId);

            ArrayNode spectatorArray = message.putArray("spectators");
            // Use connected spectator states as the list
            for (SpectatorState s : spectatorStates.values()) {
                if (s.sessionId.equals(state.sessionId)) {
                    spectatorArray.add(s.spectatorId);
                }
            }
            message.put("spectatorCount", spectatorArray.size());

            sendMessage(session, message);
        } catch (Exception e) {
            log.warn("Failed to send spectator list: {}", e.getMessage());
        }
    }

    private void sendAvailableSessions(WebSocketSession session) {
        try {
            ObjectNode message = objectMapper.createObjectNode();
            message.put("type", "available_sessions");

            ArrayNode sessions = message.putArray("sessions");

            // Add demo sessions for testing
            String[] demoSessions = {"demo-1", "demo-2", "spectator-demo-1"};
            for (String demoSession : demoSessions) {
                ObjectNode sessionNode = sessions.addObject();
                sessionNode.put("sessionId", demoSession);
                sessionNode.put("playerCount", 4);
                sessionNode.put("spectatorCount", 0);
                sessionNode.put("source", "simulated");
            }

            sendMessage(session, message);
        } catch (Exception e) {
            log.error("Failed to send available sessions: {}", e.getMessage());
        }
    }

    // ==================== Simulated Data ====================

    private ObjectNode generateSimulatedGameState(String sessionId) {
        Random random = new Random(sessionId.hashCode());
        ObjectNode state = objectMapper.createObjectNode();

        // Simulated players
        ArrayNode players = state.putArray("players");
        String[] playerNames = {"Commander Solo", "Admiral Thrawn", "Captain Janeway", "Lieutenant Data"};
        for (int i = 0; i < 4; i++) {
            ObjectNode player = players.addObject();
            player.put("id", String.valueOf(i + 1));
            player.put("name", playerNames[i]);
            player.put("fleetCount", random.nextInt(5) + 1);
            player.put("planetCount", random.nextInt(3) + 1);
        }

        // Simulated fleets
        ArrayNode fleets = state.putArray("fleets");
        String[] fleetNames = {"Alpha Fleet", "Bravo Fleet", "Charlie Fleet", "Delta Fleet"};
        for (int i = 0; i < 4; i++) {
            ObjectNode fleet = fleets.addObject();
            fleet.put("id", String.valueOf(i + 1));
            fleet.put("name", fleetNames[i]);
            fleet.put("x", random.nextDouble() * 100);
            fleet.put("y", random.nextDouble() * 100);
            fleet.put("z", random.nextDouble() * 100);
            fleet.put("shipCount", random.nextInt(10) + 1);
            fleet.put("health", random.nextDouble());
        }

        // Simulated battles
        ArrayNode battles = state.putArray("battles");
        if (random.nextBoolean()) {
            ObjectNode battle = battles.addObject();
            battle.put("id", "battle-1");
            battle.put("attacker", "Alpha Fleet");
            battle.put("defender", "Bravo Fleet");
            battle.put("x", random.nextDouble() * 100);
            battle.put("y", random.nextDouble() * 100);
            battle.put("z", random.nextDouble() * 100);
            battle.put("round", random.nextInt(10) + 1);
        }

        state.put("turnNumber", random.nextInt(50) + 1);
        return state;
    }

    // ==================== Message Senders ====================

    private void sendError(WebSocketSession session, String errorMessage) {
        try {
            ObjectNode message = objectMapper.createObjectNode();
            message.put("type", "error");
            message.put("message", errorMessage);
            sendMessage(session, message);
        } catch (Exception e) {
            log.error("Failed to send error: {}", e.getMessage());
        }
    }

    private void sendMessage(WebSocketSession session, ObjectNode message) {
        try {
            if (session.isOpen()) {
                session.sendMessage(new TextMessage(objectMapper.writeValueAsString(message)));
            }
        } catch (IOException e) {
            log.warn("Failed to send message to session {}: {}", session.getId(), e.getMessage());
        }
    }

    // ==================== Public API ====================

    /**
     * Get the number of connected spectators.
     */
    public int getSpectatorCount() {
        return spectatorStates.size();
    }

    /**
     * Get the list of active spectator sessions.
     */
    public Set<String> getActiveSpectatorSessions() {
        Set<String> sessions = new HashSet<>();
        for (SpectatorState state : spectatorStates.values()) {
            sessions.add(state.sessionId);
        }
        return sessions;
    }
}
