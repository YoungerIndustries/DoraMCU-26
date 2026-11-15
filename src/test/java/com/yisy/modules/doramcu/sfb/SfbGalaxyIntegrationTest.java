package com.yisy.modules.doramcu.sfb;

import com.yisy.modules.doramcu.sfb.model.SfbBattleLocation;
import com.yisy.modules.doramcu.sfb.service.SfbGalaxyIntegration;
import com.yisy.modules.doramcu.sfb.service.SfbGalaxyIntegration.TerrainEffect;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("SfbGalaxyIntegration Tests")
class SfbGalaxyIntegrationTest {

    private SfbGalaxyIntegration integration;

    @BeforeEach
    void setUp() {
        integration = new SfbGalaxyIntegration();
    }

    // --- TerrainEffect Enum Tests ---

    @Nested
    @DisplayName("TerrainEffect enum")
    class TerrainEffectTests {

        @Test
        @DisplayName("All terrain effects have descriptions")
        void allTerrainEffectsHaveDescriptions() {
            for (TerrainEffect effect : TerrainEffect.values()) {
                assertNotNull(effect.getDescription());
                assertFalse(effect.getDescription().isEmpty());
            }
        }

        @Test
        @DisplayName("Terrain effect descriptions are meaningful")
        void terrainEffectDescriptions() {
            assertTrue(TerrainEffect.NEBULA.getDescription().contains("sensor"));
            assertTrue(TerrainEffect.ASTEROID_FIELD.getDescription().contains("collision"));
            assertTrue(TerrainEffect.ION_STORM.getDescription().contains("EW"));
            assertTrue(TerrainEffect.GRAVITY_WELL.getDescription().contains("movement"));
        }
    }

    // --- Sector Terrain Tests ---

    @Nested
    @DisplayName("Sector terrain management")
    class SectorTerrainTests {

        @Test
        @DisplayName("Default terrain is NONE")
        void defaultTerrainIsNone() {
            assertEquals(TerrainEffect.NONE, integration.getTerrainEffect(0, 0));
            assertEquals(TerrainEffect.NONE, integration.getTerrainEffect(5, 5));
            assertEquals(TerrainEffect.NONE, integration.getTerrainEffect(100, 100));
        }

        @Test
        @DisplayName("Set and get terrain for a sector")
        void setAndGetTerrain() {
            integration.setSectorTerrain(5, 5, TerrainEffect.NEBULA);
            assertEquals(TerrainEffect.NEBULA, integration.getTerrainEffect(5, 5));
        }

        @Test
        @DisplayName("Setting terrain does not affect other sectors")
        void setTerrainIsolation() {
            integration.setSectorTerrain(5, 5, TerrainEffect.NEBULA);
            integration.setSectorTerrain(5, 6, TerrainEffect.ASTEROID_FIELD);

            assertEquals(TerrainEffect.NEBULA, integration.getTerrainEffect(5, 5));
            assertEquals(TerrainEffect.ASTEROID_FIELD, integration.getTerrainEffect(5, 6));
            assertEquals(TerrainEffect.NONE, integration.getTerrainEffect(5, 7));
        }

        @Test
        @DisplayName("Overwriting terrain replaces previous value")
        void overwriteTerrain() {
            integration.setSectorTerrain(5, 5, TerrainEffect.NEBULA);
            assertEquals(TerrainEffect.NEBULA, integration.getTerrainEffect(5, 5));

            integration.setSectorTerrain(5, 5, TerrainEffect.ASTEROID_FIELD);
            assertEquals(TerrainEffect.ASTEROID_FIELD, integration.getTerrainEffect(5, 5));
        }

        @Test
        @DisplayName("All terrain types can be set")
        void allTerrainTypes() {
            integration.setSectorTerrain(1, 1, TerrainEffect.NEBULA);
            integration.setSectorTerrain(1, 2, TerrainEffect.ASTEROID_FIELD);
            integration.setSectorTerrain(1, 3, TerrainEffect.ION_STORM);
            integration.setSectorTerrain(1, 4, TerrainEffect.GRAVITY_WELL);
            integration.setSectorTerrain(1, 5, TerrainEffect.NONE);

            assertEquals(TerrainEffect.NEBULA, integration.getTerrainEffect(1, 1));
            assertEquals(TerrainEffect.ASTEROID_FIELD, integration.getTerrainEffect(1, 2));
            assertEquals(TerrainEffect.ION_STORM, integration.getTerrainEffect(1, 3));
            assertEquals(TerrainEffect.GRAVITY_WELL, integration.getTerrainEffect(1, 4));
            assertEquals(TerrainEffect.NONE, integration.getTerrainEffect(1, 5));
        }

        @Test
        @DisplayName("Negative sector coordinates work")
        void negativeSectorCoordinates() {
            integration.setSectorTerrain(-1, -1, TerrainEffect.NEBULA);
            assertEquals(TerrainEffect.NEBULA, integration.getTerrainEffect(-1, -1));
            assertEquals(TerrainEffect.NONE, integration.getTerrainEffect(1, 1));
        }
    }

    // --- Battle Location Tests ---

    @Nested
    @DisplayName("Battle location management")
    class BattleLocationTests {

        @Test
        @DisplayName("Set battle location creates location with correct data")
        void setBattleLocation() {
            SfbBattleLocation location = integration.setBattleLocation(
                    "battle-1", 1, 5, 5, "Alpha Centauri");

            assertNotNull(location);
            assertEquals("battle-1", location.getBattleId());
            assertEquals(1, location.getGalaxyId());
            assertEquals(5, location.getSectorRow());
            assertEquals(5, location.getSectorCol());
            assertEquals("Alpha Centauri", location.getSystemName());
        }

        @Test
        @DisplayName("Battle location in sector with terrain gets terrain effect")
        void battleLocationWithTerrain() {
            integration.setSectorTerrain(5, 5, TerrainEffect.NEBULA);

            SfbBattleLocation location = integration.setBattleLocation(
                    "battle-1", 1, 5, 5, "Nebula System");

            assertEquals(TerrainEffect.NEBULA, location.getTerrainEffect());
        }

        @Test
        @DisplayName("Battle location in empty sector gets NONE terrain")
        void battleLocationDefaultTerrain() {
            SfbBattleLocation location = integration.setBattleLocation(
                    "battle-1", 1, 5, 5, "Normal System");

            assertEquals(TerrainEffect.NONE, location.getTerrainEffect());
        }

        @Test
        @DisplayName("Get battle location returns the set location")
        void getBattleLocation() {
            integration.setBattleLocation("battle-1", 1, 5, 5, "Alpha Centauri");

            SfbBattleLocation location = integration.getBattleLocation("battle-1");

            assertNotNull(location);
            assertEquals("Alpha Centauri", location.getSystemName());
        }

        @Test
        @DisplayName("Get battle location returns null for unknown battle")
        void getBattleLocationUnknown() {
            assertNull(integration.getBattleLocation("nonexistent"));
        }

        @Test
        @DisplayName("Multiple battles can be set")
        void multipleBattles() {
            integration.setBattleLocation("battle-1", 1, 5, 5, "System A");
            integration.setBattleLocation("battle-2", 1, 6, 6, "System B");

            assertEquals("System A", integration.getBattleLocation("battle-1").getSystemName());
            assertEquals("System B", integration.getBattleLocation("battle-2").getSystemName());
        }

        @Test
        @DisplayName("Battle location has correct location ID")
        void battleLocationId() {
            integration.setBattleLocation("battle-1", 1, 5, 5, "Alpha Centauri");

            SfbBattleLocation location = integration.getBattleLocation("battle-1");
            assertEquals("1-5-5", location.getLocationId());
        }

        @Test
        @DisplayName("Battle location toString is informative")
        void battleLocationToString() {
            integration.setBattleLocation("battle-1", 1, 5, 5, "Alpha Centauri");

            SfbBattleLocation location = integration.getBattleLocation("battle-1");
            String str = location.toString();

            assertTrue(str.contains("galaxy=1"));
            assertTrue(str.contains("sector=("));
            assertTrue(str.contains("Alpha Centauri"));
        }
    }

    // --- Battles in Sector Tests ---

    @Nested
    @DisplayName("Battles in sector queries")
    class BattlesInSectorTests {

        @Test
        @DisplayName("Empty galaxy has no battles in sector")
        void noBattlesInEmptySector() {
            List<String> battles = integration.getBattlesInSector(5, 5);
            assertTrue(battles.isEmpty());
        }

        @Test
        @DisplayName("Battle in sector is found")
        void battleInSectorFound() {
            integration.setBattleLocation("battle-1", 1, 5, 5, "System A");

            List<String> battles = integration.getBattlesInSector(5, 5);

            assertEquals(1, battles.size());
            assertTrue(battles.contains("battle-1"));
        }

        @Test
        @DisplayName("Multiple battles in same sector")
        void multipleBattlesInSameSector() {
            integration.setBattleLocation("battle-1", 1, 5, 5, "System A");
            integration.setBattleLocation("battle-2", 1, 5, 5, "System B");

            List<String> battles = integration.getBattlesInSector(5, 5);

            assertEquals(2, battles.size());
            assertTrue(battles.contains("battle-1"));
            assertTrue(battles.contains("battle-2"));
        }

        @Test
        @DisplayName("Battles in different sectors are separate")
        void battlesInDifferentSectors() {
            integration.setBattleLocation("battle-1", 1, 5, 5, "System A");
            integration.setBattleLocation("battle-2", 1, 6, 6, "System B");

            assertEquals(1, integration.getBattlesInSector(5, 5).size());
            assertEquals(1, integration.getBattlesInSector(6, 6).size());
            assertTrue(integration.getBattlesInSector(7, 7).isEmpty());
        }

        @Test
        @DisplayName("Adjacent sectors have separate battles")
        void adjacentSectorsSeparate() {
            integration.setBattleLocation("battle-1", 1, 5, 5, "System A");

            assertTrue(integration.getBattlesInSector(5, 5).contains("battle-1"));
            assertTrue(integration.getBattlesInSector(5, 6).isEmpty());
            assertTrue(integration.getBattlesInSector(6, 5).isEmpty());
        }
    }

    // --- Sensor Range Modifier Tests ---

    @Nested
    @DisplayName("Sensor range modifier")
    class SensorRangeTests {

        @Test
        @DisplayName("Null location returns 0 modifier")
        void nullLocationReturnsZero() {
            assertEquals(0, integration.getSensorRangeModifier(null));
        }

        @Test
        @DisplayName("NEBULA terrain reduces sensor range by 2")
        void nebulaReducesSensorRange() {
            integration.setSectorTerrain(5, 5, TerrainEffect.NEBULA);
            SfbBattleLocation location = integration.setBattleLocation(
                    "battle-1", 1, 5, 5, "Nebula System");

            assertEquals(-2, integration.getSensorRangeModifier(location));
        }

        @Test
        @DisplayName("ION_STORM terrain reduces sensor range by 1")
        void ionStormReducesSensorRange() {
            integration.setSectorTerrain(5, 5, TerrainEffect.ION_STORM);
            SfbBattleLocation location = integration.setBattleLocation(
                    "battle-1", 1, 5, 5, "Storm System");

            assertEquals(-1, integration.getSensorRangeModifier(location));
        }

        @Test
        @DisplayName("NONE terrain has no sensor modifier")
        void noneTerrainNoModifier() {
            SfbBattleLocation location = integration.setBattleLocation(
                    "battle-1", 1, 5, 5, "Normal System");

            assertEquals(0, integration.getSensorRangeModifier(location));
        }

        @Test
        @DisplayName("ASTEROID_FIELD terrain has no sensor modifier")
        void asteroidFieldNoSensorModifier() {
            integration.setSectorTerrain(5, 5, TerrainEffect.ASTEROID_FIELD);
            SfbBattleLocation location = integration.setBattleLocation(
                    "battle-1", 1, 5, 5, "Asteroid System");

            assertEquals(0, integration.getSensorRangeModifier(location));
        }

        @Test
        @DisplayName("GRAVITY_WELL terrain has no sensor modifier")
        void gravityWellNoSensorModifier() {
            integration.setSectorTerrain(5, 5, TerrainEffect.GRAVITY_WELL);
            SfbBattleLocation location = integration.setBattleLocation(
                    "battle-1", 1, 5, 5, "Gravity System");

            assertEquals(0, integration.getSensorRangeModifier(location));
        }
    }

    // --- Movement Penalty Tests ---

    @Nested
    @DisplayName("Movement penalty")
    class MovementPenaltyTests {

        @Test
        @DisplayName("Null location returns 0 penalty")
        void nullLocationReturnsZero() {
            assertEquals(0, integration.getMovementPenalty(null));
        }

        @Test
        @DisplayName("ASTEROID_FIELD terrain adds 1 movement penalty")
        void asteroidFieldPenalty() {
            integration.setSectorTerrain(5, 5, TerrainEffect.ASTEROID_FIELD);
            SfbBattleLocation location = integration.setBattleLocation(
                    "battle-1", 1, 5, 5, "Asteroid System");

            assertEquals(1, integration.getMovementPenalty(location));
        }

        @Test
        @DisplayName("GRAVITY_WELL terrain adds 2 movement penalty")
        void gravityWellPenalty() {
            integration.setSectorTerrain(5, 5, TerrainEffect.GRAVITY_WELL);
            SfbBattleLocation location = integration.setBattleLocation(
                    "battle-1", 1, 5, 5, "Gravity System");

            assertEquals(2, integration.getMovementPenalty(location));
        }

        @Test
        @DisplayName("NONE terrain has no movement penalty")
        void noneTerrainNoPenalty() {
            SfbBattleLocation location = integration.setBattleLocation(
                    "battle-1", 1, 5, 5, "Normal System");

            assertEquals(0, integration.getMovementPenalty(location));
        }

        @Test
        @DisplayName("NEBULA terrain has no movement penalty")
        void nebulaNoMovementPenalty() {
            integration.setSectorTerrain(5, 5, TerrainEffect.NEBULA);
            SfbBattleLocation location = integration.setBattleLocation(
                    "battle-1", 1, 5, 5, "Nebula System");

            assertEquals(0, integration.getMovementPenalty(location));
        }

        @Test
        @DisplayName("ION_STORM terrain has no movement penalty")
        void ionStormNoMovementPenalty() {
            integration.setSectorTerrain(5, 5, TerrainEffect.ION_STORM);
            SfbBattleLocation location = integration.setBattleLocation(
                    "battle-1", 1, 5, 5, "Storm System");

            assertEquals(0, integration.getMovementPenalty(location));
        }
    }

    // --- Track Battle Outcome Tests ---

    @Nested
    @DisplayName("Battle outcome tracking")
    class BattleOutcomeTests {

        @Test
        @DisplayName("Track battle outcome does not throw")
        void trackBattleOutcomeNoThrow() {
            assertDoesNotThrow(() -> {
                integration.trackBattleOutcome("battle-1", 0, 10);
            });
        }

        @Test
        @DisplayName("Track multiple battle outcomes")
        void trackMultipleOutcomes() {
            assertDoesNotThrow(() -> {
                integration.trackBattleOutcome("battle-1", 0, 10);
                integration.trackBattleOutcome("battle-2", 1, 15);
                integration.trackBattleOutcome("battle-3", 0, 5);
            });
        }
    }

    // --- Apply Terrain Effects Tests ---

    @Nested
    @DisplayName("Apply terrain effects")
    class ApplyTerrainEffectsTests {

        @Test
        @DisplayName("Apply terrain effects does not throw")
        void applyTerrainEffectsNoThrow() {
            SfbBattleLocation location = new SfbBattleLocation();
            assertDoesNotThrow(() -> {
                integration.applyTerrainEffects(location);
            });
        }

        @Test
        @DisplayName("Apply terrain effects with terrain set")
        void applyTerrainEffectsWithTerrain() {
            integration.setSectorTerrain(5, 5, TerrainEffect.NEBULA);
            SfbBattleLocation location = integration.setBattleLocation(
                    "battle-1", 1, 5, 5, "Nebula System");

            assertDoesNotThrow(() -> {
                integration.applyTerrainEffects(location);
            });
            assertEquals(TerrainEffect.NEBULA, location.getTerrainEffect());
        }
    }

    // --- Integration Scenario Tests ---

    @Nested
    @DisplayName("Integration scenarios")
    class IntegrationScenarios {

        @Test
        @DisplayName("Full battle setup with terrain effects")
        void fullBattleSetup() {
            // Set up galaxy with terrain
            integration.setSectorTerrain(5, 5, TerrainEffect.NEBULA);
            integration.setSectorTerrain(6, 6, TerrainEffect.ASTEROID_FIELD);

            // Set battle locations
            SfbBattleLocation loc1 = integration.setBattleLocation(
                    "battle-1", 1, 5, 5, "Nebula System");
            SfbBattleLocation loc2 = integration.setBattleLocation(
                    "battle-2", 1, 6, 6, "Asteroid System");

            // Verify terrain effects
            assertEquals(TerrainEffect.NEBULA, loc1.getTerrainEffect());
            assertEquals(TerrainEffect.ASTEROID_FIELD, loc2.getTerrainEffect());

            // Verify sensor range modifiers
            assertEquals(-2, integration.getSensorRangeModifier(loc1));
            assertEquals(0, integration.getSensorRangeModifier(loc2));

            // Verify movement penalties
            assertEquals(0, integration.getMovementPenalty(loc1));
            assertEquals(1, integration.getMovementPenalty(loc2));

            // Verify battles in sector
            assertTrue(integration.getBattlesInSector(5, 5).contains("battle-1"));
            assertTrue(integration.getBattlesInSector(6, 6).contains("battle-2"));

            // Track outcome
            assertDoesNotThrow(() -> {
                integration.trackBattleOutcome("battle-1", 0, 10);
            });
        }

        @Test
        @DisplayName("Multi-galaxy battle setup")
        void multiGalaxyBattleSetup() {
            integration.setBattleLocation("battle-1", 1, 5, 5, "Galaxy 1 System");
            integration.setBattleLocation("battle-2", 2, 10, 10, "Galaxy 2 System");

            SfbBattleLocation loc1 = integration.getBattleLocation("battle-1");
            SfbBattleLocation loc2 = integration.getBattleLocation("battle-2");

            assertEquals(1, loc1.getGalaxyId());
            assertEquals(2, loc2.getGalaxyId());
            assertEquals("1-5-5", loc1.getLocationId());
            assertEquals("2-10-10", loc2.getLocationId());
        }
    }
}
