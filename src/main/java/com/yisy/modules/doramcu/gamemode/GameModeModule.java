package com.yisy.modules.doramcu.gamemode;

import com.yisy.modules.doramcu.gamecore.GameMode;

/**
 * Interface for game mode-specific gameplay modules.
 * <p>
 * Each game mode (RPG, 4X, Tycoon, etc.) provides a module that implements
 * specialized gameplay mechanics. When a galaxy is created with selected game modes,
 * the corresponding modules are activated to provide the gameplay features.
 * </p>
 * <p>
 * Modules are stateless services that operate on galaxy data through repositories
 * and provide the core gameplay loops for their respective modes.
 * </p>
 */
public interface GameModeModule {

    /**
     * Returns the game mode this module implements.
     *
     * @return the game mode enum value
     */
    GameMode getGameMode();

    /**
     * Returns the display name of this module.
     *
     * @return human-readable module name
     */
    String getModuleName();

    /**
     * Returns a description of the gameplay features provided by this module.
     *
     * @return feature description
     */
    String getDescription();

    /**
     * Initializes the module for a specific galaxy.
     * Called when a galaxy is created with this game mode enabled.
     *
     * @param galaxyName the name of the galaxy
     * @param loreSlug   the lore slug for the galaxy
     */
    void initializeForGalaxy(String galaxyName, String loreSlug);

    /**
     * Processes a game turn for this module in the specified galaxy.
     * Each module advances its own game state (e.g., RPG quest progression,
     * 4X empire growth, Tycoon trade cycles).
     *
     * @param galaxyName the name of the galaxy
     * @param turnNumber the current turn number
     * @param playerId   the player ID initiating the turn
     * @return a summary of what happened during this turn
     */
    String processTurn(String galaxyName, int turnNumber, String playerId);

    /**
     * Returns the current state summary for this module in the specified galaxy.
     * Used for dashboard displays and API responses.
     *
     * @param galaxyName the name of the galaxy
     * @return state summary as a string
     */
    String getStateSummary(String galaxyName);

    /**
     * Checks if this module is active for the specified galaxy.
     *
     * @param galaxyName the name of the galaxy
     * @return true if the module is active for this galaxy
     */
    boolean isActiveForGalaxy(String galaxyName);

    /**
     * Serializes the current state of this module for the specified galaxy to JSON.
     * Used for persisting module state to the database so progress survives server restarts.
     *
     * @param galaxyName the name of the galaxy
     * @return JSON string representing the module state, or null if no state exists
     */
    default String serializeState(String galaxyName) {
        return null;
    }

    /**
     * Restores the module state from a previously serialized JSON string.
     * Called on server startup to restore in-memory state from the database.
     *
     * @param galaxyName the name of the galaxy
     * @param stateJson  JSON string representing the module state
     */
    default void restoreState(String galaxyName, String stateJson) {
        // Default no-op implementation; modules should override to restore state
    }
}
