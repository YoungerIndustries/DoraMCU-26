package com.yisy.modules.doramcu.service.voice;
import org.springframework.stereotype.Service;

import com.yisy.modules.doramcu.eve.model.AiCharacter;
import com.yisy.modules.doramcu.eve.model.VoiceProfile;
import com.yisy.modules.doramcu.repository.ai.AiCharacterRepository;
import com.yisy.modules.doramcu.repository.misc.VoiceProfileRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service for managing TTS voice profiles that can be assigned to AI characters.
 * Provides CRUD operations and default profile creation on startup.
 */
@Service
public class VoiceProfileService {

    private static final Logger log = LoggerFactory.getLogger(VoiceProfileService.class);

    private final VoiceProfileRepository voiceProfileRepository;
    private final AiCharacterRepository aiCharacterRepository;

    public VoiceProfileService(
            VoiceProfileRepository voiceProfileRepository,
            AiCharacterRepository aiCharacterRepository) {
        this.voiceProfileRepository = voiceProfileRepository;
        this.aiCharacterRepository = aiCharacterRepository;
    }

    // ---- CRUD Operations ----

    /**
     * Create a new voice profile.
     */
    @Transactional
    public VoiceProfile createProfile(VoiceProfile profile) {
        log.info("Creating voice profile: {}", profile.getName());
        return voiceProfileRepository.save(profile);
    }

    /**
     * Update an existing voice profile.
     */
    @Transactional
    public Optional<VoiceProfile> updateProfile(Long id, VoiceProfile updates) {
        Optional<VoiceProfile> existing = voiceProfileRepository.findById(id);
        if (existing.isEmpty()) {
            return Optional.empty();
        }
        VoiceProfile profile = existing.get();
        if (updates.getName() != null) {
            profile.setName(updates.getName());
        }
        if (updates.getDescription() != null) {
            profile.setDescription(updates.getDescription());
        }
        if (updates.getTtsModel() != null) {
            profile.setTtsModel(updates.getTtsModel());
        }
        if (updates.getTtsVoice() != null) {
            profile.setTtsVoice(updates.getTtsVoice());
        }
        profile.setPitch(updates.getPitch());
        profile.setSpeed(updates.getSpeed());
        if (updates.getTone() != null) {
            profile.setTone(updates.getTone());
        }
        if (updates.getLanguage() != null) {
            profile.setLanguage(updates.getLanguage());
        }
        log.info("Updated voice profile: {}", profile.getName());
        return Optional.of(voiceProfileRepository.save(profile));
    }

    /**
     * Delete a voice profile by ID.
     */
    @Transactional
    public boolean deleteProfile(Long id) {
        if (!voiceProfileRepository.existsById(id)) {
            return false;
        }
        voiceProfileRepository.deleteById(id);
        log.info("Deleted voice profile: {}", id);
        return true;
    }

    /**
     * Get a voice profile by ID.
     */
    public Optional<VoiceProfile> getProfileById(Long id) {
        return voiceProfileRepository.findById(id);
    }

    /**
     * List all voice profiles.
     */
    public List<VoiceProfile> getAllProfiles() {
        return voiceProfileRepository.findAllByOrderByCreatedAtDesc();
    }

    /**
     * Get the voice profile assigned to a character.
     * Returns empty if the character has no profile or the profile doesn't exist.
     */
    public Optional<VoiceProfile> getProfileByCharacterId(Long characterId) {
        Optional<AiCharacter> character = aiCharacterRepository.findById(characterId);
        if (character.isEmpty()) {
            return Optional.empty();
        }
        Long profileId = character.get().getVoiceProfileId();
        if (profileId == null) {
            return Optional.empty();
        }
        return voiceProfileRepository.findById(profileId);
    }

    /**
     * Get the default voice profile (named "Default").
     * Falls back to the first available profile if "Default" doesn't exist.
     */
    public Optional<VoiceProfile> getDefaultProfile() {
        Optional<VoiceProfile> defaultProfile = voiceProfileRepository.findByName("Default");
        if (defaultProfile.isPresent()) {
            return defaultProfile;
        }
        List<VoiceProfile> all = voiceProfileRepository.findAllByOrderByCreatedAtDesc();
        if (!all.isEmpty()) {
            return Optional.of(all.get(0));
        }
        return Optional.empty();
    }

    /**
     * Assign a voice profile to an AI character.
     */
    @Transactional
    public boolean assignProfileToCharacter(Long characterId, Long profileId) {
        Optional<AiCharacter> character = aiCharacterRepository.findById(characterId);
        if (character.isEmpty()) {
            return false;
        }
        if (!voiceProfileRepository.existsById(profileId)) {
            return false;
        }
        character.get().setVoiceProfileId(profileId);
        aiCharacterRepository.save(character.get());
        log.info("Assigned voice profile {} to character {}", profileId, characterId);
        return true;
    }

    /**
     * Remove voice profile assignment from an AI character.
     */
    @Transactional
    public boolean removeProfileFromCharacter(Long characterId) {
        Optional<AiCharacter> character = aiCharacterRepository.findById(characterId);
        if (character.isEmpty()) {
            return false;
        }
        character.get().setVoiceProfileId(null);
        aiCharacterRepository.save(character.get());
        log.info("Removed voice profile from character {}", characterId);
        return true;
    }

    // ---- Default Profile Initialization ----

    /**
     * Initialize default voice profiles if none exist.
     * Called on application startup.
     */
    @Transactional
    public void initializeDefaultProfiles() {
        if (voiceProfileRepository.count() > 0) {
            log.info("Voice profiles already exist, skipping initialization");
            return;
        }

        log.info("Creating default voice profiles");

        // Default profile
        VoiceProfile defaultProfile = new VoiceProfile();
        defaultProfile.setName("Default");
        defaultProfile.setDescription("Standard neutral voice for AI characters");
        defaultProfile.setTone("neutral");
        defaultProfile.setLanguage("en-US");
        voiceProfileRepository.save(defaultProfile);

        // Default Male
        VoiceProfile maleProfile = new VoiceProfile();
        maleProfile.setName("Default Male");
        maleProfile.setDescription("Male voice with neutral tone");
        maleProfile.setTone("warm");
        maleProfile.setLanguage("en-US");
        voiceProfileRepository.save(maleProfile);

        // Default Female
        VoiceProfile femaleProfile = new VoiceProfile();
        femaleProfile.setName("Default Female");
        femaleProfile.setDescription("Female voice with neutral tone");
        femaleProfile.setTone("bright");
        femaleProfile.setLanguage("en-US");
        voiceProfileRepository.save(femaleProfile);

        // Robot
        VoiceProfile robotProfile = new VoiceProfile();
        robotProfile.setName("Robot");
        robotProfile.setDescription("Mechanical voice with deep tone");
        robotProfile.setPitch(0.7);
        robotProfile.setSpeed(0.9);
        robotProfile.setTone("mechanical");
        robotProfile.setLanguage("en-US");
        voiceProfileRepository.save(robotProfile);

        // Monster
        VoiceProfile monsterProfile = new VoiceProfile();
        monsterProfile.setName("Monster");
        monsterProfile.setDescription("Growling voice with low pitch");
        monsterProfile.setPitch(0.5);
        monsterProfile.setSpeed(0.8);
        monsterProfile.setTone("deep");
        monsterProfile.setLanguage("en-US");
        voiceProfileRepository.save(monsterProfile);

        // Elder
        VoiceProfile elderProfile = new VoiceProfile();
        elderProfile.setName("Elder");
        elderProfile.setDescription("Wise voice with slow speed");
        elderProfile.setSpeed(0.7);
        elderProfile.setTone("wise");
        elderProfile.setLanguage("en-US");
        voiceProfileRepository.save(elderProfile);

        log.info("Created 6 default voice profiles");
    }
}
