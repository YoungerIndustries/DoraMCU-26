package com.yisy.modules.doramcu.dnd5e.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * JPA entity representing a character's tool proficiency.
 * <p>
 * This entity links a {@link Dnd5eCharacter} to a crafting tool,
 * tracking when the proficiency was acquired and at what level.
 * In the 2024 PHB, tool proficiencies grant concrete mechanical effects
 * beyond just bonus to checks — including combat uses, exploration uses,
 * and a "Use" action that makes tools actively useful.
 * </p>
 */
@Entity
@Table(name = "dnd5e_character_tool_proficiencies", indexes = {
        @Index(name = "idx_dnd5e_char_tool_prof_char", columnList = "character_id"),
        @Index(name = "idx_dnd5e_char_tool_prof_tool", columnList = "character_id, tool_name")
})
public class Dnd5eCharacterToolProficiency {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "character_id", nullable = false)
    private Long characterId;

    @Column(nullable = false, length = 100)
    private String characterName;

    @Column(name = "tool_name", nullable = false, length = 100)
    private String toolName;

    /**
     * The character level when this proficiency was acquired.
     */
    @Column(name = "level_acquired", nullable = false)
    private int levelAcquired;

    /**
     * Whether this proficiency is currently active.
     */
    @Column(nullable = false)
    private boolean active = true;

    /**
     * Whether the character has expertise with this tool (double proficiency bonus).
     */
    @Column(nullable = false)
    private boolean expertise = false;

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

    public String getToolName() {
        return toolName;
    }

    public void setToolName(String toolName) {
        this.toolName = toolName;
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

    public boolean isExpertise() {
        return expertise;
    }

    public void setExpertise(boolean expertise) {
        this.expertise = expertise;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    @Override
    public String toString() {
        return "Dnd5eCharacterToolProficiency{character='" + characterName + "', tool='" + toolName +
                "', level=" + levelAcquired + ", expertise=" + expertise + "}";
    }
}
