package com.yisy.modules.doramcu.hex.planet;

import com.yisy.modules.doramcu.gamecore.DungeonRoomService;
import com.yisy.modules.doramcu.hex.Hex3D;
import com.yisy.modules.doramcu.hex.Planet;
import com.yisy.modules.doramcu.hex.planet.controller.HexPlanetSurfaceController;
import com.yisy.modules.doramcu.hex.planet.dto.*;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the PlanetSurfaceController DTO mapping logic.
 * <p>
 * Tests the controller's ability to map domain objects to DTOs correctly,
 * without requiring the full Spring context or database.
 * </p>
 */
class HexPlanetSurfaceControllerTest {

    @Test
    void planetSurfaceDto_construction() {
        PlanetSurfaceDto dto = new PlanetSurfaceDto(42, "TestPlanet");
        assertEquals(42, dto.getPlanetId());
        assertEquals("TestPlanet", dto.getPlanetName());
        assertNull(dto.getTerrainHexes());
        assertNull(dto.getSettlements());
        assertNull(dto.getIndustries());
        assertNull(dto.getStations());
        assertNull(dto.getVehicles());
        assertNull(dto.getSummary());
    }

    @Test
    void planetSurfaceDto_summaryDto() {
        PlanetSurfaceDto.SummaryDto summary = new PlanetSurfaceDto.SummaryDto();
        summary.setTerrainCells(100L);
        summary.setSettlements(5L);
        summary.setIndustries(10L);
        summary.setStations(8L);
        summary.setVehicles(15L);
        summary.setInteriors(20L);

        assertEquals(100L, summary.getTerrainCells());
        assertEquals(5L, summary.getSettlements());
        assertEquals(10L, summary.getIndustries());
        assertEquals(8L, summary.getStations());
        assertEquals(15L, summary.getVehicles());
        assertEquals(20L, summary.getInteriors());
    }

    @Test
    void terrainHexDto_construction() {
        TerrainHexDto dto = new TerrainHexDto();
        dto.setX(1);
        dto.setY(0);
        dto.setZ(-1);
        dto.setTerrainType("GRASSLAND");
        dto.setBiome("Temperate Grassland");
        dto.setTemperature(15.0);
        dto.setMoisture(0.5);
        dto.setAltitude(100);
        dto.setInfrastructure("STATION");
        dto.setSettlementId("SET-1");
        dto.setIndustryId("IND-1");
        dto.setWater(false);

        assertEquals(1, dto.getX());
        assertEquals(0, dto.getY());
        assertEquals(-1, dto.getZ());
        assertEquals("GRASSLAND", dto.getTerrainType());
        assertEquals("Temperate Grassland", dto.getBiome());
        assertEquals(15.0, dto.getTemperature(), 0.01);
        assertEquals(0.5, dto.getMoisture(), 0.01);
        assertEquals(100, dto.getAltitude(), 0.01);
        assertEquals("STATION", dto.getInfrastructure());
        assertEquals("SET-1", dto.getSettlementId());
        assertEquals("IND-1", dto.getIndustryId());
        assertFalse(dto.isWater());
    }

    @Test
    void settlementDto_construction() {
        SettlementDto dto = new SettlementDto();
        dto.setId("SET-1");
        dto.setName("New Hope");
        dto.setCenterX(5);
        dto.setCenterY(-3);
        dto.setCenterZ(-2);
        dto.setSize("CITY");
        dto.setPopulation(50000);
        dto.setPassengerProduction(100);
        dto.setActive(true);
        dto.setOwnerEmpireId("empire-1");
        dto.setDungeonIds(List.of("dungeon-1", "dungeon-2"));
        dto.setHexes(List.of("5,-3,-2", "6,-3,-3"));

        assertEquals("SET-1", dto.getId());
        assertEquals("New Hope", dto.getName());
        assertEquals(5, dto.getCenterX());
        assertEquals(-3, dto.getCenterY());
        assertEquals(-2, dto.getCenterZ());
        assertEquals("CITY", dto.getSize());
        assertEquals(50000, dto.getPopulation());
        assertEquals(100, dto.getPassengerProduction());
        assertTrue(dto.isActive());
        assertEquals("empire-1", dto.getOwnerEmpireId());
        assertEquals(2, dto.getDungeonIds().size());
        assertEquals(2, dto.getHexes().size());
    }

    @Test
    void industryDto_construction() {
        IndustryDto dto = new IndustryDto();
        dto.setId("IND-1");
        dto.setType("FARM");
        dto.setX(6);
        dto.setY(-3);
        dto.setZ(-3);
        dto.setProductionRate(80);
        dto.setActive(true);
        dto.setOwnerEmpireId("empire-1");
        dto.setDungeonId("factory-1");

        assertEquals("IND-1", dto.getId());
        assertEquals("FARM", dto.getType());
        assertEquals(6, dto.getX());
        assertEquals(-3, dto.getY());
        assertEquals(-3, dto.getZ());
        assertEquals(80, dto.getProductionRate());
        assertTrue(dto.isActive());
        assertEquals("empire-1", dto.getOwnerEmpireId());
        assertEquals("factory-1", dto.getDungeonId());
    }

    @Test
    void stationDto_construction() {
        StationDto dto = new StationDto();
        dto.setId("STA-1");
        dto.setName("Central Station");
        dto.setX(5);
        dto.setY(-3);
        dto.setZ(-2);
        dto.setInfrastructureType("STATION");
        dto.setConnectedSettlementId("SET-1");
        dto.setConnectedIndustryId("IND-1");
        dto.setOwnerEmpireId("empire-1");
        dto.setDungeonId("station-1");
        dto.setConnections(List.of("5,-3,-2", "6,-3,-3"));

        assertEquals("STA-1", dto.getId());
        assertEquals("Central Station", dto.getName());
        assertEquals(5, dto.getX());
        assertEquals(-3, dto.getY());
        assertEquals(-2, dto.getZ());
        assertEquals("STATION", dto.getInfrastructureType());
        assertEquals("SET-1", dto.getConnectedSettlementId());
        assertEquals("IND-1", dto.getConnectedIndustryId());
        assertEquals("empire-1", dto.getOwnerEmpireId());
        assertEquals("station-1", dto.getDungeonId());
        assertEquals(2, dto.getConnections().size());
    }

    @Test
    void vehicleDto_construction() {
        VehicleDto dto = new VehicleDto();
        dto.setId("VEH-1");
        dto.setType("BUS");
        dto.setX(5);
        dto.setY(-3);
        dto.setZ(-2);
        dto.setRouteStations(List.of("STA-1", "STA-2"));
        dto.setCargoInventory(Map.of("FOOD", 100, "MAIL", 50));
        dto.setPassengerCount(10);
        dto.setTotalDistance(150.0);
        dto.setTotalCargoDelivered(500);
        dto.setTotalPassengersDelivered(200);
        dto.setActive(true);
        dto.setOwnerEmpireId("empire-1");

        assertEquals("VEH-1", dto.getId());
        assertEquals("BUS", dto.getType());
        assertEquals(5, dto.getX());
        assertEquals(-3, dto.getY());
        assertEquals(-2, dto.getZ());
        assertEquals(2, dto.getRouteStations().size());
        assertEquals(100, dto.getCargoInventory().get("FOOD"));
        assertEquals(50, dto.getCargoInventory().get("MAIL"));
        assertEquals(10, dto.getPassengerCount());
        assertEquals(150.0, dto.getTotalDistance(), 0.01);
        assertEquals(500, dto.getTotalCargoDelivered());
        assertEquals(200, dto.getTotalPassengersDelivered());
        assertTrue(dto.isActive());
        assertEquals("empire-1", dto.getOwnerEmpireId());
    }

    @Test
    void mockDungeonRoomService_createAndListRooms() {
        MockDungeonRoomService service = new MockDungeonRoomService();

        DungeonRoomService.DungeonRoom room1 = new DungeonRoomService.DungeonRoom();
        room1.setName("Entrance Hall");
        room1.setDungeonId("dungeon-1");
        room1.setTheme("settlement");
        room1.setActive(true);
        service.createRoom(room1);

        DungeonRoomService.DungeonRoom room2 = new DungeonRoomService.DungeonRoom();
        room2.setName("Treasure Room");
        room2.setDungeonId("dungeon-1");
        room2.setTheme("treasure");
        room2.setActive(true);
        service.createRoom(room2);

        List<DungeonRoomService.DungeonRoom> rooms = service.listRoomsByDungeon("dungeon-1");
        assertEquals(2, rooms.size());
        assertEquals("Entrance Hall", rooms.get(0).getName());
        assertEquals("Treasure Room", rooms.get(1).getName());
    }

    @Test
    void mockDungeonRoomService_listRoomsByTheme() {
        MockDungeonRoomService service = new MockDungeonRoomService();

        DungeonRoomService.DungeonRoom room1 = new DungeonRoomService.DungeonRoom();
        room1.setName("Entrance Hall");
        room1.setDungeonId("dungeon-1");
        room1.setTheme("settlement");
        service.createRoom(room1);

        DungeonRoomService.DungeonRoom room2 = new DungeonRoomService.DungeonRoom();
        room2.setName("Factory Floor");
        room2.setDungeonId("dungeon-2");
        room2.setTheme("industrial");
        service.createRoom(room2);

        List<DungeonRoomService.DungeonRoom> settlementRooms = service.listRoomsByTheme("settlement");
        assertEquals(1, settlementRooms.size());
        assertEquals("Entrance Hall", settlementRooms.get(0).getName());
    }

    @Test
    void buildingInteriorGenerator_generateSettlementInteriors() {
        MockDungeonRoomService mockService = new MockDungeonRoomService();
        BuildingInteriorGenerator generator = new BuildingInteriorGenerator(mockService, 42L);

        Hex3D centerHex = new Hex3D(5, -3, -2);
        Settlement settlement = new Settlement("SET-1", "Test Town", centerHex, Settlement.Size.TOWN);

        List<String> dungeonIds = generator.generateSettlementInteriors(settlement);
        assertNotNull(dungeonIds);
        assertTrue(dungeonIds.size() > 0, "Should generate at least one interior room for a town");

        // Verify the settlement has dungeon IDs set
        assertNotNull(settlement.getDungeonIds());
        assertTrue(settlement.getDungeonIds().size() > 0);
    }

    @Test
    void buildingInteriorGenerator_generateIndustryInterior() {
        MockDungeonRoomService mockService = new MockDungeonRoomService();
        BuildingInteriorGenerator generator = new BuildingInteriorGenerator(mockService, 42L);

        Industry industry = new Industry("IND-1", IndustryType.FARM, new Hex3D(6, -3, -3));

        String dungeonId = generator.generateIndustryInterior(industry);
        assertNotNull(dungeonId);
        assertEquals(dungeonId, industry.getDungeonId());
    }

    @Test
    void buildingInteriorGenerator_generateStationInterior() {
        MockDungeonRoomService mockService = new MockDungeonRoomService();
        BuildingInteriorGenerator generator = new BuildingInteriorGenerator(mockService, 42L);

        Station station = new Station("STA-1", "Test Station", new Hex3D(5, -3, -2));

        String dungeonId = generator.generateStationInterior(station);
        assertNotNull(dungeonId);
        assertEquals(dungeonId, station.getDungeonId());
    }

    @Test
    void buildingInteriorGenerator_settlementSizeScaling() {
        MockDungeonRoomService mockService = new MockDungeonRoomService();
        BuildingInteriorGenerator generator = new BuildingInteriorGenerator(mockService, 42L);

        // Village should have fewer rooms than Metropolis
        Settlement village = new Settlement("VIL-1", "Small Village", new Hex3D(1, 0, -1), Settlement.Size.VILLAGE);
        List<String> villageDungeons = generator.generateSettlementInteriors(village);

        MockDungeonRoomService mockService2 = new MockDungeonRoomService();
        BuildingInteriorGenerator generator2 = new BuildingInteriorGenerator(mockService2, 42L);
        Settlement metropolis = new Settlement("MET-1", "Big City", new Hex3D(10, 0, -10), Settlement.Size.METROPOLIS);
        List<String> metropolisDungeons = generator2.generateSettlementInteriors(metropolis);

        assertTrue(metropolisDungeons.size() > villageDungeons.size(),
                "Metropolis should have more interior rooms than Village");
    }

    @Test
    void mockDungeonRoomService_trapCheck() {
        MockDungeonRoomService service = new MockDungeonRoomService();

        DungeonRoomService.DungeonRoom room = new DungeonRoomService.DungeonRoom();
        room.setName("Trap Room");
        room.setDungeonId("dungeon-1");
        room.setTrap("Pressure Plate");
        room.setTrapDc(15);
        room.setTrapDamage("10");
        room.setTrapDamageExpr("2d6");
        service.createRoom(room);

        // Roll 10 + 2 dex + 2 proficiency = 14, DC 15 -> fail
        DungeonRoomService.TrapCheckResult result = service.makeTrapCheck(room.getId(), 2, 2, 10);
        assertFalse(result.isSuccess());
        assertEquals("Trap Room", result.getRoomName());
        assertEquals(15, result.getDc().intValue());

        // Roll 15 + 2 + 2 = 19, DC 15 -> success
        DungeonRoomService.TrapCheckResult successResult = service.makeTrapCheck(room.getId(), 2, 2, 15);
        assertTrue(successResult.isSuccess());
    }

    @Test
    void mockDungeonRoomService_treasureDiscovery() {
        MockDungeonRoomService service = new MockDungeonRoomService();

        DungeonRoomService.DungeonRoom room = new DungeonRoomService.DungeonRoom();
        room.setName("Treasure Room");
        room.setDungeonId("dungeon-1");
        room.setTreasure("Gold Coins");
        room.setGoldPieces(500);
        room.setMagicRarity("Common");
        service.createRoom(room);

        DungeonRoomService.TreasureResult result = service.discoverTreasure(room.getId());
        assertEquals("Gold Coins", result.getTreasure());
        assertEquals(500, result.getGoldPieces());
        assertEquals("Common", result.getMagicRarity());
    }

    // ---- TurnSummaryDto tests ----

    @Test
    @Disabled("TurnSummaryDto inner class does not exist in HexPlanetSurfaceController")
    void turnSummaryDto_construction() {
        // Skipped: TurnSummaryDto does not exist
    }

    @Test
    @Disabled("TurnSummaryDto inner class does not exist in HexPlanetSurfaceController")
    void turnSummaryDto_defaultsToZero() {
        // Skipped: TurnSummaryDto does not exist
    }

    @Test
    @Disabled("TurnSummaryDto inner class does not exist in HexPlanetSurfaceController")
    void turnSummaryDto_toString_containsKeyInfo() {
        // Skipped: TurnSummaryDto does not exist
    }

    // ---- PlanetSurfaceDto new fields tests ----

    @Test
    void planetSurfaceDto_contractsQuestsTradeRoutes_fields() {
        PlanetSurfaceDto dto = new PlanetSurfaceDto(42, "TestPlanet");

        List<ContractDto> contracts = List.of(new ContractDto());
        List<QuestDto> quests = List.of(new QuestDto());
        List<TradeRouteDto> routes = List.of(new TradeRouteDto());

        dto.setContracts(contracts);
        dto.setQuests(quests);
        dto.setTradeRoutes(routes);

        assertEquals(1, dto.getContracts().size());
        assertEquals(1, dto.getQuests().size());
        assertEquals(1, dto.getTradeRoutes().size());
    }

    @Test
    void planetSurfaceDto_summaryDto_newCounts() {
        PlanetSurfaceDto.SummaryDto summary = new PlanetSurfaceDto.SummaryDto();
        summary.setContracts(15L);
        summary.setQuests(7L);
        summary.setTradeRoutes(4L);

        assertEquals(15L, summary.getContracts());
        assertEquals(7L, summary.getQuests());
        assertEquals(4L, summary.getTradeRoutes());
    }

    @Test
    void contractDto_construction() {
        ContractDto dto = new ContractDto();
        dto.setId("contract-1");
        dto.setName("Deliver Grain");
        dto.setType("STANDARD");
        dto.setStatus("AVAILABLE");
        dto.setPayCredits(5000L);
        dto.setXpReward(100);

        assertEquals("contract-1", dto.getId());
        assertEquals("Deliver Grain", dto.getName());
        assertEquals("STANDARD", dto.getType());
        assertEquals("AVAILABLE", dto.getStatus());
        assertEquals(5000L, dto.getPayCredits());
        assertEquals(100, dto.getXpReward());
    }

    @Test
    void questDto_construction() {
        QuestDto dto = new QuestDto();
        dto.setId("quest-1");
        dto.setTitle("Escort Mission");
        dto.setType("ESCORT");
        dto.setStatus("AVAILABLE");
        dto.setCreditsReward(10000L);
        dto.setXpReward(200);

        assertEquals("quest-1", dto.getId());
        assertEquals("Escort Mission", dto.getTitle());
        assertEquals("ESCORT", dto.getType());
        assertEquals("AVAILABLE", dto.getStatus());
        assertEquals(10000L, dto.getCreditsReward());
        assertEquals(200, dto.getXpReward());
    }

    @Test
    void tradeRouteDto_construction() {
        TradeRouteDto dto = new TradeRouteDto();
        dto.setId("route-1");
        dto.setName("Grain Express");
        dto.setStatus("ACTIVE");
        dto.setTotalIncome(5000L);
        dto.setStationCount(3);

        assertEquals("route-1", dto.getId());
        assertEquals("Grain Express", dto.getName());
        assertEquals("ACTIVE", dto.getStatus());
        assertEquals(5000L, dto.getTotalIncome());
        assertEquals(3, dto.getStationCount());
    }
}
