package com.yisy.modules.doramcu.babylon5.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("B5Career Tests")
class B5CareerTest {

    @Nested
    @DisplayName("Enum values")
    class EnumValues {

        @Test
        @DisplayName("All career enum values exist")
        void allValues() {
            B5Career[] values = B5Career.values();
            assertEquals(13, values.length);
        }

        @Test
        @DisplayName("Career values")
        void careerValues() {
            assertSame(B5Career.EARTH_FORCE, B5Career.valueOf("EARTH_FORCE"));
            assertSame(B5Career.NARN_MILITARY, B5Career.valueOf("NARN_MILITARY"));
            assertSame(B5Career.CENTAURI_NOBILITY, B5Career.valueOf("CENTAURI_NOBILITY"));
            assertSame(B5Career.MINBARI_WARRIOR, B5Career.valueOf("MINBARI_WARRIOR"));
            assertSame(B5Career.MINBARI_TECHNICAL, B5Career.valueOf("MINBARI_TECHNICAL"));
            assertSame(B5Career.INDEPENDENT_MERCHANT, B5Career.valueOf("INDEPENDENT_MERCHANT"));
            assertSame(B5Career.INDEPENDENT_ADVENTURER, B5Career.valueOf("INDEPENDENT_ADVENTURER"));
            assertSame(B5Career.VREE_MERCHANT, B5Career.valueOf("VREE_MERCHANT"));
            assertSame(B5Career.HYACH_PEACEKEEPER, B5Career.valueOf("HYACH_PEACEKEEPER"));
            assertSame(B5Career.BRAKIRI_SPIRITUAL, B5Career.valueOf("BRAKIRI_SPIRITUAL"));
            assertSame(B5Career.PAKMARA_RESISTANCE, B5Career.valueOf("PAKMARA_RESISTANCE"));
            assertSame(B5Career.GOBLYN_SCAVENGER, B5Career.valueOf("GOBLYN_SCAVENGER"));
        }
    }

    @Nested
    @DisplayName("Career properties")
    class CareerProperties {

        @Test
        @DisplayName("Each career has a name")
        void names() {
            for (B5Career career : B5Career.values()) {
                assertNotNull(career.getName());
                assertFalse(career.getName().isEmpty());
            }
        }

        @Test
        @DisplayName("Each career has a description")
        void descriptions() {
            for (B5Career career : B5Career.values()) {
                assertNotNull(career.getDescription());
                assertFalse(career.getDescription().isEmpty());
            }
        }

        @Test
        @DisplayName("Each career has a faction")
        void factions() {
            for (B5Career career : B5Career.values()) {
                assertNotNull(career.getFaction());
                assertFalse(career.getFaction().isEmpty());
            }
        }

        @Test
        @DisplayName("Each career has attribute bonuses")
        void attributeBonuses() {
            for (B5Career career : B5Career.values()) {
                assertNotNull(career.getAttributeBonuses());
                assertTrue(career.getAttributeBonuses().size() >= 2);
            }
        }

        @Test
        @DisplayName("Each career has a skill table")
        void skillTables() {
            for (B5Career career : B5Career.values()) {
                assertNotNull(career.getSkillTable());
                assertEquals(10, career.getSkillTable().size());
            }
        }

        @Test
        @DisplayName("Specific career properties")
        void specificProperties() {
            assertEquals("Earth Force", B5Career.EARTH_FORCE.getName());
            assertEquals("Earth Alliance military service", B5Career.EARTH_FORCE.getDescription());
            assertEquals("Earth Alliance", B5Career.EARTH_FORCE.getFaction());
            assertTrue(B5Career.EARTH_FORCE.getAttributeBonuses().contains("ST+1"));
            assertTrue(B5Career.EARTH_FORCE.getAttributeBonuses().contains("DX+1"));
        }
    }

    @Nested
    @DisplayName("Military vs Civilian classification")
    class MilitaryVsCivilian {

        @Test
        @DisplayName("Military careers")
        void militaryCareers() {
            assertTrue(B5Career.EARTH_FORCE.isMilitaryCareer());
            assertTrue(B5Career.NARN_MILITARY.isMilitaryCareer());
            assertTrue(B5Career.MINBARI_WARRIOR.isMilitaryCareer());
            assertTrue(B5Career.HYACH_PEACEKEEPER.isMilitaryCareer());
            assertTrue(B5Career.MARKAB_MERCENARY.isMilitaryCareer());
        }

        @Test
        @DisplayName("Civilian careers")
        void civilianCareers() {
            assertTrue(B5Career.CENTAURI_NOBILITY.isCivilianCareer());
            assertTrue(B5Career.MINBARI_TECHNICAL.isCivilianCareer());
            assertTrue(B5Career.INDEPENDENT_MERCHANT.isCivilianCareer());
            assertTrue(B5Career.INDEPENDENT_ADVENTURER.isCivilianCareer());
            assertTrue(B5Career.VREE_MERCHANT.isCivilianCareer());
            assertTrue(B5Career.BRAKIRI_SPIRITUAL.isCivilianCareer());
            assertTrue(B5Career.PAKMARA_RESISTANCE.isCivilianCareer());
            assertTrue(B5Career.GOBLYN_SCAVENGER.isCivilianCareer());
        }

        @Test
        @DisplayName("Military and civilian are mutually exclusive")
        void mutualExclusion() {
            for (B5Career career : B5Career.values()) {
                assertNotEquals(career.isMilitaryCareer(), career.isCivilianCareer());
            }
        }
    }

    @Nested
    @DisplayName("All races flag")
    class AllRaces {

        @Test
        @DisplayName("All careers allow all races")
        void allRaces() {
            for (B5Career career : B5Career.values()) {
                assertTrue(career.isAllRaces());
            }
        }
    }
}
