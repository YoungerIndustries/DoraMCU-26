package com.yisy.modules.doramcu.controller.misc;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.yisy.modules.doramcu.admin.model.AiAdminCollaborationNote;
import com.yisy.modules.doramcu.admin.model.AiAdminIncidentCollaborator;
import com.yisy.modules.doramcu.admin.service.AiAdminCollaborationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * CollaborationWebSocketHandler — Real-time collaboration event updates.
 *
 * WebSocket endpoint: /ws/collaboration
 *
 * Message types (server → client):
 * - collaborator_joined: A new collaborator joined an incident
 * - collaborator_left: A collaborator left an incident
 * - note_added: A new collaboration note was added
 * - status_changed: A collaborator's status changed (e.g., ACTIVE → COMPLETED)
 * - auto_invited: Auto-invitation added collaborators
 * - snapshot: Initial collaboration state when subscribing to an incident
 * - pong: Response to ping
 * - error: Error message
 *
 * Client messages:
 * - subscribe: {"type":"subscribe","incidentId":123}
 * - unsubscribe: {"type":"unsubscribe","incidentId":123}
 * - ping: {"type":"ping"}
 */
public class CollaborationWebSocketHandler extends TextWebSocketHandler {
    private static final Logger log = LoggerFactory.getLogger(CollaborationWebSocketHandler.class);

    private final AiAdminCollaborationService collaborationService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // All connected sessions
    private final Set<WebSocketSession> sessions = ConcurrentHashMap.newKeySet();

    // Maps incidentId to sessions subscribed to that incident
    private final Map<Long, Set<WebSocketSession>> incidentSubscriptions = new ConcurrentHashMap<>();

    // Maps session to set of incidentIds it is subscribed to
    private final Map<WebSocketSession, Set<Long>> sessionSubscriptions = new ConcurrentHashMap<>();

    public CollaborationWebSocketHandler(AiAdminCollaborationService collaborationService) {
        this.collaborationService = collaborationService;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessions.add(session);
        sessionSubscriptions.put(session, ConcurrentHashMap.newKeySet());
        log.info("Collaboration WebSocket connected: {}", session.getId());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.remove(session);
        // Unsubscribe from all incidents
        Set<Long> subscribedIncidents = sessionSubscriptions.remove(session);
        if (subscribedIncidents != null) {
            for (Long incidentId : subscribedIncidents) {
                Set<WebSocketSession> incidentSessions = incidentSubscriptions.get(incidentId);
                if (incidentSessions != null) {
                    incidentSessions.remove(session);
                    if (incidentSessions.isEmpty()) {
                        incidentSubscriptions.remove(incidentId);
                    }
                }
            }
        }
        log.info("Collaboration WebSocket disconnected: {}", session.getId());
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
            log.error("Error handling collaboration message: {}", e.getMessage());
            try {
                sendError(session, "Error: " + e.getMessage());
            } catch (IOException ioException) {
                log.error("Error sending error message: {}", ioException.getMessage());
            }
        }
    }

    /**
     * Handle subscribe message: client wants to receive updates for a specific incident.
     */
    private void handleSubscribe(WebSocketSession session, JsonNode json) throws IOException {
        if (!json.has("incidentId")) {
            sendError(session, "Missing incidentId in subscribe message");
            return;
        }

        Long incidentId = json.get("incidentId").asLong();
        Set<WebSocketSession> incidentSessions = incidentSubscriptions.computeIfAbsent(incidentId, k -> ConcurrentHashMap.newKeySet());
        incidentSessions.add(session);

        Set<Long> subscribedIncidents = sessionSubscriptions.get(session);
        if (subscribedIncidents != null) {
            subscribedIncidents.add(incidentId);
        }

        // Send initial snapshot of current collaboration state
        sendSnapshot(session, incidentId);
        log.info("Session {} subscribed to incident {}", session.getId(), incidentId);
    }

    /**
     * Handle unsubscribe message: client no longer wants updates for a specific incident.
     */
    private void handleUnsubscribe(WebSocketSession session, JsonNode json) throws IOException {
        if (!json.has("incidentId")) {
            sendError(session, "Missing incidentId in unsubscribe message");
            return;
        }

        Long incidentId = json.get("incidentId").asLong();
        Set<WebSocketSession> incidentSessions = incidentSubscriptions.get(incidentId);
        if (incidentSessions != null) {
            incidentSessions.remove(session);
            if (incidentSessions.isEmpty()) {
                incidentSubscriptions.remove(incidentId);
            }
        }

        Set<Long> subscribedIncidents = sessionSubscriptions.get(session);
        if (subscribedIncidents != null) {
            subscribedIncidents.remove(incidentId);
        }

        log.info("Session {} unsubscribed from incident {}", session.getId(), incidentId);
    }

    /**
     * Send initial collaboration snapshot when a client subscribes to an incident.
     */
    private void sendSnapshot(WebSocketSession session, Long incidentId) throws IOException {
        ObjectNode response = objectMapper.createObjectNode();
        response.put("type", "snapshot");
        response.put("incidentId", incidentId);

        // Add collaborators
        try {
            List<AiAdminIncidentCollaborator> collaborators = collaborationService.getCollaborators(incidentId);
            var collabArray = response.putArray("collaborators");
            for (AiAdminIncidentCollaborator collab : collaborators) {
                ObjectNode collabNode = objectMapper.createObjectNode();
                collabNode.put("id", collab.getId());
                collabNode.put("aiAdminId", collab.getAiAdminId());
                collabNode.put("role", collab.getRole().name());
                collabNode.put("status", collab.getStatus().name());
                collabNode.put("joinedAt", collab.getJoinedAt().toString());
                if (collab.getCompletedAt() != null) {
                    collabNode.put("completedAt", collab.getCompletedAt().toString());
                }
                if (collab.getNotes() != null) {
                    collabNode.put("notes", collab.getNotes());
                }
                collabArray.add(collabNode);
            }
        } catch (Exception e) {
            log.warn("Failed to get collaborators for snapshot: {}", e.getMessage());
            response.putArray("collaborators");
        }

        // Add notes
        try {
            List<AiAdminCollaborationNote> notes = collaborationService.getNotes(incidentId);
            var notesArray = response.putArray("notes");
            for (AiAdminCollaborationNote note : notes) {
                ObjectNode noteNode = objectMapper.createObjectNode();
                noteNode.put("id", note.getId());
                noteNode.put("aiAdminId", note.getAiAdminId());
                noteNode.put("note", note.getNote());
                noteNode.put("createdAt", note.getCreatedAt().toString());
                notesArray.add(noteNode);
            }
        } catch (Exception e) {
            log.warn("Failed to get notes for snapshot: {}", e.getMessage());
            response.putArray("notes");
        }

        response.put("timestamp", System.currentTimeMillis());
        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));
    }

    /**
     * Broadcast collaboration event to all sessions subscribed to a specific incident.
     * Called by AiAdminCollaborationService when collaboration events occur.
     */
    public void broadcastCollaborationEvent(Long incidentId, String eventType, Map<String, Object> eventData) {
        Set<WebSocketSession> incidentSessions = incidentSubscriptions.get(incidentId);
        if (incidentSessions == null || incidentSessions.isEmpty()) {
            log.debug("No subscribers for incident {}", incidentId);
            return;
        }

        ObjectNode message = objectMapper.createObjectNode();
        message.put("type", eventType);
        message.put("incidentId", incidentId != null ? incidentId : 0);
        if (eventData != null) {
            eventData.forEach((key, value) -> {
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
            log.error("Error serializing collaboration event: {}", e.getMessage());
            return;
        }

        TextMessage textMessage = new TextMessage(json);
        incidentSessions.forEach(s -> {
            try {
                if (s.isOpen()) {
                    s.sendMessage(textMessage);
                }
            } catch (IOException e) {
                log.error("Error broadcasting collaboration event to session {}: {}", s.getId(), e.getMessage());
            }
        });
        log.debug("Broadcast {} for incident {} to {} sessions", eventType, incidentId, incidentSessions.size());
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
     * Get the number of unique incidents being monitored.
     */
    public int getMonitoredIncidents() {
        return incidentSubscriptions.size();
    }
}
