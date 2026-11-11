package com.yisy.modules.doramcu.star_trek_4x.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("StarTrekGroundUnit Tests")
class StarTrekGroundUnitTest {

    @Nested
    @DisplayName("Constructor")
    class Constructor {

        @Test
        @DisplayName("Default constructor initializes defaults")
        void defaults() {
            StarTrekGroundUnit unit = new StarTrekGroundUnit();
            assertNull(unit.getId());
            assertNull(unit.getName());
            assertNull(unit.getUnitType());
            assertEquals(100, unit.getStrength());
            assertEquals(100, unit.getMaxStrength());
            assertEquals(80, unit.getMorale());
            assertEquals(3, unit.getAttackValue());
            assertEquals(3, unit.getDefenseValue());
            assertNull(unit.getCharacterId());
            assertNull(unit.getOwnerFactionId());
            assertNull(unit.getWeaponLoadout());
            assertFalse(unit.isInCombat());
            assertNull(unit.getGameSession());
        }
    }

    @Nested
    @DisplayName("UnitType enum")
    class UnitTypeEnum {

        @Test
        @DisplayName("All unit type values")
        void allUnitTypes() {
            assertSame(StarTrekGroundUnit.UnitType.SECURITY, StarTrekGroundUnit.UnitType.valueOf("SECURITY"));
            assertSame(StarTrekGroundUnit.UnitType.MARINE, StarTrekGroundUnit.UnitType.valueOf("MARINE"));
            assertSame(StarTrekGroundUnit.UnitType.SCIENCE, StarTrekGroundUnit.UnitType.valueOf("SCIENCE"));
            assertSame(StarTrekGroundUnit.UnitType.ENGINEERING, StarTrekGroundUnit.UnitType.valueOf("ENGINEERING"));
            assertSame(StarTrekGroundUnit.UnitType.MEDICAL, StarTrekGroundUnit.UnitType.valueOf("MEDICAL"));
            assertSame(StarTrekGroundUnit.UnitType.DIPLOMAT, StarTrekGroundUnit.UnitType.valueOf("DIPLOMAT"));
        }

        @Test
        @DisplayName("Set unit type")
        void setUnitType() {
            StarTrekGroundUnit unit = new StarTrekGroundUnit();
            unit.setUnitType(StarTrekGroundUnit.UnitType.MARINE);
            assertEquals(StarTrekGroundUnit.UnitType.MARINE, unit.getUnitType());
        }
    }

    @Nested
    @DisplayName("Morale clamping")
    class MoraleClamping {

        @Test
        @DisplayName("Morale clamps at 0")
        void clampMin() {
            StarTrekGroundUnit unit = new StarTrekGroundUnit();
            unit.setMorale(-10);
            assertEquals(0, unit.getMorale());
        }

        @Test
        @DisplayName("Morale clamps at 100")
        void clampMax() {
            StarTrekGroundUnit unit = new StarTrekGroundUnit();
            unit.setMorale(150);
            assertEquals(100, unit.getMorale());
        }

        @Test
        @DisplayName("Morale within range is unchanged")
        void withinRange() {
            StarTrekGroundUnit unit = new StarTrekGroundUnit();
            unit.setMorale(50);
            assertEquals(50, unit.getMorale());
        }
    }

    @Nested
    @DisplayName("Damage")
    class Damage {

        @Test
        @DisplayName("Take damage reduces strength")
        void takeDamage() {
            StarTrekGroundUnit unit = new StarTrekGroundUnit();
            unit.setStrength(100);
            unit.takeDamage(30);
            assertEquals(70, unit.getStrength());
        }

        @Test
        @DisplayName("Take damage doesn't go below 0")
        void damageClamp() {
            StarTrekGroundUnit unit = new StarTrekGroundUnit();
            unit.setStrength(50);
            unit.takeDamage(100);
            assertEquals(0, unit.getStrength());
        }

        @Test
        @DisplayName("Multiple damage applications")
        void multipleDamage() {
            StarTrekGroundUnit unit = new StarTrekGroundUnit();
            unit.setStrength(100);
            unit.takeDamage(20);
            unit.takeDamage(30);
            unit.takeDamage(25);
            assertEquals(25, unit.getStrength());
        }
    }

    @Nested
    @DisplayName("Destroyed check")
    class DestroyedCheck {

        @Test
        @DisplayName("Unit not destroyed at full strength")
        void notDestroyed() {
            StarTrekGroundUnit unit = new StarTrekGroundUnit();
            assertFalse(unit.isDestroyed());
        }

        @Test
        @DisplayName("Unit destroyed at 0 strength")
        void destroyedAtZero() {
            StarTrekGroundUnit unit = new StarTrekGroundUnit();
            unit.setStrength(0);
            assertTrue(unit.isDestroyed());
        }

        @Test
        @DisplayName("Unit destroyed at negative strength")
        void destroyedAtNegative() {
            StarTrekGroundUnit unit = new StarTrekGroundUnit();
            unit.setStrength(-10);
            assertTrue(unit.isDestroyed());
        }

        @Test
        @DisplayName("Unit destroyed after taking full damage")
        void destroyedAfterDamage() {
            StarTrekGroundUnit unit = new StarTrekGroundUnit();
            unit.setStrength(50);
            unit.takeDamage(50);
            assertTrue(unit.isDestroyed());
        }
    }

    @Nested
    @DisplayName("Combat status")
    class CombatStatus {

        @Test
        @DisplayName("Default not in combat")
        void defaultNotInCombat() {
            StarTrekGroundUnit unit = new StarTrekGroundUnit();
            assertFalse(unit.isInCombat());
        }

        @Test
        @DisplayName("Set in combat")
        void setInCombat() {
            StarTrekGroundUnit unit = new StarTrekGroundUnit();
            unit.setInCombat(true);
            assertTrue(unit.isInCombat());
        }
    }

    @Nested
    @DisplayName("Unit stats")
    class UnitStats {

        @Test
        @DisplayName("Set attack and defense values")
        void setCombatValues() {
            StarTrekGroundUnit unit = new StarTrekGroundUnit();
            unit.setAttackValue(5);
            unit.setDefenseValue(4);
            assertEquals(5, unit.getAttackValue());
            assertEquals(4, unit.getDefenseValue());
        }

        @Test
        @DisplayName("Set max strength")
        void setMaxStrength() {
            StarTrekGroundUnit unit = new StarTrekGroundUnit();
            unit.setMaxStrength(200);
            assertEquals(200, unit.getMaxStrength());
        }

        @Test
        @DisplayName("Set weapon loadout")
        void setWeaponLoadout() {
            StarTrekGroundUnit unit = new StarTrekGroundUnit();
            unit.setWeaponLoadout("Phaser Rifle, Photon Grenades");
            assertEquals("Phaser Rifle, Photon Grenades", unit.getWeaponLoadout());
        }
    }
}
