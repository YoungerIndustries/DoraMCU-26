package com.yisy.modules.doramcu.hex.planet;

import com.yisy.modules.doramcu.gamecore.DungeonRoomService;
import com.yisy.modules.doramcu.hex.Hex3D;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link BuildingInteriorGenerator}.
 * Tests all 13 room templates and the settlement/industry/station generation methods.
 */
class BuildingInteriorGeneratorTest {

    private MockDungeonRoomService mockService;
    private BuildingInteriorGenerator generator;

    @BeforeEach
    void setUp() {
        mockService = new MockDungeonRoomService();
        generator = new BuildingInteriorGenerator(mockService, 42L);
    }

    // ---- Settlement Interior Tests ----

    @Test
    void generateSettlementInteriors_village_hasHouseAndMarket() {
        Settlement settlement = new Settlement("SET-VILLAGE", "Village", new Hex3D(0, 0, 0), Settlement.Size.VILLAGE);
        List<String> dungeonIds = generator.generateSettlementInteriors(settlement);

        assertEquals(2, dungeonIds.size(), "Village should have 2 buildings");
        assertTrue(dungeonIds.get(0).contains("-house-1"));
        assertTrue(dungeonIds.get(1).contains("-market-1"));
        assertEquals(dungeonIds, settlement.getDungeonIds(), "Settlement should store dungeon IDs");
    }

    @Test
    void generateSettlementInteriors_town_hasFourBuildings() {
        Settlement settlement = new Settlement("SET-TOWN", "Town", new Hex3D(0, 0, 0), Settlement.Size.TOWN);
        List<String> dungeonIds = generator.generateSettlementInteriors(settlement);

        assertEquals(4, dungeonIds.size(), "Town should have 4 buildings");
        assertTrue(dungeonIds.stream().anyMatch(id -> id.contains("-house-1")));
        assertTrue(dungeonIds.stream().anyMatch(id -> id.contains("-market-1")));
        assertTrue(dungeonIds.stream().anyMatch(id -> id.contains("-townhall-1")));
        assertTrue(dungeonIds.stream().anyMatch(id -> id.contains("-inn-1")));
    }

    @Test
    void generateSettlementInteriors_city_hasSevenBuildings() {
        Settlement settlement = new Settlement("SET-CITY", "City", new Hex3D(0, 0, 0), Settlement.Size.CITY);
        List<String> dungeonIds = generator.generateSettlementInteriors(settlement);

        assertEquals(7, dungeonIds.size(), "City should have 7 buildings");
        assertTrue(dungeonIds.stream().anyMatch(id -> id.contains("-church-1")));
        assertTrue(dungeonIds.stream().anyMatch(id -> id.contains("-police-1")));
        assertTrue(dungeonIds.stream().anyMatch(id -> id.contains("-warehouse-1")));
    }

    @Test
    void generateSettlementInteriors_metropolis_hasSpacePort() {
        Settlement settlement = new Settlement("SET-METRO", "Metropolis", new Hex3D(0, 0, 0), Settlement.Size.METROPOLIS);
        List<String> dungeonIds = generator.generateSettlementInteriors(settlement);

        assertTrue(dungeonIds.size() >= 10, "Metropolis should have 10+ buildings");
        assertTrue(dungeonIds.stream().anyMatch(id -> id.contains("-spaceport-1")));
        // Extra houses
        assertTrue(dungeonIds.stream().filter(id -> id.contains("-house-")).count() >= 4);
    }

    // ---- Industry Interior Tests ----

    @Test
    void generateIndustryInterior_createsFactory() {
        Industry industry = new Industry("IND-1", IndustryType.FARM, new Hex3D(1, 0, -1));
        String dungeonId = generator.generateIndustryInterior(industry);

        assertNotNull(dungeonId);
        assertEquals("IND-1", dungeonId);
        assertEquals(dungeonId, industry.getDungeonId());

        // Verify rooms were created
        List<DungeonRoomService.DungeonRoom> rooms = mockService.listRoomsByDungeon(dungeonId);
        assertEquals(5, rooms.size(), "Factory should have 5 rooms");
    }

    // ---- Station Interior Tests ----

    @Test
    void generateStationInterior_busStop_createsTwoRooms() {
        Station station = new Station("STAT-BUS", "Bus Stop", new Hex3D(2, -1, -1));
        station.setInfrastructureType(TerrainData.InfrastructureType.STATION);
        String dungeonId = generator.generateStationInterior(station);

        assertNotNull(dungeonId);
        assertEquals(dungeonId, station.getDungeonId());

        List<DungeonRoomService.DungeonRoom> rooms = mockService.listRoomsByDungeon(dungeonId);
        assertEquals(2, rooms.size(), "Bus stop should have 2 rooms");
    }

    @Test
    void generateStationInterior_railStation_createsSevenRooms() {
        Station station = new Station("STAT-RAIL", "Rail Station", new Hex3D(3, -2, -1));
        station.setInfrastructureType(TerrainData.InfrastructureType.STATION);
        // Add extra catchment hexes to trigger rail station template
        station.addCatchmentHex(new Hex3D(4, -2, -2));
        station.addCatchmentHex(new Hex3D(3, -1, -2));
        station.addCatchmentHex(new Hex3D(4, -3, -1));

        String dungeonId = generator.generateStationInterior(station);

        List<DungeonRoomService.DungeonRoom> rooms = mockService.listRoomsByDungeon(dungeonId);
        assertEquals(7, rooms.size(), "Rail station should have 7 rooms");
    }

    // ---- Individual Template Tests ----

    @Test
    void generateHouse_createsFiveRooms() {
        String dungeonId = generator.generateHouse("test-house");
        List<DungeonRoomService.DungeonRoom> rooms = mockService.listRoomsByDungeon(dungeonId);
        assertEquals(5, rooms.size());

        // Check room names
        List<String> names = rooms.stream().map(DungeonRoomService.DungeonRoom::getName).toList();
        assertTrue(names.contains("Front Porch"));
        assertTrue(names.contains("Living Room"));
        assertTrue(names.contains("Kitchen"));
        assertTrue(names.contains("Bedroom"));
        assertTrue(names.contains("Storage Closet"));
    }

    @Test
    void generateTownHall_hasVaultWithTrap() {
        String dungeonId = generator.generateTownHall("test-townhall");
        List<DungeonRoomService.DungeonRoom> rooms = mockService.listRoomsByDungeon(dungeonId);
        assertEquals(5, rooms.size());

        DungeonRoomService.DungeonRoom vault = rooms.stream()
                .filter(r -> "Vault".equals(r.getName()))
                .findFirst().orElseThrow();
        assertNotNull(vault.getTrap(), "Vault should have a trap");
        assertEquals(15, vault.getTrapDc().intValue());
        assertNotNull(vault.getTreasure());
    }

    @Test
    void generateMarket_hasAuctionHouseWithTreasure() {
        String dungeonId = generator.generateMarket("test-market");
        List<DungeonRoomService.DungeonRoom> rooms = mockService.listRoomsByDungeon(dungeonId);
        assertEquals(4, rooms.size());

        DungeonRoomService.DungeonRoom auction = rooms.stream()
                .filter(r -> "Auction House".equals(r.getName()))
                .findFirst().orElseThrow();
        assertNotNull(auction.getTreasure());
        assertNotNull(auction.getEncounter());
    }

    @Test
    void generateRailStation_hasPlatforms() {
        String dungeonId = generator.generateRailStation("test-rail");
        List<DungeonRoomService.DungeonRoom> rooms = mockService.listRoomsByDungeon(dungeonId);
        assertEquals(7, rooms.size());

        List<String> names = rooms.stream().map(DungeonRoomService.DungeonRoom::getName).toList();
        assertTrue(names.contains("Platform A"));
        assertTrue(names.contains("Platform B"));
        assertTrue(names.contains("Control Room"));
    }

    @Test
    void generateAirport_hasSecurityWithTrap() {
        String dungeonId = generator.generateAirport("test-airport");
        List<DungeonRoomService.DungeonRoom> rooms = mockService.listRoomsByDungeon(dungeonId);
        assertEquals(7, rooms.size());

        DungeonRoomService.DungeonRoom security = rooms.stream()
                .filter(r -> "Security Check".equals(r.getName()))
                .findFirst().orElseThrow();
        assertNotNull(security.getTrap());
        assertEquals("Combat", security.getTheme());
    }

    @Test
    void generateSpacePort_hasTenRooms() {
        String dungeonId = generator.generateSpacePort("test-spaceport");
        List<DungeonRoomService.DungeonRoom> rooms = mockService.listRoomsByDungeon(dungeonId);
        assertEquals(10, rooms.size());

        List<String> names = rooms.stream().map(DungeonRoomService.DungeonRoom::getName).toList();
        assertTrue(names.contains("Docking Bay Alpha"));
        assertTrue(names.contains("Docking Bay Beta"));
        assertTrue(names.contains("Fuel Station"));
    }

    @Test
    void generateSpacePort_fuelStationHasExplosionTrap() {
        String dungeonId = generator.generateSpacePort("test-spaceport-2");
        List<DungeonRoomService.DungeonRoom> rooms = mockService.listRoomsByDungeon(dungeonId);

        DungeonRoomService.DungeonRoom fuel = rooms.stream()
                .filter(r -> "Fuel Station".equals(r.getName()))
                .findFirst().orElseThrow();
        assertNotNull(fuel.getTrap());
        assertEquals(18, fuel.getTrapDc().intValue());
    }

    @Test
    void generateInn_hasThreeGuestRooms() {
        String dungeonId = generator.generateInn("test-inn");
        List<DungeonRoomService.DungeonRoom> rooms = mockService.listRoomsByDungeon(dungeonId);
        assertEquals(7, rooms.size());

        List<String> names = rooms.stream().map(DungeonRoomService.DungeonRoom::getName).toList();
        assertTrue(names.contains("Room 1"));
        assertTrue(names.contains("Room 2"));
        assertTrue(names.contains("Room 3"));
    }

    @Test
    void generateChurch_hasHolyRelic() {
        String dungeonId = generator.generateChurch("test-church");
        List<DungeonRoomService.DungeonRoom> rooms = mockService.listRoomsByDungeon(dungeonId);
        assertEquals(4, rooms.size());

        DungeonRoomService.DungeonRoom chapel = rooms.stream()
                .filter(r -> "Chapel".equals(r.getName()))
                .findFirst().orElseThrow();
        assertNotNull(chapel.getTreasure());
        assertTrue(chapel.getTreasure().contains("Holy Relic"));
    }

    @Test
    void generatePoliceStation_hasHoldingCells() {
        String dungeonId = generator.generatePoliceStation("test-police");
        List<DungeonRoomService.DungeonRoom> rooms = mockService.listRoomsByDungeon(dungeonId);
        assertEquals(4, rooms.size());

        DungeonRoomService.DungeonRoom cells = rooms.stream()
                .filter(r -> "Holding Cells".equals(r.getName()))
                .findFirst().orElseThrow();
        assertEquals("Combat", cells.getTheme());
        assertNotNull(cells.getTrap());
    }

    @Test
    void generateFactory_hasProductionFloor() {
        String dungeonId = generator.generateFactory("test-factory");
        List<DungeonRoomService.DungeonRoom> rooms = mockService.listRoomsByDungeon(dungeonId);
        assertEquals(5, rooms.size());

        DungeonRoomService.DungeonRoom production = rooms.stream()
                .filter(r -> "Production Floor".equals(r.getName()))
                .findFirst().orElseThrow();
        assertEquals("Industry", production.getTheme());
        assertNotNull(production.getTrap());
    }

    // ---- Connection Tests ----

    @Test
    void generateHouse_roomsAreConnected() {
        generator.generateHouse("test-conn-house");
        List<DungeonRoomService.DungeonRoom> rooms = mockService.listRoomsByDungeon("test-conn-house");

        // Find the Living Room and check it has connections
        DungeonRoomService.DungeonRoom livingRoom = rooms.stream()
                .filter(r -> "Living Room".equals(r.getName()))
                .findFirst().orElseThrow();

        List<DungeonRoomService.DungeonRoom> connected = mockService.getConnectedRooms(livingRoom.getId());
        assertTrue(connected.size() >= 2, "Living Room should connect to at least 2 rooms");
    }

    @Test
    void generateBoatDock_waterfrontConnectsToMultipleRooms() {
        generator.generateBoatDock("test-conn-dock");
        List<DungeonRoomService.DungeonRoom> rooms = mockService.listRoomsByDungeon("test-conn-dock");

        DungeonRoomService.DungeonRoom waterfront = rooms.stream()
                .filter(r -> "Waterfront".equals(r.getName()))
                .findFirst().orElseThrow();

        List<DungeonRoomService.DungeonRoom> connected = mockService.getConnectedRooms(waterfront.getId());
        assertTrue(connected.size() >= 2, "Waterfront should connect to multiple rooms");
    }

    // ---- Dungeon ID Storage Tests ----

    @Test
    void settlementStoresDungeonIds() {
        Settlement settlement = new Settlement("SET-STORE", "Store", new Hex3D(0, 0, 0), Settlement.Size.VILLAGE);
        assertTrue(settlement.getDungeonIds().isEmpty());

        generator.generateSettlementInteriors(settlement);
        assertEquals(2, settlement.getDungeonIds().size());
    }

    @Test
    void industryStoresDungeonId() {
        Industry industry = new Industry("IND-STORE", IndustryType.FARM, new Hex3D(1, 0, -1));
        assertNull(industry.getDungeonId());

        generator.generateIndustryInterior(industry);
        assertNotNull(industry.getDungeonId());
        assertEquals("IND-STORE", industry.getDungeonId());
    }

    @Test
    void stationStoresDungeonId() {
        Station station = new Station("STAT-STORE", "Store Station", new Hex3D(2, -1, -1));
        assertNull(station.getDungeonId());

        generator.generateStationInterior(station);
        assertNotNull(station.getDungeonId());
    }

    // ---- MockDungeonRoomService Tests ----

    @Test
    void mockService_createsAndRetrievesRooms() {
        DungeonRoomService.DungeonRoom room = new DungeonRoomService.DungeonRoom();
        room.setName("Test Room");
        room.setDungeonId("test-dungeon");
        room.setTheme("Social");

        DungeonRoomService.DungeonRoom created = mockService.createRoom(room);
        assertNotNull(created.getId());

        DungeonRoomService.DungeonRoom retrieved = mockService.getRoom(created.getId());
        assertEquals("Test Room", retrieved.getName());
    }

    @Test
    void mockService_listByTheme() {
        generator.generateHouse("theme-test");
        generator.generateFactory("theme-test-factory");

        List<DungeonRoomService.DungeonRoom> socialRooms = mockService.listRoomsByTheme("Social");
        assertTrue(socialRooms.size() > 3, "Should have multiple Social rooms");

        List<DungeonRoomService.DungeonRoom> industryRooms = mockService.listRoomsByTheme("Industry");
        assertEquals(1, industryRooms.size(), "Should have 1 Industry room");
    }

    @Test
    void mockService_trapCheck() {
        generator.generateTownHall("trap-test");
        List<DungeonRoomService.DungeonRoom> rooms = mockService.listRoomsByDungeon("trap-test");

        DungeonRoomService.DungeonRoom vault = rooms.stream()
                .filter(r -> r.getTrap() != null)
                .findFirst().orElseThrow();

        // Roll 1 with +5 dex and +2 prof = 8, DC 15 → fail
        DungeonRoomService.TrapCheckResult result = mockService.makeTrapCheck(vault.getId(), 5, 2, 1);
        assertTrue(result.isCriticalFailure());
    }

    @Test
    void mockService_treasureDiscovery() {
        generator.generateMarket("treasure-test");
        List<DungeonRoomService.DungeonRoom> rooms = mockService.listRoomsByDungeon("treasure-test");

        DungeonRoomService.DungeonRoom auction = rooms.stream()
                .filter(r -> r.getTreasure() != null)
                .findFirst().orElseThrow();

        DungeonRoomService.TreasureResult result = mockService.discoverTreasure(auction.getId());
        assertNotNull(result.getTreasure());
        assertTrue(result.getGoldPieces() > 0);
    }
}
