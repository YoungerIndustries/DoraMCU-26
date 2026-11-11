package com.yisy.modules.doramcu.babylon5.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("B5Character Tests")
class B5CharacterTest {

    @Nested
    @DisplayName("Constructor")
    class Constructor {

        @Test
        @DisplayName("Default constructor initializes null defaults")
        void defaults() {
            B5Character character = new B5Character();
            assertNull(character.getId());
            assertNull(character.getName());
            assertNull(character.getSpecies());
            assertNull(character.getActor());
            assertNull(character.getRole());
            assertNull(character.getDescription());
            assertNull(character.getFirstAppearance());
            assertNull(character.getLastAppearance());
            assertNull(character.getPersonality());
            assertNull(character.getArchetype());
            assertNull(character.getCharacteristics());
            assertNull(character.getKeySkills());
            assertNull(character.getStoryArc());
            assertNull(character.getRelationships());
            assertNull(character.getFamousQuotes());
        }
    }

    @Nested
    @DisplayName("Field setters and getters")
    class Fields {

        @Test
        @DisplayName("Set and get id")
        void id() {
            B5Character character = new B5Character();
            character.setId("char-1");
            assertEquals("char-1", character.getId());
        }

        @Test
        @DisplayName("Set and get name")
        void name() {
            B5Character character = new B5Character();
            character.setName("Sheridan");
            assertEquals("Sheridan", character.getName());
        }

        @Test
        @DisplayName("Set and get species")
        void species() {
            B5Character character = new B5Character();
            character.setSpecies("Human");
            assertEquals("Human", character.getSpecies());
        }

        @Test
        @DisplayName("Set and get actor")
        void actor() {
            B5Character character = new B5Character();
            character.setActor("Bruce Boxleitner");
            assertEquals("Bruce Boxleitner", character.getActor());
        }

        @Test
        @DisplayName("Set and get role")
        void role() {
            B5Character character = new B5Character();
            character.setRole("Commanding Officer");
            assertEquals("Commanding Officer", character.getRole());
        }

        @Test
        @DisplayName("Set and get description")
        void description() {
            B5Character character = new B5Character();
            character.setDescription("Earth Force commander");
            assertEquals("Earth Force commander", character.getDescription());
        }

        @Test
        @DisplayName("Set and get appearances")
        void appearances() {
            B5Character character = new B5Character();
            character.setFirstAppearance("The Gathering");
            character.setLastAppearance("Sleeping in Light");
            assertEquals("The Gathering", character.getFirstAppearance());
            assertEquals("Sleeping in Light", character.getLastAppearance());
        }

        @Test
        @DisplayName("Set and get archetype")
        void archetype() {
            B5Character character = new B5Character();
            character.setArchetype("Hero");
            assertEquals("Hero", character.getArchetype());
        }

        @Test
        @DisplayName("Set and get story arc")
        void storyArc() {
            B5Character character = new B5Character();
            character.setStoryArc("Rising from captain to commander");
            assertEquals("Rising from captain to commander", character.getStoryArc());
        }
    }

    @Nested
    @DisplayName("Complex fields")
    class ComplexFields {

        @Test
        @DisplayName("Set and get personality")
        void personality() {
            B5Character character = new B5Character();
            B5Personality personality = new B5Personality();
            personality.setAggression(0.8);
            character.setPersonality(personality);
            assertSame(personality, character.getPersonality());
            assertEquals(0.8, character.getPersonality().getAggression());
        }

        @Test
        @DisplayName("Set and get characteristics")
        void characteristics() {
            B5Character character = new B5Character();
            B5Characteristics characteristics = new B5Characteristics(10, 9, 8, 7, 6, 5);
            character.setCharacteristics(characteristics);
            assertSame(characteristics, character.getCharacteristics());
            assertEquals(10, character.getCharacteristics().getST());
        }

        @Test
        @DisplayName("Set and get key skills")
        void keySkills() {
            B5Character character = new B5Character();
            List<String> skills = Arrays.asList("Leadership", "Tactics");
            character.setKeySkills(skills);
            assertSame(skills, character.getKeySkills());
            assertEquals(2, character.getKeySkills().size());
        }

        @Test
        @DisplayName("Set and get relationships")
        void relationships() {
            B5Character character = new B5Character();
            B5Relationship rel = new B5Relationship("ivanova", "Friend");
            character.setRelationships(Arrays.asList(rel));
            assertEquals(1, character.getRelationships().size());
            assertEquals("ivanova", character.getRelationships().get(0).getCharacterId());
        }

        @Test
        @DisplayName("Set and get famous quotes")
        void famousQuotes() {
            B5Character character = new B5Character();
            List<String> quotes = Arrays.asList("I have a bad feeling about this");
            character.setFamousQuotes(quotes);
            assertEquals(1, character.getFamousQuotes().size());
        }
    }

    @Nested
    @DisplayName("toString")
    class ToString {

        @Test
        @DisplayName("toString includes id, name, and species")
        void toStringContent() {
            B5Character character = new B5Character();
            character.setId("char-1");
            character.setName("Sheridan");
            character.setSpecies("Human");
            String result = character.toString();
            assertTrue(result.contains("char-1"));
            assertTrue(result.contains("Sheridan"));
            assertTrue(result.contains("Human"));
        }
    }
}
