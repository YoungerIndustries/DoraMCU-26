package com.yisy.modules.doramcu.gamemode;
import com.yisy.modules.doramcu.model.players.Player;

import com.yisy.modules.doramcu.gamecore.GameMode;
import com.yisy.modules.doramcu.model.session.GameModeState;
import com.yisy.modules.doramcu.model.galaxy.Galaxy;
import com.yisy.modules.doramcu.repository.galaxy.GalaxyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Re-initializes game mode modules for all existing galaxies on server startup.
 * <p>
 * Restores module-to-galaxy mappings and loads persisted module state from the
 * database so that player progress survives server restarts.
 * </p>
 */
@Component
public class GameModeStartupInitializer {

    private static final Logger log = LoggerFactory.getLogger(GameModeStartupInitializer.class);

    private final GalaxyRepository galaxyRepository;
    private final GameModeRegistry gameModeRegistry;
    private final GameModeStateService stateService;

    public GameModeStartupInitializer(GalaxyRepository galaxyRepository,
                                      GameModeRegistry gameModeRegistry,
                                      GameModeStateService stateService) {
        this.galaxyRepository = galaxyRepository;
        this.gameModeRegistry = gameModeRegistry;
        this.stateService = stateService;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void reinitializeGameModeModules() {
        log.info("Starting game mode module re-initialization for existing galaxies...");

        List<Galaxy> galaxies = galaxyRepository.findAll();
        int initializedCount = 0;
        int skippedCount = 0;
        int statesRestored = 0;

        for (Galaxy galaxy : galaxies) {
            String galaxyName = galaxy.getName();
            String gameModesStr = galaxy.getGameModes();
            String loreSlug = galaxy.getLoreSlug();

            if (gameModesStr == null || gameModesStr.trim().isEmpty()) {
                skippedCount++;
                continue;
            }

            try {
                Set<GameMode> gameModes = parseGameModes(gameModesStr);
                if (gameModes.isEmpty()) {
                    skippedCount++;
                    continue;
                }

                log.info("Re-initializing {} game modes for galaxy: {} (lore: {})",
                        gameModes.size(), galaxyName, loreSlug != null ? loreSlug : "none");
                gameModeRegistry.initializeModulesForGalaxy(galaxyName, gameModes, loreSlug);

                // Restore saved module state from database
                int restored = restoreModuleStates(galaxyName, gameModes);
                statesRestored += restored;

                initializedCount++;
            } catch (Exception e) {
                log.error("Failed to re-initialize game modes for galaxy {}: {}",
                        galaxyName, e.getMessage(), e);
            }
        }

        log.info("Game mode module re-initialization complete: {} galaxies initialized, {} skipped, {} states restored",
                initializedCount, skippedCount, statesRestored);
    }

    private Set<GameMode> parseGameModes(String gameModesStr) {
        Set<GameMode> modes = new HashSet<>();
        String[] parts = gameModesStr.split(",");
        for (String part : parts) {
            String trimmed = part.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            try {
                GameMode mode = GameMode.valueOf(trimmed.toUpperCase());
                modes.add(mode);
            } catch (IllegalArgumentException e) {
                log.warn("Unknown game mode '{}' in galaxy configuration", trimmed);
            }
        }
        return modes;
    }

    /**
     * Restores saved module state from the database for a galaxy.
     *
     * @param galaxyName the name of the galaxy
     * @param gameModes  the set of active game modes
     * @return number of states successfully restored
     */
    private int restoreModuleStates(String galaxyName, Set<GameMode> gameModes) {
        int restored = 0;
        List<GameModeState> savedStates = stateService.loadAllStates(galaxyName);

        for (GameModeState saved : savedStates) {
            String modeName = saved.getGameMode();
            String stateJson = saved.getStateJson();
            if (stateJson == null || stateJson.isEmpty()) {
                continue;
            }
            try {
                GameMode mode = GameMode.valueOf(modeName);
                if (gameModes.contains(mode)) {
                    GameModeModule module = gameModeRegistry.getModule(mode);
                    if (module != null && module.isActiveForGalaxy(galaxyName)) {
                        module.restoreState(galaxyName, stateJson);
                        log.info("  Restored {} state for galaxy: {}", modeName, galaxyName);
                        restored++;
                    }
                }
            } catch (Exception e) {
                log.warn("  Failed to restore {} state for galaxy {}: {}",
                        modeName, galaxyName, e.getMessage());
            }
        }
        return restored;
    }
}
