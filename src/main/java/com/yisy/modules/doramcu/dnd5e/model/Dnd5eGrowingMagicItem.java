package com.yisy.modules.doramcu.dnd5e.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * JPA entity representing a Growing Magic Item from Arcana Unleashed.
 * <p>
 * Growing magic items are powerful artifacts that scale with the character's level,
 * providing progression-based benefits. Unlike static magic items, these items grow
 * stronger as the character advances, unlocking new abilities at certain level thresholds.
 * </p>
 * <p>
 * Key mechanics:
 * - Item Level: The current level of the item (scales with character level)
 * - Max Level: The maximum level the item can reach (typically 20)
 * - Abilities: Unlocked at specific level thresholds (e.g., 1, 5, 10, 15, 20)
 * - Attunement: Requires attunement and grows with the attuned character
 * - Category: Weapon, Armor, Wondrous Item, etc.
 * </p>
 */
@Entity
@Table(name = "dnd5e_growing_magic_items", indexes = {
        @Index(name = "idx_dnd5e_growing_item_name", columnList = "name"),
        @Index(name = "idx_dnd5e_growing_item_category", columnList = "category"),
        @Index(name = "idx_dnd5e_growing_item_rarity", columnList = "rarity")
})
public class Dnd5eGrowingMagicItem {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    /**
     * Category of the item: Weapon, Armor, Wondrous Item, etc.
     */
    @Column(nullable = false, length = 50)
    private String category;

    /**
     * Base rarity of the item (Common, Uncommon, Rare, Very Rare, Legendary, Artifact).
     */
    @Column(nullable = false, length = 50)
    private String rarity;

    /**
     * Maximum level the item can reach.
     */
    @Column(name = "max_level", nullable = false)
    private int maxLevel = 20;

    /**
     * Description of the item's lore and appearance.
     */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    /**
     * Level 1-4 ability description.
     */
    @Column(name = "ability_1", columnDefinition = "TEXT")
    private String ability1;

    /**
     * Level 5-9 ability description.
     */
    @Column(name = "ability_5", columnDefinition = "TEXT")
    private String ability5;

    /**
     * Level 10-14 ability description.
     */
    @Column(name = "ability_10", columnDefinition = "TEXT")
    private String ability10;

    /**
     * Level 15-19 ability description.
     */
    @Column(name = "ability_15", columnDefinition = "TEXT")
    private String ability15;

    /**
     * Level 20 ability description (ultimate power).
     */
    @Column(name = "ability_20", columnDefinition = "TEXT")
    private String ability20;

    /**
     * Whether the item requires attunement.
     */
    @Column(name = "requires_attunement", nullable = false)
    private boolean requiresAttunement = true;

    /**
     * Stat bonus at level 1 (e.g., "+1 to attack rolls").
     */
    @Column(name = "stat_bonus_1", length = 200)
    private String statBonus1;

    /**
     * Stat bonus at level 5.
     */
    @Column(name = "stat_bonus_5", length = 200)
    private String statBonus5;

    /**
     * Stat bonus at level 10.
     */
    @Column(name = "stat_bonus_10", length = 200)
    private String statBonus10;

    /**
     * Stat bonus at level 15.
     */
    @Column(name = "stat_bonus_15", length = 200)
    private String statBonus15;

    /**
     * Stat bonus at level 20.
     */
    @Column(name = "stat_bonus_20", length = 200)
    private String statBonus20;

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

    public int getMaxLevel() {
        return maxLevel;
    }

    public void setMaxLevel(int maxLevel) {
        this.maxLevel = maxLevel;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAbility1() {
        return ability1;
    }

    public void setAbility1(String ability1) {
        this.ability1 = ability1;
    }

    public String getAbility5() {
        return ability5;
    }

    public void setAbility5(String ability5) {
        this.ability5 = ability5;
    }

    public String getAbility10() {
        return ability10;
    }

    public void setAbility10(String ability10) {
        this.ability10 = ability10;
    }

    public String getAbility15() {
        return ability15;
    }

    public void setAbility15(String ability15) {
        this.ability15 = ability15;
    }

    public String getAbility20() {
        return ability20;
    }

    public void setAbility20(String ability20) {
        this.ability20 = ability20;
    }

    public boolean isRequiresAttunement() {
        return requiresAttunement;
    }

    public void setRequiresAttunement(boolean requiresAttunement) {
        this.requiresAttunement = requiresAttunement;
    }

    public String getStatBonus1() {
        return statBonus1;
    }

    public void setStatBonus1(String statBonus1) {
        this.statBonus1 = statBonus1;
    }

    public String getStatBonus5() {
        return statBonus5;
    }

    public void setStatBonus5(String statBonus5) {
        this.statBonus5 = statBonus5;
    }

    public String getStatBonus10() {
        return statBonus10;
    }

    public void setStatBonus10(String statBonus10) {
        this.statBonus10 = statBonus10;
    }

    public String getStatBonus15() {
        return statBonus15;
    }

    public void setStatBonus15(String statBonus15) {
        this.statBonus15 = statBonus15;
    }

    public String getStatBonus20() {
        return statBonus20;
    }

    public void setStatBonus20(String statBonus20) {
        this.statBonus20 = statBonus20;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    @Override
    public String toString() {
        return "Dnd5eGrowingMagicItem{name='" + name + "', category='" + category +
                "', rarity='" + rarity + "', maxLevel=" + maxLevel + "}";
    }
}
