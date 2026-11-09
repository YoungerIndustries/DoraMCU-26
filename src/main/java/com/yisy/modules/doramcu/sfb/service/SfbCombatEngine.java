package com.yisy.modules.doramcu.sfb.service;

import com.yisy.modules.doramcu.sfb.model.*;
import com.yisy.modules.doramcu.util.DiceUtil;

import java.util.*;

/**
 * Orchestrates the 32-impulse turn loop in SFB.
 * <p>
 * Coordinates energy allocation phase, impulse procedure (movement, weapons,
 * seeking weapons, damage), and turn completion.
 * </p>
 */
public class SfbCombatEngine {

    private final ImpulseManager impulseManager;
    private final EnergyAllocationService energyAllocationService;
    private final MovementService movementService;
    private final WeaponsFireService weaponsFireService;
    private final DamageAllocationService damageAllocationService;

    public SfbCombatEngine() {
        this.impulseManager = new ImpulseManager();
        this.energyAllocationService = new EnergyAllocationService();
        this.movementService = new MovementService();
        this.weaponsFireService = new WeaponsFireService();
        this.damageAllocationService = new DamageAllocationService();
    }

    public ImpulseManager getImpulseManager() { return impulseManager; }
    public EnergyAllocationService getEnergyAllocationService() { return energyAllocationService; }
    public MovementService getMovementService() { return movementService; }
    public WeaponsFireService getWeaponsFireService() { return weaponsFireService; }
    public DamageAllocationService getDamageAllocationService() { return damageAllocationService; }

    /**
     * Run a complete turn for the scenario.
     * Returns true if the battle is over after this turn.
     */
    public boolean runTurn(SfbScenario scenario) {
        scenario.startTurn();

        // Energy allocation phase
        processEnergyAllocation(scenario);

        // Impulse procedure
        for (int impulse = 1; impulse <= 32; impulse++) {
            scenario.nextImpulse();
            processImpulse(scenario, impulse);

            if (scenario.isBattleOver()) {
                return true;
            }
        }

        // Turn complete
        scenario.setCurrentPhase(SfbScenario.ScenarioPhase.TURN_COMPLETE);
        scenario.logEvent(new SfbEvent(scenario.getCurrentTurn(), 0,
                SfbEvent.EventType.TURN_COMPLETED, "Turn " + scenario.getCurrentTurn() + " completed", null));

        return scenario.isBattleOver();
    }

    /**
     * Process the energy allocation phase.
     */
    private void processEnergyAllocation(SfbScenario scenario) {
        for (SfbShip ship : scenario.getShips()) {
            if (ship.isInCombat()) {
                SfbEnergyAllocation ea = energyAllocationService.calculateAvailablePower(ship);
                ship.allocateEnergy(ea);
            }
        }
        scenario.logEvent(new SfbEvent(scenario.getCurrentTurn(), 0,
                SfbEvent.EventType.ENERGY_ALLOCATED, "Energy allocation phase", null));
    }

    /**
     * Process a single impulse.
     */
    private void processImpulse(SfbScenario scenario, int impulse) {
        List<SfbShip> movingShips = impulseManager.getShipsMovingOnImpulse(scenario.getShips(), impulse);

        for (SfbShip ship : movingShips) {
            // Move the ship
            processMovement(scenario, ship, impulse);

            // Fire weapons
            processWeaponsFire(scenario, ship, impulse);
        }

        // Update seeking weapons
        processSeekingWeapons(scenario, impulse);
    }

    /**
     * Process movement for a ship.
     */
    private void processMovement(SfbScenario scenario, SfbShip ship, int impulse) {
        if (!ship.isInCombat()) return;

        // Simple movement: move one hex in current facing
        movementService.moveOneHex(ship);
        scenario.logEvent(new SfbEvent(scenario.getCurrentTurn(), impulse,
                SfbEvent.EventType.SHIP_MOVED, ship.getName() + " moved", ship.getId()));
    }

    /**
     * Process weapons fire for a ship.
     */
    private void processWeaponsFire(SfbScenario scenario, SfbShip ship, int impulse) {
        if (!ship.isInCombat()) return;

        // Find targets (ships on the opposite side)
        List<SfbShip> targets = findTargets(scenario, ship);
        
        // Use WeaponsFireService for resolution
        List<WeaponsFireService.FireResult> results = weaponsFireService.resolveAllWeapons(ship, targets);
        
        for (WeaponsFireService.FireResult result : results) {
            scenario.logEvent(new SfbEvent(scenario.getCurrentTurn(), impulse,
                    result.isHit() ? SfbEvent.EventType.WEAPON_HIT : SfbEvent.EventType.WEAPON_MISS,
                    result.getMessage(), ship.getId()));
        }
    }

    /**
     * Find target ships for a given ship.
     */
    private List<SfbShip> findTargets(SfbScenario scenario, SfbShip ship) {
        List<SfbShip> targets = new ArrayList<>();
        int targetSide = 1 - ship.getSide();
        for (SfbShip other : scenario.getShips()) {
            if (other.getSide() == targetSide && other.isInCombat()) {
                targets.add(other);
            }
        }
        return targets;
    }

    /**
     * Resolve a weapon hit against a target.
     */
    public void resolveHit(SfbScenario scenario, SfbShip attacker, SfbShip target,
                           SfbWeaponBank weapon, int rangeBand, int impulse) {
        // Roll to hit
        int hitProb = weapon.getHitProbability(rangeBand);
        int roll = DiceUtil.rollD6();

        if (roll <= hitProb) {
            // Hit!
            int damage = weapon.getBaseDamage(rangeBand);
            if (weapon.isOverloaded()) {
                damage *= 2;
            }

            int direction = movementService.getDirectionTo(attacker, target);
            int penetrated = target.takeDamage(damage, direction);

            scenario.logEvent(new SfbEvent(scenario.getCurrentTurn(), impulse,
                    SfbEvent.EventType.WEAPON_HIT,
                    attacker.getName() + " hits " + target.getName() + " for " + damage + " damage",
                    attacker.getId()));

            if (penetrated > 0) {
                scenario.logEvent(new SfbEvent(scenario.getCurrentTurn(), impulse,
                        SfbEvent.EventType.DAMAGE_PENETRATED,
                        penetrated + " damage penetrates " + target.getName() + "'s shields",
                        attacker.getId()));
            }

            // Check for crippled/destroyed
            if (target.getSsd().isCrippled() && !target.isCrippled()) {
                target.setCrippled(true);
                scenario.logEvent(new SfbEvent(scenario.getCurrentTurn(), impulse,
                        SfbEvent.EventType.SHIP_CRIPPLED, target.getName() + " is crippled",
                        target.getId()));
            }

            if (target.isDestroyed()) {
                scenario.logEvent(new SfbEvent(scenario.getCurrentTurn(), impulse,
                        SfbEvent.EventType.SHIP_DESTROYED, target.getName() + " is destroyed",
                        target.getId()));
            }
        } else {
            // Miss
            scenario.logEvent(new SfbEvent(scenario.getCurrentTurn(), impulse,
                    SfbEvent.EventType.WEAPON_MISS,
                    attacker.getName() + " misses " + target.getName(),
                    attacker.getId()));
        }
    }

    /**
     * Process seeking weapons (plasma torpedoes, drones).
     */
    private void processSeekingWeapons(SfbScenario scenario, int impulse) {
        for (SfbShip ship : scenario.getShips()) {
            if (!ship.isInCombat()) continue;
            
            List<SfbShip> targets = findTargets(scenario, ship);
            List<WeaponsFireService.FireResult> results = weaponsFireService.updateSeekingWeapons(ship, targets);
            
            for (WeaponsFireService.FireResult result : results) {
                scenario.logEvent(new SfbEvent(scenario.getCurrentTurn(), impulse,
                        SfbEvent.EventType.WEAPON_HIT,
                        result.getMessage(), ship.getId()));
            }
        }
    }

    /**
     * Run the entire battle until one side is eliminated.
     * Returns the winning side (0 or 1), or -1 for stalemate.
     */
    public int runBattle(SfbScenario scenario, int maxTurns) {
        for (int turn = 1; turn <= maxTurns; turn++) {
            boolean over = runTurn(scenario);
            if (over) {
                return determineWinner(scenario);
            }
        }
        return -1; // Stalemate after max turns
    }

    /**
     * Determine the winner of the battle.
     */
    private int determineWinner(SfbScenario scenario) {
        List<SfbShip> side0 = scenario.getActiveShipsBySide(0);
        List<SfbShip> side1 = scenario.getActiveShipsBySide(1);

        if (side0.isEmpty() && side1.isEmpty()) return -1; // Mutual destruction
        if (side0.isEmpty()) return 1;
        if (side1.isEmpty()) return 0;
        return -1; // Should not reach here
    }
}
