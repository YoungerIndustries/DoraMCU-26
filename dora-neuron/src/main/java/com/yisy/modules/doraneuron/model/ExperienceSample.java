package com.yisy.modules.doraneuron.model;

/**
 * Represents a single experience sample collected by a Neuron.
 *
 * An experience is a (state, action, reward) tuple that records what the AI
 * observed, what action it took, and what outcome resulted. These samples are
 * buffered locally and periodically flushed to the central coordinator for
 * aggregation and model retraining — analogous to memory consolidation in a
 * biological brain.
 */
public class ExperienceSample {

    /** The game state feature vector at the time of the action (typically 64 dimensions). */
    private double[] state;

    /** The action index selected by the AI. */
    private int action;

    /** The reward signal received after taking the action. */
    private double reward;

    /** Optional: the next state feature vector after the action was taken. */
    private double[] nextState;

    /** Whether the episode terminated after this action. */
    private boolean done;

    /** Confidence of the AI decision that produced this action. */
    private float confidence;

    /** Version of the ONNX model that produced this action. */
    private long modelVersion;

    /** Timestamp when this experience was collected (epoch milliseconds). */
    private long timestamp;

    /** Source Neuron node ID. */
    private String nodeId;

    public ExperienceSample() {
    }

    /**
     * Create an experience sample with state, action, and reward.
     *
     * @param state        the game state feature vector
     * @param action       the action index selected
     * @param reward       the reward signal received
     */
    public ExperienceSample(double[] state, int action, double reward) {
        this.state = state;
        this.action = action;
        this.reward = reward;
        this.timestamp = System.currentTimeMillis();
    }

    public double[] getState() {
        return state;
    }

    public void setState(double[] state) {
        this.state = state;
    }

    public int getAction() {
        return action;
    }

    public void setAction(int action) {
        this.action = action;
    }

    public double getReward() {
        return reward;
    }

    public void setReward(double reward) {
        this.reward = reward;
    }

    public double[] getNextState() {
        return nextState;
    }

    public void setNextState(double[] nextState) {
        this.nextState = nextState;
    }

    public boolean isDone() {
        return done;
    }

    public void setDone(boolean done) {
        this.done = done;
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

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    @Override
    public String toString() {
        return "ExperienceSample{" +
                "action=" + action +
                ", reward=" + reward +
                ", confidence=" + confidence +
                ", modelVersion=" + modelVersion +
                ", stateDim=" + (state != null ? state.length : 0) +
                '}';
    }
}
