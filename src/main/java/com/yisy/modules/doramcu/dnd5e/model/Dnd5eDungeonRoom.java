package com.yisy.modules.doramcu.dnd5e.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * JPA entity representing a D&D 5e Dungeon Room.
 * <p>
 * Dungeon rooms are the building blocks of dungeon crawls and adventure locations.
 * Each room has a theme, connections to other rooms, and optional traps, treasures,
 * and encounters. Circular connections ensure non-linear exploration.
 * </p>
 * <p>
 * Key mechanics:
 * - Theme: determines room atmosphere and typical contents
 * - Connections: list of connected room IDs for circular navigation
 * - Traps: hidden dangers with DCs and damage types
 * - Treasures: gold, items, or magic items found in the room
 * - Encounters: combat or social encounters triggered in the room
 * - Discovered: tracks whether players have found the room
 * </p>
 */
@Entity
@Table(name = "dnd5e_dungeon_rooms", indexes = {
        @Index(name = "idx_dnd5e_dungeon_room_name", columnList = "name"),
        @Index(name = "idx_dnd5e_dungeon_room_theme", columnList = "theme"),
        @Index(name = "idx_dnd5e_dungeon_room_dungeon_id", columnList = "dungeonId")
})
public class Dnd5eDungeonRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    /**
     * Name or title of the room.
     */
    @Column(nullable = false, length = 200)
    private String name;

    /**
     * Description of the room — what players see when they enter.
     */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    /**
     * Dungeon or location this room belongs to (e.g., "Lost Mine of Phandelver", "Tomb of Horrors").
     */
    @Column(name = "dungeon_id", nullable = false, length = 100)
    private String dungeonId;

    /**
     * Theme of the room: Combat, Trap, Treasure, Puzzle, Social, Lore, Rest, Boss, etc.
     */
    @Column(nullable = false, length = 50)
    private String theme;

    /**
     * Room size: Tiny, Small, Medium, Large, Huge.
     */
    @Column(nullable = false, length = 20)
    private String size = "Medium";

    /**
     * Connections to other rooms — list of room IDs this room connects to.
     * Circular connections ensure non-linear exploration.
     */
    @Column(columnDefinition = "TEXT")
    private String connections = "[]";

    /**
     * Trap description — hidden dangers in this room. Null if no trap.
     */
    @Column(columnDefinition = "TEXT")
    private String trap;

    /**
     * Trap DC — the DC to detect or disable the trap. Null if no trap.
     */
    @Column(name = "trap_dc")
    private Integer trapDc;

    /**
     * Trap damage type (e.g., "poison", "bludgeoning", "fire"). Null if no trap.
     */
    @Column(name = "trap_damage", length = 50)
    private String trapDamage;

    /**
     * Trap damage expression (e.g., "4d6"). Null if no trap.
     */
    @Column(name = "trap_damage_expr", length = 50)
    private String trapDamageExpr;

    /**
     * Treasure description — gold, items, or magic items in this room. Null if no treasure.
     */
    @Column(columnDefinition = "TEXT")
    private String treasure;

    /**
     * Gold pieces in this room. 0 if no gold.
     */
    @Column(name = "gold_pieces")
    private int goldPieces = 0;

    /**
     * Magic item rarity found here (Common, Uncommon, Rare, Very Rare, Legendary). Null if no magic item.
     */
    @Column(name = "magic_rarity", length = 50)
    private String magicRarity;

    /**
     * Encounter description — combat or social encounter in this room. Null if no encounter.
     */
    @Column(columnDefinition = "TEXT")
    private String encounter;

    /**
     * Encounter difficulty: Trivial, Easy, Medium, Hard, Deadly. Null if no encounter.
     */
    @Column(name = "encounter_difficulty", length = 20)
    private String encounterDifficulty;

    /**
     * XP reward for this room's encounter. 0 if no encounter.
     */
    @Column(name = "xp_reward")
    private int xpReward = 0;

    /**
     * Suggested minimum character level for this room.
     */
    @Column(name = "min_level", nullable = false)
    private int minLevel = 1;

    /**
     * Suggested maximum character level for this room.
     */
    @Column(name = "max_level", nullable = false)
    private int maxLevel = 20;

    /**
     * Whether this room has been discovered by players.
     */
    @Column(nullable = false)
    private boolean discovered = false;

    /**
     * Whether this room is active/available for generation.
     */
    @Column(nullable = false)
    private boolean active = true;

    /**
     * DM-only secrets about this room.
     */
    @Column(columnDefinition = "TEXT")
    private String dmSecrets;

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

    public String getDungeonId() {
        return dungeonId;
    }

    public void setDungeonId(String dungeonId) {
        this.dungeonId = dungeonId;
    }

    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getConnections() {
        return connections;
    }

    public void setConnections(String connections) {
        this.connections = connections;
    }

    public String getTrap() {
        return trap;
    }

    public void setTrap(String trap) {
        this.trap = trap;
    }

    public Integer getTrapDc() {
        return trapDc;
    }

    public void setTrapDc(Integer trapDc) {
        this.trapDc = trapDc;
    }

    public String getTrapDamage() {
        return trapDamage;
    }

    public void setTrapDamage(String trapDamage) {
        this.trapDamage = trapDamage;
    }

    public String getTrapDamageExpr() {
        return trapDamageExpr;
    }

    public void setTrapDamageExpr(String trapDamageExpr) {
        this.trapDamageExpr = trapDamageExpr;
    }

    public String getTreasure() {
        return treasure;
    }

    public void setTreasure(String treasure) {
        this.treasure = treasure;
    }

    public int getGoldPieces() {
        return goldPieces;
    }

    public void setGoldPieces(int goldPieces) {
        this.goldPieces = Math.max(0, goldPieces);
    }

    public String getMagicRarity() {
        return magicRarity;
    }

    public void setMagicRarity(String magicRarity) {
        this.magicRarity = magicRarity;
    }

    public String getEncounter() {
        return encounter;
    }

    public void setEncounter(String encounter) {
        this.encounter = encounter;
    }

    public String getEncounterDifficulty() {
        return encounterDifficulty;
    }

    public void setEncounterDifficulty(String encounterDifficulty) {
        this.encounterDifficulty = encounterDifficulty;
    }

    public int getXpReward() {
        return xpReward;
    }

    public void setXpReward(int xpReward) {
        this.xpReward = Math.max(0, xpReward);
    }

    public int getMinLevel() {
        return minLevel;
    }

    public void setMinLevel(int minLevel) {
        this.minLevel = Math.max(1, Math.min(20, minLevel));
    }

    public int getMaxLevel() {
        return maxLevel;
    }

    public void setMaxLevel(int maxLevel) {
        this.maxLevel = Math.max(1, Math.min(20, maxLevel));
    }

    public boolean isDiscovered() {
        return discovered;
    }

    public void setDiscovered(boolean discovered) {
        this.discovered = discovered;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getDmSecrets() {
        return dmSecrets;
    }

    public void setDmSecrets(String dmSecrets) {
        this.dmSecrets = dmSecrets;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    @Override
    public String toString() {
        return "Dnd5eDungeonRoom{name='" + name + "', dungeon='" + dungeonId + "', theme='" + theme + "'}";
    }
}
