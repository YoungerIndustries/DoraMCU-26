package com.yisy.modules.doramcu.traveller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("AirBattleEvent")
class AirBattleEventTest {

    @Nested
    @DisplayName("Constructor")
    class Constructor {

        @Test
        @DisplayName("creates event with round and message")
        void createsWithRoundAndMessage() {
            AirBattleEvent event = new AirBattleEvent(1, "Battle started");

            assertEquals(1, event.getRound());
            assertEquals("Battle started", event.getMessage());
            assertTrue(event.getTimestamp() > 0);
        }

        @Test
        @DisplayName("default constructor creates empty event")
        void defaultConstructor() {
            AirBattleEvent event = new AirBattleEvent();

            assertEquals(0, event.getRound());
            assertNull(event.getMessage());
            assertEquals(0, event.getTimestamp());
        }

        @Test
        @DisplayName("timestamp is current time")
        void timestampIsCurrent() {
            long before = System.currentTimeMillis();
            AirBattleEvent event = new AirBattleEvent(1, "Test");
            long after = System.currentTimeMillis();

            assertTrue(event.getTimestamp() >= before && event.getTimestamp() <= after);
        }
    }
}

@DisplayName("AircraftConversion")
class AircraftConversionTest {

    @Nested
    @DisplayName("Constructor")
    class Constructor {

        @Test
        @DisplayName("creates conversion from Third Reich aircraft")
        void createsFromAircraft() {
            AircraftConversion conversion = new AircraftConversion(ThirdReichAircraft.MESSERSCHMITT_BF109);

            assertEquals("MESSERSCHMITT_BF109", conversion.getThirdReichAircraftId());
            assertTrue(conversion.getTravellerSpacecraftName().contains("Traveller"));
            assertEquals(7, conversion.getTechLevel());
            assertEquals(80, conversion.getCost());
            assertTrue(conversion.getRequiredSkills().contains("Pilot"));
            assertTrue(conversion.getRequiredSkills().contains("Engineering"));
            assertTrue(conversion.getDescription().contains("Germany"));
        }

        @Test
        @DisplayName("default constructor creates empty conversion")
        void defaultConstructor() {
            AircraftConversion conversion = new AircraftConversion();

            assertNull(conversion.getThirdReichAircraftId());
            assertNull(conversion.getTravellerSpacecraftName());
            assertEquals(0, conversion.getTechLevel());
        }
    }

    @Nested
    @DisplayName("Setters")
    class Setters {

        @Test
        @DisplayName("sets all fields")
        void setsAllFields() {
            AircraftConversion conversion = new AircraftConversion();
            conversion.setThirdReichAircraftId("TEST");
            conversion.setTravellerSpacecraftName("Test Craft");
            conversion.setTechLevel(9);
            conversion.setCost(500);
            conversion.setDescription("Test description");

            assertEquals("TEST", conversion.getThirdReichAircraftId());
            assertEquals("Test Craft", conversion.getTravellerSpacecraftName());
            assertEquals(9, conversion.getTechLevel());
            assertEquals(500, conversion.getCost());
            assertEquals("Test description", conversion.getDescription());
        }
    }
}

@DisplayName("ThirdReichAircraft")
class ThirdReichAircraftTest {

    @Nested
    @DisplayName("Enum values")
    class EnumValues {

        @Test
        @DisplayName("has multiple aircraft")
        void hasMultipleAircraft() {
            assertTrue(ThirdReichAircraft.values().length > 10);
        }

        @Test
        @DisplayName("includes German fighters")
        void includesGermanFighters() {
            assertEquals("Messerschmitt Bf 109", ThirdReichAircraft.MESSERSCHMITT_BF109.getName());
            assertEquals("Germany", ThirdReichAircraft.MESSERSCHMITT_BF109.getNationality());
            assertEquals(TravellerSpacecraftType.FIGHTER, ThirdReichAircraft.MESSERSCHMITT_BF109.getType());
        }

        @Test
        @DisplayName("includes British aircraft")
        void includesBritishAircraft() {
            assertEquals("United Kingdom", ThirdReichAircraft.SPITFIRE.getNationality());
            assertEquals("United Kingdom", ThirdReichAircraft.LANCASTER.getNationality());
        }

        @Test
        @DisplayName("includes US aircraft")
        void includesUsAircraft() {
            assertEquals("United States", ThirdReichAircraft.P_51_MUSTANG.getNationality());
            assertEquals("United States", ThirdReichAircraft.B_17_FORTRESS.getNationality());
        }

        @Test
        @DisplayName("fighters have correct type")
        void fightersHaveCorrectType() {
            assertEquals(TravellerSpacecraftType.FIGHTER, ThirdReichAircraft.MESSERSCHMITT_BF109.getType());
            assertEquals(TravellerSpacecraftType.FIGHTER, ThirdReichAircraft.SPITFIRE.getType());
            assertEquals(TravellerSpacecraftType.FIGHTER, ThirdReichAircraft.P_51_MUSTANG.getType());
        }

        @Test
        @DisplayName("bombers have correct type")
        void bombersHaveCorrectType() {
            assertEquals(TravellerSpacecraftType.BOMBER, ThirdReichAircraft.HEINKEL_HE111.getType());
            assertEquals(TravellerSpacecraftType.BOMBER, ThirdReichAircraft.LANCASTER.getType());
            assertEquals(TravellerSpacecraftType.BOMBER, ThirdReichAircraft.B_17_FORTRESS.getType());
        }

        @Test
        @DisplayName("interceptors have correct type")
        void interceptorsHaveCorrectType() {
            assertEquals(TravellerSpacecraftType.INTERCEPTOR, ThirdReichAircraft.ME262.getType());
            assertEquals(TravellerSpacecraftType.INTERCEPTOR, ThirdReichAircraft.P_38_LIGHTNING.getType());
        }

        @Test
        @DisplayName("tech levels are reasonable")
        void techLevelsReasonable() {
            for (ThirdReichAircraft aircraft : ThirdReichAircraft.values()) {
                assertTrue(aircraft.getTechLevel() >= 7 && aircraft.getTechLevel() <= 8);
            }
        }

        @Test
        @DisplayName("costs are positive")
        void costsPositive() {
            for (ThirdReichAircraft aircraft : ThirdReichAircraft.values()) {
                assertTrue(aircraft.getCost() > 0);
            }
        }

        @Test
        @DisplayName("attack and defense values are positive")
        void combatValuesPositive() {
            for (ThirdReichAircraft aircraft : ThirdReichAircraft.values()) {
                assertTrue(aircraft.getAttackValue() > 0);
                assertTrue(aircraft.getDefenseValue() > 0);
            }
        }
    }
}

@DisplayName("TravellerSpacecraftType")
class TravellerSpacecraftTypeTest {

    @Nested
    @DisplayName("Enum values")
    class EnumValues {

        @Test
        @DisplayName("has five spacecraft types")
        void hasFiveTypes() {
            assertEquals(5, TravellerSpacecraftType.values().length);
        }

        @Test
        @DisplayName("fighter has correct base stats")
        void fighterStats() {
            assertEquals("Fighter", TravellerSpacecraftType.FIGHTER.getDisplayName());
            assertEquals(1, TravellerSpacecraftType.FIGHTER.getBaseCrew());
            assertEquals(2, TravellerSpacecraftType.FIGHTER.getBaseArmor());
            assertEquals(4, TravellerSpacecraftType.FIGHTER.getBaseMovement());
            assertEquals(100, TravellerSpacecraftType.FIGHTER.getBaseCost());
        }

        @Test
        @DisplayName("bomber has correct base stats")
        void bomberStats() {
            assertEquals("Bomber", TravellerSpacecraftType.BOMBER.getDisplayName());
            assertEquals(2, TravellerSpacecraftType.BOMBER.getBaseCrew());
            assertEquals(1, TravellerSpacecraftType.BOMBER.getBaseArmor());
            assertEquals(2, TravellerSpacecraftType.BOMBER.getBaseMovement());
            assertEquals(200, TravellerSpacecraftType.BOMBER.getBaseCost());
        }

        @Test
        @DisplayName("interceptor has correct base stats")
        void interceptorStats() {
            assertEquals("Interceptor", TravellerSpacecraftType.INTERCEPTOR.getDisplayName());
            assertEquals(1, TravellerSpacecraftType.INTERCEPTOR.getBaseCrew());
            assertEquals(3, TravellerSpacecraftType.INTERCEPTOR.getBaseArmor());
            assertEquals(5, TravellerSpacecraftType.INTERCEPTOR.getBaseMovement());
            assertEquals(150, TravellerSpacecraftType.INTERCEPTOR.getBaseCost());
        }

        @Test
        @DisplayName("reconnaissance has lowest cost")
        void reconStats() {
            assertEquals("Reconnaissance", TravellerSpacecraftType.RECONNAISSANCE.getDisplayName());
            assertEquals(80, TravellerSpacecraftType.RECONNAISSANCE.getBaseCost());
        }

        @Test
        @DisplayName("custom type exists")
        void customType() {
            assertEquals("Custom", TravellerSpacecraftType.CUSTOM.getDisplayName());
        }
    }
}
