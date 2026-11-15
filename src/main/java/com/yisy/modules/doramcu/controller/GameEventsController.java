package com.yisy.modules.doramcu.controller;

import com.yisy.modules.doramcu.event.GalaxyEvent;
import com.yisy.modules.doramcu.event.GalaxyEventService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;
import java.util.UUID;

/**
 * REST controller for game event streaming.
 * <p>
 * Provides SSE-based real-time event streaming for the corporation dashboard
 * and other game clients. Reuses the GalaxyEventService for event distribution.
 * </p>
 */
@RestController
@RequestMapping("/api/v1/game")
public class GameEventsController {

    private static final Logger log = LoggerFactory.getLogger(GameEventsController.class);

    private final GalaxyEventService galaxyEventService;

    public GameEventsController(GalaxyEventService galaxyEventService) {
        this.galaxyEventService = galaxyEventService;
    }

    /**
     * Subscribe to live game events via Server-Sent Events.
     * <p>
     * Used by the corporation dashboard and other game clients to receive
     * real-time event notifications. The {@code corporation} parameter is
     * accepted for compatibility but all events are broadcast to all subscribers.
     * </p>
     *
     * @param corporation optional flag indicating corporation dashboard client
     * @return SSE emitter streaming game events
     */
    @GetMapping(value = "/events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribeEvents(@RequestParam(required = false, defaultValue = "false") boolean corporation) {
        String clientId = "game-" + UUID.randomUUID().toString();
        log.info("Game events client connected: {} (corporation={})", clientId, corporation);

        SseEmitter emitter = galaxyEventService.subscribeAll(clientId);

        // Send a connected event
        try {
            emitter.send(SseEmitter.event()
                    .name("connected")
                    .data(Map.of(
                            "type", "CORPORATION_CONNECTED",
                            "message", "Connected to game event stream",
                            "severity", "INFO"
                    )));
        } catch (Exception e) {
            log.warn("Failed to send connected event to game client: {}", clientId, e);
        }

        return emitter;
    }
}
