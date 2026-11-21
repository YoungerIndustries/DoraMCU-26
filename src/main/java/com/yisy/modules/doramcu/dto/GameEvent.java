package com.yisy.modules.doramcu.dto;
import com.yisy.modules.doramcu.model.players.Player;

import java.util.Map;

public class GameEvent {

    public enum EventType {
        TURN_CHANGED,
        ACTION_RESULT,
        PLAYER_JOINED,
        PLAYER_LEFT,
        GAME_STATE,
        SESSION_CREATED,
        // Corporation-specific events for CEO/VP integration
        CORPORATION_CONTRACT_COMPLETED,  // A contract was completed (success or failure)
        CORPORATION_EMERGENCY,           // General corporation emergency (multiple failures, etc.)
        CORPORATION_INSOLVENCY_WARNING,  // Corporation balance is negative or near-zero
        CORPORATION_CREW_ISSUE           // Crew problems: fired, underperforming, etc.
    }

    private EventType type;
    private String sessionId;
    private Object payload;

    public GameEvent() {
    }

    public GameEvent(EventType type, String sessionId, Object payload) {
        this.type = type;
        this.sessionId = sessionId;
        this.payload = payload;
    }

    public EventType getType() {
        return type;
    }

    public void setType(EventType type) {
        this.type = type;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public Object getPayload() {
        return payload;
    }

    public void setPayload(Object payload) {
        this.payload = payload;
    }
}
