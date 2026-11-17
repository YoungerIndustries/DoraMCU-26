package com.yisy.modules.doramcu.controller.session;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.yisy.modules.doramcu.model.session.GameActionLog;
import com.yisy.modules.doramcu.service.game.GameReplayService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * WebSocket handler for 3D game replay visualization.
 * <p>
 * Provides real-time streaming of recorded game action logs for replay playback.
 * Clients can join a replay session, control playback (play, pause, step, speed),
 * and receive action log entries as WebSocket messages for 3D reconstruction.
 * </p>
 * <p>
 * Message types sent to clients:
 * <ul>
 *   <li>{@code replay_snapshot} - Initial action log snapshot for a session</li>
 *   <li>{@code replay_action} - Single action log entry during playback</li>
 *   <li>{@code replay_status} - Playback status updates (playing, paused, stopped)</li>
 *   <li>{@code replay_timeline} - Timeline metadata (total actions, current position)</li>
 *   <li>{@code replay_sessions} - List of available replay sessions</li>
 * </ul>
 * </p>
 * <p>
 * Message types received from clients:
 * <ul>
 *   <li>{@code join} - Join a replay session: {"type":"join","sessionId":"..."}</li>
 *   <li>{@code play} - Start/resume playback: {"type":"play","speed":1.0}</li>
 *   <li>{@code pause} - Pause playback: {"type":"pause"}</li>
 *   <li>{@code stop} - Stop and reset playback: {"type":"stop"}</li>
 *   <li>{@code step_forward} - Step forward one action: {"type":"step_forward"}</li>
 *   <li>{@code step_backward} - Step backward one action: {"type":"step_backward"}</li>
 *   <li>{@code seek} - Seek to specific action index: {"type":"seek","index":42}</li>
 *   <li>{@code list_sessions} - List available sessions: {"type":"list_sessions"}</li>
 * </ul>
 * </p>
 */
@Component
public class ReplayWebSocketHandler extends TextWebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(ReplayWebSocketHandler.class);

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final GameReplayService gameReplayService;

    // Replay session state per WebSocket connection
    private final Map<WebSocketSession, ReplayState> replayStates = new ConcurrentHashMap<>();

    // Playback scheduler for timed action delivery
    private final ScheduledExecutorService playbackScheduler = Executors.newScheduledThreadPool(4);

    // Simulated action logs for demo when no real sessions exist
    private static final String[] SIMULATED_SESSIONS = {
            "replay-demo-1", "replay-demo-2", "replay-demo-3"
    };

    public ReplayWebSocketHandler(GameReplayService gameReplayService) {
        this.gameReplayService = gameReplayService;
    }

    /**
     * State tracking for each replay client connection.
     */
    private static class ReplayState {
        String sessionId;
        List<GameActionLog> actionLogs;
        int currentIndex = -1;
        boolean playing = false;
        double speed = 1.0;
        ScheduledFuture<?> playbackTask;
        long startTime = 0;

        ReplayState(String sessionId, List<GameActionLog> actionLogs) {
            this.sessionId = sessionId;
            this.actionLogs = actionLogs;
        }
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        log.info("Replay client connected: {}", session.getId());
        // Send available sessions list on connect
        sendAvailableSessions(session);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        try {
            ObjectNode json = (ObjectNode) objectMapper.readTree(message.getPayload());
            String type = json.has("type") ? json.get("type").asText() : null;

            switch (type) {
                case "join":
                    handleJoin(session, json);
                    break;
                case "play":
                    handlePlay(session, json);
                    break;
                case "pause":
                    handlePause(session);
                    break;
                case "stop":
                    handleStop(session);
                    break;
                case "step_forward":
                    handleStepForward(session);
                    break;
                case "step_backward":
                    handleStepBackward(session);
                    break;
                case "seek":
                    handleSeek(session, json);
                    break;
                case "list_sessions":
                    sendAvailableSessions(session);
                    break;
                default:
                    log.warn("Unknown replay message type: {}", type);
            }
        } catch (Exception e) {
            log.error("Error handling replay message: {}", e.getMessage(), e);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        ReplayState state = replayStates.remove(session);
        if (state != null && state.playing) {
            stopPlayback(state);
        }
        log.info("Replay client disconnected: {}", session.getId());
    }

    // ==================== Message Handlers ====================

    private void handleJoin(WebSocketSession session, ObjectNode json) {
        String sessionId = json.has("sessionId") ? json.get("sessionId").asText() : null;
        if (sessionId == null || sessionId.isBlank()) {
            sendError(session, "sessionId is required");
            return;
        }

        // Stop any existing playback
        ReplayState existing = replayStates.get(session);
        if (existing != null && existing.playing) {
            stopPlayback(existing);
        }

        // Get real action logs or generate simulated data
        List<GameActionLog> logs = gameReplayService.getSessionLogs(sessionId);
        if (logs.isEmpty()) {
            // Check if it's a simulated session
            if (Arrays.asList(SIMULATED_SESSIONS).contains(sessionId)) {
                logs = generateSimulatedActions(sessionId);
            } else {
                // Try to get any available session
                sendAvailableSessions(session);
                sendError(session, "No replay data for session: " + sessionId);
                return;
            }
        }

        ReplayState state = new ReplayState(sessionId, logs);
        replayStates.put(session, state);

        // Send initial snapshot
        ObjectNode snapshot = objectMapper.createObjectNode();
        snapshot.put("type", "replay_snapshot");
        snapshot.put("sessionId", sessionId);
        snapshot.put("totalActions", logs.size());
        snapshot.put("currentIndex", -1);

        // Add action summary by type
        ObjectNode summary = snapshot.putObject("summary");
        Map<GameActionLog.ActionType, Long> typeCounts = logs.stream()
                .collect(Collectors.groupingBy(GameActionLog::getActionType, Collectors.counting()));
        for (Map.Entry<GameActionLog.ActionType, Long> entry : typeCounts.entrySet()) {
            summary.put(entry.getKey().name(), entry.getValue().intValue());
        }

        // Add timeline metadata
        ArrayNode timeline = snapshot.putArray("timeline");
        for (GameActionLog logEntry : logs) {
            ObjectNode actionNode = timeline.addObject();
            actionNode.put("index", logs.indexOf(logEntry));
            actionNode.put("actionType", logEntry.getActionType().name());
            actionNode.put("turnNumber", logEntry.getTurnNumber());
            if (logEntry.getTimestamp() != null) {
                actionNode.put("timestamp", logEntry.getTimestamp().toString());
            }
            if (logEntry.getPlayerId() != null) {
                actionNode.put("playerId", logEntry.getPlayerId());
            }
        }

        sendMessage(session, snapshot);
        log.info("Joined replay session {} with {} actions", sessionId, logs.size());
    }

    private void handlePlay(WebSocketSession session, ObjectNode json) {
        ReplayState state = replayStates.get(session);
        if (state == null) {
            sendError(session, "Not joined to any replay session");
            return;
        }

        // Update speed if provided
        if (json.has("speed")) {
            state.speed = json.get("speed").asDouble(1.0);
        }

        // If already at the end, reset to beginning
        if (state.currentIndex >= state.actionLogs.size() - 1) {
            state.currentIndex = -1;
        }

        state.playing = true;
        state.startTime = System.currentTimeMillis();

        // Send status update
        ObjectNode status = objectMapper.createObjectNode();
        status.put("type", "replay_status");
        status.put("status", "playing");
        status.put("speed", state.speed);
        status.put("currentIndex", state.currentIndex);
        status.put("totalActions", state.actionLogs.size());
        sendMessage(session, status);

        // Start playback task
        startPlayback(state, session);
    }

    private void handlePause(WebSocketSession session) {
        ReplayState state = replayStates.get(session);
        if (state == null) return;

        stopPlayback(state);
        state.playing = false;

        ObjectNode status = objectMapper.createObjectNode();
        status.put("type", "replay_status");
        status.put("status", "paused");
        status.put("currentIndex", state.currentIndex);
        status.put("totalActions", state.actionLogs.size());
        sendMessage(session, status);
    }

    private void handleStop(WebSocketSession session) {
        ReplayState state = replayStates.get(session);
        if (state == null) return;

        stopPlayback(state);
        state.playing = false;
        state.currentIndex = -1;

        ObjectNode status = objectMapper.createObjectNode();
        status.put("type", "replay_status");
        status.put("status", "stopped");
        status.put("currentIndex", -1);
        status.put("totalActions", state.actionLogs.size());
        sendMessage(session, status);
    }

    private void handleStepForward(WebSocketSession session) {
        ReplayState state = replayStates.get(session);
        if (state == null) return;

        if (state.currentIndex < state.actionLogs.size() - 1) {
            state.currentIndex++;
            GameActionLog actionLog = state.actionLogs.get(state.currentIndex);
            sendAction(session, actionLog, state.currentIndex);
            sendTimelineUpdate(session, state);
        }
    }

    private void handleStepBackward(WebSocketSession session) {
        ReplayState state = replayStates.get(session);
        if (state == null) return;

        if (state.currentIndex > 0) {
            state.currentIndex--;
            GameActionLog actionLog = state.actionLogs.get(state.currentIndex);
            sendAction(session, actionLog, state.currentIndex);
            sendTimelineUpdate(session, state);
        }
    }

    private void handleSeek(WebSocketSession session, ObjectNode json) {
        ReplayState state = replayStates.get(session);
        if (state == null) return;

        int index = json.has("index") ? json.get("index").asInt(0) : 0;
        index = Math.max(0, Math.min(index, state.actionLogs.size() - 1));
        state.currentIndex = index;

        GameActionLog actionLog = state.actionLogs.get(index);
        sendAction(session, actionLog, index);
        sendTimelineUpdate(session, state);
    }

    // ==================== Playback Engine ====================

    private void startPlayback(ReplayState state, WebSocketSession session) {
        stopPlayback(state); // Cancel any existing task

        // Base interval: 1000ms per action at 1x speed
        long baseInterval = 1000;
        long interval = Math.max(100, (long) (baseInterval / state.speed));

        state.playbackTask = playbackScheduler.scheduleAtFixedRate(() -> {
            if (!state.playing) return;

            if (state.currentIndex < state.actionLogs.size() - 1) {
                state.currentIndex++;
                GameActionLog actionLog = state.actionLogs.get(state.currentIndex);
                sendAction(session, actionLog, state.currentIndex);
                sendTimelineUpdate(session, state);
            } else {
                // Reached end
                state.playing = false;
                ObjectNode status = objectMapper.createObjectNode();
                status.put("type", "replay_status");
                status.put("status", "ended");
                status.put("currentIndex", state.currentIndex);
                status.put("totalActions", state.actionLogs.size());
                sendMessage(session, status);
            }
        }, 0, interval, TimeUnit.MILLISECONDS);
    }

    private void stopPlayback(ReplayState state) {
        if (state.playbackTask != null) {
            state.playbackTask.cancel(false);
            state.playbackTask = null;
        }
    }

    // ==================== Message Senders ====================

    private void sendAction(WebSocketSession session, GameActionLog actionLog, int index) {
        try {
            ObjectNode message = objectMapper.createObjectNode();
            message.put("type", "replay_action");
            message.put("index", index);
            message.put("actionType", actionLog.getActionType().name());
            message.put("turnNumber", actionLog.getTurnNumber());

            if (actionLog.getPlayerId() != null) {
                message.put("playerId", actionLog.getPlayerId());
            }
            if (actionLog.getTimestamp() != null) {
                message.put("timestamp", actionLog.getTimestamp().toString());
            }

            // Include action data for 3D reconstruction
            if (actionLog.getData() != null && !actionLog.getData().isEmpty()) {
                ObjectNode data = message.putObject("data");
                for (Map.Entry<String, Object> entry : actionLog.getData().entrySet()) {
                    if (entry.getValue() instanceof Number) {
                        data.put(entry.getKey(), ((Number) entry.getValue()).doubleValue());
                    } else if (entry.getValue() instanceof Boolean) {
                        data.put(entry.getKey(), (Boolean) entry.getValue());
                    } else {
                        data.put(entry.getKey(), String.valueOf(entry.getValue()));
                    }
                }
            }

            sendMessage(session, message);
        } catch (Exception e) {
            log.error("Failed to send replay action: {}", e.getMessage());
        }
    }

    private void sendTimelineUpdate(WebSocketSession session, ReplayState state) {
        try {
            ObjectNode message = objectMapper.createObjectNode();
            message.put("type", "replay_timeline");
            message.put("currentIndex", state.currentIndex);
            message.put("totalActions", state.actionLogs.size());
            message.put("progress", state.actionLogs.isEmpty() ? 0.0 :
                    (double) (state.currentIndex + 1) / state.actionLogs.size());
            message.put("playing", state.playing);
            message.put("speed", state.speed);
            sendMessage(session, message);
        } catch (Exception e) {
            log.error("Failed to send timeline update: {}", e.getMessage());
        }
    }

    private void sendAvailableSessions(WebSocketSession session) {
        try {
            ObjectNode message = objectMapper.createObjectNode();
            message.put("type", "replay_sessions");

            ArrayNode sessions = message.putArray("sessions");

            // Add real sessions
            List<Map<String, Object>> realSessions = gameReplayService.listSessionsWithLogs();
            for (Map<String, Object> sessionMeta : realSessions) {
                String sessionId = (String) sessionMeta.get("sessionId");
                int logCount = (int) sessionMeta.get("logCount");
                if (logCount > 0) {
                    ObjectNode sessionNode = sessions.addObject();
                    sessionNode.put("sessionId", sessionId);
                    sessionNode.put("actionCount", logCount);
                    sessionNode.put("source", "real");
                    // Get first and last action for time range
                    List<GameActionLog> logs = gameReplayService.getSessionLogs(sessionId);
                    if (!logs.isEmpty()) {
                        GameActionLog first = logs.get(0);
                        GameActionLog last = logs.get(logs.size() - 1);
                        if (first.getTimestamp() != null) {
                            sessionNode.put("startTime", first.getTimestamp().toString());
                        }
                        if (last.getTimestamp() != null) {
                            sessionNode.put("endTime", last.getTimestamp().toString());
                        }
                    }
                }
            }

            // Add simulated sessions for demo
            for (String simSession : SIMULATED_SESSIONS) {
                ObjectNode sessionNode = sessions.addObject();
                sessionNode.put("sessionId", simSession);
                sessionNode.put("actionCount", 50);
                sessionNode.put("source", "simulated");
            }

            sendMessage(session, message);
        } catch (Exception e) {
            log.error("Failed to send available sessions: {}", e.getMessage());
        }
    }

    private void sendError(WebSocketSession session, String errorMessage) {
        try {
            ObjectNode message = objectMapper.createObjectNode();
            message.put("type", "error");
            message.put("message", errorMessage);
            sendMessage(session, message);
        } catch (Exception e) {
            log.error("Failed to send error: {}", e.getMessage());
        }
    }

    private void sendMessage(WebSocketSession session, ObjectNode message) {
        try {
            if (session.isOpen()) {
                session.sendMessage(new TextMessage(objectMapper.writeValueAsString(message)));
            }
        } catch (IOException e) {
            log.warn("Failed to send message to session {}: {}", session.getId(), e.getMessage());
        }
    }

    // ==================== Simulated Data ====================

    private List<GameActionLog> generateSimulatedActions(String sessionId) {
        List<GameActionLog> actions = new ArrayList<>();
        Random random = new Random(sessionId.hashCode());

        GameActionLog.ActionType[] actionTypes = GameActionLog.ActionType.values();
        String[] fleetNames = {"Alpha Fleet", "Bravo Fleet", "Charlie Fleet", "Delta Fleet",
                "Echo Fleet", "Foxtrot Fleet"};

        // Game start
        actions.add(GameActionLog.builder(GameActionLog.ActionType.GAME_STARTED, sessionId)
                .turnNumber(0)
                .data(Collections.singletonMap("description", "Replay demo session"))
                .now()
                .build());

        // Generate simulated actions
        for (int turn = 1; turn <= 50; turn++) {
            int numActions = random.nextInt(3) + 1;
            for (int i = 0; i < numActions; i++) {
                GameActionLog.ActionType type = actionTypes[random.nextInt(actionTypes.length)];
                String fleetName = fleetNames[random.nextInt(fleetNames.length)];

                Map<String, Object> data = new HashMap<>();
                data.put("fleetName", fleetName);
                data.put("x", random.nextDouble() * 100);
                data.put("y", random.nextDouble() * 100);
                data.put("z", random.nextDouble() * 100);

                actions.add(GameActionLog.builder(type, sessionId)
                        .playerId(random.nextLong(1, 10))
                        .turnNumber(turn)
                        .data(data)
                        .now()
                        .build());
            }
        }

        // Game end
        actions.add(GameActionLog.builder(GameActionLog.ActionType.GAME_ENDED, sessionId)
                .turnNumber(51)
                .data(Collections.singletonMap("description", "Replay demo completed"))
                .now()
                .build());

        return actions;
    }

    // ==================== Public API ====================

    /**
     * Get the number of connected replay clients.
     */
    public int getConnectedClientCount() {
        return replayStates.size();
    }

    /**
     * Get the list of available session IDs.
     */
    public List<String> getAvailableSessions() {
        List<String> sessionIds = new ArrayList<>();
        for (Map<String, Object> meta : gameReplayService.listSessionsWithLogs()) {
            sessionIds.add((String) meta.get("sessionId"));
        }
        return sessionIds;
    }
}
