package com.yisy.modules.doramcu.neuron.model;

import java.util.List;
import java.util.Map;

/**
 * Experience batch for cross-region forwarding.
 *
 * Sent by peer coordinators to the primary coordinator to contribute
 * experience samples for centralized model training.
 */
public class FederationExperienceBatch {

    /** Source region of the experience samples. */
    private String sourceRegion;

    /** List of experience samples. Each sample is a map of feature/value pairs. */
    private List<Map<String, Object>> samples;

    /** Timestamp when the batch was created. */
    private long createdAt;

    public FederationExperienceBatch() {
    }

    public FederationExperienceBatch(String sourceRegion, List<Map<String, Object>> samples) {
        this.sourceRegion = sourceRegion;
        this.samples = samples;
        this.createdAt = System.currentTimeMillis();
    }

    public String getSourceRegion() {
        return sourceRegion;
    }

    public void setSourceRegion(String sourceRegion) {
        this.sourceRegion = sourceRegion;
    }

    public List<Map<String, Object>> getSamples() {
        return samples;
    }

    public void setSamples(List<Map<String, Object>> samples) {
        this.samples = samples;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Number of samples in this batch.
     */
    public int getSampleCount() {
        return samples != null ? samples.size() : 0;
    }
}
