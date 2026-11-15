package com.yisy.modules.doramcu.hex.planet;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CampaignTradeRouteTest {

    private CampaignTradeRoute createLegacyRoute() {
        return new CampaignTradeRoute(
                "corp1",
                CampaignTradeRoute.RouteType.SURFACE,
                "station1",
                "station2",
                1,
                CargoType.GRAIN,
                10,
                500,
                200
        );
    }

    private CampaignTradeRoute createBuilderRoute() {
        return CampaignTradeRoute.builder()
                .name("Grain Express")
                .ownerCorporationId("corp1")
                .addStationId("station1")
                .addStationId("station2")
                .addStationId("station3")
                .addVehicleId("vehicle1")
                .addCargoType(CargoType.GRAIN)
                .addCargoType(CargoType.STEEL)
                .totalDistance(150)
                .build();
    }

    // ---- Legacy constructor ----

    @Test
    void legacyConstructor_setsFields() {
        CampaignTradeRoute route = createLegacyRoute();
        assertNotNull(route.getId());
        assertEquals("corp1", route.getOwnerCorporationId());
        assertEquals(CampaignTradeRoute.RouteType.SURFACE, route.getRouteType());
        assertEquals("station1", route.getOriginId());
        assertEquals("station2", route.getDestinationId());
        assertEquals(1, route.getPlanetId());
        assertEquals(CargoType.GRAIN, route.getCargoType());
        assertEquals(10, route.getCargoPerTurn());
        assertEquals(500, route.getRevenuePerTurn());
        assertEquals(200, route.getOperatingCostPerTurn());
    }

    @Test
    void legacyConstructor_activeDefaultsToTrue() {
        CampaignTradeRoute route = createLegacyRoute();
        assertTrue(route.isActive());
    }

    @Test
    void legacyConstructor_totalsDefaultToZero() {
        CampaignTradeRoute route = createLegacyRoute();
        assertEquals(0, route.getTotalCargoDelivered());
        assertEquals(0, route.getTotalRevenue());
    }

    @Test
    void legacyConstructor_nameIsNull() {
        CampaignTradeRoute route = createLegacyRoute();
        assertNull(route.getName());
    }

    @Test
    void legacyConstructor_stationIdsIsEmpty() {
        CampaignTradeRoute route = createLegacyRoute();
        assertEquals(0, route.getStationIds().size());
    }

    // ---- Builder pattern ----

    @Test
    void builder_setsAllFields() {
        CampaignTradeRoute route = createBuilderRoute();
        assertEquals("Grain Express", route.getName());
        assertEquals("corp1", route.getOwnerCorporationId());
        assertEquals(CampaignTradeRoute.RouteType.SURFACE, route.getRouteType());
        assertEquals(150, route.getTotalDistance());
    }

    @Test
    void builder_populatesStationIds() {
        CampaignTradeRoute route = createBuilderRoute();
        assertEquals(3, route.getStationIds().size());
        assertTrue(route.getStationIds().contains("station1"));
        assertTrue(route.getStationIds().contains("station2"));
        assertTrue(route.getStationIds().contains("station3"));
    }

    @Test
    void builder_populatesVehicleIds() {
        CampaignTradeRoute route = createBuilderRoute();
        assertEquals(1, route.getVehicleIds().size());
        assertTrue(route.getVehicleIds().contains("vehicle1"));
    }

    @Test
    void builder_populatesCargoTypes() {
        CampaignTradeRoute route = createBuilderRoute();
        assertEquals(2, route.getCargoTypes().size());
        assertTrue(route.getCargoTypes().contains(CargoType.GRAIN));
        assertTrue(route.getCargoTypes().contains(CargoType.STEEL));
    }

    @Test
    void builder_originAndDestinationFromStationIds() {
        CampaignTradeRoute route = createBuilderRoute();
        assertEquals("station1", route.getOriginId());
        assertEquals("station3", route.getDestinationId());
    }

    @Test
    void builder_activeDefaultsToTrue() {
        CampaignTradeRoute route = createBuilderRoute();
        assertTrue(route.isActive());
    }

    @Test
    void builder_totalsDefaultToZero() {
        CampaignTradeRoute route = createBuilderRoute();
        assertEquals(0, route.getTotalCargoDelivered());
        assertEquals(0, route.getTotalRevenue());
    }

    @Test
    void builder_staticMethod_returnsBuilder() {
        assertNotNull(CampaignTradeRoute.builder());
    }

    // ---- processTurn ----

    @Test
    void processTurn_returnsCargoValue() {
        CampaignTradeRoute route = createLegacyRoute();
        // GRAIN baseValue=6, cargoPerTurn=10
        long value = route.processTurn();
        assertEquals(60, value);
    }

    @Test
    void processTurn_incrementsTotalCargoDelivered() {
        CampaignTradeRoute route = createLegacyRoute();
        route.processTurn();
        assertEquals(10, route.getTotalCargoDelivered());
        route.processTurn();
        assertEquals(20, route.getTotalCargoDelivered());
    }

    @Test
    void processTurn_incrementsTotalRevenue() {
        CampaignTradeRoute route = createLegacyRoute();
        // revenuePerTurn=500, operatingCostPerTurn=200, net=300
        route.processTurn();
        assertEquals(300, route.getTotalRevenue());
        route.processTurn();
        assertEquals(600, route.getTotalRevenue());
    }

    @Test
    void processTurn_returnsZeroWhenInactive() {
        CampaignTradeRoute route = createLegacyRoute();
        route.setActive(false);
        assertEquals(0, route.processTurn());
    }

    @Test
    void processTurn_returnsZeroWhenCargoTypeIsNull() {
        CampaignTradeRoute route = createBuilderRoute();
        // Builder route has null cargoType for legacy field if cargoTypes list is used
        // Actually the builder sets cargoType from the first cargoType in the list
        // Let's test with a route that has null cargoType
        // The builder sets cargoType = builder.cargoTypes.isEmpty() ? null : builder.cargoTypes.get(0)
        // So createBuilderRoute has GRAIN as cargoType
        // We need a route with null cargoType
        CampaignTradeRoute emptyRoute = CampaignTradeRoute.builder()
                .name("Empty Route")
                .ownerCorporationId("corp1")
                .addStationId("s1")
                .build();
        assertEquals(0, emptyRoute.processTurn());
    }

    // ---- Route lifecycle ----

    @Test
    void suspend_setsInactive() {
        CampaignTradeRoute route = createBuilderRoute();
        route.suspend();
        assertFalse(route.isActive());
        assertEquals(CampaignTradeRoute.RouteStatus.SUSPENDED, route.getStatus());
    }

    @Test
    void close_setsInactive() {
        CampaignTradeRoute route = createBuilderRoute();
        route.close();
        assertFalse(route.isActive());
        assertEquals(CampaignTradeRoute.RouteStatus.CLOSED, route.getStatus());
    }

    @Test
    void reactivate_setsActive() {
        CampaignTradeRoute route = createBuilderRoute();
        route.suspend();
        assertFalse(route.isActive());
        route.reactivate();
        assertTrue(route.isActive());
        assertEquals(CampaignTradeRoute.RouteStatus.ACTIVE, route.getStatus());
    }

    @Test
    void reactivate_afterClose_setsActive() {
        CampaignTradeRoute route = createBuilderRoute();
        route.close();
        assertFalse(route.isActive());
        route.reactivate();
        assertTrue(route.isActive());
    }

    // ---- recordDelivery ----

    @Test
    void recordDelivery_incrementsCargoDeliveredToday() {
        CampaignTradeRoute route = createBuilderRoute();
        route.recordDelivery(CargoType.GRAIN, 50);
        assertEquals(50, route.getCargoDeliveredToday());
        route.recordDelivery(CargoType.STEEL, 30);
        assertEquals(80, route.getCargoDeliveredToday());
    }

    @Test
    void recordDelivery_incrementsDailyIncome() {
        CampaignTradeRoute route = createBuilderRoute();
        // GRAIN baseValue=6, amount=50, income=300
        route.recordDelivery(CargoType.GRAIN, 50);
        assertEquals(300, route.getDailyIncome());
    }

    @Test
    void recordDelivery_incrementsTotalIncome() {
        CampaignTradeRoute route = createBuilderRoute();
        route.recordDelivery(CargoType.GRAIN, 50);
        assertEquals(300, route.getTotalIncome());
        route.recordDelivery(CargoType.STEEL, 20);
        // STEEL baseValue=25, amount=20, income=500
        assertEquals(800, route.getTotalIncome());
    }

    // ---- Station accessors ----

    @Test
    void getOriginStationId_returnsFirstStation() {
        CampaignTradeRoute route = createBuilderRoute();
        assertEquals("station1", route.getOriginStationId());
    }

    @Test
    void getDestinationStationId_returnsLastStation() {
        CampaignTradeRoute route = createBuilderRoute();
        assertEquals("station3", route.getDestinationStationId());
    }

    @Test
    void getOriginStationId_returnsNullWhenEmpty() {
        CampaignTradeRoute route = CampaignTradeRoute.builder()
                .name("Empty")
                .ownerCorporationId("corp1")
                .build();
        assertNull(route.getOriginStationId());
    }

    @Test
    void getDestinationStationId_returnsNullWhenEmpty() {
        CampaignTradeRoute route = CampaignTradeRoute.builder()
                .name("Empty")
                .ownerCorporationId("corp1")
                .build();
        assertNull(route.getDestinationStationId());
    }

    // ---- Vehicle management ----

    @Test
    void addVehicle_addsToVehicleIds() {
        CampaignTradeRoute route = createBuilderRoute();
        route.addVehicle("vehicle2");
        assertTrue(route.getVehicleIds().contains("vehicle2"));
    }

    @Test
    void removeVehicle_removesFromVehicleIds() {
        CampaignTradeRoute route = createBuilderRoute();
        route.removeVehicle("vehicle1");
        assertFalse(route.getVehicleIds().contains("vehicle1"));
    }

    // ---- Count accessors ----

    @Test
    void getStationCount_returnsSize() {
        CampaignTradeRoute route = createBuilderRoute();
        assertEquals(3, route.getStationCount());
    }

    @Test
    void getVehicleCount_returnsSize() {
        CampaignTradeRoute route = createBuilderRoute();
        assertEquals(1, route.getVehicleCount());
    }

    // ---- getType alias ----

    @Test
    void getType_returnsRouteType() {
        CampaignTradeRoute route = createLegacyRoute();
        assertEquals(route.getRouteType(), route.getType());
    }

    // ---- getNetProfitPerTurn ----

    @Test
    void getNetProfitPerTurn_calculatesCorrectly() {
        CampaignTradeRoute route = createLegacyRoute();
        // revenuePerTurn=500, operatingCostPerTurn=200
        assertEquals(300, route.getNetProfitPerTurn());
    }

    // ---- Setters ----

    @Test
    void setVehicleId_setsValue() {
        CampaignTradeRoute route = createLegacyRoute();
        route.setVehicleId("truck1");
        assertEquals("truck1", route.getVehicleId());
    }

    @Test
    void setShipId_setsValue() {
        CampaignTradeRoute route = createLegacyRoute();
        route.setShipId("ship1");
        assertEquals("ship1", route.getShipId());
    }

    @Test
    void setActive_setsValue() {
        CampaignTradeRoute route = createLegacyRoute();
        route.setActive(false);
        assertFalse(route.isActive());
        route.setActive(true);
        assertTrue(route.isActive());
    }

    @Test
    void setDailyIncome_setsValue() {
        CampaignTradeRoute route = createBuilderRoute();
        route.setDailyIncome(1000);
        assertEquals(1000, route.getDailyIncome());
    }

    @Test
    void setTotalIncome_setsValue() {
        CampaignTradeRoute route = createBuilderRoute();
        route.setTotalIncome(5000);
        assertEquals(5000, route.getTotalIncome());
    }

    @Test
    void setCargoDeliveredToday_setsValue() {
        CampaignTradeRoute route = createBuilderRoute();
        route.setCargoDeliveredToday(100);
        assertEquals(100, route.getCargoDeliveredToday());
    }

    // ---- RouteType enum ----

    @Test
    void routeType_enumHasTwoValues() {
        assertEquals(2, CampaignTradeRoute.RouteType.values().length);
    }

    // ---- toString ----

    @Test
    void toString_containsKeyInfo() {
        CampaignTradeRoute route = createBuilderRoute();
        String str = route.toString();
        assertTrue(str.contains(route.getId()));
        assertTrue(str.contains("Grain Express"));
        assertTrue(str.contains("SURFACE"));
        assertTrue(str.contains("active=true"));
    }

    @Test
    void toString_legacyRoute_containsKeyInfo() {
        CampaignTradeRoute route = createLegacyRoute();
        String str = route.toString();
        assertTrue(str.contains(route.getId()));
        assertTrue(str.contains("SURFACE"));
    }
}
