package com.yisy.modules.doramcu.earth2026.service;

import com.yisy.modules.doramcu.dto.JoinSessionResponse;
import com.yisy.modules.doramcu.gamecore.GameType;
import com.yisy.modules.doramcu.repository.GalaxyRepository;
import com.yisy.modules.doramcu.ai.framework.AiPlayerServiceAdapter;
import com.yisy.modules.doramcu.service.AiPlayerService;
import com.yisy.modules.doramcu.service.GameInitializationService;
import com.yisy.modules.doramcu.service.GameSessionService;
import com.yisy.modules.doramcu.unified.GlobalClockService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service that handles portal travel between galaxies.
 * <p>
 * When a player clicks a portal in the HQ, this service:
 * <ol>
 *   <li>Finds the target galaxy by GameType</li>
 *   <li>Finds or creates a game session for that galaxy</li>
 *   <li>Joins the player to the session</li>
 *   <li>Returns session info for the frontend to navigate to</li>
 * </ol>
 * </p>
 */
@Service
public class PortalTravelService {

    private static final Logger log = LoggerFactory.getLogger(PortalTravelService.class);

    private final GalaxyRepository galaxyRepository;
    private final GameSessionService gameSessionService;
    private final GameInitializationService gameInitializationService;
    private final GlobalClockService globalClockService;
    private final AiPlayerService aiPlayerService;
    private final AiPlayerServiceAdapter aiPlayerServiceAdapter;

    public PortalTravelService(GalaxyRepository galaxyRepository,
                                GameSessionService gameSessionService,
                                GameInitializationService gameInitializationService,
                                GlobalClockService globalClockService,
                                AiPlayerService aiPlayerService,
                                AiPlayerServiceAdapter aiPlayerServiceAdapter) {
        this.galaxyRepository = galaxyRepository;
        this.gameSessionService = gameSessionService;
        this.gameInitializationService = gameInitializationService;
        this.globalClockService = globalClockService;
        this.aiPlayerService = aiPlayerService;
        this.aiPlayerServiceAdapter = aiPlayerServiceAdapter;
    }

    /**
     * Travel through a portal to the target galaxy.
     * <p>
     * This method finds the galaxy matching the given GameType, creates or reuses
     * a game session for that galaxy, and joins the player to the session.
     * </p>
     *
     * @param gameType the target game type (e.g., STAR_WARS, TRAVELLER, DND_5E)
     * @param playerId the player's ID (may be null for guest players)
     * @param playerName the player's display name
     * @return travel result containing session info and navigation URL
     */
    @Transactional
    public PortalTravelResult travelToGalaxy(GameType gameType, Long playerId, String playerName) {
        log.info("Portal travel: player '{}' (ID: {}) traveling to {}", playerName, playerId, gameType.getDisplayName());

        // Step 1: Find the galaxy for this game type
        Optional<com.yisy.modules.doramcu.model.Galaxy> dbGalaxyOpt = findGalaxyByGameType(gameType);
        if (dbGalaxyOpt.isEmpty()) {
            log.warn("No galaxy found for game type: {}", gameType.getDisplayName());
            return PortalTravelResult.failure(
                    "Galaxy not available",
                    "The " + gameType.getDisplayName() + " galaxy has not been created yet. Please try again later.");
        }

        com.yisy.modules.doramcu.model.Galaxy dbGalaxy = dbGalaxyOpt.get();
        String galaxyName = dbGalaxy.getName();
        log.info("Found galaxy '{}' for game type {}", galaxyName, gameType.getDisplayName());

        // Step 2: Find existing session for this galaxy or create a new one
        String sessionId = findOrCreateSessionForGalaxy(dbGalaxy, gameType);

        // Step 3: Join the player to the session
        String playerIdStr = joinPlayerToSession(sessionId, playerId, playerName);

        // Step 4: Build the result
        String gameUrl = buildGameUrl(sessionId, gameType);
        
        PortalTravelResult result = new PortalTravelResult();
        result.setSuccess(true);
        result.setGalaxyName(galaxyName);
        result.setGameType(gameType);
        result.setSessionId(sessionId);
        result.setPlayerId(playerIdStr);
        result.setGameUrl(gameUrl);
        result.setMessage("Welcome to " + galaxyName + "!");

        log.info("Portal travel complete: {} -> {} (session: {})", playerName, galaxyName, sessionId);
        return result;
    }

    /**
     * Find a galaxy by its GameType.
     * Searches by gameType field first, then by loreSlug, then by name pattern.
     */
    @Transactional(readOnly = true)
    public Optional<com.yisy.modules.doramcu.model.Galaxy> findGalaxyByGameType(GameType gameType) {
        String typeValue = gameType.name();
        String displayName = gameType.getDisplayName();

        List<com.yisy.modules.doramcu.model.Galaxy> allGalaxies = galaxyRepository.findAll();

        // First pass: exact gameType field match
        for (com.yisy.modules.doramcu.model.Galaxy g : allGalaxies) {
            if (typeValue.equals(g.getGameType())) {
                return Optional.of(g);
            }
        }

        // Second pass: lore slug match
        String loreSlug = getLoreSlugForGameType(gameType);
        if (loreSlug != null) {
            for (com.yisy.modules.doramcu.model.Galaxy g : allGalaxies) {
                if (loreSlug.equals(g.getLoreSlug())) {
                    return Optional.of(g);
                }
            }
        }

        // Third pass: name contains display name
        List<com.yisy.modules.doramcu.model.Galaxy> byName = galaxyRepository.findByNameContainingIgnoreCase(displayName);
        if (!byName.isEmpty()) {
            return Optional.of(byName.get(0));
        }

        // Fourth pass: name contains gameType name (case-insensitive, underscores to spaces)
        for (com.yisy.modules.doramcu.model.Galaxy g : allGalaxies) {
            if (g.getName().toLowerCase().contains(typeValue.toLowerCase().replace("_", " "))) {
                return Optional.of(g);
            }
        }

        // Fifth pass: try expected galaxy names for known game types
        String expectedName = getExpectedGalaxyName(gameType);
        if (expectedName != null) {
            for (com.yisy.modules.doramcu.model.Galaxy g : allGalaxies) {
                if (expectedName.equalsIgnoreCase(g.getName())) {
                    return Optional.of(g);
                }
            }
        }

        return Optional.empty();
    }

    /**
     * Returns the expected galaxy name for a given GameType, matching GalaxyStartupRegistry.
     */
    private String getExpectedGalaxyName(GameType gameType) {
        switch (gameType) {
            case EARTH_2026: return "Earth 2026";
            case TRAVELLER: return "Traveller Galaxy";
            case DND_5E: return "Forgotten Realms";
            case STAR_TREK_4X: return "Star Trek Galaxy";
            case BABYLON_5: return "Babylon 5 Galaxy";
            case STAR_WARS: return "Star Wars Galaxy";
            case DORAMCU_4X: return "DoraMCU 4X Galaxy";
            case WARHAMMER_40K: return "Warhammer 40K Galaxy";
            case CAPTAINS_LOG: return "Captain's Log Galaxy";
            case SFB: return "Star Frontiers Galaxy";
            case MECHWARRIOR: return "MechWarrior Galaxy";
            case STARSHIP_TROOPERS: return "Starship Troopers Galaxy";
            case THIRD_REICH: return "Third Reich Galaxy";
            case ZERO_ZERO_SEVEN: return "007 Espionage Galaxy";
            case TRANSPORT_TYCOON: return "Transport Tycoon Galaxy";
            case FPS: return "FPS Combat Galaxy";
            default: return null;
        }
    }

    /**
     * Maps GameType to the lore slug used in GalaxyStartupRegistry.
     */
    private String getLoreSlugForGameType(GameType gameType) {
        switch (gameType) {
            case EARTH_2026: return "earth-2026";
            case TRAVELLER: return "traveller";
            case DND_5E: return "dnd-5e";
            case STAR_TREK_4X: return "star-trek";
            case BABYLON_5: return "babylon5";
            case STAR_WARS: return "star-wars";
            case DORAMCU_4X: return "doramcu-4x";
            case WARHAMMER_40K: return "warhammer-40k";
            case CAPTAINS_LOG: return "captains-log";
            case SFB: return "star-frontiers";
            case MECHWARRIOR: return "mechwarrior";
            case STARSHIP_TROOPERS: return "starship-troopers";
            case THIRD_REICH: return "third-reich";
            case ZERO_ZERO_SEVEN: return "007";
            case TRANSPORT_TYCOON: return "transport-tycoon";
            case FPS: return "fps-combat";
            default: return null;
        }
    }

    /**
     * Find an existing session that has the given galaxy loaded, or create a new one.
     */
    private String findOrCreateSessionForGalaxy(com.yisy.modules.doramcu.model.Galaxy dbGalaxy, GameType gameType) {
        String galaxyName = dbGalaxy.getName();

        // Check for existing session with this galaxy
        String existingSessionId = gameSessionService.getSessionIdByGalaxyName(galaxyName);
        if (existingSessionId != null) {
            log.info("Reusing existing session {} for galaxy {}", existingSessionId, galaxyName);
            return existingSessionId;
        }

        // Create a new session for this galaxy
        String sessionId = UUID.randomUUID().toString();
        String sessionName = galaxyName + " Session";

        // Create the session via the game session service
        gameSessionService.createSessionForGalaxy(sessionId, sessionName, dbGalaxy, gameType);
        
        log.info("Created new session {} for galaxy {}", sessionId, galaxyName);
        return sessionId;
    }

    /**
     * Join a player to the given session.
     */
    private String joinPlayerToSession(String sessionId, Long playerId, String playerName) {
        try {
            // Try to join as a human player first
            try {
                JoinSessionResponse joinResponse = gameSessionService.joinSession(
                        playerId != null ? playerId : 0L,
                        playerName, sessionId);
                return String.valueOf(joinResponse.getPlayerId());
            } catch (Exception e) {
                // Fall back to AI player creation
                log.debug("Human join failed, creating AI player: {}", e.getMessage());
                JoinSessionResponse aiResponse = aiPlayerService.createAiPlayer(
                        sessionId, playerName, AiPlayerService.Difficulty.MEDIUM);
                return String.valueOf(aiResponse.getPlayerId());
            }
        } catch (Exception e) {
            log.error("Failed to join player to session {}: {}", sessionId, e.getMessage());
            return "0";
        }
    }

    /**
     * Build the game URL for the frontend to navigate to.
     * Maps each GameType to its dedicated HTML page, passing the session ID for auto-join.
     */
    private String buildGameUrl(String sessionId, GameType gameType) {
        String page = mapGameTypeToPage(gameType);
        return page + "?session=" + sessionId + "&galaxy=" + gameType.name();
    }

    /**
     * Maps a GameType to its dedicated frontend HTML page.
     */
    private String mapGameTypeToPage(GameType gameType) {
        switch (gameType) {
            case STAR_WARS: return "/starwars.html";
            case TRAVELLER: return "/traveller-rules.html";
            case DND_5E: return "/dnd5e.html";
            case BABYLON_5: return "/babylon5.html";
            case STAR_TREK_4X: return "/captainslog.html";
            case EARTH_2026: return "/earth2026.html";
            case FPS: return "/earth2026.html";
            case MECHWARRIOR: return "/mechwarrior.html";
            case STARSHIP_TROOPERS: return "/starshiptroopers.html";
            case WARHAMMER_40K: return "/warhammer40k.html";
            case SFB: return "/starfrontiers.html";
            case ZERO_ZERO_SEVEN: return "/zero07.html";
            case DORAMCU_4X: return "/fourx.html";
            case TRANSPORT_TYCOON: return "/fourx.html";
            case THIRD_REICH: return "/fourx.html";
            case CAPTAINS_LOG: return "/captainslog.html";
            default: return "/fourx.html";
        }
    }

    /**
     * Result of a portal travel attempt.
     */
    public static class PortalTravelResult {
        private boolean success;
        private String galaxyName;
        private GameType gameType;
        private String sessionId;
        private String playerId;
        private String gameUrl;
        private String message;
        private String errorMessage;

        public PortalTravelResult() {}

        public static PortalTravelResult failure(String message, String errorMessage) {
            PortalTravelResult result = new PortalTravelResult();
            result.success = false;
            result.message = message;
            result.errorMessage = errorMessage;
            return result;
        }

        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        public String getGalaxyName() { return galaxyName; }
        public void setGalaxyName(String galaxyName) { this.galaxyName = galaxyName; }
        public GameType getGameType() { return gameType; }
        public void setGameType(GameType gameType) { this.gameType = gameType; }
        public String getSessionId() { return sessionId; }
        public void setSessionId(String sessionId) { this.sessionId = sessionId; }
        public String getPlayerId() { return playerId; }
        public void setPlayerId(String playerId) { this.playerId = playerId; }
        public String getGameUrl() { return gameUrl; }
        public void setGameUrl(String gameUrl) { this.gameUrl = gameUrl; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    }
}
