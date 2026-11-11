package com.yisy.modules.doramcu.hex;

import org.junit.jupiter.api.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ShipHull Graph Connections Tests")
class ShipHullGraphTest {

    private Ship ship;
    private ShipHull hull;

    @BeforeEach
    void setUp() {
        ship = new Ship(0, ShipType.FRIGATE);
        hull = new ShipHull(ship);
    }

    @Nested
    @DisplayName("Module Registration")
    class ModuleRegistrationTests {

        @Test
        @DisplayName("should register a module")
        void registerModule_shouldRegister() {
            HullModule module = new HullModule("Bridge", 10, 10, 10);
            hull.registerModule("bridge", module);
            assertEquals(module, hull.getModuleById("bridge"));
        }

        @Test
        @DisplayName("should throw on null module ID")
        void registerModule_nullId() {
            HullModule module = new HullModule("Bridge", 10, 10, 10);
            assertThrows(IllegalArgumentException.class, () -> hull.registerModule(null, module));
        }

        @Test
        @DisplayName("should throw on blank module ID")
        void registerModule_blankId() {
            HullModule module = new HullModule("Bridge", 10, 10, 10);
            assertThrows(IllegalArgumentException.class, () -> hull.registerModule("  ", module));
        }

        @Test
        @DisplayName("should throw on null module")
        void registerModule_nullModule() {
            assertThrows(IllegalArgumentException.class, () -> hull.registerModule("bridge", null));
        }
    }

    @Nested
    @DisplayName("Module Connections")
    class ConnectionTests {

        @Test
        @DisplayName("should connect two modules")
        void connectModules_shouldConnect() {
            HullModule m1 = new HullModule("Bridge", 10, 10, 10);
            HullModule m2 = new HullModule("Engine", 10, 10, 10);
            hull.registerModule("bridge", m1);
            hull.registerModule("engine", m2);

            ShipConnection conn = hull.connectModules("bridge", new M3space(5, 0, 5), "engine", new M3space(5, 9, 5));
            assertNotNull(conn);
            assertEquals(1, hull.getConnections().size());
        }

        @Test
        @DisplayName("should throw when from module not found")
        void connectModules_fromNotFound() {
            HullModule m2 = new HullModule("Engine", 10, 10, 10);
            hull.registerModule("engine", m2);
            assertThrows(IllegalArgumentException.class, () ->
                    hull.connectModules("missing", new M3space(0, 0, 0), "engine", new M3space(0, 0, 0)));
        }

        @Test
        @DisplayName("should throw when to module not found")
        void connectModules_toNotFound() {
            HullModule m1 = new HullModule("Bridge", 10, 10, 10);
            hull.registerModule("bridge", m1);
            assertThrows(IllegalArgumentException.class, () ->
                    hull.connectModules("bridge", new M3space(0, 0, 0), "missing", new M3space(0, 0, 0)));
        }

        @Test
        @DisplayName("should remove connection")
        void removeConnection_shouldRemove() {
            HullModule m1 = new HullModule("Bridge", 10, 10, 10);
            HullModule m2 = new HullModule("Engine", 10, 10, 10);
            hull.registerModule("bridge", m1);
            hull.registerModule("engine", m2);
            ShipConnection conn = hull.connectModules("bridge", new M3space(5, 0, 5), "engine", new M3space(5, 9, 5));
            assertTrue(hull.removeConnection(conn));
            assertEquals(0, hull.getConnections().size());
        }

        @Test
        @DisplayName("should return unmodifiable connections list")
        void getConnections_unmodifiable() {
            List<ShipConnection> conns = hull.getConnections();
            assertThrows(UnsupportedOperationException.class, () -> conns.add(null));
        }
    }

    @Nested
    @DisplayName("Adjacent Modules")
    class AdjacentModulesTests {

        @Test
        @DisplayName("should return adjacent modules")
        void getAdjacentModules_shouldReturn() {
            HullModule m1 = new HullModule("Bridge", 10, 10, 10);
            HullModule m2 = new HullModule("Engine", 10, 10, 10);
            HullModule m3 = new HullModule("Cargo", 10, 10, 10);
            hull.registerModule("bridge", m1);
            hull.registerModule("engine", m2);
            hull.registerModule("cargo", m3);
            hull.connectModules("bridge", new M3space(5, 0, 5), "engine", new M3space(5, 9, 5));
            hull.connectModules("bridge", new M3space(5, 9, 5), "cargo", new M3space(5, 0, 5));

            Map<String, HullModule> adjacent = hull.getAdjacentModules("bridge");
            assertEquals(2, adjacent.size());
            assertTrue(adjacent.containsKey("engine"));
            assertTrue(adjacent.containsKey("cargo"));
        }

        @Test
        @DisplayName("should return empty map for isolated module")
        void getAdjacentModules_isolated() {
            HullModule m1 = new HullModule("Bridge", 10, 10, 10);
            hull.registerModule("bridge", m1);
            Map<String, HullModule> adjacent = hull.getAdjacentModules("bridge");
            assertTrue(adjacent.isEmpty());
        }
    }

    @Nested
    @DisplayName("Pathfinding (BFS)")
    class PathfindingTests {

        @Test
        @DisplayName("should find path between connected modules")
        void findPath_connected() {
            HullModule m1 = new HullModule("Bridge", 10, 10, 10);
            HullModule m2 = new HullModule("Engine", 10, 10, 10);
            hull.registerModule("bridge", m1);
            hull.registerModule("engine", m2);
            hull.connectModules("bridge", new M3space(5, 0, 5), "engine", new M3space(5, 9, 5));

            List<String> path = hull.findPath("bridge", "engine");
            assertNotNull(path);
            assertEquals(2, path.size());
            assertEquals("bridge", path.get(0));
            assertEquals("engine", path.get(1));
        }

        @Test
        @DisplayName("should find path through intermediate modules")
        void findPath_throughIntermediate() {
            HullModule m1 = new HullModule("Bridge", 10, 10, 10);
            HullModule m2 = new HullModule("Corridor", 10, 10, 10);
            HullModule m3 = new HullModule("Engine", 10, 10, 10);
            hull.registerModule("bridge", m1);
            hull.registerModule("corridor", m2);
            hull.registerModule("engine", m3);
            hull.connectModules("bridge", new M3space(5, 0, 5), "corridor", new M3space(5, 9, 5));
            hull.connectModules("corridor", new M3space(5, 0, 5), "engine", new M3space(5, 9, 5));

            List<String> path = hull.findPath("bridge", "engine");
            assertNotNull(path);
            assertEquals(3, path.size());
            assertEquals("bridge", path.get(0));
            assertEquals("corridor", path.get(1));
            assertEquals("engine", path.get(2));
        }

        @Test
        @DisplayName("should return null for disconnected modules")
        void findPath_disconnected() {
            HullModule m1 = new HullModule("Bridge", 10, 10, 10);
            HullModule m2 = new HullModule("Engine", 10, 10, 10);
            hull.registerModule("bridge", m1);
            hull.registerModule("engine", m2);

            List<String> path = hull.findPath("bridge", "engine");
            assertNull(path);
        }

        @Test
        @DisplayName("should return path of length 1 for same module")
        void findPath_sameModule() {
            HullModule m1 = new HullModule("Bridge", 10, 10, 10);
            hull.registerModule("bridge", m1);

            List<String> path = hull.findPath("bridge", "bridge");
            assertNotNull(path);
            assertEquals(1, path.size());
            assertEquals("bridge", path.get(0));
        }

        @Test
        @DisplayName("should throw when from module not found")
        void findPath_fromNotFound() {
            assertThrows(IllegalArgumentException.class, () -> hull.findPath("missing", "engine"));
        }

        @Test
        @DisplayName("should throw when to module not found")
        void findPath_toNotFound() {
            HullModule m1 = new HullModule("Bridge", 10, 10, 10);
            hull.registerModule("bridge", m1);
            assertThrows(IllegalArgumentException.class, () -> hull.findPath("bridge", "missing"));
        }
    }

    @Nested
    @DisplayName("Connectivity Checks")
    class ConnectivityTests {

        @Test
        @DisplayName("should detect connected modules")
        void isConnected_connected() {
            HullModule m1 = new HullModule("Bridge", 10, 10, 10);
            HullModule m2 = new HullModule("Engine", 10, 10, 10);
            hull.registerModule("bridge", m1);
            hull.registerModule("engine", m2);
            hull.connectModules("bridge", new M3space(5, 0, 5), "engine", new M3space(5, 9, 5));

            assertTrue(hull.isConnected("bridge", "engine"));
        }

        @Test
        @DisplayName("should detect disconnected modules")
        void isConnected_disconnected() {
            HullModule m1 = new HullModule("Bridge", 10, 10, 10);
            HullModule m2 = new HullModule("Engine", 10, 10, 10);
            hull.registerModule("bridge", m1);
            hull.registerModule("engine", m2);

            assertFalse(hull.isConnected("bridge", "engine"));
        }

        @Test
        @DisplayName("should detect indirect connectivity")
        void isConnected_indirect() {
            HullModule m1 = new HullModule("Bridge", 10, 10, 10);
            HullModule m2 = new HullModule("Corridor", 10, 10, 10);
            HullModule m3 = new HullModule("Engine", 10, 10, 10);
            hull.registerModule("bridge", m1);
            hull.registerModule("corridor", m2);
            hull.registerModule("engine", m3);
            hull.connectModules("bridge", new M3space(5, 0, 5), "corridor", new M3space(5, 9, 5));
            hull.connectModules("corridor", new M3space(5, 0, 5), "engine", new M3space(5, 9, 5));

            assertTrue(hull.isConnected("bridge", "engine"));
        }
    }

    @Nested
    @DisplayName("Reachable Modules")
    class ReachableModulesTests {

        @Test
        @DisplayName("should return all reachable modules")
        void getReachableModules_shouldReturn() {
            HullModule m1 = new HullModule("Bridge", 10, 10, 10);
            HullModule m2 = new HullModule("Corridor", 10, 10, 10);
            HullModule m3 = new HullModule("Engine", 10, 10, 10);
            hull.registerModule("bridge", m1);
            hull.registerModule("corridor", m2);
            hull.registerModule("engine", m3);
            hull.connectModules("bridge", new M3space(5, 0, 5), "corridor", new M3space(5, 9, 5));
            hull.connectModules("corridor", new M3space(5, 0, 5), "engine", new M3space(5, 9, 5));

            Set<String> reachable = hull.getReachableModules("bridge");
            assertEquals(3, reachable.size());
            assertTrue(reachable.contains("bridge"));
            assertTrue(reachable.contains("corridor"));
            assertTrue(reachable.contains("engine"));
        }

        @Test
        @DisplayName("should return only self for isolated module")
        void getReachableModules_isolated() {
            HullModule m1 = new HullModule("Bridge", 10, 10, 10);
            HullModule m2 = new HullModule("Engine", 10, 10, 10);
            hull.registerModule("bridge", m1);
            hull.registerModule("engine", m2);

            Set<String> reachable = hull.getReachableModules("bridge");
            assertEquals(1, reachable.size());
            assertTrue(reachable.contains("bridge"));
        }
    }

    @Nested
    @DisplayName("Connected Components")
    class ConnectedComponentsTests {

        @Test
        @DisplayName("should return 0 for empty hull")
        void getConnectedComponentCount_empty() {
            assertEquals(0, hull.getConnectedComponentCount());
        }

        @Test
        @DisplayName("should return 1 for fully connected hull")
        void getConnectedComponentCount_fullyConnected() {
            HullModule m1 = new HullModule("Bridge", 10, 10, 10);
            HullModule m2 = new HullModule("Engine", 10, 10, 10);
            hull.registerModule("bridge", m1);
            hull.registerModule("engine", m2);
            hull.connectModules("bridge", new M3space(5, 0, 5), "engine", new M3space(5, 9, 5));

            assertEquals(1, hull.getConnectedComponentCount());
        }

        @Test
        @DisplayName("should return 2 for two disconnected groups")
        void getConnectedComponentCount_twoGroups() {
            HullModule m1 = new HullModule("Bridge", 10, 10, 10);
            HullModule m2 = new HullModule("Engine", 10, 10, 10);
            HullModule m3 = new HullModule("Cargo", 10, 10, 10);
            HullModule m4 = new HullModule("Weapons", 10, 10, 10);
            hull.registerModule("bridge", m1);
            hull.registerModule("engine", m2);
            hull.registerModule("cargo", m3);
            hull.registerModule("weapons", m4);
            hull.connectModules("bridge", new M3space(5, 0, 5), "engine", new M3space(5, 9, 5));
            hull.connectModules("cargo", new M3space(5, 0, 5), "weapons", new M3space(5, 9, 5));

            assertEquals(2, hull.getConnectedComponentCount());
        }

        @Test
        @DisplayName("should return count equal to modules for all isolated")
        void getConnectedComponentCount_allIsolated() {
            HullModule m1 = new HullModule("Bridge", 10, 10, 10);
            HullModule m2 = new HullModule("Engine", 10, 10, 10);
            HullModule m3 = new HullModule("Cargo", 10, 10, 10);
            hull.registerModule("bridge", m1);
            hull.registerModule("engine", m2);
            hull.registerModule("cargo", m3);

            assertEquals(3, hull.getConnectedComponentCount());
        }
    }

    @Nested
    @DisplayName("Module Connections")
    class ModuleConnectionsTests {

        @Test
        @DisplayName("should return connections for a module")
        void getModuleConnections_shouldReturn() {
            HullModule m1 = new HullModule("Bridge", 10, 10, 10);
            HullModule m2 = new HullModule("Engine", 10, 10, 10);
            hull.registerModule("bridge", m1);
            hull.registerModule("engine", m2);
            hull.connectModules("bridge", new M3space(5, 0, 5), "engine", new M3space(5, 9, 5));

            List<ShipConnection> conns = hull.getModuleConnections("bridge");
            assertEquals(1, conns.size());
            assertTrue(conns.get(0).involvesModule("bridge"));
        }

        @Test
        @DisplayName("should return empty list for isolated module")
        void getModuleConnections_isolated() {
            HullModule m1 = new HullModule("Bridge", 10, 10, 10);
            hull.registerModule("bridge", m1);
            List<ShipConnection> conns = hull.getModuleConnections("bridge");
            assertTrue(conns.isEmpty());
        }
    }
}
