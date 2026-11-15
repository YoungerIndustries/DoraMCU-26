package com.yisy.modules.doramcu.hex;

import com.yisy.modules.doramcu.hex.manager.TechnologyTree;

import java.util.Map;

/**
 * Assembles modules from refined resources.
 * <p>
 * A module assembler can assemble one module type at a time. When assembly
 * is complete, the module is added to the module storage. Supports both
 * standard and alternative recipes, with alternative recipes requiring the
 * Advanced Assembly technology to be unlocked.
 * </p>
 */
public class ModuleAssembler {

    private final int id;
    private final String name;
    private final int assemblyRate;

    // Default module type for auto-restart when idle
    private final ConstructionModuleType defaultModule;

    // Current assembly state
    private ConstructionModuleType currentModule;
    private Map<RefinedResourceType, Integer> currentRecipe;
    private int turnsRemaining;

    /**
     * Creates a new module assembler.
     *
     * @param id the unique assembler ID
     * @param name the assembler name
     * @param assemblyRate the assembly rate multiplier (1 = normal, 2 = double speed)
     */
    public ModuleAssembler(int id, String name, int assemblyRate) {
        this.id = id;
        this.name = name;
        this.assemblyRate = Math.max(1, assemblyRate);
        this.defaultModule = null;
        this.currentModule = null;
        this.currentRecipe = null;
        this.turnsRemaining = 0;
    }

    /**
     * Creates a new module assembler configured for the given module type.
     * The module type is used as the default for auto-restart when idle.
     *
     * @param moduleType the module type this assembler will produce
     */
    public ModuleAssembler(ConstructionModuleType moduleType) {
        this.id = 0;
        this.name = moduleType.name();
        this.assemblyRate = 1;
        this.defaultModule = moduleType;
        this.currentModule = null;
        this.currentRecipe = null;
        this.turnsRemaining = 0;
    }

    /** @return the unique assembler ID */
    public int getId() {
        return id;
    }

    /** @return the assembler name */
    public String getName() {
        return name;
    }

    /** @return the assembly rate multiplier */
    public int getAssemblyRate() {
        return assemblyRate;
    }

    /** @return the current module being assembled, or null if idle */
    public ConstructionModuleType getCurrentModule() {
        return currentModule;
    }

    /** @return the current recipe being used, or null if idle */
    public Map<RefinedResourceType, Integer> getCurrentRecipe() {
        return currentRecipe;
    }

    /** @return the number of turns remaining for the current assembly */
    public int getTurnsRemaining() {
        return turnsRemaining;
    }

    /** @return true if the assembler is currently assembling */
    public boolean isAssembling() {
        return currentModule != null;
    }

    /**
     * Starts assembling the given module type using the standard recipe.
     * The assembler is idle and has enough refined resources.
     *
     * @param moduleType the module type to assemble
     * @param refinedStorage the refined resource storage to consume from
     * @return true if assembly started successfully
     */
    public boolean startAssembly(ConstructionModuleType moduleType, ResourceStorage<RefinedResourceType> refinedStorage) {
        return startAssemblyWithRecipe(moduleType, moduleType.getRecipe(), moduleType.getAssemblyTimeTurns(), refinedStorage);
    }

    /**
     * Starts assembling the given module type using a specific recipe.
     *
     * @param moduleType the module type to assemble
     * @param recipe the recipe to use (standard or alternative)
     * @param assemblyTime the assembly time in turns for this recipe
     * @param refinedStorage the refined resource storage to consume from
     * @return true if assembly started successfully
     */
    public boolean startAssemblyWithRecipe(ConstructionModuleType moduleType,
                                           Map<RefinedResourceType, Integer> recipe,
                                           int assemblyTime,
                                           ResourceStorage<RefinedResourceType> refinedStorage) {
        if (isAssembling()) {
            return false;
        }
        if (!moduleType.canProduceWith(recipe, refinedStorage.getInventory())) {
            return false;
        }
        // Consume refined resources
        for (var entry : recipe.entrySet()) {
            refinedStorage.remove(entry.getKey(), entry.getValue());
        }
        currentModule = moduleType;
        currentRecipe = recipe;
        turnsRemaining = (assemblyTime + assemblyRate - 1) / assemblyRate;
        return true;
    }

    /**
     * Processes one turn of the current assembly.
     * If idle, automatically starts assembling the default module type using the best available recipe:
     * prefers the standard recipe, falls back to the alternative recipe if unlocked.
     *
     * @param refinedStorage the refined resource storage (used for starting new recipes)
     * @param moduleStorage the module storage to add completed modules to
     */
    public void processTurn(ResourceStorage<RefinedResourceType> refinedStorage,
                            ResourceStorage<ConstructionModuleType> moduleStorage) {
        processTurn(refinedStorage, moduleStorage, null);
    }

    /**
     * Processes one turn of the current assembly.
     * If idle, automatically starts assembling the default module type using the best available recipe:
     * prefers the standard recipe, falls back to the alternative recipe if unlocked.
     *
     * @param refinedStorage the refined resource storage (used for starting new recipes)
     * @param moduleStorage the module storage to add completed modules to
     * @param technologyTree optional technology tree to check for recipe unlocks
     */
    public void processTurn(ResourceStorage<RefinedResourceType> refinedStorage,
                            ResourceStorage<ConstructionModuleType> moduleStorage,
                            TechnologyTree technologyTree) {
        if (!isAssembling()) {
            // Auto-restart: if idle and a default module is assigned, try the best available recipe
            if (defaultModule != null) {
                Map<RefinedResourceType, Integer> bestRecipe = getBestAvailableRecipe(
                        defaultModule, refinedStorage.getInventory(), technologyTree);
                if (bestRecipe != null) {
                    int assemblyTime = getBestAvailableAssemblyTime(
                            defaultModule, refinedStorage.getInventory(), technologyTree);
                    if (!startAssemblyWithRecipe(defaultModule, bestRecipe, assemblyTime, refinedStorage)) {
                        return;
                    }
                } else {
                    return;
                }
            } else {
                return;
            }
        }

        turnsRemaining--;

        if (turnsRemaining <= 0) {
            // Assembly complete: add module
            moduleStorage.add(currentModule, 1);
            currentModule = null;
            currentRecipe = null;
        }
    }

    /**
     * Returns the best available recipe for the given module type.
     * Prefers the standard recipe, falls back to the alternative only if unlocked.
     *
     * @param moduleType the module type to produce
     * @param inventory the current refined resource inventory
     * @param technologyTree optional technology tree to check for recipe unlocks
     * @return the best available recipe, or null if no recipe can be started
     */
    public static Map<RefinedResourceType, Integer> getBestAvailableRecipe(ConstructionModuleType moduleType,
                                                                           Map<RefinedResourceType, Integer> inventory,
                                                                           TechnologyTree technologyTree) {
        // Always try standard recipe first
        if (moduleType.canProduceWith(moduleType.getStandardRecipe(), inventory)) {
            return moduleType.getStandardRecipe();
        }
        // Alternative recipe is only available if Advanced Assembly is unlocked
        if (technologyTree != null && technologyTree.hasAdvancedAssembly()) {
            if (moduleType.canProduceWith(moduleType.getAlternativeRecipe(), inventory)) {
                return moduleType.getAlternativeRecipe();
            }
        }
        return null;
    }

    /**
     * Returns the assembly time for the best available recipe.
     *
     * @param moduleType the module type to produce
     * @param inventory the current refined resource inventory
     * @param technologyTree optional technology tree to check for recipe unlocks
     * @return the assembly time in turns, or -1 if no recipe is available
     */
    public static int getBestAvailableAssemblyTime(ConstructionModuleType moduleType,
                                                   Map<RefinedResourceType, Integer> inventory,
                                                   TechnologyTree technologyTree) {
        if (moduleType.canProduceWith(moduleType.getStandardRecipe(), inventory)) {
            return moduleType.getAssemblyTimeTurns();
        }
        if (technologyTree != null && technologyTree.hasAdvancedAssembly()) {
            if (moduleType.canProduceWith(moduleType.getAlternativeRecipe(), inventory)) {
                return moduleType.getAlternativeAssemblyTimeTurns();
            }
        }
        return -1;
    }

    /**
     * Stops the current assembly (if any).
     */
    public void stopAssembly() {
        currentModule = null;
        currentRecipe = null;
        turnsRemaining = 0;
    }

    @Override
    public String toString() {
        if (isAssembling()) {
            return String.format("ModuleAssembler{id=%d, name=%s, assembling=%s, turnsLeft=%d}",
                    id, name, currentModule, turnsRemaining);
        }
        return String.format("ModuleAssembler{id=%d, name=%s, idle}", id, name);
    }
}
