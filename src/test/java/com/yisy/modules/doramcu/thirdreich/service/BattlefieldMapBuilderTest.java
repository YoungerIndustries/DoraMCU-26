package com.yisy.modules.doramcu.thirdreich.service;

import com.yisy.modules.doramcu.earth2026.model.*;
import com.yisy.modules.doramcu.earth2026.service.BiosphereService;
import com.yisy.modules.doramcu.earth2026.service.PlanetService;
import com.yisy.modules.doramcu.thirdreich.domain.BattlefieldMap;
import com.yisy.modules.doramcu.thirdreich.domain.ThirdReichGroundUnit;
import com.yisy.modules.doramcu.thirdreich.domain.ThirdReichUnitType;
import com.yisy.modules.doramcu.thirdreich.domain.UnitPlacement;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("BattlefieldMapBuilder Tests")
class BattlefieldMapBuilderTest {

    private final PlanetService planetService = Mockito.mock(PlanetService.class);
    private final BiosphereService biosphereService = Mockito.mock(BiosphereService.class);
    private final BattlefieldMapBuilder builder = new BattlefieldMapBuilder(planetService, biosphereService);

    private Earth2026BattleEnvironment createEnvironment(String terrain, int techLevel) {
        List<Earth2026BattleEnvironment.StructureInfo> structures = List.of(
                new Earth2026BattleEnvironment.StructureInfo(
                        1L, "Bunker", "Bunker", 4, 80, 50, true, 3, true,
                        50.0, 50.0, 0.0, 10.0, 10.0, 10.0)
        );
        TerrainElevationMap elevationMap = TerrainElevationMap.generateRandom(
                20, 20, 10.0, 100.0, terrain, 0.3);
        return new Earth2026BattleEnvironment(
                1L, "Berlin", "Berlin", "Germany", 52.52, 13.41,
                techLevel, "World War II", terrain,
                Earth2026BattleEnvironment.WeatherCondition.CLEAR,
                structures, List.of(),
                elevationMap, 100.0, 0.3);
    }

    @Nested
    @DisplayName("buildBattlefieldMap")
    class BuildBattlefieldMapTests {

        @Test
        @DisplayName("should build map from environment")
        void shouldBuildMapFromEnvironment() {
            Earth2026BattleEnvironment env = createEnvironment("URBAN", 4);
            BiosphereContext biosphere = BiosphereContext.builder()
                    .climateZone("TEMPERATE")
                    .temperature(20.0)
                    .season("SUMMER")
                    .build();

            when(planetService.buildBattleEnvironment(eq(1L), eq("World War II"), eq("URBAN"), anyString()))
                    .thenReturn(env);
            when(biosphereService.createDefaultBiosphereContext(any()))
                    .thenReturn(biosphere);

            BattlefieldMap map = builder.buildBattlefieldMap(1L, "World War II", "URBAN",
                    Earth2026BattleEnvironment.WeatherCondition.CLEAR);

            assertNotNull(map);
            assertEquals("Berlin", map.getCity());
            assertEquals("Germany", map.getCountry());
            assertEquals(4, map.getBaseTechLevel());
            assertEquals("World War II", map.getTimePeriod());
            assertNotNull(map.getTerrainElevationMap());
            assertNotNull(map.getBiosphereContext());
            assertEquals(1, map.getStructures().size());
        }

        @Test
        @DisplayName("should handle null terrain type")
        void shouldHandleNullTerrain() {
            Earth2026BattleEnvironment env = createEnvironment("PLAINS", 4);
            BiosphereContext biosphere = BiosphereContext.builder().build();

            when(planetService.buildBattleEnvironment(eq(1L), eq("World War II"), isNull(), anyString()))
                    .thenReturn(env);
            when(biosphereService.createDefaultBiosphereContext(any()))
                    .thenReturn(biosphere);

            BattlefieldMap map = builder.buildBattlefieldMap(1L, "World War II", null,
                    Earth2026BattleEnvironment.WeatherCondition.CLEAR);

            assertNotNull(map);
        }
    }

    @Nested
    @DisplayName("autoPopulateForces")
    class AutoPopulateForcesTests {

        @Test
        @DisplayName("should populate attacker and defender forces")
        void shouldPopulateForces() {
            Earth2026BattleEnvironment env = createEnvironment("URBAN", 4);
            BiosphereContext biosphere = BiosphereContext.builder().build();

            when(planetService.buildBattleEnvironment(anyLong(), anyString(), anyString(), anyString()))
                    .thenReturn(env);
            when(biosphereService.createDefaultBiosphereContext(any()))
                    .thenReturn(biosphere);

            BattlefieldMap map = builder.buildBattlefieldMap(1L, "World War II", "URBAN",
                    Earth2026BattleEnvironment.WeatherCondition.CLEAR);

            builder.autoPopulateForces(map, 5, 3);

            assertEquals(5, map.getAttackerUnitCount());
            assertEquals(3, map.getDefenderUnitCount());
        }

        @Test
        @DisplayName("should place attackers on left side")
        void shouldPlaceAttackersOnLeft() {
            Earth2026BattleEnvironment env = createEnvironment("URBAN", 4);
            BiosphereContext biosphere = BiosphereContext.builder().build();

            when(planetService.buildBattleEnvironment(anyLong(), anyString(), anyString(), anyString()))
                    .thenReturn(env);
            when(biosphereService.createDefaultBiosphereContext(any()))
                    .thenReturn(biosphere);

            BattlefieldMap map = builder.buildBattlefieldMap(1L, "World War II", "URBAN",
                    Earth2026BattleEnvironment.WeatherCondition.CLEAR);

            builder.autoPopulateForces(map, 3, 0);

            for (UnitPlacement p : map.getAttackerPlacements()) {
                // Attackers placed in left 20% of terrain grid (0 to maxX*0.2)
                assertTrue(p.getX() >= 0 && p.getX() <= 40);
                assertTrue(p.isAttacker());
            }
        }

        @Test
        @DisplayName("should place defenders on right side")
        void shouldPlaceDefendersOnRight() {
            Earth2026BattleEnvironment env = createEnvironment("URBAN", 4);
            BiosphereContext biosphere = BiosphereContext.builder().build();

            when(planetService.buildBattleEnvironment(anyLong(), anyString(), anyString(), anyString()))
                    .thenReturn(env);
            when(biosphereService.createDefaultBiosphereContext(any()))
                    .thenReturn(biosphere);

            BattlefieldMap map = builder.buildBattlefieldMap(1L, "World War II", "URBAN",
                    Earth2026BattleEnvironment.WeatherCondition.CLEAR);

            builder.autoPopulateForces(map, 0, 3);

            for (UnitPlacement p : map.getDefenderPlacements()) {
                // Defenders placed in right 20% of terrain grid (maxX*0.8 to maxX)
                assertTrue(p.getX() >= 160 && p.getX() <= 200);
                assertFalse(p.isAttacker());
            }
        }

        @Test
        @DisplayName("should generate WWII era units for TL4")
        void shouldGenerateWWIIUnits() {
            Earth2026BattleEnvironment env = createEnvironment("URBAN", 4);
            BiosphereContext biosphere = BiosphereContext.builder().build();

            when(planetService.buildBattleEnvironment(anyLong(), anyString(), anyString(), anyString()))
                    .thenReturn(env);
            when(biosphereService.createDefaultBiosphereContext(any()))
                    .thenReturn(biosphere);

            BattlefieldMap map = builder.buildBattlefieldMap(1L, "World War II", "URBAN",
                    Earth2026BattleEnvironment.WeatherCondition.CLEAR);

            builder.autoPopulateForces(map, 5, 0);

            // WWII era should have infantry, tank, artillery, anti-air, engineer
            for (UnitPlacement p : map.getAttackerPlacements()) {
                ThirdReichUnitType type = p.getUnit().getUnitType();
                assertTrue(type == ThirdReichUnitType.INFANTRY ||
                           type == ThirdReichUnitType.TANK ||
                           type == ThirdReichUnitType.ARTILLERY ||
                           type == ThirdReichUnitType.ANTI_AIR ||
                           type == ThirdReichUnitType.ENGINEER);
            }
        }

        @Test
        @DisplayName("should generate modern units for TL5+")
        void shouldGenerateModernUnits() {
            Earth2026BattleEnvironment env = createEnvironment("URBAN", 5);
            BiosphereContext biosphere = BiosphereContext.builder().build();

            when(planetService.buildBattleEnvironment(anyLong(), anyString(), anyString(), anyString()))
                    .thenReturn(env);
            when(biosphereService.createDefaultBiosphereContext(any()))
                    .thenReturn(biosphere);

            BattlefieldMap map = builder.buildBattlefieldMap(1L, "Modern", "URBAN",
                    Earth2026BattleEnvironment.WeatherCondition.CLEAR);

            builder.autoPopulateForces(map, 5, 0);

            // Modern era should have mechanized and recon
            for (UnitPlacement p : map.getAttackerPlacements()) {
                ThirdReichUnitType type = p.getUnit().getUnitType();
                assertTrue(type == ThirdReichUnitType.INFANTRY ||
                           type == ThirdReichUnitType.MECHANIZED ||
                           type == ThirdReichUnitType.TANK ||
                           type == ThirdReichUnitType.ARTILLERY ||
                           type == ThirdReichUnitType.RECON);
            }
        }

        @Test
        @DisplayName("should handle zero units")
        void shouldHandleZeroUnits() {
            Earth2026BattleEnvironment env = createEnvironment("URBAN", 4);
            BiosphereContext biosphere = BiosphereContext.builder().build();

            when(planetService.buildBattleEnvironment(anyLong(), anyString(), anyString(), anyString()))
                    .thenReturn(env);
            when(biosphereService.createDefaultBiosphereContext(any()))
                    .thenReturn(biosphere);

            BattlefieldMap map = builder.buildBattlefieldMap(1L, "World War II", "URBAN",
                    Earth2026BattleEnvironment.WeatherCondition.CLEAR);

            builder.autoPopulateForces(map, 0, 0);

            assertEquals(0, map.getAttackerUnitCount());
            assertEquals(0, map.getDefenderUnitCount());
        }
    }

    @Nested
    @DisplayName("customizeForce")
    class CustomizeForceTests {

        @Test
        @DisplayName("should replace unit at index")
        void shouldReplaceUnit() {
            Earth2026BattleEnvironment env = createEnvironment("URBAN", 4);
            BiosphereContext biosphere = BiosphereContext.builder().build();

            when(planetService.buildBattleEnvironment(anyLong(), anyString(), anyString(), anyString()))
                    .thenReturn(env);
            when(biosphereService.createDefaultBiosphereContext(any()))
                    .thenReturn(biosphere);

            BattlefieldMap map = builder.buildBattlefieldMap(1L, "World War II", "URBAN",
                    Earth2026BattleEnvironment.WeatherCondition.CLEAR);
            builder.autoPopulateForces(map, 3, 0);

            ThirdReichGroundUnit newUnit = new ThirdReichGroundUnit("custom-1", "Custom Tank", ThirdReichUnitType.TANK);
            builder.customizeForce(map, true, 0, newUnit);

            assertEquals("custom-1", map.getAttackerPlacements().get(0).getUnit().getId());
            assertEquals(ThirdReichUnitType.TANK, map.getAttackerPlacements().get(0).getUnit().getUnitType());
        }

        @Test
        @DisplayName("should throw for out of bounds index")
        void shouldThrowForOutOfBounds() {
            Earth2026BattleEnvironment env = createEnvironment("URBAN", 4);
            BiosphereContext biosphere = BiosphereContext.builder().build();

            when(planetService.buildBattleEnvironment(anyLong(), anyString(), anyString(), anyString()))
                    .thenReturn(env);
            when(biosphereService.createDefaultBiosphereContext(any()))
                    .thenReturn(biosphere);

            BattlefieldMap map = builder.buildBattlefieldMap(1L, "World War II", "URBAN",
                    Earth2026BattleEnvironment.WeatherCondition.CLEAR);
            builder.autoPopulateForces(map, 2, 0);

            ThirdReichGroundUnit newUnit = new ThirdReichGroundUnit("custom-1", "Custom", ThirdReichUnitType.TANK);
            assertThrows(IndexOutOfBoundsException.class, () ->
                    builder.customizeForce(map, true, 5, newUnit));
        }
    }

    @Nested
    @DisplayName("repositionUnit")
    class RepositionUnitTests {

        @Test
        @DisplayName("should reposition unit")
        void shouldRepositionUnit() {
            Earth2026BattleEnvironment env = createEnvironment("URBAN", 4);
            BiosphereContext biosphere = BiosphereContext.builder().build();

            when(planetService.buildBattleEnvironment(anyLong(), anyString(), anyString(), anyString()))
                    .thenReturn(env);
            when(biosphereService.createDefaultBiosphereContext(any()))
                    .thenReturn(biosphere);

            BattlefieldMap map = builder.buildBattlefieldMap(1L, "World War II", "URBAN",
                    Earth2026BattleEnvironment.WeatherCondition.CLEAR);
            builder.autoPopulateForces(map, 3, 0);

            builder.repositionUnit(map, true, 0, 50.0, 50.0);

            UnitPlacement p = map.getAttackerPlacements().get(0);
            assertEquals(50.0, p.getX(), 0.01);
            assertEquals(50.0, p.getY(), 0.01);
        }

        @Test
        @DisplayName("should throw for out of bounds index")
        void shouldThrowForOutOfBounds() {
            Earth2026BattleEnvironment env = createEnvironment("URBAN", 4);
            BiosphereContext biosphere = BiosphereContext.builder().build();

            when(planetService.buildBattleEnvironment(anyLong(), anyString(), anyString(), anyString()))
                    .thenReturn(env);
            when(biosphereService.createDefaultBiosphereContext(any()))
                    .thenReturn(biosphere);

            BattlefieldMap map = builder.buildBattlefieldMap(1L, "World War II", "URBAN",
                    Earth2026BattleEnvironment.WeatherCondition.CLEAR);
            builder.autoPopulateForces(map, 2, 0);

            assertThrows(IndexOutOfBoundsException.class, () ->
                    builder.repositionUnit(map, true, 5, 100.0, 100.0));
        }
    }

    @Nested
    @DisplayName("validateTechLevel")
    class ValidateTechLevelTests {

        @Test
        @DisplayName("should return true for valid map")
        void shouldReturnTrue() {
            Earth2026BattleEnvironment env = createEnvironment("URBAN", 4);
            BiosphereContext biosphere = BiosphereContext.builder().build();

            when(planetService.buildBattleEnvironment(anyLong(), anyString(), anyString(), anyString()))
                    .thenReturn(env);
            when(biosphereService.createDefaultBiosphereContext(any()))
                    .thenReturn(biosphere);

            BattlefieldMap map = builder.buildBattlefieldMap(1L, "World War II", "URBAN",
                    Earth2026BattleEnvironment.WeatherCondition.CLEAR);
            builder.autoPopulateForces(map, 3, 3);

            assertTrue(builder.validateTechLevel(map));
        }
    }
}
