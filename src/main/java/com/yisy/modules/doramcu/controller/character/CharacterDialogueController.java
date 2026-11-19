package com.yisy.modules.doramcu.controller.character;

import com.yisy.modules.doramcu.service.character.CharacterDialogueService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST controller for voice-enabled character dialogue.
 * <p>
 * Provides endpoints for chatting with AI characters (with voice responses),
 * speaking text as a character, and listing characters with voice profiles.
 * </p>
 */
@RestController
@RequestMapping("/api/ai/voice/character")
public class CharacterDialogueController {

    private static final Logger log = LoggerFactory.getLogger(CharacterDialogueController.class);

    private final CharacterDialogueService characterDialogueService;

    public CharacterDialogueController(CharacterDialogueService characterDialogueService) {
        this.characterDialogueService = characterDialogueService;
    }

    /**
     * Chat with an AI character. The response is streamed via SSE with text and audio.
     *
     * @param request body containing characterId, sessionId, and message
     * @return SseEmitter streaming text chunks and final audio
     */
    @PostMapping("/chat")
    public SseEmitter chatWithCharacter(@RequestBody CharacterChatRequest request) {
        if (request.getCharacterId() == null) {
            SseEmitter emitter = new SseEmitter(300_000L);
            try {
                emitter.send(SseEmitter.event().name("error").data("Character ID is required"));
            } catch (Exception e) {
                // ignore
            }
            emitter.complete();
            return emitter;
        }

        if (request.getMessage() == null || request.getMessage().isBlank()) {
            SseEmitter emitter = new SseEmitter(300_000L);
            try {
                emitter.send(SseEmitter.event().name("error").data("Message is required"));
            } catch (Exception e) {
                // ignore
            }
            emitter.complete();
            return emitter;
        }

        log.info("Character chat request: characterId={}, sessionId={}",
                request.getCharacterId(), request.getSessionId());

        return characterDialogueService.chatWithCharacterAndAudio(
                request.getCharacterId(),
                request.getSessionId(),
                request.getMessage()
        );
    }

    /**
     * Speak text as a specific character using their voice profile.
     *
     * @param request body containing characterId and text
     * @return SynthesizeResponse with base64-encoded audio
     */
    @PostMapping("/speak")
    public ResponseEntity<?> speakAsCharacter(@RequestBody CharacterSpeakRequest request) {
        if (request.getCharacterId() == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Character ID is required"));
        }

        if (request.getText() == null || request.getText().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Text is required"));
        }

        try {
            SynthesizeResponse response = characterDialogueService.speakAsCharacter(
                    request.getCharacterId(),
                    request.getText()
            );
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Failed to speak as character", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to synthesize speech: " + e.getMessage()));
        }
    }

    /**
     * List all characters with their voice profile names.
     *
     * @return ResponseEntity containing the list of characters with voice info
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getCharactersWithVoices() {
        List<Map<String, Object>> characters = characterDialogueService.getCharactersWithVoices();
        return ResponseEntity.ok(Map.of("characters", characters));
    }

    /**
     * Request body for character chat.
     */
    public static class CharacterChatRequest {
        private Long characterId;
        private String sessionId;
        private String message;

        public Long getCharacterId() {
            return characterId;
        }

        public void setCharacterId(Long characterId) {
            this.characterId = characterId;
        }

        public String getSessionId() {
            return sessionId;
        }

        public void setSessionId(String sessionId) {
            this.sessionId = sessionId;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }

    /**
     * Request body for character speak.
     */
    public static class CharacterSpeakRequest {
        private Long characterId;
        private String text;

        public Long getCharacterId() {
            return characterId;
        }

        public void setCharacterId(Long characterId) {
            this.characterId = characterId;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }
    }
}
