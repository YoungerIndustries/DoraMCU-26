package com.yisy.modules.doramcu.dnd5e.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * JPA entity representing a character's bound Growing Magic Item.
 * <p>
 * When a character attunes to a growing magic item, this entity tracks the
 * binding between the character and the item, including the current item level
 * and which abilities have been unlocked.
 * </p>
 * <p>
 * Key mechanics:
 * - Item Level: Scales with character level, up to the item's max level
 * - Unlocked Abilities: Abilities unlocked based on current item level
 * - Attunement: Character must be attuned to benefit from the item
 * - Level Up: When character levels up, the item may gain new abilities
 * </p>
 */
@Entity
@Table(name = "dnd5e_character_growing_items", indexes = {
        @Index(name = "idx_dnd5e_char_growing_item_character", columnList = "character_id"),
        @Index(name = "idx_dnd5e_char_growing_item_item", columnList = "growing_item_id"),
        @Index(name = "idx_dnd5e_char_growing_item_active", columnList = "character_id, active")
})
public class Dnd5eCharacterGrowingItem {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "character_id", nullable = false)
    private Long characterId;

    @Column(name = "growing_item_id", nullable = false)
    private Long growingItemId;

    @Column(nullable = false, length = 100)
    private String characterName;

    @Column(nullable = false, length = 100)
    private String itemName;

    /**
     * Current level of the growing item (scales with character level).
     */
    @Column(name = "item_level", nullable = false)
    private int itemLevel = 1;

    /**
     * Whether the character is currently attuned to this item.
     */
    @Column(name = "is_attuned", nullable = false)
    private boolean isAttuned = true;

    /**
     * Whether this item binding is active.
     */
    @Column(nullable = false)
    private boolean active = true;

    /**
     * Level at which ability 1 was unlocked.
     */
    @Column(name = "ability_1_unlocked_at")
    private Integer ability1UnlockedAt = 1;

    /**
     * Level at which ability 5 was unlocked.
     */
    @Column(name = "ability_5_unlocked_at")
    private Integer ability5UnlockedAt;

    /**
     * Level at which ability 10 was unlocked.
     */
    @Column(name = "ability_10_unlocked_at")
    private Integer ability10UnlockedAt;

    /**
     * Level at which ability 15 was unlocked.
     */
    @Column(name = "ability_15_unlocked_at")
    private Integer ability15UnlockedAt;

    /**
     * Level at which ability 20 was unlocked.
     */
    @Column(name = "ability_20_unlocked_at")
    private Integer ability20UnlockedAt;

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

    public Long getCharacterId() {
        return characterId;
    }

    public void setCharacterId(Long characterId) {
        this.characterId = characterId;
    }

    public Long getGrowingItemId() {
        return growingItemId;
    }

    public void setGrowingItemId(Long growingItemId) {
        this.growingItemId = growingItemId;
    }

    public String getCharacterName() {
        return characterName;
    }

    public void setCharacterName(String characterName) {
        this.characterName = characterName;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public int getItemLevel() {
        return itemLevel;
    }

    public void setItemLevel(int itemLevel) {
        this.itemLevel = Math.max(1, itemLevel);
    }

    public boolean isAttuned() {
        return isAttuned;
    }

    public void setAttuned(boolean attuned) {
        isAttuned = attuned;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Integer getAbility1UnlockedAt() {
        return ability1UnlockedAt;
    }

    public void setAbility1UnlockedAt(Integer ability1UnlockedAt) {
        this.ability1UnlockedAt = ability1UnlockedAt;
    }

    public Integer getAbility5UnlockedAt() {
        return ability5UnlockedAt;
    }

    public void setAbility5UnlockedAt(Integer ability5UnlockedAt) {
        this.ability5UnlockedAt = ability5UnlockedAt;
    }

    public Integer getAbility10UnlockedAt() {
        return ability10UnlockedAt;
    }

    public void setAbility10UnlockedAt(Integer ability10UnlockedAt) {
        this.ability10UnlockedAt = ability10UnlockedAt;
    }

    public Integer getAbility15UnlockedAt() {
        return ability15UnlockedAt;
    }

    public void setAbility15UnlockedAt(Integer ability15UnlockedAt) {
        this.ability15UnlockedAt = ability15UnlockedAt;
    }

    public Integer getAbility20UnlockedAt() {
        return ability20UnlockedAt;
    }

    public void setAbility20UnlockedAt(Integer ability20UnlockedAt) {
        this.ability20UnlockedAt = ability20UnlockedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    @Override
    public String toString() {
        return "Dnd5eCharacterGrowingItem{character='" + characterName + "', item='" + itemName +
                "', itemLevel=" + itemLevel + ", attuned=" + isAttuned + "}";
    }
}
