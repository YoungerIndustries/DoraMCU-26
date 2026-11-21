package com.yisy.modules.doramcu.mechwarrior.ai;
import com.yisy.modules.doramcu.model.players.Player;

import com.yisy.modules.doramcu.mechwarrior.engine.MechWarriorGameEngine;
import com.yisy.modules.doramcu.mechwarrior.engine.MechWarriorMech;
import com.yisy.modules.doramcu.mechwarrior.engine.MechWarriorSession;
import com.yisy.modules.doramcu.mechwarrior.model.MechChassis;
import com.yisy.modules.doramcu.mechwarrior.model.MechComponent;
import com.yisy.modules.doramcu.mechwarrior.model.MechPilot;
import com.yisy.modules.doramcu.mechwarrior.model.MechWeapon;
import com.yisy.modules.doramcu.mechwarrior.model.Mission;
import com.yisy.modules.doramcu.mechwarrior.service.MissionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * AI controller for enemy mechs in MechWarrior missions.
 * <p>
 * Manages enemy spawning, behavior loops (targeting, firing, movement),
 * and heat management for AI-controlled mechs. Each enemy has a behavior
 * tick that runs alongside the game tick.
 * </p>
 * <p>
 * AI behavior includes:
 * <ul>
 *   <li>Target selection: closest or most damaged enemy</li>
 *   <li>Weapon selection: choose weapons based on range and cooldown</li>
 *   <li>Heat management: avoid shutdown, use override when desperate</li>
 *   <li>Movement: advance toward target or retreat when low on health</li>
 *   <li>Role-specific tactics: scouts flank, assault mechs charge, snipers keep distance</li>
 * </ul>
 * </p>
 */
@Service
public class EnemyAI {

    private static final Logger log = LoggerFactory.getLogger(EnemyAI.class);

    // AI behavior constants
    private static final double CLOSE_RANGE_THRESHOLD = 200.0;
    private static final double MEDIUM_RANGE_THRESHOLD = 500.0;
    private static final double LONG_RANGE_THRESHOLD = 1000.0;
    private static final double RETREAT_HEALTH_THRESHOLD = 0.3;
    private static final double HEAT_WARNING_THRESHOLD = 0.7;
    private static final double HEAT_OVERRIDE_THRESHOLD = 0.9;

    private final MechWarriorGameEngine gameEngine;
    private final MissionService missionService;

    // Track all active enemies: sessionId -> list of enemy pilot IDs
    private final Map<String, List<String>> sessionEnemies = new ConcurrentHashMap<>();

    // Track enemy state: pilotId -> EnemyState
    private final Map<String, EnemyState> enemyStates = new ConcurrentHashMap<>();

    public EnemyAI(MechWarriorGameEngine gameEngine, MissionService missionService) {
        this.gameEngine = gameEngine;
        this.missionService = missionService;
    }

    /**
     * Spawn enemies for a mission based on the mission's enemy count and difficulty.
     */
    public List<EnemyPilot> spawnEnemies(String sessionId, String missionId) {
        // Get mission details from MissionService
        int enemyCount = 3; // default
        int difficulty = 3; // default

        List<Mission> missions = missionService.getAllMissions();
        for (Mission mission : missions) {
            if (missionId.equals(mission.getId())) {
                enemyCount = mission.getEnemyCount();
                difficulty = mission.getDifficulty().ordinal() + 1;
                break;
            }
        }

        // Cap enemy count
        enemyCount = Math.min(enemyCount, 10);

        List<EnemyPilot> enemies = new ArrayList<>();
        List<String> enemyIds = new ArrayList<>();
        Random rand = new Random();

        // Generate role distribution based on enemy count
        EnemyPilot.EnemyRole[] roles = distributeRoles(enemyCount);

        // Enemy name pools by faction
        String[] enemyNames = {
            "Kowalski", "Volkov", "Tanaka", "Chen", "Mueller",
            "Petrov", "Sato", "Wang", "Schmidt", "Ivanov",
            "Nakamura", "Zhang", "Weber", "Petrov", "Kim",
            "Yamamoto", "Li", "Fischer", "Sidorov", "Park"
        };

        for (int i = 0; i < enemyCount; i++) {
            EnemyPilot.EnemyRole role = roles[i];
            String name = enemyNames[rand.nextInt(enemyNames.length)] + " " + (i + 1);
            EnemyPilot enemy = new EnemyPilot(name, role, difficulty);
            enemies.add(enemy);
            enemyIds.add(enemy.getPilot().getId());
        }

        sessionEnemies.put(sessionId, enemyIds);
        log.info("Spawned {} enemies for session {}", enemyCount, sessionId);
        return enemies;
    }

    /**
     * Register spawned enemies with the game engine.
     */
    public void registerEnemies(String sessionId, List<EnemyPilot> enemies) {
        for (EnemyPilot enemy : enemies) {
            // Add enemy to session
            MechWarriorMech mech = gameEngine.addPilotToSession(
                    sessionId, enemy.getPilot(), enemy.getChassis());

            if (mech == null) {
                log.warn("Failed to register enemy {} - game engine returned null", enemy.getPilot().getName());
                continue;
            }

            // Mount weapons on the mech
            List<MechWeapon> weapons = enemy.getWeapons();
            if (weapons != null) {
                for (MechWeapon weapon : weapons) {
                    if (weapon == null) continue;
                    // Distribute weapons across torso and arms
                    MechComponent.ComponentLocation location = selectWeaponLocation(mech, weapon);
                    mech.mountWeapon(weapon, location);
                }
            }

            // Set initial position (spread out)
            Random rand = new Random();
            mech.setPosX(500 + (rand.nextDouble() * 200));
            mech.setPosY(rand.nextDouble() * 400);

            // Track enemy state
            EnemyState state = new EnemyState();
            state.setTargetPilotId(null);
            state.setBehaviorTick(0);
            state.setLastFireTime(System.currentTimeMillis());
            state.setMoveDirection(rand.nextDouble() * Math.PI * 2);
            enemyStates.put(enemy.getPilot().getId(), state);

            log.debug("Registered enemy {} with {} weapons", enemy.getPilot().getName(), enemy.getWeapons().size());
        }
    }

    /**
     * Process AI behavior for all enemies in a session.
     * Called each game tick.
     */
    public void processAITick(String sessionId) {
        List<String> enemyIds = sessionEnemies.get(sessionId);
        if (enemyIds == null || enemyIds.isEmpty()) {
            return;
        }

        MechWarriorSession session = gameEngine.getSession(sessionId);
        if (session == null) {
            return;
        }

        // Find all player mechs (non-enemy)
        List<String> playerIds = new ArrayList<>();
        for (Map.Entry<String, MechWarriorMech> entry : session.getAllMechs().entrySet()) {
            String pilotId = entry.getKey();
            if (!enemyIds.contains(pilotId)) {
                MechWarriorMech mech = entry.getValue();
                if (mech.isOperational()) {
                    playerIds.add(pilotId);
                }
            }
        }

        if (playerIds.isEmpty()) {
            return; // No players to fight
        }

        // Process each enemy
        for (String enemyId : enemyIds) {
            MechWarriorMech enemyMech = session.getMech(enemyId);
            if (enemyMech == null || !enemyMech.isOperational()) {
                continue;
            }

            EnemyState state = enemyStates.get(enemyId);
            if (state == null) {
                continue;
            }

            state.setBehaviorTick(state.getBehaviorTick() + 1);

            // 1. Select or update target
            selectTarget(enemyMech, playerIds, state);

            // 2. Decide action based on role and situation
            decideAction(sessionId, enemyMech, state);

            // 3. Update movement
            updateMovement(enemyMech, state);
        }
    }

    /**
     * Select a target for the enemy.
     */
    private void selectTarget(MechWarriorMech enemyMech, List<String> playerIds, EnemyState state) {
        MechWarriorSession session = gameEngine.getSession(
                sessionEnemies.keySet().stream()
                        .filter(k -> sessionEnemies.get(k).contains(enemyMech.getPilot().getId()))
                        .findFirst().orElse(null));
        if (session == null) return;

        // Check if current target is still valid
        if (state.getTargetPilotId() != null) {
            MechWarriorMech target = session.getMech(state.getTargetPilotId());
            if (target != null && target.isOperational()) {
                return; // Target still valid
            }
        }

        // Select closest target
        double minDistance = Double.MAX_VALUE;
        String closestTarget = null;

        for (String playerId : playerIds) {
            MechWarriorMech player = session.getMech(playerId);
            if (player == null || !player.isOperational()) continue;

            double distance = enemyMech.getDistanceTo(player);
            if (distance < minDistance) {
                minDistance = distance;
                closestTarget = playerId;
            }
        }

        state.setTargetPilotId(closestTarget);
    }

    /**
     * Decide what action the enemy should take.
     */
    private void decideAction(String sessionId, MechWarriorMech enemyMech, EnemyState state) {
        if (state.getTargetPilotId() == null) return;

        MechWarriorSession session = gameEngine.getSession(sessionId);
        if (session == null) return;

        MechWarriorMech target = session.getMech(state.getTargetPilotId());
        if (target == null || !target.isOperational()) return;

        double distance = enemyMech.getDistanceTo(target);
        EnemyPilot.EnemyRole role = getEnemyRole(enemyMech);

        // Check heat situation
        double heatRatio = enemyMech.getHeatLevel() / 100.0;

        // If heat is critical, don't fire
        if (heatRatio > HEAT_OVERRIDE_THRESHOLD && !enemyMech.isHeatOverride()) {
            // Consider heat override if desperate
            if (distance < CLOSE_RANGE_THRESHOLD && role == EnemyPilot.EnemyRole.ASSAULT) {
                enemyMech.setHeatOverride(true);
            }
            return;
        }

        // Role-specific behavior
        switch (role) {
            case ASSAULT:
                // Charge and fire at close range
                if (distance > CLOSE_RANGE_THRESHOLD) {
                    // Advance toward target
                    state.setMoveDirection(calculateAngle(enemyMech, target));
                    enemyMech.setMoving(true);
                } else {
                    enemyMech.setMoving(false);
                    fireAtTarget(sessionId, enemyMech, target);
                }
                break;

            case SNIPER:
                // Keep distance and fire
                if (distance < MEDIUM_RANGE_THRESHOLD) {
                    // Retreat if too close
                    state.setMoveDirection(calculateAngle(enemyMech, target) + Math.PI);
                    enemyMech.setMoving(true);
                } else {
                    enemyMech.setMoving(false);
                    fireAtTarget(sessionId, enemyMech, target);
                }
                break;

            case SCOUT:
                // Flank and harass
                if (distance > LONG_RANGE_THRESHOLD) {
                    state.setMoveDirection(calculateAngle(enemyMech, target));
                    enemyMech.setMoving(true);
                } else {
                    // Circle around
                    state.setMoveDirection(state.getMoveDirection() + 0.3);
                    enemyMech.setMoving(true);
                    if (distance < MEDIUM_RANGE_THRESHOLD) {
                        fireAtTarget(sessionId, enemyMech, target);
                    }
                }
                break;

            case BRAWLER:
                // Close range combat
                if (distance > CLOSE_RANGE_THRESHOLD * 0.5) {
                    state.setMoveDirection(calculateAngle(enemyMech, target));
                    enemyMech.setMoving(true);
                } else {
                    enemyMech.setMoving(false);
                    fireAtTarget(sessionId, enemyMech, target);
                }
                break;

            case SUPPORT:
                // Medium range, provide covering fire
                if (distance > LONG_RANGE_THRESHOLD) {
                    state.setMoveDirection(calculateAngle(enemyMech, target));
                    enemyMech.setMoving(true);
                } else if (distance < MEDIUM_RANGE_THRESHOLD * 0.5) {
                    state.setMoveDirection(calculateAngle(enemyMech, target) + Math.PI);
                    enemyMech.setMoving(true);
                } else {
                    enemyMech.setMoving(false);
                    fireAtTarget(sessionId, enemyMech, target);
                }
                break;
        }

        // Retreat if health is low
        double healthRatio = calculateHealthRatio(enemyMech);
        if (healthRatio < RETREAT_HEALTH_THRESHOLD) {
            state.setMoveDirection(calculateAngle(enemyMech, target) + Math.PI);
            enemyMech.setMoving(true);
        }
    }

    /**
     * Fire weapons at a target.
     */
    private void fireAtTarget(String sessionId, MechWarriorMech enemyMech, MechWarriorMech target) {
        // Get all available weapons
        Collection<MechWeapon> weapons = enemyMech.getAllWeapons();
        if (weapons.isEmpty()) return;

        List<String> weaponIds = new ArrayList<>();
        for (MechWeapon weapon : weapons) {
            // Check if weapon is ready (not on cooldown)
            if (!weapon.isOnCooldown(enemyMech.getLastFireTime())) {
                weaponIds.add(weapon.getId());
            }
        }

        if (weaponIds.isEmpty()) return;

        // Fire weapons
        try {
            // Find a player pilot ID to be the target
            String targetPilotId = findPlayerPilotId(sessionId);
            if (targetPilotId == null) return;

            gameEngine.fireWeapons(
                    sessionId,
                    enemyMech.getPilot().getId(),
                    weaponIds,
                    null, // Random target location
                    targetPilotId
            );

            enemyMech.setLastFireTime(System.currentTimeMillis());
        } catch (Exception e) {
            log.debug("Enemy {} failed to fire: {}", enemyMech.getPilot().getName(), e.getMessage());
        }
    }

    /**
     * Update enemy movement.
     */
    private void updateMovement(MechWarriorMech enemyMech, EnemyState state) {
        if (!enemyMech.isMoving()) {
            return;
        }

        // Move toward the direction
        double speed = enemyMech.getChassis().getMaxSpeed() / 10.0; // Simplified movement
        double dx = Math.cos(state.getMoveDirection()) * speed;
        double dy = Math.sin(state.getMoveDirection()) * speed;

        enemyMech.setPosX(enemyMech.getPosX() + dx);
        enemyMech.setPosY(enemyMech.getPosY() + dy);
        enemyMech.setCurrentSpeed(speed);
    }

    /**
     * Calculate angle from one mech to another.
     */
    private double calculateAngle(MechWarriorMech from, MechWarriorMech to) {
        double dx = to.getPosX() - from.getPosX();
        double dy = to.getPosY() - from.getPosY();
        return Math.atan2(dy, dx);
    }

    /**
     * Calculate health ratio of a mech.
     */
    private double calculateHealthRatio(MechWarriorMech mech) {
        MechComponent centerTorso = mech.getComponent(MechComponent.ComponentLocation.CENTER_TORSO);
        if (centerTorso == null) return 0.0;
        return centerTorso.getIntegrityPercentage() / 100.0;
    }

    /**
     * Select a weapon location for mounting.
     */
    private MechComponent.ComponentLocation selectWeaponLocation(MechWarriorMech mech, MechWeapon weapon) {
        // Prefer torso locations, then arms
        MechComponent.ComponentLocation[] locations = {
                MechComponent.ComponentLocation.LEFT_TORSO,
                MechComponent.ComponentLocation.RIGHT_TORSO,
                MechComponent.ComponentLocation.CENTER_TORSO,
                MechComponent.ComponentLocation.LEFT_ARM,
                MechComponent.ComponentLocation.RIGHT_ARM
        };

        for (MechComponent.ComponentLocation loc : locations) {
            MechComponent component = mech.getComponent(loc);
            if (component != null && component.canMountWeapon(weapon)) {
                return loc;
            }
        }
        return MechComponent.ComponentLocation.CENTER_TORSO;
    }

    /**
     * Get the role of an enemy mech.
     */
    private EnemyPilot.EnemyRole getEnemyRole(MechWarriorMech mech) {
        // Infer role from chassis class
        MechChassis.MechClass mechClass = mech.getChassis().getMechClass();
        switch (mechClass) {
            case LIGHT: return EnemyPilot.EnemyRole.SCOUT;
            case MEDIUM: return EnemyPilot.EnemyRole.BRAWLER;
            case HEAVY: return EnemyPilot.EnemyRole.SNIPER;
            case ASSAULT: return EnemyPilot.EnemyRole.ASSAULT;
            default: return EnemyPilot.EnemyRole.SUPPORT;
        }
    }

    /**
     * Find a player pilot ID in the session.
     */
    private String findPlayerPilotId(String sessionId) {
        List<String> enemyIds = sessionEnemies.get(sessionId);
        MechWarriorSession session = gameEngine.getSession(sessionId);
        if (session == null || enemyIds == null) return null;

        for (Map.Entry<String, MechWarriorMech> entry : session.getAllMechs().entrySet()) {
            if (!enemyIds.contains(entry.getKey())) {
                return entry.getKey();
            }
        }
        return null;
    }

    /**
     * Remove enemies when a session ends.
     */
    public void removeSessionEnemies(String sessionId) {
        List<String> enemyIds = sessionEnemies.remove(sessionId);
        if (enemyIds != null) {
            for (String id : enemyIds) {
                enemyStates.remove(id);
            }
            log.info("Removed {} enemies from session {}", enemyIds.size(), sessionId);
        }
    }

    /**
     * Get all enemy IDs in a session.
     */
    public List<String> getEnemyIds(String sessionId) {
        return sessionEnemies.getOrDefault(sessionId, Collections.emptyList());
    }

    /**
     * Tracks the state of an AI-controlled enemy.
     */
    public static class EnemyState {
        private String targetPilotId;
        private int behaviorTick;
        private long lastFireTime;
        private double moveDirection;

        public String getTargetPilotId() {
            return targetPilotId;
        }

        public void setTargetPilotId(String targetPilotId) {
            this.targetPilotId = targetPilotId;
        }

        public int getBehaviorTick() {
            return behaviorTick;
        }

        public void setBehaviorTick(int behaviorTick) {
            this.behaviorTick = behaviorTick;
        }

        public long getLastFireTime() {
            return lastFireTime;
        }

        public void setLastFireTime(long lastFireTime) {
            this.lastFireTime = lastFireTime;
        }

        public double getMoveDirection() {
            return moveDirection;
        }

        public void setMoveDirection(double moveDirection) {
            this.moveDirection = moveDirection;
        }
    }

    /**
     * Distribute roles among enemies based on count.
     */
    private EnemyPilot.EnemyRole[] distributeRoles(int count) {
        EnemyPilot.EnemyRole[] roles = new EnemyPilot.EnemyRole[count];
        Random rand = new Random();

        // Default distribution: mix of roles
        EnemyPilot.EnemyRole[] rolePool = {
                EnemyPilot.EnemyRole.ASSAULT,
                EnemyPilot.EnemyRole.SNIPER,
                EnemyPilot.EnemyRole.SCOUT,
                EnemyPilot.EnemyRole.BRAWLER,
                EnemyPilot.EnemyRole.SUPPORT
        };

        for (int i = 0; i < count; i++) {
            roles[i] = rolePool[rand.nextInt(rolePool.length)];
        }
        return roles;
    }
}
