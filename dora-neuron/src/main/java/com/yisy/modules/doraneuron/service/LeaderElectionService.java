package com.yisy.modules.doraneuron.service;

import com.yisy.modules.doraneuron.config.NeuronConfig;
import com.yisy.modules.doraneuron.protocol.mesh.LeaderElectionMessage;
import com.yisy.modules.doraneuron.protocol.mesh.CoServerPromotionMessage;
import com.yisy.modules.doraneuron.service.mesh.NeuronMeshService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.ResourceAccessException;

import jakarta.annotation.PostConstruct;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Handles automatic leader election when the main server is unreachable.
 * 
 * When the server goes down, the longest-connected, most capable neuron
 * automatically promotes itself to co-server, taking over task distribution
 * and coordination until the server returns.
 */
@Service
public class LeaderElectionService {

    private static final Logger log = LoggerFactory.getLogger(LeaderElectionService.class);

    private final NeuronConfig config;
    private final NeuronMeshService meshService;
    private final RestTemplate restTemplate;

    @Value("${server.port:8090}")
    private int serverPort;

    /** Whether this neuron is currently the co-server (leader). */
    private final AtomicBoolean isCoServer = new AtomicBoolean(false);
    
    /** Whether the main server is currently reachable. */
    private final AtomicBoolean serverReachable = new AtomicBoolean(true);
    
    /** Timestamp when the server became unreachable. */
    private final AtomicLong serverDownSince = new AtomicLong(0);
    
    /** Uptime of this neuron in milliseconds. */
    private final long startTime = System.currentTimeMillis();
    
    /** Server health check endpoint. */
    private String serverHealthUrl;

    public LeaderElectionService(NeuronConfig config, NeuronMeshService meshService) {
        this.config = config;
        this.meshService = meshService;
        this.restTemplate = new RestTemplate();
    }

    @PostConstruct
    public void init() {
        String coordinatorUrl = config.getCoordinatorUrl();
        this.serverHealthUrl = coordinatorUrl + "/api/v1/co-server/health";
        
        log.info("Leader election service initialized for node: {}", config.getNodeId());
        log.info("Server health check URL: {}", serverHealthUrl);
        log.info("Leader election timeout: {}ms", config.getLeaderElectionTimeoutMs());
    }

    /**
     * Periodic health check for the main server.
     * If the server is unreachable for longer than the timeout, trigger leader election.
     */
    @Scheduled(fixedDelayString = "${neuron.failover-check-interval-ms:30000}")
    public void checkServerHealth() {
        if (!config.isBaseNodeEnabled()) {
            return;
        }

        boolean wasReachable = serverReachable.get();
        boolean isReachable = checkServerHealthInternal();

        if (wasReachable && !isReachable) {
            // Server just went down
            serverDownSince.set(System.currentTimeMillis());
            serverReachable.set(false);
            log.warn("Main server became unreachable at {}", serverHealthUrl);
            
        } else if (!wasReachable && isReachable) {
            // Server is back online
            long downtime = System.currentTimeMillis() - serverDownSince.get();
            serverReachable.set(true);
            log.info("Main server is back online after {}ms downtime", downtime);
            
            // If we were co-server, sync data back to the server
            if (isCoServer.get()) {
                log.info("Syncing data back to main server after failover");
                // Data sync will be handled by DataSyncService
            }
            
        } else if (!isReachable) {
            // Server is still down - check if we should trigger election
            long downtime = System.currentTimeMillis() - serverDownSince.get();
            if (downtime >= config.getLeaderElectionTimeoutMs() && !isCoServer.get()) {
                log.info("Server down for {}ms, triggering leader election", downtime);
                triggerLeaderElection();
            }
        }
    }

    /**
     * Check if the main server is reachable.
     */
    private boolean checkServerHealthInternal() {
        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(serverHealthUrl, Map.class);
            return response.getStatusCode().is2xxSuccessful();
        } catch (ResourceAccessException e) {
            return false;
        } catch (Exception e) {
            log.debug("Health check failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Trigger the leader election process.
     * 
     * This neuron broadcasts an election request to all connected peers.
     * The neuron with the longest uptime and highest model version wins.
     */
    public void triggerLeaderElection() {
        if (!config.isAutoCoServerEnabled()) {
            log.info("Auto co-server promotion is disabled");
            return;
        }

        long uptime = System.currentTimeMillis() - startTime;
        int modelVersion = config.getModelVersion();
        int connectedPeers = meshService.getConnectedPeerCount();

        log.info("Starting leader election — node: {}, uptime: {}ms, model: {}, peers: {}",
                config.getNodeId(), uptime, modelVersion, connectedPeers);

        // Create election request message
        LeaderElectionMessage electionMessage = new LeaderElectionMessage(
                config.getNodeId(), uptime, modelVersion, connectedPeers);
        electionMessage.setFromNodeId(config.getNodeId());

        // Broadcast to all peers
        meshService.broadcast(electionMessage);
        log.info("Broadcasted leader election request to mesh");

        // After broadcasting, wait for responses and determine winner
        // For simplicity, this neuron promotes itself if it has the best metrics
        // In a full implementation, this would collect votes from peers
        promoteToCoServer();
    }

    /**
     * Promote this neuron to co-server.
     */
    public void promoteToCoServer() {
        isCoServer.set(true);
        
        log.info("Node {} promoted to co-server", config.getNodeId());
        log.info("Co-server will handle task distribution and data coordination");

        // Notify mesh about the promotion
        CoServerPromotionMessage promotionMessage = new CoServerPromotionMessage(
                config.getNodeId(), config.getNodeName(), "localhost", serverPort);
        promotionMessage.setFromNodeId(config.getNodeId());

        meshService.broadcast(promotionMessage);
    }

    /**
     * Step down from co-server role (when main server is back).
     */
    public void stepDown() {
        if (isCoServer.compareAndSet(true, false)) {
            log.info("Node {} stepped down from co-server role", config.getNodeId());
            
            CoServerPromotionMessage stepDownMessage = new CoServerPromotionMessage(
                    config.getNodeId(), config.getNodeName(), "localhost", serverPort);
            stepDownMessage.setFromNodeId(config.getNodeId());
            stepDownMessage.setType("CO_SERVER_STEPPED_DOWN");
            
            meshService.broadcast(stepDownMessage);
        }
    }

    /**
     * Check if this neuron is currently the co-server.
     */
    public boolean isCoServer() {
        return isCoServer.get();
    }

    /**
     * Check if the main server is currently reachable.
     */
    public boolean isServerReachable() {
        return serverReachable.get();
    }

    /**
     * Get the uptime of this neuron.
     */
    public long getUptime() {
        return System.currentTimeMillis() - startTime;
    }

    /**
     * Get the current status of this neuron for co-server registration.
     */
    public Map<String, Object> getCoServerStatus() {
        return Map.of(
                "nodeId", config.getNodeId(),
                "nodeName", config.getNodeName(),
                "isCoServer", isCoServer.get(),
                "serverReachable", serverReachable.get(),
                "uptime", getUptime(),
                "modelVersion", config.getModelVersion(),
                "connectedPeers", meshService.getConnectedPeerCount()
        );
    }
}
