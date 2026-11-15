package com.yisy.modules.doramcu.earth2026.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yisy.modules.doramcu.earth2026.model.SanDiegoTransitRoute;
import com.yisy.modules.doramcu.earth2026.repository.SanDiegoTransitRouteRepository;
import com.yisy.modules.doramcu.googlemaps.GoogleMapsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service that provides San Diego 3D map data including buildings, terrain,
 * and transit routes between Younger Industries HQ and the Convention Center.
 * <p>
 * Younger Industries HQ is located at 4906 Campo Road, Spring Valley, San Diego.
 * The map covers the route from Spring Valley through Mira Mesa, Scripps Ranch,
 * and Mission Valley to downtown San Diego's Convention Center.
 * </p>
 */
@Service
public class SanDiegoMapService {

    private static final Logger log = LoggerFactory.getLogger(SanDiegoMapService.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    // Real-world coordinates
    public static final double YISY_HQ_LAT = 32.9356;  // 4906 Campo Road, Spring Valley
    public static final double YISY_HQ_LON = -117.0347;
    public static final double SDCC_LAT = 32.7071;     // San Diego Convention Center
    public static final double SDCC_LON = -117.1627;

    private final SanDiegoTransitRouteRepository transitRouteRepository;
    private final GoogleMapsService googleMapsService;

    public SanDiegoMapService(SanDiegoTransitRouteRepository transitRouteRepository,
                              @Nullable GoogleMapsService googleMapsService) {
        this.transitRouteRepository = transitRouteRepository;
        this.googleMapsService = googleMapsService;
    }

    /**
     * Seed the transit routes between Younger Industries HQ and Convention Center.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void seedTransitRoutes() {
        if (transitRouteRepository.count() > 0) {
            log.info("San Diego transit routes already seeded (count: {}), skipping.",
                    transitRouteRepository.count());
            return;
        }

        log.info("Seeding San Diego transit routes...");
        List<SanDiegoTransitRoute> routes = new ArrayList<>();

        // Route 922: Campo Road Bus - Direct route from Spring Valley to Mission Valley
        routes.add(createRoute(
                "Route 922 - Campo Express",
                "922", "Bus",
                "Direct bus from Campo Road (Spring Valley) to Mission Valley Transit Center. " +
                        "Connects Younger Industries HQ to the trolley system.",
                "Younger Industries HQ", "Mission Valley Transit Center",
                YISY_HQ_LAT, YISY_HQ_LON,
                32.7650, -117.1650,
                45, 250,
                Arrays.asList(
                        "4906 Campo Rd, Spring Valley",
                        "Campo Rd & I-8 East",
                        "Mira Mesa Blvd & I-15",
                        "Cottonwood & Mira Mesa",
                        "Scripps Ranch Town Center",
                        "Fletcher & I-8",
                        "Mission Valley Rd & I-8",
                        "Mission Valley Transit Center"
                ),
                generateStopsJson(
                        new double[][]{
                                {32.9356, -117.0347},  // YISY HQ
                                {32.9280, -117.0500},  // Campo & I-8
                                {32.9000, -117.0800},  // Mira Mesa
                                {32.8700, -117.1000},  // Cottonwood
                                {32.8300, -117.1200},  // Scripps Ranch
                                {32.7800, -117.1400},  // Fletcher
                                {32.7650, -117.1550},  // Mission Valley Rd
                                {32.7650, -117.1650}   // MV Transit Center
                        }
                )
        ));

        // Blue Line Trolley: Mission Valley to Convention Center
        routes.add(createRoute(
                "Blue Line Trolley",
                "Blue", "Trolley",
                "Trolley from Mission Valley Transit Center to San Diego Convention Center. " +
                        "Runs through Balboa Park and downtown.",
                "Mission Valley Transit Center", "San Diego Convention Center",
                32.7650, -117.1650,
                SDCC_LAT, SDCC_LON,
                20, 250,
                Arrays.asList(
                        "Mission Valley Transit Center",
                        "5th & Imperial",
                        "Hillcrest",
                        "Balboa Park",
                        "Old Town",
                        "Petco Park",
                        "San Diego Convention Center"
                ),
                generateStopsJson(
                        new double[][]{
                                {32.7650, -117.1650},  // MV Transit Center
                                {32.7580, -117.1600},  // 5th & Imperial
                                {32.7480, -117.1600},  // Hillcrest
                                {32.7340, -117.1450},  // Balboa Park
                                {32.7550, -117.1300},  // Old Town
                                {32.7070, -117.1530},  // Petco Park
                                {32.7071, -117.1627}   // Convention Center
                        }
                )
        ));

        // Route 20: Alternative bus through Mira Mesa
        routes.add(createRoute(
                "Route 20 - Mira Mesa Express",
                "20", "Bus",
                "Express bus from Mira Mesa through Sorrento Valley to La Jolla. " +
                        "Alternative route for characters traveling north.",
                "Mira Mesa", "La Jolla",
                32.9000, -117.0800,
                32.8470, -117.2740,
                55, 250,
                Arrays.asList(
                        "Mira Mesa & I-15",
                        "Sorrento Valley",
                        "UTC Circle",
                        "Torrey Pines",
                        "La Jolla Village"
                ),
                generateStopsJson(
                        new double[][]{
                                {32.9000, -117.0800},  // Mira Mesa
                                {32.8800, -117.1200},  // Sorrento Valley
                                {32.8600, -117.2000},  // UTC
                                {32.8550, -117.2500},  // Torrey Pines
                                {32.8470, -117.2740}   // La Jolla
                        }
                )
        ));

        transitRouteRepository.saveAll(routes);
        log.info("Seeded {} San Diego transit routes.", routes.size());
    }

    /**
     * Get all active transit routes.
     */
    public List<SanDiegoTransitRoute> getAllRoutes() {
        return transitRouteRepository.findByActiveTrue();
    }

    /**
     * Get routes from a specific location.
     */
    public List<SanDiegoTransitRoute> getRoutesFrom(String location) {
        return transitRouteRepository.findByStartLocationContainingAndActiveTrue(location);
    }

    /**
     * Get routes to a specific location.
     */
    public List<SanDiegoTransitRoute> getRoutesTo(String location) {
        return transitRouteRepository.findByEndLocationContainingAndActiveTrue(location);
    }

    /**
     * Get the complete path from Younger Industries HQ to Convention Center.
     * This combines the bus route (922) and trolley route (Blue Line).
     */
    public Map<String, Object> getHqToConventionCenterPath() {
        Map<String, Object> path = new LinkedHashMap<>();
        path.put("name", "Younger Industries HQ to Convention Center");
        path.put("description", "Take Route 922 bus from Campo Road to Mission Valley, " +
                "then transfer to the Blue Line Trolley to reach the Convention Center.");

        // Combined stops
        List<Map<String, Object>> segments = new ArrayList<>();

        // Segment 1: Bus
        Map<String, Object> busSegment = new LinkedHashMap<>();
        busSegment.put("segment", 1);
        busSegment.put("type", "Bus");
        busSegment.put("route", "922");
        busSegment.put("name", "Route 922 - Campo Express");
        busSegment.put("from", "Younger Industries HQ (4906 Campo Rd, Spring Valley)");
        busSegment.put("to", "Mission Valley Transit Center");
        busSegment.put("duration", "45 minutes");
        busSegment.put("fare", "$2.50");
        busSegment.put("stops", Arrays.asList(
                "4906 Campo Rd, Spring Valley",
                "Campo Rd & I-8 East",
                "Mira Mesa Blvd & I-15",
                "Cottonwood & Mira Mesa",
                "Scripps Ranch Town Center",
                "Fletcher & I-8",
                "Mission Valley Rd & I-8",
                "Mission Valley Transit Center"
        ));
        segments.add(busSegment);

        // Segment 2: Trolley
        Map<String, Object> trolleySegment = new LinkedHashMap<>();
        trolleySegment.put("segment", 2);
        trolleySegment.put("type", "Trolley");
        trolleySegment.put("route", "Blue");
        trolleySegment.put("name", "Blue Line Trolley");
        trolleySegment.put("from", "Mission Valley Transit Center");
        trolleySegment.put("to", "San Diego Convention Center");
        trolleySegment.put("duration", "20 minutes");
        trolleySegment.put("fare", "$2.50");
        trolleySegment.put("stops", Arrays.asList(
                "Mission Valley Transit Center",
                "5th & Imperial",
                "Hillcrest",
                "Balboa Park",
                "Old Town",
                "Petco Park",
                "San Diego Convention Center"
        ));
        segments.add(trolleySegment);

        path.put("segments", segments);
        path.put("totalDuration", "65 minutes");
        path.put("totalFare", "$5.00");
        path.put("transferAt", "Mission Valley Transit Center");

        return path;
    }

    /**
     * Get 3D building data for the San Diego map.
     * These are the buildings rendered on the 3D map between HQ and Convention Center.
     */
    public List<Map<String, Object>> get3DBuildings() {
        List<Map<String, Object>> buildings = new ArrayList<>();

        // Younger Industries HQ - the main building
        buildings.add(create3DBuilding(
                "Younger Industries HQ",
                "4906 Campo Road, Spring Valley, CA 91977",
                YISY_HQ_LAT, YISY_HQ_LON,
                45.0, 30.0, 30.0,  // height, width, depth in meters
                0x1a237e,  // Dark blue corporate color
                "Corporate",
                "Headquarters of Younger Industries. CEO Butch Younger and AI VP Sif Younger operate from here. " +
                        "Multi-story modern office building with secure server rooms and AI labs."
        ));

        // San Diego Convention Center
        buildings.add(create3DBuilding(
                "San Diego Convention Center",
                "111 West Harbor Drive, San Diego, CA 92101",
                SDCC_LAT, SDCC_LON,
                25.0, 200.0, 150.0,
                0x0d47a1,  // Blue convention center
                "Landmark",
                "Starting location for new characters. A bustling convention center where characters first arrive."
        ));

        // Mission Valley Transit Center
        buildings.add(create3DBuilding(
                "Mission Valley Transit Center",
                "1610 Camino Del Rio N, San Diego, CA 92108",
                32.7650, -117.1650,
                15.0, 40.0, 30.0,
                0x2e7d32,  // Green transit
                "Transit",
                "Major transit hub connecting bus routes and trolley lines. Transfer point between Route 922 and Blue Line."
        ));

        // Scripps Ranch Town Center
        buildings.add(create3DBuilding(
                "Scripps Ranch Town Center",
                "10450 Scripps Poway Pkwy, San Diego, CA 92131",
                32.8300, -117.1200,
                12.0, 50.0, 40.0,
                0x4527a0,  // Purple shopping
                "Commercial",
                "Shopping and dining center along the Route 922 bus path."
        ));

        // Balboa Park
        buildings.add(create3DBuilding(
                "Balboa Park",
                "1549 El Prado, San Diego, CA 92101",
                32.7340, -117.1450,
                20.0, 100.0, 80.0,
                0x1b5e20,  // Dark green park
                "Park",
                "Large urban park with museums and gardens. The Blue Line Trolley passes through here."
        ));

        // Petco Park
        buildings.add(create3DBuilding(
                "Petco Park",
                "100 Park Blvd, San Diego, CA 92101",
                32.7070, -117.1530,
                35.0, 120.0, 100.0,
                0xbf360c,  // Orange stadium
                "Stadium",
                "Home of the San Diego Padres baseball team. Visible from the Blue Line Trolley."
        ));

        // UTC Circle (alternative route landmark)
        buildings.add(create3DBuilding(
                "UTC Circle",
                "4545 La Jolla Village Dr, San Diego, CA 92122",
                32.8600, -117.2000,
                18.0, 60.0, 50.0,
                0x006064,  // Teal shopping
                "Commercial",
                "University Town Center - major shopping mall on the Route 20 alternative path."
        ));

        // Torrey Pines (scenic landmark)
        buildings.add(create3DBuilding(
                "Torrey Pines State Reserve",
                "12600 N Torrey Pines Rd, La Jolla, CA 92037",
                32.8550, -117.2500,
                50.0, 200.0, 100.0,
                0x33691e,  // Green nature
                "Nature",
                "Coastal cliffs and rare Torrey Pine trees. Scenic landmark visible from Route 20."
        ));

        return buildings;
    }

    /**
     * Get terrain data for the San Diego map area.
     * Uses Google Maps Elevation API for accurate terrain data when available,
     * falling back to hardcoded data when the API is not configured.
     */
    public Map<String, Object> getTerrainData() {
        Map<String, Object> terrain = new LinkedHashMap<>();
        terrain.put("name", "San Diego - Spring Valley to Downtown");
        terrain.put("bounds", Map.of(
                "north", 33.0000,
                "south", 32.7000,
                "east", -117.0000,
                "west", -117.3000
        ));

        // Try to get real elevation data from Google Maps
        if (googleMapsService != null && googleMapsService.isApiAvailable()) {
            try {
                log.info("Fetching terrain elevation data from Google Maps API...");
                Map<String, Object> googleTerrain = googleMapsService.getTerrainGrid(
                        33.0000, 32.7000, -117.3000, -117.0000, 16);
                terrain.put("elevationGrid", googleTerrain.get("elevations"));
                terrain.put("dataSource", "Google Maps Elevation API");
                log.info("Successfully fetched terrain grid from Google Maps API");
            } catch (Exception e) {
                log.warn("Failed to fetch terrain from Google Maps, using fallback data: {}", e.getMessage());
                terrain.put("dataSource", "Fallback (Google Maps unavailable)");
            }
        } else {
            terrain.put("dataSource", "Fallback (Google Maps not configured)");
        }

        // Terrain features (enhanced with Google Maps elevation when available)
        List<Map<String, Object>> features = new ArrayList<>();

        // Spring Valley hills
        features.add(Map.of(
                "name", "Spring Valley Hills",
                "type", "Hills",
                "centerLat", 32.9400,
                "centerLon", -117.0300,
                "radius", 3000,
                "elevation", 150.0,
                "color", 0x4caf50
        ));

        // Mission Bay
        features.add(Map.of(
                "name", "Mission Bay",
                "type", "Water",
                "centerLat", 32.7900,
                "centerLon", -117.2100,
                "radius", 5000,
                "elevation", 0.0,
                "color", 0x2196f3
        ));

        // Pacific Ocean
        features.add(Map.of(
                "name", "Pacific Ocean",
                "type", "Ocean",
                "centerLat", 32.8000,
                "centerLon", -117.3000,
                "radius", 10000,
                "elevation", 0.0,
                "color", 0x1565c0
        ));

        // Downtown San Diego
        features.add(Map.of(
                "name", "Downtown San Diego",
                "type", "Urban",
                "centerLat", 32.7150,
                "centerLon", -117.1600,
                "radius", 3000,
                "elevation", 20.0,
                "color", 0x757575
        ));

        terrain.put("features", features);
        return terrain;
    }

    /**
     * Get elevation data for a specific location in San Diego.
     * Uses Google Maps Elevation API when available.
     *
     * @param lat latitude
     * @param lng longitude
     * @return elevation data or fallback estimate
     */
    public Map<String, Object> getElevationAt(double lat, double lng) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("lat", lat);
        result.put("lng", lng);

        if (googleMapsService != null && googleMapsService.isApiAvailable()) {
            return googleMapsService.getElevation(lat, lng)
                    .orElseGet(() -> {
                        result.put("elevation", estimateElevation(lat, lng));
                        result.put("dataSource", "Estimated");
                        return result;
                    });
        }

        result.put("elevation", estimateElevation(lat, lng));
        result.put("dataSource", "Estimated (Google Maps not configured)");
        return result;
    }

    /**
     * Estimate elevation based on location (fallback when Google Maps is unavailable).
     */
    private double estimateElevation(double lat, double lng) {
        // Spring Valley hills
        if (lat > 32.90 && lat < 32.96 && lng > -117.06 && lng < -117.00) {
            return 150.0;
        }
        // Mira Mesa
        if (lat > 32.86 && lat < 32.92 && lng > -117.12 && lng < -117.06) {
            return 100.0;
        }
        // Mission Valley
        if (lat > 32.74 && lat < 32.78 && lng > -117.18 && lng < -117.14) {
            return 100.0;
        }
        // Downtown San Diego
        if (lat > 32.70 && lat < 32.73 && lng > -117.18 && lng < -117.14) {
            return 20.0;
        }
        // Default coastal elevation
        return 50.0;
    }

    /**
     * Get the complete San Diego map data package.
     */
    public Map<String, Object> getCompleteMapData() {
        Map<String, Object> mapData = new LinkedHashMap<>();
        mapData.put("buildings", get3DBuildings());
        mapData.put("terrain", getTerrainData());
        mapData.put("routes", getAllRoutes());
        mapData.put("hqToConventionCenter", getHqToConventionCenterPath());

        // Map center point (midpoint between HQ and Convention Center)
        mapData.put("centerLat", (YISY_HQ_LAT + SDCC_LAT) / 2);
        mapData.put("centerLon", (YISY_HQ_LON + SDCC_LON) / 2);
        mapData.put("zoom", 12);

        return mapData;
    }

    // --- Helper Methods ---

    private SanDiegoTransitRoute createRoute(
            String name, String number, String type, String description,
            String start, String end,
            double startLat, double startLon, double endLat, double endLon,
            int travelTime, int fare, List<String> stops, String stopsJson) {

        SanDiegoTransitRoute route = new SanDiegoTransitRoute();
        route.setRouteName(name);
        route.setRouteNumber(number);
        route.setRouteType(type);
        route.setDescription(description);
        route.setStartLocation(start);
        route.setEndLocation(end);
        route.setStartLatitude(startLat);
        route.setStartLongitude(startLon);
        route.setEndLatitude(endLat);
        route.setEndLongitude(endLon);
        route.setTravelTimeMinutes(travelTime);
        route.setFareCents(fare);
        route.setActive(true);
        route.setStopNames(stops);
        route.setStopsJson(stopsJson);
        return route;
    }

    private Map<String, Object> create3DBuilding(
            String name, String address,
            double lat, double lon,
            double height, double width, double depth,
            int color, String type, String description) {

        Map<String, Object> building = new LinkedHashMap<>();
        building.put("name", name);
        building.put("address", address);
        building.put("latitude", lat);
        building.put("longitude", lon);
        building.put("height", height);
        building.put("width", width);
        building.put("depth", depth);
        building.put("color", String.format("#%06x", color));
        building.put("type", type);
        building.put("description", description);
        return building;
    }

    private String generateStopsJson(double[][] stops) {
        try {
            List<List<Double>> jsonStops = new ArrayList<>();
            for (double[] stop : stops) {
                jsonStops.add(Arrays.asList(stop[0], stop[1]));
            }
            return objectMapper.writeValueAsString(jsonStops);
        } catch (JsonProcessingException e) {
            log.error("Failed to generate stops JSON", e);
            return "[]";
        }
    }
}
