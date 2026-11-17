package com.yisy.modules.doramcu.repository.misc;

import com.yisy.modules.doramcu.eve.model.VoiceProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VoiceProfileRepository extends JpaRepository<VoiceProfile, Long> {

    /**
     * Find voice profile by name.
     */
    Optional<VoiceProfile> findByName(String name);

    /**
     * Find voice profiles by TTS model.
     */
    List<VoiceProfile> findByTtsModel(String ttsModel);

    /**
     * Find all voice profiles ordered by creation date descending.
     */
    List<VoiceProfile> findAllByOrderByCreatedAtDesc();

    /**
     * Find voice profile by tone.
     */
    List<VoiceProfile> findByTone(String tone);

    /**
     * Check if a voice profile with the given name exists.
     */
    boolean existsByName(String name);
}
