package com.yisy.modules.doramcu.dnd5e.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * JPA entity representing a character's selected Background.
 * <p>
 * This entity links a {@link Dnd5eCharacter} to a {@link Dnd5eBackground},
 * tracking the selected feature slot options and active state.
 * </p>
 * <p>
 * Key mechanics:
 * - Feature Slot Selections: 2024 PHB backgrounds have feature slots;
 *   this tracks which options the character selected for each slot
 * - Active State: Whether the background is currently active for the character
 * </p>
 */
@Entity
@Table(name = "dnd5e_character_backgrounds", indexes = {
        @Index(name = "idx_dnd5e_char_background_character", columnList = "character_id"),
        @Index(name = "idx_dnd5e_char_background_name", columnList = "character_id, background_name")
})
public class Dnd5eCharacterBackground {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "character_id", nullable = false)
    private Long characterId;

    @Column(nullable = false, length = 100)
    private String characterName;

    @Column(name = "background_name", nullable = false, length = 100)
    private String backgroundName;

    /**
     * Selected feature slot options, comma-separated (e.g., "Sheltered, Criminal Contact").
     * Corresponds to the feature slots defined in the background.
     */
    @Column(name = "feature_selections", columnDefinition = "TEXT")
    private String featureSelections;

    /**
     * Whether this background is currently active for the character.
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

    public String getBackgroundName() {
        return backgroundName;
    }

    public void setBackgroundName(String backgroundName) {
        this.backgroundName = backgroundName;
    }

    public String getFeatureSelections() {
        return featureSelections;
    }

    public void setFeatureSelections(String featureSelections) {
        this.featureSelections = featureSelections;
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
        return "Dnd5eCharacterBackground{character='" + characterName + "', background='" + backgroundName + "'}";
    }
}
