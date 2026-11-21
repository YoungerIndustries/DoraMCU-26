package com.yisy.modules.doramcu.hex.planet;

import com.yisy.modules.doramcu.hex.Hex3D;
import com.yisy.modules.doramcu.hex.Planet;
import com.yisy.modules.doramcu.model.PlanetIndustryEntity;
import com.yisy.modules.doramcu.model.PlanetSettlementEntity;
import com.yisy.modules.doramcu.model.PlanetStationEntity;
import com.yisy.modules.doramcu.model.PlanetTerrainCell;
import com.yisy.modules.doramcu.model.PlanetVehicleEntity;
import com.yisy.modules.doramcu.repository.PlanetIndustryRepository;
import com.yisy.modules.doramcu.repository.PlanetSettlementRepository;
import com.yisy.modules.doramcu.repository.PlanetStationRepository;
import com.yisy.modules.doramcu.repository.PlanetTerrainCellRepository;
import com.yisy.modules.doramcu.repository.PlanetVehicleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * End-to-end integration tests for the planet surface build round-trip.
 * <p>
 * Verifies that the Galaxy Overlord's planet surface persistence works correctly:
 * terrain cells, settlements, industries, stations, and vehicles can be saved
 * and loaded, ensuring the planet doesn't need to be randomly regenerated.
 * </p>
 * <p>
 * These tests directly create domain objects and use the persistence service
 * to save and load them, bypassing biome generation which depends on the
 * Hex3D event system not available in the test context.
 * </p>
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = PersistenceTestConfig.class)
@ActiveProfiles("test")
@Transactional
class PlanetBuildRoundTripTest {

    @Autowired
    private DefaultPlanetSurfaceBuilder builder;

    @Autowired
    private PlanetSurfacePersistenceService persistenceService;

    @Autowired
    private PlanetTerrainCellRepository terrainRepository;

    @Autowired
    private PlanetSettlementRepository settlementRepository;

    @Autowired
    private PlanetIndustryRepository industryRepository;

    @Autowired
    private PlanetStationRepository stationRepository;

    @Autowired
    private PlanetVehicleRepository vehicleRepository;

    private static final int PLANET_ID = 1001;

    @BeforeEach
    void setUp() {
        persistenceService.clearPlanetData(PLANET_ID);
    }

    @Test
    void saveTerrainCell_thenLoad_roundTrip() {
        // Create terrain data directly
        Hex3D hex = new Hex3D(1, 0, -1);
        Biome biome = new Biome(TerrainType.GRASSLAND, 15.0, 0.5, 100);
        TerrainData terrain = new TerrainData(biome);

        // Save terrain cell
        persistenceService.saveTerrainCell(PLANET_ID, hex, terrain);

        // Verify saved
        List<PlanetTerrainCell> cells = terrainRepository.findByPlanetId(PLANET_ID);
        assertEquals(1, cells.size());
        assertEquals("GRASSLAND", cells.get(0).getTerrainType());
        assertEquals("Temperate Grassland", cells.get(0).getBiomeName());
        assertEquals(1, cells.get(0).getHexX());
        assertEquals(0, cells.get(0).getHexY());
        assertEquals(-1, cells.get(0).getHexZ());

        // Verify hasPlanetData detects the terrain
        assertTrue(persistenceService.hasPlanetData(PLANET_ID));
    }

    @Test
    void saveSettlement_thenLoad_roundTrip() {
        Planet planet = new Planet(PLANET_ID);
        Hex3D centerHex = new Hex3D(5, -3, -2);

        // Create a settlement directly
        Settlement settlement = new Settlement("SETTLE-TEST", "New Hope", centerHex, Settlement.Size.VILLAGE);
        settlement.setOwnerEmpireId("empire-1");
        settlement.setGrowthRate(10);

        // Save via persistence service
        persistenceService.saveSettlement(PLANET_ID, settlement);

        // Verify saved to DB
        List<PlanetSettlementEntity> saved = settlementRepository.findByPlanetId(PLANET_ID);
        assertEquals(1, saved.size());
        assertEquals("New Hope", saved.get(0).getName());
        assertEquals("empire-1", saved.get(0).getOwnerEmpireId());
        assertEquals("VILLAGE", saved.get(0).getSize());
    }

    @Test
    void saveIndustry_thenLoad_roundTrip() {
        Hex3D position = new Hex3D(10, -5, -5);
        Industry industry = new Industry("IND-TEST", IndustryType.COAL_MINE, position);
        industry.setOwnerEmpireId("empire-2");

        // Save
        persistenceService.saveIndustry(PLANET_ID, industry);

        // Verify saved
        List<PlanetIndustryEntity> saved = industryRepository.findByPlanetId(PLANET_ID);
        assertEquals(1, saved.size());
        assertEquals("COAL_MINE", saved.get(0).getIndustryType());
        assertEquals("empire-2", saved.get(0).getOwnerEmpireId());
    }

    @Test
    void saveStation_thenLoad_roundTrip() {
        Hex3D position = new Hex3D(15, -10, -5);
        Station station = new Station("STAT-TEST", "Central Station", position);
        station.setInfrastructureType(TerrainData.InfrastructureType.STATION);
        station.setConnectedSettlementId("SETTLE-1");
        station.setConnectedIndustryId("IND-1");
        station.setOwnerEmpireId("empire-1");

        // Save
        persistenceService.saveStation(PLANET_ID, station);

        // Verify saved
        List<PlanetStationEntity> saved = stationRepository.findByPlanetId(PLANET_ID);
        assertEquals(1, saved.size());
        assertEquals("Central Station", saved.get(0).getName());
        assertEquals("empire-1", saved.get(0).getOwnerEmpireId());
        assertEquals("SETTLE-1", saved.get(0).getConnectedSettlementId());
    }

    @Test
    void saveVehicle_thenLoad_roundTrip() {
        Hex3D position = new Hex3D(20, -10, -10);
        SurfaceVehicle vehicle = new SurfaceVehicle("VEH-TEST", VehicleType.BUS, position);
        vehicle.setOwnerEmpireId("empire-1");

        // Save
        persistenceService.saveVehicle(PLANET_ID, vehicle);

        // Verify saved
        List<PlanetVehicleEntity> saved = vehicleRepository.findByPlanetId(PLANET_ID);
        assertEquals(1, saved.size());
        assertEquals("BUS", saved.get(0).getVehicleType());
        assertEquals("empire-1", saved.get(0).getOwnerEmpireId());
    }

    @Test
    void saveMultipleObjectTypes_fullPlanetSurface() {
        Planet planet = new Planet(PLANET_ID);

        // Save terrain cells
        for (int i = 0; i < 5; i++) {
            Hex3D hex = new Hex3D(i, -i, 0);
            Biome biome = new Biome(TerrainType.GRASSLAND, 15.0, 0.5, 100);
            TerrainData terrain = new TerrainData(biome);
            persistenceService.saveTerrainCell(PLANET_ID, hex, terrain);
        }

        // Save settlements
        Hex3D settleHex = new Hex3D(5, -3, -2);
        Settlement settlement = new Settlement("SETTLE-1", "Capital", settleHex, Settlement.Size.CITY);
        settlement.setOwnerEmpireId("empire-1");
        persistenceService.saveSettlement(PLANET_ID, settlement);

        // Save industry
        Hex3D indHex = new Hex3D(10, -5, -5);
        Industry industry = new Industry("IND-1", IndustryType.COAL_MINE, indHex);
        industry.setOwnerEmpireId("empire-1");
        persistenceService.saveIndustry(PLANET_ID, industry);

        // Save station
        Hex3D statHex = new Hex3D(15, -10, -5);
        Station station = new Station("STAT-1", "Main Station", statHex);
        station.setOwnerEmpireId("empire-1");
        persistenceService.saveStation(PLANET_ID, station);

        // Save vehicle
        Hex3D vehHex = new Hex3D(20, -10, -10);
        SurfaceVehicle vehicle = new SurfaceVehicle("VEH-1", VehicleType.BUS, vehHex);
        vehicle.setOwnerEmpireId("empire-1");
        persistenceService.saveVehicle(PLANET_ID, vehicle);

        // Verify summary
        Map<String, Long> summary = persistenceService.getPlanetSummary(PLANET_ID);
        assertEquals(5L, summary.get("terrainCells"));
        assertEquals(1L, summary.get("settlements"));
        assertEquals(1L, summary.get("industries"));
        assertEquals(1L, summary.get("stations"));
        assertEquals(1L, summary.get("vehicles"));

        // Verify hasPlanetData
        assertTrue(persistenceService.hasPlanetData(PLANET_ID));
    }

    @Test
    void multiplePlanets_dataIsIsolated() {
        int planetA = 3001;
        int planetB = 3002;

        // Save settlement on planet A
        Hex3D hexA = new Hex3D(1, 0, -1);
        Settlement settleA = new Settlement("SETTLE-A", "CityA", hexA, Settlement.Size.VILLAGE);
        settleA.setOwnerEmpireId("empire-1");
        persistenceService.saveSettlement(planetA, settleA);

        // Save settlement on planet B
        Hex3D hexB = new Hex3D(2, -1, -1);
        Settlement settleB = new Settlement("SETTLE-B", "CityB", hexB, Settlement.Size.TOWN);
        settleB.setOwnerEmpireId("empire-2");
        persistenceService.saveSettlement(planetB, settleB);

        // Verify isolation
        Map<String, Long> summaryA = persistenceService.getPlanetSummary(planetA);
        Map<String, Long> summaryB = persistenceService.getPlanetSummary(planetB);

        assertEquals(1L, summaryA.get("settlements"));
        assertEquals(1L, summaryB.get("settlements"));

        List<PlanetSettlementEntity> settlementsA = settlementRepository.findByPlanetId(planetA);
        List<PlanetSettlementEntity> settlementsB = settlementRepository.findByPlanetId(planetB);

        assertEquals(1, settlementsA.size());
        assertEquals("empire-1", settlementsA.get(0).getOwnerEmpireId());
        assertEquals(1, settlementsB.size());
        assertEquals("empire-2", settlementsB.get(0).getOwnerEmpireId());

        // Clear planet A should not affect planet B
        persistenceService.clearPlanetData(planetA);
        assertEquals(0L, persistenceService.getPlanetSummary(planetA).get("settlements"));
        assertEquals(1L, persistenceService.getPlanetSummary(planetB).get("settlements"));
    }

    @Test
    void loadPlanetSurface_recreatesAllObjects() {
        Planet planet = new Planet(PLANET_ID);

        // Save multiple settlements
        for (int i = 0; i < 3; i++) {
            Hex3D hex = new Hex3D(i * 5, -(i * 5), 0);
            Settlement settlement = new Settlement("SETTLE-" + i, "Town" + i, hex, Settlement.Size.VILLAGE);
            settlement.setOwnerEmpireId("empire-1");
            persistenceService.saveSettlement(PLANET_ID, settlement);
        }

        // Save multiple industries
        for (int i = 0; i < 2; i++) {
            Hex3D hex = new Hex3D(10 + i, -(10 + i), 0);
            Industry industry = new Industry("IND-" + i, IndustryType.COAL_MINE, hex);
            industry.setOwnerEmpireId("empire-1");
            persistenceService.saveIndustry(PLANET_ID, industry);
        }

        // Save a terrain cell so hasPlanetData returns true
        Biome biome = new Biome(TerrainType.GRASSLAND, 15.0, 0.5, 100);
        TerrainData terrain = new TerrainData(biome);
        persistenceService.saveTerrainCell(PLANET_ID, new Hex3D(0, 0, 0), terrain);

        // Load into builder
        Planet loadedPlanet = new Planet(PLANET_ID);
        boolean loaded = persistenceService.loadPlanetSurface(loadedPlanet, builder);
        assertTrue(loaded, "Should load data for planet with saved objects");

        // Verify settlements were recreated
        List<Settlement> loadedSettlements = builder.getAllSettlements(loadedPlanet);
        assertEquals(3, loadedSettlements.size());
        for (Settlement s : loadedSettlements) {
            assertEquals("empire-1", s.getOwnerEmpireId());
            assertTrue(s.getName().startsWith("Town"));
        }

        // Verify industries were recreated
        List<Industry> loadedIndustries = builder.getAllIndustries(loadedPlanet);
        assertEquals(2, loadedIndustries.size());
        for (Industry ind : loadedIndustries) {
            assertEquals(IndustryType.COAL_MINE, ind.getType());
        }
    }

    @Test
    void clearPlanetData_removesAllTypes() {
        // Save all types of objects
        Hex3D hex = new Hex3D(1, 0, -1);
        Biome biome = new Biome(TerrainType.GRASSLAND, 15.0, 0.5, 100);
        TerrainData terrain = new TerrainData(biome);
        persistenceService.saveTerrainCell(PLANET_ID, hex, terrain);

        Settlement settlement = new Settlement("SETTLE-1", "City", hex, Settlement.Size.VILLAGE);
        settlement.setOwnerEmpireId("empire-1");
        persistenceService.saveSettlement(PLANET_ID, settlement);

        Industry industry = new Industry("IND-1", IndustryType.FARM, hex);
        industry.setOwnerEmpireId("empire-1");
        persistenceService.saveIndustry(PLANET_ID, industry);

        Station station = new Station("STAT-1", "Station", hex);
        station.setOwnerEmpireId("empire-1");
        persistenceService.saveStation(PLANET_ID, station);

        SurfaceVehicle vehicle = new SurfaceVehicle("VEH-1", VehicleType.BUS, hex);
        vehicle.setOwnerEmpireId("empire-1");
        persistenceService.saveVehicle(PLANET_ID, vehicle);

        // Verify all present
        Map<String, Long> before = persistenceService.getPlanetSummary(PLANET_ID);
        assertTrue(before.get("terrainCells") > 0);
        assertTrue(before.get("settlements") > 0);
        assertTrue(before.get("industries") > 0);
        assertTrue(before.get("stations") > 0);
        assertTrue(before.get("vehicles") > 0);

        // Clear
        persistenceService.clearPlanetData(PLANET_ID);

        // Verify all gone
        Map<String, Long> after = persistenceService.getPlanetSummary(PLANET_ID);
        assertEquals(0L, after.get("terrainCells"));
        assertEquals(0L, after.get("settlements"));
        assertEquals(0L, after.get("industries"));
        assertEquals(0L, after.get("stations"));
        assertEquals(0L, after.get("vehicles"));
        assertFalse(persistenceService.hasPlanetData(PLANET_ID));
    }

    @Test
    void saveSettlement_twice_updatesRecord() {
        Hex3D hex = new Hex3D(1, 0, -1);
        Settlement settlement = new Settlement("SETTLE-UPD", "OldName", hex, Settlement.Size.VILLAGE);
        settlement.setOwnerEmpireId("empire-1");
        persistenceService.saveSettlement(PLANET_ID, settlement);

        // Create a new settlement with same ID but different size
        Settlement updated = new Settlement("SETTLE-UPD", "OldName", hex, Settlement.Size.CITY);
        updated.setOwnerEmpireId("empire-1");
        persistenceService.saveSettlement(PLANET_ID, updated);

        // Verify updated - still one record
        List<PlanetSettlementEntity> saved = settlementRepository.findByPlanetId(PLANET_ID);
        assertEquals(1, saved.size());
        assertEquals("CITY", saved.get(0).getSize());
    }

    @Test
    void loadTerrainCells_restoresTerrainDataInBuilder() {
        // Save multiple terrain cells with different biomes
        Biome grassland = new Biome(TerrainType.GRASSLAND, 15.0, 0.5, 100);
        TerrainData terrain1 = new TerrainData(grassland);
        terrain1.setInfrastructure(TerrainData.InfrastructureType.ROAD);
        persistenceService.saveTerrainCell(PLANET_ID, new Hex3D(1, 0, -1), terrain1);

        Biome desert = new Biome(TerrainType.DESERT, 35.0, 0.1, 50);
        TerrainData terrain2 = new TerrainData(desert);
        persistenceService.saveTerrainCell(PLANET_ID, new Hex3D(2, -1, -1), terrain2);

        Biome ocean = new Biome(TerrainType.DEEP_OCEAN, 20.0, 0.8, 0);
        TerrainData terrain3 = new TerrainData(ocean);
        terrain3.setWaterLevel(5);
        persistenceService.saveTerrainCell(PLANET_ID, new Hex3D(0, 1, -1), terrain3);

        // Load into builder
        Planet planet = new Planet(PLANET_ID);
        boolean loaded = persistenceService.loadPlanetSurface(planet, builder);
        assertTrue(loaded, "Should load data for planet with saved terrain");

        // Verify terrain cells were restored in the builder
        TerrainData loaded1 = builder.getTerrainData(new Hex3D(1, 0, -1));
        assertNotNull(loaded1, "Grassland terrain should be restored");
        assertEquals(TerrainType.GRASSLAND, loaded1.getTerrainType());
        assertEquals(15.0, loaded1.getBiome().getTemperature(), 0.01);
        assertEquals(0.5, loaded1.getBiome().getMoisture(), 0.01);
        assertEquals(100, loaded1.getBiome().getAltitude());
        assertEquals(TerrainData.InfrastructureType.ROAD, loaded1.getInfrastructure());

        TerrainData loaded2 = builder.getTerrainData(new Hex3D(2, -1, -1));
        assertNotNull(loaded2, "Desert terrain should be restored");
        assertEquals(TerrainType.DESERT, loaded2.getTerrainType());
        assertEquals(35.0, loaded2.getBiome().getTemperature(), 0.01);

        TerrainData loaded3 = builder.getTerrainData(new Hex3D(0, 1, -1));
        assertNotNull(loaded3, "Ocean terrain should be restored");
        assertEquals(TerrainType.DEEP_OCEAN, loaded3.getTerrainType());
        assertEquals(5, loaded3.getWaterLevel());
    }

    @Test
    void loadVehicles_restoresRouteAndStats() {
        Hex3D hex = new Hex3D(1, 0, -1);

        // Create a vehicle with a route
        SurfaceVehicle vehicle = new SurfaceVehicle("VEH-ROUTE", VehicleType.BUS, hex);
        vehicle.setOwnerEmpireId("empire-1");
        vehicle.setRoute(List.of("STAT-1", "STAT-2", "STAT-3"));
        vehicle.boardPassengers(10);
        vehicle.loadCargo(CargoType.MAIL, 5);

        // Simulate some travel
        vehicle.setTotalDistance(100);
        vehicle.setTotalCargoDelivered(20);
        vehicle.setTotalPassengersDelivered(50);

        // Save the vehicle
        persistenceService.saveVehicle(PLANET_ID, vehicle);

        // Verify saved data
        List<PlanetVehicleEntity> saved = vehicleRepository.findByPlanetId(PLANET_ID);
        assertEquals(1, saved.size());
        assertNotNull(saved.get(0).getRouteStationIdsJson());
        assertEquals(10, saved.get(0).getPassengerCount());
        assertEquals(100, saved.get(0).getTotalDistance());

        // Load into a fresh builder
        Planet planet = new Planet(PLANET_ID);
        // Need terrain for hasPlanetData
        Biome biome = new Biome(TerrainType.GRASSLAND, 15.0, 0.5, 100);
        persistenceService.saveTerrainCell(PLANET_ID, hex, new TerrainData(biome));

        persistenceService.loadPlanetSurface(planet, builder);

        // Verify vehicle was restored with all data
        List<SurfaceVehicle> loadedVehicles = builder.getAllVehicles(planet);
        assertEquals(1, loadedVehicles.size());

        SurfaceVehicle loaded = loadedVehicles.get(0);
        assertEquals("VEH-ROUTE", loaded.getId());
        assertEquals(VehicleType.BUS, loaded.getType());
        assertEquals("empire-1", loaded.getOwnerEmpireId());
        assertEquals(100, loaded.getTotalDistance());
        assertEquals(20, loaded.getTotalCargoDelivered());
        assertEquals(50, loaded.getTotalPassengersDelivered());
        assertEquals(10, loaded.getPassengerCount());
        assertEquals(3, loaded.getRouteStationIds().size());
        assertEquals("STAT-1", loaded.getRouteStationIds().get(0));
        assertEquals("STAT-2", loaded.getRouteStationIds().get(1));
        assertEquals("STAT-3", loaded.getRouteStationIds().get(2));
    }

    @Test
    void loadStations_restoresInfrastructureType() {
        Hex3D hex = new Hex3D(3, -2, -1);

        Station station = new Station("STAT-LOAD", "Load Test Station", hex);
        station.setOwnerEmpireId("empire-1");
        station.setInfrastructureType(TerrainData.InfrastructureType.STATION);
        station.setConnectedSettlementId("SETTLE-CONN");
        station.setConnectedIndustryId("IND-CONN");

        persistenceService.saveStation(PLANET_ID, station);

        // Need terrain for hasPlanetData
        Biome biome = new Biome(TerrainType.GRASSLAND, 15.0, 0.5, 100);
        persistenceService.saveTerrainCell(PLANET_ID, hex, new TerrainData(biome));

        // Load
        Planet planet = new Planet(PLANET_ID);
        persistenceService.loadPlanetSurface(planet, builder);

        List<Station> loadedStations = builder.getAllStations(planet);
        assertEquals(1, loadedStations.size());

        Station loaded = loadedStations.get(0);
        assertEquals("STAT-LOAD", loaded.getId());
        assertEquals("Load Test Station", loaded.getName());
        assertEquals(TerrainData.InfrastructureType.STATION, loaded.getInfrastructureType());
        assertEquals("SETTLE-CONN", loaded.getConnectedSettlementId());
        assertEquals("IND-CONN", loaded.getConnectedIndustryId());
        assertEquals("empire-1", loaded.getOwnerEmpireId());
    }
}
