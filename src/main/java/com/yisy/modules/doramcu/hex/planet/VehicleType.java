package com.yisy.modules.doramcu.hex.planet;

/**
 * Represents types of surface vehicles that transport cargo and passengers
 * between settlements and industries.
 * <p>
 * Each vehicle type defines:
 * <ul>
 *   <li>Speed — hexes per turn</li>
 *   <li>Cargo capacity — units of cargo</li>
 *   <li>Passenger capacity — number of passengers</li>
 *   <li>Construction cost — credits</li>
 *   <li>Maintenance cost — per turn</li>
 *   <li>Required infrastructure — road, rail, water, or none</li>
 * </ul>
 */
public enum VehicleType {

    // ---- Road Vehicles ----

    TRUCK("Truck", 3, 20, 0, 100, 5, InfrastructureRequirement.ROAD,
            "Standard cargo truck for road transport"),

    BUS("Bus", 4, 0, 30, 80, 4, InfrastructureRequirement.ROAD,
            "Passenger bus for road transport"),

    EXPRESS_TRUCK("Express Truck", 5, 15, 0, 200, 8, InfrastructureRequirement.ROAD,
            "Fast cargo truck with reduced capacity"),

    // ---- Rail Vehicles ----

    FREIGHT_TRAIN("Freight Train", 6, 100, 0, 500, 15, InfrastructureRequirement.RAIL,
            "High-capacity cargo train for rail transport"),

    PASSENGER_TRAIN("Passenger Train", 8, 0, 80, 400, 12, InfrastructureRequirement.RAIL,
            "Fast passenger train for rail transport"),

    // ---- Water Vehicles ----

    CARGO_SHIP("Cargo Ship", 2, 80, 0, 300, 10, InfrastructureRequirement.WATER,
            "Large cargo ship for water transport"),

    PASSENGER_FERRY("Passenger Ferry", 3, 10, 50, 250, 8, InfrastructureRequirement.WATER,
            "Passenger ferry for water transport"),

    // ---- Air Vehicles ----

    AIRCRAFT("Aircraft", 10, 10, 10, 800, 25, InfrastructureRequirement.AIRPORT,
            "Fast air transport for cargo and passengers");

    private final String displayName;
    private final int speed;
    private final int cargoCapacity;
    private final int passengerCapacity;
    private final int constructionCost;
    private final int maintenanceCost;
    private final InfrastructureRequirement requiredInfrastructure;
    private final String description;

    VehicleType(String displayName, int speed, int cargoCapacity, int passengerCapacity,
                int constructionCost, int maintenanceCost,
                InfrastructureRequirement requiredInfrastructure, String description) {
        this.displayName = displayName;
        this.speed = speed;
        this.cargoCapacity = cargoCapacity;
        this.passengerCapacity = passengerCapacity;
        this.constructionCost = constructionCost;
        this.maintenanceCost = maintenanceCost;
        this.requiredInfrastructure = requiredInfrastructure;
        this.description = description;
    }

    /** @return the human-readable display name */
    public String getDisplayName() {
        return displayName;
    }

    /** @return the speed in hexes per turn */
    public int getSpeed() {
        return speed;
    }

    /** @return the cargo capacity in units */
    public int getCargoCapacity() {
        return cargoCapacity;
    }

    /** @return the passenger capacity */
    public int getPassengerCapacity() {
        return passengerCapacity;
    }

    /** @return the construction cost in credits */
    public int getConstructionCost() {
        return constructionCost;
    }

    /** @return the per-turn maintenance cost in credits */
    public int getMaintenanceCost() {
        return maintenanceCost;
    }

    /** @return the required infrastructure type */
    public InfrastructureRequirement getRequiredInfrastructure() {
        return requiredInfrastructure;
    }

    /** @return a description of the vehicle */
    public String getDescription() {
        return description;
    }

    /**
     * Infrastructure requirements for vehicle operation.
     */
    public enum InfrastructureRequirement {
        /** No special infrastructure needed */
        NONE,
        /** Requires road infrastructure */
        ROAD,
        /** Requires rail infrastructure */
        RAIL,
        /** Requires water access */
        WATER,
        /** Requires airport */
        AIRPORT
    }
}
