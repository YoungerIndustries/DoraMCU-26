package com.yisy.modules.doramcu.starshiptroopers.model;

import java.util.List;
import java.util.Map;

/**
 * Represents a game scenario with forces and victory conditions.
 */
public record Scenario(
    int id,
    String name,
    String description,
    String difficulty,
    int turns,
    List<String> specialRules,
    Map<String, Integer> terranForces,
    Map<String, Integer> arachnidForces,
    Map<String, Integer> humanoidForces,
    VictoryConditions victoryConditions
) {
    public Scenario {
        if (id <= 0)
            throw new IllegalArgumentException("Scenario id must be positive");
        if (name == null || name.isBlank())
            throw new IllegalArgumentException("Scenario name must not be blank");
        if (turns <= 0)
            throw new IllegalArgumentException("Scenario turns must be positive");
    }

    public int totalTerranUnits() {
        if (terranForces == null) return 0;
        return terranForces.values().stream().mapToInt(Integer::intValue).sum();
    }

    public int totalArachnidUnits() {
        if (arachnidForces == null) return 0;
        return arachnidForces.values().stream().mapToInt(Integer::intValue).sum();
    }
}
