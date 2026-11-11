package com.yisy.modules.doramcu.dnd5e.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * JPA entity representing a psionic power definition.
 * <p>
 * Psionic powers are the psion's version of spells, fueled by power points
 * instead of spell slots. Each power has a level, cost, duration, and subclass
 * association. Powers can be cantrips (level 0), regular powers (1-9), or
 * subclass-specific abilities.
 * </p>
 * <p>
 * Key mechanics:
 * - Power Level: 0 (cantrip) to 9, plus subclass abilities
 * - Power Point Cost: Varies by level and power
 * - Duration: Instantaneous, concentration, or fixed duration
 * - Subclass: Some powers are subclass-specific
 * - Ritual: Some powers can be cast as rituals
 * </p>
 */
@Entity
@Table(name = "dnd5e_psionic_powers", indexes = {
        @Index(name = "idx_dnd5e_psionic_power_name", columnList = "name"),
        @Index(name = "idx_dnd5e_psionic_power_level", columnList = "power_level"),
        @Index(name = "idx_dnd5e_psionic_power_subclass", columnList = "subclass")
})
public class Dnd5ePsionicPower {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    /**
     * Power level: 0 (cantrip) to 9.
     */
    @Column(name = "power_level", nullable = false)
    private int powerLevel;

    /**
     * Power point cost to use this power.
     */
    @Column(name = "power_point_cost", nullable = false)
    private int powerPointCost;

    /**
     * Casting time (e.g., "1 action", "1 bonus action", "1 reaction").
     */
    @Column(name = "casting_time", nullable = false, length = 50)
    private String castingTime;

    /**
     * Duration (e.g., "Instantaneous", "Concentration, up to 1 minute", "1 hour").
     */
    @Column(nullable = false, length = 100)
    private String duration;

    /**
     * Range of the power (e.g., "Self", "60 feet", "Touch").
     */
    @Column(nullable = false, length = 50)
    private String range;

    /**
     * Whether this power requires concentration.
     */
    @Column(nullable = false)
    private boolean concentration = false;

    /**
     * Whether this power can be cast as a ritual.
     */
    @Column(nullable = false)
    private boolean ritual = false;

    /**
     * The psion subclass this power belongs to, or "All" for general powers.
     */
    @Column(nullable = false, length = 50)
    private String subclass;

    /**
     * The school of psionic power (e.g., "Evocation", "Divination", "Enchantment").
     */
    @Column(nullable = false, length = 50)
    private String school;

    /**
     * Description of the power's effects.
     */
    @Column(columnDefinition = "TEXT")
    private String description;

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

    public int getPowerLevel() {
        return powerLevel;
    }

    public void setPowerLevel(int powerLevel) {
        this.powerLevel = powerLevel;
    }

    public int getPowerPointCost() {
        return powerPointCost;
    }

    public void setPowerPointCost(int powerPointCost) {
        this.powerPointCost = powerPointCost;
    }

    public String getCastingTime() {
        return castingTime;
    }

    public void setCastingTime(String castingTime) {
        this.castingTime = castingTime;
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

    public boolean isConcentration() {
        return concentration;
    }

    public void setConcentration(boolean concentration) {
        this.concentration = concentration;
    }

    public boolean isRitual() {
        return ritual;
    }

    public void setRitual(boolean ritual) {
        this.ritual = ritual;
    }

    public String getSubclass() {
        return subclass;
    }

    public void setSubclass(String subclass) {
        this.subclass = subclass;
    }

    public String getSchool() {
        return school;
    }

    public void setSchool(String school) {
        this.school = school;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    @Override
    public String toString() {
        return "Dnd5ePsionicPower{name='" + name + "', level=" + powerLevel +
                ", cost=" + powerPointCost + ", subclass='" + subclass + "'}";
    }
}
