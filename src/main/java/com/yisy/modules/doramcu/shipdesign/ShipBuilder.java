package com.yisy.modules.doramcu.shipdesign;

import java.util.ArrayList;
import java.util.List;

/**
 * Builder for constructing {@link Ship} instances with a fluent API.
 * <p>
 * Provides sensible defaults based on ship size, following FullThrust rules
 * where larger ships have more hit points, crew capacity, and fuel, but may
 * have different speed and armor characteristics.
 * </p>
 * <p>
 * Example usage:
 * <pre>
 * Ship ship = new ShipBuilder()
 *     .name("Star Explorer")
 *     .size(Ship.Size.MEDIUM)
 *     .speed(12)
 *     .addBeam("Main Laser", 3, 6, 1)
 *     .build();
 * </pre>
 */
public class ShipBuilder {

    private String name;
    private Ship.Size size;
    private Integer speed;
    private Integer armor;
    private Integer hitPoints;
    private Integer sensorRange;
    private Integer crewCapacity;
    private Integer maxFuel;
    private final List<Ship.Weapon> weapons = new ArrayList<>();

    /**
     * Sets the ship's name.
     *
     * @param name the ship's name
     * @return this builder for chaining
     */
    public ShipBuilder name(String name) {
        this.name = name;
        return this;
    }

    /**
     * Sets the ship's size category. This is the only required field.
     * Setting the size also applies default values for all other properties.
     *
     * @param size the ship's size
     * @return this builder for chaining
     */
    public ShipBuilder size(Ship.Size size) {
        this.size = size;
        applyDefaultsForSize(size);
        return this;
    }

    /**
     * Sets the ship's maximum speed in hexes per phase.
     *
     * @param speed the maximum speed
     * @return this builder for chaining
     */
    public ShipBuilder speed(int speed) {
        this.speed = speed;
        return this;
    }

    /**
     * Sets the ship's armor value.
     *
     * @param armor the armor value
     * @return this builder for chaining
     */
    public ShipBuilder armor(int armor) {
        this.armor = armor;
        return this;
    }

    /**
     * Sets the ship's hit points.
     *
     * @param hitPoints the hit points
     * @return this builder for chaining
     */
    public ShipBuilder hitPoints(int hitPoints) {
        this.hitPoints = hitPoints;
        return this;
    }

    /**
     * Sets the ship's sensor range in hexes.
     *
     * @param sensorRange the sensor range
     * @return this builder for chaining
     */
    public ShipBuilder sensorRange(int sensorRange) {
        this.sensorRange = sensorRange;
        return this;
    }

    /**
     * Sets the ship's crew capacity.
     *
     * @param crewCapacity the maximum crew
     * @return this builder for chaining
     */
    public ShipBuilder crewCapacity(int crewCapacity) {
        this.crewCapacity = crewCapacity;
        return this;
    }

    /**
     * Sets the ship's maximum fuel capacity.
     *
     * @param maxFuel the maximum fuel
     * @return this builder for chaining
     */
    public ShipBuilder maxFuel(int maxFuel) {
        this.maxFuel = maxFuel;
        return this;
    }

    // --- Weapon convenience methods ---

    /**
     * Adds a beam weapon to the ship.
     *
     * @param name     the weapon's name
     * @param strength the weapon strength
     * @param range    the maximum range in hexes
     * @param arcs     the number of arcs the weapon can fire into
     * @return this builder for chaining
     */
    public ShipBuilder addBeam(String name, int strength, int range, int arcs) {
        weapons.add(new Ship.Weapon(Ship.WeaponType.BEAM, name, strength, range, arcs));
        return this;
    }

    /**
     * Adds a missile weapon to the ship.
     *
     * @param name     the weapon's name
     * @param strength the weapon strength
     * @param range    the maximum range in hexes
     * @param arcs     the number of arcs the weapon can fire into
     * @return this builder for chaining
     */
    public ShipBuilder addMissile(String name, int strength, int range, int arcs) {
        weapons.add(new Ship.Weapon(Ship.WeaponType.MISSILE, name, strength, range, arcs));
        return this;
    }

    /**
     * Adds a torpedo weapon to the ship.
     *
     * @param name     the weapon's name
     * @param strength the weapon strength
     * @param range    the maximum range in hexes
     * @param arcs     the number of arcs the weapon can fire into
     * @return this builder for chaining
     */
    public ShipBuilder addTorpedo(String name, int strength, int range, int arcs) {
        weapons.add(new Ship.Weapon(Ship.WeaponType.TORPEDO, name, strength, range, arcs));
        return this;
    }

    /**
     * Adds a generic weapon to the ship.
     *
     * @param weapon the weapon to add
     * @return this builder for chaining
     */
    public ShipBuilder addWeapon(Ship.Weapon weapon) {
        weapons.add(weapon);
        return this;
    }

    /**
     * Applies default values based on the ship's size category.
     * Defaults follow FullThrust scaling: larger ships get more HP, crew, and fuel.
     *
     * @param size the ship size to apply defaults for
     */
    private void applyDefaultsForSize(Ship.Size size) {
        switch (size) {
            case TINY:
                this.speed = 10;
                this.armor = 0;
                this.hitPoints = 1;
                this.sensorRange = 1;
                this.crewCapacity = 1;
                this.maxFuel = 1;
                break;
            case SMALL:
                this.speed = 8;
                this.armor = 1;
                this.hitPoints = 3;
                this.sensorRange = 2;
                this.crewCapacity = 3;
                this.maxFuel = 3;
                break;
            case MEDIUM:
                this.speed = 6;
                this.armor = 2;
                this.hitPoints = 6;
                this.sensorRange = 3;
                this.crewCapacity = 6;
                this.maxFuel = 6;
                break;
            case LARGE:
                this.speed = 5;
                this.armor = 3;
                this.hitPoints = 10;
                this.sensorRange = 4;
                this.crewCapacity = 10;
                this.maxFuel = 10;
                break;
            case HUGE:
                this.speed = 4;
                this.armor = 4;
                this.hitPoints = 15;
                this.sensorRange = 5;
                this.crewCapacity = 15;
                this.maxFuel = 15;
                break;
            case GIANT:
                this.speed = 3;
                this.armor = 5;
                this.hitPoints = 21;
                this.sensorRange = 6;
                this.crewCapacity = 21;
                this.maxFuel = 21;
                break;
        }
    }

    /**
     * Builds and returns a new Ship instance.
     *
     * @return the constructed Ship
     * @throws IllegalStateException if size is not set
     */
    public Ship build() {
        if (size == null) {
            throw new IllegalStateException("Ship size must be set before building");
        }

        String finalName = (name != null) ? name : "Unnamed " + size;
        int finalSpeed = (speed != null) ? speed : 0;
        int finalArmor = (armor != null) ? armor : 0;
        int finalHitPoints = (hitPoints != null) ? hitPoints : 1;
        int finalSensorRange = (sensorRange != null) ? sensorRange : 1;
        int finalCrewCapacity = (crewCapacity != null) ? crewCapacity : 1;
        int finalMaxFuel = (maxFuel != null) ? maxFuel : 1;

        Ship ship = new Ship(finalName, size, finalSpeed, finalArmor, finalHitPoints,
                finalSensorRange, finalCrewCapacity, finalMaxFuel);

        for (Ship.Weapon weapon : weapons) {
            ship.addWeapon(weapon);
        }

        return ship;
    }

    /**
     * Resets the builder to its initial state, clearing all settings.
     *
     * @return this builder for chaining
     */
    public ShipBuilder reset() {
        this.name = null;
        this.size = null;
        this.speed = null;
        this.armor = null;
        this.hitPoints = null;
        this.sensorRange = null;
        this.crewCapacity = null;
        this.maxFuel = null;
        this.weapons.clear();
        return this;
    }
}
