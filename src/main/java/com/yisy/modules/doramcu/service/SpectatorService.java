package com.yisy.modules.doramcu.service;

import com.yisy.modules.doramcu.dto.GameEvent;
import com.yisy.modules.doramcu.dto.GameStateResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Spectator service for read-only observation of game sessions.
 * <p>
 * Allows spectators to watch game sessions in real-time without participating.
 * Useful for streaming, tutorials, and game analysis. Spectators receive
 * redacted game state that hides sensitive player information.
 * </p>
 * <p>
 * Features:
 * <ul>
 *   <li>Spectator registration and session joining</li>
 *   <li>Redacted game state snapshots (no sensitive player data)</li>
 *   <li>Event history for catch-up after joining</li>
 *   <li>Spectator statistics and management</li>
 *   <li>Auto-removal of stale spectator connections</li>
 * </ul>
 * </p>
 */
public class SpectatorService {

    private static final Logger log = LoggerFactory.getLogger(SpectatorService.class);
    private static final int MAX_EVENT_HISTORY = 200;
    private static final int SPECTATOR_TIMEOUT_MINUTES = 30;

    private final GameSessionService gameSessionService;

    /** Spectator ID → SpectatorInfo mapping. */
    private final ConcurrentHashMap<String, SpectatorInfo> spectators = new ConcurrentHashMap<>();

    /** Session ID → list of spectator IDs. */
    private final ConcurrentHashMap<String, List<String>> sessionSpectators = new ConcurrentHashMap<>();

    /** Session ID → event history for spectators. */
    private final ConcurrentHashMap<String, Deque<GameEvent>> eventHistory = new ConcurrentHashMap<>();

    public SpectatorService(GameSessionService gameSessionService) {
        this.gameSessionService = gameSessionService;
    }

    /**
     * Information about a registered spectator.
     */
    public static class SpectatorInfo {
        private final String spectatorId;
        private final String name;
        private final Instant joinedAt;
        private String currentSession;
        private boolean active;
        private Instant lastHeartbeat;

        private SpectatorInfo(Builder builder) {
            this.spectatorId = builder.spectatorId;
            this.name = builder.name;
            this.joinedAt = builder.joinedAt;
            this.currentSession = builder.currentSession;
            this.active = true;
            this.lastHeartbeat = Instant.now();
        }

        public String getSpectatorId() { return spectatorId; }
        public String getName() { return name; }
        public Instant getJoinedAt() { return joinedAt; }
        public String getCurrentSession() { return currentSession; }
        public void setCurrentSession(String session) { this.currentSession = session; }
        public boolean isActive() { return active; }
        public void setActive(boolean active) { this.active = active; }
        public Instant getLastHeartbeat() { return lastHeartbeat; }
        public void heartbeat() { this.lastHeartbeat = Instant.now(); }

        public boolean isStale() {
            return !active || lastHeartbeat.isBefore(Instant.now().minusSeconds(SPECTATOR_TIMEOUT_MINUTES * 60));
        }

        public static Builder builder(String spectatorId) { return new Builder(spectatorId); }

        public static class Builder {
            private final String spectatorId;
            private String name;
            private Instant joinedAt = Instant.now();
            private String currentSession;

            public Builder(String spectatorId) { this.spectatorId = spectatorId; }
            public Builder name(String name) { this.name = name; return this; }
            public Builder joinedAt(Instant joinedAt) { this.joinedAt = joinedAt; return this; }
            public Builder currentSession(String session) { this.currentSession = session; return this; }
            public SpectatorInfo build() {
                if (this.name == null || this.name.isEmpty()) {
                    this.name = "Spectator-" + this.spectatorId.substring(0, Math.min(8, this.spectatorId.length()));
                }
                return new SpectatorInfo(this);
            }
        }
    }

    /**
     * Redacted game state for spectators — hides sensitive player information.
     */
    public static class SpectatorGameState {
        private final String sessionId;
        private final int currentTurn;
        private final int playerCount;
        private final String currentPlayerName;
        private final List<SpectatorPlayerSummary> players;
        private final List<SpectatorFleetSummary> fleets;
        private final int fleetCount;
        private final Instant timestamp;
        private final Map<String, Object> metadata;

        private SpectatorGameState(Builder builder) {
            this.sessionId = builder.sessionId;
            this.currentTurn = builder.currentTurn;
            this.playerCount = builder.playerCount;
            this.currentPlayerName = builder.currentPlayerName;
            this.players = Collections.unmodifiableList(new ArrayList<>(builder.players));
            this.fleets = Collections.unmodifiableList(new ArrayList<>(builder.fleets));
            this.fleetCount = builder.fleetCount;
            this.timestamp = builder.timestamp;
            this.metadata = Collections.unmodifiableMap(new HashMap<>(builder.metadata));
        }

        public String getSessionId() { return sessionId; }
        public int getCurrentTurn() { return currentTurn; }
        public int getPlayerCount() { return playerCount; }
        public String getCurrentPlayerName() { return currentPlayerName; }
        public List<SpectatorPlayerSummary> getPlayers() { return players; }
        public List<SpectatorFleetSummary> getFleets() { return fleets; }
        public int getFleetCount() { return fleetCount; }
        public Instant getTimestamp() { return timestamp; }
        public Map<String, Object> getMetadata() { return metadata; }

        public static Builder builder() { return new Builder(); }

        public static class Builder {
            private String sessionId;
            private int currentTurn;
            private int playerCount;
            private String currentPlayerName;
            private final List<SpectatorPlayerSummary> players = new ArrayList<>();
            private final List<SpectatorFleetSummary> fleets = new ArrayList<>();
            private int fleetCount;
            private Instant timestamp = Instant.now();
            private final Map<String, Object> metadata = new HashMap<>();

            public Builder sessionId(String sessionId) { this.sessionId = sessionId; return this; }
            public Builder currentTurn(int currentTurn) { this.currentTurn = currentTurn; return this; }
            public Builder playerCount(int playerCount) { this.playerCount = playerCount; return this; }
            public Builder currentPlayerName(String name) { this.currentPlayerName = name; return this; }
            public Builder players(List<SpectatorPlayerSummary> players) { this.players.addAll(players); return this; }
            public Builder fleets(List<SpectatorFleetSummary> fleets) { this.fleets.addAll(fleets); return this; }
            public Builder fleetCount(int fleetCount) { this.fleetCount = fleetCount; return this; }
            public Builder timestamp(Instant timestamp) { this.timestamp = timestamp; return this; }
            public Builder metadata(Map<String, Object> metadata) { this.metadata.putAll(metadata); return this; }
            public SpectatorGameState build() { return new SpectatorGameState(this); }
        }
    }

    /**
     * Redacted player summary for spectators.
     */
    public static class SpectatorPlayerSummary {
        private final String name;
        private final int fleetCount;
        private final int planetCount;

        public SpectatorPlayerSummary(String name, int fleetCount, int planetCount) {
            this.name = name;
            this.fleetCount = fleetCount;
            this.planetCount = planetCount;
        }

        public String getName() { return name; }
        public int getFleetCount() { return fleetCount; }
        public int getPlanetCount() { return planetCount; }
    }

    /**
     * Redacted fleet summary for spectators.
     */
    public static class SpectatorFleetSummary {
        private final String id;
        private final String playerName;
        private final int shipCount;
        private final String sector;

        public SpectatorFleetSummary(String id, String playerName, int shipCount, String sector) {
            this.id = id;
            this.playerName = playerName;
            this.shipCount = shipCount;
            this.sector = sector;
        }

        public String getId() { return id; }
        public String getPlayerName() { return playerName; }
        public int getShipCount() { return shipCount; }
        public String getSector() { return sector; }
    }

    /**
     * Register a new spectator.
     */
    public SpectatorInfo registerSpectator(String spectatorId, String name) {
        SpectatorInfo info = SpectatorInfo.builder(spectatorId)
                .name(name)
                .build();
        spectators.put(spectatorId, info);
        log.info("Spectator registered: {} ({})", name, spectatorId);
        return info;
    }

    /**
     * Unregister a spectator.
     */
    public void unregisterSpectator(String spectatorId) {
        SpectatorInfo info = spectators.remove(spectatorId);
        if (info != null && info.getCurrentSession() != null) {
            leaveSession(spectatorId, info.getCurrentSession());
        }
        log.info("Spectator unregistered: {}", spectatorId);
    }

    /**
     * Get spectator info by ID.
     */
    public Optional<SpectatorInfo> getSpectator(String spectatorId) {
        return Optional.ofNullable(spectators.get(spectatorId));
    }

    /**
     * A spectator joins a game session for observation.
     */
    public Optional<SpectatorGameState> joinSession(String spectatorId, String sessionId) {
        SpectatorInfo info = spectators.get(spectatorId);
        if (info == null) {
            log.warn("Unknown spectator attempted to join session: {}", spectatorId);
            return Optional.empty();
        }

        // Leave previous session if any
        if (info.getCurrentSession() != null) {
            leaveSession(spectatorId, info.getCurrentSession());
        }

        // Check if session exists
        GameStateResponse gameState = gameSessionService.getGameState(sessionId);
        if (gameState == null) {
            log.warn("Spectator {} attempted to join non-existent session: {}", spectatorId, sessionId);
            return Optional.empty();
        }

        // Add spectator to session
        sessionSpectators.computeIfAbsent(sessionId, k -> Collections.synchronizedList(new ArrayList<>()))
                .add(spectatorId);
        info.setCurrentSession(sessionId);

        log.info("Spectator {} joined session {}", spectatorId, sessionId);

        // Return redacted game state
        return Optional.of(buildSpectatorGameState(gameState));
    }

    /**
     * A spectator leaves a game session.
     */
    public void leaveSession(String spectatorId, String sessionId) {
        sessionSpectators.computeIfPresent(sessionId, (key, list) -> {
            list.removeIf(id -> id.equals(spectatorId));
            return list.isEmpty() ? null : list;
        });

        SpectatorInfo info = spectators.get(spectatorId);
        if (info != null && sessionId.equals(info.getCurrentSession())) {
            info.setCurrentSession(null);
        }

        log.info("Spectator {} left session {}", spectatorId, sessionId);
    }

    /**
     * Get redacted game state for a session.
     */
    public Optional<SpectatorGameState> getSpectatorGameState(String sessionId) {
        GameStateResponse gameState = gameSessionService.getGameState(sessionId);
        if (gameState == null) {
            return Optional.empty();
        }
        return Optional.of(buildSpectatorGameState(gameState));
    }

    /**
     * Record an event for spectator history.
     */
    public void recordEvent(String sessionId, GameEvent event) {
        Deque<GameEvent> history = eventHistory.computeIfAbsent(sessionId,
                k -> new LinkedList<>());
        history.addLast(event);

        // Trim history
        while (history.size() > MAX_EVENT_HISTORY) {
            history.removeFirst();
        }
    }

    /**
     * Get event history for a session (for catch-up after joining).
     */
    public List<GameEvent> getEventHistory(String sessionId, int count) {
        Deque<GameEvent> history = eventHistory.get(sessionId);
        if (history == null || history.isEmpty()) {
            return Collections.emptyList();
        }
        int from = Math.max(0, history.size() - count);
        return new ArrayList<>(history).subList(from, history.size());
    }

    /**
     * Get all spectators for a session.
     */
    public List<SpectatorInfo> getSessionSpectators(String sessionId) {
        List<String> ids = sessionSpectators.get(sessionId);
        if (ids == null) return Collections.emptyList();

        return ids.stream()
                .map(spectators::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * Get the number of spectators for a session.
     */
    public int getSpectatorCount(String sessionId) {
        List<String> ids = sessionSpectators.get(sessionId);
        return ids != null ? ids.size() : 0;
    }

    /**
     * Get all registered spectators.
     */
    public List<SpectatorInfo> getAllSpectators() {
        return new ArrayList<>(spectators.values());
    }

    /**
     * Get all sessions with active spectators.
     */
    public List<String> getSessionsWithSpectators() {
        return new ArrayList<>(sessionSpectators.keySet());
    }

    /**
     * Get spectator statistics.
     */
    public Map<String, Object> getStatistics() {
        int totalSpectators = spectators.size();
        int activeSpectators = (int) spectators.values().stream()
                .filter(SpectatorInfo::isActive)
                .count();
        int sessionsWithSpectators = sessionSpectators.size();

        return Map.of(
                "totalSpectators", totalSpectators,
                "activeSpectators", activeSpectators,
                "sessionsWithSpectators", sessionsWithSpectators,
                "totalEventHistoryEntries", eventHistory.values().stream()
                        .mapToInt(Deque::size).sum()
        );
    }

    /**
     * Get statistics for a specific session.
     */
    public Map<String, Object> getSessionStatistics(String sessionId) {
        int spectatorCount = getSpectatorCount(sessionId);
        Deque<GameEvent> history = eventHistory.get(sessionId);
        int eventCount = history != null ? history.size() : 0;

        return Map.of(
                "sessionId", sessionId,
                "spectatorCount", spectatorCount,
                "eventHistorySize", eventCount
        );
    }

    /**
     * Remove stale spectators.
     */
    public int removeStaleSpectators() {
        int removed = 0;
        Iterator<Map.Entry<String, SpectatorInfo>> it = spectators.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, SpectatorInfo> entry = it.next();
            if (entry.getValue().isStale()) {
                it.remove();
                leaveSession(entry.getKey(), entry.getValue().getCurrentSession());
                removed++;
            }
        }
        if (removed > 0) {
            log.info("Removed {} stale spectators", removed);
        }
        return removed;
    }

    /**
     * Clear all spectator data.
     */
    public void clearAll() {
        spectators.clear();
        sessionSpectators.clear();
        eventHistory.clear();
    }

    /**
     * Build a redacted game state for spectators.
     */
    private SpectatorGameState buildSpectatorGameState(GameStateResponse gameState) {
        SpectatorGameState.Builder builder = SpectatorGameState.builder()
                .sessionId(gameState.getSessionId())
                .currentTurn(gameState.getCurrentTurn())
                .playerCount(gameState.getPlayers() != null ? gameState.getPlayers().size() : 0)
                .currentPlayerName(gameState.getCurrentPlayerName())
                .timestamp(Instant.now());

        // Add redacted player summaries
        if (gameState.getPlayers() != null) {
            for (var player : gameState.getPlayers()) {
                builder.players.add(new SpectatorPlayerSummary(
                        player.getName(),
                        player.getFleetCount(),
                        player.getPlanetCount()
                ));
            }
        }

        // Add redacted fleet summaries
        if (gameState.getFleets() != null) {
            for (var fleet : gameState.getFleets()) {
                String sector = fleet.getSystemId() != null ? "System-" + fleet.getSystemId() :
                        (fleet.getSubSectorRow() != 0 || fleet.getSubSectorCol() != 0 ?
                                "R" + fleet.getSubSectorRow() + "C" + fleet.getSubSectorCol() : "Unknown");
                builder.fleets.add(new SpectatorFleetSummary(
                        String.valueOf(fleet.getId()),
                        fleet.getName() != null ? fleet.getName() : "Fleet-" + fleet.getId(),
                        fleet.getShipCount(),
                        sector
                ));
            }
            builder.fleetCount(gameState.getFleets().size());
        }

        return builder.build();
    }
}
