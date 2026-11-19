package com.yisy.modules.doramcu.lord_of_the_rings.dto;

/**
 * Request to create a new Lord of the Rings campaign.
 */
public class CreateLotrCampaignRequest {

    private String name;
    private String description;
    private String dmName;
    private String settingName;
    private Integer startingLevel;

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

    public Integer getStartingLevel() {
        return startingLevel;
    }

    public void setStartingLevel(Integer startingLevel) {
        this.startingLevel = startingLevel;
    }
}
