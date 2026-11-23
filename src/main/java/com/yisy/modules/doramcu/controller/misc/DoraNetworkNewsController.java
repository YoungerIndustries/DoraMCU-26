package com.yisy.modules.doramcu.controller.misc;
import com.yisy.modules.doramcu.event.GalaxyEvent;
import com.yisy.modules.doramcu.event.GalaxyEventService;
import com.yisy.modules.doramcu.repository.galaxy.SolarSystemRepository;
import com.yisy.modules.doramcu.service.galaxy.BackgroundGalaxyBuilder;
import com.yisy.modules.doramcu.service.galaxy.GalaxyOverseerService;
import com.yisy.modules.doramcu.service.galaxy.GalaxyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

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
    private final GalaxyService galaxyService;
    private final SolarSystemRepository solarSystemRepository;
    private final GalaxyOverseerService galaxyOverseerService;
    private final BackgroundGalaxyBuilder backgroundGalaxyBuilder;

    public DoraNetworkNewsController(GalaxyEventService galaxyEventService, GalaxyService galaxyService,
                                      SolarSystemRepository solarSystemRepository,
                                      GalaxyOverseerService galaxyOverseerService,
                                      BackgroundGalaxyBuilder backgroundGalaxyBuilder) {
        this.galaxyEventService = galaxyEventService;
        this.galaxyService = galaxyService;
        this.solarSystemRepository = solarSystemRepository;
        this.galaxyOverseerService = galaxyOverseerService;
        this.backgroundGalaxyBuilder = backgroundGalaxyBuilder;
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
     * Get current presence information for active connections.
     * <p>
     * Used by the Dora News Network to display real-time connection counts
     * instead of simulated player numbers.
     * </p>
     *
     * @return map with active SSE subscriber count and event history size
     */
    @GetMapping("/presence")
    public Map<String, Object> getPresence() {
        return Map.of(
                "activeSubscribers", galaxyEventService.getSubscriberCount(),
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
     * Lightweight cached galaxy health summary.
     * <p>
     * Returns the latest assessment data for each galaxy from the in-memory
     * event history instead of running expensive synchronous assessments.
     * This endpoint is designed for the Dora News Network proxy which needs
     * fast responses on low-power hardware (e.g., Raspberry Pi).
     * </p>
     *
     * @return list of cached health summaries, one per galaxy with recent assessments
     */
    @GetMapping("/galaxy-health-summary")
    public List<Map<String, Object>> getCachedGalaxyHealth() {
        // Start with ALL active galaxies from the database
        List<com.yisy.modules.doramcu.model.galaxy.Galaxy> allGalaxies = galaxyService.getAllGalaxies();

        // Build a map of the latest ASSESSMENT_COMPLETED event per galaxy from event history
        // to enrich the summary with assessment data where available
        Map<String, GalaxyEvent> latestAssessment = new java.util.LinkedHashMap<>();
        List<GalaxyEvent> events = galaxyEventService.getEventHistory(null);
        for (GalaxyEvent event : events) {
            if (event.getType() == com.yisy.modules.doramcu.event.GalaxyEventType.ASSESSMENT_COMPLETED) {
                latestAssessment.putIfAbsent(event.getGalaxyName(), event);
            }
        }

        // Build a summary for EVERY active galaxy, using assessment data when available
        List<Map<String, Object>> summaries = new java.util.ArrayList<>();
        for (com.yisy.modules.doramcu.model.galaxy.Galaxy galaxy : allGalaxies) {
            Map<String, Object> summary = new java.util.HashMap<>();
            String galaxyName = galaxy.getName();
            Long galaxyId = galaxy.getId();
            summary.put("galaxyName", galaxyName);
            summary.put("galaxyId", galaxyId);
            summary.put("gameType", galaxy.getGameType() != null ? galaxy.getGameType() : "UNKNOWN");
            summary.put("type", galaxy.getGameType() != null ? galaxy.getGameType() : "UNKNOWN");

            // Always get the real solar system count from the database
            long dbSolarSystemCount = solarSystemRepository.countByGalaxyId(galaxyId);

            // Compute expected max systems early — needed for both event and fallback paths
            int expectedMaxSystems = galaxyService.getExpectedMaxSystems(galaxyName);

            GalaxyEvent event = latestAssessment.get(galaxyName);
            if (event != null) {
                // Galaxy has assessment data — use it for health and AI info
                int healthScoreVal = parsePayloadInt(event, "healthScore", -1);
                summary.put("healthScore", healthScoreVal);
                summary.put("healthStatus", deriveHealthStatus(healthScoreVal));
                summary.put("findingsCount", parsePayloadInt(event, "findingsCount", 0));
                summary.put("recommendationsCount", parsePayloadInt(event, "recommendationsCount", 0));
                summary.put("lastAssessed", event.getTimestamp().toString());
                summary.put("aiAgentCount", parsePayloadInt(event, "aiAgentCount", 0));
            } else {
                // No assessment event yet — get real AI agent count from workforce
                try {
                    GalaxyOverseerService.WorkforceReport workforceFallback = galaxyOverseerService.monitorWorkforce(galaxyId);
                    summary.put("aiAgentCount", workforceFallback.getTotalAiAgents());
                } catch (Exception e) {
                    log.debug("Failed to get workforce for {} (no assessment): {}", galaxyName, e.getMessage());
                    summary.put("aiAgentCount", 0);
                }
                // Derive health from completion using the same two-component formula as GalaxyOverseerService:
                // 50 base + completion bonus (0-50), no finding deductions (no assessment yet)
                if (dbSolarSystemCount > 0) {
                    int completionBonus;
                    if (expectedMaxSystems > 0) {
                        double ratio = (double) dbSolarSystemCount / expectedMaxSystems;
                        completionBonus = Math.min(50, Math.max(1, (int) Math.ceil(ratio * 50)));
                    } else {
                        completionBonus = 50;
                    }
                    int fallbackHealth = 50 + completionBonus;
                    summary.put("healthScore", fallbackHealth);
                    summary.put("healthStatus", deriveHealthStatus(fallbackHealth));
                } else {
                    summary.put("healthScore", 50);
                    summary.put("healthStatus", "WARNING");
                }
                summary.put("findingsCount", 0);
                summary.put("recommendationsCount", 0);
                summary.put("lastAssessed", "not yet assessed");
            }

            // Use real database count for solar systems (not cached event data)
            summary.put("solarSystemCount", (int) dbSolarSystemCount);
            summary.put("expectedMaxSystems", expectedMaxSystems);

            // Use real playable check from GalaxyService
            boolean playable = galaxyService.isGalaxyPlayable(galaxyName);
            summary.put("isPlayable", playable);
            summary.put("playable", playable);

            int completionPercent;
            if (dbSolarSystemCount > 0 && expectedMaxSystems > 0) {
                completionPercent = Math.min(100, (int) ((double) dbSolarSystemCount / expectedMaxSystems * 100));
            } else if (dbSolarSystemCount > 0) {
                completionPercent = 100;
            } else {
                completionPercent = 0;
            }
            summary.put("completionPercent", completionPercent);

            boolean metadataComplete = galaxy.getGameType() != null &&
                    galaxy.getDifficultyLevel() != null && !galaxy.getDifficultyLevel().isBlank() &&
                    galaxy.getTheme() != null && !galaxy.getTheme().isBlank() &&
                    galaxy.getDescription() != null && !galaxy.getDescription().isBlank();
            summary.put("metadataComplete", metadataComplete);

            // Galaxy metadata fields used by the detail view
            summary.put("difficultyLevel", galaxy.getDifficultyLevel() != null ? galaxy.getDifficultyLevel() : "");
            summary.put("theme", galaxy.getTheme() != null ? galaxy.getTheme() : "");
            summary.put("description", galaxy.getDescription() != null ? galaxy.getDescription() : "");

            // Get real build status from BackgroundGalaxyBuilder
            com.yisy.modules.doramcu.service.galaxy.GalaxyBuildStatus buildStatus = backgroundGalaxyBuilder.getBuildStatus(galaxyName);
            if (buildStatus != null) {
                summary.put("buildRunning", buildStatus.isRunning());
                summary.put("buildPhase", buildStatus.getPhase());
                summary.put("buildErrorMessage", buildStatus.getErrorMessage());
                summary.put("buildElapsedMs", buildStatus.getElapsedTimeMs());
            } else {
                summary.put("buildRunning", false);
                summary.put("buildPhase", completionPercent >= 100 ? "Complete" : "Not started");
            }

            // Get real workforce status from GalaxyOverseerService
            try {
                GalaxyOverseerService.WorkforceReport workforce = galaxyOverseerService.monitorWorkforce(galaxyId);
                summary.put("overstaffed", workforce.isOverstaffed());
                summary.put("workforceRecommendation", workforce.getRecommendation());
            } catch (Exception e) {
                log.debug("Failed to get workforce report for {}: {}", galaxyName, e.getMessage());
                summary.put("overstaffed", false);
                summary.put("workforceRecommendation", "");
            }

            // Extract findings breakdown from assessment event if available
            if (event != null) {
                summary.put("criticalFindings", parsePayloadInt(event, "criticalFindings", 0));
                summary.put("highFindings", parsePayloadInt(event, "highFindings", 0));
                summary.put("mediumFindings", parsePayloadInt(event, "mediumFindings", 0));

                // Extract top recommendations from assessment event
                Object topRecObj = event.getPayloadValue("topRecommendations");
                if (topRecObj != null && !topRecObj.toString().isEmpty()) {
                    List<String> topRecs = java.util.Arrays.asList(topRecObj.toString().split(" \\| "));
                    summary.put("topRecommendations", topRecs);
                } else {
                    summary.put("topRecommendations", new java.util.ArrayList<String>());
                }
            } else {
                summary.put("criticalFindings", 0);
                summary.put("highFindings", 0);
                summary.put("mediumFindings", 0);
                summary.put("topRecommendations", new java.util.ArrayList<String>());
            }

            summaries.add(summary);
        }
        return summaries;
    }

    private int parsePayloadInt(GalaxyEvent event, String key, int defaultValue) {
        Object value = event.getPayloadValue(key);
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private String deriveHealthStatus(int healthScore) {
        if (healthScore >= 80) return "HEALTHY";
        if (healthScore >= 50) return "WARNING";
        if (healthScore >= 20) return "CRITICAL";
        return "DEAD";
    }

    /**
     * Convert a GalaxyEvent to a news-friendly map.
     */
    private Map<String, Object> toNewsItem(GalaxyEvent event) {
        Map<String, Object> item = new java.util.HashMap<>();
        item.put("id", event.getTimestamp().toEpochMilli());
        item.put("type", event.getType().name());
        item.put("title", event.getType().formatTitle(event.getGalaxyName()));
        item.put("galaxyName", event.getGalaxyName());
        item.put("timestamp", event.getTimestamp().toString());
        if (!event.getPayload().isEmpty()) {
            item.put("details", event.getPayload());
        }
        return item;
    }
}
