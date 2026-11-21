package com.yisy.modules.doramcu.gamecreator;
import com.yisy.modules.doramcu.model.players.Player;
import org.springframework.stereotype.Service;

import com.yisy.modules.doramcu.model.galaxy.Galaxy;
import com.yisy.modules.doramcu.repository.galaxy.GalaxyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service that provides a unified dashboard view of all games (galaxies).
 * <p>
 * When a player logs in, this service presents the list of existing games/galaxies
 * along with their lore information, game modes, and character counts.
 * Each galaxy is its own game, and the dashboard shows which lore each game is based on.
 * </p>
 */
@Service
public class GameDashboardService {

    private static final Logger log = LoggerFactory.getLogger(GameDashboardService.class);

    private final GalaxyRepository galaxyRepository;
    private final GameLoreService gameLoreService;

    public GameDashboardService(GalaxyRepository galaxyRepository, GameLoreService gameLoreService) {
        this.galaxyRepository = galaxyRepository;
        this.gameLoreService = gameLoreService;
    }

    /**
     * Get all games (galaxies) with their lore and game mode information.
     * <p>
     * This is the primary endpoint for the player's game selection screen.
     * Each entry shows the galaxy name, lore name, game modes, and creation date.
     * </p>
     *
     * @return list of game dashboard entries
     */
    public List<Map<String, Object>> getAllGames() {
        List<Galaxy> galaxies = galaxyRepository.findAll();
        log.debug("Returning {} games for dashboard", galaxies.size());

        return galaxies.stream()
                .map(this::toDashboardEntry)
                .collect(Collectors.toList());
    }

    /**
     * Get games filtered by lore slug.
     *
     * @param loreSlug the lore slug to filter by
     * @return list of games based on the specified lore
     */
    public List<Map<String, Object>> getGamesByLore(String loreSlug) {
        List<Galaxy> galaxies = galaxyRepository.findByLoreSlug(loreSlug);
        log.debug("Returning {} games for lore '{}'", galaxies.size(), loreSlug);

        return galaxies.stream()
                .map(this::toDashboardEntry)
                .collect(Collectors.toList());
    }

    /**
     * Get a single game's dashboard entry by galaxy name.
     *
     * @param galaxyName the galaxy name
     * @return the game dashboard entry or empty
     */
    public Optional<Map<String, Object>> getGameByName(String galaxyName) {
        return galaxyRepository.findByName(galaxyName)
                .map(this::toDashboardEntry);
    }

    /**
     * Get dashboard statistics.
     *
     * @return statistics about games and lores
     */
    public Map<String, Object> getDashboardStats() {
        List<Galaxy> galaxies = galaxyRepository.findAll();

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalGames", galaxies.size());

        // Count games by lore
        Map<String, Long> gamesByLore = galaxies.stream()
                .filter(g -> g.getLoreSlug() != null)
                .collect(Collectors.groupingBy(Galaxy::getLoreSlug, Collectors.counting()));
        stats.put("gamesByLore", gamesByLore);

        // Count games by mode
        Map<String, Long> gamesByMode = new HashMap<>();
        for (Galaxy g : galaxies) {
            if (g.getGameModes() != null && !g.getGameModes().isBlank()) {
                for (String mode : g.getGameModes().split(",")) {
                    String trimmed = mode.trim();
                    gamesByMode.merge(trimmed, 1L, Long::sum);
                }
            }
        }
        stats.put("gamesByMode", gamesByMode);

        // Available lores
        stats.put("availableLores", gameLoreService.countActiveLores());

        return stats;
    }

    /**
     * Convert a Galaxy entity to a dashboard entry map.
     */
    private Map<String, Object> toDashboardEntry(Galaxy galaxy) {
        Map<String, Object> entry = new HashMap<>();
        entry.put("id", galaxy.getId());
        entry.put("name", galaxy.getName());
        entry.put("displayName", galaxy.getDisplayName());
        entry.put("loreSlug", galaxy.getLoreSlug());
        entry.put("gameModes", galaxy.getGameModes());
        entry.put("gameType", galaxy.getGameType());
        entry.put("difficultyLevel", galaxy.getDifficultyLevel());
        entry.put("theme", galaxy.getTheme());
        entry.put("description", galaxy.getDescription());
        entry.put("createdAt", galaxy.getCreatedAt());
        entry.put("totalSectors", galaxy.getTotalSectors());
        entry.put("solarSystems", galaxy.getSolarSystems().size());

        // Enrich with lore details if available
        if (galaxy.getLoreSlug() != null) {
            gameLoreService.getLoreBySlug(galaxy.getLoreSlug()).ifPresent(lore -> {
                Map<String, Object> loreInfo = new HashMap<>();
                loreInfo.put("name", lore.getName());
                loreInfo.put("shortDescription", lore.getShortDescription());
                loreInfo.put("genre", lore.getGenre());
                loreInfo.put("category", lore.getCategory());
                loreInfo.put("technologyLevel", lore.getTechnologyLevel());
                loreInfo.put("tone", lore.getTone());
                entry.put("lore", loreInfo);
            });
        }

        // Parse game modes into a list
        if (galaxy.getGameModes() != null && !galaxy.getGameModes().isBlank()) {
            List<String> modeList = Arrays.stream(galaxy.getGameModes().split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList());
            entry.put("gameModesList", modeList);
        }

        return entry;
    }
}
