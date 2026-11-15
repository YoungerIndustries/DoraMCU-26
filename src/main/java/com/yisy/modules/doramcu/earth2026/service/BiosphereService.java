package com.yisy.modules.doramcu.earth2026.service;

import com.yisy.modules.doramcu.earth2026.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;

/**
 * Service that builds BiosphereContext from Earth2026Planet, Earth2026Location,
 * and time period data.
 * <p>
 * Reads climate zones, weather patterns, vegetation, water bodies, and atmospheric
 * conditions to create a complete biosphere context for combat resolution.
 * </p>
 */
@Service
public class BiosphereService {

    private static final Logger log = LoggerFactory.getLogger(BiosphereService.class);

    private final Random random = new Random();

    /**
     * Builds a BiosphereContext for a given location and time period.
     *
     * @param location the Earth2026Location
     * @param timePeriod the time period template name
     * @param weather the weather condition
     * @return a complete biosphere context
     */
    public BiosphereContext buildBiosphereContext(Earth2026Location location, String timePeriod,
                                                   Earth2026BattleEnvironment.WeatherCondition weather) {
        if (location == null) {
            return createDefaultBiosphereContext(weather);
        }

        // Determine climate zone from location
        String climateZone = determineClimateZone(location);

        // Get vegetation density from location
        double vegetationDensity = location.getVegetationDensity() != null ?
                location.getVegetationDensity() : determineVegetationDensity(climateZone);

        // Determine temperature based on climate zone and season
        String season = determineSeason(timePeriod);
        double temperature = determineTemperature(climateZone, season);

        // Determine humidity based on climate zone
        double humidity = determineHumidity(climateZone);

        // Determine wind speed based on weather
        double windSpeed = determineWindSpeed(weather);

        // Determine time of day (use current time)
        LocalDateTime now = LocalDateTime.now();
        double timeOfDay = now.getHour() / 24.0 + now.getMinute() / 1440.0;
        boolean isNight = timeOfDay < 0.25 || timeOfDay > 0.75;

        // Determine atmospheric visibility based on weather
        double atmosphericVisibility = determineAtmosphericVisibility(weather);

        // Determine water proximity (simplified - based on location type)
        double waterProximity = determineWaterProximity(location);

        BiosphereContext context = BiosphereContext.builder()
                .climateZone(climateZone)
                .weather(weather != null ? weather : Earth2026BattleEnvironment.WeatherCondition.CLEAR)
                .vegetationDensity(vegetationDensity)
                .waterProximity(waterProximity)
                .temperature(temperature)
                .humidity(humidity)
                .windSpeed(windSpeed)
                .timeOfDay(timeOfDay)
                .isNight(isNight)
                .season(season)
                .atmosphericVisibility(atmosphericVisibility)
                .generatedAt(now)
                .build();

        log.debug("Built biosphere context for location {}: {}", location.getName(), context);
        return context;
    }

    /**
     * Creates a default biosphere context when location data is unavailable.
     */
    public BiosphereContext createDefaultBiosphereContext(
            Earth2026BattleEnvironment.WeatherCondition weather) {
        LocalDateTime now = LocalDateTime.now();
        double timeOfDay = now.getHour() / 24.0 + now.getMinute() / 1440.0;
        boolean isNight = timeOfDay < 0.25 || timeOfDay > 0.75;

        return BiosphereContext.builder()
                .climateZone("TEMPERATE")
                .weather(weather != null ? weather : Earth2026BattleEnvironment.WeatherCondition.CLEAR)
                .vegetationDensity(0.3)
                .waterProximity(1000.0)
                .temperature(20.0)
                .humidity(50.0)
                .windSpeed(5.0)
                .timeOfDay(timeOfDay)
                .isNight(isNight)
                .season("SUMMER")
                .atmosphericVisibility(10000.0)
                .generatedAt(now)
                .build();
    }

    // ---- Climate Zone Determination ----

    String determineClimateZone(Earth2026Location location) {
        // Use latitude to determine climate zone
        Double lat = location.getLatitude();
        if (lat == null) {
            return "TEMPERATE";
        }

        double absLat = Math.abs(lat);
        if (absLat < 23.5) {
            return "TROPICAL";
        } else if (absLat < 35) {
            return "ARID";
        } else if (absLat < 55) {
            return "TEMPERATE";
        } else if (absLat < 66.5) {
            return "CONTINENTAL";
        } else {
            return "ARCTIC";
        }
    }

    // ---- Vegetation Density Determination ----

    private double determineVegetationDensity(String climateZone) {
        switch (climateZone != null ? climateZone.toUpperCase() : "TEMPERATE") {
            case "TROPICAL":
                return 0.8;
            case "ARID":
                return 0.1;
            case "TEMPERATE":
                return 0.4;
            case "CONTINENTAL":
                return 0.3;
            case "ARCTIC":
                return 0.05;
            default:
                return 0.3;
        }
    }

    // ---- Season Determination ----

    private String determineSeason(String timePeriod) {
        // Use current month to determine season
        int month = LocalDateTime.now().getMonthValue();
        if (month >= 3 && month <= 5) {
            return "SPRING";
        } else if (month >= 6 && month <= 8) {
            return "SUMMER";
        } else if (month >= 9 && month <= 11) {
            return "AUTUMN";
        } else {
            return "WINTER";
        }
    }

    // ---- Temperature Determination ----

    private double determineTemperature(String climateZone, String season) {
        double baseTemp;
        switch (climateZone != null ? climateZone.toUpperCase() : "TEMPERATE") {
            case "TROPICAL":
                baseTemp = 28.0;
                break;
            case "ARID":
                baseTemp = 35.0;
                break;
            case "TEMPERATE":
                baseTemp = 15.0;
                break;
            case "CONTINENTAL":
                baseTemp = 5.0;
                break;
            case "ARCTIC":
                baseTemp = -15.0;
                break;
            default:
                baseTemp = 15.0;
                break;
        }

        // Season adjustment
        double seasonAdjustment;
        switch (season != null ? season.toUpperCase() : "SUMMER") {
            case "SPRING":
                seasonAdjustment = 5.0;
                break;
            case "SUMMER":
                seasonAdjustment = 10.0;
                break;
            case "AUTUMN":
                seasonAdjustment = -5.0;
                break;
            case "WINTER":
                seasonAdjustment = -15.0;
                break;
            default:
                seasonAdjustment = 0.0;
                break;
        }

        // Add some randomness
        return baseTemp + seasonAdjustment + (random.nextDouble() - 0.5) * 5.0;
    }

    // ---- Humidity Determination ----

    private double determineHumidity(String climateZone) {
        switch (climateZone != null ? climateZone.toUpperCase() : "TEMPERATE") {
            case "TROPICAL":
                return 80.0;
            case "ARID":
                return 15.0;
            case "TEMPERATE":
                return 55.0;
            case "CONTINENTAL":
                return 45.0;
            case "ARCTIC":
                return 70.0;
            default:
                return 50.0;
        }
    }

    // ---- Wind Speed Determination ----

    private double determineWindSpeed(Earth2026BattleEnvironment.WeatherCondition weather) {
        if (weather == null) return 5.0;

        switch (weather) {
            case CLEAR:
                return 3.0 + random.nextDouble() * 4.0;
            case CLOUDY:
                return 4.0 + random.nextDouble() * 5.0;
            case RAIN:
                return 6.0 + random.nextDouble() * 8.0;
            case STORM:
                return 15.0 + random.nextDouble() * 15.0;
            case FOG:
                return 1.0 + random.nextDouble() * 3.0;
            case SANDSTORM:
                return 12.0 + random.nextDouble() * 10.0;
            default:
                return 5.0;
        }
    }

    // ---- Atmospheric Visibility Determination ----

    private double determineAtmosphericVisibility(
            Earth2026BattleEnvironment.WeatherCondition weather) {
        if (weather == null) return 10000.0;

        switch (weather) {
            case CLEAR:
                return 15000.0;
            case CLOUDY:
                return 8000.0;
            case RAIN:
                return 3000.0;
            case STORM:
                return 1000.0;
            case FOG:
                return 200.0;
            case SNOW:
                return 1500.0;
            case SANDSTORM:
                return 100.0;
            case NIGHT:
                return 500.0; // Night reduces effective visibility
            case TWILIGHT:
                return 2000.0;
            default:
                return 10000.0;
        }
    }

    // ---- Water Proximity Determination ----

    private double determineWaterProximity(Earth2026Location location) {
        // Check location type for water indicators
        String type = location.getType();
        if (type != null) {
            switch (type.toLowerCase()) {
                case "port":
                case "harbor":
                case "dock":
                    return 50.0; // Very close to water
                case "coastal":
                    return 200.0;
                case "island":
                    return 100.0;
            }
        }

        // Check city name for water indicators
        String city = location.getCity();
        if (city != null && (city.toLowerCase().contains("port") ||
                city.toLowerCase().contains("harbor") ||
                city.toLowerCase().contains("beach"))) {
            return 300.0;
        }

        // Default: inland location
        return 1000.0 + random.nextDouble() * 5000.0;
    }
}
