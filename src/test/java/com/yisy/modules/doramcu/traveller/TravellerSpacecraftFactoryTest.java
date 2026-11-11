package com.yisy.modules.doramcu.traveller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("TravellerSpacecraftFactory")
class TravellerSpacecraftFactoryTest {

    @Nested
    @DisplayName("Create Spacecraft")
    class CreateSpacecraft {

        @Test
        @DisplayName("creates fighter with laser weapon")
        void createsFighter() {
            TravellerSpacecraftFactory factory = new TravellerSpacecraftFactory();
            TravellerSpacecraft sc = factory.createSpacecraft(TravellerSpacecraftType.FIGHTER, 7);

            assertEquals(TravellerSpacecraftType.FIGHTER, sc.getType());
            assertEquals(7, sc.getTechLevel());
            assertEquals(1, sc.getWeapons().size());
            assertEquals(AirWeaponMount.MountType.LASER, sc.getWeapons().get(0).getMountType());
            assertNotNull(sc.getId());
            assertTrue(sc.getId().startsWith("sc-fighter-"));
        }

        @Test
        @DisplayName("creates bomber with laser weapon")
        void createsBomber() {
            TravellerSpacecraftFactory factory = new TravellerSpacecraftFactory();
            TravellerSpacecraft sc = factory.createSpacecraft(TravellerSpacecraftType.BOMBER, 8);

            assertEquals(TravellerSpacecraftType.BOMBER, sc.getType());
            assertEquals(8, sc.getTechLevel());
            assertTrue(sc.isPressurized());
        }

        @Test
        @DisplayName("weapon damage scales with TL")
        void weaponScalesWithTl() {
            TravellerSpacecraftFactory factory = new TravellerSpacecraftFactory();
            TravellerSpacecraft sc7 = factory.createSpacecraft(TravellerSpacecraftType.FIGHTER, 7);
            TravellerSpacecraft sc9 = factory.createSpacecraft(TravellerSpacecraftType.FIGHTER, 9);

            assertEquals(7, sc7.getWeapons().get(0).getDamageCode());
            assertEquals(9, sc9.getWeapons().get(0).getDamageCode());
        }

        @Test
        @DisplayName("creates all spacecraft types")
        void createsAllTypes() {
            TravellerSpacecraftFactory factory = new TravellerSpacecraftFactory();

            for (TravellerSpacecraftType type : TravellerSpacecraftType.values()) {
                TravellerSpacecraft sc = factory.createSpacecraft(type, 7);
                assertNotNull(sc);
                assertEquals(type, sc.getType());
            }
        }
    }

    @Nested
    @DisplayName("Available Spacecraft")
    class AvailableSpacecraft {

        @Test
        @DisplayName("returns available types for low TL")
        void lowTlAvailable() {
            TravellerSpacecraftFactory factory = new TravellerSpacecraftFactory();
            List<TravellerSpacecraftType> available = factory.getAvailableSpacecraft(1);

            // At TL 1, only RECONNAISSANCE (80 <= 100) should be available
            assertFalse(available.isEmpty());
            assertTrue(available.contains(TravellerSpacecraftType.RECONNAISSANCE));
        }

        @Test
        @DisplayName("returns more types for higher TL")
        void higherTlMoreAvailable() {
            TravellerSpacecraftFactory factory = new TravellerSpacecraftFactory();
            List<TravellerSpacecraftType> low = factory.getAvailableSpacecraft(1);
            List<TravellerSpacecraftType> high = factory.getAvailableSpacecraft(10);

            assertTrue(high.size() >= low.size());
        }

        @Test
        @DisplayName("all types available at high TL")
        void allTypesAtHighTl() {
            TravellerSpacecraftFactory factory = new TravellerSpacecraftFactory();
            List<TravellerSpacecraftType> available = factory.getAvailableSpacecraft(10);

            assertEquals(TravellerSpacecraftType.values().length, available.size());
        }
    }

    @Nested
    @DisplayName("Can Build")
    class CanBuild {

        @Test
        @DisplayName("can build when world TL matches spacecraft TL")
        void canBuildWhenTlMatches() {
            TravellerSpacecraftFactory factory = new TravellerSpacecraftFactory();
            TravellerSpacecraft sc = factory.createSpacecraft(TravellerSpacecraftType.FIGHTER, 7);

            assertTrue(factory.canBuild(sc, 7));
        }

        @Test
        @DisplayName("can build when world TL is higher")
        void canBuildWhenWorldTlHigher() {
            TravellerSpacecraftFactory factory = new TravellerSpacecraftFactory();
            TravellerSpacecraft sc = factory.createSpacecraft(TravellerSpacecraftType.FIGHTER, 7);

            assertTrue(factory.canBuild(sc, 9));
        }

        @Test
        @DisplayName("cannot build when world TL is lower")
        void cannotBuildWhenWorldTlLower() {
            TravellerSpacecraftFactory factory = new TravellerSpacecraftFactory();
            TravellerSpacecraft sc = factory.createSpacecraft(TravellerSpacecraftType.FIGHTER, 9);

            assertFalse(factory.canBuild(sc, 7));
        }
    }
}
