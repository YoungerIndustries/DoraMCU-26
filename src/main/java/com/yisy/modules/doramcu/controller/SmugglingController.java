package com.yisy.modules.doramcu.controller;

import com.yisy.modules.doramcu.traveller.ContrabandCommodity;
import com.yisy.modules.doramcu.traveller.CustomsCheck;
import com.yisy.modules.doramcu.traveller.SmugglingOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * REST controller for smuggling operations from Classic Traveller B07: Merchant Prince.
 * <p>
 * Provides endpoints for contraband types, smuggling operations,
 * and customs checks.
 * </p>
 * <p>
 * All endpoints are under {@code /api/v1/traveller/smuggling}.
 * </p>
 */
@RestController
@RequestMapping("/api/v1/traveller/smuggling")
public class SmugglingController {

    private static final Logger log = LoggerFactory.getLogger(SmugglingController.class);

    private final Map<Integer, SmugglingOperation> operations = new ConcurrentHashMap<>();
    private final AtomicInteger operationIdCounter = new AtomicInteger(1);

    /**
     * Returns all contraband commodity types with risk levels and prices.
     *
     * @return list of all contraband types
     */
    @GetMapping("/contraband")
    public ResponseEntity<List<Map<String, Object>>> getContraband() {
        log.debug("GET /api/v1/traveller/smuggling/contraband");
        List<Map<String, Object>> items = new ArrayList<>();
        for (ContrabandCommodity c : ContrabandCommodity.values()) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("name", c.name());
            map.put("displayName", c.getDisplayName());
            map.put("description", c.getDescription());
            map.put("riskLevel", c.getRiskLevel());
            map.put("basePrice", c.getBasePrice());
            map.put("volumePerUnit", c.getVolumePerUnit());
            map.put("profitMultiplier", c.getProfitMultiplier());
            items.add(map);
        }
        return ResponseEntity.ok(items);
    }

    /**
     * Returns all customs check outcomes.
     *
     * @return list of all possible customs outcomes
     */
    @GetMapping("/customs-outcomes")
    public ResponseEntity<List<Map<String, Object>>> getCustomsOutcomes() {
        log.debug("GET /api/v1/traveller/smuggling/customs-outcomes");
        List<Map<String, Object>> outcomes = new ArrayList<>();
        for (CustomsCheck.Outcome o : CustomsCheck.Outcome.values()) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("name", o.name());
            map.put("displayName", o.getDisplayName());
            map.put("description", o.getDescription());
            outcomes.add(map);
        }
        return ResponseEntity.ok(outcomes);
    }

    /**
     * Returns all smuggler actions available during customs encounter.
     *
     * @return list of all smuggler actions
     */
    @GetMapping("/actions")
    public ResponseEntity<List<Map<String, Object>>> getActions() {
        log.debug("GET /api/v1/traveller/smuggling/actions");
        List<Map<String, Object>> actions = new ArrayList<>();
        for (CustomsCheck.Action a : CustomsCheck.Action.values()) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("name", a.name());
            map.put("displayName", a.getDisplayName());
            map.put("description", a.getDescription());
            actions.add(map);
        }
        return ResponseEntity.ok(actions);
    }

    /**
     * Creates a new smuggling operation.
     *
     * @param request the smuggling operation request
     * @return the created operation
     */
    @PostMapping("/operations")
    public ResponseEntity<Map<String, Object>> createOperation(@RequestBody SmugglingRequest request) {
        log.debug("POST /api/v1/traveller/smuggling/operations: {}", request);

        ContrabandCommodity contraband;
        try {
            contraband = ContrabandCommodity.valueOf(request.getContrabandType().toUpperCase());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }

        int id = operationIdCounter.getAndIncrement();
        char starport = 'E';
        if (request.getDestinationStarport() != null && !request.getDestinationStarport().isEmpty()) {
            starport = request.getDestinationStarport().charAt(0);
        }
        SmugglingOperation operation = new SmugglingOperation(
                id,
                contraband,
                request.getQuantity(),
                request.getSourceWorld(),
                request.getDestinationWorld(),
                request.getDestinationLawLevel(),
                starport
        );
        // Calculate purchase cost and sell revenue
        operation.calculatePurchaseCost();
        operation.calculateSellRevenue();

        operations.put(id, operation);
        return ResponseEntity.ok(toMap(operation));
    }

    /**
     * Gets a smuggling operation by ID.
     *
     * @param id the operation ID
     * @return the operation or 404
     */
    @GetMapping("/operations/{id}")
    public ResponseEntity<Map<String, Object>> getOperation(@PathVariable int id) {
        log.debug("GET /api/v1/traveller/smuggling/operations/{}", id);
        SmugglingOperation operation = operations.get(id);
        if (operation == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(toMap(operation));
    }

    /**
     * Lists all smuggling operations.
     *
     * @return list of all operations
     */
    @GetMapping("/operations")
    public ResponseEntity<List<Map<String, Object>>> listOperations() {
        log.debug("GET /api/v1/traveller/smuggling/operations");
        List<Map<String, Object>> result = new ArrayList<>();
        for (SmugglingOperation op : operations.values()) {
            result.add(toMap(op));
        }
        return ResponseEntity.ok(result);
    }

    /**
     * Advances a smuggling operation to customs check.
     *
     * @param id the operation ID
     * @return the updated operation
     */
    @PostMapping("/operations/{id}/customs-check")
    public ResponseEntity<Map<String, Object>> customsCheck(@PathVariable int id) {
        log.debug("POST /api/v1/traveller/smuggling/operations/{}/customs-check", id);
        SmugglingOperation operation = operations.get(id);
        if (operation == null) {
            return ResponseEntity.notFound().build();
        }
        operation.advanceToCustoms();
        return ResponseEntity.ok(toMap(operation));
    }

    /**
     * Resolves a customs check for a smuggling operation.
     *
     * @param id the operation ID
     * @param request the resolution request
     * @return the updated operation
     */
    @PostMapping("/operations/{id}/resolve")
    public ResponseEntity<Map<String, Object>> resolveCustoms(@PathVariable int id,
                                                              @RequestBody ResolveRequest request) {
        log.debug("POST /api/v1/traveller/smuggling/operations/{}/resolve", id);
        SmugglingOperation operation = operations.get(id);
        if (operation == null) {
            return ResponseEntity.notFound().build();
        }

        CustomsCheck.Action action;
        try {
            action = CustomsCheck.Action.valueOf(request.getAction().toUpperCase());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }

        CustomsCheck.CustomsResult result = operation.resolveCustoms(
                action,
                request.getPilotSkill() != null ? request.getPilotSkill() : 0
        );
        return ResponseEntity.ok(toMap(operation));
    }

    /**
     * Calculates estimated profit for a smuggling route.
     *
     * @param request the profit calculation request
     * @return estimated profit details
     */
    @PostMapping("/estimate-profit")
    public ResponseEntity<Map<String, Object>> estimateProfit(@RequestBody EstimateRequest request) {
        log.debug("POST /api/v1/traveller/smuggling/estimate-profit");

        ContrabandCommodity contraband;
        try {
            contraband = ContrabandCommodity.valueOf(request.getContrabandType().toUpperCase());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }

        int quantity = request.getQuantity() != null ? request.getQuantity() : 1;
        long purchaseCost = contraband.getBasePrice() * quantity;
        double profitMultiplier = contraband.getProfitMultiplier();
        long estimatedRevenue = (long) (purchaseCost * profitMultiplier);
        long estimatedProfit = estimatedRevenue - purchaseCost;

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("contraband", contraband.getDisplayName());
        result.put("quantity", quantity);
        result.put("purchaseCost", purchaseCost);
        result.put("estimatedRevenue", estimatedRevenue);
        result.put("estimatedProfit", estimatedProfit);
        result.put("riskLevel", contraband.getRiskLevel());
        result.put("profitMultiplier", profitMultiplier);
        return ResponseEntity.ok(result);
    }

    // ---- Helper methods ----

    private Map<String, Object> toMap(SmugglingOperation op) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", op.getId());
        map.put("contraband", op.getContraband().getDisplayName());
        map.put("contrabandType", op.getContraband().name());
        map.put("quantity", op.getQuantity());
        map.put("sourceWorld", op.getSourceWorld());
        map.put("destinationWorld", op.getDestinationWorld());
        map.put("destinationLawLevel", op.getDestinationLawLevel());
        map.put("destinationStarport", op.getDestinationStarport());
        map.put("status", op.getStatus().getDisplayName());
        map.put("purchaseCost", op.getPurchaseCost());
        map.put("sellRevenue", op.getSellRevenue());
        map.put("profit", op.getProfit());
        map.put("finePaid", op.getFinePaid());
        map.put("bribePaid", op.getBribePaid());
        map.put("cargoConfiscated", op.isCargoConfiscated());
        map.put("log", op.getLog());
        return map;
    }

    // ---- Request DTOs ----

    public static class SmugglingRequest {
        private String contrabandType;
        private int quantity;
        private String sourceWorld;
        private String destinationWorld;
        private int destinationLawLevel;
        private String destinationStarport;

        public String getContrabandType() { return contrabandType; }
        public void setContrabandType(String contrabandType) { this.contrabandType = contrabandType; }
        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }
        public String getSourceWorld() { return sourceWorld; }
        public void setSourceWorld(String sourceWorld) { this.sourceWorld = sourceWorld; }
        public String getDestinationWorld() { return destinationWorld; }
        public void setDestinationWorld(String destinationWorld) { this.destinationWorld = destinationWorld; }
        public int getDestinationLawLevel() { return destinationLawLevel; }
        public void setDestinationLawLevel(int destinationLawLevel) { this.destinationLawLevel = destinationLawLevel; }
        public String getDestinationStarport() { return destinationStarport; }
        public void setDestinationStarport(String destinationStarport) { this.destinationStarport = destinationStarport; }
    }

    public static class ResolveRequest {
        private String action;
        private Integer pilotSkill;

        public String getAction() { return action; }
        public void setAction(String action) { this.action = action; }
        public Integer getPilotSkill() { return pilotSkill; }
        public void setPilotSkill(Integer pilotSkill) { this.pilotSkill = pilotSkill; }
    }

    public static class EstimateRequest {
        private String contrabandType;
        private Integer quantity;

        public String getContrabandType() { return contrabandType; }
        public void setContrabandType(String contrabandType) { this.contrabandType = contrabandType; }
        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
    }
}
