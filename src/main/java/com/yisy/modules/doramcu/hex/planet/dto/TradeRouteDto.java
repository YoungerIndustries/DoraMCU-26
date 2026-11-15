package com.yisy.modules.doramcu.hex.planet.dto;

import java.util.List;

/**
 * DTO representing a trade route on a planet.
 */
public class TradeRouteDto {

    private String id;
    private String name;
    private String ownerCorporationId;
    private List<String> stationIds;
    private List<String> vehicleIds;
    private List<String> cargoTypes;
    private String status;
    private int totalDistance;
    private long dailyIncome;
    private long totalIncome;
    private int cargoDeliveredToday;
    private int stationCount;
    private int vehicleCount;
    private String originStationId;
    private String destinationStationId;

    public TradeRouteDto() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOwnerCorporationId() {
        return ownerCorporationId;
    }

    public void setOwnerCorporationId(String ownerCorporationId) {
        this.ownerCorporationId = ownerCorporationId;
    }

    public List<String> getStationIds() {
        return stationIds;
    }

    public void setStationIds(List<String> stationIds) {
        this.stationIds = stationIds;
    }

    public List<String> getVehicleIds() {
        return vehicleIds;
    }

    public void setVehicleIds(List<String> vehicleIds) {
        this.vehicleIds = vehicleIds;
    }

    public List<String> getCargoTypes() {
        return cargoTypes;
    }

    public void setCargoTypes(List<String> cargoTypes) {
        this.cargoTypes = cargoTypes;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getTotalDistance() {
        return totalDistance;
    }

    public void setTotalDistance(int totalDistance) {
        this.totalDistance = totalDistance;
    }

    public long getDailyIncome() {
        return dailyIncome;
    }

    public void setDailyIncome(long dailyIncome) {
        this.dailyIncome = dailyIncome;
    }

    public long getTotalIncome() {
        return totalIncome;
    }

    public void setTotalIncome(long totalIncome) {
        this.totalIncome = totalIncome;
    }

    public int getCargoDeliveredToday() {
        return cargoDeliveredToday;
    }

    public void setCargoDeliveredToday(int cargoDeliveredToday) {
        this.cargoDeliveredToday = cargoDeliveredToday;
    }

    public int getStationCount() {
        return stationCount;
    }

    public void setStationCount(int stationCount) {
        this.stationCount = stationCount;
    }

    public int getVehicleCount() {
        return vehicleCount;
    }

    public void setVehicleCount(int vehicleCount) {
        this.vehicleCount = vehicleCount;
    }

    public String getOriginStationId() {
        return originStationId;
    }

    public void setOriginStationId(String originStationId) {
        this.originStationId = originStationId;
    }

    public String getDestinationStationId() {
        return destinationStationId;
    }

    public void setDestinationStationId(String destinationStationId) {
        this.destinationStationId = destinationStationId;
    }
}
