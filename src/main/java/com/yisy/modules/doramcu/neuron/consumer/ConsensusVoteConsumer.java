package com.yisy.modules.doramcu.neuron.consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yisy.modules.doramcu.neuron.model.NeuronTopicConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Kafka consumer for consensus vote events from neurons.
 *
 * Consumes vote data from the {@code neuron-consensus-vote} topic and
 * aggregates votes for ongoing consensus rounds. Each consensus round
 * is identified by a request key, and votes are collected until timeout.
 *
 * Enabled only when Kafka is configured and connected.
 */
@Service
@ConditionalOnBean(ObjectMapper.class)
public class ConsensusVoteConsumer {

    private static final Logger log = LoggerFactory.getLogger(ConsensusVoteConsumer.class);

    private final ObjectMapper objectMapper;

    /** Ongoing consensus rounds, keyed by request identifier. */
    private final ConcurrentHashMap<String, List<JsonNode>> votesByRound =
            new ConcurrentHashMap<>();

    public ConsensusVoteConsumer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = NeuronTopicConstants.TOPIC_CONSENSUS_VOTE, groupId = "neuron-coordinator")
    public void consumeVote(String key, String value, Acknowledgment ack) {
        try {
            JsonNode node = objectMapper.readTree(value);

            String nodeId = node.has("nodeId") ? node.get("nodeId").asText() : key;

            // Extract the consensus request identifier
            String roundId = node.has("roundId") ? node.get("roundId").asText() :
                             node.has("requestId") ? node.get("requestId").asText() :
                             "default";

            // Accumulate votes by round
            votesByRound.computeIfAbsent(roundId, k -> new CopyOnWriteArrayList<>()).add(node);

            log.debug("Consumed consensus vote from nodeId={} for round={}", nodeId, roundId);
            ack.acknowledge();
        } catch (Exception e) {
            log.warn("Failed to consume consensus vote event: {}", e.getMessage());
            ack.acknowledge(); // Skip bad message
        }
    }

    /**
     * Get all votes collected for a specific consensus round.
     *
     * @param roundId the consensus round identifier
     * @return list of vote JSON nodes, or empty list if none
     */
    public List<JsonNode> getVotesForRound(String roundId) {
        return votesByRound.getOrDefault(roundId, new CopyOnWriteArrayList<>());
    }

    /**
     * Complete a consensus round and remove its votes.
     *
     * @param roundId the consensus round identifier
     * @return the collected votes, or empty list if none
     */
    public List<JsonNode> completeRound(String roundId) {
        return votesByRound.remove(roundId);
    }

    /**
     * Get the number of votes collected for a specific round.
     *
     * @param roundId the consensus round identifier
     * @return vote count
     */
    public int getVoteCount(String roundId) {
        List<JsonNode> votes = votesByRound.get(roundId);
        return votes != null ? votes.size() : 0;
    }

    /**
     * Get the number of active consensus rounds.
     */
    public int getActiveRoundCount() {
        return votesByRound.size();
    }
}
