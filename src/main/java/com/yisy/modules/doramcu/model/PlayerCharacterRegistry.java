package com.yisy.modules.doramcu.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.PrePersist;
import com.yisy.modules.doramcu.neuron.service.CdcEntityListener;

import java.time.LocalDateTime;

/**
 * Central registry linking a player to all their characters across game systems.
 * <p>
 * Each entry tracks the character type, reference ID, galaxy, role, and AI control state.
 * This enables unified character management and crew AI travel through wormholes.
 * </p>
 */
@Entity
@Table(name = "player_character_registry", indexes = {
        @Index(name = "idx_pcr_player", columnList = "player_id"),
        @Index(name = "idx_pcr_galaxy", columnList = "galaxy_name"),
        @Index(name = "idx_pcr_role", columnList = "character_role")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uq_pcr_player_character", columnNames = {"player_id", "character_type", "character_id"})
})
@EntityListeners(CdcEntityListener.class)
public class PlayerCharacterRegistry {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    /** The owner player. */
    @ManyToOne
    @JoinColumn(name = "player_id", nullable = false)
    private User player;

    /** Type of character: TRAVELLER, CAPTAINS_LOG, or AI_CREW. */
    @Enumerated(EnumType.STRING)
    @Column(name = "character_type", nullable = false, length = 20)
    private CharacterType characterType;

    /** Reference to the actual character (DoraTCharacter.id, CaptainLogCharacter.id, or CaptainLogCrewMember.id). */
    @Column(name = "character_id", nullable = false)
    private Long characterId;

    /** Denormalized name for quick lookup. */
    @Column(name = "character_name", nullable = false, length = 100)
    private String characterName;

    /** Relationship: COMMANDER, CREW, OWNED, ENCOUNTERED. */
    @Enumerated(EnumType.STRING)
    @Column(name = "character_role", nullable = false, length = 20)
    private CharacterRole characterRole;

    /** Which galaxy this character is in. */
    @Column(name = "galaxy_name", nullable = false, length = 100)
    private String galaxyName;

    /** Whether the character is currently AI-managed. */
    @Column(name = "is_ai_controlled", nullable = false)
    private boolean isAiControlled = false;

    /** Home location reference (station ID, planet ID, etc.). */
    @Column(name = "home_location_id", length = 100)
    private String homeLocationId;

    /** Home location type (APARTMENT, CREW_QUARTERS, etc.). */
    @Column(name = "home_location_type", length = 30)
    private String homeLocationType;

    /** Creation timestamp. */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public PlayerCharacterRegistry() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getPlayer() {
        return player;
    }

    public void setPlayer(User player) {
        this.player = player;
    }

    public CharacterType getCharacterType() {
        return characterType;
    }

    public void setCharacterType(CharacterType characterType) {
        this.characterType = characterType;
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

    public CharacterRole getCharacterRole() {
        return characterRole;
    }

    public void setCharacterRole(CharacterRole characterRole) {
        this.characterRole = characterRole;
    }

    public String getGalaxyName() {
        return galaxyName;
    }

    public void setGalaxyName(String galaxyName) {
        this.galaxyName = galaxyName;
    }

    public boolean isAiControlled() {
        return isAiControlled;
    }

    public void setAiControlled(boolean aiControlled) {
        isAiControlled = aiControlled;
    }

    public String getHomeLocationId() {
        return homeLocationId;
    }

    public void setHomeLocationId(String homeLocationId) {
        this.homeLocationId = homeLocationId;
    }

    public String getHomeLocationType() {
        return homeLocationType;
    }

    public void setHomeLocationType(String homeLocationType) {
        this.homeLocationType = homeLocationType;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "PlayerCharacterRegistry{" +
                "id=" + id +
                ", player=" + (player != null ? player.getUsername() : "null") +
                ", type=" + characterType +
                ", charId=" + characterId +
                ", name='" + characterName + '\'' +
                ", role=" + characterRole +
                ", galaxy='" + galaxyName + '\'' +
                '}';
    }
}
