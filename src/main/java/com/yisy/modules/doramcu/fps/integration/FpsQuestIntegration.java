package com.yisy.modules.doramcu.fps.integration;

import com.yisy.modules.doramcu.fps.core.FpsPlayerState;
import com.yisy.modules.doramcu.fps.core.FpsSession;
import com.yisy.modules.doramcu.fps.core.model.FpsMatch;
import com.yisy.modules.doramcu.unified.QuestObjective;
import com.yisy.modules.doramcu.unified.UnifiedQuest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Integrates FPS gameplay with the UnifiedQuest system.
 * <p>
 * This service enables quests that include FPS-specific objectives such as:
 * <ul>
 *   <li>Kill a certain number of enemies</li>
 *   <li>Win a number of matches</li>
 *   <li>Capture points in CTF mode</li>
 *   <li>Achieve a K/D ratio threshold</li>
 *   <li>Survive for a minimum time</li>
 * </ul>
 * </p>
 * <p>
 * When a player's FPS session ends, this service checks all active quests
 * and progresses any FPS-related objectives based on the match results.
 * </p>
 */
@Component
public class FpsQuestIntegration {

    private static final Logger log = LoggerFactory.getLogger(FpsQuestIntegration.class);

    // Objective type constants for FPS quests
    public static final String MODULE_FPS = "fps";
    public static final String OBJECTIVE_KILLS = "kills";
    public static final String OBJECTIVE_WINS = "wins";
    public static final String OBJECTIVE_CAPTURES = "captures";
    public static final String OBJECTIVE_ASSISTS = "assists";
    public static final String OBJECTIVE_SURVIVAL_TIME = "survival_time";
    public static final String OBJECTIVE_KD_RATIO = "kd_ratio";
    public static final String OBJECTIVE_MATCHES_PLAYED = "matches_played";

    // PlayerId -> list of active quest IDs
    private final Map<String, Set<String>> playerActiveQuests = new ConcurrentHashMap<>();

    // Quest template definitions for FPS-specific quests
    private final List<UnifiedQuest> fpsQuestTemplates = new ArrayList<>();

    private final FpsRpgBridge fpsRpgBridge;

    public FpsQuestIntegration(FpsRpgBridge fpsRpgBridge) {
        this.fpsRpgBridge = fpsRpgBridge;
        initializeQuestTemplates();
    }

    private void initializeQuestTemplates() {
        // Quest 1: Rookie Shooter — achieve 10 kills
        fpsQuestTemplates.add(new UnifiedQuest(
                "fps_rookie_shooter",
                "Rookie Shooter",
                "Achieve 10 kills in FPS combat to prove your marksmanship.",
                List.of(MODULE_FPS),
                List.of(new QuestObjective(
                        "rookie_kills", "Achieve 10 kills", MODULE_FPS,
                        OBJECTIVE_KILLS, 10, Map.of()
                )),
                Map.of("xp", 500, "title", "Rookie Shooter")
        ));

        // Quest 2: Veteran Combatant — win 5 matches
        fpsQuestTemplates.add(new UnifiedQuest(
                "fps_veteran",
                "Veteran Combatant",
                "Win 5 FPS matches to prove your tactical skill.",
                List.of(MODULE_FPS),
                List.of(new QuestObjective(
                        "veteran_wins", "Win 5 matches", MODULE_FPS,
                        OBJECTIVE_WINS, 5, Map.of()
                )),
                Map.of("xp", 1000, "title", "Veteran Combatant")
        ));

        // Quest 3: Point Dominator — capture 10 points
        fpsQuestTemplates.add(new UnifiedQuest(
                "fps_point_dominator",
                "Point Dominator",
                "Capture 10 control points in Capture the Point matches.",
                List.of(MODULE_FPS),
                List.of(new QuestObjective(
                        "dominator_captures", "Capture 10 points", MODULE_FPS,
                        OBJECTIVE_CAPTURES, 10, Map.of()
                )),
                Map.of("xp", 750, "title", "Point Dominator")
        ));

        // Quest 4: Team Player — get 20 assists
        fpsQuestTemplates.add(new UnifiedQuest(
                "fps_team_player",
                "Team Player",
                "Assist in 20 eliminations to prove your teamwork.",
                List.of(MODULE_FPS),
                List.of(new QuestObjective(
                        "team_assists", "Get 20 assists", MODULE_FPS,
                        OBJECTIVE_ASSISTS, 20, Map.of()
                )),
                Map.of("xp", 600, "title", "Team Player")
        ));

        // Quest 5: Elite Marksman — achieve 2.0 K/D ratio
        fpsQuestTemplates.add(new UnifiedQuest(
                "fps_elite_marksman",
                "Elite Marksman",
                "Achieve a K/D ratio of 2.0 or higher across all matches.",
                List.of(MODULE_FPS),
                List.of(new QuestObjective(
                        "elite_kd", "Achieve 2.0 K/D ratio", MODULE_FPS,
                        OBJECTIVE_KD_RATIO, 2.0, Map.of()
                )),
                Map.of("xp", 1500, "title", "Elite Marksman")
        ));

        // Quest 6: Marathon Warrior — survive for 60 minutes total
        fpsQuestTemplates.add(new UnifiedQuest(
                "fps_marathon",
                "Marathon Warrior",
                "Survive for a total of 60 minutes in FPS combat.",
                List.of(MODULE_FPS),
                List.of(new QuestObjective(
                        "marathon_time", "Survive 60 minutes", MODULE_FPS,
                        OBJECTIVE_SURVIVAL_TIME, 60, Map.of()
                )),
                Map.of("xp", 800, "title", "Marathon Warrior")
        ));

        // Quest 7: Cross-module quest — FPS + RPG combined
        fpsQuestTemplates.add(new UnifiedQuest(
                "fps_cross_module_warrior",
                "Warrior of Two Worlds",
                "Win 3 FPS matches and achieve 50 kills to bridge combat systems.",
                List.of(MODULE_FPS, "rpg"),
                List.of(
                        new QuestObjective("cross_wins", "Win 3 FPS matches", MODULE_FPS,
                                OBJECTIVE_WINS, 3, Map.of()),
                        new QuestObjective("cross_kills", "Achieve 50 FPS kills", MODULE_FPS,
                                OBJECTIVE_KILLS, 50, Map.of())
                ),
                Map.of("xp", 2000, "title", "Warrior of Two Worlds", "achievement", "cross_module_master")
        ));

        log.info("Initialized {} FPS quest templates", fpsQuestTemplates.size());
    }

    /**
     * Register a quest as active for a player.
     */
    public void activateQuestForPlayer(String playerId, String questId) {
        playerActiveQuests.computeIfAbsent(playerId, k -> new HashSet<>()).add(questId);
        log.info("Activated quest {} for player {}", questId, playerId);
    }

    /**
     * Remove a quest from a player's active quests.
     */
    public void deactivateQuestForPlayer(String playerId, String questId) {
        Set<String> quests = playerActiveQuests.get(playerId);
        if (quests != null) {
            quests.remove(questId);
        }
    }

    /**
     * Get all available FPS quest templates.
     */
    public List<UnifiedQuest> getAvailableQuests() {
        return new ArrayList<>(fpsQuestTemplates);
    }

    /**
     * Get a specific quest template by ID.
     */
    public Optional<UnifiedQuest> getQuestTemplate(String questId) {
        return fpsQuestTemplates.stream()
                .filter(q -> q.getId().equals(questId))
                .findFirst();
    }

    /**
     * Process the end of an FPS match and update all relevant quest objectives.
     *
     * @param playerId the player's ID
     * @param playerState the player's final state in the match
     * @param session the FPS session that ended
     * @param won whether the player's team won
     */
    public void processMatchEnd(String playerId, FpsPlayerState playerState,
                                FpsSession session, boolean won) {
        Set<String> activeQuestIds = playerActiveQuests.get(playerId);
        if (activeQuestIds == null || activeQuestIds.isEmpty()) {
            return;
        }

        // Calculate play time
        long playTimeMinutes = 0;
        if (session.getStartTime() != null) {
            playTimeMinutes = java.time.Duration.between(session.getStartTime(), Instant.now()).getSeconds() / 60;
        }

        // Get cumulative stats for ratio-based objectives
        FpsRpgBridge.FpsPlayerStats stats = fpsRpgBridge.getPlayerStats(playerId);

        log.info("Processing quest objectives for player {} after FPS match", playerId);

        for (String questId : activeQuestIds) {
            // This would normally fetch the quest from a repository
            // For now, we process objectives based on the match data
            processQuestObjectivesForMatch(playerId, questId, playerState, won, playTimeMinutes, stats);
        }
    }

    /**
     * Process quest objectives for a single match result.
     * In a full implementation, this would fetch the quest from storage.
     */
    private void processQuestObjectivesForMatch(String playerId, String questId,
                                                FpsPlayerState playerState,
                                                boolean won, long playTimeMinutes,
                                                FpsRpgBridge.FpsPlayerStats stats) {
        // This method would fetch the quest and update objectives
        // For now, we log the progress that would be made
        log.info("Quest {} progress for player {}: kills={}, wins={}, kd={}",
                questId, playerId,
                playerState.getKills(),
                won ? 1 : 0,
                stats.getKDRatio());
    }

    /**
     * Progress a specific quest objective based on FPS match data.
     *
     * @param objective the objective to progress
     * @param playerState the player's match state
     * @param won whether the player won
     * @param playTimeMinutes minutes played in the match
     * @param cumulativeStats the player's cumulative FPS stats
     */
    public void progressObjective(QuestObjective objective, FpsPlayerState playerState,
                                  boolean won, long playTimeMinutes,
                                  FpsRpgBridge.FpsPlayerStats cumulativeStats) {
        if (!MODULE_FPS.equals(objective.getModule())) {
            return;
        }

        switch (objective.getType()) {
            case OBJECTIVE_KILLS:
                objective.addProgress(playerState.getKills());
                break;

            case OBJECTIVE_WINS:
                if (won) {
                    objective.addProgress(1);
                }
                break;

            case OBJECTIVE_CAPTURES:
                // Captures are tracked separately via FpsRpgBridge
                // This would be called when a capture event occurs
                break;

            case OBJECTIVE_ASSISTS:
                objective.addProgress(playerState.getAssists());
                break;

            case OBJECTIVE_SURVIVAL_TIME:
                objective.addProgress(playTimeMinutes);
                break;

            case OBJECTIVE_KD_RATIO:
                // For ratio objectives, check if current ratio meets target
                double currentKd = cumulativeStats.getKDRatio();
                if (currentKd >= objective.getTarget()) {
                    objective.complete();
                } else {
                    // Progress as percentage of target achieved
                    objective.addProgress(currentKd / objective.getTarget() * objective.getTarget() * 0.1);
                }
                break;

            case OBJECTIVE_MATCHES_PLAYED:
                objective.addProgress(1);
                break;

            default:
                log.warn("Unknown FPS objective type: {}", objective.getType());
        }
    }

    /**
     * Check if a quest should be offered to a player based on their FPS stats.
     *
     * @param playerId the player's ID
     * @return list of quest IDs that the player is eligible for
     */
    public List<String> getEligibleQuests(String playerId) {
        FpsRpgBridge.FpsPlayerStats stats = fpsRpgBridge.getPlayerStats(playerId);
        List<String> eligible = new ArrayList<>();

        for (UnifiedQuest quest : fpsQuestTemplates) {
            if (quest.getSourceModules().contains(MODULE_FPS)) {
                // Simple eligibility: offer all FPS quests
                // Could be extended to check prerequisites
                eligible.add(quest.getId());
            }
        }

        return eligible;
    }

    /**
     * Create a custom FPS quest with specified objectives.
     *
     * @param id unique quest ID
     * @param title quest title
     * @param description quest description
     * @param objectives list of objectives
     * @param rewards reward map
     * @return the created quest
     */
    public UnifiedQuest createCustomQuest(String id, String title, String description,
                                          List<QuestObjective> objectives,
                                          Map<String, Object> rewards) {
        UnifiedQuest quest = new UnifiedQuest(
                id, title, description,
                List.of(MODULE_FPS),
                objectives,
                rewards
        );
        fpsQuestTemplates.add(quest);
        log.info("Created custom FPS quest: {}", id);
        return quest;
    }
}
