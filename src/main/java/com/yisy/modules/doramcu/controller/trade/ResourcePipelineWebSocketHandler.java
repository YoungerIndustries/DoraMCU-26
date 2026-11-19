package com.yisy.modules.doramcu.controller.trade;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.yisy.modules.doramcu.service.game.GameStateBridge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * WebSocket handler for real-time resource pipeline updates in 3D galaxy visualization.
 * <p>
 * Connection URL: {@code ws://host:port/ws/resource-pipeline?sessionId=ABC&galaxyId=Corellian}
 * </p>
 * <p>
 * Server-to-client message types:
 * <ul>
 *   <li>{@code pipeline_snapshot} — Full pipeline data on connection</li>
 *   <li>{@code route_update} — Transport route status or progress update</li>
 *   <li>{@code mining_update} — Mining operation production or status change</li>
 *   <li>{@code facility_update} — Processing facility status or output change</li>
 *   <li>{@code statistics_update} — Pipeline statistics refresh</li>
 *   <li>{@code pong} — Response to client ping</li>
 * </ul>
 * </p>
 * <p>
 * Client-to-server message types:
 * <ul>
 *   <li>{@code subscribe} — Subscribe to pipeline updates for a galaxy</li>
 *   <li>{@code ping} — Keepalive ping</li>
 * </ul>
 * </p>
 */
@Component
public class ResourcePipelineWebSocketHandler extends TextWebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(ResourcePipelineWebSocketHandler.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final GameStateBridge gameStateBridge;
    private final boolean realDataMode;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);

    // Session management
    private final ConcurrentHashMap<WebSocketSession, String> sessionGalaxyIds = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<WebSocketSession, String> sessionSessionIds = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, List<WebSocketSession>> galaxyGroups = new ConcurrentHashMap<>();

    // Pipeline data
    private final ConcurrentHashMap<String, RouteData> routes = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, MiningData> miningOps = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, FacilityData> facilities = new ConcurrentHashMap<>();

    private final AtomicInteger routeCounter = new AtomicInteger(0);
    private final AtomicInteger miningCounter = new AtomicInteger(0);
    private final AtomicInteger facilityCounter = new AtomicInteger(0);

    public ResourcePipelineWebSocketHandler() {
        this(null, false);
    }

    public ResourcePipelineWebSocketHandler(GameStateBridge gameStateBridge, boolean realDataMode) {
        this.gameStateBridge = gameStateBridge;
        this.realDataMode = realDataMode;
        if (!realDataMode) {
            initializePipelineData();
            scheduler.scheduleAtFixedRate(this::simulateRouteUpdates, 5, 5, TimeUnit.SECONDS);
            scheduler.scheduleAtFixedRate(this::simulateMiningUpdates, 8, 8, TimeUnit.SECONDS);
            scheduler.scheduleAtFixedRate(this::simulateFacilityUpdates, 10, 10, TimeUnit.SECONDS);
            scheduler.scheduleAtFixedRate(this::simulateStatisticsUpdate, 15, 15, TimeUnit.SECONDS);
        } else {
            log.info("ResourcePipeline starting in real-data mode with periodic refresh");
            scheduler.scheduleAtFixedRate(this::refreshRealData, 10, 10, TimeUnit.SECONDS);
        }
    }

    /**
     * Initialize simulated pipeline data.
     */
    private void initializePipelineData() {
        String[] resourceTypes = {"iron", "copper", "gold", "titanium", "uranium",
                "water", "hydrogen", "helium", "crystals", "organic"};
        String[] bodyNames = {"Alpha Prime", "Nova Terra", "Cygnus IV", "Andromeda VII",
                "Kepler-22b", "Proxima Centauri b", "Trappist-1e", "Gliese 667Cc",
                "Mining Station A", "Processing Hub B", "Trade Outpost C"};

        Random random = new Random();

        // Create initial transport routes
        for (int i = 0; i < 8; i++) {
            String id = "route_" + routeCounter.incrementAndGet();
            RouteData route = new RouteData();
            route.id = id;
            route.resourceType = resourceTypes[random.nextInt(resourceTypes.length)];
            route.sourceName = bodyNames[random.nextInt(bodyNames.length)];
            route.destinationName = bodyNames[random.nextInt(bodyNames.length)];
            route.sourceX = random.nextDouble() * 300 - 150;
            route.sourceY = random.nextDouble() * 80 - 40;
            route.sourceZ = random.nextDouble() * 300 - 150;
            route.destX = random.nextDouble() * 300 - 150;
            route.destY = random.nextDouble() * 80 - 40;
            route.destZ = random.nextDouble() * 300 - 150;
            route.volume = 100 + random.nextDouble() * 900;
            route.speed = 0.5 + random.nextDouble() * 2;
            route.progress = random.nextDouble();
            route.status = "active";
            route.shipsOnRoute = 1 + random.nextInt(5);
            route.lastUpdate = System.currentTimeMillis();
            routes.put(id, route);
        }

        // Create initial mining operations
        String[] miningBodies = {"Asteroid Belt Alpha", "Mining World Beta", "Resource Moon Gamma",
                "Deep Space Mine Delta", "Orbital Platform Epsilon"};
        String[] miningResources = {"iron", "copper", "gold", "titanium", "uranium", "crystals"};
        for (int i = 0; i < 5; i++) {
            String id = "mining_" + miningCounter.incrementAndGet();
            MiningData mining = new MiningData();
            mining.id = id;
            mining.name = miningBodies[random.nextInt(miningBodies.length)];
            mining.resourceType = miningResources[random.nextInt(miningResources.length)];
            mining.x = random.nextDouble() * 300 - 150;
            mining.y = random.nextDouble() * 80 - 40;
            mining.z = random.nextDouble() * 300 - 150;
            mining.productionRate = 10 + random.nextDouble() * 90;
            mining.capacity = 100 + random.nextDouble() * 400;
            mining.currentOutput = mining.productionRate * (0.5 + random.nextDouble() * 0.5);
            mining.status = "active";
            mining.lastUpdate = System.currentTimeMillis();
            miningOps.put(id, mining);
        }

        // Create initial processing facilities
        String[] facilityTypes = {"refinery", "fabrication", "assembly", "research", "storage"};
        String[] facilityNames = {"Central Refinery", "Fabrication Hub", "Assembly Plant",
                "Research Station", "Storage Depot", "Processing Complex"};
        for (int i = 0; i < 4; i++) {
            String id = "facility_" + facilityCounter.incrementAndGet();
            FacilityData facility = new FacilityData();
            facility.id = id;
            facility.name = facilityNames[random.nextInt(facilityNames.length)] + " " + (i + 1);
            facility.type = facilityTypes[random.nextInt(facilityTypes.length)];
            facility.x = random.nextDouble() * 300 - 150;
            facility.y = random.nextDouble() * 80 - 40;
            facility.z = random.nextDouble() * 300 - 150;
            facility.productionRate = 20 + random.nextDouble() * 80;
            facility.efficiency = 0.5 + random.nextDouble() * 0.5;
            facility.status = "operational";
            facility.lastUpdate = System.currentTimeMillis();
            facilities.put(id, facility);
        }

        log.info("Initialized pipeline data: {} routes, {} mining ops, {} facilities",
                routes.size(), miningOps.size(), facilities.size());
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        String galaxyId = extractParameter(session.getUri().getQuery(), "galaxyId");
        String sessionId = extractParameter(session.getUri().getQuery(), "sessionId");

        if (galaxyId == null) galaxyId = "default";
        if (sessionId == null) sessionId = "default";

        sessionGalaxyIds.put(session, galaxyId);
        sessionSessionIds.put(session, sessionId);

        // Group by galaxy
        galaxyGroups.computeIfAbsent(galaxyId, k -> Collections.synchronizedList(new ArrayList<>())).add(session);

        log.info("Resource pipeline connected: galaxyId={}, sessionId={}", galaxyId, sessionId);

        // Send initial pipeline snapshot
        sendPipelineSnapshot(session);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        try {
            ObjectNode payload = (ObjectNode) objectMapper.readTree(message.getPayload());
            String type = payload.has("type") ? payload.get("type").asText() : "";

            switch (type) {
                case "subscribe":
                    handleSubscribe(session, payload);
                    break;
                case "ping":
                    sendPingResponse(session);
                    break;
                default:
                    log.warn("Unknown resource pipeline message type: {}", type);
            }
        } catch (Exception e) {
            log.error("Error handling resource pipeline message", e);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        String galaxyId = sessionGalaxyIds.remove(session);
        String sessionId = sessionSessionIds.remove(session);

        if (galaxyId != null) {
            List<WebSocketSession> group = galaxyGroups.get(galaxyId);
            if (group != null) {
                group.remove(session);
                if (group.isEmpty()) {
                    galaxyGroups.remove(galaxyId);
                }
            }
        }

        log.info("Resource pipeline disconnected: galaxyId={}, sessionId={}", galaxyId, sessionId);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        log.error("Resource pipeline transport error: {}", exception.getMessage());
        afterConnectionClosed(session, CloseStatus.SERVER_ERROR);
    }

    /**
     * Handle subscribe message.
     */
    private void handleSubscribe(WebSocketSession session, ObjectNode payload) {
        String galaxyId = payload.has("galaxyId") ? payload.get("galaxyId").asText() : "default";
        sessionGalaxyIds.put(session, galaxyId);

        log.info("Subscribed to pipeline: galaxyId={}", galaxyId);
        sendPipelineSnapshot(session);
    }

    /**
     * Send full pipeline snapshot to a session.
     */
    private void sendPipelineSnapshot(WebSocketSession session) {
        try {
            ObjectNode data = objectMapper.createObjectNode();

            // Routes
            ArrayNode routesArray = objectMapper.createArrayNode();
            for (RouteData route : routes.values()) {
                ObjectNode routeNode = objectMapper.createObjectNode();
                routeNode.put("id", route.id);
                routeNode.put("resourceType", route.resourceType);
                routeNode.put("volume", route.volume);
                routeNode.put("speed", route.speed);
                routeNode.put("progress", route.progress);
                routeNode.put("status", route.status);
                routeNode.put("shipsOnRoute", route.shipsOnRoute);

                ObjectNode source = objectMapper.createObjectNode();
                source.put("name", route.sourceName);
                source.put("x", route.sourceX);
                source.put("y", route.sourceY);
                source.put("z", route.sourceZ);
                routeNode.set("source", source);

                ObjectNode destination = objectMapper.createObjectNode();
                destination.put("name", route.destinationName);
                destination.put("x", route.destX);
                destination.put("y", route.destY);
                destination.put("z", route.destZ);
                routeNode.set("destination", destination);

                routesArray.add(routeNode);
            }
            data.set("routes", routesArray);

            // Mining operations
            ArrayNode miningArray = objectMapper.createArrayNode();
            for (MiningData mining : miningOps.values()) {
                ObjectNode miningNode = objectMapper.createObjectNode();
                miningNode.put("id", mining.id);
                miningNode.put("name", mining.name);
                miningNode.put("resourceType", mining.resourceType);
                miningNode.put("x", mining.x);
                miningNode.put("y", mining.y);
                miningNode.put("z", mining.z);
                miningNode.put("productionRate", mining.productionRate);
                miningNode.put("capacity", mining.capacity);
                miningNode.put("currentOutput", mining.currentOutput);
                miningNode.put("status", mining.status);
                miningArray.add(miningNode);
            }
            data.set("miningOperations", miningArray);

            // Processing facilities
            ArrayNode facilitiesArray = objectMapper.createArrayNode();
            for (FacilityData facility : facilities.values()) {
                ObjectNode facilityNode = objectMapper.createObjectNode();
                facilityNode.put("id", facility.id);
                facilityNode.put("name", facility.name);
                facilityNode.put("type", facility.type);
                facilityNode.put("x", facility.x);
                facilityNode.put("y", facility.y);
                facilityNode.put("z", facility.z);
                facilityNode.put("productionRate", facility.productionRate);
                facilityNode.put("efficiency", facility.efficiency);
                facilityNode.put("status", facility.status);
                facilitiesArray.add(facilityNode);
            }
            data.set("processingFacilities", facilitiesArray);

            // Statistics
            ObjectNode stats = objectMapper.createObjectNode();
            stats.put("totalRoutes", routes.size());
            stats.put("activeRoutes", (int) routes.values().stream().filter(r -> "active".equals(r.status)).count());
            stats.put("totalMiningOperations", miningOps.size());
            stats.put("activeMiningOperations", (int) miningOps.values().stream().filter(m -> "active".equals(m.status)).count());
            stats.put("totalFacilities", facilities.size());
            stats.put("operationalFacilities", (int) facilities.values().stream().filter(f -> "operational".equals(f.status)).count());
            double totalProduction = miningOps.values().stream().mapToDouble(m -> m.currentOutput).sum()
                    + facilities.values().stream().mapToDouble(f -> f.productionRate * f.efficiency).sum();
            stats.put("totalProductionRate", totalProduction);
            data.set("statistics", stats);

            ObjectNode message = objectMapper.createObjectNode();
            message.put("type", "pipeline_snapshot");
            message.set("data", data);

            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(message)));
        } catch (IOException e) {
            log.error("Failed to send pipeline snapshot", e);
        }
    }

    /**
     * Send ping response.
     */
    private void sendPingResponse(WebSocketSession session) {
        try {
            ObjectNode message = objectMapper.createObjectNode();
            message.put("type", "pong");
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(message)));
        } catch (IOException e) {
            log.error("Failed to send pong", e);
        }
    }

    /**
     * Simulate periodic route progress updates.
     */
    private void simulateRouteUpdates() {
        Random random = new Random();

        for (RouteData route : routes.values()) {
            // Advance progress
            route.progress += random.nextDouble() * 0.1;
            if (route.progress >= 1.0) {
                route.progress = 0;
            }
            route.lastUpdate = System.currentTimeMillis();

            // Occasionally change status
            if (random.nextDouble() < 0.02) {
                route.status = "active".equals(route.status) ? "inactive" : "active";
            }

            try {
                ObjectNode data = objectMapper.createObjectNode();
                data.put("id", route.id);
                data.put("resourceType", route.resourceType);
                data.put("progress", route.progress);
                data.put("status", route.status);
                data.put("timestamp", System.currentTimeMillis());

                ObjectNode message = objectMapper.createObjectNode();
                message.put("type", "route_update");
                message.set("data", data);

                broadcast(new TextMessage(objectMapper.writeValueAsString(message)));
            } catch (IOException e) {
                log.error("Failed to send route update", e);
            }
        }
    }

    /**
     * Simulate periodic mining operation updates.
     */
    private void simulateMiningUpdates() {
        Random random = new Random();

        for (MiningData mining : miningOps.values()) {
            // Fluctuate production
            mining.currentOutput = mining.productionRate * (0.5 + random.nextDouble() * 0.5);
            mining.lastUpdate = System.currentTimeMillis();

            // Occasionally change status
            if (random.nextDouble() < 0.03) {
                mining.status = "active".equals(mining.status) ? "maintenance" : "active";
            }

            try {
                ObjectNode data = objectMapper.createObjectNode();
                data.put("id", mining.id);
                data.put("name", mining.name);
                data.put("resourceType", mining.resourceType);
                data.put("productionRate", mining.productionRate);
                data.put("currentOutput", mining.currentOutput);
                data.put("status", mining.status);
                data.put("timestamp", System.currentTimeMillis());

                ObjectNode message = objectMapper.createObjectNode();
                message.put("type", "mining_update");
                message.set("data", data);

                broadcast(new TextMessage(objectMapper.writeValueAsString(message)));
            } catch (IOException e) {
                log.error("Failed to send mining update", e);
            }
        }
    }

    /**
     * Simulate periodic facility updates.
     */
    private void simulateFacilityUpdates() {
        Random random = new Random();

        for (FacilityData facility : facilities.values()) {
            // Fluctuate efficiency
            facility.efficiency = Math.max(0.3, Math.min(1.0, facility.efficiency + (random.nextDouble() - 0.5) * 0.1));
            facility.lastUpdate = System.currentTimeMillis();

            // Occasionally change status
            if (random.nextDouble() < 0.02) {
                facility.status = "operational".equals(facility.status) ? "offline" : "operational";
            }

            try {
                ObjectNode data = objectMapper.createObjectNode();
                data.put("id", facility.id);
                data.put("name", facility.name);
                data.put("type", facility.type);
                data.put("productionRate", facility.productionRate);
                data.put("efficiency", facility.efficiency);
                data.put("status", facility.status);
                data.put("timestamp", System.currentTimeMillis());

                ObjectNode message = objectMapper.createObjectNode();
                message.put("type", "facility_update");
                message.set("data", data);

                broadcast(new TextMessage(objectMapper.writeValueAsString(message)));
            } catch (IOException e) {
                log.error("Failed to send facility update", e);
            }
        }
    }

    /**
     * Simulate periodic statistics updates.
     */
    private void simulateStatisticsUpdate() {
        try {
            ObjectNode stats = objectMapper.createObjectNode();
            stats.put("totalRoutes", routes.size());
            stats.put("activeRoutes", (int) routes.values().stream().filter(r -> "active".equals(r.status)).count());
            stats.put("totalMiningOperations", miningOps.size());
            stats.put("activeMiningOperations", (int) miningOps.values().stream().filter(m -> "active".equals(m.status)).count());
            stats.put("totalFacilities", facilities.size());
            stats.put("operationalFacilities", (int) facilities.values().stream().filter(f -> "operational".equals(f.status)).count());
            double totalProduction = miningOps.values().stream().mapToDouble(m -> m.currentOutput).sum()
                    + facilities.values().stream().mapToDouble(f -> f.productionRate * f.efficiency).sum();
            stats.put("totalProductionRate", totalProduction);
            stats.put("timestamp", System.currentTimeMillis());

            ObjectNode message = objectMapper.createObjectNode();
            message.put("type", "statistics_update");
            message.set("data", stats);

            broadcast(new TextMessage(objectMapper.writeValueAsString(message)));
        } catch (IOException e) {
            log.error("Failed to send statistics update", e);
        }
    }

    /**
     * Broadcast message to all connected sessions.
     */
    private void broadcast(TextMessage message) {
        for (List<WebSocketSession> group : galaxyGroups.values()) {
            List<WebSocketSession> snapshot;
            synchronized (group) {
                snapshot = new ArrayList<>(group);
            }
            for (WebSocketSession session : snapshot) {
                if (session.isOpen()) {
                    try {
                        session.sendMessage(message);
                    } catch (IOException e) {
                        log.error("Failed to broadcast to session {}", session.getId(), e);
                    }
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
     * Get connection count.
     */
    public int getConnectionCount() {
        return sessionGalaxyIds.size();
    }

    /**
     * Get route count.
     */
    public int getRouteCount() {
        return routes.size();
    }

    /**
     * Get mining operation count.
     */
    public int getMiningOperationCount() {
        return miningOps.size();
    }

    /**
     * Get facility count.
     */
    public int getFacilityCount() {
        return facilities.size();
    }

    /**
     * Broadcast a galaxy event to all connected sessions.
     * Called by GalaxyEventBridge when real galaxy events occur.
     *
     * @param message the JSON message to broadcast
     */
    public void broadcastGalaxyEvent(ObjectNode message) {
        try {
            String json = objectMapper.writeValueAsString(message);
            broadcast(new TextMessage(json));
        } catch (Exception e) {
            log.error("Failed to broadcast galaxy event to resource pipeline", e);
        }
    }

    // Data classes

    private static class RouteData {
        String id;
        String resourceType;
        String sourceName;
        String destinationName;
        double sourceX, sourceY, sourceZ;
        double destX, destY, destZ;
        double volume;
        double speed;
        double progress;
        String status;
        int shipsOnRoute;
        long lastUpdate;
    }

    private static class MiningData {
        String id;
        String name;
        String resourceType;
        double x, y, z;
        double productionRate;
        double capacity;
        double currentOutput;
        String status;
        long lastUpdate;
    }

    private static class FacilityData {
        String id;
        String name;
        String type;
        double x, y, z;
        double productionRate;
        double efficiency;
        String status;
        long lastUpdate;
    }

    /**
     * Refresh real pipeline data from GameStateBridge.
     */
    private void refreshRealData() {
        if (gameStateBridge == null) return;
        for (String galaxyId : galaxyGroups.keySet()) {
            ObjectNode snapshot = gameStateBridge.getPipelineSnapshot(galaxyId);
            if (snapshot != null) {
                try {
                    String json = objectMapper.writeValueAsString(snapshot);
                    for (WebSocketSession session : galaxyGroups.get(galaxyId)) {
                        if (session.isOpen()) {
                            session.sendMessage(new TextMessage(json));
                        }
                    }
                } catch (Exception e) {
                    log.warn("Failed to broadcast real pipeline data for galaxy {}: {}", galaxyId, e.getMessage());
                }
            }
        }
    }

    public boolean isRealDataMode() {
        return realDataMode;
    }
}
