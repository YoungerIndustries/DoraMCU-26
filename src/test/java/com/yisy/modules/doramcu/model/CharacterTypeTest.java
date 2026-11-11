package com.yisy.modules.doramcu.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CharacterType")
class CharacterTypeTest {

    @Nested
    @DisplayName("Enum values")
    class EnumValues {

        @Test
        @DisplayName("has 7 values")
        void hasSevenValues() {
            assertEquals(7, CharacterType.values().length);
        }

        @Test
        @DisplayName("TRAVELLER exists")
        void travellerExists() {
            assertNotNull(CharacterType.TRAVELLER);
        }

        @Test
        @DisplayName("CAPTAINS_LOG exists")
        void captainsLogExists() {
            assertNotNull(CharacterType.CAPTAINS_LOG);
        }

        @Test
        @DisplayName("AI_CREW exists")
        void aiCrewExists() {
            assertNotNull(CharacterType.AI_CREW);
        }

        @Test
        @DisplayName("EARTH_2026 exists")
        void earth2026Exists() {
            assertNotNull(CharacterType.EARTH_2026);
        }

        @Test
        @DisplayName("DND_5E exists")
        void dnd5eExists() {
            assertNotNull(CharacterType.DND_5E);
        }

        @Test
        @DisplayName("BABYLON5 exists")
        void babylon5Exists() {
            assertNotNull(CharacterType.BABYLON5);
        }

        @Test
        @DisplayName("SFB exists")
        void sfbExists() {
            assertNotNull(CharacterType.SFB);
        }

        @Test
        @DisplayName("valueOf returns correct enum")
        void valueOfReturnsCorrect() {
            assertEquals(CharacterType.TRAVELLER, CharacterType.valueOf("TRAVELLER"));
            assertEquals(CharacterType.CAPTAINS_LOG, CharacterType.valueOf("CAPTAINS_LOG"));
            assertEquals(CharacterType.AI_CREW, CharacterType.valueOf("AI_CREW"));
            assertEquals(CharacterType.EARTH_2026, CharacterType.valueOf("EARTH_2026"));
            assertEquals(CharacterType.DND_5E, CharacterType.valueOf("DND_5E"));
            assertEquals(CharacterType.BABYLON5, CharacterType.valueOf("BABYLON5"));
            assertEquals(CharacterType.SFB, CharacterType.valueOf("SFB"));
        }

        @Test
        @DisplayName("valueOf throws for invalid name")
        void valueOfThrowsForInvalid() {
            assertThrows(IllegalArgumentException.class, () -> CharacterType.valueOf("INVALID"));
        }
    }
}
