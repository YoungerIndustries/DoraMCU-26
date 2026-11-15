package com.yisy.modules.doramcu.gamecore;

import com.yisy.modules.doramcu.techlevel.TechLevelAdapter;
import com.yisy.modules.doramcu.thirdreich.adapter.GroundCombatAdapter;
import com.yisy.modules.doramcu.gamecore.GameType;
import com.yisy.modules.doramcu.gamecore.RulesEngine;

import java.util.Set;

/**
 * Common interface for all game modules in DoraMCU.
 * <p>
 * Each game module (D&amp;D 5e, Traveller, Earth2026, Captain's Log, etc.) implements this interface
 * to provide a unified access point for shared game mechanics. This eliminates code duplication
 * and allows Earth2026 to reuse D&amp;D and Traveller features without duplicating logic.
 * </p>
 * <p>
 * The plugin system registers each module at startup, and this interface provides the contract
 * for cross-module feature sharing. For example, Earth2026 can use D&amp;D's dungeon room engine
 * for building interiors, or Traveller's combat system for firearms encounters.
 * </p>
 */
public interface GameModule {

    /**
     * Returns the game type this module implements.
     *
     * @return the game type enum value
     */
    GameType getGameType();

    /**
     * Returns the display name of this module.
     *
     * @return human-readable module name
     */
    String getModuleName();

    /**
     * Returns the dice service for this module.
     * All modules share the same DiceUtil-based rolling system.
     *
     * @return the dice service, or null if not available
     */
    DiceService getDiceService();

    /**
     * Returns the combat service for this module.
     * Different modules may have different combat systems (D&amp;D tactical, Traveller firearms, etc.).
     *
     * @return the combat service, or null if not available
     */
    CombatService getCombatService();

    /**
     * Returns the inventory service for this module.
     *
     * @return the inventory service, or null if not available
     */
    InventoryService getInventoryService();

    /**
     * Returns the crafting service for this module.
     *
     * @return the crafting service, or null if not available
     */
    CraftingService getCraftingService();

    /**
     * Returns the dungeon/interior room service for this module.
     * Used for building interiors, 3D city rooms, and structure layouts.
     *
     * @return the dungeon room service, or null if not available
     */
    DungeonRoomService getDungeonRoomService();

    /**
     * Returns the spell/supernatural service for this module.
     * May return spells (D&amp;D), psionics (D&amp;D/Traveller), or supernatural abilities (Earth2026).
     *
     * @return the spell service, or null if not available
     */
    SpellService getSpellService();

    /**
     * Returns the psionics service for this module.
     *
     * @return the psionics service, or null if not available
     */
    PsionicsService getPsionicsService();

    /**
     * Returns the encounter service for this module.
     * Used for random encounters, NPC interactions, and event generation.
     *
     * @return the encounter service, or null if not available
     */
    EncounterService getEncounterService();

    /**
     * Returns the character service for this module.
     *
     * @return the character service, or null if not available
     */
    CharacterService getCharacterService();

    /**
     * Returns the campaign/adventure service for this module.
     *
     * @return the campaign service, or null if not available
     */
    CampaignService getCampaignService();

    /**
     * Returns the rules database service for this module.
     *
     * @return the rules service, or null if not available
     */
    RulesService getRulesService();

    /**
     * Checks if this module is currently loaded and active.
     *
     * @return true if the module is enabled
     */
    boolean isEnabled();

    /**
     * Returns the tech level adapter for this module.
     * <p>
     * The adapter maps generic tech-level building modules to game-specific
     * lore names, stats, and descriptions. For example, D&amp;D 5e maps
     * TL2 settlements to "Stone Castle" while Traveller maps them to "Feudal Hold".
     * </p>
     *
     * @return the tech level adapter, or null if not available
     */
    default TechLevelAdapter getTechLevelAdapter() {
        return null;
    }

    /**
     * Returns the real estate service for this module.
     * Used for buying, renting, and listing properties such as offices and apartments.
     *
     * @return the real estate service, or null if not available
     */
    default RealEstateService getRealEstateService() {
        return null;
    }

    /**
     * Returns the building instance service for this module.
     * Used for managing building instances, floors, and basement dungeon levels.
     *
     * @return the building instance service, or null if not available
     */
    default BuildingInstanceService getBuildingInstanceService() {
        return null;
    }

    /**
     * Returns the ground combat adapter for this module.
     * <p>
     * The adapter converts game-specific ground units to 3R4 (Third Reich)
     * ground units for cross-module battlefield combat. For example, D&amp;D
     * monsters are converted to 3R4 units so they can fight alongside Star Trek
     * ground teams and MechWarrior BattleMechs on the same battlefield.
     * </p>
     *
     * @return the ground combat adapter, or null if not available
     */
    default GroundCombatAdapter getGroundCombatAdapter() {
        return null;
    }

    /**
     * Returns the set of game mechanics this module provides.
     * <p>
     * Each module declares which {@link MechanicType} values it supports,
     * enabling the system to discover and filter modules by mechanic.
     * For example, a combat module returns {@code COMBAT}, while a trading
     * module returns {@code ECONOMY}.
     * </p>
     * <p>
     * The default implementation returns an empty set. Subclasses should
     * override this method to declare their supported mechanics.
     * </p>
     *
     * @return the set of mechanic types this module provides
     */
    default Set<MechanicType> getMechanics() {
        return Set.of();
    }

    /**
     * Initializes this module for a new galaxy created from a template.
     * <p>
     * Called when a new galaxy is cloned from the Earth 2026 template.
     * Modules can use this hook to set up galaxy-specific data, such as
     * creating initial factions, species, or technology entries.
     * </p>
     *
     * @param galaxyName the name of the new galaxy
     * @param loreSlug   the lore slug identifying the source template
     */
    default void initializeForGalaxy(String galaxyName, String loreSlug) {
        // Default no-op: subclasses override when they need template-based initialization
    }

    /**
     * Returns the rules engine for this module.
     * <p>
     * The rules engine provides a unified interface for dice rolling, task resolution,
     * and combat rounds specific to this game system. Different modules implement
     * different dice mechanics (d20, 2D6, 2d20, etc.).
     * </p>
     *
     * @return the rules engine, or null if not available
     */
    default RulesEngine getRulesEngine() {
        return null;
    }
}
