package com.yisy.modules.doramcu.dnd5e.service;

import com.yisy.modules.doramcu.dnd5e.model.Dnd5eCharacter;
import com.yisy.modules.doramcu.dnd5e.model.Dnd5eExhaustion;
import com.yisy.modules.doramcu.dnd5e.repository.Dnd5eCharacterRepository;
import com.yisy.modules.doramcu.dnd5e.repository.Dnd5eExhaustionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service for managing D&D 5e Revised Exhaustion (2024 PHB).
 * <p>
 * In D&D 2024 PHB, exhaustion is tracked as a level from 0 to 6.
 * Each level imposes a penalty of 2 × exhaustion level on d20 tests,
 * and reduces speed by 5 × exhaustion level feet.
 * </p>
 * <p>
 * Exhaustion is gained from harsh environments, starvation, dehydration,
 * and other severe stresses. It is removed by long rests.
 * </p>
 * <p>
 * Formula:
 * - d20 test penalty: -2 × exhaustion level
 * - Speed reduction: 5 × exhaustion level feet (minimum 0)
 * - At level 6, speed is reduced by 30 feet (minimum 0)
 * </p>
 */
@Service
public class Dnd5eExhaustionService {

    private static final Logger log = LoggerFactory.getLogger(Dnd5eExhaustionService.class);

    /**
     * Maximum exhaustion level.
     */
    static final int MAX_EXHAUSTION_LEVEL = 6;

    /**
     * Minimum exhaustion level.
     */
    static final int MIN_EXHAUSTION_LEVEL = 0;

    /**
     * Penalty multiplier per exhaustion level for d20 tests.
     */
    static final int D20_PENALTY_MULTIPLIER = 2;

    /**
     * Speed reduction in feet per exhaustion level.
     */
    static final int SPEED_REDUCTION_PER_LEVEL = 5;

    private final Dnd5eExhaustionRepository exhaustionRepository;
    private final Dnd5eCharacterRepository characterRepository;

    public Dnd5eExhaustionService(Dnd5eExhaustionRepository exhaustionRepository,
                                  Dnd5eCharacterRepository characterRepository) {
        this.exhaustionRepository = exhaustionRepository;
        this.characterRepository = characterRepository;
    }

    // ==================== Exhaustion Level Management ====================

    /**
     * Get the current exhaustion level for a character.
     */
    @Transactional(readOnly = true)
    public int getExhaustionLevel(Long characterId) {
        Dnd5eCharacter character = characterRepository.findById(characterId)
                .orElseThrow(() -> new IllegalArgumentException("Character not found: " + characterId));
        return character.getExhaustionLevel();
    }

    /**
     * Gain exhaustion levels for a character.
     * Each level gained is logged as an exhaustion event.
     *
     * @param characterId the character's ID
     * @param levels      number of levels to gain (clamped to 1-6)
     * @param source      the source of exhaustion (e.g., "Starvation", "Extreme Cold")
     * @return the updated character
     */
    @Transactional
    public Dnd5eCharacter gainExhaustion(Long characterId, int levels, String source) {
        return modifyExhaustion(characterId, levels, source, null);
    }

    /**
     * Gain exhaustion levels for a character with a description.
     *
     * @param characterId the character's ID
     * @param levels      number of levels to gain (clamped to 1-6)
     * @param source      the source of exhaustion
     * @param description optional description of the event
     * @return the updated character
     */
    @Transactional
    public Dnd5eCharacter gainExhaustion(Long characterId, int levels, String source, String description) {
        return modifyExhaustion(characterId, levels, source, description);
    }

    /**
     * Remove exhaustion levels from a character.
     * Typically called after a long rest.
     *
     * @param characterId the character's ID
     * @param levels      number of levels to remove (clamped to 1-6)
     * @param source      the source of removal (e.g., "Long Rest")
     * @return the updated character
     */
    @Transactional
    public Dnd5eCharacter removeExhaustion(Long characterId, int levels, String source) {
        return modifyExhaustion(characterId, -levels, source, null);
    }

    /**
     * Remove exhaustion levels from a character with a description.
     *
     * @param characterId the character's ID
     * @param levels      number of levels to remove (clamped to 1-6)
     * @param source      the source of removal
     * @param description optional description of the event
     * @return the updated character
     */
    @Transactional
    public Dnd5eCharacter removeExhaustion(Long characterId, int levels, String source, String description) {
        return modifyExhaustion(characterId, -levels, source, description);
    }

    /**
     * Set the exhaustion level directly (for DM adjustments).
     *
     * @param characterId the character's ID
     * @param level       the new exhaustion level (clamped to 0-6)
     * @param source      the source of the adjustment
     * @return the updated character
     */
    @Transactional
    public Dnd5eCharacter setExhaustionLevel(Long characterId, int level, String source) {
        Dnd5eCharacter character = characterRepository.findById(characterId)
                .orElseThrow(() -> new IllegalArgumentException("Character not found: " + characterId));

        int clampedLevel = Math.max(MIN_EXHAUSTION_LEVEL, Math.min(MAX_EXHAUSTION_LEVEL, level));
        int currentLevel = character.getExhaustionLevel();
        int change = clampedLevel - currentLevel;

        if (change != 0) {
            character.setExhaustionLevel(clampedLevel);
            character = characterRepository.save(character);

            // Log the event
            Dnd5eExhaustion event = new Dnd5eExhaustion();
            event.setCharacterId(characterId);
            event.setCharacterName(character.getName());
            event.setLevelChange(change);
            event.setResultingLevel(clampedLevel);
            event.setSource(source);
            event.setDescription("DM adjustment: set exhaustion to level " + clampedLevel);
            exhaustionRepository.save(event);

            log.info("Set exhaustion for '{}' to level {} (change: {}, source: {})",
                    character.getName(), clampedLevel, change, source);
        }

        return character;
    }

    /**
     * Clear all exhaustion for a character (set to 0).
     * Typically called after a long rest.
     *
     * @param characterId the character's ID
     * @return the updated character
     */
    @Transactional
    public Dnd5eCharacter clearExhaustion(Long characterId) {
        return removeExhaustion(characterId, Integer.MAX_VALUE, "Long Rest");
    }

    // ==================== Exhaustion Effects ====================

    /**
     * Calculate the d20 test penalty for a given exhaustion level.
     * Formula: -2 × exhaustion level
     *
     * @param exhaustionLevel the exhaustion level (0-6)
     * @return the penalty (0 to -12)
     */
    public int getD20TestPenalty(int exhaustionLevel) {
        return -D20_PENALTY_MULTIPLIER * exhaustionLevel;
    }

    /**
     * Calculate the speed reduction for a given exhaustion level.
     * Formula: 5 × exhaustion level feet
     *
     * @param exhaustionLevel the exhaustion level (0-6)
     * @return the speed reduction in feet (0 to 30)
     */
    public int getSpeedReduction(int exhaustionLevel) {
        return SPEED_REDUCTION_PER_LEVEL * exhaustionLevel;
    }

    /**
     * Calculate the effective speed for a character given their base speed and exhaustion level.
     * The speed cannot go below 0.
     *
     * @param baseSpeed       the character's base speed in feet
     * @param exhaustionLevel the exhaustion level (0-6)
     * @return the effective speed in feet (minimum 0)
     */
    public int getEffectiveSpeed(int baseSpeed, int exhaustionLevel) {
        int reduction = getSpeedReduction(exhaustionLevel);
        return Math.max(0, baseSpeed - reduction);
    }

    /**
     * Get the d20 test penalty for a character based on their current exhaustion level.
     *
     * @param characterId the character's ID
     * @return the penalty (0 to -12)
     */
    @Transactional(readOnly = true)
    public int getCharacterD20Penalty(Long characterId) {
        int level = getExhaustionLevel(characterId);
        return getD20TestPenalty(level);
    }

    /**
     * Get the effective speed for a character based on their base speed and exhaustion level.
     *
     * @param characterId the character's ID
     * @return the effective speed in feet (minimum 0)
     */
    @Transactional(readOnly = true)
    public int getCharacterEffectiveSpeed(Long characterId) {
        Dnd5eCharacter character = characterRepository.findById(characterId)
                .orElseThrow(() -> new IllegalArgumentException("Character not found: " + characterId));
        return getEffectiveSpeed(character.getSpeed(), character.getExhaustionLevel());
    }

    // ==================== Exhaustion History ====================

    /**
     * Get the exhaustion history for a character.
     *
     * @param characterId the character's ID
     * @return list of exhaustion events, ordered by creation time descending
     */
    @Transactional(readOnly = true)
    public List<Dnd5eExhaustion> getExhaustionHistory(Long characterId) {
        return exhaustionRepository.findExhaustionHistory(characterId);
    }

    /**
     * Get all exhaustion events for a character.
     *
     * @param characterId the character's ID
     * @return list of exhaustion events, ordered by creation time descending
     */
    @Transactional(readOnly = true)
    public List<Dnd5eExhaustion> listExhaustionEvents(Long characterId) {
        return exhaustionRepository.findByCharacterIdOrderByCreatedAtDesc(characterId);
    }

    /**
     * Get all exhaustion events across all characters.
     *
     * @return list of all exhaustion events
     */
    @Transactional(readOnly = true)
    public List<Dnd5eExhaustion> listAllExhaustionEvents() {
        return exhaustionRepository.findAll();
    }

    /**
     * Get exhaustion events by source.
     *
     * @param source the source to filter by
     * @return list of exhaustion events matching the source
     */
    @Transactional(readOnly = true)
    public List<Dnd5eExhaustion> listExhaustionBySource(String source) {
        return exhaustionRepository.findBySource(source);
    }

    /**
     * Get all characters who currently have exhaustion (level > 0).
     *
     * @return list of exhaustion events where resulting level > 0
     */
    @Transactional(readOnly = true)
    public List<Dnd5eExhaustion> listCharactersWithExhaustion() {
        return exhaustionRepository.findCharactersWithExhaustion();
    }

    // ==================== Common Exhaustion Sources ====================

    /**
     * Apply exhaustion from starvation (1 day without food).
     * Gains 1 level of exhaustion.
     */
    @Transactional
    public Dnd5eCharacter applyStarvation(Long characterId) {
        return gainExhaustion(characterId, 1, "Starvation",
                "Character has gone a day without food");
    }

    /**
     * Apply exhaustion from dehydration (1 day without water).
     * Gains 1 level of exhaustion.
     */
    @Transactional
    public Dnd5eCharacter applyDehydration(Long characterId) {
        return gainExhaustion(characterId, 1, "Dehydration",
                "Character has gone a day without water");
    }

    /**
     * Apply exhaustion from extreme temperature.
     * Gains 1 level of exhaustion.
     *
     * @param characterId the character's ID
     * @param isHot       true for extreme heat, false for extreme cold
     */
    @Transactional
    public Dnd5eCharacter applyExtremeTemperature(Long characterId, boolean isHot) {
        String source = isHot ? "Extreme Heat" : "Extreme Cold";
        return gainExhaustion(characterId, 1, source,
                "Character exposed to " + source.toLowerCase());
    }

    /**
     * Apply exhaustion from a long day without rest.
     * Gains 1 level of exhaustion.
     */
    @Transactional
    public Dnd5eCharacter applySleepDeprivation(Long characterId) {
        return gainExhaustion(characterId, 1, "Sleep Deprivation",
                "Character has gone without proper rest");
    }

    /**
     * Remove exhaustion after a long rest.
     * Removes 1 level of exhaustion.
     */
    @Transactional
    public Dnd5eCharacter removeExhaustionAfterLongRest(Long characterId) {
        return removeExhaustion(characterId, 1, "Long Rest",
                "Character completed a long rest");
    }

    // ==================== Internal Methods ====================

    /**
     * Modify a character's exhaustion level.
     * Positive values gain exhaustion; negative values remove it.
     */
    private Dnd5eCharacter modifyExhaustion(Long characterId, int levels, String source, String description) {
        if (levels == 0) {
            return characterRepository.findById(characterId)
                    .orElseThrow(() -> new IllegalArgumentException("Character not found: " + characterId));
        }

        Dnd5eCharacter character = characterRepository.findById(characterId)
                .orElseThrow(() -> new IllegalArgumentException("Character not found: " + characterId));

        int currentLevel = character.getExhaustionLevel();
        int newLevel;

        if (levels > 0) {
            // Gaining exhaustion
            newLevel = Math.min(MAX_EXHAUSTION_LEVEL, currentLevel + levels);
        } else {
            // Removing exhaustion
            newLevel = Math.max(MIN_EXHAUSTION_LEVEL, currentLevel + levels);
        }

        int actualChange = newLevel - currentLevel;

        if (actualChange != 0) {
            character.setExhaustionLevel(newLevel);
            character = characterRepository.save(character);

            // Log the event
            Dnd5eExhaustion event = new Dnd5eExhaustion();
            event.setCharacterId(characterId);
            event.setCharacterName(character.getName());
            event.setLevelChange(actualChange);
            event.setResultingLevel(newLevel);
            event.setSource(source);
            event.setDescription(description != null ? description : source);
            exhaustionRepository.save(event);

            log.info("{} exhaustion for '{}' by {} level(s) to level {} (source: {})",
                    actualChange > 0 ? "Gained" : "Removed",
                    character.getName(), Math.abs(actualChange), newLevel, source);
        }

        return character;
    }
}
