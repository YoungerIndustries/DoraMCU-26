package com.yisy.modules.doramcu.techlevel;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Adapter interface for mapping generic tech-level modules to game-specific lore.
 * <p>
 * Each game module (D&D 5e, Traveller, Star Trek, Babylon 5) implements this interface
 * to provide lore-appropriate names, stats, and descriptions for building modules
 * at each tech level. This allows the same generic module database to produce
 * universe-specific results.
 * </p>
 * <p>
 * For example, a TL2 SETTLEMENT might be "Stone Castle" in D&D, "Feudal Hold" in Traveller,
 * or not available at all in Star Trek (which starts at TL6+).
 * </p>
 */
public interface TechLevelAdapter {

    /**
     * Returns the game-specific lore name for a module.
     *
     * @param techLevel   the technology level
     * @param category    the module category
     * @param genericType the generic type identifier
     * @return the lore-specific name, or null if not mapped
     */
    String getLoreName(TechLevel techLevel, ModuleCategory category, String genericType);

    /**
     * Returns game-specific stats for a module.
     * <p>
     * The returned map contains key-value pairs that modify the base module stats
     * for this specific game universe. For example, D&D might add hit points,
     * while Traveller might add cargo capacity modifiers.
     * </p>
     *
     * @param techLevel   the technology level
     * @param category    the module category
     * @param genericType the generic type identifier
     * @return map of stat name to stat value, or empty map if not mapped
     */
    Map<String, Object> getLoreStats(TechLevel techLevel, ModuleCategory category, String genericType);

    /**
     * Returns the maximum tech level this game supports.
     * <p>
     * For example, D&D 5e returns TL4 (Renaissance), while Traveller returns TL12.
     * Requests for tech levels above this value should return empty results.
     * </p>
     *
     * @return the maximum supported tech level
     */
    TechLevel getMaxTechLevel();

    /**
     * Returns the minimum tech level this game supports.
     * <p>
     * For example, Star Trek 4X returns TL6 (Information Age), while D&D returns TL0.
     * </p>
     *
     * @return the minimum supported tech level
     */
    TechLevel getMinTechLevel();

    /**
     * Returns all available module mappings for a given tech level.
     * <p>
     * This method returns the lore names for all modules at the given tech level
     * that are supported by this game. If the tech level is outside the game's
     * range, returns an empty list.
     * </p>
     *
     * @param techLevel the technology level
     * @return list of available module generic types for this tech level, or empty list
     */
    List<String> getAvailableModules(TechLevel techLevel);

    /**
     * Returns all available module mappings for a given tech level and category.
     *
     * @param techLevel the technology level
     * @param category  the module category
     * @return list of available module generic types, or empty list
     */
    default List<String> getAvailableModules(TechLevel techLevel, ModuleCategory category) {
        return Collections.emptyList();
    }

    /**
     * Checks if a given tech level is supported by this adapter.
     *
     * @param techLevel the technology level to check
     * @return true if the tech level is within this game's range
     */
    default boolean supportsTechLevel(TechLevel techLevel) {
        if (techLevel == null) return false;
        TechLevel min = getMinTechLevel();
        TechLevel max = getMaxTechLevel();
        return techLevel.getLevel() >= min.getLevel() && techLevel.getLevel() <= max.getLevel();
    }
}
