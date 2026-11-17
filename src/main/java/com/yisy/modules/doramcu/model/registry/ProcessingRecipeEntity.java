package com.yisy.modules.doramcu.model.registry;

import jakarta.persistence.*;

/**
 * JPA entity for processing recipes that convert raw resources into refined resources.
 */
@Entity
@Table(name = "processing_recipe", indexes = {
        @Index(name = "idx_pr_output", columnList = "output_resource"),
        @Index(name = "idx_pr_variant", columnList = "recipe_variant")
})
public class ProcessingRecipeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "output_resource", nullable = false, length = 50)
    private String outputResource;

    @Column(name = "output_amount", nullable = false)
    private int outputAmount;

    @Column(name = "recipe_variant", length = 20)
    private String recipeVariant = "STANDARD";

    @Column(name = "processing_time_turns", nullable = false)
    private int processingTimeTurns;

    public ProcessingRecipeEntity() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOutputResource() {
        return outputResource;
    }

    public void setOutputResource(String outputResource) {
        this.outputResource = outputResource;
    }

    public int getOutputAmount() {
        return outputAmount;
    }

    public void setOutputAmount(int outputAmount) {
        this.outputAmount = outputAmount;
    }

    public String getRecipeVariant() {
        return recipeVariant;
    }

    public void setRecipeVariant(String recipeVariant) {
        this.recipeVariant = recipeVariant;
    }

    public int getProcessingTimeTurns() {
        return processingTimeTurns;
    }

    public void setProcessingTimeTurns(int processingTimeTurns) {
        this.processingTimeTurns = processingTimeTurns;
    }
}
