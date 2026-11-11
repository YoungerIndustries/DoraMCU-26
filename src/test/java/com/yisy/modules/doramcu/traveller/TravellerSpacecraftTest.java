package com.yisy.modules.doramcu.traveller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("TravellerSpacecraft")
class TravellerSpacecraftTest {

    @Nested
    @DisplayName("Constructor")
    class Constructor {

        @Test
        @DisplayName("creates spacecraft with correct type defaults")
        void createsWithDefaults() {
            TravellerSpacecraft sc = new TravellerSpacecraft("id1", "Test Fighter", TravellerSpacecraftType.FIGHTER, 7);

            assertEquals("id1", sc.getId());
            assertEquals("Test Fighter", sc.getName());
            assertEquals(TravellerSpacecraftType.FIGHTER, sc.getType());
            assertEquals(7, sc.getTechLevel());
            assertEquals(2, sc.getArmorClass()); // FIGHTER base armor
            assertEquals(24, sc.getHullPoints()); // 10 + 7*2
            assertEquals(11, sc.getMovement()); // 4 + 7
            assertEquals(1, sc.getCrew()); // FIGHTER base crew
            assertEquals(800, sc.getCost()); // 100 * (1 + 7)
            assertFalse(sc.isPressurized());
        }

        @Test
        @DisplayName("pressurized at TL 8+")
        void pressurizedAtTl8() {
            TravellerSpacecraft sc = new TravellerSpacecraft("id1", "Advanced Fighter", TravellerSpacecraftType.FIGHTER, 8);

            assertTrue(sc.isPressurized());
        }

        @Test
        @DisplayName("not pressurized below TL 8")
        void notPressurizedBelowTl8() {
            TravellerSpacecraft sc = new TravellerSpacecraft("id1", "Basic Fighter", TravellerSpacecraftType.FIGHTER, 7);

            assertFalse(sc.isPressurized());
        }

        @Test
        @DisplayName("bomber has higher crew and cost")
        void bomberStats() {
            TravellerSpacecraft sc = new TravellerSpacecraft("id1", "Test Bomber", TravellerSpacecraftType.BOMBER, 7);

            assertEquals(2, sc.getCrew());
            assertEquals(1, sc.getArmorClass());
            assertEquals(1600, sc.getCost()); // 200 * (1 + 7)
        }

        @Test
        @DisplayName("interceptor has higher movement")
        void interceptorStats() {
            TravellerSpacecraft sc = new TravellerSpacecraft("id1", "Test Interceptor", TravellerSpacecraftType.INTERCEPTOR, 7);

            assertEquals(12, sc.getMovement()); // 5 + 7
            assertEquals(1200, sc.getCost()); // 150 * (1 + 7)
        }

        @Test
        @DisplayName("default constructor creates empty spacecraft")
        void defaultConstructor() {
            TravellerSpacecraft sc = new TravellerSpacecraft();

            assertNull(sc.getId());
            assertNull(sc.getName());
            assertNull(sc.getType());
            assertEquals(0, sc.getTechLevel());
            assertTrue(sc.getWeapons().isEmpty());
        }
    }

    @Nested
    @DisplayName("Weapons")
    class Weapons {

        @Test
        @DisplayName("addWeapon adds weapon and recalculates attack value")
        void addWeapon() {
            TravellerSpacecraft sc = new TravellerSpacecraft("id1", "Fighter", TravellerSpacecraftType.FIGHTER, 7);
            AirWeaponMount weapon = new AirWeaponMount("w1", AirWeaponMount.MountType.LASER, "Laser", 5, 10);
            sc.addWeapon(weapon);

            assertEquals(1, sc.getWeapons().size());
            assertEquals(5, sc.getAttackValue());
        }

        @Test
        @DisplayName("multiple weapons sum attack values")
        void multipleWeapons() {
            TravellerSpacecraft sc = new TravellerSpacecraft("id1", "Fighter", TravellerSpacecraftType.FIGHTER, 7);
            sc.addWeapon(new AirWeaponMount("w1", AirWeaponMount.MountType.LASER, "Laser 1", 3, 8));
            sc.addWeapon(new AirWeaponMount("w2", AirWeaponMount.MountType.GUN, "Gun", 4, 5));

            assertEquals(2, sc.getWeapons().size());
            assertEquals(7, sc.getAttackValue());
        }

        @Test
        @DisplayName("recalculateCombatFactors updates defense with pressurized bonus")
        void recalculateDefenseWithPressurized() {
            TravellerSpacecraft sc = new TravellerSpacecraft("id1", "Fighter", TravellerSpacecraftType.FIGHTER, 8);
            sc.addWeapon(new AirWeaponMount("w1", AirWeaponMount.MountType.LASER, "Laser", 5, 10));

            assertEquals(3, sc.getDefenseValue()); // armor 2 + pressurized 1
        }

        @Test
        @DisplayName("recalculateCombatFactors without pressurized")
        void recalculateDefenseWithoutPressurized() {
            TravellerSpacecraft sc = new TravellerSpacecraft("id1", "Fighter", TravellerSpacecraftType.FIGHTER, 7);
            sc.addWeapon(new AirWeaponMount("w1", AirWeaponMount.MountType.LASER, "Laser", 5, 10));

            assertEquals(2, sc.getDefenseValue()); // armor 2 + no pressurized
        }
    }

    @Nested
    @DisplayName("Setters")
    class Setters {

        @Test
        @DisplayName("setters update values")
        void settersWork() {
            TravellerSpacecraft sc = new TravellerSpacecraft();
            sc.setId("new-id");
            sc.setName("New Name");
            sc.setType(TravellerSpacecraftType.BOMBER);
            sc.setTechLevel(9);
            sc.setArmorClass(5);
            sc.setHullPoints(50);
            sc.setMovement(15);
            sc.setCrew(5);
            sc.setCost(500);
            sc.setAltitude(10000);
            sc.setPressurized(true);
            sc.setAttackValue(10);
            sc.setDefenseValue(8);

            assertEquals("new-id", sc.getId());
            assertEquals("New Name", sc.getName());
            assertEquals(TravellerSpacecraftType.BOMBER, sc.getType());
            assertEquals(9, sc.getTechLevel());
            assertEquals(5, sc.getArmorClass());
            assertEquals(50, sc.getHullPoints());
            assertEquals(15, sc.getMovement());
            assertEquals(5, sc.getCrew());
            assertEquals(500, sc.getCost());
            assertEquals(10000, sc.getAltitude());
            assertTrue(sc.isPressurized());
            assertEquals(10, sc.getAttackValue());
            assertEquals(8, sc.getDefenseValue());
        }
    }
}
