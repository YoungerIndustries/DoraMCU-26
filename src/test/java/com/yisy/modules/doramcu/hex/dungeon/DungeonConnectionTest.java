package com.yisy.modules.doramcu.hex.dungeon;

import com.yisy.modules.doramcu.hex.M3space;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("DungeonConnection Tests")
class DungeonConnectionTest {

    @Nested
    @DisplayName("Constructor Validation")
    class ConstructorTests {

        @Test
        @DisplayName("should create valid connection")
        void constructor_valid() {
            M3space fromCell = new M3space(5, 0, 5);
            M3space toCell = new M3space(5, 9, 5);
            DungeonConnection conn = new DungeonConnection("entrance", fromCell, "corridor", toCell);
            assertEquals("entrance", conn.getFromRoomId());
            assertEquals(fromCell, conn.getFromCell());
            assertEquals("corridor", conn.getToRoomId());
            assertEquals(toCell, conn.getToCell());
        }

        @Test
        @DisplayName("should throw on null fromRoomId")
        void constructor_nullFromRoomId() {
            assertThrows(IllegalArgumentException.class, () ->
                    new DungeonConnection(null, new M3space(0, 0, 0), "corridor", new M3space(0, 0, 0)));
        }

        @Test
        @DisplayName("should throw on blank fromRoomId")
        void constructor_blankFromRoomId() {
            assertThrows(IllegalArgumentException.class, () ->
                    new DungeonConnection("  ", new M3space(0, 0, 0), "corridor", new M3space(0, 0, 0)));
        }

        @Test
        @DisplayName("should throw on null toRoomId")
        void constructor_nullToRoomId() {
            assertThrows(IllegalArgumentException.class, () ->
                    new DungeonConnection("entrance", new M3space(0, 0, 0), null, new M3space(0, 0, 0)));
        }

        @Test
        @DisplayName("should throw on blank toRoomId")
        void constructor_blankToRoomId() {
            assertThrows(IllegalArgumentException.class, () ->
                    new DungeonConnection("entrance", new M3space(0, 0, 0), "  ", new M3space(0, 0, 0)));
        }

        @Test
        @DisplayName("should throw on null fromCell")
        void constructor_nullFromCell() {
            assertThrows(IllegalArgumentException.class, () ->
                    new DungeonConnection("entrance", null, "corridor", new M3space(0, 0, 0)));
        }

        @Test
        @DisplayName("should throw on null toCell")
        void constructor_nullToCell() {
            assertThrows(IllegalArgumentException.class, () ->
                    new DungeonConnection("entrance", new M3space(0, 0, 0), "corridor", null));
        }
    }

    @Nested
    @DisplayName("Bidirectional Access")
    class BidirectionalTests {

        private DungeonConnection conn;

        @org.junit.jupiter.api.BeforeEach
        void setUp() {
            conn = new DungeonConnection("entrance", new M3space(5, 0, 5), "corridor", new M3space(5, 9, 5));
        }

        @Test
        @DisplayName("should return other room ID from fromRoomId")
        void getOtherRoomId_fromTo() {
            assertEquals("corridor", conn.getOtherRoomId("entrance"));
        }

        @Test
        @DisplayName("should return other room ID from toRoomId")
        void getOtherRoomId_toFrom() {
            assertEquals("entrance", conn.getOtherRoomId("corridor"));
        }

        @Test
        @DisplayName("should return null for unrelated room")
        void getOtherRoomId_unrelated() {
            assertNull(conn.getOtherRoomId("treasure"));
        }

        @Test
        @DisplayName("should return other cell from fromRoomId")
        void getOtherCell_fromTo() {
            M3space other = conn.getOtherCell("entrance");
            assertEquals(5, other.getX());
            assertEquals(9, other.getY());
            assertEquals(5, other.getZ());
        }

        @Test
        @DisplayName("should return other cell from toRoomId")
        void getOtherCell_toFrom() {
            M3space other = conn.getOtherCell("corridor");
            assertEquals(5, other.getX());
            assertEquals(0, other.getY());
            assertEquals(5, other.getZ());
        }

        @Test
        @DisplayName("should return null for unrelated room cell")
        void getOtherCell_unrelated() {
            assertNull(conn.getOtherCell("treasure"));
        }
    }

    @Nested
    @DisplayName("Room Involvement")
    class InvolvementTests {

        @Test
        @DisplayName("should detect involvement of fromRoom")
        void involvesRoom_from() {
            DungeonConnection conn = new DungeonConnection("entrance", new M3space(0, 0, 0), "corridor", new M3space(0, 0, 0));
            assertTrue(conn.involvesRoom("entrance"));
        }

        @Test
        @DisplayName("should detect involvement of toRoom")
        void involvesRoom_to() {
            DungeonConnection conn = new DungeonConnection("entrance", new M3space(0, 0, 0), "corridor", new M3space(0, 0, 0));
            assertTrue(conn.involvesRoom("corridor"));
        }

        @Test
        @DisplayName("should reject unrelated room")
        void involvesRoom_unrelated() {
            DungeonConnection conn = new DungeonConnection("entrance", new M3space(0, 0, 0), "corridor", new M3space(0, 0, 0));
            assertFalse(conn.involvesRoom("treasure"));
        }
    }

    @Nested
    @DisplayName("Equality and HashCode")
    class EqualityTests {

        @Test
        @DisplayName("should be equal with same room IDs")
        void equals_sameIds() {
            DungeonConnection c1 = new DungeonConnection("entrance", new M3space(0, 0, 0), "corridor", new M3space(0, 0, 0));
            DungeonConnection c2 = new DungeonConnection("entrance", new M3space(9, 9, 9), "corridor", new M3space(9, 9, 9));
            assertEquals(c1, c2);
        }

        @Test
        @DisplayName("should not be equal with different room IDs")
        void equals_differentIds() {
            DungeonConnection c1 = new DungeonConnection("entrance", new M3space(0, 0, 0), "corridor", new M3space(0, 0, 0));
            DungeonConnection c2 = new DungeonConnection("entrance", new M3space(0, 0, 0), "treasure", new M3space(0, 0, 0));
            assertNotEquals(c1, c2);
        }

        @Test
        @DisplayName("should be equal to self")
        void equals_self() {
            DungeonConnection c = new DungeonConnection("entrance", new M3space(0, 0, 0), "corridor", new M3space(0, 0, 0));
            assertEquals(c, c);
        }

        @Test
        @DisplayName("should not be equal to null")
        void equals_null() {
            DungeonConnection c = new DungeonConnection("entrance", new M3space(0, 0, 0), "corridor", new M3space(0, 0, 0));
            assertNotEquals(null, c);
        }

        @Test
        @DisplayName("should not be equal to different class")
        void equals_differentClass() {
            DungeonConnection c = new DungeonConnection("entrance", new M3space(0, 0, 0), "corridor", new M3space(0, 0, 0));
            assertNotEquals("string", c);
        }

        @Test
        @DisplayName("should have consistent hashCode")
        void hashCode_consistent() {
            DungeonConnection c1 = new DungeonConnection("entrance", new M3space(0, 0, 0), "corridor", new M3space(0, 0, 0));
            DungeonConnection c2 = new DungeonConnection("entrance", new M3space(9, 9, 9), "corridor", new M3space(9, 9, 9));
            assertEquals(c1.hashCode(), c2.hashCode());
        }
    }

    @Nested
    @DisplayName("ToString")
    class ToStringTests {

        @Test
        @DisplayName("should produce meaningful string representation")
        void toString_shouldProduceOutput() {
            DungeonConnection conn = new DungeonConnection("entrance", new M3space(5, 0, 5), "corridor", new M3space(5, 9, 5));
            String s = conn.toString();
            assertTrue(s.contains("entrance"));
            assertTrue(s.contains("corridor"));
            assertTrue(s.contains("5"));
            assertTrue(s.contains("<->"));
        }
    }
}
