package com.yisy.modules.doramcu.gamecore;

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
    ZERO_ZERO_SEVEN("007", "James Bond espionage 4X game"),
    FPS("FPS", "First-Person Shooter framework"),
    MECHWARRIOR("MechWarrior", "BattleTech-style mech combat simulation"),
    STARSHIP_TROOPERS("Starship Troopers", "Federated Solar System vs Arachnid tactical combat"),
    STAR_WARS("Star Wars", "Galactic-scale combat across eras: Republic, Empire, Rebels, and more"),
    TRANSPORT_TYCOON("Transport Tycoon", "Economic strategy and transport management"),
    WARHAMMER_40K("Warhammer 40K", "Grimdark sci-fi universe: Imperium, Chaos, Orks, Necrons, Eldar, Tyranids");

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
