package com.yisy.modules.doramcu.zero_zero_seven.dto;

import com.yisy.modules.doramcu.zero_zero_seven.model.BondGameSession;

public class BondGameSessionDto {

    private Long id;
    private String name;
    private int mapId;
    private int currentTurn;
    private int currentYear;
    private long mapSeed;

    public BondGameSessionDto() {
    }

    public BondGameSessionDto(BondGameSession session) {
        this.id = session.getId();
        this.name = session.getName();
        this.mapId = session.getMapId();
        this.currentTurn = session.getCurrentTurn();
        this.currentYear = session.getCurrentYear();
        this.mapSeed = session.getMapSeed();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getMapId() { return mapId; }
    public void setMapId(int mapId) { this.mapId = mapId; }

    public int getCurrentTurn() { return currentTurn; }
    public void setCurrentTurn(int currentTurn) { this.currentTurn = currentTurn; }

    public int getCurrentYear() { return currentYear; }
    public void setCurrentYear(int currentYear) { this.currentYear = currentYear; }

    public long getMapSeed() { return mapSeed; }
    public void setMapSeed(long mapSeed) { this.mapSeed = mapSeed; }
}
