package com.yisy.modules.doramcu.ai;

import java.util.ArrayList;
import java.util.List;

/**
 * A single recorded player action with metadata for behavioral analysis.
 * <p>
 * The PlayerActionAnalyzer stores these records to compute personality profiles.
 * Each record captures the action category, intensity, and turn number.
 * </p>
 */
public class PlayerActionRecord {

    /** The category of the action. */
    private final PlayerActionCategory category;

    /** Intensity of the action (0.0-1.0), indicating how strongly it signals a trait. */
    private final double intensity;

    /** Turn number when the action occurred. */
    private final int turnNumber;

    /** Optional context description. */
    private final String context;

    /**
     * Creates a new action record.
     *
     * @param category the action category
     * @param intensity the intensity (0.0-1.0)
     * @param turnNumber the turn number
     * @param context optional context description
     */
    public PlayerActionRecord(PlayerActionCategory category, double intensity, int turnNumber, String context) {
        this.category = category;
        this.intensity = Math.max(0.0, Math.min(1.0, intensity));
        this.turnNumber = turnNumber;
        this.context = context;
    }

    public PlayerActionCategory getCategory() {
        return category;
    }

    public double getIntensity() {
        return intensity;
    }

    public int getTurnNumber() {
        return turnNumber;
    }

    public String getContext() {
        return context;
    }

    @Override
    public String toString() {
        return "PlayerActionRecord{" +
                "category=" + category +
                ", intensity=" + String.format("%.2f", intensity) +
                ", turn=" + turnNumber +
                '}';
    }
}
