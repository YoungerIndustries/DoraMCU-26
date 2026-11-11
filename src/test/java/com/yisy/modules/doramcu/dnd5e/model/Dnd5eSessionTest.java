package com.yisy.modules.doramcu.dnd5e.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Dnd5eSession Entity Tests")
class Dnd5eSessionTest {

    @Nested
    @DisplayName("Basic Properties")
    class BasicPropertiesTests {

        @Test
        @DisplayName("should set and get session name")
        void sessionName_shouldWork() {
            Dnd5eSession session = new Dnd5eSession();
            session.setSessionName("The Beginning");
            assertEquals("The Beginning", session.getSessionName());
        }

        @Test
        @DisplayName("should set and get session number")
        void sessionNumber_shouldWork() {
            Dnd5eSession session = new Dnd5eSession();
            session.setSessionNumber(3);
            assertEquals(3, session.getSessionNumber());
        }

        @Test
        @DisplayName("should set and get session date")
        void sessionDate_shouldWork() {
            Dnd5eSession session = new Dnd5eSession();
            LocalDate date = LocalDate.of(2026, 11, 10);
            session.setSessionDate(date);
            assertEquals(date, session.getSessionDate());
        }

        @Test
        @DisplayName("should set and get notes")
        void notes_shouldWork() {
            Dnd5eSession session = new Dnd5eSession();
            session.setNotes("The party explored the dungeon");
            assertEquals("The party explored the dungeon", session.getNotes());
        }

        @Test
        @DisplayName("should default xp awarded to 0")
        void xpAwarded_shouldDefaultToZero() {
            Dnd5eSession session = new Dnd5eSession();
            assertEquals(0, session.getXpAwarded());
        }

        @Test
        @DisplayName("should set and get xp awarded")
        void xpAwarded_shouldSet() {
            Dnd5eSession session = new Dnd5eSession();
            session.setXpAwarded(500);
            assertEquals(500, session.getXpAwarded());
        }

        @Test
        @DisplayName("should default duration to 0")
        void durationMinutes_shouldDefaultToZero() {
            Dnd5eSession session = new Dnd5eSession();
            assertEquals(0, session.getDurationMinutes());
        }

        @Test
        @DisplayName("should set and get duration")
        void durationMinutes_shouldSet() {
            Dnd5eSession session = new Dnd5eSession();
            session.setDurationMinutes(180);
            assertEquals(180, session.getDurationMinutes());
        }

        @Test
        @DisplayName("should set and get id")
        void id_shouldWork() {
            Dnd5eSession session = new Dnd5eSession();
            session.setId(42L);
            assertEquals(42L, session.getId());
        }
    }

    @Nested
    @DisplayName("Lifecycle Callbacks")
    class LifecycleTests {

        @Test
        @DisplayName("should set createdAt and updatedAt on persist")
        void onCreate_shouldSetTimestamps() {
            Dnd5eSession session = new Dnd5eSession();
            session.onCreate();
            assertNotNull(session.getCreatedAt());
            assertNotNull(session.getUpdatedAt());
        }

        @Test
        @DisplayName("should update updatedAt on update")
        void onUpdate_shouldSetUpdatedAt() {
            Dnd5eSession session = new Dnd5eSession();
            session.onCreate();
            var created = session.getUpdatedAt();
            try {
                Thread.sleep(10);
            } catch (InterruptedException ignored) {}
            session.onUpdate();
            assertNotEquals(created, session.getUpdatedAt());
        }
    }
}
