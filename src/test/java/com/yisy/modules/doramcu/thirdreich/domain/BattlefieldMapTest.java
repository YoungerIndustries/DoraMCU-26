package com.yisy.modules.doramcu.thirdreich.domain;

import com.yisy.modules.doramcu.earth2026.model.BiosphereContext;
import com.yisy.modules.doramcu.earth2026.model.Earth2026BattleEnvironment;
import com.yisy.modules.doramcu.earth2026.model.TerrainElevationMap;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("BattlefieldMap Tests")
class BattlefieldMapTest {

    private TerrainElevationMap createElevationMap() {
        return TerrainElevationMap.generateRandom(20, 20, 10.0, 100.0, "URBAN", 0.3);
    }

    private BiosphereContext createBiosphere() {
        return BiosphereContext.builder()
                .climateZone("TEMPERATE")
                .temperature(20.0)
                .season("SUMMER")
                .build();
    }

    private Earth2026BattleEnvironment.StructureInfo createStructure(Long id, Double x, Double y, Double z) {
        return new Earth2026BattleEnvironment.StructureInfo(
                id, "Building " + id, "Bunker", 4, 80, 50, true, 3, true,
                x, y, z, 10.0, 10.0, 10.0);
    }

    private UnitPlacement createPlacement(String id, String name, double x, double y, double z, boolean isAttacker) {
        ThirdReichGroundUnit unit = new ThirdReichGroundUnit(id, name, ThirdReichUnitType.INFANTRY);
        return new UnitPlacement(unit, x, y, z, 90.0, isAttacker);
    }

    @Nested
    @DisplayName("Builder")
    class BuilderTests {

        @Test
        @DisplayName("should build with minimal fields")
        void shouldBuildWithMinimalFields() {
            BattlefieldMap map = BattlefieldMap.builder().build();

            assertNotNull(map.getId());
            assertNull(map.getName());
            assertEquals(TerrainType.PLAINS, map.getTerrainType());
            assertTrue(map.getStructures().isEmpty());
            assertTrue(map.getAttackerPlacements().isEmpty());
            assertTrue(map.getDefenderPlacements().isEmpty());
        }

        @Test
        @DisplayName("should build with all fields")
        void shouldBuildWithAllFields() {
            LocalDateTime now = LocalDateTime.of(2026, 11, 12, 10, 0);
            TerrainElevationMap elevationMap = createElevationMap();
            BiosphereContext biosphere = createBiosphere();
            List<Earth2026BattleEnvironment.StructureInfo> structures = List.of(createStructure(1L, 50.0, 50.0, 0.0));

            BattlefieldMap map = BattlefieldMap.builder()
                    .id("test-map-1")
                    .name("Test Battlefield")
                    .locationId(1L)
                    .locationName("Berlin")
                    .city("Berlin")
                    .country("Germany")
                    .terrainElevationMap(elevationMap)
                    .terrainType(TerrainType.URBAN)
                    .groundElevation(50.0)
                    .biosphereContext(biosphere)
                    .structures(structures)
                    .baseTechLevel(4)
                    .timePeriod("World War II")
                    .createdAt(now)
                    .build();

            assertEquals("test-map-1", map.getId());
            assertEquals("Test Battlefield", map.getName());
            assertEquals(1L, map.getLocationId());
            assertEquals("Berlin", map.getLocationName());
            assertEquals("Berlin", map.getCity());
            assertEquals("Germany", map.getCountry());
            assertEquals(elevationMap, map.getTerrainElevationMap());
            assertEquals(TerrainType.URBAN, map.getTerrainType());
            assertEquals(50.0, map.getGroundElevation());
            assertEquals(biosphere, map.getBiosphereContext());
            assertEquals(1, map.getStructures().size());
            assertEquals(4, map.getBaseTechLevel());
            assertEquals("World War II", map.getTimePeriod());
            assertEquals(now, map.getCreatedAt());
        }
    }

    @Nested
    @DisplayName("Unit Placements")
    class UnitPlacementTests {

        @Test
        @DisplayName("should add and retrieve attacker placements")
        void shouldAddAttackerPlacements() {
            BattlefieldMap map = BattlefieldMap.builder().build();
            UnitPlacement p = createPlacement("u1", "Unit 1", 10.0, 10.0, 0.0, true);
            map.addAttackerPlacement(p);

            assertEquals(1, map.getAttackerPlacements().size());
            assertEquals(1, map.getAttackerUnitCount());
        }

        @Test
        @DisplayName("should add and retrieve defender placements")
        void shouldAddDefenderPlacements() {
            BattlefieldMap map = BattlefieldMap.builder().build();
            UnitPlacement p = createPlacement("u1", "Unit 1", 400.0, 10.0, 0.0, false);
            map.addDefenderPlacement(p);

            assertEquals(1, map.getDefenderPlacements().size());
            assertEquals(1, map.getDefenderUnitCount());
        }

        @Test
        @DisplayName("should remove attacker placement by unit ID")
        void shouldRemoveAttackerPlacement() {
            BattlefieldMap map = BattlefieldMap.builder().build();
            map.addAttackerPlacement(createPlacement("u1", "Unit 1", 10.0, 10.0, 0.0, true));
            map.addAttackerPlacement(createPlacement("u2", "Unit 2", 20.0, 20.0, 0.0, true));

            map.removeAttackerPlacement("u1");

            assertEquals(1, map.getAttackerPlacements().size());
            assertEquals("u2", map.getAttackerPlacements().get(0).getUnit().getId());
        }

        @Test
        @DisplayName("should remove defender placement by unit ID")
        void shouldRemoveDefenderPlacement() {
            BattlefieldMap map = BattlefieldMap.builder().build();
            map.addDefenderPlacement(createPlacement("u1", "Unit 1", 400.0, 10.0, 0.0, false));
            map.addDefenderPlacement(createPlacement("u2", "Unit 2", 410.0, 20.0, 0.0, false));

            map.removeDefenderPlacement("u2");

            assertEquals(1, map.getDefenderPlacements().size());
        }

        @Test
        @DisplayName("should return all placements")
        void shouldReturnAllPlacements() {
            BattlefieldMap map = BattlefieldMap.builder().build();
            map.addAttackerPlacement(createPlacement("u1", "Unit 1", 10.0, 10.0, 0.0, true));
            map.addDefenderPlacement(createPlacement("u2", "Unit 2", 400.0, 10.0, 0.0, false));

            assertEquals(2, map.getAllPlacements().size());
        }
    }

    @Nested
    @DisplayName("Position Occupancy")
    class PositionOccupancyTests {

        @Test
        @DisplayName("should detect occupied position")
        void shouldDetectOccupiedPosition() {
            BattlefieldMap map = BattlefieldMap.builder().build();
            map.addAttackerPlacement(createPlacement("u1", "Unit 1", 10.0, 10.0, 0.0, true));

            assertTrue(map.isPositionOccupied(10.0, 10.0, 5.0));
        }

        @Test
        @DisplayName("should detect unoccupied position")
        void shouldDetectUnoccupiedPosition() {
            BattlefieldMap map = BattlefieldMap.builder().build();
            map.addAttackerPlacement(createPlacement("u1", "Unit 1", 10.0, 10.0, 0.0, true));

            assertFalse(map.isPositionOccupied(200.0, 200.0, 5.0));
        }

        @Test
        @DisplayName("should check both attacker and defender positions")
        void shouldCheckBothTeams() {
            BattlefieldMap map = BattlefieldMap.builder().build();
            map.addAttackerPlacement(createPlacement("u1", "Unit 1", 10.0, 10.0, 0.0, true));
            map.addDefenderPlacement(createPlacement("u2", "Unit 2", 400.0, 10.0, 0.0, false));

            assertTrue(map.isPositionOccupied(400.0, 10.0, 5.0));
        }
    }

    @Nested
    @DisplayName("Structures with Coordinates")
    class StructuresWithCoordinatesTests {

        @Test
        @DisplayName("should filter structures with spatial coordinates")
        void shouldFilterStructuresWithCoordinates() {
            Earth2026BattleEnvironment.StructureInfo withCoords =
                    createStructure(1L, 50.0, 50.0, 0.0);
            Earth2026BattleEnvironment.StructureInfo withoutCoords =
                    new Earth2026BattleEnvironment.StructureInfo(2L, "No Coords", "Bunker", 4, 80, 50, true, 3, true);

            BattlefieldMap map = BattlefieldMap.builder()
                    .structures(List.of(withCoords, withoutCoords))
                    .build();

            List<Earth2026BattleEnvironment.StructureInfo> filtered = map.getStructuresWithCoordinates();
            assertEquals(1, filtered.size());
            assertEquals(1L, filtered.get(0).getId());
        }
    }

    @Nested
    @DisplayName("toString")
    class ToStringTests {

        @Test
        @DisplayName("should include key fields")
        void shouldIncludeKeyFields() {
            BattlefieldMap map = BattlefieldMap.builder()
                    .id("test-1")
                    .name("Test")
                    .locationName("Berlin")
                    .terrainType(TerrainType.URBAN)
                    .build();

            String str = map.toString();
            assertTrue(str.contains("test-1"));
            assertTrue(str.contains("Test"));
            assertTrue(str.contains("Berlin"));
        }
    }
}
