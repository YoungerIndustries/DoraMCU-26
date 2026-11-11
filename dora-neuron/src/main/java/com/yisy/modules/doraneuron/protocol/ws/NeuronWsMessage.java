package com.yisy.modules.doraneuron.protocol.ws;

import java.io.Serializable;

/**
 * Base class for all WebSocket messages between Neurons and the Coordinator.
 *
 * Every message carries a type identifier so the receiver can dispatch
 * to the correct handler without inspecting the payload structure.
 */
public abstract class NeuronWsMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Message type discriminator (e.g. "HEARTBEAT", "MODEL_SYNC"). */
    private String type;

    /** Sender node ID (null for coordinator-originated broadcast messages). */
    private String nodeId;

    /** Timestamp when the message was created (epoch ms). */
    private long timestamp;

    public NeuronWsMessage() {
        this.timestamp = System.currentTimeMillis();
    }

    public NeuronWsMessage(String type, String nodeId) {
        this.type = type;
        this.nodeId = nodeId;
        this.timestamp = System.currentTimeMillis();
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "NeuronWsMessage{type='" + type + "', nodeId='" + nodeId + "', timestamp=" + timestamp + '}';
    }
}
