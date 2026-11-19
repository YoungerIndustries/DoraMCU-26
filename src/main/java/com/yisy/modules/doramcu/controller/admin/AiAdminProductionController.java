package com.yisy.modules.doramcu.controller.admin;

import com.yisy.modules.doramcu.admin.model.AiAdminOrder;
import com.yisy.modules.doramcu.admin.service.AiAdminProductionService;
import com.yisy.modules.doramcu.hex.model.BuildContract;
import com.yisy.modules.doramcu.hex.model.InfrastructureItem;
import com.yisy.modules.doramcu.hex.repository.InfrastructureItemRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * REST controller for AI admin production management.
 * <p>
 * Exposes endpoints for creating production orders, managing production chains,
 * assigning crew to production orders, and viewing production summaries.
 * </p>
 */
@RestController
@RequestMapping("/api/v1/admin/production")
public class AiAdminProductionController {

    private static final Logger log = LoggerFactory.getLogger(AiAdminProductionController.class);

    private final AiAdminProductionService productionService;
    private final InfrastructureItemRepository infrastructureItemRepository;

    public AiAdminProductionController(AiAdminProductionService productionService,
                                        InfrastructureItemRepository infrastructureItemRepository) {
        this.productionService = productionService;
        this.infrastructureItemRepository = infrastructureItemRepository;
    }

    /**
     * Create a production order from an infrastructure item.
     * Creates both the build contract and the AI admin order.
     *
     * POST /{galaxyId}/admin/production/contract
     */
    @PostMapping("/{galaxyId}/contract")
    public ResponseEntity<?> createContractOrder(
            @PathVariable Long galaxyId,
            @RequestBody Map<String, Object> request) {
        try {
            String itemName = (String) request.get("itemName");
            String submittedBy = (String) request.get("submittedBy");

            if (itemName == null || submittedBy == null) {
                return ResponseEntity.badRequest().body(
                        Map.of("error", "itemName and submittedBy are required"));
            }

            InfrastructureItem item = infrastructureItemRepository
                    .findByItemTypeAndItemName(null, itemName)
                    .orElseThrow(() -> new IllegalArgumentException("Infrastructure item not found: " + itemName));

            AiAdminOrder order = productionService.createProductionOrder(item, galaxyId, submittedBy);

            Map<String, Object> result = new HashMap<>();
            result.put("message", "Production order created");
            result.put("orderId", order.getId());
            result.put("buildContractId", order.getBuildContractId());
            result.put("orderText", order.getOrderText());
            result.put("status", order.getStatus().name());

            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error creating production order", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Create production chain orders for an infrastructure item.
     * Creates a full production chain and AI admin orders for all contracts.
     *
     * POST /{galaxyId}/admin/production/chain
     */
    @PostMapping("/{galaxyId}/chain")
    public ResponseEntity<?> createChainOrders(
            @PathVariable Long galaxyId,
            @RequestBody Map<String, Object> request) {
        try {
            String itemName = (String) request.get("itemName");
            String submittedBy = (String) request.get("submittedBy");

            if (itemName == null || submittedBy == null) {
                return ResponseEntity.badRequest().body(
                        Map.of("error", "itemName and submittedBy are required"));
            }

            InfrastructureItem item = infrastructureItemRepository
                    .findByItemTypeAndItemName(null, itemName)
                    .orElseThrow(() -> new IllegalArgumentException("Infrastructure item not found: " + itemName));

            List<AiAdminOrder> orders = productionService.createChainProductionOrders(item, galaxyId, submittedBy);

            Map<String, Object> result = new HashMap<>();
            result.put("message", "Production chain orders created");
            result.put("orderCount", orders.size());
            result.put("orders", orders.stream().map(order -> {
                Map<String, Object> orderInfo = new HashMap<>();
                orderInfo.put("orderId", order.getId());
                orderInfo.put("buildContractId", order.getBuildContractId());
                orderInfo.put("productionChainId", order.getProductionChainId());
                orderInfo.put("orderText", order.getOrderText());
                orderInfo.put("status", order.getStatus().name());
                return orderInfo;
            }).collect(Collectors.toList()));

            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error creating chain production orders", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * List all production orders for a galaxy.
     *
     * GET /{galaxyId}/admin/production/orders
     */
    @GetMapping("/{galaxyId}/orders")
    public ResponseEntity<?> listOrders(@PathVariable Long galaxyId) {
        try {
            List<AiAdminOrder> orders = productionService.getProductionOrders(galaxyId);

            Map<String, Object> result = new HashMap<>();
            result.put("galaxyId", galaxyId);
            result.put("orderCount", orders.size());
            result.put("orders", orders.stream().map(order -> {
                Map<String, Object> orderInfo = new HashMap<>();
                orderInfo.put("orderId", order.getId());
                orderInfo.put("orderText", order.getOrderText());
                orderInfo.put("status", order.getStatus().name());
                orderInfo.put("progressPercent", order.getProgressPercent());
                orderInfo.put("submittedBy", order.getSubmittedBy());
                orderInfo.put("buildContractId", order.getBuildContractId());
                orderInfo.put("productionChainId", order.getProductionChainId());
                orderInfo.put("isChainOrder", order.isChainOrder());
                return orderInfo;
            }).collect(Collectors.toList()));

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error listing production orders", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get detailed summary of a production order.
     *
     * GET /{galaxyId}/admin/production/orders/{orderId}
     */
    @GetMapping("/{galaxyId}/orders/{orderId}")
    public ResponseEntity<?> getOrderDetails(
            @PathVariable Long galaxyId,
            @PathVariable Long orderId) {
        try {
            return productionService.getProductionOrderSummary(orderId)
                    .map(summary -> ResponseEntity.ok(summary))
                    .orElseGet(() -> ResponseEntity.notFound().build());
        } catch (Exception e) {
            log.error("Error getting production order details", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Assign crew to a production order's build contract.
     *
     * POST /{galaxyId}/admin/production/orders/{orderId}/assign-crew
     */
    @PostMapping("/{galaxyId}/orders/{orderId}/assign-crew")
    public ResponseEntity<?> assignCrew(
            @PathVariable Long galaxyId,
            @PathVariable Long orderId,
            @RequestBody Map<String, Object> request) {
        try {
            Long leaderId = ((Number) request.get("leaderId")).longValue();
            List<Long> memberIds = request.get("memberIds") != null
                    ? (List<Long>) request.get("memberIds")
                    : List.of();

            BuildContract contract = productionService.assignCrewToProductionOrder(
                    orderId, leaderId, memberIds);

            Map<String, Object> result = new HashMap<>();
            result.put("message", "Crew assigned to production order");
            result.put("orderId", orderId);
            result.put("contractId", contract.getId());
            result.put("status", contract.getStatus().name());
            result.put("targetItemName", contract.getTargetItemName());
            result.put("laborCostCredits", contract.getLaborCostTotalCredits());

            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error assigning crew to production order", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get aggregate production summary for all orders in a galaxy.
     *
     * GET /{galaxyId}/admin/production/summary
     */
    @GetMapping("/{galaxyId}/summary")
    public ResponseEntity<?> productionSummary(@PathVariable Long galaxyId) {
        try {
            Map<String, Object> summary = productionService.getProductionSummary(galaxyId);
            return ResponseEntity.ok(summary);
        } catch (Exception e) {
            log.error("Error getting production summary", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
