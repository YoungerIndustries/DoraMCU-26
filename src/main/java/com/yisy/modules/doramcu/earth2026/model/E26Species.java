package com.yisy.modules.doramcu.earth2026.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;
import java.util.Map;

/**
 * Species definition from the Earth 2026 rules database.
 * <p>
 * Each species has unique traits, characteristics, and a name database
 * for generating AI character names that match the species' lore.
 * </p>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class E26Species {

    private String id;
    private String name;
    private String description;
    private String gameModule;
    private Map<String, Integer> characteristics;
    private int combatDM;
    private int socialDM;
    private int navigationDM;
    private int armorBonus;
    private int tradeBonusPercent;
    private int lifespan;
    private String homeworld;
    private E26NameDatabase nameDatabase;

    public E26Species() {
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getGameModule() { return gameModule; }
    public void setGameModule(String gameModule) { this.gameModule = gameModule; }

    public Map<String, Integer> getCharacteristics() { return characteristics; }
    public void setCharacteristics(Map<String, Integer> characteristics) { this.characteristics = characteristics; }

    public int getCombatDM() { return combatDM; }
    public void setCombatDM(int combatDM) { this.combatDM = combatDM; }

    public int getSocialDM() { return socialDM; }
    public void setSocialDM(int socialDM) { this.socialDM = socialDM; }

    public int getNavigationDM() { return navigationDM; }
    public void setNavigationDM(int navigationDM) { this.navigationDM = navigationDM; }

    public int getArmorBonus() { return armorBonus; }
    public void setArmorBonus(int armorBonus) { this.armorBonus = armorBonus; }

    public int getTradeBonusPercent() { return tradeBonusPercent; }
    public void setTradeBonusPercent(int tradeBonusPercent) { this.tradeBonusPercent = tradeBonusPercent; }

    public int getLifespan() { return lifespan; }
    public void setLifespan(int lifespan) { this.lifespan = lifespan; }

    public String getHomeworld() { return homeworld; }
    public void setHomeworld(String homeworld) { this.homeworld = homeworld; }

    public E26NameDatabase getNameDatabase() { return nameDatabase; }
    public void setNameDatabase(E26NameDatabase nameDatabase) { this.nameDatabase = nameDatabase; }

    @Override
    public String toString() {
        return name + " (" + id + ")";
    }
}
