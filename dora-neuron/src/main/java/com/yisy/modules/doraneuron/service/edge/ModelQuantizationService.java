package com.yisy.modules.doraneuron.service.edge;

import com.yisy.modules.doraneuron.config.edge.NeuronEdgeConfig;
import com.yisy.modules.doraneuron.util.edge.SystemResourceDetector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for managing ONNX model quantization on edge devices.
 *
 * Quantization converts FP32 (32-bit float) models to INT8 (8-bit integer)
 * representation, reducing model size by ~75% and improving inference speed
 * on edge devices with limited CPU capabilities.
 *
 * The service:
 * - Detects if a quantized version of a model already exists
 * - Downloads or generates quantized models from the coordinator
 * - Caches quantized models locally
 * - Tracks quantization status and model integrity
 *
 * Note: Actual ONNX quantization requires the ONNX Runtime Python API.
 * This service manages the quantized model lifecycle and requests
 * quantized models from the coordinator when needed.
 */
@Service
public class ModelQuantizationService {

    private static final Logger log = LoggerFactory.getLogger(ModelQuantizationService.class);

    private final NeuronEdgeConfig edgeConfig;
    private final SystemResourceDetector detector;

    /** Cache of model paths to their quantized counterparts. */
    private final ConcurrentHashMap<String, String> quantizedModelCache = new ConcurrentHashMap<>();

    /** Cache of model hashes for integrity verification. */
    private final ConcurrentHashMap<String, String> modelHashCache = new ConcurrentHashMap<>();

    /** Total number of quantization requests made. */
    private volatile long totalQuantizationRequests = 0;

    /** Total number of successful quantizations. */
    private volatile long totalQuantizations = 0;

    /** Total number of quantization failures. */
    private volatile long totalFailures = 0;

    public ModelQuantizationService(NeuronEdgeConfig edgeConfig, SystemResourceDetector detector) {
        this.edgeConfig = edgeConfig;
        this.detector = detector;
    }

    @PostConstruct
    public void init() {
        // Ensure quantized model directory exists
        File quantizedDir = new File(edgeConfig.getQuantizedModelDir());
        if (!quantizedDir.exists()) {
            boolean created = quantizedDir.mkdirs();
            if (created) {
                log.info("Created quantized model directory: {}", edgeConfig.getQuantizedModelDir());
            }
        }

        // Log quantization status
        boolean quantizationActive = edgeConfig.isQuantizationEnabled() && detector.shouldUseQuantizedModel();
        log.info("Model quantization service initialized: enabled={}, shouldUse={}, profile={}",
                edgeConfig.isQuantizationEnabled(), detector.shouldUseQuantizedModel(),
                detector.getProfile());
    }

    /**
     * Get the quantized version of a model, creating it if necessary.
     *
     * @param originalModelPath path to the original FP32 ONNX model
     * @return path to the quantized INT8 model, or the original path if quantization is not available
     */
    public String getQuantizedModel(String originalModelPath) {
        if (!edgeConfig.isQuantizationEnabled()) {
            log.debug("Quantization disabled, using original model: {}", originalModelPath);
            return originalModelPath;
        }

        if (!detector.shouldUseQuantizedModel()) {
            log.debug("System profile {} does not require quantization", detector.getProfile());
            return originalModelPath;
        }

        // Check cache first
        String cached = quantizedModelCache.get(originalModelPath);
        if (cached != null && new File(cached).exists()) {
            log.debug("Using cached quantized model: {}", cached);
            return cached;
        }

        // Try to find or create quantized model
        String quantizedPath = findOrCreateQuantizedModel(originalModelPath);
        if (quantizedPath != null) {
            quantizedModelCache.put(originalModelPath, quantizedPath);
            totalQuantizations++;
            return quantizedPath;
        }

        // Fallback to original model
        log.warn("Quantized model not available, using original: {}", originalModelPath);
        totalFailures++;
        return originalModelPath;
    }

    /**
     * Find or create a quantized version of the model.
     */
    private String findOrCreateQuantizedModel(String originalModelPath) {
        totalQuantizationRequests++;

        File originalFile = new File(originalModelPath);
        if (!originalFile.exists()) {
            log.warn("Original model not found: {}", originalModelPath);
            return null;
        }

        // Generate quantized model filename
        String originalName = originalFile.getName();
        String baseName = originalName.replace(".onnx", "");
        String quantizedName = baseName + "_int8.onnx";
        String quantizedPath = edgeConfig.getQuantizedModelDir() + File.separator + quantizedName;

        // Check if quantized model already exists
        File quantizedFile = new File(quantizedPath);
        if (quantizedFile.exists()) {
            // Verify integrity
            String originalHash = computeHash(originalFile);
            String cachedHash = modelHashCache.get(quantizedPath);

            if (cachedHash != null && cachedHash.equals(originalHash)) {
                log.info("Using existing quantized model: {}", quantizedPath);
                return quantizedPath;
            } else {
                // Original model changed, need to re-quantize
                log.info("Original model changed, re-quantization needed");
            }
        }

        // Request quantized model from coordinator
        // In a real implementation, this would call the coordinator's quantization endpoint
        // For now, we log the request and return null to use the original model
        log.info("Quantized model not found at {}. In production, this would request quantization from the coordinator.",
                quantizedPath);

        // TODO: Implement actual quantization request to coordinator
        // This would involve:
        // 1. POST /api/v1/dl/quantize with the original model
        // 2. Download the quantized model response
        // 3. Save to quantizedPath
        // 4. Update hash cache

        return null;
    }

    /**
     * Compute SHA-256 hash of a file for integrity verification.
     */
    private String computeHash(File file) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(Files.readAllBytes(file.toPath()));
            return bytesToHex(hash);
        } catch (IOException | NoSuchAlgorithmException e) {
            log.error("Failed to compute hash for {}: {}", file.getAbsolutePath(), e.getMessage());
            return null;
        }
    }

    /**
     * Convert byte array to hex string.
     */
    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    /**
     * Clear the quantized model cache (useful for model updates).
     */
    public void clearCache() {
        quantizedModelCache.clear();
        modelHashCache.clear();
        log.info("Quantized model cache cleared");
    }

    /**
     * Get quantization statistics.
     */
    public Map<String, Object> getStats() {
        return Map.of(
                "enabled", edgeConfig.isQuantizationEnabled(),
                "shouldUse", detector.shouldUseQuantizedModel(),
                "totalRequests", totalQuantizationRequests,
                "totalSuccess", totalQuantizations,
                "totalFailures", totalFailures,
                "cachedModels", quantizedModelCache.size(),
                "quantizedModelDir", edgeConfig.getQuantizedModelDir()
        );
    }

    /**
     * Check if quantization is active for this system.
     */
    public boolean isQuantizationActive() {
        return edgeConfig.isQuantizationEnabled() && detector.shouldUseQuantizedModel();
    }
}
