package com.yisy.modules.doramcu.earth2026.service;

import com.yisy.modules.doramcu.earth2026.model.Earth2026TransportRoute;
import com.yisy.modules.doramcu.earth2026.repository.Earth2026TransportRouteRepository;
import com.yisy.modules.doramcu.earth2026.repository.YoungerOfficeRepository;
import com.yisy.modules.doramcu.event.GalaxyEventService;
import com.yisy.modules.doramcu.event.GalaxyEventType;
import com.yisy.modules.doramcu.hex.planet.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Bridge service that converts Earth2026TransportRoutes into Transport Tycoon
 * CampaignTradeRoutes with vehicles and cargo flow.
 * <p>
 * This service:
 * <ul>
 *   <li>Converts Earth2026 transport routes to Tycoon surface trade routes</li>
 *   <li>Deploys vehicles on routes based on vehicle type mapping</li>
 *   <li>Enables passenger transport booking between capitals</li>
 *   <li>Processes turn-based vehicle movement and cargo delivery</li>
 * </ul>
 * <p>
 * Vehicle type mapping:
 * <ul>
 *   <li>"Commercial Airliner" â†’ AIRCRAFT</li>
 *   <li>"High-Speed Rail" â†’ PASSENGER_TRAIN</li>
 *   <li>"Freight Train" â†’ FREIGHT_TRAIN</li>
 *   <li>"Cargo Ship" â†’ CARGO_SHIP</li>
 *   <li>Default â†’ AIRCRAFT (fastest for inter-capital travel)</li>
 * </ul>
 * </p>
 */
@Service
public class TransportBridge {

    private static final Logger log = LoggerFactory.getLogger(TransportBridge.class);

    private final Earth2026TransportRouteRepository transportRouteRepository;
    private final PlanetSurfaceBuilder surfaceBuilder;
    private final TradeRouteManager tradeRouteManager;
    private final YoungerOfficeRepository youngerOfficeRepository;
    private final GalaxyEventService galaxyEventService;
    private final PlanetBuilderBridge planetBuilderBridge;

    /** Cache: route ID â†’ CampaignTradeRoute */
    private final Map<Long, CampaignTradeRoute> routeCache = new HashMap<>();

    /** Cache: route ID â†’ list of deployed vehicles */
    private final Map<Long, List<SurfaceVehicle>> vehicleCache = new HashMap<>();

    public TransportBridge(Earth2026TransportRouteRepository transportRouteRepository,
                            PlanetSurfaceBuilder surfaceBuilder,
                            TradeRouteManager tradeRouteManager,
                            YoungerOfficeRepository youngerOfficeRepository,
                            GalaxyEventService galaxyEventService,
                            PlanetBuilderBridge planetBuilderBridge) {
        this.transportRouteRepository = transportRouteRepository;
        this.surfaceBuilder = surfaceBuilder;
        this.tradeRouteManager = tradeRouteManager;
        this.youngerOfficeRepository = youngerOfficeRepository;
        this.galaxyEventService = galaxyEventService;
        this.planetBuilderBridge = planetBuilderBridge;
    }

    // ---- Initialization ----

    /**
     * Initializes the transport network by converting all Earth2026TransportRoutes
     * to Tycoon CampaignTradeRoutes with deployed vehicles.
     *
     * @param galaxyName the galaxy name for event publishing
     * @return the number of routes created
     */
    public int initializeTransportNetwork(String galaxyName) {
        log.info("Initializing transport network from Earth2026 routes");

        List<Earth2026TransportRoute> routes = transportRouteRepository.findByPlanetIdAndActiveTrue(1L);
        int created = 0;

        for (Earth2026TransportRoute route : routes) {
            try {
                CampaignTradeRoute tradeRoute = createTradeRoute(route, galaxyName);
                if (tradeRoute != null) {
                    routeCache.put(route.getId(), tradeRoute);
                    created++;
                    log.info("  Route: {} -> {} ({})", route.getFromCity(), route.getToCity(), route.getVehicleType());
                }
            } catch (Exception e) {
                log.warn("Failed to create trade route for {}: {}", route.getName(), e.getMessage());
            }
        }

        log.info("Created {} trade routes with vehicles", created);
        return created;
    }

    // ---- Trade Route Creation ----

    /**
     * Creates a CampaignTradeRoute from an Earth2026TransportRoute.
     */
    private CampaignTradeRoute createTradeRoute(Earth2026TransportRoute route, String galaxyName) {
        // Find the stations for the from/to locations
        Station fromStation = planetBuilderBridge.getStationForLocation(route.getFromLocationId());
        Station toStation = planetBuilderBridge.getStationForLocation(route.getToLocationId());

        if (fromStation == null || toStation == null) {
            log.warn("Stations not found for route {}: {} -> {}",
                    route.getName(), route.getFromCity(), route.getToCity());
            return null;
        }

        // Determine vehicle type and deploy vehicle first
        VehicleType vehicleType = mapVehicleType(route.getVehicleType());
        String corporationId = "younger_industries";

        SurfaceVehicle vehicle = surfaceBuilder.deployVehicle(
                null, // planet not needed for vehicle deployment
                vehicleType,
                fromStation,
                corporationId
        );

        // Choose cargo type based on vehicle type
        CargoType cargoType = mapCargoType(vehicleType);

        // Create the trade route
        CampaignTradeRoute tradeRoute = tradeRouteManager.createSurfaceRoute(
                corporationId,
                fromStation,
                toStation,
                cargoType,
                vehicle
        );

        // Cache the vehicle for this route
        vehicleCache.computeIfAbsent(route.getId(), k -> new ArrayList<>()).add(vehicle);

        // Publish event
        String planetName = "Earth 2026";
        galaxyEventService.publish(GalaxyEventType.TRANSPORT_ROUTE_ESTABLISHED, planetName, 1L,
                "planetName", planetName, "fromCity", route.getFromCity(),
                "toCity", route.getToCity(), "vehicleType", route.getVehicleType(),
                "travelTime", String.valueOf(route.getTravelTimeMinutes()));

        log.info("  Created trade route: {} -> {} (vehicle: {}, cargo: {})",
                route.getFromCity(), route.getToCity(), vehicleType.getDisplayName(), cargoType.getDisplayName());
        return tradeRoute;
    }

    /**
     * Maps vehicle type to a default cargo type for the trade route.
     */
    private CargoType mapCargoType(VehicleType vehicleType) {
        switch (vehicleType) {
            case FREIGHT_TRAIN:
            case CARGO_SHIP:
            case TRUCK:
            case EXPRESS_TRUCK:
                return CargoType.STEEL; // Heavy cargo
            case PASSENGER_TRAIN:
            case PASSENGER_FERRY:
            case BUS:
                return CargoType.PASSENGERS; // Passenger transport
            case AIRCRAFT:
                return CargoType.ELECTRONICS; // High-value air cargo
            default:
                return CargoType.MAIL;
        }
    }

    /**
     * Maps Earth2026 vehicle type string to Tycoon VehicleType enum.
     */
    private VehicleType mapVehicleType(String vehicleType) {
        if (vehicleType == null) {
            return VehicleType.AIRCRAFT;
        }

        String type = vehicleType.toLowerCase();
        if (type.contains("airlin") || type.contains("aircraft") || type.contains("plane") || type.contains("suborbital")) {
            return VehicleType.AIRCRAFT;
        } else if (type.contains("rail") || type.contains("train") || type.contains("high-speed")) {
            return VehicleType.PASSENGER_TRAIN;
        } else if (type.contains("freight") || type.contains("cargo train")) {
            return VehicleType.FREIGHT_TRAIN;
        } else if (type.contains("ship") || type.contains("ferry") || type.contains("boat")) {
            return VehicleType.PASSENGER_FERRY;
        } else if (type.contains("truck") || type.contains("bus")) {
            return VehicleType.BUS;
        } else {
            // Default to aircraft for inter-capital travel
            return VehicleType.AIRCRAFT;
        }
    }

    // ---- Turn Processing ----

    /**
     * Processes one transport turn: vehicle movement, cargo pickup/delivery, revenue calculation.
     *
     * @return total cargo value delivered this turn
     */
    public long processTransportTurn() {
        log.info("Processing transport turn");

        long totalCargoValue = tradeRouteManager.processTurn();

        log.info("Transport turn complete: {} cargo value delivered", totalCargoValue);
        return totalCargoValue;
    }

    // ---- Passenger Booking ----

    /**
     * Books passenger transport between two capital cities.
     * <p>
     * This is the player-facing method for booking transport via YoungerOffice stations.
     * </p>
     *
     * @param fromLocationId the origin location ID
     * @param toLocationId the destination location ID
     * @param passengers number of passengers
     * @return booking result with cost and estimated travel time
     */
    public Map<String, Object> bookPassengerTransport(Long fromLocationId, Long toLocationId, int passengers) {
        Map<String, Object> result = new HashMap<>();

        // Find the route between these locations
        Optional<Earth2026TransportRoute> routeOpt = transportRouteRepository
                .findByFromLocationIdAndToLocationId(fromLocationId, toLocationId);

        if (routeOpt.isEmpty()) {
            // Try reverse direction for bidirectional routes
            routeOpt = transportRouteRepository
                    .findByFromLocationIdAndToLocationId(toLocationId, fromLocationId);
        }

        if (routeOpt.isEmpty()) {
            result.put("success", false);
            result.put("error", "No direct route found between locations");

            // Suggest available routes from origin
            List<Earth2026TransportRoute> availableRoutes = transportRouteRepository
                    .findByFromLocationIdAndActiveTrue(fromLocationId);
            result.put("availableRoutes", availableRoutes.stream()
                    .map(r -> r.getFromCity() + " -> " + r.getToCity())
                    .collect(Collectors.toList()));
            return result;
        }

        Earth2026TransportRoute route = routeOpt.get();
        long totalCost = route.getTicketCost() * passengers;

        result.put("success", true);
        result.put("routeName", route.getName());
        result.put("fromCity", route.getFromCity());
        result.put("toCity", route.getToCity());
        result.put("vehicleType", route.getVehicleType());
        result.put("travelTimeMinutes", route.getTravelTimeMinutes());
        result.put("passengers", passengers);
        result.put("totalCost", totalCost);
        result.put("currency", "credits");

        // Find the YoungerOffice for the origin
        com.yisy.modules.doramcu.earth2026.model.YoungerOffice office =
                youngerOfficeRepository.findByLocationId(fromLocationId);
        if (office != null) {
            result.put("bookingOffice", office.getName());
            result.put("bookingCity", office.getCity());
        }

        log.info("Booked transport: {} passengers from {} to {} ({} credits)",
                passengers, route.getFromCity(), route.getToCity(), totalCost);
        return result;
    }

    // ---- Route Status ----

    /**
     * Returns the current status of a transport route.
     *
     * @param routeId the Earth2026 transport route ID
     * @return map with route status, vehicle positions, cargo carried, and revenue
     */
    public Map<String, Object> getRouteStatus(Long routeId) {
        Map<String, Object> status = new HashMap<>();

        Earth2026TransportRoute route = transportRouteRepository.findById(routeId).orElse(null);
        if (route == null) {
            status.put("error", "Route not found: " + routeId);
            return status;
        }

        status.put("routeId", route.getId());
        status.put("routeName", route.getName());
        status.put("fromCity", route.getFromCity());
        status.put("toCity", route.getToCity());
        status.put("vehicleType", route.getVehicleType());
        status.put("travelTimeMinutes", route.getTravelTimeMinutes());
        status.put("ticketCost", route.getTicketCost());
        status.put("active", route.isActive());

        // Tycoon trade route info
        CampaignTradeRoute tradeRoute = routeCache.get(routeId);
        if (tradeRoute != null) {
            status.put("tradeRouteId", tradeRoute.getId());
            status.put("revenuePerTurn", tradeRoute.getRevenuePerTurn());
            status.put("operatingCostPerTurn", tradeRoute.getOperatingCostPerTurn());
        }

        // Vehicle info
        List<SurfaceVehicle> vehicles = vehicleCache.getOrDefault(routeId, Collections.emptyList());
        status.put("vehicleCount", vehicles.size());

        return status;
    }

    /**
     * Returns a summary of the entire transport network.
     */
    public Map<String, Object> getNetworkSummary() {
        Map<String, Object> summary = new HashMap<>();

        long totalRoutes = transportRouteRepository.countByPlanetId(1L);
        long activeRoutes = transportRouteRepository.findByPlanetIdAndActiveTrue(1L).size();

        summary.put("totalRoutes", totalRoutes);
        summary.put("activeRoutes", activeRoutes);
        summary.put("tradeRoutesCreated", (long) routeCache.size());
        summary.put("vehiclesDeployed", (long) vehicleCache.values().stream()
                .mapToInt(List::size).sum());

        // Route list
        List<Map<String, String>> routeList = new ArrayList<>();
        transportRouteRepository.findByPlanetIdAndActiveTrue(1L).forEach(route -> {
            Map<String, String> entry = new HashMap<>();
            entry.put("from", route.getFromCity());
            entry.put("to", route.getToCity());
            entry.put("vehicle", route.getVehicleType());
            entry.put("time", route.getTravelTimeMinutes() + " min");
            entry.put("cost", route.getTicketCost() + " credits");
            routeList.add(entry);
        });
        summary.put("routes", routeList);

        return summary;
    }

    // ---- Lookup Methods ----

    /**
     * Returns the trade route for a given Earth2026 route ID.
     */
    public CampaignTradeRoute getTradeRoute(Long routeId) {
        return routeCache.get(routeId);
    }

    /**
     * Returns all deployed vehicles for a given route.
     */
    public List<SurfaceVehicle> getVehiclesForRoute(Long routeId) {
        return vehicleCache.getOrDefault(routeId, Collections.emptyList());
    }

    /**
     * Returns all cached trade routes.
     */
    public Map<Long, CampaignTradeRoute> getAllTradeRoutes() {
        return Collections.unmodifiableMap(routeCache);
    }

    /**
     * Returns the TradeRouteManager instance.
     */
    public TradeRouteManager getTradeRouteManager() {
        return tradeRouteManager;
    }
}
