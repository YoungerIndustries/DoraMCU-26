package com.yisy.modules.doraneuron.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for a DoraMCU Neuron node.
 *
 * All properties are prefixed with {@code neuron} in application.properties.
 */
@ConfigurationProperties(prefix = "neuron")
public class NeuronConfig {

    // ── Identity ──────────────────────────────────────────────────────────────

    /** Unique node ID; auto-generated UUID if not set. */
    private String nodeId;

    /** Human-readable name for this Neuron. */
    private String nodeName;

    /** Geographic region tag (e.g. "us-east", "eu-west"). */
    private String nodeRegion = "default";

    // ── Coordinator connection ────────────────────────────────────────────────

    /** Base URL of the DoraMCU game server that hosts the Neuron Coordinator. */
    private String coordinatorUrl = "http://localhost:8084";

    /** Interval between heartbeat signals (milliseconds). */
    private int heartbeatIntervalMs = 15000;

    /** Heartbeat timeout (milliseconds). */
    private int heartbeatTimeoutMs = 5000;

    // ── Satellite AI (Phase 2+) ───────────────────────────────────────────────

    /** Local ONNX model cache path. */
    private String onnxModelPath;

    /** Number of threads for inference. */
    private int inferenceThreads = 2;

    /** Minimum confidence threshold for AI decisions. */
    private double confidenceThreshold = 0.6;

    // ── Experience collection (Phase 3+) ──────────────────────────────────────

    /** Local experience buffer size before flush. */
    private int experienceBufferSize = 500;

    /** Periodic flush interval (milliseconds). */
    private int experienceFlushIntervalMs = 30000;

    // ── Web portal ────────────────────────────────────────────────────────────

    /** Target game server URL for redirect or proxy. */
    private String gameServerUrl;

    /** Enable transparent reverse proxy mode; otherwise HTTP redirect. */
    private boolean enableGameProxy = true;

    /** Title displayed on the landing page. */
    private String portalTitle = "DoraMCU Gateway";

    // ── Consensus participation (Phase 4+) ────────────────────────────────────

    /** Enable inter-neuron consensus voting. */
    private boolean enableConsensus = true;

    /** Consensus request timeout (milliseconds). */
    private int consensusTimeoutMs = 5000;

    /** Weight for this neuron's votes in consensus. */
    private double voteWeight = 1.0;

    // ── P2P Mesh Network (Phase 9+) ───────────────────────────────────────────

    /** Enable direct neuron-to-neuron P2P mesh communication. */
    private boolean meshEnabled = true;

    /** Maximum number of direct peer connections per neuron. */
    private int meshMaxPeers = 5;

    /** Interval for peer discovery and topology updates (milliseconds). */
    private int meshPeerDiscoveryIntervalMs = 30000;

    /** Timeout for peer health checks (milliseconds). */
    private int meshPeerTimeoutMs = 10000;

    /** Enable decentralized consensus via P2P mesh. */
    private boolean meshConsensusEnabled = true;

    /** Enable experience sharing via P2P mesh. */
    private boolean meshExperienceSyncEnabled = true;

    /** Maximum hop count for mesh message propagation. */
    private int meshMaxHops = 3;

    /** Port offset for P2P communication (base port + offset). */
    private int meshPortOffset = 100;

    // ── Federation Failover (Phase 16+) ───────────────────────────────────────

    /** Comma-separated list of fallback coordinator URLs for failover. */
    private String coordinatorFallbackUrls = "";

    /** Interval for checking primary coordinator availability (milliseconds). */
    private int failoverCheckIntervalMs = 30000;

    /** Enable automatic failover to fallback coordinators. */
    private boolean failoverEnabled = true;

    /**
     * Parse the comma-separated coordinatorFallbackUrls into a list.
     */
    public java.util.List<String> getCoordinatorFallbackUrlList() {
        java.util.List<String> urls = new java.util.ArrayList<>();
        if (coordinatorFallbackUrls != null && !coordinatorFallbackUrls.isBlank()) {
            for (String url : coordinatorFallbackUrls.split(",")) {
                String trimmed = url.trim();
                if (!trimmed.isEmpty()) {
                    urls.add(trimmed);
                }
            }
        }
        return urls;
    }

    // ── Getters and Setters ───────────────────────────────────────────────────

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

    public String getNodeRegion() {
        return nodeRegion;
    }

    public void setNodeRegion(String nodeRegion) {
        this.nodeRegion = nodeRegion;
    }

    public String getCoordinatorUrl() {
        return coordinatorUrl;
    }

    public void setCoordinatorUrl(String coordinatorUrl) {
        this.coordinatorUrl = coordinatorUrl;
    }

    public int getHeartbeatIntervalMs() {
        return heartbeatIntervalMs;
    }

    public void setHeartbeatIntervalMs(int heartbeatIntervalMs) {
        this.heartbeatIntervalMs = heartbeatIntervalMs;
    }

    public int getHeartbeatTimeoutMs() {
        return heartbeatTimeoutMs;
    }

    public void setHeartbeatTimeoutMs(int heartbeatTimeoutMs) {
        this.heartbeatTimeoutMs = heartbeatTimeoutMs;
    }

    public String getOnnxModelPath() {
        return onnxModelPath;
    }

    public void setOnnxModelPath(String onnxModelPath) {
        this.onnxModelPath = onnxModelPath;
    }

    public int getInferenceThreads() {
        return inferenceThreads;
    }

    public void setInferenceThreads(int inferenceThreads) {
        this.inferenceThreads = inferenceThreads;
    }

    public double getConfidenceThreshold() {
        return confidenceThreshold;
    }

    public void setConfidenceThreshold(double confidenceThreshold) {
        this.confidenceThreshold = confidenceThreshold;
    }

    public int getExperienceBufferSize() {
        return experienceBufferSize;
    }

    public void setExperienceBufferSize(int experienceBufferSize) {
        this.experienceBufferSize = experienceBufferSize;
    }

    public int getExperienceFlushIntervalMs() {
        return experienceFlushIntervalMs;
    }

    public void setExperienceFlushIntervalMs(int experienceFlushIntervalMs) {
        this.experienceFlushIntervalMs = experienceFlushIntervalMs;
    }

    public String getGameServerUrl() {
        return gameServerUrl;
    }

    public void setGameServerUrl(String gameServerUrl) {
        this.gameServerUrl = gameServerUrl;
    }

    public boolean isEnableGameProxy() {
        return enableGameProxy;
    }

    public void setEnableGameProxy(boolean enableGameProxy) {
        this.enableGameProxy = enableGameProxy;
    }

    public String getPortalTitle() {
        return portalTitle;
    }

    public void setPortalTitle(String portalTitle) {
        this.portalTitle = portalTitle;
    }

    public boolean isEnableConsensus() {
        return enableConsensus;
    }

    public void setEnableConsensus(boolean enableConsensus) {
        this.enableConsensus = enableConsensus;
    }

    public int getConsensusTimeoutMs() {
        return consensusTimeoutMs;
    }

    public void setConsensusTimeoutMs(int consensusTimeoutMs) {
        this.consensusTimeoutMs = consensusTimeoutMs;
    }

    public double getVoteWeight() {
        return voteWeight;
    }

    public void setVoteWeight(double voteWeight) {
        this.voteWeight = voteWeight;
    }

    // ── Mesh Getters and Setters ──────────────────────────────────────────────

    public boolean isMeshEnabled() {
        return meshEnabled;
    }

    public void setMeshEnabled(boolean meshEnabled) {
        this.meshEnabled = meshEnabled;
    }

    public int getMeshMaxPeers() {
        return meshMaxPeers;
    }

    public void setMeshMaxPeers(int meshMaxPeers) {
        this.meshMaxPeers = meshMaxPeers;
    }

    public int getMeshPeerDiscoveryIntervalMs() {
        return meshPeerDiscoveryIntervalMs;
    }

    public void setMeshPeerDiscoveryIntervalMs(int meshPeerDiscoveryIntervalMs) {
        this.meshPeerDiscoveryIntervalMs = meshPeerDiscoveryIntervalMs;
    }

    public int getMeshPeerTimeoutMs() {
        return meshPeerTimeoutMs;
    }

    public void setMeshPeerTimeoutMs(int meshPeerTimeoutMs) {
        this.meshPeerTimeoutMs = meshPeerTimeoutMs;
    }

    public boolean isMeshConsensusEnabled() {
        return meshConsensusEnabled;
    }

    public void setMeshConsensusEnabled(boolean meshConsensusEnabled) {
        this.meshConsensusEnabled = meshConsensusEnabled;
    }

    public boolean isMeshExperienceSyncEnabled() {
        return meshExperienceSyncEnabled;
    }

    public void setMeshExperienceSyncEnabled(boolean meshExperienceSyncEnabled) {
        this.meshExperienceSyncEnabled = meshExperienceSyncEnabled;
    }

    public int getMeshMaxHops() {
        return meshMaxHops;
    }

    public void setMeshMaxHops(int meshMaxHops) {
        this.meshMaxHops = meshMaxHops;
    }

    public int getMeshPortOffset() {
        return meshPortOffset;
    }

    public void setMeshPortOffset(int meshPortOffset) {
        this.meshPortOffset = meshPortOffset;
    }

    public String getCoordinatorFallbackUrls() {
        return coordinatorFallbackUrls;
    }

    public void setCoordinatorFallbackUrls(String coordinatorFallbackUrls) {
        this.coordinatorFallbackUrls = coordinatorFallbackUrls;
    }

    public int getFailoverCheckIntervalMs() {
        return failoverCheckIntervalMs;
    }

    public void setFailoverCheckIntervalMs(int failoverCheckIntervalMs) {
        this.failoverCheckIntervalMs = failoverCheckIntervalMs;
    }

    public boolean isFailoverEnabled() {
        return failoverEnabled;
    }

    public void setFailoverEnabled(boolean failoverEnabled) {
        this.failoverEnabled = failoverEnabled;
    }

    // ── Kafka Event-Driven Architecture (Phase 17+) ──────────────────────────

    /** Enable Kafka event-driven communication. */
    private boolean kafkaEnabled = false;

    /** Kafka broker URLs (comma-separated). */
    private String kafkaBootstrapServers = "localhost:9092";

    // ── Base Node (Decentralized Neural Network) ──────────────────────────────

    /** Enable Base Node mode — this neuron acts as a task distributor and can become co-server. */
    private boolean baseNodeEnabled = true;

    /** Leader election timeout — if server is unreachable for this long, trigger election (milliseconds). */
    private int leaderElectionTimeoutMs = 60000;

    /** Data sync interval — how often to sync with the main server when available (milliseconds). */
    private int dataSyncIntervalMs = 30000;

    /** Maximum local data queue size before backpressure. */
    private int dataQueueMaxSize = 10000;

    /** Enable automatic co-server promotion when server is unreachable. */
    private boolean autoCoServerEnabled = true;

    /** Model version for this neuron (used in leader election). */
    private int modelVersion = 1;

    // ── Kafka Getters and Setters ─────────────────────────────────────────────

    public boolean isKafkaEnabled() {
        return kafkaEnabled;
    }

    public void setKafkaEnabled(boolean kafkaEnabled) {
        this.kafkaEnabled = kafkaEnabled;
    }

    public String getKafkaBootstrapServers() {
        return kafkaBootstrapServers;
    }

    public void setKafkaBootstrapServers(String kafkaBootstrapServers) {
        this.kafkaBootstrapServers = kafkaBootstrapServers;
    }

    // ── Base Node Getters and Setters ─────────────────────────────────────────

    public boolean isBaseNodeEnabled() {
        return baseNodeEnabled;
    }

    public void setBaseNodeEnabled(boolean baseNodeEnabled) {
        this.baseNodeEnabled = baseNodeEnabled;
    }

    public int getLeaderElectionTimeoutMs() {
        return leaderElectionTimeoutMs;
    }

    public void setLeaderElectionTimeoutMs(int leaderElectionTimeoutMs) {
        this.leaderElectionTimeoutMs = leaderElectionTimeoutMs;
    }

    public int getDataSyncIntervalMs() {
        return dataSyncIntervalMs;
    }

    public void setDataSyncIntervalMs(int dataSyncIntervalMs) {
        this.dataSyncIntervalMs = dataSyncIntervalMs;
    }

    public int getDataQueueMaxSize() {
        return dataQueueMaxSize;
    }

    public void setDataQueueMaxSize(int dataQueueMaxSize) {
        this.dataQueueMaxSize = dataQueueMaxSize;
    }

    public boolean isAutoCoServerEnabled() {
        return autoCoServerEnabled;
    }

    public void setAutoCoServerEnabled(boolean autoCoServerEnabled) {
        this.autoCoServerEnabled = autoCoServerEnabled;
    }

    public int getModelVersion() {
        return modelVersion;
    }

    public void setModelVersion(int modelVersion) {
        this.modelVersion = modelVersion;
    }
}
