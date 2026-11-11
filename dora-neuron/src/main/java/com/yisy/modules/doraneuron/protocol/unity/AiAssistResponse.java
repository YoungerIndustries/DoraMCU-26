package com.yisy.modules.doraneuron.protocol.unity;

import java.io.Serializable;

/**
 * Response from the Neuron to a Unity client AI assistance request.
 *
 * Contains the predicted action, confidence scores for all actions,
 * and metadata about the model version used for the prediction.
 */
public class AiAssistResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    /** The predicted action index (0-16 matching Unity action enum). */
    private int predictedAction;

    /** Human-readable action name (e.g. "BUILD_SHIP", "ENGAGE_COMBAT"). */
    private String predictedActionName;

    /** Confidence score for the predicted action (0.0-1.0). */
    private float confidence;

    /** Probability distribution across all 17 actions. */
    private float[] probabilities;

    /** Model version used for this prediction. */
    private long modelVersion;

    /** Whether consensus was used (true) or local inference only (false). */
    private boolean usedConsensus;

    /** Latency in milliseconds for generating this response. */
    private long latencyMs;

    /** Optional explanation of the prediction reasoning. */
    private String explanation;

    public AiAssistResponse() {
    }

    public AiAssistResponse(int predictedAction, String predictedActionName, float confidence,
                            float[] probabilities, long modelVersion, boolean usedConsensus) {
        this.predictedAction = predictedAction;
        this.predictedActionName = predictedActionName;
        this.confidence = confidence;
        this.probabilities = probabilities;
        this.modelVersion = modelVersion;
        this.usedConsensus = usedConsensus;
    }

    // ── Getters and Setters ───────────────────────────────────────────────────

    public int getPredictedAction() {
        return predictedAction;
    }

    public void setPredictedAction(int predictedAction) {
        this.predictedAction = predictedAction;
    }

    public String getPredictedActionName() {
        return predictedActionName;
    }

    public void setPredictedActionName(String predictedActionName) {
        this.predictedActionName = predictedActionName;
    }

    public float getConfidence() {
        return confidence;
    }

    public void setConfidence(float confidence) {
        this.confidence = confidence;
    }

    public float[] getProbabilities() {
        return probabilities;
    }

    public void setProbabilities(float[] probabilities) {
        this.probabilities = probabilities;
    }

    public long getModelVersion() {
        return modelVersion;
    }

    public void setModelVersion(long modelVersion) {
        this.modelVersion = modelVersion;
    }

    public boolean isUsedConsensus() {
        return usedConsensus;
    }

    public void setUsedConsensus(boolean usedConsensus) {
        this.usedConsensus = usedConsensus;
    }

    public long getLatencyMs() {
        return latencyMs;
    }

    public void setLatencyMs(long latencyMs) {
        this.latencyMs = latencyMs;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    @Override
    public String toString() {
        return "AiAssistResponse{action=" + predictedAction + " (" + predictedActionName + ")" +
                ", confidence=" + confidence + ", modelVersion=" + modelVersion + '}';
    }
}
