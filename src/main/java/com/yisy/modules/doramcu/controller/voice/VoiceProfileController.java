package com.yisy.modules.doramcu.controller.voice;

import com.yisy.modules.doramcu.ai.client.AiRestClient;
import com.yisy.modules.doramcu.ai.client.AiRestClient.SynthesizeResponse;
import com.yisy.modules.doramcu.eve.model.VoiceProfile;
import com.yisy.modules.doramcu.service.voice.VoiceProfileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST controller for voice profile management and character-aware TTS.
 * <p>
 * Provides endpoints for CRUD operations on voice profiles and synthesizing
 * speech using a character's assigned voice profile.
 * </p>
 */
@RestController
@RequestMapping("/api/ai/voice/profiles")
public class VoiceProfileController {

    private static final Logger log = LoggerFactory.getLogger(VoiceProfileController.class);

    private final VoiceProfileService voiceProfileService;
    private final AiRestClient aiRestClient;

    public VoiceProfileController(VoiceProfileService voiceProfileService, AiRestClient aiRestClient) {
        this.voiceProfileService = voiceProfileService;
        this.aiRestClient = aiRestClient;
    }

    /**
     * Create a new voice profile.
     */
    @PostMapping
    public ResponseEntity<?> createProfile(@RequestBody VoiceProfile profile) {
        try {
            if (profile.getName() == null || profile.getName().isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Profile name is required"));
            }
            VoiceProfile created = voiceProfileService.createProfile(profile);
            return ResponseEntity.ok(created);
        } catch (Exception e) {
            log.error("Failed to create voice profile", e);
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to create profile: " + e.getMessage()));
        }
    }

    /**
     * List all voice profiles.
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllProfiles() {
        List<VoiceProfile> profiles = voiceProfileService.getAllProfiles();
        return ResponseEntity.ok(Map.of("profiles", profiles));
    }

    /**
     * Get a voice profile by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getProfile(@PathVariable Long id) {
        return voiceProfileService.getProfileById(id)
                .map(p -> ResponseEntity.ok(p))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Update a voice profile.
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateProfile(@PathVariable Long id, @RequestBody VoiceProfile updates) {
        try {
            return voiceProfileService.updateProfile(id, updates)
                    .map(p -> ResponseEntity.ok(p))
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            log.error("Failed to update voice profile", e);
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to update profile: " + e.getMessage()));
        }
    }

    /**
     * Delete a voice profile.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProfile(@PathVariable Long id) {
        boolean deleted = voiceProfileService.deleteProfile(id);
        if (deleted) {
            return ResponseEntity.ok(Map.of("message", "Profile deleted"));
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * Get the default voice profile.
     */
    @GetMapping("/default")
    public ResponseEntity<?> getDefaultProfile() {
        java.util.Optional<VoiceProfile> profile = voiceProfileService.getDefaultProfile();
        if (profile.isPresent()) {
            return ResponseEntity.ok(profile.get());
        }
        return ResponseEntity.ok(Map.of("message", "No default profile found"));
    }

    /**
     * Get the voice profile assigned to a character.
     */
    @GetMapping("/character/{characterId}")
    public ResponseEntity<?> getProfileByCharacter(@PathVariable Long characterId) {
        java.util.Optional<VoiceProfile> profile = voiceProfileService.getProfileByCharacterId(characterId);
        if (profile.isPresent()) {
            return ResponseEntity.ok(profile.get());
        }
        return ResponseEntity.ok(Map.of("message", "No profile assigned to this character"));
    }

    /**
     * Assign a voice profile to an AI character.
     */
    @PostMapping("/character/{characterId}/assign")
    public ResponseEntity<?> assignProfile(@PathVariable Long characterId, @RequestBody Map<String, Object> body) {
        Object profileIdObj = body.get("profileId");
        if (profileIdObj == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "profileId is required"));
        }
        long profileId;
        try {
            profileId = ((Number) profileIdObj).longValue();
        } catch (ClassCastException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "profileId must be a number"));
        }
        boolean assigned = voiceProfileService.assignProfileToCharacter(characterId, profileId);
        if (assigned) {
            return ResponseEntity.ok(Map.of("message", "Profile assigned"));
        }
        return ResponseEntity.badRequest().body(Map.of("error", "Character or profile not found"));
    }

    /**
     * Remove voice profile assignment from an AI character.
     */
    @DeleteMapping("/character/{characterId}/assign")
    public ResponseEntity<?> removeProfile(@PathVariable Long characterId) {
        boolean removed = voiceProfileService.removeProfileFromCharacter(characterId);
        if (removed) {
            return ResponseEntity.ok(Map.of("message", "Profile removed"));
        }
        return ResponseEntity.badRequest().body(Map.of("error", "Character not found"));
    }

    /**
     * Synthesize text to speech using a character's voice profile.
     * If the character has no profile, falls back to the default profile or OllamaConfig defaults.
     */
    @PostMapping("/synthesize/character")
    public ResponseEntity<?> synthesizeWithCharacter(@RequestBody Map<String, Object> body) {
        try {
            Object characterIdObj = body.get("characterId");
            String text = (String) body.get("text");

            if (text == null || text.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Text is required"));
            }
            if (characterIdObj == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "characterId is required"));
            }

            Long characterId;
            try {
                characterId = ((Number) characterIdObj).longValue();
            } catch (ClassCastException e) {
                return ResponseEntity.badRequest().body(Map.of("error", "characterId must be a number"));
            }

            // Build synthesis request
            String model = body.containsKey("model") ? body.get("model").toString() : null;
            String voice = body.containsKey("voice") ? body.get("voice").toString() : null;

            // Look up character's voice profile
            com.yisy.modules.doramcu.eve.model.VoiceProfile profile =
                    voiceProfileService.getProfileByCharacterId(characterId).orElse(null);

            if (profile != null) {
                log.info("Synthesizing for character {} with profile: {}", characterId, profile.getName());
                SynthesizeResponse response = aiRestClient.synthesizeWithProfile(
                        text,
                        profile.getTtsModel(),
                        profile.getTtsVoice(),
                        profile.getPitch(),
                        profile.getSpeed(),
                        profile.getTone()
                );
                return ResponseEntity.ok(response);
            } else {
                // Fallback: try default profile
                com.yisy.modules.doramcu.eve.model.VoiceProfile defaultProfile =
                        voiceProfileService.getDefaultProfile().orElse(null);

                if (defaultProfile != null) {
                    log.info("Synthesizing for character {} with default profile", characterId);
                    SynthesizeResponse response = aiRestClient.synthesizeWithProfile(
                            text,
                            defaultProfile.getTtsModel(),
                            defaultProfile.getTtsVoice(),
                            defaultProfile.getPitch(),
                            defaultProfile.getSpeed(),
                            defaultProfile.getTone()
                    );
                    return ResponseEntity.ok(response);
                } else {
                    // No profile at all — use OllamaConfig defaults
                    log.info("Synthesizing for character {} with config defaults", characterId);
                    SynthesizeResponse response = aiRestClient.synthesize(text, model, voice);
                    return ResponseEntity.ok(response);
                }
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Character TTS synthesis failed", e);
            return ResponseEntity.internalServerError().body(Map.of("error", "TTS synthesis failed: " + e.getMessage()));
        }
    }

    /**
     * Speak text using a specific voice profile.
     * Simplified endpoint that takes profileId and text.
     */
    @PostMapping("/speak")
    public ResponseEntity<?> speakWithProfile(@RequestBody Map<String, Object> body) {
        try {
            Object profileIdObj = body.get("profileId");
            String text = (String) body.get("text");

            if (text == null || text.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Text is required"));
            }
            if (profileIdObj == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "profileId is required"));
            }

            Long profileId;
            try {
                profileId = ((Number) profileIdObj).longValue();
            } catch (ClassCastException e) {
                return ResponseEntity.badRequest().body(Map.of("error", "profileId must be a number"));
            }

            VoiceProfile profile = voiceProfileService.getProfileById(profileId).orElse(null);
            if (profile == null) {
                return ResponseEntity.notFound().build();
            }

            log.info("Speaking with profile {}: {}", profileId, profile.getName());
            SynthesizeResponse response = aiRestClient.synthesizeWithProfile(
                    text,
                    profile.getTtsModel(),
                    profile.getTtsVoice(),
                    profile.getPitch(),
                    profile.getSpeed(),
                    profile.getTone()
            );
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Profile speak failed", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "TTS synthesis failed: " + e.getMessage()));
        }
    }

    /**
     * Synthesize text to speech using a specific voice profile.
     */
    @PostMapping("/synthesize/profile/{profileId}")
    public ResponseEntity<?> synthesizeWithProfile(@PathVariable Long profileId, @RequestBody Map<String, Object> body) {
        String text = (String) body.get("text");
        try {
            if (text == null || text.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Text is required"));
            }

            VoiceProfile profile = voiceProfileService.getProfileById(profileId).orElse(null);
            if (profile == null) {
                return ResponseEntity.notFound().build();
            }

            log.info("Synthesizing with profile {}: {}", profileId, profile.getName());
            SynthesizeResponse response = aiRestClient.synthesizeWithProfile(
                    text,
                    profile.getTtsModel(),
                    profile.getTtsVoice(),
                    profile.getPitch(),
                    profile.getSpeed(),
                    profile.getTone()
            );
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Profile TTS synthesis failed", e);
            return ResponseEntity.internalServerError().body(Map.of("error", "TTS synthesis failed: " + e.getMessage()));
        }
    }
}
