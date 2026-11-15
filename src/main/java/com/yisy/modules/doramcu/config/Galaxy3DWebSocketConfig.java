package com.yisy.modules.doramcu.config;

import com.yisy.modules.doramcu.controller.AchievementWebSocketHandler;
import com.yisy.modules.doramcu.controller.FleetTrackerWebSocketHandler;
import com.yisy.modules.doramcu.controller.GameStateWebSocketHandler;
import com.yisy.modules.doramcu.controller.NarrativeWebSocketHandler;
import com.yisy.modules.doramcu.controller.PresenceWebSocketHandler;
import com.yisy.modules.doramcu.controller.ReplayWebSocketHandler;
import com.yisy.modules.doramcu.controller.ResourcePipelineWebSocketHandler;
import com.yisy.modules.doramcu.controller.SpectatorWebSocketHandler;
// dora-neuron excluded: missing servlet dependency
// import com.yisy.modules.doraneuron.ws.GameSyncWebSocketHandler;
import com.yisy.modules.doramcu.service.AchievementService;
import com.yisy.modules.doramcu.service.GameReplayService;
import com.yisy.modules.doramcu.service.GameSessionService;
import com.yisy.modules.doramcu.service.GameStateBridge;
import com.yisy.modules.doramcu.service.SpectatorService;
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

    public Galaxy3DWebSocketConfig(GameStateBridge gameStateBridge, GameReplayService gameReplayService,
                                   GameSessionService gameSessionService, SpectatorService spectatorService,
                                   AchievementService achievementService) {
        this.gameStateBridge = gameStateBridge;
        this.gameReplayService = gameReplayService;
        this.gameSessionService = gameSessionService;
        this.spectatorService = spectatorService;
        this.achievementService = achievementService;
    }

    @Bean
    public FleetTrackerWebSocketHandler fleetTrackerWebSocketHandler() {
        return new FleetTrackerWebSocketHandler(gameStateBridge, realDataMode);
    }

    @Bean
    public PresenceWebSocketHandler presenceWebSocketHandler() {
        return new PresenceWebSocketHandler(gameStateBridge, realDataMode);
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
        // Game sync WebSocket for player game nodes (from neuron module) - excluded
        // registry.addHandler(gameSyncWebSocketHandler(), "/ws/game-sync")
        //         .setAllowedOrigins("*");
    }

    // dora-neuron excluded: missing servlet dependency
    // @Bean
    // public GameSyncWebSocketHandler gameSyncWebSocketHandler() {
    //     return new GameSyncWebSocketHandler();
    // }
}
