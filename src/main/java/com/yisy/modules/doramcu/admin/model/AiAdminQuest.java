package com.yisy.modules.doramcu.admin.model;
import com.yisy.modules.doramcu.model.players.Player;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import com.yisy.modules.doramcu.neuron.service.CdcEntityListener;

import java.time.LocalDateTime;

/**
 * JPA entity representing a quest assigned by an AI admin to a player.
 * Tracks quest lifecycle from assignment through completion or failure.
 */
@Entity
@Table(name = "ai_admin_quests")
@EntityListeners(CdcEntityListener.class)
public class AiAdminQuest {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    /**
     * ID of the AI character (quest giver) who assigned this quest.
     */
    @Column(name = "quest_giver_id", nullable = false)
    private Long questGiverId;

    /**
     * ID of the player who received this quest.
     */
    @Column(name = "player_id", nullable = false)
    private Long playerId;

    /**
     * Short title of the quest, e.g., "Investigate the Mining Anomaly".
     */
    @Column(nullable = false, length = 255)
    private String title;

    /**
     * Detailed description of the quest objectives.
     */
    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * Type of quest (INVESTIGATE, DELIVER, ESCORT, GATHER, COMBAT, OTHER).
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "quest_type", nullable = false, length = 50)
    private QuestType questType;

    /**
     * Current status of the quest.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private QuestStatus status = QuestStatus.ASSIGNED;

    /**
     * XP reward for completing this quest.
     */
    @Column(name = "xp_reward", nullable = false)
    private int xpReward = 10;

    /**
     * Disposition improvement for the quest giver when the quest is completed.
     */
    @Column(name = "disposition_reward", nullable = false)
    private int dispositionReward = 1;

    /**
     * Human-readable description of the quest target, e.g., "Find 5 ore samples".
     */
    @Column(name = "target_description", length = 500)
    private String targetDescription;

    /**
     * Total count required to complete the quest.
     */
    @Column(name = "target_count", nullable = false)
    private int targetCount = 1;

    /**
     * Current progress count toward the target.
     */
    @Column(name = "current_count", nullable = false)
    private int currentCount = 0;

    /**
     * When the quest was assigned.
     */
    @Column(name = "assigned_at", nullable = false)
    private LocalDateTime assignedAt = LocalDateTime.now();

    /**
     * When the quest was completed (null if not yet completed).
     */
    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    // --- Chain fields ---

    /**
     * Parent quest in a chain (null if this is the first quest or not part of a chain).
     */
    @Column(name = "parent_quest_id")
    private Long parentQuestId;

    /**
     * Chain ID groups related quests together, e.g., "galaxy-defense".
     */
    @Column(name = "chain_id", length = 50)
    private String chainId;

    /**
     * Position in the chain (1 = first, 2 = second, etc.).
     */
    @Column(name = "chain_order")
    private Integer chainOrder;

    // --- Rich reward fields ---

    /**
     * Credits awarded on quest completion.
     */
    @Column(name = "reward_credits", nullable = false)
    private int rewardCredits = 0;

    /**
     * Reputation points awarded on quest completion.
     */
    @Column(name = "reward_reputation", nullable = false)
    private int rewardReputation = 0;

    /**
     * Item ID unlocked on quest completion (e.g., "shield_generator").
     */
    @Column(name = "reward_item_id", length = 100)
    private String rewardItemId;

    /**
     * Title/badge unlocked on quest completion (e.g., "Defender").
     */
    @Column(name = "reward_title", length = 100)
    private String rewardTitle;

    // --- Enums ---

    public enum QuestType {
        /** Investigate a location, person, or event. */
        INVESTIGATE,
        /** Deliver an item or message to a location. */
        DELIVER,
        /** Escort someone or something to a destination. */
        ESCORT,
        /** Gather resources or information. */
        GATHER,
        /** Combat-related objective. */
        COMBAT,
        /** Diplomatic mission: negotiate, mediate, or represent interests. */
        DIPLOMACY,
        /** Espionage: covert intelligence gathering or sabotage. */
        ESPIONAGE,
        /** Research: scientific study, technology development, or innovation. */
        RESEARCH,
        /** Other miscellaneous quest type. */
        OTHER
    }

    public enum QuestStatus {
        /** Quest was just assigned, not yet accepted. */
        ASSIGNED,
        /** Player has started working on the quest. */
        IN_PROGRESS,
        /** Quest is successfully completed. */
        COMPLETED,
        /** Quest failed (objective not met). */
        FAILED,
        /** Quest was abandoned by the player. */
        ABANDONED
    }

    // --- Constructors ---

    public AiAdminQuest() {
    }

    public AiAdminQuest(Long questGiverId, Long playerId, String title, String description,
                        QuestType questType, int xpReward, int dispositionReward,
                        String targetDescription, int targetCount) {
        this.questGiverId = questGiverId;
        this.playerId = playerId;
        this.title = title;
        this.description = description;
        this.questType = questType;
        this.xpReward = xpReward;
        this.dispositionReward = dispositionReward;
        this.targetDescription = targetDescription;
        this.targetCount = targetCount;
        this.status = QuestStatus.ASSIGNED;
        this.assignedAt = LocalDateTime.now();
    }

    // --- Getters and Setters ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getQuestGiverId() {
        return questGiverId;
    }

    public void setQuestGiverId(Long questGiverId) {
        this.questGiverId = questGiverId;
    }

    public Long getPlayerId() {
        return playerId;
    }

    public void setPlayerId(Long playerId) {
        this.playerId = playerId;
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

    public QuestType getQuestType() {
        return questType;
    }

    public void setQuestType(QuestType questType) {
        this.questType = questType;
    }

    public QuestStatus getStatus() {
        return status;
    }

    public void setStatus(QuestStatus status) {
        this.status = status;
    }

    public int getXpReward() {
        return xpReward;
    }

    public void setXpReward(int xpReward) {
        this.xpReward = xpReward;
    }

    public int getDispositionReward() {
        return dispositionReward;
    }

    public void setDispositionReward(int dispositionReward) {
        this.dispositionReward = dispositionReward;
    }

    public String getTargetDescription() {
        return targetDescription;
    }

    public void setTargetDescription(String targetDescription) {
        this.targetDescription = targetDescription;
    }

    public int getTargetCount() {
        return targetCount;
    }

    public void setTargetCount(int targetCount) {
        this.targetCount = targetCount;
    }

    public int getCurrentCount() {
        return currentCount;
    }

    public void setCurrentCount(int currentCount) {
        this.currentCount = currentCount;
    }

    public LocalDateTime getAssignedAt() {
        return assignedAt;
    }

    public void setAssignedAt(LocalDateTime assignedAt) {
        this.assignedAt = assignedAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public Long getParentQuestId() {
        return parentQuestId;
    }

    public void setParentQuestId(Long parentQuestId) {
        this.parentQuestId = parentQuestId;
    }

    public String getChainId() {
        return chainId;
    }

    public void setChainId(String chainId) {
        this.chainId = chainId;
    }

    public Integer getChainOrder() {
        return chainOrder;
    }

    public void setChainOrder(Integer chainOrder) {
        this.chainOrder = chainOrder;
    }

    public int getRewardCredits() {
        return rewardCredits;
    }

    public void setRewardCredits(int rewardCredits) {
        this.rewardCredits = rewardCredits;
    }

    public int getRewardReputation() {
        return rewardReputation;
    }

    public void setRewardReputation(int rewardReputation) {
        this.rewardReputation = rewardReputation;
    }

    public String getRewardItemId() {
        return rewardItemId;
    }

    public void setRewardItemId(String rewardItemId) {
        this.rewardItemId = rewardItemId;
    }

    public String getRewardTitle() {
        return rewardTitle;
    }

    public void setRewardTitle(String rewardTitle) {
        this.rewardTitle = rewardTitle;
    }

    // --- Helper methods ---

    /**
     * Returns true if the quest is active (not yet completed, failed, or abandoned).
     */
    public boolean isActive() {
        return status == QuestStatus.ASSIGNED || status == QuestStatus.IN_PROGRESS;
    }

    /**
     * Returns the progress percentage (0-100).
     */
    public int getProgressPercent() {
        if (targetCount <= 0) return 0;
        return Math.min(100, (currentCount * 100) / targetCount);
    }

    /**
     * Increments the current count by the given amount.
     * Auto-completes if the target is reached.
     */
    public void incrementProgress(int amount) {
        this.currentCount = Math.min(targetCount, this.currentCount + amount);
        if (this.currentCount >= this.targetCount && isActive()) {
            this.status = QuestStatus.COMPLETED;
            this.completedAt = LocalDateTime.now();
        }
    }

    /**
     * Returns true if this quest is part of a chain.
     */
    public boolean isPartOfChain() {
        return chainId != null && !chainId.isEmpty();
    }

    /**
     * Returns true if this quest has a parent quest (is not the first in the chain).
     */
    public boolean hasParentQuest() {
        return parentQuestId != null;
    }

    /**
     * Returns true if this quest has any item or title rewards.
     */
    public boolean hasItemReward() {
        return rewardItemId != null && !rewardItemId.isEmpty();
    }

    /**
     * Returns true if this quest has a title reward.
     */
    public boolean hasTitleReward() {
        return rewardTitle != null && !rewardTitle.isEmpty();
    }

    @Override
    public String toString() {
        return "AiAdminQuest{id=" + id + ", title='" + title + "', type=" + questType +
                ", status=" + status + ", progress=" + currentCount + "/" + targetCount +
                ", chain=" + chainId + ", order=" + chainOrder + "}";
    }
}
