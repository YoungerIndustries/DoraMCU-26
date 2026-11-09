package com.yisy.modules.doramcu.sfb.model;

import java.util.UUID;

/**
 * Represents a seeking weapon (plasma torpedo or drone) in SFB.
 * <p>
 * Seeking weapons pursue targets after launch. Plasma torpedoes have
 * degrading warheads, while drones have fixed warhead strength.
 * </p>
 */
public class SfbSeekingWeapon {

    public enum SeekingWeaponType {
        PLASMA_TORPEDO("Plasma torpedo with degrading warhead"),
        DRONE("Drone with fixed warhead");

        private final String description;

        SeekingWeaponType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }

        public String getName() {
            return name();
        }
    }

    public enum DroneType {
        DRONE_I("Drone I - basic seeking drone"),
        DRONE_II("Drone II - improved seeking"),
        DRONE_IV("Drone IV - advanced seeking"),
        DRONE_V("Drone V - best seeking");

        private final String description;

        DroneType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    private final String id;
    private final SeekingWeaponType type;
    private final int initialWarhead;
    private int currentWarhead;
    private SfbShip target;
    private SfbShip launchingShip;
    private SfbHexPosition position;
    private int rangeTraveled;
    private boolean active;

    public SfbSeekingWeapon(String id, SeekingWeaponType type, int initialWarhead) {
        this.id = id;
        this.type = type;
        this.initialWarhead = initialWarhead;
        this.currentWarhead = initialWarhead;
        this.rangeTraveled = 0;
        this.active = true;
    }

    /**
     * Constructor for creating a seeking weapon with a target.
     */
    public SfbSeekingWeapon(SeekingWeaponType type, SfbShip launchingShip, SfbShip target, int warhead) {
        this.id = UUID.randomUUID().toString().substring(0, 8);
        this.type = type;
        this.initialWarhead = warhead;
        this.currentWarhead = warhead;
        this.launchingShip = launchingShip;
        this.target = target;
        this.position = launchingShip.getPosition() != null ? 
                new SfbHexPosition(launchingShip.getPosition().getQ(), launchingShip.getPosition().getR(), launchingShip.getPosition().getFacing()) : 
                new SfbHexPosition();
        this.rangeTraveled = 0;
        this.active = true;
    }

    /**
     * Constructor for creating a drone with a target.
     */
    public SfbSeekingWeapon(DroneType droneType, SfbShip launchingShip, SfbShip target) {
        this.id = UUID.randomUUID().toString().substring(0, 8);
        this.type = SeekingWeaponType.DRONE;
        this.initialWarhead = 4; // Standard drone warhead
        this.currentWarhead = 4;
        this.launchingShip = launchingShip;
        this.target = target;
        this.position = launchingShip.getPosition() != null ? 
                new SfbHexPosition(launchingShip.getPosition().getQ(), launchingShip.getPosition().getR(), launchingShip.getPosition().getFacing()) : 
                new SfbHexPosition();
        this.rangeTraveled = 0;
        this.active = true;
    }

    public String getId() { return id; }
    public SeekingWeaponType getType() { return type; }
    public int getInitialWarhead() { return initialWarhead; }
    public int getCurrentWarhead() { return currentWarhead; }
    
    /**
     * Get the current warhead strength (alias for getCurrentWarhead).
     */
    public int getWarheadStrength() { return currentWarhead; }
    
    /**
     * Check if the weapon has reached its target (simplified: after traveling enough range).
     */
    public boolean isAtTarget() {
        if (target == null || position == null || !active) return false;
        // Simplified: weapon hits when it has traveled enough to reach target
        int distance = position.distanceTo(target.getPosition());
        return distance <= 1;
    }
    public SfbShip getTarget() { return target; }
    public SfbShip getLaunchingShip() { return launchingShip; }
    public SfbHexPosition getPosition() { return position; }
    public int getRangeTraveled() { return rangeTraveled; }
    public boolean isActive() { return active; }

    public void setTarget(SfbShip target) { this.target = target; }
    public void setLaunchingShip(SfbShip launchingShip) { this.launchingShip = launchingShip; }
    public void setPosition(SfbHexPosition position) { this.position = position; }

    /**
     * Pursue the target. Moves the weapon toward the target and degrades the warhead.
     */
    public void pursue() {
        if (!active || target == null || position == null) return;

        rangeTraveled++;

        // Degrade warhead for plasma torpedoes
        if (type == SeekingWeaponType.PLASMA_TORPEDO) {
            degrade();
        }

        // Check if weapon has gone inert
        if (currentWarhead <= 0) {
            active = false;
        }
    }

    /**
     * Degrade the warhead based on distance traveled.
     * Plasma torpedoes degrade from initial warhead to 0 over 30+ hexes.
     */
    public void degrade() {
        if (type != SeekingWeaponType.PLASMA_TORPEDO) return;

        // Simple degradation: lose 1 warhead point every 4 hexes
        int degradation = rangeTraveled / 4;
        currentWarhead = Math.max(0, initialWarhead - degradation);
    }

    /**
     * Attempt to intercept this weapon by reducing its warhead.
     * Returns true if the weapon is destroyed.
     */
    public boolean intercept(int damage) {
        if (!active) return false;

        currentWarhead -= damage;
        if (currentWarhead <= 0) {
            active = false;
            return true;
        }
        return false;
    }

    /**
     * Deactivate the weapon (hit target, destroyed, or went inert).
     */
    public void deactivate() {
        this.active = false;
    }

    @Override
    public String toString() {
        return "SfbSeekingWeapon{id=" + id + ", type=" + type +
                ", warhead=" + currentWarhead + "/" + initialWarhead +
                ", range=" + rangeTraveled + ", active=" + active + "}";
    }
}
