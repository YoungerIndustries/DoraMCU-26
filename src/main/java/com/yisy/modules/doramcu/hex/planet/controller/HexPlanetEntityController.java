package com.yisy.modules.doramcu.hex.planet.controller;

import com.yisy.modules.doramcu.gamecore.DungeonRoomService;
import com.yisy.modules.doramcu.gamecore.DungeonRoomService.DungeonRoom;
import com.yisy.modules.doramcu.hex.Planet;
import com.yisy.modules.doramcu.hex.planet.DefaultPlanetSurfaceBuilder;
import com.yisy.modules.doramcu.hex.planet.Industry;
import com.yisy.modules.doramcu.hex.planet.PlanetSurfacePersistenceService;
import com.yisy.modules.doramcu.hex.planet.Settlement;
import com.yisy.modules.doramcu.hex.planet.Station;
import com.yisy.modules.doramcu.hex.planet.SurfaceVehicle;
import com.yisy.modules.doramcu.hex.planet.TransportCorporation;
import com.yisy.modules.doramcu.hex.planet.dto.CorporationDto;
import com.yisy.modules.doramcu.hex.planet.dto.IndustryDto;
import com.yisy.modules.doramcu.hex.planet.dto.SettlementDto;
import com.yisy.modules.doramcu.hex.planet.dto.StationDto;
import com.yisy.modules.doramcu.hex.planet.dto.VehicleDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * REST controller for planet entity lists (settlements, industries, stations, vehicles, corporations)
 * and interior dungeon rooms.
 * Extracted from HexPlanetSurfaceController to reduce god class size.
 */
@RestController
@RequestMapping("/api/v1/planets")
public class HexPlanetEntityController {

    private static final Logger log = LoggerFactory.getLogger(HexPlanetEntityController.class);

    private final DefaultPlanetSurfaceBuilder planetSurfaceBuilder;
    private final PlanetSurfacePersistenceService persistenceService;
    private final DungeonRoomService dungeonRoomService;

    public HexPlanetEntityController(
            DefaultPlanetSurfaceBuilder planetSurfaceBuilder,
            PlanetSurfacePersistenceService persistenceService,
            DungeonRoomService dungeonRoomService) {
        this.planetSurfaceBuilder = planetSurfaceBuilder;
        this.persistenceService = persistenceService;
        this.dungeonRoomService = dungeonRoomService;
    }

    // ---- Entity List Endpoints ----

    /**
     * Get all settlements on a planet.
     */
    @GetMapping("/{planetId}/settlements")
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public ResponseEntity<List<SettlementDto>> getSettlements(@PathVariable int planetId) {
        log.info("GET /api/v1/planets/{}/settlements", planetId);

        Planet planet = new Planet(planetId);
        List<Settlement> settlements = planetSurfaceBuilder.getAllSettlements(planet);

        if (settlements.isEmpty()) {
            if (persistenceService.hasPlanetData(planetId)) {
                persistenceService.loadPlanetSurface(planet, planetSurfaceBuilder);
                settlements = planetSurfaceBuilder.getAllSettlements(planet);
            }
        }

        List<SettlementDto> dtos = settlements.stream()
                .map(HexPlanetDtoMapper::toSettlementDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    /**
     * Get all industries on a planet.
     */
    @GetMapping("/{planetId}/industries")
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public ResponseEntity<List<IndustryDto>> getIndustries(@PathVariable int planetId) {
        log.info("GET /api/v1/planets/{}/industries", planetId);

        Planet planet = new Planet(planetId);
        List<Industry> industries = planetSurfaceBuilder.getAllIndustries(planet);

        if (industries.isEmpty()) {
            if (persistenceService.hasPlanetData(planetId)) {
                persistenceService.loadPlanetSurface(planet, planetSurfaceBuilder);
                industries = planetSurfaceBuilder.getAllIndustries(planet);
            }
        }

        List<IndustryDto> dtos = industries.stream()
                .map(HexPlanetDtoMapper::toIndustryDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    /**
     * Get all stations on a planet.
     */
    @GetMapping("/{planetId}/stations")
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public ResponseEntity<List<StationDto>> getStations(@PathVariable int planetId) {
        log.info("GET /api/v1/planets/{}/stations", planetId);

        Planet planet = new Planet(planetId);
        List<Station> stations = planetSurfaceBuilder.getAllStations(planet);

        if (stations.isEmpty()) {
            if (persistenceService.hasPlanetData(planetId)) {
                persistenceService.loadPlanetSurface(planet, planetSurfaceBuilder);
                stations = planetSurfaceBuilder.getAllStations(planet);
            }
        }

        List<StationDto> dtos = stations.stream()
                .map(HexPlanetDtoMapper::toStationDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    /**
     * Get all vehicles on a planet.
     */
    @GetMapping("/{planetId}/vehicles")
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public ResponseEntity<List<VehicleDto>> getVehicles(@PathVariable int planetId) {
        log.info("GET /api/v1/planets/{}/vehicles", planetId);

        Planet planet = new Planet(planetId);
        List<SurfaceVehicle> vehicles = planetSurfaceBuilder.getAllVehicles(planet);

        if (vehicles.isEmpty()) {
            if (persistenceService.hasPlanetData(planetId)) {
                persistenceService.loadPlanetSurface(planet, planetSurfaceBuilder);
                vehicles = planetSurfaceBuilder.getAllVehicles(planet);
            }
        }

        List<VehicleDto> dtos = vehicles.stream()
                .map(HexPlanetDtoMapper::toVehicleDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    /**
     * Get all corporations operating on a planet.
     */
    @GetMapping("/{planetId}/corporations")
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public ResponseEntity<List<CorporationDto>> getCorporations(@PathVariable int planetId) {
        log.info("GET /api/v1/planets/{}/corporations", planetId);

        Planet planet = new Planet(planetId);
        List<TransportCorporation> corporations = planetSurfaceBuilder.getAllCorporations(planet);

        if (corporations.isEmpty()) {
            if (persistenceService.hasPlanetData(planetId)) {
                persistenceService.loadPlanetSurface(planet, planetSurfaceBuilder);
                corporations = planetSurfaceBuilder.getAllCorporations(planet);
            }
        }

        List<CorporationDto> dtos = corporations.stream()
                .map(HexPlanetDtoMapper::toCorporationDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    /**
     * Get the interior dungeon rooms for a specific building.
     */
    @GetMapping("/{planetId}/interiors/{dungeonId}")
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public ResponseEntity<List<Map<String, Object>>> getInteriorRooms(
            @PathVariable int planetId, @PathVariable String dungeonId) {
        log.info("GET /api/v1/planets/{}/interiors/{}", planetId, dungeonId);

        List<DungeonRoom> rooms = dungeonRoomService.listRoomsByDungeon(dungeonId);

        if (rooms.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        List<Map<String, Object>> dtos = rooms.stream()
                .map(HexPlanetDtoMapper::toRoomMap)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

}
