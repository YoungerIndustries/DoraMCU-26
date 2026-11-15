package com.yisy.modules.doramcu.hex.planet;
import org.springframework.stereotype.Service;

import com.yisy.modules.doramcu.hex.Hex3D;
import com.yisy.modules.doramcu.hex.Planet;
import com.yisy.modules.doramcu.model.*;
import com.yisy.modules.doramcu.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Service that persists all planet surface data so the Galaxy Overlord doesn't
 * have to randomly regenerate locations.
 * <p>
 * When the Galaxy Overlord creates or modifies anything on a planet surface â€”
 * terrain, settlements, industries, stations, or vehicles â€” this service saves
 * it to the mapping database. When a planet needs to be recreated (after restart
 * or when a player enters the planet), the Galaxy Overlord loads everything from
 * the database instead of re-running procedural generation.
 * </p>
 * <p>
 * Usage pattern:
 * <pre>
 * {@code
 * // After generating a planet surface:
 * persistenceService.savePlanetSurface(planet);
 *
 * // Later, to recreate the planet:
 * if (persistenceService.hasPlanetData(planetId)) {
 *     persistenceService.loadPlanetSurface(planet);
 * } else {
 *     // First time â€” generate procedurally
 *     builder.generateBiomes(planet, seed);
 *     persistenceService.savePlanetSurface(planet);
 * }
 * }
 * </pre>
 * </p>
 */
@Service
public class PlanetSurfacePersistenceService {

    private static final Logger log = LoggerFactory.getLogger(PlanetSurfacePersistenceService.class);

    private final PlanetTerrainCellRepository terrainRepository;
    private final PlanetSettlementRepository settlementRepository;
    private final PlanetIndustryRepository industryRepository;
    private final PlanetStationRepository stationRepository;
    private final PlanetVehicleRepository vehicleRepository;
    private final PlanetCorporationRepository corporationRepository;
    private final PlanetContractRepository contractRepository;
    private final RpgQuestRepository questRepository;
    private final CampaignTradeRouteRepository tradeRouteRepository;

    public PlanetSurfacePersistenceService(
            PlanetTerrainCellRepository terrainRepository,
            PlanetSettlementRepository settlementRepository,
            PlanetIndustryRepository industryRepository,
            PlanetStationRepository stationRepository,
            PlanetVehicleRepository vehicleRepository,
            PlanetCorporationRepository corporationRepository,
            PlanetContractRepository contractRepository,
            RpgQuestRepository questRepository,
            CampaignTradeRouteRepository tradeRouteRepository) {
        this.terrainRepository = terrainRepository;
        this.settlementRepository = settlementRepository;
        this.industryRepository = industryRepository;
        this.stationRepository = stationRepository;
        this.vehicleRepository = vehicleRepository;
        this.corporationRepository = corporationRepository;
        this.contractRepository = contractRepository;
        this.questRepository = questRepository;
        this.tradeRouteRepository = tradeRouteRepository;
    }

    // ---- Check if planet data exists ----

    /**
     * Returns true if the database already has terrain data for this planet.
     *
     * @param planetId the planet ID
     * @return true if terrain cells exist for this planet
     */
    @Transactional(readOnly = true)
    public boolean hasPlanetData(int planetId) {
        return terrainRepository.countByPlanetId(planetId) > 0;
    }

    /**
     * Returns a summary of what's stored for a planet.
     *
     * @param planetId the planet ID
     * @return map with counts of each object type
     */
    @Transactional(readOnly = true)
    public Map<String, Long> getPlanetSummary(int planetId) {
        Map<String, Long> summary = new HashMap<>();
        summary.put("terrainCells", terrainRepository.countByPlanetId(planetId));
        summary.put("settlements", settlementRepository.countByPlanetId(planetId));
        summary.put("industries", industryRepository.countByPlanetId(planetId));
        summary.put("stations", stationRepository.countByPlanetId(planetId));
        summary.put("vehicles", vehicleRepository.countByPlanetId(planetId));
        summary.put("corporations", corporationRepository.countByPlanetId(planetId));
        summary.put("contracts", contractRepository.countByPlanetId(planetId));
        summary.put("quests", questRepository.countByPlanetId(planetId));
        summary.put("tradeRoutes", tradeRouteRepository.countByPlanetId(planetId));
        return summary;
    }

    // ---- Save operations ----

    /**
     * Saves the entire planet surface to the database.
     * <p>
     * This saves all terrain cells, settlements, industries, stations, and vehicles
     * from the provided PlanetSurfaceBuilder's in-memory state.
     * </p>
     *
     * @param planet the planet being saved
     * @param builder the surface builder containing current state
     */
    @Transactional
    public void savePlanetSurface(Planet planet, PlanetSurfaceBuilder builder) {
        int planetId = planet.getId();
        log.info("Saving planet surface for planet {}...", planetId);

        saveTerrainCells(planetId, builder);
        saveSettlements(planetId, builder);
        saveIndustries(planetId, builder);
        saveStations(planetId, builder);
        saveVehicles(planetId, builder);
        saveCorporations(planetId, builder);
        saveContracts(planetId, builder);
        saveQuests(planetId, builder);
        saveTradeRoutes(planetId, builder);

        Map<String, Long> summary = getPlanetSummary(planetId);
        log.info("Planet {} saved: {}", planetId, summary);
    }

    /**
     * Saves all terrain cells for a planet.
     */
    @Transactional
    public void saveTerrainCells(int planetId, PlanetSurfaceBuilder builder) {
        Map<String, TerrainData> terrainMap = builder.getAllTerrainData();
        int saved = 0;
        for (Map.Entry<String, TerrainData> entry : terrainMap.entrySet()) {
            String key = entry.getKey();
            TerrainData terrain = entry.getValue();
            // Parse hex key "x,y,z"
            String[] parts = key.split(",");
            if (parts.length == 3) {
                try {
                    int x = Integer.parseInt(parts[0].trim());
                    int y = Integer.parseInt(parts[1].trim());
                    int z = Integer.parseInt(parts[2].trim());
                    Hex3D hex = new Hex3D(x, y, z);
                    saveTerrainCell(planetId, hex, terrain);
                    saved++;
                } catch (Exception ignored) {
                    // Skip invalid keys
                }
            }
        }
        log.debug("Saved {} terrain cells for planet {}", saved, planetId);
    }

    /**
     * Saves a single terrain cell.
     * Call this after any terrain change (biome generation, infrastructure build).
     */
    @Transactional
    public void saveTerrainCell(int planetId, Hex3D hex, TerrainData terrain) {
        PlanetTerrainCell cell = terrainRepository
                .findByPlanetIdAndHexXAndHexYAndHexZ(planetId, hex.getX(), hex.getY(), hex.getZ());

        if (cell == null) {
            cell = new PlanetTerrainCell();
            cell.setPlanetId(planetId);
            cell.setHexX(hex.getX());
            cell.setHexY(hex.getY());
            cell.setHexZ(hex.getZ());
        }

        cell.setTerrainType(terrain.getTerrainType().name());
        cell.setBiomeName(terrain.getBiome().getName());
        cell.setTemperature(terrain.getBiome().getTemperature());
        cell.setMoisture(terrain.getBiome().getMoisture());
        cell.setAltitude(terrain.getBiome().getAltitude());
        cell.setInfrastructure(terrain.getInfrastructure().name());
        cell.setWaterLevel(terrain.getWaterLevel());
        cell.setSettlementId(terrain.getSettlementId());
        cell.setIndustryId(terrain.getIndustryId());

        terrainRepository.save(cell);
    }

    /**
     * Saves all settlements for a planet.
     */
    @Transactional
    public void saveSettlements(int planetId, PlanetSurfaceBuilder builder) {
        List<Settlement> settlements = builder.getAllSettlements(
                new Planet(planetId));
        int saved = 0;
        for (Settlement s : settlements) {
            saveSettlement(planetId, s);
            saved++;
        }
        log.debug("Saved {} settlements for planet {}", saved, planetId);
    }

    /**
     * Saves a single settlement.
     */
    @Transactional
    public void saveSettlement(int planetId, Settlement settlement) {
        PlanetSettlementEntity entity = settlementRepository
                .findBySettlementId(settlement.getId())
                .orElse(new PlanetSettlementEntity());

        entity.setSettlementId(settlement.getId());
        entity.setPlanetId(planetId);
        entity.setName(settlement.getName());
        entity.setCenterHexX(settlement.getCenterHex().getX());
        entity.setCenterHexY(settlement.getCenterHex().getY());
        entity.setCenterHexZ(settlement.getCenterHex().getZ());
        entity.setSize(settlement.getSize().name());
        entity.setPopulation(settlement.getPopulation());
        entity.setMaxPopulation(settlement.getMaxPopulation());
        entity.setGrowthRate(settlement.getGrowthRate());
        entity.setMailProduction(settlement.getMailProduction());
        entity.setPassengerProduction(settlement.getPassengerProduction());
        entity.setActive(settlement.isActive());
        entity.setOwnerEmpireId(settlement.getOwnerEmpireId());

        // Serialize cargo goals
        entity.setCargoGoalsJson(serializeMap(settlement.getCargoGoals()));

        // Serialize dungeon IDs for interior rooms
        if (!settlement.getDungeonIds().isEmpty()) {
            entity.setDungeonIdsJson(serializeList(settlement.getDungeonIds()));
        }

        settlementRepository.save(entity);
    }

    /**
     * Saves all industries for a planet.
     */
    @Transactional
    public void saveIndustries(int planetId, PlanetSurfaceBuilder builder) {
        List<Industry> industries = builder.getAllIndustries(
                new Planet(planetId));
        int saved = 0;
        for (Industry i : industries) {
            saveIndustry(planetId, i);
            saved++;
        }
        log.debug("Saved {} industries for planet {}", saved, planetId);
    }

    /**
     * Saves a single industry.
     */
    @Transactional
    public void saveIndustry(int planetId, Industry industry) {
        PlanetIndustryEntity entity = industryRepository
                .findByIndustryId(industry.getId())
                .orElse(new PlanetIndustryEntity());

        entity.setIndustryId(industry.getId());
        entity.setPlanetId(planetId);
        entity.setIndustryType(industry.getType().name());
        entity.setHexX(industry.getPosition().getX());
        entity.setHexY(industry.getPosition().getY());
        entity.setHexZ(industry.getPosition().getZ());
        entity.setProductionLevel(industry.getProductionLevel());
        entity.setTurnsSinceBuilt(industry.getTurnsSinceBuilt());
        entity.setActive(industry.isActive());
        entity.setOwnerEmpireId(industry.getOwnerEmpireId());
        entity.setInputInventoryJson(serializeMap(industry.getInputInventory()));
        entity.setOutputInventoryJson(serializeMap(industry.getOutputInventory()));
        entity.setDungeonId(industry.getDungeonId());

        industryRepository.save(entity);
    }

    /**
     * Saves all stations for a planet.
     */
    @Transactional
    public void saveStations(int planetId, PlanetSurfaceBuilder builder) {
        List<Station> stations = builder.getAllStations(new Planet(planetId));
        int saved = 0;
        for (Station s : stations) {
            saveStation(planetId, s);
            saved++;
        }
        log.debug("Saved {} stations for planet {}", saved, planetId);
    }

    /**
     * Saves a single station.
     */
    @Transactional
    public void saveStation(int planetId, Station station) {
        PlanetStationEntity entity = stationRepository
                .findByStationId(station.getId())
                .orElse(new PlanetStationEntity());

        entity.setStationId(station.getId());
        entity.setPlanetId(planetId);
        entity.setName(station.getName());
        entity.setHexX(station.getPosition().getX());
        entity.setHexY(station.getPosition().getY());
        entity.setHexZ(station.getPosition().getZ());
        entity.setInfrastructureType(station.getInfrastructureType().name());
        entity.setPassengersWaiting(station.getPassengersWaiting());
        entity.setTotalIncome(station.getTotalIncome());
        entity.setConnectedSettlementId(station.getConnectedSettlementId());
        entity.setConnectedIndustryId(station.getConnectedIndustryId());
        entity.setOwnerEmpireId(station.getOwnerEmpireId());
        entity.setCargoWaitingJson(serializeMap(station.getCargoWaiting()));
        entity.setDungeonId(station.getDungeonId());

        stationRepository.save(entity);
    }

    /**
     * Saves all vehicles for a planet.
     */
    @Transactional
    public void saveVehicles(int planetId, PlanetSurfaceBuilder builder) {
        List<SurfaceVehicle> vehicles = builder.getAllVehicles(new Planet(planetId));
        int saved = 0;
        for (SurfaceVehicle v : vehicles) {
            saveVehicle(planetId, v);
            saved++;
        }
        log.debug("Saved {} vehicles for planet {}", saved, planetId);
    }

    /**
     * Saves a single vehicle.
     */
    @Transactional
    public void saveVehicle(int planetId, SurfaceVehicle vehicle) {
        PlanetVehicleEntity entity = vehicleRepository
                .findByVehicleId(vehicle.getId())
                .orElse(new PlanetVehicleEntity());

        entity.setVehicleId(vehicle.getId());
        entity.setPlanetId(planetId);
        entity.setVehicleType(vehicle.getType().name());
        entity.setCurrentHexX(vehicle.getCurrentPosition().getX());
        entity.setCurrentHexY(vehicle.getCurrentPosition().getY());
        entity.setCurrentHexZ(vehicle.getCurrentPosition().getZ());
        entity.setPassengerCount(vehicle.getPassengerCount());
        entity.setTotalDistance(vehicle.getTotalDistance());
        entity.setTotalCargoDelivered(vehicle.getTotalCargoDelivered());
        entity.setTotalPassengersDelivered(vehicle.getTotalPassengersDelivered());
        entity.setActive(vehicle.isActive());
        entity.setOwnerEmpireId(vehicle.getOwnerEmpireId());
        entity.setCurrentRouteIndex(vehicle.getCurrentRouteIndex());
        entity.setRouteStationIdsJson(serializeList(vehicle.getRouteStationIds()));
        entity.setCargoInventoryJson(serializeMap(vehicle.getCargoInventory()));

        vehicleRepository.save(entity);
    }

    /**
     * Saves all corporations for a planet.
     */
    @Transactional
    public void saveCorporations(int planetId, PlanetSurfaceBuilder builder) {
        List<TransportCorporation> corporations = builder.getAllCorporations(new Planet(planetId));
        int saved = 0;
        for (TransportCorporation c : corporations) {
            saveCorporation(planetId, c);
            saved++;
        }
        log.debug("Saved {} corporations for planet {}", saved, planetId);
    }

    /**
     * Saves a single corporation.
     */
    @Transactional
    public void saveCorporation(int planetId, TransportCorporation corp) {
        PlanetCorporationEntity entity = corporationRepository.findByCorporationId(corp.getId());
        if (entity == null) {
            entity = new PlanetCorporationEntity();
        }

        entity.setCorporationId(corp.getId());
        entity.setPlanetId(planetId);
        entity.setName(corp.getName());
        entity.setOwnerEmpireId(corp.getOwnerId());
        entity.setRank(corp.getRank().name());
        entity.setCredits(corp.getCredits());
        entity.setReputation(corp.getReputation());
        entity.setXp(corp.getXp());
        entity.setXpToNextRank(corp.getXpToNextRank());
        entity.setPlanetsOperated(corp.getPlanetsOperated());
        entity.setTotalCargoDelivered(corp.getTotalCargoDelivered());
        entity.setOwnedStationIdsJson(serializeList(new ArrayList<>(corp.getOwnedStationIds())));
        entity.setOwnedVehicleIdsJson(serializeList(new ArrayList<>(corp.getOwnedVehicleIds())));
        entity.setOwnedRouteIdsJson(serializeList(new ArrayList<>(corp.getOwnedRouteIds())));
        entity.setActiveContractIdsJson(serializeList(new ArrayList<>(corp.getActiveContractIds())));
        entity.setCargoInventoryJson(serializeMap(corp.getCargoInventory()));
        entity.setActiveQuestIdsJson(serializeList(corp.getActiveQuestIds()));
        entity.setCompletedQuestIdsJson(serializeList(corp.getCompletedQuestIds()));

        corporationRepository.save(entity);
    }

    // ---- Load operations ----

    /**
     * Loads the entire planet surface from the database into the builder.
     * <p>
     * This recreates all terrain cells, settlements, industries, stations,
     * and vehicles from the mapping database so the Galaxy Overlord doesn't
     * need to randomly generate them again.
     * </p>
     *
     * @param planet the planet to load data into
     * @param builder the surface builder to populate
     * @return true if data was loaded, false if no data exists
     */
    @Transactional(readOnly = true)
    public boolean loadPlanetSurface(Planet planet, PlanetSurfaceBuilder builder) {
        int planetId = planet.getId();

        if (!hasPlanetData(planetId)) {
            log.info("No saved data for planet {}, will need procedural generation", planetId);
            return false;
        }

        log.info("Loading planet surface for planet {} from database...", planetId);

        loadTerrainCells(planetId, builder);
        loadSettlements(planetId, builder);
        loadIndustries(planetId, builder);
        loadStations(planetId, builder);
        loadVehicles(planetId, builder);
        loadCorporations(planetId, builder);
        loadContracts(planetId, builder);
        loadQuests(planetId, builder);
        loadTradeRoutes(planetId, builder);

        Map<String, Long> summary = getPlanetSummary(planetId);
        log.info("Planet {} loaded from database: {}", planetId, summary);
        return true;
    }

    /**
     * Loads all terrain cells for a planet.
     * Reconstructs Biome and TerrainData objects from the entity fields
     * and puts them into the builder's terrainMap via reflection.
     */
    @Transactional(readOnly = true)
    public void loadTerrainCells(int planetId, PlanetSurfaceBuilder builder) {
        List<PlanetTerrainCell> cells = terrainRepository.findByPlanetId(planetId);
        for (PlanetTerrainCell cell : cells) {
            // Reconstruct the Biome from saved fields
            TerrainType terrainType = TerrainType.valueOf(cell.getTerrainType());
            Biome biome = new Biome(terrainType, cell.getTemperature(), cell.getMoisture(), cell.getAltitude());

            // Reconstruct TerrainData
            TerrainData terrain = new TerrainData(biome);
            terrain.setPlanetId(cell.getPlanetId());
            if (cell.getSettlementId() != null) {
                terrain.setSettlement(cell.getSettlementId());
            }
            if (cell.getIndustryId() != null) {
                terrain.setIndustry(cell.getIndustryId());
            }
            terrain.setWaterLevel(cell.getWaterLevel());

            // Restore infrastructure
            if (cell.getInfrastructure() != null) {
                try {
                    terrain.setInfrastructure(TerrainData.InfrastructureType.valueOf(cell.getInfrastructure()));
                } catch (IllegalArgumentException ignored) {
                    terrain.setInfrastructure(TerrainData.InfrastructureType.NONE);
                }
            }

            // Put into builder's terrainMap
            Hex3D hex = new Hex3D(cell.getHexX(), cell.getHexY(), cell.getHexZ());
            reAddTerrainToBuilder(builder, hex, terrain);
        }
        log.debug("Loaded {} terrain cells for planet {}", cells.size(), planetId);
    }

    /**
     * Loads all settlements for a planet.
     */
    @Transactional(readOnly = true)
    public void loadSettlements(int planetId, PlanetSurfaceBuilder builder) {
        List<PlanetSettlementEntity> entities = settlementRepository.findByPlanetId(planetId);
        for (PlanetSettlementEntity e : entities) {
            Hex3D centerHex = new Hex3D(e.getCenterHexX(), e.getCenterHexY(), e.getCenterHexZ());
            Settlement.Size size = Settlement.Size.valueOf(e.getSize());
            Settlement settlement = new Settlement(e.getSettlementId(), e.getName(), centerHex, size);
            settlement.setActive(e.isActive());
            settlement.setOwnerEmpireId(e.getOwnerEmpireId());

            // Restore dungeon IDs for interior rooms
            List<String> dungeonIds = deserializeList(e.getDungeonIdsJson());
            if (dungeonIds != null && !dungeonIds.isEmpty()) {
                settlement.setDungeonIds(dungeonIds);
            }

            // Re-add to builder's internal map
            reAddSettlementToBuilder(builder, planetId, settlement);
        }
        log.debug("Loaded {} settlements for planet {}", entities.size(), planetId);
    }

    /**
     * Loads all industries for a planet.
     */
    @Transactional(readOnly = true)
    public void loadIndustries(int planetId, PlanetSurfaceBuilder builder) {
        List<PlanetIndustryEntity> entities = industryRepository.findByPlanetId(planetId);
        for (PlanetIndustryEntity e : entities) {
            IndustryType type = IndustryType.valueOf(e.getIndustryType());
            Hex3D position = new Hex3D(e.getHexX(), e.getHexY(), e.getHexZ());
            Industry industry = new Industry(e.getIndustryId(), type, position);
            industry.setActive(e.isActive());
            industry.setOwnerEmpireId(e.getOwnerEmpireId());
            industry.setDungeonId(e.getDungeonId());

            reAddIndustryToBuilder(builder, planetId, industry);
        }
        log.debug("Loaded {} industries for planet {}", entities.size(), planetId);
    }

    /**
     * Loads all stations for a planet.
     */
    @Transactional(readOnly = true)
    public void loadStations(int planetId, PlanetSurfaceBuilder builder) {
        List<PlanetStationEntity> entities = stationRepository.findByPlanetId(planetId);
        for (PlanetStationEntity e : entities) {
            Hex3D position = new Hex3D(e.getHexX(), e.getHexY(), e.getHexZ());
            Station station = new Station(e.getStationId(), e.getName(), position);
            station.setInfrastructureType(TerrainData.InfrastructureType.valueOf(e.getInfrastructureType()));
            station.setConnectedSettlementId(e.getConnectedSettlementId());
            station.setConnectedIndustryId(e.getConnectedIndustryId());
            station.setOwnerEmpireId(e.getOwnerEmpireId());
            station.setDungeonId(e.getDungeonId());

            reAddStationToBuilder(builder, planetId, station);
        }
        log.debug("Loaded {} stations for planet {}", entities.size(), planetId);
    }

    /**
     * Loads all vehicles for a planet.
     * Restores route station IDs, cargo inventory, passenger count, and delivery stats.
     */
    @Transactional(readOnly = true)
    public void loadVehicles(int planetId, PlanetSurfaceBuilder builder) {
        List<PlanetVehicleEntity> entities = vehicleRepository.findByPlanetId(planetId);
        for (PlanetVehicleEntity e : entities) {
            VehicleType type = VehicleType.valueOf(e.getVehicleType());
            Hex3D position = new Hex3D(e.getCurrentHexX(), e.getCurrentHexY(), e.getCurrentHexZ());
            SurfaceVehicle vehicle = new SurfaceVehicle(e.getVehicleId(), type, position);
            vehicle.setActive(e.isActive());
            vehicle.setOwnerEmpireId(e.getOwnerEmpireId());

            // Restore route station IDs
            List<String> routeIds = deserializeList(e.getRouteStationIdsJson());
            if (routeIds != null && !routeIds.isEmpty()) {
                try {
                    vehicle.setRoute(routeIds);
                } catch (IllegalArgumentException ignored) {
                    // Route validation failed; vehicle will have empty route
                }
            }

            // Restore current route index
            vehicle.setCurrentRouteIndex(e.getCurrentRouteIndex());

            // Restore cargo inventory
            Map<String, String> cargoMap = deserializeMap(e.getCargoInventoryJson());
            if (cargoMap != null) {
                for (Map.Entry<String, String> entry : cargoMap.entrySet()) {
                    try {
                        CargoType cargoType = CargoType.valueOf(entry.getKey());
                        int amount = Integer.parseInt(entry.getValue());
                        if (amount > 0) {
                            vehicle.loadCargo(cargoType, amount);
                        }
                    } catch (NumberFormatException ignored) {
                    } catch (IllegalArgumentException ignored) {
                        // Skip invalid cargo entries
                    }
                }
            }

            // Restore passenger count and delivery stats
            vehicle.setPassengerCount(e.getPassengerCount());
            vehicle.setTotalDistance(e.getTotalDistance());
            vehicle.setTotalCargoDelivered(e.getTotalCargoDelivered());
            vehicle.setTotalPassengersDelivered(e.getTotalPassengersDelivered());

            reAddVehicleToBuilder(builder, planetId, vehicle);
        }
        log.debug("Loaded {} vehicles for planet {}", entities.size(), planetId);
    }

    /**
     * Loads all corporations for a planet.
     * Reconstructs TransportCorporation objects from the entity fields
     * and puts them into the builder's planetCorporations via reflection.
     */
    @Transactional(readOnly = true)
    public void loadCorporations(int planetId, PlanetSurfaceBuilder builder) {
        List<PlanetCorporationEntity> entities = corporationRepository.findByPlanetId(planetId);
        for (PlanetCorporationEntity e : entities) {
            // Create a new corporation â€” we use a dummy owner to construct, then restore state
            TransportCorporation corp = new TransportCorporation(e.getName(), e.getOwnerEmpireId(), e.getCredits());

            // Replace the auto-generated ID with the saved one via reflection
            try {
                java.lang.reflect.Field idField = TransportCorporation.class.getDeclaredField("id");
                idField.setAccessible(true);
                idField.set(corp, e.getCorporationId());
            } catch (Exception ex) {
                log.warn("Could not restore corporation ID for {}", e.getCorporationId());
            }

            // Restore rank
            try {
                java.lang.reflect.Field rankField = TransportCorporation.class.getDeclaredField("rank");
                rankField.setAccessible(true);
                rankField.set(corp, CorporationRank.valueOf(e.getRank()));
            } catch (Exception ex) {
                log.warn("Could not restore rank for {}", e.getCorporationId());
            }

            // Restore primitive fields via reflection (no setters for these)
            try {
                java.lang.reflect.Field repField = TransportCorporation.class.getDeclaredField("reputation");
                repField.setAccessible(true);
                repField.set(corp, e.getReputation());

                java.lang.reflect.Field xpField = TransportCorporation.class.getDeclaredField("xp");
                xpField.setAccessible(true);
                xpField.set(corp, e.getXp());

                java.lang.reflect.Field xpNextField = TransportCorporation.class.getDeclaredField("xpToNextRank");
                xpNextField.setAccessible(true);
                xpNextField.set(corp, e.getXpToNextRank());

                java.lang.reflect.Field planetsField = TransportCorporation.class.getDeclaredField("planetsOperated");
                planetsField.setAccessible(true);
                planetsField.set(corp, e.getPlanetsOperated());

                java.lang.reflect.Field cargoField = TransportCorporation.class.getDeclaredField("totalCargoDelivered");
                cargoField.setAccessible(true);
                cargoField.set(corp, e.getTotalCargoDelivered());
            } catch (Exception ex) {
                log.warn("Could not restore primitive fields for {}", e.getCorporationId());
            }

            // Restore owned station IDs
            List<String> stationIds = deserializeList(e.getOwnedStationIdsJson());
            if (stationIds != null) {
                for (String sid : stationIds) corp.addStation(sid);
            }

            // Restore owned vehicle IDs
            List<String> vehicleIds = deserializeList(e.getOwnedVehicleIdsJson());
            if (vehicleIds != null) {
                for (String vid : vehicleIds) corp.addVehicle(vid);
            }

            // Restore owned route IDs
            List<String> routeIds = deserializeList(e.getOwnedRouteIdsJson());
            if (routeIds != null) {
                for (String rid : routeIds) corp.addRoute(rid);
            }

            // Restore active contract IDs
            List<String> contractIds = deserializeList(e.getActiveContractIdsJson());
            if (contractIds != null) {
                for (String cid : contractIds) corp.addContract(cid);
            }

            // Restore cargo inventory
            Map<String, String> cargoMap = deserializeMap(e.getCargoInventoryJson());
            if (cargoMap != null) {
                for (Map.Entry<String, String> entry : cargoMap.entrySet()) {
                    try {
                        CargoType cargoType = CargoType.valueOf(entry.getKey());
                        int amount = Integer.parseInt(entry.getValue());
                        if (amount > 0) corp.addCargo(cargoType, amount);
                    } catch (Exception ignored) {
                    }
                }
            }

            // Restore active quest IDs
            List<String> activeQuests = deserializeList(e.getActiveQuestIdsJson());
            if (activeQuests != null) {
                try {
                    java.lang.reflect.Field field = TransportCorporation.class.getDeclaredField("activeQuestIds");
                    field.setAccessible(true);
                    @SuppressWarnings("unchecked")
                    List<String> list = (List<String>) field.get(corp);
                    list.addAll(activeQuests);
                } catch (Exception ignored) {
                }
            }

            // Restore completed quest IDs
            List<String> completedQuests = deserializeList(e.getCompletedQuestIdsJson());
            if (completedQuests != null) {
                try {
                    java.lang.reflect.Field field = TransportCorporation.class.getDeclaredField("completedQuestIds");
                    field.setAccessible(true);
                    @SuppressWarnings("unchecked")
                    List<String> list = (List<String>) field.get(corp);
                    list.addAll(completedQuests);
                } catch (Exception ignored) {
                }
            }

            reAddCorporationToBuilder(builder, planetId, corp);
        }
        log.debug("Loaded {} corporations for planet {}", entities.size(), planetId);
    }

    // ---- Contracts ----

    @Transactional
    public void saveContracts(int planetId, PlanetSurfaceBuilder builder) {
        List<DeliveryContract> contracts = builder.getAllContracts(new Planet(planetId));
        int saved = 0;
        for (DeliveryContract c : contracts) {
            saveContract(planetId, c);
            saved++;
        }
        log.debug("Saved {} contracts for planet {}", saved, planetId);
    }

    @Transactional
    public void saveContract(int planetId, DeliveryContract contract) {
        PlanetContractEntity entity = contractRepository
                .findByContractId(contract.getId())
                .orElse(new PlanetContractEntity());

        entity.setContractId(contract.getId());
        entity.setPlanetId(planetId);
        entity.setName(contract.getName());
        entity.setType(contract.getType().name());
        entity.setCargoType(contract.getCargoType().name());
        entity.setCargoAmount(contract.getCargoAmount());
        entity.setFromStationId(contract.getFromStationId());
        entity.setToStationId(contract.getToStationId());
        entity.setPayCredits(contract.getPayCredits());
        entity.setXpReward(contract.getXpReward());
        entity.setReputationReward(contract.getReputationReward());
        entity.setStatus(contract.getStatus().name());
        entity.setIssuerEmpireId(contract.getIssuerEmpireId());
        entity.setAssignedCorporationId(contract.getAssignedCorporationId());
        entity.setCreatedAt(contract.getCreatedAt());
        entity.setExpiresAt(contract.getExpiresAt());
        entity.setCompletedAt(contract.getCompletedAt());

        contractRepository.save(entity);
    }

    @Transactional
    public void loadContracts(int planetId, PlanetSurfaceBuilder builder) {
        List<PlanetContractEntity> entities = contractRepository.findByPlanetId(planetId);
        for (PlanetContractEntity e : entities) {
            DeliveryContract.ContractType type = DeliveryContract.ContractType.valueOf(e.getType());
            CargoType cargo = CargoType.valueOf(e.getCargoType());

            DeliveryContract contract = new DeliveryContract(
                    e.getName(), type, cargo, e.getCargoAmount(),
                    e.getFromStationId(), e.getToStationId(),
                    e.getPayCredits(), e.getXpReward(), e.getReputationReward(),
                    e.getIssuerEmpireId()
            );

            if (e.getAssignedCorporationId() != null) {
                contract.setAssignedCorporationId(e.getAssignedCorporationId());
            }
            if (e.getCompletedAt() != null) {
                contract.setCompletedAt(e.getCompletedAt());
            }

            builder.addContract(new Planet(planetId), contract);
        }
        log.debug("Loaded {} contracts for planet {}", entities.size(), planetId);
    }

    // ---- Quests ----

    @Transactional
    public void saveQuests(int planetId, PlanetSurfaceBuilder builder) {
        List<PlanetQuest> quests = builder.getAllQuests(new Planet(planetId));
        int saved = 0;
        for (PlanetQuest q : quests) {
            saveQuest(planetId, q);
            saved++;
        }
        log.debug("Saved {} quests for planet {}", saved, planetId);
    }

    @Transactional
    public void saveQuest(int planetId, PlanetQuest quest) {
        RpgQuestEntity entity = questRepository
                .findByQuestId(quest.getId())
                .orElse(new RpgQuestEntity());

        entity.setQuestId(quest.getId());
        entity.setPlanetId(planetId);
        entity.setTitle(quest.getTitle());
        entity.setDescription(quest.getDescription());
        entity.setType(quest.getType().name());
        entity.setDifficulty(quest.getDifficulty());
        entity.setCreditsReward(quest.getCreditsReward());
        entity.setXpReward(quest.getXpReward());
        entity.setReputationReward(quest.getReputationReward());
        entity.setStatus(quest.getStatus().name());
        entity.setIssuerName(quest.getIssuerName());
        entity.setIssuerEmpireId(quest.getIssuerEmpireId());
        entity.setObjectivesJson(serializeList(quest.getObjectives()));
        entity.setRequiredStationIdsJson(serializeList(quest.getRequiredStationIds()));
        entity.setAssignedCorporationId(quest.getAssignedCorporationId());
        entity.setCreatedAt(quest.getCreatedAt());
        entity.setExpiresAt(quest.getExpiresAt());
        entity.setCompletedAt(quest.getCompletedAt());

        questRepository.save(entity);
    }

    @Transactional
    public void loadQuests(int planetId, PlanetSurfaceBuilder builder) {
        List<RpgQuestEntity> entities = questRepository.findByPlanetId(planetId);
        for (RpgQuestEntity e : entities) {
            PlanetQuest.QuestType type = PlanetQuest.QuestType.valueOf(e.getType());

            PlanetQuest quest = PlanetQuest.builder()
                    .title(e.getTitle())
                    .description(e.getDescription())
                    .type(type)
                    .difficulty(e.getDifficulty())
                    .creditsReward(e.getCreditsReward())
                    .xpReward(e.getXpReward())
                    .reputationReward(e.getReputationReward())
                    .issuerName(e.getIssuerName())
                    .issuerEmpireId(e.getIssuerEmpireId())
                    .build();

            if (e.getAssignedCorporationId() != null) {
                quest.setAssignedCorporationId(e.getAssignedCorporationId());
            }
            if (e.getCompletedAt() != null) {
                quest.setCompletedAt(e.getCompletedAt());
            }

            // Restore objectives
            List<String> objectives = deserializeList(e.getObjectivesJson());
            if (objectives != null) {
                try {
                    java.lang.reflect.Field field = PlanetQuest.class.getDeclaredField("objectives");
                    field.setAccessible(true);
                    @SuppressWarnings("unchecked")
                    List<String> list = (List<String>) field.get(quest);
                    list.addAll(objectives);
                } catch (Exception ignored) {
                }
            }

            // Restore required station IDs
            List<String> stationIds = deserializeList(e.getRequiredStationIdsJson());
            if (stationIds != null) {
                try {
                    java.lang.reflect.Field field = PlanetQuest.class.getDeclaredField("requiredStationIds");
                    field.setAccessible(true);
                    @SuppressWarnings("unchecked")
                    List<String> list = (List<String>) field.get(quest);
                    list.addAll(stationIds);
                } catch (Exception ignored) {
                }
            }

            builder.addQuest(new Planet(planetId), quest);
        }
        log.debug("Loaded {} quests for planet {}", entities.size(), planetId);
    }

    // ---- Trade Routes ----

    @Transactional
    public void saveTradeRoutes(int planetId, PlanetSurfaceBuilder builder) {
        List<CampaignTradeRoute> routes = builder.getAllTradeRoutes(new Planet(planetId));
        int saved = 0;
        for (CampaignTradeRoute r : routes) {
            saveTradeRoute(planetId, r);
            saved++;
        }
        log.debug("Saved {} trade routes for planet {}", saved, planetId);
    }

    @Transactional
    public void saveTradeRoute(int planetId, CampaignTradeRoute route) {
        CampaignTradeRouteEntity entity = tradeRouteRepository
                .findByRouteId(route.getId())
                .orElse(new CampaignTradeRouteEntity());

        entity.setRouteId(route.getId());
        entity.setPlanetId(planetId);
        entity.setName(route.getName());
        entity.setOwnerCorporationId(route.getOwnerCorporationId());
        entity.setStationIdsJson(serializeList(route.getStationIds()));
        entity.setVehicleIdsJson(serializeList(route.getVehicleIds()));

        // Serialize cargo types
        List<String> cargoTypeNames = new ArrayList<>();
        for (CargoType ct : route.getCargoTypes()) {
            cargoTypeNames.add(ct.name());
        }
        entity.setCargoTypesJson(serializeList(cargoTypeNames));

        entity.setStatus(route.getStatus().name());
        entity.setTotalDistance(route.getTotalDistance());
        entity.setDailyIncome(route.getDailyIncome());
        entity.setTotalIncome(route.getTotalIncome());
        entity.setCargoDeliveredToday(route.getCargoDeliveredToday());

        tradeRouteRepository.save(entity);
    }

    @Transactional
    public void loadTradeRoutes(int planetId, PlanetSurfaceBuilder builder) {
        List<CampaignTradeRouteEntity> entities = tradeRouteRepository.findByPlanetId(planetId);
        for (CampaignTradeRouteEntity e : entities) {
            List<String> stationIds = deserializeList(e.getStationIdsJson());
            List<String> vehicleIds = deserializeList(e.getVehicleIdsJson());
            List<String> cargoTypeNames = deserializeList(e.getCargoTypesJson());

            CampaignTradeRoute.Builder routeBuilder = CampaignTradeRoute.builder()
                    .name(e.getName())
                    .ownerCorporationId(e.getOwnerCorporationId())
                    .totalDistance(e.getTotalDistance());

            if (stationIds != null) {
                for (String sid : stationIds) {
                    routeBuilder.addStationId(sid);
                }
            }
            if (vehicleIds != null) {
                for (String vid : vehicleIds) {
                    routeBuilder.addVehicleId(vid);
                }
            }
            if (cargoTypeNames != null) {
                for (String ctName : cargoTypeNames) {
                    try {
                        routeBuilder.addCargoType(CargoType.valueOf(ctName));
                    } catch (Exception ignored) {
                    }
                }
            }

            CampaignTradeRoute route = routeBuilder.build();
            route.setDailyIncome(e.getDailyIncome());
            route.setTotalIncome(e.getTotalIncome());
            route.setCargoDeliveredToday(e.getCargoDeliveredToday());

            builder.addTradeRoute(new Planet(planetId), route);
        }
        log.debug("Loaded {} trade routes for planet {}", entities.size(), planetId);
    }

    // ---- Delete operations ----

    /**
     * Clears all saved data for a planet (used when regenerating a planet).
     */
    @Transactional
    public void clearPlanetData(int planetId) {
        terrainRepository.deleteByPlanetId(planetId);
        settlementRepository.deleteByPlanetId(planetId);
        industryRepository.deleteByPlanetId(planetId);
        stationRepository.deleteByPlanetId(planetId);
        vehicleRepository.deleteByPlanetId(planetId);
        corporationRepository.deleteByPlanetId(planetId);
        contractRepository.deleteByPlanetId(planetId);
        questRepository.deleteByPlanetId(planetId);
        tradeRouteRepository.deleteByPlanetId(planetId);
        log.info("Cleared all saved data for planet {}", planetId);
    }

    /**
     * Lists all planet IDs that have persisted terrain data.
     *
     * @return set of planet IDs with saved data
     */
    @Transactional(readOnly = true)
    public java.util.Set<Integer> listPersistedPlanets() {
        return terrainRepository.findDistinctPlanetIds();
    }

    // ---- Reflection-based builder integration ----
    // These methods re-add objects to the builder's internal maps using reflection
    // because the builder's internal fields are private.

    private void reAddSettlementToBuilder(PlanetSurfaceBuilder builder, int planetId, Settlement settlement) {
        try {
            java.lang.reflect.Field field = builder.getClass()
                    .getDeclaredField("planetSettlements");
            field.setAccessible(true);
            @SuppressWarnings("unchecked")
            Map<Integer, List<Settlement>> map = (Map<Integer, List<Settlement>>) field.get(builder);
            map.computeIfAbsent(planetId, k -> new ArrayList<>()).add(settlement);
        } catch (Exception e) {
            log.error("Failed to re-add settlement to builder: {}", e.getMessage());
        }
    }

    private void reAddIndustryToBuilder(PlanetSurfaceBuilder builder, int planetId, Industry industry) {
        try {
            java.lang.reflect.Field field = builder.getClass()
                    .getDeclaredField("planetIndustries");
            field.setAccessible(true);
            @SuppressWarnings("unchecked")
            Map<Integer, List<Industry>> map = (Map<Integer, List<Industry>>) field.get(builder);
            map.computeIfAbsent(planetId, k -> new ArrayList<>()).add(industry);
        } catch (Exception e) {
            log.error("Failed to re-add industry to builder: {}", e.getMessage());
        }
    }

    private void reAddStationToBuilder(PlanetSurfaceBuilder builder, int planetId, Station station) {
        try {
            java.lang.reflect.Field field = builder.getClass()
                    .getDeclaredField("planetStations");
            field.setAccessible(true);
            @SuppressWarnings("unchecked")
            Map<Integer, List<Station>> map = (Map<Integer, List<Station>>) field.get(builder);
            map.computeIfAbsent(planetId, k -> new ArrayList<>()).add(station);
        } catch (Exception e) {
            log.error("Failed to re-add station to builder: {}", e.getMessage());
        }
    }

    private void reAddVehicleToBuilder(PlanetSurfaceBuilder builder, int planetId, SurfaceVehicle vehicle) {
        try {
            java.lang.reflect.Field field = builder.getClass()
                    .getDeclaredField("planetVehicles");
            field.setAccessible(true);
            @SuppressWarnings("unchecked")
            Map<Integer, List<SurfaceVehicle>> map = (Map<Integer, List<SurfaceVehicle>>) field.get(builder);
            map.computeIfAbsent(planetId, k -> new ArrayList<>()).add(vehicle);
        } catch (Exception e) {
            log.error("Failed to re-add vehicle to builder: {}", e.getMessage());
        }
    }

    private void reAddTerrainToBuilder(PlanetSurfaceBuilder builder, Hex3D hex, TerrainData terrain) {
        try {
            java.lang.reflect.Field field = builder.getClass()
                    .getDeclaredField("terrainMap");
            field.setAccessible(true);
            @SuppressWarnings("unchecked")
            Map<String, TerrainData> map = (Map<String, TerrainData>) field.get(builder);
            map.put(hex.getX() + "," + hex.getY() + "," + hex.getZ(), terrain);
        } catch (Exception e) {
            log.error("Failed to re-add terrain to builder: {}", e.getMessage());
        }
    }

    private void reAddCorporationToBuilder(PlanetSurfaceBuilder builder, int planetId, TransportCorporation corp) {
        try {
            java.lang.reflect.Field field = builder.getClass()
                    .getDeclaredField("planetCorporations");
            field.setAccessible(true);
            @SuppressWarnings("unchecked")
            Map<Integer, List<TransportCorporation>> map = (Map<Integer, List<TransportCorporation>>) field.get(builder);
            map.computeIfAbsent(planetId, k -> new ArrayList<>()).add(corp);
        } catch (Exception e) {
            log.error("Failed to re-add corporation to builder: {}", e.getMessage());
        }
    }

    private void reAddContractToBuilder(PlanetSurfaceBuilder builder, int planetId, DeliveryContract contract) {
        try {
            java.lang.reflect.Field field = builder.getClass()
                    .getDeclaredField("planetContracts");
            field.setAccessible(true);
            @SuppressWarnings("unchecked")
            Map<Integer, List<DeliveryContract>> map = (Map<Integer, List<DeliveryContract>>) field.get(builder);
            map.computeIfAbsent(planetId, k -> new ArrayList<>()).add(contract);
        } catch (Exception e) {
            log.error("Failed to re-add contract to builder: {}", e.getMessage());
        }
    }

    private void reAddQuestToBuilder(PlanetSurfaceBuilder builder, int planetId, PlanetQuest quest) {
        try {
            java.lang.reflect.Field field = builder.getClass()
                    .getDeclaredField("planetQuests");
            field.setAccessible(true);
            @SuppressWarnings("unchecked")
            Map<Integer, List<PlanetQuest>> map = (Map<Integer, List<PlanetQuest>>) field.get(builder);
            map.computeIfAbsent(planetId, k -> new ArrayList<>()).add(quest);
        } catch (Exception e) {
            log.error("Failed to re-add quest to builder: {}", e.getMessage());
        }
    }

    private void reAddTradeRouteToBuilder(PlanetSurfaceBuilder builder, int planetId, CampaignTradeRoute route) {
        try {
            java.lang.reflect.Field field = builder.getClass()
                    .getDeclaredField("planetTradeRoutes");
            field.setAccessible(true);
            @SuppressWarnings("unchecked")
            Map<Integer, List<CampaignTradeRoute>> map = (Map<Integer, List<CampaignTradeRoute>>) field.get(builder);
            map.computeIfAbsent(planetId, k -> new ArrayList<>()).add(route);
        } catch (Exception e) {
            log.error("Failed to re-add trade route to builder: {}", e.getMessage());
        }
    }

    // ---- Serialization helpers ----

    private String serializeMap(Map<?, ?> map) {
        if (map == null || map.isEmpty()) return null;
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            if (!first) sb.append(",");
            sb.append("\"").append(entry.getKey().toString()).append("\":\"")
              .append(entry.getValue().toString()).append("\"");
            first = false;
        }
        sb.append("}");
        return sb.toString();
    }

    private String serializeList(List<?> list) {
        if (list == null || list.isEmpty()) return null;
        StringBuilder sb = new StringBuilder("[");
        boolean first = true;
        for (Object item : list) {
            if (!first) sb.append(",");
            sb.append("\"").append(item.toString()).append("\"");
            first = false;
        }
        sb.append("]");
        return sb.toString();
    }

    /**
     * Deserializes a JSON string like "[\"A\",\"B\"]" into a List of Strings.
     */
    private List<String> deserializeList(String json) {
        if (json == null || json.isEmpty() || json.equals("[]")) return null;
        // Strip brackets
        String inner = json.trim();
        if (inner.startsWith("[")) inner = inner.substring(1);
        if (inner.endsWith("]")) inner = inner.substring(0, inner.length() - 1);
        inner = inner.trim();
        if (inner.isEmpty()) return null;

        List<String> result = new ArrayList<>();
        // Split by comma, but respect quoted strings
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < inner.length(); i++) {
            char c = inner.charAt(i);
            if (c == '"') {
                inQuotes = !inQuotes;
                current.append(c);
            } else if (c == ',' && !inQuotes) {
                String token = current.toString().trim().strip().replaceAll("^\"|\"$", "");
                if (!token.isEmpty()) result.add(token);
                current = new StringBuilder();
            } else {
                current.append(c);
            }
        }
        String last = current.toString().trim().replaceAll("^\"|\"$", "");
        if (!last.isEmpty()) result.add(last);
        return result;
    }

    /**
     * Deserializes a JSON string like "{\"KEY\":\"VALUE\"}" into a Map of Strings.
     */
    private Map<String, String> deserializeMap(String json) {
        if (json == null || json.isEmpty() || json.equals("{}")) return null;
        // Strip braces
        String inner = json.trim();
        if (inner.startsWith("{")) inner = inner.substring(1);
        if (inner.endsWith("}")) inner = inner.substring(0, inner.length() - 1);
        inner = inner.trim();
        if (inner.isEmpty()) return null;

        Map<String, String> result = new HashMap<>();
        // Split by comma, respecting quoted strings
        List<String> pairs = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < inner.length(); i++) {
            char c = inner.charAt(i);
            if (c == '"') {
                inQuotes = !inQuotes;
                current.append(c);
            } else if (c == ',' && !inQuotes) {
                pairs.add(current.toString().trim());
                current = new StringBuilder();
            } else {
                current.append(c);
            }
        }
        String last = current.toString().trim();
        if (!last.isEmpty()) pairs.add(last);

        for (String pair : pairs) {
            int colonIdx = pair.indexOf(':');
            if (colonIdx > 0) {
                String key = pair.substring(0, colonIdx).trim().replaceAll("^\"|\"$", "");
                String value = pair.substring(colonIdx + 1).trim().replaceAll("^\"|\"$", "");
                result.put(key, value);
            }
        }
        return result;
    }
}
