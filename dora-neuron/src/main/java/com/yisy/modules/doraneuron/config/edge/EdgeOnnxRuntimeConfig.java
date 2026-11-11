package com.yisy.modules.doraneuron.config.edge;

import com.yisy.modules.doraneuron.config.NeuronConfig;
import com.yisy.modules.doraneuron.service.edge.ModelQuantizationService;
import com.yisy.modules.doraneuron.util.edge.EdgeOnnxInferenceEngine;
import com.yisy.modules.doraneuron.util.edge.SystemResourceDetector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PreDestroy;

/**
 * Configuration for edge-optimized ONNX Runtime inference.
 *
 * This configuration is activated when running on an edge device
 * (detected by SystemResourceDetector) or when the edge profile
 * is explicitly enabled via {@code neuron.edge.auto-optimize=true}.
 *
 * It creates the EdgeOnnxInferenceEngine bean which replaces the
 * standard OnnxInferenceEngine with edge-optimized settings:
 * - Reduced thread count
 * - Thermal throttling awareness
 * - Inference rate limiting
 * - Model quantization support
 */
@Configuration
@ConditionalOnProperty(prefix = "neuron.edge", name = "auto-optimize", havingValue = "true", matchIfMissing = true)
public class EdgeOnnxRuntimeConfig {

    private static final Logger log = LoggerFactory.getLogger(EdgeOnnxRuntimeConfig.class);

    private final NeuronConfig config;
    private final NeuronEdgeConfig edgeConfig;
    private final SystemResourceDetector detector;
    private final ModelQuantizationService quantizationService;

    private EdgeOnnxInferenceEngine edgeEngine;

    public EdgeOnnxRuntimeConfig(NeuronConfig config,
                                  NeuronEdgeConfig edgeConfig,
                                  SystemResourceDetector detector,
                                  ModelQuantizationService quantizationService) {
        this.config = config;
        this.edgeConfig = edgeConfig;
        this.detector = detector;
        this.quantizationService = quantizationService;
    }

    /**
     * Create the edge-optimized ONNX inference engine.
     * This bean is created only when an ONNX model path is configured.
     */
    @Bean
    @ConditionalOnProperty(prefix = "neuron", name = "onnx-model-path")
    public EdgeOnnxInferenceEngine edgeOnnxInferenceEngine() {
        edgeEngine = new EdgeOnnxInferenceEngine(detector);

        // Load the model, using quantized version if available
        String modelPath = config.getOnnxModelPath();
        if (modelPath != null && !modelPath.isEmpty()) {
            modelPath = quantizationService.getQuantizedModel(modelPath);
            log.info("Initializing edge ONNX engine with model: {}", modelPath);
            edgeEngine.loadModel(modelPath);
        } else {
            log.warn("neuron.onnx-model-path is not set; edge inference engine will be unavailable until a model is loaded");
        }

        return edgeEngine;
    }

    @PreDestroy
    public void close() {
        if (edgeEngine != null) {
            edgeEngine.close();
        }
    }
}
