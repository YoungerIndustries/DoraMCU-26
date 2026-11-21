package com.yisy.modules.doramcu.hex.planet.controller;

import com.yisy.modules.doramcu.hex.planet.PlanetEventManager;
import com.yisy.modules.doramcu.hex.planet.PlanetEventType;
import com.yisy.modules.doramcu.hex.planet.PlanetEvent;
import com.yisy.modules.doramcu.hex.planet.DefaultPlanetSurfaceBuilder;
import com.yisy.modules.doramcu.hex.planet.dto.PlanetEventDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.*;

/**
 * REST controller for planet events: history queries and real-time SSE streaming.
 * Extracted from HexPlanetSurfaceController to reduce god class size.
 */
@RestController
@RequestMapping("/api/v1/planets")
public class HexPlanetEventController {

    private static final Logger log = LoggerFactory.getLogger(HexPlanetEventController.class);

    private final DefaultPlanetSurfaceBuilder planetSurfaceBuilder;

    public HexPlanetEventController(DefaultPlanetSurfaceBuilder planetSurfaceBuilder) {
        this.planetSurfaceBuilder = planetSurfaceBuilder;
    }

    /**
     * Get event history for a planet.
     * <p>
     * Returns past planet events, optionally filtered by type, with pagination.
     * </p>
     *
     * @param planetId the planet ID
     * @param type     optional event type filter
     * @param limit    max number of events to return (default 100, max 100)
     * @param offset   pagination offset (default 0)
     * @return list of event DTOs
     */
    @GetMapping("/{planetId}/events")
    public ResponseEntity<List<PlanetEventDto>> getEvents(
            @PathVariable int planetId,
            @RequestParam(required = false) String type,
            @RequestParam(defaultValue = "100") int limit,
            @RequestParam(defaultValue = "0") int offset) {
        log.info("GET /api/v1/planets/{}/events type={} limit={} offset={}", planetId, type, limit, offset);

        PlanetEventManager eventManager = planetSurfaceBuilder.getEventManager();
        if (eventManager == null) {
            return ResponseEntity.ok(Collections.emptyList());
        }

        List<PlanetEvent> events;
        if (type != null && !type.isEmpty()) {
            try {
                PlanetEventType eventType = PlanetEventType.valueOf(type);
                events = eventManager.getEventsByType(eventType);
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().build();
            }
        } else {
            events = eventManager.getEventHistory();
        }

        int maxLimit = 100;
        limit = Math.min(limit, maxLimit);
        int fromIndex = Math.min(offset, events.size());
        int toIndex = Math.min(offset + limit, events.size());

        List<PlanetEventDto> dtos = new ArrayList<>();
        for (int i = fromIndex; i < toIndex; i++) {
            dtos.add(toEventDto(events.get(i)));
        }

        return ResponseEntity.ok(dtos);
    }

    /**
     * Stream real-time planet events via Server-Sent Events.
     * <p>
     * The connection stays open until the client disconnects. Each event is
     * pushed as it is dispatched by the PlanetEventManager.
     * </p>
     *
     * @param planetId the planet ID
     * @return SSE emitter for real-time event streaming
     */
    @GetMapping(value = "/{planetId}/events/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamEvents(@PathVariable int planetId) {
        log.info("GET /api/v1/planets/{}/events/stream", planetId);

        SseEmitter emitter = new SseEmitter(0L); // no timeout
        PlanetEventManager eventManager = planetSurfaceBuilder.getEventManager();

        if (eventManager != null) {
            java.util.function.Consumer<PlanetEvent> listener = (event) -> {
                try {
                    PlanetEventDto dto = toEventDto(event);
                    emitter.send(SseEmitter.event()
                            .name("planet-event")
                            .data(dto));
                } catch (Exception e) {
                    log.debug("SSE send failed for planet {}: {}", planetId, e.getMessage());
                    emitter.complete();
                }
            };
            eventManager.registerGlobalListener(listener);

            emitter.onCompletion(() -> eventManager.unregisterGlobalListener(listener));
            emitter.onTimeout(() -> eventManager.unregisterGlobalListener(listener));
            emitter.onError((e) -> eventManager.unregisterGlobalListener(listener));
        }

        return emitter;
    }

    private PlanetEventDto toEventDto(PlanetEvent event) {
        return new PlanetEventDto(
                event.getType().name(),
                event.getTimestamp(),
                event.getSourceId(),
                event.getPayload()
        );
    }
}
