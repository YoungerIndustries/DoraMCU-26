package com.yisy.modules.doramcu.starshiptroopers.model;

/**
 * Represents the victory conditions for a scenario.
 */
public record VictoryConditions(
    String terranVictory,
    String arachnidVictory
) {
    public VictoryConditions {
        if (terranVictory == null || terranVictory.isBlank())
            throw new IllegalArgumentException("Terran victory condition must not be blank");
        if (arachnidVictory == null || arachnidVictory.isBlank())
            throw new IllegalArgumentException("Arachnid victory condition must not be blank");
    }
}
