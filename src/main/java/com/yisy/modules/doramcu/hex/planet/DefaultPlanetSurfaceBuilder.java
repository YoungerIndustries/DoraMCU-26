package com.yisy.modules.doramcu.hex.planet;

import com.yisy.modules.doramcu.hex.Hex3D;
import com.yisy.modules.doramcu.hex.Planet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Default implementation of {@link PlanetSurfaceBuilder} that manages all planet
 * surface operations for an Empire.
 * <p>
 * This class maintains per-planet registries of settlements, industries, stations,
 * vehicles, and terrain data. It handles placement validation, turn processing,
 * and event dispatch.
 * </p>
 */
public class DefaultPlanetSurfaceBuilder implements PlanetSurfaceBuilder {

    private final PlanetEventManager eventManager = new PlanetEventManager();
    private final AtomicLong nextId = new AtomicLong(1);

    /** Planet ID -> list of settlements */
    private final Map<Integer, List<Settlement>> planetSettlements = new HashMap<>();

    /** Planet ID -> list of industries */
    private final Map<Integer, List<Industry>> planetIndustries = new HashMap<>();

    /** Planet ID -> list of stations */
    private final Map<Integer, List<Station>> planetStations = new HashMap<>();

    /** Planet ID -> list of vehicles */
    private final Map<Integer, List<SurfaceVehicle>> planetVehicles = new HashMap<>();

    /** Hex3D -> TerrainData mapping */
    private final Map<String, TerrainData> terrainMap = new HashMap<>();

    @Override
    public int generateBiomes(Planet planet, long seed) {
        BiomeGenerator generator = new BiomeGenerator(seed);
        generator.generateBiomes(planet.getGeometry());

        List<Hex3D> surface = planet.getGeometry().getSurface();
        int count = 0;
        for (Hex3D hex : surface) {
            Object payload = hex.getPayload();
            if (payload instanceof TerrainData) {
                terrainMap.put(hexKey(hex), (TerrainData) payload);
                count++;
            }
        }

        eventManager.dispatch(new PlanetEvent(
                PlanetEventType.BIOME_GENERATED, System.currentTimeMillis(),
                String.valueOf(planet.getId()), count
        ));

        return count;
    }

    @Override
    public TerrainData getTerrainData(Hex3D hex) {
        return terrainMap.get(hexKey(hex));
    }

    @Override
    public Settlement foundSettlement(Planet planet, String name, Hex3D centerHex, String empireId) {
        // Validate terrain
        TerrainData terrain = getTerrainData(centerHex);
        if (terrain == null) {
            throw new IllegalArgumentException("Terrain not generated at hex: " + centerHex);
        }
        if (!terrain.getTerrainType().isSettlementAllowed()) {
            throw new IllegalArgumentException(
                    "Settlement not allowed on terrain: " + terrain.getTerrainType());
        }
        if (terrain.hasSettlement()) {
            throw new IllegalArgumentException("Hex already occupied by a settlement: " + terrain.getSettlementId());
        }

        // Create settlement
        String id = "SETTLE-" + nextId.getAndIncrement();
        Settlement settlement = new Settlement(id, name, centerHex, Settlement.Size.VILLAGE);
        settlement.setOwnerEmpireId(empireId);

        // Register on terrain
        terrain.setSettlement(id);

        // Register on planet
        planetSettlements.computeIfAbsent(planet.getId(), k -> new ArrayList<>()).add(settlement);

        eventManager.dispatch(new PlanetEvent(
                PlanetEventType.SETTLEMENT_FOUND, System.currentTimeMillis(),
                id, empireId
        ));

        return settlement;
    }

    @Override
    public List<Settlement> getSettlements(Planet planet, String empireId) {
        List<Settlement> all = planetSettlements.getOrDefault(planet.getId(), Collections.emptyList());
        return all.stream()
                .filter(s -> empireId.equals(s.getOwnerEmpireId()))
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    public List<Settlement> getAllSettlements(Planet planet) {
        return planetSettlements.getOrDefault(planet.getId(), Collections.emptyList());
    }

    @Override
    public boolean removeSettlement(String settlementId) {
        for (List<Settlement> settlements : planetSettlements.values()) {
            Settlement removed = settlements.stream()
                    .filter(s -> settlementId.equals(s.getId()))
                    .findFirst()
                    .orElse(null);
            if (removed != null) {
                settlements.remove(removed);
                Hex3D center = removed.getCenterHex();
                TerrainData terrain = getTerrainData(center);
                if (terrain != null && settlementId.equals(terrain.getSettlementId())) {
                    terrain.setSettlement(null);
                }
                eventManager.dispatch(new PlanetEvent(
                        PlanetEventType.SETTLEMENT_REMOVED, System.currentTimeMillis(),
                        settlementId, null
                ));
                return true;
            }
        }
        return false;
    }

    @Override
    public Industry buildIndustry(Planet planet, IndustryType industryType, Hex3D hex, String empireId) {
        // Validate terrain
        TerrainData terrain = getTerrainData(hex);
        if (terrain == null) {
            throw new IllegalArgumentException("Terrain not generated at hex: " + hex);
        }
        if (terrain.hasIndustry()) {
            throw new IllegalArgumentException("Hex already occupied by an industry: " + terrain.getIndustryId());
        }
        if (!terrain.getTerrainType().isLand()) {
            throw new IllegalArgumentException("Industry cannot be built on water terrain");
        }

        // Create industry
        String id = "IND-" + nextId.getAndIncrement();
        Industry industry = new Industry(id, industryType, hex);
        industry.setOwnerEmpireId(empireId);

        // Register on terrain
        terrain.setIndustry(id);

        // Register on planet
        planetIndustries.computeIfAbsent(planet.getId(), k -> new ArrayList<>()).add(industry);

        eventManager.dispatch(new PlanetEvent(
                PlanetEventType.INDUSTRY_BUILT, System.currentTimeMillis(),
                id, empireId
        ));

        return industry;
    }

    @Override
    public List<Industry> getIndustries(Planet planet, String empireId) {
        List<Industry> all = planetIndustries.getOrDefault(planet.getId(), Collections.emptyList());
        return all.stream()
                .filter(i -> empireId.equals(i.getOwnerEmpireId()))
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    public List<Industry> getAllIndustries(Planet planet) {
        return planetIndustries.getOrDefault(planet.getId(), Collections.emptyList());
    }

    @Override
    public boolean removeIndustry(String industryId) {
        for (List<Industry> industries : planetIndustries.values()) {
            Industry removed = industries.stream()
                    .filter(i -> industryId.equals(i.getId()))
                    .findFirst()
                    .orElse(null);
            if (removed != null) {
                industries.remove(removed);
                Hex3D hex = removed.getLocation();
                TerrainData terrain = getTerrainData(hex);
                if (terrain != null && industryId.equals(terrain.getIndustryId())) {
                    terrain.setIndustry(null);
                }
                eventManager.dispatch(new PlanetEvent(
                        PlanetEventType.INDUSTRY_CLOSED, System.currentTimeMillis(),
                        industryId, null
                ));
                return true;
            }
        }
        return false;
    }

    @Override
    public Station buildStation(Planet planet, String name, Hex3D hex, String empireId) {
        String id = "STAT-" + nextId.getAndIncrement();
        Station station = new Station(id, name, hex);
        station.setOwnerEmpireId(empireId);

        // Register on terrain
        TerrainData terrain = getTerrainData(hex);
        if (terrain != null) {
            terrain.setInfrastructure(TerrainData.InfrastructureType.STATION);
        }

        // Register on planet
        planetStations.computeIfAbsent(planet.getId(), k -> new ArrayList<>()).add(station);

        eventManager.dispatch(new PlanetEvent(
                PlanetEventType.STATION_BUILT, System.currentTimeMillis(),
                id, empireId
        ));

        return station;
    }

    @Override
    public List<Station> getStations(Planet planet, String empireId) {
        List<Station> all = planetStations.getOrDefault(planet.getId(), Collections.emptyList());
        return all.stream()
                .filter(s -> empireId.equals(s.getOwnerEmpireId()))
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    public boolean removeStation(String stationId) {
        for (List<Station> stations : planetStations.values()) {
            Station removed = stations.stream()
                    .filter(s -> stationId.equals(s.getId()))
                    .findFirst()
                    .orElse(null);
            if (removed != null) {
                stations.remove(removed);
                Hex3D hex = removed.getLocation();
                TerrainData terrain = getTerrainData(hex);
                if (terrain != null && terrain.getInfrastructure() == TerrainData.InfrastructureType.STATION) {
                    terrain.setInfrastructure(TerrainData.InfrastructureType.NONE);
                }
                eventManager.dispatch(new PlanetEvent(
                        PlanetEventType.STATION_REMOVED, System.currentTimeMillis(),
                        stationId, null
                ));
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean buildInfrastructure(Hex3D hex, TerrainData.InfrastructureType infraType, String empireId) {
        TerrainData terrain = getTerrainData(hex);
        if (terrain == null) {
            return false;
        }
        if (infraType == TerrainData.InfrastructureType.NONE) {
            return false;
        }
        terrain.setInfrastructure(infraType);

        eventManager.dispatch(new PlanetEvent(
                PlanetEventType.ROAD_CONSTRUCTED, System.currentTimeMillis(),
                hexKey(hex), infraType.name()
        ));

        return true;
    }

    @Override
    public boolean removeInfrastructure(Hex3D hex) {
        TerrainData terrain = getTerrainData(hex);
        if (terrain == null) {
            return false;
        }
        terrain.setInfrastructure(TerrainData.InfrastructureType.NONE);
        return true;
    }

    @Override
    public SurfaceVehicle deployVehicle(Planet planet, VehicleType vehicleType, Station startStation, String empireId) {
        String id = "VEH-" + nextId.getAndIncrement();
        SurfaceVehicle vehicle = new SurfaceVehicle(id, vehicleType, startStation.getPosition());
        vehicle.setOwnerEmpireId(empireId);

        // Register on planet
        planetVehicles.computeIfAbsent(planet.getId(), k -> new ArrayList<>()).add(vehicle);

        eventManager.dispatch(new PlanetEvent(
                PlanetEventType.VEHICLE_DEPLOYED, System.currentTimeMillis(),
                id, empireId
        ));

        return vehicle;
    }

    @Override
    public List<SurfaceVehicle> getVehicles(Planet planet, String empireId) {
        List<SurfaceVehicle> all = planetVehicles.getOrDefault(planet.getId(), Collections.emptyList());
        return all.stream()
                .filter(v -> empireId.equals(v.getOwnerEmpireId()))
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    public boolean removeVehicle(String vehicleId) {
        for (List<SurfaceVehicle> vehicles : planetVehicles.values()) {
            SurfaceVehicle removed = vehicles.stream()
                    .filter(v -> vehicleId.equals(v.getId()))
                    .findFirst()
                    .orElse(null);
            if (removed != null) {
                vehicles.remove(removed);
                eventManager.dispatch(new PlanetEvent(
                        PlanetEventType.VEHICLE_REMOVED, System.currentTimeMillis(),
                        vehicleId, null
                ));
                return true;
            }
        }
        return false;
    }

    @Override
    public void processTurn(Planet planet) {
        int planetId = planet.getId();

        // Process settlements
        List<Settlement> settlements = planetSettlements.getOrDefault(planetId, Collections.emptyList());
        for (Settlement settlement : settlements) {
            settlement.processTurn();
        }

        // Process industries
        List<Industry> industries = planetIndustries.getOrDefault(planetId, Collections.emptyList());
        for (Industry industry : industries) {
            industry.processTurn();
        }

        // Process vehicles with station map
        List<Station> allStations = planetStations.getOrDefault(planetId, Collections.emptyList());
        Map<String, Station> stationMap = new HashMap<>();
        for (Station s : allStations) {
            stationMap.put(s.getId(), s);
        }

        List<SurfaceVehicle> vehicles = planetVehicles.getOrDefault(planetId, Collections.emptyList());
        for (SurfaceVehicle vehicle : vehicles) {
            vehicle.processTurn(stationMap);
        }
    }

    @Override
    public PlanetEventManager getEventManager() {
        return eventManager;
    }

    /**
     * Generates a unique key for a Hex3D cell for map lookups.
     */
    private String hexKey(Hex3D hex) {
        return hex.getX() + "," + hex.getY() + "," + hex.getZ();
    }
}
