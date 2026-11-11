package com.yisy.modules.doraneuron.protocol.mesh;

import java.io.Serializable;
import java.util.List;

/**
 * Request for decentralized consensus voting via P2P mesh.
 *
 * This message is broadcast to all connected peers, asking them to vote
 * on a game state feature vector. Each peer runs local inference and
 * responds with a MeshConsensusResponse.
 */
public class MeshConsensusRequest extends MeshMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Game state feature vector for inference. */
    private double[] features;

    /** Timeout for collecting votes (milliseconds). */
    private long timeoutMs;

    /** List of known peer node IDs to include in voting. */
    private List<String> peerNodeIds;

    public MeshConsensusRequest() {
        super("MESH_CONSENSUS_REQUEST", null);
    }

    public MeshConsensusRequest(String fromNodeId, double[] features, long timeoutMs) {
        super("MESH_CONSENSUS_REQUEST", fromNodeId);
        this.features = features;
        this.timeoutMs = timeoutMs;
    }

    // ── Getters and Setters ───────────────────────────────────────────────────

    public double[] getFeatures() {
        return features;
    }

    public void setFeatures(double[] features) {
        this.features = features;
    }

    public long getTimeoutMs() {
        return timeoutMs;
    }

    public void setTimeoutMs(long timeoutMs) {
        this.timeoutMs = timeoutMs;
    }

    public List<String> getPeerNodeIds() {
        return peerNodeIds;
    }

    public void setPeerNodeIds(List<String> peerNodeIds) {
        this.peerNodeIds = peerNodeIds;
    }

    @Override
    public String toString() {
        return "MeshConsensusRequest{features=" + (features != null ? features.length : 0) +
                ", timeoutMs=" + timeoutMs + ", " + super.toString() + '}';
    }
}
