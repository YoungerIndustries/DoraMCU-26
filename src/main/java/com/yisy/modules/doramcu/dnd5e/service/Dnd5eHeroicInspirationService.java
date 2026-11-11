package com.yisy.modules.doramcu.dnd5e.service;

import com.yisy.modules.doramcu.dnd5e.dto.HeroicInspirationRerollResponse;
import com.yisy.modules.doramcu.dnd5e.model.Dnd5eCharacter;
import com.yisy.modules.doramcu.dnd5e.model.Dnd5eHeroicInspiration;
import com.yisy.modules.doramcu.dnd5e.repository.Dnd5eCharacterRepository;
import com.yisy.modules.doramcu.dnd5e.repository.Dnd5eHeroicInspirationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

/**
 * Service for managing D&D 5e Heroic Inspiration.
 * <p>
 * Heroic Inspiration (2024 PHB) replaces the old Inspiration mechanic.
 * It lets a character reroll any die after rolling it — including damage,
 * HP on level-up, wild magic surges, and more. The character keeps the
 * higher of the two rolls.
 * </p>
 * <p>
 * Humans get one Heroic Inspiration after a long rest. Certain features
 * (e.g., Musician origin feat) award it after any rest.
 * </p>
 */
@Service
public class Dnd5eHeroicInspirationService {

    private static final Logger log = LoggerFactory.getLogger(Dnd5eHeroicInspirationService.class);
    private static final Random random = new Random();

    private final Dnd5eHeroicInspirationRepository inspirationRepository;
    private final Dnd5eCharacterRepository characterRepository;

    public Dnd5eHeroicInspirationService(Dnd5eHeroicInspirationRepository inspirationRepository,
                                         Dnd5eCharacterRepository characterRepository) {
        this.inspirationRepository = inspirationRepository;
        this.characterRepository = characterRepository;
    }

    // ==================== Inspiration Management ====================

    /**
     * Get or create a Heroic Inspiration record for a character.
     * Auto-creates a record for Human characters.
     */
    @Transactional
    public Dnd5eHeroicInspiration getOrCreateInspiration(Long characterId) {
        Dnd5eCharacter character = characterRepository.findById(characterId)
                .orElseThrow(() -> new IllegalArgumentException("Character not found: " + characterId));

        return inspirationRepository.findByCharacterId(characterId)
                .orElseGet(() -> {
                    // Auto-create for Human characters
                    if ("Human".equalsIgnoreCase(character.getRace())) {
                        return awardInspiration(characterId, "Human", 1);
                    }
                    // Create an empty record for non-Human characters
                    Dnd5eHeroicInspiration inspiration = new Dnd5eHeroicInspiration();
                    inspiration.setCharacterId(characterId);
                    inspiration.setCharacterName(character.getName());
                    inspiration.setPointsAvailable(0);
                    inspiration.setMaxPoints(0);
                    inspiration.setSource("None");
                    return inspirationRepository.save(inspiration);
                });
    }

    /**
     * Award Heroic Inspiration to a character.
     * Typically called after a long rest for Humans, or after any rest for Musician feat.
     */
    @Transactional
    public Dnd5eHeroicInspiration awardInspiration(Long characterId, String source, int points) {
        Dnd5eCharacter character = characterRepository.findById(characterId)
                .orElseThrow(() -> new IllegalArgumentException("Character not found: " + characterId));

        Dnd5eHeroicInspiration inspiration = inspirationRepository.findByCharacterId(characterId)
                .orElseGet(() -> {
                    Dnd5eHeroicInspiration newInspiration = new Dnd5eHeroicInspiration();
                    newInspiration.setCharacterId(characterId);
                    newInspiration.setCharacterName(character.getName());
                    newInspiration.setSource("None");
                    return newInspiration;
                });

        // Update max points if source provides more
        if (points > inspiration.getMaxPoints()) {
            inspiration.setMaxPoints(points);
        }

        // Add points (cap at max)
        inspiration.setPointsAvailable(Math.min(inspiration.getMaxPoints(), inspiration.getPointsAvailable() + points));
        inspiration.setSource(source);
        inspiration.setLastAwardedAt(LocalDateTime.now());
        inspiration.setUsed(false);

        Dnd5eHeroicInspiration saved = inspirationRepository.save(inspiration);
        log.info("Awarded {} Heroic Inspiration point(s) to '{}' (source: {})", points, character.getName(), source);
        return saved;
    }

    /**
     * Consume Heroic Inspiration to reroll a die.
     * The character keeps the higher of the original and rerolled values.
     */
    @Transactional
    public HeroicInspirationRerollResponse rerollWithInspiration(Long characterId, String description,
                                                                  int originalRoll, int diceSides) {
        Dnd5eCharacter character = characterRepository.findById(characterId)
                .orElseThrow(() -> new IllegalArgumentException("Character not found: " + characterId));

        Dnd5eHeroicInspiration inspiration = inspirationRepository.findByCharacterId(characterId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No Heroic Inspiration record for character: " + characterId));

        if (inspiration.getPointsAvailable() <= 0) {
            throw new IllegalStateException(
                    "No Heroic Inspiration points available for " + character.getName());
        }

        // Roll the new die
        int rerollValue = random.nextInt(diceSides) + 1;

        // Character keeps the higher value
        int finalValue = Math.max(originalRoll, rerollValue);
        boolean improved = rerollValue > originalRoll;

        // Consume the point
        inspiration.setPointsAvailable(inspiration.getPointsAvailable() - 1);
        inspiration.setUsed(true);
        inspiration.setLastRerollDescription(description);
        inspiration.setLastOriginalRoll(originalRoll);
        inspiration.setLastRerollValue(rerollValue);

        inspirationRepository.save(inspiration);

        // Build response
        HeroicInspirationRerollResponse response = new HeroicInspirationRerollResponse();
        response.setInspirationId(inspiration.getId());
        response.setCharacterId(characterId);
        response.setCharacterName(character.getName());
        response.setDescription(description);
        response.setOriginalRoll(originalRoll);
        response.setRerollValue(rerollValue);
        response.setFinalValue(finalValue);
        response.setImproved(improved);
        response.setPointsRemaining(inspiration.getPointsAvailable());
        response.setSource(inspiration.getSource());

        log.info("Heroic Inspiration reroll for '{}': {} -> {} (final: {}, improved: {})",
                character.getName(), originalRoll, rerollValue, finalValue, improved);
        return response;
    }

    /**
     * Check if a character has available Heroic Inspiration points.
     */
    @Transactional(readOnly = true)
    public boolean hasAvailableInspiration(Long characterId) {
        return inspirationRepository.findByCharacterId(characterId)
                .map(insp -> insp.getPointsAvailable() > 0)
                .orElse(false);
    }

    // ==================== Query Methods ====================

    /**
     * Get Heroic Inspiration record for a character.
     */
    @Transactional(readOnly = true)
    public Dnd5eHeroicInspiration getInspiration(Long characterId) {
        return inspirationRepository.findByCharacterId(characterId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No Heroic Inspiration record for character: " + characterId));
    }

    /**
     * List all Heroic Inspiration records.
     */
    @Transactional(readOnly = true)
    public List<Dnd5eHeroicInspiration> listAllInspiration() {
        return inspirationRepository.findAll();
    }

    /**
     * List all characters with available (unused) Heroic Inspiration.
     */
    @Transactional(readOnly = true)
    public List<Dnd5eHeroicInspiration> listAvailableInspiration() {
        return inspirationRepository.findByIsUsedFalse();
    }

    /**
     * List Heroic Inspiration records by source.
     */
    @Transactional(readOnly = true)
    public List<Dnd5eHeroicInspiration> listInspirationBySource(String source) {
        return inspirationRepository.findBySource(source);
    }

    /**
     * Reset Heroic Inspiration for a character (e.g., after a long rest).
     * Humans get 1 point after a long rest.
     */
    @Transactional
    public Dnd5eHeroicInspiration resetInspiration(Long characterId) {
        Dnd5eCharacter character = characterRepository.findById(characterId)
                .orElseThrow(() -> new IllegalArgumentException("Character not found: " + characterId));

        Dnd5eHeroicInspiration inspiration = inspirationRepository.findByCharacterId(characterId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No Heroic Inspiration record for character: " + characterId));

        // Reset based on race/features
        int points = 0;
        String source = "None";

        if ("Human".equalsIgnoreCase(character.getRace())) {
            points = 1;
            source = "Human";
        }

        // Check for Musician feat in equipment
        if (character.getEquipment() != null && character.getEquipment().contains("Musician")) {
            points = Math.max(points, 1);
            source = "Musician feat";
        }

        inspiration.setPointsAvailable(points);
        inspiration.setMaxPoints(Math.max(inspiration.getMaxPoints(), points));
        inspiration.setSource(source);
        inspiration.setLastAwardedAt(LocalDateTime.now());
        inspiration.setUsed(false);

        Dnd5eHeroicInspiration saved = inspirationRepository.save(inspiration);
        log.info("Reset Heroic Inspiration for '{}': {} points from {}", character.getName(), points, source);
        return saved;
    }

    /**
     * Manually set the number of Heroic Inspiration points for a character.
     * Useful for DMs adjusting points based on house rules.
     */
    @Transactional
    public Dnd5eHeroicInspiration setInspirationPoints(Long characterId, int points, String source) {
        Dnd5eCharacter character = characterRepository.findById(characterId)
                .orElseThrow(() -> new IllegalArgumentException("Character not found: " + characterId));

        Dnd5eHeroicInspiration inspiration = inspirationRepository.findByCharacterId(characterId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No Heroic Inspiration record for character: " + characterId));

        inspiration.setPointsAvailable(Math.max(0, Math.min(points, inspiration.getMaxPoints())));
        if (source != null && !source.isEmpty()) {
            inspiration.setSource(source);
        }
        inspiration.setUsed(false);

        return inspirationRepository.save(inspiration);
    }

    /**
     * Set the maximum Heroic Inspiration points a character can hold.
     */
    @Transactional
    public Dnd5eHeroicInspiration setMaxPoints(Long characterId, int maxPoints) {
        Dnd5eHeroicInspiration inspiration = inspirationRepository.findByCharacterId(characterId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No Heroic Inspiration record for character: " + characterId));

        int clampedMax = Math.max(0, maxPoints);
        inspiration.setMaxPoints(clampedMax);
        inspiration.setPointsAvailable(Math.min(inspiration.getPointsAvailable(), clampedMax));

        return inspirationRepository.save(inspiration);
    }
}
