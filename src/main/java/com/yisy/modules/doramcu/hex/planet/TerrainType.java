package com.yisy.modules.doramcu.hex.planet;

/**
 * Represents the terrain type of a single hex cell on a planet surface.
 * <p>
 * Inspired by OpenTTD's terrain type system, each terrain type defines:
 * <ul>
 *   <li>A base color for rendering (RGB hex)</li>
 *   <li>Whether the terrain is passable by land vehicles</li>
 *   <li>Whether the terrain is navigable by water vessels</li>
 *   <li>Whether settlements can be built on this terrain</li>
 *   <li>Base movement cost for vehicles traversing this terrain</li>
 * </ul>
 * <p>
 * Terrain types are determined by biome generation algorithms that consider
 * altitude, latitude, and moisture — see {@link BiomeGenerator}.
 */
public enum TerrainType {

    /** Deep ocean — not passable by land, navigable by ships */
    DEEP_OCEAN("#1a3a5c", false, true, false, 1.0),

    /** Shallow water — not passable by land, navigable by ships */
    SHALLOW_WATER("#2e6b9e", false, true, false, 1.0),

    /** Beach/sand — passable by land, no settlements */
    BEACH("#d4c5a0", true, false, false, 1.2),

    /** Grassland — primary settlement terrain */
    GRASSLAND("#4a8c3f", true, false, true, 1.0),

    /** Forest — passable, slower movement, settlements possible */
    FOREST("#2d5a1e", true, false, true, 1.5),

    /** Desert — passable, settlements possible, low fertility */
    DESERT("#c2a661", true, false, true, 1.3),

    /** Tundra — cold grassland, settlements possible */
    TUNDRA("#8a9ba8", true, false, true, 1.4),

    /** Ice/snow — very cold, limited settlement */
    ICE("#d8e8f0", true, false, false, 1.6),

    /** Mountain — blocked for most vehicles, no settlements */
    MOUNTAIN("#6b6b6b", false, false, false, 0.0),

    /** Highland — elevated grassland, slower movement */
    HIGHLAND("#7a7a5a", true, false, true, 1.8),

    /** Swamp — slow movement, no settlements */
    SWAMP("#4a5a3a", true, false, false, 2.0),

    /** Volcanic terrain — dangerous, no settlements */
    VOLCANIC("#5a2a1a", false, false, false, 0.0),

    /** Jungle — dense vegetation, very slow movement */
    JUNGLE("#1a4a0a", true, false, false, 2.2),

    /** Savanna — warm grassland, good for settlements */
    SAVANNA("#8a7a3a", true, false, true, 1.1),

    /** Wasteland — barren, no settlements */
    WASTELAND("#5a5a4a", true, false, false, 1.5);

    private final String color;
    private final boolean passableByLand;
    private final boolean navigableByWater;
    private final boolean settlementAllowed;
    private final double movementCost;

    TerrainType(String color, boolean passableByLand, boolean navigableByWater,
                boolean settlementAllowed, double movementCost) {
        this.color = color;
        this.passableByLand = passableByLand;
        this.navigableByWater = navigableByWater;
        this.settlementAllowed = settlementAllowed;
        this.movementCost = movementCost;
    }

    /** @return the RGB hex color for rendering (e.g., "#4a8c3f") */
    public String getColor() {
        return color;
    }

    /** @return true if land vehicles can traverse this terrain */
    public boolean isPassableByLand() {
        return passableByLand;
    }

    /** @return true if water vessels can navigate this terrain */
    public boolean isNavigableByWater() {
        return navigableByWater;
    }

    /** @return true if settlements can be built on this terrain */
    public boolean isSettlementAllowed() {
        return settlementAllowed;
    }

    /** @return the base movement cost multiplier for vehicles */
    public double getMovementCost() {
        return movementCost;
    }

    /** @return true if this terrain is a water type (deep or shallow ocean) */
    public boolean isWater() {
        return this == DEEP_OCEAN || this == SHALLOW_WATER;
    }

    /** @return true if this terrain is a land type (not water) */
    public boolean isLand() {
        return !isWater();
    }

    /** @return true if this terrain is a frozen type (ice or tundra) */
    public boolean isFrozen() {
        return this == ICE || this == TUNDRA;
    }

    /** @return true if this terrain is a forest type (forest or jungle) */
    public boolean isForest() {
        return this == FOREST || this == JUNGLE;
    }

    /** @return true if this terrain is a mountain type (mountain or highland) */
    public boolean isMountain() {
        return this == MOUNTAIN || this == HIGHLAND;
    }
}
