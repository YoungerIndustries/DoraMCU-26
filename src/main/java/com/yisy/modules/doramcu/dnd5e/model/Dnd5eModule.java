package com.yisy.modules.doramcu.dnd5e.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * JPA entity representing a D&D 5e Adventure Module.
 * <p>
 * A module is a complete, self-contained adventure that ties together all
 * the building blocks: adventure hooks, dungeon rooms, NPCs, encounters,
 * villains, bastions, tension pools, and more. This is the master model
 * for module building, allowing DMs to create, organize, and manage
 * complete adventures.
 * </p>
 * <p>
 * Key mechanics:
 * - Module references all subsystems by ID for flexibility
 * - Theme determines the overall tone (Horror, High Magic, Sandbox, etc.)
 * - Level range defines the target party levels
 * - Estimated sessions helps with campaign planning
 * - Status tracks module completion (Draft, In Progress, Complete, Archived)
 * - Module summary provides a quick overview for DMs
 * - DM notes contain private information not shared with players
 * </p>
 * <p>
 * Module themes:
 * - Horror: Ravenloft-style dread and fear mechanics
 * - High Magic: Arcana Unleashed growing items and psionics
 * - Sandbox: Open-world exploration with tension pools
 * - Dungeon Crawl: Room-by-room exploration with traps and treasure
 * - Feywild: Whimsical and magical encounters
 * - Steampunk: Magic-technology fusion
 * </p>
 */
@Entity
@Table(name = "dnd5e_modules", indexes = {
        @Index(name = "idx_dnd5e_module_name", columnList = "name"),
        @Index(name = "idx_dnd5e_module_theme", columnList = "theme"),
        @Index(name = "idx_dnd5e_module_status", columnList = "status"),
        @Index(name = "idx_dnd5e_module_level_range", columnList = "minLevel, maxLevel")
})
public class Dnd5eModule {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    /**
     * Name of the module (e.g., "Curse of Strahd", "Tomb of Annihilation").
     */
    @Column(nullable = false, length = 200)
    private String name;

    /**
     * Brief tagline or subtitle for the module.
     */
    @Column(length = 300)
    private String tagline;

    /**
     * Full description of the module's premise and scope.
     */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    /**
     * Module theme: Horror, High Magic, Sandbox, Dungeon Crawl, Feywild, Steampunk.
     */
    @Column(nullable = false, length = 30)
    private String theme;

    /**
     * Minimum recommended party level for this module.
     */
    @Column(name = "min_level", nullable = false)
    private int minLevel = 1;

    /**
     * Maximum recommended party level for this module.
     */
    @Column(name = "max_level", nullable = false)
    private int maxLevel = 5;

    /**
     * Estimated number of sessions to complete this module.
     */
    @Column(name = "estimated_sessions", nullable = false)
    private int estimatedSessions = 4;

    /**
     * Module status: Draft, In Progress, Complete, Archived.
     */
    @Column(nullable = false, length = 20)
    private String status = "Draft";

    /**
     * Module summary — a quick overview for DMs (generated or manual).
     */
    @Column(columnDefinition = "TEXT")
    private String summary;

    /**
     * DM-only notes about this module (private information).
     */
    @Column(name = "dm_notes", columnDefinition = "TEXT")
    private String dmNotes;

    // --- Referenced Adventure Hooks ---

    /**
     * Comma-separated list of adventure hook IDs included in this module.
     */
    @Column(name = "hook_ids", columnDefinition = "TEXT")
    private String hookIds = "";

    // --- Referenced Dungeon Rooms ---

    /**
     * Comma-separated list of dungeon room IDs included in this module.
     */
    @Column(name = "room_ids", columnDefinition = "TEXT")
    private String roomIds = "";

    // --- Referenced NPCs ---

    /**
     * Comma-separated list of NPC IDs included in this module.
     */
    @Column(name = "npc_ids", columnDefinition = "TEXT")
    private String npcIds = "";

    // --- Referenced Encounters ---

    /**
     * Comma-separated list of encounter IDs included in this module.
     */
    @Column(name = "encounter_ids", columnDefinition = "TEXT")
    private String encounterIds = "";

    // --- Referenced Villains ---

    /**
     * Comma-separated list of villain IDs included in this module.
     */
    @Column(name = "villain_ids", columnDefinition = "TEXT")
    private String villainIds = "";

    // --- Referenced Bastions ---

    /**
     * Comma-separated list of bastion IDs included in this module.
     */
    @Column(name = "bastion_ids", columnDefinition = "TEXT")
    private String bastionIds = "";

    // --- Referenced Tension Pools ---

    /**
     * Comma-separated list of tension pool IDs included in this module.
     */
    @Column(name = "tension_pool_ids", columnDefinition = "TEXT")
    private String tensionPoolIds = "";

    // --- Referenced Growing Magic Items ---

    /**
     * Comma-separated list of growing magic item IDs included in this module.
     */
    @Column(name = "growing_item_ids", columnDefinition = "TEXT")
    private String growingItemIds = "";

    // --- Referenced Dark Gifts (Horror) ---

    /**
     * Comma-separated list of Dark Gift IDs available in this module.
     */
    @Column(name = "dark_gift_ids", columnDefinition = "TEXT")
    private String darkGiftIds = "";

    // --- Referenced Feats ---

    /**
     * Comma-separated list of feat IDs available in this module.
     */
    @Column(name = "feat_ids", columnDefinition = "TEXT")
    private String featIds = "";

    // --- Referenced Crafting Recipes ---

    /**
     * Comma-separated list of crafting recipe IDs available in this module.
     */
    @Column(name = "crafting_recipe_ids", columnDefinition = "TEXT")
    private String craftingRecipeIds = "";

    // --- Referenced Psionic Powers ---

    /**
     * Comma-separated list of psionic power IDs available in this module.
     */
    @Column(name = "psionic_power_ids", columnDefinition = "TEXT")
    private String psionicPowerIds = "";

    /**
     * Whether this module is active (not archived).
     */
    @Column(nullable = false)
    private boolean active = true;

    /**
     * Number of times this module has been played.
     */
    @Column(name = "play_count", nullable = false)
    private int playCount = 0;

    /**
     * Average rating of this module (1-5 stars).
     */
    @Column(nullable = false)
    private double rating = 0.0;

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

    public String getTagline() {
        return tagline;
    }

    public void setTagline(String tagline) {
        this.tagline = tagline;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
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

    public int getEstimatedSessions() {
        return estimatedSessions;
    }

    public void setEstimatedSessions(int estimatedSessions) {
        this.estimatedSessions = Math.max(1, estimatedSessions);
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getDmNotes() {
        return dmNotes;
    }

    public void setDmNotes(String dmNotes) {
        this.dmNotes = dmNotes;
    }

    public String getHookIds() {
        return hookIds;
    }

    public void setHookIds(String hookIds) {
        this.hookIds = hookIds;
    }

    public String getRoomIds() {
        return roomIds;
    }

    public void setRoomIds(String roomIds) {
        this.roomIds = roomIds;
    }

    public String getNpcIds() {
        return npcIds;
    }

    public void setNpcIds(String npcIds) {
        this.npcIds = npcIds;
    }

    public String getEncounterIds() {
        return encounterIds;
    }

    public void setEncounterIds(String encounterIds) {
        this.encounterIds = encounterIds;
    }

    public String getVillainIds() {
        return villainIds;
    }

    public void setVillainIds(String villainIds) {
        this.villainIds = villainIds;
    }

    public String getBastionIds() {
        return bastionIds;
    }

    public void setBastionIds(String bastionIds) {
        this.bastionIds = bastionIds;
    }

    public String getTensionPoolIds() {
        return tensionPoolIds;
    }

    public void setTensionPoolIds(String tensionPoolIds) {
        this.tensionPoolIds = tensionPoolIds;
    }

    public String getGrowingItemIds() {
        return growingItemIds;
    }

    public void setGrowingItemIds(String growingItemIds) {
        this.growingItemIds = growingItemIds;
    }

    public String getDarkGiftIds() {
        return darkGiftIds;
    }

    public void setDarkGiftIds(String darkGiftIds) {
        this.darkGiftIds = darkGiftIds;
    }

    public String getFeatIds() {
        return featIds;
    }

    public void setFeatIds(String featIds) {
        this.featIds = featIds;
    }

    public String getCraftingRecipeIds() {
        return craftingRecipeIds;
    }

    public void setCraftingRecipeIds(String craftingRecipeIds) {
        this.craftingRecipeIds = craftingRecipeIds;
    }

    public String getPsionicPowerIds() {
        return psionicPowerIds;
    }

    public void setPsionicPowerIds(String psionicPowerIds) {
        this.psionicPowerIds = psionicPowerIds;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public int getPlayCount() {
        return playCount;
    }

    public void setPlayCount(int playCount) {
        this.playCount = Math.max(0, playCount);
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = Math.max(0, Math.min(5, rating));
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    @Override
    public String toString() {
        return "Dnd5eModule{name='" + name + "', theme='" + theme + "', status='" + status + "'}";
    }
}
