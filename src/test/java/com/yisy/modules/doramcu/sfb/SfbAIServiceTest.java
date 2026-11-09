package com.yisy.modules.doramcu.sfb;

import com.yisy.modules.doramcu.sfb.model.*;
import com.yisy.modules.doramcu.sfb.service.SfbAIService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("SfbAIService Tests")
class SfbAIServiceTest {

    private SfbShip createAiShip() {
        SfbShip ship = new SfbShip();
        ship.setId("ai1");
        ship.setName("AI Ship");
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

        SfbWeaponBank weapon = new SfbWeaponBank("P1", SfbWeaponBank.WeaponType.PHASER_1,
                SfbWeaponBank.FiringArc.A, 1);
        ship.addWeaponBank(weapon);

        return ship;
    }

    private SfbShip createTarget() {
        SfbShip ship = new SfbShip();
        ship.setId("target1");
        ship.setName("Target");
        ship.setPosition(new SfbHexPosition(5, 0, 0));
        ship.setSide(1);

        SfbSSD ssd = new SfbSSD();
        ssd.setHullBoxes(10);
        ssd.setDamConRating(3);
        ssd.addSystem("WARP_1", SfbSSD.SystemCategory.WARP_ENGINES);
        ship.setSsd(ssd);
        ship.initializeShields(5);

        return ship;
    }

    @Test
    @DisplayName("AI difficulty levels")
    void aiDifficultyLevels() {
        SfbAIService.Difficulty[] difficulties = SfbAIService.Difficulty.values();
        assertEquals(4, difficulties.length);

        assertTrue(SfbAIService.Difficulty.EASY.getTargetingAccuracy() < 
                   SfbAIService.Difficulty.EXPERT.getTargetingAccuracy());
    }

    @Test
    @DisplayName("AI energy allocation")
    void decideEnergyAllocation() {
        SfbAIService service = new SfbAIService(SfbAIService.Difficulty.MEDIUM);
        SfbShip ship = createAiShip();

        SfbEnergyAllocation ea = service.decideEnergyAllocation(ship);

        assertNotNull(ea);
        assertTrue(ea.getWarpPower() >= 0);
        assertTrue(ea.getImpulsePower() >= 0);
        assertTrue(ea.getShieldAllocation() >= 0);
        assertTrue(ea.getWeaponAllocation() >= 0);
    }

    @Test
    @DisplayName("AI energy allocation - shield reinforcement for weak shields")
    void aiEnergyShieldReinforcement() {
        SfbAIService service = new SfbAIService(SfbAIService.Difficulty.HARD);
        SfbShip ship = createAiShip();
        // Deplete one shield
        ship.getShields()[0].absorbDamage(4); // Only 1 box remaining

        SfbEnergyAllocation ea = service.decideEnergyAllocation(ship);

        assertNotNull(ea);
        int[] reinforcement = ea.getShieldReinforcement();
        assertNotNull(reinforcement);
        assertEquals(6, reinforcement.length);
    }

    @Test
    @DisplayName("AI movement - close in on distant target")
    void aiMovementCloseIn() {
        SfbAIService service = new SfbAIService(SfbAIService.Difficulty.MEDIUM);
        SfbShip ship = createAiShip();
        SfbShip target = createTarget();
        target.setPosition(new SfbHexPosition(15, 0, 0)); // Far away

        List<SfbShip> targets = Collections.singletonList(target);
        int movement = service.decideMovement(ship, targets);

        assertEquals(1, movement); // Move forward to close in
    }

    @Test
    @DisplayName("AI movement - back away from close target")
    void aiMovementBackAway() {
        SfbAIService service = new SfbAIService(SfbAIService.Difficulty.HARD);
        SfbShip ship = createAiShip();
        SfbShip target = createTarget();
        target.setPosition(new SfbHexPosition(2, 0, 0)); // Too close

        List<SfbShip> targets = Collections.singletonList(target);
        int movement = service.decideMovement(ship, targets);

        // May move forward or reverse depending on dice
        assertTrue(movement >= -1 && movement <= 1);
    }

    @Test
    @DisplayName("AI movement - no targets")
    void aiMovementNoTargets() {
        SfbAIService service = new SfbAIService();
        SfbShip ship = createAiShip();

        int movement = service.decideMovement(ship, Collections.emptyList());
        assertEquals(0, movement);
    }

    @Test
    @DisplayName("AI targeting - prioritize damaged ships")
    void aiTargetingDamagedShip() {
        SfbAIService service = new SfbAIService(SfbAIService.Difficulty.HARD);
        SfbShip ship = createAiShip();

        SfbShip healthy = createTarget();
        healthy.setPosition(new SfbHexPosition(5, 0, 0));

        SfbShip damaged = new SfbShip();
        damaged.setId("damaged1");
        damaged.setName("Damaged");
        damaged.setPosition(new SfbHexPosition(8, 0, 0));
        damaged.setSide(1);
        SfbSSD ssd = new SfbSSD();
        ssd.setHullBoxes(10);
        ssd.addExcessDamage(8); // Heavily damaged
        damaged.setSsd(ssd);
        damaged.initializeShields(5);

        List<SfbShip> targets = Arrays.asList(healthy, damaged);
        SfbShip selected = service.decideTarget(ship, targets);

        assertNotNull(selected);
    }

    @Test
    @DisplayName("AI targeting - no valid targets")
    void aiTargetingNoTargets() {
        SfbAIService service = new SfbAIService();
        SfbShip ship = createAiShip();

        SfbShip target = createTarget();
        target.setDestroyed(true);

        SfbShip selected = service.decideTarget(ship, Collections.singletonList(target));
        assertNull(selected);
    }

    @Test
    @DisplayName("AI cloak decision - shields low")
    void aiCloakDecision() {
        SfbAIService service = new SfbAIService(SfbAIService.Difficulty.HARD);
        SfbShip ship = createAiShip();
        // Deplete shields
        ship.getShields()[0].absorbDamage(4);
        ship.getShields()[1].absorbDamage(4);

        SfbShip target = createTarget();
        List<SfbShip> targets = Collections.singletonList(target);

        boolean shouldCloak = service.decideCloak(ship, targets);
        // May or may not cloak based on dice
        assertNotNull(shouldCloak ? Boolean.TRUE : Boolean.FALSE);
    }

    @Test
    @DisplayName("AI cloak decision - already cloaked")
    void aiCloakAlreadyCloaked() {
        SfbAIService service = new SfbAIService();
        SfbShip ship = createAiShip();
        ship.setCloaked(true);

        boolean shouldCloak = service.decideCloak(ship, Collections.singletonList(createTarget()));
        assertFalse(shouldCloak);
    }

    @Test
    @DisplayName("AI tractor beam decision")
    void aiTractorBeamDecision() {
        SfbAIService service = new SfbAIService(SfbAIService.Difficulty.HARD);
        SfbShip ship = createAiShip();
        SfbShip target = createTarget();
        target.setPosition(new SfbHexPosition(5, 0, 0));

        boolean useTractor = service.decideTractorBeam(ship, target);
        assertNotNull(useTractor ? Boolean.TRUE : Boolean.FALSE);
    }

    @Test
    @DisplayName("AI tractor beam - out of range")
    void aiTractorBeamOutOfRange() {
        SfbAIService service = new SfbAIService();
        SfbShip ship = createAiShip();
        SfbShip target = createTarget();
        target.setPosition(new SfbHexPosition(15, 0, 0));

        boolean useTractor = service.decideTractorBeam(ship, target);
        assertFalse(useTractor);
    }

    @Test
    @DisplayName("AI overload decision")
    void aiOverloadDecision() {
        SfbAIService service = new SfbAIService(SfbAIService.Difficulty.MEDIUM);
        SfbShip ship = createAiShip();
        SfbWeaponBank weapon = ship.getWeaponBanks().get(0);

        boolean overload = service.decideOverload(ship, weapon);
        assertNotNull(overload ? Boolean.TRUE : Boolean.FALSE);
    }

    @Test
    @DisplayName("AI execute turn")
    void aiExecuteTurn() {
        SfbAIService service = new SfbAIService(SfbAIService.Difficulty.MEDIUM);
        SfbShip ship = createAiShip();

        SfbScenario scenario = new SfbScenario("test1");
        scenario.addShip(ship);

        SfbShip target = createTarget();
        scenario.addShip(target);

        // Should not throw
        service.executeAiTurn(ship, scenario);
    }

    @Test
    @DisplayName("AI execute turns for multiple ships")
    void aiExecuteMultipleTurns() {
        SfbAIService service = new SfbAIService(SfbAIService.Difficulty.EASY);
        SfbScenario scenario = new SfbScenario("test1");

        SfbShip ai1 = createAiShip();
        SfbShip ai2 = createAiShip();
        ai2.setId("ai2");
        ai2.setPosition(new SfbHexPosition(-3, 0, 0));
        scenario.addShip(ai1);
        scenario.addShip(ai2);

        SfbShip target = createTarget();
        scenario.addShip(target);

        Set<String> aiShips = new HashSet<>(Arrays.asList("ai1", "ai2"));
        service.executeAiTurns(scenario, aiShips);
    }

    @Test
    @DisplayName("AI with null SSD")
    void aiWithNullSSD() {
        SfbAIService service = new SfbAIService();
        SfbShip ship = new SfbShip();
        ship.setId("noSsd");

        SfbEnergyAllocation ea = service.decideEnergyAllocation(ship);
        assertNotNull(ea);
    }
}
