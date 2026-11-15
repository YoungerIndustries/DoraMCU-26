package com.yisy.modules.doradl.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.yisy.modules.doradl.dto.SubSectorInfo;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("SubSectorInfo Tests")
class SubSectorInfoTest {

    @Nested
    @DisplayName("Default constructor")
    class DefaultConstructor {

        @Test
        @DisplayName("Initializes zero/null defaults")
        void defaults() {
            SubSectorInfo info = new SubSectorInfo();
            assertEquals(0, info.getId());
            assertNull(info.getName());
            assertEquals(0, info.getSectorRow());
            assertEquals(0, info.getSectorCol());
            assertEquals(0, info.getSubRow());
            assertEquals(0, info.getSubCol());
            assertFalse(info.isGenerated());
            assertEquals(0, info.getSystemCount());
            assertNull(info.getSystems());
            assertNull(info.getOwnerEmpireIds());
            assertFalse(info.isContested());
            assertNull(info.getTradeType());
        }
    }

    @Nested
    @DisplayName("Parameterized constructor")
    class ParameterizedConstructor {

        @Test
        @DisplayName("Sets id, name, sectorRow, sectorCol, subRow, subCol, generated, systemCount, systems")
        void setsFields() {
            List<Map<String, Object>> systems = List.of(Map.of("name", "Alpha"));
            SubSectorInfo info = new SubSectorInfo(1, "Core", 0, 0, 0, 0, true, 2, systems);
            assertEquals(1, info.getId());
            assertEquals("Core", info.getName());
            assertEquals(0, info.getSectorRow());
            assertEquals(0, info.getSectorCol());
            assertEquals(0, info.getSubRow());
            assertEquals(0, info.getSubCol());
            assertTrue(info.isGenerated());
            assertEquals(2, info.getSystemCount());
            assertSame(systems, info.getSystems());
        }
    }

    @Nested
    @DisplayName("Field setters and getters")
    class Fields {

        @Test
        @DisplayName("Set and get id")
        void id() {
            SubSectorInfo info = new SubSectorInfo();
            info.setId(42);
            assertEquals(42, info.getId());
        }

        @Test
        @DisplayName("Set and get name")
        void name() {
            SubSectorInfo info = new SubSectorInfo();
            info.setName("Alpha");
            assertEquals("Alpha", info.getName());
        }

        @Test
        @DisplayName("Set and get sectorRow")
        void sectorRow() {
            SubSectorInfo info = new SubSectorInfo();
            info.setSectorRow(5);
            assertEquals(5, info.getSectorRow());
        }

        @Test
        @DisplayName("Set and get sectorCol")
        void sectorCol() {
            SubSectorInfo info = new SubSectorInfo();
            info.setSectorCol(3);
            assertEquals(3, info.getSectorCol());
        }

        @Test
        @DisplayName("Set and get subRow")
        void subRow() {
            SubSectorInfo info = new SubSectorInfo();
            info.setSubRow(1);
            assertEquals(1, info.getSubRow());
        }

        @Test
        @DisplayName("Set and get subCol")
        void subCol() {
            SubSectorInfo info = new SubSectorInfo();
            info.setSubCol(2);
            assertEquals(2, info.getSubCol());
        }

        @Test
        @DisplayName("Set and get generated")
        void generated() {
            SubSectorInfo info = new SubSectorInfo();
            info.setGenerated(true);
            assertTrue(info.isGenerated());
        }

        @Test
        @DisplayName("Set and get systemCount")
        void systemCount() {
            SubSectorInfo info = new SubSectorInfo();
            info.setSystemCount(3);
            assertEquals(3, info.getSystemCount());
        }

        @Test
        @DisplayName("Set and get systems")
        void systems() {
            SubSectorInfo info = new SubSectorInfo();
            List<Map<String, Object>> systems = List.of(Map.of("name", "Beta"));
            info.setSystems(systems);
            assertSame(systems, info.getSystems());
        }

        @Test
        @DisplayName("Set and get ownerEmpireIds")
        void ownerEmpireIds() {
            SubSectorInfo info = new SubSectorInfo();
            Set<Integer> owners = Set.of(1, 2);
            info.setOwnerEmpireIds(owners);
            assertSame(owners, info.getOwnerEmpireIds());
        }

        @Test
        @DisplayName("Set and get contested")
        void contested() {
            SubSectorInfo info = new SubSectorInfo();
            info.setContested(true);
            assertTrue(info.isContested());
        }

        @Test
        @DisplayName("Set and get tradeType")
        void tradeType() {
            SubSectorInfo info = new SubSectorInfo();
            info.setTradeType("Agricultural");
            assertEquals("Agricultural", info.getTradeType());
        }
    }
}
