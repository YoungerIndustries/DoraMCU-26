package com.yisy.modules.doraneuron.protocol.mesh;

import java.io.Serializable;
import java.util.Map;

/**
 * Message for leader election in the decentralized neural network.
 * 
 * Sent when a neuron candidates itself as co-server during server downtime.
 */
public class LeaderElectionMessage extends MeshMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    private String candidateNodeId;
    private long uptime;
    private int modelVersion;
    private int connectedPeers;
    private Map<String, Object> payload;

    public LeaderElectionMessage() {
        super("LEADER_ELECTION", null);
    }

    public LeaderElectionMessage(String candidateNodeId, long uptime, int modelVersion, int connectedPeers) {
        super("LEADER_ELECTION", null);
        this.candidateNodeId = candidateNodeId;
        this.uptime = uptime;
        this.modelVersion = modelVersion;
        this.connectedPeers = connectedPeers;
    }

    public String getCandidateNodeId() {
        return candidateNodeId;
    }

    public void setCandidateNodeId(String candidateNodeId) {
        this.candidateNodeId = candidateNodeId;
    }

    public long getUptime() {
        return uptime;
    }

    public void setUptime(long uptime) {
        this.uptime = uptime;
    }

    public int getModelVersion() {
        return modelVersion;
    }

    public void setModelVersion(int modelVersion) {
        this.modelVersion = modelVersion;
    }

    public int getConnectedPeers() {
        return connectedPeers;
    }

    public void setConnectedPeers(int connectedPeers) {
        this.connectedPeers = connectedPeers;
    }

    public Map<String, Object> getPayload() {
        return payload;
    }

    public void setPayload(Map<String, Object> payload) {
        this.payload = payload;
    }
}
