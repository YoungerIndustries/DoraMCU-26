package com.yisy.modules.doramcu.hex.planet.depot;
import com.yisy.modules.doramcu.hex.planet.TerrainData;

import com.yisy.modules.doramcu.hex.Hex3D;
import com.yisy.modules.doramcu.hex.planet.Station;
import com.yisy.modules.doramcu.hex.planet.SurfaceVehicle;
import com.yisy.modules.doramcu.hex.planet.TransportCorporation;
import com.yisy.modules.doramcu.hex.planet.VehicleType;
import com.yisy.modules.doramcu.hex.planet.depot.dto.DepotVehicleDto;
import com.yisy.modules.doramcu.hex.planet.pathfinding.InfrastructurePathfinder;
import com.yisy.modules.doramcu.hex.planet.pathfinding.dto.PathResultDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * Manages vehicle depots: purchasing, selling, assigning vehicles to routes.
 * <p>
 * Each depot is located at a station and can purchase vehicle types compatible
 * with the station's infrastructure. The service:
 * <ul>
 *   <li>Creates depots at stations</li>
 *   <li>Purchases vehicles by deducting credits from the corporation</li>
 *   <li>Assigns vehicles to routes between stations</li>
 *   <li>Sells or retires vehicles with partial credit refunds</li>
 *   <li>Tracks vehicle statistics and depot activity</li>
 * </ul>
 * </p>
 */
@Service
public class VehicleDepotService {

    private static final Logger log = LoggerFactory.getLogger(VehicleDepotService.class);

    /** Depot ID -> depot mapping */
    private final Map<String, VehicleDepot> depots;

    /** Vehicle ID -> depot ID mapping */
    private final Map<String, String> vehicleToDepot;

    /** Vehicle ID -> SurfaceVehicle mapping */
    private final Map<String, SurfaceVehicle> vehicleRegistry;

    /** Counter for generating unique IDs */
    private final AtomicLong nextId;

    private final InfrastructurePathfinder pathfinder;

    public VehicleDepotService(InfrastructurePathfinder pathfinder) {
        this.depots = new ConcurrentHashMap<>();
        this.vehicleToDepot = new ConcurrentHashMap<>();
        this.vehicleRegistry = new ConcurrentHashMap<>();
        this.nextId = new AtomicLong(1);
        this.pathfinder = pathfinder;
    }

    /**
     * Creates a new depot at a station.
     *
     * @param station the station where the depot is located
     * @param ownerCorporationId the corporation that owns the depot
     * @param availableTypes vehicle types that can be purchased here
     * @return the created depot
     */
    public VehicleDepot createDepot(Station station, String ownerCorporationId,
                                     Set<VehicleType> availableTypes) {
        String depotId = "depot_" + station.getId() + "_" + nextId.getAndIncrement();
        VehicleDepot depot = new VehicleDepot(
                depotId,
                station.getId(),
                station.getPosition(),
                ownerCorporationId,
                availableTypes
        );
        depots.put(depotId, depot);
        log.info("Created depot {} at station {}", depotId, station.getId());
        return depot;
    }

    /**
     * Creates a depot at a station with all compatible vehicle types.
     *
     * @param station the station
     * @param ownerCorporationId the owning corporation
     * @return the created depot
     */
    public VehicleDepot createDepot(Station station, String ownerCorporationId) {
        Set<VehicleType> types = determineAvailableTypes(station);
        return createDepot(station, ownerCorporationId, types);
    }

    /**
     * Purchases a new vehicle at a depot.
     *
     * @param depotId the depot ID
     * @param vehicleType the type of vehicle to purchase
     * @param corporation the corporation purchasing the vehicle
     * @return the purchased vehicle, or null if purchase failed
     */
    public SurfaceVehicle purchaseVehicle(String depotId, VehicleType vehicleType,
                                           TransportCorporation corporation) {
        VehicleDepot depot = depots.get(depotId);
        if (depot == null) {
            log.warn("Depot {} not found", depotId);
            return null;
        }

        // Check if the depot can purchase this vehicle type
        if (!depot.canPurchase(vehicleType)) {
            log.warn("Depot {} cannot purchase {}", depotId, vehicleType.getDisplayName());
            return null;
        }

        // Check if the corporation has enough credits
        int cost = vehicleType.getConstructionCost();
        if (corporation.getCredits() < cost) {
            log.warn("Corporation {} has insufficient credits: {} < {}",
                    corporation.getName(), corporation.getCredits(), cost);
            return null;
        }

        // Deduct credits
        if (!corporation.spendCredits(cost)) {
            log.error("Failed to spend {} credits for corporation {}", cost, corporation.getName());
            return null;
        }

        // Create the vehicle
        String vehicleId = "vehicle_" + depotId + "_" + nextId.getAndIncrement();
        SurfaceVehicle vehicle = new SurfaceVehicle(vehicleId, vehicleType, depot.getPosition());
        vehicle.setOwnerEmpireId(corporation.getId());

        // Register the vehicle
        vehicleRegistry.put(vehicleId, vehicle);
        vehicleToDepot.put(vehicleId, depotId);
        depot.addAvailableVehicle(vehicleId);
        corporation.addVehicle(vehicleId);

        log.info("Purchased {} {} for {} at depot {}",
                vehicleType.getDisplayName(), vehicleId, corporation.getName(), depotId);

        return vehicle;
    }

    /**
     * Assigns a vehicle to a route between stations.
     *
     * @param vehicleId the vehicle ID
     * @param stationIds ordered list of station IDs for the route
     * @param stationMap map of all stations
     * @param planetId the planet ID
     * @return true if the vehicle was assigned successfully
     */
    public boolean assignToRoute(String vehicleId, List<String> stationIds,
                                  Map<String, Station> stationMap, int planetId) {
        SurfaceVehicle vehicle = vehicleRegistry.get(vehicleId);
        if (vehicle == null) {
            log.warn("Vehicle {} not found", vehicleId);
            return false;
        }

        // Validate route stations exist
        for (String stationId : stationIds) {
            if (!stationMap.containsKey(stationId)) {
                log.warn("Station {} not found in route", stationId);
                return false;
            }
        }

        // Check path exists between consecutive stations
        for (int i = 0; i < stationIds.size() - 1; i++) {
            Station from = stationMap.get(stationIds.get(i));
            Station to = stationMap.get(stationIds.get(i + 1));
            PathResultDto path = pathfinder.findPath(planetId, from, to, vehicle.getType());
            if (!path.isPathFound()) {
                log.warn("No path between {} and {}: {}",
                        from.getName(), to.getName(), path.getMessage());
                return false;
            }
        }

        // Assign the route
        vehicle.setRoute(stationIds);
        vehicle.setActive(true);

        // Update depot
        String depotId = vehicleToDepot.get(vehicleId);
        if (depotId != null) {
            VehicleDepot depot = depots.get(depotId);
            if (depot != null) {
                depot.placeInService(vehicleId);
            }
        }

        log.info("Assigned vehicle {} to route with {} stations", vehicleId, stationIds.size());
        return true;
    }

    /**
     * Sells a vehicle, refunding a portion of its construction cost.
     *
     * @param vehicleId the vehicle ID
     * @param corporation the corporation selling the vehicle
     * @return the refund amount
     */
    public long sellVehicle(String vehicleId, TransportCorporation corporation) {
        SurfaceVehicle vehicle = vehicleRegistry.get(vehicleId);
        if (vehicle == null) {
            log.warn("Vehicle {} not found for sale", vehicleId);
            return 0;
        }

        // Calculate refund (50% of construction cost, reduced by usage)
        int constructionCost = vehicle.getType().getConstructionCost();
        double usageFactor = Math.max(0.1, 1.0 - (vehicle.getTotalDistance() / 1000.0));
        long refund = (long) (constructionCost * 0.5 * usageFactor);

        // Add refund to corporation
        corporation.addCredits(refund);

        // Remove from depot
        String depotId = vehicleToDepot.get(vehicleId);
        if (depotId != null) {
            VehicleDepot depot = depots.get(depotId);
            if (depot != null) {
                depot.removeAvailableVehicle(vehicleId);
                depot.removeFromService(vehicleId);
                depot.recordSale();
            }
        }

        // Remove from corporation
        corporation.removeVehicle(vehicleId);

        // Remove from registry
        vehicleRegistry.remove(vehicleId);
        vehicleToDepot.remove(vehicleId);

        log.info("Sold vehicle {} for {} credits (refund: {})",
                vehicleId, corporation.getName(), refund);

        return refund;
    }

    /**
     * Retires a vehicle (removes it without refund).
     *
     * @param vehicleId the vehicle ID
     * @param corporation the corporation retiring the vehicle
     */
    public void retireVehicle(String vehicleId, TransportCorporation corporation) {
        SurfaceVehicle vehicle = vehicleRegistry.get(vehicleId);
        if (vehicle == null) {
            log.warn("Vehicle {} not found for retirement", vehicleId);
            return;
        }

        vehicle.setActive(false);

        // Remove from depot
        String depotId = vehicleToDepot.get(vehicleId);
        if (depotId != null) {
            VehicleDepot depot = depots.get(depotId);
            if (depot != null) {
                depot.removeAvailableVehicle(vehicleId);
                depot.removeFromService(vehicleId);
            }
        }

        // Remove from corporation
        corporation.removeVehicle(vehicleId);

        // Keep in registry for history but mark as inactive
        log.info("Retired vehicle {} from {}", vehicleId, corporation.getName());
    }

    /**
     * Gets a depot by ID.
     *
     * @param depotId the depot ID
     * @return the depot, or null if not found
     */
    public VehicleDepot getDepot(String depotId) {
        return depots.get(depotId);
    }

    /**
     * Gets all depots for a corporation.
     *
     * @param corporationId the corporation ID
     * @return list of depots owned by the corporation
     */
    public List<VehicleDepot> getDepotsByCorporation(String corporationId) {
        return depots.values().stream()
                .filter(d -> d.getOwnerCorporationId().equals(corporationId))
                .collect(Collectors.toList());
    }

    /**
     * Gets all vehicles in a depot.
     *
     * @param depotId the depot ID
     * @return list of vehicle DTOs
     */
    public List<DepotVehicleDto> getDepotVehicles(String depotId) {
        VehicleDepot depot = depots.get(depotId);
        if (depot == null) return Collections.emptyList();

        List<String> allVehicleIds = new ArrayList<>();
        allVehicleIds.addAll(depot.getAvailableVehicleIds());
        allVehicleIds.addAll(depot.getServicingVehicleIds());

        return allVehicleIds.stream()
                .map(vehicleRegistry::get)
                .filter(Objects::nonNull)
                .map(v -> {
                    boolean inService = depot.getServicingVehicleIds().contains(v.getId());
                    return new DepotVehicleDto(v, depotId, inService);
                })
                .collect(Collectors.toList());
    }

    /**
     * Gets all available vehicle types for purchase.
     *
     * @return all vehicle types
     */
    public List<VehicleType> getAvailableVehicleTypes() {
        return Arrays.asList(VehicleType.values());
    }

    /**
     * Gets a vehicle by ID.
     *
     * @param vehicleId the vehicle ID
     * @return the vehicle, or null if not found
     */
    public SurfaceVehicle getVehicle(String vehicleId) {
        return vehicleRegistry.get(vehicleId);
    }

    /**
     * Gets all vehicles registered in the system.
     *
     * @return list of all vehicles
     */
    public List<SurfaceVehicle> getAllVehicles() {
        return new ArrayList<>(vehicleRegistry.values());
    }

    /**
     * Determines which vehicle types are available at a station based on its infrastructure.
     *
     * @param station the station
     * @return set of compatible vehicle types
     */
    private Set<VehicleType> determineAvailableTypes(Station station) {
        Set<VehicleType> types = new HashSet<>();
        TerrainData.InfrastructureType infra = station.getInfrastructureType();

        for (VehicleType type : VehicleType.values()) {
            switch (type.getRequiredInfrastructure()) {
                case ROAD:
                    if (infra == TerrainData.InfrastructureType.ROAD ||
                            infra == TerrainData.InfrastructureType.STATION) {
                        types.add(type);
                    }
                    break;
                case RAIL:
                    if (infra == TerrainData.InfrastructureType.RAIL ||
                            infra == TerrainData.InfrastructureType.STATION) {
                        types.add(type);
                    }
                    break;
                case WATER:
                    if (infra == TerrainData.InfrastructureType.PORT ||
                            infra == TerrainData.InfrastructureType.STATION) {
                        types.add(type);
                    }
                    break;
                case AIRPORT:
                    if (infra == TerrainData.InfrastructureType.AIRPORT ||
                            infra == TerrainData.InfrastructureType.STATION) {
                        types.add(type);
                    }
                    break;
                case NONE:
                    types.add(type);
                    break;
            }
        }

        return types;
    }
}
