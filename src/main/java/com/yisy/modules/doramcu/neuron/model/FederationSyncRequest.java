package com.yisy.modules.doramcu.neuron.model;

import java.util.Map;

/**
 * Registry sync request between coordinator peers.
 *
 * Sent by one coordinator to another to request registry state information.
 * Supports summary-only sync (counts) and full sync (complete neuron list).
 */
public class FederationSyncRequest {

    /** Region of the requesting coordinator. */
    private String requestingRegion;

    /** Type of sync requested. */
    private SyncType syncType = SyncType.SUMMARY;

    /**
     * Sync type determines how much data is exchanged.
     */
    public enum SyncType {
        /** Summary only: neuron counts by status, model version. */
        SUMMARY,
        /** Full sync: complete neuron identity list. */
        FULL
    }

    public FederationSyncRequest() {
    }

    public FederationSyncRequest(String requestingRegion, SyncType syncType) {
        this.requestingRegion = requestingRegion;
        this.syncType = syncType;
    }

    public String getRequestingRegion() {
        return requestingRegion;
    }

    public void setRequestingRegion(String requestingRegion) {
        this.requestingRegion = requestingRegion;
    }

    public SyncType getSyncType() {
        return syncType;
    }

    public void setSyncType(SyncType syncType) {
        this.syncType = syncType;
    }
}
