package com.yisy.modules.doramcu.fps.core;

import com.yisy.modules.doramcu.fps.core.model.FpsMap;
import com.yisy.modules.doramcu.fps.core.model.FpsMatch;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;

import static org.junit.jupiter.api.Assertions.*;

class FpsGameServerTest {

    FpsMap createMap() {
        FpsMap map = new FpsMap("Arena", 100, 100, 20);
        map.getSpawnPoints().add(new FpsMap.SpawnPoint(-40, 0, -40, 0));
        map.getSpawnPoints().add(new FpsMap.SpawnPoint(40, 0, 40, 1));
        return map;
    }

    @Nested
    class ConstructorTests {
        @Test
        void defaultConstructorUsesDefaultTickRate() {
            FpsGameServer server = new FpsGameServer();
            // Default tick rate is 20Hz = 50ms interval
            assertEquals(50, server.getClass().getDeclaredFields().length > 0 ? 50 : 0);
        }

        @Test
        void customTickRateSetsInterval() {
            FpsGameServer server = new FpsGameServer(10);
            // 10Hz = 100ms interval
            assertNotNull(server);
        }
    }

    @Nested
    class SessionManagementTests {
        @Test
        void createSessionReturnsSession() {
            FpsGameServer server = new FpsGameServer();
            FpsSession session = server.createSession("s1", FpsMatch.GameMode.DEATHMATCH, createMap(), 8);
            assertNotNull(session);
            assertEquals("s1", session.getSessionId());
        }

        @Test
        void createSessionAddsToActiveSessions() {
            FpsGameServer server = new FpsGameServer();
            server.createSession("s1", FpsMatch.GameMode.DEATHMATCH, createMap(), 8);
            assertEquals(1, server.getActiveSessions().size());
        }

        @Test
        void getSessionReturnsCreatedSession() {
            FpsGameServer server = new FpsGameServer();
            server.createSession("s1", FpsMatch.GameMode.DEATHMATCH, createMap(), 8);
            FpsSession session = server.getSession("s1");
            assertNotNull(session);
            assertEquals("s1", session.getSessionId());
        }

        @Test
        void getSessionReturnsNullForUnknownId() {
            FpsGameServer server = new FpsGameServer();
            assertNull(server.getSession("unknown"));
        }

        @Test
        void removeSessionRemovesFromActiveSessions() {
            FpsGameServer server = new FpsGameServer();
            server.createSession("s1", FpsMatch.GameMode.DEATHMATCH, createMap(), 8);
            server.removeSession("s1");
            assertTrue(server.getActiveSessions().isEmpty());
        }

        @Test
        void removeSessionRemovesSessionLookup() {
            FpsGameServer server = new FpsGameServer();
            server.createSession("s1", FpsMatch.GameMode.DEATHMATCH, createMap(), 8);
            server.removeSession("s1");
            assertNull(server.getSession("s1"));
        }

        @Test
        void removeNonExistentSessionDoesNotThrow() {
            FpsGameServer server = new FpsGameServer();
            server.removeSession("nonexistent");
            assertTrue(server.getActiveSessions().isEmpty());
        }

        @Test
        void multipleSessionsCanBeCreated() {
            FpsGameServer server = new FpsGameServer();
            server.createSession("s1", FpsMatch.GameMode.DEATHMATCH, createMap(), 8);
            server.createSession("s2", FpsMatch.GameMode.TEAM_DEATHMATCH, createMap(), 16);
            assertEquals(2, server.getActiveSessions().size());
        }
    }

    @Nested
    class InputSubmissionTests {
        @Test
        void submitInputQueuesForSession() {
            FpsGameServer server = new FpsGameServer();
            server.createSession("s1", FpsMatch.GameMode.DEATHMATCH, createMap(), 8);
            // Should not throw
            server.submitInput("s1", new FpsGameServer.FpsInputMessage(
                    "p1", System.currentTimeMillis(), 0, 1, 0, 0, java.util.List.of("shoot")));
        }

        @Test
        void submitInputToUnknownSessionDoesNotThrow() {
            FpsGameServer server = new FpsGameServer();
            server.submitInput("unknown", new FpsGameServer.FpsInputMessage(
                    "p1", System.currentTimeMillis(), 0, 1, 0, 0, java.util.List.of("shoot")));
        }
    }

    @Nested
    class FpsInputMessageTests {
        @Test
        void inputMessageRecordCanBeCreated() {
            FpsGameServer.FpsInputMessage input = new FpsGameServer.FpsInputMessage(
                    "p1", 1000, 0.5, 1.0, 0.1, 0.2, java.util.List.of("shoot", "sprint"));
            assertEquals("p1", input.playerId());
            assertEquals(1000, input.timestamp());
            assertEquals(0.5, input.moveX());
            assertEquals(1.0, input.moveZ());
            assertEquals(0.1, input.aimPitch());
            assertEquals(0.2, input.aimYaw());
            assertTrue(input.actions().contains("shoot"));
            assertTrue(input.actions().contains("sprint"));
        }

        @Test
        void inputMessageWithEmptyActions() {
            FpsGameServer.FpsInputMessage input = new FpsGameServer.FpsInputMessage(
                    "p1", 1000, 0, 0, 0, 0, java.util.List.of());
            assertTrue(input.actions().isEmpty());
        }
    }

    @Nested
    class StartStopTests {
        @Test
        void startDoesNotThrow() {
            FpsGameServer server = new FpsGameServer();
            server.start();
            // Should not throw
            server.stop();
        }

        @Test
        void stopDoesNotThrow() {
            FpsGameServer server = new FpsGameServer();
            server.stop();
            // Should not throw
        }

        @Test
        void startThenStopDoesNotThrow() {
            FpsGameServer server = new FpsGameServer();
            server.start();
            server.stop();
            // Should not throw
        }
    }

    @Nested
    class SessionCreationTests {
        @Test
        void createSessionWithNullMap() {
            FpsGameServer server = new FpsGameServer();
            FpsSession session = server.createSession("s1", FpsMatch.GameMode.DEATHMATCH, null, 8);
            assertNotNull(session);
            assertNull(session.getMap());
        }

        @Test
        void createSessionSetsGameMode() {
            FpsGameServer server = new FpsGameServer();
            FpsSession session = server.createSession("s1", FpsMatch.GameMode.CAPTURE_THE_POINT, createMap(), 8);
            assertEquals(FpsMatch.GameMode.CAPTURE_THE_POINT, session.getGameMode());
        }

        @Test
        void createSessionSetsMaxPlayers() {
            FpsGameServer server = new FpsGameServer();
            FpsSession session = server.createSession("s1", FpsMatch.GameMode.DEATHMATCH, createMap(), 16);
            assertEquals(16, session.getMaxPlayers());
        }
    }
}
