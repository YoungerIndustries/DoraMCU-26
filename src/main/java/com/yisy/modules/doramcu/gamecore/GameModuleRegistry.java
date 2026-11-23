package com.yisy.modules.doramcu.gamecore;

import com.yisy.modules.doramcu.techlevel.TechLevelAdapter;
import com.yisy.modules.doramcu.thirdreich.adapter.GroundCombatAdapter;
import com.yisy.modules.doramcu.thirdreich.service.ThirdReichRulesEngine;
import com.yisy.modules.doramcu.gamecore.GameType;
import com.yisy.modules.doramcu.gamecore.RulesEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.context.event.ContextRefreshedEvent;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Central registry for all game modules in DoraMCU.
 * <p>
 * Each game module (D&amp;D 5e, Traveller, Earth2026, etc.) registers itself with this registry
 * at startup. The registry provides a unified way to access any module's features through
 * the shared {@link GameModule} interface, eliminating code duplication and enabling
 * cross-module feature sharing.
 * </p>
 * <p>
 * For example, Earth2026 can access D&amp;D's dungeon room engine for building interiors:
 * <pre>
 * DungeonRoomService dungeonService = gameModuleRegistry.getDungeonRoomService(GameType.DND_5E);
 * dungeonService.createRoom(...);
 * </pre>
 * </p>
 */
@Service
public class GameModuleRegistry {

    private static final Logger log = LoggerFactory.getLogger(GameModuleRegistry.class);

    private final Map<GameType, GameModule> modules = new ConcurrentHashMap<>();

    @Resource
    @SuppressWarnings("SpringAutowiredFieldsWarningInspection_2")
    private MechanicRegistry mechanicRegistry;

    // Package-private setter for testing
    void setMechanicRegistry(MechanicRegistry mechanicRegistry) {
        this.mechanicRegistry = mechanicRegistry;
    }

    @Autowired(required = false)
    @SuppressWarnings("SpringAutowiredFieldsWarningInspection_2")
    private ThirdReichRulesEngine thirdReichRulesEngine;

    /**
     * Register a game module.
     *
     * @param module the module to register
     */
    public void registerModule(GameModule module) {
        modules.put(module.getGameType(), module);
        log.info("Registered game module: {} ({}) with mechanics: {}",
                module.getModuleName(), module.getGameType(), module.getMechanics());
    }

    /**
     * Rebuild the mechanic index after all modules are registered.
     * Call this at application startup to ensure the index is up to date.
     */
    public void rebuildMechanicIndex() {
        if (mechanicRegistry != null) {
            mechanicRegistry.rebuildIndex();
        }
    }

    /**
     * Rebuild the mechanic index after the application context is fully refreshed.
     * This fires after all {@link GameModule} beans have registered themselves
     * via {@link org.springframework.context.event.EventListener @EventListener(ApplicationReadyEvent.class)}.
     */
    @EventListener(ContextRefreshedEvent.class)
    public void onApplicationReady() {
        rebuildMechanicIndex();
    }

    /**
     * Get all modules that provide a specific mechanic.
     *
     * @param mechanicType the mechanic type to look up
     * @return list of modules providing this mechanic
     */
    public List<GameModule> getModulesForMechanic(MechanicType mechanicType) {
        if (mechanicRegistry == null) {
            return Collections.emptyList();
        }
        return mechanicRegistry.getModulesForMechanic(mechanicType);
    }

    /**
     * Get all available mechanics across all registered modules.
     *
     * @return set of all mechanic types
     */
    public Set<MechanicType> getAllMechanics() {
        if (mechanicRegistry == null) {
            return Collections.emptySet();
        }
        return mechanicRegistry.getAllMechanics();
    }

    /**
     * Get a registered game module by game type.
     *
     * @param gameType the game type
     * @return the module if registered
     */
    public Optional<GameModule> getModule(GameType gameType) {
        return Optional.ofNullable(modules.get(gameType));
    }

    /**
     * List all registered modules.
     *
     * @return list of all modules
     */
    public List<GameModule> getAllModules() {
        return List.copyOf(modules.values());
    }

    /**
     * Get the dice service from a specific module.
     *
     * @param gameType the game type
     * @return the dice service or null
     */
    public DiceService getDiceService(GameType gameType) {
        GameModule module = modules.get(gameType);
        return module != null ? module.getDiceService() : null;
    }

    /**
     * Get the combat service from a specific module.
     *
     * @param gameType the game type
     * @return the combat service or null
     */
    public CombatService getCombatService(GameType gameType) {
        GameModule module = modules.get(gameType);
        return module != null ? module.getCombatService() : null;
    }

    /**
     * Get the inventory service from a specific module.
     *
     * @param gameType the game type
     * @return the inventory service or null
     */
    public InventoryService getInventoryService(GameType gameType) {
        GameModule module = modules.get(gameType);
        return module != null ? module.getInventoryService() : null;
    }

    /**
     * Get the crafting service from a specific module.
     *
     * @param gameType the game type
     * @return the crafting service or null
     */
    public CraftingService getCraftingService(GameType gameType) {
        GameModule module = modules.get(gameType);
        return module != null ? module.getCraftingService() : null;
    }

    /**
     * Get the dungeon room service from a specific module.
     *
     * @param gameType the game type
     * @return the dungeon room service or null
     */
    public DungeonRoomService getDungeonRoomService(GameType gameType) {
        GameModule module = modules.get(gameType);
        return module != null ? module.getDungeonRoomService() : null;
    }

    /**
     * Get the spell service from a specific module.
     *
     * @param gameType the game type
     * @return the spell service or null
     */
    public SpellService getSpellService(GameType gameType) {
        GameModule module = modules.get(gameType);
        return module != null ? module.getSpellService() : null;
    }

    /**
     * Get the psionics service from a specific module.
     *
     * @param gameType the game type
     * @return the psionics service or null
     */
    public PsionicsService getPsionicsService(GameType gameType) {
        GameModule module = modules.get(gameType);
        return module != null ? module.getPsionicsService() : null;
    }

    /**
     * Get the encounter service from a specific module.
     *
     * @param gameType the game type
     * @return the encounter service or null
     */
    public EncounterService getEncounterService(GameType gameType) {
        GameModule module = modules.get(gameType);
        return module != null ? module.getEncounterService() : null;
    }

    /**
     * Get the character service from a specific module.
     *
     * @param gameType the game type
     * @return the character service or null
     */
    public CharacterService getCharacterService(GameType gameType) {
        GameModule module = modules.get(gameType);
        return module != null ? module.getCharacterService() : null;
    }

    /**
     * Get the campaign service from a specific module.
     *
     * @param gameType the game type
     * @return the campaign service or null
     */
    public CampaignService getCampaignService(GameType gameType) {
        GameModule module = modules.get(gameType);
        return module != null ? module.getCampaignService() : null;
    }

    /**
     * Get the rules service from a specific module.
     *
     * @param gameType the game type
     * @return the rules service or null
     */
    public RulesService getRulesService(GameType gameType) {
        GameModule module = modules.get(gameType);
        return module != null ? module.getRulesService() : null;
    }

    /**
     * Check if a module is registered and enabled.
     *
     * @param gameType the game type
     * @return true if the module is enabled
     */
    public boolean isModuleEnabled(GameType gameType) {
        GameModule module = modules.get(gameType);
        return module != null && module.isEnabled();
    }

    /**
     * Find a module that provides a specific service type.
     * Searches all enabled modules and returns the first one that has the service.
     *
     * @param gameType the game type to look for
     * @return the first enabled module with the service, or empty
     */
    public Optional<GameModule> findModuleWithDiceService() {
        return getAllModules().stream()
                .filter(m -> m.isEnabled() && m.getDiceService() != null)
                .findFirst();
    }

    public Optional<GameModule> findModuleWithCombatService() {
        return getAllModules().stream()
                .filter(m -> m.isEnabled() && m.getCombatService() != null)
                .findFirst();
    }

    public Optional<GameModule> findModuleWithDungeonRoomService() {
        return getAllModules().stream()
                .filter(m -> m.isEnabled() && m.getDungeonRoomService() != null)
                .findFirst();
    }

    public Optional<GameModule> findModuleWithCraftingService() {
        return getAllModules().stream()
                .filter(m -> m.isEnabled() && m.getCraftingService() != null)
                .findFirst();
    }

    public Optional<GameModule> findModuleWithSpellService() {
        return getAllModules().stream()
                .filter(m -> m.isEnabled() && m.getSpellService() != null)
                .findFirst();
    }

    public Optional<GameModule> findModuleWithPsionicsService() {
        return getAllModules().stream()
                .filter(m -> m.isEnabled() && m.getPsionicsService() != null)
                .findFirst();
    }

    /**
     * Get the tech level adapter from a specific module.
     *
     * @param gameType the game type
     * @return the tech level adapter or null
     */
    public TechLevelAdapter getTechLevelAdapter(GameType gameType) {
        GameModule module = modules.get(gameType);
        return module != null ? module.getTechLevelAdapter() : null;
    }

    /**
     * Get the ground combat adapter from a specific module.
     * <p>
     * The adapter converts game-specific ground units to 3R4 (Third Reich) format
     * for cross-module battlefield combat. For example, D&amp;D monsters can fight
     * alongside Star Trek ground teams and MechWarrior BattleMechs.
     * </p>
     *
     * @param gameType the game type
     * @return the ground combat adapter or null
     */
    public GroundCombatAdapter getGroundCombatAdapter(GameType gameType) {
        GameModule module = modules.get(gameType);
        return module != null ? module.getGroundCombatAdapter() : null;
    }

    /**
     * Get the rules engine from a specific module.
     * <p>
     * The rules engine provides a unified interface for dice rolling, task resolution,
     * and combat rounds specific to the game system. This enables cross-system task
     * resolution and normalized combat mechanics.
     * </p>
     *
     * @param gameType the game type
     * @return the rules engine, or empty if not available
     */
    public Optional<RulesEngine> getRulesEngine(GameType gameType) {
        GameModule module = modules.get(gameType);
        if (module == null) {
            return Optional.empty();
        }
        RulesEngine engine = module.getRulesEngine();
        return Optional.ofNullable(engine);
    }

    /**
     * Get the Third Reich rules engine directly.
     * <p>
     * Third Reich uses the 3R4 adapter pattern and has no GameModule,
     * so the rules engine is accessed directly via injection.
     * </p>
     *
     * @return the Third Reich rules engine, or empty if not available
     */
    public Optional<RulesEngine> getThirdReichRulesEngine() {
        return Optional.ofNullable(thirdReichRulesEngine);
    }

    // ========== Hot-Swap Lifecycle Methods ==========

    /**
     * Unregister a module from the registry.
     * <p>
     * Called when a module is being unloaded or reloaded.
     * Removes the module from the map and rebuilds the mechanic index.
     * </p>
     *
     * @param gameType the game type to unregister
     */
    public void unregisterModule(GameType gameType) {
        GameModule removed = modules.remove(gameType);
        if (removed != null) {
            log.info("Unregistered game module: {} ({})", removed.getModuleName(), gameType);
            // Rebuild mechanic index without this module
            if (mechanicRegistry != null) {
                mechanicRegistry.rebuildIndex();
            }
        }
    }

    /**
     * Reload a module by replacing its instance in the registry.
     * <p>
     * Called when a module has been reloaded with new code.
     * Replaces the old instance and rebuilds the mechanic index.
     * </p>
     *
     * @param gameType the game type
     * @param newModule the new module instance
     */
    public void reloadModule(GameType gameType, GameModule newModule) {
        modules.put(gameType, newModule);
        log.info("Reloaded game module: {} ({}) with mechanics: {}",
                newModule.getModuleName(), gameType, newModule.getMechanics());
        if (mechanicRegistry != null) {
            mechanicRegistry.rebuildIndex();
        }
    }

    /**
     * Toggle a module's enabled state.
     *
     * @param gameType the game type
     * @param enabled the new enabled state
     */
    public void enableModule(GameType gameType, boolean enabled) {
        GameModule module = modules.get(gameType);
        if (module != null) {
            log.info("Module {} {} ({})", gameType, enabled ? "enabled" : "disabled", module.getModuleName());
        }
    }

    /**
     * Get the internal modules map for iteration.
     *
     * @return the modules map
     */
    public Map<GameType, GameModule> getModulesMap() {
        return modules;
    }
}
