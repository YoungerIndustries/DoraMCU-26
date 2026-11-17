package com.yisy.modules.doramcu.controller.economic;

import com.yisy.modules.doramcu.eve.model.AiCharacter;
import com.yisy.modules.doramcu.hex.CostCalculatorService;
import com.yisy.modules.doramcu.hex.MaterialCostService;
import com.yisy.modules.doramcu.repository.ai.AiCharacterRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * REST controller for cost calculation and reporting.
 * <p>
 * Provides endpoints for calculating material costs, labor costs (with leadership
 * modifiers), and total costs for individual contracts, production chains, and
 * infrastructure items.
 * </p>
 * <p>
 * Endpoints:
 * <ul>
 *   <li>{@code GET /api/v1/economic/{galaxyId}/costs/contract/{contractId}} — Contract cost report</li>
 *   <li>{@code GET /api/v1/economic/{galaxyId}/costs/chain/{chainId}} — Chain cost report</li>
 *   <li>{@code GET /api/v1/economic/{galaxyId}/costs/material/{materialName}} — Material cost lookup</li>
 *   <li>{@code POST /api/v1/economic/{galaxyId}/costs/estimate} — Estimate infrastructure item cost</li>
 * </ul>
 * </p>
 */
@RestController
@RequestMapping("/api/v1/economic")
public class CostReportController {

    private static final Logger log = LoggerFactory.getLogger(CostReportController.class);

    private final CostCalculatorService costCalculatorService;
    private final MaterialCostService materialCostService;
    private final AiCharacterRepository aiCharacterRepository;

    public CostReportController(CostCalculatorService costCalculatorService,
                                 MaterialCostService materialCostService,
                                 AiCharacterRepository aiCharacterRepository) {
        this.costCalculatorService = costCalculatorService;
        this.materialCostService = materialCostService;
        this.aiCharacterRepository = aiCharacterRepository;
    }

    // ------------------------------------------------------------------
    // Contract cost report
    // ------------------------------------------------------------------

    /**
     * Get cost breakdown for a single build contract.
     */
    @GetMapping("/{galaxyId}/costs/contract/{contractId}")
    public ResponseEntity<?> getContractCostReport(
            @PathVariable Long galaxyId,
            @PathVariable Long contractId) {

        return costCalculatorService.calculateContractCostReport(contractId)
                .map(report -> {
                    Map<String, Object> result = new HashMap<>();
                    result.put("galaxyId", galaxyId);
                    result.put("report", report);
                    return ResponseEntity.ok(result);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // ------------------------------------------------------------------
    // Chain cost report
    // ------------------------------------------------------------------

    /**
     * Get aggregate cost report for a production chain.
     */
    @GetMapping("/{galaxyId}/costs/chain/{chainId}")
    public ResponseEntity<?> getChainCostReport(
            @PathVariable Long galaxyId,
            @PathVariable Long chainId) {

        return costCalculatorService.calculateChainCostReport(chainId)
                .map(report -> {
                    Map<String, Object> result = new HashMap<>();
                    result.put("galaxyId", galaxyId);
                    result.put("report", report);
                    return ResponseEntity.ok(result);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // ------------------------------------------------------------------
    // Material cost lookup
    // ------------------------------------------------------------------

    /**
     * Look up the total material cost for a given material name.
     */
    @GetMapping("/{galaxyId}/costs/material/{materialName}")
    public ResponseEntity<Map<String, Object>> getMaterialCost(
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
    // Infrastructure item cost estimate
    // ------------------------------------------------------------------

    /**
     * Estimate the total cost for building an infrastructure item.
     */
    @PostMapping("/{galaxyId}/costs/estimate")
    public ResponseEntity<?> estimateInfrastructureItemCost(
            @PathVariable Long galaxyId,
            @RequestBody Map<String, Object> request) {

        Long targetItemId = Long.valueOf(request.get("targetItemId").toString());
        Long leaderId = request.containsKey("leaderId")
                ? Long.valueOf(request.get("leaderId").toString())
                : null;

        return materialCostService.getInfrastructureItem(targetItemId)
                .map(item -> {
                    AiCharacter leader = null;
                    if (leaderId != null) {
                        leader = aiCharacterRepository.findById(leaderId).orElse(null);
                    }
                    Map<String, Object> costReport = costCalculatorService.calculateInfrastructureItemCost(item, leader);

                    Map<String, Object> result = new HashMap<>();
                    result.put("galaxyId", galaxyId);
                    result.put("targetItemId", targetItemId);
                    result.put("itemName", item.getItemName());
                    result.put("itemType", item.getItemType() != null ? item.getItemType().name() : null);
                    result.put("costReport", costReport);
                    return ResponseEntity.ok(result);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // ------------------------------------------------------------------
    // Labor cost calculator
    // ------------------------------------------------------------------

    /**
     * Calculate labor cost with leadership modifier.
     */
    @PostMapping("/{galaxyId}/costs/labor")
    public ResponseEntity<Map<String, Object>> calculateLaborCost(
            @PathVariable Long galaxyId,
            @RequestBody Map<String, Object> request) {

        int crewSize = Integer.parseInt(request.get("crewSize").toString());
        long dailySalary = Long.parseLong(request.get("dailySalary").toString());
        int durationTurns = Integer.parseInt(request.get("durationTurns").toString());
        int leadershipRating = Integer.parseInt(request.get("leadershipRating").toString());

        BigDecimal laborCost = costCalculatorService.calculateLaborCost(
                crewSize, dailySalary, durationTurns, leadershipRating);
        double modifier = costCalculatorService.calculateLeadershipModifier(leadershipRating);

        Map<String, Object> result = new HashMap<>();
        result.put("galaxyId", galaxyId);
        result.put("crewSize", crewSize);
        result.put("dailySalary", dailySalary);
        result.put("durationTurns", durationTurns);
        result.put("leadershipRating", leadershipRating);
        result.put("leadershipModifier", modifier);
        result.put("laborCost", laborCost);
        return ResponseEntity.ok(result);
    }
}
