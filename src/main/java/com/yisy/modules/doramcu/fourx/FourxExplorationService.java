package com.yisy.modules.doramcu.fourx;

import java.util.List;
import java.util.Map;

/**
 * Interface for 4X exploration mechanics.
 * Implemented by game-specific exploration services (B5, CaptainsLog, etc.).
 */
public interface FourxExplorationService {

    List<Map<String, Object>> scanSector(String sectorId);

    List<Map<String, Object>> getDiscoveredSystems();

    Map<String, Object> getExplorationReport(String systemId);

    List<String> generateExplorationEvents(String systemId);

    int calculateDistance(String fromSystemId, String toSystemId);

    boolean isWithinSensorRange(String baseSystemId, String targetSystemId, int sensorRange);

    List<String> getAllSectors();

    int getDiscoveredSystemCount();
}
