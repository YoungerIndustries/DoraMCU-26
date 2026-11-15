package com.yisy.modules.doramcu.hex.planet.dto;

import java.util.List;
import java.util.Map;

/**
 * DTO representing a surface vehicle on a planet.
 * <p>
 * Includes the vehicle's type, current position, route, cargo inventory,
 * and delivery statistics.
 * </p>
 */
public class VehicleDto {

    private String id;
    private String type;
    private int x;
    private int y;
    private int z;
    private String currentStationId;
    private List<String> routeStations;
    private Map<String, Integer> cargoInventory;
    private int passengerCount;
    private double totalDistance;
    private double totalCargoDelivered;
    private double totalPassengersDelivered;
    private boolean active;
    private String ownerEmpireId;

    public VehicleDto() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getZ() {
        return z;
    }

    public void setZ(int z) {
        this.z = z;
    }

    public String getCurrentStationId() {
        return currentStationId;
    }

    public void setCurrentStationId(String currentStationId) {
        this.currentStationId = currentStationId;
    }

    public List<String> getRouteStations() {
        return routeStations;
    }

    public void setRouteStations(List<String> routeStations) {
        this.routeStations = routeStations;
    }

    public Map<String, Integer> getCargoInventory() {
        return cargoInventory;
    }

    public void setCargoInventory(Map<String, Integer> cargoInventory) {
        this.cargoInventory = cargoInventory;
    }

    public int getPassengerCount() {
        return passengerCount;
    }

    public void setPassengerCount(int passengerCount) {
        this.passengerCount = passengerCount;
    }

    public double getTotalDistance() {
        return totalDistance;
    }

    public void setTotalDistance(double totalDistance) {
        this.totalDistance = totalDistance;
    }

    public double getTotalCargoDelivered() {
        return totalCargoDelivered;
    }

    public void setTotalCargoDelivered(double totalCargoDelivered) {
        this.totalCargoDelivered = totalCargoDelivered;
    }

    public double getTotalPassengersDelivered() {
        return totalPassengersDelivered;
    }

    public void setTotalPassengersDelivered(double totalPassengersDelivered) {
        this.totalPassengersDelivered = totalPassengersDelivered;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getOwnerEmpireId() {
        return ownerEmpireId;
    }

    public void setOwnerEmpireId(String ownerEmpireId) {
        this.ownerEmpireId = ownerEmpireId;
    }
}
