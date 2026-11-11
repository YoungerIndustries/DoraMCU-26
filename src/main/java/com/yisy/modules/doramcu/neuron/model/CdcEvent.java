package com.yisy.modules.doramcu.neuron.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.Map;

/**
 * Represents a Change Data Capture (CDC) event that describes a single
 * entity change in the database. Used to keep game client replicas in sync.
 */
public record CdcEvent(
        String entity,
        String id,
        String operation,
        Map<String, Object> data,
        Instant timestamp,
        long vectorClock
) {
    public static final String OP_CREATE = "CREATE";
    public static final String OP_UPDATE = "UPDATE";
    public static final String OP_DELETE = "DELETE";

    @JsonCreator
    public CdcEvent(
            @JsonProperty("entity") String entity,
            @JsonProperty("id") String id,
            @JsonProperty("operation") String operation,
            @JsonProperty("data") Map<String, Object> data,
            @JsonProperty("timestamp") Instant timestamp,
            @JsonProperty("vectorClock") long vectorClock
    ) {
        this.entity = entity;
        this.id = id;
        this.operation = operation;
        this.data = data;
        this.timestamp = timestamp;
        this.vectorClock = vectorClock;
    }
}
