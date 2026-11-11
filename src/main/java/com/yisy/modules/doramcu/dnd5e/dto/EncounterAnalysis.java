package com.yisy.modules.doramcu.dnd5e.dto;

/**
 * DTO representing the result of an encounter analysis.
 * <p>
 * Contains the XP thresholds for a given party size and level,
 * the encounter's monster XP, adjusted XP, and determined difficulty.
 * </p>
 */
public class EncounterAnalysis {

    private int partyLevel;
    private int partySize;
    private int monsterXp;
    private double encounterMultiplier;
    private int tensionBonus;
    private int adjustedXp;
    private String difficulty;
    private int trivialThreshold;
    private int easyThreshold;
    private int mediumThreshold;
    private int hardThreshold;
    private int deadlyThreshold;
    private String recommendation;

    public EncounterAnalysis() {
    }

    public EncounterAnalysis(int partyLevel, int partySize, int monsterXp, double encounterMultiplier,
                             int tensionBonus, int adjustedXp, String difficulty,
                             int trivialThreshold, int easyThreshold, int mediumThreshold,
                             int hardThreshold, int deadlyThreshold, String recommendation) {
        this.partyLevel = partyLevel;
        this.partySize = partySize;
        this.monsterXp = monsterXp;
        this.encounterMultiplier = encounterMultiplier;
        this.tensionBonus = tensionBonus;
        this.adjustedXp = adjustedXp;
        this.difficulty = difficulty;
        this.trivialThreshold = trivialThreshold;
        this.easyThreshold = easyThreshold;
        this.mediumThreshold = mediumThreshold;
        this.hardThreshold = hardThreshold;
        this.deadlyThreshold = deadlyThreshold;
        this.recommendation = recommendation;
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

    public double getEncounterMultiplier() {
        return encounterMultiplier;
    }

    public void setEncounterMultiplier(double encounterMultiplier) {
        this.encounterMultiplier = encounterMultiplier;
    }

    public int getTensionBonus() {
        return tensionBonus;
    }

    public void setTensionBonus(int tensionBonus) {
        this.tensionBonus = tensionBonus;
    }

    public int getAdjustedXp() {
        return adjustedXp;
    }

    public void setAdjustedXp(int adjustedXp) {
        this.adjustedXp = adjustedXp;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public int getTrivialThreshold() {
        return trivialThreshold;
    }

    public void setTrivialThreshold(int trivialThreshold) {
        this.trivialThreshold = trivialThreshold;
    }

    public int getEasyThreshold() {
        return easyThreshold;
    }

    public void setEasyThreshold(int easyThreshold) {
        this.easyThreshold = easyThreshold;
    }

    public int getMediumThreshold() {
        return mediumThreshold;
    }

    public void setMediumThreshold(int mediumThreshold) {
        this.mediumThreshold = mediumThreshold;
    }

    public int getHardThreshold() {
        return hardThreshold;
    }

    public void setHardThreshold(int hardThreshold) {
        this.hardThreshold = hardThreshold;
    }

    public int getDeadlyThreshold() {
        return deadlyThreshold;
    }

    public void setDeadlyThreshold(int deadlyThreshold) {
        this.deadlyThreshold = deadlyThreshold;
    }

    public String getRecommendation() {
        return recommendation;
    }

    public void setRecommendation(String recommendation) {
        this.recommendation = recommendation;
    }
}
