package com.yisy.modules.doramcu.hex;

/**
 * Configuration for galaxy generation parameters.
 * <p>
 * Extracted from GalaxyGenerator to provide a single value object for all
 * configurable generation parameters like system count, planet count, and
 * celestial body probabilities.
 * </p>
 */
public class GalaxyGenerationConfig {

    public static final int DEFAULT_SYSTEM_COUNT = SectorBuilder.recommendedSystemCount();
    public static final int DEFAULT_PLANETS_PER_SYSTEM = 5;
    public static final double DEFAULT_MOON_CHANCE = 0.4;
    public static final double DEFAULT_ASTEROID_BELT_CHANCE = 0.5;
    public static final double DEFAULT_GAS_GIANT_CHANCE = 0.3;
    public static final double DEFAULT_NEBULA_CHANCE = 0.15;
    public static final double DEFAULT_BLACK_HOLE_CHANCE = 0.05;
    public static final double DEFAULT_COMET_CHANCE = 0.2;
    public static final double DEFAULT_SPACE_STATION_CHANCE = 0.1;
    public static final double DEFAULT_HYPERLANE_DENSITY = 1.0;
    public static final int DEFAULT_SECTOR_SIZE = 10;
    public static final int DEFAULT_SUBSECTOR_DIMENSION = 10;
    public static final double DEFAULT_CIVILIZATION_DENSITY = 0.3;
    public static final double DEFAULT_RESOURCE_DISTRIBUTION = 0.5;
    public static final String DEFAULT_GALAXY_SHAPE = "spiral";
    public static final String DEFAULT_NAMING_CONVENTION = "astronomical";
    public static final int DEFAULT_EMPIRE_COUNT = 4;
    public static final String DEFAULT_DIFFICULTY_LEVEL = "medium";
    public static final String DEFAULT_GAME_TYPE = "DoraT";

    private final int systemCount;
    private final int planetsPerSystem;
    private final double moonChance;
    private final double asteroidBeltChance;
    private final double gasGiantChance;
    private final double nebulaChance;
    private final double blackHoleChance;
    private final double cometChance;
    private final double spaceStationChance;
    private final double hyperlaneDensity;
    private final int sectorSize;
    private final int subSectorDimension;
    private final double civilizationDensity;
    private final double resourceDistribution;
    private final String galaxyShape;
    private final String namingConvention;
    private final int empireCount;
    private final String difficultyLevel;
    private final String gameType;
    private final String campaignTheme;
    private final int adventureHookCount;
    private final double npcDensity;
    private final String systemTheme;
    private final boolean enable4xLayer;

    private GalaxyGenerationConfig(Builder builder) {
        this.systemCount = builder.systemCount;
        this.planetsPerSystem = builder.planetsPerSystem;
        this.moonChance = builder.moonChance;
        this.asteroidBeltChance = builder.asteroidBeltChance;
        this.gasGiantChance = builder.gasGiantChance;
        this.nebulaChance = builder.nebulaChance;
        this.blackHoleChance = builder.blackHoleChance;
        this.cometChance = builder.cometChance;
        this.spaceStationChance = builder.spaceStationChance;
        this.hyperlaneDensity = builder.hyperlaneDensity;
        this.sectorSize = builder.sectorSize;
        this.subSectorDimension = builder.subSectorDimension;
        this.civilizationDensity = builder.civilizationDensity;
        this.resourceDistribution = builder.resourceDistribution;
        this.galaxyShape = builder.galaxyShape;
        this.namingConvention = builder.namingConvention;
        this.empireCount = builder.empireCount;
        this.difficultyLevel = builder.difficultyLevel;
        this.gameType = builder.gameType;
        this.campaignTheme = builder.campaignTheme;
        this.adventureHookCount = builder.adventureHookCount;
        this.npcDensity = builder.npcDensity;
        this.systemTheme = builder.systemTheme;
        this.enable4xLayer = builder.enable4xLayer;
    }

    /**
     * Creates a builder with default values.
     *
     * @return a new builder
     */
    public static Builder builder() {
        return new Builder();
    }

    public int getSystemCount() {
        return systemCount;
    }

    public int getPlanetsPerSystem() {
        return planetsPerSystem;
    }

    public double getMoonChance() {
        return moonChance;
    }

    public double getAsteroidBeltChance() {
        return asteroidBeltChance;
    }

    public double getGasGiantChance() {
        return gasGiantChance;
    }

    public double getNebulaChance() {
        return nebulaChance;
    }

    public double getBlackHoleChance() {
        return blackHoleChance;
    }

    public double getCometChance() {
        return cometChance;
    }

    public double getSpaceStationChance() {
        return spaceStationChance;
    }

    public double getHyperlaneDensity() {
        return hyperlaneDensity;
    }

    public int getSectorSize() { return sectorSize; }
    public int getSubSectorDimension() { return subSectorDimension; }
    public double getCivilizationDensity() { return civilizationDensity; }
    public double getResourceDistribution() { return resourceDistribution; }
    public String getGalaxyShape() { return galaxyShape; }
    public String getNamingConvention() { return namingConvention; }
    public int getEmpireCount() { return empireCount; }
    public String getDifficultyLevel() { return difficultyLevel; }
    public String getGameType() { return gameType; }
    public String getCampaignTheme() { return campaignTheme; }
    public int getAdventureHookCount() { return adventureHookCount; }
    public double getNpcDensity() { return npcDensity; }
    public String getSystemTheme() { return systemTheme; }
    public boolean isEnable4xLayer() { return enable4xLayer; }

    @Override
    public String toString() {
        return String.format("GalaxyGenerationConfig{systems=%d, planets=%d, moon=%.0f%%, asteroid=%.0f%%, gasGiant=%.0f%%, nebula=%.0f%%, blackHole=%.0f%%, comet=%.0f%%, station=%.0f%%, hyperlanes=%.1fx}",
                systemCount, planetsPerSystem,
                moonChance * 100, asteroidBeltChance * 100, gasGiantChance * 100,
                nebulaChance * 100, blackHoleChance * 100, cometChance * 100,
                spaceStationChance * 100, hyperlaneDensity);
    }

    /**
     * Builder for GalaxyGenerationConfig with default values.
     */
    public static class Builder {
        private int systemCount = DEFAULT_SYSTEM_COUNT;
        private int planetsPerSystem = DEFAULT_PLANETS_PER_SYSTEM;
        private double moonChance = DEFAULT_MOON_CHANCE;
        private double asteroidBeltChance = DEFAULT_ASTEROID_BELT_CHANCE;
        private double gasGiantChance = DEFAULT_GAS_GIANT_CHANCE;
        private double nebulaChance = DEFAULT_NEBULA_CHANCE;
        private double blackHoleChance = DEFAULT_BLACK_HOLE_CHANCE;
        private double cometChance = DEFAULT_COMET_CHANCE;
        private double spaceStationChance = DEFAULT_SPACE_STATION_CHANCE;
        private double hyperlaneDensity = DEFAULT_HYPERLANE_DENSITY;
        private int sectorSize = DEFAULT_SECTOR_SIZE;
        private int subSectorDimension = DEFAULT_SUBSECTOR_DIMENSION;
        private double civilizationDensity = DEFAULT_CIVILIZATION_DENSITY;
        private double resourceDistribution = DEFAULT_RESOURCE_DISTRIBUTION;
        private String galaxyShape = DEFAULT_GALAXY_SHAPE;
        private String namingConvention = DEFAULT_NAMING_CONVENTION;
        private int empireCount = DEFAULT_EMPIRE_COUNT;
        private String difficultyLevel = DEFAULT_DIFFICULTY_LEVEL;
        private String gameType = DEFAULT_GAME_TYPE;
        private String campaignTheme;
        private int adventureHookCount;
        private double npcDensity;
        private String systemTheme;
        private boolean enable4xLayer;

        public Builder systemCount(int systemCount) {
            if (systemCount < 0) {
                throw new IllegalArgumentException("System count must be non-negative: " + systemCount);
            }
            this.systemCount = systemCount;
            return this;
        }

        public Builder planetsPerSystem(int planetsPerSystem) {
            if (planetsPerSystem < 0) {
                throw new IllegalArgumentException("Planets per system must be non-negative: " + planetsPerSystem);
            }
            this.planetsPerSystem = planetsPerSystem;
            return this;
        }

        public Builder moonChance(double moonChance) {
            if (moonChance < 0.0 || moonChance > 1.0) {
                throw new IllegalArgumentException("Moon chance must be between 0.0 and 1.0: " + moonChance);
            }
            this.moonChance = moonChance;
            return this;
        }

        public Builder asteroidBeltChance(double chance) {
            if (chance < 0.0 || chance > 1.0) {
                throw new IllegalArgumentException("Asteroid belt chance must be between 0.0 and 1.0: " + chance);
            }
            this.asteroidBeltChance = chance;
            return this;
        }

        public Builder gasGiantChance(double chance) {
            if (chance < 0.0 || chance > 1.0) {
                throw new IllegalArgumentException("Gas giant chance must be between 0.0 and 1.0: " + chance);
            }
            this.gasGiantChance = chance;
            return this;
        }

        public Builder nebulaChance(double chance) {
            if (chance < 0.0 || chance > 1.0) {
                throw new IllegalArgumentException("Nebula chance must be between 0.0 and 1.0: " + chance);
            }
            this.nebulaChance = chance;
            return this;
        }

        public Builder blackHoleChance(double chance) {
            if (chance < 0.0 || chance > 1.0) {
                throw new IllegalArgumentException("Black hole chance must be between 0.0 and 1.0: " + chance);
            }
            this.blackHoleChance = chance;
            return this;
        }

        public Builder cometChance(double chance) {
            if (chance < 0.0 || chance > 1.0) {
                throw new IllegalArgumentException("Comet chance must be between 0.0 and 1.0: " + chance);
            }
            this.cometChance = chance;
            return this;
        }

        public Builder spaceStationChance(double chance) {
            if (chance < 0.0 || chance > 1.0) {
                throw new IllegalArgumentException("Space station chance must be between 0.0 and 1.0: " + chance);
            }
            this.spaceStationChance = chance;
            return this;
        }

        public Builder hyperlaneDensity(double density) {
            if (density < 0.0 || density > 2.75) {
                throw new IllegalArgumentException("Hyperlane density must be between 0.0 and 2.75: " + density);
            }
            this.hyperlaneDensity = density;
            return this;
        }

        public Builder sectorSize(int sectorSize) {
            this.sectorSize = sectorSize;
            return this;
        }

        public Builder subSectorDimension(int subSectorDimension) {
            this.subSectorDimension = subSectorDimension;
            return this;
        }

        public Builder civilizationDensity(double civilizationDensity) {
            this.civilizationDensity = civilizationDensity;
            return this;
        }

        public Builder resourceDistribution(double resourceDistribution) {
            this.resourceDistribution = resourceDistribution;
            return this;
        }

        public Builder galaxyShape(String galaxyShape) {
            this.galaxyShape = galaxyShape;
            return this;
        }

        public Builder namingConvention(String namingConvention) {
            this.namingConvention = namingConvention;
            return this;
        }

        public Builder empireCount(int empireCount) {
            this.empireCount = empireCount;
            return this;
        }

        public Builder difficultyLevel(String difficultyLevel) {
            this.difficultyLevel = difficultyLevel;
            return this;
        }

        public Builder gameType(String gameType) {
            this.gameType = gameType;
            return this;
        }

        public Builder campaignTheme(String campaignTheme) {
            this.campaignTheme = campaignTheme;
            return this;
        }

        public Builder adventureHookCount(int adventureHookCount) {
            this.adventureHookCount = adventureHookCount;
            return this;
        }

        public Builder npcDensity(double npcDensity) {
            this.npcDensity = npcDensity;
            return this;
        }

        public Builder systemTheme(String systemTheme) {
            this.systemTheme = systemTheme;
            return this;
        }

        public Builder enable4xLayer(boolean enable4xLayer) {
            this.enable4xLayer = enable4xLayer;
            return this;
        }

        public GalaxyGenerationConfig build() {
            return new GalaxyGenerationConfig(this);
        }
    }
}
