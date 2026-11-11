package com.yisy.modules.doraneuron.controller;

import com.yisy.modules.doraneuron.service.LeaderElectionService;
import com.yisy.modules.doraneuron.service.TaskDistributionService;
import com.yisy.modules.doraneuron.service.DataSyncService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST controller for co-server operations in the dora-neuron module.
 * 
 * Provides endpoints for monitoring and managing the co-server functionality
 * including leader election status, task distribution, and data sync.
 */
@RestController
@RequestMapping("/api/v1/co-server")
public class CoServerController {

    private static final Logger log = LoggerFactory.getLogger(CoServerController.class);

    private final LeaderElectionService leaderElectionService;
    private final TaskDistributionService taskDistributionService;
    private final DataSyncService dataSyncService;

    public CoServerController(LeaderElectionService leaderElectionService,
                              TaskDistributionService taskDistributionService,
                              DataSyncService dataSyncService) {
        this.leaderElectionService = leaderElectionService;
        this.taskDistributionService = taskDistributionService;
        this.dataSyncService = dataSyncService;
    }

    /**
     * Get the current co-server status.
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus() {
        return ResponseEntity.ok(Map.of(
                "leaderElection", leaderElectionService.getCoServerStatus(),
                "taskDistribution", taskDistributionService.getTaskStats(),
                "dataSync", dataSyncService.getSyncStats()
        ));
    }

    /**
     * Trigger leader election manually.
     */
    @PostMapping("/elect")
    public ResponseEntity<Map<String, Object>> triggerElection() {
        log.info("Manual leader election triggered");
        leaderElectionService.triggerLeaderElection();
        
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Leader election triggered",
                "isCoServer", leaderElectionService.isCoServer()
        ));
    }

    /**
     * Step down from co-server role.
     */
    @PostMapping("/step-down")
    public ResponseEntity<Map<String, Object>> stepDown() {
        log.info("Manual step-down from co-server triggered");
        leaderElectionService.stepDown();
        
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Stepped down from co-server",
                "isCoServer", leaderElectionService.isCoServer()
        ));
    }

    /**
     * Force sync with the main server.
     */
    @PostMapping("/sync")
    public ResponseEntity<Map<String, Object>> forceSync() {
        log.info("Manual sync triggered");
        dataSyncService.syncWithServer();
        
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Sync triggered",
                "syncStats", dataSyncService.getSyncStats()
        ));
    }

    /**
     * Health check endpoint.
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "isCoServer", leaderElectionService.isCoServer(),
                "serverReachable", leaderElectionService.isServerReachable(),
                "timestamp", System.currentTimeMillis()
        ));
    }
}
