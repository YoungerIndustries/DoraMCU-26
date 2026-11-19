package com.yisy.modules.doramcu.lord_of_the_rings.dto;

/**
 * Response for a dice roll in LOTR.
 */
public class DiceRollResponse {

    private String diceType;
    private int roll;
    private int modifier;
    private int total;
    private boolean success;
    private String description;

    public DiceRollResponse() {
    }

    public DiceRollResponse(String diceType, int roll, int modifier, int total, boolean success, String description) {
        this.diceType = diceType;
        this.roll = roll;
        this.modifier = modifier;
        this.total = total;
        this.success = success;
        this.description = description;
    }

    public String getDiceType() {
        return diceType;
    }

    public void setDiceType(String diceType) {
        this.diceType = diceType;
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
