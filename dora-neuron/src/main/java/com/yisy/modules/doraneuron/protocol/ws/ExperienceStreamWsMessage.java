package com.yisy.modules.doraneuron.protocol.ws;

import java.util.List;

/**
 * WebSocket experience stream message sent from Neuron to Coordinator.
 *
 * Instead of flushing experience samples via HTTP POST, the neuron
 * streams them in real time over WebSocket. This reduces latency
 * for experience collection and enables immediate aggregation.
 */
public class ExperienceStreamWsMessage extends NeuronWsMessage {

    private static final long serialVersionUID = 1L;

    public static final String TYPE = "EXPERIENCE_STREAM";

    /** List of experience samples in this batch. */
    private List<ExperienceSampleDto> samples;

    /** Cumulative count of samples collected by this neuron. */
    private long totalCollected;

    public ExperienceStreamWsMessage() {
        super(TYPE, null);
    }

    public ExperienceStreamWsMessage(String nodeId, List<ExperienceSampleDto> samples, long totalCollected) {
        super(TYPE, nodeId);
        this.samples = samples;
        this.totalCollected = totalCollected;
    }

    public List<ExperienceSampleDto> getSamples() {
        return samples;
    }

    public void setSamples(List<ExperienceSampleDto> samples) {
        this.samples = samples;
    }

    public long getTotalCollected() {
        return totalCollected;
    }

    public void setTotalCollected(long totalCollected) {
        this.totalCollected = totalCollected;
    }

    /**
     * Lightweight DTO for an experience sample in a WebSocket message.
     * Uses primitive arrays instead of the full ExperienceSample class
     * to reduce serialization overhead.
     */
    public static class ExperienceSampleDto {

        /** Game state feature vector. */
        private double[] state;

        /** Action taken. */
        private int action;

        /** Reward received. */
        private double reward;

        /** Next state feature vector (optional). */
        private double[] nextState;

        /** Whether the episode ended. */
        private boolean done;

        /** Confidence of the prediction that led to this action. */
        private double confidence;

        /** Model version used for this prediction. */
        private long modelVersion;

        public ExperienceSampleDto() {
        }

        public ExperienceSampleDto(double[] state, int action, double reward,
                                    double[] nextState, boolean done,
                                    double confidence, long modelVersion) {
            this.state = state;
            this.action = action;
            this.reward = reward;
            this.nextState = nextState;
            this.done = done;
            this.confidence = confidence;
            this.modelVersion = modelVersion;
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

        public double getConfidence() {
            return confidence;
        }

        public void setConfidence(double confidence) {
            this.confidence = confidence;
        }

        public long getModelVersion() {
            return modelVersion;
        }

        public void setModelVersion(long modelVersion) {
            this.modelVersion = modelVersion;
        }
    }
}
