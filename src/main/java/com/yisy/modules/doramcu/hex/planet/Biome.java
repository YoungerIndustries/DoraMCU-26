package com.yisy.modules.doramcu.hex.planet;

import java.util.Objects;

/**
 * Represents a biome on a planet surface — a combination of terrain type and climate.
 * <p>
 * A biome determines what can grow, what resources are available, and how settlements
 * develop on a hex cell. It combines:
 * <ul>
 *   <li>{@link TerrainType} — the physical terrain (ocean, grassland, desert, etc.)</li>
 *   <li>Temperature — from -50°C (polar) to +50°C (equatorial)</li>
 *   <li>Moisture — from 0% (arid) to 100% (humid)</li>
 *   <li>Altitude — elevation above sea level in meters</li>
 * </ul>
 * <p>
 * Biomes are generated procedurally by {@link BiomeGenerator} using noise functions
 * based on latitude, longitude, and altitude on the planet's hex sphere.
 */
public class Biome {

    private final TerrainType terrainType;
    private final double temperature;
    private final double moisture;
    private final int altitude;
    private final String name;

    /**
     * Creates a biome with the given parameters.
     *
     * @param terrainType the physical terrain type
     * @param temperature temperature in Celsius (-50 to +50)
     * @param moisture moisture level (0.0 to 1.0)
     * @param altitude elevation above sea level in meters
     */
    public Biome(TerrainType terrainType, double temperature, double moisture, int altitude) {
        if (terrainType == null) {
            throw new IllegalArgumentException("Terrain type must not be null");
        }
        if (temperature < -50 || temperature > 50) {
            throw new IllegalArgumentException("Temperature must be between -50 and +50: " + temperature);
        }
        if (moisture < 0.0 || moisture > 1.0) {
            throw new IllegalArgumentException("Moisture must be between 0.0 and 1.0: " + moisture);
        }
        if (altitude < 0) {
            throw new IllegalArgumentException("Altitude must be non-negative: " + altitude);
        }
        this.terrainType = terrainType;
        this.temperature = temperature;
        this.moisture = moisture;
        this.altitude = altitude;
        this.name = generateName(terrainType, temperature, moisture, altitude);
    }

    /**
     * Generates a human-readable biome name based on terrain and climate.
     */
    private static String generateName(TerrainType terrain, double temperature, double moisture, int altitude) {
        if (terrain.isWater()) {
            return temperature < 0 ? "Frozen Ocean" : "Ocean";
        }
        if (terrain == TerrainType.DESERT) {
            return temperature > 30 ? "Hot Desert" : "Cold Desert";
        }
        if (terrain == TerrainType.GRASSLAND) {
            if (temperature > 25 && moisture > 0.6) return "Tropical Grassland";
            if (temperature > 20) return "Warm Grassland";
            if (temperature < 5) return "Cold Steppe";
            return "Temperate Grassland";
        }
        if (terrain == TerrainType.FOREST) {
            if (temperature > 25 && moisture > 0.7) return "Tropical Rainforest";
            if (temperature > 15) return "Temperate Forest";
            if (temperature < 0) return "Boreal Forest";
            return "Mixed Forest";
        }
        if (terrain == TerrainType.TUNDRA) {
            return "Tundra";
        }
        if (terrain == TerrainType.ICE) {
            return "Ice Sheet";
        }
        if (terrain == TerrainType.MOUNTAIN) {
            return altitude > 4000 ? "High Mountain" : "Mountain";
        }
        if (terrain == TerrainType.JUNGLE) {
            return "Jungle";
        }
        if (terrain == TerrainType.SAVANNA) {
            return "Savanna";
        }
        if (terrain == TerrainType.SWAMP) {
            return temperature > 20 ? "Tropical Swamp" : "Swamp";
        }
        return terrain.name().replace('_', ' ');
    }

    /** @return the terrain type */
    public TerrainType getTerrainType() {
        return terrainType;
    }

    /** @return the temperature in Celsius */
    public double getTemperature() {
        return temperature;
    }

    /** @return the moisture level (0.0 to 1.0) */
    public double getMoisture() {
        return moisture;
    }

    /** @return the altitude in meters above sea level */
    public int getAltitude() {
        return altitude;
    }

    /** @return a human-readable biome name */
    public String getName() {
        return name;
    }

    /**
     * @return the fertility of this biome for agriculture (0.0 to 1.0)
     * Higher fertility means better crop yields for settlements.
     */
    public double getFertility() {
        double base = 0.5;
        // Moisture increases fertility
        base += moisture * 0.3;
        // Temperature: optimal around 15-25°C
        if (temperature >= 15 && temperature <= 25) {
            base += 0.2;
        } else if (temperature >= 5 && temperature <= 35) {
            base += 0.1;
        }
        // Altitude decreases fertility slightly
        base -= (altitude / 10000.0) * 0.2;
        // Terrain-specific modifiers
        switch (terrainType) {
            case GRASSLAND: base += 0.1; break;
            case SAVANNA: base += 0.05; break;
            case FOREST: base += 0.05; break;
            case DESERT: base -= 0.3; break;
            case TUNDRA: base -= 0.2; break;
            case ICE: base -= 0.4; break;
            case MOUNTAIN: base -= 0.3; break;
            case SWAMP: base -= 0.1; break;
            default: break;
        }
        return Math.max(0.0, Math.min(1.0, base));
    }

    /**
     * @return the resource richness of this biome (0.0 to 1.0)
     * Higher richness means more minerals and strategic resources.
     */
    public double getResourceRichness() {
        double base = 0.3;
        // Mountains are rich in minerals
        if (terrainType == TerrainType.MOUNTAIN) base += 0.4;
        if (terrainType == TerrainType.HIGHLAND) base += 0.2;
        // Volcanic areas have rare resources
        if (terrainType == TerrainType.VOLCANIC) base += 0.5;
        // Deserts sometimes have oil/gas
        if (terrainType == TerrainType.DESERT) base += 0.1;
        // Water has fish resources
        if (terrainType.isWater()) base += 0.15;
        return Math.max(0.0, Math.min(1.0, base));
    }

    /**
     * @return the habitability score for settlements (0.0 to 1.0)
     * Higher scores mean larger, faster-growing settlements.
     */
    public double getHabitability() {
        if (!terrainType.isSettlementAllowed()) return 0.0;
        double base = 0.5;
        // Optimal temperature
        if (temperature >= 10 && temperature <= 30) base += 0.2;
        else if (temperature >= 0 && temperature <= 35) base += 0.1;
        // Moderate moisture is best
        if (moisture >= 0.3 && moisture <= 0.7) base += 0.15;
        // Low altitude is better
        if (altitude < 500) base += 0.1;
        else if (altitude > 2000) base -= 0.1;
        return Math.max(0.0, Math.min(1.0, base));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Biome biome = (Biome) o;
        return terrainType == biome.terrainType &&
                Double.compare(biome.temperature, temperature) == 0 &&
                Double.compare(biome.moisture, moisture) == 0 &&
                altitude == biome.altitude;
    }

    @Override
    public int hashCode() {
        return Objects.hash(terrainType, temperature, moisture, altitude);
    }

    @Override
    public String toString() {
        return String.format("Biome{%s, temp=%.1f°C, moisture=%.0f%%, altitude=%dm}",
                name, temperature, moisture * 100, altitude);
    }
}
