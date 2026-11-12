package com.yisy.modules.doramcu.unified;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Registry that maps {@link GameType} to concrete {@link GameEngine} implementations.
 * <p>
 * This service provides a centralized way to look up game engines by type,
 * enabling the unified system to delegate to the correct engine for each
 * game module.
 * </p>
 */
@Component
public class GameEngineRegistry {

    private static final Logger log = LoggerFactory.getLogger(GameEngineRegistry.class);

    private final Map<GameType, GameEngine> engines = new EnumMap<>(GameType.class);

    public GameEngineRegistry(List<GameEngine> gameEngines) {
        for (GameEngine engine : gameEngines) {
            register(engine);
        }
        log.info("Registered {} game engines: {}", engines.size(), engines.keySet());
    }

    /**
     * Registers a game engine for a specific game type.
     *
     * @param engine the game engine
     */
    public void register(GameEngine engine) {
        if (engine == null) return;
        GameType type = engine.getGameType();
        engines.put(type, engine);
        log.debug("Registered game engine for {}: {}", type, engine.getName());
    }

    /**
     * Unregisters a game engine for a specific game type.
     *
     * @param gameType the game type
     */
    public void unregister(GameType gameType) {
        engines.remove(gameType);
    }

    /**
     * Gets the game engine for a specific game type.
     *
     * @param gameType the game type
     * @return the game engine, or null if not registered
     */
    public GameEngine getEngine(GameType gameType) {
        return engines.get(gameType);
    }

    /**
     * Gets the game engine for a specific game type, throwing if not found.
     *
     * @param gameType the game type
     * @return the game engine
     * @throws IllegalArgumentException if no engine is registered for the type
     */
    public GameEngine getRequiredEngine(GameType gameType) {
        GameEngine engine = engines.get(gameType);
        if (engine == null) {
            throw new IllegalArgumentException("No game engine registered for: " + gameType);
        }
        return engine;
    }

    /**
     * Returns all registered game types.
     *
     * @return the set of registered game types
     */
    public Set<GameType> getRegisteredGameTypes() {
        return Collections.unmodifiableSet(engines.keySet());
    }

    /**
     * Returns all registered game engines.
     *
     * @return the list of game engines
     */
    public List<GameEngine> getAllEngines() {
        return new ArrayList<>(engines.values());
    }

    /**
     * Returns whether a game engine is registered for the given type.
     *
     * @param gameType the game type
     * @return true if registered
     */
    public boolean hasEngine(GameType gameType) {
        return engines.containsKey(gameType);
    }

    /**
     * Advances all active game engines by one turn.
     */
    public void advanceAllTurns() {
        for (GameEngine engine : engines.values()) {
            if (engine.isActive()) {
                engine.nextTurn();
            }
        }
    }

    /**
     * Returns a summary of all registered engines.
     *
     * @return the summary map
     */
    public Map<String, Object> getSummary() {
        Map<String, Object> summary = new HashMap<>();
        summary.put("registeredEngines", engines.size());
        summary.put("gameTypes", new ArrayList<>(engines.keySet()));
        Map<String, Integer> turnNumbers = new HashMap<>();
        for (Map.Entry<GameType, GameEngine> entry : engines.entrySet()) {
            turnNumbers.put(entry.getKey().name(), entry.getValue().getCurrentTurn());
        }
        summary.put("turnNumbers", turnNumbers);
        return summary;
    }
}
