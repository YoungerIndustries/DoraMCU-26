package com.yisy.modules.doramcu.traveller.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Dice rolling utility for Traveller's 2d6-based system.
 * All task checks in Traveller use 2d6 + modifiers against a target number.
 */
public class DiceEngine {

    private final Random random;

    public DiceEngine() {
        this.random = new Random();
    }

    public DiceEngine(long seed) {
        this.random = new Random(seed);
    }

    /**
     * Roll a single d6 die.
     * @return value between 1 and 6
     */
    public int rollD6() {
        return random.nextInt(6) + 1;
    }

    /**
     * Roll 2d6 — the core mechanic for all task checks.
     * @return sum of two d6 dice (2-12)
     */
    public int roll2D6() {
        return rollD6() + rollD6();
    }

    /**
     * Roll Nd6 dice.
     * @param n number of dice
     * @return sum of n d6 dice
     */
    public int rollNd6(int n) {
        int sum = 0;
        for (int i = 0; i < n; i++) {
            sum += rollD6();
        }
        return sum;
    }

    /**
     * Perform a task check: roll 2d6 + modifier and compare against target number.
     * @param modifier total modifier (characteristic + skill + situational)
     * @param targetNumber target number to meet or exceed (default 8 for Routine)
     * @return TaskResult with roll details
     */
    public TaskResult taskCheck(int modifier, int targetNumber) {
        int roll = roll2D6();
        int total = roll + modifier;
        boolean success = total >= targetNumber;

        // Calculate effect: how much the total exceeds or falls short of target
        int effect = total - targetNumber;

        return new TaskResult(roll, modifier, total, targetNumber, success, effect);
    }

    /**
     * Perform a task check at Routine difficulty (8+).
     */
    public TaskResult routineCheck(int modifier) {
        return taskCheck(modifier, Difficulty.ROUTINE.targetNumber());
    }

    /**
     * Get the effect level description for a given effect value.
     */
    public static String effectDescription(int effect) {
        if (effect >= 6) return "Exceptional Success";
        if (effect >= 1) return "Average Success";
        if (effect == 0) return "Marginal Success";
        if (effect >= -5) return "Average Failure";
        return "Exceptional Failure";
    }

    /**
     * Result of a task check.
     */
    public record TaskResult(
        int roll,
        int modifier,
        int total,
        int targetNumber,
        boolean success,
        int effect
    ) {
        public String description() {
            return String.format("Rolled %d + %d = %d (target %d) — %s",
                roll, modifier, total, targetNumber, effectDescription(effect));
        }
    }

    /**
     * Task difficulty levels from Traveller SRD.
     */
    public enum Difficulty {
        AUTOMATIC(2),
        SIMPLE(4),
        ROUTINE(8),
        HARD(10),
        DIFFICULT(12),
        FORMIDABLE(14),
        HEROIC(16),
        IMPOSSIBLE(18);

        private final int target;

        Difficulty(int target) {
            this.target = target;
        }

        public int targetNumber() {
            return target;
        }
    }
}
