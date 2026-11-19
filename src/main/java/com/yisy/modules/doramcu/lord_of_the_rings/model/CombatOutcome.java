package com.yisy.modules.doramcu.lord_of_the_rings.model;

/**
 * Represents a combat outcome in Lord of the Rings combat resolution.
 */
public enum CombatOutcome {
    MISS("M", "Miss — attack fails to hit target"),
    GLANCE("G", "Glance — minor damage, reduced effectiveness"),
    HIT("H", "Hit — standard damage"),
    CRIT("C", "Critical Hit — severe damage with possible critical effects"),
    EVADE("E", "Evade — target dodges the attack"),
    CORRUPTED("X", "Corrupted — Ring corruption interferes with the attack");

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

    public int severity() {
        switch (this) {
            case EVADE: return 0;
            case MISS: return 0;
            case CORRUPTED: return 1;
            case GLANCE: return 2;
            case HIT: return 3;
            case CRIT: return 4;
            default: return -1;
        }
    }
}
