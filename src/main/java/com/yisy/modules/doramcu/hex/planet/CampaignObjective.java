package com.yisy.modules.doramcu.hex.planet;

import java.util.Objects;
import java.util.UUID;

/**
 * Represents an objective in the Business Tycoon campaign.
 * <p>
 * Objectives define specific goals the player must achieve to progress through
 * the campaign. They track progress toward a target value and provide rewards
 * upon completion.
 * </p>
 * <p>
 * Inspired by OpenTTD's GSGoal API, objectives can have buttons, questions,
 * and progress tracking.
 * </p>
 */
public class CampaignObjective {

    /**
     * Types of objectives in the campaign.
     */
    public enum ObjectiveType {
        /** Build a certain number of infrastructure items */
        BUILD,
        /** Deliver a certain amount of cargo */
        DELIVER,
        /** Earn a certain amount of credits */
        EARN,
        /** Achieve a certain market share percentage */
        MARKET_SHARE,
        /** Reach a specific corporation rank */
        RANK_UP,
        /** Complete a certain number of contracts */
        COMPLETE_CONTRACTS,
        /** Operate on a certain number of planets */
        EXPAND_PLANETS,
        /** Establish a certain number of trade routes */
        ESTABLISH_ROUTES
    }

    private final String id;
    private final String title;
    private final String description;
    private final ObjectiveType type;

    /** Target value to achieve (e.g., 100 for 100 cargo delivered) */
    private final int targetValue;

    /** Current progress toward the target */
    private int currentValue;

    /** Whether this objective has been completed */
    private boolean completed;

    /** Credits awarded upon completion */
    private final int rewardCredits;

    /** XP awarded upon completion */
    private final int rewardXp;

    /** Whether this objective is required to progress the campaign */
    private final boolean required;

    /** Prerequisite objective ID, or null if none */
    private final String prerequisiteId;

    /**
     * Creates a new campaign objective.
     *
     * @param title           human-readable title
     * @param description     detailed description
     * @param type            the objective type
     * @param targetValue     the target value to achieve
     * @param rewardCredits   credits awarded on completion
     * @param rewardXp        XP awarded on completion
     * @param required        whether this is required for campaign progress
     * @param prerequisiteId  prerequisite objective ID, or null
     */
    public CampaignObjective(String title, String description,
                              ObjectiveType type, int targetValue,
                              int rewardCredits, int rewardXp,
                              boolean required, String prerequisiteId) {
        this.id = UUID.randomUUID().toString();
        this.title = Objects.requireNonNull(title, "Title must not be null");
        this.description = Objects.requireNonNull(description, "Description must not be null");
        this.type = Objects.requireNonNull(type, "Type must not be null");
        if (targetValue <= 0) {
            throw new IllegalArgumentException("Target value must be positive: " + targetValue);
        }
        this.targetValue = targetValue;
        this.currentValue = 0;
        this.completed = false;
        if (rewardCredits < 0) {
            throw new IllegalArgumentException("Reward credits must be non-negative");
        }
        this.rewardCredits = rewardCredits;
        if (rewardXp < 0) {
            throw new IllegalArgumentException("Reward XP must be non-negative");
        }
        this.rewardXp = rewardXp;
        this.required = required;
        this.prerequisiteId = prerequisiteId;
    }

    /** @return the unique objective ID */
    public String getId() {
        return id;
    }

    /** @return the objective title */
    public String getTitle() {
        return title;
    }

    /** @return the objective description */
    public String getDescription() {
        return description;
    }

    /** @return the objective type */
    public ObjectiveType getType() {
        return type;
    }

    /** @return the target value to achieve */
    public int getTargetValue() {
        return targetValue;
    }

    /** @return the current progress value */
    public int getCurrentValue() {
        return currentValue;
    }

    /**
     * Adds progress toward the objective target.
     * Automatically marks the objective as completed if the target is reached.
     *
     * @param amount the amount of progress to add
     * @return true if the objective was just completed by this update
     */
    public boolean addProgress(int amount) {
        if (completed) {
            return false;
        }
        this.currentValue = Math.min(this.currentValue + amount, this.targetValue);
        if (this.currentValue >= this.targetValue) {
            this.completed = true;
            return true;
        }
        return false;
    }

    /**
     * Sets the current progress value directly.
     *
     * @param value the new current value
     */
    public void setCurrentValue(int value) {
        this.currentValue = Math.max(0, Math.min(value, this.targetValue));
        if (this.currentValue >= this.targetValue) {
            this.completed = true;
        }
    }

    /** @return true if the objective is completed */
    public boolean isCompleted() {
        return completed;
    }

    /**
     * Marks the objective as completed.
     */
    public void setCompleted() {
        this.completed = true;
        this.currentValue = this.targetValue;
    }

    /** @return the credit reward for completion */
    public int getRewardCredits() {
        return rewardCredits;
    }

    /** @return the XP reward for completion */
    public int getRewardXp() {
        return rewardXp;
    }

    /** @return true if this objective is required for campaign progress */
    public boolean isRequired() {
        return required;
    }

    /** @return the prerequisite objective ID, or null */
    public String getPrerequisiteId() {
        return prerequisiteId;
    }

    /**
     * Returns the progress percentage (0-100).
     *
     * @return progress percentage
     */
    public int getProgressPercentage() {
        if (targetValue == 0) {
            return 100;
        }
        return (int) ((currentValue * 100L) / targetValue);
    }

    /**
     * Returns the remaining value needed to complete the objective.
     *
     * @return remaining value
     */
    public int getRemainingValue() {
        return Math.max(0, targetValue - currentValue);
    }

    @Override
    public String toString() {
        return "CampaignObjective{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", type=" + type +
                ", progress=" + currentValue + "/" + targetValue +
                ", completed=" + completed +
                '}';
    }
}
