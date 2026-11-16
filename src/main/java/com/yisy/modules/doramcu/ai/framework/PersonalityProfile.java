package com.yisy.modules.doramcu.ai.framework;

import java.util.Map;

/**
 * Unified personality profile interface that consolidates AiPersonality,
 * DynamicAiPersonality, and B5Personality into a single contract.
 * <p>
 * All personality types implement this interface, providing a common API
 * for accessing personality traits regardless of the underlying implementation.
 * </p>
 * <p>
 * The 6 core axes are:
 * <ul>
 *   <li>aggression - combat倾向</li>
 *   <li>expansionDrive - exploration and expansion</li>
 *   <li>economicFocus - economic investment</li>
 *   <li>scientificFocus - research investment</li>
 *   <li>diplomaticStance - diplomatic倾向</li>
 *   <li>riskTolerance - risk-taking behavior</li>
 * </ul>
 * </p>
 * <p>
 * Extended personalities (like B5Personality) add 6 more axes:
 * altruism, determination, adaptability, leadership, cunning, honor.
 * </p>
 */
public interface PersonalityProfile {

    /**
     * Gets the value for a personality axis.
     *
     * @param axis the axis name (e.g., "aggression", "expansionDrive")
     * @return the value (0.0-1.0)
     */
    double getTrait(String axis);

    /**
     * Sets the value for a personality axis.
     *
     * @param axis the axis name
     * @param value the value (0.0-1.0)
     */
    void setTrait(String axis, double value);

    /**
     * Gets the aggression level.
     *
     * @return aggression value (0.0-1.0)
     */
    double getAggression();

    /**
     * Gets the expansion drive.
     *
     * @return expansion drive value (0.0-1.0)
     */
    double getExpansionDrive();

    /**
     * Gets the economic focus.
     *
     * @return economic focus value (0.0-1.0)
     */
    double getEconomicFocus();

    /**
     * Gets the scientific focus.
     *
     * @return scientific focus value (0.0-1.0)
     */
    double getScientificFocus();

    /**
     * Gets the diplomatic stance.
     *
     * @return diplomatic stance value (0.0-1.0)
     */
    double getDiplomaticStance();

    /**
     * Gets the risk tolerance.
     *
     * @return risk tolerance value (0.0-1.0)
     */
    double getRiskTolerance();

    /**
     * Returns all personality axes as a map.
     *
     * @return a map of axis name to value
     */
    Map<String, Double> toMap();

    /**
     * Returns the confidence score for this personality profile.
     * <p>
     * For static personalities, this is 1.0. For dynamic personalities,
     * this reflects the amount of observed data (0.0-1.0).
     * </p>
     *
     * @return the confidence score (0.0-1.0)
     */
    default double getConfidenceScore() {
        return 1.0;
    }

    /**
     * Returns the number of observed actions used to compute this profile.
     *
     * @return the action count (0 for static personalities)
     */
    default int getObservedActionCount() {
        return 0;
    }

    /**
     * Returns the type of this personality profile.
     *
     * @return the profile type
     */
    PersonalityType getType();

    /**
     * Returns a human-readable label for this personality.
     *
     * @return the label
     */
    String getLabel();

    /**
     * The type of personality profile.
     */
    enum PersonalityType {
        /** Static personality derived from government/ethos */
        STATIC,
        /** Dynamic personality that blends observed behavior */
        DYNAMIC,
        /** Extended 12-axis personality (B5 model) */
        EXTENDED,
        /** AI NPC personality */
        NPC
    }
}
