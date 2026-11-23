package com.yisy.modules.doramcu.service.session;

import com.yisy.modules.doramcu.hex.Game;
import com.yisy.modules.doramcu.model.session.GameSession;
import com.yisy.modules.doramcu.model.session.PersistedGameSession;
import com.yisy.modules.doramcu.repository.session.PersistedGameSessionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("GameSnapshotScheduler Tests")
@ExtendWith(MockitoExtension.class)
class GameSnapshotSchedulerTest {

    @Mock
    private GameSessionService gameSessionService;

    @Mock
    private PersistedGameSessionRepository persistedGameSessionRepository;

    private GameSnapshotScheduler scheduler;

    @BeforeEach
    void setUp() {
        scheduler = new GameSnapshotScheduler(gameSessionService, persistedGameSessionRepository);
        scheduler.snapshotEnabled = true;
        scheduler.snapshotInterval = 300000;
    }

    // ---- snapshotAllSessions ----

    @Test
    @DisplayName("snapshotAllSessions - should skip when disabled")
    void snapshotAllSessions_shouldSkipWhenDisabled() {
        scheduler.snapshotEnabled = false;

        scheduler.snapshotAllSessions();

        verify(gameSessionService, never()).getAllSessions();
    }

    @Test
    @DisplayName("snapshotAllSessions - should skip when repository is null")
    void snapshotAllSessions_shouldSkipWhenRepositoryNull() {
        scheduler = new GameSnapshotScheduler(gameSessionService, null);
        scheduler.snapshotEnabled = true;

        scheduler.snapshotAllSessions();

        // Should not throw
    }

    @Test
    @DisplayName("snapshotAllSessions - should skip empty session map")
    void snapshotAllSessions_shouldSkipEmptyMap() {
        Map<String, GameSession> sessions = new HashMap<>();
        when(gameSessionService.getAllSessions()).thenReturn(sessions);

        scheduler.snapshotAllSessions();

        verify(persistedGameSessionRepository, never()).save(any());
    }

    @Test
    @DisplayName("snapshotAllSessions - should skip sessions with no players")
    void snapshotAllSessions_shouldSkipNoPlayers() {
        Game game = new Game("Test Game");
        GameSession session = new GameSession("session1", "Test Game", game);
        Map<String, GameSession> sessions = new HashMap<>();
        sessions.put("session1", session);
        when(gameSessionService.getAllSessions()).thenReturn(sessions);

        scheduler.snapshotAllSessions();

        verify(persistedGameSessionRepository, never()).findBySessionId(anyString());
    }

    @Test
    @DisplayName("snapshotAllSessions - should snapshot session with players")
    void snapshotAllSessions_shouldSnapshotWithPlayers() {
        Game game = new Game("Test Game");
        GameSession session = new GameSession("session1", "Test Game", game);
        session.addPlayer(1L);
        Map<String, GameSession> sessions = new HashMap<>();
        sessions.put("session1", session);
        when(gameSessionService.getAllSessions()).thenReturn(sessions);
        when(persistedGameSessionRepository.findBySessionId("session1"))
                .thenReturn(Optional.empty());

        scheduler.snapshotAllSessions();

        ArgumentCaptor<PersistedGameSession> captor = ArgumentCaptor.forClass(PersistedGameSession.class);
        verify(persistedGameSessionRepository).save(captor.capture());
        PersistedGameSession saved = captor.getValue();
        assertEquals("session1", saved.getSessionId());
        assertEquals("Test Game", saved.getSessionName());
        assertEquals(1, saved.getSnapshotCount());
    }

    @Test
    @DisplayName("snapshotAllSessions - should update existing persisted session")
    void snapshotAllSessions_shouldUpdateExisting() {
        Game game = new Game("Test Game");
        GameSession session = new GameSession("session1", "Test Game", game);
        session.addPlayer(1L);
        Map<String, GameSession> sessions = new HashMap<>();
        sessions.put("session1", session);
        when(gameSessionService.getAllSessions()).thenReturn(sessions);

        PersistedGameSession existing = new PersistedGameSession();
        existing.setSessionId("session1");
        existing.setSessionName("Old Name");
        existing.setSnapshotCount(3);
        when(persistedGameSessionRepository.findBySessionId("session1"))
                .thenReturn(Optional.of(existing));

        scheduler.snapshotAllSessions();

        ArgumentCaptor<PersistedGameSession> captor = ArgumentCaptor.forClass(PersistedGameSession.class);
        verify(persistedGameSessionRepository).save(captor.capture());
        PersistedGameSession saved = captor.getValue();
        assertEquals(4, saved.getSnapshotCount());
        assertEquals("Test Game", saved.getSessionName());
    }

    @Test
    @DisplayName("snapshotAllSessions - should handle multiple sessions")
    void snapshotAllSessions_shouldHandleMultipleSessions() {
        Game game1 = new Game("Game 1");
        GameSession session1 = new GameSession("s1", "Game 1", game1);
        session1.addPlayer(1L);

        Game game2 = new Game("Game 2");
        GameSession session2 = new GameSession("s2", "Game 2", game2);
        session2.addPlayer(2L);

        Map<String, GameSession> sessions = new HashMap<>();
        sessions.put("s1", session1);
        sessions.put("s2", session2);
        when(gameSessionService.getAllSessions()).thenReturn(sessions);
        when(persistedGameSessionRepository.findBySessionId("s1")).thenReturn(Optional.empty());
        when(persistedGameSessionRepository.findBySessionId("s2")).thenReturn(Optional.empty());

        scheduler.snapshotAllSessions();

        verify(persistedGameSessionRepository, times(2)).save(any(PersistedGameSession.class));
    }

    @Test
    @DisplayName("snapshotAllSessions - should continue after one session fails")
    void snapshotAllSessions_shouldContinueAfterFailure() {
        Game game1 = new Game("Game 1");
        GameSession session1 = new GameSession("s1", "Game 1", game1);
        session1.addPlayer(1L);

        Game game2 = new Game("Game 2");
        GameSession session2 = new GameSession("s2", "Game 2", game2);
        session2.addPlayer(2L);

        Map<String, GameSession> sessions = new HashMap<>();
        sessions.put("s1", session1);
        sessions.put("s2", session2);
        when(gameSessionService.getAllSessions()).thenReturn(sessions);

        // First session throws an exception
        when(persistedGameSessionRepository.findBySessionId("s1"))
                .thenThrow(new RuntimeException("DB error"));
        when(persistedGameSessionRepository.findBySessionId("s2")).thenReturn(Optional.empty());

        scheduler.snapshotAllSessions();

        // Second session should still be saved
        ArgumentCaptor<PersistedGameSession> captor = ArgumentCaptor.forClass(PersistedGameSession.class);
        verify(persistedGameSessionRepository).save(captor.capture());
        assertEquals("s2", captor.getValue().getSessionId());
    }

    // ---- snapshotNow ----

    @Test
    @DisplayName("snapshotNow - should return result map with counts")
    void snapshotNow_shouldReturnResultMap() {
        Game game = new Game("Test Game");
        GameSession session = new GameSession("session1", "Test Game", game);
        session.addPlayer(1L);
        Map<String, GameSession> sessions = new HashMap<>();
        sessions.put("session1", session);
        when(gameSessionService.getAllSessions()).thenReturn(sessions);
        when(persistedGameSessionRepository.findBySessionId("session1"))
                .thenReturn(Optional.empty());

        Map<String, Object> result = scheduler.snapshotNow();

        assertEquals(1, result.get("snapped"));
        assertEquals(0, result.get("skipped"));
        assertEquals(0, result.get("failed"));
        assertNotNull(result.get("timestamp"));
    }

    @Test
    @DisplayName("snapshotNow - should count skipped sessions")
    void snapshotNow_shouldCountSkipped() {
        Game game = new Game("Test Game");
        GameSession session = new GameSession("session1", "Test Game", game);
        // No players added
        Map<String, GameSession> sessions = new HashMap<>();
        sessions.put("session1", session);
        when(gameSessionService.getAllSessions()).thenReturn(sessions);

        Map<String, Object> result = scheduler.snapshotNow();

        assertEquals(0, result.get("snapped"));
        assertEquals(1, result.get("skipped"));
    }

    // ---- snapshotSession ----

    @Test
    @DisplayName("snapshotSession - should set timestamps")
    void snapshotSession_shouldSetTimestamps() {
        Game game = new Game("Test Game");
        GameSession session = new GameSession("session1", "Test Game", game);
        session.addPlayer(1L);
        when(persistedGameSessionRepository.findBySessionId("session1"))
                .thenReturn(Optional.empty());

        scheduler.snapshotSession(session);

        ArgumentCaptor<PersistedGameSession> captor = ArgumentCaptor.forClass(PersistedGameSession.class);
        verify(persistedGameSessionRepository).save(captor.capture());
        PersistedGameSession saved = captor.getValue();
        assertNotNull(saved.getCreatedAt());
        assertNotNull(saved.getUpdatedAt());
        assertNotNull(saved.getLastActivityAt());
    }

    @Test
    @DisplayName("snapshotSession - should serialize game state")
    void snapshotSession_shouldSerializeGameState() {
        Game game = new Game("Test Game");
        GameSession session = new GameSession("session1", "Test Game", game);
        session.addPlayer(1L);
        when(persistedGameSessionRepository.findBySessionId("session1"))
                .thenReturn(Optional.empty());

        scheduler.snapshotSession(session);

        ArgumentCaptor<PersistedGameSession> captor = ArgumentCaptor.forClass(PersistedGameSession.class);
        verify(persistedGameSessionRepository).save(captor.capture());
        PersistedGameSession saved = captor.getValue();
        assertNotNull(saved.getGameState());
        assertTrue(saved.getGameState().contains("\"name\":\"Test Game\""));
    }
}
