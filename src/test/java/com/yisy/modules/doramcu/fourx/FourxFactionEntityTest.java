package com.yisy.modules.doramcu.fourx;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("FourxFactionEntity Tests")
class FourxFactionEntityTest {

    @Nested
    @DisplayName("Constructor")
    class Constructor {

        @Test
        @DisplayName("Parameterized constructor sets fields")
        void parameterized() {
            FourxFactionEntity entity = new FourxFactionEntity("f1", "Federation", "StarTrek4X");
            assertEquals("f1", entity.getFactionId());
            assertEquals("Federation", entity.getName());
            assertEquals("StarTrek4X", entity.getGameModule());
            assertEquals("NEUTRAL", entity.getDefaultStance());
            assertEquals(0, entity.getFleetStrength());
            assertEquals(0, entity.getColonyCount());
            assertEquals(0, entity.getResearchPoints());
            assertEquals(0, entity.getEconomyOutput());
            assertNotNull(entity.getCreatedAt());
            assertNotNull(entity.getUpdatedAt());
        }
    }

    @Nested
    @DisplayName("toDto conversion")
    class ToDto {

        @Test
        @DisplayName("Converts all fields correctly")
        void convertsAllFields() {
            FourxFactionEntity entity = new FourxFactionEntity("f1", "Federation", "StarTrek4X");
            entity.setDescription("A united government");
            entity.setCombatStyle("BALANCED");
            entity.setDefaultStance("ALLIED");
            entity.setFleetStrength(100);
            entity.setColonyCount(5);
            entity.setResearchPoints(200);
            entity.setEconomyOutput(50);

            FourxFaction dto = entity.toDto();
            assertEquals("f1", dto.getId());
            assertEquals("Federation", dto.getName());
            assertEquals("StarTrek4X", dto.getGameModule());
            assertEquals("A united government", dto.getDescription());
            assertEquals("BALANCED", dto.getCombatStyle());
            assertEquals(FourxFaction.DiplomaticStance.ALLIED, dto.getDefaultStance());
            assertEquals(100, dto.getFleetStrength());
            assertEquals(5, dto.getColonyCount());
            assertEquals(200, dto.getResearchPoints());
            assertEquals(50, dto.getEconomyOutput());
        }

        @Test
        @DisplayName("Handles invalid stance gracefully")
        void invalidStance() {
            FourxFactionEntity entity = new FourxFactionEntity("f1", "Federation", "StarTrek4X");
            entity.setDefaultStance("INVALID_STANCE");

            FourxFaction dto = entity.toDto();
            assertEquals(FourxFaction.DiplomaticStance.NEUTRAL, dto.getDefaultStance());
        }

        @Test
        @DisplayName("Handles null stance")
        void nullStance() {
            FourxFactionEntity entity = new FourxFactionEntity("f1", "Federation", "StarTrek4X");
            entity.setDefaultStance(null);

            FourxFaction dto = entity.toDto();
            assertEquals(FourxFaction.DiplomaticStance.NEUTRAL, dto.getDefaultStance());
        }
    }

    @Nested
    @DisplayName("Setters and getters")
    class SettersAndGetters {

        @Test
        @DisplayName("All fields are accessible")
        void allFields() {
            FourxFactionEntity entity = new FourxFactionEntity("f1", "Federation", "StarTrek4X");
            entity.setDescription("Test");
            entity.setCombatStyle("OFFENSIVE");
            entity.setReputationMapJson("{}");
            entity.setControlledSectorsJson("[1,2,3]");
            entity.setFleetStrength(50);
            entity.setColonyCount(3);
            entity.setResearchPoints(100);
            entity.setEconomyOutput(25);

            assertEquals("Test", entity.getDescription());
            assertEquals("OFFENSIVE", entity.getCombatStyle());
            assertEquals("{}", entity.getReputationMapJson());
            assertEquals("[1,2,3]", entity.getControlledSectorsJson());
            assertEquals(50, entity.getFleetStrength());
            assertEquals(3, entity.getColonyCount());
            assertEquals(100, entity.getResearchPoints());
            assertEquals(25, entity.getEconomyOutput());
        }
    }
}
