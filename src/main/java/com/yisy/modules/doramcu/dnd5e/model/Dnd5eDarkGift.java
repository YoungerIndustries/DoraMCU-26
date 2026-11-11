package com.yisy.modules.doramcu.dnd5e.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * JPA entity representing a Dark Gift from Ravenloft.
 * <p>
 * Dark Gifts are horror-themed abilities that characters in the Domains of Dread
 * can acquire. They provide powerful benefits but come with a curse or drawback.
 * Each Dark Gift has a level requirement, a benefit, and a curse.
 * </p>
 * <p>
 * Key mechanics:
 * - Dark Gift Level: The minimum character level to acquire the gift
 * - Benefit: The powerful ability granted by the gift
 * - Curse: The drawback or curse that accompanies the gift
 * - Category: The type of horror (e.g., Undead, Fiend, Aberration, Fey)
 * - Darklord: Some gifts are associated with specific Darklords
 * </p>
 */
@Entity
@Table(name = "dnd5e_dark_gifts", indexes = {
        @Index(name = "idx_dnd5e_dark_gift_name", columnList = "name"),
        @Index(name = "idx_dnd5e_dark_gift_category", columnList = "category"),
        @Index(name = "idx_dnd5e_dark_gift_level", columnList = "min_level")
})
public class Dnd5eDarkGift {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    /**
     * Minimum character level required to acquire this Dark Gift.
     */
    @Column(name = "min_level", nullable = false)
    private int minLevel;

    /**
     * Category of horror: Undead, Fiend, Aberration, Fey, or Beast.
     */
    @Column(nullable = false, length = 50)
    private String category;

    /**
     * The powerful benefit granted by this Dark Gift.
     */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String benefit;

    /**
     * The curse or drawback that accompanies this Dark Gift.
     */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String curse;

    /**
     * Associated Darklord, if any (e.g., "Strahd", "Azalin", "Vecna").
     */
    @Column(length = 100)
    private String darklord;

    /**
     * Whether this Dark Gift can be acquired multiple times.
     */
    @Column(name = "repeatable", nullable = false)
    private boolean repeatable = false;

    /**
     * Description of the Dark Gift's lore and flavor.
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

    public int getMinLevel() {
        return minLevel;
    }

    public void setMinLevel(int minLevel) {
        this.minLevel = minLevel;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getBenefit() {
        return benefit;
    }

    public void setBenefit(String benefit) {
        this.benefit = benefit;
    }

    public String getCurse() {
        return curse;
    }

    public void setCurse(String curse) {
        this.curse = curse;
    }

    public String getDarklord() {
        return darklord;
    }

    public void setDarklord(String darklord) {
        this.darklord = darklord;
    }

    public boolean isRepeatable() {
        return repeatable;
    }

    public void setRepeatable(boolean repeatable) {
        this.repeatable = repeatable;
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
        return "Dnd5eDarkGift{name='" + name + "', category='" + category +
                "', minLevel=" + minLevel + ", darklord='" + darklord + "'}";
    }
}
