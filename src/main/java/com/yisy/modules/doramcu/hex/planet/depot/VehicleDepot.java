package com.yisy.modules.doramcu.hex.planet.depot;

import com.yisy.modules.doramcu.hex.Hex3D;
import com.yisy.modules.doramcu.hex.planet.SurfaceVehicle;
import com.yisy.modules.doramcu.hex.planet.VehicleType;

import java.util.*;

/**
 * Represents a vehicle depot located at a station.
 * <p>
 * A depot allows:
 * <ul>
 *   <li>Purchasing new vehicles</li>
 *   <li>Servicing and repairing vehicles</li>
 *   <li>Selling or retiring vehicles</li>
 * </ul>
 * </p>
 * <p>
 * Each depot is associated with a station and can only purchase vehicle types
 * compatible with the station's infrastructure.
 * </p>
 */
public class VehicleDepot {

    private final String id;
    private final String stationId;
    private final Hex3D position;
    private final String ownerCorporationId;

    /** Vehicles currently in the depot (not yet assigned to routes) */
    private final List<String> availableVehicleIds;

    /** Vehicles currently being serviced */
    private final List<String> servicingVehicleIds;

    /** Vehicle types that can be purchased at this depot */
    private final Set<VehicleType> availableTypes;

    /** Total vehicles purchased from this depot */
    private int totalPurchased;

    /** Total vehicles sold from this depot */
    private int totalSold;

    /**
     * Creates a new vehicle depot.
     *
     * @param id unique depot identifier
     * @param stationId the station this depot is located at
     * @param position the hex position
     * @param ownerCorporationId the corporation that owns this depot
     * @param availableTypes vehicle types that can be purchased here
     */
    public VehicleDepot(String id, String stationId, Hex3D position,
                         String ownerCorporationId, Set<VehicleType> availableTypes) {
        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException("Depot ID must not be empty");
        }
        if (stationId == null || stationId.isEmpty()) {
            throw new IllegalArgumentException("Station ID must not be empty");
        }
        if (position == null) {
            throw new IllegalArgumentException("Position must not be null");
        }
        this.id = id;
        this.stationId = stationId;
        this.position = position;
        this.ownerCorporationId = ownerCorporationId;
        this.availableVehicleIds = new ArrayList<>();
        this.servicingVehicleIds = new ArrayList<>();
        this.availableTypes = availableTypes != null ? new HashSet<>(availableTypes) : new HashSet<>();
        this.totalPurchased = 0;
        this.totalSold = 0;
    }

    /** @return the unique depot ID */
    public String getId() {
        return id;
    }

    /** @return the station ID this depot is located at */
    public String getStationId() {
        return stationId;
    }

    /** @return the hex position of the depot */
    public Hex3D getPosition() {
        return position;
    }

    /** @return the corporation that owns this depot */
    public String getOwnerCorporationId() {
        return ownerCorporationId;
    }

    /** @return list of vehicle IDs available at the depot */
    public List<String> getAvailableVehicleIds() {
        return Collections.unmodifiableList(availableVehicleIds);
    }

    /** @return list of vehicle IDs currently being serviced */
    public List<String> getServicingVehicleIds() {
        return Collections.unmodifiableList(servicingVehicleIds);
    }

    /** @return vehicle types available for purchase */
    public Set<VehicleType> getAvailableTypes() {
        return Collections.unmodifiableSet(availableTypes);
    }

    /** @return total vehicles purchased from this depot */
    public int getTotalPurchased() {
        return totalPurchased;
    }

    /** @return total vehicles sold from this depot */
    public int getTotalSold() {
        return totalSold;
    }

    /**
     * Adds a vehicle to the depot's available list.
     *
     * @param vehicleId the vehicle ID
     */
    public void addAvailableVehicle(String vehicleId) {
        if (vehicleId != null && !availableVehicleIds.contains(vehicleId)) {
            availableVehicleIds.add(vehicleId);
            totalPurchased++;
        }
    }

    /**
     * Removes a vehicle from the depot's available list.
     *
     * @param vehicleId the vehicle ID
     * @return true if the vehicle was removed
     */
    public boolean removeAvailableVehicle(String vehicleId) {
        if (availableVehicleIds.remove(vehicleId)) {
            return true;
        }
        return false;
    }

    /**
     * Places a vehicle in service.
     *
     * @param vehicleId the vehicle ID
     */
    public void placeInService(String vehicleId) {
        servicingVehicleIds.add(vehicleId);
        availableVehicleIds.remove(vehicleId);
    }

    /**
     * Removes a vehicle from service.
     *
     * @param vehicleId the vehicle ID
     * @return true if the vehicle was in service
     */
    public boolean removeFromService(String vehicleId) {
        return servicingVehicleIds.remove(vehicleId);
    }

    /**
     * Records a vehicle sale.
     */
    public void recordSale() {
        totalSold++;
    }

    /**
     * Checks if a vehicle type can be purchased at this depot.
     *
     * @param type the vehicle type
     * @return true if the type is available
     */
    public boolean canPurchase(VehicleType type) {
        return availableTypes.contains(type);
    }

    @Override
    public String toString() {
        return String.format("VehicleDepot{id=%s, station=%s, available=%d, servicing=%d}",
                id, stationId, availableVehicleIds.size(), servicingVehicleIds.size());
    }
}
