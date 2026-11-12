package com.yisy.modules.doramcu.hex;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("GalaxyFactory Tests")
class GalaxyFactoryTest {

    @Nested
    @DisplayName("Constants")
    class Constants {

        @Test
        @DisplayName("GAME_GALAXY_NAME should be Milky Way")
        void gameGalaxyName_shouldBeMilkyWay() {
            assertEquals("Milky Way", GalaxyFactory.GAME_GALAXY_NAME);
        }

        @Test
        @DisplayName("TEST_GALAXY_NAME should be Test Galaxy")
        void testGalaxyName_shouldBeTestGalaxy() {
            assertEquals("Test Galaxy", GalaxyFactory.TEST_GALAXY_NAME);
        }

        @Test
        @DisplayName("GAME_GALAXY_SEED should match default seed")
        void gameGalaxySeed_shouldMatchDefault() {
            assertEquals(GalaxyGenerator.DEFAULT_SEED, GalaxyFactory.GAME_GALAXY_SEED);
        }

        @Test
        @DisplayName("TEST_GALAXY_SEED should be 12345")
        void testGalaxySeed_shouldBe12345() {
            assertEquals(12345L, GalaxyFactory.TEST_GALAXY_SEED);
        }
    }

    @Nested
    @DisplayName("Predefined Galaxy Configs")
    class PredefinedConfigs {

        @Test
        @DisplayName("TRAVELLER_CLASSIC template should use default values")
        void travellerClassicConfig_shouldUseDefaults() {
            GalaxyGenerationConfig config = GalaxyTemplate.TRAVELLER_CLASSIC.getConfig();

            assertEquals(GalaxyGenerationConfig.DEFAULT_SYSTEM_COUNT, config.getSystemCount());
            assertEquals(GalaxyGenerationConfig.DEFAULT_PLANETS_PER_SYSTEM, config.getPlanetsPerSystem());
            assertEquals(GalaxyGenerationConfig.DEFAULT_MOON_CHANCE, config.getMoonChance());
            assertEquals(GalaxyGenerationConfig.DEFAULT_HYPERLANE_DENSITY, config.getHyperlaneDensity());
        }

        @Test
        @DisplayName("testGalaxyConfig should use minimal values")
        void testGalaxyConfig_shouldUseMinimalValues() {
            GalaxyGenerationConfig config = GalaxyFactory.testGalaxyConfig();

            assertEquals(50, config.getSystemCount());
            assertEquals(2, config.getPlanetsPerSystem());
            assertEquals(0.4, config.getMoonChance());
            assertEquals(1.0, config.getHyperlaneDensity());
        }

        @Test
        @DisplayName("game and test configs should differ")
        void gameAndTestConfigs_shouldDiffer() {
            GalaxyGenerationConfig gameConfig = GalaxyTemplate.TRAVELLER_CLASSIC.getConfig();
            GalaxyGenerationConfig testConfig = GalaxyFactory.testGalaxyConfig();

            assertNotEquals(testConfig.getSystemCount(), gameConfig.getSystemCount());
            assertNotEquals(testConfig.getPlanetsPerSystem(), gameConfig.getPlanetsPerSystem());
        }
    }

    @Nested
    @DisplayName("Predefined Galaxy Generation")
    class PredefinedGeneration {

        @Test
        @DisplayName("generateGameGalaxy should produce Milky Way")
        void generateGameGalaxy_shouldProduceMilkyWay() {
            Galaxy galaxy = GalaxyFactory.generateGameGalaxy();

            assertNotNull(galaxy);
            assertEquals("Milky Way", galaxy.getName());
            assertTrue(galaxy.size() > 0, "Game galaxy should have systems");
        }

        @Test
        @DisplayName("generateTestGalaxy should produce Test Galaxy")
        void generateTestGalaxy_shouldProduceTestGalaxy() {
            Galaxy galaxy = GalaxyFactory.generateTestGalaxy();

            assertNotNull(galaxy);
            assertEquals("Test Galaxy", galaxy.getName());
            assertTrue(galaxy.size() > 0, "Test galaxy should have systems");
        }

        @Test
        @DisplayName("game galaxy should have more systems than test galaxy")
        void gameGalaxy_shouldHaveMoreSystems() {
            Galaxy gameGalaxy = GalaxyFactory.generateGameGalaxy();
            Galaxy testGalaxy = GalaxyFactory.generateTestGalaxy();

            assertTrue(gameGalaxy.size() > testGalaxy.size(),
                    "Game galaxy should have more systems than test galaxy");
        }

        @Test
        @DisplayName("generateGalaxyByName should return game galaxy for Milky Way")
        void generateGalaxyByName_shouldReturnGameGalaxy() {
            Galaxy galaxy = GalaxyFactory.generateGalaxyByName(GalaxyFactory.GAME_GALAXY_NAME);

            assertNotNull(galaxy);
            assertEquals("Milky Way", galaxy.getName());
        }

        @Test
        @DisplayName("generateGalaxyByName should return test galaxy for Test Galaxy")
        void generateGalaxyByName_shouldReturnTestGalaxy() {
            Galaxy galaxy = GalaxyFactory.generateGalaxyByName(GalaxyFactory.TEST_GALAXY_NAME);

            assertNotNull(galaxy);
            assertEquals("Test Galaxy", galaxy.getName());
        }

        @Test
        @DisplayName("generateGalaxyByName should throw for unknown name")
        void generateGalaxyByName_shouldThrowForUnknown() {
            assertThrows(IllegalArgumentException.class, () ->
                    GalaxyFactory.generateGalaxyByName("Unknown Galaxy")
            );
        }

        @Test
        @DisplayName("generateGalaxyByName error message should mention generateCustomGalaxy")
        void generateGalaxyByName_errorShouldMentionCustom() {
            Exception ex = assertThrows(IllegalArgumentException.class, () ->
                    GalaxyFactory.generateGalaxyByName("Unknown Galaxy")
            );
            assertTrue(ex.getMessage().contains("generateCustomGalaxy"),
                    "Error message should suggest using generateCustomGalaxy()");
        }
    }

    @Nested
    @DisplayName("Custom Galaxy Generation")
    class CustomGeneration {

        @Test
        @DisplayName("generateCustomGalaxy should accept any non-empty name")
        void generateCustomGalaxy_shouldAcceptAnyName() {
            Galaxy galaxy = GalaxyFactory.generateCustomGalaxy(
                    "Andromeda", GalaxyFactory.defaultConfig(), 42L);

            assertNotNull(galaxy);
            assertEquals("Andromeda", galaxy.getName());
        }

        @Test
        @DisplayName("generateCustomGalaxy should use provided config")
        void generateCustomGalaxy_shouldUseProvidedConfig() {
            GalaxyGenerationConfig config = GalaxyGenerationConfig.builder()
                    .systemCount(200)
                    .planetsPerSystem(3)
                    .build();

            Galaxy galaxy = GalaxyFactory.generateCustomGalaxy(
                    "Custom", config, 123L);

            assertNotNull(galaxy);
            assertEquals("Custom", galaxy.getName());
            assertTrue(galaxy.size() > 0);
        }

        @Test
        @DisplayName("generateCustomGalaxy should be reproducible with same seed")
        void generateCustomGalaxy_shouldBeReproducible() {
            GalaxyGenerationConfig config = GalaxyFactory.defaultConfig();
            long seed = 99999L;

            Galaxy galaxy1 = GalaxyFactory.generateCustomGalaxy("Repro", config, seed);
            Galaxy galaxy2 = GalaxyFactory.generateCustomGalaxy("Repro", config, seed);

            assertEquals(galaxy1.size(), galaxy2.size(),
                    "Same seed should produce same system count");
        }

        @Test
        @DisplayName("generateCustomGalaxy should throw for null name")
        void generateCustomGalaxy_shouldThrowForNullName() {
            assertThrows(IllegalArgumentException.class, () ->
                    GalaxyFactory.generateCustomGalaxy(null, GalaxyFactory.defaultConfig(), 42L)
            );
        }

        @Test
        @DisplayName("generateCustomGalaxy should throw for blank name")
        void generateCustomGalaxy_shouldThrowForBlankName() {
            assertThrows(IllegalArgumentException.class, () ->
                    GalaxyFactory.generateCustomGalaxy("   ", GalaxyFactory.defaultConfig(), 42L)
            );
        }

        @Test
        @DisplayName("generateCustomGalaxy should throw for empty name")
        void generateCustomGalaxy_shouldThrowForEmptyName() {
            assertThrows(IllegalArgumentException.class, () ->
                    GalaxyFactory.generateCustomGalaxy("", GalaxyFactory.defaultConfig(), 42L)
            );
        }

        @Test
        @DisplayName("generateCustomGalaxy should throw for null config")
        void generateCustomGalaxy_shouldThrowForNullConfig() {
            assertThrows(IllegalArgumentException.class, () ->
                    GalaxyFactory.generateCustomGalaxy("Name", null, 42L)
            );
        }

        @Test
        @DisplayName("generateCustomGalaxy should accept predefined galaxy names")
        void generateCustomGalaxy_shouldAcceptPredefinedNames() {
            // Custom galaxy can use the same name as a predefined galaxy
            Galaxy galaxy = GalaxyFactory.generateCustomGalaxy(
                    GalaxyFactory.GAME_GALAXY_NAME, GalaxyFactory.defaultConfig(), 42L);

            assertNotNull(galaxy);
            assertEquals("Milky Way", galaxy.getName());
        }
    }

    @Nested
    @DisplayName("Default Config")
    class DefaultConfig {

        @Test
        @DisplayName("defaultConfig should return same config as TRAVELLER_CLASSIC template")
        void defaultConfig_shouldMatchTravellerClassic() {
            GalaxyGenerationConfig defaultConfig = GalaxyFactory.defaultConfig();
            GalaxyGenerationConfig gameConfig = GalaxyTemplate.TRAVELLER_CLASSIC.getConfig();

            assertEquals(gameConfig.getSystemCount(), defaultConfig.getSystemCount());
            assertEquals(gameConfig.getPlanetsPerSystem(), defaultConfig.getPlanetsPerSystem());
            assertEquals(gameConfig.getMoonChance(), defaultConfig.getMoonChance());
            assertEquals(gameConfig.getHyperlaneDensity(), defaultConfig.getHyperlaneDensity());
        }

        @Test
        @DisplayName("defaultConfig should be usable for custom galaxy creation")
        void defaultConfig_shouldBeUsableForCustomGalaxy() {
            Galaxy galaxy = GalaxyFactory.generateCustomGalaxy(
                    "Default Template", GalaxyFactory.defaultConfig(), 42L);

            assertNotNull(galaxy);
            assertTrue(galaxy.size() > 0);
        }
    }

    @Nested
    @DisplayName("Name Validation")
    class NameValidation {

        @Test
        @DisplayName("isPredefinedGalaxyName should return true for Milky Way")
        void isPredefined_shouldReturnTrueForMilkyWay() {
            assertTrue(GalaxyFactory.isPredefinedGalaxyName("Milky Way"));
        }

        @Test
        @DisplayName("isPredefinedGalaxyName should return true for Test Galaxy")
        void isPredefined_shouldReturnTrueForTestGalaxy() {
            assertTrue(GalaxyFactory.isPredefinedGalaxyName("Test Galaxy"));
        }

        @Test
        @DisplayName("isPredefinedGalaxyName should return false for custom names")
        void isPredefined_shouldReturnFalseForCustom() {
            assertFalse(GalaxyFactory.isPredefinedGalaxyName("Andromeda"));
            assertFalse(GalaxyFactory.isPredefinedGalaxyName("Custom Galaxy"));
        }

        @Test
        @DisplayName("isValidGalaxyName should accept any non-blank string")
        void isValid_shouldAcceptNonBlank() {
            assertTrue(GalaxyFactory.isValidGalaxyName("Milky Way"));
            assertTrue(GalaxyFactory.isValidGalaxyName("Andromeda"));
            assertTrue(GalaxyFactory.isValidGalaxyName("X"));
            assertTrue(GalaxyFactory.isValidGalaxyName("Test Galaxy 2"));
        }

        @Test
        @DisplayName("isValidGalaxyName should reject null")
        void isValid_shouldRejectNull() {
            assertFalse(GalaxyFactory.isValidGalaxyName(null));
        }

        @Test
        @DisplayName("isValidGalaxyName should reject empty string")
        void isValid_shouldRejectEmpty() {
            assertFalse(GalaxyFactory.isValidGalaxyName(""));
        }

        @Test
        @DisplayName("isValidGalaxyName should reject blank string")
        void isValid_shouldRejectBlank() {
            assertFalse(GalaxyFactory.isValidGalaxyName("   "));
        }
    }

    @Nested
    @DisplayName("Private Constructor")
    class PrivateConstructor {

        @Test
        @DisplayName("GalaxyFactory should not be instantiable")
        void factory_shouldNotBeInstantiable() throws NoSuchMethodException {
            // The constructor is private — verify via reflection
            var constructor = GalaxyFactory.class.getDeclaredConstructor();
            assertTrue(java.lang.reflect.Modifier.isPrivate(constructor.getModifiers()),
                    "GalaxyFactory constructor should be private");
        }
    }
}
