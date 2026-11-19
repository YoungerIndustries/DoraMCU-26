package com.yisy.modules.doramcu.mechwarrior.engine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Core MechWarrior FPS game engine implementing all BattleTech mechanics.
 * <p>
 * This service manages the game state, processes combat actions, handles heat management,
 * component-based damage, stability, and weapon cooldowns.
 * </p>
 * <p>
 * Key mechanics implemented:
 * <ul>
 *   <li>Component-based damage (head, torso, arms, legs)</li>
 *   <li>Heat management with override system</li>
 *   <li>Weapon cooldowns and grouping (alpha strike, chain fire)</li>
 *   <li>Stability and knockdown system</li>
 *   <li>Called shots on specific components</li>
 *   <li>Pilot skills affecting combat performance</li>
 *   <li>Jump jets with heat cost</li>
 *   <li>Torso twist for independent aiming</li>
 * </ul>
 * </p>
 */
@Service
public class MechWarriorGameEngine {

    private static final Logger log = LoggerFactory.getLogger(MechWarriorGameEngine.class);

    // Game constants
    private static final double HEAT_DISSIPATION_PER_TICK = 2.0;  // heat units per tick
    private static final double HEAT_OVERRIDE_PENALTY = 1.5;      // heat penalty per tick when overriding
    private static final int HEAT_SHUTDOWN_THRESHOLD = 100;
    private static final double STABILITY_DAMAGE_PER_HIT = 5.0;
    private static final double KNOCKDOWN_THRESHOLD = 50.0;
    private static final double JUMP_JET_HEAT_COST = 10.0;

    // Active game sessions
    private final Map<String, MechWarriorSession> sessions = new ConcurrentHashMap<>();

    /**
     * Create a new MechWarrior game session.
     */
    public MechWarriorSession createSession(String sessionId, String mapName, int maxPlayers) {
        MechWarriorSession session = new MechWarriorSession(sessionId, mapName, maxPlayers);
        sessions.put(sessionId, session);
        log.info("Created MechWarrior session {} with map {}", sessionId, mapName);
        return session;
    }

    /**
     * Add a pilot with their mech to a session.
     */
    public MechWarriorMech addPilotToSession(String sessionId, MechPilot pilot, MechChassis chassis) {
        MechWarriorSession session = sessions.get(sessionId);
        if (session == null) {
            throw new IllegalArgumentException("Session not found: " + sessionId);
        }
        if (session.getPlayerCount() >= session.getMaxPlayers()) {
            throw new IllegalStateException("Session is full");
        }

        MechWarriorMech mech = new MechWarriorMech(pilot, chassis);
        session.addMech(pilot.getId(), mech);
        log.info("Added pilot {} with mech {} to session {}", pilot.getName(), chassis.getName(), sessionId);
        return mech;
    }

    /**
     * Process a combat action (firing weapons).
     */
    public CombatResult fireWeapons(String sessionId, String pilotId, List<String> weaponIds,
                                     MechComponent.ComponentLocation targetLocation, String targetPilotId) {
        MechWarriorSession session = sessions.get(sessionId);
        if (session == null) {
            throw new IllegalArgumentException("Session not found: " + sessionId);
        }

        MechWarriorMech shooter = session.getMech(pilotId);
        MechWarriorMech target = session.getMech(targetPilotId);

        if (shooter == null || target == null) {
            throw new IllegalArgumentException("Mech not found");
        }

        CombatResult result = new CombatResult();
        result.setShooterPilotId(pilotId);
        result.setTargetPilotId(targetPilotId);

        // Calculate total heat from firing
        int totalHeat = 0;
        int totalDamage = 0;

        for (String weaponId : weaponIds) {
            MechWeapon weapon = shooter.getWeapon(weaponId);
            if (weapon == null || weapon.isOnCooldown(shooter.getLastFireTime())) {
                continue;
            }

            // Check heat before firing
            if (shooter.getHeatLevel() + weapon.getHeatGenerated() > HEAT_SHUTDOWN_THRESHOLD) {
                if (!shooter.isHeatOverride()) {
                    result.addMessage("Cannot fire " + weapon.getName() + ": would cause shutdown");
                    continue;
                }
            }

            // Fire the weapon
            totalHeat += weapon.getHeatGenerated();
            shooter.setHeatLevel(shooter.getHeatLevel() + weapon.getHeatGenerated());
            weapon.setCooldownTime(weapon.getCooldownTime()); // Start cooldown

            // Calculate hit chance
            double hitChance = calculateHitChance(shooter, target, weapon);
            boolean hit = Math.random() < hitChance;

            if (hit) {
                // Apply damage
                int damage = weapon.getDamage();

                // Apply pilot skill bonuses
                double accuracyBonus = shooter.getPilot().getAccuracyBonus(weapon.getType());
                damage = (int) (damage * (1.0 + accuracyBonus * 0.1));

                // Apply target's shielding bonus
                double shieldingBonus = target.getPilot().getDamageReductionBonus();
                damage = (int) (damage * (1.0 - shieldingBonus));

                // Determine target component
                MechComponent.ComponentLocation hitLocation = targetLocation;
                if (hitLocation == null) {
                    hitLocation = determineHitLocation(shooter, target);
                }

                // Apply damage to component
                MechComponent component = target.getComponent(hitLocation);
                if (component != null) {
                    int overflow = component.applyDamage(damage);
                    totalDamage += damage;

                    // Check for critical hits
                    if (weapon.isCritSeeking() && Math.random() < 0.3) {
                        result.addCriticalHit(hitLocation.toString());
                    }

                    // Apply stability damage
                    double stabilityDamage = STABILITY_DAMAGE_PER_HIT * (damage / 10.0);
                    target.addStabilityDamage(stabilityDamage);

                    // Check for knockdown
                    if (target.getStabilityDamage() >= KNOCKDOWN_THRESHOLD) {
                        target.setKnockedDown(true);
                        result.setTargetKnockedDown(true);
                    }
                }

                result.addHit(weapon.getName(), hitLocation.toString(), damage);
            } else {
                result.addMiss(weapon.getName());
            }
        }

        // Update heat
        shooter.setHeatLevel(Math.max(0, shooter.getHeatLevel() - totalHeat * 0.1));

        // Check for shutdown
        if (shooter.getHeatLevel() >= HEAT_SHUTDOWN_THRESHOLD && !shooter.isHeatOverride()) {
            shooter.setShutdown(true);
            result.setShooterShutdown(true);
        }

        // Check for reactor explosion
        if (shooter.isHeatOverride() && shooter.getHeatLevel() >= HEAT_SHUTDOWN_THRESHOLD * 1.5) {
            if (Math.random() < 0.1) {
                shooter.setDestroyed(true);
                result.setShooterExploded(true);
            }
        }

        result.setTotalDamage(totalDamage);
        result.setTotalHeat(totalHeat);

        log.debug("Combat result: {} fired at {}, damage={}, heat={}",
                shooter.getPilot().getName(), target.getPilot().getName(), totalDamage, totalHeat);

        return result;
    }

    /**
     * Process a jump jet action.
     */
    public void useJumpJets(String sessionId, String pilotId, double distance) {
        MechWarriorSession session = sessions.get(sessionId);
        if (session == null) return;

        MechWarriorMech mech = session.getMech(pilotId);
        if (mech == null || mech.isKnockedDown() || mech.isShutdown()) return;

        // Calculate heat cost
        double heatCost = JUMP_JET_HEAT_COST * (distance / 100.0);
        mech.setHeatLevel(mech.getHeatLevel() + heatCost);

        // Update position
        mech.setPosX(mech.getPosX() + distance);

        // Check for shutdown
        if (mech.getHeatLevel() >= HEAT_SHUTDOWN_THRESHOLD) {
            mech.setShutdown(true);
        }

        log.debug("Pilot {} used jump jets, heat cost={}", mech.getPilot().getName(), heatCost);
    }

    /**
     * Process a tick (game loop update).
     */
    public void processTick(String sessionId) {
        MechWarriorSession session = sessions.get(sessionId);
        if (session == null) return;

        for (MechWarriorMech mech : session.getAllMechs().values()) {
            // Dissipate heat
            double heatReduction = HEAT_DISSIPATION_PER_TICK;

            // Apply pilot's heat management skill bonus
            double heatBonus = mech.getPilot().getHeatReductionBonus();
            heatReduction *= (1.0 + heatBonus);

            mech.setHeatLevel(Math.max(0, mech.getHeatLevel() - heatReduction));

            // Apply heat override penalty
            if (mech.isHeatOverride()) {
                mech.setHeatLevel(mech.getHeatLevel() + HEAT_OVERRIDE_PENALTY);
            }

            // Check for shutdown recovery
            if (mech.isShutdown() && mech.getHeatLevel() < HEAT_SHUTDOWN_THRESHOLD * 0.8) {
                mech.setShutdown(false);
            }

            // Recover stability
            if (!mech.isKnockedDown()) {
                mech.setStabilityDamage(Math.max(0, mech.getStabilityDamage() - 1.0));
            }

            // Update weapon cooldowns
            for (MechWeapon weapon : mech.getAllWeapons()) {
                weapon.updateCooldown();
            }
        }

        session.incrementTick();
    }

    /**
     * Calculate hit chance based on range, movement, and pilot skills.
     */
    private double calculateHitChance(MechWarriorMech shooter, MechWarriorMech target, MechWeapon weapon) {
        double baseChance = 0.7; // 70% base hit chance

        // Range modifier
        double distance = shooter.getDistanceTo(target);
        double rangeModifier = 1.0 - (distance / weapon.getRange());
        rangeModifier = Math.max(0.1, Math.min(1.0, rangeModifier));

        // Movement modifier
        double movementModifier = 1.0;
        if (target.isMoving()) {
            movementModifier = 0.8;
        }

        // Pilot skill modifier
        double skillModifier = shooter.getPilot().getAccuracyBonus(weapon.getType());

        // Target's evasion modifier
        double evasionModifier = 1.0 - target.getPilot().getStabilityBonus();

        // Calculate final hit chance
        double hitChance = baseChance * rangeModifier * movementModifier * (1.0 + skillModifier) * evasionModifier;
        return Math.max(0.1, Math.min(0.95, hitChance));
    }

    /**
     * Determine which component is hit based on aim direction.
     */
    private MechComponent.ComponentLocation determineHitLocation(MechWarriorMech shooter, MechWarriorMech target) {
        // Simplified: random hit location weighted by component size
        double random = Math.random();

        if (random < 0.05) return MechComponent.ComponentLocation.HEAD; // 5% head
        if (random < 0.30) return MechComponent.ComponentLocation.CENTER_TORSO; // 25% center torso
        if (random < 0.45) return MechComponent.ComponentLocation.LEFT_TORSO; // 15% left torso
        if (random < 0.60) return MechComponent.ComponentLocation.RIGHT_TORSO; // 15% right torso
        if (random < 0.72) return MechComponent.ComponentLocation.LEFT_ARM; // 12% left arm
        if (random < 0.84) return MechComponent.ComponentLocation.RIGHT_ARM; // 12% right arm
        if (random < 0.92) return MechComponent.ComponentLocation.LEFT_LEG; // 8% left leg
        return MechComponent.ComponentLocation.RIGHT_LEG; // 8% right leg
    }

    /**
     * Get a session by ID.
     */
    public MechWarriorSession getSession(String sessionId) {
        return sessions.get(sessionId);
    }

    /**
     * List all active sessions.
     */
    public List<MechWarriorSession> getActiveSessions() {
        return new ArrayList<>(sessions.values());
    }

    /**
     * Remove a session.
     */
    public void removeSession(String sessionId) {
        sessions.remove(sessionId);
        log.info("Removed MechWarrior session {}", sessionId);
    }

    /**
     * Result of a combat action.
     */
    public static class CombatResult {
        private String shooterPilotId;
        private String targetPilotId;
        private int totalDamage;
        private int totalHeat;
        private boolean shooterShutdown;
        private boolean shooterExploded;
        private boolean targetKnockedDown;
        private List<String> hits = new ArrayList<>();
        private List<String> misses = new ArrayList<>();
        private List<String> criticalHits = new ArrayList<>();
        private List<String> messages = new ArrayList<>();

        public void addHit(String weapon, String location, int damage) {
            hits.add(weapon + " hit " + location + " for " + damage + " damage");
        }

        public void addMiss(String weapon) {
            misses.add(weapon + " missed");
        }

        public void addCriticalHit(String location) {
            criticalHits.add("Critical hit on " + location);
        }

        public void addMessage(String message) {
            messages.add(message);
        }

        // Getters and Setters
        public String getShooterPilotId() { return shooterPilotId; }
        public void setShooterPilotId(String shooterPilotId) { this.shooterPilotId = shooterPilotId; }
        public String getTargetPilotId() { return targetPilotId; }
        public void setTargetPilotId(String targetPilotId) { this.targetPilotId = targetPilotId; }
        public int getTotalDamage() { return totalDamage; }
        public void setTotalDamage(int totalDamage) { this.totalDamage = totalDamage; }
        public int getTotalHeat() { return totalHeat; }
        public void setTotalHeat(int totalHeat) { this.totalHeat = totalHeat; }
        public boolean isShooterShutdown() { return shooterShutdown; }
        public void setShooterShutdown(boolean shooterShutdown) { this.shooterShutdown = shooterShutdown; }
        public boolean isShooterExploded() { return shooterExploded; }
        public void setShooterExploded(boolean shooterExploded) { this.shooterExploded = shooterExploded; }
        public boolean isTargetKnockedDown() { return targetKnockedDown; }
        public void setTargetKnockedDown(boolean targetKnockedDown) { this.targetKnockedDown = targetKnockedDown; }
        public List<String> getHits() { return hits; }
        public List<String> getMisses() { return misses; }
        public List<String> getCriticalHits() { return criticalHits; }
        public List<String> getMessages() { return messages; }
    }
}
