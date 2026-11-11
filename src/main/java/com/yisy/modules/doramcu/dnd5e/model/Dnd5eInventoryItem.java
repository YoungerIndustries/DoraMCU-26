package com.yisy.modules.doramcu.dnd5e.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * JPA entity representing a D&D 5e inventory item.
 * <p>
 * An inventory item belongs to a character and tracks properties such as
 * type, rarity, quantity, weight, value, equipped status, and magic item
 * properties (attunement, charges, AC, damage, etc.).
 * </p>
 */
@Entity
@Table(name = "dnd5e_inventory_items", indexes = {
        @Index(name = "idx_dnd5e_inventory_character", columnList = "character_id"),
        @Index(name = "idx_dnd5e_inventory_type", columnList = "item_type"),
        @Index(name = "idx_dnd5e_inventory_equipped", columnList = "character_id, equipped")
})
public class Dnd5eInventoryItem {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "character_id", nullable = false)
    private Long characterId;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "item_type", nullable = false, length = 50)
    private String itemType;

    @Column(length = 50)
    private String rarity = "Common";

    @Column(nullable = false)
    private int quantity = 1;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal weight = BigDecimal.ZERO;

    @Column(nullable = false)
    private int value = 0;

    @Column(nullable = false)
    private boolean equipped = false;

    @Column(nullable = false)
    private boolean attuned = false;

    @Column(nullable = false)
    private boolean identified = true;

    private Integer charges;

    @Column(name = "max_charges")
    private Integer maxCharges;

    @Column(name = "property_ac")
    private Integer propertyAc;

    @Column(name = "property_damage", length = 50)
    private String propertyDamage;

    @Column(name = "property_range", length = 50)
    private String propertyRange;

    @Column(name = "property_healing", length = 50)
    private String propertyHealing;

    @Column(columnDefinition = "TEXT")
    private String notes;

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

    public String getItemType() {
        return itemType;
    }

    public void setItemType(String itemType) {
        this.itemType = itemType;
    }

    public String getRarity() {
        return rarity;
    }

    public void setRarity(String rarity) {
        this.rarity = rarity;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getWeight() {
        return weight;
    }

    public void setWeight(BigDecimal weight) {
        this.weight = weight;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public boolean isEquipped() {
        return equipped;
    }

    public void setEquipped(boolean equipped) {
        this.equipped = equipped;
    }

    public boolean isAttuned() {
        return attuned;
    }

    public void setAttuned(boolean attuned) {
        this.attuned = attuned;
    }

    public boolean isIdentified() {
        return identified;
    }

    public void setIdentified(boolean identified) {
        this.identified = identified;
    }

    public Integer getCharges() {
        return charges;
    }

    public void setCharges(Integer charges) {
        this.charges = charges;
    }

    public Integer getMaxCharges() {
        return maxCharges;
    }

    public void setMaxCharges(Integer maxCharges) {
        this.maxCharges = maxCharges;
    }

    public Integer getPropertyAc() {
        return propertyAc;
    }

    public void setPropertyAc(Integer propertyAc) {
        this.propertyAc = propertyAc;
    }

    public String getPropertyDamage() {
        return propertyDamage;
    }

    public void setPropertyDamage(String propertyDamage) {
        this.propertyDamage = propertyDamage;
    }

    public String getPropertyRange() {
        return propertyRange;
    }

    public void setPropertyRange(String propertyRange) {
        this.propertyRange = propertyRange;
    }

    public String getPropertyHealing() {
        return propertyHealing;
    }

    public void setPropertyHealing(String propertyHealing) {
        this.propertyHealing = propertyHealing;
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

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
