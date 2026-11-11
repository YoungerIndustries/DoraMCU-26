package com.yisy.modules.doramcu.dnd5e.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * JPA entity representing a D&D 5e Class Feature (2024 PHB).
 * <p>
 * Class features are abilities granted at specific levels within a class.
 * Each class has a progression of features from level 1 to 20.
 * </p>
 * <p>
 * Key mechanics:
 * - Level: The character level at which this feature is gained
 * - Feature Name: The name of the feature (e.g., "Second Wind", "Rage")
 * - Description: The mechanical effect of the feature
 * - Class: The class this feature belongs to
 * - Subclass: If applicable, the subclass that grants this feature (null for base class)
 * - Reusable on Short Rest: Whether the feature resets on a short rest
 * - Uses per Long Rest: Number of uses before requiring a long rest (0 if unlimited)
 * </p>
 */
@Entity
@Table(name = "dnd5e_class_features", indexes = {
        @Index(name = "idx_dnd5e_class_feature_class", columnList = "className"),
        @Index(name = "idx_dnd5e_class_feature_level", columnList = "className, level"),
        @Index(name = "idx_dnd5e_class_feature_subclass", columnList = "subclass")
})
public class Dnd5eClassFeature {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    /**
     * The class this feature belongs to (e.g., "Barbarian", "Fighter").
     */
    @Column(name = "className", nullable = false, length = 50)
    private String className;

    /**
     * The subclass this feature belongs to, if any (e.g., "Path of the Berserker").
     * Null for base class features.
     */
    @Column(length = 100)
    private String subclass;

    /**
     * The level at which this feature is gained (1-20).
     */
    @Column(nullable = false)
    private int level;

    /**
     * Name of the feature (e.g., "Rage", "Unarmored Defense", "Spell Mastery").
     */
    @Column(nullable = false, length = 100)
    private String name;

    /**
     * Full description of the feature's mechanical effects.
     */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    /**
     * Whether this feature's uses reset on a short rest.
     */
    @Column(name = "shortRestReset", nullable = false)
    private boolean shortRestReset = false;

    /**
     * Number of uses per long rest (0 if unlimited).
     */
    @Column(name = "usesPerLongRest")
    private int usesPerLongRest = 0;

    /**
     * Whether this feature is active and available.
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

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getSubclass() {
        return subclass;
    }

    public void setSubclass(String subclass) {
        this.subclass = subclass;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
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

    public boolean isShortRestReset() {
        return shortRestReset;
    }

    public void setShortRestReset(boolean shortRestReset) {
        this.shortRestReset = shortRestReset;
    }

    public int getUsesPerLongRest() {
        return usesPerLongRest;
    }

    public void setUsesPerLongRest(int usesPerLongRest) {
        this.usesPerLongRest = usesPerLongRest;
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
        return "Dnd5eClassFeature{class='" + className + "', level=" + level + ", name='" + name + "'}";
    }
}
