package com.yisy.modules.doraneuron.protocol;

/**
 * Periodic health signal sent from a Neuron to the coordinator.
 *
 * The coordinator uses heartbeats to track Neuron health, evict stale nodes,
 * and maintain an up-to-date cluster registry.
 */
public class NeuronHeartbeat {

    private String nodeId;
    private String status;
    private long currentModelVersion;
    private int experienceCount;
    private long timestamp;

    public NeuronHeartbeat() {
    }

    public NeuronHeartbeat(String nodeId, String status, long currentModelVersion,
                           int experienceCount, long timestamp) {
        this.nodeId = nodeId;
        this.status = status;
        this.currentModelVersion = currentModelVersion;
        this.experienceCount = experienceCount;
        this.timestamp = timestamp;
    }

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getCurrentModelVersion() {
        return currentModelVersion;
    }

    public void setCurrentModelVersion(long currentModelVersion) {
        this.currentModelVersion = currentModelVersion;
    }

    public int getExperienceCount() {
        return experienceCount;
    }

    public void setExperienceCount(int experienceCount) {
        this.experienceCount = experienceCount;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
