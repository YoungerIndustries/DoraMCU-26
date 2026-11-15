package com.yisy.modules.doramcu.hex;

import java.util.EnumMap;
import java.util.Map;

/**
 * Represents module types assembled from refined resources for construction.
 * <p>
 * Each module type has a standard recipe and an alternative recipe. The standard
 * recipe is more efficient but may require scarce resources (like POWER_CELL).
 * The alternative recipe uses more common resources but takes longer to assemble.
 * </p>
 */
public enum ConstructionModuleType {

    /**
     * Hull module for ship structure.
     * Standard: 2 metal alloy + 1 composite (2 turns)
     * Alternative: 3 metal alloy (3 turns)
     */
    HULL_MODULE("Hull Module",
            Map.of(RefinedResourceType.METAL_ALLOY, 2, RefinedResourceType.COMPOSITE, 1), 2,
            Map.of(RefinedResourceType.METAL_ALLOY, 3), 3),

    /**
     * Weapon module for ship armament.
     * Standard: 1 metal alloy + 1 power cell (1 turn)
     * Alternative: 2 metal alloy + 1 fuel (2 turns)
     */
    WEAPON_MODULE("Weapon Module",
            Map.of(RefinedResourceType.METAL_ALLOY, 1, RefinedResourceType.POWER_CELL, 1), 1,
            Map.of(RefinedResourceType.METAL_ALLOY, 2, RefinedResourceType.FUEL, 1), 2),

    /**
     * Shield module for ship defense.
     * Standard: 1 power cell + 1 composite (1 turn)
     * Alternative: 2 composite + 1 fuel (2 turns)
     */
    SHIELD_MODULE("Shield Module",
            Map.of(RefinedResourceType.POWER_CELL, 1, RefinedResourceType.COMPOSITE, 1), 1,
            Map.of(RefinedResourceType.COMPOSITE, 2, RefinedResourceType.FUEL, 1), 2),

    /**
     * Engine module for ship propulsion.
     * Standard: 1 fuel + 1 power cell (1 turn)
     * Alternative: 2 fuel + 1 metal alloy (2 turns)
     */
    ENGINE_MODULE("Engine Module",
            Map.of(RefinedResourceType.FUEL, 1, RefinedResourceType.POWER_CELL, 1), 1,
            Map.of(RefinedResourceType.FUEL, 2, RefinedResourceType.METAL_ALLOY, 1), 2),

    /**
     * Facility module for station construction.
     * Standard: 2 metal alloy + 1 nutrient (3 turns)
     * Alternative: 3 metal alloy + 1 composite (4 turns)
     */
    FACILITY_MODULE("Facility Module",
            Map.of(RefinedResourceType.METAL_ALLOY, 2, RefinedResourceType.NUTRIENT, 1), 3,
            Map.of(RefinedResourceType.METAL_ALLOY, 3, RefinedResourceType.COMPOSITE, 1), 4);

    private final String displayName;

    /** Standard (primary) recipe — more efficient */
    private final Map<RefinedResourceType, Integer> standardRecipe;
    private final int standardAssemblyTimeTurns;

    /** Alternative recipe — uses different resources, usually slower */
    private final Map<RefinedResourceType, Integer> alternativeRecipe;
    private final int alternativeAssemblyTimeTurns;

    ConstructionModuleType(String displayName,
                           Map<RefinedResourceType, Integer> standardRecipe, int standardAssemblyTimeTurns,
                           Map<RefinedResourceType, Integer> alternativeRecipe, int alternativeAssemblyTimeTurns) {
        this.displayName = displayName;
        this.standardRecipe = Map.copyOf(standardRecipe);
        this.standardAssemblyTimeTurns = standardAssemblyTimeTurns;
        this.alternativeRecipe = Map.copyOf(alternativeRecipe);
        this.alternativeAssemblyTimeTurns = alternativeAssemblyTimeTurns;
    }

    /** @return the human-readable display name */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Returns the standard recipe for this module type.
     *
     * @return unmodifiable map of refined resource types to required quantities
     */
    public Map<RefinedResourceType, Integer> getRecipe() {
        return standardRecipe;
    }

    /**
     * Returns the standard recipe for this module type (alias for getRecipe).
     */
    public Map<RefinedResourceType, Integer> getStandardRecipe() {
        return standardRecipe;
    }

    /**
     * Returns the alternative recipe for this module type.
     * Alternative recipes use different resources but may be slower.
     *
     * @return unmodifiable map of refined resource types to required quantities
     */
    public Map<RefinedResourceType, Integer> getAlternativeRecipe() {
        return alternativeRecipe;
    }

    /**
     * Returns the number of turns required to assemble one unit using the standard recipe.
     */
    public int getAssemblyTimeTurns() {
        return standardAssemblyTimeTurns;
    }

    /**
     * Returns the number of turns required to assemble one unit using the alternative recipe.
     */
    public int getAlternativeAssemblyTimeTurns() {
        return alternativeAssemblyTimeTurns;
    }

    /**
     * Checks if the given inventory can produce one unit of this module using the standard recipe.
     *
     * @param inventory the current refined resource inventory
     * @return true if all required resources are available in sufficient quantity
     */
    public boolean canProduce(Map<RefinedResourceType, Integer> inventory) {
        return canProduceWith(standardRecipe, inventory);
    }

    /**
     * Checks if the given inventory can produce one unit using the alternative recipe.
     *
     * @param inventory the current refined resource inventory
     * @return true if all required resources are available in sufficient quantity
     */
    public boolean canProduceAlternative(Map<RefinedResourceType, Integer> inventory) {
        return canProduceWith(alternativeRecipe, inventory);
    }

    /**
     * Checks if the given inventory can produce one unit using a specific recipe.
     *
     * @param recipe the recipe to check against
     * @param inventory the current refined resource inventory
     * @return true if all required resources are available in sufficient quantity
     */
    public boolean canProduceWith(Map<RefinedResourceType, Integer> recipe, Map<RefinedResourceType, Integer> inventory) {
        for (Map.Entry<RefinedResourceType, Integer> entry : recipe.entrySet()) {
            int available = inventory.getOrDefault(entry.getKey(), 0);
            if (available < entry.getValue()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns the best available recipe for this module type based on current inventory.
     * Prefers the standard recipe if resources are available, falls back to the alternative.
     *
     * @param inventory the current refined resource inventory
     * @return the best available recipe, or null if neither recipe can be started
     */
    public Map<RefinedResourceType, Integer> getBestRecipe(Map<RefinedResourceType, Integer> inventory) {
        if (canProduceWith(standardRecipe, inventory)) {
            return standardRecipe;
        }
        if (canProduceWith(alternativeRecipe, inventory)) {
            return alternativeRecipe;
        }
        return null;
    }

    /**
     * Returns the assembly time for the best available recipe based on current inventory.
     *
     * @param inventory the current refined resource inventory
     * @return the assembly time in turns, or -1 if no recipe is available
     */
    public int getBestAssemblyTime(Map<RefinedResourceType, Integer> inventory) {
        if (canProduceWith(standardRecipe, inventory)) {
            return standardAssemblyTimeTurns;
        }
        if (canProduceWith(alternativeRecipe, inventory)) {
            return alternativeAssemblyTimeTurns;
        }
        return -1;
    }

    /**
     * Returns the total refined resource cost for producing the specified quantity
     * using the standard recipe.
     *
     * @param quantity the number of modules to produce
     * @return map of refined resource types to total required quantities
     */
    public Map<RefinedResourceType, Integer> getCostForQuantity(int quantity) {
        return getCostForQuantity(standardRecipe, quantity);
    }

    /**
     * Returns the total refined resource cost for producing the specified quantity
     * using a specific recipe.
     *
     * @param recipe the recipe to use
     * @param quantity the number of modules to produce
     * @return map of refined resource types to total required quantities
     */
    public Map<RefinedResourceType, Integer> getCostForQuantity(Map<RefinedResourceType, Integer> recipe, int quantity) {
        Map<RefinedResourceType, Integer> cost = new EnumMap<>(RefinedResourceType.class);
        for (Map.Entry<RefinedResourceType, Integer> entry : recipe.entrySet()) {
            cost.put(entry.getKey(), entry.getValue() * quantity);
        }
        return cost;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
