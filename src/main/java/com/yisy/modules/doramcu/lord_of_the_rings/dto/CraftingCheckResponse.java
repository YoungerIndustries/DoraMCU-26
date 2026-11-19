package com.yisy.modules.doramcu.lord_of_the_rings.dto;

/**
 * Response for a crafting check in LOTR.
 */
public class CraftingCheckResponse {

    private String characterName;
    private String recipeName;
    private int roll;
    private int dc;
    private boolean success;
    private String description;

    public CraftingCheckResponse() {
    }

    public CraftingCheckResponse(String characterName, String recipeName, int roll, int dc, boolean success, String description) {
        this.characterName = characterName;
        this.recipeName = recipeName;
        this.roll = roll;
        this.dc = dc;
        this.success = success;
        this.description = description;
    }

    public String getCharacterName() {
        return characterName;
    }

    public void setCharacterName(String characterName) {
        this.characterName = characterName;
    }

    public String getRecipeName() {
        return recipeName;
    }

    public void setRecipeName(String recipeName) {
        this.recipeName = recipeName;
    }

    public int getRoll() {
        return roll;
    }

    public void setRoll(int roll) {
        this.roll = roll;
    }

    public int getDc() {
        return dc;
    }

    public void setDc(int dc) {
        this.dc = dc;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
