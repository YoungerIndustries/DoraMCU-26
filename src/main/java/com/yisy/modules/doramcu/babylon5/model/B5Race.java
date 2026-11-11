package com.yisy.modules.doramcu.babylon5.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Represents a Babylon 5 race/species with characteristics, abilities, and modifiers.
 */
public class B5Race {

    private String id;
    private String name;
    private String description;
    private String homeworld;
    private String government;
    private int lifespan;
    private B5Characteristics characteristics;
    private List<String> specialAbilities;
    private String combatStyle;
    private int tradeBonus;
    private int socialDM;
    private int navigationDM;
    private int armorBonus;

    public B5Race() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getHomeworld() {
        return homeworld;
    }

    public void setHomeworld(String homeworld) {
        this.homeworld = homeworld;
    }

    public String getGovernment() {
        return government;
    }

    public void setGovernment(String government) {
        this.government = government;
    }

    public int getLifespan() {
        return lifespan;
    }

    public void setLifespan(int lifespan) {
        this.lifespan = lifespan;
    }

    public B5Characteristics getCharacteristics() {
        return characteristics;
    }

    public void setCharacteristics(B5Characteristics characteristics) {
        this.characteristics = characteristics;
    }

    public List<String> getSpecialAbilities() {
        return specialAbilities;
    }

    public void setSpecialAbilities(List<String> specialAbilities) {
        this.specialAbilities = specialAbilities;
    }

    public String getCombatStyle() {
        return combatStyle;
    }

    public void setCombatStyle(String combatStyle) {
        this.combatStyle = combatStyle;
    }

    public int getTradeBonus() {
        return tradeBonus;
    }

    public void setTradeBonus(int tradeBonus) {
        this.tradeBonus = tradeBonus;
    }

    public int getSocialDM() {
        return socialDM;
    }

    public void setSocialDM(int socialDM) {
        this.socialDM = socialDM;
    }

    public int getNavigationDM() {
        return navigationDM;
    }

    public void setNavigationDM(int navigationDM) {
        this.navigationDM = navigationDM;
    }

    public int getArmorBonus() {
        return armorBonus;
    }

    public void setArmorBonus(int armorBonus) {
        this.armorBonus = armorBonus;
    }

    @Override
    public String toString() {
        return "B5Race{id='" + id + "', name='" + name + "', homeworld='" + homeworld + "'}";
    }
}
