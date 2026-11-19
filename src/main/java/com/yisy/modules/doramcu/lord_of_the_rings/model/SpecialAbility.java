package com.yisy.modules.doramcu.lord_of_the_rings.model;

import java.util.List;

/**
 * Represents a special ability or Ring power in Lord of the Rings.
 * Supports Ring Powers, Wizard Abilities, Racial Traits, and Unit Keywords.
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
            category = "Ring Power";
        if (side == null) side = "";
        if (description == null) description = "";
        if (cost == null) cost = "";
        if (effect == null) effect = "";
        if (rank == null) rank = "";
        if (users == null) users = List.of();
        if (type == null) type = "";
    }

    public boolean isLightSide() {
        return "Free Peoples".equalsIgnoreCase(side) || "Light".equalsIgnoreCase(side);
    }

    public boolean isDarkSide() {
        return "Forces of Darkness".equalsIgnoreCase(side) || "Dark".equalsIgnoreCase(side);
    }

    /**
     * Returns true if this is a Ring Power.
     */
    public boolean isRingPower() {
        return "Ring Power".equalsIgnoreCase(category);
    }

    /**
     * Returns true if this is a Wizard Ability.
     */
    public boolean isWizardAbility() {
        return "Wizard Ability".equalsIgnoreCase(category);
    }

    /**
     * Returns true if this is a Racial Trait.
     */
    public boolean isRacialTrait() {
        return "Racial Trait".equalsIgnoreCase(category);
    }

    /**
     * Returns true if this is a Unit Keyword.
     */
    public boolean isUnitKeyword() {
        return "Keyword".equalsIgnoreCase(category);
    }
}
