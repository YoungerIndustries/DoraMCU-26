package com.yisy.modules.doraneuron.ws;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket handler for game state synchronization between game nodes and the server.
 *
 * Supports the following message types:
 * - STATE_UPDATE: Client sends state changes to the server
 * - STATE_REQUEST: Client requests the current state from the server
 * - SYNC_ACK: Server acknowledges a state update
 * - SYNC_ERROR: Server reports an error
 */
public class GameSyncWebSocketHandler extends TextWebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(GameSyncWebSocketHandler.class);

    private final ObjectMapper objectMapper = new ObjectMapper();

    // Track active sessions by playerId
    private final ConcurrentHashMap<String, WebSocketSession> activeSessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String playerId = extractPlayerId(session);
        if (playerId != null) {
            activeSessions.put(playerId, session);
            log.info("Game sync connection established for player: {} (sessions: {})",
                    playerId, activeSessions.size());
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        try {
            Map<String, Object> payload = objectMapper.readValue(message.getPayload(), Map.class);
            String type = (String) payload.get("type");
            String playerId = (String) payload.get("playerId");
            String sessionId = (String) payload.get("sessionId");

            log.debug("Received {} from player {} (session: {})", type, playerId, sessionId);

            switch (type) {
                case "STATE_UPDATE":
                    handleStateUpdate(session, payload);
                    break;
                case "STATE_REQUEST":
                    handleStateRequest(session, payload);
                    break;
                default:
                    log.warn("Unknown message type: {}", type);
                    sendError(session, "Unknown message type: " + type);
            }
        } catch (Exception e) {
            log.error("Failed to handle WebSocket message", e);
            sendError(session, "Failed to process message: " + e.getMessage());
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        String playerId = extractPlayerId(session);
        if (playerId != null) {
            activeSessions.remove(playerId);
            log.info("Game sync connection closed for player: {} (status: {})", playerId, status);
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        String playerId = extractPlayerId(session);
        log.error("Transport error for player {}: {}", playerId, exception.getMessage());
    }

    /**
     * Handle a state update from a game node.
     * Implements last-write-wins conflict resolution with version tracking.
     */
    @SuppressWarnings("unchecked")
    private void handleStateUpdate(WebSocketSession session, Map<String, Object> payload) throws IOException {
        String playerId = (String) payload.get("playerId");
        String sessionId = (String) payload.get("sessionId");
        Map<String, Object> state = (Map<String, Object>) payload.get("state");
        Integer version = (Integer) payload.get("version");

        log.info("State update from player {} (session: {}, version: {})", playerId, sessionId, version);

        // In production, persist the state to the database and check for conflicts
        // For now, simply acknowledge with incremented version
        Map<String, Object> ack = Map.of(
                "type", "SYNC_ACK",
                "playerId", playerId,
                "sessionId", sessionId,
                "version", version != null ? version + 1 : 1
        );

        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(ack)));
    }

    /**
     * Handle a state request from a game node.
     */
    private void handleStateRequest(WebSocketSession session, Map<String, Object> payload) throws IOException {
        String playerId = (String) payload.get("playerId");
        String sessionId = (String) payload.get("sessionId");

        log.info("State request from player {} (session: {})", playerId, sessionId);

        // In production, fetch the latest state from the database
        // For now, return an empty state
        Map<String, Object> response = Map.of(
                "type", "STATE_UPDATE",
                "playerId", playerId,
                "sessionId", sessionId,
                "state", Map.of(),
                "version", 0
        );

        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));
    }

    /**
     * Send an error message to the client.
     */
    private void sendError(WebSocketSession session, String errorMessage) {
        try {
            Map<String, Object> error = Map.of(
                    "type", "SYNC_ERROR",
                    "error", errorMessage
            );
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(error)));
        } catch (IOException e) {
            log.error("Failed to send error message", e);
        }
    }

    /**
     * Extract player ID from the session (from query params or headers).
     */
    private String extractPlayerId(WebSocketSession session) {
        // Try query parameters first
        var params = session.getUri().getQuery();
        if (params != null) {
            String[] pairs = params.split("&");
            for (String pair : pairs) {
                if (pair.startsWith("playerId=")) {
                    return pair.substring("playerId=".length());
                }
            }
        }
        // Fallback to session ID
        return session.getId();
    }

    /**
     * Get the number of active game sync sessions.
     */
    public int getActiveSessionCount() {
        return activeSessions.size();
    }
}
