package com.yisy.modules.doramcu.earth2026.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("TerrainElevationMap Tests")
class TerrainElevationMapTest {

    @Nested
    @DisplayName("Constructor")
    class ConstructorTests {

        @Test
        @DisplayName("should create valid elevation map")
        void shouldCreateValidMap() {
            TerrainElevationMap map = new TerrainElevationMap(10, 10, 10.0, 100.0);

            assertEquals(10, map.getWidth());
            assertEquals(10, map.getHeight());
            assertEquals(10.0, map.getCellSizeMeters(), 0.01);
            assertEquals(100.0, map.getBaseElevation(), 0.01);
        }

        @Test
        @DisplayName("should throw for zero width")
        void shouldThrowForZeroWidth() {
            assertThrows(IllegalArgumentException.class, () ->
                    new TerrainElevationMap(0, 10, 10.0, 100.0));
        }

        @Test
        @DisplayName("should throw for zero height")
        void shouldThrowForZeroHeight() {
            assertThrows(IllegalArgumentException.class, () ->
                    new TerrainElevationMap(10, 0, 10.0, 100.0));
        }

        @Test
        @DisplayName("should throw for negative cell size")
        void shouldThrowForNegativeCellSize() {
            assertThrows(IllegalArgumentException.class, () ->
                    new TerrainElevationMap(10, 10, -1.0, 100.0));
        }
    }

    @Nested
    @DisplayName("Elevation Access")
    class ElevationAccessTests {

        @Test
        @DisplayName("should get and set elevation")
        void shouldGetAndSetElevation() {
            TerrainElevationMap map = new TerrainElevationMap(5, 5, 10.0, 100.0);
            map.setElevation(2, 2, 15.0);

            assertEquals(15.0, map.getElevation(2, 2), 0.01);
            assertEquals(115.0, map.getAbsoluteElevation(2, 2), 0.01);
        }

        @Test
        @DisplayName("should default elevation to 0")
        void shouldDefaultElevationToZero() {
            TerrainElevationMap map = new TerrainElevationMap(5, 5, 10.0, 100.0);
            assertEquals(0.0, map.getElevation(0, 0), 0.01);
            assertEquals(100.0, map.getAbsoluteElevation(0, 0), 0.01);
        }
    }

    @Nested
    @DisplayName("Vegetation and Water")
    class VegetationAndWaterTests {

        @Test
        @DisplayName("should get and set vegetation density")
        void shouldGetAndSetVegetation() {
            TerrainElevationMap map = new TerrainElevationMap(5, 5, 10.0, 100.0);
            map.setVegetationDensity(2, 2, 0.7);

            assertEquals(0.7, map.getVegetationDensity(2, 2), 0.01);
        }

        @Test
        @DisplayName("should clamp vegetation density")
        void shouldClampVegetationDensity() {
            TerrainElevationMap map = new TerrainElevationMap(5, 5, 10.0, 100.0);
            map.setVegetationDensity(0, 0, -0.5);
            map.setVegetationDensity(1, 1, 1.5);

            assertEquals(0.0, map.getVegetationDensity(0, 0), 0.01);
            assertEquals(1.0, map.getVegetationDensity(1, 1), 0.01);
        }

        @Test
        @DisplayName("should get and set water mask")
        void shouldGetAndSetWater() {
            TerrainElevationMap map = new TerrainElevationMap(5, 5, 10.0, 100.0);
            map.setWater(2, 2, true);

            assertTrue(map.isWater(2, 2));
            assertFalse(map.isWater(0, 0));
        }
    }

    @Nested
    @DisplayName("Slope Calculation")
    class SlopeCalculationTests {

        @Test
        @DisplayName("should return zero slope for flat terrain")
        void shouldReturnZeroSlopeForFlat() {
            TerrainElevationMap map = new TerrainElevationMap(5, 5, 10.0, 100.0);
            // All elevations are 0 by default
            assertEquals(0.0, map.getSlopeAngle(2, 2), 0.01);
        }

        @Test
        @DisplayName("should calculate slope for elevation change")
        void shouldCalculateSlope() {
            TerrainElevationMap map = new TerrainElevationMap(5, 5, 10.0, 100.0);
            // Central difference: dy = elevations[row+1][col] - elevations[row-1][col]
            // Set asymmetric values so slope is non-zero
            map.setElevation(1, 2, 0.0);
            map.setElevation(2, 2, 0.0);
            map.setElevation(3, 2, 20.0);

            double slope = map.getSlopeAngle(2, 2);
            assertTrue(slope > 0, "Slope should be positive: " + slope);
            assertTrue(slope < 90, "Slope should be less than 90: " + slope);
        }
    }

    @Nested
    @DisplayName("Interpolation")
    class InterpolationTests {

        @Test
        @DisplayName("should interpolate elevation at world coordinates")
        void shouldInterpolateElevation() {
            TerrainElevationMap map = new TerrainElevationMap(5, 5, 10.0, 100.0);
            map.setElevation(0, 0, 0.0);
            map.setElevation(0, 1, 10.0);
            map.setElevation(1, 0, 0.0);
            map.setElevation(1, 1, 10.0);

            // At exact grid point (0,0) should return 0
            double elev = map.getElevationAtWorldCoords(0.0, 0.0);
            assertEquals(0.0, elev, 0.01);

            // At exact grid point (0,1) should return 10
            double elev2 = map.getElevationAtWorldCoords(10.0, 0.0);
            assertEquals(10.0, elev2, 0.01);
        }

        @Test
        @DisplayName("should handle edge coordinates")
        void shouldHandleEdgeCoordinates() {
            TerrainElevationMap map = new TerrainElevationMap(5, 5, 10.0, 100.0);
            map.setElevation(4, 4, 20.0);

            // Should not throw for coordinates at edge
            double elev = map.getElevationAtWorldCoords(45.0, 45.0);
            assertTrue(elev >= 0);
        }
    }

    @Nested
    @DisplayName("Factory Methods")
    class FactoryMethodTests {

        @Test
        @DisplayName("should generate random terrain for URBAN")
        void shouldGenerateUrbanTerrain() {
            TerrainElevationMap map = TerrainElevationMap.generateRandom(
                    10, 10, 10.0, 100.0, "URBAN", 0.3);

            assertNotNull(map);
            assertEquals(10, map.getWidth());
            assertEquals(10, map.getHeight());
        }

        @Test
        @DisplayName("should generate random terrain for MOUNTAIN")
        void shouldGenerateMountainTerrain() {
            TerrainElevationMap map = TerrainElevationMap.generateRandom(
                    10, 10, 10.0, 500.0, "MOUNTAIN", 0.2);

            // Mountain terrain should have high elevation variation
            double maxElev = 0;
            for (int r = 0; r < map.getHeight(); r++) {
                for (int c = 0; c < map.getWidth(); c++) {
                    maxElev = Math.max(maxElev, Math.abs(map.getElevation(r, c)));
                }
            }
            assertTrue(maxElev > 10.0);
        }

        @Test
        @DisplayName("should generate random terrain for DESERT")
        void shouldGenerateDesertTerrain() {
            TerrainElevationMap map = TerrainElevationMap.generateRandom(
                    10, 10, 10.0, 200.0, "DESERT", 0.1);

            // Desert should have very low vegetation (clamped to 0 by the generator)
            double avgVegetation = 0;
            for (int r = 0; r < map.getHeight(); r++) {
                for (int c = 0; c < map.getWidth(); c++) {
                    avgVegetation += map.getVegetationDensity(r, c);
                }
            }
            avgVegetation /= (map.getWidth() * map.getHeight());
            assertTrue(avgVegetation < 0.1, "Desert avg vegetation should be very low: " + avgVegetation);
        }

        @Test
        @DisplayName("should generate random terrain for JUNGLE")
        void shouldGenerateJungleTerrain() {
            TerrainElevationMap map = TerrainElevationMap.generateRandom(
                    10, 10, 10.0, 50.0, "JUNGLE", 0.3);

            // Jungle should have high average vegetation (min 0.7 set by generator)
            double avgVegetation = 0;
            for (int r = 0; r < map.getHeight(); r++) {
                for (int c = 0; c < map.getWidth(); c++) {
                    avgVegetation += map.getVegetationDensity(r, c);
                }
            }
            avgVegetation /= (map.getWidth() * map.getHeight());
            assertTrue(avgVegetation >= 0.5, "Jungle avg vegetation should be high: " + avgVegetation);
        }

        @Test
        @DisplayName("should generate random terrain for ARCTIC")
        void shouldGenerateArcticTerrain() {
            TerrainElevationMap map = TerrainElevationMap.generateRandom(
                    10, 10, 10.0, 0.0, "ARCTIC", 0.1);

            // Arctic should have very low average vegetation (clamped to 0 by generator)
            double avgVegetation = 0;
            for (int r = 0; r < map.getHeight(); r++) {
                for (int c = 0; c < map.getWidth(); c++) {
                    avgVegetation += map.getVegetationDensity(r, c);
                }
            }
            avgVegetation /= (map.getWidth() * map.getHeight());
            assertTrue(avgVegetation < 0.1, "Arctic avg vegetation should be very low: " + avgVegetation);
        }

        @Test
        @DisplayName("should create flat terrain")
        void shouldCreateFlatTerrain() {
            TerrainElevationMap map = TerrainElevationMap.createFlat(10, 10, 10.0, 100.0);

            assertNotNull(map);
            assertEquals(10, map.getWidth());
        }

        @Test
        @DisplayName("should handle unknown terrain type")
        void shouldHandleUnknownTerrainType() {
            TerrainElevationMap map = TerrainElevationMap.generateRandom(
                    10, 10, 10.0, 100.0, "UNKNOWN_TYPE", 0.3);

            assertNotNull(map);
        }
    }

    @Nested
    @DisplayName("toString")
    class ToStringTests {

        @Test
        @DisplayName("should include dimensions and base elevation")
        void shouldIncludeDimensions() {
            TerrainElevationMap map = new TerrainElevationMap(10, 20, 10.0, 100.0);
            String str = map.toString();

            assertTrue(str.contains("width=10"));
            assertTrue(str.contains("height=20"));
            assertTrue(str.contains("baseElevation=100.0"));
        }
    }
}
