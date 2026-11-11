package com.yisy.modules.doramcu.neuron.dto;

import java.time.Instant;

/**
 * Record of a single sync operation between coordinators.
 */
public class SyncHistoryDto {

    /** Peer region that was synced with. */
    private String region;

    /** Sync type (SUMMARY or FULL). */
    private String syncType;

    /** Whether the sync was successful. */
    private boolean success;

    /** Error message if sync failed. */
    private String errorMessage;

    /** Timestamp of the sync operation. */
    private Instant timestamp;

    /** Duration of the sync in milliseconds. */
    private long durationMs;

    /** Neuron count reported by peer (0 if sync failed). */
    private int neuronCount;

    /** Model version reported by peer (0 if sync failed). */
    private long modelVersion;

    public SyncHistoryDto() {
    }

    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }

    public String getSyncType() { return syncType; }
    public void setSyncType(String syncType) { this.syncType = syncType; }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }

    public long getDurationMs() { return durationMs; }
    public void setDurationMs(long durationMs) { this.durationMs = durationMs; }

    public int getNeuronCount() { return neuronCount; }
    public void setNeuronCount(int neuronCount) { this.neuronCount = neuronCount; }

    public long getModelVersion() { return modelVersion; }
    public void setModelVersion(long modelVersion) { this.modelVersion = modelVersion; }
}
