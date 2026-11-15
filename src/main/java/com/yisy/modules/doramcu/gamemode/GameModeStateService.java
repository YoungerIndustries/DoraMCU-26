package com.yisy.modules.doramcu.gamemode;
import org.springframework.stereotype.Service;

import com.yisy.modules.doramcu.model.GameModeState;
import com.yisy.modules.doramcu.repository.GameModeStateRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service for persisting and loading game mode module state to/from the database.
 * <p>
 * Each game mode module's state is serialized to JSON and stored in the
 * {@code game_mode_state} table, keyed by galaxy name and game mode.
 * This allows module state to survive server restarts.
 * </p>
 */
@Service
public class GameModeStateService {

    private static final Logger log = LoggerFactory.getLogger(GameModeStateService.class);

    private final GameModeStateRepository repository;

    public GameModeStateService(GameModeStateRepository repository) {
        this.repository = repository;
    }

    /**
     * Saves or updates the state for a specific game mode in a galaxy.
     *
     * @param galaxyName the galaxy name
     * @param gameMode   the game mode name (e.g., "RPG", "FOUR_X")
     * @param stateJson  JSON-serialized state string
     */
    @Transactional
    public void saveState(String galaxyName, String gameMode, String stateJson) {
        Optional<GameModeState> existing = repository.findByGalaxyNameAndGameMode(galaxyName, gameMode);
        GameModeState entity;
        if (existing.isPresent()) {
            entity = existing.get();
            entity.setStateJson(stateJson);
        } else {
            entity = new GameModeState();
            entity.setGalaxyName(galaxyName);
            entity.setGameMode(gameMode);
            entity.setStateJson(stateJson);
        }
        repository.save(entity);
        log.debug("Saved state for galaxy={}, mode={}", galaxyName, gameMode);
    }

    /**
     * Loads the saved state for a specific game mode in a galaxy.
     *
     * @param galaxyName the galaxy name
     * @param gameMode   the game mode name
     * @return the JSON state string, or null if not found
     */
    @Transactional(readOnly = true)
    public String loadState(String galaxyName, String gameMode) {
        return repository.findByGalaxyNameAndGameMode(galaxyName, gameMode)
                .map(GameModeState::getStateJson)
                .orElse(null);
    }

    /**
     * Loads all saved states for a galaxy.
     *
     * @param galaxyName the galaxy name
     * @return list of all state entries for the galaxy
     */
    @Transactional(readOnly = true)
    public List<GameModeState> loadAllStates(String galaxyName) {
        return repository.findByGalaxyName(galaxyName);
    }

    /**
     * Deletes the saved state for a specific game mode in a galaxy.
     *
     * @param galaxyName the galaxy name
     * @param gameMode   the game mode name
     */
    @Transactional
    public void deleteState(String galaxyName, String gameMode) {
        repository.deleteByGalaxyNameAndGameMode(galaxyName, gameMode);
        log.debug("Deleted state for galaxy={}, mode={}", galaxyName, gameMode);
    }
}
