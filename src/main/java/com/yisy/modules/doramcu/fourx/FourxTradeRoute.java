package com.yisy.modules.doramcu.fourx;

/**
 * Shared trade route model for the 4X framework.
 * <p>
 * A trade route connects two star systems, carrying cargo of a specific type
 * and generating profit per trade cycle.
 * </p>
 * <p>
 * This is a plain DTO — not a JPA entity.
 * </p>
 */
public class FourxTradeRoute {

    private String id;

    /** Origin star system ID. */
    private String originSystemId;

    /** Destination star system ID. */
    private String destinationSystemId;

    /** Cargo type. */
    private CargoType cargoType;

    /** Profit per trade cycle. */
    private int profit;

    /** Security level (0-100). */
    private int securityLevel = 50;

    /** Whether this route is active. */
    private boolean active = true;

    /** Owner faction ID. */
    private String ownerFactionId;

    public FourxTradeRoute() {
    }

    public FourxTradeRoute(String originSystemId, String destinationSystemId, CargoType cargoType, String ownerFactionId) {
        this.originSystemId = originSystemId;
        this.destinationSystemId = destinationSystemId;
        this.cargoType = cargoType;
        this.ownerFactionId = ownerFactionId;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getOriginSystemId() { return originSystemId; }
    public void setOriginSystemId(String originSystemId) { this.originSystemId = originSystemId; }

    public String getDestinationSystemId() { return destinationSystemId; }
    public void setDestinationSystemId(String destinationSystemId) { this.destinationSystemId = destinationSystemId; }

    public CargoType getCargoType() { return cargoType; }
    public void setCargoType(CargoType cargoType) { this.cargoType = cargoType; }

    public int getProfit() { return profit; }
    public void setProfit(int profit) { this.profit = profit; }

    public int getSecurityLevel() { return securityLevel; }
    public void setSecurityLevel(int securityLevel) { this.securityLevel = securityLevel; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public String getOwnerFactionId() { return ownerFactionId; }
    public void setOwnerFactionId(String ownerFactionId) { this.ownerFactionId = ownerFactionId; }

    public enum CargoType {
        MINERALS,
        ENERGY,
        FOOD,
        RESEARCH,
        GENERAL
    }
}
