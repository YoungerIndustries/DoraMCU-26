package com.yisy.modules.doramcu.controller.voice;
import com.yisy.modules.doramcu.model.players.Player;

import com.yisy.modules.doramcu.service.voice.TtsService;
import com.yisy.modules.doramcu.service.voice.TtsService.Narration;
import com.yisy.modules.doramcu.service.voice.TtsService.NarrationSettings;
import com.yisy.modules.doramcu.service.voice.TtsService.EventType;
import com.yisy.modules.doramcu.service.voice.TtsService.VoiceProfile;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * REST controller for TTS narration of game events.
 * <p>
 * Provides endpoints for generating narration, managing settings,
 * retrieving history, and configuring voice profiles.
 * </p>
 * <p>
 * All endpoints are under {@code /api/v1/tts}.
 * </p>
 */
@RestController
@RequestMapping("/api/v1/tts")
public class TtsController {

    private final TtsService ttsService;

    public TtsController(TtsService ttsService) {
        this.ttsService = ttsService;
    }

    /**
     * Generate narration for a game event.
     *
     * @param sessionId the game session ID
     * @param eventType the type of event
     * @param data      event data (player, subject, target, count, detail)
     * @return the narration with text and SSML
     */
    @PostMapping("/narrate/{sessionId}/{eventType}")
    public ResponseEntity<?> narrate(@PathVariable String sessionId,
                                     @PathVariable String eventType,
                                     @RequestBody(required = false) Map<String, Object> data) {
        try {
            EventType et;
            try {
                et = EventType.valueOf(eventType);
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid event type: " + eventType));
            }

            Narration narration = ttsService.narrate(sessionId, et, data != null ? data : Collections.emptyMap());
            if (narration == null) {
                return ResponseEntity.ok(Map.of("narrated", false, "reason", "Event type disabled for this session"));
            }

            return ResponseEntity.ok(Map.of(
                    "id", narration.getId(),
                    "sessionId", narration.getSessionId(),
                    "eventType", narration.getEventType().name(),
                    "voiceProfile", narration.getVoiceProfile().name(),
                    "text", narration.getText(),
                    "ssml", narration.getSsml(),
                    "timestamp", narration.getTimestamp().toString()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Generate narration text only (no SSML, no history).
     *
     * @param eventType the type of event
     * @param data      event data
     * @return the narration text
     */
    @PostMapping("/text/{eventType}")
    public ResponseEntity<Map<String, String>> generateText(@PathVariable String eventType,
                                                            @RequestBody(required = false) Map<String, Object> data) {
        try {
            EventType et = EventType.valueOf(eventType);
            String text = ttsService.generateNarrationText(et, data != null ? data : Collections.emptyMap());
            return ResponseEntity.ok(Map.of("text", text));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid event type: " + eventType));
        }
    }

    /**
     * Get or create narration settings for a session.
     *
     * @param sessionId the game session ID
     * @return the current settings
     */
    @GetMapping("/settings/{sessionId}")
    public ResponseEntity<Map<String, Object>> getSettings(@PathVariable String sessionId) {
        NarrationSettings settings = ttsService.getSettings(sessionId);
        return ResponseEntity.ok(toSettingsMap(settings));
    }

    /**
     * Update narration settings for a session.
     *
     * @param sessionId the game session ID
     * @param updates   settings to update
     * @return the updated settings
     */
    @PutMapping("/settings/{sessionId}")
    public ResponseEntity<Map<String, Object>> updateSettings(@PathVariable String sessionId,
                                                              @RequestBody Map<String, Object> updates) {
        NarrationSettings settings = ttsService.getSettings(sessionId);

        if (updates.containsKey("enabled")) {
            settings.setEnabled((Boolean) updates.get("enabled"));
        }
        if (updates.containsKey("defaultVoice")) {
            try {
                settings.setDefaultVoice(VoiceProfile.valueOf((String) updates.get("defaultVoice")));
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid voice profile"));
            }
        }
        if (updates.containsKey("volume")) {
            settings.setVolume(((Number) updates.get("volume")).doubleValue());
        }
        if (updates.containsKey("autoPlay")) {
            settings.setAutoPlay((Boolean) updates.get("autoPlay"));
        }
        if (updates.containsKey("enabledEvents")) {
            @SuppressWarnings("unchecked")
            List<String> events = (List<String>) updates.get("enabledEvents");
            Set<EventType> eventSet = EnumSet.noneOf(EventType.class);
            for (String event : events) {
                try {
                    eventSet.add(EventType.valueOf(event));
                } catch (IllegalArgumentException ignored) {
                }
            }
            settings.setEnabledEvents(eventSet);
        }

        ttsService.registerSettings(settings);
        return ResponseEntity.ok(toSettingsMap(settings));
    }

    /**
     * Get narration history for a session.
     *
     * @param sessionId the game session ID
     * @return list of narrations
     */
    @GetMapping("/history/{sessionId}")
    public ResponseEntity<List<Map<String, Object>>> getHistory(@PathVariable String sessionId) {
        List<Narration> history = ttsService.getHistory(sessionId);
        List<Map<String, Object>> result = new ArrayList<>();
        for (Narration n : history) {
            result.add(toNarrationMap(n));
        }
        return ResponseEntity.ok(result);
    }

    /**
     * Get recent narrations for a session.
     *
     * @param sessionId the game session ID
     * @param count     number of recent narrations (default 10)
     * @return list of recent narrations
     */
    @GetMapping("/history/{sessionId}/recent")
    public ResponseEntity<List<Map<String, Object>>> getRecent(@PathVariable String sessionId,
                                                               @RequestParam(defaultValue = "10") int count) {
        List<Narration> recent = ttsService.getRecent(sessionId, count);
        List<Map<String, Object>> result = new ArrayList<>();
        for (Narration n : recent) {
            result.add(toNarrationMap(n));
        }
        return ResponseEntity.ok(result);
    }

    /**
     * Get narration history filtered by event type.
     *
     * @param sessionId the game session ID
     * @param eventType the event type to filter by
     * @return filtered list of narrations
     */
    @GetMapping("/history/{sessionId}/type/{eventType}")
    public ResponseEntity<List<Map<String, Object>>> getHistoryByType(@PathVariable String sessionId,
                                                                      @PathVariable String eventType) {
        try {
            EventType et = EventType.valueOf(eventType);
            List<Narration> history = ttsService.getHistoryByType(sessionId, et);
            List<Map<String, Object>> result = new ArrayList<>();
            for (Narration n : history) {
                result.add(toNarrationMap(n));
            }
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Collections.emptyList());
        }
    }

    /**
     * Get narration statistics for a session.
     *
     * @param sessionId the game session ID
     * @return statistics map
     */
    @GetMapping("/stats/{sessionId}")
    public ResponseEntity<Map<String, Object>> getStatistics(@PathVariable String sessionId) {
        return ResponseEntity.ok(ttsService.getStatistics(sessionId));
    }

    /**
     * Clear narration history for a session.
     *
     * @param sessionId the game session ID
     * @return confirmation
     */
    @DeleteMapping("/history/{sessionId}")
    public ResponseEntity<Map<String, String>> clearHistory(@PathVariable String sessionId) {
        ttsService.clearHistory(sessionId);
        return ResponseEntity.ok(Map.of("status", "cleared", "sessionId", sessionId));
    }

    /**
     * Get all available event types.
     *
     * @return list of event types
     */
    @GetMapping("/event-types")
    public ResponseEntity<List<Map<String, String>>> getEventTypes() {
        return ResponseEntity.ok(ttsService.getAllEventTypes());
    }

    /**
     * Get all available voice profiles.
     *
     * @return list of voice profiles
     */
    @GetMapping("/voice-profiles")
    public ResponseEntity<List<Map<String, Object>>> getVoiceProfiles() {
        return ResponseEntity.ok(ttsService.getAllVoiceProfiles());
    }

    /**
     * Get all sessions with narration settings.
     *
     * @return list of session IDs
     */
    @GetMapping("/sessions")
    public ResponseEntity<List<String>> getSessions() {
        return ResponseEntity.ok(ttsService.getAllSessions());
    }

    private Map<String, Object> toSettingsMap(NarrationSettings settings) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("sessionId", settings.getSessionId());
        map.put("enabled", settings.isEnabled());
        map.put("defaultVoice", settings.getDefaultVoice().name());
        map.put("enabledEvents", settings.getEnabledEvents().stream().map(Enum::name).toList());
        map.put("volume", settings.getVolume());
        map.put("autoPlay", settings.isAutoPlay());
        return map;
    }

    private Map<String, Object> toNarrationMap(Narration narration) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", narration.getId());
        map.put("sessionId", narration.getSessionId());
        map.put("eventType", narration.getEventType().name());
        map.put("voiceProfile", narration.getVoiceProfile().name());
        map.put("text", narration.getText());
        map.put("ssml", narration.getSsml());
        map.put("timestamp", narration.getTimestamp().toString());
        return map;
    }
}
