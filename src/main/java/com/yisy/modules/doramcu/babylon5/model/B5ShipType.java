package com.yisy.modules.doramcu.babylon5.model;

import java.util.List;

/**
 * Represents a Babylon 5 ship type for fleet combat.
 * <p>
 * Each ship type has stats for hull, armor, weapons, speed, and crew requirements.
 * Ship types are associated with a specific faction/race.
 * </p>
 */
public class B5ShipType {

    private String id;
    private String name;
    private String faction; // "Earth Alliance", "Narn", "Centauri", "Minbari", etc.
    private String classType; // "Frigate", "Cruiser", "Battle Cruiser", "Destroyer", etc.

    /** Hull points (hit points). */
    private int hull;

    /** Armor value. */
    private int armor;

    /** Offensive weapon power. */
    private int weapons;

    /** Defensive systems power. */
    private int defenses;

    /** Speed / maneuverability. */
    private int speed;

    /** Crew required to operate. */
    private int crew;

    /** Available weapon systems (e.g., "Lasers", "Missiles", "Particle Beam"). */
    private List<String> weaponSystems;

    /** Available defensive systems (e.g., "Point Defense", "Energy Shield"). */
    private List<String> defensiveSystems;

    public B5ShipType() {
    }

    public B5ShipType(String id, String name, String faction, String classType) {
        this.id = id;
        this.name = name;
        this.faction = faction;
        this.classType = classType;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getFaction() { return faction; }
    public void setFaction(String faction) { this.faction = faction; }

    public String getClassType() { return classType; }
    public void setClassType(String classType) { this.classType = classType; }

    public int getHull() { return hull; }
    public void setHull(int hull) { this.hull = hull; }

    public int getArmor() { return armor; }
    public void setArmor(int armor) { this.armor = armor; }

    public int getWeapons() { return weapons; }
    public void setWeapons(int weapons) { this.weapons = weapons; }

    public int getDefenses() { return defenses; }
    public void setDefenses(int defenses) { this.defenses = defenses; }

    public int getSpeed() { return speed; }
    public void setSpeed(int speed) { this.speed = speed; }

    public int getCrew() { return crew; }
    public void setCrew(int crew) { this.crew = crew; }

    public List<String> getWeaponSystems() { return weaponSystems; }
    public void setWeaponSystems(List<String> weaponSystems) { this.weaponSystems = weaponSystems; }

    public List<String> getDefensiveSystems() { return defensiveSystems; }
    public void setDefensiveSystems(List<String> defensiveSystems) { this.defensiveSystems = defensiveSystems; }

    /** Calculate total combat power for this ship. */
    public int getCombatPower() {
        return weapons + defenses + armor;
    }
}
