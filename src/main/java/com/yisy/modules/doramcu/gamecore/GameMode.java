package com.yisy.modules.doramcu.gamecore;

/**
 * Represents the different game modes that can be combined when creating a new game/galaxy.
 * <p>
 * Players can select multiple modes to create hybrid game experiences.
 * For example, a Star Wars galaxy could be both RPG and 4X, allowing
 * character-driven storytelling alongside empire management.
 * </p>
 */
public enum GameMode {

    /**
     * Role-Playing Game mode: character-driven storytelling with quests,
     * dialogue, skill checks, and narrative choices.
     */
    RPG("RPG", "Role-Playing Game — character-driven storytelling with quests and narrative choices"),

    /**
     * 4X strategy mode: eXplore, eXpand, eXploit, eXterminate.
     * Empire management, diplomacy, research, and galactic conquest.
     */
    FOUR_X("4X", "4X Strategy — eXplore, eXpand, eXploit, eXterminate empire management"),

    /**
     * Tycoon mode: economic simulation with resource management,
     * trade routes, industry chains, and profit optimization.
     */
    TYCOON("Tycoon", "Tycoon — economic simulation with trade routes and industry management"),

    /**
     * Tactical combat mode: turn-based or real-time tactical battles
     * with positioning, cover, and unit abilities.
     */
    TACTICAL("Tactical", "Tactical Combat — turn-based tactical battles with positioning and unit abilities"),

    /**
     * Survival mode: resource scarcity, environmental hazards,
     * and base building under pressure.
     */
    SURVIVAL("Survival", "Survival — resource scarcity and base building under environmental pressure"),

    /**
     * Sandbox mode: open-ended creative freedom with minimal
     * constraints and goals.
     */
    SANDBOX("Sandbox", "Sandbox — open-ended creative freedom with minimal constraints"),

    /**
     * MMORPG mode: persistent multiplayer world with social features,
     * guilds, and shared economy.
     */
    MMORPG("MMORPG", "MMORPG — persistent multiplayer world with social features and guilds"),

    /**
     * Roguelike mode: procedurally generated content with permadeath
     * and run-based progression.
     */
    ROGUELIKE("Roguelike", "Roguelike — procedural generation with permadeath and run-based progression"),

    /**
     * Simulation mode: realistic physics, economics, and social systems.
     */
    SIMULATION("Simulation", "Simulation — realistic physics, economics, and social systems"),

    /**
     * Adventure mode: puzzle-solving, exploration, and story-driven
     * progression with light combat.
     */
    ADVENTURE("Adventure", "Adventure — puzzle-solving and story-driven exploration with light combat"),

    /**
     * FPS mode: first-person shooter with real-time combat,
     * weapon management, and tactical movement.
     */
    FPS("FPS", "FPS — first-person shooter with real-time combat and tactical movement");

    private final String displayName;
    private final String description;

    GameMode(String displayName, String description) {
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
