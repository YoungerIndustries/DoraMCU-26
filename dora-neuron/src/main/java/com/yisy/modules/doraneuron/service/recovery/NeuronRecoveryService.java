package com.yisy.modules.doraneuron.service.recovery;

import com.yisy.modules.doraneuron.config.NeuronConfig;
import com.yisy.modules.doraneuron.model.ExperienceSample;
import com.yisy.modules.doraneuron.service.ExperienceCollectorService;
import com.yisy.modules.doraneuron.service.ModelSyncService;
import com.yisy.modules.doraneuron.service.NeuronRegistryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Handles neuron-side recovery scenarios on startup.
 *
 * On startup, this service:
 * 1. Checks for local backup of unflushed experience data
 * 2. Restores experience buffer from disk if coordinator was down
 * 3. Re-downloads ONNX model from coordinator if local model is missing or corrupted
 * 4. Re-registers with the coordinator
 */
@Service
public class NeuronRecoveryService {

    private static final Logger log = LoggerFactory.getLogger(NeuronRecoveryService.class);

    private final NeuronConfig config;
    private final ExperiencePersistenceService experiencePersistence;
    private final ExperienceCollectorService experienceCollector;
    private final ModelSyncService modelSyncService;
    private final NeuronRegistryService registryService;

    public NeuronRecoveryService(NeuronConfig config,
                                  ExperiencePersistenceService experiencePersistence,
                                  ExperienceCollectorService experienceCollector,
                                  ModelSyncService modelSyncService,
                                  NeuronRegistryService registryService) {
        this.config = config;
        this.experiencePersistence = experiencePersistence;
        this.experienceCollector = experienceCollector;
        this.modelSyncService = modelSyncService;
        this.registryService = registryService;
    }

    /**
     * Run recovery procedures after the application is fully ready.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void onStartup() {
        log.info("NeuronRecoveryService starting recovery checks...");

        // 1. Replay persisted experience samples
        replayExperience();

        // 2. Verify and recover ONNX model
        verifyModel();

        // 3. Re-register with coordinator (handled by NeuronRegistryService)
        log.info("NeuronRecoveryService recovery checks completed");
    }

    /**
     * Replay persisted experience samples from disk.
     */
    private void replayExperience() {
        if (!experiencePersistence.isEnabled()) {
            log.info("Experience persistence is disabled; skipping replay");
            return;
        }

        List<ExperienceSample> samples = experiencePersistence.replay();
        if (samples.isEmpty()) {
            log.info("No persisted experience samples to replay");
            return;
        }

        // Re-collect the replayed samples into the experience buffer
        for (ExperienceSample sample : samples) {
            experienceCollector.collect(sample);
        }
        log.info("Replayed {} experience samples into buffer", samples.size());
    }

    /**
     * Verify the local ONNX model and re-download if missing or corrupted.
     */
    private void verifyModel() {
        String modelPath = config.getOnnxModelPath();
        if (modelPath == null || modelPath.isEmpty()) {
            log.info("No ONNX model path configured; skipping model verification");
            return;
        }

        Path path = Paths.get(modelPath).toAbsolutePath().normalize();
        if (!Files.exists(path)) {
            log.warn("Local ONNX model is missing: {}", path);
            log.info("Attempting to re-download model from coordinator...");
            try {
                modelSyncService.downloadAndSwapLatest();
                log.info("Model re-download completed");
            } catch (Exception e) {
                log.error("Failed to re-download model: {}", e.getMessage());
            }
            return;
        }

        // Verify model integrity by checking file size
        try {
            long size = Files.size(path);
            if (size == 0) {
                log.warn("Local ONNX model is empty: {}", path);
                log.info("Attempting to re-download model from coordinator...");
                modelSyncService.downloadAndSwapLatest();
            } else {
                log.info("Local ONNX model verified: {} ({} bytes)", path, size);
            }
        } catch (IOException e) {
            log.error("Failed to verify local model: {}", e.getMessage());
        }
    }

    /**
     * Get the recovery status.
     */
    public Map<String, Object> getStatus() {
        Map<String, Object> status = new LinkedHashMap<>();
        status.put("experiencePersistence", Map.of(
                "enabled", experiencePersistence.isEnabled(),
                "pendingCount", experiencePersistence.getPendingCount(),
                "totalPersisted", experiencePersistence.getTotalPersisted(),
                "totalReplayed", experiencePersistence.getTotalReplayed()
        ));
        status.put("modelPath", config.getOnnxModelPath());
        status.put("modelExists", config.getOnnxModelPath() != null
                && Files.exists(Paths.get(config.getOnnxModelPath())));
        return status;
    }
}
