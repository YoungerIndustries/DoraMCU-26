package com.yisy.modules.doramcu.earth2026.model;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Aggregated biosphere context for a battlefield location.
 * <p>
 * Combines the full planet data (climate zones, weather patterns, day/night cycles,
 * vegetation, water bodies) into a single context object that affects combat
 * resolution. This connects the Earth2026Planet model to the Third Reich combat engine.
 * </p>
 * <p>
 * Biosphere effects include:
 * <ul>
 *   <li>Visibility modifiers from vegetation density and weather</li>
 *   <li>Movement penalties from terrain type and water proximity</li>
 *   <li>Weather effects from climate zone and time of day</li>
 *   <li>Atmospheric conditions affecting ranged combat</li>
 * </ul>
 * </p>
 */
public class BiosphereContext {

    /** Climate zone (e.g., "TROPICAL", "TEMPERATE", "ARID", "ARCTIC"). */
    private final String climateZone;

    /** Current weather condition. */
    private final Earth2026BattleEnvironment.WeatherCondition weather;

    /** Vegetation density (0.0 = bare ground, 1.0 = dense jungle). */
    private final double vegetationDensity;

    /** Distance to nearest water body in meters. */
    private final double waterProximity;

    /** Whether the location is near a significant water body. */
    private final boolean nearWater;

    /** Temperature in Celsius. */
    private final double temperature;

    /** Humidity percentage (0-100). */
    private final double humidity;

    /** Wind speed in m/s. */
    private final double windSpeed;

    /** Time of day factor (0.0 = midnight, 0.5 = noon, 1.0 = midnight). */
    private final double timeOfDay;

    /** Whether it's currently night. */
    private final boolean isNight;

    /** Season (e.g., "SPRING", "SUMMER", "AUTUMN", "WINTER"). */
    private final String season;

    /** Atmospheric visibility in meters. */
    private final double atmosphericVisibility;

    /** When this context was generated. */
    private final LocalDateTime generatedAt;

    // ---- Combat Modifiers ----

    /** Visibility modifier for combat (-5 to +5). */
    private final int visibilityModifier;

    /** Movement modifier for combat (-5 to +5). */
    private final int movementModifier;

    /** Defense modifier for combat (-5 to +5). */
    private final int defenseModifier;

    /** Ranged attack modifier (-5 to +5). */
    private final int rangedAttackModifier;

    private BiosphereContext(Builder builder) {
        this.climateZone = builder.climateZone;
        this.weather = builder.weather;
        this.vegetationDensity = builder.vegetationDensity;
        this.waterProximity = builder.waterProximity;
        this.nearWater = builder.nearWater;
        this.temperature = builder.temperature;
        this.humidity = builder.humidity;
        this.windSpeed = builder.windSpeed;
        this.timeOfDay = builder.timeOfDay;
        this.isNight = builder.isNight;
        this.season = builder.season;
        this.atmosphericVisibility = builder.atmosphericVisibility;
        this.generatedAt = builder.generatedAt != null ? builder.generatedAt : LocalDateTime.now();

        // Calculate combat modifiers from biosphere data
        this.visibilityModifier = calculateVisibilityModifier();
        this.movementModifier = calculateMovementModifier();
        this.defenseModifier = calculateDefenseModifier();
        this.rangedAttackModifier = calculateRangedAttackModifier();
    }

    // ---- Modifier Calculations ----

    private int calculateVisibilityModifier() {
        int modifier = 0;

        // Weather effects
        if (weather != null) {
            modifier += weather.getVisibilityModifier();
        }

        // Vegetation effects
        if (vegetationDensity > 0.7) {
            modifier -= 2; // Dense jungle severely reduces visibility
        } else if (vegetationDensity > 0.4) {
            modifier -= 1; // Moderate vegetation
        }

        // Night effects
        if (isNight) {
            modifier -= 2;
        }

        // Atmospheric visibility
        if (atmosphericVisibility < 100) {
            modifier -= 2;
        } else if (atmosphericVisibility < 500) {
            modifier -= 1;
        }

        return Math.max(-5, Math.min(5, modifier));
    }

    private int calculateMovementModifier() {
        int modifier = 0;

        // Weather effects
        if (weather != null) {
            modifier += weather.getMovementModifier();
        }

        // Vegetation effects
        if (vegetationDensity > 0.7) {
            modifier -= 2; // Dense jungle slows movement
        } else if (vegetationDensity > 0.4) {
            modifier -= 1;
        }

        // Water proximity effects
        if (nearWater && waterProximity < 50) {
            modifier -= 1; // Muddy ground near water
        }

        // Temperature effects
        if (temperature < -10) {
            modifier -= 1; // Extreme cold slows movement
        } else if (temperature > 40) {
            modifier -= 1; // Extreme heat causes fatigue
        }

        return Math.max(-5, Math.min(5, modifier));
    }

    private int calculateDefenseModifier() {
        int modifier = 0;

        // Vegetation provides cover
        if (vegetationDensity > 0.5) {
            modifier += 1;
        }

        // Night favors defense
        if (isNight) {
            modifier += 1;
        }

        return Math.max(-5, Math.min(5, modifier));
    }

    private int calculateRangedAttackModifier() {
        int modifier = 0;

        // Weather effects on ranged attacks
        if (weather != null) {
            switch (weather) {
                case RAIN:
                case STORM:
                    modifier -= 1;
                    break;
                case FOG:
                case SANDSTORM:
                    modifier -= 2;
                    break;
                default:
                    break;
            }
        }

        // Wind effects
        if (windSpeed > 15) {
            modifier -= 1; // Strong wind affects accuracy
        }

        // Vegetation blocks line of sight
        if (vegetationDensity > 0.6) {
            modifier -= 1;
        }

        // Night affects ranged attacks
        if (isNight) {
            modifier -= 1;
        }

        return Math.max(-5, Math.min(5, modifier));
    }

    // ---- Accessors ----

    public String getClimateZone() { return climateZone; }
    public Earth2026BattleEnvironment.WeatherCondition getWeather() { return weather; }
    public double getVegetationDensity() { return vegetationDensity; }
    public double getWaterProximity() { return waterProximity; }
    public boolean isNearWater() { return nearWater; }
    public double getTemperature() { return temperature; }
    public double getHumidity() { return humidity; }
    public double getWindSpeed() { return windSpeed; }
    public double getTimeOfDay() { return timeOfDay; }
    public boolean isNight() { return isNight; }
    public String getSeason() { return season; }
    public double getAtmosphericVisibility() { return atmosphericVisibility; }
    public LocalDateTime getGeneratedAt() { return generatedAt; }

    public int getVisibilityModifier() { return visibilityModifier; }
    public int getMovementModifier() { return movementModifier; }
    public int getDefenseModifier() { return defenseModifier; }
    public int getRangedAttackModifier() { return rangedAttackModifier; }

    /**
     * Gets all combat modifiers as a map.
     */
    public Map<String, Integer> getCombatModifiers() {
        Map<String, Integer> modifiers = new HashMap<>();
        modifiers.put("visibility", visibilityModifier);
        modifiers.put("movement", movementModifier);
        modifiers.put("defense", defenseModifier);
        modifiers.put("rangedAttack", rangedAttackModifier);
        return modifiers;
    }

    /**
     * Gets a description of the biosphere conditions.
     */
    public String getDescription() {
        StringBuilder sb = new StringBuilder();
        sb.append(climateZone).append(" climate, ");
        if (weather != null) {
            sb.append(weather.getName()).append(", ");
        }
        sb.append(season).append(", ");
        sb.append(String.format("%.1f°C", temperature));
        if (isNight) {
            sb.append(" (night)");
        }
        if (vegetationDensity > 0.5) {
            sb.append(", dense vegetation");
        }
        if (nearWater) {
            sb.append(", near water");
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return "BiosphereContext{" +
                "climate='" + climateZone + "', weather=" + weather +
                ", vegetation=" + String.format("%.2f", vegetationDensity) +
                ", temp=" + String.format("%.1f", temperature) +
                ", night=" + isNight +
                ", modifiers={vis=" + visibilityModifier + ", mov=" + movementModifier +
                ", def=" + defenseModifier + ", ranged=" + rangedAttackModifier + "}}";
    }

    // ---- Builder ----

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String climateZone = "TEMPERATE";
        private Earth2026BattleEnvironment.WeatherCondition weather =
                Earth2026BattleEnvironment.WeatherCondition.CLEAR;
        private double vegetationDensity = 0.3;
        private double waterProximity = 1000.0;
        private boolean nearWater = false;
        private double temperature = 20.0;
        private double humidity = 50.0;
        private double windSpeed = 5.0;
        private double timeOfDay = 0.5; // noon
        private boolean isNight = false;
        private String season = "SUMMER";
        private double atmosphericVisibility = 10000.0; // 10km default
        private LocalDateTime generatedAt;

        public Builder climateZone(String climateZone) {
            this.climateZone = climateZone;
            return this;
        }

        public Builder weather(Earth2026BattleEnvironment.WeatherCondition weather) {
            this.weather = weather;
            return this;
        }

        public Builder vegetationDensity(double vegetationDensity) {
            this.vegetationDensity = Math.max(0.0, Math.min(1.0, vegetationDensity));
            return this;
        }

        public Builder waterProximity(double waterProximity) {
            this.waterProximity = waterProximity;
            this.nearWater = waterProximity < 500;
            return this;
        }

        public Builder nearWater(boolean nearWater) {
            this.nearWater = nearWater;
            return this;
        }

        public Builder temperature(double temperature) {
            this.temperature = temperature;
            return this;
        }

        public Builder humidity(double humidity) {
            this.humidity = Math.max(0.0, Math.min(100.0, humidity));
            return this;
        }

        public Builder windSpeed(double windSpeed) {
            this.windSpeed = windSpeed;
            return this;
        }

        public Builder timeOfDay(double timeOfDay) {
            this.timeOfDay = Math.max(0.0, Math.min(1.0, timeOfDay));
            this.isNight = timeOfDay < 0.25 || timeOfDay > 0.75;
            return this;
        }

        public Builder isNight(boolean isNight) {
            this.isNight = isNight;
            return this;
        }

        public Builder season(String season) {
            this.season = season;
            return this;
        }

        public Builder atmosphericVisibility(double atmosphericVisibility) {
            this.atmosphericVisibility = atmosphericVisibility;
            return this;
        }

        public Builder generatedAt(LocalDateTime generatedAt) {
            this.generatedAt = generatedAt;
            return this;
        }

        public BiosphereContext build() {
            return new BiosphereContext(this);
        }
    }
}
