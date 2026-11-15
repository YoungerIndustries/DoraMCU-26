package com.yisy.modules.doramcu.earth2026.model;

import com.yisy.modules.doramcu.earth2026.model.Earth2026BattleEnvironment.WeatherCondition;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("BiosphereContext Tests")
class BiosphereContextTest {

    @Nested
    @DisplayName("Builder")
    class BuilderTests {

        @Test
        @DisplayName("should build with default values")
        void shouldBuildWithDefaults() {
            BiosphereContext ctx = BiosphereContext.builder().build();

            assertEquals("TEMPERATE", ctx.getClimateZone());
            assertEquals(WeatherCondition.CLEAR, ctx.getWeather());
            assertEquals(0.3, ctx.getVegetationDensity(), 0.01);
            assertEquals(1000.0, ctx.getWaterProximity(), 0.01);
            assertFalse(ctx.isNearWater());
            assertEquals(20.0, ctx.getTemperature(), 0.01);
            assertEquals(50.0, ctx.getHumidity(), 0.01);
            assertEquals(5.0, ctx.getWindSpeed(), 0.01);
            assertEquals(0.5, ctx.getTimeOfDay(), 0.01);
            assertFalse(ctx.isNight());
            assertEquals("SUMMER", ctx.getSeason());
            assertEquals(10000.0, ctx.getAtmosphericVisibility(), 0.01);
            assertNotNull(ctx.getGeneratedAt());
        }

        @Test
        @DisplayName("should build with custom values")
        void shouldBuildWithCustomValues() {
            LocalDateTime now = LocalDateTime.of(2026, 11, 12, 10, 0);
            BiosphereContext ctx = BiosphereContext.builder()
                    .climateZone("ARCTIC")
                    .weather(WeatherCondition.SNOW)
                    .vegetationDensity(0.1)
                    .waterProximity(100.0)
                    .temperature(-20.0)
                    .humidity(80.0)
                    .windSpeed(15.0)
                    .timeOfDay(0.1)
                    .season("WINTER")
                    .atmosphericVisibility(500.0)
                    .generatedAt(now)
                    .build();

            assertEquals("ARCTIC", ctx.getClimateZone());
            assertEquals(WeatherCondition.SNOW, ctx.getWeather());
            assertEquals(0.1, ctx.getVegetationDensity(), 0.01);
            assertTrue(ctx.isNearWater());
            assertEquals(-20.0, ctx.getTemperature(), 0.01);
            assertEquals(80.0, ctx.getHumidity(), 0.01);
            assertEquals(15.0, ctx.getWindSpeed(), 0.01);
            assertTrue(ctx.isNight());
            assertEquals("WINTER", ctx.getSeason());
            assertEquals(500.0, ctx.getAtmosphericVisibility(), 0.01);
            assertEquals(now, ctx.getGeneratedAt());
        }

        @Test
        @DisplayName("should clamp vegetation density to 0-1")
        void shouldClampVegetationDensity() {
            BiosphereContext ctx1 = BiosphereContext.builder().vegetationDensity(-0.5).build();
            assertEquals(0.0, ctx1.getVegetationDensity(), 0.01);

            BiosphereContext ctx2 = BiosphereContext.builder().vegetationDensity(1.5).build();
            assertEquals(1.0, ctx2.getVegetationDensity(), 0.01);
        }

        @Test
        @DisplayName("should clamp humidity to 0-100")
        void shouldClampHumidity() {
            BiosphereContext ctx1 = BiosphereContext.builder().humidity(-10.0).build();
            assertEquals(0.0, ctx1.getHumidity(), 0.01);

            BiosphereContext ctx2 = BiosphereContext.builder().humidity(150.0).build();
            assertEquals(100.0, ctx2.getHumidity(), 0.01);
        }

        @Test
        @DisplayName("should set nearWater based on waterProximity")
        void shouldSetNearWaterFromProximity() {
            BiosphereContext ctx1 = BiosphereContext.builder().waterProximity(400.0).build();
            assertTrue(ctx1.isNearWater());

            BiosphereContext ctx2 = BiosphereContext.builder().waterProximity(600.0).build();
            assertFalse(ctx2.isNearWater());
        }

        @Test
        @DisplayName("should set isNight based on timeOfDay")
        void shouldSetIsNightFromTimeOfDay() {
            BiosphereContext ctx1 = BiosphereContext.builder().timeOfDay(0.1).build();
            assertTrue(ctx1.isNight());

            BiosphereContext ctx2 = BiosphereContext.builder().timeOfDay(0.5).build();
            assertFalse(ctx2.isNight());

            BiosphereContext ctx3 = BiosphereContext.builder().timeOfDay(0.9).build();
            assertTrue(ctx3.isNight());
        }
    }

    @Nested
    @DisplayName("Combat Modifiers")
    class CombatModifierTests {

        @Test
        @DisplayName("should calculate zero modifiers for clear weather")
        void shouldCalculateZeroModifiersForClear() {
            BiosphereContext ctx = BiosphereContext.builder()
                    .weather(WeatherCondition.CLEAR)
                    .vegetationDensity(0.2)
                    .isNight(false)
                    .atmosphericVisibility(10000.0)
                    .temperature(20.0)
                    .nearWater(false)
                    .windSpeed(5.0)
                    .build();

            assertEquals(0, ctx.getVisibilityModifier());
            assertEquals(0, ctx.getMovementModifier());
            assertEquals(0, ctx.getDefenseModifier());
            assertEquals(0, ctx.getRangedAttackModifier());
        }

        @Test
        @DisplayName("should reduce visibility for dense vegetation")
        void shouldReduceVisibilityForDenseVegetation() {
            BiosphereContext ctx = BiosphereContext.builder()
                    .weather(WeatherCondition.CLEAR)
                    .vegetationDensity(0.8)
                    .isNight(false)
                    .atmosphericVisibility(10000.0)
                    .build();

            assertTrue(ctx.getVisibilityModifier() < 0);
        }

        @Test
        @DisplayName("should reduce visibility for night")
        void shouldReduceVisibilityForNight() {
            BiosphereContext ctx = BiosphereContext.builder()
                    .weather(WeatherCondition.CLEAR)
                    .vegetationDensity(0.2)
                    .isNight(true)
                    .atmosphericVisibility(10000.0)
                    .build();

            assertTrue(ctx.getVisibilityModifier() < 0);
        }

        @Test
        @DisplayName("should reduce visibility for fog")
        void shouldReduceVisibilityForFog() {
            BiosphereContext ctx = BiosphereContext.builder()
                    .weather(WeatherCondition.FOG)
                    .vegetationDensity(0.2)
                    .isNight(false)
                    .atmosphericVisibility(200.0)
                    .build();

            assertTrue(ctx.getVisibilityModifier() < 0);
        }

        @Test
        @DisplayName("should reduce movement for dense vegetation")
        void shouldReduceMovementForDenseVegetation() {
            BiosphereContext ctx = BiosphereContext.builder()
                    .weather(WeatherCondition.CLEAR)
                    .vegetationDensity(0.8)
                    .temperature(20.0)
                    .nearWater(false)
                    .waterProximity(1000.0)
                    .build();

            assertTrue(ctx.getMovementModifier() < 0);
        }

        @Test
        @DisplayName("should reduce movement for extreme cold")
        void shouldReduceMovementForExtremeCold() {
            BiosphereContext ctx = BiosphereContext.builder()
                    .weather(WeatherCondition.CLEAR)
                    .vegetationDensity(0.1)
                    .temperature(-20.0)
                    .nearWater(false)
                    .waterProximity(1000.0)
                    .build();

            assertTrue(ctx.getMovementModifier() < 0);
        }

        @Test
        @DisplayName("should reduce movement for extreme heat")
        void shouldReduceMovementForExtremeHeat() {
            BiosphereContext ctx = BiosphereContext.builder()
                    .weather(WeatherCondition.CLEAR)
                    .vegetationDensity(0.1)
                    .temperature(45.0)
                    .nearWater(false)
                    .waterProximity(1000.0)
                    .build();

            assertTrue(ctx.getMovementModifier() < 0);
        }

        @Test
        @DisplayName("should increase defense for vegetation and night")
        void shouldIncreaseDefenseForVegetationAndNight() {
            BiosphereContext ctx = BiosphereContext.builder()
                    .vegetationDensity(0.6)
                    .isNight(true)
                    .build();

            assertTrue(ctx.getDefenseModifier() > 0);
        }

        @Test
        @DisplayName("should reduce ranged attack for storm")
        void shouldReduceRangedAttackForStorm() {
            BiosphereContext ctx = BiosphereContext.builder()
                    .weather(WeatherCondition.STORM)
                    .vegetationDensity(0.2)
                    .windSpeed(5.0)
                    .isNight(false)
                    .build();

            assertTrue(ctx.getRangedAttackModifier() < 0);
        }

        @Test
        @DisplayName("should reduce ranged attack for high wind")
        void shouldReduceRangedAttackForHighWind() {
            BiosphereContext ctx = BiosphereContext.builder()
                    .weather(WeatherCondition.CLEAR)
                    .vegetationDensity(0.2)
                    .windSpeed(20.0)
                    .isNight(false)
                    .build();

            assertTrue(ctx.getRangedAttackModifier() < 0);
        }

        @Test
        @DisplayName("should clamp modifiers to -5 to +5")
        void shouldClampModifiers() {
            BiosphereContext ctx = BiosphereContext.builder()
                    .weather(WeatherCondition.SANDSTORM)
                    .vegetationDensity(0.9)
                    .isNight(true)
                    .atmosphericVisibility(50.0)
                    .temperature(-20.0)
                    .nearWater(true)
                    .waterProximity(10.0)
                    .windSpeed(25.0)
                    .build();

            assertTrue(ctx.getVisibilityModifier() >= -5 && ctx.getVisibilityModifier() <= 5);
            assertTrue(ctx.getMovementModifier() >= -5 && ctx.getMovementModifier() <= 5);
            assertTrue(ctx.getDefenseModifier() >= -5 && ctx.getDefenseModifier() <= 5);
            assertTrue(ctx.getRangedAttackModifier() >= -5 && ctx.getRangedAttackModifier() <= 5);
        }
    }

    @Nested
    @DisplayName("getCombatModifiers")
    class GetCombatModifiersTests {

        @Test
        @DisplayName("should return all four modifiers")
        void shouldReturnAllModifiers() {
            BiosphereContext ctx = BiosphereContext.builder().build();
            Map<String, Integer> modifiers = ctx.getCombatModifiers();

            assertEquals(4, modifiers.size());
            assertTrue(modifiers.containsKey("visibility"));
            assertTrue(modifiers.containsKey("movement"));
            assertTrue(modifiers.containsKey("defense"));
            assertTrue(modifiers.containsKey("rangedAttack"));
        }
    }

    @Nested
    @DisplayName("getDescription")
    class GetDescriptionTests {

        @Test
        @DisplayName("should include climate zone and temperature")
        void shouldIncludeClimateAndTemp() {
            BiosphereContext ctx = BiosphereContext.builder()
                    .climateZone("ARCTIC")
                    .temperature(-15.0)
                    .season("WINTER")
                    .build();

            String desc = ctx.getDescription();
            assertTrue(desc.contains("ARCTIC"));
            assertTrue(desc.contains("-15.0"));
            assertTrue(desc.contains("WINTER"));
        }

        @Test
        @DisplayName("should include night indicator")
        void shouldIncludeNightIndicator() {
            BiosphereContext ctx = BiosphereContext.builder()
                    .isNight(true)
                    .temperature(20.0)
                    .season("SUMMER")
                    .build();

            assertTrue(ctx.getDescription().contains("night"));
        }

        @Test
        @DisplayName("should include dense vegetation indicator")
        void shouldIncludeDenseVegetation() {
            BiosphereContext ctx = BiosphereContext.builder()
                    .vegetationDensity(0.8)
                    .temperature(20.0)
                    .season("SUMMER")
                    .build();

            assertTrue(ctx.getDescription().contains("dense vegetation"));
        }

        @Test
        @DisplayName("should include near water indicator")
        void shouldIncludeNearWater() {
            BiosphereContext ctx = BiosphereContext.builder()
                    .nearWater(true)
                    .temperature(20.0)
                    .season("SUMMER")
                    .build();

            assertTrue(ctx.getDescription().contains("near water"));
        }
    }
}
