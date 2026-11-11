package com.yisy.modules.doramcu.fourx;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("FourxTradeRoute Tests")
class FourxTradeRouteTest {

    @Nested
    @DisplayName("Constructor")
    class Constructor {

        @Test
        @DisplayName("Default constructor initializes defaults")
        void defaults() {
            FourxTradeRoute route = new FourxTradeRoute();
            assertNull(route.getId());
            assertNull(route.getOriginSystemId());
            assertNull(route.getDestinationSystemId());
            assertNull(route.getCargoType());
            assertEquals(0, route.getProfit());
            assertEquals(50, route.getSecurityLevel());
            assertTrue(route.isActive());
        }

        @Test
        @DisplayName("Parameterized constructor sets fields")
        void parameterized() {
            FourxTradeRoute route = new FourxTradeRoute("sys1", "sys2", FourxTradeRoute.CargoType.MINERALS, "f1");
            assertEquals("sys1", route.getOriginSystemId());
            assertEquals("sys2", route.getDestinationSystemId());
            assertEquals(FourxTradeRoute.CargoType.MINERALS, route.getCargoType());
            assertEquals("f1", route.getOwnerFactionId());
        }
    }

    @Nested
    @DisplayName("Cargo types")
    class CargoTypes {

        @Test
        @DisplayName("All cargo types are present")
        void allCargoTypes() {
            assertSame(FourxTradeRoute.CargoType.MINERALS, FourxTradeRoute.CargoType.valueOf("MINERALS"));
            assertSame(FourxTradeRoute.CargoType.ENERGY, FourxTradeRoute.CargoType.valueOf("ENERGY"));
            assertSame(FourxTradeRoute.CargoType.FOOD, FourxTradeRoute.CargoType.valueOf("FOOD"));
            assertSame(FourxTradeRoute.CargoType.RESEARCH, FourxTradeRoute.CargoType.valueOf("RESEARCH"));
            assertSame(FourxTradeRoute.CargoType.GENERAL, FourxTradeRoute.CargoType.valueOf("GENERAL"));
        }
    }

    @Nested
    @DisplayName("Setters and getters")
    class SettersAndGetters {

        @Test
        @DisplayName("Profit and security")
        void profitAndSecurity() {
            FourxTradeRoute route = new FourxTradeRoute();
            route.setProfit(100);
            route.setSecurityLevel(75);
            assertEquals(100, route.getProfit());
            assertEquals(75, route.getSecurityLevel());
        }

        @Test
        @DisplayName("Active status")
        void active() {
            FourxTradeRoute route = new FourxTradeRoute();
            assertTrue(route.isActive());
            route.setActive(false);
            assertFalse(route.isActive());
        }
    }
}
