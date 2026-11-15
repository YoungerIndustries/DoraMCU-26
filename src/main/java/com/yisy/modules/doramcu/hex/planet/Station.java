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
    private String dungeonId;
    private StationType stationType;
    private int maxPassengers;
    private int maxCargo;
    private int buildCost;
    private int maintenanceCost;

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
        this.stationType = StationType.GENERAL;
        this.maxPassengers = 200;
        this.maxCargo = 200;
        this.buildCost = 50;
        this.maintenanceCost = 3;
    }

    /**
     * Creates a new station with a specific type.
     *
     * @param id       unique identifier for this station
     * @param name     human-readable name
     * @param position the hex cell where the station is built
     * @param type     the station type (bus stop, rail station, port, airport, etc.)
     */
    public Station(String id, String name, Hex3D position, StationType type) {
        this(id, name, position);
        this.stationType = type != null ? type : StationType.GENERAL;
        this.maxPassengers = this.stationType.getMaxPassengers();
        this.maxCargo = this.stationType.getMaxCargo();
        this.buildCost = this.stationType.getBuildCost();
        this.maintenanceCost = this.stationType.getMaintenanceCost();
        this.infrastructureType = this.stationType.getInfrastructureType();
    }

    /** @return the station type */
    public StationType getStationType() {
        return stationType;
    }

    /**
     * Sets the station type and updates stats accordingly.
     *
     * @param type the station type
     */
    public void setStationType(StationType type) {
        if (type != null) {
            this.stationType = type;
            this.maxPassengers = type.getMaxPassengers();
            this.maxCargo = type.getMaxCargo();
            this.buildCost = type.getBuildCost();
            this.maintenanceCost = type.getMaintenanceCost();
            this.infrastructureType = type.getInfrastructureType();
        }
    }

    /** @return the maximum passengers this station can hold */
    public int getMaxPassengers() {
        return maxPassengers;
    }

    /** @return the maximum cargo this station can hold */
    public int getMaxCargo() {
        return maxCargo;
    }

    /** @return the build cost of this station */
    public int getBuildCost() {
        return buildCost;
    }

    /** @return the maintenance cost per turn */
    public int getMaintenanceCost() {
        return maintenanceCost;
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

    /**
     * Adds a hex to the station's catchment area.
     *
     * @param hex the hex to add
     */
    public void addCatchmentHex(Hex3D hex) {
        if (hex != null && !catchmentArea.contains(hex)) {
            catchmentArea.add(hex);
        }
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

    /**
     * @return the dungeon ID for the interior rooms of this station, or null if not yet generated
     */
    public String getDungeonId() {
        return dungeonId;
    }

    /**
     * Sets the dungeon ID for interior rooms.
     * Called after BuildingInteriorGenerator creates the rooms.
     *
     * @param dungeonId the dungeon ID
     */
    public void setDungeonId(String dungeonId) {
        this.dungeonId = dungeonId;
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
        return String.format("Station{%s, type=%s, cargo=%d, passengers=%d, income=%d}",
                name, stationType, getTotalCargoWaiting(), passengersWaiting, totalIncome);
    }

    /**
     * Station types based on OpenTTD station database.
     * Each type has different costs, capacities, and infrastructure requirements.
     */
    public enum StationType {
        /** Simple bus stop for passenger pickup */
        BUS_STOP("Bus Stop", 10, 1, 2, 50, 0, TerrainData.InfrastructureType.STATION),

        /** General-purpose rail station for trains */
        RAIL_STATION("Rail Station", 50, 3, 4, 200, 200, TerrainData.InfrastructureType.STATION),

        /** Elevated monorail station */
        MONORAIL_STATION("Monorail Station", 100, 5, 5, 300, 300, TerrainData.InfrastructureType.STATION),

        /** Futuristic maglev station */
        MAGLEV_STATION("Maglev Station", 200, 8, 6, 500, 500, TerrainData.InfrastructureType.STATION),

        /** Small boat dock for ferries */
        BOAT_DOCK("Boat Dock", 30, 2, 3, 100, 100, TerrainData.InfrastructureType.PORT),

        /** Large seaport with cranes and warehouses */
        PORT("Port", 150, 8, 6, 300, 500, TerrainData.InfrastructureType.PORT),

        /** Airport with runway and terminal */
        AIRPORT("Airport", 500, 20, 8, 500, 200, TerrainData.InfrastructureType.AIRPORT),

        /** Small heliport for helicopters */
        HELIPORT("Heliport", 100, 5, 4, 50, 50, TerrainData.InfrastructureType.AIRPORT),

        /** Space port for rockets and spacecraft */
        SPACEPORT("Space Port", 2000, 50, 10, 1000, 1000, TerrainData.InfrastructureType.AIRPORT),

        /** Orbital space station */
        ORBITAL_STATION("Orbital Station", 10000, 200, 15, 5000, 5000, TerrainData.InfrastructureType.AIRPORT),

        /** Dedicated cargo-only station */
        CARGO_STATION("Cargo Station", 80, 4, 5, 0, 400, TerrainData.InfrastructureType.STATION),

        /** Default general station */
        GENERAL("General Station", 50, 3, 4, 200, 200, TerrainData.InfrastructureType.STATION);

        private final String displayName;
        private final int buildCost;
        private final int maintenanceCost;
        private final int catchmentRadius;
        private final int maxPassengers;
        private final int maxCargo;
        private final TerrainData.InfrastructureType infrastructureType;

        StationType(String displayName, int buildCost, int maintenanceCost, int catchmentRadius,
                    int maxPassengers, int maxCargo, TerrainData.InfrastructureType infrastructureType) {
            this.displayName = displayName;
            this.buildCost = buildCost;
            this.maintenanceCost = maintenanceCost;
            this.catchmentRadius = catchmentRadius;
            this.maxPassengers = maxPassengers;
            this.maxCargo = maxCargo;
            this.infrastructureType = infrastructureType;
        }

        public String getDisplayName() { return displayName; }
        public int getBuildCost() { return buildCost; }
        public int getMaintenanceCost() { return maintenanceCost; }
        public int getCatchmentRadius() { return catchmentRadius; }
        public int getMaxPassengers() { return maxPassengers; }
        public int getMaxCargo() { return maxCargo; }
        public TerrainData.InfrastructureType getInfrastructureType() { return infrastructureType; }

        public static StationType fromName(String name) {
            if (name == null) return GENERAL;
            for (StationType type : values()) {
                if (type.name().equalsIgnoreCase(name) || type.displayName.equalsIgnoreCase(name)) {
                    return type;
                }
            }
            return GENERAL;
        }
    }
}
