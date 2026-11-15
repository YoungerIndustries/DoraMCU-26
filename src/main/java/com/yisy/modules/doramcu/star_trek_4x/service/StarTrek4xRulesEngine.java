package com.yisy.modules.doramcu.star_trek_4x.service;

import com.yisy.modules.doramcu.shared.model.GameSystem;
import com.yisy.modules.doramcu.shared.model.RulesEntry;
import com.yisy.modules.doramcu.shared.service.RulesService;
import com.yisy.modules.doramcu.gamecore.RulesEngine;
import com.yisy.modules.doramcu.util.DiceUtil;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Rules engine for Star Trek 4X.
 * <p>
 * Implements the 2d10 percentile system with difficulty thresholds:
 * <ul>
 *   <li>Task: roll 2d10 vs difficulty threshold (Routine→Legendary).</li>
 *   <li>Combat: skill + talent vs difficulty threshold.</li>
 *   <li>Dice: delegates to {@link DiceUtil} for generic rolling.</li>
 * </ul>
 * </p>
 */
@Component
public class StarTrek4xRulesEngine implements RulesEngine {

    private final RulesService rulesService;

    public StarTrek4xRulesEngine(RulesService rulesService) {
        this.rulesService = rulesService;
    }

    @Override
    public GameSystem getGameSystem() {
        return GameSystem.STAR_TREK_4X;
    }

    @Override
    public int rollDice(int sides, int count) {
        return DiceUtil.rollNdX(count, sides);
    }

    @Override
    public TaskResult taskAttempt(int difficulty, int skillLevel) {
        // Star Trek 4x: roll 2d10 (percentile) vs difficulty threshold
        // Difficulty: 1=Routine(90%), 2=Easy(80%), 3=Average(70%), 4=Hard(60%), 5=Very Hard(50%), 6=Heroic(30%), 7=Legendary(10%)
        int threshold = Math.max(1, 100 - (difficulty * 10) + (skillLevel * 5));
        int roll = DiceUtil.rollNdX(2, 10);
        // Convert 2d10 to percentile: 1-100
        int percentile = ((roll / 10) * 10) + (roll % 10);
        if (percentile == 0) percentile = 100;
        boolean success = percentile <= threshold;

        String description;
        if (percentile <= threshold / 2) {
            description = "Outstanding success! Roll " + percentile + " <= threshold " + threshold + " (half).";
        } else if (success) {
            description = "Success! Roll " + percentile + " <= threshold " + threshold + ".";
        } else {
            description = "Failure. Roll " + percentile + " > threshold " + threshold + ".";
        }
        return new St4xTaskResult(success, percentile, threshold, description);
    }

    @Override
    public CombatResult combatRound(Map<String, Object> attacker, Map<String, Object> defender) {
        int attackSkill = getInt(attacker, "skill", 5);
        int defenseSkill = getInt(defender, "defense", 5);
        int damageBase = getInt(attacker, "damage", 2);

        // Attack threshold
        int attackThreshold = Math.max(1, 80 + attackSkill * 3);
        int attackRoll = DiceUtil.rollNdX(2, 10);
        int attackPercentile = ((attackRoll / 10) * 10) + (attackRoll % 10);
        if (attackPercentile == 0) attackPercentile = 100;

        // Defense threshold
        int defenseThreshold = Math.max(1, 80 + defenseSkill * 3);
        int defenseRoll = DiceUtil.rollNdX(2, 10);
        int defensePercentile = ((defenseRoll / 10) * 10) + (defenseRoll % 10);
        if (defensePercentile == 0) defensePercentile = 100;

        boolean hit = attackPercentile <= attackThreshold && defensePercentile > defenseThreshold;
        int damage = hit ? damageBase : 0;

        String description;
        if (hit) {
            description = "Hit! Attack " + attackPercentile + " <= " + attackThreshold + ", Defense " + defensePercentile + " > " + defenseThreshold + ". Damage: " + damage + ".";
        } else {
            description = "Miss. Attack " + attackPercentile + ", Defense " + defensePercentile + ".";
        }
        return new St4xCombatResult(hit, damage, description);
    }

    @Override
    public List<RulesEntry> queryRules(String category) {
        return rulesService.getEntriesByCategory(GameSystem.STAR_TREK_4X, category);
    }

    @Override
    public List<RulesEntry> queryRules(String category, GameSystem gameSystem) {
        if (gameSystem == null || gameSystem == GameSystem.STAR_TREK_4X) {
            return rulesService.getEntriesByCategory(GameSystem.STAR_TREK_4X, category);
        }
        return List.of();
    }

    private int getInt(Map<String, Object> map, String key, int defaultValue) {
        if (map == null || !map.containsKey(key)) {
            return defaultValue;
        }
        Object val = map.get(key);
        if (val instanceof Number) {
            return ((Number) val).intValue();
        }
        try {
            return Integer.parseInt(val.toString());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private record St4xTaskResult(boolean success, int roll, int target, String description) implements TaskResult {
        @Override public boolean isSuccess() { return success; }
        @Override public int getRoll() { return roll; }
        @Override public int getTarget() { return target; }
        @Override public String getDescription() { return description; }
    }

    private record St4xCombatResult(boolean hit, int damage, String description) implements CombatResult {
        @Override public boolean isHit() { return hit; }
        @Override public int getDamage() { return damage; }
        @Override public String getDescription() { return description; }
    }
}
