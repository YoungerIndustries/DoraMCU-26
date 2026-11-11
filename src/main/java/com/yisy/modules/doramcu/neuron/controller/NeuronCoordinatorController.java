package com.yisy.modules.doramcu.neuron.controller;

import com.yisy.modules.doramcu.neuron.model.NeuronIdentity;
import com.yisy.modules.doramcu.neuron.model.NeuronHeartbeat;
import com.yisy.modules.doramcu.neuron.service.NeuronRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Neuron Coordinator — handles neuron registration, heartbeats, and deregistration.
 *
 * This is the primary interface between DoraMCU (coordinator) and dora-neuron nodes.
 * Neurons register with this coordinator, send periodic heartbeats, and deregister
 * on shutdown.
 */
@RestController
@RequestMapping("/api/v1/neuron")
public class NeuronCoordinatorController {

    private static final Logger log = LoggerFactory.getLogger(NeuronCoordinatorController.class);

    private final NeuronRegistry neuronRegistry;

    public NeuronCoordinatorController(NeuronRegistry neuronRegistry) {
        this.neuronRegistry = neuronRegistry;
    }

    /**
     * Register a new neuron with this coordinator.
     */
    @PostMapping("/register")
    public ResponseEntity<Void> register(@RequestBody NeuronIdentity identity) {
        neuronRegistry.register(identity);
        log.info("Neuron registered: {} ({}) in region {}",
                identity.getNodeName(), identity.getNodeId(), identity.getRegion());
        return ResponseEntity.ok().build();
    }

    /**
     * Send a heartbeat to update neuron status.
     */
    @PostMapping("/heartbeat")
    public ResponseEntity<Void> heartbeat(@RequestBody NeuronHeartbeat heartbeat) {
        neuronRegistry.updateHeartbeat(heartbeat);
        return ResponseEntity.ok().build();
    }

    /**
     * Deregister a neuron (graceful shutdown).
     */
    @DeleteMapping("/deregister")
    public ResponseEntity<Void> deregister(@RequestParam String nodeId) {
        boolean removed = neuronRegistry.deregister(nodeId);
        if (removed) {
            log.info("Neuron deregistered: {}", nodeId);
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * List all registered neurons.
     */
    @GetMapping("/list")
    public ResponseEntity<List<NeuronIdentity>> listNeurons() {
        return ResponseEntity.ok(neuronRegistry.getAllNeurons());
    }

    /**
     * Get a specific neuron by ID.
     */
    @GetMapping("/{nodeId}")
    public ResponseEntity<NeuronIdentity> getNeuron(@PathVariable String nodeId) {
        return neuronRegistry.getNeuron(nodeId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get neuron count by status.
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        Map<String, Object> stats = neuronRegistry.getStats();
        return ResponseEntity.ok(stats);
    }
}
