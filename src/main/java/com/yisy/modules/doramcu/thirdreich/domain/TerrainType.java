package com.yisy.modules.doramcu.thirdreich.domain;

/**
 * Terrain types for 3R4 ground combat.
 * <p>
 * Terrain affects combat resolution by providing defense bonuses and movement
 * penalties. These modifiers are applied during CRT (Combat Results Table) lookup.
 * </p>
 * <p>
 * Based on 3R4 board game terrain types, adapted for planetary surfaces.
 * </p>
 */
public enum TerrainType {

    /**
     * Open plains. No terrain bonus, normal movement.
     * Favors armor and mechanized units.
     */
    PLAINS("Plains", 0, 0, "Open flat terrain favoring armor"),

    /**
     * Mountainous terrain. High defense bonus, reduced movement.
     * Favors infantry and defensive positions.
     */
    MOUNTAIN("Mountain", 2, -1, "Rugged terrain favoring defense"),

    /**
     * Dense jungle. Moderate defense bonus, reduced movement.
     * Favors infantry, hinders armor.
     */
    JUNGLE("Jungle", 1, -1, "Dense vegetation hindering movement"),

    /**
     * Urban area. High defense bonus, reduced movement.
     * Favors infantry, neutralizes armor advantage.
     */
    URBAN("Urban", 2, -1, "City environment favoring infantry"),

    /**
     * Desert terrain. No terrain bonus, normal movement.
     * Favors mechanized and tank units.
     */
    DESERT("Desert", 0, 0, "Arid open terrain"),

    /**
     * Arctic terrain. Moderate defense bonus, reduced movement.
     * Harsh conditions affect all units.
     */
    ARCTIC("Arctic", 1, -1, "Frozen terrain with limited mobility"),

    /**
     * Forest terrain. Moderate defense bonus, normal movement.
     * Provides cover for infantry.
     */
    FOREST("Forest", 1, 0, "Wooded area providing cover"),

    /**
     * Swamp terrain. High defense bonus, severely reduced movement.
     * Hinders all unit types, especially armor.
     */
    SWAMP("Swamp", 2, -2, "Wetland severely limiting movement");

    private final String displayName;
    private final int defenseBonus;
    private final int movementPenalty;
    private final String description;

    TerrainType(String displayName, int defenseBonus, int movementPenalty, String description) {
        this.displayName = displayName;
        this.defenseBonus = defenseBonus;
        this.movementPenalty = movementPenalty;
        this.description = description;
    }

    /** Returns the display name. */
    public String getDisplayName() {
        return displayName;
    }

    /** Returns the defense bonus for units in this terrain. */
    public int getDefenseBonus() {
        return defenseBonus;
    }

    /** Returns the movement penalty for units in this terrain. */
    public int getMovementPenalty() {
        return movementPenalty;
    }

    /** Returns the description. */
    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
