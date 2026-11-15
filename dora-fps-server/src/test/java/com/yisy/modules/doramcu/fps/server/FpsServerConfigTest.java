package com.yisy.modules.doramcu.fps.server;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for FpsServerConfig.
 */
class FpsServerConfigTest {

    @Nested
    @DisplayName("Default values")
    class DefaultValuesTests {

        @Test
        @DisplayName("Default tick rate is 20")
        void defaultTickRate() {
            FpsServerConfig config = new FpsServerConfig();
            assertEquals(20, config.getTickRate());
        }

        @Test
        @DisplayName("Default max players is 16")
        void defaultMaxPlayers() {
            FpsServerConfig config = new FpsServerConfig();
            assertEquals(16, config.getMaxPlayers());
        }

        @Test
        @DisplayName("Default backend URL is localhost:8080")
        void defaultBackendUrl() {
            FpsServerConfig config = new FpsServerConfig();
            assertEquals("http://localhost:8080", config.getBackendUrl());
        }

        @Test
        @DisplayName("Default server port is 9000")
        void defaultServerPort() {
            FpsServerConfig config = new FpsServerConfig();
            assertEquals(9000, config.getServerPort());
        }

        @Test
        @DisplayName("Default server ID is fps-server-1")
        void defaultServerId() {
            FpsServerConfig config = new FpsServerConfig();
            assertEquals("fps-server-1", config.getServerId());
        }

        @Test
        @DisplayName("Default heartbeat interval is 30")
        void defaultHeartbeatInterval() {
            FpsServerConfig config = new FpsServerConfig();
            assertEquals(30, config.getHeartbeatInterval());
        }

        @Test
        @DisplayName("Default respawn time is 5")
        void defaultRespawnTime() {
            FpsServerConfig config = new FpsServerConfig();
            assertEquals(5, config.getRespawnTime());
        }

        @Test
        @DisplayName("Default match time limit is 300")
        void defaultMatchTimeLimit() {
            FpsServerConfig config = new FpsServerConfig();
            assertEquals(300, config.getMatchTimeLimit());
        }

        @Test
        @DisplayName("Default score threshold is 1000")
        void defaultScoreThreshold() {
            FpsServerConfig config = new FpsServerConfig();
            assertEquals(1000, config.getScoreThreshold());
        }
    }

    @Nested
    @DisplayName("Setters")
    class SetterTests {

        @Test
        @DisplayName("Tick rate can be changed")
        void setTickRate() {
            FpsServerConfig config = new FpsServerConfig();
            config.setTickRate(30);
            assertEquals(30, config.getTickRate());
        }

        @Test
        @DisplayName("Max players can be changed")
        void setMaxPlayers() {
            FpsServerConfig config = new FpsServerConfig();
            config.setMaxPlayers(32);
            assertEquals(32, config.getMaxPlayers());
        }

        @Test
        @DisplayName("Backend URL can be changed")
        void setBackendUrl() {
            FpsServerConfig config = new FpsServerConfig();
            config.setBackendUrl("http://backend:8080");
            assertEquals("http://backend:8080", config.getBackendUrl());
        }

        @Test
        @DisplayName("Server port can be changed")
        void setServerPort() {
            FpsServerConfig config = new FpsServerConfig();
            config.setServerPort(9001);
            assertEquals(9001, config.getServerPort());
        }

        @Test
        @DisplayName("Server ID can be changed")
        void setServerId() {
            FpsServerConfig config = new FpsServerConfig();
            config.setServerId("fps-server-2");
            assertEquals("fps-server-2", config.getServerId());
        }

        @Test
        @DisplayName("Score threshold can be changed")
        void setScoreThreshold() {
            FpsServerConfig config = new FpsServerConfig();
            config.setScoreThreshold(500);
            assertEquals(500, config.getScoreThreshold());
        }
    }
}
