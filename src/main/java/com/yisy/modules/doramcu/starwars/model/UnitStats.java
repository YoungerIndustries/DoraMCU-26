package com.yisy.modules.doramcu.starwars.model;

/**
 * Statistics for a Star Wars unit.
 */
public record UnitStats(
    int attack,
    int defense,
    int hull,
    int shields,
    int speed,
    Integer wounds,
    Integer agility,
    Integer forcePower
) {
}
