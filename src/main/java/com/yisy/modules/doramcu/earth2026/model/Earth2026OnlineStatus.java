package com.yisy.modules.doramcu.earth2026.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * JPA entity representing the online status of an Earth 2026 character.
 * <p>
 * Tracks whether a character is currently online, their location, activity,
 * and last seen time. This enables real-time presence awareness in the MMOG.
 * </p>
 * <p>
 * Status is updated via heartbeat; characters not seen for over 5 minutes
 * are considered offline.
 * </p>
 */
@Entity
@Table(name = "earth2026_online_status", indexes = {
        @Index(name = "idx_earth2026_status_character", columnList = "characterId", unique = true),
        @Index(name = "idx_earth2026_status_online", columnList = "isOnline"),
        @Index(name = "idx_earth2026_status_location", columnList = "locationId")
})
public class Earth2026OnlineStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    /**
     * The character this status belongs to.
     */
    @Column(name = "character_id", nullable = false, unique = true)
    private Long characterId;

    /**
     * Whether the character is currently online.
     */
    @Column(name = "is_online", nullable = false)
    private boolean isOnline = false;

    /**
     * The character's current Earth location while online.
     */
    @Column(name = "location_id")
    private Long locationId;

    /**
     * Current activity (e.g., "idle", "exploring", "in_scenario", "chatting").
     */
    @Column(length = 50)
    private String activity = "idle";

    /**
     * When the character last sent a heartbeat.
     */
    @Column(name = "last_seen_at")
    private LocalDateTime lastSeenAt;

    /**
     * When the character went online.
     */
    @Column(name = "connected_at")
    private LocalDateTime connectedAt;

    /**
     * When this status record was last updated.
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Earth2026OnlineStatus() {
    }

    public Earth2026OnlineStatus(Long characterId) {
        this.characterId = characterId;
        this.updatedAt = LocalDateTime.now();
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

    public boolean isOnline() {
        return isOnline;
    }

    public void setOnline(boolean online) {
        isOnline = online;
    }

    public Long getLocationId() {
        return locationId;
    }

    public void setLocationId(Long locationId) {
        this.locationId = locationId;
    }

    public String getActivity() {
        return activity;
    }

    public void setActivity(String activity) {
        this.activity = activity;
    }

    public LocalDateTime getLastSeenAt() {
        return lastSeenAt;
    }

    public void setLastSeenAt(LocalDateTime lastSeenAt) {
        this.lastSeenAt = lastSeenAt;
    }

    public LocalDateTime getConnectedAt() {
        return connectedAt;
    }

    public void setConnectedAt(LocalDateTime connectedAt) {
        this.connectedAt = connectedAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    /**
     * Check if the character has been stale (not seen for over 5 minutes).
     *
     * @param now the current time
     * @return true if the character should be considered offline
     */
    public boolean isStale(LocalDateTime now) {
        if (lastSeenAt == null) {
            return true;
        }
        return lastSeenAt.isBefore(now.minusMinutes(5));
    }

    @Override
    public String toString() {
        return "Earth2026OnlineStatus{" +
                "id=" + id +
                ", characterId=" + characterId +
                ", isOnline=" + isOnline +
                ", activity='" + activity + '\'' +
                '}';
    }
}
