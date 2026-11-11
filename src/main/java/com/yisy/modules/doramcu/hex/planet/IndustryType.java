package com.yisy.modules.doramcu.hex.planet;

import java.util.*;

/**
 * Defines a type of industry that can be built on the planet surface.
 * <p>
 * Each industry type specifies:
 * <ul>
 *   <li>What cargo it produces (output)</li>
 *   <li>What cargo it consumes (input)</li>
 *   <li>The production chain level (primary, secondary, tertiary)</li>
 *   <li>Required terrain types for placement</li>
 *   <li>Construction cost and maintenance</li>
 * </ul>
 * <p>
 * Production chains follow the OpenTTD pattern:
 * <ul>
 *   <li>Primary: Mine, Farm, Quarry → produce raw materials</li>
 *   <li>Secondary: Steel Mill, Bakery, Sawmill → consume raw, produce processed</li>
 *   <li>Tertiary: Electronics Factory, Luxury Factory → consume processed, produce advanced</li>
 * </ul>
 */
public enum IndustryType {

    // ---- Primary Industries (Raw Materials) ----

    MINE("Mine", ProductionLevel.PRIMARY,
            Map.of(CargoType.ORE, 10),
            Map.of(),
            Set.of(TerrainType.MOUNTAIN, TerrainType.HIGHLAND),
            500, 10),

    COAL_MINE("Coal Mine", ProductionLevel.PRIMARY,
            Map.of(CargoType.COAL, 10),
            Map.of(),
            Set.of(TerrainType.MOUNTAIN, TerrainType.HIGHLAND, TerrainType.GRASSLAND),
            400, 8),

    OIL_WELL("Oil Well", ProductionLevel.PRIMARY,
            Map.of(CargoType.OIL, 8),
            Map.of(),
            Set.of(TerrainType.DESERT, TerrainType.GRASSLAND, TerrainType.DEEP_OCEAN),
            800, 15),

    LOGGING_CAMP("Logging Camp", ProductionLevel.PRIMARY,
            Map.of(CargoType.WOOD, 12),
            Map.of(),
            Set.of(TerrainType.FOREST, TerrainType.JUNGLE),
            300, 5),

    FARM("Farm", ProductionLevel.PRIMARY,
            Map.of(CargoType.GRAIN, 10),
            Map.of(),
            Set.of(TerrainType.GRASSLAND, TerrainType.SAVANNA),
            250, 5),

    RANCH("Ranch", ProductionLevel.PRIMARY,
            Map.of(CargoType.LIVESTOCK, 6),
            Map.of(),
            Set.of(TerrainType.GRASSLAND, TerrainType.SAVANNA),
            350, 7),

    FISHING_PORT("Fishing Port", ProductionLevel.PRIMARY,
            Map.of(CargoType.FISH, 8),
            Map.of(),
            Set.of(TerrainType.SHALLOW_WATER, TerrainType.BEACH),
            400, 8),

    ORCHARD("Orchard", ProductionLevel.PRIMARY,
            Map.of(CargoType.FRUIT, 8),
            Map.of(),
            Set.of(TerrainType.GRASSLAND, TerrainType.FOREST, TerrainType.SAVANNA),
            300, 6),

    // ---- Secondary Industries (Processed Materials) ----

    STEEL_MILL("Steel Mill", ProductionLevel.SECONDARY,
            Map.of(CargoType.STEEL, 6),
            Map.of(CargoType.ORE, 8, CargoType.COAL, 4),
            Set.of(TerrainType.GRASSLAND, TerrainType.DESERT, TerrainType.HIGHLAND),
            1200, 20),

    CEMENT_PLANT("Cement Plant", ProductionLevel.SECONDARY,
            Map.of(CargoType.CONCRETE, 8),
            Map.of(CargoType.ORE, 6),
            Set.of(TerrainType.GRASSLAND, TerrainType.DESERT, TerrainType.MOUNTAIN),
            800, 12),

    GLASS_FACTORY("Glass Factory", ProductionLevel.SECONDARY,
            Map.of(CargoType.GLASS, 6),
            Map.of(CargoType.CONCRETE, 4, CargoType.COAL, 2),
            Set.of(TerrainType.GRASSLAND, TerrainType.DESERT),
            900, 15),

    SAWMILL("Sawmill", ProductionLevel.SECONDARY,
            Map.of(CargoType.LUMBER, 8),
            Map.of(CargoType.WOOD, 10),
            Set.of(TerrainType.FOREST, TerrainType.GRASSLAND),
            500, 8),

    BAKERY("Bakery", ProductionLevel.SECONDARY,
            Map.of(CargoType.BREAD, 10),
            Map.of(CargoType.GRAIN, 8),
            Set.of(TerrainType.GRASSLAND, TerrainType.SAVANNA),
            400, 6),

    SLAUGHTERHOUSE("Slaughterhouse", ProductionLevel.SECONDARY,
            Map.of(CargoType.MEAT, 6),
            Map.of(CargoType.LIVESTOCK, 8),
            Set.of(TerrainType.GRASSLAND, TerrainType.SAVANNA),
            450, 7),

    REFINERY("Refinery", ProductionLevel.SECONDARY,
            Map.of(CargoType.REFINED_OIL, 6),
            Map.of(CargoType.OIL, 8),
            Set.of(TerrainType.DESERT, TerrainType.GRASSLAND),
            1500, 25),

    CHEMICAL_PLANT("Chemical Plant", ProductionLevel.SECONDARY,
            Map.of(CargoType.PLASTIC, 5),
            Map.of(CargoType.REFINED_OIL, 6),
            Set.of(TerrainType.DESERT, TerrainType.GRASSLAND),
            1200, 20),

    // ---- Tertiary Industries (Advanced Goods) ----

    ELECTRONICS_FACTORY("Electronics Factory", ProductionLevel.TERTIARY,
            Map.of(CargoType.ELECTRONICS, 3),
            Map.of(CargoType.STEEL, 4, CargoType.GLASS, 3, CargoType.PLASTIC, 2),
            Set.of(TerrainType.GRASSLAND),
            3000, 50),

    MACHINE_SHOP("Machine Shop", ProductionLevel.TERTIARY,
            Map.of(CargoType.MACHINERY, 3),
            Map.of(CargoType.STEEL, 6, CargoType.REFINED_OIL, 2),
            Set.of(TerrainType.GRASSLAND, TerrainType.DESERT),
            2500, 40),

    LUXURY_FACTORY("Luxury Factory", ProductionLevel.TERTIARY,
            Map.of(CargoType.LUXURY, 2),
            Map.of(CargoType.FRUIT, 4, CargoType.BREAD, 2, CargoType.PLASTIC, 2),
            Set.of(TerrainType.GRASSLAND),
            2000, 35),

    ADVANCED_CHEMICALS("Advanced Chemicals", ProductionLevel.TERTIARY,
            Map.of(CargoType.CHEMICALS, 3),
            Map.of(CargoType.PLASTIC, 4, CargoType.REFINED_OIL, 3),
            Set.of(TerrainType.DESERT, TerrainType.GRASSLAND),
            2200, 38);

    private final String displayName;
    private final ProductionLevel level;
    private final Map<CargoType, Integer> output;
    private final Map<CargoType, Integer> input;
    private final Set<TerrainType> requiredTerrain;
    private final int constructionCost;
    private final int maintenanceCost;

    IndustryType(String displayName, ProductionLevel level,
                 Map<CargoType, Integer> output, Map<CargoType, Integer> input,
                 Set<TerrainType> requiredTerrain,
                 int constructionCost, int maintenanceCost) {
        this.displayName = displayName;
        this.level = level;
        this.output = Collections.unmodifiableMap(new LinkedHashMap<>(output));
        this.input = Collections.unmodifiableMap(new LinkedHashMap<>(input));
        this.requiredTerrain = Collections.unmodifiableSet(new HashSet<>(requiredTerrain));
        this.constructionCost = constructionCost;
        this.maintenanceCost = maintenanceCost;
    }

    /** @return the human-readable display name */
    public String getDisplayName() {
        return displayName;
    }

    /** @return the production chain level */
    public ProductionLevel getLevel() {
        return level;
    }

    /** @return the cargo types and amounts produced per turn */
    public Map<CargoType, Integer> getOutput() {
        return output;
    }

    /** @return the cargo types and amounts consumed per turn */
    public Map<CargoType, Integer> getInput() {
        return input;
    }

    /** @return the terrain types where this industry can be built */
    public Set<TerrainType> getRequiredTerrain() {
        return requiredTerrain;
    }

    /** @return the construction cost in credits */
    public int getConstructionCost() {
        return constructionCost;
    }

    /** @return the per-turn maintenance cost in credits */
    public int getMaintenanceCost() {
        return maintenanceCost;
    }

    /**
     * @return true if this industry can be built on the given terrain type
     */
    public boolean canBuildOn(TerrainType terrain) {
        return requiredTerrain.contains(terrain);
    }

    /**
     * Production chain levels that determine industry complexity.
     */
    public enum ProductionLevel {
        /** Primary: extracts raw materials from the environment */
        PRIMARY,
        /** Secondary: processes raw materials into usable goods */
        SECONDARY,
        /** Tertiary: produces advanced goods from processed materials */
        TERTIARY
    }
}
