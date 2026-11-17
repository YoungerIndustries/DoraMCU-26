package com.yisy.modules.doramcu.admin.service;

import com.yisy.modules.doramcu.admin.model.AiAdminQuest;
import com.yisy.modules.doramcu.eve.model.AiCharacter;
import com.yisy.modules.doramcu.repository.ai.AiAdminQuestRepository;
import com.yisy.modules.doramcu.repository.ai.AiCharacterRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
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

    public AiAdminInteractionService(AiAdminQuestRepository questRepository,
                                      AiCharacterRepository aiCharacterRepository) {
        this.questRepository = questRepository;
        this.aiCharacterRepository = aiCharacterRepository;
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
     * Completes a quest, awards XP to the player's character (if any),
     * and updates the AI admin's disposition toward the player.
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
        quest = questRepository.save(quest);

        // Update AI admin disposition toward the player
        Optional<AiCharacter> aiAdminOpt = aiCharacterRepository.findById(quest.getQuestGiverId());
        if (aiAdminOpt.isPresent()) {
            AiCharacter aiAdmin = aiAdminOpt.get();
            aiAdmin.modifyDisposition(quest.getDispositionReward());
            aiCharacterRepository.save(aiAdmin);
            log.info("AI admin '{}' disposition updated to {} after quest completion",
                    aiAdmin.getName(), aiAdmin.getDispositionLabel());
        }

        log.info("Quest {} completed by player {}. XP reward: {}, disposition reward: {}",
                questId, quest.getPlayerId(), quest.getXpReward(), quest.getDispositionReward());
        return quest;
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
}
