package com.yisy.modules.doramcu.dnd5e.service;

import com.yisy.modules.doramcu.dnd5e.dto.DiceRollResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * General-purpose dice rolling service for D&D 5e.
 * Supports formulas like: d20, 2d6, 3d8+2, 4d6dl, d100
 */
@Service
public class Dnd5eDiceService {

    private static final Logger log = LoggerFactory.getLogger(Dnd5eDiceService.class);
    private static final Random random = new Random();

    // Pattern: optional count, 'd', sides, optional modifier, optional drop lowest
    private static final Pattern DICE_PATTERN = Pattern.compile(
            "(\\d*)d(100|20|12|10|8|6|4)([+-]\\d+)?(dl)?",
            Pattern.CASE_INSENSITIVE
    );

    /**
     * Roll dice using a formula string.
     * Examples: "d20", "2d6", "3d8+2", "4d6dl"
     */
    public DiceRollResponse rollDice(String formula) {
        if (formula == null || formula.trim().isEmpty()) {
            throw new IllegalArgumentException("Dice formula cannot be empty");
        }

        String cleanFormula = formula.trim().toLowerCase();
        Matcher matcher = DICE_PATTERN.matcher(cleanFormula);

        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid dice formula: " + formula + ". Use format like: d20, 2d6, 3d8+2, 4d6dl");
        }

        int count = matcher.group(1).isEmpty() ? 1 : Integer.parseInt(matcher.group(1));
        int sides = Integer.parseInt(matcher.group(2));
        int modifier = matcher.group(3) != null ? Integer.parseInt(matcher.group(3)) : 0;
        boolean dropLowest = matcher.group(4) != null;

        List<Integer> rolls = new ArrayList<>();
        int sum = 0;

        for (int i = 0; i < count; i++) {
            int roll = random.nextInt(sides) + 1;
            rolls.add(roll);
            sum += roll;
        }

        // Drop lowest if requested
        if (dropLowest && count > 1) {
            int minIndex = 0;
            for (int i = 1; i < rolls.size(); i++) {
                if (rolls.get(i) < rolls.get(minIndex)) {
                    minIndex = i;
                }
            }
            sum -= rolls.get(minIndex);
            rolls.remove(minIndex);
        }

        int total = sum + modifier;

        String description = buildDescription(count, sides, modifier, dropLowest, rolls, total);

        log.debug("Rolled {}: {} = {}", formula, rolls, total);
        return new DiceRollResponse(formula, rolls, total, description);
    }

    /**
     * Roll with advantage (roll twice, take higher).
     */
    public DiceRollResponse rollWithAdvantage(String formula) {
        DiceRollResponse roll1 = rollDice(formula);
        DiceRollResponse roll2 = rollDice(formula);

        int total = Math.max(roll1.getTotal(), roll2.getTotal());
        List<Integer> allRolls = new ArrayList<>();
        allRolls.addAll(roll1.getIndividualRolls());
        allRolls.add(roll2.getTotal()); // Add the second roll total for comparison

        String description = roll1.getDescription() + " (advantage: " + total + ")";
        return new DiceRollResponse(formula + " (advantage)", allRolls, total, description);
    }

    /**
     * Roll with disadvantage (roll twice, take lower).
     */
    public DiceRollResponse rollWithDisadvantage(String formula) {
        DiceRollResponse roll1 = rollDice(formula);
        DiceRollResponse roll2 = rollDice(formula);

        int total = Math.min(roll1.getTotal(), roll2.getTotal());
        List<Integer> allRolls = new ArrayList<>();
        allRolls.addAll(roll1.getIndividualRolls());
        allRolls.add(roll2.getTotal());

        String description = roll1.getDescription() + " (disadvantage: " + total + ")";
        return new DiceRollResponse(formula + " (disadvantage)", allRolls, total, description);
    }

    /**
     * Roll multiple dice formulas at once.
     * Example: ["2d6+3", "d20"] -> rolls both and returns combined result
     */
    public DiceRollResponse rollMultiple(List<String> formulas) {
        List<Integer> allRolls = new ArrayList<>();
        int combinedTotal = 0;
        StringBuilder description = new StringBuilder();

        for (String formula : formulas) {
            DiceRollResponse response = rollDice(formula);
            allRolls.addAll(response.getIndividualRolls());
            combinedTotal += response.getTotal();
            description.append(response.getDescription()).append("; ");
        }

        description.setLength(Math.max(0, description.length() - 2)); // Remove trailing "; "
        return new DiceRollResponse(String.join(" + ", formulas), allRolls, combinedTotal, description.toString());
    }

    private String buildDescription(int count, int sides, int modifier, boolean dropLowest, List<Integer> rolls, int total) {
        StringBuilder sb = new StringBuilder();
        sb.append(count).append("d").append(sides);
        if (dropLowest) sb.append(" drop lowest");
        if (modifier > 0) sb.append("+").append(modifier);
        else if (modifier < 0) sb.append(modifier);
        sb.append(" = ").append(total);
        return sb.toString();
    }
}
