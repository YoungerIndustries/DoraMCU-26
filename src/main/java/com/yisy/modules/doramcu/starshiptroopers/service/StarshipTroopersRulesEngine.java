package com.yisy.modules.doramcu.starshiptroopers.service;

import com.yisy.modules.doramcu.shared.model.GameSystem;
import com.yisy.modules.doramcu.shared.model.RulesEntry;
import com.yisy.modules.doramcu.starshiptroopers.model.Unit;
import com.yisy.modules.doramcu.gamecore.RulesEngine;
import com.yisy.modules.doramcu.util.DiceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Rules engine for Starship Troopers.
 * <p>
 * Implements the d6-based CRT (Combat Results Table) resolution system:
 * <ul>
 *   <li>Task: roll d6 pools based on skill level, count successes (5-6)</li>
 *   <li>Combat: CRT-based resolution using attacker/defender stats vs lookup table</li>
 *   <li>Dice: delegates to {@link DiceUtil} for generic rolling.</li>
 * </ul>
 * </p>
 */
@Component
public class StarshipTroopersRulesEngine implements RulesEngine {

    private static final Logger log = LoggerFactory.getLogger(StarshipTroopersRulesEngine.class);

    private final StarshipTroopersService starshipTroopersService;

    public StarshipTroopersRulesEngine(StarshipTroopersService starshipTroopersService) {
        this.starshipTroopersService = starshipTroopersService;
    }

    @Override
    public GameSystem getGameSystem() {
        return GameSystem.STARSHIP_TROOPERS;
    }

    @Override
    public int rollDice(int sides, int count) {
        return DiceUtil.rollNdX(count, sides);
    }

    @Override
    public TaskResult taskAttempt(int difficulty, int skillLevel) {
        // Starship Troopers uses d6 pools: roll skillLevel d6s, count 5-6 as successes
        // difficulty = number of successes needed
        int successes = 0;
        List<Integer> rolls = new ArrayList<>();
        for (int i = 0; i < Math.max(1, skillLevel); i++) {
            int roll = DiceUtil.rollD6();
            rolls.add(roll);
            if (roll >= 5) {
                successes++;
            }
        }
        boolean success = successes >= Math.max(1, difficulty);

        String description;
        if (successes == 0) {
            description = "Failure. Rolled " + rolls + " with 0 successes, needed " + difficulty + ".";
        } else if (successes >= difficulty * 2) {
            description = "Critical success! Rolled " + rolls + " with " + successes + " successes, needed " + difficulty + ".";
        } else if (success) {
            description = "Success! Rolled " + rolls + " with " + successes + " successes, needed " + difficulty + ".";
        } else {
            description = "Failure. Rolled " + rolls + " with " + successes + " successes, needed " + difficulty + ".";
        }
        return new StTaskResult(success, successes, difficulty, description);
    }

    @Override
    public CombatResult combatRound(Map<String, Object> attacker, Map<String, Object> defender) {
        // Starship Troopers CRT-based combat
        int attackPower = getInt(attacker, "attackPower", 3);
        int defensePower = getInt(defender, "defensePower", 3);
        int attackerMorale = getInt(attacker, "morale", 8);
        int defenderMorale = getInt(defender, "morale", 8);

        // Roll CRT: d6 + attackPower vs d6 + defensePower
        int attackRoll = DiceUtil.rollD6() + attackPower;
        int defenseRoll = DiceUtil.rollD6() + defensePower;
        int differential = attackRoll - defenseRoll;

        String description;
        int damage = 0;
        boolean hit;

        if (differential >= 3) {
            // Decisive victory - double damage
            damage = Math.max(1, attackPower * 2);
            // Check morale break
            if (DiceUtil.rollD6() >= defenderMorale) {
                description = "Decisive victory! CRT differential " + differential + ". Defender breaks! Damage: " + damage + " (double).";
            } else {
                description = "Decisive victory! CRT differential " + differential + ". Damage: " + damage + " (double).";
            }
            hit = true;
        } else if (differential >= 1) {
            // Marginal victory
            damage = Math.max(1, attackPower);
            description = "Marginal victory. CRT differential " + differential + ". Damage: " + damage + ".";
            hit = true;
        } else if (differential == 0) {
            // Standoff
            description = "Standoff. CRT differential 0. No damage.";
            hit = false;
        } else {
            // Defender wins
            description = "Defender holds. CRT differential " + differential + ". No damage.";
            hit = false;
        }
        return new StCombatResult(hit, damage, description);
    }

    @Override
    public List<RulesEntry> queryRules(String category) {
        // Build rules entries from Starship Troopers service data
        List<RulesEntry> results = new ArrayList<>();

        // Combat outcomes
        if ("Combat".equalsIgnoreCase(category) || "COMBAT".equals(category)) {
            RulesEntry entry = new RulesEntry();
            entry.setGameSystem(GameSystem.STARSHIP_TROOPERS);
            entry.setTitle("Combat Outcomes");
            entry.setCategory("Combat");
            entry.setText(String.valueOf(starshipTroopersService.getCombatOutcomes()));
            entry.setSource("Starship Troopers");
            entry.setActive(true);
            results.add(entry);
        }

        // Turn phases
        if ("Turn Sequence".equalsIgnoreCase(category) || "TURN".equalsIgnoreCase(category)) {
            RulesEntry entry = new RulesEntry();
            entry.setGameSystem(GameSystem.STARSHIP_TROOPERS);
            entry.setTitle("Turn Phases");
            entry.setCategory("Turn Sequence");
            entry.setText(String.valueOf(starshipTroopersService.getTerranTurnPhases()));
            entry.setSource("Starship Troopers");
            entry.setActive(true);
            results.add(entry);
        }

        // Units
        if ("Unit".equalsIgnoreCase(category) || "UNIT".equals(category)) {
            for (Unit unit : starshipTroopersService.getTerranUnits()) {
                RulesEntry entry = new RulesEntry();
                entry.setGameSystem(GameSystem.STARSHIP_TROOPERS);
                entry.setTitle(unit.name());
                entry.setCategory("Terran Unit");
                entry.setText(unit.description());
                entry.setSource("Starship Troopers");
                entry.setActive(true);
                results.add(entry);
            }
            for (Unit unit : starshipTroopersService.getArachnidUnits()) {
                RulesEntry entry = new RulesEntry();
                entry.setGameSystem(GameSystem.STARSHIP_TROOPERS);
                entry.setTitle(unit.name());
                entry.setCategory("Arachnid Unit");
                entry.setText(unit.description());
                entry.setSource("Starship Troopers");
                entry.setActive(true);
                results.add(entry);
            }
        }

        // Scenarios
        if ("Scenario".equalsIgnoreCase(category) || "SCENARIO".equals(category)) {
            for (var scenario : starshipTroopersService.getScenarios()) {
                RulesEntry entry = new RulesEntry();
                entry.setGameSystem(GameSystem.STARSHIP_TROOPERS);
                entry.setTitle(scenario.name());
                entry.setCategory("Scenario");
                entry.setText(scenario.description());
                entry.setSource("Starship Troopers");
                entry.setActive(true);
                results.add(entry);
            }
        }

        return results;
    }

    @Override
    public List<RulesEntry> queryRules(String category, GameSystem gameSystem) {
        if (gameSystem == null || gameSystem == GameSystem.STARSHIP_TROOPERS) {
            return queryRules(category);
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
     * Starship Troopers task result implementation.
     */
    private record StTaskResult(boolean success, int roll, int target, String description) implements TaskResult {
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
     * Starship Troopers combat result implementation.
     */
    private record StCombatResult(boolean hit, int damage, String description) implements CombatResult {
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
