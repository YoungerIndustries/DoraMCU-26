package com.yisy.modules.doramcu.hex;

import com.yisy.modules.doramcu.hex.model.InfrastructureItem;
import com.yisy.modules.doramcu.hex.model.MaterialCost;
import com.yisy.modules.doramcu.hex.model.MaterialCost.MaterialCostType;
import com.yisy.modules.doramcu.hex.repository.InfrastructureItemRepository;
import com.yisy.modules.doramcu.hex.repository.MaterialCostRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Provides lookups for material costs, recipes, and cost calculations
 * in the economic production system.
 * <p>
 * This service covers raw resources, refined resources, and construction modules.
 * It supports standard and alternative recipes, and can calculate total material
 * costs by recursively resolving all upstream dependencies.
 * </p>
 */
@Service
public class MaterialCostService {

    private static final Logger log = LoggerFactory.getLogger(MaterialCostService.class);

    /** Maximum recursion depth for cost calculation to prevent infinite loops. */
    private static final int MAX_RECURSION_DEPTH = 10;

    private final MaterialCostRepository materialCostRepository;
    private final InfrastructureItemRepository infrastructureItemRepository;

    public MaterialCostService(MaterialCostRepository materialCostRepository,
                                InfrastructureItemRepository infrastructureItemRepository) {
        this.materialCostRepository = materialCostRepository;
        this.infrastructureItemRepository = infrastructureItemRepository;
    }

    // ------------------------------------------------------------------
    // Material lookups
    // ------------------------------------------------------------------

    /**
     * Look up a material cost by name.
     *
     * @param materialName the name of the material (e.g., "ORE", "METAL_ALLOY", "HULL_MODULE")
     * @return optional material cost
     */
    public Optional<MaterialCost> getMaterialCost(String materialName) {
        return materialCostRepository.findByMaterialName(materialName);
    }

    /**
     * Get all raw resources.
     *
     * @return list of raw resource materials
     */
    public List<MaterialCost> getAllRawResources() {
        return materialCostRepository.findByMaterialType(MaterialCostType.RAW_RESOURCE);
    }

    /**
     * Get all refined resources.
     *
     * @return list of refined resource materials
     */
    public List<MaterialCost> getAllRefinedResources() {
        return materialCostRepository.findByMaterialType(MaterialCostType.REFINED_RESOURCE);
    }

    /**
     * Get all construction modules.
     *
     * @return list of construction module materials
     */
    public List<MaterialCost> getAllConstructionModules() {
        return materialCostRepository.findByMaterialType(MaterialCostType.CONSTRUCTION_MODULE);
    }

    /**
     * Get all materials of a given type, sorted by base credit value descending.
     *
     * @param materialType the material type
     * @return list of materials sorted by value
     */
    public List<MaterialCost> getAllByTypeSortedByValue(MaterialCostType materialType) {
        return materialCostRepository.findByMaterialTypeOrderByBaseValueCreditsDesc(materialType);
    }

    // ------------------------------------------------------------------
    // Recipe retrieval
    // ------------------------------------------------------------------

    /**
     * Get the standard recipe for a material.
     * Returns the raw inputs and refined inputs JSON strings along with processing time.
     *
     * @param materialName the name of the material
     * @return optional material cost with recipe data
     */
    public Optional<MaterialCost> getRecipe(String materialName) {
        return materialCostRepository.findByMaterialName(materialName);
    }

    /**
     * Get the alternative recipe for a material (used when primary inputs are scarce).
     *
     * @param materialName the name of the material
     * @return optional material cost; check getAlternativeRecipeJson() for the alternative
     */
    public Optional<MaterialCost> getAlternativeRecipe(String materialName) {
        return materialCostRepository.findByMaterialName(materialName)
                .filter(mc -> mc.getAlternativeRecipeJson() != null);
    }

    // ------------------------------------------------------------------
    // Cost calculation
    // ------------------------------------------------------------------

    /**
     * Calculate the total material cost for a given material by recursively
     * resolving all upstream dependencies.
     * <p>
     * For raw resources, returns the base value.
     * For refined resources, calculates the cost of raw inputs plus a processing markup.
     * For construction modules, calculates the cost of refined inputs plus an assembly markup.
     * </p>
     *
     * @param materialName the name of the material
     * @return total material cost in credits, or null if material not found
     */
    public BigDecimal calculateTotalMaterialCost(String materialName) {
        return calculateTotalMaterialCost(materialName, 0);
    }

    private BigDecimal calculateTotalMaterialCost(String materialName, int depth) {
        if (depth > MAX_RECURSION_DEPTH) {
            log.warn("Max recursion depth reached for material cost calculation: {}", materialName);
            return BigDecimal.ZERO;
        }

        Optional<MaterialCost> opt = materialCostRepository.findByMaterialName(materialName);
        if (opt.isEmpty()) {
            log.warn("Material not found: {}", materialName);
            return null;
        }

        MaterialCost material = opt.get();

        // Raw resources have no upstream dependencies
        if (material.getMaterialType() == MaterialCostType.RAW_RESOURCE) {
            return material.getBaseValueCredits();
        }

        BigDecimal totalCost = BigDecimal.ZERO;

        // Calculate cost from raw inputs
        if (material.getRawInputsJson() != null && !material.getRawInputsJson().isEmpty()) {
            totalCost = totalCost.add(calculateRawInputCost(material.getRawInputsJson(), depth + 1));
        }

        // Calculate cost from refined inputs
        if (material.getRefinedInputsJson() != null && !material.getRefinedInputsJson().isEmpty()) {
            totalCost = totalCost.add(calculateRefinedInputCost(material.getRefinedInputsJson(), depth + 1));
        }

        // Apply markup based on material type
        if (material.getMaterialType() == MaterialCostType.REFINED_RESOURCE) {
            // 1.3x processing markup for refined resources
            totalCost = totalCost.multiply(new BigDecimal("1.3"));
        } else if (material.getMaterialType() == MaterialCostType.CONSTRUCTION_MODULE) {
            // 1.5x assembly markup for construction modules
            totalCost = totalCost.multiply(new BigDecimal("1.5"));
        }

        log.debug("Total material cost for {}: {} credits", materialName, totalCost);
        return totalCost;
    }

    /**
     * Calculate the cost of raw inputs from a JSON string.
     * Parses the JSON format: {"ORE": 2, "CRYSTAL": 1}
     *
     * @param rawInputsJson the raw inputs JSON string
     * @param depth current recursion depth
     * @return total cost of raw inputs
     */
    private BigDecimal calculateRawInputCost(String rawInputsJson, int depth) {
        BigDecimal total = BigDecimal.ZERO;
        // Simple JSON parsing for {"NAME": qty, ...} format
        Map<String, Integer> inputs = parseJsonQuantityMap(rawInputsJson);
        for (var entry : inputs.entrySet()) {
            BigDecimal unitCost = calculateTotalMaterialCost(entry.getKey(), depth);
            if (unitCost != null) {
                total = total.add(unitCost.multiply(BigDecimal.valueOf(entry.getValue())));
            }
        }
        return total;
    }

    /**
     * Calculate the cost of refined inputs from a JSON string.
     * Parses the JSON format: {"METAL_ALLOY": 2, "COMPOSITE": 1}
     *
     * @param refinedInputsJson the refined inputs JSON string
     * @param depth current recursion depth
     * @return total cost of refined inputs
     */
    private BigDecimal calculateRefinedInputCost(String refinedInputsJson, int depth) {
        BigDecimal total = BigDecimal.ZERO;
        Map<String, Integer> inputs = parseJsonQuantityMap(refinedInputsJson);
        for (var entry : inputs.entrySet()) {
            BigDecimal unitCost = calculateTotalMaterialCost(entry.getKey(), depth);
            if (unitCost != null) {
                total = total.add(unitCost.multiply(BigDecimal.valueOf(entry.getValue())));
            }
        }
        return total;
    }

    /**
     * Look up an infrastructure item by ID.
     *
     * @param itemId the infrastructure item ID
     * @return optional infrastructure item
     */
    public Optional<InfrastructureItem> getInfrastructureItem(Long itemId) {
        return infrastructureItemRepository.findById(itemId);
    }

    /**
     * Parse a simple JSON object into a map of name to quantity.
     * Expected format: {"NAME": qty, "NAME2": qty2}
     *
     * @param json the JSON string
     * @return map of material names to quantities
     */
    public Map<String, Integer> parseJsonQuantityMap(String json) {
        Map<String, Integer> map = new HashMap<>();
        if (json == null || json.isEmpty()) {
            return map;
        }

        // Remove braces and whitespace
        String content = json.trim();
        if (content.startsWith("{")) content = content.substring(1);
        if (content.endsWith("}")) content = content.substring(0, content.length() - 1);

        // Split by comma
        String[] pairs = content.split(",");
        for (String pair : pairs) {
            pair = pair.trim();
            // Split by colon
            int colonIdx = pair.indexOf(':');
            if (colonIdx > 0) {
                String key = pair.substring(0, colonIdx).trim().replace("\"", "").replace("'", "");
                String valueStr = pair.substring(colonIdx + 1).trim().replace("\"", "").replace("'", "");
                try {
                    int value = Integer.parseInt(valueStr);
                    map.put(key, value);
                } catch (NumberFormatException e) {
                    log.warn("Failed to parse quantity for {}: {}", key, valueStr);
                }
            }
        }
        return map;
    }
}
