package com.yisy.modules.doramcu.hex.dungeon;

/**
 * Enum representing dungeon-specific room types for categorizing module templates.
 * <p>
 * These types are tailored for dungeon adventures, as opposed to the ship/space-station
 * focused {@link com.yisy.modules.doramcu.hex.CompartmentType}. Each type describes
 * the primary function or atmosphere of a dungeon room.
 * </p>
 */
public enum DungeonRoom {

    /** A grand room with a throne, typically the center of power in a dungeon */
    THRONES_ROOM("Throne Room", "grand,authority,boss"),

    /** A room filled with treasure, gold, and valuable artifacts */
    TREASURE_VAULT("Treasure Vault", "treasure,loot,riches"),

    /** A narrow passage connecting rooms */
    CORRIDOR("Corridor", "passage,connection,narrow"),

    /** A junction where multiple corridors meet */
    INTERSECTION("Intersection", "junction,crossroads,hub"),

    /** A room containing traps, hazards, or puzzles */
    TRAP_ROOM("Trap Room", "danger,hazard,puzzle"),

    /** A cell or holding area for prisoners */
    PRISON("Prison", "cells,holding,captivity"),

    /** A room with bookshelves, scrolls, and knowledge */
    LIBRARY("Library", "books,knowledge,scrolls"),

    /** A room storing weapons, armor, and military equipment */
    ARMORY("Armory", "weapons,armor,military"),

    /** A sacred or ritualistic chamber */
    SANCTUM("Sanctum", "sacred,ritual,holy"),

    /** A burial chamber or tomb */
    CRYPT("Crypt", "tomb,burial,undead"),

    /** A room with a pit, chasm, or vertical drop */
    PIT("Pit", "chasm,drop,vertical"),

    /** A large chamber designed for a major encounter */
    BOSS_CHAMBER("Boss Chamber", "encounter,final,large"),

    /** A storage room for supplies and materials */
    STORAGE("Storage", "supplies,materials,stockpile"),

    /** A workshop or crafting area */
    WORKSHOP("Workshop", "crafting,alchemy,tools"),

    /** An empty or undetermined room */
    EMPTY("Empty Room", "blank,undetermined");

    private final String displayName;
    private final String defaultTags;

    DungeonRoom(String displayName, String defaultTags) {
        this.displayName = displayName;
        this.defaultTags = defaultTags;
    }

    /** @return a human-readable name for this room type */
    public String getDisplayName() {
        return displayName;
    }

    /** @return comma-separated default tags for this room type */
    public String getDefaultTags() {
        return defaultTags;
    }
}
