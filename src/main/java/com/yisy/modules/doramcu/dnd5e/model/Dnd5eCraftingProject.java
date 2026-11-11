package com.yisy.modules.doramcu.dnd5e.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * JPA entity representing an active D&D 5e crafting project.
 * <p>
 * A crafting project tracks the progress of a character crafting an item,
 * including hours worked, check results, and completion status.
 * Based on the 2024 PHB crafting system (p.233).
 * </p>
 */
@Entity
@Table(name = "dnd5e_crafting_projects", indexes = {
        @Index(name = "idx_dnd5e_crafting_project_character", columnList = "character_id"),
        @Index(name = "idx_dnd5e_crafting_project_recipe", columnList = "recipe_id"),
        @Index(name = "idx_dnd5e_crafting_project_status", columnList = "status")
})
public class Dnd5eCraftingProject {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "character_id", nullable = false)
    private Long characterId;

    @Column(name = "recipe_id", nullable = false)
    private Long recipeId;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(name = "status", nullable = false, length = 20)
    private String status = "IN_PROGRESS";

    @Column(name = "hours_worked", nullable = false)
    private int hoursWorked = 0;

    @Column(name = "hours_required", nullable = false)
    private int hoursRequired;

    @Column(name = "check_rolls", columnDefinition = "TEXT")
    private String checkRolls;

    @Column(name = "last_check_result", length = 50)
    private String lastCheckResult;

    @Column(name = "gold_spent", nullable = false)
    private int goldSpent = 0;

    @Column(name = "gold_required", nullable = false)
    private int goldRequired;

    @Column(name = "is_group_craft", nullable = false)
    private boolean isGroupCraft = false;

    @Column(name = "group_members", columnDefinition = "TEXT")
    private String groupMembers;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getHoursWorked() {
        return hoursWorked;
    }

    public void setHoursWorked(int hoursWorked) {
        this.hoursWorked = hoursWorked;
    }

    public int getHoursRequired() {
        return hoursRequired;
    }

    public void setHoursRequired(int hoursRequired) {
        this.hoursRequired = hoursRequired;
    }

    public String getCheckRolls() {
        return checkRolls;
    }

    public void setCheckRolls(String checkRolls) {
        this.checkRolls = checkRolls;
    }

    public String getLastCheckResult() {
        return lastCheckResult;
    }

    public void setLastCheckResult(String lastCheckResult) {
        this.lastCheckResult = lastCheckResult;
    }

    public int getGoldSpent() {
        return goldSpent;
    }

    public void setGoldSpent(int goldSpent) {
        this.goldSpent = goldSpent;
    }

    public int getGoldRequired() {
        return goldRequired;
    }

    public void setGoldRequired(int goldRequired) {
        this.goldRequired = goldRequired;
    }

    public boolean isGroupCraft() {
        return isGroupCraft;
    }

    public void setGroupCraft(boolean groupCraft) {
        isGroupCraft = groupCraft;
    }

    public String getGroupMembers() {
        return groupMembers;
    }

    public void setGroupMembers(String groupMembers) {
        this.groupMembers = groupMembers;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
