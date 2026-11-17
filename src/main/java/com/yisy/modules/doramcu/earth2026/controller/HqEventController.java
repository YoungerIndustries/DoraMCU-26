package com.yisy.modules.doramcu.earth2026.controller;

import com.yisy.modules.doramcu.earth2026.service.HqEventService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST controller for HQ events.
 * Provides endpoints for viewing, participating in, and managing HQ events.
 */
@RestController
@RequestMapping("/api/v1/earth2026/hq-events")
public class HqEventController {

    private static final Logger log = LoggerFactory.getLogger(HqEventController.class);
    private final HqEventService hqEventService;

    public HqEventController(HqEventService hqEventService) {
        this.hqEventService = hqEventService;
    }

    /**
     * Get all active events for an HQ.
     */
    @GetMapping("/active/{hqId}")
    public ResponseEntity<List<Map<String, Object>>> getActiveEvents(@PathVariable Long hqId) {
        List<Map<String, Object>> events = hqEventService.getActiveEvents(hqId);
        return ResponseEntity.ok(events);
    }

    /**
     * Get completed event history for an HQ.
     */
    @GetMapping("/history/{hqId}")
    public ResponseEntity<List<Map<String, Object>>> getEventHistory(@PathVariable Long hqId) {
        List<Map<String, Object>> events = hqEventService.getEventHistory(hqId);
        // Limit to last 10 completed events
        if (events.size() > 10) {
            return ResponseEntity.ok(events.subList(0, 10));
        }
        return ResponseEntity.ok(events);
    }

    /**
     * Participate in an active event.
     */
    @PostMapping("/participate")
    public ResponseEntity<?> participateInEvent(@RequestBody Map<String, Object> request) {
        Object eventIdObj = request.get("eventId");
        Object corporationIdObj = request.get("corporationId");

        if (eventIdObj == null || corporationIdObj == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "eventId and corporationId are required"));
        }

        Long eventId = ((Number) eventIdObj).longValue();
        Long corporationId = ((Number) corporationIdObj).longValue();

        try {
            Map<String, Object> result = hqEventService.participateInEvent(eventId, corporationId);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Manually complete an active event.
     */
    @PostMapping("/complete/{eventId}")
    public ResponseEntity<?> completeEvent(@PathVariable Long eventId, @RequestBody Map<String, Object> request) {
        Object corporationIdObj = request.get("corporationId");

        if (corporationIdObj == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "corporationId is required"));
        }

        Long corporationId = ((Number) corporationIdObj).longValue();

        try {
            Map<String, Object> result = hqEventService.completeEvent(eventId, corporationId);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Manually trigger event generation (for testing/initial seeding).
     */
    @PostMapping("/generate/{hqId}")
    public ResponseEntity<?> generateEvent(@PathVariable Long hqId) {
        try {
            Object result = hqEventService.tryGenerateEvent(hqId);
            if (result == null) {
                return ResponseEntity.ok(Map.of("message", "No event generated this turn"));
            }
            return ResponseEntity.ok(Map.of("event", result, "message", "Event generated successfully"));
        } catch (Exception e) {
            log.error("Failed to generate event: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
