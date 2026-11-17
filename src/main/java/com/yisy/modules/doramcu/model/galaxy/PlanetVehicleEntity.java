package com.yisy.modules.doramcu.model.galaxy;
import com.yisy.modules.doramcu.hex.Hex;
import com.yisy.modules.doramcu.model.galaxy.Galaxy;
import com.yisy.modules.doramcu.model.misc.Planet;


import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * JPA entity that persists a surface vehicle on a planet.
 * <p>
 * The Galaxy Overlord saves every vehicle so it can be recreated from the
 * database instead of being randomly deployed again.
 * </p>
 */
@Entity
@Table(name = "planet_vehicles", indexes = {
    @Index(name = "idx_vehicle_planet", columnList = "planet_id"),
    @Index(name = "idx_vehicle_empire", columnList = "owner_empire_id")
})
public class PlanetVehicleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    /** Unique vehicle ID (matches SurfaceVehicle.id, e.g., "VEH-1"). */
    @Column(name = "vehicle_id", nullable = false, unique = true, length = 100)
    private String vehicleId;

    /** The planet this vehicle operates on. */
    @Column(name = "planet_id", nullable = false)
    private int planetId;

    /** Vehicle type (TRUCK, BUS, FREIGHT_TRAIN, etc.). */
    @Column(name = "vehicle_type", nullable = false, length = 30)
    private String vehicleType;

    /** Current hex X coordinate. */
    @Column(name = "current_hex_x", nullable = false)
    private int currentHexX;

    /** Current hex Y coordinate. */
    @Column(name = "current_hex_y", nullable = false)
    private int currentHexY;

    /** Current hex Z coordinate. */
    @Column(name = "current_hex_z", nullable = false)
    private int currentHexZ;

    /** JSON-serialized route station IDs list. */
    @Column(name = "route_station_ids_json", columnDefinition = "TEXT")
    private String routeStationIdsJson;

    /** Current index in the route. */
    @Column(name = "current_route_index", nullable = false)
    private int currentRouteIndex = 0;

    /** JSON-serialized cargo inventory map. */
    @Column(name = "cargo_inventory_json", columnDefinition = "TEXT")
    private String cargoInventoryJson;

    /** Number of passengers on board. */
    @Column(name = "passenger_count", nullable = false)
    private int passengerCount = 0;

    /** Total distance traveled (hexes). */
    @Column(name = "total_distance", nullable = false)
    private int totalDistance = 0;

    /** Total cargo delivered. */
    @Column(name = "total_cargo_delivered", nullable = false)
    private int totalCargoDelivered = 0;

    /** Total passengers delivered. */
    @Column(name = "total_passengers_delivered", nullable = false)
    private int totalPassengersDelivered = 0;

    /** Whether the vehicle is active. */
    @Column(name = "active", nullable = false)
    private boolean active = true;

    /** Owner empire ID (nullable). */
    @Column(name = "owner_empire_id", length = 100)
    private String ownerEmpireId;

    /** When this record was created. */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /** When this record was last updated. */
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public PlanetVehicleEntity() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(String vehicleId) {
        this.vehicleId = vehicleId;
    }

    public int getPlanetId() {
        return planetId;
    }

    public void setPlanetId(int planetId) {
        this.planetId = planetId;
    }

    public String getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(String vehicleType) {
        this.vehicleType = vehicleType;
    }

    public int getCurrentHexX() {
        return currentHexX;
    }

    public void setCurrentHexX(int currentHexX) {
        this.currentHexX = currentHexX;
        this.updatedAt = LocalDateTime.now();
    }

    public int getCurrentHexY() {
        return currentHexY;
    }

    public void setCurrentHexY(int currentHexY) {
        this.currentHexY = currentHexY;
        this.updatedAt = LocalDateTime.now();
    }

    public int getCurrentHexZ() {
        return currentHexZ;
    }

    public void setCurrentHexZ(int currentHexZ) {
        this.currentHexZ = currentHexZ;
        this.updatedAt = LocalDateTime.now();
    }

    public String getRouteStationIdsJson() {
        return routeStationIdsJson;
    }

    public void setRouteStationIdsJson(String routeStationIdsJson) {
        this.routeStationIdsJson = routeStationIdsJson;
    }

    public int getCurrentRouteIndex() {
        return currentRouteIndex;
    }

    public void setCurrentRouteIndex(int currentRouteIndex) {
        this.currentRouteIndex = currentRouteIndex;
        this.updatedAt = LocalDateTime.now();
    }

    public String getCargoInventoryJson() {
        return cargoInventoryJson;
    }

    public void setCargoInventoryJson(String cargoInventoryJson) {
        this.cargoInventoryJson = cargoInventoryJson;
    }

    public int getPassengerCount() {
        return passengerCount;
    }

    public void setPassengerCount(int passengerCount) {
        this.passengerCount = passengerCount;
        this.updatedAt = LocalDateTime.now();
    }

    public int getTotalDistance() {
        return totalDistance;
    }

    public void setTotalDistance(int totalDistance) {
        this.totalDistance = totalDistance;
    }

    public int getTotalCargoDelivered() {
        return totalCargoDelivered;
    }

    public void setTotalCargoDelivered(int totalCargoDelivered) {
        this.totalCargoDelivered = totalCargoDelivered;
    }

    public int getTotalPassengersDelivered() {
        return totalPassengersDelivered;
    }

    public void setTotalPassengersDelivered(int totalPassengersDelivered) {
        this.totalPassengersDelivered = totalPassengersDelivered;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
        this.updatedAt = LocalDateTime.now();
    }

    public String getOwnerEmpireId() {
        return ownerEmpireId;
    }

    public void setOwnerEmpireId(String ownerEmpireId) {
        this.ownerEmpireId = ownerEmpireId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "PlanetVehicleEntity{id=" + vehicleId + ", type=" + vehicleType +
                ", planet=" + planetId + ", pos=(" + currentHexX + "," + currentHexY + "," + currentHexZ + ")}";
    }
}
