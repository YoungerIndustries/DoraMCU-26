package com.yisy.modules.doramcu.fourx;

import java.util.List;
import java.util.Map;

/**
 * Shared interface for the Exploration 4X pillar.
 * <p>
 * Implementing services handle sector scanning, system discovery, exploration events,
 * and distance calculations. Each game module provides its own implementation.
 * </p>
 */
public interface FourxExplorationService {

    /**
     * Scan a sector to discover star systems.
     *
     * @param sectorId the sector ID to scan
     * @return list of discovered systems as exploration reports
     */
    List<Map<String, Object>> scanSector(String sectorId);

    /**
     * Get all discovered systems for a game session.
     *
     * @return list of discovered system reports
     */
    List<Map<String, Object>> getDiscoveredSystems();

    /**
     * Get exploration data for a specific system.
     *
     * @param systemId the system ID
     * @return exploration report
     */
    Map<String, Object> getExplorationReport(String systemId);

    /**
     * Generate exploration events for a system.
     *
     * @param systemId the system ID
     * @return list of exploration event descriptions
     */
    List<String> generateExplorationEvents(String systemId);

    /**
     * Calculate the distance between two systems.
     *
     * @param fromSystemId the source system ID
     * @param toSystemId   the destination system ID
     * @return distance in hex units, or -1 if either system is not found
     */
    int calculateDistance(String fromSystemId, String toSystemId);

    /**
     * Check if a system is within sensor range of another system.
     *
     * @param baseSystemId   the base system ID
     * @param targetSystemId the target system ID
     * @param sensorRange    the sensor range in hex units
     * @return true if within range
     */
    boolean isWithinSensorRange(String baseSystemId, String targetSystemId, int sensorRange);

    /**
     * Get all sectors in the galaxy.
     *
     * @return list of sector IDs
     */
    List<String> getAllSectors();

    /**
     * Get the number of discovered systems.
     *
     * @return count of discovered systems
     */
    int getDiscoveredSystemCount();
}
