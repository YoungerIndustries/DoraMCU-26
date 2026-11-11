package com.yisy.modules.doramcu.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Galaxy JPA Entity Tests")
class GalaxyModelTest {

    @Nested
    @DisplayName("Basic Fields")
    class BasicFields {

        @Test
        @DisplayName("no-arg constructor should initialize defaults")
        void noArgConstructor_shouldInitializeDefaults() {
            Galaxy galaxy = new Galaxy();

            assertNull(galaxy.getId());
            assertNull(galaxy.getName());
            assertEquals(100, galaxy.getTotalSectors());
            assertEquals(0.0, galaxy.getHyperlaneDensity());
        }

        @Test
        @DisplayName("two-arg constructor should set id, name, and hyperlane density")
        void twoArgConstructor_shouldSetFields() {
            Galaxy galaxy = new Galaxy(1L, "Milky Way");

            assertEquals(1L, galaxy.getId());
            assertEquals("Milky Way", galaxy.getName());
            assertEquals(1.0, galaxy.getHyperlaneDensity());
            assertEquals(100, galaxy.getTotalSectors());
        }

        @Test
        @DisplayName("id getter and setter should work")
        void id_getterAndSetter() {
            Galaxy galaxy = new Galaxy();
            galaxy.setId(42L);
            assertEquals(42L, galaxy.getId());
        }

        @Test
        @DisplayName("name getter and setter should work")
        void name_getterAndSetter() {
            Galaxy galaxy = new Galaxy();
            galaxy.setName("Andromeda");
            assertEquals("Andromeda", galaxy.getName());
        }

        @Test
        @DisplayName("totalSectors getter and setter should work")
        void totalSectors_getterAndSetter() {
            Galaxy galaxy = new Galaxy();
            galaxy.setTotalSectors(200);
            assertEquals(200, galaxy.getTotalSectors());
        }

        @Test
        @DisplayName("hyperlaneDensity getter and setter should work")
        void hyperlaneDensity_getterAndSetter() {
            Galaxy galaxy = new Galaxy();
            galaxy.setHyperlaneDensity(1.5);
            assertEquals(1.5, galaxy.getHyperlaneDensity());
        }
    }

    @Nested
    @DisplayName("Generation Parameters")
    class GenerationParameters {

        @Test
        @DisplayName("seed should be null by default")
        void seed_shouldBeNullByDefault() {
            Galaxy galaxy = new Galaxy();
            assertNull(galaxy.getSeed());
        }

        @Test
        @DisplayName("seed getter and setter should work")
        void seed_getterAndSetter() {
            Galaxy galaxy = new Galaxy();
            galaxy.setSeed(12345L);
            assertEquals(12345L, galaxy.getSeed());
        }

        @Test
        @DisplayName("systemCount should be null by default")
        void systemCount_shouldBeNullByDefault() {
            Galaxy galaxy = new Galaxy();
            assertNull(galaxy.getSystemCount());
        }

        @Test
        @DisplayName("systemCount getter and setter should work")
        void systemCount_getterAndSetter() {
            Galaxy galaxy = new Galaxy();
            galaxy.setSystemCount(500);
            assertEquals(500, galaxy.getSystemCount());
        }

        @Test
        @DisplayName("planetsPerSystem should be null by default")
        void planetsPerSystem_shouldBeNullByDefault() {
            Galaxy galaxy = new Galaxy();
            assertNull(galaxy.getPlanetsPerSystem());
        }

        @Test
        @DisplayName("planetsPerSystem getter and setter should work")
        void planetsPerSystem_getterAndSetter() {
            Galaxy galaxy = new Galaxy();
            galaxy.setPlanetsPerSystem(7);
            assertEquals(7, galaxy.getPlanetsPerSystem());
        }

        @Test
        @DisplayName("moonChance should be null by default")
        void moonChance_shouldBeNullByDefault() {
            Galaxy galaxy = new Galaxy();
            assertNull(galaxy.getMoonChance());
        }

        @Test
        @DisplayName("moonChance getter and setter should work")
        void moonChance_getterAndSetter() {
            Galaxy galaxy = new Galaxy();
            galaxy.setMoonChance(0.4);
            assertEquals(0.4, galaxy.getMoonChance());
        }

        @Test
        @DisplayName("asteroidBeltChance getter and setter should work")
        void asteroidBeltChance_getterAndSetter() {
            Galaxy galaxy = new Galaxy();
            galaxy.setAsteroidBeltChance(0.5);
            assertEquals(0.5, galaxy.getAsteroidBeltChance());
        }

        @Test
        @DisplayName("gasGiantChance getter and setter should work")
        void gasGiantChance_getterAndSetter() {
            Galaxy galaxy = new Galaxy();
            galaxy.setGasGiantChance(0.3);
            assertEquals(0.3, galaxy.getGasGiantChance());
        }

        @Test
        @DisplayName("nebulaChance getter and setter should work")
        void nebulaChance_getterAndSetter() {
            Galaxy galaxy = new Galaxy();
            galaxy.setNebulaChance(0.15);
            assertEquals(0.15, galaxy.getNebulaChance());
        }

        @Test
        @DisplayName("blackHoleChance getter and setter should work")
        void blackHoleChance_getterAndSetter() {
            Galaxy galaxy = new Galaxy();
            galaxy.setBlackHoleChance(0.05);
            assertEquals(0.05, galaxy.getBlackHoleChance());
        }

        @Test
        @DisplayName("cometChance getter and setter should work")
        void cometChance_getterAndSetter() {
            Galaxy galaxy = new Galaxy();
            galaxy.setCometChance(0.2);
            assertEquals(0.2, galaxy.getCometChance());
        }

        @Test
        @DisplayName("spaceStationChance getter and setter should work")
        void spaceStationChance_getterAndSetter() {
            Galaxy galaxy = new Galaxy();
            galaxy.setSpaceStationChance(0.1);
            assertEquals(0.1, galaxy.getSpaceStationChance());
        }

        @Test
        @DisplayName("all generation parameters should be settable together")
        void allGenerationParams_shouldBeSettableTogether() {
            Galaxy galaxy = new Galaxy();
            galaxy.setSeed(42L);
            galaxy.setSystemCount(500);
            galaxy.setPlanetsPerSystem(5);
            galaxy.setMoonChance(0.4);
            galaxy.setAsteroidBeltChance(0.5);
            galaxy.setGasGiantChance(0.3);
            galaxy.setNebulaChance(0.15);
            galaxy.setBlackHoleChance(0.05);
            galaxy.setCometChance(0.2);
            galaxy.setSpaceStationChance(0.1);

            assertEquals(42L, galaxy.getSeed());
            assertEquals(500, galaxy.getSystemCount());
            assertEquals(5, galaxy.getPlanetsPerSystem());
            assertEquals(0.4, galaxy.getMoonChance());
            assertEquals(0.5, galaxy.getAsteroidBeltChance());
            assertEquals(0.3, galaxy.getGasGiantChance());
            assertEquals(0.15, galaxy.getNebulaChance());
            assertEquals(0.05, galaxy.getBlackHoleChance());
            assertEquals(0.2, galaxy.getCometChance());
            assertEquals(0.1, galaxy.getSpaceStationChance());
        }
    }

    @Nested
    @DisplayName("Metadata Fields")
    class MetadataFields {

        @Test
        @DisplayName("createdAt should be null by default")
        void createdAt_shouldBeNullByDefault() {
            Galaxy galaxy = new Galaxy();
            assertNull(galaxy.getCreatedAt());
        }

        @Test
        @DisplayName("createdAt getter and setter should work")
        void createdAt_getterAndSetter() {
            Galaxy galaxy = new Galaxy();
            LocalDateTime now = LocalDateTime.of(2025, 1, 15, 10, 30, 0);
            galaxy.setCreatedAt(now);
            assertEquals(now, galaxy.getCreatedAt());
        }

        @Test
        @DisplayName("description should be null by default")
        void description_shouldBeNullByDefault() {
            Galaxy galaxy = new Galaxy();
            assertNull(galaxy.getDescription());
        }

        @Test
        @DisplayName("description getter and setter should work")
        void description_getterAndSetter() {
            Galaxy galaxy = new Galaxy();
            galaxy.setDescription("The nearest spiral galaxy");
            assertEquals("The nearest spiral galaxy", galaxy.getDescription());
        }

        @Test
        @DisplayName("gameType should be null by default")
        void gameType_shouldBeNullByDefault() {
            Galaxy galaxy = new Galaxy();
            assertNull(galaxy.getGameType());
        }

        @Test
        @DisplayName("gameType getter and setter should work")
        void gameType_getterAndSetter() {
            Galaxy galaxy = new Galaxy();
            galaxy.setGameType("DoraT");
            assertEquals("DoraT", galaxy.getGameType());
        }

        @Test
        @DisplayName("difficultyLevel should be null by default")
        void difficultyLevel_shouldBeNullByDefault() {
            Galaxy galaxy = new Galaxy();
            assertNull(galaxy.getDifficultyLevel());
        }

        @Test
        @DisplayName("difficultyLevel getter and setter should work")
        void difficultyLevel_getterAndSetter() {
            Galaxy galaxy = new Galaxy();
            galaxy.setDifficultyLevel("hard");
            assertEquals("hard", galaxy.getDifficultyLevel());
        }

        @Test
        @DisplayName("theme should be null by default")
        void theme_shouldBeNullByDefault() {
            Galaxy galaxy = new Galaxy();
            assertNull(galaxy.getTheme());
        }

        @Test
        @DisplayName("theme getter and setter should work")
        void theme_getterAndSetter() {
            Galaxy galaxy = new Galaxy();
            galaxy.setTheme("Sci-Fi Classic");
            assertEquals("Sci-Fi Classic", galaxy.getTheme());
        }

        @Test
        @DisplayName("all metadata fields should be settable together")
        void allMetadata_shouldBeSettableTogether() {
            Galaxy galaxy = new Galaxy();
            LocalDateTime now = LocalDateTime.of(2025, 6, 1, 12, 0, 0);
            galaxy.setCreatedAt(now);
            galaxy.setDescription("Test galaxy");
            galaxy.setGameType("CaptainsLog");
            galaxy.setDifficultyLevel("easy");
            galaxy.setTheme("Mythological");

            assertEquals(now, galaxy.getCreatedAt());
            assertEquals("Test galaxy", galaxy.getDescription());
            assertEquals("CaptainsLog", galaxy.getGameType());
            assertEquals("easy", galaxy.getDifficultyLevel());
            assertEquals("Mythological", galaxy.getTheme());
        }
    }

    @Nested
    @DisplayName("toString")
    class ToStringTest {

        @Test
        @DisplayName("toString should include id, name, sectors, systems, seed, gameType, difficulty")
        void toString_shouldIncludeAllFields() {
            Galaxy galaxy = new Galaxy(1L, "Milky Way");
            galaxy.setSeed(42L);
            galaxy.setGameType("DoraT");
            galaxy.setDifficultyLevel("medium");

            String str = galaxy.toString();

            assertTrue(str.contains("id=1"));
            assertTrue(str.contains("name='Milky Way'"));
            assertTrue(str.contains("sectors=100"));
            assertTrue(str.contains("systems=0"));
            assertTrue(str.contains("seed=42"));
            assertTrue(str.contains("gameType=DoraT"));
            assertTrue(str.contains("difficulty=medium"));
        }

        @Test
        @DisplayName("toString should handle null values gracefully")
        void toString_shouldHandleNulls() {
            Galaxy galaxy = new Galaxy();
            String str = galaxy.toString();

            // Should not throw NPE
            assertNotNull(str);
            assertTrue(str.contains("Galaxy{"));
        }
    }

    @Nested
    @DisplayName("Solar Systems")
    class SolarSystems {

        @Test
        @DisplayName("solarSystems should be initialized as empty list")
        void solarSystems_shouldBeInitialized() {
            Galaxy galaxy = new Galaxy();
            assertNotNull(galaxy.getSolarSystems());
            assertTrue(galaxy.getSolarSystems().isEmpty());
        }

        @Test
        @DisplayName("solarSystems setter should work")
        void solarSystems_setter() {
            Galaxy galaxy = new Galaxy();
            galaxy.setSolarSystems(null);
            assertNull(galaxy.getSolarSystems());
        }
    }
}
