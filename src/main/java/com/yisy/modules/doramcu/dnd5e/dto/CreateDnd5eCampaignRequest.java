package com.yisy.modules.doramcu.dnd5e.dto;

/**
 * DTO for creating a D&D 5e campaign.
 */
public class CreateDnd5eCampaignRequest {

    private String name;
    private String description;
    private String dmName;
    private String settingName;
    private int startingLevel = 1;

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

    public String getDmName() {
        return dmName;
    }

    public void setDmName(String dmName) {
        this.dmName = dmName;
    }

    public String getSettingName() {
        return settingName;
    }

    public void setSettingName(String settingName) {
        this.settingName = settingName;
    }

    public int getStartingLevel() {
        return startingLevel;
    }

    public void setStartingLevel(int startingLevel) {
        this.startingLevel = startingLevel;
    }
}
