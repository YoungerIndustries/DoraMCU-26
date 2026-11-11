package com.yisy.modules.doraneuron.service;

import com.yisy.modules.doraneuron.config.NeuronConfig;
import com.yisy.modules.doraneuron.config.NeuronPrometheusMetrics;
import com.yisy.modules.doraneuron.model.NeuronIdentity;
import com.yisy.modules.doraneuron.protocol.ModelUpdateNotification;
import com.yisy.modules.doraneuron.security.ModelSignatureService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import jakarta.annotation.PostConstruct;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

/**
 * Handles ONNX model synchronization from the central coordinator.
 *
 * On startup, this service fetches the latest model info from the coordinator
 * and downloads the model if available. It also exposes an endpoint for the
 * coordinator to push model updates to this Neuron.
 */
@Service
@ConditionalOnBean(SatelliteAiService.class)
public class ModelSyncService {

    private static final Logger log = LoggerFactory.getLogger(ModelSyncService.class);

    private final NeuronConfig config;
    private final RestTemplate restTemplate;
    private final SatelliteAiService satelliteAiService;
    private final NeuronRegistryService registryService;

    private final NeuronWebSocketClient webSocketClient;
    private final ModelSignatureService modelSignatureService;
    private final NeuronPrometheusMetrics prometheusMetrics;

    public ModelSyncService(NeuronConfig config, RestTemplate restTemplate,
                            SatelliteAiService satelliteAiService,
                            NeuronRegistryService registryService,
                            NeuronWebSocketClient webSocketClient,
                            ModelSignatureService modelSignatureService,
                            NeuronPrometheusMetrics prometheusMetrics) {
        this.config = config;
        this.restTemplate = restTemplate;
        this.satelliteAiService = satelliteAiService;
        this.registryService = registryService;
        this.webSocketClient = webSocketClient;
        this.modelSignatureService = modelSignatureService;
        this.prometheusMetrics = prometheusMetrics;
    }

    /**
     * On startup, attempt to download the latest model from the coordinator
     * and register for WebSocket model sync notifications.
     */
    @PostConstruct
    public void init() {
        log.info("ModelSyncService initialized — checking for latest model from coordinator");
        downloadAndSwapLatest();

        // Register for real-time model sync via WebSocket
        webSocketClient.addModelSyncCallback(this::handleModelSyncWs);
    }

    /**
     * Handle a model sync message received via WebSocket from the coordinator.
     */
    public void handleModelSyncWs(long version, String downloadUrl, String sha256Hash) {
        log.info("WebSocket model sync received: v{} from {}", version, downloadUrl);
        downloadAndSwapModel(downloadUrl, version, sha256Hash, null);
    }

    /**
     * Fetch model info from the coordinator and download the latest model.
     */
    @SuppressWarnings("unchecked")
    public void downloadAndSwapLatest() {
        String url = config.getCoordinatorUrl() + "/api/v1/dl/model/info";
        try {
            Map<String, Object> info = restTemplate.getForObject(url, Map.class);
            if (info == null) {
                log.warn("No model info response from coordinator");
                return;
            }
            String downloadUrl = (String) info.get("downloadUrl");
            long version = info.get("version") != null
                    ? ((Number) info.get("version")).longValue() : 0;
            String expectedHash = (String) info.get("sha256Hash");
            String modelSignature = (String) info.get("modelSignature");

            if (downloadUrl == null || downloadUrl.isEmpty()) {
                log.warn("No download URL in model info");
                return;
            }

            downloadAndSwapModel(downloadUrl, version, expectedHash, modelSignature);

        } catch (Exception e) {
            log.warn("Failed to fetch model info from coordinator: {}", e.getMessage());
        }
    }

    /**
     * Handle a model update notification pushed by the coordinator.
     *
     * @param notification the model update notification
     */
    public void handleModelUpdate(ModelUpdateNotification notification) {
        log.info("Received model update notification: {}", notification);
        downloadAndSwapModel(notification.getUrl(), notification.getVersion(), 
                notification.getSha256Hash(), notification.getModelSignature());
    }

    /**
     * Handle a model update event received via Kafka from the coordinator.
     * This method is called by ModelUpdateEventConsumer.
     *
     * @param url the model download URL
     * @param version the model version
     * @param sha256Hash the expected SHA-256 hash
     * @param modelSignature the RSA signature (optional)
     */
    public void handleModelUpdateFromKafka(String url, long version, String sha256Hash, String modelSignature) {
        // Version comparison: skip if we already have this version
        long currentVersion = satelliteAiService.getCurrentModelVersion();
        if (currentVersion == version) {
            log.debug("Kafka model update skipped: already running v{}", version);
            return;
        }

        log.info("Kafka model update received: v{} from {} (current: v{})", version, url, currentVersion);
        downloadAndSwapModel(url, version, sha256Hash, modelSignature);
    }

    /**
     * Download a specific model version from the coordinator.
     * Used for A/B test assignment or manual version selection.
     *
     * @param version the version number to download
     */
    public void downloadSpecificVersion(long version) {
        String url = config.getCoordinatorUrl() + "/api/v1/dl/model/version/" + version + "/download";
        try {
            log.info("Downloading specific model version: v{}", version);
            downloadAndSwapModel(url, version, null, null);
        } catch (Exception e) {
            log.warn("Failed to download specific model version {}: {}", version, e.getMessage());
        }
    }

    /**
     * Download a model from the given URL, verify its hash and signature, and swap it into the inference engine.
     */
    private void downloadAndSwapModel(String downloadUrl, long version, String expectedHash, String modelSignature) {
        // Ensure model directory exists
        String modelDir = config.getOnnxModelPath();
        if (modelDir == null || modelDir.isEmpty()) {
            modelDir = "dl-models";
        }
        Path modelPath = Paths.get(modelDir);
        try {
            Files.createDirectories(modelPath);
        } catch (IOException e) {
            log.error("Failed to create model directory: {}", e.getMessage());
            return;
        }

        String modelFileName = "neuron-model-v" + version + ".onnx";
        Path targetPath = modelPath.resolve(modelFileName);

        // Download the model
        try {
            log.info("Downloading ONNX model from: {} to {}", downloadUrl, targetPath);
            ResponseEntity<byte[]> response = restTemplate.exchange(
                    downloadUrl, HttpMethod.GET, null, byte[].class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                byte[] modelBytes = response.getBody();

                // Verify hash if expected
                if (expectedHash != null && !expectedHash.isEmpty()) {
                    String actualHash = sha256(modelBytes);
                    if (!expectedHash.equalsIgnoreCase(actualHash)) {
                        log.error("Model hash mismatch! expected={}, actual={}", expectedHash, actualHash);
                        return;
                    }
                    log.info("Model hash verified: {}", actualHash);
                }

                // Write to file
                try (FileOutputStream fos = new FileOutputStream(targetPath.toString())) {
                    fos.write(modelBytes);
                }
                log.info("Model downloaded: {} ({} bytes)", targetPath, modelBytes.length);

                // Verify model signature if provided (Phase 8)
                if (modelSignature != null && !modelSignature.isEmpty()) {
                    boolean signatureValid = modelSignatureService.verifySignatureWithBase64(
                            targetPath.toString(), modelSignature);
                    if (!signatureValid) {
                        log.error("Model signature verification failed! Rejecting model v{}", version);
                        return;
                    }
                    log.info("Model signature verified for v{}", version);
                }

                // Swap into the inference engine
                satelliteAiService.swapModel(targetPath.toString(), version);

                // Update identity's model version
                NeuronIdentity identity = registryService.getIdentity();
                if (identity != null) {
                    identity.setCurrentModelVersion(version);
                }
                prometheusMetrics.recordModelSync();
                prometheusMetrics.setModelVersion(version);
                log.info("Model swap complete. Neuron now running model version {}", version);
            } else {
                prometheusMetrics.recordModelSyncError();
                log.warn("Failed to download model: {}", response.getStatusCode());
            }
        } catch (Exception e) {
            prometheusMetrics.recordModelSyncError();
            log.error("Failed to download and swap model: {}", e.getMessage());
        }
    }

    /**
     * Compute SHA-256 hash of the given bytes.
     */
    private String sha256(byte[] data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data);
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }
}
