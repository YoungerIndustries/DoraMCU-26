package com.yisy.modules.doramcu.warhammer40k.model;

import java.util.List;

/**
 * Represents a unit in the Warhammer 40K universe (infantry, vehicle, or character).
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
    List<String> variants
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

    public boolean isVehicle() {
        return "vehicle".equals(type);
    }

    public boolean isInfantry() {
        return "infantry".equals(type);
    }

    public int attackStrength() {
        return stats != null ? stats.attack() : 0;
    }

    public int defenseStrength() {
        return stats != null ? stats.defense() : 0;
    }

    public int totalHp() {
        if (stats == null) return 0;
        return stats.hull() + stats.wounds();
    }

    public int weaponSkill() {
        return stats != null && stats.ws() != null ? stats.ws() : 0;
    }

    public int ballisticSkill() {
        return stats != null && stats.bs() != null ? stats.bs() : 0;
    }

    public int strength() {
        return stats != null && stats.strength() != null ? stats.strength() : 0;
    }

    public int toughness() {
        return stats != null && stats.toughness() != null ? stats.toughness() : 0;
    }

    public int armorSave() {
        return stats != null && stats.armor() != null ? stats.armor() : 0;
    }
}
