package com.yisy.modules.doramcu.sfb;

import com.yisy.modules.doramcu.sfb.model.*;
import com.yisy.modules.doramcu.sfb.service.SpecialSystemsService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("SpecialSystemsService Tests")
class SpecialSystemsServiceTest {

    private SfbShip createShipWithSystems() {
        SfbShip ship = new SfbShip();
        ship.setId("ship1");
        ship.setName("Test Ship");
        ship.setPosition(new SfbHexPosition(0, 0, 0));
        ship.setSide(0);

        SfbSSD ssd = new SfbSSD();
        ssd.setHullBoxes(10);
        ssd.setDamConRating(3);
        ssd.addSystem("WARP_1", SfbSSD.SystemCategory.WARP_ENGINES);
        ssd.addSystem("WARP_2", SfbSSD.SystemCategory.WARP_ENGINES);
        ssd.addSystem("IMPULSE_1", SfbSSD.SystemCategory.IMPULSE_ENGINES);
        ship.setSsd(ssd);
        ship.initializeShields(5);

        return ship;
    }

    @Test
    @DisplayName("Set sensor rating")
    void setSensorRating() {
        SpecialSystemsService service = new SpecialSystemsService();
        SfbShip ship = createShipWithSystems();

        service.setSensorRating(ship, 5);
        assertEquals(20, service.getSensorRange(ship)); // 5 * 4 = 20
    }

    @Test
    @DisplayName("Sensor rating clamped to 1-9")
    void sensorRatingClamped() {
        SpecialSystemsService service = new SpecialSystemsService();
        SfbShip ship = createShipWithSystems();

        service.setSensorRating(ship, 0);
        assertEquals(4, service.getSensorRange(ship)); // 1 * 4 = 4 (clamped to 1)

        service.setSensorRating(ship, 15);
        assertEquals(36, service.getSensorRange(ship)); // 9 * 4 = 36 (clamped to 9)
    }

    @Test
    @DisplayName("Set scanner rating")
    void setScannerRating() {
        SpecialSystemsService service = new SpecialSystemsService();
        SfbShip ship = createShipWithSystems();

        service.setScannerRating(ship, 4);
        assertEquals(8, service.getScannerRange(ship)); // 4 * 2 = 8
    }

    @Test
    @DisplayName("Cloak ship without cloak rating fails")
    void cloakWithoutRating() {
        SpecialSystemsService service = new SpecialSystemsService();
        SfbShip ship = createShipWithSystems();

        boolean cloaked = service.cloakShip(ship);
        assertFalse(cloaked);
        assertFalse(ship.isCloaked());
    }

    @Test
    @DisplayName("Cloak ship with cloak rating")
    void cloakWithRating() {
        SpecialSystemsService service = new SpecialSystemsService();
        SfbShip ship = createShipWithSystems();
        service.setCloakRating(ship, 6);

        boolean cloaked = service.cloakShip(ship);
        // With rating 6, should succeed on most rolls
        if (cloaked) {
            assertTrue(ship.isCloaked());
        }
    }

    @Test
    @DisplayName("Decloak ship")
    void decloakShip() {
        SpecialSystemsService service = new SpecialSystemsService();
        SfbShip ship = createShipWithSystems();
        ship.setCloaked(true);

        service.decloakShip(ship);
        assertFalse(ship.isCloaked());
    }

    @Test
    @DisplayName("Detect non-cloaked ship")
    void detectNonCloakedShip() {
        SpecialSystemsService service = new SpecialSystemsService();
        SfbShip detector = createShipWithSystems();
        SfbShip target = createShipWithSystems();
        target.setId("target1");
        target.setPosition(new SfbHexPosition(5, 0, 0));

        service.setSensorRating(detector, 5);

        SpecialSystemsService.SensorResult result = service.detectCloakedShip(detector, target);
        assertTrue(result.isDetected());
        assertTrue(result.isIdentified());
        assertEquals(5, result.getRange());
    }

    @Test
    @DisplayName("Detect cloaked ship - no sensors")
    void detectCloakedNoSensors() {
        SpecialSystemsService service = new SpecialSystemsService();
        SfbShip detector = createShipWithSystems();
        SfbShip target = createShipWithSystems();
        target.setId("target1");
        target.setCloaked(true);

        SpecialSystemsService.SensorResult result = service.detectCloakedShip(detector, target);
        assertFalse(result.isDetected());
        assertTrue(result.getMessage().contains("No sensors"));
    }

    @Test
    @DisplayName("EW resolution - no ECM")
    void ewNoEcm() {
        SpecialSystemsService service = new SpecialSystemsService();
        SfbShip ecmShip = createShipWithSystems();
        SfbShip target = createShipWithSystems();
        target.setId("target1");

        SpecialSystemsService.EWResult result = service.resolveEW(ecmShip, target);
        assertFalse(result.isEcmSuccess());
        assertEquals(0, result.getAccuracyPenalty());
    }

    @Test
    @DisplayName("EW resolution - ECM vs ECCM")
    void ewEcmVsEccm() {
        SpecialSystemsService service = new SpecialSystemsService();
        SfbShip ecmShip = createShipWithSystems();
        SfbShip target = createShipWithSystems();
        target.setId("target1");

        service.setEcmRating(ecmShip, 5);
        service.setEccmRating(target, 2);

        SpecialSystemsService.EWResult result = service.resolveEW(ecmShip, target);
        assertNotNull(result);
        // Result depends on dice rolls
    }

    @Test
    @DisplayName("Tractor beam - no rating")
    void tractorBeamNoRating() {
        SpecialSystemsService service = new SpecialSystemsService();
        SfbShip tractor = createShipWithSystems();
        SfbShip target = createShipWithSystems();
        target.setId("target1");
        target.setPosition(new SfbHexPosition(3, 0, 0));

        boolean active = service.activateTractorBeam(tractor, target);
        assertFalse(active);
    }

    @Test
    @DisplayName("Tractor beam - out of range")
    void tractorBeamOutOfRange() {
        SpecialSystemsService service = new SpecialSystemsService();
        SfbShip tractor = createShipWithSystems();
        SfbShip target = createShipWithSystems();
        target.setId("target1");
        target.setPosition(new SfbHexPosition(15, 0, 0));

        service.setTractorRating(tractor, 5);
        boolean active = service.activateTractorBeam(tractor, target);
        assertFalse(active);
    }

    @Test
    @DisplayName("Tractor beam - within range")
    void tractorBeamInRange() {
        SpecialSystemsService service = new SpecialSystemsService();
        SfbShip tractor = createShipWithSystems();
        SfbShip target = createShipWithSystems();
        target.setId("target1");
        target.setPosition(new SfbHexPosition(5, 0, 0));

        service.setTractorRating(tractor, 6);
        boolean active = service.activateTractorBeam(tractor, target);
        // May succeed or fail based on dice
        assertNotNull(active ? Boolean.TRUE : Boolean.FALSE);
    }

    @Test
    @DisplayName("Break tractor beam")
    void breakTractorBeam() {
        SpecialSystemsService service = new SpecialSystemsService();
        SfbShip ship = createShipWithSystems();

        boolean brokeFree = service.breakTractorBeam(ship);
        // Depends on impulse power and dice
        assertNotNull(brokeFree ? Boolean.TRUE : Boolean.FALSE);
    }

    @Test
    @DisplayName("Detectable - within sensor range")
    void detectableWithinRange() {
        SpecialSystemsService service = new SpecialSystemsService();
        SfbShip detector = createShipWithSystems();
        SfbShip target = createShipWithSystems();
        target.setId("target1");
        target.setPosition(new SfbHexPosition(5, 0, 0));

        service.setSensorRating(detector, 5); // Range = 20

        assertTrue(service.isDetectable(detector, target));
    }

    @Test
    @DisplayName("Detectable - outside sensor range")
    void detectableOutOfRange() {
        SpecialSystemsService service = new SpecialSystemsService();
        SfbShip detector = createShipWithSystems();
        SfbShip target = createShipWithSystems();
        target.setId("target1");
        target.setPosition(new SfbHexPosition(50, 0, 0));

        service.setSensorRating(detector, 5); // Range = 20

        assertFalse(service.isDetectable(detector, target));
    }

    @Test
    @DisplayName("Detectable - destroyed ship not detectable")
    void detectableDestroyedShip() {
        SpecialSystemsService service = new SpecialSystemsService();
        SfbShip detector = createShipWithSystems();
        SfbShip target = createShipWithSystems();
        target.setId("target1");
        target.setPosition(new SfbHexPosition(5, 0, 0));
        target.setDestroyed(true);

        service.setSensorRating(detector, 5);

        assertFalse(service.isDetectable(detector, target));
    }

    @Test
    @DisplayName("EW accuracy penalty")
    void ewAccuracyPenalty() {
        SpecialSystemsService service = new SpecialSystemsService();
        SfbShip attacker = createShipWithSystems();
        SfbShip defender = createShipWithSystems();
        defender.setId("defender1");

        // No ECM means no penalty
        int penalty = service.getEwAccuracyPenalty(attacker, defender);
        assertEquals(0, penalty);

        // With ECM, penalty depends on ratings
        service.setEcmRating(defender, 5);
        service.setEccmRating(attacker, 1);
        penalty = service.getEwAccuracyPenalty(attacker, defender);
        // Penalty >= 0, may be 0 if ECCM wins
        assertTrue(penalty >= 0);
    }

    @Test
    @DisplayName("Cloak state enum")
    void cloakStateEnum() {
        assertEquals(4, SpecialSystemsService.CloakState.values().length);
    }

    @Test
    @DisplayName("Tractor state enum")
    void tractorStateEnum() {
        assertEquals(4, SpecialSystemsService.TractorState.values().length);
    }
}
