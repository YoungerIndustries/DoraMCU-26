package com.yisy.modules.doramcu.babylon5.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Provides refueling, repair, and maintenance services at a B5 space station.
 * <p>
 * The service provider handles all ship maintenance operations:
 * </p>
 * <ul>
 *   <li>Refueling — filling fusion fuel tanks for jump travel</li>
 *   <li>Hull repair — restoring damaged hull points</li>
 *   <li>System repair — fixing damaged weapon systems and drives</li>
 *   <li>Maintenance — routine upkeep to prevent future damage</li>
 * </ul>
 * <p>
 * Service costs vary by station type. Major stations like Babylon 5 offer
 * the best rates, while remote outposts charge premium prices.
 * </p>
 * <p>
 * B5 stations include Babylon 5, Earth Force stations, Minbari temples,
 * Centauri ports, and Narn military bases.
 * </p>
 */
public class B5StationService {

    /**
     * Base fuel cost per ton, in credits.
     */
    private static final int BASE_FUEL_COST_PER_TON = 100;

    /**
     * Base hull repair cost per hull point, in credits.
     */
    private static final int BASE_HULL_REPAIR_COST = 500;

    /**
     * Base system repair cost per system, in credits.
     */
    private static final int BASE_SYSTEM_REPAIR_COST = 10000;

    /**
     * Base maintenance cost per 1000 tons, in credits per week.
     */
    private static final int BASE_MAINTENANCE_COST_PER_TON = 5;

    private final B5StationType stationType;
    private final String stationName;
    private final List<ServiceRecord> serviceLog;

    /**
     * Types of B5 space stations, each with different service rates.
     */
    public enum B5StationType {
        /**
         * Major station like Babylon 5 — best rates, full services.
         */
        MAJOR("Major Station", 0.8, 0.8),

        /**
         * Earth Force military station — good rates, military priority.
         */
        MILITARY("Military Station", 0.9, 0.9),

        /**
         * Standard commercial station — average rates.
         */
        COMMERCIAL("Commercial Station", 1.0, 1.0),

        /**
         * Minbari temple station — spiritual focus, limited services.
         */
        TEMPLE("Temple Station", 1.2, 1.5),

        /**
         * Remote outpost — expensive, limited services.
         */
        OUTPOST("Remote Outpost", 1.5, 2.0),

        /**
         * Shadow/Vorlon hidden station — extremely expensive, rare.
         */
        HIDDEN("Hidden Station", 2.0, 2.5);

        private final String name;
        private final double serviceRateMultiplier;
        private final double repairRateMultiplier;

        B5StationType(String name, double serviceRateMultiplier, double repairRateMultiplier) {
            this.name = name;
            this.serviceRateMultiplier = serviceRateMultiplier;
            this.repairRateMultiplier = repairRateMultiplier;
        }

        public String getName() {
            return name;
        }

        public double getServiceRateMultiplier() {
            return serviceRateMultiplier;
        }

        public double getRepairRateMultiplier() {
            return repairRateMultiplier;
        }
    }

    /**
     * Record of a service performed at the station.
     */
    public static class ServiceRecord {
        private final String shipName;
        private final ServiceType serviceType;
        private final int cost;
        private final String description;
        private final int tick;

        ServiceRecord(String shipName, ServiceType serviceType, int cost, String description, int tick) {
            this.shipName = shipName;
            this.serviceType = serviceType;
            this.cost = cost;
            this.description = description;
            this.tick = tick;
        }

        public String getShipName() {
            return shipName;
        }

        public ServiceType getServiceType() {
            return serviceType;
        }

        public int getCost() {
            return cost;
        }

        public String getDescription() {
            return description;
        }

        public int getTick() {
            return tick;
        }
    }

    /**
     * Types of services available at stations.
     */
    public enum ServiceType {
        REFUEL("Refuel"),
        HULL_REPAIR("Hull Repair"),
        SYSTEM_REPAIR("System Repair"),
        MAINTENANCE("Maintenance"),
        CARGO_HANDLING("Cargo Handling"),
        CREW_QUARTERS("Crew Quarters");

        private final String name;

        ServiceType(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    public B5StationService(String stationName, B5StationType stationType) {
        this.stationName = stationName;
        this.stationType = stationType;
        this.serviceLog = new ArrayList<>();
    }

    // ---- Accessors ----

    /**
     * Returns the station name.
     *
     * @return the station name
     */
    public String getStationName() {
        return stationName;
    }

    /**
     * Returns the station type.
     *
     * @return the station type
     */
    public B5StationType getStationType() {
        return stationType;
    }

    /**
     * Returns the service rate multiplier for this station.
     * <p>
     * Lower values mean cheaper services. Major stations have the best rates (0.8×),
     * while hidden stations have the worst (2.0×).
     * </p>
     *
     * @return the service rate multiplier
     */
    public double getServiceRateMultiplier() {
        return stationType.getServiceRateMultiplier();
    }

    /**
     * Returns the repair rate multiplier for this station.
     *
     * @return the repair rate multiplier
     */
    public double getRepairRateMultiplier() {
        return stationType.getRepairRateMultiplier();
    }

    /**
     * Returns the service log.
     *
     * @return unmodifiable list of service records
     */
    public List<ServiceRecord> getServiceLog() {
        return Collections.unmodifiableList(serviceLog);
    }

    // ---- Refueling ----

    /**
     * Refuels a ship's fuel tank at this station.
     * <p>
     * The cost is based on the amount of fuel added, the station type,
     * and the ship's fuel capacity.
     * </p>
     *
     * @param shipName the name of the ship
     * @param fuelTons the amount of fuel to add in tons
     * @param currentFuel the current fuel level in tons
     * @param maxFuel the maximum fuel capacity in tons
     * @return the cost in credits
     */
    public int refuel(String shipName, int fuelTons, int currentFuel, int maxFuel) {
        if (fuelTons <= 0) {
            return 0;
        }

        int actualFuel = Math.min(fuelTons, maxFuel - currentFuel);
        if (actualFuel <= 0) {
            return 0;
        }

        int cost = (int) (actualFuel * BASE_FUEL_COST_PER_TON * getServiceRateMultiplier());

        serviceLog.add(new ServiceRecord(
                shipName,
                ServiceType.REFUEL,
                cost,
                "Refueled " + actualFuel + " tons at " + stationName,
                0
        ));

        return cost;
    }

    // ---- Hull Repair ----

    /**
     * Repairs hull damage on a ship.
     * <p>
     * The cost is based on the amount of hull damage, the station type,
     * and the ship's construction cost.
     * </p>
     *
     * @param shipName the name of the ship
     * @param hullDamage the amount of hull damage to repair
     * @param totalHull the total hull points of the ship
     * @return the cost in credits
     */
    public int repairHull(String shipName, int hullDamage, int totalHull) {
        if (hullDamage <= 0) {
            return 0;
        }

        // Cost scales with percentage of hull damaged
        double damagePercentage = (double) hullDamage / totalHull;
        int cost = (int) (hullDamage * BASE_HULL_REPAIR_COST * getRepairRateMultiplier() * (1.0 + damagePercentage));

        serviceLog.add(new ServiceRecord(
                shipName,
                ServiceType.HULL_REPAIR,
                cost,
                "Repaired " + hullDamage + " hull points at " + stationName,
                0
        ));

        return cost;
    }

    // ---- System Repair ----

    /**
     * Repairs damaged systems on a ship.
     * <p>
     * The cost is based on the number of damaged systems, the station type,
     * and the complexity of the systems.
     * </p>
     *
     * @param shipName the name of the ship
     * @param damagedSystems the number of damaged systems
     * @return the cost in credits
     */
    public int repairSystems(String shipName, int damagedSystems) {
        if (damagedSystems <= 0) {
            return 0;
        }

        int cost = (int) (damagedSystems * BASE_SYSTEM_REPAIR_COST * getRepairRateMultiplier());

        serviceLog.add(new ServiceRecord(
                shipName,
                ServiceType.SYSTEM_REPAIR,
                cost,
                "Repaired " + damagedSystems + " systems at " + stationName,
                0
        ));

        return cost;
    }

    // ---- Maintenance ----

    /**
     * Performs routine maintenance on a ship.
     * <p>
     * The cost is based on the ship's tonnage and the station type.
     * Regular maintenance prevents future damage and system failures.
     * </p>
     *
     * @param shipName the name of the ship
     * @param tonnage the ship's tonnage
     * @return the cost in credits
     */
    public int maintenance(String shipName, int tonnage) {
        int cost = (int) ((tonnage / 1000.0) * BASE_MAINTENANCE_COST_PER_TON * getServiceRateMultiplier());
        cost = Math.max(cost, 100); // Minimum maintenance cost

        serviceLog.add(new ServiceRecord(
                shipName,
                ServiceType.MAINTENANCE,
                cost,
                "Routine maintenance for " + tonnage + " ton ship at " + stationName,
                0
        ));

        return cost;
    }

    // ---- Cargo Handling ----

    /**
     * Handles cargo loading/unloading at the station.
     * <p>
     * The cost is based on the cargo tonnage and the station type.
     * </p>
     *
     * @param shipName the name of the ship
     * @param cargoTons the amount of cargo in tons
     * @return the cost in credits
     */
    public int cargoHandling(String shipName, double cargoTons) {
        int cost = (int) (cargoTons * 10 * getServiceRateMultiplier());
        cost = Math.max(cost, 50); // Minimum cargo handling cost

        serviceLog.add(new ServiceRecord(
                shipName,
                ServiceType.CARGO_HANDLING,
                cost,
                "Handled " + cargoTons + " tons of cargo at " + stationName,
                0
        ));

        return cost;
    }

    // ---- Crew Quarters ----

    /**
     * Provides crew quarters at the station.
     * <p>
     * The cost is based on the crew size and the station type.
     * </p>
     *
     * @param shipName the name of the ship
     * @param crewSize the number of crew members
     * @param weeks the number of weeks
     * @return the cost in credits
     */
    public int crewQuarters(String shipName, int crewSize, int weeks) {
        int costPerWeek = (int) (crewSize * 50 * getServiceRateMultiplier());
        int cost = costPerWeek * weeks;

        serviceLog.add(new ServiceRecord(
                shipName,
                ServiceType.CREW_QUARTERS,
                cost,
                "Housed " + crewSize + " crew for " + weeks + " weeks at " + stationName,
                0
        ));

        return cost;
    }

    // ---- Full Service ----

    /**
     * Performs a full service on a ship: refuel, repair, and maintenance.
     *
     * @param shipName the name of the ship
     * @param fuelTons the amount of fuel to add
     * @param currentFuel the current fuel level
     * @param maxFuel the maximum fuel capacity
     * @param hullDamage the amount of hull damage
     * @param totalHull the total hull points
     * @param damagedSystems the number of damaged systems
     * @param tonnage the ship's tonnage
     * @return the total cost in credits
     */
    public int fullService(String shipName, int fuelTons, int currentFuel, int maxFuel,
                           int hullDamage, int totalHull, int damagedSystems, int tonnage) {
        int refuelCost = refuel(shipName, fuelTons, currentFuel, maxFuel);
        int hullCost = repairHull(shipName, hullDamage, totalHull);
        int systemCost = repairSystems(shipName, damagedSystems);
        int maintenanceCost = maintenance(shipName, tonnage);

        return refuelCost + hullCost + systemCost + maintenanceCost;
    }

    /**
     * Generates a summary of station services.
     *
     * @return a map with station info and service statistics
     */
    public java.util.Map<String, Object> getStationSummary() {
        java.util.Map<String, Object> summary = new java.util.LinkedHashMap<>();
        summary.put("stationName", stationName);
        summary.put("stationType", stationType.getName());
        summary.put("serviceRateMultiplier", getServiceRateMultiplier());
        summary.put("repairRateMultiplier", getRepairRateMultiplier());
        summary.put("totalServices", serviceLog.size());

        int totalRevenue = serviceLog.stream().mapToInt(ServiceRecord::getCost).sum();
        summary.put("totalRevenue", totalRevenue);

        return summary;
    }
}
