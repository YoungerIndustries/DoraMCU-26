package com.yisy.modules.doramcu.dnd5e.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * JPA entity representing a Heroic Inspiration usage record.
 * <p>
 * Heroic Inspiration (2024 PHB) replaces the old Inspiration mechanic.
 * It lets a character reroll any die after rolling it — including damage,
 * HP on level-up, wild magic surges, and more. The character keeps the
 * higher of the two rolls.
 * </p>
 * <p>
 * Humans get one Heroic Inspiration after a long rest. Certain features
 * (e.g., Musician origin feat) award it after any rest.
 * </p>
 */
@Entity
@Table(name = "dnd5e_heroic_inspiration", indexes = {
        @Index(name = "idx_dnd5e_heroic_inspiration_character", columnList = "character_id"),
        @Index(name = "idx_dnd5e_heroic_inspiration_used", columnList = "is_used")
})
public class Dnd5eHeroicInspiration {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "character_id", nullable = false)
    private Long characterId;

    @Column(nullable = false, length = 100)
    private String characterName;

    /**
     * Number of Heroic Inspiration points currently available.
     */
    @Column(name = "points_available", nullable = false)
    private int pointsAvailable = 0;

    /**
     * Maximum number of Heroic Inspiration points this character can hold.
     * Default is 1 (Human race trait).
     */
    @Column(name = "max_points", nullable = false)
    private int maxPoints = 1;

    /**
     * Source of the Heroic Inspiration (e.g., "Human", "Musician feat").
     */
    @Column(nullable = false, length = 100)
    private String source;

    /**
     * When the Heroic Inspiration was last awarded.
     */
    @Column(name = "last_awarded_at")
    private LocalDateTime lastAwardedAt;

    /**
     * Description of the last reroll made with Heroic Inspiration.
     */
    @Column(name = "last_reroll_description", columnDefinition = "TEXT")
    private String lastRerollDescription;

    /**
     * The original roll value before reroll.
     */
    @Column(name = "last_original_roll")
    private Integer lastOriginalRoll;

    /**
     * The rerolled value.
     */
    @Column(name = "last_reroll_value")
    private Integer lastRerollValue;

    /**
     * Whether the reroll was used (kept the new value).
     */
    @Column(name = "is_used", nullable = false)
    private boolean isUsed = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // --- Getters and Setters ---

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

    public String getCharacterName() {
        return characterName;
    }

    public void setCharacterName(String characterName) {
        this.characterName = characterName;
    }

    public int getPointsAvailable() {
        return pointsAvailable;
    }

    public void setPointsAvailable(int pointsAvailable) {
        this.pointsAvailable = pointsAvailable;
    }

    public int getMaxPoints() {
        return maxPoints;
    }

    public void setMaxPoints(int maxPoints) {
        this.maxPoints = maxPoints;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public LocalDateTime getLastAwardedAt() {
        return lastAwardedAt;
    }

    public void setLastAwardedAt(LocalDateTime lastAwardedAt) {
        this.lastAwardedAt = lastAwardedAt;
    }

    public String getLastRerollDescription() {
        return lastRerollDescription;
    }

    public void setLastRerollDescription(String lastRerollDescription) {
        this.lastRerollDescription = lastRerollDescription;
    }

    public Integer getLastOriginalRoll() {
        return lastOriginalRoll;
    }

    public void setLastOriginalRoll(Integer lastOriginalRoll) {
        this.lastOriginalRoll = lastOriginalRoll;
    }

    public Integer getLastRerollValue() {
        return lastRerollValue;
    }

    public void setLastRerollValue(Integer lastRerollValue) {
        this.lastRerollValue = lastRerollValue;
    }

    public boolean isUsed() {
        return isUsed;
    }

    public void setUsed(boolean used) {
        isUsed = used;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
