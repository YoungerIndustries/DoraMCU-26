package com.yisy.modules.doraneuron.protocol.mesh;

import java.io.Serializable;

/**
 * Describes a Neuron peer in the P2P mesh network.
 *
 * This DTO is used for peer discovery and topology maintenance.
 * Each neuron advertises its capabilities and status to peers.
 */
public class MeshPeerInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Unique node ID of this peer. */
    private String nodeId;

    /** Human-readable name. */
    private String nodeName;

    /** Geographic region tag. */
    private String region;

    /** Host address for P2P communication. */
    private String hostAddress;

    /** Port for P2P communication (base port + meshPortOffset). */
    private int port;

    /** Current status of the peer. */
    private String status;

    /** Current ONNX model version. */
    private long modelVersion;

    /** Vote weight for consensus. */
    private double voteWeight;

    /** Number of direct peer connections. */
    private int peerCount;

    /** Experience samples collected since last sync. */
    private int experienceCount;

    /** Timestamp of last heartbeat (epoch ms). */
    private long lastHeartbeat;

    /** CPU capability profile for task routing. */
    private String cpuProfile;

    /** Number of CPU cores available on this peer. */
    private int cpuCores;

    /** Available memory in MB on this peer. */
    private long availableMemoryMb;

    /** Whether this peer has GPU acceleration available. */
    private boolean hasGpu;

    /** Current inference queue depth (pending tasks). */
    private int inferenceQueueDepth;

    /** Average inference latency in milliseconds. */
    private double avgInferenceLatencyMs;

    /** Client type: UNITY, ELECTRON, PWA, FPS_SERVER, NEURON_NODE. */
    private String clientType;

    /** Whether this peer supports local ONNX inference. */
    private boolean supportsInference;

    /** Whether this peer can act as a co-server. */
    private boolean canBeCoServer;

    /** Network latency to this peer in milliseconds (set by discoverer). */
    private double networkLatencyMs;

    // ── Getters and Setters ───────────────────────────────────────────────────

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getHostAddress() {
        return hostAddress;
    }

    public void setHostAddress(String hostAddress) {
        this.hostAddress = hostAddress;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getModelVersion() {
        return modelVersion;
    }

    public void setModelVersion(long modelVersion) {
        this.modelVersion = modelVersion;
    }

    public double getVoteWeight() {
        return voteWeight;
    }

    public void setVoteWeight(double voteWeight) {
        this.voteWeight = voteWeight;
    }

    public int getPeerCount() {
        return peerCount;
    }

    public void setPeerCount(int peerCount) {
        this.peerCount = peerCount;
    }

    public int getExperienceCount() {
        return experienceCount;
    }

    public void setExperienceCount(int experienceCount) {
        this.experienceCount = experienceCount;
    }

    public long getLastHeartbeat() {
        return lastHeartbeat;
    }

    public void setLastHeartbeat(long lastHeartbeat) {
        this.lastHeartbeat = lastHeartbeat;
    }

    public String getCpuProfile() {
        return cpuProfile;
    }

    public void setCpuProfile(String cpuProfile) {
        this.cpuProfile = cpuProfile;
    }

    public int getCpuCores() {
        return cpuCores;
    }

    public void setCpuCores(int cpuCores) {
        this.cpuCores = cpuCores;
    }

    public long getAvailableMemoryMb() {
        return availableMemoryMb;
    }

    public void setAvailableMemoryMb(long availableMemoryMb) {
        this.availableMemoryMb = availableMemoryMb;
    }

    public boolean isHasGpu() {
        return hasGpu;
    }

    public void setHasGpu(boolean hasGpu) {
        this.hasGpu = hasGpu;
    }

    public int getInferenceQueueDepth() {
        return inferenceQueueDepth;
    }

    public void setInferenceQueueDepth(int inferenceQueueDepth) {
        this.inferenceQueueDepth = inferenceQueueDepth;
    }

    public double getAvgInferenceLatencyMs() {
        return avgInferenceLatencyMs;
    }

    public void setAvgInferenceLatencyMs(double avgInferenceLatencyMs) {
        this.avgInferenceLatencyMs = avgInferenceLatencyMs;
    }

    public String getClientType() {
        return clientType;
    }

    public void setClientType(String clientType) {
        this.clientType = clientType;
    }

    public boolean isSupportsInference() {
        return supportsInference;
    }

    public void setSupportsInference(boolean supportsInference) {
        this.supportsInference = supportsInference;
    }

    public boolean isCanBeCoServer() {
        return canBeCoServer;
    }

    public void setCanBeCoServer(boolean canBeCoServer) {
        this.canBeCoServer = canBeCoServer;
    }

    public double getNetworkLatencyMs() {
        return networkLatencyMs;
    }

    public void setNetworkLatencyMs(double networkLatencyMs) {
        this.networkLatencyMs = networkLatencyMs;
    }

    /**
     * Calculate a capability score for task distribution weighting.
     * Higher score = more capable peer for AI inference tasks.
     */
    public double getCapabilityScore() {
        if (!supportsInference) {
            return 0;
        }
        double score = 0;
        // CPU cores contribute up to 40 points
        score += Math.min(cpuCores, 16) * 2.5;
        // Memory contributes up to 20 points
        score += Math.min(availableMemoryMb, 8192) / 8192.0 * 20;
        // GPU bonus: 20 points
        if (hasGpu) {
            score += 20;
        }
        // Queue depth penalty: up to -20 points for deep queues
        score -= Math.min(inferenceQueueDepth, 20);
        // Latency penalty: up to -10 points for high latency
        if (avgInferenceLatencyMs > 0) {
            score -= Math.min(avgInferenceLatencyMs / 100.0, 10);
        }
        // Network latency penalty: up to -10 points
        if (networkLatencyMs > 0) {
            score -= Math.min(networkLatencyMs / 50.0, 10);
        }
        return Math.max(0, score);
    }

    /**
     * Build the full URL for P2P communication with this peer.
     */
    public String getUrl() {
        return "http://" + hostAddress + ":" + port;
    }

    @Override
    public String toString() {
        return "MeshPeerInfo{nodeId='" + nodeId + "', host='" + hostAddress +
                ":" + port + "', region='" + region + "', status='" + status + "'}";
    }
}
