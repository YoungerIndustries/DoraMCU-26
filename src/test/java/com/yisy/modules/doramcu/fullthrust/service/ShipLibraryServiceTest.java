package com.yisy.modules.doramcu.fullthrust.service;

import com.yisy.modules.doramcu.fullthrust.model.ShipClass;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ShipLibraryServiceTest {

    private GameService gameService;

    @BeforeEach
    void setUp() {
        PersistenceService persistenceService = mock(PersistenceService.class);
        AIService aiService = mock(AIService.class);
        PlayerAccountService playerAccountService = mock(PlayerAccountService.class);
        gameService = new GameService(persistenceService, aiService, playerAccountService);
    }

    @Test
    void getDefaultShipClasses_returnsFiveShipClasses() {
        List<ShipClass> shipClasses = gameService.getDefaultShipClasses();

        assertNotNull(shipClasses);
        assertEquals(5, shipClasses.size());
    }

    @Test
    void getDefaultShipClasses_containsAllExpectedIds() {
        List<ShipClass> shipClasses = gameService.getDefaultShipClasses();

        List<String> ids = shipClasses.stream()
                .map(ShipClass::getId)
                .collect(Collectors.toList());

        assertTrue(ids.contains("scout"));
        assertTrue(ids.contains("fighter"));
        assertTrue(ids.contains("cruiser"));
        assertTrue(ids.contains("destroyer"));
        assertTrue(ids.contains("dreadnought"));
    }

    @Test
    void getDefaultShipClasses_scoutStats() {
        List<ShipClass> shipClasses = gameService.getDefaultShipClasses();
        ShipClass scout = shipClasses.stream()
                .filter(sc -> "scout".equals(sc.getId()))
                .findFirst()
                .orElseThrow();

        assertEquals("Scout", scout.getName());
        assertEquals(10, scout.getHullPoints());
        assertEquals(1, scout.getArmorValue());
        assertEquals(8, scout.getMaxSpeed());
        assertEquals(3, scout.getAcceleration());
        assertEquals(2, scout.getDeceleration());
        assertEquals(2, scout.getMaxHeadingChange());
        assertEquals(5, scout.getShieldPoints());
        assertEquals(1, scout.getShieldRegeneration());
        assertEquals(20, scout.getFuelCapacity());
        assertEquals(2, scout.getWeaponSlots().size());
    }

    @Test
    void getDefaultShipClasses_fighterStats() {
        List<ShipClass> shipClasses = gameService.getDefaultShipClasses();
        ShipClass fighter = shipClasses.stream()
                .filter(sc -> "fighter".equals(sc.getId()))
                .findFirst()
                .orElseThrow();

        assertEquals("Fighter", fighter.getName());
        assertEquals(20, fighter.getHullPoints());
        assertEquals(2, fighter.getArmorValue());
        assertEquals(6, fighter.getMaxSpeed());
        assertEquals(3, fighter.getWeaponSlots().size());
    }

    @Test
    void getDefaultShipClasses_cruiserStats() {
        List<ShipClass> shipClasses = gameService.getDefaultShipClasses();
        ShipClass cruiser = shipClasses.stream()
                .filter(sc -> "cruiser".equals(sc.getId()))
                .findFirst()
                .orElseThrow();

        assertEquals("Cruiser", cruiser.getName());
        assertEquals(40, cruiser.getHullPoints());
        assertEquals(3, cruiser.getArmorValue());
        assertEquals(5, cruiser.getMaxSpeed());
        assertEquals(4, cruiser.getWeaponSlots().size());
    }

    @Test
    void getDefaultShipClasses_destroyerStats() {
        List<ShipClass> shipClasses = gameService.getDefaultShipClasses();
        ShipClass destroyer = shipClasses.stream()
                .filter(sc -> "destroyer".equals(sc.getId()))
                .findFirst()
                .orElseThrow();

        assertEquals("Destroyer", destroyer.getName());
        assertEquals(30, destroyer.getHullPoints());
        assertEquals(2, destroyer.getArmorValue());
        assertEquals(7, destroyer.getMaxSpeed());
        assertEquals(3, destroyer.getWeaponSlots().size());
    }

    @Test
    void getDefaultShipClasses_dreadnoughtStats() {
        List<ShipClass> shipClasses = gameService.getDefaultShipClasses();
        ShipClass dreadnought = shipClasses.stream()
                .filter(sc -> "dreadnought".equals(sc.getId()))
                .findFirst()
                .orElseThrow();

        assertEquals("Dreadnought", dreadnought.getName());
        assertEquals(80, dreadnought.getHullPoints());
        assertEquals(5, dreadnought.getArmorValue());
        assertEquals(3, dreadnought.getMaxSpeed());
        assertEquals(5, dreadnought.getWeaponSlots().size());
    }

    @Test
    void getDefaultShipClasses_weaponSlotsHaveCorrectTypes() {
        List<ShipClass> shipClasses = gameService.getDefaultShipClasses();

        ShipClass scout = shipClasses.stream().filter(sc -> "scout".equals(sc.getId())).findFirst().orElseThrow();
        ShipClass.WeaponSlot s1 = scout.getWeaponSlots().get(0);
        ShipClass.WeaponSlot s2 = scout.getWeaponSlots().get(1);
        assertEquals("laser", s1.getWeaponType());
        assertEquals("laser", s2.getWeaponType());
        assertEquals(0, s1.getArc());
        assertEquals(3, s2.getArc());

        ShipClass fighter = shipClasses.stream().filter(sc -> "fighter".equals(sc.getId())).findFirst().orElseThrow();
        assertEquals("laser", fighter.getWeaponSlots().get(0).getWeaponType());
        assertEquals("laser", fighter.getWeaponSlots().get(1).getWeaponType());
        assertEquals("missile", fighter.getWeaponSlots().get(2).getWeaponType());

        ShipClass dreadnought = shipClasses.stream().filter(sc -> "dreadnought".equals(sc.getId())).findFirst().orElseThrow();
        assertEquals("laser", dreadnought.getWeaponSlots().get(0).getWeaponType());
        assertEquals("laser", dreadnought.getWeaponSlots().get(1).getWeaponType());
        assertEquals("missile", dreadnought.getWeaponSlots().get(2).getWeaponType());
        assertEquals("missile", dreadnought.getWeaponSlots().get(3).getWeaponType());
        assertEquals("torpedo", dreadnought.getWeaponSlots().get(4).getWeaponType());
    }

    @Test
    void getDefaultShipClasses_weaponSlotsHaveRangeAndDamage() {
        List<ShipClass> shipClasses = gameService.getDefaultShipClasses();

        // Scout: 2 lasers
        ShipClass scout = shipClasses.stream().filter(sc -> "scout".equals(sc.getId())).findFirst().orElseThrow();
        for (ShipClass.WeaponSlot slot : scout.getWeaponSlots()) {
            assertEquals("laser", slot.getWeaponType());
            assertEquals(1, slot.getDamage());
            assertEquals(6, slot.getRange());
        }

        // Fighter: 2 lasers + 1 missile
        ShipClass fighter = shipClasses.stream().filter(sc -> "fighter".equals(sc.getId())).findFirst().orElseThrow();
        assertEquals(1, fighter.getWeaponSlots().get(0).getDamage());
        assertEquals(6, fighter.getWeaponSlots().get(0).getRange());
        assertEquals(1, fighter.getWeaponSlots().get(1).getDamage());
        assertEquals(6, fighter.getWeaponSlots().get(1).getRange());
        assertEquals(2, fighter.getWeaponSlots().get(2).getDamage());
        assertEquals(12, fighter.getWeaponSlots().get(2).getRange());

        // Dreadnought: 2 lasers + 2 missiles + 1 torpedo
        ShipClass dreadnought = shipClasses.stream().filter(sc -> "dreadnought".equals(sc.getId())).findFirst().orElseThrow();
        assertEquals(1, dreadnought.getWeaponSlots().get(0).getDamage());
        assertEquals(6, dreadnought.getWeaponSlots().get(0).getRange());
        assertEquals(2, dreadnought.getWeaponSlots().get(2).getDamage());
        assertEquals(12, dreadnought.getWeaponSlots().get(2).getRange());
        assertEquals(3, dreadnought.getWeaponSlots().get(4).getDamage());
        assertEquals(8, dreadnought.getWeaponSlots().get(4).getRange());
    }

    @Test
    void getDefaultShipClasses_weaponSlotRangeByType() {
        List<ShipClass> shipClasses = gameService.getDefaultShipClasses();

        for (ShipClass shipClass : shipClasses) {
            for (ShipClass.WeaponSlot slot : shipClass.getWeaponSlots()) {
                switch (slot.getWeaponType()) {
                    case "laser":
                        assertEquals(6, slot.getRange(), "Laser range should be 6");
                        assertEquals(1, slot.getDamage(), "Laser damage should be 1");
                        break;
                    case "missile":
                        assertEquals(12, slot.getRange(), "Missile range should be 12");
                        assertEquals(2, slot.getDamage(), "Missile damage should be 2");
                        break;
                    case "torpedo":
                        assertEquals(8, slot.getRange(), "Torpedo range should be 8");
                        assertEquals(3, slot.getDamage(), "Torpedo damage should be 3");
                        break;
                }
            }
        }
    }

    @Test
    void weaponSlotConstructor_setsDefaultRangeAndDamage() {
        ShipClass.WeaponSlot laser = new ShipClass.WeaponSlot("test", 0, "laser");
        assertEquals(1, laser.getDamage());
        assertEquals(6, laser.getRange());

        ShipClass.WeaponSlot missile = new ShipClass.WeaponSlot("test", 1, "missile");
        assertEquals(2, missile.getDamage());
        assertEquals(12, missile.getRange());

        ShipClass.WeaponSlot torpedo = new ShipClass.WeaponSlot("test", 2, "torpedo");
        assertEquals(3, torpedo.getDamage());
        assertEquals(8, torpedo.getRange());
    }

    @Test
    void weaponSlotConstructor_defaultType() {
        ShipClass.WeaponSlot unknown = new ShipClass.WeaponSlot("test", 0, "unknown");
        assertEquals(1, unknown.getDamage());
        assertEquals(4, unknown.getRange());
    }

    @Test
    void weaponSlotSetters_work() {
        ShipClass.WeaponSlot slot = new ShipClass.WeaponSlot();
        slot.setName("test");
        slot.setArc(3);
        slot.setWeaponType("laser");
        slot.setDamage(5);
        slot.setRange(10);

        assertEquals("test", slot.getName());
        assertEquals(3, slot.getArc());
        assertEquals("laser", slot.getWeaponType());
        assertEquals(5, slot.getDamage());
        assertEquals(10, slot.getRange());
    }

    @Test
    void getDefaultShipClasses_weaponSlotsHaveNames() {
        List<ShipClass> shipClasses = gameService.getDefaultShipClasses();

        for (ShipClass shipClass : shipClasses) {
            for (ShipClass.WeaponSlot slot : shipClass.getWeaponSlots()) {
                assertNotNull(slot.getName());
                assertFalse(slot.getName().isEmpty());
            }
        }
    }

    @Test
    void getDefaultShipClasses_descriptionIsSet() {
        List<ShipClass> shipClasses = gameService.getDefaultShipClasses();

        for (ShipClass shipClass : shipClasses) {
            assertNotNull(shipClass.getDescription());
            assertTrue(shipClass.getDescription().contains(shipClass.getName()));
        }
    }

    @Test
    void getDefaultShipClasses_dreadnoughtHasHighestHull() {
        List<ShipClass> shipClasses = gameService.getDefaultShipClasses();

        ShipClass maxHull = shipClasses.stream()
                .max((a, b) -> Integer.compare(a.getHullPoints(), b.getHullPoints()))
                .orElseThrow();

        assertEquals("dreadnought", maxHull.getId());
        assertEquals(80, maxHull.getHullPoints());
    }

    @Test
    void getDefaultShipClasses_scoutHasHighestSpeed() {
        List<ShipClass> shipClasses = gameService.getDefaultShipClasses();

        ShipClass maxSpeed = shipClasses.stream()
                .max((a, b) -> Integer.compare(a.getMaxSpeed(), b.getMaxSpeed()))
                .orElseThrow();

        assertEquals("scout", maxSpeed.getId());
        assertEquals(8, maxSpeed.getMaxSpeed());
    }

    @Test
    void getDefaultShipClasses_allHaveNonEmptyWeaponSlots() {
        List<ShipClass> shipClasses = gameService.getDefaultShipClasses();

        for (ShipClass shipClass : shipClasses) {
            assertFalse(shipClass.getWeaponSlots().isEmpty(),
                    "Ship class " + shipClass.getId() + " should have at least one weapon slot");
        }
    }

    @Test
    void getDefaultShipClasses_isIdempotent() {
        List<ShipClass> firstCall = gameService.getDefaultShipClasses();
        List<ShipClass> secondCall = gameService.getDefaultShipClasses();

        assertEquals(5, firstCall.size());
        assertEquals(5, secondCall.size());
        assertNotSame(firstCall, secondCall, "Each call should return a new list instance");
    }
}
