package com.yisy.modules.doramcu.hex;

import org.junit.jupiter.api.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("BuildingHull Graph Connections Tests")
class BuildingHullGraphTest {

    private BuildingHull building;

    @BeforeEach
    void setUp() {
        building = new BuildingHull("Test Building");
    }

    @Nested
    @DisplayName("Floor Registration")
    class FloorRegistrationTests {

        @Test
        @DisplayName("should register a floor")
        void registerFloor_shouldRegister() {
            HullModule floor = new HullModule("Floor 1", 10, 10, 3);
            building.registerFloor("floor_0", floor);
            assertEquals(floor, building.getFloorById("floor_0"));
        }

        @Test
        @DisplayName("should throw on null floor ID")
        void registerFloor_nullId() {
            HullModule floor = new HullModule("Floor 1", 10, 10, 3);
            assertThrows(IllegalArgumentException.class, () -> building.registerFloor(null, floor));
        }

        @Test
        @DisplayName("should throw on blank floor ID")
        void registerFloor_blankId() {
            HullModule floor = new HullModule("Floor 1", 10, 10, 3);
            assertThrows(IllegalArgumentException.class, () -> building.registerFloor("  ", floor));
        }

        @Test
        @DisplayName("should throw on null floor")
        void registerFloor_nullFloor() {
            assertThrows(IllegalArgumentException.class, () -> building.registerFloor("floor_0", null));
        }
    }

    @Nested
    @DisplayName("Floor Connections")
    class ConnectionTests {

        @Test
        @DisplayName("should connect two floors")
        void connectFloors_shouldConnect() {
            HullModule f1 = new HullModule("Floor 1", 10, 10, 3);
            HullModule f2 = new HullModule("Floor 2", 10, 10, 3);
            building.registerFloor("floor_0", f1);
            building.registerFloor("floor_1", f2);

            BuildingHull.BuildingConnection conn = building.connectFloors("floor_0", new M3space(5, 5, 2), "floor_1", new M3space(5, 5, 0));
            assertNotNull(conn);
            assertEquals(1, building.getConnections().size());
        }

        @Test
        @DisplayName("should throw when from floor not found")
        void connectFloors_fromNotFound() {
            HullModule f2 = new HullModule("Floor 2", 10, 10, 3);
            building.registerFloor("floor_1", f2);
            assertThrows(IllegalArgumentException.class, () ->
                    building.connectFloors("missing", new M3space(0, 0, 0), "floor_1", new M3space(0, 0, 0)));
        }

        @Test
        @DisplayName("should throw when to floor not found")
        void connectFloors_toNotFound() {
            HullModule f1 = new HullModule("Floor 1", 10, 10, 3);
            building.registerFloor("floor_0", f1);
            assertThrows(IllegalArgumentException.class, () ->
                    building.connectFloors("floor_0", new M3space(0, 0, 0), "missing", new M3space(0, 0, 0)));
        }

        @Test
        @DisplayName("should return unmodifiable connections list")
        void getConnections_unmodifiable() {
            List<BuildingHull.BuildingConnection> conns = building.getConnections();
            assertThrows(UnsupportedOperationException.class, () -> conns.add(null));
        }
    }

    @Nested
    @DisplayName("Adjacent Floors")
    class AdjacentFloorsTests {

        @Test
        @DisplayName("should return adjacent floors")
        void getAdjacentFloors_shouldReturn() {
            HullModule f1 = new HullModule("Floor 1", 10, 10, 3);
            HullModule f2 = new HullModule("Floor 2", 10, 10, 3);
            HullModule f3 = new HullModule("Floor 3", 10, 10, 3);
            building.registerFloor("floor_0", f1);
            building.registerFloor("floor_1", f2);
            building.registerFloor("floor_2", f3);
            building.connectFloors("floor_0", new M3space(5, 5, 2), "floor_1", new M3space(5, 5, 0));
            building.connectFloors("floor_1", new M3space(5, 5, 2), "floor_2", new M3space(5, 5, 0));

            Map<String, HullModule> adjacent = building.getAdjacentFloors("floor_1");
            assertEquals(2, adjacent.size());
            assertTrue(adjacent.containsKey("floor_0"));
            assertTrue(adjacent.containsKey("floor_2"));
        }

        @Test
        @DisplayName("should return empty map for isolated floor")
        void getAdjacentFloors_isolated() {
            HullModule f1 = new HullModule("Floor 1", 10, 10, 3);
            building.registerFloor("floor_0", f1);
            Map<String, HullModule> adjacent = building.getAdjacentFloors("floor_0");
            assertTrue(adjacent.isEmpty());
        }
    }

    @Nested
    @DisplayName("Pathfinding (BFS)")
    class PathfindingTests {

        @Test
        @DisplayName("should find path between connected floors")
        void findPath_connected() {
            HullModule f1 = new HullModule("Floor 1", 10, 10, 3);
            HullModule f2 = new HullModule("Floor 2", 10, 10, 3);
            building.registerFloor("floor_0", f1);
            building.registerFloor("floor_1", f2);
            building.connectFloors("floor_0", new M3space(5, 5, 2), "floor_1", new M3space(5, 5, 0));

            List<String> path = building.findPath("floor_0", "floor_1");
            assertNotNull(path);
            assertEquals(2, path.size());
            assertEquals("floor_0", path.get(0));
            assertEquals("floor_1", path.get(1));
        }

        @Test
        @DisplayName("should find path through intermediate floors")
        void findPath_throughIntermediate() {
            HullModule f1 = new HullModule("Floor 1", 10, 10, 3);
            HullModule f2 = new HullModule("Floor 2", 10, 10, 3);
            HullModule f3 = new HullModule("Floor 3", 10, 10, 3);
            building.registerFloor("floor_0", f1);
            building.registerFloor("floor_1", f2);
            building.registerFloor("floor_2", f3);
            building.connectFloors("floor_0", new M3space(5, 5, 2), "floor_1", new M3space(5, 5, 0));
            building.connectFloors("floor_1", new M3space(5, 5, 2), "floor_2", new M3space(5, 5, 0));

            List<String> path = building.findPath("floor_0", "floor_2");
            assertNotNull(path);
            assertEquals(3, path.size());
            assertEquals("floor_0", path.get(0));
            assertEquals("floor_1", path.get(1));
            assertEquals("floor_2", path.get(2));
        }

        @Test
        @DisplayName("should return null for disconnected floors")
        void findPath_disconnected() {
            HullModule f1 = new HullModule("Floor 1", 10, 10, 3);
            HullModule f2 = new HullModule("Floor 2", 10, 10, 3);
            building.registerFloor("floor_0", f1);
            building.registerFloor("floor_1", f2);

            List<String> path = building.findPath("floor_0", "floor_1");
            assertNull(path);
        }

        @Test
        @DisplayName("should return path of length 1 for same floor")
        void findPath_sameFloor() {
            HullModule f1 = new HullModule("Floor 1", 10, 10, 3);
            building.registerFloor("floor_0", f1);

            List<String> path = building.findPath("floor_0", "floor_0");
            assertNotNull(path);
            assertEquals(1, path.size());
            assertEquals("floor_0", path.get(0));
        }

        @Test
        @DisplayName("should throw when from floor not found")
        void findPath_fromNotFound() {
            assertThrows(IllegalArgumentException.class, () -> building.findPath("missing", "floor_1"));
        }
    }

    @Nested
    @DisplayName("Connectivity Checks")
    class ConnectivityTests {

        @Test
        @DisplayName("should detect connected floors")
        void isConnected_connected() {
            HullModule f1 = new HullModule("Floor 1", 10, 10, 3);
            HullModule f2 = new HullModule("Floor 2", 10, 10, 3);
            building.registerFloor("floor_0", f1);
            building.registerFloor("floor_1", f2);
            building.connectFloors("floor_0", new M3space(5, 5, 2), "floor_1", new M3space(5, 5, 0));

            assertTrue(building.isConnected("floor_0", "floor_1"));
        }

        @Test
        @DisplayName("should detect disconnected floors")
        void isConnected_disconnected() {
            HullModule f1 = new HullModule("Floor 1", 10, 10, 3);
            HullModule f2 = new HullModule("Floor 2", 10, 10, 3);
            building.registerFloor("floor_0", f1);
            building.registerFloor("floor_1", f2);

            assertFalse(building.isConnected("floor_0", "floor_1"));
        }
    }

    @Nested
    @DisplayName("BuildingConnection")
    class BuildingConnectionTests {

        @Test
        @DisplayName("should create valid connection")
        void constructor_valid() {
            M3space from = new M3space(5, 5, 2);
            M3space to = new M3space(5, 5, 0);
            BuildingHull.BuildingConnection conn = new BuildingHull.BuildingConnection("floor_0", from, "floor_1", to);
            assertEquals("floor_0", conn.getFromFloorId());
            assertEquals(from, conn.getFromCell());
            assertEquals("floor_1", conn.getToFloorId());
            assertEquals(to, conn.getToCell());
        }

        @Test
        @DisplayName("should return other floor ID")
        void getOtherFloorId() {
            BuildingHull.BuildingConnection conn = new BuildingHull.BuildingConnection(
                    "floor_0", new M3space(0, 0, 0), "floor_1", new M3space(0, 0, 0));
            assertEquals("floor_1", conn.getOtherFloorId("floor_0"));
            assertEquals("floor_0", conn.getOtherFloorId("floor_1"));
            assertNull(conn.getOtherFloorId("floor_2"));
        }

        @Test
        @DisplayName("should detect floor involvement")
        void involvesFloor() {
            BuildingHull.BuildingConnection conn = new BuildingHull.BuildingConnection(
                    "floor_0", new M3space(0, 0, 0), "floor_1", new M3space(0, 0, 0));
            assertTrue(conn.involvesFloor("floor_0"));
            assertTrue(conn.involvesFloor("floor_1"));
            assertFalse(conn.involvesFloor("floor_2"));
        }

        @Test
        @DisplayName("should throw on null fromFloorId")
        void constructor_nullFromFloorId() {
            assertThrows(IllegalArgumentException.class, () ->
                    new BuildingHull.BuildingConnection(null, new M3space(0, 0, 0), "floor_1", new M3space(0, 0, 0)));
        }

        @Test
        @DisplayName("should throw on null fromCell")
        void constructor_nullFromCell() {
            assertThrows(IllegalArgumentException.class, () ->
                    new BuildingHull.BuildingConnection("floor_0", null, "floor_1", new M3space(0, 0, 0)));
        }

        @Test
        @DisplayName("should produce meaningful toString")
        void toString_shouldProduceOutput() {
            BuildingHull.BuildingConnection conn = new BuildingHull.BuildingConnection(
                    "floor_0", new M3space(5, 5, 2), "floor_1", new M3space(5, 5, 0));
            String s = conn.toString();
            assertTrue(s.contains("floor_0"));
            assertTrue(s.contains("floor_1"));
        }

        @Test
        @DisplayName("should use reference equality (no custom equals)")
        void referenceEquality() {
            BuildingHull.BuildingConnection c1 = new BuildingHull.BuildingConnection(
                    "floor_0", new M3space(0, 0, 0), "floor_1", new M3space(0, 0, 0));
            BuildingHull.BuildingConnection c2 = new BuildingHull.BuildingConnection(
                    "floor_0", new M3space(9, 9, 9), "floor_1", new M3space(9, 9, 9));
            // BuildingConnection does not override equals, so different instances are not equal
            assertNotEquals(c1, c2);
            assertEquals(c1, c1);
        }
    }
}
