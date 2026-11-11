package com.yisy.modules.doramcu.earth2026.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * JPA entity representing a party of Earth 2026 characters.
 * <p>
 * Parties allow multiple characters at the same location to group together
 * for scenarios and adventures. Characters must be at the same Earth location
 * to form a party.
 * </p>
 */
@Entity
@Table(name = "earth2026_parties", indexes = {
        @Index(name = "idx_earth2026_party_name", columnList = "name"),
        @Index(name = "idx_earth2026_party_location", columnList = "locationId")
})
public class Earth2026Party {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    /**
     * Party name (e.g., "The San Diego Squad", "Area 51 Investigators").
     */
    @Column(nullable = false, length = 100)
    private String name;

    /**
     * Description of the party's purpose or mission.
     */
    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * The Earth location where the party is currently gathered.
     */
    @Column(name = "location_id", nullable = false)
    private Long locationId;

    /**
     * The leader of the party.
     */
    @Column(name = "leader_id", nullable = false)
    private Long leaderId;

    /**
     * Maximum number of members allowed in the party.
     */
    @Column(name = "max_members", nullable = false)
    private int maxMembers = 6;

    /**
     * Party status (active, disbanded, on_scenario).
     */
    @Column(nullable = false, length = 20)
    private String status = "active";

    /**
     * The current scenario the party is working on, if any.
     */
    @Column(name = "scenario_id")
    private Long scenarioId;

    /**
     * When the party was created.
     */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /**
     * When the party was last updated.
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Whether the party is open to new members.
     */
    @Column(name = "is_recruiting", nullable = false)
    private boolean isRecruiting = true;

    public Earth2026Party() {
    }

    public Earth2026Party(String name, Long locationId, Long leaderId) {
        this.name = name;
        this.locationId = locationId;
        this.leaderId = leaderId;
        this.createdAt = LocalDateTime.now();
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getLocationId() {
        return locationId;
    }

    public void setLocationId(Long locationId) {
        this.locationId = locationId;
    }

    public Long getLeaderId() {
        return leaderId;
    }

    public void setLeaderId(Long leaderId) {
        this.leaderId = leaderId;
    }

    public int getMaxMembers() {
        return maxMembers;
    }

    public void setMaxMembers(int maxMembers) {
        this.maxMembers = maxMembers;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getScenarioId() {
        return scenarioId;
    }

    public void setScenarioId(Long scenarioId) {
        this.scenarioId = scenarioId;
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

    public boolean isRecruiting() {
        return isRecruiting;
    }

    public void setRecruiting(boolean recruiting) {
        isRecruiting = recruiting;
    }

    @Override
    public String toString() {
        return "Earth2026Party{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", locationId=" + locationId +
                ", leaderId=" + leaderId +
                ", status='" + status + '\'' +
                '}';
    }
}
