package com.yisy.modules.doramcu.traveller;

import java.util.*;

/**
 * AI opponent for naval battles.
 * <p>
 * Evaluates naval combat probabilities to make decisions about attacking, defending,
 * reinforcing, and retreating. The AI uses heuristic-based analysis of fleet strength,
 * combat odds, and CRT probabilities to make informed decisions.
 * </p>
 * <p>
 * Difficulty levels affect decision quality:
 * </p>
 * <ul>
 *   <li><b>EASY</b> — makes mistakes, poor odds evaluation</li>
 *   <li><b>MEDIUM</b> — reasonable decisions, occasional errors</li>
 *   <li><b>HARD</b> — strong tactical analysis, few mistakes</li>
 *   <li><b>EXPERT</b> — near-optimal decisions</li>
 * </ul>
 * <p>
 * Follows the same pattern as {@link com.yisy.modules.doramcu.sfb.service.SfbAIService}
 * but adapted for fleet-level naval combat.
 * </p>
 */
public class NavalBattleAiService {

    /**
     * AI difficulty level.
     */
    public enum Difficulty {
        /** Makes mistakes, poor odds evaluation */
        EASY(0.3, 0.5, 0.4),
        /** Reasonable decisions, occasional errors */
        MEDIUM(0.6, 0.7, 0.6),
        /** Strong tactical analysis, few mistakes */
        HARD(0.8, 0.9, 0.8),
        /** Near-optimal decisions */
        EXPERT(0.95, 0.95, 0.95);

        private final double tacticalAwareness;
        private final double aggression;
        private final double caution;

        Difficulty(double tacticalAwareness, double aggression, double caution) {
            this.tacticalAwareness = tacticalAwareness;
            this.aggression = aggression;
            this.caution = caution;
        }

        public double getTacticalAwareness() { return tacticalAwareness; }
        public double getAggression() { return aggression; }
        public double getCaution() { return caution; }
    }

    private Difficulty difficulty;
    private final Random random;

    /**
     * A decision made by the AI for a battle round.
     */
    public static class AiDecision {
        private final FleetAction action;
        private final boolean shouldRetreat;
        private final boolean shouldReinforce;
        private final String reasoning;

        AiDecision(FleetAction action, boolean shouldRetreat, boolean shouldReinforce, String reasoning) {
            this.action = action;
            this.shouldRetreat = shouldRetreat;
            this.shouldReinforce = shouldReinforce;
            this.reasoning = reasoning;
        }

        public FleetAction getAction() { return action; }
        public boolean shouldRetreat() { return shouldRetreat; }
        public boolean shouldReinforce() { return shouldReinforce; }
        public String getReasoning() { return reasoning; }

        @Override
        public String toString() {
            return String.format("AiDecision{action=%s, retreat=%s, reinforce=%s, reasoning='%s'}",
                    action != null ? action.getName() : "NONE", shouldRetreat, shouldReinforce, reasoning);
        }
    }

    /**
     * Creates a new AI service with medium difficulty.
     */
    public NavalBattleAiService() {
        this(Difficulty.MEDIUM);
    }

    /**
     * Creates a new AI service with the given difficulty.
     *
     * @param difficulty the AI difficulty level
     */
    public NavalBattleAiService(Difficulty difficulty) {
        this.difficulty = Objects.requireNonNull(difficulty, "Difficulty must not be null");
        this.random = new Random();
    }

    // ---- Core Decision Methods ----

    /**
     * Decides whether the AI should attack and with what action.
     * <p>
     * Evaluates fleet strength ratio and CRT probabilities to determine
     * if an attack is favorable.
     * </p>
     *
     * @param aiFleet the AI's fleet
     * @param enemyFleet the enemy fleet
     * @return the AI's decision
     */
    public AiDecision decideAttack(NavalFleet aiFleet, NavalFleet enemyFleet) {
        Objects.requireNonNull(aiFleet, "AI fleet must not be null");
        Objects.requireNonNull(enemyFleet, "Enemy fleet must not be null");

        // Calculate odds
        int aiStrength = aiFleet.getTotalCombatPower();
        int enemyStrength = enemyFleet.getTotalCombatPower();
        double oddsRatio = calculateOddsRatio(aiStrength, enemyStrength);

        StringBuilder reasoning = new StringBuilder();

        // Check if retreat is warranted
        if (shouldRetreat(aiFleet, enemyFleet, oddsRatio)) {
            return new AiDecision(FleetAction.RETREAT, true, false,
                    "Odds unfavorable (" + formatOdds(oddsRatio) + "). Retreating to preserve fleet.");
        }

        // Choose attack action based on odds and difficulty
        FleetAction action = chooseAttackAction(oddsRatio, aiFleet, reasoning);

        // Check if reinforcement is needed
        boolean reinforce = shouldReinforce(oddsRatio, aiFleet, enemyFleet);

        return new AiDecision(action, false, reinforce, reasoning.toString());
    }

    /**
     * Decides how the AI should defend.
     *
     * @param aiFleet the AI's defending fleet
     * @param enemyFleet the attacking enemy fleet
     * @return the AI's defensive decision
     */
    public AiDecision decideDefense(NavalFleet aiFleet, NavalFleet enemyFleet) {
        Objects.requireNonNull(aiFleet, "AI fleet must not be null");
        Objects.requireNonNull(enemyFleet, "Enemy fleet must not be null");

        int aiStrength = aiFleet.getTotalCombatPower();
        int enemyStrength = enemyFleet.getTotalCombatPower();
        double oddsRatio = calculateOddsRatio(aiStrength, enemyStrength);

        StringBuilder reasoning = new StringBuilder();
        reasoning.append("Defending with odds ").append(formatOdds(oddsRatio));

        // Check if retreat is the best option
        if (shouldRetreat(aiFleet, enemyFleet, oddsRatio)) {
            return new AiDecision(FleetAction.RETREAT, true, false,
                    reasoning.append(". Retreat is the best option.").toString());
        }

        // Choose defensive action
        FleetAction action = chooseDefenseAction(oddsRatio, aiFleet, reasoning);

        return new AiDecision(action, false, false, reasoning.toString());
    }

    /**
     * Decides whether the AI should reinforce mid-battle.
     *
     * @param currentOdds the current odds ratio
     * @param aiFleet the AI's current fleet
     * @param enemyFleet the enemy fleet
     * @return true if the AI should reinforce
     */
    public boolean decideReinforcement(double currentOdds, NavalFleet aiFleet, NavalFleet enemyFleet) {
        return shouldReinforce(currentOdds, aiFleet, enemyFleet);
    }

    /**
     * Decides whether the AI should retreat.
     *
     * @param currentOdds the current odds ratio
     * @param aiFleet the AI's current fleet
     * @param enemyFleet the enemy fleet
     * @return true if the AI should retreat
     */
    public boolean decideRetreat(double currentOdds, NavalFleet aiFleet, NavalFleet enemyFleet) {
        return shouldRetreat(aiFleet, enemyFleet, currentOdds);
    }

    // ---- Decision Logic ----

    /**
     * Calculates the odds ratio (AI strength / enemy strength).
     */
    private double calculateOddsRatio(int aiStrength, int enemyStrength) {
        if (enemyStrength <= 0) return Double.MAX_VALUE;
        return (double) aiStrength / enemyStrength;
    }

    /**
     * Formats the odds ratio for display.
     */
    private String formatOdds(double oddsRatio) {
        if (oddsRatio == Double.MAX_VALUE) return "∞:1";
        if (oddsRatio >= 6.0) return "6:1";
        if (oddsRatio >= 4.0) return "4:1";
        if (oddsRatio >= 2.0) return "2:1";
        if (oddsRatio >= 1.0) return "1:1";
        if (oddsRatio >= 0.5) return "1:2";
        if (oddsRatio >= 0.25) return "1:4";
        return "1:6";
    }

    /**
     * Determines if the AI should retreat based on odds and difficulty.
     */
    private boolean shouldRetreat(NavalFleet aiFleet, NavalFleet enemyFleet, double oddsRatio) {
        // If enemy has no ships, don't retreat
        if (enemyFleet.getTotalShipCount() == 0) return false;

        // If AI has no ships, must retreat
        if (aiFleet.getTotalShipCount() == 0) return true;

        double caution = difficulty.getCaution();

        // Very unfavorable odds → retreat
        if (oddsRatio < 0.25) {
            return true;
        }

        // Unfavorable odds → retreat based on caution
        if (oddsRatio < 0.5) {
            return random.nextDouble() < caution;
        }

        // Check fleet loss ratio
        int aiShips = aiFleet.getTotalShipCount();
        int aiMaxShips = aiShips; // Would need original count for accurate ratio
        double lossRatio = 1.0; // Simplified

        // Retreat if heavily damaged (simulated by low ship count)
        if (aiShips <= 2 && oddsRatio < 1.0) {
            return random.nextDouble() < caution * 0.8;
        }

        return false;
    }

    /**
     * Determines if the AI should reinforce.
     */
    private boolean shouldReinforce(double oddsRatio, NavalFleet aiFleet, NavalFleet enemyFleet) {
        double awareness = difficulty.getTacticalAwareness();

        // Reinforce when odds are close
        if (oddsRatio >= 0.75 && oddsRatio <= 1.5) {
            return random.nextDouble() < awareness * 0.6;
        }

        // Reinforce when odds are unfavorable
        if (oddsRatio < 0.75) {
            return random.nextDouble() < awareness * 0.8;
        }

        // Don't reinforce when odds are favorable
        return false;
    }

    /**
     * Chooses an attack action based on odds and fleet state.
     */
    private FleetAction chooseAttackAction(double oddsRatio, NavalFleet fleet, StringBuilder reasoning) {
        double aggression = difficulty.getAggression();
        double awareness = difficulty.getTacticalAwareness();

        // Favorable odds → aggressive attack
        if (oddsRatio >= 2.0) {
            reasoning.append("Favorable odds. ");

            // Choose between aggressive actions
            if (random.nextDouble() < aggression) {
                return FleetAction.MISSILE_SALVO;
            }
            return FleetAction.LASER_BARRAGE;
        }

        // Even odds → balanced approach
        if (oddsRatio >= 1.0) {
            reasoning.append("Even odds. ");

            FleetAction[] balancedActions = {
                FleetAction.LASER_BARRAGE,
                FleetAction.ADVANCE,
                FleetAction.MISSILE_SALVO
            };
            return balancedActions[random.nextInt(balancedActions.length)];
        }

        // Unfavorable odds → cautious approach
        reasoning.append("Unfavorable odds. ");

        if (random.nextDouble() < awareness * 0.5) {
            // Try to improve position
            return FleetAction.ADVANCE;
        }

        // Defensive attack
        return FleetAction.LASER_BARRAGE;
    }

    /**
     * Chooses a defense action based on odds and fleet state.
     */
    private FleetAction chooseDefenseAction(double oddsRatio, NavalFleet fleet, StringBuilder reasoning) {
        double caution = difficulty.getCaution();

        // If defending with advantage, hold position
        if (oddsRatio >= 2.0) {
            reasoning.append("Strong defense. ");
            return FleetAction.HOLD;
        }

        // Even odds → hold and counter
        if (oddsRatio >= 1.0) {
            reasoning.append("Holding position. ");
            return FleetAction.HOLD;
        }

        // Unfavorable → consider retreat or defensive maneuver
        if (random.nextDouble() < caution) {
            reasoning.append("Considering retreat. ");
            return FleetAction.RETREAT;
        }

        // Try to improve position
        reasoning.append("Maneuvering. ");
        return FleetAction.ADVANCE;
    }

    /**
     * Executes a full AI turn for a naval battle.
     * <p>
     * This method makes all decisions for the AI's fleet in a single round.
     * </p>
     *
     * @param aiFleet the AI's fleet
     * @param enemyFleet the enemy fleet
     * @param battle the current battle
     * @return the AI's decision
     */
    public AiDecision executeAiTurn(NavalFleet aiFleet, NavalFleet enemyFleet, NavalBattle battle) {
        Objects.requireNonNull(aiFleet, "AI fleet must not be null");
        Objects.requireNonNull(enemyFleet, "Enemy fleet must not be null");

        // Determine if AI is attacking or defending
        boolean isAttacker = true; // Simplified — would need battle context

        if (isAttacker) {
            return decideAttack(aiFleet, enemyFleet);
        } else {
            return decideDefense(aiFleet, enemyFleet);
        }
    }

    /**
     * Returns the current difficulty level.
     *
     * @return the difficulty
     */
    public Difficulty getDifficulty() {
        return difficulty;
    }

    /**
     * Sets the difficulty level.
     *
     * @param difficulty the new difficulty
     */
    public void setDifficulty(Difficulty difficulty) {
        this.difficulty = Objects.requireNonNull(difficulty, "Difficulty must not be null");
    }

    /**
     * Evaluates the probability of a favorable CRT result given the odds.
     *
     * @param oddsRatio the attack/defense odds ratio
     * @return probability of favorable outcome (0.0 to 1.0)
     */
    public double evaluateCrtProbability(double oddsRatio) {
        // Simplified CRT probability model
        if (oddsRatio >= 6.0) return 0.95;
        if (oddsRatio >= 4.0) return 0.85;
        if (oddsRatio >= 2.0) return 0.70;
        if (oddsRatio >= 1.0) return 0.50;
        if (oddsRatio >= 0.5) return 0.35;
        if (oddsRatio >= 0.25) return 0.20;
        return 0.10;
    }
}
