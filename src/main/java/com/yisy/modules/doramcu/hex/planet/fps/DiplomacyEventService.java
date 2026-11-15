package com.yisy.modules.doramcu.hex.planet.fps;

import com.yisy.modules.doramcu.earth2026.service.CountryDiplomacyService;
import com.yisy.modules.doramcu.fourx.FourxDiplomaticRelation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Broadcasts diplomacy events to WebSocket clients in real-time.
 * <p>
 * When countries declare war, establish peace, or sign treaties,
 * this service notifies all connected FPS clients so the 3D battlefield
 * can update faction colors, alliance markers, and battle modifiers.
 * </p>
 */
@Service
public class DiplomacyEventService {

    private static final Logger log = LoggerFactory.getLogger(DiplomacyEventService.class);

    private final CountryDiplomacyService diplomacyService;
    private final List<WebSocketSession> sessions = new CopyOnWriteArrayList<>();

    public DiplomacyEventService(CountryDiplomacyService diplomacyService) {
        this.diplomacyService = diplomacyService;
    }

    /**
     * Register a WebSocket session for diplomacy events.
     */
    public void registerSession(WebSocketSession session) {
        sessions.add(session);
        log.info("Registered WebSocket session for diplomacy events. Total: {}", sessions.size());
    }

    /**
     * Unregister a WebSocket session.
     */
    public void unregisterSession(WebSocketSession session) {
        sessions.remove(session);
        log.info("Unregistered WebSocket session. Total: {}", sessions.size());
    }

    /**
     * Broadcast a diplomacy event to all connected clients.
     */
    public void broadcastEvent(String eventType, Map<String, Object> data) {
        Map<String, Object> message = new HashMap<>();
        message.put("type", "DIPLOMACY_EVENT");
        message.put("event", eventType);
        message.put("timestamp", System.currentTimeMillis());
        message.put("data", data);

        String json = toJson(message);
        broadcast(json);
    }

    /**
     * Generate and broadcast a random diplomacy event for a country.
     */
    public void generateAndBroadcast(String countryIso) {
        Map<String, Object> event = diplomacyService.generateDiplomaticEvent(countryIso);
        broadcastEvent((String) event.get("event"), event);
    }

    /**
     * Broadcast war declaration.
     */
    public void broadcastWarDeclaration(String attacker, String defender) {
        FourxDiplomaticRelation relation = diplomacyService.declareWar(attacker, defender);
        Map<String, Object> data = Map.of(
                "attacker", attacker,
                "defender", defender,
                "atWar", relation.isAtWar(),
                "relationScore", relation.getRelationScore()
        );
        broadcastEvent("WAR_DECLARED", data);
    }

    /**
     * Broadcast peace establishment.
     */
    public void broadcastPeace(String faction1, String faction2) {
        FourxDiplomaticRelation relation = diplomacyService.establishPeace(faction1, faction2);
        Map<String, Object> data = Map.of(
                "faction1", faction1,
                "faction2", faction2,
                "atWar", relation.isAtWar(),
                "relationScore", relation.getRelationScore()
        );
        broadcastEvent("PEACE_ESTABLISHED", data);
    }

    /**
     * Broadcast treaty signing.
     */
    public void broadcastTreaty(String faction1, String faction2, String treatyType) {
        FourxDiplomaticRelation relation = diplomacyService.addTreaty(faction1, faction2, treatyType);
        Map<String, Object> data = Map.of(
                "faction1", faction1,
                "faction2", faction2,
                "treatyType", treatyType,
                "treaties", relation.getActiveTreaties(),
                "relationScore", relation.getRelationScore()
        );
        broadcastEvent("TREATY_SIGNED", data);
    }

    /**
     * Broadcast all current faction relations to a new client.
     */
    public void broadcastCurrentState(WebSocketSession session) {
        try {
            Map<String, Object> state = new HashMap<>();
            state.put("type", "DIPLOMACY_STATE");
            state.put("factions", diplomacyService.getAllFactions());
            state.put("timestamp", System.currentTimeMillis());

            session.sendMessage(new TextMessage(toJson(state)));
        } catch (IOException e) {
            log.warn("Failed to send diplomacy state: {}", e.getMessage());
        }
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
                log.warn("Failed to send diplomacy event: {}", e.getMessage());
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
