package com.yisy.modules.doramcu.model;

import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/**
 * Immutable record of a single game action for replay and analysis.
 * <p>
 * Each action log entry captures the action type, player, turn number,
 * timestamp, and any additional data relevant to the action (e.g., fleet ID,
 * target position, commodity name).
 * </p>
 */
public class GameActionLog {

    public enum ActionType {
        MOVE_FLEET,
        BUILD_SHIP,
        ENGAGE_COMBAT,
        ADVANCE_TURN,
        BUILD_STATION,
        BUILD_FACILITY,
        CREATE_FLEET,
        COLONIZE_PLANET,
        REINFORCE_BATTLE,
        RETREAT_BATTLE,
        BUILD_SHIP_FROM_DESIGN,
        BUY_COMMODITY,
        SELL_COMMODITY,
        ASSIGN_COMMANDER,
        PLAYER_JOINED,
        PLAYER_LEFT,
        GAME_STARTED,
        GAME_ENDED
    }

    private final ActionType actionType;
    private final Long playerId;
    private final String sessionId;
    private final int turnNumber;
    private final Instant timestamp;
    private final Map<String, Object> data;

    private GameActionLog(ActionType actionType, Long playerId, String sessionId,
                          int turnNumber, Instant timestamp, Map<String, Object> data) {
        this.actionType = Objects.requireNonNull(actionType, "actionType");
        this.playerId = playerId;
        this.sessionId = Objects.requireNonNull(sessionId, "sessionId");
        this.turnNumber = turnNumber;
        this.timestamp = timestamp;
        // Defensive copy — store immutable view
        this.data = (data != null && !data.isEmpty())
                ? Collections.unmodifiableMap(new java.util.HashMap<>(data))
                : Collections.emptyMap();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Builder builder(ActionType actionType, String sessionId) {
        return new Builder(actionType, sessionId);
    }

    // ---- Getters ----

    public ActionType getActionType() {
        return actionType;
    }

    public Long getPlayerId() {
        return playerId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public int getTurnNumber() {
        return turnNumber;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public Object getDataValue(String key) {
        return data.get(key);
    }

    public String getDataString(String key) {
        Object val = data.get(key);
        return val != null ? val.toString() : null;
    }

    public int getDataInt(String key, int defaultValue) {
        Object val = data.get(key);
        if (val instanceof Number) {
            return ((Number) val).intValue();
        }
        return defaultValue;
    }

    // ---- Builder ----

    public static class Builder {
        private ActionType actionType;
        private String sessionId;
        private Long playerId;
        private int turnNumber;
        private Instant timestamp;
        private final Map<String, Object> data = new java.util.HashMap<>();

        private Builder() {
            this(null, null);
        }

        private Builder(ActionType actionType, String sessionId) {
            this.actionType = actionType;
            this.sessionId = sessionId;
        }

        public Builder actionType(ActionType actionType) {
            this.actionType = actionType;
            return this;
        }

        public Builder sessionId(String sessionId) {
            this.sessionId = sessionId;
            return this;
        }

        public Builder playerId(Long playerId) {
            this.playerId = playerId;
            return this;
        }

        public Builder turnNumber(int turnNumber) {
            this.turnNumber = turnNumber;
            return this;
        }

        public Builder timestamp(Instant timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public Builder data(Map<String, Object> data) {
            if (data != null) {
                this.data.putAll(data);
            }
            return this;
        }

        public Builder data(String key, Object value) {
            this.data.put(key, value);
            return this;
        }

        public Builder now() {
            this.timestamp = Instant.now();
            return this;
        }

        public GameActionLog build() {
            if (timestamp == null) {
                this.timestamp = Instant.now();
            }
            return new GameActionLog(actionType, playerId, sessionId, turnNumber, timestamp, data);
        }
    }

    @Override
    public String toString() {
        return String.format("GameActionLog{%s, player=%s, session=%s, turn=%d, time=%s}",
                actionType, playerId, sessionId, turnNumber, timestamp);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GameActionLog that = (GameActionLog) o;
        return turnNumber == that.turnNumber &&
                Objects.equals(actionType, that.actionType) &&
                Objects.equals(playerId, that.playerId) &&
                Objects.equals(sessionId, that.sessionId) &&
                Objects.equals(timestamp, that.timestamp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(actionType, playerId, sessionId, turnNumber, timestamp);
    }
}
