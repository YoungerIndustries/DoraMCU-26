package com.yisy.modules.doramcu.earth2026.survival.dto;

import java.time.LocalDateTime;

/**
 * DTO representing the current survival status of a character.
 */
public class SurvivalStatusDto {

    private Long characterId;
    private int hunger;
    private int thirst;
    private int fatigue;
    private double bodyTemperature;
    private int exposure;
    private boolean isStarving;
    private boolean isDehydrated;
    private boolean isExhausted;
    private boolean hasHypothermia;
    private boolean hasHeatStroke;
    private boolean hasCriticalExposure;
    private boolean isCritical;
    private boolean isActive;
    private LocalDateTime lastTick;

    public SurvivalStatusDto() {
    }

    public Long getCharacterId() {
        return characterId;
    }

    public void setCharacterId(Long characterId) {
        this.characterId = characterId;
    }

    public int getHunger() {
        return hunger;
    }

    public void setHunger(int hunger) {
        this.hunger = hunger;
    }

    public int getThirst() {
        return thirst;
    }

    public void setThirst(int thirst) {
        this.thirst = thirst;
    }

    public int getFatigue() {
        return fatigue;
    }

    public void setFatigue(int fatigue) {
        this.fatigue = fatigue;
    }

    public double getBodyTemperature() {
        return bodyTemperature;
    }

    public void setBodyTemperature(double bodyTemperature) {
        this.bodyTemperature = bodyTemperature;
    }

    public int getExposure() {
        return exposure;
    }

    public void setExposure(int exposure) {
        this.exposure = exposure;
    }

    public boolean isStarving() {
        return isStarving;
    }

    public void setStarving(boolean starving) {
        isStarving = starving;
    }

    public boolean isDehydrated() {
        return isDehydrated;
    }

    public void setDehydrated(boolean dehydrated) {
        isDehydrated = dehydrated;
    }

    public boolean isExhausted() {
        return isExhausted;
    }

    public void setExhausted(boolean exhausted) {
        isExhausted = exhausted;
    }

    public boolean hasHypothermia() {
        return hasHypothermia;
    }

    public void setHypothermia(boolean hypothermia) {
        hasHypothermia = hypothermia;
    }

    public boolean hasHeatStroke() {
        return hasHeatStroke;
    }

    public void setHeatStroke(boolean heatStroke) {
        hasHeatStroke = heatStroke;
    }

    public boolean hasCriticalExposure() {
        return hasCriticalExposure;
    }

    public void setCriticalExposure(boolean criticalExposure) {
        hasCriticalExposure = criticalExposure;
    }

    public boolean isCritical() {
        return isCritical;
    }

    public void setCritical(boolean critical) {
        isCritical = critical;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public LocalDateTime getLastTick() {
        return lastTick;
    }

    public void setLastTick(LocalDateTime lastTick) {
        this.lastTick = lastTick;
    }
}
