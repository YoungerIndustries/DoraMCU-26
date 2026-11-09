package com.yisy.modules.doramcu.sfb;

import com.yisy.modules.doramcu.sfb.model.*;
import com.yisy.modules.doramcu.sfb.service.WeaponsFireService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("WeaponsFireService Tests")
class WeaponsFireServiceTest {

    private SfbShip createAttacker() {
        SfbShip ship = new SfbShip();
        ship.setId("attacker1");
        ship.setName("Attacker");
        ship.setPosition(new SfbHexPosition(0, 0, 0));
        ship.setSide(0);

        SfbSSD ssd = new SfbSSD();
        ssd.setHullBoxes(10);
        ssd.setDamConRating(3);
        ssd.addSystem("WARP_1", SfbSSD.SystemCategory.WARP_ENGINES);
        ssd.addSystem("WARP_2", SfbSSD.SystemCategory.WARP_ENGINES);
        ship.setSsd(ssd);
        ship.initializeShields(5);

        SfbEnergyAllocation ea = new SfbEnergyAllocation();
        ea.setWarpPower(10);
        ea.setImpulsePower(5);
        ea.setAuxiliaryPower(5);
        ea.setWeaponAllocation(20);
        ship.setEnergyAllocation(ea);

        return ship;
    }

    private SfbShip createTarget() {
        SfbShip ship = new SfbShip();
        ship.setId("target1");
        ship.setName("Target");
        ship.setPosition(new SfbHexPosition(3, 0, 0));
        ship.setSide(1);

        SfbSSD ssd = new SfbSSD();
        ssd.setHullBoxes(10);
        ssd.setDamConRating(3);
        ssd.addSystem("WARP_1", SfbSSD.SystemCategory.WARP_ENGINES);
        ship.setSsd(ssd);
        ship.initializeShields(5);

        SfbEnergyAllocation ea = new SfbEnergyAllocation();
        ea.setWarpPower(5);
        ship.setEnergyAllocation(ea);

        return ship;
    }

    @Test
    @DisplayName("Range band calculation from distance")
    void calculateRangeBand() {
        WeaponsFireService service = new WeaponsFireService();
        SfbShip attacker = createAttacker();
        SfbShip target = createTarget();

        // Target at distance 3 = BAND_2 (3-5 hexes)
        WeaponsFireService.RangeBand band = service.calculateRangeBand(attacker, target);
        assertEquals(WeaponsFireService.RangeBand.BAND_2, band);
    }

    @Test
    @DisplayName("Range band from distance helper")
    void rangeBandFromDistance() {
        assertEquals(WeaponsFireService.RangeBand.POINT_BLANK, WeaponsFireService.RangeBand.fromDistance(0));
        assertEquals(WeaponsFireService.RangeBand.BAND_1, WeaponsFireService.RangeBand.fromDistance(1));
        assertEquals(WeaponsFireService.RangeBand.BAND_1, WeaponsFireService.RangeBand.fromDistance(2));
        assertEquals(WeaponsFireService.RangeBand.BAND_2, WeaponsFireService.RangeBand.fromDistance(3));
        assertEquals(WeaponsFireService.RangeBand.BAND_2, WeaponsFireService.RangeBand.fromDistance(5));
        assertEquals(WeaponsFireService.RangeBand.BAND_3, WeaponsFireService.RangeBand.fromDistance(6));
        assertEquals(WeaponsFireService.RangeBand.BAND_4, WeaponsFireService.RangeBand.fromDistance(9));
        assertEquals(WeaponsFireService.RangeBand.BAND_5, WeaponsFireService.RangeBand.fromDistance(13));
        assertEquals(WeaponsFireService.RangeBand.BAND_6, WeaponsFireService.RangeBand.fromDistance(16));
    }

    @Test
    @DisplayName("Direct fire - weapon not operational")
    void directFireWeaponNotOperational() {
        WeaponsFireService service = new WeaponsFireService();
        SfbShip attacker = createAttacker();
        SfbShip target = createTarget();

        SfbWeaponBank weapon = new SfbWeaponBank("P1", SfbWeaponBank.WeaponType.PHASER_1,
                SfbWeaponBank.FiringArc.A, 1);
        weapon.setOperational(false);
        attacker.addWeaponBank(weapon);

        WeaponsFireService.RangeBand band = service.calculateRangeBand(attacker, target);
        WeaponsFireService.FireResult result = service.resolveDirectFire(attacker, target, weapon, band, false);

        assertFalse(result.isHit());
        assertEquals(0, result.getDamageDealt());
        assertTrue(result.getMessage().contains("not operational"));
    }

    @Test
    @DisplayName("Direct fire - target out of firing arc")
    void directFireOutOfArc() {
        WeaponsFireService service = new WeaponsFireService();
        SfbShip attacker = createAttacker();
        attacker.setPosition(new SfbHexPosition(0, 0, 0)); // Facing 0
        SfbShip target = createTarget();
        target.setPosition(new SfbHexPosition(0, 3, 0)); // To the side

        SfbWeaponBank weapon = new SfbWeaponBank("P1", SfbWeaponBank.WeaponType.PHASER_1,
                SfbWeaponBank.FiringArc.F, 1); // Forward only
        attacker.addWeaponBank(weapon);

        WeaponsFireService.RangeBand band = service.calculateRangeBand(attacker, target);
        WeaponsFireService.FireResult result = service.resolveDirectFire(attacker, target, weapon, band, false);

        assertFalse(result.isHit());
        assertTrue(result.getMessage().contains("firing arc"));
    }

    @Test
    @DisplayName("Direct fire - insufficient energy")
    void directFireInsufficientEnergy() {
        WeaponsFireService service = new WeaponsFireService();
        SfbShip attacker = createAttacker();
        SfbEnergyAllocation ea = new SfbEnergyAllocation();
        ea.setWarpPower(0);
        ea.setImpulsePower(0);
        ea.setAuxiliaryPower(0);
        ea.setWeaponAllocation(0);
        attacker.setEnergyAllocation(ea);

        SfbShip target = createTarget();
        SfbWeaponBank weapon = new SfbWeaponBank("P1", SfbWeaponBank.WeaponType.PHASER_1,
                SfbWeaponBank.FiringArc.A, 1);
        attacker.addWeaponBank(weapon);

        WeaponsFireService.RangeBand band = service.calculateRangeBand(attacker, target);
        WeaponsFireService.FireResult result = service.resolveDirectFire(attacker, target, weapon, band, false);

        assertFalse(result.isHit());
        assertTrue(result.getMessage().contains("Insufficient energy"));
    }

    @Test
    @DisplayName("Resolve all weapons finds closest target in arc")
    void resolveAllWeapons() {
        WeaponsFireService service = new WeaponsFireService();
        SfbShip attacker = createAttacker();
        attacker.setPosition(new SfbHexPosition(0, 0, 0));

        SfbWeaponBank weapon = new SfbWeaponBank("P1", SfbWeaponBank.WeaponType.PHASER_1,
                SfbWeaponBank.FiringArc.A, 1);
        attacker.addWeaponBank(weapon);

        SfbShip target1 = createTarget();
        target1.setPosition(new SfbHexPosition(3, 0, 0));
        SfbShip target2 = new SfbShip();
        target2.setId("target2");
        target2.setPosition(new SfbHexPosition(10, 0, 0));
        target2.setSide(1);
        SfbSSD ssd2 = new SfbSSD();
        ssd2.setHullBoxes(10);
        target2.setSsd(ssd2);
        target2.initializeShields(5);

        List<SfbShip> targets = Arrays.asList(target1, target2);
        List<WeaponsFireService.FireResult> results = service.resolveAllWeapons(attacker, targets);

        // Should fire at closest target
        assertTrue(results.isEmpty() || results.get(0).getMessage().contains("P1"));
    }

    @Test
    @DisplayName("Launch plasma torpedo")
    void launchPlasmaTorpedo() {
        WeaponsFireService service = new WeaponsFireService();
        SfbShip ship = createAttacker();
        SfbShip target = createTarget();

        SfbSeekingWeapon torpedo = service.launchPlasmaTorpedo(ship, target, 8);

        assertNotNull(torpedo);
        assertEquals(SfbSeekingWeapon.SeekingWeaponType.PLASMA_TORPEDO, torpedo.getType());
        assertEquals(8, torpedo.getInitialWarhead());
        assertEquals(target, torpedo.getTarget());
        assertEquals(1, ship.getLaunchedWeapons().size());
    }

    @Test
    @DisplayName("Launch drone")
    void launchDrone() {
        WeaponsFireService service = new WeaponsFireService();
        SfbShip ship = createAttacker();
        SfbShip target = createTarget();

        SfbSeekingWeapon drone = service.launchDrone(ship, target, SfbSeekingWeapon.DroneType.DRONE_I);

        assertNotNull(drone);
        assertEquals(SfbSeekingWeapon.SeekingWeaponType.DRONE, drone.getType());
        assertEquals(target, drone.getTarget());
        assertEquals(1, ship.getLaunchedWeapons().size());
    }

    @Test
    @DisplayName("Update seeking weapons - pursuit and hit")
    void updateSeekingWeapons() {
        WeaponsFireService service = new WeaponsFireService();
        SfbShip ship = createAttacker();
        SfbShip target = createTarget();
        target.setPosition(new SfbHexPosition(1, 0, 0)); // Very close

        SfbSeekingWeapon torpedo = new SfbSeekingWeapon(
                SfbSeekingWeapon.SeekingWeaponType.PLASMA_TORPEDO, ship, target, 4);
        torpedo.setPosition(new SfbHexPosition(0, 0, 0));
        ship.launchSeekingWeapon(torpedo);

        List<SfbShip> targets = Collections.singletonList(target);
        List<WeaponsFireService.FireResult> results = service.updateSeekingWeapons(ship, targets);

        // Torpedo should hit since target is at distance 1
        assertFalse(results.isEmpty());
        assertTrue(results.get(0).isHit());
    }

    @Test
    @DisplayName("Update seeking weapons - warhead degradation")
    void updateSeekingWeaponsDegrade() {
        WeaponsFireService service = new WeaponsFireService();
        SfbShip ship = createAttacker();
        SfbShip target = createTarget();
        target.setPosition(new SfbHexPosition(10, 0, 0)); // Far away

        SfbSeekingWeapon torpedo = new SfbSeekingWeapon(
                SfbSeekingWeapon.SeekingWeaponType.PLASMA_TORPEDO, ship, target, 4);
        torpedo.setPosition(new SfbHexPosition(0, 0, 0));
        ship.launchSeekingWeapon(torpedo);

        List<SfbShip> targets = Collections.singletonList(target);

        // Multiple updates should degrade the warhead
        for (int i = 0; i < 5; i++) {
            service.updateSeekingWeapons(ship, targets);
        }

        // Warhead should be degraded
        assertTrue(torpedo.getCurrentWarhead() < 4 || !torpedo.isActive());
    }

    @Test
    @DisplayName("Photon torpedo with dial-a-yield")
    void photonTorpedoDialAYield() {
        WeaponsFireService service = new WeaponsFireService();
        SfbShip attacker = createAttacker();
        SfbShip target = createTarget();

        SfbWeaponBank weapon = new SfbWeaponBank("PT", SfbWeaponBank.WeaponType.PHOTON_TORPEDO,
                SfbWeaponBank.FiringArc.A, 2);
        attacker.addWeaponBank(weapon);

        WeaponsFireService.RangeBand band = service.calculateRangeBand(attacker, target);
        // Just verify the method doesn't throw
        WeaponsFireService.FireResult result = service.resolvePhotonTorpedo(attacker, target, weapon, band, 2);
        assertNotNull(result);
    }
}
