package com.yisy.modules.doramcu.hex;

import java.util.*;

/**
 * Represents a survey mission to a celestial body.
 * <p>
 * Survey missions discover new resource deposits, assess mineral quality,
 * and reveal strategic resources. They consume turns and may improve
 * the celestial body's mining yield or resource level.
 * </p>
 * <p>
 * A survey can:
 * </p>
 * <ul>
 *   <li>Reveal hidden strategic resources</li>
 *   <li>Improve the mining yield multiplier</li>
 *   <li>Restore depleted resource levels (re-surveying)</li>
 *   <li>Discover new resource types</li>
 * </ul>
 */
public class SurveyMission {

    private final int id;
    private final int empireId;
    private final CelestialBody targetBody;
    private final SurveyType type;
    private final int totalTurns;
    private int turnsRemaining;
    private boolean completed;
    private boolean cancelled;

    /** Results of the survey. */
    private SurveyResult result;

    /** Default survey duration in turns. */
    public static final int DEFAULT_SURVEY_TURNS = 3;

    /** Probability of discovering a strategic resource (10%). */
    public static final double STRATEGIC_DISCOVERY_CHANCE = 0.1;

    /** Maximum mining yield improvement per survey (0.2). */
    public static final double MAX_YIELD_IMPROVEMENT = 0.2;

    /** Maximum resource level restoration per survey (20%). */
    public static final double MAX_RESOURCE_RESTORE = 20.0;

    /**
     * The type of survey mission.
     */
    public enum SurveyType {
        /** Basic survey: reveals basic resource information. */
        BASIC,
        /** Detailed survey: improves mining yield. */
        DETAILED,
        /** Strategic survey: searches for strategic resources. */
        STRATEGIC,
        /** Re-survey: restores depleted resource levels. */
        RESURVEY
    }

    /**
     * Results of a completed survey mission.
     */
    public static class SurveyResult {
        public final boolean discoveredStrategicResource;
        public final StrategicResource strategicResource;
        public final double yieldImprovement;
        public final double resourceRestoration;
        public final String description;

        public SurveyResult(boolean discoveredStrategicResource, StrategicResource strategicResource,
                             double yieldImprovement, double resourceRestoration, String description) {
            this.discoveredStrategicResource = discoveredStrategicResource;
            this.strategicResource = strategicResource;
            this.yieldImprovement = yieldImprovement;
            this.resourceRestoration = resourceRestoration;
            this.description = description;
        }

        @Override
        public String toString() {
            return String.format("SurveyResult{strategic=%s, yield+%.0f%%, restore+%.0f%%, %s}",
                    discoveredStrategicResource ? strategicResource : "none",
                    yieldImprovement * 100, resourceRestoration, description);
        }
    }

    private final Random random;

    /**
     * Creates a new survey mission.
     *
     * @param id the unique mission ID
     * @param empireId the empire conducting the survey
     * @param targetBody the celestial body to survey
     * @param type the type of survey
     * @param random the random number generator
     */
    public SurveyMission(int id, int empireId, CelestialBody targetBody,
                          SurveyType type, Random random) {
        this.id = id;
        this.empireId = empireId;
        this.targetBody = targetBody;
        this.type = type;
        this.totalTurns = DEFAULT_SURVEY_TURNS;
        this.turnsRemaining = totalTurns;
        this.completed = false;
        this.cancelled = false;
        this.result = null;
        this.random = random;
    }

    /** @return the unique mission ID */
    public int getId() {
        return id;
    }

    /** @return the empire ID conducting the survey */
    public int getEmpireId() {
        return empireId;
    }

    /** @return the celestial body being surveyed */
    public CelestialBody getTargetBody() {
        return targetBody;
    }

    /** @return the type of survey */
    public SurveyType getType() {
        return type;
    }

    /** @return the total turns for this survey */
    public int getTotalTurns() {
        return totalTurns;
    }

    /** @return the remaining turns */
    public int getTurnsRemaining() {
        return turnsRemaining;
    }

    /** @return true if the survey is in progress */
    public boolean isInProgress() {
        return !completed && !cancelled;
    }

    /** @return true if the survey is completed */
    public boolean isCompleted() {
        return completed;
    }

    /** @return true if the survey is cancelled */
    public boolean isCancelled() {
        return cancelled;
    }

    /** @return the survey result, or null if not completed */
    public SurveyResult getResult() {
        return result;
    }

    /**
     * Advances the survey by one turn.
     *
     * @return true if the survey just completed this turn
     */
    public boolean processTurn() {
        if (completed || cancelled) return false;
        turnsRemaining--;
        if (turnsRemaining <= 0) {
            completed = true;
            executeSurvey();
        }
        return completed;
    }

    /**
     * Cancels the survey mission.
     */
    public void cancel() {
        cancelled = true;
    }

    /**
     * Executes the survey and generates results.
     */
    private void executeSurvey() {
        boolean discoveredStrategic = false;
        StrategicResource strategicResource = null;
        double yieldImprovement = 0.0;
        double resourceRestoration = 0.0;
        StringBuilder description = new StringBuilder();

        switch (type) {
            case BASIC:
                // Basic survey: small yield improvement
                yieldImprovement = random.nextDouble() * 0.1;
                description.append("Basic survey completed. Minor yield improvement detected.");
                break;

            case DETAILED:
                // Detailed survey: larger yield improvement
                yieldImprovement = random.nextDouble() * MAX_YIELD_IMPROVEMENT;
                description.append("Detailed survey completed. Significant yield improvement detected.");
                break;

            case STRATEGIC:
                // Strategic survey: chance to discover strategic resource
                if (random.nextDouble() < STRATEGIC_DISCOVERY_CHANCE && !targetBody.hasStrategicDeposit()) {
                    discoveredStrategic = true;
                    StrategicResource[] resources = StrategicResource.values();
                    strategicResource = resources[random.nextInt(resources.length)];
                    targetBody.setStrategicResource(strategicResource);
                    description.append("Strategic resource discovered: ").append(strategicResource.name());
                } else {
                    description.append("Strategic survey completed. No new strategic resources found.");
                }
                yieldImprovement = random.nextDouble() * 0.05;
                break;

            case RESURVEY:
                // Re-survey: restore depleted resources
                if (targetBody.getResourceLevel() < 100.0) {
                    resourceRestoration = random.nextDouble() * MAX_RESOURCE_RESTORE;
                    double newLevel = Math.min(100.0, targetBody.getResourceLevel() + resourceRestoration);
                    targetBody.setResourceLevel(newLevel);
                    description.append("Re-survey completed. Resource deposits partially restored.");
                } else {
                    description.append("Re-survey completed. Resources already at full capacity.");
                }
                break;
        }

        // Apply yield improvement
        if (yieldImprovement > 0) {
            double currentYield = targetBody.getMiningYield();
            double newYield = Math.min(2.0, currentYield + yieldImprovement);
            targetBody.setMiningYield(newYield);
        }

        result = new SurveyResult(discoveredStrategic, strategicResource,
                yieldImprovement, resourceRestoration, description.toString());
    }

    @Override
    public String toString() {
        return String.format("SurveyMission{id=%d, empire=%d, body=%s, type=%s, turns=%d/%d, status=%s}",
                id, empireId, targetBody.getName(), type,
                totalTurns - turnsRemaining, totalTurns,
                completed ? "completed" : (cancelled ? "cancelled" : "in progress"));
    }
}
