package com.yisy.modules.doramcu.fullthrust.entity;

import jakarta.persistence.*;

/**
 * JPA entity for a game session.
 */
@Entity
@Table(name = "game_sessions")
public class GameSessionEntity {

    @Id
    @Column(length = 36)
    private String id;

    private String name;

    private int currentTurn;

    @Column(length = 36)
    private String currentPlayerId;

    private boolean started;

    private boolean ended;

    @Column(length = 36)
    private String winnerId;

    private int boardWidth = 20;

    private int boardHeight = 20;

    public GameSessionEntity() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getCurrentTurn() { return currentTurn; }
    public void setCurrentTurn(int currentTurn) { this.currentTurn = currentTurn; }
    public String getCurrentPlayerId() { return currentPlayerId; }
    public void setCurrentPlayerId(String currentPlayerId) { this.currentPlayerId = currentPlayerId; }
    public boolean isStarted() { return started; }
    public void setStarted(boolean started) { this.started = started; }
    public boolean isEnded() { return ended; }
    public void setEnded(boolean ended) { this.ended = ended; }
    public String getWinnerId() { return winnerId; }
    public void setWinnerId(String winnerId) { this.winnerId = winnerId; }
    public int getBoardWidth() { return boardWidth; }
    public void setBoardWidth(int boardWidth) { this.boardWidth = boardWidth; }
    public int getBoardHeight() { return boardHeight; }
    public void setBoardHeight(int boardHeight) { this.boardHeight = boardHeight; }
}
