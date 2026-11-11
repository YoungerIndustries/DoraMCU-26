package com.yisy.modules.doramcu.dnd5e.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * JPA entity representing an exhaustion event for a D&D 5e character.
 * <p>
 * In D&D 2024 PHB, exhaustion is tracked as a level from 0 to 6.
 * Each level imposes a penalty of 2 × exhaustion level on d20 tests,
 * and reduces speed by 5 × exhaustion level feet.
 * </p>
 * <p>
 * Exhaustion is gained from harsh environments, starvation, dehydration,
 * and other severe stresses. It is removed by long rests.
 * </p>
 */
@Entity
@Table(name = "dnd5e_exhaustion", indexes = {
        @Index(name = "idx_dnd5e_exhaustion_character", columnList = "character_id"),
        @Index(name = "idx_dnd5e_exhaustion_created", columnList = "created_at")
})
public class Dnd5eExhaustion {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "character_id", nullable = false)
    private Long characterId;

    @Column(nullable = false, length = 100)
    private String characterName;

    /**
     * The exhaustion level gained or removed in this event.
     * Positive values indicate gaining exhaustion; negative values indicate removing it.
     */
    @Column(name = "level_change", nullable = false)
    private int levelChange;

    /**
     * The exhaustion level after this event was applied.
     */
    @Column(name = "resulting_level", nullable = false)
    private int resultingLevel;

    /**
     * Source of the exhaustion change (e.g., "Starvation", "Long Rest", "Extreme Cold").
     */
    @Column(nullable = false, length = 100)
    private String source;

    /**
     * Optional description of the exhaustion event.
     */
    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
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

    public int getLevelChange() {
        return levelChange;
    }

    public void setLevelChange(int levelChange) {
        this.levelChange = levelChange;
    }

    public int getResultingLevel() {
        return resultingLevel;
    }

    public void setResultingLevel(int resultingLevel) {
        this.resultingLevel = resultingLevel;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
