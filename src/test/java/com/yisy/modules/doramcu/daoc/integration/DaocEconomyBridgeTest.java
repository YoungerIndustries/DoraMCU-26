package com.yisy.modules.doramcu.daoc.integration;

import com.yisy.modules.doramcu.fourx.FourxColony;
import com.yisy.modules.doramcu.fourx.FourxTradeRoute;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("DaocEconomyBridge")
class DaocEconomyBridgeTest {

    private DaocEconomyBridge bridge;

    @BeforeEach
    void setUp() {
        bridge = new DaocEconomyBridge();
    }

    @Nested
    @DisplayName("Economy Summary")
    class EconomySummary {

        @Test
        @DisplayName("returns economy summary for null faction")
        void nullFactionSummary() {
            Map<String, Object> summary = bridge.calculateEconomySummary(null);

            assertNotNull(summary);
            assertEquals("daoc", summary.get("module"));
            assertEquals("Gold", summary.get("currency"));
            assertEquals("General Goods", summary.get("primaryResource"));
            assertEquals(1.0, summary.get("realmMultiplier"));
        }

        @Test
        @DisplayName("Albion has highest base gold")
        void albionHasHighestGold() {
            Map<String, Object> albion = bridge.calculateEconomySummary("albion");
            Map<String, Object> hibernia = bridge.calculateEconomySummary("hibernia");
            Map<String, Object> midgard = bridge.calculateEconomySummary("midgard");

            assertTrue((int) albion.get("gold") > (int) hibernia.get("gold"));
            assertTrue((int) albion.get("gold") > (int) midgard.get("gold"));
        }

        @Test
        @DisplayName("Albion economy has correct values")
        void albionEconomyValues() {
            Map<String, Object> summary = bridge.calculateEconomySummary("albion");

            assertEquals("Arcane Reagents", summary.get("primaryResource"));
            assertEquals(1.2, summary.get("realmMultiplier"));
            assertEquals(12000, summary.get("gold")); // 10000 * 1.2
            assertEquals(21600, summary.get("silver")); // 18000 * 1.2
            assertEquals(216000, summary.get("copper")); // 18000 * 10 * 1.2
            assertEquals(10200, summary.get("income")); // 8500 * 1.2
            assertEquals(6700, summary.get("netIncome")); // 10200 - 3500
        }

        @Test
        @DisplayName("Hibernia economy has correct values")
        void hiberniaEconomyValues() {
            Map<String, Object> summary = bridge.calculateEconomySummary("hibernia");

            assertEquals("Enchanted Herbs", summary.get("primaryResource"));
            assertEquals(1.0, summary.get("realmMultiplier"));
            assertEquals(7000, summary.get("gold"));
            assertEquals(14000, summary.get("silver"));
            assertEquals(140000, summary.get("copper"));
            assertEquals(6500, summary.get("income"));
            assertEquals(3000, summary.get("netIncome")); // 6500 - 3500
        }

        @Test
        @DisplayName("Midgard economy has correct values")
        void midgardEconomyValues() {
            Map<String, Object> summary = bridge.calculateEconomySummary("midgard");

            assertEquals("Runic Stones", summary.get("primaryResource"));
            assertEquals(1.1, summary.get("realmMultiplier"));
            assertEquals(9900, summary.get("gold")); // 9000 * 1.1
            assertEquals(17600, summary.get("silver")); // 16000 * 1.1
            assertEquals(176000, summary.get("copper")); // 16000 * 10 * 1.1
            assertEquals(8250, summary.get("income")); // 7500 * 1.1
            assertEquals(4750, summary.get("netIncome")); // 8250 - 3500
        }

        @Test
        @DisplayName("unknown faction uses default values")
        void unknownFactionUsesDefaults() {
            Map<String, Object> summary = bridge.calculateEconomySummary("unknown");

            assertEquals("General Goods", summary.get("primaryResource"));
            assertEquals(1.0, summary.get("realmMultiplier"));
        }

        @Test
        @DisplayName("faction ID is case insensitive")
        void factionIdCaseInsensitive() {
            Map<String, Object> lower = bridge.calculateEconomySummary("albion");
            Map<String, Object> upper = bridge.calculateEconomySummary("ALBION");

            assertEquals(lower.get("gold"), upper.get("gold"));
            assertEquals(lower.get("primaryResource"), upper.get("primaryResource"));
        }

        @Test
        @DisplayName("expenses are constant across realms")
        void expensesConstant() {
            assertEquals(3500, bridge.calculateEconomySummary("albion").get("expenses"));
            assertEquals(3500, bridge.calculateEconomySummary("hibernia").get("expenses"));
            assertEquals(3500, bridge.calculateEconomySummary("midgard").get("expenses"));
        }

        @Test
        @DisplayName("net income is positive for all realms")
        void netIncomePositive() {
            assertTrue((int) bridge.calculateEconomySummary("albion").get("netIncome") > 0);
            assertTrue((int) bridge.calculateEconomySummary("hibernia").get("netIncome") > 0);
            assertTrue((int) bridge.calculateEconomySummary("midgard").get("netIncome") > 0);
        }
    }

    @Nested
    @DisplayName("Realm Resources")
    class RealmResources {

        @Test
        @DisplayName("Albion primary resource is Arcane Reagents")
        void albionResource() {
            assertEquals("Arcane Reagents", bridge.getRealmResource("albion"));
        }

        @Test
        @DisplayName("Hibernia primary resource is Enchanted Herbs")
        void hiberniaResource() {
            assertEquals("Enchanted Herbs", bridge.getRealmResource("hibernia"));
        }

        @Test
        @DisplayName("Midgard primary resource is Runic Stones")
        void midgardResource() {
            assertEquals("Runic Stones", bridge.getRealmResource("midgard"));
        }

        @Test
        @DisplayName("unknown realm returns General Goods")
        void unknownRealmResource() {
            assertEquals("General Goods", bridge.getRealmResource("unknown"));
        }

        @Test
        @DisplayName("null realm returns General Goods")
        void nullRealmResource() {
            assertEquals("General Goods", bridge.getRealmResource(null));
        }

        @Test
        @DisplayName("resource lookup is case insensitive")
        void resourceLookupCaseInsensitive() {
            assertEquals(bridge.getRealmResource("albion"), bridge.getRealmResource("Albion"));
            assertEquals(bridge.getRealmResource("hibernia"), bridge.getRealmResource("HIBERNIA"));
            assertEquals(bridge.getRealmResource("midgard"), bridge.getRealmResource("Midgard"));
        }
    }

    @Nested
    @DisplayName("Realm Multipliers")
    class RealmMultipliers {

        @Test
        @DisplayName("Albion has highest multiplier")
        void albionHighestMultiplier() {
            assertEquals(1.2, bridge.getRealmMultiplier("albion"));
        }

        @Test
        @DisplayName("Hibernia has base multiplier")
        void hiberniaBaseMultiplier() {
            assertEquals(1.0, bridge.getRealmMultiplier("hibernia"));
        }

        @Test
        @DisplayName("Midgard has intermediate multiplier")
        void midgardIntermediateMultiplier() {
            assertEquals(1.1, bridge.getRealmMultiplier("midgard"));
        }

        @Test
        @DisplayName("unknown realm has base multiplier")
        void unknownRealmBaseMultiplier() {
            assertEquals(1.0, bridge.getRealmMultiplier("unknown"));
        }

        @Test
        @DisplayName("null realm has base multiplier")
        void nullRealmBaseMultiplier() {
            assertEquals(1.0, bridge.getRealmMultiplier(null));
        }

        @Test
        @DisplayName("multiplier lookup is case insensitive")
        void multiplierLookupCaseInsensitive() {
            assertEquals(bridge.getRealmMultiplier("albion"), bridge.getRealmMultiplier("Albion"));
        }
    }

    @Nested
    @DisplayName("Colonies")
    class Colonies {

        @Test
        @DisplayName("creates a colony")
        void createColony() {
            FourxColony colony = bridge.createColony("albion", "Camelot", "frontier-1");

            assertNotNull(colony);
            assertEquals("Camelot", colony.getName());
            assertEquals("albion", colony.getOwnerFactionId());
            assertEquals("frontier-1", colony.getStarSystemId());
        }

        @Test
        @DisplayName("colony has generated ID")
        void colonyHasGeneratedId() {
            FourxColony colony = bridge.createColony("albion", "Camelot", "frontier-1");

            assertNotNull(colony.getId());
            assertTrue(colony.getId().startsWith("system-"));
        }

        @Test
        @DisplayName("retrieves colony by ID")
        void getColonyById() {
            FourxColony created = bridge.createColony("albion", "Camelot", "frontier-1");
            FourxColony retrieved = bridge.getColony(created.getId());

            assertSame(created, retrieved);
        }

        @Test
        @DisplayName("returns null for non-existent colony")
        void nullForNonExistentColony() {
            assertNull(bridge.getColony("non-existent"));
        }

        @Test
        @DisplayName("returns all colonies for faction")
        void getAllColonies() {
            bridge.createColony("albion", "Camelot", "frontier-1");
            bridge.createColony("albion", "Stonehenge", "frontier-2");

            List<FourxColony> colonies = bridge.getColonies("albion");
            assertEquals(2, colonies.size());
        }

        @Test
        @DisplayName("adds building to colony")
        void addBuildingToColony() {
            FourxColony colony = bridge.createColony("albion", "Camelot", "frontier-1");
            FourxColony result = bridge.addBuilding(colony.getId(), "Blacksmith");

            assertSame(colony, result);
        }

        @Test
        @DisplayName("addBuilding returns null for non-existent colony")
        void addBuildingNullForNonExistent() {
            assertNull(bridge.addBuilding("non-existent", "Blacksmith"));
        }
    }

    @Nested
    @DisplayName("Trade Routes")
    class TradeRoutes {

        @Test
        @DisplayName("creates a trade route")
        void createTradeRoute() {
            FourxTradeRoute route = bridge.createTradeRoute(
                    "albion", "frontier-1", "frontier-2", FourxTradeRoute.CargoType.GENERAL
            );

            assertNotNull(route);
            assertEquals("frontier-1", route.getOriginSystemId());
            assertEquals("frontier-2", route.getDestinationSystemId());
            assertEquals(FourxTradeRoute.CargoType.GENERAL, route.getCargoType());
            assertEquals("albion", route.getOwnerFactionId());
            assertTrue(route.isActive());
        }

        @Test
        @DisplayName("trade route has generated ID")
        void tradeRouteHasGeneratedId() {
            FourxTradeRoute route = bridge.createTradeRoute(
                    "albion", "frontier-1", "frontier-2", FourxTradeRoute.CargoType.GENERAL
            );

            assertNotNull(route.getId());
            assertTrue(route.getId().startsWith("route-"));
        }

        @Test
        @DisplayName("Albion trade profit is higher")
        void albionTradeProfitHigher() {
            assertEquals(3000, bridge.getTradeProfit("albion")); // 2500 * 1.2
        }

        @Test
        @DisplayName("Hibernia trade profit is base")
        void hiberniaTradeProfitBase() {
            assertEquals(2500, bridge.getTradeProfit("hibernia")); // 2500 * 1.0
        }

        @Test
        @DisplayName("Midgard trade profit is intermediate")
        void midgardTradeProfitIntermediate() {
            assertEquals(2750, bridge.getTradeProfit("midgard")); // 2500 * 1.1
        }

        @Test
        @DisplayName("null faction trade profit is base")
        void nullFactionTradeProfitBase() {
            assertEquals(2500, bridge.getTradeProfit(null));
        }

        @Test
        @DisplayName("returns all trade routes")
        void getAllTradeRoutes() {
            bridge.createTradeRoute("albion", "f1", "f2", FourxTradeRoute.CargoType.GENERAL);
            bridge.createTradeRoute("albion", "f2", "f3", FourxTradeRoute.CargoType.MINERALS);

            List<FourxTradeRoute> routes = bridge.getTradeRoutes("albion");
            assertEquals(2, routes.size());
        }

        @Test
        @DisplayName("trade route profit is set based on realm")
        void tradeRouteProfitSetBasedOnRealm() {
            FourxTradeRoute albionRoute = bridge.createTradeRoute(
                    "albion", "f1", "f2", FourxTradeRoute.CargoType.GENERAL
            );
            FourxTradeRoute hiberniaRoute = bridge.createTradeRoute(
                    "hibernia", "f1", "f2", FourxTradeRoute.CargoType.GENERAL
            );

            assertTrue(albionRoute.getProfit() > hiberniaRoute.getProfit());
        }
    }

    @Nested
    @DisplayName("Economy Turn Processing")
    class EconomyTurnProcessing {

        @Test
        @DisplayName("processEconomyTurn does not throw")
        void processEconomyTurnDoesNotThrow() {
            assertDoesNotThrow(() -> bridge.processEconomyTurn());
        }
    }

    @Nested
    @DisplayName("Trade Route Activation")
    class TradeRouteActivation {

        @Test
        @DisplayName("setTradeRouteActive does not throw")
        void setTradeRouteActiveDoesNotThrow() {
            assertDoesNotThrow(() -> bridge.setTradeRouteActive("route-1", true));
            assertDoesNotThrow(() -> bridge.setTradeRouteActive("route-1", false));
        }
    }
}
