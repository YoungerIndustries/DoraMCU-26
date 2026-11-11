package com.yisy.modules.doramcu.captainslog.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CaptainLogFocus Tests")
class CaptainLogFocusTest {

    @Nested
    @DisplayName("Constructor")
    class Constructor {

        @Test
        @DisplayName("Default constructor initializes defaults")
        void defaults() {
            CaptainLogFocus focus = new CaptainLogFocus();
            assertNull(focus.getId());
            assertNull(focus.getCharacter());
            assertNull(focus.getDiscipline());
            assertNull(focus.getName());
        }

        @Test
        @DisplayName("Parameterized constructor sets discipline and name")
        void parameterized() {
            CaptainLogFocus focus = new CaptainLogFocus(CaptainLogFocus.Discipline.SCIENCE, "Xenobiology");
            assertEquals(CaptainLogFocus.Discipline.SCIENCE, focus.getDiscipline());
            assertEquals("Xenobiology", focus.getName());
        }
    }

    @Nested
    @DisplayName("Discipline enum")
    class DisciplineEnum {

        @Test
        @DisplayName("All discipline values")
        void allDisciplines() {
            assertSame(CaptainLogFocus.Discipline.COMMAND, CaptainLogFocus.Discipline.valueOf("COMMAND"));
            assertSame(CaptainLogFocus.Discipline.LEADERSHIP, CaptainLogFocus.Discipline.valueOf("LEADERSHIP"));
            assertSame(CaptainLogFocus.Discipline.SCIENCE, CaptainLogFocus.Discipline.valueOf("SCIENCE"));
            assertSame(CaptainLogFocus.Discipline.SECURITY, CaptainLogFocus.Discipline.valueOf("SECURITY"));
            assertSame(CaptainLogFocus.Discipline.MEDICINE, CaptainLogFocus.Discipline.valueOf("MEDICINE"));
            assertSame(CaptainLogFocus.Discipline.OPERATIONS, CaptainLogFocus.Discipline.valueOf("OPERATIONS"));
        }
    }

    @Nested
    @DisplayName("Setters")
    class Setters {

        @Test
        @DisplayName("Set discipline")
        void setDiscipline() {
            CaptainLogFocus focus = new CaptainLogFocus();
            focus.setDiscipline(CaptainLogFocus.Discipline.SECURITY);
            assertEquals(CaptainLogFocus.Discipline.SECURITY, focus.getDiscipline());
        }

        @Test
        @DisplayName("Set name")
        void setName() {
            CaptainLogFocus focus = new CaptainLogFocus();
            focus.setName("Starship Tactics");
            assertEquals("Starship Tactics", focus.getName());
        }
    }

    @Nested
    @DisplayName("toString")
    class ToString {

        @Test
        @DisplayName("toString includes discipline and name")
        void toStringIncludesFields() {
            CaptainLogFocus focus = new CaptainLogFocus(CaptainLogFocus.Discipline.COMMAND, "Starship Tactics");
            String str = focus.toString();
            assertTrue(str.contains("COMMAND"));
            assertTrue(str.contains("Starship Tactics"));
        }
    }
}
