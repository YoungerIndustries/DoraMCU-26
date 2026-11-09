package com.yisy.modules.doramcu.sfb.model;

/**
 * Represents a weapon mount on an SFB ship (phasers, disruptors, torpedoes, etc.).
 * <p>
 * Each weapon bank has a firing arc, energy cost, and ammunition (for projectile weapons).
 * The firing arc is defined by the directions it can fire in (0-5).
 * </p>
 */
public class SfbWeaponBank {

    public enum WeaponType {
        PHASER_1, PHASER_2, PHASER_3, PHASER_4,
        DISRUPTOR, VARON_T_DISRUPTOR,
        PHOTON_TORPEDO, FUSION_BEAM, LASER, POLARON,
        PLASMA_TORPEDO, DRONE,
        RAIL_GUN, NEUTRON_BEAM,
        OTHER
    }

    public enum FiringArc {
        F("Forward"), LF("Left-Forward"), RF("Right-Forward"),
        L("Left"), R("Right"),
        LR("Left-Rear"), RR("Right-Rear"),
        A("All-around"),
        LF_R("Left-Forward to Right"), LF_LR("Left-Forward to Left-Rear"),
        RF_RR("Right-Forward to Right-Rear");

        private final String description;

        FiringArc(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    private final String id;
    private final WeaponType weaponType;
    private final FiringArc firingArc;
    private final int energyCost;
    private int ammo; // 0 means unlimited (energy weapons)
    private boolean operational;
    private boolean overloaded;

    public SfbWeaponBank(String id, WeaponType weaponType, FiringArc firingArc, int energyCost) {
        this.id = id;
        this.weaponType = weaponType;
        this.firingArc = firingArc;
        this.energyCost = energyCost;
        this.ammo = 0;
        this.operational = true;
        this.overloaded = false;
    }

    public String getId() { return id; }
    public WeaponType getWeaponType() { return weaponType; }
    public FiringArc getFiringArc() { return firingArc; }
    public int getEnergyCost() { return energyCost; }
    public int getAmmo() { return ammo; }
    public boolean isOperational() { return operational; }
    public boolean isOverloaded() { return overloaded; }

    public void setAmmo(int ammo) { this.ammo = ammo; }
    public void setOperational(boolean operational) { this.operational = operational; }
    public void setOverloaded(boolean overloaded) { this.overloaded = overloaded; }

    /**
     * Check if this weapon can fire at the given target direction.
     */
    public boolean canFireAt(int shipFacing, int targetDirection) {
        if (!operational) return false;
        if (ammo < 0) return false; // projectile weapon out of ammo

        int relativeDir = (targetDirection - shipFacing + 6) % 6;

        switch (firingArc) {
            case F:
                return relativeDir == 0 || relativeDir == 1 || relativeDir == 5;
            case LF:
                return relativeDir == 1 || relativeDir == 2;
            case RF:
                return relativeDir == 4 || relativeDir == 5;
            case L:
                return relativeDir == 2 || relativeDir == 3;
            case R:
                return relativeDir == 3 || relativeDir == 4;
            case LR:
                return relativeDir == 2 || relativeDir == 3 || relativeDir == 4;
            case RR:
                return relativeDir == 1 || relativeDir == 2 || relativeDir == 3;
            case A:
                return true;
            case LF_R:
                return relativeDir >= 1 && relativeDir <= 4;
            case LF_LR:
                return relativeDir >= 1 && relativeDir <= 5;
            case RF_RR:
                return relativeDir == 0 || relativeDir >= 4;
            default:
                return false;
        }
    }

    /**
     * Fire the weapon. Returns true if successful.
     */
    public boolean fire() {
        if (!operational) return false;
        if (ammo < 0) return false;

        if (ammo > 0) {
            ammo--;
        }
        return true;
    }

    /**
     * Get the base damage for this weapon at the given range band.
     * Range bands: 0=point blank, 1=1-2 hexes, 2=3-5 hexes, 3=6-8 hexes,
     * 4=9-12 hexes, 5=13-15 hexes, 6=16+ hexes.
     */
    public int getBaseDamage(int rangeBand) {
        switch (weaponType) {
            case PHASER_1:
                return getPhaser1Damage(rangeBand);
            case PHASER_2:
                return getPhaser2Damage(rangeBand);
            case PHASER_3:
                return getPhaser3Damage(rangeBand);
            case DISRUPTOR:
                return getDisruptorDamage(rangeBand);
            case PHOTON_TORPEDO:
                return 8; // Base damage, can be increased with dial-a-yield
            default:
                return 4; // Default damage for other weapons
        }
    }

    private int getPhaser1Damage(int rangeBand) {
        switch (rangeBand) {
            case 0: case 1: case 2: return 5;
            case 3: return 4;
            case 4: return 3;
            case 5: return 2;
            case 6: return 1;
            default: return 0;
        }
    }

    private int getPhaser2Damage(int rangeBand) {
        switch (rangeBand) {
            case 0: case 1: return 5;
            case 2: return 4;
            case 3: return 3;
            case 4: return 2;
            case 5: return 1;
            case 6: return 1;
            default: return 0;
        }
    }

    private int getPhaser3Damage(int rangeBand) {
        switch (rangeBand) {
            case 0: return 4;
            case 1: return 3;
            case 2: return 2;
            case 3: case 4: return 1;
            case 5: return 1;
            default: return 0;
        }
    }

    private int getDisruptorDamage(int rangeBand) {
        switch (rangeBand) {
            case 0: return 5;
            case 1: case 2: return 4;
            case 3: case 4: return 3;
            case 5: return 2;
            default: return 0;
        }
    }

    /**
     * Get the hit probability for this weapon at the given range band.
     * Returns the maximum die roll (1-6) that scores a hit.
     */
    public int getHitProbability(int rangeBand) {
        switch (weaponType) {
            case PHASER_1:
                switch (rangeBand) {
                    case 0: case 1: case 2: return 6;
                    case 3: return 5;
                    case 4: return 4;
                    case 5: return 3;
                    case 6: return 2;
                    default: return 1;
                }
            case PHASER_2:
                switch (rangeBand) {
                    case 0: case 1: return 6;
                    case 2: return 5;
                    case 3: return 4;
                    case 4: return 3;
                    case 5: return 2;
                    case 6: return 1;
                    default: return 1;
                }
            case PHASER_3:
                switch (rangeBand) {
                    case 0: return 6;
                    case 1: return 5;
                    case 2: return 4;
                    case 3: return 3;
                    case 4: return 2;
                    case 5: return 1;
                    default: return 1;
                }
            case DISRUPTOR:
                switch (rangeBand) {
                    case 0: return 6;
                    case 1: case 2: return 5;
                    case 3: case 4: return 4;
                    case 5: return 3;
                    default: return 1;
                }
            case PHOTON_TORPEDO:
                switch (rangeBand) {
                    case 0: case 1: return 6;
                    case 2: return 5;
                    case 3: return 4;
                    case 4: return 3;
                    case 5: return 2;
                    case 6: return 1;
                    default: return 1;
                }
            default:
                return 4; // Default hit probability
        }
    }

    @Override
    public String toString() {
        return "SfbWeaponBank{id=" + id + ", type=" + weaponType + ", arc=" + firingArc +
                ", ammo=" + ammo + ", operational=" + operational + "}";
    }
}
