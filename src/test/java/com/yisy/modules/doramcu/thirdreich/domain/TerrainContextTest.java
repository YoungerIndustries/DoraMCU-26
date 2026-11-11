package com.yisy.modules.doramcu.thirdreich.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("TerrainContext")
class TerrainContextTest {

    @Nested
    @DisplayName("Constructor")
    class Constructor {

        @Test
        @DisplayName("creates with terrain type and default modifiers")
        void createsWithDefaults() {
            TerrainContext context = new TerrainContext(TerrainType.PLAINS);

            assertEquals(TerrainType.PLAINS, context.getTerrainType());
            assertNull(context.getPlanetName());
            assertEquals(0, context.getWeatherModifier());
            assertEquals(0, context.getVisibilityModifier());
        }

        @Test
        @DisplayName("creates with planet name")
        void createsWithPlanet() {
            TerrainContext context = new TerrainContext(TerrainType.URBAN, "Terra");

            assertEquals(TerrainType.URBAN, context.getTerrainType());
            assertEquals("Terra", context.getPlanetName());
        }

        @Test
        @DisplayName("throws on null terrain type")
        void throwsOnNullTerrain() {
            assertThrows(IllegalArgumentException.class, () ->
                    new TerrainContext(null));
        }
    }

    @Nested
    @DisplayName("Modifiers")
    class Modifiers {

        @Test
        @DisplayName("weather modifier clamps to -2 to +2")
        void weatherClamps() {
            TerrainContext context = new TerrainContext(TerrainType.PLAINS);
            context.setWeatherModifier(-5);
            assertEquals(-2, context.getWeatherModifier());
            context.setWeatherModifier(5);
            assertEquals(2, context.getWeatherModifier());
        }

        @Test
        @DisplayName("visibility modifier clamps to -2 to +2")
        void visibilityClamps() {
            TerrainContext context = new TerrainContext(TerrainType.PLAINS);
            context.setVisibilityModifier(-5);
            assertEquals(-2, context.getVisibilityModifier());
            context.setVisibilityModifier(5);
            assertEquals(2, context.getVisibilityModifier());
        }
    }

    @Nested
    @DisplayName("Calculations")
    class Calculations {

        @Test
        @DisplayName("total defense bonus combines terrain and modifiers")
        void totalDefenseBonus() {
            TerrainContext context = new TerrainContext(TerrainType.MOUNTAIN);
            // Mountain defense bonus = 2
            assertEquals(2, context.getTotalDefenseBonus());

            context.setWeatherModifier(1);
            context.setVisibilityModifier(-1);
            // 2 + 1 + (-1) = 2
            assertEquals(2, context.getTotalDefenseBonus());
        }

        @Test
        @DisplayName("plains with no modifiers has 0 defense bonus")
        void plainsZeroBonus() {
            TerrainContext context = new TerrainContext(TerrainType.PLAINS);
            assertEquals(0, context.getTotalDefenseBonus());
        }

        @Test
        @DisplayName("movement penalty combines terrain and weather")
        void movementPenalty() {
            TerrainContext context = new TerrainContext(TerrainType.SWAMP);
            // Swamp movement penalty = -2, weather = 0
            assertEquals(-2, context.getMovementPenalty());

            context.setWeatherModifier(1);
            // -2 + abs(1) = -1
            assertEquals(-1, context.getMovementPenalty());
        }

        @Test
        @DisplayName("urban terrain provides defense bonus")
        void urbanDefenseBonus() {
            TerrainContext context = new TerrainContext(TerrainType.URBAN);
            assertEquals(2, context.getTotalDefenseBonus());
        }
    }

    @Nested
    @DisplayName("Setters")
    class Setters {

        @Test
        @DisplayName("setPlanetName works")
        void setPlanetName() {
            TerrainContext context = new TerrainContext(TerrainType.PLAINS);
            context.setPlanetName("Vulcan");
            assertEquals("Vulcan", context.getPlanetName());
        }
    }

    @Nested
    @DisplayName("ToString")
    class ToString {

        @Test
        @DisplayName("toString contains terrain type")
        void containsTerrain() {
            TerrainContext context = new TerrainContext(TerrainType.MOUNTAIN);
            String str = context.toString();
            assertTrue(str.contains("Mountain"));
        }

        @Test
        @DisplayName("toString contains planet name when set")
        void containsPlanet() {
            TerrainContext context = new TerrainContext(TerrainType.PLAINS, "Terra");
            String str = context.toString();
            assertTrue(str.contains("Terra"));
        }
    }
}
