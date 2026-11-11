package com.yisy.modules.doramcu.fourx;

import java.util.List;
import java.util.Map;

/**
 * Shared interface for the Character Creation 4X pillar.
 * <p>
 * Implementing services handle character creation with lifepath, attributes, skills,
 * and careers. Each game module provides its own implementation with theme-specific
 * races, careers, and skills.
 * </p>
 */
public interface FourxCharacterService {

    /**
     * Create a new character with the given name and race.
     *
     * @param name  the character name
     * @param race  the character race/species
     * @return the created character as a map of attributes
     */
    Map<String, Object> createCharacter(String name, String race);

    /**
     * Create a character with a random lifepath.
     *
     * @param name  the character name
     * @param race  the character race/species
     * @param career the career path
     * @return the created character with lifepath details
     */
    Map<String, Object> createCharacterWithLifepath(String name, String race, String career);

    /**
     * Get available races for character creation.
     *
     * @return list of race names
     */
    List<String> getAvailableRaces();

    /**
     * Get available careers for character creation.
     *
     * @return list of career names
     */
    List<String> getAvailableCareers();

    /**
     * Get available skills for a career.
     *
     * @param career the career name
     * @return list of skill names
     */
    List<String> getAvailableSkills(String career);

    /**
     * Get the attribute modifiers for a race.
     *
     * @param race the race name
     * @return map of attribute names to modifiers
     */
    Map<String, Integer> getRaceModifiers(String race);

    /**
     * Get the skills granted by a career.
     *
     * @param career the career name
     * @return list of skill names with proficiency levels
     */
    List<Map<String, Object>> getCareerSkills(String career);

    /**
     * Advance a character through a career term.
     *
     * @param character the character data
     * @param career    the career name
     * @return updated character with new skills and experience
     */
    Map<String, Object> advanceCareer(Map<String, Object> character, String career);

    /**
     * Get a list of all created characters.
     *
     * @return list of character maps
     */
    List<Map<String, Object>> getAllCharacters();
}
