package com.yisy.modules.doramcu.dnd5e.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * JPA entity representing a Psion character's psionic abilities.
 * <p>
 * The Psion is a new class in D&D 2024 that uses psionic power points instead of
 * spell slots. Psions have four subclasses: Psi Warper, Metamorph, Psykinetic, and Telepath.
 * They can use psionic powers, gain psionic surges, and develop psionic abilities
 * as they level up.
 * </p>
 * <p>
 * Key mechanics:
 * - Power Points: Used to fuel psionic powers instead of spell slots
 * - Psionic Surge: A class feature that enhances psionic effects
 * - Subclass abilities: Each subclass grants unique psionic powers
 * - Psionic training: Psions can learn new powers through study
 * </p>
 */
@Entity
@Table(name = "dnd5e_psions", indexes = {
        @Index(name = "idx_dnd5e_psion_character", columnList = "character_id"),
        @Index(name = "idx_dnd5e_psion_subclass", columnList = "subclass")
})
public class Dnd5ePsion {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "character_id", nullable = false, unique = true)
    private Long characterId;

    @Column(nullable = false, length = 100)
    private String characterName;

    /**
     * The psion's subclass: Psi Warper, Metamorph, Psykinetic, or Telepath.
     */
    @Column(nullable = false, length = 50)
    private String subclass;

    /**
     * The character's level as a psion (may differ from overall character level).
     */
    @Column(name = "psion_level", nullable = false)
    private int psionLevel = 1;

    /**
     * Current psionic power points available.
     */
    @Column(name = "power_points", nullable = false)
    private int powerPoints = 0;

    /**
     * Maximum psionic power points based on level and subclass.
     */
    @Column(name = "max_power_points", nullable = false)
    private int maxPowerPoints = 0;

    /**
     * Current psionic surge points available.
     */
    @Column(name = "surge_points", nullable = false)
    private int surgePoints = 0;

    /**
     * Maximum psionic surge points based on level.
     */
    @Column(name = "max_surge_points", nullable = false)
    private int maxSurgePoints = 0;

    /**
     * The psion's primary ability score (INT or WIS).
     */
    @Column(name = "primary_ability", nullable = false, length = 10)
    private String primaryAbility = "INT";

    /**
     * Number of known psionic powers.
     */
    @Column(name = "known_powers", nullable = false)
    private int knownPowers = 0;

    /**
     * Whether the psion is currently in a psionic surge state.
     */
    @Column(name = "is_surge_active", nullable = false)
    private boolean isSurgeActive = false;

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

    public Long getCharacterId() {
        return characterId;
    }

    public void setCharacterId(Long characterId) {
        this.characterId = characterId;
    }

    public String getCharacterName() {
        return characterName;
    }

    public void setCharacterName(String characterName) {
        this.characterName = characterName;
    }

    public String getSubclass() {
        return subclass;
    }

    public void setSubclass(String subclass) {
        this.subclass = subclass;
    }

    public int getPsionLevel() {
        return psionLevel;
    }

    public void setPsionLevel(int psionLevel) {
        this.psionLevel = psionLevel;
    }

    public int getPowerPoints() {
        return powerPoints;
    }

    public void setPowerPoints(int powerPoints) {
        this.powerPoints = Math.max(0, Math.min(powerPoints, maxPowerPoints));
    }

    public int getMaxPowerPoints() {
        return maxPowerPoints;
    }

    public void setMaxPowerPoints(int maxPowerPoints) {
        this.maxPowerPoints = maxPowerPoints;
    }

    public int getSurgePoints() {
        return surgePoints;
    }

    public void setSurgePoints(int surgePoints) {
        this.surgePoints = Math.max(0, Math.min(surgePoints, maxSurgePoints));
    }

    public int getMaxSurgePoints() {
        return maxSurgePoints;
    }

    public void setMaxSurgePoints(int maxSurgePoints) {
        this.maxSurgePoints = maxSurgePoints;
    }

    public String getPrimaryAbility() {
        return primaryAbility;
    }

    public void setPrimaryAbility(String primaryAbility) {
        this.primaryAbility = primaryAbility;
    }

    public int getKnownPowers() {
        return knownPowers;
    }

    public void setKnownPowers(int knownPowers) {
        this.knownPowers = knownPowers;
    }

    public boolean isSurgeActive() {
        return isSurgeActive;
    }

    public void setSurgeActive(boolean surgeActive) {
        isSurgeActive = surgeActive;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    @Override
    public String toString() {
        return "Dnd5ePsion{character='" + characterName + "', subclass='" + subclass +
                "', level=" + psionLevel + ", powerPoints=" + powerPoints + "/" + maxPowerPoints +
                ", surgePoints=" + surgePoints + "/" + maxSurgePoints + "}";
    }
}
