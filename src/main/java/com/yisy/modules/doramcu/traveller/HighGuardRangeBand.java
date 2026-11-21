package com.yisy.modules.doramcu.traveller;

import java.util.Arrays;

/**
 * Enum for High Guard range bands.
 */
public enum HighGuardRangeBand {

    ADJACENT(0, 10, 1, true, "Adjacent range - dogfighting"),
    CLOSE(10, 50, 1, true, "Close range - dogfighting"),
    SHORT(50, 500, 1, false, "Short range"),
    MEDIUM(500, 2000, 1, false, "Medium range"),
    LONG(2000, 8000, 1, false, "Long range"),
    VERY_LONG(8000, 16000, 1, false, "Very long range"),
    DISTANT(16000, 32000, 1, false, "Distant range"),
    DOGFIGHT(0, 100, 1, true, "Close range dogfighting"),
    EXTREME(8000, 32000, 1, false, "Extreme range");

    private final int minDistanceKm;
    private final int maxDistanceKm;
    private final int thrustToChange;
    private final boolean dogfighting;
    private final String description;

    HighGuardRangeBand(int minDistanceKm, int maxDistanceKm, int thrustToChange, boolean dogfighting, String description) {
        this.minDistanceKm = minDistanceKm;
        this.maxDistanceKm = maxDistanceKm;
        this.thrustToChange = thrustToChange;
        this.dogfighting = dogfighting;
        this.description = description;
    }

    public String getName() {
        return name().toLowerCase();
    }

    public int getMinDistanceKm() {
        return minDistanceKm;
    }

    public int getMaxDistanceKm() {
        return maxDistanceKm;
    }

    public int getThrustToChange() {
        return thrustToChange;
    }

    public boolean isDogfighting() {
        return dogfighting;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Calculates the new range band after thrust movement.
     */
    public static HighGuardRangeBand calculateNewRange(HighGuardRangeBand current, int thrust0, int thrust1, boolean side0Firing, boolean side1Firing) {
        int totalThrust = thrust0 + thrust1;
        if (totalThrust == 0) return current;
        
        int index = Arrays.asList(values()).indexOf(current);
        if (side0Firing != side1Firing) {
            // One side is trying to change range
            int delta = thrust0 > thrust1 ? 1 : -1;
            index = Math.max(0, Math.min(values().length - 1, index + delta));
        }
        return values()[index];
    }
}
