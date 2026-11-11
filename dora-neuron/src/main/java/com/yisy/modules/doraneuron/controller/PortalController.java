package com.yisy.modules.doraneuron.controller;

import com.yisy.modules.doraneuron.config.NeuronConfig;
import com.yisy.modules.doraneuron.model.multigame.GameProfile;
import com.yisy.modules.doraneuron.service.multigame.GameRegistryService;
import com.yisy.modules.doraneuron.service.multigame.GameRouterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.List;
import java.util.Optional;

/**
 * Public-facing web controller that serves the landing page and proxies or
 * redirects requests to the actual DoraMCU game server.
 *
 * This is the entry point that visitors see — the Neuron acts as a gateway
 * between the public internet and the game server.
 *
 * Phase 14: Supports multi-game routing via /game/{gameId}/** paths.
 */
@RestController
@RequestMapping("/")
public class PortalController {

    private static final Logger log = LoggerFactory.getLogger(PortalController.class);

    private final NeuronConfig config;
    private final GameRegistryService gameRegistry;
    private final GameRouterService gameRouter;

    public PortalController(NeuronConfig config, GameRegistryService gameRegistry,
                            GameRouterService gameRouter) {
        this.config = config;
        this.gameRegistry = gameRegistry;
        this.gameRouter = gameRouter;
    }

    /**
     * Serve the landing page with links to all registered games.
     */
    @GetMapping(value = "/", produces = MediaType.TEXT_HTML_VALUE)
    public String portal() {
        List<GameProfile> games = gameRegistry.getActiveGames();
        StringBuilder gameButtons = new StringBuilder();

        if (games.isEmpty()) {
            // Fallback to single-game mode
            gameButtons.append("    <a class=\"btn\" href=\"/game\">Enter the Game</a>\n");
        } else {
            for (GameProfile game : games) {
                String gameId = game.getGameId();
                String gameName = escapeHtml(game.getGameName() != null ? game.getGameName() : gameId);
                gameButtons.append("    <a class=\"btn\" href=\"/game/").append(gameId)
                        .append("\">").append(gameName).append("</a>\n");
            }
        }

        return "<!DOCTYPE html>\n" +
                "<html lang=\"en\">\n" +
                "<head>\n" +
                "  <meta charset=\"UTF-8\">\n" +
                "  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                "  <title>" + escapeHtml(config.getPortalTitle()) + "</title>\n" +
                "  <style>\n" +
                "    body { font-family: Arial, sans-serif; background: #0a0a1a; color: #e0e0e0; margin: 0; padding: 0; display: flex; justify-content: center; align-items: center; min-height: 100vh; }\n" +
                "    .container { text-align: center; max-width: 600px; padding: 40px; }\n" +
                "    h1 { color: #00d4ff; font-size: 2.5em; margin-bottom: 10px; }\n" +
                "    p { color: #a0a0b0; font-size: 1.1em; line-height: 1.6; }\n" +
                "    .btn { display: inline-block; margin: 10px 10px; padding: 15px 40px; background: #00d4ff; color: #0a0a1a; text-decoration: none; border-radius: 8px; font-weight: bold; font-size: 1.2em; transition: background 0.3s; }\n" +
                "    .btn:hover { background: #00a8cc; }\n" +
                "    .status { margin-top: 20px; font-size: 0.9em; color: #606070; }\n" +
                "  </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "  <div class=\"container\">\n" +
                "    <h1>" + escapeHtml(config.getPortalTitle()) + "</h1>\n" +
                "    <p>Welcome to the DoraMCU gateway. Select a game to enter.</p>\n" +
                gameButtons.toString() +
                "    <div class=\"status\">Neuron node — " + games.size() + " game(s) available</div>\n" +
                "  </div>\n" +
                "</body>\n" +
                "</html>";
    }

    /**
     * Redirect to the default game server.
     */
    @GetMapping("/game")
    public ResponseEntity<?> redirectToGame(HttpServletRequest request) {
        String target = resolveDefaultGameUrl();
        log.debug("Redirecting to default game server: {}", target);
        return ResponseEntity.status(302)
                .location(URI.create(target))
                .build();
    }

    /**
     * Catch-all for /game/** paths — routes to the appropriate game server.
     *
     * Supports multi-game routing: /game/{gameId}/** → game-specific server
     * Falls back to default game server when no gameId is found.
     */
    @GetMapping("/game/**")
    public ResponseEntity<?> proxyToGame(HttpServletRequest request) {
        String requestPath = request.getRequestURI();
        String gameId = gameRouter.resolveGameId(requestPath);
        String queryString = request.getQueryString();

        String target = gameRouter.buildTargetUrl(gameId, requestPath, queryString);
        log.debug("Routing {} (game: {}) to {}", requestPath, gameId, target);
        return ResponseEntity.status(302)
                .location(URI.create(target))
                .build();
    }

    /**
     * Resolve the default game server URL.
     */
    private String resolveDefaultGameUrl() {
        Optional<GameProfile> defaultGame = gameRegistry.getDefaultGame();
        if (defaultGame.isPresent() && defaultGame.get().getGameServerUrl() != null) {
            String url = defaultGame.get().getGameServerUrl();
            return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
        }
        // Fallback to config
        String url = config.getGameServerUrl();
        if (url == null || url.isEmpty()) {
            url = config.getCoordinatorUrl();
        }
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }

    private String escapeHtml(String input) {
        if (input == null) return "";
        return input.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}
