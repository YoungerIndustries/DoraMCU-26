package com.yisy.modules.doraneuron.protocol;

import com.yisy.modules.doraneuron.model.ExperienceSample;

import java.util.List;

/**
 * Batch of experience samples sent from a Neuron to the coordinator.
 *
 * When a Neuron's local experience buffer reaches capacity or the flush
 * interval elapses, the buffered samples are wrapped in this protocol
 * message and submitted to the coordinator via
 * {@code POST /api/v1/neuron/experience/batch}.
 */
public class ExperienceBatch {

    /** Source Neuron node ID. */
    private String nodeId;

    /** List of experience samples in this batch. */
    private List<ExperienceSample> samples;

    /** Total number of samples collected by this Neuron since startup (cumulative). */
    private long totalCollected;

    public ExperienceBatch() {
    }

    /**
     * Create a new experience batch.
     *
     * @param nodeId         the source Neuron's node ID
     * @param samples        the list of experience samples
     * @param totalCollected cumulative count of all samples collected
     */
    public ExperienceBatch(String nodeId, List<ExperienceSample> samples, long totalCollected) {
        this.nodeId = nodeId;
        this.samples = samples;
        this.totalCollected = totalCollected;
    }

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public List<ExperienceSample> getSamples() {
        return samples;
    }

    public void setSamples(List<ExperienceSample> samples) {
        this.samples = samples;
    }

    public long getTotalCollected() {
        return totalCollected;
    }

    public void setTotalCollected(long totalCollected) {
        this.totalCollected = totalCollected;
    }

    /**
     * Return the number of samples in this batch.
     */
    public int getSize() {
        return samples != null ? samples.size() : 0;
    }

    @Override
    public String toString() {
        return "ExperienceBatch{" +
                "nodeId='" + nodeId + '\'' +
                ", size=" + getSize() +
                ", totalCollected=" + totalCollected +
                '}';
    }
}
