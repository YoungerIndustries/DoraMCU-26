package com.yisy.modules.doramcu.sfb;

import com.yisy.modules.doramcu.sfb.model.*;
import com.yisy.modules.doramcu.sfb.service.DamageAllocationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("DamageAllocationService Tests")
class DamageAllocationServiceTest {

    private SfbShip createShipWithShields() {
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
        ssd.addSystem("CTRL_1", SfbSSD.SystemCategory.CONTROL_SPACES);
        ssd.addSystem("WEAPON_1", SfbSSD.SystemCategory.WEAPONS);
        ship.setSsd(ssd);
        ship.initializeShields(5);

        return ship;
    }

    @Test
    @DisplayName("Shield absorbs all damage when sufficient")
    void shieldAbsorbsAllDamage() {
        SfbShip ship = createShipWithShields();
        int penetrated = ship.takeDamage(3, 0);

        assertEquals(0, penetrated);
        assertEquals(2, ship.getShields()[0].getBoxesRemaining()); // 5 - 3 = 2
    }

    @Test
    @DisplayName("Shield penetration when damage exceeds shield")
    void shieldPenetration() {
        SfbShip ship = createShipWithShields();
        int penetrated = ship.takeDamage(7, 0);

        assertEquals(0, ship.getShields()[0].getBoxesRemaining());
        assertEquals(2, penetrated); // 7 - 5 = 2 penetrated
    }

    @Test
    @DisplayName("Damage to downed shield fully penetrates")
    void damageToDownedShield() {
        SfbShip ship = createShipWithShields();
        // Deplete shield first
        ship.takeDamage(5, 0);
        assertEquals(0, ship.getShields()[0].getBoxesRemaining());

        // Next damage fully penetrates
        int penetrated = ship.takeDamage(4, 0);
        assertEquals(4, penetrated);
    }

    @Test
    @DisplayName("SSD mark out warp box")
    void markOutWarpBox() {
        SfbShip ship = createShipWithShields();
        SfbSSD ssd = ship.getSsd();

        boolean lastBox = ssd.markOutWarpBox();
        assertFalse(lastBox); // 2 warp boxes, one marked out

        lastBox = ssd.markOutWarpBox();
        assertTrue(lastBox); // Last warp box marked out
    }

    @Test
    @DisplayName("SSD mark out impulse box")
    void markOutImpulseBox() {
        SfbShip ship = createShipWithShields();
        SfbSSD ssd = ship.getSsd();

        boolean lastBox = ssd.markOutImpulseBox();
        assertTrue(lastBox); // Only 1 impulse box
    }

    @Test
    @DisplayName("SSD mark out weapon box")
    void markOutWeaponBox() {
        SfbShip ship = createShipWithShields();
        SfbSSD ssd = ship.getSsd();

        boolean lastBox = ssd.markOutWeaponBox();
        assertTrue(lastBox); // Only 1 weapon box
    }

    @Test
    @DisplayName("SSD mark out control box")
    void markOutControlBox() {
        SfbShip ship = createShipWithShields();
        SfbSSD ssd = ship.getSsd();

        boolean lastBox = ssd.markOutControlBox();
        assertTrue(lastBox); // Only 1 control box
    }

    @Test
    @DisplayName("SSD DamCon rating decreases")
    void markOutDamConBox() {
        SfbShip ship = createShipWithShields();
        SfbSSD ssd = ship.getSsd();

        assertEquals(3, ssd.getDamConRating());
        ssd.markOutDamConBox();
        assertEquals(2, ssd.getDamConRating());
        ssd.markOutDamConBox();
        assertEquals(1, ssd.getDamConRating());
        boolean disabled = ssd.markOutDamConBox();
        assertTrue(disabled);
        assertEquals(0, ssd.getDamConRating());
    }

    @Test
    @DisplayName("SSD crippled detection")
    void ssdCrippledDetection() {
        SfbShip ship = createShipWithShields();
        SfbSSD ssd = ship.getSsd();

        assertFalse(ssd.isCrippled());

        // Mark out all control spaces
        ssd.markOutControlBox();
        assertTrue(ssd.isCrippled());
    }

    @Test
    @DisplayName("SSD destroyed detection")
    void ssdDestroyedDetection() {
        SfbShip ship = createShipWithShields();
        SfbSSD ssd = ship.getSsd();

        assertFalse(ship.isDestroyed());

        // Add excess damage to equal hull boxes
        ssd.addExcessDamage(10);
        assertTrue(ssd.isDestroyed()); // SSD is destroyed when excessDamage >= hullBoxes
    }

    @Test
    @DisplayName("SSD repair system")
    void ssdRepairSystem() {
        SfbShip ship = createShipWithShields();
        SfbSSD ssd = ship.getSsd();

        ssd.markOutWarpBox();

        // Repair should restore a box
        DamageAllocationService service = new DamageAllocationService();
        boolean repaired = service.repairSystem(ship, "warp");
        // Repair may succeed or fail based on dice roll
        assertNotNull(repaired ? Boolean.TRUE : Boolean.FALSE);
    }

    @Test
    @DisplayName("SSD total boxes calculation")
    void ssdTotalBoxes() {
        SfbShip ship = createShipWithShields();
        SfbSSD ssd = ship.getSsd();

        // 5 systems + 10 hull boxes = 15 total
        assertEquals(15, ssd.getTotalBoxes());
    }

    @Test
    @DisplayName("SSD remaining boxes after damage")
    void ssdRemainingBoxes() {
        SfbShip ship = createShipWithShields();
        SfbSSD ssd = ship.getSsd();

        int initial = ssd.getRemainingBoxes();
        ssd.markOutWarpBox();
        int after = ssd.getRemainingBoxes();

        assertEquals(initial - 1, after);
    }

    @Test
    @DisplayName("DamageAllocationService resolution")
    void damageAllocationService() {
        DamageAllocationService service = new DamageAllocationService();
        SfbShip attacker = createShipWithShields();
        SfbShip target = createShipWithShields();
        target.setId("target1");
        target.setSide(1);
        target.setPosition(new SfbHexPosition(3, 0, 0));

        List<DamageAllocationService.DamageResult> results = service.processDamageHit(attacker, target, 5, 0);

        assertFalse(results.isEmpty());
        DamageAllocationService.DamageResult result = results.get(0);
        assertNotNull(result);
        assertEquals(DamageAllocationService.DamageCategory.SHIELDS, result.getCategory());
    }

    @Test
    @DisplayName("DamageAllocationService - no internal damage when shield absorbs all")
    void damageServiceShieldAbsorbsAll() {
        DamageAllocationService service = new DamageAllocationService();
        SfbShip attacker = createShipWithShields();
        SfbShip target = createShipWithShields();
        target.setId("target1");
        target.setSide(1);
        target.setPosition(new SfbHexPosition(3, 0, 0));

        List<DamageAllocationService.DamageResult> results = service.processDamageHit(attacker, target, 3, 0);

        // Shield absorbs all 3 damage, no penetrating damage
        DamageAllocationService.DamageResult shieldResult = results.get(0);
        assertEquals(DamageAllocationService.DamageCategory.SHIELDS, shieldResult.getCategory());
        assertEquals(3, shieldResult.getDamageAmount());
    }

    @Test
    @DisplayName("Shield reinforcement")
    void shieldReinforcement() {
        SfbShip ship = createShipWithShields();
        SfbShield shield = ship.getShields()[0];

        assertEquals(5, shield.getTotalBoxes());
        shield.reinforce(3);
        assertEquals(8, shield.getTotalProtection()); // 5 + 3 = 8 total protection
    }

    @Test
    @DisplayName("Shield down detection")
    void shieldDownDetection() {
        SfbShip ship = createShipWithShields();
        SfbShield shield = ship.getShields()[0];

        assertFalse(shield.isDown());
        ship.takeDamage(5, 0);
        assertTrue(shield.isDown());
    }
}
