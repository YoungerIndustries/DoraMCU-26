package com.yisy.modules.doramcu.gamecreator;
import com.yisy.modules.doramcu.model.players.Player;

import com.yisy.modules.doramcu.gamecore.GameMode;

/**
 * Interface for game mode modules that provide specialized gameplay mechanics.
 * Each module handles a specific game mode (RPG, FPS, Survival, etc.) and
 * provides turn-based processing for galaxies using that mode.
 */
public interface GameModeModule {

    /**
     * Get the game mode this module handles.
     */
    GameMode getGameMode();

    /**
     * Get the module name.
     */
    String getModuleName();

    /**
     * Get the module description.
     */
    String getDescription();

    /**
     * Initialize the module for a galaxy.
     */
    void initializeForGalaxy(String galaxyName, String loreSlug);

    /**
     * Process a turn for a galaxy.
     */
    String processTurn(String galaxyName, int turnNumber, String playerId);

    /**
     * Get a summary of the current state for a galaxy.
     */
    String getStateSummary(String galaxyName);

    /**
     * Check if the module is active for a galaxy.
     */
    boolean isActiveForGalaxy(String galaxyName);

    /**
     * Serialize the current state for a galaxy.
     */
    String serializeState(String galaxyName);

    /**
     * Restore state from JSON for a galaxy.
     */
    void restoreState(String galaxyName, String stateJson);
}
