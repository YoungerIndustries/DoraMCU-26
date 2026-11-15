package com.yisy.modules.doramcu.sfb.service;

import com.yisy.modules.doramcu.sfb.service.SfbEconomyService.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class SfbEconomyServiceTest {

    private SfbEconomyService service;

    @BeforeEach
    void setUp() {
        service = new SfbEconomyService();
    }

    @Nested
    class ResourceTypeTests {
        @Test
        void allTypes_haveNames() {
            for (ResourceType type : ResourceType.values()) {
                assertNotNull(type.getName());
                assertFalse(type.getName().isEmpty());
            }
        }

        @Test
        void resourceType_count() {
            assertEquals(5, ResourceType.values().length);
        }
    }

    @Nested
    class ShipClassCategoryTests {
        @Test
        void frigate_cheapest() {
            assertTrue(ShipClassCategory.FRIGATE.getCreditsCost() < ShipClassCategory.DESTROYER.getCreditsCost());
        }

        @Test
        void dreadnought_mostExpensive() {
            for (ShipClassCategory category : ShipClassCategory.values()) {
                if (category == ShipClassCategory.STATION) continue;
                assertTrue(ShipClassCategory.DREADNOUGHT.getCreditsCost() >= category.getCreditsCost());
            }
        }

        @Test
        void allCategories_haveCosts() {
            for (ShipClassCategory category : ShipClassCategory.values()) {
                assertTrue(category.getCreditsCost() > 0);
                assertTrue(category.getMaterialsCost() > 0);
                assertTrue(category.getFuelCost() > 0);
                assertTrue(category.getManpowerCost() > 0);
            }
        }
    }

    @Nested
    class ShipBuildCostTests {
        @Test
        void of_defaultMultiplier() {
            ShipBuildCost cost = ShipBuildCost.of(ShipClassCategory.DESTROYER);
            assertEquals(100, cost.getCredits());
            assertEquals(50, cost.getMaterials());
            assertEquals(20, cost.getFuel());
            assertEquals(10, cost.getManpower());
        }

        @Test
        void of_customMultiplier() {
            ShipBuildCost cost = ShipBuildCost.of(ShipClassCategory.DESTROYER, 1.5);
            assertEquals(150, cost.getCredits());
            assertEquals(75, cost.getMaterials());
        }

        @Test
        void totalCost() {
            ShipBuildCost cost = ShipBuildCost.of(ShipClassCategory.FRIGATE);
            assertEquals(50 + 25 + 10 + 5, cost.getTotalCost());
        }
    }

    @Nested
    class FactionEconomyTests {
        @Test
        void builder_defaultResources() {
            FactionEconomy econ = FactionEconomy.builder("Federation")
                    .credits(1000)
                    .build();
            assertEquals(1000, econ.getResource(ResourceType.CREDITS));
            assertEquals(0, econ.getResource(ResourceType.MATERIALS));
        }

        @Test
        void builder_allResources() {
            FactionEconomy econ = FactionEconomy.builder("Federation")
                    .credits(1000)
                    .materials(500)
                    .fuel(200)
                    .manpower(100)
                    .build();
            assertEquals(1000, econ.getResource(ResourceType.CREDITS));
            assertEquals(500, econ.getResource(ResourceType.MATERIALS));
            assertEquals(200, econ.getResource(ResourceType.FUEL));
            assertEquals(100, econ.getResource(ResourceType.MANPOWER));
        }

        @Test
        void builder_requiresName() {
            assertThrows(IllegalArgumentException.class, () ->
                    FactionEconomy.builder("").build());
        }

        @Test
        void canAfford_sufficient() {
            FactionEconomy econ = FactionEconomy.builder("Federation")
                    .credits(200)
                    .materials(100)
                    .fuel(50)
                    .manpower(30)
                    .build();
            assertTrue(econ.canAfford(ShipBuildCost.of(ShipClassCategory.DESTROYER)));
        }

        @Test
        void canAfford_insufficient() {
            FactionEconomy econ = FactionEconomy.builder("Federation")
                    .credits(50)
                    .build();
            assertFalse(econ.canAfford(ShipBuildCost.of(ShipClassCategory.DESTROYER)));
        }

        @Test
        void spendResources() {
            FactionEconomy econ = FactionEconomy.builder("Federation")
                    .credits(200)
                    .materials(100)
                    .fuel(50)
                    .manpower(30)
                    .build();
            econ.spendResources(ShipBuildCost.of(ShipClassCategory.DESTROYER));
            assertEquals(100, econ.getResource(ResourceType.CREDITS));
            assertEquals(50, econ.getResource(ResourceType.MATERIALS));
        }

        @Test
        void addResource_respectsMaxStorage() {
            FactionEconomy econ = FactionEconomy.builder("Federation")
                    .credits(500)
                    .maxStorage(ResourceType.CREDITS, 1000)
                    .build();
            econ.addResource(ResourceType.CREDITS, 600);
            assertEquals(1000, econ.getResource(ResourceType.CREDITS));
        }

        @Test
        void removeResource_noNegative() {
            FactionEconomy econ = FactionEconomy.builder("Federation")
                    .credits(50)
                    .build();
            econ.removeResource(ResourceType.CREDITS, 100);
            assertEquals(0, econ.getResource(ResourceType.CREDITS));
        }

        @Test
        void advanceTurn_addsIncome() {
            FactionEconomy econ = FactionEconomy.builder("Federation")
                    .credits(100)
                    .income(ResourceType.CREDITS, 50)
                    .build();
            econ.advanceTurn();
            assertEquals(150, econ.getResource(ResourceType.CREDITS));
            assertEquals(1, econ.getTurnsSinceLastTrade());
        }

        @Test
        void resources_immutable() {
            FactionEconomy econ = FactionEconomy.builder("Federation").build();
            assertThrows(UnsupportedOperationException.class, () ->
                    econ.getResources().put(ResourceType.CREDITS, 999));
        }
    }

    @Nested
    class TradeDealTests {
        @Test
        void builder_createsDeal() {
            TradeDeal deal = TradeDeal.builder()
                    .id("d1")
                    .factionA("Federation")
                    .factionB("Klingon")
                    .factionAGives(ResourceType.CREDITS, 100)
                    .factionBGives(ResourceType.MATERIALS, 50)
                    .build();
            assertEquals("d1", deal.getId());
            assertEquals(100, deal.getFactionAGives().get(ResourceType.CREDITS).intValue());
            assertEquals(50, deal.getFactionBGives().get(ResourceType.MATERIALS).intValue());
        }

        @Test
        void builder_generatesId() {
            TradeDeal deal = TradeDeal.builder()
                    .factionA("A")
                    .factionB("B")
                    .build();
            assertNotNull(deal.getId());
        }

        @Test
        void gives_immutable() {
            TradeDeal deal = TradeDeal.builder()
                    .factionA("A")
                    .factionB("B")
                    .build();
            assertThrows(UnsupportedOperationException.class, () ->
                    deal.getFactionAGives().put(ResourceType.CREDITS, 999));
        }
    }

    @Nested
    class EconomicRecommendationTests {
        @Test
        void builder_createsRecommendation() {
            EconomicRecommendation rec = EconomicRecommendation.builder("Federation")
                    .recommendation("Build ships")
                    .suggestedBuild(ShipClassCategory.DESTROYER)
                    .shipsAffordable(3)
                    .detail("key", "value")
                    .build();
            assertEquals("Federation", rec.getFactionName());
            assertEquals(1, rec.getRecommendations().size());
            assertEquals(ShipClassCategory.DESTROYER, rec.getSuggestedBuild());
            assertEquals(3, rec.getShipsAffordable());
        }

        @Test
        void details_immutable() {
            EconomicRecommendation rec = EconomicRecommendation.builder("Federation")
                    .detail("key", "value")
                    .build();
            assertThrows(UnsupportedOperationException.class, () ->
                    rec.getDetails().put("new", "value"));
        }
    }

    @Nested
    class EconomyRegistration {
        @Test
        void registerEconomy_object() {
            FactionEconomy econ = FactionEconomy.builder("Federation")
                    .credits(1000)
                    .build();
            service.registerEconomy(econ);
            assertEquals(1, service.getAllFactions().size());
            assertEquals(1000, service.getEconomy("Federation").getResource(ResourceType.CREDITS));
        }

        @Test
        void registerEconomy_convenience() {
            service.registerEconomy("Federation", 1000, 500, 200, 100);
            assertEquals(1000, service.getEconomy("Federation").getResource(ResourceType.CREDITS));
            assertEquals(500, service.getEconomy("Federation").getResource(ResourceType.MATERIALS));
        }

        @Test
        void getEconomy_notFound() {
            assertNull(service.getEconomy("Unknown"));
        }
    }

    @Nested
    class Shipbuilding {
        @BeforeEach
        void registerEconomy() {
            service.registerEconomy("Federation", 80, 30, 15, 8);
        }

        @Test
        void getBuildCost() {
            ShipBuildCost cost = service.getBuildCost(ShipClassCategory.DESTROYER);
            assertEquals(100, cost.getCredits());
        }

        @Test
        void getBuildCost_withMultiplier() {
            ShipBuildCost cost = service.getBuildCost(ShipClassCategory.DESTROYER, 2.0);
            assertEquals(200, cost.getCredits());
        }

        @Test
        void canAfford_true() {
            assertTrue(service.canAfford("Federation", ShipClassCategory.FRIGATE));
        }

        @Test
        void canAfford_false() {
            assertFalse(service.canAfford("Federation", ShipClassCategory.DESTROYER));
        }

        @Test
        void canAfford_notRegistered() {
            assertFalse(service.canAfford("Unknown", ShipClassCategory.FRIGATE));
        }

        @Test
        void buildShip_success() {
            assertTrue(service.buildShip("Federation", ShipClassCategory.FRIGATE));
            assertEquals(30, service.getEconomy("Federation").getResource(ResourceType.CREDITS));
        }

        @Test
        void buildShip_insufficient() {
            assertFalse(service.buildShip("Federation", ShipClassCategory.DESTROYER));
        }

        @Test
        void shipsAffordable() {
            int count = service.shipsAffordable("Federation", ShipClassCategory.FRIGATE);
            assertTrue(count > 0, "Should afford at least 1 frigate");
        }

        @Test
        void shipsAffordable_expensive() {
            int count = service.shipsAffordable("Federation", ShipClassCategory.DESTROYER);
            assertEquals(0, count);
        }

        @Test
        void shipsAffordable_notRegistered() {
            assertEquals(0, service.shipsAffordable("Unknown", ShipClassCategory.FRIGATE));
        }
    }

    @Nested
    class Trade {
        @BeforeEach
        void registerEconomies() {
            service.registerEconomy("Federation", 1000, 500, 200, 100);
            service.registerEconomy("Klingon", 500, 1000, 300, 200);
        }

        @Test
        void executeTrade_success() {
            TradeDeal deal = TradeDeal.builder()
                    .factionA("Federation")
                    .factionB("Klingon")
                    .factionAGives(ResourceType.CREDITS, 100)
                    .factionBGives(ResourceType.MATERIALS, 200)
                    .build();
            SfbDiplomacyService diplomacy = new SfbDiplomacyService();
            assertTrue(service.executeTrade(deal, diplomacy));
            // Federation: 1000 - 100 (gives) + 100 (income) = 1000 credits
            // Federation: 500 + 200 (receives) + 50 (income) = 750 materials
            // Klingon: 1000 - 200 (gives) + 50 (income) = 850 materials
            assertEquals(1000, service.getEconomy("Federation").getResource(ResourceType.CREDITS));
            assertEquals(750, service.getEconomy("Federation").getResource(ResourceType.MATERIALS));
        }

        @Test
        void executeTrade_insufficient() {
            TradeDeal deal = TradeDeal.builder()
                    .factionA("Federation")
                    .factionB("Klingon")
                    .factionAGives(ResourceType.CREDITS, 2000)
                    .build();
            SfbDiplomacyService diplomacy = new SfbDiplomacyService();
            assertFalse(service.executeTrade(deal, diplomacy));
        }

        @Test
        void executeTrade_notRegistered() {
            TradeDeal deal = TradeDeal.builder()
                    .factionA("Unknown")
                    .factionB("Federation")
                    .build();
            SfbDiplomacyService diplomacy = new SfbDiplomacyService();
            assertFalse(service.executeTrade(deal, diplomacy));
        }
    }

    @Nested
    class Recommendations {
        @Test
        void recommendations_lowResources() {
            service.registerEconomy("Federation", 50, 20, 10, 5);
            EconomicRecommendation rec = service.getRecommendations("Federation");
            assertTrue(rec.getRecommendations().size() > 0);
        }

        @Test
        void recommendations_strongEconomy() {
            service.registerEconomy("Federation", 2000, 1000, 500, 200);
            EconomicRecommendation rec = service.getRecommendations("Federation");
            assertTrue(rec.getRecommendations().stream()
                    .anyMatch(r -> r.contains("fleet expansion")));
        }

        @Test
        void recommendations_notRegistered() {
            EconomicRecommendation rec = service.getRecommendations("Unknown");
            assertEquals(1, rec.getRecommendations().size());
        }
    }

    @Nested
    class TurnAdvancement {
        @Test
        void advanceTurn_allEconomies() {
            service.registerEconomy("Federation", 100, 50, 20, 10);
            service.registerEconomy("Klingon", 200, 100, 50, 30);
            service.advanceTurn();
            // Default income: 100 credits, 50 materials, 30 fuel, 20 manpower
            assertTrue(service.getEconomy("Federation").getResource(ResourceType.CREDITS) > 100);
        }
    }

    @Nested
    class EconomyStats {
        @Test
        void stats_singleFaction() {
            service.registerEconomy("Federation", 1000, 500, 200, 100);
            Map<String, Object> stats = service.getEconomyStats("Federation");
            assertEquals("Federation", stats.get("faction"));
            assertNotNull(stats.get("resources"));
        }

        @Test
        void stats_notFound() {
            Map<String, Object> stats = service.getEconomyStats("Unknown");
            assertEquals("Faction economy not found", stats.get("error"));
        }

        @Test
        void stats_allFactions() {
            service.registerEconomy("Federation", 1000, 500, 200, 100);
            service.registerEconomy("Klingon", 500, 200, 100, 50);
            Map<String, Map<String, Object>> all = service.getAllEconomyStats();
            assertEquals(2, all.size());
        }
    }

    @Nested
    class Clear {
        @Test
        void clear_removesAll() {
            service.registerEconomy("Federation", 1000, 500, 200, 100);
            service.clear();
            assertTrue(service.getAllFactions().isEmpty());
        }
    }
}
