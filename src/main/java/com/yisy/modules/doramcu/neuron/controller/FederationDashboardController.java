package com.yisy.modules.doramcu.neuron.controller;

import com.yisy.modules.doramcu.neuron.dto.*;
import com.yisy.modules.doramcu.neuron.service.FederationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST API for Federation Health Dashboard.
 *
 * Provides comprehensive monitoring endpoints for the Multi-Region Federation:
 * overall dashboard, peer health, model version comparison, sync history,
 * and health assessment.
 */
@RestController
@RequestMapping("/api/v1/neuron/federation/dashboard")
public class FederationDashboardController {

    private final FederationService federationService;

    public FederationDashboardController(FederationService federationService) {
        this.federationService = federationService;
    }

    /**
     * Get comprehensive federation health dashboard.
     *
     * Aggregates all federation state — peers, neurons, sync status, model versions,
     * and health assessment — into a single response.
     */
    @GetMapping
    public ResponseEntity<FederationDashboardDto> getDashboard() {
        FederationDashboardDto dashboard = federationService.getFederationDashboard();
        return ResponseEntity.ok(dashboard);
    }

    /**
     * Get health summary for a specific peer region.
     */
    @GetMapping("/peer/{region}")
    public ResponseEntity<PeerHealthDto> getPeerHealth(@PathVariable String region) {
        PeerHealthDto health = federationService.getPeerHealth(region);
        if (health == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(health);
    }

    /**
     * Detect model version drift across all federated regions.
     *
     * Returns only regions where the model version differs from the local coordinator.
     */
    @GetMapping("/model-drift")
    public ResponseEntity<List<ModelVersionDto>> getModelVersionDrift() {
        List<ModelVersionDto> drift = federationService.detectModelVersionDrift();
        return ResponseEntity.ok(drift);
    }

    /**
     * Calculate sync lag for all peers.
     *
     * Returns seconds since last contact for each peer region.
     * A value of -1 indicates the peer has never been contacted.
     */
    @GetMapping("/sync-lag")
    public ResponseEntity<Map<String, Long>> getSyncLag() {
        Map<String, Long> lag = federationService.calculateSyncLag();
        return ResponseEntity.ok(lag);
    }

    /**
     * Get recent sync history.
     *
     * Returns the last 50 sync operations with all peers.
     */
    @GetMapping("/sync-history")
    public ResponseEntity<List<SyncHistoryDto>> getSyncHistory() {
        List<SyncHistoryDto> history = federationService.getSyncHistory();
        return ResponseEntity.ok(history);
    }
}
