package com.yisy.modules.doramcu.hex.dungeon;

import com.yisy.modules.doramcu.hex.Compartment;
import com.yisy.modules.doramcu.hex.CompartmentType;
import com.yisy.modules.doramcu.hex.HullModule;
import com.yisy.modules.doramcu.hex.M3space;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Dungeon Tests")
class DungeonTest {

    private HullModule createModule(String name) {
        return new HullModule(name, 10, 10, 10);
    }

    // ---- Constructor ----

    @Nested
    @DisplayName("Constructor Validation")
    class ConstructorTests {

        @Test
        @DisplayName("should create dungeon with name and description")
        void constructor_valid() {
            Dungeon dungeon = new Dungeon("Dwarven Mine", "A deep underground mine");
            assertEquals("Dwarven Mine", dungeon.getName());
            assertEquals("A deep underground mine", dungeon.getDescription());
            assertEquals(0, dungeon.getRoomCount());
            assertEquals(1, dungeon.getDifficulty());
        }

        @Test
        @DisplayName("should throw on null name")
        void constructor_nullName() {
            assertThrows(IllegalArgumentException.class, () -> new Dungeon(null, "desc"));
        }

        @Test
        @DisplayName("should throw on blank name")
        void constructor_blankName() {
            assertThrows(IllegalArgumentException.class, () -> new Dungeon("  ", "desc"));
        }

        @Test
        @DisplayName("should throw on empty name")
        void constructor_emptyName() {
            assertThrows(IllegalArgumentException.class, () -> new Dungeon("", "desc"));
        }

        @Test
        @DisplayName("should allow null description")
        void constructor_nullDescription() {
            Dungeon dungeon = new Dungeon("Mine", null);
            assertNull(dungeon.getDescription());
        }
    }

    // ---- Room Management ----

    @Nested
    @DisplayName("Room Management")
    class RoomManagementTests {

        @Test
        @DisplayName("should add a room")
        void addRoom_shouldAdd() {
            Dungeon dungeon = new Dungeon("Mine", "desc");
            dungeon.addRoom("room1", createModule("Entrance"));
            assertEquals(1, dungeon.getRoomCount());
            assertNotNull(dungeon.getRoom("room1"));
        }

        @Test
        @DisplayName("should throw on null roomId")
        void addRoom_nullRoomId() {
            Dungeon dungeon = new Dungeon("Mine", "desc");
            assertThrows(IllegalArgumentException.class, () -> dungeon.addRoom(null, createModule("Room")));
        }

        @Test
        @DisplayName("should throw on blank roomId")
        void addRoom_blankRoomId() {
            Dungeon dungeon = new Dungeon("Mine", "desc");
            assertThrows(IllegalArgumentException.class, () -> dungeon.addRoom("  ", createModule("Room")));
        }

        @Test
        @DisplayName("should throw on null module")
        void addRoom_nullModule() {
            Dungeon dungeon = new Dungeon("Mine", "desc");
            assertThrows(IllegalArgumentException.class, () -> dungeon.addRoom("room1", null));
        }

        @Test
        @DisplayName("should throw on duplicate roomId")
        void addRoom_duplicateId() {
            Dungeon dungeon = new Dungeon("Mine", "desc");
            dungeon.addRoom("room1", createModule("Entrance"));
            assertThrows(IllegalArgumentException.class, () -> dungeon.addRoom("room1", createModule("Another")));
        }

        @Test
        @DisplayName("should remove a room and its connections")
        void removeRoom_shouldRemove() {
            Dungeon dungeon = new Dungeon("Mine", "desc");
            dungeon.addRoom("room1", createModule("Entrance"));
            dungeon.addRoom("room2", createModule("Corridor"));
            dungeon.connectRooms("room1", new M3space(5, 0, 5), "room2", new M3space(5, 9, 5));
            assertTrue(dungeon.removeRoom("room1"));
            assertEquals(1, dungeon.getRoomCount());
            assertEquals(0, dungeon.getConnections().size());
        }

        @Test
        @DisplayName("should return false when removing non-existent room")
        void removeRoom_nonExistent() {
            Dungeon dungeon = new Dungeon("Mine", "desc");
            assertFalse(dungeon.removeRoom("nonexistent"));
        }

        @Test
        @DisplayName("should return unmodifiable rooms map")
        void getRooms_unmodifiable() {
            Dungeon dungeon = new Dungeon("Mine", "desc");
            dungeon.addRoom("room1", createModule("Entrance"));
            assertThrows(UnsupportedOperationException.class, () -> dungeon.getRooms().put("room2", createModule("Corridor")));
        }

        @Test
        @DisplayName("should get room by ID")
        void getRoom_shouldReturn() {
            Dungeon dungeon = new Dungeon("Mine", "desc");
            HullModule module = createModule("Entrance");
            dungeon.addRoom("room1", module);
            assertSame(module, dungeon.getRoom("room1"));
        }

        @Test
        @DisplayName("should return null for non-existent room")
        void getRoom_nonExistent() {
            Dungeon dungeon = new Dungeon("Mine", "desc");
            assertNull(dungeon.getRoom("nonexistent"));
        }
    }

    // ---- Room Positions ----

    @Nested
    @DisplayName("Room Positions")
    class RoomPositionTests {

        @Test
        @DisplayName("should set and get room position")
        void setRoomPosition_shouldSet() {
            Dungeon dungeon = new Dungeon("Mine", "desc");
            dungeon.addRoom("room1", createModule("Entrance"));
            dungeon.setRoomPosition("room1", 10, 20, 30);
            Dungeon.Point3D pos = dungeon.getRoomPosition("room1");
            assertNotNull(pos);
            assertEquals(10, pos.getX());
            assertEquals(20, pos.getY());
            assertEquals(30, pos.getZ());
        }

        @Test
        @DisplayName("should throw on non-existent room position")
        void setRoomPosition_nonExistent() {
            Dungeon dungeon = new Dungeon("Mine", "desc");
            assertThrows(IllegalArgumentException.class, () -> dungeon.setRoomPosition("nonexistent", 0, 0, 0));
        }

        @Test
        @DisplayName("should return null for unset position")
        void getRoomPosition_unset() {
            Dungeon dungeon = new Dungeon("Mine", "desc");
            dungeon.addRoom("room1", createModule("Entrance"));
            assertNull(dungeon.getRoomPosition("room1"));
        }

        @Test
        @DisplayName("should remove position when room is removed")
        void removeRoom_removesPosition() {
            Dungeon dungeon = new Dungeon("Mine", "desc");
            dungeon.addRoom("room1", createModule("Entrance"));
            dungeon.setRoomPosition("room1", 10, 20, 30);
            dungeon.removeRoom("room1");
            assertNull(dungeon.getRoomPosition("room1"));
        }
    }

    // ---- Connection Management ----

    @Nested
    @DisplayName("Connection Management")
    class ConnectionManagementTests {

        @Test
        @DisplayName("should connect two rooms")
        void connectRooms_shouldConnect() {
            Dungeon dungeon = new Dungeon("Mine", "desc");
            dungeon.addRoom("room1", createModule("Entrance"));
            dungeon.addRoom("room2", createModule("Corridor"));
            DungeonConnection conn = dungeon.connectRooms("room1", new M3space(5, 0, 5), "room2", new M3space(5, 9, 5));
            assertNotNull(conn);
            assertEquals(1, dungeon.getConnections().size());
        }

        @Test
        @DisplayName("should throw on non-existent fromRoom")
        void connectRooms_nonExistentFrom() {
            Dungeon dungeon = new Dungeon("Mine", "desc");
            dungeon.addRoom("room2", createModule("Corridor"));
            assertThrows(IllegalArgumentException.class, () ->
                    dungeon.connectRooms("nonexistent", new M3space(0, 0, 0), "room2", new M3space(0, 0, 0)));
        }

        @Test
        @DisplayName("should throw on non-existent toRoom")
        void connectRooms_nonExistentTo() {
            Dungeon dungeon = new Dungeon("Mine", "desc");
            dungeon.addRoom("room1", createModule("Entrance"));
            assertThrows(IllegalArgumentException.class, () ->
                    dungeon.connectRooms("room1", new M3space(0, 0, 0), "nonexistent", new M3space(0, 0, 0)));
        }

        @Test
        @DisplayName("should remove a connection")
        void removeConnection_shouldRemove() {
            Dungeon dungeon = new Dungeon("Mine", "desc");
            dungeon.addRoom("room1", createModule("Entrance"));
            dungeon.addRoom("room2", createModule("Corridor"));
            DungeonConnection conn = dungeon.connectRooms("room1", new M3space(5, 0, 5), "room2", new M3space(5, 9, 5));
            assertTrue(dungeon.removeConnection(conn));
            assertEquals(0, dungeon.getConnections().size());
        }

        @Test
        @DisplayName("should return false when removing non-existent connection")
        void removeConnection_nonExistent() {
            Dungeon dungeon = new Dungeon("Mine", "desc");
            DungeonConnection conn = new DungeonConnection("a", new M3space(0, 0, 0), "b", new M3space(0, 0, 0));
            assertFalse(dungeon.removeConnection(conn));
        }

        @Test
        @DisplayName("should return unmodifiable connections list")
        void getConnections_unmodifiable() {
            Dungeon dungeon = new Dungeon("Mine", "desc");
            dungeon.addRoom("room1", createModule("Entrance"));
            dungeon.addRoom("room2", createModule("Corridor"));
            dungeon.connectRooms("room1", new M3space(5, 0, 5), "room2", new M3space(5, 9, 5));
            assertThrows(UnsupportedOperationException.class, () -> dungeon.getConnections().add(
                    new DungeonConnection("a", new M3space(0, 0, 0), "b", new M3space(0, 0, 0))));
        }

        @Test
        @DisplayName("should get adjacent rooms")
        void getAdjacentRooms_shouldReturn() {
            Dungeon dungeon = new Dungeon("Mine", "desc");
            dungeon.addRoom("room1", createModule("Entrance"));
            dungeon.addRoom("room2", createModule("Corridor"));
            dungeon.addRoom("room3", createModule("Treasure"));
            dungeon.connectRooms("room1", new M3space(5, 0, 5), "room2", new M3space(5, 9, 5));
            dungeon.connectRooms("room1", new M3space(5, 9, 5), "room3", new M3space(5, 0, 5));
            Map<String, HullModule> adjacent = dungeon.getAdjacentRooms("room1");
            assertEquals(2, adjacent.size());
            assertTrue(adjacent.containsKey("room2"));
            assertTrue(adjacent.containsKey("room3"));
        }

        @Test
        @DisplayName("should return empty map for isolated room")
        void getAdjacentRooms_isolated() {
            Dungeon dungeon = new Dungeon("Mine", "desc");
            dungeon.addRoom("room1", createModule("Entrance"));
            Map<String, HullModule> adjacent = dungeon.getAdjacentRooms("room1");
            assertTrue(adjacent.isEmpty());
        }

        @Test
        @DisplayName("should get room connections")
        void getRoomConnections_shouldReturn() {
            Dungeon dungeon = new Dungeon("Mine", "desc");
            dungeon.addRoom("room1", createModule("Entrance"));
            dungeon.addRoom("room2", createModule("Corridor"));
            dungeon.addRoom("room3", createModule("Treasure"));
            dungeon.connectRooms("room1", new M3space(5, 0, 5), "room2", new M3space(5, 9, 5));
            dungeon.connectRooms("room1", new M3space(5, 9, 5), "room3", new M3space(5, 0, 5));
            List<DungeonConnection> conns = dungeon.getRoomConnections("room1");
            assertEquals(2, conns.size());
        }

        @Test
        @DisplayName("should return empty list for isolated room connections")
        void getRoomConnections_isolated() {
            Dungeon dungeon = new Dungeon("Mine", "desc");
            dungeon.addRoom("room1", createModule("Entrance"));
            List<DungeonConnection> conns = dungeon.getRoomConnections("room1");
            assertTrue(conns.isEmpty());
        }
    }

    // ---- Pathfinding ----

    @Nested
    @DisplayName("Pathfinding")
    class PathfindingTests {

        @Test
        @DisplayName("should find path between connected rooms")
        void findPath_connected() {
            Dungeon dungeon = new Dungeon("Mine", "desc");
            dungeon.addRoom("room1", createModule("Entrance"));
            dungeon.addRoom("room2", createModule("Corridor"));
            dungeon.addRoom("room3", createModule("Treasure"));
            dungeon.connectRooms("room1", new M3space(5, 0, 5), "room2", new M3space(5, 9, 5));
            dungeon.connectRooms("room2", new M3space(5, 0, 5), "room3", new M3space(5, 9, 5));
            List<String> path = dungeon.findPath("room1", "room3");
            assertNotNull(path);
            assertEquals(3, path.size());
            assertEquals("room1", path.get(0));
            assertEquals("room2", path.get(1));
            assertEquals("room3", path.get(2));
        }

        @Test
        @DisplayName("should return single element path for same room")
        void findPath_sameRoom() {
            Dungeon dungeon = new Dungeon("Mine", "desc");
            dungeon.addRoom("room1", createModule("Entrance"));
            List<String> path = dungeon.findPath("room1", "room1");
            assertNotNull(path);
            assertEquals(1, path.size());
            assertEquals("room1", path.get(0));
        }

        @Test
        @DisplayName("should return null for disconnected rooms")
        void findPath_disconnected() {
            Dungeon dungeon = new Dungeon("Mine", "desc");
            dungeon.addRoom("room1", createModule("Entrance"));
            dungeon.addRoom("room2", createModule("Isolated"));
            List<String> path = dungeon.findPath("room1", "room2");
            assertNull(path);
        }

        @Test
        @DisplayName("should throw on non-existent fromRoom")
        void findPath_nonExistentFrom() {
            Dungeon dungeon = new Dungeon("Mine", "desc");
            dungeon.addRoom("room1", createModule("Entrance"));
            assertThrows(IllegalArgumentException.class, () -> dungeon.findPath("nonexistent", "room1"));
        }

        @Test
        @DisplayName("should throw on non-existent toRoom")
        void findPath_nonExistentTo() {
            Dungeon dungeon = new Dungeon("Mine", "desc");
            dungeon.addRoom("room1", createModule("Entrance"));
            assertThrows(IllegalArgumentException.class, () -> dungeon.findPath("room1", "nonexistent"));
        }

        @Test
        @DisplayName("should find shortest path via BFS")
        void findPath_shortest() {
            Dungeon dungeon = new Dungeon("Mine", "desc");
            dungeon.addRoom("A", createModule("A"));
            dungeon.addRoom("B", createModule("B"));
            dungeon.addRoom("C", createModule("C"));
            dungeon.addRoom("D", createModule("D"));
            // A-B-C-D (long path)
            dungeon.connectRooms("A", new M3space(0, 0, 0), "B", new M3space(0, 0, 0));
            dungeon.connectRooms("B", new M3space(0, 0, 0), "C", new M3space(0, 0, 0));
            dungeon.connectRooms("C", new M3space(0, 0, 0), "D", new M3space(0, 0, 0));
            // A-D (short path)
            dungeon.connectRooms("A", new M3space(5, 0, 0), "D", new M3space(5, 0, 0));
            List<String> path = dungeon.findPath("A", "D");
            assertNotNull(path);
            assertEquals(2, path.size());
            assertEquals("A", path.get(0));
            assertEquals("D", path.get(1));
        }
    }

    // ---- Cell Lookup ----

    @Nested
    @DisplayName("Cell Lookup")
    class CellLookupTests {

        @Test
        @DisplayName("should get cell at coordinates")
        void getCellAt_valid() {
            Dungeon dungeon = new Dungeon("Mine", "desc");
            dungeon.addRoom("room1", createModule("Entrance"));
            M3space cell = dungeon.getCellAt("room1", 5, 5, 5);
            assertNotNull(cell);
        }

        @Test
        @DisplayName("should return null for non-existent room")
        void getCellAt_nonExistentRoom() {
            Dungeon dungeon = new Dungeon("Mine", "desc");
            assertNull(dungeon.getCellAt("nonexistent", 0, 0, 0));
        }
    }

    // ---- Compartment Management ----

    @Nested
    @DisplayName("Compartment Management")
    class CompartmentManagementTests {

        @Test
        @DisplayName("should get compartments across all rooms")
        void getCompartments_shouldReturn() {
            Dungeon dungeon = new Dungeon("Mine", "desc");
            HullModule m1 = createModule("Entrance");
            Compartment comp = new Compartment("Entrance", CompartmentType.COMMAND);
            m1.addCompartment(comp);
            dungeon.addRoom("room1", m1);
            Map<String, List<Compartment>> comps = dungeon.getCompartments();
            assertEquals(1, comps.size());
            assertTrue(comps.containsKey("room1"));
            assertEquals(1, comps.get("room1").size());
        }

        @Test
        @DisplayName("should return empty map when no compartments")
        void getCompartments_empty() {
            Dungeon dungeon = new Dungeon("Mine", "desc");
            dungeon.addRoom("room1", createModule("Entrance"));
            Map<String, List<Compartment>> comps = dungeon.getCompartments();
            assertTrue(comps.isEmpty());
        }

        @Test
        @DisplayName("should get compartment at coordinates")
        void getCompartmentAt_valid() {
            Dungeon dungeon = new Dungeon("Mine", "desc");
            HullModule m1 = createModule("Entrance");
            M3space cell = new M3space(5, 5, 5);
            Compartment comp = new Compartment("Entrance", CompartmentType.COMMAND);
            comp.addCell(cell);
            m1.addCompartment(comp);
            dungeon.addRoom("room1", m1);
            Compartment found = dungeon.getCompartmentAt("room1", 5, 5, 5);
            assertNotNull(found);
        }

        @Test
        @DisplayName("should return null for non-existent room compartment")
        void getCompartmentAt_nonExistentRoom() {
            Dungeon dungeon = new Dungeon("Mine", "desc");
            assertNull(dungeon.getCompartmentAt("nonexistent", 0, 0, 0));
        }
    }

    // ---- Integrity ----

    @Nested
    @DisplayName("Integrity")
    class IntegrityTests {

        @Test
        @DisplayName("should return 1.0 for empty dungeon")
        void getIntegrity_empty() {
            Dungeon dungeon = new Dungeon("Mine", "desc");
            assertEquals(1.0, dungeon.getIntegrity(), 0.01);
        }

        @Test
        @DisplayName("should return 1.0 for rooms with no compartments")
        void getIntegrity_noCompartments() {
            Dungeon dungeon = new Dungeon("Mine", "desc");
            dungeon.addRoom("room1", createModule("Entrance"));
            assertEquals(1.0, dungeon.getIntegrity(), 0.01);
        }

        @Test
        @DisplayName("should return average integrity")
        void getIntegrity_average() {
            Dungeon dungeon = new Dungeon("Mine", "desc");
            HullModule m1 = createModule("Entrance");
            HullModule m2 = createModule("Corridor");
            // Get cells from the actual HullModules so hitpoints are properly initialized
            M3space cell1 = m1.getCell(0, 0, 0);
            M3space cell2 = m2.getCell(0, 0, 0);
            assertNotNull(cell1);
            assertNotNull(cell2);
            Compartment comp1 = new Compartment("C1", CompartmentType.COMMAND);
            Compartment comp2 = new Compartment("C2", CompartmentType.TRANSIT);
            comp1.addCell(cell1);
            comp2.addCell(cell2);
            // Damage cell1 to 50% integrity
            int maxHp = cell1.getMaxHitpoints();
            if (maxHp > 0) {
                cell1.setHitpoints(maxHp / 2);
            }
            // cell2 remains at full hitpoints
            m1.addCompartment(comp1);
            m2.addCompartment(comp2);
            dungeon.addRoom("room1", m1);
            dungeon.addRoom("room2", m2);
            double integrity = dungeon.getIntegrity();
            // room1 has damaged cell (~50%), room2 has full cell (100%)
            // overall should be between 0.5 and 1.0
            assertTrue(integrity > 0.4 && integrity < 1.0, "Expected integrity between 0.4 and 1.0, got: " + integrity);
        }
    }

    // ---- Difficulty ----

    @Nested
    @DisplayName("Difficulty")
    class DifficultyTests {

        @Test
        @DisplayName("should set difficulty")
        void setDifficulty_valid() {
            Dungeon dungeon = new Dungeon("Mine", "desc");
            dungeon.setDifficulty(5);
            assertEquals(5, dungeon.getDifficulty());
        }

        @Test
        @DisplayName("should clamp difficulty to minimum 1")
        void setDifficulty_belowMin() {
            Dungeon dungeon = new Dungeon("Mine", "desc");
            dungeon.setDifficulty(0);
            assertEquals(1, dungeon.getDifficulty());
        }

        @Test
        @DisplayName("should clamp difficulty to maximum 10")
        void setDifficulty_aboveMax() {
            Dungeon dungeon = new Dungeon("Mine", "desc");
            dungeon.setDifficulty(15);
            assertEquals(10, dungeon.getDifficulty());
        }

        @Test
        @DisplayName("should clamp negative difficulty to 1")
        void setDifficulty_negative() {
            Dungeon dungeon = new Dungeon("Mine", "desc");
            dungeon.setDifficulty(-5);
            assertEquals(1, dungeon.getDifficulty());
        }
    }

    // ---- ToString ----

    @Nested
    @DisplayName("ToString")
    class ToStringTests {

        @Test
        @DisplayName("should produce meaningful string representation")
        void toString_shouldProduceOutput() {
            Dungeon dungeon = new Dungeon("Dwarven Mine", "desc");
            dungeon.addRoom("room1", createModule("Entrance"));
            String s = dungeon.toString();
            assertTrue(s.contains("Dwarven Mine"));
            assertTrue(s.contains("rooms=1"));
            assertTrue(s.contains("connections=0"));
            assertTrue(s.contains("difficulty=1"));
        }
    }

    // ---- Point3D ----

    @Nested
    @DisplayName("Point3D")
    class Point3DTests {

        @Test
        @DisplayName("should create point with coordinates")
        void constructor_shouldCreate() {
            Dungeon.Point3D p = new Dungeon.Point3D(1, 2, 3);
            assertEquals(1, p.getX());
            assertEquals(2, p.getY());
            assertEquals(3, p.getZ());
        }

        @Test
        @DisplayName("should be equal with same coordinates")
        void equals_same() {
            Dungeon.Point3D p1 = new Dungeon.Point3D(1, 2, 3);
            Dungeon.Point3D p2 = new Dungeon.Point3D(1, 2, 3);
            assertEquals(p1, p2);
        }

        @Test
        @DisplayName("should not be equal with different coordinates")
        void equals_different() {
            Dungeon.Point3D p1 = new Dungeon.Point3D(1, 2, 3);
            Dungeon.Point3D p2 = new Dungeon.Point3D(4, 5, 6);
            assertNotEquals(p1, p2);
        }

        @Test
        @DisplayName("should have consistent hashCode")
        void hashCode_consistent() {
            Dungeon.Point3D p1 = new Dungeon.Point3D(1, 2, 3);
            Dungeon.Point3D p2 = new Dungeon.Point3D(1, 2, 3);
            assertEquals(p1.hashCode(), p2.hashCode());
        }

        @Test
        @DisplayName("should produce meaningful toString")
        void toString_shouldProduceOutput() {
            Dungeon.Point3D p = new Dungeon.Point3D(10, 20, 30);
            String s = p.toString();
            assertEquals("(10,20,30)", s);
        }
    }
}
