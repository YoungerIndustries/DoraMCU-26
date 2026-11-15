package com.yisy.modules.doramcu.starwars.model;

/**
 * Represents a variant of a Star Wars unit (e.g., Clone Sharpshooter, Snowtrooper).
 */
public record UnitVariant(
    String name,
    String specialty
) {
}
