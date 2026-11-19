package com.yisy.modules.doramcu.lord_of_the_rings.model;

import java.util.List;

/**
 * Represents a unit in the Lord of the Rings universe (ground unit, starship, vehicle, or character).
 */
public record Unit(
    String id,
    String name,
    String faction,
    String type,
    String species,
    String description,
    UnitStats stats,
    List<String> weapons,
    List<String> abilities,
    List<UnitVariant> variants
) {
    public Unit {
        if (id == null || id.isBlank())
            throw new IllegalArgumentException("Unit id must not be blank");
        if (name == null || name.isBlank())
            throw new IllegalArgumentException("Unit name must not be blank");
    }

    public boolean isCombatUnit() {
        return stats != null && stats.attack() > 0;
    }

    public boolean isStarship() {
        return "starship".equals(type);
    }

    public boolean isCharacter() {
        return "character".equals(type) || "commander".equals(type);
    }

    public int attackStrength() {
        return stats != null ? stats.attack() : 0;
    }

    public int defenseStrength() {
        return stats != null ? stats.defense() : 0;
    }

    public int totalHp() {
        if (stats == null) return 0;
        int hp = stats.hull();
        if (stats.wounds() != null) hp += stats.wounds();
        return hp;
    }
}
