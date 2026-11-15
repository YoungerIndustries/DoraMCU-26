package com.yisy.modules.doramcu.hex.planet.military;

import java.util.List;
import java.util.Map;

/**
 * Interface that each game module implements to provide battle-capable units
 * to the {@link UniversalBattleManager}.
 * <p>
 * A module adapter provides:
 * <ul>
 *   <li>Unit creation — creates UniversalBattleUnit instances for the module</li>
 *   <li>Stat calculation — computes attack/defense/morale based on module-specific data</li>
 *   <li>3D model paths — provides paths to 3D models for FPS rendering</li>
 *   <li>Available unit types — lists what unit types the module can provide</li>
 * </ul>
 * </p>
 * <p>
 * New modules (Warhammer, Warcraft, EverQuest) only need to implement this
 * interface to join universal battles. No other changes are required.
 * </p>
 */
public interface ModuleBattleAdapter {

    /**
     * Returns the module identifier (e.g., "Traveller", "StarWars", "Dnd5e").
     */
    String getModuleId();

    /**
     * Returns the display name for this module (e.g., "Traveller RPG", "Star Wars").
     */
    String getModuleName();

    /**
     * Returns a map of all available unit types for this module.
     * Key is the module-specific unit type string, value is the display name.
     */
    Map<String, String> getAvailableUnitTypes();

    /**
     * Creates a battle unit for the given module-specific unit type.
     *
     * @param unitId unique identifier for this unit instance
     * @param unitName display name for this unit instance
     * @param moduleUnitType module-specific unit type identifier
     * @param faction faction this unit belongs to
     * @param techLevel current tech level for stat scaling
     * @return a configured UniversalBattleUnit
     */
    UniversalBattleUnit createUnit(String unitId, String unitName, String moduleUnitType, String faction, int techLevel);

    /**
     * Creates multiple battle units for a batch request.
     *
     * @param unitIds list of unique identifiers
     * @param moduleUnitType module-specific unit type for all units
     * @param faction faction these units belong to
     * @param techLevel current tech level for stat scaling
     * @return list of configured UniversalBattleUnits
     */
    List<UniversalBattleUnit> createUnits(List<String> unitIds, String moduleUnitType, String faction, int techLevel);

    /**
     * Returns the 3D model path for a given module unit type.
     *
     * @param moduleUnitType module-specific unit type
     * @return path to the 3D model file, or null if not available
     */
    String getModelPath(String moduleUnitType);

    /**
     * Returns the texture path for a given module unit type.
     *
     * @param moduleUnitType module-specific unit type
     * @return path to the texture file, or null if not available
     */
    String getTexturePath(String moduleUnitType);

    /**
     * Calculates combat stats for a unit based on module-specific factors.
     *
     * @param unit the unit to calculate stats for
     * @param terrainBonus bonus from terrain
     * @param commanderBonus bonus from assigned commander
     * @return map of stat adjustments (attack, defense, morale)
     */
    Map<String, Integer> calculateCombatStats(UniversalBattleUnit unit, int terrainBonus, double commanderBonus);

    /**
     * Returns the default faction color for 3D rendering.
     *
     * @param faction faction name
     * @return hex color code (e.g., "#FF0000")
     */
    String getFactionColor(String faction);
}
