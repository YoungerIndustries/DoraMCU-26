package com.yisy.modules.doramcu.sfb.service;

import com.yisy.modules.doramcu.fourx.FourxExplorationService;
import com.yisy.modules.doramcu.hex.GalaxyServiceRegistry;
import com.yisy.modules.doramcu.hex.GalaxyTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SFB implementation of the Exploration 4X pillar.
 * <p>
 * Handles sector scanning, system discovery, exploration events, and distance calculations
 * for SFB galaxies (Alpha Octant, Andromeda, M81).
 * </p>
 */
@Service
@Primary
public class SfbExplorationService implements FourxExplorationService, GalaxyServiceRegistry.ServiceProvider {

    private static final Logger log = LoggerFactory.getLogger(SfbExplorationService.class);

    /** All sectors in the galaxy, keyed by "row,col". */
    private final Map<String, List<Map<String, Object>>> sectors = new ConcurrentHashMap<>();

    /** Discovered systems keyed by system ID. */
    private final Map<String, Map<String, Object>> discoveredSystems = new ConcurrentHashMap<>();

    private final Random random = new Random();

    @PostConstruct
    public void initSectors() {
        // Generate a 10x10 galaxy grid
        for (int row = 0; row < 10; row++) {
            for (int col = 0; col < 10; col++) {
                String sectorId = row + "," + col;
                List<Map<String, Object>> systems = generateSectorSystems(row, col);
                sectors.put(sectorId, systems);
            }
        }
        log.info("Initialized {} sectors in SFB galaxy", sectors.size());
    }

    private List<Map<String, Object>> generateSectorSystems(int row, int col) {
        List<Map<String, Object>> systems = new ArrayList<>();
        int numSystems = random.nextInt(4) + 1; // 1-4 systems per sector

        for (int i = 0; i < numSystems; i++) {
            Map<String, Object> system = new LinkedHashMap<>();
            String systemId = row + "," + col + "-" + i;
            system.put("id", systemId);
            system.put("sectorRow", row);
            system.put("sectorCol", col);
            system.put("name", generateSystemName(row, col, i));
            system.put("starType", randomStarType());
            system.put("planets", random.nextInt(8) + 1);
            system.put("habitablePlanets", random.nextInt(3));
            system.put("resources", randomResourceLevel());
            system.put("discovered", false);
            system.put("explorationEvents", new ArrayList<String>());
            systems.add(system);
        }

        return systems;
    }

    private String generateSystemName(int row, int col, int index) {
        String[] prefixes = {"Alpha", "Beta", "Gamma", "Delta", "Epsilon", "Zeta", "Eta", "Theta",
                "Iota", "Kappa", "Lambda", "Mu", "Nu", "Xi", "Omicron", "Pi", "Rho", "Sigma"};
        String[] suffixes = {"Prime", "Major", "Minor", "Secundus", "Tertius", "Quartus"};

        int prefixIdx = (row * 10 + col * 4 + index) % prefixes.length;
        int suffixIdx = (row + col + index) % suffixes.length;
        return prefixes[prefixIdx] + "-" + (row + 1) + (col + 1) + " " + suffixes[suffixIdx];
    }

    private String randomStarType() {
        String[] types = {"O", "B", "A", "F", "G", "K", "M", "White Dwarf", "Neutron Star", "Red Giant"};
        return types[random.nextInt(types.length)];
    }

    private String randomResourceLevel() {
        String[] levels = {"Poor", "Average", "Rich", "Abundant"};
        return levels[random.nextInt(levels.length)];
    }

    @Override
    public GalaxyTemplate getSupportedTemplate() {
        return GalaxyTemplate.CUSTOM;
    }

    @Override
    public List<Map<String, Object>> scanSector(String sectorId) {
        List<Map<String, Object>> systems = sectors.get(sectorId);
        if (systems == null) {
            log.warn("Sector not found: {}", sectorId);
            return List.of();
        }

        List<Map<String, Object>> discovered = new ArrayList<>();
        for (Map<String, Object> system : systems) {
            system.put("discovered", true);
            String systemId = (String) system.get("id");
            discoveredSystems.put(systemId, system);
            discovered.add(system);
        }

        log.info("Scanned sector {}: discovered {} systems", sectorId, discovered.size());
        return discovered;
    }

    @Override
    public List<Map<String, Object>> getDiscoveredSystems() {
        return new ArrayList<>(discoveredSystems.values());
    }

    @Override
    public Map<String, Object> getExplorationReport(String systemId) {
        Map<String, Object> system = discoveredSystems.get(systemId);
        if (system == null) {
            // Check all sectors for the system
            for (List<Map<String, Object>> sectorSystems : sectors.values()) {
                for (Map<String, Object> s : sectorSystems) {
                    if (s.get("id").equals(systemId)) {
                        return s;
                    }
                }
            }
            return Map.of("error", "System not found: " + systemId);
        }
        return system;
    }

    @Override
    public List<String> generateExplorationEvents(String systemId) {
        List<String> events = new ArrayList<>();
        Map<String, Object> system = discoveredSystems.get(systemId);
        if (system == null) {
            events.add("System not discovered yet. Scan the sector first.");
            return events;
        }

        int numEvents = random.nextInt(3) + 1;
        for (int i = 0; i < numEvents; i++) {
            String event = generateEvent(system);
            events.add(event);
        }

        // Store events in the system
        @SuppressWarnings("unchecked")
        List<String> existingEvents = (List<String>) system.get("explorationEvents");
        if (existingEvents != null) {
            existingEvents.addAll(events);
        }

        log.info("Generated {} exploration events for {}", numEvents, systemId);
        return events;
    }

    private String generateEvent(Map<String, Object> system) {
        String systemName = (String) system.get("name");
        String starType = (String) system.get("starType");

        List<String> possibleEvents = new ArrayList<>();

        // Anomaly events
        possibleEvents.add("Discovered an energy anomaly near " + systemName + ".");
        possibleEvents.add("Sensor readings indicate a subspace rift in " + systemName + ".");
        possibleEvents.add("Unknown signal detected from the " + starType + " star in " + systemName + ".");

        // Planet events
        possibleEvents.add("Scanned a habitable planet in " + systemName + " with signs of ancient ruins.");
        possibleEvents.add("Discovered a gas giant with valuable mineral deposits in " + systemName + ".");
        possibleEvents.add("Found an asteroid belt rich in rare minerals in " + systemName + ".");

        // Faction events
        possibleEvents.add("Detected Klingon patrol ships near " + systemName + ".");
        possibleEvents.add("Romulan cloaking device signature detected near " + systemName + ".");
        possibleEvents.add("Abandoned Tholian web structure found in " + systemName + ".");

        // Hazard events
        possibleEvents.add("Nebula in " + systemName + " causes sensor interference.");
        possibleEvents.add("Asteroid field in " + systemName + " poses navigation hazard.");
        possibleEvents.add("Gravitational anomaly near " + systemName + " distorts space-time.");

        // Discovery events
        possibleEvents.add("Ancient alien artifact discovered in " + systemName + ".");
        possibleEvents.add("Wormhole entrance detected near " + systemName + ".");
        possibleEvents.add("Derelict ship found drifting in " + systemName + ".");

        return possibleEvents.get(random.nextInt(possibleEvents.size()));
    }

    @Override
    public int calculateDistance(String fromSystemId, String toSystemId) {
        Map<String, Object> from = findSystem(fromSystemId);
        Map<String, Object> to = findSystem(toSystemId);

        if (from == null || to == null) {
            return -1;
        }

        int fromRow = (int) from.get("sectorRow");
        int fromCol = (int) from.get("sectorCol");
        int toRow = (int) to.get("sectorRow");
        int toCol = (int) to.get("sectorCol");

        // Hex distance approximation
        return Math.abs(fromRow - toRow) + Math.abs(fromCol - toCol);
    }

    @Override
    public boolean isWithinSensorRange(String baseSystemId, String targetSystemId, int sensorRange) {
        int distance = calculateDistance(baseSystemId, targetSystemId);
        return distance >= 0 && distance <= sensorRange;
    }

    @Override
    public List<String> getAllSectors() {
        return new ArrayList<>(sectors.keySet());
    }

    @Override
    public int getDiscoveredSystemCount() {
        return discoveredSystems.size();
    }

    private Map<String, Object> findSystem(String systemId) {
        // Check discovered systems first
        Map<String, Object> system = discoveredSystems.get(systemId);
        if (system != null) return system;

        // Check all sectors
        for (List<Map<String, Object>> sectorSystems : sectors.values()) {
            for (Map<String, Object> s : sectorSystems) {
                if (s.get("id").equals(systemId)) {
                    return s;
                }
            }
        }
        return null;
    }
}
