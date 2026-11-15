package com.yisy.modules.doramcu.hex.planet;
import org.springframework.stereotype.Service;

import com.yisy.modules.doramcu.hex.Hex3D;
import com.yisy.modules.doramcu.hex.Hex3DDirection;
import com.yisy.modules.doramcu.hex.Planet;
import com.yisy.modules.doramcu.hex.planet.hullmodule.HullModuleService;
import com.yisy.modules.doramcu.hex.planet.hullmodule.HullModuleType;
import com.yisy.modules.doramcu.hex.spi.AiEntityRegistration;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
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
@Service
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

    /** Planet ID -> list of corporations */
    private final Map<Integer, List<TransportCorporation>> planetCorporations = new HashMap<>();

    /** Planet ID -> list of delivery contracts */
    private final Map<Integer, List<DeliveryContract>> planetContracts = new HashMap<>();

    /** Planet ID -> list of quests */
    private final Map<Integer, List<PlanetQuest>> planetQuests = new HashMap<>();

    /** Planet ID -> list of trade routes */
    private final Map<Integer, List<CampaignTradeRoute>> planetTradeRoutes = new HashMap<>();

    /** Hex3D -> TerrainData mapping */
    private final Map<String, TerrainData> terrainMap = new HashMap<>();

    /** Optional: service for deactivating AI characters when vehicles are removed. */
    @Autowired(required = false)
    private AiEntityRegistration aiEntityRegistrationService;

    /** Service for creating unified HullModule instances for all buildings */
    @Autowired(required = false)
    private HullModuleService hullModuleService;

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

        // Create unified HullModule instance for 3D rendering
        if (hullModuleService != null) {
            try {
                HullModuleType moduleType = HullModuleType.fromIndustryType(industryType.name());
                hullModuleService.buildModuleWithoutValidation(
                        industryType.getDisplayName() + " (" + id + ")",
                        moduleType,
                        (long) planet.getId(),
                        hex.getX(), hex.getY(), hex.getZ(),
                        empireId != null ? empireId : "SYSTEM"
                );
            } catch (Exception e) {
                // Log but don't fail - Industry POJO is the primary object
                System.out.println("Warning: Failed to create HullModule for industry " + id + ": " + e.getMessage());
            }
        }

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
    public Station buildStation(Planet planet, String name, Hex3D hex, String empireId, Station.StationType stationType) {
        String id = "STAT-" + nextId.getAndIncrement();
        Station station = new Station(id, name, hex, stationType);
        station.setOwnerEmpireId(empireId);

        // Register on terrain
        TerrainData terrain = getTerrainData(hex);
        if (terrain != null) {
            terrain.setInfrastructure(stationType.getInfrastructureType());
        }

        // Register on planet
        planetStations.computeIfAbsent(planet.getId(), k -> new ArrayList<>()).add(station);

        // Create unified HullModule instance for 3D rendering
        if (hullModuleService != null) {
            try {
                HullModuleType moduleType = HullModuleType.fromStationType(stationType.name());
                hullModuleService.buildModuleWithoutValidation(
                        name + " (" + id + ")",
                        moduleType,
                        (long) planet.getId(),
                        hex.getX(), hex.getY(), hex.getZ(),
                        empireId != null ? empireId : "SYSTEM"
                );
            } catch (Exception e) {
                // Log but don't fail - Station POJO is the primary object
                System.out.println("Warning: Failed to create HullModule for station " + id + ": " + e.getMessage());
            }
        }

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
                // Deactivate AI character before removing
                if (aiEntityRegistrationService != null) {
                    try {
                        aiEntityRegistrationService.deactivateVehicleAiCharacter(vehicleId);
                    } catch (Exception e) {
                        // Gracefully skip deactivation if it fails
                    }
                }
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
    public Map<String, Long> processTurn(Planet planet) {
        int planetId = planet.getId();
        Map<String, Long> summary = new HashMap<>();
        long contractsExpired = 0;
        long questsExpired = 0;
        long totalRevenue = 0;
        long cargoDelivered = 0;

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
            cargoDelivered += vehicle.getTotalCargoDelivered();
        }

        // Process contracts: auto-fail expired AVAILABLE/ACTIVE contracts
        List<DeliveryContract> contracts = planetContracts.getOrDefault(planetId, Collections.emptyList());
        for (DeliveryContract contract : contracts) {
            DeliveryContract.ContractStatus status = contract.getStatus();
            if (status == DeliveryContract.ContractStatus.AVAILABLE || status == DeliveryContract.ContractStatus.ACTIVE) {
                if (contract.isExpired()) {
                    // Expired AVAILABLE contracts just fail silently; ACTIVE contracts need fail() call
                    if (status == DeliveryContract.ContractStatus.ACTIVE) {
                        try {
                            contract.fail();
                        } catch (IllegalStateException e) {
                            // Already in terminal state, skip
                        }
                    }
                    contractsExpired++;
                }
            }
        }

        // Process quests: auto-fail expired AVAILABLE/IN_PROGRESS quests
        List<PlanetQuest> quests = planetQuests.getOrDefault(planetId, Collections.emptyList());
        for (PlanetQuest quest : quests) {
            PlanetQuest.QuestStatus status = quest.getStatus();
            if (status == PlanetQuest.QuestStatus.AVAILABLE || status == PlanetQuest.QuestStatus.IN_PROGRESS) {
                if (quest.isExpired()) {
                    if (status == PlanetQuest.QuestStatus.IN_PROGRESS) {
                        try {
                            quest.fail();
                        } catch (IllegalStateException e) {
                            // Already in terminal state, skip
                        }
                    }
                    questsExpired++;
                }
            }
        }

        // Process trade routes: generate revenue and deliver cargo
        List<CampaignTradeRoute> tradeRoutes = planetTradeRoutes.getOrDefault(planetId, Collections.emptyList());
        for (CampaignTradeRoute route : tradeRoutes) {
            if (route.isActive()) {
                long routeRevenue = route.processTurn();
                totalRevenue += routeRevenue;
            }
        }

        // Process corporations: distribute trade route revenue and award completed contract rewards
        List<TransportCorporation> corporations = planetCorporations.getOrDefault(planetId, Collections.emptyList());
        for (TransportCorporation corp : corporations) {
            // Distribute revenue from trade routes owned by this corporation
            for (CampaignTradeRoute route : tradeRoutes) {
                if (route.isActive() && corp.getOwnedRouteIds().contains(route.getId())) {
                    long corpRevenue = route.getTotalRevenue() / Math.max(1, corporations.size());
                    corp.addCredits(corpRevenue);
                }
            }
            // Award credits and XP from completed contracts assigned to this corporation
            for (DeliveryContract contract : contracts) {
                if (contract.getStatus() == DeliveryContract.ContractStatus.COMPLETED
                        && corp.getId().equals(contract.getAssignedCorporationId())) {
                    corp.addCredits(contract.getPayCredits());
                    if (contract.getXpReward() > 0) {
                        corp.addXp(contract.getXpReward());
                    }
                }
            }
            // Award credits and XP from completed quests assigned to this corporation
            for (PlanetQuest quest : quests) {
                if (quest.getStatus() == PlanetQuest.QuestStatus.COMPLETED
                        && corp.getId().equals(quest.getAssignedCorporationId())) {
                    corp.addCredits(quest.getCreditsReward());
                    if (quest.getXpReward() > 0) {
                        corp.addXp(quest.getXpReward());
                    }
                }
            }
        }

        summary.put("contractsExpired", contractsExpired);
        summary.put("questsExpired", questsExpired);
        summary.put("totalRevenue", totalRevenue);
        summary.put("cargoDelivered", cargoDelivered);
        return summary;
    }

    @Override
    public PlanetEventManager getEventManager() {
        return eventManager;
    }

    /**
     * Returns all terrain data managed by this builder.
     * Used by persistence service to save terrain cells.
     *
     * @return unmodifiable map of hex key to terrain data
     */
    public Map<String, TerrainData> getAllTerrainData() {
        return Collections.unmodifiableMap(terrainMap);
    }

    /**
     * Returns all stations on a planet (regardless of empire).
     *
     * @param planet the planet
     * @return list of all stations
     */
    public List<Station> getAllStations(Planet planet) {
        return planetStations.getOrDefault(planet.getId(), Collections.emptyList());
    }

    /**
     * Returns all vehicles on a planet (regardless of empire).
     *
     * @param planet the planet
     * @return list of all vehicles
     */
    public List<SurfaceVehicle> getAllVehicles(Planet planet) {
        return planetVehicles.getOrDefault(planet.getId(), Collections.emptyList());
    }

    /**
     * Returns all corporations on a planet (regardless of empire).
     *
     * @param planet the planet
     * @return list of all corporations
     */
    public List<TransportCorporation> getAllCorporations(Planet planet) {
        return planetCorporations.getOrDefault(planet.getId(), Collections.emptyList());
    }

    /**
     * Creates a new corporation on a planet and registers it.
     *
     * @param planet the planet
     * @param name the corporation name
     * @param empireId the owner empire ID
     * @param startingCredits initial credits
     * @return the created corporation
     */
    public TransportCorporation createCorporation(Planet planet, String name, String empireId, long startingCredits) {
        TransportCorporation corp = new TransportCorporation(name, empireId, startingCredits);
        planetCorporations.computeIfAbsent(planet.getId(), k -> new ArrayList<>()).add(corp);
        return corp;
    }

    /**
     * Registers a contract on a planet.
     */
    public void addContract(Planet planet, DeliveryContract contract) {
        planetContracts.computeIfAbsent(planet.getId(), k -> new ArrayList<>()).add(contract);
    }

    /**
     * Registers a quest on a planet.
     */
    public void addQuest(Planet planet, PlanetQuest quest) {
        planetQuests.computeIfAbsent(planet.getId(), k -> new ArrayList<>()).add(quest);
    }

    /**
     * Registers a trade route on a planet.
     */
    public void addTradeRoute(Planet planet, CampaignTradeRoute route) {
        planetTradeRoutes.computeIfAbsent(planet.getId(), k -> new ArrayList<>()).add(route);
    }

    /**
     * Returns all contracts on a planet.
     */
    public List<DeliveryContract> getAllContracts(Planet planet) {
        return planetContracts.getOrDefault(planet.getId(), Collections.emptyList());
    }

    /**
     * Returns all quests on a planet.
     */
    public List<PlanetQuest> getAllQuests(Planet planet) {
        return planetQuests.getOrDefault(planet.getId(), Collections.emptyList());
    }

    /**
     * Returns all trade routes on a planet.
     */
    public List<CampaignTradeRoute> getAllTradeRoutes(Planet planet) {
        return planetTradeRoutes.getOrDefault(planet.getId(), Collections.emptyList());
    }

    @Override
    public Settlement buildCity(Planet planet, String cityName, Hex3D centerHex,
                                 String empireId, int districtCount) {
        // Create the city center settlement
        Settlement cityCenter = foundSettlement(planet, cityName, centerHex, empireId);

        // Generate districts around the center using hex directions
        int districtsBuilt = 0;
        for (com.yisy.modules.doramcu.hex.Hex3DDirection dir : com.yisy.modules.doramcu.hex.Hex3DDirection.values()) {
            if (districtsBuilt >= districtCount) {
                break;
            }

            Hex3D neighbor = centerHex.neighbor(dir);
            TerrainData terrain = getTerrainData(neighbor);
            if (terrain != null && terrain.getTerrainType().isSettlementAllowed() && !terrain.hasSettlement()) {
                Settlement district = foundSettlement(
                        planet, cityName + " District " + (districtsBuilt + 1), neighbor, empireId);

                // Build road connecting district to city center
                buildInfrastructure(neighbor, TerrainData.InfrastructureType.ROAD, empireId);

                // Build industry in some districts
                if (districtsBuilt % 2 == 0) {
                    buildIndustry(planet, IndustryType.MINE, neighbor, empireId);
                }

                districtsBuilt++;
            }
        }

        // Build station at city center
        buildStation(planet, cityName + " Station", centerHex, empireId);

        eventManager.dispatch(new PlanetEvent(
                PlanetEventType.SETTLEMENT_FOUND, System.currentTimeMillis(),
                cityCenter.getId(), "City built: " + cityName + " (" + districtsBuilt + " districts)"
        ));

        return cityCenter;
    }

    /**
     * Generates a unique key for a Hex3D cell for map lookups.
     */
    private String hexKey(Hex3D hex) {
        return hex.getX() + "," + hex.getY() + "," + hex.getZ();
    }

    @Override
    public Map<String, Integer> autoPopulatePlanet(Planet planet, long seed, String empireId) {
        Random random = new Random(seed);
        Map<String, Integer> summary = new HashMap<>();

        // Collect all surface hexes with terrain data
        List<Hex3D> surfaceHexes = planet.getGeometry().getSurface();
        List<Hex3D> landHexes = new ArrayList<>();
        List<Hex3D> settlementAllowedHexes = new ArrayList<>();

        for (Hex3D hex : surfaceHexes) {
            TerrainData terrain = getTerrainData(hex);
            if (terrain == null) {
                continue;
            }
            if (!terrain.getTerrainType().isWater()) {
                landHexes.add(hex);
                if (terrain.getTerrainType().isSettlementAllowed()) {
                    settlementAllowedHexes.add(hex);
                }
            }
        }

        // Shuffle for random placement
        Collections.shuffle(settlementAllowedHexes, random);
        Collections.shuffle(landHexes, random);

        // 1. Place settlements — scale with available land
        int settlementCount = Math.min(
                Math.max(1, settlementAllowedHexes.size() / 30),
                settlementAllowedHexes.size()
        );
        int settlementsPlaced = 0;

        // Settlement name pools
        String[] settlementNames = {
                "New Hope", "Starfall", "Ironhold", "Greenfield", "Port Haven",
                "Silver Creek", "Oakwood", "Riverside", "Highland", "Sunset City",
                "Crystal Bay", "Thunder Ridge", "Mistwood", "Goldfield", "Stormwatch",
                "Brightwater", "Shadowmere", "Windcrest", "Stonehaven", "Firebrand"
        };

        for (int i = 0; i < settlementCount && i < settlementAllowedHexes.size(); i++) {
            Hex3D hex = settlementAllowedHexes.get(i);
            TerrainData terrain = getTerrainData(hex);
            if (terrain != null && !terrain.hasSettlement()) {
                String name = settlementNames[i % settlementNames.length];
                try {
                    foundSettlement(planet, name, hex, empireId);
                    settlementsPlaced++;
                } catch (IllegalArgumentException ignored) {
                    // Hex may have been taken by a previous placement
                }
            }
        }
        summary.put("settlements", settlementsPlaced);

        // 2. Place industries near settlements
        List<Settlement> settlements = getAllSettlements(planet);
        int industriesPlaced = 0;

        // Primary industries that can go on common terrain
        IndustryType[] primaryIndustries = {
                IndustryType.FARM, IndustryType.RANCH, IndustryType.ORCHARD,
                IndustryType.COAL_MINE, IndustryType.LOGGING_CAMP
        };

        for (Settlement settlement : settlements) {
            // Place 1-2 industries per settlement
            int industriesForSettlement = 1 + random.nextInt(2);
            for (int j = 0; j < industriesForSettlement; j++) {
                Hex3D center = settlement.getCenterHex();
                // Try neighbors first, then random land hexes
                Hex3D industryHex = findIndustryHex(center, landHexes, random);
                if (industryHex != null) {
                    TerrainData terrain = getTerrainData(industryHex);
                    if (terrain != null && !terrain.hasIndustry() && terrain.getTerrainType().isLand()) {
                        IndustryType type = primaryIndustries[random.nextInt(primaryIndustries.length)];
                        // Check terrain compatibility
                        if (type.canBuildOn(terrain.getTerrainType())) {
                            try {
                                buildIndustry(planet, type, industryHex, empireId);
                                industriesPlaced++;
                            } catch (IllegalArgumentException ignored) {
                            }
                        }
                    }
                }
            }
        }
        summary.put("industries", industriesPlaced);

        // 3. Build stations at settlement centers
        int stationsPlaced = 0;
        for (Settlement settlement : settlements) {
            Hex3D center = settlement.getCenterHex();
            try {
                buildStation(planet, settlement.getName() + " Station", center, empireId);
                stationsPlaced++;
            } catch (Exception ignored) {
            }
        }
        summary.put("stations", stationsPlaced);

        // 4. Build roads connecting settlements to their stations
        int roadsBuilt = 0;
        for (Settlement settlement : settlements) {
            Hex3D center = settlement.getCenterHex();
            // Build road on the settlement hex
            if (buildInfrastructure(center, TerrainData.InfrastructureType.ROAD, empireId)) {
                roadsBuilt++;
            }
            // Build roads to neighbors
            for (Hex3DDirection dir : Hex3DDirection.values()) {
                Hex3D neighbor = center.neighbor(dir);
                TerrainData terrain = getTerrainData(neighbor);
                if (terrain != null && terrain.getTerrainType().isLand()) {
                    if (buildInfrastructure(neighbor, TerrainData.InfrastructureType.ROAD, empireId)) {
                        roadsBuilt++;
                    }
                }
            }
        }
        summary.put("roads", roadsBuilt);

        // 5. Deploy vehicles between stations
        List<Station> stations = getStations(planet, empireId);
        int vehiclesDeployed = 0;
        if (stations.size() >= 2) {
            // Deploy buses between connected stations
            for (int i = 0; i < stations.size() - 1; i++) {
                Station from = stations.get(i);
                Station to = stations.get((i + 1) % stations.size());
                try {
                    deployVehicle(planet, VehicleType.BUS, from, empireId);
                    vehiclesDeployed++;
                } catch (Exception ignored) {
                }
            }
            // Deploy trains if we have enough stations
            if (stations.size() >= 3) {
                try {
                    deployVehicle(planet, VehicleType.PASSENGER_TRAIN, stations.get(0), empireId);
                    vehiclesDeployed++;
                } catch (Exception ignored) {
                }
            }
        }
        summary.put("vehicles", vehiclesDeployed);

        // 6. Deploy trucks from industries to stations
        List<Industry> industries = getAllIndustries(planet);
        for (Industry industry : industries) {
            if (!stations.isEmpty()) {
                Station nearestStation = stations.get(random.nextInt(stations.size()));
                try {
                    deployVehicle(planet, VehicleType.TRUCK, nearestStation, empireId);
                    vehiclesDeployed++;
                } catch (Exception ignored) {
                }
            }
        }
        summary.put("vehicles", vehiclesDeployed);

        // 7. Generate 3D interior rooms for all buildings
        int interiorsGenerated = 0;
        BuildingInteriorGenerator interiorGenerator = new BuildingInteriorGenerator(
                new MockDungeonRoomService(), seed
        );

        for (Settlement settlement : settlements) {
            try {
                List<String> dungeonIds = interiorGenerator.generateSettlementInteriors(settlement);
                settlement.setDungeonIds(dungeonIds);
                interiorsGenerated += dungeonIds.size();

                // Generate apartment building for each settlement as HullModule
                com.yisy.modules.doramcu.hex.Hex3D pos = settlement.getCenterHex();
                if (hullModuleService != null) {
                    try {
                        String aptName = settlement.getName() + " Apartments";
                        hullModuleService.buildModuleWithoutValidation(
                                aptName,
                                HullModuleType.APARTMENT_BUILDING,
                                (long) planet.getId(),
                                pos.getX(), pos.getY(), pos.getZ(),
                                empireId != null ? empireId : "SYSTEM"
                        );
                        interiorsGenerated++;
                    } catch (Exception e) {
                        System.out.println("Warning: Failed to create HullModule for apartment " + settlement.getId() + ": " + e.getMessage());
                    }
                }

                // Generate Younger Industries HQ Hub for major capitals (CITY and METROPOLIS) as HullModule
                if (settlement.getSize() == Settlement.Size.CITY || settlement.getSize() == Settlement.Size.METROPOLIS) {
                    if (hullModuleService != null) {
                        try {
                            // Offset HQ slightly from settlement center so it's distinct
                            com.yisy.modules.doramcu.hex.Hex3D hqPos = new com.yisy.modules.doramcu.hex.Hex3D(
                                    pos.getX() + 2, pos.getY() - 1, pos.getZ() - 1
                            );
                            String hqName = "Younger Industries HQ (" + settlement.getName() + ")";
                            hullModuleService.buildModuleWithoutValidation(
                                    hqName,
                                    HullModuleType.YOUNGER_HQ,
                                    (long) planet.getId(),
                                    hqPos.getX(), hqPos.getY(), hqPos.getZ(),
                                    empireId != null ? empireId : "SYSTEM"
                            );
                            interiorsGenerated++;
                        } catch (Exception e) {
                            System.out.println("Warning: Failed to create HullModule for HQ " + settlement.getId() + ": " + e.getMessage());
                        }
                    }
                }
            } catch (Exception ignored) {
            }
        }

        for (Industry industry : industries) {
            try {
                String dungeonId = interiorGenerator.generateIndustryInterior(industry);
                industry.setDungeonId(dungeonId);
                interiorsGenerated++;
            } catch (Exception ignored) {
            }
        }

        for (Station station : stations) {
            try {
                String dungeonId = interiorGenerator.generateStationInterior(station);
                station.setDungeonId(dungeonId);
                interiorsGenerated++;
            } catch (Exception ignored) {
            }
        }
        summary.put("interiors", interiorsGenerated);

        // 8. Create transport corporations and assign vehicles and stations
        int corporationsCreated = 0;
        int numCorporations = Math.min(3, Math.max(1, stations.size() / 2));

        String[] corpNames = {
                "Stellar Transit Co.", "Nebula Freight Lines", "Cosmic Cargo Express",
                "Void Runner Logistics", "AstroWay Transport", "Galaxy Haulers Inc.",
                "Orbital Delivery Service", "Starlink Shipping", "Quantum Freight Corp.",
                "Interstellar Movers Ltd."
        };

        List<TransportCorporation> corporations = new ArrayList<>();
        for (int i = 0; i < numCorporations; i++) {
            String corpName = corpNames[(i + (int)(seed % corpNames.length)) % corpNames.length];
            long startingCredits = 10000 + (long)(random.nextInt(5) * 5000);
            TransportCorporation corp = createCorporation(planet, corpName, empireId, startingCredits);
            corporations.add(corp);
            corporationsCreated++;
        }

        // Distribute stations among corporations
        List<Station> allStations = getAllStations(planet);
        for (int i = 0; i < allStations.size(); i++) {
            TransportCorporation corp = corporations.get(i % corporations.size());
            corp.addStation(allStations.get(i).getId());
        }

        // Distribute vehicles among corporations
        List<SurfaceVehicle> allVehicles = getAllVehicles(planet);
        for (int i = 0; i < allVehicles.size(); i++) {
            TransportCorporation corp = corporations.get(i % corporations.size());
            corp.addVehicle(allVehicles.get(i).getId());
        }

        // Create trade routes between stations for each corporation
        for (TransportCorporation corp : corporations) {
            List<String> corpStationIds = new ArrayList<>(corp.getOwnedStationIds());
            for (int i = 0; i < corpStationIds.size() - 1; i++) {
                String routeId = "ROUTE-" + corpStationIds.get(i) + "-" + corpStationIds.get(i + 1);
                corp.addRoute(routeId);
            }
        }

        summary.put("corporations", corporationsCreated);

        // 9. Generate contracts, quests, and trade routes for the economy
        int contractsCreated = 0;
        int questsCreated = 0;
        int routesCreated = 0;

        // Generate delivery contracts between stations
        DeliveryContract.ContractType[] contractTypes = DeliveryContract.ContractType.values();
        CargoType[] cargoTypes = CargoType.values();
        String[] contractNames = {
                "Ore Delivery", "Grain Transport", "Coal Run", "Timber Haul",
                "Fish Express", "Mail Service", "Steel Run", "Oil Transport",
                "Passenger Charter", "Urgent Supply", "Bulk Freight", "VIP Courier"
        };

        int numContracts = Math.min(10, Math.max(3, stations.size() * 2));
        for (int i = 0; i < numContracts; i++) {
            DeliveryContract.ContractType type = contractTypes[i % contractTypes.length];
            CargoType cargo = cargoTypes[i % cargoTypes.length];
            int amount = 10 + random.nextInt(50);
            Station fromStation = stations.get(random.nextInt(stations.size()));
            int toIndex;
            do {
                toIndex = random.nextInt(stations.size());
            } while (toIndex == stations.indexOf(fromStation) && stations.size() > 1);
            Station toStation = stations.get(toIndex);

            long pay = (long) (cargo.getBaseValue() * amount * type.getPayMultiplier());
            int xp = (int) (cargo.getBaseValue() * amount * 0.1);
            String name = contractNames[i % contractNames.length];

            DeliveryContract contract = new DeliveryContract(
                    name, type, cargo, amount,
                    fromStation.getId(), toStation.getId(),
                    pay, xp, 1 + random.nextInt(3), empireId
            );
            addContract(planet, contract);

            // Assign some contracts to corporations
            if (!corporations.isEmpty() && random.nextBoolean()) {
                TransportCorporation corp = corporations.get(random.nextInt(corporations.size()));
                corp.addContract(contract.getId());
                contract.accept(corp.getId());
            }
            contractsCreated++;
        }
        summary.put("contracts", contractsCreated);

        // Generate RPG quests
        PlanetQuest.QuestType[] questTypes = PlanetQuest.QuestType.values();
        String[] questTitles = {
                "Escort the Diplomat", "Rescue the Miners", "Survey the Northern Wastes",
                "Deliver the Treaty", "Defend the Outpost", "Investigate the Signals",
                "Convoy Protection", "Lost Caravan Recovery", "Map the Unknown Region",
                "Ambassador's Journey", "Guard the Supply Line", "Decode the Message"
        };
        String[] questDescriptions = {
                "A diplomat needs safe passage between settlements.",
                "Miners are trapped in a collapsed tunnel — retrieve them quickly.",
                "The northern territories are unmapped. Send a survey team.",
                "A peace treaty must reach the neighboring settlement before sunset.",
                "Bandits are threatening an outpost. Defend it until reinforcements arrive.",
                "Strange signals have been detected. Investigate the source.",
                "Protect a convoy carrying valuable cargo through dangerous territory.",
                "A caravan was lost in the wilderness. Find and rescue the survivors.",
                "Chart the unexplored regions and report back with maps.",
                "An ambassador requires secure transport to a diplomatic meeting.",
                "Guard a supply line against raids for one week.",
                "Intercept and decode mysterious transmissions from an unknown source."
        };
        String[] issuerNames = {
                "Mayor", "General", "Trade Council", "Explorer's Guild",
                "Military Command", "Intelligence Agency", "Merchant League",
                "Colonial Office", "Frontier Patrol", "Royal Court"
        };

        int numQuests = Math.min(8, Math.max(2, settlements.size()));
        for (int i = 0; i < numQuests; i++) {
            PlanetQuest.QuestType type = questTypes[i % questTypes.length];
            int difficulty = 1 + random.nextInt(5);
            long credits = (long)(500 + random.nextInt(5) * 500 * type.getDifficultyMultiplier());
            int xp = (int) (type.getBaseXpReward() * type.getDifficultyMultiplier());

            PlanetQuest quest = PlanetQuest.builder()
                    .title(questTitles[i % questTitles.length])
                    .description(questDescriptions[i % questDescriptions.length])
                    .type(type)
                    .difficulty(difficulty)
                    .creditsReward(credits)
                    .xpReward(xp)
                    .reputationReward(1 + random.nextInt(3))
                    .issuerName(issuerNames[i % issuerNames.length])
                    .issuerEmpireId(empireId)
                    .addObjective("Complete the primary objective")
                    .timeLimitHours(48 + random.nextInt(48))
                    .build();

            // Add required stations to quest
            if (!stations.isEmpty()) {
                int stationCount = 1 + random.nextInt(Math.min(3, stations.size()));
                for (int j = 0; j < stationCount; j++) {
                    quest.getRequiredStationIds().add(stations.get(random.nextInt(stations.size())).getId());
                }
            }

            addQuest(planet, quest);
            questsCreated++;
        }
        summary.put("quests", questsCreated);

        // Create CampaignTradeRoute objects for corporation routes
        String[] routeNames = {
                "Express Line", "Freight Corridor", "Cargo Express",
                "Passenger Route", "Supply Chain", "Trade Artery",
                "Commerce Link", "Transport Hub Route"
        };

        for (TransportCorporation corp : corporations) {
            List<String> corpStationIds = new ArrayList<>(corp.getOwnedStationIds());
            if (corpStationIds.size() >= 2) {
                for (int i = 0; i < corpStationIds.size() - 1; i++) {
                    String routeName = routeNames[(i + (int)(seed % routeNames.length)) % routeNames.length];
                    CampaignTradeRoute route = CampaignTradeRoute.builder()
                            .name(routeName + " " + (i + 1))
                            .ownerCorporationId(corp.getId())
                            .addStationId(corpStationIds.get(i))
                            .addStationId(corpStationIds.get(i + 1))
                            .addCargoType(cargoTypes[random.nextInt(cargoTypes.length)])
                            .totalDistance(10 + random.nextInt(40))
                            .build();

                    // Assign vehicles to route
                    for (String vehicleId : corp.getOwnedVehicleIds()) {
                        if (random.nextBoolean()) {
                            route.addVehicle(vehicleId);
                        }
                    }

                    addTradeRoute(planet, route);
                    routesCreated++;
                }
            }
        }
        summary.put("routes", routesCreated);

        // Dispatch event
        eventManager.dispatch(new PlanetEvent(
                PlanetEventType.BIOME_GENERATED, System.currentTimeMillis(),
                String.valueOf(planet.getId()),
                "Auto-populated: " + settlementsPlaced + " settlements, " +
                industriesPlaced + " industries, " + stationsPlaced + " stations, " +
                vehiclesDeployed + " vehicles, " + interiorsGenerated + " interiors, " +
                corporationsCreated + " corporations, " + contractsCreated + " contracts, " +
                questsCreated + " quests, " + routesCreated + " routes"
        ));

        return summary;
    }

    /**
     * Finds a suitable hex for an industry near a settlement.
     * Tries neighbors first, then falls back to random land hexes.
     */
    private Hex3D findIndustryHex(Hex3D center, List<Hex3D> landHexes, Random random) {
        // Try neighbors first
        List<Hex3DDirection> directions = new ArrayList<>(Arrays.asList(Hex3DDirection.values()));
        Collections.shuffle(directions, random);

        for (Hex3DDirection dir : directions) {
            Hex3D neighbor = center.neighbor(dir);
            TerrainData terrain = getTerrainData(neighbor);
            if (terrain != null && terrain.getTerrainType().isLand() && !terrain.hasIndustry()) {
                return neighbor;
            }
        }

        // Fall back to random land hex
        if (!landHexes.isEmpty()) {
            for (int i = 0; i < Math.min(10, landHexes.size()); i++) {
                Hex3D hex = landHexes.get(random.nextInt(landHexes.size()));
                TerrainData terrain = getTerrainData(hex);
                if (terrain != null && !terrain.hasIndustry()) {
                    return hex;
                }
            }
        }
        return null;
    }
}
