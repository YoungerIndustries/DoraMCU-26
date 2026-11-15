package com.yisy.modules.doramcu.thirdreich.service;

import com.yisy.modules.doramcu.earth2026.model.*;
import com.yisy.modules.doramcu.earth2026.service.BiosphereService;
import com.yisy.modules.doramcu.earth2026.service.PlanetService;
import com.yisy.modules.doramcu.thirdreich.domain.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("3D Battlefield End-to-End Integration Tests")
class ThirdReich3DBattlefieldIntegrationTest {

    private final PlanetService planetService = Mockito.mock(PlanetService.class);
    private final BiosphereService biosphereService = Mockito.mock(BiosphereService.class);
    private final BattlefieldMapBuilder mapBuilder = new BattlefieldMapBuilder(planetService, biosphereService);
    private final SceneGraphExporter sceneGraphExporter = new SceneGraphExporter();
    private final ModelFileExporter modelFileExporter = new ModelFileExporter();

    private Earth2026BattleEnvironment createEnvironment(String terrain, int techLevel, String locationName) {
        List<Earth2026BattleEnvironment.StructureInfo> structures = List.of(
                new Earth2026BattleEnvironment.StructureInfo(
                        1L, "Bunker", "Bunker", techLevel, 80, 50, true, 3, true,
                        50.0, 50.0, 0.0, 10.0, 10.0, 10.0),
                new Earth2026BattleEnvironment.StructureInfo(
                        2L, "Factory", "Factory", techLevel, 60, 100, false, 1, false,
                        150.0, 200.0, 5.0, 20.0, 15.0, 12.0)
        );
        TerrainElevationMap elevationMap = TerrainElevationMap.generateRandom(
                20, 20, 10.0, 100.0, terrain, 0.3);
        return new Earth2026BattleEnvironment(
                1L, locationName, "Berlin", "Germany", 52.52, 13.41,
                techLevel, "World War II", terrain,
                Earth2026BattleEnvironment.WeatherCondition.CLEAR,
                structures, List.of(),
                elevationMap, 100.0, 0.3);
    }

    @Nested
    @DisplayName("Full Pipeline: Build → Populate → Export")
    class FullPipelineTests {

        @Test
        @DisplayName("should build battlefield, populate units, and export scene graph")
        void shouldBuildPopulateAndExportSceneGraph() {
            // Arrange
            Earth2026BattleEnvironment env = createEnvironment("URBAN", 4, "Berlin");
            BiosphereContext biosphere = BiosphereContext.builder()
                    .climateZone("TEMPERATE")
                    .temperature(20.0)
                    .season("SUMMER")
                    .build();

            when(planetService.buildBattleEnvironment(anyLong(), anyString(), anyString(), anyString()))
                    .thenReturn(env);
            when(biosphereService.createDefaultBiosphereContext(any()))
                    .thenReturn(biosphere);

            // Act: Build battlefield map
            BattlefieldMap map = mapBuilder.buildBattlefieldMap(
                    1L, "World War II", "URBAN",
                    Earth2026BattleEnvironment.WeatherCondition.CLEAR);

            // Act: Auto-populate forces
            mapBuilder.autoPopulateForces(map, 5, 3);

            // Act: Export scene graph
            Map<String, Object> scene = sceneGraphExporter.exportSceneGraph(map);

            // Assert
            assertNotNull(scene);
            assertEquals("Berlin", ((Map<String, Object>) scene.get("metadata")).get("city"));
            assertEquals(4, ((Map<String, Object>) scene.get("metadata")).get("techLevel"));

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> attackerUnits = (List<Map<String, Object>>) scene.get("attackerUnits");
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> defenderUnits = (List<Map<String, Object>>) scene.get("defenderUnits");
            assertEquals(5, attackerUnits.size());
            assertEquals(3, defenderUnits.size());

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> buildings = (List<Map<String, Object>>) scene.get("buildings");
            assertEquals(2, buildings.size());

            @SuppressWarnings("unchecked")
            Map<String, Object> terrain = (Map<String, Object>) scene.get("terrain");
            assertNotNull(terrain.get("elevations"));
        }

        @Test
        @DisplayName("should build battlefield and export OBJ model")
        void shouldBuildAndExportOBJ() {
            // Arrange
            Earth2026BattleEnvironment env = createEnvironment("URBAN", 4, "Berlin");
            BiosphereContext biosphere = BiosphereContext.builder().build();

            when(planetService.buildBattleEnvironment(anyLong(), anyString(), anyString(), anyString()))
                    .thenReturn(env);
            when(biosphereService.createDefaultBiosphereContext(any()))
                    .thenReturn(biosphere);

            // Act
            BattlefieldMap map = mapBuilder.buildBattlefieldMap(
                    1L, "World War II", "URBAN",
                    Earth2026BattleEnvironment.WeatherCondition.CLEAR);
            mapBuilder.autoPopulateForces(map, 3, 2);

            String obj = modelFileExporter.exportOBJ(map);

            // Assert
            assertNotNull(obj);
            assertTrue(obj.contains("Berlin"));
            assertTrue(obj.contains("o Terrain"));
            assertTrue(obj.contains("o Bunker"));
            assertTrue(obj.contains("Attacker"));
            assertTrue(obj.contains("Defender"));
        }

        @Test
        @DisplayName("should build battlefield and export GLTF model")
        void shouldBuildAndExportGLTF() {
            // Arrange
            Earth2026BattleEnvironment env = createEnvironment("URBAN", 4, "Berlin");
            BiosphereContext biosphere = BiosphereContext.builder().build();

            when(planetService.buildBattleEnvironment(anyLong(), anyString(), anyString(), anyString()))
                    .thenReturn(env);
            when(biosphereService.createDefaultBiosphereContext(any()))
                    .thenReturn(biosphere);

            // Act
            BattlefieldMap map = mapBuilder.buildBattlefieldMap(
                    1L, "World War II", "URBAN",
                    Earth2026BattleEnvironment.WeatherCondition.CLEAR);
            mapBuilder.autoPopulateForces(map, 3, 2);

            Map<String, Object> gltf = modelFileExporter.exportGLTF(map);

            // Assert
            assertNotNull(gltf);
            assertEquals("2.0", ((Map<String, Object>) gltf.get("asset")).get("version"));
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> nodes = (List<Map<String, Object>>) gltf.get("nodes");
            assertEquals(3, nodes.size());
        }
    }

    @Nested
    @DisplayName("Tech Level Variations")
    class TechLevelVariationTests {

        @Test
        @DisplayName("should populate WWII era units for TL4")
        void shouldPopulateWWIIUnits() {
            Earth2026BattleEnvironment env = createEnvironment("URBAN", 4, "Stalingrad");
            BiosphereContext biosphere = BiosphereContext.builder().build();

            when(planetService.buildBattleEnvironment(anyLong(), anyString(), anyString(), anyString()))
                    .thenReturn(env);
            when(biosphereService.createDefaultBiosphereContext(any()))
                    .thenReturn(biosphere);

            BattlefieldMap map = mapBuilder.buildBattlefieldMap(
                    1L, "World War II", "URBAN",
                    Earth2026BattleEnvironment.WeatherCondition.CLEAR);
            mapBuilder.autoPopulateForces(map, 10, 0);

            for (UnitPlacement p : map.getAttackerPlacements()) {
                ThirdReichUnitType type = p.getUnit().getUnitType();
                assertTrue(type == ThirdReichUnitType.INFANTRY ||
                           type == ThirdReichUnitType.TANK ||
                           type == ThirdReichUnitType.ARTILLERY ||
                           type == ThirdReichUnitType.ANTI_AIR ||
                           type == ThirdReichUnitType.ENGINEER,
                        "WWII unit type expected: " + type);
            }
        }

        @Test
        @DisplayName("should populate modern era units for TL5")
        void shouldPopulateModernUnits() {
            Earth2026BattleEnvironment env = createEnvironment("URBAN", 5, "Kyiv");
            BiosphereContext biosphere = BiosphereContext.builder().build();

            when(planetService.buildBattleEnvironment(anyLong(), anyString(), anyString(), anyString()))
                    .thenReturn(env);
            when(biosphereService.createDefaultBiosphereContext(any()))
                    .thenReturn(biosphere);

            BattlefieldMap map = mapBuilder.buildBattlefieldMap(
                    1L, "Modern", "URBAN",
                    Earth2026BattleEnvironment.WeatherCondition.CLEAR);
            mapBuilder.autoPopulateForces(map, 10, 0);

            for (UnitPlacement p : map.getAttackerPlacements()) {
                ThirdReichUnitType type = p.getUnit().getUnitType();
                assertTrue(type == ThirdReichUnitType.INFANTRY ||
                           type == ThirdReichUnitType.MECHANIZED ||
                           type == ThirdReichUnitType.TANK ||
                           type == ThirdReichUnitType.ARTILLERY ||
                           type == ThirdReichUnitType.RECON,
                        "Modern unit type expected: " + type);
            }
        }

        @Test
        @DisplayName("should populate primitive units for TL0")
        void shouldPopulatePrimitiveUnits() {
            Earth2026BattleEnvironment env = createEnvironment("PLAINS", 0, "Ancient Field");
            BiosphereContext biosphere = BiosphereContext.builder().build();

            when(planetService.buildBattleEnvironment(anyLong(), anyString(), anyString(), anyString()))
                    .thenReturn(env);
            when(biosphereService.createDefaultBiosphereContext(any()))
                    .thenReturn(biosphere);

            BattlefieldMap map = mapBuilder.buildBattlefieldMap(
                    1L, "Ancient", "PLAINS",
                    Earth2026BattleEnvironment.WeatherCondition.CLEAR);
            mapBuilder.autoPopulateForces(map, 5, 0);

            for (UnitPlacement p : map.getAttackerPlacements()) {
                assertEquals(ThirdReichUnitType.INFANTRY, p.getUnit().getUnitType());
            }
        }
    }

    @Nested
    @DisplayName("Terrain Variations")
    class TerrainVariationTests {

        @Test
        @DisplayName("should handle mountain terrain")
        void shouldHandleMountainTerrain() {
            Earth2026BattleEnvironment env = createEnvironment("MOUNTAIN", 4, "Alps");
            BiosphereContext biosphere = BiosphereContext.builder().build();

            when(planetService.buildBattleEnvironment(anyLong(), anyString(), anyString(), anyString()))
                    .thenReturn(env);
            when(biosphereService.createDefaultBiosphereContext(any()))
                    .thenReturn(biosphere);

            BattlefieldMap map = mapBuilder.buildBattlefieldMap(
                    1L, "World War II", "MOUNTAIN",
                    Earth2026BattleEnvironment.WeatherCondition.CLEAR);

            assertNotNull(map.getTerrainElevationMap());
            assertEquals(TerrainType.MOUNTAIN, map.getTerrainType());
        }

        @Test
        @DisplayName("should handle desert terrain")
        void shouldHandleDesertTerrain() {
            Earth2026BattleEnvironment env = createEnvironment("DESERT", 4, "North Africa");
            BiosphereContext biosphere = BiosphereContext.builder().build();

            when(planetService.buildBattleEnvironment(anyLong(), anyString(), anyString(), anyString()))
                    .thenReturn(env);
            when(biosphereService.createDefaultBiosphereContext(any()))
                    .thenReturn(biosphere);

            BattlefieldMap map = mapBuilder.buildBattlefieldMap(
                    1L, "World War II", "DESERT",
                    Earth2026BattleEnvironment.WeatherCondition.CLEAR);

            assertNotNull(map.getTerrainElevationMap());
            assertEquals(TerrainType.DESERT, map.getTerrainType());
        }

        @Test
        @DisplayName("should handle jungle terrain")
        void shouldHandleJungleTerrain() {
            Earth2026BattleEnvironment env = createEnvironment("JUNGLE", 4, "Burma");
            BiosphereContext biosphere = BiosphereContext.builder().build();

            when(planetService.buildBattleEnvironment(anyLong(), anyString(), anyString(), anyString()))
                    .thenReturn(env);
            when(biosphereService.createDefaultBiosphereContext(any()))
                    .thenReturn(biosphere);

            BattlefieldMap map = mapBuilder.buildBattlefieldMap(
                    1L, "World War II", "JUNGLE",
                    Earth2026BattleEnvironment.WeatherCondition.CLEAR);

            assertNotNull(map.getTerrainElevationMap());
            assertEquals(TerrainType.JUNGLE, map.getTerrainType());
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCaseTests {

        @Test
        @DisplayName("should handle empty battlefield (no structures)")
        void shouldHandleEmptyBattlefield() {
            TerrainElevationMap elevationMap = TerrainElevationMap.generateRandom(
                    10, 10, 10.0, 100.0, "PLAINS", 0.0);
            Earth2026BattleEnvironment env = new Earth2026BattleEnvironment(
                    1L, "Empty Field", "Nowhere", "Nowhere", 0.0, 0.0,
                    4, "World War II", "PLAINS",
                    Earth2026BattleEnvironment.WeatherCondition.CLEAR,
                    List.of(), List.of(),
                    elevationMap, 100.0, 0.0);
            BiosphereContext biosphere = BiosphereContext.builder().build();

            when(planetService.buildBattleEnvironment(anyLong(), anyString(), anyString(), anyString()))
                    .thenReturn(env);
            when(biosphereService.createDefaultBiosphereContext(any()))
                    .thenReturn(biosphere);

            BattlefieldMap map = mapBuilder.buildBattlefieldMap(
                    1L, "World War II", "PLAINS",
                    Earth2026BattleEnvironment.WeatherCondition.CLEAR);
            mapBuilder.autoPopulateForces(map, 3, 3);

            Map<String, Object> scene = sceneGraphExporter.exportSceneGraph(map);
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> buildings = (List<Map<String, Object>>) scene.get("buildings");
            assertTrue(buildings.isEmpty());
        }

        @Test
        @DisplayName("should handle large battlefield")
        void shouldHandleLargeBattlefield() {
            Earth2026BattleEnvironment env = createEnvironment("URBAN", 4, "Berlin");
            BiosphereContext biosphere = BiosphereContext.builder().build();

            when(planetService.buildBattleEnvironment(anyLong(), anyString(), anyString(), anyString()))
                    .thenReturn(env);
            when(biosphereService.createDefaultBiosphereContext(any()))
                    .thenReturn(biosphere);

            BattlefieldMap map = mapBuilder.buildBattlefieldMap(
                    1L, "World War II", "URBAN",
                    Earth2026BattleEnvironment.WeatherCondition.CLEAR);
            mapBuilder.autoPopulateForces(map, 20, 20);

            assertEquals(20, map.getAttackerUnitCount());
            assertEquals(20, map.getDefenderUnitCount());

            Map<String, Object> scene = sceneGraphExporter.exportSceneGraph(map);
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> attackers = (List<Map<String, Object>>) scene.get("attackerUnits");
            assertEquals(20, attackers.size());
        }

        @Test
        @DisplayName("should handle customization workflow")
        void shouldHandleCustomizationWorkflow() {
            Earth2026BattleEnvironment env = createEnvironment("URBAN", 4, "Berlin");
            BiosphereContext biosphere = BiosphereContext.builder().build();

            when(planetService.buildBattleEnvironment(anyLong(), anyString(), anyString(), anyString()))
                    .thenReturn(env);
            when(biosphereService.createDefaultBiosphereContext(any()))
                    .thenReturn(biosphere);

            BattlefieldMap map = mapBuilder.buildBattlefieldMap(
                    1L, "World War II", "URBAN",
                    Earth2026BattleEnvironment.WeatherCondition.CLEAR);
            mapBuilder.autoPopulateForces(map, 3, 3);

            // Customize: replace first attacker with a tank
            ThirdReichGroundUnit tank = new ThirdReichGroundUnit(
                    "custom-tank", "Custom Tank", ThirdReichUnitType.TANK);
            mapBuilder.customizeForce(map, true, 0, tank);

            // Reposition: move first defender (within terrain grid bounds: 20x20 * 10m = 200m)
            mapBuilder.repositionUnit(map, false, 0, 150.0, 100.0);

            // Validate
            assertEquals("custom-tank", map.getAttackerPlacements().get(0).getUnit().getId());
            assertEquals(150.0, map.getDefenderPlacements().get(0).getX(), 0.01);
            assertTrue(mapBuilder.validateTechLevel(map));
        }
    }
}
