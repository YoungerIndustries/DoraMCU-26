package com.yisy.modules.doramcu.hex;

/**
 * Predefined galaxy templates that provide starting configurations for galaxy generation.
 * <p>
 * Each template encapsulates a complete set of generation parameters including
 * celestial body probabilities, sector dimensions, naming conventions, and trade
 * type distributions. Templates serve as the foundation for creating thematically
 * distinct galaxies.
 * </p>
 * <p>
 * Templates are immutable and thread-safe. Use {@link #getConfig()} to obtain the
 * configuration, then customize it via {@link GalaxyGenerationConfig.Builder} if needed.
 * </p>
 */
public enum GalaxyTemplate {

    /**
     * Classic Traveller galaxy: 10×10 grid, Greek sector naming, full trade type set.
     * Balanced celestial body distribution with moderate hyperlane density.
     * Ideal for traditional Traveller-style campaigns.
     */
    TRAVELLER_CLASSIC(
            "Traveller Classic",
            "A traditional Traveller-style galaxy with Greek sector naming and balanced generation.",
            GalaxyGenerationConfig.builder()
                    .sectorSize(10)
                    .subSectorDimension(10)
                    .civilizationDensity(0.3)
                    .resourceDistribution(0.5)
                    .galaxyShape("spiral")
                    .namingConvention("astronomical")
                    .empireCount(4)
                    .difficultyLevel("medium")
                    .gameType("DoraT")
                    .build()
    ),

    /**
     * Star Trek-style galaxy: larger grid, Federation-style naming, lower civilization density.
     * Fewer gas giants and black holes, more space stations. Lower hyperlane density for
     * longer travel times between systems.
     */
    STAR_TREK(
            "Star Trek",
            "A Star Trek-inspired galaxy with Federation-style naming and sparse civilization.",
            GalaxyGenerationConfig.builder()
                    .sectorSize(12)
                    .subSectorDimension(10)
                    .civilizationDensity(0.15)
                    .resourceDistribution(0.3)
                    .galaxyShape("spiral")
                    .namingConvention("mythological")
                    .empireCount(3)
                    .difficultyLevel("easy")
                    .gameType("Captain's Log")
                    .moonChance(0.5)
                    .spaceStationChance(0.2)
                    .gasGiantChance(0.2)
                    .blackHoleChance(0.02)
                    .hyperlaneDensity(0.7)
                    .build()
    ),

    /**
     * High-density frontier galaxy: small grid, high civilization density, rich resources.
     * Many systems per sector with abundant strategic resources. Higher hyperlane density
     * for well-connected trade routes. Suitable for trade-focused campaigns.
     */
    FRONTIER(
            "Frontier",
            "A dense frontier galaxy with abundant resources and well-connected trade routes.",
            GalaxyGenerationConfig.builder()
                    .sectorSize(8)
                    .subSectorDimension(10)
                    .civilizationDensity(0.6)
                    .resourceDistribution(0.8)
                    .galaxyShape("elliptical")
                    .namingConvention("star")
                    .empireCount(6)
                    .difficultyLevel("hard")
                    .gameType("DoraT")
                    .asteroidBeltChance(0.7)
                    .spaceStationChance(0.15)
                    .hyperlaneDensity(1.5)
                    .build()
    ),

    /**
     * Void galaxy: large grid, very low civilization density, dangerous environments.
     * Few inhabited systems, many black holes and nebulae. Low hyperlane density makes
     * travel risky. Suitable for horror or survival campaigns.
     */
    VOID(
            "Void",
            "A dangerous, sparsely populated galaxy with hazardous environments.",
            GalaxyGenerationConfig.builder()
                    .sectorSize(14)
                    .subSectorDimension(10)
                    .civilizationDensity(0.05)
                    .resourceDistribution(0.2)
                    .galaxyShape("irregular")
                    .namingConvention("mythological")
                    .empireCount(2)
                    .difficultyLevel("hard")
                    .gameType("DoraT")
                    .nebulaChance(0.3)
                    .blackHoleChance(0.15)
                    .gasGiantChance(0.5)
                    .hyperlaneDensity(0.5)
                    .build()
    ),

    /**
     * Babylon 5-inspired galaxy: 10×10 grid, 13 race-based empires, mythological naming.
     * Spiral galaxy shape with moderate civilization density and space stations for
     * Babylon stations. Suitable for Babylon 5-themed campaigns with B5 races.
     */
    BABYLON5(
            "Babylon 5",
            "A Babylon 5-inspired galaxy with 13 race-based empires.",
            GalaxyGenerationConfig.builder()
                    .sectorSize(10)
                    .subSectorDimension(10)
                    .civilizationDensity(0.25)
                    .resourceDistribution(0.4)
                    .galaxyShape("spiral")
                    .namingConvention("mythological")
                    .empireCount(13)
                    .difficultyLevel("medium")
                    .gameType("Babylon5")
                    .spaceStationChance(0.15)
                    .hyperlaneDensity(1.0)
                    .build()
    ),

    /**
     * Single System RPG template: a single solar system for tabletop RPG campaigns.
     */
    SINGLE_SYSTEM_RPG(
            "Single System RPG",
            "A single solar system for tabletop RPG campaigns.",
            GalaxyGenerationConfig.builder()
                    .systemCount(1)
                    .planetsPerSystem(8)
                    .moonChance(0.7)
                    .asteroidBeltChance(0.6)
                    .gasGiantChance(0.3)
                    .nebulaChance(0.0)
                    .blackHoleChance(0.0)
                    .cometChance(0.1)
                    .spaceStationChance(0.3)
                    .hyperlaneDensity(0.0)
                    .sectorSize(2)
                    .subSectorDimension(2)
                    .empireCount(1)
                    .difficultyLevel("medium")
                    .gameType("Traveler4x")
                    .galaxyShape("single")
                    .namingConvention("mythological")
                    .build()
    ),

    /**
     * Subsector RPG template: small-scale galaxy for subsector-focused campaigns.
     */
    SUBSECTOR_RPG(
            "Subsector RPG",
            "A small-scale subsector galaxy for focused RPG campaigns.",
            GalaxyGenerationConfig.builder()
                    .systemCount(25)
                    .planetsPerSystem(4)
                    .moonChance(0.5)
                    .asteroidBeltChance(0.5)
                    .hyperlaneDensity(1.2)
                    .sectorSize(2)
                    .subSectorDimension(2)
                    .civilizationDensity(0.5)
                    .resourceDistribution(0.6)
                    .galaxyShape("cluster")
                    .namingConvention("astronomical")
                    .empireCount(2)
                    .difficultyLevel("medium")
                    .gameType("Traveler4x")
                    .campaignTheme("Traveler")
                    .adventureHookCount(10)
                    .npcDensity(0.5)
                    .systemTheme("mixed")
                    .build()
    ),

    /**
     * Custom template: uses all default values. Starting point for fully custom galaxies.
     */
    CUSTOM(
            "Custom",
            "A blank slate using all default generation parameters.",
            GalaxyGenerationConfig.builder().build()
    );

    private final String displayName;
    private final String description;
    private final GalaxyGenerationConfig config;

    /**
     * Optional empire builder for this template. When set, {@link #launchGame(String, long)}
     * will use this builder to populate the galaxy with themed empires.
     */
    private static volatile EmpireBuilder empireBuilder;

    GalaxyTemplate(String displayName, String description, GalaxyGenerationConfig config) {
        this.displayName = displayName;
        this.description = description;
        this.config = config;
    }

    /**
     * Sets the global empire builder used by {@link #launchGame(String, long)}.
     *
     * @param builder the empire builder, or null to use the default
     */
    public static void setEmpireBuilder(EmpireBuilder builder) {
        empireBuilder = builder;
    }

    /**
     * Returns the current global empire builder.
     *
     * @return the empire builder, or null if not set
     */
    public static EmpireBuilder getEmpireBuilder() {
        return empireBuilder;
    }

    /**
     * Launches a new game using this template's configuration.
     * <p>
     * Creates a {@link Game} with a single galaxy generated from this template's config,
     * then populates it with empires using the configured {@link EmpireBuilder}.
     * </p>
     *
     * @param name the game/galaxy name
     * @param seed the random seed for reproducibility
     * @return the created game with galaxy and empires
     */
    public Game launchGame(String name, long seed) {
        Game game = new Game(name);

        // Generate galaxy from this template's config
        Galaxy galaxy = GalaxyFactory.generateFromTemplate(name, this, seed);
        game.addGalaxy(galaxy);

        // Build empires using the configured builder or a default one
        EmpireBuilder builder = empireBuilder;
        if (builder == null) {
            builder = new com.yisy.modules.doramcu.hex.manager.DefaultEmpireBuilder(seed);
        }
        builder.buildEmpires(game, galaxy);

        return game;
    }

    /**
     * @return the human-readable name of this template
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * @return a description of this template's characteristics
     */
    public String getDescription() {
        return description;
    }

    /**
     * @return the generation configuration for this template
     */
    public GalaxyGenerationConfig getConfig() {
        return config;
    }

    /**
     * Creates a customized configuration based on this template.
     * <p>
     * The returned builder is pre-populated with this template's values.
     * Callers can override any parameter before building.
     * </p>
     *
     * @return a builder initialized with this template's configuration
     */
    public GalaxyGenerationConfig.Builder customize() {
        return GalaxyGenerationConfig.builder()
                .systemCount(config.getSystemCount())
                .planetsPerSystem(config.getPlanetsPerSystem())
                .moonChance(config.getMoonChance())
                .asteroidBeltChance(config.getAsteroidBeltChance())
                .gasGiantChance(config.getGasGiantChance())
                .nebulaChance(config.getNebulaChance())
                .blackHoleChance(config.getBlackHoleChance())
                .cometChance(config.getCometChance())
                .spaceStationChance(config.getSpaceStationChance())
                .hyperlaneDensity(config.getHyperlaneDensity())
                .sectorSize(config.getSectorSize())
                .subSectorDimension(config.getSubSectorDimension())
                .civilizationDensity(config.getCivilizationDensity())
                .resourceDistribution(config.getResourceDistribution())
                .galaxyShape(config.getGalaxyShape())
                .namingConvention(config.getNamingConvention())
                .empireCount(config.getEmpireCount())
                .difficultyLevel(config.getDifficultyLevel())
                .gameType(config.getGameType());
    }

    /**
     * Finds a template by its name (case-insensitive).
     *
     * @param name the template name (e.g., "traveller_classic", "Star Trek")
     * @return the matching template
     * @throws IllegalArgumentException if no template matches
     */
    public static GalaxyTemplate findByName(String name) {
        if (name == null) {
            throw new IllegalArgumentException("Template name must not be null");
        }
        for (GalaxyTemplate template : values()) {
            if (template.name().equalsIgnoreCase(name) ||
                template.getDisplayName().equalsIgnoreCase(name)) {
                return template;
            }
        }
        throw new IllegalArgumentException("Unknown galaxy template: " + name +
                ". Available templates: " + java.util.Arrays.toString(values()));
    }

    /**
     * @return all available templates
     */
    public static GalaxyTemplate[] getAllTemplates() {
        return values();
    }
}
