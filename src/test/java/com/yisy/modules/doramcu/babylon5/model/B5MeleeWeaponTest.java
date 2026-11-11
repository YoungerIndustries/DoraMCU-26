package com.yisy.modules.doramcu.babylon5.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("B5MeleeWeapon Tests")
class B5MeleeWeaponTest {

    @Nested
    @DisplayName("Enum values")
    class EnumValues {

        @Test
        @DisplayName("All 10 melee weapon enum values exist")
        void allValues() {
            B5MeleeWeapon[] values = B5MeleeWeapon.values();
            assertEquals(10, values.length);
        }

        @Test
        @DisplayName("Unarmed weapons")
        void unarmed() {
            assertSame(B5MeleeWeapon.FISTS, B5MeleeWeapon.valueOf("FISTS"));
        }

        @Test
        @DisplayName("Blade weapons")
        void blades() {
            assertSame(B5MeleeWeapon.KNIFE, B5MeleeWeapon.valueOf("KNIFE"));
            assertSame(B5MeleeWeapon.COMBAT_KNIFE, B5MeleeWeapon.valueOf("COMBAT_KNIFE"));
            assertSame(B5MeleeWeapon.SWORD, B5MeleeWeapon.valueOf("SWORD"));
            assertSame(B5MeleeWeapon.MINBARI_SWORD, B5MeleeWeapon.valueOf("MINBARI_SWORD"));
            assertSame(B5MeleeWeapon.NARN_CLAW, B5MeleeWeapon.valueOf("NARN_CLAW"));
        }

        @Test
        @DisplayName("Blunt weapons")
        void blunt() {
            assertSame(B5MeleeWeapon.BATON, B5MeleeWeapon.valueOf("BATON"));
            assertSame(B5MeleeWeapon.STAFF, B5MeleeWeapon.valueOf("STAFF"));
        }

        @Test
        @DisplayName("Energy weapons")
        void energy() {
            assertSame(B5MeleeWeapon.ENERGY_WHIP, B5MeleeWeapon.valueOf("ENERGY_WHIP"));
            assertSame(B5MeleeWeapon.PLASMA_EDGE, B5MeleeWeapon.valueOf("PLASMA_EDGE"));
        }
    }

    @Nested
    @DisplayName("Weapon properties")
    class WeaponProperties {

        @Test
        @DisplayName("Each weapon has a name")
        void names() {
            for (B5MeleeWeapon weapon : B5MeleeWeapon.values()) {
                assertNotNull(weapon.getName());
                assertFalse(weapon.getName().isEmpty());
            }
        }

        @Test
        @DisplayName("Each weapon has a description")
        void descriptions() {
            for (B5MeleeWeapon weapon : B5MeleeWeapon.values()) {
                assertNotNull(weapon.getDescription());
                assertFalse(weapon.getDescription().isEmpty());
            }
        }

        @Test
        @DisplayName("Each weapon has non-negative parry")
        void parry() {
            for (B5MeleeWeapon weapon : B5MeleeWeapon.values()) {
                assertTrue(weapon.getParry() >= 0);
            }
        }

        @Test
        @DisplayName("Each weapon has positive damage")
        void damage() {
            for (B5MeleeWeapon weapon : B5MeleeWeapon.values()) {
                assertTrue(weapon.getDamage() > 0);
            }
        }

        @Test
        @DisplayName("Each weapon has non-negative encumbrance")
        void encumbrance() {
            for (B5MeleeWeapon weapon : B5MeleeWeapon.values()) {
                assertTrue(weapon.getEncumbrance() >= 0);
            }
        }

        @Test
        @DisplayName("Specific weapon properties")
        void specificProperties() {
            assertEquals("Fists", B5MeleeWeapon.FISTS.getName());
            assertEquals(0, B5MeleeWeapon.FISTS.getParry());
            assertEquals(1, B5MeleeWeapon.FISTS.getDamage());
            assertEquals(0, B5MeleeWeapon.FISTS.getEncumbrance());

            assertEquals("Plasma Edge", B5MeleeWeapon.PLASMA_EDGE.getName());
            assertEquals(4, B5MeleeWeapon.PLASMA_EDGE.getParry());
            assertEquals(6, B5MeleeWeapon.PLASMA_EDGE.getDamage());
            assertEquals(2, B5MeleeWeapon.PLASMA_EDGE.getEncumbrance());
        }

        @Test
        @DisplayName("Minbari sword has highest parry among blades")
        void minbariSwordParry() {
            assertTrue(B5MeleeWeapon.MINBARI_SWORD.getParry() >= B5MeleeWeapon.SWORD.getParry());
            assertTrue(B5MeleeWeapon.MINBARI_SWORD.getParry() >= B5MeleeWeapon.KNIFE.getParry());
        }
    }
}
