package com.yisy.modules.doramcu.shared.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Standardized category taxonomy for cross-system rules queries.
 * <p>
 * Different game modules use different naming conventions for rule categories:
 * <ul>
 *   <li>D&D 5e: {@code ABILITY_CHECKS}, {@code SAVING_THROWS}, {@code COMBAT}, {@code SPELLCASTING}</li>
 *   <li>B5: {@code DICE_MECHANICS}, {@code MELEE_COMBAT}, {@code FIREARMS_COMBAT}, {@code SHIP_COMBAT}</li>
 *   <li>Star Wars: {@code GROUND_COMBAT}, {@code SPACE_COMBAT}, {@code GALAXY_CONTROL}, {@code RPG_MECHANICS}</li>
 * </ul>
 * </p>
 * <p>
 * This enum provides a unified taxonomy so cross-system queries can find related
 * rules regardless of which module's naming convention was used. Each standard
 * category maps to the module-specific category strings used by each game system.
 * </p>
 * <p>
 * Usage: {@code RulesCategory.COMBAT.getModuleCategory(GameSystem.STAR_WARS)} returns
 * {@code "GROUND_COMBAT"}, while {@code RulesCategory.COMBAT.getModuleCategory(GameSystem.DND_5E)}
 * returns {@code "COMBAT"}.
 * </p>
 */
public enum RulesCategory {

    /** Core dice rolling mechanics, task resolution, success/failure determination. */
    DICE_MECHANICS("DICE", Map.of(
            "DND_5E", "ABILITY_CHECKS",
            "BABYLON_5", "DICE_MECHANICS",
            "STAR_WARS", "RPG_MECHANICS",
            "CAPTAINS_LOG", "DICE_MECHANICS",
            "TRAVELLER", "DICE_MECHANICS",
            "EARTH_2026", "DICE_MECHANICS",
            "MECH_WARRIOR", "DICE_MECHANICS",
            "THIRD_REICH", "DICE_MECHANICS"
    )),

    /** Melee combat: swords, fists, close-quarters fighting. */
    MELEE_COMBAT("COMBAT", Map.of(
            "DND_5E", "COMBAT",
            "BABYLON_5", "MELEE_COMBAT",
            "STAR_WARS", "GROUND_COMBAT",
            "CAPTAINS_LOG", "COMBAT",
            "TRAVELLER", "MELEE_COMBAT",
            "EARTH_2026", "COMBAT",
            "MECH_WARRIOR", "MELEE_COMBAT",
            "THIRD_REICH", "MELEE_COMBAT"
    )),

    /** Ranged combat: firearms, energy weapons, projectile attacks. */
    RANGED_COMBAT("COMBAT", Map.of(
            "DND_5E", "COMBAT",
            "BABYLON_5", "FIREARMS_COMBAT",
            "STAR_WARS", "GROUND_COMBAT",
            "CAPTAINS_LOG", "COMBAT",
            "TRAVELLER", "RANGED_COMBAT",
            "EARTH_2026", "COMBAT",
            "MECH_WARRIOR", "RANGED_COMBAT",
            "THIRD_REICH", "RANGED_COMBAT"
    )),

    /** Space combat: starship engagements, orbital battles. */
    SPACE_COMBAT("COMBAT", Map.of(
            "DND_5E", "COMBAT",
            "BABYLON_5", "SHIP_COMBAT",
            "STAR_WARS", "SPACE_COMBAT",
            "CAPTAINS_LOG", "SPACE_COMBAT",
            "TRAVELLER", "SHIP_COMBAT",
            "EARTH_2026", "SPACE_COMBAT",
            "MECH_WARRIOR", "SPACE_COMBAT",
            "THIRD_REICH", "SPACE_COMBAT"
    )),

    /** Vehicle and ground vehicle combat (tanks, walkers, AT-ATs). */
    VEHICLE_COMBAT("COMBAT", Map.of(
            "DND_5E", "COMBAT",
            "BABYLON_5", "VEHICLE_COMBAT",
            "STAR_WARS", "GROUND_COMBAT",
            "CAPTAINS_LOG", "COMBAT",
            "TRAVELLER", "VEHICLE_COMBAT",
            "EARTH_2026", "VEHICLE_COMBAT",
            "MECH_WARRIOR", "VEHICLE_COMBAT",
            "THIRD_REICH", "VEHICLE_COMBAT"
    )),

    /** Cover, concealment, terrain effects on combat. */
    COVER_AND_TERRAIN("COMBAT", Map.of(
            "DND_5E", "COMBAT",
            "BABYLON_5", "MELEE_COMBAT",
            "STAR_WARS", "GROUND_COMBAT",
            "CAPTAINS_LOG", "COMBAT",
            "TRAVELLER", "COMBAT",
            "EARTH_2026", "COMBAT",
            "MECH_WARRIOR", "COMBAT",
            "THIRD_REICH", "COMBAT"
    )),

    /** Spellcasting, magic, supernatural abilities. */
    SPELLCASTING("MAGIC", Map.of(
            "DND_5E", "SPELLCASTING",
            "STAR_WARS", "THE_FORCE",
            "CAPTAINS_LOG", "SPELLCASTING",
            "TRAVELLER", "PSIONICS"
    )),

    /** Force powers, light side and dark side abilities (Star Wars specific). */
    THE_FORCE("MAGIC", Map.of(
            "STAR_WARS", "THE_FORCE",
            "DND_5E", "SPELLCASTING"
    )),

    /** Saving throws, resistance checks, defense rolls. */
    SAVING_THROWS("DEFENSE", Map.of(
            "DND_5E", "SAVING_THROWS",
            "BABYLON_5", "DICE_MECHANICS",
            "STAR_WARS", "RPG_MECHANICS",
            "CAPTAINS_LOG", "SAVING_THROWS",
            "TRAVELLER", "SAVING_THROWS"
    )),

    /** Damage calculation, hit points, wound tracking. */
    DAMAGE("COMBAT", Map.of(
            "DND_5E", "COMBAT",
            "BABYLON_5", "DAMAGE",
            "STAR_WARS", "GROUND_COMBAT",
            "CAPTAINS_LOG", "DAMAGE",
            "TRAVELLER", "DAMAGE",
            "EARTH_2026", "DAMAGE",
            "MECH_WARRIOR", "DAMAGE",
            "THIRD_REICH", "DAMAGE"
    )),

    /** Encounter generation, random encounters, difficulty scaling. */
    ENCOUNTERS("ENCOUNTER", Map.of(
            "DND_5E", "ENCOUNTERS",
            "BABYLON_5", "ENCOUNTERS",
            "STAR_WARS", "SCENARIOS",
            "CAPTAINS_LOG", "ENCOUNTERS",
            "TRAVELLER", "ENCOUNTERS",
            "EARTH_2026", "ENCOUNTERS"
    )),

    /** Galaxy-scale strategy, territory control, resource management. */
    GALAXY_CONTROL("STRATEGY", Map.of(
            "STAR_WARS", "GALAXY_CONTROL",
            "BABYLON_5", "STRATEGY",
            "CAPTAINS_LOG", "STRATEGY",
            "TRAVELLER", "EMPIRE_BUILDING"
    )),

    /** Card game mechanics, deck building, card interactions. */
    CARD_GAME("CARD_GAME", Map.of(
            "STAR_WARS", "CARD_GAME",
            "CAPTAINS_LOG", "CARD_GAME"
    )),

    /** Ship design, starship stats, vessel customization. */
    SHIP_DESIGN("SHIP", Map.of(
            "DND_5E", "COMBAT",
            "BABYLON_5", "SHIP_DESIGN",
            "STAR_WARS", "SPACE_COMBAT",
            "CAPTAINS_LOG", "SHIP_DESIGN",
            "TRAVELLER", "SHIP_DESIGN"
    )),

    /** Faction information, military branches, leaders. */
    FACTIONS("FACTION", Map.of(
            "DND_5E", "FACTIONS",
            "BABYLON_5", "FACTIONS",
            "STAR_WARS", "FACTIONS",
            "CAPTAINS_LOG", "FACTIONS",
            "TRAVELLER", "FACTIONS",
            "EARTH_2026", "FACTIONS"
    )),

    /** Unit statistics, character classes, troop types. */
    UNITS("UNITS", Map.of(
            "DND_5E", "UNITS",
            "BABYLON_5", "UNITS",
            "STAR_WARS", "UNITS",
            "CAPTAINS_LOG", "UNITS",
            "TRAVELLER", "UNITS",
            "EARTH_2026", "UNITS",
            "MECH_WARRIOR", "UNITS",
            "THIRD_REICH", "UNITS"
    )),

    /** Lore, planets, species, galactic eras, world-building. */
    LORE("LORE", Map.of(
            "DND_5E", "LORE",
            "BABYLON_5", "LORE",
            "STAR_WARS", "LORE",
            "CAPTAINS_LOG", "LORE",
            "TRAVELLER", "LORE",
            "EARTH_2026", "LORE"
    )),

    /** Special abilities, leader powers, unit keywords. */
    SPECIAL_ABILITIES("SPECIAL", Map.of(
            "DND_5E", "SPECIAL_ABILITIES",
            "BABYLON_5", "SPECIAL_ABILITIES",
            "STAR_WARS", "SPECIAL_ABILITIES",
            "CAPTAINS_LOG", "SPECIAL_ABILITIES",
            "TRAVELLER", "SPECIAL_ABILITIES",
            "MECH_WARRIOR", "SPECIAL_ABILITIES"
    )),

    /** Scenarios, battles, mission objectives. */
    SCENARIOS("SCENARIO", Map.of(
            "DND_5E", "SCENARIOS",
            "BABYLON_5", "SCENARIOS",
            "STAR_WARS", "SCENARIOS",
            "CAPTAINS_LOG", "SCENARIOS",
            "TRAVELLER", "SCENARIOS",
            "EARTH_2026", "SCENARIOS",
            "THIRD_REICH", "SCENARIOS"
    )),

    /** Movement, positioning, zones of control. */
    MOVEMENT("MOVEMENT", Map.of(
            "DND_5E", "COMBAT",
            "BABYLON_5", "MOVEMENT",
            "STAR_WARS", "GROUND_COMBAT",
            "CAPTAINS_LOG", "MOVEMENT",
            "TRAVELLER", "MOVEMENT",
            "EARTH_2026", "MOVEMENT",
            "THIRD_REICH", "MOVEMENT"
    )),

    /** Initiative, turn order, action economy. */
    INITIATIVE("COMBAT", Map.of(
            "DND_5E", "COMBAT",
            "BABYLON_5", "DICE_MECHANICS",
            "STAR_WARS", "GROUND_COMBAT",
            "CAPTAINS_LOG", "COMBAT",
            "TRAVELLER", "COMBAT",
            "EARTH_2026", "COMBAT",
            "THIRD_REICH", "COMBAT"
    )),

    /** Critical hits, fumbles, edge cases in combat resolution. */
    CRITICALS("COMBAT", Map.of(
            "DND_5E", "COMBAT",
            "BABYLON_5", "CRITICALS",
            "STAR_WARS", "GROUND_COMBAT",
            "CAPTAINS_LOG", "CRITICALS",
            "TRAVELLER", "CRITICALS",
            "MECH_WARRIOR", "CRITICALS",
            "THIRD_REICH", "CRITICALS"
    )),

    /** Equipment, weapons, armor, gear. */
    EQUIPMENT("EQUIPMENT", Map.of(
            "DND_5E", "EQUIPMENT",
            "BABYLON_5", "EQUIPMENT",
            "STAR_WARS", "EQUIPMENT",
            "CAPTAINS_LOG", "EQUIPMENT",
            "TRAVELLER", "EQUIPMENT",
            "EARTH_2026", "EQUIPMENT",
            "MECH_WARRIOR", "EQUIPMENT",
            "THIRD_REICH", "EQUIPMENT"
    )),

    /** Character creation, advancement, experience, leveling. */
    CHARACTER_PROGRESSION("CHARACTER", Map.of(
            "DND_5E", "CHARACTER_CREATION",
            "BABYLON_5", "CHARACTER_CREATION",
            "STAR_WARS", "RPG_MECHANICS",
            "CAPTAINS_LOG", "CHARACTER_CREATION",
            "TRAVELLER", "CHARACTER_CREATION"
    ));

    private final String standardGroup;
    private final Map<String, String> moduleCategoryMap;

    RulesCategory(String standardGroup, Map<String, String> moduleCategoryMap) {
        this.standardGroup = standardGroup;
        this.moduleCategoryMap = moduleCategoryMap;
    }

    /**
     * Get the module-specific category string for a given game system.
     * Returns the enum name if no mapping exists for the system.
     *
     * @param gameSystem the game system to look up
     * @return the module-specific category string
     */
    public String getModuleCategory(GameSystem gameSystem) {
        return moduleCategoryMap.getOrDefault(gameSystem.name(), this.name());
    }

    /**
     * Get the standard group this category belongs to (e.g., "COMBAT", "MAGIC", "ENCOUNTER").
     *
     * @return the standard group
     */
    public String getStandardGroup() {
        return standardGroup;
    }

    /**
     * Find the standard category that a module-specific category maps to.
     *
     * @param moduleCategory the module-specific category string
     * @param gameSystem the game system it belongs to
     * @return the matching standard category, or null if not found
     */
    public static RulesCategory fromModuleCategory(String moduleCategory, GameSystem gameSystem) {
        for (RulesCategory cat : values()) {
            String mapped = cat.moduleCategoryMap.get(gameSystem.name());
            if (mapped != null && mapped.equalsIgnoreCase(moduleCategory)) {
                return cat;
            }
        }
        // Fallback: check if the module category matches any enum name directly
        for (RulesCategory cat : values()) {
            if (cat.name().equalsIgnoreCase(moduleCategory)) {
                return cat;
            }
        }
        return null;
    }

    /**
     * Get all standard categories in a specific group.
     *
     * @param group the standard group (e.g., "COMBAT", "MAGIC")
     * @return array of categories in that group
     */
    public static RulesCategory[] getByGroup(String group) {
        RulesCategory[] result = new RulesCategory[0];
        for (RulesCategory cat : values()) {
            if (cat.standardGroup.equals(group)) {
                result = java.util.Arrays.copyOf(result, result.length + 1);
                result[result.length - 1] = cat;
            }
        }
        return result;
    }
}
