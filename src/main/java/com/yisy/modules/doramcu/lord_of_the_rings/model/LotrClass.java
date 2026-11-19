package com.yisy.modules.doramcu.lord_of_the_rings.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * JPA entity representing a Class (Calling) in the Lord of the Rings galaxy.
 */
@Entity
@Table(name = "lotr_classes", indexes = {
        @Index(name = "idx_lotr_class_name", columnList = "name")
})
public class LotrClass {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "hit_dice", length = 20)
    private String hitDice;

    @Column(name = "primary_ability", length = 50)
    private String primaryAbility;

    @Column(name = "proficiency_1", length = 50)
    private String proficiency1;

    @Column(name = "proficiency_2", length = 50)
    private String proficiency2;

    @Column(name = "is_caster", nullable = false)
    private boolean isCaster = false;

    @Column(name = "caster_ability", length = 50)
    private String casterAbility;

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

    public String getHitDice() {
        return hitDice;
    }

    public void setHitDice(String hitDice) {
        this.hitDice = hitDice;
    }

    public String getPrimaryAbility() {
        return primaryAbility;
    }

    public void setPrimaryAbility(String primaryAbility) {
        this.primaryAbility = primaryAbility;
    }

    public String getProficiency1() {
        return proficiency1;
    }

    public void setProficiency1(String proficiency1) {
        this.proficiency1 = proficiency1;
    }

    public String getProficiency2() {
        return proficiency2;
    }

    public void setProficiency2(String proficiency2) {
        this.proficiency2 = proficiency2;
    }

    public boolean isCaster() {
        return isCaster;
    }

    public void setCaster(boolean caster) {
        isCaster = caster;
    }

    public String getCasterAbility() {
        return casterAbility;
    }

    public void setCasterAbility(String casterAbility) {
        this.casterAbility = casterAbility;
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
