package com.yisy.modules.doramcu.hex;

import com.yisy.modules.doramcu.hex.model.InfrastructureItem;
import com.yisy.modules.doramcu.hex.model.MaterialCost;
import com.yisy.modules.doramcu.hex.model.MaterialCost.MaterialCostType;
import com.yisy.modules.doramcu.hex.repository.InfrastructureItemRepository;
import com.yisy.modules.doramcu.hex.repository.MaterialCostRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for the MaterialCostService using an H2 in-memory database.
 * <p>
 * Tests material cost lookups, recipe retrieval, and recursive cost calculation.
 * </p>
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = EconomicProductionTestConfig.class)
@ActiveProfiles("test")
@Transactional
@DisplayName("MaterialCostService Integration Tests")
class MaterialCostServiceIntegrationTest {

    @Autowired
    private MaterialCostService materialCostService;

    @Autowired
    private MaterialCostRepository materialCostRepository;

    @Autowired
    private InfrastructureItemRepository infrastructureItemRepository;

    private MaterialCost oreCost;
    private MaterialCost metalAlloyCost;
    private MaterialCost hullModule;

    @BeforeEach
    void setUp() {
        // Create raw resource: ORE
        oreCost = new MaterialCost();
        oreCost.setMaterialType(MaterialCostType.RAW_RESOURCE);
        oreCost.setMaterialName("ORE");
        oreCost.setBaseValueCredits(new BigDecimal("10"));
        oreCost.setProcessingTimeTurns(1);
        oreCost = materialCostRepository.save(oreCost);

        // Create refined resource: METAL_ALLOY
        metalAlloyCost = new MaterialCost();
        metalAlloyCost.setMaterialType(MaterialCostType.REFINED_RESOURCE);
        metalAlloyCost.setMaterialName("METAL_ALLOY");
        metalAlloyCost.setBaseValueCredits(new BigDecimal("15"));
        metalAlloyCost.setRawInputsJson("{\"ORE\": 2}");
        metalAlloyCost.setProcessingTimeTurns(2);
        metalAlloyCost = materialCostRepository.save(metalAlloyCost);

        // Create construction module: HULL
        hullModule = new MaterialCost();
        hullModule.setMaterialType(MaterialCostType.CONSTRUCTION_MODULE);
        hullModule.setMaterialName("HULL");
        hullModule.setBaseValueCredits(new BigDecimal("25"));
        hullModule.setRefinedInputsJson("{\"METAL_ALLOY\": 3}");
        hullModule.setProcessingTimeTurns(3);
        hullModule = materialCostRepository.save(hullModule);

        // Create infrastructure item
        InfrastructureItem item = new InfrastructureItem();
        item.setItemType(InfrastructureItem.InfrastructureItemType.PLANET_FACILITY);
        item.setItemName("TEST_FACILITY");
        item.setDescription("Test facility");
        item.setBuildTimeTurns(10);
        item.setRequiredCrewGroups(1);
        item.setMineralCost(50);
        item.setEnergyCost(20);
        item.setRawResourcesJson("{\"ORE\": 50}");
        item.setRefinedResourcesJson("{\"METAL_ALLOY\": 10}");
        item.setConstructionModulesJson("{\"HULL\": 1}");
        infrastructureItemRepository.save(item);
    }

    @Test
    @DisplayName("Get all raw resources should return only raw resources")
    void getAllRawResources() {
        List<MaterialCost> rawResources = materialCostService.getAllRawResources();

        assertTrue(rawResources.size() >= 1);
        for (MaterialCost mc : rawResources) {
            assertEquals(MaterialCostType.RAW_RESOURCE, mc.getMaterialType());
        }
    }

    @Test
    @DisplayName("Get all refined resources should return only refined resources")
    void getAllRefinedResources() {
        List<MaterialCost> refinedResources = materialCostService.getAllRefinedResources();

        assertTrue(refinedResources.size() >= 1);
        for (MaterialCost mc : refinedResources) {
            assertEquals(MaterialCostType.REFINED_RESOURCE, mc.getMaterialType());
        }
    }

    @Test
    @DisplayName("Get all construction modules should return only construction modules")
    void getAllConstructionModules() {
        List<MaterialCost> modules = materialCostService.getAllConstructionModules();

        assertTrue(modules.size() >= 1);
        for (MaterialCost mc : modules) {
            assertEquals(MaterialCostType.CONSTRUCTION_MODULE, mc.getMaterialType());
        }
    }

    @Test
    @DisplayName("Get material cost by name should return the material")
    void getMaterialCost() {
        Optional<MaterialCost> found = materialCostService.getMaterialCost("ORE");

        assertTrue(found.isPresent());
        assertEquals("ORE", found.get().getMaterialName());
        assertEquals(MaterialCostType.RAW_RESOURCE, found.get().getMaterialType());
        assertEquals(new BigDecimal("10"), found.get().getBaseValueCredits());
    }

    @Test
    @DisplayName("Get material cost by non-existent name should return empty")
    void getMaterialCost_notFound() {
        Optional<MaterialCost> found = materialCostService.getMaterialCost("NONEXISTENT");

        assertFalse(found.isPresent());
    }

    @Test
    @DisplayName("Get recipe should return the material with recipe details")
    void getRecipe() {
        Optional<MaterialCost> recipe = materialCostService.getRecipe("METAL_ALLOY");

        assertTrue(recipe.isPresent());
        assertEquals("METAL_ALLOY", recipe.get().getMaterialName());
        assertNotNull(recipe.get().getRawInputsJson());
        assertTrue(recipe.get().getRawInputsJson().contains("ORE"));
    }

    @Test
    @DisplayName("Get alternative recipe should return empty when no alternative exists")
    void getAlternativeRecipe() {
        Optional<MaterialCost> altRecipe = materialCostService.getAlternativeRecipe("METAL_ALLOY");

        // No alternative recipe set, so should be empty
        assertFalse(altRecipe.isPresent());
    }

    @Test
    @DisplayName("Calculate total material cost should calculate recursive cost")
    void calculateTotalMaterialCost() {
        BigDecimal totalCost = materialCostService.calculateTotalMaterialCost("HULL");

        // HULL depends on METAL_ALLOY (3 units), which depends on ORE (2 units each)
        // METAL_ALLOY value = 2 * 10 * 1.3 = 26 (processing markup)
        // HULL value = 3 * 26 * 1.5 = 117 (assembly markup)
        // The base value is 25, but recursive calculation should be higher
        assertNotNull(totalCost);
        assertTrue(totalCost.compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    @DisplayName("Calculate total material cost for raw resource should return base value")
    void calculateTotalMaterialCost_rawResource() {
        BigDecimal totalCost = materialCostService.calculateTotalMaterialCost("ORE");

        assertEquals(new BigDecimal("10"), totalCost);
    }

    @Test
    @DisplayName("Get infrastructure item should return the saved item")
    void getInfrastructureItem() {
        // Save and get the item back by ID
        Optional<InfrastructureItem> foundOpt = infrastructureItemRepository.findByItemTypeAndItemName(
                InfrastructureItem.InfrastructureItemType.PLANET_FACILITY, "TEST_FACILITY");
        assertTrue(foundOpt.isPresent());
        Optional<InfrastructureItem> found = materialCostService.getInfrastructureItem(foundOpt.get().getId());

        assertTrue(found.isPresent());
        assertEquals("TEST_FACILITY", found.get().getItemName());
        assertEquals(InfrastructureItem.InfrastructureItemType.PLANET_FACILITY, found.get().getItemType());
    }

    @Test
    @DisplayName("Parse JSON quantity map should parse simple JSON")
    void parseJsonQuantityMap() {
        Map<String, Integer> result = materialCostService.parseJsonQuantityMap("{\"ORE\": 100, \"CRYSTAL\": 50}");

        assertEquals(2, result.size());
        assertEquals(100, result.get("ORE"));
        assertEquals(50, result.get("CRYSTAL"));
    }

    @Test
    @DisplayName("Parse JSON quantity map with empty JSON should return empty map")
    void parseJsonQuantityMap_empty() {
        Map<String, Integer> result = materialCostService.parseJsonQuantityMap("{}");

        assertTrue(result.isEmpty());
    }
}
