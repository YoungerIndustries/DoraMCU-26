package com.yisy.modules.doramcu.lord_of_the_rings.dto;

/**
 * Response for a skill check in LOTR.
 */
public class SkillCheckResponse {

    private String type;
    private String characterName;
    private String skill;
    private int d20Roll;
    private int roll;
    private int modifier;
    private int total;
    private int dc;
    private boolean success;
    private boolean advantage;
    private boolean disadvantage;
    private String description;

    public SkillCheckResponse() {
    }

    public SkillCheckResponse(String characterName, String skill, int roll, int modifier, int total, int dc, boolean success, String description) {
        this.characterName = characterName;
        this.skill = skill;
        this.roll = roll;
        this.modifier = modifier;
        this.total = total;
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

    public String getSkill() {
        return skill;
    }

    public void setSkill(String skill) {
        this.skill = skill;
    }

    public int getRoll() {
        return roll;
    }

    public void setRoll(int roll) {
        this.roll = roll;
    }

    public int getModifier() {
        return modifier;
    }

    public void setModifier(int modifier) {
        this.modifier = modifier;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getD20Roll() {
        return d20Roll;
    }

    public void setD20Roll(int d20Roll) {
        this.d20Roll = d20Roll;
    }

    public boolean isAdvantage() {
        return advantage;
    }

    public void setAdvantage(boolean advantage) {
        this.advantage = advantage;
    }

    public boolean isDisadvantage() {
        return disadvantage;
    }

    public void setDisadvantage(boolean disadvantage) {
        this.disadvantage = disadvantage;
    }
}
