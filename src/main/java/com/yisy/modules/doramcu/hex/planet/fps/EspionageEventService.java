package com.yisy.modules.doramcu.hex.planet.fps;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Broadcasts espionage events to WebSocket clients in real-time.
 * <p>
 * When spies are deployed, sabotage is executed, intelligence is gathered,
 * or counter-espionage operations occur, this service notifies all connected
 * FPS clients so the 3D battlefield can update espionage indicators.
 * </p>
 */
@Service
public class EspionageEventService {

    private static final Logger log = LoggerFactory.getLogger(EspionageEventService.class);

    private final List<WebSocketSession> sessions = new CopyOnWriteArrayList<>();

    public EspionageEventService() {
    }

    /**
     * Register a WebSocket session for espionage events.
     */
    public void registerSession(WebSocketSession session) {
        sessions.add(session);
        log.info("Registered WebSocket session for espionage events. Total: {}", sessions.size());
    }

    /**
     * Unregister a WebSocket session.
     */
    public void unregisterSession(WebSocketSession session) {
        sessions.remove(session);
        log.info("Unregistered WebSocket session for espionage events. Total: {}", sessions.size());
    }

    /**
     * Broadcast an espionage event to all connected clients.
     */
    public void broadcastEvent(String eventType, Map<String, Object> data) {
        Map<String, Object> message = new HashMap<>();
        message.put("type", "ESPIONAGE_EVENT");
        message.put("event", eventType);
        message.put("timestamp", System.currentTimeMillis());
        message.put("data", data);

        String json = toJson(message);
        broadcast(json);
    }

    /**
     * Broadcast spy deployment.
     */
    public void broadcastSpyDeployment(String deployerIso, String targetIso, 
                                         String spyType, boolean success, String spyId) {
        Map<String, Object> data = new HashMap<>();
        data.put("deployerIso", deployerIso);
        data.put("targetIso", targetIso);
        data.put("spyType", spyType);
        data.put("success", success);
        if (success && spyId != null) {
            data.put("spyId", spyId);
        }
        broadcastEvent(success ? "SPY_DEPLOYED" : "SPY_DEPLOYMENT_FAILED", data);
    }

    /**
     * Broadcast sabotage execution.
     */
    public void broadcastSabotage(String spyId, String targetIso,
                                   String sabotageType,
                                   double effectiveness, boolean detected) {
        Map<String, Object> data = new HashMap<>();
        data.put("spyId", spyId);
        data.put("targetIso", targetIso);
        data.put("sabotageType", sabotageType);
        data.put("effectiveness", effectiveness);
        data.put("detected", detected);
        broadcastEvent("SABOTAGE_EXECUTED", data);
    }

    /**
     * Broadcast intelligence gathering.
     */
    public void broadcastIntelligenceGathered(String spyId, String targetIso, boolean spyDetected) {
        Map<String, Object> data = new HashMap<>();
        data.put("spyId", spyId);
        data.put("targetIso", targetIso);
        data.put("spyDetected", spyDetected);
        broadcastEvent("INTELLIGENCE_GATHERED", data);
    }

    /**
     * Broadcast counter-espionage operation.
     */
    public void broadcastCounterEspionage(String countryIso, int detected, int captured) {
        Map<String, Object> data = new HashMap<>();
        data.put("countryIso", countryIso);
        data.put("detected", detected);
        data.put("captured", captured);
        broadcastEvent("COUNTER_ESPIONAGE", data);
    }

    private void broadcast(String json) {
        Iterator<WebSocketSession> it = sessions.iterator();
        while (it.hasNext()) {
            WebSocketSession session = it.next();
            try {
                if (session.isOpen()) {
                    session.sendMessage(new TextMessage(json));
                } else {
                    it.remove();
                }
            } catch (IOException e) {
                it.remove();
                log.warn("Failed to send espionage event: {}", e.getMessage());
            }
        }
    }

    /**
     * Simple JSON serialization (no external dependency).
     */
    private String toJson(Object obj) {
        if (obj instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) obj;
            StringBuilder sb = new StringBuilder("{");
            boolean first = true;
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                if (!first) sb.append(",");
                first = false;
                sb.append("\"").append(escape(entry.getKey().toString())).append("\":");
                sb.append(toJson(entry.getValue()));
            }
            sb.append("}");
            return sb.toString();
        } else if (obj instanceof List) {
            List<?> list = (List<?>) obj;
            StringBuilder sb = new StringBuilder("[");
            boolean first = true;
            for (Object item : list) {
                if (!first) sb.append(",");
                first = false;
                sb.append(toJson(item));
            }
            sb.append("]");
            return sb.toString();
        } else if (obj instanceof String) {
            return "\"" + escape(obj.toString()) + "\"";
        } else if (obj instanceof Number || obj instanceof Boolean) {
            return obj.toString();
        } else if (obj == null) {
            return "null";
        } else {
            return "\"" + escape(obj.toString()) + "\"";
        }
    }

    private String escape(String s) {
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
