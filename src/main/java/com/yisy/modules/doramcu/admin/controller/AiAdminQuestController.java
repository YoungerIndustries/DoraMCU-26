package com.yisy.modules.doramcu.admin.controller;

import com.yisy.modules.doramcu.admin.model.AiAdminQuest;
import com.yisy.modules.doramcu.admin.model.AiAdminQuestStep;
import com.yisy.modules.doramcu.admin.service.AiAdminInteractionService;
import com.yisy.modules.doramcu.model.players.Player;
import com.yisy.modules.doramcu.model.user.User;
import com.yisy.modules.doramcu.repository.players.PlayerRepository;
import com.yisy.modules.doramcu.repository.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * REST controller for AI admin quest management endpoints.
 * Extracted from AiAdminController to reduce god class size.
 * <p>
 * Provides quest assignment, progress tracking, completion, and quest chain management.
 * </p>
 */
@RestController
@RequestMapping("/api/v1/admin/ai-orders")
public class AiAdminQuestController {

    private static final Logger log = LoggerFactory.getLogger(AiAdminQuestController.class);

    private final AiAdminInteractionService interactionService;
    private final PlayerRepository playerRepository;
    private final UserRepository userRepository;

    public AiAdminQuestController(AiAdminInteractionService interactionService,
                                   PlayerRepository playerRepository,
                                   UserRepository userRepository) {
        this.interactionService = interactionService;
        this.playerRepository = playerRepository;
        this.userRepository = userRepository;
    }

    // ------------------------------------------------------------------
    // Quest endpoints
    // ------------------------------------------------------------------

    /**
     * Assign a quest from an AI admin to a player.
     */
    @PostMapping("/admins/{aiAdminId}/quest")
    public ResponseEntity<Map<String, Object>> assignQuest(
            @PathVariable Long aiAdminId,
            @RequestBody AiAdminQuestRequest request,
            Authentication authentication) {
        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
        Player player = playerRepository.findByUser(user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Player not found"));

        AiAdminQuest.QuestType questType = AiAdminQuest.QuestType.OTHER;
        try {
            questType = AiAdminQuest.QuestType.valueOf(request.getQuestType());
        } catch (IllegalArgumentException e) {
            // default to OTHER
        }

        AiAdminQuest quest = interactionService.assignQuest(
                aiAdminId, player.getId(),
                request.getTitle(), request.getDescription(),
                questType,
                request.getXpReward() != null ? request.getXpReward() : 10,
                request.getDispositionReward() != null ? request.getDispositionReward() : 1,
                request.getTargetDescription(),
                request.getTargetCount() != null ? request.getTargetCount() : 1);

        Map<String, Object> result = questToMap(quest);
        log.info("Quest '{}' assigned by AI admin {} to player {}", quest.getTitle(), aiAdminId, player.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    /**
     * List all quests given by a specific AI admin.
     */
    @GetMapping("/admins/{aiAdminId}/quests")
    public ResponseEntity<List<Map<String, Object>>> getQuestsByAdmin(@PathVariable Long aiAdminId) {
        List<AiAdminQuest> quests = interactionService.getQuestsByGiver(aiAdminId);
        return ResponseEntity.ok(quests.stream().map(this::questToMap).collect(Collectors.toList()));
    }

    /**
     * Get all active quests for the current player.
     */
    @GetMapping("/quests/player/active")
    public ResponseEntity<List<Map<String, Object>>> getActiveQuests(Authentication authentication) {
        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
        Player player = playerRepository.findByUser(user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Player not found"));

        List<AiAdminQuest> quests = interactionService.getActiveQuestsForPlayer(player.getId());
        return ResponseEntity.ok(quests.stream().map(this::questToMap).collect(Collectors.toList()));
    }

    /**
     * Update quest progress.
     */
    @PostMapping("/quests/{questId}/progress")
    public ResponseEntity<Map<String, Object>> updateQuestProgress(
            @PathVariable Long questId,
            @RequestBody QuestProgressRequest request) {
        AiAdminQuest quest = interactionService.updateQuestProgress(questId, request.getAmount());
        return ResponseEntity.ok(questToMap(quest));
    }

    /**
     * Complete a quest.
     */
    @PostMapping("/quests/{questId}/complete")
    public ResponseEntity<Map<String, Object>> completeQuest(@PathVariable Long questId) {
        AiAdminQuest quest = interactionService.completeQuest(questId);
        return ResponseEntity.ok(questToMap(quest));
    }

    /**
     * Fail a quest.
     */
    @PostMapping("/quests/{questId}/fail")
    public ResponseEntity<Map<String, Object>> failQuest(
            @PathVariable Long questId,
            @RequestBody QuestFailRequest request) {
        AiAdminQuest quest = interactionService.failQuest(questId, request.getReason());
        return ResponseEntity.ok(questToMap(quest));
    }

    /**
     * Abandon a quest.
     */
    @PostMapping("/quests/{questId}/abandon")
    public ResponseEntity<Map<String, Object>> abandonQuest(@PathVariable Long questId) {
        AiAdminQuest quest = interactionService.abandonQuest(questId);
        return ResponseEntity.ok(questToMap(quest));
    }

    /**
     * Get disposition toward an AI admin.
     */
    @GetMapping("/admins/{aiAdminId}/disposition")
    public ResponseEntity<Map<String, Object>> getDisposition(@PathVariable Long aiAdminId) {
        return ResponseEntity.ok(interactionService.getDisposition(aiAdminId));
    }

    // ------------------------------------------------------------------
    // Advanced Quest endpoints (chains, steps, rewards)
    // ------------------------------------------------------------------

    /**
     * Assign a quest chain to a player (admin-only).
     */
    @PostMapping("/admins/{adminId}/quest-chain")
    public ResponseEntity<?> assignQuestChain(@PathVariable Long adminId,
                                               @RequestBody AiAdminQuestChainRequest request,
                                               Authentication authentication) {
        if (!isAdmin(authentication)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Admin access required"));
        }
        try {
            List<AiAdminQuest> quests = new ArrayList<>();
            for (AiAdminQuestRequest qReq : request.getQuests()) {
                AiAdminQuest quest = new AiAdminQuest(
                        adminId, request.getPlayerId(),
                        qReq.getTitle(), qReq.getDescription(),
                        AiAdminQuest.QuestType.valueOf(qReq.getQuestType()),
                        qReq.getXpReward() != null ? qReq.getXpReward() : 10,
                        qReq.getDispositionReward() != null ? qReq.getDispositionReward() : 1,
                        qReq.getTargetDescription(),
                        qReq.getTargetCount() != null ? qReq.getTargetCount() : 1);
                quests.add(quest);
            }
            List<AiAdminQuest> saved = interactionService.assignQuestChain(
                    adminId, request.getPlayerId(), request.getChainId(), quests);
            List<Map<String, Object>> result = new ArrayList<>();
            for (AiAdminQuest q : saved) {
                result.add(questToMap(q));
            }
            return ResponseEntity.ok(Map.of(
                    "chainId", request.getChainId(),
                    "quests", result,
                    "message", "Quest chain assigned successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get quest steps for a quest.
     */
    @GetMapping("/quests/{questId}/steps")
    public ResponseEntity<?> getQuestSteps(@PathVariable Long questId) {
        try {
            List<AiAdminQuestStep> steps = interactionService.getQuestSteps(questId);
            List<Map<String, Object>> result = new ArrayList<>();
            for (AiAdminQuestStep step : steps) {
                Map<String, Object> map = new HashMap<>();
                map.put("id", step.getId());
                map.put("questId", step.getQuestId());
                map.put("stepOrder", step.getStepOrder());
                map.put("title", step.getTitle());
                map.put("description", step.getDescription());
                map.put("targetCount", step.getTargetCount());
                map.put("currentCount", step.getCurrentCount());
                map.put("completed", step.isCompleted());
                map.put("progressPercent", step.getProgressPercent());
                result.add(map);
            }
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Complete a specific step.
     */
    @PostMapping("/quests/{questId}/steps/{stepOrder}/complete")
    public ResponseEntity<?> completeStep(@PathVariable Long questId,
                                           @PathVariable int stepOrder) {
        try {
            AiAdminQuestStep step = interactionService.completeStep(questId, stepOrder);
            Map<String, Object> map = new HashMap<>();
            map.put("id", step.getId());
            map.put("stepOrder", step.getStepOrder());
            map.put("title", step.getTitle());
            map.put("completed", step.isCompleted());
            return ResponseEntity.ok(map);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Update progress on a specific step.
     */
    @PostMapping("/quests/{questId}/steps/{stepOrder}/progress")
    public ResponseEntity<?> updateStepProgress(@PathVariable Long questId,
                                                  @PathVariable int stepOrder,
                                                  @RequestBody QuestProgressRequest request) {
        try {
            AiAdminQuestStep step = interactionService.updateStepProgress(
                    questId, stepOrder, request.getAmount());
            Map<String, Object> map = new HashMap<>();
            map.put("id", step.getId());
            map.put("stepOrder", step.getStepOrder());
            map.put("currentCount", step.getCurrentCount());
            map.put("targetCount", step.getTargetCount());
            map.put("completed", step.isCompleted());
            return ResponseEntity.ok(map);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get all rewards for a quest.
     */
    @GetMapping("/quests/{questId}/rewards")
    public ResponseEntity<?> getQuestRewards(@PathVariable Long questId) {
        try {
            Map<String, Object> summary = interactionService.getQuestRewardSummary(questId);
            return ResponseEntity.ok(summary);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get chain progress for a player.
     */
    @GetMapping("/chains/{chainId}/progress")
    public ResponseEntity<?> getChainProgress(@PathVariable String chainId,
                                               @RequestParam Long playerId) {
        try {
            Map<String, Object> progress = interactionService.getChainProgress(chainId, playerId);
            return ResponseEntity.ok(progress);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get all active chains for a player.
     */
    @GetMapping("/player/{playerId}/chains")
    public ResponseEntity<?> getPlayerChains(@PathVariable Long playerId) {
        try {
            List<String> chains = interactionService.getAllChainsForPlayer(playerId);
            return ResponseEntity.ok(Map.of("chains", chains, "count", chains.size()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ------------------------------------------------------------------
    // Quest helpers
    // ------------------------------------------------------------------

    private Map<String, Object> questToMap(AiAdminQuest quest) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", quest.getId());
        map.put("questGiverId", quest.getQuestGiverId());
        map.put("playerId", quest.getPlayerId());
        map.put("title", quest.getTitle());
        map.put("description", quest.getDescription());
        map.put("questType", quest.getQuestType().name());
        map.put("status", quest.getStatus().name());
        map.put("xpReward", quest.getXpReward());
        map.put("dispositionReward", quest.getDispositionReward());
        map.put("targetDescription", quest.getTargetDescription());
        map.put("targetCount", quest.getTargetCount());
        map.put("currentCount", quest.getCurrentCount());
        map.put("progressPercent", quest.getProgressPercent());
        map.put("assignedAt", quest.getAssignedAt());
        map.put("completedAt", quest.getCompletedAt());
        map.put("isActive", quest.isActive());
        // Chain fields
        map.put("chainId", quest.getChainId());
        map.put("chainOrder", quest.getChainOrder());
        map.put("parentQuestId", quest.getParentQuestId());
        map.put("isPartOfChain", quest.isPartOfChain());
        // Reward fields
        map.put("rewardCredits", quest.getRewardCredits());
        map.put("rewardReputation", quest.getRewardReputation());
        map.put("rewardItemId", quest.getRewardItemId());
        map.put("rewardTitle", quest.getRewardTitle());
        return map;
    }

    private boolean isAdmin(Authentication authentication) {
        String username = authentication.getName();
        var playerOpt = playerRepository.findByEmail(username);
        if (playerOpt.isEmpty()) {
            playerOpt = playerRepository.findByEmail(username + "@doramcu.local");
        }
        if (playerOpt.isEmpty()) {
            var userOpt = userRepository.findByUsername(username);
            if (userOpt.isPresent()) {
                playerOpt = playerRepository.findByUser(userOpt.get());
            }
        }
        return playerOpt.map(Player::isAdmin).orElse(false);
    }

    /**
     * Request body for quest assignment.
     */
    public static class AiAdminQuestRequest {
        private String title;
        private String description;
        private String questType = "OTHER";
        private Integer xpReward;
        private Integer dispositionReward;
        private String targetDescription;
        private Integer targetCount;

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getQuestType() { return questType; }
        public void setQuestType(String questType) { this.questType = questType; }
        public Integer getXpReward() { return xpReward; }
        public void setXpReward(Integer xpReward) { this.xpReward = xpReward; }
        public Integer getDispositionReward() { return dispositionReward; }
        public void setDispositionReward(Integer dispositionReward) { this.dispositionReward = dispositionReward; }
        public String getTargetDescription() { return targetDescription; }
        public void setTargetDescription(String targetDescription) { this.targetDescription = targetDescription; }
        public Integer getTargetCount() { return targetCount; }
        public void setTargetCount(Integer targetCount) { this.targetCount = targetCount; }
    }

    /**
     * Request body for quest progress update.
     */
    public static class QuestProgressRequest {
        private int amount = 1;

        public int getAmount() { return amount; }
        public void setAmount(int amount) { this.amount = amount; }
    }

    /**
     * Request body for quest failure.
     */
    public static class QuestFailRequest {
        private String reason;

        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
    }

    /**
     * Request body for quest chain assignment.
     */
    public static class AiAdminQuestChainRequest {
        private String chainId;
        private Long playerId;
        private List<AiAdminQuestRequest> quests;

        public String getChainId() { return chainId; }
        public void setChainId(String chainId) { this.chainId = chainId; }
        public Long getPlayerId() { return playerId; }
        public void setPlayerId(Long playerId) { this.playerId = playerId; }
        public List<AiAdminQuestRequest> getQuests() { return quests; }
        public void setQuests(List<AiAdminQuestRequest> quests) { this.quests = quests; }
    }
}
