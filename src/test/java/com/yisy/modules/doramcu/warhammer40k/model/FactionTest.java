package com.yisy.modules.doramcu.warhammer40k.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Faction Model")
class FactionTest {

    @Nested
    @DisplayName("Constructor Validation")
    class ConstructorValidation {

        @Test
        @DisplayName("creates faction with valid data")
        void createsWithValidData() {
            Faction faction = new Faction(
                    "imperium",
                    "Imperium of Man",
                    "41st Millennium",
                    "Theocratic autocracy",
                    "Largest human faction",
                    List.of("adeptus_astartes", "imperial_guard"),
                    List.of("The God-Emperor", "Guilliman"),
                    List.of("Vast resources"),
                    List.of("Bureaucracy"),
                    "Terra",
                    List.of(),
                    List.of("Chaos", "Orks")
            );

            assertEquals("imperium", faction.id());
            assertEquals("Imperium of Man", faction.name());
            assertEquals("41st Millennium", faction.era());
            assertEquals("Terra", faction.homeWorld());
            assertEquals(2, faction.militaryBranches().size());
            assertEquals(2, faction.enemies().size());
        }

        @Test
        @DisplayName("throws when id is null")
        void throwsWhenIdNull() {
            assertThrows(IllegalArgumentException.class, () ->
                    new Faction(null, "Name", "era", "ideology", "desc",
                            List.of(), List.of(), List.of(), List.of(),
                            "Home", List.of(), List.of())
            );
        }

        @Test
        @DisplayName("throws when id is blank")
        void throwsWhenIdBlank() {
            assertThrows(IllegalArgumentException.class, () ->
                    new Faction("  ", "Name", "era", "ideology", "desc",
                            List.of(), List.of(), List.of(), List.of(),
                            "Home", List.of(), List.of())
            );
        }

        @Test
        @DisplayName("throws when name is null")
        void throwsWhenNameNull() {
            assertThrows(IllegalArgumentException.class, () ->
                    new Faction("id", null, "era", "ideology", "desc",
                            List.of(), List.of(), List.of(), List.of(),
                            "Home", List.of(), List.of())
            );
        }

        @Test
        @DisplayName("throws when name is blank")
        void throwsWhenNameBlank() {
            assertThrows(IllegalArgumentException.class, () ->
                    new Faction("id", "  ", "era", "ideology", "desc",
                            List.of(), List.of(), List.of(), List.of(),
                            "Home", List.of(), List.of())
            );
        }

        @Test
        @DisplayName("creates faction with empty lists")
        void createsWithEmptyLists() {
            Faction faction = new Faction(
                    "id", "Name", "era", "ideology", "desc",
                    List.of(), List.of(), List.of(), List.of(),
                    "Home", List.of(), List.of()
            );

            assertTrue(faction.militaryBranches().isEmpty());
            assertTrue(faction.keyLeaders().isEmpty());
            assertTrue(faction.allies().isEmpty());
        }
    }
}
