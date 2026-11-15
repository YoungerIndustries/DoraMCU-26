package com.yisy.modules.doramcu.thirdreich.domain;

import com.yisy.modules.doramcu.earth2026.model.BiosphereContext;

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
    private BiosphereContext biosphereContext;

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

    /** Returns the biosphere context, or null if not set. */
    public BiosphereContext getBiosphereContext() {
        return biosphereContext;
    }

    /** Sets the biosphere context. */
    public void setBiosphereContext(BiosphereContext biosphereContext) {
        this.biosphereContext = biosphereContext;
    }

    // ---- Calculations ----

    /**
     * Returns the total defense bonus for defending units in this terrain.
     * Combines terrain defense bonus with weather, visibility, and biosphere modifiers.
     */
    public int getTotalDefenseBonus() {
        int bonus = terrainType.getDefenseBonus() + weatherModifier + visibilityModifier;
        if (biosphereContext != null) {
            bonus += biosphereContext.getDefenseModifier();
        }
        return bonus;
    }

    /**
     * Returns the movement penalty for units moving through this terrain.
     * Includes biosphere movement effects if available.
     */
    public int getMovementPenalty() {
        int penalty = terrainType.getMovementPenalty() + Math.abs(weatherModifier);
        if (biosphereContext != null) {
            penalty -= biosphereContext.getMovementModifier(); // negative modifier = penalty
        }
        return penalty;
    }

    /**
     * Returns the ranged attack modifier from biosphere effects.
     */
    public int getRangedAttackModifier() {
        return biosphereContext != null ? biosphereContext.getRangedAttackModifier() : 0;
    }

    @Override
    public String toString() {
        String planet = planetName != null ? " on " + planetName : "";
        return String.format("Terrain{%s%s, weather=%d, visibility=%d}",
                terrainType, planet, weatherModifier, visibilityModifier);
    }
}
