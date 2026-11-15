package com.yisy.modules.doramcu.corporation.model;

/**
 * Predefined corporate positions for AI character hiring.
 * <p>
 * Split into leadership positions (available in CEO mode) and crew positions
 * (available in VP mode). Each position has a display name and description.
 * </p>
 */
public enum CorporatePosition {

    // ---- Leadership positions (CEO mode) ----

    /** Manages corporation finances and investments */
    CFO("Chief Financial Officer", "Manages corporation finances and investments"),

    /** Oversees daily operations and logistics */
    COO("Chief Operations Officer", "Oversees daily operations and logistics"),

    /** Leads research and development */
    CTO("Chief Technology Officer", "Leads research and development"),

    /** Represents corporation in sector governance */
    SECTOR_GOVERNOR_DELEGATE("Sector Governor Delegate", "Represents corporation in sector governance"),

    /** Manages trade routes and negotiations */
    TRADE_MINISTER("Trade Minister", "Manages trade routes and negotiations"),

    // ---- Crew positions (VP mode) ----

    /** Commands corporation vessels */
    SHIP_CAPTAIN("Ship Captain", "Commands corporation vessels"),

    /** Leads security and defense operations */
    SECURITY_CHIEF("Security Chief", "Leads security and defense operations"),

    /** Conducts research and analysis */
    SCIENCE_OFFICER("Science Officer", "Conducts research and analysis"),

    /** Maintains and upgrades equipment */
    ENGINEER("Engineer", "Maintains and upgrades equipment");

    private final String displayName;
    private final String description;

    CorporatePosition(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    /**
     * Returns the human-readable display name.
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Returns the position description.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Checks if this is a leadership position (available in CEO mode).
     */
    public boolean isLeadership() {
        return this == CFO || this == COO || this == CTO
                || this == SECTOR_GOVERNOR_DELEGATE || this == TRADE_MINISTER;
    }

    /**
     * Checks if this is a crew position (available in VP mode).
     */
    public boolean isCrew() {
        return !isLeadership();
    }

    /**
     * Returns the primary skill for this position.
     * Used for career progression and skill-based contract success calculation.
     */
    public HireSkill getPrimarySkill() {
        return HireSkill.fromPosition(this);
    }
}
