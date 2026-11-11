package com.yisy.modules.doramcu.hex;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ShipConnection Tests")
class ShipConnectionTest {

    @Nested
    @DisplayName("Constructor Validation")
    class ConstructorTests {

        @Test
        @DisplayName("should create valid connection")
        void constructor_valid() {
            M3space fromCell = new M3space(5, 0, 5);
            M3space toCell = new M3space(5, 9, 5);
            ShipConnection conn = new ShipConnection("bridge", fromCell, "engine", toCell);
            assertEquals("bridge", conn.getFromModuleId());
            assertEquals(fromCell, conn.getFromCell());
            assertEquals("engine", conn.getToModuleId());
            assertEquals(toCell, conn.getToCell());
        }

        @Test
        @DisplayName("should throw on null fromModuleId")
        void constructor_nullFromModuleId() {
            assertThrows(IllegalArgumentException.class, () ->
                    new ShipConnection(null, new M3space(0, 0, 0), "engine", new M3space(0, 0, 0)));
        }

        @Test
        @DisplayName("should throw on blank fromModuleId")
        void constructor_blankFromModuleId() {
            assertThrows(IllegalArgumentException.class, () ->
                    new ShipConnection("  ", new M3space(0, 0, 0), "engine", new M3space(0, 0, 0)));
        }

        @Test
        @DisplayName("should throw on null toModuleId")
        void constructor_nullToModuleId() {
            assertThrows(IllegalArgumentException.class, () ->
                    new ShipConnection("bridge", new M3space(0, 0, 0), null, new M3space(0, 0, 0)));
        }

        @Test
        @DisplayName("should throw on null fromCell")
        void constructor_nullFromCell() {
            assertThrows(IllegalArgumentException.class, () ->
                    new ShipConnection("bridge", null, "engine", new M3space(0, 0, 0)));
        }

        @Test
        @DisplayName("should throw on null toCell")
        void constructor_nullToCell() {
            assertThrows(IllegalArgumentException.class, () ->
                    new ShipConnection("bridge", new M3space(0, 0, 0), "engine", null));
        }
    }

    @Nested
    @DisplayName("Bidirectional Access")
    class BidirectionalTests {

        private ShipConnection conn;

        @org.junit.jupiter.api.BeforeEach
        void setUp() {
            conn = new ShipConnection("bridge", new M3space(5, 0, 5), "engine", new M3space(5, 9, 5));
        }

        @Test
        @DisplayName("should return other module ID from fromModuleId")
        void getOtherModuleId_fromTo() {
            assertEquals("engine", conn.getOtherModuleId("bridge"));
        }

        @Test
        @DisplayName("should return other module ID from toModuleId")
        void getOtherModuleId_toFrom() {
            assertEquals("bridge", conn.getOtherModuleId("engine"));
        }

        @Test
        @DisplayName("should return null for unrelated module")
        void getOtherModuleId_unrelated() {
            assertNull(conn.getOtherModuleId("cargo"));
        }

        @Test
        @DisplayName("should return other cell from fromModuleId")
        void getOtherCell_fromTo() {
            M3space other = conn.getOtherCell("bridge");
            assertEquals(5, other.getX());
            assertEquals(9, other.getY());
            assertEquals(5, other.getZ());
        }

        @Test
        @DisplayName("should return other cell from toModuleId")
        void getOtherCell_toFrom() {
            M3space other = conn.getOtherCell("engine");
            assertEquals(5, other.getX());
            assertEquals(0, other.getY());
            assertEquals(5, other.getZ());
        }

        @Test
        @DisplayName("should return null for unrelated module cell")
        void getOtherCell_unrelated() {
            assertNull(conn.getOtherCell("cargo"));
        }
    }

    @Nested
    @DisplayName("Module Involvement")
    class InvolvementTests {

        @Test
        @DisplayName("should detect involvement of fromModule")
        void involvesModule_from() {
            ShipConnection conn = new ShipConnection("bridge", new M3space(0, 0, 0), "engine", new M3space(0, 0, 0));
            assertTrue(conn.involvesModule("bridge"));
        }

        @Test
        @DisplayName("should detect involvement of toModule")
        void involvesModule_to() {
            ShipConnection conn = new ShipConnection("bridge", new M3space(0, 0, 0), "engine", new M3space(0, 0, 0));
            assertTrue(conn.involvesModule("engine"));
        }

        @Test
        @DisplayName("should reject unrelated module")
        void involvesModule_unrelated() {
            ShipConnection conn = new ShipConnection("bridge", new M3space(0, 0, 0), "engine", new M3space(0, 0, 0));
            assertFalse(conn.involvesModule("cargo"));
        }
    }

    @Nested
    @DisplayName("Equality and HashCode")
    class EqualityTests {

        @Test
        @DisplayName("should be equal with same module IDs")
        void equals_sameIds() {
            ShipConnection c1 = new ShipConnection("bridge", new M3space(0, 0, 0), "engine", new M3space(0, 0, 0));
            ShipConnection c2 = new ShipConnection("bridge", new M3space(9, 9, 9), "engine", new M3space(9, 9, 9));
            assertEquals(c1, c2);
        }

        @Test
        @DisplayName("should not be equal with different module IDs")
        void equals_differentIds() {
            ShipConnection c1 = new ShipConnection("bridge", new M3space(0, 0, 0), "engine", new M3space(0, 0, 0));
            ShipConnection c2 = new ShipConnection("bridge", new M3space(0, 0, 0), "cargo", new M3space(0, 0, 0));
            assertNotEquals(c1, c2);
        }

        @Test
        @DisplayName("should be equal to self")
        void equals_self() {
            ShipConnection c = new ShipConnection("bridge", new M3space(0, 0, 0), "engine", new M3space(0, 0, 0));
            assertEquals(c, c);
        }

        @Test
        @DisplayName("should not be equal to null")
        void equals_null() {
            ShipConnection c = new ShipConnection("bridge", new M3space(0, 0, 0), "engine", new M3space(0, 0, 0));
            assertNotEquals(null, c);
        }

        @Test
        @DisplayName("should have consistent hashCode")
        void hashCode_consistent() {
            ShipConnection c1 = new ShipConnection("bridge", new M3space(0, 0, 0), "engine", new M3space(0, 0, 0));
            ShipConnection c2 = new ShipConnection("bridge", new M3space(9, 9, 9), "engine", new M3space(9, 9, 9));
            assertEquals(c1.hashCode(), c2.hashCode());
        }
    }

    @Nested
    @DisplayName("ToString")
    class ToStringTests {

        @Test
        @DisplayName("should produce meaningful string representation")
        void toString_shouldProduceOutput() {
            ShipConnection conn = new ShipConnection("bridge", new M3space(5, 0, 5), "engine", new M3space(5, 9, 5));
            String s = conn.toString();
            assertTrue(s.contains("bridge"));
            assertTrue(s.contains("engine"));
            assertTrue(s.contains("5"));
        }
    }
}
