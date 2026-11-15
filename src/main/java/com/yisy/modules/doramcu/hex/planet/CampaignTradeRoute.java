package com.yisy.modules.doramcu.hex.planet;

import com.yisy.modules.doramcu.hex.spi.AiEntityRegistration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Represents a trade route connecting stations on a planet or between planets.
 * <p>
 * A trade route defines a path that vehicles follow to transport cargo
 * between stations. Routes are owned by corporations and generate income
 * based on cargo volume, distance, and demand between connected stations.
 * </p>
 * <p>
 * Supports two creation patterns:
 * <ul>
 *   <li>Legacy constructor for TradeRouteManager compatibility</li>
 *   <li>Builder pattern for auto-population and new code</li>
 * </ul>
 * </p>
 */
public class CampaignTradeRoute {

    public enum RouteType {
        /** Surface route between stations on the same planet */
        SURFACE,
        /** Space route between planets */
        SPACE
    }

    public enum RouteStatus {
        /** Route is active and vehicles are operating */
        ACTIVE,
        /** Route is suspended — no vehicles operating */
        SUSPENDED,
        /** Route is closed — can be reactivated */
        CLOSED
    }

    private final String id;
    private final String ownerCorporationId;
    private final RouteType routeType;
    private final String originId;
    private final String destinationId;
    private final int planetId;
    private final CargoType cargoType;
    private final int cargoPerTurn;
    private final int revenuePerTurn;
    private final int operatingCostPerTurn;
    private String vehicleId;
    private String shipId;
    private boolean active;
    private long totalCargoDelivered;
    private long totalRevenue;

    // Builder-pattern fields for new auto-population code
    private String name;
    private final List<String> stationIds = new ArrayList<>();
    private final List<String> vehicleIds = new ArrayList<>();
    private final List<CargoType> cargoTypes = new ArrayList<>();
    private RouteStatus status = RouteStatus.ACTIVE;
    private int totalDistance;
    private long dailyIncome;
    private long totalIncome;
    private int cargoDeliveredToday;

    /** Optional: service for deactivating AI characters when vehicles are removed. */
    private AiEntityRegistration aiEntityRegistrationService;

    /**
     * Legacy constructor for TradeRouteManager compatibility.
     */
    public CampaignTradeRoute(String corporationId, RouteType routeType,
                               String originId, String destinationId,
                               int planetId, CargoType cargoType,
                               int cargoPerTurn, int revenuePerTurn,
                               int operatingCostPerTurn) {
        this.id = UUID.randomUUID().toString();
        this.ownerCorporationId = corporationId;
        this.routeType = routeType;
        this.originId = originId;
        this.destinationId = destinationId;
        this.planetId = planetId;
        this.cargoType = cargoType;
        this.cargoPerTurn = cargoPerTurn;
        this.revenuePerTurn = revenuePerTurn;
        this.operatingCostPerTurn = operatingCostPerTurn;
        this.active = true;
        this.totalCargoDelivered = 0;
        this.totalRevenue = 0;
    }

    /**
     * Builder for creating CampaignTradeRoute instances (new auto-population pattern).
     */
    public static class Builder {
        private String name;
        private String ownerCorporationId;
        private final List<String> stationIds = new ArrayList<>();
        private final List<String> vehicleIds = new ArrayList<>();
        private final List<CargoType> cargoTypes = new ArrayList<>();
        private int totalDistance;

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder ownerCorporationId(String ownerCorporationId) {
            this.ownerCorporationId = ownerCorporationId;
            return this;
        }

        public Builder addStationId(String stationId) {
            this.stationIds.add(stationId);
            return this;
        }

        public Builder addVehicleId(String vehicleId) {
            this.vehicleIds.add(vehicleId);
            return this;
        }

        public Builder addCargoType(CargoType cargoType) {
            this.cargoTypes.add(cargoType);
            return this;
        }

        public Builder totalDistance(int totalDistance) {
            this.totalDistance = totalDistance;
            return this;
        }

        public CampaignTradeRoute build() {
            CampaignTradeRoute route = new CampaignTradeRoute(this);
            return route;
        }
    }

    /**
     * Creates a builder for CampaignTradeRoute.
     */
    public static Builder builder() {
        return new Builder();
    }

    private CampaignTradeRoute(Builder builder) {
        this.id = UUID.randomUUID().toString();
        this.name = builder.name;
        this.ownerCorporationId = builder.ownerCorporationId;
        this.routeType = RouteType.SURFACE;
        this.originId = builder.stationIds.isEmpty() ? null : builder.stationIds.get(0);
        this.destinationId = builder.stationIds.isEmpty() ? null :
                builder.stationIds.get(builder.stationIds.size() - 1);
        this.planetId = -1;
        this.cargoType = builder.cargoTypes.isEmpty() ? null : builder.cargoTypes.get(0);
        this.cargoPerTurn = 0;
        this.revenuePerTurn = 0;
        this.operatingCostPerTurn = 0;
        this.active = true;
        this.totalCargoDelivered = 0;
        this.totalRevenue = 0;

        // Builder-specific fields
        this.stationIds.addAll(builder.stationIds);
        this.vehicleIds.addAll(builder.vehicleIds);
        this.cargoTypes.addAll(builder.cargoTypes);
        this.totalDistance = builder.totalDistance;
    }

    // ---- Legacy API (TradeRouteManager compatibility) ----

    public String getId() {
        return id;
    }

    public String getOwnerCorporationId() {
        return ownerCorporationId;
    }

    public RouteType getRouteType() {
        return routeType;
    }

    /** Alias for getRouteType() — TradeRouteManager compatibility */
    public RouteType getType() {
        return routeType;
    }

    /** Returns net profit per turn (revenue - operating cost) */
    public int getNetProfitPerTurn() {
        return revenuePerTurn - operatingCostPerTurn;
    }

    public String getOriginId() {
        return originId;
    }

    public String getDestinationId() {
        return destinationId;
    }

    public int getPlanetId() {
        return planetId;
    }

    public CargoType getCargoType() {
        return cargoType;
    }

    public int getCargoPerTurn() {
        return cargoPerTurn;
    }

    public int getRevenuePerTurn() {
        return revenuePerTurn;
    }

    public int getOperatingCostPerTurn() {
        return operatingCostPerTurn;
    }

    public String getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(String vehicleId) {
        this.vehicleId = vehicleId;
    }

    public String getShipId() {
        return shipId;
    }

    public void setShipId(String shipId) {
        this.shipId = shipId;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public long getTotalCargoDelivered() {
        return totalCargoDelivered;
    }

    public long getTotalRevenue() {
        return totalRevenue;
    }

    /**
     * Processes one turn for this route.
     * @return cargo value delivered this turn
     */
    public long processTurn() {
        if (!active || cargoType == null) return 0;
        long value = (long) cargoType.getBaseValue() * cargoPerTurn;
        totalCargoDelivered += cargoPerTurn;
        totalRevenue += revenuePerTurn - operatingCostPerTurn;
        return value;
    }

    // ---- Builder-pattern API (auto-population) ----

    public String getName() {
        return name;
    }

    public List<String> getStationIds() {
        return Collections.unmodifiableList(stationIds);
    }

    public List<String> getVehicleIds() {
        return Collections.unmodifiableList(vehicleIds);
    }

    public List<CargoType> getCargoTypes() {
        return Collections.unmodifiableList(cargoTypes);
    }

    public RouteStatus getStatus() {
        return status;
    }

    public int getTotalDistance() {
        return totalDistance;
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

    public void addVehicle(String vehicleId) {
        this.vehicleIds.add(vehicleId);
    }

    public void removeVehicle(String vehicleId) {
        if (aiEntityRegistrationService != null) {
            try {
                aiEntityRegistrationService.deactivateVehicleAiCharacter(vehicleId);
            } catch (Exception e) {
                // Gracefully skip deactivation if it fails
            }
        }
        this.vehicleIds.remove(vehicleId);
    }

    /**
     * Sets the AI entity registration service for deactivating vehicle AI characters.
     *
     * @param service the registration service
     */
    public void setAiEntityRegistrationService(AiEntityRegistration service) {
        this.aiEntityRegistrationService = service;
    }

    public void suspend() {
        this.status = RouteStatus.SUSPENDED;
        this.active = false;
    }

    public void close() {
        this.status = RouteStatus.CLOSED;
        this.active = false;
    }

    public void reactivate() {
        this.status = RouteStatus.ACTIVE;
        this.active = true;
    }

    public void recordDelivery(CargoType cargoType, int amount) {
        this.cargoDeliveredToday += amount;
        long income = (long) cargoType.getBaseValue() * amount;
        this.dailyIncome += income;
        this.totalIncome += income;
    }

    public int getStationCount() {
        return stationIds.size();
    }

    public int getVehicleCount() {
        return vehicleIds.size();
    }

    public String getOriginStationId() {
        return stationIds.isEmpty() ? null : stationIds.get(0);
    }

    public String getDestinationStationId() {
        return stationIds.isEmpty() ? null : stationIds.get(stationIds.size() - 1);
    }

    @Override
    public String toString() {
        return "CampaignTradeRoute{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", type=" + routeType +
                ", stations=" + stationIds.size() +
                ", active=" + active +
                ", totalIncome=" + totalIncome +
                '}';
    }
}
