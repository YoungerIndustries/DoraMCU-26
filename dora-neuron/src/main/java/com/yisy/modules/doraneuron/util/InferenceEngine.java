package com.yisy.modules.doraneuron.util;

/**
 * Common interface for ONNX inference engines.
 *
 * Implemented by both the standard {@link OnnxInferenceEngine} and
 * the edge-optimized {@link com.yisy.modules.doraneuron.util.edge.EdgeOnnxInferenceEngine}.
 *
 * This allows {@link com.yisy.modules.doraneuron.service.SatelliteAiService}
 * to work with either engine transparently.
 */
public interface InferenceEngine {

    /**
     * Load an ONNX model from the specified file path.
     */
    void loadModel(String modelPath);

    /**
     * Reload the model from the specified path.
     */
    void reloadModel(String modelPath);

    /**
     * Run inference on a single input feature vector.
     */
    float[] runInference(float[] features);

    /**
     * Run inference on a double[] input.
     */
    float[] runInference(double[] features);

    /**
     * Check if a model is currently loaded.
     */
    boolean isModelLoaded();

    /**
     * Return the path of the currently loaded model.
     */
    String getCurrentModelPath();

    /**
     * Close the session and release native resources.
     */
    void close();
}
