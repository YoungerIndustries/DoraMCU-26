package com.yisy.modules.doramcu.thirdreich.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ThirdReichGroundUnit")
class ThirdReichGroundUnitTest {

    @Nested
    @DisplayName("Constructor")
    class Constructor {

        @Test
        @DisplayName("creates unit with default values from unit type")
        void createsWithDefaults() {
            ThirdReichGroundUnit unit = new ThirdReichGroundUnit("u-1", "Alpha Company", ThirdReichUnitType.INFANTRY);

            assertEquals("u-1", unit.getId());
            assertEquals("Alpha Company", unit.getName());
            assertEquals(ThirdReichUnitType.INFANTRY, unit.getUnitType());
            assertEquals(100, unit.getStrength());
            assertEquals(100, unit.getMaxStrength());
            assertEquals(100, unit.getMorale());
            assertEquals(1, unit.getAttackValue());
            assertEquals(1, unit.getDefenseValue());
            assertEquals(1, unit.getMobility());
            assertEquals(1, unit.getSupplyCost());
            assertEquals(0, unit.getTerrainBonus());
            assertNull(unit.getCommanderId());
            assertNull(unit.getSourceGame());
        }

        @Test
        @DisplayName("creates tank unit with correct base stats")
        void createsTankWithStats() {
            ThirdReichGroundUnit unit = new ThirdReichGroundUnit("u-2", "Panzer Division", ThirdReichUnitType.TANK);

            assertEquals(4, unit.getAttackValue());
            assertEquals(3, unit.getDefenseValue());
            assertEquals(3, unit.getMobility());
            assertEquals(3, unit.getSupplyCost());
        }

        @Test
        @DisplayName("throws on null ID")
        void throwsOnNullId() {
            assertThrows(IllegalArgumentException.class, () ->
                    new ThirdReichGroundUnit(null, "Test", ThirdReichUnitType.INFANTRY));
        }

        @Test
        @DisplayName("throws on blank ID")
        void throwsOnBlankId() {
            assertThrows(IllegalArgumentException.class, () ->
                    new ThirdReichGroundUnit("  ", "Test", ThirdReichUnitType.INFANTRY));
        }

        @Test
        @DisplayName("throws on null name")
        void throwsOnNullName() {
            assertThrows(IllegalArgumentException.class, () ->
                    new ThirdReichGroundUnit("u-1", null, ThirdReichUnitType.INFANTRY));
        }

        @Test
        @DisplayName("throws on null unit type")
        void throwsOnNullType() {
            assertThrows(IllegalArgumentException.class, () ->
                    new ThirdReichGroundUnit("u-1", "Test", null));
        }
    }

    @Nested
    @DisplayName("Strength")
    class Strength {

        @Test
        @DisplayName("clamps strength to 0 minimum")
        void clampsToZero() {
            ThirdReichGroundUnit unit = new ThirdReichGroundUnit("u-1", "Test", ThirdReichUnitType.INFANTRY);
            unit.setStrength(-50);
            assertEquals(0, unit.getStrength());
        }

        @Test
        @DisplayName("clamps strength to maxStrength")
        void clampsToMax() {
            ThirdReichGroundUnit unit = new ThirdReichGroundUnit("u-1", "Test", ThirdReichUnitType.INFANTRY);
            unit.setStrength(200);
            assertEquals(100, unit.getStrength());
        }

        @Test
        @DisplayName("reduces strength when maxStrength lowered")
        void reducesWhenMaxLowered() {
            ThirdReichGroundUnit unit = new ThirdReichGroundUnit("u-1", "Test", ThirdReichUnitType.INFANTRY);
            unit.setMaxStrength(50);
            assertEquals(50, unit.getStrength());
        }
    }

    @Nested
    @DisplayName("Morale")
    class Morale {

        @Test
        @DisplayName("clamps morale to 0-100 range")
        void clampsMorale() {
            ThirdReichGroundUnit unit = new ThirdReichGroundUnit("u-1", "Test", ThirdReichUnitType.INFANTRY);
            unit.setMorale(-10);
            assertEquals(0, unit.getMorale());
            unit.setMorale(150);
            assertEquals(100, unit.getMorale());
        }

        @Test
        @DisplayName("adjustMorale clamps to range")
        void adjustMoraleClamps() {
            ThirdReichGroundUnit unit = new ThirdReichGroundUnit("u-1", "Test", ThirdReichUnitType.INFANTRY);
            unit.adjustMorale(-150);
            assertEquals(0, unit.getMorale());
            unit.setMorale(50);
            unit.adjustMorale(100);
            assertEquals(100, unit.getMorale());
        }
    }

    @Nested
    @DisplayName("Effective Combat Values")
    class EffectiveValues {

        @Test
        @DisplayName("full strength and morale returns base attack")
        void fullStrengthAttack() {
            ThirdReichGroundUnit unit = new ThirdReichGroundUnit("u-1", "Test", ThirdReichUnitType.TANK);
            assertEquals(4, unit.getEffectiveAttack());
        }

        @Test
        @DisplayName("half strength reduces effective attack")
        void halfStrengthAttack() {
            ThirdReichGroundUnit unit = new ThirdReichGroundUnit("u-1", "Test", ThirdReichUnitType.TANK);
            unit.setStrength(50);
            assertEquals(2, unit.getEffectiveAttack());
        }

        @Test
        @DisplayName("low morale reduces effective attack")
        void lowMoraleAttack() {
            ThirdReichGroundUnit unit = new ThirdReichGroundUnit("u-1", "Test", ThirdReichUnitType.TANK);
            unit.setMorale(50);
            assertEquals(2, unit.getEffectiveAttack());
        }

        @Test
        @DisplayName("effective attack minimum is 1")
        void effectiveAttackMin() {
            ThirdReichGroundUnit unit = new ThirdReichGroundUnit("u-1", "Test", ThirdReichUnitType.INFANTRY);
            unit.setStrength(1);
            unit.setMorale(1);
            assertTrue(unit.getEffectiveAttack() >= 1);
        }

        @Test
        @DisplayName("terrain bonus adds to effective defense")
        void terrainBonusDefense() {
            ThirdReichGroundUnit unit = new ThirdReichGroundUnit("u-1", "Test", ThirdReichUnitType.INFANTRY);
            unit.setTerrainBonus(2);
            assertEquals(3, unit.getEffectiveDefense());
        }

        @Test
        @DisplayName("effective defense minimum is 1")
        void effectiveDefenseMin() {
            ThirdReichGroundUnit unit = new ThirdReichGroundUnit("u-1", "Test", ThirdReichUnitType.INFANTRY);
            unit.setStrength(1);
            unit.setMorale(1);
            assertTrue(unit.getEffectiveDefense() >= 1);
        }
    }

    @Nested
    @DisplayName("Damage")
    class Damage {

        @Test
        @DisplayName("takeDamage reduces strength")
        void reducesStrength() {
            ThirdReichGroundUnit unit = new ThirdReichGroundUnit("u-1", "Test", ThirdReichUnitType.INFANTRY);
            boolean destroyed = unit.takeDamage(30);
            assertFalse(destroyed);
            assertEquals(70, unit.getStrength());
        }

        @Test
        @DisplayName("takeDamage returns true when destroyed")
        void returnsTrueWhenDestroyed() {
            ThirdReichGroundUnit unit = new ThirdReichGroundUnit("u-1", "Test", ThirdReichUnitType.INFANTRY);
            boolean destroyed = unit.takeDamage(100);
            assertTrue(destroyed);
            assertEquals(0, unit.getStrength());
            assertEquals(0, unit.getMorale());
        }

        @Test
        @DisplayName("takeDamage sets morale to 0 on destruction")
        void setsMoraleZeroOnDestruction() {
            ThirdReichGroundUnit unit = new ThirdReichGroundUnit("u-1", "Test", ThirdReichUnitType.INFANTRY);
            unit.takeDamage(100);
            assertEquals(0, unit.getMorale());
        }

        @Test
        @DisplayName("takeDamage does not go below 0")
        void doesNotGoBelowZero() {
            ThirdReichGroundUnit unit = new ThirdReichGroundUnit("u-1", "Test", ThirdReichUnitType.INFANTRY);
            unit.takeDamage(200);
            assertEquals(0, unit.getStrength());
        }
    }

    @Nested
    @DisplayName("Combat Effectiveness")
    class CombatEffectiveness {

        @Test
        @DisplayName("unit is effective when strength and morale > 0")
        void effectiveWhenHealthy() {
            ThirdReichGroundUnit unit = new ThirdReichGroundUnit("u-1", "Test", ThirdReichUnitType.INFANTRY);
            assertTrue(unit.isCombatEffective());
        }

        @Test
        @DisplayName("unit is not effective when strength is 0")
        void notEffectiveWhenDead() {
            ThirdReichGroundUnit unit = new ThirdReichGroundUnit("u-1", "Test", ThirdReichUnitType.INFANTRY);
            unit.setStrength(0);
            assertFalse(unit.isCombatEffective());
        }

        @Test
        @DisplayName("unit is not effective when morale is 0")
        void notEffectiveWhenRouted() {
            ThirdReichGroundUnit unit = new ThirdReichGroundUnit("u-1", "Test", ThirdReichUnitType.INFANTRY);
            unit.setMorale(0);
            assertFalse(unit.isCombatEffective());
        }
    }

    @Nested
    @DisplayName("Setters")
    class Setters {

        @Test
        @DisplayName("setCommanderId and getSourceGame work")
        void settersWork() {
            ThirdReichGroundUnit unit = new ThirdReichGroundUnit("u-1", "Test", ThirdReichUnitType.INFANTRY);
            unit.setCommanderId("cmd-1");
            unit.setSourceGame("Traveller");
            assertEquals("cmd-1", unit.getCommanderId());
            assertEquals("Traveller", unit.getSourceGame());
        }
    }

    @Nested
    @DisplayName("ToString")
    class ToString {

        @Test
        @DisplayName("toString contains unit info")
        void containsInfo() {
            ThirdReichGroundUnit unit = new ThirdReichGroundUnit("u-1", "Alpha", ThirdReichUnitType.INFANTRY);
            String str = unit.toString();
            assertTrue(str.contains("u-1"));
            assertTrue(str.contains("Alpha"));
            assertTrue(str.contains("Infantry"));
        }
    }
}
