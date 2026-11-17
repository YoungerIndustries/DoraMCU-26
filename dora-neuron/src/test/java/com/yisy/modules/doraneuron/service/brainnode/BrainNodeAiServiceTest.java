package com.yisy.modules.doraneuron.service.brainnode;

import com.yisy.modules.doraneuron.config.BrainNodeConfig;
import com.yisy.modules.doraneuron.config.NeuronConfig;
import com.yisy.modules.doraneuron.config.NeuronPrometheusMetrics;
import com.yisy.modules.doraneuron.model.NeuronDecision;
import com.yisy.modules.doraneuron.util.InferenceEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BrainNodeAiServiceTest {

    private BrainNodeAiService service;

    @Mock
    private InferenceEngine engine;

    @Mock
    private BrainNodeConfig brainNodeConfig;

    @Mock
    private NeuronConfig neuronConfig;

    @Mock
    private NeuronPrometheusMetrics prometheusMetrics;

    @BeforeEach
    void setUp() {
        service = new BrainNodeAiService(engine, brainNodeConfig, neuronConfig, prometheusMetrics);
        when(brainNodeConfig.getInferenceThreads()).thenReturn(4);
        when(brainNodeConfig.getConfidenceThreshold()).thenReturn(0.85);
        when(brainNodeConfig.isGpuAccelerationEnabled()).thenReturn(false);
        when(brainNodeConfig.getAiModelPath()).thenReturn("models/brain-node/");
        when(neuronConfig.getModelVersion()).thenReturn(1);
    }

    @Test
    @DisplayName("Predict should return empty decision when model not loaded")
    void predictModelNotLoaded() {
        when(engine.isModelLoaded()).thenReturn(false);

        NeuronDecision decision = service.predict(new double[]{0.1, 0.2, 0.3});

        assertNotNull(decision);
        verify(prometheusMetrics).recordInferenceError();
    }

    @Test
    @DisplayName("Predict should return decision with valid output")
    void predictWithValidOutput() {
        when(engine.isModelLoaded()).thenReturn(true);
        when(engine.runInference(any(double[].class))).thenReturn(new float[]{0.1f, 0.7f, 0.2f});

        NeuronDecision decision = service.predict(new double[]{0.1, 0.2, 0.3});

        assertNotNull(decision);
        verify(prometheusMetrics).recordInference(anyLong());
    }

    @Test
    @DisplayName("Predict with float array should convert and call predict")
    void predictFloatArray() {
        when(engine.isModelLoaded()).thenReturn(true);
        when(engine.runInference(any(double[].class))).thenReturn(new float[]{0.1f, 0.7f, 0.2f});

        NeuronDecision decision = service.predict(new float[]{0.1f, 0.2f, 0.3f});

        assertNotNull(decision);
    }

    @Test
    @DisplayName("Predict async should return future")
    void predictAsync() {
        when(engine.isModelLoaded()).thenReturn(true);
        when(engine.runInference(any(double[].class))).thenReturn(new float[]{0.1f, 0.7f, 0.2f});

        Future<NeuronDecision> future = service.predictAsync(new double[]{0.1, 0.2, 0.3});

        assertNotNull(future);
    }

    @Test
    @DisplayName("Predict batch should return array of decisions")
    void predictBatch() {
        when(engine.isModelLoaded()).thenReturn(true);
        when(engine.runInference(any(double[].class))).thenReturn(new float[]{0.1f, 0.7f, 0.2f});

        double[][] features = {
                {0.1, 0.2, 0.3},
                {0.4, 0.5, 0.6}
        };
        NeuronDecision[] decisions = service.predictBatch(features);

        assertEquals(2, decisions.length);
    }

    @Test
    @DisplayName("Swap model should update version")
    void swapModel() {
        when(engine.reloadModel(anyString())).thenReturn(true);

        service.swapModel("models/new-model.onnx", 2);

        assertEquals(2, service.getCurrentModelVersion());
        verify(prometheusMetrics).setModelVersion(2L);
    }

    @Test
    @DisplayName("Register model variant should add to variants map")
    void registerModelVariant() {
        service.registerModelVariant("A", "models/model-a.onnx");
        service.registerModelVariant("B", "models/model-b.onnx");

        Map<String, String> variants = service.getModelVariants();
        assertEquals(2, variants.size());
        assertEquals("models/model-a.onnx", variants.get("A"));
        assertEquals("models/model-b.onnx", variants.get("B"));
    }

    @Test
    @DisplayName("Get model variant should return path")
    void getModelVariant() {
        service.registerModelVariant("A", "models/model-a.onnx");

        String path = service.getModelVariant("A");
        assertEquals("models/model-a.onnx", path);
    }

    @Test
    @DisplayName("Get model variant should return null for unknown")
    void getModelVariantNotFound() {
        String path = service.getModelVariant("unknown");
        assertNull(path);
    }

    @Test
    @DisplayName("Is ready should return engine model loaded status")
    void isReady() {
        when(engine.isModelLoaded()).thenReturn(true);
        assertTrue(service.isReady());

        when(engine.isModelLoaded()).thenReturn(false);
        assertFalse(service.isReady());
    }

    @Test
    @DisplayName("Get inference stats should return statistics map")
    void getInferenceStats() {
        when(engine.isModelLoaded()).thenReturn(true);

        Map<String, Object> stats = service.getInferenceStats();

        assertNotNull(stats);
        assertEquals(4, stats.get("inferenceThreads"));
        assertEquals(0.85, stats.get("confidenceThreshold"));
        assertFalse(stats.get("gpuAccelerationEnabled"));
        assertTrue(stats.containsKey("inferenceCount"));
        assertTrue(stats.containsKey("errorCount"));
    }
}
