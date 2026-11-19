package com.yisy.modules.doramcu.lord_of_the_rings.dto;

/**
 * Encounter analysis result for LOTR.
 */
public class EncounterAnalysis {

    private String encounterName;
    private int partyLevel;
    private int partySize;
    private int monsterXp;
    private int monsterCount;
    private String averageCr;
    private String difficulty;
    private int adjustedXp;
    private String recommendation;

    public EncounterAnalysis() {
    }

    public EncounterAnalysis(String encounterName, int partyLevel, int partySize, int monsterXp,
                             int monsterCount, String averageCr, String difficulty, int adjustedXp,
                             String recommendation) {
        this.encounterName = encounterName;
        this.partyLevel = partyLevel;
        this.partySize = partySize;
        this.monsterXp = monsterXp;
        this.monsterCount = monsterCount;
        this.averageCr = averageCr;
        this.difficulty = difficulty;
        this.adjustedXp = adjustedXp;
        this.recommendation = recommendation;
    }

    public String getEncounterName() {
        return encounterName;
    }

    public void setEncounterName(String encounterName) {
        this.encounterName = encounterName;
    }

    public int getPartyLevel() {
        return partyLevel;
    }

    public void setPartyLevel(int partyLevel) {
        this.partyLevel = partyLevel;
    }

    public int getPartySize() {
        return partySize;
    }

    public void setPartySize(int partySize) {
        this.partySize = partySize;
    }

    public int getMonsterXp() {
        return monsterXp;
    }

    public void setMonsterXp(int monsterXp) {
        this.monsterXp = monsterXp;
    }

    public int getMonsterCount() {
        return monsterCount;
    }

    public void setMonsterCount(int monsterCount) {
        this.monsterCount = monsterCount;
    }

    public String getAverageCr() {
        return averageCr;
    }

    public void setAverageCr(String averageCr) {
        this.averageCr = averageCr;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public int getAdjustedXp() {
        return adjustedXp;
    }

    public void setAdjustedXp(int adjustedXp) {
        this.adjustedXp = adjustedXp;
    }

    public String getRecommendation() {
        return recommendation;
    }

    public void setRecommendation(String recommendation) {
        this.recommendation = recommendation;
    }
}
