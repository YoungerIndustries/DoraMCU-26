package com.yisy.modules.doramcu.hex.planet;

import java.util.*;

/**
 * Represents an RPG-style quest issued by an AI empire to the player.
 * <p>
 * Quests are the narrative bridge between the 4X galaxy layer and the tycoon
 * economy layer. AI empires issue quests that reward credits, XP, reputation,
 * and unique opportunities. Quests have prerequisites (min reputation, min
 * corporation rank) and progress tracking.
 * </p>
 * <p>
 * Quest types:
 * <ul>
 *   <li><b>DELIVERY</b>: Transport specific cargo to a destination</li>
 *   <li><b>CONSTRUCTION</b>: Build infrastructure on a planet</li>
 *   <li><b>ESPIONAGE</b>: Gather intelligence on rival empires</li>
 *   <li><b>MILITARY_LOGISTICS</b>: Supply troops during wars</li>
 * </ul>
 * </p>
 */
public class RpgQuest {

    /** Types of quests that can be issued */
    public enum QuestType {
        /** Transport specific cargo to a destination */
        DELIVERY("Delivery", "Transport cargo between locations"),

        /** Build infrastructure on a planet */
        CONSTRUCTION("Construction", "Build stations, roads, or settlements"),

        /** Gather intelligence on rival empires */
        ESPIONAGE("Espionage", "Collect secrets from enemy territory"),

        /** Supply troops during wars */
        MILITARY_LOGISTICS("Military Logistics", "Support war efforts with supplies"),

        /** Establish a new trade route */
        ROUTE_ESTABLISHMENT("Route Establishment", "Create a new trade connection"),

        /** Rescue mission - save stranded personnel */
        RESCUE("Rescue", "Save people in distress"),

        /** Escort mission - protect a valuable convoy */
        ESCORT("Escort", "Protect a convoy from threats"),

        /** Sabotage mission - disrupt enemy operations */
        SABOTAGE("Sabotage", "Disrupt rival infrastructure");

        private final String label;
        private final String description;

        QuestType(String label, String description) {
            this.label = label;
            this.description = description;
        }

        public String getLabel() {
            return label;
        }

        public String getDescription() {
            return description;
        }
    }

    /** Quest status */
    public enum QuestStatus {
        /** Quest is available for acceptance */
        AVAILABLE,

        /** Quest has been accepted and is in progress */
        ACTIVE,

        /** Quest has been completed successfully */
        COMPLETED,

        /** Quest has failed or been abandoned */
        FAILED,

        /** Quest has expired due to time limit */
        EXPIRED
    }

    private final String id;
    private final String title;
    private final String description;
    private final QuestType type;
    private final int issuingEmpireId;

    /** Target value for progress tracking */
    private final int targetValue;

    /** Current progress toward target */
    private int currentValue;

    /** Quest status */
    private QuestStatus status;

    /** Rewards */
    private final int rewardCredits;
    private final int rewardXp;
    private final int rewardReputation;

    /** Time limit */
    private final int maxTurns;
    private int turnsRemaining;

    /** Prerequisites */
    private final int minReputation;
    private final CorporationRank minRank;

    /** Optional cargo type for delivery quests */
    private final CargoType cargoType;

    /** Optional destination planet ID */
    private final String destinationPlanetId;

    /** Timestamp when quest was created */
    private final long createdAt;

    /**
     * Creates a new quest.
     *
     * @param id unique quest ID
     * @param title quest title
     * @param description quest description
     * @param type quest type
     * @param issuingEmpireId empire that issued the quest
     * @param targetValue target progress value
     * @param rewardCredits credits reward
     * @param rewardXp XP reward
     * @param rewardReputation reputation reward with issuing empire
     * @param maxTurns maximum turns to complete
     * @param minReputation minimum reputation required
     * @param minRank minimum corporation rank required
     * @param cargoType optional cargo type for delivery quests
     * @param destinationPlanetId optional destination planet
     */
    public RpgQuest(String id, String title, String description, QuestType type,
                    int issuingEmpireId, int targetValue,
                    int rewardCredits, int rewardXp, int rewardReputation,
                    int maxTurns, int minReputation, CorporationRank minRank,
                    CargoType cargoType, String destinationPlanetId) {
        this.id = Objects.requireNonNull(id, "Quest ID must not be null");
        this.title = Objects.requireNonNull(title, "Title must not be null");
        this.description = Objects.requireNonNull(description, "Description must not be null");
        this.type = Objects.requireNonNull(type, "Type must not be null");
        this.issuingEmpireId = issuingEmpireId;
        this.targetValue = targetValue;
        this.currentValue = 0;
        this.status = QuestStatus.AVAILABLE;
        this.rewardCredits = rewardCredits;
        this.rewardXp = rewardXp;
        this.rewardReputation = rewardReputation;
        this.maxTurns = maxTurns;
        this.turnsRemaining = maxTurns;
        this.minReputation = minReputation;
        this.minRank = Objects.requireNonNull(minRank, "Min rank must not be null");
        this.cargoType = cargoType;
        this.destinationPlanetId = destinationPlanetId;
        this.createdAt = System.currentTimeMillis();
    }

    /** @return the unique quest ID */
    public String getId() {
        return id;
    }

    /** @return the quest title */
    public String getTitle() {
        return title;
    }

    /** @return the quest description */
    public String getDescription() {
        return description;
    }

    /** @return the quest type */
    public QuestType getType() {
        return type;
    }

    /** @return the empire ID that issued this quest */
    public int getIssuingEmpireId() {
        return issuingEmpireId;
    }

    /** @return the target progress value */
    public int getTargetValue() {
        return targetValue;
    }

    /** @return the current progress value */
    public int getCurrentValue() {
        return currentValue;
    }

    /**
     * Adds progress to the quest.
     *
     * @param amount amount to add
     * @return true if the quest is now completed
     */
    public boolean addProgress(int amount) {
        if (status != QuestStatus.ACTIVE) {
            return false;
        }
        this.currentValue = Math.min(targetValue, currentValue + amount);
        if (this.currentValue >= this.targetValue) {
            this.status = QuestStatus.COMPLETED;
        }
        return this.status == QuestStatus.COMPLETED;
    }

    /** @return the quest status */
    public QuestStatus getStatus() {
        return status;
    }

    /**
     * Accepts the quest, changing status to ACTIVE.
     *
     * @return true if the quest was accepted
     */
    public boolean accept() {
        if (status != QuestStatus.AVAILABLE) {
            return false;
        }
        this.status = QuestStatus.ACTIVE;
        return true;
    }

    /**
     * Fails the quest.
     */
    public void fail() {
        if (status == QuestStatus.ACTIVE) {
            this.status = QuestStatus.FAILED;
        }
    }

    /**
     * Processes a turn, decrementing the time limit.
     *
     * @return true if the quest has expired
     */
    public boolean processTurn() {
        if (status == QuestStatus.ACTIVE) {
            this.turnsRemaining--;
            if (this.turnsRemaining <= 0) {
                this.status = QuestStatus.EXPIRED;
                return true;
            }
        }
        return false;
    }

    /** @return the credits reward */
    public int getRewardCredits() {
        return rewardCredits;
    }

    /** @return the XP reward */
    public int getRewardXp() {
        return rewardXp;
    }

    /** @return the reputation reward */
    public int getRewardReputation() {
        return rewardReputation;
    }

    /** @return the maximum turns allowed */
    public int getMaxTurns() {
        return maxTurns;
    }

    /** @return the turns remaining */
    public int getTurnsRemaining() {
        return turnsRemaining;
    }

    /** @return the minimum reputation required */
    public int getMinReputation() {
        return minReputation;
    }

    /** @return the minimum corporation rank required */
    public CorporationRank getMinRank() {
        return minRank;
    }

    /** @return the cargo type for delivery quests, or null */
    public CargoType getCargoType() {
        return cargoType;
    }

    /** @return the destination planet ID, or null */
    public String getDestinationPlanetId() {
        return destinationPlanetId;
    }

    /** @return the creation timestamp */
    public long getCreatedAt() {
        return createdAt;
    }

    /**
     * Checks if the quest is completable by the given corporation.
     *
     * @param corp the corporation
     * @param reputation the reputation with the issuing empire
     * @return true if prerequisites are met
     */
    public boolean isCompletableBy(TransportCorporation corp, int reputation) {
        return reputation >= minReputation &&
                corp.getRank().ordinal() >= minRank.ordinal();
    }

    /**
     * Gets the progress percentage.
     *
     * @return progress from 0.0 to 1.0
     */
    public double getProgressPercentage() {
        if (targetValue <= 0) {
            return 1.0;
        }
        return (double) currentValue / targetValue;
    }

    @Override
    public String toString() {
        return "RpgQuest{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", type=" + type +
                ", status=" + status +
                ", progress=" + currentValue + "/" + targetValue +
                ", rewards=" + rewardCredits + "cr/" + rewardXp + "xp/" + rewardReputation + "rep" +
                '}';
    }
}
