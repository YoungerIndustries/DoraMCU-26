package com.yisy.modules.doramcu.lord_of_the_rings.model;

/**
 * Statistics for a Lord of the Rings unit.
 */
public record UnitStats(
    int attack,
    int defense,
    int hull,
    int shields,
    int speed,
    Integer wounds,
    Integer agility,
    Integer magicPower
) {
}
