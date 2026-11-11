package com.yisy.modules.doramcu.babylon5.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("B5Firearm Tests")
class B5FirearmTest {

    @Nested
    @DisplayName("Enum values")
    class EnumValues {

        @Test
        @DisplayName("All 8 firearm enum values exist")
        void allValues() {
            B5Firearm[] values = B5Firearm.values();
            assertEquals(8, values.length);
        }

        @Test
        @DisplayName("Sidearms")
        void sidearms() {
            assertSame(B5Firearm.PISTOL, B5Firearm.valueOf("PISTOL"));
            assertSame(B5Firearm.LASER_PISTOL, B5Firearm.valueOf("LASER_PISTOL"));
            assertSame(B5Firearm.REVOLVER, B5Firearm.valueOf("REVOLVER"));
        }

        @Test
        @DisplayName("Rifles")
        void rifles() {
            assertSame(B5Firearm.RIFLE, B5Firearm.valueOf("RIFLE"));
            assertSame(B5Firearm.LASER_RIFLE, B5Firearm.valueOf("LASER_RIFLE"));
            assertSame(B5Firearm.ASSAULT_RIFLE, B5Firearm.valueOf("ASSAULT_RIFLE"));
        }

        @Test
        @DisplayName("Heavy weapons")
        void heavyWeapons() {
            assertSame(B5Firearm.HEAVY_WEAPON, B5Firearm.valueOf("HEAVY_WEAPON"));
            assertSame(B5Firearm.PLASMA_CANNON, B5Firearm.valueOf("PLASMA_CANNON"));
        }
    }

    @Nested
    @DisplayName("Firearm properties")
    class FirearmProperties {

        @Test
        @DisplayName("Each firearm has a name")
        void names() {
            for (B5Firearm firearm : B5Firearm.values()) {
                assertNotNull(firearm.getName());
                assertFalse(firearm.getName().isEmpty());
            }
        }

        @Test
        @DisplayName("Each firearm has a description")
        void descriptions() {
            for (B5Firearm firearm : B5Firearm.values()) {
                assertNotNull(firearm.getDescription());
                assertFalse(firearm.getDescription().isEmpty());
            }
        }

        @Test
        @DisplayName("Each firearm has positive attack value")
        void attackValues() {
            for (B5Firearm firearm : B5Firearm.values()) {
                assertTrue(firearm.getAttackValue() > 0);
            }
        }

        @Test
        @DisplayName("Each firearm has positive damage")
        void damage() {
            for (B5Firearm firearm : B5Firearm.values()) {
                assertTrue(firearm.getDamage() > 0);
            }
        }

        @Test
        @DisplayName("Each firearm has non-negative encumbrance")
        void encumbrance() {
            for (B5Firearm firearm : B5Firearm.values()) {
                assertTrue(firearm.getEncumbrance() >= 0);
            }
        }

        @Test
        @DisplayName("Specific firearm properties")
        void specificProperties() {
            assertEquals("Pistol", B5Firearm.PISTOL.getName());
            assertEquals(4, B5Firearm.PISTOL.getAttackValue());
            assertEquals(1, B5Firearm.PISTOL.getDamage());
            assertEquals(2, B5Firearm.PISTOL.getEncumbrance());
            assertEquals(B5Firearm.RangeClass.NEAR, B5Firearm.PISTOL.getRangeClass());

            assertEquals("Plasma Cannon", B5Firearm.PLASMA_CANNON.getName());
            assertEquals(9, B5Firearm.PLASMA_CANNON.getAttackValue());
            assertEquals(6, B5Firearm.PLASMA_CANNON.getDamage());
            assertEquals(10, B5Firearm.PLASMA_CANNON.getEncumbrance());
            assertEquals(B5Firearm.RangeClass.FAR, B5Firearm.PLASMA_CANNON.getRangeClass());
        }
    }

    @Nested
    @DisplayName("RangeClass enum")
    class RangeClassEnum {

        @Test
        @DisplayName("All 3 range class values exist")
        void allValues() {
            B5Firearm.RangeClass[] values = B5Firearm.RangeClass.values();
            assertEquals(3, values.length);
        }

        @Test
        @DisplayName("Range class values")
        void rangeClassValues() {
            assertSame(B5Firearm.RangeClass.NEAR, B5Firearm.RangeClass.valueOf("NEAR"));
            assertSame(B5Firearm.RangeClass.INTERMEDIATE, B5Firearm.RangeClass.valueOf("INTERMEDIATE"));
            assertSame(B5Firearm.RangeClass.FAR, B5Firearm.RangeClass.valueOf("FAR"));
        }

        @Test
        @DisplayName("Range class names")
        void names() {
            assertEquals("Near", B5Firearm.RangeClass.NEAR.getName());
            assertEquals("Intermediate", B5Firearm.RangeClass.INTERMEDIATE.getName());
            assertEquals("Far", B5Firearm.RangeClass.FAR.getName());
        }

        @Test
        @DisplayName("Range class modifiers")
        void modifiers() {
            assertEquals(0, B5Firearm.RangeClass.NEAR.getModifier());
            assertEquals(-1, B5Firearm.RangeClass.INTERMEDIATE.getModifier());
            assertEquals(-2, B5Firearm.RangeClass.FAR.getModifier());
        }

        @Test
        @DisplayName("Sidearms have NEAR range")
        void sidearmRange() {
            assertEquals(B5Firearm.RangeClass.NEAR, B5Firearm.PISTOL.getRangeClass());
            assertEquals(B5Firearm.RangeClass.NEAR, B5Firearm.LASER_PISTOL.getRangeClass());
            assertEquals(B5Firearm.RangeClass.NEAR, B5Firearm.REVOLVER.getRangeClass());
        }

        @Test
        @DisplayName("Rifles have INTERMEDIATE range")
        void rifleRange() {
            assertEquals(B5Firearm.RangeClass.INTERMEDIATE, B5Firearm.RIFLE.getRangeClass());
            assertEquals(B5Firearm.RangeClass.INTERMEDIATE, B5Firearm.LASER_RIFLE.getRangeClass());
            assertEquals(B5Firearm.RangeClass.INTERMEDIATE, B5Firearm.ASSAULT_RIFLE.getRangeClass());
        }

        @Test
        @DisplayName("Heavy weapons have FAR range")
        void heavyWeaponRange() {
            assertEquals(B5Firearm.RangeClass.FAR, B5Firearm.HEAVY_WEAPON.getRangeClass());
            assertEquals(B5Firearm.RangeClass.FAR, B5Firearm.PLASMA_CANNON.getRangeClass());
        }
    }
}
