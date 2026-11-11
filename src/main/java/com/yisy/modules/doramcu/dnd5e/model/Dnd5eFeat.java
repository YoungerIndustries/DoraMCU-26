package com.yisy.modules.doramcu.dnd5e.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * JPA entity representing a D&D 5e feat.
 * <p>
 * Feats are optional benefits that characters can select instead of
 * taking an ability score improvement. Based on the 2024 PHB feat system,
 * which revises several feats from the 2014 edition.
 * </p>
 * <p>
 * Key 2024 changes:
 * - Lucky: grants advantage instead of rerolls
 * - Sharpshooter/Great Weapon Master: -5/+10 removed
 * - Sentinel/Polearm Master combo addressed
 * - Magic Initiate: learned spells can use other spell slots
 * </p>
 */
@Entity
@Table(name = "dnd5e_feats", indexes = {
        @Index(name = "idx_dnd5e_feat_name", columnList = "name", unique = true),
        @Index(name = "idx_dnd5e_feat_category", columnList = "category")
})
public class Dnd5eFeat {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false, length = 100, unique = true)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * Category of the feat (e.g., "General", "Combat", "Magic", "Skill", "Defense").
     */
    @Column(nullable = false, length = 50)
    private String category;

    /**
     * Required ability score minimum (e.g., "STR 13", "DEX 13", "INT 13", "WIS 13", "CHA 13", or "None").
     */
    @Column(name = "ability_requirement", length = 20)
    private String abilityRequirement;

    /**
     * Required ability score value (0 if none).
     */
    @Column(name = "ability_score_required")
    private int abilityScoreRequired = 0;

    /**
     * Prerequisite feat or feature (e.g., "Magic Initiate" for Magic Initiate 2, or "None").
     */
    @Column(length = 100)
    private String prerequisite;

    /**
     * Whether this feat can be taken multiple times.
     */
    @Column(name = "repeatable", nullable = false)
    private boolean repeatable = false;

    /**
     * Ability score increase granted by this feat (0 if none).
     */
    @Column(name = "ability_increase")
    private int abilityIncrease = 0;

    /**
     * The ability that increases (e.g., "STR", "DEX", "CON", "INT", "WIS", "CHA", or "Any").
     */
    @Column(name = "ability_increased", length = 10)
    private String abilityIncreased;

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getAbilityRequirement() {
        return abilityRequirement;
    }

    public void setAbilityRequirement(String abilityRequirement) {
        this.abilityRequirement = abilityRequirement;
    }

    public int getAbilityScoreRequired() {
        return abilityScoreRequired;
    }

    public void setAbilityScoreRequired(int abilityScoreRequired) {
        this.abilityScoreRequired = abilityScoreRequired;
    }

    public String getPrerequisite() {
        return prerequisite;
    }

    public void setPrerequisite(String prerequisite) {
        this.prerequisite = prerequisite;
    }

    public boolean isRepeatable() {
        return repeatable;
    }

    public void setRepeatable(boolean repeatable) {
        this.repeatable = repeatable;
    }

    public int getAbilityIncrease() {
        return abilityIncrease;
    }

    public void setAbilityIncrease(int abilityIncrease) {
        this.abilityIncrease = abilityIncrease;
    }

    public String getAbilityIncreased() {
        return abilityIncreased;
    }

    public void setAbilityIncreased(String abilityIncreased) {
        this.abilityIncreased = abilityIncreased;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    @Override
    public String toString() {
        return "Dnd5eFeat{name='" + name + "', category='" + category + "'}";
    }
}
