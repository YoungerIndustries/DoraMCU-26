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

import java.util.ArrayList;
import java.util.List;

/**
 * Kafka consumer for experience events from neurons.
 *
 * Consumes experience data from the {@code neuron-experience} topic,
 * aggregates samples by nodeId, and stores them for model training.
 *
 * Enabled only when Kafka is configured and connected.
 */
@Service
@ConditionalOnBean(ObjectMapper.class)
public class ExperienceEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(ExperienceEventConsumer.class);

    private final ObjectMapper objectMapper;

    /** Accumulated experience samples, keyed by nodeId. */
    private final java.util.concurrent.ConcurrentHashMap<String, List<JsonNode>> experienceByNode =
            new java.util.concurrent.ConcurrentHashMap<>();

    public ExperienceEventConsumer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = NeuronTopicConstants.TOPIC_EXPERIENCE, groupId = "neuron-coordinator")
    public void consumeExperience(String key, String value, Acknowledgment ack) {
        try {
            String nodeId = key != null ? key : "unknown";
            JsonNode node = objectMapper.readTree(value);

            // Extract samples from the batch
            List<JsonNode> samples = new ArrayList<>();
            if (node.has("samples") && node.get("samples").isArray()) {
                for (JsonNode sample : node.get("samples")) {
                    samples.add(sample);
                }
            }

            // Accumulate by nodeId
            experienceByNode.computeIfAbsent(nodeId, k -> new ArrayList<>()).addAll(samples);

            log.debug("Consumed {} experience samples from nodeId={}", samples.size(), nodeId);
            ack.acknowledge();
        } catch (Exception e) {
            log.warn("Failed to consume experience event: {}", e.getMessage());
            ack.acknowledge(); // Skip bad message
        }
    }

    /**
     * Get accumulated experience samples for a specific node.
     *
     * @param nodeId the neuron node ID
     * @return list of experience samples, or empty list if none
     */
    public List<JsonNode> getExperienceSamples(String nodeId) {
        return experienceByNode.getOrDefault(nodeId, new ArrayList<>());
    }

    /**
     * Clear accumulated experience samples for a specific node.
     *
     * @param nodeId the neuron node ID
     * @return number of samples cleared
     */
    public int clearExperienceSamples(String nodeId) {
        List<JsonNode> removed = experienceByNode.remove(nodeId);
        return removed != null ? removed.size() : 0;
    }

    /**
     * Get total accumulated sample count across all nodes.
     */
    public long getTotalSampleCount() {
        return experienceByNode.values().stream().mapToLong(List::size).sum();
    }
}
