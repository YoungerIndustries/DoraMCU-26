package com.yisy.modules.doramcu.service.session;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yisy.modules.doramcu.hex.Game;
import com.yisy.modules.doramcu.model.session.GameSession;
import com.yisy.modules.doramcu.model.session.PersistedGameSession;
import com.yisy.modules.doramcu.repository.session.PersistedGameSessionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Scheduled service that periodically snapshots all active game sessions
 * to the database so they survive server crashes during active gameplay.
 * <p>
 * Runs every 5 minutes by default (configurable via {@code doramcu.snapshot.interval}).
 * Each snapshot serializes the in-memory game state and persists it to
 * {@link PersistedGameSession}, incrementing the snapshot count.
 * </p>
 * <p>
 * A failed snapshot for one session does not stop the scheduler from
 * snapshotting other sessions. The scheduler can be disabled via
 * {@code doramcu.snapshot.enabled=false}.
 * </p>
 */
@Service
public class GameSnapshotScheduler {

    private static final Logger log = LoggerFactory.getLogger(GameSnapshotScheduler.class);

    private final GameSessionService gameSessionService;
    private final PersistedGameSessionRepository persistedGameSessionRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${doramcu.snapshot.enabled:true}")
    boolean snapshotEnabled;

    @Value("${doramcu.snapshot.interval:300000}")
    long snapshotInterval;

    public GameSnapshotScheduler(GameSessionService gameSessionService,
                                 PersistedGameSessionRepository persistedGameSessionRepository) {
        this.gameSessionService = gameSessionService;
        this.persistedGameSessionRepository = persistedGameSessionRepository;
    }

    /**
     * Periodically snapshot all active game sessions to the database.
     * Skips sessions with no players to avoid wasting resources.
     */
    @Scheduled(fixedDelayString = "${doramcu.snapshot.interval:300000}")
    public void snapshotAllSessions() {
        if (!snapshotEnabled) {
            return;
        }
        if (persistedGameSessionRepository == null) {
            return;
        }

        int snapped = 0;
        int skipped = 0;
        int failed = 0;

        for (GameSession session : gameSessionService.getAllSessions().values()) {
            if (session.getPlayerIds().isEmpty()) {
                skipped++;
                continue;
            }
            try {
                snapshotSession(session);
                snapped++;
            } catch (Exception e) {
                failed++;
                log.error("Failed to snapshot session {}: {}", session.getSessionId(), e.getMessage());
            }
        }

        if (snapped > 0) {
            log.info("Snapshot complete: {} sessions saved, {} skipped (no players), {} failed",
                    snapped, skipped, failed);
        }
    }

    /**
     * Trigger an immediate snapshot of all active sessions.
     * Used by the admin API endpoint for on-demand snapshots.
     *
     * @return a map with snapshot results (snapped, skipped, failed counts)
     */
    @Transactional
    public Map<String, Object> snapshotNow() {
        int snapped = 0;
        int skipped = 0;
        int failed = 0;

        for (GameSession session : gameSessionService.getAllSessions().values()) {
            if (session.getPlayerIds().isEmpty()) {
                skipped++;
                continue;
            }
            try {
                snapshotSession(session);
                snapped++;
            } catch (Exception e) {
                failed++;
                log.error("Failed to snapshot session {}: {}", session.getSessionId(), e.getMessage());
            }
        }

        log.info("On-demand snapshot: {} sessions saved, {} skipped, {} failed", snapped, skipped, failed);
        Map<String, Object> result = new java.util.LinkedHashMap<>();
        result.put("snapped", snapped);
        result.put("skipped", skipped);
        result.put("failed", failed);
        result.put("timestamp", LocalDateTime.now().toString());
        return result;
    }

    /**
     * Snapshot a single game session to the database.
     * Serializes the game state, updates the persisted record, and increments the snapshot count.
     */
    @Transactional
    void snapshotSession(GameSession session) {
        if (persistedGameSessionRepository == null) {
            return;
        }

        String sessionId = session.getSessionId();
        String sessionName = session.getName();

        // Serialize the game state
        final String[] gameStateJson = new String[1];
        if (session.getGame() instanceof Game) {
            try {
                Map<String, Object> gameState = ((Game) session.getGame()).serializeGameState();
                gameStateJson[0] = objectMapper.writeValueAsString(gameState);
            } catch (JsonProcessingException e) {
                log.warn("Failed to serialize game state for session {}: {}", sessionId, e.getMessage());
            }
        }

        persistedGameSessionRepository.findBySessionId(sessionId).ifPresentOrElse(
                persisted -> {
                    persisted.setSessionName(sessionName);
                    persisted.setPlayerIds(session.getPlayerIds().toString());
                    if (gameStateJson[0] != null) {
                        persisted.setGameState(gameStateJson[0]);
                    }
                    persisted.setUpdatedAt(LocalDateTime.now());
                    persisted.setLastActivityAt(LocalDateTime.now());
                    persisted.incrementSnapshotCount();
                    persistedGameSessionRepository.save(persisted);
                },
                () -> {
                    // Session not yet persisted — create the record
                    PersistedGameSession persisted = new PersistedGameSession();
                    persisted.setSessionId(sessionId);
                    persisted.setSessionName(sessionName);
                    persisted.setPlayerIds(session.getPlayerIds().toString());
                    persisted.setGameState(gameStateJson[0]);
                    persisted.setCreatedAt(LocalDateTime.now());
                    persisted.setUpdatedAt(LocalDateTime.now());
                    persisted.setLastActivityAt(LocalDateTime.now());
                    persisted.setSnapshotCount(1);
                    persistedGameSessionRepository.save(persisted);
                }
        );
    }
}
