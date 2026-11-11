package com.yisy.modules.doramcu.hex.planet;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class TransportCorporationTest {

    private TransportCorporation corp;

    @BeforeEach
    void setUp() {
        corp = new TransportCorporation("Test Corp", "player1", 10000);
    }

    @Test
    void constructor_setsFields() {
        assertEquals("Test Corp", corp.getName());
        assertEquals("player1", corp.getOwnerId());
        assertEquals(10000, corp.getCredits());
        assertEquals(CorporationRank.FREELANCER, corp.getRank());
        assertEquals(10, corp.getReputation());
        assertEquals(0, corp.getXp());
        assertEquals(100, corp.getXpToNextRank());
        assertEquals(0, corp.getTotalAssetCount());
    }

    @Test
    void constructor_nullName_throws() {
        assertThrows(IllegalArgumentException.class, () -> new TransportCorporation(null, "player1", 1000));
    }

    @Test
    void constructor_emptyName_throws() {
        assertThrows(IllegalArgumentException.class, () -> new TransportCorporation("", "player1", 1000));
    }

    @Test
    void constructor_nullOwnerId_throws() {
        assertThrows(IllegalArgumentException.class, () -> new TransportCorporation("Name", null, 1000));
    }

    @Test
    void constructor_emptyOwnerId_throws() {
        assertThrows(IllegalArgumentException.class, () -> new TransportCorporation("Name", "", 1000));
    }

    @Test
    void getId_returnsNonEmpty() {
        assertNotNull(corp.getId());
        assertFalse(corp.getId().isEmpty());
    }

    @Test
    void addCredits_increasesBalance() {
        corp.addCredits(5000);
        assertEquals(15000, corp.getCredits());
    }

    @Test
    void addCredits_negative_throws() {
        assertThrows(IllegalArgumentException.class, () -> corp.addCredits(-100));
    }

    @Test
    void spendCredits_decreasesBalance() {
        assertTrue(corp.spendCredits(3000));
        assertEquals(7000, corp.getCredits());
    }

    @Test
    void spendCredits_insufficientFunds_returnsFalse() {
        assertFalse(corp.spendCredits(20000));
        assertEquals(10000, corp.getCredits());
    }

    @Test
    void spendCredits_negative_throws() {
        assertThrows(IllegalArgumentException.class, () -> corp.spendCredits(-100));
    }

    @Test
    void updateReputation_clampsToRange() {
        corp.updateReputation(100);
        assertEquals(100, corp.getReputation());
        corp.updateReputation(-200);
        assertEquals(0, corp.getReputation());
    }

    @Test
    void addXp_progressesRank() {
        assertFalse(corp.addXp(50));
        assertEquals(CorporationRank.FREELANCER, corp.getRank());
        assertTrue(corp.addXp(50));
        assertEquals(CorporationRank.STARTUP, corp.getRank());
    }

    @Test
    void addXp_negative_throws() {
        assertThrows(IllegalArgumentException.class, () -> corp.addXp(-10));
    }

    @Test
    void addStation_tracksAsset() {
        corp.addStation("station1");
        assertTrue(corp.getOwnedStationIds().contains("station1"));
        assertEquals(1, corp.getTotalAssetCount());
    }

    @Test
    void addStation_null_throws() {
        assertThrows(NullPointerException.class, () -> corp.addStation(null));
    }

    @Test
    void removeStation_removesAsset() {
        corp.addStation("station1");
        assertTrue(corp.removeStation("station1"));
        assertFalse(corp.getOwnedStationIds().contains("station1"));
    }

    @Test
    void removeStation_notFound_returnsFalse() {
        assertFalse(corp.removeStation("nonexistent"));
    }

    @Test
    void addVehicle_tracksAsset() {
        corp.addVehicle("vehicle1");
        assertTrue(corp.getOwnedVehicleIds().contains("vehicle1"));
    }

    @Test
    void addRoute_tracksAsset() {
        corp.addRoute("route1");
        assertTrue(corp.getOwnedRouteIds().contains("route1"));
    }

    @Test
    void addContract_tracksContract() {
        corp.addContract("contract1");
        assertTrue(corp.getActiveContractIds().contains("contract1"));
    }

    @Test
    void removeContract_removesContract() {
        corp.addContract("contract1");
        assertTrue(corp.removeContract("contract1"));
        assertFalse(corp.getActiveContractIds().contains("contract1"));
    }

    @Test
    void setPlanetsOperated_setsValue() {
        corp.setPlanetsOperated(5);
        assertEquals(5, corp.getPlanetsOperated());
    }

    @Test
    void setPlanetsOperated_negative_throws() {
        assertThrows(IllegalArgumentException.class, () -> corp.setPlanetsOperated(-1));
    }

    @Test
    void addCargoDelivered_accumulates() {
        corp.addCargoDelivered(1000);
        corp.addCargoDelivered(500);
        assertEquals(1500, corp.getTotalCargoDelivered());
    }

    @Test
    void addCargoDelivered_negative_throws() {
        assertThrows(IllegalArgumentException.class, () -> corp.addCargoDelivered(-100));
    }

    @Test
    void addCargo_updatesInventory() {
        corp.addCargo(CargoType.GRAIN, 100);
        assertEquals(100, (int) corp.getCargoInventory().get(CargoType.GRAIN));
    }

    @Test
    void addCargo_mergesExisting() {
        corp.addCargo(CargoType.GRAIN, 100);
        corp.addCargo(CargoType.GRAIN, 50);
        assertEquals(150, (int) corp.getCargoInventory().get(CargoType.GRAIN));
    }

    @Test
    void removeCargo_success() {
        corp.addCargo(CargoType.GRAIN, 100);
        assertTrue(corp.removeCargo(CargoType.GRAIN, 30));
        assertEquals(70, (int) corp.getCargoInventory().get(CargoType.GRAIN));
    }

    @Test
    void removeCargo_exactAmount_removesEntry() {
        corp.addCargo(CargoType.GRAIN, 100);
        assertTrue(corp.removeCargo(CargoType.GRAIN, 100));
        assertNull(corp.getCargoInventory().get(CargoType.GRAIN));
    }

    @Test
    void removeCargo_insufficient_returnsFalse() {
        corp.addCargo(CargoType.GRAIN, 50);
        assertFalse(corp.removeCargo(CargoType.GRAIN, 100));
        assertEquals(50, (int) corp.getCargoInventory().get(CargoType.GRAIN));
    }

    @Test
    void removeCargo_negative_throws() {
        assertThrows(IllegalArgumentException.class, () -> corp.removeCargo(CargoType.GRAIN, -10));
    }

    @Test
    void getTotalAssetCount_sumsAllTypes() {
        corp.addStation("s1");
        corp.addVehicle("v1");
        corp.addRoute("r1");
        assertEquals(3, corp.getTotalAssetCount());
    }

    @Test
    void setRank_null_throws() {
        assertThrows(IllegalArgumentException.class, () -> corp.setRank(null));
    }

    @Test
    void setRank_changesRank() {
        corp.setRank(CorporationRank.STARTUP);
        assertEquals(CorporationRank.STARTUP, corp.getRank());
    }

    @Test
    void questManagement_worksCorrectly() {
        corp.addActiveQuest("quest1");
        assertEquals(1, corp.getActiveQuestIds().size());
        assertTrue(corp.completeQuest("quest1"));
        assertEquals(0, corp.getActiveQuestIds().size());
        assertEquals(1, corp.getCompletedQuestIds().size());
        assertEquals(1, corp.getCompletedQuestCount());
    }

    @Test
    void completeQuest_notActive_returnsFalse() {
        assertFalse(corp.completeQuest("nonexistent"));
    }

    @Test
    void getCargoInventory_returnsUnmodifiableMap() {
        assertThrows(UnsupportedOperationException.class, () ->
            corp.getCargoInventory().put(CargoType.GRAIN, 100));
    }

    @Test
    void toString_containsKeyInfo() {
        String str = corp.toString();
        assertTrue(str.contains("Test Corp"));
        assertTrue(str.contains("Freelancer"));
    }
}
