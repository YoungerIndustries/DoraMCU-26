package com.yisy.modules.doramcu.fullthrust.service;

import org.springframework.stereotype.Service;

import java.util.*;

/**
 * AI player service for FullThrust.
 * Makes decisions for AI-controlled players: movement, targeting, and weapon firing.
 * Uses heuristic-based strategy: approach enemies, maintain optimal range, fire when possible.
 * Supports difficulty levels: easy (makes mistakes), medium (balanced), hard (plays optimally).
 */
@Service
public class AIService {
    private final Random random = new Random();

    /**
     * Process a complete turn for an AI player.
     * Moves all ships and fires weapons as appropriate.
     * Difficulty affects decision quality: easy makes random mistakes, hard plays optimally.
     */
    public void processAITurn(GameSession session) {
        String playerId = session.getCurrentPlayerId();
        Player player = session.getPlayers().get(playerId);
        if (player == null || !player.isAI()) {
            return;
        }

        String difficulty = player.getAiDifficulty();
        if (difficulty == null) difficulty = "medium";

        List<Ship> myShips = session.getShipsForPlayer(playerId);
        List<Ship> enemies = getEnemyShips(session, playerId);

        session.logEvent("🤖 " + player.getName() + " (AI - " + difficulty.toUpperCase() + ") is thinking...");

        for (Ship ship : myShips) {
            if (ship.isDestroyed() || enemies.isEmpty()) {
                continue;
            }

            // Decide movement
            AIMovement movement = decideMovement(session, ship, enemies, difficulty);
            moveShipInSession(session, ship.getId(), movement.headingChange, movement.thrust, player.getName());

            // Decide whether to fire
            decideFire(session, ship, enemies, difficulty, player.getName());
        }
    }

    /**
     * Move a ship within the session (duplicate of GameService logic for AI use).
     */
    private void moveShipInSession(GameSession session, String shipId, int headingChange, int thrust, String playerName) {
        Ship ship = session.getShips().get(shipId);
        if (ship == null || ship.isDestroyed()) return;

        ShipClass shipClass = session.getShipClasses().get(ship.getShipClassId());
        if (shipClass == null) return;

        // Apply heading change
        int maxHeadingChange = shipClass.getMaxHeadingChange();
        headingChange = Math.max(-maxHeadingChange, Math.min(maxHeadingChange, headingChange));
        int newHeading = (ship.getHeading() + headingChange + 6) % 6;
        ship.setHeading(newHeading);

        // Apply thrust
        int maxAccel = shipClass.getAcceleration();
        int maxDeaccel = shipClass.getDeceleration();
        thrust = Math.max(-maxDeaccel, Math.min(maxAccel, thrust));

        int newSpeed = ship.getSpeed() + thrust;
        newSpeed = Math.max(0, Math.min(shipClass.getMaxSpeed(), newSpeed));
        ship.setSpeed(newSpeed);

        // Move the ship
        if (newSpeed > 0 && ship.getCurrentFuel() > 0) {
            int[] dq = {1, 1, 0, -1, -1, 0};
            int[] dr = {0, 1, 1, 0, -1, -1};

            HexPosition currentPos = ship.getPosition();
            int newQ = currentPos.getQ() + dq[newHeading] * newSpeed;
            int newR = currentPos.getR() + dr[newHeading] * newSpeed;

            if (session.getBoard().isValidPosition(new HexPosition(newQ, newR))) {
                currentPos.setQ(newQ);
                currentPos.setR(newR);
                ship.setCurrentFuel(Math.max(0, ship.getCurrentFuel() - 1));
            } else {
                newSpeed = 0;
                ship.setSpeed(0);
            }
        }

        session.logEvent("🤖 " + ship.getName() + " moved (heading=" + newHeading + ", speed=" + newSpeed + ")");
    }

    /**
     * Fire weapons for a ship at enemies.
     * Difficulty affects accuracy: easy may miss, hard always hits.
     */
    private void decideFire(GameSession session, Ship ship, List<Ship> enemies, String difficulty, String playerName) {
        if (ship.isDestroyed()) return;

        // Find the best target for each weapon
        for (int i = 0; i < ship.getWeapons().size(); i++) {
            Weapon weapon = ship.getWeapons().get(i);
            if (weapon.isDisabled() || weapon.isReloading()) continue;

            // Easy AI sometimes skips firing even when it could
            if ("easy".equals(difficulty) && random.nextInt(100) < 30) {
                session.logEvent("🤖 " + ship.getName() + " hesitated and didn't fire");
                continue;
            }

            // Find best enemy in this weapon's arc and range
            Ship bestTarget = null;
            int bestScore = Integer.MIN_VALUE;

            for (Ship enemy : enemies) {
                if (enemy.isDestroyed()) continue;

                int distance = ship.getPosition().distanceTo(enemy.getPosition());
                if (distance > weapon.getRange()) continue;

                int directionToTarget = ship.getPosition().directionTo(enemy.getPosition());
                int relativeDirection = (directionToTarget - ship.getHeading() + 6) % 6;
                // Allow targets within ±1 direction of the weapon's arc (60-degree cone)
                int arcDiff = Math.abs(relativeDirection - weapon.getArc());
                arcDiff = Math.min(arcDiff, 6 - arcDiff);
                if (arcDiff > 1) continue;

                // Score: prefer closer, lower-hull targets
                int score = (20 - distance) * 10 + (enemy.getMaxHull() - enemy.getCurrentHull());
                // Hard AI prioritizes finishing off wounded ships
                if ("hard".equals(difficulty)) {
                    score += (enemy.getMaxHull() - enemy.getCurrentHull()) * 5;
                }
                if (score > bestScore) {
                    bestScore = score;
                    bestTarget = enemy;
                }
            }

            if (bestTarget != null) {
                fireWeaponInSession(session, ship.getId(), bestTarget.getId(), i, difficulty, playerName);
            }
        }
    }

    /**
     * Fire a weapon within the session (duplicate of GameService logic for AI use).
     * Difficulty affects damage: easy does reduced damage, hard does bonus damage.
     */
    private void fireWeaponInSession(GameSession session, String shipId, String targetId, int weaponIndex, String difficulty, String playerName) {
        Ship ship = session.getShips().get(shipId);
        Ship target = session.getShips().get(targetId);
        if (ship == null || target == null || ship.isDestroyed() || target.isDestroyed()) return;

        if (weaponIndex < 0 || weaponIndex >= ship.getWeapons().size()) return;

        Weapon weapon = ship.getWeapons().get(weaponIndex);
        if (weapon.isDisabled() || weapon.isReloading()) return;

        int distance = ship.getPosition().distanceTo(target.getPosition());
        if (distance > weapon.getRange()) return;

        int directionToTarget = ship.getPosition().directionTo(target.getPosition());
        int relativeDirection = (directionToTarget - ship.getHeading() + 6) % 6;
        // Allow targets within ±1 direction of the weapon's arc (60-degree cone)
        int arcDiff = Math.abs(relativeDirection - weapon.getArc());
        arcDiff = Math.min(arcDiff, 6 - arcDiff);
        if (arcDiff > 1) return;

        // Fire the weapon
        weapon.fire();

        if ("laser".equals(weapon.getType())) {
            int damage = calculateDamage(weapon.getDamage(), target.getArmorValue(), difficulty);
            target.takeDamage(damage);
            session.logEvent("🤖 " + ship.getName() + " fired " + weapon.getType() + " at " + target.getName() +
                    " for " + damage + " damage");

            if (target.isDestroyed()) {
                session.logEvent("💥 " + target.getName() + " was destroyed by " + ship.getName() + "!");
            }
        } else {
            String missileId = UUID.randomUUID().toString();
            Missile missile = new Missile(missileId, shipId, ship.getName(),
                    new HexPosition(ship.getPosition().getQ(), ship.getPosition().getR()),
                    weapon.getArc(), weapon.getDamage(), 3);
            session.getActiveMissiles().add(missile);
            session.logEvent("🤖 " + ship.getName() + " launched " + weapon.getType() + " at " + target.getName());
        }
    }

    /**
     * Calculate damage based on weapon power and target armor.
     * Difficulty affects the roll: easy gets -1 bonus, hard gets +1 bonus.
     */
    private int calculateDamage(int weaponDamage, int targetArmor, String difficulty) {
        int roll = random.nextInt(6) + 1;
        int bonus = 0;
        if ("easy".equals(difficulty)) bonus = -1;
        else if ("hard".equals(difficulty)) bonus = 1;
        int damage = Math.max(0, weaponDamage + roll + bonus - targetArmor);
        return damage;
    }

    /**
     * Decide the best movement for a ship.
     * Difficulty affects decision quality: easy may choose wrong heading, hard plays optimally.
     */
    public AIMovement decideMovement(GameSession session, Ship ship, List<Ship> enemies, String difficulty) {
        if (enemies.isEmpty()) {
            // No enemies - drift
            return new AIMovement(0, 0);
        }

        // Find the nearest enemy as primary target
        Ship target = nearestEnemy(ship, enemies);
        if (target == null) {
            return new AIMovement(0, 0);
        }

        int directionToTarget = ship.getPosition().directionTo(target.getPosition());
        int distance = ship.getPosition().distanceTo(target.getPosition());

        // Determine optimal heading change to face the target
        int currentHeading = ship.getHeading();
        int targetHeading = directionToTarget;

        // Calculate shortest heading change to face the target
        int headingDiff = (targetHeading - currentHeading + 6) % 6;
        if (headingDiff > 3) {
            headingDiff -= 6; // Go the other way
        }
        if (headingDiff < -3) {
            headingDiff += 6; // Go the other way (negative)
        }

        // Clamp to ship's max heading change
        ShipClass shipClass = session.getShipClasses().get(ship.getShipClassId());
        int maxHeadingChange = shipClass != null ? shipClass.getMaxHeadingChange() : 2;
        int headingChange = Math.max(-maxHeadingChange, Math.min(maxHeadingChange, headingDiff));

        // Easy AI sometimes makes wrong heading decisions
        if ("easy".equals(difficulty) && random.nextInt(100) < 40) {
            headingChange = -headingChange; // Reverse the heading change
        }

        // Decide thrust
        int thrust = decideThrust(session, ship, distance, target, difficulty, headingChange);

        return new AIMovement(headingChange, thrust);
    }

    /**
     * Decide thrust value based on distance to target and weapon ranges.
     * Difficulty affects aggression: hard AI pushes harder, easy AI is more cautious.
     */
    private int decideThrust(GameSession session, Ship ship, int distance, Ship target, String difficulty, int headingChange) {
        ShipClass shipClass = session.getShipClasses().get(ship.getShipClassId());
        int maxAccel = shipClass != null ? shipClass.getAcceleration() : 2;
        int maxDeaccel = shipClass != null ? shipClass.getDeceleration() : 2;

        // If not facing the target, accelerate to close distance while turning
        boolean isFacingTarget = headingChange == 0;

        if (!isFacingTarget) {
            // Not facing target yet — accelerate toward it
            if ("hard".equals(difficulty)) {
                return maxAccel;
            } else {
                return Math.min(maxAccel, 1);
            }
        }

        // Check if any weapon can actually fire at the target (arc AND range match)
        boolean canFireAnyWeapon = false;
        int minFireRange = Integer.MAX_VALUE;
        for (Weapon weapon : ship.getWeapons()) {
            if (weapon.isDisabled() || weapon.isReloading()) continue;
            if (distance <= weapon.getRange()) {
                // Check arc — since we're facing the target, relative direction is 0
                // Only forward-facing weapons (arc 0) can fire when facing the target
                if (weapon.getArc() == 0) {
                    canFireAnyWeapon = true;
                    minFireRange = Math.min(minFireRange, weapon.getRange());
                }
            }
        }

        if (canFireAnyWeapon) {
            // Can fire — maintain position or slow down if too close
            if (distance <= minFireRange / 2) {
                // Too close — slow down
                return "hard".equals(difficulty) ? -1 : -maxDeaccel;
            } else {
                // Good range — maintain speed
                return 0;
            }
        } else {
            // Cannot fire any weapon — approach the target
            if ("hard".equals(difficulty)) {
                return maxAccel;
            } else if ("easy".equals(difficulty)) {
                return Math.min(maxAccel, 1);
            } else {
                return Math.min(maxAccel, 1);
            }
        }
    }

    /**
     * Find the nearest enemy to a ship.
     */
    private Ship nearestEnemy(Ship ship, List<Ship> enemies) {
        Ship nearest = null;
        int nearestDist = Integer.MAX_VALUE;

        for (Ship enemy : enemies) {
            if (enemy.isDestroyed()) continue;
            int dist = ship.getPosition().distanceTo(enemy.getPosition());
            if (dist < nearestDist) {
                nearestDist = dist;
                nearest = enemy;
            }
        }
        return nearest;
    }

    /**
     * Get all enemy ships for a player.
     */
    private List<Ship> getEnemyShips(GameSession session, String playerId) {
        List<Ship> enemies = new ArrayList<>();
        for (Ship ship : session.getActiveShips()) {
            if (!ship.getPlayerId().equals(playerId)) {
                enemies.add(ship);
            }
        }
        return enemies;
    }

    /**
     * Simple data class for AI movement decisions.
     */
    public static class AIMovement {
        public final int headingChange;
        public final int thrust;

        public AIMovement(int headingChange, int thrust) {
            this.headingChange = headingChange;
            this.thrust = thrust;
        }
    }
}
