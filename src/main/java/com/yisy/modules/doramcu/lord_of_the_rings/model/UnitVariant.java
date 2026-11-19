package com.yisy.modules.doramcu.lord_of_the_rings.model;

/**
 * Represents a variant of a Lord of the Rings unit (e.g., Elven Guard, Rohirrim Cavalry).
 */
public record UnitVariant(
    String name,
    String specialty
) {
}
