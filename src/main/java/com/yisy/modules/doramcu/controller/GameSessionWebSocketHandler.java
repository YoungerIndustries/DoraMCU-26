package com.yisy.modules.doramcu.controller;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.yisy.modules.doramcu.dto.GameEvent;
import com.yisy.modules.doramcu.dto.GameEvent.EventType;
import com.yisy.modules.doramcu.hex.Game;
import com.yisy.modules.doramcu.hex.Player;
import com.yisy.modules.doramcu.hex.chat.ChatMessage;
import com.yisy.modules.doramcu.model.GameSession;
import com.yisy.modules.doramcu.service.ChatService;
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
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * WebSocket handler for 4X game sessions providing bidirectional real-time communication.
 * <p>
 * Unlike the SSE {@link GameWebSocketController} which only pushes server-to-client events,
 * this handler allows clients to send actions (move, attack, build, etc.) and receive
 * game state updates in real-time. Each connected player has one WebSocket session.
 * </p>
 * <p>
 * Message protocol (JSON):
 * <ul>
 *   <li><b>Client â†’ Server:</b>
 *     <pre>{"action": "move", "sessionId": "abc", "playerId": 1, "data": {...}}</pre>
 *     Actions: move, attack, build, produce, trade, endTurn, chat, ping
 *     </pre>
 *   </li>
 *   <li><b>Server â†’ Client:</b>
 *     <pre>{"type": "ACTION_RESULT", "sessionId": "abc", "payload": {...}}</pre>
 *     Types: TURN_CHANGED, ACTION_RESULT, PLAYER_JOINED, PLAYER_LEFT, GAME_STATE, PONG
 *     </pre>
 *   </li>
 * </ul>
 * </p>
 * <p>
 * Connection URL: {@code ws://host:port/ws/v1/4x/session?sessionId=ABC&amp;playerId=1}
 * </p>
 */
@Component
public class GameSessionWebSocketHandler extends TextWebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(GameSessionWebSocketHandler.class);

    private final GameSessionService gameSessionService;
    private final ChatService chatService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /** Session ID â†’ list of connected WebSocket sessions. */
    private final ConcurrentHashMap<String, List<WebSocketSession>> sessionConnections = new ConcurrentHashMap<>();

    /** WebSocket session â†’ player ID mapping. */
    private final ConcurrentHashMap<WebSocketSession, Long> sessionPlayerIds = new ConcurrentHashMap<>();

    /** WebSocket session â†’ game session ID mapping. */
    private final ConcurrentHashMap<WebSocketSession, String> sessionGameIds = new ConcurrentHashMap<>();

    public GameSessionWebSocketHandler(GameSessionService gameSessionService) {
        this(gameSessionService, new ChatService());
    }

    public GameSessionWebSocketHandler(GameSessionService gameSessionService, ChatService chatService) {
        this.gameSessionService = gameSessionService;
        this.chatService = chatService;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession wsSession) throws Exception {
        String sessionId = getQueryParam(wsSession, "sessionId");
        String playerIdParam = getQueryParam(wsSession, "playerId");

        if (sessionId == null || sessionId.isEmpty()) {
            log.warn("WebSocket connection without sessionId â€” closing");
            sendError(wsSession, "sessionId query parameter is required");
            wsSession.close(CloseStatus.BAD_DATA);
            return;
        }

        if (playerIdParam == null || playerIdParam.isEmpty()) {
            log.warn("WebSocket connection without playerId â€” closing");
            sendError(wsSession, "playerId query parameter is required");
            wsSession.close(CloseStatus.BAD_DATA);
            return;
        }

        long playerId;
        try {
            playerId = Long.parseLong(playerIdParam);
        } catch (NumberFormatException e) {
            log.warn("Invalid playerId parameter: {}", playerIdParam);
            sendError(wsSession, "Invalid playerId: " + playerIdParam);
            wsSession.close(CloseStatus.BAD_DATA);
            return;
        }

        GameSession gameSession = gameSessionService.getGameSession(sessionId);
        if (gameSession == null) {
            log.warn("WebSocket connection to non-existent session: {}", sessionId);
            sendError(wsSession, "Game session not found: " + sessionId);
            wsSession.close(CloseStatus.POLICY_VIOLATION);
            return;
        }

        // Register this WebSocket connection
        sessionPlayerIds.put(wsSession, playerId);
        sessionGameIds.put(wsSession, sessionId);
        sessionConnections.computeIfAbsent(sessionId, k -> new CopyOnWriteArrayList<>()).add(wsSession);

        log.info("Player {} connected to game session {} via WebSocket (total: {})",
                playerId, sessionId, sessionConnections.getOrDefault(sessionId, Collections.emptyList()).size());

        // Send connection confirmation
        sendAck(wsSession, "CONNECTED", "Connected to game session: " + sessionId);

        // Broadcast player joined event to all players in this session
        GameEvent joinEvent = new GameEvent(EventType.PLAYER_JOINED, sessionId,
                Map.of("playerId", playerId, "message", "Player " + playerId + " joined"));
        broadcastToSession(sessionId, joinEvent);

        // Post system message for player joining
        try {
            chatService.postSystemMessage(sessionId, "Player " + playerId + " joined the game", null);
        } catch (Exception e) {
            log.debug("Failed to post join system message: {}", e.getMessage());
        }

        // Send current game state to the new player
        sendGameState(wsSession, gameSession);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession wsSession, CloseStatus status) {
        Long playerId = sessionPlayerIds.remove(wsSession);
        String sessionId = sessionGameIds.remove(wsSession);

        if (sessionId != null && playerId != null) {
            List<WebSocketSession> connections = sessionConnections.get(sessionId);
            if (connections != null) {
                connections.remove(wsSession);
                if (connections.isEmpty()) {
                    sessionConnections.remove(sessionId);
                }
            }

            log.info("Player {} disconnected from game session {} (status: {})",
                    playerId, sessionId, status);

            // Broadcast player left event
            GameEvent leaveEvent = new GameEvent(EventType.PLAYER_LEFT, sessionId,
                    Map.of("playerId", playerId, "message", "Player " + playerId + " left"));
            broadcastToSession(sessionId, leaveEvent);

            // Post system message for player leaving
            try {
                chatService.postSystemMessage(sessionId, "Player " + playerId + " left the game", null);
            } catch (Exception e) {
                log.debug("Failed to post leave system message: {}", e.getMessage());
            }
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession wsSession, TextMessage message) {
        String payload = message.getPayload();
        Long playerId = sessionPlayerIds.get(wsSession);
        String sessionId = sessionGameIds.get(wsSession);

        if (playerId == null || sessionId == null) {
            log.warn("Received message from unregistered session");
            return;
        }

        try {
            JsonNode json = objectMapper.readTree(payload);
            String action = json.has("action") ? json.get("action").asText() : null;

            if (action == null) {
                sendError(wsSession, "Missing 'action' field in message");
                return;
            }

            log.debug("Player {} in session {} action: {}", playerId, sessionId, action);

            // Dispatch action
            Object result = dispatchAction(action, json, sessionId, playerId);

            // Send action result back to the player
            GameEvent resultEvent = new GameEvent(EventType.ACTION_RESULT, sessionId,
                    Map.of("playerId", playerId, "action", action, "result", result));
            sendEvent(wsSession, resultEvent);

        } catch (JsonProcessingException e) {
            log.warn("Invalid JSON from player {}: {}", playerId, e.getMessage());
            sendError(wsSession, "Invalid JSON: " + e.getMessage());
        } catch (Exception e) {
            log.error("Error handling message from player {}: {}", playerId, e.getMessage(), e);
            sendError(wsSession, "Internal error: " + e.getMessage());
        }
    }

    @Override
    public void handleTransportError(WebSocketSession wsSession, Throwable exception) {
        Long playerId = sessionPlayerIds.get(wsSession);
        String sessionId = sessionGameIds.get(wsSession);
        log.error("Transport error for player {} in session {}: {}",
                playerId, sessionId, exception.getMessage(), exception);
    }

    // ============================================================
    // Action Dispatch
    // ============================================================

    /**
     * Dispatches a client action to the appropriate handler.
     *
     * @param action    the action type
     * @param json      the full JSON message
     * @param sessionId the game session ID
     * @param playerId  the player ID
     * @return the result of the action
     */
    private Object dispatchAction(String action, JsonNode json, String sessionId, long playerId) {
        GameSession gameSession = gameSessionService.getGameSession(sessionId);
        if (gameSession == null) {
            return Map.of("success", false, "error", "Session not found");
        }

        return switch (action.toLowerCase()) {
            case "ping" -> handlePing();
            case "moveto", "move" -> handleMove(json, gameSession, playerId);
            case "attack" -> handleAttack(json, gameSession, playerId);
            case "build" -> handleBuild(json, gameSession, playerId);
            case "produce" -> handleProduce(json, gameSession, playerId);
            case "trade" -> handleTrade(json, gameSession, playerId);
            case "endturn" -> handleEndTurn(gameSession, playerId);
            case "chat" -> handleChat(json, sessionId, playerId);
            case "state" -> handleStateRequest(gameSession);
            default -> Map.of("success", false, "error", "Unknown action: " + action);
        };
    }

    // ============================================================
    // Action Handlers
    // ============================================================

    private Map<String, Object> handlePing() {
        return Map.of("pong", true, "serverTime", System.currentTimeMillis());
    }

    private Map<String, Object> handleMove(JsonNode json, GameSession gameSession, long playerId) {
        Map<String, Object> result = new HashMap<>();
        result.put("action", "move");
        result.put("playerId", playerId);

        // Extract movement data
        JsonNode data = json.has("data") ? json.get("data") : null;
        if (data == null) {
            result.put("success", false);
            result.put("error", "Missing data field");
            return result;
        }

        // Record the move action for the game session
        // The actual movement logic is handled by the game engine
        result.put("success", true);
        result.put("message", "Move action received");
        result.put("timestamp", System.currentTimeMillis());

        // Broadcast move to other players
        String sessionId = gameSession.getSessionId();
        GameEvent moveEvent = new GameEvent(EventType.ACTION_RESULT, sessionId, result);
        broadcastToSessionExcept(sessionId, moveEvent, playerId);

        return result;
    }

    private Map<String, Object> handleAttack(JsonNode json, GameSession gameSession, long playerId) {
        Map<String, Object> result = new HashMap<>();
        result.put("action", "attack");
        result.put("playerId", playerId);

        JsonNode data = json.has("data") ? json.get("data") : null;
        if (data == null) {
            result.put("success", false);
            result.put("error", "Missing data field");
            return result;
        }

        result.put("success", true);
        result.put("message", "Attack action received");
        result.put("timestamp", System.currentTimeMillis());

        String sessionId = gameSession.getSessionId();
        GameEvent attackEvent = new GameEvent(EventType.ACTION_RESULT, sessionId, result);
        broadcastToSessionExcept(sessionId, attackEvent, playerId);

        return result;
    }

    private Map<String, Object> handleBuild(JsonNode json, GameSession gameSession, long playerId) {
        Map<String, Object> result = new HashMap<>();
        result.put("action", "build");
        result.put("playerId", playerId);

        JsonNode data = json.has("data") ? json.get("data") : null;
        if (data == null) {
            result.put("success", false);
            result.put("error", "Missing data field");
            return result;
        }

        result.put("success", true);
        result.put("message", "Build action received");
        result.put("timestamp", System.currentTimeMillis());

        return result;
    }

    private Map<String, Object> handleProduce(JsonNode json, GameSession gameSession, long playerId) {
        Map<String, Object> result = new HashMap<>();
        result.put("action", "produce");
        result.put("playerId", playerId);

        JsonNode data = json.has("data") ? json.get("data") : null;
        if (data == null) {
            result.put("success", false);
            result.put("error", "Missing data field");
            return result;
        }

        result.put("success", true);
        result.put("message", "Produce action received");
        result.put("timestamp", System.currentTimeMillis());

        return result;
    }

    private Map<String, Object> handleTrade(JsonNode json, GameSession gameSession, long playerId) {
        Map<String, Object> result = new HashMap<>();
        result.put("action", "trade");
        result.put("playerId", playerId);

        JsonNode data = json.has("data") ? json.get("data") : null;
        if (data == null) {
            result.put("success", false);
            result.put("error", "Missing data field");
            return result;
        }

        result.put("success", true);
        result.put("message", "Trade action received");
        result.put("timestamp", System.currentTimeMillis());

        return result;
    }

    private Map<String, Object> handleEndTurn(GameSession gameSession, long playerId) {
        Map<String, Object> result = new HashMap<>();
        result.put("action", "endTurn");
        result.put("playerId", playerId);
        result.put("success", true);
        result.put("message", "Turn ended");
        result.put("timestamp", System.currentTimeMillis());

        // Broadcast turn change to all players
        String sessionId = gameSession.getSessionId();
        GameEvent turnEvent = new GameEvent(EventType.TURN_CHANGED, sessionId, result);
        broadcastToSession(sessionId, turnEvent);

        return result;
    }

    private Map<String, Object> handleChat(JsonNode json, String sessionId, long playerId) {
        Map<String, Object> result = new HashMap<>();
        result.put("action", "chat");
        result.put("playerId", playerId);

        JsonNode data = json.has("data") ? json.get("data") : null;
        String content = (data != null && data.has("message")) ? data.get("message").asText() : "";
        String channelName = (data != null && data.has("channel")) ? data.get("channel").asText() : "general";
        String messageType = (data != null && data.has("type")) ? data.get("type").asText() : "text";
        long targetPlayerId = (data != null && data.has("targetPlayerId")) ? data.get("targetPlayerId").asLong() : -1;

        // Persist the message via ChatService
        ChatMessage chatMessage;
        try {
            if ("whisper".equalsIgnoreCase(messageType) && targetPlayerId > 0) {
                chatMessage = chatService.sendWhisper(sessionId, playerId, null, targetPlayerId, content);
            } else {
                com.yisy.modules.doramcu.hex.chat.ChatMessage.Channel channel =
                        com.yisy.modules.doramcu.hex.chat.ChatMessage.Channel.fromName(channelName);
                chatMessage = chatService.sendMessage(sessionId, playerId, null, content, channel);
            }
        } catch (IllegalArgumentException e) {
            result.put("success", false);
            result.put("error", e.getMessage());
            return result;
        }

        result.put("message", chatMessage.getContent());
        result.put("channel", chatMessage.getChannel().getName());
        result.put("type", chatMessage.getType().name());
        result.put("timestamp", chatMessage.getTimestamp().toEpochMilli());
        result.put("senderId", chatMessage.getSenderId());
        result.put("senderName", chatMessage.getSenderName());

        // Broadcast chat to all players in the session
        GameEvent chatEvent = new GameEvent(EventType.ACTION_RESULT, sessionId, result);
        broadcastToSession(sessionId, chatEvent);

        return result;
    }

    private Map<String, Object> handleStateRequest(GameSession gameSession) {
        Map<String, Object> state = new HashMap<>();
        state.put("sessionId", gameSession.getSessionId());
        state.put("name", gameSession.getName());
        state.put("playerCount", gameSession.getPlayerIds().size());
        state.put("timestamp", System.currentTimeMillis());

        if (gameSession.getGame() instanceof Game game) {
            state.put("currentTurn", game.getCurrentTurn());
            state.put("playerCount", game.getActivePlayerCount());
        }

        return state;
    }

    // ============================================================
    // Broadcasting
    // ============================================================

    /**
     * Send a game event to a specific WebSocket session.
     */
    private void sendEvent(WebSocketSession wsSession, GameEvent event) {
        try {
            String json = objectMapper.writeValueAsString(event);
            wsSession.sendMessage(new TextMessage(json));
        } catch (IOException e) {
            log.error("Failed to send event to session: {}", e.getMessage());
        }
    }

    /**
     * Broadcast a game event to all WebSocket sessions in a game session.
     */
    public void broadcastToSession(String sessionId, GameEvent event) {
        List<WebSocketSession> connections = sessionConnections.get(sessionId);
        if (connections == null || connections.isEmpty()) {
            log.debug("No WebSocket connections for session {}", sessionId);
            return;
        }

        try {
            String json = objectMapper.writeValueAsString(event);
            TextMessage message = new TextMessage(json);

            // Collect sessions to remove separately since CopyOnWriteArrayList iterator doesn't support remove()
            List<WebSocketSession> toRemove = new ArrayList<>();
            for (WebSocketSession wsSession : connections) {
                try {
                    if (wsSession.isOpen()) {
                        wsSession.sendMessage(message);
                    } else {
                        toRemove.add(wsSession);
                    }
                } catch (IOException e) {
                    log.warn("Failed to broadcast to session: {}", e.getMessage());
                    toRemove.add(wsSession);
                }
            }
            // Remove closed sessions after iteration
            if (!toRemove.isEmpty()) {
                connections.removeAll(toRemove);
            }
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize event: {}", e.getMessage());
        }
    }

    /**
     * Broadcast a game event to all WebSocket sessions in a game session except the sender.
     */
    private void broadcastToSessionExcept(String sessionId, GameEvent event, long excludePlayerId) {
        List<WebSocketSession> connections = sessionConnections.get(sessionId);
        if (connections == null || connections.isEmpty()) {
            return;
        }

        try {
            String json = objectMapper.writeValueAsString(event);
            TextMessage message = new TextMessage(json);

            // Collect sessions to remove separately since CopyOnWriteArrayList iterator doesn't support remove()
            List<WebSocketSession> toRemove = new ArrayList<>();
            for (WebSocketSession wsSession : connections) {
                Long playerId = sessionPlayerIds.get(wsSession);
                if (playerId != null && playerId == excludePlayerId) {
                    continue; // Skip the sender
                }
                try {
                    if (wsSession.isOpen()) {
                        wsSession.sendMessage(message);
                    } else {
                        toRemove.add(wsSession);
                    }
                } catch (IOException e) {
                    log.warn("Failed to broadcast to session: {}", e.getMessage());
                    toRemove.add(wsSession);
                }
            }
            // Remove closed sessions after iteration
            if (!toRemove.isEmpty()) {
                connections.removeAll(toRemove);
            }
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize event: {}", e.getMessage());
        }
    }

    /**
     * Send the current game state to a player.
     */
    private void sendGameState(WebSocketSession wsSession, GameSession gameSession) {
        Map<String, Object> state = new HashMap<>();
        state.put("sessionId", gameSession.getSessionId());
        state.put("name", gameSession.getName());
        state.put("playerIds", new ArrayList<>(gameSession.getPlayerIds()));

        if (gameSession.getGame() instanceof Game game) {
            state.put("currentTurn", game.getCurrentTurn());
            state.put("currentPlayerIndex", game.getCurrentPlayerIndex());
            state.put("activePlayerCount", game.getActivePlayerCount());
            state.put("planetCount", game.getPlanets().size());
            state.put("fleetCount", game.getFleets().size());
            state.put("empireCount", game.getEmpires().size());
        }

        GameEvent stateEvent = new GameEvent(EventType.GAME_STATE, gameSession.getSessionId(), state);
        sendEvent(wsSession, stateEvent);
    }

    // ============================================================
    // Message Helpers
    // ============================================================

    private void sendAck(WebSocketSession wsSession, String actionType, String message) {
        ObjectNode ack = objectMapper.createObjectNode();
        ack.put("type", "ACK");
        ack.put("actionType", actionType);
        ack.put("message", message);
        ack.put("timestamp", System.currentTimeMillis());
        try {
            wsSession.sendMessage(new TextMessage(objectMapper.writeValueAsString(ack)));
        } catch (IOException e) {
            log.error("Failed to send ACK: {}", e.getMessage());
        }
    }

    private void sendError(WebSocketSession wsSession, String message) {
        ObjectNode error = objectMapper.createObjectNode();
        error.put("type", "ERROR");
        error.put("message", message);
        error.put("timestamp", System.currentTimeMillis());
        try {
            wsSession.sendMessage(new TextMessage(objectMapper.writeValueAsString(error)));
        } catch (IOException e) {
            log.error("Failed to send error: {}", e.getMessage());
        }
    }

    // ============================================================
    // Utilities
    // ============================================================

    private String getQueryParam(WebSocketSession wsSession, String name) {
        String query = wsSession.getUri().getQuery();
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

    // ============================================================
    // Management Methods (for testing and monitoring)
    // ============================================================

    /**
     * Get the number of connected WebSocket sessions for a game session.
     */
    public int getConnectionCount(String sessionId) {
        List<WebSocketSession> connections = sessionConnections.get(sessionId);
        return connections != null ? connections.size() : 0;
    }

    /**
     * Get the list of game session IDs with active WebSocket connections.
     */
    public List<String> getActiveSessionIds() {
        return new ArrayList<>(sessionConnections.keySet());
    }

    /**
     * Get the total number of active WebSocket connections across all sessions.
     */
    public int getTotalConnectionCount() {
        return sessionConnections.values().stream().mapToInt(List::size).sum();
    }
}
