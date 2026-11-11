package com.yisy.modules.doramcu.star_trek_4x.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("StarSystem Tests")
class StarSystemTest {

    @Nested
    @DisplayName("Constructor")
    class Constructor {

        @Test
        @DisplayName("Default constructor initializes defaults")
        void defaults() {
            StarSystem system = new StarSystem();
            assertNull(system.getId());
            assertNull(system.getName());
            assertEquals(0, system.getSectorId());
            assertEquals(0, system.getSubSectorId());
            assertEquals(0, system.getPosX());
            assertEquals(0, system.getPosY());
            assertEquals(0, system.getPosZ());
            assertNull(system.getUwpAtmosphere());
            assertNull(system.getUwpGravity());
            assertNull(system.getUwpSize());
            assertNull(system.getUwpHydrosphere());
            assertNull(system.getUwpPopulation());
            assertNull(system.getUwpGovernment());
            assertNull(system.getUwpLaw());
            assertNull(system.getUwpTrade());
            assertEquals(StarSystem.StarAlignment.UNKNOWN, system.getAlignment());
            assertTrue(system.getNativeSpecies().isEmpty());
            assertFalse(system.isHasWarpDeposits());
            assertNull(system.getNotableFeatures());
            assertEquals(0, system.getTradeValue());
            assertNull(system.getEarth2026PlanetId());
            assertFalse(system.isDiscovered());
            assertNull(system.getOwnerFactionId());
            assertNull(system.getGameSession());
        }
    }

    @Nested
    @DisplayName("UWP code")
    class UwpCode {

        @Test
        @DisplayName("Get UWP code with all values set")
        void fullUwpCode() {
            StarSystem system = new StarSystem();
            system.setUwpAtmosphere("A");
            system.setUwpGravity("0");
            system.setUwpSize("8");
            system.setUwpHydrosphere("5");
            system.setUwpPopulation("6");
            system.setUwpGovernment("6");
            system.setUwpLaw("5");
            system.setUwpTrade("Ag");
            assertEquals("A085665Ag", system.getUwpCode());
        }

        @Test
        @DisplayName("Get UWP code with null values defaults to 0")
        void nullUwpCode() {
            StarSystem system = new StarSystem();
            assertEquals("00000000", system.getUwpCode());
        }

        @Test
        @DisplayName("Get UWP code with partial values")
        void partialUwpCode() {
            StarSystem system = new StarSystem();
            system.setUwpAtmosphere("B");
            system.setUwpGravity("3");
            assertEquals("B3000000", system.getUwpCode());
        }
    }

    @Nested
    @DisplayName("StarAlignment enum")
    class StarAlignmentEnum {

        @Test
        @DisplayName("All alignment values")
        void allAlignments() {
            assertSame(StarSystem.StarAlignment.FEDERATION, StarSystem.StarAlignment.valueOf("FEDERATION"));
            assertSame(StarSystem.StarAlignment.KLINGON, StarSystem.StarAlignment.valueOf("KLINGON"));
            assertSame(StarSystem.StarAlignment.ROMULAN, StarSystem.StarAlignment.valueOf("ROMULAN"));
            assertSame(StarSystem.StarAlignment.CARDASSIAN, StarSystem.StarAlignment.valueOf("CARDASSIAN"));
            assertSame(StarSystem.StarAlignment.FERENGI, StarSystem.StarAlignment.valueOf("FERENGI"));
            assertSame(StarSystem.StarAlignment.BORG, StarSystem.StarAlignment.valueOf("BORG"));
            assertSame(StarSystem.StarAlignment.NEUTRAL, StarSystem.StarAlignment.valueOf("NEUTRAL"));
            assertSame(StarSystem.StarAlignment.HOSTILE, StarSystem.StarAlignment.valueOf("HOSTILE"));
            assertSame(StarSystem.StarAlignment.UNKNOWN, StarSystem.StarAlignment.valueOf("UNKNOWN"));
        }

        @Test
        @DisplayName("Set alignment")
        void setAlignment() {
            StarSystem system = new StarSystem();
            system.setAlignment(StarSystem.StarAlignment.FEDERATION);
            assertEquals(StarSystem.StarAlignment.FEDERATION, system.getAlignment());
        }
    }

    @Nested
    @DisplayName("Native species")
    class NativeSpecies {

        @Test
        @DisplayName("Add native species")
        void addSpecies() {
            StarSystem system = new StarSystem();
            system.getNativeSpecies().add("Human");
            system.getNativeSpecies().add("Vulcan");
            assertEquals(2, system.getNativeSpecies().size());
            assertTrue(system.getNativeSpecies().contains("Human"));
            assertTrue(system.getNativeSpecies().contains("Vulcan"));
        }
    }

    @Nested
    @DisplayName("Discovery status")
    class DiscoveryStatus {

        @Test
        @DisplayName("Default is not discovered")
        void defaultNotDiscovered() {
            StarSystem system = new StarSystem();
            assertFalse(system.isDiscovered());
        }

        @Test
        @DisplayName("Set discovered")
        void setDiscovered() {
            StarSystem system = new StarSystem();
            system.setDiscovered(true);
            assertTrue(system.isDiscovered());
        }
    }

    @Nested
    @DisplayName("Warp deposits")
    class WarpDeposits {

        @Test
        @DisplayName("Default has no warp deposits")
        void defaultNoWarpDeposits() {
            StarSystem system = new StarSystem();
            assertFalse(system.isHasWarpDeposits());
        }

        @Test
        @DisplayName("Set warp deposits")
        void setWarpDeposits() {
            StarSystem system = new StarSystem();
            system.setHasWarpDeposits(true);
            assertTrue(system.isHasWarpDeposits());
        }
    }

    @Nested
    @DisplayName("Position")
    class Position {

        @Test
        @DisplayName("Set position coordinates")
        void setPosition() {
            StarSystem system = new StarSystem();
            system.setPosX(10);
            system.setPosY(20);
            system.setPosZ(30);
            assertEquals(10, system.getPosX());
            assertEquals(20, system.getPosY());
            assertEquals(30, system.getPosZ());
        }
    }
}
