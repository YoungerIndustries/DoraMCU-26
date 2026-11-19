package com.yisy.modules.doramcu.lord_of_the_rings.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * JPA entity representing a Background in the Lord of the Rings galaxy.
 * Backgrounds provide skill proficiencies and lore-based traits.
 */
@Entity
@Table(name = "lotr_backgrounds", indexes = {
        @Index(name = "idx_lotr_background_name", columnList = "name")
})
public class LotrBackground {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 100)
    private String skillProficiency1;

    @Column(length = 100)
    private String skillProficiency2;

    @Column(length = 100)
    private String toolProficiency;

    @Column(length = 50)
    private String language;

    @Column(name = "feature_name", length = 100)
    private String featureName;

    @Column(name = "feature_description", columnDefinition = "TEXT")
    private String featureDescription;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

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

    public String getSkillProficiency1() {
        return skillProficiency1;
    }

    public void setSkillProficiency1(String skillProficiency1) {
        this.skillProficiency1 = skillProficiency1;
    }

    public String getSkillProficiency2() {
        return skillProficiency2;
    }

    public void setSkillProficiency2(String skillProficiency2) {
        this.skillProficiency2 = skillProficiency2;
    }

    public String getToolProficiency() {
        return toolProficiency;
    }

    public void setToolProficiency(String toolProficiency) {
        this.toolProficiency = toolProficiency;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getFeatureName() {
        return featureName;
    }

    public void setFeatureName(String featureName) {
        this.featureName = featureName;
    }

    public String getFeatureDescription() {
        return featureDescription;
    }

    public void setFeatureDescription(String featureDescription) {
        this.featureDescription = featureDescription;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
