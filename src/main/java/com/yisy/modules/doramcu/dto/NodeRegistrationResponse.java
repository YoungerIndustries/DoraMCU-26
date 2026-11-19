package com.yisy.modules.doramcu.dto;

import java.util.Map;

/**
 * DTO for node registration responses returned to devices joining the brain network.
 */
public class NodeRegistrationResponse {

    private String nodeId;
    private String nodeType;
    private String status;
    private String brainStemUrl;
    private Map<String, Object> metadata;

    public NodeRegistrationResponse() {
    }

    public NodeRegistrationResponse(String nodeId, String nodeType, String status) {
        this.nodeId = nodeId;
        this.nodeType = nodeType;
        this.status = status;
    }

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public String getNodeType() {
        return nodeType;
    }

    public void setNodeType(String nodeType) {
        this.nodeType = nodeType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getBrainStemUrl() {
        return brainStemUrl;
    }

    public void setBrainStemUrl(String brainStemUrl) {
        this.brainStemUrl = brainStemUrl;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }
}
