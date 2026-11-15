package com.yisy.modules.doramcu.earth2026.service;

import com.yisy.modules.doramcu.earth2026.model.BiosphereContext;
import com.yisy.modules.doramcu.earth2026.model.Earth2026BattleEnvironment;
import com.yisy.modules.doramcu.earth2026.model.Earth2026BattleEnvironment.WeatherCondition;
import com.yisy.modules.doramcu.earth2026.model.Earth2026Location;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("BiosphereService Tests")
class BiosphereServiceTest {

    private final BiosphereService service = new BiosphereService();

    private Earth2026Location createLocation(Double latitude, String type, String city) {
        Earth2026Location loc = new Earth2026Location();
        loc.setId(1L);
        loc.setName("Test Location");
        loc.setLatitude(latitude);
        loc.setLongitude(10.0);
        loc.setType(type);
        loc.setCity(city);
        return loc;
    }

    @Nested
    @DisplayName("buildBiosphereContext")
    class BuildBiosphereContextTests {

        @Test
        @DisplayName("should return default context for null location")
        void shouldReturnDefaultForNullLocation() {
            BiosphereContext ctx = service.buildBiosphereContext(null, "World War II", WeatherCondition.CLEAR);

            assertNotNull(ctx);
            assertEquals("TEMPERATE", ctx.getClimateZone());
            assertEquals(WeatherCondition.CLEAR, ctx.getWeather());
        }

        @Test
        @DisplayName("should determine TROPICAL climate for low latitude")
        void shouldDetermineTropicalClimate() {
            Earth2026Location loc = createLocation(10.0, "city", "Miami");
            BiosphereContext ctx = service.buildBiosphereContext(loc, "World War II", WeatherCondition.CLEAR);

            assertEquals("TROPICAL", ctx.getClimateZone());
            assertTrue(ctx.getVegetationDensity() > 0.5);
        }

        @Test
        @DisplayName("should determine ARID climate for mid latitude")
        void shouldDetermineAridClimate() {
            Earth2026Location loc = createLocation(30.0, "city", "Phoenix");
            BiosphereContext ctx = service.buildBiosphereContext(loc, "World War II", WeatherCondition.CLEAR);

            assertEquals("ARID", ctx.getClimateZone());
            assertTrue(ctx.getVegetationDensity() < 0.3);
        }

        @Test
        @DisplayName("should determine TEMPERATE climate for moderate latitude")
        void shouldDetermineTemperateClimate() {
            Earth2026Location loc = createLocation(50.0, "city", "Berlin");
            BiosphereContext ctx = service.buildBiosphereContext(loc, "World War II", WeatherCondition.CLEAR);

            assertEquals("TEMPERATE", ctx.getClimateZone());
        }

        @Test
        @DisplayName("should determine ARCTIC climate for high latitude")
        void shouldDetermineArcticClimate() {
            Earth2026Location loc = createLocation(70.0, "city", "Tromso");
            BiosphereContext ctx = service.buildBiosphereContext(loc, "World War II", WeatherCondition.CLEAR);

            assertEquals("ARCTIC", ctx.getClimateZone());
            assertTrue(ctx.getVegetationDensity() < 0.1);
        }

        @Test
        @DisplayName("should use location vegetation density if set")
        void shouldUseLocationVegetationDensity() {
            Earth2026Location loc = createLocation(50.0, "city", "Berlin");
            loc.setVegetationDensity(0.7);
            BiosphereContext ctx = service.buildBiosphereContext(loc, "World War II", WeatherCondition.CLEAR);

            assertEquals(0.7, ctx.getVegetationDensity(), 0.01);
        }

        @Test
        @DisplayName("should determine water proximity for port location")
        void shouldDetermineWaterProximityForPort() {
            Earth2026Location loc = createLocation(50.0, "port", "Hamburg");
            BiosphereContext ctx = service.buildBiosphereContext(loc, "World War II", WeatherCondition.CLEAR);

            assertTrue(ctx.isNearWater());
            assertTrue(ctx.getWaterProximity() < 200.0);
        }

        @Test
        @DisplayName("should determine water proximity for coastal location")
        void shouldDetermineWaterProximityForCoastal() {
            Earth2026Location loc = createLocation(50.0, "coastal", "Amsterdam");
            BiosphereContext ctx = service.buildBiosphereContext(loc, "World War II", WeatherCondition.CLEAR);

            assertTrue(ctx.isNearWater());
        }

        @Test
        @DisplayName("should set weather from parameter")
        void shouldSetWeatherFromParameter() {
            Earth2026Location loc = createLocation(50.0, "city", "Berlin");
            BiosphereContext ctx = service.buildBiosphereContext(loc, "World War II", WeatherCondition.RAIN);

            assertEquals(WeatherCondition.RAIN, ctx.getWeather());
        }

        @Test
        @DisplayName("should default to CLEAR weather when null")
        void shouldDefaultToClearWeather() {
            Earth2026Location loc = createLocation(50.0, "city", "Berlin");
            BiosphereContext ctx = service.buildBiosphereContext(loc, "World War II", null);

            assertEquals(WeatherCondition.CLEAR, ctx.getWeather());
        }

        @Test
        @DisplayName("should determine temperature based on climate and season")
        void shouldDetermineTemperature() {
            // Arctic climate should have low temperature
            Earth2026Location loc = createLocation(70.0, "city", "Tromso");
            BiosphereContext ctx = service.buildBiosphereContext(loc, "World War II", WeatherCondition.CLEAR);

            assertTrue(ctx.getTemperature() < 10.0);
        }

        @Test
        @DisplayName("should determine humidity based on climate zone")
        void shouldDetermineHumidity() {
            // Tropical should have high humidity
            Earth2026Location loc = createLocation(10.0, "city", "Miami");
            BiosphereContext ctx = service.buildBiosphereContext(loc, "World War II", WeatherCondition.CLEAR);

            assertTrue(ctx.getHumidity() > 70.0);

            // Arid should have low humidity
            Earth2026Location loc2 = createLocation(30.0, "city", "Phoenix");
            BiosphereContext ctx2 = service.buildBiosphereContext(loc2, "World War II", WeatherCondition.CLEAR);

            assertTrue(ctx2.getHumidity() < 30.0);
        }

        @Test
        @DisplayName("should determine wind speed based on weather")
        void shouldDetermineWindSpeed() {
            Earth2026Location loc = createLocation(50.0, "city", "Berlin");
            BiosphereContext ctx = service.buildBiosphereContext(loc, "World War II", WeatherCondition.STORM);

            assertTrue(ctx.getWindSpeed() > 10.0);
        }

        @Test
        @DisplayName("should determine atmospheric visibility based on weather")
        void shouldDetermineVisibility() {
            Earth2026Location loc = createLocation(50.0, "city", "Berlin");

            BiosphereContext ctxClear = service.buildBiosphereContext(loc, "World War II", WeatherCondition.CLEAR);
            BiosphereContext ctxFog = service.buildBiosphereContext(loc, "World War II", WeatherCondition.FOG);

            assertTrue(ctxClear.getAtmosphericVisibility() > ctxFog.getAtmosphericVisibility());
            assertTrue(ctxFog.getAtmosphericVisibility() < 500.0);
        }
    }

    @Nested
    @DisplayName("createDefaultBiosphereContext")
    class CreateDefaultBiosphereContextTests {

        @Test
        @DisplayName("should create default context with TEMPERATE climate")
        void shouldCreateDefaultContext() {
            BiosphereContext ctx = service.createDefaultBiosphereContext(WeatherCondition.CLEAR);

            assertEquals("TEMPERATE", ctx.getClimateZone());
            assertEquals(WeatherCondition.CLEAR, ctx.getWeather());
            assertEquals(0.3, ctx.getVegetationDensity(), 0.01);
            assertEquals(20.0, ctx.getTemperature(), 0.01);
            assertEquals(50.0, ctx.getHumidity(), 0.01);
        }

        @Test
        @DisplayName("should use provided weather condition")
        void shouldUseProvidedWeather() {
            BiosphereContext ctx = service.createDefaultBiosphereContext(WeatherCondition.RAIN);

            assertEquals(WeatherCondition.RAIN, ctx.getWeather());
        }

        @Test
        @DisplayName("should default to CLEAR when weather is null")
        void shouldDefaultToClear() {
            BiosphereContext ctx = service.createDefaultBiosphereContext(null);

            assertEquals(WeatherCondition.CLEAR, ctx.getWeather());
        }
    }
}
