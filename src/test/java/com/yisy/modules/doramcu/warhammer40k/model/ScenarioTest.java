package com.yisy.modules.doramcu.warhammer40k.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Scenario Model")
class ScenarioTest {

    @Nested
    @DisplayName("Constructor Validation")
    class ConstructorValidation {

        @Test
        @DisplayName("creates scenario with valid data")
        void createsWithValidData() {
            Scenario scenario = new Scenario(
                    "tactical_victory",
                    "Tactical Victory",
                    "Standard battle scenario",
                    List.of("imperium", "chaos"),
                    5,
                    List.of("Hold the objective", "Destroy enemy forces"),
                    List.of("Open ground", "Ruins"),
                    Map.of("imperium", List.of("space_marine"), "chaos", List.of("chaos_marine"))
            );

            assertEquals("tactical_victory", scenario.id());
            assertEquals("Tactical Victory", scenario.name());
            assertEquals(5, scenario.difficulty());
            assertEquals(2, scenario.factions().size());
            assertEquals(2, scenario.objectives().size());
            assertEquals(2, scenario.units().size());
        }

        @Test
        @DisplayName("throws when id is null")
        void throwsWhenIdNull() {
            assertThrows(IllegalArgumentException.class, () ->
                    new Scenario(null, "Name", "desc",
                            List.of(), 1, List.of(), List.of(), Map.of())
            );
        }

        @Test
        @DisplayName("throws when id is blank")
        void throwsWhenIdBlank() {
            assertThrows(IllegalArgumentException.class, () ->
                    new Scenario("  ", "Name", "desc",
                            List.of(), 1, List.of(), List.of(), Map.of())
            );
        }

        @Test
        @DisplayName("throws when name is null")
        void throwsWhenNameNull() {
            assertThrows(IllegalArgumentException.class, () ->
                    new Scenario("id", null, "desc",
                            List.of(), 1, List.of(), List.of(), Map.of())
            );
        }

        @Test
        @DisplayName("throws when name is blank")
        void throwsWhenNameBlank() {
            assertThrows(IllegalArgumentException.class, () ->
                    new Scenario("id", "  ", "desc",
                            List.of(), 1, List.of(), List.of(), Map.of())
            );
        }

        @Test
        @DisplayName("creates scenario with empty data")
        void createsWithEmptyData() {
            Scenario scenario = new Scenario(
                    "id", "Name", "desc",
                    List.of(), 1, List.of(), List.of(), Map.of()
            );

            assertTrue(scenario.factions().isEmpty());
            assertTrue(scenario.objectives().isEmpty());
            assertTrue(scenario.terrain().isEmpty());
            assertTrue(scenario.units().isEmpty());
        }
    }
}
