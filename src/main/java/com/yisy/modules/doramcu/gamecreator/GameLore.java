package com.yisy.modules.doramcu.gamecreator;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * JPA entity representing a game lore/universe in the universal game database.
 * <p>
 * Each lore entry contains the metadata needed to create a themed game/galaxy,
 * including factions, technology level, tone, naming conventions, and suggested
 * game modes. Lore entries can be pre-loaded from the popular game lores database
 * or fetched dynamically from the internet when a new lore is requested.
 * </p>
 * <p>
 * Examples of lores: Star Wars, Star Trek, Warhammer 40K, Dune, Mass Effect,
 * Babylon 5, D&D Forgotten Realms, The Expanse, Halo, etc.
 * </p>
 */
@Entity
@Table(name = "game_lores", indexes = {
        @Index(name = "idx_game_lore_slug", columnList = "slug", unique = true),
        @Index(name = "idx_game_lore_genre", columnList = "genre"),
        @Index(name = "idx_game_lore_category", columnList = "category")
})
public class GameLore {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    /**
     * Human-readable name of the lore (e.g., "Star Wars", "Warhammer 40,000").
     */
    @Column(nullable = false)
    private String name;

    /**
     * URL-friendly unique identifier (e.g., "star-wars", "warhammer-40k").
     */
    @Column(nullable = false, unique = true)
    private String slug;

    /**
     * Short description of the lore universe (1-2 sentences).
     */
    @Column(length = 1000)
    private String shortDescription;

    /**
     * Detailed lore description including history, key events, and setting.
     */
    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * Genre of the lore: SCI_FI, FANTASY, HORROR, POST_APOCALYPTIC, STEAMPUNK, etc.
     */
    @Column(nullable = false)
    private String genre;

    /**
     * Broader category: SPACE_OPERA, DARK_FANTASY, MILITARY_SCI_FI, etc.
     */
    private String category;

    /**
     * Technology level: PRIMITIVE, MEDIEVAL, RENAISSANCE, INDUSTRIAL, MODERN,
     * NEAR_FUTURE, FAR_FUTURE, MAGIC, MIXED.
     */
    private String technologyLevel;

    /**
     * Tone of the universe: HOPEFUL, GRIMDARK, SATIRICAL, SERIOUS, ADVENTUROUS, etc.
     */
    private String tone;

    /**
     * JSON array of faction names in this universe.
     * e.g., ["Rebels", "Empire", "Mandalorians"] for Star Wars.
     */
    @Column(columnDefinition = "TEXT")
    private String factions;

    /**
     * JSON array of species/races in this universe.
     * e.g., ["Humans", "Zabrak", "Twi'lek"] for Star Wars.
     */
    @Column(columnDefinition = "TEXT")
    private String species;

    /**
     * Naming convention for celestial bodies: ASTRONOMICAL, MYTHOLOGICAL,
     * LORE_SPECIFIC, NUMERICAL, etc.
     */
    private String namingConvention;

    /**
     * Suggested galaxy shape: SPIRAL, ELLIPTICAL, IRREGULAR, RING, etc.
     */
    private String galaxyShape;

    /**
     * Suggested number of empires/factions for a typical game.
     */
    private Integer suggestedEmpireCount;

    /**
     * JSON array of recommended game modes for this lore.
     * e.g., ["RPG", "4X", "TACTICAL"] for Star Wars.
     */
    @Column(columnDefinition = "TEXT")
    private String recommendedGameModes;

    /**
     * Whether this lore is suitable for RPG gameplay.
     */
    private Boolean suitableForRpg;

    /**
     * Whether this lore is suitable for 4X gameplay.
     */
    private Boolean suitableFor4x;

    /**
     * Whether this lore is suitable for Tycoon gameplay.
     */
    private Boolean suitableForTycoon;

    /**
     * Whether this lore is suitable for Tactical gameplay.
     */
    private Boolean suitableForTactical;

    /**
     * Whether this lore is suitable for Survival gameplay.
     */
    private Boolean suitableForSurvival;

    /**
     * Whether this lore is suitable for Sandbox gameplay.
     */
    private Boolean suitableForSandbox;

    /**
     * Whether this lore is suitable for MMORPG gameplay.
     */
    private Boolean suitableForMmORPG;

    /**
     * Whether this lore is suitable for Roguelike gameplay.
     */
    private Boolean suitableForRoguelike;

    /**
     * Whether this lore is suitable for Simulation gameplay.
     */
    private Boolean suitableForSimulation;

    /**
     * Whether this lore is suitable for Adventure gameplay.
     */
    private Boolean suitableForAdventure;

    /**
     * Whether this lore is suitable for FPS gameplay.
     */
    private Boolean suitableForFps;

    /**
     * Source URL where this lore data was fetched from (for internet-sourced lores).
     */
    private String sourceUrl;

    /**
     * Whether this lore was fetched from the internet (true) or pre-loaded (false).
     */
    private Boolean internetSourced;

    /**
     * Whether this lore is actively available for game creation.
     */
    @Column(nullable = false)
    private Boolean active = true;

    /**
     * When this lore entry was created.
     */
    private LocalDateTime createdAt;

    /**
     * When this lore entry was last updated.
     */
    private LocalDateTime updatedAt;

    public GameLore() {
    }

    public GameLore(String name, String slug, String genre) {
        this.name = name;
        this.slug = slug;
        this.genre = genre;
        this.active = true;
        this.createdAt = LocalDateTime.now();
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

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getTechnologyLevel() {
        return technologyLevel;
    }

    public void setTechnologyLevel(String technologyLevel) {
        this.technologyLevel = technologyLevel;
    }

    public String getTone() {
        return tone;
    }

    public void setTone(String tone) {
        this.tone = tone;
    }

    public String getFactions() {
        return factions;
    }

    public void setFactions(String factions) {
        this.factions = factions;
    }

    public String getSpecies() {
        return species;
    }

    public void setSpecies(String species) {
        this.species = species;
    }

    public String getNamingConvention() {
        return namingConvention;
    }

    public void setNamingConvention(String namingConvention) {
        this.namingConvention = namingConvention;
    }

    public String getGalaxyShape() {
        return galaxyShape;
    }

    public void setGalaxyShape(String galaxyShape) {
        this.galaxyShape = galaxyShape;
    }

    public Integer getSuggestedEmpireCount() {
        return suggestedEmpireCount;
    }

    public void setSuggestedEmpireCount(Integer suggestedEmpireCount) {
        this.suggestedEmpireCount = suggestedEmpireCount;
    }

    public String getRecommendedGameModes() {
        return recommendedGameModes;
    }

    public void setRecommendedGameModes(String recommendedGameModes) {
        this.recommendedGameModes = recommendedGameModes;
    }

    public Boolean getSuitableForRpg() {
        return suitableForRpg;
    }

    public void setSuitableForRpg(Boolean suitableForRpg) {
        this.suitableForRpg = suitableForRpg;
    }

    public Boolean getSuitableFor4x() {
        return suitableFor4x;
    }

    public void setSuitableFor4x(Boolean suitableFor4x) {
        this.suitableFor4x = suitableFor4x;
    }

    public Boolean getSuitableForTycoon() {
        return suitableForTycoon;
    }

    public void setSuitableForTycoon(Boolean suitableForTycoon) {
        this.suitableForTycoon = suitableForTycoon;
    }

    public Boolean getSuitableForTactical() {
        return suitableForTactical;
    }

    public void setSuitableForTactical(Boolean suitableForTactical) {
        this.suitableForTactical = suitableForTactical;
    }

    public Boolean getSuitableForSurvival() {
        return suitableForSurvival;
    }

    public void setSuitableForSurvival(Boolean suitableForSurvival) {
        this.suitableForSurvival = suitableForSurvival;
    }

    public Boolean getSuitableForSandbox() {
        return suitableForSandbox;
    }

    public void setSuitableForSandbox(Boolean suitableForSandbox) {
        this.suitableForSandbox = suitableForSandbox;
    }

    public Boolean getSuitableForMmORPG() {
        return suitableForMmORPG;
    }

    public void setSuitableForMmORPG(Boolean suitableForMmORPG) {
        this.suitableForMmORPG = suitableForMmORPG;
    }

    public Boolean getSuitableForRoguelike() {
        return suitableForRoguelike;
    }

    public void setSuitableForRoguelike(Boolean suitableForRoguelike) {
        this.suitableForRoguelike = suitableForRoguelike;
    }

    public Boolean getSuitableForSimulation() {
        return suitableForSimulation;
    }

    public void setSuitableForSimulation(Boolean suitableForSimulation) {
        this.suitableForSimulation = suitableForSimulation;
    }

    public Boolean getSuitableForAdventure() {
        return suitableForAdventure;
    }

    public void setSuitableForAdventure(Boolean suitableForAdventure) {
        this.suitableForAdventure = suitableForAdventure;
    }

    public Boolean getSuitableForFps() {
        return suitableForFps;
    }

    public void setSuitableForFps(Boolean suitableForFps) {
        this.suitableForFps = suitableForFps;
    }

    public String getSourceUrl() {
        return sourceUrl;
    }

    public void setSourceUrl(String sourceUrl) {
        this.sourceUrl = sourceUrl;
    }

    public Boolean getInternetSourced() {
        return internetSourced;
    }

    public void setInternetSourced(Boolean internetSourced) {
        this.internetSourced = internetSourced;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
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

    @Override
    public String toString() {
        return "GameLore{id=" + id + ", name='" + name + "', slug='" + slug + "', genre='" + genre + "'}";
    }
}
