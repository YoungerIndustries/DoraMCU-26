package com.yisy.modules.doramcu.starwars.model;

import java.util.List;

/**
 * Represents a unit in the Star Wars universe (ground unit, starship, vehicle, or character).
 */
public record Unit(
    String id,
    String name,
    String faction,
    String type,
    String era,
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
        return "starfighter".equals(type) || "freighter".equals(type)
            || "capital_ship".equals(type) || "space_station".equals(type);
    }

    public boolean isCharacter() {
        return "commander".equals(type);
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
