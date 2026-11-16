package com.yisy.modules.doramcu.ai.framework;

import java.util.List;
import java.util.Map;

/**
 * Unified AI learning service that provides a single interface for neural network
 * training, inference, and model management.
 */
public interface AiLearningService {

    /**
     * Trains the AI model with the given training data.
     */
    Map<String, Double> trainModel(List<Map<String, Object>> trainingData);

    /**
     * Makes a prediction based on the given features.
     */
    double[] predict(double[] features);

    /**
     * Makes a prediction and returns the best action.
     */
    int predictBestAction(double[] features);

    /**
     * Returns the current model metrics.
     */
    Map<String, Double> getModelMetrics();

    /**
     * Returns the model status.
     */
    ModelStatus getModelStatus();

    /**
     * Saves the current model to disk.
     */
    void saveModel(String path);

    /**
     * Loads a model from disk.
     */
    void loadModel(String path);

    /**
     * Clears the current model and training data.
     */
    void resetModel();

    /**
     * Returns the number of training iterations completed.
     */
    long getTrainingIterations();

    /**
     * Model status.
     */
    enum ModelStatus {
        EMPTY,
        TRAINING,
        READY,
        ERROR
    }
}
