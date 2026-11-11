package com.yisy.modules.doraneuron.protocol;

import com.yisy.modules.doraneuron.model.NeuronDecision;

import java.util.List;

/**
 * Response from the coordinator after aggregating all Neuron votes.
 *
 * When a consensus round completes, the coordinator returns this result
 * containing the aggregated decision, the list of participating votes,
 * and metadata about the consensus round (total weight, vote count, etc.).
 */
public class ConsensusResult {

    /** The aggregated consensus decision from all votes. */
    private NeuronDecision decision;

    /** List of individual votes that contributed to the consensus. */
    private List<NeuronVote> votes;

    /** Total weight of all votes in the aggregation. */
    private double totalWeight;

    /** Number of Neurons that participated in this consensus round. */
    private int voteCount;

    /** Time taken to complete the consensus round (milliseconds). */
    private long durationMs;

    public ConsensusResult() {
    }

    /**
     * Create a new consensus result.
     *
     * @param decision    the aggregated decision
     * @param votes       the list of participating votes
     * @param durationMs  time taken to complete the round
     */
    public ConsensusResult(NeuronDecision decision, List<NeuronVote> votes, long durationMs) {
        this.decision = decision;
        this.votes = votes;
        this.durationMs = durationMs;
        this.voteCount = votes != null ? votes.size() : 0;
        this.totalWeight = 0;
        if (votes != null) {
            for (NeuronVote vote : votes) {
                this.totalWeight += vote.getWeight();
            }
        }
    }

    public NeuronDecision getDecision() {
        return decision;
    }

    public void setDecision(NeuronDecision decision) {
        this.decision = decision;
    }

    public List<NeuronVote> getVotes() {
        return votes;
    }

    public void setVotes(List<NeuronVote> votes) {
        this.votes = votes;
        this.voteCount = votes != null ? votes.size() : 0;
    }

    public double getTotalWeight() {
        return totalWeight;
    }

    public void setTotalWeight(double totalWeight) {
        this.totalWeight = totalWeight;
    }

    public int getVoteCount() {
        return voteCount;
    }

    public void setVoteCount(int voteCount) {
        this.voteCount = voteCount;
    }

    public long getDurationMs() {
        return durationMs;
    }

    public void setDurationMs(long durationMs) {
        this.durationMs = durationMs;
    }

    @Override
    public String toString() {
        return "ConsensusResult{" +
                "voteCount=" + voteCount +
                ", totalWeight=" + totalWeight +
                ", durationMs=" + durationMs +
                ", decision=" + decision +
                '}';
    }
}
