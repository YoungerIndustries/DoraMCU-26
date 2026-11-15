package com.yisy.modules.doramcu.starwars.model;

import java.util.List;
import java.util.Map;

/**
 * Represents a Star Wars faction (e.g., Galactic Empire, Rebel Alliance).
 */
public record Faction(
    String id,
    String name,
    String era,
    String ideology,
    String description,
    List<MilitaryBranch> militaryBranches,
    List<FactionLeader> keyLeaders,
    List<String> strengths,
    List<String> weaknesses,
    String homeWorld,
    List<String> colors,
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
