package com.yisy.modules.doraneuron.producer;

import com.yisy.modules.doraneuron.model.NeuronIdentity;
import com.yisy.modules.doraneuron.protocol.ConsensusRequest;
import com.yisy.modules.doraneuron.protocol.ExperienceBatch;
import com.yisy.modules.doraneuron.protocol.NeuronHeartbeat;
import com.yisy.modules.doraneuron.protocol.NeuronVote;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

/**
 * Kafka event producer for the Neuron node.
 *
 * Publishes events to Kafka topics for experience data, health heartbeats,
 * and consensus votes. Falls back to HTTP REST if Kafka is unavailable.
 *
 * Enabled only when KafkaTemplate bean is present (Kafka configured and connected).
 */
@Service
@ConditionalOnBean(KafkaTemplate.class)
public class NeuronEventProducer {

    private static final Logger log = LoggerFactory.getLogger(NeuronEventProducer.class);

    private static final String TOPIC_EXPERIENCE = "neuron-experience";
    private static final String TOPIC_HEALTH = "neuron-health";
    private static final String TOPIC_CONSENSUS_VOTE = "neuron-consensus-vote";

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final NeuronIdentity identity;

    public NeuronEventProducer(KafkaTemplate<String, Object> kafkaTemplate, NeuronIdentity identity) {
        this.kafkaTemplate = kafkaTemplate;
        this.identity = identity;
    }

    /**
     * Publish experience batch to Kafka.
     * Partitioned by nodeId for ordering.
     */
    public void publishExperience(ExperienceBatch batch) {
        try {
            kafkaTemplate.send(TOPIC_EXPERIENCE, identity.getNodeId(), batch).get();
            log.debug("Published experience batch to Kafka: {} samples", batch.getSamples().size());
        } catch (Exception e) {
            log.warn("Failed to publish experience to Kafka: {}", e.getMessage());
        }
    }

    /**
     * Publish heartbeat to Kafka.
     * Partitioned by nodeId for ordering.
     */
    public void publishHeartbeat(NeuronHeartbeat heartbeat) {
        try {
            kafkaTemplate.send(TOPIC_HEALTH, identity.getNodeId(), heartbeat).get();
            log.debug("Published heartbeat to Kafka");
        } catch (Exception e) {
            log.warn("Failed to publish heartbeat to Kafka: {}", e.getMessage());
        }
    }

    /**
     * Publish consensus vote to Kafka.
     */
    public void publishVote(NeuronVote vote) {
        try {
            kafkaTemplate.send(TOPIC_CONSENSUS_VOTE, identity.getNodeId(), vote).get();
            log.debug("Published consensus vote to Kafka");
        } catch (Exception e) {
            log.warn("Failed to publish vote to Kafka: {}", e.getMessage());
        }
    }

    /**
     * Check if Kafka is available for publishing.
     */
    public boolean isAvailable() {
        return kafkaTemplate != null;
    }
}
