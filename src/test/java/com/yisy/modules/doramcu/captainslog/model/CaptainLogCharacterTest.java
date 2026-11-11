package com.yisy.modules.doramcu.captainslog.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CaptainLogCharacter Tests")
class CaptainLogCharacterTest {

    @Nested
    @DisplayName("Constructor")
    class Constructor {

        @Test
        @DisplayName("Default constructor initializes defaults")
        void defaults() {
            CaptainLogCharacter charact = new CaptainLogCharacter();
            assertNull(charact.getId());
            assertNull(charact.getName());
            assertNull(charact.getPronouns());
            assertNull(charact.getRank());
            assertNull(charact.getAssignment());
            assertNull(charact.getEnvironment());
            assertNull(charact.getOutlook());
            assertNull(charact.getEducation());
            assertEquals(0, charact.getFitness());
            assertEquals(0, charact.getTechnical());
            assertEquals(0, charact.getStrength());
            assertEquals(0, charact.getReason());
            assertEquals(0, charact.getInsight());
            assertEquals(0, charact.getPresence());
            assertEquals(0, charact.getConstitution());
            assertEquals(0, charact.getCommand());
            assertEquals(0, charact.getLeadership());
            assertEquals(0, charact.getScience());
            assertEquals(0, charact.getSecurity());
            assertEquals(0, charact.getMedicine());
            assertEquals(0, charact.getOperations());
            assertTrue(charact.getFocuses().isEmpty());
            assertTrue(charact.getValues().isEmpty());
            assertTrue(charact.getCrewMembers().isEmpty());
            assertTrue(charact.getTalents().isEmpty());
            assertNull(charact.getEquipment());
            assertEquals(0, charact.getCareerEvents());
            assertEquals(0, charact.getMissionsCompleted());
            assertEquals(0, charact.getExperiencePoints());
            assertEquals(CaptainLogCharacter.InjuryState.HEALTHY, charact.getInjuryState());
        }

        @Test
        @DisplayName("Parameterized constructor sets name, rank, assignment")
        void parameterized() {
            CaptainLogCharacter charact = new CaptainLogCharacter("Janeway", CaptainLogCharacter.Rank.CAPTAIN, CaptainLogCharacter.Assignment.COMMAND);
            assertEquals("Janeway", charact.getName());
            assertEquals(CaptainLogCharacter.Rank.CAPTAIN, charact.getRank());
            assertEquals(CaptainLogCharacter.Assignment.COMMAND, charact.getAssignment());
        }
    }

    @Nested
    @DisplayName("Enums")
    class Enums {

        @Test
        @DisplayName("Rank values")
        void rank() {
            assertSame(CaptainLogCharacter.Rank.ENSIGN, CaptainLogCharacter.Rank.valueOf("ENSIGN"));
            assertSame(CaptainLogCharacter.Rank.LIEUTENANT, CaptainLogCharacter.Rank.valueOf("LIEUTENANT"));
            assertSame(CaptainLogCharacter.Rank.CAPTAIN, CaptainLogCharacter.Rank.valueOf("CAPTAIN"));
            assertSame(CaptainLogCharacter.Rank.COMMANDING_OFFICER, CaptainLogCharacter.Rank.valueOf("COMMANDING_OFFICER"));
        }

        @Test
        @DisplayName("Assignment values")
        void assignment() {
            assertSame(CaptainLogCharacter.Assignment.COMMAND, CaptainLogCharacter.Assignment.valueOf("COMMAND"));
            assertSame(CaptainLogCharacter.Assignment.SCIENCE, CaptainLogCharacter.Assignment.valueOf("SCIENCE"));
            assertSame(CaptainLogCharacter.Assignment.SECURITY, CaptainLogCharacter.Assignment.valueOf("SECURITY"));
            assertSame(CaptainLogCharacter.Assignment.ENGINEERING, CaptainLogCharacter.Assignment.valueOf("ENGINEERING"));
            assertSame(CaptainLogCharacter.Assignment.MEDICAL, CaptainLogCharacter.Assignment.valueOf("MEDICAL"));
            assertSame(CaptainLogCharacter.Assignment.OPERATIONS, CaptainLogCharacter.Assignment.valueOf("OPERATIONS"));
        }

        @Test
        @DisplayName("Environment values")
        void environment() {
            assertSame(CaptainLogCharacter.Environment.CITY, CaptainLogCharacter.Environment.valueOf("CITY"));
            assertSame(CaptainLogCharacter.Environment.SPACE, CaptainLogCharacter.Environment.valueOf("SPACE"));
            assertSame(CaptainLogCharacter.Environment.WILD, CaptainLogCharacter.Environment.valueOf("WILD"));
        }

        @Test
        @DisplayName("Outlook values")
        void outlook() {
            assertSame(CaptainLogCharacter.Outlook.OPTIMISTIC, CaptainLogCharacter.Outlook.valueOf("OPTIMISTIC"));
            assertSame(CaptainLogCharacter.Outlook.PESSIMISTIC, CaptainLogCharacter.Outlook.valueOf("PESSIMISTIC"));
            assertSame(CaptainLogCharacter.Outlook.PRAGMATIC, CaptainLogCharacter.Outlook.valueOf("PRAGMATIC"));
            assertSame(CaptainLogCharacter.Outlook.IDEALISTIC, CaptainLogCharacter.Outlook.valueOf("IDEALISTIC"));
            assertSame(CaptainLogCharacter.Outlook.CYNICAL, CaptainLogCharacter.Outlook.valueOf("CYNICAL"));
            assertSame(CaptainLogCharacter.Outlook.ADVENTUROUS, CaptainLogCharacter.Outlook.valueOf("ADVENTUROUS"));
        }

        @Test
        @DisplayName("Education values")
        void education() {
            assertSame(CaptainLogCharacter.Education.ACADEMIC, CaptainLogCharacter.Education.valueOf("ACADEMIC"));
            assertSame(CaptainLogCharacter.Education.MILITARY, CaptainLogCharacter.Education.valueOf("MILITARY"));
            assertSame(CaptainLogCharacter.Education.SELF_TAUGHT, CaptainLogCharacter.Education.valueOf("SELF_TAUGHT"));
            assertSame(CaptainLogCharacter.Education.APPRENTICE, CaptainLogCharacter.Education.valueOf("APPRENTICE"));
            assertSame(CaptainLogCharacter.Education.ACADEMY, CaptainLogCharacter.Education.valueOf("ACADEMY"));
            assertSame(CaptainLogCharacter.Education.FIELD_TRAINING, CaptainLogCharacter.Education.valueOf("FIELD_TRAINING"));
        }

        @Test
        @DisplayName("InjuryState values")
        void injuryState() {
            assertSame(CaptainLogCharacter.InjuryState.HEALTHY, CaptainLogCharacter.InjuryState.valueOf("HEALTHY"));
            assertSame(CaptainLogCharacter.InjuryState.STUNNED, CaptainLogCharacter.InjuryState.valueOf("STUNNED"));
            assertSame(CaptainLogCharacter.InjuryState.INJURED, CaptainLogCharacter.InjuryState.valueOf("INJURED"));
            assertSame(CaptainLogCharacter.InjuryState.DOWN, CaptainLogCharacter.InjuryState.valueOf("DOWN"));
        }

        @Test
        @DisplayName("Attribute values")
        void attribute() {
            assertSame(CaptainLogCharacter.Attribute.FITNESS, CaptainLogCharacter.Attribute.valueOf("FITNESS"));
            assertSame(CaptainLogCharacter.Attribute.TECHNICAL, CaptainLogCharacter.Attribute.valueOf("TECHNICAL"));
            assertSame(CaptainLogCharacter.Attribute.STRENGTH, CaptainLogCharacter.Attribute.valueOf("STRENGTH"));
            assertSame(CaptainLogCharacter.Attribute.REASON, CaptainLogCharacter.Attribute.valueOf("REASON"));
            assertSame(CaptainLogCharacter.Attribute.INSIGHT, CaptainLogCharacter.Attribute.valueOf("INSIGHT"));
            assertSame(CaptainLogCharacter.Attribute.PRESENCE, CaptainLogCharacter.Attribute.valueOf("PRESENCE"));
        }

        @Test
        @DisplayName("Discipline values")
        void discipline() {
            assertSame(CaptainLogCharacter.Discipline.COMMAND, CaptainLogCharacter.Discipline.valueOf("COMMAND"));
            assertSame(CaptainLogCharacter.Discipline.LEADERSHIP, CaptainLogCharacter.Discipline.valueOf("LEADERSHIP"));
            assertSame(CaptainLogCharacter.Discipline.SCIENCE, CaptainLogCharacter.Discipline.valueOf("SCIENCE"));
            assertSame(CaptainLogCharacter.Discipline.SECURITY, CaptainLogCharacter.Discipline.valueOf("SECURITY"));
            assertSame(CaptainLogCharacter.Discipline.MEDICINE, CaptainLogCharacter.Discipline.valueOf("MEDICINE"));
            assertSame(CaptainLogCharacter.Discipline.OPERATIONS, CaptainLogCharacter.Discipline.valueOf("OPERATIONS"));
        }
    }

    @Nested
    @DisplayName("Attribute value lookup")
    class AttributeValueLookup {

        @Test
        @DisplayName("Get attribute value by enum")
        void getAttributeValue() {
            CaptainLogCharacter charact = new CaptainLogCharacter();
            charact.setFitness(3);
            charact.setTechnical(2);
            charact.setStrength(4);
            charact.setReason(1);
            charact.setInsight(2);
            charact.setPresence(3);
            assertEquals(3, charact.getAttributeValue(CaptainLogCharacter.Attribute.FITNESS));
            assertEquals(2, charact.getAttributeValue(CaptainLogCharacter.Attribute.TECHNICAL));
            assertEquals(4, charact.getAttributeValue(CaptainLogCharacter.Attribute.STRENGTH));
            assertEquals(1, charact.getAttributeValue(CaptainLogCharacter.Attribute.REASON));
            assertEquals(2, charact.getAttributeValue(CaptainLogCharacter.Attribute.INSIGHT));
            assertEquals(3, charact.getAttributeValue(CaptainLogCharacter.Attribute.PRESENCE));
        }

        @Test
        @DisplayName("Null attribute returns 0")
        void nullAttribute() {
            CaptainLogCharacter charact = new CaptainLogCharacter();
            assertEquals(0, charact.getAttributeValue(null));
        }
    }

    @Nested
    @DisplayName("Discipline rating lookup")
    class DisciplineRatingLookup {

        @Test
        @DisplayName("Get discipline rating by enum")
        void getDisciplineRating() {
            CaptainLogCharacter charact = new CaptainLogCharacter();
            charact.setCommand(2);
            charact.setLeadership(1);
            charact.setScience(3);
            charact.setSecurity(2);
            charact.setMedicine(1);
            charact.setOperations(2);
            assertEquals(2, charact.getDisciplineRating(CaptainLogCharacter.Discipline.COMMAND));
            assertEquals(1, charact.getDisciplineRating(CaptainLogCharacter.Discipline.LEADERSHIP));
            assertEquals(3, charact.getDisciplineRating(CaptainLogCharacter.Discipline.SCIENCE));
            assertEquals(2, charact.getDisciplineRating(CaptainLogCharacter.Discipline.SECURITY));
            assertEquals(1, charact.getDisciplineRating(CaptainLogCharacter.Discipline.MEDICINE));
            assertEquals(2, charact.getDisciplineRating(CaptainLogCharacter.Discipline.OPERATIONS));
        }

        @Test
        @DisplayName("Null discipline returns 0")
        void nullDiscipline() {
            CaptainLogCharacter charact = new CaptainLogCharacter();
            assertEquals(0, charact.getDisciplineRating(null));
        }
    }

    @Nested
    @DisplayName("Focus check")
    class FocusCheck {

        @Test
        @DisplayName("Has focus returns true for matching focus")
        void hasFocus() {
            CaptainLogCharacter charact = new CaptainLogCharacter();
            CaptainLogFocus focus = new CaptainLogFocus(CaptainLogFocus.Discipline.SCIENCE, "Xenobiology");
            charact.getFocuses().add(focus);
            assertTrue(charact.hasFocus(CaptainLogCharacter.Discipline.SCIENCE, "Xenobiology"));
        }

        @Test
        @DisplayName("Has focus is case insensitive")
        void caseInsensitive() {
            CaptainLogCharacter charact = new CaptainLogCharacter();
            CaptainLogFocus focus = new CaptainLogFocus(CaptainLogFocus.Discipline.SCIENCE, "Xenobiology");
            charact.getFocuses().add(focus);
            assertTrue(charact.hasFocus(CaptainLogCharacter.Discipline.SCIENCE, "xenobiology"));
        }

        @Test
        @DisplayName("Has focus returns false for non-matching")
        void noFocus() {
            CaptainLogCharacter charact = new CaptainLogCharacter();
            assertFalse(charact.hasFocus(CaptainLogCharacter.Discipline.SCIENCE, "Astrophysics"));
        }

        @Test
        @DisplayName("Null discipline returns false")
        void nullDiscipline() {
            CaptainLogCharacter charact = new CaptainLogCharacter();
            assertFalse(charact.hasFocus(null, "Xenobiology"));
        }

        @Test
        @DisplayName("Null focus name returns false")
        void nullFocusName() {
            CaptainLogCharacter charact = new CaptainLogCharacter();
            assertFalse(charact.hasFocus(CaptainLogCharacter.Discipline.SCIENCE, null));
        }
    }
}
