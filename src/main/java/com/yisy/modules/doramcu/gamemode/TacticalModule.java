package com.yisy.modules.doramcu.gamemode;

import com.yisy.modules.doramcu.gamecore.GameMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tactical combat game mode module: turn-based tactical battles
 * with positioning, cover, and unit abilities.
 * <p>
 * Provides unit management, battle scenarios, combat resolution,
 * and tactical map generation for tactical-style gameplay.
 * </p>
 */
@Component
public class TacticalModule implements GameModeModule {

    private static final Logger log = LoggerFactory.getLogger(TacticalModule.class);

    // Galaxy name -> battle state
    private final Map<String, BattleState> battles = new ConcurrentHashMap<>();

    // Galaxy name -> unit roster
    private final Map<String, List<Unit>> units = new ConcurrentHashMap<>();

    // Unit types
    private static final List<String> UNIT_TYPES = Arrays.asList(
            "Infantry", "Heavy Trooper", "Sniper", "Medic", "Engineer",
            "Mech Pilot", "Tank Commander", "Artillery", "Scout", "Assault"
    );

    // Battle scenarios
    private static final List<String> SCENARIOS = Arrays.asList(
            "Defend the space station from boarders",
            "Assault the enemy fortress",
            "Ambush the supply convoy",
            "Hold the line against the alien wave",
            "Infiltrate the command center",
            "Rescue the hostages from the compound",
            "Destroy the weapon depot",
            "Secure the strategic position"
    );

    @Override
    public GameMode getGameMode() {
        return GameMode.TACTICAL;
    }

    @Override
    public String getModuleName() {
        return "Tactical Combat Module";
    }

    @Override
    public String getDescription() {
        return "Turn-based tactical battles with positioning, cover, and unit abilities";
    }

    @Override
    public void initializeForGalaxy(String galaxyName, String loreSlug) {
        log.info("Initializing Tactical module for galaxy: {} with lore: {}", galaxyName, loreSlug);
        battles.put(galaxyName, new BattleState());
        units.put(galaxyName, new ArrayList<>());

        // Recruit initial units
        Random random = new Random();
        for (int i = 0; i < 5; i++) {
            String type = UNIT_TYPES.get(random.nextInt(UNIT_TYPES.size()));
            units.get(galaxyName).add(new Unit(type, 1 + random.nextInt(3)));
        }
        log.info("Recruited initial units for galaxy: {}", galaxyName);
    }

    @Override
    public String processTurn(String galaxyName, int turnNumber, String playerId) {
        BattleState battle = battles.getOrDefault(galaxyName, new BattleState());
        List<Unit> unitList = units.getOrDefault(galaxyName, Collections.emptyList());

        StringBuilder summary = new StringBuilder();
        summary.append("Tactical Turn ").append(turnNumber).append(" in ").append(galaxyName).append(":\n");

        // Check if there's an active battle
        if (!battle.isActive()) {
            // Start a new battle
            Random random = new Random();
            String scenario = SCENARIOS.get(random.nextInt(SCENARIOS.size()));
            battle.startBattle(scenario);
            summary.append("  âš” New battle: ").append(scenario).append("\n");
        } else {
            // Process battle
            battle.progress();
            summary.append("  âš” Battle: ").append(battle.getScenario()).append("\n");

            // Unit actions
            int surviving = 0;
            for (Unit unit : unitList) {
                if (unit.isAlive()) {
                    surviving++;
                    unit.takeAction();
                    if (unit.getHealth() <= 0) {
                        unit.setAlive(false);
                        summary.append("  âœ Unit lost: ").append(unit.getType())
                               .append(" (Lv.").append(unit.getLevel()).append(")\n");
                    }
                }
            }
            summary.append("  Surviving units: ").append(surviving).append("/").append(unitList.size()).append("\n");

            // Check battle end
            if (battle.isCompleted()) {
                boolean won = battle.getResult() == BattleResult.VICTORY;
                summary.append("  â˜… Battle ended: ").append(battle.getResult()).append("\n");

                if (won) {
                    // Rewards
                    int xpReward = 50 + new Random().nextInt(100);
                    battle.totalVictories++;
                    summary.append("  Victory! XP reward: ").append(xpReward).append("\n");

                    // Level up units
                    for (Unit unit : unitList) {
                        if (unit.isAlive() && new Random().nextDouble() < 0.3) {
                            unit.levelUp();
                            summary.append("  â†‘ ").append(unit.getType()).append(" leveled up to ").append(unit.getLevel()).append("\n");
                        }
                    }
                } else {
                    battle.totalDefeats++;
                    summary.append("  Defeat. Retreat and regroup.\n");
                }
                battle.endBattle();
            } else {
                summary.append("  Battle progress: ").append(battle.getProgress()).append("/").append(battle.getTargetProgress()).append("\n");
            }
        }

        // Recruit new units if affordable
        Random random = new Random();
        if (battle.totalVictories > 0 && unitList.size() < 15 && turnNumber % 4 == 0) {
            String type = UNIT_TYPES.get(random.nextInt(UNIT_TYPES.size()));
            unitList.add(new Unit(type, 1));
            summary.append("  âœ¦ Recruited new ").append(type).append("\n");
        }

        summary.append("  Record: ").append(battle.totalVictories).append("W - ").append(battle.totalDefeats).append("L");
        return summary.toString();
    }

    @Override
    public String getStateSummary(String galaxyName) {
        BattleState battle = battles.getOrDefault(galaxyName, new BattleState());
        List<Unit> unitList = units.getOrDefault(galaxyName, Collections.emptyList());
        long alive = unitList.stream().filter(Unit::isAlive).count();
        return String.format("Tactical: %d/%d units alive, %dW-%dL record",
                alive, unitList.size(), battle.totalVictories, battle.totalDefeats);
    }

    @Override
    public boolean isActiveForGalaxy(String galaxyName) {
        return battles.containsKey(galaxyName);
    }

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String serializeState(String galaxyName) {
        try {
            Map<String, Object> state = new LinkedHashMap<>();
            BattleState battle = battles.get(galaxyName);
            if (battle == null) return null;
            state.put("scenario", battle.scenario);
            state.put("progress", battle.progress);
            state.put("targetProgress", battle.targetProgress);
            state.put("active", battle.active);
            state.put("completed", battle.completed);
            state.put("result", battle.result != null ? battle.result.name() : null);
            state.put("totalVictories", battle.totalVictories);
            state.put("totalDefeats", battle.totalDefeats);
            List<Unit> unitList = units.getOrDefault(galaxyName, Collections.emptyList());
            List<Map<String, Object>> unitData = new ArrayList<>();
            for (Unit u : unitList) {
                Map<String, Object> ud = new LinkedHashMap<>();
                ud.put("type", u.getType());
                ud.put("level", u.getLevel());
                ud.put("health", u.getHealth());
                ud.put("maxHealth", u.getMaxHealth());
                ud.put("alive", u.isAlive());
                unitData.add(ud);
            }
            state.put("units", unitData);
            return objectMapper.writeValueAsString(state);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize Tactical state for {}: {}", galaxyName, e.getMessage());
            return null;
        }
    }

    @Override
    public void restoreState(String galaxyName, String stateJson) {
        if (stateJson == null || stateJson.isEmpty()) return;
        try {
            Map<String, Object> state = objectMapper.readValue(stateJson, Map.class);
            BattleState battle = new BattleState();
            battle.scenario = (String) state.getOrDefault("scenario", "");
            battle.progress = ((Number) state.getOrDefault("progress", 0)).intValue();
            battle.targetProgress = ((Number) state.getOrDefault("targetProgress", 10)).intValue();
            battle.active = (Boolean) state.getOrDefault("active", false);
            battle.completed = (Boolean) state.getOrDefault("completed", false);
            String resultStr = (String) state.get("result");
            battle.result = resultStr != null ? BattleResult.valueOf(resultStr) : null;
            battle.totalVictories = ((Number) state.getOrDefault("totalVictories", 0)).intValue();
            battle.totalDefeats = ((Number) state.getOrDefault("totalDefeats", 0)).intValue();
            battles.put(galaxyName, battle);
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> unitData = (List<Map<String, Object>>) state.get("units");
            if (unitData != null) {
                List<Unit> unitList = new ArrayList<>();
                for (Map<String, Object> ud : unitData) {
                    Unit u = new Unit((String) ud.get("type"), ((Number) ud.get("level")).intValue());
                    u.setHealth(((Number) ud.get("health")).intValue());
                    u.setMaxHealth(((Number) ud.get("maxHealth")).intValue());
                    u.setAlive((Boolean) ud.get("alive"));
                    unitList.add(u);
                }
                units.put(galaxyName, unitList);
            }
            log.info("Restored Tactical state for {}: {}W-{}L, {} units", galaxyName, battle.totalVictories, battle.totalDefeats,
                    units.getOrDefault(galaxyName, Collections.emptyList()).size());
        } catch (Exception e) {
            log.error("Failed to restore Tactical state for {}: {}", galaxyName, e.getMessage(), e);
        }
    }

    /**
     * Battle state for tactical gameplay.
     */
    public static class BattleState {
        String scenario;
        int progress;
        int targetProgress;
        boolean active;
        boolean completed;
        BattleResult result;
        int totalVictories;
        int totalDefeats;

        void startBattle(String scenario) {
            this.scenario = scenario;
            this.targetProgress = 5 + new Random().nextInt(10);
            this.progress = 0;
            this.active = true;
            this.completed = false;
        }

        void progress() {
            if (active && !completed) {
                progress += 1 + new Random().nextInt(2);
                if (progress >= targetProgress) {
                    progress = targetProgress;
                    completed = true;
                    result = new Random().nextDouble() < 0.7 ? BattleResult.VICTORY : BattleResult.DEFEAT;
                }
            }
        }

        void endBattle() {
            active = false;
        }

        String getScenario() { return scenario; }
        int getProgress() { return progress; }
        int getTargetProgress() { return targetProgress; }
        boolean isActive() { return active; }
        boolean isCompleted() { return completed; }
        BattleResult getResult() { return result; }
    }

    /**
     * Unit for tactical gameplay.
     */
    public static class Unit {
        private final String type;
        private int level;
        private int health;
        private int maxHealth;
        private boolean alive;

        public Unit(String type, int level) {
            this.type = type;
            this.level = level;
            this.maxHealth = 50 + (level * 20);
            this.health = maxHealth;
            this.alive = true;
        }

        void takeAction() {
            // Random damage taken
            int damage = new Random().nextInt(15);
            health -= damage;
            if (health < 0) health = 0;
        }

        void levelUp() {
            level++;
            maxHealth = 50 + (level * 20);
            health = maxHealth;
        }

        public String getType() { return type; }
        public int getLevel() { return level; }
        public int getHealth() { return health; }
        public void setHealth(int health) { this.health = health; }
        public int getMaxHealth() { return maxHealth; }
        public void setMaxHealth(int maxHealth) { this.maxHealth = maxHealth; }
        public boolean isAlive() { return alive && health > 0; }
        public void setAlive(boolean alive) { this.alive = alive; }
    }

    /**
     * Battle result enum.
     */
    public enum BattleResult {
        VICTORY, DEFEAT
    }
}
