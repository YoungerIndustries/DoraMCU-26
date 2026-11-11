package com.yisy.modules.doramcu.hex.planet;

import com.yisy.modules.doramcu.hex.Hex3D;

import java.util.*;

/**
 * Represents an individual surface vehicle that transports cargo and passengers
 * between stations along a route.
 * <p>
 * A vehicle:
 * <ul>
 *   <li>Has a {@link VehicleType} defining speed, capacity, and costs</li>
 *   <li>Follows a route between stations</li>
 *   <li>Carries cargo and passengers in its inventory</li>
 *   <li>Moves along the route each turn</li>
 * </ul>
 */
public class SurfaceVehicle {

    private final String id;
    private final VehicleType type;
    private Hex3D currentPosition;
    private final List<String> routeStationIds;
    private int currentRouteIndex;
    private final Map<CargoType, Integer> cargoInventory;
    private int passengerCount;
    private int totalDistance;
    private int totalCargoDelivered;
    private int totalPassengersDelivered;
    private boolean active;
    private String ownerEmpireId;

    /**
     * Creates a new surface vehicle.
     *
     * @param id     unique identifier
     * @param type   the vehicle type
     * @param start  starting position
     */
    public SurfaceVehicle(String id, VehicleType type, Hex3D start) {
        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException("Vehicle ID must not be empty");
        }
        if (type == null) {
            throw new IllegalArgumentException("Vehicle type must not be null");
        }
        if (start == null) {
            throw new IllegalArgumentException("Start position must not be null");
        }
        this.id = id;
        this.type = type;
        this.currentPosition = start;
        this.routeStationIds = new ArrayList<>();
        this.currentRouteIndex = 0;
        this.cargoInventory = new EnumMap<>(CargoType.class);
        this.passengerCount = 0;
        this.totalDistance = 0;
        this.totalCargoDelivered = 0;
        this.totalPassengersDelivered = 0;
        this.active = true;
        this.ownerEmpireId = null;
    }

    /** @return the unique vehicle ID */
    public String getId() {
        return id;
    }

    /** @return the vehicle type */
    public VehicleType getType() {
        return type;
    }

    /** @return the current hex position */
    public Hex3D getCurrentPosition() {
        return currentPosition;
    }

    /** @return the route station IDs */
    public List<String> getRouteStationIds() {
        return Collections.unmodifiableList(routeStationIds);
    }

    /** @return the current index in the route */
    public int getCurrentRouteIndex() {
        return currentRouteIndex;
    }

    /** @return the cargo currently being carried */
    public Map<CargoType, Integer> getCargoInventory() {
        return Collections.unmodifiableMap(cargoInventory);
    }

    /** @return the number of passengers on board */
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

    /** @return true if the vehicle is active */
    public boolean isActive() {
        return active;
    }

    /** Activates or deactivates this vehicle. */
    public void setActive(boolean active) {
        this.active = active;
    }

    /** @return the owner empire ID, or null if independent */
    public String getOwnerEmpireId() {
        return ownerEmpireId;
    }

    /**
     * Sets the owner empire.
     *
     * @param empireId the empire ID
     */
    public void setOwnerEmpireId(String empireId) {
        this.ownerEmpireId = empireId;
    }

    /**
     * Sets the route for this vehicle.
     *
     * @param stationIds ordered list of station IDs to visit
     */
    public void setRoute(List<String> stationIds) {
        if (stationIds == null || stationIds.isEmpty()) {
            throw new IllegalArgumentException("Route must not be empty");
        }
        this.routeStationIds.clear();
        this.routeStationIds.addAll(stationIds);
        this.currentRouteIndex = 0;
    }

    /**
     * Loads cargo onto this vehicle.
     *
     * @param cargoType the cargo type
     * @param amount    amount to load
     * @return the actual amount loaded
     */
    public int loadCargo(CargoType cargoType, int amount) {
        int totalCargo = cargoInventory.values().stream().mapToInt(Integer::intValue).sum();
        int remainingCapacity = type.getCargoCapacity() - totalCargo;
        int loaded = Math.min(amount, Math.max(0, remainingCapacity));
        if (loaded > 0) {
            cargoInventory.merge(cargoType, loaded, Integer::sum);
        }
        return loaded;
    }

    /**
     * Unloads cargo from this vehicle.
     *
     * @param cargoType the cargo type
     * @param amount    maximum amount to unload
     * @return the actual amount unloaded
     */
    public int unloadCargo(CargoType cargoType, int amount) {
        int available = cargoInventory.getOrDefault(cargoType, 0);
        int unloaded = Math.min(amount, available);
        if (unloaded > 0) {
            int remaining = available - unloaded;
            if (remaining == 0) {
                cargoInventory.remove(cargoType);
            } else {
                cargoInventory.put(cargoType, remaining);
            }
            totalCargoDelivered += unloaded;
        }
        return unloaded;
    }

    /**
     * Boards passengers onto this vehicle.
     *
     * @param count number of passengers to board
     * @return the actual number boarded
     */
    public int boardPassengers(int count) {
        int remainingCapacity = type.getPassengerCapacity() - passengerCount;
        int boarded = Math.min(count, Math.max(0, remainingCapacity));
        passengerCount += boarded;
        return boarded;
    }

    /**
     * Unboards passengers from this vehicle.
     *
     * @param count maximum number to unboard
     * @return the actual number unboarded
     */
    public int unboardPassengers(int count) {
        int unboarded = Math.min(count, passengerCount);
        passengerCount -= unboarded;
        totalPassengersDelivered += unboarded;
        return unboarded;
    }

    /**
     * Moves the vehicle toward the next station in the route.
     *
     * @param targetPosition the target hex position
     * @return true if the vehicle reached the target
     */
    public boolean move(Hex3D targetPosition) {
        if (!active) return false;

        int distance = currentPosition.distance(targetPosition);
        if (distance == 0) return true;

        int speed = type.getSpeed();
        if (speed >= distance) {
            currentPosition = targetPosition;
            totalDistance += distance;
            return true;
        }

        // Move toward target
        Hex3D next = currentPosition.lerp(targetPosition, (double) speed / distance);
        totalDistance += currentPosition.distance(next);
        currentPosition = next;
        return false;
    }

    /**
     * Processes a turn: advances along the route.
     *
     * @param stationMap map of station IDs to Station objects
     * @return the station reached, or null if still en route
     */
    public Station processTurn(Map<String, Station> stationMap) {
        if (!active || routeStationIds.isEmpty()) return null;

        String targetId = routeStationIds.get(currentRouteIndex);
        Station target = stationMap.get(targetId);
        if (target == null) return null;

        boolean reached = move(target.getPosition());
        if (reached) {
            // Advance to next station in route
            currentRouteIndex = (currentRouteIndex + 1) % routeStationIds.size();
            return target;
        }
        return null;
    }

    /**
     * @return the total cargo currently on board
     */
    public int getTotalCargoOnBoard() {
        return cargoInventory.values().stream().mapToInt(Integer::intValue).sum();
    }

    /**
     * @return the per-turn maintenance cost
     */
    public int getMaintenanceCost() {
        return type.getMaintenanceCost();
    }

    @Override
    public String toString() {
        return String.format("SurfaceVehicle{%s, cargo=%d, passengers=%d, pos=%s}",
                type.getDisplayName(), getTotalCargoOnBoard(), passengerCount, currentPosition);
    }
}
