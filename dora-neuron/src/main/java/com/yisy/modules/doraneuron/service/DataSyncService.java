package com.yisy.modules.doraneuron.service;

import com.yisy.modules.doraneuron.config.NeuronConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.ResourceAccessException;

import jakarta.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Handles local data queuing and synchronization with the main server.
 * 
 * When the server is down, data changes are queued locally with vector clocks.
 * When the server is back, the queued changes are synced using last-write-wins
 * conflict resolution.
 */
@Service
public class DataSyncService {

    private static final Logger log = LoggerFactory.getLogger(DataSyncService.class);

    private final NeuronConfig config;
    private final LeaderElectionService leaderElectionService;
    private final RestTemplate restTemplate;

    /** Local queue for data changes. */
    private final LinkedBlockingQueue<DataChange> dataQueue = new LinkedBlockingQueue<>();
    
    /** Vector clock for this node. */
    private final Map<String, Long> vectorClock = new ConcurrentHashMap<>();
    
    /** Change counter for unique IDs. */
    private final AtomicLong changeCounter = new AtomicLong(0);
    
    /** Server sync endpoint. */
    private String serverSyncUrl;
    
    /** Server register endpoint. */
    private String serverRegisterUrl;
    
    /** Whether sync is currently in progress. */
    private final AtomicLong syncInProgress = new AtomicLong(0);

    public DataSyncService(NeuronConfig config, LeaderElectionService leaderElectionService) {
        this.config = config;
        this.leaderElectionService = leaderElectionService;
        this.restTemplate = new RestTemplate();
    }

    @PostConstruct
    public void init() {
        String coordinatorUrl = config.getCoordinatorUrl();
        this.serverSyncUrl = coordinatorUrl + "/api/v1/co-server/sync";
        this.serverRegisterUrl = coordinatorUrl + "/api/v1/co-server/register";
        
        // Increment vector clock for this node
        vectorClock.merge(config.getNodeId(), 1L, Long::sum);
        
        log.info("Data sync service initialized for node: {}", config.getNodeId());
        log.info("Server sync URL: {}", serverSyncUrl);
        log.info("Data queue max size: {}", config.getDataQueueMaxSize());
    }

    /**
     * Queue a data change locally.
     * 
     * @param entity the entity type (e.g., "GameEmpire", "Character")
     * @param id the entity ID
     * @param operation the operation (CREATE, UPDATE, DELETE)
     * @param data the data for the change
     * @return the change ID
     */
    public String queueDataChange(String entity, String id, String operation, Map<String, Object> data) {
        if (dataQueue.size() >= config.getDataQueueMaxSize()) {
            log.warn("Data queue full, dropping change for {} {}", entity, id);
            return null;
        }

        String changeId = "change-" + config.getNodeId() + "-" + changeCounter.incrementAndGet();
        
        // Increment vector clock
        vectorClock.merge(config.getNodeId(), 1L, Long::sum);
        
        DataChange change = new DataChange(changeId, entity, id, operation, data, 
                System.currentTimeMillis(), new HashMap<>(vectorClock));
        
        dataQueue.offer(change);
        log.debug("Queued data change: {} {} {}", operation, entity, id);
        
        return changeId;
    }

    /**
     * Periodic sync with the main server when available.
     */
    @Scheduled(fixedDelayString = "${neuron.data-sync-interval-ms:30000}")
    public void syncWithServer() {
        if (!config.isBaseNodeEnabled()) {
            return;
        }

        // Only sync if server is reachable and we're not the co-server
        if (!leaderElectionService.isServerReachable() || leaderElectionService.isCoServer()) {
            return;
        }

        if (dataQueue.isEmpty()) {
            return;
        }

        try {
            syncInProgress.incrementAndGet();
            
            // Drain the queue
            List<DataChange> changes = new ArrayList<>();
            dataQueue.drainTo(changes);
            
            if (changes.isEmpty()) {
                syncInProgress.decrementAndGet();
                return;
            }

            log.info("Syncing {} changes with server", changes.size());

            // Create sync request
            Map<String, Object> request = new HashMap<>();
            request.put("nodeId", config.getNodeId());
            request.put("vectorClock", new HashMap<>(vectorClock));
            
            // Convert changes to list of maps
            List<Map<String, Object>> changeList = new ArrayList<>();
            for (DataChange change : changes) {
                Map<String, Object> changeMap = new HashMap<>();
                changeMap.put("entity", change.entity);
                changeMap.put("id", change.id);
                changeMap.put("operation", change.operation);
                changeMap.put("data", change.data);
                changeMap.put("timestamp", change.timestamp);
                changeMap.put("vectorClock", change.vectorClock);
                changeList.add(changeMap);
            }
            request.put("changes", changeList);

            // Send to server
            ResponseEntity<Map> response = restTemplate.postForEntity(serverSyncUrl, request, Map.class);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                Map<String, Object> body = response.getBody();
                log.info("Sync completed: {}", body);
                
                // Merge server's vector clock
                if (body.containsKey("vectorClock")) {
                    @SuppressWarnings("unchecked")
                    Map<String, Long> serverClock = (Map<String, Long>) body.get("vectorClock");
                    mergeVectorClock(serverClock);
                }
            } else {
                log.warn("Sync failed with status: {}", response.getStatusCode());
                // Put changes back in queue
                dataQueue.addAll(changes);
            }
            
        } catch (ResourceAccessException e) {
            log.warn("Server unreachable during sync: {}", e.getMessage());
        } catch (Exception e) {
            log.error("Sync failed: {}", e.getMessage());
        } finally {
            syncInProgress.decrementAndGet();
        }
    }

    /**
     * Merge the server's vector clock with ours.
     */
    private void mergeVectorClock(Map<String, Long> serverClock) {
        serverClock.forEach((node, timestamp) -> {
            vectorClock.merge(node, timestamp, Math::max);
        });
    }

    /**
     * Register this node as co-server with the main server.
     */
    public void registerAsCoServer() {
        try {
            Map<String, Object> registration = new HashMap<>();
            registration.put("nodeId", config.getNodeId());
            registration.put("nodeName", config.getNodeName());
            registration.put("capabilities", List.of("inference", "mesh", "storage"));
            registration.put("uptime", leaderElectionService.getUptime());
            registration.put("modelVersion", config.getModelVersion());
            
            ResponseEntity<Map> response = restTemplate.postForEntity(serverRegisterUrl, registration, Map.class);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("Registered as co-server with main server");
            } else {
                log.warn("Failed to register as co-server: {}", response.getStatusCode());
            }
            
        } catch (Exception e) {
            log.error("Co-server registration failed: {}", e.getMessage());
        }
    }

    /**
     * Get the current queue size.
     */
    public int getQueueSize() {
        return dataQueue.size();
    }

    /**
     * Get the current vector clock.
     */
    public Map<String, Long> getVectorClock() {
        return new HashMap<>(vectorClock);
    }

    /**
     * Get sync statistics.
     */
    public Map<String, Object> getSyncStats() {
        return Map.of(
                "queueSize", dataQueue.size(),
                "vectorClock", new HashMap<>(vectorClock),
                "syncInProgress", syncInProgress.get() > 0,
                "serverReachable", leaderElectionService.isServerReachable(),
                "isCoServer", leaderElectionService.isCoServer()
        );
    }

    /**
     * Represents a data change.
     */
    private static class DataChange {
        final String changeId;
        final String entity;
        final String id;
        final String operation;
        final Map<String, Object> data;
        final long timestamp;
        final Map<String, Long> vectorClock;

        DataChange(String changeId, String entity, String id, String operation, 
                   Map<String, Object> data, long timestamp, Map<String, Long> vectorClock) {
            this.changeId = changeId;
            this.entity = entity;
            this.id = id;
            this.operation = operation;
            this.data = data;
            this.timestamp = timestamp;
            this.vectorClock = vectorClock;
        }
    }
}
