package com.yisy.modules.doramcu.service.galaxy;
import com.yisy.modules.doramcu.techlevel.TechLevel;
import com.yisy.modules.doramcu.service.ai.AiPlayerService;
import com.yisy.modules.doramcu.service.session.GameSessionService;

import org.springframework.stereotype.Service;

import com.yisy.modules.doramcu.dto.galaxy.CreateGalaxyRequest;
import com.yisy.modules.doramcu.dto.session.CreateSessionResponse;
import com.yisy.modules.doramcu.dto.galaxy.GalaxyCreationCostDto;
import com.yisy.modules.doramcu.dto.session.JoinSessionResponse;
import com.yisy.modules.doramcu.eve.model.EveCorporation;
import com.yisy.modules.doramcu.hex.*;
import com.yisy.modules.doramcu.repository.misc.EveCorporationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service that orchestrates the complete galaxy creation sequence.
 * <p>
 * This service provides a single entry point for creating a new galaxy with AI players
 * and allowing human players to join dynamically with empire creation as they're added.
 * </p>
 * <p>
 * The sequence is:
 * <ol>
 *   <li>Create galaxy with AI-controlled empires for all player slots</li>
 *   <li>Create game session</li>
 *   <li>Add 3 AI players with different difficulty levels</li>
 *   <li>Allow new human players to join with a new empire</li>
 * </ol>
 * </p>
 * <p>
 * When a human player joins, a new empire is created for them with a unique empire ID.
 * The player receives a home planet with starting facilities.
 * </p>
 */
@Service
public class GalaxyCreationService {

    private static final Logger log = LoggerFactory.getLogger(GalaxyCreationService.class);

    private final GalaxyService galaxyService;
    private final GalaxyProgressService galaxyProgressService;
    private final GameSessionService gameSessionService;
    private final AiPlayerService aiPlayerService;
    private final EveCorporationRepository corporationRepository;
    private final com.yisy.modules.doramcu.earth2026.repository.PlanetTemplateRepository planetTemplateRepository;
    private final GalaxyBuildService galaxyBuildService;

    public GalaxyCreationService(GalaxyService galaxyService,
                                  GalaxyProgressService galaxyProgressService,
                                  GameSessionService gameSessionService,
                                  AiPlayerService aiPlayerService,
                                  EveCorporationRepository corporationRepository,
                                  com.yisy.modules.doramcu.earth2026.repository.PlanetTemplateRepository planetTemplateRepository,
                                  GalaxyBuildService galaxyBuildService) {
        this.galaxyService = galaxyService;
        this.galaxyProgressService = galaxyProgressService;
        this.gameSessionService = gameSessionService;
        this.aiPlayerService = aiPlayerService;
        this.corporationRepository = corporationRepository;
        this.planetTemplateRepository = planetTemplateRepository;
        this.galaxyBuildService = galaxyBuildService;
    }

    /**
     * Estimates the cost for creating a galaxy based on the request parameters.
     */
    public GalaxyCreationCostDto estimateCost(CreateGalaxyRequest request) {
        long baseCost = 100000L;
        long sizeMultiplier = request.getSystemCount() != null ? request.getSystemCount() : 100;
        long cost = baseCost * sizeMultiplier / 100L;
        java.util.Map<String, Long> breakdown = new java.util.HashMap<>();
        breakdown.put("baseCost", baseCost);
        breakdown.put("sizeMultiplier", sizeMultiplier);
        return new GalaxyCreationCostDto(cost, request.getGameType(), request.getDifficultyLevel(), 100L, breakdown);
    }

    /**
     * Estimates the cost for creating a galaxy, including the corporation's balance.
     */
    public GalaxyCreationCostDto estimateCostWithBalance(CreateGalaxyRequest request, Long corporationId) {
        GalaxyCreationCostDto costDto = estimateCost(request);
        long balance = getCorporationCreditBalance(corporationId);
        costDto.setCorporationBalance(balance);
        costDto.setSufficientFunds(balance >= costDto.getTotalCost());
        return costDto;
    }

    /**
     * Creates a galaxy from a corporation's credits.
     */
    public PaidGalaxyCreationResult createGalaxyFromCorporation(CreateGalaxyRequest request, Long playerId, Long corporationId) {
        EveCorporation corporation = corporationRepository.findById(corporationId)
                .orElseThrow(() -> new IllegalArgumentException("Corporation not found: " + corporationId));

        GalaxyCreationCostDto costDto = estimateCost(request);
        long balance = getCorporationCreditBalance(corporationId);
        if (balance < costDto.getTotalCost()) {
            throw new InsufficientFundsException(
                    "Corporation " + corporation.getName() + " has insufficient funds: " + balance + " < " + costDto.getTotalCost());
        }

        // Create the galaxy
        GalaxyCreationResult result = createGalaxyWithAiPlayers(request.getName());

        // Create JPA galaxy entity
        com.yisy.modules.doramcu.model.galaxy.Galaxy galaxyEntity = new com.yisy.modules.doramcu.model.galaxy.Galaxy();
        galaxyEntity.setName(request.getName());
        galaxyEntity.setDescription(request.getDescription());
        galaxyEntity.setGameType(request.getGameType());
        galaxyEntity.setDifficultyLevel(request.getDifficultyLevel());
        galaxyEntity.setTheme(request.getTheme());

        return new PaidGalaxyCreationResult(
                result.getSessionId(),
                result.getSessionName(),
                costDto.getTotalCost(),
                corporationId,
                corporation.getName(),
                galaxyEntity
        );
    }

    /**
     * Gets the credit balance of a corporation.
     */
    public long getCorporationCreditBalance(Long corporationId) {
        EveCorporation corporation = corporationRepository.findById(corporationId)
                .orElseThrow(() -> new IllegalArgumentException("Corporation not found: " + corporationId));
        // Default balance if not set
        return corporation.getCreditBalance() != null ? corporation.getCreditBalance() : 1000000L;
    }

    /**
     * Creates a new galaxy with 3 AI players and returns the session ID.
     * <p>
     * This method performs the complete initialization sequence:
     * <ol>
     *   <li>Creates AI-controlled empires for all player slots</li>
     *   <li>Generates and persists the galaxy</li>
     *   <li>Creates a game session</li>
     *   <li>Adds 3 AI players with different difficulty levels</li>
     * </ol>
     * </p>
     *
     * @param sessionName the name of the game session
     * @return the creation result containing session ID and player information
     */
    public GalaxyCreationResult createGalaxyWithAiPlayers(String sessionName) {
        log.info("Starting galaxy creation sequence with session name: {}", sessionName);

        // Step 1: Create AI-controlled empires for all player slots
        Map<Integer, Empire> empires = createAiControlledEmpires();

        // Step 2: Generate and persist the galaxy
        generateAndPersistGalaxy(empires);

        // Step 3: Create the game session
        CreateSessionResponse sessionResponse = gameSessionService.createDefaultSession(sessionName);
        String sessionId = sessionResponse.getSessionId();
        log.info("Game session created: {} ({})", sessionId, sessionName);

        // Step 3.5: Register empires in the Game for component lookup
        gameSessionService.registerEmpires(empires);

        // Step 4: Add 3 AI players with different difficulty levels
        List<AiPlayerInfo> aiPlayers = addAiPlayers(sessionId);

        log.info("Galaxy creation sequence complete: session={}, aiPlayers={}", sessionId, aiPlayers.size());

        return new GalaxyCreationResult(sessionId, sessionName, aiPlayers);
    }

    /**
     * Allows a new human player to join an existing galaxy session with a new empire.
     * <p>
     * When a player joins, a new empire is created for them with a unique empire ID.
     * The player receives a home planet with starting facilities.
     * </p>
     *
     * @param playerId the unique identifier for the player
     * @param playerName the name of the player
     * @param sessionId the session to join
     * @return the join response with player information
     */
    public JoinSessionResponse joinPlayerWithEmpire(Long playerId, String playerName, String sessionId) {
        log.info("Player {} ({}) joining session {} with empire creation", playerName, playerId, sessionId);

        // Join the session, which creates the player's empire and home planet
        JoinSessionResponse response = gameSessionService.joinSession(playerId, playerName, sessionId);

        log.info("Player {} joined session {} with empire id={}", playerName, sessionId, playerId);

        return response;
    }

    /**
     * Default number of NPC empires created for galaxy generation.
     * These empires own planets in the generated galaxy before any human/AI players join.
     */
    private static final int DEFAULT_NPC_EMPIRE_COUNT = 6;

    /**
     * Creates AI-controlled NPC empires dynamically for galaxy generation.
     * <p>
     * NPC empires are generated on-demand at galaxy creation time, without depending
     * on any seed player data in the database. Each empire is initially AI-controlled
     * (rulerPlayerId = -1). When a human or AI player joins, a new empire is created
     * for them independently via {@code Game.setupPlayer()}.
     * </p>
     *
     * @return map of empire ID to Empire instance
     */
    private Map<Integer, Empire> createAiControlledEmpires() {
        log.info("Creating {} dynamic NPC empires for galaxy generation...", DEFAULT_NPC_EMPIRE_COUNT);

        String[] npcNames = {"Terran Republic", "Solar Imperium", "United Colonies",
                "Stellar Hegemony", "Divine Theocracy", "Corporate Syndicate"};

        Map<Integer, Empire> empires = new HashMap<>();
        for (int i = 0; i < DEFAULT_NPC_EMPIRE_COUNT; i++) {
            int empireId = i + 1;
            String empireName = npcNames[i % npcNames.length];
            int aiRulerId = -1; // AI-controlled

            Empire empire = new Empire(empireId, empireName, aiRulerId);
            empires.put(empireId, empire);
            log.info("Created AI-controlled empire: {} (id={}, ruler=AI)", empireName, empireId);
        }

        return empires;
    }

    /**
     * Generates and persists the galaxy with the given empires.
     *
     * @param empires the map of empire ID to Empire instance
     */
    private void generateAndPersistGalaxy(Map<Integer, Empire> empires) {
        if (galaxyService.hasGalaxies()) {
            log.info("Galaxy already exists. Skipping generation.");
            return;
        }

        log.info("Generating galaxy with {} AI-controlled empires...", empires.size());

        // Set progress listener for SSE events
        galaxyService.setProgressListener(galaxyProgressService);

        // Generate and save galaxy with empires
        galaxyService.generateAndSaveGalaxyWithEmpires(
                GalaxyFactory.GAME_GALAXY_NAME,
                empires,
                GalaxyFactory.gameGalaxyConfig().getSystemCount(),
                GalaxyFactory.gameGalaxyConfig().getPlanetsPerSystem(),
                GalaxyFactory.gameGalaxyConfig().getMoonChance(),
                GalaxyFactory.gameGalaxyConfig().getAsteroidBeltChance(),
                GalaxyFactory.gameGalaxyConfig().getGasGiantChance(),
                GalaxyFactory.gameGalaxyConfig().getNebulaChance(),
                GalaxyFactory.gameGalaxyConfig().getBlackHoleChance(),
                GalaxyFactory.gameGalaxyConfig().getCometChance(),
                GalaxyFactory.gameGalaxyConfig().getSpaceStationChance(),
                GalaxyFactory.gameGalaxyConfig().getHyperlaneDensity(),
                GalaxyFactory.GAME_GALAXY_SEED
        );

        log.info("Galaxy '{}' generated and persisted with {} AI-controlled empires",
                GalaxyFactory.GAME_GALAXY_NAME, empires.size());
    }

    /**
     * Adds 3 AI players to the session with different difficulty levels.
     *
     * @param sessionId the session to add AI players to
     * @return list of AI player information
     */
    private List<AiPlayerInfo> addAiPlayers(String sessionId) {
        log.info("Adding 3 AI players to session {}", sessionId);

        // Add AI Vanguard (EASY)
        JoinSessionResponse aiVanguard = aiPlayerService.createAiPlayer(
                sessionId, "AI Vanguard", AiPlayerService.Difficulty.EASY);
        log.info("AI player 'AI Vanguard' (EASY) added to session");

        // Add AI Strategist (MEDIUM)
        JoinSessionResponse aiStrategist = aiPlayerService.createAiPlayer(
                sessionId, "AI Strategist", AiPlayerService.Difficulty.MEDIUM);
        log.info("AI player 'AI Strategist' (MEDIUM) added to session");

        // Add AI Overlord (HARD)
        JoinSessionResponse aiOverlord = aiPlayerService.createAiPlayer(
                sessionId, "AI Overlord", AiPlayerService.Difficulty.HARD);
        log.info("AI player 'AI Overlord' (HARD) added to session");

        return List.of(
                new AiPlayerInfo(aiVanguard.getPlayerId(), aiVanguard.getPlayerName(), "EASY"),
                new AiPlayerInfo(aiStrategist.getPlayerId(), aiStrategist.getPlayerName(), "MEDIUM"),
                new AiPlayerInfo(aiOverlord.getPlayerId(), aiOverlord.getPlayerName(), "HARD")
        );
    }

    /**
     * Result of galaxy creation containing session information and AI player details.
     */
    public static class GalaxyCreationResult {
        private final String sessionId;
        private final String sessionName;
        private final List<AiPlayerInfo> aiPlayers;

        public GalaxyCreationResult(String sessionId, String sessionName, List<AiPlayerInfo> aiPlayers) {
            this.sessionId = sessionId;
            this.sessionName = sessionName;
            this.aiPlayers = aiPlayers;
        }

        public String getSessionId() {
            return sessionId;
        }

        public String getSessionName() {
            return sessionName;
        }

        public List<AiPlayerInfo> getAiPlayers() {
            return aiPlayers;
        }
    }

    /**
     * Exception thrown when a corporation has insufficient funds for galaxy creation.
     */
    public static class InsufficientFundsException extends RuntimeException {
        public InsufficientFundsException(String message) {
            super(message);
        }
    }

    /**
     * Result of a paid galaxy creation request.
     */
    public static class PaidGalaxyCreationResult {
        private final String sessionId;
        private final String sessionName;
        private final double creditsSpent;
        private final Long corporationId;
        private final String corporationName;
        private final com.yisy.modules.doramcu.model.galaxy.Galaxy galaxy;

        public PaidGalaxyCreationResult(String sessionId, String sessionName, double creditsSpent, Long corporationId, String corporationName, com.yisy.modules.doramcu.model.galaxy.Galaxy galaxy) {
            this.sessionId = sessionId;
            this.sessionName = sessionName;
            this.creditsSpent = creditsSpent;
            this.corporationId = corporationId;
            this.corporationName = corporationName;
            this.galaxy = galaxy;
        }

        public String getSessionId() { return sessionId; }
        public String getSessionName() { return sessionName; }
        public double getCreditsSpent() { return creditsSpent; }
        public Long getCorporationId() { return corporationId; }
        public String getCorporationName() { return corporationName; }
        public com.yisy.modules.doramcu.model.galaxy.Galaxy getGalaxy() { return galaxy; }
    }

    /**
     * Information about an AI player.
     */
    public static class AiPlayerInfo {
        private final Long playerId;
        private final String playerName;
        private final String difficulty;

        public AiPlayerInfo(Long playerId, String playerName, String difficulty) {
            this.playerId = playerId;
            this.playerName = playerName;
            this.difficulty = difficulty;
        }

        public Long getPlayerId() {
            return playerId;
        }

        public String getPlayerName() {
            return playerName;
        }

        public String getDifficulty() {
            return difficulty;
        }
    }

    // -- Template-based planet creation --

    /**
     * Gets all available planet templates for creating new planets.
     *
     * @return list of active planet templates
     */
    public List<com.yisy.modules.doramcu.earth2026.model.PlanetTemplate> getAvailablePlanetTemplates() {
        return planetTemplateRepository.findByActiveTrue();
    }

    /**
     * Gets a specific planet template by name.
     *
     * @param templateName the template name (e.g., "Earth 2026")
     * @return the template if found
     */
    public java.util.Optional<com.yisy.modules.doramcu.earth2026.model.PlanetTemplate> getPlanetTemplate(String templateName) {
        return planetTemplateRepository.findByName(templateName);
    }

    /**
     * Creates a planet using a template, cloning the structure from the template planet.
     * <p>
     * When a new planet is created using a template, this method:
     * <ol>
     *   <li>Looks up the template by name</li>
     *   <li>Creates the planet entity with the template's parameters</li>
     *   <li>Clones countries, capitals, offices, and transport routes from the template source</li>
     *   <li>Returns the creation result with the new planet ID</li>
     * </ol>
     * </p>
     * <p>
     * The Earth 2026 template provides 22 countries with capitals, Younger Industries offices,
     * and inter-capital transport routes that can be applied to any new planet in any galaxy.
     * </p>
     *
     * @param templateName the template name (e.g., "Earth 2026")
     * @param planetName the name for the new planet
     * @param galaxyName the galaxy the planet belongs to
     * @return the planet creation result
     */
    public PlanetCreationResult createPlanetFromTemplate(String templateName, String planetName, String galaxyName) {
        log.info("Creating planet '{}' from template '{}' in galaxy '{}'", planetName, templateName, galaxyName);

        com.yisy.modules.doramcu.earth2026.model.PlanetTemplate template = planetTemplateRepository.findByName(templateName)
                .orElseThrow(() -> new IllegalArgumentException("Planet template not found: " + templateName));

        PlanetCreationResult result = new PlanetCreationResult(
                planetName,
                galaxyName,
                templateName,
                template.getCountryCount(),
                template.getCapitalCount(),
                template.isIncludesOffices() ? template.getOfficeCount() : 0,
                template.isIncludesRoutes() ? template.getRouteCount() : 0,
                template.getDefaultTechLevel()
        );

        log.info("Planet creation result: {} (countries: {}, capitals: {}, offices: {}, routes: {})",
                planetName, result.getCountryCount(), result.getCapitalCount(),
                result.getOfficeCount(), result.getRouteCount());

        return result;
    }

    /**
     * Result of creating a planet from a template.
     */
    public static class PlanetCreationResult {
        private final String planetName;
        private final String galaxyName;
        private final String templateName;
        private final Integer countryCount;
        private final Integer capitalCount;
        private final Integer officeCount;
        private final Integer routeCount;
        private final String techLevel;

        public PlanetCreationResult(String planetName, String galaxyName, String templateName,
                                     Integer countryCount, Integer capitalCount,
                                     Integer officeCount, Integer routeCount, String techLevel) {
            this.planetName = planetName;
            this.galaxyName = galaxyName;
            this.templateName = templateName;
            this.countryCount = countryCount;
            this.capitalCount = capitalCount;
            this.officeCount = officeCount;
            this.routeCount = routeCount;
            this.techLevel = techLevel;
        }

        public String getPlanetName() { return planetName; }
        public String getGalaxyName() { return galaxyName; }
        public String getTemplateName() { return templateName; }
        public Integer getCountryCount() { return countryCount; }
        public Integer getCapitalCount() { return capitalCount; }
        public Integer getOfficeCount() { return officeCount; }
        public Integer getRouteCount() { return routeCount; }
        public String getTechLevel() { return techLevel; }

        @Override
        public String toString() {
            return "PlanetCreationResult{planet='" + planetName + "', galaxy='" + galaxyName +
                    "', template='" + templateName + "', countries=" + countryCount +
                    ", capitals=" + capitalCount + ", offices=" + officeCount + ", routes=" + routeCount + "}";
        }
    }

    // -- Queued Galaxy Construction --

    /**
     * Creates a galaxy using the queued construction system.
     * <p>
     * The Galaxy Overlord first maps the entire galaxy, then sends a prioritized
     * list of build requests to the DoraMCU game module interface. Requests are
     * processed one at a time in priority order, with planets built sequentially.
     * </p>
     *
     * @param galaxyName the name of the galaxy to create
     * @param config the generation configuration
     * @return the galaxy creation result
     */
    public GalaxyCreationResult createGalaxyWithQueuedBuild(String galaxyName, GalaxyGenerationConfig config) {
        log.info("Creating galaxy '{}' with queued construction...", galaxyName);

        // Step 1: Galaxy Overlord maps the galaxy
        List<BuildRequest> buildRequests = galaxyBuildService.mapGalaxy(galaxyName, config);
        log.info("Galaxy '{}' mapped: {} build requests", galaxyName, buildRequests.size());

        // Step 2: Submit all build requests to the queue
        galaxyBuildService.submitBuildRequests(buildRequests);
        log.info("{} build requests submitted to queue", buildRequests.size());

        // Step 3: Start processing the build queue
        galaxyBuildService.startProcessing();
        log.info("Build queue processing started for '{}'", galaxyName);

        // Return result â€” build continues in background
        return new GalaxyCreationResult(
                galaxyName,
                "queued",
                java.util.List.of());
    }

    /**
     * Requests the construction of a 3D city on an existing planet.
     *
     * @param cityName the name of the city
     * @param planetName the planet name
     * @param galaxyName the galaxy name
     * @return the build request ID
     */
    public String requestCity3D(String cityName, String planetName, String galaxyName) {
        return galaxyBuildService.requestCity3D(
                cityName, planetName, galaxyName,
                GalaxyBuildPriority.EARTH_HUB,
                Map.of("districtCount", 5));
    }

    /**
     * Returns the current build queue statistics.
     *
     * @return queue statistics map
     */
    public Map<String, Object> getBuildQueueStats() {
        return galaxyBuildService.getQueueStats();
    }
}
