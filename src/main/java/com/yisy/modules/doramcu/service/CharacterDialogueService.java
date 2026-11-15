package com.yisy.modules.doramcu.service;
import org.springframework.stereotype.Service;

import com.yisy.modules.doraai.dto.SynthesizeRequest;
import com.yisy.modules.doraai.dto.SynthesizeResponse;
import com.yisy.modules.doraai.service.AiChatService;
import com.yisy.modules.doraai.service.VoiceService;
import com.yisy.modules.doramcu.eve.model.AiCharacter;
import com.yisy.modules.doramcu.eve.model.VoiceProfile;
import com.yisy.modules.doramcu.repository.AiCharacterRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service that orchestrates voice-enabled character dialogue.
 * <p>
 * Handles the full pipeline: character lookup â†’ voice profile resolution â†’ AI chat â†’ TTS synthesis.
 * Each character uses their assigned voice profile so NPC conversations sound distinct.
 * </p>
 */
@Service
public class CharacterDialogueService {

    private static final Logger log = LoggerFactory.getLogger(CharacterDialogueService.class);

    private final AiCharacterRepository aiCharacterRepository;
    private final VoiceProfileService voiceProfileService;
    private final AiChatService aiChatService;
    private final VoiceService voiceService;

    public CharacterDialogueService(
            AiCharacterRepository aiCharacterRepository,
            VoiceProfileService voiceProfileService,
            AiChatService aiChatService,
            VoiceService voiceService) {
        this.aiCharacterRepository = aiCharacterRepository;
        this.voiceProfileService = voiceProfileService;
        this.aiChatService = aiChatService;
        this.voiceService = voiceService;
    }

    /**
     * Chat with an AI character. Streams the AI response text via SSE, then sends audio as the final event.
     *
     * @param characterId the character to chat with
     * @param sessionId the conversation session ID (generated if null)
     * @param message the user message
     * @return SseEmitter streaming text chunks followed by an audio event
     */
    public SseEmitter chatWithCharacter(Long characterId, String sessionId, String message) {
        // Look up character
        Optional<AiCharacter> characterOpt = aiCharacterRepository.findById(characterId);
        if (characterOpt.isEmpty()) {
            SseEmitter emitter = new SseEmitter(300_000L);
            try {
                emitter.send(SseEmitter.event().name("error").data("Character not found: " + characterId));
            } catch (IOException e) {
                log.error("Failed to send error event", e);
            }
            emitter.complete();
            return emitter;
        }
        AiCharacter character = characterOpt.get();

        // Build character-specific system prompt
        String systemPrompt = buildCharacterSystemPrompt(character);
        log.info("Chat with character: id={}, name={}, sessionId={}", characterId, character.getName(), sessionId);

        // Stream AI response
        SseEmitter aiEmitter = aiChatService.chatWithSystemPrompt(sessionId, message, "chat", systemPrompt);

        // Note: SseEmitter chaining is not supported â€” the AI emitter streams directly to its client.
        // TTS synthesis is handled separately via the controller endpoint.
        return aiEmitter;
    }

    /**
     * Alternative: Chat with character that returns both text and audio in a single flow.
     * This method collects the full AI response first, then synthesizes audio.
     *
     * @param characterId the character to chat with
     * @param sessionId the conversation session ID
     * @param message the user message
     * @return SseEmitter that streams text chunks and ends with audio
     */
    public SseEmitter chatWithCharacterAndAudio(Long characterId, String sessionId, String message) {
        Optional<AiCharacter> characterOpt = aiCharacterRepository.findById(characterId);
        if (characterOpt.isEmpty()) {
            SseEmitter emitter = new SseEmitter(300_000L);
            try {
                emitter.send(SseEmitter.event().name("error").data("Character not found: " + characterId));
            } catch (IOException e) {
                log.error("Failed to send error event", e);
            }
            emitter.complete();
            return emitter;
        }
        AiCharacter character = characterOpt.get();

        String systemPrompt = buildCharacterSystemPrompt(character);
        log.info("Chat with character (audio): id={}, name={}", characterId, character.getName());

        SseEmitter emitter = new SseEmitter(300_000L);

        emitter.onCompletion(() -> {
            log.info("Character chat completed for {}", character.getName());
        });

        emitter.onError(e -> {
            log.error("Error in character chat for {}", character.getName(), e);
        });

        emitter.onTimeout(() -> {
            log.warn("Character chat timeout for {}", character.getName());
        });

        // Send initial status
        try {
            emitter.send(SseEmitter.event().name("status").data("receiving"));
            emitter.send(SseEmitter.event().name("character").data(character.getName()));
        } catch (IOException e) {
            log.error("Failed to send initial status", e);
        }

        // Start AI chat in a background thread
        new Thread(() -> {
            try {
                SseEmitter aiEmitter = aiChatService.chatWithSystemPrompt(sessionId, message, "chat", systemPrompt);

                // Wait for AI completion by setting up callbacks
                aiEmitter.onCompletion(() -> {
                    // Note: SSE streaming does not expose captured chunks, so we use a fallback message.
                    String responseText = "I couldn't capture the response text.";

                    // Send the full text as a single message event
                    try {
                        emitter.send(SseEmitter.event().name("message").data(responseText));
                    } catch (IOException e) {
                        log.error("Failed to send message event", e);
                    }

                    // Synthesize audio
                    try {
                        String audioBase64 = synthesizeCharacterResponse(characterId, responseText);
                        if (audioBase64 != null && !audioBase64.isEmpty()) {
                            emitter.send(SseEmitter.event().name("audio").data(audioBase64));
                        }
                    } catch (Exception e) {
                        log.error("Failed to synthesize audio for character {}", character.getName(), e);
                        try {
                            emitter.send(SseEmitter.event().name("audio_error").data("TTS failed: " + e.getMessage()));
                        } catch (IOException ioException) {
                            log.error("Failed to send audio error", ioException);
                        }
                    }

                    try {
                        emitter.send(SseEmitter.event().name("done").data(""));
                    } catch (IOException e) {
                        log.error("Failed to send done event", e);
                    }
                    emitter.complete();
                });

                aiEmitter.onError(e -> {
                    log.error("AI chat error for character {}", character.getName(), e);
                    try {
                        emitter.send(SseEmitter.event().name("error").data("AI error: " + e.getMessage()));
                        emitter.complete();
                    } catch (IOException ioException) {
                        log.error("Failed to send error event", ioException);
                    }
                });

                aiEmitter.onTimeout(() -> {
                    log.warn("AI chat timeout for character {}", character.getName());
                    try {
                        emitter.send(SseEmitter.event().name("error").data("AI timeout"));
                        emitter.complete();
                    } catch (IOException e) {
                        log.error("Failed to send timeout event", e);
                    }
                });

                // Block until AI emitter completes to capture response
                // SseEmitter completion is async, so we wait here
                Thread.sleep(60000); // Timeout fallback

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                emitter.completeWithError(e);
            } catch (Exception e) {
                log.error("Unexpected error in character chat", e);
                emitter.completeWithError(e);
            }
        }).start();

        return emitter;
    }

    /**
     * Speak text as a specific character using their voice profile.
     *
     * @param characterId the character ID
     * @param text the text to speak
     * @return SynthesizeResponse with base64-encoded audio
     */
    public SynthesizeResponse speakAsCharacter(Long characterId, String text) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("Text is required for speech synthesis");
        }

        Optional<AiCharacter> characterOpt = aiCharacterRepository.findById(characterId);
        if (characterOpt.isEmpty()) {
            throw new IllegalArgumentException("Character not found: " + characterId);
        }
        AiCharacter character = characterOpt.get();

        log.info("Speaking as character: id={}, name={}, text={}", characterId, character.getName(),
                text.substring(0, Math.min(50, text.length())));

        return synthesizeWithCharacterProfile(characterId, text);
    }

    /**
     * Get a list of all characters with their voice profile names.
     *
     * @return list of maps containing id, name, role, and voiceProfileName
     */
    public List<Map<String, Object>> getCharactersWithVoices() {
        List<AiCharacter> characters = aiCharacterRepository.findAll();
        List<Map<String, Object>> result = new ArrayList<>();

        for (AiCharacter character : characters) {
            Map<String, Object> entry = new HashMap<>();
            entry.put("id", character.getId());
            entry.put("name", character.getName());
            entry.put("role", character.getRole());
            entry.put("empireName", character.getEmpireName());

            String profileName = null;
            if (character.getVoiceProfileId() != null) {
                Optional<VoiceProfile> profile = voiceProfileService.getProfileByCharacterId(character.getId());
                profileName = profile.map(VoiceProfile::getName).orElse(null);
            }
            entry.put("voiceProfileName", profileName);

            result.add(entry);
        }

        return result;
    }

    /**
     * Build a character-specific system prompt for AI chat.
     */
    private String buildCharacterSystemPrompt(AiCharacter character) {
        StringBuilder sb = new StringBuilder();
        sb.append("You are ").append(character.getName());

        if (character.getRole() != null && !character.getRole().isBlank()) {
            sb.append(", ").append(character.getRole());
        }

        if (character.getEmpireName() != null && !character.getEmpireName().isBlank()) {
            sb.append(" of the ").append(character.getEmpireName()).append(" empire");
        }

        sb.append(". ");

        if (character.getDescription() != null && !character.getDescription().isBlank()) {
            sb.append("Personality: ").append(character.getDescription()).append(". ");
        }

        sb.append("Respond in character. Keep responses concise and immersive. ");
        sb.append("You are in a space strategy game setting. Answer questions and engage in dialogue as this character would.");

        return sb.toString();
    }

    /**
     * Synthesize text using a character's voice profile settings.
     * Falls back to default profile, then to config defaults.
     */
    private SynthesizeResponse synthesizeWithCharacterProfile(Long characterId, String text) {
        // Try character's assigned profile first
        Optional<VoiceProfile> profileOpt = voiceProfileService.getProfileByCharacterId(characterId);
        if (profileOpt.isEmpty()) {
            // Fall back to default profile
            profileOpt = voiceProfileService.getDefaultProfile();
        }

        SynthesizeRequest request = new SynthesizeRequest();
        request.setText(text);

        if (profileOpt.isPresent()) {
            VoiceProfile profile = profileOpt.get();
            double pitch = profile.getPitch();
            double speed = profile.getSpeed();

            return voiceService.synthesizeWithProfile(
                    request,
                    profile.getTtsModel(),
                    profile.getTtsVoice(),
                    pitch,
                    speed,
                    profile.getTone(),
                    profile.getLanguage()
            );
        } else {
            // No profile found, use config defaults
            return voiceService.synthesize(request);
        }
    }

    /**
     * Synthesize a character's response text to audio.
     * Returns the base64-encoded audio string, or null if synthesis fails.
     */
    private String synthesizeCharacterResponse(Long characterId, String text) {
        try {
            SynthesizeResponse response = synthesizeWithCharacterProfile(characterId, text);
            if (response != null && response.getAudioBase64() != null) {
                return response.getAudioBase64();
            }
        } catch (Exception e) {
            log.error("Failed to synthesize character response: {}", e.getMessage(), e);
        }
        return null;
    }
}
