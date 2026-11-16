package com.yisy.modules.doramcu.ai.framework;

import com.yisy.modules.doradl.dl.service.DlGameAiService;
import com.yisy.modules.doradl.dl.service.DoraAiLearningService;
import com.yisy.modules.doradl.dto.GameStateResponse;
import com.yisy.modules.doradl.model.ActionReward;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Adapts DlGameAiService and DoraAiLearningService to the unified AiLearningService interface.
 * <p>
 * This adapter provides a single entry point for all neural network learning operations,
 * combining the prediction capabilities of DlGameAiService with the training capabilities
 * of DoraAiLearningService.
 * </p>
 */
@Service
public class DlGameAiLearningAdapter implements AiLearningService {

    private static final Logger log = LoggerFactory.getLogger(DlGameAiLearningAdapter.class);

    private final DlGameAiService dlGameAiService;
    private final DoraAiLearningService doraAiLearningService;

    public DlGameAiLearningAdapter(DlGameAiService dlGameAiService,
                                    DoraAiLearningService doraAiLearningService) {
        this.dlGameAiService = dlGameAiService;
        this.doraAiLearningService = doraAiLearningService;
    }

    @Override
    public Map<String, Double> trainModel(List<Map<String, Object>> trainingData) {
        // Convert training data to recordAction calls
        for (Map<String, Object> data : trainingData) {
            try {
                Object actionObj = data.getOrDefault("action", "WAIT");
                double reward = ((Number) data.getOrDefault("reward", 0.0)).doubleValue();
                Long playerId = data.containsKey("playerId")
                        ? ((Number) data.get("playerId")).longValue()
                        : 1L;

                ActionReward.Action action;
                if (actionObj instanceof ActionReward.Action) {
                    action = (ActionReward.Action) actionObj;
                } else if (actionObj instanceof Number) {
                    action = ActionReward.Action.fromIndex(((Number) actionObj).intValue());
                } else {
                    try {
                        action = ActionReward.Action.valueOf(actionObj.toString().toUpperCase().replace(" ", "_"));
                    } catch (IllegalArgumentException e) {
                        action = ActionReward.Action.WAIT;
                    }
                }

                GameStateResponse gameState = new GameStateResponse();
                gameState.setCurrentPlayerId(playerId);
                doraAiLearningService.recordAction(gameState, playerId, action, reward);
            } catch (Exception e) {
                log.warn("Failed to process training data: {}", e.getMessage());
            }
        }
        return getModelMetrics();
    }

    @Override
    public double[] predict(double[] features) {
        // Use DlGameAiService to get prediction scores
        GameStateResponse gameState = new GameStateResponse();
        gameState.setCurrentPlayerId(1L);
        Map<String, Object> prediction = dlGameAiService.predictAction(gameState, 1L);

        // Extract blended scores from prediction
        @SuppressWarnings("unchecked")
        Map<String, Double> actionScores = (Map<String, Double>) prediction.getOrDefault("blendedScores", Map.of());

        double[] scores = new double[ActionReward.Action.values().length];
        for (ActionReward.Action action : ActionReward.Action.values()) {
            scores[action.getIndex()] = actionScores.getOrDefault(action.name(), 0.0);
        }
        return scores;
    }

    @Override
    public int predictBestAction(double[] features) {
        double[] scores = predict(features);
        int bestIndex = 0;
        double bestScore = scores[0];
        for (int i = 1; i < scores.length; i++) {
            if (scores[i] > bestScore) {
                bestScore = scores[i];
                bestIndex = i;
            }
        }
        return bestIndex;
    }

    @Override
    public Map<String, Double> getModelMetrics() {
        Map<String, Double> metrics = new HashMap<>();
        metrics.put("experienceBufferSize", (double) doraAiLearningService.getExperienceBufferSize());
        metrics.put("totalActionsRecorded", (double) doraAiLearningService.getTotalActionsRecorded());
        metrics.put("totalTrainingCycles", (double) doraAiLearningService.getTotalTrainingCycles());
        metrics.put("networkReady", dlGameAiService.isNetworkReady() ? 1.0 : 0.0);
        metrics.put("blendFactor", (double) dlGameAiService.getBlendFactor());
        return metrics;
    }

    @Override
    public ModelStatus getModelStatus() {
        if (!dlGameAiService.isNetworkReady()) {
            return ModelStatus.EMPTY;
        }
        return ModelStatus.READY;
    }

    @Override
    public void saveModel(String path) {
        doraAiLearningService.saveModel();
        log.info("Model saved (path parameter ignored, using configured path)");
    }

    @Override
    public void loadModel(String path) {
        // DlGameAiService initializes network on first use
        GameStateResponse gameState = new GameStateResponse();
        gameState.setCurrentPlayerId(1L);
        dlGameAiService.predictAction(gameState, 1L);
        log.info("Model loaded (path parameter ignored, using configured path)");
    }

    @Override
    public void resetModel() {
        doraAiLearningService.clearExperience();
        log.info("Model experience buffer cleared");
    }

    @Override
    public long getTrainingIterations() {
        return doraAiLearningService.getTotalTrainingCycles();
    }
}
