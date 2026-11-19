package com.yisy.modules.doramcu.lord_of_the_rings.service;

import com.yisy.modules.doramcu.lord_of_the_rings.dto.DiceRollResponse;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Service for rolling dice in Lord of the Rings RPG.
 * Supports formulas like "2d6+2", "4d6dl" (drop lowest), etc.
 */
@Service
public class LotrDiceService {

    private static final Pattern DICE_PATTERN = Pattern.compile("(\\d*)d(\\d+)([+-]\\d+)?(dl|dh)?", Pattern.CASE_INSENSITIVE);
    private final Random random = new Random();

    /**
     * Rolls dice using a formula string.
     *
     * @param formula dice formula, e.g., "2d6+2", "4d6dl", "d20"
     * @return the result of the dice roll
     */
    public DiceRollResponse rollDice(String formula) {
        if (formula == null || formula.trim().isEmpty()) {
            return new DiceRollResponse("d6", 1, 0, 1, false, "Invalid formula");
        }

        String trimmed = formula.trim().toLowerCase();
        Matcher matcher = DICE_PATTERN.matcher(trimmed);

        if (!matcher.matches()) {
            return new DiceRollResponse(formula, 0, 0, 0, false, "Invalid dice formula: " + formula);
        }

        int count = matcher.group(1).isEmpty() ? 1 : Integer.parseInt(matcher.group(1));
        int sides = Integer.parseInt(matcher.group(2));
        int modifier = matcher.group(3) != null ? Integer.parseInt(matcher.group(3)) : 0;
        String modifierStr = matcher.group(4);

        List<Integer> rolls = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            rolls.add(random.nextInt(sides) + 1);
        }

        // Handle drop lowest / drop highest
        if ("dl".equals(modifierStr) && count > 1) {
            rolls.sort(Integer::compareTo);
            rolls.remove(0); // drop lowest
        } else if ("dh".equals(modifierStr) && count > 1) {
            rolls.sort(Integer::compareTo);
            rolls.remove(rolls.size() - 1); // drop highest
        }

        int total = rolls.stream().mapToInt(Integer::intValue).sum() + modifier;
        String diceType = count + "d" + sides + (modifierStr != null ? modifierStr : "") + (modifier != 0 ? (modifier > 0 ? "+" + modifier : modifier) : "");

        return new DiceRollResponse(
                diceType,
                total,
                modifier,
                total,
                true,
                diceType + " = " + total
        );
    }

    /**
     * Rolls the Eye of Sauron check (d12).
     *
     * @return the result of the Eye of Sauron roll
     */
    public DiceRollResponse rollEyeOfSauron() {
        int roll = random.nextInt(12) + 1;
        return new DiceRollResponse(
                "d12",
                roll,
                0,
                roll,
                roll <= 3,
                "Eye of Sauron: " + roll + (roll <= 3 ? " (Sauron is watching!)" : "")
        );
    }

    /**
     * Rolls with advantage (take higher of two rolls).
     *
     * @param sides number of sides on the die
     * @return the result of the advantage roll
     */
    public DiceRollResponse rollWithAdvantage(int sides) {
        int roll1 = random.nextInt(sides) + 1;
        int roll2 = random.nextInt(sides) + 1;
        int result = Math.max(roll1, roll2);
        return new DiceRollResponse(
                "d" + sides + " (advantage)",
                result,
                0,
                result,
                true,
                "Rolled " + roll1 + " and " + roll2 + ", taking " + result
        );
    }

    /**
     * Rolls with disadvantage (take lower of two rolls).
     *
     * @param sides number of sides on the die
     * @return the result of the disadvantage roll
     */
    public DiceRollResponse rollWithDisadvantage(int sides) {
        int roll1 = random.nextInt(sides) + 1;
        int roll2 = random.nextInt(sides) + 1;
        int result = Math.min(roll1, roll2);
        return new DiceRollResponse(
                "d" + sides + " (disadvantage)",
                result,
                0,
                result,
                true,
                "Rolled " + roll1 + " and " + roll2 + ", taking " + result
        );
    }

    /**
     * Rolls multiple dice formulas at once.
     *
     * @param formulas list of dice formulas
     * @return list of dice roll results
     */
    public List<DiceRollResponse> rollMultiple(List<String> formulas) {
        List<DiceRollResponse> results = new ArrayList<>();
        for (String formula : formulas) {
            results.add(rollDice(formula));
        }
        return results;
    }

    /**
     * Rolls a narrative task check (2d6).
     *
     * @return the result of the narrative task roll
     */
    public DiceRollResponse rollNarrativeTask() {
        int roll1 = random.nextInt(6) + 1;
        int roll2 = random.nextInt(6) + 1;
        int total = roll1 + roll2;
        String description;
        boolean success;

        if (total <= 4) {
            description = "Fumble! Things go terribly wrong.";
            success = false;
        } else if (total <= 7) {
            description = "Partial success with complications.";
            success = true;
        } else if (total <= 10) {
            description = "Success!";
            success = true;
        } else {
            description = "Critical success! Exceptional outcome.";
            success = true;
        }

        return new DiceRollResponse(
                "2d6",
                total,
                0,
                total,
                success,
                "Narrative task: " + roll1 + " + " + roll2 + " = " + total + " - " + description
        );
    }
}
