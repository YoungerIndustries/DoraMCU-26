package com.yisy.modules.doramcu.dnd5e.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * JPA entity representing a D&D 5e Tension Pool.
 * <p>
 * Tension pools are a mechanical system for tracking the consequences of player actions.
 * As tension increases, wandering encounters, alarms, and other complications become more likely.
 * This creates a dynamic, reactive game world where player choices matter.
 * </p>
 * <p>
 * Key mechanics:
 * - Tension Level: current tension (0 to maxTension, typically 10)
 * - Thresholds: tension levels that trigger specific consequences
 * - Consequences: wandering encounters, alarms, reinforcements, environmental hazards
 * - Actions: player actions that increase or decrease tension
 * - Reset: tension resets at rest stops or when leaving an area
 * </p>
 */
@Entity
@Table(name = "dnd5e_tension_pools", indexes = {
        @Index(name = "idx_dnd5e_tension_pool_name", columnList = "name"),
        @Index(name = "idx_dnd5e_tension_pool_area", columnList = "areaId")
})
public class Dnd5eTensionPool {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    /**
     * Name of the tension pool (e.g., "Guard Alertness", "Forest Danger").
     */
    @Column(nullable = false, length = 200)
    private String name;

    /**
     * Description of what this tension pool represents.
     */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    /**
     * Area or location this tension pool applies to (e.g., "Castle Ward", "Dark Forest").
     */
    @Column(name = "area_id", nullable = false, length = 100)
    private String areaId;

    /**
     * Current tension level (0 to maxTension).
     */
    @Column(name = "current_tension", nullable = false)
    private int currentTension = 0;

    /**
     * Maximum tension level before ultimate consequence triggers.
     */
    @Column(name = "max_tension", nullable = false)
    private int maxTension = 10;

    /**
     * Tension thresholds — JSON array of tension levels that trigger consequences.
     * e.g., [3, 5, 7, 9] means consequences at tension 3, 5, 7, and 9.
     */
    @Column(columnDefinition = "TEXT")
    private String thresholds = "[3, 5, 7, 9]";

    /**
     * Consequence descriptions at each threshold level.
     * JSON array matching thresholds order.
     */
    @Column(columnDefinition = "TEXT")
    private String consequences = "[]";

    /**
     * Ultimate consequence when max tension is reached.
     */
    @Column(name = "ultimate_consequence", columnDefinition = "TEXT")
    private String ultimateConsequence;

    /**
     * Whether thresholds have been triggered (tracks which consequences have fired).
     * JSON array of booleans matching thresholds order.
     */
    @Column(name = "triggered", columnDefinition = "TEXT")
    private String triggered = "[false, false, false, false]";

    /**
     * Whether this tension pool is active.
     */
    @Column(nullable = false)
    private boolean active = true;

    /**
     * DM-only notes about this tension pool.
     */
    @Column(columnDefinition = "TEXT")
    private String dmNotes;

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

    public String getAreaId() {
        return areaId;
    }

    public void setAreaId(String areaId) {
        this.areaId = areaId;
    }

    public int getCurrentTension() {
        return currentTension;
    }

    public void setCurrentTension(int currentTension) {
        this.currentTension = Math.max(0, Math.min(maxTension, currentTension));
    }

    public int getMaxTension() {
        return maxTension;
    }

    public void setMaxTension(int maxTension) {
        this.maxTension = Math.max(1, maxTension);
    }

    public String getThresholds() {
        return thresholds;
    }

    public void setThresholds(String thresholds) {
        this.thresholds = thresholds;
    }

    public String getConsequences() {
        return consequences;
    }

    public void setConsequences(String consequences) {
        this.consequences = consequences;
    }

    public String getUltimateConsequence() {
        return ultimateConsequence;
    }

    public void setUltimateConsequence(String ultimateConsequence) {
        this.ultimateConsequence = ultimateConsequence;
    }

    public String getTriggered() {
        return triggered;
    }

    public void setTriggered(String triggered) {
        this.triggered = triggered;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getDmNotes() {
        return dmNotes;
    }

    public void setDmNotes(String dmNotes) {
        this.dmNotes = dmNotes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    @Override
    public String toString() {
        return "Dnd5eTensionPool{name='" + name + "', area='" + areaId + "', tension=" + currentTension + "/" + maxTension + "}";
    }
}
