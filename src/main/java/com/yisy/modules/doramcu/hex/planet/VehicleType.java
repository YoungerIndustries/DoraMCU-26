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

    // ---- OpenTTD Extended Rail Vehicles (NUTS, FRIMS, Modern Maglev) ----

    STEAM_LOCOMOTIVE("Steam Locomotive", 4, 50, 40, 300, 10, InfrastructureRequirement.RAIL,
            "Classic steam-powered locomotive — early industrial technology (NUTS)"),

    DIESEL_LOCOMOTIVE("Diesel Locomotive", 6, 80, 60, 450, 12, InfrastructureRequirement.RAIL,
            "Diesel-powered locomotive with improved speed and reliability (NUTS)"),

    ELECTRIC_LOCOMOTIVE("Electric Locomotive", 8, 100, 80, 600, 15, InfrastructureRequirement.RAIL,
            "Electric train requiring electrified railway — clean, efficient transport (NUTS)"),

    MONORAIL_TRAIN("Monorail Train", 10, 120, 100, 800, 20, InfrastructureRequirement.MONORAIL,
            "Advanced monorail system for high-tech planets — elevated tracks (NUTS)"),

    MAGLEV_TRAIN("Maglev Train", 12, 150, 120, 1000, 25, InfrastructureRequirement.MAGLEV,
            "Magnetic levitation train — pinnacle of conventional rail (Modern Maglev)"),

    FRIMS_MAGLEV("FRIMS Futuristic Maglev", 15, 200, 150, 1500, 30, InfrastructureRequirement.MAGLEV,
            "Futuristic maglev for space-faring civilizations (FRIMS)"),

    // ---- Water Vehicles ----

    CARGO_SHIP("Cargo Ship", 2, 80, 0, 300, 10, InfrastructureRequirement.WATER,
            "Large cargo ship for water transport"),

    PASSENGER_FERRY("Passenger Ferry", 3, 10, 50, 250, 8, InfrastructureRequirement.WATER,
            "Passenger ferry for water transport"),

    // ---- Air Vehicles ----

    AIRCRAFT("Aircraft", 10, 10, 10, 800, 25, InfrastructureRequirement.AIRPORT,
            "Fast air transport for cargo and passengers"),

    HELICOPTER("Helicopter", 6, 5, 5, 400, 15, InfrastructureRequirement.AIRPORT,
            "Short-range helicopter transport — flexible operations"),

    FICTIONAL_AIRCRAFT("Fictional Aircraft", 15, 20, 20, 1500, 40, InfrastructureRequirement.AIRPORT,
            "Fictional aircraft and spacecraft designs for advanced transport (Kaaskrokets)"),

    // ---- OpenTTD Extended Water Vehicles ----

    ADVANCED_CARGO_LINER("Advanced Cargo Liner", 3, 200, 0, 800, 20, InfrastructureRequirement.WATER,
            "High-capacity container ship for interplanetary trade routes"),

    // ---- Space Vehicles (SpaceY, Apollo Rocket Set) ----

    ROCKET("Rocket", 20, 30, 10, 3000, 60, InfrastructureRequirement.SPACEPORT,
            "Space-themed rocket for orbital and interplanetary travel (SpaceY)"),

    APOLLO_ROCKET("Apollo Rocket", 25, 50, 15, 5000, 100, InfrastructureRequirement.SPACEPORT,
            "Full space logistics rocket for interplanetary travel (Apollo Rocket Set)"),

    SPACE_STATION("Orbital Space Station", 0, 500, 100, 10000, 200, InfrastructureRequirement.ORBITAL_STATION,
            "Orbital station serving as a hub for interplanetary transport");

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
        AIRPORT,
        /** Requires monorail track */
        MONORAIL,
        /** Requires maglev track */
        MAGLEV,
        /** Requires spaceport for launch and landing */
        SPACEPORT,
        /** Requires orbital station docking */
        ORBITAL_STATION
    }
}
