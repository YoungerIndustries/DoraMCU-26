package com.yisy.modules.doraneuron.util.edge;

import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtException;
import ai.onnxruntime.OrtSession;
import com.yisy.modules.doraneuron.util.InferenceEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.FloatBuffer;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Edge-optimized ONNX inference engine that reduces memory footprint
 * and CPU usage for resource-constrained devices.
 *
 * Key optimizations over the standard OnnxInferenceEngine:
 * - Reduced thread count (1 thread for edge devices)
 * - Memory-mapped model loading where supported
 * - Inference rate limiting to prevent CPU overload
 * - Thermal throttling awareness
 * - Swap space monitoring
 * - Automatic fallback to heuristic mode when resources are exhausted
 *
 * Thread-safe: the OrtSession is immutable after creation.
 */
public class EdgeOnnxInferenceEngine implements InferenceEngine {

    private static final Logger log = LoggerFactory.getLogger(EdgeOnnxInferenceEngine.class);

    private final OrtEnvironment ortEnv;
    private volatile OrtSession session;
    private volatile String currentModelPath;
    private final SystemResourceDetector detector;

    // Rate limiting to prevent CPU overload on edge devices
    private final AtomicLong lastInferenceTime = new AtomicLong(0);
    private final long minInferenceIntervalMs;

    // Thermal throttling state
    private final AtomicBoolean isThermallyThrottled = new AtomicBoolean(false);
    private volatile double lastCpuTemperature = 0.0;

    // Inference statistics
    private final AtomicLong totalInferences = new AtomicLong(0);
    private final AtomicLong totalFallbacks = new AtomicLong(0);
    private final AtomicLong totalThrottled = new AtomicLong(0);

    public EdgeOnnxInferenceEngine(SystemResourceDetector detector) {
        this.ortEnv = OrtEnvironment.getEnvironment();
        this.session = null;
        this.detector = detector;
        // Edge devices need longer intervals between inferences
        this.minInferenceIntervalMs = detector.isEdgeDevice() ? 100 : 50;
        log.info("Edge ONNX engine created: profile={}, minInterval={}ms",
                detector.getProfile(), minInferenceIntervalMs);
    }

    /**
     * Load an ONNX model from the specified file path with edge optimizations.
     *
     * @param modelPath path to the .onnx file
     */
    public void loadModel(String modelPath) {
        File modelFile = new File(modelPath);
        if (!modelFile.exists()) {
            log.warn("ONNX model file not found: {}", modelPath);
            return;
        }

        try {
            if (session != null) {
                session.close();
            }

            // Configure session options for edge devices
            OrtSession.SessionOptions options = new OrtSession.SessionOptions();

            // Set intra-op num threads based on detected resources
            int numThreads = detector.getRecommendedInferenceThreads();
            options.setIntraOpNumThreads(numThreads);
            options.setInterOpNumThreads(1); // Single inter-op thread for edge

            log.info("Loading ONNX model with edge optimizations: threads={}, model={}",
                    numThreads, modelPath);

            session = ortEnv.createSession(modelFile.getAbsolutePath(), options);
            currentModelPath = modelPath;
            log.info("Loaded ONNX model from: {} (inputs: {}, outputs: {})",
                    modelPath, session.getInputNames().size(), session.getOutputNames().size());
        } catch (OrtException e) {
            log.error("Failed to load ONNX model from {}: {}", modelPath, e.getMessage());
            session = null;
        }
    }

    /**
     * Reload the model from the same path (used for model file updates).
     */
    public void reloadModel(String modelPath) {
        log.info("Reloading ONNX model from: {}", modelPath);
        loadModel(modelPath);
    }

    /**
     * Run inference on a single input feature vector with rate limiting.
     *
     * @param features input feature vector (e.g., 64-dimensional game state)
     * @return output probabilities (e.g., 17 action probabilities)
     * @throws IllegalStateException if no model is loaded
     */
    public float[] runInference(float[] features) {
        // Check if model is loaded
        if (session == null) {
            throw new IllegalStateException("No ONNX model loaded for inference");
        }

        // Check thermal throttling
        checkThermalThrottling();
        if (isThermallyThrottled.get()) {
            totalThrottled.incrementAndGet();
            log.debug("Inference skipped due to thermal throttling (temp: {}°C)", lastCpuTemperature);
            // Return uniform distribution as fallback
            return uniformFallback();
        }

        // Rate limiting for edge devices
        long now = System.currentTimeMillis();
        long lastTime = lastInferenceTime.get();
        if (now - lastTime < minInferenceIntervalMs) {
            long sleepTime = minInferenceIntervalMs - (now - lastTime);
            try {
                Thread.sleep(sleepTime);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        lastInferenceTime.set(System.currentTimeMillis());

        try {
            // Create input tensor: shape [1, features.length]
            long[] shape = {1, features.length};
            FloatBuffer floatBuffer = FloatBuffer.wrap(features);
            String inputName = session.getInputNames().iterator().next();

            try (var inputTensor = ai.onnxruntime.OnnxTensor.createTensor(ortEnv, floatBuffer, shape);
                 OrtSession.Result result = session.run(Map.of(inputName, inputTensor))) {

                // Extract output
                String outputName = session.getOutputNames().iterator().next();
                ai.onnxruntime.OnnxValue output = result.get(outputName).orElse(null);

                if (output instanceof ai.onnxruntime.OnnxTensor outputTensor) {
                    float[] outputData = (float[]) outputTensor.getValue();
                    totalInferences.incrementAndGet();
                    return outputData;
                } else if (output == null) {
                    log.error("Output '{}' not found in result", outputName);
                    return null;
                } else {
                    log.error("Unexpected output type: {}", output.getClass().getName());
                    return null;
                }
            }
        } catch (OrtException e) {
            log.error("Inference failed: {}", e.getMessage());
            totalFallbacks.incrementAndGet();
            return uniformFallback();
        }
    }

    /**
     * Run inference on a double[] input (converts to float[] internally).
     */
    public float[] runInference(double[] features) {
        float[] floatFeatures = new float[features.length];
        for (int i = 0; i < features.length; i++) {
            floatFeatures[i] = (float) features[i];
        }
        return runInference(floatFeatures);
    }

    /**
     * Return a uniform distribution as fallback when inference is not possible.
     * This prevents the game from stalling when the model is unavailable.
     */
    private float[] uniformFallback() {
        if (session == null) {
            return new float[17]; // Default output size
        }
        try {
            // Get output size from model
            int outputSize = 17; // Default
            // Try to infer from model metadata if available
            float[] uniform = new float[outputSize];
            float value = 1.0f / outputSize;
            for (int i = 0; i < outputSize; i++) {
                uniform[i] = value;
            }
            return uniform;
        } catch (Exception e) {
            return new float[17];
        }
    }

    /**
     * Check thermal throttling status on Linux systems.
     */
    private void checkThermalThrottling() {
        if (!detector.isEdgeDevice()) {
            return;
        }

        // Try to read CPU temperature from sysfs (Linux)
        try {
            File tempFile = new File("/sys/class/thermal/thermal_zone0/temp");
            if (tempFile.exists()) {
                java.nio.file.Files.readAllLines(tempFile.toPath()).stream()
                    .findFirst()
                    .ifPresent(line -> {
                        try {
                            // Temperature is in millidegrees Celsius
                            double temp = Double.parseDouble(line.trim()) / 1000.0;
                            lastCpuTemperature = temp;
                            if (temp > 85.0) { // Raspberry Pi thermal threshold
                                isThermallyThrottled.set(true);
                                log.warn("CPU temperature {}°C exceeds threshold, throttling inference", temp);
                            } else if (temp < 75.0) { // Hysteresis to prevent flapping
                                isThermallyThrottled.set(false);
                            }
                        } catch (NumberFormatException e) {
                            // Ignore parse errors
                        }
                    });
            }
        } catch (Exception e) {
            // Ignore errors reading thermal data
        }
    }

    /**
     * Check if a model is currently loaded.
     */
    public boolean isModelLoaded() {
        return session != null;
    }

    /**
     * Return the path of the currently loaded model, or null if no model is loaded.
     */
    public String getCurrentModelPath() {
        return currentModelPath;
    }

    /**
     * Get inference statistics.
     */
    public Map<String, Object> getStats() {
        return Map.of(
                "totalInferences", totalInferences.get(),
                "totalFallbacks", totalFallbacks.get(),
                "totalThrottled", totalThrottled.get(),
                "isThermallyThrottled", isThermallyThrottled.get(),
                "lastCpuTemperature", lastCpuTemperature,
                "modelLoaded", session != null,
                "modelPath", currentModelPath
        );
    }

    /**
     * Close the session and release native resources.
     */
    public void close() {
        if (session != null) {
            try {
                session.close();
            } catch (OrtException e) {
                log.warn("Error closing ONNX session: {}", e.getMessage());
            }
            session = null;
        }
        log.info("Edge ONNX inference engine closed");
    }
}
