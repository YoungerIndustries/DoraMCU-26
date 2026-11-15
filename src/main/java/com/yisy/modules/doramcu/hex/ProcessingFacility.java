package com.yisy.modules.doramcu.hex;

import com.yisy.modules.doramcu.hex.manager.TechnologyTree;

import java.util.Map;

/**
 * Represents a facility that processes raw resources into refined resources.
 * <p>
 * A processing facility can process one recipe at a time. When processing is
 * complete, the refined resource output is added to the refined storage.
 * Supports both standard and alternative recipes, with alternative recipes
 * requiring the Advanced Processing technology to be unlocked.
 * </p>
 */
public class ProcessingFacility {

    private final int id;
    private final String name;
    private final int processingRate;

    // Default recipe for auto-restart when idle
    private final ProcessingRecipe defaultRecipe;

    // Current processing state
    private ProcessingRecipe currentRecipe;
    private int turnsRemaining;

    /**
     * Creates a new processing facility.
     *
     * @param id the unique facility ID
     * @param name the facility name
     * @param processingRate the processing rate multiplier (1 = normal, 2 = double speed)
     */
    public ProcessingFacility(int id, String name, int processingRate) {
        this.id = id;
        this.name = name;
        this.processingRate = Math.max(1, processingRate);
        this.defaultRecipe = null;
        this.currentRecipe = null;
        this.turnsRemaining = 0;
    }

    /**
     * Creates a new processing facility configured for the given recipe.
     * The recipe is used as the default for auto-restart when idle.
     *
     * @param recipe the recipe this facility will process
     */
    public ProcessingFacility(ProcessingRecipe recipe) {
        this.id = 0;
        this.name = recipe.getOutput().name();
        this.processingRate = 1;
        this.defaultRecipe = recipe;
        this.currentRecipe = recipe;
        this.turnsRemaining = recipe.getProcessingTimeTurns();
    }

    /** @return the unique facility ID */
    public int getId() {
        return id;
    }

    /** @return the facility name */
    public String getName() {
        return name;
    }

    /** @return the processing rate multiplier */
    public int getProcessingRate() {
        return processingRate;
    }

    /** @return the current recipe being processed, or null if idle */
    public ProcessingRecipe getCurrentRecipe() {
        return currentRecipe;
    }

    /** @return the number of turns remaining for the current recipe */
    public int getTurnsRemaining() {
        return turnsRemaining;
    }

    /** @return true if the facility is currently processing */
    public boolean isProcessing() {
        return currentRecipe != null;
    }

    /**
     * Starts processing the given recipe if the facility is idle and has enough raw resources.
     *
     * @param recipe the recipe to process
     * @param rawStorage the raw resource storage to consume from
     * @return true if processing started successfully
     */
    public boolean startProcessing(ProcessingRecipe recipe, ResourceStorage<RawResourceType> rawStorage) {
        if (isProcessing()) {
            return false;
        }
        if (!recipe.canStart(rawStorage.getInventory())) {
            return false;
        }
        // Consume inputs
        for (Map.Entry<RawResourceType, Integer> entry : recipe.getInputs().entrySet()) {
            rawStorage.remove(entry.getKey(), entry.getValue());
        }
        currentRecipe = recipe;
        turnsRemaining = (recipe.getProcessingTimeTurns() + processingRate - 1) / processingRate;
        return true;
    }

    /**
     * Processes one turn of the current recipe.
     * If idle, automatically starts processing the best available recipe:
     * prefers the standard recipe, falls back to the alternative recipe if unlocked.
     *
     * @param rawStorage the raw resource storage (used for starting new recipes)
     * @param refinedStorage the refined resource storage to add output to
     */
    public void processTurn(ResourceStorage<RawResourceType> rawStorage,
                            ResourceStorage<RefinedResourceType> refinedStorage) {
        processTurn(rawStorage, refinedStorage, null);
    }

    /**
     * Processes one turn of the current recipe.
     * If idle, automatically starts processing the best available recipe:
     * prefers the standard recipe, falls back to the alternative recipe if unlocked.
     *
     * @param rawStorage the raw resource storage (used for starting new recipes)
     * @param refinedStorage the refined resource storage to add output to
     * @param technologyTree optional technology tree to check for recipe unlocks
     */
    public void processTurn(ResourceStorage<RawResourceType> rawStorage,
                            ResourceStorage<RefinedResourceType> refinedStorage,
                            TechnologyTree technologyTree) {
        if (!isProcessing()) {
            // Auto-restart: try the best available recipe (standard first, then alternative if unlocked)
            if (defaultRecipe != null) {
                ProcessingRecipe best = getBestAvailableRecipe(
                        defaultRecipe.getOutput(), rawStorage.getInventory(), technologyTree);
                if (best != null && !startProcessing(best, rawStorage)) {
                    return;
                } else if (best == null) {
                    return;
                }
            } else {
                return;
            }
        }

        turnsRemaining--;

        if (turnsRemaining <= 0) {
            // Processing complete: add output
            refinedStorage.add(currentRecipe.getOutput(), currentRecipe.getOutputAmount());
            currentRecipe = null;
        }
    }

    /**
     * Returns the best available recipe for the given output type.
     * Prefers the standard recipe, falls back to the alternative only if unlocked.
     *
     * @param type the refined resource type to produce
     * @param inventory the current raw resource inventory
     * @param technologyTree optional technology tree to check for recipe unlocks
     * @return the best available recipe, or null if no recipe can be started
     */
    public static ProcessingRecipe getBestAvailableRecipe(RefinedResourceType type,
                                                          Map<RawResourceType, Integer> inventory,
                                                          TechnologyTree technologyTree) {
        ProcessingRecipe standard = ProcessingRecipe.getStandardRecipe(type);
        if (standard != null && standard.canStart(inventory)) {
            return standard;
        }
        // Alternative recipe is only available if Advanced Processing is unlocked
        if (technologyTree != null && technologyTree.hasAdvancedProcessing()) {
            ProcessingRecipe alternative = ProcessingRecipe.getAlternativeRecipe(type);
            if (alternative != null && alternative.canStart(inventory)) {
                return alternative;
            }
        }
        return null;
    }

    /**
     * Stops the current processing (if any).
     */
    public void stopProcessing() {
        currentRecipe = null;
        turnsRemaining = 0;
    }

    @Override
    public String toString() {
        if (isProcessing()) {
            return String.format("ProcessingFacility{id=%d, name=%s, processing=%s, turnsLeft=%d}",
                    id, name, currentRecipe.getOutput(), turnsRemaining);
        }
        return String.format("ProcessingFacility{id=%d, name=%s, idle}", id, name);
    }
}
