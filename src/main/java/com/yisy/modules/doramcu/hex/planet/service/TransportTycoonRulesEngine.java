package com.yisy.modules.doramcu.hex.planet.service;

import com.yisy.modules.doramcu.shared.model.GameSystem;
import com.yisy.modules.doramcu.shared.model.RulesEntry;
import com.yisy.modules.doramcu.shared.service.RulesService;
import com.yisy.modules.doramcu.gamecore.RulesEngine;
import com.yisy.modules.doramcu.util.DiceUtil;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Rules engine for Transport Tycoon.
 * <p>
 * Implements the d20+modifier business operations system:
 * <ul>
 *   <li>Task: roll d20 + business skill vs DC for market operations.</li>
 *   <li>Combat: economic warfare, market share battles.</li>
 *   <li>Dice: delegates to {@link DiceUtil} for generic rolling.</li>
 * </ul>
 * </p>
 */
@Component
public class TransportTycoonRulesEngine implements RulesEngine {

    private final RulesService rulesService;

    public TransportTycoonRulesEngine(RulesService rulesService) {
        this.rulesService = rulesService;
    }

    @Override
    public GameSystem getGameSystem() {
        // Transport Tycoon uses UNIVERSAL game system for its rules
        return GameSystem.UNIVERSAL;
    }

    @Override
    public int rollDice(int sides, int count) {
        return DiceUtil.rollNdX(count, sides);
    }

    @Override
    public TaskResult taskAttempt(int difficulty, int skillLevel) {
        // Transport Tycoon: d20 + business skill vs DC
        int roll = DiceUtil.rollD20();
        int total = roll + skillLevel;
        boolean success = total >= difficulty;

        String description;
        if (roll == 20) {
            description = "Market boom! Natural 20. Roll " + roll + " + " + skillLevel + " = " + total + " vs DC " + difficulty + ".";
            return new TtTaskResult(true, roll, difficulty, description);
        } else if (roll == 1) {
            description = "Market crash! Natural 1. Roll " + roll + " + " + skillLevel + " = " + total + " vs DC " + difficulty + ".";
            return new TtTaskResult(false, roll, difficulty, description);
        } else if (success) {
            description = "Profit! Roll " + roll + " + " + skillLevel + " = " + total + " >= DC " + difficulty + ".";
        } else {
            description = "Loss. Roll " + roll + " + " + skillLevel + " = " + total + " < DC " + difficulty + ".";
        }
        return new TtTaskResult(success, roll, difficulty, description);
    }

    @Override
    public CombatResult combatRound(Map<String, Object> attacker, Map<String, Object> defender) {
        // Economic warfare: market share battle
        int attackPower = getInt(attacker, "marketShare", 5);
        int defensePower = getInt(defender, "marketShare", 5);
        int revenue = getInt(attacker, "revenue", 1);

        int roll = DiceUtil.rollD20();
        int attackTotal = roll + attackPower;
        int defenseTotal = 10 + defensePower;
        boolean hit = attackTotal > defenseTotal;

        int damage = 0;
        String description;
        if (hit) {
            int margin = attackTotal - defenseTotal;
            damage = revenue + margin;
            description = "Market share gained! Attack " + attackTotal + " (roll " + roll + " + " + attackPower + ") > defense " + defenseTotal + ". Revenue impact: " + damage + ".";
        } else {
            description = "Market share held. Attack " + attackTotal + " (roll " + roll + " + " + attackPower + ") <= defense " + defenseTotal + ".";
        }
        return new TtCombatResult(hit, damage, description);
    }

    @Override
    public List<RulesEntry> queryRules(String category) {
        return rulesService.getEntriesByCategory(GameSystem.UNIVERSAL, category);
    }

    @Override
    public List<RulesEntry> queryRules(String category, GameSystem gameSystem) {
        if (gameSystem == null || gameSystem == GameSystem.UNIVERSAL) {
            return rulesService.getEntriesByCategory(GameSystem.UNIVERSAL, category);
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

    private record TtTaskResult(boolean success, int roll, int target, String description) implements TaskResult {
        @Override public boolean isSuccess() { return success; }
        @Override public int getRoll() { return roll; }
        @Override public int getTarget() { return target; }
        @Override public String getDescription() { return description; }
    }

    private record TtCombatResult(boolean hit, int damage, String description) implements CombatResult {
        @Override public boolean isHit() { return hit; }
        @Override public int getDamage() { return damage; }
        @Override public String getDescription() { return description; }
    }
}
