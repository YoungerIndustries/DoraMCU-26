package com.yisy.modules.doramcu.traveller;

/**
 * Enum for High Guard ship weapons.
 */
public enum HighGuardWeapon {

    PULSE_LASER(2, 5, WeaponCategory.LASER, HighGuardRangeBand.LONG, 5, "Standard pulse laser weapon"),
    BEAM_LASER(3, 8, WeaponCategory.LASER, HighGuardRangeBand.MEDIUM, 7, "Focused beam laser weapon"),
    PARTICLE_BEAM(4, 10, WeaponCategory.PARTICLE, HighGuardRangeBand.MEDIUM, 8, "High energy particle beam"),
    MISSILE_HE(6, 20, WeaponCategory.MISSILE, HighGuardRangeBand.EXTREME, 10, "High explosive missile"),
    MISSILE_KINETIC(8, 25, WeaponCategory.MISSILE, HighGuardRangeBand.EXTREME, 10, "Kinetic impact missile"),
    TORPEDO_BASIC(10, 40, WeaponCategory.TORPEDO, HighGuardRangeBand.EXTREME, 12, "Basic torpedo weapon"),
    TORPEDO_ADVANCED(12, 60, WeaponCategory.TORPEDO, HighGuardRangeBand.EXTREME, 14, "Advanced torpedo weapon"),
    MESON_GUN(14, 50, WeaponCategory.PARTICLE, HighGuardRangeBand.LONG, 12, "Meson particle gun"),
    MISSILE_NUCLEAR(16, 30, WeaponCategory.MISSILE, HighGuardRangeBand.EXTREME, 13, "Nuclear warhead missile"),
    TORPEDO_NUCLEAR(18, 70, WeaponCategory.TORPEDO, HighGuardRangeBand.EXTREME, 15, "Nuclear warhead torpedo"),
    FUSION_GUN(10, 30, WeaponCategory.BEAM, HighGuardRangeBand.LONG, 10, "Fusion gun weapon"),
    SANDCASTER(12, 40, WeaponCategory.SPECIAL, HighGuardRangeBand.EXTREME, 12, "Sandcaster special weapon"),
    BEAM(8, 20, WeaponCategory.BEAM, HighGuardRangeBand.MEDIUM, 8, "Generic beam weapon"),
    SPECIAL(6, 15, WeaponCategory.SPECIAL, HighGuardRangeBand.LONG, 6, "Generic special weapon");

    private final int baseDamageDice;
    private final int tons;
    private final WeaponCategory category;
    private final HighGuardRangeBand optimalRange;
    private final int minTechLevel;
    private final String description;

    HighGuardWeapon(int baseDamageDice, int tons, WeaponCategory category, HighGuardRangeBand optimalRange, int minTechLevel, String description) {
        this.baseDamageDice = baseDamageDice;
        this.tons = tons;
        this.category = category;
        this.optimalRange = optimalRange;
        this.minTechLevel = minTechLevel;
        this.description = description;
    }

    public int getBaseDamageDice() {
        return baseDamageDice;
    }

    public int getTons() {
        return tons;
    }

    public String getName() {
        return name().replace('_', ' ');
    }

    public WeaponCategory getCategory() {
        return category;
    }

    public HighGuardRangeBand getOptimalRange() {
        return optimalRange;
    }

    public int getMinTechLevel() {
        return minTechLevel;
    }

    public String getDescription() {
        return description;
    }

    public boolean isInRange(HighGuardRangeBand range) {
        return range == optimalRange || range == HighGuardRangeBand.MEDIUM || range == HighGuardRangeBand.LONG;
    }

    public int getHitModifier(HighGuardRangeBand range) {
        if (range == optimalRange) return 2;
        if (isInRange(range)) return 0;
        return -2;
    }

    public int getFlightTime(HighGuardRangeBand range) {
        switch (range) {
            case ADJACENT: return 1;
            case CLOSE: return 1;
            case SHORT: return 2;
            case MEDIUM: return 3;
            case LONG: return 5;
            case VERY_LONG: return 7;
            case DISTANT: return 10;
            default: return 3;
        }
    }

    public boolean isBeam() {
        return category == WeaponCategory.LASER || category == WeaponCategory.PARTICLE;
    }

    public boolean isSpecial() {
        return name().contains("MESON");
    }

    public boolean isMeson() {
        return this == MESON_GUN;
    }

    public enum WeaponCategory {
        LASER, PARTICLE, MISSILE, TORPEDO, BEAM, SPECIAL
    }

    public int getTurretTonnage(TurretSize size) {
        return tons * size.getMultiplier();
    }

    public enum TurretSize {
        SINGLE(1),
        DOUBLE(2),
        TRIPLE(3);

        private final int multiplier;

        TurretSize(int multiplier) {
            this.multiplier = multiplier;
        }

        public int getMultiplier() {
            return multiplier;
        }

        public String getName() {
            return name().toLowerCase();
        }
    }

    public enum BaySize {
        SMALL(10),
        LARGE(20);

        private final int tons;

        BaySize(int tons) {
            this.tons = tons;
        }

        public int getTons() {
            return tons;
        }

        public String getName() {
            return name().toLowerCase();
        }
    }
}
