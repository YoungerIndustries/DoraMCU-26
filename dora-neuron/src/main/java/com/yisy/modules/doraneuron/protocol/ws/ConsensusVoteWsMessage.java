package com.yisy.modules.doraneuron.protocol.ws;

/**
 * WebSocket consensus vote message for distributed decision-making.
 *
 * Two directions:
 * 1. Coordinator → Neuron: ConsensusVoteRequest asking the neuron to vote
 * 2. Neuron → Coordinator: ConsensusVoteResponse with the neuron's local prediction
 */
public class ConsensusVoteWsMessage extends NeuronWsMessage {

    private static final long serialVersionUID = 1L;

    public static final String TYPE_REQUEST = "CONSENSUS_VOTE_REQUEST";
    public static final String TYPE_RESPONSE = "CONSENSUS_VOTE_RESPONSE";

    /** Feature vector for the game state being voted on. */
    private double[] features;

    /** Timeout in milliseconds for the vote. */
    private long timeoutMs;

    /** Unique request ID to correlate requests with responses. */
    private String requestId;

    // ── Response-only fields ──

    /** Predicted action probabilities (response only). */
    private double[] probabilities;

    /** Selected action index (response only). */
    private int actionIndex;

    /** Confidence score (response only). */
    private double confidence;

    /** Vote weight for this neuron (response only). */
    private double voteWeight;

    public ConsensusVoteWsMessage() {
        super(TYPE_REQUEST, null);
    }

    /** Create a vote request from the coordinator. */
    public ConsensusVoteWsMessage(String requestId, double[] features, long timeoutMs) {
        super(TYPE_REQUEST, null);
        this.requestId = requestId;
        this.features = features;
        this.timeoutMs = timeoutMs;
    }

    /** Create a vote response from a neuron. */
    public ConsensusVoteWsMessage(String nodeId, String requestId,
                                   double[] probabilities, int actionIndex,
                                   double confidence, double voteWeight) {
        super(TYPE_RESPONSE, nodeId);
        this.requestId = requestId;
        this.probabilities = probabilities;
        this.actionIndex = actionIndex;
        this.confidence = confidence;
        this.voteWeight = voteWeight;
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

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public double[] getProbabilities() {
        return probabilities;
    }

    public void setProbabilities(double[] probabilities) {
        this.probabilities = probabilities;
    }

    public int getActionIndex() {
        return actionIndex;
    }

    public void setActionIndex(int actionIndex) {
        this.actionIndex = actionIndex;
    }

    public double getConfidence() {
        return confidence;
    }

    public void setConfidence(double confidence) {
        this.confidence = confidence;
    }

    public double getVoteWeight() {
        return voteWeight;
    }

    public void setVoteWeight(double voteWeight) {
        this.voteWeight = voteWeight;
    }

    /** Check if this is a request message. */
    public boolean isRequest() {
        return TYPE_REQUEST.equals(getType());
    }

    /** Check if this is a response message. */
    public boolean isResponse() {
        return TYPE_RESPONSE.equals(getType());
    }
}
