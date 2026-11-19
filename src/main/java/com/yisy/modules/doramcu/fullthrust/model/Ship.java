package com.yisy.modules.doramcu.fullthrust.model;

import java.util.*;

/**
 * Represents an individual ship instance in the game.
 * Each ship has a position, heading, velocity, and current status.
 */
public class Ship {
    private String id;
    private String name;
    private String shipClassId; // Reference to ShipClass
    private String playerId; // Owner
    private HexPosition position;
    private int heading; // 0-5, current facing direction
    private int speed; // Current speed
    private int currentHull;
    private int maxHull;
    private int currentShield;
    private int maxShield;
    private int currentFuel;
    private int maxFuel;
    private int armorValue;
    private boolean destroyed;
    private List<Weapon> weapons;
    private List<Missile> launchedMissiles;
    private Set<String> damagedSystems;

    public Ship() {
        this.position = new HexPosition();
        this.heading = 0;
        this.speed = 0;
        this.weapons = new ArrayList<>();
        this.launchedMissiles = new ArrayList<>();
        this.damagedSystems = new HashSet<>();
        this.destroyed = false;
    }

    /**
     * Create a ship from a ShipClass template
     */
    public Ship(String id, String name, String playerId, ShipClass shipClass) {
        this.id = id;
        this.name = name;
        this.playerId = playerId;
        this.shipClassId = shipClass.getId();
        this.position = new HexPosition();
        this.heading = 0;
        this.speed = 0;
        this.maxHull = shipClass.getHullPoints();
        this.currentHull = shipClass.getHullPoints();
        this.maxShield = shipClass.getShieldPoints();
        this.currentShield = shipClass.getShieldPoints();
        this.maxFuel = shipClass.getFuelCapacity();
        this.currentFuel = shipClass.getFuelCapacity();
        this.armorValue = shipClass.getArmorValue();
        this.weapons = new ArrayList<>();
        this.launchedMissiles = new ArrayList<>();
        this.damagedSystems = new HashSet<>();
        this.destroyed = false;

        // Initialize weapons from ship class
        for (ShipClass.WeaponSlot slot : shipClass.getWeaponSlots()) {
            Weapon weapon = new Weapon(slot.getName(), slot.getArc(), slot.getWeaponType());
            this.weapons.add(weapon);
        }
    }

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getShipClassId() { return shipClassId; }
    public void setShipClassId(String shipClassId) { this.shipClassId = shipClassId; }
    public String getPlayerId() { return playerId; }
    public void setPlayerId(String playerId) { this.playerId = playerId; }
    public HexPosition getPosition() { return position; }
    public void setPosition(HexPosition position) { this.position = position; }
    public int getHeading() { return heading; }
    public void setHeading(int heading) { this.heading = heading; }
    public int getSpeed() { return speed; }
    public void setSpeed(int speed) { this.speed = speed; }
    public int getCurrentHull() { return currentHull; }
    public void setCurrentHull(int currentHull) { this.currentHull = currentHull; }
    public int getMaxHull() { return maxHull; }
    public void setMaxHull(int maxHull) { this.maxHull = maxHull; }
    public int getCurrentShield() { return currentShield; }
    public void setCurrentShield(int currentShield) { this.currentShield = currentShield; }
    public int getMaxShield() { return maxShield; }
    public void setMaxShield(int maxShield) { this.maxShield = maxShield; }
    public int getCurrentFuel() { return currentFuel; }
    public void setCurrentFuel(int currentFuel) { this.currentFuel = currentFuel; }
    public int getMaxFuel() { return maxFuel; }
    public void setMaxFuel(int maxFuel) { this.maxFuel = maxFuel; }
    public int getArmorValue() { return armorValue; }
    public void setArmorValue(int armorValue) { this.armorValue = armorValue; }
    public boolean isDestroyed() { return destroyed; }
    public void setDestroyed(boolean destroyed) { this.destroyed = destroyed; }
    public List<Weapon> getWeapons() { return weapons; }
    public void setWeapons(List<Weapon> weapons) { this.weapons = weapons; }
    public List<Missile> getLaunchedMissiles() { return launchedMissiles; }
    public void setLaunchedMissiles(List<Missile> launchedMissiles) { this.launchedMissiles = launchedMissiles; }
    public Set<String> getDamagedSystems() { return damagedSystems; }
    public void setDamagedSystems(Set<String> damagedSystems) { this.damagedSystems = damagedSystems; }

    /**
     * Take damage, applying to shields first, then hull
     */
    public int takeDamage(int damage) {
        int remainingDamage = damage;

        // Apply to shields first
        if (currentShield > 0) {
            int shieldDamage = Math.min(currentShield, remainingDamage);
            currentShield -= shieldDamage;
            remainingDamage -= shieldDamage;
        }

        // Remaining damage goes to hull
        if (remainingDamage > 0) {
            currentHull = Math.max(0, currentHull - remainingDamage);
            if (currentHull <= 0) {
                this.destroyed = true;
            }
        }

        return damage - remainingDamage;
    }

    /**
     * Regenerate shields
     */
    public void regenerateShields(int amount) {
        this.currentShield = Math.min(this.maxShield, this.currentShield + amount);
    }

    /**
     * Check if this ship can fire at a target direction
     */
    public boolean canFireInDirection(int direction) {
        for (Weapon weapon : weapons) {
            if (weapon.getArc() == direction && !weapon.isDisabled() && !weapon.isReloading()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get weapons that can fire in a given direction
     */
    public List<Weapon> getWeaponsForDirection(int direction) {
        List<Weapon> result = new ArrayList<>();
        for (Weapon weapon : weapons) {
            if (weapon.getArc() == direction && !weapon.isDisabled() && !weapon.isReloading()) {
                result.add(weapon);
            }
        }
        return result;
    }
}
