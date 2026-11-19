package com.yisy.modules.doramcu.lord_of_the_rings.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * JPA entity representing a Species (race) in the Lord of the Rings galaxy.
 */
@Entity
@Table(name = "lotr_species", indexes = {
        @Index(name = "idx_lotr_species_name", columnList = "name")
})
public class LotrSpecies {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "size", length = 20)
    private String size;

    @Column(name = "speed", nullable = false)
    private int speed = 30;

    @Column(name = "darkvision", nullable = false)
    private boolean darkvision = false;

    @Column(name = "age_modifier", length = 100)
    private String ageModifier;

    @Column(name = "height_modifier", length = 100)
    private String heightModifier;

    @Column(name = "weight_modifier", length = 100)
    private String weightModifier;

    @Column(name = "ability_increase", length = 100)
    private String abilityIncrease;

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

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public int getSpeed() {
        return speed;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    public boolean isDarkvision() {
        return darkvision;
    }

    public void setDarkvision(boolean darkvision) {
        this.darkvision = darkvision;
    }

    public String getAgeModifier() {
        return ageModifier;
    }

    public void setAgeModifier(String ageModifier) {
        this.ageModifier = ageModifier;
    }

    public String getHeightModifier() {
        return heightModifier;
    }

    public void setHeightModifier(String heightModifier) {
        this.heightModifier = heightModifier;
    }

    public String getWeightModifier() {
        return weightModifier;
    }

    public void setWeightModifier(String weightModifier) {
        this.weightModifier = weightModifier;
    }

    public String getAbilityIncrease() {
        return abilityIncrease;
    }

    public void setAbilityIncrease(String abilityIncrease) {
        this.abilityIncrease = abilityIncrease;
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
