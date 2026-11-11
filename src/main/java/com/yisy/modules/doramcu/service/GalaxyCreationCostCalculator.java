package com.yisy.modules.doramcu.service;

import com.yisy.modules.doramcu.hex.GalaxyGenerationConfig;
import org.springframework.stereotype.Component;

/**
 * Calculates the credit cost to create a new galaxy based on the game module type
 * and galaxy generation parameters.
 * <p>
 * The cost reflects the ships, facilities, and infrastructure required to build
 * a new galaxy. Different game modules have different base costs and scaling factors.
 * </p>
 * <p>
 * Cost components:
 * </p>
 * <ul>
 *   <li><b>Base cost</b> — fixed cost per game module type</li>
 *   <li><b>System count cost</b> — scales with the number of solar systems</li>
 *   <li><b>Planet cost</b> — scales with planets per system</li>
 *   <li><b>Celestial body cost</b> — scales with probability-weighted celestial bodies</li>
 *   <li><b>Facility cost</b> — space stations and industrial facilities</li>
 *   <li><b>Empire cost</b> — AI empires, fleets, and starting facilities</li>
 *   <li><b>Difficulty multiplier</b> — harder galaxies cost more to create</li>
 * </ul>
 */
@Component
public class GalaxyCreationCostCalculator {

    // ============================================================
    // Base costs by game module type (in credits)
    // ============================================================

    /** Base cost for a DoraT (Traveller-style) galaxy — minimal infrastructure. */
    public static final long BASE_COST_DORAT = 50_000_000L;

    /** Base cost for a CaptainsLog (Star Trek-style) galaxy — moderate infrastructure. */
    public static final long BASE_COST_CAPTAINSLOG = 100_000_000L;

    /** Base cost for a Hybrid galaxy — full infrastructure with both systems. */
    public static final long BASE_COST_HYBRID = 200_000_000L;

    // ============================================================
    // Per-unit costs (in credits)
    // ============================================================

    /** Cost per solar system (star, orbital mechanics, positioning). */
    public static final long COST_PER_SYSTEM = 1_000_000L;

    /** Cost per planet (terrain, atmosphere, population, resources). */
    public static final long COST_PER_PLANET = 500_000L;

    /** Cost per moon (orbital body, outpost slots). */
    public static final long COST_PER_MOON = 100_000L;

    /** Cost per asteroid belt (mining yield, station slots). */
    public static final long COST_PER_ASTEROID_BELT = 200_000L;

    /** Cost per gas giant (energy yield, gravitational effects). */
    public static final long COST_PER_GAS_GIANT = 300_000L;

    /** Cost per nebula (elerium yield, sensor jamming). */
    public static final long COST_PER_NEBULA = 400_000L;

    /** Cost per black hole (anti-matter yield, gravitational pull). */
    public static final long COST_PER_BLACK_HOLE = 600_000L;

    /** Cost per comet (resource bonus, transient body). */
    public static final long COST_PER_COMET = 50_000L;

    /** Cost per space station (hull, shields, modules, defenses). */
    public static final long COST_PER_SPACE_STATION = 1_500_000L;

    /** Cost per empire (AI setup, starting fleet, capital facilities). */
    public static final long COST_PER_EMPIRE = 10_000_000L;

    /** Cost per hyperlane connection (warp gate infrastructure). */
    public static final long COST_PER_HYPERLANE = 250_000L;

    // ============================================================
    // Difficulty multipliers
    // ============================================================

    /** Difficulty multiplier for "easy" galaxies. */
    public static final double DIFFICULTY_MULTIPLIER_EASY = 0.75;

    /** Difficulty multiplier for "medium" galaxies. */
    public static final double DIFFICULTY_MULTIPLIER_MEDIUM = 1.0;

    /** Difficulty multiplier for "hard" galaxies. */
    public static final double DIFFICULTY_MULTIPLIER_HARD = 1.5;

    /**
     * Calculates the total credit cost to create a galaxy with the given configuration.
     *
     * @param config the galaxy generation configuration
     * @return the total cost in credits
     */
    public long calculateCost(GalaxyGenerationConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("GalaxyGenerationConfig must not be null");
        }

        // 1. Base cost from game module type
        long baseCost = getBaseCostForGameType(config.getGameType());

        // 2. System count cost
        long systemsCost = config.getSystemCount() * COST_PER_SYSTEM;

        // 3. Planet cost (total planets = systems * planetsPerSystem)
        int totalPlanets = config.getSystemCount() * config.getPlanetsPerSystem();
        long planetsCost = totalPlanets * COST_PER_PLANET;

        // 4. Celestial body costs (probability-weighted)
        long celestialCosts = calculateCelestialBodiesCost(config, totalPlanets);

        // 5. Space station cost
        long stationsCost = calculateSpaceStationsCost(config, totalPlanets);

        // 6. Empire cost (AI empires with fleets and facilities)
        long empiresCost = config.getEmpireCount() * COST_PER_EMPIRE;

        // 7. Hyperlane cost (estimated connections based on density and system count)
        long hyperlanesCost = calculateHyperlanesCost(config);

        // Sum all components
        long totalCost = baseCost + systemsCost + planetsCost + celestialCosts + stationsCost + empiresCost + hyperlanesCost;

        // Apply difficulty multiplier
        double difficultyMultiplier = getDifficultyMultiplier(config.getDifficultyLevel());
        totalCost = (long) Math.ceil(totalCost * difficultyMultiplier);

        return totalCost;
    }

    /**
     * Calculates the estimated cost of celestial bodies based on probabilities.
     */
    private long calculateCelestialBodiesCost(GalaxyGenerationConfig config, int totalPlanets) {
        long cost = 0L;

        // Moons: each planet has a chance of having a moon
        double expectedMoons = totalPlanets * config.getMoonChance();
        cost += (long) Math.ceil(expectedMoons * COST_PER_MOON);

        // Asteroid belts: per-system probability
        double expectedBelts = config.getSystemCount() * config.getAsteroidBeltChance();
        cost += (long) Math.ceil(expectedBelts * COST_PER_ASTEROID_BELT);

        // Gas giants: per-system probability
        double expectedGasGiants = config.getSystemCount() * config.getGasGiantChance();
        cost += (long) Math.ceil(expectedGasGiants * COST_PER_GAS_GIANT);

        // Nebulas: per-system probability
        double expectedNebulas = config.getSystemCount() * config.getNebulaChance();
        cost += (long) Math.ceil(expectedNebulas * COST_PER_NEBULA);

        // Black holes: per-system probability
        double expectedBlackHoles = config.getSystemCount() * config.getBlackHoleChance();
        cost += (long) Math.ceil(expectedBlackHoles * COST_PER_BLACK_HOLE);

        // Comets: per-system probability
        double expectedComets = config.getSystemCount() * config.getCometChance();
        cost += (long) Math.ceil(expectedComets * COST_PER_COMET);

        return cost;
    }

    /**
     * Calculates the estimated cost of space stations.
     */
    private long calculateSpaceStationsCost(GalaxyGenerationConfig config, int totalPlanets) {
        // Space stations are placed per-system with the given probability
        double expectedStations = config.getSystemCount() * config.getSpaceStationChance();
        return (long) Math.ceil(expectedStations * COST_PER_SPACE_STATION);
    }

    /**
     * Calculates the estimated cost of hyperlane connections.
     */
    private long calculateHyperlanesCost(GalaxyGenerationConfig config) {
        // Estimated hyperlane connections: systemCount * hyperlaneDensity
        double expectedConnections = config.getSystemCount() * config.getHyperlaneDensity();
        return (long) Math.ceil(expectedConnections * COST_PER_HYPERLANE);
    }

    /**
     * Returns the base cost for the given game module type.
     */
    public long getBaseCostForGameType(String gameType) {
        if (gameType == null || gameType.isBlank()) {
            return BASE_COST_DORAT;
        }
        switch (gameType.toLowerCase()) {
            case "captainslog":
                return BASE_COST_CAPTAINSLOG;
            case "hybrid":
                return BASE_COST_HYBRID;
            case "dorat":
            default:
                return BASE_COST_DORAT;
        }
    }

    /**
     * Returns the difficulty multiplier for the given difficulty level.
     */
    public double getDifficultyMultiplier(String difficultyLevel) {
        if (difficultyLevel == null || difficultyLevel.isBlank()) {
            return DIFFICULTY_MULTIPLIER_MEDIUM;
        }
        switch (difficultyLevel.toLowerCase()) {
            case "easy":
                return DIFFICULTY_MULTIPLIER_EASY;
            case "hard":
                return DIFFICULTY_MULTIPLIER_HARD;
            case "medium":
            default:
                return DIFFICULTY_MULTIPLIER_MEDIUM;
        }
    }

    /**
     * Returns a detailed cost breakdown for the given configuration.
     *
     * @param config the galaxy generation configuration
     * @return a map of cost component names to their credit values
     */
    public java.util.Map<String, Long> getCostBreakdown(GalaxyGenerationConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("GalaxyGenerationConfig must not be null");
        }

        java.util.Map<String, Long> breakdown = new java.util.LinkedHashMap<>();

        long baseCost = getBaseCostForGameType(config.getGameType());
        breakdown.put("baseCost", baseCost);

        long systemsCost = config.getSystemCount() * COST_PER_SYSTEM;
        breakdown.put("systemsCost", systemsCost);

        int totalPlanets = config.getSystemCount() * config.getPlanetsPerSystem();
        long planetsCost = totalPlanets * COST_PER_PLANET;
        breakdown.put("planetsCost", planetsCost);

        long celestialCosts = calculateCelestialBodiesCost(config, totalPlanets);
        breakdown.put("celestialBodiesCost", celestialCosts);

        long stationsCost = calculateSpaceStationsCost(config, totalPlanets);
        breakdown.put("spaceStationsCost", stationsCost);

        long empiresCost = config.getEmpireCount() * COST_PER_EMPIRE;
        breakdown.put("empiresCost", empiresCost);

        long hyperlanesCost = calculateHyperlanesCost(config);
        breakdown.put("hyperlanesCost", hyperlanesCost);

        double difficultyMultiplier = getDifficultyMultiplier(config.getDifficultyLevel());
        long subtotal = baseCost + systemsCost + planetsCost + celestialCosts + stationsCost + empiresCost + hyperlanesCost;
        breakdown.put("subtotal", subtotal);
        breakdown.put("difficultyMultiplier", (long) (difficultyMultiplier * 100)); // stored as percentage

        long totalCost = (long) Math.ceil(subtotal * difficultyMultiplier);
        breakdown.put("totalCost", totalCost);

        return breakdown;
    }
}
