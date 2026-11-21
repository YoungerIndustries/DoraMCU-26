package com.yisy.modules.doramcu.controller.economic;
import com.yisy.modules.doramcu.hex.model.ProductionChainTracking.ProductionChainStatus;

import com.yisy.modules.doramcu.hex.ProductionChainService;
import com.yisy.modules.doramcu.hex.model.BuildContract;
import com.yisy.modules.doramcu.hex.model.InfrastructureItem;
import com.yisy.modules.doramcu.hex.model.ProductionChainTracking;
import com.yisy.modules.doramcu.hex.repository.ProductionChainTrackingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * REST controller for production chain management.
 * <p>
 * Provides endpoints for creating, tracking, and managing production chains
 * that span from raw material extraction to final construction.
 * </p>
 * <p>
 * Endpoints:
 * <ul>
 *   <li>{@code GET /api/v1/economic/{galaxyId}/chains} — List all chains</li>
 *   <li>{@code GET /api/v1/economic/{galaxyId}/chains/{chainId}} — Single chain details</li>
 *   <li>{@code POST /api/v1/economic/{galaxyId}/chains} — Create production chain</li>
 *   <li>{@code POST /api/v1/economic/{galaxyId}/chains/{chainId}/generate-contracts} — Generate contracts</li>
 *   <li>{@code GET /api/v1/economic/{galaxyId}/chains/{chainId}/ready-contracts} — Ready contracts</li>
 *   <li>{@code POST /api/v1/economic/{galaxyId}/chains/{chainId}/complete} — Complete chain</li>
 *   <li>{@code POST /api/v1/economic/{galaxyId}/chains/{chainId}/fail} — Fail chain</li>
 * </ul>
 * </p>
 */
@RestController
@RequestMapping("/api/v1/economic")
public class ProductionChainController {

    private static final Logger log = LoggerFactory.getLogger(ProductionChainController.class);

    private final ProductionChainService productionChainService;
    private final ProductionChainTrackingRepository chainRepository;

    public ProductionChainController(ProductionChainService productionChainService,
                                      ProductionChainTrackingRepository chainRepository) {
        this.productionChainService = productionChainService;
        this.chainRepository = chainRepository;
    }

    // ------------------------------------------------------------------
    // List endpoints
    // ------------------------------------------------------------------

    /**
     * List all production chains.
     */
    @GetMapping("/{galaxyId}/chains")
    public ResponseEntity<Map<String, Object>> listChains(
            @PathVariable Long galaxyId,
            @RequestParam(required = false) String status) {

        List<ProductionChainTracking> chains;

        if (status != null) {
            ProductionChainStatus chainStatus = ProductionChainStatus.valueOf(status);
            chains = chainRepository.findByStatus(chainStatus);
        } else {
            chains = chainRepository.findAll();
        }

        List<Map<String, Object>> items = chains.stream()
                .map(this::toMap)
                .collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("galaxyId", galaxyId);
        result.put("count", items.size());
        result.put("chains", items);
        return ResponseEntity.ok(result);
    }

    /**
     * Get a single production chain by ID.
     */
    @GetMapping("/{galaxyId}/chains/{chainId}")
    public ResponseEntity<?> getChain(
            @PathVariable Long galaxyId,
            @PathVariable Long chainId) {

        return productionChainService.getChain(chainId)
                .map(chain -> {
                    Map<String, Object> result = new HashMap<>();
                    result.put("galaxyId", galaxyId);
                    result.put("chain", toMap(chain));
                    return ResponseEntity.ok(result);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // ------------------------------------------------------------------
    // Create endpoint
    // ------------------------------------------------------------------

    /**
     * Create a new production chain for an infrastructure item.
     */
    @PostMapping("/{galaxyId}/chains")
    public ResponseEntity<Map<String, Object>> createChain(
            @PathVariable Long galaxyId,
            @RequestBody Map<String, Object> request) {

        Long targetItemId = Long.valueOf(request.get("targetItemId").toString());

        // Look up the infrastructure item
        return productionChainService.getInfrastructureItem(targetItemId)
                .map(item -> {
                    ProductionChainTracking chain = productionChainService.createProductionChain(item, galaxyId);

                    Map<String, Object> result = new HashMap<>();
                    result.put("galaxyId", galaxyId);
                    result.put("chain", toMap(chain));
                    result.put("message", "Production chain created");
                    return ResponseEntity.ok(result);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // ------------------------------------------------------------------
    // Generate contracts endpoint
    // ------------------------------------------------------------------

    /**
     * Auto-generate build contracts from a production chain.
     */
    @PostMapping("/{galaxyId}/chains/{chainId}/generate-contracts")
    public ResponseEntity<?> generateContracts(
            @PathVariable Long galaxyId,
            @PathVariable Long chainId) {

        return productionChainService.getChain(chainId)
                .map(chain -> {
                    ProductionChainTracking updated = productionChainService.generateContracts(chain);

                    Map<String, Object> result = new HashMap<>();
                    result.put("galaxyId", galaxyId);
                    result.put("chain", toMap(updated));
                    result.put("message", "Contracts generated successfully");
                    return ResponseEntity.ok(result);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // ------------------------------------------------------------------
    // Ready contracts endpoint
    // ------------------------------------------------------------------

    /**
     * Get contracts whose dependencies are all met.
     */
    @GetMapping("/{galaxyId}/chains/{chainId}/ready-contracts")
    public ResponseEntity<?> getReadyContracts(
            @PathVariable Long galaxyId,
            @PathVariable Long chainId) {

        return productionChainService.getChain(chainId)
                .map(chain -> {
                    List<BuildContract> ready = productionChainService.getReadyContracts(chainId);

                    List<Map<String, Object>> items = ready.stream()
                            .map(this::contractToMap)
                            .collect(Collectors.toList());

                    Map<String, Object> result = new HashMap<>();
                    result.put("galaxyId", galaxyId);
                    result.put("chainId", chainId);
                    result.put("count", items.size());
                    result.put("readyContracts", items);
                    return ResponseEntity.ok(result);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // ------------------------------------------------------------------
    // Update progress endpoint
    // ------------------------------------------------------------------

    /**
     * Recalculate aggregate progress from child contracts.
     */
    @PostMapping("/{galaxyId}/chains/{chainId}/update-progress")
    public ResponseEntity<?> updateProgress(
            @PathVariable Long galaxyId,
            @PathVariable Long chainId) {

        return productionChainService.getChain(chainId)
                .map(chain -> {
                    ProductionChainTracking updated = productionChainService.updateChainProgress(chainId);

                    Map<String, Object> result = new HashMap<>();
                    result.put("galaxyId", galaxyId);
                    result.put("chain", toMap(updated));
                    result.put("message", "Chain progress updated");
                    return ResponseEntity.ok(result);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // ------------------------------------------------------------------
    // Complete endpoint
    // ------------------------------------------------------------------

    /**
     * Mark the entire chain as completed.
     */
    @PostMapping("/{galaxyId}/chains/{chainId}/complete")
    public ResponseEntity<?> completeChain(
            @PathVariable Long galaxyId,
            @PathVariable Long chainId) {

        return productionChainService.getChain(chainId)
                .map(chain -> {
                    ProductionChainTracking updated = productionChainService.completeChain(chainId);

                    Map<String, Object> result = new HashMap<>();
                    result.put("galaxyId", galaxyId);
                    result.put("chain", toMap(updated));
                    result.put("message", "Production chain completed");
                    return ResponseEntity.ok(result);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // ------------------------------------------------------------------
    // Fail endpoint
    // ------------------------------------------------------------------

    /**
     * Mark the chain as failed.
     */
    @PostMapping("/{galaxyId}/chains/{chainId}/fail")
    public ResponseEntity<?> failChain(
            @PathVariable Long galaxyId,
            @PathVariable Long chainId,
            @RequestBody Map<String, Object> request) {

        String reason = (String) request.get("reason");

        return productionChainService.getChain(chainId)
                .map(chain -> {
                    ProductionChainTracking updated = productionChainService.failChain(chainId, reason);

                    Map<String, Object> result = new HashMap<>();
                    result.put("galaxyId", galaxyId);
                    result.put("chain", toMap(updated));
                    result.put("message", "Production chain failed");
                    return ResponseEntity.ok(result);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    /**
     * Convert a ProductionChainTracking entity to a Map for JSON serialization.
     */
    private Map<String, Object> toMap(ProductionChainTracking chain) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", chain.getId());
        map.put("chainName", chain.getChainName());
        map.put("targetItemId", chain.getTargetItemId());
        map.put("targetItemName", chain.getTargetItemName());
        map.put("targetItemType", chain.getTargetItemType());
        map.put("status", chain.getStatus() != null ? chain.getStatus().name() : null);
        map.put("statusDisplayName", chain.getStatus() != null ? chain.getStatus().getDisplayName() : null);
        map.put("totalContracts", chain.getTotalContracts());
        map.put("completedContracts", chain.getCompletedContracts());
        map.put("failedContracts", chain.getFailedContracts());
        map.put("progressPercent", chain.getProgressPercent());
        map.put("phasesJson", chain.getPhasesJson());
        map.put("totalMaterialCostCredits", chain.getTotalMaterialCostCredits());
        map.put("totalLaborCostCredits", chain.getTotalLaborCostCredits());
        map.put("totalDurationTurns", chain.getTotalDurationTurns());
        map.put("galaxyId", chain.getGalaxyId());
        map.put("createdAt", chain.getCreatedAt());
        map.put("completedAt", chain.getCompletedAt());
        map.put("updatedAt", chain.getUpdatedAt());
        return map;
    }

    /**
     * Convert a BuildContract entity to a simplified Map for JSON serialization.
     */
    private Map<String, Object> contractToMap(BuildContract contract) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", contract.getId());
        map.put("contractType", contract.getContractType() != null ? contract.getContractType().name() : null);
        map.put("targetItemName", contract.getTargetItemName());
        map.put("status", contract.getStatus() != null ? contract.getStatus().name() : null);
        map.put("progressPercent", contract.getProgressPercent());
        map.put("estimatedDurationTurns", contract.getEstimatedDurationTurns());
        map.put("priority", contract.getPriority());
        map.put("materialRequirementsJson", contract.getMaterialRequirementsJson());
        map.put("dependenciesJson", contract.getDependenciesJson());
        return map;
    }
}
