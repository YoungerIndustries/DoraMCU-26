package com.yisy.modules.doramcu.mechwarrior.service;

import com.yisy.modules.doramcu.mechwarrior.MechWarriorRulesService;
import com.yisy.modules.doramcu.shared.model.GameSystem;
import com.yisy.modules.doramcu.shared.model.RulesEntry;
import com.yisy.modules.doramcu.gamecore.RulesEngine;
import com.yisy.modules.doramcu.util.DiceUtil;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Rules engine for MechWarrior / BattleTech.
 * <p>
 * Implements the d6-based combat system with heat management:
 * <ul>
 *   <li>Task: roll d6 vs pilot skill 1-6. Roll under or equal to skill.</li>
 *   <li>Combat: component damage, location targeting, heat tracking.</li>
 *   <li>Dice: delegates to {@link DiceUtil} for generic rolling.</li>
 * </ul>
 * </p>
 */
@Component
public class MechWarriorRulesEngine implements RulesEngine {

    private final MechWarriorRulesService rulesService;

    public MechWarriorRulesEngine(MechWarriorRulesService rulesService) {
        this.rulesService = rulesService;
    }

    @Override
    public GameSystem getGameSystem() {
        return GameSystem.MECH_WARRIOR;
    }

    @Override
    public int rollDice(int sides, int count) {
        return DiceUtil.rollNdX(count, sides);
    }

    @Override
    public TaskResult taskAttempt(int difficulty, int skillLevel) {
        // MechWarrior: roll d6 ≤ pilot skill (1-6)
        // Difficulty reduces effective skill
        int effectiveSkill = Math.max(1, skillLevel - difficulty);
        int roll = DiceUtil.rollNdX(1, 6);
        boolean success = roll <= effectiveSkill;

        String description;
        if (roll == 1) {
            description = "Perfect roll! Roll 1 on d6. Effective skill: " + effectiveSkill + ".";
            return new MwTaskResult(true, roll, effectiveSkill, description);
        } else if (roll == 6) {
            description = "Worst roll! Roll 6 on d6. Effective skill: " + effectiveSkill + ".";
            return new MwTaskResult(success && effectiveSkill == 6, roll, effectiveSkill, description);
        } else if (success) {
            description = "Success! Roll " + roll + " <= effective skill " + effectiveSkill + ".";
        } else {
            description = "Failure. Roll " + roll + " > effective skill " + effectiveSkill + ".";
        }
        return new MwTaskResult(success, roll, effectiveSkill, description);
    }

    @Override
    public CombatResult combatRound(Map<String, Object> attacker, Map<String, Object> defender) {
        int toHit = getInt(attacker, "toHit", 4);
        int heat = getInt(attacker, "heat", 0);
        int maxHeat = getInt(attacker, "maxHeat", 20);
        int damageBase = getInt(attacker, "damage", 3);
        int armor = getInt(defender, "armor", 5);

        // MechWarrior: roll 2d6 + toHit vs 10 (base target number)
        int roll = DiceUtil.rollNdX(2, 6);
        int attackTotal = roll + toHit;
        boolean hit = attackTotal <= 10;

        int damage = 0;
        String description;
        if (hit) {
            // Heat overflow reduces accuracy
            int heatPenalty = Math.max(0, heat - maxHeat);
            damage = Math.max(1, damageBase - (armor / 3) - heatPenalty);
            if (heat > maxHeat) {
                description = "Hit with heat warning! Roll " + roll + " + toHit " + toHit + " = " + attackTotal + " <= 10. Damage: " + damage + " (heat " + heat + "/" + maxHeat + ").";
            } else {
                description = "Hit! Roll " + roll + " + toHit " + toHit + " = " + attackTotal + " <= 10. Damage: " + damage + " (armor " + armor + " absorbs some).";
            }
        } else {
            description = "Miss. Roll " + roll + " + toHit " + toHit + " = " + attackTotal + " > 10.";
        }
        return new MwCombatResult(hit, damage, description);
    }

    @Override
    public List<RulesEntry> queryRules(String category) {
        return rulesService.getRulesByCategory(category);
    }

    @Override
    public List<RulesEntry> queryRules(String category, GameSystem gameSystem) {
        if (gameSystem == null || gameSystem == GameSystem.MECH_WARRIOR) {
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

    private record MwTaskResult(boolean success, int roll, int target, String description) implements TaskResult {
        @Override public boolean isSuccess() { return success; }
        @Override public int getRoll() { return roll; }
        @Override public int getTarget() { return target; }
        @Override public String getDescription() { return description; }
    }

    private record MwCombatResult(boolean hit, int damage, String description) implements CombatResult {
        @Override public boolean isHit() { return hit; }
        @Override public int getDamage() { return damage; }
        @Override public String getDescription() { return description; }
    }
}
