package com.yisy.modules.doramcu.sfb;

import com.yisy.modules.doramcu.sfb.model.SfbShip;
import com.yisy.modules.doramcu.sfb.model.SfbShield;
import com.yisy.modules.doramcu.sfb.model.SfbWeaponBank;
import com.yisy.modules.doramcu.sfb.service.SfbShipLoader;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("SfbShipLoader Tests")
class SfbShipLoaderTest {

    private SfbShipLoader createLoader() {
        return new SfbShipLoader();
    }

    @Test
    @DisplayName("Loads available empires from JSON")
    void loadsAvailableEmpires() {
        SfbShipLoader loader = createLoader();
        Set<String> empires = loader.getAvailableEmpires();
        assertFalse(empires.isEmpty(), "Should load at least one empire");
        assertTrue(empires.contains("federation"), "Should contain federation");
        assertTrue(empires.contains("klingon"), "Should contain klingon");
        assertTrue(empires.contains("romulan"), "Should contain romulan");
    }

    @Test
    @DisplayName("Lists ship classes for an empire")
    void listsShipClasses() {
        SfbShipLoader loader = createLoader();
        List<String> classes = loader.getShipClasses("federation");
        assertFalse(classes.isEmpty(), "Federation should have ship classes");
        assertTrue(classes.contains("Constitution-class"), "Should contain Constitution-class");
    }

    @Test
    @DisplayName("Returns empty list for unknown empire")
    void unknownEmpireReturnsEmpty() {
        SfbShipLoader loader = createLoader();
        List<String> classes = loader.getShipClasses("nonexistent");
        assertTrue(classes.isEmpty(), "Unknown empire should return empty list");
    }

    @Test
    @DisplayName("Creates Federation Constitution-class ship")
    void createFederationShip() {
        SfbShipLoader loader = createLoader();
        SfbShip ship = loader.createShip("federation", "Constitution-class", "test-const", 0);

        assertNotNull(ship);
        assertEquals("test-const", ship.getId());
        assertEquals("Constitution-class", ship.getName());
        assertEquals("CA", ship.getShipClass());
        assertEquals(0, ship.getSide());
        assertNotNull(ship.getSsd());
        assertNotNull(ship.getEmpire());
        assertEquals(0, ship.getSide());
    }

    @Test
    @DisplayName("Creates Klingon D7-class ship")
    void createKlingonShip() {
        SfbShipLoader loader = createLoader();
        SfbShip ship = loader.createShip("klingon", "D7-class", "test-d7", 1);

        assertNotNull(ship);
        assertEquals("D7-class", ship.getName());
        assertEquals("CA", ship.getShipClass());
        assertEquals(1, ship.getSide());
        assertNotNull(ship.getSsd());
    }

    @Test
    @DisplayName("Creates Romulan Warbird with cloaking")
    void createRomulanWarbird() {
        SfbShipLoader loader = createLoader();
        SfbShip ship = loader.createShip("romulan", "Warbird (D5-class)", "test-warbird", 0);

        assertNotNull(ship);
        assertEquals("Warbird (D5-class)", ship.getName());
        assertNotNull(ship.getSsd());
    }

    @Test
    @DisplayName("Ship has weapon banks configured from JSON")
    void shipHasWeaponBanks() {
        SfbShipLoader loader = createLoader();
        SfbShip ship = loader.createShip("federation", "Constitution-class", "test-weapons", 0);

        List<SfbWeaponBank> weapons = ship.getWeaponBanks();
        assertFalse(weapons.isEmpty(), "Ship should have weapon banks");
        
        boolean hasPhaser = false;
        boolean hasTorpedo = false;
        for (SfbWeaponBank bank : weapons) {
            if (bank.getWeaponType() == SfbWeaponBank.WeaponType.PHASER_2) {
                hasPhaser = true;
            }
            if (bank.getWeaponType() == SfbWeaponBank.WeaponType.PHOTON_TORPEDO) {
                hasTorpedo = true;
            }
        }
        assertTrue(hasPhaser, "Should have phaser weapon bank");
        assertTrue(hasTorpedo, "Should have torpedo weapon bank");
    }

    @Test
    @DisplayName("Ship has shields initialized")
    void shipHasShields() {
        SfbShipLoader loader = createLoader();
        SfbShip ship = loader.createShip("federation", "Constitution-class", "test-shields", 0);

        SfbShield[] shields = ship.getShields();
        assertNotNull(shields);
        assertEquals(6, shields.length);
        for (int i = 0; i < 6; i++) {
            assertNotNull(shields[i], "Shield " + i + " should be initialized");
        }
    }

    @Test
    @DisplayName("Ship has SSD with hull boxes and DamCon")
    void shipHasSSD() {
        SfbShipLoader loader = createLoader();
        SfbShip ship = loader.createShip("federation", "Constitution-class", "test-ssd", 0);

        assertNotNull(ship.getSsd());
        assertTrue(ship.getSsd().getHullBoxes() > 0, "Should have hull boxes");
        assertTrue(ship.getSsd().getDamConRating() > 0, "Should have DamCon rating");
    }

    @Test
    @DisplayName("SC1 ship (B10) has more hull boxes than SC2")
    void sc1ShipHasMoreHull() {
        SfbShipLoader loader = createLoader();
        SfbShip b10 = loader.createShip("klingon", "B10 Battleship", "test-b10", 0);
        SfbShip d7 = loader.createShip("klingon", "D7-class", "test-d7", 0);

        assertTrue(b10.getSsd().getHullBoxes() > d7.getSsd().getHullBoxes(),
                "B10 (SC1) should have more hull boxes than D7 (SC2)");
    }

    @Test
    @DisplayName("SC3 ship is faster than SC2")
    void sc3ShipIsFaster() {
        SfbShipLoader loader = createLoader();
        SfbShip nova = loader.createShip("federation", "Nova-class", "test-nova", 0);
        SfbShip constitution = loader.createShip("federation", "Constitution-class", "test-const", 0);

        assertTrue(nova.getSpeed() > constitution.getSpeed(),
                "Nova (SC3) should be faster than Constitution (SC2)");
    }

    @Test
    @DisplayName("Throws exception for unknown empire")
    void unknownEmpireThrows() {
        SfbShipLoader loader = createLoader();
        assertThrows(IllegalArgumentException.class, () -> {
            loader.createShip("nonexistent", "SomeShip");
        });
    }

    @Test
    @DisplayName("Throws exception for unknown ship class")
    void unknownShipClassThrows() {
        SfbShipLoader loader = createLoader();
        assertThrows(IllegalArgumentException.class, () -> {
            loader.createShip("federation", "NonexistentShip");
        });
    }

    @Test
    @DisplayName("Auto-generates ship ID when null")
    void autoGenerateShipId() {
        SfbShipLoader loader = createLoader();
        SfbShip ship = loader.createShip("federation", "Constitution-class");

        assertNotNull(ship.getId());
        assertFalse(ship.getId().isEmpty());
    }

    @Test
    @DisplayName("Empire data loaded from empires.json")
    void empireDataLoaded() {
        SfbShipLoader loader = createLoader();
        SfbShip ship = loader.createShip("federation", "Constitution-class", "test-empire", 0);

        assertNotNull(ship.getEmpire());
        assertNotNull(ship.getEmpire().getName());
    }
}
