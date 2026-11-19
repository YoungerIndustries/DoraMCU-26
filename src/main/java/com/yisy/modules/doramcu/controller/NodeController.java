package com.yisy.modules.doramcu.controller;

import com.yisy.modules.doramcu.dto.NodeHealthResponse;
import com.yisy.modules.doramcu.dto.NodeRegistrationRequest;
import com.yisy.modules.doramcu.dto.NodeRegistrationResponse;
import com.yisy.modules.doramcu.model.Node;
import com.yisy.modules.doramcu.repository.NodeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * REST controller for managing nodes in the DoraMCU brain network.
 * <p>
 * Handles device registration, heartbeat updates, health checks, and cleanup
 * for Neuron, Node, and BrainCell devices connecting to the BrainStem.
 * </p>
 */
@RestController
@RequestMapping("/api/v1/nodes")
public class NodeController {

    private static final Logger log = LoggerFactory.getLogger(NodeController.class);

    private final NodeRepository nodeRepository;

    // Heartbeat timeout: 5 minutes
    private static final long HEARTBEAT_TIMEOUT_MINUTES = 5;

    public NodeController(NodeRepository nodeRepository) {
        this.nodeRepository = nodeRepository;
    }

    /**
     * Register a new node in the brain network.
     * POST /api/v1/nodes/register
     */
    @PostMapping("/register")
    public ResponseEntity<NodeRegistrationResponse> registerNode(
            @RequestBody NodeRegistrationRequest request) {

        log.info("Registering node: type={}, device={}", request.getNodeType(), request.getDeviceId());

        // Check if device already registered
        Optional<Node> existing = nodeRepository.findByDeviceId(request.getDeviceId());
        if (existing.isPresent()) {
            Node node = existing.get();
            // Update existing node
            node.setNodeType(request.getNodeType());
            node.setLastHeartbeat(LocalDateTime.now());
            node.setUpdatedAt(LocalDateTime.now());
            node.setStatus("active");
            nodeRepository.save(node);

            log.info("Node re-registered: {}", node.getNodeId());
            return ResponseEntity.ok(new NodeRegistrationResponse(
                    node.getNodeId(),
                    node.getNodeType(),
                    node.getStatus()
            ));
        }

        // Create new node
        Node node = new Node();
        node.setNodeId(UUID.randomUUID().toString());
        node.setNodeType(request.getNodeType());
        node.setDeviceId(request.getDeviceId());

        // Set capabilities
        if (request.getCapabilities() != null) {
            node.setCpuCores(request.getCapabilities().getCpuCores());
            node.setRamMb(request.getCapabilities().getRamMb());
            node.setGpuTier(request.getCapabilities().getGpuTier());
        }

        // Set network info
        if (request.getNetworkInfo() != null) {
            node.setIpAddress(request.getNetworkInfo().getIpAddress());
            node.setPort(request.getNetworkInfo().getPort());
        }

        node.setStatus("active");
        node.setRegisteredAt(LocalDateTime.now());
        node.setLastHeartbeat(LocalDateTime.now());
        node.setUpdatedAt(LocalDateTime.now());

        node = nodeRepository.save(node);

        log.info("Node registered successfully: id={}, type={}", node.getNodeId(), node.getNodeType());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new NodeRegistrationResponse(
                        node.getNodeId(),
                        node.getNodeType(),
                        node.getStatus()
                ));
    }

    /**
     * Update heartbeat for a node.
     * POST /api/v1/nodes/{nodeId}/heartbeat
     */
    @PostMapping("/{nodeId}/heartbeat")
    public ResponseEntity<Void> updateHeartbeat(@PathVariable String nodeId) {
        Optional<Node> nodeOpt = nodeRepository.findByNodeId(nodeId);

        if (nodeOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Node node = nodeOpt.get();
        node.setLastHeartbeat(LocalDateTime.now());
        node.setUpdatedAt(LocalDateTime.now());
        node.setStatus("active");
        nodeRepository.save(node);

        log.debug("Heartbeat received from node: {}", nodeId);
        return ResponseEntity.ok().build();
    }

    /**
     * Check health of a specific node.
     * GET /api/v1/nodes/{id}/health
     */
    @GetMapping("/{id}/health")
    public ResponseEntity<NodeHealthResponse> checkHealth(@PathVariable String id) {
        Optional<Node> nodeOpt = nodeRepository.findByNodeId(id);

        if (nodeOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Node node = nodeOpt.get();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime threshold = now.minus(HEARTBEAT_TIMEOUT_MINUTES, ChronoUnit.MINUTES);
        boolean healthy = node.getLastHeartbeat().isAfter(threshold);

        if (!healthy) {
            node.setStatus("disconnected");
            nodeRepository.save(node);
        }

        NodeHealthResponse response = new NodeHealthResponse(
                node.getNodeId(),
                healthy,
                node.getStatus(),
                node.getLastHeartbeat().toInstant(java.time.ZoneOffset.UTC).toEpochMilli()
        );

        return ResponseEntity.ok(response);
    }

    /**
     * List all connected nodes.
     * GET /api/v1/nodes
     */
    @GetMapping
    public ResponseEntity<List<Node>> listNodes() {
        List<Node> nodes = nodeRepository.findAll();
        return ResponseEntity.ok(nodes);
    }

    /**
     * Unregister a node.
     * DELETE /api/v1/nodes/{nodeId}
     */
    @DeleteMapping("/{nodeId}")
    public ResponseEntity<Void> unregisterNode(@PathVariable String nodeId) {
        Optional<Node> nodeOpt = nodeRepository.findByNodeId(nodeId);

        if (nodeOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        nodeRepository.deleteByNodeId(nodeId);
        log.info("Node unregistered: {}", nodeId);

        return ResponseEntity.noContent().build();
    }

    /**
     * Clean up stale nodes.
     * Nodes without heartbeat for more than HEARTBEAT_TIMEOUT_MINUTES are marked as disconnected.
     * GET /api/v1/nodes/cleanup
     */
    @GetMapping("/cleanup")
    public ResponseEntity<Integer> cleanupStaleNodes() {
        LocalDateTime threshold = LocalDateTime.now()
                .minus(HEARTBEAT_TIMEOUT_MINUTES, ChronoUnit.MINUTES);

        List<Node> staleNodes = nodeRepository.findStaleNodes(threshold);
        for (Node node : staleNodes) {
            node.setStatus("disconnected");
            node.setUpdatedAt(LocalDateTime.now());
            nodeRepository.save(node);
        }

        log.info("Cleaned up {} stale nodes", staleNodes.size());
        return ResponseEntity.ok(staleNodes.size());
    }
}
