package com.yisy.modules.doraneuron.config;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Exposes all Neuron metrics via Micrometer for Prometheus scraping.
 *
 * Replaces manual AtomicLong counters with proper Micrometer instruments
 * that Prometheus can scrape at /actuator/prometheus.
 *
 * Metrics categories:
 * - neuron_http_*: HTTP request counters and error rates
 * - neuron_inference_*: AI inference counters, timers, and errors
 * - neuron_experience_*: Experience collection counters
 * - neuron_consensus_*: Consensus voting counters
 * - neuron_mesh_*: P2P mesh network metrics
 * - neuron_unity_*: Unity client connection metrics
 * - neuron_model_*: Model version and sync metrics
 * - neuron_jvm_*: JVM memory and CPU gauges
 */
@Service
public class NeuronPrometheusMetrics {

    private static final Logger log = LoggerFactory.getLogger(NeuronPrometheusMetrics.class);

    private final MeterRegistry registry;

    // ── HTTP Metrics ───────────────────────────────────────────────────────

    private Counter httpRequestsTotal;
    private Counter httpErrorsTotal;

    // ── Inference Metrics ──────────────────────────────────────────────────

    private Counter inferenceTotal;
    private Counter inferenceErrorsTotal;
    private Timer inferenceTimer;

    // ── Experience Metrics ─────────────────────────────────────────────────

    private Counter experienceCollectedTotal;
    private Counter experienceFlushedTotal;
    private Counter experienceFlushErrorsTotal;
    private final AtomicLong experienceBufferSize = new AtomicLong(0);

    // ── Consensus Metrics ──────────────────────────────────────────────────

    private Counter consensusRequestedTotal;
    private Counter consensusVotesCastTotal;
    private Counter consensusErrorsTotal;
    private Timer consensusTimer;

    // ── Mesh Metrics ───────────────────────────────────────────────────────

    private Counter meshMessagesSentTotal;
    private Counter meshMessagesReceivedTotal;
    private Counter meshErrorsTotal;
    private final AtomicInteger meshPeerCount = new AtomicInteger(0);

    // ── Unity Metrics ──────────────────────────────────────────────────────

    private Counter unityPredictionsTotal;
    private Counter unityExperienceUploadedTotal;
    private final AtomicInteger unityConnectedClients = new AtomicInteger(0);

    // ── Model Metrics ──────────────────────────────────────────────────────

    private Counter modelSyncsTotal;
    private Counter modelSyncErrorsTotal;
    private final AtomicReference<Long> modelVersion = new AtomicReference<>(0L);

    // ── WebSocket Metrics ──────────────────────────────────────────────────

    private Counter websocketReconnectsTotal;
    private final AtomicInteger websocketConnected = new AtomicInteger(0);

    public NeuronPrometheusMetrics(MeterRegistry registry) {
        this.registry = registry;
    }

    @PostConstruct
    public void init() {
        // ── HTTP Counters ────────────────────────────────────────────────
        httpRequestsTotal = Counter.builder("neuron.http.requests.total")
                .description("Total HTTP requests received by the Neuron")
                .register(registry);

        httpErrorsTotal = Counter.builder("neuron.http.errors.total")
                .description("Total HTTP errors returned by the Neuron")
                .register(registry);

        // ── Inference Counters and Timer ─────────────────────────────────
        inferenceTotal = Counter.builder("neuron.inference.total")
                .description("Total AI inference requests processed")
                .register(registry);

        inferenceErrorsTotal = Counter.builder("neuron.inference.errors.total")
                .description("Total AI inference errors")
                .register(registry);

        inferenceTimer = Timer.builder("neuron.inference.duration")
                .description("AI inference duration in seconds")
                .register(registry);

        // ── Experience Counters and Gauge ────────────────────────────────
        experienceCollectedTotal = Counter.builder("neuron.experience.collected.total")
                .description("Total experience samples collected")
                .register(registry);

        experienceFlushedTotal = Counter.builder("neuron.experience.flushed.total")
                .description("Total experience samples flushed to coordinator")
                .register(registry);

        experienceFlushErrorsTotal = Counter.builder("neuron.experience.flush.errors.total")
                .description("Total experience flush errors")
                .register(registry);

        Gauge.builder("neuron.experience.buffer.size", experienceBufferSize, AtomicLong::get)
                .description("Current experience buffer size")
                .register(registry);

        // ── Consensus Counters and Timer ─────────────────────────────────
        consensusRequestedTotal = Counter.builder("neuron.consensus.requested.total")
                .description("Total consensus requests initiated")
                .register(registry);

        consensusVotesCastTotal = Counter.builder("neuron.consensus.votes.cast.total")
                .description("Total consensus votes cast by this Neuron")
                .register(registry);

        consensusErrorsTotal = Counter.builder("neuron.consensus.errors.total")
                .description("Total consensus errors")
                .register(registry);

        consensusTimer = Timer.builder("neuron.consensus.duration")
                .description("Consensus voting duration in seconds")
                .register(registry);

        // ── Mesh Counters and Gauges ─────────────────────────────────────
        meshMessagesSentTotal = Counter.builder("neuron.mesh.messages.sent.total")
                .description("Total P2P mesh messages sent")
                .register(registry);

        meshMessagesReceivedTotal = Counter.builder("neuron.mesh.messages.received.total")
                .description("Total P2P mesh messages received")
                .register(registry);

        meshErrorsTotal = Counter.builder("neuron.mesh.errors.total")
                .description("Total P2P mesh errors")
                .register(registry);

        Gauge.builder("neuron.mesh.peers.active", meshPeerCount, AtomicInteger::get)
                .description("Number of active P2P mesh peers")
                .register(registry);

        // ── Unity Counters and Gauges ────────────────────────────────────
        unityPredictionsTotal = Counter.builder("neuron.unity.predictions.total")
                .description("Total AI predictions served to Unity clients")
                .register(registry);

        unityExperienceUploadedTotal = Counter.builder("neuron.unity.experience.uploaded.total")
                .description("Total experience samples uploaded from Unity clients")
                .register(registry);

        Gauge.builder("neuron.unity.clients.connected", unityConnectedClients, AtomicInteger::get)
                .description("Number of connected Unity game clients")
                .register(registry);

        // ── Model Counters and Gauges ────────────────────────────────────
        modelSyncsTotal = Counter.builder("neuron.model.syncs.total")
                .description("Total successful model syncs")
                .register(registry);

        modelSyncErrorsTotal = Counter.builder("neuron.model.sync.errors.total")
                .description("Total model sync errors")
                .register(registry);

        Gauge.builder("neuron.model.version", modelVersion, AtomicReference::get)
                .description("Current ONNX model version loaded")
                .register(registry);

        // ── WebSocket Counters and Gauges ────────────────────────────────
        websocketReconnectsTotal = Counter.builder("neuron.websocket.reconnects.total")
                .description("Total WebSocket reconnection attempts")
                .register(registry);

        Gauge.builder("neuron.websocket.connected", websocketConnected, AtomicInteger::get)
                .description("WebSocket connection status (1=connected, 0=disconnected)")
                .register(registry);

        log.info("Neuron Prometheus metrics registered successfully");
    }

    // ── HTTP Methods ───────────────────────────────────────────────────────

    public void recordHttpRequest() {
        httpRequestsTotal.increment();
    }

    public void recordHttpError() {
        httpErrorsTotal.increment();
    }

    // ── Inference Methods ─────────────────────────────────────────────────

    public void recordInference(long durationMs) {
        inferenceTotal.increment();
        inferenceTimer.record(durationMs, java.util.concurrent.TimeUnit.MILLISECONDS);
    }

    public void recordInferenceError() {
        inferenceErrorsTotal.increment();
    }

    // ── Experience Methods ─────────────────────────────────────────────────

    public void recordExperienceCollected(int count) {
        experienceCollectedTotal.increment(count);
    }

    public void recordExperienceFlushed(int count) {
        experienceFlushedTotal.increment(count);
    }

    public void recordExperienceFlushError() {
        experienceFlushErrorsTotal.increment();
    }

    public void setExperienceBufferSize(long size) {
        experienceBufferSize.set(size);
    }

    // ── Consensus Methods ─────────────────────────────────────────────────

    public void recordConsensusRequested() {
        consensusRequestedTotal.increment();
    }

    public void recordConsensusVote() {
        consensusVotesCastTotal.increment();
    }

    public void recordConsensusError() {
        consensusErrorsTotal.increment();
    }

    public void recordConsensusDuration(long durationMs) {
        consensusTimer.record(durationMs, java.util.concurrent.TimeUnit.MILLISECONDS);
    }

    // ── Mesh Methods ──────────────────────────────────────────────────────

    public void recordMeshMessageSent() {
        meshMessagesSentTotal.increment();
    }

    public void recordMeshMessageReceived() {
        meshMessagesReceivedTotal.increment();
    }

    public void recordMeshError() {
        meshErrorsTotal.increment();
    }

    public void setMeshPeerCount(int count) {
        meshPeerCount.set(count);
    }

    // ── Unity Methods ─────────────────────────────────────────────────────

    public void recordUnityPrediction() {
        unityPredictionsTotal.increment();
    }

    public void recordUnityExperienceUploaded(int count) {
        unityExperienceUploadedTotal.increment(count);
    }

    public void setUnityConnectedClients(int count) {
        unityConnectedClients.set(count);
    }

    // ── Model Methods ─────────────────────────────────────────────────────

    public void recordModelSync() {
        modelSyncsTotal.increment();
    }

    public void recordModelSyncError() {
        modelSyncErrorsTotal.increment();
    }

    public void setModelVersion(long version) {
        modelVersion.set(version);
    }

    // ── WebSocket Methods ─────────────────────────────────────────────────

    public void recordWebSocketReconnect() {
        websocketReconnectsTotal.increment();
    }

    public void setWebSocketConnected(boolean connected) {
        websocketConnected.set(connected ? 1 : 0);
    }
}
