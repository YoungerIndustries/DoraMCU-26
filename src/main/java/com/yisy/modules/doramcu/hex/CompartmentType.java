package com.yisy.modules.doramcu.hex;

/**
 * Defines the functional type of a {@link Compartment} within a hull structure.
 * <p>
 * Each compartment type determines how the compartment contributes to the
 * overall structure's capabilities and how damage to it affects operations.
 * </p>
 */
public enum CompartmentType {
    // ---- Ship/Space compartments ----

    /** Command and control center — navigation, communications, tactical operations */
    COMMAND,

    /** Power generation and distribution — reactors, engines, power conduits */
    ENGINE,

    /** Storage area for cargo, supplies, or raw materials */
    CARGO,

    /** Living quarters, mess halls, medical facilities for crew */
    CREW,

    /** Weapon systems — missile bays, turret mounts, weapon control */
    WEAPONS,

    /** Shield generators, armor control, defensive systems */
    DEFENSE,

    /** Scientific instruments, laboratories, research equipment */
    RESEARCH,

    /** Docking bays, airlocks, ship maintenance facilities */
    DOCKING,

    /** Corridors, elevators, access tunnels connecting other compartments */
    TRANSIT,

    /** Reinforced storage for hazardous or valuable materials */
    VAULT,

    // ---- Dungeon compartments ----

    /** Throne room or boss chamber — main encounter area */
    THRONES_ROOM,

    /** Treasure vault — loot and valuable artifacts */
    TREASURE_VAULT,

    /** Library or sanctum — ancient knowledge and scrolls */
    LIBRARY,

    /** Prison cells — holding and captivity */
    PRISON,

    /** Armory or workshop — crafting and weapon storage */
    ARMORY,

    /** Trap room — hazardous area with deadly mechanisms */
    TRAP_ROOM,

    /** Crypt or pit — burial chambers or bottomless pits */
    CRYPT,

    /** Intersection — crossroads where corridors meet */
    INTERSECTION,

    // ---- Facility compartments ----

    /** Mining shaft — underground resource extraction */
    MINING_SHAFT,

    /** Power generator — energy production facility */
    POWER_GENERATOR,

    /** Warehouse — large storage area */
    WAREHOUSE,

    /** Processing plant — resource refinement */
    PROCESSING_PLANT,

    /** Worker quarters — facility staff housing */
    WORKER_QUARTERS,

    /** Communications array — signal transmission */
    COMMS_ARRAY,

    /** Empty or unassigned space — no specific function */
    EMPTY;

    /**
     * Maps a dungeon room type string to the closest CompartmentType.
     * <p>
     * Provides bidirectional mapping between domain-specific types and universal types.
     * </p>
     *
     * @param typeStr the type string (case-insensitive)
     * @return the matching CompartmentType, or EMPTY if no match
     */
    public static CompartmentType fromString(String typeStr) {
        if (typeStr == null) return EMPTY;
        String upper = typeStr.toUpperCase().trim();
        try {
            return CompartmentType.valueOf(upper);
        } catch (IllegalArgumentException e) {
            // Fuzzy mapping for common aliases
            return switch (upper) {
                case "BOSS_CHAMBER" -> THRONES_ROOM;
                case "STORAGE" -> CARGO;
                case "SANCTUM" -> LIBRARY;
                case "WORKSHOP" -> ARMORY;
                case "PIT" -> CRYPT;
                case "CORRIDOR" -> TRANSIT;
                default -> EMPTY;
            };
        }
    }
}
