package com.yisy.modules.doramcu.thirdreich.service;

import com.yisy.modules.doramcu.earth2026.model.Earth2026BattleEnvironment;
import com.yisy.modules.doramcu.thirdreich.domain.BattlefieldMap;
import com.yisy.modules.doramcu.thirdreich.domain.UnitPlacement;

import java.util.*;

/**
 * Exports a BattlefieldMap as a structured JSON scene graph for real-time 3D clients.
 * <p>
 * The scene graph contains nodes for terrain, buildings, units, and environmental
 * effects, each with transforms (position, rotation, scale) and metadata.
 * </p>
 */
public class SceneGraphExporter {

    /**
     * Exports the battlefield map to a scene graph.
     *
     * @param map the battlefield map
     * @return a Map representing the JSON scene graph
     */
    public Map<String, Object> exportSceneGraph(BattlefieldMap map) {
        Map<String, Object> scene = new LinkedHashMap<>();

        // Scene metadata
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("id", map.getId());
        metadata.put("name", map.getName());
        metadata.put("location", map.getLocationName());
        metadata.put("city", map.getCity());
        metadata.put("country", map.getCountry());
        metadata.put("terrainType", map.getTerrainType().name());
        metadata.put("techLevel", map.getBaseTechLevel());
        metadata.put("timePeriod", map.getTimePeriod());
        scene.put("metadata", metadata);

        // Camera settings
        Map<String, Object> camera = new LinkedHashMap<>();
        camera.put("position", List.of(200.0, 200.0, 300.0));
        camera.put("target", List.of(200.0, 200.0, 0.0));
        camera.put("fov", 60.0);
        scene.put("camera", camera);

        // Lighting based on biosphere
        Map<String, Object> lighting = new LinkedHashMap<>();
        if (map.getBiosphereContext() != null) {
            boolean isNight = map.getBiosphereContext().isNight();
            lighting.put("ambient", isNight ? 0.2 : 0.6);
            lighting.put("directional", isNight ? 0.1 : 0.8);
            lighting.put("sunAngle", map.getBiosphereContext().getTimeOfDay() * 180.0);
            lighting.put("fogDensity", calculateFogDensity(map));
        } else {
            lighting.put("ambient", 0.6);
            lighting.put("directional", 0.8);
            lighting.put("sunAngle", 90.0);
            lighting.put("fogDensity", 0.0);
        }
        scene.put("lighting", lighting);

        // Terrain node
        Map<String, Object> terrain = exportTerrain(map);
        scene.put("terrain", terrain);

        // Building nodes
        List<Map<String, Object>> buildings = exportBuildings(map);
        scene.put("buildings", buildings);

        // Unit nodes
        List<Map<String, Object>> attackerUnits = exportUnits(map.getAttackerPlacements(), "attacker");
        List<Map<String, Object>> defenderUnits = exportUnits(map.getDefenderPlacements(), "defender");
        scene.put("attackerUnits", attackerUnits);
        scene.put("defenderUnits", defenderUnits);

        // Biosphere effects
        Map<String, Object> biosphere = exportBiosphere(map);
        scene.put("biosphere", biosphere);

        return scene;
    }

    private Map<String, Object> exportTerrain(BattlefieldMap map) {
        Map<String, Object> terrain = new LinkedHashMap<>();
        terrain.put("type", "terrain");

        if (map.getTerrainElevationMap() != null) {
            var elevationMap = map.getTerrainElevationMap();
            terrain.put("width", elevationMap.getWidth());
            terrain.put("height", elevationMap.getHeight());
            terrain.put("cellSize", elevationMap.getCellSizeMeters());
            terrain.put("baseElevation", elevationMap.getBaseElevation());

            // Export elevation data as flat array
            List<Double> elevations = new ArrayList<>();
            for (int row = 0; row < elevationMap.getHeight(); row++) {
                for (int col = 0; col < elevationMap.getWidth(); col++) {
                    elevations.add(elevationMap.getAbsoluteElevation(row, col));
                }
            }
            terrain.put("elevations", elevations);
        }

        terrain.put("transform", Map.of(
                "position", List.of(0.0, 0.0, 0.0),
                "rotation", List.of(0.0, 0.0, 0.0),
                "scale", List.of(1.0, 1.0, 1.0)
        ));

        return terrain;
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> exportBuildings(BattlefieldMap map) {
        List<Map<String, Object>> buildings = new ArrayList<>();

        for (Earth2026BattleEnvironment.StructureInfo structure : map.getStructures()) {
            Map<String, Object> building = new LinkedHashMap<>();
            building.put("id", "structure-" + structure.getId());
            building.put("name", structure.getName());
            building.put("type", structure.getStructureType());
            building.put("techLevel", structure.getTechLevel());
            building.put("defenseRating", structure.getDefenseRating());
            building.put("providesCover", structure.isProvidesCover());
            building.put("coverBonus", structure.getCoverBonus());
            building.put("isStrategic", structure.isStrategic());

            // Position from 3D coordinates
            double x = structure.getX() != null ? structure.getX() : 0.0;
            double y = structure.getY() != null ? structure.getY() : 0.0;
            double z = structure.getZ() != null ? structure.getZ() : 0.0;

            // Dimensions
            double width = structure.getBuildingWidth() != null ? structure.getBuildingWidth() : 10.0;
            double depth = structure.getBuildingDepth() != null ? structure.getBuildingDepth() : 10.0;
            double height = structure.getBuildingHeight() != null ? structure.getBuildingHeight() : 10.0;

            building.put("transform", Map.of(
                    "position", List.of(x, y, z),
                    "rotation", List.of(0.0, 0.0, 0.0),
                    "scale", List.of(width, depth, height)
            ));

            // Mesh reference based on structure type
            building.put("mesh", determineBuildingMesh(structure.getStructureType()));

            buildings.add(building);
        }

        return buildings;
    }

    private List<Map<String, Object>> exportUnits(List<UnitPlacement> placements, String team) {
        List<Map<String, Object>> units = new ArrayList<>();

        for (UnitPlacement placement : placements) {
            Map<String, Object> unit = new LinkedHashMap<>();
            unit.put("id", placement.getUnit().getId());
            unit.put("name", placement.getUnit().getName());
            unit.put("type", placement.getUnit().getUnitType().name());
            unit.put("team", team);

            // Unit stats
            Map<String, Object> stats = new LinkedHashMap<>();
            stats.put("strength", placement.getUnit().getStrength());
            stats.put("maxStrength", placement.getUnit().getMaxStrength());
            stats.put("morale", placement.getUnit().getMorale());
            stats.put("attack", placement.getUnit().getAttackValue());
            stats.put("defense", placement.getUnit().getDefenseValue());
            stats.put("mobility", placement.getUnit().getMobility());
            unit.put("stats", stats);

            // Position and orientation
            unit.put("transform", Map.of(
                    "position", List.of(placement.getX(), placement.getY(), placement.getZ()),
                    "rotation", List.of(0.0, placement.getFacingDirection(), 0.0),
                    "scale", List.of(1.0, 1.0, 1.0)
            ));

            // Mesh reference based on unit type
            unit.put("mesh", determineUnitMesh(placement.getUnit().getUnitType().name()));

            units.add(unit);
        }

        return units;
    }

    private Map<String, Object> exportBiosphere(BattlefieldMap map) {
        Map<String, Object> biosphere = new LinkedHashMap<>();

        if (map.getBiosphereContext() != null) {
            var ctx = map.getBiosphereContext();
            biosphere.put("climateZone", ctx.getClimateZone());
            biosphere.put("weather", ctx.getWeather() != null ? ctx.getWeather().name() : "CLEAR");
            biosphere.put("temperature", ctx.getTemperature());
            biosphere.put("humidity", ctx.getHumidity());
            biosphere.put("windSpeed", ctx.getWindSpeed());
            biosphere.put("vegetationDensity", ctx.getVegetationDensity());
            biosphere.put("isNight", ctx.isNight());
            biosphere.put("season", ctx.getSeason());

            // Combat modifiers
            biosphere.put("modifiers", ctx.getCombatModifiers());
        } else {
            biosphere.put("climateZone", "TEMPERATE");
            biosphere.put("weather", "CLEAR");
            biosphere.put("modifiers", Map.of());
        }

        return biosphere;
    }

    private double calculateFogDensity(BattlefieldMap map) {
        if (map.getBiosphereContext() == null) return 0.0;

        var ctx = map.getBiosphereContext();
        double visibility = ctx.getAtmosphericVisibility();

        // Fog density inversely proportional to visibility
        if (visibility < 100) return 0.05;
        if (visibility < 500) return 0.02;
        if (visibility < 1000) return 0.01;
        if (visibility < 5000) return 0.005;
        return 0.0;
    }

    private String determineBuildingMesh(String structureType) {
        if (structureType == null) return "box";
        switch (structureType.toLowerCase()) {
            case "bunker":
            case "command center":
                return "box";
            case "airfield":
                return "plane";
            case "port":
            case "harbor":
                return "dock";
            case "bridge":
                return "bridge";
            case "factory":
                return "industrial";
            case "hospital":
                return "building";
            default:
                return "box";
        }
    }

    private String determineUnitMesh(String unitType) {
        if (unitType == null) return "infantry";
        switch (unitType.toLowerCase()) {
            case "infantry":
                return "infantry";
            case "mechanized infantry":
                return "apc";
            case "tank":
                return "tank";
            case "artillery":
                return "artillery";
            case "anti-air":
                return "aa_gun";
            case "reconnaissance":
                return "recon";
            case "engineer":
                return "engineer";
            case "close air support from aircraft":
                return "aircraft";
            default:
                return "infantry";
        }
    }
}
