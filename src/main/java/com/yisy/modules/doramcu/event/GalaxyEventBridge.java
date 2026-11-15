package com.yisy.modules.doramcu.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.yisy.modules.doramcu.controller.FleetTrackerWebSocketHandler;
import com.yisy.modules.doramcu.controller.NarrativeWebSocketHandler;
import com.yisy.modules.doramcu.controller.ResourcePipelineWebSocketHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;

/**
 * Bridge service that connects GalaxyEventService SSE events to WebSocket
 * handlers for real-time 3D visualization.
 * <p>
 * When galaxy events are published via GalaxyEventService, this bridge
 * converts them to WebSocket messages and forwards them to the appropriate
 * handlers (FleetTracker, Narrative, ResourcePipeline) based on event type.
 * </p>
 * <p>
 * Event type mappings:
 * <ul>
 *   <li>BATTLE_COMPLETED, OBJECT_DAMAGED, OBJECT_DESTROYED, OBJECT_REBUILT → FleetTracker</li>
 *   <li>WORMHOLE_CREATED, WORMHOLE_TRAVEL, WORMHOLE_TOGGLED → Narrative (as narrative events)</li>
 *   <li>PLANET_BUILD_*, CAPITAL_BUILT, OFFICE_OPENED, COUNTRY_ADDED → Narrative</li>
 *   <li>TRANSPORT_ROUTE_ESTABLISHED → ResourcePipeline</li>
 *   <li>BUILD_*, GALAXY_*, SECTOR_GENERATED → Narrative (as system events)</li>
 *   <li>CHARACTER_TRANSITIONED, AI_AGENT_DEPLOYED → Narrative</li>
 *   <li>HEALTH_THRESHOLD_CROSSED, ASSESSMENT_COMPLETED → Narrative</li>
 *   <li>WORK_ORDER_CREATED → Narrative</li>
 * </ul>
 * </p>
 */
@Service
public class GalaxyEventBridge {

    private static final Logger log = LoggerFactory.getLogger(GalaxyEventBridge.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired(required = false)
    @SuppressWarnings("SpringAutowiredFieldsWarningInspection")
    private FleetTrackerWebSocketHandler fleetTrackerHandler;

    @Autowired(required = false)
    @SuppressWarnings("SpringAutowiredFieldsWarningInspection")
    private NarrativeWebSocketHandler narrativeHandler;

    @Autowired(required = false)
    @SuppressWarnings("SpringAutowiredFieldsWarningInspection")
    private ResourcePipelineWebSocketHandler resourcePipelineHandler;

    @Autowired
    private GalaxyEventService galaxyEventService;

    /**
     * Called by GalaxyEventService when a new event is published.
     * Routes the event to the appropriate WebSocket handler.
     *
     * @param event the galaxy event to route
     */
    public void onEvent(GalaxyEvent event) {
        try {
            GalaxyEventType type = event.getType();

            // Route based on event type
            switch (type) {
                // Battle and object lifecycle events → FleetTracker
                case BATTLE_COMPLETED:
                case OBJECT_DAMAGED:
                case OBJECT_DESTROYED:
                case OBJECT_REBUILT:
                    forwardToFleetTracker(event);
                    break;

                // Transport route events → ResourcePipeline
                case TRANSPORT_ROUTE_ESTABLISHED:
                    forwardToResourcePipeline(event);
                    break;

                // All other events → Narrative (as narrative events)
                default:
                    forwardToNarrative(event);
                    break;
            }

            log.debug("Routed event {} to WebSocket handler(s)", type);
        } catch (Exception e) {
            log.error("Failed to route galaxy event: {}", event.getType(), e);
        }
    }

    /**
     * Forwards battle/object events to FleetTracker WebSocket handler.
     */
    private void forwardToFleetTracker(GalaxyEvent event) {
        if (fleetTrackerHandler == null) {
            log.debug("FleetTracker handler not available, skipping event: {}", event.getType());
            return;
        }

        ObjectNode message = objectMapper.createObjectNode();
        message.put("type", mapToFleetTrackerType(event.getType()));
        message.put("galaxyName", event.getGalaxyName());
        message.put("timestamp", event.getTimestamp().toString());

        // Add payload data
        if (!event.getPayload().isEmpty()) {
            ObjectNode payload = objectMapper.createObjectNode();
            event.getPayload().forEach((k, v) -> {
                if (v instanceof String) {
                    payload.put(k, (String) v);
                } else if (v instanceof Number) {
                    payload.put(k, ((Number) v).doubleValue());
                } else {
                    payload.put(k, String.valueOf(v));
                }
            });
            message.set("data", payload);
        }

        // Broadcast to all connected fleet tracker sessions
        fleetTrackerHandler.broadcastGalaxyEvent(message);
    }

    /**
     * Forwards transport route events to ResourcePipeline WebSocket handler.
     */
    private void forwardToResourcePipeline(GalaxyEvent event) {
        if (resourcePipelineHandler == null) {
            log.debug("ResourcePipeline handler not available, skipping event: {}", event.getType());
            return;
        }

        ObjectNode message = objectMapper.createObjectNode();
        message.put("type", "route_event");
        message.put("galaxyName", event.getGalaxyName());
        message.put("timestamp", event.getTimestamp().toString());

        // Add payload data
        if (!event.getPayload().isEmpty()) {
            ObjectNode payload = objectMapper.createObjectNode();
            event.getPayload().forEach((k, v) -> {
                if (v instanceof String) {
                    payload.put(k, (String) v);
                } else if (v instanceof Number) {
                    payload.put(k, ((Number) v).doubleValue());
                } else {
                    payload.put(k, String.valueOf(v));
                }
            });
            message.set("data", payload);
        }

        resourcePipelineHandler.broadcastGalaxyEvent(message);
    }

    /**
     * Forwards narrative events to Narrative WebSocket handler.
     */
    private void forwardToNarrative(GalaxyEvent event) {
        if (narrativeHandler == null) {
            log.debug("Narrative handler not available, skipping event: {}", event.getType());
            return;
        }

        ObjectNode message = objectMapper.createObjectNode();
        message.put("type", "galaxy_event");
        message.put("eventType", event.getType().name());
        message.put("title", event.getType().formatTitle(event.getGalaxyName()));
        message.put("galaxyName", event.getGalaxyName());
        message.put("timestamp", event.getTimestamp().toString());

        // Map event type to narrative style
        message.put("style", mapToNarrativeStyle(event.getType()));

        // Add payload data
        if (!event.getPayload().isEmpty()) {
            ObjectNode payload = objectMapper.createObjectNode();
            event.getPayload().forEach((k, v) -> {
                if (v instanceof String) {
                    payload.put(k, (String) v);
                } else if (v instanceof Number) {
                    payload.put(k, ((Number) v).doubleValue());
                } else {
                    payload.put(k, String.valueOf(v));
                }
            });
            message.set("data", payload);
        }

        narrativeHandler.broadcastGalaxyEvent(message);
    }

    /**
     * Maps GalaxyEventType to FleetTracker message type.
     */
    private String mapToFleetTrackerType(GalaxyEventType type) {
        return switch (type) {
            case BATTLE_COMPLETED -> "fleet_combat";
            case OBJECT_DAMAGED -> "fleet_update";
            case OBJECT_DESTROYED -> "fleet_destroyed";
            case OBJECT_REBUILT -> "fleet_created";
            default -> "fleet_update";
        };
    }

    /**
     * Maps GalaxyEventType to narrative style for 3D visualization.
     */
    private String mapToNarrativeStyle(GalaxyEventType type) {
        return switch (type) {
            case WORMHOLE_CREATED, WORMHOLE_TRAVEL, WORMHOLE_TOGGLED -> "anomaly";
            case PLANET_BUILD_STARTED, PLANET_BUILD_COMPLETED, CAPITAL_BUILT,
                 OFFICE_OPENED, COUNTRY_ADDED, TRANSPORT_ROUTE_ESTABLISHED -> "discovery";
            case BUILD_STARTED, BUILD_COMPLETED, BUILD_FAILED, SECTOR_GENERATED,
                 GALAXY_CREATED, GALAXY_MAPPING_STARTED, GALAXY_MAPPING_COMPLETED,
                 GALAXY_GENERATION_STARTED, GALAXY_GENERATION_COMPLETED -> "exploration";
            case BUILD_QUEUE_STARTED, BUILD_REQUEST_STARTED, BUILD_REQUEST_COMPLETED,
                 BUILD_REQUEST_FAILED, BUILD_QUEUE_COMPLETED, CITY_3D_REQUESTED,
                 PLANET_BUILD_REQUESTED -> "exploration";
            case CHARACTER_TRANSITIONED, AI_AGENT_DEPLOYED -> "mystery";
            case HEALTH_THRESHOLD_CROSSED -> "crisis";
            case ASSESSMENT_COMPLETED -> "diplomatic";
            case WORK_ORDER_CREATED -> "personal";
            case OBJECT_DAMAGED, OBJECT_DESTROYED, OBJECT_REBUILT -> "battle";
            case BATTLE_COMPLETED -> "battle";
            default -> "discovery";
        };
    }

    /**
     * Gets the recent event history for a galaxy, converted to WebSocket message format.
     *
     * @param galaxyName the galaxy name (null for all galaxies)
     * @return array of event messages
     */
    public ArrayNode getRecentEventsAsMessages(String galaxyName) {
        ArrayNode messages = objectMapper.createArrayNode();

        for (GalaxyEvent event : galaxyEventService.getEventHistory(galaxyName)) {
            ObjectNode message = objectMapper.createObjectNode();
            message.put("type", "galaxy_event");
            message.put("eventType", event.getType().name());
            message.put("title", event.getType().formatTitle(event.getGalaxyName()));
            message.put("galaxyName", event.getGalaxyName());
            message.put("timestamp", event.getTimestamp().toString());
            message.put("style", mapToNarrativeStyle(event.getType()));

            if (!event.getPayload().isEmpty()) {
                ObjectNode payload = objectMapper.createObjectNode();
                event.getPayload().forEach((k, v) -> {
                    if (v instanceof String) {
                        payload.put(k, (String) v);
                    } else if (v instanceof Number) {
                        payload.put(k, ((Number) v).doubleValue());
                    } else {
                        payload.put(k, String.valueOf(v));
                    }
                });
                message.set("data", payload);
            }

            messages.add(message);
        }

        return messages;
    }
}
