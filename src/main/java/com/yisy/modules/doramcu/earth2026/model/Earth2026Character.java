package com.yisy.modules.doramcu.earth2026.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * JPA entity representing an Earth 2026 character.
 * <p>
 * A modern-day RPG character with six ability scores (STR, DEX, CON, INT, WIS, CHA)
 * that maps to game-specific stats when transitioning to other game galaxies.
 * Characters start on Earth and can transition to any game galaxy by walking through
 * a "door" and boarding their starting ship.
 * </p>
 * <p>
 * This character type is extensible to Cthulhu and D&D games that only need
 * the planet Earth and not the entire galaxy.
 * </p>
 */
@Entity
@Table(name = "earth2026_characters", indexes = {
        @Index(name = "idx_earth2026_character_name", columnList = "name")
})
public class Earth2026Character {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    /**
     * Reference to the owning Player (User). Used for galaxy transitions to link
     * the character back to the player in PlayerCharacterRegistry.
     * Null for AI-controlled characters.
     */
    @Column(name = "player_id")
    private Long playerId;

    @Column(nullable = false, length = 100)
    private String name;

    /**
     * Character's age in years.
     */
    @Column(nullable = false)
    private int age = 25;

    /**
     * Character's occupation on Earth (e.g., "Software Engineer", "Student", "Detective").
     */
    @Column(length = 100)
    private String occupation;

    /**
     * Character's background story.
     */
    @Column(columnDefinition = "TEXT")
    private String background;

    /**
     * Strength score (1-20).
     */
    @Column(name = "str_score", nullable = false)
    private int strScore = 10;

    /**
     * Dexterity score (1-20).
     */
    @Column(name = "dex_score", nullable = false)
    private int dexScore = 10;

    /**
     * Constitution score (1-20).
     */
    @Column(name = "con_score", nullable = false)
    private int conScore = 10;

    /**
     * Intelligence score (1-20).
     */
    @Column(name = "int_score", nullable = false)
    private int intScore = 10;

    /**
     * Wisdom score (1-20).
     */
    @Column(name = "wis_score", nullable = false)
    private int wisScore = 10;

    /**
     * Charisma score (1-20).
     */
    @Column(name = "cha_score", nullable = false)
    private int chaScore = 10;

    /**
     * Current hit points.
     */
    @Column(name = "hit_points", nullable = false)
    private int hitPoints;

    /**
     * Maximum hit points.
     */
    @Column(name = "max_hit_points", nullable = false)
    private int maxHitPoints;

    /**
     * Current experience points.
     */
    @Column(name = "experience_points", nullable = false)
    private int experiencePoints = 0;

    /**
     * Character's level (1-20).
     */
    @Column(nullable = false)
    private int level = 1;

    /**
     * Current location on Earth (e.g., "San Diego, CA", "New York, NY").
     */
    @Column(name = "earth_location", length = 200)
    private String earthLocation;

    /**
     * When the character arrived on Earth (real-world arrival date/time).
     */
    @Column(name = "arrival_date_time", nullable = false)
    private LocalDateTime arrivalDateTime;

    /**
     * Whether the character has supernatural abilities enabled (for Cthulhu/D&D extensions).
     */
    @Column(name = "supernatural_enabled", nullable = false)
    private boolean supernaturalEnabled = false;

    /**
     * Sanity score for Cthulhu-style horror scenarios (100 = fully sane, 0 = insane).
     * Only used when supernaturalEnabled is true.
     */
    @Column(name = "sanity_score")
    private Integer sanityScore;

    /**
     * The target game galaxy the character has selected to transition to.
     * Null while the character is still on Earth.
     */
    @Column(name = "target_galaxy", length = 100)
    private String targetGalaxy;

    /**
     * Whether the character has transitioned to another galaxy.
     */
    @Column(name = "has_transitioned", nullable = false)
    private boolean hasTransitioned = false;

    /**
     * Equipment JSON string.
     */
    @Column(columnDefinition = "TEXT")
    private String equipment;

    /**
     * Conditions JSON string.
     */
    @Column(columnDefinition = "JSON")
    private String conditions;

    /**
     * Character creation date.
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Last update date.
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Earth2026Character() {
    }

    public Earth2026Character(String name, int age, String occupation) {
        this.name = name;
        this.age = age;
        this.occupation = occupation;
        this.hitPoints = this.maxHitPoints = calculateMaxHitPoints();
        this.arrivalDateTime = LocalDateTime.now();
    }

    public Long getPlayerId() {
        return playerId;
    }

    public void setPlayerId(Long playerId) {
        this.playerId = playerId;
    }

    /**
     * Calculates maximum hit points based on CON score and level.
     */
    private int calculateMaxHitPoints() {
        return 10 + (conScore - 10) / 2;
    }

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

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getOccupation() {
        return occupation;
    }

    public void setOccupation(String occupation) {
        this.occupation = occupation;
    }

    public String getBackground() {
        return background;
    }

    public void setBackground(String background) {
        this.background = background;
    }

    public int getStrScore() {
        return strScore;
    }

    public void setStrScore(int strScore) {
        this.strScore = strScore;
    }

    public int getDexScore() {
        return dexScore;
    }

    public void setDexScore(int dexScore) {
        this.dexScore = dexScore;
    }

    public int getConScore() {
        return conScore;
    }

    public void setConScore(int conScore) {
        this.conScore = conScore;
    }

    public int getIntScore() {
        return intScore;
    }

    public void setIntScore(int intScore) {
        this.intScore = intScore;
    }

    public int getWisScore() {
        return wisScore;
    }

    public void setWisScore(int wisScore) {
        this.wisScore = wisScore;
    }

    public int getChaScore() {
        return chaScore;
    }

    public void setChaScore(int chaScore) {
        this.chaScore = chaScore;
    }

    public int getHitPoints() {
        return hitPoints;
    }

    public void setHitPoints(int hitPoints) {
        this.hitPoints = hitPoints;
    }

    public int getMaxHitPoints() {
        return maxHitPoints;
    }

    public void setMaxHitPoints(int maxHitPoints) {
        this.maxHitPoints = maxHitPoints;
    }

    public int getExperiencePoints() {
        return experiencePoints;
    }

    public void setExperiencePoints(int experiencePoints) {
        this.experiencePoints = experiencePoints;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public String getEarthLocation() {
        return earthLocation;
    }

    public void setEarthLocation(String earthLocation) {
        this.earthLocation = earthLocation;
    }

    public LocalDateTime getArrivalDateTime() {
        return arrivalDateTime;
    }

    public void setArrivalDateTime(LocalDateTime arrivalDateTime) {
        this.arrivalDateTime = arrivalDateTime;
    }

    public boolean isSupernaturalEnabled() {
        return supernaturalEnabled;
    }

    public void setSupernaturalEnabled(boolean supernaturalEnabled) {
        this.supernaturalEnabled = supernaturalEnabled;
    }

    public Integer getSanityScore() {
        return sanityScore;
    }

    public void setSanityScore(Integer sanityScore) {
        this.sanityScore = sanityScore;
    }

    public String getTargetGalaxy() {
        return targetGalaxy;
    }

    public void setTargetGalaxy(String targetGalaxy) {
        this.targetGalaxy = targetGalaxy;
    }

    public boolean isHasTransitioned() {
        return hasTransitioned;
    }

    public void setHasTransitioned(boolean hasTransitioned) {
        this.hasTransitioned = hasTransitioned;
    }

    public String getEquipment() {
        return equipment;
    }

    public void setEquipment(String equipment) {
        this.equipment = equipment;
    }

    public String getConditions() {
        return conditions;
    }

    public void setConditions(String conditions) {
        this.conditions = conditions;
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
        return "Earth2026Character{id=" + id + ", playerId=" + playerId + ", name='" + name + "', age=" + age +
                ", occupation='" + occupation + "', location='" + earthLocation +
                "', level=" + level + ", transitioned=" + hasTransitioned + "}";
    }
}
