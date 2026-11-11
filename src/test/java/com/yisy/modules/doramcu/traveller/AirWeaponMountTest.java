package com.yisy.modules.doramcu.traveller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("AirWeaponMount")
class AirWeaponMountTest {

    @Nested
    @DisplayName("Constructor")
    class Constructor {

        @Test
        @DisplayName("creates laser weapon with infinite ammo")
        void createsLaser() {
            AirWeaponMount weapon = new AirWeaponMount("w1", AirWeaponMount.MountType.LASER, "Laser", 5, 10);

            assertEquals("w1", weapon.getId());
            assertEquals(AirWeaponMount.MountType.LASER, weapon.getMountType());
            assertEquals("Laser", weapon.getWeaponName());
            assertEquals(5, weapon.getDamageCode());
            assertEquals(10, weapon.getRange());
            assertEquals(-1, weapon.getAmmoCount()); // infinite for laser
            assertEquals(90, weapon.getArcOfFire());
        }

        @Test
        @DisplayName("creates gun weapon with finite ammo")
        void createsGun() {
            AirWeaponMount weapon = new AirWeaponMount("w1", AirWeaponMount.MountType.GUN, "Cannon", 4, 5);

            assertEquals(AirWeaponMount.MountType.GUN, weapon.getMountType());
            assertEquals(10, weapon.getAmmoCount());
        }

        @Test
        @DisplayName("creates missile weapon with finite ammo")
        void createsMissile() {
            AirWeaponMount weapon = new AirWeaponMount("w1", AirWeaponMount.MountType.MISSILE, "Missile", 6, 15);

            assertEquals(AirWeaponMount.MountType.MISSILE, weapon.getMountType());
            assertEquals(10, weapon.getAmmoCount());
        }

        @Test
        @DisplayName("default constructor creates empty weapon")
        void defaultConstructor() {
            AirWeaponMount weapon = new AirWeaponMount();

            assertNull(weapon.getId());
            assertNull(weapon.getMountType());
            assertEquals(0, weapon.getDamageCode());
        }
    }

    @Nested
    @DisplayName("Setters")
    class Setters {

        @Test
        @DisplayName("setters update values")
        void settersWork() {
            AirWeaponMount weapon = new AirWeaponMount();
            weapon.setId("w2");
            weapon.setMountType(AirWeaponMount.MountType.LASER);
            weapon.setWeaponName("Plasma");
            weapon.setDamageCode(8);
            weapon.setRange(20);
            weapon.setAmmoCount(-1);
            weapon.setArcOfFire(180);

            assertEquals("w2", weapon.getId());
            assertEquals(AirWeaponMount.MountType.LASER, weapon.getMountType());
            assertEquals("Plasma", weapon.getWeaponName());
            assertEquals(8, weapon.getDamageCode());
            assertEquals(20, weapon.getRange());
            assertEquals(-1, weapon.getAmmoCount());
            assertEquals(180, weapon.getArcOfFire());
        }
    }

    @Nested
    @DisplayName("MountType enum")
    class MountType {

        @Test
        @DisplayName("has three mount types")
        void hasThreeTypes() {
            assertEquals(3, AirWeaponMount.MountType.values().length);
        }
    }
}
