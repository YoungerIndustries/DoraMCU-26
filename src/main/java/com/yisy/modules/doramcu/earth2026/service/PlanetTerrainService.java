package com.yisy.modules.doramcu.earth2026.service;

import com.yisy.modules.doramcu.earth2026.model.Earth2026Location;
import com.yisy.modules.doramcu.earth2026.model.PlanetStructure;
import com.yisy.modules.doramcu.earth2026.model.PlanetTemplate;
import com.yisy.modules.doramcu.earth2026.model.TerrainElevationMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Service for generating planet terrain using hybrid procedural + manual generation.
 * <p>
 * Combines procedural terrain generation based on climate zone data with manually
 * placed planet structures (mines, stores, stations, etc.). This allows both
 * automatic generation of realistic terrain and curation of specific locations.
 * </p>
 * <p>
 * Key features:
 * - Procedural heightmaps from climate zone data
 * - Manual structure overlay on terrain grid
 * - Mine placement suggestions based on terrain analysis
 * - Integration with BiosphereService for climate data
 * </p>
 */
@Service
public class PlanetTerrainService {

    private static final Logger log = LoggerFactory.getLogger(PlanetTerrainService.class);
    private static final Random random = new Random(42);

    private final BiosphereService biosphereService;

    public PlanetTerrainService(BiosphereService biosphereService) {
        this.biosphereService = biosphereService;
    }

    /**
     * Generate terrain for a planet with manually placed structures.
     *
     * @param planetTemplate the planet blueprint
     * @param structures list of manually placed structures
     * @param width grid width in cells
     * @param height grid height in cells
     * @param cellSizeMeters size of each cell in meters
     * @return generated terrain elevation map with structures overlaid
     */
    public TerrainElevationMap generatePlanetTerrain(PlanetTemplate planetTemplate,
                                                      List<PlanetStructure> structures,
                                                      int width, int height,
                                                      double cellSizeMeters) {
        if (width <= 0 || height <= 0) {
            log.warn("Invalid terrain dimensions: {}x{}", width, height);
            return new TerrainElevationMap(10, 10, cellSizeMeters, 0.0);
        }

        // Determine base elevation and terrain type from planet template
        double baseElevation = 0.0;
        String terrainType = "URBAN";
        double vegetationDensity = 0.3;

        if (planetTemplate != null) {
            baseElevation = planetTemplate.getBaseElevation() != null ? planetTemplate.getBaseElevation() : 0.0;
            terrainType = planetTemplate.getTerrainType() != null ? planetTemplate.getTerrainType() : "URBAN";
            vegetationDensity = planetTemplate.getVegetationDensity() != null ?
                    planetTemplate.getVegetationDensity() : 0.3;
        }

        log.info("Generating terrain for planet: {} ({}x{}, {}m cells)",
                planetTemplate != null ? planetTemplate.getName() : "Unknown",
                width, height, cellSizeMeters);

        // Generate procedural base terrain
        TerrainElevationMap terrain = TerrainElevationMap.generateRandom(
                width, height, cellSizeMeters, baseElevation, terrainType, vegetationDensity);

        // Overlay manually placed structures
        if (structures != null && !structures.isEmpty()) {
            overlayStructures(terrain, structures);
        }

        log.info("Generated terrain: {}", terrain);
        return terrain;
    }

    /**
     * Generate terrain for a location using biosphere data.
     *
     * @param location the Earth location
     * @param width grid width in cells
     * @param height grid height in cells
     * @param cellSizeMeters size of each cell in meters
     * @return generated terrain elevation map
     */
    public TerrainElevationMap generateLocationTerrain(Earth2026Location location,
                                                        int width, int height,
                                                        double cellSizeMeters) {
        if (location == null) {
            log.warn("No location provided, generating default terrain");
            return TerrainElevationMap.generateRandom(width, height, cellSizeMeters, 0.0, "URBAN", 0.3);
        }

        // Determine terrain type from location
        String terrainType = determineTerrainType(location);
        double baseElevation = location.getLatitude() != null ?
                Math.abs(location.getLatitude()) * 10.0 : 0.0;
        double vegetationDensity = location.getVegetationDensity() != null ?
                location.getVegetationDensity() : 0.3;

        log.info("Generating terrain for location: {} (type: {})", location.getName(), terrainType);
        return TerrainElevationMap.generateRandom(width, height, cellSizeMeters, baseElevation,
                terrainType, vegetationDensity);
    }

    /**
     * Analyze terrain to suggest mine locations based on elevation and resources.
     * <p>
     * Mines are best placed in high-elevation areas (mountains, hills) where
     * mineral deposits are more likely to be found.
     * </p>
     *
     * @param location the Earth location to analyze
     * @return list of suggested mine locations with coordinates and resource types
     */
    public List<MineSuggestion> analyzeTerrainForMines(Earth2026Location location) {
        List<MineSuggestion> suggestions = new ArrayList<>();

        if (location == null || location.getLatitude() == null || location.getLongitude() == null) {
            log.warn("Cannot analyze terrain for mines: invalid location");
            return suggestions;
        }

        double baseLat = location.getLatitude();
        double baseLon = location.getLongitude();

        // Determine climate zone for resource type
        String climateZone = biosphereService.determineClimateZone(location);

        // Suggest mine locations based on terrain features
        switch (climateZone.toLowerCase()) {
            case "mountain":
            case "alpine":
                // High elevation areas are good for metal mines
                suggestions.add(new MineSuggestion(
                        "Mountain Iron Mine",
                        baseLat + 0.05, baseLon - 0.03,
                        500.0, "iron_ore",
                        "High elevation area suitable for iron ore extraction"));
                suggestions.add(new MineSuggestion(
                        "Mountain Gold Mine",
                        baseLat + 0.08, baseLon + 0.02,
                        600.0, "gold_ore",
                        "Peak area with potential gold deposits"));
                break;

            case "desert":
                // Desert areas may have copper and uranium
                suggestions.add(new MineSuggestion(
                        "Desert Copper Mine",
                        baseLat - 0.03, baseLon + 0.05,
                        200.0, "copper_ore",
                        "Desert area with copper deposits"));
                break;

            case "coastal":
            case "tropical":
                // Coastal areas may have oil and gas
                suggestions.add(new MineSuggestion(
                        "Coastal Oil Field",
                        baseLat - 0.05, baseLon - 0.02,
                        50.0, "oil",
                        "Coastal area with potential oil reserves"));
                break;

            default:
                // Default suggestions for temperate/urban areas
                suggestions.add(new MineSuggestion(
                        "Urban Quarry",
                        baseLat + 0.03, baseLon + 0.03,
                        100.0, "stone",
                        "Quarry for construction materials"));
                break;
        }

        log.info("Suggested {} mine locations for {}", suggestions.size(), location.getName());
        return suggestions;
    }

    /**
     * Overlay manually placed structures on the terrain grid.
     * <p>
     * Structures modify the terrain elevation and vegetation density at their
     * grid positions to reflect the physical presence of the structure.
     * </p>
     *
     * @param terrain the terrain map to modify
     * @param structures list of structures to overlay
     */
    private void overlayStructures(TerrainElevationMap terrain, List<PlanetStructure> structures) {
        for (PlanetStructure structure : structures) {
            if (!structure.isActive()) {
                continue;
            }

            // Convert lat/lon to grid coordinates
            int row = (int) ((structure.getLatitude() + 90.0) / 180.0 * (terrain.getHeight() - 1));
            int col = (int) ((structure.getLongitude() + 180.0) / 360.0 * (terrain.getWidth() - 1));

            // Clamp to valid range
            row = Math.max(0, Math.min(terrain.getHeight() - 1, row));
            col = Math.max(0, Math.min(terrain.getWidth() - 1, col));

            // Modify terrain based on structure type
            switch (structure.getStructureType()) {
                case MINE:
                    // Mines are typically in elevated areas
                    terrain.setElevation(row, col, Math.max(terrain.getElevation(row, col), 50.0));
                    terrain.setVegetationDensity(row, col, 0.1);
                    break;

                case FACTORY:
                    // Factories flatten the terrain
                    terrain.setElevation(row, col, 0.0);
                    terrain.setVegetationDensity(row, col, 0.0);
                    break;

                case TRANSPORT_STATION:
                    // Stations are on flat ground
                    terrain.setElevation(row, col, 0.0);
                    terrain.setVegetationDensity(row, col, 0.2);
                    break;

                case SPACEPORT:
                    // Spaceports need large flat areas
                    terrain.setElevation(row, col, 0.0);
                    terrain.setVegetationDensity(row, col, 0.0);
                    break;

                case EQUIPMENT_STORE:
                    // Stores are on flat ground
                    terrain.setElevation(row, col, 0.0);
                    terrain.setVegetationDensity(row, col, 0.1);
                    break;

                case RESIDENCE:
                    // Residences have some vegetation
                    terrain.setElevation(row, col, Math.max(terrain.getElevation(row, col), 5.0));
                    terrain.setVegetationDensity(row, col, 0.4);
                    break;
            }

            log.debug("Overlayed structure {} at grid ({}, {})", structure.getName(), row, col);
        }
    }

    /**
     * Determine terrain type from location data.
     */
    private String determineTerrainType(Earth2026Location location) {
        String type = location.getType();
        if (type != null) {
            switch (type.toLowerCase()) {
                case "mountain":
                case "hill":
                    return "MOUNTAIN";
                case "desert":
                    return "DESERT";
                case "forest":
                case "jungle":
                    return "JUNGLE";
                case "swamp":
                    return "SWAMP";
                case "arctic":
                case "tundra":
                    return "ARCTIC";
                case "urban":
                case "city":
                    return "URBAN";
                case "plains":
                case "field":
                    return "PLAINS";
                default:
                    return "URBAN";
            }
        }
        return "URBAN";
    }

    /**
     * Suggestion for mine placement based on terrain analysis.
     */
    public static class MineSuggestion {
        private final String name;
        private final double latitude;
        private final double longitude;
        private final double elevation;
        private final String resourceType;
        private final String description;

        public MineSuggestion(String name, double latitude, double longitude,
                              double elevation, String resourceType, String description) {
            this.name = name;
            this.latitude = latitude;
            this.longitude = longitude;
            this.elevation = elevation;
            this.resourceType = resourceType;
            this.description = description;
        }

        public String getName() {
            return name;
        }

        public double getLatitude() {
            return latitude;
        }

        public double getLongitude() {
            return longitude;
        }

        public double getElevation() {
            return elevation;
        }

        public String getResourceType() {
            return resourceType;
        }

        public String getDescription() {
            return description;
        }

        @Override
        public String toString() {
            return "MineSuggestion{name='" + name + "', resource='" + resourceType +
                    "', lat=" + latitude + ", lon=" + longitude + "}";
        }
    }
}
