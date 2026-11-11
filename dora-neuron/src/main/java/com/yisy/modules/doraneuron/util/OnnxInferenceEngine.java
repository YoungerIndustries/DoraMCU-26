package com.yisy.modules.doraneuron.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtException;
import ai.onnxruntime.OrtSession;

import java.io.File;
import java.nio.FloatBuffer;
import java.util.Map;

/**
 * Lightweight ONNX model inference engine.
 *
 * Wraps the ONNX Runtime Java API to load an ONNX model and run
 * inference on input feature vectors. Designed for single-input,
 * single-output models like the DoraMCU strategy network.
 *
 * Thread-safe: the OrtSession is immutable after creation and can be
 * shared across threads for concurrent inference.
 */
public class OnnxInferenceEngine implements InferenceEngine {

    private static final Logger log = LoggerFactory.getLogger(OnnxInferenceEngine.class);

    private final OrtEnvironment ortEnv;
    private volatile OrtSession session;
    private volatile String currentModelPath;

    public OnnxInferenceEngine() {
        this.ortEnv = OrtEnvironment.getEnvironment();
        this.session = null;
    }

    /**
     * Load an ONNX model from the specified file path.
     * Replaces any previously loaded model.
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
            session = ortEnv.createSession(modelFile.getAbsolutePath(), new OrtSession.SessionOptions());
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
     * Run inference on a single input feature vector.
     *
     * @param features input feature vector (e.g., 64-dimensional game state)
     * @return output probabilities (e.g., 17 action probabilities)
     * @throws IllegalStateException if no model is loaded
     */
    public float[] runInference(float[] features) {
        if (session == null) {
            throw new IllegalStateException("No ONNX model loaded for inference");
        }

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
            throw new RuntimeException("ONNX inference failed", e);
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
        log.info("ONNX inference engine closed");
    }
}
