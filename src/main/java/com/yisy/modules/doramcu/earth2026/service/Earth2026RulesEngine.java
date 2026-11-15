package com.yisy.modules.doramcu.earth2026.service;

import com.yisy.modules.doramcu.shared.model.GameSystem;
import com.yisy.modules.doramcu.shared.model.RulesEntry;
import com.yisy.modules.doramcu.shared.service.RulesService;
import com.yisy.modules.doramcu.gamecore.RulesEngine;
import com.yisy.modules.doramcu.util.DiceUtil;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Rules engine for Earth 2026.
 * <p>
 * Implements the 2D6 Traveller-style bell curve system (Earth 2026 is Traveller-based):
 * <ul>
 *   <li>Task: roll 2D6 ≤ skill + modifier. Lower roll is better.</li>
 *   <li>Combat: Traveller firearms/melee — DM + skill vs target number.</li>
 *   <li>Dice: delegates to {@link DiceUtil} for generic rolling.</li>
 * </ul>
 * </p>
 */
@Component
public class Earth2026RulesEngine implements RulesEngine {

    private final RulesService rulesService;

    public Earth2026RulesEngine(RulesService rulesService) {
        this.rulesService = rulesService;
    }

    @Override
    public GameSystem getGameSystem() {
        return GameSystem.EARTH_2026;
    }

    @Override
    public int rollDice(int sides, int count) {
        return DiceUtil.rollNdX(count, sides);
    }

    @Override
    public TaskResult taskAttempt(int difficulty, int skillLevel) {
        int roll = DiceUtil.rollNdX(2, 6);
        int target = skillLevel + difficulty;
        boolean success = roll <= target;

        String description;
        if (roll == 2) {
            description = "Automatic success! Roll 2 on 2D6. Target: " + target + ".";
            return new E26TaskResult(true, roll, target, description);
        } else if (roll == 12) {
            description = "Automatic failure! Roll 12 on 2D6. Target: " + target + ".";
            return new E26TaskResult(false, roll, target, description);
        } else if (success) {
            description = "Success! Roll " + roll + " <= target " + target + ".";
        } else {
            description = "Failure. Roll " + roll + " > target " + target + ".";
        }
        return new E26TaskResult(success, roll, target, description);
    }

    @Override
    public CombatResult combatRound(Map<String, Object> attacker, Map<String, Object> defender) {
        int skill = getInt(attacker, "skill", 4);
        int dm = getInt(attacker, "dm", 0);
        int defense = getInt(defender, "defense", 4);
        int damageBase = getInt(attacker, "damage", 1);

        int roll = DiceUtil.rollNdX(2, 6);
        int attackTotal = roll + skill + dm;
        boolean hit = attackTotal <= 12;

        int damage = 0;
        String description;
        if (hit) {
            damage = Math.max(1, damageBase - (defense / 2));
            description = "Hit! Roll " + roll + " + skill " + skill + " + DM " + dm + " = " + attackTotal + " <= 12. Damage: " + damage + ".";
        } else {
            description = "Miss. Roll " + roll + " + skill " + skill + " + DM " + dm + " = " + attackTotal + " > 12.";
        }
        return new E26CombatResult(hit, damage, description);
    }

    @Override
    public List<RulesEntry> queryRules(String category) {
        return rulesService.getEntriesByCategory(GameSystem.EARTH_2026, category);
    }

    @Override
    public List<RulesEntry> queryRules(String category, GameSystem gameSystem) {
        if (gameSystem == null || gameSystem == GameSystem.EARTH_2026) {
            return rulesService.getEntriesByCategory(GameSystem.EARTH_2026, category);
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

    private record E26TaskResult(boolean success, int roll, int target, String description) implements TaskResult {
        @Override public boolean isSuccess() { return success; }
        @Override public int getRoll() { return roll; }
        @Override public int getTarget() { return target; }
        @Override public String getDescription() { return description; }
    }

    private record E26CombatResult(boolean hit, int damage, String description) implements CombatResult {
        @Override public boolean isHit() { return hit; }
        @Override public int getDamage() { return damage; }
        @Override public String getDescription() { return description; }
    }
}
