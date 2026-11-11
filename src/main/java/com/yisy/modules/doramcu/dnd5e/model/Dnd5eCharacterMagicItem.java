package com.yisy.modules.doramcu.dnd5e.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * JPA entity representing a character's bound Magic Item.
 * <p>
 * When a character acquires a magic item, this entity tracks the binding
 * between the character and the item, including attunement state, charges
 * remaining, and usage history.
 * </p>
 * <p>
 * Key mechanics:
 * - Attunement: A character can attune to at most 3 items that require attunement
 * - Charges: Items with charges track remaining charges; using an item may expend charges
 * - Recharging: Charges may recharge after short rest, long rest, or dawn
 * - Curse: Cursed items may have ongoing detrimental effects
 * - Active State: Whether the item is currently in the character's possession
 * </p>
 */
@Entity
@Table(name = "dnd5e_character_magic_items", indexes = {
        @Index(name = "idx_dnd5e_char_magic_item_character", columnList = "character_id"),
        @Index(name = "idx_dnd5e_char_magic_item_item", columnList = "magic_item_id"),
        @Index(name = "idx_dnd5e_char_magic_item_attuned", columnList = "character_id, is_attuned"),
        @Index(name = "idx_dnd5e_char_magic_item_active", columnList = "character_id, active")
})
public class Dnd5eCharacterMagicItem {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "character_id", nullable = false)
    private Long characterId;

    @Column(name = "magic_item_id", nullable = false)
    private Long magicItemId;

    @Column(nullable = false, length = 100)
    private String characterName;

    @Column(nullable = false, length = 100)
    private String itemName;

    /**
     * Whether the character is currently attuned to this item.
     */
    @Column(name = "is_attuned", nullable = false)
    private boolean isAttuned = false;

    /**
     * Current charges remaining (for items with charges).
     */
    @Column(name = "charges_remaining")
    private Integer chargesRemaining;

    /**
     * Whether this item binding is active (item is in character's possession).
     */
    @Column(nullable = false)
    private boolean active = true;

    /**
     * Whether the curse has been revealed to the character.
     */
    @Column(name = "curse_revealed", nullable = false)
    private boolean curseRevealed = false;

    /**
     * Number of times the item's property has been used.
     */
    @Column(name = "use_count", nullable = false)
    private int useCount = 0;

    /**
     * When the charges were last recharged.
     */
    @Column(name = "last_recharged_at")
    private LocalDateTime lastRechargedAt;

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

    public Long getMagicItemId() {
        return magicItemId;
    }

    public void setMagicItemId(Long magicItemId) {
        this.magicItemId = magicItemId;
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

    public boolean isAttuned() {
        return isAttuned;
    }

    public void setAttuned(boolean attuned) {
        isAttuned = attuned;
    }

    public Integer getChargesRemaining() {
        return chargesRemaining;
    }

    public void setChargesRemaining(Integer chargesRemaining) {
        this.chargesRemaining = chargesRemaining;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isCurseRevealed() {
        return curseRevealed;
    }

    public void setCurseRevealed(boolean curseRevealed) {
        this.curseRevealed = curseRevealed;
    }

    public int getUseCount() {
        return useCount;
    }

    public void setUseCount(int useCount) {
        this.useCount = Math.max(0, useCount);
    }

    public LocalDateTime getLastRechargedAt() {
        return lastRechargedAt;
    }

    public void setLastRechargedAt(LocalDateTime lastRechargedAt) {
        this.lastRechargedAt = lastRechargedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    @Override
    public String toString() {
        return "Dnd5eCharacterMagicItem{character='" + characterName + "', item='" + itemName + "', attuned=" + isAttuned + "}";
    }
}
