package com.yisy.modules.doramcu.starshiptroopers.model;

import java.util.List;

/**
 * Represents a unit in the Starship Troopers board game.
 */
public record Unit(
    String id,
    String name,
    String type,
    int attackStrength,
    int defenseStrength,
    int movementAllowance,
    String description,
    List<String> abilities,
    Integer victoryPoints,
    Integer countersPerScenario,
    Integer range,
    List<String> equipment,
    List<UnitVariant> variants,
    String special
) {
    public Unit {
        if (id == null || id.isBlank())
            throw new IllegalArgumentException("Unit id must not be blank");
        if (name == null || name.isBlank())
            throw new IllegalArgumentException("Unit name must not be blank");
        if (attackStrength < 0)
            throw new IllegalArgumentException("Attack strength must be non-negative");
        if (defenseStrength < 0)
            throw new IllegalArgumentException("Defense strength must be non-negative");
        if (movementAllowance < 0)
            throw new IllegalArgumentException("Movement allowance must be non-negative");
    }

    public boolean isCombatUnit() {
        return attackStrength > 0;
    }

    public boolean isHeavyWeapon() {
        return "Heavy Weapon".equals(type);
    }

    public boolean isImmobile() {
        return movementAllowance == 0;
    }
}
