package com.yisy.modules.doraneuron.consumer;

import com.yisy.modules.doraneuron.service.ConsensusService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

/**
 * Kafka consumer for consensus request events from the coordinator.
 *
 * Consumes consensus requests from the `neuron-consensus-request` topic
 * and triggers ConsensusService to cast a vote.
 *
 * Enabled only when Kafka is configured and connected.
 */
@Service
@ConditionalOnBean(ConsensusService.class)
public class ConsensusRequestEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(ConsensusRequestEventConsumer.class);

    private static final String TOPIC_CONSENSUS_REQUEST = "neuron-consensus-request";

    private final ConsensusService consensusService;
    private final ObjectMapper objectMapper;

    public ConsensusRequestEventConsumer(ConsensusService consensusService, ObjectMapper objectMapper) {
        this.consensusService = consensusService;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = TOPIC_CONSENSUS_REQUEST, groupId = "#{@environment.getProperty('neuron.kafka.node-id', 'neuron-default')}")
    public void consumeConsensusRequest(String key, String value, Acknowledgment ack) {
        try {
            JsonNode node = objectMapper.readTree(value);

            long timeoutMs = node.has("timeoutMs") ? node.get("timeoutMs").asLong() : 5000;
            String requestingRegion = node.has("requestingRegion") ? node.get("requestingRegion").asText() : null;

            // Extract features array
            double[] features = null;
            if (node.has("features") && node.get("features").isArray()) {
                var featuresNode = node.get("features");
                features = new double[featuresNode.size()];
                for (int i = 0; i < featuresNode.size(); i++) {
                    features[i] = featuresNode.get(i).asDouble();
                }
            }

            if (features != null && features.length > 0) {
                log.info("Received consensus request via Kafka: features={}, timeout={}ms", features.length, timeoutMs);
                consensusService.handleKafkaConsensusRequest(features, timeoutMs, requestingRegion);
            }
            ack.acknowledge();
        } catch (Exception e) {
            log.warn("Failed to consume consensus request event: {}", e.getMessage());
            ack.acknowledge(); // Skip bad message
        }
    }
}
