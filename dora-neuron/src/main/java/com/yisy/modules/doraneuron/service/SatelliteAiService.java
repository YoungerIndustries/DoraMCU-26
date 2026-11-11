package com.yisy.modules.doraneuron.service;

import com.yisy.modules.doraneuron.config.NeuronConfig;
import com.yisy.modules.doraneuron.config.NeuronPrometheusMetrics;
import com.yisy.modules.doraneuron.model.NeuronDecision;
import com.yisy.modules.doraneuron.util.InferenceEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;

/**
 * Satellite AI service that runs local ONNX-based inference on game states.
 *
 * Each Neuron uses this service to make AI decisions independently,
 * using the shared ONNX model distributed by the central coordinator.
 * The service supports hot-swapping models without downtime.
 */
@Service
@ConditionalOnBean(InferenceEngine.class)
public class SatelliteAiService {

    private static final Logger log = LoggerFactory.getLogger(SatelliteAiService.class);

    private final InferenceEngine engine;
    private final NeuronConfig config;
    private final NeuronPrometheusMetrics prometheusMetrics;
    private volatile long currentModelVersion;

    public SatelliteAiService(InferenceEngine engine, NeuronConfig config,
                              NeuronPrometheusMetrics prometheusMetrics) {
        this.engine = engine;
        this.config = config;
        this.prometheusMetrics = prometheusMetrics;
        this.currentModelVersion = 0;
    }

    /**
     * Run inference on a game state feature vector using the local ONNX model.
     *
     * @param features the game state feature vector (typically 64 dimensions)
     * @return a decision containing selected action, confidence, and full probability distribution
     */
    public NeuronDecision predict(double[] features) {
        if (!engine.isModelLoaded()) {
            log.warn("No ONNX model loaded; returning empty decision");
            prometheusMetrics.recordInferenceError();
            return new NeuronDecision();
        }

        long start = System.currentTimeMillis();
        try {
            float[] output = engine.runInference(features);
            if (output == null) {
                log.warn("Inference returned null output");
                prometheusMetrics.recordInferenceError();
                return new NeuronDecision();
            }

            NeuronDecision decision = NeuronDecision.fromProbabilities(output, currentModelVersion);

            // Check confidence threshold
            if (decision.getConfidence() < config.getConfidenceThreshold()) {
                log.debug("Low confidence prediction: {} (threshold: {})",
                        decision.getConfidence(), config.getConfidenceThreshold());
            }

            long duration = System.currentTimeMillis() - start;
            prometheusMetrics.recordInference(duration);

            log.debug("AI prediction: action={}, confidence={}, modelVersion={}",
                    decision.getSelectedAction(), decision.getConfidence(), currentModelVersion);
            return decision;
        } catch (Exception e) {
            log.error("Inference failed: {}", e.getMessage());
            prometheusMetrics.recordInferenceError();
            return new NeuronDecision();
        }
    }

    /**
     * Run inference on a float[] feature vector.
     */
    public NeuronDecision predict(float[] features) {
        double[] doubleFeatures = new double[features.length];
        for (int i = 0; i < features.length; i++) {
            doubleFeatures[i] = features[i];
        }
        return predict(doubleFeatures);
    }

    /**
     * Swap to a new ONNX model without downtime.
     * The new model is loaded while the old session is closed.
     *
     * @param modelPath path to the new ONNX model file
     * @param version   the version number of the new model
     */
    public void swapModel(String modelPath, long version) {
        log.info("Swapping ONNX model: {} (version {})", modelPath, version);
        engine.reloadModel(modelPath);
        currentModelVersion = version;
        prometheusMetrics.setModelVersion(version);
        log.info("Model swap complete. Current version: {}", version);
    }

    /**
     * Return the current model version.
     */
    public long getCurrentModelVersion() {
        return currentModelVersion;
    }

    /**
     * Check if the AI service is ready (model loaded and engine available).
     */
    public boolean isReady() {
        return engine.isModelLoaded();
    }
}
