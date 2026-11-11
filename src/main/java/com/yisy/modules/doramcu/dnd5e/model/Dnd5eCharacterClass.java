package com.yisy.modules.doramcu.dnd5e.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * JPA entity representing a character's class membership.
 * <p>
 * This entity links a {@link Dnd5eCharacter} to a {@link Dnd5eClass},
 * tracking the level in that class and the chosen subclass.
 * Supports multiclassing by allowing multiple entries per character.
 * </p>
 * <p>
 * Key mechanics:
 * - Level in Class: Tracks how many levels the character has in this specific class
 * - Subclass: The chosen subclass (if any) for this class
 * - Active State: Whether this class is currently active for the character
 * - Multiclassing: Characters can have multiple class entries
 * - Spellcaster Level: Effective spellcaster level for multiclass spellcasting
 * </p>
 */
@Entity
@Table(name = "dnd5e_character_classes", indexes = {
        @Index(name = "idx_dnd5e_char_class_character", columnList = "character_id"),
        @Index(name = "idx_dnd5e_char_class_name", columnList = "character_id, className")
})
public class Dnd5eCharacterClass {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "character_id", nullable = false)
    private Long characterId;

    @Column(nullable = false, length = 100)
    private String characterName;

    /**
     * The class name (e.g., "Fighter", "Wizard").
     */
    @Column(name = "className", nullable = false, length = 50)
    private String className;

    /**
     * Level in this specific class (1-20).
     */
    @Column(nullable = false)
    private int level = 1;

    /**
     * The chosen subclass, if any (e.g., "Champion", "School of Evocation").
     */
    @Column(length = 100)
    private String subclass;

    /**
     * Whether this class entry is currently active for the character.
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

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public String getSubclass() {
        return subclass;
    }

    public void setSubclass(String subclass) {
        this.subclass = subclass;
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
        return "Dnd5eCharacterClass{character='" + characterName + "', class='" + className + "', level=" + level + "}";
    }
}
