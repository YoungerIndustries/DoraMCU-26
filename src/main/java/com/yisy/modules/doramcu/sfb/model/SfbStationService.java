package com.yisy.modules.doramcu.sfb.model;

/**
 * Station services for SFB.
 * <p>
 * Provides refueling, hull repair, system repair, maintenance,
 * cargo handling, and crew quarters at starbases and stations.
 * </p>
 */
public class SfbStationService {

    private final String stationName;
    private final SfbStationType stationType;

    public SfbStationService(String stationName, SfbStationType stationType) {
        this.stationName = stationName;
        this.stationType = stationType;
    }

    /**
     * Refuels a ship.
     *
     * @param shipName name of the ship
     * @param capacity fuel capacity in tons
     * @param currentFuel current fuel level
     * @param costPerTon cost per ton of fuel
     * @return total cost
     */
    public int refuel(String shipName, int capacity, int currentFuel, int costPerTon) {
        int needed = capacity - currentFuel;
        if (needed <= 0) return 0;
        return (int) (needed * costPerTon * stationType.getRefuelMultiplier());
    }

    /**
     * Repairs hull damage.
     *
     * @param shipName name of the ship
     * @param damagePoints current damage points
     * @param costPerPoint cost per damage point
     * @return total cost
     */
    public int repairHull(String shipName, int damagePoints, int costPerPoint) {
        if (damagePoints <= 0) return 0;
        return (int) (damagePoints * costPerPoint * stationType.getRepairMultiplier());
    }

    /**
     * Repairs system damage.
     *
     * @param shipName name of the ship
     * @param systemsDamaged number of systems damaged
     * @param costPerSystem cost per system
     * @return total cost
     */
    public int repairSystems(String shipName, int systemsDamaged, int costPerSystem) {
        if (systemsDamaged <= 0) return 0;
        return (int) (systemsDamaged * costPerSystem * stationType.getRepairMultiplier());
    }

    /**
     * Performs routine maintenance.
     *
     * @param shipName name of the ship
     * @param baseCost base maintenance cost
     * @return total cost
     */
    public int maintenance(String shipName, int baseCost) {
        return (int) (baseCost * stationType.getMaintenanceMultiplier());
    }

    /**
     * Handles cargo operations.
     *
     * @param cargoTons tons of cargo
     * @param costPerTon cost per ton
     * @return total cost
     */
    public int cargoHandling(int cargoTons, int costPerTon) {
        return cargoTons * costPerTon;
    }

    /**
     * Provides crew quarters.
     *
     * @param crewSize number of crew members
     * @param costPerCrew cost per crew member
     * @return total cost
     */
    public int crewQuarters(int crewSize, int costPerCrew) {
        return crewSize * costPerCrew;
    }

    public String getStationName() {
        return stationName;
    }

    public SfbStationType getStationType() {
        return stationType;
    }

    /**
     * Types of stations in SFB.
     */
    public enum SfbStationType {
        MAJOR("Major Station", 1, 1, 1, true, true, true),
        MILITARY("Military Base", 1, 0.8, 1.2, true, true, false),
        COMMERCIAL("Commercial Hub", 1.2, 1.2, 0.8, true, true, true),
        STARBASE("Starbase", 0.8, 0.8, 0.8, true, true, true),
        OUTPOST("Outpost", 1.5, 1.5, 1.5, false, false, false),
        HIDDEN("Hidden Base", 2, 2, 2, false, false, false);

        private final String name;
        private final double refuelMultiplier;
        private final double repairMultiplier;
        private final double maintenanceMultiplier;
        private final boolean hasCargoHandling;
        private final boolean hasCrewQuarters;
        private final boolean hasRepairs;

        SfbStationType(String name, double refuelMultiplier, double repairMultiplier,
                       double maintenanceMultiplier, boolean hasCargoHandling,
                       boolean hasCrewQuarters, boolean hasRepairs) {
            this.name = name;
            this.refuelMultiplier = refuelMultiplier;
            this.repairMultiplier = repairMultiplier;
            this.maintenanceMultiplier = maintenanceMultiplier;
            this.hasCargoHandling = hasCargoHandling;
            this.hasCrewQuarters = hasCrewQuarters;
            this.hasRepairs = hasRepairs;
        }

        public String getName() { return name; }
        public double getRefuelMultiplier() { return refuelMultiplier; }
        public double getRepairMultiplier() { return repairMultiplier; }
        public double getMaintenanceMultiplier() { return maintenanceMultiplier; }
        public boolean hasCargoHandling() { return hasCargoHandling; }
        public boolean hasCrewQuarters() { return hasCrewQuarters; }
        public boolean hasRepairs() { return hasRepairs; }
    }
}
