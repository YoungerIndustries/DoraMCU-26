package com.yisy.modules.doramcu.earth2026.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Complete battle environment package provided by Earth 2026's PlanetService.
 * <p>
 * When a game (e.g., Third Reich) requests a battle environment from Earth 2026,
 * this object contains everything needed to build the battle: terrain type,
 * structures list, weather conditions, map coordinates, available unit types
 * based on tech level, and the time period template used.
 * </p>
 * <p>
 * This is a value object, not a JPA entity — it is assembled on demand from
 * Earth2026Location, Earth2026Structure, and TechLevel data.
 * </p>
 */
public class Earth2026BattleEnvironment {

    private final Long locationId;
    private final String locationName;
    private final String city;
    private final String country;
    private final Double latitude;
    private final Double longitude;

    /** Base tech level of the planet/location. */
    private final int baseTechLevel;

    /** Time period template used (e.g., WWII, MODERN). */
    private final String timePeriod;

    /** Terrain type for combat resolution. */
    private final String terrainType;

    /** Weather conditions affecting the battle. */
    private final WeatherCondition weather;

    /** List of structures at this location. */
    private final List<StructureInfo> structures;

    /** Available unit types based on tech level. */
    private final List<UnitTypeInfo> availableUnits;

    /** Visibility modifier for combat (-5 to +5). */
    private final int visibilityModifier;

    /** Movement modifier for combat (-5 to +5). */
    private final int movementModifier;

    /** When this environment was generated. */
    private final LocalDateTime generatedAt;

    /**
     * Structure information within the battle environment.
     */
    public static class StructureInfo {
        private final Long id;
        private final String name;
        private final String structureType;
        private final int techLevel;
        private final int defenseRating;
        private final int maxCapacity;
        private final boolean providesCover;
        private final int coverBonus;
        private final boolean isStrategic;

        public StructureInfo(Long id, String name, String structureType, int techLevel,
                             int defenseRating, int maxCapacity, boolean providesCover,
                             int coverBonus, boolean isStrategic) {
            this.id = id;
            this.name = name;
            this.structureType = structureType;
            this.techLevel = techLevel;
            this.defenseRating = defenseRating;
            this.maxCapacity = maxCapacity;
            this.providesCover = providesCover;
            this.coverBonus = coverBonus;
            this.isStrategic = isStrategic;
        }

        public Long getId() { return id; }
        public String getName() { return name; }
        public String getStructureType() { return structureType; }
        public int getTechLevel() { return techLevel; }
        public int getDefenseRating() { return defenseRating; }
        public int getMaxCapacity() { return maxCapacity; }
        public boolean isProvidesCover() { return providesCover; }
        public int getCoverBonus() { return coverBonus; }
        public boolean isStrategic() { return isStrategic; }
    }

    /**
     * Unit type information available at this tech level.
     */
    public static class UnitTypeInfo {
        private final String unitType;
        private final String description;
        private final int minTechLevel;
        private final int attackPower;
        private final int defensePower;
        private final int movement;

        public UnitTypeInfo(String unitType, String description, int minTechLevel,
                            int attackPower, int defensePower, int movement) {
            this.unitType = unitType;
            this.description = description;
            this.minTechLevel = minTechLevel;
            this.attackPower = attackPower;
            this.defensePower = defensePower;
            this.movement = movement;
        }

        public String getUnitType() { return unitType; }
        public String getDescription() { return description; }
        public int getMinTechLevel() { return minTechLevel; }
        public int getAttackPower() { return attackPower; }
        public int getDefensePower() { return defensePower; }
        public int getMovement() { return movement; }
    }

    /**
     * Weather conditions affecting combat.
     */
    public enum WeatherCondition {
        CLEAR("Clear", 0, 0, "Good visibility, normal movement"),
        CLOUDY("Cloudy", 0, 0, "Slightly reduced visibility"),
        RAIN("Rain", -1, -1, "Reduced visibility, slippery ground"),
        STORM("Storm", -2, -2, "Poor visibility, difficult movement"),
        FOG("Fog", -3, -1, "Very poor visibility, cautious movement"),
        SNOW("Snow", -1, -2, "Reduced visibility, slow movement"),
        SANDSTORM("Sandstorm", -3, -3, "Near zero visibility, very slow movement"),
        NIGHT("Night", -2, 0, "Darkness, normal movement"),
        TWILIGHT("Twilight", -1, 0, "Dim light, normal movement");

        private final String name;
        private final int visibilityModifier;
        private final int movementModifier;
        private final String description;

        WeatherCondition(String name, int visibilityModifier, int movementModifier, String description) {
            this.name = name;
            this.visibilityModifier = visibilityModifier;
            this.movementModifier = movementModifier;
            this.description = description;
        }

        public String getName() { return name; }
        public int getVisibilityModifier() { return visibilityModifier; }
        public int getMovementModifier() { return movementModifier; }
        public String getDescription() { return description; }
    }

    public Earth2026BattleEnvironment(Long locationId, String locationName, String city,
                                       String country, Double latitude, Double longitude,
                                       int baseTechLevel, String timePeriod, String terrainType,
                                       WeatherCondition weather, List<StructureInfo> structures,
                                       List<UnitTypeInfo> availableUnits) {
        this.locationId = locationId;
        this.locationName = locationName;
        this.city = city;
        this.country = country;
        this.latitude = latitude;
        this.longitude = longitude;
        this.baseTechLevel = baseTechLevel;
        this.timePeriod = timePeriod;
        this.terrainType = terrainType;
        this.weather = weather;
        this.structures = structures != null ? structures : new ArrayList<>();
        this.availableUnits = availableUnits != null ? availableUnits : new ArrayList<>();
        this.visibilityModifier = (weather != null ? weather.getVisibilityModifier() : 0);
        this.movementModifier = (weather != null ? weather.getMovementModifier() : 0);
        this.generatedAt = LocalDateTime.now();
    }

    public Long getLocationId() { return locationId; }
    public String getLocationName() { return locationName; }
    public String getCity() { return city; }
    public String getCountry() { return country; }
    public Double getLatitude() { return latitude; }
    public Double getLongitude() { return longitude; }
    public int getBaseTechLevel() { return baseTechLevel; }
    public TechLevel getBaseTechLevelEnum() { return TechLevel.fromLevel(baseTechLevel); }
    public String getTimePeriod() { return timePeriod; }
    public String getTerrainType() { return terrainType; }
    public WeatherCondition getWeather() { return weather; }
    public List<StructureInfo> getStructures() { return structures; }
    public List<UnitTypeInfo> getAvailableUnits() { return availableUnits; }
    public int getVisibilityModifier() { return visibilityModifier; }
    public int getMovementModifier() { return movementModifier; }
    public LocalDateTime getGeneratedAt() { return generatedAt; }

    /**
     * Get the effective defense bonus from terrain and weather combined.
     */
    public int getEffectiveDefenseBonus() {
        // Terrain defense bonus is determined by the consuming game
        // Weather visibility modifier affects defense
        return visibilityModifier;
    }

    /**
     * Get the effective movement penalty from terrain and weather combined.
     */
    public int getEffectiveMovementPenalty() {
        return movementModifier;
    }

    /**
     * Get structures filtered by type.
     */
    public List<StructureInfo> getStructuresByType(String type) {
        return structures.stream()
                .filter(s -> s.getStructureType().equalsIgnoreCase(type))
                .toList();
    }

    /**
     * Get strategic structures only.
     */
    public List<StructureInfo> getStrategicStructures() {
        return structures.stream()
                .filter(StructureInfo::isStrategic)
                .toList();
    }

    /**
     * Get units available at or below this environment's tech level.
     */
    public List<UnitTypeInfo> getUnitsForTechLevel(int techLevel) {
        return availableUnits.stream()
                .filter(u -> u.getMinTechLevel() <= techLevel)
                .toList();
    }

    /**
     * Convert to a Map for JSON serialization.
     */
    public Map<String, Object> toMap() {
        Map<String, Object> map = new java.util.HashMap<>();
        map.put("locationId", locationId);
        map.put("locationName", locationName);
        map.put("city", city);
        map.put("country", country);
        map.put("latitude", latitude);
        map.put("longitude", longitude);
        map.put("baseTechLevel", baseTechLevel);
        map.put("timePeriod", timePeriod);
        map.put("terrainType", terrainType);
        map.put("weather", weather != null ? weather.getName() : "CLEAR");
        map.put("structures", structures);
        map.put("availableUnits", availableUnits);
        map.put("visibilityModifier", visibilityModifier);
        map.put("movementModifier", movementModifier);
        map.put("generatedAt", generatedAt);
        return map;
    }

    @Override
    public String toString() {
        return "Earth2026BattleEnvironment{location='" + locationName + "', city='" + city +
                "', TL=" + baseTechLevel + ", period='" + timePeriod + "', terrain='" +
                terrainType + "', weather=" + weather + ", structures=" + structures.size() + "}";
    }
}
