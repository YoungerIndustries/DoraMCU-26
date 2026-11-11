package com.yisy.modules.doraneuron.protocol.mesh;

import com.yisy.modules.doraneuron.model.ExperienceSample;

import java.io.Serializable;
import java.util.List;

/**
 * Experience synchronization message for P2P mesh.
 *
 * This message carries a batch of experience samples from one neuron
 * to its peers for distributed learning. The receiving neurons can
 * use these samples to update their local models or forward them
 * to the coordinator for centralized training.
 */
public class MeshExperienceSync extends MeshMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Batch of experience samples to share. */
    private List<ExperienceSample> samples;

    /** Total samples collected by sender since last sync. */
    private int totalCollected;

    /** Current model version of the sender. */
    private long modelVersion;

    /** Flag indicating if this is a request for samples from peers. */
    private boolean isRequest;

    public MeshExperienceSync() {
        super("MESH_EXPERIENCE_SYNC", null);
    }

    public MeshExperienceSync(String fromNodeId, List<ExperienceSample> samples,
                               int totalCollected, long modelVersion) {
        super("MESH_EXPERIENCE_SYNC", fromNodeId);
        this.samples = samples;
        this.totalCollected = totalCollected;
        this.modelVersion = modelVersion;
        this.isRequest = false;
    }

    /**
     * Create a request for experience samples from peers.
     */
    public static MeshExperienceSync createRequest(String fromNodeId) {
        MeshExperienceSync sync = new MeshExperienceSync();
        sync.setType("MESH_EXPERIENCE_SYNC");
        sync.setFromNodeId(fromNodeId);
        sync.setIsRequest(true);
        sync.setMaxHops(2);
        return sync;
    }

    // ── Getters and Setters ───────────────────────────────────────────────────

    public List<ExperienceSample> getSamples() {
        return samples;
    }

    public void setSamples(List<ExperienceSample> samples) {
        this.samples = samples;
    }

    public int getTotalCollected() {
        return totalCollected;
    }

    public void setTotalCollected(int totalCollected) {
        this.totalCollected = totalCollected;
    }

    public long getModelVersion() {
        return modelVersion;
    }

    public void setModelVersion(long modelVersion) {
        this.modelVersion = modelVersion;
    }

    public boolean isIsRequest() {
        return isRequest;
    }

    public void setIsRequest(boolean isRequest) {
        this.isRequest = isRequest;
    }

    @Override
    public String toString() {
        return "MeshExperienceSync{samples=" + (samples != null ? samples.size() : 0) +
                ", totalCollected=" + totalCollected + ", isRequest=" + isRequest +
                ", " + super.toString() + '}';
    }
}
