package com.yisy.modules.doramcu.sfb.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.yisy.modules.doramcu.sfb.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * WebSocket handler for SFB real-time multiplayer battles.
 * <p>
 * Each connected player has one WebSocket session. The handler:
 * <ul>
 *   <li>Joins a player to a battle on connection</li>
 *   <li>Dispatches incoming messages by type (action, impulse advance, etc.)</li>
 *   <li>Broadcasts game state changes to all connected players</li>
 *   <li>Manages impulse progression and turn synchronization</li>
 * </ul>
 * </p>
 */
@Service
public class SfbWebSocketHandler extends TextWebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(SfbWebSocketHandler.class);

    private final SfbCombatEngine combatEngine;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /** Battle ID → list of connected player sessions. */
    private final ConcurrentHashMap<String, List<WebSocketSession>> battleSessions = new ConcurrentHashMap<>();

    /** Battle ID → current scenario state. */
    private final ConcurrentHashMap<String, SfbScenario> battles = new ConcurrentHashMap<>();

    /** Session → player side (0 or 1) mapping. */
    private final ConcurrentHashMap<WebSocketSession, Integer> playerSides = new ConcurrentHashMap<>();

    public SfbWebSocketHandler(SfbCombatEngine combatEngine) {
        this.combatEngine = combatEngine;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // Extract battle ID and player side from query parameters
        String battleId = getQueryParam(session, "battleId");
        String sideParam = getQueryParam(session, "side");

        if (battleId == null || battleId.isEmpty()) {
            log.warn("WebSocket connection without battleId — closing");
            session.close(CloseStatus.BAD_DATA);
            return;
        }

        int side = 0;
        if (sideParam != null) {
            try {
                side = Integer.parseInt(sideParam);
            } catch (NumberFormatException e) {
                log.warn("Invalid side parameter: {}", sideParam);
            }
        }

        // Register session for this battle
        battleSessions.computeIfAbsent(battleId, k -> new CopyOnWriteArrayList<>()).add(session);
        playerSides.put(session, side);

        log.info("Player connected to SFB battle {} as side {} — total players: {}",
                battleId, side, battleSessions.get(battleId).size());

        // Send connection acknowledgment
        ObjectNode ack = objectMapper.createObjectNode();
        ack.put("type", "CONNECT_ACK");
        ack.put("battleId", battleId);
        ack.put("side", side);
        ack.put("message", "Connected to battle");
        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(ack)));

        // If battle exists, send current game state
        SfbScenario scenario = battles.get(battleId);
        if (scenario != null) {
            broadcastGameState(battleId, scenario);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        Integer side = playerSides.remove(session);
        if (side != null) {
            // Find the battle this session belonged to
            for (Map.Entry<String, List<WebSocketSession>> entry : battleSessions.entrySet()) {
                if (entry.getValue().remove(session)) {
                    String battleId = entry.getKey();
                    log.info("Player disconnected from SFB battle {} (side {}) — status: {}",
                            battleId, side, status.getCode());

                    // If no players left, clean up
                    if (entry.getValue().isEmpty()) {
                        battleSessions.remove(battleId);
                        log.info("No players left in battle {} — battle ended", battleId);
                    } else {
                        // Notify remaining players
                        ObjectNode msg = objectMapper.createObjectNode();
                        msg.put("type", "PLAYER_DISCONNECTED");
                        msg.put("side", side);
                        broadcastToBattle(battleId, msg);
                    }
                    break;
                }
            }
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        try {
            JsonNode root = objectMapper.readTree(message.getPayload());
            String type = root.path("type").asText("");
            String battleId = root.path("battleId").asText("");

            log.debug("Received WS message type={} for battle={}", type, battleId);

            switch (type) {
                case "START_BATTLE":
                    handleStartBattle(session, battleId, root);
                    break;
                case "ACTION":
                    handleAction(session, battleId, root);
                    break;
                case "ADVANCE_IMPULSE":
                    handleAdvanceImpulse(session, battleId);
                    break;
                case "ADVANCE_TURN":
                    handleAdvanceTurn(session, battleId);
                    break;
                case "GET_STATE":
                    handleGetState(session, battleId);
                    break;
                default:
                    log.warn("Unknown WS message type: {} from {}", type, session.getId());
            }
        } catch (JsonProcessingException e) {
            log.error("Failed to parse WebSocket message: {}", e.getMessage());
        } catch (Exception e) {
            log.error("Error handling WebSocket message: {}", e.getMessage(), e);
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        log.error("WebSocket transport error: {}", exception.getMessage());
    }

    // ── Message Handlers ──────────────────────────────────────────────────────

    private void handleStartBattle(WebSocketSession session, String battleId, JsonNode root) {
        // Create a new scenario for this battle
        SfbScenario scenario = new SfbScenario();
        scenario.setId(battleId);
        scenario.setCurrentTurn(0);
        scenario.setCurrentImpulse(0);
        scenario.setCurrentPhase(SfbScenario.ScenarioPhase.SETUP);

        battles.put(battleId, scenario);

        log.info("Battle {} started", battleId);

        // Acknowledge
        ObjectNode ack = objectMapper.createObjectNode();
        ack.put("type", "BATTLE_STARTED");
        ack.put("battleId", battleId);
        try {
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(ack)));
        } catch (IOException e) {
            log.error("Failed to send battle started: {}", e.getMessage());
        }
    }

    private void handleAction(WebSocketSession session, String battleId, JsonNode root) {
        SfbScenario scenario = battles.get(battleId);
        if (scenario == null) {
            sendError(session, "Battle not found: " + battleId);
            return;
        }

        String shipId = root.path("shipId").asText("");
        String actionType = root.path("actionType").asText("");
        JsonNode parameters = root.path("parameters");

        log.info("Player action: {} for ship {} in battle {}", actionType, shipId, battleId);

        // Process the action based on type
        switch (actionType) {
            case "ENERGY_ALLOCATE":
                // Energy allocation is handled at turn start by the engine
                sendAck(session, "ENERGY_ALLOCATE", "Energy allocation processed");
                break;
            case "MOVE":
                // Movement is handled by the engine based on speed
                sendAck(session, "MOVE", "Movement processed");
                break;
            case "FIRE":
                // Weapons fire is handled by the engine
                sendAck(session, "FIRE", "Weapons fire processed");
                break;
            case "SPECIAL_SYSTEM":
                // Special systems (cloaking, tractors, etc.)
                sendAck(session, "SPECIAL_SYSTEM", "Special system activated");
                break;
            default:
                sendError(session, "Unknown action type: " + actionType);
        }
    }

    private void handleAdvanceImpulse(WebSocketSession session, String battleId) {
        SfbScenario scenario = battles.get(battleId);
        if (scenario == null) {
            sendError(session, "Battle not found: " + battleId);
            return;
        }

        if (scenario.getCurrentImpulse() < 32) {
            scenario.nextImpulse();
            log.info("Battle {} advanced to impulse {}", battleId, scenario.getCurrentImpulse());

            ObjectNode msg = objectMapper.createObjectNode();
            msg.put("type", "IMPULSE_ADVANCED");
            msg.put("impulse", scenario.getCurrentImpulse());
            broadcastToBattle(battleId, msg);
        }
    }

    private void handleAdvanceTurn(WebSocketSession session, String battleId) {
        SfbScenario scenario = battles.get(battleId);
        if (scenario == null) {
            sendError(session, "Battle not found: " + battleId);
            return;
        }

        // Run a complete turn through the combat engine
        boolean over = combatEngine.runTurn(scenario);

        // Broadcast updated game state
        broadcastGameState(battleId, scenario);

        if (over) {
            ObjectNode msg = objectMapper.createObjectNode();
            msg.put("type", "BATTLE_ENDED");
            msg.put("winner", determineWinner(scenario));
            broadcastToBattle(battleId, msg);
            log.info("Battle {} ended", battleId);
        } else {
            log.info("Battle {} completed turn {}", battleId, scenario.getCurrentTurn());
        }
    }

    private void handleGetState(WebSocketSession session, String battleId) {
        SfbScenario scenario = battles.get(battleId);
        if (scenario == null) {
            sendError(session, "Battle not found: " + battleId);
            return;
        }

        // Send current game state to requesting player
        ObjectNode state = createGameState(scenario);
        try {
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(state)));
        } catch (IOException e) {
            log.error("Failed to send game state: {}", e.getMessage());
        }
    }

    // ── Broadcast Methods ─────────────────────────────────────────────────────

    /**
     * Broadcast game state to all players in a battle.
     */
    public void broadcastGameState(String battleId, SfbScenario scenario) {
        ObjectNode state = createGameState(scenario);
        broadcastToBattle(battleId, state);
    }

    /**
     * Create a game state JSON object from the scenario.
     */
    private ObjectNode createGameState(SfbScenario scenario) {
        ObjectNode state = objectMapper.createObjectNode();
        state.put("type", "GAME_STATE");
        state.put("battleId", scenario.getId());
        state.put("turn", scenario.getCurrentTurn());
        state.put("impulse", scenario.getCurrentImpulse());
        state.put("phase", scenario.getCurrentPhase().name());

        // Add ship snapshots
        var shipsArray = state.putArray("ships");
        for (SfbShip ship : scenario.getShips()) {
            var shipNode = objectMapper.createObjectNode();
            shipNode.put("id", ship.getId());
            shipNode.put("name", ship.getName());
            shipNode.put("side", ship.getSide());
            shipNode.put("speed", ship.getSpeed());
            shipNode.put("hexesMoved", ship.getHexesMovedThisTurn());
            shipNode.put("cloaked", ship.isCloaked());
            shipNode.put("crippled", ship.isCrippled());
            shipNode.put("destroyed", ship.isDestroyed());

            if (ship.getPosition() != null) {
                shipNode.put("q", ship.getPosition().getQ());
                shipNode.put("r", ship.getPosition().getR());
                shipNode.put("facing", ship.getPosition().getFacing());
            }

            // Add shield status
            var shieldsArray = shipNode.putArray("shields");
            for (SfbShield shield : ship.getShields()) {
                if (shield != null) {
                    var shieldNode = objectMapper.createObjectNode();
                    shieldNode.put("direction", shield.getDirection());
                    shieldNode.put("boxes", shield.getBoxesRemaining());
                    shieldNode.put("reinforcement", shield.getReinforcementPoints());
                    shieldNode.put("down", shield.isDown());
                    shieldsArray.add(shieldNode);
                }
            }

            shipsArray.add(shipNode);
        }

        // Add recent events
        var eventsArray = state.putArray("events");
        List<SfbEvent> events = scenario.getEventLog();
        int start = Math.max(0, events.size() - 20); // Last 20 events
        for (int i = start; i < events.size(); i++) {
            SfbEvent event = events.get(i);
            var eventNode = objectMapper.createObjectNode();
            eventNode.put("turn", event.getTurn());
            eventNode.put("impulse", event.getImpulse());
            eventNode.put("type", event.getType().name());
            eventNode.put("message", event.getMessage());
            eventsArray.add(eventNode);
        }

        return state;
    }

    /**
     * Broadcast a message to all players in a battle.
     */
    private void broadcastToBattle(String battleId, JsonNode message) {
        List<WebSocketSession> sessions = battleSessions.get(battleId);
        if (sessions == null || sessions.isEmpty()) {
            return;
        }

        TextMessage textMsg;
        try {
            textMsg = new TextMessage(objectMapper.writeValueAsString(message));
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize message: {}", e.getMessage());
            return;
        }

        int sent = 0;
        for (WebSocketSession session : sessions) {
            try {
                if (session.isOpen()) {
                    session.sendMessage(textMsg);
                    sent++;
                }
            } catch (IOException e) {
                log.warn("Failed to send to {}: {}", session.getId(), e.getMessage());
            }
        }
        if (sent > 0) {
            log.debug("Broadcast to {} players in battle {}", sent, battleId);
        }
    }

    /**
     * Send an acknowledgment to a player.
     */
    private void sendAck(WebSocketSession session, String actionType, String message) {
        ObjectNode ack = objectMapper.createObjectNode();
        ack.put("type", "ACK");
        ack.put("actionType", actionType);
        ack.put("message", message);
        try {
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(ack)));
        } catch (IOException e) {
            log.error("Failed to send ACK: {}", e.getMessage());
        }
    }

    /**
     * Send an error message to a player.
     */
    private void sendError(WebSocketSession session, String message) {
        ObjectNode error = objectMapper.createObjectNode();
        error.put("type", "ERROR");
        error.put("message", message);
        try {
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(error)));
        } catch (IOException e) {
            log.error("Failed to send error: {}", e.getMessage());
        }
    }

    // ── Utilities ─────────────────────────────────────────────────────────────

    private String getQueryParam(WebSocketSession session, String name) {
        String query = session.getUri().getQuery();
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

    /**
     * Get the number of connected players in a battle.
     */
    public int getPlayerCount(String battleId) {
        List<WebSocketSession> sessions = battleSessions.get(battleId);
        return sessions != null ? sessions.size() : 0;
    }

    /**
     * Get the list of active battle IDs.
     */
    public List<String> getActiveBattles() {
        return new ArrayList<>(battles.keySet());
    }

    /**
     * Determine the winner of a battle.
     */
    private int determineWinner(SfbScenario scenario) {
        List<SfbShip> side0 = scenario.getActiveShipsBySide(0);
        List<SfbShip> side1 = scenario.getActiveShipsBySide(1);

        if (side0.isEmpty() && side1.isEmpty()) return -1; // Mutual destruction
        if (side0.isEmpty()) return 1;
        if (side1.isEmpty()) return 0;
        return -1; // Should not reach here
    }
}
