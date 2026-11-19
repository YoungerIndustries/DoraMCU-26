package com.yisy.modules.doramcu.lord_of_the_rings.dto;

/**
 * Response for an attack roll in LOTR combat.
 */
public class LotrAttackResponse {

    private String attackerName;
    private String defenderName;
    private int attackRoll;
    private int attackModifier;
    private int attackTotal;
    private int armorClass;
    private boolean hit;
    private int damageRoll;
    private int damageModifier;
    private int damageTotal;
    private String damageType;
    private String description;

    public LotrAttackResponse() {
    }

    public LotrAttackResponse(String attackerName, String defenderName, int attackRoll, int attackModifier,
                              int attackTotal, int armorClass, boolean hit, int damageRoll, int damageModifier,
                              int damageTotal, String damageType, String description) {
        this.attackerName = attackerName;
        this.defenderName = defenderName;
        this.attackRoll = attackRoll;
        this.attackModifier = attackModifier;
        this.attackTotal = attackTotal;
        this.armorClass = armorClass;
        this.hit = hit;
        this.damageRoll = damageRoll;
        this.damageModifier = damageModifier;
        this.damageTotal = damageTotal;
        this.damageType = damageType;
        this.description = description;
    }

    public String getAttackerName() {
        return attackerName;
    }

    public void setAttackerName(String attackerName) {
        this.attackerName = attackerName;
    }

    public String getDefenderName() {
        return defenderName;
    }

    public void setDefenderName(String defenderName) {
        this.defenderName = defenderName;
    }

    public int getAttackRoll() {
        return attackRoll;
    }

    public void setAttackRoll(int attackRoll) {
        this.attackRoll = attackRoll;
    }

    public int getAttackModifier() {
        return attackModifier;
    }

    public void setAttackModifier(int attackModifier) {
        this.attackModifier = attackModifier;
    }

    public int getAttackTotal() {
        return attackTotal;
    }

    public void setAttackTotal(int attackTotal) {
        this.attackTotal = attackTotal;
    }

    public int getArmorClass() {
        return armorClass;
    }

    public void setArmorClass(int armorClass) {
        this.armorClass = armorClass;
    }

    public boolean isHit() {
        return hit;
    }

    public void setHit(boolean hit) {
        this.hit = hit;
    }

    public int getDamageRoll() {
        return damageRoll;
    }

    public void setDamageRoll(int damageRoll) {
        this.damageRoll = damageRoll;
    }

    public int getDamageModifier() {
        return damageModifier;
    }

    public void setDamageModifier(int damageModifier) {
        this.damageModifier = damageModifier;
    }

    public int getDamageTotal() {
        return damageTotal;
    }

    public void setDamageTotal(int damageTotal) {
        this.damageTotal = damageTotal;
    }

    public String getDamageType() {
        return damageType;
    }

    public void setDamageType(String damageType) {
        this.damageType = damageType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
