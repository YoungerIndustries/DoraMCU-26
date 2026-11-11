package com.yisy.modules.doramcu.captainslog.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("StarTrekSpecies Tests")
class StarTrekSpeciesTest {

    @Nested
    @DisplayName("Enum values")
    class EnumValues {

        @Test
        @DisplayName("All 9 species values")
        void allSpecies() {
            assertSame(StarTrekSpecies.HUMAN, StarTrekSpecies.valueOf("HUMAN"));
            assertSame(StarTrekSpecies.VULCAN, StarTrekSpecies.valueOf("VULCAN"));
            assertSame(StarTrekSpecies.KLINGON, StarTrekSpecies.valueOf("KLINGON"));
            assertSame(StarTrekSpecies.ANDORIAN, StarTrekSpecies.valueOf("ANDORIAN"));
            assertSame(StarTrekSpecies.BETAZOID, StarTrekSpecies.valueOf("BETAZOID"));
            assertSame(StarTrekSpecies.DENOBUAN, StarTrekSpecies.valueOf("DENOBUAN"));
            assertSame(StarTrekSpecies.FERENGI, StarTrekSpecies.valueOf("FERENGI"));
            assertSame(StarTrekSpecies.TRILL, StarTrekSpecies.valueOf("TRILL"));
            assertSame(StarTrekSpecies.BAJORAN, StarTrekSpecies.valueOf("BAJORAN"));
        }
    }

    @Nested
    @DisplayName("Display names")
    class DisplayNames {

        @Test
        @DisplayName("Human display name")
        void human() {
            assertEquals("Human", StarTrekSpecies.HUMAN.getDisplayName());
        }

        @Test
        @DisplayName("Klingon display name")
        void klingon() {
            assertEquals("Klingon", StarTrekSpecies.KLINGON.getDisplayName());
        }

        @Test
        @DisplayName("toString returns display name")
        void toStringReturnsDisplayName() {
            assertEquals("Vulcan", StarTrekSpecies.VULCAN.toString());
        }
    }

    @Nested
    @DisplayName("Descriptions")
    class Descriptions {

        @Test
        @DisplayName("Human description")
        void human() {
            assertTrue(StarTrekSpecies.HUMAN.getDescription().contains("Earth"));
        }

        @Test
        @DisplayName("Klingon description")
        void klingon() {
            assertTrue(StarTrekSpecies.KLINGON.getDescription().contains("warriors"));
        }
    }

    @Nested
    @DisplayName("Combat DM")
    class CombatDM {

        @Test
        @DisplayName("Klingon has +1 combat DM")
        void klingon() {
            assertEquals(1, StarTrekSpecies.KLINGON.getCombatDM());
        }

        @Test
        @DisplayName("Andorian has +1 combat DM")
        void andorian() {
            assertEquals(1, StarTrekSpecies.ANDORIAN.getCombatDM());
        }

        @Test
        @DisplayName("Human has 0 combat DM")
        void human() {
            assertEquals(0, StarTrekSpecies.HUMAN.getCombatDM());
        }

        @Test
        @DisplayName("Ferengi has -1 combat DM")
        void ferengi() {
            assertEquals(-1, StarTrekSpecies.FERENGI.getCombatDM());
        }
    }

    @Nested
    @DisplayName("Social DM")
    class SocialDM {

        @Test
        @DisplayName("Betazoid has +2 social DM")
        void betazoid() {
            assertEquals(2, StarTrekSpecies.BETAZOID.getSocialDM());
        }

        @Test
        @DisplayName("Human has +1 social DM")
        void human() {
            assertEquals(1, StarTrekSpecies.HUMAN.getSocialDM());
        }

        @Test
        @DisplayName("Klingon has -1 social DM")
        void klingon() {
            assertEquals(-1, StarTrekSpecies.KLINGON.getSocialDM());
        }
    }

    @Nested
    @DisplayName("Navigation DM")
    class NavigationDM {

        @Test
        @DisplayName("Denobulan has +1 navigation DM")
        void denobulan() {
            assertEquals(1, StarTrekSpecies.DENOBUAN.getNavigationDM());
        }

        @Test
        @DisplayName("Trill has +1 navigation DM")
        void trill() {
            assertEquals(1, StarTrekSpecies.TRILL.getNavigationDM());
        }

        @Test
        @DisplayName("Human has 0 navigation DM")
        void human() {
            assertEquals(0, StarTrekSpecies.HUMAN.getNavigationDM());
        }
    }

    @Nested
    @DisplayName("Armor bonus")
    class ArmorBonus {

        @Test
        @DisplayName("Klingon has +1 armor bonus")
        void klingon() {
            assertEquals(1, StarTrekSpecies.KLINGON.getArmorBonus());
        }

        @Test
        @DisplayName("Human has 0 armor bonus")
        void human() {
            assertEquals(0, StarTrekSpecies.HUMAN.getArmorBonus());
        }
    }

    @Nested
    @DisplayName("Trade bonus")
    class TradeBonus {

        @Test
        @DisplayName("Ferengi has +3% trade bonus")
        void ferengi() {
            assertEquals(3, StarTrekSpecies.FERENGI.getTradeBonusPercent());
        }

        @Test
        @DisplayName("Human has 0% trade bonus")
        void human() {
            assertEquals(0, StarTrekSpecies.HUMAN.getTradeBonusPercent());
        }
    }
}
