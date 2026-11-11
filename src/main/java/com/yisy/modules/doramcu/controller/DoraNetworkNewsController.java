package com.yisy.modules.doramcu.controller;

import com.yisy.modules.doramcu.event.GalaxyEvent;
import com.yisy.modules.doramcu.event.GalaxyEventService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * REST controller for the Dora Network News feed.
 * <p>
 * Provides a public-facing news feed that announces major system events
 * across the DoraMCU universe. Clients can subscribe via SSE for live
 * updates or fetch the recent event history via a simple REST call.
 * </p>
 */
@RestController
@RequestMapping("/api/v1/news")
public class DoraNetworkNewsController {

    private static final Logger log = LoggerFactory.getLogger(DoraNetworkNewsController.class);
    private static final int FEED_SIZE = 50;

    private final GalaxyEventService galaxyEventService;

    public DoraNetworkNewsController(GalaxyEventService galaxyEventService) {
        this.galaxyEventService = galaxyEventService;
    }

    /**
     * Fetch the most recent news events (REST endpoint).
     *
     * @param limit maximum number of events to return (default 50)
     * @return list of recent events formatted for the news feed
     */
    @GetMapping("/feed")
    public List<Map<String, Object>> getFeed(@RequestParam(defaultValue = "50") int limit) {
        List<GalaxyEvent> events = galaxyEventService.getEventHistory(null);
        int fromIndex = Math.max(0, events.size() - Math.min(limit, FEED_SIZE));
        return events.subList(fromIndex, events.size()).stream()
                .map(this::toNewsItem)
                .toList();
    }

    /**
     * Subscribe to live news updates via Server-Sent Events.
     * <p>
     * On connection, the client receives a "connected" event followed by
     * recent history events, then real-time events as they occur.
     * </p>
     *
     * @return SSE emitter streaming news events
     */
    @GetMapping(value = "/live", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribeLive() {
        String clientId = "news-" + UUID.randomUUID().toString();
        log.info("Dora Network News client connected: {}", clientId);

        SseEmitter emitter = galaxyEventService.subscribeAll(clientId);

        // Send a welcome event with feed metadata
        try {
            emitter.send(SseEmitter.event()
                    .name("connected")
                    .data(Map.of(
                            "title", "Dora Network News",
                            "tagline", "Live updates from the DoraMCU Universe",
                            "subscriberCount", galaxyEventService.getSubscriberCount()
                    )));
        } catch (Exception e) {
            log.warn("Failed to send welcome event to news client: {}", clientId, e);
        }

        return emitter;
    }

    /**
     * Get current feed statistics.
     *
     * @return map with subscriber count and event history size
     */
    @GetMapping("/stats")
    public Map<String, Object> getStats() {
        return Map.of(
                "subscribers", galaxyEventService.getSubscriberCount(),
                "eventHistorySize", galaxyEventService.getEventHistory(null).size()
        );
    }

    /**
     * Manually announce a news event (for testing or admin use).
     *
     * @param type event type
     * @param galaxyName galaxy name
     * @param message human-readable announcement message
     */
    @PostMapping("/announce")
    public Map<String, String> announce(
            @RequestParam String type,
            @RequestParam String galaxyName,
            @RequestParam String message) {
        galaxyEventService.publish(
                com.yisy.modules.doramcu.event.GalaxyEventType.valueOf(type),
                galaxyName,
                null,
                "message", message
        );
        return Map.of("status", "announced", "type", type, "galaxy", galaxyName);
    }

    /**
     * Convert a GalaxyEvent to a news-friendly map.
     */
    private Map<String, Object> toNewsItem(GalaxyEvent event) {
        Map<String, Object> item = new java.util.HashMap<>();
        item.put("id", event.getTimestamp().toEpochMilli());
        item.put("type", event.getType().name());
        item.put("title", formatEventTitle(event));
        item.put("galaxyName", event.getGalaxyName());
        item.put("timestamp", event.getTimestamp().toString());
        if (!event.getPayload().isEmpty()) {
            item.put("details", event.getPayload());
        }
        return item;
    }

    /**
     * Format a human-readable title for an event.
     */
    private String formatEventTitle(GalaxyEvent event) {
        String galaxy = event.getGalaxyName() != null ? event.getGalaxyName() : "Unknown";
        return switch (event.getType()) {
            case BUILD_STARTED -> "Galaxy Build Initiated: " + galaxy;
            case BUILD_COMPLETED -> "Galaxy Build Complete: " + galaxy;
            case BUILD_FAILED -> "Build Failure Detected: " + galaxy;
            case SECTOR_GENERATED -> "New Sector Generated in " + galaxy;
            case GALAXY_CREATED -> "New Galaxy Discovered: " + galaxy;
            case HEALTH_THRESHOLD_CROSSED -> "Health Alert: " + galaxy;
            case CHARACTER_TRANSITIONED -> "Character Transitioned to " + galaxy;
            case WORK_ORDER_CREATED -> "Work Order Created for " + galaxy;
            case AI_AGENT_DEPLOYED -> "AI Agent Deployed to " + galaxy;
            case ASSESSMENT_COMPLETED -> "Assessment Complete: " + galaxy;
            case WORMHOLE_CREATED -> "Wormhole Established: " + galaxy;
            case WORMHOLE_TRAVEL -> "Wormhole Travel Detected via " + galaxy;
            case WORMHOLE_TOGGLED -> "Wormhole Status Changed: " + galaxy;
        };
    }
}
