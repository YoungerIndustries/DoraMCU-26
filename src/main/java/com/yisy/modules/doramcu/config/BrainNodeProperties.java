package com.yisy.modules.doramcu.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties for Brain Node mode.
 * <p>
 * When a PC server runs as a Brain Node, it registers with the Pi Brain Stem
 * and sends periodic heartbeats. This class holds all the configuration needed
 * for node registration and synchronization.
 * </p>
 */
@Configuration
@ConfigurationProperties(prefix = "doramcu.brain-node")
public class BrainNodeProperties {

    /**
     * Enable brain node mode. When true, this server will register with the brain stem.
     */
    private boolean enabled = false;

    /**
     * URL of the Brain Stem server (e.g., http://192.168.50.147:8080).
     */
    private String brainStemUrl;

    /**
     * Node type: BrainCell (desktop), Node (tablet), Neuron (PDA).
     * Default: BrainCell for PC servers.
     */
    private String nodeType = "BrainCell";

    /**
     * Unique device identifier. Auto-generated if not set.
     */
    private String deviceId;

    /**
     * Heartbeat interval in seconds. Default: 60 seconds.
     */
    private long heartbeatIntervalSeconds = 60;

    /**
     * CPU cores to report to brain stem. Auto-detected if 0.
     */
    private int cpuCores = 0;

    /**
     * RAM in MB to report to brain stem. Auto-detected if 0.
     */
    private int ramMb = 0;

    /**
     * GPU tier: none, basic, mid, high. Default: none.
     */
    private String gpuTier = "none";

    /**
     * Local IP address to report to brain stem. Auto-detected if empty.
     */
    private String ipAddress = "";

    /**
     * Local port to report to brain stem. Defaults to server.port.
     */
    private int port = 8080;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getBrainStemUrl() {
        return brainStemUrl;
    }

    public void setBrainStemUrl(String brainStemUrl) {
        this.brainStemUrl = brainStemUrl;
    }

    public String getNodeType() {
        return nodeType;
    }

    public void setNodeType(String nodeType) {
        this.nodeType = nodeType;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public long getHeartbeatIntervalSeconds() {
        return heartbeatIntervalSeconds;
    }

    public void setHeartbeatIntervalSeconds(long heartbeatIntervalSeconds) {
        this.heartbeatIntervalSeconds = heartbeatIntervalSeconds;
    }

    public int getCpuCores() {
        return cpuCores;
    }

    public void setCpuCores(int cpuCores) {
        this.cpuCores = cpuCores;
    }

    public int getRamMb() {
        return ramMb;
    }

    public void setRamMb(int ramMb) {
        this.ramMb = ramMb;
    }

    public String getGpuTier() {
        return gpuTier;
    }

    public void setGpuTier(String gpuTier) {
        this.gpuTier = gpuTier;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
