package com.yisy.modules.doramcu.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CharacterRole")
class CharacterRoleTest {

    @Nested
    @DisplayName("Enum values")
    class EnumValues {

        @Test
        @DisplayName("has 4 values")
        void hasFourValues() {
            assertEquals(4, CharacterRole.values().length);
        }

        @Test
        @DisplayName("COMMANDER exists")
        void commanderExists() {
            assertNotNull(CharacterRole.COMMANDER);
        }

        @Test
        @DisplayName("CREW exists")
        void crewExists() {
            assertNotNull(CharacterRole.CREW);
        }

        @Test
        @DisplayName("OWNED exists")
        void ownedExists() {
            assertNotNull(CharacterRole.OWNED);
        }

        @Test
        @DisplayName("ENCOUNTERED exists")
        void encounteredExists() {
            assertNotNull(CharacterRole.ENCOUNTERED);
        }

        @Test
        @DisplayName("valueOf returns correct enum")
        void valueOfReturnsCorrect() {
            assertEquals(CharacterRole.COMMANDER, CharacterRole.valueOf("COMMANDER"));
            assertEquals(CharacterRole.CREW, CharacterRole.valueOf("CREW"));
            assertEquals(CharacterRole.OWNED, CharacterRole.valueOf("OWNED"));
            assertEquals(CharacterRole.ENCOUNTERED, CharacterRole.valueOf("ENCOUNTERED"));
        }

        @Test
        @DisplayName("valueOf throws for invalid name")
        void valueOfThrowsForInvalid() {
            assertThrows(IllegalArgumentException.class, () -> CharacterRole.valueOf("INVALID"));
        }

        @Test
        @DisplayName("valueOf throws for null")
        void valueOfThrowsForNull() {
            assertThrows(NullPointerException.class, () -> CharacterRole.valueOf(null));
        }
    }
}
