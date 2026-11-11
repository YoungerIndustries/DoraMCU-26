package com.yisy.modules.doramcu.thirdreich.combat;

/**
 * Combat Results Table (CRT) for 3R4 ground combat.
 * <p>
 * The CRT determines the outcome of a combat encounter based on the attack-to-defense
 * odds ratio and a 2D6 dice roll. This follows the classic 3R4 board game mechanics.
 * </p>
 */
public class CombatResultsTable {

    /**
     * Possible combat results from a CRT lookup.
     */
    public enum CombatResult {
        ATTACKER_VICTORY("Attacker Victory"),
        ATTACKER_GOOD("Attacker Good"),
        EVEN("Even"),
        DEFENDER_HOLDS("Defender Holds"),
        DEFENDER_VICTORY("Defender Victory"),
        ATTACKER_ROUT("Attacker Rout"),
        DEFENDER_ROUT("Defender Rout");

        private final String displayName;

        CombatResult(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }

        @Override
        public String toString() {
            return displayName;
        }
    }

    private static final CombatResult[][] CRT = {
            {CombatResult.ATTACKER_ROUT, CombatResult.ATTACKER_ROUT, CombatResult.ATTACKER_ROUT, CombatResult.ATTACKER_ROUT, CombatResult.ATTACKER_ROUT, CombatResult.ATTACKER_ROUT, CombatResult.ATTACKER_ROUT, CombatResult.ATTACKER_ROUT, CombatResult.ATTACKER_ROUT, CombatResult.ATTACKER_ROUT, CombatResult.ATTACKER_ROUT},
            {CombatResult.ATTACKER_ROUT, CombatResult.ATTACKER_ROUT, CombatResult.ATTACKER_ROUT, CombatResult.ATTACKER_ROUT, CombatResult.ATTACKER_ROUT, CombatResult.ATTACKER_ROUT, CombatResult.ATTACKER_ROUT, CombatResult.ATTACKER_ROUT, CombatResult.ATTACKER_ROUT, CombatResult.ATTACKER_ROUT, CombatResult.ATTACKER_ROUT},
            {CombatResult.ATTACKER_ROUT, CombatResult.ATTACKER_ROUT, CombatResult.ATTACKER_ROUT, CombatResult.ATTACKER_ROUT, CombatResult.ATTACKER_ROUT, CombatResult.EVEN, CombatResult.EVEN, CombatResult.EVEN, CombatResult.DEFENDER_HOLDS, CombatResult.DEFENDER_HOLDS, CombatResult.DEFENDER_HOLDS},
            {CombatResult.ATTACKER_ROUT, CombatResult.ATTACKER_ROUT, CombatResult.ATTACKER_ROUT, CombatResult.EVEN, CombatResult.EVEN, CombatResult.EVEN, CombatResult.EVEN, CombatResult.ATTACKER_GOOD, CombatResult.ATTACKER_GOOD, CombatResult.ATTACKER_VICTORY, CombatResult.ATTACKER_VICTORY},
            {CombatResult.ATTACKER_ROUT, CombatResult.EVEN, CombatResult.EVEN, CombatResult.EVEN, CombatResult.EVEN, CombatResult.ATTACKER_GOOD, CombatResult.ATTACKER_GOOD, CombatResult.ATTACKER_GOOD, CombatResult.ATTACKER_VICTORY, CombatResult.ATTACKER_VICTORY, CombatResult.ATTACKER_VICTORY},
            {CombatResult.EVEN, CombatResult.EVEN, CombatResult.EVEN, CombatResult.ATTACKER_GOOD, CombatResult.ATTACKER_GOOD, CombatResult.ATTACKER_GOOD, CombatResult.ATTACKER_GOOD, CombatResult.ATTACKER_VICTORY, CombatResult.ATTACKER_VICTORY, CombatResult.ATTACKER_VICTORY, CombatResult.DEFENDER_ROUT},
            {CombatResult.EVEN, CombatResult.EVEN, CombatResult.ATTACKER_GOOD, CombatResult.ATTACKER_GOOD, CombatResult.ATTACKER_GOOD, CombatResult.ATTACKER_VICTORY, CombatResult.ATTACKER_VICTORY, CombatResult.ATTACKER_VICTORY, CombatResult.ATTACKER_VICTORY, CombatResult.DEFENDER_ROUT, CombatResult.DEFENDER_ROUT}
    };

    public CombatResult lookup(double oddsRatio, int diceRoll) {
        int oddsIndex = getOddsIndex(oddsRatio);
        int diceIndex = Math.max(0, Math.min(10, diceRoll - 2));
        return CRT[oddsIndex][diceIndex];
    }

    public static int getOddsIndex(double oddsRatio) {
        if (oddsRatio <= 0.33) return 0;
        if (oddsRatio <= 0.5) return 1;
        if (oddsRatio <= 1.0) return 2;
        if (oddsRatio <= 2.0) return 3;
        if (oddsRatio <= 3.0) return 4;
        if (oddsRatio <= 4.0) return 5;
        return 6;
    }

    public static double calculateOdds(int attack, int defense) {
        if (defense <= 0) return 6.0;
        if (attack <= 0) return 0.0;
        return (double) attack / defense;
    }

    public static int getAttackerCasualtyPercent(CombatResult result) {
        switch (result) {
            case ATTACKER_VICTORY: return 5;
            case ATTACKER_GOOD: return 10;
            case EVEN: return 15;
            case DEFENDER_HOLDS: return 20;
            case DEFENDER_VICTORY: return 30;
            case ATTACKER_ROUT: return 40;
            case DEFENDER_ROUT: return 10;
            default: return 15;
        }
    }

    public static int getDefenderCasualtyPercent(CombatResult result) {
        switch (result) {
            case ATTACKER_VICTORY: return 25;
            case ATTACKER_GOOD: return 15;
            case EVEN: return 15;
            case DEFENDER_HOLDS: return 10;
            case DEFENDER_VICTORY: return 5;
            case ATTACKER_ROUT: return 10;
            case DEFENDER_ROUT: return 35;
            default: return 15;
        }
    }

    public static int getAttackerMoralePenalty(CombatResult result) {
        switch (result) {
            case ATTACKER_VICTORY: return -5;
            case ATTACKER_GOOD: return 0;
            case EVEN: return 5;
            case DEFENDER_HOLDS: return 10;
            case DEFENDER_VICTORY: return 15;
            case ATTACKER_ROUT: return 20;
            case DEFENDER_ROUT: return 0;
            default: return 5;
        }
    }

    public static int getDefenderMoralePenalty(CombatResult result) {
        switch (result) {
            case ATTACKER_VICTORY: return 15;
            case ATTACKER_GOOD: return 10;
            case EVEN: return 5;
            case DEFENDER_HOLDS: return 0;
            case DEFENDER_VICTORY: return -5;
            case ATTACKER_ROUT: return 0;
            case DEFENDER_ROUT: return 20;
            default: return 5;
        }
    }
}
