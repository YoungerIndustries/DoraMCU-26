package com.yisy.modules.doramcu.traveller;

/**
 * Enum for armor types from Classic Traveller.
 */
public enum Armor {

    NONE(0, 0, 0, "No armor"),
    LIGHT(1, 1, 1, "Light leather armor"),
    MEDIUM(2, 2, 2, "Medium padded armor"),
    HEAVY(3, 3, 3, "Heavy plate armor"),
    POWERSUIT(4, 4, 4, "Powered combat suit");

    private final int cutProtection;
    private final int thrustProtection;
    private final int impactProtection;
    private final String description;

    Armor(int cutProtection, int thrustProtection, int impactProtection, String description) {
        this.cutProtection = cutProtection;
        this.thrustProtection = thrustProtection;
        this.impactProtection = impactProtection;
        this.description = description;
    }

    public String getName() {
        return name().replace('_', ' ');
    }

    public int getProtection(AttackType type) {
        switch (type) {
            case CUT: return cutProtection;
            case THRUST: return thrustProtection;
            case IMPACT: return impactProtection;
            default: return 0;
        }
    }

    public boolean isPowered() {
        return this == POWERSUIT;
    }

    public int getCutProtection() {
        return cutProtection;
    }

    public int getThrustProtection() {
        return thrustProtection;
    }

    public int getImpactProtection() {
        return impactProtection;
    }

    public String getDescription() {
        return description;
    }

    public enum AttackType {
        CUT,
        THRUST,
        IMPACT
    }
}
