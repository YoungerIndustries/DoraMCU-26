package com.yisy.modules.doraneuron.protocol.mesh;

import java.io.Serializable;

/**
 * Base class for all P2P mesh messages between Neurons.
 *
 * Every mesh message carries a type identifier so the receiver can dispatch
 * to the correct handler without inspecting the payload structure.
 * Messages also include hop count tracking to prevent infinite propagation
 * in the mesh network.
 */
public abstract class MeshMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Message type discriminator (e.g. "PEER_DISCOVERY", "CONSENSUS_REQUEST"). */
    private String type;

    /** Sender node ID. */
    private String fromNodeId;

    /** Optional target node ID (null for broadcast). */
    private String toNodeId;

    /** Unique message ID for deduplication and response correlation. */
    private String messageId;

    /** Current hop count — incremented on each relay. */
    private int hopCount;

    /** Maximum allowed hops before message expires. */
    private int maxHops;

    /** Timestamp when the message was created (epoch ms). */
    private long timestamp;

    public MeshMessage() {
        this.timestamp = System.currentTimeMillis();
        this.hopCount = 0;
    }

    public MeshMessage(String type, String fromNodeId) {
        this.type = type;
        this.fromNodeId = fromNodeId;
        this.messageId = java.util.UUID.randomUUID().toString();
        this.timestamp = System.currentTimeMillis();
        this.hopCount = 0;
    }

    /**
     * Increment hop count and check if message should still propagate.
     *
     * @return true if the message can still propagate, false if max hops reached
     */
    public boolean incrementHop() {
        hopCount++;
        return hopCount < maxHops;
    }

    /**
     * Check if this message has exceeded the maximum hop count.
     */
    public boolean isExpired() {
        return hopCount >= maxHops;
    }

    // ── Getters and Setters ───────────────────────────────────────────────────

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getFromNodeId() {
        return fromNodeId;
    }

    public void setFromNodeId(String fromNodeId) {
        this.fromNodeId = fromNodeId;
    }

    public String getToNodeId() {
        return toNodeId;
    }

    public void setToNodeId(String toNodeId) {
        this.toNodeId = toNodeId;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public int getHopCount() {
        return hopCount;
    }

    public void setHopCount(int hopCount) {
        this.hopCount = hopCount;
    }

    public int getMaxHops() {
        return maxHops;
    }

    public void setMaxHops(int maxHops) {
        this.maxHops = maxHops;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "MeshMessage{type='" + type + "', from='" + fromNodeId +
                "', messageId='" + messageId + "', hops=" + hopCount + "/" + maxHops + '}';
    }
}
