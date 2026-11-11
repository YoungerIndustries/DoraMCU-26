package com.yisy.modules.doramcu.earth2026.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Earth2026Structure Model Tests")
class Earth2026StructureTest {

    @Nested
    @DisplayName("Constructors Tests")
    class ConstructorTests {

        @Test
        @DisplayName("default constructor should initialize defaults")
        void defaultConstructor() {
            Earth2026Structure structure = new Earth2026Structure();
            assertEquals(0, structure.getTechLevel());
            assertEquals(0, structure.getDefenseRating());
            assertEquals(0, structure.getMaxCapacity());
            assertEquals(0, structure.getCurrentCapacity());
            assertFalse(structure.isProvidesCover());
            assertEquals(0, structure.getCoverBonus());
            assertFalse(structure.isStrategic());
            assertTrue(structure.isActive());
        }

        @Test
        @DisplayName("parameterized constructor should set fields")
        void parameterizedConstructor() {
            Earth2026Structure structure = new Earth2026Structure("Bunker 7", 1L, "Bunker", 4);
            assertEquals("Bunker 7", structure.getName());
            assertEquals(1L, structure.getLocationId());
            assertEquals("Bunker", structure.getStructureType());
            assertEquals(4, structure.getTechLevel());
        }
    }

    @Nested
    @DisplayName("Getters and Setters Tests")
    class GettersSettersTests {

        @Test
        @DisplayName("should set and get all fields")
        void allFields() {
            Earth2026Structure structure = new Earth2026Structure();
            structure.setId(42L);
            structure.setName("Airfield");
            structure.setLocationId(2L);
            structure.setStructureType("Airfield");
            structure.setTechLevel(4);
            structure.setDefenseRating(75);
            structure.setMaxCapacity(100);
            structure.setCurrentCapacity(50);
            structure.setProvidesCover(true);
            structure.setCoverBonus(3);
            structure.setStrategic(true);
            structure.setDescription("Major airfield");
            structure.setActive(false);

            assertEquals(42L, structure.getId());
            assertEquals("Airfield", structure.getName());
            assertEquals(2L, structure.getLocationId());
            assertEquals("Airfield", structure.getStructureType());
            assertEquals(4, structure.getTechLevel());
            assertEquals(75, structure.getDefenseRating());
            assertEquals(100, structure.getMaxCapacity());
            assertEquals(50, structure.getCurrentCapacity());
            assertTrue(structure.isProvidesCover());
            assertEquals(3, structure.getCoverBonus());
            assertTrue(structure.isStrategic());
            assertEquals("Major airfield", structure.getDescription());
            assertFalse(structure.isActive());
        }
    }

    @Nested
    @DisplayName("getTechLevelEnum Tests")
    class GetTechLevelEnumTests {

        @Test
        @DisplayName("should return TL4 for tech level 4")
        void getTechLevelEnum_TL4() {
            Earth2026Structure structure = new Earth2026Structure();
            structure.setTechLevel(4);
            assertEquals(TechLevel.TL4, structure.getTechLevelEnum());
        }

        @Test
        @DisplayName("should return TL0 for tech level 0")
        void getTechLevelEnum_TL0() {
            Earth2026Structure structure = new Earth2026Structure();
            structure.setTechLevel(0);
            assertEquals(TechLevel.TL0, structure.getTechLevelEnum());
        }

        @Test
        @DisplayName("should throw for invalid tech level")
        void getTechLevelEnum_invalid() {
            Earth2026Structure structure = new Earth2026Structure();
            structure.setTechLevel(99);
            assertThrows(IllegalArgumentException.class, structure::getTechLevelEnum);
        }
    }

    @Nested
    @DisplayName("hasCapacity Tests")
    class HasCapacityTests {

        @Test
        @DisplayName("should return true when capacity available")
        void hasCapacity_available() {
            Earth2026Structure structure = new Earth2026Structure();
            structure.setMaxCapacity(100);
            structure.setCurrentCapacity(50);
            assertTrue(structure.hasCapacity(50));
            assertTrue(structure.hasCapacity(49));
        }

        @Test
        @DisplayName("should return false when capacity exceeded")
        void hasCapacity_exceeded() {
            Earth2026Structure structure = new Earth2026Structure();
            structure.setMaxCapacity(100);
            structure.setCurrentCapacity(50);
            assertFalse(structure.hasCapacity(51));
        }

        @Test
        @DisplayName("should return true for exact capacity")
        void hasCapacity_exact() {
            Earth2026Structure structure = new Earth2026Structure();
            structure.setMaxCapacity(100);
            structure.setCurrentCapacity(50);
            assertTrue(structure.hasCapacity(50));
        }

        @Test
        @DisplayName("should return true for zero units")
        void hasCapacity_zero() {
            Earth2026Structure structure = new Earth2026Structure();
            structure.setMaxCapacity(100);
            structure.setCurrentCapacity(100);
            assertTrue(structure.hasCapacity(0));
        }
    }

    @Nested
    @DisplayName("addUnits Tests")
    class AddUnitsTests {

        @Test
        @DisplayName("should add units within capacity")
        void addUnits_success() {
            Earth2026Structure structure = new Earth2026Structure();
            structure.setMaxCapacity(100);
            structure.setCurrentCapacity(50);
            assertTrue(structure.addUnits(25));
            assertEquals(75, structure.getCurrentCapacity());
        }

        @Test
        @DisplayName("should reject units exceeding capacity")
        void addUnits_rejected() {
            Earth2026Structure structure = new Earth2026Structure();
            structure.setMaxCapacity(100);
            structure.setCurrentCapacity(90);
            assertFalse(structure.addUnits(20));
            assertEquals(90, structure.getCurrentCapacity());
        }

        @Test
        @DisplayName("should add exact capacity")
        void addUnits_exact() {
            Earth2026Structure structure = new Earth2026Structure();
            structure.setMaxCapacity(100);
            structure.setCurrentCapacity(50);
            assertTrue(structure.addUnits(50));
            assertEquals(100, structure.getCurrentCapacity());
        }

        @Test
        @DisplayName("should add zero units")
        void addUnits_zero() {
            Earth2026Structure structure = new Earth2026Structure();
            structure.setMaxCapacity(100);
            structure.setCurrentCapacity(50);
            assertTrue(structure.addUnits(0));
            assertEquals(50, structure.getCurrentCapacity());
        }
    }

    @Nested
    @DisplayName("removeUnits Tests")
    class RemoveUnitsTests {

        @Test
        @DisplayName("should remove units within capacity")
        void removeUnits_success() {
            Earth2026Structure structure = new Earth2026Structure();
            structure.setCurrentCapacity(50);
            int removed = structure.removeUnits(25);
            assertEquals(25, removed);
            assertEquals(25, structure.getCurrentCapacity());
        }

        @Test
        @DisplayName("should remove all when requesting more than available")
        void removeUnits_all() {
            Earth2026Structure structure = new Earth2026Structure();
            structure.setCurrentCapacity(30);
            int removed = structure.removeUnits(50);
            assertEquals(30, removed);
            assertEquals(0, structure.getCurrentCapacity());
        }

        @Test
        @DisplayName("should return zero when empty")
        void removeUnits_empty() {
            Earth2026Structure structure = new Earth2026Structure();
            structure.setCurrentCapacity(0);
            int removed = structure.removeUnits(10);
            assertEquals(0, removed);
            assertEquals(0, structure.getCurrentCapacity());
        }

        @Test
        @DisplayName("should remove zero units")
        void removeUnits_zero() {
            Earth2026Structure structure = new Earth2026Structure();
            structure.setCurrentCapacity(50);
            int removed = structure.removeUnits(0);
            assertEquals(0, removed);
            assertEquals(50, structure.getCurrentCapacity());
        }
    }

    @Nested
    @DisplayName("toString Tests")
    class ToStringTests {

        @Test
        @DisplayName("should include id, name, type, TL, and defense")
        void toString_format() {
            Earth2026Structure structure = new Earth2026Structure();
            structure.setId(1L);
            structure.setName("Bunker 7");
            structure.setStructureType("Bunker");
            structure.setTechLevel(4);
            structure.setDefenseRating(80);

            String s = structure.toString();
            assertTrue(s.contains("id=1"));
            assertTrue(s.contains("Bunker 7"));
            assertTrue(s.contains("Bunker"));
            assertTrue(s.contains("TL=4"));
            assertTrue(s.contains("defense=80"));
        }
    }
}
