package com.yisy.modules.doramcu.dnd5e.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * JPA entity representing a D&D 5e Background (2024 PHB).
 * <p>
 * Backgrounds represent a character's origins and upbringing, providing
 * roleplaying hooks and mechanical benefits. The 2024 PHB revised backgrounds
 * to include feature slots that allow more customization.
 * </p>
 * <p>
 * Key mechanics:
 * - Skill Proficiencies: Each background grants proficiency in specific skills
 * - Tool Proficiencies: Some backgrounds grant tool or language proficiencies
 * - Feature Slots: 2024 PHB backgrounds have feature slots that can be filled
 *   with different options, allowing customization within the background theme
 * - Equipment: Starting equipment provided by the background
 * - Feature: The main mechanical benefit of the background
 * </p>
 */
@Entity
@Table(name = "dnd5e_backgrounds", indexes = {
        @Index(name = "idx_dnd5e_background_name", columnList = "name", unique = true),
        @Index(name = "idx_dnd5e_background_category", columnList = "category")
})
public class Dnd5eBackground {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    /**
     * Name of the background (e.g., "Acolyte", "Criminal", "Folk Hero").
     */
    @Column(nullable = false, length = 100, unique = true)
    private String name;

    /**
     * Full description of the background's lore and roleplaying guidance.
     */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    /**
     * Category of the background (e.g., "Common", "Rare", "Exotic").
     */
    @Column(nullable = false, length = 30)
    private String category;

    /**
     * Skill proficiencies granted by this background (comma-separated, e.g., "Insight, Religion").
     */
    @Column(name = "skill_proficiencies", nullable = false, length = 200)
    private String skillProficiencies;

    /**
     * Tool or language proficiencies granted by this background (comma-separated, e.g., "Herbalism kit, Celestial").
     */
    @Column(name = "tool_proficiencies", length = 200)
    private String toolProficiencies;

    /**
     * Feature slots available for this background (2024 PHB mechanic).
     * Each slot can be filled with one of several options.
     * Format: "Slot1: OptionA, OptionB | Slot2: OptionC, OptionD"
     */
    @Column(name = "feature_slots", columnDefinition = "TEXT")
    private String featureSlots;

    /**
     * Main feature name of the background (e.g., "Sheltered", "Criminal Contact").
     */
    @Column(nullable = false, length = 100)
    private String feature;

    /**
     * Description of the main feature's mechanical benefit.
     */
    @Column(name = "feature_description", columnDefinition = "TEXT")
    private String featureDescription;

    /**
     * Starting equipment provided by this background (comma-separated, e.g., "Holy symbol, Prayer book, 5 sheets of parchment").
     */
    @Column(name = "equipment", columnDefinition = "TEXT")
    private String equipment;

    /**
     * Whether this background is active and available for use.
     */
    @Column(nullable = false)
    private boolean active = true;

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

    public String getSkillProficiencies() {
        return skillProficiencies;
    }

    public void setSkillProficiencies(String skillProficiencies) {
        this.skillProficiencies = skillProficiencies;
    }

    public String getToolProficiencies() {
        return toolProficiencies;
    }

    public void setToolProficiencies(String toolProficiencies) {
        this.toolProficiencies = toolProficiencies;
    }

    public String getFeatureSlots() {
        return featureSlots;
    }

    public void setFeatureSlots(String featureSlots) {
        this.featureSlots = featureSlots;
    }

    public String getFeature() {
        return feature;
    }

    public void setFeature(String feature) {
        this.feature = feature;
    }

    public String getFeatureDescription() {
        return featureDescription;
    }

    public void setFeatureDescription(String featureDescription) {
        this.featureDescription = featureDescription;
    }

    public String getEquipment() {
        return equipment;
    }

    public void setEquipment(String equipment) {
        this.equipment = equipment;
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

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    @Override
    public String toString() {
        return "Dnd5eBackground{name='" + name + "', category='" + category + "'}";
    }
}
