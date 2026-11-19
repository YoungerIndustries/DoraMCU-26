package com.yisy.modules.doramcu.shipdesign;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents a spaceship in the FullThrust space combat game system.
 * <p>
 * A ship is defined by its physical characteristics (size, speed, armor, hit points),
 * weapon systems (beam, missile, torpedo), sensor range, and crew capacity.
 * </p>
 */
public class Ship {

    /**
     * The size category of a ship, determining its physical footprint in hexes.
     */
    public enum Size {
        TINY(1), SMALL(2), MEDIUM(3), LARGE(4), HUGE(5), GIANT(6);

        private final int hexes;

        Size(int hexes) {
            this.hexes = hexes;
        }

        /**
         * Returns the number of hexes this ship occupies.
         */
        public int getHexes() {
            return hexes;
        }
    }

    /**
     * The type of weapon system.
     */
    public enum WeaponType {
        BEAM, MISSILE, TORPEDO
    }

    /**
     * Represents a single weapon mount on a ship.
     */
    public static class Weapon {
        private final WeaponType type;
        private final String name;
        private final int strength;
        private final int range;
        private final int arcs; // number of arcs covered (1=front, 2=front+side, etc.)

        /**
         * Creates a new weapon.
         *
         * @param type     the weapon type (beam, missile, torpedo)
         * @param name     the weapon's name
         * @param strength the weapon strength (damage value)
         * @param range    the maximum range in hexes
         * @param arcs     the number of arcs the weapon can fire into
         */
        public Weapon(WeaponType type, String name, int strength, int range, int arcs) {
            this.type = type;
            this.name = name;
            this.strength = strength;
            this.range = range;
            this.arcs = arcs;
        }

        /**
         * Returns the weapon type.
         */
        public WeaponType getType() {
            return type;
        }

        /**
         * Returns the weapon's name.
         */
        public String getName() {
            return name;
        }

        /**
         * Returns the weapon strength (damage value).
         */
        public int getStrength() {
            return strength;
        }

        /**
         * Returns the maximum range in hexes.
         */
        public int getRange() {
            return range;
        }

        /**
         * Returns the number of arcs the weapon can fire into.
         */
        public int getArcs() {
            return arcs;
        }

        @Override
        public String toString() {
            return "Weapon{" + name + " type=" + type + " str=" + strength + " range=" + range + " arcs=" + arcs + "}";
        }
    }

    // Identity
    private String name;
    private final Size size;

    // Physical characteristics
    private final int speed;          // maximum speed in hexes per phase
    private final int armor;          // armor value (reduces incoming damage)
    private int hitPoints;            // current hit points
    private final int maxHitPoints;   // maximum hit points

    // Systems
    private final int sensorRange;    // sensor range in hexes
    private final int crewCapacity;   // maximum crew
    private int currentCrew;          // current crew on board

    // Weapons
    private final List<Weapon> weapons;

    // Fuel
    private int fuel;                 // current fuel
    private final int maxFuel;        // maximum fuel capacity

    /**
     * Creates a new ship with the given characteristics.
     *
     * @param name         the ship's name
     * @param size         the ship's size category
     * @param speed        maximum speed in hexes per phase
     * @param armor        armor value
     * @param hitPoints    initial (and max) hit points
     * @param sensorRange  sensor range in hexes
     * @param crewCapacity maximum crew capacity
     * @param maxFuel      maximum fuel capacity
     */
    public Ship(String name, Size size, int speed, int armor, int hitPoints,
                int sensorRange, int crewCapacity, int maxFuel) {
        this.name = name;
        this.size = size;
        this.speed = speed;
        this.armor = armor;
        this.hitPoints = hitPoints;
        this.maxHitPoints = hitPoints;
        this.sensorRange = sensorRange;
        this.crewCapacity = crewCapacity;
        this.currentCrew = crewCapacity;
        this.fuel = maxFuel;
        this.maxFuel = maxFuel;
        this.weapons = new ArrayList<>();
    }

    // --- Identity Getters ---

    /**
     * Returns the ship's name.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the ship's name.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the ship's size category.
     */
    public Size getSize() {
        return size;
    }

    /**
     * Returns the number of hexes this ship occupies.
     */
    public int getHexSize() {
        return size.getHexes();
    }

    // --- Physical Characteristics ---

    /**
     * Returns the maximum speed in hexes per phase.
     */
    public int getSpeed() {
        return speed;
    }

    /**
     * Returns the armor value.
     */
    public int getArmor() {
        return armor;
    }

    /**
     * Returns the current hit points.
     */
    public int getHitPoints() {
        return hitPoints;
    }

    /**
     * Returns the maximum hit points.
     */
    public int getMaxHitPoints() {
        return maxHitPoints;
    }

    /**
     * Returns true if the ship is destroyed (hit points <= 0).
     */
    public boolean isDestroyed() {
        return hitPoints <= 0;
    }

    /**
     * Returns true if the ship is damaged (hit points < max hit points).
     */
    public boolean isDamaged() {
        return hitPoints < maxHitPoints;
    }

    // --- Systems ---

    /**
     * Returns the sensor range in hexes.
     */
    public int getSensorRange() {
        return sensorRange;
    }

    /**
     * Returns the maximum crew capacity.
     */
    public int getCrewCapacity() {
        return crewCapacity;
    }

    /**
     * Returns the current crew on board.
     */
    public int getCurrentCrew() {
        return currentCrew;
    }

    /**
     * Adds crew to the ship, up to capacity.
     *
     * @param count number of crew to add
     * @return the actual number of crew added
     */
    public int addCrew(int count) {
        int available = crewCapacity - currentCrew;
        int added = Math.min(count, available);
        currentCrew += added;
        return added;
    }

    /**
     * Removes crew from the ship.
     *
     * @param count number of crew to remove
     * @return the actual number of crew removed
     */
    public int removeCrew(int count) {
        int removed = Math.min(count, currentCrew);
        currentCrew -= removed;
        return removed;
    }

    // --- Weapons ---

    /**
     * Adds a weapon to the ship.
     *
     * @param weapon the weapon to add
     */
    public void addWeapon(Weapon weapon) {
        weapons.add(weapon);
    }

    /**
     * Returns all weapons on the ship.
     */
    public List<Weapon> getWeapons() {
        return new ArrayList<>(weapons);
    }

    /**
     * Returns weapons of a specific type.
     *
     * @param type the weapon type to filter by
     * @return a list of weapons matching the given type
     */
    public List<Weapon> getWeaponsByType(WeaponType type) {
        List<Weapon> result = new ArrayList<>();
        for (Weapon w : weapons) {
            if (w.getType() == type) {
                result.add(w);
            }
        }
        return result;
    }

    /**
     * Returns the total beam weapon strength.
     */
    public int getTotalBeamStrength() {
        int total = 0;
        for (Weapon w : weapons) {
            if (w.getType() == WeaponType.BEAM) {
                total += w.getStrength();
            }
        }
        return total;
    }

    // --- Fuel ---

    /**
     * Returns the current fuel level.
     */
    public int getFuel() {
        return fuel;
    }

    /**
     * Returns the maximum fuel capacity.
     */
    public int getMaxFuel() {
        return maxFuel;
    }

    /**
     * Returns true if the ship is out of fuel.
     */
    public boolean isOutOfFuel() {
        return fuel <= 0;
    }

    /**
     * Consumes fuel for movement.
     *
     * @param amount fuel to consume
     * @return the actual amount consumed
     */
    public int consumeFuel(int amount) {
        int consumed = Math.min(amount, fuel);
        fuel -= consumed;
        return consumed;
    }

    /**
     * Refuels the ship.
     *
     * @param amount fuel to add
     * @return the actual amount added
     */
    public int refuel(int amount) {
        int available = maxFuel - fuel;
        int added = Math.min(amount, available);
        fuel += added;
        return added;
    }

    // --- Damage ---

    /**
     * Takes damage, reduced by armor.
     *
     * @param damage the raw damage value
     * @return the actual damage taken (after armor reduction)
     */
    public int takeDamage(int damage) {
        int actualDamage = Math.max(0, damage - armor);
        hitPoints -= actualDamage;
        if (hitPoints < 0) {
            hitPoints = 0;
        }
        return actualDamage;
    }

    /**
     * Repairs the ship by restoring hit points.
     *
     * @param amount hit points to restore
     * @return the actual amount repaired
     */
    public int repair(int amount) {
        int available = maxHitPoints - hitPoints;
        int repaired = Math.min(amount, available);
        hitPoints += repaired;
        return repaired;
    }

    // --- Object methods ---

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Ship ship = (Ship) o;
        return Objects.equals(name, ship.name) && size == ship.size;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, size);
    }

    @Override
    public String toString() {
        return "Ship{" +
                "name='" + name + '\'' +
                ", size=" + size +
                ", speed=" + speed +
                ", armor=" + armor +
                ", hp=" + hitPoints + "/" + maxHitPoints +
                ", sensors=" + sensorRange +
                ", crew=" + currentCrew + "/" + crewCapacity +
                ", fuel=" + fuel + "/" + maxFuel +
                ", weapons=" + weapons.size() +
                '}';
    }
}
