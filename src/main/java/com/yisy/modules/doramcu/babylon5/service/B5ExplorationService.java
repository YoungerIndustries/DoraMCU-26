package com.yisy.modules.doramcu.babylon5.service;

import com.yisy.modules.doramcu.babylon5.model.B5Location;
import com.yisy.modules.doramcu.fourx.FourxExplorationService;
import com.yisy.modules.doramcu.hex.GalaxyServiceRegistry;
import com.yisy.modules.doramcu.hex.GalaxyTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * B5 implementation of the Exploration 4X pillar.
 * <p>
 * Handles sector scanning, system discovery, and B5-themed exploration events.
 * Integrates with B5 locations for themed discoveries.
 * </p>
 */
@Service
public class B5ExplorationService implements FourxExplorationService, GalaxyServiceRegistry.ServiceProvider {

    private static final Logger log = LoggerFactory.getLogger(B5ExplorationService.class);

    private final Babylon5DatabaseService databaseService;
    private final Map<String, Map<String, Object>> discoveredSystems = new ConcurrentHashMap<>();
    private final Set<String> discoveredSystemIds = ConcurrentHashMap.newKeySet();
    private final AtomicLong systemIdCounter = new AtomicLong(1);
    private final Random random = new Random();

    public B5ExplorationService(Babylon5DatabaseService databaseService) {
        this.databaseService = databaseService;
    }

    @Override
    public GalaxyTemplate getSupportedTemplate() {
        return GalaxyTemplate.BABYLON5;
    }

    @Override
    public List<Map<String, Object>> scanSector(String sectorId) {
        List<Map<String, Object>> results = new ArrayList<>();
        
        // Generate 1-5 systems per sector scan
        int systemCount = random.nextInt(5) + 1;
        for (int i = 0; i < systemCount; i++) {
            String systemId = String.valueOf(systemIdCounter.getAndIncrement());
            Map<String, Object> system = generateSystem(systemId, sectorId);
            results.add(system);
            discoveredSystems.put(systemId, system);
            discoveredSystemIds.add(systemId);
        }

        log.info("Scanned sector {}: discovered {} systems", sectorId, systemCount);
        return results;
    }

    @Override
    public List<Map<String, Object>> getDiscoveredSystems() {
        return new ArrayList<>(discoveredSystems.values());
    }

    @Override
    public Map<String, Object> getExplorationReport(String systemId) {
        return discoveredSystems.get(systemId);
    }

    @Override
    public List<String> generateExplorationEvents(String systemId) {
        List<String> events = new ArrayList<>();
        
        // Check if this system matches a known B5 location
        List<B5Location> locations = databaseService.getAllLocations();
        if (!locations.isEmpty()) {
            B5Location location = locations.get(random.nextInt(locations.size()));
            events.add("Discovered remnants near " + location.getName() + ": " + location.getDescription());
        }

        // Generate random B5-themed events
        String[] eventsPool = {
            "Ancient Vorlon artifact detected in orbit",
            "Shadow mining facility abandoned in asteroid belt",
            "Wormhole fluctuation detected - unstable",
            "Derelict Earth Force vessel drifting",
            "Narn resistance cache found on moon surface",
            "Minbari temple ruins on planetary surface",
            "Centauri pleasure palace in disrepair",
            "Unknown energy signature from gas giant",
            "Distress signal from nearby asteroid",
            "Pirate activity reported in sector",
            "Trade convoy passing through system",
            "Scientific anomaly detected - further study recommended",
            "Abandoned space station with intact life support",
            "Rare mineral deposits discovered",
            "First contact opportunity with unknown species"
        };

        // Add 1-3 random events
        int eventCount = random.nextInt(3) + 1;
        Set<Integer> usedIndices = new HashSet<>();
        for (int i = 0; i < eventCount; i++) {
            int idx;
            do {
                idx = random.nextInt(eventsPool.length);
            } while (usedIndices.contains(idx));
            usedIndices.add(idx);
            events.add(eventsPool[idx]);
        }

        return events;
    }

    @Override
    public int calculateDistance(String fromSystemId, String toSystemId) {
        Map<String, Object> from = discoveredSystems.get(fromSystemId);
        Map<String, Object> to = discoveredSystems.get(toSystemId);
        if (from == null || to == null) return -1;

        // Calculate distance based on sector coordinates
        @SuppressWarnings("unchecked")
        Map<String, Integer> fromCoords = (Map<String, Integer>) from.get("coordinates");
        @SuppressWarnings("unchecked")
        Map<String, Integer> toCoords = (Map<String, Integer>) to.get("coordinates");
        if (fromCoords == null || toCoords == null) return -1;

        int dx = Math.abs(fromCoords.get("x") - toCoords.get("x"));
        int dy = Math.abs(fromCoords.get("y") - toCoords.get("y"));
        // Hex distance approximation
        return Math.max(dx, dy);
    }

    @Override
    public boolean isWithinSensorRange(String baseSystemId, String targetSystemId, int sensorRange) {
        int distance = calculateDistance(baseSystemId, targetSystemId);
        return distance >= 0 && distance <= sensorRange;
    }

    @Override
    public List<String> getAllSectors() {
        // Return unique sector IDs from discovered systems
        Set<String> sectors = new HashSet<>();
        for (Map<String, Object> system : discoveredSystems.values()) {
            Object sector = system.get("sectorId");
            if (sector != null) {
                sectors.add(sector.toString());
            }
        }
        return new ArrayList<>(sectors);
    }

    @Override
    public int getDiscoveredSystemCount() {
        return discoveredSystems.size();
    }

    private Map<String, Object> generateSystem(String systemId, String sectorId) {
        Map<String, Object> system = new LinkedHashMap<>();
        system.put("id", systemId);
        system.put("sectorId", sectorId);

        // Generate coordinates within sector
        Map<String, Integer> coords = new HashMap<>();
        coords.put("x", random.nextInt(10));
        coords.put("y", random.nextInt(10));
        system.put("coordinates", coords);

        // Generate star type
        String[] starTypes = {"Red Dwarf", "Yellow Dwarf", "Blue Giant", "White Dwarf", "Neutron Star", "Binary", "Trinary"};
        system.put("starType", starTypes[random.nextInt(starTypes.length)]);

        // Generate planet count
        int planetCount = random.nextInt(8) + 1;
        system.put("planetCount", planetCount);

        // Generate planets
        List<Map<String, Object>> planets = new ArrayList<>();
        String[] planetTypes = {"Barren", "Desert", "Ocean", "Habitable", "Jungle", "Ice", "Gas Giant", "Asteroid Belt"};
        for (int i = 0; i < planetCount; i++) {
            Map<String, Object> planet = new LinkedHashMap<>();
            planet.put("name", "Planet " + (i + 1));
            planet.put("type", planetTypes[random.nextInt(planetTypes.length)]);
            planet.put("habitable", "Habitable".equals(planetTypes[random.nextInt(planetTypes.length)]));
            planets.add(planet);
        }
        system.put("planets", planets);

        // Resource richness
        system.put("resourceRichness", random.nextInt(100) + 1);

        // Security level
        system.put("securityLevel", random.nextInt(100) + 1);

        return system;
    }
}
