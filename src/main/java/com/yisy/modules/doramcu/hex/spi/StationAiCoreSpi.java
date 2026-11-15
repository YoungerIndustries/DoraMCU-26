package com.yisy.modules.doramcu.hex.spi;

import com.yisy.modules.doramcu.hex.Fleet;
import com.yisy.modules.doramcu.hex.SpaceStation;

import java.util.List;

/**
 * SPI for station AI core operations.
 * <p>
 * Hex domain classes depend on this interface instead of the concrete
 * {@code StationAiCore} in the ai package, breaking the circular dependency
 * between hex and ai packages.
 * </p>
 * <p>
 * The ai package implements this interface in {@code StationAiCore}.
 * </p>
 */
public interface StationAiCoreSpi {

    /**
     * The AI Core level installed on a station.
     */
    enum Level {
        /** No AI Core installed */
        NONE(0),
        /** Basic AI Core — monitoring and reporting */
        BASIC(1),
        /** Advanced AI Core — active optimization */
        ADVANCED(2),
        /** Elite AI Core — autonomous operation */
        ELITE(3);

        private final int value;

        Level(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    /**
     * Returns the station managed by this AI Core.
     */
    SpaceStation getStation();

    /**
     * Returns the AI Core level installed on this station.
     */
    Level getAiCoreLevel();

    /**
     * Sets the AI Core level for this station.
     */
    void setAiCoreLevel(Level level);

    /**
     * Returns true if this station has an AI Core installed.
     */
    boolean hasAiCore();

    /**
     * Returns the numeric value of the AI Core level (0-3).
     */
    int getLevelValue();

    /**
     * Returns the tactical support radius for this station.
     */
    int getSupportRadius();

    /**
     * Returns the defense bonus provided to stationed fleets.
     */
    double getDefenseBonus();

    /**
     * Returns the tactical support bonus for a fleet within range.
     */
    double getFleetSupportBonus(Fleet fleet);

    /**
     * Checks if a fleet is within the support radius of this station.
     */
    boolean isFleetInRange(Fleet fleet);
}
