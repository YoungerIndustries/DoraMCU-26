package com.yisy.modules.doramcu.controller;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.yisy.modules.doramcu.service.Galaxy3DService;
import com.yisy.modules.doramcu.service.GameStateBridge;
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
import java.util.Collections;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * WebSocket handler for real-time fleet tracking in 3D galaxy visualization.
 * <p>
 * Connection URL: {@code ws://host:port/ws/fleet-tracker?sessionId=ABC&playerId=1}
 * </p>
 * <p>
 * Server-to-client message types:
 * <ul>
 *   <li>{@code fleet_update} — Fleet position and status update</li>
 *   <li>{@code fleet_moved} — Fleet movement animation data</li>
 *   <li>{@code fleet_combat} — Combat event with attack details</li>
 *   <li>{@code fleet_destroyed} — Fleet destruction with explosion position</li>
 *   <li>{@code fleet_created} — New fleet spawned</li>
 * </ul>
 * </p>
 * <p>
 * Client-to-server message types:
 * <ul>
 *   <li>{@code subscribe} — Subscribe to fleet updates for a galaxy</li>
 *   <li>{@code ping} — Keep-alive ping</li>
 * </ul>
 * </p>
 */
@Service
public class FleetTrackerWebSocketHandler extends TextWebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(FleetTrackerWebSocketHandler.class);

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Galaxy3DService galaxy3DService = new Galaxy3DService();
    private final GameStateBridge gameStateBridge;
    private final boolean realDataMode;

    // Session management
    private final Map<WebSocketSession, String> sessionPlayerIds = new ConcurrentHashMap<>();
    private final Map<WebSocketSession, String> sessionSessionIds = new ConcurrentHashMap<>();
    private final Map<String, List<WebSocketSession>> sessionGroups = new ConcurrentHashMap<>();

    // Simulated fleet data
    private final Map<String, FleetData> fleets = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    @SuppressWarnings("unused")
    private final Set<ScheduledFuture<?>> scheduledTasks = Collections.newSetFromMap(new ConcurrentHashMap<>());

    /**
     * Simulated fleet data structure.
     */
    static class FleetData {
        String fleetId;
        String name;
        String type; // warship, transport, fighter, capital, support
        double x, y, z;
        double health;
        double maxHealth;
        String sessionId;
        String playerId;
        long lastUpdate;
    }

    public FleetTrackerWebSocketHandler() {
        this(null, false);
    }

    public FleetTrackerWebSocketHandler(GameStateBridge gameStateBridge, boolean realDataMode) {
        this.gameStateBridge = gameStateBridge;
        this.realDataMode = realDataMode;
        // Start simulation tasks only when real data mode is disabled
        if (!realDataMode) {
            scheduler.scheduleAtFixedRate(this::simulateFleetMovements, 5, 5, TimeUnit.SECONDS);
            scheduler.scheduleAtFixedRate(this::simulateFleetUpdates, 8, 8, TimeUnit.SECONDS);
            scheduler.scheduleAtFixedRate(this::simulateCombatEvents, 15, 20, TimeUnit.SECONDS);
            scheduler.scheduleAtFixedRate(this::simulateFleetCreation, 30, 45, TimeUnit.SECONDS);
        } else {
            log.info("FleetTracker starting in real-data mode with periodic refresh");
            scheduler.scheduleAtFixedRate(this::refreshRealData, 10, 10, TimeUnit.SECONDS);
        }
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        String sessionId = session.getUri().getQuery();
        String playerId = extractParameter(session.getUri().getQuery(), "playerId");
        String gameSessionId = extractParameter(session.getUri().getQuery(), "sessionId");

        sessionPlayerIds.put(session, playerId);
        sessionSessionIds.put(session, gameSessionId);

        // Group by game session
        sessionGroups.computeIfAbsent(gameSessionId, k -> Collections.synchronizedList(new ArrayList<>())).add(session);

        log.info("Fleet tracker connected: sessionId={}, playerId={}", gameSessionId, playerId);

        // Send initial fleet snapshot
        sendInitialFleets(session, gameSessionId);
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
                    log.warn("Unknown fleet tracker message type: {}", type);
            }
        } catch (Exception e) {
            log.error("Error handling fleet tracker message", e);
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
                    // Clean up fleets for this session
                    fleets.entrySet().removeIf(entry -> entry.getValue().sessionId.equals(sessionId));
                }
            }
        }

        log.info("Fleet tracker disconnected: sessionId={}, playerId={}", sessionId, playerId);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        log.error("Fleet tracker transport error", exception);
        afterConnectionClosed(session, CloseStatus.SERVER_ERROR);
    }

    /**
     * Handle subscribe request.
     */
    private void handleSubscribe(WebSocketSession session, ObjectNode payload) {
        String galaxyId = payload.has("galaxyId") ? payload.get("galaxyId").asText() : "Corellian";
        log.info("Client subscribing to fleet updates: galaxy={}", galaxyId);
        sendInitialFleets(session, galaxyId);
    }

    /**
     * Send initial fleet snapshot to a newly connected client.
     */
    private void sendInitialFleets(WebSocketSession session, String sessionId) {
        if (fleets.isEmpty()) {
            // Generate initial fleets
            generateInitialFleets(sessionId);
        }

        for (FleetData fleet : fleets.values()) {
            try {
                ObjectNode data = objectMapper.createObjectNode();
                data.put("fleetId", fleet.fleetId);
                data.put("name", fleet.name);
                data.put("type", fleet.type);
                data.put("sessionId", fleet.sessionId);
                data.put("playerId", fleet.playerId);

                ObjectNode position = objectMapper.createObjectNode();
                position.put("x", fleet.x);
                position.put("y", fleet.y);
                position.put("z", fleet.z);
                data.set("position", position);

                data.put("health", fleet.health);
                data.put("maxHealth", fleet.maxHealth);

                ObjectNode message = objectMapper.createObjectNode();
                message.put("type", "fleet_created");
                message.set("data", data);

                session.sendMessage(new TextMessage(objectMapper.writeValueAsString(message)));
            } catch (IOException e) {
                log.error("Failed to send fleet data", e);
            }
        }
    }

    /**
     * Generate initial simulated fleets.
     */
    private void generateInitialFleets(String sessionId) {
        Random random = new Random();
        String[] types = {"warship", "transport", "fighter", "capital", "support"};
        String[] names = {"Alpha", "Bravo", "Charlie", "Delta", "Echo", "Foxtrot", "Golf", "Hotel"};

        for (int i = 0; i < 8; i++) {
            String fleetId = "fleet_" + (i + 1);
            FleetData fleet = new FleetData();
            fleet.fleetId = fleetId;
            fleet.name = names[i] + " Squadron";
            fleet.type = types[i % types.length];
            fleet.x = random.nextDouble() * 200 - 100;
            fleet.y = random.nextDouble() * 50 - 25;
            fleet.z = random.nextDouble() * 200 - 100;
            fleet.health = 100;
            fleet.maxHealth = 100;
            fleet.sessionId = sessionId;
            fleet.playerId = "player_" + (random.nextInt(4) + 1);
            fleet.lastUpdate = System.currentTimeMillis();
            fleets.put(fleetId, fleet);
        }
    }

    /**
     * Simulate fleet movements.
     */
    private void simulateFleetMovements() {
        Random random = new Random();

        for (FleetData fleet : fleets.values()) {
            double oldX = fleet.x;
            double oldY = fleet.y;
            double oldZ = fleet.z;

            fleet.x += (random.nextDouble() - 0.5) * 10;
            fleet.y += (random.nextDouble() - 0.5) * 3;
            fleet.z += (random.nextDouble() - 0.5) * 10;
            fleet.lastUpdate = System.currentTimeMillis();

            try {
                ObjectNode data = objectMapper.createObjectNode();
                data.put("fleetId", fleet.fleetId);

                ObjectNode fromPosition = objectMapper.createObjectNode();
                fromPosition.put("x", oldX);
                fromPosition.put("y", oldY);
                fromPosition.put("z", oldZ);
                data.set("fromPosition", fromPosition);

                ObjectNode toPosition = objectMapper.createObjectNode();
                toPosition.put("x", fleet.x);
                toPosition.put("y", fleet.y);
                toPosition.put("z", fleet.z);
                data.set("toPosition", toPosition);

                data.put("duration", 2000 + random.nextInt(2000));

                ObjectNode message = objectMapper.createObjectNode();
                message.put("type", "fleet_moved");
                message.set("data", data);

                broadcast(new TextMessage(objectMapper.writeValueAsString(message)));
            } catch (IOException e) {
                log.error("Failed to send fleet movement", e);
            }
        }
    }

    /**
     * Simulate periodic fleet status updates (health, position snapshots).
     * Sends 'fleet_update' messages so the frontend can refresh fleet state.
     */
    private void simulateFleetUpdates() {
        Random random = new Random();

        for (FleetData fleet : fleets.values()) {
            // Simulate small health changes (regeneration or damage from ambient effects)
            double healthChange = (random.nextDouble() - 0.4) * 2;
            fleet.health = Math.max(0, Math.min(fleet.maxHealth, fleet.health + healthChange));
            fleet.lastUpdate = System.currentTimeMillis();

            try {
                ObjectNode data = objectMapper.createObjectNode();
                data.put("fleetId", fleet.fleetId);
                data.put("name", fleet.name);
                data.put("type", fleet.type);

                ObjectNode position = objectMapper.createObjectNode();
                position.put("x", fleet.x);
                position.put("y", fleet.y);
                position.put("z", fleet.z);
                data.set("position", position);

                data.put("health", fleet.health);
                data.put("maxHealth", fleet.maxHealth);
                data.put("timestamp", System.currentTimeMillis());

                ObjectNode message = objectMapper.createObjectNode();
                message.put("type", "fleet_update");
                message.set("data", data);

                broadcast(new TextMessage(objectMapper.writeValueAsString(message)));
            } catch (IOException e) {
                log.error("Failed to send fleet update", e);
            }
        }
    }

    /**
     * Simulate combat events.
     */
    private void simulateCombatEvents() {
        if (fleets.size() < 2) return;

        Random random = new Random();
        List<String> fleetIds = new ArrayList<>(fleets.keySet());

        String attackerId = fleetIds.get(random.nextInt(fleetIds.size()));
        String defenderId;
        do {
            defenderId = fleetIds.get(random.nextInt(fleetIds.size()));
        } while (defenderId.equals(attackerId));

        FleetData attacker = fleets.get(attackerId);
        FleetData defender = fleets.get(defenderId);
        if (attacker == null || defender == null) return;

        double damage = 5 + random.nextDouble() * 20;
        defender.health = Math.max(0, defender.health - damage);

        try {
            ObjectNode data = objectMapper.createObjectNode();
            data.put("eventId", "combat_" + System.currentTimeMillis());
            data.put("attackerId", attackerId);
            data.put("defenderId", defenderId);
            data.put("damage", damage);
            data.put("timestamp", System.currentTimeMillis());

            ObjectNode attackerPos = objectMapper.createObjectNode();
            attackerPos.put("x", attacker.x);
            attackerPos.put("y", attacker.y);
            attackerPos.put("z", attacker.z);
            data.set("attackerPosition", attackerPos);

            ObjectNode defenderPos = objectMapper.createObjectNode();
            defenderPos.put("x", defender.x);
            defenderPos.put("y", defender.y);
            defenderPos.put("z", defender.z);
            data.set("defenderPosition", defenderPos);

            ObjectNode message = objectMapper.createObjectNode();
            message.put("type", "fleet_combat");
            message.set("data", data);

            broadcast(new TextMessage(objectMapper.writeValueAsString(message)));

            // Check if defender is destroyed
            if (defender.health <= 0) {
                ObjectNode destroyData = objectMapper.createObjectNode();
                destroyData.put("fleetId", defenderId);
                destroyData.put("size", 5 + random.nextDouble() * 5);

                ObjectNode position = objectMapper.createObjectNode();
                position.put("x", defender.x);
                position.put("y", defender.y);
                position.put("z", defender.z);
                destroyData.set("position", position);

                ObjectNode destroyMessage = objectMapper.createObjectNode();
                destroyMessage.put("type", "fleet_destroyed");
                destroyMessage.set("data", destroyData);

                broadcast(new TextMessage(objectMapper.writeValueAsString(destroyMessage)));
                fleets.remove(defenderId);
            }
        } catch (IOException e) {
            log.error("Failed to send combat event", e);
        }
    }

    /**
     * Simulate fleet creation.
     */
    private void simulateFleetCreation() {
        Random random = new Random();
        String[] types = {"warship", "transport", "fighter", "capital", "support"};
        String[] names = {"Nova", "Viper", "Hawk", "Titan", "Sentinel", "Phantom"};

        String fleetId = "fleet_" + System.currentTimeMillis();
        FleetData fleet = new FleetData();
        fleet.fleetId = fleetId;
        fleet.name = names[random.nextInt(names.length)] + " Squadron";
        fleet.type = types[random.nextInt(types.length)];
        fleet.x = random.nextDouble() * 200 - 100;
        fleet.y = random.nextDouble() * 50 - 25;
        fleet.z = random.nextDouble() * 200 - 100;
        fleet.health = 100;
        fleet.maxHealth = 100;
        fleet.sessionId = "default";
        fleet.playerId = "player_" + (random.nextInt(4) + 1);
        fleet.lastUpdate = System.currentTimeMillis();
        fleets.put(fleetId, fleet);

        try {
            ObjectNode data = objectMapper.createObjectNode();
            data.put("fleetId", fleet.fleetId);
            data.put("name", fleet.name);
            data.put("type", fleet.type);
            data.put("playerId", fleet.playerId);

            ObjectNode position = objectMapper.createObjectNode();
            position.put("x", fleet.x);
            position.put("y", fleet.y);
            position.put("z", fleet.z);
            data.set("position", position);

            data.put("health", fleet.health);
            data.put("maxHealth", fleet.maxHealth);

            ObjectNode message = objectMapper.createObjectNode();
            message.put("type", "fleet_created");
            message.set("data", data);

            broadcast(new TextMessage(objectMapper.writeValueAsString(message)));
        } catch (IOException e) {
            log.error("Failed to send fleet creation", e);
        }
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
     * Get connection count.
     */
    public int getConnectionCount() {
        return sessionPlayerIds.size();
    }

    /**
     * Get active session IDs.
     */
    public Set<String> getActiveSessionIds() {
        return sessionGroups.keySet();
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
            log.error("Failed to broadcast galaxy event to fleet tracker", e);
        }
    }

    /**
     * Refresh real data from GameStateBridge and broadcast to all connected sessions.
     */
    private void refreshRealData() {
        if (gameStateBridge == null) return;
        for (String sessionId : sessionGroups.keySet()) {
            ObjectNode snapshot = gameStateBridge.getFleetSnapshot(sessionId);
            if (snapshot != null) {
                try {
                    broadcast(new TextMessage(objectMapper.writeValueAsString(snapshot)));
                } catch (Exception e) {
                    log.warn("Failed to broadcast real fleet data for session {}: {}", sessionId, e.getMessage());
                }
            }
        }
    }

    /**
     * Returns whether real data mode is enabled.
     */
    public boolean isRealDataMode() {
        return realDataMode;
    }
}
