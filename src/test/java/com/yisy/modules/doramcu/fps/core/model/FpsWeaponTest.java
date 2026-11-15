package com.yisy.modules.doramcu.fps.core.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;

import static org.junit.jupiter.api.Assertions.*;

class FpsWeaponTest {

    @Nested
    class ConstructorTests {
        @Test
        void defaultConstructorInitializesNulls() {
            FpsWeapon weapon = new FpsWeapon();
            assertNull(weapon.getId());
            assertNull(weapon.getName());
            assertNull(weapon.getType());
            assertEquals(0, weapon.getDamage());
            assertEquals(0.0, weapon.getFireRate());
        }

        @Test
        void fullConstructorGeneratesUuid() {
            FpsWeapon weapon = new FpsWeapon("Pistol", FpsWeapon.WeaponType.PISTOL, 25, 4.0, 1.5, 15);
            assertNotNull(weapon.getId());
            assertEquals("Pistol", weapon.getName());
            assertEquals(FpsWeapon.WeaponType.PISTOL, weapon.getType());
        }

        @Test
        void fullConstructorSetsDefaults() {
            FpsWeapon weapon = new FpsWeapon("Rifle", FpsWeapon.WeaponType.RIFLE, 20, 10.0, 2.0, 30);
            assertEquals(20, weapon.getDamage());
            assertEquals(10.0, weapon.getFireRate());
            assertEquals(2.0, weapon.getReloadTime());
            assertEquals(30, weapon.getMagSize());
            assertEquals(150, weapon.getReserveAmmo());
            assertTrue(weapon.isHitScan());
            assertEquals(100.0, weapon.getProjectileSpeed());
            assertEquals(2.0, weapon.getRecoilPitch());
            assertEquals(1.0, weapon.getRecoilYaw());
            assertEquals(100.0, weapon.getRange());
        }
    }

    @Nested
    class WeaponTypeTests {
        @Test
        void allWeaponTypesExist() {
            assertSame(FpsWeapon.WeaponType.PISTOL, FpsWeapon.WeaponType.valueOf("PISTOL"));
            assertSame(FpsWeapon.WeaponType.RIFLE, FpsWeapon.WeaponType.valueOf("RIFLE"));
            assertSame(FpsWeapon.WeaponType.SHOTGUN, FpsWeapon.WeaponType.valueOf("SHOTGUN"));
            assertSame(FpsWeapon.WeaponType.MACHINE_GUN, FpsWeapon.WeaponType.valueOf("MACHINE_GUN"));
            assertSame(FpsWeapon.WeaponType.SNIPER, FpsWeapon.WeaponType.valueOf("SNIPER"));
            assertSame(FpsWeapon.WeaponType.ROCKET_LAUNCHER, FpsWeapon.WeaponType.valueOf("ROCKET_LAUNCHER"));
            assertSame(FpsWeapon.WeaponType.GRENADE, FpsWeapon.WeaponType.valueOf("GRENADE"));
        }

        @Test
        void eachWeaponTypeCanBeCreated() {
            FpsWeapon pistol = new FpsWeapon("Pistol", FpsWeapon.WeaponType.PISTOL, 25, 4.0, 1.5, 15);
            FpsWeapon rifle = new FpsWeapon("Rifle", FpsWeapon.WeaponType.RIFLE, 20, 10.0, 2.0, 30);
            FpsWeapon shotgun = new FpsWeapon("Shotgun", FpsWeapon.WeaponType.SHOTGUN, 50, 1.5, 2.5, 8);
            FpsWeapon mg = new FpsWeapon("MG", FpsWeapon.WeaponType.MACHINE_GUN, 15, 15.0, 3.0, 100);
            FpsWeapon sniper = new FpsWeapon("Sniper", FpsWeapon.WeaponType.SNIPER, 80, 1.0, 3.0, 5);
            FpsWeapon rocket = new FpsWeapon("Rocket", FpsWeapon.WeaponType.ROCKET_LAUNCHER, 150, 0.5, 4.0, 4);
            FpsWeapon grenade = new FpsWeapon("Grenade", FpsWeapon.WeaponType.GRENADE, 100, 0.3, 1.0, 1);

            assertEquals(FpsWeapon.WeaponType.PISTOL, pistol.getType());
            assertEquals(FpsWeapon.WeaponType.RIFLE, rifle.getType());
            assertEquals(FpsWeapon.WeaponType.SHOTGUN, shotgun.getType());
            assertEquals(FpsWeapon.WeaponType.MACHINE_GUN, mg.getType());
            assertEquals(FpsWeapon.WeaponType.SNIPER, sniper.getType());
            assertEquals(FpsWeapon.WeaponType.ROCKET_LAUNCHER, rocket.getType());
            assertEquals(FpsWeapon.WeaponType.GRENADE, grenade.getType());
        }
    }

    @Nested
    class GetterSetterTests {
        @Test
        void idCanBeSet() {
            FpsWeapon weapon = new FpsWeapon();
            weapon.setId("custom-id");
            assertEquals("custom-id", weapon.getId());
        }

        @Test
        void nameCanBeSet() {
            FpsWeapon weapon = new FpsWeapon();
            weapon.setName("Plasma Rifle");
            assertEquals("Plasma Rifle", weapon.getName());
        }

        @Test
        void typeCanBeSet() {
            FpsWeapon weapon = new FpsWeapon();
            weapon.setType(FpsWeapon.WeaponType.SNIPER);
            assertEquals(FpsWeapon.WeaponType.SNIPER, weapon.getType());
        }

        @Test
        void damageCanBeSet() {
            FpsWeapon weapon = new FpsWeapon();
            weapon.setDamage(999);
            assertEquals(999, weapon.getDamage());
        }

        @Test
        void fireRateCanBeSet() {
            FpsWeapon weapon = new FpsWeapon();
            weapon.setFireRate(20.0);
            assertEquals(20.0, weapon.getFireRate());
        }

        @Test
        void reloadTimeCanBeSet() {
            FpsWeapon weapon = new FpsWeapon();
            weapon.setReloadTime(0.5);
            assertEquals(0.5, weapon.getReloadTime());
        }

        @Test
        void magSizeCanBeSet() {
            FpsWeapon weapon = new FpsWeapon();
            weapon.setMagSize(200);
            assertEquals(200, weapon.getMagSize());
        }

        @Test
        void reserveAmmoCanBeSet() {
            FpsWeapon weapon = new FpsWeapon();
            weapon.setReserveAmmo(500);
            assertEquals(500, weapon.getReserveAmmo());
        }

        @Test
        void hitScanCanBeToggled() {
            FpsWeapon weapon = new FpsWeapon();
            weapon.setHitScan(false);
            assertFalse(weapon.isHitScan());
        }

        @Test
        void projectileSpeedCanBeSet() {
            FpsWeapon weapon = new FpsWeapon();
            weapon.setProjectileSpeed(200.0);
            assertEquals(200.0, weapon.getProjectileSpeed());
        }

        @Test
        void recoilPitchCanBeSet() {
            FpsWeapon weapon = new FpsWeapon();
            weapon.setRecoilPitch(5.0);
            assertEquals(5.0, weapon.getRecoilPitch());
        }

        @Test
        void recoilYawCanBeSet() {
            FpsWeapon weapon = new FpsWeapon();
            weapon.setRecoilYaw(3.0);
            assertEquals(3.0, weapon.getRecoilYaw());
        }

        @Test
        void rangeCanBeSet() {
            FpsWeapon weapon = new FpsWeapon();
            weapon.setRange(500.0);
            assertEquals(500.0, weapon.getRange());
        }

        @Test
        void descriptionCanBeSet() {
            FpsWeapon weapon = new FpsWeapon();
            weapon.setDescription("A powerful weapon");
            assertEquals("A powerful weapon", weapon.getDescription());
        }
    }

    @Nested
    class ReserveAmmoCalculationTests {
        @Test
        void reserveAmmoIsFiveTimesMagSize() {
            FpsWeapon weapon = new FpsWeapon("Test", FpsWeapon.WeaponType.PISTOL, 10, 1.0, 1.0, 20);
            assertEquals(100, weapon.getReserveAmmo());
        }

        @Test
        void reserveAmmoCanBeOverridden() {
            FpsWeapon weapon = new FpsWeapon("Test", FpsWeapon.WeaponType.PISTOL, 10, 1.0, 1.0, 20);
            weapon.setReserveAmmo(50);
            assertEquals(50, weapon.getReserveAmmo());
        }
    }

    @Nested
    class UniqueIdTests {
        @Test
        void twoWeaponsHaveDifferentIds() {
            FpsWeapon w1 = new FpsWeapon("A", FpsWeapon.WeaponType.PISTOL, 10, 1.0, 1.0, 10);
            FpsWeapon w2 = new FpsWeapon("B", FpsWeapon.WeaponType.PISTOL, 10, 1.0, 1.0, 10);
            assertNotEquals(w1.getId(), w2.getId());
        }
    }
}
