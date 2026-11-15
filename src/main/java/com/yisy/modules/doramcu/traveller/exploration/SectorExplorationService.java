package com.yisy.modules.doramcu.traveller.exploration;
import org.springframework.stereotype.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;

import java.time.LocalDateTime;
import java.util.*;
import java.util.Optional;

/**
 * Service for sector exploration in the Traveller module.
 * <p>
 * Provides navigation between hex sectors, planet landings,
 * random encounters, and trade opportunities.
 * </p>
 */
@Service
public class SectorExplorationService implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(SectorExplorationService.class);

    private final StarSystemRepository systemRepository;
    private final PlanetLandingRepository landingRepository;
    private final CharacterPositionRepository positionRepository;

    public SectorExplorationService(StarSystemRepository systemRepository,
                                     PlanetLandingRepository landingRepository,
                                     CharacterPositionRepository positionRepository) {
        this.systemRepository = systemRepository;
        this.landingRepository = landingRepository;
        this.positionRepository = positionRepository;
    }

    /**
     * Get the current hex sector for a character from the database.
     */
    private Optional<String> getPositionHex(String characterId) {
        return positionRepository.findByCharacterId(characterId)
                .map(CharacterPosition::getHexSector);
    }

    /**
     * Save or update a character's current position in the database.
     */
    private void savePosition(String characterId, String hexSector, StarSystem system) {
        CharacterPosition position = positionRepository.findByCharacterId(characterId)
                .orElse(new CharacterPosition(characterId, hexSector));
        position.setHexSector(hexSector);
        if (system != null) {
            position.setSystemName(system.getName());
        }
        position.setUpdatedAt(LocalDateTime.now());
        positionRepository.save(position);
    }

    /**
     * Get the current star system for a character.
     */
    public Optional<StarSystem> getCurrentSystem(String characterId) {
        String hex = getPositionHex(characterId).orElse(null);
        if (hex == null) {
            // Default to first system
            List<StarSystem> systems = systemRepository.findAll();
            if (systems.isEmpty()) return Optional.empty();
            return Optional.of(systems.get(0));
        }
        return Optional.ofNullable(systemRepository.findByHexSector(hex));
    }

    /**
     * Set a character's current position.
     */
    public void setPosition(String characterId, String hexSector) {
        StarSystem system = systemRepository.findByHexSector(hexSector);
        savePosition(characterId, hexSector, system);
    }

    /**
     * Navigate to an adjacent hex sector.
     */
    public NavigationResult navigate(String characterId, String direction) {
        String currentHex = getPositionHex(characterId).orElse(null);
        if (currentHex == null) {
            List<StarSystem> systems = systemRepository.findAll();
            if (systems.isEmpty()) {
                return new NavigationResult(false, "No star systems available");
            }
            currentHex = systems.get(0).getHexSector();
            savePosition(characterId, currentHex, systems.get(0));
        }

        String newHex = computeNeighborHex(currentHex, direction);
        StarSystem system = systemRepository.findByHexSector(newHex);

        if (system == null) {
            // Generate a random system for unexplored hex
            system = generateRandomSystem(newHex);
            systemRepository.save(system);
        }

        savePosition(characterId, newHex, system);
        log.info("Character {} navigated from {} to {} ({})", characterId, currentHex, newHex, system.getName());

        // Check for hazards
        List<String> hazards = system.getHazardList();
        if (!hazards.isEmpty() && new Random().nextInt(100) < 30) {
            String hazard = hazards.get(new Random().nextInt(hazards.size()));
            return new NavigationResult(true, "Navigated to " + system.getName() + ". Hazard detected: " + hazard, system, hazard);
        }

        return new NavigationResult(true, "Navigated to " + system.getName(), system, null);
    }

    /**
     * Attempt a planet landing.
     */
    public PlanetLanding attemptLanding(String characterId) {
        String currentHex = getPositionHex(characterId).orElse(null);
        if (currentHex == null) {
            throw new IllegalArgumentException("Not in a valid star system");
        }
        StarSystem system = systemRepository.findByHexSector(currentHex);
        if (system == null) {
            throw new IllegalArgumentException("Not in a valid star system");
        }

        Random rand = new Random();
        PlanetLanding landing = new PlanetLanding();
        landing.setCharacterId(characterId);
        landing.setSystemName(system.getName());
        landing.setHexSector(system.getHexSector());
        landing.setPlanetName(system.getName() + "-P" + system.getPrimaryPlanet());
        landing.setTimestamp(LocalDateTime.now());

        // Landing roll based on starport class
        int roll = rand.nextInt(12);
        int difficulty = 6; // Average starport
        if (system.getStarports() != null && !system.getStarports().isEmpty()) {
            String firstPort = system.getStarports().split(",")[0].trim();
            if (firstPort.equals("A")) difficulty = 3;
            else if (firstPort.equals("B")) difficulty = 4;
            else if (firstPort.equals("C")) difficulty = 5;
            else if (firstPort.equals("X")) difficulty = 8;
        }

        if (roll >= difficulty) {
            landing.setOutcome("SUCCESS");
            landing.setDescription("Successful landing at " + system.getName());
            landing.setXpEarned(10);
        } else if (roll >= difficulty - 3) {
            landing.setOutcome("ACCIDENT");
            landing.setDescription("Minor accident during landing at " + system.getName());
            landing.setCreditsSpent(rand.nextInt(1000) + 500);
            landing.setXpEarned(5);
        } else {
            landing.setOutcome("FAILURE");
            landing.setDescription("Failed to land at " + system.getName() + ". Must try again.");
            landing.setCreditsSpent(rand.nextInt(500));
        }

        // Random encounter chance
        if (rand.nextInt(100) < 20) {
            landing.setOutcome("ENCOUNTER");
            landing.setDescription("Encounter during landing at " + system.getName() + ": " + randomEncounter());
            landing.setXpEarned(rand.nextInt(50) + 20);
        }

        return landingRepository.save(landing);
    }

    /**
     * Get landing history for a character.
     */
    public List<PlanetLanding> getLandingHistory(String characterId) {
        return landingRepository.findByCharacterIdOrderByTimestampDesc(characterId);
    }

    /**
     * Get available trade goods at current system.
     */
    public List<String> getTradeGoods(String characterId) {
        String currentHex = getPositionHex(characterId).orElse(null);
        if (currentHex == null) return Collections.emptyList();
        StarSystem system = systemRepository.findByHexSector(currentHex);
        if (system == null) return Collections.emptyList();
        return system.getCommodityList();
    }

    /**
     * Get all star systems.
     */
    public List<StarSystem> getAllSystems() {
        return systemRepository.findAll();
    }

    private String computeNeighborHex(String hex, String direction) {
        if (hex.length() < 4) return hex;
        int sectorX = Integer.parseInt(hex.substring(0, 2));
        int sectorY = Integer.parseInt(hex.substring(2, 4));
        switch (direction != null ? direction.toLowerCase() : "") {
            case "n": case "north": sectorY++; break;
            case "s": case "south": sectorY--; break;
            case "e": case "east": sectorX++; break;
            case "w": case "west": sectorX--; break;
            case "ne": case "northeast": case "northeasts": sectorX++; sectorY++; break;
            case "nw": case "northwest": sectorX--; sectorY++; break;
            case "se": case "southeast": sectorX++; sectorY--; break;
            case "sw": case "southwest": sectorX--; sectorY--; break;
            default: // random direction
                String[] dirs = {"N", "NE", "SE", "S", "SW", "NW"};
                direction = dirs[new Random().nextInt(dirs.length)];
                return computeNeighborHex(hex, direction);
        }
        sectorX = Math.max(0, Math.min(99, sectorX));
        sectorY = Math.max(0, Math.min(99, sectorY));
        return String.format("%02d%02d", sectorX, sectorY);
    }

    private String randomEncounter() {
        String[] encounters = {
            "Pirate raiders spotted!",
            "Imperial customs inspection",
            "Distress signal from nearby ship",
            "Space station offers trade deal",
            "Navy patrol requests documents",
            "Abandoned derelict ship found",
            "Smuggler offers contraband deal",
            "Alien contact detected"
        };
        return encounters[new Random().nextInt(encounters.length)];
    }

    private StarSystem generateRandomSystem(String hexSector) {
        Random rand = new Random(hexSector.hashCode());
        StarSystem system = new StarSystem("System-" + hexSector, hexSector);

        String[] starTypes = {"M0", "M2", "M5", "K0", "K5", "G0", "G2", "F0", "A0"};
        system.setStarType(starTypes[rand.nextInt(starTypes.length)]);
        system.setPlanetCount(rand.nextInt(8) + 1);
        system.setPrimaryPlanet(rand.nextInt(system.getPlanetCount()));
        system.setPopulation(rand.nextInt(10));
        system.setWealth(rand.nextInt(10));
        system.setTechLevel(rand.nextInt(10) + 6);

        String[] governments = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "I", "K", "L", "R", "T", "X"};
        system.setGovernment(governments[rand.nextInt(governments.length)]);

        String[] laws = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A"};
        system.setLawLevel(laws[rand.nextInt(laws.length)]);

        String[] ports = {"A", "B", "C", "D", "E", "X"};
        system.setStarports(ports[rand.nextInt(ports.length)]);

        String[] commodities = {"Food", "Electronics", "Luxuries", "Machines", "Medicines", "Metals", "Textiles", "Chemicals"};
        List<String> available = new ArrayList<>();
        for (int i = 0; i < rand.nextInt(4) + 1; i++) {
            available.add(commodities[rand.nextInt(commodities.length)]);
        }
        system.setCommodities(String.join(",", available));

        String[] hazards = {"Rift", "Cloud", "Nebula", "Asteroid Field", "None"};
        system.setHazards(hazards[rand.nextInt(hazards.length)]);

        return system;
    }

    /**
     * Navigation result DTO.
     */
    public static class NavigationResult {
        private final boolean success;
        private final String message;
        private final StarSystem system;
        private final String hazard;

        public NavigationResult(boolean success, String message) {
            this(success, message, null, null);
        }

        public NavigationResult(boolean success, String message, StarSystem system, String hazard) {
            this.success = success;
            this.message = message;
            this.system = system;
            this.hazard = hazard;
        }

        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public StarSystem getSystem() { return system; }
        public String getHazard() { return hazard; }
    }

    @Override
    public void run(String... args) {
        if (systemRepository.count() > 0) {
            log.info("Star systems already initialized with {} systems", systemRepository.count());
            return;
        }

        log.info("Initializing default star systems...");

        // Add Sol system
        StarSystem sol = new StarSystem("Sol", "1717");
        sol.setStarType("G2");
        sol.setPlanetCount(9);
        sol.setPrimaryPlanet(3);
        sol.setPopulation(8);
        sol.setWealth(9);
        sol.setTechLevel(12);
        sol.setGovernment("7");
        sol.setLawLevel("5");
        sol.setStarports("A");
        sol.setCommodities("Food,Electronics,Luxuries,Machines,Medicines");
        sol.setHazards("None");
        systemRepository.save(sol);

        // Add a few more systems
        String[][] systems = {
            {"Rokuga", "1716", "K5", "6", "4", "7", "8", "10", "5", "4", "B", "Food,Chemicals"},
            {"Troll", "1718", "M0", "3", "1", "3", "3", "8", "2", "6", "C", "Metals,Textiles"},
            {"Sectoid", "1617", "F0", "5", "2", "5", "5", "9", "6", "5", "A", "Electronics,Luxuries"},
            {"Kafai", "1817", "M5", "4", "0", "4", "4", "7", "3", "7", "B", "Food,Metals"},
        };

        for (String[] s : systems) {
            StarSystem sys = new StarSystem(s[0], s[1]);
            sys.setStarType(s[2]);
            sys.setPlanetCount(Integer.parseInt(s[3]));
            sys.setPrimaryPlanet(Integer.parseInt(s[4]));
            sys.setPopulation(Integer.parseInt(s[5]));
            sys.setWealth(Integer.parseInt(s[6]));
            sys.setTechLevel(Integer.parseInt(s[7]));
            sys.setGovernment(s[8]);
            sys.setLawLevel(s[9]);
            sys.setStarports(s[10]);
            sys.setCommodities(s[11]);
            sys.setHazards("None");
            systemRepository.save(sys);
        }

        log.info("Initialized {} default star systems", systemRepository.count());
    }
}
