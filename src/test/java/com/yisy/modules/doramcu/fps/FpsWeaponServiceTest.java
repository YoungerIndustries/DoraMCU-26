package com.yisy.modules.doramcu.fps;

import com.yisy.modules.doramcu.fps.core.model.FpsWeapon;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;

import static org.junit.jupiter.api.Assertions.*;

class FpsWeaponServiceTest {

    @Nested
    class DefaultWeaponsTests {
        @Test
        void initializesWithDefaultWeapons() {
            FpsWeaponService service = new FpsWeaponService();
            assertEquals(6, service.getAllWeapons().size());
        }

        @Test
        void defaultWeaponsIncludePistol() {
            FpsWeaponService service = new FpsWeaponService();
            FpsWeapon pistol = service.getAllWeapons().stream()
                    .filter(w -> "Pistol".equals(w.getName()))
                    .findFirst().orElse(null);
            assertNotNull(pistol);
            assertEquals(FpsWeapon.WeaponType.PISTOL, pistol.getType());
        }

        @Test
        void defaultWeaponsIncludeAssaultRifle() {
            FpsWeaponService service = new FpsWeaponService();
            FpsWeapon rifle = service.getAllWeapons().stream()
                    .filter(w -> "Assault Rifle".equals(w.getName()))
                    .findFirst().orElse(null);
            assertNotNull(rifle);
            assertEquals(FpsWeapon.WeaponType.RIFLE, rifle.getType());
        }

        @Test
        void defaultWeaponsIncludeShotgun() {
            FpsWeaponService service = new FpsWeaponService();
            FpsWeapon shotgun = service.getAllWeapons().stream()
                    .filter(w -> "Shotgun".equals(w.getName()))
                    .findFirst().orElse(null);
            assertNotNull(shotgun);
            assertEquals(FpsWeapon.WeaponType.SHOTGUN, shotgun.getType());
        }

        @Test
        void defaultWeaponsIncludeMachineGun() {
            FpsWeaponService service = new FpsWeaponService();
            FpsWeapon mg = service.getAllWeapons().stream()
                    .filter(w -> "Machine Gun".equals(w.getName()))
                    .findFirst().orElse(null);
            assertNotNull(mg);
            assertEquals(FpsWeapon.WeaponType.MACHINE_GUN, mg.getType());
        }

        @Test
        void defaultWeaponsIncludeSniper() {
            FpsWeaponService service = new FpsWeaponService();
            FpsWeapon sniper = service.getAllWeapons().stream()
                    .filter(w -> "Sniper Rifle".equals(w.getName()))
                    .findFirst().orElse(null);
            assertNotNull(sniper);
            assertEquals(FpsWeapon.WeaponType.SNIPER, sniper.getType());
        }

        @Test
        void defaultWeaponsIncludeRocketLauncher() {
            FpsWeaponService service = new FpsWeaponService();
            FpsWeapon rocket = service.getAllWeapons().stream()
                    .filter(w -> "Rocket Launcher".equals(w.getName()))
                    .findFirst().orElse(null);
            assertNotNull(rocket);
            assertEquals(FpsWeapon.WeaponType.ROCKET_LAUNCHER, rocket.getType());
        }
    }

    @Nested
    class AddWeaponTests {
        @Test
        void addWeaponIncreasesCount() {
            FpsWeaponService service = new FpsWeaponService();
            int before = service.getAllWeapons().size();
            FpsWeapon weapon = new FpsWeapon("Custom", FpsWeapon.WeaponType.GRENADE, 100, 0.3, 1.0, 1);
            service.addWeapon(weapon);
            assertEquals(before + 1, service.getAllWeapons().size());
        }

        @Test
        void addWeaponCanBeRetrieved() {
            FpsWeaponService service = new FpsWeaponService();
            FpsWeapon weapon = new FpsWeapon("Custom", FpsWeapon.WeaponType.GRENADE, 100, 0.3, 1.0, 1);
            service.addWeapon(weapon);
            assertTrue(service.getWeapon(weapon.getId()).isPresent());
        }
    }

    @Nested
    class GetWeaponTests {
        @Test
        void getWeaponReturnsExistingWeapon() {
            FpsWeaponService service = new FpsWeaponService();
            FpsWeapon pistol = service.getAllWeapons().get(0);
            assertTrue(service.getWeapon(pistol.getId()).isPresent());
            assertEquals(pistol, service.getWeapon(pistol.getId()).get());
        }

        @Test
        void getWeaponReturnsEmptyForUnknownId() {
            FpsWeaponService service = new FpsWeaponService();
            assertFalse(service.getWeapon("unknown").isPresent());
        }
    }

    @Nested
    class RemoveWeaponTests {
        @Test
        void removeWeaponDecreasesCount() {
            FpsWeaponService service = new FpsWeaponService();
            FpsWeapon weapon = new FpsWeapon("Custom", FpsWeapon.WeaponType.GRENADE, 100, 0.3, 1.0, 1);
            service.addWeapon(weapon);
            int before = service.getAllWeapons().size();
            assertTrue(service.removeWeapon(weapon.getId()));
            assertEquals(before - 1, service.getAllWeapons().size());
        }

        @Test
        void removeNonExistentWeaponReturnsFalse() {
            FpsWeaponService service = new FpsWeaponService();
            assertFalse(service.removeWeapon("unknown"));
        }

        @Test
        void removeWeaponThenGetReturnsEmpty() {
            FpsWeaponService service = new FpsWeaponService();
            FpsWeapon weapon = new FpsWeapon("Custom", FpsWeapon.WeaponType.GRENADE, 100, 0.3, 1.0, 1);
            service.addWeapon(weapon);
            service.removeWeapon(weapon.getId());
            assertFalse(service.getWeapon(weapon.getId()).isPresent());
        }
    }

    @Nested
    class TypeFilteringTests {
        @Test
        void getWeaponsByTypeReturnsPistols() {
            FpsWeaponService service = new FpsWeaponService();
            assertEquals(1, service.getWeaponsByType(FpsWeapon.WeaponType.PISTOL).size());
        }

        @Test
        void getWeaponsByTypeReturnsRifles() {
            FpsWeaponService service = new FpsWeaponService();
            assertEquals(1, service.getWeaponsByType(FpsWeapon.WeaponType.RIFLE).size());
        }

        @Test
        void getWeaponsByTypeReturnsEmptyForNoMatch() {
            FpsWeaponService service = new FpsWeaponService();
            assertTrue(service.getWeaponsByType(FpsWeapon.WeaponType.GRENADE).isEmpty());
        }

        @Test
        void getWeaponsByTypeReturnsMultipleAfterAdd() {
            FpsWeaponService service = new FpsWeaponService();
            service.addWeapon(new FpsWeapon("Grenade 1", FpsWeapon.WeaponType.GRENADE, 100, 0.3, 1.0, 1));
            service.addWeapon(new FpsWeapon("Grenade 2", FpsWeapon.WeaponType.GRENADE, 120, 0.2, 1.5, 1));
            assertEquals(2, service.getWeaponsByType(FpsWeapon.WeaponType.GRENADE).size());
        }
    }

    @Nested
    class GetAllWeaponsTests {
        @Test
        void getAllWeaponsReturnsNewList() {
            FpsWeaponService service = new FpsWeaponService();
            java.util.List<FpsWeapon> list1 = service.getAllWeapons();
            java.util.List<FpsWeapon> list2 = service.getAllWeapons();
            assertNotSame(list1, list2);
        }
    }

    @Nested
    class WeaponDamageTests {
        @Test
        void rocketLauncherHasHighestDamage() {
            FpsWeaponService service = new FpsWeaponService();
            FpsWeapon rocket = service.getAllWeapons().stream()
                    .filter(w -> "Rocket Launcher".equals(w.getName()))
                    .findFirst().orElse(null);
            assertNotNull(rocket);
            assertEquals(150, rocket.getDamage());
        }

        @Test
        void machineGunHasLowestDamage() {
            FpsWeaponService service = new FpsWeaponService();
            FpsWeapon mg = service.getAllWeapons().stream()
                    .filter(w -> "Machine Gun".equals(w.getName()))
                    .findFirst().orElse(null);
            assertNotNull(mg);
            assertEquals(15, mg.getDamage());
        }
    }
}
