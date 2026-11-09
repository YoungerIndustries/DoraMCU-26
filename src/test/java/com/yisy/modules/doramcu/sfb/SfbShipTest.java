package com.yisy.modules.doramcu.sfb;

import com.yisy.modules.doramcu.sfb.model.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("SfbShip Tests")
class SfbShipTest {

    private SfbShip createTestShip() {
        SfbShip ship = new SfbShip();
        ship.setId("ship1");
        ship.setName("USS Enterprise");
        ship.setEmpire(new SfbEmpire("Federation", "United Federation of Planets", "Alpha Octant"));
        ship.setShipClass("CA");
        ship.setPosition(new SfbHexPosition(0, 0, 0));
        ship.setSpeed(16);
        ship.setSide(0);

        SfbSSD ssd = new SfbSSD();
        ssd.setHullBoxes(10);
        ssd.setDamConRating(3);
        ssd.addSystem("WARP_1", SfbSSD.SystemCategory.WARP_ENGINES);
        ssd.addSystem("WARP_2", SfbSSD.SystemCategory.WARP_ENGINES);
        ssd.addSystem("CTRL_1", SfbSSD.SystemCategory.CONTROL_SPACES);
        ship.setSsd(ssd);

        ship.initializeShields(5);

        SfbEnergyAllocation ea = new SfbEnergyAllocation();
        ea.setWarpPower(10);
        ship.setEnergyAllocation(ea);

        return ship;
    }

    @Test
    @DisplayName("Constructor initializes default values")
    void constructorInitializesDefaults() {
        SfbShip ship = new SfbShip();
        assertEquals(16, ship.getSpeed());
        assertEquals(1, ship.getTurnMode());
        assertEquals(0, ship.getSide());
        assertFalse(ship.isCloaked());
        assertFalse(ship.isCrippled());
        assertFalse(ship.isDestroyed());
    }

    @Test
    @DisplayName("Speed is clamped to 1-32")
    void speedClamped() {
        SfbShip ship = new SfbShip();
        ship.setSpeed(0);
        assertEquals(1, ship.getSpeed());
        ship.setSpeed(50);
        assertEquals(32, ship.getSpeed());
    }

    @Test
    @DisplayName("Turn mode is clamped to 1-3")
    void turnModeClamped() {
        SfbShip ship = new SfbShip();
        ship.setTurnMode(0);
        assertEquals(1, ship.getTurnMode());
        ship.setTurnMode(5);
        assertEquals(3, ship.getTurnMode());
    }

    @Test
    @DisplayName("Initialize shields creates 6 shields")
    void initializeShields() {
        SfbShip ship = new SfbShip();
        ship.initializeShields(5);
        assertEquals(6, ship.getShields().length);
        for (SfbShield shield : ship.getShields()) {
            assertEquals(5, shield.getTotalBoxes());
        }
    }

    @Test
    @DisplayName("Move updates position")
    void moveUpdatesPosition() {
        SfbShip ship = createTestShip();
        ship.move(3);
        assertEquals(3, ship.getPosition().getQ());
    }

    @Test
    @DisplayName("Crippled ship cannot move")
    void crippledShipCannotMove() {
        SfbShip ship = createTestShip();
        ship.setCrippled(true);
        ship.move(3);
        assertEquals(0, ship.getPosition().getQ());
    }

    @Test
    @DisplayName("Destroyed ship cannot move")
    void destroyedShipCannotMove() {
        SfbShip ship = createTestShip();
        ship.setDestroyed(true);
        ship.move(3);
        assertEquals(0, ship.getPosition().getQ());
    }

    @Test
    @DisplayName("Fire weapons returns operational weapons in arc")
    void fireWeapons() {
        SfbShip ship = createTestShip();
        ship.addWeaponBank(new SfbWeaponBank("P1", SfbWeaponBank.WeaponType.PHASER_1,
                SfbWeaponBank.FiringArc.F, 1));
        ship.addWeaponBank(new SfbWeaponBank("P2", SfbWeaponBank.WeaponType.PHASER_1,
                SfbWeaponBank.FiringArc.L, 1));
        List<SfbWeaponBank> fired = ship.fireWeapons(0);
        assertEquals(1, fired.size()); // Only forward-firing weapon
    }

    @Test
    @DisplayName("Crippled ship cannot fire")
    void crippledShipCannotFire() {
        SfbShip ship = createTestShip();
        ship.addWeaponBank(new SfbWeaponBank("P1", SfbWeaponBank.WeaponType.PHASER_1,
                SfbWeaponBank.FiringArc.A, 1));
        ship.setCrippled(true);
        List<SfbWeaponBank> fired = ship.fireWeapons(0);
        assertTrue(fired.isEmpty());
    }

    @Test
    @DisplayName("Take damage passes through shields")
    void takeDamageThroughShields() {
        SfbShip ship = createTestShip();
        int penetrated = ship.takeDamage(3, 0);
        assertEquals(2, ship.getShields()[0].getBoxesRemaining()); // 5 - 3 = 2
        assertEquals(0, penetrated);
    }

    @Test
    @DisplayName("Take damage penetrates when shield depleted")
    void takeDamagePenetrates() {
        SfbShip ship = createTestShip();
        int penetrated = ship.takeDamage(7, 0);
        assertEquals(0, ship.getShields()[0].getBoxesRemaining());
        assertEquals(2, penetrated);
    }

    @Test
    @DisplayName("Is in combat when not crippled or destroyed")
    void isInCombat() {
        SfbShip ship = createTestShip();
        assertTrue(ship.isInCombat());
        ship.setCrippled(true);
        assertFalse(ship.isInCombat());
    }

    @Test
    @DisplayName("Launch seeking weapon")
    void launchSeekingWeapon() {
        SfbShip ship = createTestShip();
        SfbSeekingWeapon weapon = new SfbSeekingWeapon("pt1",
                SfbSeekingWeapon.SeekingWeaponType.PLASMA_TORPEDO, 8);
        ship.launchSeekingWeapon(weapon);
        assertEquals(1, ship.getLaunchedWeapons().size());
        assertEquals(ship, weapon.getLaunchingShip());
    }
}

@DisplayName("SfbScenario Tests")
class SfbScenarioTest {

    @Test
    @DisplayName("Constructor initializes scenario")
    void constructorInitializesScenario() {
        SfbScenario scenario = new SfbScenario();
        assertEquals(0, scenario.getCurrentTurn());
        assertEquals(0, scenario.getCurrentImpulse());
        assertEquals(SfbScenario.ScenarioPhase.SETUP, scenario.getCurrentPhase());
        assertTrue(scenario.getShips().isEmpty());
    }

    @Test
    @DisplayName("Add and retrieve ships")
    void addAndRetrieveShips() {
        SfbScenario scenario = new SfbScenario();
        SfbShip ship1 = new SfbShip();
        ship1.setId("ship1");
        ship1.setSide(0);
        SfbShip ship2 = new SfbShip();
        ship2.setId("ship2");
        ship2.setSide(1);
        scenario.addShip(ship1);
        scenario.addShip(ship2);
        assertEquals(2, scenario.getShips().size());
    }

    @Test
    @DisplayName("Get ships by side")
    void getShipsBySide() {
        SfbScenario scenario = new SfbScenario();
        SfbShip ship1 = new SfbShip();
        ship1.setId("ship1");
        ship1.setSide(0);
        SfbShip ship2 = new SfbShip();
        ship2.setId("ship2");
        ship2.setSide(1);
        scenario.addShip(ship1);
        scenario.addShip(ship2);
        assertEquals(1, scenario.getShipsBySide(0).size());
        assertEquals(1, scenario.getShipsBySide(1).size());
    }

    @Test
    @DisplayName("Start turn advances turn number")
    void startTurn() {
        SfbScenario scenario = new SfbScenario();
        scenario.startTurn();
        assertEquals(1, scenario.getCurrentTurn());
        assertEquals(SfbScenario.ScenarioPhase.ENERGY_ALLOCATION, scenario.getCurrentPhase());
    }

    @Test
    @DisplayName("Next impulse advances impulse")
    void nextImpulse() {
        SfbScenario scenario = new SfbScenario();
        scenario.startTurn();
        scenario.nextImpulse();
        assertEquals(1, scenario.getCurrentImpulse());
        assertEquals(SfbScenario.ScenarioPhase.IMPULSE_PROCEDURE, scenario.getCurrentPhase());
    }

    @Test
    @DisplayName("After impulse 32, turn is complete")
    void afterImpulse32() {
        SfbScenario scenario = new SfbScenario();
        scenario.startTurn();
        for (int i = 0; i < 32; i++) {
            scenario.nextImpulse();
        }
        scenario.nextImpulse(); // 33rd impulse
        assertEquals(SfbScenario.ScenarioPhase.TURN_COMPLETE, scenario.getCurrentPhase());
    }

    @Test
    @DisplayName("Battle over when one side has no active ships")
    void battleOver() {
        SfbScenario scenario = new SfbScenario();
        SfbShip ship1 = new SfbShip();
        ship1.setId("ship1");
        ship1.setSide(0);
        SfbShip ship2 = new SfbShip();
        ship2.setId("ship2");
        ship2.setSide(1);
        ship2.setDestroyed(true);
        scenario.addShip(ship1);
        scenario.addShip(ship2);
        assertTrue(scenario.isBattleOver());
    }

    @Test
    @DisplayName("Log event adds to event log")
    void logEvent() {
        SfbScenario scenario = new SfbScenario();
        SfbEvent event = new SfbEvent(1, 1, SfbEvent.EventType.BATTLE_STARTED,
                "Battle started", null);
        scenario.logEvent(event);
        assertEquals(1, scenario.getEventLog().size());
    }

    @Test
    @DisplayName("Remove ship by ID")
    void removeShip() {
        SfbScenario scenario = new SfbScenario();
        SfbShip ship = new SfbShip();
        ship.setId("ship1");
        scenario.addShip(ship);
        scenario.removeShip("ship1");
        assertTrue(scenario.getShips().isEmpty());
    }
}

@DisplayName("SfbSeekingWeapon Tests")
class SfbSeekingWeaponTest {

    @Test
    @DisplayName("Plasma torpedo degrades over distance")
    void plasmaTorpedoDegrades() {
        SfbSeekingWeapon weapon = new SfbSeekingWeapon("pt1",
                SfbSeekingWeapon.SeekingWeaponType.PLASMA_TORPEDO, 8);
        weapon.setTarget(new SfbShip());
        weapon.setPosition(new SfbHexPosition(0, 0, 0));
        for (int i = 0; i < 16; i++) {
            weapon.pursue();
        }
        assertEquals(4, weapon.getCurrentWarhead()); // 8 - (16/4) = 4
    }

    @Test
    @DisplayName("Drone maintains warhead strength")
    void droneMaintainsWarhead() {
        SfbSeekingWeapon weapon = new SfbSeekingWeapon("d1",
                SfbSeekingWeapon.SeekingWeaponType.DRONE, 6);
        weapon.setTarget(new SfbShip());
        weapon.setPosition(new SfbHexPosition(0, 0, 0));
        for (int i = 0; i < 20; i++) {
            weapon.pursue();
        }
        assertEquals(6, weapon.getCurrentWarhead());
    }

    @Test
    @DisplayName("Intercept reduces warhead")
    void interceptReducesWarhead() {
        SfbSeekingWeapon weapon = new SfbSeekingWeapon("pt1",
                SfbSeekingWeapon.SeekingWeaponType.PLASMA_TORPEDO, 8);
        boolean destroyed = weapon.intercept(8);
        assertTrue(destroyed);
        assertFalse(weapon.isActive());
    }

    @Test
    @DisplayName("Deactivate stops weapon")
    void deactivate() {
        SfbSeekingWeapon weapon = new SfbSeekingWeapon("pt1",
                SfbSeekingWeapon.SeekingWeaponType.PLASMA_TORPEDO, 8);
        weapon.deactivate();
        assertFalse(weapon.isActive());
    }
}
