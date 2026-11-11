package com.yisy.modules.doramcu.earth2026.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("TimePeriodTemplate Enum Tests")
class TimePeriodTemplateTest {

    @Nested
    @DisplayName("Enum values Tests")
    class EnumValuesTests {

        @Test
        @DisplayName("should have 12 time period templates")
        void values_shouldHave12Templates() {
            assertEquals(12, TimePeriodTemplate.values().length);
        }

        @Test
        @DisplayName("ANCIENT should map to TL1")
        void ANCIENT_shouldMapToTL1() {
            assertEquals("Ancient", TimePeriodTemplate.ANCIENT.getName());
            assertEquals(TechLevel.TL1, TimePeriodTemplate.ANCIENT.getBaseTechLevel());
            assertNotNull(TimePeriodTemplate.ANCIENT.getDescription());
            assertNotNull(TimePeriodTemplate.ANCIENT.getDateRange());
        }

        @Test
        @DisplayName("WWII should map to TL4")
        void WWII_shouldMapToTL4() {
            assertEquals("World War II", TimePeriodTemplate.WWII.getName());
            assertEquals(TechLevel.TL4, TimePeriodTemplate.WWII.getBaseTechLevel());
            assertTrue(TimePeriodTemplate.WWII.getDateRange().contains("1939"));
            assertTrue(TimePeriodTemplate.WWII.getDateRange().contains("1945"));
        }

        @Test
        @DisplayName("MODERN should map to TL5")
        void MODERN_shouldMapToTL5() {
            assertEquals("Modern", TimePeriodTemplate.MODERN.getName());
            assertEquals(TechLevel.TL5, TimePeriodTemplate.MODERN.getBaseTechLevel());
        }

        @Test
        @DisplayName("AVERAGE_STELLAR should map to TL13")
        void AVERAGE_STELLAR_shouldMapToTL13() {
            assertEquals("Average Stellar", TimePeriodTemplate.AVERAGE_STELLAR.getName());
            assertEquals(TechLevel.TL13, TimePeriodTemplate.AVERAGE_STELLAR.getBaseTechLevel());
        }
    }

    @Nested
    @DisplayName("fromName Tests")
    class FromNameTests {

        @Test
        @DisplayName("should find by exact name")
        void fromName_exact() {
            assertEquals(TimePeriodTemplate.WWII, TimePeriodTemplate.fromName("World War II"));
        }

        @Test
        @DisplayName("should find by case-insensitive name")
        void fromName_caseInsensitive() {
            assertEquals(TimePeriodTemplate.WWII, TimePeriodTemplate.fromName("world war ii"));
            assertEquals(TimePeriodTemplate.WWII, TimePeriodTemplate.fromName("WORLD WAR II"));
            assertEquals(TimePeriodTemplate.MODERN, TimePeriodTemplate.fromName("modern"));
        }

        @Test
        @DisplayName("should throw for unknown name")
        void fromName_unknown() {
            assertThrows(IllegalArgumentException.class, () -> TimePeriodTemplate.fromName("Unknown Era"));
        }

        @Test
        @DisplayName("should throw for empty name")
        void fromName_empty() {
            assertThrows(IllegalArgumentException.class, () -> TimePeriodTemplate.fromName(""));
        }

        @Test
        @DisplayName("should find all templates by name")
        void fromName_allTemplates() {
            for (TimePeriodTemplate tpl : TimePeriodTemplate.values()) {
                assertEquals(tpl, TimePeriodTemplate.fromName(tpl.getName()));
            }
        }
    }

    @Nested
    @DisplayName("fromTechLevel Tests")
    class FromTechLevelTests {

        @Test
        @DisplayName("should find by TL1")
        void fromTechLevel_TL1() {
            assertEquals(TimePeriodTemplate.ANCIENT, TimePeriodTemplate.fromTechLevel(TechLevel.TL1));
        }

        @Test
        @DisplayName("should find by TL4")
        void fromTechLevel_TL4() {
            // Both INDUSTRIAL and WWII map to TL4; fromTechLevel returns the first match
            assertNotNull(TimePeriodTemplate.fromTechLevel(TechLevel.TL4));
            assertEquals(TechLevel.TL4, TimePeriodTemplate.fromTechLevel(TechLevel.TL4).getBaseTechLevel());
        }

        @Test
        @DisplayName("should find by TL13")
        void fromTechLevel_TL13() {
            assertEquals(TimePeriodTemplate.AVERAGE_STELLAR, TimePeriodTemplate.fromTechLevel(TechLevel.TL13));
        }

        @Test
        @DisplayName("should return null for TL with no template")
        void fromTechLevel_noMatch() {
            // TL11 has no template
            assertNull(TimePeriodTemplate.fromTechLevel(TechLevel.TL11));
        }

        @Test
        @DisplayName("should return null for TL0")
        void fromTechLevel_TL0() {
            assertNull(TimePeriodTemplate.fromTechLevel(TechLevel.TL0));
        }
    }

    @Nested
    @DisplayName("getStructureTypes Tests")
    class GetStructureTypesTests {

        @Test
        @DisplayName("should parse comma-separated structure types")
        void getStructureTypes_WWII() {
            String[] types = TimePeriodTemplate.WWII.getStructureTypes();
            assertTrue(types.length > 1);
            assertEquals("Bunkers", types[0].trim());
        }

        @Test
        @DisplayName("should parse structure types for MODERN")
        void getStructureTypes_MODERN() {
            String[] types = TimePeriodTemplate.MODERN.getStructureTypes();
            assertTrue(types.length > 1);
        }

        @Test
        @DisplayName("should parse structure types for ANCIENT")
        void getStructureTypes_ANCIENT() {
            String[] types = TimePeriodTemplate.ANCIENT.getStructureTypes();
            assertTrue(types.length > 1);
            assertEquals("Pyramids", types[0].trim());
        }
    }

    @Nested
    @DisplayName("getConflictTypes Tests")
    class GetConflictTypesTests {

        @Test
        @DisplayName("should parse comma-separated conflict types")
        void getConflictTypes_WWII() {
            String[] types = TimePeriodTemplate.WWII.getConflictTypes();
            assertTrue(types.length > 1);
            assertEquals("Blitzkrieg", types[0].trim());
        }

        @Test
        @DisplayName("should parse conflict types for MODERN")
        void getConflictTypes_MODERN() {
            String[] types = TimePeriodTemplate.MODERN.getConflictTypes();
            assertTrue(types.length > 1);
        }
    }

    @Nested
    @DisplayName("toString Tests")
    class ToStringTests {

        @Test
        @DisplayName("should format as name (TL)")
        void toString_format() {
            String s = TimePeriodTemplate.WWII.toString();
            assertTrue(s.contains("World War II"));
            assertTrue(s.contains("TL4"));
        }

        @Test
        @DisplayName("should format MODERN correctly")
        void toString_MODERN() {
            String s = TimePeriodTemplate.MODERN.toString();
            assertTrue(s.contains("Modern"));
            assertTrue(s.contains("TL5"));
        }
    }

    @Nested
    @DisplayName("Typical structures and conflicts Tests")
    class TypicalDataTests {

        @Test
        @DisplayName("WWII should have typical structures")
        void WWII_typicalStructures() {
            String structures = TimePeriodTemplate.WWII.getTypicalStructures();
            assertTrue(structures.contains("Bunkers"));
            assertTrue(structures.contains("airfields"));
            assertTrue(structures.contains("U-boat pens"));
        }

        @Test
        @DisplayName("WWII should have typical conflicts")
        void WWII_typicalConflicts() {
            String conflicts = TimePeriodTemplate.WWII.getTypicalConflicts();
            assertTrue(conflicts.contains("Blitzkrieg"));
            assertTrue(conflicts.contains("D-Day"));
        }

        @Test
        @DisplayName("COLD_WAR should have typical structures")
        void COLD_WAR_typicalStructures() {
            String structures = TimePeriodTemplate.COLD_WAR.getTypicalStructures();
            assertTrue(structures.contains("Silos"));
            assertTrue(structures.contains("space stations"));
        }

        @Test
        @DisplayName("SPACE_AGE should have typical structures")
        void SPACE_AGE_typicalStructures() {
            String structures = TimePeriodTemplate.SPACE_AGE.getTypicalStructures();
            assertTrue(structures.contains("Space stations"));
            assertTrue(structures.contains("lunar bases"));
        }
    }
}
