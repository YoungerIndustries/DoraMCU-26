package com.yisy.modules.doramcu.starwars.model;

import java.util.List;
import java.util.UUID;

/**
 * Represents a special ability or Force power in Star Wars.
 * Supports Force Powers (Light/Dark Side), Leader Abilities, and Unit Keywords.
 */
public record SpecialAbility(
    String id,
    String name,
    String category,
    String side,
    String description,
    String cost,
    String effect,
    String rank,
    List<String> users,
    String type
) {
    public SpecialAbility {
        if (id == null || id.isBlank())
            throw new IllegalArgumentException("Ability id must not be blank");
        if (name == null || name.isBlank())
            throw new IllegalArgumentException("Ability name must not be blank");
        if (category == null || category.isBlank())
            category = "Force Power";
        if (side == null) side = "";
        if (description == null) description = "";
        if (cost == null) cost = "";
        if (effect == null) effect = "";
        if (rank == null) rank = "";
        if (users == null) users = List.of();
        if (type == null) type = "";
    }

    public boolean isLightSide() {
        return "Light Side".equalsIgnoreCase(side);
    }

    public boolean isDarkSide() {
        return "Dark Side".equalsIgnoreCase(side);
    }

    /**
     * Returns true if this is a Force Power (Light or Dark Side).
     */
    public boolean isForcePower() {
        return "Force Power".equalsIgnoreCase(category);
    }

    /**
     * Returns true if this is a Leader Ability.
     */
    public boolean isLeaderAbility() {
        return "Leader Ability".equalsIgnoreCase(category);
    }

    /**
     * Returns true if this is a Unit Keyword.
     */
    public boolean isUnitKeyword() {
        return "Keyword".equalsIgnoreCase(category);
    }
}
