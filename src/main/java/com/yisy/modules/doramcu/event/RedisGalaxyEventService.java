package com.yisy.modules.doramcu.event;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Redis-backed implementation of galaxy event publishing and subscription.
 * <p>
 * Uses Redis pub/sub for cross-process event distribution, enabling
 * multiple galaxy processes to share events. Falls back to in-memory
 * mode when Redis is unavailable.
 * </p>
 * <p>
 * Activate with: {@code doramcu.event.redis.enabled=true}
 * </p>
 */
@ConditionalOnClass(StringRedisTemplate.class)
@ConditionalOnProperty(name = "doramcu.event.redis.enabled", havingValue = "true", matchIfMissing = false)
@Service
public class RedisGalaxyEventService {

    private static final Logger log = LoggerFactory.getLogger(RedisGalaxyEventService.class);
    private static final String EVENT_CHANNEL_PREFIX = "galaxy:events:";
    private static final String HISTORY_KEY_PREFIX = "galaxy:history:";
    private static final int EVENT_HISTORY_SIZE = 100;

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    /** Local SSE emitters for this process */
    private final Map<String, Subscriber> emitters = new ConcurrentHashMap<>();

    /** Local event history fallback */
    private final List<GalaxyEvent> localEventHistory = new CopyOnWriteArrayList<>();

    @Value("${doramcu.event.redis.history-ttl:3600}")
    private int historyTtlSeconds;

    @Autowired
    public RedisGalaxyEventService(StringRedisTemplate redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        log.info("Redis galaxy event service initialized");
    }

    /**
     * Publishes a galaxy event to Redis pub/sub channel.
     * <p>
     * The event is published to both the global channel and the galaxy-specific channel.
     * </p>
     *
     * @param event the event to publish
     */
    public void publish(GalaxyEvent event) {
        try {
            String json = objectMapper.writeValueAsString(eventToMap(event));

            // Publish to galaxy-specific channel
            String galaxyChannel = EVENT_CHANNEL_PREFIX + event.getGalaxyName();
            redisTemplate.convertAndSend(galaxyChannel, json);
            log.debug("Published event to Redis channel: {}", galaxyChannel);

            // Store in Redis history for this galaxy
            String historyKey = HISTORY_KEY_PREFIX + event.getGalaxyName();
            redisTemplate.opsForList().leftPush(historyKey, json);
            redisTemplate.opsForList().trim(historyKey, 0, EVENT_HISTORY_SIZE - 1);
            redisTemplate.expire(historyKey, historyTtlSeconds, java.util.concurrent.TimeUnit.SECONDS);

            // Also store locally for immediate SSE broadcast
            localEventHistory.add(event);
            if (localEventHistory.size() > EVENT_HISTORY_SIZE) {
                localEventHistory.remove(0);
            }

            // Broadcast to local SSE subscribers
            broadcastLocal(event, json);

        } catch (JsonProcessingException e) {
            log.error("Failed to serialize galaxy event: {}", e.getMessage(), e);
        } catch (Exception e) {
            log.warn("Failed to publish event to Redis, using local fallback: {}", e.getMessage());
            // Fallback to local-only publish
            localEventHistory.add(event);
            broadcastLocal(event, null);
        }
    }

    /**
     * Subscribes a client to galaxy events via SSE.
     *
     * @param clientId unique client identifier
     * @param galaxyNames list of galaxy names to filter by (null = all)
     * @return the registered SseEmitter
     */
    public org.springframework.web.servlet.mvc.method.annotation.SseEmitter subscribe(
            String clientId, List<String> galaxyNames) {

        org.springframework.web.servlet.mvc.method.annotation.SseEmitter emitter =
                new org.springframework.web.servlet.mvc.method.annotation.SseEmitter(30 * 60 * 1000L);

        emitters.put(clientId, new Subscriber(emitter, galaxyNames));

        emitter.onCompletion(() -> {
            log.debug("SSE event stream completed for client: {}", clientId);
            emitters.remove(clientId);
        });
        emitter.onTimeout(() -> {
            log.debug("SSE event stream timed out for client: {}", clientId);
            emitters.remove(clientId);
        });
        emitter.onError((e) -> {
            log.warn("SSE event stream error for client: {}", clientId, e);
            emitters.remove(clientId);
        });

        // Send recent event history on subscription
        sendHistoryToClient(clientId, emitter, galaxyNames);

        return emitter;
    }

    /**
     * Returns the recent event history from Redis, optionally filtered by galaxy name.
     */
    public List<GalaxyEvent> getEventHistory(String galaxyName) {
        if (galaxyName == null) {
            // Return local history as fallback
            return new java.util.ArrayList<>(localEventHistory);
        }

        try {
            String historyKey = HISTORY_KEY_PREFIX + galaxyName;
            List<String> events = redisTemplate.opsForList().range(historyKey, 0, -1);

            if (events == null || events.isEmpty()) {
                return localEventHistory.stream()
                        .filter(e -> galaxyName.equals(e.getGalaxyName()))
                        .toList();
            }

            return events.stream()
                    .map(json -> {
                        try {
                            return mapToEvent(json, galaxyName);
                        } catch (JsonProcessingException e) {
                            log.warn("Failed to parse event from Redis: {}", e.getMessage());
                            return null;
                        }
                    })
                    .filter(e -> e != null)
                    .toList();
        } catch (Exception ex) {
            log.warn("Failed to get event history from Redis: {}", ex.getMessage());
            return localEventHistory.stream()
                    .filter(evt -> galaxyName.equals(evt.getGalaxyName()))
                    .toList();
        }
    }

    /**
     * Returns the total number of connected subscribers.
     */
    public int getSubscriberCount() {
        return emitters.size();
    }

    // -- Internal methods --

    private void broadcastLocal(GalaxyEvent event, String json) {
        List<String> failedClients = new java.util.ArrayList<>();
        emitters.forEach((clientId, subscriber) -> {
            if (!matchesFilter(subscriber.galaxyFilters, event.getGalaxyName())) {
                return;
            }
            try {
                org.springframework.web.servlet.mvc.method.annotation.SseEmitter emitter = subscriber.emitter;
                if (json != null) {
                    emitter.send(org.springframework.web.servlet.mvc.method.annotation.SseEmitter.event()
                            .name(event.getType().name().toLowerCase())
                            .data(json));
                } else {
                    emitter.send(org.springframework.web.servlet.mvc.method.annotation.SseEmitter.event()
                            .name(event.getType().name().toLowerCase())
                            .data(eventToMap(event)));
                }
            } catch (Exception e) {
                log.warn("Failed to send event to client: {}", clientId);
                failedClients.add(clientId);
            }
        });
        failedClients.forEach(emitters::remove);
    }

    private boolean matchesFilter(List<String> filters, String galaxyName) {
        if (filters == null || filters.isEmpty()) {
            return true;
        }
        return filters.contains(galaxyName);
    }

    private void sendHistoryToClient(String clientId,
                                     org.springframework.web.servlet.mvc.method.annotation.SseEmitter emitter,
                                     List<String> galaxyNames) {
        try {
            emitter.send(org.springframework.web.servlet.mvc.method.annotation.SseEmitter.event()
                    .name("subscribed")
                    .data(Map.of(
                            "clientId", clientId,
                            "galaxyFilter", galaxyNames != null && !galaxyNames.isEmpty() ? galaxyNames : "all",
                            "historySize", localEventHistory.size()
                    )));

            for (GalaxyEvent event : localEventHistory) {
                if (matchesFilter(galaxyNames, event.getGalaxyName())) {
                    emitter.send(org.springframework.web.servlet.mvc.method.annotation.SseEmitter.event()
                            .name("history")
                            .data(eventToMap(event)));
                }
            }
        } catch (Exception e) {
            log.warn("Failed to send history to client: {}", clientId, e);
        }
    }

    private Map<String, Object> eventToMap(GalaxyEvent event) {
        Map<String, Object> map = new java.util.HashMap<>();
        map.put("type", event.getType().name());
        map.put("galaxyName", event.getGalaxyName());
        if (event.getGalaxyId() != null) {
            map.put("galaxyId", event.getGalaxyId());
        }
        map.put("timestamp", event.getTimestamp().toString());
        if (!event.getPayload().isEmpty()) {
            map.put("payload", event.getPayload());
        }
        return map;
    }

    private GalaxyEvent mapToEvent(String json, String galaxyName) throws JsonProcessingException {
        Map<String, Object> map = objectMapper.readValue(json, Map.class);
        String type = (String) map.get("type");
        Long galaxyId = map.get("galaxyId") != null ? Long.valueOf(map.get("galaxyId").toString()) : null;
        Map<String, Object> payload = map.get("payload") != null ?
                (Map<String, Object>) map.get("payload") : new java.util.HashMap<>();

        return new GalaxyEvent(
                GalaxyEventType.valueOf(type),
                galaxyName,
                galaxyId,
                payload
        );
    }

    static class Subscriber {
        final org.springframework.web.servlet.mvc.method.annotation.SseEmitter emitter;
        final List<String> galaxyFilters;

        Subscriber(org.springframework.web.servlet.mvc.method.annotation.SseEmitter emitter, List<String> galaxyFilters) {
            this.emitter = emitter;
            this.galaxyFilters = galaxyFilters;
        }
    }
}
