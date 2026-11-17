package com.yisy.modules.doramcu.model.registry;

import jakarta.persistence.*;

/**
 * JPA entity linking processing recipes to their raw resource inputs.
 */
@Entity
@Table(name = "processing_recipe_input", indexes = {
        @Index(name = "idx_pri_recipe", columnList = "recipe_id"),
        @Index(name = "idx_pri_resource", columnList = "raw_resource_type")
})
public class ProcessingRecipeInputEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "recipe_id", nullable = false)
    private long recipeId;

    @Column(name = "raw_resource_type", nullable = false, length = 50)
    private String rawResourceType;

    @Column(name = "required_amount", nullable = false)
    private int requiredAmount;

    public ProcessingRecipeInputEntity() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public long getRecipeId() {
        return recipeId;
    }

    public void setRecipeId(long recipeId) {
        this.recipeId = recipeId;
    }

    public String getRawResourceType() {
        return rawResourceType;
    }

    public void setRawResourceType(String rawResourceType) {
        this.rawResourceType = rawResourceType;
    }

    public int getRequiredAmount() {
        return requiredAmount;
    }

    public void setRequiredAmount(int requiredAmount) {
        this.requiredAmount = requiredAmount;
    }
}
