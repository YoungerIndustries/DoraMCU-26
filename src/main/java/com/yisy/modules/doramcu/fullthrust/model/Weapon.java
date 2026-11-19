package com.yisy.modules.doramcu.fullthrust.model;

/**
 * Represents a weapon on a ship.
 * Based on FullThrust weapon types: Lasers, Missiles, and Torpedoes.
 */
public class Weapon {
    private String id;
    private String name;
    private int arc; // Firing arc direction (0-5)
    private String type; // "laser", "missile", "torpedo"
    private int damage;
    private int range; // Maximum range in hexes
    private int reloadTime; // Turns to reload (0 for lasers)
    private int currentReload; // Current reload counter
    private boolean disabled;

    public Weapon() {}

    public Weapon(String name, int arc, String type) {
        this.name = name;
        this.arc = arc;
        this.type = type;
        this.disabled = false;
        this.currentReload = 0;

        // Set default stats based on type
        switch (type.toLowerCase()) {
            case "laser":
                this.damage = 1;
                this.range = 6;
                this.reloadTime = 0;
                break;
            case "missile":
                this.damage = 2;
                this.range = 12;
                this.reloadTime = 2;
                break;
            case "torpedo":
                this.damage = 3;
                this.range = 8;
                this.reloadTime = 3;
                break;
            default:
                this.damage = 1;
                this.range = 4;
                this.reloadTime = 1;
        }
    }

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getArc() { return arc; }
    public void setArc(int arc) { this.arc = arc; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public int getDamage() { return damage; }
    public void setDamage(int damage) { this.damage = damage; }
    public int getRange() { return range; }
    public void setRange(int range) { this.range = range; }
    public int getReloadTime() { return reloadTime; }
    public void setReloadTime(int reloadTime) { this.reloadTime = reloadTime; }
    public int getCurrentReload() { return currentReload; }
    public void setCurrentReload(int currentReload) { this.currentReload = currentReload; }
    public boolean isDisabled() { return disabled; }
    public void setDisabled(boolean disabled) { this.disabled = disabled; }

    /**
     * Check if weapon is currently reloading
     */
    public boolean isReloading() {
        return currentReload > 0;
    }

    /**
     * Fire the weapon, starting reload timer
     */
    public boolean fire() {
        if (disabled || currentReload > 0) {
            return false;
        }
        this.currentReload = reloadTime;
        return true;
    }

    /**
     * Advance reload counter by one turn
     */
    public void advanceReload() {
        if (currentReload > 0) {
            currentReload--;
        }
    }
}
