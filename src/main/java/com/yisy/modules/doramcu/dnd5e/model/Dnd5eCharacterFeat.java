package com.yisy.modules.doramcu.dnd5e.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * JPA entity representing a character's selection of a feat.
 * <p>
 * This entity links a {@link Dnd5eCharacter} to a {@link Dnd5eFeat},
 * tracking when the feat was acquired and at what level.
 * </p>
 */
@Entity
@Table(name = "dnd5e_character_feats", indexes = {
        @Index(name = "idx_dnd5e_character_feat_character", columnList = "character_id"),
        @Index(name = "idx_dnd5e_character_feat_name", columnList = "character_id, feat_name")
})
public class Dnd5eCharacterFeat {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "character_id", nullable = false)
    private Long characterId;

    @Column(nullable = false, length = 100)
    private String characterName;

    @Column(name = "feat_name", nullable = false, length = 100)
    private String featName;

    /**
     * The character level when this feat was acquired.
     */
    @Column(name = "level_acquired", nullable = false)
    private int levelAcquired;

    /**
     * Whether this feat is currently active (can be toggled off for certain feats).
     */
    @Column(nullable = false)
    private boolean active = true;

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

    public String getFeatName() {
        return featName;
    }

    public void setFeatName(String featName) {
        this.featName = featName;
    }

    public int getLevelAcquired() {
        return levelAcquired;
    }

    public void setLevelAcquired(int levelAcquired) {
        this.levelAcquired = levelAcquired;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    @Override
    public String toString() {
        return "Dnd5eCharacterFeat{character='" + characterName + "', feat='" + featName + "', level=" + levelAcquired + "}";
    }
}
