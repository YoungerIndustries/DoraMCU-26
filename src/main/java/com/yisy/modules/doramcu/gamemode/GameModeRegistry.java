package com.yisy.modules.doramcu.gamemode;
import com.yisy.modules.doramcu.model.players.Player;
import org.springframework.stereotype.Component;

import com.yisy.modules.doramcu.gamecore.GameMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Registry that manages all game mode modules and activates them
 * for galaxies based on selected game modes.
 * <p>
 * At startup, all {@link GameModeModule} beans are auto-registered.
 * When a galaxy is created with specific game modes, the registry
 * initializes the corresponding modules for that galaxy.
 * </p>
 * <p>
 * The registry also provides turn processing across all active modules,
 * enabling hybrid gameplay experiences (e.g., RPG + 4X + Tycoon).
 * </p>
 * <p>
 * After each turn, module state is persisted to the database via
 * {@link GameModeStateService} so progress survives server restarts.
 * </p>
 */
@Component
public class GameModeRegistry {

    private static final Logger log = LoggerFactory.getLogger(GameModeRegistry.class);

    private final List<GameModeModule> modules;
    private final GameModeStateService stateService;

    /** Galaxy name -> set of active game modes */
    private final Map<String, Set<GameMode>> galaxyModes = new HashMap<>();

    /** Game mode -> module mapping for quick lookup */
    private final Map<GameMode, GameModeModule> moduleMap = new HashMap<>();

    /**
     * Auto-register all GameModeModule beans at startup.
     *
     * @param modules     list of all GameModeModule beans found by Spring
     * @param stateService service for persisting module state to the database
     */
    public GameModeRegistry(List<GameModeModule> modules, GameModeStateService stateService) {
        this.modules = modules;
        this.stateService = stateService;
        for (GameModeModule module : modules) {
            moduleMap.put(module.getGameMode(), module);
        }
        log.info("GameModeRegistry initialized with {} modules: {}",
                modules.size(),
                modules.stream().map(m -> m.getModuleName()).collect(Collectors.joining(", ")));
    }

    /**
     * Returns all registered game mode modules.
     *
     * @return list of all modules
     */
    public List<GameModeModule> getAllModules() {
        return new ArrayList<>(modules);
    }

    /**
     * Returns the module for a specific game mode.
     *
     * @param gameMode the game mode enum
     * @return the module, or null if not found
     */
    public GameModeModule getModule(GameMode gameMode) {
        return moduleMap.get(gameMode);
    }

    /**
     * Initializes all selected game mode modules for a new galaxy.
     * Called by GameCreatorService after galaxy creation.
     *
     * @param galaxyName  the name of the galaxy
     * @param gameModes   the set of selected game modes
     * @param loreSlug    the lore slug for the galaxy
     */
    public void initializeModulesForGalaxy(String galaxyName, Set<GameMode> gameModes, String loreSlug) {
        log.info("Initializing game mode modules for galaxy: {} with modes: {}", galaxyName, gameModes);
        galaxyModes.put(galaxyName, new HashSet<>(gameModes));

        for (GameMode mode : gameModes) {
            GameModeModule module = moduleMap.get(mode);
            if (module != null) {
                try {
                    module.initializeForGalaxy(galaxyName, loreSlug);
                    log.info("  Ã¢Å“â€œ Initialized {} for galaxy: {}", module.getModuleName(), galaxyName);
                } catch (Exception e) {
                    log.error("  Ã¢Å“â€” Failed to initialize {} for galaxy: {}: {}",
                            module.getModuleName(), galaxyName, e.getMessage(), e);
                }
            } else {
                log.warn("  ! No module found for game mode: {}", mode);
            }
        }
    }

    /**
     * Processes a game turn across all active modules for a galaxy.
     * Each active module advances its own game state.
     *
     * @param galaxyName the name of the galaxy
     * @param turnNumber the current turn number
     * @param playerId   the player ID initiating the turn
     * @return combined summary from all active modules
     */
    public String processTurnForGalaxy(String galaxyName, int turnNumber, String playerId) {
        Set<GameMode> modes = galaxyModes.get(galaxyName);
        if (modes == null || modes.isEmpty()) {
            return "No game modes active for galaxy: " + galaxyName;
        }

        StringBuilder summary = new StringBuilder();
        summary.append("=== Turn ").append(turnNumber).append(" in ").append(galaxyName).append(" ===\n\n");

        for (GameMode mode : modes) {
            GameModeModule module = moduleMap.get(mode);
            if (module != null && module.isActiveForGalaxy(galaxyName)) {
                try {
                    String result = module.processTurn(galaxyName, turnNumber, playerId);
                    summary.append(result).append("\n\n");
                } catch (Exception e) {
                    log.error("Error processing turn for {} in galaxy {}: {}",
                            module.getModuleName(), galaxyName, e.getMessage(), e);
                    summary.append(module.getModuleName()).append(": Error - ").append(e.getMessage()).append("\n\n");
                }
            }
        }

        // Persist module state to database after turn processing
        saveAllModuleStates(galaxyName, modes);

        return summary.toString();
    }

    /**
     * Saves the serialized state of all active modules for a galaxy to the database.
     * Called after each turn to ensure progress is not lost on server restart.
     *
     * @param galaxyName the name of the galaxy
     * @param modes      the set of active game modes
     */
    public void saveAllModuleStates(String galaxyName, Set<GameMode> modes) {
        if (modes == null || modes.isEmpty()) return;
        for (GameMode mode : modes) {
            GameModeModule module = moduleMap.get(mode);
            if (module != null && module.isActiveForGalaxy(galaxyName)) {
                try {
                    String stateJson = module.serializeState(galaxyName);
                    if (stateJson != null) {
                        stateService.saveState(galaxyName, mode.name(), stateJson);
                    }
                } catch (Exception e) {
                    log.warn("Failed to save state for {} in galaxy {}: {}",
                            module.getModuleName(), galaxyName, e.getMessage());
                }
            }
        }
    }

    /**
     * Returns the combined state summary for all active modules in a galaxy.
     *
     * @param galaxyName the name of the galaxy
     * @return combined state summary
     */
    public String getGalaxyStateSummary(String galaxyName) {
        Set<GameMode> modes = galaxyModes.get(galaxyName);
        if (modes == null || modes.isEmpty()) {
            return "No game modes active for galaxy: " + galaxyName;
        }

        return modes.stream()
                .map(mode -> moduleMap.get(mode))
                .filter(Objects::nonNull)
                .filter(m -> m.isActiveForGalaxy(galaxyName))
                .map(m -> m.getStateSummary(galaxyName))
                .collect(Collectors.joining(" | "));
    }

    /**
     * Returns the active game modes for a galaxy.
     *
     * @param galaxyName the name of the galaxy
     * @return set of active game modes
     */
    public Set<GameMode> getActiveModesForGalaxy(String galaxyName) {
        return galaxyModes.getOrDefault(galaxyName, Collections.emptySet());
    }

    /**
     * Checks if a specific game mode is active for a galaxy.
     *
     * @param galaxyName the name of the galaxy
     * @param gameMode   the game mode to check
     * @return true if the mode is active for the galaxy
     */
    public boolean isModeActiveForGalaxy(String galaxyName, GameMode gameMode) {
        Set<GameMode> modes = galaxyModes.get(galaxyName);
        return modes != null && modes.contains(gameMode);
    }

    /**
     * Returns a summary of all available game modes and their modules.
     * Used for API responses and dashboard displays.
     *
     * @return list of mode summaries
     */
    public List<Map<String, Object>> getModeSummaries() {
        List<Map<String, Object>> summaries = new ArrayList<>();
        for (GameModeModule module : modules) {
            Map<String, Object> summary = new HashMap<>();
            summary.put("mode", module.getGameMode().name());
            summary.put("displayName", module.getGameMode().getDisplayName());
            summary.put("moduleName", module.getModuleName());
            summary.put("description", module.getDescription());
            summaries.add(summary);
        }
        return summaries;
    }
}
