package com.yisy.modules.doramcu.controller.economic;

import com.yisy.modules.doramcu.hex.MaterialCostService;
import com.yisy.modules.doramcu.hex.model.MaterialCost;
import com.yisy.modules.doramcu.hex.model.MaterialCost.MaterialCostType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * REST controller for material cost read-only data.
 * <p>
 * Provides endpoints for querying material costs, recipes, and cost calculations
 * in the economic production system.
 * </p>
 * <p>
 * Endpoints:
 * <ul>
 *   <li>{@code GET /api/v1/economic/{galaxyId}/materials} — All materials</li>
 *   <li>{@code GET /api/v1/economic/{galaxyId}/materials/raw} — Raw resources only</li>
 *   <li>{@code GET /api/v1/economic/{galaxyId}/materials/refined} — Refined resources only</li>
 *   <li>{@code GET /api/v1/economic/{galaxyId}/modules} — Construction modules only</li>
 *   <li>{@code GET /api/v1/economic/{galaxyId}/materials/{materialName}} — Single material</li>
 *   <li>{@code GET /api/v1/economic/{galaxyId}/materials/{materialName}/recipe} — Standard recipe</li>
 *   <li>{@code GET /api/v1/economic/{galaxyId}/materials/{materialName}/alt-recipe} — Alternative recipe</li>
 * </ul>
 * </p>
 */
@RestController
@RequestMapping("/api/v1/economic")
public class MaterialCostController {

    private static final Logger log = LoggerFactory.getLogger(MaterialCostController.class);

    private final MaterialCostService materialCostService;

    public MaterialCostController(MaterialCostService materialCostService) {
        this.materialCostService = materialCostService;
    }

    // ------------------------------------------------------------------
    // List endpoints
    // ------------------------------------------------------------------

    /**
     * Get all materials.
     */
    @GetMapping("/{galaxyId}/materials")
    public ResponseEntity<Map<String, Object>> getAllMaterials(
            @PathVariable Long galaxyId,
            @RequestParam(required = false) String type) {

        List<MaterialCost> materials;

        if (type != null) {
            MaterialCostType materialCostType = MaterialCostType.valueOf(type);
            materials = materialCostService.getAllByTypeSortedByValue(materialCostType);
        } else {
            materials = materialCostService.getAllRawResources();
            // Combine all types
            materials.addAll(materialCostService.getAllRefinedResources());
            materials.addAll(materialCostService.getAllConstructionModules());
        }

        List<Map<String, Object>> items = materials.stream()
                .map(this::toMap)
                .collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("galaxyId", galaxyId);
        result.put("count", items.size());
        result.put("materials", items);
        return ResponseEntity.ok(result);
    }

    /**
     * Get raw resources only.
     */
    @GetMapping("/{galaxyId}/materials/raw")
    public ResponseEntity<Map<String, Object>> getRawResources(@PathVariable Long galaxyId) {
        List<MaterialCost> materials = materialCostService.getAllRawResources();

        List<Map<String, Object>> items = materials.stream()
                .map(this::toMap)
                .collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("galaxyId", galaxyId);
        result.put("count", items.size());
        result.put("materials", items);
        return ResponseEntity.ok(result);
    }

    /**
     * Get refined resources only.
     */
    @GetMapping("/{galaxyId}/materials/refined")
    public ResponseEntity<Map<String, Object>> getRefinedResources(@PathVariable Long galaxyId) {
        List<MaterialCost> materials = materialCostService.getAllRefinedResources();

        List<Map<String, Object>> items = materials.stream()
                .map(this::toMap)
                .collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("galaxyId", galaxyId);
        result.put("count", items.size());
        result.put("materials", items);
        return ResponseEntity.ok(result);
    }

    /**
     * Get construction modules only.
     */
    @GetMapping("/{galaxyId}/modules")
    public ResponseEntity<Map<String, Object>> getConstructionModules(@PathVariable Long galaxyId) {
        List<MaterialCost> materials = materialCostService.getAllConstructionModules();

        List<Map<String, Object>> items = materials.stream()
                .map(this::toMap)
                .collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("galaxyId", galaxyId);
        result.put("count", items.size());
        result.put("modules", items);
        return ResponseEntity.ok(result);
    }

    // ------------------------------------------------------------------
    // Single material endpoints
    // ------------------------------------------------------------------

    /**
     * Get a single material by name.
     */
    @GetMapping("/{galaxyId}/materials/{materialName}")
    public ResponseEntity<?> getMaterial(
            @PathVariable Long galaxyId,
            @PathVariable String materialName) {

        return materialCostService.getMaterialCost(materialName)
                .map(material -> {
                    Map<String, Object> result = new HashMap<>();
                    result.put("galaxyId", galaxyId);
                    result.put("material", toMap(material));
                    return ResponseEntity.ok(result);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get the standard recipe for a material.
     */
    @GetMapping("/{galaxyId}/materials/{materialName}/recipe")
    public ResponseEntity<?> getRecipe(
            @PathVariable Long galaxyId,
            @PathVariable String materialName) {

        return materialCostService.getRecipe(materialName)
                .map(recipe -> {
                    Map<String, Object> result = new HashMap<>();
                    result.put("galaxyId", galaxyId);
                    result.put("recipe", recipe);
                    return ResponseEntity.ok(result);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get the alternative recipe for a material.
     */
    @GetMapping("/{galaxyId}/materials/{materialName}/alt-recipe")
    public ResponseEntity<?> getAlternativeRecipe(
            @PathVariable Long galaxyId,
            @PathVariable String materialName) {

        return materialCostService.getAlternativeRecipe(materialName)
                .map(recipe -> {
                    Map<String, Object> result = new HashMap<>();
                    result.put("galaxyId", galaxyId);
                    result.put("recipe", recipe);
                    return ResponseEntity.ok(result);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Calculate total material cost for a given material name.
     */
    @GetMapping("/{galaxyId}/materials/{materialName}/total-cost")
    public ResponseEntity<?> getTotalCost(
            @PathVariable Long galaxyId,
            @PathVariable String materialName) {

        BigDecimal totalCost = materialCostService.calculateTotalMaterialCost(materialName);

        Map<String, Object> result = new HashMap<>();
        result.put("galaxyId", galaxyId);
        result.put("materialName", materialName);
        result.put("totalCost", totalCost);
        return ResponseEntity.ok(result);
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    /**
     * Convert a MaterialCost entity to a Map for JSON serialization.
     */
    private Map<String, Object> toMap(MaterialCost material) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", material.getId());
        map.put("materialType", material.getMaterialType() != null ? material.getMaterialType().name() : null);
        map.put("materialTypeDisplayName", material.getMaterialType() != null ? material.getMaterialType().getDisplayName() : null);
        map.put("materialName", material.getMaterialName());
        map.put("baseValueCredits", material.getBaseValueCredits());
        map.put("rawInputsJson", material.getRawInputsJson());
        map.put("refinedInputsJson", material.getRefinedInputsJson());
        map.put("processingTimeTurns", material.getProcessingTimeTurns());
        map.put("alternativeRecipeJson", material.getAlternativeRecipeJson());
        map.put("alternativeTimeTurns", material.getAlternativeTimeTurns());
        map.put("createdAt", material.getCreatedAt());
        map.put("updatedAt", material.getUpdatedAt());
        return map;
    }
}
