package com.yisy.modules.doraneuron.protocol;

/**
 * Request for a distributed consensus decision across all active Neurons.
 *
 * When a high-stakes AI decision is needed, the coordinator broadcasts this
 * request to all registered Neurons. Each Neuron runs local inference and
 * returns a {@link com.yisy.modules.doraneuron.protocol.NeuronVote}. The
 * coordinator then aggregates the weighted votes into a final consensus
 * decision — analogous to ensemble neural networks and collective brain
 * decision-making.
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

    /**
     * Create a new consensus request.
     *
     * @param features    the game state feature vector
     * @param timeoutMs   maximum time to wait for consensus (milliseconds)
     */
    public ConsensusRequest(double[] features, long timeoutMs) {
        this.features = features;
        this.timeoutMs = timeoutMs;
    }

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

    public String getRequestingNode() {
        return requestingNode;
    }

    public void setRequestingNode(String requestingNode) {
        this.requestingNode = requestingNode;
    }

    /**
     * Return the dimensionality of the feature vector.
     */
    public int getFeatureDim() {
        return features != null ? features.length : 0;
    }

    @Override
    public String toString() {
        return "ConsensusRequest{" +
                "featureDim=" + getFeatureDim() +
                ", timeoutMs=" + timeoutMs +
                ", requestingNode='" + requestingNode + '\'' +
                '}';
    }
}
