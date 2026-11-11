package com.yisy.modules.doramcu.babylon5.model;

import java.util.Random;

/**
 * Wealth system for Babylon 5 character creation.
 * <p>
 * At the end of each career term, a character rolls for wealth to determine
 * how many credits they have available for spending on skills, equipment,
 * and other benefits.
 * </p>
 * <p>
 * Wealth is determined by the career and role within that career.
 * Military careers tend to have lower wealth, while merchant and noble
 * careers have higher wealth.
 * </p>
 */
public class B5WealthSystem {

    private static final Random RANDOM = new Random();

    /**
     * Wealth dice by career type.
     */
    public enum WealthDice {
        POOR("1D6", 1, 6),
        MODERATE("2D6", 2, 6),
        COMFORTABLE("3D6", 3, 6),
        WEALTHY("4D6", 4, 6),
        VERY_WEALTHY("5D6", 5, 6);

        private final String notation;
        private final int dice;
        private final int sides;

        WealthDice(String notation, int dice, int sides) {
            this.notation = notation;
            this.dice = dice;
            this.sides = sides;
        }

        public String getNotation() {
            return notation;
        }

        public int roll() {
            int total = 0;
            for (int i = 0; i < dice; i++) {
                total += RANDOM.nextInt(sides) + 1;
            }
            return total;
        }
    }

    /**
     * Retirement benefits based on career and rank.
     */
    public enum RetirementBenefit {
        NONE("None", 0, "No retirement benefit"),
        PENSION("Pension", 1, "Monthly pension payment"),
        SHIP("Ship", 2, "Retirement ship"),
        PROPERTY("Property", 2, "Retirement property"),
        SHIP_AND_PENSION("Ship & Pension", 3, "Ship and monthly pension"),
        COMMAND("Command", 4, "Retirement command position");

        private final String name;
        private final int value;
        private final String description;

        RetirementBenefit(String name, int value, String description) {
            this.name = name;
            this.value = value;
            this.description = description;
        }

        public String getName() {
            return name;
        }

        public int getValue() {
            return value;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * Result of a wealth roll.
     */
    public static class WealthResult {
        private final WealthDice dice;
        private final int level;
        private final int credits;
        private final RetirementBenefit retirementBenefit;

        public WealthResult(WealthDice dice, int level, int credits, RetirementBenefit retirementBenefit) {
            this.dice = dice;
            this.level = level;
            this.credits = credits;
            this.retirementBenefit = retirementBenefit;
        }

        public WealthDice getDice() {
            return dice;
        }

        public int getLevel() {
            return level;
        }

        public int getCredits() {
            return credits;
        }

        public RetirementBenefit getRetirementBenefit() {
            return retirementBenefit;
        }
    }

    /**
     * Roll for wealth based on career type.
     *
     * @param career the career
     * @return the wealth result
     */
    public static WealthResult rollWealth(B5Career career) {
        WealthDice dice = getWealthDice(career);
        int level = dice.roll();
        int credits = level * 1000;
        RetirementBenefit benefit = determineRetirementBenefit(career, level);
        return new WealthResult(dice, level, credits, benefit);
    }

    /**
     * Get the wealth dice for a career.
     */
    private static WealthDice getWealthDice(B5Career career) {
        switch (career) {
            case EARTH_FORCE:
            case NARN_MILITARY:
            case MINBARI_WARRIOR:
                return WealthDice.MODERATE;
            case CENTAURI_NOBILITY:
            case INDEPENDENT_MERCHANT:
                return WealthDice.COMFORTABLE;
            case INDEPENDENT_ADVENTURER:
                return WealthDice.POOR;
            case VREE_MERCHANT:
                return WealthDice.WEALTHY;
            case MINBARI_TECHNICAL:
            case HYACH_PEACEKEEPER:
                return WealthDice.MODERATE;
            case BRAKIRI_SPIRITUAL:
                return WealthDice.POOR;
            case PAKMARA_RESISTANCE:
                return WealthDice.POOR;
            case GOBLYN_SCAVENGER:
                return WealthDice.POOR;
            case MARKAB_MERCENARY:
                return WealthDice.COMFORTABLE;
            default:
                return WealthDice.MODERATE;
        }
    }

    /**
     * Determine retirement benefit based on career and wealth level.
     */
    private static RetirementBenefit determineRetirementBenefit(B5Career career, int wealthLevel) {
        if (wealthLevel >= 9) {
            return RetirementBenefit.COMMAND;
        } else if (wealthLevel >= 7) {
            return RetirementBenefit.SHIP_AND_PENSION;
        } else if (wealthLevel >= 5) {
            return RetirementBenefit.SHIP;
        } else if (wealthLevel >= 3) {
            return RetirementBenefit.PENSION;
        } else {
            return RetirementBenefit.NONE;
        }
    }
}
