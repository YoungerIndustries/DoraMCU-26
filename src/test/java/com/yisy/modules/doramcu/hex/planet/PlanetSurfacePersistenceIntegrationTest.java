package com.yisy.modules.doramcu.hex.planet;

import com.yisy.modules.doramcu.hex.Hex3D;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for the planet surface persistence layer.
 * <p>
 * Tests the 5 JPA repositories and PlanetSurfacePersistenceService using
 * a minimal Spring context (PersistenceTestConfig) with an H2 in-memory database.
 * Each test is wrapped in a transaction that rolls back automatically,
 * so the database is clean for every test method.
 * </p>
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = PersistenceTestConfig.class)
@ActiveProfiles("test")
@Transactional
class PlanetSurfacePersistenceIntegrationTest {

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

    private static final int PLANET_ID = 999;

    @BeforeEach
    void setUp() {
        // Clear any leftover data from previous tests
        terrainRepository.deleteByPlanetId(PLANET_ID);
        settlementRepository.deleteByPlanetId(PLANET_ID);
        industryRepository.deleteByPlanetId(PLANET_ID);
        stationRepository.deleteByPlanetId(PLANET_ID);
        vehicleRepository.deleteByPlanetId(PLANET_ID);
    }

    // =====================================================================
    // PlanetSurfacePersistenceService tests
    // =====================================================================

    @Test
    void hasPlanetData_emptyDatabase_returnsFalse() {
        assertFalse(persistenceService.hasPlanetData(PLANET_ID));
    }

    @Test
    void hasPlanetData_withTerrainCells_returnsTrue() {
        PlanetTerrainCell cell = new PlanetTerrainCell();
        cell.setPlanetId(PLANET_ID);
        cell.setHexX(1);
        cell.setHexY(0);
        cell.setHexZ(-1);
        cell.setTerrainType("GRASSLAND");
        terrainRepository.save(cell);

        assertTrue(persistenceService.hasPlanetData(PLANET_ID));
    }

    @Test
    void getPlanetSummary_empty_returnsZeroCounts() {
        Map<String, Long> summary = persistenceService.getPlanetSummary(PLANET_ID);

        assertEquals(0L, summary.get("terrainCells"));
        assertEquals(0L, summary.get("settlements"));
        assertEquals(0L, summary.get("industries"));
        assertEquals(0L, summary.get("stations"));
        assertEquals(0L, summary.get("vehicles"));
    }

    @Test
    void getPlanetSummary_withData_returnsCorrectCounts() {
        // Save 3 terrain cells
        for (int i = 0; i < 3; i++) {
            PlanetTerrainCell cell = new PlanetTerrainCell();
            cell.setPlanetId(PLANET_ID);
            cell.setHexX(i);
            cell.setHexY(-i);
            cell.setHexZ(0);
            cell.setTerrainType("DESERT");
            terrainRepository.save(cell);
        }

        // Save 1 settlement
        PlanetSettlementEntity settlement = new PlanetSettlementEntity();
        settlement.setSettlementId("SETTLE-1");
        settlement.setPlanetId(PLANET_ID);
        settlement.setName("TestTown");
        settlement.setCenterHexX(0);
        settlement.setCenterHexY(0);
        settlement.setCenterHexZ(0);
        settlement.setSize("VILLAGE");
        settlement.setPopulation(100);
        settlement.setMaxPopulation(500);
        settlement.setGrowthRate(10);
        settlementRepository.save(settlement);

        Map<String, Long> summary = persistenceService.getPlanetSummary(PLANET_ID);

        assertEquals(3L, summary.get("terrainCells"));
        assertEquals(1L, summary.get("settlements"));
        assertEquals(0L, summary.get("industries"));
        assertEquals(0L, summary.get("stations"));
        assertEquals(0L, summary.get("vehicles"));
    }

    @Test
    void clearPlanetData_removesAllData() {
        // Save some data
        PlanetTerrainCell cell = new PlanetTerrainCell();
        cell.setPlanetId(PLANET_ID);
        cell.setHexX(1);
        cell.setHexY(-1);
        cell.setHexZ(0);
        cell.setTerrainType("GRASSLAND");
        terrainRepository.save(cell);

        PlanetSettlementEntity settlement = new PlanetSettlementEntity();
        settlement.setSettlementId("SETTLE-1");
        settlement.setPlanetId(PLANET_ID);
        settlement.setName("Town");
        settlement.setCenterHexX(0);
        settlement.setCenterHexY(0);
        settlement.setCenterHexZ(0);
        settlement.setSize("TOWN");
        settlement.setPopulation(500);
        settlement.setMaxPopulation(2000);
        settlement.setGrowthRate(10);
        settlementRepository.save(settlement);

        assertTrue(persistenceService.hasPlanetData(PLANET_ID));

        // Clear all data
        persistenceService.clearPlanetData(PLANET_ID);

        assertFalse(persistenceService.hasPlanetData(PLANET_ID));
        assertEquals(0, settlementRepository.countByPlanetId(PLANET_ID));
        assertEquals(0, industryRepository.countByPlanetId(PLANET_ID));
        assertEquals(0, stationRepository.countByPlanetId(PLANET_ID));
        assertEquals(0, vehicleRepository.countByPlanetId(PLANET_ID));
    }

    // =====================================================================
    // Terrain Cell Repository tests
    // =====================================================================

    @Test
    void terrainRepository_saveAndFindByPlanetId() {
        PlanetTerrainCell cell = new PlanetTerrainCell();
        cell.setPlanetId(PLANET_ID);
        cell.setHexX(2);
        cell.setHexY(-1);
        cell.setHexZ(-1);
        cell.setTerrainType("DESERT");
        cell.setBiomeName("Hot Desert");
        cell.setTemperature(40.0);
        cell.setMoisture(0.1);
        cell.setAltitude(100);
        cell.setInfrastructure("NONE");
        terrainRepository.save(cell);

        List<PlanetTerrainCell> cells = terrainRepository.findByPlanetId(PLANET_ID);
        assertEquals(1, cells.size());

        PlanetTerrainCell found = cells.get(0);
        assertEquals(PLANET_ID, found.getPlanetId());
        assertEquals(2, found.getHexX());
        assertEquals(-1, found.getHexY());
        assertEquals(-1, found.getHexZ());
        assertEquals("DESERT", found.getTerrainType());
        assertEquals("Hot Desert", found.getBiomeName());
        assertEquals(40.0, found.getTemperature(), 0.001);
        assertEquals(0.1, found.getMoisture(), 0.001);
        assertEquals(100, found.getAltitude());
        assertEquals("NONE", found.getInfrastructure());
    }

    @Test
    void terrainRepository_findByPlanetIdAndHexXAndHexYAndHexZ() {
        PlanetTerrainCell cell = new PlanetTerrainCell();
        cell.setPlanetId(PLANET_ID);
        cell.setHexX(5);
        cell.setHexY(-3);
        cell.setHexZ(-2);
        cell.setTerrainType("OCEAN");
        terrainRepository.save(cell);

        PlanetTerrainCell found = terrainRepository
                .findByPlanetIdAndHexXAndHexYAndHexZ(PLANET_ID, 5, -3, -2);
        assertNotNull(found);
        assertEquals("OCEAN", found.getTerrainType());
    }

    @Test
    void terrainRepository_findByHex_returnsNullWhenNotFound() {
        PlanetTerrainCell found = terrainRepository
                .findByPlanetIdAndHexXAndHexYAndHexZ(PLANET_ID, 99, -50, -49);
        assertNull(found);
    }

    @Test
    void terrainRepository_findByInfrastructure() {
        PlanetTerrainCell road = new PlanetTerrainCell();
        road.setPlanetId(PLANET_ID);
        road.setHexX(1);
        road.setHexY(0);
        road.setHexZ(-1);
        road.setTerrainType("GRASSLAND");
        road.setInfrastructure("ROAD");
        terrainRepository.save(road);

        PlanetTerrainCell rail = new PlanetTerrainCell();
        rail.setPlanetId(PLANET_ID);
        rail.setHexX(2);
        rail.setHexY(-1);
        rail.setHexZ(-1);
        rail.setTerrainType("GRASSLAND");
        rail.setInfrastructure("RAIL");
        terrainRepository.save(rail);

        List<PlanetTerrainCell> roads = terrainRepository
                .findByPlanetIdAndInfrastructure(PLANET_ID, "ROAD");
        assertEquals(1, roads.size());
        assertEquals("ROAD", roads.get(0).getInfrastructure());
    }

    @Test
    void terrainRepository_countByPlanetId() {
        assertEquals(0, terrainRepository.countByPlanetId(PLANET_ID));

        for (int i = 0; i < 5; i++) {
            PlanetTerrainCell cell = new PlanetTerrainCell();
            cell.setPlanetId(PLANET_ID);
            cell.setHexX(i);
            cell.setHexY(-i);
            cell.setHexZ(0);
            cell.setTerrainType("GRASSLAND");
            terrainRepository.save(cell);
        }

        assertEquals(5, terrainRepository.countByPlanetId(PLANET_ID));
    }

    @Test
    void terrainRepository_updateExistingCell() {
        PlanetTerrainCell cell = new PlanetTerrainCell();
        cell.setPlanetId(PLANET_ID);
        cell.setHexX(1);
        cell.setHexY(0);
        cell.setHexZ(-1);
        cell.setTerrainType("DESERT");
        cell.setInfrastructure("NONE");
        terrainRepository.save(cell);
        Long id = cell.getId();

        // Update the cell
        PlanetTerrainCell updated = terrainRepository.findById(id).orElseThrow();
        updated.setInfrastructure("ROAD");
        updated.setBiomeName("Temperate Grassland");
        terrainRepository.save(updated);

        PlanetTerrainCell found = terrainRepository.findById(id).orElseThrow();
        assertEquals("ROAD", found.getInfrastructure());
        assertEquals("Temperate Grassland", found.getBiomeName());
    }

    // =====================================================================
    // Settlement Repository tests
    // =====================================================================

    @Test
    void settlementRepository_saveAndFindBySettlementId() {
        PlanetSettlementEntity entity = new PlanetSettlementEntity();
        entity.setSettlementId("SETTLE-42");
        entity.setPlanetId(PLANET_ID);
        entity.setName("MegaCity");
        entity.setCenterHexX(100);
        entity.setCenterHexY(-50);
        entity.setCenterHexZ(-50);
        entity.setSize("METROPOLIS");
        entity.setPopulation(1000000);
        entity.setMaxPopulation(2000000);
        entity.setGrowthRate(5);
        entity.setCargoGoalsJson("{\"coal\":10}");
        entity.setMailProduction(50);
        entity.setPassengerProduction(200);
        entity.setActive(true);
        entity.setOwnerEmpireId("empire-1");
        settlementRepository.save(entity);

        Optional<PlanetSettlementEntity> found = settlementRepository
                .findBySettlementId("SETTLE-42");
        assertTrue(found.isPresent());
        assertEquals("MegaCity", found.get().getName());
        assertEquals("METROPOLIS", found.get().getSize());
        assertEquals(1000000, found.get().getPopulation());
        assertEquals("{\"coal\":10}", found.get().getCargoGoalsJson());
    }

    @Test
    void settlementRepository_findByPlanetId() {
        for (int i = 0; i < 3; i++) {
            PlanetSettlementEntity entity = new PlanetSettlementEntity();
            entity.setSettlementId("SETTLE-" + i);
            entity.setPlanetId(PLANET_ID);
            entity.setName("Town" + i);
            entity.setCenterHexX(i * 10);
            entity.setCenterHexY(-i * 5);
            entity.setCenterHexZ(-i * 5);
            entity.setSize("TOWN");
            entity.setPopulation(1000 + i * 100);
            entity.setMaxPopulation(5000);
            entity.setGrowthRate(10);
            settlementRepository.save(entity);
        }

        List<PlanetSettlementEntity> settlements = settlementRepository
                .findByPlanetId(PLANET_ID);
        assertEquals(3, settlements.size());
    }

    @Test
    void settlementRepository_findByPlanetIdAndOwnerEmpireId() {
        PlanetSettlementEntity emp1 = new PlanetSettlementEntity();
        emp1.setSettlementId("SETTLE-1");
        emp1.setPlanetId(PLANET_ID);
        emp1.setName("Empire1 Town");
        emp1.setCenterHexX(10);
        emp1.setCenterHexY(-5);
        emp1.setCenterHexZ(-5);
        emp1.setSize("TOWN");
        emp1.setPopulation(1000);
        emp1.setMaxPopulation(5000);
        emp1.setGrowthRate(10);
        emp1.setOwnerEmpireId("empire-1");
        settlementRepository.save(emp1);

        PlanetSettlementEntity emp2 = new PlanetSettlementEntity();
        emp2.setSettlementId("SETTLE-2");
        emp2.setPlanetId(PLANET_ID);
        emp2.setName("Empire2 Town");
        emp2.setCenterHexX(20);
        emp2.setCenterHexY(-10);
        emp2.setCenterHexZ(-10);
        emp2.setSize("CITY");
        emp2.setPopulation(5000);
        emp2.setMaxPopulation(20000);
        emp2.setGrowthRate(8);
        emp2.setOwnerEmpireId("empire-2");
        settlementRepository.save(emp2);

        List<PlanetSettlementEntity> emp1Settlements = settlementRepository
                .findByPlanetIdAndOwnerEmpireId(PLANET_ID, "empire-1");
        assertEquals(1, emp1Settlements.size());
        assertEquals("Empire1 Town", emp1Settlements.get(0).getName());
    }

    @Test
    void settlementRepository_deleteBySettlementId() {
        PlanetSettlementEntity entity = new PlanetSettlementEntity();
        entity.setSettlementId("SETTLE-DEL");
        entity.setPlanetId(PLANET_ID);
        entity.setName("ToDelete");
        entity.setCenterHexX(0);
        entity.setCenterHexY(0);
        entity.setCenterHexZ(0);
        entity.setSize("VILLAGE");
        entity.setPopulation(100);
        entity.setMaxPopulation(500);
        entity.setGrowthRate(10);
        settlementRepository.save(entity);

        settlementRepository.deleteBySettlementId("SETTLE-DEL");
        assertFalse(settlementRepository.findBySettlementId("SETTLE-DEL").isPresent());
    }

    @Test
    void settlementRepository_countByPlanetId() {
        assertEquals(0, settlementRepository.countByPlanetId(PLANET_ID));

        for (int i = 0; i < 4; i++) {
            PlanetSettlementEntity entity = new PlanetSettlementEntity();
            entity.setSettlementId("S-" + i);
            entity.setPlanetId(PLANET_ID);
            entity.setName("Town" + i);
            entity.setCenterHexX(i);
            entity.setCenterHexY(-i);
            entity.setCenterHexZ(0);
            entity.setSize("VILLAGE");
            entity.setPopulation(100);
            entity.setMaxPopulation(500);
            entity.setGrowthRate(10);
            settlementRepository.save(entity);
        }

        assertEquals(4, settlementRepository.countByPlanetId(PLANET_ID));
    }

    // =====================================================================
    // Industry Repository tests
    // =====================================================================

    @Test
    void industryRepository_saveAndFindByIndustryId() {
        PlanetIndustryEntity entity = new PlanetIndustryEntity();
        entity.setIndustryId("IND-99");
        entity.setPlanetId(PLANET_ID);
        entity.setIndustryType("COAL_MINE");
        entity.setHexX(50);
        entity.setHexY(-25);
        entity.setHexZ(-25);
        entity.setProductionLevel(0.75);
        entity.setTurnsSinceBuilt(10);
        entity.setActive(true);
        entity.setOwnerEmpireId("empire-2");
        entity.setInputInventoryJson("{\"ore\":5}");
        entity.setOutputInventoryJson("{\"coal\":10}");
        industryRepository.save(entity);

        Optional<PlanetIndustryEntity> found = industryRepository
                .findByIndustryId("IND-99");
        assertTrue(found.isPresent());
        assertEquals("COAL_MINE", found.get().getIndustryType());
        assertEquals(0.75, found.get().getProductionLevel(), 0.001);
        assertEquals(10, found.get().getTurnsSinceBuilt());
    }

    @Test
    void industryRepository_findByPlanetIdAndIndustryType() {
        PlanetIndustryEntity coal = new PlanetIndustryEntity();
        coal.setIndustryId("IND-1");
        coal.setPlanetId(PLANET_ID);
        coal.setIndustryType("COAL_MINE");
        coal.setHexX(10);
        coal.setHexY(-5);
        coal.setHexZ(-5);
        industryRepository.save(coal);

        PlanetIndustryEntity iron = new PlanetIndustryEntity();
        iron.setIndustryId("IND-2");
        iron.setPlanetId(PLANET_ID);
        iron.setIndustryType("IRON_MINE");
        iron.setHexX(20);
        iron.setHexY(-10);
        iron.setHexZ(-10);
        industryRepository.save(iron);

        List<PlanetIndustryEntity> coalMines = industryRepository
                .findByPlanetIdAndIndustryType(PLANET_ID, "COAL_MINE");
        assertEquals(1, coalMines.size());
        assertEquals("COAL_MINE", coalMines.get(0).getIndustryType());
    }

    @Test
    void industryRepository_deleteByIndustryId() {
        PlanetIndustryEntity entity = new PlanetIndustryEntity();
        entity.setIndustryId("IND-DEL");
        entity.setPlanetId(PLANET_ID);
        entity.setIndustryType("FARM");
        entity.setHexX(0);
        entity.setHexY(0);
        entity.setHexZ(0);
        industryRepository.save(entity);

        industryRepository.deleteByIndustryId("IND-DEL");
        assertFalse(industryRepository.findByIndustryId("IND-DEL").isPresent());
    }

    // =====================================================================
    // Station Repository tests
    // =====================================================================

    @Test
    void stationRepository_saveAndFindByStationId() {
        PlanetStationEntity entity = new PlanetStationEntity();
        entity.setStationId("STAT-50");
        entity.setPlanetId(PLANET_ID);
        entity.setName("Central Hub");
        entity.setHexX(100);
        entity.setHexY(-50);
        entity.setHexZ(-50);
        entity.setInfrastructureType("AIRPORT");
        entity.setPassengersWaiting(30);
        entity.setTotalIncome(5000);
        entity.setConnectedSettlementId("SETTLE-1");
        entity.setConnectedIndustryId("IND-1");
        entity.setOwnerEmpireId("empire-1");
        entity.setCargoWaitingJson("{\"mail\":5}");
        stationRepository.save(entity);

        Optional<PlanetStationEntity> found = stationRepository
                .findByStationId("STAT-50");
        assertTrue(found.isPresent());
        assertEquals("Central Hub", found.get().getName());
        assertEquals("AIRPORT", found.get().getInfrastructureType());
        assertEquals(30, found.get().getPassengersWaiting());
        assertEquals(5000, found.get().getTotalIncome());
    }

    @Test
    void stationRepository_findByPlanetIdAndInfrastructureType() {
        PlanetStationEntity airport = new PlanetStationEntity();
        airport.setStationId("STAT-1");
        airport.setPlanetId(PLANET_ID);
        airport.setName("Airport");
        airport.setHexX(10);
        airport.setHexY(-5);
        airport.setHexZ(-5);
        airport.setInfrastructureType("AIRPORT");
        stationRepository.save(airport);

        PlanetStationEntity port = new PlanetStationEntity();
        port.setStationId("STAT-2");
        port.setPlanetId(PLANET_ID);
        port.setName("Port");
        port.setHexX(20);
        port.setHexY(-10);
        port.setHexZ(-10);
        port.setInfrastructureType("PORT");
        stationRepository.save(port);

        List<PlanetStationEntity> airports = stationRepository
                .findByPlanetIdAndInfrastructureType(PLANET_ID, "AIRPORT");
        assertEquals(1, airports.size());
        assertEquals("AIRPORT", airports.get(0).getInfrastructureType());
    }

    @Test
    void stationRepository_deleteByStationId() {
        PlanetStationEntity entity = new PlanetStationEntity();
        entity.setStationId("STAT-DEL");
        entity.setPlanetId(PLANET_ID);
        entity.setName("ToDelete");
        entity.setHexX(0);
        entity.setHexY(0);
        entity.setHexZ(0);
        entity.setInfrastructureType("STATION");
        stationRepository.save(entity);

        stationRepository.deleteByStationId("STAT-DEL");
        assertFalse(stationRepository.findByStationId("STAT-DEL").isPresent());
    }

    // =====================================================================
    // Vehicle Repository tests
    // =====================================================================

    @Test
    void vehicleRepository_saveAndFindByVehicleId() {
        PlanetVehicleEntity entity = new PlanetVehicleEntity();
        entity.setVehicleId("VEH-77");
        entity.setPlanetId(PLANET_ID);
        entity.setVehicleType("BUS");
        entity.setCurrentHexX(200);
        entity.setCurrentHexY(-100);
        entity.setCurrentHexZ(-100);
        entity.setRouteStationIdsJson("[\"STAT-1\",\"STAT-2\"]");
        entity.setCurrentRouteIndex(1);
        entity.setCargoInventoryJson("{\"mail\":3}");
        entity.setPassengerCount(25);
        entity.setTotalDistance(500);
        entity.setTotalCargoDelivered(100);
        entity.setTotalPassengersDelivered(200);
        entity.setActive(true);
        entity.setOwnerEmpireId("empire-3");
        vehicleRepository.save(entity);

        Optional<PlanetVehicleEntity> found = vehicleRepository
                .findByVehicleId("VEH-77");
        assertTrue(found.isPresent());
        assertEquals("BUS", found.get().getVehicleType());
        assertEquals(25, found.get().getPassengerCount());
        assertEquals(500, found.get().getTotalDistance());
        assertEquals("[\"STAT-1\",\"STAT-2\"]", found.get().getRouteStationIdsJson());
    }

    @Test
    void vehicleRepository_findByPlanetIdAndVehicleType() {
        PlanetVehicleEntity bus = new PlanetVehicleEntity();
        bus.setVehicleId("VEH-1");
        bus.setPlanetId(PLANET_ID);
        bus.setVehicleType("BUS");
        bus.setCurrentHexX(10);
        bus.setCurrentHexY(-5);
        bus.setCurrentHexZ(-5);
        vehicleRepository.save(bus);

        PlanetVehicleEntity truck = new PlanetVehicleEntity();
        truck.setVehicleId("VEH-2");
        truck.setPlanetId(PLANET_ID);
        truck.setVehicleType("TRUCK");
        truck.setCurrentHexX(20);
        truck.setCurrentHexY(-10);
        truck.setCurrentHexZ(-10);
        vehicleRepository.save(truck);

        List<PlanetVehicleEntity> buses = vehicleRepository
                .findByPlanetIdAndVehicleType(PLANET_ID, "BUS");
        assertEquals(1, buses.size());
        assertEquals("BUS", buses.get(0).getVehicleType());
    }

    @Test
    void vehicleRepository_deleteByVehicleId() {
        PlanetVehicleEntity entity = new PlanetVehicleEntity();
        entity.setVehicleId("VEH-DEL");
        entity.setPlanetId(PLANET_ID);
        entity.setVehicleType("TRUCK");
        entity.setCurrentHexX(0);
        entity.setCurrentHexY(0);
        entity.setCurrentHexZ(0);
        vehicleRepository.save(entity);

        vehicleRepository.deleteByVehicleId("VEH-DEL");
        assertFalse(vehicleRepository.findByVehicleId("VEH-DEL").isPresent());
    }

    // =====================================================================
    // Cross-repository and service integration tests
    // =====================================================================

    @Test
    void saveTerrainCell_viaService_createsEntity() {
        Biome biome = new Biome(TerrainType.GRASSLAND, 20.0, 0.5, 100);
        TerrainData terrain = new TerrainData(biome);
        terrain.setInfrastructure(TerrainData.InfrastructureType.ROAD);
        terrain.setWaterLevel(0);
        Hex3D hex = new Hex3D(3, -1, -2);

        persistenceService.saveTerrainCell(PLANET_ID, hex, terrain);

        PlanetTerrainCell found = terrainRepository
                .findByPlanetIdAndHexXAndHexYAndHexZ(PLANET_ID, 3, -1, -2);
        assertNotNull(found);
        assertEquals("GRASSLAND", found.getTerrainType());
        assertEquals("ROAD", found.getInfrastructure());
    }

    @Test
    void saveTerrainCell_viaService_updatesExistingEntity() {
        Hex3D hex = new Hex3D(3, -1, -2);

        // First save with desert
        Biome desertBiome = new Biome(TerrainType.DESERT, 40.0, 0.1, 50);
        TerrainData terrain1 = new TerrainData(desertBiome);
        terrain1.setInfrastructure(TerrainData.InfrastructureType.NONE);
        terrain1.setWaterLevel(0);
        persistenceService.saveTerrainCell(PLANET_ID, hex, terrain1);

        // Update with grassland
        Biome grassBiome = new Biome(TerrainType.GRASSLAND, 20.0, 0.5, 100);
        TerrainData terrain2 = new TerrainData(grassBiome);
        terrain2.setInfrastructure(TerrainData.InfrastructureType.RAIL);
        terrain2.setWaterLevel(0);
        persistenceService.saveTerrainCell(PLANET_ID, hex, terrain2);

        PlanetTerrainCell found = terrainRepository
                .findByPlanetIdAndHexXAndHexYAndHexZ(PLANET_ID, 3, -1, -2);
        assertEquals("GRASSLAND", found.getTerrainType());
        assertEquals("RAIL", found.getInfrastructure());
    }

    @Test
    void saveSettlement_viaService_createsEntity() {
        Hex3D center = new Hex3D(10, -5, -5);
        Settlement settlement = new Settlement("SETTLE-SVC", "ServiceTown", center, Settlement.Size.TOWN);
        settlement.setActive(true);
        settlement.setOwnerEmpireId("empire-svc");

        persistenceService.saveSettlement(PLANET_ID, settlement);

        Optional<PlanetSettlementEntity> found = settlementRepository
                .findBySettlementId("SETTLE-SVC");
        assertTrue(found.isPresent());
        assertEquals("ServiceTown", found.get().getName());
        assertEquals("TOWN", found.get().getSize());
    }

    @Test
    void saveIndustry_viaService_createsEntity() {
        Hex3D pos = new Hex3D(50, -25, -25);
        Industry industry = new Industry("IND-SVC", IndustryType.COAL_MINE, pos);
        industry.setActive(true);
        industry.setOwnerEmpireId("empire-svc");

        persistenceService.saveIndustry(PLANET_ID, industry);

        Optional<PlanetIndustryEntity> found = industryRepository
                .findByIndustryId("IND-SVC");
        assertTrue(found.isPresent());
        assertEquals("COAL_MINE", found.get().getIndustryType());
    }

    @Test
    void saveStation_viaService_createsEntity() {
        Hex3D pos = new Hex3D(100, -50, -50);
        Station station = new Station("STAT-SVC", "ServiceStation", pos);
        station.setInfrastructureType(TerrainData.InfrastructureType.AIRPORT);
        station.setConnectedSettlementId("SETTLE-1");
        station.setConnectedIndustryId("IND-1");
        station.setOwnerEmpireId("empire-svc");

        persistenceService.saveStation(PLANET_ID, station);

        Optional<PlanetStationEntity> found = stationRepository
                .findByStationId("STAT-SVC");
        assertTrue(found.isPresent());
        assertEquals("ServiceStation", found.get().getName());
        assertEquals("AIRPORT", found.get().getInfrastructureType());
        assertEquals("SETTLE-1", found.get().getConnectedSettlementId());
    }

    @Test
    void saveVehicle_viaService_createsEntity() {
        Hex3D pos = new Hex3D(200, -100, -100);
        SurfaceVehicle vehicle = new SurfaceVehicle("VEH-SVC", VehicleType.BUS, pos);
        vehicle.setActive(true);
        vehicle.setOwnerEmpireId("empire-svc");

        persistenceService.saveVehicle(PLANET_ID, vehicle);

        Optional<PlanetVehicleEntity> found = vehicleRepository
                .findByVehicleId("VEH-SVC");
        assertTrue(found.isPresent());
        assertEquals("BUS", found.get().getVehicleType());
    }

    @Test
    void multiplePlanets_dataIsIsolated() {
        // Save data for planet 999
        PlanetTerrainCell cell1 = new PlanetTerrainCell();
        cell1.setPlanetId(PLANET_ID);
        cell1.setHexX(1);
        cell1.setHexY(0);
        cell1.setHexZ(-1);
        cell1.setTerrainType("DESERT");
        terrainRepository.save(cell1);

        // Save data for planet 888
        PlanetTerrainCell cell2 = new PlanetTerrainCell();
        cell2.setPlanetId(888);
        cell2.setHexX(1);
        cell2.setHexY(0);
        cell2.setHexZ(-1);
        cell2.setTerrainType("OCEAN");
        terrainRepository.save(cell2);

        // Verify isolation
        List<PlanetTerrainCell> planet999 = terrainRepository.findByPlanetId(PLANET_ID);
        List<PlanetTerrainCell> planet888 = terrainRepository.findByPlanetId(888);

        assertEquals(1, planet999.size());
        assertEquals("DESERT", planet999.get(0).getTerrainType());

        assertEquals(1, planet888.size());
        assertEquals("OCEAN", planet888.get(0).getTerrainType());
    }

    @Test
    void terrainCell_settlementAndIndustryReferences() {
        PlanetTerrainCell cell = new PlanetTerrainCell();
        cell.setPlanetId(PLANET_ID);
        cell.setHexX(5);
        cell.setHexY(-3);
        cell.setHexZ(-2);
        cell.setTerrainType("GRASSLAND");
        cell.setSettlementId("SETTLE-REF");
        cell.setIndustryId("IND-REF");
        terrainRepository.save(cell);

        List<PlanetTerrainCell> bySettlement = terrainRepository
                .findByPlanetIdAndSettlementId(PLANET_ID, "SETTLE-REF");
        assertEquals(1, bySettlement.size());

        List<PlanetTerrainCell> byIndustry = terrainRepository
                .findByPlanetIdAndIndustryId(PLANET_ID, "IND-REF");
        assertEquals(1, byIndustry.size());
    }

    @Test
    void settlement_byOwnerEmpire_returnsEmptyWhenNoOwner() {
        PlanetSettlementEntity entity = new PlanetSettlementEntity();
        entity.setSettlementId("SETTLE-NO-OWNER");
        entity.setPlanetId(PLANET_ID);
        entity.setName("WildTown");
        entity.setCenterHexX(0);
        entity.setCenterHexY(0);
        entity.setCenterHexZ(0);
        entity.setSize("VILLAGE");
        entity.setPopulation(100);
        entity.setMaxPopulation(500);
        entity.setGrowthRate(10);
        // No ownerEmpireId set
        settlementRepository.save(entity);

        List<PlanetSettlementEntity> owned = settlementRepository
                .findByPlanetIdAndOwnerEmpireId(PLANET_ID, "empire-none");
        assertTrue(owned.isEmpty());
    }

    @Test
    void industry_byOwnerEmpire() {
        PlanetIndustryEntity entity = new PlanetIndustryEntity();
        entity.setIndustryId("IND-OWNER");
        entity.setPlanetId(PLANET_ID);
        entity.setIndustryType("FARM");
        entity.setHexX(0);
        entity.setHexY(0);
        entity.setHexZ(0);
        entity.setOwnerEmpireId("empire-farm");
        industryRepository.save(entity);

        List<PlanetIndustryEntity> owned = industryRepository
                .findByPlanetIdAndOwnerEmpireId(PLANET_ID, "empire-farm");
        assertEquals(1, owned.size());
        assertEquals("FARM", owned.get(0).getIndustryType());
    }

    @Test
    void station_byOwnerEmpire() {
        PlanetStationEntity entity = new PlanetStationEntity();
        entity.setStationId("STAT-OWNER");
        entity.setPlanetId(PLANET_ID);
        entity.setName("OwnedStation");
        entity.setHexX(0);
        entity.setHexY(0);
        entity.setHexZ(0);
        entity.setInfrastructureType("STATION");
        entity.setOwnerEmpireId("empire-station");
        stationRepository.save(entity);

        List<PlanetStationEntity> owned = stationRepository
                .findByPlanetIdAndOwnerEmpireId(PLANET_ID, "empire-station");
        assertEquals(1, owned.size());
    }

    @Test
    void vehicle_byOwnerEmpire() {
        PlanetVehicleEntity entity = new PlanetVehicleEntity();
        entity.setVehicleId("VEH-OWNER");
        entity.setPlanetId(PLANET_ID);
        entity.setVehicleType("TRUCK");
        entity.setCurrentHexX(0);
        entity.setCurrentHexY(0);
        entity.setCurrentHexZ(0);
        entity.setOwnerEmpireId("empire-truck");
        vehicleRepository.save(entity);

        List<PlanetVehicleEntity> owned = vehicleRepository
                .findByPlanetIdAndOwnerEmpireId(PLANET_ID, "empire-truck");
        assertEquals(1, owned.size());
    }
}
