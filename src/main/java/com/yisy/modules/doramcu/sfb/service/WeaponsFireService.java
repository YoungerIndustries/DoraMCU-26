package com.yisy.modules.doramcu.sfb.service;

import com.yisy.modules.doramcu.sfb.model.*;
import com.yisy.modules.doramcu.util.DiceUtil;

import java.util.*;

/**
 * Resolves weapons fire in SFB combat.
 * <p>
 * Handles direct-fire weapons (phasers, disruptors, torpedoes) with range band
 * hit probability, damage calculation, overload, and dial-a-yield. Also handles
 * seeking weapon launches (plasma torpedoes, drones).
 * </p>
 */
public class WeaponsFireService {

    /**
     * Range bands for SFB weapons.
     * 0 = point blank (same hex), 1 = 1-2 hexes, 2 = 3-5 hexes, 3 = 6-8 hexes,
     * 4 = 9-12 hexes, 5 = 13-15 hexes, 6 = 16+ hexes.
     */
    public enum RangeBand {
        POINT_BLANK(0, 0, 0),
        BAND_1(1, 1, 2),
        BAND_2(2, 3, 5),
        BAND_3(3, 6, 8),
        BAND_4(4, 9, 12),
        BAND_5(5, 13, 15),
        BAND_6(6, 16, Integer.MAX_VALUE);

        private final int band;
        private final int minRange;
        private final int maxRange;

        RangeBand(int band, int minRange, int maxRange) {
            this.band = band;
            this.minRange = minRange;
            this.maxRange = maxRange;
        }

        public int getBand() { return band; }
        public int getMinRange() { return minRange; }
        public int getMaxRange() { return maxRange; }

        public static RangeBand fromDistance(int distance) {
            for (RangeBand band : values()) {
                if (distance >= band.minRange && distance <= band.maxRange) {
                    return band;
                }
            }
            return BAND_6;
        }
    }

    /**
     * Result of a weapons fire resolution.
     */
    public static class FireResult {
        private final boolean hit;
        private final int damageDealt;
        private final int damagePenetrated;
        private final String message;

        public FireResult(boolean hit, int damageDealt, int damagePenetrated, String message) {
            this.hit = hit;
            this.damageDealt = damageDealt;
            this.damagePenetrated = damagePenetrated;
            this.message = message;
        }

        public boolean isHit() { return hit; }
        public int getDamageDealt() { return damageDealt; }
        public int getDamagePenetrated() { return damagePenetrated; }
        public String getMessage() { return message; }
    }

    /**
     * Calculate the range band between two ships.
     */
    public RangeBand calculateRangeBand(SfbShip attacker, SfbShip target) {
        int distance = attacker.getPosition().distanceTo(target.getPosition());
        return RangeBand.fromDistance(distance);
    }

    /**
     * Resolve a direct-fire weapon shot.
     * @param attacker the firing ship
     * @param target the target ship
     * @param weapon the weapon bank firing
     * @param rangeBand the calculated range band
     * @param overload whether the weapon is overloaded (doubles damage, may cause breakdown)
     * @return the fire result
     */
    public FireResult resolveDirectFire(SfbShip attacker, SfbShip target,
                                         SfbWeaponBank weapon, RangeBand rangeBand,
                                         boolean overload) {
        // Check if weapon can fire
        if (!weapon.isOperational()) {
            return new FireResult(false, 0, 0, "Weapon " + weapon.getId() + " is not operational");
        }

        // Check firing arc
        int direction = attacker.getPosition().relativeDirection(target.getPosition());
        if (!weapon.canFireAt(attacker.getPosition().getFacing(), direction)) {
            return new FireResult(false, 0, 0, "Target out of firing arc for " + weapon.getId());
        }

        // Check energy allocation
        if (attacker.getEnergyAllocation() != null) {
            int cost = weapon.getEnergyCost();
            if (overload) cost *= 2;
            if (!attacker.getEnergyAllocation().hasPower(cost)) {
                return new FireResult(false, 0, 0, "Insufficient energy for " + weapon.getId());
            }
            attacker.getEnergyAllocation().consumePower(cost);
        }

        // Fire the weapon
        if (!weapon.fire()) {
            return new FireResult(false, 0, 0, "Failed to fire " + weapon.getId());
        }

        // Roll to hit
        int hitProb = weapon.getHitProbability(rangeBand.getBand());
        int roll = DiceUtil.rollD6();
        
        if (roll > hitProb) {
            return new FireResult(false, 0, 0, "Miss by " + weapon.getId() + " (rolled " + roll + ", needed " + hitProb + ")");
        }

        // Calculate damage
        int damage = weapon.getBaseDamage(rangeBand.getBand());
        if (overload) {
            damage *= 2;
        }

        // Apply damage to target
        int targetDirection = attacker.getPosition().relativeDirection(target.getPosition());
        int penetrated = target.takeDamage(damage, targetDirection);

        return new FireResult(true, damage, penetrated, 
                "Hit by " + weapon.getId() + " for " + damage + " damage (" + penetrated + " penetrated)");
    }

    /**
     * Resolve photon torpedo fire with dial-a-yield.
     * @param yieldBonus additional power dialled (+0, +2, or +4)
     */
    public FireResult resolvePhotonTorpedo(SfbShip attacker, SfbShip target,
                                            SfbWeaponBank weapon, RangeBand rangeBand,
                                            int yieldBonus) {
        FireResult baseResult = resolveDirectFire(attacker, target, weapon, rangeBand, false);
        
        if (!baseResult.isHit()) {
            return baseResult;
        }

        // Dial-a-yield increases damage
        int damage = baseResult.getDamageDealt() + yieldBonus;
        int penetrated = baseResult.getDamagePenetrated() + yieldBonus;
        
        return new FireResult(true, damage, penetrated,
                "Photon torpedo hit with + " + yieldBonus + " yield for " + damage + " damage");
    }

    /**
     * Resolve all weapons for a ship against all valid targets.
     */
    public List<FireResult> resolveAllWeapons(SfbShip ship, List<SfbShip> targets) {
        List<FireResult> results = new ArrayList<>();
        
        for (SfbWeaponBank weapon : ship.getWeaponBanks()) {
            if (!weapon.isOperational()) continue;

            // Find the closest target in firing arc
            SfbShip bestTarget = null;
            int bestDistance = Integer.MAX_VALUE;

            for (SfbShip target : targets) {
                if (target.isDestroyed() || target.isCrippled()) continue;
                
                int direction = ship.getPosition().relativeDirection(target.getPosition());
                if (weapon.canFireAt(ship.getPosition().getFacing(), direction)) {
                    int distance = ship.getPosition().distanceTo(target.getPosition());
                    if (distance < bestDistance) {
                        bestDistance = distance;
                        bestTarget = target;
                    }
                }
            }

            if (bestTarget != null) {
                RangeBand rangeBand = calculateRangeBand(ship, bestTarget);
                boolean overload = weapon.isOverloaded();
                FireResult result = resolveDirectFire(ship, bestTarget, weapon, rangeBand, overload);
                results.add(result);
            }
        }

        return results;
    }

    /**
     * Launch a plasma torpedo.
     */
    public SfbSeekingWeapon launchPlasmaTorpedo(SfbShip ship, SfbShip target, int warheadStrength) {
        SfbSeekingWeapon torpedo = new SfbSeekingWeapon(
                SfbSeekingWeapon.SeekingWeaponType.PLASMA_TORPEDO, ship, target, warheadStrength);
        ship.launchSeekingWeapon(torpedo);
        return torpedo;
    }

    /**
     * Launch a drone.
     */
    public SfbSeekingWeapon launchDrone(SfbShip ship, SfbShip target, SfbSeekingWeapon.DroneType droneType) {
        SfbSeekingWeapon drone = new SfbSeekingWeapon(droneType, ship, target);
        ship.launchSeekingWeapon(drone);
        return drone;
    }

    /**
     * Update all seeking weapons for a ship (pursuit, degradation, hit resolution).
     */
    public List<FireResult> updateSeekingWeapons(SfbShip ship, List<SfbShip> targets) {
        List<FireResult> results = new ArrayList<>();
        
        Iterator<SfbSeekingWeapon> iterator = ship.getLaunchedWeapons().iterator();
        while (iterator.hasNext()) {
            SfbSeekingWeapon weapon = iterator.next();
            
            if (!weapon.isActive()) {
                iterator.remove();
                continue;
            }

            // Check if target is still valid
            SfbShip target = weapon.getTarget();
            boolean targetValid = false;
            for (SfbShip t : targets) {
                if (t.getId().equals(target.getId())) {
                    targetValid = true;
                    if (t.isDestroyed()) {
                        // Target destroyed, weapon explodes harmlessly
                        weapon.deactivate();
                        iterator.remove();
                    }
                    break;
                }
            }

            if (!targetValid) {
                // Target no longer in combat, weapon continues but may be intercepted
                weapon.pursue();
                weapon.degrade();
                if (weapon.getWarheadStrength() <= 0) {
                    weapon.deactivate();
                    iterator.remove();
                }
                continue;
            }

            // Pursue target
            weapon.pursue();
            
            // Check for hit (when weapon reaches target)
            if (weapon.isAtTarget()) {
                int damage = weapon.getWarheadStrength();
                int direction = ship.getPosition().relativeDirection(target.getPosition());
                int penetrated = target.takeDamage(damage, direction);
                
                results.add(new FireResult(true, damage, penetrated,
                        weapon.getType().getName() + " hits for " + damage + " damage"));
                weapon.deactivate();
                iterator.remove();
            } else {
                // Degrade warhead over distance
                weapon.degrade();
                if (weapon.getWarheadStrength() <= 0) {
                    weapon.deactivate();
                    iterator.remove();
                }
            }
        }

        return results;
    }
}
