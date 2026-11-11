package com.yisy.modules.doramcu.traveller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("AircraftConversionService")
class AircraftConversionServiceTest {

    @Nested
    @DisplayName("Constructor")
    class Constructor {

        @Test
        @DisplayName("preloads all Third Reich aircraft conversions")
        void preloadsConversions() {
            AircraftConversionService service = new AircraftConversionService();
            List<AircraftConversion> conversions = service.getAllConversions();

            assertEquals(ThirdReichAircraft.values().length, conversions.size());
        }
    }

    @Nested
    @DisplayName("Convert")
    class Convert {

        @Test
        @DisplayName("converts Bf 109 to Traveller fighter")
        void convertsBf109() {
            AircraftConversionService service = new AircraftConversionService();
            TravellerSpacecraft sc = service.convert(ThirdReichAircraft.MESSERSCHMITT_BF109);

            assertEquals(TravellerSpacecraftType.FIGHTER, sc.getType());
            assertEquals(7, sc.getTechLevel());
            assertEquals(3, sc.getAttackValue());
            assertEquals(2, sc.getDefenseValue());
            assertEquals(80, sc.getCost());
            assertNotNull(sc.getName());
            assertTrue(sc.getName().contains("Traveller"));
        }

        @Test
        @DisplayName("converts Me 262 to Traveller interceptor")
        void convertsMe262() {
            AircraftConversionService service = new AircraftConversionService();
            TravellerSpacecraft sc = service.convert(ThirdReichAircraft.ME262);

            assertEquals(TravellerSpacecraftType.INTERCEPTOR, sc.getType());
            assertEquals(8, sc.getTechLevel());
            assertEquals(4, sc.getAttackValue());
            assertEquals(4, sc.getDefenseValue());
        }

        @Test
        @DisplayName("converts B-17 to Traveller bomber")
        void convertsB17() {
            AircraftConversionService service = new AircraftConversionService();
            TravellerSpacecraft sc = service.convert(ThirdReichAircraft.B_17_FORTRESS);

            assertEquals(TravellerSpacecraftType.BOMBER, sc.getType());
            assertEquals(8, sc.getTechLevel());
            assertEquals(7, sc.getAttackValue());
        }

        @Test
        @DisplayName("converted spacecraft has weapon")
        void hasWeapon() {
            AircraftConversionService service = new AircraftConversionService();
            TravellerSpacecraft sc = service.convert(ThirdReichAircraft.MESSERSCHMITT_BF109);

            assertEquals(1, sc.getWeapons().size());
            assertEquals(AirWeaponMount.MountType.GUN, sc.getWeapons().get(0).getMountType());
        }

        @Test
        @DisplayName("converts all aircraft types")
        void convertsAllTypes() {
            AircraftConversionService service = new AircraftConversionService();

            for (ThirdReichAircraft aircraft : ThirdReichAircraft.values()) {
                TravellerSpacecraft sc = service.convert(aircraft);
                assertNotNull(sc);
                assertNotNull(sc.getId());
                assertNotNull(sc.getName());
            }
        }
    }

    @Nested
    @DisplayName("Get Conversion")
    class GetConversion {

        @Test
        @DisplayName("returns conversion for known aircraft")
        void returnsKnownConversion() {
            AircraftConversionService service = new AircraftConversionService();
            AircraftConversion conversion = service.getConversion(ThirdReichAircraft.MESSERSCHMITT_BF109);

            assertNotNull(conversion);
            assertEquals("MESSERSCHMITT_BF109", conversion.getThirdReichAircraftId());
        }

        @Test
        @DisplayName("all known aircraft have conversions")
        void allAircraftHaveConversions() {
            AircraftConversionService service = new AircraftConversionService();

            for (ThirdReichAircraft aircraft : ThirdReichAircraft.values()) {
                assertNotNull(service.getConversion(aircraft));
            }
        }
    }

    @Nested
    @DisplayName("Get All Conversions")
    class GetAllConversions {

        @Test
        @DisplayName("returns independent list")
        void returnsIndependentList() {
            AircraftConversionService service = new AircraftConversionService();
            List<AircraftConversion> list1 = service.getAllConversions();
            List<AircraftConversion> list2 = service.getAllConversions();

            assertNotSame(list1, list2);
            assertEquals(list1.size(), list2.size());
        }
    }
}
