package com.yisy.modules.doramcu.lord_of_the_rings.service;

import com.yisy.modules.doramcu.shared.model.GameSystem;
import com.yisy.modules.doramcu.shared.model.RulesEntry;
import com.yisy.modules.doramcu.gamecore.RulesEngine;
import com.yisy.modules.doramcu.util.DiceUtil;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Rules engine for Lord of the Rings.
 * <p>
 * Implements a d6-based combat system inspired by War of the Ring and The One Ring RPG:
 * <ul>
 *   <li>Task: roll d6 + skillLevel vs DC set by difficulty. Difficulty scales from 3 (easy) to 6 (heroic).</li>
 *   <li>Combat: d6 dice pools — attack value vs defense value with hit/glance/crit resolution.</li>
 *   <li>Corruption: Ring corruption can interfere with rolls, adding Shadow effects.</li>
 * </ul>
 * </p>
 */
@Component
public class LordOfTheRingsRulesEngine implements RulesEngine {

    private final LordOfTheRingsRulesDatabase rulesDatabase;

    public LordOfTheRingsRulesEngine(LordOfTheRingsRulesDatabase rulesDatabase) {
        this.rulesDatabase = rulesDatabase;
    }

    @Override
    public GameSystem getGameSystem() {
        return GameSystem.LORD_OF_THE_RINGS;
    }

    @Override
    public int rollDice(int sides, int count) {
        return DiceUtil.rollNdX(count, sides);
    }

    @Override
    public TaskResult taskAttempt(int difficulty, int skillLevel) {
        // LOTR narrative dice approximation:
        // Difficulty sets DC: 3=easy, 4=average, 5=hard, 6=very hard
        int dc = difficulty;
        int roll = DiceUtil.rollD6() + DiceUtil.rollD6(); // 2d6 for narrative feel
        int total = roll + skillLevel;
        boolean success = total >= dc;

        String description;
        if (roll == 12) {
            description = "Triumph! Natural 12 (2d6). Roll " + roll + " + " + skillLevel + " = " + total + " vs DC " + dc + ".";
            return new LotrTaskResult(true, roll, dc, description);
        } else if (roll == 2) {
            description = "Disaster! Natural 2 (2d6). Roll " + roll + " + " + skillLevel + " = " + total + " vs DC " + dc + ".";
            return new LotrTaskResult(false, roll, dc, description);
        } else if (success) {
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
        return new LotrTaskResult(success, roll, dc, description);
    }

    @Override
    public CombatResult combatRound(Map<String, Object> attacker, Map<String, Object> defender) {
        // War of the Ring-style combat: d6 dice pools
        int attackValue = getInt(attacker, "attack", 3);
        int defenseValue = getInt(defender, "defense", 3);
        int damageBase = getInt(attacker, "damage", 1);

        // Roll attack dice
        int attackRolls = 0;
        int attackHits = 0;
        for (int i = 0; i < attackValue; i++) {
            int roll = DiceUtil.rollD6();
            attackRolls += roll;
            if (roll >= 5) attackHits++;
        }

        // Roll defense dice
        int defenseRolls = 0;
        int defenseBlocks = 0;
        for (int i = 0; i < defenseValue; i++) {
            int roll = DiceUtil.rollD6();
            defenseRolls += roll;
            if (roll >= 5) defenseBlocks++;
        }

        int damage = 0;
        String description;
        int unblockedHits = Math.max(0, attackHits - defenseBlocks);

        // Check for critical (all attack dice are 6)
        boolean allSixes = attackRolls == attackValue * 6;
        if (allSixes && unblockedHits > 0) {
            damage = damageBase * unblockedHits * 2;
            description = "Critical hit! All 6s on attack. " + attackHits + " hits vs " + defenseBlocks + " blocks. Damage: " + damage + " (critical).";
            return new LotrCombatResult(true, damage, description);
        } else if (unblockedHits > 0) {
            damage = damageBase * unblockedHits;
            description = "Hit! " + attackHits + " hits vs " + defenseBlocks + " blocks. " + unblockedHits + " unblocked. Damage: " + damage + ".";
        } else {
            description = "Miss. " + attackHits + " hits vs " + defenseBlocks + " blocks. All blocked.";
        }
        return new LotrCombatResult(unblockedHits > 0, damage, description);
    }

    @Override
    public List<RulesEntry> queryRules(String category) {
        return rulesDatabase.getRulesByCategory(category);
    }

    @Override
    public List<RulesEntry> queryRules(String category, GameSystem gameSystem) {
        if (gameSystem == null || gameSystem == GameSystem.LORD_OF_THE_RINGS) {
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
     * LOTR task result implementation.
     */
    private record LotrTaskResult(boolean success, int roll, int target, String description) implements TaskResult {
        @Override
        public boolean isSuccess() { return success; }
        @Override
        public int getRoll() { return roll; }
        @Override
        public int getTarget() { return target; }
        @Override
        public String getDescription() { return description; }
    }

    /**
     * LOTR combat result implementation.
     */
    private record LotrCombatResult(boolean hit, int damage, String description) implements CombatResult {
        @Override
        public boolean isHit() { return hit; }
        @Override
        public int getDamage() { return damage; }
        @Override
        public String getDescription() { return description; }
    }
}
