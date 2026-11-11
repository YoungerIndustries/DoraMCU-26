package com.yisy.modules.doramcu.hex.dungeon;

import com.yisy.modules.doramcu.hex.ShipType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ShipModuleTemplate Tests")
class ShipModuleTemplateTest {

    @Nested
    @DisplayName("ShipModuleType Enum")
    class ShipModuleTypeTests {

        @Test
        @DisplayName("should have all expected module types")
        void enumValues_shouldHaveAllTypes() {
            ShipModuleTemplate.ShipModuleType[] values = ShipModuleTemplate.ShipModuleType.values();
            assertEquals(13, values.length);
        }

        @Test
        @DisplayName("should include BRIDGE type")
        void enumValues_bridge() {
            assertEquals("BRIDGE", ShipModuleTemplate.ShipModuleType.BRIDGE.name());
        }

        @Test
        @DisplayName("should include ENGINE_ROOM type")
        void enumValues_engineRoom() {
            assertEquals("ENGINE_ROOM", ShipModuleTemplate.ShipModuleType.ENGINE_ROOM.name());
        }

        @Test
        @DisplayName("should include WEAPON_BAY type")
        void enumValues_weaponBay() {
            assertEquals("WEAPON_BAY", ShipModuleTemplate.ShipModuleType.WEAPON_BAY.name());
        }
    }

    @Nested
    @DisplayName("Basic Properties")
    class BasicPropertiesTests {

        @Test
        @DisplayName("should set and get type")
        void type_shouldWork() {
            ShipModuleTemplate t = new ShipModuleTemplate();
            t.setType(ShipModuleTemplate.ShipModuleType.BRIDGE);
            assertEquals(ShipModuleTemplate.ShipModuleType.BRIDGE, t.getType());
        }

        @Test
        @DisplayName("should set and get mass tons")
        void massTons_shouldWork() {
            ShipModuleTemplate t = new ShipModuleTemplate();
            t.setMassTons(150.5);
            assertEquals(150.5, t.getMassTons(), 0.01);
        }

        @Test
        @DisplayName("should set and get crew capacity")
        void crewCapacity_shouldWork() {
            ShipModuleTemplate t = new ShipModuleTemplate();
            t.setCrewCapacity(25);
            assertEquals(25, t.getCrewCapacity());
        }

        @Test
        @DisplayName("should set and get power consumption")
        void powerConsumption_shouldWork() {
            ShipModuleTemplate t = new ShipModuleTemplate();
            t.setPowerConsumption(50.0);
            assertEquals(50.0, t.getPowerConsumption(), 0.01);
        }

        @Test
        @DisplayName("should inherit name from ModuleTemplate")
        void name_inherited() {
            ShipModuleTemplate t = new ShipModuleTemplate();
            t.setName("Command Bridge");
            assertEquals("Command Bridge", t.getName());
        }

        @Test
        @DisplayName("should inherit dimensions from ModuleTemplate")
        void dimensions_inherited() {
            ShipModuleTemplate t = new ShipModuleTemplate();
            t.setWidth(10);
            t.setHeight(10);
            t.setDepth(5);
            assertEquals(10, t.getWidth());
            assertEquals(10, t.getHeight());
            assertEquals(5, t.getDepth());
        }
    }

    @Nested
    @DisplayName("Ship Type Compatibility")
    class CompatibilityTests {

        @Test
        @DisplayName("should be compatible with all types when no restriction set")
        void isCompatibleWith_noRestriction() {
            ShipModuleTemplate t = new ShipModuleTemplate();
            assertTrue(t.isCompatibleWith(ShipType.FRIGATE));
            assertTrue(t.isCompatibleWith(ShipType.DESTROYER));
            assertTrue(t.isCompatibleWith(ShipType.CRUISER));
        }

        @Test
        @DisplayName("should be compatible when type is in list")
        void isCompatibleWith_inList() {
            ShipModuleTemplate t = new ShipModuleTemplate();
            t.setCompatibleShipTypes("FRIGATE,DESTROYER");
            assertTrue(t.isCompatibleWith(ShipType.FRIGATE));
            assertTrue(t.isCompatibleWith(ShipType.DESTROYER));
        }

        @Test
        @DisplayName("should not be compatible when type not in list")
        void isCompatibleWith_notInList() {
            ShipModuleTemplate t = new ShipModuleTemplate();
            t.setCompatibleShipTypes("FRIGATE");
            assertFalse(t.isCompatibleWith(ShipType.MOTHERSHIP));
        }

        @Test
        @DisplayName("should handle blank compatible types")
        void isCompatibleWith_blank() {
            ShipModuleTemplate t = new ShipModuleTemplate();
            t.setCompatibleShipTypes("  ");
            assertTrue(t.isCompatibleWith(ShipType.FRIGATE));
        }

        @Test
        @DisplayName("should handle null compatible types")
        void isCompatibleWith_null() {
            ShipModuleTemplate t = new ShipModuleTemplate();
            t.setCompatibleShipTypes(null);
            assertTrue(t.isCompatibleWith(ShipType.CRUISER));
        }
    }

    @Nested
    @DisplayName("ToString")
    class ToStringTests {

        @Test
        @DisplayName("should produce meaningful string representation")
        void toString_shouldProduceOutput() {
            ShipModuleTemplate t = new ShipModuleTemplate();
            t.setId(1L);
            t.setName("Bridge");
            t.setType(ShipModuleTemplate.ShipModuleType.BRIDGE);
            t.setWidth(10);
            t.setHeight(10);
            t.setDepth(5);
            t.setMassTons(150.5);
            String s = t.toString();
            assertTrue(s.contains("Bridge"));
            assertTrue(s.contains("BRIDGE"));
            assertTrue(s.contains("10x10x5"));
            assertTrue(s.contains("150.5"));
        }
    }
}
