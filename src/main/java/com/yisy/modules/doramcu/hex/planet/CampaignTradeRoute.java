package com.yisy.modules.doramcu.hex.planet;

import java.util.Objects;
import java.util.UUID;

/**
 * Represents a trade route in the Business Tycoon campaign.
 * <p>
 * A trade route connects two points and transports cargo automatically each turn.
 * Routes can be either space routes (planet-to-planet) or surface routes
 * (station-to-station on a single planet).
 * </p>
 * <p>
 * Space routes use patterns from the Traveller TradeSimulator, while surface
 * routes use SurfaceVehicle and Station from the hex.planet package.
 * </p>
 */
public class CampaignTradeRoute {

    /**
     * Type of trade route.
     */
    public enum RouteType {
        /** Space route between two planets */
        SPACE,
        /** Surface route between two stations on the same planet */
        SURFACE
    }

    private final String id;
    private final String ownerCorporationId;
    private final RouteType type;

    /** Origin planet ID (space) or station ID (surface) */
    private final String originId;

    /** Destination planet ID (space) or station ID (surface) */
    private final String destinationId;

    /** Planet ID where this route operates (for surface routes) */
    private final int planetId;

    /** Type of cargo transported on this route */
    private final CargoType cargoType;

    /** Cargo units delivered per turn */
    private int cargoPerTurn;

    /** Revenue in credits earned per turn */
    private int revenuePerTurn;

    /** Operating cost per turn (fuel, maintenance, etc.) */
    private int operatingCostPerTurn;

    /** Whether the route is currently active */
    private boolean active;

    /** Total cargo delivered on this route since creation */
    private long totalCargoDelivered;

    /** Total revenue earned from this route since creation */
    private long totalRevenueEarned;

    /** Reference to the vehicle ID used for surface routes */
    private String vehicleId;

    /** Reference to the ship ID used for space routes */
    private String shipId;

    /**
     * Creates a new trade route.
     *
     * @param ownerCorporationId the corporation that owns this route
     * @param type               the route type (SPACE or SURFACE)
     * @param originId           origin planet or station ID
     * @param destinationId      destination planet or station ID
     * @param planetId           planet ID (for surface routes, -1 for space)
     * @param cargoType          type of cargo transported
     * @param cargoPerTurn       cargo units delivered per turn
     * @param revenuePerTurn     credits earned per turn
     * @param operatingCostPerTurn cost per turn
     */
    public CampaignTradeRoute(String ownerCorporationId, RouteType type,
                               String originId, String destinationId,
                               int planetId, CargoType cargoType,
                               int cargoPerTurn, int revenuePerTurn,
                               int operatingCostPerTurn) {
        this.id = UUID.randomUUID().toString();
        this.ownerCorporationId = Objects.requireNonNull(ownerCorporationId, "Owner corporation ID must not be null");
        this.type = Objects.requireNonNull(type, "Route type must not be null");
        this.originId = Objects.requireNonNull(originId, "Origin ID must not be null");
        this.destinationId = Objects.requireNonNull(destinationId, "Destination ID must not be null");
        this.planetId = planetId;
        this.cargoType = Objects.requireNonNull(cargoType, "Cargo type must not be null");
        this.cargoPerTurn = cargoPerTurn;
        this.revenuePerTurn = revenuePerTurn;
        this.operatingCostPerTurn = operatingCostPerTurn;
        this.active = true;
        this.totalCargoDelivered = 0;
        this.totalRevenueEarned = 0;
    }

    /** @return the unique route ID */
    public String getId() {
        return id;
    }

    /** @return the owner corporation ID */
    public String getOwnerCorporationId() {
        return ownerCorporationId;
    }

    /** @return the route type */
    public RouteType getType() {
        return type;
    }

    /** @return the origin planet or station ID */
    public String getOriginId() {
        return originId;
    }

    /** @return the destination planet or station ID */
    public String getDestinationId() {
        return destinationId;
    }

    /** @return the planet ID (-1 for space routes) */
    public int getPlanetId() {
        return planetId;
    }

    /** @return the cargo type transported */
    public CargoType getCargoType() {
        return cargoType;
    }

    /** @return cargo units delivered per turn */
    public int getCargoPerTurn() {
        return cargoPerTurn;
    }

    /**
     * Sets the cargo per turn amount.
     *
     * @param cargoPerTurn the new amount
     */
    public void setCargoPerTurn(int cargoPerTurn) {
        if (cargoPerTurn < 0) {
            throw new IllegalArgumentException("Cargo per turn must be non-negative");
        }
        this.cargoPerTurn = cargoPerTurn;
    }

    /** @return revenue earned per turn */
    public int getRevenuePerTurn() {
        return revenuePerTurn;
    }

    /**
     * Sets the revenue per turn.
     *
     * @param revenuePerTurn the new revenue
     */
    public void setRevenuePerTurn(int revenuePerTurn) {
        if (revenuePerTurn < 0) {
            throw new IllegalArgumentException("Revenue must be non-negative");
        }
        this.revenuePerTurn = revenuePerTurn;
    }

    /** @return operating cost per turn */
    public int getOperatingCostPerTurn() {
        return operatingCostPerTurn;
    }

    /**
     * Sets the operating cost per turn.
     *
     * @param operatingCostPerTurn the new cost
     */
    public void setOperatingCostPerTurn(int operatingCostPerTurn) {
        if (operatingCostPerTurn < 0) {
            throw new IllegalArgumentException("Operating cost must be non-negative");
        }
        this.operatingCostPerTurn = operatingCostPerTurn;
    }

    /** @return true if the route is active */
    public boolean isActive() {
        return active;
    }

    /**
     * Sets the active status of the route.
     *
     * @param active the new status
     */
    public void setActive(boolean active) {
        this.active = active;
    }

    /** @return total cargo delivered since creation */
    public long getTotalCargoDelivered() {
        return totalCargoDelivered;
    }

    /** @return total revenue earned since creation */
    public long getTotalRevenueEarned() {
        return totalRevenueEarned;
    }

    /**
     * Processes one turn of cargo delivery for this route.
     * Updates total cargo delivered and total revenue earned.
     *
     * @return the cargo value delivered this turn (cargoPerTurn * cargoType base value)
     */
    public long processTurn() {
        if (!active) {
            return 0;
        }
        long cargoValue = (long) cargoPerTurn * cargoType.getBaseValue();
        this.totalCargoDelivered += cargoPerTurn;
        this.totalRevenueEarned += revenuePerTurn;
        return cargoValue;
    }

    /**
     * Returns the net profit per turn (revenue minus operating cost).
     *
     * @return net profit per turn
     */
    public int getNetProfitPerTurn() {
        return revenuePerTurn - operatingCostPerTurn;
    }

    /** @return the vehicle ID for surface routes, or null */
    public String getVehicleId() {
        return vehicleId;
    }

    /**
     * Sets the vehicle ID for surface routes.
     *
     * @param vehicleId the vehicle ID
     */
    public void setVehicleId(String vehicleId) {
        this.vehicleId = vehicleId;
    }

    /** @return the ship ID for space routes, or null */
    public String getShipId() {
        return shipId;
    }

    /**
     * Sets the ship ID for space routes.
     *
     * @param shipId the ship ID
     */
    public void setShipId(String shipId) {
        this.shipId = shipId;
    }

    @Override
    public String toString() {
        return "CampaignTradeRoute{" +
                "id='" + id + '\'' +
                ", type=" + type +
                ", origin=" + originId +
                ", destination=" + destinationId +
                ", cargo=" + cargoType.getDisplayName() +
                ", active=" + active +
                ", profit=" + getNetProfitPerTurn() +
                '}';
    }
}
