package com.yisy.modules.doramcu.sfb.model;

import java.util.Random;

/**
 * Wealth system for SFB character creation.
 * <p>
 * Determines starting credits and retirement benefits based on career.
 * Follows the Traveller-style wealth dice system where each career
 * has a wealth dice type that determines starting credits.
 * </p>
 */
public class SfbWealthSystem {

    private static final Random RANDOM = new Random();

    /**
     * Wealth dice types for SFB careers.
     */
    public enum WealthDice {
        POOR("1D6", 1, 6),
        MODERATE("2D6", 2, 12),
        COMFORTABLE("3D6", 3, 18),
        WEALTHY("4D6", 4, 24),
        VERY_WEALTHY("5D6", 5, 30);

        private final String notation;
        private final int dice;
        private final int max;

        WealthDice(String notation, int dice, int max) {
            this.notation = notation;
            this.dice = dice;
            this.max = max;
        }

        public String getNotation() {
            return notation;
        }

        public int getDice() {
            return dice;
        }

        public int getMax() {
            return max;
        }
    }

    /**
     * SFB career wealth mapping.
     */
    public enum SfbCareerWealth {
        STARFLEET_ACADEMY("Starfleet Academy", WealthDice.MODERATE),
        STARFLEET_COMMAND("Starfleet Command", WealthDice.COMFORTABLE),
        STARFLEET_SCIENCE("Starfleet Science", WealthDice.MODERATE),
        STARFLEET_ENGINEERING("Starfleet Engineering", WealthDice.MODERATE),
        STARFLEET_MEDICAL("Starfleet Medical", WealthDice.COMFORTABLE),
        KLINGON_DEFENSE_FORCE("Klingon Defense Force", WealthDice.MODERATE),
        KLINGON_HONOR_GUARD("Klingon Honor Guard", WealthDice.COMFORTABLE),
        ROMULAN_SENTINEL("Romulan Sentinel", WealthDice.MODERATE),
        ROMULAN_SYNDICATE("Romulan Syndicate", WealthDice.VERY_WEALTHY),
        INDEPENDENT_MERCHANT("Independent Merchant", WealthDice.WEALTHY),
        INDEPENDENT_PIRATE("Independent Pirate", WealthDice.WEALTHY),
        INDEPENDENT_EXPLORER("Independent Explorer", WealthDice.POOR),
        FEDERATION_DIPLOMAT("Federation Diplomat", WealthDice.COMFORTABLE);

        private final String name;
        private final WealthDice wealthDice;

        SfbCareerWealth(String name, WealthDice wealthDice) {
            this.name = name;
            this.wealthDice = wealthDice;
        }

        public String getName() {
            return name;
        }

        public WealthDice getWealthDice() {
            return wealthDice;
        }
    }

    /**
     * Result of a wealth roll.
     */
    public static class WealthResult {
        private final SfbCareerWealth career;
        private final WealthDice dice;
        private final int level;
        private final int credits;
        private final String retirementBenefit;

        public WealthResult(SfbCareerWealth career, WealthDice dice, int level, int credits, String retirementBenefit) {
            this.career = career;
            this.dice = dice;
            this.level = level;
            this.credits = credits;
            this.retirementBenefit = retirementBenefit;
        }

        public SfbCareerWealth getCareer() {
            return career;
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

        public String getRetirementBenefit() {
            return retirementBenefit;
        }
    }

    /**
     * Rolls wealth for a given career.
     *
     * @param career the career to roll wealth for
     * @return the wealth result
     */
    public static WealthResult rollWealth(SfbCareerWealth career) {
        WealthDice dice = career.getWealthDice();
        int roll = 0;
        for (int i = 0; i < dice.getDice(); i++) {
            roll += RANDOM.nextInt(6) + 1;
        }

        int level = roll;
        int credits = roll * 1000;

        String benefit = determineRetirementBenefit(career, roll);

        return new WealthResult(career, dice, level, credits, benefit);
    }

    /**
     * Determines retirement benefit based on career and wealth roll.
     */
    private static String determineRetirementBenefit(SfbCareerWealth career, int roll) {
        if (roll >= 16) {
            return switch (career) {
                case STARFLEET_COMMAND -> "Starfleet Captain's pension + personal shuttle";
                case STARFLEET_SCIENCE -> "Science division pension + research grant";
                case STARFLEET_ENGINEERING -> "Engineering pension + workshop";
                case STARFLEET_MEDICAL -> "Medical pension + medical facility access";
                case STARFLEET_ACADEMY -> "Academy pension + teaching position";
                case KLINGON_DEFENSE_FORCE -> "KDF pension + honor guard position";
                case KLINGON_HONOR_GUARD -> "Emperor's guard pension + land grant";
                case ROMULAN_SENTINEL -> "Sentinel pension + cloaking device access";
                case ROMULAN_SYNDICATE -> "Syndicate fortune + hidden base";
                case INDEPENDENT_MERCHANT -> "Merchant fleet + trade routes";
                case INDEPENDENT_PIRATE -> "Pirate treasure + hidden ship";
                case INDEPENDENT_EXPLORER -> "Explorer's journal + star charts";
                case FEDERATION_DIPLOMAT -> "Diplomatic pension + embassy access";
            };
        } else if (roll >= 10) {
            return switch (career) {
                case STARFLEET_COMMAND -> "Starfleet Lieutenant pension";
                case STARFLEET_SCIENCE -> "Science division pension";
                case STARFLEET_ENGINEERING -> "Engineering pension";
                case STARFLEET_MEDICAL -> "Medical pension";
                case STARFLEET_ACADEMY -> "Academy pension";
                case KLINGON_DEFENSE_FORCE -> "KDF pension";
                case KLINGON_HONOR_GUARD -> "Honor guard pension";
                case ROMULAN_SENTINEL -> "Sentinel pension";
                case ROMULAN_SYNDICATE -> "Syndicate connections";
                case INDEPENDENT_MERCHANT -> "Small merchant vessel";
                case INDEPENDENT_PIRATE -> "Pirate crew position";
                case INDEPENDENT_EXPLORER -> "Star charts";
                case FEDERATION_DIPLOMAT -> "Diplomatic pension";
            };
        } else {
            return "Standard retirement pension";
        }
    }
}
