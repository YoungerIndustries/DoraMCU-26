package com.yisy.modules.doramcu.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.yisy.modules.doramcu.event.GalaxyEventService;
import com.yisy.modules.doramcu.event.GalaxyEventType;
import com.yisy.modules.doramcu.model.*;
import com.yisy.modules.doramcu.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for creating and restoring galaxy backups.
 * <p>
 * Exports the entire galaxy state — galaxy config, sectors, solar systems,
 * celestial bodies, and hyperlanes — to timestamped JSON files stored in the
 * configured backup directory. Restores recreate the galaxy from a backup file,
 * replacing the current state.
 * </p>
 * <p>
 * Supports scheduled backups at a configurable interval. Each galaxy is backed
 * up independently, and old backups are retained for disaster recovery.
 * </p>
 */
@Service
public class GalaxyBackupService {

    private static final Logger log = LoggerFactory.getLogger(GalaxyBackupService.class);
    private static final DateTimeFormatter BACKUP_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");

    private final GalaxyRepository galaxyRepository;
    private final SectorRepository sectorRepository;
    private final SolarSystemRepository solarSystemRepository;
    private final HyperlaneRepository hyperlaneRepository;
    private final StarRepository starRepository;
    private final PlanetRepository planetRepository;
    private final MoonRepository moonRepository;
    private final AsteroidBeltRepository asteroidBeltRepository;
    private final GasGiantRepository gasGiantRepository;
    private final NebulaRepository nebulaRepository;
    private final BlackHoleRepository blackHoleRepository;
    private final CometRepository cometRepository;
    private final SpaceStationRepository spaceStationRepository;
    private final AsteroidRepository asteroidRepository;
    private final GalaxyEventService galaxyEventService;
    private final GalaxyMetricsService galaxyMetricsService;

    private final ObjectMapper objectMapper;
    private Path backupDirectory;

    @Value("${doramcu.galaxy.backup.directory:backups/galaxies}")
    private String backupDirectoryPath;

    @Value("${doramcu.galaxy.backup.scheduled.enabled:false}")
    private boolean scheduledBackupEnabled;

    @Value("${doramcu.galaxy.backup.scheduled.interval-hours:24}")
    private int scheduledBackupIntervalHours;

    public GalaxyBackupService(
            GalaxyRepository galaxyRepository,
            SectorRepository sectorRepository,
            SolarSystemRepository solarSystemRepository,
            HyperlaneRepository hyperlaneRepository,
            StarRepository starRepository,
            PlanetRepository planetRepository,
            MoonRepository moonRepository,
            AsteroidBeltRepository asteroidBeltRepository,
            GasGiantRepository gasGiantRepository,
            NebulaRepository nebulaRepository,
            BlackHoleRepository blackHoleRepository,
            CometRepository cometRepository,
            SpaceStationRepository spaceStationRepository,
            AsteroidRepository asteroidRepository,
            GalaxyEventService galaxyEventService,
            GalaxyMetricsService galaxyMetricsService) {
        this.galaxyRepository = galaxyRepository;
        this.sectorRepository = sectorRepository;
        this.solarSystemRepository = solarSystemRepository;
        this.hyperlaneRepository = hyperlaneRepository;
        this.starRepository = starRepository;
        this.planetRepository = planetRepository;
        this.moonRepository = moonRepository;
        this.asteroidBeltRepository = asteroidBeltRepository;
        this.gasGiantRepository = gasGiantRepository;
        this.nebulaRepository = nebulaRepository;
        this.blackHoleRepository = blackHoleRepository;
        this.cometRepository = cometRepository;
        this.spaceStationRepository = spaceStationRepository;
        this.asteroidRepository = asteroidRepository;
        this.galaxyEventService = galaxyEventService;
        this.galaxyMetricsService = galaxyMetricsService;

        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    @PostConstruct
    public void init() {
        this.backupDirectory = Path.of(backupDirectoryPath);
        try {
            Files.createDirectories(backupDirectory);
            log.info("Galaxy backup directory initialized: {}", backupDirectory.toAbsolutePath());
        } catch (IOException e) {
            log.error("Failed to create backup directory: {}", backupDirectory, e);
        }
    }

    /**
     * Creates a backup of the specified galaxy.
     *
     * @param galaxyName the name of the galaxy to back up
     * @return map containing backup file path, size, and statistics
     * @throws IllegalArgumentException if the galaxy does not exist
     */
    @Transactional(readOnly = true)
    public Map<String, Object> createBackup(String galaxyName) {
        Instant startTime = Instant.now();
        Galaxy galaxy = galaxyRepository.findByName(galaxyName)
                .orElseThrow(() -> new IllegalArgumentException("Galaxy not found: " + galaxyName));

        String timestamp = LocalDateTime.now().format(BACKUP_TIME_FORMAT);
        String safeName = galaxyName.replaceAll("[^a-zA-Z0-9_-]", "_");
        String backupFileName = safeName + "-" + timestamp + ".json";
        Path backupFile = backupDirectory.resolve(backupFileName);

        log.info("Creating backup for galaxy '{}' -> {}", galaxyName, backupFile.toAbsolutePath());

        // Build the backup data structure
        Map<String, Object> backupData = new LinkedHashMap<>();
        backupData.put("version", 1);
        backupData.put("backupTime", LocalDateTime.now().toString());
        backupData.put("galaxyName", galaxyName);

        // Galaxy config
        Map<String, Object> galaxyConfig = serializeGalaxyConfig(galaxy);
        backupData.put("galaxy", galaxyConfig);

        // Sectors
        List<Sector> sectors = sectorRepository.findByGalaxyId(galaxy.getId());
        List<Map<String, Object>> sectorData = sectors.stream()
                .map(this::serializeSector)
                .collect(Collectors.toList());
        backupData.put("sectors", sectorData);

        // Solar systems with celestial bodies
        List<SolarSystem> systems = solarSystemRepository.findByGalaxyId(galaxy.getId());
        List<Map<String, Object>> systemData = systems.stream()
                .map(this::serializeSolarSystem)
                .collect(Collectors.toList());
        backupData.put("solarSystems", systemData);

        // Hyperlanes
        List<Hyperlane> hyperlanes = hyperlaneRepository.findByGalaxyId(galaxy.getId());
        List<Map<String, Object>> hyperlaneData = hyperlanes.stream()
                .map(this::serializeHyperlane)
                .collect(Collectors.toList());
        backupData.put("hyperlanes", hyperlaneData);

        // Statistics
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("sectorCount", sectors.size());
        stats.put("systemCount", systems.size());
        stats.put("hyperlaneCount", hyperlanes.size());
        int totalBodies = systems.stream()
                .mapToInt(ss -> ss.getPlanets().size() + ss.getMoons().size() +
                        ss.getAsteroidBelts().size() + ss.getGasGiants().size() +
                        ss.getNebulas().size() + ss.getBlackHoles().size() +
                        ss.getComets().size() + ss.getSpaceStations().size() +
                        ss.getAsteroids().size() + (ss.getStar() != null ? 1 : 0))
                .sum();
        stats.put("celestialBodyCount", totalBodies);
        backupData.put("statistics", stats);

        // Write to file
        try {
            objectMapper.writeValue(backupFile.toFile(), backupData);
            long sizeBytes = Files.size(backupFile);
            Duration duration = Duration.between(startTime, Instant.now());

            log.info("Backup completed: {} ({} bytes, {}ms)", backupFileName, sizeBytes, duration.toMillis());

            // Publish event
            galaxyEventService.publish(GalaxyEventType.BUILD_COMPLETED, galaxyName, galaxy.getId(),
                    "eventType", "BACKUP_CREATED",
                    "backupFile", backupFileName,
                    "sizeBytes", String.valueOf(sizeBytes),
                    "elapsedMs", String.valueOf(duration.toMillis()));

            // Update metrics
            galaxyMetricsService.incrementBackupCreated();
            galaxyMetricsService.setLastBackupTime(galaxyName, LocalDateTime.now());

            // Build response
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("backupFile", backupFileName);
            result.put("backupPath", backupFile.toAbsolutePath().toString());
            result.put("sizeBytes", sizeBytes);
            result.put("elapsedMs", duration.toMillis());
            result.putAll(stats);
            return result;

        } catch (IOException e) {
            log.error("Failed to write backup for galaxy '{}': {}", galaxyName, e.getMessage(), e);
            throw new RuntimeException("Failed to create backup: " + e.getMessage(), e);
        }
    }

    /**
     * Restores a galaxy from a backup file.
     * <p>
     * This deletes the current galaxy state and recreates it from the backup.
     * </p>
     *
     * @param galaxyName  the name of the galaxy to restore (will be recreated)
     * @param backupFile  the backup file name (without directory path)
     * @return map containing restore statistics
     * @throws IllegalArgumentException if the backup file does not exist
     */
    @Transactional
    public Map<String, Object> restoreBackup(String galaxyName, String backupFile) {
        Instant startTime = Instant.now();
        Path backupPath = backupDirectory.resolve(backupFile);

        if (!Files.exists(backupPath)) {
            throw new IllegalArgumentException("Backup file not found: " + backupFile);
        }

        log.info("Restoring galaxy '{}' from backup: {}", galaxyName, backupFile);

        // Read backup data
        Map<String, Object> backupData;
        try {
            backupData = objectMapper.readValue(backupPath.toFile(), LinkedHashMap.class);
        } catch (IOException e) {
            log.error("Failed to read backup file: {}", backupFile, e);
            throw new RuntimeException("Failed to read backup: " + e.getMessage(), e);
        }

        // Delete existing galaxy if it exists
        galaxyRepository.findByName(galaxyName).ifPresent(g -> {
            // Delete related entities first
            hyperlaneRepository.findByGalaxyId(g.getId()).forEach(hyperlaneRepository::delete);
            solarSystemRepository.findByGalaxyId(g.getId()).forEach(solarSystemRepository::delete);
            sectorRepository.findByGalaxyId(g.getId()).forEach(sectorRepository::delete);
            galaxyRepository.delete(g);
            log.info("Deleted existing galaxy '{}' for restore", galaxyName);
        });

        // Restore galaxy config
        @SuppressWarnings("unchecked")
        Map<String, Object> galaxyConfig = (Map<String, Object>) backupData.get("galaxy");
        Galaxy galaxy = deserializeGalaxyConfig(galaxyConfig, galaxyName);
        galaxy = galaxyRepository.save(galaxy);

        // Restore sectors
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> sectorData = (List<Map<String, Object>>) backupData.get("sectors");
        int sectorsRestored = 0;
        if (sectorData != null) {
            for (Map<String, Object> sd : sectorData) {
                Sector sector = deserializeSector(sd, galaxy);
                sectorRepository.save(sector);
                sectorsRestored++;
            }
        }

        // Restore solar systems with celestial bodies
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> systemData = (List<Map<String, Object>>) backupData.get("solarSystems");
        int systemsRestored = 0;
        if (systemData != null) {
            for (Map<String, Object> ssd : systemData) {
                SolarSystem system = deserializeSolarSystem(ssd, galaxy);
                solarSystemRepository.save(system);
                systemsRestored++;
            }
        }

        // Restore hyperlanes
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> hyperlaneData = (List<Map<String, Object>>) backupData.get("hyperlanes");
        int hyperlanesRestored = 0;
        if (hyperlaneData != null) {
            for (Map<String, Object> hd : hyperlaneData) {
                Hyperlane hyperlane = deserializeHyperlane(hd, galaxy);
                hyperlaneRepository.save(hyperlane);
                hyperlanesRestored++;
            }
        }

        Duration duration = Duration.between(startTime, Instant.now());
        long sizeBytes;
        try {
            sizeBytes = Files.size(backupPath);
        } catch (IOException e) {
            sizeBytes = 0;
        }

        log.info("Restore completed: {} sectors, {} systems, {} hyperlanes in {}ms",
                sectorsRestored, systemsRestored, hyperlanesRestored, duration.toMillis());

        // Publish event
        galaxyEventService.publish(GalaxyEventType.BUILD_COMPLETED, galaxyName, galaxy.getId(),
                "eventType", "BACKUP_RESTORED",
                "backupFile", backupFile,
                "sectorsRestored", String.valueOf(sectorsRestored),
                "systemsRestored", String.valueOf(systemsRestored),
                "elapsedMs", String.valueOf(duration.toMillis()));

        // Update metrics
        galaxyMetricsService.incrementBackupRestored();

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("backupFile", backupFile);
        result.put("galaxyName", galaxyName);
        result.put("sectorsRestored", sectorsRestored);
        result.put("systemsRestored", systemsRestored);
        result.put("hyperlanesRestored", hyperlanesRestored);
        result.put("elapsedMs", duration.toMillis());
        return result;
    }

    /**
     * Lists all available backups for a galaxy.
     *
     * @param galaxyName the galaxy name to filter by
     * @return list of backup info maps sorted by backup time (newest first)
     */
    public List<Map<String, Object>> listBackups(String galaxyName) {
        String safeName = galaxyName.replaceAll("[^a-zA-Z0-9_-]", "_");
        List<Map<String, Object>> backups = new ArrayList<>();

        if (!Files.exists(backupDirectory)) {
            return backups;
        }

        try (var streams = Files.newDirectoryStream(backupDirectory, safeName + "-*.json")) {
            for (Path file : streams) {
                String fileName = file.getFileName().toString();
                long sizeBytes = Files.size(file);

                // Parse backup time from filename
                String timePart = fileName.replace(safeName + "-", "").replace(".json", "");
                LocalDateTime backupTime;
                try {
                    backupTime = LocalDateTime.parse(timePart, BACKUP_TIME_FORMAT);
                } catch (Exception e) {
                    backupTime = LocalDateTime.ofInstant(
                            Files.getLastModifiedTime(file).toInstant(),
                            java.time.ZoneId.systemDefault());
                }

                Map<String, Object> info = new LinkedHashMap<>();
                info.put("backupFile", fileName);
                info.put("backupTime", backupTime.toString());
                info.put("sizeBytes", sizeBytes);
                info.put("galaxyName", galaxyName);
                backups.add(info);
            }
        } catch (IOException e) {
            log.warn("Failed to list backups for '{}': {}", galaxyName, e.getMessage());
        }

        // Sort by backup time, newest first
        backups.sort((a, b) -> {
            String timeA = (String) a.get("backupTime");
            String timeB = (String) b.get("backupTime");
            return timeB.compareTo(timeA);
        });

        return backups;
    }

    /**
     * Deletes a specific backup file.
     *
     * @param galaxyName the galaxy name (for logging)
     * @param backupFile the backup file name to delete
     * @return true if the file was deleted
     */
    public boolean deleteBackup(String galaxyName, String backupFile) {
        Path backupPath = backupDirectory.resolve(backupFile);

        if (!Files.exists(backupPath)) {
            log.warn("Backup file not found for deletion: {}", backupFile);
            return false;
        }

        try {
            Files.delete(backupPath);
            log.info("Deleted backup '{}' for galaxy '{}'", backupFile, galaxyName);
            return true;
        } catch (IOException e) {
            log.error("Failed to delete backup '{}': {}", backupFile, e.getMessage(), e);
            return false;
        }
    }

    /**
     * Gets the last backup time for a galaxy.
     */
    public Optional<LocalDateTime> getLastBackupTime(String galaxyName) {
        List<Map<String, Object>> backups = listBackups(galaxyName);
        if (backups.isEmpty()) {
            return Optional.empty();
        }
        String timeStr = (String) backups.get(0).get("backupTime");
        return Optional.of(LocalDateTime.parse(timeStr));
    }

    // ---- Scheduled backups ----

    @Scheduled(fixedRateString = "${doramcu.galaxy.backup.scheduled.interval-ms:86400000}")
    public void scheduledBackup() {
        if (!scheduledBackupEnabled) {
            return;
        }

        log.info("Running scheduled galaxy backup");
        List<Galaxy> galaxies = galaxyRepository.findAll();
        int success = 0;
        int failed = 0;

        for (Galaxy galaxy : galaxies) {
            try {
                createBackup(galaxy.getName());
                success++;
            } catch (Exception e) {
                log.error("Scheduled backup failed for galaxy '{}': {}", galaxy.getName(), e.getMessage());
                failed++;
            }
        }

        log.info("Scheduled backup completed: {} succeeded, {} failed", success, failed);
    }

    // ---- Serialization helpers ----

    private Map<String, Object> serializeGalaxyConfig(Galaxy g) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("name", g.getName());
        map.put("totalSectors", g.getTotalSectors());
        map.put("hyperlaneDensity", g.getHyperlaneDensity());
        map.put("seed", g.getSeed());
        map.put("systemCount", g.getSystemCount());
        map.put("planetsPerSystem", g.getPlanetsPerSystem());
        map.put("moonChance", g.getMoonChance());
        map.put("asteroidBeltChance", g.getAsteroidBeltChance());
        map.put("gasGiantChance", g.getGasGiantChance());
        map.put("nebulaChance", g.getNebulaChance());
        map.put("blackHoleChance", g.getBlackHoleChance());
        map.put("cometChance", g.getCometChance());
        map.put("spaceStationChance", g.getSpaceStationChance());
        map.put("gameType", g.getGameType());
        map.put("difficultyLevel", g.getDifficultyLevel());
        map.put("theme", g.getTheme());
        map.put("description", g.getDescription());
        map.put("galaxyOverseerId", g.getGalaxyOverseerId());
        map.put("timeCompressionRatio", g.getTimeCompressionRatio());
        map.put("timezone", g.getTimezone());
        map.put("createdAt", g.getCreatedAt() != null ? g.getCreatedAt().toString() : null);
        return map;
    }

    private Map<String, Object> serializeSector(Sector s) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("sectorRow", s.getSectorRow());
        map.put("sectorCol", s.getSectorCol());
        map.put("name", s.getName());
        map.put("tradeType", s.getTradeType());
        return map;
    }

    private Map<String, Object> serializeSolarSystem(SolarSystem ss) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("name", ss.getName());
        map.put("sectorRow", ss.getSectorRow());
        map.put("sectorCol", ss.getSectorCol());
        map.put("subSectorRow", ss.getSubSectorRow());
        map.put("subSectorCol", ss.getSubSectorCol());
        map.put("posX", ss.getPosX());
        map.put("posY", ss.getPosY());
        map.put("posZ", ss.getPosZ());
        map.put("hexSystemId", ss.getHexSystemId());

        if (ss.getStar() != null) {
            map.put("star", serializeStar(ss.getStar()));
        }
        if (!ss.getPlanets().isEmpty()) {
            map.put("planets", ss.getPlanets().stream().map(this::serializePlanet).collect(Collectors.toList()));
        }
        if (!ss.getMoons().isEmpty()) {
            map.put("moons", ss.getMoons().stream().map(this::serializeMoon).collect(Collectors.toList()));
        }
        if (!ss.getAsteroidBelts().isEmpty()) {
            map.put("asteroidBelts", ss.getAsteroidBelts().stream().map(this::serializeAsteroidBelt).collect(Collectors.toList()));
        }
        if (!ss.getGasGiants().isEmpty()) {
            map.put("gasGiants", ss.getGasGiants().stream().map(this::serializeGasGiant).collect(Collectors.toList()));
        }
        if (!ss.getNebulas().isEmpty()) {
            map.put("nebulas", ss.getNebulas().stream().map(this::serializeNebula).collect(Collectors.toList()));
        }
        if (!ss.getBlackHoles().isEmpty()) {
            map.put("blackHoles", ss.getBlackHoles().stream().map(this::serializeBlackHole).collect(Collectors.toList()));
        }
        if (!ss.getComets().isEmpty()) {
            map.put("comets", ss.getComets().stream().map(this::serializeComet).collect(Collectors.toList()));
        }
        if (!ss.getSpaceStations().isEmpty()) {
            map.put("spaceStations", ss.getSpaceStations().stream().map(this::serializeSpaceStation).collect(Collectors.toList()));
        }
        if (!ss.getAsteroids().isEmpty()) {
            map.put("asteroids", ss.getAsteroids().stream().map(this::serializeAsteroid).collect(Collectors.toList()));
        }
        return map;
    }

    private Map<String, Object> serializeHyperlane(Hyperlane h) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("fromSystemId", h.getFromSystemId());
        map.put("toSystemId", h.getToSystemId());
        map.put("distance", h.getDistance());
        return map;
    }

    // Minimal serializers for celestial bodies — captures essential fields
    private Map<String, Object> serializeStar(Star s) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("name", s.getName());
        m.put("starSize", s.getStarSize() != null ? s.getStarSize().name() : null);
        m.put("starType", s.getStarType() != null ? s.getStarType().name() : null);
        m.put("radius", s.getRadius());
        m.put("energyYield", s.getEnergyYield());
        m.put("stationSlots", s.getStationSlots());
        m.put("radiationDamage", s.getRadiationDamage());
        m.put("gravityWellBonus", s.getGravityWellBonus());
        return m;
    }

    private Map<String, Object> serializePlanet(Planet p) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("name", p.getName());
        m.put("planetSize", p.getPlanetSize() != null ? p.getPlanetSize().name() : null);
        m.put("mineralQuality", p.getMineralQuality() != null ? p.getMineralQuality().name() : null);
        m.put("radius", p.getRadius());
        m.put("mineralProduction", p.getMineralProduction());
        m.put("energyProduction", p.getEnergyProduction());
        m.put("foodProduction", p.getFoodProduction());
        m.put("researchProduction", p.getResearchProduction());
        m.put("influenceProduction", p.getInfluenceProduction());
        m.put("currentPopulation", p.getCurrentPopulation());
        m.put("maxPopulation", p.getMaxPopulation());
        m.put("socialStat", p.getSocialStat());
        m.put("intelligenceStat", p.getIntelligenceStat());
        m.put("diligenceStat", p.getDiligenceStat());
        m.put("resolveStat", p.getResolveStat());
        return m;
    }

    private Map<String, Object> serializeMoon(Moon m) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("name", m.getName());
        map.put("moonSize", m.getMoonSize() != null ? m.getMoonSize().name() : null);
        map.put("mineralYield", m.getMineralYield());
        map.put("outpostSlots", m.getOutpostSlots());
        map.put("strategicDeposit", m.getStrategicDeposit() != null ? m.getStrategicDeposit().name() : null);
        return map;
    }

    private Map<String, Object> serializeAsteroidBelt(AsteroidBelt ab) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("name", ab.getName());
        m.put("radius", ab.getRadius());
        m.put("mineralYield", ab.getMineralYield());
        m.put("stationSlots", ab.getStationSlots());
        m.put("movementPenalty", ab.getMovementPenalty());
        return m;
    }

    private Map<String, Object> serializeGasGiant(GasGiant gg) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("name", gg.getName());
        m.put("radius", gg.getRadius());
        m.put("energyYield", gg.getEnergyYield());
        m.put("stationSlots", gg.getStationSlots());
        return m;
    }

    private Map<String, Object> serializeNebula(Nebula n) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("name", n.getName());
        m.put("nebulaSize", n.getNebulaSize() != null ? n.getNebulaSize().name() : null);
        m.put("radius", n.getRadius());
        m.put("eleriumYield", n.getEleriumYield());
        m.put("stationSlots", n.getStationSlots());
        m.put("sensorJamming", n.getSensorJamming());
        m.put("cloakingBonus", n.getCloakingBonus());
        return m;
    }

    private Map<String, Object> serializeBlackHole(BlackHole bh) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("name", bh.getName());
        m.put("radius", bh.getRadius());
        m.put("antiMatterYield", bh.getAntiMatterYield());
        m.put("stationSlots", bh.getStationSlots());
        m.put("gravitationalPull", bh.getGravitationalPull());
        m.put("destructionChance", bh.getDestructionChance());
        return m;
    }

    private Map<String, Object> serializeComet(Comet c) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("name", c.getName());
        m.put("resourceBonus", c.getResourceBonus());
        m.put("durationTurns", c.getDurationTurns());
        return m;
    }

    private Map<String, Object> serializeSpaceStation(SpaceStation ss) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("name", ss.getName());
        m.put("stationType", ss.getStationType() != null ? ss.getStationType().name() : null);
        m.put("hexX", ss.getHexX());
        m.put("hexY", ss.getHexY());
        m.put("hexZ", ss.getHexZ());
        m.put("hullPoints", ss.getHullPoints());
        m.put("currentHull", ss.getCurrentHull());
        m.put("shieldPoints", ss.getShieldPoints());
        m.put("currentShield", ss.getCurrentShield());
        m.put("componentSlots", ss.getComponentSlots());
        m.put("occupiedSlots", ss.getOccupiedSlots());
        m.put("damage", ss.getDamage());
        return m;
    }

    private Map<String, Object> serializeAsteroid(Asteroid a) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("name", a.getName());
        m.put("mineralYield", a.getMineralYield());
        m.put("outpostSlots", a.getOutpostSlots());
        m.put("movementPenalty", a.getMovementPenalty());
        return m;
    }

    // ---- Deserialization helpers ----

    private Galaxy deserializeGalaxyConfig(Map<String, Object> config, String galaxyName) {
        Galaxy galaxy = new Galaxy();
        galaxy.setName(galaxyName);
        galaxy.setTotalSectors((Integer) config.getOrDefault("totalSectors", 100));
        galaxy.setHyperlaneDensity((Double) config.getOrDefault("hyperlaneDensity", 0.1));

        Object seed = config.get("seed");
        if (seed instanceof Number) galaxy.setSeed(((Number) seed).longValue());

        Object systemCount = config.get("systemCount");
        if (systemCount instanceof Number) galaxy.setSystemCount(((Number) systemCount).intValue());

        Object planetsPerSystem = config.get("planetsPerSystem");
        if (planetsPerSystem instanceof Number) galaxy.setPlanetsPerSystem(((Number) planetsPerSystem).intValue());

        Object moonChance = config.get("moonChance");
        if (moonChance instanceof Number) galaxy.setMoonChance(((Number) moonChance).doubleValue());

        Object asteroidBeltChance = config.get("asteroidBeltChance");
        if (asteroidBeltChance instanceof Number) galaxy.setAsteroidBeltChance(((Number) asteroidBeltChance).doubleValue());

        Object gasGiantChance = config.get("gasGiantChance");
        if (gasGiantChance instanceof Number) galaxy.setGasGiantChance(((Number) gasGiantChance).doubleValue());

        Object nebulaChance = config.get("nebulaChance");
        if (nebulaChance instanceof Number) galaxy.setNebulaChance(((Number) nebulaChance).doubleValue());

        Object blackHoleChance = config.get("blackHoleChance");
        if (blackHoleChance instanceof Number) galaxy.setBlackHoleChance(((Number) blackHoleChance).doubleValue());

        Object cometChance = config.get("cometChance");
        if (cometChance instanceof Number) galaxy.setCometChance(((Number) cometChance).doubleValue());

        Object spaceStationChance = config.get("spaceStationChance");
        if (spaceStationChance instanceof Number) galaxy.setSpaceStationChance(((Number) spaceStationChance).doubleValue());

        galaxy.setGameType((String) config.get("gameType"));
        galaxy.setDifficultyLevel((String) config.get("difficultyLevel"));
        galaxy.setTheme((String) config.get("theme"));
        galaxy.setDescription((String) config.get("description"));

        Object overseerId = config.get("galaxyOverseerId");
        if (overseerId instanceof Number) galaxy.setGalaxyOverseerId(((Number) overseerId).longValue());

        Object tcr = config.get("timeCompressionRatio");
        if (tcr instanceof Number) galaxy.setTimeCompressionRatio(((Number) tcr).doubleValue());

        galaxy.setTimezone((String) config.getOrDefault("timezone", "UTC"));
        galaxy.setCreatedAt(LocalDateTime.now());
        return galaxy;
    }

    private Sector deserializeSector(Map<String, Object> data, Galaxy galaxy) {
        Sector sector = new Sector();
        sector.setSectorRow((Integer) data.get("sectorRow"));
        sector.setSectorCol((Integer) data.get("sectorCol"));
        sector.setName((String) data.get("name"));
        sector.setTradeType((String) data.get("tradeType"));
        sector.setGalaxy(galaxy);
        return sector;
    }

    @SuppressWarnings("unchecked")
    private SolarSystem deserializeSolarSystem(Map<String, Object> data, Galaxy galaxy) {
        SolarSystem system = new SolarSystem();
        system.setName((String) data.get("name"));
        system.setGalaxy(galaxy);
        system.setSectorRow((Integer) data.get("sectorRow"));
        system.setSectorCol((Integer) data.get("sectorCol"));
        system.setSubSectorRow((Integer) data.get("subSectorRow"));
        system.setSubSectorCol((Integer) data.get("subSectorCol"));
        system.setPosX((Integer) data.get("posX"));
        system.setPosY((Integer) data.get("posY"));
        system.setPosZ((Integer) data.get("posZ"));

        Object hexId = data.get("hexSystemId");
        if (hexId instanceof Number) system.setHexSystemId(((Number) hexId).intValue());

        // Star
        Map<String, Object> starData = (Map<String, Object>) data.get("star");
        if (starData != null) {
            Star star = new Star();
            star.setName((String) starData.get("name"));
            String sizeStr = (String) starData.get("starSize");
            if (sizeStr != null) star.setStarSize(Star.StarSize.valueOf(sizeStr));
            String typeStr = (String) starData.get("starType");
            if (typeStr != null) star.setStarType(Star.StarType.valueOf(typeStr));
            Object radius = starData.get("radius");
            if (radius instanceof Number) star.setRadius(((Number) radius).intValue());
            Object ey = starData.get("energyYield");
            if (ey instanceof Number) star.setEnergyYield(((Number) ey).intValue());
            Object ss = starData.get("stationSlots");
            if (ss instanceof Number) star.setStationSlots(((Number) ss).intValue());
            Object rd = starData.get("radiationDamage");
            if (rd instanceof Number) star.setRadiationDamage(((Number) rd).doubleValue());
            Object gw = starData.get("gravityWellBonus");
            if (gw instanceof Number) star.setGravityWellBonus(((Number) gw).doubleValue());
            star.setSolarSystem(system);
            system.setStar(star);
        }

        // Planets
        List<Map<String, Object>> planetData = (List<Map<String, Object>>) data.get("planets");
        if (planetData != null) {
            for (Map<String, Object> pd : planetData) {
                Planet planet = new Planet();
                planet.setName((String) pd.get("name"));
                String sizeStr = (String) pd.get("planetSize");
                if (sizeStr != null) planet.setPlanetSize(Planet.PlanetSize.valueOf(sizeStr));
                String mqStr = (String) pd.get("mineralQuality");
                if (mqStr != null) planet.setMineralQuality(Planet.MineralQuality.valueOf(mqStr));
                Object radius = pd.get("radius");
                if (radius instanceof Number) planet.setRadius(((Number) radius).intValue());
                Object mp = pd.get("mineralProduction");
                if (mp instanceof Number) planet.setMineralProduction(((Number) mp).intValue());
                Object ep = pd.get("energyProduction");
                if (ep instanceof Number) planet.setEnergyProduction(((Number) ep).intValue());
                Object fp = pd.get("foodProduction");
                if (fp instanceof Number) planet.setFoodProduction(((Number) fp).intValue());
                Object rp = pd.get("researchProduction");
                if (rp instanceof Number) planet.setResearchProduction(((Number) rp).intValue());
                Object ip = pd.get("influenceProduction");
                if (ip instanceof Number) planet.setInfluenceProduction(((Number) ip).intValue());
                Object cp = pd.get("currentPopulation");
                if (cp instanceof Number) planet.setCurrentPopulation(((Number) cp).intValue());
                Object maxPop = pd.get("maxPopulation");
                if (maxPop instanceof Number) planet.setMaxPopulation(((Number) maxPop).intValue());
                Object ss = pd.get("socialStat");
                if (ss instanceof Number) planet.setSocialStat(((Number) ss).intValue());
                Object is2 = pd.get("intelligenceStat");
                if (is2 instanceof Number) planet.setIntelligenceStat(((Number) is2).intValue());
                Object ds = pd.get("diligenceStat");
                if (ds instanceof Number) planet.setDiligenceStat(((Number) ds).intValue());
                Object rs = pd.get("resolveStat");
                if (rs instanceof Number) planet.setResolveStat(((Number) rs).intValue());
                planet.setSolarSystem(system);
                system.getPlanets().add(planet);
            }
        }

        // Moons
        List<Map<String, Object>> moonData = (List<Map<String, Object>>) data.get("moons");
        if (moonData != null) {
            for (Map<String, Object> md : moonData) {
                Moon moon = new Moon();
                moon.setName((String) md.get("name"));
                String sizeStr = (String) md.get("moonSize");
                if (sizeStr != null) moon.setMoonSize(Moon.MoonSize.valueOf(sizeStr));
                Object my = md.get("mineralYield");
                if (my instanceof Number) moon.setMineralYield(((Number) my).intValue());
                Object os = md.get("outpostSlots");
                if (os instanceof Number) moon.setOutpostSlots(((Number) os).intValue());
                String sd = (String) md.get("strategicDeposit");
                if (sd != null) moon.setStrategicDeposit(com.yisy.modules.doramcu.model.StrategicResource.valueOf(sd));
                moon.setSolarSystem(system);
                system.getMoons().add(moon);
            }
        }

        // Asteroid belts
        List<Map<String, Object>> abd = (List<Map<String, Object>>) data.get("asteroidBelts");
        if (abd != null) {
            for (Map<String, Object> ad : abd) {
                AsteroidBelt belt = new AsteroidBelt();
                belt.setName((String) ad.get("name"));
                Object radius = ad.get("radius");
                if (radius instanceof Number) belt.setRadius(((Number) radius).intValue());
                Object my = ad.get("mineralYield");
                if (my instanceof Number) belt.setMineralYield(((Number) my).intValue());
                Object slots = ad.get("stationSlots");
                if (slots instanceof Number) belt.setStationSlots(((Number) slots).intValue());
                Object mp = ad.get("movementPenalty");
                if (mp instanceof Number) belt.setMovementPenalty(((Number) mp).doubleValue());
                belt.setSolarSystem(system);
                system.getAsteroidBelts().add(belt);
            }
        }

        // Gas giants
        List<Map<String, Object>> ggData = (List<Map<String, Object>>) data.get("gasGiants");
        if (ggData != null) {
            for (Map<String, Object> gd : ggData) {
                GasGiant gg = new GasGiant();
                gg.setName((String) gd.get("name"));
                Object radius = gd.get("radius");
                if (radius instanceof Number) gg.setRadius(((Number) radius).intValue());
                Object ey = gd.get("energyYield");
                if (ey instanceof Number) gg.setEnergyYield(((Number) ey).intValue());
                Object slots = gd.get("stationSlots");
                if (slots instanceof Number) gg.setStationSlots(((Number) slots).intValue());
                gg.setSolarSystem(system);
                system.getGasGiants().add(gg);
            }
        }

        // Nebulas
        List<Map<String, Object>> nebData = (List<Map<String, Object>>) data.get("nebulas");
        if (nebData != null) {
            for (Map<String, Object> nd : nebData) {
                Nebula nebula = new Nebula();
                nebula.setName((String) nd.get("name"));
                String sizeStr = (String) nd.get("nebulaSize");
                if (sizeStr != null) nebula.setNebulaSize(Nebula.NebulaSize.valueOf(sizeStr));
                Object radius = nd.get("radius");
                if (radius instanceof Number) nebula.setRadius(((Number) radius).intValue());
                Object ey = nd.get("eleriumYield");
                if (ey instanceof Number) nebula.setEleriumYield(((Number) ey).intValue());
                Object slots = nd.get("stationSlots");
                if (slots instanceof Number) nebula.setStationSlots(((Number) slots).intValue());
                Object sj = nd.get("sensorJamming");
                if (sj instanceof Number) nebula.setSensorJamming(((Number) sj).doubleValue());
                Object cb = nd.get("cloakingBonus");
                if (cb instanceof Number) nebula.setCloakingBonus(((Number) cb).doubleValue());
                nebula.setSolarSystem(system);
                system.getNebulas().add(nebula);
            }
        }

        // Black holes
        List<Map<String, Object>> bhData = (List<Map<String, Object>>) data.get("blackHoles");
        if (bhData != null) {
            for (Map<String, Object> bd : bhData) {
                BlackHole bh = new BlackHole();
                bh.setName((String) bd.get("name"));
                Object radius = bd.get("radius");
                if (radius instanceof Number) bh.setRadius(((Number) radius).intValue());
                Object amy = bd.get("antiMatterYield");
                if (amy instanceof Number) bh.setAntiMatterYield(((Number) amy).intValue());
                Object slots = bd.get("stationSlots");
                if (slots instanceof Number) bh.setStationSlots(((Number) slots).intValue());
                Object gp = bd.get("gravitationalPull");
                if (gp instanceof Number) bh.setGravitationalPull(((Number) gp).doubleValue());
                Object dc = bd.get("destructionChance");
                if (dc instanceof Number) bh.setDestructionChance(((Number) dc).doubleValue());
                bh.setSolarSystem(system);
                system.getBlackHoles().add(bh);
            }
        }

        // Comets
        List<Map<String, Object>> comData = (List<Map<String, Object>>) data.get("comets");
        if (comData != null) {
            for (Map<String, Object> cd : comData) {
                Comet comet = new Comet();
                comet.setName((String) cd.get("name"));
                Object rb = cd.get("resourceBonus");
                if (rb instanceof Number) comet.setResourceBonus(((Number) rb).intValue());
                Object dt = cd.get("durationTurns");
                if (dt instanceof Number) comet.setDurationTurns(((Number) dt).intValue());
                comet.setSolarSystem(system);
                system.getComets().add(comet);
            }
        }

        // Space stations
        List<Map<String, Object>> ssData = (List<Map<String, Object>>) data.get("spaceStations");
        if (ssData != null) {
            for (Map<String, Object> sd : ssData) {
                SpaceStation station = new SpaceStation();
                station.setName((String) sd.get("name"));
                String typeStr = (String) sd.get("stationType");
                if (typeStr != null) station.setStationType(SpaceStation.StationType.valueOf(typeStr));
                Object hx = sd.get("hexX");
                if (hx instanceof Number) station.setHexX(((Number) hx).intValue());
                Object hy = sd.get("hexY");
                if (hy instanceof Number) station.setHexY(((Number) hy).intValue());
                Object hz = sd.get("hexZ");
                if (hz instanceof Number) station.setHexZ(((Number) hz).intValue());
                Object hp = sd.get("hullPoints");
                if (hp instanceof Number) station.setHullPoints(((Number) hp).intValue());
                Object ch = sd.get("currentHull");
                if (ch instanceof Number) station.setCurrentHull(((Number) ch).intValue());
                Object sp = sd.get("shieldPoints");
                if (sp instanceof Number) station.setShieldPoints(((Number) sp).intValue());
                Object cs = sd.get("currentShield");
                if (cs instanceof Number) station.setCurrentShield(((Number) cs).intValue());
                Object compSlots = sd.get("componentSlots");
                if (compSlots instanceof Number) station.setComponentSlots(((Number) compSlots).intValue());
                Object occSlots = sd.get("occupiedSlots");
                if (occSlots instanceof Number) station.setOccupiedSlots(((Number) occSlots).intValue());
                Object dmg = sd.get("damage");
                if (dmg instanceof Number) station.setDamage(((Number) dmg).intValue());
                station.setSolarSystem(system);
                system.getSpaceStations().add(station);
            }
        }

        // Asteroids
        List<Map<String, Object>> astData = (List<Map<String, Object>>) data.get("asteroids");
        if (astData != null) {
            for (Map<String, Object> ad : astData) {
                Asteroid asteroid = new Asteroid();
                asteroid.setName((String) ad.get("name"));
                Object my = ad.get("mineralYield");
                if (my instanceof Number) asteroid.setMineralYield(((Number) my).intValue());
                Object os = ad.get("outpostSlots");
                if (os instanceof Number) asteroid.setOutpostSlots(((Number) os).intValue());
                Object mp = ad.get("movementPenalty");
                if (mp instanceof Number) asteroid.setMovementPenalty(((Number) mp).doubleValue());
                asteroid.setSolarSystem(system);
                system.getAsteroids().add(asteroid);
            }
        }

        return system;
    }

    private Hyperlane deserializeHyperlane(Map<String, Object> data, Galaxy galaxy) {
        Hyperlane hyperlane = new Hyperlane();
        hyperlane.setGalaxy(galaxy);
        hyperlane.setFromSystemId((Integer) data.get("fromSystemId"));
        hyperlane.setToSystemId((Integer) data.get("toSystemId"));
        hyperlane.setDistance((Integer) data.get("distance"));
        return hyperlane;
    }
}
