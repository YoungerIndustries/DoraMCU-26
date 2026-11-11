package com.yisy.modules.doramcu.neuron.consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yisy.modules.doramcu.neuron.model.NeuronTopicConstants;
import com.yisy.modules.doramcu.neuron.service.NeuronRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

/**
 * Kafka consumer for health heartbeat events from neurons.
 *
 * Consumes heartbeat data from the {@code neuron-health} topic and updates
 * the NeuronRegistry with the latest neuron status information.
 *
 * Enabled only when Kafka is configured and connected.
 */
@Service
@ConditionalOnBean(NeuronRegistry.class)
public class HealthEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(HealthEventConsumer.class);

    private final ObjectMapper objectMapper;
    private final NeuronRegistry neuronRegistry;

    public HealthEventConsumer(ObjectMapper objectMapper, NeuronRegistry neuronRegistry) {
        this.objectMapper = objectMapper;
        this.neuronRegistry = neuronRegistry;
    }

    @KafkaListener(topics = NeuronTopicConstants.TOPIC_HEALTH, groupId = "neuron-coordinator")
    public void consumeHeartbeat(String key, String value, Acknowledgment ack) {
        try {
            JsonNode node = objectMapper.readTree(value);

            String nodeId = node.has("nodeId") ? node.get("nodeId").asText() : key;
            String status = node.has("status") ? node.get("status").asText() : "UNKNOWN";
            long modelVersion = node.has("currentModelVersion") ? node.get("currentModelVersion").asLong() : 0;
            int experienceCount = node.has("experienceCount") ? node.get("experienceCount").asInt() : 0;
            long timestamp = node.has("timestamp") ? node.get("timestamp").asLong() : System.currentTimeMillis();

            if (nodeId != null && !nodeId.isEmpty()) {
                // Update the neuron registry with heartbeat info
                neuronRegistry.updateHeartbeat(nodeId, status, modelVersion, experienceCount, timestamp);
                log.debug("Consumed heartbeat from nodeId={}: status={}, modelVersion={}",
                        nodeId, status, modelVersion);
            }
            ack.acknowledge();
        } catch (Exception e) {
            log.warn("Failed to consume heartbeat event: {}", e.getMessage());
            ack.acknowledge(); // Skip bad message
        }
    }
}
