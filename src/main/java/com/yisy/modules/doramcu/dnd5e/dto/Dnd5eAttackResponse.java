package com.yisy.modules.doramcu.dnd5e.dto;

/**
 * Response from a D&D 5e attack roll.
 */
public class Dnd5eAttackResponse {

    private String attackerName;
    private String targetName;
    private int attackRoll;
    private int attackTotal;
    private int targetAC;
    private boolean hit;
    private boolean criticalHit;
    private boolean criticalFail;
    private int damageDealt;
    private String damageType;
    private int remainingHP;
    private String message;

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

    public int getAttackRoll() {
        return attackRoll;
    }

    public void setAttackRoll(int attackRoll) {
        this.attackRoll = attackRoll;
    }

    public int getAttackTotal() {
        return attackTotal;
    }

    public void setAttackTotal(int attackTotal) {
        this.attackTotal = attackTotal;
    }

    public int getTargetAC() {
        return targetAC;
    }

    public void setTargetAC(int targetAC) {
        this.targetAC = targetAC;
    }

    public boolean isHit() {
        return hit;
    }

    public void setHit(boolean hit) {
        this.hit = hit;
    }

    public boolean isCriticalHit() {
        return criticalHit;
    }

    public void setCriticalHit(boolean criticalHit) {
        this.criticalHit = criticalHit;
    }

    public boolean isCriticalFail() {
        return criticalFail;
    }

    public void setCriticalFail(boolean criticalFail) {
        this.criticalFail = criticalFail;
    }

    public int getDamageDealt() {
        return damageDealt;
    }

    public void setDamageDealt(int damageDealt) {
        this.damageDealt = damageDealt;
    }

    public String getDamageType() {
        return damageType;
    }

    public void setDamageType(String damageType) {
        this.damageType = damageType;
    }

    public int getRemainingHP() {
        return remainingHP;
    }

    public void setRemainingHP(int remainingHP) {
        this.remainingHP = remainingHP;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
