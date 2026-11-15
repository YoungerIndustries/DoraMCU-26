package com.yisy.modules.doramcu.starwars.service;

import com.yisy.modules.doramcu.shared.model.GameSystem;
import com.yisy.modules.doramcu.shared.model.RulesEntry;
import com.yisy.modules.doramcu.gamecore.RulesEngine;
import com.yisy.modules.doramcu.util.DiceUtil;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Rules engine for Star Wars.
 * <p>
 * Implements a d20-based approximation of the narrative dice system (Force &amp; Destiny):
 * <ul>
 *   <li>Task: roll d20 + skillLevel vs DC set by difficulty. Difficulty scales from 8 (easy) to 20 (heroic).</li>
 *   <li>Combat: Legion-style attack/defense dice pools — attack value vs defense value with hit/ glance/crit resolution.</li>
 *   <li>Dice: delegates to {@link DiceUtil} for generic rolling.</li>
 * </ul>
 * </p>
 */
@Component
public class StarWarsRulesEngine implements RulesEngine {

    private final StarWarsRulesDatabase rulesDatabase;

    public StarWarsRulesEngine(StarWarsRulesDatabase rulesDatabase) {
        this.rulesDatabase = rulesDatabase;
    }

    @Override
    public GameSystem getGameSystem() {
        return GameSystem.STAR_WARS;
    }

    @Override
    public int rollDice(int sides, int count) {
        return DiceUtil.rollNdX(count, sides);
    }

    @Override
    public TaskResult taskAttempt(int difficulty, int skillLevel) {
        // Star Wars narrative dice approximation:
        // Difficulty sets DC: 8=easy, 10=average, 12=hard, 14=very hard, 16=heroic, 18=legendary
        int dc = difficulty;
        int roll = DiceUtil.rollD20();
        int total = roll + skillLevel;
        boolean success = total >= dc;

        String description;
        if (roll == 20) {
            description = "Triumph! Natural 20. Roll " + roll + " + " + skillLevel + " = " + total + " vs DC " + dc + ".";
            return new SwTaskResult(true, roll, dc, description);
        } else if (roll == 1) {
            description = "Despair! Natural 1. Roll " + roll + " + " + skillLevel + " = " + total + " vs DC " + dc + ".";
            return new SwTaskResult(false, roll, dc, description);
        } else if (success) {
            // Check for threat/strain on edge results
            if (total == dc) {
                description = "Success with strain. Roll " + roll + " + " + skillLevel + " = " + total + " = DC " + dc + ".";
            } else {
                description = "Success! Roll " + roll + " + " + skillLevel + " = " + total + " >= DC " + dc + ".";
            }
        } else {
            if (total == dc - 1) {
                description = "Failure with threat. Roll " + roll + " + " + skillLevel + " = " + total + " < DC " + dc + ".";
            } else {
                description = "Failure. Roll " + roll + " + " + skillLevel + " = " + total + " < DC " + dc + ".";
            }
        }
        return new SwTaskResult(success, roll, dc, description);
    }

    @Override
    public CombatResult combatRound(Map<String, Object> attacker, Map<String, Object> defender) {
        // Legion-style combat: attack dice pool vs defense dice pool
        int attackValue = getInt(attacker, "attack", 5);
        int defenseValue = getInt(defender, "defense", 5);
        int damageBase = getInt(attacker, "damage", 1);
        int critThreshold = getInt(attacker, "critThreshold", 18);

        int roll = DiceUtil.rollD20();
        int attackTotal = roll + attackValue;
        int defenseTotal = defenseValue + 10; // Base defense target
        boolean hit = attackTotal > defenseTotal;

        int damage = 0;
        String description;
        if (roll == 20) {
            damage = damageBase * 2;
            description = "Critical hit! Natural 20. Attack " + attackTotal + " (roll " + roll + " + " + attackValue + ") vs defense " + defenseTotal + ". Damage: " + damage + " (double).";
            return new SwCombatResult(true, damage, description);
        } else if (roll == 1) {
            description = "Miss! Natural 1. Attack " + attackTotal + " (roll " + roll + " + " + attackValue + ") vs defense " + defenseTotal + ".";
            return new SwCombatResult(false, 0, description);
        } else if (hit) {
            int margin = attackTotal - defenseTotal;
            damage = damageBase + (margin > 0 ? margin : 0);
            if (attackTotal >= critThreshold) {
                damage *= 2;
                description = "Glance/Crit! Attack " + attackTotal + " (roll " + roll + " + " + attackValue + ") vs defense " + defenseTotal + ". Damage: " + damage + " (critical).";
            } else {
                description = "Hit! Attack " + attackTotal + " (roll " + roll + " + " + attackValue + ") vs defense " + defenseTotal + ". Damage: " + damage + ".";
            }
        } else {
            description = "Miss. Attack " + attackTotal + " (roll " + roll + " + " + attackValue + ") <= defense " + defenseTotal + ".";
        }
        return new SwCombatResult(hit, damage, description);
    }

    @Override
    public List<RulesEntry> queryRules(String category) {
        return rulesDatabase.getRulesByCategory(category);
    }

    @Override
    public List<RulesEntry> queryRules(String category, GameSystem gameSystem) {
        if (gameSystem == null || gameSystem == GameSystem.STAR_WARS) {
            return rulesDatabase.getRulesByCategory(category);
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

    /**
     * Star Wars task result implementation.
     */
    private record SwTaskResult(boolean success, int roll, int target, String description) implements TaskResult {
        @Override
        public boolean isSuccess() {
            return success;
        }

        @Override
        public int getRoll() {
            return roll;
        }

        @Override
        public int getTarget() {
            return target;
        }

        @Override
        public String getDescription() {
            return description;
        }
    }

    /**
     * Star Wars combat result implementation.
     */
    private record SwCombatResult(boolean hit, int damage, String description) implements CombatResult {
        @Override
        public boolean isHit() {
            return hit;
        }

        @Override
        public int getDamage() {
            return damage;
        }

        @Override
        public String getDescription() {
            return description;
        }
    }
}
