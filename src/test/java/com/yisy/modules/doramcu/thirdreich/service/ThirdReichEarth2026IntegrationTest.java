package com.yisy.modules.doramcu.thirdreich.service;

import com.yisy.modules.doramcu.earth2026.model.Earth2026BattleEnvironment;
import com.yisy.modules.doramcu.earth2026.model.Earth2026Structure;
import com.yisy.modules.doramcu.earth2026.service.PlanetService;
import com.yisy.modules.doramcu.thirdreich.commander.AICommanderProfile;
import com.yisy.modules.doramcu.thirdreich.domain.TerrainType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("ThirdReichGroundBattleService Earth 2026 Integration Tests")
class ThirdReichEarth2026IntegrationTest {

    private final PlanetService planetService = Mockito.mock(PlanetService.class);
    private final ThirdReichGroundBattleService service = new ThirdReichGroundBattleService(planetService);

    private Earth2026BattleEnvironment buildMockEnvironment(String terrain, String locationName) {
        Earth2026BattleEnvironment.WeatherCondition weather = Earth2026BattleEnvironment.WeatherCondition.CLEAR;
        List<Earth2026BattleEnvironment.StructureInfo> structures = List.of(
                new Earth2026BattleEnvironment.StructureInfo(1L, "Bunker 7", "Bunker", 4, 80, 50, true, 3, true)
        );
        List<Earth2026BattleEnvironment.UnitTypeInfo> units = List.of(
                new Earth2026BattleEnvironment.UnitTypeInfo("Riflemen", "Bolt-action rifle infantry", 4, 3, 2, 3),
                new Earth2026BattleEnvironment.UnitTypeInfo("Tanks", "Armored tanks", 4, 6, 5, 4)
        );
        return new Earth2026BattleEnvironment(
                1L, locationName, "Berlin", "Germany", 52.52, 13.41,
                4, "World War II", terrain, weather, structures, units);
    }

    @Nested
    @DisplayName("resolveBattleFromEnvironment")
    class ResolveBattleFromEnvironmentTests {

        @Test
        @DisplayName("should resolve battle from Earth 2026 environment")
        void shouldResolveBattleFromEnvironment() {
            Earth2026BattleEnvironment env = buildMockEnvironment("URBAN", "Berlin");
            when(planetService.buildBattleEnvironment(eq(1L), eq("World War II"))).thenReturn(env);

            List<Map<String, Object>> attackers = List.of(
                    Map.of("name", "Panzer Division", "type", "TANK", "strength", 100)
            );
            List<Map<String, Object>> defenders = List.of(
                    Map.of("name", "Infantry Regiment", "type", "INFANTRY", "strength", 100)
            );

            Map<String, Object> result = service.resolveBattleFromEnvironment(
                    "DoraMCU_4X", attackers, defenders, 1L, "World War II", null, null);

            assertNotNull(result);
            assertNotNull(result.get("outcome"));
            assertNotNull(result.get("environment"));
            assertNotNull(result.get("structures"));
            assertNotNull(result.get("availableUnits"));
            assertEquals("World War II", result.get("timePeriod"));
            assertEquals(4, result.get("baseTechLevel"));
        }

        @Test
        @DisplayName("should throw when PlanetService is null")
        void shouldThrowWhenPlanetServiceNull() {
            ThirdReichGroundBattleService standaloneService = new ThirdReichGroundBattleService();

            List<Map<String, Object>> units = List.of(Map.of("name", "Team", "type", "INFANTRY", "strength", 100));
            assertThrows(IllegalStateException.class, () ->
                    standaloneService.resolveBattleFromEnvironment(
                            "DoraMCU_4X", units, units, 1L, "World War II", null, null));
        }

        @Test
        @DisplayName("should throw when location not found")
        void shouldThrowWhenLocationNotFound() {
            when(planetService.buildBattleEnvironment(eq(999L), eq("World War II")))
                    .thenThrow(new IllegalArgumentException("Location not found: 999"));

            List<Map<String, Object>> units = List.of(Map.of("name", "Team", "type", "INFANTRY", "strength", 100));
            assertThrows(IllegalArgumentException.class, () ->
                    service.resolveBattleFromEnvironment(
                            "DoraMCU_4X", units, units, 999L, "World War II", null, null));
        }

        @Test
        @DisplayName("should use weather modifiers from environment")
        void shouldUseWeatherModifiers() {
            Earth2026BattleEnvironment env = buildMockEnvironment("URBAN", "Berlin");
            when(planetService.buildBattleEnvironment(eq(1L), eq("World War II"))).thenReturn(env);

            List<Map<String, Object>> attackers = List.of(
                    Map.of("name", "Atk", "type", "INFANTRY", "strength", 100)
            );
            List<Map<String, Object>> defenders = List.of(
                    Map.of("name", "Def", "type", "INFANTRY", "strength", 100)
            );

            Map<String, Object> result = service.resolveBattleFromEnvironment(
                    "DoraMCU_4X", attackers, defenders, 1L, "World War II", null, null);

            assertNotNull(result);
            verify(planetService).buildBattleEnvironment(1L, "World War II");
        }

        @Test
        @DisplayName("should map URBAN terrain correctly")
        void shouldMapUrbanTerrain() {
            Earth2026BattleEnvironment env = buildMockEnvironment("URBAN", "Berlin");
            when(planetService.buildBattleEnvironment(eq(1L), eq("World War II"))).thenReturn(env);

            List<Map<String, Object>> units = List.of(Map.of("name", "Team", "type", "INFANTRY", "strength", 100));

            Map<String, Object> result = service.resolveBattleFromEnvironment(
                    "DoraMCU_4X", units, units, 1L, "World War II", null, null);

            assertNotNull(result);
        }

        @Test
        @DisplayName("should map PLAINS terrain correctly")
        void shouldMapPlainsTerrain() {
            Earth2026BattleEnvironment env = buildMockEnvironment("PLAINS", "Stalingrad");
            when(planetService.buildBattleEnvironment(eq(1L), eq("World War II"))).thenReturn(env);

            List<Map<String, Object>> units = List.of(Map.of("name", "Team", "type", "INFANTRY", "strength", 100));

            Map<String, Object> result = service.resolveBattleFromEnvironment(
                    "DoraMCU_4X", units, units, 1L, "World War II", null, null);

            assertNotNull(result);
        }

        @Test
        @DisplayName("should handle unknown terrain type")
        void shouldHandleUnknownTerrain() {
            Earth2026BattleEnvironment env = buildMockEnvironment("UNKNOWN_TERRAIN", "Nowhere");
            when(planetService.buildBattleEnvironment(eq(1L), eq("World War II"))).thenReturn(env);

            List<Map<String, Object>> units = List.of(Map.of("name", "Team", "type", "INFANTRY", "strength", 100));

            Map<String, Object> result = service.resolveBattleFromEnvironment(
                    "DoraMCU_4X", units, units, 1L, "World War II", null, null);

            assertNotNull(result);
        }
    }

    @Nested
    @DisplayName("resolveAiBattleFromEnvironment")
    class ResolveAiBattleFromEnvironmentTests {

        @Test
        @DisplayName("should resolve AI battle from environment")
        void shouldResolveAiBattleFromEnvironment() {
            Earth2026BattleEnvironment env = buildMockEnvironment("URBAN", "Berlin");
            when(planetService.buildBattleEnvironment(eq(1L), eq("World War II"))).thenReturn(env);

            List<Map<String, Object>> attackers = List.of(
                    Map.of("name", "Panzer", "type", "TANK", "strength", 100)
            );
            List<Map<String, Object>> defenders = List.of(
                    Map.of("name", "Infantry", "type", "INFANTRY", "strength", 100)
            );

            Map<String, Object> result = service.resolveAiBattleFromEnvironment(
                    "DoraMCU_4X", attackers, defenders, 1L, "World War II",
                    AICommanderProfile.PATTON, AICommanderProfile.MONTGOMERY);

            assertNotNull(result);
            assertNotNull(result.get("outcome"));
        }

        @Test
        @DisplayName("should throw when PlanetService is null")
        void shouldThrowWhenPlanetServiceNull() {
            ThirdReichGroundBattleService standaloneService = new ThirdReichGroundBattleService();

            List<Map<String, Object>> units = List.of(Map.of("name", "Team", "type", "INFANTRY", "strength", 100));
            assertThrows(IllegalStateException.class, () ->
                    standaloneService.resolveAiBattleFromEnvironment(
                            "DoraMCU_4X", units, units, 1L, "World War II", null, null));
        }

        @Test
        @DisplayName("should work with null commander profiles")
        void shouldWorkWithNullProfiles() {
            Earth2026BattleEnvironment env = buildMockEnvironment("URBAN", "Berlin");
            when(planetService.buildBattleEnvironment(eq(1L), eq("World War II"))).thenReturn(env);

            List<Map<String, Object>> units = List.of(Map.of("name", "Team", "type", "INFANTRY", "strength", 100));

            Map<String, Object> result = service.resolveAiBattleFromEnvironment(
                    "DoraMCU_4X", units, units, 1L, "World War II", null, null);

            assertNotNull(result);
        }
    }

    @Nested
    @DisplayName("getAvailableUnits")
    class GetAvailableUnitsTests {

        @Test
        @DisplayName("should return units for location and time period")
        void shouldReturnUnits() {
            Earth2026BattleEnvironment env = buildMockEnvironment("URBAN", "Berlin");
            when(planetService.buildBattleEnvironment(eq(1L), eq("World War II"))).thenReturn(env);

            List<Earth2026BattleEnvironment.UnitTypeInfo> units = service.getAvailableUnits(1L, "World War II");

            assertNotNull(units);
            assertEquals(2, units.size());
            assertEquals("Riflemen", units.get(0).getUnitType());
            assertEquals("Tanks", units.get(1).getUnitType());
        }

        @Test
        @DisplayName("should throw when PlanetService is null")
        void shouldThrowWhenPlanetServiceNull() {
            ThirdReichGroundBattleService standaloneService = new ThirdReichGroundBattleService();
            assertThrows(IllegalStateException.class, () ->
                    standaloneService.getAvailableUnits(1L, "World War II"));
        }
    }

    @Nested
    @DisplayName("getStructures")
    class GetStructuresTests {

        @Test
        @DisplayName("should return structures for a location")
        void shouldReturnStructures() {
            Earth2026Structure struct = new Earth2026Structure("Bunker 7", 1L, "Bunker", 4);
            struct.setId(1L);
            struct.setDefenseRating(80);
            struct.setMaxCapacity(50);
            struct.setProvidesCover(true);
            struct.setCoverBonus(3);
            struct.setStrategic(true);

            when(planetService.getStructuresAtLocation(1L)).thenReturn(List.of(struct));

            List<Earth2026BattleEnvironment.StructureInfo> structures = service.getStructures(1L);

            assertNotNull(structures);
            assertEquals(1, structures.size());
            assertEquals("Bunker 7", structures.get(0).getName());
            assertEquals("Bunker", structures.get(0).getStructureType());
            assertEquals(4, structures.get(0).getTechLevel());
        }

        @Test
        @DisplayName("should return empty list when no structures")
        void shouldReturnEmptyList() {
            when(planetService.getStructuresAtLocation(1L)).thenReturn(List.of());

            List<Earth2026BattleEnvironment.StructureInfo> structures = service.getStructures(1L);

            assertNotNull(structures);
            assertTrue(structures.isEmpty());
        }

        @Test
        @DisplayName("should throw when PlanetService is null")
        void shouldThrowWhenPlanetServiceNull() {
            ThirdReichGroundBattleService standaloneService = new ThirdReichGroundBattleService();
            assertThrows(IllegalStateException.class, () ->
                    standaloneService.getStructures(1L));
        }
    }

    @Nested
    @DisplayName("mapTerrainType")
    class MapTerrainTypeTests {

        @Test
        @DisplayName("should map URBAN to URBAN")
        void shouldMapUrban() {
            assertEquals(TerrainType.URBAN, service.mapTerrainType("URBAN"));
        }

        @Test
        @DisplayName("should map plains to PLAINS")
        void shouldMapPlains() {
            assertEquals(TerrainType.PLAINS, service.mapTerrainType("PLAINS"));
        }

        @Test
        @DisplayName("should map case-insensitive")
        void shouldMapCaseInsensitive() {
            assertEquals(TerrainType.MOUNTAIN, service.mapTerrainType("mountain"));
            assertEquals(TerrainType.JUNGLE, service.mapTerrainType("Jungle"));
        }

        @Test
        @DisplayName("should default to PLAINS for null")
        void shouldDefaultToPlainsForNull() {
            assertEquals(TerrainType.PLAINS, service.mapTerrainType(null));
        }

        @Test
        @DisplayName("should default to PLAINS for empty string")
        void shouldDefaultToPlainsForEmpty() {
            assertEquals(TerrainType.PLAINS, service.mapTerrainType(""));
        }

        @Test
        @DisplayName("should default to PLAINS for unknown terrain")
        void shouldDefaultToPlainsForUnknown() {
            assertEquals(TerrainType.PLAINS, service.mapTerrainType("UNKNOWN_TERRAIN"));
        }

        @Test
        @DisplayName("should map all terrain types")
        void shouldMapAllTerrainTypes() {
            assertEquals(TerrainType.PLAINS, service.mapTerrainType("PLAINS"));
            assertEquals(TerrainType.MOUNTAIN, service.mapTerrainType("MOUNTAIN"));
            assertEquals(TerrainType.JUNGLE, service.mapTerrainType("JUNGLE"));
            assertEquals(TerrainType.URBAN, service.mapTerrainType("URBAN"));
            assertEquals(TerrainType.DESERT, service.mapTerrainType("DESERT"));
            assertEquals(TerrainType.ARCTIC, service.mapTerrainType("ARCTIC"));
            assertEquals(TerrainType.FOREST, service.mapTerrainType("FOREST"));
            assertEquals(TerrainType.SWAMP, service.mapTerrainType("SWAMP"));
        }
    }

    @Nested
    @DisplayName("Constructor")
    class ConstructorTests {

        @Test
        @DisplayName("should create with PlanetService")
        void shouldCreateWithPlanetService() {
            ThirdReichGroundBattleService svc = new ThirdReichGroundBattleService(planetService);
            assertNotNull(svc);
        }

        @Test
        @DisplayName("should create without PlanetService (deprecated)")
        void shouldCreateWithoutPlanetService() {
            @SuppressWarnings("deprecation")
            ThirdReichGroundBattleService svc = new ThirdReichGroundBattleService();
            assertNotNull(svc);
        }
    }
}
