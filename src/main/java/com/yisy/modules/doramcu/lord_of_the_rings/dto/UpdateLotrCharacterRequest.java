package com.yisy.modules.doramcu.lord_of_the_rings.dto;

/**
 * Request to update an existing Lord of the Rings character.
 */
public class UpdateLotrCharacterRequest {

    private String name;
    private String race;
    private String calling;
    private String background;
    private Integer level;
    private Integer strScore;
    private Integer dexScore;
    private Integer conScore;
    private Integer intScore;
    private Integer wisScore;
    private Integer chaScore;
    private Integer hitPoints;
    private Integer maxHitPoints;
    private Integer armorClass;
    private Integer corruption;
    private Integer hope;
    private Integer shadow;
    private String faction;
    private String homeRealm;
    private String ringBearer;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRace() {
        return race;
    }

    public void setRace(String race) {
        this.race = race;
    }

    public String getCalling() {
        return calling;
    }

    public void setCalling(String calling) {
        this.calling = calling;
    }

    public String getBackground() {
        return background;
    }

    public void setBackground(String background) {
        this.background = background;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public Integer getStrScore() {
        return strScore;
    }

    public void setStrScore(Integer strScore) {
        this.strScore = strScore;
    }

    public Integer getDexScore() {
        return dexScore;
    }

    public void setDexScore(Integer dexScore) {
        this.dexScore = dexScore;
    }

    public Integer getConScore() {
        return conScore;
    }

    public void setConScore(Integer conScore) {
        this.conScore = conScore;
    }

    public Integer getIntScore() {
        return intScore;
    }

    public void setIntScore(Integer intScore) {
        this.intScore = intScore;
    }

    public Integer getWisScore() {
        return wisScore;
    }

    public void setWisScore(Integer wisScore) {
        this.wisScore = wisScore;
    }

    public Integer getChaScore() {
        return chaScore;
    }

    public void setChaScore(Integer chaScore) {
        this.chaScore = chaScore;
    }

    public Integer getHitPoints() {
        return hitPoints;
    }

    public void setHitPoints(Integer hitPoints) {
        this.hitPoints = hitPoints;
    }

    public Integer getMaxHitPoints() {
        return maxHitPoints;
    }

    public void setMaxHitPoints(Integer maxHitPoints) {
        this.maxHitPoints = maxHitPoints;
    }

    public Integer getArmorClass() {
        return armorClass;
    }

    public void setArmorClass(Integer armorClass) {
        this.armorClass = armorClass;
    }

    public Integer getCorruption() {
        return corruption;
    }

    public void setCorruption(Integer corruption) {
        this.corruption = corruption;
    }

    public Integer getHope() {
        return hope;
    }

    public void setHope(Integer hope) {
        this.hope = hope;
    }

    public Integer getShadow() {
        return shadow;
    }

    public void setShadow(Integer shadow) {
        this.shadow = shadow;
    }

    public String getFaction() {
        return faction;
    }

    public void setFaction(String faction) {
        this.faction = faction;
    }

    public String getHomeRealm() {
        return homeRealm;
    }

    public void setHomeRealm(String homeRealm) {
        this.homeRealm = homeRealm;
    }

    public String getRingBearer() {
        return ringBearer;
    }

    public void setRingBearer(String ringBearer) {
        this.ringBearer = ringBearer;
    }
}
