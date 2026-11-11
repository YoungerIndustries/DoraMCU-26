package com.yisy.modules.doramcu.dnd5e.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * JPA entity representing a D&D 5e Adventure Hook.
 * <p>
 * Adventure hooks are personalized story starters that tie into a character's
 * background, race, class, or motivations. They provide the DM with compelling
 * reasons for a specific character to become involved in an adventure.
 * </p>
 * <p>
 * Key mechanics:
 * - Type: Personal, Political, Mystery, Combat, Exploration, Social, Supernatural
 * - Theme: Horror, High Magic, Sandbox, Dungeon Crawl, Feywild, Steampunk, etc.
 * - Urgency: Immediate, Soon, Eventual (how quickly the hook demands action)
 * - Target Background: Links to specific backgrounds (Soldier, Sage, Criminal, etc.)
 * - Target Race: Links to specific races (Elf, Dwarf, Dragonborn, etc.)
 * - Target Class: Links to specific classes (Wizard, Cleric, Rogue, etc.)
 * </p>
 */
@Entity
@Table(name = "dnd5e_adventure_hooks", indexes = {
        @Index(name = "idx_dnd5e_adventure_hook_name", columnList = "name"),
        @Index(name = "idx_dnd5e_adventure_hook_type", columnList = "type"),
        @Index(name = "idx_dnd5e_adventure_hook_theme", columnList = "theme"),
        @Index(name = "idx_dnd5e_adventure_hook_urgency", columnList = "urgency")
})
public class Dnd5eAdventureHook {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    /**
     * Name or title of the adventure hook.
     */
    @Column(nullable = false, length = 200)
    private String name;

    /**
     * Type of hook: Personal, Political, Mystery, Combat, Exploration, Social, Supernatural.
     */
    @Column(nullable = false, length = 50)
    private String type;

    /**
     * Theme of the hook: Horror, High Magic, Sandbox, Dungeon Crawl, Feywild, Steampunk, etc.
     */
    @Column(nullable = false, length = 50)
    private String theme;

    /**
     * Urgency level: Immediate, Soon, Eventual.
     */
    @Column(nullable = false, length = 20)
    private String urgency;

    /**
     * Target background for this hook (e.g., Soldier, Sage, Criminal). Null means any background.
     */
    @Column(name = "target_background", length = 50)
    private String targetBackground;

    /**
     * Target race for this hook (e.g., Elf, Dwarf, Dragonborn). Null means any race.
     */
    @Column(name = "target_race", length = 50)
    private String targetRace;

    /**
     * Target class for this hook (e.g., Wizard, Cleric, Rogue). Null means any class.
     */
    @Column(name = "target_class", length = 50)
    private String targetClass;

    /**
     * The hook's narrative text — the story starter presented to the player.
     */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String narrative;

    /**
     * DM notes about the hook — secrets, connections, and follow-up leads.
     */
    @Column(columnDefinition = "TEXT")
    private String dmNotes;

    /**
     * Suggested minimum character level for this hook.
     */
    @Column(name = "min_level", nullable = false)
    private int minLevel = 1;

    /**
     * Suggested maximum character level for this hook.
     */
    @Column(name = "max_level", nullable = false)
    private int maxLevel = 20;

    /**
     * Whether this hook is active/available for generation.
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }

    public String getUrgency() {
        return urgency;
    }

    public void setUrgency(String urgency) {
        this.urgency = urgency;
    }

    public String getTargetBackground() {
        return targetBackground;
    }

    public void setTargetBackground(String targetBackground) {
        this.targetBackground = targetBackground;
    }

    public String getTargetRace() {
        return targetRace;
    }

    public void setTargetRace(String targetRace) {
        this.targetRace = targetRace;
    }

    public String getTargetClass() {
        return targetClass;
    }

    public void setTargetClass(String targetClass) {
        this.targetClass = targetClass;
    }

    public String getNarrative() {
        return narrative;
    }

    public void setNarrative(String narrative) {
        this.narrative = narrative;
    }

    public String getDmNotes() {
        return dmNotes;
    }

    public void setDmNotes(String dmNotes) {
        this.dmNotes = dmNotes;
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
        return "Dnd5eAdventureHook{name='" + name + "', type='" + type + "', theme='" + theme + "', urgency='" + urgency + "'}";
    }
}
