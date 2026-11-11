package com.yisy.modules.doramcu.traveller;

import java.util.ArrayList;
import java.util.List;

/**
 * Traveller spacecraft for air and space combat.
 * Supports fighters, bombers, interceptors, and custom designs.
 */
public class TravellerSpacecraft {
    private String id;
    private String name;
    private TravellerSpacecraftType type;
    private int techLevel;
    private int armorClass;
    private int hullPoints;
    private int movement;
    private int crew;
    private int cost;
    private List<AirWeaponMount> weapons = new ArrayList<>();
    private int altitude;
    private boolean pressurized;
    private int attackValue;
    private int defenseValue;

    public TravellerSpacecraft() {}

    public TravellerSpacecraft(String id, String name, TravellerSpacecraftType type, int techLevel) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.techLevel = techLevel;
        this.armorClass = type.getBaseArmor();
        this.hullPoints = 10 + techLevel * 2;
        this.movement = type.getBaseMovement() + techLevel;
        this.crew = type.getBaseCrew();
        this.cost = type.getBaseCost() * (1 + techLevel);
        this.pressurized = techLevel >= 8;
    }

    public void addWeapon(AirWeaponMount weapon) {
        weapons.add(weapon);
        recalculateCombatFactors();
    }

    public void recalculateCombatFactors() {
        attackValue = 0;
        for (AirWeaponMount w : weapons) {
            attackValue += w.getDamageCode();
        }
        defenseValue = armorClass + (pressurized ? 1 : 0);
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public TravellerSpacecraftType getType() { return type; }
    public void setType(TravellerSpacecraftType type) { this.type = type; }
    public int getTechLevel() { return techLevel; }
    public void setTechLevel(int techLevel) { this.techLevel = techLevel; }
    public int getArmorClass() { return armorClass; }
    public void setArmorClass(int armorClass) { this.armorClass = armorClass; }
    public int getHullPoints() { return hullPoints; }
    public void setHullPoints(int hullPoints) { this.hullPoints = hullPoints; }
    public int getMovement() { return movement; }
    public void setMovement(int movement) { this.movement = movement; }
    public int getCrew() { return crew; }
    public void setCrew(int crew) { this.crew = crew; }
    public int getCost() { return cost; }
    public void setCost(int cost) { this.cost = cost; }
    public List<AirWeaponMount> getWeapons() { return weapons; }
    public void setWeapons(List<AirWeaponMount> weapons) { this.weapons = weapons; }
    public int getAltitude() { return altitude; }
    public void setAltitude(int altitude) { this.altitude = altitude; }
    public boolean isPressurized() { return pressurized; }
    public void setPressurized(boolean pressurized) { this.pressurized = pressurized; }
    public int getAttackValue() { return attackValue; }
    public void setAttackValue(int attackValue) { this.attackValue = attackValue; }
    public int getDefenseValue() { return defenseValue; }
    public void setDefenseValue(int defenseValue) { this.defenseValue = defenseValue; }
}
