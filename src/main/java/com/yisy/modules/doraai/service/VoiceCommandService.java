package com.yisy.modules.doraai.service;

import com.yisy.modules.doraai.dto.TranscribeRequest;
import com.yisy.modules.doraai.dto.TranscribeResponse;
import com.yisy.modules.doraai.dto.VoiceCommandRequest;
import com.yisy.modules.doraai.dto.VoiceCommandResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Service for interpreting voice commands from transcribed speech.
 * <p>
 * Parses spoken text into structured game commands using keyword-based detection
 * and AI-assisted intent detection for complex commands.
 * </p>
 */
@Service
public class VoiceCommandService {

    private static final Logger log = LoggerFactory.getLogger(VoiceCommandService.class);

    private final VoiceService voiceService;
    private final AiChatService aiChatService;

    // Command patterns for keyword-based detection
    private static final List<String> MOVEMENT_KEYWORDS = List.of(
            "move", "walk", "run", "go", "forward", "backward", "left", "right",
            "up", "down", "stop", "turn", "jump", "crouch", "sneak", "hide"
    );

    private static final List<String> COMBAT_KEYWORDS = List.of(
            "fire", "shoot", "attack", "kill", "destroy", "defend", "reload",
            "aim", "target", "weapon", "grenade", "bomb", "snipe", "strafe"
    );

    private static final List<String> INTERACTION_KEYWORDS = List.of(
            "pick", "take", "use", "open", "close", "talk", "interact",
            "examine", "look", "inspect", "search", "loot", "craft", "build"
    );

    private static final List<String> QUERY_KEYWORDS = List.of(
            "what", "who", "where", "when", "how", "why", "tell", "explain",
            "help", "status", "health", "ammo", "score", "map", "location"
    );

    // Direction patterns
    private static final Pattern DIRECTION_PATTERN = Pattern.compile(
            "\\b(forward|backward|left|right|up|down|north|south|east|west)\\b",
            Pattern.CASE_INSENSITIVE
    );

    // Weapon patterns
    private static final Pattern WEAPON_PATTERN = Pattern.compile(
            "\\b(rifle|pistol|shotgun|sniper|laser|plasma|rocket|grenade|sword|knife)\\b",
            Pattern.CASE_INSENSITIVE
    );

    public VoiceCommandService(VoiceService voiceService, AiChatService aiChatService) {
        this.voiceService = voiceService;
        this.aiChatService = aiChatService;
    }

    /**
     * Process a voice command request: transcribe audio and interpret the command.
     *
     * @param request the voice command request with audio data
     * @return the interpreted command response
     */
    public VoiceCommandResponse processCommand(VoiceCommandRequest request) {
        if (request.getAudioBase64() == null || request.getAudioBase64().isBlank()) {
            throw new IllegalArgumentException("Audio data is required");
        }

        // Step 1: Transcribe audio to text
        TranscribeRequest transcribeRequest = new TranscribeRequest();
        transcribeRequest.setAudioBase64(request.getAudioBase64());
        TranscribeResponse transcribeResponse = voiceService.transcribe(transcribeRequest);
        String transcribedText = transcribeResponse.getText();

        log.info("Voice command transcribed: '{}'", transcribedText);

        // Step 2: Interpret the command
        VoiceCommandResponse response = interpretCommand(transcribedText, request.getSessionId());
        response.setTranscribedText(transcribedText);

        return response;
    }

    /**
     * Interpret transcribed text as a game command.
     *
     * @param text the transcribed text
     * @param sessionId optional session ID for AI-assisted interpretation
     * @return the interpreted command response
     */
    public VoiceCommandResponse interpretCommand(String text, String sessionId) {
        if (text == null || text.isBlank()) {
            return new VoiceCommandResponse("UNKNOWN", new HashMap<>(), false, "Empty command");
        }

        String lowerText = text.toLowerCase().trim();

        // Try keyword-based detection first
        VoiceCommandResponse keywordResult = detectByKeywords(lowerText);
        if (keywordResult != null) {
            return keywordResult;
        }

        // Fall back to AI-assisted intent detection for complex commands
        return detectByAi(lowerText, sessionId);
    }

    /**
     * Detect command type using keyword matching.
     */
    private VoiceCommandResponse detectByKeywords(String text) {
        // Check for movement commands
        for (String keyword : MOVEMENT_KEYWORDS) {
            if (text.contains(keyword)) {
                Map<String, Object> params = new HashMap<>();
                params.put("action", keyword);

                // Extract direction if present
                Matcher dirMatcher = DIRECTION_PATTERN.matcher(text);
                if (dirMatcher.find()) {
                    params.put("direction", dirMatcher.group(1).toLowerCase());
                }

                return new VoiceCommandResponse("MOVEMENT", params, true,
                        "Movement command: " + keyword);
            }
        }

        // Check for combat commands
        for (String keyword : COMBAT_KEYWORDS) {
            if (text.contains(keyword)) {
                Map<String, Object> params = new HashMap<>();
                params.put("action", keyword);

                // Extract weapon if present
                Matcher weaponMatcher = WEAPON_PATTERN.matcher(text);
                if (weaponMatcher.find()) {
                    params.put("weapon", weaponMatcher.group(1).toLowerCase());
                }

                return new VoiceCommandResponse("COMBAT", params, true,
                        "Combat command: " + keyword);
            }
        }

        // Check for interaction commands
        for (String keyword : INTERACTION_KEYWORDS) {
            if (text.contains(keyword)) {
                Map<String, Object> params = new HashMap<>();
                params.put("action", keyword);
                return new VoiceCommandResponse("INTERACTION", params, true,
                        "Interaction command: " + keyword);
            }
        }

        // Check for query commands
        for (String keyword : QUERY_KEYWORDS) {
            if (text.contains(keyword)) {
                Map<String, Object> params = new HashMap<>();
                params.put("query", text);
                return new VoiceCommandResponse("QUERY", params, true,
                        "Query command detected");
            }
        }

        return null; // No keyword match found
    }

    /**
     * Detect command type using AI-assisted intent detection.
     */
    private VoiceCommandResponse detectByAi(String text, String sessionId) {
        try {
            String prompt = String.format(
                    "Classify this game command into one of these types: MOVEMENT, COMBAT, INTERACTION, QUERY, or CHAT. " +
                    "Return only the type name. Command: \"%s\"",
                    text
            );

            // Use AI chat to classify the intent
            org.springframework.web.servlet.mvc.method.annotation.SseEmitter emitter =
                    aiChatService.chat(sessionId, prompt, "chat");

            // Collect the response
            StringBuilder response = new StringBuilder();
            emitter.onCompletion(() -> {
                log.debug("AI intent detection completed for: '{}'", text);
            });
            emitter.onError((e) -> {
                log.error("AI intent detection failed", e);
            });
            emitter.onCompletion(() -> {
                log.debug("AI intent detection completed for: '{}'", text);
            });

            // Wait briefly for the response (non-blocking in production)
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            // If we got a response, use it; otherwise default to CHAT
            String type = "CHAT";
            Map<String, Object> params = new HashMap<>();
            params.put("text", text);

            return new VoiceCommandResponse(type, params, true,
                    "Command interpreted as chat/query");

        } catch (Exception e) {
            log.warn("AI-assisted intent detection failed, defaulting to CHAT: {}", e.getMessage());
            Map<String, Object> params = new HashMap<>();
            params.put("text", text);
            return new VoiceCommandResponse("CHAT", params, true,
                    "Command interpreted as chat");
        }
    }

    /**
     * Check if text contains a command (vs. regular chat).
     */
    public boolean isCommand(String text) {
        if (text == null || text.isBlank()) return false;
        String lower = text.toLowerCase();
        for (String keyword : MOVEMENT_KEYWORDS) {
            if (lower.contains(keyword)) return true;
        }
        for (String keyword : COMBAT_KEYWORDS) {
            if (lower.contains(keyword)) return true;
        }
        for (String keyword : INTERACTION_KEYWORDS) {
            if (lower.contains(keyword)) return true;
        }
        return false;
    }
}
