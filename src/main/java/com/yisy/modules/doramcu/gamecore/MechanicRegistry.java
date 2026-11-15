package com.yisy.modules.doramcu.gamecore;

import com.yisy.modules.doramcu.gamecore.GameType;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Registry that indexes all {@link GameModule} instances by the mechanics they provide.
 * <p>
 * When a {@link GameModule} is registered with the {@link GameModuleRegistry}, this registry
 * automatically indexes it by the {@link MechanicType} values returned from {@link GameModule#getMechanics()}.
 * This enables fast lookup of which module provides a given mechanic, and filtering of mechanics
 * by game type.
 * </p>
 * <p>
 * Example usage:
 * <pre>
 * // Find all modules that provide combat mechanics
 * List<GameModule> combatModules = mechanicRegistry.getModulesForMechanic(MechanicType.COMBAT);
 *
 * // Find which module provides a specific mechanic
 * Optional<GameModule> module = mechanicRegistry.getModuleForMechanic(MechanicType.ECONOMY);
 *
 * // Get all available mechanics across all registered modules
 * Set<MechanicType> allMechanics = mechanicRegistry.getAllMechanics();
 * </pre>
 * </p>
 */
@Service
public class MechanicRegistry {

    private static final Logger log = LoggerFactory.getLogger(MechanicRegistry.class);

    @Resource
    private GameModuleRegistry gameModuleRegistry;
    private final Map<MechanicType, List<GameModule>> mechanicToModules = new ConcurrentHashMap<>();

    /**
     * Rebuilds the mechanic index from all currently registered modules.
     * <p>
     * Call this method after modules are registered to ensure the index is up to date.
     * Typically called at application startup after all {@link GameModule} beans are initialized.
     * </p>
     */
    public void rebuildIndex() {
        if (gameModuleRegistry == null) {
            log.warn("GameModuleRegistry not set, cannot rebuild mechanic index");
            return;
        }
        mechanicToModules.clear();
        for (GameModule module : gameModuleRegistry.getAllModules()) {
            Set<MechanicType> mechanics = module.getMechanics();
            if (mechanics != null) {
                for (MechanicType mechanic : mechanics) {
                    mechanicToModules.computeIfAbsent(mechanic, k -> new ArrayList<>()).add(module);
                    log.debug("Indexed module {} for mechanic {}", module.getModuleName(), mechanic);
                }
            }
        }
        log.info("Mechanic index rebuilt: {} mechanics indexed across {} modules",
                mechanicToModules.size(), gameModuleRegistry.getAllModules().size());
    }

    /**
     * Get all modules that provide a specific mechanic.
     *
     * @param mechanicType the mechanic type to look up
     * @return list of modules providing this mechanic, empty if none
     */
    public List<GameModule> getModulesForMechanic(MechanicType mechanicType) {
        return Collections.unmodifiableList(mechanicToModules.getOrDefault(mechanicType, Collections.emptyList()));
    }

    /**
     * Get the first module that provides a specific mechanic.
     *
     * @param mechanicType the mechanic type to look up
     * @return the first module providing this mechanic, or empty if none
     */
    public Optional<GameModule> getModuleForMechanic(MechanicType mechanicType) {
        List<GameModule> modules = mechanicToModules.get(mechanicType);
        if (modules == null || modules.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(modules.get(0));
    }

    /**
     * Get all available mechanics across all registered modules.
     *
     * @return set of all mechanic types provided by registered modules
     */
    public Set<MechanicType> getAllMechanics() {
        return Collections.unmodifiableSet(mechanicToModules.keySet());
    }

    /**
     * Get all mechanics provided by a specific game type.
     *
     * @param gameType the game type to query
     * @return set of mechanics provided by modules of this game type
     */
    public Set<MechanicType> getMechanicsForGameType(GameType gameType) {
        return gameModuleRegistry.getModule(gameType)
                .map(GameModule::getMechanics)
                .orElse(Collections.emptySet());
    }

    /**
     * Check if a specific mechanic is provided by any registered module.
     *
     * @param mechanicType the mechanic type to check
     * @return true if at least one module provides this mechanic
     */
    public boolean hasMechanic(MechanicType mechanicType) {
        return mechanicToModules.containsKey(mechanicType);
    }

    /**
     * Get a summary of all mechanics and their providing modules.
     *
     * @return map of mechanic type to list of module names
     */
    public Map<MechanicType, List<String>> getMechanicSummary() {
        Map<MechanicType, List<String>> summary = new LinkedHashMap<>();
        for (Map.Entry<MechanicType, List<GameModule>> entry : mechanicToModules.entrySet()) {
            List<String> moduleNames = entry.getValue().stream()
                    .map(GameModule::getModuleName)
                    .collect(Collectors.toList());
            summary.put(entry.getKey(), moduleNames);
        }
        return Collections.unmodifiableMap(summary);
    }
}
