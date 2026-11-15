package com.yisy.modules.doramcu.fps.server;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yisy.modules.doramcu.thirdreich.commander.AICommander;
import com.yisy.modules.doramcu.thirdreich.domain.ThirdReichBattleResult;
import com.yisy.modules.doramcu.thirdreich.domain.ThirdReichGroundUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Service that synchronizes 3rd Reich battle state to FPS clients via WebSocket.
 * <p>
 * Subscribes to battle events (start, round advance, end) and publishes
 * battle state updates to connected FPS WebSocket clients.
 * </p>
 */
@Service
public class FpsBattleSyncService {

    private static final Logger log = LoggerFactory.getLogger(FpsBattleSyncService.class);

    private final ObjectMapper objectMapper;

    /** Connected FPS WebSocket sessions */
    private final List<WebSocketSession> sessions = new CopyOnWriteArrayList<>();

    /** Active battles keyed by battle ID */
    private final Map<String, ActiveBattle> activeBattles = new ConcurrentHashMap<>();

    private final AtomicInteger battleIdCounter = new AtomicInteger(1);

    public FpsBattleSyncService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Register a WebSocket session for battle updates.
     *
     * @param session the WebSocket session
     */
    public void addSession(WebSocketSession session) {
        sessions.add(session);
        log.info("FPS session registered: {}", session.getId());
    }

    /**
     * Remove a WebSocket session.
     *
     * @param session the WebSocket session
     */
    public void removeSession(WebSocketSession session) {
        sessions.remove(session);
        log.info("FPS session removed: {}", session.getId());
    }

    /**
     * Get the number of connected sessions.
     */
    public int getConnectedSessions() {
        return sessions.size();
    }

    /**
     * Publish a battle start event.
     *
     * @param attackerFaction the attacking faction
     * @param defenderFaction the defending faction
     * @param attackerUnits   the attacker's units
     * @param defenderUnits   the defender's units
     * @return the battle ID
     */
    public String publishBattleStart(String attackerFaction, String defenderFaction,
                                     List<ThirdReichGroundUnit> attackerUnits,
                                     List<ThirdReichGroundUnit> defenderUnits) {
        String battleId = "BATTLE-" + battleIdCounter.getAndIncrement();

        ActiveBattle battle = new ActiveBattle(battleId, attackerFaction, defenderFaction);
        battle.setPhase(BattlePhase.START);
        battle.setAttackerUnits(attackerUnits);
        battle.setDefenderUnits(defenderUnits);
        battle.setRound(0);
        activeBattles.put(battleId, battle);

        // Send battle start message to all clients
        Map<String, Object> message = new HashMap<>();
        message.put("type", "BATTLE_START");
        message.put("battleId", battleId);
        message.put("attackerFaction", attackerFaction);
        message.put("defenderFaction", defenderFaction);
        message.put("attackerUnitCount", attackerUnits.size());
        message.put("defenderUnitCount", defenderUnits.size());

        broadcast(message);
        log.info("Battle started: {} - {} vs {}", battleId, attackerFaction, defenderFaction);

        return battleId;
    }

    /**
     * Publish a round advance event.
     *
     * @param battleId the battle ID
     * @param round    the current round number
     * @param units    the current unit states
     */
    public void publishRoundAdvance(String battleId, int round, List<ThirdReichGroundUnit> units) {
        ActiveBattle battle = activeBattles.get(battleId);
        if (battle == null) {
            log.warn("No active battle found for ID: {}", battleId);
            return;
        }

        battle.setPhase(BattlePhase.IN_PROGRESS);
        battle.setRound(round);
        battle.setUnits(units);

        // Build unit states for broadcast
        List<Map<String, Object>> unitStates = new ArrayList<>();
        for (ThirdReichGroundUnit unit : units) {
            Map<String, Object> state = new HashMap<>();
            state.put("id", unit.getId());
            state.put("name", unit.getName());
            state.put("unitType", unit.getUnitType().name());
            state.put("strength", unit.getStrength());
            state.put("maxStrength", unit.getMaxStrength());
            state.put("morale", unit.getMorale());
            state.put("attackValue", unit.getAttackValue());
            state.put("defenseValue", unit.getDefenseValue());
            state.put("active", unit.isCombatEffective());
            unitStates.add(state);
        }

        Map<String, Object> message = new HashMap<>();
        message.put("type", "ROUND_ADVANCE");
        message.put("battleId", battleId);
        message.put("round", round);
        message.put("units", unitStates);

        broadcast(message);
        log.debug("Round {} advanced for battle {}", round, battleId);
    }

    /**
     * Publish a commander order event.
     *
     * @param battleId    the battle ID
     * @param commander   the commander issuing the order
     * @param orderType   the type of order
     * @param targetUnitId the target unit ID
     */
    public void publishCommanderOrder(String battleId, AICommander commander,
                                      String orderType, String targetUnitId) {
        Map<String, Object> message = new HashMap<>();
        message.put("type", "COMMANDER_ORDER");
        message.put("battleId", battleId);
        message.put("commanderId", commander.getId());
        message.put("commanderName", commander.getName());
        message.put("orderType", orderType);
        message.put("targetUnitId", targetUnitId);

        broadcast(message);
    }

    /**
     * Publish a battle end event.
     *
     * @param battleId the battle ID
     * @param result   the battle result
     */
    public void publishBattleEnd(String battleId, ThirdReichBattleResult result) {
        ActiveBattle battle = activeBattles.get(battleId);
        if (battle != null) {
            battle.setPhase(BattlePhase.ENDED);
            battle.setOutcome(result.getOutcome());
        }

        Map<String, Object> message = new HashMap<>();
        message.put("type", "BATTLE_END");
        message.put("battleId", battleId);
        message.put("outcome", result.getOutcome().name());
        message.put("attackerCasualties", result.getAttackerCasualties());
        message.put("defenderCasualties", result.getDefenderCasualties());
        message.put("attackerUnitsLost", result.getAttackerUnitsLost());
        message.put("defenderUnitsLost", result.getDefenderUnitsLost());
        message.put("roundsFought", result.getRoundsFought());

        broadcast(message);
        log.info("Battle ended: {} - {}", battleId, result.getOutcome());

        // Clean up after a delay
        activeBattles.remove(battleId);
    }

    /**
     * Get an active battle by ID.
     *
     * @param battleId the battle ID
     * @return the active battle, or null if not found
     */
    public ActiveBattle getActiveBattle(String battleId) {
        return activeBattles.get(battleId);
    }

    /**
     * Get all active battles.
     */
    public List<ActiveBattle> getActiveBattles() {
        return new ArrayList<>(activeBattles.values());
    }

    /**
     * Broadcast a message to all connected WebSocket sessions.
     */
    private void broadcast(Map<String, Object> message) {
        if (sessions.isEmpty()) {
            return;
        }

        try {
            String json = objectMapper.writeValueAsString(message);
            TextMessage textMessage = new TextMessage(json);

            List<WebSocketSession> toRemove = new ArrayList<>();
            for (WebSocketSession session : sessions) {
                try {
                    if (session.isOpen()) {
                        session.sendMessage(textMessage);
                    } else {
                        toRemove.add(session);
                    }
                } catch (IOException e) {
                    log.warn("Failed to send message to session {}: {}", session.getId(), e.getMessage());
                    toRemove.add(session);
                }
            }
            sessions.removeAll(toRemove);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize battle message: {}", e.getMessage());
        }
    }

    /**
     * Battle phase enumeration.
     */
    public enum BattlePhase {
        START,
        IN_PROGRESS,
        ENDED
    }

    /**
     * Active battle state.
     */
    public static class ActiveBattle {
        private final String battleId;
        private final String attackerFaction;
        private final String defenderFaction;
        private final long startTime;

        private BattlePhase phase;
        private int round;
        private ThirdReichBattleResult.Outcome outcome;
        private List<ThirdReichGroundUnit> attackerUnits;
        private List<ThirdReichGroundUnit> defenderUnits;
        private List<ThirdReichGroundUnit> units;
        private List<Map<String, Object>> events;

        public ActiveBattle(String battleId, String attackerFaction, String defenderFaction) {
            this.battleId = battleId;
            this.attackerFaction = attackerFaction;
            this.defenderFaction = defenderFaction;
            this.startTime = System.currentTimeMillis();
            this.phase = BattlePhase.START;
            this.events = new ArrayList<>();
        }

        public String getBattleId() { return battleId; }
        public String getAttackerFaction() { return attackerFaction; }
        public String getDefenderFaction() { return defenderFaction; }
        public long getStartTime() { return startTime; }
        public BattlePhase getPhase() { return phase; }
        public void setPhase(BattlePhase phase) { this.phase = phase; }
        public int getRound() { return round; }
        public void setRound(int round) { this.round = round; }
        public ThirdReichBattleResult.Outcome getOutcome() { return outcome; }
        public void setOutcome(ThirdReichBattleResult.Outcome outcome) { this.outcome = outcome; }
        public List<ThirdReichGroundUnit> getAttackerUnits() { return attackerUnits; }
        public void setAttackerUnits(List<ThirdReichGroundUnit> attackerUnits) { this.attackerUnits = attackerUnits; }
        public List<ThirdReichGroundUnit> getDefenderUnits() { return defenderUnits; }
        public void setDefenderUnits(List<ThirdReichGroundUnit> defenderUnits) { this.defenderUnits = defenderUnits; }
        public List<ThirdReichGroundUnit> getUnits() { return units; }
        public void setUnits(List<ThirdReichGroundUnit> units) { this.units = units; }
        public List<Map<String, Object>> getEvents() { return events; }

        public void addEvent(String eventType, Map<String, Object> data) {
            Map<String, Object> event = new HashMap<>();
            event.put("type", eventType);
            event.put("timestamp", System.currentTimeMillis());
            event.put("data", data);
            events.add(event);
        }
    }
}
