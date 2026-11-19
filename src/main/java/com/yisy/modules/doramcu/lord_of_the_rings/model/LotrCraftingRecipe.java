package com.yisy.modules.doramcu.lord_of_the_rings.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * JPA entity representing a Lord of the Rings crafting recipe.
 * <p>
 * Crafting in LOTR includes forging weapons (Andúril), creating armor (mithril coats),
 * brewing potions, and enchanting items.
 * </p>
 */
@Entity
@Table(name = "lotr_crafting_recipes", indexes = {
        @Index(name = "idx_lotr_crafting_name", columnList = "name")
})
public class LotrCraftingRecipe {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "result_item", nullable = false, length = 100)
    private String resultItem;

    @Column(name = "crafting_time", nullable = false)
    private int craftingTime;

    @Column(name = "skill_required", length = 50)
    private String skillRequired;

    @Column(name = "skill_dc", nullable = false)
    private int skillDc;

    @Column(name = "materials", columnDefinition = "TEXT")
    private String materials;

    @Column(name = "gold_cost", nullable = false)
    private int goldCost = 0;

    @Column(name = "requires_workshop", nullable = false)
    private boolean requiresWorkshop = false;

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

    public String getResultItem() {
        return resultItem;
    }

    public void setResultItem(String resultItem) {
        this.resultItem = resultItem;
    }

    public int getCraftingTime() {
        return craftingTime;
    }

    public void setCraftingTime(int craftingTime) {
        this.craftingTime = craftingTime;
    }

    public String getSkillRequired() {
        return skillRequired;
    }

    public void setSkillRequired(String skillRequired) {
        this.skillRequired = skillRequired;
    }

    public int getSkillDc() {
        return skillDc;
    }

    public void setSkillDc(int skillDc) {
        this.skillDc = skillDc;
    }

    public String getMaterials() {
        return materials;
    }

    public void setMaterials(String materials) {
        this.materials = materials;
    }

    public int getGoldCost() {
        return goldCost;
    }

    public void setGoldCost(int goldCost) {
        this.goldCost = goldCost;
    }

    public boolean isRequiresWorkshop() {
        return requiresWorkshop;
    }

    public void setRequiresWorkshop(boolean requiresWorkshop) {
        this.requiresWorkshop = requiresWorkshop;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "LotrCraftingRecipe{name='" + name + "', result='" + resultItem + "'}";
    }
}
