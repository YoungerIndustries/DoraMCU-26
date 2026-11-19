package com.yisy.modules.doramcu.lord_of_the_rings.model;

import java.util.List;

/**
 * Represents a Lord of the Rings faction (e.g., Free Peoples Alliance, Mordor).
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
