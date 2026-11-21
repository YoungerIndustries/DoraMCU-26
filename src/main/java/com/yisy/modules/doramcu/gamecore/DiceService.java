package com.yisy.modules.doramcu.gamecore;

import java.util.List;

/**
 * Shared interface for dice rolling across all game modules.
 * <p>
 * All game modules use {@link com.yisy.modules.doramcu.util.DiceUtil} as the underlying
 * random number generator, but this interface provides a unified API for dice rolling
 * with game-specific context (advantage/disadvantage, modifiers, etc.).
 * </p>
 */
public interface DiceService {

    /**
     * Roll a single die with the specified number of sides.
     *
     * @param sides number of sides (e.g., 4, 6, 8, 10, 12, 20, 100)
     * @return the roll result
     */
    DiceRollResult rollDSides(int sides);

    /**
     * Roll N dice with X sides each.
     *
     * @param count number of dice
     * @param sides sides per die
     * @return the roll result with individual rolls and total
     */
    DiceRollResult rollNdX(int count, int sides);

    /**
     * Roll dice with a formula string (e.g., "2d6+3", "d20", "4d6dl").
     *
     * @param formula the dice formula
     * @return the roll result
     */
    DiceRollResult rollFormula(String formula);

    /**
     * Roll with advantage (roll twice, take higher).
     *
     * @param formula the dice formula
     * @return the roll result
     */
    DiceRollResult rollWithAdvantage(String formula);

    /**
     * Roll with disadvantage (roll twice, take lower).
     *
     * @param formula the dice formula
     * @return the roll result
     */
    DiceRollResult rollWithDisadvantage(String formula);

    /**
     * Roll 2D6 with a modifier (Traveller skill check pattern).
     *
     * @param modifier bonus or penalty
     * @return the roll result
     */
    DiceRollResult roll2D6WithModifier(int modifier);

    /**
     * Result of a dice roll.
     */
    static class DiceRollResult {
        private final String formula;
        private final List<Integer> individualRolls;
        private final int total;
        private final String description;

        public DiceRollResult(String formula, List<Integer> individualRolls, int total, String description) {
            this.formula = formula;
            this.individualRolls = individualRolls;
            this.total = total;
            this.description = description;
        }

        public String getFormula() { return formula; }
        public List<Integer> getIndividualRolls() { return individualRolls; }
        public int getTotal() { return total; }
        public String getDescription() { return description; }
    }
}
