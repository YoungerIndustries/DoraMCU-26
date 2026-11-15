package com.yisy.modules.doramcu.thirdreich.service;

import com.yisy.modules.doramcu.earth2026.model.BiosphereContext;
import com.yisy.modules.doramcu.earth2026.model.Earth2026BattleEnvironment;
import com.yisy.modules.doramcu.earth2026.model.TerrainElevationMap;
import com.yisy.modules.doramcu.thirdreich.domain.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("SceneGraphExporter Tests")
class SceneGraphExporterTest {

    private final SceneGraphExporter exporter = new SceneGraphExporter();

    private BattlefieldMap createBattlefieldMap() {
        TerrainElevationMap elevationMap = TerrainElevationMap.generateRandom(
                10, 10, 10.0, 100.0, "URBAN", 0.3);
        BiosphereContext biosphere = BiosphereContext.builder()
                .climateZone("TEMPERATE")
                .weather(Earth2026BattleEnvironment.WeatherCondition.CLEAR)
                .temperature(20.0)
                .season("SUMMER")
                .build();

        Earth2026BattleEnvironment.StructureInfo structure =
                new Earth2026BattleEnvironment.StructureInfo(
                        1L, "Bunker", "Bunker", 4, 80, 50, true, 3, true,
                        50.0, 50.0, 0.0, 10.0, 10.0, 10.0);

        ThirdReichGroundUnit attackerUnit = new ThirdReichGroundUnit(
                "atk-1", "Attacker Infantry", ThirdReichUnitType.INFANTRY);
        ThirdReichGroundUnit defenderUnit = new ThirdReichGroundUnit(
                "def-1", "Defender Tank", ThirdReichUnitType.TANK);

        UnitPlacement attackerPlacement = new UnitPlacement(
                attackerUnit, 20.0, 20.0, 0.0, 90.0, true);
        UnitPlacement defenderPlacement = new UnitPlacement(
                defenderUnit, 380.0, 20.0, 0.0, 270.0, false);

        return BattlefieldMap.builder()
                .id("test-map")
                .name("Test Battlefield")
                .locationId(1L)
                .locationName("Berlin")
                .city("Berlin")
                .country("Germany")
                .terrainElevationMap(elevationMap)
                .terrainType(TerrainType.URBAN)
                .groundElevation(100.0)
                .biosphereContext(biosphere)
                .structures(List.of(structure))
                .attackerPlacements(List.of(attackerPlacement))
                .defenderPlacements(List.of(defenderPlacement))
                .baseTechLevel(4)
                .timePeriod("World War II")
                .build();
    }

    @Nested
    @DisplayName("exportSceneGraph")
    class ExportSceneGraphTests {

        @Test
        @DisplayName("should export complete scene graph")
        void shouldExportCompleteSceneGraph() {
            BattlefieldMap map = createBattlefieldMap();
            Map<String, Object> scene = exporter.exportSceneGraph(map);

            assertNotNull(scene);
            assertTrue(scene.containsKey("metadata"));
            assertTrue(scene.containsKey("camera"));
            assertTrue(scene.containsKey("lighting"));
            assertTrue(scene.containsKey("terrain"));
            assertTrue(scene.containsKey("buildings"));
            assertTrue(scene.containsKey("attackerUnits"));
            assertTrue(scene.containsKey("defenderUnits"));
            assertTrue(scene.containsKey("biosphere"));
        }

        @Test
        @DisplayName("should include metadata")
        void shouldIncludeMetadata() {
            BattlefieldMap map = createBattlefieldMap();
            Map<String, Object> scene = exporter.exportSceneGraph(map);

            @SuppressWarnings("unchecked")
            Map<String, Object> metadata = (Map<String, Object>) scene.get("metadata");
            assertEquals("test-map", metadata.get("id"));
            assertEquals("Test Battlefield", metadata.get("name"));
            assertEquals("Berlin", metadata.get("location"));
            assertEquals("Germany", metadata.get("country"));
            assertEquals(4, metadata.get("techLevel"));
            assertEquals("World War II", metadata.get("timePeriod"));
        }

        @Test
        @DisplayName("should include camera settings")
        void shouldIncludeCamera() {
            BattlefieldMap map = createBattlefieldMap();
            Map<String, Object> scene = exporter.exportSceneGraph(map);

            @SuppressWarnings("unchecked")
            Map<String, Object> camera = (Map<String, Object>) scene.get("camera");
            assertNotNull(camera.get("position"));
            assertNotNull(camera.get("target"));
            assertEquals(60.0, camera.get("fov"));
        }

        @Test
        @DisplayName("should include lighting based on biosphere")
        void shouldIncludeLighting() {
            BattlefieldMap map = createBattlefieldMap();
            Map<String, Object> scene = exporter.exportSceneGraph(map);

            @SuppressWarnings("unchecked")
            Map<String, Object> lighting = (Map<String, Object>) scene.get("lighting");
            assertNotNull(lighting.get("ambient"));
            assertNotNull(lighting.get("directional"));
            assertNotNull(lighting.get("sunAngle"));
            assertNotNull(lighting.get("fogDensity"));
        }

        @Test
        @DisplayName("should include terrain data")
        void shouldIncludeTerrain() {
            BattlefieldMap map = createBattlefieldMap();
            Map<String, Object> scene = exporter.exportSceneGraph(map);

            @SuppressWarnings("unchecked")
            Map<String, Object> terrain = (Map<String, Object>) scene.get("terrain");
            assertEquals("terrain", terrain.get("type"));
            assertEquals(10, terrain.get("width"));
            assertEquals(10, terrain.get("height"));
            assertEquals(10.0, terrain.get("cellSize"));
            assertEquals(100.0, terrain.get("baseElevation"));

            @SuppressWarnings("unchecked")
            List<Double> elevations = (List<Double>) terrain.get("elevations");
            assertEquals(100, elevations.size()); // 10x10 grid
        }

        @Test
        @DisplayName("should include buildings")
        void shouldIncludeBuildings() {
            BattlefieldMap map = createBattlefieldMap();
            Map<String, Object> scene = exporter.exportSceneGraph(map);

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> buildings = (List<Map<String, Object>>) scene.get("buildings");
            assertEquals(1, buildings.size());

            Map<String, Object> building = buildings.get(0);
            assertEquals("structure-1", building.get("id"));
            assertEquals("Bunker", building.get("name"));
            assertEquals("Bunker", building.get("type"));
            assertEquals(80, building.get("defenseRating"));
            assertTrue((Boolean) building.get("providesCover"));
            assertNotNull(building.get("transform"));
            assertNotNull(building.get("mesh"));
        }

        @Test
        @DisplayName("should include attacker units")
        void shouldIncludeAttackerUnits() {
            BattlefieldMap map = createBattlefieldMap();
            Map<String, Object> scene = exporter.exportSceneGraph(map);

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> units = (List<Map<String, Object>>) scene.get("attackerUnits");
            assertEquals(1, units.size());

            Map<String, Object> unit = units.get(0);
            assertEquals("atk-1", unit.get("id"));
            assertEquals("Attacker Infantry", unit.get("name"));
            assertEquals("attacker", unit.get("team"));
            assertNotNull(unit.get("stats"));
            assertNotNull(unit.get("transform"));
        }

        @Test
        @DisplayName("should include defender units")
        void shouldIncludeDefenderUnits() {
            BattlefieldMap map = createBattlefieldMap();
            Map<String, Object> scene = exporter.exportSceneGraph(map);

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> units = (List<Map<String, Object>>) scene.get("defenderUnits");
            assertEquals(1, units.size());

            Map<String, Object> unit = units.get(0);
            assertEquals("def-1", unit.get("id"));
            assertEquals("Defender Tank", unit.get("name"));
            assertEquals("defender", unit.get("team"));
        }

        @Test
        @DisplayName("should include unit stats")
        void shouldIncludeUnitStats() {
            BattlefieldMap map = createBattlefieldMap();
            Map<String, Object> scene = exporter.exportSceneGraph(map);

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> units = (List<Map<String, Object>>) scene.get("attackerUnits");
            Map<String, Object> unit = units.get(0);

            @SuppressWarnings("unchecked")
            Map<String, Object> stats = (Map<String, Object>) unit.get("stats");
            assertTrue(stats.containsKey("strength"));
            assertTrue(stats.containsKey("maxStrength"));
            assertTrue(stats.containsKey("morale"));
            assertTrue(stats.containsKey("attack"));
            assertTrue(stats.containsKey("defense"));
            assertTrue(stats.containsKey("mobility"));
        }

        @Test
        @DisplayName("should include biosphere data")
        void shouldIncludeBiosphere() {
            BattlefieldMap map = createBattlefieldMap();
            Map<String, Object> scene = exporter.exportSceneGraph(map);

            @SuppressWarnings("unchecked")
            Map<String, Object> biosphere = (Map<String, Object>) scene.get("biosphere");
            assertEquals("TEMPERATE", biosphere.get("climateZone"));
            assertEquals(20.0, biosphere.get("temperature"));
            assertEquals("SUMMER", biosphere.get("season"));
            assertNotNull(biosphere.get("modifiers"));
        }

        @Test
        @DisplayName("should handle map without biosphere")
        void shouldHandleNoBiosphere() {
            BattlefieldMap map = BattlefieldMap.builder()
                    .id("no-biosphere")
                    .name("Empty")
                    .terrainType(TerrainType.PLAINS)
                    .build();

            Map<String, Object> scene = exporter.exportSceneGraph(map);

            @SuppressWarnings("unchecked")
            Map<String, Object> biosphere = (Map<String, Object>) scene.get("biosphere");
            assertEquals("TEMPERATE", biosphere.get("climateZone"));
            assertEquals("CLEAR", biosphere.get("weather"));
        }

        @Test
        @DisplayName("should handle map without terrain elevation")
        void shouldHandleNoTerrainElevation() {
            BattlefieldMap map = BattlefieldMap.builder()
                    .id("no-terrain")
                    .name("Empty")
                    .terrainType(TerrainType.PLAINS)
                    .build();

            Map<String, Object> scene = exporter.exportSceneGraph(map);

            @SuppressWarnings("unchecked")
            Map<String, Object> terrain = (Map<String, Object>) scene.get("terrain");
            assertEquals("terrain", terrain.get("type"));
        }

        @Test
        @DisplayName("should handle empty battlefield")
        void shouldHandleEmptyBattlefield() {
            BattlefieldMap map = BattlefieldMap.builder()
                    .id("empty")
                    .name("Empty Battlefield")
                    .terrainType(TerrainType.PLAINS)
                    .build();

            Map<String, Object> scene = exporter.exportSceneGraph(map);

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> buildings = (List<Map<String, Object>>) scene.get("buildings");
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> attackers = (List<Map<String, Object>>) scene.get("attackerUnits");
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> defenders = (List<Map<String, Object>>) scene.get("defenderUnits");

            assertTrue(buildings.isEmpty());
            assertTrue(attackers.isEmpty());
            assertTrue(defenders.isEmpty());
        }
    }
}
