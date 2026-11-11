package com.yisy.modules.doramcu.neuron.dto;

import java.time.Instant;

/**
 * Health summary for a single federation peer coordinator.
 */
public class PeerHealthDto {

    /** Region identifier. */
    private String region;

    /** Peer coordinator URL. */
    private String url;

    /** Connection status (ACTIVE, UNREACHABLE, SYNCING, UNKNOWN). */
    private String status;

    /** Number of neurons registered at this peer. */
    private int neuronCount;

    /** Model version deployed at this peer. */
    private long modelVersion;

    /** Model version drift from this coordinator (positive = peer is ahead, negative = peer is behind). */
    private long modelVersionDrift;

    /** Last successful contact timestamp. */
    private Instant lastContact;

    /** Seconds since last contact (0 if never contacted). */
    private long secondsSinceContact;

    /** Whether this peer is reachable. */
    private boolean reachable;

    public PeerHealthDto() {
    }

    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public int getNeuronCount() { return neuronCount; }
    public void setNeuronCount(int neuronCount) { this.neuronCount = neuronCount; }

    public long getModelVersion() { return modelVersion; }
    public void setModelVersion(long modelVersion) { this.modelVersion = modelVersion; }

    public long getModelVersionDrift() { return modelVersionDrift; }
    public void setModelVersionDrift(long modelVersionDrift) { this.modelVersionDrift = modelVersionDrift; }

    public Instant getLastContact() { return lastContact; }
    public void setLastContact(Instant lastContact) { this.lastContact = lastContact; }

    public long getSecondsSinceContact() { return secondsSinceContact; }
    public void setSecondsSinceContact(long secondsSinceContact) { this.secondsSinceContact = secondsSinceContact; }

    public boolean isReachable() { return reachable; }
    public void setReachable(boolean reachable) { this.reachable = reachable; }
}
