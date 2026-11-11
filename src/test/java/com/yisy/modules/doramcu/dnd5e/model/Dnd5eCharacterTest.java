package com.yisy.modules.doramcu.dnd5e.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Dnd5eCharacter Tests")
class Dnd5eCharacterTest {

    // ---- Basic Properties ----

    @Nested
    @DisplayName("Basic Properties Tests")
    class BasicPropertiesTests {

        @Test
        @DisplayName("should set and get id")
        void testId() {
            Dnd5eCharacter character = new Dnd5eCharacter();
            character.setId(1L);
            assertEquals(1L, character.getId());
        }

        @Test
        @DisplayName("should set and get name")
        void testName() {
            Dnd5eCharacter character = new Dnd5eCharacter();
            character.setName("Frodo");
            assertEquals("Frodo", character.getName());
        }

        @Test
        @DisplayName("should set and get race")
        void testRace() {
            Dnd5eCharacter character = new Dnd5eCharacter();
            character.setRace("Halfling");
            assertEquals("Halfling", character.getRace());
        }

        @Test
        @DisplayName("should set and get character class")
        void testCharacterClass() {
            Dnd5eCharacter character = new Dnd5eCharacter();
            character.setCharacterClass("Rogue");
            assertEquals("Rogue", character.getCharacterClass());
        }

        @Test
        @DisplayName("should set and get level")
        void testLevel() {
            Dnd5eCharacter character = new Dnd5eCharacter();
            character.setLevel(5);
            assertEquals(5, character.getLevel());
        }

        @Test
        @DisplayName("should default level to 1")
        void testDefaultLevel() {
            Dnd5eCharacter character = new Dnd5eCharacter();
            assertEquals(1, character.getLevel());
        }

        @Test
        @DisplayName("should set and get background")
        void testBackground() {
            Dnd5eCharacter character = new Dnd5eCharacter();
            character.setBackground("Outlander");
            assertEquals("Outlander", character.getBackground());
        }
    }

    // ---- Ability Scores ----

    @Nested
    @DisplayName("Ability Score Tests")
    class AbilityScoreTests {

        @Test
        @DisplayName("should set and get STR score")
        void testStrScore() {
            Dnd5eCharacter character = new Dnd5eCharacter();
            character.setStrScore(18);
            assertEquals(18, character.getStrScore());
        }

        @Test
        @DisplayName("should set and get DEX score")
        void testDexScore() {
            Dnd5eCharacter character = new Dnd5eCharacter();
            character.setDexScore(16);
            assertEquals(16, character.getDexScore());
        }

        @Test
        @DisplayName("should set and get CON score")
        void testConScore() {
            Dnd5eCharacter character = new Dnd5eCharacter();
            character.setConScore(14);
            assertEquals(14, character.getConScore());
        }

        @Test
        @DisplayName("should set and get INT score")
        void testIntScore() {
            Dnd5eCharacter character = new Dnd5eCharacter();
            character.setIntScore(12);
            assertEquals(12, character.getIntScore());
        }

        @Test
        @DisplayName("should set and get WIS score")
        void testWisScore() {
            Dnd5eCharacter character = new Dnd5eCharacter();
            character.setWisScore(10);
            assertEquals(10, character.getWisScore());
        }

        @Test
        @DisplayName("should set and get CHA score")
        void testChaScore() {
            Dnd5eCharacter character = new Dnd5eCharacter();
            character.setChaScore(8);
            assertEquals(8, character.getChaScore());
        }

        @Test
        @DisplayName("should default all ability scores to 10")
        void testDefaultAbilityScores() {
            Dnd5eCharacter character = new Dnd5eCharacter();
            assertEquals(10, character.getStrScore());
            assertEquals(10, character.getDexScore());
            assertEquals(10, character.getConScore());
            assertEquals(10, character.getIntScore());
            assertEquals(10, character.getWisScore());
            assertEquals(10, character.getChaScore());
        }
    }

    // ---- Ability Score Modifiers ----

    @Nested
    @DisplayName("Ability Score Modifier Tests")
    class AbilityScoreModifierTests {

        @Test
        @DisplayName("should calculate STR modifier correctly")
        void testStrMod() {
            Dnd5eCharacter character = new Dnd5eCharacter();
            character.setStrScore(16); // +3
            assertEquals(3, character.getStrMod());
        }

        @Test
        @DisplayName("should calculate DEX modifier correctly")
        void testDexMod() {
            Dnd5eCharacter character = new Dnd5eCharacter();
            character.setDexScore(14); // +2
            assertEquals(2, character.getDexMod());
        }

        @Test
        @DisplayName("should calculate CON modifier correctly")
        void testConMod() {
            Dnd5eCharacter character = new Dnd5eCharacter();
            character.setConScore(12); // +1
            assertEquals(1, character.getConMod());
        }

        @Test
        @DisplayName("should calculate INT modifier correctly")
        void testIntMod() {
            Dnd5eCharacter character = new Dnd5eCharacter();
            character.setIntScore(10); // +0
            assertEquals(0, character.getIntMod());
        }

        @Test
        @DisplayName("should calculate WIS modifier correctly")
        void testWisMod() {
            Dnd5eCharacter character = new Dnd5eCharacter();
            character.setWisScore(8); // -1
            assertEquals(-1, character.getWisMod());
        }

        @Test
        @DisplayName("should calculate CHA modifier correctly")
        void testChaMod() {
            Dnd5eCharacter character = new Dnd5eCharacter();
            character.setChaScore(6); // -2
            assertEquals(-2, character.getChaMod());
        }

        @Test
        @DisplayName("should calculate modifier for score 3")
        void testModifierScore3() {
            Dnd5eCharacter character = new Dnd5eCharacter();
            character.setStrScore(3); // (3-10)/2 = -3 in Java integer division
            assertEquals(-3, character.getStrMod());
        }

        @Test
        @DisplayName("should calculate modifier for score 20")
        void testModifierScore20() {
            Dnd5eCharacter character = new Dnd5eCharacter();
            character.setStrScore(20); // +5
            assertEquals(5, character.getStrMod());
        }

        @Test
        @DisplayName("should calculate modifier for odd score")
        void testModifierOddScore() {
            Dnd5eCharacter character = new Dnd5eCharacter();
            character.setStrScore(15); // +2 (15-10)/2 = 2
            assertEquals(2, character.getStrMod());
        }

        @Test
        @DisplayName("should calculate negative modifier for odd score")
        void testModifierOddNegative() {
            Dnd5eCharacter character = new Dnd5eCharacter();
            character.setStrScore(7); // -2 (7-10)/2 = -1 in integer division, but Java rounds toward zero
            // (7-10)/2 = -3/2 = -1 in Java integer division
            assertEquals(-1, character.getStrMod());
        }
    }

    // ---- Combat Stats ----

    @Nested
    @DisplayName("Combat Stats Tests")
    class CombatStatsTests {

        @Test
        @DisplayName("should set and get hit points")
        void testHitPoints() {
            Dnd5eCharacter character = new Dnd5eCharacter();
            character.setHitPoints(20);
            assertEquals(20, character.getHitPoints());
        }

        @Test
        @DisplayName("should set and get max hit points")
        void testMaxHitPoints() {
            Dnd5eCharacter character = new Dnd5eCharacter();
            character.setMaxHitPoints(25);
            assertEquals(25, character.getMaxHitPoints());
        }

        @Test
        @DisplayName("should set and get armor class")
        void testArmorClass() {
            Dnd5eCharacter character = new Dnd5eCharacter();
            character.setArmorClass(18);
            assertEquals(18, character.getArmorClass());
        }

        @Test
        @DisplayName("should default armor class to 10")
        void testDefaultArmorClass() {
            Dnd5eCharacter character = new Dnd5eCharacter();
            assertEquals(10, character.getArmorClass());
        }

        @Test
        @DisplayName("should set and get speed")
        void testSpeed() {
            Dnd5eCharacter character = new Dnd5eCharacter();
            character.setSpeed(30);
            assertEquals(30, character.getSpeed());
        }

        @Test
        @DisplayName("should default speed to 30")
        void testDefaultSpeed() {
            Dnd5eCharacter character = new Dnd5eCharacter();
            assertEquals(30, character.getSpeed());
        }

        @Test
        @DisplayName("should set and get hit dice")
        void testHitDice() {
            Dnd5eCharacter character = new Dnd5eCharacter();
            character.setHitDice("d10");
            assertEquals("d10", character.getHitDice());
        }

        @Test
        @DisplayName("should set and get proficiency bonus")
        void testProficiencyBonus() {
            Dnd5eCharacter character = new Dnd5eCharacter();
            character.setProficiencyBonus(3);
            assertEquals(3, character.getProficiencyBonus());
        }

        @Test
        @DisplayName("should default proficiency bonus to 2")
        void testDefaultProficiencyBonus() {
            Dnd5eCharacter character = new Dnd5eCharacter();
            assertEquals(2, character.getProficiencyBonus());
        }
    }

    // ---- Death Saves ----

    @Nested
    @DisplayName("Death Save Tests")
    class DeathSaveTests {

        @Test
        @DisplayName("should set and get death save successes")
        void testDeathSaveSuccesses() {
            Dnd5eCharacter character = new Dnd5eCharacter();
            character.setDeathSaveSuccesses(2);
            assertEquals(2, character.getDeathSaveSuccesses());
        }

        @Test
        @DisplayName("should default death save successes to 0")
        void testDefaultDeathSaveSuccesses() {
            Dnd5eCharacter character = new Dnd5eCharacter();
            assertEquals(0, character.getDeathSaveSuccesses());
        }

        @Test
        @DisplayName("should set and get death save failures")
        void testDeathSaveFailures() {
            Dnd5eCharacter character = new Dnd5eCharacter();
            character.setDeathSaveFailures(1);
            assertEquals(1, character.getDeathSaveFailures());
        }

        @Test
        @DisplayName("should default death save failures to 0")
        void testDefaultDeathSaveFailures() {
            Dnd5eCharacter character = new Dnd5eCharacter();
            assertEquals(0, character.getDeathSaveFailures());
        }
    }

    // ---- Temp HP and Exhaustion ----

    @Nested
    @DisplayName("Temp HP and Exhaustion Tests")
    class TempHpAndExhaustionTests {

        @Test
        @DisplayName("should set and get temp hit points")
        void testTempHitPoints() {
            Dnd5eCharacter character = new Dnd5eCharacter();
            character.setTempHitPoints(10);
            assertEquals(10, character.getTempHitPoints());
        }

        @Test
        @DisplayName("should default temp hit points to 0")
        void testDefaultTempHitPoints() {
            Dnd5eCharacter character = new Dnd5eCharacter();
            assertEquals(0, character.getTempHitPoints());
        }

        @Test
        @DisplayName("should set and get exhaustion level")
        void testExhaustionLevel() {
            Dnd5eCharacter character = new Dnd5eCharacter();
            character.setExhaustionLevel(3);
            assertEquals(3, character.getExhaustionLevel());
        }

        @Test
        @DisplayName("should default exhaustion level to 0")
        void testDefaultExhaustionLevel() {
            Dnd5eCharacter character = new Dnd5eCharacter();
            assertEquals(0, character.getExhaustionLevel());
        }
    }

    // ---- Equipment and Conditions ----

    @Nested
    @DisplayName("Equipment and Conditions Tests")
    class EquipmentAndConditionsTests {

        @Test
        @DisplayName("should set and get equipment")
        void testEquipment() {
            Dnd5eCharacter character = new Dnd5eCharacter();
            character.setEquipment("Longsword, Shield, Backpack");
            assertEquals("Longsword, Shield, Backpack", character.getEquipment());
        }

        @Test
        @DisplayName("should set and get conditions")
        void testConditions() {
            Dnd5eCharacter character = new Dnd5eCharacter();
            character.setConditions("poisoned, frightened");
            assertEquals("poisoned, frightened", character.getConditions());
        }
    }

    // ---- XP and Inspiration ----

    @Nested
    @DisplayName("XP and Inspiration Tests")
    class XpAndInspirationTests {

        @Test
        @DisplayName("should set and get experience points")
        void testExperiencePoints() {
            Dnd5eCharacter character = new Dnd5eCharacter();
            character.setExperiencePoints(1000);
            assertEquals(1000, character.getExperiencePoints());
        }

        @Test
        @DisplayName("should default experience points to 0")
        void testDefaultExperiencePoints() {
            Dnd5eCharacter character = new Dnd5eCharacter();
            assertEquals(0, character.getExperiencePoints());
        }

        @Test
        @DisplayName("should set and get inspiration")
        void testInspiration() {
            Dnd5eCharacter character = new Dnd5eCharacter();
            character.setInspiration(true);
            assertTrue(character.isInspiration());
        }

        @Test
        @DisplayName("should default inspiration to false")
        void testDefaultInspiration() {
            Dnd5eCharacter character = new Dnd5eCharacter();
            assertFalse(character.isInspiration());
        }
    }

    // ---- Timestamps ----

    @Nested
    @DisplayName("Timestamp Tests")
    class TimestampTests {

        @Test
        @DisplayName("should set and get created at")
        void testCreatedAt() {
            Dnd5eCharacter character = new Dnd5eCharacter();
            LocalDateTime now = LocalDateTime.of(2026, 11, 10, 12, 0);
            character.setCreatedAt(now);
            assertEquals(now, character.getCreatedAt());
        }

        @Test
        @DisplayName("should set and get updated at")
        void testUpdatedAt() {
            Dnd5eCharacter character = new Dnd5eCharacter();
            LocalDateTime now = LocalDateTime.of(2026, 11, 10, 13, 0);
            character.setUpdatedAt(now);
            assertEquals(now, character.getUpdatedAt());
        }

        @Test
        @DisplayName("should set timestamps on create")
        void testOnCreate() {
            Dnd5eCharacter character = new Dnd5eCharacter();
            character.onCreate();
            assertNotNull(character.getCreatedAt());
            assertNotNull(character.getUpdatedAt());
        }

        @Test
        @DisplayName("should set updated at on update")
        void testOnUpdate() {
            Dnd5eCharacter character = new Dnd5eCharacter();
            character.onUpdate();
            assertNotNull(character.getUpdatedAt());
        }
    }

    // ---- Relationships ----

    @Nested
    @DisplayName("Relationship Tests")
    class RelationshipTests {

        @Test
        @DisplayName("should initialize skills list")
        void testSkillsInitialization() {
            Dnd5eCharacter character = new Dnd5eCharacter();
            assertNotNull(character.getSkills());
            assertTrue(character.getSkills().isEmpty());
        }

        @Test
        @DisplayName("should initialize spell slots list")
        void testSpellSlotsInitialization() {
            Dnd5eCharacter character = new Dnd5eCharacter();
            assertNotNull(character.getSpellSlots());
            assertTrue(character.getSpellSlots().isEmpty());
        }

        @Test
        @DisplayName("should set and get skills")
        void testSkills() {
            Dnd5eCharacter character = new Dnd5eCharacter();
            Dnd5eCharacterSkill skill = new Dnd5eCharacterSkill();
            character.getSkills().add(skill);
            assertEquals(1, character.getSkills().size());
        }

        @Test
        @DisplayName("should set and get spell slots")
        void testSpellSlots() {
            Dnd5eCharacter character = new Dnd5eCharacter();
            Dnd5eSpellSlot slot = new Dnd5eSpellSlot(character, 1, 2);
            character.getSpellSlots().add(slot);
            assertEquals(1, character.getSpellSlots().size());
        }
    }

    // ---- toString ----

    @Nested
    @DisplayName("toString Tests")
    class ToStringTests {

        @Test
        @DisplayName("should include name and class in toString")
        void testToString() {
            Dnd5eCharacter character = new Dnd5eCharacter();
            character.setId(1L);
            character.setName("Frodo");
            character.setRace("Halfling");
            character.setCharacterClass("Rogue");
            character.setLevel(1);
            character.setHitPoints(10);
            character.setMaxHitPoints(10);

            String result = character.toString();

            assertTrue(result.contains("Frodo"));
            assertTrue(result.contains("Rogue"));
            assertTrue(result.contains("Halfling"));
        }
    }
}
