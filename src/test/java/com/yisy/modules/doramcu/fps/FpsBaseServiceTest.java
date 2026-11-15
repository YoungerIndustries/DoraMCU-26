package com.yisy.modules.doramcu.fps;

import com.yisy.modules.doramcu.fps.core.model.FpsEnemy;
import com.yisy.modules.doramcu.fps.core.model.FpsMap;
import com.yisy.modules.doramcu.fps.core.model.FpsWeapon;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FpsBaseServiceTest {

    static class TestFpsService extends FpsBaseService {
        @Override
        public String getTheme() {
            return "TestTheme";
        }

        @Override
        public List<FpsWeapon> getWeapons() {
            return List.of(
                    new FpsWeapon("Test Pistol", FpsWeapon.WeaponType.PISTOL, 30, 5.0, 1.5, 20),
                    new FpsWeapon("Test Rifle", FpsWeapon.WeaponType.RIFLE, 40, 8.0, 2.0, 40)
            );
        }

        @Override
        public List<FpsEnemy> getEnemies() {
            return List.of(
                    new FpsEnemy("Test Soldier", 100, 3.0, FpsEnemy.EnemyBehavior.PATROL, 20),
                    new FpsEnemy("Test Elite", 200, 2.0, FpsEnemy.EnemyBehavior.ATTACK, 40)
            );
        }

        @Override
        public List<FpsMap> getMaps() {
            FpsMap map = new FpsMap("Test Arena", 100, 100, 20);
            map.getSpawnPoints().add(new FpsMap.SpawnPoint(-40, 0, -40, 0));
            map.getSpawnPoints().add(new FpsMap.SpawnPoint(40, 0, 40, 1));
            return List.of(map);
        }
    }

    @Nested
    class ThemeTests {
        @Test
        void getThemeReturnsThemeName() {
            FpsBaseService service = new TestFpsService();
            assertEquals("TestTheme", service.getTheme());
        }
    }

    @Nested
    class DefaultImplementationTests {
        @Test
        void defaultGetWeaponsReturnsEmptyList() {
            FpsBaseService service = new FpsBaseService() {
                @Override
                public String getTheme() {
                    return "Empty";
                }
            };
            assertTrue(service.getWeapons().isEmpty());
        }

        @Test
        void defaultGetEnemiesReturnsEmptyList() {
            FpsBaseService service = new FpsBaseService() {
                @Override
                public String getTheme() {
                    return "Empty";
                }
            };
            assertTrue(service.getEnemies().isEmpty());
        }

        @Test
        void defaultGetMapsReturnsEmptyList() {
            FpsBaseService service = new FpsBaseService() {
                @Override
                public String getTheme() {
                    return "Empty";
                }
            };
            assertTrue(service.getMaps().isEmpty());
        }
    }

    @Nested
    class CustomContentTests {
        @Test
        void getWeaponsReturnsCustomWeapons() {
            FpsBaseService service = new TestFpsService();
            List<FpsWeapon> weapons = service.getWeapons();
            assertEquals(2, weapons.size());
            assertEquals("Test Pistol", weapons.get(0).getName());
            assertEquals("Test Rifle", weapons.get(1).getName());
        }

        @Test
        void getEnemiesReturnsCustomEnemies() {
            FpsBaseService service = new TestFpsService();
            List<FpsEnemy> enemies = service.getEnemies();
            assertEquals(2, enemies.size());
            assertEquals("Test Soldier", enemies.get(0).getName());
            assertEquals("Test Elite", enemies.get(1).getName());
        }

        @Test
        void getMapsReturnsCustomMaps() {
            FpsBaseService service = new TestFpsService();
            List<FpsMap> maps = service.getMaps();
            assertEquals(1, maps.size());
            assertEquals("Test Arena", maps.get(0).getName());
        }
    }

    @Nested
    class RegisterContentTests {
        @Test
        void registerContentAddsWeapons() {
            FpsBaseService service = new TestFpsService();
            FpsWeaponService weaponService = new FpsWeaponService();
            FpsMapService mapService = new FpsMapService();
            int before = weaponService.getAllWeapons().size();
            service.registerContent(weaponService, mapService);
            assertEquals(before + 2, weaponService.getAllWeapons().size());
        }

        @Test
        void registerContentAddsMaps() {
            FpsBaseService service = new TestFpsService();
            FpsWeaponService weaponService = new FpsWeaponService();
            FpsMapService mapService = new FpsMapService();
            int before = mapService.getAllMaps().size();
            service.registerContent(weaponService, mapService);
            assertEquals(before + 1, mapService.getAllMaps().size());
        }

        @Test
        void registerContentSetsThemeOnMaps() {
            FpsBaseService service = new TestFpsService();
            FpsWeaponService weaponService = new FpsWeaponService();
            FpsMapService mapService = new FpsMapService();
            service.registerContent(weaponService, mapService);
            List<FpsMap> themed = mapService.getMapsByTheme("TestTheme");
            assertEquals(1, themed.size());
        }

        @Test
        void registerContentDoesNotThrowWithEmptyContent() {
            FpsBaseService service = new FpsBaseService() {
                @Override
                public String getTheme() {
                    return "Empty";
                }
            };
            FpsWeaponService weaponService = new FpsWeaponService();
            FpsMapService mapService = new FpsMapService();
            int weaponBefore = weaponService.getAllWeapons().size();
            int mapBefore = mapService.getAllMaps().size();
            service.registerContent(weaponService, mapService);
            assertEquals(weaponBefore, weaponService.getAllWeapons().size());
            assertEquals(mapBefore, mapService.getAllMaps().size());
        }
    }

    @Nested
    class AbstractClassTests {
        @Test
        void fpsBaseServiceIsAbstract() {
            assertTrue(java.lang.reflect.Modifier.isAbstract(FpsBaseService.class.getModifiers()));
        }
    }
}
