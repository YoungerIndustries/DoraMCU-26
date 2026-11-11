package com.yisy.modules.doramcu.hex.dungeon;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("StationModuleTemplate Tests")
class StationModuleTemplateTest {

    @Nested
    @DisplayName("StationModuleType Enum")
    class StationModuleTypeTests {

        @Test
        @DisplayName("should have all expected module types")
        void enumValues_shouldHaveAllTypes() {
            StationModuleTemplate.StationModuleType[] values = StationModuleTemplate.StationModuleType.values();
            assertEquals(13, values.length);
        }

        @Test
        @DisplayName("should include COMMAND_MODULE type")
        void enumValues_commandModule() {
            assertEquals("COMMAND_MODULE", StationModuleTemplate.StationModuleType.COMMAND_MODULE.name());
        }

        @Test
        @DisplayName("should include DOCKING_RING type")
        void enumValues_dockingRing() {
            assertEquals("DOCKING_RING", StationModuleTemplate.StationModuleType.DOCKING_RING.name());
        }
    }

    @Nested
    @DisplayName("Basic Properties")
    class BasicPropertiesTests {

        @Test
        @DisplayName("should set and get type")
        void type_shouldWork() {
            StationModuleTemplate t = new StationModuleTemplate();
            t.setType(StationModuleTemplate.StationModuleType.COMMAND_MODULE);
            assertEquals(StationModuleTemplate.StationModuleType.COMMAND_MODULE, t.getType());
        }

        @Test
        @DisplayName("should set and get docking ports")
        void dockingPorts_shouldWork() {
            StationModuleTemplate t = new StationModuleTemplate();
            t.setDockingPorts(4);
            assertEquals(4, t.getDockingPorts());
        }

        @Test
        @DisplayName("should set and get shield contribution")
        void shieldContribution_shouldWork() {
            StationModuleTemplate t = new StationModuleTemplate();
            t.setShieldContribution(100);
            assertEquals(100, t.getShieldContribution());
        }

        @Test
        @DisplayName("should set and get hull contribution")
        void hullContribution_shouldWork() {
            StationModuleTemplate t = new StationModuleTemplate();
            t.setHullContribution(500);
            assertEquals(500, t.getHullContribution());
        }

        @Test
        @DisplayName("should inherit name from ModuleTemplate")
        void name_inherited() {
            StationModuleTemplate t = new StationModuleTemplate();
            t.setName("Command Module");
            assertEquals("Command Module", t.getName());
        }
    }

    @Nested
    @DisplayName("ToString")
    class ToStringTests {

        @Test
        @DisplayName("should produce meaningful string representation")
        void toString_shouldProduceOutput() {
            StationModuleTemplate t = new StationModuleTemplate();
            t.setId(1L);
            t.setName("Docking Ring");
            t.setType(StationModuleTemplate.StationModuleType.DOCKING_RING);
            t.setWidth(20);
            t.setHeight(20);
            t.setDepth(5);
            t.setDockingPorts(8);
            String s = t.toString();
            assertTrue(s.contains("Docking Ring"));
            assertTrue(s.contains("DOCKING_RING"));
            assertTrue(s.contains("20x20x5"));
            assertTrue(s.contains("docking=8"));
        }
    }
}
