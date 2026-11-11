package com.yisy.modules.doraneuron.protocol.unity;

import com.yisy.modules.doraneuron.model.ExperienceSample;

import java.io.Serializable;
import java.util.List;

/**
 * Batch of experience samples from a Unity client game session.
 *
 * Unity clients periodically upload collected (state, action, reward) tuples
 * to the Neuron for aggregation and eventual model retraining.
 */
public class UnityExperienceBatch implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Client ID that collected these samples. */
    private String clientId;

    /** Client type (e.g. "unity"). */
    private String clientType;

    /** ONNX model version the client was using when collecting. */
    private long clientModelVersion;

    /** List of experience samples from the game session. */
    private List<ExperienceSample> samples;

    public UnityExperienceBatch() {
    }

    public UnityExperienceBatch(String clientId, String clientType, long clientModelVersion,
                                List<ExperienceSample> samples) {
        this.clientId = clientId;
        this.clientType = clientType;
        this.clientModelVersion = clientModelVersion;
        this.samples = samples;
    }

    // ── Getters and Setters ───────────────────────────────────────────────────

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientType() {
        return clientType;
    }

    public void setClientType(String clientType) {
        this.clientType = clientType;
    }

    public long getClientModelVersion() {
        return clientModelVersion;
    }

    public void setClientModelVersion(long clientModelVersion) {
        this.clientModelVersion = clientModelVersion;
    }

    public List<ExperienceSample> getSamples() {
        return samples;
    }

    public void setSamples(List<ExperienceSample> samples) {
        this.samples = samples;
    }

    @Override
    public String toString() {
        return "UnityExperienceBatch{clientId='" + clientId + "', samples=" +
                (samples != null ? samples.size() : 0) + ", modelVersion=" + clientModelVersion + '}';
    }
}
