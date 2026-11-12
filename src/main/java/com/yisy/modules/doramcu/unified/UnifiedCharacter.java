package com.yisy.modules.doramcu.unified;

import java.util.List;
import java.util.Map;

/**
 * Unified character interface that normalizes all game-specific character models
 * into a common representation.
 * <p>
 * This interface defines the minimum set of attributes shared across all game
 * systems in the DoraMCU universe. Each game module provides a {@link CharacterMapper}
 * adapter that converts between game-specific characters and this unified representation.
 * </p>
 * <p>
 * The 7-core stats are:
 * <ul>
 *   <li>STR — Strength (physical power)</li>
 *   <li>DEX — Dexterity (agility and reflexes)</li>
 *   <li>CON — Constitution (endurance and health)</li>
 *   <li>INT — Intelligence (knowledge and reasoning)</li>
 *   <li>WIS — Wisdom (intuition and perception)</li>
 *   <li>CHA — Charisma (social influence and presence)</li>
 *   <li>TECH — Technical aptitude (engineering and science)</li>
 * </ul>
 * </p>
 * <p>
 * Ability score ranges vary by system:
 * <ul>
 *   <li>Traveller: 2-12 (2D6)</li>
 *   <li>Earth2026/D&D 5e: 1-20</li>
 *   <li>Captain's Log: 0-5 (ranks)</li>
 * </ul>
 * The unified representation uses the raw values from each system; the {@link CharacterMapper}
 * handles normalization when converting between systems.
 * </p>
 */
public interface UnifiedCharacter {

    /**
     * Returns the unique identifier for this character.
     *
     * @return the character ID
     */
    Long getId();

    /**
     * Returns the character's name.
     *
     * @return the name
     */
    String getName();

    /**
     * Returns the character's age in years.
     *
     * @return the age
     */
    int getAge();

    /**
     * Returns the character's race or species.
     *
     * @return the race
     */
    String getRace();

    /**
     * Returns the 7-core ability scores.
     * <p>
     * The map keys are the ability names: STR, DEX, CON, INT, WIS, CHA, TECH.
     * Values are the raw scores from the source game system.
     * </p>
     *
     * @return the ability scores
     */
    Map<String, Integer> getAbilities();

    /**
     * Returns the value of a specific ability score.
     *
     * @param ability the ability name (STR, DEX, CON, INT, WIS, CHA, TECH)
     * @return the score, or 0 if not applicable
     */
    default int getAbility(String ability) {
        return getAbilities().getOrDefault(ability, 0);
    }

    /**
     * Returns the character's current hit points.
     *
     * @return the current hit points
     */
    int getHitPoints();

    /**
     * Returns the character's maximum hit points.
     *
     * @return the maximum hit points
     */
    int getMaxHitPoints();

    /**
     * Returns the character's current experience points.
     *
     * @return the experience points
     */
    int getExperiencePoints();

    /**
     * Returns the character's current level.
     *
     * @return the level
     */
    int getLevel();

    /**
     * Returns the character's skills.
     * <p>
     * Each skill is represented as a name-value pair where the value is the
     * skill level or rating.
     * </p>
     *
     * @return the list of skills
     */
    List<SkillEntry> getSkills();

    /**
     * Returns the character's equipment as a JSON string or plain text description.
     *
     * @return the equipment
     */
    String getEquipment();

    /**
     * Returns the character's current status (e.g., HEALTHY, INJURED, DEAD).
     *
     * @return the status
     */
    String getStatus();

    /**
     * Returns whether the character is alive.
     *
     * @return true if alive
     */
    boolean isAlive();

    /**
     * Returns the game system this character belongs to.
     *
     * @return the game type
     */
    GameType getGameType();

    /**
     * Returns additional game-specific metadata as key-value pairs.
     * <p>
     * This allows game-specific attributes to be stored without polluting
     * the unified interface. For example, D&D 5e characters might store
     * armor class, spell slots, and death saves here.
     * </p>
     *
     * @return the metadata
     */
    Map<String, Object> getMetadata();

    /**
     * A simple skill entry with a name and level.
     */
    interface SkillEntry {
        /**
         * Returns the skill name.
         */
        String getName();

        /**
         * Returns the skill level or rating.
         */
        int getLevel();
    }
}
