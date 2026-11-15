package com.yisy.modules.doramcu.config;

import com.yisy.modules.doramcu.controller.FleetTrackerWebSocketHandler;
import com.yisy.modules.doramcu.controller.NarrativeWebSocketHandler;
import com.yisy.modules.doramcu.controller.PresenceWebSocketHandler;
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
 * </ul>
 * </p>
 */
@Configuration
@EnableWebSocket
public class Galaxy3DWebSocketConfig implements WebSocketConfigurer {

    @Bean
    public FleetTrackerWebSocketHandler fleetTrackerWebSocketHandler() {
        return new FleetTrackerWebSocketHandler();
    }

    @Bean
    public PresenceWebSocketHandler presenceWebSocketHandler() {
        return new PresenceWebSocketHandler();
    }

    @Bean
    public NarrativeWebSocketHandler narrativeWebSocketHandler() {
        return new NarrativeWebSocketHandler();
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(fleetTrackerWebSocketHandler(), "/ws/fleet-tracker")
                .setAllowedOrigins("*");
        registry.addHandler(presenceWebSocketHandler(), "/ws/presence")
                .setAllowedOrigins("*");
        registry.addHandler(narrativeWebSocketHandler(), "/ws/narrative")
                .setAllowedOrigins("*");
    }
}
