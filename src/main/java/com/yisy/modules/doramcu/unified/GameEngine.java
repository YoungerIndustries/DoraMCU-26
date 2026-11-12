package com.yisy.modules.doramcu.unified;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Interface for game engines that can be managed by shared infrastructure.
 * <p>
 * This interface extracts the common operations needed by shared services
 * like {@code TurnScheduler} and {@code GameSession}, without depending on any
 * game-specific types. Each game system (4X, Traveller, Captain's Log, etc.)
 * can implement this interface to integrate with the shared session management layer.
 * </p>
 * <p>
 * Game-specific operations (fleet movement, combat, resource management, etc.)
 * remain in the concrete game engine classes and their dedicated services.
 * </p>
 */
public interface GameEngine {

    /**
     * Returns the name of this game instance.
     *
     * @return the game name
     */
    String getName();

    /**
     * Returns the list of players in this game.
     * <p>
     * The player type is generic to accommodate different game systems:
     * the 4X game uses {@code hex.Player}, Traveller uses {@code TravellerCharacter},
     * and Captain's Log uses its own character model.
     * </p>
     *
     * @return the list of players
     */
    List<?> getPlayers();

    /**
     * Advances the game by one turn.
     * <p>
     * Called by {@code TurnScheduler} to auto-advance turns in active sessions.
     * </p>
     */
    void nextTurn();

    /**
     * Returns the current turn number.
     *
     * @return the current turn number
     */
    int getCurrentTurn();

    // ==================== Extended Operations ====================

    /**
     * Returns the current game state as a map of key-value pairs.
     * <p>
     * This provides a snapshot of the game's current state for serialization,
     * debugging, or cross-engine coordination.
     * </p>
     *
     * @return the game state
     */
    default Map<String, Object> getGameState() {
        return Map.of(
                "name", getName(),
                "currentTurn", getCurrentTurn(),
                "playerCount", getPlayers().size()
        );
    }

    /**
     * Processes a game action and returns the result.
     * <p>
     * This is the unified entry point for player actions. The action is a map
     * containing the action type and parameters. The result is a map containing
     * the outcome and any side effects.
     * </p>
     *
     * @param action the action to process
     * @return the action result
     */
    default Map<String, Object> processAction(Map<String, Object> action) {
        throw new UnsupportedOperationException("processAction not implemented by " + getName());
    }

    /**
     * Returns the set of available actions for the current game state.
     * <p>
     * Each action is described by a key (action type) and a human-readable label.
     * </p>
     *
     * @return the available actions
     */
    default Set<String> getAvailableActions() {
        return Set.of();
    }

    /**
     * Returns whether this game engine uses turn-based gameplay.
     *
     * @return true if turn-based
     */
    default boolean isTurnBased() {
        return true;
    }

    /**
     * Returns the duration of a single turn in milliseconds.
     * <p>
     * Used by {@code UnifiedTurnScheduler} to coordinate turns across engines.
     * </p>
     *
     * @return the turn duration in milliseconds
     */
    default long getTurnDuration() {
        return 60_000; // default 60 seconds
    }

    /**
     * Returns the game type this engine implements.
     *
     * @return the game type
     */
    GameType getGameType();

    /**
     * Returns whether the game is currently active.
     *
     * @return true if active
     */
    default boolean isActive() {
        return true;
    }

    /**
     * Starts the game.
     */
    default void start() {
        // default no-op
    }

    /**
     * Pauses the game.
     */
    default void pause() {
        // default no-op
    }

    /**
     * Ends the game.
     */
    default void end() {
        // default no-op
    }
}
