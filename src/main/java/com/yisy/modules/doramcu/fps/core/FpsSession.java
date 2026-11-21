package com.yisy.modules.doramcu.fps.core;
import com.yisy.modules.doramcu.model.players.Player;

import com.yisy.modules.doramcu.fps.core.model.FpsMap;
import com.yisy.modules.doramcu.fps.core.model.FpsMatch;
import com.yisy.modules.doramcu.fps.core.model.FpsWeapon;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Represents an active FPS multiplayer match session.
 */
public class FpsSession {

    public enum SessionState {
        WAITING, READY, IN_PROGRESS, FINISHED
    }

    private String sessionId;
    private SessionState state;
    private FpsMatch.GameMode gameMode;
    private FpsMap map;
    private Map<String, FpsPlayerState> players;
    private List<FpsWeapon> availableWeapons;
    private List<SessionEvent> events;
    private long currentTick;
    private Instant startTime;
    private Instant endTime;
    private int maxPlayers;
    private int respawnDelaySeconds;
    private String theme;
    private Map<String, Long> respawnTimers;

    // Capture the Point state
    private Map<String, CapturePointState> capturePointStates;
    private Map<Integer, Integer> teamScores;
    private static final int CAPTURE_RADIUS = 10;
    private static final int SCORE_THRESHOLD = 1000;
    private static final double CAPTURE_RATE = 2.0;

    public FpsSession() {
    }

    public FpsSession(String sessionId, FpsMatch.GameMode gameMode, FpsMap map, int maxPlayers) {
        this.sessionId = sessionId;
        this.state = SessionState.WAITING;
        this.gameMode = gameMode;
        this.map = map;
        this.maxPlayers = maxPlayers;
        this.players = new ConcurrentHashMap<>();
        this.availableWeapons = new ArrayList<>();
        this.events = new CopyOnWriteArrayList<>();
        this.respawnTimers = new ConcurrentHashMap<>();
        this.respawnDelaySeconds = 3;
        this.currentTick = 0;
        this.capturePointStates = new ConcurrentHashMap<>();
        this.teamScores = new ConcurrentHashMap<>();

        this.availableWeapons.add(new FpsWeapon("Pistol", FpsWeapon.WeaponType.PISTOL, 25, 4.0, 1.5, 15));
        this.availableWeapons.add(new FpsWeapon("Assault Rifle", FpsWeapon.WeaponType.RIFLE, 20, 10.0, 2.0, 30));
        this.availableWeapons.add(new FpsWeapon("Shotgun", FpsWeapon.WeaponType.SHOTGUN, 50, 1.5, 2.5, 8));
    }

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    public SessionState getState() { return state; }
    public void setState(SessionState state) { this.state = state; }
    public FpsMatch.GameMode getGameMode() { return gameMode; }
    public void setGameMode(FpsMatch.GameMode gameMode) { this.gameMode = gameMode; }
    public FpsMap getMap() { return map; }
    public void setMap(FpsMap map) { this.map = map; }
    public Map<String, FpsPlayerState> getPlayers() { return players; }
    public List<FpsWeapon> getAvailableWeapons() { return availableWeapons; }
    public void setAvailableWeapons(List<FpsWeapon> availableWeapons) { this.availableWeapons = availableWeapons; }
    public List<SessionEvent> getEvents() { return events; }
    public long getCurrentTick() { return currentTick; }
    public void setCurrentTick(long currentTick) { this.currentTick = currentTick; }
    public Instant getStartTime() { return startTime; }
    public void setStartTime(Instant startTime) { this.startTime = startTime; }
    public Instant getEndTime() { return endTime; }
    public void setEndTime(Instant endTime) { this.endTime = endTime; }
    public int getMaxPlayers() { return maxPlayers; }
    public void setMaxPlayers(int maxPlayers) { this.maxPlayers = maxPlayers; }
    public int getRespawnDelaySeconds() { return respawnDelaySeconds; }
    public void setRespawnDelaySeconds(int respawnDelaySeconds) { this.respawnDelaySeconds = respawnDelaySeconds; }
    public String getTheme() { return theme; }
    public void setTheme(String theme) { this.theme = theme; }

    public boolean addPlayer(String playerId, String playerName, int team) {
        if (players.size() >= maxPlayers) return false;
        if (players.containsKey(playerId)) return false;

        FpsPlayerState playerState = new FpsPlayerState(playerId, playerName, team);

        if (map != null && !map.getSpawnPoints().isEmpty()) {
            List<FpsMap.SpawnPoint> teamSpawns = map.getSpawnPoints().stream()
                    .filter(sp -> sp.team() == team || sp.team() == -1).toList();
            if (teamSpawns.isEmpty()) teamSpawns = map.getSpawnPoints();
            if (!teamSpawns.isEmpty()) {
                FpsMap.SpawnPoint spawn = teamSpawns.get(playerId.hashCode() % teamSpawns.size());
                playerState.setPosition(spawn.x(), spawn.y() + 1.7, spawn.z());
                playerState.setGroundY(spawn.y());
            }
        }

        if (!availableWeapons.isEmpty()) {
            playerState.setCurrentWeaponId(availableWeapons.get(0).getId());
        }

        players.put(playerId, playerState);
        addEvent(new SessionEvent(SessionEventType.PLAYER_JOINED, playerId, playerName, null));
        return true;
    }

    public void removePlayer(String playerId) {
        FpsPlayerState removed = players.remove(playerId);
        if (removed != null) {
            addEvent(new SessionEvent(SessionEventType.PLAYER_LEFT, playerId, removed.getPlayerName(), null));
            respawnTimers.remove(playerId);
        }
    }

    public int getPlayerCount() { return players.size(); }
    public boolean isFull() { return players.size() >= maxPlayers; }

    public void startMatch() {
        this.state = SessionState.IN_PROGRESS;
        this.startTime = Instant.now();
        this.currentTick = 0;
        addEvent(new SessionEvent(SessionEventType.MATCH_STARTED, null, null, null));
        initCapturePoints();
    }

    public void initCapturePoints() {
        if (map == null || map.getCapturePoints() == null) return;
        capturePointStates.clear();
        teamScores.clear();
        for (FpsMap.CapturePoint cp : map.getCapturePoints()) {
            capturePointStates.put(cp.id(), new CapturePointState(cp.id(), -1, 0.0));
        }
        teamScores.put(0, 0);
        teamScores.put(1, 0);
    }

    public void updateCapturePoints() {
        if (map == null || map.getCapturePoints() == null || capturePointStates.isEmpty()) return;

        for (FpsMap.CapturePoint cp : map.getCapturePoints()) {
            CapturePointState state = capturePointStates.get(cp.id());
            if (state == null) continue;

            int team0Count = 0;
            int team1Count = 0;
            for (FpsPlayerState player : players.values()) {
                if (!player.isAlive()) continue;
                double dx = player.getPosX() - cp.x();
                double dz = player.getPosZ() - cp.z();
                double dist = Math.sqrt(dx * dx + dz * dz);
                if (dist <= cp.radius()) {
                    if (player.getTeam() == 0) team0Count++;
                    else if (player.getTeam() == 1) team1Count++;
                }
            }

            int totalNearby = team0Count + team1Count;
            if (totalNearby == 0) {
                if (state.ownerTeam() >= 0) {
                    double newProgress = Math.max(0, state.captureProgress() - (CAPTURE_RATE * 0.5));
                    if (newProgress <= 0) {
                        capturePointStates.put(cp.id(), new CapturePointState(cp.id(), -1, 0.0));
                    } else {
                        capturePointStates.put(cp.id(), new CapturePointState(cp.id(), state.ownerTeam(), newProgress));
                    }
                }
                continue;
            }

            int balance = team0Count - team1Count;

            if (state.ownerTeam() == -1) {
                if (balance == 0) continue;
                double progress = Math.abs(balance) * CAPTURE_RATE;
                int capturingTeam = balance > 0 ? 0 : 1;
                progress = Math.min(100.0, progress);
                if (progress >= 100.0) {
                    capturePointStates.put(cp.id(), new CapturePointState(cp.id(), capturingTeam, 100.0));
                    addEvent(new SessionEvent(SessionEventType.POINT_CAPTURED, null, null, cp.id() + ":" + capturingTeam));
                } else {
                    capturePointStates.put(cp.id(), new CapturePointState(cp.id(), capturingTeam, progress));
                }
            } else if (state.ownerTeam() == 0) {
                if (balance > 0) {
                    capturePointStates.put(cp.id(), new CapturePointState(cp.id(), 0, Math.min(100.0, state.captureProgress() + CAPTURE_RATE)));
                } else if (balance < 0) {
                    double newProgress = Math.max(0, state.captureProgress() + (balance * CAPTURE_RATE));
                    if (newProgress <= 0) {
                        capturePointStates.put(cp.id(), new CapturePointState(cp.id(), 1, 0.0));
                        addEvent(new SessionEvent(SessionEventType.POINT_CAPTURED, null, null, cp.id() + ":1"));
                    } else {
                        capturePointStates.put(cp.id(), new CapturePointState(cp.id(), 0, newProgress));
                    }
                }
            } else {
                if (balance < 0) {
                    capturePointStates.put(cp.id(), new CapturePointState(cp.id(), 1, Math.min(100.0, state.captureProgress() + CAPTURE_RATE)));
                } else if (balance > 0) {
                    double newProgress = Math.max(0, state.captureProgress() - (balance * CAPTURE_RATE));
                    if (newProgress <= 0) {
                        capturePointStates.put(cp.id(), new CapturePointState(cp.id(), 0, 0.0));
                        addEvent(new SessionEvent(SessionEventType.POINT_CAPTURED, null, null, cp.id() + ":0"));
                    } else {
                        capturePointStates.put(cp.id(), new CapturePointState(cp.id(), 1, newProgress));
                    }
                }
            }
        }

        for (CapturePointState cps : capturePointStates.values()) {
            if (cps.ownerTeam() >= 0 && cps.captureProgress() >= 100.0) {
                teamScores.merge(cps.ownerTeam(), 1, Integer::sum);
            }
        }
    }

    public void endMatch() {
        this.state = SessionState.FINISHED;
        this.endTime = Instant.now();
        if (startTime != null) {
            addEvent(new SessionEvent(SessionEventType.MATCH_ENDED, null, null, determineWinner()));
        }
    }

    public void addEvent(SessionEvent event) {
        events.add(event);
        while (events.size() > 100) events.remove(0);
    }

    public void scheduleRespawn(String playerId) {
        respawnTimers.put(playerId, currentTick + (respawnDelaySeconds * 20L));
    }

    public boolean canRespawn(String playerId) {
        Long respawnTick = respawnTimers.get(playerId);
        return respawnTick != null && currentTick >= respawnTick;
    }

    public void respawnPlayer(String playerId) {
        FpsPlayerState player = players.get(playerId);
        if (player == null || player.isAlive()) return;

        if (map != null && !map.getSpawnPoints().isEmpty()) {
            List<FpsMap.SpawnPoint> teamSpawns = map.getSpawnPoints().stream()
                    .filter(sp -> sp.team() == player.getTeam() || sp.team() == -1).toList();
            if (teamSpawns.isEmpty()) teamSpawns = map.getSpawnPoints();
            if (!teamSpawns.isEmpty()) {
                FpsMap.SpawnPoint spawn = teamSpawns.get(playerId.hashCode() % teamSpawns.size());
                FpsWeapon weapon = availableWeapons.isEmpty() ? null : availableWeapons.get(0);
                player.respawn(spawn.x(), spawn.y() + 1.7, spawn.z(), weapon);
            }
        }

        respawnTimers.remove(playerId);
        addEvent(new SessionEvent(SessionEventType.PLAYER_RESPAWNED, playerId, player.getPlayerName(), null));
    }

    private String determineWinner() {
        if (gameMode == FpsMatch.GameMode.DEATHMATCH) {
            FpsPlayerState leader = players.values().stream()
                    .max(Comparator.comparingInt(FpsPlayerState::getKills)).orElse(null);
            return leader != null ? leader.getPlayerId() : null;
        } else if (gameMode == FpsMatch.GameMode.TEAM_DEATHMATCH) {
            Map<Integer, Integer> teamKills = new HashMap<>();
            for (FpsPlayerState p : players.values()) {
                teamKills.merge(p.getTeam(), p.getKills(), Integer::sum);
            }
            return teamKills.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(e -> "team:" + e.getKey()).orElse(null);
        } else if (gameMode == FpsMatch.GameMode.CAPTURE_THE_POINT) {
            if (teamScores != null && !teamScores.isEmpty()) {
                return teamScores.entrySet().stream()
                        .max(Map.Entry.comparingByValue())
                        .map(e -> "team:" + e.getKey()).orElse(null);
            }
            if (capturePointStates != null && !capturePointStates.isEmpty()) {
                Map<Integer, Long> teamPoints = new HashMap<>();
                for (CapturePointState cps : capturePointStates.values()) {
                    if (cps.ownerTeam() >= 0) teamPoints.merge(cps.ownerTeam(), 1L, Long::sum);
                }
                return teamPoints.entrySet().stream()
                        .max(Map.Entry.comparingByValue())
                        .map(e -> "team:" + e.getKey()).orElse(null);
            }
        }
        return null;
    }

    public FpsMatch toMatchRecord() {
        FpsMatch match = new FpsMatch(gameMode, map != null ? map.getId() : null, map != null ? map.getName() : null);
        match.setTheme(theme);
        match.setStartTime(startTime);
        match.setEndTime(endTime);
        if (startTime != null && endTime != null) {
            match.setDurationSeconds(java.time.Duration.between(startTime, endTime).getSeconds());
        }
        for (FpsPlayerState p : players.values()) {
            match.getPlayerScores().add(new FpsMatch.PlayerScore(
                    p.getPlayerId(), p.getPlayerName(), p.getTeam(),
                    p.getKills(), p.getDeaths(), p.getAssists()));
        }
        match.setWinner(determineWinner());
        return match;
    }

    public record SessionEvent(SessionEventType type, String playerId, String playerName, String data) {
    }

    public enum SessionEventType {
        PLAYER_JOINED, PLAYER_LEFT, MATCH_STARTED, MATCH_ENDED,
        PLAYER_KILLED, PLAYER_RESPAWNED, WEAPON_PICKED_UP, POINT_CAPTURED
    }

    public record CapturePointState(String pointId, int ownerTeam, double captureProgress) {
    }

    public Map<String, CapturePointState> getCapturePointStates() { return capturePointStates; }
    public Map<Integer, Integer> getTeamScores() { return teamScores; }
    public static int getCaptureRadius() { return CAPTURE_RADIUS; }
    public static int getScoreThreshold() { return SCORE_THRESHOLD; }
}
