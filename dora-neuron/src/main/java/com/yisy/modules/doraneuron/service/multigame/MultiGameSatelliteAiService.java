package com.yisy.modules.doraneuron.service.multigame;

import com.yisy.modules.doraneuron.config.NeuronConfig;
import com.yisy.modules.doraneuron.config.NeuronPrometheusMetrics;
import com.yisy.modules.doraneuron.model.NeuronDecision;
import com.yisy.modules.doraneuron.model.multigame.GameProfile;
import com.yisy.modules.doraneuron.util.InferenceEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Multi-game aware satellite AI service that routes inference requests
 * to the appropriate game-specific InferenceEngine.
 *
 * This service replaces the single-game SatelliteAiService for multi-game
 * scenarios. It uses GameRegistryService to look up the correct engine
 * for each game and runs inference with game-specific confidence thresholds.
 *
 * For backward compatibility, it also supports the default game when no
 * gameId is specified.
 */
@Service
public class MultiGameSatelliteAiService {

    private static final Logger log = LoggerFactory.getLogger(MultiGameSatelliteAiService.class);

    private final GameRegistryService gameRegistry;
    private final NeuronConfig config;
    private final NeuronPrometheusMetrics prometheusMetrics;

    public MultiGameSatelliteAiService(GameRegistryService gameRegistry, NeuronConfig config,
                                       NeuronPrometheusMetrics prometheusMetrics) {
        this.gameRegistry = gameRegistry;
        this.config = config;
        this.prometheusMetrics = prometheusMetrics;
    }

    /**
     * Run inference for a specific game.
     *
     * @param gameId the game identifier
     * @param features the game state feature vector
     * @return a decision containing selected action, confidence, and probability distribution
     */
    public NeuronDecision predict(String gameId, double[] features) {
        Optional<GameProfile> profileOpt = gameRegistry.getGame(gameId);
        if (profileOpt.isEmpty()) {
            log.warn("Unknown game '{}', falling back to default", gameId);
            return predictDefault(features);
        }

        GameProfile profile = profileOpt.get();
        profile.incrementPredictions();

        Optional<InferenceEngine> engineOpt = gameRegistry.getEngine(gameId);
        if (engineOpt.isEmpty() || !engineOpt.get().isModelLoaded()) {
            log.warn("No ONNX model loaded for game '{}'; returning empty decision", gameId);
            prometheusMetrics.recordInferenceError();
            return new NeuronDecision();
        }

        InferenceEngine engine = engineOpt.get();
        long start = System.currentTimeMillis();
        try {
            float[] output = engine.runInference(features);
            if (output == null) {
                log.warn("Inference returned null output for game {}", gameId);
                prometheusMetrics.recordInferenceError();
                return new NeuronDecision();
            }

            NeuronDecision decision = NeuronDecision.fromProbabilities(output, profile.getModelVersion());

            // Check game-specific confidence threshold
            if (decision.getConfidence() < profile.getConfidenceThreshold()) {
                log.debug("Low confidence prediction for game {}: {} (threshold: {})",
                        gameId, decision.getConfidence(), profile.getConfidenceThreshold());
            }

            long duration = System.currentTimeMillis() - start;
            prometheusMetrics.recordInference(duration);

            log.debug("AI prediction for game {}: action={}, confidence={}, modelVersion={}",
                    gameId, decision.getSelectedAction(), decision.getConfidence(), profile.getModelVersion());
            return decision;
        } catch (Exception e) {
            log.error("Inference failed for game {}: {}", gameId, e.getMessage());
            prometheusMetrics.recordInferenceError();
            return new NeuronDecision();
        }
    }

    /**
     * Run inference for a specific game with float[] features.
     */
    public NeuronDecision predict(String gameId, float[] features) {
        double[] doubleFeatures = new double[features.length];
        for (int i = 0; i < features.length; i++) {
            doubleFeatures[i] = features[i];
        }
        return predict(gameId, doubleFeatures);
    }

    /**
     * Run inference using the default game (backward compatibility).
     */
    public NeuronDecision predict(double[] features) {
        return predictDefault(features);
    }

    /**
     * Run inference using the default game.
     */
    private NeuronDecision predictDefault(double[] features) {
        Optional<GameProfile> defaultOpt = gameRegistry.getDefaultGame();
        if (defaultOpt.isEmpty()) {
            log.warn("No default game registered; returning empty decision");
            return new NeuronDecision();
        }

        GameProfile profile = defaultOpt.get();
        return predict(profile.getGameId(), features);
    }

    /**
     * Swap the model for a specific game.
     *
     * @param gameId the game identifier
     * @param modelPath path to the new ONNX model
     * @param version the version number of the new model
     */
    public void swapModel(String gameId, String modelPath, long version) {
        Optional<GameProfile> profileOpt = gameRegistry.getGame(gameId);
        if (profileOpt.isEmpty()) {
            log.warn("Cannot swap model: unknown game '{}'", gameId);
            return;
        }

        GameProfile profile = profileOpt.get();
        log.info("Swapping ONNX model for game {}: {} (version {})", gameId, modelPath, version);

        Optional<InferenceEngine> engineOpt = gameRegistry.getEngine(gameId);
        if (engineOpt.isPresent()) {
            try {
                engineOpt.get().reloadModel(modelPath);
                profile.setOnnxModelPath(modelPath);
                profile.setModelVersion(version);
                prometheusMetrics.setModelVersion(version);
                log.info("Model swap complete for game {}. Current version: {}", gameId, version);
            } catch (Exception e) {
                log.error("Failed to swap model for game {}: {}", gameId, e.getMessage());
            }
        } else {
            // No engine yet; update the profile so it loads on next access
            profile.setOnnxModelPath(modelPath);
            profile.setModelVersion(version);
            log.info("No engine for game {}; model path updated for next load", gameId);
        }
    }

    /**
     * Check if AI is ready for a specific game.
     */
    public boolean isReady(String gameId) {
        Optional<GameProfile> profileOpt = gameRegistry.getGame(gameId);
        if (profileOpt.isEmpty()) {
            return false;
        }
        return profileOpt.get().isReady();
    }

    /**
     * Check if AI is ready for the default game.
     */
    public boolean isReady() {
        Optional<GameProfile> defaultOpt = gameRegistry.getDefaultGame();
        if (defaultOpt.isEmpty()) {
            return false;
        }
        return isReady(defaultOpt.get().getGameId());
    }

    /**
     * Get the current model version for a specific game.
     */
    public long getModelVersion(String gameId) {
        return gameRegistry.getGame(gameId)
                .map(GameProfile::getModelVersion)
                .orElse(0L);
    }
}
