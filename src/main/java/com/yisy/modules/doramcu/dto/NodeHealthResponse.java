package com.yisy.modules.doramcu.dto;

/**
 * DTO for node health check responses.
 */
public class NodeHealthResponse {

    private String nodeId;
    private boolean healthy;
    private String status;
    private long lastHeartbeat;

    public NodeHealthResponse() {
    }

    public NodeHealthResponse(String nodeId, boolean healthy, String status, long lastHeartbeat) {
        this.nodeId = nodeId;
        this.healthy = healthy;
        this.status = status;
        this.lastHeartbeat = lastHeartbeat;
    }

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public boolean isHealthy() {
        return healthy;
    }

    public void setHealthy(boolean healthy) {
        this.healthy = healthy;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getLastHeartbeat() {
        return lastHeartbeat;
    }

    public void setLastHeartbeat(long lastHeartbeat) {
        this.lastHeartbeat = lastHeartbeat;
    }
}
