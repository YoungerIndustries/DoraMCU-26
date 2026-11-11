package com.yisy.modules.doramcu.ai;

import com.yisy.modules.doramcu.babylon5.model.B5Personality;
import com.yisy.modules.doramcu.hex.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service that builds AI clones of human players based on their observed behavior.
 * <p>
 * The clone is an EmpireAiCore configured with a DynamicAiPersonality that blends
 * the player's government/ethos-based traits with their observed behavioral patterns.
 * The clone can be used for:
 * <ul>
 *   <li>AI opponents that mimic the player's style</li>
 *   <li>Predictive modeling of player behavior</li>
 *   <li>Personalized difficulty scaling</li>
 *   <li>Post-game analysis and replay</li>
 * </ul>
 * </p>
 */
@Service
public class PlayerCloneService {

    private static final Logger log = LoggerFactory.getLogger(PlayerCloneService.class);

    private final PlayerActionAnalyzer actionAnalyzer;

    /** Cache of dynamic personalities per player. */
    private final Map<Integer, DynamicAiPersonality> personalityCache = new ConcurrentHashMap<>();

    public PlayerCloneService(PlayerActionAnalyzer actionAnalyzer) {
        this.actionAnalyzer = actionAnalyzer;
    }

    /**
     * Builds a DynamicAiPersonality for a player based on their observed actions.
     * <p>
     * If no actions have been observed, returns a personality based solely on
     * the player's government type and ethos.
     * </p>
     *
     * @param player the player to build a personality for
     * @return the dynamic personality profile
     */
    public DynamicAiPersonality buildPersonality(Player player) {
        if (player == null) {
            log.warn("Cannot build personality for null player");
            return null;
        }

        int playerId = player.getId();
        DynamicAiPersonality personality = new DynamicAiPersonality(player);
        personality.updateFromAnalyzer(actionAnalyzer, playerId);

        // Cache the personality
        personalityCache.put(playerId, personality);

        log.info("Built dynamic personality for player {}: {}", playerId, personality);
        return personality;
    }

    /**
     * Gets the cached dynamic personality for a player, or builds a new one.
     *
     * @param player the player
     * @return the dynamic personality profile
     */
    public DynamicAiPersonality getOrCreatePersonality(Player player) {
        if (player == null) {
            return null;
        }

        int playerId = player.getId();
        DynamicAiPersonality cached = personalityCache.get(playerId);
        if (cached != null) {
            // Refresh with latest observed data
            cached.updateFromAnalyzer(actionAnalyzer, playerId);
            return cached;
        }
        return buildPersonality(player);
    }

    /**
     * Creates an EmpireAiCore clone of a human player.
     * <p>
     * The clone uses the player's observed personality to make decisions
     * that mimic the player's behavioral patterns.
     * </p>
     *
     * @param playerId the session player ID (used as the AI core ID)
     * @param player the player to clone
     * @param researchLevel the AI research level (0-10)
     * @return the EmpireAiCore configured with the player's observed personality
     */
    public EmpireAiCore createClone(int playerId, Player player, int researchLevel) {
        if (player == null) {
            log.warn("Cannot create clone for null player");
            return null;
        }

        DynamicAiPersonality personality = buildPersonality(player);
        EmpireAiCore clone = new EmpireAiCore(playerId, player, researchLevel);
        clone.setDynamicPersonality(personality);

        log.info("Created AI clone for player {} with research level {}", playerId, researchLevel);
        return clone;
    }

    /**
     * Creates a B5Personality profile for a player.
     * <p>
     * Maps the 6 core personality axes plus 6 additional behavioral axes
     * to the 12-axis B5Personality model.
     * </p>
     *
     * @param player the player
     * @return the B5Personality profile
     */
    public B5Personality createB5Personality(Player player) {
        DynamicAiPersonality dynamic = getOrCreatePersonality(player);
        if (dynamic == null) {
            return null;
        }
        return dynamic.toB5Personality();
    }

    /**
     * Refreshes the cached personality for a player with the latest observed data.
     *
     * @param playerId the player's ID
     */
    public void refreshPersonality(int playerId) {
        DynamicAiPersonality personality = personalityCache.get(playerId);
        if (personality != null) {
            personality.updateFromAnalyzer(actionAnalyzer, playerId);
            log.debug("Refreshed personality for player {}: {}", playerId, personality);
        }
    }

    /**
     * Gets the confidence score for a player's personality profile.
     * <p>
     * Higher confidence means the clone is more accurate.
     * </p>
     *
     * @param playerId the player's ID
     * @return the confidence score (0.0-1.0)
     */
    public double getConfidenceScore(int playerId) {
        return actionAnalyzer.getConfidenceScore(playerId);
    }

    /**
     * Gets the number of observed actions for a player.
     *
     * @param playerId the player's ID
     * @return the action count
     */
    public int getObservedActionCount(int playerId) {
        return actionAnalyzer.getTotalActionCount(playerId);
    }

    /**
     * Clears the cached personality for a player.
     *
     * @param playerId the player's ID
     */
    public void clearCache(int playerId) {
        personalityCache.remove(playerId);
    }

    /**
     * Clears all cached personalities.
     */
    public void clearAllCaches() {
        personalityCache.clear();
    }
}
