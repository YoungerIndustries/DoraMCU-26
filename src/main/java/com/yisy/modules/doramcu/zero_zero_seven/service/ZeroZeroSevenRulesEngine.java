package com.yisy.modules.doramcu.zero_zero_seven.service;

import com.yisy.modules.doramcu.shared.model.GameSystem;
import com.yisy.modules.doramcu.shared.model.RulesEntry;
import com.yisy.modules.doramcu.shared.service.RulesService;
import com.yisy.modules.doramcu.gamecore.RulesEngine;
import com.yisy.modules.doramcu.util.DiceUtil;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Rules engine for 007 (Zero Zero Seven).
 * <p>
 * Implements the d20+modifier skill check system with espionage modifiers:
 * <ul>
 *   <li>Task: roll d20 + skill vs DC with espionage modifiers (gadget bonuses, stealth, etc.).</li>
 *   <li>Combat: action/reaction combat with gadget bonuses.</li>
 *   <li>Dice: delegates to {@link DiceUtil} for generic rolling.</li>
 * </ul>
 * </p>
 */
@Component
public class ZeroZeroSevenRulesEngine implements RulesEngine {

    private final RulesService rulesService;

    public ZeroZeroSevenRulesEngine(RulesService rulesService) {
        this.rulesService = rulesService;
    }

    @Override
    public GameSystem getGameSystem() {
        return GameSystem.ZERO_ZERO_SEVEN;
    }

    @Override
    public int rollDice(int sides, int count) {
        return DiceUtil.rollNdX(count, sides);
    }

    @Override
    public TaskResult taskAttempt(int difficulty, int skillLevel) {
        // 007: d20 + skill vs DC, with espionage modifiers
        int roll = DiceUtil.rollD20();
        int total = roll + skillLevel;
        boolean success = total >= difficulty;

        String description;
        if (roll == 20) {
            description = "Mission accomplished perfectly! Natural 20. Roll " + roll + " + " + skillLevel + " = " + total + " vs DC " + difficulty + ".";
            return new Z07TaskResult(true, roll, difficulty, description);
        } else if (roll == 1) {
            description = "Mission compromised! Natural 1. Roll " + roll + " + " + skillLevel + " = " + total + " vs DC " + difficulty + ".";
            return new Z07TaskResult(false, roll, difficulty, description);
        } else if (success) {
            description = "Success! Roll " + roll + " + " + skillLevel + " = " + total + " >= DC " + difficulty + ".";
        } else {
            description = "Failure. Roll " + roll + " + " + skillLevel + " = " + total + " < DC " + difficulty + ".";
        }
        return new Z07TaskResult(success, roll, difficulty, description);
    }

    @Override
    public CombatResult combatRound(Map<String, Object> attacker, Map<String, Object> defender) {
        int attackBonus = getInt(attacker, "attackBonus", 0);
        int ac = getInt(defender, "ac", 10);
        int damageBase = getInt(attacker, "damage", 1);
        int gadgetBonus = getInt(attacker, "gadgetBonus", 0);

        int roll = DiceUtil.rollD20();
        int total = roll + attackBonus + gadgetBonus;
        boolean hit = total >= ac;

        int damage = 0;
        String description;
        if (roll == 20) {
            damage = damageBase * 2;
            description = "Critical hit! Natural 20. Attack " + total + " (roll " + roll + " + " + attackBonus + " + gadget " + gadgetBonus + ") vs AC " + ac + ". Damage: " + damage + ".";
            return new Z07CombatResult(true, damage, description);
        } else if (roll == 1) {
            description = "Fumble! Natural 1. Attack " + total + " (roll " + roll + " + " + attackBonus + " + gadget " + gadgetBonus + ") vs AC " + ac + ".";
            return new Z07CombatResult(false, 0, description);
        } else if (hit) {
            damage = damageBase + gadgetBonus;
            description = "Hit! Attack " + total + " (roll " + roll + " + " + attackBonus + " + gadget " + gadgetBonus + ") >= AC " + ac + ". Damage: " + damage + ".";
        } else {
            description = "Miss. Attack " + total + " (roll " + roll + " + " + attackBonus + " + gadget " + gadgetBonus + ") < AC " + ac + ".";
        }
        return new Z07CombatResult(hit, damage, description);
    }

    @Override
    public List<RulesEntry> queryRules(String category) {
        return rulesService.getEntriesByCategory(GameSystem.ZERO_ZERO_SEVEN, category);
    }

    @Override
    public List<RulesEntry> queryRules(String category, GameSystem gameSystem) {
        if (gameSystem == null || gameSystem == GameSystem.ZERO_ZERO_SEVEN) {
            return rulesService.getEntriesByCategory(GameSystem.ZERO_ZERO_SEVEN, category);
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

    private record Z07TaskResult(boolean success, int roll, int target, String description) implements TaskResult {
        @Override public boolean isSuccess() { return success; }
        @Override public int getRoll() { return roll; }
        @Override public int getTarget() { return target; }
        @Override public String getDescription() { return description; }
    }

    private record Z07CombatResult(boolean hit, int damage, String description) implements CombatResult {
        @Override public boolean isHit() { return hit; }
        @Override public int getDamage() { return damage; }
        @Override public String getDescription() { return description; }
    }
}
