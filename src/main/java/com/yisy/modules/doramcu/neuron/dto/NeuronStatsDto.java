package com.yisy.modules.doramcu.neuron.dto;

import java.util.Map;

/**
 * Neuron statistics for a single region.
 */
public class NeuronStatsDto {

    /** Total neurons registered. */
    private int totalNeurons;

    /** Active neurons. */
    private int activeNeurons;

    /** Neuron count by status. */
    private Map<String, Integer> countByStatus;

    /** Highest model version among neurons. */
    private long modelVersion;

    /** Total experience samples collected. */
    private int totalExperienceSamples;

    public NeuronStatsDto() {
    }

    public int getTotalNeurons() { return totalNeurons; }
    public void setTotalNeurons(int totalNeurons) { this.totalNeurons = totalNeurons; }

    public int getActiveNeurons() { return activeNeurons; }
    public void setActiveNeurons(int activeNeurons) { this.activeNeurons = activeNeurons; }

    public Map<String, Integer> getCountByStatus() { return countByStatus; }
    public void setCountByStatus(Map<String, Integer> countByStatus) { this.countByStatus = countByStatus; }

    public long getModelVersion() { return modelVersion; }
    public void setModelVersion(long modelVersion) { this.modelVersion = modelVersion; }

    public int getTotalExperienceSamples() { return totalExperienceSamples; }
    public void setTotalExperienceSamples(int totalExperienceSamples) { this.totalExperienceSamples = totalExperienceSamples; }
}
