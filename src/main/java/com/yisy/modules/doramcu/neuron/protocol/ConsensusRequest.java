package com.yisy.modules.doramcu.neuron.protocol;

/**
 * Request for a distributed consensus decision across all active Neurons.
 *
 * Mirror of dora-neuron's ConsensusRequest for use by the coordinator's
 * Kafka producer without depending on the dora-neuron module.
 */
public class ConsensusRequest {

    /** The game state feature vector to evaluate (typically 64 dimensions). */
    private double[] features;

    /** Maximum time to wait for consensus votes (milliseconds). */
    private long timeoutMs;

    /** Optional: the requesting Neuron's node ID, or null if from coordinator directly. */
    private String requestingNode;

    public ConsensusRequest() {
    }

    public ConsensusRequest(double[] features, long timeoutMs) {
        this.features = features;
        this.timeoutMs = timeoutMs;
    }

    public double[] getFeatures() { return features; }
    public void setFeatures(double[] features) { this.features = features; }

    public long getTimeoutMs() { return timeoutMs; }
    public void setTimeoutMs(long timeoutMs) { this.timeoutMs = timeoutMs; }

    public String getRequestingNode() { return requestingNode; }
    public void setRequestingNode(String requestingNode) { this.requestingNode = requestingNode; }

    /**
     * Return the dimensionality of the feature vector.
     */
    public int getFeatureDim() {
        return features != null ? features.length : 0;
    }
}
