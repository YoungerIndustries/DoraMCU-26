package com.yisy.modules.doramcu.thirdreich.service;

import com.yisy.modules.doramcu.earth2026.model.Earth2026BattleEnvironment;
import com.yisy.modules.doramcu.earth2026.model.TerrainElevationMap;
import com.yisy.modules.doramcu.thirdreich.domain.BattlefieldMap;
import com.yisy.modules.doramcu.thirdreich.domain.UnitPlacement;

import java.io.IOException;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;

/**
 * Generates OBJ and GLTF 3D model files from a BattlefieldMap.
 * <p>
 * These files can be opened in external 3D tools (Blender, Maya) for
 * static visualization and analysis.
 * </p>
 */
public class ModelFileExporter {

    /**
     * Exports the battlefield map as an OBJ file.
     *
     * @param map the battlefield map
     * @return the OBJ file content as a string
     */
    public String exportOBJ(BattlefieldMap map) {
        StringWriter writer = new StringWriter();

        // Header
        writer.write("# Battlefield: " + map.getName() + "\n");
        writer.write("# Location: " + map.getLocationName() + ", " + map.getCity() + "\n");
        writer.write("# Terrain: " + map.getTerrainType() + "\n");
        writer.write("# Tech Level: " + map.getBaseTechLevel() + "\n\n");

        // Terrain mesh
        exportTerrainOBJ(map, writer);

        // Buildings
        exportBuildingsOBJ(map, writer);

        // Unit markers
        exportUnitsOBJ(map, writer);

        return writer.toString();
    }

    /**
     * Exports the battlefield map as a GLTF file (JSON format).
     *
     * @param map the battlefield map
     * @return a Map representing the GLTF JSON structure
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> exportGLTF(BattlefieldMap map) {
        return Map.of(
                "asset", Map.of("version", "2.0", "generator", "DoraMCU BattlefieldExporter"),
                "scene", 0,
                "scenes", List.of(Map.of("name", map.getName(), "nodes", List.of(0, 1, 2))),
                "nodes", List.of(
                        Map.of("name", "Terrain", "mesh", 0),
                        Map.of("name", "Buildings", "mesh", 1),
                        Map.of("name", "Units", "mesh", 2)
                ),
                "meshes", List.of(
                        exportTerrainGLTF(map),
                        exportBuildingsGLTF(map),
                        exportUnitsGLTF(map)
                ),
                "accessors", buildAccessors(map),
                "bufferViews", List.of(Map.of("buffer", 0, "byteOffset", 0, "byteLength", 1024)),
                "buffers", List.of(Map.of("byteLength", 1024))
        );
    }

    private void exportTerrainOBJ(BattlefieldMap map, StringWriter writer) {
        TerrainElevationMap elevationMap = map.getTerrainElevationMap();
        if (elevationMap == null) return;

        writer.write("# Terrain Mesh\n");
        writer.write("o Terrain\n");

        int width = elevationMap.getWidth();
        int height = elevationMap.getHeight();
        double cellSize = elevationMap.getCellSizeMeters();

        // Vertices
        int vertexIndex = 0;
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                double x = col * cellSize;
                double y = row * cellSize;
                double z = elevationMap.getAbsoluteElevation(row, col);
                writer.write(String.format("v %.2f %.2f %.2f\n", x, y, z));
                vertexIndex++;
            }
        }

        // Faces (triangles)
        writer.write("\n# Terrain Faces\n");
        for (int row = 0; row < height - 1; row++) {
            for (int col = 0; col < width - 1; col++) {
                int v0 = row * width + col;
                int v1 = row * width + col + 1;
                int v2 = (row + 1) * width + col;
                int v3 = (row + 1) * width + col + 1;

                // Two triangles per quad
                writer.write(String.format("f %d %d %d\n", v0 + 1, v1 + 1, v2 + 1));
                writer.write(String.format("f %d %d %d\n", v1 + 1, v3 + 1, v2 + 1));
            }
        }
        writer.write("\n");
    }

    @SuppressWarnings("unchecked")
    private void exportBuildingsOBJ(BattlefieldMap map, StringWriter writer) {
        int vertexOffset = 0;

        if (map.getTerrainElevationMap() != null) {
            vertexOffset = map.getTerrainElevationMap().getWidth() * map.getTerrainElevationMap().getHeight();
        }

        for (Earth2026BattleEnvironment.StructureInfo structure : map.getStructures()) {
            writer.write("# " + structure.getName() + "\n");
            writer.write("o " + structure.getName().replace(" ", "_") + "\n");

            double x = structure.getX() != null ? structure.getX() : 0.0;
            double y = structure.getY() != null ? structure.getY() : 0.0;
            double z = structure.getZ() != null ? structure.getZ() : 0.0;
            double w = structure.getBuildingWidth() != null ? structure.getBuildingWidth() : 10.0;
            double d = structure.getBuildingDepth() != null ? structure.getBuildingDepth() : 10.0;
            double h = structure.getBuildingHeight() != null ? structure.getBuildingHeight() : 10.0;

            // Simple box geometry (8 vertices)
            double hx = w / 2.0;
            double hy = d / 2.0;

            // Bottom face
            writer.write(String.format("v %.2f %.2f %.2f\n", x - hx, y - hy, z));
            writer.write(String.format("v %.2f %.2f %.2f\n", x + hx, y - hy, z));
            writer.write(String.format("v %.2f %.2f %.2f\n", x + hx, y + hy, z));
            writer.write(String.format("v %.2f %.2f %.2f\n", x - hx, y + hy, z));
            // Top face
            writer.write(String.format("v %.2f %.2f %.2f\n", x - hx, y - hy, z + h));
            writer.write(String.format("v %.2f %.2f %.2f\n", x + hx, y - hy, z + h));
            writer.write(String.format("v %.2f %.2f %.2f\n", x + hx, y + hy, z + h));
            writer.write(String.format("v %.2f %.2f %.2f\n", x - hx, y + hy, z + h));

            int base = vertexOffset;
            // Faces
            writer.write(String.format("f %d %d %d %d\n", base + 1, base + 2, base + 3, base + 4)); // bottom
            writer.write(String.format("f %d %d %d %d\n", base + 5, base + 6, base + 7, base + 8)); // top
            writer.write(String.format("f %d %d %d %d\n", base + 1, base + 5, base + 6, base + 2)); // front
            writer.write(String.format("f %d %d %d %d\n", base + 2, base + 6, base + 7, base + 3)); // right
            writer.write(String.format("f %d %d %d %d\n", base + 3, base + 7, base + 8, base + 4)); // back
            writer.write(String.format("f %d %d %d %d\n", base + 4, base + 8, base + 5, base + 1)); // left

            vertexOffset += 8;
            writer.write("\n");
        }
    }

    @SuppressWarnings("unchecked")
    private void exportUnitsOBJ(BattlefieldMap map, StringWriter writer) {
        int vertexOffset = 0;

        if (map.getTerrainElevationMap() != null) {
            vertexOffset += map.getTerrainElevationMap().getWidth() * map.getTerrainElevationMap().getHeight();
        }
        vertexOffset += map.getStructures().size() * 8;

        for (UnitPlacement placement : map.getAllPlacements()) {
            String team = placement.isAttacker() ? "Attacker" : "Defender";
            writer.write("# " + team + " " + placement.getUnit().getName() + "\n");
            writer.write("o " + team + "_" + placement.getUnit().getName().replace(" ", "_") + "\n");

            double x = placement.getX();
            double y = placement.getY();
            double z = placement.getZ();

            // Simple cone marker for units
            double radius = 2.0;
            double height = 4.0;
            int segments = 8;

            // Base center
            writer.write(String.format("v %.2f %.2f %.2f\n", x, y, z));
            // Base circle
            for (int i = 0; i < segments; i++) {
                double angle = (2.0 * Math.PI * i) / segments;
                double vx = x + radius * Math.cos(angle);
                double vy = y + radius * Math.sin(angle);
                writer.write(String.format("v %.2f %.2f %.2f\n", vx, vy, z));
            }
            // Top point
            writer.write(String.format("v %.2f %.2f %.2f\n", x, y, z + height));

            int base = vertexOffset;
            // Faces
            for (int i = 0; i < segments; i++) {
                int next = (i + 1) % segments;
                writer.write(String.format("f %d %d %d\n", base + 1, base + i + 2, base + next + 2)); // base
                writer.write(String.format("f %d %d %d\n", base + segments + 1, base + i + 2, base + next + 2)); // side
            }

            vertexOffset += segments + 2;
            writer.write("\n");
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> exportTerrainGLTF(BattlefieldMap map) {
        TerrainElevationMap elevationMap = map.getTerrainElevationMap();
        if (elevationMap == null) {
            return Map.of("primitives", List.of());
        }

        int width = elevationMap.getWidth();
        int height = elevationMap.getHeight();
        int vertexCount = width * height;

        return Map.of(
                "primitives", List.of(Map.of(
                        "attributes", Map.of("POSITION", 0),
                        "indices", 1,
                        "mode", 4 // TRIANGLES
                ))
        );
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> exportBuildingsGLTF(BattlefieldMap map) {
        return Map.of(
                "primitives", List.of(Map.of(
                        "attributes", Map.of("POSITION", 2),
                        "indices", 3,
                        "mode", 4
                ))
        );
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> exportUnitsGLTF(BattlefieldMap map) {
        return Map.of(
                "primitives", List.of(Map.of(
                        "attributes", Map.of("POSITION", 4),
                        "indices", 5,
                        "mode", 4
                ))
        );
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> buildAccessors(BattlefieldMap map) {
        // Simplified accessor definitions
        return List.of(
                Map.of("bufferView", 0, "componentType", 5126, "count", 1000, "type", "VEC3", "name", "TerrainPositions"),
                Map.of("bufferView", 0, "componentType", 5123, "count", 500, "type", "SCALAR", "name", "TerrainIndices"),
                Map.of("bufferView", 0, "componentType", 5126, "count", 500, "type", "VEC3", "name", "BuildingPositions"),
                Map.of("bufferView", 0, "componentType", 5123, "count", 200, "type", "SCALAR", "name", "BuildingIndices"),
                Map.of("bufferView", 0, "componentType", 5126, "count", 200, "type", "VEC3", "name", "UnitPositions"),
                Map.of("bufferView", 0, "componentType", 5123, "count", 100, "type", "SCALAR", "name", "UnitIndices")
        );
    }
}
