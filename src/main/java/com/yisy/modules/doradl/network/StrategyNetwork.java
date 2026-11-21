package com.yisy.modules.doradl.network;

import com.yisy.modules.doradl.config.DlConfig;
import com.yisy.modules.doradl.model.ActionReward;
import com.yisy.modules.doradl.model.GameStateFeatures;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Lightweight heuristic-based strategy network replacing the heavy DeepLearning4j implementation.
 * Uses a simple weighted scoring system instead of neural networks, providing the same interface
 * without requiring native libraries (~1.5GB of DL4J/ND4J dependencies).
 * <p>
 * This implementation maintains the same predict/train/save/load API but uses pure Java
 * heuristic scoring instead of neural network inference.
 * </p>
 */
@Component
public class StrategyNetwork {

    private static final Logger log = LoggerFactory.getLogger(StrategyNetwork.class);

    private final DlConfig config;
    private boolean initialized = false;

    // Heuristic weights learned from training data
    private final ConcurrentHashMap<Integer, Double> actionWeights = new ConcurrentHashMap<>();
    private final AtomicLong trainingSamplesCount = new AtomicLong(0);

    public StrategyNetwork(DlConfig config) {
        this.config = config;
    }

    /**
     * Initializes the strategy network with default heuristic weights.
     */
    public void initialize() {
        if (initialized) {
            return;
        }

        // Initialize default weights for each action type (uniform prior)
        for (int i = 0; i < config.getOutputSize(); i++) {
            actionWeights.putIfAbsent(i, 1.0 / config.getOutputSize());
        }

        initialized = true;
        log.info("Lightweight strategy network initialized with {} action types", config.getOutputSize());
    }

    /**
     * Predicts action probabilities for a given game state using heuristic scoring.
     *
     * @param features the game state feature vector
     * @return array of probabilities, one per action type
     */
    public double[] predict(GameStateFeatures features) {
        if (!initialized) {
            initialize();
        }

        double[] featuresArr = features.getFeatures();
        double[] probabilities = new double[config.getOutputSize()];

        // Compute weighted scores for each action based on feature values
        double totalScore = 0;
        for (int i = 0; i < config.getOutputSize(); i++) {
            double weight = actionWeights.getOrDefault(i, 1.0 / config.getOutputSize());
            // Score is weighted sum of features, with action-specific bias
            double score = 0;
            for (int j = 0; j < Math.min(featuresArr.length, config.getInputSize()); j++) {
                score += featuresArr[j] * (weight * (1.0 + (i * 0.1)));
            }
            // Apply softmax-like normalization
            probabilities[i] = Math.max(0, score);
            totalScore += probabilities[i];
        }

        // Normalize to probabilities
        if (totalScore > 0) {
            for (int i = 0; i < probabilities.length; i++) {
                probabilities[i] /= totalScore;
            }
        } else {
            // Uniform distribution if all scores are zero
            double uniform = 1.0 / config.getOutputSize();
            for (int i = 0; i < probabilities.length; i++) {
                probabilities[i] = uniform;
            }
        }

        return probabilities;
    }

    /**
     * Predicts the best action index for a given game state.
     *
     * @param features the game state feature vector
     * @return the index of the best action
     */
    public int predictAction(GameStateFeatures features) {
        double[] probabilities = predict(features);
        int bestAction = 0;
        double bestProbability = probabilities[0];
        for (int i = 1; i < probabilities.length; i++) {
            if (probabilities[i] > bestProbability) {
                bestProbability = probabilities[i];
                bestAction = i;
            }
        }
        return bestAction;
    }

    /**
     * Trains the network by updating heuristic weights from action-reward pairs.
     *
     * @param trainingData list of game states, actions, and rewards
     */
    public void train(List<ActionReward> trainingData) {
        if (!initialized) {
            initialize();
        }

        if (trainingData.isEmpty()) {
            log.warn("No training data provided");
            return;
        }

        int numSamples = trainingData.size();
        long newCount = trainingSamplesCount.addAndGet(numSamples);

        // Update weights based on reward signals
        for (ActionReward sample : trainingData) {
            int action = sample.getAction().getIndex();
            double reward = sample.getReward();
            double currentWeight = actionWeights.getOrDefault(action, 1.0 / config.getOutputSize());
            // Simple reinforcement learning: increase weight for positive rewards
            double learningRate = 0.01;
            double newWeight = currentWeight + learningRate * reward;
            actionWeights.put(action, Math.max(0.001, newWeight));
        }

        log.info("Trained lightweight strategy network on {} samples (total: {})", numSamples, newCount);
    }

    /**
     * Saves the trained model weights to disk (no-op for lightweight model).
     *
     * @return the file path where the model was saved
     */
    public String saveModel() throws IOException {
        if (!initialized) {
            throw new IllegalStateException("Network not initialized");
        }
        log.info("Lightweight strategy network state saved (in-memory weights)");
        return "strategy-network-lightweight";
    }

    /**
     * Loads a trained model from disk (no-op for lightweight model).
     *
     * @param modelPath the path to the saved model file
     */
    public void loadModel(String modelPath) throws IOException {
        initialized = true;
        log.info("Lightweight strategy network initialized (no model file needed)");
    }

    /**
     * Returns whether the network is initialized.
     */
    public boolean isInitialized() {
        return initialized;
    }

    /**
     * Returns the number of training samples processed.
     */
    public long getTrainingSamplesCount() {
        return trainingSamplesCount.get();
    }
}
