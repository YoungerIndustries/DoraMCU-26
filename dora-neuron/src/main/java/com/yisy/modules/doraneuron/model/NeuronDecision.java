package com.yisy.modules.doraneuron.model;

/**
 * Represents an AI decision produced by a Neuron's local ONNX inference.
 *
 * Contains the predicted action probabilities, the selected action index,
 * confidence score, and the model version used for the prediction.
 */
public class NeuronDecision {

    /** Probabilities for each action type (size matches DL output size, typically 17). */
    private float[] probabilities;

    /** Index of the action with the highest probability. */
    private int selectedAction;

    /** Confidence score (probability of the selected action). */
    private float confidence;

    /** Version of the ONNX model used for this prediction. */
    private long modelVersion;

    public NeuronDecision() {
    }

    public NeuronDecision(float[] probabilities, int selectedAction, float confidence, long modelVersion) {
        this.probabilities = probabilities;
        this.selectedAction = selectedAction;
        this.confidence = confidence;
        this.modelVersion = modelVersion;
    }

    /**
     * Create a NeuronDecision from raw output probabilities.
     * Finds the argmax and sets confidence to the max probability.
     */
    public static NeuronDecision fromProbabilities(float[] probabilities, long modelVersion) {
        int selectedAction = 0;
        float confidence = 0f;
        for (int i = 0; i < probabilities.length; i++) {
            if (probabilities[i] > confidence) {
                confidence = probabilities[i];
                selectedAction = i;
            }
        }
        return new NeuronDecision(probabilities.clone(), selectedAction, confidence, modelVersion);
    }

    public float[] getProbabilities() {
        return probabilities;
    }

    public void setProbabilities(float[] probabilities) {
        this.probabilities = probabilities;
    }

    public int getSelectedAction() {
        return selectedAction;
    }

    public void setSelectedAction(int selectedAction) {
        this.selectedAction = selectedAction;
    }

    public float getConfidence() {
        return confidence;
    }

    public void setConfidence(float confidence) {
        this.confidence = confidence;
    }

    public long getModelVersion() {
        return modelVersion;
    }

    public void setModelVersion(long modelVersion) {
        this.modelVersion = modelVersion;
    }

    @Override
    public String toString() {
        return "NeuronDecision{" +
                "selectedAction=" + selectedAction +
                ", confidence=" + confidence +
                ", modelVersion=" + modelVersion +
                '}';
    }
}
