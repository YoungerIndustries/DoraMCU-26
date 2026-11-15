package com.yisy.modules.doramcu.service;

import com.yisy.modules.doramcu.dto.GameEvent;
import com.yisy.modules.doramcu.dto.GameStateResponse;
import com.yisy.modules.doramcu.dto.PlayerSummary;
import com.yisy.modules.doramcu.dto.FleetDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive tests for SpectatorService.
 */
class SpectatorServiceTest {

    private GameSessionService gameSessionService;
    private SpectatorService spectatorService;

    @BeforeEach
    void setUp() {
        gameSessionService = mock(GameSessionService.class);
        spectatorService = new SpectatorService(gameSessionService);
    }

    // ============================================================
    // Spectator Registration
    // ============================================================

    @Nested
    class RegisterSpectator {

        @Test
        @DisplayName("Register spectator with ID and name")
        void registerWithIdAndName() {
            SpectatorService.SpectatorInfo info = spectatorService.registerSpectator("spec1", "Watcher");

            assertEquals("spec1", info.getSpectatorId());
            assertEquals("Watcher", info.getName());
            assertTrue(info.isActive());
            assertNotNull(info.getJoinedAt());
        }

        @Test
        @DisplayName("Register spectator with null name generates default")
        void registerWithNullName() {
            SpectatorService.SpectatorInfo info = spectatorService.registerSpectator("spec12345678", null);

            assertEquals("spec12345678", info.getSpectatorId());
            assertEquals("Spectator-spec1234", info.getName());
        }

        @Test
        @DisplayName("Register spectator with empty name generates default")
        void registerWithEmptyName() {
            SpectatorService.SpectatorInfo info = spectatorService.registerSpectator("spec1", "");

            assertEquals("Spectator-spec1", info.getName());
        }

        @Test
        @DisplayName("Register multiple spectators")
        void registerMultiple() {
            spectatorService.registerSpectator("spec1", "Watcher1");
            spectatorService.registerSpectator("spec2", "Watcher2");
            spectatorService.registerSpectator("spec3", "Watcher3");

            List<SpectatorService.SpectatorInfo> all = spectatorService.getAllSpectators();
            assertEquals(3, all.size());
        }

        @Test
        @DisplayName("Re-register spectator overwrites previous")
        void reRegisterOverwrites() {
            spectatorService.registerSpectator("spec1", "Watcher1");
            SpectatorService.SpectatorInfo info = spectatorService.registerSpectator("spec1", "Watcher2");

            assertEquals("Watcher2", info.getName());
            assertEquals(1, spectatorService.getAllSpectators().size());
        }
    }

    // ============================================================
    // Spectator Unregistration
    // ============================================================

    @Nested
    class UnregisterSpectator {

        @Test
        @DisplayName("Unregister spectator removes from list")
        void unregisterRemoves() {
            spectatorService.registerSpectator("spec1", "Watcher");
            spectatorService.unregisterSpectator("spec1");

            assertTrue(spectatorService.getSpectator("spec1").isEmpty());
            assertEquals(0, spectatorService.getAllSpectators().size());
        }

        @Test
        @DisplayName("Unregister non-existent spectator does nothing")
        void unregisterNonExistent() {
            spectatorService.unregisterSpectator("nonexistent");
            assertEquals(0, spectatorService.getAllSpectators().size());
        }

        @Test
        @DisplayName("Unregister leaves current session")
        void unregisterLeavesSession() {
            spectatorService.registerSpectator("spec1", "Watcher");
            GameStateResponse gameState = createGameState("session1");
            when(gameSessionService.getGameState(null, "session1")).thenReturn(gameState);

            spectatorService.joinSession("spec1", "session1");
            assertEquals(1, spectatorService.getSpectatorCount("session1"));

            spectatorService.unregisterSpectator("spec1");
            assertEquals(0, spectatorService.getSpectatorCount("session1"));
        }
    }

    // ============================================================
    // Get Spectator
    // ============================================================

    @Nested
    class GetSpectator {

        @Test
        @DisplayName("Get existing spectator")
        void getExisting() {
            spectatorService.registerSpectator("spec1", "Watcher");

            Optional<SpectatorService.SpectatorInfo> opt = spectatorService.getSpectator("spec1");
            assertTrue(opt.isPresent());
            assertEquals("Watcher", opt.get().getName());
        }

        @Test
        @DisplayName("Get non-existent spectator returns empty")
        void getNonExistent() {
            Optional<SpectatorService.SpectatorInfo> opt = spectatorService.getSpectator("nonexistent");
            assertTrue(opt.isEmpty());
        }
    }

    // ============================================================
    // Join Session
    // ============================================================

    @Nested
    class JoinSession {

        @Test
        @DisplayName("Join session returns redacted game state")
        void joinReturnsGameState() {
            spectatorService.registerSpectator("spec1", "Watcher");
            GameStateResponse gameState = createGameState("session1");
            when(gameSessionService.getGameState(null, "session1")).thenReturn(gameState);

            Optional<SpectatorService.SpectatorGameState> result = spectatorService.joinSession("spec1", "session1");

            assertTrue(result.isPresent());
            assertEquals("session1", result.get().getSessionId());
            assertEquals(1, result.get().getCurrentTurn());
            assertEquals(2, result.get().getPlayerCount());
        }

        @Test
        @DisplayName("Join session with unregistered spectator returns empty")
        void joinUnregisteredSpectator() {
            Optional<SpectatorService.SpectatorGameState> result = spectatorService.joinSession("unknown", "session1");
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Join non-existent session returns empty")
        void joinNonExistentSession() {
            spectatorService.registerSpectator("spec1", "Watcher");
            when(gameSessionService.getGameState(null, "session1")).thenReturn(null);

            Optional<SpectatorService.SpectatorGameState> result = spectatorService.joinSession("spec1", "session1");
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Join session adds spectator to session")
        void joinAddsSpectator() {
            spectatorService.registerSpectator("spec1", "Watcher");
            GameStateResponse gameState = createGameState("session1");
            when(gameSessionService.getGameState(null, "session1")).thenReturn(gameState);

            spectatorService.joinSession("spec1", "session1");

            assertEquals(1, spectatorService.getSpectatorCount("session1"));
            List<SpectatorService.SpectatorInfo> spectators = spectatorService.getSessionSpectators("session1");
            assertEquals(1, spectators.size());
            assertEquals("spec1", spectators.get(0).getSpectatorId());
        }

        @Test
        @DisplayName("Join new session leaves previous session")
        void joinNewLeavesPrevious() {
            spectatorService.registerSpectator("spec1", "Watcher");
            GameStateResponse gs1 = createGameState("session1");
            GameStateResponse gs2 = createGameState("session2");
            when(gameSessionService.getGameState(null, "session1")).thenReturn(gs1);
            when(gameSessionService.getGameState(null, "session2")).thenReturn(gs2);

            spectatorService.joinSession("spec1", "session1");
            assertEquals(1, spectatorService.getSpectatorCount("session1"));

            spectatorService.joinSession("spec1", "session2");
            assertEquals(0, spectatorService.getSpectatorCount("session1"));
            assertEquals(1, spectatorService.getSpectatorCount("session2"));
        }

        @Test
        @DisplayName("Join session updates spectator current session")
        void joinUpdatesCurrentSession() {
            spectatorService.registerSpectator("spec1", "Watcher");
            GameStateResponse gameState = createGameState("session1");
            when(gameSessionService.getGameState(null, "session1")).thenReturn(gameState);

            spectatorService.joinSession("spec1", "session1");

            SpectatorService.SpectatorInfo info = spectatorService.getSpectator("spec1").orElseThrow();
            assertEquals("session1", info.getCurrentSession());
        }

        @Test
        @DisplayName("Multiple spectators can join same session")
        void multipleSpectatorsJoinSameSession() {
            spectatorService.registerSpectator("spec1", "Watcher1");
            spectatorService.registerSpectator("spec2", "Watcher2");
            GameStateResponse gameState = createGameState("session1");
            when(gameSessionService.getGameState(null, "session1")).thenReturn(gameState);

            spectatorService.joinSession("spec1", "session1");
            spectatorService.joinSession("spec2", "session1");

            assertEquals(2, spectatorService.getSpectatorCount("session1"));
        }
    }

    // ============================================================
    // Leave Session
    // ============================================================

    @Nested
    class LeaveSession {

        @Test
        @DisplayName("Leave session removes spectator")
        void leaveRemovesSpectator() {
            spectatorService.registerSpectator("spec1", "Watcher");
            GameStateResponse gameState = createGameState("session1");
            when(gameSessionService.getGameState(null, "session1")).thenReturn(gameState);

            spectatorService.joinSession("spec1", "session1");
            spectatorService.leaveSession("spec1", "session1");

            assertEquals(0, spectatorService.getSpectatorCount("session1"));
        }

        @Test
        @DisplayName("Leave session clears current session")
        void leaveClearsCurrentSession() {
            spectatorService.registerSpectator("spec1", "Watcher");
            GameStateResponse gameState = createGameState("session1");
            when(gameSessionService.getGameState(null, "session1")).thenReturn(gameState);

            spectatorService.joinSession("spec1", "session1");
            spectatorService.leaveSession("spec1", "session1");

            SpectatorService.SpectatorInfo info = spectatorService.getSpectator("spec1").orElseThrow();
            assertNull(info.getCurrentSession());
        }

        @Test
        @DisplayName("Leave session removes session from map when empty")
        void leaveRemovesEmptySession() {
            spectatorService.registerSpectator("spec1", "Watcher");
            GameStateResponse gameState = createGameState("session1");
            when(gameSessionService.getGameState(null, "session1")).thenReturn(gameState);

            spectatorService.joinSession("spec1", "session1");
            spectatorService.leaveSession("spec1", "session1");

            List<String> sessions = spectatorService.getSessionsWithSpectators();
            assertFalse(sessions.contains("session1"));
        }

        @Test
        @DisplayName("Leave session keeps session when other spectators remain")
        void leaveKeepsSessionWithOthers() {
            spectatorService.registerSpectator("spec1", "Watcher1");
            spectatorService.registerSpectator("spec2", "Watcher2");
            GameStateResponse gameState = createGameState("session1");
            when(gameSessionService.getGameState(null, "session1")).thenReturn(gameState);

            spectatorService.joinSession("spec1", "session1");
            spectatorService.joinSession("spec2", "session1");
            spectatorService.leaveSession("spec1", "session1");

            assertEquals(1, spectatorService.getSpectatorCount("session1"));
        }
    }

    // ============================================================
    // Spectator Game State
    // ============================================================

    @Nested
    class SpectatorGameState {

        @Test
        @DisplayName("Get spectator game state for existing session")
        void getExistingSession() {
            GameStateResponse gameState = createGameState("session1");
            when(gameSessionService.getGameState(null, "session1")).thenReturn(gameState);

            Optional<SpectatorService.SpectatorGameState> result = spectatorService.getSpectatorGameState("session1");

            assertTrue(result.isPresent());
            assertEquals("session1", result.get().getSessionId());
            assertEquals(2, result.get().getPlayerCount());
            assertEquals(1, result.get().getFleetCount());
        }

        @Test
        @DisplayName("Get spectator game state for non-existent session")
        void getNonExistentSession() {
            when(gameSessionService.getGameState(null, "session1")).thenReturn(null);

            Optional<SpectatorService.SpectatorGameState> result = spectatorService.getSpectatorGameState("session1");
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Redacted state hides sensitive data")
        void redactedStateHidesSensitiveData() {
            GameStateResponse gameState = createGameState("session1");
            when(gameSessionService.getGameState(null, "session1")).thenReturn(gameState);

            SpectatorService.SpectatorGameState state = spectatorService.getSpectatorGameState("session1").orElseThrow();

            // Should have player summaries without resources
            List<SpectatorService.SpectatorPlayerSummary> players = state.getPlayers();
            assertEquals(2, players.size());
            assertEquals("Federation", players.get(0).getName());
            assertEquals(1, players.get(0).getFleetCount());
            assertEquals(3, players.get(0).getPlanetCount());

            // Should have fleet summaries without detailed ship data
            List<SpectatorService.SpectatorFleetSummary> fleets = state.getFleets();
            assertEquals(1, fleets.size());
            assertEquals("1", fleets.get(0).getId());
            assertEquals("Federation Fleet", fleets.get(0).getPlayerName());
            assertEquals(5, fleets.get(0).getShipCount());
        }

        @Test
        @DisplayName("Redacted state handles null fleets")
        void handlesNullFleets() {
            GameStateResponse gameState = new GameStateResponse();
            gameState.setSessionId("session1");
            gameState.setCurrentTurn(1);
            gameState.setPlayers(List.of(
                    new PlayerSummary(1L, "Federation", null, 3, 1)
            ));
            gameState.setFleets(null);
            when(gameSessionService.getGameState(null, "session1")).thenReturn(gameState);

            SpectatorService.SpectatorGameState state = spectatorService.getSpectatorGameState("session1").orElseThrow();
            assertEquals(0, state.getFleetCount());
            assertTrue(state.getFleets().isEmpty());
        }

        @Test
        @DisplayName("Redacted state handles null players")
        void handlesNullPlayers() {
            GameStateResponse gameState = new GameStateResponse();
            gameState.setSessionId("session1");
            gameState.setCurrentTurn(1);
            gameState.setPlayers(null);
            gameState.setFleets(null);
            when(gameSessionService.getGameState(null, "session1")).thenReturn(gameState);

            SpectatorService.SpectatorGameState state = spectatorService.getSpectatorGameState("session1").orElseThrow();
            assertEquals(0, state.getPlayerCount());
            assertTrue(state.getPlayers().isEmpty());
        }
    }

    // ============================================================
    // Event History
    // ============================================================

    @Nested
    class EventHistory {

        @Test
        @DisplayName("Record and retrieve events")
        void recordAndRetrieve() {
            GameEvent event1 = new GameEvent(GameEvent.EventType.TURN_CHANGED, "session1", Map.of("turn", 1));
            GameEvent event2 = new GameEvent(GameEvent.EventType.ACTION_RESULT, "session1", Map.of("action", "move"));

            spectatorService.recordEvent("session1", event1);
            spectatorService.recordEvent("session1", event2);

            List<GameEvent> history = spectatorService.getEventHistory("session1", 10);
            assertEquals(2, history.size());
            assertEquals(GameEvent.EventType.TURN_CHANGED, history.get(0).getType());
            assertEquals(GameEvent.EventType.ACTION_RESULT, history.get(1).getType());
        }

        @Test
        @DisplayName("Get recent events with count limit")
        void getRecentWithLimit() {
            for (int i = 0; i < 10; i++) {
                spectatorService.recordEvent("session1",
                        new GameEvent(GameEvent.EventType.TURN_CHANGED, "session1", Map.of("turn", i)));
            }

            List<GameEvent> recent = spectatorService.getEventHistory("session1", 3);
            assertEquals(3, recent.size());
        }

        @Test
        @DisplayName("Get history for session with no events")
        void getHistoryNoEvents() {
            List<GameEvent> history = spectatorService.getEventHistory("session1", 10);
            assertTrue(history.isEmpty());
        }

        @Test
        @DisplayName("Event history trims at max size")
        void historyTrimsAtMax() {
            for (int i = 0; i < 250; i++) {
                spectatorService.recordEvent("session1",
                        new GameEvent(GameEvent.EventType.TURN_CHANGED, "session1", Map.of("turn", i)));
            }

            List<GameEvent> history = spectatorService.getEventHistory("session1", 300);
            assertEquals(200, history.size());
        }

        @Test
        @DisplayName("Multiple sessions have separate event histories")
        void separateHistories() {
            spectatorService.recordEvent("session1",
                    new GameEvent(GameEvent.EventType.TURN_CHANGED, "session1", Map.of("turn", 1)));
            spectatorService.recordEvent("session2",
                    new GameEvent(GameEvent.EventType.ACTION_RESULT, "session2", Map.of("action", "move")));

            List<GameEvent> history1 = spectatorService.getEventHistory("session1", 10);
            List<GameEvent> history2 = spectatorService.getEventHistory("session2", 10);

            assertEquals(1, history1.size());
            assertEquals(1, history2.size());
            assertNotEquals(history1.get(0).getSessionId(), history2.get(0).getSessionId());
        }
    }

    // ============================================================
    // Session Spectators
    // ============================================================

    @Nested
    class SessionSpectators {

        @Test
        @DisplayName("Get spectators for session with no spectators")
        void getNoSpectators() {
            List<SpectatorService.SpectatorInfo> spectators = spectatorService.getSessionSpectators("session1");
            assertTrue(spectators.isEmpty());
        }

        @Test
        @DisplayName("Get spectator count for session with no spectators")
        void getCountNoSpectators() {
            assertEquals(0, spectatorService.getSpectatorCount("session1"));
        }

        @Test
        @DisplayName("Get sessions with spectators")
        void getSessionsWithSpectators() {
            spectatorService.registerSpectator("spec1", "Watcher1");
            GameStateResponse gameState = createGameState("session1");
            when(gameSessionService.getGameState(null, "session1")).thenReturn(gameState);
            spectatorService.joinSession("spec1", "session1");

            List<String> sessions = spectatorService.getSessionsWithSpectators();
            assertTrue(sessions.contains("session1"));
        }
    }

    // ============================================================
    // Statistics
    // ============================================================

    @Nested
    class Statistics {

        @Test
        @DisplayName("Get global statistics")
        void getGlobalStatistics() {
            spectatorService.registerSpectator("spec1", "Watcher1");
            spectatorService.registerSpectator("spec2", "Watcher2");
            GameStateResponse gameState = createGameState("session1");
            when(gameSessionService.getGameState(null, "session1")).thenReturn(gameState);
            spectatorService.joinSession("spec1", "session1");

            Map<String, Object> stats = spectatorService.getStatistics();

            assertEquals(2, stats.get("totalSpectators"));
            assertEquals(2, stats.get("activeSpectators"));
            assertEquals(1, stats.get("sessionsWithSpectators"));
        }

        @Test
        @DisplayName("Get session statistics")
        void getSessionStatistics() {
            spectatorService.registerSpectator("spec1", "Watcher1");
            GameStateResponse gameState = createGameState("session1");
            when(gameSessionService.getGameState(null, "session1")).thenReturn(gameState);
            spectatorService.joinSession("spec1", "session1");

            spectatorService.recordEvent("session1",
                    new GameEvent(GameEvent.EventType.TURN_CHANGED, "session1", Map.of("turn", 1)));

            Map<String, Object> stats = spectatorService.getSessionStatistics("session1");

            assertEquals("session1", stats.get("sessionId"));
            assertEquals(1, stats.get("spectatorCount"));
            assertEquals(1, stats.get("eventHistorySize"));
        }

        @Test
        @DisplayName("Statistics for empty session")
        void statisticsEmptySession() {
            Map<String, Object> stats = spectatorService.getSessionStatistics("session1");
            assertEquals(0, stats.get("spectatorCount"));
            assertEquals(0, stats.get("eventHistorySize"));
        }

        @Test
        @DisplayName("Inactive spectators not counted as active")
        void inactiveNotCounted() {
            spectatorService.registerSpectator("spec1", "Watcher1");
            SpectatorService.SpectatorInfo info = spectatorService.getSpectator("spec1").orElseThrow();
            info.setActive(false);

            Map<String, Object> stats = spectatorService.getStatistics();
            assertEquals(1, stats.get("totalSpectators"));
            assertEquals(0, stats.get("activeSpectators"));
        }
    }

    // ============================================================
    // Stale Spectators
    // ============================================================

    @Nested
    class StaleSpectators {

        @Test
        @DisplayName("Remove stale spectators")
        void removeStale() {
            spectatorService.registerSpectator("spec1", "Watcher1");
            SpectatorService.SpectatorInfo info = spectatorService.getSpectator("spec1").orElseThrow();
            info.setActive(false);

            int removed = spectatorService.removeStaleSpectators();
            assertEquals(1, removed);
            assertTrue(spectatorService.getSpectator("spec1").isEmpty());
        }

        @Test
        @DisplayName("Remove stale spectators leaves active ones")
        void removeStaleLeavesActive() {
            spectatorService.registerSpectator("spec1", "Watcher1");
            spectatorService.registerSpectator("spec2", "Watcher2");
            SpectatorService.SpectatorInfo info2 = spectatorService.getSpectator("spec2").orElseThrow();
            info2.setActive(false);

            int removed = spectatorService.removeStaleSpectators();
            assertEquals(1, removed);
            assertTrue(spectatorService.getSpectator("spec1").isPresent());
            assertTrue(spectatorService.getSpectator("spec2").isEmpty());
        }

        @Test
        @DisplayName("Remove stale spectators leaves session")
        void removeStaleLeavesSession() {
            spectatorService.registerSpectator("spec1", "Watcher1");
            GameStateResponse gameState = createGameState("session1");
            when(gameSessionService.getGameState(null, "session1")).thenReturn(gameState);
            spectatorService.joinSession("spec1", "session1");

            SpectatorService.SpectatorInfo info = spectatorService.getSpectator("spec1").orElseThrow();
            info.setActive(false);

            spectatorService.removeStaleSpectators();
            assertEquals(0, spectatorService.getSpectatorCount("session1"));
        }

        @Test
        @DisplayName("No stale spectators returns 0")
        void noStaleReturnsZero() {
            spectatorService.registerSpectator("spec1", "Watcher1");
            int removed = spectatorService.removeStaleSpectators();
            assertEquals(0, removed);
        }
    }

    // ============================================================
    // Heartbeat
    // ============================================================

    @Nested
    class Heartbeat {

        @Test
        @DisplayName("Heartbeat updates last heartbeat time")
        void heartbeatUpdatesTime() {
            spectatorService.registerSpectator("spec1", "Watcher1");
            Instant before = Instant.now();
            SpectatorService.SpectatorInfo info = spectatorService.getSpectator("spec1").orElseThrow();
            info.heartbeat();
            Instant after = Instant.now();

            assertTrue(info.getLastHeartbeat().isAfter(before) || info.getLastHeartbeat().equals(before));
            assertTrue(info.getLastHeartbeat().isBefore(after) || info.getLastHeartbeat().equals(after));
        }
    }

    // ============================================================
    // Clear All
    // ============================================================

    @Nested
    class ClearAll {

        @Test
        @DisplayName("Clear all removes all data")
        void clearAll() {
            spectatorService.registerSpectator("spec1", "Watcher1");
            GameStateResponse gameState = createGameState("session1");
            when(gameSessionService.getGameState(null, "session1")).thenReturn(gameState);
            spectatorService.joinSession("spec1", "session1");
            spectatorService.recordEvent("session1",
                    new GameEvent(GameEvent.EventType.TURN_CHANGED, "session1", Map.of("turn", 1)));

            spectatorService.clearAll();

            assertEquals(0, spectatorService.getAllSpectators().size());
            assertEquals(0, spectatorService.getSessionsWithSpectators().size());
            assertEquals(0, spectatorService.getEventHistory("session1", 10).size());
        }
    }

    // ============================================================
    // SpectatorInfo Builder
    // ============================================================

    @Nested
    class SpectatorInfoBuilder {

        @Test
        @DisplayName("Builder with all fields")
        void builderWithAllFields() {
            Instant now = Instant.now();
            SpectatorService.SpectatorInfo info = SpectatorService.SpectatorInfo.builder("spec1")
                    .name("Watcher")
                    .joinedAt(now)
                    .currentSession("session1")
                    .build();

            assertEquals("spec1", info.getSpectatorId());
            assertEquals("Watcher", info.getName());
            assertEquals(now, info.getJoinedAt());
            assertEquals("session1", info.getCurrentSession());
            assertTrue(info.isActive());
        }

        @Test
        @DisplayName("Builder sets active to true by default")
        void builderActiveDefault() {
            SpectatorService.SpectatorInfo info = SpectatorService.SpectatorInfo.builder("spec1").build();
            assertTrue(info.isActive());
        }

        @Test
        @DisplayName("Set active false")
        void setActiveFalse() {
            SpectatorService.SpectatorInfo info = SpectatorService.SpectatorInfo.builder("spec1").build();
            info.setActive(false);
            assertFalse(info.isActive());
        }

        @Test
        @DisplayName("Set current session")
        void setCurrentSession() {
            SpectatorService.SpectatorInfo info = SpectatorService.SpectatorInfo.builder("spec1").build();
            info.setCurrentSession("session1");
            assertEquals("session1", info.getCurrentSession());
        }

        @Test
        @DisplayName("Stale spectator when inactive")
        void staleWhenInactive() {
            SpectatorService.SpectatorInfo info = SpectatorService.SpectatorInfo.builder("spec1").build();
            info.setActive(false);
            assertTrue(info.isStale());
        }

        @Test
        @DisplayName("Not stale when active and recent heartbeat")
        void notStaleWhenActive() {
            SpectatorService.SpectatorInfo info = SpectatorService.SpectatorInfo.builder("spec1").build();
            assertFalse(info.isStale());
        }
    }

    // ============================================================
    // SpectatorGameState Builder
    // ============================================================

    @Nested
    class SpectatorGameStateBuilder {

        @Test
        @DisplayName("Builder with all fields")
        void builderWithAllFields() {
            SpectatorService.SpectatorGameState state = SpectatorService.SpectatorGameState.builder()
                    .sessionId("session1")
                    .currentTurn(5)
                    .playerCount(3)
                    .currentPlayerName("Federation")
                    .fleetCount(10)
                    .build();

            assertEquals("session1", state.getSessionId());
            assertEquals(5, state.getCurrentTurn());
            assertEquals(3, state.getPlayerCount());
            assertEquals("Federation", state.getCurrentPlayerName());
            assertEquals(10, state.getFleetCount());
        }

        @Test
        @DisplayName("Builder with players and fleets")
        void builderWithPlayersAndFleets() {
            SpectatorService.SpectatorGameState state = SpectatorService.SpectatorGameState.builder()
                    .players(List.of(
                            new SpectatorService.SpectatorPlayerSummary("Federation", 2, 5),
                            new SpectatorService.SpectatorPlayerSummary("Romulan", 1, 3)
                    ))
                    .fleets(List.of(
                            new SpectatorService.SpectatorFleetSummary("1", "Federation Fleet", 5, "A4")
                    ))
                    .build();

            assertEquals(2, state.getPlayers().size());
            assertEquals(1, state.getFleets().size());
        }

        @Test
        @DisplayName("Builder with metadata")
        void builderWithMetadata() {
            SpectatorService.SpectatorGameState state = SpectatorService.SpectatorGameState.builder()
                    .metadata(Map.of("key", "value", "count", 42))
                    .build();

            assertEquals("value", state.getMetadata().get("key"));
            assertEquals(42, state.getMetadata().get("count"));
        }

        @Test
        @DisplayName("Players list is unmodifiable")
        void playersUnmodifiable() {
            SpectatorService.SpectatorGameState state = SpectatorService.SpectatorGameState.builder().build();
            assertThrows(UnsupportedOperationException.class, () -> state.getPlayers().add(
                    new SpectatorService.SpectatorPlayerSummary("Test", 0, 0)));
        }

        @Test
        @DisplayName("Fleets list is unmodifiable")
        void fleetsUnmodifiable() {
            SpectatorService.SpectatorGameState state = SpectatorService.SpectatorGameState.builder().build();
            assertThrows(UnsupportedOperationException.class, () -> state.getFleets().add(
                    new SpectatorService.SpectatorFleetSummary("1", "Test", 0, "A1")));
        }

        @Test
        @DisplayName("Metadata map is unmodifiable")
        void metadataUnmodifiable() {
            SpectatorService.SpectatorGameState state = SpectatorService.SpectatorGameState.builder().build();
            assertThrows(UnsupportedOperationException.class, () -> state.getMetadata().put("key", "value"));
        }
    }

    // ============================================================
    // SpectatorPlayerSummary
    // ============================================================

    @Nested
    class SpectatorPlayerSummaryTest {

        @Test
        @DisplayName("Constructor sets all fields")
        void constructor() {
            SpectatorService.SpectatorPlayerSummary summary =
                    new SpectatorService.SpectatorPlayerSummary("Federation", 5, 10);

            assertEquals("Federation", summary.getName());
            assertEquals(5, summary.getFleetCount());
            assertEquals(10, summary.getPlanetCount());
        }
    }

    // ============================================================
    // SpectatorFleetSummary
    // ============================================================

    @Nested
    class SpectatorFleetSummaryTest {

        @Test
        @DisplayName("Constructor sets all fields")
        void constructor() {
            SpectatorService.SpectatorFleetSummary summary =
                    new SpectatorService.SpectatorFleetSummary("1", "Federation Fleet", 5, "A4");

            assertEquals("1", summary.getId());
            assertEquals("Federation Fleet", summary.getPlayerName());
            assertEquals(5, summary.getShipCount());
            assertEquals("A4", summary.getSector());
        }
    }

    // ============================================================
    // Integration Scenarios
    // ============================================================

    @Nested
    class IntegrationScenarios {

        @Test
        @DisplayName("Full workflow: register, join, view state, leave, unregister")
        void fullWorkflow() {
            // Register
            SpectatorService.SpectatorInfo info = spectatorService.registerSpectator("spec1", "Watcher");
            assertEquals("Watcher", info.getName());

            // Join session
            GameStateResponse gameState = createGameState("session1");
            when(gameSessionService.getGameState(null, "session1")).thenReturn(gameState);
            Optional<SpectatorService.SpectatorGameState> state = spectatorService.joinSession("spec1", "session1");
            assertTrue(state.isPresent());
            assertEquals("session1", state.get().getSessionId());

            // View state
            assertEquals(2, state.get().getPlayerCount());
            assertEquals(1, state.get().getFleetCount());

            // Record events
            spectatorService.recordEvent("session1",
                    new GameEvent(GameEvent.EventType.TURN_CHANGED, "session1", Map.of("turn", 2)));
            assertEquals(1, spectatorService.getEventHistory("session1", 10).size());

            // Leave session
            spectatorService.leaveSession("spec1", "session1");
            assertEquals(0, spectatorService.getSpectatorCount("session1"));

            // Unregister
            spectatorService.unregisterSpectator("spec1");
            assertTrue(spectatorService.getSpectator("spec1").isEmpty());
        }

        @Test
        @DisplayName("Multiple spectators watch same session")
        void multipleSpectatorsSameSession() {
            spectatorService.registerSpectator("spec1", "Watcher1");
            spectatorService.registerSpectator("spec2", "Watcher2");
            spectatorService.registerSpectator("spec3", "Watcher3");

            GameStateResponse gameState = createGameState("session1");
            when(gameSessionService.getGameState(null, "session1")).thenReturn(gameState);

            spectatorService.joinSession("spec1", "session1");
            spectatorService.joinSession("spec2", "session1");
            spectatorService.joinSession("spec3", "session1");

            assertEquals(3, spectatorService.getSpectatorCount("session1"));

            // One leaves
            spectatorService.leaveSession("spec2", "session1");
            assertEquals(2, spectatorService.getSpectatorCount("session1"));

            // Statistics reflect correct count
            Map<String, Object> stats = spectatorService.getSessionStatistics("session1");
            assertEquals(2, stats.get("spectatorCount"));
        }

        @Test
        @DisplayName("Spectator switches between sessions")
        void spectatorSwitchesSessions() {
            spectatorService.registerSpectator("spec1", "Watcher");

            GameStateResponse gs1 = createGameState("session1");
            GameStateResponse gs2 = createGameState("session2");
            when(gameSessionService.getGameState(null, "session1")).thenReturn(gs1);
            when(gameSessionService.getGameState(null, "session2")).thenReturn(gs2);

            spectatorService.joinSession("spec1", "session1");
            assertEquals(1, spectatorService.getSpectatorCount("session1"));
            assertEquals(0, spectatorService.getSpectatorCount("session2"));

            spectatorService.joinSession("spec1", "session2");
            assertEquals(0, spectatorService.getSpectatorCount("session1"));
            assertEquals(1, spectatorService.getSpectatorCount("session2"));

            SpectatorService.SpectatorInfo info = spectatorService.getSpectator("spec1").orElseThrow();
            assertEquals("session2", info.getCurrentSession());
        }
    }

    // ============================================================
    // Helper Methods
    // ============================================================

    private GameStateResponse createGameState(String sessionId) {
        GameStateResponse gameState = new GameStateResponse();
        gameState.setSessionId(sessionId);
        gameState.setCurrentTurn(1);
        gameState.setCurrentPlayerName("Federation");
        gameState.setPlayers(List.of(
                new PlayerSummary(1L, "Federation", null, 3, 1),
                new PlayerSummary(2L, "Romulan", null, 2, 1)
        ));
        FleetDto fleet = new FleetDto();
        fleet.setId(1);
        fleet.setName("Federation Fleet");
        fleet.setShipCount(5);
        fleet.setSystemId(1);
        fleet.setSubSectorRow(4);
        fleet.setSubSectorCol(5);
        gameState.setFleets(List.of(fleet));
        return gameState;
    }
}
