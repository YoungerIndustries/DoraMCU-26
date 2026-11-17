package com.yisy.modules.doramcu.earth2026.model;
import com.yisy.modules.doramcu.model.galaxy.Galaxy;


/**
 * Enum defining the types of NPCs in the Younger Industries HQ.
 */
public enum HqNpcType {

    RECEPTIONIST("Receptionist", "Greets visitors and provides general information about the HQ", "Lobby"),
    SECURITY_GUARD("Security Guard", "Monitors security and handles access control", "Lobby"),
    JANITOR("Janitor", "Maintains the building and can repair office equipment", "Basement"),
    TECH_SUPPORT("Tech Support", "Helps with kiosk upgrades and technical issues", "Floor 1"),
    LEGAL_ADVISOR("Legal Advisor", "Provides legal consultation for contracts and disputes", "Floor 2"),
    RECRUITER("Recruiter", "Helps find and hire AI characters for your corporation", "Floor 3"),
    TRADER("Trader", "Buys and sells resources and equipment", "Floor 1"),
    MEDIC("Medic", "Provides health services and first aid", "Floor 2"),
    BARISTA("Barista", "Serves coffee and refreshments in the lobby cafe", "Lobby"),
    NEWS_ANCHOR("News Anchor", "Provides updates on galaxy events and news", "Lobby"),
    HISTORIAN("Historian", "Shares stories about the history of Younger Industries", "Floor 3"),
    PORTAL_OPERATOR("Portal Operator", "Manages the basement portals to other galaxies", "Basement B1");

    private final String displayName;
    private final String description;
    private final String defaultLocation;

    HqNpcType(String displayName, String description, String defaultLocation) {
        this.displayName = displayName;
        this.description = description;
        this.defaultLocation = defaultLocation;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public String getDefaultLocation() {
        return defaultLocation;
    }
}
