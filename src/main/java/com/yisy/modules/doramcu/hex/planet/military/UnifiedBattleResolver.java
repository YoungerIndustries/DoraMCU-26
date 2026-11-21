package com.yisy.modules.doramcu.hex.planet.military;

import com.yisy.modules.doramcu.hex.planet.TerrainType;
import com.yisy.modules.doramcu.thirdreich.combat.ThirdReichCombatEngine;
import com.yisy.modules.doramcu.thirdreich.domain.ThirdReichBattle;
import com.yisy.modules.doramcu.thirdreich.domain.ThirdReichBattleResult;
import com.yisy.modules.doramcu.thirdreich.domain.ThirdReichGroundUnit;
import com.yisy.modules.doramcu.thirdreich.domain.TerrainContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Universal battle resolver that routes all combat types through the 3rd Reich engine.
 * <p>
 * Supports:
 * <ul>
 *   <li>Ground battles — resolved directly by 3rd Reich combat engine</li>
 *   <li>Naval battles — adapted to ground combat with naval modifiers</li>
 *   <li>Space battles — adapted to ground combat with space modifiers</li>
 * </ul>
 * </p>
 */
@Service
public class UnifiedBattleResolver {

    private static final Logger log = LoggerFactory.getLogger(UnifiedBattleResolver.class);

    private final ThirdReichCombatEngine combatEngine;
    private final Map<String, ThirdReichBattle> activeBattles;
    private final List<BattleRecord> battleHistory;
    private final AtomicInteger battleIdCounter;

    public UnifiedBattleResolver() {
        this.combatEngine = new ThirdReichCombatEngine();
        this.activeBattles = new ConcurrentHashMap<>();
        this.battleHistory = new ArrayList<>();
        this.battleIdCounter = new AtomicInteger(1);
    }

    /**
     * Battle type classification.
     */
    public enum BattleType {
        GROUND,
        NAVAL,
        SPACE
    }

    /**
     * Request to start a battle.
     */
    public static class BattleRequest {
        final String attackerFaction;
        final String defenderFaction;
        final List<ThirdReichGroundUnit> attackerUnits;
        final List<ThirdReichGroundUnit> defenderUnits;
        final BattleType battleType;
        final int hexX, hexY, hexZ;
        final TerrainType terrain;

        public BattleRequest(String attackerFaction, String defenderFaction,
                      List<ThirdReichGroundUnit> attackerUnits,
                      List<ThirdReichGroundUnit> defenderUnits,
                      BattleType battleType, int hexX, int hexY, int hexZ,
                      TerrainType terrain) {
            this.attackerFaction = attackerFaction;
            this.defenderFaction = defenderFaction;
            this.attackerUnits = attackerUnits;
            this.defenderUnits = defenderUnits;
            this.battleType = battleType;
            this.hexX = hexX;
            this.hexY = hexY;
            this.hexZ = hexZ;
            this.terrain = terrain;
        }
    }

    /**
     * Record of a completed battle.
     */
    public static class BattleRecord {
        private final String battleId;
        private final BattleType battleType;
        private final String attackerFaction;
        private final String defenderFaction;
        private final ThirdReichBattleResult.Outcome outcome;
        private final int attackerCasualties;
        private final int defenderCasualties;
        private final int rounds;
        private final long timestamp;

        BattleRecord(String battleId, BattleType battleType,
                     String attackerFaction, String defenderFaction,
                     ThirdReichBattleResult.Outcome outcome,
                     int attackerCasualties, int defenderCasualties,
                     int rounds, long timestamp) {
            this.battleId = battleId;
            this.battleType = battleType;
            this.attackerFaction = attackerFaction;
            this.defenderFaction = defenderFaction;
            this.outcome = outcome;
            this.attackerCasualties = attackerCasualties;
            this.defenderCasualties = defenderCasualties;
            this.rounds = rounds;
            this.timestamp = timestamp;
        }

        public String getBattleId() { return battleId; }
        public BattleType getBattleType() { return battleType; }
        public String getAttackerFaction() { return attackerFaction; }
        public String getDefenderFaction() { return defenderFaction; }
        public ThirdReichBattleResult.Outcome getOutcome() { return outcome; }
        public int getAttackerCasualties() { return attackerCasualties; }
        public int getDefenderCasualties() { return defenderCasualties; }
        public int getRounds() { return rounds; }
        public long getTimestamp() { return timestamp; }
    }

    /**
     * Start and resolve a battle.
     *
     * @param request the battle request
     * @return the battle result
     */
    public ThirdReichBattleResult resolveBattle(BattleRequest request) {
        String battleId = "BATTLE-" + battleIdCounter.getAndIncrement();
        log.info("Resolving {} battle: {} vs {} at ({},{},{})",
                request.battleType, request.attackerFaction, request.defenderFaction,
                request.hexX, request.hexY, request.hexZ);

        // Create the battle
        ThirdReichBattle battle = new ThirdReichBattle(battleId);

        // Add attacker units
        for (ThirdReichGroundUnit unit : request.attackerUnits) {
            battle.addAttackerUnit(unit);
        }

        // Add defender units
        for (ThirdReichGroundUnit unit : request.defenderUnits) {
            battle.addDefenderUnit(unit);
        }

        // Set terrain context
        TerrainContext terrainContext = createTerrainContext(request.terrain, request.battleType);
        battle.setTerrainContext(terrainContext);

        // Apply battle type modifiers
        applyBattleTypeModifiers(battle, request.battleType);

        // Resolve the battle
        ThirdReichBattleResult result = combatEngine.resolveBattle(battle, null, null);

        // Record the battle
        BattleRecord record = new BattleRecord(
                battleId, request.battleType,
                request.attackerFaction, request.defenderFaction,
                result.getOutcome(),
                result.getAttackerCasualties(), result.getDefenderCasualties(),
                result.getRoundsFought(), System.currentTimeMillis()
        );
        battleHistory.add(record);

        // Remove from active battles if present
        activeBattles.remove(battleId);

        log.info("Battle {} result: {} (attacker casualties: {}, defender casualties: {})",
                battleId, result.getOutcome(), result.getAttackerCasualties(), result.getDefenderCasualties());

        return result;
    }

    /**
     * Get an active battle by ID.
     */
    public Optional<ThirdReichBattle> getActiveBattle(String battleId) {
        return Optional.ofNullable(activeBattles.get(battleId));
    }

    /**
     * Get battle history.
     */
    public List<BattleRecord> getBattleHistory() {
        return new ArrayList<>(battleHistory);
    }

    /**
     * Get recent battles (last N).
     */
    public List<BattleRecord> getRecentBattles(int count) {
        int size = battleHistory.size();
        int from = Math.max(0, size - count);
        return new ArrayList<>(battleHistory.subList(from, size));
    }

    /**
     * Create a terrain context from planet terrain and battle type.
     */
    private TerrainContext createTerrainContext(TerrainType terrain, BattleType battleType) {
        com.yisy.modules.doramcu.thirdreich.domain.TerrainType trTerrain;

        // Map planet terrain to 3rd Reich terrain
        if (battleType == BattleType.NAVAL) {
            trTerrain = com.yisy.modules.doramcu.thirdreich.domain.TerrainType.SWAMP;
        } else if (battleType == BattleType.SPACE) {
            trTerrain = com.yisy.modules.doramcu.thirdreich.domain.TerrainType.PLAINS;
        } else {
            // Ground battle — map planet terrain
            switch (terrain) {
                case MOUNTAIN:
                case HIGHLAND:
                    trTerrain = com.yisy.modules.doramcu.thirdreich.domain.TerrainType.MOUNTAIN;
                    break;
                case FOREST:
                    trTerrain = com.yisy.modules.doramcu.thirdreich.domain.TerrainType.FOREST;
                    break;
                case DESERT:
                    trTerrain = com.yisy.modules.doramcu.thirdreich.domain.TerrainType.DESERT;
                    break;
                case SWAMP:
                    trTerrain = com.yisy.modules.doramcu.thirdreich.domain.TerrainType.SWAMP;
                    break;
                default:
                    trTerrain = com.yisy.modules.doramcu.thirdreich.domain.TerrainType.PLAINS;
                    break;
            }
        }

        return new TerrainContext(trTerrain);
    }

    /**
     * Apply battle type modifiers to units.
     */
    private void applyBattleTypeModifiers(ThirdReichBattle battle, BattleType battleType) {
        double attackModifier = 1.0;
        double defenseModifier = 1.0;

        switch (battleType) {
            case NAVAL:
                // Naval: reduced mobility, increased defense
                attackModifier = 0.9;
                defenseModifier = 1.2;
                break;
            case SPACE:
                // Space: increased attack, reduced defense
                attackModifier = 1.2;
                defenseModifier = 0.8;
                break;
            case GROUND:
            default:
                break;
        }

        // Apply modifiers to attacker units
        for (ThirdReichGroundUnit unit : battle.getAttackerUnits()) {
            unit.setAttackValue((int) (unit.getAttackValue() * attackModifier));
            unit.setDefenseValue((int) (unit.getDefenseValue() * defenseModifier));
        }

        // Apply modifiers to defender units
        for (ThirdReichGroundUnit unit : battle.getDefenderUnits()) {
            unit.setAttackValue((int) (unit.getAttackValue() * attackModifier));
            unit.setDefenseValue((int) (unit.getDefenseValue() * defenseModifier));
        }
    }
}
