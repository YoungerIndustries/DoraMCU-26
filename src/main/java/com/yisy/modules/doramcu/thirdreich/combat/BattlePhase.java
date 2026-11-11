package com.yisy.modules.doramcu.thirdreich.combat;

/**
 * Phases of a 3R4 ground combat round.
 * <p>
 * Each round of combat proceeds through these phases in order:
 * movement, combat, and morale checks.
 * </p>
 */
public enum BattlePhase {

    /**
     * Movement phase. Units advance, retreat, or reposition.
     */
    MOVEMENT("Movement", "Units advance, retreat, or reposition"),

    /**
     * Combat phase. Attacks are resolved using CRT tables.
     */
    COMBAT("Combat", "Attacks resolved using CRT tables"),

    /**
     * Morale phase. Units check morale after combat.
     */
    MORALE("Morale", "Units check morale after combat");

    private final String displayName;
    private final String description;

    BattlePhase(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    /** Returns the display name. */
    public String getDisplayName() {
        return displayName;
    }

    /** Returns the description. */
    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
