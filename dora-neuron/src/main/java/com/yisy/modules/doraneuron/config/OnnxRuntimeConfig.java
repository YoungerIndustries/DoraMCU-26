package com.yisy.modules.doraneuron.config;

import com.yisy.modules.doraneuron.util.OnnxInferenceEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import jakarta.annotation.PreDestroy;

/**
 * Configuration for the ONNX Runtime inference engine.
 *
 * Creates the shared {@link OnnxInferenceEngine} bean that all Neuron
 * services use for local AI inference. The engine is loaded only when
 * {@code neuron.onnx-model-path} is configured.
 *
 * This configuration is disabled when edge optimization is active
 * (Phase 13+), in which case EdgeOnnxRuntimeConfig provides the
 * edge-optimized inference engine instead.
 */
@Configuration
@ConditionalOnProperty(prefix = "neuron", name = "onnx-model-path")
@ConditionalOnProperty(prefix = "neuron.edge", name = "auto-optimize", havingValue = "false", matchIfMissing = false)
public class OnnxRuntimeConfig {

    private static final Logger log = LoggerFactory.getLogger(OnnxRuntimeConfig.class);

    private final NeuronConfig config;
    private OnnxInferenceEngine engine;

    public OnnxRuntimeConfig(NeuronConfig config) {
        this.config = config;
    }

    /**
     * Create the ONNX inference engine and load the configured model.
     */
    @Bean
    public OnnxInferenceEngine onnxInferenceEngine() {
        engine = new OnnxInferenceEngine();
        if (config.getOnnxModelPath() != null && !config.getOnnxModelPath().isEmpty()) {
            log.info("Initializing ONNX inference engine with model: {}", config.getOnnxModelPath());
            engine.loadModel(config.getOnnxModelPath());
        } else {
            log.warn("neuron.onnx-model-path is not set; inference engine will be unavailable until a model is loaded");
        }
        return engine;
    }

    @PreDestroy
    public void close() {
        if (engine != null) {
            engine.close();
        }
    }
}
