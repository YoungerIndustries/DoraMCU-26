package com.yisy.modules.doramcu.neuron.service;

import com.yisy.modules.doramcu.neuron.model.FederationPeer;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Centralized Micrometer metrics service for Federation Health monitoring.
 * <p>
 * Exposes federation peer connectivity, sync success/failure, consensus latency,
 * experience forwarding, and model version drift as Prometheus-compatible metrics
 * via the Micrometer registry.
 * </p>
 * <p>
 * All metrics are prefixed with {@code dora.neuron.federation.} to namespace them
 * from other DoraMCU metrics.
 * </p>
 */
@Service
public class FederationMetricsService {

    private static final Logger log = LoggerFactory.getLogger(FederationMetricsService.class);

    private final MeterRegistry meterRegistry;

    // ---- Counters ----

    /** Total sync operations completed successfully. */
    private Counter syncSuccessCounter;

    /** Total sync operations that failed. */
    private Counter syncFailureCounter;

    /** Total cross-region consensus requests. */
    private Counter consensusRequestedCounter;

    /** Total cross-region consensus responses received. */
    private Counter consensusResponseCounter;

    /** Total cross-region consensus timeouts. */
    private Counter consensusTimeoutCounter;

    /** Total experience samples forwarded to primary. */
    private Counter experienceForwardedCounter;

    /** Total experience samples received from peers. */
    private Counter experienceReceivedCounter;

    /** Total model updates propagated to peers. */
    private Counter modelUpdatePropagatedCounter;

    /** Total model updates received from primary. */
    private Counter modelUpdateReceivedCounter;

    /** Total peer health checks. */
    private Counter healthCheckCounter;

    /** Total peer health check failures. */
    private Counter healthCheckFailureCounter;

    // ---- Timers ----

    /** Timer for sync operation duration. */
    private Timer syncDurationTimer;

    /** Timer for cross-region consensus duration. */
    private Timer consensusDurationTimer;

    // ---- Gauges (registered via PostConstruct with suppliers) ----

    /** Current peer count gauge. */
    private final Map<String, Integer> peerCountByStatus = new ConcurrentHashMap<>();

    /** Current model version per region. */
    private final Map<String, Long> modelVersionByRegion = new ConcurrentHashMap<>();

    /** Model version drift per region (from primary). */
    private final Map<String, Long> modelVersionDriftByRegion = new ConcurrentHashMap<>();

    /** Seconds since last sync per peer. */
    private final Map<String, Long> secondsSinceLastSync = new ConcurrentHashMap<>();

    /** Federation health score (0-100). */
    private final Map<String, Integer> federationHealthScore = new ConcurrentHashMap<>();

    public FederationMetricsService(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @PostConstruct
    public void init() {
        log.info("Initializing FederationMetricsService — registering Prometheus metrics");

        // ---- Counters ----
        syncSuccessCounter = Counter.builder("dora.neuron.federation.sync.success")
                .description("Total successful sync operations with peer coordinators")
                .register(meterRegistry);

        syncFailureCounter = Counter.builder("dora.neuron.federation.sync.failure")
                .description("Total failed sync operations with peer coordinators")
                .register(meterRegistry);

        consensusRequestedCounter = Counter.builder("dora.neuron.federation.consensus.requested")
                .description("Total cross-region consensus requests")
                .register(meterRegistry);

        consensusResponseCounter = Counter.builder("dora.neuron.federation.consensus.response")
                .description("Total cross-region consensus responses received")
                .register(meterRegistry);

        consensusTimeoutCounter = Counter.builder("dora.neuron.federation.consensus.timeout")
                .description("Total cross-region consensus timeouts")
                .register(meterRegistry);

        experienceForwardedCounter = Counter.builder("dora.neuron.federation.experience.forwarded")
                .description("Total experience samples forwarded to primary coordinator")
                .register(meterRegistry);

        experienceReceivedCounter = Counter.builder("dora.neuron.federation.experience.received")
                .description("Total experience samples received from peer coordinators")
                .register(meterRegistry);

        modelUpdatePropagatedCounter = Counter.builder("dora.neuron.federation.model.propagated")
                .description("Total model updates propagated to peer coordinators")
                .register(meterRegistry);

        modelUpdateReceivedCounter = Counter.builder("dora.neuron.federation.model.received")
                .description("Total model updates received from primary coordinator")
                .register(meterRegistry);

        healthCheckCounter = Counter.builder("dora.neuron.federation.health.check")
                .description("Total peer health checks performed")
                .register(meterRegistry);

        healthCheckFailureCounter = Counter.builder("dora.neuron.federation.health.check.failure")
                .description("Total failed peer health checks")
                .register(meterRegistry);

        // ---- Timers ----
        syncDurationTimer = Timer.builder("dora.neuron.federation.sync.duration")
                .description("Duration of sync operations with peer coordinators")
                .register(meterRegistry);

        consensusDurationTimer = Timer.builder("dora.neuron.federation.consensus.duration")
                .description("Duration of cross-region consensus operations")
                .register(meterRegistry);

        // ---- Gauges ----
        Gauge.builder("dora.neuron.federation.peers.active", peerCountByStatus,
                map -> (double) map.getOrDefault("ACTIVE", 0))
                .description("Number of active federation peers")
                .register(meterRegistry);

        Gauge.builder("dora.neuron.federation.peers.unreachable", peerCountByStatus,
                map -> (double) map.getOrDefault("UNREACHABLE", 0))
                .description("Number of unreachable federation peers")
                .register(meterRegistry);

        Gauge.builder("dora.neuron.federation.peers.total", peerCountByStatus,
                Map::size)
                .description("Total number of federation peers")
                .register(meterRegistry);

        Gauge.builder("dora.neuron.federation.model.version", modelVersionByRegion,
                map -> map.values().stream().mapToLong(Long::longValue).max().orElse(0))
                .description("Current model version across federation")
                .register(meterRegistry);

        Gauge.builder("dora.neuron.federation.model.drift.max", modelVersionDriftByRegion,
                map -> map.values().stream().mapToLong(Long::longValue).map(Math::abs).max().orElse(0))
                .description("Maximum model version drift across federation")
                .register(meterRegistry);

        Gauge.builder("dora.neuron.federation.health.score", federationHealthScore,
                map -> (double) map.getOrDefault("score", 100))
                .description("Federation health score (0-100)")
                .register(meterRegistry);

        log.info("FederationMetricsService initialized with {} counters, {} timers, {} gauges",
                12, 2, 6);
    }

    // ---- Sync Metrics ----

    public void incrementSyncSuccess() {
        syncSuccessCounter.increment();
    }

    public void incrementSyncFailure() {
        syncFailureCounter.increment();
    }

    public void recordSyncDuration(long durationMs) {
        syncDurationTimer.record(java.time.Duration.ofMillis(durationMs));
    }

    public void updateSecondsSinceLastSync(String region, long seconds) {
        secondsSinceLastSync.put(region, seconds);
    }

    // ---- Consensus Metrics ----

    public void incrementConsensusRequested() {
        consensusRequestedCounter.increment();
    }

    public void incrementConsensusResponse() {
        consensusResponseCounter.increment();
    }

    public void incrementConsensusTimeout() {
        consensusTimeoutCounter.increment();
    }

    public void recordConsensusDuration(long durationMs) {
        consensusDurationTimer.record(java.time.Duration.ofMillis(durationMs));
    }

    // ---- Experience Metrics ----

    public void incrementExperienceForwarded(int count) {
        experienceForwardedCounter.increment(count);
    }

    public void incrementExperienceReceived(int count) {
        experienceReceivedCounter.increment(count);
    }

    // ---- Model Update Metrics ----

    public void incrementModelUpdatePropagated() {
        modelUpdatePropagatedCounter.increment();
    }

    public void incrementModelUpdateReceived() {
        modelUpdateReceivedCounter.increment();
    }

    // ---- Health Check Metrics ----

    public void incrementHealthCheck() {
        healthCheckCounter.increment();
    }

    public void incrementHealthCheckFailure() {
        healthCheckFailureCounter.increment();
    }

    // ---- Gauge Updates ----

    public void updatePeerCountByStatus(Map<String, Integer> counts) {
        peerCountByStatus.clear();
        peerCountByStatus.putAll(counts);
    }

    public void updateModelVersion(String region, long version) {
        modelVersionByRegion.put(region, version);
    }

    public void updateModelVersionDrift(String region, long drift) {
        modelVersionDriftByRegion.put(region, drift);
    }

    public void setFederationHealthScore(int score) {
        federationHealthScore.put("score", score);
    }

    // ---- Batch update from peers ----

    /**
     * Update all peer-related gauges from the current peer list.
     */
    public void updateFromPeers(java.util.Collection<FederationPeer> peers, long localModelVersion) {
        Map<String, Integer> statusCounts = new ConcurrentHashMap<>();
        for (FederationPeer peer : peers) {
            String status = peer.getStatus().name();
            statusCounts.merge(status, 1, Integer::sum);
            updateModelVersion(peer.getRegion(), peer.getModelVersion());
            updateModelVersionDrift(peer.getRegion(), peer.getModelVersion() - localModelVersion);

            if (peer.getLastContact() != null) {
                long seconds = java.time.Duration.between(peer.getLastContact(), java.time.Instant.now()).getSeconds();
                updateSecondsSinceLastSync(peer.getRegion(), seconds);
            }
        }
        updatePeerCountByStatus(statusCounts);
    }
}
