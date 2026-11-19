package com.yisy.modules.doramcu.shipdesign;

import java.util.ArrayList;
import java.util.List;

/**
 * Validates ship designs using a design point system.
 * <p>
 * In FullThrust, ship design is balanced using design points: each component
 * (speed, armor, weapons, etc.) costs points, and the total must not exceed
 * the budget allowed by the ship's size. This class calculates and validates
 * design points for custom ship designs.
 * </p>
 */
public class DesignPoints {

    /**
     * Design point budget by ship size.
     */
    public static final int[] POINTS_BY_SIZE = {
            15,  // TINY
            30,  // SMALL
            50,  // MEDIUM
            80,  // LARGE
            120, // HUGE
            160  // GIANT
    };

    private final Ship.Size size;
    private final int maxPoints;
    private int speedPoints;
    private int armorPoints;
    private int hitPointsPoints;
    private int sensorPoints;
    private int crewPoints;
    private int fuelPoints;
    private int weaponPoints;
    private final List<String> warnings = new ArrayList<>();

    /**
     * Creates a new DesignPoints validator for the given ship size.
     *
     * @param size the ship's size category
     */
    public DesignPoints(Ship.Size size) {
        this.size = size;
        this.maxPoints = POINTS_BY_SIZE[size.ordinal()];
    }

    /**
     * Returns the ship size being validated.
     */
    public Ship.Size getSize() {
        return size;
    }

    /**
     * Returns the maximum design points allowed for this size.
     */
    public int getMaxPoints() {
        return maxPoints;
    }

    /**
     * Calculates design points for speed.
     * Speed costs 1 point per level above 3.
     *
     * @param speed the ship's speed
     * @return the points consumed
     */
    public int calculateSpeedPoints(int speed) {
        this.speedPoints = Math.max(0, speed - 3);
        return this.speedPoints;
    }

    /**
     * Calculates design points for armor.
     * Armor costs 2 points per level.
     *
     * @param armor the ship's armor value
     * @return the points consumed
     */
    public int calculateArmorPoints(int armor) {
        this.armorPoints = armor * 2;
        return this.armorPoints;
    }

    /**
     * Calculates design points for hit points.
     * Hit points cost 1 point per 2 HP.
     *
     * @param hitPoints the ship's hit points
     * @return the points consumed
     */
    public int calculateHitPointsPoints(int hitPoints) {
        this.hitPointsPoints = hitPoints / 2;
        return this.hitPointsPoints;
    }

    /**
     * Calculates design points for sensor range.
     * Sensors cost 1 point per range level above 1.
     *
     * @param sensorRange the ship's sensor range
     * @return the points consumed
     */
    public int calculateSensorPoints(int sensorRange) {
        this.sensorPoints = Math.max(0, sensorRange - 1);
        return this.sensorPoints;
    }

    /**
     * Calculates design points for crew capacity.
     * Crew costs 1 point per 5 crew members.
     *
     * @param crewCapacity the ship's crew capacity
     * @return the points consumed
     */
    public int calculateCrewPoints(int crewCapacity) {
        this.crewPoints = crewCapacity / 5;
        return this.crewPoints;
    }

    /**
     * Calculates design points for fuel capacity.
     * Fuel costs 1 point per 3 fuel units.
     *
     * @param maxFuel the ship's maximum fuel
     * @return the points consumed
     */
    public int calculateFuelPoints(int maxFuel) {
        this.fuelPoints = maxFuel / 3;
        return this.fuelPoints;
    }

    /**
     * Calculates design points for a weapon.
     * Weapons cost points based on type and strength.
     *
     * @param weapon the weapon to calculate points for
     * @return the points consumed by this weapon
     */
    public int calculateWeaponPoints(Ship.Weapon weapon) {
        int points;
        switch (weapon.getType()) {
            case BEAM:
                points = weapon.getStrength() * 2;
                break;
            case MISSILE:
                points = weapon.getStrength() * 3;
                break;
            case TORPEDO:
                points = weapon.getStrength() * 4;
                break;
            default:
                points = 0;
        }
        this.weaponPoints += points;
        return points;
    }

    /**
     * Calculates design points for multiple weapons.
     *
     * @param weapons the list of weapons
     * @return the total points consumed by all weapons
     */
    public int calculateWeaponPoints(List<Ship.Weapon> weapons) {
        int total = 0;
        for (Ship.Weapon weapon : weapons) {
            total += calculateWeaponPoints(weapon);
        }
        return total;
    }

    /**
     * Returns the total design points used so far.
     */
    public int getTotalPoints() {
        return speedPoints + armorPoints + hitPointsPoints + sensorPoints +
                crewPoints + fuelPoints + weaponPoints;
    }

    /**
     * Returns the remaining design points available.
     */
    public int getRemainingPoints() {
        return maxPoints - getTotalPoints();
    }

    /**
     * Returns the points used by speed.
     */
    public int getSpeedPoints() {
        return speedPoints;
    }

    /**
     * Returns the points used by armor.
     */
    public int getArmorPoints() {
        return armorPoints;
    }

    /**
     * Returns the points used by hit points.
     */
    public int getHitPointsPoints() {
        return hitPointsPoints;
    }

    /**
     * Returns the points used by sensors.
     */
    public int getSensorPoints() {
        return sensorPoints;
    }

    /**
     * Returns the points used by crew.
     */
    public int getCrewPoints() {
        return crewPoints;
    }

    /**
     * Returns the points used by fuel.
     */
    public int getFuelPoints() {
        return fuelPoints;
    }

    /**
     * Returns the points used by weapons.
     */
    public int getWeaponPoints() {
        return weaponPoints;
    }

    /**
     * Validates the current design point allocation.
     * Adds warnings if the design exceeds the budget.
     *
     * @return true if the design is within budget, false if over budget
     */
    public boolean validate() {
        warnings.clear();
        int total = getTotalPoints();

        if (total > maxPoints) {
            warnings.add("Design exceeds budget by " + (total - maxPoints) + " points");
            return false;
        }

        if (total > maxPoints * 0.9) {
            warnings.add("Design is near budget limit (" + total + "/" + maxPoints + " points)");
        }

        if (weaponPoints == 0) {
            warnings.add("Ship has no weapons");
        }

        if (speedPoints == 0) {
            warnings.add("Ship has minimal speed");
        }

        return true;
    }

    /**
     * Returns any warnings generated during validation.
     */
    public List<String> getWarnings() {
        return new ArrayList<>(warnings);
    }

    /**
     * Resets all calculated points to zero.
     *
     * @return this validator for chaining
     */
    public DesignPoints reset() {
        this.speedPoints = 0;
        this.armorPoints = 0;
        this.hitPointsPoints = 0;
        this.sensorPoints = 0;
        this.crewPoints = 0;
        this.fuelPoints = 0;
        this.weaponPoints = 0;
        this.warnings.clear();
        return this;
    }

    /**
     * Calculates design points for a complete ship.
     *
     * @param ship the ship to evaluate
     * @return a new DesignPoints instance with all points calculated
     */
    public static DesignPoints evaluate(Ship ship) {
        DesignPoints dp = new DesignPoints(ship.getSize());
        dp.calculateSpeedPoints(ship.getSpeed());
        dp.calculateArmorPoints(ship.getArmor());
        dp.calculateHitPointsPoints(ship.getMaxHitPoints());
        dp.calculateSensorPoints(ship.getSensorRange());
        dp.calculateCrewPoints(ship.getCrewCapacity());
        dp.calculateFuelPoints(ship.getMaxFuel());
        dp.calculateWeaponPoints(ship.getWeapons());
        return dp;
    }

    /**
     * Calculates design points from a ShipBuilder's configuration.
     *
     * @param builder the builder to evaluate
     * @return a new DesignPoints instance with all points calculated
     * @throws IllegalStateException if the builder's size is not set
     */
    public static DesignPoints evaluate(ShipBuilder builder) {
        Ship.Size size = builder.getClass().getDeclaredClasses().length > 0 ?
                Ship.Size.SMALL : Ship.Size.SMALL;

        // Build a temporary ship to extract values
        try {
            Ship tempShip = builder.build();
            DesignPoints dp = evaluate(tempShip);
            return dp;
        } catch (IllegalStateException e) {
            throw new IllegalStateException("Cannot evaluate design points: builder size is not set");
        }
    }

    @Override
    public String toString() {
        return "DesignPoints{" +
                "size=" + size +
                ", total=" + getTotalPoints() + "/" + maxPoints +
                ", speed=" + speedPoints +
                ", armor=" + armorPoints +
                ", hp=" + hitPointsPoints +
                ", sensors=" + sensorPoints +
                ", crew=" + crewPoints +
                ", fuel=" + fuelPoints +
                ", weapons=" + weaponPoints +
                ", remaining=" + getRemainingPoints() +
                '}';
    }
}
