package com.yisy.modules.doramcu.dnd5e.dto;

/**
 * Request to make an attack in D&D 5e combat.
 */
public class Dnd5eAttackRequest {

    private String attackerName;
    private String targetName;
    private int attackBonus;
    private String damageFormula;
    private String damageType;
    private boolean advantage;
    private boolean disadvantage;

    public String getAttackerName() {
        return attackerName;
    }

    public void setAttackerName(String attackerName) {
        this.attackerName = attackerName;
    }

    public String getTargetName() {
        return targetName;
    }

    public void setTargetName(String targetName) {
        this.targetName = targetName;
    }

    public int getAttackBonus() {
        return attackBonus;
    }

    public void setAttackBonus(int attackBonus) {
        this.attackBonus = attackBonus;
    }

    public String getDamageFormula() {
        return damageFormula;
    }

    public void setDamageFormula(String damageFormula) {
        this.damageFormula = damageFormula;
    }

    public String getDamageType() {
        return damageType;
    }

    public void setDamageType(String damageType) {
        this.damageType = damageType;
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
