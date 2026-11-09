package com.yisy.modules.doramcu.sfb.model;

import java.util.List;

/**
 * Represents an empire/faction in Star Fleet Battles.
 * <p>
 * Loaded from empires.json in the rules database. Each empire has
 * unique weapons, combat doctrines, and notable ship classes.
 * </p>
 */
public class SfbEmpire {

    private String name;
    private String description;
    private String galaxy; // Alpha Octant, Andromeda, M81, etc.
    private String combatStyle;
    private List<String> primaryWeapons;
    private List<String> notableShips;

    public SfbEmpire() {
    }

    public SfbEmpire(String name, String description, String galaxy) {
        this.name = name;
        this.description = description;
        this.galaxy = galaxy;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getGalaxy() { return galaxy; }
    public void setGalaxy(String galaxy) { this.galaxy = galaxy; }

    public String getCombatStyle() { return combatStyle; }
    public void setCombatStyle(String combatStyle) { this.combatStyle = combatStyle; }

    public List<String> getPrimaryWeapons() { return primaryWeapons; }
    public void setPrimaryWeapons(List<String> primaryWeapons) { this.primaryWeapons = primaryWeapons; }

    public List<String> getNotableShips() { return notableShips; }
    public void setNotableShips(List<String> notableShips) { this.notableShips = notableShips; }

    @Override
    public String toString() {
        return "SfbEmpire{name=" + name + ", galaxy=" + galaxy + "}";
    }
}
