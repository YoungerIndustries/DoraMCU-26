package com.yisy.modules.doramcu.gamecore;

/**
 * Enumerates the game mechanics that a {@link GameModule} can provide.
 * <p>
 * Each mechanic represents a distinct gameplay domain (combat, economy, diplomacy, etc.).
 * Modules declare which mechanics they support via {@link GameModule#getMechanics()},
 * enabling the system to discover and filter modules by the mechanics they provide.
 * </p>
 * <p>
 * This is the foundation for the plugin-based architecture where Earth 2026 serves
 * as a template galaxy with all mechanics installed, and new galaxies are created
 * by selecting a subset of these mechanics.
 * </p>
 */
public enum MechanicType {

    /**
     * Combat system: weapon attacks, defense, hit points, damage calculation,
     * tactical positioning, and combat resolution.
     */
    COMBAT("Combat", "Weapon attacks, defense, damage, and combat resolution"),

    /**
     * Economy system: currency, trade, markets, pricing, and financial transactions.
     */
    ECONOMY("Economy", "Currency, trade, markets, and financial transactions"),

    /**
     * Diplomacy system: negotiations, alliances, treaties, and inter-faction relations.
     */
    DIPLOMACY("Diplomacy", "Negotiations, alliances, treaties, and inter-faction relations"),

    /**
     * Crafting system: resource gathering, item creation, recipes, and manufacturing.
     */
    CRAFTING("Crafting", "Resource gathering, item creation, recipes, and manufacturing"),

    /**
     * Exploration system: discovery, mapping, unknown systems, and frontier expansion.
     */
    EXPLORATION("Exploration", "Discovery, mapping, unknown systems, and frontier expansion"),

    /**
     * Survival system: hunger, thirst, oxygen, radiation, and environmental hazards.
     */
    SURVIVAL("Survival", "Hunger, thirst, oxygen, radiation, and environmental hazards"),

    /**
     * RPG system: character creation, skills, abilities, leveling, and progression.
     */
    RPG("RPG", "Character creation, skills, abilities, leveling, and progression"),

    /**
     * Tactical system: grid-based combat, movement orders, and strategic positioning.
     */
    TACTICAL("Tactical", "Grid-based combat, movement orders, and strategic positioning"),

    /**
     * FPS system: first-person shooter mechanics, aiming, and real-time combat.
     */
    FPS("FPS", "First-person shooter mechanics, aiming, and real-time combat"),

    /**
     * Simulation system: realistic physics, orbital mechanics, and system simulation.
     */
    SIMULATION("Simulation", "Realistic physics, orbital mechanics, and system simulation"),

    /**
     * 4X system: explore, expand, exploit, exterminate — empire management and strategy.
     */
    FOUR_X("4X", "Explore, expand, exploit, exterminate — empire management"),

    /**
     * Tycoon system: business management, property ownership, and profit optimization.
     */
    TYCOON("Tycoon", "Business management, property ownership, and profit optimization"),

    /**
     * Character system: character stats, attributes, and character data management.
     */
    CHARACTER("Character", "Character stats, attributes, and character data management"),

    /**
     * Inventory system: item storage, equipment, and inventory management.
     */
    INVENTORY("Inventory", "Item storage, equipment, and inventory management"),

    /**
     * Encounter system: random encounters, NPC interactions, and event generation.
     */
    ENCOUNTER("Encounter", "Random encounters, NPC interactions, and event generation"),

    /**
     * Campaign system: adventure tracking, quests, and campaign progression.
     */
    CAMPAIGN("Campaign", "Adventure tracking, quests, and campaign progression"),

    /**
     * Supernatural system: spells, psionics, and supernatural abilities.
     */
    SUPERNATURAL("Supernatural", "Spells, psionics, and supernatural abilities"),

    /**
     * Real estate system: property buying, renting, and listing management.
     */
    REAL_ESTATE("Real Estate", "Property buying, renting, and listing management"),

    /**
     * Technology system: tech levels, research, and technology progression.
     */
    TECHNOLOGY("Technology", "Tech levels, research, and technology progression");

    private final String displayName;
    private final String description;

    MechanicType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    /**
     * @return the human-readable name of this mechanic
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * @return a short description of what this mechanic covers
     */
    public String getDescription() {
        return description;
    }
}
