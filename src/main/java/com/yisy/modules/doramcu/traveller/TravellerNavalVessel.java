package com.yisy.modules.doramcu.traveller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Represents a Traveller naval vessel that can be used in naval battles.
 * <p>
 * Unlike {@link ShipDesign} (Book 2 starships) and {@link HighGuardShipDesign} (Book 5 capital ships),
 * this class represents naval vessels — ships designed for surface and subsurface combat on planets
 * with oceans, as well as space-capable naval craft. These vessels bridge the gap between Third Reich
 * naval warfare and Traveller spacecraft.
 * </p>
 * <p>
 * Key attributes:
 * </p>
 * <ul>
 *   <li><b>Tech Level</b> — the TL required to build this vessel (TL7-9 for WWII-era equivalents)</li>
 *   <li><b>Armor Class</b> — protection rating from 0 (none) to 9 (heavy armor)</li>
 *   <li><b>Hull Points</b> — structural durability, derived from tonnage and armor</li>
 *   <li><b>Movement</b> — speed in hexes or kph depending on environment</li>
 *   <li><b>Crew</b> — personnel required to operate the vessel</li>
 *   <li><b>Cost</b> — construction cost in credits</li>
 *   <li><b>Weapons</b> — mounted weapon systems ({@link NavalWeaponMount})</li>
 * </ul>
 * <p>
 * Vessels can be:
 * </p>
 * <ul>
 *   <li><b>Surface-only</b> — traditional naval ships (battleships, cruisers, destroyers)</li>
 *   <li><b>Submersible</b> — submarines and submersibles</li>
 *   <li><b>Amphibious</b> — can operate in both water and limited space</li>
 *   <li><b>Space-capable</b> — pressurized hulls for vacuum operations</li>
 * </ul>
 */
public class TravellerNavalVessel {

    /**
     * Types of naval vessels.
     */
    public enum VesselType {
        /** Small fast attack craft (100-500 tons) */
        PATROL_BOAT("Patrol Boat", 100, 500, 20, 5, 50000),
        /** Corvette-class light warship (500-2000 tons) */
        CORVETTE("Corvette", 500, 2000, 30, 15, 200000),
        /** Destroyer-class escort (2000-5000 tons) */
        DESTROYER("Destroyer", 2000, 5000, 40, 30, 500000),
        /** Cruiser-class heavy warship (5000-20000 tons) */
        CRUISER("Cruiser", 5000, 20000, 50, 80, 1500000),
        /** Battleship-class capital ship (20000-100000 tons) */
        BATTLESHIP("Battleship", 20000, 100000, 60, 200, 5000000),
        /** Aircraft carrier (20000-100000 tons) */
        CARRIER("Carrier", 20000, 100000, 50, 150, 8000000),
        /** Submarine (500-5000 tons) */
        SUBMARINE("Submarine", 500, 5000, 25, 25, 1000000),
        /** Amphibious assault ship (10000-50000 tons) */
        AMPHIBIOUS("Amphibious Assault", 10000, 50000, 45, 100, 3000000),
        /** Support and repair tender (5000-30000 tons) */
        TENDER("Tender", 5000, 30000, 35, 60, 1000000),
        /** Dreadnought-class super-capital (100000+ tons) */
        DREADNOUGHT("Dreadnought", 100000, 500000, 70, 500, 20000000),
        /** Mothership-class ultimate capital (500000+ tons) */
        MOTHERSHIP("Mothership", 500000, 2000000, 80, 1000, 50000000);

        private final String displayName;
        private final int minTonnage;
        private final int maxTonnage;
        private final int baseMovement;
        private final int baseCrew;
        private final int baseCost;

        VesselType(String displayName, int minTonnage, int maxTonnage, int baseMovement, int baseCrew, int baseCost) {
            this.displayName = displayName;
            this.minTonnage = minTonnage;
            this.maxTonnage = maxTonnage;
            this.baseMovement = baseMovement;
            this.baseCrew = baseCrew;
            this.baseCost = baseCost;
        }

        public String getDisplayName() { return displayName; }
        public int getMinTonnage() { return minTonnage; }
        public int getMaxTonnage() { return maxTonnage; }
        public int getBaseMovement() { return baseMovement; }
        public int getBaseCrew() { return baseCrew; }
        public int getBaseCost() { return baseCost; }
    }

    private final String id;
    private String name;
    private VesselType type;
    private int techLevel;
    private int totalTonnage;
    private int armorClass;
    private int hullPoints;
    private int movement;
    private int crew;
    private long cost;
    private final List<NavalWeaponMount> weapons;
    private boolean amphibious;
    private boolean pressurized;
    private boolean submersible;

    /**
     * Creates a new Traveller naval vessel.
     *
     * @param id the unique vessel identifier
     * @param name the vessel's name
     * @param type the vessel type
     */
    public TravellerNavalVessel(String id, String name, VesselType type) {
        this.id = Objects.requireNonNull(id, "ID must not be null");
        this.name = Objects.requireNonNull(name, "Name must not be null");
        this.type = Objects.requireNonNull(type, "Type must not be null");
        this.techLevel = 7;
        this.totalTonnage = type.getMinTonnage();
        this.armorClass = 0;
        this.movement = type.getBaseMovement();
        this.crew = type.getBaseCrew();
        this.weapons = new ArrayList<>();
        this.amphibious = false;
        this.pressurized = false;
        this.submersible = (type == VesselType.SUBMARINE);
        recalculateDerivedStats();
    }

    // ---- Accessors & Mutators ----

    /** Returns the vessel ID. */
    public String getId() { return id; }

    /** Returns the vessel's name. */
    public String getName() { return name; }

    /** Sets the vessel's name. */
    public void setName(String name) {
        this.name = Objects.requireNonNull(name, "Name must not be null");
    }

    /** Returns the vessel type. */
    public VesselType getType() { return type; }

    /** Sets the vessel type and recalculates stats. */
    public void setType(VesselType type) {
        this.type = Objects.requireNonNull(type, "Type must not be null");
        this.totalTonnage = type.getMinTonnage();
        this.movement = type.getBaseMovement();
        this.crew = type.getBaseCrew();
        this.submersible = (type == VesselType.SUBMARINE);
        recalculateDerivedStats();
    }

    /** Returns the tech level required to build this vessel. */
    public int getTechLevel() { return techLevel; }

    /** Sets the tech level (7-15). */
    public void setTechLevel(int techLevel) {
        this.techLevel = Math.max(7, Math.min(15, techLevel));
        recalculateDerivedStats();
    }

    /** Returns the total tonnage. */
    public int getTotalTonnage() { return totalTonnage; }

    /** Sets the total tonnage within type bounds. */
    public void setTotalTonnage(int totalTonnage) {
        if (totalTonnage < type.getMinTonnage() || totalTonnage > type.getMaxTonnage()) {
            throw new IllegalArgumentException(
                "Tonnage " + totalTonnage + " out of range for " + type.getDisplayName() +
                " (" + type.getMinTonnage() + "-" + type.getMaxTonnage() + " tons)");
        }
        this.totalTonnage = totalTonnage;
        recalculateDerivedStats();
    }

    /** Returns the armor class (0-9). */
    public int getArmorClass() { return armorClass; }

    /** Sets the armor class (0-9). */
    public void setArmorClass(int armorClass) {
        this.armorClass = Math.max(0, Math.min(9, armorClass));
        recalculateDerivedStats();
    }

    /** Returns the hull points. */
    public int getHullPoints() { return hullPoints; }

    /** Returns the movement rating. */
    public int getMovement() { return movement; }

    /** Sets the movement rating. */
    public void setMovement(int movement) {
        this.movement = Math.max(0, movement);
    }

    /** Returns the crew count. */
    public int getCrew() { return crew; }

    /** Sets the crew count. */
    public void setCrew(int crew) {
        this.crew = Math.max(1, crew);
    }

    /** Returns the construction cost in credits. */
    public long getCost() { return cost; }

    /** Returns whether the vessel can support amphibious operations. */
    public boolean isAmphibious() { return amphibious; }

    /** Sets whether the vessel is amphibious. */
    public void setAmphibious(boolean amphibious) {
        this.amphibious = amphibious;
    }

    /** Returns whether the vessel has a pressurized hull for space operations. */
    public boolean isPressurized() { return pressurized; }

    /** Sets whether the vessel is pressurized. */
    public void setPressurized(boolean pressurized) {
        this.pressurized = pressurized;
    }

    /** Returns whether the vessel is submersible. */
    public boolean isSubmersible() { return submersible; }

    /** Sets whether the vessel is submersible. */
    public void setSubmersible(boolean submersible) {
        this.submersible = submersible;
    }

    // ---- Weapons ----

    /** Returns an unmodifiable list of weapon mounts. */
    public List<NavalWeaponMount> getWeapons() {
        return Collections.unmodifiableList(weapons);
    }

    /**
     * Adds a weapon mount to this vessel.
     *
     * @param mount the weapon mount to add
     */
    public void addWeapon(NavalWeaponMount mount) {
        Objects.requireNonNull(mount, "Weapon mount must not be null");
        weapons.add(mount);
        recalculateDerivedStats();
    }

    /**
     * Removes a weapon mount from this vessel.
     *
     * @param mount the weapon mount to remove
     */
    public void removeWeapon(NavalWeaponMount mount) {
        weapons.remove(mount);
        recalculateDerivedStats();
    }

    // ---- Derived Statistics ----

    /**
     * Recalculates all derived statistics based on current configuration.
     * <p>
     * Hull Points = (tonnage / 100) × (1 + armorClass / 2)
     * Crew = base crew + weapon crew
     * Cost = base cost × tonnage multiplier × armor multiplier
     * </p>
     */
    private void recalculateDerivedStats() {
        // Hull points: tonnage-based with armor bonus
        this.hullPoints = (totalTonnage / 100) * (1 + armorClass / 2);
        if (hullPoints < 10) {
            this.hullPoints = 10;
        }

        // Crew: base + weapon crew
        int weaponCrew = weapons.size() * 2;
        this.crew = type.getBaseCrew() + weaponCrew;

        // Cost calculation
        long tonnageMultiplier = Math.max(1, totalTonnage / type.getMinTonnage());
        long armorMultiplier = 1 + (armorClass * 10L);
        long weaponCost = weapons.stream().mapToLong(NavalWeaponMount::getCost).sum();
        this.cost = (long) type.getBaseCost() * tonnageMultiplier * armorMultiplier + weaponCost;
    }

    /**
     * Returns the total attack value for CRT combat resolution.
     * <p>
     * Calculated as the sum of all weapon damage values.
     * </p>
     *
     * @return total attack combat factor
     */
    public int getAttackValue() {
        return weapons.stream().mapToInt(NavalWeaponMount::getDamageValue).sum();
    }

    /**
     * Returns the defense value for CRT combat resolution.
     * <p>
     * Based on armor class and hull points.
     * </p>
     *
     * @return defense combat factor
     */
    public int getDefenseValue() {
        return armorClass * 10 + (hullPoints / 10);
    }

    /**
     * Returns the total damage output of all weapons.
     *
     * @return total damage in megatons
     */
    public int getTotalDamage() {
        return weapons.stream().mapToInt(NavalWeaponMount::getDamageValue).sum();
    }

    /**
     * Returns the number of operational weapons.
     *
     * @return count of operational weapons
     */
    public int getOperationalWeaponCount() {
        return (int) weapons.stream().filter(NavalWeaponMount::isOperational).count();
    }

    // ---- Utility Methods ----

    /**
     * Returns a summary string of the vessel.
     *
     * @return a formatted summary
     */
    public String getSummary() {
        return String.format("%s (%s, %d tons) — TL%d AC%d HP%d Atk%d Def%d",
                name, type.getDisplayName(), totalTonnage, techLevel,
                armorClass, hullPoints, getAttackValue(), getDefenseValue());
    }

    /**
     * Checks if this vessel can be built on a world with the given tech level.
     *
     * @param worldTl the world's tech level
     * @return true if the vessel can be built
     */
    public boolean canBuildOnWorld(int worldTl) {
        return worldTl >= techLevel;
    }

    /**
     * Creates a copy of this vessel with a new name.
     *
     * @param newName the new vessel name
     * @return a copy of this vessel
     */
    public TravellerNavalVessel copyWithNewName(String newName) {
        TravellerNavalVessel copy = new TravellerNavalVessel(id, newName, type);
        copy.setTechLevel(techLevel);
        copy.setTotalTonnage(totalTonnage);
        copy.setArmorClass(armorClass);
        copy.setMovement(movement);
        copy.setAmphibious(amphibious);
        copy.setPressurized(pressurized);
        copy.setSubmersible(submersible);
        for (NavalWeaponMount mount : weapons) {
            copy.addWeapon(mount.copy());
        }
        return copy;
    }

    @Override
    public String toString() {
        return getSummary();
    }
}
