package com.yisy.modules.doraneuron.consumer;

import com.yisy.modules.doraneuron.service.ModelSyncService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

/**
 * Kafka consumer for model update events from the coordinator.
 *
 * Consumes model update notifications from the `neuron-model-update` topic
 * and triggers ModelSyncService to download and swap the new ONNX model.
 *
 * Enabled only when Kafka is configured and connected.
 */
@Service
@ConditionalOnBean(ModelSyncService.class)
public class ModelUpdateEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(ModelUpdateEventConsumer.class);

    private static final String TOPIC_MODEL_UPDATE = "neuron-model-update";

    private final ModelSyncService modelSyncService;
    private final ObjectMapper objectMapper;

    public ModelUpdateEventConsumer(ModelSyncService modelSyncService, ObjectMapper objectMapper) {
        this.modelSyncService = modelSyncService;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = TOPIC_MODEL_UPDATE, groupId = "#{@environment.getProperty('neuron.kafka.node-id', 'neuron-default')}")
    public void consumeModelUpdate(String key, String value, Acknowledgment ack) {
        try {
            JsonNode node = objectMapper.readTree(value);

            long version = node.has("version") ? node.get("version").asLong() : -1;
            String url = node.has("url") ? node.get("url").asText() : null;
            String hash = node.has("sha256Hash") ? node.get("sha256Hash").asText() : null;
            String signature = node.has("modelSignature") ? node.get("modelSignature").asText() : null;

            if (url != null && !url.isEmpty()) {
                log.info("Received model update event via Kafka: version={}, url={}", version, url);
                modelSyncService.handleModelUpdateFromKafka(url, version, hash, signature);
            }
            ack.acknowledge();
        } catch (Exception e) {
            log.warn("Failed to consume model update event: {}", e.getMessage());
            ack.acknowledge(); // Skip bad message
        }
    }
}
