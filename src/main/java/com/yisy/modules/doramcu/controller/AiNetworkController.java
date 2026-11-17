package com.yisy.modules.doramcu.controller;

import com.yisy.modules.doramcu.eve.model.AiCharacter;
import com.yisy.modules.doramcu.service.ai.AiNetworkService;
import com.yisy.modules.doramcu.service.ai.AiNetworkService.AiNetworkStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST API for AI network status and topology.
 * <p>
 * Provides endpoints for viewing the AI network — all ships, stations,
 * vehicles, and facilities connected to the AI network as AI characters.
 * </p>
 */
@RestController
@RequestMapping("/api/v1/ai-network")
public class AiNetworkController {

    private final AiNetworkService aiNetworkService;

    public AiNetworkController(AiNetworkService aiNetworkService) {
        this.aiNetworkService = aiNetworkService;
    }

    /**
     * GET /api/v1/ai-network
     * Get AI network status summaries for all empires.
     * Uses the underlying AiNetworkService; the AiNetworkManager framework interface
     * provides the same topology data through getNetworkTopology().
     */
    @GetMapping
    public ResponseEntity<List<AiNetworkStatus>> getAllNetworks() {
        List<AiNetworkStatus> statuses = aiNetworkService.getAllNetworksStatus();
        return ResponseEntity.ok(statuses);
    }

    /**
     * GET /api/v1/ai-network/{empireName}
     * Get AI network status for a specific empire.
     */
    @GetMapping("/{empireName}")
    public ResponseEntity<AiNetworkStatus> getNetworkStatus(@PathVariable String empireName) {
        AiNetworkStatus status = aiNetworkService.getNetworkStatus(empireName);
        return ResponseEntity.ok(status);
    }

    /**
     * GET /api/v1/ai-network/{empireName}/entities
     * Get all active AI network entities for an empire.
     */
    @GetMapping("/{empireName}/entities")
    public ResponseEntity<List<AiCharacter>> getActiveEntities(@PathVariable String empireName) {
        List<AiCharacter> entities = aiNetworkService.getActiveNetworkEntities(empireName);
        return ResponseEntity.ok(entities);
    }

    /**
     * GET /api/v1/ai-network/{empireName}/entities/all
     * Get all AI network entities (active and inactive) for an empire.
     */
    @GetMapping("/{empireName}/entities/all")
    public ResponseEntity<List<AiCharacter>> getAllEntities(@PathVariable String empireName) {
        List<AiCharacter> entities = aiNetworkService.getAllNetworkEntities(empireName);
        return ResponseEntity.ok(entities);
    }

    /**
     * GET /api/v1/ai-network/{empireName}/topology
     * Get AI network topology grouped by entity type.
     */
    @GetMapping("/{empireName}/topology")
    public ResponseEntity<Map<String, List<AiCharacter>>> getTopology(@PathVariable String empireName) {
        Map<AiCharacter.CharacterType, List<AiCharacter>> topology = aiNetworkService.getNetworkTopology(empireName);
        // Convert enum keys to strings for JSON
        Map<String, List<AiCharacter>> stringKeyed = new java.util.HashMap<>();
        for (Map.Entry<AiCharacter.CharacterType, List<AiCharacter>> entry : topology.entrySet()) {
            stringKeyed.put(entry.getKey().name(), entry.getValue());
        }
        return ResponseEntity.ok(stringKeyed);
    }

    /**
     * GET /api/v1/ai-network/ship/{shipId}/connected
     * Check if a ship is connected to the AI network.
     */
    @GetMapping("/ship/{shipId}/connected")
    public ResponseEntity<Map<String, Object>> isShipConnected(@PathVariable int shipId) {
        boolean connected = aiNetworkService.isShipConnected(shipId);
        return ResponseEntity.ok(Map.of(
                "shipId", shipId,
                "connected", connected
        ));
    }

    /**
     * GET /api/v1/ai-network/station/{stationId}/connected
     * Check if a station is connected to the AI network.
     */
    @GetMapping("/station/{stationId}/connected")
    public ResponseEntity<Map<String, Object>> isStationConnected(@PathVariable int stationId) {
        boolean connected = aiNetworkService.isStationConnected(stationId);
        return ResponseEntity.ok(Map.of(
                "stationId", stationId,
                "connected", connected
        ));
    }

    /**
     * GET /api/v1/ai-network/vehicle/{vehicleId}/connected
     * Check if a vehicle is connected to the AI network.
     */
    @GetMapping("/vehicle/{vehicleId}/connected")
    public ResponseEntity<Map<String, Object>> isVehicleConnected(@PathVariable String vehicleId) {
        boolean connected = aiNetworkService.isVehicleConnected(vehicleId);
        return ResponseEntity.ok(Map.of(
                "vehicleId", vehicleId,
                "connected", connected
        ));
    }

    /**
     * GET /api/v1/ai-network/facility/{facilityId}/connected
     * Check if a facility is connected to the AI network.
     */
    @GetMapping("/facility/{facilityId}/connected")
    public ResponseEntity<Map<String, Object>> isFacilityConnected(@PathVariable int facilityId) {
        boolean connected = aiNetworkService.isFacilityConnected(facilityId);
        return ResponseEntity.ok(Map.of(
                "facilityId", facilityId,
                "connected", connected
        ));
    }
}
