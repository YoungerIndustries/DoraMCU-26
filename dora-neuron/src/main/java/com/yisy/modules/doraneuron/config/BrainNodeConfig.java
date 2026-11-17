package com.yisy.modules.doraneuron.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for a DoraMCU Brain Node running on a desktop computer.
 * <p>
 * The Brain Node extends the base Neuron configuration with desktop-specific settings
 * for enhanced AI inference, persistent local storage, and USB serial communication
 * with the Raspberry Pi.
 *
 * All properties are prefixed with {@code brainnode} in application.properties.
 */
@ConfigurationProperties(prefix = "brainnode")
public class BrainNodeConfig {

    // ── Identity ──────────────────────────────────────────────────────────────

    /** Node type identifier for Brain Node profile. */
    private String nodeType = "BRAIN_NODE";

    /** Whether the Brain Node is enabled. */
    private boolean enabled = true;

    // ── Raspberry Pi connection ───────────────────────────────────────────────

    /** URL of the Raspberry Pi acting as the primary coordinator. */
    private String piCoordinatorUrl = "http://localhost:8084";

    /** Whether to use USB serial as primary communication with Pi. */
    private boolean serialEnabled = true;

    /** Auto-detect the Pi's USB serial port. */
    private boolean serialPortAutoDetect = true;

    /** Manual serial port override (e.g. "COM4"). */
    private String serialPort;

    /** Baud rate for serial communication with Pi. */
    private int serialBaudRate = 115200;

    /** Heartbeat interval in milliseconds for serial connection monitoring. */
    private int serialHeartbeatInterval = 10000;

    // ── AI Inference ──────────────────────────────────────────────────────────

    /** Number of threads for AI inference (desktop has more CPU cores). */
    private int inferenceThreads = 8;

    /** Size class of ONNX models to load. */
    private String aiModelSize = "large";

    /** Path to larger ONNX models for desktop Brain Node. */
    private String aiModelPath = "models/brain-node/";

    /** Confidence threshold for AI decisions (higher for desktop). */
    private double confidenceThreshold = 0.85;

    /** Whether to enable GPU acceleration if available. */
    private boolean gpuAccelerationEnabled = false;

    // ── Persistent Storage ────────────────────────────────────────────────────

    /** Type of local storage: H2, SQLite. */
    private String storageType = "H2";

    /** File system path for local storage files. */
    private String storagePath = "backups/brain-node/";

    /** Maximum size of local experience buffer. */
    private int experienceBufferSize = 5000;

    /** Maximum size of local data queue for sync. */
    private int dataQueueMaxSize = 10000;

    /** Whether to cache AI models locally. */
    private boolean aiModelCacheEnabled = true;

    // ── Resource Limits ───────────────────────────────────────────────────────

    /** Maximum CPU usage percentage to avoid impacting desktop. */
    private int maxCpuUsagePercent = 70;

    /** Maximum memory usage in MB for Brain Node operations. */
    private int maxMemoryMb = 4096;

    // ── Failover ──────────────────────────────────────────────────────────────

    /** Timeout in milliseconds before considering Pi unreachable. */
    private int piHealthCheckTimeout = 30000;

    /** Interval in milliseconds between Pi health checks. */
    private int piHealthCheckInterval = 10000;

    /** Whether to automatically take over as co-server when Pi is unreachable. */
    private boolean autoFailoverEnabled = true;

    // ── Getters and Setters ───────────────────────────────────────────────────

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

    public String getPiCoordinatorUrl() {
        return piCoordinatorUrl;
    }

    public void setPiCoordinatorUrl(String piCoordinatorUrl) {
        this.piCoordinatorUrl = piCoordinatorUrl;
    }

    public boolean isSerialEnabled() {
        return serialEnabled;
    }

    public void setSerialEnabled(boolean serialEnabled) {
        this.serialEnabled = serialEnabled;
    }

    public boolean isSerialPortAutoDetect() {
        return serialPortAutoDetect;
    }

    public void setSerialPortAutoDetect(boolean serialPortAutoDetect) {
        this.serialPortAutoDetect = serialPortAutoDetect;
    }

    public String getSerialPort() {
        return serialPort;
    }

    public void setSerialPort(String serialPort) {
        this.serialPort = serialPort;
    }

    public int getSerialBaudRate() {
        return serialBaudRate;
    }

    public void setSerialBaudRate(int serialBaudRate) {
        this.serialBaudRate = serialBaudRate;
    }

    public int getSerialHeartbeatInterval() {
        return serialHeartbeatInterval;
    }

    public void setSerialHeartbeatInterval(int serialHeartbeatInterval) {
        this.serialHeartbeatInterval = serialHeartbeatInterval;
    }

    public int getInferenceThreads() {
        return inferenceThreads;
    }

    public void setInferenceThreads(int inferenceThreads) {
        this.inferenceThreads = inferenceThreads;
    }

    public String getAiModelSize() {
        return aiModelSize;
    }

    public void setAiModelSize(String aiModelSize) {
        this.aiModelSize = aiModelSize;
    }

    public String getAiModelPath() {
        return aiModelPath;
    }

    public void setAiModelPath(String aiModelPath) {
        this.aiModelPath = aiModelPath;
    }

    public double getConfidenceThreshold() {
        return confidenceThreshold;
    }

    public void setConfidenceThreshold(double confidenceThreshold) {
        this.confidenceThreshold = confidenceThreshold;
    }

    public boolean isGpuAccelerationEnabled() {
        return gpuAccelerationEnabled;
    }

    public void setGpuAccelerationEnabled(boolean gpuAccelerationEnabled) {
        this.gpuAccelerationEnabled = gpuAccelerationEnabled;
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

    public int getExperienceBufferSize() {
        return experienceBufferSize;
    }

    public void setExperienceBufferSize(int experienceBufferSize) {
        this.experienceBufferSize = experienceBufferSize;
    }

    public int getDataQueueMaxSize() {
        return dataQueueMaxSize;
    }

    public void setDataQueueMaxSize(int dataQueueMaxSize) {
        this.dataQueueMaxSize = dataQueueMaxSize;
    }

    public boolean isAiModelCacheEnabled() {
        return aiModelCacheEnabled;
    }

    public void setAiModelCacheEnabled(boolean aiModelCacheEnabled) {
        this.aiModelCacheEnabled = aiModelCacheEnabled;
    }

    public int getMaxCpuUsagePercent() {
        return maxCpuUsagePercent;
    }

    public void setMaxCpuUsagePercent(int maxCpuUsagePercent) {
        this.maxCpuUsagePercent = maxCpuUsagePercent;
    }

    public int getMaxMemoryMb() {
        return maxMemoryMb;
    }

    public void setMaxMemoryMb(int maxMemoryMb) {
        this.maxMemoryMb = maxMemoryMb;
    }

    public int getPiHealthCheckTimeout() {
        return piHealthCheckTimeout;
    }

    public void setPiHealthCheckTimeout(int piHealthCheckTimeout) {
        this.piHealthCheckTimeout = piHealthCheckTimeout;
    }

    public int getPiHealthCheckInterval() {
        return piHealthCheckInterval;
    }

    public void setPiHealthCheckInterval(int piHealthCheckInterval) {
        this.piHealthCheckInterval = piHealthCheckInterval;
    }

    public boolean isAutoFailoverEnabled() {
        return autoFailoverEnabled;
    }

    public void setAutoFailoverEnabled(boolean autoFailoverEnabled) {
        this.autoFailoverEnabled = autoFailoverEnabled;
    }
}
