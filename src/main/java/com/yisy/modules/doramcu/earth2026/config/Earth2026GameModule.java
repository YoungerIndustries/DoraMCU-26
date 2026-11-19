package com.yisy.modules.doramcu.earth2026.config;

import com.yisy.modules.doramcu.earth2026.service.Earth2026BuildingInstanceService;
import com.yisy.modules.doramcu.earth2026.service.Earth2026RealEstateService;
import com.yisy.modules.doramcu.earth2026.survival.crafting.E26CraftingService;
import com.yisy.modules.doramcu.earth2026.survival.crafting.E26InventoryService;
import com.yisy.modules.doramcu.earth2026.techlevel.Earth2026TechLevelAdapter;
import com.yisy.modules.doramcu.earth2026.service.Earth2026RulesEngine;
import com.yisy.modules.doramcu.techlevel.TechLevelAdapter;
import com.yisy.modules.doramcu.gamecore.RulesEngine;
import com.yisy.modules.doramcu.gamecore.GameType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * Earth 2026 GameModule implementation.
 * <p>
 * Provides the GameModule interface for Earth 2026, a near-future sci-fi RPG
 * set in a modern Earth with optional supernatural elements.
 * </p>
 * <p>
 * Currently provides:
 * - TechLevelAdapter for cross-galaxy conversion (TL5-TL6)
 * - Placeholder for future services (encounter, character, etc.)
 * </p>
 */
@Component
public class Earth2026GameModule extends AbstractGameModule {

    private final Earth2026TechLevelAdapter techLevelAdapter;
    private final Earth2026RealEstateService realEstateService;
    private final Earth2026BuildingInstanceService buildingInstanceService;
    private final E26InventoryService inventoryService;
    private final E26CraftingServiceAdapter craftingServiceAdapter;
    private final Earth2026RulesEngine rulesEngine;

    @Autowired
    public Earth2026GameModule(Earth2026TechLevelAdapter techLevelAdapter, GameModuleRegistry registry,
                                Earth2026RealEstateService realEstateService,
                                Earth2026BuildingInstanceService buildingInstanceService,
                                E26InventoryService inventoryService,
                                E26CraftingService craftingService,
                                Earth2026RulesEngine rulesEngine) {
        super(registry);
        this.techLevelAdapter = techLevelAdapter;
        this.realEstateService = realEstateService;
        this.buildingInstanceService = buildingInstanceService;
        this.inventoryService = inventoryService;
        this.craftingServiceAdapter = new E26CraftingServiceAdapter(craftingService);
        this.rulesEngine = rulesEngine;
    }

    @Override
    public GameType getGameType() {
        return GameType.EARTH_2026;
    }

    @Override
    public String getModuleName() {
        return "Earth 2026";
    }

    @Override
    public InventoryService getInventoryService() {
        return inventoryService;
    }

    @Override
    public CraftingService getCraftingService() {
        return craftingServiceAdapter;
    }

    @Override
    public TechLevelAdapter getTechLevelAdapter() {
        return techLevelAdapter;
    }

    @Override
    public RealEstateService getRealEstateService() {
        return realEstateService;
    }

    @Override
    public BuildingInstanceService getBuildingInstanceService() {
        return buildingInstanceService;
    }

    @Override
    public Set<MechanicType> getMechanics() {
        return Set.of(
                MechanicType.CRAFTING,
                MechanicType.INVENTORY,
                MechanicType.REAL_ESTATE,
                MechanicType.TECHNOLOGY,
                MechanicType.SURVIVAL,
                MechanicType.ECONOMY
        );
    }

    @Override
    public RulesEngine getRulesEngine() {
        return rulesEngine;
    }
}
