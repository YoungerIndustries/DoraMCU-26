package com.yisy.modules.doramcu.fps.core;
import com.yisy.modules.doramcu.model.players.Player;

import com.yisy.modules.doramcu.fps.core.model.FpsMap;
import com.yisy.modules.doramcu.fps.core.model.FpsMatch;
import com.yisy.modules.doramcu.fps.core.model.FpsWeapon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Core FPS game server that runs the server-authoritative game loop.
 * Framework-agnostic version — no Spring dependencies.
 * <p>
 * Manages active sessions, processes player inputs, updates player states,
 * handles combat (hit detection, damage), and broadcasts state to clients.
 * Runs at a configurable tick rate (default 20Hz = 50ms per tick).
 * </p>
 */
@Component
public class FpsGameServer {

    private static final Logger log = LoggerFactory.getLogger(FpsGameServer.class);

    private static final int DEFAULT_TICK_RATE = 20;
    private static final double GRAVITY = 9.81;
    private static final double WALK_SPEED = 5.0;
    private static final double SPRINT_SPEED = 8.0;
    private static final double CROUCH_SPEED = 2.5;
    private static final double JUMP_VELOCITY = 5.0;
    private static final double PLAYER_HEIGHT = 1.7;
    private static final double CROUCH_HEIGHT = 1.0;

    private final int tickRate;
    private final long tickIntervalMs;

    private final Map<String, FpsSession> sessions;
    private final Map<String, Queue<FpsInputMessage>> pendingInputs;
    private final ScheduledExecutorService gameLoopExecutor;
    private final AtomicBoolean running;

    public FpsGameServer() {
        this(DEFAULT_TICK_RATE);
    }

    public FpsGameServer(int tickRate) {
        this.tickRate = tickRate;
        this.tickIntervalMs = 1000L / tickRate;
        this.sessions = new ConcurrentHashMap<>();
        this.pendingInputs = new ConcurrentHashMap<>();
        this.gameLoopExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "fps-game-loop");
            t.setDaemon(true);
            return t;
        });
        this.running = new AtomicBoolean(false);
    }

    /**
     * Start the game loop. Called by the host application on startup.
     */
    public void start() {
        running.set(true);
        gameLoopExecutor.scheduleAtFixedRate(this::gameTick, 0, tickIntervalMs, TimeUnit.MILLISECONDS);
        log.info("FPS Game Server started with {} Hz tick rate", tickRate);
    }

    /**
     * Stop the game loop. Called by the host application on shutdown.
     */
    public void stop() {
        running.set(false);
        gameLoopExecutor.shutdown();
        log.info("FPS Game Server stopped");
    }

    public FpsSession createSession(String sessionId, FpsMatch.GameMode gameMode, FpsMap map, int maxPlayers) {
        FpsSession session = new FpsSession(sessionId, gameMode, map, maxPlayers);
        sessions.put(sessionId, session);
        pendingInputs.put(sessionId, new ConcurrentLinkedQueue<>());
        log.info("Created FPS session {} with mode {} and map {}", sessionId, gameMode, map != null ? map.getName() : "default");
        return session;
    }

    public void removeSession(String sessionId) {
        FpsSession removed = sessions.remove(sessionId);
        pendingInputs.remove(sessionId);
        if (removed != null) {
            log.info("Removed FPS session {} with {} players", sessionId, removed.getPlayerCount());
        }
    }

    public FpsSession getSession(String sessionId) {
        return sessions.get(sessionId);
    }

    public List<FpsSession> getActiveSessions() {
        return new ArrayList<>(sessions.values());
    }

    public void submitInput(String sessionId, FpsInputMessage input) {
        Queue<FpsInputMessage> queue = pendingInputs.get(sessionId);
        if (queue != null) {
            queue.offer(input);
        }
    }

    private void gameTick() {
        if (!running.get()) return;

        double dt = 1.0 / tickRate;

        for (FpsSession session : sessions.values()) {
            if (session.getState() != FpsSession.SessionState.IN_PROGRESS) continue;

            session.setCurrentTick(session.getCurrentTick() + 1);

            Queue<FpsInputMessage> inputs = pendingInputs.get(session.getSessionId());
            if (inputs != null) {
                FpsInputMessage lastInput = null;
                FpsInputMessage input;
                while ((input = inputs.poll()) != null) {
                    lastInput = input;
                }
                if (lastInput != null) {
                    processInput(session, lastInput, dt);
                }
            }

            for (FpsPlayerState player : session.getPlayers().values()) {
                updatePlayerPhysics(player, dt);
            }

            for (String playerId : new ArrayList<>(session.getPlayers().keySet())) {
                FpsPlayerState player = session.getPlayers().get(playerId);
                if (player != null && !player.isAlive() && session.canRespawn(playerId)) {
                    session.respawnPlayer(playerId);
                }
            }

            if (session.getGameMode() == FpsMatch.GameMode.CAPTURE_THE_POINT) {
                session.updateCapturePoints();
            }

            checkMatchEnd(session);
        }
    }

    private void processInput(FpsSession session, FpsInputMessage input, double dt) {
        FpsPlayerState player = session.getPlayers().get(input.playerId());
        if (player == null || !player.isAlive()) return;

        player.setLastSeenTick(session.getCurrentTick());
        player.setPing(System.currentTimeMillis() - input.timestamp());

        if (input.aimPitch() != 0 || input.aimYaw() != 0) {
            player.setPitch(input.aimPitch());
            player.setYaw(input.aimYaw());
        }

        double moveX = input.moveX();
        double moveZ = input.moveZ();
        boolean sprinting = input.actions().contains("sprint");
        boolean crouching = input.actions().contains("crouch");

        player.setSprinting(sprinting);
        player.setCrouching(crouching);

        double speed = WALK_SPEED;
        if (sprinting) speed = SPRINT_SPEED;
        if (crouching) speed = CROUCH_SPEED;

        double yaw = player.getYaw();
        double forwardX = -Math.sin(yaw);
        double forwardZ = -Math.cos(yaw);
        double rightX = -Math.cos(yaw);
        double rightZ = Math.sin(yaw);

        double velX = (forwardX * moveZ + rightX * moveX) * speed;
        double velZ = (forwardZ * moveZ + rightZ * moveX) * speed;

        player.setVelX(velX);
        player.setVelZ(velZ);

        if (input.actions().contains("jump") && Math.abs(player.getVelY()) < 0.01) {
            player.setVelY(JUMP_VELOCITY);
            player.setJumping(true);
        }

        if (input.actions().contains("shoot")) {
            processShoot(session, player, dt);
        }

        if (input.actions().contains("reload")) {
            processReload(session, player);
        }
    }

    private void updatePlayerPhysics(FpsPlayerState player, double dt) {
        player.setVelY(player.getVelY() - GRAVITY * dt);

        double newX = player.getPosX() + player.getVelX() * dt;
        double newY = player.getPosY() + player.getVelY() * dt;
        double newZ = player.getPosZ() + player.getVelZ() * dt;

        double groundY = player.getGroundY();
        double playerHeight = player.isCrouching() ? CROUCH_HEIGHT : PLAYER_HEIGHT;

        if (newY < groundY + playerHeight) {
            newY = groundY + playerHeight;
            player.setVelY(0);
            player.setJumping(false);
        }

        FpsMap map = null;
        for (FpsSession s : sessions.values()) {
            if (s.getPlayers().containsKey(player.getPlayerId())) {
                map = s.getMap();
                break;
            }
        }
        if (map != null) {
            double halfWidth = map.getMapWidth() / 2.0;
            double halfDepth = map.getMapDepth() / 2.0;
            newX = Math.max(-halfWidth, Math.min(halfWidth, newX));
            newZ = Math.max(-halfDepth, Math.min(halfDepth, newZ));
        }

        player.setPosition(newX, newY, newZ);

        if (Math.abs(player.getVelX()) < 0.1) player.setVelX(0);
        if (Math.abs(player.getVelZ()) < 0.1) player.setVelZ(0);
    }

    private void processShoot(FpsSession session, FpsPlayerState shooter, double dt) {
        if (shooter.isReloading()) return;

        FpsWeapon weapon = null;
        for (FpsWeapon w : session.getAvailableWeapons()) {
            if (w.getId().equals(shooter.getCurrentWeaponId())) {
                weapon = w;
                break;
            }
        }
        if (weapon == null) return;

        double now = session.getCurrentTick() / (double) tickRate;
        double timeSinceLastFire = now - shooter.getLastFireTime();
        double fireInterval = 1.0 / weapon.getFireRate();
        if (timeSinceLastFire < fireInterval) return;

        if (shooter.getAmmo() <= 0) {
            processReload(session, shooter);
            return;
        }

        shooter.setAmmo(shooter.getAmmo() - 1);
        shooter.setLastFireTime(now);

        performHitScan(session, shooter, weapon);

        shooter.setPitch(shooter.getPitch() + (weapon.getRecoilPitch() * Math.PI / 180.0));
        shooter.setYaw(shooter.getYaw() + (weapon.getRecoilYaw() * Math.PI / 180.0 * (Math.random() - 0.5)));
    }

    private void performHitScan(FpsSession session, FpsPlayerState shooter, FpsWeapon weapon) {
        double pitch = shooter.getPitch();
        double yaw = shooter.getYaw();

        double dirX = -Math.sin(yaw) * Math.cos(pitch);
        double dirY = Math.sin(pitch);
        double dirZ = -Math.cos(yaw) * Math.cos(pitch);

        double closestDist = weapon.getRange();
        FpsPlayerState hitTarget = null;

        for (FpsPlayerState target : session.getPlayers().values()) {
            if (target == shooter || !target.isAlive()) continue;

            if ((session.getGameMode() == FpsMatch.GameMode.TEAM_DEATHMATCH
                    || session.getGameMode() == FpsMatch.GameMode.CAPTURE_THE_POINT)
                    && shooter.getTeam() == target.getTeam()) {
                continue;
            }

            double dx = target.getPosX() - shooter.getPosX();
            double dy = target.getPosY() - shooter.getPosY();
            double dz = target.getPosZ() - shooter.getPosZ();
            double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);

            if (dist > closestDist) continue;

            double targetDirX = dx / dist;
            double targetDirY = dy / dist;
            double targetDirZ = dz / dist;

            double dot = dirX * targetDirX + dirY * targetDirY + dirZ * targetDirZ;
            if (dot > 0.866) {
                closestDist = dist;
                hitTarget = target;
            }
        }

        if (hitTarget != null) {
            int damage = weapon.getDamage();
            hitTarget.setHealth(hitTarget.getHealth() - damage);

            session.addEvent(new FpsSession.SessionEvent(
                    FpsSession.SessionEventType.PLAYER_KILLED,
                    shooter.getPlayerId(),
                    shooter.getPlayerName(),
                    hitTarget.getPlayerId()
            ));

            shooter.addKill();
            if (hitTarget.getHealth() <= 0) {
                hitTarget.addDeath();
                session.scheduleRespawn(hitTarget.getPlayerId());
            }
        }
    }

    private void processReload(FpsSession session, FpsPlayerState player) {
        if (player.isReloading()) return;

        FpsWeapon weapon = null;
        for (FpsWeapon w : session.getAvailableWeapons()) {
            if (w.getId().equals(player.getCurrentWeaponId())) {
                weapon = w;
                break;
            }
        }
        if (weapon == null || player.getAmmo() == weapon.getMagSize()) return;

        player.setReloading(true);
        player.setLastReloadTime(session.getCurrentTick() / (double) tickRate);

        final int finalReloadTicks = (int) (weapon.getReloadTime() * tickRate);
        final long finalTickIntervalMs = tickIntervalMs;
        final String finalWeaponId = weapon.getId();
        final int finalMagSize = weapon.getMagSize();
        CompletableFuture.runAsync(() -> {
            try {
                Thread.sleep(finalReloadTicks * finalTickIntervalMs);
                FpsWeapon currentWeapon = null;
                for (FpsWeapon w : session.getAvailableWeapons()) {
                    if (finalWeaponId.equals(w.getId())) {
                        currentWeapon = w;
                        break;
                    }
                }
                if (currentWeapon != null && finalWeaponId.equals(player.getCurrentWeaponId())) {
                    int needed = finalMagSize - player.getAmmo();
                    int available = Math.min(needed, player.getReserveAmmo());
                    player.setAmmo(player.getAmmo() + available);
                    player.setReserveAmmo(player.getReserveAmmo() - available);
                }
                player.setReloading(false);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
    }

    private void checkMatchEnd(FpsSession session) {
        if (session.getGameMode() == FpsMatch.GameMode.TEAM_DEATHMATCH) {
            Map<Integer, Long> aliveTeams = new HashMap<>();
            for (FpsPlayerState p : session.getPlayers().values()) {
                aliveTeams.merge(p.getTeam(), 1L, Long::sum);
            }
            if (aliveTeams.size() <= 1) {
                session.endMatch();
            }
        }

        if (session.getGameMode() == FpsMatch.GameMode.CAPTURE_THE_POINT) {
            Map<String, FpsSession.CapturePointState> cps = session.getCapturePointStates();
            if (cps != null && !cps.isEmpty()) {
                boolean allTeam0 = cps.values().stream()
                        .allMatch(s -> s.ownerTeam() == 0 && s.captureProgress() >= 100.0);
                boolean allTeam1 = cps.values().stream()
                        .allMatch(s -> s.ownerTeam() == 1 && s.captureProgress() >= 100.0);
                if (allTeam0 || allTeam1) {
                    session.endMatch();
                    return;
                }

                Map<Integer, Integer> scores = session.getTeamScores();
                if (scores != null) {
                    for (int score : scores.values()) {
                        if (score >= FpsSession.getScoreThreshold()) {
                            session.endMatch();
                            return;
                        }
                    }
                }
            }
        }

        if (session.getStartTime() != null) {
            long elapsed = java.time.Duration.between(session.getStartTime(), Instant.now()).getSeconds();
            if (elapsed >= 300) {
                session.endMatch();
            }
        }
    }

    /**
     * Player input message from the client.
     */
    public record FpsInputMessage(
            String playerId,
            long timestamp,
            double moveX,
            double moveZ,
            double aimPitch,
            double aimYaw,
            List<String> actions
    ) {
    }
}
