package com.yisy.modules.doramcu.sfb.service;

import com.yisy.modules.doramcu.shared.model.GameSystem;
import com.yisy.modules.doramcu.shared.model.RulesEntry;
import com.yisy.modules.doramcu.sfb.model.SfbRulesDatabase;
import com.yisy.modules.doramcu.gamecore.RulesEngine;
import com.yisy.modules.doramcu.util.DiceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Rules engine for SFB (Star Frontiers Battle).
 * <p>
 * Implements the 2D6 roll-under task resolution system:
 * <ul>
 *   <li>Task: roll 2D6 + modifier ≤ target number (ability score or skill level). Lower is better.</li>
 *   <li>Combat: roll 2D6 + attack bonus vs defense. Ship combat uses energy allocation and impulse turns.</li>
 *   <li>Dice: delegates to {@link DiceUtil} for generic rolling.</li>
 * </ul>
 * </p>
 */
@Component
public class SfbRulesEngine implements RulesEngine {

    private static final Logger log = LoggerFactory.getLogger(SfbRulesEngine.class);

    private final SfbRulesDatabase rulesDatabase;

    public SfbRulesEngine(SfbRulesDatabase rulesDatabase) {
        this.rulesDatabase = rulesDatabase;
    }

    @Override
    public GameSystem getGameSystem() {
        return GameSystem.SFB;
    }

    @Override
    public int rollDice(int sides, int count) {
        return DiceUtil.rollNdX(count, sides);
    }

    @Override
    public TaskResult taskAttempt(int difficulty, int skillLevel) {
        // SFB uses 2D6 roll-under: roll 2D6 + modifier ≤ target number
        // difficulty = target number (higher is harder to meet)
        // skillLevel = modifier (positive helps, negative hurts)
        int roll = DiceUtil.roll2D6();
        int total = roll + skillLevel;
        boolean success = total <= difficulty;

        String description;
        if (roll == 2) {
            description = "Critical success! Roll 2. Total " + total + " ≤ target " + difficulty + ".";
            return new SfbTaskResult(true, roll, difficulty, description);
        } else if (roll == 12) {
            description = "Fumble! Roll 12. Total " + total + " > target " + difficulty + ".";
            return new SfbTaskResult(false, roll, difficulty, description);
        } else if (success) {
            description = "Success! Roll " + roll + " + " + skillLevel + " = " + total + " ≤ target " + difficulty + ".";
        } else {
            description = "Failure. Roll " + roll + " + " + skillLevel + " = " + total + " > target " + difficulty + ".";
        }
        return new SfbTaskResult(success, roll, difficulty, description);
    }

    @Override
    public CombatResult combatRound(Map<String, Object> attacker, Map<String, Object> defender) {
        // SFB combat: 2D6 + attack bonus vs defense
        int attackBonus = getInt(attacker, "attackBonus", 0);
        int defense = getInt(defender, "defense", 0);
        int weapons = getInt(attacker, "weapons", 10);
        int armor = getInt(defender, "armor", 0);
        int hull = getInt(defender, "hull", 100);

        int attackRoll = DiceUtil.roll2D6() + attackBonus;
        int defenseRoll = DiceUtil.roll2D6() + defense;
        boolean hit = attackRoll >= defenseRoll;

        String description;
        int damage = 0;
        if (attackRoll - defenseRoll >= 4) {
            // Critical hit - double damage
            damage = Math.max(1, (weapons / 2) * 2 - armor);
            description = "Critical hit! Attack " + attackRoll + " vs defense " + defenseRoll + ". Damage: " + damage + " (double).";
            return new SfbCombatResult(true, damage, description);
        } else if (hit) {
            damage = Math.max(1, weapons / 2 - armor / 2);
            description = "Hit! Attack " + attackRoll + " >= defense " + defenseRoll + ". Damage: " + damage + ".";
        } else {
            description = "Miss. Attack " + attackRoll + " < defense " + defenseRoll + ".";
        }
        return new SfbCombatResult(hit, damage, description);
    }

    @Override
    public List<RulesEntry> queryRules(String category) {
        return rulesDatabase.getEntriesByCategory(category);
    }

    @Override
    public List<RulesEntry> queryRules(String category, GameSystem gameSystem) {
        if (gameSystem == null || gameSystem == GameSystem.SFB) {
            return rulesDatabase.getEntriesByCategory(category);
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
     * SFB task result implementation.
     */
    private record SfbTaskResult(boolean success, int roll, int target, String description) implements TaskResult {
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
     * SFB combat result implementation.
     */
    private record SfbCombatResult(boolean hit, int damage, String description) implements CombatResult {
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
