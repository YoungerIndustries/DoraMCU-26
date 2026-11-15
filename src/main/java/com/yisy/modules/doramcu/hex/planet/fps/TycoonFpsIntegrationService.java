package com.yisy.modules.doramcu.hex.planet.fps;

import com.yisy.modules.doramcu.fps.FpsMapService;
import com.yisy.modules.doramcu.fps.core.FpsGameServer;
import com.yisy.modules.doramcu.fps.core.model.FpsMap;
import com.yisy.modules.doramcu.hex.Planet;
import com.yisy.modules.doramcu.hex.planet.DefaultPlanetSurfaceBuilder;
import com.yisy.modules.doramcu.hex.planet.fps.dto.FpsWorldStateDto;
import com.yisy.modules.doramcu.hex.planet.hullmodule.HullModuleService;
import com.yisy.modules.doramcu.hex.planet.military.MilitaryProductionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Integrates FpsGameServer with TycoonFpsBridgeService for planet-based battles.
 * <p>
 * This service:
 * <ul>
 *   <li>Converts planet terrain to FPS maps using Hex3D coordinates</li>
 *   <li>Places military units and structures in FPS battlefield</li>
 *   <li>Links FpsSession to planet ID for terrain-aware combat</li>
 *   <li>Generates spawn points from station and military base locations</li>
 *   <li>Creates capture points from strategic hull modules</li>
 * </ul>
 * </p>
 */
@Service
public class TycoonFpsIntegrationService {

    private static final Logger log = LoggerFactory.getLogger(TycoonFpsIntegrationService.class);

    private final TycoonFpsBridgeService fpsBridgeService;
    private final FpsMapService fpsMapService;
    private final FpsGameServer fpsGameServer;
    private final HullModuleService hullModuleService;
    private final MilitaryProductionService militaryProductionService;
    private final DefaultPlanetSurfaceBuilder planetSurfaceBuilder;

    public TycoonFpsIntegrationService(
            TycoonFpsBridgeService fpsBridgeService,
            FpsMapService fpsMapService,
            FpsGameServer fpsGameServer,
            HullModuleService hullModuleService,
            MilitaryProductionService militaryProductionService,
            DefaultPlanetSurfaceBuilder planetSurfaceBuilder) {
        this.fpsBridgeService = fpsBridgeService;
        this.fpsMapService = fpsMapService;
        this.fpsGameServer = fpsGameServer;
        this.hullModuleService = hullModuleService;
        this.militaryProductionService = militaryProductionService;
        this.planetSurfaceBuilder = planetSurfaceBuilder;
    }

    /**
     * Converts a planet's terrain and structures into an FPS map.
     * <p>
     * The map includes:
     * <ul>
     *   <li>Spawn points from stations and military bases</li>
     *   <li>Collision boxes from buildings and terrain obstacles</li>
     *   <li>Capture points from strategic hull modules</li>
     *   <li>Power-up locations from industries</li>
     * </ul>
     * </p>
     *
     * @param planet the planet to convert
     * @return the FPS map
     */
    public FpsMap convertPlanetToFpsMap(Planet planet) {
        if (planet == null) {
            throw new IllegalArgumentException("Planet cannot be null");
        }

        FpsWorldStateDto worldState = fpsBridgeService.getWorldState(planetSurfaceBuilder, planet.getId(), 0);
        FpsMap map = new FpsMap();
        map.setId("planet-" + planet.getId());
        map.setName(planet.getName() + " Battlefield");
        map.setDescription("FPS battlefield generated from planet surface: " + planet.getName());
        map.setTheme("planet");

        // Calculate map dimensions from hex grid
        double mapSize = calculateMapSize(planet);
        map.setMapWidth(mapSize);
        map.setMapDepth(mapSize);
        map.setMapHeight(100.0);

        // Generate spawn points from stations and military bases
        generateSpawnPoints(worldState, map);

        // Generate collision boxes from hull modules
        generateCollisionBoxes(worldState, map);

        // Generate capture points from strategic locations
        generateCapturePoints(worldState, map);

        // Generate power-up locations from industries
        generatePowerUpLocations(worldState, map);

        // Register the map with FpsMapService
        fpsMapService.addMap(map);
        log.info("Converted planet {} to FPS map with {} spawn points, {} collision boxes, {} capture points",
                planet.getName(), map.getSpawnPoints().size(),
                map.getCollisionBoxes().size(), map.getCapturePoints().size());

        return map;
    }

    /**
     * Starts a battle on a planet's surface.
     * <p>
     * Converts planet terrain to an FPS map and places military units.
     * </p>
     *
     * @param planet the planet
     * @param attackerFaction the attacking faction name
     * @param defenderFaction the defending faction name
     * @return the map ID
     */
    public String startBattle(Planet planet, String attackerFaction, String defenderFaction) {
        if (planet == null) {
            throw new IllegalArgumentException("Planet cannot be null");
        }

        // Convert planet to FPS map
        FpsMap map = convertPlanetToFpsMap(planet);

        // Get military units for both factions
        List<com.yisy.modules.doramcu.thirdreich.domain.ThirdReichGroundUnit> attackerUnits =
                militaryProductionService.getUnitsByFaction(attackerFaction);
        List<com.yisy.modules.doramcu.thirdreich.domain.ThirdReichGroundUnit> defenderUnits =
                militaryProductionService.getUnitsByFaction(defenderFaction);

        log.info("Starting battle on planet {}: {} vs {} (attacker: {} units, defender: {} units)",
                planet.getName(), attackerFaction, defenderFaction,
                attackerUnits.size(), defenderUnits.size());

        return map.getId();
    }

    /**
     * Gets the FPS map for a planet, creating one if it doesn't exist.
     *
     * @param planet the planet
     * @return the FPS map
     */
    public FpsMap getOrCreateMap(Planet planet) {
        Optional<FpsMap> existing = fpsMapService.getMap("planet-" + planet.getId());
        if (existing.isPresent()) {
            return existing.get();
        }
        return convertPlanetToFpsMap(planet);
    }

    /**
     * Calculates the map size based on the planet's terrain grid.
     */
    private double calculateMapSize(Planet planet) {
        // Default size based on planet radius
        int radius = 5; // default
        if (planet.getGeometry() != null) {
            radius = planet.getGeometry().getRadius();
        }
        // Scale: each hex is ~10 units, map covers radius * 2 hexes
        return radius * 10.0 * 2.0;
    }

    /**
     * Generates spawn points from stations and military bases.
     */
    private void generateSpawnPoints(FpsWorldStateDto worldState, FpsMap map) {
        int team = 0;

        // Stations become spawn points
        for (FpsWorldStateDto.FpsStationStateDto station : worldState.getStations()) {
            map.getSpawnPoints().add(new FpsMap.SpawnPoint(station.getX(), station.getY(), station.getZ(), team % 2));
            team++;
        }

        // Military hull modules become spawn points
        for (FpsWorldStateDto.FpsHullModuleStateDto module : worldState.getHullModules()) {
            if ("MILITARY".equals(module.getCategory())) {
                map.getSpawnPoints().add(new FpsMap.SpawnPoint(module.getX(), module.getY(), module.getZ(), team % 2));
                team++;
            }
        }

        // Fallback: default spawn points if no structures exist
        if (map.getSpawnPoints().isEmpty()) {
            double halfSize = map.getMapWidth() / 2.0;
            map.getSpawnPoints().add(new FpsMap.SpawnPoint(-halfSize, 0, -halfSize, 0));
            map.getSpawnPoints().add(new FpsMap.SpawnPoint(halfSize, 0, halfSize, 1));
            map.getSpawnPoints().add(new FpsMap.SpawnPoint(-halfSize, 0, halfSize, 0));
            map.getSpawnPoints().add(new FpsMap.SpawnPoint(halfSize, 0, -halfSize, 1));
        }
    }

    /**
     * Generates collision boxes from hull modules (buildings block movement).
     */
    private void generateCollisionBoxes(FpsWorldStateDto worldState, FpsMap map) {
        for (FpsWorldStateDto.FpsHullModuleStateDto module : worldState.getHullModules()) {
            double width = 10.0;
            double depth = 10.0;
            double height = module.getFloors() * 3.0;

            map.getCollisionBoxes().add(new FpsMap.CollisionBox(
                    module.getX() - width / 2, module.getX() + width / 2,
                    module.getY(), module.getY() + height,
                    module.getZ() - depth / 2, module.getZ() + depth / 2
            ));
        }
    }

    /**
     * Generates capture points from strategic hull modules.
     */
    private void generateCapturePoints(FpsWorldStateDto worldState, FpsMap map) {
        int pointIndex = 0;
        String[] pointNames = {"Alpha", "Bravo", "Charlie", "Delta", "Echo"};

        // Military bases and stations become capture points
        for (FpsWorldStateDto.FpsHullModuleStateDto module : worldState.getHullModules()) {
            if ("MILITARY".equals(module.getCategory()) || "INFRASTRUCTURE".equals(module.getCategory())) {
                String name = pointNames[pointIndex % pointNames.length];
                map.getCapturePoints().add(new FpsMap.CapturePoint(
                        name + "-" + pointIndex, module.getX(), module.getY(), module.getZ(), 15.0));
                pointIndex++;
            }
        }

        // Fallback: center capture point
        if (map.getCapturePoints().isEmpty()) {
            map.getCapturePoints().add(new FpsMap.CapturePoint("Center", 0, 0, 0, 15.0));
        }
    }

    /**
     * Generates power-up locations from industries.
     */
    private void generatePowerUpLocations(FpsWorldStateDto worldState, FpsMap map) {
        String[] powerUpTypes = {"health", "ammo", "shield", "speed"};
        int typeIndex = 0;

        for (com.yisy.modules.doramcu.hex.planet.fps.dto.FpsIndustryStateDto industry : worldState.getIndustries()) {
            String type = powerUpTypes[typeIndex % powerUpTypes.length];
            map.getPowerUpLocations().add(new FpsMap.PowerUpLocation(
                    industry.getX(), industry.getY(), industry.getZ(), type));
            typeIndex++;
        }
    }
}
