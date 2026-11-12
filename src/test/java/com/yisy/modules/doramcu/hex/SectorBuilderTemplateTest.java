package com.yisy.modules.doramcu.hex;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("SectorBuilder Template Features Tests")
class SectorBuilderTemplateTest {

    @Nested
    @DisplayName("Naming convention constants")
    class NamingConstants {

        @Test
        @DisplayName("Greek naming constant")
        void greek() {
            assertEquals("astronomical", SectorBuilder.NAMING_GREEK);
        }

        @Test
        @DisplayName("Roman naming constant")
        void roman() {
            assertEquals("roman", SectorBuilder.NAMING_ROMAN);
        }

        @Test
        @DisplayName("Numerical naming constant")
        void numerical() {
            assertEquals("numerical", SectorBuilder.NAMING_NUMERICAL);
        }

        @Test
        @DisplayName("Mythological naming constant")
        void mythological() {
            assertEquals("mythological", SectorBuilder.NAMING_MYTHOLOGICAL);
        }
    }

    @Nested
    @DisplayName("generateSectorName (default)")
    class DefaultNaming {

        @Test
        @DisplayName("Position 0,0 returns Alpha-Prime")
        void position00() {
            assertEquals("Alpha-Prime", SectorBuilder.generateSectorName(0, 0));
        }

        @Test
        @DisplayName("Position 1,0 returns Beta-Prime")
        void position10() {
            assertEquals("Beta-Prime", SectorBuilder.generateSectorName(1, 0));
        }

        @Test
        @DisplayName("Position 0,1 returns Alpha-Secundus")
        void position01() {
            assertEquals("Alpha-Secundus", SectorBuilder.generateSectorName(0, 1));
        }

        @Test
        @DisplayName("Position 9,9 returns Kappa-Decimus")
        void position99() {
            assertEquals("Kappa-Decimus", SectorBuilder.generateSectorName(9, 9));
        }

        @Test
        @DisplayName("Throws on negative row")
        void throwsNegativeRow() {
            assertThrows(IllegalArgumentException.class, () -> SectorBuilder.generateSectorName(-1, 0));
        }

        @Test
        @DisplayName("Throws on out of range row")
        void throwsOutOfRangeRow() {
            assertThrows(IllegalArgumentException.class, () -> SectorBuilder.generateSectorName(10, 0));
        }
    }

    @Nested
    @DisplayName("generateSectorName with naming convention")
    class NamedConvention {

        @Test
        @DisplayName("Greek naming at 0,0")
        void greek00() {
            assertEquals("Alpha-Prime", SectorBuilder.generateSectorName(0, 0, 10, "astronomical"));
        }

        @Test
        @DisplayName("Roman naming at 0,0")
        void roman00() {
            assertEquals("I-Primus", SectorBuilder.generateSectorName(0, 0, 10, "roman"));
        }

        @Test
        @DisplayName("Roman naming at 1,0")
        void roman10() {
            assertEquals("II-Primus", SectorBuilder.generateSectorName(1, 0, 10, "roman"));
        }

        @Test
        @DisplayName("Numerical naming at 0,0")
        void numerical00() {
            assertEquals("Sector-0000", SectorBuilder.generateSectorName(0, 0, 10, "numerical"));
        }

        @Test
        @DisplayName("Numerical naming at 5,3")
        void numerical53() {
            assertEquals("Sector-0503", SectorBuilder.generateSectorName(5, 3, 10, "numerical"));
        }

        @Test
        @DisplayName("Mythological naming at 0,0")
        void mythological00() {
            assertEquals("Ares-Sanctum", SectorBuilder.generateSectorName(0, 0, 10, "mythological"));
        }

        @Test
        @DisplayName("Mythological naming at 1,0")
        void mythological10() {
            assertEquals("Athena-Sanctum", SectorBuilder.generateSectorName(1, 0, 10, "mythological"));
        }

        @Test
        @DisplayName("Null convention defaults to Greek")
        void nullDefaultsToGreek() {
            assertEquals("Alpha-Prime", SectorBuilder.generateSectorName(0, 0, 10, null));
        }

        @Test
        @DisplayName("Unknown convention defaults to Greek")
        void unknownDefaultsToGreek() {
            assertEquals("Alpha-Prime", SectorBuilder.generateSectorName(0, 0, 10, "unknown"));
        }

        @Test
        @DisplayName("Case insensitive convention")
        void caseInsensitive() {
            assertEquals("I-Primus", SectorBuilder.generateSectorName(0, 0, 10, "ROMAN"));
        }
    }

    @Nested
    @DisplayName("generateSectorName with larger grid sizes")
    class LargerGridSizes {

        @Test
        @DisplayName("12x12 grid with Greek naming")
        void greek12x12() {
            // Should not throw for grid size 12
            String name = SectorBuilder.generateSectorName(11, 11, 12, "astronomical");
            assertNotNull(name);
            assertTrue(name.contains("-"));
        }

        @Test
        @DisplayName("8x8 grid with Roman naming")
        void roman8x8() {
            String name = SectorBuilder.generateSectorName(7, 7, 8, "roman");
            assertNotNull(name);
            assertTrue(name.contains("-"));
        }

        @Test
        @DisplayName("14x14 grid with mythological naming")
        void mythological14x14() {
            String name = SectorBuilder.generateSectorName(13, 13, 14, "mythological");
            assertNotNull(name);
            assertTrue(name.contains("-"));
        }

        @Test
        @DisplayName("Throws on out of range for custom grid size")
        void throwsOutOfRange() {
            assertThrows(IllegalArgumentException.class,
                    () -> SectorBuilder.generateSectorName(12, 0, 12, "astronomical"));
        }
    }

    @Nested
    @DisplayName("generateTradeType (default)")
    class DefaultTradeType {

        @Test
        @DisplayName("Position 0,0 returns Agricultural")
        void position00() {
            assertEquals("Agricultural", SectorBuilder.generateTradeType(0, 0));
        }

        @Test
        @DisplayName("Position 0,1 returns Industrial")
        void position01() {
            assertEquals("Industrial", SectorBuilder.generateTradeType(0, 1));
        }

        @Test
        @DisplayName("Returns valid trade type for all positions")
        void allPositions() {
            for (int row = 0; row < 10; row++) {
                for (int col = 0; col < 10; col++) {
                    String tradeType = SectorBuilder.generateTradeType(row, col);
                    assertNotNull(tradeType);
                    assertFalse(tradeType.isEmpty());
                }
            }
        }
    }

    @Nested
    @DisplayName("generateTradeType with custom trade types")
    class CustomTradeTypes {

        @Test
        @DisplayName("Custom trade type array")
        void customArray() {
            String[] custom = {"Mining", "Trade", "Military"};
            String tradeType = SectorBuilder.generateTradeType(0, 0, 10, custom);
            assertEquals("Mining", tradeType);
        }

        @Test
        @DisplayName("Null trade types defaults to standard")
        void nullDefaults() {
            String tradeType = SectorBuilder.generateTradeType(0, 0, 10, null);
            assertNotNull(tradeType);
        }

        @Test
        @DisplayName("Empty trade types defaults to standard")
        void emptyDefaults() {
            String tradeType = SectorBuilder.generateTradeType(0, 0, 10, new String[]{});
            assertNotNull(tradeType);
        }

        @Test
        @DisplayName("Random trade type selection")
        void randomSelection() {
            String[] custom = {"Alpha", "Beta", "Gamma"};
            String tradeType = SectorBuilder.generateTradeType(0, 0, 10, custom, new Random(42L));
            assertTrue(java.util.Arrays.asList(custom).contains(tradeType));
        }

        @Test
        @DisplayName("Position-based with random")
        void positionBasedWithRandom() {
            String[] custom = {"A", "B", "C"};
            String tradeType = SectorBuilder.generateTradeType(0, 0, 10, custom, new Random(42L));
            assertTrue(java.util.Arrays.asList(custom).contains(tradeType));
        }

        @Test
        @DisplayName("Throws on negative row")
        void throwsNegativeRow() {
            assertThrows(IllegalArgumentException.class,
                    () -> SectorBuilder.generateTradeType(-1, 0, 10, SectorBuilder.TRADE_TYPES));
        }

        @Test
        @DisplayName("Throws on out of range row")
        void throwsOutOfRangeRow() {
            assertThrows(IllegalArgumentException.class,
                    () -> SectorBuilder.generateTradeType(10, 0, 10, SectorBuilder.TRADE_TYPES));
        }
    }

    @Nested
    @DisplayName("Trade type constants")
    class TradeTypeConstants {

        @Test
        @DisplayName("Has 20 trade types")
        void count() {
            assertEquals(20, SectorBuilder.TRADE_TYPES.length);
        }

        @Test
        @DisplayName("Contains expected types")
        void containsExpected() {
            assertTrue(java.util.Arrays.asList(SectorBuilder.TRADE_TYPES).contains("Agricultural"));
            assertTrue(java.util.Arrays.asList(SectorBuilder.TRADE_TYPES).contains("Industrial"));
            assertTrue(java.util.Arrays.asList(SectorBuilder.TRADE_TYPES).contains("High Technology"));
            assertTrue(java.util.Arrays.asList(SectorBuilder.TRADE_TYPES).contains("Hellworld"));
        }
    }
}
