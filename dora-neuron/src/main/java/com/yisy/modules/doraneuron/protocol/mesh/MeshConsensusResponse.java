package com.yisy.modules.doraneuron.protocol.mesh;

import java.io.Serializable;

/**
 * Response to a decentralized consensus vote request via P2P mesh.
 *
 * Each peer responds with its local inference result, including the
 * predicted action probabilities, selected action, and confidence score.
 */
public class MeshConsensusResponse extends MeshMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Original request ID for correlation. */
    private String requestId;

    /** Predicted action probabilities from local inference. */
    private float[] probabilities;

    /** Selected action index (argmax of probabilities). */
    private int selectedAction;

    /** Confidence score of the prediction. */
    private float confidence;

    /** Vote weight of this neuron. */
    private double voteWeight;

    /** Current model version used for inference. */
    private long modelVersion;

    public MeshConsensusResponse() {
        super("MESH_CONSENSUS_RESPONSE", null);
    }

    public MeshConsensusResponse(String fromNodeId, String requestId,
                                  float[] probabilities, int selectedAction,
                                  float confidence, double voteWeight) {
        super("MESH_CONSENSUS_RESPONSE", fromNodeId);
        this.requestId = requestId;
        this.probabilities = probabilities;
        this.selectedAction = selectedAction;
        this.confidence = confidence;
        this.voteWeight = voteWeight;
    }

    // ── Getters and Setters ───────────────────────────────────────────────────

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
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

    public double getVoteWeight() {
        return voteWeight;
    }

    public void setVoteWeight(double voteWeight) {
        this.voteWeight = voteWeight;
    }

    public long getModelVersion() {
        return modelVersion;
    }

    public void setModelVersion(long modelVersion) {
        this.modelVersion = modelVersion;
    }

    @Override
    public String toString() {
        return "MeshConsensusResponse{requestId='" + requestId + "', action=" + selectedAction +
                ", confidence=" + confidence + ", weight=" + voteWeight + ", " + super.toString() + '}';
    }
}
