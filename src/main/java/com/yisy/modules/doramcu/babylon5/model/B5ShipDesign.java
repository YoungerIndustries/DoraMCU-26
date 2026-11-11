package com.yisy.modules.doramcu.babylon5.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Represents a complete Babylon 5 starship design based on tonnage allocation.
 * <p>
 * A ship design is defined by its total tonnage and the allocation of that tonnage
 * to various systems: hull, jump drive, fusion drive, power plant, life support,
 * computers, cargo, passengers, and weapons. This mirrors the Traveller ship design
 * system but uses B5-themed components and terminology.
 * </p>
 * <p>
 * The design process follows these steps:
 * </p>
 * <ol>
 *   <li>Set total tonnage (e.g., 2D6×1000 for military, 4D6×1000 for merchant)</li>
 *   <li>Allocate tons to hull type (determines HP and cost)</li>
 *   <li>Allocate tons to jump drive (jump rating)</li>
 *   <li>Allocate tons to fusion drive (maneuver rating)</li>
 *   <li>Allocate tons to power plant</li>
 *   <li>Allocate tons to life support</li>
 *   <li>Allocate tons to shipboard computers</li>
 *   <li>Allocate remaining tons to cargo, passengers, and weapons</li>
 * </ol>
 * <p>
 * This class validates that all allocations fit within the total tonnage
 * and calculates derived statistics like crew count, ship cost, and maintenance.
 * </p>
 */
public class B5ShipDesign {

    /**
     * Hull types for B5 ships, based on faction technology levels.
     */
    public enum HullType {
        /** Basic hull, used by Narn and older Earth Alliance ships. */
        STANDARD("Standard", 1.0, 10),
        /** Reinforced hull, used by Minbari and modern Earth Alliance ships. */
        REINFORCED("Reinforced", 1.5, 15),
        /** Advanced hull, used by Centauri and Vorlon ships. */
        ADVANCED("Advanced", 2.0, 20),
        /** Shadow-grade hull, extremely durable. */
        SHADOW_GRADE("Shadow-Grade", 3.0, 30);

        private final String displayName;
        private final double costMultiplier;
        private final int hullPointsPer1000Tons;

        HullType(String displayName, double costMultiplier, int hullPointsPer1000Tons) {
            this.displayName = displayName;
            this.costMultiplier = costMultiplier;
            this.hullPointsPer1000Tons = hullPointsPer1000Tons;
        }

        public String getDisplayName() {
            return displayName;
        }

        public double getCostMultiplier() {
            return costMultiplier;
        }

        public int getHullPointsPer1000Tons() {
            return hullPointsPer1000Tons;
        }
    }

    /**
     * Jump drive ratings for B5 ships.
     * Jump drives allow faster-than-light travel between star systems.
     */
    public enum JumpDrive {
        NONE("No Jump", 0, 0),
        J1("Jump-1", 1, 1),
        J2("Jump-2", 2, 2),
        J3("Jump-3", 3, 3),
        J4("Jump-4", 4, 4),
        J5("Jump-5", 5, 5),
        J6("Jump-6", 6, 6);

        private final String displayName;
        private final int rating;
        private final int tonsPer1000Tonnage;

        JumpDrive(String displayName, int rating, int tonsPer1000Tonnage) {
            this.displayName = displayName;
            this.rating = rating;
            this.tonsPer1000Tonnage = tonsPer1000Tonnage;
        }

        public String getDisplayName() {
            return displayName;
        }

        public int getRating() {
            return rating;
        }

        /**
         * Calculates tons required for this jump drive based on ship tonnage.
         * Formula: (totalTonnage / 1000) × tonsPer1000Tonnage
         */
        public int getTonsRequired(int totalTonnage) {
            if (rating == 0) return 0;
            return (totalTonnage / 1000) * tonsPer1000Tonnage;
        }
    }

    /**
     * Fusion drive (maneuver) ratings for B5 ships.
     * Fusion drives provide sub-light acceleration.
     */
    public enum FusionDrive {
        NONE("No Fusion", 0, 0),
        M1("Fusion-1", 1, 1),
        M2("Fusion-2", 2, 2),
        M3("Fusion-3", 3, 3),
        M4("Fusion-4", 4, 4),
        M5("Fusion-5", 5, 5),
        M6("Fusion-6", 6, 6),
        M7("Fusion-7", 7, 7),
        M8("Fusion-8", 8, 8),
        M9("Fusion-9", 9, 9);

        private final String displayName;
        private final int rating;
        private final int tonsPer1000Tonnage;

        FusionDrive(String displayName, int rating, int tonsPer1000Tonnage) {
            this.displayName = displayName;
            this.rating = rating;
            this.tonsPer1000Tonnage = tonsPer1000Tonnage;
        }

        public String getDisplayName() {
            return displayName;
        }

        public int getRating() {
            return rating;
        }

        /**
         * Calculates tons required for this fusion drive based on ship tonnage.
         */
        public int getTonsRequired(int totalTonnage) {
            if (rating == 0) return 0;
            return (totalTonnage / 1000) * tonsPer1000Tonnage;
        }
    }

    /**
     * Power plant sizes for B5 ships.
     */
    public enum PowerPlant {
        NONE("No Power", 0, 0),
        SMALL("Small", 1, 1),
        MEDIUM("Medium", 2, 2),
        LARGE("Large", 3, 3),
        VORLON("Vorlon", 4, 4),
        SHADOW("Shadow", 5, 5);

        private final String displayName;
        private final int size;
        private final int tonsPer1000Tonnage;

        PowerPlant(String displayName, int size, int tonsPer1000Tonnage) {
            this.displayName = displayName;
            this.size = size;
            this.tonsPer1000Tonnage = tonsPer1000Tonnage;
        }

        public String getDisplayName() {
            return displayName;
        }

        public int getSize() {
            return size;
        }

        public int getTonsRequired(int totalTonnage) {
            if (size == 0) return 0;
            return (totalTonnage / 1000) * tonsPer1000Tonnage;
        }

        public int getCrewRequired() {
            return size;
        }
    }

    /**
     * Life support types for B5 ships.
     */
    public enum LifeSupport {
        NONE("No Life Support", 0),
        BASIC("Basic", 1),
        STANDARD("Standard", 2),
        LUXURY("Luxury", 3),
        VORLON("Vorlon", 4);

        private final String displayName;
        private final int tonsPer1000Tonnage;

        LifeSupport(String displayName, int tonsPer1000Tonnage) {
            this.displayName = displayName;
            this.tonsPer1000Tonnage = tonsPer1000Tonnage;
        }

        public String getDisplayName() {
            return displayName;
        }

        public int getTonsRequired(int totalTonnage) {
            if (tonsPer1000Tonnage == 0) return 0;
            return (totalTonnage / 1000) * tonsPer1000Tonnage;
        }
    }

    /**
     * Shipboard computer classes for B5 ships.
     */
    public enum ShipComputer {
        NONE("No Computer", 0, 0),
        CLASS0("Class 0", 0, 0),
        CLASS1("Class 1", 1, 1),
        CLASS2("Class 2", 2, 2),
        CLASS3("Class 3", 3, 3),
        CLASS4("Class 4", 4, 4),
        CLASS5("Class 5", 5, 5),
        VORLON("Vorlon AI", 6, 6);

        private final String displayName;
        private final int classLevel;
        private final int tonsPer1000Tonnage;

        ShipComputer(String displayName, int classLevel, int tonsPer1000Tonnage) {
            this.displayName = displayName;
            this.classLevel = classLevel;
            this.tonsPer1000Tonnage = tonsPer1000Tonnage;
        }

        public String getDisplayName() {
            return displayName;
        }

        public int getClassLevel() {
            return classLevel;
        }

        public int getTonsRequired(int totalTonnage) {
            if (classLevel == 0 && this != CLASS0) return 0;
            return (totalTonnage / 1000) * tonsPer1000Tonnage;
        }

        public int getGunneryBonus() {
            return classLevel / 2;
        }
    }

    /**
     * Passenger comfort classes for B5 ships.
     */
    public enum PassengerClass {
        FIRST("First Class", 100),
        SECOND("Second Class", 50),
        THIRD("Third Class", 25),
        STEERAGE("Steerage", 10);

        private final String displayName;
        private final int tonsPerPassenger;

        PassengerClass(String displayName, int tonsPerPassenger) {
            this.displayName = displayName;
            this.tonsPerPassenger = tonsPerPassenger;
        }

        public String getDisplayName() {
            return displayName;
        }

        public int getTonsRequired(int passengerCount) {
            return (passengerCount * tonsPerPassenger) / 1000;
        }
    }

    // ==================== Fields ====================

    private final int id;
    private String name;
    private final int totalTonnage;
    private String faction; // "Earth Alliance", "Narn", "Centauri", "Minbari", etc.
    private String shipClass; // "Corvette", "Destroyer", "Cruiser", "Battle Cruiser", "Freighter", etc.

    private HullType hullType;
    private JumpDrive jumpDrive;
    private FusionDrive fusionDrive;
    private PowerPlant powerPlant;
    private LifeSupport lifeSupport;
    private ShipComputer computer;

    private int cargoTons;
    private int passengerCount;
    private PassengerClass passengerClass;
    private final List<B5ShipWeapon> weapons;
    private int fuelTons;

    // ==================== Constructor ====================

    /**
     * Creates a new ship design with the given ID, name, and total tonnage.
     *
     * @param id the unique design identifier
     * @param name the ship's name
     * @param totalTonnage the total ship tonnage in metric tons (minimum 1000)
     */
    public B5ShipDesign(int id, String name, int totalTonnage) {
        if (totalTonnage < 1000) {
            throw new IllegalArgumentException("Minimum ship tonnage is 1000 tons");
        }
        this.id = id;
        this.name = Objects.requireNonNull(name, "Name must not be null");
        this.totalTonnage = totalTonnage;
        this.hullType = HullType.STANDARD;
        this.jumpDrive = JumpDrive.NONE;
        this.fusionDrive = FusionDrive.NONE;
        this.powerPlant = PowerPlant.NONE;
        this.lifeSupport = LifeSupport.NONE;
        this.computer = ShipComputer.NONE;
        this.cargoTons = 0;
        this.passengerCount = 0;
        this.passengerClass = PassengerClass.THIRD;
        this.weapons = new ArrayList<>();
        this.fuelTons = 0;
    }

    // ==================== Accessors ====================

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = Objects.requireNonNull(name, "Name must not be null");
    }

    public int getTotalTonnage() {
        return totalTonnage;
    }

    public String getFaction() {
        return faction;
    }

    public void setFaction(String faction) {
        this.faction = faction;
    }

    public String getShipClass() {
        return shipClass;
    }

    public void setShipClass(String shipClass) {
        this.shipClass = shipClass;
    }

    public HullType getHullType() {
        return hullType;
    }

    public void setHullType(HullType hullType) {
        this.hullType = Objects.requireNonNull(hullType, "Hull type must not be null");
    }

    public JumpDrive getJumpDrive() {
        return jumpDrive;
    }

    public void setJumpDrive(JumpDrive jumpDrive) {
        this.jumpDrive = Objects.requireNonNull(jumpDrive, "Jump drive must not be null");
    }

    public FusionDrive getFusionDrive() {
        return fusionDrive;
    }

    public void setFusionDrive(FusionDrive fusionDrive) {
        this.fusionDrive = Objects.requireNonNull(fusionDrive, "Fusion drive must not be null");
    }

    public PowerPlant getPowerPlant() {
        return powerPlant;
    }

    public void setPowerPlant(PowerPlant powerPlant) {
        this.powerPlant = Objects.requireNonNull(powerPlant, "Power plant must not be null");
    }

    public LifeSupport getLifeSupport() {
        return lifeSupport;
    }

    public void setLifeSupport(LifeSupport lifeSupport) {
        this.lifeSupport = Objects.requireNonNull(lifeSupport, "Life support must not be null");
    }

    public ShipComputer getComputer() {
        return computer;
    }

    public void setComputer(ShipComputer computer) {
        this.computer = Objects.requireNonNull(computer, "Computer must not be null");
    }

    public int getCargoTons() {
        return cargoTons;
    }

    public void setCargoTons(int cargoTons) {
        if (cargoTons < 0) {
            throw new IllegalArgumentException("Cargo tons cannot be negative");
        }
        this.cargoTons = cargoTons;
    }

    public int getPassengerCount() {
        return passengerCount;
    }

    public void setPassengerCount(int passengerCount) {
        if (passengerCount < 0) {
            throw new IllegalArgumentException("Passenger count cannot be negative");
        }
        this.passengerCount = passengerCount;
    }

    public PassengerClass getPassengerClass() {
        return passengerClass;
    }

    public void setPassengerClass(PassengerClass passengerClass) {
        this.passengerClass = Objects.requireNonNull(passengerClass, "Passenger class must not be null");
    }

    public int getFuelTons() {
        return fuelTons;
    }

    public void setFuelTons(int fuelTons) {
        if (fuelTons < 0) {
            throw new IllegalArgumentException("Fuel tons cannot be negative");
        }
        this.fuelTons = fuelTons;
    }

    // ==================== Weapons ====================

    /**
     * A weapon system on a B5 ship.
     */
    public static class B5ShipWeapon {
        private final String name;
        private final String type; // "Laser", "Missile", "Particle Beam", "Plasma", "Torpedo"
        private final int count;
        private final int tonsPerUnit;
        private final int damageDice;
        private final int damageMultiplier;
        private final boolean isMissile;

        public B5ShipWeapon(String name, String type, int count, int tonsPerUnit,
                            int damageDice, int damageMultiplier) {
            this.name = Objects.requireNonNull(name, "Weapon name must not be null");
            this.type = Objects.requireNonNull(type, "Weapon type must not be null");
            this.count = count;
            this.tonsPerUnit = tonsPerUnit;
            this.damageDice = damageDice;
            this.damageMultiplier = damageMultiplier;
            this.isMissile = "Missile".equals(type) || "Torpedo".equals(type);
        }

        public String getName() {
            return name;
        }

        public String getType() {
            return type;
        }

        public int getCount() {
            return count;
        }

        public int getTonsPerUnit() {
            return tonsPerUnit;
        }

        /** Total tons for all units of this weapon. */
        public int getTotalTons() {
            return count * tonsPerUnit;
        }

        public int getDamageDice() {
            return damageDice * count;
        }

        public double getAverageDamage() {
            return (damageDice * 3.5 * damageMultiplier) * count;
        }

        public boolean isMissile() {
            return isMissile;
        }

        @Override
        public String toString() {
            return String.format("%s (%s ×%d, %dD6×%d)", name, type, count, damageDice, damageMultiplier);
        }
    }

    public List<B5ShipWeapon> getWeapons() {
        return Collections.unmodifiableList(weapons);
    }

    public void addWeapon(B5ShipWeapon weapon) {
        Objects.requireNonNull(weapon, "Weapon must not be null");
        weapons.add(weapon);
    }

    public void removeWeapon(B5ShipWeapon weapon) {
        weapons.remove(weapon);
    }

    // ==================== Calculated Statistics ====================

    /**
     * Calculates the total tons consumed by all fixed systems
     * (jump drive, fusion drive, power plant, life support, computer).
     */
    public int getFixedSystemTons() {
        return jumpDrive.getTonsRequired(totalTonnage)
                + fusionDrive.getTonsRequired(totalTonnage)
                + powerPlant.getTonsRequired(totalTonnage)
                + lifeSupport.getTonsRequired(totalTonnage)
                + computer.getTonsRequired(totalTonnage);
    }

    /**
     * Calculates the total tons consumed by weapons.
     */
    public int getWeaponTons() {
        return weapons.stream().mapToInt(B5ShipWeapon::getTotalTons).sum();
    }

    /**
     * Calculates the tons consumed by passengers.
     */
    public int getPassengerTons() {
        return passengerClass.getTonsRequired(passengerCount);
    }

    /**
     * Calculates the total allocated tons (fixed systems + weapons + passengers + cargo + fuel).
     */
    public int getTotalAllocatedTons() {
        return getFixedSystemTons() + getWeaponTons() + getPassengerTons() + cargoTons + fuelTons;
    }

    /**
     * Returns the remaining unallocated tons.
     */
    public int getRemainingTons() {
        return totalTonnage - getTotalAllocatedTons();
    }

    /**
     * Validates that the design does not exceed total tonnage.
     */
    public boolean isValid() {
        return getTotalAllocatedTons() <= totalTonnage;
    }

    /**
     * Calculates the ship's hull points based on tonnage and hull type.
     */
    public int getHullPoints() {
        return (totalTonnage / 1000) * hullType.getHullPointsPer1000Tons();
    }

    /**
     * Calculates the estimated crew count.
     * Formula: (totalTonnage / 100) + drive crew + weapon crew + power plant crew.
     */
    public int getEstimatedCrew() {
        int base = totalTonnage / 100;
        int driveCrew = jumpDrive.getRating() + fusionDrive.getRating();
        int weaponCrew = weapons.size();
        int powerCrew = powerPlant.getCrewRequired();
        return base + driveCrew + weaponCrew + powerCrew;
    }

    /**
     * Calculates the ship's construction cost in credits.
     */
    public long getConstructionCost() {
        long hullCost = (long) totalTonnage * 100 * (long) hullType.getCostMultiplier();
        long jumpCost = (long) jumpDrive.getRating() * totalTonnage * 50;
        long fusionCost = (long) fusionDrive.getRating() * totalTonnage * 10;
        long powerCost = (long) powerPlant.getSize() * totalTonnage * 5;
        long lifeSupportCost = (long) lifeSupport.getTonsRequired(totalTonnage) * 10;
        long computerCost = (long) computer.getClassLevel() * totalTonnage * 2;
        long weaponCost = (long) getWeaponTons() * 50;
        return hullCost + jumpCost + fusionCost + powerCost + lifeSupportCost + computerCost + weaponCost;
    }

    /**
     * Calculates the annual maintenance cost in credits.
     * Formula: construction cost × 0.5%.
     */
    public long getAnnualMaintenanceCost() {
        return getConstructionCost() / 200;
    }

    /**
     * Returns the maximum jump distance in parsecs.
     */
    public int getMaxJumpParsecs() {
        return jumpDrive.getRating();
    }

    /**
     * Returns the maneuver rating.
     */
    public int getManeuverRating() {
        return fusionDrive.getRating();
    }

    /**
     * Calculates the total average damage output of all weapons.
     */
    public double getTotalAverageDamage() {
        return weapons.stream().mapToDouble(B5ShipWeapon::getAverageDamage).sum();
    }

    /**
     * Returns the total number of D6 damage dice from all weapons.
     */
    public int getTotalDamageDice() {
        return weapons.stream().mapToInt(B5ShipWeapon::getDamageDice).sum();
    }

    /**
     * Returns the gunnery dice modifier from the ship's computer.
     */
    public int getGunneryBonus() {
        return computer.getGunneryBonus();
    }

    // ==================== Design Validation ====================

    /**
     * Validates the complete design and throws if invalid.
     *
     * @throws IllegalStateException if the design exceeds total tonnage or has invalid configuration
     */
    public void validate() {
        int allocated = getTotalAllocatedTons();
        if (allocated > totalTonnage) {
            throw new IllegalStateException(
                "Design exceeds total tonnage: " + allocated + " > " + totalTonnage +
                " (overflow: " + (allocated - totalTonnage) + " tons)"
            );
        }
        if (jumpDrive.getRating() > 0 && computer == ShipComputer.NONE) {
            throw new IllegalStateException("Jump drive requires at least a Class 0 computer");
        }
        if (!weapons.isEmpty() && powerPlant == PowerPlant.NONE) {
            throw new IllegalStateException("Weapons require a power plant");
        }
        if (weapons.stream().anyMatch(B5ShipWeapon::isMissile) &&
            computer.getClassLevel() < 2) {
            throw new IllegalStateException("Missile targeting requires at least a Class 2 computer");
        }
    }

    // ==================== toString ====================

    @Override
    public String toString() {
        return String.format(
            "B5ShipDesign{id=%d, name='%s', faction='%s', %d tons, J-%d, M-%d, %d HP, crew=%d, cost=%d cr}",
            id, name, faction, totalTonnage, jumpDrive.getRating(), fusionDrive.getRating(),
            getHullPoints(), getEstimatedCrew(), getConstructionCost()
        );
    }
}
