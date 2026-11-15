package com.yisy.modules.doramcu.babylon5.service;

import com.yisy.modules.doramcu.shared.model.GameSystem;
import com.yisy.modules.doramcu.shared.model.RulesEntry;
import com.yisy.modules.doramcu.gamecore.RulesEngine;
import com.yisy.modules.doramcu.util.DiceUtil;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Rules engine for Babylon 5.
 * <p>
 * Implements the 2D6 bell curve task resolution system:
 * <ul>
 *   <li>Task: roll 2D6, target = skillLevel + difficulty modifier (lower roll is better).</li>
 *   <li>Combat: uses B5 ship combat stats — hit probability and damage severity.</li>
 *   <li>Dice: delegates to {@link DiceUtil} for generic rolling.</li>
 * </ul>
 * </p>
 */
@Component
public class B5RulesEngine implements RulesEngine {

    private final B5RulesDatabase rulesDatabase;

    public B5RulesEngine(B5RulesDatabase rulesDatabase) {
        this.rulesDatabase = rulesDatabase;
    }

    @Override
    public GameSystem getGameSystem() {
        return GameSystem.BABYLON_5;
    }

    @Override
    public int rollDice(int sides, int count) {
        return DiceUtil.rollNdX(count, sides);
    }

    @Override
    public TaskResult taskAttempt(int difficulty, int skillLevel) {
        int roll = DiceUtil.rollNdX(2, 6);
        // B5: roll 2D6 ≤ target. Target = skillLevel + difficulty modifier.
        // Difficulty scales: easy=12, moderate=9, hard=6, very hard=3
        int target = skillLevel + difficulty;
        boolean success = roll <= target;

        String description;
        if (roll == 2) {
            description = "Automatic success! Roll 2 on 2D6. Target: " + target + ".";
            return new B5TaskResult(true, roll, target, description);
        } else if (roll == 12) {
            description = "Automatic failure! Roll 12 on 2D6. Target: " + target + ".";
            return new B5TaskResult(false, roll, target, description);
        } else if (success) {
            description = "Success! Roll " + roll + " <= target " + target + ".";
        } else {
            description = "Failure. Roll " + roll + " > target " + target + ".";
        }
        return new B5TaskResult(success, roll, target, description);
    }

    @Override
    public CombatResult combatRound(Map<String, Object> attacker, Map<String, Object> defender) {
        int attackValue = getInt(attacker, "attack", 5);
        int defenseValue = getInt(defender, "defense", 5);
        int damageBase = getInt(attacker, "damage", 1);

        // B5 ship combat: roll 2D6 + attack vs defense + 6
        int roll = DiceUtil.rollNdX(2, 6);
        int attackTotal = roll + attackValue;
        int defenseTotal = defenseValue + 6;
        boolean hit = attackTotal > defenseTotal;

        int damage = 0;
        String description;
        if (hit) {
            // Damage severity based on margin of success
            int margin = attackTotal - defenseTotal;
            damage = damageBase + margin;
            description = "Hit! Attack " + attackTotal + " (roll " + roll + " + " + attackValue + ") > defense " + defenseTotal + " (" + defenseValue + " + 6). Damage: " + damage + ".";
        } else {
            description = "Miss. Attack " + attackTotal + " (roll " + roll + " + " + attackValue + ") <= defense " + defenseTotal + " (" + defenseValue + " + 6).";
        }
        return new B5CombatResult(hit, damage, description);
    }

    @Override
    public List<RulesEntry> queryRules(String category) {
        return rulesDatabase.getRulesByCategory(category);
    }

    @Override
    public List<RulesEntry> queryRules(String category, GameSystem gameSystem) {
        if (gameSystem == null || gameSystem == GameSystem.BABYLON_5) {
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
     * Babylon 5 task result implementation.
     */
    private record B5TaskResult(boolean success, int roll, int target, String description) implements TaskResult {
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
     * Babylon 5 combat result implementation.
     */
    private record B5CombatResult(boolean hit, int damage, String description) implements CombatResult {
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
