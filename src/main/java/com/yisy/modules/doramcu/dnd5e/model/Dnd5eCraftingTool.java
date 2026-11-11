package com.yisy.modules.doramcu.dnd5e.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * JPA entity representing a D&D 5e crafting tool.
 * <p>
 * A tool defines the proficiency requirements, associated ability score,
 * and the types of items that can be crafted with it.
 * Based on the 2024 PHB tool proficiency system.
 * </p>
 */
@Entity
@Table(name = "dnd5e_crafting_tools", indexes = {
        @Index(name = "idx_dnd5e_crafting_tool_name", columnList = "name"),
        @Index(name = "idx_dnd5e_crafting_tool_ability", columnList = "associated_ability")
})
public class Dnd5eCraftingTool {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false, length = 100, unique = true)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "associated_ability", nullable = false, length = 20)
    private String associatedAbility;

    @Column(name = "tool_type", nullable = false, length = 50)
    private String toolType;

    @Column(name = "craftable_items", columnDefinition = "TEXT")
    private String craftableItems;

    @Column(name = "combat_use", columnDefinition = "TEXT")
    private String combatUse;

    @Column(name = "exploration_use", columnDefinition = "TEXT")
    private String explorationUse;

    @Column(name = "use_action", columnDefinition = "TEXT")
    private String useAction;

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

    public String getAssociatedAbility() {
        return associatedAbility;
    }

    public void setAssociatedAbility(String associatedAbility) {
        this.associatedAbility = associatedAbility;
    }

    public String getToolType() {
        return toolType;
    }

    public void setToolType(String toolType) {
        this.toolType = toolType;
    }

    public String getCraftableItems() {
        return craftableItems;
    }

    public void setCraftableItems(String craftableItems) {
        this.craftableItems = craftableItems;
    }

    public String getCombatUse() {
        return combatUse;
    }

    public void setCombatUse(String combatUse) {
        this.combatUse = combatUse;
    }

    public String getExplorationUse() {
        return explorationUse;
    }

    public void setExplorationUse(String explorationUse) {
        this.explorationUse = explorationUse;
    }

    public String getUseAction() {
        return useAction;
    }

    public void setUseAction(String useAction) {
        this.useAction = useAction;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
