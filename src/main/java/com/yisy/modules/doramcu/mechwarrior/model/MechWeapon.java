package com.yisy.modules.doramcu.mechwarrior.model;

import java.util.UUID;

/**
 * Represents a MechWarrior weapon with heat generation, cooldown, and damage.
 * <p>
 * Weapons are categorized as Energy, Ballistic, or Missile. Each weapon generates
 * heat when fired and has a cooldown period before it can fire again.
 * </p>
 */
public class MechWeapon {

    public enum WeaponType {
        ENERGY,       // Lasers, PPCs
        BALLISTIC,    // Autocannons, Gauss Rifles
        MISSILE       // SRMs, LRMs
    }

    public enum WeaponSubType {
        // Energy weapons
        SMALL_LASER,
        MEDIUM_LASER,
        LARGE_LASER,
        MEDIUM_PULSE_LASER,
        LARGE_PULSE_LASER,
        EXTENDED_PULSE_LASER,
        PPC,
        ER_PPC,
        FLAME_THROWER,

        // Ballistic weapons
        AC2,
        AC5,
        AC10,
        AC20,
        LBX_10,
        LBX_20,
        GAUSS_RIFLE,
        UAC_20,
        MACHINE_GUN,
        ROTARY_AC,

        // Missile weapons
        SRM_2,
        SRM_4,
        SRM_6,
        SRM_2_STREAK,
        SRM_4_STREAK,
        SRM_6_STREAK,
        LRM_5,
        LRM_10,
        LRM_15,
        LRM_20,
        LRM_5_STREAK,
        LRM_10_STREAK,
        LRM_15_STREAK,
        LRM_20_STREAK,

        // Special
        AMS,           // Anti-Missile System
        ECM,           // Electronic Countermeasures
        ECCM           // Electronic Counter-Countermeasures
    }

    private String id;
    private String name;
    private WeaponType type;
    private WeaponSubType subType;
    private int damage;            // base damage per shot
    private int heatGenerated;     // heat generated per shot
    private double cooldownTime;   // seconds before weapon can fire again
    private int range;             // effective range in meters
    private int tonnage;           // weight in tons
    private int criticalSlots;     // critical hit slots used
    private int ammoCapacity;      // ammo shots (0 for energy weapons)
    private int ammoHeat;          // heat generated if ammo explodes
    private boolean critSeeking;   // true for MGs, LBX autocannons
    private String description;

    // Cooldown tracking
    private long cooldownStart;    // timestamp when cooldown started
    private boolean onCooldown;    // currently on cooldown

    public MechWeapon() {
    }

    public MechWeapon(String name, WeaponType type, WeaponSubType subType, int damage, int heatGenerated, double cooldownTime) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.type = type;
        this.subType = subType;
        this.damage = damage;
        this.heatGenerated = heatGenerated;
        this.cooldownTime = cooldownTime;
        this.range = 600;
        this.tonnage = 1;
        this.criticalSlots = 1;
        this.ammoCapacity = 0;
    }

    // Getters and Setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public WeaponType getType() {
        return type;
    }

    public void setType(WeaponType type) {
        this.type = type;
    }

    public WeaponSubType getSubType() {
        return subType;
    }

    public void setSubType(WeaponSubType subType) {
        this.subType = subType;
    }

    public int getDamage() {
        return damage;
    }

    public void setDamage(int damage) {
        this.damage = damage;
    }

    public int getHeatGenerated() {
        return heatGenerated;
    }

    public void setHeatGenerated(int heatGenerated) {
        this.heatGenerated = heatGenerated;
    }

    public double getCooldownTime() {
        return cooldownTime;
    }

    public void setCooldownTime(double cooldownTime) {
        this.cooldownTime = cooldownTime;
    }

    public int getRange() {
        return range;
    }

    public void setRange(int range) {
        this.range = range;
    }

    public int getTonnage() {
        return tonnage;
    }

    public void setTonnage(int tonnage) {
        this.tonnage = tonnage;
    }

    public int getCriticalSlots() {
        return criticalSlots;
    }

    public void setCriticalSlots(int criticalSlots) {
        this.criticalSlots = criticalSlots;
    }

    public int getAmmoCapacity() {
        return ammoCapacity;
    }

    public void setAmmoCapacity(int ammoCapacity) {
        this.ammoCapacity = ammoCapacity;
    }

    public int getAmmoHeat() {
        return ammoHeat;
    }

    public void setAmmoHeat(int ammoHeat) {
        this.ammoHeat = ammoHeat;
    }

    public boolean isCritSeeking() {
        return critSeeking;
    }

    public void setCritSeeking(boolean critSeeking) {
        this.critSeeking = critSeeking;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Check if this weapon is currently on cooldown.
     *
     * @param currentTime current time in milliseconds
     * @return true if weapon is on cooldown
     */
    public boolean isOnCooldown(long currentTime) {
        if (!onCooldown) return false;
        double elapsed = (currentTime - cooldownStart) / 1000.0;
        return elapsed < cooldownTime;
    }

    /**
     * Update cooldown status based on current time.
     */
    public void updateCooldown() {
        if (onCooldown) {
            double elapsed = (System.currentTimeMillis() - cooldownStart) / 1000.0;
            if (elapsed >= cooldownTime) {
                onCooldown = false;
            }
        }
    }

    /**
     * Start the cooldown timer.
     */
    public void startCooldown() {
        this.onCooldown = true;
        this.cooldownStart = System.currentTimeMillis();
    }

    public long getCooldownStart() {
        return cooldownStart;
    }

    public void setCooldownStart(long cooldownStart) {
        this.cooldownStart = cooldownStart;
    }

    public boolean isOnCooldown() {
        return onCooldown;
    }

    public void setOnCooldown(boolean onCooldown) {
        this.onCooldown = onCooldown;
    }
}
