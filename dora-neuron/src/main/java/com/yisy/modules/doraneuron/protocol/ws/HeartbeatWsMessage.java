package com.yisy.modules.doraneuron.protocol.ws;

/**
 * WebSocket heartbeat message sent from Neuron to Coordinator.
 *
 * Replaces the HTTP heartbeat polling with a real-time signal that
 * keeps the WebSocket connection alive and updates the neuron's
 * registry entry.
 */
public class HeartbeatWsMessage extends NeuronWsMessage {

    private static final long serialVersionUID = 1L;

    public static final String TYPE = "HEARTBEAT";

    /** Current status of the neuron. */
    private String status;

    /** Current ONNX model version. */
    private long currentModelVersion;

    /** Number of experience samples collected since last flush. */
    private int experienceCount;

    public HeartbeatWsMessage() {
        super(TYPE, null);
    }

    public HeartbeatWsMessage(String nodeId, String status, long currentModelVersion, int experienceCount) {
        super(TYPE, nodeId);
        this.status = status;
        this.currentModelVersion = currentModelVersion;
        this.experienceCount = experienceCount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getCurrentModelVersion() {
        return currentModelVersion;
    }

    public void setCurrentModelVersion(long currentModelVersion) {
        this.currentModelVersion = currentModelVersion;
    }

    public int getExperienceCount() {
        return experienceCount;
    }

    public void setExperienceCount(int experienceCount) {
        this.experienceCount = experienceCount;
    }
}
