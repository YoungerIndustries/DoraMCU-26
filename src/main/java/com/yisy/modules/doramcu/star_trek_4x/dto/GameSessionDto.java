package com.yisy.modules.doramcu.star_trek_4x.dto;

import com.yisy.modules.doramcu.star_trek_4x.model.GameSession;

public class GameSessionDto {

    private Long id;
    private String name;
    private int galaxyId;
    private int currentTurn;
    private GameSession.GameMode mode;
    private long galaxySeed;

    public GameSessionDto() {
    }

    public GameSessionDto(GameSession session) {
        this.id = session.getId();
        this.name = session.getName();
        this.galaxyId = session.getGalaxyId();
        this.currentTurn = session.getCurrentTurn();
        this.mode = session.getMode();
        this.galaxySeed = session.getGalaxySeed();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getGalaxyId() { return galaxyId; }
    public void setGalaxyId(int galaxyId) { this.galaxyId = galaxyId; }

    public int getCurrentTurn() { return currentTurn; }
    public void setCurrentTurn(int currentTurn) { this.currentTurn = currentTurn; }

    public GameSession.GameMode getMode() { return mode; }
    public void setMode(GameSession.GameMode mode) { this.mode = mode; }

    public long getGalaxySeed() { return galaxySeed; }
    public void setGalaxySeed(long galaxySeed) { this.galaxySeed = galaxySeed; }
}
