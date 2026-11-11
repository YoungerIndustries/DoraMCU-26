package com.yisy.modules.doramcu.traveller;

import java.util.Objects;

/**
 * Represents a weapon mounted on a {@link TravellerNavalVessel}.
 * <p>
 * A weapon mount links a Traveller ship weapon ({@link ShipWeaponSystem}) to a naval vessel,
 * adding naval-specific attributes like mount type, ammo count, and arc of fire.
 * </p>
 * <p>
 * Mount types:
 * </p>
 * <ul>
 *   <li><b>TURRET</b> — rotating gun turret, typically 360° arc</li>
 *   <li><b>BATTERY</b> — fixed gun battery, limited arc (e.g., forward or broadside)</li>
 *   <li><b>MISSILE_SILE</b> — missile launch silo, directional firing</li>
 * </ul>
 * <p>
 * The mount references a {@link ShipWeaponSystem} for damage calculations
 * and provides naval-specific combat factors for CRT resolution.
 * </p>
 */
public class NavalWeaponMount {

    /**
     * Types of weapon mounts.
     */
    public enum MountType {
        /** Rotating gun turret with full or near-full arc of fire */
        TURRET("Turret", 360),
        /** Fixed gun battery with limited arc (forward, broadside, etc.) */
        BATTERY("Battery", 120),
        /** Missile launch silo with directional firing */
        MISSILE_SILE("Missile Silo", 90);

        private final String displayName;
        private final int defaultArc;

        MountType(String displayName, int defaultArc) {
            this.displayName = displayName;
            this.defaultArc = defaultArc;
        }

        public String getDisplayName() { return displayName; }
        public int getDefaultArc() { return defaultArc; }
    }

    private final String id;
    private final MountType mountType;
    private final ShipWeaponSystem shipWeapon;
    private int ammoCount;
    private int arcOfFire;
    private boolean isOperational;

    /**
     * Creates a new weapon mount.
     *
     * @param id the unique mount identifier
     * @param mountType the type of mount (turret, battery, missile silo)
     * @param shipWeapon the Traveller ship weapon system
     */
    public NavalWeaponMount(String id, MountType mountType, ShipWeaponSystem shipWeapon) {
        this.id = Objects.requireNonNull(id, "ID must not be null");
        this.mountType = Objects.requireNonNull(mountType, "Mount type must not be null");
        this.shipWeapon = Objects.requireNonNull(shipWeapon, "Ship weapon must not be null");
        this.ammoCount = shipWeapon.isMissile() ? 10 : Integer.MAX_VALUE;
        this.arcOfFire = mountType.getDefaultArc();
        this.isOperational = true;
    }

    // ---- Accessors ----

    /** Returns the mount ID. */
    public String getId() { return id; }

    /** Returns the mount type. */
    public MountType getMountType() { return mountType; }

    /** Returns the underlying ship weapon system. */
    public ShipWeaponSystem getShipWeapon() { return shipWeapon; }

    /** Returns the ammo count (Integer.MAX_VALUE for non-missile weapons). */
    public int getAmmoCount() { return ammoCount; }

    /** Sets the ammo count. */
    public void setAmmoCount(int ammoCount) {
        this.ammoCount = Math.max(0, ammoCount);
    }

    /** Returns the arc of fire in degrees. */
    public int getArcOfFire() { return arcOfFire; }

    /** Sets the arc of fire in degrees (0-360). */
    public void setArcOfFire(int arcOfFire) {
        this.arcOfFire = Math.max(0, Math.min(360, arcOfFire));
    }

    /** Returns whether the mount is operational. */
    public boolean isOperational() { return isOperational; }

    /** Sets the operational status. */
    public void setOperational(boolean operational) {
        this.isOperational = operational;
    }

    // ---- Derived Values ----

    /**
     * Returns the damage value for CRT combat resolution.
     * <p>
     * Based on the weapon's average damage, scaled for naval combat factors.
     * </p>
     *
     * @return damage combat factor
     */
    public int getDamageValue() {
        if (!isOperational) {
            return 0;
        }
        // Scale weapon damage to naval combat factors
        // Average damage × 2 for naval CRT
        return (int) (shipWeapon.getAverageDamage() * 2);
    }

    /**
     * Returns the cost of this weapon mount in credits.
     *
     * @return cost in credits
     */
    public long getCost() {
        // Weapon tons × 50 + mount cost
        long weaponCost = (long) shipWeapon.getTotalTons() * 50;
        long mountCost = switch (mountType) {
            case TURRET -> 50000;
            case BATTERY -> 25000;
            case MISSILE_SILE -> 100000;
        };
        return weaponCost + mountCost;
    }

    /**
     * Returns the tons consumed by this mount.
     *
     * @return tons consumed
     */
    public int getTons() {
        return shipWeapon.getTotalTons();
    }

    /**
     * Fires this weapon, consuming one ammo if applicable.
     *
     * @return true if the weapon fired successfully
     */
    public boolean fire() {
        if (!isOperational) {
            return false;
        }
        if (shipWeapon.isMissile() && ammoCount <= 0) {
            return false;
        }
        if (shipWeapon.isMissile()) {
            ammoCount--;
        }
        return true;
    }

    /**
     * Creates a copy of this mount.
     *
     * @return a copy of this mount
     */
    public NavalWeaponMount copy() {
        NavalWeaponMount copy = new NavalWeaponMount(id, mountType, shipWeapon);
        copy.setAmmoCount(ammoCount);
        copy.setArcOfFire(arcOfFire);
        copy.setOperational(isOperational);
        return copy;
    }

    @Override
    public String toString() {
        return String.format("%s %s (%d° arc)",
                mountType.getDisplayName(), shipWeapon.getWeaponType(), arcOfFire);
    }
}
