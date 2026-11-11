package com.yisy.modules.doramcu.thirdreich.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("TerrainType")
class TerrainTypeTest {

    @Test
    @DisplayName("all terrain types have valid display names")
    void allHaveDisplayNames() {
        for (TerrainType type : TerrainType.values()) {
            assertNotNull(type.getDisplayName());
            assertFalse(type.getDisplayName().isBlank());
        }
    }

    @Test
    @DisplayName("all terrain types have descriptions")
    void allHaveDescriptions() {
        for (TerrainType type : TerrainType.values()) {
            assertNotNull(type.getDescription());
        }
    }

    @Nested
    @DisplayName("Defense Bonuses")
    class DefenseBonuses {

        @Test
        @DisplayName("plains has no defense bonus")
        void plainsNoBonus() {
            assertEquals(0, TerrainType.PLAINS.getDefenseBonus());
        }

        @Test
        @DisplayName("mountain has high defense bonus")
        void mountainHighBonus() {
            assertEquals(2, TerrainType.MOUNTAIN.getDefenseBonus());
        }

        @Test
        @DisplayName("urban has high defense bonus")
        void urbanHighBonus() {
            assertEquals(2, TerrainType.URBAN.getDefenseBonus());
        }

        @Test
        @DisplayName("swamp has high defense bonus")
        void swampHighBonus() {
            assertEquals(2, TerrainType.SWAMP.getDefenseBonus());
        }

        @Test
        @DisplayName("jungle has moderate defense bonus")
        void jungleModerateBonus() {
            assertEquals(1, TerrainType.JUNGLE.getDefenseBonus());
        }

        @Test
        @DisplayName("desert has no defense bonus")
        void desertNoBonus() {
            assertEquals(0, TerrainType.DESERT.getDefenseBonus());
        }
    }

    @Nested
    @DisplayName("Movement Penalties")
    class MovementPenalties {

        @Test
        @DisplayName("plains has no movement penalty")
        void plainsNoPenalty() {
            assertEquals(0, TerrainType.PLAINS.getMovementPenalty());
        }

        @Test
        @DisplayName("swamp has severe movement penalty")
        void swampSeverePenalty() {
            assertEquals(-2, TerrainType.SWAMP.getMovementPenalty());
        }

        @Test
        @DisplayName("mountain has movement penalty")
        void mountainPenalty() {
            assertEquals(-1, TerrainType.MOUNTAIN.getMovementPenalty());
        }

        @Test
        @DisplayName("forest has no movement penalty")
        void forestNoPenalty() {
            assertEquals(0, TerrainType.FOREST.getMovementPenalty());
        }
    }

    @Test
    @DisplayName("toString returns display name")
    void toStringReturnsDisplayName() {
        assertEquals("Plains", TerrainType.PLAINS.toString());
        assertEquals("Mountain", TerrainType.MOUNTAIN.toString());
    }

    @Test
    @DisplayName("enum has 8 terrain types")
    void hasEightTypes() {
        assertEquals(8, TerrainType.values().length);
    }
}
