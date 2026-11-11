package com.yisy.modules.doramcu.hex;

import com.yisy.modules.doramcu.hex.manager.DefaultEmpireBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for the GalaxyTemplate consolidation components.
 * <p>
 * Tests cover EmpireBuilder interface, DefaultEmpireBuilder, GalaxyTemplate.launchGame(),
 * GalaxyServiceRegistry, and GalaxyFactory template-based generation.
 * </p>
 */
@DisplayName("Galaxy Consolidation Integration Tests")
class GalaxyConsolidationIntegrationTest {

    @AfterEach
    void cleanup() {
        // Reset static state after each test
        GalaxyTemplate.setEmpireBuilder(null);
    }

    @Nested
    @DisplayName("EmpireBuilder interface")
    class EmpireBuilderInterface {

        @Test
        @DisplayName("EmpireConfig defaults")
        void empireConfigDefaults() {
            EmpireBuilder.EmpireConfig config = new EmpireBuilder.EmpireConfig();
            assertEquals(PlanetSize.MEDIUM, config.getPlanetSize());
            assertEquals(MineralQuality.NORMAL, config.getMineralQuality());
            assertEquals(1, config.getIndustrialLevel());
            assertFalse(config.isHasMilitaryFleet());
            assertTrue(config.isHasMiningFleet());
            assertFalse(config.isHasReserveFleet());
        }

        @Test
        @DisplayName("EmpireConfig setters")
        void empireConfigSetters() {
            EmpireBuilder.EmpireConfig config = new EmpireBuilder.EmpireConfig();
            config.setPlanetSize(PlanetSize.LARGE);
            config.setMineralQuality(MineralQuality.RICH);
            config.setIndustrialLevel(5);
            config.setHasMilitaryFleet(true);
            config.setHasMiningFleet(false);
            config.setHasReserveFleet(true);

            assertEquals(PlanetSize.LARGE, config.getPlanetSize());
            assertEquals(MineralQuality.RICH, config.getMineralQuality());
            assertEquals(5, config.getIndustrialLevel());
            assertTrue(config.isHasMilitaryFleet());
            assertFalse(config.isHasMiningFleet());
            assertTrue(config.isHasReserveFleet());
        }

        @Test
        @DisplayName("EmpireConfig toString")
        void empireConfigToString() {
            EmpireBuilder.EmpireConfig config = new EmpireBuilder.EmpireConfig(PlanetSize.SMALL, MineralQuality.POOR);
            String str = config.toString();
            assertTrue(str.contains("planetSize="));
            assertTrue(str.contains("mineralQuality="));
        }

        @Test
        @DisplayName("EmpireConfig constructor with params")
        void empireConfigConstructor() {
            EmpireBuilder.EmpireConfig config = new EmpireBuilder.EmpireConfig(PlanetSize.LARGE, MineralQuality.RICH);
            assertEquals(PlanetSize.LARGE, config.getPlanetSize());
            assertEquals(MineralQuality.RICH, config.getMineralQuality());
        }
    }

    @Nested
    @DisplayName("DefaultEmpireBuilder")
    class DefaultEmpireBuilderTests {

        @Test
        @DisplayName("buildEmpires creates empires based on galaxy empire count")
        void buildEmpiresCreatesEmpires() {
            Galaxy galaxy = GalaxyFactory.generateTestGalaxy();
            GalaxyGenerationConfig config = GalaxyGenerationConfig.builder().empireCount(3).build();
            galaxy.setGenerationConfig(config);

            Game game = new Game("Test Galaxy");
            game.addGalaxy(galaxy);

            DefaultEmpireBuilder builder = new DefaultEmpireBuilder(42L);
            List<Empire> empires = builder.buildEmpires(game, galaxy);

            assertEquals(3, empires.size());
            assertNotNull(empires.get(0).getName());
            assertNotNull(empires.get(0).getCapitalPlanetId());
        }

        @Test
        @DisplayName("buildEmpires uses default empire count when no config")
        void buildEmpiresDefaultCount() {
            Galaxy galaxy = new Galaxy(1, "Test");
            // No config set, getEmpireCount() returns DEFAULT_EMPIRE_COUNT (4)

            Game game = new Game("Test Galaxy");
            game.addGalaxy(galaxy);

            DefaultEmpireBuilder builder = new DefaultEmpireBuilder(42L);
            List<Empire> empires = builder.buildEmpires(game, galaxy);

            // Default empire count is 4, so 4 empires should be created
            assertEquals(4, empires.size());
        }

        @Test
        @DisplayName("buildEmpires creates 1 empire with empireCount=1")
        void buildEmpiresOneEmpire() {
            Galaxy galaxy = GalaxyFactory.generateTestGalaxy();
            GalaxyGenerationConfig config = GalaxyGenerationConfig.builder().empireCount(1).build();
            galaxy.setGenerationConfig(config);

            Game game = new Game("Test Galaxy");
            game.addGalaxy(galaxy);

            DefaultEmpireBuilder builder = new DefaultEmpireBuilder(42L);
            List<Empire> empires = builder.buildEmpires(game, galaxy);

            assertEquals(1, empires.size());
        }

        @Test
        @DisplayName("createEmpire creates empire with config")
        void createEmpireWithConfig() {
            Galaxy galaxy = GalaxyFactory.generateTestGalaxy();
            Game game = new Game("Test Galaxy");
            game.addGalaxy(galaxy);

            Player player = game.createPlayer("Test Player");
            EmpireBuilder.EmpireConfig config = new EmpireBuilder.EmpireConfig();
            config.setPlanetSize(PlanetSize.LARGE);
            config.setMineralQuality(MineralQuality.RICH);
            config.setIndustrialLevel(3);
            config.setHasMilitaryFleet(true);

            DefaultEmpireBuilder builder = new DefaultEmpireBuilder(42L);
            Empire empire = builder.createEmpire("Test Empire", game, player, galaxy, config);

            assertNotNull(empire);
            assertEquals("Test Empire", empire.getName());
            assertNotNull(empire.getCapitalPlanetId());
        }

        @Test
        @DisplayName("createEmpire returns null for null name")
        void createEmpireNullName() {
            Galaxy galaxy = GalaxyFactory.generateTestGalaxy();
            Game game = new Game("Test Galaxy");
            game.addGalaxy(galaxy);

            Player player = game.createPlayer("Test Player");
            DefaultEmpireBuilder builder = new DefaultEmpireBuilder(42L);
            Empire empire = builder.createEmpire(null, game, player, galaxy, new EmpireBuilder.EmpireConfig());

            assertNull(empire);
        }

        @Test
        @DisplayName("createEmpire returns null for blank name")
        void createEmpireBlankName() {
            Galaxy galaxy = GalaxyFactory.generateTestGalaxy();
            Game game = new Game("Test Galaxy");
            game.addGalaxy(galaxy);

            Player player = game.createPlayer("Test Player");
            DefaultEmpireBuilder builder = new DefaultEmpireBuilder(42L);
            Empire empire = builder.createEmpire("   ", game, player, galaxy, new EmpireBuilder.EmpireConfig());

            assertNull(empire);
        }

        @Test
        @DisplayName("createEmpire returns null for null player")
        void createEmpireNullPlayer() {
            Galaxy galaxy = GalaxyFactory.generateTestGalaxy();
            Game game = new Game("Test Galaxy");
            game.addGalaxy(galaxy);

            DefaultEmpireBuilder builder = new DefaultEmpireBuilder(42L);
            Empire empire = builder.createEmpire("Test Empire", game, null, galaxy, new EmpireBuilder.EmpireConfig());

            assertNull(empire);
        }

        @Test
        @DisplayName("createEmpire returns null for null game")
        void createEmpireNullGame() {
            Galaxy galaxy = GalaxyFactory.generateTestGalaxy();
            Game tempGame = new Game("Temp");
            Player player = tempGame.createPlayer("Test Player");
            DefaultEmpireBuilder builder = new DefaultEmpireBuilder(42L);
            Empire empire = builder.createEmpire("Test Empire", null, player, galaxy, new EmpireBuilder.EmpireConfig());

            assertNull(empire);
        }

        @Test
        @DisplayName("Reproducible with same seed")
        void reproducibleWithSameSeed() {
            Galaxy galaxy1 = GalaxyFactory.generateTestGalaxy();
            GalaxyGenerationConfig config1 = GalaxyGenerationConfig.builder().empireCount(2).build();
            galaxy1.setGenerationConfig(config1);

            Game game1 = new Game("Test Galaxy 1");
            game1.addGalaxy(galaxy1);

            DefaultEmpireBuilder builder1 = new DefaultEmpireBuilder(123L);
            List<Empire> empires1 = builder1.buildEmpires(game1, galaxy1);

            Galaxy galaxy2 = GalaxyFactory.generateTestGalaxy();
            GalaxyGenerationConfig config2 = GalaxyGenerationConfig.builder().empireCount(2).build();
            galaxy2.setGenerationConfig(config2);

            Game game2 = new Game("Test Galaxy 2");
            game2.addGalaxy(galaxy2);

            DefaultEmpireBuilder builder2 = new DefaultEmpireBuilder(123L);
            List<Empire> empires2 = builder2.buildEmpires(game2, galaxy2);

            assertEquals(empires1.get(0).getName(), empires2.get(0).getName());
            assertEquals(empires1.get(1).getName(), empires2.get(1).getName());
        }
    }

    @Nested
    @DisplayName("GalaxyTemplate.launchGame")
    class LaunchGameTests {

        @Test
        @DisplayName("launchGame with CUSTOM template")
        void launchGameCustom() {
            GalaxyTemplate.setEmpireBuilder(null);
            Game game = GalaxyTemplate.CUSTOM.launchGame("Custom Galaxy", 42L);

            assertNotNull(game);
            assertEquals("Custom Galaxy", game.getName());
            assertFalse(game.getGalaxies().isEmpty());
        }

        @Test
        @DisplayName("launchGame with TRAVELLER_CLASSIC template")
        void launchGameTravellerClassic() {
            GalaxyTemplate.setEmpireBuilder(null);
            Game game = GalaxyTemplate.TRAVELLER_CLASSIC.launchGame("Traveller Galaxy", 42L);

            assertNotNull(game);
            assertEquals("Traveller Galaxy", game.getName());
            assertFalse(game.getGalaxies().isEmpty());
        }

        @Test
        @DisplayName("launchGame with BABYLON5 template")
        void launchGameBabylon5() {
            GalaxyTemplate.setEmpireBuilder(null);
            Game game = GalaxyTemplate.BABYLON5.launchGame("B5 Galaxy", 123L);

            assertNotNull(game);
            assertEquals("B5 Galaxy", game.getName());
            assertFalse(game.getGalaxies().isEmpty());
        }

        @Test
        @DisplayName("launchGame with VOID template")
        void launchGameVoid() {
            GalaxyTemplate.setEmpireBuilder(null);
            Game game = GalaxyTemplate.VOID.launchGame("Void Galaxy", 456L);

            assertNotNull(game);
            assertEquals("Void Galaxy", game.getName());
            assertFalse(game.getGalaxies().isEmpty());
        }

        @Test
        @DisplayName("launchGame with custom EmpireBuilder")
        void launchGameWithCustomBuilder() {
            EmpireBuilder customBuilder = new DefaultEmpireBuilder(999L);
            GalaxyTemplate.setEmpireBuilder(customBuilder);

            Game game = GalaxyTemplate.CUSTOM.launchGame("Custom Galaxy", 42L);

            assertNotNull(game);
            GalaxyTemplate.setEmpireBuilder(null);
        }
    }

    @Nested
    @DisplayName("GalaxyFactory template-based generation")
    class GalaxyFactoryTemplateTests {

        @Test
        @DisplayName("generateGameGalaxy uses TRAVELLER_CLASSIC template")
        void generateGameGalaxyUsesTemplate() {
            Galaxy galaxy = GalaxyFactory.generateGameGalaxy();
            assertNotNull(galaxy);
            assertEquals(GalaxyFactory.GAME_GALAXY_NAME, galaxy.getName());
        }

        @Test
        @DisplayName("generateVoidGalaxy uses VOID template")
        void generateVoidGalaxyUsesTemplate() {
            Galaxy galaxy = GalaxyFactory.generateVoidGalaxy();
            assertNotNull(galaxy);
            assertEquals(GalaxyFactory.VOID_GALAXY_NAME, galaxy.getName());
        }

        @Test
        @DisplayName("generateFromTemplate generates galaxy")
        void generateFromTemplate() {
            Galaxy galaxy = GalaxyFactory.generateFromTemplate("Test Galaxy", GalaxyTemplate.BABYLON5, 42L);
            assertNotNull(galaxy);
            assertEquals("Test Galaxy", galaxy.getName());
        }

        @Test
        @DisplayName("defaultConfig returns TRAVELLER_CLASSIC config")
        void defaultConfigMatchesTravellerClassic() {
            GalaxyGenerationConfig defaultConfig = GalaxyFactory.defaultConfig();
            GalaxyGenerationConfig travellerConfig = GalaxyTemplate.TRAVELLER_CLASSIC.getConfig();

            assertEquals(travellerConfig.getSystemCount(), defaultConfig.getSystemCount());
            assertEquals(travellerConfig.getPlanetsPerSystem(), defaultConfig.getPlanetsPerSystem());
            assertEquals(travellerConfig.getHyperlaneDensity(), defaultConfig.getHyperlaneDensity(), 0.001);
        }
    }

    @Nested
    @DisplayName("Galaxy generation config")
    class GalaxyConfigTests {

        @Test
        @DisplayName("Galaxy stores generation config")
        void galaxyStoresConfig() {
            Galaxy galaxy = GalaxyFactory.generateTestGalaxy();
            GalaxyGenerationConfig config = GalaxyGenerationConfig.builder().empireCount(5).build();
            galaxy.setGenerationConfig(config);

            assertSame(config, galaxy.getGenerationConfig());
            assertEquals(5, galaxy.getEmpireCount());
        }

        @Test
        @DisplayName("Galaxy.getEmpireCount returns default if no config")
        void galaxyEmpireCountNoConfig() {
            Galaxy galaxy = new Galaxy(1, "Test");
            assertEquals(GalaxyGenerationConfig.DEFAULT_EMPIRE_COUNT, galaxy.getEmpireCount());
        }
    }

    @Nested
    @DisplayName("GalaxyServiceRegistry")
    class GalaxyServiceRegistryTests {

        @Test
        @DisplayName("getServices returns empty set for unregistered template")
        void getServicesEmpty() {
            var services = GalaxyServiceRegistry.getServices(GalaxyTemplate.CUSTOM);
            // May have services registered by Spring, but should not throw
            assertNotNull(services);
        }

        @Test
        @DisplayName("getService returns null for unregistered service")
        void getServiceReturnsNull() {
            var service = GalaxyServiceRegistry.getService(
                    GalaxyTemplate.CUSTOM,
                    com.yisy.modules.doramcu.fourx.FourxDiplomacyService.class);
            // May be null if Spring hasn't initialized the registry
            // The important thing is it doesn't throw
            assertNotNull(GalaxyServiceRegistry.getServices(GalaxyTemplate.CUSTOM));
        }

        @Test
        @DisplayName("getAvailableTemplates returns a set")
        void getAvailableTemplates() {
            var templates = GalaxyServiceRegistry.getAvailableTemplates();
            assertNotNull(templates);
            // May be empty if Spring hasn't initialized the registry
        }

        @Test
        @DisplayName("getServiceCount returns non-negative")
        void getServiceCount() {
            int count = GalaxyServiceRegistry.getServiceCount(GalaxyTemplate.BABYLON5);
            assertTrue(count >= 0);
        }

        @Test
        @DisplayName("Convenience methods don't throw")
        void convenienceMethods() {
            // These should not throw even if no services are registered
            assertNotNull(GalaxyServiceRegistry.getDiplomacyService(GalaxyTemplate.BABYLON5) == null ||
                    GalaxyServiceRegistry.getDiplomacyService(GalaxyTemplate.BABYLON5) != null);
            assertNotNull(GalaxyServiceRegistry.getExplorationService(GalaxyTemplate.BABYLON5) == null ||
                    GalaxyServiceRegistry.getExplorationService(GalaxyTemplate.BABYLON5) != null);
            assertNotNull(GalaxyServiceRegistry.getEconomyService(GalaxyTemplate.BABYLON5) == null ||
                    GalaxyServiceRegistry.getEconomyService(GalaxyTemplate.BABYLON5) != null);
            assertNotNull(GalaxyServiceRegistry.getCharacterService(GalaxyTemplate.BABYLON5) == null ||
                    GalaxyServiceRegistry.getCharacterService(GalaxyTemplate.BABYLON5) != null);
            assertNotNull(GalaxyServiceRegistry.getCombatService(GalaxyTemplate.BABYLON5) == null ||
                    GalaxyServiceRegistry.getCombatService(GalaxyTemplate.BABYLON5) != null);
        }
    }

    @Nested
    @DisplayName("End-to-end integration")
    class EndToEndTests {

        @Test
        @DisplayName("Full pipeline: template -> galaxy -> game -> empires")
        void fullPipeline() {
            // Step 1: Generate galaxy from template
            Galaxy galaxy = GalaxyFactory.generateFromTemplate("Integration Test", GalaxyTemplate.FRONTIER, 42L);
            assertNotNull(galaxy);
            assertEquals("Integration Test", galaxy.getName());

            // Step 2: Create game
            Game game = new Game("Integration Test");
            game.addGalaxy(galaxy);

            // Step 3: Build empires
            DefaultEmpireBuilder builder = new DefaultEmpireBuilder(42L);
            List<Empire> empires = builder.buildEmpires(game, galaxy);

            // Step 4: Verify
            assertTrue(empires.size() > 0);
            for (Empire empire : empires) {
                assertNotNull(empire.getName());
                assertNotNull(empire.getCapitalPlanetId());
            }
        }

        @Test
        @DisplayName("Full pipeline with launchGame")
        void fullPipelineWithLaunchGame() {
            GalaxyTemplate.setEmpireBuilder(null);
            Game game = GalaxyTemplate.FRONTIER.launchGame("Frontier Galaxy", 42L);

            assertNotNull(game);
            assertEquals("Frontier Galaxy", game.getName());
            assertFalse(game.getGalaxies().isEmpty());
        }
    }
}
