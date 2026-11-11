package com.yisy.modules.doramcu.thirdreich.domain;

/**
 * Terrain context for a 3R4 ground battle.
 * <p>
 * Encapsulates the terrain type and any additional modifiers that affect
 * combat resolution, such as weather conditions or planetary characteristics.
 * </p>
 */
public class TerrainContext {

    private final TerrainType terrainType;
    private String planetName;
    private int weatherModifier;
    private int visibilityModifier;

    /**
     * Creates a terrain context with the given terrain type.
     *
     * @param terrainType the primary terrain type
     */
    public TerrainContext(TerrainType terrainType) {
        if (terrainType == null) {
            throw new IllegalArgumentException("Terrain type must not be null");
        }
        this.terrainType = terrainType;
        this.weatherModifier = 0;
        this.visibilityModifier = 0;
    }

    /**
     * Creates a terrain context for a specific planet.
     *
     * @param terrainType the primary terrain type
     * @param planetName the name of the planet
     */
    public TerrainContext(TerrainType terrainType, String planetName) {
        this(terrainType);
        this.planetName = planetName;
    }

    // ---- Accessors ----

    /** Returns the terrain type. */
    public TerrainType getTerrainType() {
        return terrainType;
    }

    /** Returns the planet name, or null if not specified. */
    public String getPlanetName() {
        return planetName;
    }

    /** Sets the planet name. */
    public void setPlanetName(String planetName) {
        this.planetName = planetName;
    }

    /** Returns the weather modifier (-2 to +2). */
    public int getWeatherModifier() {
        return weatherModifier;
    }

    /** Sets the weather modifier. */
    public void setWeatherModifier(int weatherModifier) {
        this.weatherModifier = Math.max(-2, Math.min(2, weatherModifier));
    }

    /** Returns the visibility modifier (-2 to +2). */
    public int getVisibilityModifier() {
        return visibilityModifier;
    }

    /** Sets the visibility modifier. */
    public void setVisibilityModifier(int visibilityModifier) {
        this.visibilityModifier = Math.max(-2, Math.min(2, visibilityModifier));
    }

    // ---- Calculations ----

    /**
     * Returns the total defense bonus for defending units in this terrain.
     * Combines terrain defense bonus with weather and visibility modifiers.
     */
    public int getTotalDefenseBonus() {
        return terrainType.getDefenseBonus() + weatherModifier + visibilityModifier;
    }

    /**
     * Returns the movement penalty for units moving through this terrain.
     */
    public int getMovementPenalty() {
        return terrainType.getMovementPenalty() + Math.abs(weatherModifier);
    }

    @Override
    public String toString() {
        String planet = planetName != null ? " on " + planetName : "";
        return String.format("Terrain{%s%s, weather=%d, visibility=%d}",
                terrainType, planet, weatherModifier, visibilityModifier);
    }
}
