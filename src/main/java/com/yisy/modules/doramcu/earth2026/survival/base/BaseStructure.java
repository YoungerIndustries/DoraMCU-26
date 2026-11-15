package com.yisy.modules.doramcu.earth2026.survival.base;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * JPA entity for base structures in Earth2026 survival mode.
 * <p>
 * Structures include shelters, storage, workshops, defenses, and gardens
 * that characters can build and upgrade at their base location.
 * </p>
 */
@Entity
@Table(name = "base_structures", indexes = {
        @Index(name = "idx_base_structure_character", columnList = "characterId"),
        @Index(name = "idx_base_structure_type", columnList = "structureType")
})
public class BaseStructure {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    /**
     * Reference to the owning character.
     */
    @Column(name = "character_id", nullable = false)
    private Long characterId;

    /**
     * Structure type: SHELTER, STORAGE, WORKSHOP, DEFENSE, GARDEN.
     */
    @Column(name = "structure_type", nullable = false, length = 20)
    private String structureType;

    /**
     * Structure name (e.g., "Wooden Cabin", "Reinforced Wall").
     */
    @Column(nullable = false, length = 100)
    private String name;

    /**
     * Structure description.
     */
    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * Structure level (1-3 for most, 1-2 for some).
     */
    @Column(nullable = false)
    private int level = 1;

    /**
     * Maximum level for this structure type.
     */
    @Column(name = "max_level", nullable = false)
    private int maxLevel;

    /**
     * Health of the structure (0 = destroyed).
     */
    @Column(nullable = false)
    private int health = 100;

    /**
     * Maximum health of the structure.
     */
    @Column(name = "max_health", nullable = false)
    private int maxHealth = 100;

    /**
     * Hunger bonus per tick (for gardens).
     */
    @Column(name = "hunger_bonus")
    private int hungerBonus = 0;

    /**
     * Exposure reduction (for shelters).
     */
    @Column(name = "exposure_reduction")
    private int exposureReduction = 0;

    /**
     * Storage capacity bonus (for storage structures).
     */
    @Column(name = "storage_bonus")
    private int storageBonus = 0;

    /**
     * Whether this structure provides a workshop for crafting.
     */
    @Column(name = "is_workshop")
    private boolean isWorkshop = false;

    /**
     * Whether this structure provides defense.
     */
    @Column(name = "is_defense")
    private boolean isDefense = false;

    /**
     * Defense value (reduces damage from encounters).
     */
    @Column(name = "defense_value")
    private int defenseValue = 0;

    /**
     * Creation timestamp.
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Last upgrade timestamp.
     */
    @Column(name = "upgraded_at")
    private LocalDateTime upgradedAt;

    public BaseStructure() {
    }

    public BaseStructure(Long characterId, String structureType, String name) {
        this.characterId = characterId;
        this.structureType = structureType;
        this.name = name;
        this.createdAt = LocalDateTime.now();
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

    public String getStructureType() {
        return structureType;
    }

    public void setStructureType(String structureType) {
        this.structureType = structureType;
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

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = Math.max(1, Math.min(maxLevel, level));
    }

    public int getMaxLevel() {
        return maxLevel;
    }

    public void setMaxLevel(int maxLevel) {
        this.maxLevel = maxLevel;
    }

    public int getHealth() {
        return health;
    }

    public void setHealth(int health) {
        this.health = Math.max(0, Math.min(maxHealth, health));
    }

    public int getMaxHealth() {
        return maxHealth;
    }

    public void setMaxHealth(int maxHealth) {
        this.maxHealth = maxHealth;
    }

    public int getHungerBonus() {
        return hungerBonus;
    }

    public void setHungerBonus(int hungerBonus) {
        this.hungerBonus = hungerBonus;
    }

    public int getExposureReduction() {
        return exposureReduction;
    }

    public void setExposureReduction(int exposureReduction) {
        this.exposureReduction = exposureReduction;
    }

    public int getStorageBonus() {
        return storageBonus;
    }

    public void setStorageBonus(int storageBonus) {
        this.storageBonus = storageBonus;
    }

    public boolean isWorkshop() {
        return isWorkshop;
    }

    public void setWorkshop(boolean workshop) {
        isWorkshop = workshop;
    }

    public boolean isDefense() {
        return isDefense;
    }

    public void setDefense(boolean defense) {
        isDefense = defense;
    }

    public int getDefenseValue() {
        return defenseValue;
    }

    public void setDefenseValue(int defenseValue) {
        this.defenseValue = defenseValue;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpgradedAt() {
        return upgradedAt;
    }

    public void setUpgradedAt(LocalDateTime upgradedAt) {
        this.upgradedAt = upgradedAt;
    }

    /**
     * Check if the structure is destroyed.
     */
    public boolean isDestroyed() {
        return health <= 0;
    }

    /**
     * Check if the structure is at max level.
     */
    public boolean isMaxLevel() {
        return level >= maxLevel;
    }
}
