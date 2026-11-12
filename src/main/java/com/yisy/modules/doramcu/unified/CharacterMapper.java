package com.yisy.modules.doramcu.unified;

/**
 * Adapter interface that converts between game-specific character models
 * and the {@link UnifiedCharacter} representation.
 * <p>
 * Each game module provides a concrete implementation that knows how to
 * map its character model to/from the unified 7-core stats (STR/DEX/CON/INT/WIS/CHA/TECH).
 * </p>
 * <p>
 * The mapper is responsible for:
 * <ul>
 *   <li>Converting game-specific ability scores to unified stats</li>
 *   <li>Converting unified stats back to game-specific ability scores</li>
 *   <li>Mapping skills, equipment, and metadata</li>
 *   <li>Handling system-specific normalization (e.g., Traveller 2-12 vs D&D 1-20)</li>
 * </ul>
 * </p>
 *
 * @param <T> the game-specific character type
 */
public interface CharacterMapper<T> {

    /**
     * Returns the game type this mapper handles.
     *
     * @return the game type
     */
    GameType getGameType();

    /**
     * Converts a game-specific character to the unified representation.
     *
     * @param gameCharacter the game-specific character
     * @return the unified character
     */
    UnifiedCharacter toUnified(T gameCharacter);

    /**
     * Converts a unified character back to the game-specific representation.
     * <p>
     * This method creates a new game-specific character from the unified data.
     * It does not modify the existing game character.
     * </p>
     *
     * @param unified the unified character
     * @return the game-specific character
     */
    T fromUnified(UnifiedCharacter unified);

    /**
     * Normalizes an ability score from the source system's range to the target system's range.
     * <p>
     * For example, converting a Traveller score (2-12) to a D&D score (1-20):
     * <pre>
     *   travellerScore 7 -> normalized to D&D score 12
     * </pre>
     * </p>
     *
     * @param sourceValue the value in the source system's range
     * @param sourceType  the source game type
     * @param targetType  the target game type
     * @return the normalized value in the target system's range
     */
    int normalizeAbilityScore(int sourceValue, GameType sourceType, GameType targetType);
}
