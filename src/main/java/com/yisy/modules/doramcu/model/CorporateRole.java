package com.yisy.modules.doramcu.model;

/**
 * Corporate role that a player can hold within their corporation.
 * <p>
 * Supports a dual-identity system where every player has both a CEO identity
 * (full corporation control) and a VP identity (delegated operations).
 * The player toggles between them via an API endpoint.
 * </p>
 */
public enum CorporateRole {

    /**
     * Chief Executive Officer — full control over corporation operations.
     * In this mode, the player directly manages contracts, hiring, and strategy.
     */
    CEO("Chief Executive Officer"),

    /**
     * Vice President — delegated operations mode.
     * In this mode, the player's AI assistant takes over corporation operations,
     * while the player remains in the orders loop but can adventure.
     */
    VP("Vice President");

    private final String displayName;

    CorporateRole(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Returns the human-readable display name for this role.
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Checks if this role is the CEO role.
     */
    public boolean isCEO() {
        return this == CEO;
    }

    /**
     * Checks if this role is the VP role.
     */
    public boolean isVP() {
        return this == VP;
    }

    /**
     * Returns the opposite role (CEO → VP, VP → CEO).
     * Used for role toggling.
     */
    public CorporateRole switchTo() {
        return this == CEO ? VP : CEO;
    }
}
