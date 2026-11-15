package com.yisy.modules.doramcu.fps.core.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;

import static org.junit.jupiter.api.Assertions.*;

class FpsMapTest {

    @Nested
    class ConstructorTests {
        @Test
        void defaultConstructorInitializesEmptyLists() {
            FpsMap map = new FpsMap();
            assertNotNull(map.getSpawnPoints());
            assertNotNull(map.getCollisionBoxes());
            assertNotNull(map.getPowerUpLocations());
            assertNotNull(map.getCapturePoints());
            assertTrue(map.getSpawnPoints().isEmpty());
            assertTrue(map.getCollisionBoxes().isEmpty());
            assertTrue(map.getPowerUpLocations().isEmpty());
            assertTrue(map.getCapturePoints().isEmpty());
        }

        @Test
        void fullConstructorGeneratesUuid() {
            FpsMap map = new FpsMap("Arena", 100, 100, 20);
            assertNotNull(map.getId());
            assertEquals("Arena", map.getName());
            assertEquals(100.0, map.getMapWidth());
            assertEquals(100.0, map.getMapDepth());
            assertEquals(20.0, map.getMapHeight());
        }

        @Test
        void fullConstructorInitializesEmptyLists() {
            FpsMap map = new FpsMap("Arena", 100, 100, 20);
            assertTrue(map.getSpawnPoints().isEmpty());
            assertTrue(map.getCollisionBoxes().isEmpty());
        }
    }

    @Nested
    class SpawnPointTests {
        @Test
        void spawnPointRecordCanBeCreated() {
            FpsMap.SpawnPoint sp = new FpsMap.SpawnPoint(10.0, 0.0, -10.0, 0);
            assertEquals(10.0, sp.x());
            assertEquals(0.0, sp.y());
            assertEquals(-10.0, sp.z());
            assertEquals(0, sp.team());
        }

        @Test
        void spawnPointsCanBeAdded() {
            FpsMap map = new FpsMap("Arena", 100, 100, 20);
            map.getSpawnPoints().add(new FpsMap.SpawnPoint(0, 0, 0, 0));
            map.getSpawnPoints().add(new FpsMap.SpawnPoint(50, 0, 50, 1));
            assertEquals(2, map.getSpawnPoints().size());
        }

        @Test
        void neutralSpawnPointHasTeamMinusOne() {
            FpsMap.SpawnPoint sp = new FpsMap.SpawnPoint(0, 0, 0, -1);
            assertEquals(-1, sp.team());
        }
    }

    @Nested
    class CollisionBoxTests {
        @Test
        void collisionBoxRecordCanBeCreated() {
            FpsMap.CollisionBox box = new FpsMap.CollisionBox(-10, 10, 0, 5, -10, 10);
            assertEquals(-10.0, box.minX());
            assertEquals(10.0, box.maxX());
            assertEquals(0.0, box.minY());
            assertEquals(5.0, box.maxY());
            assertEquals(-10.0, box.minZ());
            assertEquals(10.0, box.maxZ());
        }

        @Test
        void collisionBoxesCanBeAdded() {
            FpsMap map = new FpsMap("Arena", 100, 100, 20);
            map.getCollisionBoxes().add(new FpsMap.CollisionBox(-5, 5, 0, 10, -5, 5));
            assertEquals(1, map.getCollisionBoxes().size());
        }
    }

    @Nested
    class PowerUpLocationTests {
        @Test
        void powerUpLocationRecordCanBeCreated() {
            FpsMap.PowerUpLocation pu = new FpsMap.PowerUpLocation(0, 0, 0, "health");
            assertEquals(0.0, pu.x());
            assertEquals(0.0, pu.y());
            assertEquals(0.0, pu.z());
            assertEquals("health", pu.type());
        }

        @Test
        void powerUpLocationsCanBeAdded() {
            FpsMap map = new FpsMap("Arena", 100, 100, 20);
            map.getPowerUpLocations().add(new FpsMap.PowerUpLocation(0, 0, 0, "health"));
            map.getPowerUpLocations().add(new FpsMap.PowerUpLocation(50, 0, 0, "ammo"));
            assertEquals(2, map.getPowerUpLocations().size());
        }
    }

    @Nested
    class CapturePointTests {
        @Test
        void capturePointRecordCanBeCreated() {
            FpsMap.CapturePoint cp = new FpsMap.CapturePoint("A", 0, 0, 0, 10);
            assertEquals("A", cp.id());
            assertEquals(0.0, cp.x());
            assertEquals(0.0, cp.y());
            assertEquals(0.0, cp.z());
            assertEquals(10.0, cp.radius());
        }

        @Test
        void capturePointsCanBeAdded() {
            FpsMap map = new FpsMap("Arena", 100, 100, 20);
            map.getCapturePoints().add(new FpsMap.CapturePoint("A", 0, 0, 0, 10));
            map.getCapturePoints().add(new FpsMap.CapturePoint("B", 50, 0, 50, 12));
            assertEquals(2, map.getCapturePoints().size());
        }
    }

    @Nested
    class GetterSetterTests {
        @Test
        void idCanBeSet() {
            FpsMap map = new FpsMap();
            map.setId("custom-map");
            assertEquals("custom-map", map.getId());
        }

        @Test
        void nameCanBeSet() {
            FpsMap map = new FpsMap();
            map.setName("Doom Arena");
            assertEquals("Doom Arena", map.getName());
        }

        @Test
        void descriptionCanBeSet() {
            FpsMap map = new FpsMap();
            map.setDescription("A dark arena");
            assertEquals("A dark arena", map.getDescription());
        }

        @Test
        void mapWidthCanBeSet() {
            FpsMap map = new FpsMap();
            map.setMapWidth(200.0);
            assertEquals(200.0, map.getMapWidth());
        }

        @Test
        void mapDepthCanBeSet() {
            FpsMap map = new FpsMap();
            map.setMapDepth(200.0);
            assertEquals(200.0, map.getMapDepth());
        }

        @Test
        void mapHeightCanBeSet() {
            FpsMap map = new FpsMap();
            map.setMapHeight(50.0);
            assertEquals(50.0, map.getMapHeight());
        }

        @Test
        void themeCanBeSet() {
            FpsMap map = new FpsMap();
            map.setTheme("SciFi");
            assertEquals("SciFi", map.getTheme());
        }

        @Test
        void spawnPointsCanBeReplaced() {
            FpsMap map = new FpsMap();
            java.util.List<FpsMap.SpawnPoint> points = new java.util.ArrayList<>();
            points.add(new FpsMap.SpawnPoint(0, 0, 0, 0));
            map.setSpawnPoints(points);
            assertEquals(1, map.getSpawnPoints().size());
        }

        @Test
        void collisionBoxesCanBeReplaced() {
            FpsMap map = new FpsMap();
            java.util.List<FpsMap.CollisionBox> boxes = new java.util.ArrayList<>();
            boxes.add(new FpsMap.CollisionBox(-5, 5, 0, 10, -5, 5));
            map.setCollisionBoxes(boxes);
            assertEquals(1, map.getCollisionBoxes().size());
        }

        @Test
        void powerUpLocationsCanBeReplaced() {
            FpsMap map = new FpsMap();
            java.util.List<FpsMap.PowerUpLocation> locations = new java.util.ArrayList<>();
            locations.add(new FpsMap.PowerUpLocation(0, 0, 0, "health"));
            map.setPowerUpLocations(locations);
            assertEquals(1, map.getPowerUpLocations().size());
        }

        @Test
        void capturePointsCanBeReplaced() {
            FpsMap map = new FpsMap();
            java.util.List<FpsMap.CapturePoint> points = new java.util.ArrayList<>();
            points.add(new FpsMap.CapturePoint("A", 0, 0, 0, 10));
            map.setCapturePoints(points);
            assertEquals(1, map.getCapturePoints().size());
        }
    }

    @Nested
    class UniqueIdTests {
        @Test
        void twoMapsHaveDifferentIds() {
            FpsMap m1 = new FpsMap("A", 100, 100, 20);
            FpsMap m2 = new FpsMap("B", 100, 100, 20);
            assertNotEquals(m1.getId(), m2.getId());
        }
    }
}
