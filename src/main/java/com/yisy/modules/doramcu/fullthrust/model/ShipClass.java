package com.yisy.modules.doramcu.fullthrust.model;

import java.util.*;

/**
 * Defines a ship class template with all its characteristics.
 * Based on FullThrust ship statistics.
 */
public class ShipClass {
    private String id;
    private String name;
    private int hullPoints;
    private int armorValue;
    private int maxSpeed;
    private int acceleration;
    private int deceleration;
    private int maxHeadingChange;
    private int shieldPoints;
    private int shieldRegeneration;
    private int fuelCapacity;
    private List<WeaponSlot> weaponSlots;
    private List<String> availableWeapons;
    private int crew;
    private int cost;
    private String description;

    public ShipClass() {
        this.weaponSlots = new ArrayList<>();
        this.availableWeapons = new ArrayList<>();
    }

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getHullPoints() { return hullPoints; }
    public void setHullPoints(int hullPoints) { this.hullPoints = hullPoints; }
    public int getArmorValue() { return armorValue; }
    public void setArmorValue(int armorValue) { this.armorValue = armorValue; }
    public int getMaxSpeed() { return maxSpeed; }
    public void setMaxSpeed(int maxSpeed) { this.maxSpeed = maxSpeed; }
    public int getAcceleration() { return acceleration; }
    public void setAcceleration(int acceleration) { this.acceleration = acceleration; }
    public int getDeceleration() { return deceleration; }
    public void setDeceleration(int deceleration) { this.deceleration = deceleration; }
    public int getMaxHeadingChange() { return maxHeadingChange; }
    public void setMaxHeadingChange(int maxHeadingChange) { this.maxHeadingChange = maxHeadingChange; }
    public int getShieldPoints() { return shieldPoints; }
    public void setShieldPoints(int shieldPoints) { this.shieldPoints = shieldPoints; }
    public int getShieldRegeneration() { return shieldRegeneration; }
    public void setShieldRegeneration(int shieldRegeneration) { this.shieldRegeneration = shieldRegeneration; }
    public int getFuelCapacity() { return fuelCapacity; }
    public void setFuelCapacity(int fuelCapacity) { this.fuelCapacity = fuelCapacity; }
    public List<WeaponSlot> getWeaponSlots() { return weaponSlots; }
    public void setWeaponSlots(List<WeaponSlot> weaponSlots) { this.weaponSlots = weaponSlots; }
    public List<String> getAvailableWeapons() { return availableWeapons; }
    public void setAvailableWeapons(List<String> availableWeapons) { this.availableWeapons = availableWeapons; }
    public int getCrew() { return crew; }
    public void setCrew(int crew) { this.crew = crew; }
    public int getCost() { return cost; }
    public void setCost(int cost) { this.cost = cost; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    /**
     * Add a weapon slot to this ship class
     */
    public void addWeaponSlot(WeaponSlot slot) {
        this.weaponSlots.add(slot);
    }

    /**
     * Represents a weapon mounting position on a ship
     */
    public static class WeaponSlot {
        private String name;
        private int arc; // 0-5, representing the firing arc (direction)
        private String weaponType; // "laser", "missile", "torpedo"
        private int damage;
        private int range; // Maximum range in hexes

        public WeaponSlot() {}
        public WeaponSlot(String name, int arc, String weaponType) {
            this.name = name;
            this.arc = arc;
            this.weaponType = weaponType;
            // Set default stats based on type
            switch (weaponType.toLowerCase()) {
                case "laser":
                    this.damage = 1;
                    this.range = 6;
                    break;
                case "missile":
                    this.damage = 2;
                    this.range = 12;
                    break;
                case "torpedo":
                    this.damage = 3;
                    this.range = 8;
                    break;
                default:
                    this.damage = 1;
                    this.range = 4;
            }
        }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public int getArc() { return arc; }
        public void setArc(int arc) { this.arc = arc; }
        public String getWeaponType() { return weaponType; }
        public void setWeaponType(String weaponType) { this.weaponType = weaponType; }
        public int getDamage() { return damage; }
        public void setDamage(int damage) { this.damage = damage; }
        public int getRange() { return range; }
        public void setRange(int range) { this.range = range; }
    }
}
