package com.yisy.modules.doramcu.hex;

/**
 * Factory for creating the single galaxy in DoraMCU.
 * <p>
 * Only one galaxy is allowed:
 * <ul>
 *   <li><b>Game Galaxy</b> ("Milky Way") — Full-size galaxy for actual gameplay with default settings.</li>
 * </ul>
 * <p>
 * This factory ensures consistent galaxy configurations and prevents arbitrary galaxy creation.
 */
public class GalaxyFactory {

    /**
     * Name of the game galaxy used for actual gameplay.
     */
    public static final String GAME_GALAXY_NAME = "Milky Way";

    /**
     * Name of the Void galaxy used for Traveller plugin.
     */
    public static final String VOID_GALAXY_NAME = "The Void";

    /**
     * Name of the test galaxy used for model development.
     */
    public static final String TEST_GALAXY_NAME = "Test Galaxy";

    /**
     * Random seed for the test galaxy.
     */
    public static final long TEST_GALAXY_SEED = 12345L;

    /**
     * Random seed for the game galaxy — ensures reproducibility.
     */
    public static final long GAME_GALAXY_SEED = GalaxyGenerator.DEFAULT_SEED;

    /**
     * Random seed for the Void galaxy — ensures reproducibility.
     */
    public static final long VOID_GALAXY_SEED = 42L;

    private GalaxyFactory() {
        // Utility class, no instantiation
    }

    /**
     * Returns the generation configuration for the game galaxy.
     * <p>
     * Uses default settings for a full-size galaxy suitable for gameplay.
     *
     * @return the configuration for the game galaxy
     */
    public static GalaxyGenerationConfig gameGalaxyConfig() {
        return GalaxyGenerationConfig.builder()
                .systemCount(GalaxyGenerationConfig.DEFAULT_SYSTEM_COUNT)
                .planetsPerSystem(GalaxyGenerationConfig.DEFAULT_PLANETS_PER_SYSTEM)
                .moonChance(GalaxyGenerationConfig.DEFAULT_MOON_CHANCE)
                .asteroidBeltChance(GalaxyGenerationConfig.DEFAULT_ASTEROID_BELT_CHANCE)
                .gasGiantChance(GalaxyGenerationConfig.DEFAULT_GAS_GIANT_CHANCE)
                .nebulaChance(GalaxyGenerationConfig.DEFAULT_NEBULA_CHANCE)
                .blackHoleChance(GalaxyGenerationConfig.DEFAULT_BLACK_HOLE_CHANCE)
                .cometChance(GalaxyGenerationConfig.DEFAULT_COMET_CHANCE)
                .spaceStationChance(GalaxyGenerationConfig.DEFAULT_SPACE_STATION_CHANCE)
                .hyperlaneDensity(GalaxyGenerationConfig.DEFAULT_HYPERLANE_DENSITY)
                .build();
    }

    /**
     * Generates the game galaxy in memory.
     *
     * @return the in-memory game galaxy
     */
    public static Galaxy generateGameGalaxy() {
        GalaxyGenerator generator = new GalaxyGenerator(GAME_GALAXY_SEED);
        generator.setConfig(gameGalaxyConfig());
        return generator.generateGalaxy(1, GAME_GALAXY_NAME);
    }

    /**
     * Generates a small test galaxy for unit tests and development.
     * <p>
     * Uses a reduced system count for faster generation.
     *
     * @return the in-memory test galaxy
     */
    public static Galaxy generateTestGalaxy() {
        GalaxyGenerationConfig config = GalaxyGenerationConfig.builder()
                .systemCount(50)
                .planetsPerSystem(2)
                .build();
        GalaxyGenerator generator = new GalaxyGenerator(12345L);
        generator.setConfig(config);
        return generator.generateGalaxy(1, TEST_GALAXY_NAME);
    }

    /**
     * Generates a Void galaxy for Traveller plugin.
     * <p>
     * Uses the VOID template configuration with low civilization density.
     *
     * @return the in-memory Void galaxy
     */
    public static Galaxy generateVoidGalaxy() {
        return generateFromTemplate(VOID_GALAXY_NAME, GalaxyTemplate.VOID, VOID_GALAXY_SEED);
    }

    /**
     * Generates a custom galaxy with the given name, configuration, and seed.
     *
     * @param name the galaxy name (must not be null or empty)
     * @param config the generation configuration
     * @param seed the random seed for reproducibility
     * @return the in-memory galaxy
     * @throws IllegalArgumentException if name is null or empty
     */
    public static Galaxy generateCustomGalaxy(String name, GalaxyGenerationConfig config, long seed) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Galaxy name must not be null or empty");
        }
        GalaxyGenerator generator = new GalaxyGenerator(seed);
        generator.setConfig(config);
        return generator.generateGalaxy(1, name);
    }

    /**
     * Generates a galaxy by name.
     *
     * @param name the galaxy name (must be a predefined name)
     * @return the in-memory galaxy
     * @throws IllegalArgumentException if the name is not recognized
     */
    public static Galaxy generateGalaxyByName(String name) {
        if (GAME_GALAXY_NAME.equals(name)) {
            return generateGameGalaxy();
        } else if (TEST_GALAXY_NAME.equals(name)) {
            return generateTestGalaxy();
        } else if (VOID_GALAXY_NAME.equals(name)) {
            return generateVoidGalaxy();
        } else {
            throw new IllegalArgumentException(
                    "Unknown galaxy: " + name + ". Use generateCustomGalaxy() for custom names.");
        }
    }

    /**
     * Checks if the given name is a valid galaxy name.
     *
     * @param name the name to check
     * @return true if the name is a predefined galaxy name
     */
    public static boolean isValidGalaxyName(String name) {
        return isPredefinedGalaxyName(name);
    }

    /**
     * Checks if the given name is a predefined galaxy name.
     *
     * @param name the name to check
     * @return true if the name matches a predefined galaxy (Milky Way, Test Galaxy, or The Void)
     */
    public static boolean isPredefinedGalaxyName(String name) {
        return GAME_GALAXY_NAME.equals(name) ||
               TEST_GALAXY_NAME.equals(name) ||
               VOID_GALAXY_NAME.equals(name);
    }

    /**
     * Generates a galaxy from a template with a custom name and seed.
     *
     * @param name the galaxy name
     * @param template the galaxy template
     * @param seed the random seed
     * @return the in-memory galaxy
     */
    public static Galaxy generateFromTemplate(String name, GalaxyTemplate template, long seed) {
        GalaxyGenerationConfig config = template.getConfig();
        GalaxyGenerator generator = new GalaxyGenerator(seed);
        generator.setConfig(config);
        return generator.generateGalaxy(1, name);
    }

    /**
     * Returns the default galaxy generation config.
     */
    public static GalaxyGenerationConfig defaultConfig() {
        return gameGalaxyConfig();
    }

    /**
     * Returns the test galaxy generation config with reduced system count.
     */
    public static GalaxyGenerationConfig testGalaxyConfig() {
        return GalaxyGenerationConfig.builder()
                .systemCount(50)
                .planetsPerSystem(2)
                .build();
    }
}
