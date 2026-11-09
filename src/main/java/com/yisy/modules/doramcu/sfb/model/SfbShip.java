package com.yisy.modules.doramcu.sfb.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Core entity representing a starship in Star Fleet Battles.
 * <p>
 * Holds position, facing, speed, SSD reference, energy allocation,
 * shields, weapons, and special system state. Provides behaviors
 * for movement, weapons fire, energy allocation, and damage.
 * </p>
 */
public class SfbShip {

    private String id;
    private String name;
    private SfbEmpire empire;
    private String shipClass;
    private SfbHexPosition position;
    private int speed; // 1-32
    private int turnMode; // 1-3
    private SfbSSD ssd;
    private SfbEnergyAllocation energyAllocation;
    private SfbShield[] shields; // 6 directional shields
    private List<SfbWeaponBank> weaponBanks;
    private List<SfbSeekingWeapon> launchedWeapons;
    private boolean cloaked;
    private boolean crippled;
    private boolean destroyed;
    private int side; // 0 or 1, for two-player battles
    private int hexesMovedThisTurn; // Track movement for acceleration limits
    private int previousSpeed; // Track previous speed for acceleration validation

    public SfbShip() {
        this.shields = new SfbShield[6];
        this.weaponBanks = new ArrayList<>();
        this.launchedWeapons = new ArrayList<>();
        this.speed = 16;
        this.turnMode = 1;
        this.side = 0;
        this.hexesMovedThisTurn = 0;
        this.previousSpeed = 16;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public SfbEmpire getEmpire() { return empire; }
    public void setEmpire(SfbEmpire empire) { this.empire = empire; }

    public String getShipClass() { return shipClass; }
    public void setShipClass(String shipClass) { this.shipClass = shipClass; }

    public SfbHexPosition getPosition() { return position; }
    public void setPosition(SfbHexPosition position) { this.position = position; }

    public int getSpeed() { return speed; }
    public void setSpeed(int speed) {
        this.speed = Math.max(1, Math.min(32, speed));
    }

    public int getTurnMode() { return turnMode; }
    public void setTurnMode(int turnMode) {
        this.turnMode = Math.max(1, Math.min(3, turnMode));
    }

    public SfbSSD getSsd() { return ssd; }
    public void setSsd(SfbSSD ssd) { this.ssd = ssd; }

    public SfbEnergyAllocation getEnergyAllocation() { return energyAllocation; }
    public void setEnergyAllocation(SfbEnergyAllocation energyAllocation) {
        this.energyAllocation = energyAllocation;
    }

    public SfbShield[] getShields() { return shields; }
    public void setShields(SfbShield[] shields) { this.shields = shields; }

    public List<SfbWeaponBank> getWeaponBanks() { return weaponBanks; }
    public void setWeaponBanks(List<SfbWeaponBank> weaponBanks) { this.weaponBanks = weaponBanks; }

    public void addWeaponBank(SfbWeaponBank bank) { this.weaponBanks.add(bank); }

    public List<SfbSeekingWeapon> getLaunchedWeapons() { return launchedWeapons; }

    public boolean isCloaked() { return cloaked; }
    public void setCloaked(boolean cloaked) { this.cloaked = cloaked; }

    public boolean isCrippled() { return crippled; }
    public void setCrippled(boolean crippled) { this.crippled = crippled; }

    public boolean isDestroyed() { return destroyed; }
    public void setDestroyed(boolean destroyed) { this.destroyed = destroyed; }

    public int getSide() { return side; }
    public void setSide(int side) { this.side = side; }

    public int getHexesMovedThisTurn() { return hexesMovedThisTurn; }
    public void setHexesMovedThisTurn(int hexesMovedThisTurn) { this.hexesMovedThisTurn = hexesMovedThisTurn; }

    public int getPreviousSpeed() { return previousSpeed; }
    public void setPreviousSpeed(int previousSpeed) { this.previousSpeed = previousSpeed; }

    /**
     * Reset movement tracking at the start of a new turn.
     * Saves current speed as previous speed for acceleration validation.
     */
    public void resetMovementTracking() {
        this.previousSpeed = this.speed;
        this.hexesMovedThisTurn = 0;
    }

    /**
     * Record a hex movement this turn.
     */
    public void recordHexMoved() {
        this.hexesMovedThisTurn++;
    }

    /**
     * Initialize the shields for this ship.
     */
    public void initializeShields(int boxesPerShield) {
        for (int i = 0; i < 6; i++) {
            shields[i] = new SfbShield(i, boxesPerShield);
        }
    }

    /**
     * Move the ship by the given number of hexes in the current facing direction.
     */
    public void move(int hexes) {
        if (position != null && !crippled && !destroyed) {
            position.move(hexes);
        }
    }

    /**
     * Turn the ship by the given number of directions.
     */
    public void turn(int directions) {
        if (position != null && !crippled && !destroyed) {
            position.turn(directions);
        }
    }

    /**
     * Fire all weapons that can target the given direction.
     * Returns the list of weapons that fired.
     */
    public List<SfbWeaponBank> fireWeapons(int targetDirection) {
        List<SfbWeaponBank> fired = new ArrayList<>();
        if (crippled || destroyed) return fired;

        for (SfbWeaponBank bank : weaponBanks) {
            if (bank.isOperational() && position != null) {
                if (bank.canFireAt(position.getFacing(), targetDirection)) {
                    if (bank.fire()) {
                        fired.add(bank);
                    }
                }
            }
        }
        return fired;
    }

    /**
     * Take damage from a specific direction.
     * First absorbs through shields, then applies internal damage.
     * Returns the amount of damage that reaches internal systems.
     */
    public int takeDamage(int damage, int direction) {
        if (destroyed) return 0;

        // First pass through shields
        SfbShield shield = shields[direction % 6];
        if (shield != null && !shield.isDown()) {
            damage = shield.absorbDamage(damage);
        }

        // Remaining damage goes to internal systems
        if (damage > 0) {
            ssd.addExcessDamage(damage);
            if (ssd.isDestroyed()) {
                destroyed = true;
            }
        }

        return damage;
    }

    /**
     * Allocate energy for this turn.
     */
    public void allocateEnergy(SfbEnergyAllocation allocation) {
        this.energyAllocation = allocation;
    }

    /**
     * Launch a seeking weapon.
     */
    public void launchSeekingWeapon(SfbSeekingWeapon weapon) {
        weapon.setLaunchingShip(this);
        launchedWeapons.add(weapon);
    }

    /**
     * Update all launched seeking weapons (pursue targets, degrade, etc.).
     */
    public void updateSeekingWeapons() {
        for (SfbSeekingWeapon weapon : launchedWeapons) {
            if (weapon.isActive()) {
                weapon.pursue();
            }
        }
        // Remove inactive weapons
        launchedWeapons.removeIf(w -> !w.isActive());
    }

    /**
     * Check if the ship is still in combat (not crippled or destroyed).
     */
    public boolean isInCombat() {
        return !crippled && !destroyed;
    }

    @Override
    public String toString() {
        return "SfbShip{id=" + id + ", name=" + name + ", empire=" +
                (empire != null ? empire.getName() : "null") +
                ", speed=" + speed + ", side=" + side +
                ", crippled=" + crippled + ", destroyed=" + destroyed + "}";
    }
}
