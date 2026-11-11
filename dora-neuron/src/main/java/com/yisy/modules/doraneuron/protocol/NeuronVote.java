package com.yisy.modules.doraneuron.protocol;

import com.yisy.modules.doraneuron.model.NeuronDecision;

/**
 * A single Neuron's vote in a distributed consensus round.
 *
 * When the coordinator broadcasts a {@link ConsensusRequest}, each active
 * Neuron runs local inference and returns this vote. The vote includes the
 * Neuron's decision (probabilities, selected action, confidence) and the
 * Neuron's vote weight. The coordinator aggregates all votes using weighted
 * averaging to produce the final consensus decision.
 */
public class NeuronVote {

    /** The voting Neuron's unique node ID. */
    private String nodeId;

    /** The Neuron's local AI decision. */
    private NeuronDecision decision;

    /** Weight for this Neuron's vote in the aggregation (default 1.0). */
    private double weight;

    /** Timestamp when this vote was cast (epoch milliseconds). */
    private long timestamp;

    public NeuronVote() {
    }

    /**
     * Create a new vote.
     *
     * @param nodeId   the voting Neuron's node ID
     * @param decision the local AI decision
     * @param weight   the vote weight
     */
    public NeuronVote(String nodeId, NeuronDecision decision, double weight) {
        this.nodeId = nodeId;
        this.decision = decision;
        this.weight = weight;
        this.timestamp = System.currentTimeMillis();
    }

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public NeuronDecision getDecision() {
        return decision;
    }

    public void setDecision(NeuronDecision decision) {
        this.decision = decision;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "NeuronVote{" +
                "nodeId='" + nodeId + '\'' +
                ", weight=" + weight +
                ", decision=" + decision +
                '}';
    }
}
