package com.yisy.modules.doramcu.config;
import com.yisy.modules.doramcu.model.galaxy.Galaxy;


import com.yisy.modules.doramcu.controller.misc.AchievementWebSocketHandler;
import com.yisy.modules.doramcu.controller.misc.FleetTrackerWebSocketHandler;
import com.yisy.modules.doramcu.controller.session.GameStateWebSocketHandler;
import com.yisy.modules.doramcu.controller.misc.NarrativeWebSocketHandler;
import com.yisy.modules.doramcu.controller.misc.PresenceWebSocketHandler;
import com.yisy.modules.doramcu.controller.session.ReplayWebSocketHandler;
import com.yisy.modules.doramcu.controller.trade.ResourcePipelineWebSocketHandler;
import com.yisy.modules.doramcu.controller.session.SpectatorWebSocketHandler;
import com.yisy.modules.doramcu.service.misc.AchievementService;
import com.yisy.modules.doramcu.service.game.GameReplayService;
import com.yisy.modules.doramcu.service.session.GameSessionService;
import com.yisy.modules.doramcu.service.game.GameStateBridge;
import com.yisy.modules.doramcu.service.ai.AiAdminVisibilityService;
import com.yisy.modules.doramcu.service.galaxy.Galaxy3DService;
import com.yisy.modules.doramcu.service.misc.SpectatorService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * Configures WebSocket endpoints for 3D galaxy visualization.
 * <p>
 * Registers handlers for:
     * <ul>
     *   <li>{@code /ws/fleet-tracker} — Real-time fleet position and combat updates</li>
     *   <li>{@code /ws/presence} — Multiplayer presence and activity tracking</li>
     *   <li>{@code /ws/narrative} — AI narrative event streaming</li>
     *   <li>{@code /ws/resource-pipeline} — Real-time resource pipeline updates</li>
     * </ul>
 * </p>
 */
@Configuration
@EnableWebSocket
public class Galaxy3DWebSocketConfig implements WebSocketConfigurer {

    @Value("${doramcu.3d.real-data-mode:false}")
    private boolean realDataMode;

    private GameStateBridge gameStateBridge;
    private GameReplayService gameReplayService;
    private GameSessionService gameSessionService;
    private SpectatorService spectatorService;
    private AchievementService achievementService;
    private AiAdminVisibilityService aiAdminVisibilityService;
    private Galaxy3DService galaxy3DService;

    public Galaxy3DWebSocketConfig(GameStateBridge gameStateBridge, GameReplayService gameReplayService,
                                   GameSessionService gameSessionService, SpectatorService spectatorService,
                                   AchievementService achievementService,
                                   AiAdminVisibilityService aiAdminVisibilityService,
                                   Galaxy3DService galaxy3DService) {
        this.gameStateBridge = gameStateBridge;
        this.gameReplayService = gameReplayService;
        this.gameSessionService = gameSessionService;
        this.spectatorService = spectatorService;
        this.achievementService = achievementService;
        this.aiAdminVisibilityService = aiAdminVisibilityService;
        this.galaxy3DService = galaxy3DService;
    }

    @Bean
    public FleetTrackerWebSocketHandler fleetTrackerWebSocketHandler() {
        return new FleetTrackerWebSocketHandler(galaxy3DService, gameStateBridge, realDataMode);
    }

    @Bean
    public PresenceWebSocketHandler presenceWebSocketHandler() {
        return new PresenceWebSocketHandler(gameStateBridge, aiAdminVisibilityService, realDataMode);
    }

    @Bean
    public NarrativeWebSocketHandler narrativeWebSocketHandler() {
        return new NarrativeWebSocketHandler(gameStateBridge, realDataMode);
    }

    @Bean
    public ResourcePipelineWebSocketHandler resourcePipelineWebSocketHandler() {
        return new ResourcePipelineWebSocketHandler(gameStateBridge, realDataMode);
    }

    @Bean
    public ReplayWebSocketHandler replayWebSocketHandler() {
        return new ReplayWebSocketHandler(gameReplayService);
    }

    @Bean
    public SpectatorWebSocketHandler spectatorWebSocketHandler() {
        return new SpectatorWebSocketHandler(spectatorService);
    }

    @Bean
    public AchievementWebSocketHandler achievementWebSocketHandler() {
        return new AchievementWebSocketHandler(achievementService);
    }

    @Bean
    public GameStateWebSocketHandler gameStateWebSocketHandler() {
        return new GameStateWebSocketHandler(gameSessionService);
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(fleetTrackerWebSocketHandler(), "/ws/fleet-tracker")
                .setAllowedOrigins("*");
        registry.addHandler(presenceWebSocketHandler(), "/ws/presence")
                .setAllowedOrigins("*");
        registry.addHandler(narrativeWebSocketHandler(), "/ws/narrative")
                .setAllowedOrigins("*");
        registry.addHandler(resourcePipelineWebSocketHandler(), "/ws/resource-pipeline")
                .setAllowedOrigins("*");
        registry.addHandler(replayWebSocketHandler(), "/ws/replay")
                .setAllowedOrigins("*");
        registry.addHandler(spectatorWebSocketHandler(), "/ws/spectator")
                .setAllowedOrigins("*");
        registry.addHandler(achievementWebSocketHandler(), "/ws/achievements")
                .setAllowedOrigins("*");
        registry.addHandler(gameStateWebSocketHandler(), "/ws/game-state")
                .setAllowedOrigins("*");
    }

}
