package com.yisy.modules.doramcu.service;

import com.yisy.modules.doramcu.dto.WormholeConnectionDto;
import com.yisy.modules.doramcu.dto.WormholeInfoDto;
import com.yisy.modules.doramcu.dto.WormholePairRequest;
import com.yisy.modules.doramcu.event.GalaxyEventService;
import com.yisy.modules.doramcu.event.GalaxyEventType;
import com.yisy.modules.doramcu.model.Galaxy;
import com.yisy.modules.doramcu.model.SolarSystem;
import com.yisy.modules.doramcu.model.User;
import com.yisy.modules.doramcu.model.Wormhole;
import com.yisy.modules.doramcu.repository.GalaxyRepository;
import com.yisy.modules.doramcu.repository.SolarSystemRepository;
import com.yisy.modules.doramcu.repository.UserRepository;
import com.yisy.modules.doramcu.repository.WormholeRepository;
import com.yisy.modules.doramcu.service.GalaxyMetricsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

/**
 * Service that manages wormhole pairs and travel through wormholes.
 * <p>
 * A wormhole pair connects two galaxies, allowing players to travel between them.
 * Each galaxy can have at most one wormhole. When a player travels through a
 * wormhole, their galaxy binding is updated to the destination galaxy.
 * </p>
 */
@Service
public class WormholeConnectionService {

    private static final Logger log = LoggerFactory.getLogger(WormholeConnectionService.class);
    private static final Random random = new Random();

    private final WormholeRepository wormholeRepository;
    private final GalaxyRepository galaxyRepository;
    private final SolarSystemRepository solarSystemRepository;
    private final UserRepository userRepository;
    private final JdbcTemplate jdbcTemplate;
    private final PlayerCharacterService playerCharacterService;
    private final GalaxyEventService galaxyEventService;
    private final GalaxyMetricsService galaxyMetricsService;

    public WormholeConnectionService(WormholeRepository wormholeRepository,
                                     GalaxyRepository galaxyRepository,
                                     SolarSystemRepository solarSystemRepository,
                                     UserRepository userRepository,
                                     JdbcTemplate jdbcTemplate,
                                     PlayerCharacterService playerCharacterService,
                                     GalaxyEventService galaxyEventService,
                                     GalaxyMetricsService galaxyMetricsService) {
        this.wormholeRepository = wormholeRepository;
        this.galaxyRepository = galaxyRepository;
        this.solarSystemRepository = solarSystemRepository;
        this.userRepository = userRepository;
        this.jdbcTemplate = jdbcTemplate;
        this.playerCharacterService = playerCharacterService;
        this.galaxyEventService = galaxyEventService;
        this.galaxyMetricsService = galaxyMetricsService;
    }

    /**
     * Creates a wormhole pair connecting two galaxies.
     * <p>
     * Both galaxies must exist. If a galaxy already has a wormhole, the existing
     * wormhole is replaced. Solar system IDs are auto-selected if not provided.
     * </p>
     *
     * @param request the pair request containing both galaxy names
     * @return the connection DTO with both wormholes
     */
    @Transactional
    public WormholeConnectionDto createWormholePair(WormholePairRequest request) {
        String galaxyA = request.getGalaxyA();
        String galaxyB = request.getGalaxyB();

        if (galaxyA == null || galaxyA.isBlank()) {
            throw new IllegalArgumentException("Galaxy A name is required");
        }
        if (galaxyB == null || galaxyB.isBlank()) {
            throw new IllegalArgumentException("Galaxy B name is required");
        }
        if (galaxyA.equals(galaxyB)) {
            throw new IllegalArgumentException("Galaxy A and Galaxy B must be different");
        }

        // Verify both galaxies exist
        Optional<Galaxy> galaxyAOpt = galaxyRepository.findByName(galaxyA);
        Optional<Galaxy> galaxyBOpt = galaxyRepository.findByName(galaxyB);

        if (galaxyAOpt.isEmpty()) {
            throw new IllegalArgumentException("Galaxy '" + galaxyA + "' does not exist");
        }
        if (galaxyBOpt.isEmpty()) {
            throw new IllegalArgumentException("Galaxy '" + galaxyB + "' does not exist");
        }

        Galaxy galA = galaxyAOpt.get();
        Galaxy galB = galaxyBOpt.get();

        // Delete existing wormholes for these galaxies if any
        wormholeRepository.findByGalaxyName(galaxyA).ifPresent(w -> wormholeRepository.delete(w));
        wormholeRepository.findByGalaxyName(galaxyB).ifPresent(w -> wormholeRepository.delete(w));

        // Select solar systems for wormhole placement
        SolarSystem systemA = selectSolarSystem(galA.getId(), request.getSolarSystemIdA());
        SolarSystem systemB = selectSolarSystem(galB.getId(), request.getSolarSystemIdB());

        // Create wormhole A
        Wormhole wormholeA = new Wormhole();
        wormholeA.setGalaxyName(galaxyA);
        wormholeA.setSolarSystemId(systemA.getId());
        wormholeA.setSectorRow(systemA.getSectorRow());
        wormholeA.setSectorCol(systemA.getSectorCol());
        wormholeA.setSubSectorRow(systemA.getSubSectorRow());
        wormholeA.setSubSectorCol(systemA.getSubSectorCol());
        wormholeA.setPosX(systemA.getPosX());
        wormholeA.setPosY(systemA.getPosY());
        wormholeA.setPosZ(systemA.getPosZ());
        wormholeA.setName(request.getNameA() != null ? request.getNameA() : galaxyA + " Gateway");
        wormholeA.setActive(true);
        wormholeA = wormholeRepository.save(wormholeA);

        // Create wormhole B
        Wormhole wormholeB = new Wormhole();
        wormholeB.setGalaxyName(galaxyB);
        wormholeB.setSolarSystemId(systemB.getId());
        wormholeB.setSectorRow(systemB.getSectorRow());
        wormholeB.setSectorCol(systemB.getSectorCol());
        wormholeB.setSubSectorRow(systemB.getSubSectorRow());
        wormholeB.setSubSectorCol(systemB.getSubSectorCol());
        wormholeB.setPosX(systemB.getPosX());
        wormholeB.setPosY(systemB.getPosY());
        wormholeB.setPosZ(systemB.getPosZ());
        wormholeB.setName(request.getNameB() != null ? request.getNameB() : galaxyB + " Gateway");
        wormholeB.setActive(true);
        wormholeB = wormholeRepository.save(wormholeB);

        // Link the wormholes
        wormholeA.setConnectedWormholeId(wormholeB.getId());
        wormholeB.setConnectedWormholeId(wormholeA.getId());
        wormholeRepository.save(wormholeA);
        wormholeRepository.save(wormholeB);

        log.info("Created wormhole pair: {} ({}) <-> {} ({})", galaxyA, wormholeA.getId(), galaxyB, wormholeB.getId());

        // Publish event and metrics
        galaxyEventService.publish(GalaxyEventType.WORMHOLE_CREATED, galaxyA, null,
                "galaxyA", galaxyA, "galaxyB", galaxyB,
                "wormholeIdA", String.valueOf(wormholeA.getId()), "wormholeIdB", String.valueOf(wormholeB.getId()));
        galaxyEventService.publish(GalaxyEventType.WORMHOLE_CREATED, galaxyB, null,
                "galaxyA", galaxyA, "galaxyB", galaxyB,
                "wormholeIdA", String.valueOf(wormholeA.getId()), "wormholeIdB", String.valueOf(wormholeB.getId()));
        galaxyMetricsService.incrementWormholeCreated();

        return toConnectionDto(wormholeA, wormholeB);
    }

    /**
     * Travels through a wormhole from the source galaxy to the connected galaxy.
     * <p>
     * Validates that the wormhole exists and is active, finds the destination,
     * and updates the player's galaxy binding.
     * </p>
     *
     * @param playerName the player initiating travel
     * @param fromGalaxy the galaxy the player is currently in
     * @return the destination galaxy name
     */
    @Transactional
    public String travelThroughWormhole(String playerName, String fromGalaxy) {
        if (playerName == null || playerName.isBlank()) {
            throw new IllegalArgumentException("Player name is required");
        }
        if (fromGalaxy == null || fromGalaxy.isBlank()) {
            throw new IllegalArgumentException("Source galaxy is required");
        }

        // Find the wormhole in the source galaxy
        Optional<Wormhole> wormholeOpt = wormholeRepository.findByGalaxyName(fromGalaxy);
        if (wormholeOpt.isEmpty()) {
            throw new IllegalStateException("No wormhole found in galaxy '" + fromGalaxy + "'");
        }

        Wormhole wormhole = wormholeOpt.get();
        if (!wormhole.isActive()) {
            throw new IllegalStateException("Wormhole in galaxy '" + fromGalaxy + "' is currently inactive");
        }

        // Find the connected wormhole
        if (wormhole.getConnectedWormholeId() == null) {
            throw new IllegalStateException("Wormhole in galaxy '" + fromGalaxy + "' is not connected to another galaxy");
        }

        Optional<Wormhole> connectedOpt = wormholeRepository.findByConnectedWormholeId(wormhole.getConnectedWormholeId());
        if (connectedOpt.isEmpty()) {
            throw new IllegalStateException("Connected wormhole not found for galaxy '" + fromGalaxy + "'");
        }

        Wormhole destinationWormhole = connectedOpt.get();
        if (!destinationWormhole.isActive()) {
            throw new IllegalStateException("Destination wormhole in galaxy '" + destinationWormhole.getGalaxyName() + "' is inactive");
        }

        String toGalaxy = destinationWormhole.getGalaxyName();

        // Update player's galaxy binding and move crew AI
        Optional<User> userOpt = userRepository.findByUsername(playerName);
        userOpt.ifPresent(user -> {
            String previousGalaxy = user.getGalaxyName();
            user.setGalaxyName(toGalaxy);
            userRepository.save(user);

            // Move crew AI characters with the player
            int crewMoved = playerCharacterService.moveCrewToGalaxy(user, previousGalaxy, toGalaxy);
            if (crewMoved > 0) {
                log.info("Moved {} crew members with player '{}'", crewMoved, playerName);
            }
        });

        // Log the travel event
        logTravelEvent(playerName, fromGalaxy, toGalaxy, "PLAYER");

        // Publish event and metrics
        galaxyEventService.publish(GalaxyEventType.WORMHOLE_TRAVEL, fromGalaxy, null,
                "playerName", playerName, "fromGalaxy", fromGalaxy, "toGalaxy", toGalaxy);
        galaxyEventService.publish(GalaxyEventType.WORMHOLE_TRAVEL, toGalaxy, null,
                "playerName", playerName, "fromGalaxy", fromGalaxy, "toGalaxy", toGalaxy);
        galaxyMetricsService.incrementWormholeTravel();

        log.info("Player '{}' traveled from '{}' to '{}' through wormhole", playerName, fromGalaxy, toGalaxy);

        return toGalaxy;
    }

    /**
     * Returns all galaxies connected by wormholes to the given galaxy.
     *
     * @param galaxyName the galaxy to check
     * @return list of connected galaxy names
     */
    @Transactional(readOnly = true)
    public List<String> getConnectedGalaxies(String galaxyName) {
        Optional<Wormhole> wormholeOpt = wormholeRepository.findByGalaxyName(galaxyName);
        if (wormholeOpt.isEmpty()) {
            return List.of();
        }

        Wormhole wormhole = wormholeOpt.get();
        if (wormhole.getConnectedWormholeId() == null) {
            return List.of();
        }

        Optional<Wormhole> connectedOpt = wormholeRepository.findByConnectedWormholeId(wormhole.getConnectedWormholeId());
        if (connectedOpt.isEmpty()) {
            return List.of();
        }

        return List.of(connectedOpt.get().getGalaxyName());
    }

    /**
     * Lists all wormhole connections (pairs) in the system.
     *
     * @return list of all wormhole connections
     */
    @Transactional(readOnly = true)
    public List<WormholeConnectionDto> listAllConnections() {
        List<Wormhole> allWormholes = wormholeRepository.findAll();
        List<WormholeConnectionDto> connections = new ArrayList<>();
        List<Long> processed = new ArrayList<>();

        for (Wormhole w : allWormholes) {
            if (processed.contains(w.getId())) {
                continue;
            }

            if (w.getConnectedWormholeId() != null) {
                Optional<Wormhole> connectedOpt = wormholeRepository.findById(w.getConnectedWormholeId());
                connectedOpt.ifPresent(connected -> {
                    WormholeConnectionDto dto = toConnectionDto(w, connected);
                    connections.add(dto);
                    processed.add(w.getId());
                    processed.add(connected.getId());
                });
            }
        }

        return connections;
    }

    /**
     * Toggles the active state of a wormhole in the given galaxy.
     * <p>
     * Toggling one wormhole in a pair also toggles its connected counterpart,
     * ensuring both sides are always in sync.
     * </p>
     *
     * @param galaxyName the galaxy containing the wormhole to toggle
     * @return the updated wormhole info DTO
     */
    @Transactional
    public WormholeInfoDto toggleWormhole(String galaxyName) {
        Optional<Wormhole> wormholeOpt = wormholeRepository.findByGalaxyName(galaxyName);
        if (wormholeOpt.isEmpty()) {
            throw new IllegalStateException("No wormhole found in galaxy '" + galaxyName + "'");
        }

        Wormhole wormhole = wormholeOpt.get();
        boolean newState = !wormhole.isActive();
        wormhole.setActive(newState);
        wormholeRepository.save(wormhole);

        // Also toggle the connected wormhole
        if (wormhole.getConnectedWormholeId() != null) {
            wormholeRepository.findById(wormhole.getConnectedWormholeId()).ifPresent(connected -> {
                connected.setActive(newState);
                wormholeRepository.save(connected);
            });
        }

        log.info("Toggled wormhole in galaxy '{}' to active={}", galaxyName, newState);

        // Publish event
        final String[] connectedGalaxyHolder = new String[1];
        if (wormhole.getConnectedWormholeId() != null) {
            wormholeRepository.findById(wormhole.getConnectedWormholeId())
                    .ifPresent(connected -> connectedGalaxyHolder[0] = connected.getGalaxyName());
        }
        galaxyEventService.publish(GalaxyEventType.WORMHOLE_TOGGLED, galaxyName, null,
                "galaxyName", galaxyName, "isActive", String.valueOf(newState),
                "connectedGalaxyName", connectedGalaxyHolder[0] != null ? connectedGalaxyHolder[0] : "");

        return toDto(wormhole);
    }

    /**
     * Gets the wormhole for a specific galaxy.
     *
     * @param galaxyName the galaxy name
     * @return optional wormhole info DTO
     */
    @Transactional(readOnly = true)
    public Optional<WormholeInfoDto> getWormhole(String galaxyName) {
        return wormholeRepository.findByGalaxyName(galaxyName).map(this::toDto);
    }

    /**
     * Lists all wormholes in the system.
     *
     * @return list of all wormhole info DTOs
     */
    @Transactional(readOnly = true)
    public List<WormholeInfoDto> listAllWormholes() {
        return wormholeRepository.findAll().stream()
                .map(this::toDto)
                .toList();
    }

    // ---- Private helpers ----

    private SolarSystem selectSolarSystem(Long galaxyId, Long requestedId) {
        if (requestedId != null) {
            return solarSystemRepository.findById(requestedId)
                    .orElseThrow(() -> new IllegalArgumentException("Solar system with id " + requestedId + " not found"));
        }

        // Auto-select: pick a random solar system from the galaxy
        List<SolarSystem> systems = solarSystemRepository.findByGalaxyId(galaxyId);
        if (systems.isEmpty()) {
            throw new IllegalStateException("No solar systems found in galaxy for wormhole placement");
        }
        return systems.get(random.nextInt(systems.size()));
    }

    private WormholeInfoDto toDto(Wormhole wormhole) {
        WormholeInfoDto dto = new WormholeInfoDto();
        dto.setId(wormhole.getId());
        dto.setGalaxyName(wormhole.getGalaxyName());
        dto.setSolarSystemId(wormhole.getSolarSystemId());
        dto.setSectorRow(wormhole.getSectorRow());
        dto.setSectorCol(wormhole.getSectorCol());
        dto.setSubSectorRow(wormhole.getSubSectorRow());
        dto.setSubSectorCol(wormhole.getSubSectorCol());
        dto.setPosX(wormhole.getPosX());
        dto.setPosY(wormhole.getPosY());
        dto.setPosZ(wormhole.getPosZ());
        dto.setName(wormhole.getName());
        dto.setActive(wormhole.isActive());
        dto.setConnectedWormholeId(wormhole.getConnectedWormholeId());
        dto.setCreatedAt(wormhole.getCreatedAt());

        // Resolve connected galaxy name
        if (wormhole.getConnectedWormholeId() != null) {
            wormholeRepository.findById(wormhole.getConnectedWormholeId())
                    .ifPresent(connected -> dto.setConnectedGalaxyName(connected.getGalaxyName()));
        }

        return dto;
    }

    private WormholeConnectionDto toConnectionDto(Wormhole wormholeA, Wormhole wormholeB) {
        WormholeConnectionDto dto = new WormholeConnectionDto();
        dto.setWormholeA(toDto(wormholeA));
        dto.setWormholeB(toDto(wormholeB));
        dto.setGalaxyA(wormholeA.getGalaxyName());
        dto.setGalaxyB(wormholeB.getGalaxyName());
        return dto;
    }

    private void logTravelEvent(String playerName, String fromGalaxy, String toGalaxy, String initiatedBy) {
        try {
            jdbcTemplate.update(
                    "INSERT INTO galaxy_travel_log (player_name, from_galaxy, to_galaxy, travel_timestamp, initiated_by) VALUES (?, ?, ?, ?, ?)",
                    playerName, fromGalaxy, toGalaxy, LocalDateTime.now().toString(), initiatedBy);
        } catch (Exception e) {
            log.warn("Failed to log travel event for player '{}': {}", playerName, e.getMessage());
        }
    }
}
