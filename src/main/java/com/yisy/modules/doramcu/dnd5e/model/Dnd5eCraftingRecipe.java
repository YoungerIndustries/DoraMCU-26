package com.yisy.modules.doramcu.dnd5e.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * JPA entity representing a D&D 5e crafting recipe.
 * <p>
 * A recipe defines what can be crafted, including the required tool,
 * ability score, DC, materials, time, and the resulting item.
 * Based on the 2024 PHB crafting system (p.233).
 * </p>
 */
@Entity
@Table(name = "dnd5e_crafting_recipes", indexes = {
        @Index(name = "idx_dnd5e_crafting_recipe_category", columnList = "category"),
        @Index(name = "idx_dnd5e_crafting_recipe_tool", columnList = "required_tool"),
        @Index(name = "idx_dnd5e_crafting_recipe_rarity", columnList = "rarity")
})
public class Dnd5eCraftingRecipe {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, length = 50)
    private String category;

    @Column(nullable = false, length = 50)
    private String rarity = "Common";

    @Column(name = "required_tool", nullable = false, length = 100)
    private String requiredTool;

    @Column(name = "required_ability", nullable = false, length = 20)
    private String requiredAbility;

    @Column(name = "dc", nullable = false)
    private int dc;

    @Column(name = "gold_cost", nullable = false)
    private int goldCost;

    @Column(name = "hours_required", nullable = false)
    private int hoursRequired;

    @Column(name = "material_components", columnDefinition = "TEXT")
    private String materialComponents;

    @Column(name = "result_item_name", length = 200)
    private String resultItemName;

    @Column(name = "result_item_type", length = 50)
    private String resultItemType;

    @Column(name = "result_quantity", nullable = false)
    private int resultQuantity = 1;

    @Column(name = "is_magic_item", nullable = false)
    private boolean isMagicItem = false;

    @Column(name = "prerequisites", columnDefinition = "TEXT")
    private String prerequisites;

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

    public String getRarity() {
        return rarity;
    }

    public void setRarity(String rarity) {
        this.rarity = rarity;
    }

    public String getRequiredTool() {
        return requiredTool;
    }

    public void setRequiredTool(String requiredTool) {
        this.requiredTool = requiredTool;
    }

    public String getRequiredAbility() {
        return requiredAbility;
    }

    public void setRequiredAbility(String requiredAbility) {
        this.requiredAbility = requiredAbility;
    }

    public int getDc() {
        return dc;
    }

    public void setDc(int dc) {
        this.dc = dc;
    }

    public int getGoldCost() {
        return goldCost;
    }

    public void setGoldCost(int goldCost) {
        this.goldCost = goldCost;
    }

    public int getHoursRequired() {
        return hoursRequired;
    }

    public void setHoursRequired(int hoursRequired) {
        this.hoursRequired = hoursRequired;
    }

    public String getMaterialComponents() {
        return materialComponents;
    }

    public void setMaterialComponents(String materialComponents) {
        this.materialComponents = materialComponents;
    }

    public String getResultItemName() {
        return resultItemName;
    }

    public void setResultItemName(String resultItemName) {
        this.resultItemName = resultItemName;
    }

    public String getResultItemType() {
        return resultItemType;
    }

    public void setResultItemType(String resultItemType) {
        this.resultItemType = resultItemType;
    }

    public int getResultQuantity() {
        return resultQuantity;
    }

    public void setResultQuantity(int resultQuantity) {
        this.resultQuantity = resultQuantity;
    }

    public boolean isMagicItem() {
        return isMagicItem;
    }

    public void setMagicItem(boolean magicItem) {
        isMagicItem = magicItem;
    }

    public String getPrerequisites() {
        return prerequisites;
    }

    public void setPrerequisites(String prerequisites) {
        this.prerequisites = prerequisites;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
