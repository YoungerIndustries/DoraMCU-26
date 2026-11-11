package com.yisy.modules.doramcu.star_trek_4x.exploration;

import com.yisy.modules.doramcu.star_trek_4x.model.GameSession;
import com.yisy.modules.doramcu.star_trek_4x.model.StarSystem;
import com.yisy.modules.doramcu.star_trek_4x.repository.GameSessionRepository;
import com.yisy.modules.doramcu.star_trek_4x.repository.StarSystemRepository;
import com.yisy.modules.doramcu.star_trek_4x.service.StarTrekLoreDatabase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Service for the eXplore pillar: galaxy scanning, system discovery, and hyperlane mapping.
 * <p>
 * Handles scanning sectors to reveal star systems, marking them as discovered,
 * and generating exploration events based on system properties.
 * </p>
 */
@Service
public class ExplorationService {

    private static final Logger log = LoggerFactory.getLogger(ExplorationService.class);
    private static final Random random = new Random();

    private final StarSystemRepository starSystemRepository;
    private final GameSessionRepository gameSessionRepository;
    private final StarTrekLoreDatabase starTrekLoreDatabase;

    public ExplorationService(StarSystemRepository starSystemRepository,
                              GameSessionRepository gameSessionRepository,
                              StarTrekLoreDatabase starTrekLoreDatabase) {
        this.starSystemRepository = starSystemRepository;
        this.gameSessionRepository = gameSessionRepository;
        this.starTrekLoreDatabase = starTrekLoreDatabase;
    }

    /**
     * Scan a sector to discover star systems.
     *
     * @param gameSessionId the game session ID
     * @param sectorId      the sector ID to scan
     * @return list of discovered systems
     */
    public List<StarSystem> scanSector(Long gameSessionId, int sectorId) {
        log.info("Scanning sector {} in game session {}", sectorId, gameSessionId);

        GameSession session = gameSessionRepository.findById(gameSessionId)
                .orElseThrow(() -> new IllegalArgumentException("Game session not found: " + gameSessionId));

        List<StarSystem> systems = starSystemRepository.findBySectorId(sectorId);
        List<StarSystem> discovered = new ArrayList<>();

        for (StarSystem system : systems) {
            if (system.getGameSession().getId().equals(gameSessionId) && !system.isDiscovered()) {
                system.setDiscovered(true);
                discovered.add(system);
                log.info("Discovered system: {} in sector {}", system.getName(), sectorId);
            }
        }

        starSystemRepository.saveAll(discovered);
        return discovered;
    }

    /**
     * Get all discovered systems for a game session.
     *
     * @param gameSessionId the game session ID
     * @return list of discovered systems
     */
    public List<StarSystem> getDiscoveredSystems(Long gameSessionId) {
        return starSystemRepository.findByGameSessionId(gameSessionId)
                .stream()
                .filter(StarSystem::isDiscovered)
                .toList();
    }

    /**
     * Get exploration data for a specific system.
     *
     * @param systemId the system ID
     * @return exploration report
     */
    public Map<String, Object> getExplorationReport(Long systemId) {
        StarSystem system = starSystemRepository.findById(systemId)
                .orElseThrow(() -> new IllegalArgumentException("System not found: " + systemId));

        if (!system.isDiscovered()) {
            throw new IllegalStateException("System not yet discovered: " + systemId);
        }

        return Map.of(
                "systemId", system.getId(),
                "name", system.getName(),
                "sectorId", system.getSectorId(),
                "uwpCode", system.getUwpCode(),
                "alignment", system.getAlignment(),
                "nativeSpecies", system.getNativeSpecies(),
                "hasWarpDeposits", system.isHasWarpDeposits(),
                "notableFeatures", system.getNotableFeatures(),
                "tradeValue", system.getTradeValue(),
                "earth2026PlanetId", system.getEarth2026PlanetId()
        );
    }

    /**
     * Generate exploration events based on system properties.
     *
     * @param system the system to generate events for
     * @return list of exploration event descriptions
     */
    public List<String> generateExplorationEvents(StarSystem system) {
        List<String> events = new ArrayList<>();

        if (system.isHasWarpDeposits()) {
            events.add("Warp core deposits detected! Scientific value: HIGH");
        }

        if (system.getNativeSpecies() != null && !system.getNativeSpecies().isEmpty()) {
            events.add("Native species detected: " + String.join(", ", system.getNativeSpecies()));
        }

        if (system.getNotableFeatures() != null && !system.getNotableFeatures().isEmpty()) {
            events.add("Notable features: " + system.getNotableFeatures());
        }

        // Random exploration events
        int eventRoll = random.nextInt(100);
        if (eventRoll < 10) {
            events.add("Ancient alien ruins detected on surface!");
        } else if (eventRoll < 20) {
            events.add("Distress signal detected from nearby planet!");
        } else if (eventRoll < 30) {
            events.add("Nebula interference affecting sensors!");
        }

        return events;
    }

    /**
     * Calculate the distance between two systems in hex grid.
     *
     * @param from the source system
     * @param to   the destination system
     * @return distance in hex units
     */
    public int calculateDistance(StarSystem from, StarSystem to) {
        int dx = Math.abs(from.getPosX() - to.getPosX());
        int dy = Math.abs(from.getPosY() - to.getPosY());
        int dz = Math.abs(from.getPosZ() - to.getPosZ());
        return Math.max(dx, Math.max(dy, dz));
    }

    /**
     * Check if a system is within sensor range of another system.
     *
     * @param baseSystem     the base system
     * @param targetSystem   the target system
     * @param sensorRange    the sensor range in hex units
     * @return true if within range
     */
    public boolean isWithinSensorRange(StarSystem baseSystem, StarSystem targetSystem, int sensorRange) {
        return calculateDistance(baseSystem, targetSystem) <= sensorRange;
    }
}
