package com.yisy.modules.doramcu.warhammer40k.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Unit Model")
class UnitTest {

    private UnitStats createStats() {
        return new UnitStats(8, 7, 15, 3, 5, 4, 7, 7, 3, null, 6);
    }

    @Nested
    @DisplayName("Constructor Validation")
    class ConstructorValidation {

        @Test
        @DisplayName("creates unit with valid data")
        void createsWithValidData() {
            Unit unit = new Unit(
                    "space_marine",
                    "Space Marine",
                    "imperium",
                    "infantry",
                    "41st Millennium",
                    "Elite warrior",
                    createStats(),
                    List.of("Bolter", "Power Sword"),
                    List.of("Fear", "Fearless"),
                    List.of()
            );

            assertEquals("space_marine", unit.id());
            assertEquals("Space Marine", unit.name());
            assertEquals("imperium", unit.faction());
            assertEquals("infantry", unit.type());
        }

        @Test
        @DisplayName("throws when id is null")
        void throwsWhenIdNull() {
            assertThrows(IllegalArgumentException.class, () ->
                    new Unit(null, "Name", "faction", "type", "era", "desc",
                            createStats(), List.of(), List.of(), List.of())
            );
        }

        @Test
        @DisplayName("throws when id is blank")
        void throwsWhenIdBlank() {
            assertThrows(IllegalArgumentException.class, () ->
                    new Unit("  ", "Name", "faction", "type", "era", "desc",
                            createStats(), List.of(), List.of(), List.of())
            );
        }

        @Test
        @DisplayName("throws when name is null")
        void throwsWhenNameNull() {
            assertThrows(IllegalArgumentException.class, () ->
                    new Unit("id", null, "faction", "type", "era", "desc",
                            createStats(), List.of(), List.of(), List.of())
            );
        }

        @Test
        @DisplayName("throws when name is blank")
        void throwsWhenNameBlank() {
            assertThrows(IllegalArgumentException.class, () ->
                    new Unit("id", "  ", "faction", "type", "era", "desc",
                            createStats(), List.of(), List.of(), List.of())
            );
        }
    }

    @Nested
    @DisplayName("Type Checks")
    class TypeChecks {

        @Test
        @DisplayName("identifies infantry units")
        void identifiesInfantry() {
            Unit unit = new Unit("id", "Name", "faction", "infantry", "era", "desc",
                    createStats(), List.of(), List.of(), List.of());

            assertTrue(unit.isInfantry());
            assertFalse(unit.isVehicle());
        }

        @Test
        @DisplayName("identifies vehicle units")
        void identifiesVehicle() {
            Unit unit = new Unit("id", "Name", "faction", "vehicle", "era", "desc",
                    createStats(), List.of(), List.of(), List.of());

            assertTrue(unit.isVehicle());
            assertFalse(unit.isInfantry());
        }

        @Test
        @DisplayName("identifies non-combat units")
        void identifiesNonCombatUnit() {
            UnitStats zeroStats = new UnitStats(0, 0, 10, 1, null, null, null, null, null, null, null);
            Unit unit = new Unit("id", "Name", "faction", "infantry", "era", "desc",
                    zeroStats, List.of(), List.of(), List.of());

            assertFalse(unit.isCombatUnit());
        }

        @Test
        @DisplayName("identifies combat units")
        void identifiesCombatUnit() {
            Unit unit = new Unit("id", "Name", "faction", "infantry", "era", "desc",
                    createStats(), List.of(), List.of(), List.of());

            assertTrue(unit.isCombatUnit());
        }
    }

    @Nested
    @DisplayName("Stat Accessors")
    class StatAccessors {

        @Test
        @DisplayName("returns attack strength")
        void returnsAttackStrength() {
            Unit unit = new Unit("id", "Name", "faction", "infantry", "era", "desc",
                    createStats(), List.of(), List.of(), List.of());

            assertEquals(8, unit.attackStrength());
        }

        @Test
        @DisplayName("returns defense strength")
        void returnsDefenseStrength() {
            Unit unit = new Unit("id", "Name", "faction", "infantry", "era", "desc",
                    createStats(), List.of(), List.of(), List.of());

            assertEquals(7, unit.defenseStrength());
        }

        @Test
        @DisplayName("returns total HP as hull + wounds")
        void returnsTotalHp() {
            Unit unit = new Unit("id", "Name", "faction", "infantry", "era", "desc",
                    createStats(), List.of(), List.of(), List.of());

            assertEquals(18, unit.totalHp()); // 15 hull + 3 wounds
        }

        @Test
        @DisplayName("returns weapon skill")
        void returnsWeaponSkill() {
            Unit unit = new Unit("id", "Name", "faction", "infantry", "era", "desc",
                    createStats(), List.of(), List.of(), List.of());

            assertEquals(5, unit.weaponSkill());
        }

        @Test
        @DisplayName("returns ballistic skill")
        void returnsBallisticSkill() {
            Unit unit = new Unit("id", "Name", "faction", "infantry", "era", "desc",
                    createStats(), List.of(), List.of(), List.of());

            assertEquals(4, unit.ballisticSkill());
        }

        @Test
        @DisplayName("returns strength")
        void returnsStrength() {
            Unit unit = new Unit("id", "Name", "faction", "infantry", "era", "desc",
                    createStats(), List.of(), List.of(), List.of());

            assertEquals(7, unit.strength());
        }

        @Test
        @DisplayName("returns toughness")
        void returnsToughness() {
            Unit unit = new Unit("id", "Name", "faction", "infantry", "era", "desc",
                    createStats(), List.of(), List.of(), List.of());

            assertEquals(7, unit.toughness());
        }

        @Test
        @DisplayName("returns armor save")
        void returnsArmorSave() {
            Unit unit = new Unit("id", "Name", "faction", "infantry", "era", "desc",
                    createStats(), List.of(), List.of(), List.of());

            assertEquals(3, unit.armorSave());
        }

        @Test
        @DisplayName("returns zero for null stats")
        void returnsZeroForNullStats() {
            Unit unit = new Unit("id", "Name", "faction", "infantry", "era", "desc",
                    null, List.of(), List.of(), List.of());

            assertEquals(0, unit.attackStrength());
            assertEquals(0, unit.defenseStrength());
            assertEquals(0, unit.totalHp());
            assertEquals(0, unit.weaponSkill());
            assertEquals(0, unit.ballisticSkill());
            assertEquals(0, unit.strength());
            assertEquals(0, unit.toughness());
            assertEquals(0, unit.armorSave());
        }

        @Test
        @DisplayName("returns zero for null optional stats")
        void returnsZeroForNullOptionalStats() {
            UnitStats stats = new UnitStats(5, 3, 10, 2, null, null, null, null, null, null, null);
            Unit unit = new Unit("id", "Name", "faction", "infantry", "era", "desc",
                    stats, List.of(), List.of(), List.of());

            assertEquals(0, unit.weaponSkill());
            assertEquals(0, unit.ballisticSkill());
            assertEquals(0, unit.strength());
            assertEquals(0, unit.toughness());
            assertEquals(0, unit.armorSave());
        }
    }
}
