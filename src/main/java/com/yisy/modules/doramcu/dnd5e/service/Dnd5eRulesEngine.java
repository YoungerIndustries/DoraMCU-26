package com.yisy.modules.doramcu.dnd5e.service;

import com.yisy.modules.doramcu.shared.model.GameSystem;
import com.yisy.modules.doramcu.shared.model.RulesEntry;
import com.yisy.modules.doramcu.gamecore.RulesEngine;
import com.yisy.modules.doramcu.util.DiceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Rules engine for D&amp;D 5th Edition.
 * <p>
 * Implements the d20+modifier task resolution system:
 * <ul>
 *   <li>Task: roll d20 + skillLevel vs DC (difficulty). Natural 20 is critical success, natural 1 is fumble.</li>
 *   <li>Combat: roll d20 + attack bonus vs AC. Damage from attacker's damage dice.</li>
 *   <li>Dice: delegates to {@link DiceUtil} for generic rolling.</li>
 * </ul>
 * </p>
 */
@Component
public class Dnd5eRulesEngine implements RulesEngine {

    private static final Logger log = LoggerFactory.getLogger(Dnd5eRulesEngine.class);

    private final Dnd5eRulesService rulesService;

    public Dnd5eRulesEngine(Dnd5eRulesService rulesService) {
        this.rulesService = rulesService;
    }

    @Override
    public GameSystem getGameSystem() {
        return GameSystem.DND_5E;
    }

    @Override
    public int rollDice(int sides, int count) {
        return DiceUtil.rollNdX(count, sides);
    }

    @Override
    public TaskResult taskAttempt(int difficulty, int skillLevel) {
        int roll = DiceUtil.rollD20();
        int total = roll + skillLevel;
        boolean success = total >= difficulty;

        String description;
        if (roll == 20) {
            description = "Critical success! Natural 20. Roll " + roll + " + " + skillLevel + " = " + total + " vs DC " + difficulty + ".";
            return new Dnd5eTaskResult(true, roll, difficulty, description);
        } else if (roll == 1) {
            description = "Fumble! Natural 1. Roll " + roll + " + " + skillLevel + " = " + total + " vs DC " + difficulty + ".";
            return new Dnd5eTaskResult(false, roll, difficulty, description);
        } else if (success) {
            description = "Success! Roll " + roll + " + " + skillLevel + " = " + total + " >= DC " + difficulty + ".";
        } else {
            description = "Failure. Roll " + roll + " + " + skillLevel + " = " + total + " < DC " + difficulty + ".";
        }
        return new Dnd5eTaskResult(success, roll, difficulty, description);
    }

    @Override
    public CombatResult combatRound(Map<String, Object> attacker, Map<String, Object> defender) {
        int attackBonus = getInt(attacker, "attackBonus", 0);
        int ac = getInt(defender, "ac", 10);
        int damageDice = getInt(attacker, "damageDice", 6);
        int damageCount = getInt(attacker, "damageCount", 1);
        int damageModifier = getInt(attacker, "damageModifier", 0);

        int roll = DiceUtil.rollD20();
        int total = roll + attackBonus;
        boolean hit = total >= ac;

        String description;
        int damage = 0;
        if (roll == 20) {
            damage = DiceUtil.rollNdX(damageCount * 2, damageDice) + damageModifier;
            description = "Critical hit! Natural 20. Attack roll " + roll + " + " + attackBonus + " = " + total + " vs AC " + ac + ". Damage: " + damage + " (double dice).";
            return new Dnd5eCombatResult(true, damage, description);
        } else if (roll == 1) {
            description = "Attack fumble! Natural 1. Attack roll " + roll + " + " + attackBonus + " = " + total + " vs AC " + ac + ". Miss.";
            return new Dnd5eCombatResult(false, 0, description);
        } else if (hit) {
            damage = DiceUtil.rollNdX(damageCount, damageDice) + damageModifier;
            description = "Hit! Attack roll " + roll + " + " + attackBonus + " = " + total + " >= AC " + ac + ". Damage: " + damage + ".";
        } else {
            description = "Miss. Attack roll " + roll + " + " + attackBonus + " = " + total + " < AC " + ac + ".";
        }
        return new Dnd5eCombatResult(hit, damage, description);
    }

    @Override
    public List<RulesEntry> queryRules(String category) {
        return rulesService.getRulesByCategory(category);
    }

    @Override
    public List<RulesEntry> queryRules(String category, GameSystem gameSystem) {
        if (gameSystem == null || gameSystem == GameSystem.DND_5E) {
            return rulesService.getRulesByCategory(category);
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
     * D&amp;D 5e task result implementation.
     */
    private record Dnd5eTaskResult(boolean success, int roll, int target, String description) implements TaskResult {
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
     * D&amp;D 5e combat result implementation.
     */
    private record Dnd5eCombatResult(boolean hit, int damage, String description) implements CombatResult {
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
