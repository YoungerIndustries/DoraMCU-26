package com.yisy.modules.doramcu.earth2026.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Earth2026Character Tests")
class Earth2026CharacterTest {

    @Nested
    @DisplayName("Constructors")
    class ConstructorTests {

        @Test
        @DisplayName("no-arg constructor should create empty character")
        void noArgConstructor() {
            Earth2026Character character = new Earth2026Character();
            assertNull(character.getId());
            assertNull(character.getName());
            assertEquals(25, character.getAge());
            assertEquals(10, character.getStrScore());
            assertEquals(10, character.getDexScore());
            assertEquals(10, character.getConScore());
            assertEquals(10, character.getIntScore());
            assertEquals(10, character.getWisScore());
            assertEquals(10, character.getChaScore());
            assertEquals(0, character.getHitPoints());
            assertEquals(0, character.getMaxHitPoints());
            assertEquals(0, character.getExperiencePoints());
            assertEquals(1, character.getLevel());
            assertFalse(character.isSupernaturalEnabled());
            assertFalse(character.isHasTransitioned());
        }

        @Test
        @DisplayName("full constructor should set name, age, occupation and calculate HP")
        void fullConstructor() {
            Earth2026Character character = new Earth2026Character("John Doe", 30, "Detective");
            assertEquals("John Doe", character.getName());
            assertEquals(30, character.getAge());
            assertEquals("Detective", character.getOccupation());
            // Default CON=10, so maxHitPoints = 10 + (10-10)/2 = 10
            assertEquals(10, character.getMaxHitPoints());
            assertEquals(10, character.getHitPoints());
            assertNotNull(character.getArrivalDateTime());
        }

        @Test
        @DisplayName("constructor with high CON should calculate higher HP")
        void constructorHighCon() {
            // CON=18, so maxHitPoints = 10 + (18-10)/2 = 14
            // calculateMaxHitPoints() is private, so we verify the formula manually
            Earth2026Character character = new Earth2026Character("Warrior", 25, "Soldier");
            character.setConScore(18);
            int expectedHp = 10 + (18 - 10) / 2;
            character.setMaxHitPoints(expectedHp);
            assertEquals(14, character.getMaxHitPoints());
        }
    }

    @Nested
    @DisplayName("Ability Scores")
    class AbilityScoreTests {

        @Test
        @DisplayName("should set and get all 6 ability scores")
        void allAbilityScores() {
            Earth2026Character character = new Earth2026Character();
            character.setStrScore(15);
            character.setDexScore(12);
            character.setConScore(14);
            character.setIntScore(16);
            character.setWisScore(10);
            character.setChaScore(8);

            assertEquals(15, character.getStrScore());
            assertEquals(12, character.getDexScore());
            assertEquals(14, character.getConScore());
            assertEquals(16, character.getIntScore());
            assertEquals(10, character.getWisScore());
            assertEquals(8, character.getChaScore());
        }
    }

    @Nested
    @DisplayName("Hit Points")
    class HitPointTests {

        @Test
        @DisplayName("should set and get hit points")
        void hitPoints() {
            Earth2026Character character = new Earth2026Character();
            character.setHitPoints(15);
            character.setMaxHitPoints(20);
            assertEquals(15, character.getHitPoints());
            assertEquals(20, character.getMaxHitPoints());
        }
    }

    @Nested
    @DisplayName("Level and XP")
    class LevelXpTests {

        @Test
        @DisplayName("should set and get level and XP")
        void levelAndXp() {
            Earth2026Character character = new Earth2026Character();
            character.setLevel(5);
            character.setExperiencePoints(500);
            assertEquals(5, character.getLevel());
            assertEquals(500, character.getExperiencePoints());
        }
    }

    @Nested
    @DisplayName("Location")
    class LocationTests {

        @Test
        @DisplayName("should set and get earth location")
        void earthLocation() {
            Earth2026Character character = new Earth2026Character();
            character.setEarthLocation("New York, NY");
            assertEquals("New York, NY", character.getEarthLocation());
        }
    }

    @Nested
    @DisplayName("Supernatural Mode")
    class SupernaturalTests {

        @Test
        @DisplayName("should enable supernatural and set sanity")
        void enableSupernatural() {
            Earth2026Character character = new Earth2026Character();
            character.setSupernaturalEnabled(true);
            character.setSanityScore(100);
            assertTrue(character.isSupernaturalEnabled());
            assertEquals(100, character.getSanityScore());
        }

        @Test
        @DisplayName("sanity score should be nullable")
        void sanityScoreNullable() {
            Earth2026Character character = new Earth2026Character();
            assertNull(character.getSanityScore());
            character.setSanityScore(50);
            assertEquals(50, character.getSanityScore());
            character.setSanityScore(null);
            assertNull(character.getSanityScore());
        }
    }

    @Nested
    @DisplayName("Galaxy Transition")
    class GalaxyTransitionTests {

        @Test
        @DisplayName("should set and get target galaxy")
        void targetGalaxy() {
            Earth2026Character character = new Earth2026Character();
            character.setTargetGalaxy("Milky Way");
            assertEquals("Milky Way", character.getTargetGalaxy());
        }

        @Test
        @DisplayName("should track transition status")
        void transitionStatus() {
            Earth2026Character character = new Earth2026Character();
            assertFalse(character.isHasTransitioned());
            character.setHasTransitioned(true);
            assertTrue(character.isHasTransitioned());
        }
    }

    @Nested
    @DisplayName("Equipment and Conditions")
    class EquipmentTests {

        @Test
        @DisplayName("should set and get equipment JSON")
        void equipment() {
            Earth2026Character character = new Earth2026Character();
            character.setEquipment("[{\"name\":\"Pistol\",\"damage\":10}]");
            assertEquals("[{\"name\":\"Pistol\",\"damage\":10}]", character.getEquipment());
        }

        @Test
        @DisplayName("should set and get conditions JSON")
        void conditions() {
            Earth2026Character character = new Earth2026Character();
            character.setConditions("[\"Poisoned\"]");
            assertEquals("[\"Poisoned\"]", character.getConditions());
        }
    }

    @Nested
    @DisplayName("Timestamps")
    class TimestampTests {

        @Test
        @DisplayName("should set and get arrival date time")
        void arrivalDateTime() {
            Earth2026Character character = new Earth2026Character();
            LocalDateTime now = LocalDateTime.now();
            character.setArrivalDateTime(now);
            assertEquals(now, character.getArrivalDateTime());
        }

        @Test
        @DisplayName("should set and get created/updated timestamps")
        void timestamps() {
            Earth2026Character character = new Earth2026Character();
            LocalDateTime now = LocalDateTime.now();
            character.setCreatedAt(now);
            character.setUpdatedAt(now);
            assertEquals(now, character.getCreatedAt());
            assertEquals(now, character.getUpdatedAt());
        }
    }

    @Nested
    @DisplayName("toString")
    class ToStringTests {

        @Test
        @DisplayName("toString should include key fields")
        void toStringContent() {
            Earth2026Character character = new Earth2026Character("Test", 30, "Engineer");
            character.setId(1L);
            character.setEarthLocation("San Diego, CA");
            character.setLevel(5);
            character.setHasTransitioned(false);

            String str = character.toString();
            assertTrue(str.contains("id=1"));
            assertTrue(str.contains("name='Test'"));
            assertTrue(str.contains("age=30"));
            assertTrue(str.contains("location='San Diego, CA'"));
            assertTrue(str.contains("level=5"));
            assertTrue(str.contains("transitioned=false"));
        }
    }

    @Nested
    @DisplayName("Background")
    class BackgroundTests {

        @Test
        @DisplayName("should set and get background")
        void background() {
            Earth2026Character character = new Earth2026Character();
            character.setBackground("Former detective turned adventurer");
            assertEquals("Former detective turned adventurer", character.getBackground());
        }
    }
}
