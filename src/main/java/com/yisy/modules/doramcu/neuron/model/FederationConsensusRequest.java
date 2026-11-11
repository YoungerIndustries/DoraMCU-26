package com.yisy.modules.doramcu.neuron.model;

import java.util.List;

/**
 * Cross-region consensus request.
 *
 * Sent by one coordinator to its peers to gather votes from their local neurons
 * for a consensus decision that spans all regions.
 */
public class FederationConsensusRequest {

    /** Feature vector for the consensus decision. */
    private double[] features;

    /** Timeout in milliseconds for the peer to respond. */
    private long timeoutMs;

    /** Region of the requesting coordinator. */
    private String requestingRegion;

    /** Human-readable description of the decision being made. */
    private String description;

    public FederationConsensusRequest() {
    }

    public FederationConsensusRequest(double[] features, long timeoutMs, String requestingRegion) {
        this.features = features;
        this.timeoutMs = timeoutMs;
        this.requestingRegion = requestingRegion;
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

    public String getRequestingRegion() {
        return requestingRegion;
    }

    public void setRequestingRegion(String requestingRegion) {
        this.requestingRegion = requestingRegion;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
