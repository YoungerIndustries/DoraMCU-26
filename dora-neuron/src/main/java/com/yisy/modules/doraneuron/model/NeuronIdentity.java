package com.yisy.modules.doraneuron.model;

/**
 * Self-description metadata for a Neuron node.
 *
 * Sent to the coordinator during registration and heartbeats so the cluster
 * knows each Neuron's identity, capabilities, and health.
 */
public class NeuronIdentity {

    private String nodeId;
    private String nodeName;
    private String region;
    private String hostAddress;
    private int port;
    private long registeredAt;
    private long lastHeartbeat;
    private NeuronStatus status;
    private double voteWeight;
    private long currentModelVersion;
    private int experienceCount;

    public NeuronIdentity() {
    }

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

    public long getRegisteredAt() {
        return registeredAt;
    }

    public void setRegisteredAt(long registeredAt) {
        this.registeredAt = registeredAt;
    }

    public long getLastHeartbeat() {
        return lastHeartbeat;
    }

    public void setLastHeartbeat(long lastHeartbeat) {
        this.lastHeartbeat = lastHeartbeat;
    }

    public NeuronStatus getStatus() {
        return status;
    }

    public void setStatus(NeuronStatus status) {
        this.status = status;
    }

    public double getVoteWeight() {
        return voteWeight;
    }

    public void setVoteWeight(double voteWeight) {
        this.voteWeight = voteWeight;
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

    /**
     * Build the full URL for this Neuron (e.g. "http://10.0.0.5:8090").
     */
    public String getUrl() {
        return "http://" + hostAddress + ":" + port;
    }

    @Override
    public String toString() {
        return "NeuronIdentity{" +
                "nodeId='" + nodeId + '\'' +
                ", nodeName='" + nodeName + '\'' +
                ", region='" + region + '\'' +
                ", status=" + status +
                ", url='" + getUrl() + '\'' +
                '}';
    }
}
