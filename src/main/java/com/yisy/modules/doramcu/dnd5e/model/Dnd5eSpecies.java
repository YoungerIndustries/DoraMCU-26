package com.yisy.modules.doramcu.dnd5e.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * JPA entity representing a D&D 5e species (race).
 * <p>
 * A species defines the racial traits, ability score increases, size, speed,
 * lifespan, and name database for a D&D 5e race. This follows the Earth 2026
 * species database pattern, extending it to D&D 5e races with 2024 PHB rules.
 * </p>
 * <p>
 * Key mechanics:
 * - Ability score increases: fixed or custom increases per race
 * - Size: Tiny, Small, Medium, Large (affects equipment and space)
 * - Speed: base walking speed in feet
 * - Lifespan: average lifespan in years
 * - Darkvision: range in feet (0 if none)
 * - Racial traits: JSON array of trait names
 * - Name database: male/female first names, last names, title prefixes/suffixes
 * - Language: default languages for the race
 * </p>
 * <p>
 * Supported races (2024 PHB):
 * - Human, Elf, Dwarf, Halfling, Dragonborn, Gnome, Half-Elf, Half-Orc, Tiefling
 * - Aasimar, Genasi (Air, Earth, Fire, Water), Tabaxi, Tortle, Triton, Kenku
 * - Bugbear, Goliath, Hobgoblin, Lizardfolk, Satyr, Firbolg, Horned Dragonborn
 * </p>
 */
@Entity
@Table(name = "dnd5e_species", indexes = {
        @Index(name = "idx_dnd5e_species_name", columnList = "name", unique = true),
        @Index(name = "idx_dnd5e_species_size", columnList = "size"),
        @Index(name = "idx_dnd5e_species_category", columnList = "category")
})
public class Dnd5eSpecies {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    /**
     * Name of the species (e.g., "Human", "Elf", "Dwarf").
     */
    @Column(nullable = false, length = 100, unique = true)
    private String name;

    /**
     * Subspecies or subrace name (e.g., "High Elf", "Hill Dwarf", or null for base race).
     */
    @Column(name = "subspecies", length = 100)
    private String subspecies;

    /**
     * Description of the species.
     */
    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * Category of the species (e.g., "Core", "TCE", "FMT", "VGtM").
     */
    @Column(nullable = false, length = 20)
    private String category = "Core";

    /**
     * Size of the species: Tiny, Small, Medium, Large.
     */
    @Column(nullable = false, length = 10)
    private String size = "Medium";

    /**
     * Base walking speed in feet.
     */
    @Column(name = "base_speed", nullable = false)
    private int baseSpeed = 30;

    /**
     * Average lifespan in years.
     */
    @Column(name = "lifespan", nullable = false)
    private int lifespan = 100;

    /**
     * Darkvision range in feet (0 if none).
     */
    @Column(name = "darkvision_range", nullable = false)
    private int darkvisionRange = 0;

    /**
     * Ability score increases as JSON (e.g., {"STR": 2, "CON": 1}).
     */
    @Column(name = "ability_increases", columnDefinition = "JSON")
    private String abilityIncreases = "{}";

    /**
     * Racial traits as JSON array (e.g., ["Darkvision", "Fey Ancestry", "Trance"]).
     */
    @Column(name = "racial_traits", columnDefinition = "JSON")
    private String racialTraits = "[]";

    /**
     * Default languages for the race (e.g., "Common, Elvish").
     */
    @Column(length = 200)
    private String languages = "Common";

    // --- Name Database ---

    /**
     * Male first names as JSON array.
     */
    @Column(name = "male_first_names", columnDefinition = "JSON")
    private String maleFirstNames = "[]";

    /**
     * Female first names as JSON array.
     */
    @Column(name = "female_first_names", columnDefinition = "JSON")
    private String femaleFirstNames = "[]";

    /**
     * Last names as JSON array.
     */
    @Column(name = "last_names", columnDefinition = "JSON")
    private String lastNames = "[]";

    /**
     * Title prefixes as JSON array (e.g., ["Lord", "Lady", "Sir"]).
     */
    @Column(name = "title_prefixes", columnDefinition = "JSON")
    private String titlePrefixes = "[]";

    /**
     * Title suffixes as JSON array (e.g., ["the Brave", "the Wise"]).
     */
    @Column(name = "title_suffixes", columnDefinition = "JSON")
    private String titleSuffixes = "[]";

    /**
     * AI leader titles as JSON array.
     */
    @Column(name = "ai_leader_titles", columnDefinition = "JSON")
    private String aiLeaderTitles = "[]";

    /**
     * AI governor titles as JSON array.
     */
    @Column(name = "ai_governor_titles", columnDefinition = "JSON")
    private String aiGovernorTitles = "[]";

    /**
     * AI captain titles as JSON array.
     */
    @Column(name = "ai_captain_titles", columnDefinition = "JSON")
    private String aiCaptainTitles = "[]";

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

    public String getSubspecies() {
        return subspecies;
    }

    public void setSubspecies(String subspecies) {
        this.subspecies = subspecies;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public int getBaseSpeed() {
        return baseSpeed;
    }

    public void setBaseSpeed(int baseSpeed) {
        this.baseSpeed = Math.max(10, baseSpeed);
    }

    public int getLifespan() {
        return lifespan;
    }

    public void setLifespan(int lifespan) {
        this.lifespan = Math.max(1, lifespan);
    }

    public int getDarkvisionRange() {
        return darkvisionRange;
    }

    public void setDarkvisionRange(int darkvisionRange) {
        this.darkvisionRange = Math.max(0, darkvisionRange);
    }

    public String getAbilityIncreases() {
        return abilityIncreases;
    }

    public void setAbilityIncreases(String abilityIncreases) {
        this.abilityIncreases = abilityIncreases;
    }

    public String getRacialTraits() {
        return racialTraits;
    }

    public void setRacialTraits(String racialTraits) {
        this.racialTraits = racialTraits;
    }

    public String getLanguages() {
        return languages;
    }

    public void setLanguages(String languages) {
        this.languages = languages;
    }

    public String getMaleFirstNames() {
        return maleFirstNames;
    }

    public void setMaleFirstNames(String maleFirstNames) {
        this.maleFirstNames = maleFirstNames;
    }

    public String getFemaleFirstNames() {
        return femaleFirstNames;
    }

    public void setFemaleFirstNames(String femaleFirstNames) {
        this.femaleFirstNames = femaleFirstNames;
    }

    public String getLastNames() {
        return lastNames;
    }

    public void setLastNames(String lastNames) {
        this.lastNames = lastNames;
    }

    public String getTitlePrefixes() {
        return titlePrefixes;
    }

    public void setTitlePrefixes(String titlePrefixes) {
        this.titlePrefixes = titlePrefixes;
    }

    public String getTitleSuffixes() {
        return titleSuffixes;
    }

    public void setTitleSuffixes(String titleSuffixes) {
        this.titleSuffixes = titleSuffixes;
    }

    public String getAiLeaderTitles() {
        return aiLeaderTitles;
    }

    public void setAiLeaderTitles(String aiLeaderTitles) {
        this.aiLeaderTitles = aiLeaderTitles;
    }

    public String getAiGovernorTitles() {
        return aiGovernorTitles;
    }

    public void setAiGovernorTitles(String aiGovernorTitles) {
        this.aiGovernorTitles = aiGovernorTitles;
    }

    public String getAiCaptainTitles() {
        return aiCaptainTitles;
    }

    public void setAiCaptainTitles(String aiCaptainTitles) {
        this.aiCaptainTitles = aiCaptainTitles;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    @Override
    public String toString() {
        return "Dnd5eSpecies{name='" + name + "', size='" + size + "', category='" + category + "'}";
    }
}
