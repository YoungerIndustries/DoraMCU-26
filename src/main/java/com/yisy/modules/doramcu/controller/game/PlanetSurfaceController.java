package com.yisy.modules.doramcu.controller.game;
import com.yisy.modules.doramcu.model.misc.Planet;



import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * REST controller for planet surface data.
 * <p>
 * Provides endpoints for procedural planet rendering with terrain, atmosphere,
 * cities, and facilities data used by the PlanetRenderer frontend module.
 * </p>
 * <p>
 * Endpoints:
 * <ul>
 *   <li>{@code GET /api/v1/planet-surface/{planetId}} — Get planet surface data</li>
 *   <li>{@code GET /api/v1/planet-surface/{planetId}/terrain} — Get terrain grid data</li>
 *   <li>{@code GET /api/v1/planet-surface/{planetId}/facilities} — Get facility list</li>
 *   <li>{@code GET /api/v1/planet-surface/{planetId}/atmosphere} — Get atmosphere data</li>
 * </ul>
 * </p>
 */
@RestController
@RequestMapping("/api/v1/planet-surface")
public class PlanetSurfaceController {

    private final Random random = new Random();

    /**
     * Get complete planet surface data.
     */
    @GetMapping("/{planetId}")
    public ResponseEntity<Map<String, Object>> getPlanetSurface(@PathVariable String planetId) {
        Map<String, Object> result = new HashMap<>();
        result.put("planetId", planetId);

        // Planet basic info
        Map<String, Object> planet = generatePlanetInfo(planetId);
        result.put("planet", planet);

        // Terrain data
        result.put("terrain", generateTerrainData(planetId));

        // Atmosphere data
        result.put("atmosphere", generateAtmosphereData(planetId));

        // Facilities
        result.put("facilities", generateFacilities(planetId));

        // Cloud layers
        result.put("cloudLayers", generateCloudLayers(planetId));

        return ResponseEntity.ok(result);
    }

    /**
     * Get terrain grid data only.
     */
    @GetMapping("/{planetId}/terrain")
    public ResponseEntity<Map<String, Object>> getTerrainData(@PathVariable String planetId,
                                                              @RequestParam(defaultValue = "16") int resolution) {
        return ResponseEntity.ok(generateTerrainData(planetId));
    }

    /**
     * Get facility list only.
     */
    @GetMapping("/{planetId}/facilities")
    public ResponseEntity<List<Map<String, Object>>> getFacilities(@PathVariable String planetId) {
        return ResponseEntity.ok(generateFacilities(planetId));
    }

    /**
     * Get atmosphere data only.
     */
    @GetMapping("/{planetId}/atmosphere")
    public ResponseEntity<Map<String, Object>> getAtmosphereData(@PathVariable String planetId) {
        return ResponseEntity.ok(generateAtmosphereData(planetId));
    }

    /**
     * Generate planet basic information.
     */
    private Map<String, Object> generatePlanetInfo(String planetId) {
        long seed = planetId.hashCode();
        Random rng = new Random(seed);

        String[] planetTypes = {"terran", "desert", "ocean", "ice", "volcanic", "toxic", "gas_giant", "rocky"};
        String[] planetNames = {"Alpha Prime", "Nova Terra", "Cygnus IV", "Andromeda VII",
                "Kepler-22b", "Proxima Centauri b", "Trappist-1e", "Gliese 667Cc"};

        String type = planetTypes[rng.nextInt(planetTypes.length)];
        String name = planetNames[rng.nextInt(planetNames.length)];

        Map<String, Object> planet = new HashMap<>();
        planet.put("id", planetId);
        planet.put("name", name);
        planet.put("type", type);
        planet.put("radius", 6000 + rng.nextDouble() * 4000); // km
        planet.put("gravity", 5 + rng.nextDouble() * 15); // m/s²
        planet.put("temperature", -200 + rng.nextDouble() * 400); // °C
        planet.put("rotationPeriod", 6 + rng.nextDouble() * 40); // hours
        planet.put("orbitalPeriod", 10 + rng.nextDouble() * 600); // days
        planet.put("moons", rng.nextInt(5));
        planet.put("habitable", type.equals("terran") || type.equals("ocean"));

        // Colors based on type
        Map<String, String> colors = new HashMap<>();
        switch (type) {
            case "terran":
                colors.put("land", "#4488ff");
                colors.put("water", "#2266cc");
                colors.put("atmosphere", "#88aaff");
                break;
            case "desert":
                colors.put("land", "#ddaa44");
                colors.put("water", "#886622");
                colors.put("atmosphere", "#ffcc88");
                break;
            case "ocean":
                colors.put("land", "#2266aa");
                colors.put("water", "#114488");
                colors.put("atmosphere", "#6699cc");
                break;
            case "ice":
                colors.put("land", "#aaddff");
                colors.put("water", "#6688aa");
                colors.put("atmosphere", "#ccddff");
                break;
            case "volcanic":
                colors.put("land", "#884422");
                colors.put("water", "#442211");
                colors.put("atmosphere", "#ff6644");
                break;
            case "toxic":
                colors.put("land", "#668844");
                colors.put("water", "#446622");
                colors.put("atmosphere", "#88aa44");
                break;
            default:
                colors.put("land", "#888888");
                colors.put("water", "#666666");
                colors.put("atmosphere", "#aaaaaa");
        }
        planet.put("colors", colors);

        return planet;
    }

    /**
     * Generate terrain grid data.
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> generateTerrainData(String planetId) {
        long seed = planetId.hashCode() + 1;
        Random rng = new Random(seed);

        int gridSize = 16;
        double[][] heightMap = new double[gridSize][gridSize];

        // Simple Perlin-like noise using sine waves
        for (int x = 0; x < gridSize; x++) {
            for (int y = 0; y < gridSize; y++) {
                double nx = x / (double) gridSize;
                double ny = y / (double) gridSize;

                double height = 0;
                height += Math.sin(nx * 6.28 * 2) * Math.cos(ny * 6.28 * 2) * 0.5;
                height += Math.sin(nx * 6.28 * 4 + 1) * Math.cos(ny * 6.28 * 4 + 2) * 0.25;
                height += Math.sin(nx * 6.28 * 8 + 3) * Math.cos(ny * 6.28 * 8 + 4) * 0.125;
                height += (rng.nextDouble() - 0.5) * 0.1;

                heightMap[x][y] = (height + 1) / 2; // Normalize to 0-1
            }
        }

        Map<String, Object> terrain = new HashMap<>();
        terrain.put("gridSize", gridSize);

        List<Map<String, Object>> cells = new ArrayList<>();
        for (int x = 0; x < gridSize; x++) {
            for (int y = 0; y < gridSize; y++) {
                Map<String, Object> cell = new HashMap<>();
                cell.put("x", x);
                cell.put("y", y);
                cell.put("height", heightMap[x][y]);

                // Terrain type based on height
                double h = heightMap[x][y];
                String terrainType;
                if (h < 0.3) terrainType = "deep_water";
                else if (h < 0.4) terrainType = "shallow_water";
                else if (h < 0.45) terrainType = "beach";
                else if (h < 0.6) terrainType = "plains";
                else if (h < 0.75) terrainType = "forest";
                else if (h < 0.85) terrainType = "mountains";
                else terrainType = "peaks";

                cell.put("terrainType", terrainType);
                cells.add(cell);
            }
        }
        terrain.put("cells", cells);

        return terrain;
    }

    /**
     * Generate atmosphere data.
     */
    private Map<String, Object> generateAtmosphereData(String planetId) {
        long seed = planetId.hashCode() + 2;
        Random rng = new Random(seed);

        Map<String, Object> atmosphere = new HashMap<>();
        atmosphere.put("hasAtmosphere", rng.nextDouble() > 0.2);
        atmosphere.put("thickness", 0.05 + rng.nextDouble() * 0.15);
        atmosphere.put("opacity", 0.3 + rng.nextDouble() * 0.5);

        // Composition
        Map<String, Double> composition = new HashMap<>();
        composition.put("nitrogen", 70 + rng.nextDouble() * 20);
        composition.put("oxygen", rng.nextDouble() * 30);
        composition.put("argon", rng.nextDouble() * 5);
        composition.put("co2", rng.nextDouble() * 10);
        atmosphere.put("composition", composition);

        // Weather
        String[] weatherTypes = {"clear", "cloudy", "stormy", "foggy", "rainy", "sandy"};
        atmosphere.put("weather", weatherTypes[rng.nextInt(weatherTypes.length)]);
        atmosphere.put("windSpeed", rng.nextDouble() * 50); // m/s
        atmosphere.put("cloudCover", rng.nextDouble());

        return atmosphere;
    }

    /**
     * Generate facility list.
     */
    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> generateFacilities(String planetId) {
        long seed = planetId.hashCode() + 3;
        Random rng = new Random(seed);

        String[] facilityTypes = {"city", "mine", "factory", "spaceport", "research_station",
                "military_base", "farm", "power_plant", "communication_array", "habitat"};
        String[] facilityNames = {"New Hope", "Starbase Alpha", "Mining Outpost", "Trade Hub",
                "Research Lab", "Fortress", "Green Valley", "Power Core", "Signal Tower", "Dome City"};

        int facilityCount = 3 + rng.nextInt(8);
        List<Map<String, Object>> facilities = new ArrayList<>();

        for (int i = 0; i < facilityCount; i++) {
            String type = facilityTypes[rng.nextInt(facilityTypes.length)];

            Map<String, Object> facility = new HashMap<>();
            facility.put("id", planetId + "_facility_" + i);
            facility.put("name", facilityNames[rng.nextInt(facilityNames.length)] + " " + (i + 1));
            facility.put("type", type);

            // Position on sphere (latitude/longitude)
            facility.put("latitude", -90 + rng.nextDouble() * 180);
            facility.put("longitude", -180 + rng.nextDouble() * 360);

            // Type-specific colors
            switch (type) {
                case "city":
                    facility.put("color", "#4488ff");
                    break;
                case "mine":
                    facility.put("color", "#ff8800");
                    break;
                case "factory":
                    facility.put("color", "#888888");
                    break;
                case "spaceport":
                    facility.put("color", "#ff0000");
                    break;
                case "research_station":
                    facility.put("color", "#00ff00");
                    break;
                case "military_base":
                    facility.put("color", "#ff4444");
                    break;
                case "farm":
                    facility.put("color", "#44cc44");
                    break;
                case "power_plant":
                    facility.put("color", "#ffff00");
                    break;
                case "communication_array":
                    facility.put("color", "#aa44ff");
                    break;
                default:
                    facility.put("color", "#88aacc");
            }

            facility.put("population", (int) (1000 + rng.nextDouble() * 100000));
            facility.put("productionRate", rng.nextDouble() * 100);

            facilities.add(facility);
        }

        return facilities;
    }

    /**
     * Generate cloud layer data.
     */
    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> generateCloudLayers(String planetId) {
        long seed = planetId.hashCode() + 4;
        Random rng = new Random(seed);

        int layerCount = 1 + rng.nextInt(3);
        List<Map<String, Object>> layers = new ArrayList<>();

        for (int i = 0; i < layerCount; i++) {
            Map<String, Object> layer = new HashMap<>();
            layer.put("altitude", 5 + rng.nextDouble() * 15); // km
            layer.put("opacity", 0.2 + rng.nextDouble() * 0.5);
            layer.put("speed", 0.1 + rng.nextDouble() * 0.5);
            layer.put("color", "#ffffff");
            layers.add(layer);
        }

        return layers;
    }
}
