package com.yisy.modules.doramcu.hex.planet;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Represents a narrative quest on a planet surface.
 * <p>
 * Quests are narrative objectives issued by AI empires or settlement leaders.
 * They give corporations purpose beyond simple cargo delivery: escort missions,
 * rescue operations, exploration, and diplomatic tasks. Completing quests
 * awards credits, XP, reputation, and sometimes unique items.
 * </p>
 * <p>
 * Quest types:
 * <ul>
 *   <li>ESCORT — Protect a convoy or VIP between stations</li>
 *   <li>RESCUE — Retrieve people or cargo from a dangerous location</li>
 *   <li>EXPLORATION — Survey uncharted terrain and report back</li>
 *   <li>DIPLOMATIC — Deliver sensitive items between settlements</li>
 *   <li>DEFENSE — Defend a settlement or station from threats</li>
 *   <li>INVESTIGATION — Gather intelligence on suspicious activity</li>
 * </ul>
 * </p>
 */
public class PlanetQuest {

    public enum QuestType {
        /** Protect a convoy or VIP between stations */
        ESCORT("Escort", 1.5, 10),
        /** Retrieve people or cargo from a dangerous location */
        RESCUE("Rescue", 2.0, 15),
        /** Survey uncharted terrain and report back */
        EXPLORATION("Exploration", 1.0, 5),
        /** Deliver sensitive items between settlements */
        DIPLOMATIC("Diplomatic", 2.5, 20),
        /** Defend a settlement or station from threats */
        DEFENSE("Defense", 1.8, 12),
        /** Gather intelligence on suspicious activity */
        INVESTIGATION("Investigation", 1.2, 8);

        private final String displayName;
        private final double difficultyMultiplier;
        private final int baseXpReward;

        QuestType(String displayName, double difficultyMultiplier, int baseXpReward) {
            this.displayName = displayName;
            this.difficultyMultiplier = difficultyMultiplier;
            this.baseXpReward = baseXpReward;
        }

        public String getDisplayName() {
            return displayName;
        }

        public double getDifficultyMultiplier() {
            return difficultyMultiplier;
        }

        public int getBaseXpReward() {
            return baseXpReward;
        }
    }

    public enum QuestStatus {
        /** Available for any corporation to accept */
        AVAILABLE,
        /** Accepted by a corporation — in progress */
        IN_PROGRESS,
        /** Successfully completed — rewards awarded */
        COMPLETED,
        /** Failed — objectives not met */
        FAILED,
        /** Abandoned — corporation gave up */
        ABANDONED
    }

    private final String id;
    private final String title;
    private final String description;
    private final QuestType type;
    private final int difficulty;
    private final long creditsReward;
    private final int xpReward;
    private final int reputationReward;
    private QuestStatus status;
    private final LocalDateTime createdAt;
    private final LocalDateTime expiresAt;
    private final String issuerName;
    private final String issuerEmpireId;
    private final List<String> objectives;
    private final List<String> requiredStationIds;
    private String assignedCorporationId;
    private LocalDateTime completedAt;

    /**
     * Builder for creating PlanetQuest instances.
     */
    public static class Builder {
        private String title;
        private String description;
        private QuestType type;
        private int difficulty;
        private long creditsReward;
        private int xpReward;
        private int reputationReward;
        private String issuerName;
        private String issuerEmpireId;
        private final List<String> objectives = new ArrayList<>();
        private final List<String> requiredStationIds = new ArrayList<>();
        private int timeLimitHours = 72;

        public Builder title(String title) {
            this.title = title;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder type(QuestType type) {
            this.type = type;
            return this;
        }

        public Builder difficulty(int difficulty) {
            this.difficulty = difficulty;
            return this;
        }

        public Builder creditsReward(long creditsReward) {
            this.creditsReward = creditsReward;
            return this;
        }

        public Builder xpReward(int xpReward) {
            this.xpReward = xpReward;
            return this;
        }

        public Builder reputationReward(int reputationReward) {
            this.reputationReward = reputationReward;
            return this;
        }

        public Builder issuerName(String issuerName) {
            this.issuerName = issuerName;
            return this;
        }

        public Builder issuerEmpireId(String issuerEmpireId) {
            this.issuerEmpireId = issuerEmpireId;
            return this;
        }

        public Builder addObjective(String objective) {
            this.objectives.add(objective);
            return this;
        }

        public Builder addRequiredStationId(String stationId) {
            this.requiredStationIds.add(stationId);
            return this;
        }

        public Builder timeLimitHours(int timeLimitHours) {
            this.timeLimitHours = timeLimitHours;
            return this;
        }

        public PlanetQuest build() {
            return new PlanetQuest(this, timeLimitHours);
        }
    }

    /**
     * Creates a builder for PlanetQuest.
     *
     * @return a new Builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    private PlanetQuest(Builder builder, int timeLimitHours) {
        this.id = UUID.randomUUID().toString();
        this.title = builder.title;
        this.description = builder.description;
        this.type = builder.type;
        this.difficulty = builder.difficulty;
        this.creditsReward = builder.creditsReward;
        this.xpReward = builder.xpReward;
        this.reputationReward = builder.reputationReward;
        this.status = QuestStatus.AVAILABLE;
        this.createdAt = LocalDateTime.now();
        this.expiresAt = LocalDateTime.now().plusHours(timeLimitHours);
        this.issuerName = builder.issuerName;
        this.issuerEmpireId = builder.issuerEmpireId;
        this.objectives = new ArrayList<>(builder.objectives);
        this.requiredStationIds = new ArrayList<>(builder.requiredStationIds);
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public QuestType getType() {
        return type;
    }

    public int getDifficulty() {
        return difficulty;
    }

    public long getCreditsReward() {
        return creditsReward;
    }

    public int getXpReward() {
        return xpReward;
    }

    public int getReputationReward() {
        return reputationReward;
    }

    public QuestStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public String getIssuerName() {
        return issuerName;
    }

    public String getIssuerEmpireId() {
        return issuerEmpireId;
    }

    public List<String> getObjectives() {
        return objectives;
    }

    public List<String> getRequiredStationIds() {
        return requiredStationIds;
    }

    public String getAssignedCorporationId() {
        return assignedCorporationId;
    }

    public void setAssignedCorporationId(String assignedCorporationId) {
        this.assignedCorporationId = assignedCorporationId;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    /**
     * Accepts this quest for a corporation.
     *
     * @param corporationId the corporation accepting the quest
     */
    public void accept(String corporationId) {
        if (this.status != QuestStatus.AVAILABLE) {
            throw new IllegalStateException("Quest is not available: " + this.status);
        }
        this.assignedCorporationId = corporationId;
        this.status = QuestStatus.IN_PROGRESS;
    }

    /**
     * Completes this quest successfully.
     */
    public void complete() {
        if (this.status != QuestStatus.IN_PROGRESS) {
            throw new IllegalStateException("Quest is not in progress: " + this.status);
        }
        this.status = QuestStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
    }

    /**
     * Fails this quest.
     */
    public void fail() {
        if (this.status != QuestStatus.IN_PROGRESS) {
            throw new IllegalStateException("Quest is not in progress: " + this.status);
        }
        this.status = QuestStatus.FAILED;
    }

    /**
     * Abandons this quest.
     */
    public void abandon() {
        if (this.status != QuestStatus.IN_PROGRESS) {
            throw new IllegalStateException("Quest is not in progress: " + this.status);
        }
        this.status = QuestStatus.ABANDONED;
    }

    /**
     * Checks if this quest has expired.
     *
     * @return true if the current time is past the expiration time
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expiresAt);
    }

    @Override
    public String toString() {
        return "PlanetQuest{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", type=" + type.getDisplayName() +
                ", difficulty=" + difficulty +
                ", credits=" + creditsReward +
                ", status=" + status +
                '}';
    }
}
