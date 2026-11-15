package com.yisy.modules.doramcu.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
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
 * WebSocket handler for AI narrative event streaming in 3D galaxy visualization.
 * <p>
 * Connection URL: {@code ws://host:port/ws/narrative?sessionId=ABC&playerId=1}
 * </p>
 * <p>
 * Server-to-client message types:
 * <ul>
 *   <li>{@code narrative_event} — New narrative event with visual effect data</li>
 *   <li>{@code event_update} — Update to an existing event</li>
 *   <li>{@code event_resolved} — Event has been resolved/completed</li>
 *   <li>{@code timeline_update} — Full timeline of past and upcoming events</li>
 * </ul>
 * </p>
 * <p>
 * Client-to-server message types:
 * <ul>
 *   <li>{@code acknowledge} — Acknowledge an event</li>
 *   <li>{@code choose} — Make a choice for an interactive event</li>
 *   <li>{@code ping} — Keep-alive ping</li>
 * </ul>
 * </p>
 * <p>
 * Narrative event types: battle, discovery, diplomatic, anomaly, exploration,
 * trade, crisis, victory, mystery, personal
 * </p>
 * <p>
 * Narrative styles: dramatic, subtle, epic, mysterious, urgent, peaceful, chaotic, reflective
 * </p>
 */
public class NarrativeWebSocketHandler extends TextWebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(NarrativeWebSocketHandler.class);

    private final ObjectMapper objectMapper = new ObjectMapper();

    // Session management
    private final Map<WebSocketSession, String> sessionPlayerIds = new ConcurrentHashMap<>();
    private final Map<WebSocketSession, String> sessionSessionIds = new ConcurrentHashMap<>();
    private final Map<String, List<WebSocketSession>> sessionGroups = new ConcurrentHashMap<>();

    // Event data
    private final Map<String, NarrativeEvent> events = new ConcurrentHashMap<>();
    private final List<NarrativeEvent> timeline = Collections.synchronizedList(new ArrayList<>());

    // Simulation
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    /**
     * Narrative event data structure.
     */
    static class NarrativeEvent {
        String eventId;
        String type; // battle, discovery, diplomatic, anomaly, exploration, trade, crisis, victory, mystery, personal
        String style; // dramatic, subtle, epic, mysterious, urgent, peaceful, chaotic, reflective
        String title;
        String description;
        double x, y, z;
        long timestamp;
        long duration;
        boolean resolved;
        String sessionId;
    }

    private static final String[] NARRATIVE_TYPES = {
            "battle", "discovery", "diplomatic", "anomaly", "exploration",
            "trade", "crisis", "victory", "mystery", "personal"
    };

    private static final String[] NARRATIVE_STYLES = {
            "dramatic", "subtle", "epic", "mysterious", "urgent",
            "peaceful", "chaotic", "reflective"
    };

    private static final Map<String, String[]> NARRATIVE_TITLES = new LinkedHashMap<>();

    static {
        NARRATIVE_TITLES.put("battle", new String[]{
                "Fleet Engagement Detected", "Orbital Bombardment", "Space Battle Erupts",
                "Ambush in the Void", "Last Stand at the Nebula"
        });
        NARRATIVE_TITLES.put("discovery", new String[]{
                "Ancient Ruins Found", "Unknown Signal Detected", "New Species Discovered",
                "Hidden Planet Revealed", "Artifact Unearthed"
        });
        NARRATIVE_TITLES.put("diplomatic", new String[]{
                "Peace Treaty Negotiations", "Alliance Formed", "Embassy Established",
                "Trade Agreement Signed", "Diplomatic Crisis Averted"
        });
        NARRATIVE_TITLES.put("anomaly", new String[]{
                "Spatial Rift Detected", "Gravity Well Forming", "Wormhole Instability",
                "Dimensional Breach", "Temporal Distortion"
        });
        NARRATIVE_TITLES.put("exploration", new String[]{
                "New Sector Mapped", "Uncharted System Found", "Beacon Signal Located",
                "Frontier Expanded", "Deep Space Expedition"
        });
        NARRATIVE_TITLES.put("trade", new String[]{
                "Trade Route Opened", "Market Boom", "Resource Shortage",
                "Smuggling Ring Discovered", "Economic Shift"
        });
        NARRATIVE_TITLES.put("crisis", new String[]{
                "Supernova Warning", "Pirate Raid", "System Failure",
                "Plague Outbreak", "Rebellion Erupts"
        });
        NARRATIVE_TITLES.put("victory", new String[]{
                "Battle Won", "Territory Secured", "Mission Accomplished",
                "Enemy Defeated", "Triumph in Space"
        });
        NARRATIVE_TITLES.put("mystery", new String[]{
                "Strange Phenomenon", "Unexplained Signals", "Ghost Ship Sighted",
                "Ancient Prophecy", "Mysterious Disappearance"
        });
        NARRATIVE_TITLES.put("personal", new String[]{
                "Character Milestone", "Personal Quest", "Memory Fragment",
                "Inner Conflict", "Fateful Decision"
        });
    }

    private static final Map<String, String[]> NARRATIVE_DESCRIPTIONS = new LinkedHashMap<>();

    static {
        NARRATIVE_DESCRIPTIONS.put("battle", new String[]{
                "Hostile forces have engaged in a fierce battle in this sector.",
                "A massive fleet confrontation is underway near the star system.",
                "Combat detected between rival factions in deep space."
        });
        NARRATIVE_DESCRIPTIONS.put("discovery", new String[]{
                "Explorers have uncovered something remarkable in this region.",
                "A signal of unknown origin has been detected.",
                "Ancient technology has been found on a remote world."
        });
        NARRATIVE_DESCRIPTIONS.put("diplomatic", new String[]{
                "Representatives from multiple factions have gathered for negotiations.",
                "A historic alliance is being formed between rival powers.",
                "Diplomatic channels have been opened after years of tension."
        });
        NARRATIVE_DESCRIPTIONS.put("anomaly", new String[]{
                "A spatial anomaly has appeared, threatening nearby systems.",
                "Scientists are baffled by readings from this sector.",
                "Reality itself seems to be warping in this region."
        });
        NARRATIVE_DESCRIPTIONS.put("exploration", new String[]{
                "A new expedition has ventured into uncharted territory.",
                "The frontier of known space has been expanded.",
                "Pioneers are mapping previously unknown star systems."
        });
        NARRATIVE_DESCRIPTIONS.put("trade", new String[]{
                "A new trade route has opened, boosting economic activity.",
                "Resource prices are fluctuating wildly across the sector.",
                "A major trade agreement has reshaped the galactic economy."
        });
        NARRATIVE_DESCRIPTIONS.put("crisis", new String[]{
                "A catastrophic event threatens the stability of this region.",
                "Emergency protocols have been activated across multiple systems.",
                "The sector is on the brink of collapse."
        });
        NARRATIVE_DESCRIPTIONS.put("victory", new String[]{
                "Against all odds, the forces have achieved a decisive victory.",
                "The battle is won, and peace returns to the sector.",
                "A great triumph marks a turning point in the conflict."
        });
        NARRATIVE_DESCRIPTIONS.put("mystery", new String[]{
                "Something strange is happening, and no one can explain it.",
                "Whispers of the unknown spread through the sector.",
                "The truth remains hidden, waiting to be discovered."
        });
        NARRATIVE_DESCRIPTIONS.put("personal", new String[]{
                "A character faces a defining moment in their journey.",
                "Personal stakes rise as the story unfolds.",
                "An inner struggle reaches its climax."
        });
    }

    public NarrativeWebSocketHandler() {
        // Simulate narrative events
        scheduler.scheduleAtFixedRate(this::simulateNarrativeEvents, 20, 30, TimeUnit.SECONDS);
        scheduler.scheduleAtFixedRate(this::resolveOldEvents, 60, 60, TimeUnit.SECONDS);
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        String playerId = extractParameter(session.getUri().getQuery(), "playerId");
        String gameSessionId = extractParameter(session.getUri().getQuery(), "sessionId");

        if (playerId == null) playerId = "player_" + System.currentTimeMillis();
        if (gameSessionId == null) gameSessionId = "default";

        sessionPlayerIds.put(session, playerId);
        sessionSessionIds.put(session, gameSessionId);

        // Group by game session
        sessionGroups.computeIfAbsent(gameSessionId, k -> Collections.synchronizedList(new ArrayList<>())).add(session);

        log.info("Narrative connected: sessionId={}, playerId={}", gameSessionId, playerId);

        // Send current timeline
        sendTimelineUpdate(session);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        try {
            ObjectNode payload = (ObjectNode) objectMapper.readTree(message.getPayload());
            String type = payload.has("type") ? payload.get("type").asText() : "";

            switch (type) {
                case "acknowledge":
                    handleAcknowledge(session, payload);
                    break;
                case "choose":
                    handleChoose(session, payload);
                    break;
                case "ping":
                    sendPingResponse(session);
                    break;
                default:
                    log.warn("Unknown narrative message type: {}", type);
            }
        } catch (Exception e) {
            log.error("Error handling narrative message", e);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        String sessionId = sessionSessionIds.remove(session);
        String playerId = sessionPlayerIds.remove(session);

        if (sessionId != null) {
            List<WebSocketSession> group = sessionGroups.get(sessionId);
            if (group != null) {
                group.remove(session);
                if (group.isEmpty()) {
                    sessionGroups.remove(sessionId);
                }
            }
        }

        log.info("Narrative disconnected: sessionId={}, playerId={}", sessionId, playerId);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        log.error("Narrative transport error", exception);
        afterConnectionClosed(session, CloseStatus.SERVER_ERROR);
    }

    /**
     * Handle event acknowledgment.
     */
    private void handleAcknowledge(WebSocketSession session, ObjectNode payload) {
        String eventId = payload.has("eventId") ? payload.get("eventId").asText() : "";
        log.info("Client acknowledged event: {}", eventId);
    }

    /**
     * Handle event choice.
     */
    private void handleChoose(WebSocketSession session, ObjectNode payload) {
        String eventId = payload.has("eventId") ? payload.get("eventId").asText() : "";
        String choice = payload.has("choice") ? payload.get("choice").asText() : "";
        log.info("Client chose '{}' for event: {}", choice, eventId);
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
     * Simulate narrative events.
     */
    private void simulateNarrativeEvents() {
        Random random = new Random();

        String type = NARRATIVE_TYPES[random.nextInt(NARRATIVE_TYPES.length)];
        String style = NARRATIVE_STYLES[random.nextInt(NARRATIVE_STYLES.length)];

        String[] titles = NARRATIVE_TITLES.get(type);
        String[] descriptions = NARRATIVE_DESCRIPTIONS.get(type);

        NarrativeEvent event = new NarrativeEvent();
        event.eventId = "narrative_" + System.currentTimeMillis();
        event.type = type;
        event.style = style;
        event.title = titles[random.nextInt(titles.length)];
        event.description = descriptions[random.nextInt(descriptions.length)];
        event.x = random.nextDouble() * 300 - 150;
        event.y = random.nextDouble() * 80 - 40;
        event.z = random.nextDouble() * 300 - 150;
        event.timestamp = System.currentTimeMillis();
        event.duration = 10000 + random.nextInt(30000);
        event.resolved = false;
        event.sessionId = "default";

        events.put(event.eventId, event);
        timeline.add(event);

        // Keep timeline manageable
        if (timeline.size() > 50) {
            timeline.subList(0, timeline.size() - 50).clear();
        }

        // Broadcast the event
        try {
            ObjectNode data = objectMapper.createObjectNode();
            data.put("eventId", event.eventId);
            data.put("type", event.type);
            data.put("style", event.style);
            data.put("title", event.title);
            data.put("description", event.description);
            data.put("timestamp", event.timestamp);
            data.put("duration", event.duration);

            ObjectNode position = objectMapper.createObjectNode();
            position.put("x", event.x);
            position.put("y", event.y);
            position.put("z", event.z);
            data.set("position", position);

            ObjectNode message = objectMapper.createObjectNode();
            message.put("type", "narrative_event");
            message.set("data", data);

            broadcast(new TextMessage(objectMapper.writeValueAsString(message)));
        } catch (IOException e) {
            log.error("Failed to broadcast narrative event", e);
        }
    }

    /**
     * Resolve old events.
     */
    private void resolveOldEvents() {
        long now = System.currentTimeMillis();

        for (Map.Entry<String, NarrativeEvent> entry : events.entrySet()) {
            NarrativeEvent event = entry.getValue();
            if (!event.resolved && now - event.timestamp > event.duration) {
                event.resolved = true;

                try {
                    ObjectNode data = objectMapper.createObjectNode();
                    data.put("eventId", event.eventId);

                    ObjectNode message = objectMapper.createObjectNode();
                    message.put("type", "event_resolved");
                    message.set("data", data);

                    broadcast(new TextMessage(objectMapper.writeValueAsString(message)));
                } catch (IOException e) {
                    log.error("Failed to broadcast event resolution", e);
                }
            }
        }
    }

    /**
     * Send timeline update to a session.
     */
    private void sendTimelineUpdate(WebSocketSession session) {
        try {
            ObjectNode message = objectMapper.createObjectNode();
            message.put("type", "timeline_update");

            ObjectNode data = objectMapper.createObjectNode();
            ArrayNode eventsArray = data.putArray("events");

            for (NarrativeEvent event : timeline) {
                ObjectNode eventNode = objectMapper.createObjectNode();
                eventNode.put("eventId", event.eventId);
                eventNode.put("type", event.type);
                eventNode.put("style", event.style);
                eventNode.put("title", event.title);
                eventNode.put("timestamp", event.timestamp);
                eventNode.put("resolved", event.resolved);

                ObjectNode position = objectMapper.createObjectNode();
                position.put("x", event.x);
                position.put("y", event.y);
                position.put("z", event.z);
                eventNode.set("position", position);

                eventsArray.add(eventNode);
            }

            message.set("data", data);
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(message)));
        } catch (IOException e) {
            log.error("Failed to send timeline update", e);
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
     * Get active event count.
     */
    public int getActiveEventCount() {
        return (int) events.values().stream().filter(e -> !e.resolved).count();
    }

    /**
     * Get timeline size.
     */
    public int getTimelineSize() {
        return timeline.size();
    }
}
