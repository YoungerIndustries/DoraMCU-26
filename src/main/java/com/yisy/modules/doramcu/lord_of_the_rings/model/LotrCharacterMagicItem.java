package com.yisy.modules.doramcu.lord_of_the_rings.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * JPA entity representing a magic item owned by a Lord of the Rings character.
 * <p>
 * Magic items in LOTR include Rings of Power, Palantíri, weapons like Andúril,
 * mithril coats, phials of Galadriel, and other artifacts.
 * </p>
 */
@Entity
@Table(name = "lotr_character_magic_items", indexes = {
        @Index(name = "idx_lotr_magic_item_character", columnList = "character_id")
})
public class LotrCharacterMagicItem {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "character_id", nullable = false)
    @JsonIgnore
    private LotrCharacter character;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "item_type", nullable = false, length = 50)
    private String itemType;

    @Column(length = 50)
    private String rarity = "Common";

    @Column(nullable = false)
    private boolean attuned = false;

    @Column(nullable = false)
    private boolean equipped = false;

    @Column(name = "ring_of_power")
    private Boolean ringOfPower = false;

    @Column(name = "corruption_resistance")
    private Integer corruptionResistance;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // --- Getters and Setters ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LotrCharacter getCharacter() {
        return character;
    }

    public void setCharacter(LotrCharacter character) {
        this.character = character;
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

    public boolean isAttuned() {
        return attuned;
    }

    public void setAttuned(boolean attuned) {
        this.attuned = attuned;
    }

    public boolean isEquipped() {
        return equipped;
    }

    public void setEquipped(boolean equipped) {
        this.equipped = equipped;
    }

    public Boolean getRingOfPower() {
        return ringOfPower;
    }

    public void setRingOfPower(Boolean ringOfPower) {
        this.ringOfPower = ringOfPower;
    }

    public Integer getCorruptionResistance() {
        return corruptionResistance;
    }

    public void setCorruptionResistance(Integer corruptionResistance) {
        this.corruptionResistance = corruptionResistance;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "LotrCharacterMagicItem{name='" + name + "', type='" + itemType + "', rarity='" + rarity + "'}";
    }
}
