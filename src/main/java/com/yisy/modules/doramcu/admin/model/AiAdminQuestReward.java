package com.yisy.modules.doramcu.admin.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import com.yisy.modules.doramcu.neuron.service.CdcEntityListener;

import java.time.LocalDateTime;

/**
 * JPA entity representing a reward for completing a quest.
 * Supports multiple reward types per quest (credits, XP, items, titles, etc.).
 */
@Entity
@Table(name = "ai_admin_quest_rewards")
@EntityListeners(CdcEntityListener.class)
public class AiAdminQuestReward {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    /**
     * Parent quest this reward belongs to.
     */
    @Column(name = "quest_id", nullable = false)
    private Long questId;

    /**
     * Type of reward (CREDITS, XP, DISPOSITION, REPUTATION, ITEM_UNLOCK, TITLE_UNLOCK, SKILL_BONUS).
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "reward_type", nullable = false, length = 50)
    private RewardType rewardType;

    /**
     * Numeric value of the reward (e.g., 500 credits, 25 XP).
     */
    @Column(name = "reward_value", nullable = false)
    private int rewardValue = 0;

    /**
     * Item ID for item-based rewards (e.g., "shield_generator").
     */
    @Column(name = "reward_item_id", length = 100)
    private String rewardItemId;

    /**
     * Human-readable description of the reward.
     */
    @Column(name = "reward_description", columnDefinition = "TEXT")
    private String rewardDescription;

    /**
     * When this reward was created.
     */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    // --- Enums ---

    public enum RewardType {
        /** In-game currency. */
        CREDITS,
        /** Experience points. */
        XP,
        /** Disposition improvement toward quest giver. */
        DISPOSITION,
        /** Reputation points in a community. */
        REPUTATION,
        /** Unlocks an item (e.g., weapon, tool). */
        ITEM_UNLOCK,
        /** Unlocks a title or badge. */
        TITLE_UNLOCK,
        /** Grants a skill bonus. */
        SKILL_BONUS
    }

    // --- Constructors ---

    public AiAdminQuestReward() {
    }

    public AiAdminQuestReward(Long questId, RewardType rewardType, int rewardValue,
                              String rewardItemId, String rewardDescription) {
        this.questId = questId;
        this.rewardType = rewardType;
        this.rewardValue = rewardValue;
        this.rewardItemId = rewardItemId;
        this.rewardDescription = rewardDescription;
        this.createdAt = LocalDateTime.now();
    }

    // --- Getters and Setters ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getQuestId() {
        return questId;
    }

    public void setQuestId(Long questId) {
        this.questId = questId;
    }

    public RewardType getRewardType() {
        return rewardType;
    }

    public void setRewardType(RewardType rewardType) {
        this.rewardType = rewardType;
    }

    public int getRewardValue() {
        return rewardValue;
    }

    public void setRewardValue(int rewardValue) {
        this.rewardValue = rewardValue;
    }

    public String getRewardItemId() {
        return rewardItemId;
    }

    public void setRewardItemId(String rewardItemId) {
        this.rewardItemId = rewardItemId;
    }

    public String getRewardDescription() {
        return rewardDescription;
    }

    public void setRewardDescription(String rewardDescription) {
        this.rewardDescription = rewardDescription;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "AiAdminQuestReward{id=" + id + ", questId=" + questId + ", type=" + rewardType +
                ", value=" + rewardValue + ", item='" + rewardItemId + "'}";
    }
}
