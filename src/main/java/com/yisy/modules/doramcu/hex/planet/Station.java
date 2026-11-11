package com.yisy.modules.doramcu.hex.planet;

import com.yisy.modules.doramcu.hex.Hex3D;

import java.util.*;

/**
 * Represents a station where vehicles load and unload cargo and passengers.
 * <p>
 * Stations are the key infrastructure connecting settlements and industries
 * to the transport network. Inspired by OpenTTD's station system:
 * <ul>
 *   <li>Vehicles visit stations to pick up and drop off cargo</li>
 *   <li>Stations have a catchment area (adjacent hexes)</li>
 *   <li>Cargo waiting at stations generates income when collected</li>
 * </ul>
 */
public class Station {

    private final String id;
    private final String name;
    private final Hex3D position;
    private final List<Hex3D> catchmentArea;
    private final Map<CargoType, Integer> cargoWaiting;
    private int passengersWaiting;
    private int totalIncome;
    private String connectedSettlementId;
    private String connectedIndustryId;
    private TerrainData.InfrastructureType infrastructureType;
    private String ownerEmpireId;

    /**
     * Creates a new station at the given hex position.
     *
     * @param id       unique identifier for this station
     * @param name     human-readable name
     * @param position the hex cell where the station is built
     */
    public Station(String id, String name, Hex3D position) {
        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException("Station ID must not be empty");
        }
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Station name must not be empty");
        }
        if (position == null) {
            throw new IllegalArgumentException("Position must not be null");
        }
        this.id = id;
        this.name = name;
        this.position = position;
        this.catchmentArea = new ArrayList<>();
        this.catchmentArea.add(position);
        this.cargoWaiting = new EnumMap<>(CargoType.class);
        this.passengersWaiting = 0;
        this.totalIncome = 0;
        this.connectedSettlementId = null;
        this.connectedIndustryId = null;
        this.infrastructureType = TerrainData.InfrastructureType.STATION;
        this.ownerEmpireId = null;
    }

    /** @return the unique station ID */
    public String getId() {
        return id;
    }

    /** @return the station name */
    public String getName() {
        return name;
    }

    /** @return the hex position */
    public Hex3D getPosition() {
        return position;
    }

    /** @return all hex cells in the station's catchment area */
    public List<Hex3D> getCatchmentArea() {
        return Collections.unmodifiableList(catchmentArea);
    }

    /** @return the cargo currently waiting at this station */
    public Map<CargoType, Integer> getCargoWaiting() {
        return Collections.unmodifiableMap(cargoWaiting);
    }

    /** @return the number of passengers waiting */
    public int getPassengersWaiting() {
        return passengersWaiting;
    }

    /** @return the total income generated from cargo delivery */
    public int getTotalIncome() {
        return totalIncome;
    }

    /**
     * @return the connected settlement ID, or null if none
     */
    public String getConnectedSettlementId() {
        return connectedSettlementId;
    }

    /**
     * Sets the connected settlement.
     *
     * @param settlementId the settlement ID
     */
    public void setConnectedSettlementId(String settlementId) {
        this.connectedSettlementId = settlementId;
    }

    /**
     * @return the connected industry ID, or null if none
     */
    public String getConnectedIndustryId() {
        return connectedIndustryId;
    }

    /**
     * Sets the connected industry.
     *
     * @param industryId the industry ID
     */
    public void setConnectedIndustryId(String industryId) {
        this.connectedIndustryId = industryId;
    }

    /** @return the infrastructure type */
    public TerrainData.InfrastructureType getInfrastructureType() {
        return infrastructureType;
    }

    /**
     * Sets the infrastructure type.
     *
     * @param type the infrastructure type
     */
    public void setInfrastructureType(TerrainData.InfrastructureType type) {
        this.infrastructureType = type;
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

    /** @return the hex position (alias for getPosition) */
    public Hex3D getLocation() {
        return position;
    }

    /** @return the x coordinate of the station position */
    public int getHexX() {
        return position.getX();
    }

    /** @return the y coordinate of the station position */
    public int getHexY() {
        return position.getY();
    }

    /** @return the z coordinate of the station position */
    public int getHexZ() {
        return position.getZ();
    }

    /**
     * Returns the planet ID where this station is located.
     * This is derived from the TerrainData attached to the station's hex.
     *
     * @return the planet ID, or -1 if not available
     */
    public int getPlanetId() {
        TerrainData terrainData = (TerrainData) position.getPayload();
        if (terrainData != null) {
            return terrainData.getPlanetId();
        }
        return -1;
    }

    /**
     * Adds cargo waiting at this station.
     *
     * @param cargoType the cargo type
     * @param amount    amount added
     */
    public void addCargo(CargoType cargoType, int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Amount must be non-negative: " + amount);
        }
        cargoWaiting.merge(cargoType, amount, Integer::sum);
    }

    /**
     * Removes cargo from this station (collected by a vehicle).
     *
     * @param cargoType the cargo type
     * @param amount    maximum amount to remove
     * @return the actual amount removed
     */
    public int removeCargo(CargoType cargoType, int amount) {
        int available = cargoWaiting.getOrDefault(cargoType, 0);
        int removed = Math.min(amount, available);
        if (removed > 0) {
            int remaining = available - removed;
            if (remaining == 0) {
                cargoWaiting.remove(cargoType);
            } else {
                cargoWaiting.put(cargoType, remaining);
            }
        }
        return removed;
    }

    /**
     * Adds passengers waiting at this station.
     *
     * @param count number of passengers
     */
    public void addPassengers(int count) {
        if (count < 0) {
            throw new IllegalArgumentException("Count must be non-negative: " + count);
        }
        passengersWaiting += count;
    }

    /**
     * Removes passengers from this station.
     *
     * @param count maximum number to remove
     * @return the actual number removed
     */
    public int removePassengers(int count) {
        int removed = Math.min(count, passengersWaiting);
        passengersWaiting -= removed;
        return removed;
    }

    /**
     * Records income from cargo delivery.
     *
     * @param income the income amount
     */
    public void addIncome(int income) {
        if (income < 0) {
            throw new IllegalArgumentException("Income must be non-negative: " + income);
        }
        totalIncome += income;
    }

    /**
     * Expands the catchment area to include adjacent hexes.
     */
    public void expandCatchmentArea() {
        List<Hex3D> current = new ArrayList<>(catchmentArea);
        for (Hex3D hex : current) {
            for (Hex3D neighbor : hex.neighbors()) {
                if (!catchmentArea.contains(neighbor)) {
                    catchmentArea.add(neighbor);
                }
            }
        }
    }

    /**
     * @return the total cargo waiting across all types
     */
    public int getTotalCargoWaiting() {
        return cargoWaiting.values().stream().mapToInt(Integer::intValue).sum();
    }

    @Override
    public String toString() {
        return String.format("Station{%s, cargo=%d, passengers=%d, income=%d}",
                name, getTotalCargoWaiting(), passengersWaiting, totalIncome);
    }
}
