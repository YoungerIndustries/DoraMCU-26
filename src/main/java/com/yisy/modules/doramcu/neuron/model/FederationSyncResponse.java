package com.yisy.modules.doramcu.neuron.model;

import java.util.List;
import java.util.Map;

/**
 * Registry sync response from a peer coordinator.
 *
 * Contains registry state information — either summary (counts) or full (neuron list)
 * depending on the sync type requested.
 */
public class FederationSyncResponse {

    /** Region of the responding coordinator. */
    private String region;

    /** Neuron count grouped by status (ACTIVE, INACTIVE, etc.). */
    private Map<String, Integer> neuronCountByStatus;

    /** Total neuron count. */
    private int totalNeuronCount;

    /** Current ONNX model version at this coordinator. */
    private long modelVersion;

    /** Full neuron identity list — only present for FULL sync type. */
    private List<NeuronIdentity> neuronIdentities;

    public FederationSyncResponse() {
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public Map<String, Integer> getNeuronCountByStatus() {
        return neuronCountByStatus;
    }

    public void setNeuronCountByStatus(Map<String, Integer> neuronCountByStatus) {
        this.neuronCountByStatus = neuronCountByStatus;
    }

    public int getTotalNeuronCount() {
        return totalNeuronCount;
    }

    public void setTotalNeuronCount(int totalNeuronCount) {
        this.totalNeuronCount = totalNeuronCount;
    }

    public long getModelVersion() {
        return modelVersion;
    }

    public void setModelVersion(long modelVersion) {
        this.modelVersion = modelVersion;
    }

    public List<NeuronIdentity> getNeuronIdentities() {
        return neuronIdentities;
    }

    public void setNeuronIdentities(List<NeuronIdentity> neuronIdentities) {
        this.neuronIdentities = neuronIdentities;
    }
}
