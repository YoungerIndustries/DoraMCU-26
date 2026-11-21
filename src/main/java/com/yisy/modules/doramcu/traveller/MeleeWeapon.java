package com.yisy.modules.doramcu.traveller;

/**
 * Enum for melee weapons from Classic Traveller.
 */
public enum MeleeWeapon {

    CUTLASS(WeaponType.CUT_THRUST, 2, 3, 4, false),
    SWORD(WeaponType.CUT_THRUST, 3, 4, 5, false),
    DAGGER(WeaponType.CUT_THRUST, 1, 2, 3, false),
    BATON(WeaponType.IMPACT, 1, 2, 3, false),
    FIST(WeaponType.IMPACT, 0, 1, 2, false),
    UNARMED(WeaponType.IMPACT, 0, 1, 2, false);

    private final WeaponType type;
    private final int cut;
    private final int thrust;
    private final int parry;
    private final boolean powered;

    MeleeWeapon(WeaponType type, int cut, int thrust, int parry, boolean powered) {
        this.type = type;
        this.cut = cut;
        this.thrust = thrust;
        this.parry = parry;
        this.powered = powered;
    }

    public String getName() {
        return name().replace('_', ' ');
    }

    public WeaponType getType() {
        return type;
    }

    public int getCut() {
        return cut;
    }

    public int getThrust() {
        return thrust;
    }

    public boolean canCut() {
        return type == WeaponType.CUT || type == WeaponType.CUT_THRUST;
    }

    public boolean canThrust() {
        return type == WeaponType.THRUST || type == WeaponType.CUT_THRUST;
    }

    public int getParry() {
        return parry;
    }

    public boolean isPowered() {
        return powered;
    }

    public String getDescription() {
        return getName() + " melee weapon (" + type.name().replace('_', ' ') + ")";
    }

    public enum WeaponType {
        CUT, THRUST, IMPACT, CUT_THRUST;
        
        public String getName() {
            return name().replace('_', ' ');
        }
    }
}
