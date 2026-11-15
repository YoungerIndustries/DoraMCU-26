package com.yisy.modules.doramcu.warhammer40k.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Lore Model")
class LoreTest {

    @Nested
    @DisplayName("Lore Record")
    class LoreRecord {

        @Test
        @DisplayName("creates lore with valid data")
        void createsWithValidData() {
            Lore.Era era = new Lore.Era(
                    "41st Millennium",
                    "M41",
                    "The grimdark future",
                    List.of("Hive Fleet Behemoth", "Black Crusades")
            );

            Lore lore = new Lore(
                    "Warhammer 40,000",
                    "The grimdark future",
                    List.of("Rulebook", "Codex"),
                    Map.of("imperium", Map.of("description", "Human faction")),
                    List.of(era)
            );

            assertEquals("Warhammer 40,000", lore.title());
            assertEquals("The grimdark future", lore.description());
            assertEquals(2, lore.sourceMaterial().size());
            assertEquals(1, lore.factions().size());
            assertEquals(1, lore.eras().size());
        }

        @Test
        @DisplayName("creates lore with empty data")
        void createsWithEmptyData() {
            Lore lore = new Lore(
                    "Title",
                    "Description",
                    List.of(),
                    Map.of(),
                    List.of()
            );

            assertTrue(lore.sourceMaterial().isEmpty());
            assertTrue(lore.factions().isEmpty());
            assertTrue(lore.eras().isEmpty());
        }
    }

    @Nested
    @DisplayName("Era Record")
    class EraRecord {

        @Test
        @DisplayName("creates era with valid data")
        void createsWithValidData() {
            Lore.Era era = new Lore.Era(
                    "Horus Heresy",
                    "M31",
                    "The greatest betrayal",
                    List.of("Fall of Primaris", "Siege of Terra", "Death of Horus")
            );

            assertEquals("Horus Heresy", era.name());
            assertEquals("M31", era.period());
            assertEquals("The greatest betrayal", era.description());
            assertEquals(3, era.keyEvents().size());
        }

        @Test
        @DisplayName("creates era with empty events")
        void createsWithEmptyEvents() {
            Lore.Era era = new Lore.Era(
                    "Era",
                    "Period",
                    "Description",
                    List.of()
            );

            assertTrue(era.keyEvents().isEmpty());
        }
    }
}
