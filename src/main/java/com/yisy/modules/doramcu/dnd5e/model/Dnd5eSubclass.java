package com.yisy.modules.doramcu.dnd5e.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * JPA entity representing a D&D 5e Subclass (2024 PHB).
 * <p>
 * Subclasses are specializations chosen at level 2 (or later for some classes)
 * that define a character's unique path within their class.
 * </p>
 * <p>
 * Key mechanics:
 * - Each class has 2-4 subclasses available
 * - Subclasses grant features at specific levels (typically 3, 6, 10, 14, 18)
 * - Subclass features are in addition to base class features
 * - Some classes have multiple subclass choices (e.g., Wizard has 10+ schools)
 * </p>
 */
@Entity
@Table(name = "dnd5e_subclasses", indexes = {
        @Index(name = "idx_dnd5e_subclass_name", columnList = "name", unique = true),
        @Index(name = "idx_dnd5e_subclass_class", columnList = "className")
})
public class Dnd5eSubclass {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    /**
     * Name of the subclass (e.g., "Path of the Berserker", "School of Evocation").
     */
    @Column(nullable = false, length = 100, unique = true)
    private String name;

    /**
     * The class this subclass belongs to (e.g., "Barbarian", "Wizard").
     */
    @Column(name = "className", nullable = false, length = 50)
    private String className;

    /**
     * Full description of the subclass's theme and roleplaying guidance.
     */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    /**
     * Level at which this subclass is chosen (typically 2, but varies).
     */
    @Column(name = "chosenLevel", nullable = false)
    private int chosenLevel = 2;

    /**
     * Feature names granted by this subclass at each level, pipe-separated.
     * Format: "Level3: Feature Name | Level6: Feature Name | Level10: Feature Name"
     */
    @Column(name = "featureSummary", columnDefinition = "TEXT")
    private String featureSummary;

    /**
     * Whether this subclass is active and available.
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

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getChosenLevel() {
        return chosenLevel;
    }

    public void setChosenLevel(int chosenLevel) {
        this.chosenLevel = chosenLevel;
    }

    public String getFeatureSummary() {
        return featureSummary;
    }

    public void setFeatureSummary(String featureSummary) {
        this.featureSummary = featureSummary;
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
        return "Dnd5eSubclass{name='" + name + "', class='" + className + "'}";
    }
}
