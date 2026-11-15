package com.yisy.modules.doramcu.hex.planet.depot.dto;

import com.yisy.modules.doramcu.hex.Hex3D;
import com.yisy.modules.doramcu.hex.planet.CargoType;
import com.yisy.modules.doramcu.hex.planet.VehicleType;

import java.util.*;

/**
 * Data transfer object for a vehicle in a depot.
 * <p>
 * Used to expose vehicle information to the REST API and frontend.
 * </p>
 */
public class DepotVehicleDto {

    private final String id;
    private final VehicleType type;
    private final Hex3D position;
    private final String depotId;
    private final String ownerCorporationId;
    private final boolean active;
    private final boolean inService;
    private final List<String> routeStationIds;
    private final Map<String, Integer> cargoInventory;
    private final int passengerCount;
    private final int totalDistance;
    private final int totalCargoDelivered;
    private final int totalPassengersDelivered;

    /**
     * Creates a DTO from a SurfaceVehicle.
     *
     * @param vehicle the vehicle
     * @param depotId the depot ID
     * @param inService whether the vehicle is currently in service
     */
    public DepotVehicleDto(com.yisy.modules.doramcu.hex.planet.SurfaceVehicle vehicle,
                            String depotId, boolean inService) {
        this.id = vehicle.getId();
        this.type = vehicle.getType();
        this.position = vehicle.getCurrentPosition();
        this.depotId = depotId;
        this.ownerCorporationId = vehicle.getOwnerEmpireId();
        this.active = vehicle.isActive();
        this.inService = inService;
        this.routeStationIds = vehicle.getRouteStationIds();
        this.cargoInventory = new HashMap<>(vehicle.getCargoInventory());
        this.passengerCount = vehicle.getPassengerCount();
        this.totalDistance = vehicle.getTotalDistance();
        this.totalCargoDelivered = vehicle.getTotalCargoDelivered();
        this.totalPassengersDelivered = vehicle.getTotalPassengersDelivered();
    }

    /** @return the vehicle ID */
    public String getId() {
        return id;
    }

    /** @return the vehicle type */
    public VehicleType getType() {
        return type;
    }

    /** @return the vehicle type name */
    public String getTypeName() {
        return type.getDisplayName();
    }

    /** @return the current position */
    public Hex3D getPosition() {
        return position;
    }

    /** @return the depot ID */
    public String getDepotId() {
        return depotId;
    }

    /** @return the owner corporation ID */
    public String getOwnerCorporationId() {
        return ownerCorporationId;
    }

    /** @return true if the vehicle is active */
    public boolean isActive() {
        return active;
    }

    /** @return true if the vehicle is in service */
    public boolean isInService() {
        return inService;
    }

    /** @return the route station IDs */
    public List<String> getRouteStationIds() {
        return Collections.unmodifiableList(routeStationIds);
    }

    /** @return the cargo inventory */
    public Map<String, Integer> getCargoInventory() {
        return Collections.unmodifiableMap(cargoInventory);
    }

    /** @return the passenger count */
    public int getPassengerCount() {
        return passengerCount;
    }

    /** @return the total distance traveled */
    public int getTotalDistance() {
        return totalDistance;
    }

    /** @return the total cargo delivered */
    public int getTotalCargoDelivered() {
        return totalCargoDelivered;
    }

    /** @return the total passengers delivered */
    public int getTotalPassengersDelivered() {
        return totalPassengersDelivered;
    }

    /** @return the construction cost for this vehicle type */
    public int getConstructionCost() {
        return type.getConstructionCost();
    }

    /** @return the maintenance cost per turn */
    public int getMaintenanceCost() {
        return type.getMaintenanceCost();
    }

    /** @return the cargo capacity */
    public int getCargoCapacity() {
        return type.getCargoCapacity();
    }

    /** @return the passenger capacity */
    public int getPassengerCapacity() {
        return type.getPassengerCapacity();
    }

    /** @return the speed in hexes per turn */
    public int getSpeed() {
        return type.getSpeed();
    }

    @Override
    public String toString() {
        return String.format("DepotVehicleDto{id=%s, type=%s, active=%s, inService=%s}",
                id, type.getDisplayName(), active, inService);
    }
}
