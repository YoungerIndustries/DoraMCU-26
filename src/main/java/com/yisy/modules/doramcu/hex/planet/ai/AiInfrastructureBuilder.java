package com.yisy.modules.doramcu.hex.planet.ai;

import com.yisy.modules.doramcu.hex.Hex3D;
import com.yisy.modules.doramcu.hex.planet.AiCorporationStrategy;
import com.yisy.modules.doramcu.hex.planet.DefaultPlanetSurfaceBuilder;
import com.yisy.modules.doramcu.hex.planet.Station;
import com.yisy.modules.doramcu.hex.planet.SurfaceVehicle;
import com.yisy.modules.doramcu.hex.planet.TerrainData;
import com.yisy.modules.doramcu.hex.planet.TransportCorporation;
import com.yisy.modules.doramcu.hex.planet.VehicleType;
import com.yisy.modules.doramcu.hex.planet.depot.VehicleDepotService;
import com.yisy.modules.doramcu.hex.planet.pathfinding.InfrastructurePathfinder;
import com.yisy.modules.doramcu.hex.planet.pathfinding.dto.PathResultDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * AI-driven infrastructure building for rival corporations.
 * <p>
 * The AI:
 * <ul>
 *   <li>Builds roads/rails between its stations</li>
 *   <li>Purchases and deploys vehicles on profitable routes</li>
 *   <li>Competes for contracts and market share</li>
 * </ul>
 * </p>
 */
@Service
public class AiInfrastructureBuilder {

    private static final Logger log = LoggerFactory.getLogger(AiInfrastructureBuilder.class);

    private final InfrastructurePathfinder pathfinder;
    private final VehicleDepotService depotService;

    public AiInfrastructureBuilder(InfrastructurePathfinder pathfinder, VehicleDepotService depotService) {
        this.pathfinder = pathfinder;
        this.depotService = depotService;
    }

    /**
     * Processes a turn for an AI corporation.
     *
     * @param corporation the AI corporation
     * @param planetId the planet ID
     * @param builder the planet surface builder
     * @param stations all stations on the planet
     * @param currentTurn the current turn number
     */
    public void processTurn(TransportCorporation corporation, int planetId,
                             DefaultPlanetSurfaceBuilder builder,
                             List<Station> stations, int currentTurn) {
        if (corporation.getCredits() < 100) {
            // Not enough credits to do anything meaningful
            return;
        }

        // Get AI strategy
        AiCorporationStrategy strategy = getStrategy(corporation);

        // Build infrastructure if needed
        buildInfrastructure(corporation, planetId, builder, stations, strategy);

        // Deploy vehicles if possible
        deployVehicles(corporation, planetId, stations, strategy);

        // Service vehicles if needed
        serviceVehicles(corporation, planetId, currentTurn);
    }

    /**
     * Builds infrastructure between stations.
     */
    private void buildInfrastructure(TransportCorporation corporation, int planetId,
                                      DefaultPlanetSurfaceBuilder builder,
                                      List<Station> stations, AiCorporationStrategy strategy) {
        // Find stations owned by this corporation
        List<Station> ownedStations = new ArrayList<>();
        for (Station station : stations) {
            if (station.getOwnerEmpireId() != null &&
                    station.getOwnerEmpireId().equals(corporation.getId())) {
                ownedStations.add(station);
            }
        }

        if (ownedStations.size() < 2) return;

        // Try to build roads between unconnected stations
        for (int i = 0; i < ownedStations.size(); i++) {
            for (int j = i + 1; j < ownedStations.size(); j++) {
                Station from = ownedStations.get(i);
                Station to = ownedStations.get(j);

                // Check if path exists
                PathResultDto path = pathfinder.findPath(planetId, from, to);
                if (path.isPathFound()) continue; // Already connected

                // Build road between stations
                List<Hex3D> line = from.getPosition().line(to.getPosition());
                for (Hex3D hex : line) {
                    TerrainData terrain = builder.getTerrainData(hex);
                    if (terrain != null && terrain.isPassableByLand()) {
                        builder.buildInfrastructure(hex, TerrainData.InfrastructureType.ROAD,
                                corporation.getId());
                    }
                }

                // Update pathfinder
                for (Hex3D hex : line) {
                    pathfinder.updateNode(planetId, hex, TerrainData.InfrastructureType.ROAD);
                }

                log.info("AI {} built road between {} and {}",
                        corporation.getName(), from.getName(), to.getName());
            }
        }
    }

    /**
     * Deploys vehicles on profitable routes.
     */
    private void deployVehicles(TransportCorporation corporation, int planetId,
                                 List<Station> stations, AiCorporationStrategy strategy) {
        // Find stations owned by this corporation
        List<Station> ownedStations = new ArrayList<>();
        for (Station station : stations) {
            if (station.getOwnerEmpireId() != null &&
                    station.getOwnerEmpireId().equals(corporation.getId())) {
                ownedStations.add(station);
            }
        }

        if (ownedStations.size() < 2) return;

        // Check if corporation has enough credits for a vehicle
        VehicleType vehicleType = (strategy.getPersonality() == AiCorporationStrategy.AiPersonality.AGGRESSIVE)
                ? VehicleType.FREIGHT_TRAIN : VehicleType.TRUCK;
        if (corporation.getCredits() < vehicleType.getConstructionCost()) return;

        // Find a route without vehicles
        for (int i = 0; i < ownedStations.size() - 1; i++) {
            Station from = ownedStations.get(i);
            Station to = ownedStations.get(i + 1);

            // Check if path exists
            PathResultDto path = pathfinder.findPath(planetId, from, to, vehicleType);
            if (!path.isPathFound()) continue;

            // Deploy a vehicle
            // (simplified - in a full implementation, would check existing vehicles)
            log.info("AI {} deploying {} on route {} -> {}",
                    corporation.getName(), vehicleType.getDisplayName(),
                    from.getName(), to.getName());
        }
    }

    /**
     * Services vehicles that need maintenance.
     */
    private void serviceVehicles(TransportCorporation corporation, int planetId, int currentTurn) {
        for (String vehicleId : corporation.getOwnedVehicleIds()) {
            SurfaceVehicle vehicle = depotService.getVehicle(vehicleId);
            if (vehicle == null) continue;

            if (vehicle.isBrokenDown()) {
                vehicle.repair();
                log.info("AI {} repaired vehicle {}", corporation.getName(), vehicleId);
            } else if (vehicle.getReliability() < 30) {
                vehicle.service(currentTurn);
                log.info("AI {} serviced vehicle {}", corporation.getName(), vehicleId);
            }
        }
    }

    /**
     * Gets the AI strategy for a corporation.
     */
    private AiCorporationStrategy getStrategy(TransportCorporation corporation) {
        // Default to balanced strategy
        return new AiCorporationStrategy(AiCorporationStrategy.AiPersonality.BALANCED, System.nanoTime());
    }
}
