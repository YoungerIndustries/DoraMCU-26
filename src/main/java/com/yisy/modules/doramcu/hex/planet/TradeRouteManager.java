package com.yisy.modules.doramcu.hex.planet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Manages trade routes for all corporations in the Business Tycoon campaign.
 * <p>
 * This manager handles both space routes (planet-to-planet) and surface routes
 * (station-to-station on a single planet). It calculates revenue per turn based
 * on cargo demand, distance, and competition, and processes cargo delivery each turn.
 * </p>
 * <p>
 * Space routes use patterns from the Traveller TradeSimulator, while surface
 * routes use SurfaceVehicle and Station from the hex.planet package.
 * </p>
 */
public class TradeRouteManager {

    /** All trade routes indexed by ID */
    private final Map<String, CampaignTradeRoute> routes;

    /** Reference to the market share tracker for cargo flow tracking */
    private MarketShareTracker marketShareTracker;

    /** Event manager for dispatching route events */
    private PlanetEventManager eventManager;

    public TradeRouteManager() {
        this.routes = new ConcurrentHashMap<>();
    }

    /**
     * Sets the market share tracker for cargo flow tracking.
     *
     * @param tracker the market share tracker
     */
    public void setMarketShareTracker(MarketShareTracker tracker) {
        this.marketShareTracker = tracker;
    }

    /**
     * Creates a new surface trade route between two stations.
     *
     * @param corporationId the corporation that owns this route
     * @param originStation the origin station
     * @param destinationStation the destination station
     * @param cargoType the type of cargo transported
     * @param vehicle the vehicle that will operate the route
     * @return the newly created route
     */
    public CampaignTradeRoute createSurfaceRoute(String corporationId,
                                                  Station originStation,
                                                  Station destinationStation,
                                                  CargoType cargoType,
                                                  SurfaceVehicle vehicle) {
        Objects.requireNonNull(originStation, "Origin station must not be null");
        Objects.requireNonNull(destinationStation, "Destination station must not be null");
        Objects.requireNonNull(cargoType, "Cargo type must not be null");

        int planetId = originStation.getPlanetId();
        int cargoPerTurn = vehicle.getType().getCargoCapacity();
        int distance = calculateSurfaceDistance(originStation, destinationStation);
        int revenue = calculateSurfaceRevenue(cargoType, cargoPerTurn, distance);
        int operatingCost = calculateOperatingCost(vehicle.getType(), distance);

        CampaignTradeRoute route = new CampaignTradeRoute(
                corporationId,
                CampaignTradeRoute.RouteType.SURFACE,
                originStation.getId(),
                destinationStation.getId(),
                planetId,
                cargoType,
                cargoPerTurn,
                revenue,
                operatingCost
        );

        if (vehicle != null) {
            route.setVehicleId(vehicle.getId());
        }

        routes.put(route.getId(), route);

        // Dispatch event
        PlanetEventManager eventManager = getEventManager();
        if (eventManager != null) {
            eventManager.dispatch(new PlanetEvent(
                    PlanetEventType.ROUTE_ESTABLISHED, System.nanoTime(),
                    route.getId(),
                    "Route established between " +
                    originStation.getName() + " and " + destinationStation.getName()
            ));
        }

        return route;
    }

    /**
     * Creates a new space trade route between two planets.
     *
     * @param corporationId the corporation that owns this route
     * @param originPlanetId the origin planet ID
     * @param destinationPlanetId the destination planet ID
     * @param cargoType the type of cargo transported
     * @param cargoPerTurn cargo units delivered per turn
     * @param shipId the ship ID operating this route, or null
     * @return the newly created route
     */
    public CampaignTradeRoute createSpaceRoute(String corporationId,
                                                String originPlanetId,
                                                String destinationPlanetId,
                                                CargoType cargoType,
                                                int cargoPerTurn,
                                                String shipId) {
        int revenue = calculateSpaceRevenue(cargoType, cargoPerTurn);
        int operatingCost = calculateSpaceOperatingCost(cargoPerTurn);

        CampaignTradeRoute route = new CampaignTradeRoute(
                corporationId,
                CampaignTradeRoute.RouteType.SPACE,
                originPlanetId,
                destinationPlanetId,
                -1,
                cargoType,
                cargoPerTurn,
                revenue,
                operatingCost
        );

        if (shipId != null) {
            route.setShipId(shipId);
        }

        routes.put(route.getId(), route);

        PlanetEventManager eventManager = getEventManager();
        if (eventManager != null) {
            eventManager.dispatch(new PlanetEvent(
                    PlanetEventType.ROUTE_ESTABLISHED, System.nanoTime(),
                    route.getId(),
                    "Space route established between planets " +
                    originPlanetId + " and " + destinationPlanetId
            ));
        }

        return route;
    }

    /**
     * Returns a route by its ID.
     *
     * @param routeId the route ID
     * @return the route, or empty if not found
     */
    public Optional<CampaignTradeRoute> getRoute(String routeId) {
        return Optional.ofNullable(routes.get(routeId));
    }

    /**
     * Returns all routes owned by a corporation.
     *
     * @param corporationId the corporation ID
     * @return list of routes owned by the corporation
     */
    public List<CampaignTradeRoute> getRoutesByCorporation(String corporationId) {
        return routes.values().stream()
                .filter(r -> r.getOwnerCorporationId().equals(corporationId))
                .collect(Collectors.toList());
    }

    /**
     * Returns all routes on a specific planet (surface routes only).
     *
     * @param planetId the planet ID
     * @return list of surface routes on the planet
     */
    public List<CampaignTradeRoute> getRoutesByPlanet(int planetId) {
        return routes.values().stream()
                .filter(r -> r.getPlanetId() == planetId)
                .collect(Collectors.toList());
    }

    /**
     * Returns all routes in the campaign.
     *
     * @return unmodifiable list of all routes
     */
    public List<CampaignTradeRoute> getAllRoutes() {
        return Collections.unmodifiableList(new ArrayList<>(routes.values()));
    }

    /**
     * Removes a route from the campaign.
     *
     * @param routeId the route ID
     * @return true if the route was found and removed
     */
    public boolean removeRoute(String routeId) {
        CampaignTradeRoute removed = routes.remove(routeId);
        if (removed != null) {
            PlanetEventManager eventManager = getEventManager();
            if (eventManager != null) {
                eventManager.dispatch(new PlanetEvent(
                        PlanetEventType.ROUTE_ESTABLISHED, System.nanoTime(),
                        routeId,
                        "Route removed"
                ));
            }
            return true;
        }
        return false;
    }

    /**
     * Processes one turn for all active routes.
     * Updates cargo delivery, revenue, and market share tracking.
     *
     * @return total cargo value delivered across all routes this turn
     */
    public long processTurn() {
        long totalCargoValue = 0;
        for (CampaignTradeRoute route : routes.values()) {
            if (route.isActive()) {
                long cargoValue = route.processTurn();
                totalCargoValue += cargoValue;

                // Track cargo flow for market share
                if (marketShareTracker != null) {
                    marketShareTracker.recordDelivery(route.getOwnerCorporationId(), cargoValue);
                }

                // Add revenue to the owning corporation's turn income
                // (handled by TransportCampaignManager)
            }
        }
        return totalCargoValue;
    }

    /**
     * Calculates the surface distance between two stations.
     * Uses hex coordinate distance as a proxy.
     *
     * @param origin the origin station
     * @param destination the destination station
     * @return distance in hex cells
     */
    private int calculateSurfaceDistance(Station origin, Station destination) {
        // Use a simple distance calculation based on station positions
        // In a real implementation, this would use pathfinding on the hex grid
        int dx = Math.abs(origin.getHexX() - destination.getHexX());
        int dy = Math.abs(origin.getHexY() - destination.getHexY());
        int dz = Math.abs(origin.getHexZ() - destination.getHexZ());
        // Hex distance is the maximum of the absolute differences
        return Math.max(Math.max(dx, dy), dz);
    }

    /**
     * Calculates the revenue for a surface route.
     *
     * @param cargoType the cargo type
     * @param cargoPerTurn cargo units per turn
     * @param distance the route distance
     * @return revenue per turn
     */
    private int calculateSurfaceRevenue(CargoType cargoType, int cargoPerTurn, int distance) {
        // Revenue is based on cargo value, quantity, and distance
        int baseValue = cargoType.getBaseValue();
        int distanceMultiplier = Math.max(1, distance / 5);
        return baseValue * cargoPerTurn * distanceMultiplier;
    }

    /**
     * Calculates the revenue for a space route.
     *
     * @param cargoType the cargo type
     * @param cargoPerTurn cargo units per turn
     * @return revenue per turn
     */
    private int calculateSpaceRevenue(CargoType cargoType, int cargoPerTurn) {
        // Space routes have higher revenue due to longer distances
        int baseValue = cargoType.getBaseValue();
        return baseValue * cargoPerTurn * 3;
    }

    /**
     * Calculates the operating cost for a vehicle on a route.
     *
     * @param vehicleType the vehicle type
     * @param distance the route distance
     * @return operating cost per turn
     */
    private int calculateOperatingCost(VehicleType vehicleType, int distance) {
        // Operating cost is based on vehicle maintenance and distance
        int maintenance = vehicleType.getMaintenanceCost();
        int fuelCost = Math.max(1, distance / 10);
        return maintenance + fuelCost;
    }

    /**
     * Calculates the operating cost for a space route.
     *
     * @param cargoPerTurn cargo units per turn
     * @return operating cost per turn
     */
    private int calculateSpaceOperatingCost(int cargoPerTurn) {
        // Space routes have higher operating costs
        return (cargoPerTurn * 2) + 10;
    }

    /**
     * Returns the event manager, or null if not set.
     *
     * @return the event manager
     */
    private PlanetEventManager getEventManager() {
        return eventManager;
    }

    /**
     * Sets the event manager for dispatching route events.
     *
     * @param eventManager the event manager
     */
    public void setEventManager(PlanetEventManager eventManager) {
        this.eventManager = eventManager;
    }

    /**
     * Returns the total number of routes.
     *
     * @return route count
     */
    public int getRouteCount() {
        return routes.size();
    }

    /**
     * Returns the total number of active routes.
     *
     * @return active route count
     */
    public int getActiveRouteCount() {
        return (int) routes.values().stream().filter(CampaignTradeRoute::isActive).count();
    }
}
