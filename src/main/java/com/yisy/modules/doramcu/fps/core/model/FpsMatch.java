package com.yisy.modules.doramcu.fps.core.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Records a completed FPS match with players, scores, and results.
 */
public class FpsMatch {

    public enum GameMode {
        DEATHMATCH, TEAM_DEATHMATCH, CAPTURE_THE_POINT
    }

    public record PlayerScore(String playerId, String playerName, int team, int kills, int deaths, int assists) {
    }

    private String id;
    private GameMode mode;
    private String mapId;
    private String mapName;
    private List<PlayerScore> playerScores;
    private long durationSeconds;
    private String winner;
    private Instant startTime;
    private Instant endTime;
    private String theme;

    public FpsMatch() {
        this.playerScores = new ArrayList<>();
    }

    public FpsMatch(GameMode mode, String mapId, String mapName) {
        this.id = UUID.randomUUID().toString();
        this.mode = mode;
        this.mapId = mapId;
        this.mapName = mapName;
        this.playerScores = new ArrayList<>();
        this.startTime = Instant.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public GameMode getMode() { return mode; }
    public void setMode(GameMode mode) { this.mode = mode; }
    public String getMapId() { return mapId; }
    public void setMapId(String mapId) { this.mapId = mapId; }
    public String getMapName() { return mapName; }
    public void setMapName(String mapName) { this.mapName = mapName; }
    public List<PlayerScore> getPlayerScores() { return playerScores; }
    public void setPlayerScores(List<PlayerScore> playerScores) { this.playerScores = playerScores; }
    public long getDurationSeconds() { return durationSeconds; }
    public void setDurationSeconds(long durationSeconds) { this.durationSeconds = durationSeconds; }
    public String getWinner() { return winner; }
    public void setWinner(String winner) { this.winner = winner; }
    public Instant getStartTime() { return startTime; }
    public void setStartTime(Instant startTime) { this.startTime = startTime; }
    public Instant getEndTime() { return endTime; }
    public void setEndTime(Instant endTime) { this.endTime = endTime; }
    public String getTheme() { return theme; }
    public void setTheme(String theme) { this.theme = theme; }
}
