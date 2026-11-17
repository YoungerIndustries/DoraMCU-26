package com.yisy.modules.doramcu.controller.economic;

import com.yisy.modules.doramcu.eve.model.AiCharacter;
import com.yisy.modules.doramcu.hex.SpaceStationContractService;
import com.yisy.modules.doramcu.hex.SpaceStationContractService.StationModuleContract;
import com.yisy.modules.doramcu.hex.SpaceStationType;
import com.yisy.modules.doramcu.hex.model.BuildContract;
import com.yisy.modules.doramcu.repository.ai.AiCharacterRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * REST controller for space station modularization.
 * <p>
 * Provides endpoints for generating modular build contracts from station types,
 * calculating station costs, and creating production chains for complete
 * space station construction.
 * </p>
 * <p>
 * Endpoints:
 * <ul>
 *   <li>{@code GET /api/v1/economic/{galaxyId}/stations/modules} — All station module mappings</li>
 *   <li>{@code GET /api/v1/economic/{galaxyId}/stations/{stationType}/modules} — Modules for a type</li>
 *   <li>{@code POST /api/v1/economic/{galaxyId}/stations} — Create station contracts</li>
 *   <li>{@code GET /api/v1/economic/{galaxyId}/stations/{stationType}/cost} — Cost estimation</li>
 *   <li>{@code POST /api/v1/economic/{galaxyId}/stations/{stationType}/chain} — Create production chain</li>
 * </ul>
 * </p>
 */
@RestController
@RequestMapping("/api/v1/economic")
public class SpaceStationController {

    private static final Logger log = LoggerFactory.getLogger(SpaceStationController.class);

    private final SpaceStationContractService spaceStationContractService;
    private final AiCharacterRepository aiCharacterRepository;

    public SpaceStationController(SpaceStationContractService spaceStationContractService,
                                   AiCharacterRepository aiCharacterRepository) {
        this.spaceStationContractService = spaceStationContractService;
        this.aiCharacterRepository = aiCharacterRepository;
    }

    // ------------------------------------------------------------------
    // Module mapping endpoints
    // ------------------------------------------------------------------

    /**
     * Get module mappings for all station types.
     */
    @GetMapping("/{galaxyId}/stations/modules")
    public ResponseEntity<Map<String, Object>> getAllStationModules(@PathVariable Long galaxyId) {
        Map<String, Object> result = new HashMap<>();
        result.put("galaxyId", galaxyId);

        Map<String, Object> allTypes = new HashMap<>();
        for (SpaceStationType type : SpaceStationType.values()) {
            List<StationModuleContract> modules = spaceStationContractService.getModuleContracts(type);
            List<Map<String, Object>> moduleMaps = modules.stream()
                    .map(this::moduleToMap)
                    .collect(Collectors.toList());
            allTypes.put(type.name(), moduleMaps);
        }

        result.put("stationTypes", allTypes);
        return ResponseEntity.ok(result);
    }

    /**
     * Get module mapping for a specific station type.
     */
    @GetMapping("/{galaxyId}/stations/{stationType}/modules")
    public ResponseEntity<?> getStationModules(
            @PathVariable Long galaxyId,
            @PathVariable String stationType) {

        try {
            SpaceStationType type = SpaceStationType.valueOf(stationType);
            List<StationModuleContract> modules = spaceStationContractService.getModuleContracts(type);

            List<Map<String, Object>> moduleMaps = modules.stream()
                    .map(this::moduleToMap)
                    .collect(Collectors.toList());

            Map<String, Object> result = new HashMap<>();
            result.put("galaxyId", galaxyId);
            result.put("stationType", type.name());
            result.put("stationTypeDisplayName", type.getDisplayName());
            result.put("count", moduleMaps.size());
            result.put("modules", moduleMaps);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Invalid station type: " + stationType);
            return ResponseEntity.badRequest().body(error);
        }
    }

    // ------------------------------------------------------------------
    // Create station contracts endpoint
    // ------------------------------------------------------------------

    /**
     * Create build contracts for all modules of a space station.
     */
    @PostMapping("/{galaxyId}/stations")
    public ResponseEntity<?> createStationContracts(
            @PathVariable Long galaxyId,
            @RequestBody Map<String, Object> request) {

        String stationTypeName = (String) request.get("stationType");
        String name = (String) request.get("name");

        if (stationTypeName == null || name == null) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "stationType and name are required");
            return ResponseEntity.badRequest().body(error);
        }

        try {
            SpaceStationType type = SpaceStationType.valueOf(stationTypeName);
            List<BuildContract> contracts = spaceStationContractService.createStationContracts(type, name, galaxyId);

            List<Map<String, Object>> contractMaps = contracts.stream()
                    .map(this::contractToMap)
                    .collect(Collectors.toList());

            Map<String, Object> result = new HashMap<>();
            result.put("galaxyId", galaxyId);
            result.put("stationType", type.name());
            result.put("stationName", name);
            result.put("count", contractMaps.size());
            result.put("contracts", contractMaps);
            result.put("message", "Station contracts created successfully");
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Invalid station type: " + stationTypeName);
            return ResponseEntity.badRequest().body(error);
        }
    }

    // ------------------------------------------------------------------
    // Cost estimation endpoint
    // ------------------------------------------------------------------

    /**
     * Calculate total cost for a station type.
     */
    @GetMapping("/{galaxyId}/stations/{stationType}/cost")
    public ResponseEntity<?> calculateStationCost(
            @PathVariable Long galaxyId,
            @PathVariable String stationType,
            @RequestParam(required = false) Long leaderId) {

        try {
            SpaceStationType type = SpaceStationType.valueOf(stationType);
            AiCharacter leader = null;
            if (leaderId != null) {
                leader = aiCharacterRepository.findById(leaderId).orElse(null);
            }
            Map<String, Object> costReport = spaceStationContractService.calculateStationCost(type, leader);

            Map<String, Object> result = new HashMap<>();
            result.put("galaxyId", galaxyId);
            result.put("stationType", type.name());
            result.put("stationTypeDisplayName", type.getDisplayName());
            result.put("costReport", costReport);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Invalid station type: " + stationType);
            return ResponseEntity.badRequest().body(error);
        }
    }

    // ------------------------------------------------------------------
    // Production chain endpoint
    // ------------------------------------------------------------------

    /**
     * Create a full production chain for a space station.
     */
    @PostMapping("/{galaxyId}/stations/{stationType}/chain")
    public ResponseEntity<?> createProductionChain(
            @PathVariable Long galaxyId,
            @PathVariable String stationType,
            @RequestBody Map<String, Object> request) {

        String name = (String) request.get("name");

        if (name == null) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "name is required");
            return ResponseEntity.badRequest().body(error);
        }

        try {
            SpaceStationType type = SpaceStationType.valueOf(stationType);
            var chain = spaceStationContractService.createProductionChainForStation(type, name, galaxyId);

            Map<String, Object> result = new HashMap<>();
            result.put("galaxyId", galaxyId);
            result.put("stationType", type.name());
            result.put("stationName", name);
            result.put("chain", chainToMap(chain));
            result.put("message", "Production chain created for station");
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Invalid station type: " + stationType);
            return ResponseEntity.badRequest().body(error);
        }
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    /**
     * Convert a StationModuleContract to a Map for JSON serialization.
     */
    private Map<String, Object> moduleToMap(StationModuleContract module) {
        Map<String, Object> map = new HashMap<>();
        map.put("contractName", module.getContractName());
        map.put("materialRequirementsJson", module.getMaterialRequirementsJson());
        map.put("estimatedDurationTurns", module.getEstimatedDurationTurns());
        map.put("priority", module.getPriority());
        map.put("moduleType", module.getModuleType() != null ? module.getModuleType().name() : null);
        map.put("leaderRoleRequired", module.getLeaderRoleRequired());
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
        return map;
    }

    /**
     * Convert a ProductionChainTracking entity to a Map for JSON serialization.
     */
    private Map<String, Object> chainToMap(com.yisy.modules.doramcu.hex.model.ProductionChainTracking chain) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", chain.getId());
        map.put("chainName", chain.getChainName());
        map.put("targetItemName", chain.getTargetItemName());
        map.put("status", chain.getStatus() != null ? chain.getStatus().name() : null);
        map.put("totalContracts", chain.getTotalContracts());
        map.put("completedContracts", chain.getCompletedContracts());
        map.put("progressPercent", chain.getProgressPercent());
        map.put("totalMaterialCostCredits", chain.getTotalMaterialCostCredits());
        map.put("totalLaborCostCredits", chain.getTotalLaborCostCredits());
        map.put("totalDurationTurns", chain.getTotalDurationTurns());
        return map;
    }
}
