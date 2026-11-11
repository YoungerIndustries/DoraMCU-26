package com.yisy.modules.doramcu.star_trek_4x.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("TradeRoute Tests")
class TradeRouteTest {

    @Nested
    @DisplayName("Constructor")
    class Constructor {

        @Test
        @DisplayName("Default constructor initializes defaults")
        void defaults() {
            TradeRoute route = new TradeRoute();
            assertNull(route.getId());
            assertNull(route.getOriginSystemId());
            assertNull(route.getDestinationSystemId());
            assertNull(route.getCargoType());
            assertEquals(0, route.getProfit());
            assertEquals(50, route.getSecurityLevel());
            assertTrue(route.isActive());
            assertNull(route.getOwnerFactionId());
            assertNull(route.getGameSession());
        }
    }

    @Nested
    @DisplayName("Cargo types")
    class CargoTypes {

        @Test
        @DisplayName("Set cargo type")
        void setCargoType() {
            TradeRoute route = new TradeRoute();
            route.setCargoType(TradeRoute.CargoType.MINERALS);
            assertEquals(TradeRoute.CargoType.MINERALS, route.getCargoType());
        }

        @Test
        @DisplayName("All cargo type enum values")
        void allCargoTypes() {
            assertSame(TradeRoute.CargoType.MINERALS, TradeRoute.CargoType.valueOf("MINERALS"));
            assertSame(TradeRoute.CargoType.ENERGY, TradeRoute.CargoType.valueOf("ENERGY"));
            assertSame(TradeRoute.CargoType.FOOD, TradeRoute.CargoType.valueOf("FOOD"));
            assertSame(TradeRoute.CargoType.RESEARCH, TradeRoute.CargoType.valueOf("RESEARCH"));
            assertSame(TradeRoute.CargoType.GENERAL, TradeRoute.CargoType.valueOf("GENERAL"));
        }
    }

    @Nested
    @DisplayName("Profit and security")
    class ProfitAndSecurity {

        @Test
        @DisplayName("Set profit")
        void setProfit() {
            TradeRoute route = new TradeRoute();
            route.setProfit(100);
            assertEquals(100, route.getProfit());
        }

        @Test
        @DisplayName("Set security level")
        void setSecurityLevel() {
            TradeRoute route = new TradeRoute();
            route.setSecurityLevel(80);
            assertEquals(80, route.getSecurityLevel());
        }
    }

    @Nested
    @DisplayName("Active status")
    class ActiveStatus {

        @Test
        @DisplayName("Default is active")
        void defaultActive() {
            TradeRoute route = new TradeRoute();
            assertTrue(route.isActive());
        }

        @Test
        @DisplayName("Set active to false")
        void setActiveFalse() {
            TradeRoute route = new TradeRoute();
            route.setActive(false);
            assertFalse(route.isActive());
        }
    }
}
