package com.yisy.modules.doramcu.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the 5 planet surface persistence JPA entities.
 * Verifies getters, setters, default values, and toString methods.
 */
class PlanetPersistenceEntityTest {

    // ---- PlanetTerrainCell tests ----

    @Test
    void planetTerrainCell_defaultValues() {
        PlanetTerrainCell cell = new PlanetTerrainCell();
        assertNull(cell.getId());
        assertEquals(0, cell.getPlanetId());
        assertEquals(0, cell.getHexX());
        assertEquals(0, cell.getHexY());
        assertEquals(0, cell.getHexZ());
        assertEquals(0, cell.getWaterLevel());
        assertNull(cell.getTerrainType());
        assertNull(cell.getBiomeName());
        assertEquals(0.0, cell.getTemperature(), 0.001);
        assertEquals(0.0, cell.getMoisture(), 0.001);
        assertEquals(0, cell.getAltitude());
        assertNull(cell.getInfrastructure());
        assertNull(cell.getSettlementId());
        assertNull(cell.getIndustryId());
        assertNotNull(cell.getCreatedAt());
        assertNotNull(cell.getUpdatedAt());
    }

    @Test
    void planetTerrainCell_setAndGet() {
        PlanetTerrainCell cell = new PlanetTerrainCell();
        cell.setId(1L);
        cell.setPlanetId(42);
        cell.setHexX(10);
        cell.setHexY(-5);
        cell.setHexZ(-5);
        cell.setTerrainType("GRASSLAND");
        cell.setBiomeName("Temperate Grassland");
        cell.setTemperature(15.5);
        cell.setMoisture(0.6);
        cell.setAltitude(200);
        cell.setInfrastructure("ROAD");
        cell.setWaterLevel(0);
        cell.setSettlementId("SETTLE-1");
        cell.setIndustryId("IND-1");

        assertEquals(1L, cell.getId());
        assertEquals(42, cell.getPlanetId());
        assertEquals(10, cell.getHexX());
        assertEquals(-5, cell.getHexY());
        assertEquals(-5, cell.getHexZ());
        assertEquals("GRASSLAND", cell.getTerrainType());
        assertEquals("Temperate Grassland", cell.getBiomeName());
        assertEquals(15.5, cell.getTemperature(), 0.001);
        assertEquals(0.6, cell.getMoisture(), 0.001);
        assertEquals(200, cell.getAltitude());
        assertEquals("ROAD", cell.getInfrastructure());
        assertEquals(0, cell.getWaterLevel());
        assertEquals("SETTLE-1", cell.getSettlementId());
        assertEquals("IND-1", cell.getIndustryId());
    }

    @Test
    void planetTerrainCell_toString_containsKeyFields() {
        PlanetTerrainCell cell = new PlanetTerrainCell();
        cell.setPlanetId(7);
        cell.setHexX(1);
        cell.setHexY(2);
        cell.setHexZ(-3);
        cell.setTerrainType("DESERT");
        cell.setBiomeName("Hot Desert");

        String str = cell.toString();
        assertTrue(str.contains("planet=7"));
        assertTrue(str.contains("1,2,-3"));
        assertTrue(str.contains("DESERT"));
        assertTrue(str.contains("Hot Desert"));
    }

    // ---- PlanetSettlementEntity tests ----

    @Test
    void planetSettlementEntity_defaultValues() {
        PlanetSettlementEntity entity = new PlanetSettlementEntity();
        assertNull(entity.getId());
        assertNull(entity.getSettlementId());
        assertEquals(0, entity.getPlanetId());
        assertNull(entity.getName());
        assertEquals(0, entity.getCenterHexX());
        assertEquals(0, entity.getCenterHexY());
        assertEquals(0, entity.getCenterHexZ());
        assertNull(entity.getSize());
        assertEquals(0, entity.getPopulation());
        assertEquals(0, entity.getMaxPopulation());
        assertEquals(0, entity.getGrowthRate());
        assertEquals(0, entity.getTurnsSinceLastGrowth());
        assertNull(entity.getCargoGoalsJson());
        assertEquals(0, entity.getMailProduction());
        assertEquals(0, entity.getPassengerProduction());
        assertTrue(entity.isActive());
        assertNull(entity.getOwnerEmpireId());
        assertNotNull(entity.getCreatedAt());
        assertNotNull(entity.getUpdatedAt());
    }

    @Test
    void planetSettlementEntity_setAndGet() {
        PlanetSettlementEntity entity = new PlanetSettlementEntity();
        entity.setId(1L);
        entity.setSettlementId("SETTLE-42");
        entity.setPlanetId(10);
        entity.setName("MegaCity");
        entity.setCenterHexX(100);
        entity.setCenterHexY(-50);
        entity.setCenterHexZ(-50);
        entity.setSize("METROPOLIS");
        entity.setPopulation(1000000);
        entity.setMaxPopulation(2000000);
        entity.setGrowthRate(5);
        entity.setTurnsSinceLastGrowth(3);
        entity.setCargoGoalsJson("{\"coal\":10}");
        entity.setMailProduction(50);
        entity.setPassengerProduction(200);
        entity.setActive(false);
        entity.setOwnerEmpireId("empire-5");

        assertEquals("SETTLE-42", entity.getSettlementId());
        assertEquals(10, entity.getPlanetId());
        assertEquals("MegaCity", entity.getName());
        assertEquals(100, entity.getCenterHexX());
        assertEquals(-50, entity.getCenterHexY());
        assertEquals(-50, entity.getCenterHexZ());
        assertEquals("METROPOLIS", entity.getSize());
        assertEquals(1000000, entity.getPopulation());
        assertEquals(2000000, entity.getMaxPopulation());
        assertEquals(5, entity.getGrowthRate());
        assertEquals(3, entity.getTurnsSinceLastGrowth());
        assertEquals("{\"coal\":10}", entity.getCargoGoalsJson());
        assertEquals(50, entity.getMailProduction());
        assertEquals(200, entity.getPassengerProduction());
        assertFalse(entity.isActive());
        assertEquals("empire-5", entity.getOwnerEmpireId());
    }

    @Test
    void planetSettlementEntity_toString_containsKeyFields() {
        PlanetSettlementEntity entity = new PlanetSettlementEntity();
        entity.setSettlementId("S-1");
        entity.setName("TestTown");
        entity.setPlanetId(3);
        entity.setPopulation(5000);
        entity.setSize("TOWN");

        String str = entity.toString();
        assertTrue(str.contains("id=S-1"));
        assertTrue(str.contains("name=TestTown"));
        assertTrue(str.contains("planet=3"));
        assertTrue(str.contains("pop=5000"));
        assertTrue(str.contains("size=TOWN"));
    }

    // ---- PlanetIndustryEntity tests ----

    @Test
    void planetIndustryEntity_defaultValues() {
        PlanetIndustryEntity entity = new PlanetIndustryEntity();
        assertNull(entity.getId());
        assertNull(entity.getIndustryId());
        assertEquals(0, entity.getPlanetId());
        assertNull(entity.getIndustryType());
        assertEquals(0, entity.getHexX());
        assertEquals(0, entity.getHexY());
        assertEquals(0, entity.getHexZ());
        assertEquals(1.0, entity.getProductionLevel(), 0.001);
        assertEquals(0, entity.getTurnsSinceBuilt());
        assertTrue(entity.isActive());
        assertNull(entity.getOwnerEmpireId());
        assertNull(entity.getInputInventoryJson());
        assertNull(entity.getOutputInventoryJson());
        assertNotNull(entity.getCreatedAt());
        assertNotNull(entity.getUpdatedAt());
    }

    @Test
    void planetIndustryEntity_setAndGet() {
        PlanetIndustryEntity entity = new PlanetIndustryEntity();
        entity.setId(1L);
        entity.setIndustryId("IND-99");
        entity.setPlanetId(5);
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

        assertEquals("IND-99", entity.getIndustryId());
        assertEquals(5, entity.getPlanetId());
        assertEquals("COAL_MINE", entity.getIndustryType());
        assertEquals(50, entity.getHexX());
        assertEquals(-25, entity.getHexY());
        assertEquals(-25, entity.getHexZ());
        assertEquals(0.75, entity.getProductionLevel(), 0.001);
        assertEquals(10, entity.getTurnsSinceBuilt());
        assertTrue(entity.isActive());
        assertEquals("empire-2", entity.getOwnerEmpireId());
        assertEquals("{\"ore\":5}", entity.getInputInventoryJson());
        assertEquals("{\"coal\":10}", entity.getOutputInventoryJson());
    }

    @Test
    void planetIndustryEntity_toString_containsKeyFields() {
        PlanetIndustryEntity entity = new PlanetIndustryEntity();
        entity.setIndustryId("I-1");
        entity.setIndustryType("COAL_MINE");
        entity.setPlanetId(2);
        entity.setProductionLevel(0.5);

        String str = entity.toString();
        assertTrue(str.contains("id=I-1"));
        assertTrue(str.contains("type=COAL_MINE"));
        assertTrue(str.contains("planet=2"));
        assertTrue(str.contains("level=0.5"));
    }

    // ---- PlanetStationEntity tests ----

    @Test
    void planetStationEntity_defaultValues() {
        PlanetStationEntity entity = new PlanetStationEntity();
        assertNull(entity.getId());
        assertNull(entity.getStationId());
        assertEquals(0, entity.getPlanetId());
        assertNull(entity.getName());
        assertEquals(0, entity.getHexX());
        assertEquals(0, entity.getHexY());
        assertEquals(0, entity.getHexZ());
        assertNull(entity.getInfrastructureType());
        assertNull(entity.getCargoWaitingJson());
        assertEquals(0, entity.getPassengersWaiting());
        assertEquals(0, entity.getTotalIncome());
        assertNull(entity.getConnectedSettlementId());
        assertNull(entity.getConnectedIndustryId());
        assertNull(entity.getOwnerEmpireId());
        assertNotNull(entity.getCreatedAt());
        assertNotNull(entity.getUpdatedAt());
    }

    @Test
    void planetStationEntity_setAndGet() {
        PlanetStationEntity entity = new PlanetStationEntity();
        entity.setId(1L);
        entity.setStationId("STAT-50");
        entity.setPlanetId(8);
        entity.setName("Central Hub");
        entity.setHexX(100);
        entity.setHexY(-50);
        entity.setHexZ(-50);
        entity.setInfrastructureType("AIRPORT");
        entity.setCargoWaitingJson("{\"mail\":5}");
        entity.setPassengersWaiting(30);
        entity.setTotalIncome(5000);
        entity.setConnectedSettlementId("SETTLE-1");
        entity.setConnectedIndustryId("IND-1");
        entity.setOwnerEmpireId("empire-1");

        assertEquals("STAT-50", entity.getStationId());
        assertEquals(8, entity.getPlanetId());
        assertEquals("Central Hub", entity.getName());
        assertEquals(100, entity.getHexX());
        assertEquals(-50, entity.getHexY());
        assertEquals(-50, entity.getHexZ());
        assertEquals("AIRPORT", entity.getInfrastructureType());
        assertEquals("{\"mail\":5}", entity.getCargoWaitingJson());
        assertEquals(30, entity.getPassengersWaiting());
        assertEquals(5000, entity.getTotalIncome());
        assertEquals("SETTLE-1", entity.getConnectedSettlementId());
        assertEquals("IND-1", entity.getConnectedIndustryId());
        assertEquals("empire-1", entity.getOwnerEmpireId());
    }

    @Test
    void planetStationEntity_toString_containsKeyFields() {
        PlanetStationEntity entity = new PlanetStationEntity();
        entity.setStationId("ST-1");
        entity.setName("Airport");
        entity.setPlanetId(4);
        entity.setInfrastructureType("AIRPORT");

        String str = entity.toString();
        assertTrue(str.contains("id=ST-1"));
        assertTrue(str.contains("name=Airport"));
        assertTrue(str.contains("planet=4"));
        assertTrue(str.contains("infra=AIRPORT"));
    }

    // ---- PlanetVehicleEntity tests ----

    @Test
    void planetVehicleEntity_defaultValues() {
        PlanetVehicleEntity entity = new PlanetVehicleEntity();
        assertNull(entity.getId());
        assertNull(entity.getVehicleId());
        assertEquals(0, entity.getPlanetId());
        assertNull(entity.getVehicleType());
        assertEquals(0, entity.getCurrentHexX());
        assertEquals(0, entity.getCurrentHexY());
        assertEquals(0, entity.getCurrentHexZ());
        assertNull(entity.getRouteStationIdsJson());
        assertEquals(0, entity.getCurrentRouteIndex());
        assertNull(entity.getCargoInventoryJson());
        assertEquals(0, entity.getPassengerCount());
        assertEquals(0, entity.getTotalDistance());
        assertEquals(0, entity.getTotalCargoDelivered());
        assertEquals(0, entity.getTotalPassengersDelivered());
        assertTrue(entity.isActive());
        assertNull(entity.getOwnerEmpireId());
        assertNotNull(entity.getCreatedAt());
        assertNotNull(entity.getUpdatedAt());
    }

    @Test
    void planetVehicleEntity_setAndGet() {
        PlanetVehicleEntity entity = new PlanetVehicleEntity();
        entity.setId(1L);
        entity.setVehicleId("VEH-77");
        entity.setPlanetId(12);
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

        assertEquals("VEH-77", entity.getVehicleId());
        assertEquals(12, entity.getPlanetId());
        assertEquals("BUS", entity.getVehicleType());
        assertEquals(200, entity.getCurrentHexX());
        assertEquals(-100, entity.getCurrentHexY());
        assertEquals(-100, entity.getCurrentHexZ());
        assertEquals("[\"STAT-1\",\"STAT-2\"]", entity.getRouteStationIdsJson());
        assertEquals(1, entity.getCurrentRouteIndex());
        assertEquals("{\"mail\":3}", entity.getCargoInventoryJson());
        assertEquals(25, entity.getPassengerCount());
        assertEquals(500, entity.getTotalDistance());
        assertEquals(100, entity.getTotalCargoDelivered());
        assertEquals(200, entity.getTotalPassengersDelivered());
        assertTrue(entity.isActive());
        assertEquals("empire-3", entity.getOwnerEmpireId());
    }

    @Test
    void planetVehicleEntity_toString_containsKeyFields() {
        PlanetVehicleEntity entity = new PlanetVehicleEntity();
        entity.setVehicleId("V-1");
        entity.setVehicleType("TRUCK");
        entity.setPlanetId(6);
        entity.setPassengerCount(0);

        String str = entity.toString();
        assertTrue(str.contains("id=V-1"));
        assertTrue(str.contains("type=TRUCK"));
        assertTrue(str.contains("planet=6"));
    }

    // ---- Entity updatedAt mutation tests ----

    @Test
    void planetTerrainCell_setInfrastructure_updatesTimestamp() {
        PlanetTerrainCell cell = new PlanetTerrainCell();
        LocalDateTime original = cell.getUpdatedAt();
        try {
            Thread.sleep(10);
        } catch (InterruptedException ignored) {
        }
        cell.setInfrastructure("ROAD");
        assertNotEquals(original, cell.getUpdatedAt());
    }

    @Test
    void planetSettlementEntity_setSize_updatesTimestamp() {
        PlanetSettlementEntity entity = new PlanetSettlementEntity();
        LocalDateTime original = entity.getUpdatedAt();
        try {
            Thread.sleep(10);
        } catch (InterruptedException ignored) {
        }
        entity.setSize("CITY");
        assertNotEquals(original, entity.getUpdatedAt());
    }

    @Test
    void planetSettlementEntity_setActive_updatesTimestamp() {
        PlanetSettlementEntity entity = new PlanetSettlementEntity();
        LocalDateTime original = entity.getUpdatedAt();
        try {
            Thread.sleep(10);
        } catch (InterruptedException ignored) {
        }
        entity.setActive(false);
        assertNotEquals(original, entity.getUpdatedAt());
    }

    // ---- Entity manual updatedAt override test ----

    @Test
    void planetTerrainCell_setUpdatedAt_overrides() {
        PlanetTerrainCell cell = new PlanetTerrainCell();
        LocalDateTime custom = LocalDateTime.of(2025, 1, 1, 0, 0);
        cell.setUpdatedAt(custom);
        assertEquals(custom, cell.getUpdatedAt());
    }

    @Test
    void planetSettlementEntity_setUpdatedAt_overrides() {
        PlanetSettlementEntity entity = new PlanetSettlementEntity();
        LocalDateTime custom = LocalDateTime.of(2025, 6, 15, 12, 0);
        entity.setUpdatedAt(custom);
        assertEquals(custom, entity.getUpdatedAt());
    }
}
