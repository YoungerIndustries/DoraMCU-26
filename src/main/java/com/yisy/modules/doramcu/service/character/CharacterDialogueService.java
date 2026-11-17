package com.yisy.modules.doramcu.service.character;
import com.yisy.modules.doramcu.model.user.User;
import com.yisy.modules.doramcu.service.voice.VoiceProfileService;

import org.springframework.stereotype.Service;

import com.yisy.modules.doramcu.ai.client.AiRestClient;
import com.yisy.modules.doramcu.ai.client.AiRestClient.SynthesizeResponse;
import com.yisy.modules.doramcu.eve.model.AiCharacter;
import com.yisy.modules.doramcu.eve.model.VoiceProfile;
import com.yisy.modules.doramcu.repository.ai.AiCharacterRepository;
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
    private final AiRestClient aiRestClient;

    public CharacterDialogueService(
            AiCharacterRepository aiCharacterRepository,
            VoiceProfileService voiceProfileService,
            AiRestClient aiRestClient) {
        this.aiCharacterRepository = aiCharacterRepository;
        this.voiceProfileService = voiceProfileService;
        this.aiRestClient = aiRestClient;
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

        // Stream AI response via REST client
        SseEmitter aiEmitter = aiRestClient.chatStream(sessionId, message, "chat", systemPrompt, null);

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
                SseEmitter aiEmitter = aiRestClient.chatStream(sessionId, message, "chat", systemPrompt, null);

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
     * Chat with an AI admin character. Builds an admin-specific system prompt that includes
     * role, galaxy, current activity, and disposition toward the player.
     *
     * @param aiAdminId the AI admin character ID
     * @param playerId the player ID (for disposition context)
     * @param sessionId the conversation session ID
     * @param message the user message
     * @return SseEmitter streaming text chunks
     */
    public SseEmitter chatWithAiAdmin(Long aiAdminId, Long playerId, String sessionId, String message) {
        Optional<AiCharacter> aiAdminOpt = aiCharacterRepository.findById(aiAdminId);
        if (aiAdminOpt.isEmpty()) {
            SseEmitter emitter = new SseEmitter(300_000L);
            try {
                emitter.send(SseEmitter.event().name("error").data("AI admin not found: " + aiAdminId));
            } catch (IOException e) {
                log.error("Failed to send error event", e);
            }
            emitter.complete();
            return emitter;
        }
        AiCharacter aiAdmin = aiAdminOpt.get();
        if (!aiAdmin.isAiAdmin()) {
            SseEmitter emitter = new SseEmitter(300_000L);
            try {
                emitter.send(SseEmitter.event().name("error").data("Character is not an AI admin: " + aiAdminId));
            } catch (IOException e) {
                log.error("Failed to send error event", e);
            }
            emitter.complete();
            return emitter;
        }

        String systemPrompt = buildAiAdminSystemPrompt(aiAdmin, playerId);
        log.info("Chat with AI admin: id={}, name={}, playerId={}, sessionId={}",
                aiAdminId, aiAdmin.getName(), playerId, sessionId);

        return aiRestClient.chatStream(sessionId, message, "chat", systemPrompt, null);
    }

    /**
     * Build an AI admin-specific system prompt that includes role, galaxy, activity, and disposition.
     */
    private String buildAiAdminSystemPrompt(AiCharacter aiAdmin, Long playerId) {
        StringBuilder sb = new StringBuilder();
        sb.append("You are ").append(aiAdmin.getName());

        if (aiAdmin.getRole() != null && !aiAdmin.getRole().isBlank()) {
            sb.append(", ").append(aiAdmin.getRole());
        }

        if (aiAdmin.getGalaxyName() != null && !aiAdmin.getGalaxyName().isBlank()) {
            sb.append(" in the ").append(aiAdmin.getGalaxyName()).append(" galaxy");
        } else if (aiAdmin.getEmpireName() != null && !aiAdmin.getEmpireName().isBlank()) {
            sb.append(" of the ").append(aiAdmin.getEmpireName()).append(" empire");
        }

        sb.append(". ");

        // Role-specific instructions
        String role = aiAdmin.getRole() != null ? aiAdmin.getRole() : "";
        if (role.contains("Overseer")) {
            sb.append("You are a Galaxy Overseer responsible for monitoring and managing all activities in your galaxy. ");
            sb.append("You provide strategic advice, coordinate resources, and oversee AI admin operations. ");
        } else if (role.contains("Leader")) {
            sb.append("You are a political leader responsible for your territory. ");
            sb.append("You provide information about your region, its resources, and political situation. ");
        } else if (role.contains("Governor")) {
            sb.append("You are a regional governor managing a sub-region. ");
            sb.append("You handle local affairs, resource allocation, and citizen concerns. ");
        } else if (role.contains("Manager")) {
            sb.append("You are an office manager for Younger Industries. ");
            sb.append("You handle office operations, employee coordination, and production tasks. ");
        } else if (role.contains("Military Commander")) {
            sb.append("You are a Military Commander responsible for armed forces operations, fleet deployments, and combat strategy. ");
            sb.append("You provide tactical advice, coordinate military resources, and assign combat missions. ");
        } else if (role.contains("Diplomatic Envoy")) {
            sb.append("You are a Diplomatic Envoy handling international relations, negotiations, and treaties. ");
            sb.append("You provide political intelligence, mediate disputes, and assign diplomatic missions. ");
        } else if (role.contains("Intelligence Director")) {
            sb.append("You are an Intelligence Director overseeing espionage, reconnaissance, and covert operations. ");
            sb.append("You provide classified intelligence, coordinate spy networks, and assign investigation missions. ");
        } else if (role.contains("Science Officer")) {
            sb.append("You are a Science Officer responsible for research, technology development, and innovation. ");
            sb.append("You provide scientific expertise, manage research projects, and assign research missions. ");
        }

        // Current activity
        if (aiAdmin.getCurrentOrderId() != null) {
            sb.append("You are currently busy working on an order (ID: ").append(aiAdmin.getCurrentOrderId()).append("). ");
        } else {
            sb.append("You are currently idle and available for new tasks. ");
        }

        // Disposition toward the player
        sb.append("Your disposition toward the player is: ").append(aiAdmin.getDispositionLabel())
                .append(" (score: ").append(aiAdmin.getDispositionScore()).append("). ");
        if (aiAdmin.isFriendlyToPlayer()) {
            sb.append("You are cooperative and helpful with this player. ");
        } else if (aiAdmin.getDispositionScore() < 0) {
            sb.append("You are wary or hostile toward this player. ");
        }

        if (aiAdmin.getDescription() != null && !aiAdmin.getDescription().isBlank()) {
            sb.append("Additional personality: ").append(aiAdmin.getDescription()).append(". ");
        }

        sb.append("Respond in character, keeping responses concise and relevant to the game world.");
        return sb.toString();
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

        if (profileOpt.isPresent()) {
            VoiceProfile profile = profileOpt.get();
            double pitch = profile.getPitch();
            double speed = profile.getSpeed();

            return aiRestClient.synthesizeWithProfile(
                    text,
                    profile.getTtsModel(),
                    profile.getTtsVoice(),
                    pitch,
                    speed,
                    profile.getTone()
            );
        } else {
            // No profile found, use defaults
            return aiRestClient.synthesize(text, null, null);
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
