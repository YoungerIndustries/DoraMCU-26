package com.yisy.modules.doramcu.dnd5e.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * JPA entity representing a D&D 5e Magic Item (2024 PHB).
 * <p>
 * Magic items are objects that have magical properties. They come in various
 * rarities, categories, and may require attunement to use. Some items have
 * charges that are consumed when using their properties.
 * </p>
 * <p>
 * Key mechanics:
 * - Rarity: Common, Uncommon, Rare, Very Rare, Legendary, Artifact
 * - Category: Weapon, Armor, Wondrous Item, Ring, Rod, Staff, Potion, Scroll, etc.
 * - Attunement: Some items require attunement; a character can attune to at most 3 items
 * - Charges: Some items have a number of charges; using a property may expend charges
 * - Recharging: Items with charges may recharge after a short rest, long rest, or dawn
 * - Curse: Some items are cursed and have detrimental effects
 * </p>
 * <p>
 * This entity defines the item's properties. Character-item bindings are tracked
 * separately in Dnd5eCharacterMagicItem.
 * </p>
 */
@Entity
@Table(name = "dnd5e_magic_items", indexes = {
        @Index(name = "idx_dnd5e_magic_item_name", columnList = "name", unique = true),
        @Index(name = "idx_dnd5e_magic_item_rarity", columnList = "rarity"),
        @Index(name = "idx_dnd5e_magic_item_category", columnList = "category"),
        @Index(name = "idx_dnd5e_magic_item_attunement", columnList = "requiresAttunement"),
        @Index(name = "idx_dnd5e_magic_item_charges", columnList = "hasCharges, maxCharges")
})
public class Dnd5eMagicItem {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    /**
     * Name of the magic item (e.g., "Bag of Holding", "Winged Boots").
     */
    @Column(nullable = false, length = 100, unique = true)
    private String name;

    /**
     * Full description of the item's lore, appearance, and properties.
     */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    /**
     * Rarity of the item: Common, Uncommon, Rare, Very Rare, Legendary, Artifact.
     */
    @Column(nullable = false, length = 20)
    private String rarity;

    /**
     * Category of the item: Weapon, Armor, Wondrous Item, Ring, Rod, Staff, Potion, Scroll, etc.
     */
    @Column(nullable = false, length = 30)
    private String category;

    /**
     * Whether the item requires attunement to use.
     */
    @Column(name = "requires_attunement", nullable = false)
    private boolean requiresAttunement = false;

    /**
     * Whether the item requires attunement by a specific class.
     */
    @Column(name = "attunement_class", length = 50)
    private String attunementClass;

    /**
     * Whether the item has charges.
     */
    @Column(name = "has_charges", nullable = false)
    private boolean hasCharges = false;

    /**
     * Maximum number of charges (if hasCharges is true).
     */
    @Column(name = "max_charges")
    private Integer maxCharges;

    /**
     * How the item recharges: Dawn, Short Rest, Long Rest, None, or a custom rule.
     */
    @Column(name = "recharge_rule", length = 50)
    private String rechargeRule = "None";

    /**
     * Recharge DC for items that recharge on a d20 roll (e.g., "DC 5" means 5 or higher on d20).
     */
    @Column(name = "recharge_dc")
    private Integer rechargeDc;

    /**
     * Whether the item is cursed.
     */
    @Column(nullable = false)
    private boolean isCursed = false;

    /**
     * Description of the curse's effects (if isCursed is true).
     */
    @Column(name = "curse_description", columnDefinition = "TEXT")
    private String curseDescription;

    /**
     * Action required to use the item: Action, Bonus Action, Reaction, No Action, or custom.
     */
    @Column(name = "use_action", length = 30)
    private String useAction;

    /**
     * Duration of the item's effect when used (e.g., "1 hour", "1 minute", "Instantaneous").
     */
    @Column(name = "effect_duration", length = 50)
    private String effectDuration;

    /**
     * Range of the item's effect (e.g., "Self", "Touch", "60 feet", "30-foot radius").
     */
    @Column(name = "effect_range", length = 50)
    private String effectRange;

    /**
     * Whether this item is active and available for use.
     */
    @Column(nullable = false)
    private boolean active = true;

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

    public String getRarity() {
        return rarity;
    }

    public void setRarity(String rarity) {
        this.rarity = rarity;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public boolean isRequiresAttunement() {
        return requiresAttunement;
    }

    public void setRequiresAttunement(boolean requiresAttunement) {
        this.requiresAttunement = requiresAttunement;
    }

    public String getAttunementClass() {
        return attunementClass;
    }

    public void setAttunementClass(String attunementClass) {
        this.attunementClass = attunementClass;
    }

    public boolean isHasCharges() {
        return hasCharges;
    }

    public void setHasCharges(boolean hasCharges) {
        this.hasCharges = hasCharges;
    }

    public Integer getMaxCharges() {
        return maxCharges;
    }

    public void setMaxCharges(Integer maxCharges) {
        this.maxCharges = maxCharges != null ? Math.max(0, maxCharges) : null;
    }

    public String getRechargeRule() {
        return rechargeRule;
    }

    public void setRechargeRule(String rechargeRule) {
        this.rechargeRule = rechargeRule;
    }

    public Integer getRechargeDc() {
        return rechargeDc;
    }

    public void setRechargeDc(Integer rechargeDc) {
        this.rechargeDc = rechargeDc;
    }

    public boolean isCursed() {
        return isCursed;
    }

    public void setCursed(boolean cursed) {
        isCursed = cursed;
    }

    public String getCurseDescription() {
        return curseDescription;
    }

    public void setCurseDescription(String curseDescription) {
        this.curseDescription = curseDescription;
    }

    public String getUseAction() {
        return useAction;
    }

    public void setUseAction(String useAction) {
        this.useAction = useAction;
    }

    public String getEffectDuration() {
        return effectDuration;
    }

    public void setEffectDuration(String effectDuration) {
        this.effectDuration = effectDuration;
    }

    public String getEffectRange() {
        return effectRange;
    }

    public void setEffectRange(String effectRange) {
        this.effectRange = effectRange;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    @Override
    public String toString() {
        return "Dnd5eMagicItem{name='" + name + "', rarity='" + rarity + "', category='" + category + "'}";
    }
}
