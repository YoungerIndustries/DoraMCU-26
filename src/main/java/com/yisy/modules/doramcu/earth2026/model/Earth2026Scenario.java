package com.yisy.modules.doramcu.earth2026.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * JPA entity representing a scenario in the Earth 2026 game.
 * <p>
 * Scenarios are modern-day adventures, mysteries, investigations, or supernatural events
 * that characters can undertake on Earth before transitioning to another game galaxy.
 * Scenarios can be extended to Cthulhu-style horror or D&D-style fantasy elements.
 * </p>
 */
@Entity
@Table(name = "earth2026_scenarios", indexes = {
        @Index(name = "idx_earth2026_scenario_name", columnList = "name"),
        @Index(name = "idx_earth2026_scenario_status", columnList = "status")
})
public class Earth2026Scenario {

    /**
     * Scenario status.
     */
    public enum ScenarioStatus {
        /** Scenario is available to start. */
        AVAILABLE,
        /** Scenario is currently in progress. */
        IN_PROGRESS,
        /** Scenario has been completed. */
        COMPLETED,
        /** Scenario has failed. */
        FAILED,
        /** Scenario is locked and requires prerequisites. */
        LOCKED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    /**
     * Scenario name (e.g., "The Missing Artifact", "Cthulhu's Call").
     */
    @Column(nullable = false, length = 200)
    private String name;

    /**
     * Scenario description.
     */
    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * Scenario type (e.g., "Mystery", "Investigation", "Supernatural", "Combat", "Social").
     */
    @Column(nullable = false, length = 50)
    private String type;

    /**
     * Current status of the scenario.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ScenarioStatus status = ScenarioStatus.AVAILABLE;

    /**
     * Difficulty level (1-10).
     */
    @Column(nullable = false)
    private int difficulty = 1;

    /**
     * Recommended minimum level for this scenario.
     */
    @Column(name = "min_level", nullable = false)
    private int minLevel = 1;

    /**
     * Experience points awarded on completion.
     */
    @Column(name = "xp_reward", nullable = false)
    private int xpReward = 0;

    /**
     * The location where this scenario takes place.
     */
    @ManyToOne
    @JoinColumn(name = "location_id")
    private Earth2026Location location;

    /**
     * Whether this scenario involves supernatural elements (Cthulhu/D&D).
     */
    @Column(name = "supernatural", nullable = false)
    private boolean supernatural = false;

    /**
     * Sanity loss on failure (for Cthulhu-style scenarios).
     */
    @Column(name = "sanity_loss")
    private Integer sanityLoss;

    /**
     * Number of characters required to complete this scenario.
     */
    @Column(name = "party_size")
    private Integer partySize;

    /**
     * Estimated duration in minutes.
     */
    @Column(name = "duration_minutes")
    private Integer durationMinutes;

    /**
     * When the scenario was started.
     */
    @Column(name = "started_at")
    private LocalDateTime startedAt;

    /**
     * When the scenario was completed.
     */
    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    /**
     * Whether the scenario is currently active and available.
     */
    @Column(nullable = false)
    private boolean active = true;

    /**
     * Scenario creation date.
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Last update date.
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Earth2026Scenario() {
    }

    public Earth2026Scenario(String name, String description, String type) {
        this.name = name;
        this.description = description;
        this.type = type;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public ScenarioStatus getStatus() {
        return status;
    }

    public void setStatus(ScenarioStatus status) {
        this.status = status;
    }

    public int getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(int difficulty) {
        this.difficulty = difficulty;
    }

    public int getMinLevel() {
        return minLevel;
    }

    public void setMinLevel(int minLevel) {
        this.minLevel = minLevel;
    }

    public int getXpReward() {
        return xpReward;
    }

    public void setXpReward(int xpReward) {
        this.xpReward = xpReward;
    }

    public Earth2026Location getLocation() {
        return location;
    }

    public void setLocation(Earth2026Location location) {
        this.location = location;
    }

    public boolean isSupernatural() {
        return supernatural;
    }

    public void setSupernatural(boolean supernatural) {
        this.supernatural = supernatural;
    }

    public Integer getSanityLoss() {
        return sanityLoss;
    }

    public void setSanityLoss(Integer sanityLoss) {
        this.sanityLoss = sanityLoss;
    }

    public Integer getPartySize() {
        return partySize;
    }

    public void setPartySize(Integer partySize) {
        this.partySize = partySize;
    }

    public Integer getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(Integer durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
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
        return "Earth2026Scenario{id=" + id + ", name='" + name + "', type='" + type +
                "', status=" + status + ", difficulty=" + difficulty + "}";
    }
}
