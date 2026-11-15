package com.yisy.modules.doramcu.warhammer40k.model;

/**
 * Statistics for a Warhammer 40K unit.
 */
public record UnitStats(
    int attack,
    int defense,
    int hull,
    int wounds,
    Integer ws,
    Integer bs,
    Integer strength,
    Integer toughness,
    Integer armor,
    Integer psyker,
    Integer movement
) {
}
