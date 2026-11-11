package com.yisy.modules.doramcu.dnd5e.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * JPA entity representing a character's fear and dread state in Ravenloft.
 * <p>
 * In the Domains of Dread, characters are affected by fear and dread mechanics.
 * Fear Level represents the character's current fear state, which can impose
 * penalties on d20 tests. Dread is a long-term corruption that increases
 * as characters spend time in a Domain of Dread.
 * </p>
 * <p>
 * Key mechanics:
 * - Fear Level: 0 (no fear) to 6 (paralyzed with fear)
 * - Dread Level: 0 to 100, representing corruption by the Domain
 * - Fear penalties: -1 to d20 tests per fear level
 * - Dread effects: At certain thresholds, dread causes physical/mental changes
 * - Phylactery: Darklords have phylacteries that anchor them to their domain
 * </p>
 */
@Entity
@Table(name = "dnd5e_fear_dread", indexes = {
        @Index(name = "idx_dnd5e_fear_dread_character", columnList = "character_id"),
        @Index(name = "idx_dnd5e_fear_dread_domain", columnList = "domain")
})
public class Dnd5eFearDread {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "character_id", nullable = false, unique = true)
    private Long characterId;

    @Column(nullable = false, length = 100)
    private String characterName;

    /**
     * Current fear level: 0 (no fear) to 6 (paralyzed with fear).
     */
    @Column(name = "fear_level", nullable = false)
    private int fearLevel = 0;

    /**
     * Maximum fear level the character can reach based on constitution.
     */
    @Column(name = "max_fear_level", nullable = false)
    private int maxFearLevel = 6;

    /**
     * Current dread level: 0 to 100, representing corruption by the Domain.
     */
    @Column(name = "dread_level", nullable = false)
    private int dreadLevel = 0;

    /**
     * The Domain of Dread the character is currently in.
     */
    @Column(length = 100)
    private String domain;

    /**
     * The Darklord of the current domain.
     */
    @Column(name = "darklord", length = 100)
    private String darklord;

    /**
     * Whether the character is currently frightened (condition).
     */
    @Column(name = "is_frightened", nullable = false)
    private boolean isFrightened = false;

    /**
     * Number of times the character has succeeded on a fear check.
     */
    @Column(name = "fear_checks_succeeded", nullable = false)
    private int fearChecksSucceeded = 0;

    /**
     * Number of times the character has failed on a fear check.
     */
    @Column(name = "fear_checks_failed", nullable = false)
    private int fearChecksFailed = 0;

    /**
     * Dread threshold effects that have triggered.
     */
    @Column(name = "dread_effects", columnDefinition = "TEXT")
    private String dreadEffects;

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

    public int getFearLevel() {
        return fearLevel;
    }

    public void setFearLevel(int fearLevel) {
        this.fearLevel = Math.max(0, Math.min(fearLevel, maxFearLevel));
    }

    public int getMaxFearLevel() {
        return maxFearLevel;
    }

    public void setMaxFearLevel(int maxFearLevel) {
        this.maxFearLevel = maxFearLevel;
    }

    public int getDreadLevel() {
        return dreadLevel;
    }

    public void setDreadLevel(int dreadLevel) {
        this.dreadLevel = Math.max(0, Math.min(dreadLevel, 100));
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getDarklord() {
        return darklord;
    }

    public void setDarklord(String darklord) {
        this.darklord = darklord;
    }

    public boolean isFrightened() {
        return isFrightened;
    }

    public void setFrightened(boolean frightened) {
        isFrightened = frightened;
    }

    public int getFearChecksSucceeded() {
        return fearChecksSucceeded;
    }

    public void setFearChecksSucceeded(int fearChecksSucceeded) {
        this.fearChecksSucceeded = fearChecksSucceeded;
    }

    public int getFearChecksFailed() {
        return fearChecksFailed;
    }

    public void setFearChecksFailed(int fearChecksFailed) {
        this.fearChecksFailed = fearChecksFailed;
    }

    public String getDreadEffects() {
        return dreadEffects;
    }

    public void setDreadEffects(String dreadEffects) {
        this.dreadEffects = dreadEffects;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    @Override
    public String toString() {
        return "Dnd5eFearDread{character='" + characterName + "', fearLevel=" + fearLevel +
                ", dreadLevel=" + dreadLevel + ", domain='" + domain + "'}";
    }
}
