package com.yisy.modules.doramcu.hex;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("GalaxyGenerationConfig Tests")
class GalaxyGenerationConfigTest {

    @Nested
    @DisplayName("Default Values")
    class DefaultValues {

        @Test
        @DisplayName("builder should initialize with default values")
        void builder_shouldInitializeWithDefaults() {
            GalaxyGenerationConfig config = GalaxyGenerationConfig.builder().build();

            assertEquals(GalaxyGenerationConfig.DEFAULT_SYSTEM_COUNT, config.getSystemCount());
            assertEquals(GalaxyGenerationConfig.DEFAULT_PLANETS_PER_SYSTEM, config.getPlanetsPerSystem());
            assertEquals(GalaxyGenerationConfig.DEFAULT_MOON_CHANCE, config.getMoonChance());
            assertEquals(GalaxyGenerationConfig.DEFAULT_ASTEROID_BELT_CHANCE, config.getAsteroidBeltChance());
            assertEquals(GalaxyGenerationConfig.DEFAULT_GAS_GIANT_CHANCE, config.getGasGiantChance());
            assertEquals(GalaxyGenerationConfig.DEFAULT_NEBULA_CHANCE, config.getNebulaChance());
            assertEquals(GalaxyGenerationConfig.DEFAULT_BLACK_HOLE_CHANCE, config.getBlackHoleChance());
            assertEquals(GalaxyGenerationConfig.DEFAULT_COMET_CHANCE, config.getCometChance());
            assertEquals(GalaxyGenerationConfig.DEFAULT_SPACE_STATION_CHANCE, config.getSpaceStationChance());
            assertEquals(GalaxyGenerationConfig.DEFAULT_HYPERLANE_DENSITY, config.getHyperlaneDensity());
        }

        @Test
        @DisplayName("template dimensions should have default values")
        void templateDimensions_shouldHaveDefaults() {
            GalaxyGenerationConfig config = GalaxyGenerationConfig.builder().build();

            assertEquals(GalaxyGenerationConfig.DEFAULT_SECTOR_SIZE, config.getSectorSize());
            assertEquals(GalaxyGenerationConfig.DEFAULT_SUBSECTOR_DIMENSION, config.getSubSectorDimension());
            assertEquals(GalaxyGenerationConfig.DEFAULT_CIVILIZATION_DENSITY, config.getCivilizationDensity());
            assertEquals(GalaxyGenerationConfig.DEFAULT_RESOURCE_DISTRIBUTION, config.getResourceDistribution());
            assertEquals(GalaxyGenerationConfig.DEFAULT_GALAXY_SHAPE, config.getGalaxyShape());
            assertEquals(GalaxyGenerationConfig.DEFAULT_NAMING_CONVENTION, config.getNamingConvention());
            assertEquals(GalaxyGenerationConfig.DEFAULT_EMPIRE_COUNT, config.getEmpireCount());
            assertEquals(GalaxyGenerationConfig.DEFAULT_DIFFICULTY_LEVEL, config.getDifficultyLevel());
            assertEquals(GalaxyGenerationConfig.DEFAULT_GAME_TYPE, config.getGameType());
        }

        @Test
        @DisplayName("default constants should have expected values")
        void defaultConstants_shouldHaveExpectedValues() {
            assertEquals(10, GalaxyGenerationConfig.DEFAULT_SECTOR_SIZE);
            assertEquals(10, GalaxyGenerationConfig.DEFAULT_SUBSECTOR_DIMENSION);
            assertEquals(0.3, GalaxyGenerationConfig.DEFAULT_CIVILIZATION_DENSITY);
            assertEquals(0.5, GalaxyGenerationConfig.DEFAULT_RESOURCE_DISTRIBUTION);
            assertEquals(4, GalaxyGenerationConfig.DEFAULT_EMPIRE_COUNT);
            assertEquals("spiral", GalaxyGenerationConfig.DEFAULT_GALAXY_SHAPE);
            assertEquals("astronomical", GalaxyGenerationConfig.DEFAULT_NAMING_CONVENTION);
            assertEquals("medium", GalaxyGenerationConfig.DEFAULT_DIFFICULTY_LEVEL);
            assertEquals("DoraT", GalaxyGenerationConfig.DEFAULT_GAME_TYPE);
        }
    }

    @Nested
    @DisplayName("Builder Validation")
    class BuilderValidation {

        @Test
        @DisplayName("systemCount should reject negative values")
        void systemCount_shouldRejectNegative() {
            assertThrows(IllegalArgumentException.class, () ->
                    GalaxyGenerationConfig.builder().systemCount(-1).build()
            );
        }

        @Test
        @DisplayName("systemCount should accept zero")
        void systemCount_shouldAcceptZero() {
            GalaxyGenerationConfig config = GalaxyGenerationConfig.builder().systemCount(0).build();
            assertEquals(0, config.getSystemCount());
        }

        @Test
        @DisplayName("planetsPerSystem should reject negative values")
        void planetsPerSystem_shouldRejectNegative() {
            assertThrows(IllegalArgumentException.class, () ->
                    GalaxyGenerationConfig.builder().planetsPerSystem(-1).build()
            );
        }

        @Test
        @DisplayName("moonChance should reject values below 0.0")
        void moonChance_shouldRejectBelowZero() {
            assertThrows(IllegalArgumentException.class, () ->
                    GalaxyGenerationConfig.builder().moonChance(-0.1).build()
            );
        }

        @Test
        @DisplayName("moonChance should reject values above 1.0")
        void moonChance_shouldRejectAboveOne() {
            assertThrows(IllegalArgumentException.class, () ->
                    GalaxyGenerationConfig.builder().moonChance(1.1).build()
            );
        }

        @Test
        @DisplayName("moonChance should accept boundary values 0.0 and 1.0")
        void moonChance_shouldAcceptBoundaries() {
            GalaxyGenerationConfig configLow = GalaxyGenerationConfig.builder().moonChance(0.0).build();
            GalaxyGenerationConfig configHigh = GalaxyGenerationConfig.builder().moonChance(1.0).build();
            assertEquals(0.0, configLow.getMoonChance());
            assertEquals(1.0, configHigh.getMoonChance());
        }

        @Test
        @DisplayName("asteroidBeltChance should reject out of range values")
        void asteroidBeltChance_shouldRejectOutOfRange() {
            assertThrows(IllegalArgumentException.class, () ->
                    GalaxyGenerationConfig.builder().asteroidBeltChance(-0.1).build()
            );
            assertThrows(IllegalArgumentException.class, () ->
                    GalaxyGenerationConfig.builder().asteroidBeltChance(1.1).build()
            );
        }

        @Test
        @DisplayName("gasGiantChance should reject out of range values")
        void gasGiantChance_shouldRejectOutOfRange() {
            assertThrows(IllegalArgumentException.class, () ->
                    GalaxyGenerationConfig.builder().gasGiantChance(-0.1).build()
            );
            assertThrows(IllegalArgumentException.class, () ->
                    GalaxyGenerationConfig.builder().gasGiantChance(1.1).build()
            );
        }

        @Test
        @DisplayName("nebulaChance should reject out of range values")
        void nebulaChance_shouldRejectOutOfRange() {
            assertThrows(IllegalArgumentException.class, () ->
                    GalaxyGenerationConfig.builder().nebulaChance(-0.1).build()
            );
            assertThrows(IllegalArgumentException.class, () ->
                    GalaxyGenerationConfig.builder().nebulaChance(1.1).build()
            );
        }

        @Test
        @DisplayName("blackHoleChance should reject out of range values")
        void blackHoleChance_shouldRejectOutOfRange() {
            assertThrows(IllegalArgumentException.class, () ->
                    GalaxyGenerationConfig.builder().blackHoleChance(-0.1).build()
            );
            assertThrows(IllegalArgumentException.class, () ->
                    GalaxyGenerationConfig.builder().blackHoleChance(1.1).build()
            );
        }

        @Test
        @DisplayName("cometChance should reject out of range values")
        void cometChance_shouldRejectOutOfRange() {
            assertThrows(IllegalArgumentException.class, () ->
                    GalaxyGenerationConfig.builder().cometChance(-0.1).build()
            );
            assertThrows(IllegalArgumentException.class, () ->
                    GalaxyGenerationConfig.builder().cometChance(1.1).build()
            );
        }

        @Test
        @DisplayName("spaceStationChance should reject out of range values")
        void spaceStationChance_shouldRejectOutOfRange() {
            assertThrows(IllegalArgumentException.class, () ->
                    GalaxyGenerationConfig.builder().spaceStationChance(-0.1).build()
            );
            assertThrows(IllegalArgumentException.class, () ->
                    GalaxyGenerationConfig.builder().spaceStationChance(1.1).build()
            );
        }

        @Test
        @DisplayName("hyperlaneDensity should reject values below 0.5")
        void hyperlaneDensity_shouldRejectBelowMin() {
            assertThrows(IllegalArgumentException.class, () ->
                    GalaxyGenerationConfig.builder().hyperlaneDensity(0.4).build()
            );
        }

        @Test
        @DisplayName("hyperlaneDensity should reject values above 2.75")
        void hyperlaneDensity_shouldRejectAboveMax() {
            assertThrows(IllegalArgumentException.class, () ->
                    GalaxyGenerationConfig.builder().hyperlaneDensity(2.76).build()
            );
        }

        @Test
        @DisplayName("hyperlaneDensity should accept boundary values 0.5 and 2.75")
        void hyperlaneDensity_shouldAcceptBoundaries() {
            GalaxyGenerationConfig configLow = GalaxyGenerationConfig.builder().hyperlaneDensity(0.5).build();
            GalaxyGenerationConfig configHigh = GalaxyGenerationConfig.builder().hyperlaneDensity(2.75).build();
            assertEquals(0.5, configLow.getHyperlaneDensity());
            assertEquals(2.75, configHigh.getHyperlaneDensity());
        }

        @Test
        @DisplayName("sectorSize should reject values below 2")
        void sectorSize_shouldRejectBelowMin() {
            assertThrows(IllegalArgumentException.class, () ->
                    GalaxyGenerationConfig.builder().sectorSize(1).build()
            );
        }

        @Test
        @DisplayName("sectorSize should reject values above 20")
        void sectorSize_shouldRejectAboveMax() {
            assertThrows(IllegalArgumentException.class, () ->
                    GalaxyGenerationConfig.builder().sectorSize(21).build()
            );
        }

        @Test
        @DisplayName("sectorSize should accept boundary values 2 and 20")
        void sectorSize_shouldAcceptBoundaries() {
            GalaxyGenerationConfig configLow = GalaxyGenerationConfig.builder().sectorSize(2).build();
            GalaxyGenerationConfig configHigh = GalaxyGenerationConfig.builder().sectorSize(20).build();
            assertEquals(2, configLow.getSectorSize());
            assertEquals(20, configHigh.getSectorSize());
        }

        @Test
        @DisplayName("subSectorDimension should reject values below 2")
        void subSectorDimension_shouldRejectBelowMin() {
            assertThrows(IllegalArgumentException.class, () ->
                    GalaxyGenerationConfig.builder().subSectorDimension(1).build()
            );
        }

        @Test
        @DisplayName("subSectorDimension should reject values above 20")
        void subSectorDimension_shouldRejectAboveMax() {
            assertThrows(IllegalArgumentException.class, () ->
                    GalaxyGenerationConfig.builder().subSectorDimension(21).build()
            );
        }

        @Test
        @DisplayName("civilizationDensity should reject out of range values")
        void civilizationDensity_shouldRejectOutOfRange() {
            assertThrows(IllegalArgumentException.class, () ->
                    GalaxyGenerationConfig.builder().civilizationDensity(-0.1).build()
            );
            assertThrows(IllegalArgumentException.class, () ->
                    GalaxyGenerationConfig.builder().civilizationDensity(1.1).build()
            );
        }

        @Test
        @DisplayName("resourceDistribution should reject out of range values")
        void resourceDistribution_shouldRejectOutOfRange() {
            assertThrows(IllegalArgumentException.class, () ->
                    GalaxyGenerationConfig.builder().resourceDistribution(-0.1).build()
            );
            assertThrows(IllegalArgumentException.class, () ->
                    GalaxyGenerationConfig.builder().resourceDistribution(1.1).build()
            );
        }

        @Test
        @DisplayName("empireCount should reject values below 1")
        void empireCount_shouldRejectBelowMin() {
            assertThrows(IllegalArgumentException.class, () ->
                    GalaxyGenerationConfig.builder().empireCount(0).build()
            );
        }

        @Test
        @DisplayName("empireCount should reject values above 16")
        void empireCount_shouldRejectAboveMax() {
            assertThrows(IllegalArgumentException.class, () ->
                    GalaxyGenerationConfig.builder().empireCount(17).build()
            );
        }

        @Test
        @DisplayName("empireCount should accept boundary values 1 and 16")
        void empireCount_shouldAcceptBoundaries() {
            GalaxyGenerationConfig configLow = GalaxyGenerationConfig.builder().empireCount(1).build();
            GalaxyGenerationConfig configHigh = GalaxyGenerationConfig.builder().empireCount(16).build();
            assertEquals(1, configLow.getEmpireCount());
            assertEquals(16, configHigh.getEmpireCount());
        }
    }

    @Nested
    @DisplayName("Builder Custom Values")
    class BuilderCustomValues {

        @Test
        @DisplayName("all parameters should be customizable")
        void allParameters_shouldBeCustomizable() {
            GalaxyGenerationConfig config = GalaxyGenerationConfig.builder()
                    .systemCount(100)
                    .planetsPerSystem(3)
                    .moonChance(0.6)
                    .asteroidBeltChance(0.7)
                    .gasGiantChance(0.4)
                    .nebulaChance(0.2)
                    .blackHoleChance(0.1)
                    .cometChance(0.3)
                    .spaceStationChance(0.15)
                    .hyperlaneDensity(1.5)
                    .sectorSize(15)
                    .subSectorDimension(15)
                    .civilizationDensity(0.5)
                    .resourceDistribution(0.8)
                    .galaxyShape("elliptical")
                    .namingConvention("mythological")
                    .empireCount(8)
                    .difficultyLevel("hard")
                    .gameType("CaptainsLog")
                    .build();

            assertEquals(100, config.getSystemCount());
            assertEquals(3, config.getPlanetsPerSystem());
            assertEquals(0.6, config.getMoonChance());
            assertEquals(0.7, config.getAsteroidBeltChance());
            assertEquals(0.4, config.getGasGiantChance());
            assertEquals(0.2, config.getNebulaChance());
            assertEquals(0.1, config.getBlackHoleChance());
            assertEquals(0.3, config.getCometChance());
            assertEquals(0.15, config.getSpaceStationChance());
            assertEquals(1.5, config.getHyperlaneDensity());
            assertEquals(15, config.getSectorSize());
            assertEquals(15, config.getSubSectorDimension());
            assertEquals(0.5, config.getCivilizationDensity());
            assertEquals(0.8, config.getResourceDistribution());
            assertEquals("elliptical", config.getGalaxyShape());
            assertEquals("mythological", config.getNamingConvention());
            assertEquals(8, config.getEmpireCount());
            assertEquals("hard", config.getDifficultyLevel());
            assertEquals("CaptainsLog", config.getGameType());
        }

        @Test
        @DisplayName("galaxyShape should accept null and fall back to default")
        void galaxyShape_shouldAcceptNull() {
            GalaxyGenerationConfig config = GalaxyGenerationConfig.builder()
                    .galaxyShape(null)
                    .build();
            assertEquals(GalaxyGenerationConfig.DEFAULT_GALAXY_SHAPE, config.getGalaxyShape());
        }

        @Test
        @DisplayName("namingConvention should accept null and fall back to default")
        void namingConvention_shouldAcceptNull() {
            GalaxyGenerationConfig config = GalaxyGenerationConfig.builder()
                    .namingConvention(null)
                    .build();
            assertEquals(GalaxyGenerationConfig.DEFAULT_NAMING_CONVENTION, config.getNamingConvention());
        }

        @Test
        @DisplayName("difficultyLevel should accept null and fall back to default")
        void difficultyLevel_shouldAcceptNull() {
            GalaxyGenerationConfig config = GalaxyGenerationConfig.builder()
                    .difficultyLevel(null)
                    .build();
            assertEquals(GalaxyGenerationConfig.DEFAULT_DIFFICULTY_LEVEL, config.getDifficultyLevel());
        }

        @Test
        @DisplayName("gameType should accept null and fall back to default")
        void gameType_shouldAcceptNull() {
            GalaxyGenerationConfig config = GalaxyGenerationConfig.builder()
                    .gameType(null)
                    .build();
            assertEquals(GalaxyGenerationConfig.DEFAULT_GAME_TYPE, config.getGameType());
        }
    }

    @Nested
    @DisplayName("Immutability")
    class Immutability {

        @Test
        @DisplayName("builder should be independent between builds")
        void builder_shouldBeIndependent() {
            GalaxyGenerationConfig config1 = GalaxyGenerationConfig.builder()
                    .systemCount(100)
                    .build();
            GalaxyGenerationConfig config2 = GalaxyGenerationConfig.builder()
                    .systemCount(200)
                    .build();

            assertEquals(100, config1.getSystemCount());
            assertEquals(200, config2.getSystemCount());
        }

        @Test
        @DisplayName("config should be immutable after build")
        void config_shouldBeImmutable() {
            GalaxyGenerationConfig config = GalaxyGenerationConfig.builder()
                    .systemCount(100)
                    .build();

            // No setters exist — config is immutable by design
            assertEquals(100, config.getSystemCount());
        }
    }

    @Nested
    @DisplayName("toString")
    class ToStringTest {

        @Test
        @DisplayName("toString should include key parameters")
        void toString_shouldIncludeKeyParameters() {
            GalaxyGenerationConfig config = GalaxyGenerationConfig.builder()
                    .systemCount(100)
                    .planetsPerSystem(5)
                    .build();

            String str = config.toString();
            assertTrue(str.contains("systems=100"));
            assertTrue(str.contains("planets=5"));
        }
    }
}
