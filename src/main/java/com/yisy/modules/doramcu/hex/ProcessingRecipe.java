package com.yisy.modules.doramcu.hex;

import java.util.EnumMap;
import java.util.Map;

/**
 * Defines a recipe for processing raw resources into refined resources.
 * <p>
 * Each recipe specifies the raw resource inputs required and the refined
 * resource output produced. Processing takes a number of turns to complete.
 * </p>
 */
public class ProcessingRecipe {

    private final RefinedResourceType output;
    private final int outputAmount;
    private final Map<RawResourceType, Integer> inputs;
    private final int processingTimeTurns;

    /**
     * Creates a new processing recipe.
     *
     * @param output the refined resource type produced
     * @param outputAmount the amount produced per completion
     * @param inputs map of raw resource types to required quantities
     * @param processingTimeTurns turns required to complete one batch
     */
    public ProcessingRecipe(RefinedResourceType output, int outputAmount,
                            Map<RawResourceType, Integer> inputs, int processingTimeTurns) {
        this.output = output;
        this.outputAmount = outputAmount;
        this.inputs = Map.copyOf(inputs);
        this.processingTimeTurns = processingTimeTurns;
    }

    /** @return the refined resource type produced by this recipe */
    public RefinedResourceType getOutput() {
        return output;
    }

    /** @return the amount of refined resource produced per completion */
    public int getOutputAmount() {
        return outputAmount;
    }

    /**
     * Returns the raw resource inputs required.
     *
     * @return unmodifiable map of raw resource types to required quantities
     */
    public Map<RawResourceType, Integer> getInputs() {
        return inputs;
    }

    /** @return the number of turns required to complete one batch */
    public int getProcessingTimeTurns() {
        return processingTimeTurns;
    }

    /**
     * Checks if the given raw resource inventory can start this recipe.
     *
     * @param inventory the current raw resource inventory
     * @return true if all required inputs are available
     */
    public boolean canStart(Map<RawResourceType, Integer> inventory) {
        for (Map.Entry<RawResourceType, Integer> entry : inputs.entrySet()) {
            int available = inventory.getOrDefault(entry.getKey(), 0);
            if (available < entry.getValue()) {
                return false;
            }
        }
        return true;
    }

    /**
     * All standard processing recipes.
     */
    public static final Map<RefinedResourceType, ProcessingRecipe> STANDARD_RECIPES = Map.of(

            // Metal alloy: 2 ore + 1 energy (represented as crystal) → 1 metal alloy
            RefinedResourceType.METAL_ALLOY, new ProcessingRecipe(
                    RefinedResourceType.METAL_ALLOY, 1,
                    Map.of(RawResourceType.ORE, 2, RawResourceType.CRYSTAL, 1),
                    2
            ),

            // Power cell: 2 crystal + 1 ore → 1 power cell
            RefinedResourceType.POWER_CELL, new ProcessingRecipe(
                    RefinedResourceType.POWER_CELL, 1,
                    Map.of(RawResourceType.CRYSTAL, 2, RawResourceType.ORE, 1),
                    2
            ),

            // Fuel: 2 gas + 1 crystal → 1 fuel
            RefinedResourceType.FUEL, new ProcessingRecipe(
                    RefinedResourceType.FUEL, 1,
                    Map.of(RawResourceType.GAS, 2, RawResourceType.CRYSTAL, 1),
                    1
            ),

            // Composite: 2 dust + 1 ore + 1 crystal → 1 composite
            RefinedResourceType.COMPOSITE, new ProcessingRecipe(
                    RefinedResourceType.COMPOSITE, 1,
                    Map.of(RawResourceType.DUST, 2, RawResourceType.ORE, 1, RawResourceType.CRYSTAL, 1),
                    3
            ),

            // Nutrient: 2 bio-mass + 1 crystal → 1 nutrient
            RefinedResourceType.NUTRIENT, new ProcessingRecipe(
                    RefinedResourceType.NUTRIENT, 1,
                    Map.of(RawResourceType.BIO_MASS, 2, RawResourceType.CRYSTAL, 1),
                    1
            )
    );

    /**
     * Returns the standard recipe for producing the given refined resource type.
     *
     * @param type the refined resource type
     * @return the standard recipe, or null if no recipe exists
     */
    public static ProcessingRecipe getStandardRecipe(RefinedResourceType type) {
        return STANDARD_RECIPES.get(type);
    }

    /**
     * Alternative recipes that reduce CRYSTAL dependency.
     * These recipes use more of other resources but fewer or no CRYSTAL,
     * at the cost of longer processing times or lower output.
     */
    public static final Map<RefinedResourceType, ProcessingRecipe> ALTERNATIVE_RECIPES = Map.of(

            // Metal alloy alternative: 3 ORE only, slower, lower output
            RefinedResourceType.METAL_ALLOY, new ProcessingRecipe(
                    RefinedResourceType.METAL_ALLOY, 1,
                    Map.of(RawResourceType.ORE, 3),
                    3
            ),

            // Power cell alternative: 1 CRYSTAL + 1 GAS + 1 ORE
            RefinedResourceType.POWER_CELL, new ProcessingRecipe(
                    RefinedResourceType.POWER_CELL, 1,
                    Map.of(RawResourceType.CRYSTAL, 1, RawResourceType.GAS, 1, RawResourceType.ORE, 1),
                    3
            ),

            // Composite alternative: 3 DUST + 1 BIO_MASS, no CRYSTAL
            RefinedResourceType.COMPOSITE, new ProcessingRecipe(
                    RefinedResourceType.COMPOSITE, 1,
                    Map.of(RawResourceType.DUST, 3, RawResourceType.BIO_MASS, 1),
                    3
            ),

            // Fuel alternative: 3 GAS only, no CRYSTAL
            RefinedResourceType.FUEL, new ProcessingRecipe(
                    RefinedResourceType.FUEL, 1,
                    Map.of(RawResourceType.GAS, 3),
                    2
            ),

            // Nutrient alternative: 3 BIO_MASS only, no CRYSTAL
            RefinedResourceType.NUTRIENT, new ProcessingRecipe(
                    RefinedResourceType.NUTRIENT, 1,
                    Map.of(RawResourceType.BIO_MASS, 3),
                    2
            )
    );

    /**
     * Returns the alternative recipe for producing the given refined resource type.
     * Alternative recipes use fewer CRYSTAL but may be slower or require more of other resources.
     *
     * @param type the refined resource type
     * @return the alternative recipe, or null if no alternative exists
     */
    public static ProcessingRecipe getAlternativeRecipe(RefinedResourceType type) {
        return ALTERNATIVE_RECIPES.get(type);
    }

    /**
     * Returns the best available recipe for the given output type based on current inventory.
     * Prefers the standard recipe if resources are available, falls back to alternative.
     *
     * @param type the refined resource type to produce
     * @param inventory the current raw resource inventory
     * @return the best available recipe, or null if neither recipe can be started
     */
    public static ProcessingRecipe getBestRecipe(RefinedResourceType type, Map<RawResourceType, Integer> inventory) {
        ProcessingRecipe standard = STANDARD_RECIPES.get(type);
        if (standard != null && standard.canStart(inventory)) {
            return standard;
        }
        ProcessingRecipe alternative = ALTERNATIVE_RECIPES.get(type);
        if (alternative != null && alternative.canStart(inventory)) {
            return alternative;
        }
        return null;
    }
}
