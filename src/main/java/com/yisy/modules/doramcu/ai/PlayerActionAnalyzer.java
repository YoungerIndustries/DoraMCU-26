package com.yisy.modules.doramcu.ai;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service that intercepts and categorizes player actions to build behavioral personality profiles.
 * <p>
 * Every human player action is recorded and analyzed to compute personality trait scores.
 * The analyzer uses exponential moving averages to smooth out short-term anomalies and
 * provides confidence scores that increase with more observed actions.
 * </p>
 * <p>
 * Action categories map to personality axes as follows:
 * <ul>
 *   <li>COMBAT → aggression</li>
 *   <li>RETREAT → riskTolerance (inverse)</li>
 *   <li>EXPANSION → expansionDrive</li>
 *   <li>BUILD_SHIP → aggression + economicFocus</li>
 *   <li>BUILD_STATION → economicFocus</li>
 *   <li>RESEARCH → scientificFocus</li>
 *   <li>DEFENSIVE → diplomaticStance</li>
 *   <li>PASSIVE → riskTolerance (inverse)</li>
 *   <li>ALTRUISTIC → altruism</li>
 *   <li>DECEPTIVE → cunning</li>
 *   <li>PERSISTENT → determination</li>
 *   <li>ADAPTIVE → adaptability</li>
 *   <li>LEADERSHIP → leadership</li>
 *   <li>HONOR → honor</li>
 * </ul>
 * </p>
 */
@Service
public class PlayerActionAnalyzer {

    private static final Logger log = LoggerFactory.getLogger(PlayerActionAnalyzer.class);

    /** Smoothing factor for exponential moving average (0.1 = slow, 0.5 = fast). */
    private static final double SMOOTHING_FACTOR = 0.15;

    /** Maximum number of action records to keep per player. */
    private static final int MAX_RECORDS_PER_PLAYER = 500;

    /** Per-player action records. */
    private final Map<Integer, List<PlayerActionRecord>> playerActions = new ConcurrentHashMap<>();

    /** Per-player computed trait scores (exponential moving average). */
    private final Map<Integer, Map<PlayerActionCategory, Double>> playerTraitScores = new ConcurrentHashMap<>();

    /** Per-player action counts per category. */
    private final Map<Integer, Map<PlayerActionCategory, Integer>> playerActionCounts = new ConcurrentHashMap<>();

    /**
     * Records a player action for behavioral analysis.
     *
     * @param playerId the player's ID
     * @param category the action category
     * @param intensity the intensity of the action signal (0.0-1.0)
     * @param turnNumber the current turn number
     * @param context optional context description
     */
    public void recordAction(int playerId, PlayerActionCategory category, double intensity, int turnNumber, String context) {
        PlayerActionRecord record = new PlayerActionRecord(category, intensity, turnNumber, context);

        // Store the record
        playerActions.computeIfAbsent(playerId, k -> new ArrayList<>()).add(record);

        // Trim old records
        List<PlayerActionRecord> records = playerActions.get(playerId);
        while (records.size() > MAX_RECORDS_PER_PLAYER) {
            records.remove(0);
        }

        // Update trait scores using exponential moving average
        Map<PlayerActionCategory, Double> scores = playerTraitScores.computeIfAbsent(playerId, k -> new HashMap<>());
        double currentScore = scores.getOrDefault(category, 0.5);
        double newScore = currentScore * (1.0 - SMOOTHING_FACTOR) + intensity * SMOOTHING_FACTOR;
        scores.put(category, Math.max(0.0, Math.min(1.0, newScore)));

        // Update action counts
        Map<PlayerActionCategory, Integer> counts = playerActionCounts.computeIfAbsent(playerId, k -> new HashMap<>());
        counts.merge(category, 1, Integer::sum);

        log.debug("Recorded action for player {}: {} (intensity={:.2f})", playerId, category, intensity);
    }

    /**
     * Convenience method to record an action without context.
     */
    public void recordAction(int playerId, PlayerActionCategory category, double intensity, int turnNumber) {
        recordAction(playerId, category, intensity, turnNumber, null);
    }

    /**
     * Maps an AiDecision.ActionType to a PlayerActionCategory.
     *
     * @param actionType the AI decision action type
     * @return the corresponding player action category
     */
    public PlayerActionCategory mapActionType(AiDecision.ActionType actionType) {
        if (actionType == null) {
            return PlayerActionCategory.PASSIVE;
        }
        return switch (actionType) {
            case ENGAGE_COMBAT -> PlayerActionCategory.COMBAT;
            case RETREAT -> PlayerActionCategory.RETREAT;
            case MOVE_FLEET, SCOUT -> PlayerActionCategory.EXPANSION;
            case BUILD_SHIP -> PlayerActionCategory.BUILD_SHIP;
            case BUILD_STATION -> PlayerActionCategory.BUILD_STATION;
            case UPGRADE_STATION -> PlayerActionCategory.RESEARCH;
            case FORTIFY -> PlayerActionCategory.DEFENSIVE;
            case WAIT -> PlayerActionCategory.PASSIVE;
        };
    }

    /**
     * Records an action from an AiDecision.ActionType.
     *
     * @param playerId the player's ID
     * @param actionType the action type
     * @param turnNumber the current turn number
     */
    public void recordActionFromDecision(int playerId, AiDecision.ActionType actionType, int turnNumber) {
        PlayerActionCategory category = mapActionType(actionType);
        double intensity = computeDefaultIntensity(actionType);
        recordAction(playerId, category, intensity, turnNumber);
    }

    /**
     * Computes a default intensity for an action type.
     */
    private double computeDefaultIntensity(AiDecision.ActionType actionType) {
        return switch (actionType) {
            case ENGAGE_COMBAT -> 0.8;
            case RETREAT -> 0.6;
            case MOVE_FLEET -> 0.5;
            case SCOUT -> 0.7;
            case BUILD_SHIP -> 0.6;
            case BUILD_STATION -> 0.5;
            case UPGRADE_STATION -> 0.7;
            case FORTIFY -> 0.4;
            case WAIT -> 0.2;
        };
    }

    /**
     * Analyzes the combat behavior of a player.
     *
     * @param playerId the player's ID
     * @return the aggression score (0.0-1.0)
     */
    public double analyzeCombatBehavior(int playerId) {
        Map<PlayerActionCategory, Double> scores = playerTraitScores.get(playerId);
        if (scores == null) {
            return 0.5;
        }
        return scores.getOrDefault(PlayerActionCategory.COMBAT, 0.5);
    }

    /**
     * Analyzes the expansion behavior of a player.
     *
     * @param playerId the player's ID
     * @return the expansion drive score (0.0-1.0)
     */
    public double analyzeExpansionBehavior(int playerId) {
        Map<PlayerActionCategory, Double> scores = playerTraitScores.get(playerId);
        if (scores == null) {
            return 0.5;
        }
        return scores.getOrDefault(PlayerActionCategory.EXPANSION, 0.5);
    }

    /**
     * Analyzes the economic behavior of a player.
     *
     * @param playerId the player's ID
     * @return the economic focus score (0.0-1.0)
     */
    public double analyzeEconomicBehavior(int playerId) {
        Map<PlayerActionCategory, Double> scores = playerTraitScores.get(playerId);
        if (scores == null) {
            return 0.5;
        }
        double buildShip = scores.getOrDefault(PlayerActionCategory.BUILD_SHIP, 0.5);
        double buildStation = scores.getOrDefault(PlayerActionCategory.BUILD_STATION, 0.5);
        return (buildShip + buildStation) / 2.0;
    }

    /**
     * Analyzes the research behavior of a player.
     *
     * @param playerId the player's ID
     * @return the scientific focus score (0.0-1.0)
     */
    public double analyzeResearchBehavior(int playerId) {
        Map<PlayerActionCategory, Double> scores = playerTraitScores.get(playerId);
        if (scores == null) {
            return 0.5;
        }
        return scores.getOrDefault(PlayerActionCategory.RESEARCH, 0.5);
    }

    /**
     * Analyzes the diplomatic behavior of a player.
     *
     * @param playerId the player's ID
     * @return the diplomatic stance score (0.0-1.0)
     */
    public double analyzeDiplomaticBehavior(int playerId) {
        Map<PlayerActionCategory, Double> scores = playerTraitScores.get(playerId);
        if (scores == null) {
            return 0.5;
        }
        double defensive = scores.getOrDefault(PlayerActionCategory.DEFENSIVE, 0.5);
        double altruistic = scores.getOrDefault(PlayerActionCategory.ALTRUISTIC, 0.5);
        return (defensive + altruistic) / 2.0;
    }

    /**
     * Analyzes the risk behavior of a player.
     *
     * @param playerId the player's ID
     * @return the risk tolerance score (0.0-1.0)
     */
    public double analyzeRiskBehavior(int playerId) {
        Map<PlayerActionCategory, Double> scores = playerTraitScores.get(playerId);
        if (scores == null) {
            return 0.5;
        }
        double combat = scores.getOrDefault(PlayerActionCategory.COMBAT, 0.5);
        double retreat = scores.getOrDefault(PlayerActionCategory.RETREAT, 0.5);
        double passive = scores.getOrDefault(PlayerActionCategory.PASSIVE, 0.5);
        // High combat + low retreat + low passive = high risk tolerance
        return (combat + (1.0 - retreat) + (1.0 - passive)) / 3.0;
    }

    /**
     * Gets the total number of recorded actions for a player.
     *
     * @param playerId the player's ID
     * @return the total action count
     */
    public int getTotalActionCount(int playerId) {
        List<PlayerActionRecord> records = playerActions.get(playerId);
        return records != null ? records.size() : 0;
    }

    /**
     * Gets the action records for a player.
     *
     * @param playerId the player's ID
     * @return the list of action records
     */
    public List<PlayerActionRecord> getActionRecords(int playerId) {
        List<PlayerActionRecord> records = playerActions.get(playerId);
        return records != null ? new ArrayList<>(records) : Collections.emptyList();
    }

    /**
     * Gets the action counts per category for a player.
     *
     * @param playerId the player's ID
     * @return the action counts map
     */
    public Map<PlayerActionCategory, Integer> getActionCounts(int playerId) {
        Map<PlayerActionCategory, Integer> counts = playerActionCounts.get(playerId);
        return counts != null ? new HashMap<>(counts) : new HashMap<>();
    }

    /**
     * Gets the trait scores for a player.
     *
     * @param playerId the player's ID
     * @return the trait scores map
     */
    public Map<PlayerActionCategory, Double> getTraitScores(int playerId) {
        Map<PlayerActionCategory, Double> scores = playerTraitScores.get(playerId);
        return scores != null ? new HashMap<>(scores) : new HashMap<>();
    }

    /**
     * Clears all data for a player (e.g., when a session ends).
     *
     * @param playerId the player's ID
     */
    public void clearPlayerData(int playerId) {
        playerActions.remove(playerId);
        playerTraitScores.remove(playerId);
        playerActionCounts.remove(playerId);
        log.debug("Cleared action data for player {}", playerId);
    }

    /**
     * Computes the confidence score based on the number of observed actions.
     * <p>
     * Confidence increases with more actions:
     * <ul>
     *   <li>0-10 actions: 0.1 confidence</li>
     *   <li>10-50 actions: 0.4 confidence</li>
     *   <li>50-200 actions: 0.7 confidence</li>
     *   <li>200+ actions: 0.95 confidence</li>
     * </ul>
     * </p>
     *
     * @param playerId the player's ID
     * @return the confidence score (0.0-1.0)
     */
    public double getConfidenceScore(int playerId) {
        int count = getTotalActionCount(playerId);
        if (count <= 10) {
            return Math.min(0.4, 0.1 + (count / 10.0) * 0.3);
        } else if (count <= 50) {
            return 0.4 + ((count - 10) / 40.0) * 0.3;
        } else if (count <= 200) {
            return 0.7 + ((count - 50) / 150.0) * 0.25;
        } else {
            return 0.95;
        }
    }
}
