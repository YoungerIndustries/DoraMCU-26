package com.yisy.modules.doraneuron.model.brainnode;

import java.util.Map;

/**
 * Status model for the Brain Node, used in REST API responses.
 */
public class BrainNodeStatus {

    private String nodeId;
    private String nodeName;
    private String nodeType;
    private boolean enabled;
    private String serialConnectionStatus;
    private boolean aiReady;
    private long currentModelVersion;
    private String piCoordinatorUrl;
    private boolean piReachable;
    private boolean autoFailoverEnabled;
    private String storageType;
    private String storagePath;
    private Map<String, Object> storageStats;
    private Map<String, Object> inferenceStats;
    private String message;

    public BrainNodeStatus() {
    }

    public BrainNodeStatus(String nodeId, String nodeName, String nodeType) {
        this.nodeId = nodeId;
        this.nodeName = nodeName;
        this.nodeType = nodeType;
        this.enabled = true;
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

    public String getNodeType() {
        return nodeType;
    }

    public void setNodeType(String nodeType) {
        this.nodeType = nodeType;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getSerialConnectionStatus() {
        return serialConnectionStatus;
    }

    public void setSerialConnectionStatus(String serialConnectionStatus) {
        this.serialConnectionStatus = serialConnectionStatus;
    }

    public boolean isAiReady() {
        return aiReady;
    }

    public void setAiReady(boolean aiReady) {
        this.aiReady = aiReady;
    }

    public long getCurrentModelVersion() {
        return currentModelVersion;
    }

    public void setCurrentModelVersion(long currentModelVersion) {
        this.currentModelVersion = currentModelVersion;
    }

    public String getPiCoordinatorUrl() {
        return piCoordinatorUrl;
    }

    public void setPiCoordinatorUrl(String piCoordinatorUrl) {
        this.piCoordinatorUrl = piCoordinatorUrl;
    }

    public boolean isPiReachable() {
        return piReachable;
    }

    public void setPiReachable(boolean piReachable) {
        this.piReachable = piReachable;
    }

    public boolean isAutoFailoverEnabled() {
        return autoFailoverEnabled;
    }

    public void setAutoFailoverEnabled(boolean autoFailoverEnabled) {
        this.autoFailoverEnabled = autoFailoverEnabled;
    }

    public String getStorageType() {
        return storageType;
    }

    public void setStorageType(String storageType) {
        this.storageType = storageType;
    }

    public String getStoragePath() {
        return storagePath;
    }

    public void setStoragePath(String storagePath) {
        this.storagePath = storagePath;
    }

    public Map<String, Object> getStorageStats() {
        return storageStats;
    }

    public void setStorageStats(Map<String, Object> storageStats) {
        this.storageStats = storageStats;
    }

    public Map<String, Object> getInferenceStats() {
        return inferenceStats;
    }

    public void setInferenceStats(Map<String, Object> inferenceStats) {
        this.inferenceStats = inferenceStats;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
