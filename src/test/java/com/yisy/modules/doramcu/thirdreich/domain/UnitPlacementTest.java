package com.yisy.modules.doramcu.thirdreich.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("UnitPlacement Tests")
class UnitPlacementTest {

    private ThirdReichGroundUnit createUnit(String id, String name) {
        return new ThirdReichGroundUnit(id, name, ThirdReichUnitType.INFANTRY);
    }

    @Nested
    @DisplayName("Constructor")
    class ConstructorTests {

        @Test
        @DisplayName("should create attacker placement")
        void shouldCreateAttackerPlacement() {
            ThirdReichGroundUnit unit = createUnit("u1", "Infantry 1");
            UnitPlacement p = new UnitPlacement(unit, 10.0, 20.0, 5.0, 90.0, true);

            assertEquals(unit, p.getUnit());
            assertEquals(10.0, p.getX(), 0.01);
            assertEquals(20.0, p.getY(), 0.01);
            assertEquals(5.0, p.getZ(), 0.01);
            assertEquals(90.0, p.getFacingDirection(), 0.01);
            assertTrue(p.isAttacker());
            assertEquals("attacker", p.getTeamName());
        }

        @Test
        @DisplayName("should create defender placement")
        void shouldCreateDefenderPlacement() {
            ThirdReichGroundUnit unit = createUnit("u1", "Infantry 1");
            UnitPlacement p = new UnitPlacement(unit, 400.0, 20.0, 5.0, 270.0, false);

            assertFalse(p.isAttacker());
            assertEquals("defender", p.getTeamName());
        }

        @Test
        @DisplayName("should throw for null unit")
        void shouldThrowForNullUnit() {
            assertThrows(IllegalArgumentException.class, () ->
                    new UnitPlacement(null, 0, 0, 0, 0, true));
        }

        @Test
        @DisplayName("should normalize negative facing direction")
        void shouldNormalizeNegativeFacing() {
            ThirdReichGroundUnit unit = createUnit("u1", "Infantry 1");
            UnitPlacement p = new UnitPlacement(unit, 0, 0, 0, -90.0, true);

            assertEquals(270.0, p.getFacingDirection(), 0.01);
        }

        @Test
        @DisplayName("should normalize facing direction > 360")
        void shouldNormalizeFacingOver360() {
            ThirdReichGroundUnit unit = createUnit("u1", "Infantry 1");
            UnitPlacement p = new UnitPlacement(unit, 0, 0, 0, 450.0, true);

            assertEquals(90.0, p.getFacingDirection(), 0.01);
        }
    }

    @Nested
    @DisplayName("Factory Methods")
    class FactoryMethodTests {

        @Test
        @DisplayName("should create moved placement")
        void shouldCreateMovedPlacement() {
            ThirdReichGroundUnit unit = createUnit("u1", "Infantry 1");
            UnitPlacement p1 = new UnitPlacement(unit, 10.0, 10.0, 0.0, 90.0, true);
            UnitPlacement p2 = p1.createMoved(20.0, 30.0, 5.0);

            assertEquals(20.0, p2.getX(), 0.01);
            assertEquals(30.0, p2.getY(), 0.01);
            assertEquals(5.0, p2.getZ(), 0.01);
            assertEquals(90.0, p2.getFacingDirection(), 0.01);
            assertNotSame(p1, p2);
        }

        @Test
        @DisplayName("should create placement with new facing")
        void shouldCreateWithNewFacing() {
            ThirdReichGroundUnit unit = createUnit("u1", "Infantry 1");
            UnitPlacement p1 = new UnitPlacement(unit, 10.0, 10.0, 0.0, 90.0, true);
            UnitPlacement p2 = p1.createWithFacing(180.0);

            assertEquals(180.0, p2.getFacingDirection(), 0.01);
            assertEquals(10.0, p2.getX(), 0.01);
        }
    }

    @Nested
    @DisplayName("Distance Calculations")
    class DistanceCalculationTests {

        @Test
        @DisplayName("should calculate 3D distance")
        void shouldCalculate3DDistance() {
            ThirdReichGroundUnit u1 = createUnit("u1", "Unit 1");
            ThirdReichGroundUnit u2 = createUnit("u2", "Unit 2");
            UnitPlacement p1 = new UnitPlacement(u1, 0.0, 0.0, 0.0, 0.0, true);
            UnitPlacement p2 = new UnitPlacement(u2, 3.0, 4.0, 0.0, 0.0, false);

            assertEquals(5.0, p1.distanceTo(p2), 0.01);
        }

        @Test
        @DisplayName("should calculate horizontal distance")
        void shouldCalculateHorizontalDistance() {
            ThirdReichGroundUnit u1 = createUnit("u1", "Unit 1");
            ThirdReichGroundUnit u2 = createUnit("u2", "Unit 2");
            UnitPlacement p1 = new UnitPlacement(u1, 0.0, 0.0, 0.0, 0.0, true);
            UnitPlacement p2 = new UnitPlacement(u2, 3.0, 4.0, 10.0, 0.0, false);

            assertEquals(5.0, p1.horizontalDistanceTo(p2), 0.01);
            // 3D distance should be larger due to elevation difference
            assertTrue(p1.distanceTo(p2) > 5.0);
        }

        @Test
        @DisplayName("should return zero distance to self")
        void shouldReturnZeroDistanceToSelf() {
            ThirdReichGroundUnit u1 = createUnit("u1", "Unit 1");
            UnitPlacement p = new UnitPlacement(u1, 10.0, 10.0, 0.0, 90.0, true);

            assertEquals(0.0, p.distanceTo(p), 0.01);
        }
    }

    @Nested
    @DisplayName("toString")
    class ToStringTests {

        @Test
        @DisplayName("should include unit name and position")
        void shouldIncludeUnitAndPosition() {
            ThirdReichGroundUnit unit = createUnit("u1", "Infantry 1");
            UnitPlacement p = new UnitPlacement(unit, 10.5, 20.5, 5.5, 90.0, true);

            String str = p.toString();
            assertTrue(str.contains("Infantry 1"));
            assertTrue(str.contains("attacker"));
        }
    }
}
