package com.yisy.modules.doramcu.babylon5.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("B5WorldProfile Tests")
class B5WorldProfileTest {

    @Nested
    @DisplayName("Parse")
    class Parse {

        @Test
        @DisplayName("Parse valid 8-char profile string")
        void validProfile() {
            B5WorldProfile profile = B5WorldProfile.parse("44444300");
            assertEquals(4, profile.getPlanetSize());
            assertEquals(4, profile.getAtmosphere());
            assertEquals(4, profile.getGravity());
            assertEquals(4, profile.getStarport());
            assertEquals(4, profile.getPopulation());
            assertEquals(3, profile.getGovernment());
            assertEquals(0, profile.getLawLevel());
        }

        @Test
        @DisplayName("Parse profile with modifier")
        void withModifier() {
            B5WorldProfile profile = B5WorldProfile.parse("44444300-Vorlon");
            assertEquals("Vorlon", profile.getModifier());
        }

        @Test
        @DisplayName("Parse profile without modifier")
        void withoutModifier() {
            B5WorldProfile profile = B5WorldProfile.parse("34565432");
            assertNull(profile.getModifier());
        }

        @Test
        @DisplayName("Throws on null profile")
        void nullProfile() {
            assertThrows(NullPointerException.class, () -> B5WorldProfile.parse(null));
        }

        @Test
        @DisplayName("Throws on too short profile")
        void tooShort() {
            assertThrows(IllegalArgumentException.class, () -> B5WorldProfile.parse("1234567"));
        }

        @Test
        @DisplayName("Throws on invalid character")
        void invalidCharacter() {
            assertThrows(IllegalArgumentException.class, () -> B5WorldProfile.parse("Z5444300"));
        }

        @Test
        @DisplayName("Parse with lowercase A")
        void lowercaseA() {
            B5WorldProfile profile = B5WorldProfile.parse("A4444300");
            assertEquals(10, profile.getPlanetSize());
        }
    }

    @Nested
    @DisplayName("Constructor validation")
    class ConstructorValidation {

        @Test
        @DisplayName("Valid values")
        void validValues() {
            B5WorldProfile profile = new B5WorldProfile(5, 4, 4, 3, 5, 1, 5, null, "54435150");
            assertEquals(5, profile.getPlanetSize());
        }

        @Test
        @DisplayName("Throws on value > 10")
        void valueTooHigh() {
            assertThrows(IllegalArgumentException.class, () ->
                new B5WorldProfile(11, 4, 4, 3, 5, 1, 5, null, "invalid")
            );
        }

        @Test
        @DisplayName("Throws on negative value")
        void negativeValue() {
            assertThrows(IllegalArgumentException.class, () ->
                new B5WorldProfile(5, -1, 4, 3, 5, 1, 5, null, "invalid")
            );
        }
    }

    @Nested
    @DisplayName("Habitable checks")
    class HabitableChecks {

        @Test
        @DisplayName("Standard world is habitable")
        void standardWorld() {
            B5WorldProfile profile = new B5WorldProfile(4, 4, 4, 4, 4, 3, 0, null, "44444300");
            assertTrue(profile.isHabitable());
        }

        @Test
        @DisplayName("Vacuum world is not habitable")
        void vacuumWorld() {
            B5WorldProfile profile = new B5WorldProfile(4, 0, 4, 4, 4, 3, 0, null, "40444300");
            assertFalse(profile.isHabitable());
        }

        @Test
        @DisplayName("World with atmosphere 10 is habitable")
        void atmosphere10() {
            B5WorldProfile profile = new B5WorldProfile(4, 10, 4, 4, 4, 3, 0, null, "4A444300");
            assertTrue(profile.hasBreathableAtmosphere());
        }

        @Test
        @DisplayName("Vacuum detection")
        void vacuumDetection() {
            B5WorldProfile profile = new B5WorldProfile(4, 0, 4, 4, 4, 3, 0, null, "40444300");
            assertTrue(profile.isVacuum());
        }

        @Test
        @DisplayName("Starport detection")
        void starportDetection() {
            B5WorldProfile profile = new B5WorldProfile(4, 4, 4, 4, 4, 3, 0, null, "44444300");
            assertTrue(profile.hasStarport());

            B5WorldProfile noStarport = new B5WorldProfile(4, 4, 4, 5, 4, 3, 0, null, "44544300");
            assertFalse(noStarport.hasStarport());
        }
    }

    @Nested
    @DisplayName("Population")
    class Population {

        @Test
        @DisplayName("Population 0 returns 0 billion")
        void zeroPopulation() {
            B5WorldProfile profile = new B5WorldProfile(4, 4, 4, 4, 0, 3, 0, null, "44404300");
            assertEquals(0, profile.getPopulationBillions());
        }

        @Test
        @DisplayName("Population 4 returns 1 billion")
        void population4() {
            B5WorldProfile profile = new B5WorldProfile(4, 4, 4, 4, 4, 3, 0, null, "44444300");
            assertEquals(1, profile.getPopulationBillions());
        }

        @Test
        @DisplayName("Population 10 returns 200 billion")
        void population10() {
            B5WorldProfile profile = new B5WorldProfile(4, 4, 4, 4, 10, 3, 0, null, "444A4300");
            assertEquals(200, profile.getPopulationBillions());
        }
    }

    @Nested
    @DisplayName("Government types")
    class GovernmentTypes {

        @Test
        @DisplayName("Government 0 is None")
        void none() {
            B5WorldProfile profile = new B5WorldProfile(4, 4, 4, 4, 4, 0, 0, null, "44440000");
            assertEquals("None", profile.getGovernmentType());
        }

        @Test
        @DisplayName("Government 1 is Earth Alliance Democracy")
        void earthAlliance() {
            B5WorldProfile profile = new B5WorldProfile(4, 4, 4, 4, 4, 1, 0, null, "44441000");
            assertEquals("Earth Alliance Democracy", profile.getGovernmentType());
        }

        @Test
        @DisplayName("Government 2 is Narn Collective")
        void narnCollective() {
            B5WorldProfile profile = new B5WorldProfile(4, 4, 4, 4, 4, 2, 0, null, "44442000");
            assertEquals("Narn Collective", profile.getGovernmentType());
        }

        @Test
        @DisplayName("Government 3 is Centauri Empire")
        void centauriEmpire() {
            B5WorldProfile profile = new B5WorldProfile(4, 4, 4, 4, 4, 3, 0, null, "44443000");
            assertEquals("Centauri Empire", profile.getGovernmentType());
        }

        @Test
        @DisplayName("Government 4 is Minbari Theocracy")
        void minbariTheocracy() {
            B5WorldProfile profile = new B5WorldProfile(4, 4, 4, 4, 4, 4, 0, null, "44444000");
            assertEquals("Minbari Theocracy", profile.getGovernmentType());
        }

        @Test
        @DisplayName("Government 8 is Vorlon Ancient Order")
        void vorlonOrder() {
            B5WorldProfile profile = new B5WorldProfile(4, 4, 4, 4, 4, 8, 0, null, "44448000");
            assertEquals("Vorlon Ancient Order", profile.getGovernmentType());
        }

        @Test
        @DisplayName("Government 9 is Shadow Manipulation")
        void shadowManipulation() {
            B5WorldProfile profile = new B5WorldProfile(4, 4, 4, 4, 4, 9, 0, null, "44449000");
            assertEquals("Shadow Manipulation", profile.getGovernmentType());
        }
    }

    @Nested
    @DisplayName("Law level descriptions")
    class LawLevelDescriptions {

        @Test
        @DisplayName("Law 0 is Anarchy")
        void anarchy() {
            B5WorldProfile profile = new B5WorldProfile(4, 4, 4, 4, 4, 3, 0, null, "44443000");
            assertEquals("None (Anarchy)", profile.getLawLevelDescription());
        }

        @Test
        @DisplayName("Law 10 is Totalitarian")
        void totalitarian() {
            B5WorldProfile profile = new B5WorldProfile(4, 4, 4, 4, 4, 3, 10, null, "44443A00");
            assertEquals("Totalitarian", profile.getLawLevelDescription());
        }

        @Test
        @DisplayName("Law 4 is Average")
        void average() {
            B5WorldProfile profile = new B5WorldProfile(4, 4, 4, 4, 4, 3, 4, null, "44443400");
            assertEquals("Average", profile.getLawLevelDescription());
        }
    }

    @Nested
    @DisplayName("Planet size descriptions")
    class PlanetSizeDescriptions {

        @Test
        @DisplayName("Size 0 is Small Sub-Terran")
        void smallSubTerran() {
            B5WorldProfile profile = new B5WorldProfile(0, 4, 4, 4, 4, 3, 0, null, "04444300");
            assertEquals("Small Sub-Terran", profile.getPlanetSizeDescription());
        }

        @Test
        @DisplayName("Size 3 is Terran")
        void terran() {
            B5WorldProfile profile = new B5WorldProfile(3, 4, 4, 4, 4, 3, 0, null, "34444300");
            assertEquals("Terran", profile.getPlanetSizeDescription());
        }

        @Test
        @DisplayName("Size 7 is Gas Giant")
        void gasGiant() {
            B5WorldProfile profile = new B5WorldProfile(7, 4, 4, 4, 4, 3, 0, null, "74444300");
            assertEquals("Gas Giant", profile.getPlanetSizeDescription());
        }
    }

    @Nested
    @DisplayName("Atmosphere descriptions")
    class AtmosphereDescriptions {

        @Test
        @DisplayName("Atmosphere 0 is None")
        void none() {
            B5WorldProfile profile = new B5WorldProfile(4, 0, 4, 4, 4, 3, 0, null, "40444300");
            assertEquals("None", profile.getAtmosphereDescription());
        }

        @Test
        @DisplayName("Atmosphere 4 is Standard")
        void standard() {
            B5WorldProfile profile = new B5WorldProfile(4, 4, 4, 4, 4, 3, 0, null, "44444300");
            assertEquals("Standard", profile.getAtmosphereDescription());
        }

        @Test
        @DisplayName("Atmosphere 8 is None (Too Hot)")
        void tooHot() {
            B5WorldProfile profile = new B5WorldProfile(4, 8, 4, 4, 4, 3, 0, null, "48444300");
            assertEquals("None (Too Hot)", profile.getAtmosphereDescription());
        }
    }

    @Nested
    @DisplayName("Trade code derivation")
    class TradeCodeDerivation {

        @Test
        @DisplayName("Vacuum world derives VACUUM")
        void vacuum() {
            B5WorldProfile profile = new B5WorldProfile(4, 0, 4, 4, 4, 3, 0, null, "40444300");
            assertEquals(B5WorldProfile.B5TradeCode.VACUUM, profile.derivePrimaryTradeCode());
        }

        @Test
        @DisplayName("No population derives NONE")
        void none() {
            B5WorldProfile profile = new B5WorldProfile(4, 4, 4, 4, 0, 3, 0, null, "44404300");
            assertEquals(B5WorldProfile.B5TradeCode.NONE, profile.derivePrimaryTradeCode());
        }

        @Test
        @DisplayName("High population derives HIGH_POPULATION")
        void highPopulation() {
            B5WorldProfile profile = new B5WorldProfile(4, 4, 4, 4, 7, 3, 0, null, "44474300");
            assertEquals(B5WorldProfile.B5TradeCode.HIGH_POPULATION, profile.derivePrimaryTradeCode());
        }

        @Test
        @DisplayName("Low population derives COLONIAL")
        void colonial() {
            B5WorldProfile profile = new B5WorldProfile(4, 4, 4, 4, 2, 3, 0, null, "44424300");
            assertEquals(B5WorldProfile.B5TradeCode.COLONIAL, profile.derivePrimaryTradeCode());
        }

        @Test
        @DisplayName("Agricultural world")
        void agricultural() {
            B5WorldProfile profile = new B5WorldProfile(4, 4, 4, 4, 4, 3, 0, null, "44444300");
            assertEquals(B5WorldProfile.B5TradeCode.AGRICULTURAL, profile.derivePrimaryTradeCode());
        }

        @Test
        @DisplayName("Poor world with no government")
        void poor() {
            B5WorldProfile profile = new B5WorldProfile(4, 4, 2, 4, 4, 0, 0, null, "44240000");
            assertEquals(B5WorldProfile.B5TradeCode.POOR, profile.derivePrimaryTradeCode());
        }

        @Test
        @DisplayName("Rich world")
        void rich() {
            B5WorldProfile profile = new B5WorldProfile(4, 4, 2, 4, 4, 4, 5, null, "44244450");
            assertEquals(B5WorldProfile.B5TradeCode.RICH, profile.derivePrimaryTradeCode());
        }
    }

    @Nested
    @DisplayName("B5TradeCode enum")
    class B5TradeCodeEnum {

        @Test
        @DisplayName("All trade code values exist")
        void allValues() {
            B5WorldProfile.B5TradeCode[] codes = B5WorldProfile.B5TradeCode.values();
            assertTrue(codes.length >= 10);
        }

        @Test
        @DisplayName("Trade code codes and descriptions")
        void codesAndDescriptions() {
            assertEquals("Va", B5WorldProfile.B5TradeCode.VACUUM.getCode());
            assertEquals("Vacuum world", B5WorldProfile.B5TradeCode.VACUUM.getDescription());
            assertEquals("Vo", B5WorldProfile.B5TradeCode.VORLON.getCode());
            assertEquals("Sh", B5WorldProfile.B5TradeCode.SHADOW.getCode());
        }

        @Test
        @DisplayName("Trade code toString returns code")
        void toStringReturnsCode() {
            assertEquals("Va", B5WorldProfile.B5TradeCode.VACUUM.toString());
        }
    }

    @Nested
    @DisplayName("Equals and hashCode")
    class EqualsAndHashCode {

        @Test
        @DisplayName("Equal profiles are equal")
        void equalProfiles() {
            B5WorldProfile p1 = new B5WorldProfile(4, 4, 4, 4, 4, 3, 0, null, "44444300");
            B5WorldProfile p2 = new B5WorldProfile(4, 4, 4, 4, 4, 3, 0, null, "44444300");
            assertEquals(p1, p2);
            assertEquals(p1.hashCode(), p2.hashCode());
        }

        @Test
        @DisplayName("Different profiles are not equal")
        void differentProfiles() {
            B5WorldProfile p1 = new B5WorldProfile(4, 4, 4, 4, 4, 3, 0, null, "44444300");
            B5WorldProfile p2 = new B5WorldProfile(5, 4, 4, 4, 4, 3, 0, null, "54444300");
            assertNotEquals(p1, p2);
        }

        @Test
        @DisplayName("Same object equals itself")
        void sameObject() {
            B5WorldProfile profile = new B5WorldProfile(4, 4, 4, 4, 4, 3, 0, null, "44444300");
            assertEquals(profile, profile);
        }

        @Test
        @DisplayName("Not equal to null")
        void notEqualNull() {
            B5WorldProfile profile = new B5WorldProfile(4, 4, 4, 4, 4, 3, 0, null, "44444300");
            assertNotEquals(profile, null);
        }
    }

    @Nested
    @DisplayName("toString")
    class ToString {

        @Test
        @DisplayName("toString returns profile string")
        void returnsProfileString() {
            B5WorldProfile profile = new B5WorldProfile(4, 4, 4, 4, 4, 3, 0, "Vorlon", "44444300-Vorlon");
            assertEquals("44444300-Vorlon", profile.toString());
        }
    }
}
