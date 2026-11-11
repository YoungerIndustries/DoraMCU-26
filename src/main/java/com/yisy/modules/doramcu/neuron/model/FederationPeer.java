package com.yisy.modules.doramcu.neuron.model;

import java.time.Instant;

/**
 * Peer coordinator metadata for Multi-Region Federation.
 *
 * Represents a remote DoraMCU coordinator instance in another geographic region.
 * Each coordinator maintains a list of its peers for federation sync,
 * cross-region consensus, and experience forwarding.
 */
public class FederationPeer {

    /** Unique region identifier (e.g. "us-east", "eu-west", "apac"). */
    private String region;

    /** Base URL of the peer coordinator's REST API. */
    private String url;

    /** Current connection status. */
    private Status status = Status.UNKNOWN;

    /** Number of neurons registered with this peer coordinator. */
    private int neuronCount;

    /** Last successful contact timestamp. */
    private Instant lastContact;

    /** Current ONNX model version deployed at this peer. */
    private long modelVersion;

    /**
     * Connection status of a peer coordinator.
     */
    public enum Status {
        UNKNOWN,
        ACTIVE,
        UNREACHABLE,
        SYNCING
    }

    public FederationPeer() {
    }

    public FederationPeer(String region, String url) {
        this.region = region;
        this.url = url;
        this.status = Status.UNKNOWN;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public int getNeuronCount() {
        return neuronCount;
    }

    public void setNeuronCount(int neuronCount) {
        this.neuronCount = neuronCount;
    }

    public Instant getLastContact() {
        return lastContact;
    }

    public void setLastContact(Instant lastContact) {
        this.lastContact = lastContact;
    }

    public long getModelVersion() {
        return modelVersion;
    }

    public void setModelVersion(long modelVersion) {
        this.modelVersion = modelVersion;
    }

    @Override
    public String toString() {
        return "FederationPeer{" +
                "region='" + region + '\'' +
                ", url='" + url + '\'' +
                ", status=" + status +
                ", neuronCount=" + neuronCount +
                ", modelVersion=" + modelVersion +
                '}';
    }
}
