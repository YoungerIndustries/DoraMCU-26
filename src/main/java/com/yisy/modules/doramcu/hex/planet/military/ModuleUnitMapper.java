package com.yisy.modules.doramcu.hex.planet.military;

import java.util.List;

/**
 * Interface for mapping module-specific unit types to {@link GenericUnitType}.
 * <p>
 * Each game module implements this interface to provide:
 * <ul>
 *   <li>A mapping from its unit types to generic unit types</li>
 *   <li>A list of available unit types for a given tech level</li>
 *   <li>A reverse mapping from generic to module-specific types</li>
 * </ul>
 * </p>
 * <p>
 * This allows the military production system to work with any module's units
 * without knowing the module-specific details.
 * </p>
 */
public interface ModuleUnitMapper {

    /**
     * Returns the module ID this mapper belongs to.
     *
     * @return the module ID (e.g., "earth2026", "starwars", "dnd5e")
     */
    String getModuleId();

    /**
     * Maps a module-specific unit type to a generic unit type.
     *
     * @param moduleUnitType the module-specific unit type identifier
     * @return the corresponding generic unit type, or null if not mapped
     */
    GenericUnitType mapToGeneric(String moduleUnitType);

    /**
     * Maps a generic unit type back to module-specific unit types.
     * <p>
     * Returns a list because one generic type may map to multiple module-specific types.
     * </p>
     *
     * @param genericType the generic unit type
     * @return list of module-specific unit type identifiers
     */
    List<String> mapFromGeneric(GenericUnitType genericType);

    /**
     * Returns all available unit types for this module at a given tech level.
     *
     * @param techLevel the tech level
     * @return list of module-specific unit type identifiers
     */
    List<String> getAvailableUnitTypes(int techLevel);

    /**
     * Returns the display name for a module-specific unit type.
     *
     * @param moduleUnitType the module-specific unit type identifier
     * @return the display name
     */
    String getUnitDisplayName(String moduleUnitType);
}
