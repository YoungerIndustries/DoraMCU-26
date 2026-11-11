package com.yisy.modules.doramcu.neuron.service;

import com.yisy.modules.doramcu.neuron.model.NeuronHeartbeat;
import com.yisy.modules.doramcu.neuron.model.NeuronIdentity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * In-memory neuron registry for the coordinator.
 *
 * Tracks all registered neurons, their status, and heartbeat timestamps.
 * Used by NeuronCoordinatorController and FederationService.
 */
@Service
public class NeuronRegistry {

    private static final Logger log = LoggerFactory.getLogger(NeuronRegistry.class);

    /** Map of nodeId -> neuron identity. */
    private final ConcurrentHashMap<String, NeuronIdentity> neurons = new ConcurrentHashMap<>();

    /**
     * Register a new neuron.
     */
    public void register(NeuronIdentity identity) {
        if (identity == null || identity.getNodeId() == null) {
            log.warn("Attempted to register neuron with null ID");
            return;
        }
        neurons.put(identity.getNodeId(), identity);
        log.info("Registered neuron: {} ({})", identity.getNodeName(), identity.getNodeId());
    }

    /**
     * Update heartbeat for a neuron.
     */
    public void updateHeartbeat(NeuronHeartbeat heartbeat) {
        if (heartbeat == null || heartbeat.getNodeId() == null) {
            return;
        }
        NeuronIdentity identity = neurons.get(heartbeat.getNodeId());
        if (identity != null) {
            identity.setLastHeartbeat(heartbeat.getTimestamp());
            identity.setStatus(heartbeat.getStatus());
            identity.setCurrentModelVersion(heartbeat.getCurrentModelVersion());
            identity.setExperienceCount(heartbeat.getExperienceCount());
        } else {
            log.warn("Heartbeat from unknown neuron: {}", heartbeat.getNodeId());
        }
    }

    /**
     * Update heartbeat for a neuron from Kafka event data.
     * Overloaded method for use by HealthEventConsumer.
     */
    public void updateHeartbeat(String nodeId, String status, long modelVersion,
                                int experienceCount, long timestamp) {
        NeuronIdentity identity = neurons.get(nodeId);
        if (identity != null) {
            identity.setLastHeartbeat(timestamp);
            identity.setStatus(status);
            identity.setCurrentModelVersion(modelVersion);
            identity.setExperienceCount(experienceCount);
        } else {
            log.warn("Heartbeat from unknown neuron: {}", nodeId);
        }
    }

    /**
     * Deregister a neuron by ID.
     * @return true if the neuron was found and removed
     */
    public boolean deregister(String nodeId) {
        NeuronIdentity removed = neurons.remove(nodeId);
        return removed != null;
    }

    /**
     * Get all registered neurons.
     */
    public List<NeuronIdentity> getAllNeurons() {
        return new ArrayList<>(neurons.values());
    }

    /**
     * Get a specific neuron by ID.
     */
    public Optional<NeuronIdentity> getNeuron(String nodeId) {
        return Optional.ofNullable(neurons.get(nodeId));
    }

    /**
     * Get neurons by region.
     */
    public List<NeuronIdentity> getNeuronsByRegion(String region) {
        return neurons.values().stream()
                .filter(n -> region.equals(n.getRegion()))
                .collect(Collectors.toList());
    }

    /**
     * Get neuron count by status.
     */
    public Map<String, Object> getStats() {
        Map<String, Long> countByStatus = new HashMap<>();
        long total = 0;
        long active = 0;
        long modelVersion = 0;

        for (NeuronIdentity n : neurons.values()) {
            total++;
            String status = n.getStatus() != null ? n.getStatus() : "UNKNOWN";
            countByStatus.merge(status, 1L, Long::sum);
            if ("ACTIVE".equals(status)) {
                active++;
            }
            if (n.getCurrentModelVersion() > modelVersion) {
                modelVersion = n.getCurrentModelVersion();
            }
        }

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalNeurons", total);
        stats.put("activeNeurons", active);
        stats.put("countByStatus", countByStatus);
        stats.put("modelVersion", modelVersion);
        return stats;
    }

    /**
     * Get total neuron count.
     */
    public int getTotalCount() {
        return neurons.size();
    }

    /**
     * Get neuron count by status map.
     */
    public Map<String, Integer> getCountByStatus() {
        Map<String, Integer> counts = new HashMap<>();
        for (NeuronIdentity n : neurons.values()) {
            String status = n.getStatus() != null ? n.getStatus() : "UNKNOWN";
            counts.merge(status, 1, Integer::sum);
        }
        return counts;
    }
}
