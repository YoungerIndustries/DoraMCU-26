package com.yisy.modules.doramcu.hex.planet.fps.dto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Complete world state snapshot for the FPS 3D renderer.
 */
public class FpsWorldStateDto {

    private final int planetId;
    private final int currentTurn;
    private final List<FpsVehicleStateDto> vehicles;
    private final List<FpsIndustryStateDto> industries;
    private final List<FpsStationStateDto> stations;

    public FpsWorldStateDto(int planetId, int currentTurn) {
        this.planetId = planetId;
        this.currentTurn = currentTurn;
        this.vehicles = new ArrayList<>();
        this.industries = new ArrayList<>();
        this.stations = new ArrayList<>();
    }

    public int getPlanetId() { return planetId; }
    public int getCurrentTurn() { return currentTurn; }

    public List<FpsVehicleStateDto> getVehicles() {
        return Collections.unmodifiableList(vehicles);
    }

    public List<FpsIndustryStateDto> getIndustries() {
        return Collections.unmodifiableList(industries);
    }

    public List<FpsStationStateDto> getStations() {
        return Collections.unmodifiableList(stations);
    }

    public void addVehicle(FpsVehicleStateDto vehicle) {
        vehicles.add(vehicle);
    }

    public void addIndustry(FpsIndustryStateDto industry) {
        industries.add(industry);
    }

    public void addStation(FpsStationStateDto station) {
        stations.add(station);
    }

    /**
     * Station state for the FPS 3D renderer.
     */
    public static class FpsStationStateDto {
        private final String id;
        private final String name;
        private final int x, y, z;
        private final int cargoWaiting;
        private final int passengersWaiting;

        public FpsStationStateDto(String id, String name, int x, int y, int z,
                                   int cargoWaiting, int passengersWaiting) {
            this.id = id;
            this.name = name;
            this.x = x;
            this.y = y;
            this.z = z;
            this.cargoWaiting = cargoWaiting;
            this.passengersWaiting = passengersWaiting;
        }

        public String getId() { return id; }
        public String getName() { return name; }
        public int getX() { return x; }
        public int getY() { return y; }
        public int getZ() { return z; }
        public int getCargoWaiting() { return cargoWaiting; }
        public int getPassengersWaiting() { return passengersWaiting; }
    }
}
