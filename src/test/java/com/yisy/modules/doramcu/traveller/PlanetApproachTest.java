package com.yisy.modules.doramcu.traveller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PlanetApproach — Landing feasibility by planet size")
class PlanetApproachTest {

    private TravellerWorld createWorld(int planetSize) {
        String sizeChar = planetSize == 10 ? "A" : String.valueOf(planetSize);
        UniversalWorldProfile uwp = UniversalWorldProfile.parse(
                sizeChar + "4433221"); // size varies, rest is fixed
        return new TravellerWorld(1, "TestWorld", uwp, 0, 0, false, false);
    }

    @Nested
    @DisplayName("Landing should be blocked for non-rocky bodies")
    class LandingBlocked {

        @Test
        @DisplayName("Size 0 (Nil) — no landing")
        void size0Nil() {
            PlanetApproach approach = new PlanetApproach(createWorld(0));
            assertFalse(approach.isLandingPossible(), "Size 0 (Nil) should not allow landing");
        }

        @Test
        @DisplayName("Size 1 (Gas Giant) — no landing")
        void size1GasGiant() {
            PlanetApproach approach = new PlanetApproach(createWorld(1));
            assertFalse(approach.isLandingPossible(), "Size 1 (Gas Giant) should not allow landing");
        }

        @Test
        @DisplayName("Size 7 (Sub-brown dwarf) — no landing")
        void size7SubBrownDwarf() {
            PlanetApproach approach = new PlanetApproach(createWorld(7));
            assertFalse(approach.isLandingPossible(), "Size 7 (Sub-brown dwarf) should not allow landing");
        }

        @Test
        @DisplayName("Size 8 (Brown dwarf) — no landing")
        void size8BrownDwarf() {
            PlanetApproach approach = new PlanetApproach(createWorld(8));
            assertFalse(approach.isLandingPossible(), "Size 8 (Brown dwarf) should not allow landing");
        }

        @Test
        @DisplayName("Size 9 (Star) — no landing")
        void size9Star() {
            PlanetApproach approach = new PlanetApproach(createWorld(9));
            assertFalse(approach.isLandingPossible(), "Size 9 (Star) should not allow landing");
        }

        @Test
        @DisplayName("Size A (10, giant gas planet) — no landing")
        void sizeAGiantGasPlanet() {
            PlanetApproach approach = new PlanetApproach(createWorld(10));
            assertFalse(approach.isLandingPossible(), "Size A (giant gas planet) should not allow landing");
        }
    }

    @Nested
    @DisplayName("Landing should be allowed for rocky worlds (size 2-6)")
    class LandingAllowed {

        @ParameterizedTest(name = "Size {0} — landing allowed")
        @ValueSource(ints = {2, 3, 4, 5, 6})
        @DisplayName("Rocky worlds should allow landing")
        void rockyWorldsAllowLanding(int size) {
            PlanetApproach approach = new PlanetApproach(createWorld(size));
            assertTrue(approach.isLandingPossible(), "Size " + size + " (rocky world) should allow landing");
        }
    }
}
