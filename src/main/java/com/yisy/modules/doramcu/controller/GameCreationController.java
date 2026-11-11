package com.yisy.modules.doramcu.controller;

import com.yisy.modules.doramcu.dto.CreateGalaxyRequest;
import com.yisy.modules.doramcu.dto.CreateSessionResponse;
import com.yisy.modules.doramcu.dto.FourxUnlockState;
import com.yisy.modules.doramcu.dto.ThemeGameResult;
import com.yisy.modules.doramcu.hex.GalaxyGenerationConfig;
import com.yisy.modules.doramcu.hex.GalaxyTemplate;
import com.yisy.modules.doramcu.model.GameSession;
import com.yisy.modules.doramcu.service.FourxProgressiveUnlockService;
import com.yisy.modules.doramcu.service.GalaxyCreationService;
import com.yisy.modules.doramcu.service.GameSessionService;
import com.yisy.modules.doramcu.service.ThemeBasedGameService;
import com.yisy.modules.doramcu.util.PlayerIdExtractor;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller for RPG-first game creation.
 * <p>
 * Provides endpoints for creating new games with template selection or custom themes.
 * Players can choose from predefined RPG templates (Single System, Subsector) or
 * enter a custom theme that will be researched via web search and AI generation.
 * </p>
 */
@RestController
@RequestMapping("/api/v1/games")
public class GameCreationController {

    private static final Logger log = LoggerFactory.getLogger(GameCreationController.class);

    private final GalaxyCreationService galaxyCreationService;
    private final GameSessionService gameSessionService;
    private final ThemeBasedGameService themeBasedGameService;
    private final FourxProgressiveUnlockService fourxProgressiveUnlockService;
    private final PlayerIdExtractor playerIdExtractor;

    public GameCreationController(
            GalaxyCreationService galaxyCreationService,
            GameSessionService gameSessionService,
            ThemeBasedGameService themeBasedGameService,
            FourxProgressiveUnlockService fourxProgressiveUnlockService,
            PlayerIdExtractor playerIdExtractor) {
        this.galaxyCreationService = galaxyCreationService;
        this.gameSessionService = gameSessionService;
        this.themeBasedGameService = themeBasedGameService;
        this.fourxProgressiveUnlockService = fourxProgressiveUnlockService;
        this.playerIdExtractor = playerIdExtractor;
    }

    /**
     * Creates a new game with the specified template or custom theme.
     * <p>
     * If {@link CreateGalaxyRequest#isCustomTheme()} is true, the system researches the theme
     * via web search and AI to generate rules and galaxy configuration.
     * Otherwise, it uses the specified template (defaulting to SUBSECTOR_RPG).
     * </p>
     *
     * @param request the game creation request with template or theme
     * @param authentication the current user's authentication
     * @return the creation result with session ID and game state
     */
    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> createGame(
            @Valid @RequestBody CreateGalaxyRequest request,
            Authentication authentication) {
        log.info("Creating new game for user: {}, request: {}", authentication.getName(), request.getName());

        Long playerId = playerIdExtractor.extract(authentication);

        try {
            if (request.isCustomTheme()) {
                // Custom theme: research and generate via AI
                return createThemeBasedGame(request, playerId, authentication.getName());
            } else {
                // Template-based: use predefined template
                return createTemplateBasedGame(request, playerId, authentication.getName());
            }
        } catch (IllegalArgumentException e) {
            log.warn("Invalid game creation request: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Failed to create game: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to create game: " + e.getMessage()));
        }
    }

    /**
     * Creates a game based on a custom theme using web search + AI generation.
     */
    private ResponseEntity<Map<String, Object>> createThemeBasedGame(
            CreateGalaxyRequest request, Long playerId, String username) {
        String theme = request.getTheme();
        if (theme == null || theme.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Theme is required for custom theme creation"));
        }

        log.info("Generating theme-based game for theme: {}", theme);

        // Generate theme game result
        ThemeGameResult themeResult = themeBasedGameService.generateThemeGame(theme);

        // Build config from theme result
        GalaxyGenerationConfig config = themeResult.getConfig();

        // Create galaxy and session
        CreateGalaxyRequest themedRequest = new CreateGalaxyRequest();
        themedRequest.setName(request.getName() != null ? request.getName() : theme + " Galaxy");
        themedRequest.setSystemCount(config.getSystemCount());
        themedRequest.setPlanetsPerSystem(config.getPlanetsPerSystem());
        themedRequest.setGameType(config.getGameType());
        themedRequest.setDifficultyLevel(config.getDifficultyLevel());
        themedRequest.setTemplate("THEME_BASED");

        GalaxyCreationService.GalaxyCreationResult result =
                galaxyCreationService.createGalaxyWithAiPlayers(themedRequest.getName());

        // Get session and enable 4X layer
        GameSession session = gameSessionService.getGameSession(result.getSessionId());
        if (session != null && config.isEnable4xLayer()) {
            FourxUnlockState state = session.getFourxUnlockState();
            // 4X layer is enabled but starts locked — milestones will unlock progressively
            log.info("4X layer enabled for session {}, starting with all layers locked", result.getSessionId());
        }

        return buildResponse(result, themeResult, username);
    }

    /**
     * Creates a game based on a predefined template.
     */
    private ResponseEntity<Map<String, Object>> createTemplateBasedGame(
            CreateGalaxyRequest request, Long playerId, String username) {
        // Determine template
        GalaxyTemplate template = resolveTemplate(request.getTemplate());
        log.info("Creating template-based game with template: {}", template.name());

        // Build config from template, overridden by request values
        GalaxyGenerationConfig config = template.getConfig();

        // Create galaxy and session
        String sessionName = request.getName() != null ? request.getName() : template.getDisplayName() + " Game";
        GalaxyCreationService.GalaxyCreationResult result =
                galaxyCreationService.createGalaxyWithAiPlayers(sessionName);

        // Get session and check 4X layer
        GameSession session = gameSessionService.getGameSession(result.getSessionId());
        if (session != null && request.isEnable4xLayer()) {
            log.info("4X layer enabled for session {}, starting with all layers locked", result.getSessionId());
        }

        Map<String, Object> response = new HashMap<>();
        response.put("sessionId", result.getSessionId());
        response.put("sessionName", result.getSessionName());
        response.put("template", template.name());
        response.put("displayName", template.getDisplayName());
        response.put("systemCount", config.getSystemCount());
        response.put("gameType", config.getGameType());
        response.put("enable4xLayer", request.isEnable4xLayer());
        response.put("unlockState", session != null ? session.getFourxUnlockState() : new FourxUnlockState());
        response.put("playerId", playerId);
        response.put("playerName", username);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Resolves the template from the request, defaulting to SUBSECTOR_RPG.
     */
    private GalaxyTemplate resolveTemplate(String templateName) {
        if (templateName == null || templateName.isBlank()) {
            return GalaxyTemplate.SUBSECTOR_RPG;
        }
        GalaxyTemplate template = GalaxyTemplate.findByName(templateName);
        if (template == null) {
            log.warn("Unknown template '{}', defaulting to SUBSECTOR_RPG", templateName);
            return GalaxyTemplate.SUBSECTOR_RPG;
        }
        return template;
    }

    /**
     * Builds the response map for theme-based game creation.
     */
    private ResponseEntity<Map<String, Object>> buildResponse(
            GalaxyCreationService.GalaxyCreationResult result,
            ThemeGameResult themeResult,
            String username) {
        Map<String, Object> response = new HashMap<>();
        response.put("sessionId", result.getSessionId());
        response.put("sessionName", result.getSessionName());
        response.put("theme", themeResult.getThemeName());
        response.put("namingConvention", themeResult.getNamingConvention());
        response.put("empireCount", themeResult.getEmpireCount());
        response.put("galaxyShape", themeResult.getGalaxyShape());
        response.put("systemCount", themeResult.getConfig().getSystemCount());
        response.put("gameType", themeResult.getConfig().getGameType());
        response.put("ruleCount", themeResult.getRules().size());
        response.put("enable4xLayer", themeResult.getConfig().isEnable4xLayer());
        response.put("playerName", username);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Returns the current 4X unlock state for a session.
     *
     * @param sessionId the session ID
     * @param authentication the current user's authentication
     * @return the unlock state
     */
    @GetMapping("/{sessionId}/4x-unlock-state")
    public ResponseEntity<FourxUnlockState> getFourxUnlockState(
            @PathVariable String sessionId,
            Authentication authentication) {
        log.debug("User {} requesting 4X unlock state for session: {}", authentication.getName(), sessionId);

        Long playerId = playerIdExtractor.extract(authentication);
        GameSession session = gameSessionService.getGameSession(sessionId);

        if (session == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(session.getFourxUnlockState());
    }
}
