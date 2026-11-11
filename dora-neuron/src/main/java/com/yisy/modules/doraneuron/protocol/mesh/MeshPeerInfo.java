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
