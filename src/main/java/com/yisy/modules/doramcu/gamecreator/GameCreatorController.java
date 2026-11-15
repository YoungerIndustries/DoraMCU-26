package com.yisy.modules.doramcu.gamecreator;

import com.yisy.modules.doramcu.gamecore.GameMode;
import com.yisy.modules.doramcu.gamemode.GameModeModule;
import com.yisy.modules.doramcu.gamemode.GameModeRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.*;

/**
 * REST controller for the game creator service.
 * <p>
 * Provides endpoints for:
 * <ul>
 *   <li>List all available game lores</li>
 *   <li>Search lores by keyword</li>
 *   <li>Get lore details by slug</li>
 *   <li>List all available game modes</li>
 *   <li>Create a new game with selected lore and game modes</li>
 *   <li>Fetch new lore from the internet</li>
 * </ul>
 * </p>
 * <p>
 * This controller is the primary interface for the game creation panel
 * that appears when a player logs in and views their character list.
 * </p>
 */
@RestController
@RequestMapping("/api/v1/game-creator")
public class GameCreatorController {

    private static final Logger log = LoggerFactory.getLogger(GameCreatorController.class);

    private final GameCreatorService gameCreatorService;
    private final GameLoreService gameLoreService;
    private final GameDashboardService gameDashboardService;
    private final GameModeRegistry gameModeRegistry;

    public GameCreatorController(
            GameCreatorService gameCreatorService,
            GameLoreService gameLoreService,
            GameDashboardService gameDashboardService,
            GameModeRegistry gameModeRegistry) {
        this.gameCreatorService = gameCreatorService;
        this.gameLoreService = gameLoreService;
        this.gameDashboardService = gameDashboardService;
        this.gameModeRegistry = gameModeRegistry;
    }

    /**
     * Get all available game lores for the character selection panel.
     *
     * @return list of lore summaries
     */
    @GetMapping("/lores")
    public ResponseEntity<List<Map<String, Object>>> getAllLores() {
        log.debug("Returning all available lores");
        return ResponseEntity.ok(gameCreatorService.getLoreSummaries());
    }

    /**
     * Search game lores by keyword.
     *
     * @param keyword search keyword (optional — returns all if blank)
     * @return list of matching lore summaries
     */
    @GetMapping("/lores/search")
    public ResponseEntity<List<Map<String, Object>>> searchLores(
            @RequestParam(required = false, defaultValue = "") String keyword) {
        log.debug("Searching lores with keyword: {}", keyword);
        List<Map<String, Object>> results = gameLoreService.searchLores(keyword).stream()
                .map(lore -> {
                    Map<String, Object> summary = new HashMap<>();
                    summary.put("id", lore.getId());
                    summary.put("name", lore.getName());
                    summary.put("slug", lore.getSlug());
                    summary.put("genre", lore.getGenre());
                    summary.put("category", lore.getCategory());
                    summary.put("shortDescription", lore.getShortDescription());
                    summary.put("technologyLevel", lore.getTechnologyLevel());
                    summary.put("tone", lore.getTone());
                    summary.put("suitableForRpg", lore.getSuitableForRpg());
                    summary.put("suitableFor4x", lore.getSuitableFor4x());
                    summary.put("suitableForTycoon", lore.getSuitableForTycoon());
                    summary.put("suitableForTactical", lore.getSuitableForTactical());
                    summary.put("suitableForSurvival", lore.getSuitableForSurvival());
                    summary.put("suitableForSandbox", lore.getSuitableForSandbox());
                    summary.put("suitableForMmORPG", lore.getSuitableForMmORPG());
                    summary.put("suitableForRoguelike", lore.getSuitableForRoguelike());
                    summary.put("suitableForSimulation", lore.getSuitableForSimulation());
                    summary.put("suitableForAdventure", lore.getSuitableForAdventure());
                    summary.put("suitableForFps", lore.getSuitableForFps());
                    return summary;
                })
                .collect(java.util.stream.Collectors.toList());
        return ResponseEntity.ok(results);
    }

    /**
     * Get detailed information about a specific lore.
     *
     * @param slug the lore slug
     * @return lore details or 404
     */
    @GetMapping("/lores/{slug}")
    public ResponseEntity<?> getLoreBySlug(@PathVariable String slug) {
        log.debug("Getting lore by slug: {}", slug);
        return gameLoreService.getLoreBySlug(slug)
                .map(lore -> {
                    Map<String, Object> details = new HashMap<>();
                    details.put("id", lore.getId());
                    details.put("name", lore.getName());
                    details.put("slug", lore.getSlug());
                    details.put("shortDescription", lore.getShortDescription());
                    details.put("description", lore.getDescription());
                    details.put("genre", lore.getGenre());
                    details.put("category", lore.getCategory());
                    details.put("technologyLevel", lore.getTechnologyLevel());
                    details.put("tone", lore.getTone());
                    details.put("factions", lore.getFactions());
                    details.put("species", lore.getSpecies());
                    details.put("namingConvention", lore.getNamingConvention());
                    details.put("galaxyShape", lore.getGalaxyShape());
                    details.put("suggestedEmpireCount", lore.getSuggestedEmpireCount());
                    details.put("recommendedGameModes", lore.getRecommendedGameModes());
                    details.put("suitableForRpg", lore.getSuitableForRpg());
                    details.put("suitableFor4x", lore.getSuitableFor4x());
                    details.put("suitableForTycoon", lore.getSuitableForTycoon());
                    details.put("suitableForTactical", lore.getSuitableForTactical());
                    details.put("suitableForSurvival", lore.getSuitableForSurvival());
                    details.put("suitableForSandbox", lore.getSuitableForSandbox());
                    details.put("suitableForMmORPG", lore.getSuitableForMmORPG());
                    details.put("suitableForRoguelike", lore.getSuitableForRoguelike());
                    details.put("suitableForSimulation", lore.getSuitableForSimulation());
                    details.put("suitableForAdventure", lore.getSuitableForAdventure());
                    details.put("suitableForFps", lore.getSuitableForFps());
                    details.put("internetSourced", lore.getInternetSourced());
                    details.put("createdAt", lore.getCreatedAt());
                    return ResponseEntity.ok(details);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get all available game modes with descriptions.
     *
     * @return list of game modes
     */
    @GetMapping("/modes")
    public ResponseEntity<List<Map<String, String>>> getGameModes() {
        log.debug("Returning all available game modes");
        return ResponseEntity.ok(gameCreatorService.getAvailableGameModes());
    }

    /**
     * Get available genres for filtering lores.
     *
     * @return list of genre strings
     */
    @GetMapping("/genres")
    public ResponseEntity<List<String>> getGenres() {
        return ResponseEntity.ok(gameLoreService.getAvailableGenres());
    }

    /**
     * Get available categories for filtering lores.
     *
     * @return list of category strings
     */
    @GetMapping("/categories")
    public ResponseEntity<List<String>> getCategories() {
        return ResponseEntity.ok(gameLoreService.getAvailableCategories());
    }

    /**
     * Create a new game with the selected lore and game modes.
     * <p>
     * The player selects a lore/universe and checks boxes for the desired game modes.
     * If the lore doesn't exist in the database, it is automatically fetched from the internet.
     * </p>
     *
     * @param request the game creation request
     * @param authentication the current user's authentication
     * @return the game creation result
     */
    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> createGame(
            @Valid @RequestBody GameCreationRequest request,
            Authentication authentication) {
        log.info("Creating game for user: {}, lore='{}', modes={}",
                authentication.getName(), request.getLoreName(), request.getGameModes());

        try {
            GameCreatorService.GameCreationResult result = gameCreatorService.createGame(request);

            Map<String, Object> response = new HashMap<>();
            response.put("sessionId", result.getSessionId());
            response.put("sessionName", result.getSessionName());
            response.put("loreName", result.getLore().getName());
            response.put("loreSlug", result.getLore().getSlug());
            response.put("gameModes", result.getGameModes().stream()
                    .map(GameMode::name).toArray(String[]::new));
            response.put("systemCount", result.getConfig().getSystemCount());
            response.put("difficultyLevel", result.getConfig().getDifficultyLevel());
            response.put("galaxyShape", result.getConfig().getGalaxyShape());
            response.put("namingConvention", result.getConfig().getNamingConvention());
            response.put("empireCount", result.getConfig().getEmpireCount());
            response.put("enable4xLayer", result.getConfig().isEnable4xLayer());
            response.put("playerName", authentication.getName());
            response.put("createdAt", result.getCreatedAt());

            return ResponseEntity.status(201).body(response);

        } catch (IllegalArgumentException e) {
            log.warn("Invalid game creation request: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Failed to create game: {}", e.getMessage(), e);
            return ResponseEntity.status(500)
                    .body(Map.of("error", "Failed to create game: " + e.getMessage()));
        }
    }

    /**
     * Fetch a new lore from the internet and add it to the database.
     * <p>
     * Use this endpoint when a player requests a lore that doesn't exist
     * in the current database. The system will search the internet, extract
     * structured data, and store it for future use.
     * </p>
     *
     * @param loreName the name of the lore to fetch
     * @return the created lore entry
     */
    @PostMapping("/lores/fetch")
    public ResponseEntity<?> fetchLoreFromInternet(
            @RequestParam String loreName) {
        log.info("Fetching lore '{}' from internet", loreName);

        try {
            GameLore lore = gameLoreService.fetchLoreFromInternet(loreName);

            Map<String, Object> response = new HashMap<>();
            response.put("id", lore.getId());
            response.put("name", lore.getName());
            response.put("slug", lore.getSlug());
            response.put("genre", lore.getGenre());
            response.put("shortDescription", lore.getShortDescription());
            response.put("fetched", true);

            return ResponseEntity.status(201).body(response);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Failed to fetch lore '{}': {}", loreName, e.getMessage(), e);
            return ResponseEntity.status(500)
                    .body(Map.of("error", "Failed to fetch lore: " + e.getMessage()));
        }
    }

    /**
     * Get all games (galaxies) for the player's game selection dashboard.
     * <p>
     * This is the primary endpoint shown when a player logs in — it lists
     * all existing games/galaxies with their lore, game modes, and metadata.
     * </p>
     *
     * @return list of game dashboard entries
     */
    @GetMapping("/games")
    public ResponseEntity<List<Map<String, Object>>> getAllGames() {
        log.debug("Returning all games for dashboard");
        return ResponseEntity.ok(gameDashboardService.getAllGames());
    }

    /**
     * Get games filtered by lore slug.
     *
     * @param loreSlug the lore slug to filter by
     * @return list of games based on the specified lore
     */
    @GetMapping("/games/lore/{loreSlug}")
    public ResponseEntity<List<Map<String, Object>>> getGamesByLore(@PathVariable String loreSlug) {
        log.debug("Returning games for lore: {}", loreSlug);
        return ResponseEntity.ok(gameDashboardService.getGamesByLore(loreSlug));
    }

    /**
     * Get a single game's details by galaxy name.
     *
     * @param galaxyName the galaxy name
     * @return game details or 404
     */
    @GetMapping("/games/{galaxyName}")
    public ResponseEntity<?> getGameByName(@PathVariable String galaxyName) {
        log.debug("Getting game by name: {}", galaxyName);
        return gameDashboardService.getGameByName(galaxyName)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get dashboard statistics.
     *
     * @return statistics about games and lores
     */
    @GetMapping("/dashboard/stats")
    public ResponseEntity<Map<String, Object>> getDashboardStats() {
        return ResponseEntity.ok(gameDashboardService.getDashboardStats());
    }

    /**
     * Process a game turn for all active game modes in a galaxy.
     */
    @PostMapping("/games/{galaxyName}/turn")
    public ResponseEntity<?> processTurn(
            @PathVariable String galaxyName,
            @RequestParam int turnNumber,
            @RequestParam String playerId) {
        log.info("Processing turn {} for galaxy: {} by player: {}", turnNumber, galaxyName, playerId);
        try {
            String summary = gameModeRegistry.processTurnForGalaxy(galaxyName, turnNumber, playerId);
            Map<String, Object> response = new HashMap<>();
            response.put("galaxyName", galaxyName);
            response.put("turnNumber", turnNumber);
            response.put("summary", summary);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to process turn: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get the current state summary for all active game modes in a galaxy.
     */
    @GetMapping("/games/{galaxyName}/state")
    public ResponseEntity<?> getGameState(@PathVariable String galaxyName) {
        log.debug("Getting game state for galaxy: {}", galaxyName);
        try {
            String summary = gameModeRegistry.getGalaxyStateSummary(galaxyName);
            Set<GameMode> modes = gameModeRegistry.getActiveModesForGalaxy(galaxyName);
            Map<String, Object> response = new HashMap<>();
            response.put("galaxyName", galaxyName);
            response.put("activeModes", modes.stream().map(GameMode::name).collect(java.util.stream.Collectors.toList()));
            response.put("stateSummary", summary);

            // Per-module state details for richer UI display
            List<Map<String, Object>> moduleStates = new ArrayList<>();
            for (GameMode mode : modes) {
                GameModeModule module = gameModeRegistry.getModule(mode);
                if (module != null && module.isActiveForGalaxy(galaxyName)) {
                    Map<String, Object> moduleState = new HashMap<>();
                    moduleState.put("mode", mode.name());
                    moduleState.put("displayName", mode.getDisplayName());
                    moduleState.put("state", module.getStateSummary(galaxyName));
                    moduleStates.add(moduleState);
                }
            }
            response.put("moduleStates", moduleStates);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to get game state: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get all available game mode modules with descriptions.
     */
    @GetMapping("/mode-modules")
    public ResponseEntity<List<Map<String, Object>>> getGameModeModules() {
        return ResponseEntity.ok(gameModeRegistry.getModeSummaries());
    }

    /**
     * Get statistics about the universal lore database.
     *
     * @return database statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalLores", gameLoreService.countActiveLores());
        stats.put("genres", gameLoreService.getAvailableGenres());
        stats.put("categories", gameLoreService.getAvailableCategories());
        stats.put("gameModes", GameMode.values().length);
        return ResponseEntity.ok(stats);
    }
}
