package com.yisy.modules.doramcu.event;
import com.yisy.modules.doramcu.model.players.Player;

/**
 * Enumerates all galaxy lifecycle event types.
 * <p>
 * These events are published by the GalaxyEventService and broadcast via SSE
 * to connected admin dashboard clients. Each event type corresponds to a
 * significant state change in the galaxy system.
 * </p>
 */
public enum GalaxyEventType {

    /**
     * A sector has been generated during background galaxy build.
     * Payload: sectorRow, sectorCol, systemsInSector, priorityLevel
     */
    SECTOR_GENERATED,

    /**
     * Background galaxy build has started.
     * Payload: galaxyName, totalSectors, seed
     */
    BUILD_STARTED,

    /**
     * Background galaxy build has completed successfully.
     * Payload: galaxyName, totalSystemsGenerated, elapsedMs
     */
    BUILD_COMPLETED,

    /**
     * Background galaxy build has failed.
     * Payload: galaxyName, errorMessage, systemsGeneratedBeforeFailure
     */
    BUILD_FAILED,

    /**
     * Galaxy health score has crossed a severity threshold.
     * Payload: galaxyName, healthScore, previousScore, thresholdCrossed (HEALTHY/WARNING/CRITICAL)
     */
    HEALTH_THRESHOLD_CROSSED,

    /**
     * A character from Earth 2026 has transitioned into the galaxy.
     * Payload: characterId, characterName, role, difficulty, sessionId
     */
    CHARACTER_TRANSITIONED,

    /**
     * A work order has been created by the Galaxy Overseer.
     * Payload: galaxyName, orderText, category, severity
     */
    WORK_ORDER_CREATED,

    /**
     * An AI agent has been deployed to the galaxy.
     * Payload: galaxyName, agentId, agentName, role
     */
    AI_AGENT_DEPLOYED,

    /**
     * A new galaxy has been created.
     * Payload: galaxyName, template, seed, systemCount
     */
    GALAXY_CREATED,

    /**
     * Galaxy assessment has been completed.
     * Payload: galaxyName, healthScore, findingsCount, recommendationsCount
     */
    ASSESSMENT_COMPLETED,

    /**
     * A wormhole pair has been created connecting two galaxies.
     * Payload: galaxyA, galaxyB, wormholeIdA, wormholeIdB
     */
    WORMHOLE_CREATED,

    /**
     * A player has traveled through a wormhole to another galaxy.
     * Payload: playerName, fromGalaxy, toGalaxy, crewMoved
     */
    WORMHOLE_TRAVEL,

    /**
     * A wormhole has been toggled (activated or deactivated).
     * Payload: galaxyName, isActive, connectedGalaxyName
     */
    WORMHOLE_TOGGLED,

    // -- Planet building events --

    /**
     * Planet surface generation has started.
     * Payload: planetName, galaxyName, templateName
     */
    PLANET_BUILD_STARTED,

    /**
     * A capital city has been constructed on a planet.
     * Payload: planetName, capitalName, countryName
     */
    CAPITAL_BUILT,

    /**
     * A Younger Industries office has been opened at a location.
     * Payload: planetName, officeName, cityName, services
     */
    OFFICE_OPENED,

    /**
     * A new transport route has been established between two locations.
     * Payload: planetName, fromCity, toCity, vehicleType, travelTime
     */
    TRANSPORT_ROUTE_ESTABLISHED,

    /**
     * A new country/empire has been added to a planet.
     * Payload: planetName, countryName, capitalName, population
     */
    COUNTRY_ADDED,

    /**
     * Planet building has completed successfully.
     * Payload: planetName, totalCapitals, totalOffices, totalRoutes
     */
    PLANET_BUILD_COMPLETED,

    // -- Queued construction events --

    /**
     * Galaxy mapping has started (Galaxy Overlord scanning the galaxy).
     * Payload: galaxyName, systemCount, totalSectors
     */
    GALAXY_MAPPING_STARTED,

    /**
     * Galaxy mapping has completed, build requests created.
     * Payload: galaxyName, buildRequestCount, mappingTimeMs
     */
    GALAXY_MAPPING_COMPLETED,

    /**
     * Build queue processing has started.
     * Payload: galaxyName, totalRequests, priorityOrder
     */
    BUILD_QUEUE_STARTED,

    /**
     * A single build request has started processing.
     * Payload: requestId, objectType, priority, galaxyName
     */
    BUILD_REQUEST_STARTED,

    /**
     * A single build request has completed.
     * Payload: requestId, objectType, buildTimeMs, galaxyName
     */
    BUILD_REQUEST_COMPLETED,

    /**
     * A single build request has failed.
     * Payload: requestId, objectType, errorMessage, galaxyName
     */
    BUILD_REQUEST_FAILED,

    /**
     * Build queue processing has completed (all requests done).
     * Payload: galaxyName, totalCompleted, totalFailed, totalTimeMs
     */
    BUILD_QUEUE_COMPLETED,

    /**
     * A 3D city has been requested for construction.
     * Payload: cityName, planetName, galaxyName, districtCount
     */
    CITY_3D_REQUESTED,

    /**
     * A planet build has been requested (sequential build).
     * Payload: planetName, galaxyName, positionInQueue
     */
    PLANET_BUILD_REQUESTED,

    /**
     * Galaxy generation has started (legacy alias for GALAXY_MAPPING_STARTED).
     * Payload: galaxyName, systemCount
     */
    GALAXY_GENERATION_STARTED,

    /**
     * Galaxy generation has completed (legacy alias for GALAXY_MAPPING_COMPLETED).
     * Payload: galaxyName, buildRequests, mappingTimeMs
     */
    GALAXY_GENERATION_COMPLETED,

    // -- Object lifecycle events --

    /**
     * A galaxy object has been damaged (from conflict or disaster).
     * Payload: galaxyName, objectType, objectId, objectName, state
     */
    OBJECT_DAMAGED,

    /**
     * A galaxy object has been destroyed (from conflict or disaster).
     * Payload: galaxyName, objectType, objectId, objectName, destroyedBy, state
     */
    OBJECT_DESTROYED,

    /**
     * A galaxy object has been rebuilt after destruction.
     * Payload: galaxyName, objectType, objectId, objectName, rebuildCount, state
     */
    OBJECT_REBUILT,

    // -- Battle events --

    /**
     * A battle has been completed (ship-to-ship or ground combat).
     * Payload: attackerName, defenderName, outcome, rounds, attackerShipsLost, defenderShipsLost
     */
    BATTLE_COMPLETED;

    /**
     * Format a human-readable title for this event type with the given galaxy name.
     * <p>
     * Used by the news feed and onboarding controllers to display event titles.
     * </p>
     *
     * @param galaxyName the name of the galaxy (may be null)
     * @return a human-readable title string
     */
    public String formatTitle(String galaxyName) {
        String galaxy = galaxyName != null ? galaxyName : "Unknown";
        return switch (this) {
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
            case PLANET_BUILD_STARTED -> "Planet Build Started: " + galaxy;
            case CAPITAL_BUILT -> "Capital City Constructed on " + galaxy;
            case OFFICE_OPENED -> "Younger Industries Office Opened on " + galaxy;
            case TRANSPORT_ROUTE_ESTABLISHED -> "Transport Route Established on " + galaxy;
            case COUNTRY_ADDED -> "New Country Added to " + galaxy;
            case PLANET_BUILD_COMPLETED -> "Planet Build Complete: " + galaxy;
            case GALAXY_MAPPING_STARTED -> "Galaxy Mapping Started: " + galaxy;
            case GALAXY_MAPPING_COMPLETED -> "Galaxy Mapping Complete: " + galaxy;
            case BUILD_QUEUE_STARTED -> "Build Queue Started: " + galaxy;
            case BUILD_REQUEST_STARTED -> "Build Request Started in " + galaxy;
            case BUILD_REQUEST_COMPLETED -> "Build Request Complete in " + galaxy;
            case BUILD_REQUEST_FAILED -> "Build Request Failed in " + galaxy;
            case BUILD_QUEUE_COMPLETED -> "Build Queue Complete: " + galaxy;
            case CITY_3D_REQUESTED -> "3D City Requested on " + galaxy;
            case PLANET_BUILD_REQUESTED -> "Planet Build Requested in " + galaxy;
            case GALAXY_GENERATION_STARTED -> "Galaxy Generation Started: " + galaxy;
            case GALAXY_GENERATION_COMPLETED -> "Galaxy Generation Complete: " + galaxy;
            case OBJECT_DAMAGED -> "Object Damaged in " + galaxy;
            case OBJECT_DESTROYED -> "Object Destroyed in " + galaxy;
            case OBJECT_REBUILT -> "Object Rebuilt in " + galaxy;
            case BATTLE_COMPLETED -> "Battle Report: " + galaxy;
        };
    }
}
