package com.yisy.modules.doraneuron.service.mesh;

import com.yisy.modules.doraneuron.config.NeuronConfig;
import com.yisy.modules.doraneuron.model.ExperienceSample;
import com.yisy.modules.doraneuron.model.NeuronIdentity;
import com.yisy.modules.doraneuron.protocol.mesh.MeshExperienceSync;
import com.yisy.modules.doraneuron.protocol.mesh.MeshMessage;
import com.yisy.modules.doraneuron.service.ExperienceCollectorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Implements direct experience sharing between neurons via P2P mesh.
 *
 * This service periodically shares experience samples with mesh peers
 * for distributed learning. When a neuron collects experience samples,
 * it can share them with peers so they can benefit from the learning
 * without waiting for centralized aggregation.
 *
 * This provides faster learning propagation and reduces the load on
 * the coordinator's experience aggregation service.
 */
@Service
@ConditionalOnProperty(prefix = "neuron", name = "mesh-experience-sync-enabled", havingValue = "true", matchIfMissing = true)
public class MeshExperienceSyncService implements NeuronMeshService.MeshMessageCallback {

    private static final Logger log = LoggerFactory.getLogger(MeshExperienceSyncService.class);

    private final NeuronConfig config;
    private final NeuronIdentity identity;
    private final NeuronMeshService meshService;
    private final ExperienceCollectorService experienceCollector;

    /** Statistics tracking. */
    private final AtomicLong samplesSent = new AtomicLong(0);
    private final AtomicLong samplesReceived = new AtomicLong(0);
    private final AtomicLong syncRounds = new AtomicLong(0);

    public MeshExperienceSyncService(NeuronConfig config, NeuronIdentity identity,
                                      NeuronMeshService meshService,
                                      @Autowired(required = false) ExperienceCollectorService experienceCollector) {
        this.config = config;
        this.identity = identity;
        this.meshService = meshService;
        this.experienceCollector = experienceCollector;
    }

    @PostConstruct
    public void init() {
        meshService.addMessageCallback(this);
        log.info("MeshExperienceSyncService initialized");
    }

    /**
     * Periodically share experience samples with mesh peers.
     */
    @Scheduled(fixedDelayString = "${neuron.experience-flush-interval-ms:30000}")
    public void syncExperience() {
        if (meshService.getPeerCount() == 0) {
            return;
        }

        if (experienceCollector == null) {
            log.warn("Cannot sync experience: ExperienceCollectorService unavailable");
            return;
        }

        // Get samples from the experience collector
        List<ExperienceSample> samples = experienceCollector.getSamplesForSync(
                config.getExperienceBufferSize() / 2);

        if (samples.isEmpty()) {
            return;
        }

        // Create sync message
        MeshExperienceSync sync = new MeshExperienceSync(
                identity.getNodeId(),
                samples,
                (int) Math.min(experienceCollector.getTotalCollected(), Integer.MAX_VALUE),
                identity.getCurrentModelVersion()
        );
        sync.setMaxHops(config.getMeshMaxHops());

        // Broadcast to all peers
        meshService.broadcast(sync);
        samplesSent.addAndGet(samples.size());
        syncRounds.incrementAndGet();
        log.debug("Synced {} experience samples to {} peers", samples.size(), meshService.getPeerCount());
    }

    /**
     * Handle incoming mesh messages (callback from NeuronMeshService).
     */
    @Override
    public void onMeshMessage(MeshMessage message) {
        if (message instanceof MeshExperienceSync) {
            handleExperienceSync((MeshExperienceSync) message);
        }
    }

    /**
     * Handle an incoming experience sync message from a peer.
     */
    private void handleExperienceSync(MeshExperienceSync sync) {
        if (sync.isIsRequest()) {
            // Respond with our samples
            syncExperience();
            return;
        }

        if (sync.getSamples() == null || sync.getSamples().isEmpty()) {
            return;
        }

        // Process received samples
        int count = 0;
        for (ExperienceSample sample : sync.getSamples()) {
            // Skip samples from ourselves
            if (identity.getNodeId().equals(sample.getNodeId())) {
                continue;
            }

            // Add received samples to local buffer for coordinator submission
            if (experienceCollector != null) {
                experienceCollector.collect(sample);
                count++;
            }
        }

        if (count > 0) {
            samplesReceived.addAndGet(count);
            log.debug("Received {} experience samples from peer: {}",
                    count, sync.getFromNodeId());
        }
    }

    /**
     * Request experience samples from mesh peers.
     */
    public void requestExperienceFromPeers() {
        if (meshService.getPeerCount() == 0) {
            log.warn("No mesh peers available for experience request");
            return;
        }

        MeshExperienceSync request = MeshExperienceSync.createRequest(identity.getNodeId());
        meshService.broadcast(request);
        log.info("Requested experience samples from {} peers", meshService.getPeerCount());
    }

    // ── Getters for monitoring ────────────────────────────────────────────────

    public long getSamplesSent() {
        return samplesSent.get();
    }

    public long getSamplesReceived() {
        return samplesReceived.get();
    }

    public long getSyncRounds() {
        return syncRounds.get();
    }
}
