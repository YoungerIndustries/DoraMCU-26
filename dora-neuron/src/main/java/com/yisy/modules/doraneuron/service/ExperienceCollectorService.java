package com.yisy.modules.doraneuron.service;

import com.yisy.modules.doraneuron.config.NeuronConfig;
import com.yisy.modules.doraneuron.config.NeuronPrometheusMetrics;
import com.yisy.modules.doraneuron.model.ExperienceSample;
import com.yisy.modules.doraneuron.model.NeuronIdentity;
import com.yisy.modules.doraneuron.protocol.ExperienceBatch;
import com.yisy.modules.doraneuron.protocol.ws.ExperienceStreamWsMessage;
import com.yisy.modules.doraneuron.producer.NeuronEventProducer;
import com.yisy.modules.doraneuron.service.recovery.ExperiencePersistenceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Buffers experience samples locally and periodically flushes them to the
 * central coordinator for aggregation and model retraining.
 *
 * This service implements the "memory consolidation" pattern of the Neuron
 * network: each Neuron collects local experience (state, action, reward)
 * tuples, buffers them, and periodically sends them to the coordinator.
 * The coordinator aggregates experiences from all Neurons and triggers
 * retraining of the central DL model.
 *
 * Flushing occurs when:
 * <ul>
 *   <li>The buffer reaches {@code neuron.experience-buffer-size} samples</li>
 *   <li>The periodic flush interval ({@code neuron.experience-flush-interval-ms}) elapses</li>
 *   <li>A manual flush is triggered via the API</li>
 * </ul>
 */
@Service
public class ExperienceCollectorService {

    private static final Logger log = LoggerFactory.getLogger(ExperienceCollectorService.class);

    private final NeuronConfig config;
    private final RestTemplate restTemplate;
    private final NeuronRegistryService registryService;
    private final NeuronWebSocketClient webSocketClient;
    private final NeuronPrometheusMetrics prometheusMetrics;
    private final ExperiencePersistenceService experiencePersistence;
    private final NeuronEventProducer eventProducer; // null if Kafka not configured

    /** Thread-safe queue for buffering experience samples. */
    private final java.util.concurrent.ConcurrentLinkedQueue<ExperienceSample> buffer =
        new java.util.concurrent.ConcurrentLinkedQueue<>();

    /** Cumulative count of all samples collected since startup. */
    private final AtomicLong totalCollected = new AtomicLong(0);

    /** Cumulative count of all samples successfully flushed to coordinator. */
    private final AtomicLong totalFlushed = new AtomicLong(0);

    /** Cumulative count of flush failures. */
    private final AtomicLong flushFailures = new AtomicLong(0);

    public ExperienceCollectorService(NeuronConfig config, RestTemplate restTemplate,
                                      NeuronRegistryService registryService,
                                      NeuronWebSocketClient webSocketClient,
                                      NeuronPrometheusMetrics prometheusMetrics,
                                      ExperiencePersistenceService experiencePersistence,
                                      @Autowired(required = false) NeuronEventProducer eventProducer) {
        this.config = config;
        this.restTemplate = restTemplate;
        this.registryService = registryService;
        this.webSocketClient = webSocketClient;
        this.prometheusMetrics = prometheusMetrics;
        this.experiencePersistence = experiencePersistence;
        this.eventProducer = eventProducer;
    }

    @PostConstruct
    public void init() {
        log.info("ExperienceCollectorService initialized — buffer size: {}, flush interval: {}ms",
                config.getExperienceBufferSize(), config.getExperienceFlushIntervalMs());
    }

    /**
     * Collect a single experience sample.
     *
     * The sample is enriched with the Neuron's nodeId and the current model version,
     * then added to the local buffer. If the buffer reaches capacity, an immediate
     * flush is triggered.
     *
     * @param sample the experience sample to collect
     */
    public void collect(ExperienceSample sample) {
        enrichSample(sample);
        buffer.offer(sample);
        totalCollected.incrementAndGet();
        prometheusMetrics.recordExperienceCollected(1);
        prometheusMetrics.setExperienceBufferSize(buffer.size());

        // Update identity's experience count
        updateExperienceCount();

        // Flush immediately if buffer is full
        if (buffer.size() >= config.getExperienceBufferSize()) {
            log.info("Buffer full ({} samples) — flushing immediately", buffer.size());
            flushImmediately();
        }
    }

    /**
     * Collect multiple experience samples at once.
     *
     * @param samples the list of experience samples to collect
     */
    public void collectAll(List<ExperienceSample> samples) {
        for (ExperienceSample sample : samples) {
            enrichSample(sample);
            buffer.offer(sample);
            totalCollected.incrementAndGet();
        }
        prometheusMetrics.recordExperienceCollected(samples.size());
        prometheusMetrics.setExperienceBufferSize(buffer.size());
        updateExperienceCount();

        if (buffer.size() >= config.getExperienceBufferSize()) {
            log.info("Buffer full ({} samples) — flushing immediately", buffer.size());
            flushImmediately();
        }
    }

    /**
     * Periodic flush — sends buffered samples to the coordinator at configured intervals.
     */
    @Scheduled(fixedDelayString = "${neuron.experience-flush-interval-ms:30000}")
    public void flushPeriodically() {
        if (!buffer.isEmpty()) {
            log.debug("Periodic flush triggered — {} samples in buffer", buffer.size());
            flushImmediately();
        }
    }

    /**
     * Manually trigger an immediate flush of all buffered samples.
     */
    public void flushNow() {
        log.info("Manual flush triggered — {} samples in buffer", buffer.size());
        flushImmediately();
    }

    /**
     * Drain the buffer and send all samples to the coordinator as a batch.
     * Tries Kafka first (event-driven), then WebSocket (real-time), then HTTP POST (fallback).
     */
    private void flushImmediately() {
        // Drain the buffer using an iterator to avoid ConcurrentLinkedQueue.drainTo() issues
        List<ExperienceSample> batch = new ArrayList<>();
        Iterator<ExperienceSample> it = buffer.iterator();
        while (it.hasNext()) {
            ExperienceSample sample = it.next();
            batch.add(sample);
            it.remove();
        }

        if (batch.isEmpty()) {
            return;
        }

        // Persist to disk before sending to coordinator for disaster recovery
        experiencePersistence.persistAll(batch);

        String nodeId = getNodeId();
        log.info("Flushing experience batch: {} samples from {}", batch.size(), nodeId);

        ExperienceBatch experienceBatch = new ExperienceBatch(nodeId, batch, totalCollected.get());

        // Try Kafka first (event-driven, async)
        if (eventProducer != null && eventProducer.isAvailable()) {
            try {
                eventProducer.publishExperience(experienceBatch);
                totalFlushed.addAndGet(batch.size());
                prometheusMetrics.recordExperienceFlushed(batch.size());
                prometheusMetrics.setExperienceBufferSize(buffer.size());
                log.info("Experience batch published via Kafka: {} samples (total flushed: {})",
                        batch.size(), totalFlushed.get());
                resetExperienceCount();
                return;
            } catch (Exception e) {
                log.warn("Kafka experience publish failed: {} — falling back to WebSocket/HTTP", e.getMessage());
            }
        }

        // Try WebSocket (real-time streaming)
        if (webSocketClient.isConnected()) {
            try {
                List<ExperienceStreamWsMessage.ExperienceSampleDto> dtoList = new ArrayList<>();
                for (ExperienceSample sample : batch) {
                    dtoList.add(new ExperienceStreamWsMessage.ExperienceSampleDto(
                            sample.getState(), sample.getAction(), sample.getReward(),
                            sample.getNextState(), sample.isDone(),
                            sample.getConfidence(), sample.getModelVersion()
                    ));
                }
                webSocketClient.streamExperience(dtoList, totalCollected.get());
                totalFlushed.addAndGet(batch.size());
                prometheusMetrics.recordExperienceFlushed(batch.size());
                prometheusMetrics.setExperienceBufferSize(buffer.size());
                log.info("Experience batch streamed via WebSocket: {} samples (total flushed: {})",
                        batch.size(), totalFlushed.get());
                resetExperienceCount();
                return;
            } catch (Exception e) {
                log.warn("WebSocket experience stream failed: {} — falling back to HTTP", e.getMessage());
            }
        }

        // Fallback to HTTP POST
        String url = config.getCoordinatorUrl() + "/api/v1/neuron/experience/batch";
        try {
            ResponseEntity<Void> response = restTemplate.postForEntity(url, experienceBatch, Void.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                totalFlushed.addAndGet(batch.size());
                prometheusMetrics.recordExperienceFlushed(batch.size());
                prometheusMetrics.setExperienceBufferSize(buffer.size());
                log.info("Experience batch flushed via HTTP: {} samples (total flushed: {})",
                        batch.size(), totalFlushed.get());
                resetExperienceCount();
            } else {
                log.warn("Coordinator returned {} during experience flush — re-queuing samples",
                        response.getStatusCode());
                flushFailures.incrementAndGet();
                prometheusMetrics.recordExperienceFlushError();
                buffer.addAll(batch);
            }
        } catch (Exception e) {
            log.warn("Failed to flush experience batch: {} — re-queuing {} samples",
                    e.getMessage(), batch.size());
            flushFailures.incrementAndGet();
            prometheusMetrics.recordExperienceFlushError();
            buffer.addAll(batch);
        }
    }

    /**
     * Enrich a sample with Neuron metadata (nodeId, modelVersion, timestamp).
     */
    private void enrichSample(ExperienceSample sample) {
        if (sample.getNodeId() == null) {
            sample.setNodeId(getNodeId());
        }
        if (sample.getTimestamp() == 0) {
            sample.setTimestamp(System.currentTimeMillis());
        }
        // Set model version from SatelliteAiService if available
        if (sample.getModelVersion() == 0) {
            NeuronIdentity identity = registryService.getIdentity();
            if (identity != null) {
                sample.setModelVersion(identity.getCurrentModelVersion());
            }
        }
    }

    /**
     * Get the current Neuron's node ID.
     */
    private String getNodeId() {
        NeuronIdentity identity = registryService.getIdentity();
        if (identity != null) {
            return identity.getNodeId();
        }
        return config.getNodeId() != null ? config.getNodeId() : "unknown";
    }

    /**
     * Update the experience count in the Neuron's identity.
     */
    private void updateExperienceCount() {
        NeuronIdentity identity = registryService.getIdentity();
        if (identity != null) {
            identity.setExperienceCount(buffer.size());
        }
    }

    /**
     * Reset the experience count in the Neuron's identity after successful flush.
     */
    private void resetExperienceCount() {
        NeuronIdentity identity = registryService.getIdentity();
        if (identity != null) {
            identity.setExperienceCount(0);
        }
    }

    // ── Status queries ────────────────────────────────────────────────────────

    /**
     * Return the current number of samples in the buffer.
     */
    public int getBufferSize() {
        return buffer.size();
    }

    /**
     * Return the cumulative count of all samples collected since startup.
     */
    public long getTotalCollected() {
        return totalCollected.get();
    }

    /**
     * Return the cumulative count of all samples successfully flushed.
     */
    public long getTotalFlushed() {
        return totalFlushed.get();
    }

    /**
     * Return the cumulative count of flush failures.
     */
    public long getFlushFailures() {
        return flushFailures.get();
    }

    /**
     * Get samples from the buffer for P2P mesh synchronization.
     * This method retrieves samples without removing them from the buffer,
     * allowing mesh peers to share experience while keeping the local copy
     * for coordinator submission.
     *
     * @param maxSamples maximum number of samples to return
     * @return list of experience samples for sharing
     */
    public List<ExperienceSample> getSamplesForSync(int maxSamples) {
        List<ExperienceSample> samples = new ArrayList<>();
        int count = 0;
        Iterator<ExperienceSample> iterator = buffer.iterator();
        
        // Peek at samples without removing them
        while (iterator.hasNext() && count < maxSamples) {
            samples.add(iterator.next());
            count++;
        }
        
        return samples;
    }
}
