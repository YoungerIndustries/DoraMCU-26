package com.yisy.modules.doramcu.hex.planet;

import com.yisy.modules.doramcu.hex.Hex3D;
import com.yisy.modules.doramcu.hex.Hex3DMap;
import com.yisy.modules.doramcu.hex.PlanetGeometry;

import java.util.List;
import java.util.Random;

/**
 * Generates biomes for all surface hex cells on a planet.
 * <p>
 * Uses procedural generation based on:
 * <ul>
 *   <li>Latitude — derived from the z-coordinate on the hex sphere (equator to poles)</li>
 *   <li>Altitude — derived from noise functions seeded by planet ID</li>
 *   <li>Moisture — derived from separate noise functions for rainfall patterns</li>
 * </ul>
 * <p>
 * The algorithm produces realistic biome distributions:
 * <ul>
 *   <li>Equatorial regions → tropical biomes (jungle, savanna, rainforest)</li>
 *   <li>Temperate regions → grassland, forest, desert</li>
 *   <li>Polar regions → tundra, ice</li>
 *   <li>High altitude → mountains, highlands</li>
 *   <li>Low altitude near coasts → beaches, shallow water</li>
 * </ul>
 * <p>
 * After generation, each surface hex cell has a {@link TerrainData} payload
 * with the assigned biome.
 */
public class BiomeGenerator {

    private final long seed;
    private final Random random;
    private final double waterCoverage;
    private final double mountainChance;
    private final double desertChance;

    /**
     * Creates a biome generator with the given seed.
     *
     * @param seed the random seed for reproducible generation
     */
    public BiomeGenerator(long seed) {
        this(seed, 0.6, 0.1, 0.15);
    }

    /**
     * Creates a biome generator with custom parameters.
     *
     * @param seed           the random seed
     * @param waterCoverage  fraction of surface covered by water (0.0 to 0.8)
     * @param mountainChance chance of mountain terrain at high altitude
     * @param desertChance   chance of desert in dry temperate zones
     */
    public BiomeGenerator(long seed, double waterCoverage, double mountainChance, double desertChance) {
        if (waterCoverage < 0.0 || waterCoverage > 0.8) {
            throw new IllegalArgumentException("Water coverage must be between 0.0 and 0.8: " + waterCoverage);
        }
        this.seed = seed;
        this.random = new Random(seed);
        this.waterCoverage = waterCoverage;
        this.mountainChance = mountainChance;
        this.desertChance = desertChance;
    }

    /**
     * Generates biomes for all surface hex cells in the given planet geometry.
     * <p>
     * Each surface hex cell will have a {@link TerrainData} payload after this method completes.
     *
     * @param geometry the planet geometry containing surface hex cells
     */
    public void generateBiomes(PlanetGeometry geometry) {
        List<Hex3D> surface = geometry.getSurface();
        int radius = geometry.getRadius();

        for (Hex3D hex : surface) {
            Biome biome = generateBiomeForHex(hex, radius);
            TerrainData terrainData = new TerrainData(biome);
            hex.place(terrainData);
        }
    }

    /**
     * Generates a biome for a single hex cell.
     */
    private Biome generateBiomeForHex(Hex3D hex, int radius) {
        // Calculate latitude: z-coordinate normalized to [-1, 1]
        double latitude = hex.getZ() / (double) radius;
        // Clamp to [-1, 1]
        latitude = Math.max(-1.0, Math.min(1.0, latitude));

        // Generate altitude noise
        double altitudeNoise = noise2D(hex.getX(), hex.getY(), seed);
        int altitude = (int) (altitudeNoise * 5000); // 0 to 5000 meters
        altitude = Math.max(0, altitude);

        // Generate moisture noise
        double moistureNoise = noise2D(hex.getX(), hex.getY(), seed + 1000);
        double moisture = moistureNoise * 0.8 + 0.1; // 0.1 to 0.9

        // Calculate temperature based on latitude
        // Equator = 30°C, poles = -30°C
        double temperature = 30.0 - Math.abs(latitude) * 60.0;
        // Altitude cools temperature: -6°C per 1000m
        temperature -= (altitude / 1000.0) * 6.0;
        // Add small random variation
        temperature += (random.nextDouble() - 0.5) * 5.0;

        // Determine terrain type
        TerrainType terrain = determineTerrainType(latitude, altitude, moisture, altitudeNoise);

        return new Biome(terrain, temperature, moisture, altitude);
    }

    /**
     * Determines the terrain type based on climate factors.
     */
    private TerrainType determineTerrainType(double latitude, int altitude, double moisture, double altitudeNoise) {
        // Water determination
        if (altitudeNoise < (1.0 - waterCoverage) * 0.5 - 0.25) {
            return altitude < 100 ? TerrainType.DEEP_OCEAN : TerrainType.SHALLOW_WATER;
        }

        // Beach at water's edge
        if (altitude < 200 && altitudeNoise > (1.0 - waterCoverage) * 0.5 - 0.25) {
            return TerrainType.BEACH;
        }

        // Mountains at high altitude
        if (altitude > 3000 && random.nextDouble() < mountainChance + 0.3) {
            return TerrainType.MOUNTAIN;
        }
        if (altitude > 2000) {
            return TerrainType.HIGHLAND;
        }

        // Volcanic terrain (rare)
        if (random.nextDouble() < 0.02) {
            return TerrainType.VOLCANIC;
        }

        // Polar regions
        double absLat = Math.abs(latitude);
        if (absLat > 0.8) {
            return moisture > 0.5 ? TerrainType.TUNDRA : TerrainType.ICE;
        }

        // Sub-polar
        if (absLat > 0.6) {
            if (moisture > 0.6) return TerrainType.FOREST;
            if (moisture < 0.3) return TerrainType.TUNDRA;
            return TerrainType.GRASSLAND;
        }

        // Tropical regions (near equator)
        if (absLat < 0.2) {
            if (moisture > 0.7) return TerrainType.JUNGLE;
            if (moisture > 0.4) return TerrainType.FOREST;
            if (moisture < 0.25) return TerrainType.DESERT;
            return TerrainType.SAVANNA;
        }

        // Temperate regions
        if (moisture > 0.65) {
            return TerrainType.FOREST;
        }
        if (moisture > 0.45) {
            return TerrainType.GRASSLAND;
        }
        if (moisture < 0.25 && random.nextDouble() < desertChance + 0.2) {
            return TerrainType.DESERT;
        }
        if (moisture < 0.35) {
            return TerrainType.SAVANNA;
        }
        if (moisture > 0.5 && random.nextDouble() < 0.15) {
            return TerrainType.SWAMP;
        }

        // Default: grassland
        return TerrainType.GRASSLAND;
    }

    /**
     * Simple 2D noise function for procedural generation.
     * Uses a hash-based approach for reproducibility.
     *
     * @param x the x coordinate
     * @param y the y coordinate
     * @param seed the random seed
     * @return a value between 0.0 and 1.0
     */
    private double noise2D(int x, int y, long seed) {
        long n = x * 374761393L + y * 668265263L + seed;
        n = (n ^ (n >>> 13)) * 1274126177L;
        n = n ^ (n >>> 16);
        return (n & 0x7fffffffL) / (double) 0x7fffffffL;
    }

    /** @return the seed used for generation */
    public long getSeed() {
        return seed;
    }
}
