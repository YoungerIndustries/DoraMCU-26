package com.yisy.modules.doramcu.traveller.shipcustomization;

/**
 * Types of ship components that can be installed on custom ship designs.
 * <p>
 * Each component type has specific slots on a ship and contributes
 * to the ship's overall combat and navigation capabilities.
 * </p>
 */
public enum ComponentType {

    /** Turret-mounted weapons: lasers, particle beams, fusion guns, meson guns */
    TURRET_WEAPON("Turret Weapon", "Ship-mounted turret weapons"),

    /** Bay-mounted weapons: missiles, heavy torpedoes */
    BAY_WEAPON("Bay Weapon", "Missile bays and heavy weapon mounts"),

    /** Hull armor: titanium steel, crystaliron, bonded superdense */
    ARMOR("Armor", "Hull armor plating for damage absorption"),

    /** Maneuver drive: fusion drive for combat thrust */
    MANEUVER_DRIVE("Maneuver Drive", "Fusion drive for combat maneuvering"),

    /** Jump drive: for interstellar travel */
    JUMP_DRIVE("Jump Drive", "Interstellar jump capability"),

    /** Power plant: generates energy for systems and weapons */
    POWER_PLANT("Power Plant", "Energy generation for ship systems"),

    /** Computer: fire control, navigation, and ship management */
    COMPUTER("Computer", "Ship computer for fire control and navigation"),

    /** Sensors: detection and targeting */
    SENSORS("Sensors", "Detection and targeting systems"),

    /** Nuclear dampers: defense against nuclear weapons */
    NUCLEAR_DAMPERS("Nuclear Dampers", "Defense against nuclear warheads"),

    /** Meson screens: defense against meson guns */
    MESON_SCREENS("Meson Screens", "Defense against meson penetration"),

    /** Cargo capacity: storage for trade goods */
    CARGO("Cargo", "Cargo storage capacity"),

    /** Fuel tanks: fuel for maneuver drive */
    FUEL("Fuel", "Fuel storage for maneuver drive"),

    /** Life support: crew habitat systems */
    LIFE_SUPPORT("Life Support", "Crew habitat and life support"),

    /** Marine force: boarding party with weapons */
    MARINES("Marines", "Marine boarding party"),

    /** Crew quarters: accommodation for ship crew */
    CREW("Crew", "Crew accommodation and facilities");

    private final String displayName;
    private final String description;

    ComponentType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
}
