package com.yisy.modules.doramcu.dnd5e.dto;

import java.util.List;

/**
 * Response from a skill check or saving throw.
 */
public class SkillCheckResponse {

    private String type; // "skill_check" or "saving_throw"
    private String skill; // e.g. "Athletics", "Stealth", "Strength"
    private int d20Roll;
    private int modifier;
    private int total;
    private boolean advantage;
    private boolean disadvantage;
    private boolean success;
    private int dc; // Difficulty Class
    private String description;

    public SkillCheckResponse() {}

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getSkill() { return skill; }
    public void setSkill(String skill) { this.skill = skill; }

    public int getD20Roll() { return d20Roll; }
    public void setD20Roll(int d20Roll) { this.d20Roll = d20Roll; }

    public int getModifier() { return modifier; }
    public void setModifier(int modifier) { this.modifier = modifier; }

    public int getTotal() { return total; }
    public void setTotal(int total) { this.total = total; }

    public boolean isAdvantage() { return advantage; }
    public void setAdvantage(boolean advantage) { this.advantage = advantage; }

    public boolean isDisadvantage() { return disadvantage; }
    public void setDisadvantage(boolean disadvantage) { this.disadvantage = disadvantage; }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public int getDc() { return dc; }
    public void setDc(int dc) { this.dc = dc; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
