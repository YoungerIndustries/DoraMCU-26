package com.yisy.modules.doramcu.service.session;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yisy.modules.doramcu.ai.AiDelegationService;
import com.yisy.modules.doramcu.dto.battle.BattleSummaryDto;
import com.yisy.modules.doramcu.dto.battle.EngageCombatRequest;
import com.yisy.modules.doramcu.dto.battle.ReinforceBattleRequest;
import com.yisy.modules.doramcu.dto.fleet.CreateFleetRequest;
import com.yisy.modules.doramcu.dto.fleet.FleetDto;
import com.yisy.modules.doramcu.dto.fleet.MoveFleetRequest;
import com.yisy.modules.doramcu.dto.game.ColonizeRequest;
import com.yisy.modules.doramcu.dto.misc.FourxUnlockState;
import com.yisy.modules.doramcu.dto.misc.IncomeProjectionDto;
import com.yisy.modules.doramcu.dto.misc.PipelineDetailsDto;
import com.yisy.modules.doramcu.dto.misc.ResourcePipelineSummary;
import com.yisy.modules.doramcu.dto.players.PlayerSummary;
import com.yisy.modules.doramcu.dto.session.CreateSessionResponse;
import com.yisy.modules.doramcu.dto.session.GameEvent;
import com.yisy.modules.doramcu.dto.session.GameStateResponse;
import com.yisy.modules.doramcu.dto.session.JoinSessionResponse;
import com.yisy.modules.doramcu.dto.ship_design.BuildShipRequest;
import com.yisy.modules.doramcu.dto.ship_design.ShipDesignRequest;
import com.yisy.modules.doramcu.dto.station.BuildFacilityRequest;
import com.yisy.modules.doramcu.dto.station.BuildStationRequest;
import com.yisy.modules.doramcu.dto.trade.TradeOpportunityDto;
import com.yisy.modules.doramcu.dto.trade.TradeRequest;
import com.yisy.modules.doramcu.dto.trade.TradeSummaryDto;
import com.yisy.modules.doramcu.exception.NotYourTurnException;
import com.yisy.modules.doramcu.gamecore.GameType;
import com.yisy.modules.doramcu.hex.Empire;
import com.yisy.modules.doramcu.hex.Fleet;
import com.yisy.modules.doramcu.hex.Galaxy;
import com.yisy.modules.doramcu.hex.Game;
import com.yisy.modules.doramcu.hex.Hex;
import com.yisy.modules.doramcu.hex.Hex3D;
import com.yisy.modules.doramcu.hex.Player;
import com.yisy.modules.doramcu.hex.ResourcePipelineManager;
import com.yisy.modules.doramcu.hex.Ship;
import com.yisy.modules.doramcu.hex.ShipType;
import com.yisy.modules.doramcu.hex.SubSector;
import com.yisy.modules.doramcu.model.session.GameActionLog;
import com.yisy.modules.doramcu.model.session.GameSession;
import com.yisy.modules.doramcu.model.session.PersistedGameSession;
import com.yisy.modules.doramcu.repository.galaxy.GalaxyRepository;
import com.yisy.modules.doramcu.repository.session.PersistedGameSessionRepository;
import com.yisy.modules.doramcu.service.character.CharacterService;
import com.yisy.modules.doramcu.service.galaxy.GalaxyCreationService;
import com.yisy.modules.doramcu.service.game.GameInitializationService;
import com.yisy.modules.doramcu.service.game.GameReplayService;
import com.yisy.modules.doramcu.service.game.GameStateBuilder;
import com.yisy.modules.doramcu.service.ship_design.ShipDesignService;
import com.yisy.modules.doramcu.unified.GameCalendarFormatter;
import com.yisy.modules.doramcu.unified.GameEngine;
import com.yisy.modules.doramcu.unified.GlobalClock;
import com.yisy.modules.doramcu.unified.GlobalClockService;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Central game session service that owns the session map and delegates to
 * specialized handlers for actions and state queries.
 * <p>
 * This class was refactored from a 1,579-line monolith into a thin facade
 * that coordinates between:
 * <ul>
 *   <li>{@code SessionActionHandler} â€” all mutable game actions (move, build, combat, etc.)</li>
 *   <li>{@code GameStateBuilder} â€” all response DTO building</li>
 * </ul>
 * Session lifecycle operations (create, join, leave, persist) remain here
 * as they are tightly coupled to the session map ownership.
 * </p>
 */
@Service
@Transactional(readOnly = true)
public class GameSessionService {

    private static final Logger log = LoggerFactory.getLogger(GameSessionService.class);

    private final Map<String, GameSession> sessions = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final PersistedGameSessionRepository persistedGameSessionRepository;
    private final GalaxyRepository galaxyRepository;
    private final GameInitializationService gameInitializationService;
    private final TurnScheduler turnScheduler;
    private final GameStateBuilder gameStateBuilder;
    private final CharacterService characterService;
    private final AiDelegationService aiDelegationService;
    private final SessionActionHandler sessionActionHandler;
    private final GlobalClockService globalClockService;
    private final GameReplayService gameReplayService;

    public GameSessionService(PersistedGameSessionRepository persistedGameSessionRepository,
                              GalaxyRepository galaxyRepository,
                              GameInitializationService gameInitializationService,
                              @Lazy TurnScheduler turnScheduler,
                              GameStateBuilder gameStateBuilder,
                              CharacterService characterService,
                              @Lazy AiDelegationService aiDelegationService,
                              SessionActionHandler sessionActionHandler,
                              GlobalClockService globalClockService,
                              GameReplayService gameReplayService) {
        this.persistedGameSessionRepository = persistedGameSessionRepository;
        this.galaxyRepository = galaxyRepository;
        this.gameInitializationService = gameInitializationService;
        this.turnScheduler = turnScheduler;
        this.gameStateBuilder = gameStateBuilder;
        this.characterService = characterService;
        this.aiDelegationService = aiDelegationService;
        this.sessionActionHandler = sessionActionHandler;
        this.globalClockService = globalClockService;
        this.gameReplayService = gameReplayService;
    }

    /**
     * Restores persisted game sessions on application startup.
     * Loads all sessions from the database that have saved game state.
     */
    @PostConstruct
    void restorePersistedSessions() {
        if (persistedGameSessionRepository == null) {
            return;
        }
        try {
            List<PersistedGameSession> persisted = persistedGameSessionRepository.findAll();
            int restored = 0;
            for (PersistedGameSession pgs : persisted) {
                if (pgs.getGameState() == null || pgs.getGameState().isBlank()) {
                    log.info("Skipping session {} (no saved game state)", pgs.getSessionId());
                    continue;
                }
                try {
                    restoreSession(pgs.getSessionId());
                    restored++;
                } catch (Exception e) {
                    log.error("Failed to restore session {}: {}", pgs.getSessionId(), e.getMessage());
                }
            }
            if (restored > 0) {
                log.info("Restored {} persisted game sessions on startup", restored);
            }
        } catch (Exception e) {
            log.error("Failed to restore persisted sessions: {}", e.getMessage());
        }
    }

    /**
     * Restores a single game session from the database into the in-memory session map.
     * Reconstructs the Game object from the serialized JSON state and reloads the galaxy.
     *
     * @param sessionId the session ID to restore
     * @return true if the session was successfully restored
     */
    @Transactional
    public boolean restoreSession(String sessionId) {
        if (persistedGameSessionRepository == null) {
            return false;
        }

        PersistedGameSession persisted = persistedGameSessionRepository.findBySessionId(sessionId)
                .orElse(null);
        if (persisted == null) {
            log.warn("No persisted session found for: {}", sessionId);
            return false;
        }

        if (persisted.getGameState() == null || persisted.getGameState().isBlank()) {
            log.warn("No saved game state for session: {}", sessionId);
            return false;
        }

        // Parse the serialized game state
        Map<String, Object> gameState;
        try {
            gameState = objectMapper.readValue(persisted.getGameState(),
                    new TypeReference<Map<String, Object>>() {});
        } catch (JsonProcessingException e) {
            log.error("Failed to parse game state for session {}: {}", sessionId, e.getMessage());
            return false;
        }

        // Create a new game and deserialize the state
        Game game = new Game(persisted.getSessionName());
        game.deserializeGameState(gameState);

        // Load galaxy from database
        loadGalaxyToGame(game, persisted.getSessionName());

        // Resolve game type from persisted session
        GameType gameType = GameType.DORAMCU_4X;
        if (persisted.getGameType() != null && !persisted.getGameType().isBlank()) {
            try {
                gameType = GameType.valueOf(persisted.getGameType());
            } catch (IllegalArgumentException e) {
                log.warn("Unknown game type '{}' in persisted session {}, defaulting to DORAMCU_4X", persisted.getGameType(), sessionId);
            }
        }

        // Create the session wrapper
        GameSession session = new GameSession(sessionId, persisted.getSessionName(), game);
        GlobalClock clock = globalClockService.getOrCreateClock(sessionId, gameType);
        session.setGlobalClock(clock);

        // Restore player IDs
        if (persisted.getPlayerIds() != null && !persisted.getPlayerIds().isBlank()) {
            String cleaned = persisted.getPlayerIds().replaceAll("[\\[\\] ]", "");
            for (String pid : cleaned.split(",")) {
                if (!pid.isEmpty()) {
                    try {
                        session.addPlayer(Long.parseLong(pid.trim()));
                    } catch (NumberFormatException e) {
                        log.warn("Invalid player ID in persisted session {}: {}", sessionId, pid);
                    }
                }
            }
        }

        sessions.put(sessionId, session);
        log.info("Restored session {} with {} players, {} planets, {} fleets",
                sessionId, session.getPlayerIds().size(),
                ((Game) session.getGame()).getPlanets().size(),
                ((Game) session.getGame()).getFleets().size());
        return true;
    }

    /**
     * Lists all persisted game sessions with their metadata.
     *
     * @return list of persisted session metadata maps
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> listPersistedSessions() {
        if (persistedGameSessionRepository == null) {
            return List.of();
        }
        List<PersistedGameSession> all = persistedGameSessionRepository.findAll();
        List<Map<String, Object>> result = new ArrayList<>();
        for (PersistedGameSession pgs : all) {
            Map<String, Object> map = new HashMap<>();
            map.put("sessionId", pgs.getSessionId());
            map.put("sessionName", pgs.getSessionName());
            map.put("playerIds", pgs.getPlayerIds());
            map.put("hasGameState", pgs.getGameState() != null && !pgs.getGameState().isBlank());
            map.put("createdAt", pgs.getCreatedAt());
            map.put("updatedAt", pgs.getUpdatedAt());
            map.put("lastActivityAt", pgs.getLastActivityAt());
            map.put("snapshotCount", pgs.getSnapshotCount());
            map.put("isLoaded", sessions.containsKey(pgs.getSessionId()));
            result.add(map);
        }
        return result;
    }

    /**
     * Creates a new game session without an initial human player.
     * <p>
     * Used by {@link GalaxyCreationService} to create a session that AI players can join first,
     * before human players join dynamically.
     * </p>
     *
     * @param sessionName the name of the session
     * @return the session response with a generated session ID
     */
    @Transactional
    public CreateSessionResponse createDefaultSession(String sessionName) {
        String sessionId = UUID.randomUUID().toString();
        log.debug("Creating default game session: {}", sessionName);

        Game game = new Game(sessionName);
        GameType gameType = loadGalaxyToGame(game, sessionName);

        GameSession session = new GameSession(sessionId, sessionName, game);
        // Initialize global clock for this session
        GlobalClock clock = globalClockService.getOrCreateClock(sessionId, gameType);
        session.setGlobalClock(clock);
        sessions.put(sessionId, session);

        // Persist session metadata to database
        persistSession(sessionId, sessionName, session.getPlayerIds());

        broadcastEvent(sessionId, new GameEvent(GameEvent.EventType.SESSION_CREATED, sessionId,
                Map.of("sessionName", sessionName)));

        return new CreateSessionResponse(sessionId, sessionName, null, null);
    }

    @Transactional
    public CreateSessionResponse createSession(Long playerId, String playerName, String sessionName) {
        String sessionId = UUID.randomUUID().toString();
        log.debug("Creating new game session: {} for player: {} ({})", sessionId, playerName, playerId);

        Game game = new Game(sessionName);
        GameType gameType = loadGalaxyToGame(game, sessionName);

        // Set up the complete player: player, empire, home system, fleets, navy sync
        Game.PlayerSetupResult setup = game.setupPlayer(playerId.intValue(), playerName);
        log.info("Player {} (id={}) created with empire id={}", playerName, playerId, setup.getEmpire().getId());

        GameSession session = new GameSession(sessionId, sessionName, game);
        // Initialize global clock for this session
        GlobalClock clock = globalClockService.getOrCreateClock(sessionId, gameType);
        session.setGlobalClock(clock);
        session.addPlayer(playerId);
        sessions.put(sessionId, session);

        // Persist session metadata to database
        persistSession(sessionId, sessionName, session.getPlayerIds());

        broadcastEvent(sessionId, new GameEvent(GameEvent.EventType.PLAYER_JOINED, sessionId,
                Map.of("playerId", playerId, "playerName", playerName)));

        return new CreateSessionResponse(sessionId, sessionName, playerId, playerName);
    }

    @Transactional
    public JoinSessionResponse joinSession(Long playerId, String playerName, String sessionId) {
        GameSession session = sessions.get(sessionId);
        if (session == null) {
            log.warn("Session not found: {}", sessionId);
            throw new NoSuchElementException("Session not found: " + sessionId);
        }

        log.debug("Player {} ({}) joining session: {}", playerName, playerId, sessionId);

        // If player is already in the session, throw an exception
        if (session.getPlayerIds().contains(playerId)) {
            log.warn("Player {} is already in session: {}", playerId, sessionId);
            throw new IllegalStateException("Player is already in this session");
        }

        Game game = (Game) session.getGame();
        session.getLock().writeLock().lock();
        try {
            // Set up the complete player: player, empire, home system, fleets, navy sync
            Game.PlayerSetupResult setup = game.setupPlayer(playerId.intValue(), playerName);
            log.info("Player {} (id={}) joined with empire id={}", playerName, playerId, setup.getEmpire().getId());
        } finally {
            session.getLock().writeLock().unlock();
        }

        session.addPlayer(playerId);

        // Update persisted session with new player
        persistSession(sessionId, session.getName(), session.getPlayerIds());

        broadcastEvent(sessionId, new GameEvent(GameEvent.EventType.PLAYER_JOINED, sessionId,
                Map.of("playerId", playerId, "playerName", playerName)));

        return new JoinSessionResponse(sessionId, session.getName(), playerId, playerName);
    }

    /**
     * Initializes an existing player's empire if it was never set up.
     * Used when a player was added to the session without going through joinSession's setupPlayer flow.
     *
     * @param playerId the player ID to initialize
     * @param sessionId the session ID
     * @return the empire ID that was assigned
     */
    @Transactional
    public Map<String, Object> initializePlayerEmpire(Long playerId, String sessionId) {
        GameSession session = sessions.get(sessionId);
        if (session == null) {
            log.warn("Session not found: {}", sessionId);
            throw new NoSuchElementException("Session not found: " + sessionId);
        }

        Game game = (Game) session.getGame();
        session.getLock().writeLock().lock();
        try {
            // Find the existing player in the game
            com.yisy.modules.doramcu.hex.Player existingPlayer = game.getPlayer(playerId.intValue());
            if (existingPlayer == null) {
                log.warn("Player {} not found in game for session {}", playerId, sessionId);
                throw new NoSuchElementException("Player not found in game: " + playerId);
            }

            Integer existingEmpireId = existingPlayer.getEmpireId();
            if (existingEmpireId != null && existingEmpireId > 0) {
                Empire existingEmpire = game.getEmpire(existingEmpireId);
                if (existingEmpire != null) {
                    log.info("Player {} already has empire {} ({})", playerId, existingEmpireId, existingEmpire.getName());
                    return Map.of(
                            "success", true,
                            "message", "Player already has an empire",
                            "empireId", existingEmpireId,
                            "empireName", existingEmpire.getName()
                    );
                }
            }

            // Set up the complete player: empire, home system, fleets, navy sync
            String playerName = existingPlayer.getName();
            Game.PlayerSetupResult setup = game.setupPlayer(playerId.intValue(), playerName);
            log.info("Initialized player {} (id={}) with empire id={}", playerName, playerId, setup.getEmpire().getId());

            return Map.of(
                    "success", true,
                    "message", "Player empire initialized",
                    "playerId", playerId,
                    "playerName", playerName,
                    "empireId", setup.getEmpire().getId(),
                    "empireName", setup.getEmpire().getName(),
                    "fleetCount", setup.getStartingFleets().size()
            );
        } finally {
            session.getLock().writeLock().unlock();
        }
    }

    @Transactional
    public void leaveSession(Long playerId, String sessionId) {
        GameSession session = sessions.get(sessionId);
        if (session == null) {
            log.warn("Session not found: {}", sessionId);
            return;
        }

        log.debug("Player {} leaving session: {}", playerId, sessionId);
        session.removePlayer(playerId);

        broadcastEvent(sessionId, new GameEvent(GameEvent.EventType.PLAYER_LEFT, sessionId,
                Map.of("playerId", playerId)));

        if (session.getPlayerIds().isEmpty()) {
            // Clean up global clock when session is removed
            globalClockService.removeClock(sessionId);
            sessions.remove(sessionId);
            // Remove persisted session when last player leaves
            if (persistedGameSessionRepository != null) {
                persistedGameSessionRepository.findBySessionId(sessionId).ifPresent(persistedGameSessionRepository::delete);
            }
            log.debug("Removed empty session: {}", sessionId);
        } else {
            // Update player list in persisted session
            persistSession(sessionId, session.getName(), session.getPlayerIds());
        }
    }

    public List<Map<String, Object>> listSessions() {
        return sessions.values().stream().map(session -> {
            Map<String, Object> info = new HashMap<>();
            info.put("sessionId", session.getSessionId());
            info.put("name", session.getName());
            info.put("playerCount", session.getPlayerIds().size());
            info.put("players", session.getPlayerIds());
            return info;
        }).collect(Collectors.toList());
    }

    /**
     * Deletes a session by ID (removes from memory and persisted storage).
     *
     * @param sessionId the session ID to delete
     * @return true if the session was found and deleted
     */
    @Transactional
    public boolean deleteSession(String sessionId) {
        log.info("Deleting session: {}", sessionId);
        boolean deleted = false;
        // Remove from in-memory sessions
        GameSession session = sessions.remove(sessionId);
        if (session != null) {
            globalClockService.removeClock(sessionId);
            deleted = true;
        }
        // Remove from persisted storage
        if (persistedGameSessionRepository != null) {
            deleted = persistedGameSessionRepository.findBySessionId(sessionId).isPresent() || deleted;
            persistedGameSessionRepository.findBySessionId(sessionId).ifPresent(persistedGameSessionRepository::delete);
        }
        if (deleted) {
            log.info("Session {} deleted successfully", sessionId);
        }
        return deleted;
    }

    /**
     * Returns the first active game session, or null if no sessions exist.
     * <p>
     * Used to provide a default session for auto-login in development mode.
     * </p>
     *
     * @return the first active session, or null if none exist
     */
    public GameSession getDefaultSession() {
        if (sessions.isEmpty()) {
            return null;
        }
        return sessions.values().iterator().next();
    }

    /**
     * Returns the session with the given ID, or null if not found.
     *
     * @param sessionId the session ID
     * @return the matching session, or null
     */
    public GameSession getSession(String sessionId) {
        return sessions.get(sessionId);
    }

    /**
     * Returns the session with the given name, or null if not found.
     *
     * @param name the session name
     * @return the matching session, or null
     */
    public GameSession getSessionByName(String name) {
        return sessions.values().stream()
                .filter(s -> name.equals(s.getName()))
                .findFirst()
                .orElse(null);
    }

    /**
     * Returns the session ID for a session that has the given galaxy loaded, or null.
     * Used by the portal travel system to find existing sessions per galaxy.
     *
     * @param galaxyName the galaxy name to match
     * @return the session ID, or null if no session has this galaxy
     */
    public String getSessionIdByGalaxyName(String galaxyName) {
        for (var entry : sessions.entrySet()) {
            GameSession session = entry.getValue();
            Game game = (Game) session.getGame();
            if (game == null) continue;
            Galaxy galaxy = game.getGalaxy(0);
            if (galaxy != null && galaxyName.equals(galaxy.getName())) {
                return entry.getKey();
            }
        }
        return null;
    }

    /**
     * Creates a new game session for a specific galaxy (used by portal travel).
     * Loads the given galaxy into the session game instead of the default first galaxy.
     *
     * @param sessionId the session ID
     * @param sessionName the session name
     * @param dbGalaxy the galaxy to load
     * @param gameType the game type for the global clock
     */
    @Transactional
    public void createSessionForGalaxy(String sessionId, String sessionName,
                                        com.yisy.modules.doramcu.model.galaxy.Galaxy dbGalaxy,
                                        com.yisy.modules.doramcu.gamecore.GameType gameType) {
        log.info("Creating session {} for galaxy '{}' (gameType: {})", sessionId, dbGalaxy.getName(), gameType);

        // Create the Game engine first
        Game game = new Game(sessionName);

        // Load the specific galaxy instead of the default
        com.yisy.modules.doramcu.hex.Galaxy inMemoryGalaxy =
                gameInitializationService.convertToInMemoryGalaxy(dbGalaxy);
        if (inMemoryGalaxy != null) {
            game.addGalaxy(inMemoryGalaxy);
            log.info("Loaded galaxy '{}' into session with {} systems",
                    inMemoryGalaxy.getName(), inMemoryGalaxy.getAllSystems().size());
        }

        // Create session with the game engine (constructor requires it)
        GameSession session = new GameSession(sessionId, sessionName, game);

        // Set up global clock
        GlobalClock clock = globalClockService.getOrCreateClock(sessionId, gameType);
        session.setGlobalClock(clock);
        sessions.put(sessionId, session);

        // Persist session metadata
        if (persistedGameSessionRepository != null) {
            PersistedGameSession pgs = new PersistedGameSession();
            pgs.setSessionId(sessionId);
            pgs.setSessionName(sessionName);
            pgs.setGalaxyName(dbGalaxy.getName());
            pgs.setGameType(gameType.name());
            pgs.setCreatedAt(LocalDateTime.now());
            pgs.setUpdatedAt(LocalDateTime.now());
            pgs.setLastActivityAt(LocalDateTime.now());
            persistedGameSessionRepository.save(pgs);
        }

        log.info("Session {} created for galaxy '{}'", sessionId, dbGalaxy.getName());
    }

    /**
     * Returns the resource pipeline summary for the given player.
     * Returns null if the session or pipeline is not found.
     */
    public ResourcePipelineSummary getPipelineSummary(Long playerId, String sessionId) {
        GameSession session = sessions.get(sessionId);
        if (session == null) {
            return null;
        }
        Game game = (Game) session.getGame();
        Player player = game.getPlayer(playerId.intValue());
        if (player == null) {
            return null;
        }
        ResourcePipelineManager pipeline = game.getPipelineManager(player.getEmpireId());
        if (pipeline == null) {
            return null;
        }
        return gameStateBuilder.buildPipelineSummary(pipeline);
    }

    /**
     * Returns detailed pipeline information for the given player.
     * Returns null if the session or pipeline is not found.
     */
    public PipelineDetailsDto getPipelineDetails(Long playerId, String sessionId) {
        GameSession session = sessions.get(sessionId);
        if (session == null) {
            return null;
        }
        Game game = (Game) session.getGame();
        Player player = game.getPlayer(playerId.intValue());
        if (player == null) {
            return null;
        }
        ResourcePipelineManager pipeline = game.getPipelineManager(player.getEmpireId());
        if (pipeline == null) {
            return null;
        }
        return gameStateBuilder.buildPipelineDetails(pipeline);
    }

    /**
     * Returns the unified economic state for the given player's empire.
     * Returns null if the session or economic state is not found.
     */
    public java.util.Map<String, Object> getEconomicState(Long playerId, String sessionId) {
        GameSession session = sessions.get(sessionId);
        if (session == null) {
            return null;
        }
        Game game = (Game) session.getGame();
        Player player = game.getPlayer(playerId.intValue());
        if (player == null) {
            return null;
        }
        com.yisy.modules.doramcu.hex.manager.EconomicState economicState = game.getEconomicState(player.getEmpireId());
        if (economicState == null) {
            return null;
        }
        return economicState.getSummary();
    }

    // ---- Transport Endpoints ----

    /**
     * Returns the transport summary for the given player's empire.
     */
    public java.util.Map<String, Object> getTransportSummary(Long playerId, String sessionId) {
        GameSession session = sessions.get(sessionId);
        if (session == null) {
            return null;
        }
        session.getLock().readLock().lock();
        try {
            Game game = (Game) session.getGame();
            Player player = game.getPlayer(playerId.intValue());
            if (player == null) {
                return null;
            }
            ResourcePipelineManager pipeline = game.getPipelineManager(player.getEmpireId());
            if (pipeline == null) {
                return null;
            }
            return pipeline.getTransportSummary();
        } finally {
            session.getLock().readLock().unlock();
        }
    }

    /**
     * Consolidates transport orders sharing the same route.
     */
    @Transactional
    public java.util.Map<String, Object> consolidateTransports(Long playerId, String sessionId) {
        GameSession session = sessions.get(sessionId);
        if (session == null) {
            throw new NoSuchElementException("Session not found: " + sessionId);
        }
        session.getLock().writeLock().lock();
        try {
            Game game = (Game) session.getGame();
            Player player = game.getPlayer(playerId.intValue());
            if (player == null) {
                throw new NoSuchElementException("Player not found: " + playerId);
            }
            ResourcePipelineManager pipeline = game.getPipelineManager(player.getEmpireId());
            if (pipeline == null) {
                throw new NoSuchElementException("Pipeline not found");
            }
            int consolidations = pipeline.consolidateTransports();
            java.util.Map<String, Object> result = new java.util.LinkedHashMap<>();
            result.put("consolidations", consolidations);
            result.put("message", consolidations > 0
                    ? consolidations + " transport(s) consolidated"
                    : "No consolidations possible");
            return result;
        } finally {
            session.getLock().writeLock().unlock();
        }
    }

    /**
     * Optimizes transport routes by reassigning fleets based on priority.
     */
    @Transactional
    public java.util.Map<String, Object> optimizeTransports(Long playerId, String sessionId) {
        GameSession session = sessions.get(sessionId);
        if (session == null) {
            throw new NoSuchElementException("Session not found: " + sessionId);
        }
        session.getLock().writeLock().lock();
        try {
            Game game = (Game) session.getGame();
            Player player = game.getPlayer(playerId.intValue());
            if (player == null) {
                throw new NoSuchElementException("Player not found: " + playerId);
            }
            ResourcePipelineManager pipeline = game.getPipelineManager(player.getEmpireId());
            if (pipeline == null) {
                throw new NoSuchElementException("Pipeline not found");
            }
            // Collect all fleet IDs as available for transport assignment
            java.util.List<Integer> availableFleets = new java.util.ArrayList<>(player.getOwnedFleetIds());
            pipeline.optimizeTransportRoutes(availableFleets);
            java.util.Map<String, Object> result = new java.util.LinkedHashMap<>();
            result.put("optimized", true);
            result.put("availableFleets", availableFleets.size());
            return result;
        } finally {
            session.getLock().writeLock().unlock();
        }
    }

    /**
     * Cancels a transport order by ID.
     */
    @Transactional
    public java.util.Map<String, Object> cancelTransport(Long playerId, String sessionId, int orderId) {
        GameSession session = sessions.get(sessionId);
        if (session == null) {
            throw new NoSuchElementException("Session not found: " + sessionId);
        }
        session.getLock().writeLock().lock();
        try {
            Game game = (Game) session.getGame();
            Player player = game.getPlayer(playerId.intValue());
            if (player == null) {
                throw new NoSuchElementException("Player not found: " + playerId);
            }
            ResourcePipelineManager pipeline = game.getPipelineManager(player.getEmpireId());
            if (pipeline == null) {
                throw new NoSuchElementException("Pipeline not found");
            }
            boolean cancelled = pipeline.cancelTransportOrder(orderId);
            java.util.Map<String, Object> result = new java.util.LinkedHashMap<>();
            result.put("cancelled", cancelled);
            if (!cancelled) {
                result.put("error", "Transport order not found or already completed");
            }
            return result;
        } finally {
            session.getLock().writeLock().unlock();
        }
    }

    /**
     * Returns the market board summary for the current session.
     * Returns null if the session is not found.
     */
    public java.util.Map<String, Object> getMarketBoard(Long playerId, String sessionId) {
        GameSession session = sessions.get(sessionId);
        if (session == null) {
            return null;
        }
        Game game = (Game) session.getGame();
        com.yisy.modules.doramcu.hex.manager.MarketBoard marketBoard = game.getTradeManager().getMarketBoard();
        return marketBoard.getSummary();
    }

    /**
     * Creates a trade listing on the market board.
     * Returns the created listing or an error map if creation failed.
     */
    @Transactional
    public java.util.Map<String, Object> createTradeListing(Long playerId, String sessionId,
                                                              String type, String commodity,
                                                              int units, int pricePerUnit, int turnsToLive) {
        GameSession session = sessions.get(sessionId);
        if (session == null) {
            throw new NoSuchElementException("Session not found: " + sessionId);
        }
        Game game = (Game) session.getGame();
        Player player = game.getPlayer(playerId.intValue());
        if (player == null) {
            throw new NoSuchElementException("Player not found: " + playerId);
        }

        com.yisy.modules.doramcu.hex.manager.TradeListing.ListingType listingType;
        if ("BUY".equalsIgnoreCase(type)) {
            listingType = com.yisy.modules.doramcu.hex.manager.TradeListing.ListingType.BUY;
        } else {
            listingType = com.yisy.modules.doramcu.hex.manager.TradeListing.ListingType.SELL;
        }

        com.yisy.modules.doramcu.traveller.Commodity comm = resolveCommodity(commodity);
        if (comm == null) {
            java.util.Map<String, Object> error = new java.util.LinkedHashMap<>();
            error.put("error", "Unknown commodity: " + commodity);
            return error;
        }

        com.yisy.modules.doramcu.hex.manager.MarketBoard marketBoard = game.getTradeManager().getMarketBoard();
        com.yisy.modules.doramcu.hex.manager.TradeListing listing = marketBoard.createListing(
                player.getEmpireId(), listingType, comm, units, pricePerUnit, turnsToLive);

        if (listing == null) {
            java.util.Map<String, Object> error = new java.util.LinkedHashMap<>();
            error.put("error", "Empire has reached maximum listing limit");
            return error;
        }

        java.util.Map<String, Object> result = new java.util.LinkedHashMap<>();
        result.put("id", listing.getId());
        result.put("empireId", listing.getEmpireId());
        result.put("type", listing.getType());
        result.put("commodity", listing.getCommodity().getName());
        result.put("units", listing.getUnits());
        result.put("pricePerUnit", listing.getPricePerUnit());
        result.put("turnsToLive", listing.getTurnsToLive());
        return result;
    }

    /**
     * Cancels a trade listing by ID.
     */
    @Transactional
    public java.util.Map<String, Object> cancelTradeListing(Long playerId, String sessionId, int listingId) {
        GameSession session = sessions.get(sessionId);
        if (session == null) {
            throw new NoSuchElementException("Session not found: " + sessionId);
        }
        Game game = (Game) session.getGame();
        Player player = game.getPlayer(playerId.intValue());
        if (player == null) {
            throw new NoSuchElementException("Player not found: " + playerId);
        }

        com.yisy.modules.doramcu.hex.manager.MarketBoard marketBoard = game.getTradeManager().getMarketBoard();
        boolean cancelled = marketBoard.cancelListing(listingId, player.getEmpireId());

        java.util.Map<String, Object> result = new java.util.LinkedHashMap<>();
        result.put("cancelled", cancelled);
        if (!cancelled) {
            result.put("error", "Listing not found or does not belong to this empire");
        }
        return result;
    }

    /**
     * Returns the technology tree summary for the given player's empire.
     */
    public java.util.Map<String, Object> getTechnologyTree(Long playerId, String sessionId) {
        GameSession session = sessions.get(sessionId);
        if (session == null) {
            return null;
        }
        Game game = (Game) session.getGame();
        Player player = game.getPlayer(playerId.intValue());
        if (player == null) {
            return null;
        }
        com.yisy.modules.doramcu.hex.manager.EconomicState economicState = game.getEconomicState(player.getEmpireId());
        if (economicState == null) {
            return null;
        }
        return economicState.getTechnologyTree().getSummary();
    }

    /**
     * Researches a technology for the given player's empire.
     */
    @Transactional
    public java.util.Map<String, Object> researchTechnology(Long playerId, String sessionId, String technologyName) {
        GameSession session = sessions.get(sessionId);
        if (session == null) {
            throw new NoSuchElementException("Session not found: " + sessionId);
        }
        Game game = (Game) session.getGame();
        Player player = game.getPlayer(playerId.intValue());
        if (player == null) {
            throw new NoSuchElementException("Player not found: " + playerId);
        }

        com.yisy.modules.doramcu.hex.manager.EconomicState economicState = game.getEconomicState(player.getEmpireId());
        if (economicState == null) {
            throw new NoSuchElementException("Economic state not found");
        }

        com.yisy.modules.doramcu.hex.manager.TechnologyUpgrade tech = resolveTechnology(technologyName);
        if (tech == null) {
            java.util.Map<String, Object> error = new java.util.LinkedHashMap<>();
            error.put("error", "Unknown technology: " + technologyName);
            return error;
        }

        com.yisy.modules.doramcu.hex.manager.TechnologyTree tree = economicState.getTechnologyTree();
        boolean success = tree.research(tech);

        java.util.Map<String, Object> result = new java.util.LinkedHashMap<>();
        result.put("success", success);
        result.put("technology", tech.getDisplayName());
        result.put("level", tree.getLevel(tech));
        result.put("researchPoints", tree.getResearchPoints());
        if (!success) {
            result.put("error", "Cannot research: prerequisites not met or insufficient research points");
            result.put("requiredCost", tree.getResearchCost(tech));
        }
        return result;
    }

    /**
     * Resolves a commodity by name (case-insensitive).
     */
    private com.yisy.modules.doramcu.traveller.Commodity resolveCommodity(String name) {
        if (name == null) return null;
        for (com.yisy.modules.doramcu.traveller.Commodity c : com.yisy.modules.doramcu.traveller.Commodity.values()) {
            if (c.name().equalsIgnoreCase(name) || c.getName().equalsIgnoreCase(name)) {
                return c;
            }
        }
        return null;
    }

    /**
     * Resolves a technology by name (case-insensitive).
     */
    private com.yisy.modules.doramcu.hex.manager.TechnologyUpgrade resolveTechnology(String name) {
        if (name == null) return null;
        for (com.yisy.modules.doramcu.hex.manager.TechnologyUpgrade t : com.yisy.modules.doramcu.hex.manager.TechnologyUpgrade.values()) {
            if (t.name().equalsIgnoreCase(name) || t.getDisplayName().equalsIgnoreCase(name)) {
                return t;
            }
        }
        return null;
    }

    // ---- Tax Management ----

    /**
     * Returns the tax summary for the given player's empire.
     */
    public java.util.Map<String, Object> getTaxSummary(Long playerId, String sessionId) {
        GameSession session = sessions.get(sessionId);
        if (session == null) {
            log.warn("Session not found: {}", sessionId);
            throw new NoSuchElementException("Session not found: " + sessionId);
        }
        updateSessionActivity(sessionId);
        session.getLock().readLock().lock();
        try {
            Game game = (Game) session.getGame();
            Player player = game.getPlayer(playerId.intValue());
            if (player == null) {
                throw new NoSuchElementException("Player not found: " + playerId);
            }
            int empireId = player.getEmpireId();
            com.yisy.modules.doramcu.hex.manager.TaxManager taxManager = game.getTaxManager();
            if (taxManager == null) {
                return new java.util.LinkedHashMap<>();
            }
            return taxManager.getTaxSummary(empireId);
        } finally {
            session.getLock().readLock().unlock();
        }
    }

    /**
     * Sets the tax policy for the given player's empire.
     */
    @Transactional
    public java.util.Map<String, Object> setTaxPolicy(Long playerId, String sessionId, String policyName) {
        GameSession session = sessions.get(sessionId);
        if (session == null) {
            log.warn("Session not found: {}", sessionId);
            throw new NoSuchElementException("Session not found: " + sessionId);
        }
        updateSessionActivity(sessionId);
        session.getLock().writeLock().lock();
        try {
            Game game = (Game) session.getGame();
            Player player = game.getPlayer(playerId.intValue());
            if (player == null) {
                throw new NoSuchElementException("Player not found: " + playerId);
            }
            int empireId = player.getEmpireId();
            com.yisy.modules.doramcu.hex.manager.TaxManager taxManager = game.getTaxManager();
            if (taxManager == null) {
                java.util.Map<String, Object> error = new java.util.LinkedHashMap<>();
                error.put("success", false);
                error.put("error", "TaxManager not available");
                return error;
            }
            com.yisy.modules.doramcu.hex.manager.TaxManager.TaxPolicy policy = resolveTaxPolicy(policyName);
            if (policy == null) {
                java.util.Map<String, Object> error = new java.util.LinkedHashMap<>();
                error.put("success", false);
                error.put("error", "Invalid tax policy: " + policyName);
                error.put("validPolicies", java.util.Arrays.toString(
                        com.yisy.modules.doramcu.hex.manager.TaxManager.TaxPolicy.values()));
                return error;
            }
            taxManager.setTaxPolicy(empireId, policy);
            java.util.Map<String, Object> result = new java.util.LinkedHashMap<>();
            result.put("success", true);
            result.put("empireId", empireId);
            result.put("taxPolicy", policy.name());
            result.put("taxPolicyDisplay", policy.getDisplayName());
            result.put("taxRate", policy.getTaxRate());
            result.put("collectionEfficiency", policy.getCollectionEfficiency());
            return result;
        } finally {
            session.getLock().writeLock().unlock();
        }
    }

    /**
     * Resolves a tax policy by name (case-insensitive).
     */
    private com.yisy.modules.doramcu.hex.manager.TaxManager.TaxPolicy resolveTaxPolicy(String name) {
        if (name == null) return null;
        for (com.yisy.modules.doramcu.hex.manager.TaxManager.TaxPolicy p :
                com.yisy.modules.doramcu.hex.manager.TaxManager.TaxPolicy.values()) {
            if (p.name().equalsIgnoreCase(name)) {
                return p;
            }
        }
        return null;
    }

    // ---- Recipe Management ----

    /**
     * Returns all available recipes (processing and module assembly) for the given player's empire.
     * Includes information about which alternative recipes are unlocked via technology research.
     */
    public java.util.Map<String, Object> getRecipes(Long playerId, String sessionId) {
        GameSession session = sessions.get(sessionId);
        if (session == null) {
            log.warn("Session not found: {}", sessionId);
            throw new NoSuchElementException("Session not found: " + sessionId);
        }
        updateSessionActivity(sessionId);
        session.getLock().readLock().lock();
        try {
            Game game = (Game) session.getGame();
            Player player = game.getPlayer(playerId.intValue());
            if (player == null) {
                throw new NoSuchElementException("Player not found: " + playerId);
            }
            int empireId = player.getEmpireId();

            // Get technology tree to check recipe unlocks
            com.yisy.modules.doramcu.hex.manager.TechnologyTree techTree = null;
            com.yisy.modules.doramcu.hex.manager.EconomicState econState =
                    game.getTurnManager().getEconomicState(empireId);
            if (econState != null) {
                techTree = econState.getTechnologyTree();
            }
            boolean advancedProcessing = techTree != null && techTree.hasAdvancedProcessing();
            boolean advancedAssembly = techTree != null && techTree.hasAdvancedAssembly();

            java.util.Map<String, Object> result = new java.util.LinkedHashMap<>();

            // Processing recipes
            java.util.List<java.util.Map<String, Object>> processingRecipes = new java.util.ArrayList<>();
            for (com.yisy.modules.doramcu.hex.RefinedResourceType type :
                    com.yisy.modules.doramcu.hex.RefinedResourceType.values()) {
                com.yisy.modules.doramcu.hex.ProcessingRecipe standard =
                        com.yisy.modules.doramcu.hex.ProcessingRecipe.getStandardRecipe(type);
                com.yisy.modules.doramcu.hex.ProcessingRecipe alternative =
                        com.yisy.modules.doramcu.hex.ProcessingRecipe.getAlternativeRecipe(type);

                if (standard != null) {
                    java.util.Map<String, Object> recipe = new java.util.LinkedHashMap<>();
                    recipe.put("output", type.name());
                    recipe.put("outputDisplay", type.getDisplayName());
                    recipe.put("inputs", standard.getInputs());
                    recipe.put("processingTimeTurns", standard.getProcessingTimeTurns());
                    recipe.put("isStandard", true);
                    recipe.put("isUnlocked", true);

                    if (alternative != null) {
                        java.util.Map<String, Object> alt = new java.util.LinkedHashMap<>();
                        alt.put("inputs", alternative.getInputs());
                        alt.put("processingTimeTurns", alternative.getProcessingTimeTurns());
                        alt.put("isUnlocked", advancedProcessing);
                        alt.put("unlockRequirement", "ADVANCED_PROCESSING");
                        recipe.put("alternative", alt);
                    }
                    processingRecipes.add(recipe);
                }
            }
            result.put("processingRecipes", processingRecipes);

            // Module assembly recipes
            java.util.List<java.util.Map<String, Object>> moduleRecipes = new java.util.ArrayList<>();
            for (com.yisy.modules.doramcu.hex.ConstructionModuleType type :
                    com.yisy.modules.doramcu.hex.ConstructionModuleType.values()) {
                java.util.Map<String, Object> recipe = new java.util.LinkedHashMap<>();
                recipe.put("module", type.name());
                recipe.put("moduleDisplay", type.getDisplayName());
                recipe.put("standardRecipe", type.getStandardRecipe());
                recipe.put("standardTimeTurns", type.getAssemblyTimeTurns());
                recipe.put("alternativeRecipe", type.getAlternativeRecipe());
                recipe.put("alternativeTimeTurns", type.getAlternativeAssemblyTimeTurns());
                recipe.put("alternativeUnlocked", advancedAssembly);
                recipe.put("unlockRequirement", "ADVANCED_ASSEMBLY");
                moduleRecipes.add(recipe);
            }
            result.put("moduleRecipes", moduleRecipes);

            // Unlock status
            java.util.Map<String, Object> unlocks = new java.util.LinkedHashMap<>();
            unlocks.put("advancedProcessing", advancedProcessing);
            unlocks.put("advancedAssembly", advancedAssembly);
            result.put("unlocks", unlocks);

            return result;
        } finally {
            session.getLock().readLock().unlock();
        }
    }

    // ---- Economic Dashboard ----

    /**
     * Returns the module requirements for building a ship of the given type.
     * <p>
     * Creates a preview Ship instance to calculate the module requirements
     * without actually queuing a build order.
     * </p>
     */
    public java.util.Map<String, Object> getShipModuleRequirements(Long playerId, String sessionId, String shipTypeName) {
        GameSession session = sessions.get(sessionId);
        if (session == null) {
            log.warn("Session not found: {}", sessionId);
            throw new NoSuchElementException("Session not found: " + sessionId);
        }
        updateSessionActivity(sessionId);
        session.getLock().readLock().lock();
        try {
            Game game = (Game) session.getGame();
            Player player = game.getPlayer(playerId.intValue());
            if (player == null) {
                throw new NoSuchElementException("Player not found: " + playerId);
            }

            ShipType shipType;
            try {
                shipType = ShipType.valueOf(shipTypeName.toUpperCase());
            } catch (IllegalArgumentException e) {
                java.util.Map<String, Object> error = new java.util.LinkedHashMap<>();
                error.put("error", "Invalid ship type: " + shipTypeName);
                error.put("validTypes", java.util.Arrays.toString(ShipType.values()));
                return error;
            }

            // Create a preview ship to calculate module requirements
            Ship previewShip = new Ship(0, shipType);
            com.yisy.modules.doramcu.hex.manager.ResourceManager resourceManager = game.getResourceManager();
            java.util.Map<com.yisy.modules.doramcu.hex.ConstructionModuleType, Integer> requirements =
                    resourceManager.calculateModuleRequirements(previewShip);

            // Check pipeline availability
            ResourcePipelineManager pipeline = game.getPipelineManager(player.getEmpireId());
            boolean canAfford = pipeline != null && pipeline.canBuildWithModules(requirements);

            // Build response
            java.util.Map<String, Object> result = new java.util.LinkedHashMap<>();
            result.put("shipType", shipType.name());
            result.put("shipDisplayName", shipType.name());
            result.put("buildTime", shipType.getBuildTimeTurns());
            result.put("canAfford", canAfford);

            // Module requirements as string-keyed map
            java.util.Map<String, Integer> moduleMap = new java.util.LinkedHashMap<>();
            for (java.util.Map.Entry<com.yisy.modules.doramcu.hex.ConstructionModuleType, Integer> entry : requirements.entrySet()) {
                moduleMap.put(entry.getKey().getDisplayName(), entry.getValue());
            }
            result.put("moduleRequirements", moduleMap);

            // Available modules in pipeline
            if (pipeline != null) {
                java.util.Map<String, Integer> available = new java.util.LinkedHashMap<>();
                for (com.yisy.modules.doramcu.hex.ConstructionModuleType moduleType :
                        com.yisy.modules.doramcu.hex.ConstructionModuleType.values()) {
                    int count = pipeline.getModuleStorage().getInventory().getOrDefault(moduleType, 0);
                    available.put(moduleType.getDisplayName(), count);
                }
                result.put("availableModules", available);
            }

            return result;
        } finally {
            session.getLock().readLock().unlock();
        }
    }

    /**
     * Returns the trade summary for the given player's empire.
     */
    public TradeSummaryDto getTradeSummary(Long playerId, String sessionId) {
        GameSession session = sessions.get(sessionId);
        if (session == null) {
            log.warn("Session not found: {}", sessionId);
            throw new NoSuchElementException("Session not found: " + sessionId);
        }
        updateSessionActivity(sessionId);
        session.getLock().readLock().lock();
        try {
            Game game = (Game) session.getGame();
            Player player = game.getPlayer(playerId.intValue());
            if (player == null) {
                throw new NoSuchElementException("Player not found: " + playerId);
            }
            int empireId = player.getEmpireId();
            return game.getTradeManager().getTradeSummary(empireId);
        } finally {
            session.getLock().readLock().unlock();
        }
    }

    /**
     * Returns the income projection for the given player's empire.
     */
    public IncomeProjectionDto getIncomeProjection(Long playerId, String sessionId) {
        GameSession session = sessions.get(sessionId);
        if (session == null) {
            log.warn("Session not found: {}", sessionId);
            throw new NoSuchElementException("Session not found: " + sessionId);
        }
        updateSessionActivity(sessionId);
        session.getLock().readLock().lock();
        try {
            Game game = (Game) session.getGame();
            Player player = game.getPlayer(playerId.intValue());
            if (player == null) {
                throw new NoSuchElementException("Player not found: " + playerId);
            }
            int empireId = player.getEmpireId();
            com.yisy.modules.doramcu.hex.manager.EconomicState economicState = game.getEconomicState(empireId);
            if (economicState == null) {
                throw new NoSuchElementException("Economic state not found for empire " + empireId);
            }
            return economicState.getIncomeProjection(game.getCurrentTurn());
        } finally {
            session.getLock().readLock().unlock();
        }
    }

    /**
     * Returns a comprehensive economic dashboard combining all economic data.
     */
    public java.util.Map<String, Object> getEconomicDashboard(Long playerId, String sessionId) {
        GameSession session = sessions.get(sessionId);
        if (session == null) {
            log.warn("Session not found: {}", sessionId);
            throw new NoSuchElementException("Session not found: " + sessionId);
        }
        updateSessionActivity(sessionId);
        session.getLock().readLock().lock();
        try {
            Game game = (Game) session.getGame();
            Player player = game.getPlayer(playerId.intValue());
            if (player == null) {
                throw new NoSuchElementException("Player not found: " + playerId);
            }
            int empireId = player.getEmpireId();

            java.util.Map<String, Object> dashboard = new java.util.LinkedHashMap<>();

            // Economic state summary
            com.yisy.modules.doramcu.hex.manager.EconomicState economicState = game.getEconomicState(empireId);
            if (economicState != null) {
                dashboard.put("economicState", economicState.getSummary());
            }

            // Trade summary
            dashboard.put("trade", game.getTradeManager().getTradeSummary(empireId));

            // Income projection
            if (economicState != null) {
                dashboard.put("incomeProjection", economicState.getIncomeProjection(game.getCurrentTurn()));
            }

            // Pipeline summary
            ResourcePipelineManager pipeline = game.getPipelineManager(empireId);
            if (pipeline != null) {
                dashboard.put("pipeline", gameStateBuilder.buildPipelineSummary(pipeline));
            }

            // Tax summary
            com.yisy.modules.doramcu.hex.manager.TaxManager taxManager = game.getTaxManager();
            if (taxManager != null) {
                dashboard.put("tax", taxManager.getTaxSummary(empireId));
            }

            // Technology summary
            if (economicState != null) {
                dashboard.put("technology", economicState.getTechnologyTree().getSummary());
            }

            // Market board summary
            dashboard.put("market", game.getTradeManager().getMarketBoard().getSummary());

            // Current turn
            dashboard.put("currentTurn", game.getCurrentTurn());

            return dashboard;
        } finally {
            session.getLock().readLock().unlock();
        }
    }

    public GameStateResponse getGameState(Long playerId, String sessionId) {
        return getGameState(playerId, sessionId, false);
    }

    public GameStateResponse getGameState(Long playerId, String sessionId, boolean summary) {
        GameSession session = sessions.get(sessionId);
        if (session == null) {
            // Try to restore from database if session exists there
            log.info("Session {} not in memory, attempting to restore from database", sessionId);
            if (persistedGameSessionRepository != null) {
                Optional<PersistedGameSession> persisted = persistedGameSessionRepository.findBySessionId(sessionId);
                if (persisted.isPresent() && persisted.get().getGameState() != null && !persisted.get().getGameState().isBlank()) {
                    try {
                        boolean restored = restoreSession(sessionId);
                        if (restored) {
                            log.info("Successfully restored session {} from database", sessionId);
                            session = sessions.get(sessionId);
                        } else {
                            log.warn("Failed to restore session {} from database", sessionId);
                            throw new NoSuchElementException("Session not found: " + sessionId);
                        }
                    } catch (Exception e) {
                        log.error("Exception restoring session {}: {}", sessionId, e.getMessage(), e);
                        throw new NoSuchElementException("Session not found: " + sessionId);
                    }
                } else {
                    log.warn("Session {} not found in memory or database", sessionId);
                    throw new NoSuchElementException("Session not found: " + sessionId);
                }
            } else {
                log.warn("Session {} not found: {}", sessionId, sessionId);
                throw new NoSuchElementException("Session not found: " + sessionId);
            }
        }

        updateSessionActivity(sessionId);
        session.getLock().readLock().lock();
        try {
            return gameStateBuilder.buildGameStateResponse(session, playerId, summary);
        } catch (Exception e) {
            session.getLock().readLock().unlock();
            log.error("Failed to build game state response for session {}: {}", sessionId, e.getMessage(), e);
            throw new RuntimeException("Failed to build game state: " + e.getMessage(), e);
        } finally {
            // Only unlock if not already unlocked in the catch block
            try {
                session.getLock().readLock().unlock();
            } catch (IllegalMonitorStateException ignore) {
                // Already unlocked in catch block
            }
        }
    }

    // ---- Delegated action methods ----

    @Transactional
    public GameStateResponse moveFleet(Long playerId, String sessionId, MoveFleetRequest request) {
        GameSession session = sessions.get(sessionId);
        if (session == null) {
            throw new NoSuchElementException("Session not found: " + sessionId);
        }
        GameStateResponse response = sessionActionHandler.moveFleet(session, playerId, request);
        broadcastEvent(sessionId, new GameEvent(GameEvent.EventType.ACTION_RESULT, sessionId,
                Map.of("action", "MOVE_FLEET", "playerId", playerId, "fleetId", request.getFleetId(),
                        "targetRow", request.getTargetRow(), "targetCol", request.getTargetCol(),
                        "solarSystemId", request.getSolarSystemId() != null ? request.getSolarSystemId() : "current")));
        logAction(sessionId, playerId, GameActionLog.ActionType.MOVE_FLEET, getGame(sessionId).getCurrentTurn(),
                Map.of("fleetId", request.getFleetId(), "targetRow", request.getTargetRow(),
                        "targetCol", request.getTargetCol()));
        return response;
    }

    @Transactional
    public GameStateResponse buildShip(Long playerId, String sessionId, BuildShipRequest request) {
        GameSession session = sessions.get(sessionId);
        if (session == null) {
            throw new NoSuchElementException("Session not found: " + sessionId);
        }
        GameStateResponse response = sessionActionHandler.buildShip(session, playerId, request);
        broadcastEvent(sessionId, new GameEvent(GameEvent.EventType.ACTION_RESULT, sessionId,
                Map.of("action", "BUILD_SHIP", "playerId", playerId, "shipType", request.getShipType(),
                        "targetFleetId", request.getTargetFleetId())));
        logAction(sessionId, playerId, GameActionLog.ActionType.BUILD_SHIP, getGame(sessionId).getCurrentTurn(),
                Map.of("shipType", request.getShipType(), "targetFleetId", request.getTargetFleetId()));
        return response;
    }

    @Transactional
    public GameStateResponse engageCombat(Long playerId, String sessionId, EngageCombatRequest request) {
        GameSession session = sessions.get(sessionId);
        if (session == null) {
            throw new NoSuchElementException("Session not found: " + sessionId);
        }
        GameStateResponse response = sessionActionHandler.engageCombat(session, playerId, request);
        broadcastEvent(sessionId, new GameEvent(GameEvent.EventType.ACTION_RESULT, sessionId,
                Map.of("action", "ENGAGE_COMBAT", "playerId", playerId,
                        "attackerFleetId", request.getAttackerFleetId(),
                        "defenderFleetId", request.getDefenderFleetId())));
        logAction(sessionId, playerId, GameActionLog.ActionType.ENGAGE_COMBAT, getGame(sessionId).getCurrentTurn(),
                Map.of("attackerFleetId", request.getAttackerFleetId(),
                        "defenderFleetId", request.getDefenderFleetId()));
        return response;
    }

    @Transactional
    public GameStateResponse advanceTurn(Long playerId, String sessionId) {
        GameSession session = sessions.get(sessionId);
        if (session == null) {
            throw new NoSuchElementException("Session not found: " + sessionId);
        }
        GameStateResponse response = sessionActionHandler.advanceTurn(session, playerId);
        Game game = (Game) session.getGame();
        broadcastEvent(sessionId, new GameEvent(GameEvent.EventType.TURN_CHANGED, sessionId,
                Map.of("turn", game.getCurrentTurn(), "autoAdvanced", false)));
        logAction(sessionId, playerId, GameActionLog.ActionType.ADVANCE_TURN, game.getCurrentTurn(), Collections.emptyMap());
        return response;
    }

    public void broadcastEvent(String sessionId, GameEvent event) {
        GameSession session = sessions.get(sessionId);
        if (session == null) {
            return;
        }

        for (SseEmitter emitter : session.getEmitters().values()) {
            try {
                emitter.send(SseEmitter.event()
                        .name("game-event")
                        .data(event));
            } catch (IOException e) {
                log.warn("Failed to send event to emitter in session: {}", sessionId, e);
            }
        }
    }

    public GameSession getGameSession(String sessionId) {
        return sessions.get(sessionId);
    }

    /**
     * Returns all active game sessions.
     */
    public Map<String, GameSession> getAllSessions() {
        return sessions;
    }

    /**
     * Persist session metadata and full game state to the database.
     * Creates or updates the persisted session record.
     * No-op if the repository is not available (e.g., in unit tests).
     */
    @Transactional
    void persistSession(String sessionId, String sessionName, Set<Long> playerIds) {
        if (persistedGameSessionRepository == null) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();

        final String[] gameStateJson = new String[1];
        GameSession session = sessions.get(sessionId);
        if (session != null) {
            try {
                Map<String, Object> gameState = ((Game) session.getGame()).serializeGameState();
                gameStateJson[0] = objectMapper.writeValueAsString(gameState);
            } catch (JsonProcessingException e) {
                log.warn("Failed to serialize game state for session: {}", sessionId, e);
            }
        }

        persistedGameSessionRepository.findBySessionId(sessionId).ifPresentOrElse(
                persisted -> {
                    persisted.setSessionName(sessionName);
                    persisted.setPlayerIds(playerIds.toString());
                    persisted.setGameState(gameStateJson[0]);
                    persisted.setUpdatedAt(now);
                    persisted.setLastActivityAt(now);
                    persistedGameSessionRepository.save(persisted);
                },
                () -> {
                    PersistedGameSession persisted = new PersistedGameSession();
                    persisted.setSessionId(sessionId);
                    persisted.setSessionName(sessionName);
                    persisted.setPlayerIds(playerIds.toString());
                    persisted.setGameState(gameStateJson[0]);
                    persisted.setCreatedAt(now);
                    persisted.setUpdatedAt(now);
                    persisted.setLastActivityAt(now);
                    persistedGameSessionRepository.save(persisted);
                }
        );
    }

    /**
     * Update the last activity timestamp for a session in the database.
     * No-op if the repository is not available (e.g., in unit tests).
     */
    @Transactional
    void updateSessionActivity(String sessionId) {
        if (persistedGameSessionRepository == null) {
            return;
        }
        persistedGameSessionRepository.findBySessionId(sessionId).ifPresent(persisted -> {
            persisted.setLastActivityAt(LocalDateTime.now());
            persisted.setUpdatedAt(LocalDateTime.now());
            persistedGameSessionRepository.save(persisted);
        });
    }

    /**
     * Loads the first available galaxy from the database into the given Game instance.
     * Returns the game type of the loaded galaxy (defaults to DORAMCU_4X if not set).
     * Shared by createDefaultSession() and createSession() to avoid duplication.
     */
    private GameType loadGalaxyToGame(Game game, String sessionName) {
        List<com.yisy.modules.doramcu.model.galaxy.Galaxy> dbGalaxies = galaxyRepository.findAll();
        if (!dbGalaxies.isEmpty()) {
            com.yisy.modules.doramcu.model.galaxy.Galaxy dbGalaxy = dbGalaxies.get(0);
            com.yisy.modules.doramcu.hex.Galaxy inMemoryGalaxy =
                    gameInitializationService.convertToInMemoryGalaxy(dbGalaxy);
            if (inMemoryGalaxy != null) {
                game.addGalaxy(inMemoryGalaxy);
                log.info("Loaded galaxy '{}' into game session with {} systems",
                        inMemoryGalaxy.getName(), inMemoryGalaxy.getAllSystems().size());
            }
            // Resolve game type from the loaded galaxy
            if (dbGalaxy.getGameType() != null && !dbGalaxy.getGameType().isBlank()) {
                try {
                    return GameType.valueOf(dbGalaxy.getGameType());
                } catch (IllegalArgumentException e) {
                    log.warn("Unknown game type '{}' in galaxy '{}', defaulting to DORAMCU_4X", dbGalaxy.getGameType(), dbGalaxy.getName());
                }
            }
        } else {
            log.warn("No galaxy found in database for session {}", sessionName);
        }
        return GameType.DORAMCU_4X;
    }

    @Transactional
    public Optional<PersistedGameSession> findPersistedSession() {
        if (persistedGameSessionRepository == null) {
            return Optional.empty();
        }
        List<PersistedGameSession> all = persistedGameSessionRepository.findAll();
        if (all.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(all.get(0));
    }

    /**
     * Registers empires in the default game session's Game.
     * Called after galaxy creation to make empires available for component lookup.
     *
     * @param empires the map of empire ID to Empire instance
     */
    public void registerEmpires(Map<Integer, Empire> empires) {
        GameSession defaultSession = getDefaultSession();
        if (defaultSession != null && defaultSession.getGame() != null) {
            for (Empire empire : empires.values()) {
                ((Game) defaultSession.getGame()).registerEmpire(empire);
            }
            log.info("Registered {} empires in game session {}", empires.size(), defaultSession.getSessionId());
        }
    }

    /**
     * Updates a sector name in all in-memory game sessions that have a matching galaxy.
     * This keeps the in-memory state consistent after a database rename.
     *
     * @param galaxyName the galaxy name to match
     * @param row the sector row
     * @param col the sector column
     * @param name the new name
     */
    public void updateSectorNameInMemory(String galaxyName, int row, int col, String name) {
        for (GameSession session : sessions.values()) {
            Game game = (Game) session.getGame();
            if (game == null) continue;
            Galaxy galaxy = game.getGalaxy(0);
            if (galaxy == null || !galaxyName.equals(galaxy.getName())) continue;
            Galaxy.Sector sector = galaxy.getSector(row, col);
            if (sector != null) {
                sector.setName(name);
                log.info("Updated in-memory sector name at ({}, {}) to '{}' in galaxy '{}'", row, col, name, galaxyName);
            }
        }
    }

    /**
     * Updates a subsector name in all in-memory game sessions that have a matching galaxy.
     * This keeps the in-memory state consistent after a database rename.
     *
     * @param galaxyName the galaxy name to match
     * @param row the sector row
     * @param col the sector column
     * @param subRow the subsector row
     * @param subCol the subsector column
     * @param name the new name
     */
    public void updateSubSectorNameInMemory(String galaxyName, int row, int col, int subRow, int subCol, String name) {
        for (GameSession session : sessions.values()) {
            Game game = (Game) session.getGame();
            if (game == null) continue;
            Galaxy galaxy = game.getGalaxy(0);
            if (galaxy == null || !galaxyName.equals(galaxy.getName())) continue;
            Galaxy.Sector sector = galaxy.getSector(row, col);
            if (sector != null) {
                com.yisy.modules.doramcu.hex.SubSector subSector = sector.getSubSector(subRow, subCol);
                if (subSector != null) {
                    subSector.setName(name);
                    log.info("Updated in-memory subsector name at ({},{},{},{}) to '{}' in galaxy '{}'",
                            row, col, subRow, subCol, name, galaxyName);
                }
            }
        }
    }

    // ---- Delegated Empire Action Methods ----

    @Transactional
    public GameStateResponse buildStation(Long playerId, String sessionId, BuildStationRequest request) {
        GameSession session = sessions.get(sessionId);
        if (session == null) {
            throw new NoSuchElementException("Session not found: " + sessionId);
        }
        GameStateResponse response = sessionActionHandler.buildStation(session, playerId, request);
        broadcastEvent(sessionId, new GameEvent(GameEvent.EventType.ACTION_RESULT, sessionId,
                Map.of("action", "BUILD_STATION", "playerId", playerId, "stationType", request.getStationType())));
        logAction(sessionId, playerId, GameActionLog.ActionType.BUILD_STATION, getGame(sessionId).getCurrentTurn(),
                Map.of("stationType", request.getStationType()));
        return response;
    }

    @Transactional
    public GameStateResponse buildFacility(Long playerId, String sessionId, BuildFacilityRequest request) {
        GameSession session = sessions.get(sessionId);
        if (session == null) {
            throw new NoSuchElementException("Session not found: " + sessionId);
        }
        GameStateResponse response = sessionActionHandler.buildFacility(session, playerId, request);
        broadcastEvent(sessionId, new GameEvent(GameEvent.EventType.ACTION_RESULT, sessionId,
                Map.of("action", "BUILD_FACILITY", "playerId", playerId, "facilityType", request.getFacilityType(),
                        "planetId", request.getPlanetId())));
        logAction(sessionId, playerId, GameActionLog.ActionType.BUILD_FACILITY, getGame(sessionId).getCurrentTurn(),
                Map.of("facilityType", request.getFacilityType(), "planetId", request.getPlanetId()));
        return response;
    }

    @Transactional
    public GameStateResponse createFleet(Long playerId, String sessionId, CreateFleetRequest request) {
        GameSession session = sessions.get(sessionId);
        if (session == null) {
            throw new NoSuchElementException("Session not found: " + sessionId);
        }
        GameStateResponse response = sessionActionHandler.createFleet(session, playerId, request);
        broadcastEvent(sessionId, new GameEvent(GameEvent.EventType.ACTION_RESULT, sessionId,
                Map.of("action", "CREATE_FLEET", "playerId", playerId, "fleetName", request.getFleetName())));
        logAction(sessionId, playerId, GameActionLog.ActionType.CREATE_FLEET, getGame(sessionId).getCurrentTurn(),
                Map.of("fleetName", request.getFleetName()));
        return response;
    }

    @Transactional
    public GameStateResponse colonizePlanet(Long playerId, String sessionId, ColonizeRequest request) {
        GameSession session = sessions.get(sessionId);
        if (session == null) {
            throw new NoSuchElementException("Session not found: " + sessionId);
        }
        GameStateResponse response = sessionActionHandler.colonizePlanet(session, playerId, request);
        broadcastEvent(sessionId, new GameEvent(GameEvent.EventType.ACTION_RESULT, sessionId,
                Map.of("action", "COLONIZE", "playerId", playerId, "planetId", request.getPlanetId())));
        logAction(sessionId, playerId, GameActionLog.ActionType.COLONIZE_PLANET, getGame(sessionId).getCurrentTurn(),
                Map.of("planetId", request.getPlanetId()));
        return response;
    }

    public List<BattleSummaryDto> getActiveBattles(Long playerId, String sessionId) {
        GameSession session = sessions.get(sessionId);
        if (session == null) {
            return new ArrayList<>();
        }
        return gameStateBuilder.buildActiveBattles(session);
    }

    @Transactional
    public GameStateResponse reinforceBattle(Long playerId, String sessionId, int battleId, ReinforceBattleRequest request) {
        GameSession session = sessions.get(sessionId);
        if (session == null) {
            throw new NoSuchElementException("Session not found: " + sessionId);
        }
        GameStateResponse response = sessionActionHandler.reinforceBattle(session, playerId, battleId, request);
        broadcastEvent(sessionId, new GameEvent(GameEvent.EventType.ACTION_RESULT, sessionId,
                Map.of("action", "REINFORCE_BATTLE", "playerId", playerId, "battleId", battleId,
                        "fleetId", request.getFleetId())));
        logAction(sessionId, playerId, GameActionLog.ActionType.REINFORCE_BATTLE, getGame(sessionId).getCurrentTurn(),
                Map.of("battleId", battleId, "fleetId", request.getFleetId()));
        return response;
    }

    @Transactional
    public GameStateResponse retreatBattle(Long playerId, String sessionId, int battleId) {
        GameSession session = sessions.get(sessionId);
        if (session == null) {
            throw new NoSuchElementException("Session not found: " + sessionId);
        }
        GameStateResponse response = sessionActionHandler.retreatBattle(session, playerId, battleId);
        broadcastEvent(sessionId, new GameEvent(GameEvent.EventType.ACTION_RESULT, sessionId,
                Map.of("action", "RETREAT_BATTLE", "playerId", playerId, "battleId", battleId)));
        logAction(sessionId, playerId, GameActionLog.ActionType.RETREAT_BATTLE, getGame(sessionId).getCurrentTurn(),
                Map.of("battleId", battleId));
        return response;
    }

    /**
     * Fetch full system details from the database for the system detail view.
     * Returns all celestial bodies with names, positions, and ownership info.
     */
    public java.util.Map<String, Object> getSystemDetails(Long playerId, String sessionId, int systemId) {
        GameSession session = sessions.get(sessionId);
        if (session == null) {
            return null;
        }

        return gameStateBuilder.buildSystemDetails(systemId, (Game) session.getGame());
    }

    /**
     * Periodically persists all active game sessions to the database.
     * Runs every 5 minutes to prevent data loss on server restart.
     */
    @org.springframework.scheduling.annotation.Scheduled(fixedRateString = "${gamesession.persist-interval-ms:300000}")
    public void persistAllSessions() {
        if (sessions.isEmpty()) {
            return;
        }

        int persisted = 0;
        for (GameSession session : sessions.values()) {
            try {
                persistSession(session.getSessionId(), session.getName(), session.getPlayerIds());
                persisted++;
            } catch (Exception e) {
                log.warn("Failed to persist session {}: {}", session.getSessionId(), e.getMessage(), e);
            }
        }

        if (persisted > 0) {
            log.info("Periodically persisted {} game sessions", persisted);
        }
    }

    // ---- Action Logging Helper ----

    /**
     * Logs a game action for replay and analysis.
     * <p>
     * Called after each action to record the action type, player, turn number, and data.
     * </p>
     */
    private void logAction(String sessionId, Long playerId, GameActionLog.ActionType actionType,
                           int turnNumber, Map<String, Object> data) {
        Game game = getGame(sessionId);
        if (game == null) {
            return;
        }
        GameActionLog actionLog = GameActionLog.builder(actionType, sessionId)
                .playerId(playerId)
                .turnNumber(turnNumber)
                .data(data)
                .now()
                .build();
        gameReplayService.recordAction(actionLog);
    }

    /**
     * Logs a system event (no player) for replay and analysis.
     */
    private void logSystemEvent(String sessionId, GameActionLog.ActionType actionType, int turnNumber,
                                Map<String, Object> data) {
        gameReplayService.recordSystemEvent(sessionId, actionType, turnNumber, data);
    }

    /**
     * Gets the Game instance for a session, or null if not found.
     */
    private Game getGame(String sessionId) {
        GameSession session = sessions.get(sessionId);
        if (session == null || session.getGame() == null) {
            return null;
        }
        return (Game) session.getGame();
    }

    // ---- Delegated Traveller RPG Integration Methods ----

    @Transactional
    public GameStateResponse buildShipFromDesign(Long playerId, String sessionId, ShipDesignRequest request) {
        GameSession session = sessions.get(sessionId);
        if (session == null) {
            throw new NoSuchElementException("Session not found: " + sessionId);
        }
        GameStateResponse response = sessionActionHandler.buildShipFromDesign(session, playerId, request);
        broadcastEvent(sessionId, new GameEvent(GameEvent.EventType.ACTION_RESULT, sessionId,
                Map.of("action", "BUILD_FROM_DESIGN", "playerId", playerId, "shipName", request.getName())));
        logAction(sessionId, playerId, GameActionLog.ActionType.BUILD_SHIP_FROM_DESIGN, getGame(sessionId).getCurrentTurn(),
                Map.of("shipName", request.getName()));
        return response;
    }

    @Transactional
    public GameStateResponse buyCommodity(Long playerId, String sessionId, TradeRequest request) {
        GameSession session = sessions.get(sessionId);
        if (session == null) {
            throw new NoSuchElementException("Session not found: " + sessionId);
        }
        GameStateResponse response = sessionActionHandler.buyCommodity(session, playerId, request);
        broadcastEvent(sessionId, new GameEvent(GameEvent.EventType.ACTION_RESULT, sessionId,
                Map.of("action", "BUY_COMMODITY", "playerId", playerId, "commodity", request.getCommodity(),
                        "quantity", request.getQuantity())));
        logAction(sessionId, playerId, GameActionLog.ActionType.BUY_COMMODITY, getGame(sessionId).getCurrentTurn(),
                Map.of("commodity", request.getCommodity(), "quantity", request.getQuantity()));
        return response;
    }

    @Transactional
    public GameStateResponse sellCommodity(Long playerId, String sessionId, TradeRequest request) {
        GameSession session = sessions.get(sessionId);
        if (session == null) {
            throw new NoSuchElementException("Session not found: " + sessionId);
        }
        GameStateResponse response = sessionActionHandler.sellCommodity(session, playerId, request);
        broadcastEvent(sessionId, new GameEvent(GameEvent.EventType.ACTION_RESULT, sessionId,
                Map.of("action", "SELL_COMMODITY", "playerId", playerId, "commodity", request.getCommodity(),
                        "quantity", request.getQuantity())));
        logAction(sessionId, playerId, GameActionLog.ActionType.SELL_COMMODITY, getGame(sessionId).getCurrentTurn(),
                Map.of("commodity", request.getCommodity(), "quantity", request.getQuantity()));
        return response;
    }

    public List<TradeOpportunityDto> getTradeMarket(Long playerId, String sessionId, int systemId) {
        GameSession session = sessions.get(sessionId);
        if (session == null) {
            return List.of();
        }

        return gameStateBuilder.buildTradeOpportunities(systemId, (Game) session.getGame());
    }

    @Transactional
    public GameStateResponse assignCommander(Long playerId, String sessionId, int characterId) {
        GameSession session = sessions.get(sessionId);
        if (session == null) {
            throw new NoSuchElementException("Session not found: " + sessionId);
        }
        GameStateResponse response = sessionActionHandler.assignCommander(session, playerId, characterId);
        broadcastEvent(sessionId, new GameEvent(GameEvent.EventType.ACTION_RESULT, sessionId,
                Map.of("action", "ASSIGN_COMMANDER", "playerId", playerId, "characterId", characterId)));
        logAction(sessionId, playerId, GameActionLog.ActionType.ASSIGN_COMMANDER, getGame(sessionId).getCurrentTurn(),
                Map.of("characterId", characterId));
        return response;
    }
}
