package com.yisy.modules.doramcu.unified;

/**
 * Identifies which game system a character belongs to.
 */
public enum GameType {

    TRAVELLER("Traveller", "Classic space-faring RPG with careers, skills, and exploration"),
    CAPTAINS_LOG("Captain's Log", "Solo Star Trek Adventures RPG with missions and character development"),
    DORAMCU_4X("DoraMCU 4X", "4X empire strategy game — build and manage your galactic empire"),
    EARTH_2026("Earth 2026", "Sci-fi RPG set in a near-future Earth"),
    THIRD_REICH("Third Reich", "Ground combat simulation engine"),
    BABYLON_5("Babylon 5", "Babylon 5 universe RPG"),
    DND_5E("D&D 5th Edition", "Dungeons & Dragons 5th Edition"),
    STAR_TREK_4X("Star Trek 4X", "Star Trek themed 4X strategy game"),
    SFB("Star Frontiers Battle", "Star Frontiers combat and exploration"),
    ZERO_ZERO_SEVEN("007", "James Bond espionage 4X game");

    private final String displayName;
    private final String description;

    GameType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }
}
