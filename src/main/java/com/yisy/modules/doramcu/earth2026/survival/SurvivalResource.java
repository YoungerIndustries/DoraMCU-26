package com.yisy.modules.doramcu.earth2026.survival;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * JPA entity for survival resources in the Earth2026 world.
 * <p>
 * Resources include food, water, materials, medicine, and tools that characters
 * can gather, craft, and use for survival. Each resource has a category, rarity,
 * and effects on survival stats.
 * </p>
 */
@Entity
@Table(name = "survival_resources", indexes = {
        @Index(name = "idx_survival_resource_name", columnList = "name", unique = true),
        @Index(name = "idx_survival_resource_category", columnList = "category"),
        @Index(name = "idx_survival_resource_rarity", columnList = "rarity")
})
public class SurvivalResource {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    /**
     * Resource name (e.g., "Canned Beans", "Water Bottle", "Wood Plank").
     */
    @Column(nullable = false, length = 100, unique = true)
    private String name;

    /**
     * Resource description.
     */
    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * Resource category: FOOD, WATER, MATERIAL, MEDICINE, TOOL, WEAPON, SHELTER.
     */
    @Column(nullable = false, length = 20)
    private String category;

    /**
     * Resource rarity: COMMON, UNCOMMON, RARE, VERY_RARE.
     */
    @Column(nullable = false, length = 20)
    private String rarity = "COMMON";

    /**
     * Hunger restoration value (0-100).
     */
    @Column(name = "hunger_value", nullable = false)
    private int hungerValue = 0;

    /**
     * Thirst restoration value (0-100).
     */
    @Column(name = "thirst_value", nullable = false)
    private int thirstValue = 0;

    /**
     * Fatigue reduction value (0-100).
     */
    @Column(name = "fatigue_reduction", nullable = false)
    private int fatigueReduction = 0;

    /**
     * Health restoration value.
     */
    @Column(name = "health_value", nullable = false)
    private int healthValue = 0;

    /**
     * Exposure reduction value (0-100).
     */
    @Column(name = "exposure_reduction", nullable = false)
    private int exposureReduction = 0;

    /**
     * Body temperature adjustment in Celsius (positive = warming, negative = cooling).
     */
    @Column(name = "temperature_adjustment", nullable = false)
    private double temperatureAdjustment = 0.0;

    /**
     * Weight of a single unit in kg.
     */
    @Column(nullable = false)
    private double weight = 0.1;

    /**
     * Whether this resource is consumable (food, water, medicine).
     */
    @Column(nullable = false)
    private boolean consumable = false;

    /**
     * Whether this resource is a crafting material.
     */
    @Column(name = "is_material", nullable = false)
    private boolean isMaterial = false;

    /**
     * Whether this resource is a tool.
     */
    @Column(name = "is_tool", nullable = false)
    private boolean isTool = false;

    /**
     * Creation timestamp.
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public SurvivalResource() {
    }

    public SurvivalResource(String name, String category) {
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

    public String getRarity() {
        return rarity;
    }

    public void setRarity(String rarity) {
        this.rarity = rarity;
    }

    public int getHungerValue() {
        return hungerValue;
    }

    public void setHungerValue(int hungerValue) {
        this.hungerValue = hungerValue;
    }

    public int getThirstValue() {
        return thirstValue;
    }

    public void setThirstValue(int thirstValue) {
        this.thirstValue = thirstValue;
    }

    public int getFatigueReduction() {
        return fatigueReduction;
    }

    public void setFatigueReduction(int fatigueReduction) {
        this.fatigueReduction = fatigueReduction;
    }

    public int getHealthValue() {
        return healthValue;
    }

    public void setHealthValue(int healthValue) {
        this.healthValue = healthValue;
    }

    public int getExposureReduction() {
        return exposureReduction;
    }

    public void setExposureReduction(int exposureReduction) {
        this.exposureReduction = exposureReduction;
    }

    public double getTemperatureAdjustment() {
        return temperatureAdjustment;
    }

    public void setTemperatureAdjustment(double temperatureAdjustment) {
        this.temperatureAdjustment = temperatureAdjustment;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public boolean isConsumable() {
        return consumable;
    }

    public void setConsumable(boolean consumable) {
        this.consumable = consumable;
    }

    public boolean isMaterial() {
        return isMaterial;
    }

    public void setMaterial(boolean material) {
        isMaterial = material;
    }

    public boolean isTool() {
        return isTool;
    }

    public void setTool(boolean tool) {
        isTool = tool;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
