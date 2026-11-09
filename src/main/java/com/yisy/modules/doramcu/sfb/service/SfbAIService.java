package com.yisy.modules.doramcu.sfb.service;

import com.yisy.modules.doramcu.sfb.model.*;
import com.yisy.modules.doramcu.util.DiceUtil;

import java.util.*;

/**
 * AI opponent for SFB single-player mode.
 * <p>
 * Uses heuristic-based decision making for energy allocation, movement,
 * targeting, and special system usage.
 * </p>
 */
public class SfbAIService {

    /**
     * AI difficulty level.
     */
    public enum Difficulty {
        EASY(0.3, 0.5, 0.4),
        MEDIUM(0.6, 0.7, 0.6),
        HARD(0.8, 0.9, 0.8),
        EXPERT(0.95, 0.95, 0.95);

        private final double targetingAccuracy;
        private final double energyEfficiency;
        private final double tacticalAwareness;

        Difficulty(double targetingAccuracy, double energyEfficiency, double tacticalAwareness) {
            this.targetingAccuracy = targetingAccuracy;
            this.energyEfficiency = energyEfficiency;
            this.tacticalAwareness = tacticalAwareness;
        }

        public double getTargetingAccuracy() { return targetingAccuracy; }
        public double getEnergyEfficiency() { return energyEfficiency; }
        public double getTacticalAwareness() { return tacticalAwareness; }
    }

    private final Difficulty difficulty;
    private final SpecialSystemsService specialSystemsService;

    public SfbAIService() {
        this(Difficulty.MEDIUM);
    }

    public SfbAIService(Difficulty difficulty) {
        this.difficulty = difficulty;
        this.specialSystemsService = new SpecialSystemsService();
    }

    public SpecialSystemsService getSpecialSystemsService() {
        return specialSystemsService;
    }

    /**
     * Make energy allocation decisions for an AI-controlled ship.
     */
    public SfbEnergyAllocation decideEnergyAllocation(SfbShip ship) {
        SfbEnergyAllocation ea = new SfbEnergyAllocation();

        // Calculate available power from SSD
        SfbSSD ssd = ship.getSsd();
        if (ssd == null) return ea;

        int warpPower = ssd.getAvailableWarpPower() * 2;
        int impulsePower = ssd.getAvailableImpulsePower();
        int auxPower = ssd.getAvailableAuxiliaryPower();

        ea.setWarpPower(warpPower);
        ea.setImpulsePower(impulsePower);
        ea.setAuxiliaryPower(auxPower);

        int totalPower = warpPower + impulsePower + auxPower;

        // AI energy allocation strategy based on difficulty
        double efficiency = difficulty.getEnergyEfficiency();

        // Allocate to shields first (priority based on current shield state)
        int shieldAllocation = (int) (totalPower * 0.4 * efficiency);
        ea.setShieldAllocation(shieldAllocation);

        // Allocate to weapons
        int weaponAllocation = (int) (totalPower * 0.35 * efficiency);
        ea.setWeaponAllocation(weaponAllocation);

        // Allocate to special systems
        int specialAllocation = (int) (totalPower * 0.15 * efficiency);
        ea.setSpecialSystemAllocation(specialAllocation);

        // Allocate to EW if available
        int ewAllocation = (int) (totalPower * 0.1 * efficiency);
        ea.setEwAllocation(ewAllocation);

        // Directed reinforcement: reinforce the weakest shield
        int[] reinforcement = new int[6];
        int minProtection = Integer.MAX_VALUE;
        int minDirection = 0;

        for (int i = 0; i < 6; i++) {
            SfbShield shield = ship.getShields()[i];
            if (shield != null) {
                int protection = shield.getTotalProtection();
                if (protection < minProtection) {
                    minProtection = protection;
                    minDirection = i;
                }
            }
        }

        // Reinforce weakest shield if it's critically low
        if (minProtection < 3) {
            reinforcement[minDirection] = Math.min(3, specialAllocation);
        }
        ea.setShieldReinforcement(reinforcement);

        return ea;
    }

    /**
     * Decide movement for an AI-controlled ship.
     * @return number of hexes to move (positive = forward, negative = reverse)
     */
    public int decideMovement(SfbShip ship, List<SfbShip> targets) {
        if (targets.isEmpty()) return 0;

        // Find closest target
        SfbShip closest = null;
        int minDistance = Integer.MAX_VALUE;

        for (SfbShip target : targets) {
            int distance = ship.getPosition().distanceTo(target.getPosition());
            if (distance < minDistance) {
                minDistance = distance;
                closest = target;
            }
        }

        if (closest == null) return 0;

        // Tactical decision based on distance
        double awareness = difficulty.getTacticalAwareness();

        if (minDistance > 10) {
            // Close in on target
            return 1; // Move forward
        } else if (minDistance < 3) {
            // Too close, back away
            if (DiceUtil.rollD6() <= awareness * 6) {
                return -1; // Reverse
            }
        }

        // Maintain position or slight adjustment
        return DiceUtil.rollD6() <= 3 ? 1 : 0;
    }

    /**
     * Decide which target to fire at.
     * @return the selected target, or null if no valid target
     */
    public SfbShip decideTarget(SfbShip ship, List<SfbShip> targets) {
        if (targets.isEmpty()) return null;

        // AI targeting strategy
        double accuracy = difficulty.getTargetingAccuracy();

        // Prioritize damaged ships
        SfbShip mostDamaged = null;
        double maxDamage = 0;

        for (SfbShip target : targets) {
            if (target.isDestroyed() || target.isCrippled()) continue;

            // Calculate damage percentage
            SfbSSD ssd = target.getSsd();
            if (ssd != null) {
                double damagePct = (double) ssd.getExcessDamage() / Math.max(1, ssd.getHullBoxes());
                if (damagePct > maxDamage) {
                    maxDamage = damagePct;
                    mostDamaged = target;
                }
            }
        }

        // If AI is smart enough, target the most damaged ship
        if (mostDamaged != null && DiceUtil.rollD6() <= accuracy * 6) {
            return mostDamaged;
        }

        // Otherwise, target the closest ship
        SfbShip closest = null;
        int minDistance = Integer.MAX_VALUE;

        for (SfbShip target : targets) {
            if (target.isDestroyed() || target.isCrippled()) continue;
            int distance = ship.getPosition().distanceTo(target.getPosition());
            if (distance < minDistance) {
                minDistance = distance;
                closest = target;
            }
        }

        return closest;
    }

    /**
     * Decide whether to use special systems.
     */
    public boolean decideCloak(SfbShip ship, List<SfbShip> targets) {
        // Cloak when taking damage or when out of weapon range
        if (ship.isCloaked()) return false;

        SfbSSD ssd = ship.getSsd();
        if (ssd == null) return false;

        // Check if shields are low
        for (SfbShield shield : ship.getShields()) {
            if (shield != null && shield.getTotalProtection() < 2) {
                return DiceUtil.rollD6() <= difficulty.getTacticalAwareness() * 6;
            }
        }

        return false;
    }

    /**
     * Decide whether to use tractor beam.
     */
    public boolean decideTractorBeam(SfbShip ship, SfbShip target) {
        if (target == null) return false;

        int distance = ship.getPosition().distanceTo(target.getPosition());
        if (distance > 12) return false;

        // Use tractor beam on damaged ships to prevent escape
        SfbSSD targetSsd = target.getSsd();
        if (targetSsd != null && targetSsd.getExcessDamage() > targetSsd.getHullBoxes() / 2) {
            return DiceUtil.rollD6() <= difficulty.getTacticalAwareness() * 6;
        }

        return false;
    }

    /**
     * Decide whether to overload weapons.
     */
    public boolean decideOverload(SfbShip ship, SfbWeaponBank weapon) {
        // Overload when target is close and heavily damaged
        return DiceUtil.rollD6() <= difficulty.getTargetingAccuracy() * 6;
    }

    /**
     * Execute a full AI turn for a ship.
     */
    public void executeAiTurn(SfbShip ship, SfbScenario scenario) {
        // Get targets
        List<SfbShip> targets = new ArrayList<>();
        int targetSide = 1 - ship.getSide();
        for (SfbShip other : scenario.getShips()) {
            if (other.getSide() == targetSide && other.isInCombat()) {
                targets.add(other);
            }
        }

        if (targets.isEmpty()) return;

        // Energy allocation
        SfbEnergyAllocation ea = decideEnergyAllocation(ship);
        ship.allocateEnergy(ea);

        // Special systems decisions
        if (decideCloak(ship, targets)) {
            specialSystemsService.cloakShip(ship);
        }

        // Movement decision
        int movement = decideMovement(ship, targets);
        if (movement > 0) {
            ship.move(movement);
        } else if (movement < 0) {
            ship.turn(3); // Turn around
            ship.move(Math.abs(movement));
        }

        // Targeting decision
        SfbShip target = decideTarget(ship, targets);
        if (target != null) {
            int direction = ship.getPosition().relativeDirection(target.getPosition());

            // Fire weapons
            for (SfbWeaponBank weapon : ship.getWeaponBanks()) {
                if (weapon.isOperational() && weapon.canFireAt(ship.getPosition().getFacing(), direction)) {
                    boolean overload = decideOverload(ship, weapon);
                    weapon.setOverloaded(overload);
                }
            }

            ship.fireWeapons(direction);
        }
    }

    /**
     * Execute AI turns for all AI-controlled ships in a scenario.
     */
    public void executeAiTurns(SfbScenario scenario, Set<String> aiShipIds) {
        for (SfbShip ship : scenario.getShips()) {
            if (aiShipIds.contains(ship.getId()) && ship.isInCombat()) {
                executeAiTurn(ship, scenario);
            }
        }
    }
}
