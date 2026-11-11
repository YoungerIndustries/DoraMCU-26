package com.yisy.modules.doramcu.earth2026.model;

import com.yisy.modules.doramcu.earth2026.model.Earth2026BattleEnvironment.StructureInfo;
import com.yisy.modules.doramcu.earth2026.model.Earth2026BattleEnvironment.UnitTypeInfo;
import com.yisy.modules.doramcu.earth2026.model.Earth2026BattleEnvironment.WeatherCondition;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Earth2026BattleEnvironment Model Tests")
class Earth2026BattleEnvironmentTest {

    @Nested
    @DisplayName("WeatherCondition Tests")
    class WeatherConditionTests {

        @Test
        @DisplayName("should have 9 weather conditions")
        void weatherCondition_count() {
            assertEquals(9, WeatherCondition.values().length);
        }

        @Test
        @DisplayName("CLEAR should have no modifiers")
        void CLEAR_noModifiers() {
            assertEquals(0, WeatherCondition.CLEAR.getVisibilityModifier());
            assertEquals(0, WeatherCondition.CLEAR.getMovementModifier());
            assertEquals("Clear", WeatherCondition.CLEAR.getName());
        }

        @Test
        @DisplayName("RAIN should have negative modifiers")
        void RAIN_negativeModifiers() {
            assertEquals(-1, WeatherCondition.RAIN.getVisibilityModifier());
            assertEquals(-1, WeatherCondition.RAIN.getMovementModifier());
        }

        @Test
        @DisplayName("STORM should have -2 modifiers")
        void STORM_modifiers() {
            assertEquals(-2, WeatherCondition.STORM.getVisibilityModifier());
            assertEquals(-2, WeatherCondition.STORM.getMovementModifier());
        }

        @Test
        @DisplayName("FOG should have -3 visibility, -1 movement")
        void FOG_modifiers() {
            assertEquals(-3, WeatherCondition.FOG.getVisibilityModifier());
            assertEquals(-1, WeatherCondition.FOG.getMovementModifier());
        }

        @Test
        @DisplayName("SNOW should have -1 visibility, -2 movement")
        void SNOW_modifiers() {
            assertEquals(-1, WeatherCondition.SNOW.getVisibilityModifier());
            assertEquals(-2, WeatherCondition.SNOW.getMovementModifier());
        }

        @Test
        @DisplayName("SANDSTORM should have -3/-3 modifiers")
        void SANDSTORM_modifiers() {
            assertEquals(-3, WeatherCondition.SANDSTORM.getVisibilityModifier());
            assertEquals(-3, WeatherCondition.SANDSTORM.getMovementModifier());
        }

        @Test
        @DisplayName("NIGHT should have -2 visibility, 0 movement")
        void NIGHT_modifiers() {
            assertEquals(-2, WeatherCondition.NIGHT.getVisibilityModifier());
            assertEquals(0, WeatherCondition.NIGHT.getMovementModifier());
        }

        @Test
        @DisplayName("all conditions should have descriptions")
        void allDescriptions() {
            for (WeatherCondition wc : WeatherCondition.values()) {
                assertNotNull(wc.getDescription());
                assertFalse(wc.getDescription().isBlank());
            }
        }
    }

    @Nested
    @DisplayName("StructureInfo Tests")
    class StructureInfoTests {

        @Test
        @DisplayName("should set all fields")
        void structureInfo_fields() {
            StructureInfo info = new StructureInfo(1L, "Bunker 7", "Bunker", 4, 80, 50, true, 3, true);
            assertEquals(1L, info.getId());
            assertEquals("Bunker 7", info.getName());
            assertEquals("Bunker", info.getStructureType());
            assertEquals(4, info.getTechLevel());
            assertEquals(80, info.getDefenseRating());
            assertEquals(50, info.getMaxCapacity());
            assertTrue(info.isProvidesCover());
            assertEquals(3, info.getCoverBonus());
            assertTrue(info.isStrategic());
        }
    }

    @Nested
    @DisplayName("UnitTypeInfo Tests")
    class UnitTypeInfoTests {

        @Test
        @DisplayName("should set all fields")
        void unitTypeInfo_fields() {
            UnitTypeInfo info = new UnitTypeInfo("Tanks", "Armored tanks", 4, 6, 5, 4);
            assertEquals("Tanks", info.getUnitType());
            assertEquals("Armored tanks", info.getDescription());
            assertEquals(4, info.getMinTechLevel());
            assertEquals(6, info.getAttackPower());
            assertEquals(5, info.getDefensePower());
            assertEquals(4, info.getMovement());
        }
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("should set all fields")
        void constructor_allFields() {
            List<StructureInfo> structures = List.of(
                    new StructureInfo(1L, "Bunker", "Bunker", 4, 80, 50, true, 3, true)
            );
            List<UnitTypeInfo> units = List.of(
                    new UnitTypeInfo("Tanks", "Armored tanks", 4, 6, 5, 4)
            );

            Earth2026BattleEnvironment env = new Earth2026BattleEnvironment(
                    1L, "Berlin", "Berlin", "Germany", 52.52, 13.405,
                    4, "World War II", "URBAN", WeatherCondition.RAIN,
                    structures, units);

            assertEquals(1L, env.getLocationId());
            assertEquals("Berlin", env.getLocationName());
            assertEquals("Berlin", env.getCity());
            assertEquals("Germany", env.getCountry());
            assertEquals(52.52, env.getLatitude());
            assertEquals(13.405, env.getLongitude());
            assertEquals(4, env.getBaseTechLevel());
            assertEquals("World War II", env.getTimePeriod());
            assertEquals("URBAN", env.getTerrainType());
            assertEquals(WeatherCondition.RAIN, env.getWeather());
            assertEquals(1, env.getStructures().size());
            assertEquals(1, env.getAvailableUnits().size());
            assertNotNull(env.getGeneratedAt());
        }

        @Test
        @DisplayName("should handle null structures")
        void constructor_nullStructures() {
            Earth2026BattleEnvironment env = new Earth2026BattleEnvironment(
                    1L, "Test", "Test", "Test", 0.0, 0.0,
                    4, "World War II", "URBAN", WeatherCondition.CLEAR,
                    null, null);

            assertNotNull(env.getStructures());
            assertTrue(env.getStructures().isEmpty());
            assertNotNull(env.getAvailableUnits());
            assertTrue(env.getAvailableUnits().isEmpty());
        }

        @Test
        @DisplayName("should handle null weather")
        void constructor_nullWeather() {
            Earth2026BattleEnvironment env = new Earth2026BattleEnvironment(
                    1L, "Test", "Test", "Test", 0.0, 0.0,
                    4, "World War II", "URBAN", null,
                    List.of(), List.of());

            assertNull(env.getWeather());
            assertEquals(0, env.getVisibilityModifier());
            assertEquals(0, env.getMovementModifier());
        }
    }

    @Nested
    @DisplayName("Weather modifiers Tests")
    class WeatherModifiersTests {

        @Test
        @DisplayName("should apply weather visibility modifier")
        void visibilityModifier_fromWeather() {
            Earth2026BattleEnvironment env = new Earth2026BattleEnvironment(
                    1L, "Test", "Test", "Test", 0.0, 0.0,
                    4, "World War II", "URBAN", WeatherCondition.FOG,
                    List.of(), List.of());

            assertEquals(-3, env.getVisibilityModifier());
        }

        @Test
        @DisplayName("should apply weather movement modifier")
        void movementModifier_fromWeather() {
            Earth2026BattleEnvironment env = new Earth2026BattleEnvironment(
                    1L, "Test", "Test", "Test", 0.0, 0.0,
                    4, "World War II", "URBAN", WeatherCondition.SNOW,
                    List.of(), List.of());

            assertEquals(-2, env.getMovementModifier());
        }
    }

    @Nested
    @DisplayName("getBaseTechLevelEnum Tests")
    class GetBaseTechLevelEnumTests {

        @Test
        @DisplayName("should return TL4 for base tech level 4")
        void getBaseTechLevelEnum_TL4() {
            Earth2026BattleEnvironment env = new Earth2026BattleEnvironment(
                    1L, "Test", "Test", "Test", 0.0, 0.0,
                    4, "World War II", "URBAN", WeatherCondition.CLEAR,
                    List.of(), List.of());

            assertEquals(TechLevel.TL4, env.getBaseTechLevelEnum());
        }
    }

    @Nested
    @DisplayName("getEffectiveDefenseBonus Tests")
    class GetEffectiveDefenseBonusTests {

        @Test
        @DisplayName("should return visibility modifier")
        void getEffectiveDefenseBonus_clear() {
            Earth2026BattleEnvironment env = new Earth2026BattleEnvironment(
                    1L, "Test", "Test", "Test", 0.0, 0.0,
                    4, "World War II", "URBAN", WeatherCondition.CLEAR,
                    List.of(), List.of());

            assertEquals(0, env.getEffectiveDefenseBonus());
        }

        @Test
        @DisplayName("should return negative for fog")
        void getEffectiveDefenseBonus_fog() {
            Earth2026BattleEnvironment env = new Earth2026BattleEnvironment(
                    1L, "Test", "Test", "Test", 0.0, 0.0,
                    4, "World War II", "URBAN", WeatherCondition.FOG,
                    List.of(), List.of());

            assertEquals(-3, env.getEffectiveDefenseBonus());
        }
    }

    @Nested
    @DisplayName("getEffectiveMovementPenalty Tests")
    class GetEffectiveMovementPenaltyTests {

        @Test
        @DisplayName("should return movement modifier")
        void getEffectiveMovementPenalty_sandstorm() {
            Earth2026BattleEnvironment env = new Earth2026BattleEnvironment(
                    1L, "Test", "Test", "Test", 0.0, 0.0,
                    4, "World War II", "URBAN", WeatherCondition.SANDSTORM,
                    List.of(), List.of());

            assertEquals(-3, env.getEffectiveMovementPenalty());
        }
    }

    @Nested
    @DisplayName("getStructuresByType Tests")
    class GetStructuresByTypeTests {

        @Test
        @DisplayName("should filter by type")
        void getStructuresByType_filter() {
            List<StructureInfo> structures = List.of(
                    new StructureInfo(1L, "Bunker 1", "Bunker", 4, 80, 50, true, 3, true),
                    new StructureInfo(2L, "Airfield", "Airfield", 4, 60, 100, false, 0, false),
                    new StructureInfo(3L, "Bunker 2", "Bunker", 4, 70, 40, true, 2, false)
            );
            Earth2026BattleEnvironment env = new Earth2026BattleEnvironment(
                    1L, "Test", "Test", "Test", 0.0, 0.0,
                    4, "World War II", "URBAN", WeatherCondition.CLEAR,
                    structures, List.of());

            List<StructureInfo> bunkers = env.getStructuresByType("Bunker");
            assertEquals(2, bunkers.size());
        }

        @Test
        @DisplayName("should be case-insensitive")
        void getStructuresByType_caseInsensitive() {
            List<StructureInfo> structures = List.of(
                    new StructureInfo(1L, "Bunker 1", "Bunker", 4, 80, 50, true, 3, true)
            );
            Earth2026BattleEnvironment env = new Earth2026BattleEnvironment(
                    1L, "Test", "Test", "Test", 0.0, 0.0,
                    4, "World War II", "URBAN", WeatherCondition.CLEAR,
                    structures, List.of());

            assertEquals(1, env.getStructuresByType("bunker").size());
            assertEquals(1, env.getStructuresByType("BUNKER").size());
        }

        @Test
        @DisplayName("should return empty for unknown type")
        void getStructuresByType_unknown() {
            Earth2026BattleEnvironment env = new Earth2026BattleEnvironment(
                    1L, "Test", "Test", "Test", 0.0, 0.0,
                    4, "World War II", "URBAN", WeatherCondition.CLEAR,
                    List.of(), List.of());

            assertTrue(env.getStructuresByType("Unknown").isEmpty());
        }
    }

    @Nested
    @DisplayName("getStrategicStructures Tests")
    class GetStrategicStructuresTests {

        @Test
        @DisplayName("should filter strategic structures")
        void getStrategicStructures_filter() {
            List<StructureInfo> structures = List.of(
                    new StructureInfo(1L, "Bunker 1", "Bunker", 4, 80, 50, true, 3, true),
                    new StructureInfo(2L, "Airfield", "Airfield", 4, 60, 100, false, 0, false),
                    new StructureInfo(3L, "Command Center", "Command Center", 4, 90, 30, true, 5, true)
            );
            Earth2026BattleEnvironment env = new Earth2026BattleEnvironment(
                    1L, "Test", "Test", "Test", 0.0, 0.0,
                    4, "World War II", "URBAN", WeatherCondition.CLEAR,
                    structures, List.of());

            List<StructureInfo> strategic = env.getStrategicStructures();
            assertEquals(2, strategic.size());
            assertTrue(strategic.stream().allMatch(StructureInfo::isStrategic));
        }

        @Test
        @DisplayName("should return empty when none strategic")
        void getStrategicStructures_none() {
            List<StructureInfo> structures = List.of(
                    new StructureInfo(1L, "Building", "Building", 4, 50, 100, false, 0, false)
            );
            Earth2026BattleEnvironment env = new Earth2026BattleEnvironment(
                    1L, "Test", "Test", "Test", 0.0, 0.0,
                    4, "World War II", "URBAN", WeatherCondition.CLEAR,
                    structures, List.of());

            assertTrue(env.getStrategicStructures().isEmpty());
        }
    }

    @Nested
    @DisplayName("getUnitsForTechLevel Tests")
    class GetUnitsForTechLevelTests {

        @Test
        @DisplayName("should filter units by tech level")
        void getUnitsForTechLevel_filter() {
            List<UnitTypeInfo> units = List.of(
                    new UnitTypeInfo("Infantry", "Basic", 0, 1, 1, 3),
                    new UnitTypeInfo("Tanks", "Armored", 4, 6, 5, 4),
                    new UnitTypeInfo("Drones", "Combat", 6, 5, 2, 6)
            );
            Earth2026BattleEnvironment env = new Earth2026BattleEnvironment(
                    1L, "Test", "Test", "Test", 0.0, 0.0,
                    4, "World War II", "URBAN", WeatherCondition.CLEAR,
                    List.of(), units);

            // TL4 should include Infantry (0) and Tanks (4), but not Drones (6)
            List<UnitTypeInfo> available = env.getUnitsForTechLevel(4);
            assertEquals(2, available.size());
        }

        @Test
        @DisplayName("should include all units at high tech level")
        void getUnitsForTechLevel_all() {
            List<UnitTypeInfo> units = List.of(
                    new UnitTypeInfo("Infantry", "Basic", 0, 1, 1, 3),
                    new UnitTypeInfo("Tanks", "Armored", 4, 6, 5, 4)
            );
            Earth2026BattleEnvironment env = new Earth2026BattleEnvironment(
                    1L, "Test", "Test", "Test", 0.0, 0.0,
                    4, "World War II", "URBAN", WeatherCondition.CLEAR,
                    List.of(), units);

            assertEquals(2, env.getUnitsForTechLevel(10).size());
        }
    }

    @Nested
    @DisplayName("toMap Tests")
    class ToMapTests {

        @Test
        @DisplayName("should include all fields")
        void toMap_allFields() {
            Earth2026BattleEnvironment env = new Earth2026BattleEnvironment(
                    1L, "Berlin", "Berlin", "Germany", 52.52, 13.405,
                    4, "World War II", "URBAN", WeatherCondition.RAIN,
                    List.of(), List.of());

            Map<String, Object> map = env.toMap();
            assertEquals(1L, map.get("locationId"));
            assertEquals("Berlin", map.get("locationName"));
            assertEquals("Berlin", map.get("city"));
            assertEquals("Germany", map.get("country"));
            assertEquals(52.52, map.get("latitude"));
            assertEquals(13.405, map.get("longitude"));
            assertEquals(4, map.get("baseTechLevel"));
            assertEquals("World War II", map.get("timePeriod"));
            assertEquals("URBAN", map.get("terrainType"));
            assertEquals("Rain", map.get("weather"));
            assertEquals(-1, map.get("visibilityModifier"));
            assertEquals(-1, map.get("movementModifier"));
            assertNotNull(map.get("generatedAt"));
        }

        @Test
        @DisplayName("should handle null weather")
        void toMap_nullWeather() {
            Earth2026BattleEnvironment env = new Earth2026BattleEnvironment(
                    1L, "Test", "Test", "Test", 0.0, 0.0,
                    4, "World War II", "URBAN", null,
                    List.of(), List.of());

            Map<String, Object> map = env.toMap();
            assertEquals("CLEAR", map.get("weather"));
        }
    }

    @Nested
    @DisplayName("toString Tests")
    class ToStringTests {

        @Test
        @DisplayName("should include location, TL, period, terrain, weather, structure count")
        void toString_format() {
            Earth2026BattleEnvironment env = new Earth2026BattleEnvironment(
                    1L, "Berlin", "Berlin", "Germany", 52.52, 13.405,
                    4, "World War II", "URBAN", WeatherCondition.RAIN,
                    List.of(
                            new StructureInfo(1L, "Bunker", "Bunker", 4, 80, 50, true, 3, true)
                    ),
                    List.of());

            String s = env.toString();
            assertTrue(s.contains("Berlin"));
            assertTrue(s.contains("TL=4"));
            assertTrue(s.contains("World War II"));
            assertTrue(s.contains("URBAN"));
            assertTrue(s.contains("structures=1"));
        }
    }
}
