package com.yisy.modules.doraneuron.service.brainnode;

import com.yisy.modules.doraneuron.config.BrainNodeConfig;
import com.yisy.modules.doraneuron.model.brainnode.AiModelCacheEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * Service for managing ONNX model lifecycle on the Brain Node.
 * <p>
 * Handles model deployment, versioning, activation, A/B testing, and caching.
 * Integrates with {@link BrainNodeAiService} for inference operations and
 * {@link BrainNodeStorageService} for persistent metadata storage.
 */
@Service
@ConditionalOnBean({BrainNodeConfig.class, BrainNodeStorageService.class})
public class ModelManagementService {

    private static final Logger log = LoggerFactory.getLogger(ModelManagementService.class);

    private static final String MODELS_DIR = "models/brain-node/";
    private static final long MAX_MODEL_SIZE_BYTES = 500 * 1024 * 1024; // 500MB default

    private final BrainNodeConfig brainNodeConfig;
    private final BrainNodeStorageService storageService;
    private final BrainNodeAiService aiService;

    /** A/B test configuration: traffic split between variants */
    private final Map<String, Double> abTestTrafficSplit = new ConcurrentHashMap<>();

    /** A/B test metrics per variant */
    private final Map<String, AtomicLong> abTestInferenceCount = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> abTestErrorCount = new ConcurrentHashMap<>();
    private final Map<String, Long> abTestTotalLatency = new ConcurrentHashMap<>();

    /** Whether A/B testing is currently active */
    private volatile boolean abTestActive = false;

    /** Current active model ID */
    private volatile String activeModelId;

    @Autowired
    public ModelManagementService(BrainNodeConfig brainNodeConfig,
                                  BrainNodeStorageService storageService,
                                  @Autowired(required = false) BrainNodeAiService aiService) {
        this.brainNodeConfig = brainNodeConfig;
        this.storageService = storageService;
        this.aiService = aiService;
    }

    /**
     * Deploy a new ONNX model from an uploaded file.
     *
     * @param file the ONNX model file
     * @param modelId optional model ID (generated if null)
     * @param version model version number
     * @param metadata optional metadata JSON
     * @return the deployed model ID
     * @throws IOException if file operations fail
     * @throws IllegalArgumentException if validation fails
     */
    public String deployModel(MultipartFile file, String modelId, int version, String metadata) throws IOException {
        // Validate the file
        validateModelFile(file);

        // Generate model ID if not provided
        if (modelId == null || modelId.trim().isEmpty()) {
            modelId = generateModelId(file.getOriginalFilename());
        }

        // Ensure models directory exists
        ensureModelsDirectory();

        // Save the model file
        String fileName = modelId + ".onnx";
        Path targetPath = Paths.get(MODELS_DIR, fileName);
        Files.createDirectories(targetPath.getParent());
        Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
        String modelPath = targetPath.toString();

        // Cache metadata in H2 database
        storageService.cacheAiModel(modelId, modelPath, version, metadata);

        log.info("Deployed model: {} (version: {}, path: {})", modelId, version, modelPath);
        return modelId;
    }

    /**
     * Deploy a new ONNX model from a URL.
     *
     * @param url the URL to download the model from
     * @param modelId optional model ID
     * @param version model version number
     * @param metadata optional metadata JSON
     * @return the deployed model ID
     * @throws IOException if download fails
     */
    public String deployModelFromUrl(String url, String modelId, int version, String metadata) throws IOException {
        if (modelId == null || modelId.trim().isEmpty()) {
            // Extract filename from URL
            String fileName = url.substring(url.lastIndexOf('/') + 1);
            modelId = fileName.replace(".onnx", "");
        }

        ensureModelsDirectory();

        // Download the model
        java.net.URL modelUrl = new java.net.URL(url);
        Path targetPath = Paths.get(MODELS_DIR, modelId + ".onnx");
        Files.createDirectories(targetPath.getParent());

        try (var inputStream = modelUrl.openStream()) {
            Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
        }

        String modelPath = targetPath.toString();
        storageService.cacheAiModel(modelId, modelPath, version, metadata);

        log.info("Deployed model from URL: {} (modelId: {}, version: {})", url, modelId, version);
        return modelId;
    }

    /**
     * List all cached models.
     *
     * @return list of model information maps
     */
    public List<Map<String, Object>> listModels() {
        List<AiModelCacheEntity> models = storageService.getAllCachedModels();
        return models.stream().map(entity -> {
            Map<String, Object> info = new HashMap<>();
            info.put("modelId", entity.getModelId());
            info.put("modelPath", entity.getModelPath());
            info.put("version", entity.getVersion());
            info.put("cachedAt", entity.getCachedAt());
            info.put("active", entity.getModelId().equals(activeModelId));
            info.put("fileExists", Files.exists(Paths.get(entity.getModelPath())));

            // Parse metadata JSON if present
            if (entity.getMetadataJson() != null && !entity.getMetadataJson().isEmpty()) {
                info.put("metadata", entity.getMetadataJson());
            }

            return info;
        }).collect(Collectors.toList());
    }

    /**
     * Get details for a specific model.
     *
     * @param modelId the model ID
     * @return model information map or null if not found
     */
    public Map<String, Object> getModelDetails(String modelId) {
        Optional<AiModelCacheEntity> optional = storageService.getCachedAiModel(modelId);
        if (optional.isEmpty()) {
            return null;
        }

        AiModelCacheEntity entity = optional.get();
        Map<String, Object> info = new HashMap<>();
        info.put("modelId", entity.getModelId());
        info.put("modelPath", entity.getModelPath());
        info.put("version", entity.getVersion());
        info.put("cachedAt", entity.getCachedAt());
        info.put("active", entity.getModelId().equals(activeModelId));
        info.put("fileExists", Files.exists(Paths.get(entity.getModelPath())));

        if (entity.getMetadataJson() != null && !entity.getMetadataJson().isEmpty()) {
            info.put("metadata", entity.getMetadataJson());
        }

        // Add file size if file exists
        if (Files.exists(Paths.get(entity.getModelPath()))) {
            try {
                long size = Files.size(Paths.get(entity.getModelPath()));
                info.put("fileSizeBytes", size);
                info.put("fileSizeMB", String.format("%.2f", size / (1024.0 * 1024.0)));
            } catch (IOException e) {
                log.warn("Failed to get file size for {}: {}", modelId, e.getMessage());
            }
        }

        return info;
    }

    /**
     * Activate a model by swapping it into the inference engine.
     *
     * @param modelId the model ID to activate
     * @return true if activation was successful
     */
    public boolean activateModel(String modelId) {
        Optional<AiModelCacheEntity> optional = storageService.getCachedAiModel(modelId);
        if (optional.isEmpty()) {
            log.warn("Model not found: {}", modelId);
            return false;
        }

        AiModelCacheEntity entity = optional.get();
        String modelPath = entity.getModelPath();

        if (!Files.exists(Paths.get(modelPath))) {
            log.warn("Model file not found: {}", modelPath);
            return false;
        }

        if (aiService != null) {
            try {
                aiService.swapModel(modelPath, entity.getVersion());
                activeModelId = modelId;
                log.info("Activated model: {} (version: {})", modelId, entity.getVersion());
                return true;
            } catch (Exception e) {
                log.error("Failed to activate model {}: {}", modelId, e.getMessage(), e);
                return false;
            }
        } else {
            log.warn("AI service not available for model activation");
            return false;
        }
    }

    /**
     * Delete a cached model.
     *
     * @param modelId the model ID to delete
     * @return true if deletion was successful
     */
    public boolean deleteModel(String modelId) {
        // Cannot delete the active model
        if (activeModelId != null && activeModelId.equals(modelId)) {
            log.warn("Cannot delete active model: {}", modelId);
            return false;
        }

        Optional<AiModelCacheEntity> optional = storageService.getCachedAiModel(modelId);
        if (optional.isEmpty()) {
            log.warn("Model not found: {}", modelId);
            return false;
        }

        AiModelCacheEntity entity = optional.get();

        // Delete the file
        try {
            Path modelPath = Paths.get(entity.getModelPath());
            if (Files.exists(modelPath)) {
                Files.delete(modelPath);
            }
        } catch (IOException e) {
            log.warn("Failed to delete model file: {}", e.getMessage());
        }

        // Remove from cache
        storageService.removeCachedAiModel(modelId);
        log.info("Deleted model: {}", modelId);
        return true;
    }

    /**
     * Create an A/B test configuration.
     *
     * @param modelA ID of model A
     * @param modelB ID of model B
     * @param trafficSplitA traffic percentage for model A (0-100)
     * @return true if A/B test was created successfully
     */
    public boolean createAbTest(String modelA, String modelB, double trafficSplitA) {
        // Validate models exist
        if (storageService.getCachedAiModel(modelA).isEmpty()) {
            log.warn("Model A not found: {}", modelA);
            return false;
        }
        if (storageService.getCachedAiModel(modelB).isEmpty()) {
            log.warn("Model B not found: {}", modelB);
            return false;
        }

        // Validate traffic split
        if (trafficSplitA < 0 || trafficSplitA > 100) {
            log.warn("Invalid traffic split: {}", trafficSplitA);
            return false;
        }

        // Register variants with AI service
        if (aiService != null) {
            AiModelCacheEntity entityA = storageService.getCachedAiModel(modelA).get();
            AiModelCacheEntity entityB = storageService.getCachedAiModel(modelB).get();
            aiService.registerModelVariant("A", entityA.getModelPath());
            aiService.registerModelVariant("B", entityB.getModelPath());
        }

        // Set up traffic split
        abTestTrafficSplit.clear();
        abTestTrafficSplit.put("A", trafficSplitA);
        abTestTrafficSplit.put("B", 100.0 - trafficSplitA);

        // Initialize metrics
        abTestInferenceCount.clear();
        abTestErrorCount.clear();
        abTestTotalLatency.clear();
        abTestInferenceCount.put("A", new AtomicLong(0));
        abTestInferenceCount.put("B", new AtomicLong(0));
        abTestErrorCount.put("A", new AtomicLong(0));
        abTestErrorCount.put("B", new AtomicLong(0));
        abTestTotalLatency.put("A", 0L);
        abTestTotalLatency.put("B", 0L);

        abTestActive = true;
        log.info("Created A/B test: A={} ({:.1f}%), B={} ({:.1f}%)",
                modelA, trafficSplitA, modelB, 100.0 - trafficSplitA);
        return true;
    }

    /**
     * Get A/B test status and metrics.
     *
     * @return A/B test information map or null if no active test
     */
    public Map<String, Object> getAbTestStatus() {
        if (!abTestActive) {
            return null;
        }

        Map<String, Object> status = new HashMap<>();
        status.put("active", abTestActive);
        status.put("trafficSplit", Map.copyOf(abTestTrafficSplit));

        // Per-variant metrics
        Map<String, Object> variantMetrics = new HashMap<>();
        for (String variant : abTestTrafficSplit.keySet()) {
            long count = abTestInferenceCount.getOrDefault(variant, new AtomicLong(0)).get();
            long errors = abTestErrorCount.getOrDefault(variant, new AtomicLong(0)).get();
            long totalLatency = abTestTotalLatency.getOrDefault(variant, 0L);
            double avgLatency = count > 0 ? (double) totalLatency / count : 0.0;
            double errorRate = count > 0 ? (double) errors / count : 0.0;

            Map<String, Object> metrics = new HashMap<>();
            metrics.put("inferenceCount", count);
            metrics.put("errorCount", errors);
            metrics.put("errorRate", errorRate);
            metrics.put("totalLatencyMs", totalLatency);
            metrics.put("avgLatencyMs", avgLatency);
            variantMetrics.put(variant, metrics);
        }
        status.put("variantMetrics", variantMetrics);

        return status;
    }

    /**
     * Update A/B test traffic split.
     *
     * @param trafficSplitA new traffic percentage for model A (0-100)
     * @return true if update was successful
     */
    public boolean updateAbTestTrafficSplit(double trafficSplitA) {
        if (!abTestActive) {
            log.warn("No active A/B test");
            return false;
        }

        if (trafficSplitA < 0 || trafficSplitA > 100) {
            log.warn("Invalid traffic split: {}", trafficSplitA);
            return false;
        }

        abTestTrafficSplit.put("A", trafficSplitA);
        abTestTrafficSplit.put("B", 100.0 - trafficSplitA);
        log.info("Updated A/B test traffic split: A={:.1f}%, B={:.1f}%",
                trafficSplitA, 100.0 - trafficSplitA);
        return true;
    }

    /**
     * Stop the current A/B test.
     */
    public void stopAbTest() {
        abTestActive = false;
        abTestTrafficSplit.clear();
        abTestInferenceCount.clear();
        abTestErrorCount.clear();
        abTestTotalLatency.clear();
        log.info("Stopped A/B test");
    }

    /**
     * Record an inference result for A/B test metrics.
     *
     * @param variant the variant name (A or B)
     * @param latencyMs inference latency in milliseconds
     * @param error whether the inference resulted in an error
     */
    public void recordAbTestInference(String variant, long latencyMs, boolean error) {
        if (!abTestActive) {
            return;
        }

        AtomicLong countAtom = abTestInferenceCount.get(variant);
        if (countAtom != null) {
            countAtom.incrementAndGet();
        }

        if (error) {
            AtomicLong errorAtom = abTestErrorCount.get(variant);
            if (errorAtom != null) {
                errorAtom.incrementAndGet();
            }
        }

        abTestTotalLatency.merge(variant, latencyMs, Long::sum);
    }

    /**
     * Determine which variant to use for the next inference based on traffic split.
     *
     * @return the variant name (A or B)
     */
    public String selectAbTestVariant() {
        if (!abTestActive || abTestTrafficSplit.isEmpty()) {
            return "A";
        }

        double random = Math.random() * 100.0;
        double splitA = abTestTrafficSplit.getOrDefault("A", 50.0);
        return random < splitA ? "A" : "B";
    }

    /**
     * Validate an ONNX model file.
     *
     * @param file the file to validate
     * @throws IllegalArgumentException if validation fails
     */
    public void validateModelFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Model file is empty");
        }

        String filename = file.getOriginalFilename();
        if (filename == null || !filename.toLowerCase().endsWith(".onnx")) {
            throw new IllegalArgumentException("Model file must have .onnx extension");
        }

        if (file.getSize() > MAX_MODEL_SIZE_BYTES) {
            throw new IllegalArgumentException(
                    "Model file too large: " + (file.getSize() / (1024 * 1024)) + "MB (max: " +
                    (MAX_MODEL_SIZE_BYTES / (1024 * 1024)) + "MB)");
        }
    }

    /**
     * Validate a model file at a given path.
     *
     * @param modelPath the path to the model file
     * @return true if the file is valid
     */
    public boolean validateModelFile(String modelPath) {
        if (modelPath == null || modelPath.trim().isEmpty()) {
            return false;
        }

        Path path = Paths.get(modelPath);
        if (!Files.exists(path)) {
            log.warn("Model file not found: {}", modelPath);
            return false;
        }

        if (!modelPath.toLowerCase().endsWith(".onnx")) {
            log.warn("Model file does not have .onnx extension: {}", modelPath);
            return false;
        }

        try {
            long size = Files.size(path);
            if (size > MAX_MODEL_SIZE_BYTES) {
                log.warn("Model file too large: {}MB", size / (1024 * 1024));
                return false;
            }
            return true;
        } catch (IOException e) {
            log.warn("Failed to validate model file: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Get the current active model ID.
     *
     * @return the active model ID or null if none
     */
    public String getActiveModelId() {
        return activeModelId;
    }

    /**
     * Check if A/B testing is currently active.
     *
     * @return true if A/B testing is active
     */
    public boolean isAbTestActive() {
        return abTestActive;
    }

    // ── Private Helpers ──────────────────────────────────────────────────────

    private void ensureModelsDirectory() {
        try {
            Path dir = Paths.get(MODELS_DIR);
            if (!Files.exists(dir)) {
                Files.createDirectories(dir);
                log.info("Created models directory: {}", MODELS_DIR);
            }
        } catch (IOException e) {
            log.error("Failed to create models directory: {}", e.getMessage(), e);
        }
    }

    private String generateModelId(String filename) {
        if (filename == null) {
            return "model-" + System.currentTimeMillis();
        }
        String base = filename.replace(".onnx", "");
        return base + "-" + System.currentTimeMillis();
    }
}
