package com.yisy.modules.doramcu.fourx;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("FourxTradeRouteEntity Tests")
class FourxTradeRouteEntityTest {

    @Nested
    @DisplayName("Constructor")
    class Constructor {

        @Test
        @DisplayName("Parameterized constructor sets fields")
        void parameterized() {
            FourxTradeRouteEntity entity = new FourxTradeRouteEntity("tr1", "sys1", "sys2", "MINERALS", "f1");
            assertEquals("tr1", entity.getTradeRouteId());
            assertEquals("sys1", entity.getOriginSystemId());
            assertEquals("sys2", entity.getDestinationSystemId());
            assertEquals("MINERALS", entity.getCargoType());
            assertEquals("f1", entity.getOwnerFactionId());
            assertEquals(0, entity.getProfit());
            assertEquals(50, entity.getSecurityLevel());
            assertTrue(entity.isActive());
            assertNotNull(entity.getCreatedAt());
        }
    }

    @Nested
    @DisplayName("toDto conversion")
    class ToDto {

        @Test
        @DisplayName("Converts all fields correctly")
        void convertsAllFields() {
            FourxTradeRouteEntity entity = new FourxTradeRouteEntity("tr1", "sys1", "sys2", "MINERALS", "f1");
            entity.setProfit(100);
            entity.setSecurityLevel(75);
            entity.setActive(false);

            FourxTradeRoute dto = entity.toDto();
            assertEquals("tr1", dto.getId());
            assertEquals("sys1", dto.getOriginSystemId());
            assertEquals("sys2", dto.getDestinationSystemId());
            assertEquals(FourxTradeRoute.CargoType.MINERALS, dto.getCargoType());
            assertEquals("f1", dto.getOwnerFactionId());
            assertEquals(100, dto.getProfit());
            assertEquals(75, dto.getSecurityLevel());
            assertFalse(dto.isActive());
        }

        @Test
        @DisplayName("Handles invalid cargo type gracefully")
        void invalidCargoType() {
            FourxTradeRouteEntity entity = new FourxTradeRouteEntity("tr1", "sys1", "sys2", "INVALID", "f1");
            FourxTradeRoute dto = entity.toDto();
            assertEquals(FourxTradeRoute.CargoType.GENERAL, dto.getCargoType());
        }

        @Test
        @DisplayName("All cargo types parse correctly")
        void allCargoTypes() {
            for (FourxTradeRoute.CargoType type : FourxTradeRoute.CargoType.values()) {
                FourxTradeRouteEntity entity = new FourxTradeRouteEntity("tr1", "sys1", "sys2", type.name(), "f1");
                FourxTradeRoute dto = entity.toDto();
                assertEquals(type, dto.getCargoType());
            }
        }
    }

    @Nested
    @DisplayName("Setters and getters")
    class SettersAndGetters {

        @Test
        @DisplayName("Profit and security")
        void profitAndSecurity() {
            FourxTradeRouteEntity entity = new FourxTradeRouteEntity("tr1", "sys1", "sys2", "MINERALS", "f1");
            entity.setProfit(200);
            entity.setSecurityLevel(90);
            assertEquals(200, entity.getProfit());
            assertEquals(90, entity.getSecurityLevel());
        }

        @Test
        @DisplayName("Active status")
        void active() {
            FourxTradeRouteEntity entity = new FourxTradeRouteEntity("tr1", "sys1", "sys2", "MINERALS", "f1");
            assertTrue(entity.isActive());
            entity.setActive(false);
            assertFalse(entity.isActive());
        }
    }
}
