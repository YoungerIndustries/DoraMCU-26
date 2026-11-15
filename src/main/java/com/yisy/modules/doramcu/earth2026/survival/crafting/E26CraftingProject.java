package com.yisy.modules.doramcu.earth2026.survival.crafting;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * JPA entity for in-progress crafting projects in Earth2026.
 * <p>
 * Tracks the progress of a crafting attempt, including the character, recipe,
 * current progress, and status.
 * </p>
 */
@Entity
@Table(name = "e26_crafting_projects", indexes = {
        @Index(name = "idx_e26_project_character", columnList = "characterId"),
        @Index(name = "idx_e26_project_status", columnList = "status")
})
public class E26CraftingProject {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    /**
     * Reference to the character crafting the item.
     */
    @Column(name = "character_id", nullable = false)
    private Long characterId;

    /**
     * Reference to the recipe being used.
     */
    @Column(name = "recipe_id", nullable = false)
    private Long recipeId;

    /**
     * Project status: IN_PROGRESS, COMPLETED, FAILED, CANCELLED.
     */
    @Column(nullable = false, length = 20)
    private String status = "IN_PROGRESS";

    /**
     * Current progress (0-100).
     */
    @Column(nullable = false)
    private int progress = 0;

    /**
     * Total progress required (usually equals timeRequired from recipe).
     */
    @Column(name = "total_progress", nullable = false)
    private int totalProgress;

    /**
     * Whether the skill check was passed.
     */
    @Column(name = "skill_check_passed")
    private Boolean skillCheckPassed;

    /**
     * Result resource name (set when completed).
     */
    @Column(name = "result_resource", length = 100)
    private String resultResource;

    /**
     * Result quantity (set when completed).
     */
    @Column(name = "result_quantity")
    private Integer resultQuantity;

    /**
     * When the project was created.
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * When the project was completed.
     */
    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    public E26CraftingProject() {
    }

    // ---- Getters and Setters ----

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCharacterId() {
        return characterId;
    }

    public void setCharacterId(Long characterId) {
        this.characterId = characterId;
    }

    public Long getRecipeId() {
        return recipeId;
    }

    public void setRecipeId(Long recipeId) {
        this.recipeId = recipeId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = Math.max(0, Math.min(totalProgress, progress));
    }

    public int getTotalProgress() {
        return totalProgress;
    }

    public void setTotalProgress(int totalProgress) {
        this.totalProgress = totalProgress;
    }

    public Boolean getSkillCheckPassed() {
        return skillCheckPassed;
    }

    public void setSkillCheckPassed(Boolean skillCheckPassed) {
        this.skillCheckPassed = skillCheckPassed;
    }

    public String getResultResource() {
        return resultResource;
    }

    public void setResultResource(String resultResource) {
        this.resultResource = resultResource;
    }

    public Integer getResultQuantity() {
        return resultQuantity;
    }

    public void setResultQuantity(Integer resultQuantity) {
        this.resultQuantity = resultQuantity;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    /**
     * Check if the project is complete.
     */
    public boolean isComplete() {
        return progress >= totalProgress;
    }
}
