package com.yisy.modules.doramcu.neuron.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yisy.modules.doramcu.neuron.model.CdcEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import jakarta.persistence.Entity;
import jakarta.persistence.PostPersist;
import jakarta.persistence.PostRemove;
import jakarta.persistence.PostUpdate;
import jakarta.persistence.PrePersist;
import java.lang.reflect.Field;
import java.time.Instant;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Publishes Change Data Capture (CDC) events whenever JPA entities are
 * created, updated, or deleted. Listeners (e.g., WebSocket handlers) subscribe
 * to receive these events in real time.
 * <p>
 * This service uses JPA EntityListeners to intercept persistence events and
 * convert them into {@link CdcEvent} records that are broadcast to subscribers.
 * </p>
 */
@Service
public class CdcEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(CdcEventPublisher.class);

    private final ObjectMapper objectMapper;
    private final AtomicLong vectorClock = new AtomicLong(0);
    private final List<CdcEventListener> listeners = new CopyOnWriteArrayList<>();

    public CdcEventPublisher(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Registers a listener for CDC events.
     */
    public void addListener(CdcEventListener listener) {
        listeners.add(listener);
    }

    /**
     * Removes a listener.
     */
    public void removeListener(CdcEventListener listener) {
        listeners.remove(listener);
    }

    /**
     * Called by entity listeners when an entity is persisted.
     */
    public void onEntityCreated(Object entity) {
        publishEvent(entity, CdcEvent.OP_CREATE);
    }

    /**
     * Called by entity listeners when an entity is updated.
     */
    public void onEntityUpdated(Object entity) {
        publishEvent(entity, CdcEvent.OP_UPDATE);
    }

    /**
     * Called by entity listeners when an entity is removed.
     */
    public void onEntityDeleted(Object entity) {
        publishEvent(entity, CdcEvent.OP_DELETE);
    }

    private void publishEvent(Object entity, String operation) {
        try {
            String entityName = entity.getClass().getSimpleName();
            String id = extractId(entity);
            Map<String, Object> data = extractData(entity);

            CdcEvent event = new CdcEvent(
                    entityName,
                    id,
                    operation,
                    data,
                    Instant.now(),
                    vectorClock.incrementAndGet()
            );

            for (CdcEventListener listener : listeners) {
                try {
                    listener.onCdcEvent(event);
                } catch (Exception e) {
                    log.warn("CDC listener failed: {}", e.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("Failed to publish CDC event for {}: {}", entity.getClass().getSimpleName(), e.getMessage());
        }
    }

    private String extractId(Object entity) {
        try {
            Field idField = entity.getClass().getDeclaredField("id");
            idField.setAccessible(true);
            Object id = idField.get(entity);
            return id != null ? id.toString() : "unknown";
        } catch (NoSuchFieldException | IllegalAccessException e) {
            return "unknown";
        }
    }

    private Map<String, Object> extractData(Object entity) {
        Map<String, Object> data = new HashMap<>();
        try {
            String json = objectMapper.writeValueAsString(entity);
            @SuppressWarnings("unchecked")
            Map<String, Object> map = objectMapper.readValue(json, Map.class);
            data.putAll(map);
        } catch (JsonProcessingException e) {
            log.warn("Failed to serialize entity for CDC: {}", e.getMessage());
        }
        return data;
    }

    /**
     * Functional interface for CDC event listeners.
     */
    @FunctionalInterface
    public interface CdcEventListener {
        void onCdcEvent(CdcEvent event);
    }
}
