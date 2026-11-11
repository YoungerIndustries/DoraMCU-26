package com.yisy.modules.doramcu.dnd5e.dto;

import java.util.Map;

/**
 * DTO for updating a D&D 5e character's stats.
 * All fields are optional; only provided fields will be updated.
 */
public class UpdateDnd5eCharacterRequest {

    private Integer hitPoints;
    private Integer maxHitPoints;
    private Integer armorClass;
    private Integer speed;
    private Integer experiencePoints;
    private Integer tempHitPoints;
    private Integer exhaustionLevel;
    private Integer deathSaveSuccesses;
    private Integer deathSaveFailures;
    private Integer strScore;
    private Integer dexScore;
    private Integer conScore;
    private Integer intScore;
    private Integer wisScore;
    private Integer chaScore;
    private String equipment;
    private String conditions;
    private Boolean inspiration;

    public Integer getHitPoints() { return hitPoints; }
    public void setHitPoints(Integer hitPoints) { this.hitPoints = hitPoints; }

    public Integer getMaxHitPoints() { return maxHitPoints; }
    public void setMaxHitPoints(Integer maxHitPoints) { this.maxHitPoints = maxHitPoints; }

    public Integer getArmorClass() { return armorClass; }
    public void setArmorClass(Integer armorClass) { this.armorClass = armorClass; }

    public Integer getSpeed() { return speed; }
    public void setSpeed(Integer speed) { this.speed = speed; }

    public Integer getExperiencePoints() { return experiencePoints; }
    public void setExperiencePoints(Integer experiencePoints) { this.experiencePoints = experiencePoints; }

    public Integer getTempHitPoints() { return tempHitPoints; }
    public void setTempHitPoints(Integer tempHitPoints) { this.tempHitPoints = tempHitPoints; }

    public Integer getExhaustionLevel() { return exhaustionLevel; }
    public void setExhaustionLevel(Integer exhaustionLevel) { this.exhaustionLevel = exhaustionLevel; }

    public Integer getDeathSaveSuccesses() { return deathSaveSuccesses; }
    public void setDeathSaveSuccesses(Integer deathSaveSuccesses) { this.deathSaveSuccesses = deathSaveSuccesses; }

    public Integer getDeathSaveFailures() { return deathSaveFailures; }
    public void setDeathSaveFailures(Integer deathSaveFailures) { this.deathSaveFailures = deathSaveFailures; }

    public Integer getStrScore() { return strScore; }
    public void setStrScore(Integer strScore) { this.strScore = strScore; }

    public Integer getDexScore() { return dexScore; }
    public void setDexScore(Integer dexScore) { this.dexScore = dexScore; }

    public Integer getConScore() { return conScore; }
    public void setConScore(Integer conScore) { this.conScore = conScore; }

    public Integer getIntScore() { return intScore; }
    public void setIntScore(Integer intScore) { this.intScore = intScore; }

    public Integer getWisScore() { return wisScore; }
    public void setWisScore(Integer wisScore) { this.wisScore = wisScore; }

    public Integer getChaScore() { return chaScore; }
    public void setChaScore(Integer chaScore) { this.chaScore = chaScore; }

    public String getEquipment() { return equipment; }
    public void setEquipment(String equipment) { this.equipment = equipment; }

    public String getConditions() { return conditions; }
    public void setConditions(String conditions) { this.conditions = conditions; }

    public Boolean getInspiration() { return inspiration; }
    public void setInspiration(Boolean inspiration) { this.inspiration = inspiration; }
}
