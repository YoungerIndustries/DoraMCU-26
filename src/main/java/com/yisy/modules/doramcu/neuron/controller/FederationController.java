package com.yisy.modules.doramcu.neuron.controller;

import com.yisy.modules.doramcu.neuron.model.*;
import com.yisy.modules.doramcu.neuron.service.FederationService;
import com.yisy.modules.doramcu.neuron.service.NeuronRegistry;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST API for Multi-Region Federation management.
 *
 * Endpoints for peer coordinator registration, registry sync,
 * cross-region consensus, experience forwarding, and federation status.
 */
@RestController
@RequestMapping("/api/v1/neuron/federation")
public class FederationController {

    private final FederationService federationService;
    private final NeuronRegistry neuronRegistry;

    public FederationController(FederationService federationService, NeuronRegistry neuronRegistry) {
        this.federationService = federationService;
        this.neuronRegistry = neuronRegistry;
    }

    // ── Peer Management ──────────────────────────────────────────────────────

    /**
     * Register a new peer coordinator.
     */
    @PostMapping("/peer/register")
    public ResponseEntity<FederationPeer> registerPeer(@RequestBody PeerRegisterRequest request) {
        FederationPeer peer = federationService.registerPeer(request.getRegion(), request.getUrl());
        return ResponseEntity.ok(peer);
    }

    /**
     * Remove a peer coordinator by region.
     */
    @DeleteMapping("/peer/{region}")
    public ResponseEntity<Void> removePeer(@PathVariable String region) {
        boolean removed = federationService.removePeer(region);
        return removed ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }

    /**
     * List all registered peer coordinators.
     */
    @GetMapping("/peers")
    public ResponseEntity<List<FederationPeer>> getPeers() {
        return ResponseEntity.ok(federationService.getPeers());
    }

    // ── Registry Sync ────────────────────────────────────────────────────────

    /**
     * Handle incoming registry sync request from a peer.
     */
    @PostMapping("/sync")
    public ResponseEntity<FederationSyncResponse> handleSyncRequest(
            @RequestBody FederationSyncRequest request) {
        // Build response from local neuron registry
        FederationSyncResponse response = new FederationSyncResponse();
        response.setRegion(federationService.getStatus().getRegion());
        response.setModelVersion(federationService.getCurrentModelVersion());
        response.setTotalNeuronCount(neuronRegistry.getTotalCount());
        response.setNeuronCountByStatus(neuronRegistry.getCountByStatus());

        if (request.getSyncType() == FederationSyncRequest.SyncType.FULL) {
            response.setNeuronIdentities(neuronRegistry.getAllNeurons());
        }
        return ResponseEntity.ok(response);
    }

    /**
     * Trigger manual registry sync with all peers.
     */
    @PostMapping("/sync/manual")
    public ResponseEntity<Map<String, FederationSyncResponse>> triggerManualSync() {
        Map<String, FederationSyncResponse> results = federationService.triggerManualSync();
        return ResponseEntity.ok(results);
    }

    // ── Cross-Region Consensus ───────────────────────────────────────────────

    /**
     * Request cross-region consensus from all peers.
     */
    @PostMapping("/consensus/cross-region")
    public ResponseEntity<Map<String, Object>> requestCrossRegionConsensus(
            @RequestBody FederationConsensusRequest request) {
        Map<String, Object> results = federationService.requestCrossRegionConsensus(request);
        return ResponseEntity.ok(results);
    }

    /**
     * Handle incoming consensus request from a peer coordinator.
     */
    @PostMapping("/consensus")
    public ResponseEntity<Map<String, Object>> handleConsensusRequest(
            @RequestBody FederationConsensusRequest request) {
        // In a full implementation, this would forward to local neurons
        // and aggregate their votes
        return ResponseEntity.ok(Map.of(
                "region", federationService.getStatus().getRegion(),
                "voteCount", 0,
                "totalWeight", 0.0
        ));
    }

    // ── Experience Forwarding ────────────────────────────────────────────────

    /**
     * Forward experience batch to primary coordinator.
     */
    @PostMapping("/experience/forward")
    public ResponseEntity<Void> forwardExperience(@RequestBody FederationExperienceBatch batch) {
        int count = federationService.receiveExperienceBatch(batch);
        return ResponseEntity.ok().build();
    }

    /**
     * Send experience batch from this coordinator to primary.
     */
    @PostMapping("/experience/send")
    public ResponseEntity<Map<String, Object>> sendExperience(@RequestBody FederationExperienceBatch batch) {
        boolean success = federationService.forwardExperienceToPrimary(batch);
        return ResponseEntity.ok(Map.of("success", success, "samples", batch.getSampleCount()));
    }

    // ── Model Update Propagation ─────────────────────────────────────────────

    /**
     * Propagate model update to all peer coordinators (primary only).
     */
    @PostMapping("/model/propagate")
    public ResponseEntity<Map<String, Boolean>> propagateModelUpdate(
            @RequestBody Map<String, Object> request) {
        long modelVersion = ((Number) request.get("modelVersion")).longValue();
        Map<String, Boolean> results = federationService.propagateModelUpdate(modelVersion);
        return ResponseEntity.ok(results);
    }

    /**
     * Handle incoming model update from primary coordinator.
     */
    @PostMapping("/model/receive")
    public ResponseEntity<Void> receiveModelUpdate(@RequestBody Map<String, Object> request) {
        long modelVersion = ((Number) request.get("modelVersion")).longValue();
        String sourceRegion = (String) request.get("sourceRegion");
        federationService.receiveModelUpdate(modelVersion, sourceRegion);
        return ResponseEntity.ok().build();
    }

    // ── Federation Status ────────────────────────────────────────────────────

    /**
     * Get federation status summary.
     */
    @GetMapping("/status")
    public ResponseEntity<FederationService.FederationStatus> getStatus() {
        return ResponseEntity.ok(federationService.getStatus());
    }

    // ── Inner Classes ────────────────────────────────────────────────────────

    /**
     * Request body for peer registration.
     */
    public static class PeerRegisterRequest {
        private String region;
        private String url;

        public String getRegion() { return region; }
        public void setRegion(String region) { this.region = region; }
        public String getUrl() { return url; }
        public void setUrl(String url) { this.url = url; }
    }
}
