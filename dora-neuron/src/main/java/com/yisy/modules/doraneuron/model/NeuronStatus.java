package com.yisy.modules.doraneuron.model;

import java.time.Instant;

/**
 * Represents the status of a Neuron node.
 */
public enum NeuronStatus {
    /** Neuron is active and participating in the cluster. */
    ACTIVE,
    /** Neuron is registered but idle. */
    IDLE,
    /** Neuron is experiencing issues but still reachable. */
    DEGRADED,
    /** Neuron is offline and not participating. */
    OFFLINE
}
