package com.yisy.modules.doraneuron.protocol.unity;

import java.io.Serializable;
import java.util.List;

/**
 * Request from a Unity client asking the Neuron for AI assistance.
 *
 * The client sends the current game state feature vector and optionally
 * requests a consensus decision from the Neuron network.
 */
public class AiAssistRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Client ID for tracking. */
    private String clientId;

    /** Game state feature vector (64 dimensions matching Unity client). */
    private float[] features;

    /** If true, request consensus from the neuron network; otherwise local inference only. */
    private boolean useConsensus;

    /** Optional hint about the game context (e.g. "combat", "exploration", "economy"). */
    private String context;

    /** List of available actions the client can take. */
    private List<String> availableActions;

    public AiAssistRequest() {
    }

    public AiAssistRequest(String clientId, float[] features, boolean useConsensus) {
        this.clientId = clientId;
        this.features = features;
        this.useConsensus = useConsensus;
    }

    // ── Getters and Setters ───────────────────────────────────────────────────

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public float[] getFeatures() {
        return features;
    }

    public void setFeatures(float[] features) {
        this.features = features;
    }

    public boolean isUseConsensus() {
        return useConsensus;
    }

    public void setUseConsensus(boolean useConsensus) {
        this.useConsensus = useConsensus;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public List<String> getAvailableActions() {
        return availableActions;
    }

    public void setAvailableActions(List<String> availableActions) {
        this.availableActions = availableActions;
    }

    @Override
    public String toString() {
        return "AiAssistRequest{clientId='" + clientId + "', features=" +
                (features != null ? features.length : 0) + ", useConsensus=" + useConsensus + '}';
    }
}
