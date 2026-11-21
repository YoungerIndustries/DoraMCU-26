package com.yisy.modules.doramcu.hex.planet.controller;

import com.yisy.modules.doramcu.hex.Hex3D;
import com.yisy.modules.doramcu.hex.Planet;
import com.yisy.modules.doramcu.hex.planet.DefaultPlanetSurfaceBuilder;
import com.yisy.modules.doramcu.hex.planet.PlanetSurfacePersistenceService;
import com.yisy.modules.doramcu.hex.planet.Station;
import com.yisy.modules.doramcu.hex.planet.SurfaceVehicle;
import com.yisy.modules.doramcu.hex.planet.TerrainData;
import com.yisy.modules.doramcu.hex.planet.TransportCorporation;
import com.yisy.modules.doramcu.hex.planet.VehicleType;
import com.yisy.modules.doramcu.hex.planet.depot.VehicleDepot;
import com.yisy.modules.doramcu.hex.planet.depot.VehicleDepotService;
import com.yisy.modules.doramcu.hex.planet.depot.dto.DepotVehicleDto;
import com.yisy.modules.doramcu.hex.planet.pathfinding.InfrastructurePathfinder;
import com.yisy.modules.doramcu.hex.planet.pathfinding.dto.PathResultDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * REST controller for TTDL (Transport Tycoon Deluxe Like) depot and pathfinding operations.
 * Handles infrastructure building, pathfinding, depot management, vehicle operations,
 * and station building. Extracted from HexPlanetSurfaceController to reduce god class size.
 */
@RestController
@RequestMapping("/api/v1/planets")
public class HexTTDLDepotController {

    private static final Logger log = LoggerFactory.getLogger(HexTTDLDepotController.class);

    private final DefaultPlanetSurfaceBuilder planetSurfaceBuilder;
    private final PlanetSurfacePersistenceService persistenceService;
    private final VehicleDepotService vehicleDepotService;
    private final InfrastructurePathfinder infrastructurePathfinder;

    public HexTTDLDepotController(
            DefaultPlanetSurfaceBuilder planetSurfaceBuilder,
            PlanetSurfacePersistenceService persistenceService,
            VehicleDepotService vehicleDepotService,
            InfrastructurePathfinder infrastructurePathfinder) {
        this.planetSurfaceBuilder = planetSurfaceBuilder;
        this.persistenceService = persistenceService;
        this.vehicleDepotService = vehicleDepotService;
        this.infrastructurePathfinder = infrastructurePathfinder;
    }

    // ==================== TTDL Depot & Pathfinding Endpoints ====================

    /**
     * Build infrastructure (road/rail) on a hex.
     *
     * @param planetId the planet ID
     * @param hexX x coordinate
     * @param hexY y coordinate
     * @param infraType infrastructure type (ROAD, RAIL, STATION, PORT, AIRPORT)
     * @return the updated terrain data
     */
    @PostMapping("/{planetId}/infrastructure/build")
    public ResponseEntity<Map<String, Object>> buildInfrastructure(
            @PathVariable int planetId,
            @RequestParam int hexX,
            @RequestParam int hexY,
            @RequestParam TerrainData.InfrastructureType infraType) {

        Hex3D hex = new Hex3D(hexX, hexY);
        boolean built = planetSurfaceBuilder.buildInfrastructure(hex, infraType, null);
        if (!built) {
            return ResponseEntity.badRequest().body(Map.of("error", "Failed to build infrastructure at hex " + hex));
        }

        // Update pathfinder graph
        infrastructurePathfinder.updateNode(planetId, hex, infraType);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "hex", hex.toString(),
                "infrastructure", infraType.name()
        ));
    }

    /**
     * Find the shortest path between two stations.
     *
     * @param planetId the planet ID
     * @param startStationId the starting station ID
     * @param endStationId the destination station ID
     * @param vehicleType optional vehicle type for infrastructure validation
     * @return the path result
     */
    @GetMapping("/{planetId}/pathfinding/find")
    public ResponseEntity<PathResultDto> findPath(
            @PathVariable int planetId,
            @RequestParam String startStationId,
            @RequestParam String endStationId,
            @RequestParam(required = false) VehicleType vehicleType) {

        Planet planet = new Planet(planetId);
        List<Station> stations = planetSurfaceBuilder.getAllStations(planet);
        if (stations.isEmpty() && persistenceService.hasPlanetData(planetId)) {
            persistenceService.loadPlanetSurface(planet, planetSurfaceBuilder);
            stations = planetSurfaceBuilder.getAllStations(planet);
        }
        Station start = null, end = null;
        for (Station s : stations) {
            if (s.getId().equals(startStationId)) start = s;
            if (s.getId().equals(endStationId)) end = s;
        }

        if (start == null) {
            return ResponseEntity.badRequest().body(
                    new PathResultDto("Start station " + startStationId + " not found"));
        }
        if (end == null) {
            return ResponseEntity.badRequest().body(
                    new PathResultDto("End station " + endStationId + " not found"));
        }

        PathResultDto result = infrastructurePathfinder.findPath(planetId, start, end, vehicleType);
        return ResponseEntity.ok(result);
    }

    /**
     * Create a depot at a station.
     *
     * @param planetId the planet ID
     * @param stationId the station ID
     * @param corporationId the corporation ID
     * @return the created depot info
     */
    @PostMapping("/{planetId}/depot/create")
    public ResponseEntity<Map<String, Object>> createDepot(
            @PathVariable int planetId,
            @RequestParam String stationId,
            @RequestParam String corporationId) {

        Planet planet = new Planet(planetId);
        List<Station> stations = planetSurfaceBuilder.getAllStations(planet);
        if (stations.isEmpty() && persistenceService.hasPlanetData(planetId)) {
            persistenceService.loadPlanetSurface(planet, planetSurfaceBuilder);
            stations = planetSurfaceBuilder.getAllStations(planet);
        }
        Station station = null;
        for (Station s : stations) {
            if (s.getId().equals(stationId)) {
                station = s;
                break;
            }
        }

        if (station == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Station " + stationId + " not found"));
        }

        VehicleDepot depot = vehicleDepotService.createDepot(station, corporationId);

        return ResponseEntity.ok(Map.of(
                "depotId", depot.getId(),
                "stationId", depot.getStationId(),
                "availableTypes", depot.getAvailableTypes().stream()
                        .map(VehicleType::getDisplayName).collect(Collectors.toList())
        ));
    }

    /**
     * Purchase a vehicle at a depot.
     *
     * @param planetId the planet ID
     * @param depotId the depot ID
     * @param vehicleTypeName the vehicle type name (e.g., "TRUCK", "FREIGHT_TRAIN")
     * @param corporationId the corporation ID
     * @return the purchased vehicle info
     */
    @PostMapping("/{planetId}/depot/purchase")
    public ResponseEntity<Map<String, Object>> purchaseVehicle(
            @PathVariable int planetId,
            @RequestParam String depotId,
            @RequestParam String vehicleTypeName,
            @RequestParam String corporationId) {

        // Parse vehicle type
        VehicleType vehicleType;
        try {
            vehicleType = VehicleType.valueOf(vehicleTypeName.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", "Unknown vehicle type: " + vehicleTypeName));
        }

        // Find corporation
        Planet planet = new Planet(planetId);
        List<TransportCorporation> corporations = planetSurfaceBuilder.getAllCorporations(planet);
        if (corporations.isEmpty() && persistenceService.hasPlanetData(planetId)) {
            persistenceService.loadPlanetSurface(planet, planetSurfaceBuilder);
            corporations = planetSurfaceBuilder.getAllCorporations(planet);
        }
        TransportCorporation corporation = null;
        for (TransportCorporation c : corporations) {
            if (c.getId().equals(corporationId)) {
                corporation = c;
                break;
            }
        }

        if (corporation == null) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", "Corporation " + corporationId + " not found"));
        }

        SurfaceVehicle vehicle = vehicleDepotService.purchaseVehicle(depotId, vehicleType, corporation);
        if (vehicle == null) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Failed to purchase vehicle. Check depot availability and corporation credits."));
        }

        return ResponseEntity.ok(Map.of(
                "vehicleId", vehicle.getId(),
                "type", vehicle.getType().getDisplayName(),
                "position", vehicle.getCurrentPosition().toString(),
                "cost", vehicle.getType().getConstructionCost()
        ));
    }

    /**
     * Assign a vehicle to a route.
     *
     * @param planetId the planet ID
     * @param vehicleId the vehicle ID
     * @param stationIds ordered list of station IDs for the route
     * @return the assignment result
     */
    @PostMapping("/{planetId}/depot/assign")
    public ResponseEntity<Map<String, Object>> assignVehicle(
            @PathVariable int planetId,
            @RequestParam String vehicleId,
            @RequestParam List<String> stationIds) {

        Planet planet = new Planet(planetId);
        List<Station> stations = planetSurfaceBuilder.getAllStations(planet);
        if (stations.isEmpty() && persistenceService.hasPlanetData(planetId)) {
            persistenceService.loadPlanetSurface(planet, planetSurfaceBuilder);
            stations = planetSurfaceBuilder.getAllStations(planet);
        }
        Map<String, Station> stationMap = new HashMap<>();
        for (Station s : stations) {
            stationMap.put(s.getId(), s);
        }

        boolean success = vehicleDepotService.assignToRoute(vehicleId, stationIds, stationMap, planetId);
        if (!success) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Failed to assign vehicle. Check route stations and path connectivity."));
        }

        return ResponseEntity.ok(Map.of(
                "success", true,
                "vehicleId", vehicleId,
                "route", stationIds
        ));
    }

    /**
     * Get all vehicles in a depot.
     *
     * @param depotId the depot ID
     * @return list of vehicle DTOs
     */
    @GetMapping("/depot/{depotId}/vehicles")
    public ResponseEntity<List<DepotVehicleDto>> getDepotVehicles(
            @PathVariable String depotId) {

        List<DepotVehicleDto> vehicles = vehicleDepotService.getDepotVehicles(depotId);
        return ResponseEntity.ok(vehicles);
    }

    /**
     * Get all available vehicle types for purchase.
     *
     * @return list of vehicle type info
     */
    @GetMapping("/depot/vehicle-types")
    public ResponseEntity<List<Map<String, Object>>> getVehicleTypes() {
        List<Map<String, Object>> types = new ArrayList<>();
        for (VehicleType vt : vehicleDepotService.getAvailableVehicleTypes()) {
            types.add(Map.of(
                    "name", vt.name(),
                    "displayName", vt.getDisplayName(),
                    "speed", vt.getSpeed(),
                    "cargoCapacity", vt.getCargoCapacity(),
                    "passengerCapacity", vt.getPassengerCapacity(),
                    "constructionCost", vt.getConstructionCost(),
                    "maintenanceCost", vt.getMaintenanceCost(),
                    "requiredInfrastructure", vt.getRequiredInfrastructure().name(),
                    "description", vt.getDescription()
            ));
        }
        return ResponseEntity.ok(types);
    }

    /**
     * Sell a vehicle.
     *
     * @param planetId the planet ID
     * @param vehicleId the vehicle ID
     * @param corporationId the corporation ID
     * @return the sale result
     */
    @PostMapping("/{planetId}/depot/sell")
    public ResponseEntity<Map<String, Object>> sellVehicle(
            @PathVariable int planetId,
            @RequestParam String vehicleId,
            @RequestParam String corporationId) {

        Planet planet = new Planet(planetId);
        List<TransportCorporation> corporations = planetSurfaceBuilder.getAllCorporations(planet);
        if (corporations.isEmpty() && persistenceService.hasPlanetData(planetId)) {
            persistenceService.loadPlanetSurface(planet, planetSurfaceBuilder);
            corporations = planetSurfaceBuilder.getAllCorporations(planet);
        }
        TransportCorporation corporation = null;
        for (TransportCorporation c : corporations) {
            if (c.getId().equals(corporationId)) {
                corporation = c;
                break;
            }
        }

        if (corporation == null) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", "Corporation " + corporationId + " not found"));
        }

        long refund = vehicleDepotService.sellVehicle(vehicleId, corporation);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "vehicleId", vehicleId,
                "refund", refund
        ));
    }

    /**
     * Rebuild the infrastructure graph for a planet.
     *
     * @param planetId the planet ID
     * @return the rebuild result
     */
    @PostMapping("/{planetId}/infrastructure/rebuild-graph")
    public ResponseEntity<Map<String, Object>> rebuildGraph(@PathVariable int planetId) {
        Map<String, TerrainData> terrainMap = planetSurfaceBuilder.getAllTerrainData();
        infrastructurePathfinder.buildGraph(planetId, terrainMap);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "planetId", planetId,
                "message", "Infrastructure graph rebuilt"
        ));
    }

    /**
     * Build a new station at a given hex position.
     *
     * @param planetId the planet ID
     * @param name the station name
     * @param hexX x coordinate of the hex
     * @param hexY y coordinate of the hex
     * @param empireId optional empire/corporation ID that owns the station
     * @param stationType optional station type (BUS_STOP, RAIL_STATION, PORT, AIRPORT, SPACEPORT, etc.)
     * @return the created station info
     */
    @PostMapping("/{planetId}/station/build")
    public ResponseEntity<Map<String, Object>> buildStation(
            @PathVariable int planetId,
            @RequestParam String name,
            @RequestParam int hexX,
            @RequestParam int hexY,
            @RequestParam(required = false, defaultValue = "overlord") String empireId,
            @RequestParam(required = false, defaultValue = "GENERAL") String stationType) {

        Planet planet = new Planet(planetId);

        // Ensure terrain data is loaded
        if (planetSurfaceBuilder.getAllTerrainData().isEmpty() && persistenceService.hasPlanetData(planetId)) {
            persistenceService.loadPlanetSurface(planet, planetSurfaceBuilder);
        }

        Hex3D hex = new Hex3D(hexX, hexY);
        TerrainData terrain = planetSurfaceBuilder.getTerrainData(hex);
        if (terrain == null) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "No terrain data at hex (" + hexX + ", " + hexY + "). Generate the planet first."));
        }

        Station.StationType type = Station.StationType.fromName(stationType);
        Station station = planetSurfaceBuilder.buildStation(planet, name, hex, empireId, type);

        // Mark the terrain as having the appropriate infrastructure type
        TerrainData.InfrastructureType infraType = type.getInfrastructureType();
        planetSurfaceBuilder.buildInfrastructure(hex, infraType, null);
        infrastructurePathfinder.updateNode(planetId, hex, infraType);

        // Persist the changes
        persistenceService.savePlanetSurface(planet, planetSurfaceBuilder);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "stationId", station.getId(),
                "name", station.getName(),
                "type", type.getDisplayName(),
                "hex", hex.toString(),
                "owner", empireId,
                "buildCost", station.getBuildCost(),
                "maintenanceCost", station.getMaintenanceCost(),
                "maxPassengers", station.getMaxPassengers(),
                "maxCargo", station.getMaxCargo()
        ));
    }

    /**
     * List all available station types.
     *
     * @return list of station types with their stats
     */
    @GetMapping("/station/types")
    public ResponseEntity<List<Map<String, Object>>> getStationTypes() {
        List<Map<String, Object>> types = new ArrayList<>();
        for (Station.StationType type : Station.StationType.values()) {
            types.add(Map.of(
                    "name", type.name(),
                    "displayName", type.getDisplayName(),
                    "buildCost", type.getBuildCost(),
                    "maintenanceCost", type.getMaintenanceCost(),
                    "catchmentRadius", type.getCatchmentRadius(),
                    "maxPassengers", type.getMaxPassengers(),
                    "maxCargo", type.getMaxCargo(),
                    "infrastructureType", type.getInfrastructureType().name()
            ));
        }
        return ResponseEntity.ok(types);
    }
}
