package com.yisy.modules.doramcu.service.voice;
import com.yisy.modules.doramcu.model.players.Player;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Text-to-Speech narration service for game events.
 * <p>
 * Generates narrated descriptions of game events for accessibility.
 * Supports multiple voices, event templates, and SSML markup.
 * The service generates narration text that can be consumed by
 * the browser's SpeechSynthesis API or external TTS services.
 * </p>
 * <p>
 * Event types include: fleet movement, combat, building, colonization,
 * trade, diplomacy, economic events, and system messages.
 * </p>
 */

@Service
public class TtsService {

    /**
     * Types of game events that can be narrated.
     */
    public enum EventType {
        FLEET_MOVE("Fleet Movement", "A fleet has moved to a new position"),
        COMBAT("Combat", "Combat has been engaged"),
        BUILD("Construction", "A new structure is being built"),
        COLONIZE("Colonization", "A new colony is established"),
        TRADE("Trade", "Trade activity has occurred"),
        DIPLOMACY("Diplomacy", "Diplomatic relations have changed"),
        ECONOMY("Economy", "Economic conditions have shifted"),
        TURN_ADVANCE("Turn Advance", "A new turn begins"),
        PLAYER_JOIN("Player Join", "A player has joined the game"),
        PLAYER_LEAVE("Player Leave", "A player has left the game"),
        SYSTEM("System", "A system message"),
        VICTORY("Victory", "Victory has been achieved"),
        DEFEAT("Defeat", "Defeat has been suffered"),
        DISCOVERY("Discovery", "Something new has been discovered"),
        WARNING("Warning", "A warning has been issued");

        private final String name;
        private final String description;

        EventType(String name, String description) {
            this.name = name;
            this.description = description;
        }

        public String getName() { return name; }
        public String getDescription() { return description; }
    }

    /**
     * Available voice profiles for narration.
     */
    public enum VoiceProfile {
        NARRATOR("Narrator", "Deep, authoritative voice for general narration", 0.9, 1.0),
        COMMANDER("Commander", "Crisp, military voice for tactical events", 1.0, 1.1),
        CHRONICLER("Chronicler", "Warm, storytelling voice for historical events", 0.8, 0.9),
        ALERT("Alert", "Urgent, attention-grabbing voice for warnings", 1.1, 1.2),
        CALM("Calm", "Soothing voice for peaceful events", 0.7, 0.8);

        private final String name;
        private final String description;
        private final double pitch;
        private final double rate;

        VoiceProfile(String name, String description, double pitch, double rate) {
            this.name = name;
            this.description = description;
            this.pitch = pitch;
            this.rate = rate;
        }

        public String getName() { return name; }
        public String getDescription() { return description; }
        public double getPitch() { return pitch; }
        public double getRate() { return rate; }
    }

    /**
     * A narration entry with text, voice profile, and metadata.
     */
    public static class Narration {
        private final String id;
        private final String sessionId;
        private final EventType eventType;
        private final VoiceProfile voiceProfile;
        private final String text;
        private final String ssml;
        private final Map<String, Object> metadata;
        private final Instant timestamp;

        private Narration(Builder builder) {
            this.id = builder.id;
            this.sessionId = builder.sessionId;
            this.eventType = builder.eventType;
            this.voiceProfile = builder.voiceProfile;
            this.text = builder.text;
            this.ssml = builder.ssml;
            this.metadata = Collections.unmodifiableMap(new HashMap<>(builder.metadata));
            this.timestamp = builder.timestamp;
        }

        public String getId() { return id; }
        public String getSessionId() { return sessionId; }
        public EventType getEventType() { return eventType; }
        public VoiceProfile getVoiceProfile() { return voiceProfile; }
        public String getText() { return text; }
        public String getSsml() { return ssml; }
        public Map<String, Object> getMetadata() { return metadata; }
        public Instant getTimestamp() { return timestamp; }

        public static Builder builder() { return new Builder(); }

        public static class Builder {
            private String id;
            private String sessionId;
            private EventType eventType;
            private VoiceProfile voiceProfile = VoiceProfile.NARRATOR;
            private String text;
            private String ssml;
            private final Map<String, Object> metadata = new HashMap<>();
            private Instant timestamp = Instant.now();

            public Builder id(String id) { this.id = id; return this; }
            public Builder sessionId(String sessionId) { this.sessionId = sessionId; return this; }
            public Builder eventType(EventType eventType) { this.eventType = eventType; return this; }
            public Builder voiceProfile(VoiceProfile voiceProfile) { this.voiceProfile = voiceProfile; return this; }
            public Builder text(String text) { this.text = text; return this; }
            public Builder ssml(String ssml) { this.ssml = ssml; return this; }
            public Builder metadata(Map<String, Object> metadata) { this.metadata.putAll(metadata); return this; }
            public Builder timestamp(Instant timestamp) { this.timestamp = timestamp; return this; }

            public Narration build() {
                if (this.id == null) this.id = UUID.randomUUID().toString().substring(0, 8);
                if (this.ssml == null) this.ssml = toSsml(this.text, this.voiceProfile);
                return new Narration(this);
            }
        }
    }

    /**
     * Narration settings for a session.
     */
    public static class NarrationSettings {
        private final String sessionId;
        private boolean enabled;
        private VoiceProfile defaultVoice;
        private final Set<EventType> enabledEvents;
        private double volume;
        private boolean autoPlay;

        private NarrationSettings(Builder builder) {
            this.sessionId = builder.sessionId;
            this.enabled = builder.enabled;
            this.defaultVoice = builder.defaultVoice;
            this.enabledEvents = EnumSet.copyOf(builder.enabledEvents);
            this.volume = builder.volume;
            this.autoPlay = builder.autoPlay;
        }

        public String getSessionId() { return sessionId; }
        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        public VoiceProfile getDefaultVoice() { return defaultVoice; }
        public void setDefaultVoice(VoiceProfile defaultVoice) { this.defaultVoice = defaultVoice; }
        public Set<EventType> getEnabledEvents() { return Collections.unmodifiableSet(enabledEvents); }
        public void setEnabledEvents(Set<EventType> events) { this.enabledEvents.clear(); this.enabledEvents.addAll(events); }
        public double getVolume() { return volume; }
        public void setVolume(double volume) { this.volume = volume; }
        public boolean isAutoPlay() { return autoPlay; }
        public void setAutoPlay(boolean autoPlay) { this.autoPlay = autoPlay; }

        public boolean shouldNarrate(EventType eventType) {
            return enabled && enabledEvents.contains(eventType);
        }

        public static Builder builder(String sessionId) { return new Builder(sessionId); }

        public static class Builder {
            private final String sessionId;
            private boolean enabled = true;
            private VoiceProfile defaultVoice = VoiceProfile.NARRATOR;
            private final Set<EventType> enabledEvents = EnumSet.allOf(EventType.class);
            private double volume = 1.0;
            private boolean autoPlay = true;

            public Builder(String sessionId) { this.sessionId = sessionId; }
            public Builder enabled(boolean enabled) { this.enabled = enabled; return this; }
            public Builder defaultVoice(VoiceProfile defaultVoice) { this.defaultVoice = defaultVoice; return this; }
            public Builder enabledEvents(Set<EventType> enabledEvents) { this.enabledEvents.clear(); this.enabledEvents.addAll(enabledEvents); return this; }
            public Builder volume(double volume) { this.volume = volume; return this; }
            public Builder autoPlay(boolean autoPlay) { this.autoPlay = autoPlay; return this; }

            public NarrationSettings build() {
                if (this.sessionId == null || this.sessionId.isEmpty()) {
                    throw new IllegalArgumentException("Session ID is required");
                }
                return new NarrationSettings(this);
            }
        }
    }

    private final Map<String, NarrationSettings> settings = new ConcurrentHashMap<>();
    private final Map<String, List<Narration>> narrationHistory = new ConcurrentHashMap<>();
    private final Random random = new Random();

    /**
     * Register narration settings for a session.
     */
    public void registerSettings(NarrationSettings narrationSettings) {
        settings.put(narrationSettings.getSessionId(), narrationSettings);
    }

    /**
     * Get narration settings for a session, creating defaults if not registered.
     */
    public NarrationSettings getSettings(String sessionId) {
        return settings.computeIfAbsent(sessionId,
                s -> NarrationSettings.builder(s).build());
    }

    /**
     * Generate narration for a game event.
     */
    public Narration narrate(String sessionId, EventType eventType, Map<String, Object> data) {
        NarrationSettings narrationSettings = getSettings(sessionId);
        if (!narrationSettings.shouldNarrate(eventType)) {
            return null;
        }

        String text = generateNarrationText(eventType, data);
        String ssml = toSsml(text, narrationSettings.getDefaultVoice());

        Narration narration = Narration.builder()
                .sessionId(sessionId)
                .eventType(eventType)
                .voiceProfile(narrationSettings.getDefaultVoice())
                .text(text)
                .ssml(ssml)
                .metadata(data)
                .build();

        // Store in history
        narrationHistory.computeIfAbsent(sessionId, k -> new ArrayList<>()).add(narration);

        // Trim history to last 100 entries
        List<Narration> history = narrationHistory.get(sessionId);
        if (history.size() > 100) {
            synchronized (history) {
                while (history.size() > 100) {
                    history.remove(0);
                }
            }
        }

        return narration;
    }

    /**
     * Generate narration text for an event type with data.
     */
    public String generateNarrationText(EventType eventType, Map<String, Object> data) {
        String player = getString(data, "player", "An unknown player");
        String subject = getString(data, "subject", "the fleet");
        String target = getString(data, "target", "an unknown location");
        String count = getString(data, "count", "several");
        String detail = getString(data, "detail", "");

        switch (eventType) {
            case FLEET_MOVE:
                return String.format("%s has moved %s to %s.", player, subject, target);
            case COMBAT:
                return String.format("Combat! %s engages %s. %s vessels involved.", player, target, count);
            case BUILD:
                return String.format("%s begins construction of %s at %s.", player, subject, target);
            case COLONIZE:
                return String.format("New colony! %s establishes %s on %s.", player, subject, target);
            case TRADE:
                return String.format("Trade activity: %s completes %s transactions with %s.", player, count, target);
            case DIPLOMACY:
                return String.format("Diplomatic update: %s changes relations with %s. %s", player, target, detail);
            case ECONOMY:
                return String.format("Economic shift: %s experiences %s. %s", player, subject, detail);
            case TURN_ADVANCE:
                return String.format("Turn %s begins. %s", count, detail);
            case PLAYER_JOIN:
                return String.format("%s joins the game.", player);
            case PLAYER_LEAVE:
                return String.format("%s has left the game.", player);
            case VICTORY:
                return String.format("Victory! %s defeats %s! %s", player, target, detail);
            case DEFEAT:
                return String.format("Defeat. %s is overcome by %s. %s", player, target, detail);
            case DISCOVERY:
                return String.format("Discovery! %s discovers %s at %s. %s", player, subject, target, detail);
            case WARNING:
                return String.format("Warning: %s ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â %s at %s. %s", player, subject, target, detail);
            case SYSTEM:
            default:
                return String.format("System: %s %s. %s", subject, target, detail);
        }
    }

    /**
     * Convert plain text to SSML markup with voice profile settings.
     */
    public static String toSsml(String text, VoiceProfile voiceProfile) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        return String.format(
                "<speak><prosody pitch=\"%f\" rate=\"%f\">%s</prosody></speak>",
                voiceProfile.getPitch(), voiceProfile.getRate(), escapeSsml(text));
    }

    /**
     * Escape special SSML characters in plain text.
     */
    private static String escapeSsml(String text) {
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }

    /**
     * Get narration history for a session.
     */
    public List<Narration> getHistory(String sessionId) {
        List<Narration> history = narrationHistory.get(sessionId);
        return history != null ? new ArrayList<>(history) : Collections.emptyList();
    }

    /**
     * Get recent narrations for a session.
     */
    public List<Narration> getRecent(String sessionId, int count) {
        List<Narration> history = narrationHistory.get(sessionId);
        if (history == null || history.isEmpty()) return Collections.emptyList();
        int from = Math.max(0, history.size() - count);
        return new ArrayList<>(history.subList(from, history.size()));
    }

    /**
     * Get narration history filtered by event type.
     */
    public List<Narration> getHistoryByType(String sessionId, EventType eventType) {
        return getHistory(sessionId).stream()
                .filter(n -> n.getEventType() == eventType)
                .collect(Collectors.toList());
    }

    /**
     * Get narration statistics for a session.
     */
    public Map<String, Object> getStatistics(String sessionId) {
        List<Narration> history = narrationHistory.get(sessionId);
        if (history == null || history.isEmpty()) {
            return Map.of("sessionId", sessionId, "totalNarrations", 0);
        }

        Map<EventType, Long> byType = history.stream()
                .collect(Collectors.groupingBy(Narration::getEventType, Collectors.counting()));

        return Map.of(
                "sessionId", sessionId,
                "totalNarrations", history.size(),
                "byType", byType,
                "byVoice", history.stream()
                        .collect(Collectors.groupingBy(Narration::getVoiceProfile, Collectors.counting())),
                "lastNarration", history.get(history.size() - 1).getTimestamp().toString()
        );
    }

    /**
     * Clear narration history for a session.
     */
    public void clearHistory(String sessionId) {
        narrationHistory.remove(sessionId);
    }

    /**
     * Clear all narration data.
     */
    public void clearAll() {
        settings.clear();
        narrationHistory.clear();
    }

    /**
     * Get all event types.
     */
    public List<Map<String, String>> getAllEventTypes() {
        List<Map<String, String>> types = new ArrayList<>();
        for (EventType type : EventType.values()) {
            types.add(Map.of("name", type.name(), "label", type.getName(), "description", type.getDescription()));
        }
        return types;
    }

    /**
     * Get all voice profiles.
     */
    public List<Map<String, Object>> getAllVoiceProfiles() {
        List<Map<String, Object>> profiles = new ArrayList<>();
        for (VoiceProfile profile : VoiceProfile.values()) {
            profiles.add(Map.of(
                    "name", profile.name(),
                    "label", profile.getName(),
                    "description", profile.getDescription(),
                    "pitch", profile.getPitch(),
                    "rate", profile.getRate()
            ));
        }
        return profiles;
    }

    /**
     * Get all registered session IDs.
     */
    public List<String> getAllSessions() {
        return new ArrayList<>(settings.keySet());
    }

    private static String getString(Map<String, Object> data, String key, String defaultValue) {
        if (data == null || !data.containsKey(key)) return defaultValue;
        Object value = data.get(key);
        return value != null ? value.toString() : defaultValue;
    }
}
