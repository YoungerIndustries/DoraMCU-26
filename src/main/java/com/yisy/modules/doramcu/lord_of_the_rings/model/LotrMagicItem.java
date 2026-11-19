package com.yisy.modules.doramcu.lord_of_the_rings.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * JPA entity representing a Magic Item in the Lord of the Rings galaxy.
 */
@Entity
@Table(name = "lotr_magic_items", indexes = {
        @Index(name = "idx_lotr_magic_item_name", columnList = "name")
})
public class LotrMagicItem {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 50)
    private String itemType;

    @Column(length = 50)
    private String rarity;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "property_ac")
    private Integer propertyAc;

    @Column(name = "property_damage")
    private String propertyDamage;

    @Column(name = "property_range")
    private String propertyRange;

    @Column(name = "property_healing")
    private String propertyHealing;

    @Column(nullable = false)
    private int charges = 0;

    @Column(name = "max_charges", nullable = false)
    private int maxCharges = 0;

    @Column(name = "requires_attunement", nullable = false)
    private boolean requiresAttunement = false;

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

    public String getItemType() {
        return itemType;
    }

    public void setItemType(String itemType) {
        this.itemType = itemType;
    }

    public String getRarity() {
        return rarity;
    }

    public void setRarity(String rarity) {
        this.rarity = rarity;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getPropertyAc() {
        return propertyAc;
    }

    public void setPropertyAc(Integer propertyAc) {
        this.propertyAc = propertyAc;
    }

    public String getPropertyDamage() {
        return propertyDamage;
    }

    public void setPropertyDamage(String propertyDamage) {
        this.propertyDamage = propertyDamage;
    }

    public String getPropertyRange() {
        return propertyRange;
    }

    public void setPropertyRange(String propertyRange) {
        this.propertyRange = propertyRange;
    }

    public String getPropertyHealing() {
        return propertyHealing;
    }

    public void setPropertyHealing(String propertyHealing) {
        this.propertyHealing = propertyHealing;
    }

    public int getCharges() {
        return charges;
    }

    public void setCharges(int charges) {
        this.charges = charges;
    }

    public int getMaxCharges() {
        return maxCharges;
    }

    public void setMaxCharges(int maxCharges) {
        this.maxCharges = maxCharges;
    }

    public boolean isRequiresAttunement() {
        return requiresAttunement;
    }

    public void setRequiresAttunement(boolean requiresAttunement) {
        this.requiresAttunement = requiresAttunement;
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
