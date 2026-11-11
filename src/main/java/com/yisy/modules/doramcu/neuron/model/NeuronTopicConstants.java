package com.yisy.modules.doramcu.neuron.model;

/**
 * Constants for Kafka topic names used in the Neuron event-driven architecture.
 *
 * Shared between the coordinator (DoraMCU) and neuron nodes (dora-neuron).
 */
public final class NeuronTopicConstants {

    private NeuronTopicConstants() {
        // Utility class
    }

    /** Neuron → Coordinator: Experience data streaming. */
    public static final String TOPIC_EXPERIENCE = "neuron-experience";

    /** Coordinator → Neuron: Model update notifications. */
    public static final String TOPIC_MODEL_UPDATE = "neuron-model-update";

    /** Coordinator → Neuron: Consensus vote requests. */
    public static final String TOPIC_CONSENSUS_REQUEST = "neuron-consensus-request";

    /** Neuron → Coordinator: Consensus vote responses. */
    public static final String TOPIC_CONSENSUS_VOTE = "neuron-consensus-vote";

    /** Neuron → Coordinator: Health heartbeat streaming. */
    public static final String TOPIC_HEALTH = "neuron-health";

    /** Coordinator ↔ Coordinator: Cross-region federation sync. */
    public static final String TOPIC_FEDERATION_SYNC = "neuron-federation-sync";
}
