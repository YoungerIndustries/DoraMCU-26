package com.yisy.modules.doramcu.sfb.model;

/**
 * Roles a Captain's Log character can play in an SFB battle.
 */
public enum SfbRole {
    ADMIRAL("Fleet commander, provides fleet-wide bonuses"),
    CAPTAIN("Ship commander, modifies ship systems"),
    CREW("Station operator, modifies specific system performance");

    private final String description;

    SfbRole(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
