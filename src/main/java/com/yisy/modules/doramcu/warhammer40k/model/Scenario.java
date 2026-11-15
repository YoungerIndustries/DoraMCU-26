package com.yisy.modules.doramcu.warhammer40k.model;

import java.util.List;
import java.util.Map;

/**
 * Represents a battle scenario in the Warhammer 40K universe.
 */
public record Scenario(
    String id,
    String name,
    String description,
    List<String> factions,
    int difficulty,
    List<String> objectives,
    List<String> terrain,
    Map<String, List<String>> units
) {
    public Scenario {
        if (id == null || id.isBlank())
            throw new IllegalArgumentException("Scenario id must not be blank");
        if (name == null || name.isBlank())
            throw new IllegalArgumentException("Scenario name must not be blank");
    }
}
