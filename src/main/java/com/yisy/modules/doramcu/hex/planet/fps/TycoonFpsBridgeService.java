package com.yisy.modules.doramcu.hex.planet.fps;

import com.yisy.modules.doramcu.hex.Planet;
import com.yisy.modules.doramcu.hex.planet.*;
import com.yisy.modules.doramcu.hex.planet.fps.dto.FpsIndustryStateDto;
import com.yisy.modules.doramcu.hex.planet.fps.dto.FpsVehicleStateDto;
import com.yisy.modules.doramcu.hex.planet.fps.dto.FpsWorldStateDto;
import com.yisy.modules.doramcu.hex.planet.fps.dto.FpsWorldStateDto.FpsStationStateDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Bridges tycoon state to the FPS battlefield.
 * <p>
 * The service:
 * <ul>
 *   <li>Exposes tycoon state to FPS renderer</li>
 *   <li>Converts Hex3D coordinates to 3D positions</li>
 *   <li>Provides world state snapshots</li>
 *   <li>Tracks vehicle position updates</li>
 * </ul>
 * </p>
 */
@Service
public class TycoonFpsBridgeService {

    private static final Logger log = LoggerFactory.getLogger(TycoonFpsBridgeService.class);

    /**
     * Gets a complete world state snapshot for the FPS renderer.
     *
     * @param builder the planet surface builder
     * @param planetId the planet ID
     * @param currentTurn the current turn number
     * @return the world state snapshot
     */
    public FpsWorldStateDto getWorldState(DefaultPlanetSurfaceBuilder builder,
                                           int planetId, int currentTurn) {
        Planet planet = new Planet(planetId);

        FpsWorldStateDto state = new FpsWorldStateDto(planetId, currentTurn);

        // Vehicles
        for (SurfaceVehicle vehicle : builder.getAllVehicles(planet)) {
            com.yisy.modules.doramcu.hex.Hex3D pos = vehicle.getCurrentPosition();
            state.addVehicle(new FpsVehicleStateDto(
                    vehicle.getId(),
                    vehicle.getType().getDisplayName(),
                    pos.getX(), pos.getY(), pos.getZ(),
                    vehicle.isActive(),
                    vehicle.getReliability(),
                    vehicle.getTotalCargoOnBoard(),
                    vehicle.getPassengerCount()
            ));
        }

        // Industries
        for (Industry industry : builder.getAllIndustries(planet)) {
            com.yisy.modules.doramcu.hex.Hex3D pos = industry.getLocation();
            state.addIndustry(new FpsIndustryStateDto(
                    industry.getId(),
                    industry.getType().getDisplayName(),
                    pos.getX(), pos.getY(), pos.getZ(),
                    industry.isActive(),
                    calculateWorkerCount(industry),
                    calculateProductionLevel(industry)
            ));
        }

        // Stations
        for (Station station : builder.getAllStations(planet)) {
            com.yisy.modules.doramcu.hex.Hex3D pos = station.getPosition();
            state.addStation(new FpsStationStateDto(
                    station.getId(),
                    station.getName(),
                    pos.getX(), pos.getY(), pos.getZ(),
                    station.getTotalCargoWaiting(),
                    station.getPassengersWaiting()
            ));
        }

        log.debug("Generated world state: {} vehicles, {} industries, {} stations",
                state.getVehicles().size(), state.getIndustries().size(), state.getStations().size());

        return state;
    }

    /**
     * Converts Hex3D coordinates to 3D world coordinates.
     * <p>
     * Uses axial-to-cartesian conversion for isometric projection.
     * </p>
     *
     * @param hex the hex coordinate
     * @return the 3D position as a double array [x, y, z]
     */
    public double[] hexTo3D(com.yisy.modules.doramcu.hex.Hex3D hex) {
        double hexSize = 10.0;
        double x = hexSize * (Math.sqrt(3) * hex.getX() + Math.sqrt(3) / 2.0 * hex.getY());
        double z = hexSize * (3.0 / 2.0 * hex.getY());
        double y = hex.getZ() * 5.0; // Height based on z coordinate
        return new double[]{x, y, z};
    }

    /**
     * Calculates the number of workers at an industry.
     */
    private int calculateWorkerCount(Industry industry) {
        IndustryType type = industry.getType();
        switch (type.getLevel()) {
            case PRIMARY: return 5;
            case SECONDARY: return 10;
            case TERTIARY: return 15;
            default: return 5;
        }
    }

    /**
     * Calculates the production level of an industry (0-100).
     */
    private int calculateProductionLevel(Industry industry) {
        if (!industry.isActive()) return 0;
        // Simplified: active industries produce at 80-100%
        return 80 + (industry.getId().hashCode() % 21);
    }
}
