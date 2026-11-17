package com.yisy.modules.doramcu.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.yisy.modules.doramcu.dto.session.GameStateResponse;
import com.yisy.modules.doramcu.dto.players.PlayerSummary;
import com.yisy.modules.doramcu.gamecore.GameType;
import com.yisy.modules.doramcu.model.session.GameSession;
import com.yisy.modules.doramcu.service.GameSessionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * GameStateWebSocketHandler — Generic WebSocket handler for any game session.
 *
 * WebSocket endpoint: /ws/game-state
 *
 * Provides real-time game state synchronization for the unified game shell.
 * Unlike specialized handlers (FleetTracker, Presence, etc.), this handler
 * routes messages to players in the same game session, enabling multi-player
 * sync for any game type.
 *
 * Message types (server → client):
 * - game_state: Full game state snapshot (on join or request)
 * - action_result: Result of a player action
 * - event_notification: In-game event (combat, discovery, etc.)
 * - player_update: Player join/leave/status change
 * - turn_update: Turn change notification
 * - error: Error message
 *
 * Client messages:
 * - join: {"type":"join","sessionId":"...","playerId":"..."}
 * - leave: {"type":"leave"}
 * - action: {"type":"action","action":"...","params":{...}}
 * - ping/pong: Health check
 * - state_request: {"type":"state_request"} — request current state
 */
public class GameStateWebSocketHandler extends TextWebSocketHandler {
    private static final Logger log = LoggerFactory.getLogger(GameStateWebSocketHandler.class);

    private final GameSessionService gameSessionService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // All connected WebSocket sessions
    private final Set<WebSocketSession> sessions = ConcurrentHashMap.newKeySet();

    // Maps WebSocket session → game session ID
    private final Map<WebSocketSession, String> sessionGameMap = new ConcurrentHashMap<>();

    // Maps WebSocket session → player ID
    private final Map<WebSocketSession, Long> sessionPlayerMap = new ConcurrentHashMap<>();

    // Maps game session ID → set of WebSocket sessions
    private final Map<String, Set<WebSocketSession>> gameSessionPlayers = new ConcurrentHashMap<>();

    public GameStateWebSocketHandler(GameSessionService gameSessionService) {
        this.gameSessionService = gameSessionService;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessions.add(session);
        log.info("Game state WebSocket connected: {}", session.getId());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        // Handle player leaving the game session
        String gameId = sessionGameMap.remove(session);
        Long playerId = sessionPlayerMap.remove(session);
        if (gameId != null) {
            Set<WebSocketSession> players = gameSessionPlayers.get(gameId);
            if (players != null) {
                players.remove(session);
                if (players.isEmpty()) {
                    gameSessionPlayers.remove(gameId);
                }
            }
            // Broadcast player left
            broadcastToSession(gameId, createPlayerUpdate("player_left", playerId, gameId));
            log.info("Player {} left game session {} via WebSocket", playerId, gameId);
        }
        sessions.remove(session);
        log.info("Game state WebSocket disconnected: {}", session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        try {
            JsonNode json = objectMapper.readTree(message.getPayload());
            String type = json.get("type").asText("");

            switch (type) {
                case "join":
                    handleJoin(session, json);
                    break;
                case "leave":
                    handleLeave(session);
                    break;
                case "action":
                    handleAction(session, json);
                    break;
                case "state_request":
                    handleStateRequest(session);
                    break;
                case "ping":
                    sendPong(session);
                    break;
                default:
                    sendError(session, "Unknown message type: " + type);
            }
        } catch (Exception e) {
            log.error("Error handling game state message: {}", e.getMessage());
            sendError(session, "Error: " + e.getMessage());
        }
    }

    /**
     * Handle a player joining a game session.
     */
    private void handleJoin(WebSocketSession session, JsonNode json) {
        String gameId = json.get("sessionId").asText("");
        String playerIdStr = json.get("playerId").asText("");

        if (gameId.isEmpty() || playerIdStr.isEmpty()) {
            sendError(session, "sessionId and playerId are required");
            return;
        }

        Long playerId;
        try {
            playerId = Long.parseLong(playerIdStr);
        } catch (NumberFormatException e) {
            sendError(session, "Invalid playerId: " + playerIdStr);
            return;
        }

        // Check if session exists
        try {
            GameSession gs = gameSessionService.getSession(gameId);
            if (gs == null) {
                sendError(session, "Game session not found: " + gameId);
                return;
            }
        } catch (Exception e) {
            sendError(session, "Game session not found: " + gameId);
            return;
        }

        // Register session mappings
        sessionGameMap.put(session, gameId);
        sessionPlayerMap.put(session, playerId);
        gameSessionPlayers.computeIfAbsent(gameId, k -> ConcurrentHashMap.newKeySet()).add(session);

        // Send full game state snapshot
        sendGameState(session, gameId, playerId);

        // Broadcast player joined to others in the session
        broadcastToSession(gameId, createPlayerUpdate("player_joined", playerId, gameId));

        log.info("Player {} joined game session {} via WebSocket", playerId, gameId);
    }

    /**
     * Handle a player leaving the current game session.
     */
    private void handleLeave(WebSocketSession session) {
        String gameId = sessionGameMap.get(session);
        Long playerId = sessionPlayerMap.get(session);
        if (gameId != null) {
            sessionGameMap.remove(session);
            sessionPlayerMap.remove(session);
            Set<WebSocketSession> players = gameSessionPlayers.get(gameId);
            if (players != null) {
                players.remove(session);
                if (players.isEmpty()) {
                    gameSessionPlayers.remove(gameId);
                }
            }
            broadcastToSession(gameId, createPlayerUpdate("player_left", playerId, gameId));
        }
    }

    /**
     * Handle a player action within the game session.
     */
    private void handleAction(WebSocketSession session, JsonNode json) {
        String gameId = sessionGameMap.get(session);
        Long playerId = sessionPlayerMap.get(session);

        if (gameId == null || playerId == null) {
            sendError(session, "Must join a session before taking actions");
            return;
        }

        String action = json.get("action").asText("");
        JsonNode params = json.has("params") ? json.get("params") : objectMapper.createObjectNode();

        try {
            // Delegate action to GameSessionService based on action type
            GameStateResponse response = dispatchAction(gameId, playerId, action, params);

            // Send action result to the acting player
            ObjectNode resultMsg = objectMapper.createObjectNode();
            resultMsg.put("type", "action_result");
            resultMsg.put("sessionId", gameId);
            resultMsg.put("action", action);
            resultMsg.put("success", response != null);
            if (response != null) {
                // Include key state changes
                ObjectNode stateChanges = resultMsg.objectNode();
                stateChanges.put("currentTurn", response.getCurrentTurn());
                stateChanges.put("isMyTurn", response.isMyTurn());
                if (response.getEmpireSummary() != null) {
                    stateChanges.set("empire", objectMapper.valueToTree(response.getEmpireSummary()));
                }
                resultMsg.set("state", stateChanges);
            }
            sendMessage(session, resultMsg);

            // Broadcast event notification to other players in the session
            ObjectNode eventMsg = objectMapper.createObjectNode();
            eventMsg.put("type", "event_notification");
            eventMsg.put("sessionId", gameId);
            eventMsg.put("eventType", "player_action");
            eventMsg.put("playerId", playerId);
            eventMsg.put("action", action);
            eventMsg.put("timestamp", System.currentTimeMillis());
            broadcastToSessionExcept(gameId, session, eventMsg);

            log.debug("Player {} executed action '{}' in session {}", playerId, action, gameId);
        } catch (Exception e) {
            log.error("Error dispatching action '{}' for player {}: {}", action, playerId, e.getMessage());
            ObjectNode resultMsg = objectMapper.createObjectNode();
            resultMsg.put("type", "action_result");
            resultMsg.put("action", action);
            resultMsg.put("success", false);
            resultMsg.put("error", e.getMessage());
            sendMessage(session, resultMsg);
        }
    }

    /**
     * Dispatch an action to the GameSessionService.
     * Returns the updated game state or null if the action is not recognized.
     */
    private GameStateResponse dispatchAction(String gameId, Long playerId, String action, JsonNode params) {
        try {
            switch (action.toUpperCase()) {
                case "END_TURN":
                    return gameSessionService.advanceTurn(playerId, gameId);
                case "EXPLORE":
                    // Simplified explore action
                    return gameSessionService.getGameState(playerId, gameId);
                case "MOVE_FLEET":
                    // Simplified fleet move
                    return gameSessionService.getGameState(playerId, gameId);
                default:
                    // For unrecognized actions, return current state
                    log.warn("Unrecognized action: {}", action);
                    return gameSessionService.getGameState(playerId, gameId);
            }
        } catch (Exception e) {
            log.error("Error in action dispatch: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Handle a request for the current game state.
     */
    private void handleStateRequest(WebSocketSession session) {
        String gameId = sessionGameMap.get(session);
        Long playerId = sessionPlayerMap.get(session);
        if (gameId == null || playerId == null) {
            sendError(session, "Must join a session before requesting state");
            return;
        }
        sendGameState(session, gameId, playerId);
    }

    /**
     * Send full game state snapshot to a client.
     */
    private void sendGameState(WebSocketSession session, String gameId, Long playerId) {
        try {
            GameStateResponse state = gameSessionService.getGameState(playerId, gameId);
            if (state == null) {
                sendError(session, "Failed to retrieve game state");
                return;
            }

            ObjectNode msg = objectMapper.createObjectNode();
            msg.put("type", "game_state");
            msg.put("sessionId", gameId);

            // Add game type from session
            try {
                GameSession gs = gameSessionService.getSession(gameId);
                if (gs != null && gs.getGame() != null) {
                    msg.put("gameType", gs.getGame().getGameType().name());
                }
            } catch (Exception e) {
                // Game type may not be available
            }

            // Convert GameStateResponse to JSON
            ObjectNode stateNode = msg.objectNode();
            stateNode.put("currentTurn", state.getCurrentTurn());
            stateNode.put("currentPlayerId", state.getCurrentPlayerId());
            stateNode.put("currentPlayerName", state.getCurrentPlayerName());
            stateNode.put("isMyTurn", state.isMyTurn());
            stateNode.put("turnIntervalSeconds", state.getTurnIntervalSeconds());
            stateNode.put("accumulatedSeconds", state.getAccumulatedSeconds());
            stateNode.put("schedulerEnabled", state.isSchedulerEnabled());

            // Players
            ArrayNode playersNode = msg.arrayNode();
            if (state.getPlayers() != null) {
                for (PlayerSummary p : state.getPlayers()) {
                    ObjectNode pNode = playersNode.objectNode();
                    pNode.put("playerId", p.getId());
                    pNode.put("name", p.getName());
                    pNode.put("isAi", p.isAi());
                    playersNode.add(pNode);
                }
            }
            msg.set("players", playersNode);

            // Empire summary
            if (state.getEmpireSummary() != null) {
                msg.set("empire", objectMapper.valueToTree(state.getEmpireSummary()));
            }

            // Fleets
            if (state.getFleets() != null) {
                msg.set("fleets", objectMapper.valueToTree(state.getFleets()));
            }

            // Active battles
            if (state.getActiveBattles() != null) {
                msg.set("battles", objectMapper.valueToTree(state.getActiveBattles()));
            }

            // Pipeline summary
            if (state.getPipelineSummary() != null) {
                msg.set("pipeline", objectMapper.valueToTree(state.getPipelineSummary()));
            }

            // Commander (RPG)
            if (state.getCommander() != null) {
                msg.set("commander", objectMapper.valueToTree(state.getCommander()));
            }

            msg.put("timestamp", System.currentTimeMillis());

            sendMessage(session, msg);
        } catch (Exception e) {
            log.error("Error sending game state for session {}: {}", gameId, e.getMessage());
            sendError(session, "Error retrieving game state: " + e.getMessage());
        }
    }

    /**
     * Create a player update message.
     */
    private ObjectNode createPlayerUpdate(String eventType, Long playerId, String gameId) {
        ObjectNode msg = objectMapper.createObjectNode();
        msg.put("type", "player_update");
        msg.put("sessionId", gameId);
        msg.put("eventType", eventType);
        if (playerId != null) {
            msg.put("playerId", playerId);
        }
        msg.put("timestamp", System.currentTimeMillis());
        return msg;
    }

    /**
     * Broadcast a turn update to all players in a session.
     */
    public void broadcastTurnUpdate(String gameId, int turn, Long currentPlayerId) {
        ObjectNode msg = objectMapper.createObjectNode();
        msg.put("type", "turn_update");
        msg.put("sessionId", gameId);
        msg.put("currentTurn", turn);
        if (currentPlayerId != null) {
            msg.put("currentPlayerId", currentPlayerId);
        }
        msg.put("timestamp", System.currentTimeMillis());
        broadcastToSession(gameId, msg);
    }

    /**
     * Broadcast an event notification to all players in a session.
     */
    public void broadcastEvent(String gameId, String eventType, Map<String, Object> data) {
        ObjectNode msg = objectMapper.createObjectNode();
        msg.put("type", "event_notification");
        msg.put("sessionId", gameId);
        msg.put("eventType", eventType);
        msg.put("timestamp", System.currentTimeMillis());
        if (data != null) {
            data.forEach((key, value) -> {
                if (value instanceof String) {
                    msg.put(key, (String) value);
                } else if (value instanceof Number) {
                    msg.put(key, ((Number) value).doubleValue());
                } else if (value instanceof Boolean) {
                    msg.put(key, (Boolean) value);
                } else {
                    msg.put(key, value.toString());
                }
            });
        }
        broadcastToSession(gameId, msg);
    }

    /**
     * Send a message to a specific session.
     */
    private void sendMessage(WebSocketSession session, ObjectNode message) {
        try {
            if (session.isOpen()) {
                session.sendMessage(new TextMessage(objectMapper.writeValueAsString(message)));
            }
        } catch (IOException e) {
            log.error("Error sending message to session {}: {}", session.getId(), e.getMessage());
        }
    }

    /**
     * Broadcast a message to all players in a game session.
     */
    private void broadcastToSession(String gameId, ObjectNode message) {
        Set<WebSocketSession> players = gameSessionPlayers.get(gameId);
        if (players == null) return;
        String json;
        try {
            json = objectMapper.writeValueAsString(message);
        } catch (Exception e) {
            log.error("Error serializing broadcast message: {}", e.getMessage());
            return;
        }
        TextMessage textMessage = new TextMessage(json);
        players.forEach(s -> {
            try {
                if (s.isOpen()) {
                    s.sendMessage(textMessage);
                }
            } catch (IOException e) {
                log.error("Error broadcasting to session {}: {}", s.getId(), e.getMessage());
            }
        });
    }

    /**
     * Broadcast to all players in a session except one.
     */
    private void broadcastToSessionExcept(String gameId, WebSocketSession exclude, ObjectNode message) {
        Set<WebSocketSession> players = gameSessionPlayers.get(gameId);
        if (players == null) return;
        String json;
        try {
            json = objectMapper.writeValueAsString(message);
        } catch (Exception e) {
            log.error("Error serializing broadcast message: {}", e.getMessage());
            return;
        }
        TextMessage textMessage = new TextMessage(json);
        players.stream().filter(s -> !s.equals(exclude)).forEach(s -> {
            try {
                if (s.isOpen()) {
                    s.sendMessage(textMessage);
                }
            } catch (IOException e) {
                log.error("Error broadcasting to session {}: {}", s.getId(), e.getMessage());
            }
        });
    }

    /**
     * Send pong response.
     */
    private void sendPong(WebSocketSession session) {
        ObjectNode response = objectMapper.createObjectNode();
        response.put("type", "pong");
        response.put("timestamp", System.currentTimeMillis());
        sendMessage(session, response);
    }

    /**
     * Send error message.
     */
    private void sendError(WebSocketSession session, String error) {
        ObjectNode response = objectMapper.createObjectNode();
        response.put("type", "error");
        response.put("message", error);
        sendMessage(session, response);
    }

    /**
     * Get the number of connected sessions.
     */
    public int getConnectedSessions() {
        return sessions.size();
    }

    /**
     * Get the number of players in a game session.
     */
    public int getPlayerCount(String gameId) {
        Set<WebSocketSession> players = gameSessionPlayers.get(gameId);
        return players == null ? 0 : players.size();
    }

    /**
     * Get list of active game sessions with WebSocket players.
     */
    public List<Map<String, Object>> getActiveSessions() {
        return gameSessionPlayers.entrySet().stream().map(entry -> {
            Map<String, Object> info = new HashMap<>();
            info.put("sessionId", entry.getKey());
            info.put("playerCount", entry.getValue().size());
            return info;
        }).collect(Collectors.toList());
    }
}
