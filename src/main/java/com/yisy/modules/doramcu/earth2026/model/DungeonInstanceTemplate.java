package com.yisy.modules.doramcu.earth2026.model;

import jakarta.persistence.*;

/**
 * JPA entity representing a dungeon instance template.
 * <p>
 * Templates define the parameters for generating dungeon instances, including
 * theme, difficulty range, room count, and types of encounters, traps, and treasures.
 * </p>
 */
@Entity
@Table(name = "dungeon_instance_templates", indexes = {
        @Index(name = "idx_dit_id", columnList = "template_id", unique = true),
        @Index(name = "idx_dit_theme", columnList = "theme")
})
public class DungeonInstanceTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    /**
     * Unique template identifier (e.g., "combat_crawl", "puzzle_dungeon").
     */
    @Column(name = "template_id", nullable = false, unique = true, length = 100)
    private String templateId;

    /**
     * Human-readable template name.
     */
    @Column(nullable = false, length = 200)
    private String name;

    /**
     * Description of the template.
     */
    @Column(length = 2000)
    private String description;

    /**
     * Dungeon theme (e.g., "Combat", "Puzzle", "Horror", "Sci-Fi").
     */
    @Column(nullable = false, length = 100)
    private String theme;

    /**
     * Minimum difficulty level (1-20).
     */
    @Column(name = "min_difficulty", nullable = false)
    private int minDifficulty;

    /**
     * Maximum difficulty level (1-20).
     */
    @Column(name = "max_difficulty", nullable = false)
    private int maxDifficulty;

    /**
     * Minimum number of rooms.
     */
    @Column(name = "min_room_count", nullable = false)
    private int minRoomCount;

    /**
     * Maximum number of rooms.
     */
    @Column(name = "max_room_count", nullable = false)
    private int maxRoomCount;

    /**
     * Comma-separated list of encounter types.
     */
    @Column(name = "encounter_types", length = 500)
    private String encounterTypes;

    /**
     * Comma-separated list of trap types.
     */
    @Column(name = "trap_types", length = 500)
    private String trapTypes;

    /**
     * Comma-separated list of treasure types.
     */
    @Column(name = "treasure_types", length = 500)
    private String treasureTypes;

    /**
     * Whether the template is active.
     */
    @Column(nullable = false)
    private boolean active = true;

    public DungeonInstanceTemplate() {
    }

    public DungeonInstanceTemplate(String templateId, String name, String description, String theme,
                                    int minDifficulty, int maxDifficulty, int minRoomCount, int maxRoomCount,
                                    String encounterTypes, String trapTypes, String treasureTypes) {
        this.templateId = templateId;
        this.name = name;
        this.description = description;
        this.theme = theme;
        this.minDifficulty = minDifficulty;
        this.maxDifficulty = maxDifficulty;
        this.minRoomCount = minRoomCount;
        this.maxRoomCount = maxRoomCount;
        this.encounterTypes = encounterTypes;
        this.trapTypes = trapTypes;
        this.treasureTypes = treasureTypes;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTemplateId() {
        return templateId;
    }

    public void setTemplateId(String templateId) {
        this.templateId = templateId;
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

    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }

    public int getMinDifficulty() {
        return minDifficulty;
    }

    public void setMinDifficulty(int minDifficulty) {
        this.minDifficulty = minDifficulty;
    }

    public int getMaxDifficulty() {
        return maxDifficulty;
    }

    public void setMaxDifficulty(int maxDifficulty) {
        this.maxDifficulty = maxDifficulty;
    }

    public int getMinRoomCount() {
        return minRoomCount;
    }

    public void setMinRoomCount(int minRoomCount) {
        this.minRoomCount = minRoomCount;
    }

    public int getMaxRoomCount() {
        return maxRoomCount;
    }

    public void setMaxRoomCount(int maxRoomCount) {
        this.maxRoomCount = maxRoomCount;
    }

    public String getEncounterTypes() {
        return encounterTypes;
    }

    public void setEncounterTypes(String encounterTypes) {
        this.encounterTypes = encounterTypes;
    }

    public String getTrapTypes() {
        return trapTypes;
    }

    public void setTrapTypes(String trapTypes) {
        this.trapTypes = trapTypes;
    }

    public String getTreasureTypes() {
        return treasureTypes;
    }

    public void setTreasureTypes(String treasureTypes) {
        this.treasureTypes = treasureTypes;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public String toString() {
        return "DungeonInstanceTemplate{id='" + templateId + "', name='" + name +
                "', theme='" + theme + "', difficulty=" + minDifficulty + "-" + maxDifficulty + "}";
    }
}
