package com.yisy.modules.doramcu.dnd5e.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * JPA entity representing a Villain Power from Villainous Options UA 2026.
 * <p>
 * Villain powers include lair actions, legendary actions, legendary resistances,
 * and regional effects that make villains unique and challenging opponents.
 * </p>
 * <p>
 * Key mechanics:
 * - Power Type: Lair Action, Legendary Action, Regional Effect, Minion Summon
 * - Cost: Action cost (0 for lair, 1-3 for legendary)
 * - Description: What the power does mechanically
 * - Recharge: When the power becomes available again
 * - Duration: How long the power's effects last
 * </p>
 */
@Entity
@Table(name = "dnd5e_villain_powers", indexes = {
        @Index(name = "idx_dnd5e_villain_power_villain", columnList = "villain_id"),
        @Index(name = "idx_dnd5e_villain_power_type", columnList = "powerType"),
        @Index(name = "idx_dnd5e_villain_power_name", columnList = "name")
})
public class Dnd5eVillainPower {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "villain_id", nullable = false)
    private Long villainId;

    @Column(nullable = false, length = 100)
    private String name;

    /**
     * Type of power: Lair Action, Legendary Action, Regional Effect, Minion Summon
     */
    @Column(name = "power_type", nullable = false, length = 50)
    private String powerType;

    /**
     * Action cost (0 for lair actions, 1-3 for legendary actions).
     */
    @Column(name = "action_cost", nullable = false)
    private int actionCost = 0;

    /**
     * Description of what the power does.
     */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    /**
     * Save ability required to resist the power (STR, DEX, CON, INT, WIS, CHA).
     */
    @Column(name = "save_ability", length = 10)
    private String saveAbility;

    /**
     * Save DC to resist the power.
     */
    @Column(name = "save_dc")
    private Integer saveDc;

    /**
     * Damage type if the power deals damage.
     */
    @Column(name = "damage_type", length = 50)
    private String damageType;

    /**
     * Damage dice formula (e.g., "4d6", "2d10+5").
     */
    @Column(name = "damage_dice", length = 50)
    private String damageDice;

    /**
     * Recharge condition (e.g., "5-6", "At the start of the villain's turn").
     */
    @Column(name = "recharge", length = 100)
    private String recharge;

    /**
     * Duration of the power's effects (e.g., "1 minute", "Until the end of the villain's next turn").
     */
    @Column(name = "duration", length = 100)
    private String duration;

    /**
     * Range of the power (e.g., "30-foot radius", "60-foot line").
     */
    @Column(name = "range", length = 100)
    private String range;

    /**
     * Whether this power is available in the villain's lair only.
     */
    @Column(name = "lair_only", nullable = false)
    private boolean lairOnly = false;

    /**
     * Whether this power is a regional effect.
     */
    @Column(name = "is_regional", nullable = false)
    private boolean isRegional = false;

    /**
     * Order of this power in the villain's power list.
     */
    @Column(name = "sort_order", nullable = false)
    private int sortOrder = 0;

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

    public Long getVillainId() {
        return villainId;
    }

    public void setVillainId(Long villainId) {
        this.villainId = villainId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPowerType() {
        return powerType;
    }

    public void setPowerType(String powerType) {
        this.powerType = powerType;
    }

    public int getActionCost() {
        return actionCost;
    }

    public void setActionCost(int actionCost) {
        this.actionCost = Math.max(0, actionCost);
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSaveAbility() {
        return saveAbility;
    }

    public void setSaveAbility(String saveAbility) {
        this.saveAbility = saveAbility;
    }

    public Integer getSaveDc() {
        return saveDc;
    }

    public void setSaveDc(Integer saveDc) {
        this.saveDc = saveDc;
    }

    public String getDamageType() {
        return damageType;
    }

    public void setDamageType(String damageType) {
        this.damageType = damageType;
    }

    public String getDamageDice() {
        return damageDice;
    }

    public void setDamageDice(String damageDice) {
        this.damageDice = damageDice;
    }

    public String getRecharge() {
        return recharge;
    }

    public void setRecharge(String recharge) {
        this.recharge = recharge;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getRange() {
        return range;
    }

    public void setRange(String range) {
        this.range = range;
    }

    public boolean isLairOnly() {
        return lairOnly;
    }

    public void setLairOnly(boolean lairOnly) {
        this.lairOnly = lairOnly;
    }

    public boolean isRegional() {
        return isRegional;
    }

    public void setRegional(boolean regional) {
        isRegional = regional;
    }

    public int getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    @Override
    public String toString() {
        return "Dnd5eVillainPower{name='" + name + "', type='" + powerType + "', cost=" + actionCost + "}";
    }
}
