package com.yisy.modules.doramcu.hex.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * JPA entity representing the cost and recipe data for a material in the production chain.
 * <p>
 * Covers raw resources, refined resources, and construction modules.
 * Each material has a base credit value and may have raw/refined inputs
 * that define how it is produced from upstream materials.
 * </p>
 * <p>
 * Key mechanics:
 * - Material Type: RAW_RESOURCE, REFINED_RESOURCE, CONSTRUCTION_MODULE
 * - Base Value: Credit value per unit
 * - Raw Inputs: JSON specifying raw resource quantities needed
 * - Refined Inputs: JSON specifying refined resource quantities needed
 * - Processing Time: Turns required to produce one batch
 * - Alternative Recipe: JSON for fallback recipe when primary inputs are scarce
 * </p>
 */
@Entity
@Table(name = "material_costs", indexes = {
        @Index(name = "idx_material_costs_type", columnList = "material_type"),
        @Index(name = "idx_material_costs_name", columnList = "material_name", unique = true)
})
public class MaterialCost {

    public enum MaterialCostType {
        RAW_RESOURCE("Raw Resource", "Extracted directly from celestial bodies"),
        REFINED_RESOURCE("Refined Resource", "Processed from raw resources"),
        CONSTRUCTION_MODULE("Construction Module", "Assembled from refined resources");

        private final String displayName;
        private final String description;

        MaterialCostType(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getDescription() {
            return description;
        }
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    /**
     * Type of material: raw resource, refined resource, or construction module.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "material_type", nullable = false, length = 50)
    private MaterialCostType materialType;

    /**
     * Name of the material (e.g., ORE, METAL_ALLOY, HULL_MODULE).
     */
    @Column(name = "material_name", nullable = false, length = 100, unique = true)
    private String materialName;

    /**
     * Base credit value per unit of this material.
     */
    @Column(name = "base_value_credits", nullable = false, precision = 15, scale = 2)
    private BigDecimal baseValueCredits;

    /**
     * JSON string specifying raw resource inputs for production.
     * Example: {"ORE": 2, "CRYSTAL": 1}
     */
    @Column(name = "raw_inputs_json", columnDefinition = "TEXT")
    private String rawInputsJson;

    /**
     * JSON string specifying refined resource inputs for production.
     * Example: {"METAL_ALLOY": 2, "COMPOSITE": 1}
     */
    @Column(name = "refined_inputs_json", columnDefinition = "TEXT")
    private String refinedInputsJson;

    /**
     * Number of turns required to process/assemble this material.
     */
    @Column(name = "processing_time_turns")
    private Integer processingTimeTurns;

    /**
     * JSON string specifying alternative recipe when primary inputs are scarce.
     * Example: {"ORE": 3}
     */
    @Column(name = "alternative_recipe_json", columnDefinition = "TEXT")
    private String alternativeRecipeJson;

    /**
     * Number of turns required for the alternative recipe.
     */
    @Column(name = "alternative_time_turns")
    private Integer alternativeTimeTurns;

    /**
     * When the record was created.
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * When the record was last updated.
     */
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

    public MaterialCostType getMaterialType() {
        return materialType;
    }

    public void setMaterialType(MaterialCostType materialType) {
        this.materialType = materialType;
    }

    public String getMaterialName() {
        return materialName;
    }

    public void setMaterialName(String materialName) {
        this.materialName = materialName;
    }

    public BigDecimal getBaseValueCredits() {
        return baseValueCredits;
    }

    public void setBaseValueCredits(BigDecimal baseValueCredits) {
        this.baseValueCredits = baseValueCredits;
    }

    public String getRawInputsJson() {
        return rawInputsJson;
    }

    public void setRawInputsJson(String rawInputsJson) {
        this.rawInputsJson = rawInputsJson;
    }

    public String getRefinedInputsJson() {
        return refinedInputsJson;
    }

    public void setRefinedInputsJson(String refinedInputsJson) {
        this.refinedInputsJson = refinedInputsJson;
    }

    public Integer getProcessingTimeTurns() {
        return processingTimeTurns;
    }

    public void setProcessingTimeTurns(Integer processingTimeTurns) {
        this.processingTimeTurns = processingTimeTurns;
    }

    public String getAlternativeRecipeJson() {
        return alternativeRecipeJson;
    }

    public void setAlternativeRecipeJson(String alternativeRecipeJson) {
        this.alternativeRecipeJson = alternativeRecipeJson;
    }

    public Integer getAlternativeTimeTurns() {
        return alternativeTimeTurns;
    }

    public void setAlternativeTimeTurns(Integer alternativeTimeTurns) {
        this.alternativeTimeTurns = alternativeTimeTurns;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    @Override
    public String toString() {
        return "MaterialCost{name='" + materialName + "', type=" + materialType +
                ", baseValue=" + baseValueCredits + "}";
    }
}
