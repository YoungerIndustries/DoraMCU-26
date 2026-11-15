package com.yisy.modules.doramcu.fps.server;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration properties for the FPS dedicated server.
 */
@Component
@ConfigurationProperties(prefix = "fps.server")
public class FpsServerConfig {

    /** Game loop tick rate in Hz (default 20). */
    private int tickRate = 20;

    /** Maximum players per session (default 16). */
    private int maxPlayers = 16;

    /** URL of the main Spring Boot backend (default http://localhost:8080). */
    private String backendUrl = "http://localhost:8080";

    /** WebSocket server port (default 9000). */
    private int serverPort = 9000;

    /** Unique server identifier (default fps-server-1). */
    private String serverId = "fps-server-1";

    /** Heartbeat interval in seconds (default 30). */
    private int heartbeatInterval = 30;

    /** Respawn time in seconds (default 5). */
    private int respawnTime = 5;

    /** Match time limit in seconds (default 300 = 5 minutes). */
    private int matchTimeLimit = 300;

    /** Score threshold for Capture the Point mode (default 1000). */
    private int scoreThreshold = 1000;

    public int getTickRate() {
        return tickRate;
    }

    public void setTickRate(int tickRate) {
        this.tickRate = tickRate;
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

    public void setMaxPlayers(int maxPlayers) {
        this.maxPlayers = maxPlayers;
    }

    public String getBackendUrl() {
        return backendUrl;
    }

    public void setBackendUrl(String backendUrl) {
        this.backendUrl = backendUrl;
    }

    public int getServerPort() {
        return serverPort;
    }

    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
    }

    public String getServerId() {
        return serverId;
    }

    public void setServerId(String serverId) {
        this.serverId = serverId;
    }

    public int getHeartbeatInterval() {
        return heartbeatInterval;
    }

    public void setHeartbeatInterval(int heartbeatInterval) {
        this.heartbeatInterval = heartbeatInterval;
    }

    public int getRespawnTime() {
        return respawnTime;
    }

    public void setRespawnTime(int respawnTime) {
        this.respawnTime = respawnTime;
    }

    public int getMatchTimeLimit() {
        return matchTimeLimit;
    }

    public void setMatchTimeLimit(int matchTimeLimit) {
        this.matchTimeLimit = matchTimeLimit;
    }

    public int getScoreThreshold() {
        return scoreThreshold;
    }

    public void setScoreThreshold(int scoreThreshold) {
        this.scoreThreshold = scoreThreshold;
    }
}
