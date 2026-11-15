package com.yisy.modules.doramcu.captainslog.service;

import com.yisy.modules.doramcu.shared.model.GameSystem;
import com.yisy.modules.doramcu.shared.model.RulesEntry;
import com.yisy.modules.doramcu.gamecore.RulesEngine;
import com.yisy.modules.doramcu.util.DiceUtil;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Rules engine for Captain's Log Solo RPG.
 * <p>
 * Implements the 2d20 count successes system with boons/threats:
 * <ul>
 *   <li>Task: roll 2d20, count dice ≥ target number. Boons add +1d20, threats remove a die.</li>
 *   <li>Combat: task-attempt combat with system attacks.</li>
 *   <li>Dice: delegates to {@link DiceUtil} for generic rolling.</li>
 * </ul>
 * </p>
 */
@Component
public class CaptainsLogRulesEngine implements RulesEngine {

    private final com.yisy.modules.doramcu.captainslog.service.RulesService rulesService;

    public CaptainsLogRulesEngine(com.yisy.modules.doramcu.captainslog.service.RulesService rulesService) {
        this.rulesService = rulesService;
    }

    @Override
    public GameSystem getGameSystem() {
        return GameSystem.CAPTAINS_LOG;
    }

    @Override
    public int rollDice(int sides, int count) {
        return DiceUtil.rollNdX(count, sides);
    }

    @Override
    public TaskResult taskAttempt(int difficulty, int skillLevel) {
        // Captain's Log: roll 2d20, count successes ≥ target
        // Difficulty sets target: 10=easy, 12=moderate, 14=hard, 16=very hard
        int target = Math.max(2, 12 - difficulty);
        int diceCount = Math.max(1, 2 + (skillLevel / 3)); // Skill adds bonus dice
        int successes = 0;
        int totalRoll = 0;
        for (int i = 0; i < diceCount; i++) {
            int die = DiceUtil.rollNdX(1, 20);
            totalRoll += die;
            if (die >= target) {
                successes++;
            }
        }
        boolean success = successes >= 1;

        String description;
        if (successes >= 2) {
            description = "Great success! " + successes + " successes on " + diceCount + "d20 (target " + target + "). Total roll: " + totalRoll + ".";
        } else if (success) {
            description = "Success! " + successes + " success on " + diceCount + "d20 (target " + target + "). Total roll: " + totalRoll + ".";
        } else {
            description = "Failure. 0 successes on " + diceCount + "d20 (target " + target + "). Total roll: " + totalRoll + ".";
        }
        return new ClTaskResult(success, totalRoll, target, description);
    }

    @Override
    public CombatResult combatRound(Map<String, Object> attacker, Map<String, Object> defender) {
        int attackSkill = getInt(attacker, "skill", 3);
        int defenseSkill = getInt(defender, "defense", 3);
        int damageBase = getInt(attacker, "damage", 1);

        // Attack roll
        int attackTarget = Math.max(2, 12 - attackSkill);
        int attackDice = Math.max(1, 2 + (attackSkill / 3));
        int attackSuccesses = 0;
        int attackRoll = 0;
        for (int i = 0; i < attackDice; i++) {
            int die = DiceUtil.rollNdX(1, 20);
            attackRoll += die;
            if (die >= attackTarget) {
                attackSuccesses++;
            }
        }

        // Defense roll
        int defenseTarget = Math.max(2, 12 - defenseSkill);
        int defenseDice = Math.max(1, 2 + (defenseSkill / 3));
        int defenseSuccesses = 0;
        int defenseRoll = 0;
        for (int i = 0; i < defenseDice; i++) {
            int die = DiceUtil.rollNdX(1, 20);
            defenseRoll += die;
            if (die >= defenseTarget) {
                defenseSuccesses++;
            }
        }

        boolean hit = attackSuccesses > defenseSuccesses;
        int damage = hit ? damageBase * (attackSuccesses - defenseSuccesses) : 0;

        String description;
        if (hit) {
            description = "Hit! Attack: " + attackSuccesses + " successes, Defense: " + defenseSuccesses + " successes. Damage: " + damage + ".";
        } else {
            description = "Miss. Attack: " + attackSuccesses + " successes, Defense: " + defenseSuccesses + " successes.";
        }
        return new ClCombatResult(hit, damage, description);
    }

    @Override
    public List<RulesEntry> queryRules(String category) {
        return rulesService.getRulesByCategory(category);
    }

    @Override
    public List<RulesEntry> queryRules(String category, GameSystem gameSystem) {
        if (gameSystem == null || gameSystem == GameSystem.CAPTAINS_LOG) {
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

    private record ClTaskResult(boolean success, int roll, int target, String description) implements TaskResult {
        @Override public boolean isSuccess() { return success; }
        @Override public int getRoll() { return roll; }
        @Override public int getTarget() { return target; }
        @Override public String getDescription() { return description; }
    }

    private record ClCombatResult(boolean hit, int damage, String description) implements CombatResult {
        @Override public boolean isHit() { return hit; }
        @Override public int getDamage() { return damage; }
        @Override public String getDescription() { return description; }
    }
}
