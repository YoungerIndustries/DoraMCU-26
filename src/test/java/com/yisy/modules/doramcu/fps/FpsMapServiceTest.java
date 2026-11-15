package com.yisy.modules.doramcu.fps;

import com.yisy.modules.doramcu.fps.core.model.FpsMap;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;

import static org.junit.jupiter.api.Assertions.*;

class FpsMapServiceTest {

    @Nested
    class DefaultMapsTests {
        @Test
        void initializesWithDefaultMaps() {
            FpsMapService service = new FpsMapService();
            assertEquals(2, service.getAllMaps().size());
        }

        @Test
        void defaultMapsIncludeArena() {
            FpsMapService service = new FpsMapService();
            FpsMap arena = service.getAllMaps().stream()
                    .filter(m -> "Arena".equals(m.getName()))
                    .findFirst().orElse(null);
            assertNotNull(arena);
            assertEquals(100.0, arena.getMapWidth());
            assertEquals(100.0, arena.getMapDepth());
        }

        @Test
        void defaultMapsIncludeCrossfire() {
            FpsMapService service = new FpsMapService();
            FpsMap crossfire = service.getAllMaps().stream()
                    .filter(m -> "Crossfire".equals(m.getName()))
                    .findFirst().orElse(null);
            assertNotNull(crossfire);
            assertEquals(150.0, crossfire.getMapWidth());
        }

        @Test
        void arenaHasSpawnPoints() {
            FpsMapService service = new FpsMapService();
            FpsMap arena = service.getAllMaps().stream()
                    .filter(m -> "Arena".equals(m.getName()))
                    .findFirst().orElse(null);
            assertNotNull(arena);
            assertEquals(4, arena.getSpawnPoints().size());
        }

        @Test
        void arenaHasCapturePoints() {
            FpsMapService service = new FpsMapService();
            FpsMap arena = service.getAllMaps().stream()
                    .filter(m -> "Arena".equals(m.getName()))
                    .findFirst().orElse(null);
            assertNotNull(arena);
            assertEquals(3, arena.getCapturePoints().size());
        }

        @Test
        void crossfireHasMoreSpawnPoints() {
            FpsMapService service = new FpsMapService();
            FpsMap crossfire = service.getAllMaps().stream()
                    .filter(m -> "Crossfire".equals(m.getName()))
                    .findFirst().orElse(null);
            assertNotNull(crossfire);
            assertEquals(6, crossfire.getSpawnPoints().size());
        }

        @Test
        void crossfireHasFiveCapturePoints() {
            FpsMapService service = new FpsMapService();
            FpsMap crossfire = service.getAllMaps().stream()
                    .filter(m -> "Crossfire".equals(m.getName()))
                    .findFirst().orElse(null);
            assertNotNull(crossfire);
            assertEquals(5, crossfire.getCapturePoints().size());
        }
    }

    @Nested
    class AddMapTests {
        @Test
        void addMapIncreasesCount() {
            FpsMapService service = new FpsMapService();
            int before = service.getAllMaps().size();
            FpsMap map = new FpsMap("Custom", 50, 50, 10);
            service.addMap(map);
            assertEquals(before + 1, service.getAllMaps().size());
        }

        @Test
        void addMapCanBeRetrieved() {
            FpsMapService service = new FpsMapService();
            FpsMap map = new FpsMap("Custom", 50, 50, 10);
            service.addMap(map);
            assertTrue(service.getMap(map.getId()).isPresent());
        }
    }

    @Nested
    class GetMapTests {
        @Test
        void getMapReturnsExistingMap() {
            FpsMapService service = new FpsMapService();
            FpsMap arena = service.getAllMaps().get(0);
            assertTrue(service.getMap(arena.getId()).isPresent());
            assertEquals(arena, service.getMap(arena.getId()).get());
        }

        @Test
        void getMapReturnsEmptyForUnknownId() {
            FpsMapService service = new FpsMapService();
            assertFalse(service.getMap("unknown").isPresent());
        }

        @Test
        void getMapReturnsEmptyForNullId() {
            FpsMapService service = new FpsMapService();
            assertFalse(service.getMap(null).isPresent());
        }
    }

    @Nested
    class RemoveMapTests {
        @Test
        void removeMapDecreasesCount() {
            FpsMapService service = new FpsMapService();
            FpsMap map = new FpsMap("Custom", 50, 50, 10);
            service.addMap(map);
            int before = service.getAllMaps().size();
            assertTrue(service.removeMap(map.getId()));
            assertEquals(before - 1, service.getAllMaps().size());
        }

        @Test
        void removeNonExistentMapReturnsFalse() {
            FpsMapService service = new FpsMapService();
            assertFalse(service.removeMap("unknown"));
        }

        @Test
        void removeMapThenGetReturnsEmpty() {
            FpsMapService service = new FpsMapService();
            FpsMap map = new FpsMap("Custom", 50, 50, 10);
            service.addMap(map);
            service.removeMap(map.getId());
            assertFalse(service.getMap(map.getId()).isPresent());
        }
    }

    @Nested
    class ThemeFilteringTests {
        @Test
        void getMapsByThemeReturnsEmptyForUnknownTheme() {
            FpsMapService service = new FpsMapService();
            assertTrue(service.getMapsByTheme("Unknown").isEmpty());
        }

        @Test
        void getMapsByThemeReturnsMapsWhenThemeSet() {
            FpsMapService service = new FpsMapService();
            FpsMap map = new FpsMap("SciFi Map", 50, 50, 10);
            map.setTheme("SciFi");
            service.addMap(map);
            assertEquals(1, service.getMapsByTheme("SciFi").size());
        }

        @Test
        void getMapsByThemeReturnsMultipleMaps() {
            FpsMapService service = new FpsMapService();
            FpsMap map1 = new FpsMap("SciFi 1", 50, 50, 10);
            map1.setTheme("SciFi");
            FpsMap map2 = new FpsMap("SciFi 2", 50, 50, 10);
            map2.setTheme("SciFi");
            service.addMap(map1);
            service.addMap(map2);
            assertEquals(2, service.getMapsByTheme("SciFi").size());
        }
    }

    @Nested
    class GetAllMapsTests {
        @Test
        void getAllMapsReturnsNewList() {
            FpsMapService service = new FpsMapService();
            java.util.List<FpsMap> list1 = service.getAllMaps();
            java.util.List<FpsMap> list2 = service.getAllMaps();
            assertNotSame(list1, list2);
        }
    }
}
