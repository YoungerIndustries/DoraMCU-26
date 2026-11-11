package com.yisy.modules.doramcu.dnd5e.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * JPA entity representing a D&D 5e Campaign Theme Template.
 * <p>
 * A campaign template provides a pre-built framework for DMs to quickly
 * start a themed campaign. Each template includes recommended components
 * from all subsystems, setup instructions, pacing guide, and example
 * content that matches the theme.
 * </p>
 * <p>
 * Key mechanics:
 * - Theme determines the overall tone and recommended subsystems
 * - Level range defines the target party levels
 * - Recommended components suggest which subsystems to use
 * - Setup instructions guide DMs through campaign creation
 * - Pacing guide provides session-by-session structure
 * - Example content provides ready-to-use hooks, NPCs, and encounters
 * - Difficulty scaling adjusts for different party sizes and levels
 * </p>
 * <p>
 * Supported themes:
 * - Horror: Ravenloft-style dread, fear checks, Dark Gifts, tension pools
 * - High Magic: Growing magic items, psionics, spellcasting focus
 * - Sandbox: Open-world exploration, bastions, NPC relationships
 * - Dungeon Crawl: Room-by-room exploration, traps, treasure, encounters
 * - Feywild: Whimsical encounters, fey NPCs, non-combat resolution
 * - Steampunk: Magic-technology fusion, crafting, artificer focus
 * </p>
 */
@Entity
@Table(name = "dnd5e_campaign_templates", indexes = {
        @Index(name = "idx_dnd5e_campaign_template_name", columnList = "name", unique = true),
        @Index(name = "idx_dnd5e_campaign_template_theme", columnList = "theme"),
        @Index(name = "idx_dnd5e_campaign_template_level", columnList = "minLevel, maxLevel")
})
public class Dnd5eCampaignTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    /**
     * Name of the template (e.g., "Ravenloft Horror", "Arcane Academy").
     */
    @Column(nullable = false, length = 200, unique = true)
    private String name;

    /**
     * Brief tagline for the template.
     */
    @Column(length = 300)
    private String tagline;

    /**
     * Full description of the template's theme and scope.
     */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    /**
     * Campaign theme: Horror, High Magic, Sandbox, Dungeon Crawl, Feywild, Steampunk.
     */
    @Column(nullable = false, length = 30)
    private String theme;

    /**
     * Minimum recommended party level.
     */
    @Column(name = "min_level", nullable = false)
    private int minLevel = 1;

    /**
     * Maximum recommended party level.
     */
    @Column(name = "max_level", nullable = false)
    private int maxLevel = 5;

    /**
     * Recommended party size.
     */
    @Column(name = "recommended_party_size", nullable = false)
    private int recommendedPartySize = 4;

    /**
     * Estimated number of sessions for a complete campaign arc.
     */
    @Column(name = "estimated_sessions", nullable = false)
    private int estimatedSessions = 12;

    /**
     * Recommended subsystems as JSON array (e.g., ["horror", "tension_pools", "dark_gifts"]).
     */
    @Column(name = "recommended_subsystems", columnDefinition = "JSON")
    private String recommendedSubsystems = "[]";

    /**
     * Setup instructions for DMs — step-by-step guide to creating a campaign from this template.
     */
    @Column(name = "setup_instructions", columnDefinition = "TEXT")
    private String setupInstructions;

    /**
     * Pacing guide — session-by-session structure for the campaign.
     */
    @Column(name = "pacing_guide", columnDefinition = "TEXT")
    private String pacingGuide;

    /**
     * Example adventure hook text for this template.
     */
    @Column(name = "example_hook", columnDefinition = "TEXT")
    private String exampleHook;

    /**
     * Example villain description for this template.
     */
    @Column(name = "example_villain", columnDefinition = "TEXT")
    private String exampleVillain;

    /**
     * Example NPC descriptions as JSON array.
     */
    @Column(name = "example_npcs", columnDefinition = "JSON")
    private String exampleNpcs = "[]";

    /**
     * Example encounter descriptions as JSON array.
     */
    @Column(name = "example_encounters", columnDefinition = "JSON")
    private String exampleEncounters = "[]";

    /**
     * Difficulty scaling notes — how to adjust for different party sizes/levels.
     */
    @Column(name = "difficulty_scaling", columnDefinition = "TEXT")
    private String difficultyScaling;

    /**
     * Tips and tricks for running this type of campaign.
     */
    @Column(name = "dm_tips", columnDefinition = "TEXT")
    private String dmTips;

    /**
     * Whether this template is active and available for use.
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
        this.minLevel = Math.max(1, minLevel);
    }

    public int getMaxLevel() {
        return maxLevel;
    }

    public void setMaxLevel(int maxLevel) {
        this.maxLevel = Math.max(minLevel, maxLevel);
    }

    public int getRecommendedPartySize() {
        return recommendedPartySize;
    }

    public void setRecommendedPartySize(int recommendedPartySize) {
        this.recommendedPartySize = Math.max(1, Math.min(8, recommendedPartySize));
    }

    public int getEstimatedSessions() {
        return estimatedSessions;
    }

    public void setEstimatedSessions(int estimatedSessions) {
        this.estimatedSessions = Math.max(1, estimatedSessions);
    }

    public String getRecommendedSubsystems() {
        return recommendedSubsystems;
    }

    public void setRecommendedSubsystems(String recommendedSubsystems) {
        this.recommendedSubsystems = recommendedSubsystems;
    }

    public String getSetupInstructions() {
        return setupInstructions;
    }

    public void setSetupInstructions(String setupInstructions) {
        this.setupInstructions = setupInstructions;
    }

    public String getPacingGuide() {
        return pacingGuide;
    }

    public void setPacingGuide(String pacingGuide) {
        this.pacingGuide = pacingGuide;
    }

    public String getExampleHook() {
        return exampleHook;
    }

    public void setExampleHook(String exampleHook) {
        this.exampleHook = exampleHook;
    }

    public String getExampleVillain() {
        return exampleVillain;
    }

    public void setExampleVillain(String exampleVillain) {
        this.exampleVillain = exampleVillain;
    }

    public String getExampleNpcs() {
        return exampleNpcs;
    }

    public void setExampleNpcs(String exampleNpcs) {
        this.exampleNpcs = exampleNpcs;
    }

    public String getExampleEncounters() {
        return exampleEncounters;
    }

    public void setExampleEncounters(String exampleEncounters) {
        this.exampleEncounters = exampleEncounters;
    }

    public String getDifficultyScaling() {
        return difficultyScaling;
    }

    public void setDifficultyScaling(String difficultyScaling) {
        this.difficultyScaling = difficultyScaling;
    }

    public String getDmTips() {
        return dmTips;
    }

    public void setDmTips(String dmTips) {
        this.dmTips = dmTips;
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
        return "Dnd5eCampaignTemplate{name='" + name + "', theme='" + theme + "', levels=" + minLevel + "-" + maxLevel + "}";
    }
}
