package com.yisy.modules.doramcu.ai;

import com.yisy.modules.doramcu.hex.Player;

import java.util.HashMap;
import java.util.Map;

/**
 * A mutable personality profile that blends static government/ethos-based traits
 * with observed behavioral traits from the PlayerActionAnalyzer.
 * <p>
 * The blending uses confidence-based weights:
 * <ul>
 *   <li>0-10 actions: 90% government/ethos, 10% observed</li>
 *   <li>10-50 actions: 60% government/ethos, 40% observed</li>
 *   <li>50-200 actions: 30% government/ethos, 70% observed</li>
 *   <li>200+ actions: 10% government/ethos, 90% observed</li>
 * </ul>
 * </p>
 * <p>
 * This ensures the clone is always functional, even with limited data, and gradually
 * shifts toward the observed behavior as more actions are recorded.
 * </p>
 */
public class DynamicAiPersonality extends AiPersonality {

    /** The observed behavioral traits from PlayerActionAnalyzer. */
    private final Map<String, Double> observedTraits = new HashMap<>();

    /** The number of observed actions used to compute the current profile. */
    private int observedActionCount;

    /**
     * Creates a DynamicAiPersonality from a Player.
     * <p>
     * Starts with the static government/ethos-based personality and no observed data.
     * </p>
     *
     * @param player the player to derive the base personality from
     */
    public DynamicAiPersonality(Player player) {
        super(player.getGovernmentType(), player.getEthos());
        this.observedActionCount = 0;
    }

    /**
     * Creates a DynamicAiPersonality from an existing AiPersonality.
     *
     * @param base the base static personality
     */
    public DynamicAiPersonality(AiPersonality base) {
        super(base.getGovernmentType(), base.getEthos());
        this.observedActionCount = 0;
    }

    /**
     * Updates the observed traits from the PlayerActionAnalyzer.
     *
     * @param analyzer the action analyzer with observed data
     * @param playerId the player's ID
     */
    public void updateFromAnalyzer(PlayerActionAnalyzer analyzer, int playerId) {
        observedTraits.clear();
        observedActionCount = analyzer.getTotalActionCount(playerId);

        observedTraits.put("aggression", analyzer.analyzeCombatBehavior(playerId));
        observedTraits.put("expansionDrive", analyzer.analyzeExpansionBehavior(playerId));
        observedTraits.put("economicFocus", analyzer.analyzeEconomicBehavior(playerId));
        observedTraits.put("scientificFocus", analyzer.analyzeResearchBehavior(playerId));
        observedTraits.put("diplomaticStance", analyzer.analyzeDiplomaticBehavior(playerId));
        observedTraits.put("riskTolerance", analyzer.analyzeRiskBehavior(playerId));
    }

    /**
     * Updates a single observed trait.
     *
     * @param axis the trait axis name
     * @param value the observed value (0.0-1.0)
     */
    public void updateObservedTrait(String axis, double value) {
        observedTraits.put(axis, Math.max(0.0, Math.min(1.0, value)));
    }

    /**
     * Computes the observed weight based on the number of observed actions.
     *
     * @return the observed weight (0.0-1.0)
     */
    private double getObservedWeight() {
        if (observedActionCount <= 10) {
            return Math.min(0.4, 0.1 + (observedActionCount / 10.0) * 0.3);
        } else if (observedActionCount <= 50) {
            return 0.4 + ((observedActionCount - 10) / 40.0) * 0.3;
        } else if (observedActionCount <= 200) {
            return 0.7 + ((observedActionCount - 50) / 150.0) * 0.2;
        } else {
            return 0.9;
        }
    }

    /**
     * Gets the blended value for a personality axis.
     *
     * @param axis the axis name (aggression, expansionDrive, economicFocus, scientificFocus, diplomaticStance, riskTolerance)
     * @return the blended value (0.0-1.0)
     */
    public double getBlendedTrait(String axis) {
        double baseValue = getBaseTrait(axis);
        double observedValue = observedTraits.getOrDefault(axis, baseValue);
        double observedWeight = getObservedWeight();
        return baseValue * (1.0 - observedWeight) + observedValue * observedWeight;
    }

    /**
     * Gets the base (static) value for a personality axis from the super class.
     *
     * @param axis the axis name
     * @return the base value (0.0-1.0)
     */
    private double getBaseTrait(String axis) {
        return switch (axis) {
            case "aggression" -> super.getAggression();
            case "expansionDrive" -> super.getExpansionDrive();
            case "economicFocus" -> super.getEconomicFocus();
            case "scientificFocus" -> super.getScientificFocus();
            case "diplomaticStance" -> super.getDiplomaticStance();
            case "riskTolerance" -> super.getRiskTolerance();
            default -> 0.5;
        };
    }

    // --- Convenience accessors for the 6 core axes (override super) ---

    @Override
    public double getAggression() {
        return getBlendedTrait("aggression");
    }

    @Override
    public double getExpansionDrive() {
        return getBlendedTrait("expansionDrive");
    }

    @Override
    public double getEconomicFocus() {
        return getBlendedTrait("economicFocus");
    }

    @Override
    public double getScientificFocus() {
        return getBlendedTrait("scientificFocus");
    }

    @Override
    public double getDiplomaticStance() {
        return getBlendedTrait("diplomaticStance");
    }

    @Override
    public double getRiskTolerance() {
        return getBlendedTrait("riskTolerance");
    }

    /**
     * Returns the confidence score (0.0-1.0) based on the number of observed actions.
     *
     * @return the confidence score
     */
    public double getConfidenceScore() {
        return getObservedWeight();
    }

    /**
     * Returns the number of observed actions.
     *
     * @return the action count
     */
    public int getObservedActionCount() {
        return observedActionCount;
    }

    /**
     * Returns all blended personality axes as a map.
     *
     * @return a map of axis name to blended value
     */
    public Map<String, Double> toMap() {
        Map<String, Double> map = new HashMap<>();
        map.put("aggression", getAggression());
        map.put("expansionDrive", getExpansionDrive());
        map.put("economicFocus", getEconomicFocus());
        map.put("scientificFocus", getScientificFocus());
        map.put("diplomaticStance", getDiplomaticStance());
        map.put("riskTolerance", getRiskTolerance());
        return map;
    }

    /**
     * Creates a B5Personality from this dynamic personality.
     * <p>
     * Maps the 6 core axes to the 12-axis B5Personality model, using defaults for the extra axes.
     * </p>
     *
     * @return a B5Personality instance
     */
    public com.yisy.modules.doramcu.babylon5.model.B5Personality toB5Personality() {
        com.yisy.modules.doramcu.babylon5.model.B5Personality b5 = new com.yisy.modules.doramcu.babylon5.model.B5Personality();
        b5.setAggression(getAggression());
        b5.setDiplomacy(getDiplomaticStance());
        b5.setEconomicFocus(getEconomicFocus());
        b5.setScientificFocus(getScientificFocus());
        b5.setExpansionDrive(getExpansionDrive());
        b5.setRiskTolerance(getRiskTolerance());
        // Extra axes default to 0.5 (neutral) unless observed data is available
        b5.setAltruism(observedTraits.getOrDefault("altruism", 0.5));
        b5.setDetermination(observedTraits.getOrDefault("determination", 0.5));
        b5.setAdaptability(observedTraits.getOrDefault("adaptability", 0.5));
        b5.setLeadership(observedTraits.getOrDefault("leadership", 0.5));
        b5.setCunning(observedTraits.getOrDefault("cunning", 0.5));
        b5.setHonor(observedTraits.getOrDefault("honor", 0.5));
        return b5;
    }

    @Override
    public String toString() {
        return "DynamicAiPersonality{" +
                "label='" + getLabel() + '\'' +
                ", aggression=" + String.format("%.2f", getAggression()) +
                ", expansion=" + String.format("%.2f", getExpansionDrive()) +
                ", economic=" + String.format("%.2f", getEconomicFocus()) +
                ", scientific=" + String.format("%.2f", getScientificFocus()) +
                ", diplomatic=" + String.format("%.2f", getDiplomaticStance()) +
                ", risk=" + String.format("%.2f", getRiskTolerance()) +
                ", confidence=" + String.format("%.2f", getConfidenceScore()) +
                ", actions=" + observedActionCount +
                '}';
    }
}
