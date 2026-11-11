package com.yisy.modules.doramcu.neuron.service;

import com.yisy.modules.doramcu.neuron.config.FederationConfig;
import com.yisy.modules.doramcu.neuron.dto.*;
import com.yisy.modules.doramcu.neuron.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * Core service managing Multi-Region Federation.
 *
 * Handles peer coordinator discovery, registry sync, cross-region consensus,
 * experience forwarding, and peer health monitoring.
 */
@Service
public class FederationService {

    private static final Logger log = LoggerFactory.getLogger(FederationService.class);

    private final FederationConfig config;
    private final RestTemplate restTemplate;
    private final FederationMetricsService metricsService;
    private final NeuronRegistry neuronRegistry;

    /** Map of region -> peer coordinator metadata. */
    private final ConcurrentHashMap<String, FederationPeer> peers = new ConcurrentHashMap<>();

    /** Thread pool for async federation operations. */
    private final ExecutorService executor = Executors.newFixedThreadPool(4);

    /** Current model version at this coordinator. */
    private volatile long currentModelVersion = 0;

    /** Sync history — last 50 sync operations. */
    private final LinkedList<SyncHistoryDto> syncHistory = new LinkedList<>();

    @Value("${server.port:8084}")
    private int serverPort;

    public FederationService(FederationConfig config, RestTemplate restTemplate,
                             FederationMetricsService metricsService, NeuronRegistry neuronRegistry) {
        this.config = config;
        this.restTemplate = restTemplate;
        this.metricsService = metricsService;
        this.neuronRegistry = neuronRegistry;
    }

    /**
     * Initialize federation peers from configuration.
     */
    @PostConstruct
    public void init() {
        if (!config.isEnabled()) {
            log.info("Federation disabled — running in standalone mode");
            return;
        }

        log.info("Initializing federation for region '{}' (primary={})", config.getRegion(), config.isPrimary());

        // Register peers from config
        for (String peerUrl : config.getPeerUrls()) {
            String region = extractRegionFromUrl(peerUrl);
            FederationPeer peer = new FederationPeer(region, peerUrl);
            peers.put(region, peer);
            log.info("Registered federation peer: {}", peer);
        }

        log.info("Federation initialized with {} peers", peers.size());
    }

    /**
     * Clean up executor on shutdown.
     */
    @PreDestroy
    public void shutdown() {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    // ── Peer Management ──────────────────────────────────────────────────────

    /**
     * Register a new peer coordinator.
     */
    public FederationPeer registerPeer(String region, String url) {
        FederationPeer peer = new FederationPeer(region, url);
        peers.put(region, peer);
        log.info("Registered new federation peer: {}", peer);
        return peer;
    }

    /**
     * Remove a peer coordinator by region.
     */
    public boolean removePeer(String region) {
        FederationPeer removed = peers.remove(region);
        if (removed != null) {
            log.info("Removed federation peer: {}", region);
        }
        return removed != null;
    }

    /**
     * List all registered peer coordinators.
     */
    public List<FederationPeer> getPeers() {
        return new ArrayList<>(peers.values());
    }

    /**
     * Get a specific peer by region.
     */
    public Optional<FederationPeer> getPeer(String region) {
        return Optional.ofNullable(peers.get(region));
    }

    // ── Registry Sync ────────────────────────────────────────────────────────

    /**
     * Sync registry with all peers (summary sync).
     * Runs periodically when federation is enabled.
     */
    @Scheduled(fixedDelayString = "${neuron.coordinator.federation.sync-interval-ms:60000}")
    public void syncRegistryWithPeers() {
        if (!config.isEnabled() || !config.isPeriodicSyncEnabled() || peers.isEmpty()) {
            return;
        }

        log.debug("Starting registry sync with {} peers", peers.size());

        for (FederationPeer peer : peers.values()) {
            executor.submit(() -> syncWithPeer(peer));
        }
    }

    /**
     * Sync registry with a single peer.
     */
    public void syncWithPeer(FederationPeer peer) {
        long startTime = System.currentTimeMillis();
        try {
            String syncUrl = peer.getUrl() + "/api/v1/neuron/federation/sync";
            FederationSyncRequest request = new FederationSyncRequest(
                    config.getRegion(), FederationSyncRequest.SyncType.SUMMARY);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<FederationSyncRequest> entity = new HttpEntity<>(request, headers);

            ResponseEntity<FederationSyncResponse> response = restTemplate.exchange(
                    syncUrl, HttpMethod.POST, entity, FederationSyncResponse.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                FederationSyncResponse body = response.getBody();
                peer.setNeuronCount(body.getTotalNeuronCount());
                peer.setModelVersion(body.getModelVersion());
                peer.setLastContact(Instant.now());
                peer.setStatus(FederationPeer.Status.ACTIVE);
                log.debug("Synced with peer {}: {} neurons, model v{}",
                        peer.getRegion(), body.getTotalNeuronCount(), body.getModelVersion());

                // Metrics
                long duration = System.currentTimeMillis() - startTime;
                metricsService.incrementSyncSuccess();
                metricsService.recordSyncDuration(duration);
                metricsService.updateSecondsSinceLastSync(peer.getRegion(), 0);

                // Sync history
                addSyncHistory(peer.getRegion(), "SUMMARY", true, null, duration,
                        body.getTotalNeuronCount(), body.getModelVersion());
            }
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.warn("Failed to sync with peer {}: {}", peer.getRegion(), e.getMessage());
            peer.setStatus(FederationPeer.Status.UNREACHABLE);

            // Metrics
            metricsService.incrementSyncFailure();
            metricsService.recordSyncDuration(duration);

            // Sync history
            addSyncHistory(peer.getRegion(), "SUMMARY", false, e.getMessage(), duration, 0, 0);
        }
    }

    /**
     * Trigger manual registry sync with all peers.
     */
    public Map<String, FederationSyncResponse> triggerManualSync() {
        Map<String, FederationSyncResponse> results = new ConcurrentHashMap<>();

        for (FederationPeer peer : peers.values()) {
            executor.submit(() -> {
                try {
                    String syncUrl = peer.getUrl() + "/api/v1/neuron/federation/sync";
                    FederationSyncRequest request = new FederationSyncRequest(
                            config.getRegion(), FederationSyncRequest.SyncType.FULL);

                    HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(MediaType.APPLICATION_JSON);
                    HttpEntity<FederationSyncRequest> entity = new HttpEntity<>(request, headers);

                    ResponseEntity<FederationSyncResponse> response = restTemplate.exchange(
                            syncUrl, HttpMethod.POST, entity, FederationSyncResponse.class);

                    if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                        results.put(peer.getRegion(), response.getBody());
                        peer.setLastContact(Instant.now());
                        peer.setStatus(FederationPeer.Status.ACTIVE);
                    }
                } catch (Exception e) {
                    log.warn("Manual sync failed for peer {}: {}", peer.getRegion(), e.getMessage());
                    peer.setStatus(FederationPeer.Status.UNREACHABLE);
                }
            });
        }

        // Wait for all sync tasks to complete
        try {
            executor.awaitTermination(1, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return results;
    }

    // ── Cross-Region Consensus ───────────────────────────────────────────────

    /**
     * Request cross-region consensus from all peers.
     *
     * Forwards the consensus request to all peer coordinators, collects their
     * aggregated results, and combines them into a global consensus.
     */
    public Map<String, Object> requestCrossRegionConsensus(FederationConsensusRequest request) {
        long startTime = System.currentTimeMillis();
        Map<String, Object> results = new ConcurrentHashMap<>();
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        long timeout = request.getTimeoutMs() != 0 ? request.getTimeoutMs() : config.getConsensusTimeoutMs();

        // Metrics: consensus requested
        metricsService.incrementConsensusRequested();

        for (FederationPeer peer : peers.values()) {
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                try {
                    String consensusUrl = peer.getUrl() + "/api/v1/neuron/federation/consensus";

                    HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(MediaType.APPLICATION_JSON);
                    HttpEntity<FederationConsensusRequest> entity = new HttpEntity<>(request, headers);

                    ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                            consensusUrl, HttpMethod.POST, entity,
                            new ParameterizedTypeReference<Map<String, Object>>() {});

                    if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                        results.put(peer.getRegion(), response.getBody());
                        peer.setLastContact(Instant.now());
                        metricsService.incrementConsensusResponse();
                    }
                } catch (Exception e) {
                    log.warn("Cross-region consensus failed for peer {}: {}",
                            peer.getRegion(), e.getMessage());
                    results.put(peer.getRegion(), Map.of("error", e.getMessage()));
                }
            }, executor);

            futures.add(future);
        }

        // Wait for all responses with timeout
        try {
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                    .get(timeout, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            log.warn("Cross-region consensus timed out after {}ms", timeout);
            metricsService.incrementConsensusTimeout();
        } catch (Exception e) {
            log.warn("Cross-region consensus error: {}", e.getMessage());
        }

        // Metrics: consensus duration
        long duration = System.currentTimeMillis() - startTime;
        metricsService.recordConsensusDuration(duration);

        return results;
    }

    // ── Experience Forwarding ────────────────────────────────────────────────

    /**
     * Forward experience batch to primary coordinator.
     * Only peer (non-primary) coordinators forward experience.
     */
    public boolean forwardExperienceToPrimary(FederationExperienceBatch batch) {
        if (config.isPrimary()) {
            log.debug("This is the primary coordinator — not forwarding experience");
            return false;
        }

        // Find primary coordinator among peers
        for (FederationPeer peer : peers.values()) {
            try {
                String forwardUrl = peer.getUrl() + "/api/v1/neuron/federation/experience/forward";

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                HttpEntity<FederationExperienceBatch> entity = new HttpEntity<>(batch, headers);

                ResponseEntity<Void> response = restTemplate.exchange(
                        forwardUrl, HttpMethod.POST, entity, Void.class);

                if (response.getStatusCode().is2xxSuccessful()) {
                    log.info("Forwarded {} experience samples to primary at {}",
                            batch.getSampleCount(), peer.getRegion());
                    metricsService.incrementExperienceForwarded(batch.getSampleCount());
                    return true;
                }
            } catch (Exception e) {
                log.warn("Failed to forward experience to peer {}: {}", peer.getRegion(), e.getMessage());
            }
        }

        log.warn("Could not forward experience to any primary coordinator");
        return false;
    }

    /**
     * Receive forwarded experience from a peer coordinator.
     * Only the primary coordinator should process these.
     */
    public int receiveExperienceBatch(FederationExperienceBatch batch) {
        if (!config.isPrimary()) {
            log.warn("Received experience batch but this is not the primary coordinator");
            return 0;
        }

        int count = batch.getSampleCount();
        log.info("Received {} experience samples from region '{}'", count, batch.getSourceRegion());
        metricsService.incrementExperienceReceived(count);
        // In a real implementation, this would store the samples for model training
        return count;
    }

    // ── Model Update Propagation ─────────────────────────────────────────────

    /**
     * Propagate model update to all peer coordinators.
     * Only the primary coordinator should broadcast model updates.
     */
    public Map<String, Boolean> propagateModelUpdate(long modelVersion) {
        this.currentModelVersion = modelVersion;
        Map<String, Boolean> results = new ConcurrentHashMap<>();

        if (!config.isPrimary()) {
            log.warn("Only primary coordinator can propagate model updates");
            return results;
        }

        metricsService.incrementModelUpdatePropagated();

        for (FederationPeer peer : peers.values()) {
            executor.submit(() -> {
                try {
                    String modelUrl = peer.getUrl() + "/api/v1/neuron/federation/model/propagate";

                    HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(MediaType.APPLICATION_JSON);
                    Map<String, Object> body = Map.of(
                            "modelVersion", modelVersion,
                            "sourceRegion", config.getRegion()
                    );
                    HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

                    ResponseEntity<Void> response = restTemplate.exchange(
                            modelUrl, HttpMethod.POST, entity, Void.class);

                    results.put(peer.getRegion(), response.getStatusCode().is2xxSuccessful());
                    if (response.getStatusCode().is2xxSuccessful()) {
                        log.info("Propagated model v{} to peer {}", modelVersion, peer.getRegion());
                    }
                } catch (Exception e) {
                    log.warn("Failed to propagate model to peer {}: {}", peer.getRegion(), e.getMessage());
                    results.put(peer.getRegion(), false);
                }
            });
        }

        try {
            executor.awaitTermination(1, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return results;
    }

    /**
     * Receive model update from primary coordinator.
     */
    public void receiveModelUpdate(long modelVersion, String sourceRegion) {
        this.currentModelVersion = modelVersion;
        log.info("Received model update v{} from primary at {}", modelVersion, sourceRegion);
        metricsService.incrementModelUpdateReceived();
    }

    // ── Peer Health Monitoring ───────────────────────────────────────────────

    /**
     * Periodic health check for all peers.
     */
    @Scheduled(fixedDelayString = "${neuron.coordinator.federation.health-check-interval-ms:30000}")
    public void checkPeerHealth() {
        if (!config.isEnabled() || peers.isEmpty()) {
            return;
        }

        for (FederationPeer peer : peers.values()) {
            executor.submit(() -> {
                metricsService.incrementHealthCheck();
                try {
                    String healthUrl = peer.getUrl() + "/api/health";
                    restTemplate.getForObject(healthUrl, String.class);
                    peer.setStatus(FederationPeer.Status.ACTIVE);
                    peer.setLastContact(Instant.now());
                } catch (Exception e) {
                    peer.setStatus(FederationPeer.Status.UNREACHABLE);
                    metricsService.incrementHealthCheckFailure();
                    log.warn("Peer {} health check failed: {}", peer.getRegion(), e.getMessage());
                }
            });
        }
    }

    // ── Federation Status ────────────────────────────────────────────────────

    /**
     * Get federation status summary.
     */
    public FederationStatus getStatus() {
        int activePeers = (int) peers.values().stream()
                .filter(p -> p.getStatus() == FederationPeer.Status.ACTIVE)
                .count();

        return new FederationStatus(
                config.isEnabled(),
                config.isPrimary(),
                config.getRegion(),
                peers.size(),
                activePeers,
                currentModelVersion
        );
    }

    /**
     * Get the current model version.
     */
    public long getCurrentModelVersion() {
        return currentModelVersion;
    }

    /**
     * Set the current model version (e.g., after model training).
     */
    public void setCurrentModelVersion(long modelVersion) {
        this.currentModelVersion = modelVersion;
    }

    // ── Health Assessment ────────────────────────────────────────────────────

    /**
     * Assess overall federation health and return a comprehensive dashboard DTO.
     */
    public FederationDashboardDto getFederationDashboard() {
        FederationDashboardDto dashboard = new FederationDashboardDto();
        dashboard.setEnabled(config.isEnabled());
        dashboard.setPrimary(config.isPrimary());
        dashboard.setRegion(config.getRegion());
        dashboard.setTimestamp(Instant.now());

        if (!config.isEnabled() || peers.isEmpty()) {
            dashboard.setHealthStatus(FederationDashboardDto.HealthStatus.HEALTHY);
            dashboard.setHealthScore(100);
            dashboard.setPeers(Collections.emptyList());
            dashboard.setLocalNeurons(buildLocalNeuronStats());
            dashboard.setModelVersions(Collections.singletonList(buildLocalModelVersion()));
            dashboard.setSyncHistory(Collections.emptyList());
            dashboard.setRecommendations(Collections.emptyList());
            metricsService.setFederationHealthScore(100);
            return dashboard;
        }

        // Build peer health list
        List<PeerHealthDto> peerHealthList = new ArrayList<>();
        for (FederationPeer peer : peers.values()) {
            peerHealthList.add(buildPeerHealthDto(peer));
        }
        dashboard.setPeers(peerHealthList);

        // Build model version comparison
        List<ModelVersionDto> modelVersions = new ArrayList<>();
        modelVersions.add(buildLocalModelVersion());
        for (FederationPeer peer : peers.values()) {
            ModelVersionDto mv = new ModelVersionDto();
            mv.setRegion(peer.getRegion());
            mv.setModelVersion(peer.getModelVersion());
            mv.setPrimary(false);
            long drift = peer.getModelVersion() - currentModelVersion;
            mv.setVersionDrift(drift);
            mv.setInSync(drift == 0);
            modelVersions.add(mv);
        }
        dashboard.setModelVersions(modelVersions);

        // Local neuron stats
        dashboard.setLocalNeurons(buildLocalNeuronStats());

        // Sync history (last 20)
        List<SyncHistoryDto> recentSync = new ArrayList<>(syncHistory);
        if (recentSync.size() > 20) {
            recentSync = recentSync.subList(recentSync.size() - 20, recentSync.size());
        }
        dashboard.setSyncHistory(recentSync);

        // Calculate health score
        int score = calculateHealthScore();
        dashboard.setHealthScore(score);

        // Determine health status
        if (score >= 70) {
            dashboard.setHealthStatus(FederationDashboardDto.HealthStatus.HEALTHY);
        } else if (score >= 40) {
            dashboard.setHealthStatus(FederationDashboardDto.HealthStatus.DEGRADED);
        } else {
            dashboard.setHealthStatus(FederationDashboardDto.HealthStatus.CRITICAL);
        }

        // Generate recommendations
        dashboard.setRecommendations(generateRecommendations());

        // Update metrics
        metricsService.setFederationHealthScore(score);
        metricsService.updateFromPeers(peers.values(), currentModelVersion);

        return dashboard;
    }

    /**
     * Get health summary for a single peer.
     */
    public PeerHealthDto getPeerHealth(String region) {
        FederationPeer peer = peers.get(region);
        if (peer == null) {
            return null;
        }
        return buildPeerHealthDto(peer);
    }

    /**
     * Detect model version drift across all peers.
     * @return list of regions with non-zero drift
     */
    public List<ModelVersionDto> detectModelVersionDrift() {
        List<ModelVersionDto> drift = new ArrayList<>();
        for (FederationPeer peer : peers.values()) {
            long diff = peer.getModelVersion() - currentModelVersion;
            if (diff != 0) {
                ModelVersionDto mv = new ModelVersionDto();
                mv.setRegion(peer.getRegion());
                mv.setModelVersion(peer.getModelVersion());
                mv.setPrimary(false);
                mv.setVersionDrift(diff);
                mv.setInSync(false);
                drift.add(mv);
            }
        }
        return drift;
    }

    /**
     * Calculate sync lag for all peers (seconds since last contact).
     */
    public Map<String, Long> calculateSyncLag() {
        Map<String, Long> lag = new HashMap<>();
        for (FederationPeer peer : peers.values()) {
            if (peer.getLastContact() != null) {
                long seconds = java.time.Duration.between(peer.getLastContact(), Instant.now()).getSeconds();
                lag.put(peer.getRegion(), seconds);
            } else {
                lag.put(peer.getRegion(), -1L); // never contacted
            }
        }
        return lag;
    }

    /**
     * Get recent sync history.
     */
    public List<SyncHistoryDto> getSyncHistory() {
        return new ArrayList<>(syncHistory);
    }

    // ── Health Assessment Helpers ────────────────────────────────────────────

    private int calculateHealthScore() {
        if (peers.isEmpty()) {
            return 100;
        }

        int score = 100;

        // Deduct for unreachable peers (20 points per unreachable peer, max 40)
        long unreachable = peers.values().stream()
                .filter(p -> p.getStatus() == FederationPeer.Status.UNREACHABLE).count();
        score -= Math.min(40, (int) (unreachable * 20));

        // Deduct for model version drift (10 points per drifting peer, max 30)
        long drifting = peers.values().stream()
                .filter(p -> p.getModelVersion() != currentModelVersion).count();
        score -= Math.min(30, (int) (drifting * 10));

        // Deduct for stale sync (5 points per peer not synced in last 120 seconds, max 20)
        long stale = peers.values().stream()
                .filter(p -> p.getLastContact() != null
                        && java.time.Duration.between(p.getLastContact(), Instant.now()).getSeconds() > 120)
                .count();
        score -= Math.min(20, (int) (stale * 5));

        // Deduct for low neuron count (5 points if local region has 0 neurons)
        if (neuronRegistry.getTotalCount() == 0) {
            score -= 5;
        }

        return Math.max(0, score);
    }

    private List<String> generateRecommendations() {
        List<String> recommendations = new ArrayList<>();

        // Check unreachable peers
        for (FederationPeer peer : peers.values()) {
            if (peer.getStatus() == FederationPeer.Status.UNREACHABLE) {
                recommendations.add("Peer '" + peer.getRegion() + "' is unreachable — check network connectivity to " + peer.getUrl());
            }
        }

        // Check model version drift
        for (FederationPeer peer : peers.values()) {
            long drift = peer.getModelVersion() - currentModelVersion;
            if (drift != 0) {
                if (drift > 0) {
                    recommendations.add("Peer '" + peer.getRegion() + "' is ahead by " + drift + " model version(s) — consider syncing from peer");
                } else {
                    recommendations.add("Peer '" + peer.getRegion() + "' is behind by " + Math.abs(drift) + " model version(s) — propagate model update");
                }
            }
        }

        // Check stale sync
        for (FederationPeer peer : peers.values()) {
            if (peer.getLastContact() != null) {
                long seconds = java.time.Duration.between(peer.getLastContact(), Instant.now()).getSeconds();
                if (seconds > 120) {
                    recommendations.add("Peer '" + peer.getRegion() + "' last contacted " + seconds + "s ago — sync may be stale");
                }
            } else {
                recommendations.add("Peer '" + peer.getRegion() + "' has never been contacted — verify peer URL is correct");
            }
        }

        // Check local neuron count
        if (neuronRegistry.getTotalCount() == 0) {
            recommendations.add("No neurons registered locally — deploy neurons to this region");
        }

        return recommendations;
    }

    private PeerHealthDto buildPeerHealthDto(FederationPeer peer) {
        PeerHealthDto dto = new PeerHealthDto();
        dto.setRegion(peer.getRegion());
        dto.setUrl(peer.getUrl());
        dto.setStatus(peer.getStatus().name());
        dto.setNeuronCount(peer.getNeuronCount());
        dto.setModelVersion(peer.getModelVersion());
        dto.setModelVersionDrift(peer.getModelVersion() - currentModelVersion);
        dto.setLastContact(peer.getLastContact());
        dto.setReachable(peer.getStatus() == FederationPeer.Status.ACTIVE);

        if (peer.getLastContact() != null) {
            dto.setSecondsSinceContact(
                    java.time.Duration.between(peer.getLastContact(), Instant.now()).getSeconds());
        } else {
            dto.setSecondsSinceContact(0);
        }
        return dto;
    }

    private NeuronStatsDto buildLocalNeuronStats() {
        NeuronStatsDto stats = new NeuronStatsDto();
        stats.setTotalNeurons(neuronRegistry.getTotalCount());

        Map<String, Integer> countByStatus = neuronRegistry.getCountByStatus();
        stats.setCountByStatus(countByStatus);
        stats.setActiveNeurons(countByStatus.getOrDefault("ACTIVE", 0));

        // Calculate total experience samples
        int totalExp = 0;
        long maxVersion = 0;
        for (NeuronIdentity n : neuronRegistry.getAllNeurons()) {
            totalExp += n.getExperienceCount();
            if (n.getCurrentModelVersion() > maxVersion) {
                maxVersion = n.getCurrentModelVersion();
            }
        }
        stats.setTotalExperienceSamples(totalExp);
        stats.setModelVersion(maxVersion);
        return stats;
    }

    private ModelVersionDto buildLocalModelVersion() {
        ModelVersionDto mv = new ModelVersionDto();
        mv.setRegion(config.getRegion());
        mv.setModelVersion(currentModelVersion);
        mv.setPrimary(config.isPrimary());
        mv.setInSync(true);
        mv.setVersionDrift(0);
        return mv;
    }

    private void addSyncHistory(String region, String syncType, boolean success,
                                 String errorMessage, long durationMs,
                                 int neuronCount, long modelVersion) {
        SyncHistoryDto entry = new SyncHistoryDto();
        entry.setRegion(region);
        entry.setSyncType(syncType);
        entry.setSuccess(success);
        entry.setErrorMessage(errorMessage);
        entry.setTimestamp(Instant.now());
        entry.setDurationMs(durationMs);
        entry.setNeuronCount(neuronCount);
        entry.setModelVersion(modelVersion);

        synchronized (syncHistory) {
            syncHistory.addLast(entry);
            // Keep last 50 entries
            while (syncHistory.size() > 50) {
                syncHistory.removeFirst();
            }
        }
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private String extractRegionFromUrl(String url) {
        // Try to extract region from URL path or use hostname
        String lower = url.toLowerCase();
        if (lower.contains("us-east")) return "us-east";
        if (lower.contains("eu-west")) return "eu-west";
        if (lower.contains("apac")) return "apac";
        return "unknown-" + System.nanoTime();
    }

    // ── Inner Classes ────────────────────────────────────────────────────────

    /**
     * Federation status summary.
     */
    public static class FederationStatus {
        private boolean enabled;
        private boolean isPrimary;
        private String region;
        private int totalPeers;
        private int activePeers;
        private long modelVersion;

        public FederationStatus(boolean enabled, boolean isPrimary, String region,
                                int totalPeers, int activePeers, long modelVersion) {
            this.enabled = enabled;
            this.isPrimary = isPrimary;
            this.region = region;
            this.totalPeers = totalPeers;
            this.activePeers = activePeers;
            this.modelVersion = modelVersion;
        }

        public boolean isEnabled() { return enabled; }
        public boolean isPrimary() { return isPrimary; }
        public String getRegion() { return region; }
        public int getTotalPeers() { return totalPeers; }
        public int getActivePeers() { return activePeers; }
        public long getModelVersion() { return modelVersion; }
    }
}
