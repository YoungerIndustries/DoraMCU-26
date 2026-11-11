package com.yisy.modules.doraneuron.service.multigame;

import com.yisy.modules.doraneuron.model.multigame.GameProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Routes requests to the appropriate game server based on the game ID.
 *
 * This service determines which game server URL to use for a given game,
 * supporting path-based routing (e.g., /game/doramcu/** → DoraMCU server)
 * and default routing when no game is specified.
 */
@Service
public class GameRouterService {

    private static final Logger log = LoggerFactory.getLogger(GameRouterService.class);

    private final GameRegistryService gameRegistry;

    public GameRouterService(GameRegistryService gameRegistry) {
        this.gameRegistry = gameRegistry;
    }

    /**
     * Get the game server URL for a specific game.
     *
     * @param gameId the game identifier
     * @return the game server URL, or empty if not found
     */
    public Optional<String> resolveGameServer(String gameId) {
        return gameRegistry.getGameServerUrl(gameId);
    }

    /**
     * Resolve the game ID from a request path.
     *
     * Path format: /game/{gameId}/** or /game/{gameId}
     * If no gameId is found, returns the default game.
     *
     * @param requestPath the request URI path
     * @return the resolved game ID
     */
    public String resolveGameId(String requestPath) {
        if (requestPath == null || requestPath.isEmpty()) {
            return getDefaultGameId();
        }

        // Extract game ID from path: /game/{gameId} or /game/{gameId}/...
        String path = requestPath.startsWith("/") ? requestPath.substring(1) : requestPath;
        String[] parts = path.split("/");

        if (parts.length >= 2 && "game".equals(parts[0])) {
            String potentialGameId = parts[1];
            if (potentialGameId != null && !potentialGameId.isEmpty()) {
                // Check if this is a registered game
                if (gameRegistry.getGame(potentialGameId).isPresent()) {
                    return potentialGameId;
                }
            }
        }

        return getDefaultGameId();
    }

    /**
     * Build the full target URL for a game request.
     *
     * @param gameId the game identifier
     * @param path the request path
     * @param queryString optional query string
     * @return the full target URL
     */
    public String buildTargetUrl(String gameId, String path, String queryString) {
        Optional<String> serverUrl = resolveGameServer(gameId);
        if (serverUrl.isEmpty()) {
            log.warn("No game server URL for game '{}'", gameId);
            return "/";
        }

        String target = serverUrl.get();
        if (target.endsWith("/")) {
            target = target.substring(0, target.length() - 1);
        }

        String fullTarget = target + (path != null ? path : "");
        if (queryString != null && !queryString.isEmpty()) {
            fullTarget += "?" + queryString;
        }

        return fullTarget;
    }

    /**
     * Get the default game ID.
     */
    private String getDefaultGameId() {
        return gameRegistry.getDefaultGame()
                .map(GameProfile::getGameId)
                .orElse("default");
    }

    /**
     * Check if a game has a configured server URL.
     */
    public boolean hasGameServer(String gameId) {
        return resolveGameServer(gameId).isPresent();
    }
}
