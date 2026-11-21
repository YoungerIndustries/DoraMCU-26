package com.yisy.modules.doramcu.ai.framework;
import com.yisy.modules.doramcu.ai.AiPersonality;
import com.yisy.modules.doramcu.ai.DynamicAiPersonality;
import com.yisy.modules.doramcu.ai.PlayerActionAnalyzer;
import com.yisy.modules.doramcu.hex.Player;

import java.util.Map;
import java.util.Optional;

/**
 * Unified personality manager that consolidates personality creation,
 * storage, and retrieval for all AI types.
 * <p>
 * This service replaces the scattered personality management code across
 * multiple services with a single point of truth. It handles:
 * <ul>
 *   <li>Creating static personalities from government/ethos</li>
 *   <li>Creating dynamic personalities from observed behavior</li>
 *   <li>Blending base and observed traits</li>
 *   <li>Personality evolution tracking</li>
 *   <li>Caching personality calculations</li>
 * </ul>
 * </p>
 * <p>
 * Usage:
 * <pre>
 * // Get or create personality for a player
 * PersonalityProfile profile = personalityManager.getOrCreatePersonality(player);
 *
 * // Update personality from observed actions
 * personalityManager.updateFromActions(playerId, actionType);
 *
 * // Get blended personality for AI decisions
 * PersonalityProfile blended = personalityManager.getBlendedPersonality(playerId);
 * </pre>
 * </p>
 */
public interface AiPersonalityManager {

    /**
     * Gets or creates a personality profile for the given player.
     * <p>
     * If a dynamic personality exists, returns it. Otherwise, creates
     * a static personality from the player's government and ethos.
     * </p>
     *
     * @param player the player
     * @return the personality profile
     */
    PersonalityProfile getOrCreatePersonality(Player player);

    /**
     * Gets the personality profile for a player by ID.
     *
     * @param playerId the player's ID
     * @return the personality profile if it exists
     */
    Optional<PersonalityProfile> getPersonality(int playerId);

    /**
     * Creates a static personality from a player's government and ethos.
     *
     * @param player the player
     * @return the static personality
     */
    AiPersonality createStaticPersonality(Player player);

    /**
     * Creates a dynamic personality that blends observed behavior with the base personality.
     *
     * @param player the player
     * @return the dynamic personality
     */
    DynamicAiPersonality createDynamicPersonality(Player player);

    /**
     * Updates a player's personality from an observed action.
     *
     * @param playerId the player's ID
     * @param actionType the action type performed
     * @param turnNumber the current turn number
     */
    void updateFromAction(int playerId, String actionType, int turnNumber);

    /**
     * Updates a player's personality from the PlayerActionAnalyzer.
     *
     * @param playerId the player's ID
     * @param analyzer the action analyzer with observed data
     */
    void updateFromAnalyzer(int playerId, PlayerActionAnalyzer analyzer);

    /**
     * Gets the blended personality for AI decisions.
     * <p>
     * Returns the dynamic personality if available, otherwise the static one.
     * </p>
     *
     * @param playerId the player's ID
     * @return the blended personality profile
     */
    PersonalityProfile getBlendedPersonality(int playerId);

    /**
     * Gets the confidence score for a player's personality.
     *
     * @param playerId the player's ID
     * @return the confidence score (0.0-1.0)
     */
    double getConfidenceScore(int playerId);

    /**
     * Gets all personality profiles for a session.
     *
     * @param sessionId the session ID
     * @return map of player ID to personality profile
     */
    Map<Integer, PersonalityProfile> getAllPersonalities(String sessionId);

    /**
     * Clears cached personality data for a player.
     *
     * @param playerId the player's ID
     */
    void clearCache(int playerId);

    /**
     * Clears all cached personality data.
     */
    void clearAllCaches();

    /**
     * Returns the number of cached personalities.
     *
     * @return the cache size
     */
    int getCacheSize();
}
