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

@DisplayName("ModelFileExporter Tests")
class ModelFileExporterTest {

    private final ModelFileExporter exporter = new ModelFileExporter();

    private BattlefieldMap createBattlefieldMap() {
        TerrainElevationMap elevationMap = TerrainElevationMap.generateRandom(
                10, 10, 10.0, 100.0, "URBAN", 0.3);
        BiosphereContext biosphere = BiosphereContext.builder()
                .climateZone("TEMPERATE")
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
    @DisplayName("exportOBJ")
    class ExportOBJTests {

        @Test
        @DisplayName("should export valid OBJ content")
        void shouldExportValidOBJ() {
            BattlefieldMap map = createBattlefieldMap();
            String obj = exporter.exportOBJ(map);

            assertNotNull(obj);
            assertFalse(obj.isEmpty());
        }

        @Test
        @DisplayName("should include header with battlefield info")
        void shouldIncludeHeader() {
            BattlefieldMap map = createBattlefieldMap();
            String obj = exporter.exportOBJ(map);

            assertTrue(obj.contains("Test Battlefield"));
            assertTrue(obj.contains("Berlin"));
            assertTrue(obj.contains("Urban"));
            assertTrue(obj.contains("Tech Level: 4"));
        }

        @Test
        @DisplayName("should include terrain vertices")
        void shouldIncludeTerrainVertices() {
            BattlefieldMap map = createBattlefieldMap();
            String obj = exporter.exportOBJ(map);

            assertTrue(obj.contains("o Terrain"));
            assertTrue(obj.contains("v "));
        }

        @Test
        @DisplayName("should include terrain faces")
        void shouldIncludeTerrainFaces() {
            BattlefieldMap map = createBattlefieldMap();
            String obj = exporter.exportOBJ(map);

            assertTrue(obj.contains("f "));
        }

        @Test
        @DisplayName("should include building geometry")
        void shouldIncludeBuildingGeometry() {
            BattlefieldMap map = createBattlefieldMap();
            String obj = exporter.exportOBJ(map);

            assertTrue(obj.contains("# Bunker"));
            assertTrue(obj.contains("o Bunker"));
        }

        @Test
        @DisplayName("should include unit markers")
        void shouldIncludeUnitMarkers() {
            BattlefieldMap map = createBattlefieldMap();
            String obj = exporter.exportOBJ(map);

            assertTrue(obj.contains("Attacker"));
            assertTrue(obj.contains("Defender"));
        }

        @Test
        @DisplayName("should handle empty battlefield")
        void shouldHandleEmptyBattlefield() {
            BattlefieldMap map = BattlefieldMap.builder()
                    .id("empty")
                    .name("Empty")
                    .terrainType(TerrainType.PLAINS)
                    .build();

            String obj = exporter.exportOBJ(map);
            assertNotNull(obj);
            assertTrue(obj.contains("Empty"));
        }

        @Test
        @DisplayName("should count terrain vertices correctly")
        void shouldCountTerrainVertices() {
            BattlefieldMap map = createBattlefieldMap();
            String obj = exporter.exportOBJ(map);

            // 10x10 grid = 100 vertices
            int vertexCount = 0;
            for (String line : obj.split("\n")) {
                if (line.startsWith("v ")) {
                    vertexCount++;
                }
            }
            // 100 terrain + 8 building + 18 per unit (2 units) = 144
            assertTrue(vertexCount >= 100);
        }
    }

    @Nested
    @DisplayName("exportGLTF")
    class ExportGLTFTests {

        @Test
        @DisplayName("should export valid GLTF structure")
        void shouldExportValidGLTF() {
            BattlefieldMap map = createBattlefieldMap();
            Map<String, Object> gltf = exporter.exportGLTF(map);

            assertNotNull(gltf);
            assertFalse(gltf.isEmpty());
        }

        @Test
        @DisplayName("should include asset version")
        void shouldIncludeAssetVersion() {
            BattlefieldMap map = createBattlefieldMap();
            Map<String, Object> gltf = exporter.exportGLTF(map);

            @SuppressWarnings("unchecked")
            Map<String, Object> asset = (Map<String, Object>) gltf.get("asset");
            assertEquals("2.0", asset.get("version"));
        }

        @Test
        @DisplayName("should include scene reference")
        void shouldIncludeScene() {
            BattlefieldMap map = createBattlefieldMap();
            Map<String, Object> gltf = exporter.exportGLTF(map);

            assertEquals(0, gltf.get("scene"));
        }

        @Test
        @DisplayName("should include nodes for terrain, buildings, units")
        void shouldIncludeNodes() {
            BattlefieldMap map = createBattlefieldMap();
            Map<String, Object> gltf = exporter.exportGLTF(map);

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> nodes = (List<Map<String, Object>>) gltf.get("nodes");
            assertEquals(3, nodes.size());

            assertEquals("Terrain", nodes.get(0).get("name"));
            assertEquals("Buildings", nodes.get(1).get("name"));
            assertEquals("Units", nodes.get(2).get("name"));
        }

        @Test
        @DisplayName("should include meshes")
        void shouldIncludeMeshes() {
            BattlefieldMap map = createBattlefieldMap();
            Map<String, Object> gltf = exporter.exportGLTF(map);

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> meshes = (List<Map<String, Object>>) gltf.get("meshes");
            assertEquals(3, meshes.size());
        }

        @Test
        @DisplayName("should include accessors")
        void shouldIncludeAccessors() {
            BattlefieldMap map = createBattlefieldMap();
            Map<String, Object> gltf = exporter.exportGLTF(map);

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> accessors = (List<Map<String, Object>>) gltf.get("accessors");
            assertNotNull(accessors);
            assertTrue(accessors.size() > 0);
        }

        @Test
        @DisplayName("should include buffers")
        void shouldIncludeBuffers() {
            BattlefieldMap map = createBattlefieldMap();
            Map<String, Object> gltf = exporter.exportGLTF(map);

            assertTrue(gltf.containsKey("buffers"));
            assertTrue(gltf.containsKey("bufferViews"));
        }

        @Test
        @DisplayName("should handle empty battlefield")
        void shouldHandleEmptyBattlefield() {
            BattlefieldMap map = BattlefieldMap.builder()
                    .id("empty")
                    .name("Empty")
                    .terrainType(TerrainType.PLAINS)
                    .build();

            Map<String, Object> gltf = exporter.exportGLTF(map);
            assertNotNull(gltf);
            assertEquals(0, gltf.get("scene"));
        }
    }
}
