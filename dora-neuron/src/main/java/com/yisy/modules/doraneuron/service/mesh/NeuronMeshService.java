package com.yisy.modules.doraneuron.service.mesh;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yisy.modules.doraneuron.config.NeuronConfig;
import com.yisy.modules.doraneuron.config.NeuronPrometheusMetrics;
import com.yisy.modules.doraneuron.model.NeuronIdentity;
import com.yisy.modules.doraneuron.protocol.mesh.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Core service managing the P2P mesh network between Neurons.
 *
 * This service handles:
 * <ul>
 *   <li>Peer discovery via coordinator and direct peer referrals</li>
 *   <li>Topology maintenance through periodic topology updates</li>
 *   <li>Message routing between peers with hop count tracking</li>
 *   <li>Peer health monitoring and automatic reconnection</li>
 *   <li>Mesh network statistics and metrics</li>
 * </ul>
 *
 * The mesh network operates alongside the coordinator-based communication,
 * providing decentralized P2P capabilities for consensus and experience sharing.
 */
@Service
@ConditionalOnProperty(prefix = "neuron", name = "mesh-enabled", havingValue = "true", matchIfMissing = true)
public class NeuronMeshService {

    private static final Logger log = LoggerFactory.getLogger(NeuronMeshService.class);

    private final NeuronConfig config;
    private final NeuronIdentity identity;
    private final RestTemplate restTemplate;
    private final NeuronPrometheusMetrics prometheusMetrics;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${server.port:8090}")
    private int serverPort;

    /** Map of nodeId -> peer info for all known peers. */
    private final ConcurrentHashMap<String, MeshPeerInfo> peers = new ConcurrentHashMap<>();

    /** Sequence number for topology updates. */
    private final AtomicLong topologySequence = new AtomicLong(0);

    /** Thread pool for async mesh operations. */
    private final ExecutorService meshExecutor = Executors.newFixedThreadPool(4, r -> {
        Thread t = new Thread(r, "mesh-worker");
        t.setDaemon(true);
        return t;
    });

    /** Message deduplication cache (messageId -> timestamp). */
    private final ConcurrentHashMap<String, Long> messageCache = new ConcurrentHashMap<>();

    /** Callbacks for incoming mesh messages. */
    private final List<MeshMessageCallback> messageCallbacks = new CopyOnWriteArrayList<>();

    /** Statistics tracking. */
    private volatile long messagesSent = 0;
    private volatile long messagesReceived = 0;
    private volatile long peersConnected = 0;
    private volatile long peersDisconnected = 0;

    /**
     * Callback interface for incoming mesh messages.
     */
    public interface MeshMessageCallback {
        void onMeshMessage(MeshMessage message);
    }

    public NeuronMeshService(NeuronConfig config, NeuronIdentity identity, RestTemplate restTemplate,
                              NeuronPrometheusMetrics prometheusMetrics) {
        this.config = config;
        this.identity = identity;
        this.restTemplate = restTemplate;
        this.prometheusMetrics = prometheusMetrics;
    }

    @PostConstruct
    public void init() {
        log.info("NeuronMeshService initialized — max peers: {}, discovery interval: {}ms",
                config.getMeshMaxPeers(), config.getMeshPeerDiscoveryIntervalMs());

        // Start periodic peer discovery
        schedulePeerDiscovery();

        // Start periodic topology updates
        scheduleTopologyUpdates();

        // Start message cache cleanup
        scheduleCacheCleanup();
    }

    @PreDestroy
    public void shutdown() {
        log.info("Shutting down NeuronMeshService");
        meshExecutor.shutdown();
        try {
            if (!meshExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                meshExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            meshExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Register a callback for incoming mesh messages.
     */
    public void addMessageCallback(MeshMessageCallback callback) {
        messageCallbacks.add(callback);
    }

    /**
     * Discover peers from the coordinator.
     * This is the initial discovery mechanism; subsequent discovery
     * happens through peer referrals in topology updates.
     */
    @Scheduled(fixedDelayString = "${neuron.mesh-peer-discovery-interval-ms:30000}")
    public void discoverPeers() {
        try {
            String url = config.getCoordinatorUrl() + "/api/v1/neuron/discover";
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> body = response.getBody();
                List<Map<String, Object>> peerList = (List<Map<String, Object>>) body.get("peers");

                if (peerList != null) {
                    for (Map<String, Object> peerMap : peerList) {
                        String nodeId = (String) peerMap.get("nodeId");
                        if (nodeId != null && !nodeId.equals(identity.getNodeId())) {
                            MeshPeerInfo peer = mapToPeerInfo(peerMap);
                            if (peer != null && !peers.containsKey(nodeId)) {
                                peers.put(nodeId, peer);
                                log.info("Discovered new peer: {}", nodeId);
                                peersConnected++;
                                prometheusMetrics.setMeshPeerCount(peers.size());
                                connectToPeer(peer);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Peer discovery failed: {}", e.getMessage());
        }
    }

    /**
     * Establish a connection to a peer by exchanging peer info.
     */
    private void connectToPeer(MeshPeerInfo peer) {
        meshExecutor.submit(() -> {
            try {
                String url = peer.getUrl() + "/api/v1/mesh/peer/register";
                MeshPeerInfo myInfo = buildPeerInfo();

                ResponseEntity<MeshPeerInfo> response = restTemplate.postForEntity(
                        url, myInfo, MeshPeerInfo.class);

                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    MeshPeerInfo theirInfo = response.getBody();
                    peers.put(theirInfo.getNodeId(), theirInfo);
                    log.info("Connected to peer: {}", theirInfo.getNodeId());
                }
            } catch (Exception e) {
                log.warn("Failed to connect to peer {}: {}", peer.getNodeId(), e.getMessage());
                peers.remove(peer.getNodeId());
                peersDisconnected++;
                prometheusMetrics.recordMeshError();
                prometheusMetrics.setMeshPeerCount(peers.size());
            }
        });
    }

    /**
     * Build the peer info for this neuron.
     */
    public MeshPeerInfo buildPeerInfo() {
        MeshPeerInfo info = new MeshPeerInfo();
        info.setNodeId(identity.getNodeId());
        info.setNodeName(identity.getNodeName());
        info.setRegion(identity.getRegion());
        info.setHostAddress(identity.getHostAddress());
        info.setPort(serverPort + config.getMeshPortOffset());
        info.setStatus(identity.getStatus().name());
        info.setModelVersion(identity.getCurrentModelVersion());
        info.setVoteWeight(identity.getVoteWeight());
        info.setPeerCount(peers.size());
        info.setExperienceCount(identity.getExperienceCount());
        info.setLastHeartbeat(System.currentTimeMillis());
        return info;
    }

    /**
     * Send a mesh message to all connected peers.
     */
    public void broadcast(MeshMessage message) {
        message.setMaxHops(config.getMeshMaxHops());
        message.setFromNodeId(identity.getNodeId());

        for (MeshPeerInfo peer : peers.values()) {
            sendToPeer(peer, message);
        }
        messagesSent++;
        prometheusMetrics.recordMeshMessageSent();
        prometheusMetrics.setMeshPeerCount(peers.size());
    }

    /**
     * Send a mesh message to a specific peer.
     */
    public void sendToPeer(MeshPeerInfo peer, MeshMessage message) {
        meshExecutor.submit(() -> {
            try {
                String url = peer.getUrl() + "/api/v1/mesh/message";
                restTemplate.postForEntity(url, message, Void.class);
                log.debug("Sent message to peer {}: {}", peer.getNodeId(), message.getType());
            } catch (Exception e) {
                log.warn("Failed to send message to peer {}: {}", peer.getNodeId(), e.getMessage());
            }
        });
    }

    /**
     * Handle an incoming mesh message from a peer.
     */
    public void handleIncomingMessage(MeshMessage message) {
        // Deduplication check
        String messageId = message.getMessageId();
        if (messageId != null) {
            Long cached = messageCache.putIfAbsent(messageId, System.currentTimeMillis());
            if (cached != null) {
                log.debug("Dropping duplicate message: {}", messageId);
                return;
            }
        }

        // Check hop count
        if (message.isExpired()) {
            log.debug("Dropping expired message: {}", message.getMessageId());
            return;
        }

        messagesReceived++;
        prometheusMetrics.recordMeshMessageReceived();
        prometheusMetrics.setMeshPeerCount(peers.size());

        // Dispatch to callbacks
        for (MeshMessageCallback callback : messageCallbacks) {
            try {
                callback.onMeshMessage(message);
            } catch (Exception e) {
                log.error("Error in mesh message callback: {}", e.getMessage());
            }
        }

        // Relay to other peers if hops remain
        if (message.incrementHop() && message.getToNodeId() == null) {
            for (MeshPeerInfo peer : peers.values()) {
                if (!peer.getNodeId().equals(message.getFromNodeId())) {
                    sendToPeer(peer, message);
                }
            }
        }
    }

    /**
     * Send topology update to all peers.
     */
    @Scheduled(fixedDelayString = "${neuron.mesh-peer-discovery-interval-ms:30000}")
    public void sendTopologyUpdate() {
        List<MeshPeerInfo> peerList = new ArrayList<>(peers.values());
        MeshTopologyUpdate update = new MeshTopologyUpdate(
                identity.getNodeId(),
                peerList,
                peerList.size() + 1,
                topologySequence.getAndIncrement()
        );
        update.setMaxHops(config.getMeshMaxHops());
        broadcast(update);
    }

    /**
     * Process incoming topology update.
     */
    public void processTopologyUpdate(MeshTopologyUpdate update) {
        if (update.isIsRequest()) {
            // Respond with our topology
            sendTopologyUpdate();
            return;
        }

        // Merge peer information
        if (update.getPeers() != null) {
            for (MeshPeerInfo peer : update.getPeers()) {
                if (!peer.getNodeId().equals(identity.getNodeId())) {
                    MeshPeerInfo existing = peers.get(peer.getNodeId());
                    if (existing == null) {
                        peers.put(peer.getNodeId(), peer);
                        log.info("Learned about new peer from topology: {}", peer.getNodeId());
                        peersConnected++;
                        connectToPeer(peer);
                    } else {
                        // Update existing peer info
                        existing.setLastHeartbeat(System.currentTimeMillis());
                        existing.setPeerCount(peer.getPeerCount());
                    }
                }
            }
        }
    }

    /**
     * Check health of all peers and remove stale ones.
     */
    @Scheduled(fixedDelayString = "${neuron.mesh-peer-timeout-ms:10000}")
    public void checkPeerHealth() {
        long cutoff = System.currentTimeMillis() - config.getMeshPeerTimeoutMs() * 2;

        peers.entrySet().removeIf(entry -> {
            if (entry.getValue().getLastHeartbeat() < cutoff) {
                log.warn("Peer {} is stale, removing", entry.getKey());
                peersDisconnected++;
                return true;
            }
            return false;
        });
        prometheusMetrics.setMeshPeerCount(peers.size());
    }

    /**
     * Clean up old message cache entries.
     */
    private void scheduleCacheCleanup() {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "mesh-cache-cleanup");
            t.setDaemon(true);
            return t;
        });

        scheduler.scheduleAtFixedRate(() -> {
            long cutoff = System.currentTimeMillis() - 60000; // 1 minute TTL
            messageCache.entrySet().removeIf(e -> e.getValue() < cutoff);
        }, 60, 60, TimeUnit.SECONDS);
    }

    private void schedulePeerDiscovery() {
        // Initial discovery after a short delay
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "mesh-discovery");
            t.setDaemon(true);
            return t;
        });

        scheduler.scheduleWithFixedDelay(() -> {
            try {
                discoverPeers();
            } catch (Exception e) {
                log.warn("Scheduled peer discovery failed: {}", e.getMessage());
            }
        }, 5, config.getMeshPeerDiscoveryIntervalMs() / 1000, TimeUnit.SECONDS);
    }

    private void scheduleTopologyUpdates() {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "mesh-topology");
            t.setDaemon(true);
            return t;
        });

        scheduler.scheduleWithFixedDelay(() -> {
            try {
                sendTopologyUpdate();
            } catch (Exception e) {
                log.warn("Scheduled topology update failed: {}", e.getMessage());
            }
        }, 10, config.getMeshPeerDiscoveryIntervalMs() / 1000, TimeUnit.SECONDS);
    }

    private MeshPeerInfo mapToPeerInfo(Map<String, Object> peerMap) {
        try {
            return objectMapper.convertValue(peerMap, MeshPeerInfo.class);
        } catch (Exception e) {
            log.warn("Failed to map peer info: {}", e.getMessage());
            return null;
        }
    }

    // ── Getters for monitoring ────────────────────────────────────────────────

    public Collection<MeshPeerInfo> getPeers() {
        return Collections.unmodifiableCollection(peers.values());
    }

    public int getPeerCount() {
        return peers.size();
    }

    /**
     * Returns the number of connected peers (alias for getPeerCount).
     */
    public int getConnectedPeerCount() {
        return peers.size();
    }

    /**
     * Returns the IDs of all connected peers.
     */
    public List<String> getConnectedPeerIds() {
        return new ArrayList<>(peers.keySet());
    }

    /**
     * Send a mesh message to a specific peer by node ID.
     */
    public void sendMessage(String nodeId, MeshMessage message) {
        MeshPeerInfo peer = peers.get(nodeId);
        if (peer != null) {
            sendToPeer(peer, message);
        } else {
            log.warn("Peer {} not found, cannot send message", nodeId);
        }
    }

    public long getMessagesSent() {
        return messagesSent;
    }

    public long getMessagesReceived() {
        return messagesReceived;
    }

    public long getPeersConnected() {
        return peersConnected;
    }

    public long getPeersDisconnected() {
        return peersDisconnected;
    }
}
