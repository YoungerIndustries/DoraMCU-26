package com.yisy.modules.doramcu.captainslog.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CaptainLogCrewMember Tests")
class CaptainLogCrewMemberTest {

    @Nested
    @DisplayName("Constructor")
    class Constructor {

        @Test
        @DisplayName("Default constructor initializes defaults")
        void defaults() {
            CaptainLogCrewMember crew = new CaptainLogCrewMember();
            assertNull(crew.getId());
            assertNull(crew.getName());
            assertNull(crew.getSpecies());
            assertNull(crew.getRole());
            assertNull(crew.getPrimaryDiscipline());
            assertNull(crew.getSecondaryDiscipline());
            assertEquals(1, crew.getExpertiseRating());
            assertNull(crew.getRelationship());
            assertNull(crew.getDescription());
            assertTrue(crew.isActive());
            assertNull(crew.getGalaxyName());
            assertNull(crew.getCharacter());
        }

        @Test
        @DisplayName("Parameterized constructor sets name, role, primary discipline")
        void parameterized() {
            CaptainLogCrewMember crew = new CaptainLogCrewMember("Scotty", "Chief Engineer", CaptainLogCharacter.Discipline.OPERATIONS);
            assertEquals("Scotty", crew.getName());
            assertEquals("Chief Engineer", crew.getRole());
            assertEquals(CaptainLogCharacter.Discipline.OPERATIONS, crew.getPrimaryDiscipline());
        }
    }

    @Nested
    @DisplayName("Discipline")
    class Discipline {

        @Test
        @DisplayName("Set primary and secondary discipline")
        void setDisciplines() {
            CaptainLogCrewMember crew = new CaptainLogCrewMember();
            crew.setPrimaryDiscipline(CaptainLogCharacter.Discipline.SCIENCE);
            crew.setSecondaryDiscipline(CaptainLogCharacter.Discipline.MEDICINE);
            assertEquals(CaptainLogCharacter.Discipline.SCIENCE, crew.getPrimaryDiscipline());
            assertEquals(CaptainLogCharacter.Discipline.MEDICINE, crew.getSecondaryDiscipline());
        }
    }

    @Nested
    @DisplayName("Expertise")
    class Expertise {

        @Test
        @DisplayName("Default expertise is 1")
        void defaultExpertise() {
            CaptainLogCrewMember crew = new CaptainLogCrewMember();
            assertEquals(1, crew.getExpertiseRating());
        }

        @Test
        @DisplayName("Set expertise rating")
        void setExpertise() {
            CaptainLogCrewMember crew = new CaptainLogCrewMember();
            crew.setExpertiseRating(3);
            assertEquals(3, crew.getExpertiseRating());
        }
    }

    @Nested
    @DisplayName("Active status")
    class ActiveStatus {

        @Test
        @DisplayName("Default is active")
        void defaultActive() {
            CaptainLogCrewMember crew = new CaptainLogCrewMember();
            assertTrue(crew.isActive());
        }

        @Test
        @DisplayName("Set active to false")
        void setActiveFalse() {
            CaptainLogCrewMember crew = new CaptainLogCrewMember();
            crew.setActive(false);
            assertFalse(crew.isActive());
        }
    }

    @Nested
    @DisplayName("Relationship")
    class Relationship {

        @Test
        @DisplayName("Set relationship")
        void setRelationship() {
            CaptainLogCrewMember crew = new CaptainLogCrewMember();
            crew.setRelationship("Trusted Friend");
            assertEquals("Trusted Friend", crew.getRelationship());
        }
    }
}
