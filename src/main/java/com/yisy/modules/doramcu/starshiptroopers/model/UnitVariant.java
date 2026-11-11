package com.yisy.modules.doramcu.starshiptroopers.model;

/**
 * Represents a variant of a unit (e.g. Desert Warrior, Heavy Warrior).
 */
public record UnitVariant(
    String name,
    String color,
    String description,
    Integer attackStrength
) {
}
