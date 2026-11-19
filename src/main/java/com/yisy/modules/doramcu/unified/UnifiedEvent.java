package com.yisy.modules.doramcu.unified;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;

/**
 * Represents a cross-module event in the unified world.
 * <p>
 * Events are published when significant actions occur in any game module
 * and can trigger effects in other modules. For example, completing a
 * Captain's Log mission might unlock a Star Wars faction.
 * </p>
 */
public class UnifiedEvent {

    public enum EventType {
        MISSION_COMPLETE,
        COMBAT_WIN,
        COMBAT_LOSS,
        DISCOVERY,
        LEVEL_UP,
        FACTION_UNLOCK,
        QUEST_COMPLETE,
        TERRITORY_CAPTURE,
        TRADE_DEAL,
        DIPLOMACY_EVENT,
        CHARACTER_DEATH,
        CUSTOM
    }

    private final String id;
    private final LocalDateTime timestamp;
    private final EventType type;
    private final String sourceModule;
    private final String title;
    private final String description;
    private final Map<String, Object> payload;
    private final String targetCharacterId;

    public UnifiedEvent(EventType type, String sourceModule, String title, String description,
                        Map<String, Object> payload, String targetCharacterId) {
        this.id = java.util.UUID.randomUUID().toString();
        this.timestamp = LocalDateTime.now();
        this.type = type;
        this.sourceModule = sourceModule;
        this.title = title;
        this.description = description;
        this.payload = payload != null ? Collections.unmodifiableMap(payload) : Collections.emptyMap();
        this.targetCharacterId = targetCharacterId;
    }

    public String getId() { return id; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public EventType getType() { return type; }
    public String getSourceModule() { return sourceModule; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public Map<String, Object> getPayload() { return payload; }
    public String getTargetCharacterId() { return targetCharacterId; }

    @Override
    public String toString() {
        return "UnifiedEvent{" +
                "id='" + id + '\'' +
                ", type=" + type +
                ", sourceModule='" + sourceModule + '\'' +
                ", title='" + title + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
