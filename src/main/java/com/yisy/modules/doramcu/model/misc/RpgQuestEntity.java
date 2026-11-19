package com.yisy.modules.doramcu.model.misc;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * JPA entity that persists an RPG Quest on a planet surface.
 * <p>
 * Quests are narrative objectives issued by AI empires or settlement leaders.
 * Completing quests awards credits, XP, reputation, and sometimes unique items.
 * </p>
 */
public class RpgQuestEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    /** Unique quest ID (matches RpgQuest.id, UUID string). */
    @Column(name = "quest_id", nullable = false, unique = true, length = 100)
    private String questId;

    /** The planet this quest is on. */
    @Column(name = "planet_id", nullable = false)
    private int planetId;

    /** Quest title. */
    @Column(name = "title", nullable = false, length = 255)
    private String title;

    /** Quest description. */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /** Quest type (ESCORT, RESCUE, EXPLORATION, DIPLOMATIC, DEFENSE, INVESTIGATION). */
    @Column(name = "type", nullable = false, length = 20)
    private String type;

    /** Difficulty level (1-10). */
    @Column(name = "difficulty", nullable = false)
    private int difficulty;

    /** Credits awarded on completion. */
    @Column(name = "credits_reward", nullable = false)
    private long creditsReward;

    /** XP awarded on completion. */
    @Column(name = "xp_reward", nullable = false)
    private int xpReward;

    /** Reputation points awarded on completion. */
    @Column(name = "reputation_reward", nullable = false)
    private int reputationReward;

    /** Quest status (AVAILABLE, IN_PROGRESS, COMPLETED, FAILED, ABANDONED). */
    @Column(name = "status", nullable = false, length = 20)
    private String status;

    /** Name of the quest issuer. */
    @Column(name = "issuer_name", nullable = false, length = 255)
    private String issuerName;

    /** Empire that issued the quest. */
    @Column(name = "issuer_empire_id", nullable = false, length = 100)
    private String issuerEmpireId;

    /** JSON-serialized list of objectives. */
    @Column(name = "objectives_json", columnDefinition = "TEXT")
    private String objectivesJson;

    /** JSON-serialized list of required station IDs. */
    @Column(name = "required_station_ids_json", columnDefinition = "TEXT")
    private String requiredStationIdsJson;

    /** Corporation currently assigned to this quest. */
    @Column(name = "assigned_corporation_id", length = 100)
    private String assignedCorporationId;

    /** When the quest was created. */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /** When the quest expires. */
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    /** When the quest was completed (nullable). */
    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    /** When this record was created in DB. */
    @Column(name = "db_created_at", nullable = false)
    private LocalDateTime dbCreatedAt;

    /** When this record was last updated in DB. */
    @Column(name = "db_updated_at", nullable = false)
    private LocalDateTime dbUpdatedAt;

    public RpgQuestEntity() {
        this.dbCreatedAt = LocalDateTime.now();
        this.dbUpdatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getQuestId() {
        return questId;
    }

    public void setQuestId(String questId) {
        this.questId = questId;
    }

    public int getPlanetId() {
        return planetId;
    }

    public void setPlanetId(int planetId) {
        this.planetId = planetId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
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

    public int getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(int difficulty) {
        this.difficulty = difficulty;
    }

    public long getCreditsReward() {
        return creditsReward;
    }

    public void setCreditsReward(long creditsReward) {
        this.creditsReward = creditsReward;
    }

    public int getXpReward() {
        return xpReward;
    }

    public void setXpReward(int xpReward) {
        this.xpReward = xpReward;
    }

    public int getReputationReward() {
        return reputationReward;
    }

    public void setReputationReward(int reputationReward) {
        this.reputationReward = reputationReward;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getIssuerName() {
        return issuerName;
    }

    public void setIssuerName(String issuerName) {
        this.issuerName = issuerName;
    }

    public String getIssuerEmpireId() {
        return issuerEmpireId;
    }

    public void setIssuerEmpireId(String issuerEmpireId) {
        this.issuerEmpireId = issuerEmpireId;
    }

    public String getObjectivesJson() {
        return objectivesJson;
    }

    public void setObjectivesJson(String objectivesJson) {
        this.objectivesJson = objectivesJson;
    }

    public String getRequiredStationIdsJson() {
        return requiredStationIdsJson;
    }

    public void setRequiredStationIdsJson(String requiredStationIdsJson) {
        this.requiredStationIdsJson = requiredStationIdsJson;
    }

    public String getAssignedCorporationId() {
        return assignedCorporationId;
    }

    public void setAssignedCorporationId(String assignedCorporationId) {
        this.assignedCorporationId = assignedCorporationId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public LocalDateTime getDbCreatedAt() {
        return dbCreatedAt;
    }

    public void setDbCreatedAt(LocalDateTime dbCreatedAt) {
        this.dbCreatedAt = dbCreatedAt;
    }

    public LocalDateTime getDbUpdatedAt() {
        return dbUpdatedAt;
    }

    public void setDbUpdatedAt(LocalDateTime dbUpdatedAt) {
        this.dbUpdatedAt = dbUpdatedAt;
    }

    @PrePersist
    protected void onCreate() {
        this.dbCreatedAt = LocalDateTime.now();
        this.dbUpdatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.dbUpdatedAt = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return "RpgQuestEntity{" +
                "id=" + id +
                ", questId='" + questId + '\'' +
                ", planetId=" + planetId +
                ", title='" + title + '\'' +
                ", type='" + type + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}