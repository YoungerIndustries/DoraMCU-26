package com.yisy.modules.doramcu.babylon5.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("B5Relationship Tests")
class B5RelationshipTest {

    @Nested
    @DisplayName("Default constructor")
    class DefaultConstructor {

        @Test
        @DisplayName("Fields default to null")
        void defaults() {
            B5Relationship rel = new B5Relationship();
            assertNull(rel.getCharacterId());
            assertNull(rel.getType());
        }
    }

    @Nested
    @DisplayName("Parameterized constructor")
    class ParameterizedConstructor {

        @Test
        @DisplayName("Sets characterId and type")
        void setsFields() {
            B5Relationship rel = new B5Relationship("sheridan", "Friend");
            assertEquals("sheridan", rel.getCharacterId());
            assertEquals("Friend", rel.getType());
        }
    }

    @Nested
    @DisplayName("Setters")
    class Setters {

        @Test
        @DisplayName("Set characterId")
        void setCharacterId() {
            B5Relationship rel = new B5Relationship();
            rel.setCharacterId("ivanova");
            assertEquals("ivanova", rel.getCharacterId());
        }

        @Test
        @DisplayName("Set type")
        void setType() {
            B5Relationship rel = new B5Relationship();
            rel.setType("Rival");
            assertEquals("Rival", rel.getType());
        }
    }

    @Nested
    @DisplayName("toString")
    class ToString {

        @Test
        @DisplayName("toString includes characterId and type")
        void toStringContent() {
            B5Relationship rel = new B5Relationship("sheridan", "Friend");
            String result = rel.toString();
            assertTrue(result.contains("sheridan"));
            assertTrue(result.contains("Friend"));
        }
    }
}
