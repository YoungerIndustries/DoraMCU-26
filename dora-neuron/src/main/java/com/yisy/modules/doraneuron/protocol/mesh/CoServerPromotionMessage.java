package com.yisy.modules.doraneuron.protocol.mesh;

import java.io.Serializable;

/**
 * Message for co-server promotion notification.
 * 
 * Sent when a neuron is promoted to co-server, notifying all peers.
 */
public class CoServerPromotionMessage extends MeshMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    private String nodeId;
    private String nodeName;
    private String hostAddress;
    private int port;

    public CoServerPromotionMessage() {
        super("CO_SERVER_PROMOTED", null);
    }

    public CoServerPromotionMessage(String nodeId, String nodeName, String hostAddress, int port) {
        super("CO_SERVER_PROMOTED", null);
        this.nodeId = nodeId;
        this.nodeName = nodeName;
        this.hostAddress = hostAddress;
        this.port = port;
    }

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public String getHostAddress() {
        return hostAddress;
    }

    public void setHostAddress(String hostAddress) {
        this.hostAddress = hostAddress;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
