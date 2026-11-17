package com.yisy.modules.doramcu.service;
import org.springframework.stereotype.Service;

import com.yisy.modules.doramcu.hex.Game;
import com.yisy.modules.doramcu.model.session.GameSession;
import com.yisy.modules.doramcu.model.PersistedGameSession;
import com.yisy.modules.doramcu.repository.GalaxyRepository;
import com.yisy.modules.doramcu.repository.PersistedGameSessionRepository;
import com.yisy.modules.doramcu.repository.SolarSystemRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;

/**
 * Service for exporting and importing game session save files.
 * <p>
 * A save file is a JSON document containing:
 * <ul>
 *   <li>Metadata: version, timestamp, session name, galaxy name, player count</li>
 *   <li>Game state: players, planets, fleets, positions (via GameStateSerializer)</li>
 * </ul>
 * <p>
 * Galaxy/solar system data is NOT included in the save file â€” it is loaded from the
 * database when the save is imported. This matches the existing persistence design.
 * </p>
 */
@Service
public class GameSaveService {

    private static final Logger log = LoggerFactory.getLogger(GameSaveService.class);
    private static final String SAVE_FORMAT_VERSION = "1.0";

    private final GameSessionService gameSessionService;
    private final GalaxyRepository galaxyRepository;
    private final SolarSystemRepository solarSystemRepository;
    private final PersistedGameSessionRepository persistedGameSessionRepository;

    public GameSaveService(GameSessionService gameSessionService, GalaxyRepository galaxyRepository,
                           SolarSystemRepository solarSystemRepository,
                           PersistedGameSessionRepository persistedGameSessionRepository) {
        this.gameSessionService = gameSessionService;
        this.galaxyRepository = galaxyRepository;
        this.solarSystemRepository = solarSystemRepository;
        this.persistedGameSessionRepository = persistedGameSessionRepository;
    }

    /**
     * Exports a game session as a save file map.
     * <p>
     * The returned map contains metadata and the serialized game state.
     * The caller should convert this to JSON for download.
     * </p>
     *
     * @param sessionId the session ID to export
     * @return a map representing the save file, or null if session not found
     * @throws IllegalStateException if the session has no game
     */
    @Transactional(readOnly = true)
    public Map<String, Object> exportSession(String sessionId) {
        GameSession session = gameSessionService.getSession(sessionId);
        if (session == null) {
            throw new NoSuchElementException("Session not found: " + sessionId);
        }

        Game game = (Game) session.getGame();
        if (game == null) {
            throw new IllegalStateException("Session " + sessionId + " has no game");
        }

        // Serialize the game state
        Map<String, Object> gameState = game.serializeGameState();

        // Build the save file with metadata
        Map<String, Object> saveFile = new LinkedHashMap<>();
        saveFile.put("version", SAVE_FORMAT_VERSION);
        saveFile.put("timestamp", Instant.now().toString());
        saveFile.put("sessionName", session.getName());
        saveFile.put("sessionId", sessionId);

        // Galaxy name for import reference
        try {
            if (game.getGalaxy(0) != null) {
                saveFile.put("galaxyName", game.getGalaxy(0).getName());
            }
        } catch (Exception e) {
            log.debug("Could not get galaxy name for save: {}", e.getMessage());
        }

        // Player count
        saveFile.put("playerCount", game.getPlayers().size());

        // Turn information
        saveFile.put("currentTurn", game.getCurrentTime());
        saveFile.put("currentPlayerIndex", game.getCurrentPlayerIndex());

        // The actual game state
        saveFile.put("gameState", gameState);

        log.info("Exported save for session {} ({}) with {} players", sessionId, session.getName(), game.getPlayers().size());
        return saveFile;
    }

    /**
     * Imports a save file and creates a new game session.
     * <p>
     * The save file must contain a "gameState" key with the serialized game state.
     * A new session ID is generated, and the galaxy is loaded from the database
     * using the "galaxyName" metadata if present.
     * </p>
     *
     * @param saveFile the save file map (parsed from JSON)
     * @return the new session ID
     * @throws IllegalArgumentException if the save file is invalid
     */
    @Transactional
    public String importSession(Map<String, Object> saveFile) {
        if (saveFile == null || saveFile.isEmpty()) {
            throw new IllegalArgumentException("Save file is empty");
        }

        // Validate version
        String version = (String) saveFile.get("version");
        if (version == null) {
            throw new IllegalArgumentException("Save file missing version");
        }
        if (!SAVE_FORMAT_VERSION.equals(version)) {
            log.warn("Save file version {} differs from current version {}", version, SAVE_FORMAT_VERSION);
        }

        // Extract game state
        @SuppressWarnings("unchecked")
        Map<String, Object> gameState = (Map<String, Object>) saveFile.get("gameState");
        if (gameState == null || gameState.isEmpty()) {
            throw new IllegalArgumentException("Save file missing game state");
        }

        // Extract metadata
        String sessionName = (String) saveFile.get("sessionName");
        if (sessionName == null || sessionName.isBlank()) {
            sessionName = "Imported Game";
        }

        // Append timestamp to avoid name collision
        String uniqueName = sessionName + " (" + Instant.now().getEpochSecond() + ")";

        // Create a new Game and deserialize the state
        Game game = new Game(uniqueName);
        game.deserializeGameState(gameState);

        // Try to load galaxy from the save file metadata
        String galaxyName = (String) saveFile.get("galaxyName");
        if (galaxyName != null && !galaxyName.isBlank()) {
            try {
                Optional<com.yisy.modules.doramcu.model.Galaxy> galaxyOpt = galaxyRepository.findByName(galaxyName);
                if (galaxyOpt.isPresent()) {
                    com.yisy.modules.doramcu.hex.Galaxy hexGalaxy = convertToHexGalaxy(galaxyOpt.get());
                    if (hexGalaxy != null) {
                        game.addGalaxy(hexGalaxy);
                        log.info("Loaded galaxy '{}' for imported session", galaxyName);
                    }
                } else {
                    log.warn("Galaxy '{}' not found in database for imported session", galaxyName);
                }
            } catch (Exception e) {
                log.warn("Failed to load galaxy for imported session: {}", e.getMessage());
            }
        }

        // Generate new session ID
        String newSessionId = UUID.randomUUID().toString();

        // Persist the imported session to the database so it can be restored
        try {
            String gameStateJson = new com.fasterxml.jackson.databind.ObjectMapper()
                    .writeValueAsString(gameState);
            PersistedGameSession persisted = new PersistedGameSession();
            persisted.setSessionId(newSessionId);
            persisted.setSessionName(uniqueName);
            persisted.setPlayerIds("[]");
            persisted.setGameState(gameStateJson);
            persistedGameSessionRepository.save(persisted);
            log.info("Persisted imported session {} to database", newSessionId);
        } catch (Exception e) {
            log.warn("Failed to persist imported session: {}", e.getMessage());
        }

        log.info("Imported save as new session {} ({}) with {} players",
                newSessionId, uniqueName, game.getPlayers().size());
        return newSessionId;
    }

    /**
     * Validates a save file without importing it.
     *
     * @param saveFile the save file map to validate
     * @return a map with "valid" (boolean) and "errors" (list of strings)
     */
    public Map<String, Object> validateSaveFile(Map<String, Object> saveFile) {
        Map<String, Object> result = new LinkedHashMap<>();
        List<String> errors = new ArrayList<>();

        if (saveFile == null || saveFile.isEmpty()) {
            errors.add("Save file is empty");
            result.put("valid", false);
            result.put("errors", errors);
            return result;
        }

        // Check version
        String version = (String) saveFile.get("version");
        if (version == null) {
            errors.add("Missing version");
        } else if (!SAVE_FORMAT_VERSION.equals(version)) {
            errors.add("Unsupported version: " + version + " (expected " + SAVE_FORMAT_VERSION + ")");
        }

        // Check game state
        Object gameState = saveFile.get("gameState");
        if (gameState == null) {
            errors.add("Missing game state");
        } else if (!(gameState instanceof Map)) {
            errors.add("Game state is not a valid object");
        } else {
            @SuppressWarnings("unchecked")
            Map<String, Object> gs = (Map<String, Object>) gameState;
            if (!gs.containsKey("players")) {
                errors.add("Game state missing players");
            }
            if (!gs.containsKey("fleets")) {
                errors.add("Game state missing fleets");
            }
        }

        // Check metadata
        if (saveFile.get("sessionName") == null) {
            errors.add("Missing session name (will use default)");
        }

        result.put("valid", errors.isEmpty());
        result.put("errors", errors);

        // Add metadata summary if valid
        if (errors.isEmpty()) {
            Map<String, Object> summary = new LinkedHashMap<>();
            summary.put("version", version);
            summary.put("sessionName", saveFile.get("sessionName"));
            summary.put("timestamp", saveFile.get("timestamp"));
            summary.put("galaxyName", saveFile.get("galaxyName"));
            summary.put("playerCount", saveFile.get("playerCount"));
            summary.put("currentTurn", saveFile.get("currentTurn"));
            result.put("summary", summary);
        }

        return result;
    }

    /**
     * Lists all available save sessions (persisted sessions with game state).
     *
     * @return list of session metadata maps
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> listSaveableSessions() {
        List<Map<String, Object>> allPersisted = gameSessionService.listPersistedSessions();
        List<Map<String, Object>> saveable = new ArrayList<>();

        for (Map<String, Object> session : allPersisted) {
            Boolean hasState = (Boolean) session.get("hasGameState");
            if (Boolean.TRUE.equals(hasState)) {
                saveable.add(session);
            }
        }

        return saveable;
    }

    /**
     * Converts a database Galaxy model to a hex.Galaxy for game use.
     */
    private com.yisy.modules.doramcu.hex.Galaxy convertToHexGalaxy(com.yisy.modules.doramcu.model.Galaxy dbGalaxy) {
        if (dbGalaxy == null) {
            return null;
        }

        // hex.Galaxy constructor takes (int id, String name)
        com.yisy.modules.doramcu.hex.Galaxy hexGalaxy = new com.yisy.modules.doramcu.hex.Galaxy(
                dbGalaxy.getId().intValue(),
                dbGalaxy.getName()
        );

        // Load systems from the database using the galaxy ID
        List<com.yisy.modules.doramcu.model.SolarSystem> systems = solarSystemRepository.findByGalaxyId(dbGalaxy.getId());
        for (com.yisy.modules.doramcu.model.SolarSystem sys : systems) {
            com.yisy.modules.doramcu.hex.SolarSystem hexSystem = new com.yisy.modules.doramcu.hex.SolarSystem(
                    sys.getId().intValue()
            );
            // addSystem requires sectorRow, sectorCol, subRow, subCol, x, y, z
            hexGalaxy.addSystem(hexSystem,
                    sys.getSectorRow(),
                    sys.getSectorCol(),
                    sys.getSubSectorRow(),
                    sys.getSubSectorCol(),
                    0, 0, 0
            );
        }

        return hexGalaxy;
    }
}
