package com.yisy.modules.doramcu.hex.planet.fps;
import com.yisy.modules.doramcu.hex.planet.trade.PlayerTradeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Broadcasts trade route events to WebSocket clients in real-time.
 * <p>
 * When trade routes are created, generate income, or are toggled,
 * this service notifies all connected FPS clients so the 3D battlefield
 * can update cargo flow indicators and economic status.
 * </p>
 */
@Service
public class TradeEventService {

    private static final Logger log = LoggerFactory.getLogger(TradeEventService.class);

    private final PlayerTradeService tradeService;
    private final List<WebSocketSession> sessions = new CopyOnWriteArrayList<>();

    public TradeEventService(PlayerTradeService tradeService) {
        this.tradeService = tradeService;
    }

    /**
     * Register a WebSocket session for trade events.
     */
    public void registerSession(WebSocketSession session) {
        sessions.add(session);
        log.info("Registered WebSocket session for trade events. Total: {}", sessions.size());
    }

    /**
     * Unregister a WebSocket session.
     */
    public void unregisterSession(WebSocketSession session) {
        sessions.remove(session);
        log.info("Unregistered WebSocket session. Total: {}", sessions.size());
    }

    /**
     * Broadcast a trade event to all connected clients.
     */
    public void broadcastEvent(String eventType, Map<String, Object> data) {
        Map<String, Object> message = new HashMap<>();
        message.put("type", "TRADE_EVENT");
        message.put("event", eventType);
        message.put("timestamp", System.currentTimeMillis());
        message.put("data", data);

        String json = toJson(message);
        broadcast(json);
    }

    /**
     * Broadcast trade route creation.
     */
    public void broadcastRouteCreated(String routeId, String playerId,
                                       String fromStation, String toStation, String cargoType) {
        Map<String, Object> data = Map.of(
                "routeId", routeId,
                "playerId", playerId,
                "fromStation", fromStation,
                "toStation", toStation,
                "cargoType", cargoType
        );
        broadcastEvent("ROUTE_CREATED", data);
    }

    /**
     * Broadcast trade income generated.
     */
    public void broadcastIncome(String playerId, double totalIncome) {
        Map<String, Object> data = Map.of(
                "playerId", playerId,
                "totalIncome", totalIncome,
                "routeCount", tradeService.getPlayerRoutes(playerId).size()
        );
        broadcastEvent("INCOME_GENERATED", data);
    }

    /**
     * Broadcast all trade routes for a player.
     */
    public void broadcastPlayerRoutes(String playerId) {
        List<Map<String, Object>> routes = new ArrayList<>();
        for (PlayerTradeService.TradeRoute route : tradeService.getPlayerRoutes(playerId)) {
            Map<String, Object> routeData = new HashMap<>();
            routeData.put("routeId", route.getId());
            routeData.put("fromStation", route.getFromStation().getName());
            routeData.put("toStation", route.getToStation().getName());
            routeData.put("cargoType", route.getCargoType().getDisplayName());
            routeData.put("active", route.isActive());
            routeData.put("totalIncome", route.getTotalIncome());
            routes.add(routeData);
        }

        Map<String, Object> data = Map.of(
                "playerId", playerId,
                "routes", routes
        );
        broadcastEvent("ROUTES_UPDATE", data);
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
                log.warn("Failed to send trade event: {}", e.getMessage());
            }
        }
    }

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
