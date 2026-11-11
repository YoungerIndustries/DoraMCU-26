package com.yisy.modules.doramcu.starshiptroopers.model;

/**
 * Represents the possible outcomes of a combat resolution roll.
 */
public enum CombatOutcome {
    MISS("M", "Miss - No effect"),
    DISRUPTED("D", "Disrupted - Target cannot move or attack next turn"),
    WIA("W", "WIA - Wounded in Action - Unit wounded with reduced effectiveness"),
    KIA("K", "KIA - Killed in Action - Unit eliminated from play"),
    HVY("H", "HVY - Heavy Damage - Unit heavily damaged");

    private final String code;
    private final String description;

    CombatOutcome(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static CombatOutcome fromCode(String code) {
        for (CombatOutcome outcome : values()) {
            if (outcome.code.equals(code)) {
                return outcome;
            }
        }
        throw new IllegalArgumentException("Unknown combat outcome code: " + code);
    }

    /**
     * Returns the severity level of this outcome (higher = worse for the target).
     */
    public int severity() {
        switch (this) {
            case MISS: return 0;
            case DISRUPTED: return 1;
            case WIA: return 2;
            case HVY: return 3;
            case KIA: return 4;
            default: return -1;
        }
    }
}
