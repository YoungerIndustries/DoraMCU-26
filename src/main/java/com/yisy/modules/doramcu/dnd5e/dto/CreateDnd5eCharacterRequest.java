package com.yisy.modules.doramcu.dnd5e.dto;

/**
 * Request to create a new D&D 5e character.
 */
public class CreateDnd5eCharacterRequest {

    private String name;
    private String race;
    private String characterClass;
    private String background;
    private Integer strScore;
    private Integer dexScore;
    private Integer conScore;
    private Integer intScore;
    private Integer wisScore;
    private Integer chaScore;
    private String scoreMethod;

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

    public String getCharacterClass() {
        return characterClass;
    }

    public void setCharacterClass(String characterClass) {
        this.characterClass = characterClass;
    }

    public String getBackground() {
        return background;
    }

    public void setBackground(String background) {
        this.background = background;
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

    public String getScoreMethod() {
        return scoreMethod;
    }

    public void setScoreMethod(String scoreMethod) {
        this.scoreMethod = scoreMethod;
    }
}
