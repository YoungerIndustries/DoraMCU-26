package com.yisy.modules.doramcu.earth2026.model;
import com.yisy.modules.doramcu.model.galaxy.Galaxy;


import com.yisy.modules.doramcu.gamecore.GameType;
import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * JPA entity representing a portal travel event from the Younger Industries HQ
 * to another game galaxy.
 * <p>
 * Each record tracks which character used which portal, when they traveled,
 * the energy cost paid, and the current travel status (completed, in cooldown, etc.).
 * This enables cooldown enforcement, travel history, and corporation resource tracking.
 * </p>
 */
@Entity
@Table(name = "portal_travel", indexes = {
        @Index(name = "idx_pt_character", columnList = "character_id"),
        @Index(name = "idx_pt_portal", columnList = "portal_id"),
        @Index(name = "idx_pt_corporation", columnList = "corporation_id"),
        @Index(name = "idx_pt_status", columnList = "status"),
        @Index(name = "idx_pt_traveled_at", columnList = "traveled_at")
})
public class PortalTravel {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "earth2026_seq")
    @SequenceGenerator(name = "earth2026_seq", sequenceName = "earth2026_sequence", allocationSize = 1)
    private Long id;

    /**
     * The Earth 2026 character who traveled through the portal.
     */
    @Column(name = "character_id", nullable = false)
    private Long characterId;

    /**
     * The portal used for travel.
     */
    @Column(name = "portal_id", nullable = false)
    private Long portalId;

    /**
     * The corporation that paid for the travel (if applicable).
     */
    @Column(name = "corporation_id")
    private Long corporationId;

    /**
     * Destination game system.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "destination_game", nullable = false, length = 50)
    private GameType destinationGame;

    /**
     * Energy cost in credits paid for this travel.
     */
    @Column(name = "energy_cost", nullable = false)
    private long energyCost;

    /**
     * Travel status: COMPLETED, IN_PROGRESS, COOLDOWN, FAILED.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private TravelStatus status = TravelStatus.COMPLETED;

    /**
     * When the character traveled through the portal.
     */
    @Column(name = "traveled_at", nullable = false)
    private LocalDateTime traveledAt;

    /**
     * When the cooldown expires and the character can travel again.
     */
    @Column(name = "cooldown_expires_at")
    private LocalDateTime cooldownExpiresAt;

    /**
     * Optional notes about the travel (e.g., reason for travel, mission ID).
     */
    @Column(name = "travel_notes", columnDefinition = "TEXT")
    private String travelNotes;

    /**
     * When this record was created.
     */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public enum TravelStatus {
        COMPLETED("Completed"),
        IN_PROGRESS("In Progress"),
        COOLDOWN("On Cooldown"),
        FAILED("Failed");

        private final String displayName;

        TravelStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public PortalTravel() {
    }

    public PortalTravel(Long characterId, Long portalId, GameType destinationGame, long energyCost) {
        this.characterId = characterId;
        this.portalId = portalId;
        this.destinationGame = destinationGame;
        this.energyCost = energyCost;
        this.status = TravelStatus.COMPLETED;
        this.traveledAt = LocalDateTime.now();
        this.createdAt = LocalDateTime.now();
    }

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

    public Long getPortalId() {
        return portalId;
    }

    public void setPortalId(Long portalId) {
        this.portalId = portalId;
    }

    public Long getCorporationId() {
        return corporationId;
    }

    public void setCorporationId(Long corporationId) {
        this.corporationId = corporationId;
    }

    public GameType getDestinationGame() {
        return destinationGame;
    }

    public void setDestinationGame(GameType destinationGame) {
        this.destinationGame = destinationGame;
    }

    public long getEnergyCost() {
        return energyCost;
    }

    public void setEnergyCost(long energyCost) {
        this.energyCost = energyCost;
    }

    public TravelStatus getStatus() {
        return status;
    }

    public void setStatus(TravelStatus status) {
        this.status = status;
    }

    public LocalDateTime getTraveledAt() {
        return traveledAt;
    }

    public void setTraveledAt(LocalDateTime traveledAt) {
        this.traveledAt = traveledAt;
    }

    public LocalDateTime getCooldownExpiresAt() {
        return cooldownExpiresAt;
    }

    public void setCooldownExpiresAt(LocalDateTime cooldownExpiresAt) {
        this.cooldownExpiresAt = cooldownExpiresAt;
    }

    public String getTravelNotes() {
        return travelNotes;
    }

    public void setTravelNotes(String travelNotes) {
        this.travelNotes = travelNotes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "PortalTravel{id=" + id + ", characterId=" + characterId + ", portalId=" + portalId +
                ", destination=" + destinationGame + ", status=" + status + ", traveledAt=" + traveledAt + "}";
    }
}
