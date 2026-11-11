package com.yisy.modules.doramcu.event;

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
    WORMHOLE_TOGGLED
}
