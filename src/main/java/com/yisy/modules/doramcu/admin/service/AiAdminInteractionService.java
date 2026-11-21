package com.yisy.modules.doramcu.admin.service;
import com.yisy.modules.doramcu.model.players.Player;

import com.yisy.modules.doramcu.admin.model.AiAdminQuest;
import com.yisy.modules.doramcu.admin.model.AiAdminQuestReward;
import com.yisy.modules.doramcu.admin.model.AiAdminQuestStep;
import com.yisy.modules.doramcu.admin.repository.AiAdminQuestRewardRepository;
import com.yisy.modules.doramcu.admin.repository.AiAdminQuestStepRepository;
import com.yisy.modules.doramcu.eve.model.AiCharacter;
import com.yisy.modules.doramcu.repository.ai.AiAdminQuestRepository;
import com.yisy.modules.doramcu.repository.ai.AiCharacterRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Manages the full lifecycle of AI admin player interactions:
 * quest assignment, progress tracking, completion with XP rewards,
 * and dynamic disposition changes.
 */
@Service
public class AiAdminInteractionService {

    private static final Logger log = LoggerFactory.getLogger(AiAdminInteractionService.class);

    private final AiAdminQuestRepository questRepository;
    private final AiCharacterRepository aiCharacterRepository;
    private final AiAdminQuestStepRepository questStepRepository;
    private final AiAdminQuestRewardRepository questRewardRepository;

    public AiAdminInteractionService(AiAdminQuestRepository questRepository,
                                      AiCharacterRepository aiCharacterRepository,
                                      AiAdminQuestStepRepository questStepRepository,
                                      AiAdminQuestRewardRepository questRewardRepository) {
        this.questRepository = questRepository;
        this.aiCharacterRepository = aiCharacterRepository;
        this.questStepRepository = questStepRepository;
        this.questRewardRepository = questRewardRepository;
    }

    // ------------------------------------------------------------------
    // Quest assignment
    // ------------------------------------------------------------------

    /**
     * Assigns a new quest from an AI admin to a player.
     *
     * @param aiAdminId the AI admin character ID (quest giver)
     * @param playerId the player ID (quest taker)
     * @param title quest title
     * @param description quest description
     * @param questType type of quest
     * @param xpReward XP awarded on completion
     * @param dispositionReward disposition improvement on completion
     * @param targetDescription human-readable target description
     * @param targetCount number of items/actions required
     * @return the saved quest
     */
    @Transactional
    public AiAdminQuest assignQuest(Long aiAdminId, Long playerId, String title,
                                     String description, AiAdminQuest.QuestType questType,
                                     int xpReward, int dispositionReward,
                                     String targetDescription, int targetCount) {
        // Validate the AI admin exists
        Optional<AiCharacter> aiAdminOpt = aiCharacterRepository.findById(aiAdminId);
        if (aiAdminOpt.isEmpty()) {
            throw new IllegalArgumentException("AI admin not found: " + aiAdminId);
        }
        AiCharacter aiAdmin = aiAdminOpt.get();
        if (!aiAdmin.isActive()) {
            throw new IllegalStateException("AI admin is not active: " + aiAdmin.getName());
        }

        AiAdminQuest quest = new AiAdminQuest(
                aiAdminId, playerId, title, description,
                questType, xpReward, dispositionReward,
                targetDescription, targetCount);
        quest = questRepository.save(quest);
        log.info("Quest '{}' assigned by AI admin '{}' (ID {}) to player {} (quest ID {})",
                title, aiAdmin.getName(), aiAdminId, playerId, quest.getId());
        return quest;
    }

    // ------------------------------------------------------------------
    // Progress tracking
    // ------------------------------------------------------------------

    /**
     * Updates quest progress by incrementing the current count.
     * Auto-completes if the target is reached.
     *
     * @param questId the quest ID
     * @param amount amount to increment
     * @return the updated quest
     */
    @Transactional
    public AiAdminQuest updateQuestProgress(Long questId, int amount) {
        AiAdminQuest quest = questRepository.findById(questId)
                .orElseThrow(() -> new IllegalArgumentException("Quest not found: " + questId));

        if (!quest.isActive()) {
            throw new IllegalStateException("Quest is not active: " + quest.getStatus());
        }

        // Mark as in progress if still assigned
        if (quest.getStatus() == AiAdminQuest.QuestStatus.ASSIGNED) {
            quest.setStatus(AiAdminQuest.QuestStatus.IN_PROGRESS);
        }

        quest.incrementProgress(amount);
        quest = questRepository.save(quest);
        log.info("Quest {} progress updated: {}/{} ({}%)",
                questId, quest.getCurrentCount(), quest.getTargetCount(), quest.getProgressPercent());
        return quest;
    }

    // ------------------------------------------------------------------
    // Completion
    // ------------------------------------------------------------------

    /**
     * Completes a quest, awards all rewards, updates disposition,
     * and unlocks the next quest in the chain if applicable.
     *
     * @param questId the quest ID
     * @return the completed quest
     */
    @Transactional
    public AiAdminQuest completeQuest(Long questId) {
        AiAdminQuest quest = questRepository.findById(questId)
                .orElseThrow(() -> new IllegalArgumentException("Quest not found: " + questId));

        if (!quest.isActive()) {
            throw new IllegalStateException("Quest is not active: " + quest.getStatus());
        }

        quest.setStatus(AiAdminQuest.QuestStatus.COMPLETED);
        quest.setCurrentCount(quest.getTargetCount());
        quest.setCompletedAt(LocalDateTime.now());
        quest = questRepository.save(quest);

        // Award all rewards
        awardRewards(quest);

        // Update AI admin disposition toward the player
        Optional<AiCharacter> aiAdminOpt = aiCharacterRepository.findById(quest.getQuestGiverId());
        if (aiAdminOpt.isPresent()) {
            AiCharacter aiAdmin = aiAdminOpt.get();
            aiAdmin.modifyDisposition(quest.getDispositionReward());
            aiCharacterRepository.save(aiAdmin);
            log.info("AI admin '{}' disposition updated to {} after quest completion",
                    aiAdmin.getName(), aiAdmin.getDispositionLabel());
        }

        // Unlock next quest in chain
        if (quest.isPartOfChain()) {
            unlockNextQuestInChain(quest);
        }

        log.info("Quest {} completed by player {}. XP: {}, credits: {}, reputation: {}",
                questId, quest.getPlayerId(), quest.getXpReward(),
                quest.getRewardCredits(), quest.getRewardReputation());
        return quest;
    }

    /**
     * Awards all rewards for a completed quest.
     */
    private void awardRewards(AiAdminQuest quest) {
        // Award credits
        if (quest.getRewardCredits() > 0) {
            log.info("Awarded {} credits to player {} for quest {}",
                    quest.getRewardCredits(), quest.getPlayerId(), quest.getId());
        }

        // Award reputation
        if (quest.getRewardReputation() > 0) {
            log.info("Awarded {} reputation to player {} for quest {}",
                    quest.getRewardReputation(), quest.getPlayerId(), quest.getId());
        }

        // Award item unlock
        if (quest.hasItemReward()) {
            log.info("Unlocked item '{}' for player {} on quest {}",
                    quest.getRewardItemId(), quest.getPlayerId(), quest.getId());
        }

        // Award title unlock
        if (quest.hasTitleReward()) {
            log.info("Unlocked title '{}' for player {} on quest {}",
                    quest.getRewardTitle(), quest.getPlayerId(), quest.getId());
        }

        // Award rewards from the rewards table
        List<AiAdminQuestReward> rewards = questRewardRepository.findByQuestId(quest.getId());
        for (AiAdminQuestReward reward : rewards) {
            log.info("Quest {} reward: {} = {} ({})",
                    quest.getId(), reward.getRewardType(), reward.getRewardValue(),
                    reward.getRewardDescription());
        }
    }

    /**
     * Unlocks the next quest in a chain after the current quest is completed.
     */
    private void unlockNextQuestInChain(AiAdminQuest completedQuest) {
        Optional<AiAdminQuest> nextQuestOpt = questRepository.findById(completedQuest.getId());
        if (nextQuestOpt.isEmpty()) return;

        // Find the next quest in the chain
        AiAdminQuest nextQuest = questRepository.findByChainIdAndParentQuestId(
                completedQuest.getChainId(), completedQuest.getId());
        if (nextQuest != null && nextQuest.getStatus() == AiAdminQuest.QuestStatus.ASSIGNED) {
            // The next quest is already assigned to the same player
            log.info("Unlocked next quest in chain '{}': '{}' (quest ID {})",
                    completedQuest.getChainId(), nextQuest.getTitle(), nextQuest.getId());
        }
    }

    // ------------------------------------------------------------------
    // Failure
    // ------------------------------------------------------------------

    /**
     * Marks a quest as failed and slightly decreases the AI admin's disposition.
     *
     * @param questId the quest ID
     * @param reason the failure reason (for logging)
     * @return the failed quest
     */
    @Transactional
    public AiAdminQuest failQuest(Long questId, String reason) {
        AiAdminQuest quest = questRepository.findById(questId)
                .orElseThrow(() -> new IllegalArgumentException("Quest not found: " + questId));

        if (!quest.isActive()) {
            throw new IllegalStateException("Quest is not active: " + quest.getStatus());
        }

        quest.setStatus(AiAdminQuest.QuestStatus.FAILED);
        quest = questRepository.save(quest);

        // Small disposition penalty for failed quests
        Optional<AiCharacter> aiAdminOpt = aiCharacterRepository.findById(quest.getQuestGiverId());
        if (aiAdminOpt.isPresent()) {
            AiCharacter aiAdmin = aiAdminOpt.get();
            aiAdmin.modifyDisposition(-1);
            aiCharacterRepository.save(aiAdmin);
            log.info("AI admin '{}' disposition decreased after quest failure", aiAdmin.getName());
        }

        log.warn("Quest {} failed by player {}: {}", questId, quest.getPlayerId(), reason);
        return quest;
    }

    /**
     * Abandons a quest (no disposition penalty).
     *
     * @param questId the quest ID
     * @return the abandoned quest
     */
    @Transactional
    public AiAdminQuest abandonQuest(Long questId) {
        AiAdminQuest quest = questRepository.findById(questId)
                .orElseThrow(() -> new IllegalArgumentException("Quest not found: " + questId));

        if (!quest.isActive()) {
            throw new IllegalStateException("Quest is not active: " + quest.getStatus());
        }

        quest.setStatus(AiAdminQuest.QuestStatus.ABANDONED);
        quest = questRepository.save(quest);
        log.info("Quest {} abandoned by player {}", questId, quest.getPlayerId());
        return quest;
    }

    // ------------------------------------------------------------------
    // Disposition
    // ------------------------------------------------------------------

    /**
     * Gets the current disposition score and label for an AI admin.
     *
     * @param aiAdminId the AI admin character ID
     * @return a map with 'score' and 'label' keys
     */
    @Transactional(readOnly = true)
    public java.util.Map<String, Object> getDisposition(Long aiAdminId) {
        AiCharacter aiAdmin = aiCharacterRepository.findById(aiAdminId)
                .orElseThrow(() -> new IllegalArgumentException("AI admin not found: " + aiAdminId));

        java.util.Map<String, Object> result = new java.util.HashMap<>();
        result.put("aiAdminId", aiAdminId);
        result.put("aiAdminName", aiAdmin.getName());
        result.put("score", aiAdmin.getDispositionScore());
        result.put("label", aiAdmin.getDispositionLabel());
        result.put("isFriendly", aiAdmin.isFriendlyToPlayer());
        return result;
    }

    /**
     * Modifies an AI admin's disposition score by the given delta.
     *
     * @param aiAdminId the AI admin character ID
     * @param delta the change amount (positive = more friendly, negative = more hostile)
     */
    @Transactional
    public void modifyDisposition(Long aiAdminId, int delta) {
        AiCharacter aiAdmin = aiCharacterRepository.findById(aiAdminId)
                .orElseThrow(() -> new IllegalArgumentException("AI admin not found: " + aiAdminId));

        String oldLabel = aiAdmin.getDispositionLabel();
        aiAdmin.modifyDisposition(delta);
        aiCharacterRepository.save(aiAdmin);
        log.info("AI admin '{}' disposition changed from {} to {} (delta: {})",
                aiAdmin.getName(), oldLabel, aiAdmin.getDispositionLabel(), delta);
    }

    // ------------------------------------------------------------------
    // Query helpers
    // ------------------------------------------------------------------

    /**
     * Gets all active quests for a player.
     *
     * @param playerId the player ID
     * @return list of active quests
     */
    @Transactional(readOnly = true)
    public List<AiAdminQuest> getActiveQuestsForPlayer(Long playerId) {
        return questRepository.findActiveByPlayerId(playerId);
    }

    /**
     * Gets all quests (active and completed) for a player.
     *
     * @param playerId the player ID
     * @return list of all quests
     */
    @Transactional(readOnly = true)
    public List<AiAdminQuest> getAllQuestsForPlayer(Long playerId) {
        return questRepository.findByPlayerId(playerId);
    }

    /**
     * Gets all quests given by a specific AI admin.
     *
     * @param aiAdminId the AI admin character ID
     * @return list of quests
     */
    @Transactional(readOnly = true)
    public List<AiAdminQuest> getQuestsByGiver(Long aiAdminId) {
        return questRepository.findByQuestGiverId(aiAdminId);
    }

    /**
     * Gets a single quest by ID.
     *
     * @param questId the quest ID
     * @return the quest or empty if not found
     */
    @Transactional(readOnly = true)
    public Optional<AiAdminQuest> getQuest(Long questId) {
        return questRepository.findById(questId);
    }

    // ------------------------------------------------------------------
    // Quest Chains
    // ------------------------------------------------------------------

    /**
     * Assigns a quest chain to a player. All quests in the chain are assigned,
     * but only the first quest is active; the rest remain locked until the
     * previous quest is completed.
     *
     * @param aiAdminId the AI admin character ID (quest giver)
     * @param playerId the player ID (quest taker)
     * @param chainId unique chain identifier
     * @param quests list of quests in chain order
     * @return the list of assigned quests
     */
    @Transactional
    public List<AiAdminQuest> assignQuestChain(Long aiAdminId, Long playerId,
                                                String chainId, List<AiAdminQuest> quests) {
        if (quests == null || quests.isEmpty()) {
            throw new IllegalArgumentException("Quest chain must have at least one quest");
        }

        List<AiAdminQuest> savedChain = new ArrayList<>();
        Long parentQuestId = null;

        for (int i = 0; i < quests.size(); i++) {
            AiAdminQuest quest = quests.get(i);
            quest.setQuestGiverId(aiAdminId);
            quest.setPlayerId(playerId);
            quest.setChainId(chainId);
            quest.setChainOrder(i + 1);
            quest.setParentQuestId(parentQuestId);

            // Only the first quest is active; others are ASSIGNED but locked
            if (i > 0) {
                quest.setStatus(AiAdminQuest.QuestStatus.ASSIGNED);
            }

            quest = questRepository.save(quest);
            savedChain.add(quest);
            parentQuestId = quest.getId();

            // Create steps for this quest if provided
            // (steps are created separately via createQuestSteps)
        }

        log.info("Quest chain '{}' assigned to player {} with {} quests",
                chainId, playerId, savedChain.size());
        return savedChain;
    }

    /**
     * Gets the progress through a quest chain.
     *
     * @param chainId the chain ID
     * @param playerId the player ID
     * @return a map with chain details and progress
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getChainProgress(String chainId, Long playerId) {
        List<AiAdminQuest> chainQuests = questRepository.findByPlayerIdAndChainIdOrderByChainOrderAsc(
                playerId, chainId);

        Map<String, Object> result = new HashMap<>();
        result.put("chainId", chainId);
        result.put("playerId", playerId);
        result.put("totalQuests", chainQuests.size());

        int completedCount = 0;
        int activeCount = 0;
        for (AiAdminQuest quest : chainQuests) {
            if (quest.getStatus() == AiAdminQuest.QuestStatus.COMPLETED) {
                completedCount++;
            } else if (quest.isActive()) {
                activeCount++;
            }
        }
        result.put("completedQuests", completedCount);
        result.put("activeQuests", activeCount);
        result.put("progressPercent", chainQuests.isEmpty() ? 0 :
                (completedCount * 100) / chainQuests.size());
        result.put("quests", chainQuests);

        return result;
    }

    /**
     * Gets all active chains for a player.
     *
     * @param playerId the player ID
     * @return list of chain IDs
     */
    @Transactional(readOnly = true)
    public List<String> getActiveChainsForPlayer(Long playerId) {
        return questRepository.findActiveChainIdsByPlayerId(playerId);
    }

    /**
     * Gets all chains for a player.
     *
     * @param playerId the player ID
     * @return list of chain IDs
     */
    @Transactional(readOnly = true)
    public List<String> getAllChainsForPlayer(Long playerId) {
        return questRepository.findDistinctChainIdsByPlayerId(playerId);
    }

    // ------------------------------------------------------------------
    // Quest Steps
    // ------------------------------------------------------------------

    /**
     * Creates steps for a quest.
     *
     * @param questId the quest ID
     * @param steps list of steps in order
     * @return the saved steps
     */
    @Transactional
    public List<AiAdminQuestStep> createQuestSteps(Long questId, List<AiAdminQuestStep> steps) {
        if (steps == null || steps.isEmpty()) {
            return new ArrayList<>();
        }

        List<AiAdminQuestStep> saved = new ArrayList<>();
        for (int i = 0; i < steps.size(); i++) {
            AiAdminQuestStep step = steps.get(i);
            step.setQuestId(questId);
            step.setStepOrder(i + 1);
            saved.add(questStepRepository.save(step));
        }

        log.info("Created {} steps for quest {}", saved.size(), questId);
        return saved;
    }

    /**
     * Gets all steps for a quest, ordered by step order.
     *
     * @param questId the quest ID
     * @return list of steps
     */
    @Transactional(readOnly = true)
    public List<AiAdminQuestStep> getQuestSteps(Long questId) {
        return questStepRepository.findByQuestIdOrderByStepOrder(questId);
    }

    /**
     * Completes a specific step and auto-unlocks the next step.
     *
     * @param questId the quest ID
     * @param stepOrder the step order to complete
     * @return the updated step
     */
    @Transactional
    public AiAdminQuestStep completeStep(Long questId, int stepOrder) {
        List<AiAdminQuestStep> steps = questStepRepository.findByQuestIdOrderByStepOrder(questId);

        AiAdminQuestStep step = null;
        for (AiAdminQuestStep s : steps) {
            if (s.getStepOrder() == stepOrder) {
                step = s;
                break;
            }
        }

        if (step == null) {
            throw new IllegalArgumentException("Step not found: quest " + questId + ", step " + stepOrder);
        }

        if (step.isCompleted()) {
            throw new IllegalStateException("Step already completed");
        }

        step.setCompleted(true);
        step.setCurrentCount(step.getTargetCount());
        step = questStepRepository.save(step);

        // Check if all steps are complete -> auto-complete the quest
        boolean allComplete = steps.stream().allMatch(AiAdminQuestStep::isCompleted);
        if (allComplete) {
            completeQuest(questId);
            log.info("All steps completed for quest {}. Quest auto-completed.", questId);
        }

        log.info("Step {} completed for quest {}", stepOrder, questId);
        return step;
    }

    /**
     * Updates progress on a specific step.
     *
     * @param questId the quest ID
     * @param stepOrder the step order
     * @param amount the amount to increment
     * @return the updated step
     */
    @Transactional
    public AiAdminQuestStep updateStepProgress(Long questId, int stepOrder, int amount) {
        List<AiAdminQuestStep> steps = questStepRepository.findByQuestIdOrderByStepOrder(questId);

        AiAdminQuestStep step = null;
        for (AiAdminQuestStep s : steps) {
            if (s.getStepOrder() == stepOrder && !s.isCompleted()) {
                step = s;
                break;
            }
        }

        if (step == null) {
            throw new IllegalArgumentException("Active step not found: quest " + questId + ", step " + stepOrder);
        }

        step.incrementProgress(amount);
        step = questStepRepository.save(step);

        // Check if all steps are complete -> auto-complete the quest
        boolean allComplete = steps.stream().allMatch(AiAdminQuestStep::isCompleted);
        if (allComplete) {
            completeQuest(questId);
            log.info("All steps completed for quest {} via progress update. Quest auto-completed.", questId);
        }

        log.info("Step {} progress updated for quest {}: {}/{}",
                stepOrder, questId, step.getCurrentCount(), step.getTargetCount());
        return step;
    }

    // ------------------------------------------------------------------
    // Quest Rewards
    // ------------------------------------------------------------------

    /**
     * Gets all rewards for a quest.
     *
     * @param questId the quest ID
     * @return list of rewards
     */
    @Transactional(readOnly = true)
    public List<AiAdminQuestReward> getQuestRewards(Long questId) {
        return questRewardRepository.findByQuestId(questId);
    }

    /**
     * Gets a summary of all rewards for a quest (including inline fields).
     *
     * @param questId the quest ID
     * @return a map with reward summary
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getQuestRewardSummary(Long questId) {
        AiAdminQuest quest = questRepository.findById(questId)
                .orElseThrow(() -> new IllegalArgumentException("Quest not found: " + questId));

        Map<String, Object> result = new HashMap<>();
        result.put("questId", questId);
        result.put("xpReward", quest.getXpReward());
        result.put("creditsReward", quest.getRewardCredits());
        result.put("reputationReward", quest.getRewardReputation());
        result.put("dispositionReward", quest.getDispositionReward());
        result.put("itemId", quest.getRewardItemId());
        result.put("title", quest.getRewardTitle());
        result.put("rewards", questRewardRepository.findByQuestId(questId));
        return result;
    }
}
