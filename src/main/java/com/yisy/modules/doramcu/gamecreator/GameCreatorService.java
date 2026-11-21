package com.yisy.modules.doramcu.gamecreator;
import com.yisy.modules.doramcu.model.players.Player;
import org.springframework.stereotype.Service;

import com.yisy.modules.doramcu.dto.CreateGalaxyRequest;
import com.yisy.modules.doramcu.dto.ThemeGameResult;
import com.yisy.modules.doramcu.gamecore.GameMode;
import com.yisy.modules.doramcu.gamecore.MechanicType;
import com.yisy.modules.doramcu.gamemode.GameModeRegistry;
import com.yisy.modules.doramcu.hex.GalaxyGenerationConfig;
import com.yisy.modules.doramcu.model.galaxy.Galaxy;
import com.yisy.modules.doramcu.repository.galaxy.GalaxyRepository;
import com.yisy.modules.doramcu.service.galaxy.GalaxyCloneService;
import com.yisy.modules.doramcu.service.galaxy.GalaxyCreationService;
import com.yisy.modules.doramcu.service.galaxy.GalaxyTemplateService;
import com.yisy.modules.doramcu.service.session.GameSessionService;
import com.yisy.modules.doramcu.service.galaxy.RulesDatabaseGalaxyPopulator;
import com.yisy.modules.doramcu.service.game.ThemeBasedGameService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Main service for creating new games/galaxies based on selected lore and game modes.
 * <p>
 * This is the core of the DoraMCU game creator service. When a player logs in and
 * is presented with the character list, they can create a new game by:
 * <ol>
 *   <li>Selecting a game lore/universe from the universal lore database</li>
 *   <li>Checking boxes for the desired game modes (RPG, 4X, Tycoon, etc.)</li>
 *   <li>Confirming the game creation</li>
 * </ol>
 * </p>
 * <p>
 * If the selected lore doesn't exist in the database, the system automatically
 * searches the internet, loads the lore data, and then creates the game.
 * </p>
 * <p>
 * The service builds the galaxy configuration based on the lore's metadata
 * (factions, technology level, tone, naming convention) and the selected game modes,
 * then delegates to {@link GalaxyCreationService} for actual galaxy generation.
 * </p>
 */
@Service
public class GameCreatorService {

    private static final Logger log = LoggerFactory.getLogger(GameCreatorService.class);

    private final GameLoreService gameLoreService;
    private final GalaxyCreationService galaxyCreationService;
    private final GameSessionService gameSessionService;
    private final ThemeBasedGameService themeBasedGameService;
    private final GalaxyRepository galaxyRepository;
    private final GameModeRegistry gameModeRegistry;
    private final GalaxyTemplateService galaxyTemplateService;
    private final GalaxyCloneService galaxyCloneService;
    private final RulesDatabaseGalaxyPopulator rulesDatabaseGalaxyPopulator;

    public GameCreatorService(
            GameLoreService gameLoreService,
            GalaxyCreationService galaxyCreationService,
            GameSessionService gameSessionService,
            ThemeBasedGameService themeBasedGameService,
            GalaxyRepository galaxyRepository,
            GameModeRegistry gameModeRegistry,
            GalaxyTemplateService galaxyTemplateService,
            GalaxyCloneService galaxyCloneService,
            RulesDatabaseGalaxyPopulator rulesDatabaseGalaxyPopulator) {
        this.gameLoreService = gameLoreService;
        this.galaxyCreationService = galaxyCreationService;
        this.gameSessionService = gameSessionService;
        this.themeBasedGameService = themeBasedGameService;
        this.galaxyRepository = galaxyRepository;
        this.gameModeRegistry = gameModeRegistry;
        this.galaxyTemplateService = galaxyTemplateService;
        this.galaxyCloneService = galaxyCloneService;
        this.rulesDatabaseGalaxyPopulator = rulesDatabaseGalaxyPopulator;
    }

    /**
     * Create a new game based on the selected lore and game modes.
     * <p>
     * This is the main entry point for game creation. The process:
     * <ol>
     *   <li>Resolve the lore (fetch from internet if needed)</li>
     *   <li>Build galaxy configuration from lore metadata + selected game modes</li>
     *   <li>Create the galaxy and game session</li>
     *   <li>Return the creation result with session info</li>
     * </ol>
     * </p>
     *
     * @param request the game creation request with lore and modes
     * @return the game creation result
     */
    @Transactional
    public GameCreationResult createGame(GameCreationRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request must not be null");
        }

        String loreName = request.getLoreName();
        if (loreName == null || loreName.isBlank()) {
            throw new IllegalArgumentException("Lore name must not be null or blank");
        }

        List<GameMode> gameModes = request.getGameModes();
        if (gameModes == null || gameModes.isEmpty()) {
            throw new IllegalArgumentException("At least one game mode must be selected");
        }

        log.info("Creating game: lore='{}', modes={}, name='{}'",
                loreName, gameModes, request.getGameName());

        // Step 1: Resolve the lore (fetch from internet if needed)
        GameLore lore = resolveLore(loreName);
        log.info("Resolved lore: {}", lore);

        // Step 2: Build galaxy configuration from lore + game modes
        GalaxyGenerationConfig config = buildGalaxyConfig(lore, gameModes, request);
        log.info("Built galaxy config: {}", config);

        // Step 3: Create the galaxy
        String gameName = request.getGameName();
        if (gameName == null || gameName.isBlank()) {
            gameName = lore.getName() + " Galaxy";
        }

        GalaxyCreationService.GalaxyCreationResult creationResult =
                galaxyCreationService.createGalaxyWithAiPlayers(gameName);
        log.info("Galaxy created: sessionId={}, sessionName={}",
                creationResult.getSessionId(), creationResult.getSessionName());

        // Step 3.5: Store lore and game modes on the Galaxy entity
        updateGalaxyWithLoreInfo(gameName, lore, gameModes);

        // Step 3.6: Initialize game mode modules for the new galaxy
        initializeGameModeModules(gameName, gameModes, lore.getSlug());

        // Step 3.7: Use template-based galaxy creation flow
        initializeTemplateBasedGalaxy(gameName, lore.getSlug());

        // Step 4: Build and return the result
        return new GameCreationResult(
                creationResult.getSessionId(),
                creationResult.getSessionName(),
                lore,
                gameModes,
                config,
                LocalDateTime.now()
        );
    }

    /**
     * Resolve a lore by name, fetching from internet if it doesn't exist.
     */
    protected GameLore resolveLore(String loreName) {
        String slug = GameLoreService.toSlug(loreName);

        // Try to find existing lore
        Optional<GameLore> existing = gameLoreService.getLoreBySlug(slug);
        if (existing.isPresent()) {
            return existing.get();
        }

        // Try searching by name
        List<GameLore> found = gameLoreService.searchLores(loreName);
        if (!found.isEmpty()) {
            return found.get(0);
        }

        // Not found â€” fetch from internet
        log.info("Lore '{}' not found in database, fetching from internet", loreName);
        return gameLoreService.fetchLoreFromInternet(loreName);
    }

    /**
     * Build a galaxy configuration based on lore metadata and selected game modes.
     */
    protected GalaxyGenerationConfig buildGalaxyConfig(
            GameLore lore, List<GameMode> gameModes, GameCreationRequest request) {

        GalaxyGenerationConfig.Builder builder = GalaxyGenerationConfig.builder()
                .systemCount(25)
                .planetsPerSystem(4)
                .moonChance(0.5)
                .asteroidBeltChance(0.5)
                .spaceStationChance(0.2)
                .sectorSize(10)
                .subSectorDimension(10)
                .civilizationDensity(0.3)
                .resourceDistribution(0.5)
                .galaxyShape(lore.getGalaxyShape() != null ? lore.getGalaxyShape() : "spiral")
                .namingConvention(lore.getNamingConvention() != null ? lore.getNamingConvention() : "astronomical")
                .empireCount(lore.getSuggestedEmpireCount() != null ? lore.getSuggestedEmpireCount() : 4)
                .difficultyLevel("medium")
                .gameType(buildGameTypeString(gameModes))
                .campaignTheme(lore.getName())
                .adventureHookCount(10)
                .npcDensity(0.5)
                .systemTheme("mixed");

        // Adjust based on technology level
        if (lore.getTechnologyLevel() != null) {
            String tech = lore.getTechnologyLevel().toLowerCase();
            if (tech.contains("advanced") || tech.contains("far_future")) {
                builder.civilizationDensity(0.6).spaceStationChance(0.3);
            } else if (tech.contains("intermediate") || tech.contains("near_future")) {
                builder.civilizationDensity(0.4).spaceStationChance(0.2);
            } else if (tech.contains("primitive") || tech.contains("medieval")) {
                builder.civilizationDensity(0.2).spaceStationChance(0.1);
            }
        }

        // Adjust based on tone
        if (lore.getTone() != null) {
            String tone = lore.getTone().toLowerCase();
            if (tone.contains("grim") || tone.contains("horror") || tone.contains("survival")) {
                builder.difficultyLevel("hard").civilizationDensity(0.2);
            } else if (tone.contains("exploration") || tone.contains("hopeful")) {
                builder.difficultyLevel("medium").resourceDistribution(0.7);
            }
        }

        // Adjust based on game modes
        if (gameModes.contains(GameMode.FOUR_X)) {
            builder.enable4xLayer(true);
        }
        if (gameModes.contains(GameMode.RPG)) {
            builder.adventureHookCount(15).npcDensity(0.7);
        }
        if (gameModes.contains(GameMode.TYCOON)) {
            builder.resourceDistribution(0.8).civilizationDensity(0.5);
        }
        if (gameModes.contains(GameMode.SURVIVAL)) {
            builder.difficultyLevel("hard").resourceDistribution(0.3);
        }

        // Apply request overrides
        if (request.getSystemCount() != null) {
            builder.systemCount(request.getSystemCount());
        }
        if (request.getDifficultyLevel() != null) {
            builder.difficultyLevel(request.getDifficultyLevel());
        }

        return builder.build();
    }

    /**
     * Initialize game mode modules for the newly created galaxy.
     */
    private void initializeGameModeModules(String gameName, List<GameMode> gameModes, String loreSlug) {
        Set<GameMode> modeSet = new HashSet<>(gameModes);
        gameModeRegistry.initializeModulesForGalaxy(gameName, modeSet, loreSlug);
        log.info("Initialized game mode modules for galaxy: {}", gameName);
    }

    /**
     * Initialize the galaxy using the template-based creation flow.
     * <p>
     * Uses Earth 2026 as the default template, clones it with selected mechanics,
     * and populates the galaxy with data from the rules database.
     * </p>
     *
     * @param gameName the name of the new galaxy
     * @param loreSlug the lore slug for the template
     */
    private void initializeTemplateBasedGalaxy(String gameName, String loreSlug) {
        try {
            // Clone the default template (Earth 2026) with all mechanics
            GalaxyCloneService.CloneResult cloneResult =
                    galaxyCloneService.cloneDefaultTemplate(gameName, loreSlug);
            log.info("Template cloned for galaxy '{}': {} modules initialized",
                    gameName, cloneResult.getInitializedModules().size());

            // Populate galaxy with rules database data
            RulesDatabaseGalaxyPopulator.PopulationResult populationResult =
                    rulesDatabaseGalaxyPopulator.populateGalaxy(gameName, null);
            log.info("Galaxy '{}' populated with {} rules database entries",
                    gameName, populationResult.getTotalEntries());
        } catch (Exception e) {
            log.error("Failed to initialize template-based galaxy '{}': {}", gameName, e.getMessage(), e);
            // Don't fail the game creation if template initialization fails
        }
    }

    /**
     * Get all available galaxy templates with their installed mechanics.
     */
    public List<Map<String, Object>> getAvailableTemplates() {
        List<Map<String, Object>> templates = new ArrayList<>();
        for (GalaxyTemplateService.TemplateInfo info : galaxyTemplateService.listAvailableTemplates()) {
            Map<String, Object> template = new HashMap<>();
            template.put("name", info.getTemplateName());
            template.put("displayName", info.getDisplayName());
            template.put("description", info.getDescription());
            template.put("mechanics", info.getMechanics().stream()
                    .map(m -> m.getDisplayName())
                    .collect(Collectors.toList()));
            template.put("modules", info.getModules());
            template.put("isDefault", info.getTemplateName().equals(
                    galaxyTemplateService.getDefaultTemplate().name()));
            templates.add(template);
        }
        return templates;
    }

    /**
     * Get all available mechanics with descriptions.
     */
    public List<Map<String, String>> getAvailableMechanics() {
        List<Map<String, String>> mechanics = new ArrayList<>();
        for (MechanicType mechanic : MechanicType.values()) {
            Map<String, String> mechanicInfo = new HashMap<>();
            mechanicInfo.put("name", mechanic.name());
            mechanicInfo.put("displayName", mechanic.getDisplayName());
            mechanicInfo.put("description", mechanic.getDescription());
            mechanics.add(mechanicInfo);
        }
        return mechanics;
    }

    /**
     * Update the Galaxy entity with lore slug and game modes after creation.
     */
    private void updateGalaxyWithLoreInfo(String gameName, GameLore lore, List<GameMode> gameModes) {
        Optional<Galaxy> galaxyOpt = galaxyRepository.findByName(gameName);
        if (galaxyOpt.isPresent()) {
            Galaxy galaxy = galaxyOpt.get();
            galaxy.setLoreSlug(lore.getSlug());
            galaxy.setGameModes(gameModes.stream()
                    .map(GameMode::name)
                    .collect(Collectors.joining(",")));
            galaxy.setTheme(lore.getName());
            galaxy.setDescription(lore.getShortDescription());
            galaxyRepository.save(galaxy);
            log.info("Updated galaxy '{}' with lore='{}', gameModes='{}'",
                    gameName, lore.getSlug(), galaxy.getGameModes());
        } else {
            log.warn("Galaxy '{}' not found in repository after creation â€” lore info not stored", gameName);
        }
    }

    /**
     * Build a game type string from selected game modes.
     */
    protected String buildGameTypeString(List<GameMode> gameModes) {
        return gameModes.stream()
                .map(GameMode::name)
                .collect(Collectors.joining(","));
    }

    /**
     * Get all available game modes with descriptions.
     */
    public List<Map<String, String>> getAvailableGameModes() {
        List<Map<String, String>> modes = new ArrayList<>();
        for (GameMode mode : GameMode.values()) {
            Map<String, String> modeInfo = new HashMap<>();
            modeInfo.put("name", mode.name());
            modeInfo.put("displayName", mode.getDisplayName());
            modeInfo.put("description", mode.getDescription());
            modes.add(modeInfo);
        }
        return modes;
    }

    /**
     * Get a summary of all available lores for the character selection panel.
     */
    public List<Map<String, Object>> getLoreSummaries() {
        return gameLoreService.getAllActiveLores().stream()
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
                    summary.put("recommendedGameModes", lore.getRecommendedGameModes());
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
                .collect(Collectors.toList());
    }

    /**
     * Result of a game creation operation.
     */
    public static class GameCreationResult {
        private final String sessionId;
        private final String sessionName;
        private final GameLore lore;
        private final List<GameMode> gameModes;
        private final GalaxyGenerationConfig config;
        private final LocalDateTime createdAt;

        public GameCreationResult(
                String sessionId,
                String sessionName,
                GameLore lore,
                List<GameMode> gameModes,
                GalaxyGenerationConfig config,
                LocalDateTime createdAt) {
            this.sessionId = sessionId;
            this.sessionName = sessionName;
            this.lore = lore;
            this.gameModes = gameModes;
            this.config = config;
            this.createdAt = createdAt;
        }

        public String getSessionId() {
            return sessionId;
        }

        public String getSessionName() {
            return sessionName;
        }

        public GameLore getLore() {
            return lore;
        }

        public List<GameMode> getGameModes() {
            return gameModes;
        }

        public GalaxyGenerationConfig getConfig() {
            return config;
        }

        public LocalDateTime getCreatedAt() {
            return createdAt;
        }
    }
}
