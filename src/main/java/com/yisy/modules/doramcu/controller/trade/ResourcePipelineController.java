package com.yisy.modules.doramcu.controller.trade;
import com.yisy.modules.doramcu.model.galaxy.Asteroid;
import com.yisy.modules.doramcu.model.galaxy.Galaxy;
import com.yisy.modules.doramcu.model.galaxy.Moon;
import com.yisy.modules.doramcu.model.misc.Planet;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * REST controller for resource pipeline visualization data.
 * <p>
 * Provides endpoints for rendering mining operations, transport routes,
 * and processing facilities in the 3D galaxy view.
 * </p>
 * <p>
 * Endpoints:
 * <ul>
 *   <li>{@code GET /api/v1/resource-pipeline/{galaxyId}} — Get full pipeline data</li>
 *   <li>{@code GET /api/v1/resource-pipeline/{galaxyId}/routes} — Get transport routes</li>
 *   <li>{@code GET /api/v1/resource-pipeline/{galaxyId}/mining} — Get mining operations</li>
 *   <li>{@code GET /api/v1/resource-pipeline/{galaxyId}/facilities} — Get processing facilities</li>
 * </ul>
 * </p>
 */
@RestController
@RequestMapping("/api/v1/resource-pipeline")
public class ResourcePipelineController {

    private final Random random = new Random();

    /**
     * Get complete resource pipeline data for a galaxy.
     */
    @GetMapping("/{galaxyId}")
    public ResponseEntity<Map<String, Object>> getPipelineData(@PathVariable String galaxyId) {
        Map<String, Object> result = new HashMap<>();
        result.put("galaxyId", galaxyId);
        result.put("routes", generateTransportRoutes(galaxyId));
        result.put("miningOperations", generateMiningOperations(galaxyId));
        result.put("processingFacilities", generateProcessingFacilities(galaxyId));
        result.put("statistics", generateStatistics(galaxyId));
        return ResponseEntity.ok(result);
    }

    /**
     * Get transport routes only.
     */
    @GetMapping("/{galaxyId}/routes")
    public ResponseEntity<List<Map<String, Object>>> getRoutes(@PathVariable String galaxyId) {
        return ResponseEntity.ok(generateTransportRoutes(galaxyId));
    }

    /**
     * Get mining operations only.
     */
    @GetMapping("/{galaxyId}/mining")
    public ResponseEntity<List<Map<String, Object>>> getMiningOperations(@PathVariable String galaxyId) {
        return ResponseEntity.ok(generateMiningOperations(galaxyId));
    }

    /**
     * Get processing facilities only.
     */
    @GetMapping("/{galaxyId}/facilities")
    public ResponseEntity<List<Map<String, Object>>> getFacilities(@PathVariable String galaxyId) {
        return ResponseEntity.ok(generateProcessingFacilities(galaxyId));
    }

    /**
     * Generate transport routes between celestial bodies.
     */
    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> generateTransportRoutes(String galaxyId) {
        long seed = galaxyId.hashCode();
        Random rng = new Random(seed);

        String[] resourceTypes = {"iron", "copper", "gold", "titanium", "uranium",
                "water", "hydrogen", "helium", "crystals", "organic"};
        String[] bodyNames = {"Alpha Prime", "Nova Terra", "Cygnus IV", "Andromeda VII",
                "Kepler-22b", "Proxima Centauri b", "Trappist-1e", "Gliese 667Cc",
                "Mining Station A", "Processing Hub B", "Trade Outpost C"};

        int routeCount = 5 + rng.nextInt(10);
        List<Map<String, Object>> routes = new ArrayList<>();

        for (int i = 0; i < routeCount; i++) {
            String resourceType = resourceTypes[rng.nextInt(resourceTypes.length)];

            Map<String, Object> route = new HashMap<>();
            route.put("id", galaxyId + "_route_" + i);
            route.put("resourceType", resourceType);
            route.put("volume", 100 + rng.nextDouble() * 900);
            route.put("speed", 0.5 + rng.nextDouble() * 2);
            route.put("status", rng.nextDouble() > 0.1 ? "active" : "inactive");

            // Source body
            Map<String, Object> source = new HashMap<>();
            source.put("id", "body_source_" + i);
            source.put("name", bodyNames[rng.nextInt(bodyNames.length)]);
            source.put("type", rng.nextDouble() > 0.5 ? "planet" : "station");
            Map<String, Object> sourcePos = new HashMap<>();
            sourcePos.put("x", rng.nextDouble() * 300 - 150);
            sourcePos.put("y", rng.nextDouble() * 80 - 40);
            sourcePos.put("z", rng.nextDouble() * 300 - 150);
            source.put("position", sourcePos);
            route.put("source", source);

            // Destination body
            Map<String, Object> destination = new HashMap<>();
            destination.put("id", "body_dest_" + i);
            destination.put("name", bodyNames[rng.nextInt(bodyNames.length)]);
            destination.put("type", rng.nextDouble() > 0.5 ? "planet" : "station");
            Map<String, Object> destPos = new HashMap<>();
            destPos.put("x", rng.nextDouble() * 300 - 150);
            destPos.put("y", rng.nextDouble() * 80 - 40);
            destPos.put("z", rng.nextDouble() * 300 - 150);
            destination.put("position", destPos);
            route.put("destination", destination);

            // Transport ships on this route
            int shipsOnRoute = 1 + rng.nextInt(5);
            route.put("shipsOnRoute", shipsOnRoute);

            // Route color based on resource type
            String color;
            switch (resourceType) {
                case "iron": color = "#888888"; break;
                case "copper": color = "#cc8844"; break;
                case "gold": color = "#ffcc00"; break;
                case "titanium": color = "#8888cc"; break;
                case "uranium": color = "#88ff00"; break;
                case "water": color = "#4488ff"; break;
                case "hydrogen": color = "#aaccff"; break;
                case "helium": color = "#ffaacc"; break;
                case "crystals": color = "#cc44ff"; break;
                case "organic": color = "#44cc44"; break;
                default: color = "#aaaaaa";
            }
            route.put("color", color);

            routes.add(route);
        }

        return routes;
    }

    /**
     * Generate mining operations at celestial bodies.
     */
    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> generateMiningOperations(String galaxyId) {
        long seed = galaxyId.hashCode() + 1;
        Random rng = new Random(seed);

        String[] resourceTypes = {"iron", "copper", "gold", "titanium", "uranium", "crystals"};
        String[] bodyNames = {"Asteroid Belt Alpha", "Mining World Beta", "Resource Moon Gamma",
                "Deep Space Mine Delta", "Orbital Platform Epsilon"};

        int opCount = 3 + rng.nextInt(7);
        List<Map<String, Object>> operations = new ArrayList<>();

        for (int i = 0; i < opCount; i++) {
            String resourceType = resourceTypes[rng.nextInt(resourceTypes.length)];

            Map<String, Object> operation = new HashMap<>();
            operation.put("id", galaxyId + "_mining_" + i);
            operation.put("name", bodyNames[rng.nextInt(bodyNames.length)]);
            operation.put("resourceType", resourceType);
            operation.put("productionRate", 10 + rng.nextDouble() * 90);
            operation.put("capacity", 100 + rng.nextDouble() * 400);
            operation.put("status", rng.nextDouble() > 0.15 ? "active" : "maintenance");

            // Position
            Map<String, Object> position = new HashMap<>();
            position.put("x", rng.nextDouble() * 300 - 150);
            position.put("y", rng.nextDouble() * 80 - 40);
            position.put("z", rng.nextDouble() * 300 - 150);
            operation.put("position", position);

            // Particle effect color
            String color;
            switch (resourceType) {
                case "iron": color = "#888888"; break;
                case "copper": color = "#cc8844"; break;
                case "gold": color = "#ffcc00"; break;
                case "titanium": color = "#8888cc"; break;
                case "uranium": color = "#88ff00"; break;
                case "crystals": color = "#cc44ff"; break;
                default: color = "#aaaaaa";
            }
            operation.put("particleColor", color);

            operations.add(operation);
        }

        return operations;
    }

    /**
     * Generate processing facilities.
     */
    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> generateProcessingFacilities(String galaxyId) {
        long seed = galaxyId.hashCode() + 2;
        Random rng = new Random(seed);

        String[] facilityTypes = {"refinery", "fabrication", "assembly", "research", "storage"};
        String[] facilityNames = {"Central Refinery", "Fabrication Hub", "Assembly Plant",
                "Research Station", "Storage Depot", "Processing Complex"};

        int facilityCount = 2 + rng.nextInt(6);
        List<Map<String, Object>> facilities = new ArrayList<>();

        for (int i = 0; i < facilityCount; i++) {
            String type = facilityTypes[rng.nextInt(facilityTypes.length)];

            Map<String, Object> facility = new HashMap<>();
            facility.put("id", galaxyId + "_facility_" + i);
            facility.put("name", facilityNames[rng.nextInt(facilityNames.length)] + " " + (i + 1));
            facility.put("type", type);
            facility.put("productionRate", 20 + rng.nextDouble() * 80);
            facility.put("efficiency", 0.5 + rng.nextDouble() * 0.5);
            facility.put("status", rng.nextDouble() > 0.1 ? "operational" : "offline");

            // Position
            Map<String, Object> position = new HashMap<>();
            position.put("x", rng.nextDouble() * 300 - 150);
            position.put("y", rng.nextDouble() * 80 - 40);
            position.put("z", rng.nextDouble() * 300 - 150);
            facility.put("position", position);

            // Type-specific color
            String color;
            switch (type) {
                case "refinery": color = "#ff8800"; break;
                case "fabrication": color = "#4488ff"; break;
                case "assembly": color = "#44ff44"; break;
                case "research": color = "#aa44ff"; break;
                case "storage": color = "#888888"; break;
                default: color = "#aaaaaa";
            }
            facility.put("color", color);

            // Input/output resources
            List<String> inputs = new ArrayList<>();
            List<String> outputs = new ArrayList<>();

            String[] allResources = {"iron", "copper", "gold", "titanium", "uranium",
                    "water", "hydrogen", "helium", "crystals", "organic"};

            int inputCount = 1 + rng.nextInt(3);
            for (int j = 0; j < inputCount; j++) {
                inputs.add(allResources[rng.nextInt(allResources.length)]);
            }
            int outputCount = 1 + rng.nextInt(2);
            for (int j = 0; j < outputCount; j++) {
                outputs.add(allResources[rng.nextInt(allResources.length)]);
            }

            facility.put("inputs", inputs);
            facility.put("outputs", outputs);

            facilities.add(facility);
        }

        return facilities;
    }

    /**
     * Generate pipeline statistics.
     */
    private Map<String, Object> generateStatistics(String galaxyId) {
        long seed = galaxyId.hashCode() + 3;
        Random rng = new Random(seed);

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalRoutes", 5 + rng.nextInt(10));
        stats.put("activeRoutes", 3 + rng.nextInt(8));
        stats.put("totalMiningOperations", 3 + rng.nextInt(7));
        stats.put("activeMiningOperations", 2 + rng.nextInt(6));
        stats.put("totalFacilities", 2 + rng.nextInt(6));
        stats.put("operationalFacilities", 1 + rng.nextInt(5));
        stats.put("totalProductionRate", 100 + rng.nextDouble() * 400);
        stats.put("pipelineEfficiency", 0.6 + rng.nextDouble() * 0.35);

        // Resource breakdown
        Map<String, Double> resourceBreakdown = new HashMap<>();
        resourceBreakdown.put("iron", rng.nextDouble() * 100);
        resourceBreakdown.put("copper", rng.nextDouble() * 50);
        resourceBreakdown.put("gold", rng.nextDouble() * 20);
        resourceBreakdown.put("titanium", rng.nextDouble() * 30);
        resourceBreakdown.put("uranium", rng.nextDouble() * 10);
        resourceBreakdown.put("water", rng.nextDouble() * 80);
        stats.put("resourceBreakdown", resourceBreakdown);

        return stats;
    }
}
