package com.yisy.modules.doramcu.gamecore;

import com.yisy.modules.doramcu.techlevel.TechLevelAdapter;
import com.yisy.modules.doramcu.thirdreich.adapter.GroundCombatAdapter;
import com.yisy.modules.doramcu.gamecore.GameType;
import com.yisy.modules.doramcu.gamecore.RulesEngine;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Set;

/**
 * Abstract base class for all game modules in DoraMCU.
 * <p>
 * Eliminates boilerplate by providing default implementations for the {@link GameModule}
 * interface. Subclasses only need to override the methods they actually implement,
 * reducing each module from 100–1000 lines to 50–200 lines.
 * </p>
 * <p>
 * Following the existing {@code AbstractGameEngine} precedent, this class handles:
 * <ul>
 *   <li>Logger initialization</li>
 *   <li>GameModuleRegistry reference</li>
 *   <li>Auto-registration via {@link ApplicationReadyEvent}</li>
 *   <li>Default {@code null} for all service getters</li>
 *   <li>Default {@code isEnabled()} returning {@code true}</li>
 *   <li>Default {@code getMechanics()} returning empty set</li>
 *   <li>Default {@code initializeForGalaxy()} as no-op</li>
 * </ul>
 * </p>
 * <p>
 * Subclasses must implement only {@link #getGameType()} and {@link #getModuleName()}.
 * </p>
 */
public abstract class AbstractGameModule implements GameModule {

    protected final Logger log = LoggerFactory.getLogger(getClass());
    protected final GameModuleRegistry gameModuleRegistry;
    protected boolean enabled = true;

    // Optional delegating service fallbacks (injected lazily to avoid circular deps)
    private static DelegatingDiceService delegatingDiceService;
    private static DelegatingCombatService delegatingCombatService;
    private static DelegatingEncounterService delegatingEncounterService;
    private static DelegatingCharacterService delegatingCharacterService;

    @Autowired
    protected static void setDelegatingServices(
            DelegatingDiceService diceService,
            DelegatingCombatService combatService,
            DelegatingEncounterService encounterService,
            DelegatingCharacterService characterService) {
        delegatingDiceService = diceService;
        delegatingCombatService = combatService;
        delegatingEncounterService = encounterService;
        delegatingCharacterService = characterService;
    }

    /**
     * Creates a new game module and stores the registry reference.
     *
     * @param gameModuleRegistry the central registry for module discovery
     */
    protected AbstractGameModule(GameModuleRegistry gameModuleRegistry) {
        this.gameModuleRegistry = gameModuleRegistry;
    }

    /**
     * Auto-registers this module when the bean is constructed.
     * Using @PostConstruct ensures all modules are registered before
     * ApplicationReadyEvent fires, allowing GalaxyStartupRegistry to
     * see all modules when it processes the multi-galaxy build queue.
     * Subclasses should not override this method; instead override {@link #onRegister()}
     * if they need custom registration logic.
     */
    @PostConstruct
    public void register() {
        gameModuleRegistry.registerModule(this);
        onRegister();
    }

    /**
     * Hook called after registration. Subclasses can override for custom startup logic.
     */
    protected void onRegister() {
        // Default no-op
    }

    @Override
    public DiceService getDiceService() {
        return delegatingDiceService;
    }

    @Override
    public CombatService getCombatService() {
        return delegatingCombatService;
    }

    @Override
    public InventoryService getInventoryService() {
        return null;
    }

    @Override
    public CraftingService getCraftingService() {
        return null;
    }

    @Override
    public DungeonRoomService getDungeonRoomService() {
        return null;
    }

    @Override
    public SpellService getSpellService() {
        return null;
    }

    @Override
    public PsionicsService getPsionicsService() {
        return null;
    }

    @Override
    public EncounterService getEncounterService() {
        return delegatingEncounterService;
    }

    @Override
    public CharacterService getCharacterService() {
        return delegatingCharacterService;
    }

    @Override
    public CampaignService getCampaignService() {
        return null;
    }

    @Override
    public RulesService getRulesService() {
        return null;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public TechLevelAdapter getTechLevelAdapter() {
        return null;
    }

    @Override
    public RealEstateService getRealEstateService() {
        return null;
    }

    @Override
    public BuildingInstanceService getBuildingInstanceService() {
        return null;
    }

    @Override
    public GroundCombatAdapter getGroundCombatAdapter() {
        return null;
    }

    @Override
    public Set<MechanicType> getMechanics() {
        return Set.of();
    }

    @Override
    public void initializeForGalaxy(String galaxyName, String loreSlug) {
        // Default no-op: subclasses override when they need template-based initialization
    }

    @Override
    public RulesEngine getRulesEngine() {
        return null;
    }
}
