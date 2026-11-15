package com.yisy.modules.doramcu.warhammer40k.model;

import java.util.List;
import java.util.Map;

/**
 * Represents a faction in the Warhammer 40K universe.
 */
public record Faction(
    String id,
    String name,
    String era,
    String ideology,
    String description,
    List<String> militaryBranches,
    List<String> keyLeaders,
    List<String> strengths,
    List<String> weaknesses,
    String homeWorld,
    List<String> allies,
    List<String> enemies
) {
    public Faction {
        if (id == null || id.isBlank())
            throw new IllegalArgumentException("Faction id must not be blank");
        if (name == null || name.isBlank())
            throw new IllegalArgumentException("Faction name must not be blank");
    }
}
