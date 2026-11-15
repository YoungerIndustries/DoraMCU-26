package com.yisy.modules.doramcu.thirdreich.service;

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
 * Rules engine for Third Reich (3R4) ground combat.
 * <p>
 * Implements the CRT-based d6 resolution system:
 * <ul>
 *   <li>Task: roll d6 + skillLevel vs difficulty, CRT differential determines outcome severity</li>
 *   <li>Combat: roll d6 + attackPower vs d6 + defensePower, differential determines decisive/marginal/standoff</li>
 *   <li>Dice: delegates to {@link DiceUtil} for generic rolling.</li>
 * </ul>
 * </p>
 */
@Component
public class ThirdReichRulesEngine implements RulesEngine {

    private static final Logger log = LoggerFactory.getLogger(ThirdReichRulesEngine.class);

    private final ThirdReichRulesDatabase rulesDatabase;

    public ThirdReichRulesEngine(ThirdReichRulesDatabase rulesDatabase) {
        this.rulesDatabase = rulesDatabase;
    }

    @Override
    public GameSystem getGameSystem() {
        return GameSystem.THIRD_REICH;
    }

    @Override
    public int rollDice(int sides, int count) {
        return DiceUtil.rollNdX(count, sides);
    }

    @Override
    public TaskResult taskAttempt(int difficulty, int skillLevel) {
        // Third Reich uses d6 + skill vs difficulty
        // The differential determines the degree of success
        int roll = DiceUtil.rollD6() + skillLevel;
        int differential = roll - difficulty;

        String description;
        boolean success;

        if (differential >= 3) {
            // Decisive success
            success = true;
            description = "Decisive success! Roll " + roll + " vs difficulty " + difficulty + ". Differential: +" + differential + ".";
        } else if (differential >= 1) {
            // Marginal success
            success = true;
            description = "Marginal success. Roll " + roll + " vs difficulty " + difficulty + ". Differential: +" + differential + ".";
        } else if (differential == 0) {
            // Standoff — partial success
            success = true;
            description = "Standoff. Roll " + roll + " vs difficulty " + difficulty + ". Differential: 0.";
        } else {
            // Failure
            success = false;
            description = "Failure. Roll " + roll + " vs difficulty " + difficulty + ". Differential: " + differential + ".";
        }
        return new TrTaskResult(success, roll, difficulty, description);
    }

    @Override
    public CombatResult combatRound(Map<String, Object> attacker, Map<String, Object> defender) {
        // Third Reich CRT-based combat: d6 + attackPower vs d6 + defensePower
        int attackPower = getInt(attacker, "attackPower", 3);
        int defensePower = getInt(defender, "defensePower", 3);
        int attackerLeadership = getInt(attacker, "leadershipModifier", 0);
        int defenderLeadership = getInt(defender, "leadershipModifier", 0);
        int armor = getInt(defender, "armor", 0);

        // Roll CRT with leadership modifiers
        int attackRoll = DiceUtil.rollD6() + attackPower + attackerLeadership;
        int defenseRoll = DiceUtil.rollD6() + defensePower + defenderLeadership;
        int differential = attackRoll - defenseRoll;

        String description;
        int damage = 0;
        boolean hit;

        if (differential >= 3) {
            // Decisive victory — double damage
            damage = Math.max(1, (attackPower * 2) - armor / 2);
            description = "Decisive victory! CRT differential " + differential + ". Damage: " + damage + " (double).";
            hit = true;
        } else if (differential >= 1) {
            // Marginal victory
            damage = Math.max(1, attackPower - armor / 2);
            description = "Marginal victory. CRT differential " + differential + ". Damage: " + damage + ".";
            hit = true;
        } else if (differential == 0) {
            // Standoff
            description = "Standoff. CRT differential 0. No damage.";
            hit = false;
        } else {
            // Defender holds
            description = "Defender holds. CRT differential " + differential + ". No damage.";
            hit = false;
        }
        return new TrCombatResult(hit, damage, description);
    }

    @Override
    public List<RulesEntry> queryRules(String category) {
        return rulesDatabase.getRulesByCategory(category);
    }

    @Override
    public List<RulesEntry> queryRules(String category, GameSystem gameSystem) {
        if (gameSystem == null || gameSystem == GameSystem.THIRD_REICH) {
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
     * Third Reich task result implementation.
     */
    private record TrTaskResult(boolean success, int roll, int target, String description) implements TaskResult {
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
     * Third Reich combat result implementation.
     */
    private record TrCombatResult(boolean hit, int damage, String description) implements CombatResult {
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
