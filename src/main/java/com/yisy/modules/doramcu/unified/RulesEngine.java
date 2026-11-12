package com.yisy.modules.doramcu.unified;

import com.yisy.modules.doramcu.shared.model.GameSystem;
import com.yisy.modules.doramcu.shared.model.RulesEntry;

import java.util.List;
import java.util.Map;

/**
 * Unified rules engine interface that normalizes rule lookup, dice mechanics,
 * and task resolution across all game systems.
 * <p>
 * Each game system provides a concrete implementation that knows its dice mechanics:
 * <ul>
 *   <li>Traveller: 2D6 (bell curve 2-12)</li>
 *   <li>Captain's Log: 2d20 (sum of two d20)</li>
 *   <li>D&D 5e: d20 + modifier</li>
 *   <li>Earth2026: 2D6 (bell curve 2-12)</li>
 * </ul>
 * </p>
 */
public interface RulesEngine {

    /**
     * Returns the game system this engine handles.
     *
     * @return the game system
     */
    GameSystem getGameSystem();

    /**
     * Rolls dice with the specified number of sides and count.
     *
     * @param sides the number of sides per die
     * @param count the number of dice to roll
     * @return the total roll result
     */
    int rollDice(int sides, int count);

    /**
     * Rolls 2D6, the most common dice mechanic in Traveller and Earth2026.
     *
     * @return the roll result (2-12)
     */
    default int roll2D6() {
        return rollDice(6, 2);
    }

    /**
     * Resolves a task attempt with the given difficulty and skill level.
     * <p>
     * The implementation varies by system:
     * <ul>
     *   <li>Traveller: roll 2D6 ≤ ability score + skill modifier</li>
     *   <li>Captain's Log: roll 2d20 ≥ target number based on difficulty</li>
     *   <li>D&D 5e: roll d20 + modifier ≥ DC</li>
     * </ul>
     * </p>
     *
     * @param difficulty the difficulty level (system-specific)
     * @param skillLevel the character's skill level
     * @return the task result
     */
    TaskResult taskAttempt(int difficulty, int skillLevel);

    /**
     * Resolves a combat round between attacker and defender.
     *
     * @param attacker the attacker's stats
     * @param defender the defender's stats
     * @return the combat result
     */
    CombatResult combatRound(Map<String, Object> attacker, Map<String, Object> defender);

    /**
     * Queries rules by category for this game system.
     *
     * @param category the rule category (e.g., "COMBAT", "EXPLORATION")
     * @return the list of matching rules
     */
    List<RulesEntry> queryRules(String category);

    /**
     * Queries rules by category and game system.
     *
     * @param category   the rule category
     * @param gameSystem the game system
     * @return the list of matching rules
     */
    List<RulesEntry> queryRules(String category, GameSystem gameSystem);

    /**
     * Result of a task attempt.
     */
    interface TaskResult {
        boolean isSuccess();
        int getRoll();
        int getTarget();
        String getDescription();
    }

    /**
     * Result of a combat round.
     */
    interface CombatResult {
        boolean isHit();
        int getDamage();
        String getDescription();
    }
}
