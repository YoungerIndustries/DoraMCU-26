package com.yisy.modules.doramcu.controller.economic;

import com.yisy.modules.doramcu.hex.BuildContractService;
import com.yisy.modules.doramcu.hex.model.BuildContract;
import com.yisy.modules.doramcu.hex.model.BuildContract.BuildContractStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * REST controller for build contract CRUD operations.
 * <p>
 * Provides endpoints for creating, assigning, tracking, completing, and failing
 * build contracts in the economic production system.
 * </p>
 * <p>
 * Endpoints:
 * <ul>
 *   <li>{@code GET /api/v1/economic/{galaxyId}/contracts} — List all contracts for a galaxy</li>
 *   <li>{@code GET /api/v1/economic/{galaxyId}/contracts/pending} — Pending contracts by priority</li>
 *   <li>{@code GET /api/v1/economic/{galaxyId}/contracts/{contractId}} — Single contract details</li>
 *   <li>{@code POST /api/v1/economic/{galaxyId}/contracts} — Create new contract</li>
 *   <li>{@code POST /api/v1/economic/{galaxyId}/contracts/{contractId}/assign} — Assign crew</li>
 *   <li>{@code POST /api/v1/economic/{galaxyId}/contracts/{contractId}/start} — Start contract</li>
 *   <li>{@code PATCH /api/v1/economic/{galaxyId}/contracts/{contractId}/progress} — Update progress</li>
 *   <li>{@code POST /api/v1/economic/{galaxyId}/contracts/{contractId}/complete} — Complete contract</li>
 *   <li>{@code POST /api/v1/economic/{galaxyId}/contracts/{contractId}/fail} — Fail contract</li>
 * </ul>
 * </p>
 */
@RestController
@RequestMapping("/api/v1/economic")
public class BuildContractController {

    private static final Logger log = LoggerFactory.getLogger(BuildContractController.class);

    private final BuildContractService buildContractService;

    public BuildContractController(BuildContractService buildContractService) {
        this.buildContractService = buildContractService;
    }

    // ------------------------------------------------------------------
    // List endpoints
    // ------------------------------------------------------------------

    /**
     * List all contracts for a galaxy.
     */
    @GetMapping("/{galaxyId}/contracts")
    public ResponseEntity<Map<String, Object>> listContracts(
            @PathVariable Long galaxyId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String type) {

        List<BuildContract> contracts;

        if (status != null && type != null) {
            BuildContract.ContractType contractType = BuildContract.ContractType.valueOf(type);
            BuildContractStatus contractStatus = BuildContractStatus.valueOf(status);
            contracts = buildContractService.getContractsByTypeAndStatus(contractType, contractStatus);
        } else if (status != null) {
            BuildContractStatus contractStatus = BuildContractStatus.valueOf(status);
            contracts = buildContractService.getContractsByStatus(contractStatus);
        } else if (type != null) {
            BuildContract.ContractType contractType = BuildContract.ContractType.valueOf(type);
            contracts = buildContractService.getContractsByType(contractType);
        } else {
            contracts = buildContractService.getContractsByStatus(BuildContractStatus.PENDING);
            // Fallback: get all pending for this galaxy
            contracts = buildContractService.getPendingContractsByPriority(galaxyId);
        }

        // Filter by galaxy
        List<Map<String, Object>> items = contracts.stream()
                .filter(c -> galaxyId == null || galaxyId.equals(c.getGalaxyId()))
                .map(this::toMap)
                .collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("galaxyId", galaxyId);
        result.put("count", items.size());
        result.put("contracts", items);
        return ResponseEntity.ok(result);
    }

    /**
     * Get pending contracts sorted by priority.
     */
    @GetMapping("/{galaxyId}/contracts/pending")
    public ResponseEntity<Map<String, Object>> getPendingContracts(@PathVariable Long galaxyId) {
        List<BuildContract> contracts = buildContractService.getPendingContractsByPriority(galaxyId);

        List<Map<String, Object>> items = contracts.stream()
                .map(this::toMap)
                .collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("galaxyId", galaxyId);
        result.put("count", items.size());
        result.put("contracts", items);
        return ResponseEntity.ok(result);
    }

    /**
     * Get a single contract by ID.
     */
    @GetMapping("/{galaxyId}/contracts/{contractId}")
    public ResponseEntity<?> getContract(
            @PathVariable Long galaxyId,
            @PathVariable Long contractId) {

        return buildContractService.getContract(contractId)
                .map(contract -> {
                    if (galaxyId != null && !galaxyId.equals(contract.getGalaxyId())) {
                        return ResponseEntity.notFound().build();
                    }
                    Map<String, Object> result = new HashMap<>();
                    result.put("galaxyId", galaxyId);
                    result.put("contract", toMap(contract));
                    return ResponseEntity.ok(result);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // ------------------------------------------------------------------
    // Create endpoint
    // ------------------------------------------------------------------

    /**
     * Create a new build contract.
     */
    @PostMapping("/{galaxyId}/contracts")
    public ResponseEntity<Map<String, Object>> createContract(
            @PathVariable Long galaxyId,
            @RequestBody Map<String, Object> request) {

        String contractTypeName = (String) request.get("contractType");
        String targetItemType = (String) request.get("targetItemType");
        String targetItemName = (String) request.get("targetItemName");
        String materialRequirementsJson = (String) request.get("materialRequirementsJson");
        Integer estimatedDurationTurns = (Integer) request.get("estimatedDurationTurns");

        if (contractTypeName == null || targetItemName == null) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "contractType and targetItemName are required");
            return ResponseEntity.badRequest().body(error);
        }

        BuildContract.ContractType contractType = BuildContract.ContractType.valueOf(contractTypeName);
        int duration = estimatedDurationTurns != null ? estimatedDurationTurns : 1;

        BuildContract contract = buildContractService.createContract(
                contractType,
                targetItemType != null ? targetItemType : "UNKNOWN",
                targetItemName,
                materialRequirementsJson != null ? materialRequirementsJson : "{}",
                duration,
                galaxyId
        );

        Map<String, Object> result = new HashMap<>();
        result.put("galaxyId", galaxyId);
        result.put("contract", toMap(contract));
        result.put("message", "Contract created successfully");
        return ResponseEntity.ok(result);
    }

    // ------------------------------------------------------------------
    // Assign endpoint
    // ------------------------------------------------------------------

    /**
     * Assign a crew group to a pending contract.
     */
    @PostMapping("/{galaxyId}/contracts/{contractId}/assign")
    public ResponseEntity<?> assignContract(
            @PathVariable Long galaxyId,
            @PathVariable Long contractId,
            @RequestBody Map<String, Object> request) {

        Long crewId = Long.valueOf(request.get("crewId").toString());
        Long leaderId = Long.valueOf(request.get("leaderId").toString());

        @SuppressWarnings("unchecked")
        List<Long> memberIds = request.containsKey("memberIds")
                ? (List<Long>) request.get("memberIds")
                : null;

        try {
            BuildContract contract = buildContractService.assignContract(contractId, crewId, leaderId, memberIds);

            Map<String, Object> result = new HashMap<>();
            result.put("galaxyId", galaxyId);
            result.put("contract", toMap(contract));
            result.put("message", "Contract assigned successfully");
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException | IllegalStateException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // ------------------------------------------------------------------
    // Start endpoint
    // ------------------------------------------------------------------

    /**
     * Start a contract (transition from ASSIGNED to IN_PROGRESS).
     */
    @PostMapping("/{galaxyId}/contracts/{contractId}/start")
    public ResponseEntity<?> startContract(
            @PathVariable Long galaxyId,
            @PathVariable Long contractId) {

        try {
            BuildContract contract = buildContractService.startContract(contractId);

            Map<String, Object> result = new HashMap<>();
            result.put("galaxyId", galaxyId);
            result.put("contract", toMap(contract));
            result.put("message", "Contract started");
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException | IllegalStateException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // ------------------------------------------------------------------
    // Progress endpoint
    // ------------------------------------------------------------------

    /**
     * Update the progress of an in-progress contract.
     */
    @PatchMapping("/{galaxyId}/contracts/{contractId}/progress")
    public ResponseEntity<?> updateProgress(
            @PathVariable Long galaxyId,
            @PathVariable Long contractId,
            @RequestBody Map<String, Object> request) {

        int progressPercent = Integer.parseInt(request.get("progressPercent").toString());

        try {
            BuildContract contract = buildContractService.updateProgress(contractId, progressPercent);

            Map<String, Object> result = new HashMap<>();
            result.put("galaxyId", galaxyId);
            result.put("contract", toMap(contract));
            result.put("message", "Progress updated to " + progressPercent + "%");
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException | IllegalStateException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // ------------------------------------------------------------------
    // Complete endpoint
    // ------------------------------------------------------------------

    /**
     * Complete a contract and award XP.
     */
    @PostMapping("/{galaxyId}/contracts/{contractId}/complete")
    public ResponseEntity<?> completeContract(
            @PathVariable Long galaxyId,
            @PathVariable Long contractId) {

        try {
            BuildContract contract = buildContractService.completeContract(contractId);

            Map<String, Object> result = new HashMap<>();
            result.put("galaxyId", galaxyId);
            result.put("contract", toMap(contract));
            result.put("message", "Contract completed successfully");
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException | IllegalStateException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // ------------------------------------------------------------------
    // Fail endpoint
    // ------------------------------------------------------------------

    /**
     * Mark a contract as failed.
     */
    @PostMapping("/{galaxyId}/contracts/{contractId}/fail")
    public ResponseEntity<?> failContract(
            @PathVariable Long galaxyId,
            @PathVariable Long contractId,
            @RequestBody Map<String, Object> request) {

        String reason = (String) request.get("reason");

        try {
            BuildContract contract = buildContractService.failContract(contractId, reason);

            Map<String, Object> result = new HashMap<>();
            result.put("galaxyId", galaxyId);
            result.put("contract", toMap(contract));
            result.put("message", "Contract marked as failed");
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException | IllegalStateException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    /**
     * Convert a BuildContract entity to a Map for JSON serialization.
     */
    private Map<String, Object> toMap(BuildContract contract) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", contract.getId());
        map.put("contractType", contract.getContractType() != null ? contract.getContractType().name() : null);
        map.put("contractTypeDisplayName", contract.getContractType() != null ? contract.getContractType().getDisplayName() : null);
        map.put("targetItemType", contract.getTargetItemType());
        map.put("targetItemName", contract.getTargetItemName());
        map.put("description", contract.getDescription());
        map.put("materialRequirementsJson", contract.getMaterialRequirementsJson());
        map.put("leaderRoleRequired", contract.getLeaderRoleRequired());
        map.put("leaderMinLeadership", contract.getLeaderMinLeadership());
        map.put("crewSize", contract.getCrewSize());
        map.put("crewSkillsRequiredJson", contract.getCrewSkillsRequiredJson());
        map.put("estimatedDurationTurns", contract.getEstimatedDurationTurns());
        map.put("priority", contract.getPriority());
        map.put("status", contract.getStatus() != null ? contract.getStatus().name() : null);
        map.put("statusDisplayName", contract.getStatus() != null ? contract.getStatus().getDisplayName() : null);
        map.put("assignedCrewId", contract.getAssignedCrewId());
        map.put("assignedLeaderId", contract.getAssignedLeaderId());
        map.put("crewMember1Id", contract.getCrewMember1Id());
        map.put("crewMember2Id", contract.getCrewMember2Id());
        map.put("crewMember3Id", contract.getCrewMember3Id());
        map.put("progressPercent", contract.getProgressPercent());
        map.put("materialCostTotalCredits", contract.getMaterialCostTotalCredits());
        map.put("laborCostTotalCredits", contract.getLaborCostTotalCredits());
        map.put("dependenciesJson", contract.getDependenciesJson());
        map.put("productionChainId", contract.getProductionChainId());
        map.put("startTurn", contract.getStartTurn());
        map.put("completionTurn", contract.getCompletionTurn());
        map.put("galaxyId", contract.getGalaxyId());
        map.put("planetId", contract.getPlanetId());
        map.put("stationId", contract.getStationId());
        map.put("createdAt", contract.getCreatedAt());
        map.put("completedAt", contract.getCompletedAt());
        map.put("updatedAt", contract.getUpdatedAt());
        return map;
    }
}
