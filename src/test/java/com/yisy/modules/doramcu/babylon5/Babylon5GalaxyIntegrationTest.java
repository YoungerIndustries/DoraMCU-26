package com.yisy.modules.doramcu.babylon5;

import com.yisy.modules.doramcu.babylon5.model.B5Location;
import com.yisy.modules.doramcu.babylon5.model.B5Race;
import com.yisy.modules.doramcu.babylon5.service.Babylon5DatabaseService;
import com.yisy.modules.doramcu.babylon5.service.Babylon5EmpireBuilder;
import com.yisy.modules.doramcu.hex.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for the B5 galaxy integration.
 * Validates that B5 races can populate empires and B5 locations can serve as home systems.
 */
class Babylon5GalaxyIntegrationTest {

    private Babylon5DatabaseService databaseService;
    private Babylon5EmpireBuilder empireBuilder;

    @BeforeEach
    void setUp() {
        databaseService = new Babylon5DatabaseService("docs/babylon5-db/babylon5-universe.json");
        databaseService.loadDatabase();
        empireBuilder = new Babylon5EmpireBuilder(databaseService, Babylon5EmpireBuilder.DEFAULT_EMPIRE_SEED);
    }

    @Test
    void testGalaxyTemplateBabylon5Exists() {
        // Verify the template is accessible
        GalaxyTemplate template = GalaxyTemplate.findByName("BABYLON5");
        assertNotNull(template, "BABYLON5 template should exist");
        assertEquals("Babylon 5", template.getDisplayName());

        // Verify the template has correct config
        GalaxyGenerationConfig config = template.getConfig();
        assertEquals(10, config.getSectorSize());
        assertEquals(10, config.getSubSectorDimension());
        assertEquals(0.25, config.getCivilizationDensity(), 0.01);
        assertEquals(0.4, config.getResourceDistribution(), 0.01);
        assertEquals("spiral", config.getGalaxyShape());
        assertEquals("mythological", config.getNamingConvention());
        assertEquals(13, config.getEmpireCount());
        assertEquals("medium", config.getDifficultyLevel());
        assertEquals("Babylon5", config.getGameType());
        assertEquals(0.15, config.getSpaceStationChance(), 0.01);
        assertEquals(1.0, config.getHyperlaneDensity(), 0.01);
    }

    @Test
    void testGalaxyTemplateBabylon5FindByDisplayName() {
        GalaxyTemplate template = GalaxyTemplate.findByName("Babylon 5");
        assertNotNull(template);
        assertEquals("BABYLON5", template.name());
    }

    @Test
    void testEmpireCreationFromRaces() {
        // Verify B5 races are loaded
        List<B5Race> races = databaseService.getAllRaces();
        assertTrue(races.size() >= 10, "Should have at least 10 B5 races, found: " + races.size());

        // Generate a galaxy
        Galaxy galaxy = GalaxyFactory.generateFromTemplate(
                "Test B5 Galaxy",
                GalaxyTemplate.BABYLON5,
                42L
        );

        // Create a game
        Game game = new Game("Test B5 Campaign");
        game.addGalaxy(galaxy);

        // Build empires
        List<Empire> empires = empireBuilder.buildEmpires(game, galaxy);

        // Verify empires were created
        assertEquals(races.size(), empires.size(), "Should create one empire per B5 race");

        // Verify each empire has a name matching a race
        for (Empire empire : empires) {
            assertTrue(races.stream().anyMatch(r -> r.getName().equals(empire.getName())),
                    "Empire name should match a B5 race name: " + empire.getName());
        }
    }

    @Test
    void testRaceStatMapping() {
        Galaxy galaxy = GalaxyFactory.generateFromTemplate(
                "Test B5 Galaxy",
                GalaxyTemplate.BABYLON5,
                42L
        );
        Game game = new Game("Test B5 Campaign");
        game.addGalaxy(galaxy);

        // Find specific races and verify their stats
        B5Race narn = databaseService.getRace("narn").orElse(null);
        B5Race centauri = databaseService.getRace("centauri").orElse(null);
        B5Race minbari = databaseService.getRace("minbari").orElse(null);

        if (narn != null) {
            Empire narnEmpire = empireBuilder.createEmpireFromRace(narn, game, galaxy);
            assertNotNull(narnEmpire, "Narn empire should be created");
            assertEquals(narn.getName(), narnEmpire.getName());
            assertTrue(narnEmpire.hasCapital(), "Narn empire should have a capital");
        }

        if (centauri != null) {
            Empire centauriEmpire = empireBuilder.createEmpireFromRace(centauri, game, galaxy);
            assertNotNull(centauriEmpire, "Centauri empire should be created");
            assertEquals(centauri.getName(), centauriEmpire.getName());
        }

        if (minbari != null) {
            Empire minbariEmpire = empireBuilder.createEmpireFromRace(minbari, game, galaxy);
            assertNotNull(minbariEmpire, "Minbari empire should be created");
            assertEquals(minbari.getName(), minbariEmpire.getName());
        }
    }

    @Test
    void testGalaxyGenerationWithB5Template() {
        // Generate a galaxy using the BABYLON5 template
        Galaxy galaxy = GalaxyFactory.generateFromTemplate(
                "B5 Test Galaxy",
                GalaxyTemplate.BABYLON5,
                123L
        );

        assertNotNull(galaxy);
        assertEquals("B5 Test Galaxy", galaxy.getName());

        // Verify galaxy has solar systems
        Map<Integer, SolarSystem> systems = galaxy.getAllSystems();
        assertFalse(systems.isEmpty(), "Galaxy should have solar systems");
    }

    @Test
    void testLocationMapping() {
        List<B5Location> locations = databaseService.getAllLocations();
        if (locations.isEmpty()) {
            // Skip if no locations loaded
            return;
        }

        Galaxy galaxy = GalaxyFactory.generateFromTemplate(
                "Test B5 Galaxy",
                GalaxyTemplate.BABYLON5,
                42L
        );

        // Test findHomeSystem for each location
        for (B5Location location : locations) {
            SolarSystem system = empireBuilder.findHomeSystem(location, galaxy);
            // System may be null if galaxy has no systems, but should not throw
            assertNotNull(system, "Should find a home system for location: " + location.getName());
        }
    }

    @Test
    void testEmpireWithNullRace() {
        Galaxy galaxy = GalaxyFactory.generateFromTemplate(
                "Test B5 Galaxy",
                GalaxyTemplate.BABYLON5,
                42L
        );
        Game game = new Game("Test B5 Campaign");
        game.addGalaxy(galaxy);

        Empire empire = empireBuilder.createEmpireFromRace(null, game, galaxy);
        assertNull(empire, "Null race should return null empire");
    }

    @Test
    void testEmpireWithMissingHomeworld() {
        Galaxy galaxy = GalaxyFactory.generateFromTemplate(
                "Test B5 Galaxy",
                GalaxyTemplate.BABYLON5,
                42L
        );
        Game game = new Game("Test B5 Campaign");
        game.addGalaxy(galaxy);

        // Create a race with no homeworld
        B5Race race = new B5Race();
        race.setId("test");
        race.setName("Test Race");
        race.setHomeworld("");

        Empire empire = empireBuilder.createEmpireFromRace(race, game, galaxy);
        assertNotNull(empire, "Empire should be created even with missing homeworld");
        assertEquals("Test Race", empire.getName());
    }

    @Test
    void testBuildEmpiresWithEmptyDatabase() {
        // Create a service with a non-existent file
        Babylon5DatabaseService emptyService = new Babylon5DatabaseService("non-existent.json");
        emptyService.loadDatabase();
        Babylon5EmpireBuilder emptyBuilder = new Babylon5EmpireBuilder(emptyService);

        Galaxy galaxy = GalaxyFactory.generateFromTemplate(
                "Test B5 Galaxy",
                GalaxyTemplate.BABYLON5,
                42L
        );
        Game game = new Game("Test B5 Campaign");
        game.addGalaxy(galaxy);

        List<Empire> empires = emptyBuilder.buildEmpires(game, galaxy);
        assertTrue(empires.isEmpty(), "Empty database should produce no empires");
    }

    @Test
    void testEmpireRegisteredInGame() {
        Galaxy galaxy = GalaxyFactory.generateFromTemplate(
                "Test B5 Galaxy",
                GalaxyTemplate.BABYLON5,
                42L
        );
        Game game = new Game("Test B5 Campaign");
        game.addGalaxy(galaxy);

        List<Empire> empires = empireBuilder.buildEmpires(game, galaxy);

        // Verify empires are registered in the game
        Map<Integer, Empire> gameEmpires = game.getEmpires();
        assertEquals(empires.size(), gameEmpires.size(), "All empires should be registered in the game");

        for (Empire empire : empires) {
            assertNotNull(game.getEmpire(empire.getId()),
                    "Empire should be registered: " + empire.getName());
        }
    }

    @Test
    void testEmpireHasCapitalPlanet() {
        Galaxy galaxy = GalaxyFactory.generateFromTemplate(
                "Test B5 Galaxy",
                GalaxyTemplate.BABYLON5,
                42L
        );
        Game game = new Game("Test B5 Campaign");
        game.addGalaxy(galaxy);

        List<Empire> empires = empireBuilder.buildEmpires(game, galaxy);

        for (Empire empire : empires) {
            assertTrue(empire.hasCapital(),
                    "Empire should have a capital planet: " + empire.getName());
            assertNotNull(empire.getCapitalPlanetId(),
                    "Capital planet ID should not be null: " + empire.getName());
        }
    }

    @Test
    void testEmpireHasStartingFleets() {
        Galaxy galaxy = GalaxyFactory.generateFromTemplate(
                "Test B5 Galaxy",
                GalaxyTemplate.BABYLON5,
                42L
        );
        Game game = new Game("Test B5 Campaign");
        game.addGalaxy(galaxy);

        List<Empire> empires = empireBuilder.buildEmpires(game, galaxy);

        // Verify that fleets were created
        Map<Integer, Fleet> fleets = game.getFleets();
        assertFalse(fleets.isEmpty(), "At least one fleet should be created");
    }

    @Test
    void testGalaxyTemplateCustomize() {
        GalaxyTemplate template = GalaxyTemplate.BABYLON5;
        GalaxyGenerationConfig customConfig = template.customize()
                .empireCount(5)
                .civilizationDensity(0.5)
                .build();

        assertEquals(5, customConfig.getEmpireCount());
        assertEquals(0.5, customConfig.getCivilizationDensity(), 0.01);
    }
}
