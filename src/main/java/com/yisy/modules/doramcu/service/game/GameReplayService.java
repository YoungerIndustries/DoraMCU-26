package com.yisy.modules.doramcu.service.game;
import com.yisy.modules.doramcu.service.session.GameSessionService;
import com.yisy.modules.doramcu.model.session.GameActionLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Service for recording and querying game action logs for replay and analysis.
 * <p>
 * Each game session has its own action log list. Actions are recorded in real-time
 * as they occur, and can be queried by session, player, turn, or action type.
 * </p>
 * <p>
 * The log is stored in-memory per session. For persistence across restarts,
 * the logs can be exported via {@link #exportSessionLogs(String)} and imported
 * via {@link #importSessionLogs(String, List)}.
 * </p>
 */
@Service
public class GameReplayService {

    private static final Logger log = LoggerFactory.getLogger(GameReplayService.class);
    private static final int MAX_LOG_SIZE_PER_SESSION = 10000;

    /** Per-session action logs. */
    private final Map<String, List<GameActionLog>> sessionLogs = new ConcurrentHashMap<>();

    /**
     * Records an action for a session.
     * <p>
     * Called automatically by GameSessionService after each action.
     * </p>
     *
     * @param actionLog the action log entry to record
     */
    public void recordAction(GameActionLog actionLog) {
        if (actionLog == null) {
            return;
        }

        String sessionId = actionLog.getSessionId();
        sessionLogs.computeIfAbsent(sessionId, k -> Collections.synchronizedList(new ArrayList<>()));
        List<GameActionLog> logs = sessionLogs.get(sessionId);

        synchronized (logs) {
            // Trim old entries if log is too large
            while (logs.size() >= MAX_LOG_SIZE_PER_SESSION) {
                logs.remove(0);
            }
            logs.add(actionLog);
        }

        log.debug("Recorded action: {} for session {}", actionLog.getActionType(), sessionId);
    }

    /**
     * Records a system event (no player) for a session.
     */
    public void recordSystemEvent(String sessionId, GameActionLog.ActionType actionType,
                                   int turnNumber, Map<String, Object> data) {
        GameActionLog actionLog = GameActionLog.builder(actionType, sessionId)
                .turnNumber(turnNumber)
                .data(data)
                .now()
                .build();
        recordAction(actionLog);
    }

    /**
     * Gets all action logs for a session.
     *
     * @param sessionId the session ID
     * @return immutable list of action logs
     */
    public List<GameActionLog> getSessionLogs(String sessionId) {
        List<GameActionLog> logs = sessionLogs.get(sessionId);
        if (logs == null) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(new ArrayList<>(logs));
    }

    /**
     * Gets action logs for a session, filtered by player.
     *
     * @param sessionId the session ID
     * @param playerId the player ID
     * @return immutable list of action logs for the player
     */
    public List<GameActionLog> getPlayerLogs(String sessionId, Long playerId) {
        return getSessionLogs(sessionId).stream()
                .filter(log -> playerId.equals(log.getPlayerId()))
                .collect(Collectors.toList());
    }

    /**
     * Gets action logs for a session, filtered by action type.
     *
     * @param sessionId the session ID
     * @param actionType the action type
     * @return immutable list of action logs of the specified type
     */
    public List<GameActionLog> getActionTypeLogs(String sessionId, GameActionLog.ActionType actionType) {
        return getSessionLogs(sessionId).stream()
                .filter(log -> log.getActionType() == actionType)
                .collect(Collectors.toList());
    }

    /**
     * Gets action logs for a session, filtered by turn number range.
     *
     * @param sessionId the session ID
     * @param fromTurn inclusive start turn
     * @param toTurn inclusive end turn
     * @return immutable list of action logs in the turn range
     */
    public List<GameActionLog> getTurnRangeLogs(String sessionId, int fromTurn, int toTurn) {
        return getSessionLogs(sessionId).stream()
                .filter(log -> log.getTurnNumber() >= fromTurn && log.getTurnNumber() <= toTurn)
                .collect(Collectors.toList());
    }

    /**
     * Gets action logs for a session, filtered by timestamp range.
     *
     * @param sessionId the session ID
     * @param fromTime inclusive start time
     * @param toTime inclusive end time
     * @return immutable list of action logs in the time range
     */
    public List<GameActionLog> getTimeRangeLogs(String sessionId, Instant fromTime, Instant toTime) {
        return getSessionLogs(sessionId).stream()
                .filter(log -> !log.getTimestamp().isBefore(fromTime) && !log.getTimestamp().isAfter(toTime))
                .collect(Collectors.toList());
    }

    /**
     * Gets the last N action logs for a session.
     *
     * @param sessionId the session ID
     * @param count number of recent logs to return
     * @return immutable list of the most recent action logs
     */
    public List<GameActionLog> getRecentLogs(String sessionId, int count) {
        List<GameActionLog> logs = getSessionLogs(sessionId);
        if (logs.size() <= count) {
            return logs;
        }
        return logs.subList(logs.size() - count, logs.size());
    }

    /**
     * Gets action statistics for a session.
     *
     * @param sessionId the session ID
     * @return a map with total actions, actions by type, actions by player, and turn count
     */
    public Map<String, Object> getSessionStatistics(String sessionId) {
        List<GameActionLog> logs = getSessionLogs(sessionId);

        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("sessionId", sessionId);
        stats.put("totalActions", logs.size());

        // Actions by type
        Map<String, Long> byType = logs.stream()
                .collect(Collectors.groupingBy(log -> log.getActionType().name(), Collectors.counting()));
        stats.put("actionsByType", byType);

        // Actions by player
        Map<String, Long> byPlayer = logs.stream()
                .filter(log -> log.getPlayerId() != null)
                .collect(Collectors.groupingBy(log -> log.getPlayerId().toString(), Collectors.counting()));
        stats.put("actionsByPlayer", byPlayer);

        // Turn range
        if (!logs.isEmpty()) {
            int minTurn = logs.stream().mapToInt(GameActionLog::getTurnNumber).min().orElse(0);
            int maxTurn = logs.stream().mapToInt(GameActionLog::getTurnNumber).max().orElse(0);
            stats.put("minTurn", minTurn);
            stats.put("maxTurn", maxTurn);
            stats.put("turnCount", maxTurn - minTurn + 1);
        } else {
            stats.put("minTurn", 0);
            stats.put("maxTurn", 0);
            stats.put("turnCount", 0);
        }

        // Time range
        if (!logs.isEmpty()) {
            Instant first = logs.stream().map(GameActionLog::getTimestamp).min(Instant::compareTo).orElseThrow();
            Instant last = logs.stream().map(GameActionLog::getTimestamp).max(Instant::compareTo).orElseThrow();
            stats.put("firstActionTime", first);
            stats.put("lastActionTime", last);
            stats.put("durationSeconds", java.time.Duration.between(first, last).getSeconds());
        }

        return stats;
    }

    /**
     * Exports all action logs for a session as a list of maps (JSON-serializable).
     *
     * @param sessionId the session ID
     * @return list of maps representing each action log entry
     */
    public List<Map<String, Object>> exportSessionLogs(String sessionId) {
        return getSessionLogs(sessionId).stream()
                .map(this::toMap)
                .collect(Collectors.toList());
    }

    /**
     * Imports action logs for a session from a list of maps.
     * <p>
     * Used for restoring logs from a previous export.
     * </p>
     *
     * @param sessionId the session ID
     * @param logMaps list of maps representing action log entries
     * @return number of logs imported
     */
    public int importSessionLogs(String sessionId, List<Map<String, Object>> logMaps) {
        if (logMaps == null || logMaps.isEmpty()) {
            return 0;
        }

        int count = 0;
        for (Map<String, Object> logMap : logMaps) {
            try {
                GameActionLog actionLog = fromMap(logMap, sessionId);
                if (actionLog != null) {
                    recordAction(actionLog);
                    count++;
                }
            } catch (Exception e) {
                log.warn("Failed to import action log: {}", e.getMessage());
            }
        }

        log.info("Imported {} action logs for session {}", count, sessionId);
        return count;
    }

    /**
     * Clears all action logs for a session.
     *
     * @param sessionId the session ID
     */
    public void clearSessionLogs(String sessionId) {
        List<GameActionLog> logs = sessionLogs.remove(sessionId);
        if (logs != null) {
            log.info("Cleared {} action logs for session {}", logs.size(), sessionId);
        }
    }

    /**
     * Gets the number of action logs for a session.
     *
     * @param sessionId the session ID
     * @return number of logged actions
     */
    public int getLogCount(String sessionId) {
        List<GameActionLog> logs = sessionLogs.get(sessionId);
        return logs != null ? logs.size() : 0;
    }

    /**
     * Lists all sessions that have action logs.
     *
     * @return list of session metadata maps
     */
    public List<Map<String, Object>> listSessionsWithLogs() {
        List<Map<String, Object>> sessions = new ArrayList<>();
        for (Map.Entry<String, List<GameActionLog>> entry : sessionLogs.entrySet()) {
            Map<String, Object> meta = new LinkedHashMap<>();
            meta.put("sessionId", entry.getKey());
            meta.put("logCount", entry.getValue().size());
            sessions.add(meta);
        }
        return sessions;
    }

    // ---- Serialization helpers ----

    private Map<String, Object> toMap(GameActionLog log) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("actionType", log.getActionType().name());
        map.put("playerId", log.getPlayerId());
        map.put("sessionId", log.getSessionId());
        map.put("turnNumber", log.getTurnNumber());
        map.put("timestamp", log.getTimestamp().toString());
        map.put("data", log.getData());
        return map;
    }

    @SuppressWarnings("unchecked")
    private GameActionLog fromMap(Map<String, Object> map, String defaultSessionId) {
        try {
            String actionTypeName = (String) map.get("actionType");
            if (actionTypeName == null) {
                return null;
            }

            GameActionLog.ActionType actionType = GameActionLog.ActionType.valueOf(actionTypeName);
            String sessionId = (String) map.get("sessionId");
            if (sessionId == null || sessionId.isBlank()) {
                sessionId = defaultSessionId;
            }

            Object playerIdObj = map.get("playerId");
            Long playerId = null;
            if (playerIdObj instanceof Number) {
                playerId = ((Number) playerIdObj).longValue();
            } else if (playerIdObj instanceof String) {
                try {
                    playerId = Long.parseLong((String) playerIdObj);
                } catch (NumberFormatException ignored) {
                }
            }

            Object turnNumberObj = map.get("turnNumber");
            int turnNumber = 0;
            if (turnNumberObj instanceof Number) {
                turnNumber = ((Number) turnNumberObj).intValue();
            }

            String timestampStr = (String) map.get("timestamp");
            Instant timestamp = null;
            if (timestampStr != null && !timestampStr.isBlank()) {
                timestamp = Instant.parse(timestampStr);
            }

            Map<String, Object> data = (Map<String, Object>) map.get("data");

            return GameActionLog.builder(actionType, sessionId)
                    .playerId(playerId)
                    .turnNumber(turnNumber)
                    .timestamp(timestamp)
                    .data(data)
                    .build();
        } catch (Exception e) {
            log.warn("Failed to parse action log from map: {}", e.getMessage());
            return null;
        }
    }
}
