package com.yisy.modules.doramcu.fps.server;

import com.yisy.modules.doramcu.fps.core.FpsGameServer;
import com.yisy.modules.doramcu.fps.core.FpsSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import jakarta.annotation.PreDestroy;
import java.util.List;

/**
 * Manages the FPS dedicated server lifecycle:
 * registers with the backend, starts the game loop, sends heartbeats,
 * and reports match events.
 */
@Component
public class FpsServerRegistration {

    private static final Logger log = LoggerFactory.getLogger(FpsServerRegistration.class);

    private final FpsServerConfig config;
    private final FpsBackendClient backendClient;
    private final FpsGameServer gameServer;

    public FpsServerRegistration(FpsServerConfig config, FpsBackendClient backendClient) {
        this.config = config;
        this.backendClient = backendClient;
        this.gameServer = new FpsGameServer(config.getTickRate());
    }

    /**
     * Called when the application is ready: register with backend and start game loop.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        log.info("FPS Dedicated Server starting: id={}, port={}, tickRate={}",
                config.getServerId(), config.getServerPort(), config.getTickRate());

        // Register with backend
        boolean registered = backendClient.registerServer();
        if (registered) {
            log.info("Server registered with backend: {}", config.getBackendUrl());
        } else {
            log.warn("Failed to register with backend — running in standalone mode");
        }

        // Start the game loop
        gameServer.start();
        log.info("FPS game loop started");
    }

    /**
     * Send periodic heartbeat to the backend.
     */
    @Scheduled(fixedDelayString = "${fps.server.heartbeat-interval:30000}")
    public void heartbeat() {
        backendClient.sendHeartbeat();
    }

    /**
     * Get the game server instance for WebSocket handler injection.
     */
    public FpsGameServer getGameServer() {
        return gameServer;
    }

    /**
     * Get current server status.
     */
    public ServerStatus getStatus() {
        List<FpsSession> sessions = gameServer.getActiveSessions();
        int totalPlayers = 0;
        for (FpsSession s : sessions) {
            totalPlayers += s.getPlayerCount();
        }
        return new ServerStatus(config.getServerId(), sessions.size(), totalPlayers);
    }

    @PreDestroy
    public void shutdown() {
        gameServer.stop();
        log.info("FPS Dedicated Server stopped");
    }

    /**
     * Server status snapshot.
     */
    public record ServerStatus(String serverId, int activeSessions, int totalPlayers) {
    }
}
