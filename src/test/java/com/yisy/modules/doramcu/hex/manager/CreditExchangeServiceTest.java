package com.yisy.modules.doramcu.hex.manager;

import com.yisy.modules.doramcu.hex.Player;
import com.yisy.modules.doramcu.hex.RefinedResourceType;
import com.yisy.modules.doramcu.hex.ResourcePipelineManager;
import com.yisy.modules.doramcu.hex.ResourceStorage;
import com.yisy.modules.doramcu.hex.manager.GameContext;
import com.yisy.modules.doramcu.traveller.Commodity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CreditExchangeService Tests")
class CreditExchangeServiceTest {

    private CreditExchangeService service;
    private ResourcePipelineManager pipeline;
    private EconomicState economicState;

    private GameContext makeContext() {
        return new GameContext() {
            @Override public String getName() { return "Test"; }
            @Override public List<Player> getPlayers() { return Collections.emptyList(); }
            @Override public int getCurrentTurn() { return 0; }
            @Override public long getCurrentTime() { return 0; }
            @Override public long getTurnDuration() { return 60; }
            @Override public int getCurrentPlayerIndex() { return 0; }
            @Override public void setCurrentPlayerIndex(int index) {}
            @Override public Player getCurrentPlayer() { return null; }
            @Override public boolean isGameEnded() { return false; }
            @Override public void setGameEnded(boolean ended) {}
            @Override public void incrementTurn() {}
            @Override public Random getRandom() { return new Random(42); }
            @Override public int getActivePlayerCount() { return 0; }
            @Override public Player addPlayer(Player player) { return player; }
            @Override public Player getPlayer(int playerId) { return null; }
            @Override public Map<Integer, com.yisy.modules.doramcu.hex.Planet> getPlanets() { return new HashMap<>(); }
            @Override public com.yisy.modules.doramcu.hex.Planet getPlanet(int planetId) { return null; }
            @Override public Map<Integer, com.yisy.modules.doramcu.hex.Fleet> getFleets() { return new HashMap<>(); }
            @Override public com.yisy.modules.doramcu.hex.Fleet getFleet(int fleetId) { return null; }
            @Override public com.yisy.modules.doramcu.hex.Fleet addFleet(com.yisy.modules.doramcu.hex.Fleet fleet, Player owner) { return fleet; }
            @Override public Map<Integer, com.yisy.modules.doramcu.hex.Ship> getShips() { return new HashMap<>(); }
            @Override public com.yisy.modules.doramcu.hex.Ship getShip(int shipId) { return null; }
            @Override public com.yisy.modules.doramcu.hex.Ship addShip(com.yisy.modules.doramcu.hex.Ship ship) { return ship; }
            @Override public int getNextShipIdAndIncrement() { return 1; }
            @Override public Map<Integer, com.yisy.modules.doramcu.hex.Galaxy> getGalaxies() { return new HashMap<>(); }
            @Override public com.yisy.modules.doramcu.hex.Galaxy getGalaxy(int galaxyId) { return null; }
            @Override public com.yisy.modules.doramcu.hex.Galaxy addGalaxy(com.yisy.modules.doramcu.hex.Galaxy galaxy) { return galaxy; }
            @Override public Map<Integer, com.yisy.modules.doramcu.hex.SolarSystem> getSolarSystems() { return new HashMap<>(); }
            @Override public Map<Integer, com.yisy.modules.doramcu.hex.Battle> getBattles() { return new HashMap<>(); }
            @Override public com.yisy.modules.doramcu.hex.Battle getBattle(int battleId) { return null; }
            @Override public com.yisy.modules.doramcu.hex.Battle addBattle(com.yisy.modules.doramcu.hex.Battle battle) { return battle; }
            @Override public int getNextBattleIdAndIncrement() { return 1; }
            @Override public int getNextPlanetIdAndIncrement() { return 1; }
            @Override public int getNextFleetIdAndIncrement() { return 1; }
            @Override public int getNextSolarSystemIdAndIncrement() { return 1; }
            @Override public List<com.yisy.modules.doramcu.hex.SpaceStation> getSpaceStations() { return Collections.emptyList(); }
            @Override public Map<Integer, com.yisy.modules.doramcu.hex.Empire> getEmpires() { return new HashMap<>(); }
        };
    }

    @BeforeEach
    void setUp() {
        service = new CreditExchangeService(makeContext());
        pipeline = new ResourcePipelineManager(1);
        economicState = new EconomicState(1, pipeline);
        service.registerEconomicState(economicState);
    }

    // ============================================================
    // Commodity to Resource Mapping
    // ============================================================

    @Nested
    @DisplayName("mapCommodityToResource")
    class MapCommodityToResource {

        @Test
        @DisplayName("Food maps to Nutrient")
        void foodToNutrient() {
            assertEquals(RefinedResourceType.NUTRIENT, service.mapCommodityToResource(Commodity.FOOD));
        }

        @Test
        @DisplayName("Medicine maps to Nutrient")
        void medicineToNutrient() {
            assertEquals(RefinedResourceType.NUTRIENT, service.mapCommodityToResource(Commodity.MEDICINE));
        }

        @Test
        @DisplayName("Fuel maps to Fuel")
        void fuelToFuel() {
            assertEquals(RefinedResourceType.FUEL, service.mapCommodityToResource(Commodity.FUEL));
        }

        @Test
        @DisplayName("Chemicals maps to Fuel")
        void chemicalsToFuel() {
            assertEquals(RefinedResourceType.FUEL, service.mapCommodityToResource(Commodity.CHEMICALS));
        }

        @Test
        @DisplayName("Electronics maps to PowerCell")
        void electronicsToPowerCell() {
            assertEquals(RefinedResourceType.POWER_CELL, service.mapCommodityToResource(Commodity.ELECTRONICS));
        }

        @Test
        @DisplayName("Information maps to PowerCell")
        void informationToPowerCell() {
            assertEquals(RefinedResourceType.POWER_CELL, service.mapCommodityToResource(Commodity.INFORMATION));
        }

        @Test
        @DisplayName("Textiles maps to Composite")
        void textilesToComposite() {
            assertEquals(RefinedResourceType.COMPOSITE, service.mapCommodityToResource(Commodity.TEXTILES));
        }

        @Test
        @DisplayName("Metals maps to MetalAlloy")
        void metalsToMetalAlloy() {
            assertEquals(RefinedResourceType.METAL_ALLOY, service.mapCommodityToResource(Commodity.METALS));
        }

        @Test
        @DisplayName("Machinery maps to MetalAlloy")
        void machineryToMetalAlloy() {
            assertEquals(RefinedResourceType.METAL_ALLOY, service.mapCommodityToResource(Commodity.MACHINERY));
        }

        @Test
        @DisplayName("Null commodity maps to MetalAlloy")
        void nullCommodityToMetalAlloy() {
            assertEquals(RefinedResourceType.METAL_ALLOY, service.mapCommodityToResource(null));
        }

        @Test
        @DisplayName("All commodities map to a resource type")
        void allCommoditiesMapped() {
            for (Commodity comm : Commodity.values()) {
                assertNotNull(service.mapCommodityToResource(comm),
                        "Commodity " + comm + " should map to a resource type");
            }
        }
    }

    // ============================================================
    // Resource to Commodity Mapping
    // ============================================================

    @Nested
    @DisplayName("mapResourceToCommodity")
    class MapResourceToCommodity {

        @Test
        @DisplayName("Nutrient maps to Food")
        void nutrientToFood() {
            assertEquals(Commodity.FOOD, service.mapResourceToCommodity(RefinedResourceType.NUTRIENT));
        }

        @Test
        @DisplayName("Fuel maps to Fuel")
        void fuelToFuel() {
            assertEquals(Commodity.FUEL, service.mapResourceToCommodity(RefinedResourceType.FUEL));
        }

        @Test
        @DisplayName("PowerCell maps to Electronics")
        void powerCellToElectronics() {
            assertEquals(Commodity.ELECTRONICS, service.mapResourceToCommodity(RefinedResourceType.POWER_CELL));
        }

        @Test
        @DisplayName("Composite maps to Textiles")
        void compositeToTextiles() {
            assertEquals(Commodity.TEXTILES, service.mapResourceToCommodity(RefinedResourceType.COMPOSITE));
        }

        @Test
        @DisplayName("MetalAlloy maps to Metals")
        void metalAlloyToMetals() {
            assertEquals(Commodity.METALS, service.mapResourceToCommodity(RefinedResourceType.METAL_ALLOY));
        }

        @Test
        @DisplayName("Null resource maps to Metals")
        void nullResourceToMetals() {
            assertEquals(Commodity.METALS, service.mapResourceToCommodity(null));
        }
    }

    // ============================================================
    // Credit to Resource Conversion
    // ============================================================

    @Nested
    @DisplayName("convertCreditsToResources")
    class ConvertCreditsToResources {

        @Test
        @DisplayName("Convert credits to resources with normal rate")
        void normalConversion() {
            int added = service.convertCreditsToResources(pipeline, 100, Commodity.FOOD);
            assertTrue(added > 0, "Should convert credits to resources");
            assertEquals(added, pipeline.getRefinedStorage().getAmount(RefinedResourceType.NUTRIENT));
        }

        @Test
        @DisplayName("Zero credits returns zero")
        void zeroCredits() {
            assertEquals(0, service.convertCreditsToResources(pipeline, 0, Commodity.FOOD));
        }

        @Test
        @DisplayName("Negative credits returns zero")
        void negativeCredits() {
            assertEquals(0, service.convertCreditsToResources(pipeline, -100, Commodity.FOOD));
        }

        @Test
        @DisplayName("Null pipeline returns zero")
        void nullPipeline() {
            assertEquals(0, service.convertCreditsToResources(null, 100, Commodity.FOOD));
        }

        @Test
        @DisplayName("Null commodity returns zero")
        void nullCommodity() {
            assertEquals(0, service.convertCreditsToResources(pipeline, 100, null));
        }

        @Test
        @DisplayName("Conversion respects storage capacity")
        void respectsCapacity() {
            ResourceStorage<RefinedResourceType> storage = pipeline.getRefinedStorage();
            int capacity = storage.getCapacity(RefinedResourceType.NUTRIENT);
            int added = service.convertCreditsToResources(pipeline, Long.MAX_VALUE, Commodity.FOOD);
            assertTrue(added <= capacity, "Should not exceed storage capacity");
        }

        @Test
        @DisplayName("Tracks resource conversion per empire")
        void tracksConversion() {
            service.convertCreditsToResources(pipeline, 100, Commodity.FOOD);
            Map<String, Object> summary = service.getExchangeSummary(1);
            @SuppressWarnings("unchecked")
            Map<RefinedResourceType, Integer> conversions =
                    (Map<RefinedResourceType, Integer>) summary.get("resourceConversions");
            assertTrue(conversions.containsKey(RefinedResourceType.NUTRIENT));
            assertTrue(conversions.get(RefinedResourceType.NUTRIENT) > 0);
        }
    }

    // ============================================================
    // Resource to Credit Conversion
    // ============================================================

    @Nested
    @DisplayName("convertResourcesToCredits")
    class ConvertResourcesToCredits {

        @Test
        @DisplayName("Convert resources to credits")
        void normalConversion() {
            pipeline.getRefinedStorage().add(RefinedResourceType.NUTRIENT, 100);
            long credits = service.convertResourcesToCredits(pipeline, RefinedResourceType.NUTRIENT, 50);
            assertTrue(credits > 0, "Should convert resources to credits");
            assertEquals(50, pipeline.getRefinedStorage().getAmount(RefinedResourceType.NUTRIENT));
        }

        @Test
        @DisplayName("Zero amount returns zero")
        void zeroAmount() {
            pipeline.getRefinedStorage().add(RefinedResourceType.NUTRIENT, 100);
            assertEquals(0, service.convertResourcesToCredits(pipeline, RefinedResourceType.NUTRIENT, 0));
        }

        @Test
        @DisplayName("Null pipeline returns zero")
        void nullPipeline() {
            assertEquals(0, service.convertResourcesToCredits(null, RefinedResourceType.NUTRIENT, 50));
        }

        @Test
        @DisplayName("Null resource type returns zero")
        void nullResourceType() {
            assertEquals(0, service.convertResourcesToCredits(pipeline, null, 50));
        }

        @Test
        @DisplayName("Cannot convert more than available")
        void limitedByAvailable() {
            pipeline.getRefinedStorage().add(RefinedResourceType.NUTRIENT, 30);
            long credits = service.convertResourcesToCredits(pipeline, RefinedResourceType.NUTRIENT, 100);
            assertTrue(credits > 0);
            assertEquals(0, pipeline.getRefinedStorage().getAmount(RefinedResourceType.NUTRIENT));
        }

        @Test
        @DisplayName("Empty storage returns zero")
        void emptyStorage() {
            assertEquals(0, service.convertResourcesToCredits(pipeline, RefinedResourceType.NUTRIENT, 50));
        }
    }

    // ============================================================
    // Exchange Rate Calculations
    // ============================================================

    @Nested
    @DisplayName("calculateCreditToResourceRate")
    class CalculateCreditToResourceRate {

        @Test
        @DisplayName("Normal rate when storage is at 50% capacity")
        void normalRate() {
            ResourceStorage<RefinedResourceType> storage = pipeline.getRefinedStorage();
            int capacity = storage.getCapacity(RefinedResourceType.NUTRIENT);
            storage.add(RefinedResourceType.NUTRIENT, capacity / 2);
            double rate = service.calculateCreditToResourceRate(storage, RefinedResourceType.NUTRIENT);
            assertEquals(1.0, rate, 0.01, "Rate should be 1.0 at 50% capacity");
        }

        @Test
        @DisplayName("Reduced rate when resource is scarce")
        void scarcityPremium() {
            ResourceStorage<RefinedResourceType> storage = pipeline.getRefinedStorage();
            // Empty storage — maximum scarcity
            double rate = service.calculateCreditToResourceRate(storage, RefinedResourceType.NUTRIENT);
            assertTrue(rate < 1.0, "Rate should be reduced when resource is scarce");
        }

        @Test
        @DisplayName("Increased rate when resource is in surplus")
        void surplusDiscount() {
            ResourceStorage<RefinedResourceType> storage = pipeline.getRefinedStorage();
            int capacity = storage.getCapacity(RefinedResourceType.NUTRIENT);
            storage.add(RefinedResourceType.NUTRIENT, capacity - 1);
            double rate = service.calculateCreditToResourceRate(storage, RefinedResourceType.NUTRIENT);
            assertTrue(rate > 1.0, "Rate should be increased when resource is in surplus");
        }
    }

    @Nested
    @DisplayName("calculateResourceToCreditRate")
    class CalculateResourceToCreditRate {

        @Test
        @DisplayName("Normal rate when storage is at 50% capacity")
        void normalRate() {
            ResourceStorage<RefinedResourceType> storage = pipeline.getRefinedStorage();
            int capacity = storage.getCapacity(RefinedResourceType.NUTRIENT);
            storage.add(RefinedResourceType.NUTRIENT, capacity / 2);
            double rate = service.calculateResourceToCreditRate(storage, RefinedResourceType.NUTRIENT);
            assertEquals(1.0, rate, 0.01, "Rate should be 1.0 at 50% capacity");
        }

        @Test
        @DisplayName("Higher rate when resource is scarce")
        void scarcityPremium() {
            ResourceStorage<RefinedResourceType> storage = pipeline.getRefinedStorage();
            double rate = service.calculateResourceToCreditRate(storage, RefinedResourceType.NUTRIENT);
            assertTrue(rate > 1.0, "Rate should be higher when resource is scarce");
        }

        @Test
        @DisplayName("Lower rate when resource is in surplus")
        void surplusDiscount() {
            ResourceStorage<RefinedResourceType> storage = pipeline.getRefinedStorage();
            int capacity = storage.getCapacity(RefinedResourceType.NUTRIENT);
            storage.add(RefinedResourceType.NUTRIENT, capacity - 1);
            double rate = service.calculateResourceToCreditRate(storage, RefinedResourceType.NUTRIENT);
            assertTrue(rate < 1.0, "Rate should be lower when resource is in surplus");
        }
    }

    // ============================================================
    // Empire Credit Balance
    // ============================================================

    @Nested
    @DisplayName("Empire Credit Balance")
    class EmpireCreditBalance {

        @Test
        @DisplayName("Add credits increases balance")
        void addCredits() {
            service.addTradeCredits(1, 1000);
            assertEquals(1000, service.getTradeCreditBalance(1));
        }

        @Test
        @DisplayName("Spend credits decreases balance")
        void spendCredits() {
            service.addTradeCredits(1, 1000);
            assertTrue(service.spendTradeCredits(1, 500));
            assertEquals(500, service.getTradeCreditBalance(1));
        }

        @Test
        @DisplayName("Spend more than balance returns false")
        void spendMoreThanBalance() {
            service.addTradeCredits(1, 100);
            assertFalse(service.spendTradeCredits(1, 200));
            assertEquals(100, service.getTradeCreditBalance(1));
        }

        @Test
        @DisplayName("Unregistered empire has zero balance")
        void unregisteredEmpire() {
            assertEquals(0, service.getTradeCreditBalance(999));
        }

        @Test
        @DisplayName("Add credits to unregistered empire is no-op")
        void addToUnregistered() {
            service.addTradeCredits(999, 1000);
            assertEquals(0, service.getTradeCreditBalance(999));
        }

        @Test
        @DisplayName("Multiple adds accumulate")
        void multipleAdds() {
            service.addTradeCredits(1, 100);
            service.addTradeCredits(1, 200);
            service.addTradeCredits(1, 300);
            assertEquals(600, service.getTradeCreditBalance(1));
        }

        @Test
        @DisplayName("Spend returns false for unregistered empire")
        void spendUnregistered() {
            assertFalse(service.spendTradeCredits(999, 100));
        }
    }

    // ============================================================
    // Exchange Summary
    // ============================================================

    @Nested
    @DisplayName("Exchange Summary")
    class ExchangeSummary {

        @Test
        @DisplayName("Summary contains empire ID and balance")
        void summaryContainsFields() {
            service.addTradeCredits(1, 500);
            Map<String, Object> summary = service.getExchangeSummary(1);
            assertEquals(1, summary.get("empireId"));
            assertEquals(500L, summary.get("tradeCredits"));
            assertNotNull(summary.get("resourceConversions"));
        }

        @Test
        @DisplayName("Summary shows resource conversions")
        void summaryShowsConversions() {
            service.convertCreditsToResources(pipeline, 100, Commodity.FOOD);
            service.convertCreditsToResources(pipeline, 50, Commodity.FUEL);
            Map<String, Object> summary = service.getExchangeSummary(1);
            @SuppressWarnings("unchecked")
            Map<RefinedResourceType, Integer> conversions =
                    (Map<RefinedResourceType, Integer>) summary.get("resourceConversions");
            assertTrue(conversions.containsKey(RefinedResourceType.NUTRIENT));
            assertTrue(conversions.containsKey(RefinedResourceType.FUEL));
        }

        @Test
        @DisplayName("All exchange summaries returns list")
        void allExchangeSummaries() {
            service.addTradeCredits(1, 100);
            EconomicState state2 = new EconomicState(2, new ResourcePipelineManager(2));
            service.registerEconomicState(state2);
            service.addTradeCredits(2, 200);

            List<Map<String, Object>> summaries = service.getAllExchangeSummaries();
            assertEquals(2, summaries.size());
        }

        @Test
        @DisplayName("All exchange summaries empty when no states")
        void allExchangeSummariesEmpty() {
            CreditExchangeService emptyService = new CreditExchangeService(makeContext());
            assertTrue(emptyService.getAllExchangeSummaries().isEmpty());
        }
    }

    // ============================================================
    // EconomicState Registration
    // ============================================================

    @Nested
    @DisplayName("EconomicState Registration")
    class EconomicStateRegistration {

        @Test
        @DisplayName("Register and retrieve economic state")
        void registerAndRetrieve() {
            assertNotNull(service.getEconomicState(1));
            assertEquals(economicState, service.getEconomicState(1));
        }

        @Test
        @DisplayName("Unregistered state returns null")
        void unregisteredReturnsNull() {
            assertNull(service.getEconomicState(999));
        }

        @Test
        @DisplayName("Null state is ignored")
        void nullStateIgnored() {
            service.registerEconomicState(null);
            assertNull(service.getEconomicState(999));
        }
    }

    // ============================================================
    // Integration Scenarios
    // ============================================================

    @Nested
    @DisplayName("Integration Scenarios")
    class IntegrationScenarios {

        @Test
        @DisplayName("Full credit-to-resource-to-credit round-trip")
        void roundTripConversion() {
            // Add resources first
            pipeline.getRefinedStorage().add(RefinedResourceType.NUTRIENT, 1000);

            // Convert resources to credits
            long credits = service.convertResourcesToCredits(pipeline, RefinedResourceType.NUTRIENT, 100);
            assertTrue(credits > 0);

            // Add credits to balance
            service.addTradeCredits(1, credits);
            assertEquals(credits, service.getTradeCreditBalance(1));

            // Spend credits
            assertTrue(service.spendTradeCredits(1, credits / 2));
            assertEquals(credits / 2, service.getTradeCreditBalance(1));
        }

        @Test
        @DisplayName("Multi-empire credit isolation")
        void multiEmpireIsolation() {
            EconomicState state2 = new EconomicState(2, new ResourcePipelineManager(2));
            service.registerEconomicState(state2);

            service.addTradeCredits(1, 1000);
            service.addTradeCredits(2, 2000);

            assertEquals(1000, service.getTradeCreditBalance(1));
            assertEquals(2000, service.getTradeCreditBalance(2));

            service.spendTradeCredits(1, 500);
            assertEquals(500, service.getTradeCreditBalance(1));
            assertEquals(2000, service.getTradeCreditBalance(2));
        }

        @Test
        @DisplayName("Scarcity affects conversion rate")
        void scarcityAffectsRate() {
            // Empty storage — scarce
            double scarceRate = service.calculateCreditToResourceRate(
                    pipeline.getRefinedStorage(), RefinedResourceType.NUTRIENT);

            // Fill storage — surplus
            int capacity = pipeline.getRefinedStorage().getCapacity(RefinedResourceType.NUTRIENT);
            pipeline.getRefinedStorage().add(RefinedResourceType.NUTRIENT, capacity - 1);
            double surplusRate = service.calculateCreditToResourceRate(
                    pipeline.getRefinedStorage(), RefinedResourceType.NUTRIENT);

            assertTrue(surplusRate > scarceRate,
                    "Surplus rate should be higher than scarce rate");
        }
    }
}
