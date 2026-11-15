package com.yisy.modules.doramcu.neuron.controller;

import com.yisy.modules.doramcu.neuron.service.NeuronRegistry;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Co-Server health endpoint — used by dora-neuron's LeaderElectionService
 * to determine if the main coordinator server is reachable.
 */
@RestController
@RequestMapping("/api/v1/co-server")
public class CoServerController {

    private final NeuronRegistry neuronRegistry;

    public CoServerController(NeuronRegistry neuronRegistry) {
        this.neuronRegistry = neuronRegistry;
    }

    /**
     * Health check for the coordinator server.
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "coordinator", "DoraMCU",
                "neuronsRegistered", neuronRegistry.getAllNeurons().size()
        ));
    }
}
