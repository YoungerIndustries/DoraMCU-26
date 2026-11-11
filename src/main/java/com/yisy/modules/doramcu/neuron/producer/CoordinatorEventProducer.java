package com.yisy.modules.doramcu.neuron.producer;

import com.yisy.modules.doramcu.neuron.model.NeuronTopicConstants;
import com.yisy.modules.doramcu.neuron.protocol.ConsensusRequest;
import com.yisy.modules.doramcu.neuron.protocol.ModelUpdateNotification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

/**
 * Kafka event producer for the Neuron Coordinator (DoraMCU).
 *
 * Publishes events to Kafka topics for model updates and consensus requests.
 * Neurons subscribe to these topics to receive model updates and participate
 * in consensus voting.
 *
 * Enabled only when KafkaTemplate bean is present (Kafka configured and connected).
 */
@Service
@ConditionalOnBean(KafkaTemplate.class)
public class CoordinatorEventProducer {

    private static final Logger log = LoggerFactory.getLogger(CoordinatorEventProducer.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public CoordinatorEventProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * Publish a model update notification to all neurons.
     *
     * @param notification the model update notification containing version, URL, and hash
     */
    public void publishModelUpdate(ModelUpdateNotification notification) {
        try {
            kafkaTemplate.send(NeuronTopicConstants.TOPIC_MODEL_UPDATE, "coordinator", notification).get();
            log.info("Published model update to Kafka: version={}, url={}",
                    notification.getVersion(), notification.getUrl());
        } catch (Exception e) {
            log.warn("Failed to publish model update to Kafka: {}", e.getMessage());
        }
    }

    /**
     * Publish a consensus request to all neurons.
     *
     * @param request the consensus request containing feature vector and timeout
     */
    public void publishConsensusRequest(ConsensusRequest request) {
        try {
            kafkaTemplate.send(NeuronTopicConstants.TOPIC_CONSENSUS_REQUEST, "coordinator", request).get();
            log.info("Published consensus request to Kafka: features={}, timeout={}ms",
                    request.getFeatureDim(), request.getTimeoutMs());
        } catch (Exception e) {
            log.warn("Failed to publish consensus request to Kafka: {}", e.getMessage());
        }
    }

    /**
     * Check if Kafka is available for publishing.
     */
    public boolean isAvailable() {
        return kafkaTemplate != null;
    }
}
