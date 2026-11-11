package com.yisy.modules.doramcu.dnd5e.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * JPA entity representing a Bastion (party base) from the DMG.
 * <p>
 * Bastions are party bases that can be created and customized for perks.
 * They provide safe havens, crafting facilities, and special abilities.
 * </p>
 * <p>
 * Key mechanics:
 * - Level: The bastion's level (1-20), determines available perks
 * - Theme: The bastion's theme (Fortress, Tower, Ship, Camp, etc.)
 * - Perks: Special abilities unlocked at certain levels
 * - Resources: Gold and materials invested in the bastion
 * - Defenses: AC, HP, and defensive features
 * </p>
 */
@Entity
@Table(name = "dnd5e_bastions", indexes = {
        @Index(name = "idx_dnd5e_bastion_name", columnList = "name"),
        @Index(name = "idx_dnd5e_bastion_theme", columnList = "theme"),
        @Index(name = "idx_dnd5e_bastion_level", columnList = "level")
})
public class Dnd5eBastion {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    /**
     * Theme of the bastion: Fortress, Tower, Ship, Camp, Temple, etc.
     */
    @Column(nullable = false, length = 50)
    private String theme;

    /**
     * Current level of the bastion (1-20).
     */
    @Column(nullable = false)
    private int level = 1;

    /**
     * Description of the bastion's appearance and lore.
     */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    /**
     * Location of the bastion.
     */
    @Column(length = 200)
    private String location;

    /**
     * Hit Points of the bastion (for siege combat).
     */
    @Column(name = "hit_points", nullable = false)
    private int hitPoints = 100;

    /**
     * Maximum Hit Points of the bastion.
     */
    @Column(name = "max_hit_points", nullable = false)
    private int maxHitPoints = 100;

    /**
     * Armor Class of the bastion (for siege combat).
     */
    @Column(name = "armor_class", nullable = false)
    private int armorClass = 10;

    /**
     * Gold invested in the bastion.
     */
    @Column(name = "gold_invested", nullable = false)
    private int goldInvested = 0;

    /**
     * Number of characters associated with this bastion.
     */
    @Column(name = "party_size", nullable = false)
    private int partySize = 0;

    /**
     * Number of available rooms/spaces.
     */
    @Column(name = "room_count", nullable = false)
    private int roomCount = 3;

    /**
     * Whether the bastion has a crafting facility.
     */
    @Column(name = "has_crafting", nullable = false)
    private boolean hasCrafting = false;

    /**
     * Whether the bastion has a training facility.
     */
    @Column(name = "has_training", nullable = false)
    private boolean hasTraining = false;

    /**
     * Whether the bastion has a library/research facility.
     */
    @Column(name = "has_library", nullable = false)
    private boolean hasLibrary = false;

    /**
     * Whether the bastion has a healing sanctuary.
     */
    @Column(name = "has_healing", nullable = false)
    private boolean hasHealing = false;

    /**
     * Whether the bastion is currently active/usable.
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

    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = Math.max(1, Math.min(20, level));
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public int getHitPoints() {
        return hitPoints;
    }

    public void setHitPoints(int hitPoints) {
        this.hitPoints = Math.max(0, Math.min(maxHitPoints, hitPoints));
    }

    public int getMaxHitPoints() {
        return maxHitPoints;
    }

    public void setMaxHitPoints(int maxHitPoints) {
        this.maxHitPoints = Math.max(0, maxHitPoints);
    }

    public int getArmorClass() {
        return armorClass;
    }

    public void setArmorClass(int armorClass) {
        this.armorClass = armorClass;
    }

    public int getGoldInvested() {
        return goldInvested;
    }

    public void setGoldInvested(int goldInvested) {
        this.goldInvested = Math.max(0, goldInvested);
    }

    public int getPartySize() {
        return partySize;
    }

    public void setPartySize(int partySize) {
        this.partySize = Math.max(0, partySize);
    }

    public int getRoomCount() {
        return roomCount;
    }

    public void setRoomCount(int roomCount) {
        this.roomCount = Math.max(1, roomCount);
    }

    public boolean isHasCrafting() {
        return hasCrafting;
    }

    public void setHasCrafting(boolean hasCrafting) {
        this.hasCrafting = hasCrafting;
    }

    public boolean isHasTraining() {
        return hasTraining;
    }

    public void setHasTraining(boolean hasTraining) {
        this.hasTraining = hasTraining;
    }

    public boolean isHasLibrary() {
        return hasLibrary;
    }

    public void setHasLibrary(boolean hasLibrary) {
        this.hasLibrary = hasLibrary;
    }

    public boolean isHasHealing() {
        return hasHealing;
    }

    public void setHasHealing(boolean hasHealing) {
        this.hasHealing = hasHealing;
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
        return "Dnd5eBastion{name='" + name + "', theme='" + theme + "', level=" + level + "}";
    }
}
