package com.yisy.modules.doramcu.earth2026.survival;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * JPA entity tracking survival stats for an Earth2026 character.
 * <p>
 * Each character in survival mode has a SurvivalState that tracks hunger, thirst,
 * fatigue, body temperature, and exposure level. These stats decay over time
 * and are affected by environmental conditions, actions, and item usage.
 * </p>
 * <p>
 * Key mechanics:
 * - Hunger: 0-100, decays over time; at 0 causes starvation damage
 * - Thirst: 0-100, decays faster than hunger; at 0 causes dehydration damage
 * - Fatigue: 0-100, increases with activity; at 100 causes exhaustion
 * - Body Temperature: Celsius, affected by weather and shelter
 * - Exposure: 0-100, increases in extreme weather; at 100 causes critical damage
 * </p>
 */
@Entity
@Table(name = "survival_states", indexes = {
        @Index(name = "idx_survival_state_character", columnList = "characterId", unique = true),
        @Index(name = "idx_survival_state_active", columnList = "isActive")
})
public class SurvivalState {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    /**
     * Reference to the Earth2026Character.
     */
    @Column(name = "character_id", nullable = false, unique = true)
    private Long characterId;

    /**
     * Hunger level (0-100). 0 = starving, 100 = fully fed.
     */
    @Column(nullable = false)
    private int hunger = 100;

    /**
     * Thirst level (0-100). 0 = dehydrated, 100 = fully hydrated.
     */
    @Column(nullable = false)
    private int thirst = 100;

    /**
     * Fatigue level (0-100). 0 = fully rested, 100 = exhausted.
     */
    @Column(nullable = false)
    private int fatigue = 0;

    /**
     * Body temperature in Celsius. Normal range: 36.1-37.2°C.
     */
    @Column(name = "body_temperature", nullable = false)
    private double bodyTemperature = 37.0;

    /**
     * Exposure level (0-100). 0 = comfortable, 100 = critical exposure.
     */
    @Column(nullable = false)
    private int exposure = 0;

    /**
     * Whether the character is currently in survival mode.
     */
    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    /**
     * Last time the survival stats were processed.
     */
    @Column(name = "last_tick")
    private LocalDateTime lastTick;

    /**
     * Creation timestamp.
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Last update timestamp.
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public SurvivalState() {
    }

    public SurvivalState(Long characterId) {
        this.characterId = characterId;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // ---- Getters and Setters ----

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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
        this.hunger = Math.max(0, Math.min(100, hunger));
    }

    public int getThirst() {
        return thirst;
    }

    public void setThirst(int thirst) {
        this.thirst = Math.max(0, Math.min(100, thirst));
    }

    public int getFatigue() {
        return fatigue;
    }

    public void setFatigue(int fatigue) {
        this.fatigue = Math.max(0, Math.min(100, fatigue));
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
        this.exposure = Math.max(0, Math.min(100, exposure));
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    /**
     * Checks if the character is starving (hunger at 0).
     */
    public boolean isStarving() {
        return hunger <= 0;
    }

    /**
     * Checks if the character is dehydrated (thirst at 0).
     */
    public boolean isDehydrated() {
        return thirst <= 0;
    }

    /**
     * Checks if the character is exhausted (fatigue at 100).
     */
    public boolean isExhausted() {
        return fatigue >= 100;
    }

    /**
     * Checks if the character has hypothermia (body temperature below 35°C).
     */
    public boolean hasHypothermia() {
        return bodyTemperature < 35.0;
    }

    /**
     * Checks if the character has heat stroke (body temperature above 40°C).
     */
    public boolean hasHeatStroke() {
        return bodyTemperature > 40.0;
    }

    /**
     * Checks if the character has critical exposure.
     */
    public boolean hasCriticalExposure() {
        return exposure >= 100;
    }

    /**
     * Checks if the character is in critical condition from any survival stat.
     */
    public boolean isCritical() {
        return isStarving() || isDehydrated() || isExhausted()
                || hasHypothermia() || hasHeatStroke() || hasCriticalExposure();
    }
}
