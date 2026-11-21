package com.yisy.modules.doramcu.earth2026.survival.crafting;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * JPA entity for Earth2026 crafting recipes.
 * <p>
 * A recipe defines what materials are needed, the difficulty, time required,
 * and what item is produced when crafting is completed.
 * </p>
 */
@Entity
@Table(name = "e26_crafting_recipes", indexes = {
        @Index(name = "idx_e26_recipe_name", columnList = "name", unique = true),
        @Index(name = "idx_e26_recipe_category", columnList = "category")
})
public class E26CraftingRecipe {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    /**
     * Recipe name (e.g., "Wooden Shelter", "Water Filter", "Bandage Kit").
     */
    @Column(nullable = false, length = 100, unique = true)
    private String name;

    /**
     * Recipe description.
     */
    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * Recipe category: SHELTER, TOOL, WEAPON, MEDICINE, FOOD, UTILITY.
     */
    @Column(nullable = false, length = 20)
    private String category;

    /**
     * Difficulty level (1-20). Higher requires better skills.
     */
    @Column(nullable = false)
    private int difficulty;

    /**
     * Time required in minutes.
     */
    @Column(name = "time_required", nullable = false)
    private int timeRequired;

    /**
     * Required materials as JSON (resource name -> quantity).
     */
    @Column(name = "materials_json", columnDefinition = "TEXT")
    private String materialsJson;

    /**
     * Required skill: STR, DEX, INT, WIS, or null for no specific skill.
     */
    @Column(name = "required_skill", length = 10)
    private String requiredSkill;

    /**
     * Result resource name.
     */
    @Column(name = "result_resource", nullable = false, length = 100)
    private String resultResource;

    /**
     * Quantity of the result resource produced.
     */
    @Column(name = "result_quantity", nullable = false)
    private int resultQuantity = 1;

    /**
     * Whether this recipe requires a workshop.
     */
    @Column(name = "requires_workshop", nullable = false)
    private boolean requiresWorkshop = false;

    /**
     * Creation timestamp.
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public E26CraftingRecipe() {
    }

    public E26CraftingRecipe(String name, String category) {
        this.name = name;
        this.category = category;
        this.createdAt = LocalDateTime.now();
    }

    // ---- Getters and Setters ----

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

    public int getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(int difficulty) {
        this.difficulty = difficulty;
    }

    public int getTimeRequired() {
        return timeRequired;
    }

    public void setTimeRequired(int timeRequired) {
        this.timeRequired = timeRequired;
    }

    public String getMaterialsJson() {
        return materialsJson;
    }

    public void setMaterialsJson(String materialsJson) {
        this.materialsJson = materialsJson;
    }

    public String getRequiredSkill() {
        return requiredSkill;
    }

    public void setRequiredSkill(String requiredSkill) {
        this.requiredSkill = requiredSkill;
    }

    public String getResultResource() {
        return resultResource;
    }

    public void setResultResource(String resultResource) {
        this.resultResource = resultResource;
    }

    public int getResultQuantity() {
        return resultQuantity;
    }

    public void setResultQuantity(int resultQuantity) {
        this.resultQuantity = resultQuantity;
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
}
