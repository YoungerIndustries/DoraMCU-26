package com.yisy.modules.doramcu.service.sector;
import com.yisy.modules.doramcu.model.misc.Planet;
import com.yisy.modules.doramcu.dto.sector.SectorRequest;
import com.yisy.modules.doramcu.dto.sector.SectorResponse;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service layer for sector and world generation.
 * <p>
 * Manages Traveller sectors: generating random sectors, querying worlds,
 * and managing hyperlanes.
 * </p>
 */
@Service
public class SectorService {

    private static final Logger log = LoggerFactory.getLogger(SectorService.class);

    private final Map<Integer, TravellerSector> sectors = new HashMap<>();
    private final Map<Integer, TravellerWorld> allWorlds = new HashMap<>();
    private final AtomicInteger sectorIdCounter = new AtomicInteger(1);

    @PostConstruct
    public void init() {
        log.info("Initializing SectorService with seed sectors...");
        generateSeedSectors();
        log.info("SectorService initialized with {} seed sectors", sectors.size());
    }

    /**
     * Generates seed sectors including the Earth solar system and other key sectors.
     */
    private void generateSeedSectors() {
        Random random = new Random(42);
        SectorGenerator generator = new SectorGenerator(random);

        // Generate the Core Sphere sectors (Traveller's Core Sphere)
        String[] coreSphereSectors = {
            "Albion", "Argo", "Core", "Deneb", "Far Space",
            "Flaming Sword", "Gor", "Lone Mountain", "Magellan", "Mayen",
            "Olin", "Rokuga", "Sparta", "Spinward Marches", "Third Pride",
            "Vland", "Witch Space", "Yukon", "Zoo"
        };

        // Generate Core Sphere sectors
        for (String sectorName : coreSphereSectors) {
            int sectorId = sectorIdCounter.getAndIncrement();
            TravellerSector sector = generator.generate(sectorName, sectorId);
            sectors.put(sectorId, sector);
            for (TravellerWorld world : sector.getAllWorlds()) {
                allWorlds.put(world.getId(), world);
            }
        }
        log.info("Generated {} Core Sphere sectors", coreSphereSectors.length);

        // Generate the Sol System (Earth's Solar System) - real planets, not random
        int solSectorId = sectorIdCounter.getAndIncrement();
        TravellerSector solSector = new TravellerSector(solSectorId, "Sol");
        int worldIdCounter = 10000; // Start Sol system worlds at ID 10000

        // Sun - G2V yellow dwarf star
        TravellerWorld sun = new TravellerWorld(
            worldIdCounter++, "Sol (Sun)", UniversalWorldProfile.parse("00000000"), 5, 5, false, false
        );
        solSector.addWorld(sun);

        // Mercury - small, no atmosphere, no population
        TravellerWorld mercury = new TravellerWorld(
            worldIdCounter++, "Mercury", UniversalWorldProfile.parse("00000000"), 3, 4, false, false
        );
        solSector.addWorld(mercury);

        // Venus - thick toxic atmosphere, no population
        TravellerWorld venus = new TravellerWorld(
            worldIdCounter++, "Venus", UniversalWorldProfile.parse("38000000"), 4, 3, false, false
        );
        solSector.addWorld(venus);

        // Earth - our home world, terran, breathable, 1G, high population, democratic
        TravellerWorld earth = new TravellerWorld(
            worldIdCounter++, "Earth", UniversalWorldProfile.parse("34565432"), 5, 5, true, true
        );
        solSector.addWorld(earth);

        // Mars - thin atmosphere, low gravity, some population
        TravellerWorld mars = new TravellerWorld(
            worldIdCounter++, "Mars", UniversalWorldProfile.parse("23443210"), 6, 6, false, false
        );
        solSector.addWorld(mars);

        // Jupiter - gas giant
        TravellerWorld jupiter = new TravellerWorld(
            worldIdCounter++, "Jupiter", UniversalWorldProfile.parse("A0000000"), 7, 7, false, false
        );
        solSector.addWorld(jupiter);

        // Saturn - gas giant with rings
        TravellerWorld saturn = new TravellerWorld(
            worldIdCounter++, "Saturn", UniversalWorldProfile.parse("A0000000"), 8, 8, false, false
        );
        solSector.addWorld(saturn);

        // Uranus - ice giant
        TravellerWorld uranus = new TravellerWorld(
            worldIdCounter++, "Uranus", UniversalWorldProfile.parse("90000000"), 3, 7, false, false
        );
        solSector.addWorld(uranus);

        // Neptune - ice giant
        TravellerWorld neptune = new TravellerWorld(
            worldIdCounter++, "Neptune", UniversalWorldProfile.parse("90000000"), 2, 8, false, false
        );
        solSector.addWorld(neptune);

        // Pluto - dwarf planet
        TravellerWorld pluto = new TravellerWorld(
            worldIdCounter++, "Pluto", UniversalWorldProfile.parse("00000000"), 1, 9, false, false
        );
        solSector.addWorld(pluto);

        // Add hyperlanes between adjacent planets
        TravellerWorld[] solWorlds = {sun, mercury, venus, earth, mars, jupiter, saturn, uranus, neptune, pluto};
        for (int i = 0; i < solWorlds.length - 1; i++) {
            solSector.addHyperlane(solWorlds[i], solWorlds[i + 1]);
        }

        sectors.put(solSectorId, solSector);
        for (TravellerWorld world : solSector.getAllWorlds()) {
            allWorlds.put(world.getId(), world);
        }
        log.info("Generated Sol System with {} real planets (Earth as capital)", solSector.getWorldCount());

        // Generate additional sectors for the galaxy map
        String[] additionalSectors = {
            "Crucis", "Hypatia", "Krescent", "Lagrange", "Mongrels",
            "Nikopol", "Oltua", "Rozzz", "Segin", "Tauri",
            "Urantia", "Vargr", "Wyrden", "Xincob", "Yarman",
            "Zenu", "Alducard", "Bellerophon", "Cataleya", "Denebola"
        };

        for (String sectorName : additionalSectors) {
            int sectorId = sectorIdCounter.getAndIncrement();
            TravellerSector sector = generator.generate(sectorName, sectorId);
            sectors.put(sectorId, sector);
            for (TravellerWorld world : sector.getAllWorlds()) {
                allWorlds.put(world.getId(), world);
            }
        }
        log.info("Generated {} additional sectors", additionalSectors.length);
    }

    /**
     * Generates a new random sector.
     */
    public SectorResponse generateSector(SectorRequest request) {
        log.info("Generating sector: {} with {} worlds",
                request.getSectorName(), request.getWorldCount());

        int sectorId = sectorIdCounter.getAndIncrement();
        Random random = request.getSeed() != null ?
                new Random(request.getSeed()) : new Random();

        SectorGenerator generator = new SectorGenerator(random);

        TravellerSector sector = generator.generate(request.getSectorName(), sectorId);

        // Store sector and worlds
        sectors.put(sectorId, sector);
        for (TravellerWorld world : sector.getAllWorlds()) {
            allWorlds.put(world.getId(), world);
        }

        return toSectorResponse(sector);
    }

    /**
     * Gets a sector by ID.
     */
    public SectorResponse getSector(int sectorId) {
        TravellerSector sector = sectors.get(sectorId);
        if (sector == null) {
            throw new IllegalArgumentException("Sector not found: " + sectorId);
        }
        return toSectorResponse(sector);
    }

    /**
     * Gets a world by ID.
     */
    public SectorResponse getWorld(int worldId) {
        TravellerWorld world = allWorlds.get(worldId);
        if (world == null) {
            throw new IllegalArgumentException("World not found: " + worldId);
        }
        return toWorldDetailResponse(world);
    }

    /**
     * Lists all sectors.
     */
    public List<SectorResponse> getAllSectors() {
        List<SectorResponse> responses = new ArrayList<>();
        for (TravellerSector sector : sectors.values()) {
            SectorResponse response = new SectorResponse();
            response.setSectorName(sector.getName());
            response.setWorldCount(sector.getWorldCount());
            response.setHyperlaneCount(sector.getHyperlaneCount());
            TravellerWorld capital = sector.getCapitalWorld();
            response.setCapitalWorld(capital != null ? capital.getName() : null);
            // Include world summaries for drill-down capability
            List<SectorResponse.WorldSummary> worldSummaries = new ArrayList<>();
            for (TravellerWorld world : sector.getAllWorlds()) {
                worldSummaries.add(toWorldSummary(world));
            }
            response.setWorlds(worldSummaries);
            responses.add(response);
        }
        return responses;
    }

    /**
     * Finds habitable worlds in a sector.
     */
    public SectorResponse getHabitableWorlds(int sectorId) {
        TravellerSector sector = sectors.get(sectorId);
        if (sector == null) {
            throw new IllegalArgumentException("Sector not found: " + sectorId);
        }

        List<TravellerWorld> habitable = sector.getHabitableWorlds();
        SectorResponse response = new SectorResponse();
        response.setSectorName(sector.getName());
        response.setWorldCount(habitable.size());
        response.setMessage(habitable.size() + " habitable worlds in " + sector.getName());

        List<SectorResponse.WorldSummary> summaries = new ArrayList<>();
        for (TravellerWorld world : habitable) {
            summaries.add(toWorldSummary(world));
        }
        response.setWorlds(summaries);

        return response;
    }

    /**
     * Finds worlds with a specific trade code.
     */
    public SectorResponse getWorldsByTradeCode(int sectorId, String tradeCode) {
        TravellerSector sector = sectors.get(sectorId);
        if (sector == null) {
            throw new IllegalArgumentException("Sector not found: " + sectorId);
        }

        TradeCode code = parseTradeCode(tradeCode);
        if (code == null) {
            throw new IllegalArgumentException("Unknown trade code: " + tradeCode);
        }

        List<TravellerWorld> worlds = sector.getWorldsWithTradeCode(code);
        SectorResponse response = new SectorResponse();
        response.setSectorName(sector.getName());
        response.setWorldCount(worlds.size());
        response.setMessage(worlds.size() + " worlds with " + tradeCode + " in " + sector.getName());

        List<SectorResponse.WorldSummary> summaries = new ArrayList<>();
        for (TravellerWorld world : worlds) {
            summaries.add(toWorldSummary(world));
        }
        response.setWorlds(summaries);

        return response;
    }

    // ---- Private Helpers ----

    private SectorResponse toSectorResponse(TravellerSector sector) {
        SectorResponse response = new SectorResponse();
        response.setSectorName(sector.getName());
        response.setWorldCount(sector.getWorldCount());
        response.setHyperlaneCount(sector.getHyperlaneCount());

        TravellerWorld capital = sector.getCapitalWorld();
        response.setCapitalWorld(capital != null ? capital.getName() : null);

        List<SectorResponse.WorldSummary> summaries = new ArrayList<>();
        for (TravellerWorld world : sector.getAllWorlds()) {
            summaries.add(toWorldSummary(world));
        }
        response.setWorlds(summaries);

        response.setMessage(sector.getWorldCount() + " worlds and " +
                sector.getHyperlaneCount() + " hyperlanes in " + sector.getName());

        return response;
    }

    private SectorResponse toWorldDetailResponse(TravellerWorld world) {
        SectorResponse response = new SectorResponse();
        SectorResponse.WorldDetail detail = new SectorResponse.WorldDetail();

        detail.setId(world.getId());
        detail.setName(world.getName());
        detail.setUwp(world.getUwp().toString());
        detail.setPort(world.getUwp().getPlanetSizeDescription());
        detail.setAtmosphere(world.getUwp().getAtmosphereDescription());
        detail.setGravity(world.getUwp().getPlanetSizeDescription());
        detail.setStarport(String.valueOf(world.getUwp().getStarport()));
        detail.setPopulation(world.getUwp().getPopulation());
        detail.setGovernmentType(world.getUwp().getGovernmentType());
        detail.setLawLevel(world.getUwp().getLawLevelDescription());
        detail.setX(world.getSectorX());
        detail.setY(world.getSectorY());
        detail.setCapital(world.isCapital());
        detail.setPortWealth((double) world.getPortWealth());
        detail.setHabitable(world.isHabitable());
        detail.setBreathable(world.getUwp().isHabitable());

        // Trade codes
        List<String> tradeCodes = new ArrayList<>();
        for (TradeCode code : world.getTradeCodes()) {
            tradeCodes.add(code.name());
        }
        detail.setTradeCodes(tradeCodes);

        // Hyperlanes
        List<String> hyperlanes = new ArrayList<>();
        for (com.yisy.modules.doramcu.traveller.Hyperlane lane : world.getHyperlanes()) {
            hyperlanes.add(lane.getOtherEnd(world).getName());
        }
        detail.setHyperlanes(hyperlanes);

        response.setWorldDetail(detail);
        return response;
    }

    private SectorResponse.WorldSummary toWorldSummary(TravellerWorld world) {
        SectorResponse.WorldSummary summary = new SectorResponse.WorldSummary();
        summary.setId(world.getId());
        summary.setName(world.getName());
        summary.setUwp(world.getUwp().toString());
        summary.setX(world.getSectorX());
        summary.setY(world.getSectorY());
        summary.setCapital(world.isCapital());
        summary.setHyperlaneCount(world.getHyperlaneCount());

        List<String> tradeCodes = new ArrayList<>();
        for (TradeCode code : world.getTradeCodes()) {
            tradeCodes.add(code.name());
        }
        summary.setTradeCodes(tradeCodes);

        return summary;
    }

    private TradeCode parseTradeCode(String name) {
        if (name == null) return null;
        for (TradeCode code : TradeCode.values()) {
            if (code.name().equalsIgnoreCase(name)) {
                return code;
            }
        }
        return null;
    }

    private static class AtomicInteger {
        private int value;

        AtomicInteger(int initialValue) {
            this.value = initialValue;
        }

        int getAndIncrement() {
            return value++;
        }
    }
}
